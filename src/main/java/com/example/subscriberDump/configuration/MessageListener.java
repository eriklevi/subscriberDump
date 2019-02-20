package com.example.subscriberDump.configuration;

import com.example.subscriberDump.MQTTClient.MQTTSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageListener implements Runnable{
    @Autowired
    MQTTSubscriber subscriber;

    @Override
    public void run(){
        while(true){
            subscriber.subscribeMessage("dump");
        }
    }
}
