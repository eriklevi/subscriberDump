package com.example.subscriberDump.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document("rawPacketsFinal")
public class Packet {
    @Id
    private String id;
    private long timestamp;
    private String snifferMac;
    private String deviceMac;
    private String oui;
    private String completeOui;
    private String fingerprintv1;
    private String fingerprintv2;
    private String fingerprintv3;
    private String ssid;
    private int ssidLen;
    private boolean global;
    private String rawData;
    private int sequenceNumber;
    private List<TaggedParameter> taggedParameters;
    private int taggedParametersLength;
    private int year;
    private int month;
    private int weekOfYear; //week of year
    private int dayOfMonth;
    private int dayOfWeek;
    private int hour;
    private int quarter;
    private int tenMinute;
    private int fiveMinute;
    private int minute;

    public Packet(){}

    public Packet(long timestamp, String snifferMac, String deviceMac, boolean global, String rawData, int sequenceNumber, List<TaggedParameter> taggedParameters, int taggedParametersLength) {
        this.timestamp = timestamp;
        this.snifferMac = snifferMac;
        this.deviceMac = deviceMac;
        this.global = global;
        this.rawData = rawData;
        this.sequenceNumber = sequenceNumber;
        this.taggedParameters = taggedParameters;
        this.taggedParametersLength = taggedParametersLength;
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

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public List<TaggedParameter> getTaggedParameters() {
        return taggedParameters;
    }

    public void setTaggedParameters(List<TaggedParameter> taggedParameters) {
        this.taggedParameters = taggedParameters;
    }

    public int getTaggedParametersLength() {
        return taggedParametersLength;
    }

    public void setTaggedParametersLength(int taggedParametersLength) {
        this.taggedParametersLength = taggedParametersLength;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getWeekOfYear() {
        return weekOfYear;
    }

    public void setWeekOfYear(int weekOfYear) {
        this.weekOfYear = weekOfYear;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getQuarter() {
        return quarter;
    }

    public void setQuarter(int quarter) {
        this.quarter = quarter;
    }

    public int getFiveMinute() {
        return fiveMinute;
    }

    public void setFiveMinute(int fiveMinute) {
        this.fiveMinute = fiveMinute;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getTenMinute() {
        return tenMinute;
    }

    public void setTenMinute(int tenMinute) {
        this.tenMinute = tenMinute;
    }

    public String getOui() {
        return oui;
    }

    public void setOui(String oui) {
        this.oui = oui;
    }

    public String getCompleteOui() {
        return completeOui;
    }

    public void setCompleteOui(String completeOui) {
        this.completeOui = completeOui;
    }

    public String getFingerprintv1() {
        return fingerprintv1;
    }

    public void setFingerprintv1(String fingerprintv1) {
        this.fingerprintv1 = fingerprintv1;
    }

    public String getFingerprintv2() {
        return fingerprintv2;
    }

    public void setFingerprintv2(String fingerprintv2) {
        this.fingerprintv2 = fingerprintv2;
    }

    public String getFingerprintv3() {
        return fingerprintv3;
    }

    public void setFingerprintv3(String fingerprintv3) {
        this.fingerprintv3 = fingerprintv3;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getSsidLen() {
        return ssidLen;
    }

    public void setSsidLen(int ssidLen) {
        this.ssidLen = ssidLen;
    }
}
