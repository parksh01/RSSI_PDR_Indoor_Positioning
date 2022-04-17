package org.techtown.rssimeasureapp;

public class ListItem {
    private String device;
    private String rssi;

    public String getDevice(){
        return device;
    }
    public void setDevice(String device) {
        this.device = device;
    }
    public String getRssi(){
        return rssi;
    }
    public void setRssi(String rssi){
        this.rssi = rssi;
    }
    ListItem(String device, String rssi){
        this.device = device;
        this.rssi = rssi;
    }
}
