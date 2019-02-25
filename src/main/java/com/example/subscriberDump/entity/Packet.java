package com.example.subscriberDump.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("rawPackets")
public class Packet {
    @Id
    String id;
    long timestamp;
    String snifferMac;
    String deviceMac;
    boolean global;
    String rawData;

    public Packet(long timestamp, String snifferMac, String deviceMac, String rawData) {
        this.timestamp = timestamp;
        this.snifferMac = snifferMac;
        this.deviceMac = deviceMac;
        char letter = this.deviceMac.charAt(1);
        if(letter == '0' || letter == '1' || letter == '4' || letter == '5' || letter == '8' || letter == '9' || letter == 'c' || letter == 'd'){
            this.global = true;
        }
        else{
            this.global = false;
        }
        this.rawData = rawData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getSnifferMac() {
        return snifferMac;
    }

    public void setSnifferMac(String snifferMac) {
        this.snifferMac = snifferMac;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }
}
