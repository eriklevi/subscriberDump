package com.example.subscriberDump.MQTTClient;

import com.example.subscriberDump.HelperMethods;
import com.example.subscriberDump.entity.Packet;
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
import java.util.List;

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

    @Autowired
    public MQTTSubscriber(PacketsRepository packetsRepository, DiscoveryClient discoveryClient){
        this.packetsRepository = packetsRepository;
        this.discoveryClient = discoveryClient;
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
            Packet p = new Packet(Instant.now().toEpochMilli(), snifferMac, deviceMac, global, rawData, sequenceNumber, HelperMethods.parseParameters(rawData), payload.length() - 68);
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
