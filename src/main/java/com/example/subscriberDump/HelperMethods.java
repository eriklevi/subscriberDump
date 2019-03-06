package com.example.subscriberDump;

import com.example.subscriberDump.entity.TaggedParameter;

import java.util.ArrayList;
import java.util.List;

public class HelperMethods {

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    public static List<TaggedParameter> parseParameters(String taggedParameters){
        List<TaggedParameter> list = new ArrayList<>();
        int i = 0;
        String tag;
        String value;
        int length;
        while(i < taggedParameters.length()){
            tag = taggedParameters.substring(i, i + 2);
            //nel caso di tag dd è necessario specificare OUI (3 byte ) ed il successivo vendor specific OUI type (1 byte)
            if(tag.equals("dd")){
                tag = tag.concat(taggedParameters.substring(i + 4, i + 12));
            }
            i += 2;
            length = Integer.parseInt(taggedParameters.substring(i, i + 2),16);
            i += 2;
            if(length == 0){
                value = "";
            } else{
                value = taggedParameters.substring(i, i + (length * 2));
            }
            list.add(new TaggedParameter(tag, length, value));
            i += length * 2;
        }
        return list;
    }
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
