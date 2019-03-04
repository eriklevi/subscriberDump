package com.example.subscriberDump;

import com.example.subscriberDump.MQTTClient.MQTTSubscriber;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableDiscoveryClient
public class SubscriberDumpApplication {
	public static void main(String[] args) {

		SpringApplication application = new SpringApplication(SubscriberDumpApplication.class);
		final ApplicationContext context = application.run(args);

		MQTTSubscriber subscriber = context.getBean(MQTTSubscriber.class);
	}
}
