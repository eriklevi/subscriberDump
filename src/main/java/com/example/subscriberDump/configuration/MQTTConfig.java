package com.example.subscriberDump.configuration;

public abstract class MQTTConfig {
    protected String broker = "192.168.1.44"; //da mettere aposto con eureka
    protected int qos = 0;
    protected Boolean hasSSL = false;
    protected Integer port = 1883;
    protected String TCP = "tcp://";
    protected final String userName = "testUserName";
    protected final String password = "demoPassword";

    /**
     * Allow to use a custom configuration
     * @param broker
     * @param port
     * @param ssl
     * @param withUsernameAndPassword
     */
    protected abstract void config(String broker, Integer port, Boolean ssl, Boolean withUsernameAndPassword);

    /**
     * Default configuration
     */
    protected abstract void config();
}
