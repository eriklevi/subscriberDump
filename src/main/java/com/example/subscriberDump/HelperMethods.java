package com.example.subscriberDump;

import com.example.subscriberDump.entity.TaggedParameter;

import java.util.ArrayList;
import java.util.List;

public class HelperMethods {

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    /**
     * This function is used to parse the parameters inside the probe request frame.
     *
     * @param taggedParameters A string containing the probe request frame payload as hex characters, starting from 00...
     * @return A list containing all the tags as TaggedParameter object
     */
    public static List<TaggedParameter> parseParameters(String taggedParameters){
        List<TaggedParameter> list = new ArrayList<>();
        int i = 0;
        String tag;
        String value;
        int length;
        while(i < taggedParameters.length()){
            tag = taggedParameters.substring(i, i + 2);
            //in this case it's necessary to indicate the OUI (3 byte ) and the subsequent vendor specific OUI type (1 byte)
            if(tag.equals("dd")){
                tag = tag.concat(taggedParameters.substring(i + 4, i + 12));
            }
            i += 2;
            length = Integer.parseInt(taggedParameters.substring(i, i + 2),16);
            i += 2;
            if(length == 0){
                value = "";
            } else{
                /*
                In this case we have to consider the 00 tag (Specified ssid)
                If the tag has a length > 0 we can convert the value as
                 */
                String hexString = taggedParameters.substring(i, i + (length * 2));
                if(tag.equals("00")){
                    value = hexToAscii(hexString);
                } else{
                    value = hexString;
                }
            }
            TaggedParameter tp = new TaggedParameter(tag, length, value);
            list.add(new TaggedParameter(tag, length, value));
            i += length * 2;
        }
        return list;
    }

    /**
     * Converts a byteArray in a string of corresponding hex characters
     * @param bytes The input byte array
     * @return Byte array as hex string
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Converts a hex characters string in the corresponding ASCII characters
     * @param hexString The string to convert
     * @return Hex string converted to ASCII string
     */
    public static String hexToAscii(String hexString){
        StringBuilder sb = new StringBuilder("");
        for(int i = 0; i < hexString.length(); i += 2 ){
            String str = hexString.substring(i, i+2);
            sb.append((char) Integer.parseInt(str,16));
        }
        return sb.toString();
    }
}
