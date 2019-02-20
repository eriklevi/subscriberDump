package com.example.subscriberDump;

import com.example.subscriberDump.MQTTClient.MQTTSubscriber;
import com.example.subscriberDump.MQTTClient.MQTTSubscriberImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;

@SpringBootApplication
@EnableDiscoveryClient
public class SubscriberDumpApplication {
	public static void main(String[] args) {

		SpringApplication application = new SpringApplication(SubscriberDumpApplication.class);
		final ApplicationContext context = application.run(args);

		MQTTSubscriberImpl subscriber = context.getBean(MQTTSubscriberImpl.class);
		subscriber.subscribeMessage("dump");
	}
}
