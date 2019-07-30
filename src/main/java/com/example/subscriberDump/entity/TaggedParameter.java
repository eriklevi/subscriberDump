package com.example.subscriberDump.entity;

public class TaggedParameter {
    String tag;
    String oui;
    String completeOui;
    int length;
    String value;


    public TaggedParameter() {
    }

    public TaggedParameter(String tag, int length, String value) {
        this.tag = tag;
        this.length = length;
        this.value = value;
        this.oui = null;
        this.completeOui = null;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
}
