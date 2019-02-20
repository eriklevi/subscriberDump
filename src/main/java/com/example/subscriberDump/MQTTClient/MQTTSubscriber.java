package com.example.subscriberDump.MQTTClient;

import org.eclipse.paho.client.mqttv3.logging.Logger;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;

public interface MQTTSubscriber {

    public void subscribeMessage(String topic);
    public void disconnect();
}
