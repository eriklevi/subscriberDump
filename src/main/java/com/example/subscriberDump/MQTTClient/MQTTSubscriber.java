package com.example.subscriberDump.MQTTClient;

import com.example.subscriberDump.HelperMethods;
import com.example.subscriberDump.entity.OUI;
import com.example.subscriberDump.entity.Packet;
import com.example.subscriberDump.entity.TaggedParameter;
import com.example.subscriberDump.repository.OUIRepository;
import com.example.subscriberDump.repository.PacketsRepository;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Optional;

@Component
public class MQTTSubscriber implements MqttCallback, DisposableBean, InitializingBean {

    private final PacketsRepository packetsRepository;
    private final DiscoveryClient discoveryClient;

    private static final Logger logger = LoggerFactory.getLogger(MQTTSubscriber.class);

    private String broker;
    @Value("${mqtt.client-id}")
    private String clientId;
    @Value("${mqtt.topic}")
    private String topic;
    @Value("${mqtt.qos}")
    private int qos;
    @Value("${mqtt.ssl}")
    private Boolean hasSSL;
    @Value("${mqtt.auto-reconnect}")
    private Boolean autoReconnect;
    @Value("${mqtt.port}")
    private Integer port;
    @Value("${mqtt.use-credentials}")
    private boolean useCredentials;
    @Value("${mqtt.username}")
    private String userName;
    @Value("${mqtt.password}")
    private String password;
    @Value("${mqtt.keep-alive-seconds}")
    private int keepAliveInterval;

    private MqttClient mqttClient;

    private final OUIRepository ouiRepository;

    @Autowired
    public MQTTSubscriber(PacketsRepository packetsRepository, DiscoveryClient discoveryClient, OUIRepository ouiRepository){
        this.packetsRepository = packetsRepository;
        this.discoveryClient = discoveryClient;
        this.ouiRepository = ouiRepository;
    }

    /**
     * This function creates the MQTT client instance
     */
    private void config() {

        String brokerUrl = "tcp://" + this.broker + ":" + this.port;
        MemoryPersistence persistence = new MemoryPersistence();
        MqttConnectOptions connectionOptions = new MqttConnectOptions();
        try {
            this.mqttClient = new MqttClient(brokerUrl, clientId, persistence);
            connectionOptions.setCleanSession(true);
            connectionOptions.setAutomaticReconnect(autoReconnect); //try to reconnect to server from 1 second after fail up to 2 minutes delay
            if(useCredentials){
                connectionOptions.setUserName(userName);
                connectionOptions.setPassword(password.toCharArray());
            }
            connectionOptions.setKeepAliveInterval(keepAliveInterval);
            connectionOptions.setConnectionTimeout(0); //wait until connection successful or fail
            this.mqttClient.setCallback(this);
            this.mqttClient.connect(connectionOptions);
        } catch (MqttException me) {
            logger.error("resason "+ me.getReasonCode());
            logger.error("message "+ me.getMessage());
            logger.error("cause "+ me.getCause());
            me.printStackTrace();
        }
    }

    private void getBrokerInstance(){
        boolean success = false;
        List<ServiceInstance> instances = null;
        while(!success){
            instances = this.discoveryClient.getInstances("moquette");
            if(instances.size() == 0){
                try{
                    Thread.sleep(2500);
                }
                catch(Exception e){
                    logger.error("Eccezzione nella thread sleep");
                }
                logger.info("Impossible to get moquette instance....trying...");
            } else{
                success = true;
            }
        }
        this.broker = instances.get(0).getHost();
    }

    @Override
    public void connectionLost(Throwable throwable) {
        logger.info("Connection with the broker lost!");
    }

    /**
     * Callback invoked when a new MQTT message is received
     * The payload structure is as follows considering bytes:
     * 6: Sniffer MAC
     * From now effective data found inside the probe request
     * 4: ProbeRequest Header
     * 6: Destination MAC (usually FF:FF:FF:FF:FF:FF)
     * 6: Source MAC (that can be either global or locally administered)
     * 6: BSSID (usually FF:FF:FF:FF:FF:FF)
     * 2: Sequence Number of packet
     * ?: Tagged Parameters
     * last 4: Frame Check Sequence
     *
     * Since all the analysis needed can be made on text data we can serialize the byte into a corresponding string and
     * store it in the db.
     * @param topic
     * @param mqttMessage
     * @throws Exception
     */
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        try {
            String time = new Timestamp(System.currentTimeMillis()).toString();
            logger.info("Arrivato messaggio su topic " + topic +" timestamp "+time);
            String payload = HelperMethods.bytesToHex(mqttMessage.getPayload());
            //serializziamo i dati come string visto che le analisi fatte sono a livello dei caratteri
            String snifferMac = payload.substring(0, 2)+ ":" + payload.substring(2,4) +":"+payload.substring(4, 6)+ ":"+payload.substring(6, 8)+ ":"+payload.substring(8, 10)+ ":"+payload.substring(10, 12);
            String deviceMac = payload.substring(32, 34)+ ":" + payload.substring(34,36) +":"+payload.substring(36, 38)+ ":"+payload.substring(38, 40)+ ":"+payload.substring(40, 42)+ ":"+payload.substring(42, 44);
            char letter = deviceMac.charAt(1);
            boolean global;
            if(letter == '0' || letter == '1' || letter == '4' || letter == '5' || letter == '8' || letter == '9' || letter == 'c' || letter == 'd'){
                global = true;
            }
            else{
                global = false;
            }
//attenzione che il sequence number è formato da 12 bit di sequence number e 4 bit di fragment number, vanno parsati solo i primi 3 caratteri hex  
            int sequenceNumber = Integer.parseInt(payload.substring(58, 60)+payload.charAt(56),16); //il carattere 59 è il fragment number
            String rawData = payload.substring(60, payload.length()-8);
            long now = Instant.now().toEpochMilli();
            Packet p = new Packet(now, snifferMac, deviceMac, global, rawData, sequenceNumber, HelperMethods.parseParameters(rawData), payload.length() - 68);
            LocalDateTime t = Instant.ofEpochMilli(p.getTimestamp()).atZone(ZoneId.of("CET")).toLocalDateTime();
            p.setYear(t.getYear());
            p.setMonth(t.getMonthValue());
            p.setWeekOfYear(t.get(WeekFields.ISO.weekOfYear()));
            p.setDayOfMonth(t.getDayOfMonth());
            p.setDayOfWeek(t.getDayOfWeek().getValue());
            p.setHour(t.getHour());
            p.setQuarter(t.getMinute()/15+1); //raggruppo su 15 minuti 1-4...0-14 15-29.....
            p.setFiveMinute(t.getMinute()/5+1); //raggruppo per 5 minuti 1-12...0-4 5-9.....
            p.setTenMinute(t.getMinute()/10+1);
            p.setMinute(t.getMinute());
            //inserimento oui
            Optional<OUI> optionalOUIDevice = ouiRepository.findByOui(p.getDeviceMac().substring(0, 8));
            if (optionalOUIDevice.isPresent()) {
                p.setOui(optionalOUIDevice.get().getShortName()); //set device mac oui
                p.setCompleteOui(optionalOUIDevice.get().getCompleteName());
            } else {
                p.setOui("Unknown");
                p.setCompleteOui("Unknown");
            }
            // inserimento oui in tag dd
            for(TaggedParameter tp : p.getTaggedParameters()){
                if (tp.getTag().startsWith("dd")) {
                    String m = tp.getTag().substring(2, 8);
                    String newM = ""+m.charAt(0) + m.charAt(1) + ":" + m.charAt(2) + m.charAt(3) + ":" + m.charAt(4) + m.charAt(5);
                    optionalOUIDevice = ouiRepository.findByOui(newM);
                    if (optionalOUIDevice.isPresent()) {
                        tp.
                        TaggedParameterDD taggedParameterDD = new TaggedParameterDD(tp.getTag(), tp.getLength(), tp.getValue(), optionalOUIDevice.get().getShortName(), optionalOUIDevice.get().getCompleteName(), newM);
                        newTags.add(taggedParameterDD);
                    } else {
                        TaggedParameterDD taggedParameterDD = new TaggedParameterDD(tp.getTag(), tp.getLength(), tp.getValue(), "Unknown", "Unknown", newM);
                        newTags.add(taggedParameterDD);
                    }
                } else {
                    newTags.add(tp);
                }
            }

            packetsRepository.save(p);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }

    @Override
    public void destroy() throws Exception {
        this.mqttClient.disconnect();
        logger.info("Shutting down service, disconnecting from the broker");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.getBrokerInstance(); //uses Discovery client so it must be called after eureka setup
        this.config();
        this.mqttClient.subscribe(topic, this.qos);
    }
}
