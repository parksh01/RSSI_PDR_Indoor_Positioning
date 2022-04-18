package org.techtown.rssimeasureapp;

public class ListItem {
    private String device;
    private String address;
    private String rssi;

    public String getDevice(){
        return device;
    }
    public void setDevice(String device) {
        this.device = device;
    }
    public String getAddress(){
        return address;
    }
    public void setAddress(String address){
        this.address = address;
    }
    public String getRssi(){
        return rssi;
    }
    public void setRssi(String rssi){
        this.rssi = rssi;
    }
    ListItem(String device, String address, String rssi){
        this.device = device;
        this.address = address;
        this.rssi = rssi;
    }
}
