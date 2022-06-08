package org.techtown.rssimeasureapp;

import java.util.ArrayList;

public class Beacon implements Comparable<Beacon>{
    public String MACaddress;
    public int beaconNumber;

    private double rssi_A_front;
    private double rssi_n_front;
    private double rssi_A_back;
    private double rssi_n_back;
    public boolean isFront; // true is front, false is back

    public ArrayList<String> rssi;
    public ArrayList<String> rssiKalman;
    public ArrayList<String> distance;
    public int tick;

    private KalmanFilter kf = new KalmanFilter();

    private void setBasicVar(String MACaddress, double A_front, double n_front, double A_back, double n_back, int beaconNumber){
        this.rssi_A_front = A_front;
        this.rssi_n_front = n_front;
        this.rssi_A_back = A_back;
        this.rssi_n_back = n_back;
        this.MACaddress = MACaddress;
        this.beaconNumber = beaconNumber;
        this.rssi = new ArrayList<String>();
        this.rssiKalman = new ArrayList<String>();
        this.distance = new ArrayList<String>();
        this.tick = 0;
    }

    public Beacon(String MACaddress, double A_front, double n_front, double A_back, double n_back, int beaconNumber){
        setBasicVar(MACaddress, A_front, n_front, A_back, n_back, beaconNumber);
        this.isFront = true;
    }

    public Beacon(String MACaddress, double A_front, double n_front, double A_back, double n_back, int beaconNumber, boolean isFront){
        setBasicVar(MACaddress, A_front, n_front, A_back, n_back, beaconNumber);
        this.isFront = isFront;
    }

    public double getCurrentDistance(double rssi){
        if(isFront) return Triangulation.RssiToDistance(rssi, rssi_A_front, rssi_n_front);
        else return Triangulation.RssiToDistance(rssi, rssi_A_back, rssi_n_back);
    }

    public void setRssi(int rssi){
        double filteredRssi = kf.filtering(rssi);
        this.rssi.add(Integer.toString(rssi));
        this.rssiKalman.add(String.format("%.3f",filteredRssi));
        this.distance.add(String.format("%.3f", getCurrentDistance(filteredRssi)));
        this.tick++;
    }

    @Override
    public int compareTo(Beacon beacon) {
        if(beacon.beaconNumber < this.beaconNumber) return 1;
        else if(beacon.beaconNumber > this.beaconNumber) return -1;
        return 0;
    }
}
