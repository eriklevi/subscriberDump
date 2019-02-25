package com.example.subscriberDump.MQTTClient;

import com.example.subscriberDump.configuration.MQTTConfig;
import com.example.subscriberDump.entity.Packet;
import com.example.subscriberDump.repository.PacketsRepository;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.sql.Timestamp;
import java.time.Instant;

@Component
public class MQTTSubscriberImpl extends MQTTConfig implements MqttCallback, MQTTSubscriber {

    @Autowired
    private PacketsRepository packetsRepository;

    private static final Logger logger = LoggerFactory.getLogger(MQTTSubscriberImpl.class);

    private String brokerUrl = null;
    final private String colon = ":";
    final private String clientId = "dump";

    private MqttClient mqttClient = null;
    private MqttConnectOptions connectionOptions = null;
    private MemoryPersistence persistence = null;

    public MQTTSubscriberImpl(){
        this.config();
    }

    @Override
    public void subscribeMessage(String topic) {
        try{
            this.mqttClient.subscribe(topic, this.qos);
        } catch (MqttException me){
            me.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        try{
            this.mqttClient.disconnect();
        } catch (MqttException me){
            logger.error("ERROR", me);
        }

    }

    @Override
    protected void config(String broker, Integer port, Boolean ssl, Boolean withUsernameAndPassword) {
        String protocol = this.TCP;
        this.brokerUrl = protocol + this.broker + colon + port;
        this.persistence = new MemoryPersistence();
        this.connectionOptions = new MqttConnectOptions();
        try{
            this.mqttClient = new MqttClient(brokerUrl, clientId, persistence);
            this.connectionOptions.setCleanSession(true);
            if (true == withUsernameAndPassword) {
                if (password != null) {
                    this.connectionOptions.setPassword(this.password.toCharArray());
                }
                if (userName != null) {
                    this.connectionOptions.setUserName(this.userName);
                }
            }
            this.mqttClient.connect(this.connectionOptions);
            this.mqttClient.setCallback(this);
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }

    @Override
    protected void config() {
        this.brokerUrl = this.TCP + this.broker + colon + this.port;
        this.persistence = new MemoryPersistence();
        this.connectionOptions = new MqttConnectOptions();
        try {
            this.mqttClient = new MqttClient(brokerUrl, clientId, persistence);
            this.connectionOptions.setCleanSession(true);
            this.mqttClient.connect(this.connectionOptions);
            this.mqttClient.setCallback(this);
        } catch (MqttException me) {
            me.printStackTrace();
        }
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
        String time = new Timestamp(System.currentTimeMillis()).toString();
        logger.info("Arrivato messaggio su topic " + topic +" timestamp "+time);
        String payload = DatatypeConverter.printHexBinary(mqttMessage.getPayload()).toLowerCase();
        logger.info(payload);
        //serializziamo i dati come string visto che le analisi fatte sono a livello dei caratteri
        String snifferMac = payload.substring(0, 2)+ ":" + payload.substring(2,4) +":"+payload.substring(4, 6)+ ":"+payload.substring(6, 8)+ ":"+payload.substring(8, 10)+ ":"+payload.substring(10, 12);
        String deviceMac = payload.substring(32, 34)+ ":" + payload.substring(34,36) +":"+payload.substring(36, 38)+ ":"+payload.substring(38, 40)+ ":"+payload.substring(40, 42)+ ":"+payload.substring(42, 44);
        System.out.println(Instant.now().toEpochMilli());
        Packet p = new Packet(Instant.now().toEpochMilli(), snifferMac, deviceMac, payload.substring(12));
        packetsRepository.save(p);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
