package org.techtown.rssimeasureapp;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class Beacon implements Comparable<Beacon>{
    // variables related to beacon's attributions.
    public String MACaddress;
    public int beaconNumber;
    private double rssi_A_front;
    private double rssi_n_front;
    private double rssi_A_back;
    private double rssi_n_back;
    public boolean isFront; // true is front, false is back

    // variables which beacon will keep tracking.
    public ArrayList<String> rssi;
    public ArrayList<String> rssiKalman;
    public ArrayList<String> distance;
    public int tick;
    private KalmanFilter kf = new KalmanFilter();

    // internal method for setting basic variables.
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

    // Constructor, default is isFront=true.
    public Beacon(String MACaddress, double A_front, double n_front, double A_back, double n_back, int beaconNumber){
        setBasicVar(MACaddress, A_front, n_front, A_back, n_back, beaconNumber);
        this.isFront = true;
    }
    public Beacon(String MACaddress, double A_front, double n_front, double A_back, double n_back, int beaconNumber, boolean isFront){
        setBasicVar(MACaddress, A_front, n_front, A_back, n_back, beaconNumber);
        this.isFront = isFront;
    }

    private double getCurrentDistance(double rssi){
        if(isFront) return Triangulation.RssiToDistance(rssi, rssi_A_front, rssi_n_front);
        else return Triangulation.RssiToDistance(rssi, rssi_A_back, rssi_n_back);
    }

    // read rssi and keep log of rssi, filtered rssi and calculated distance.
    public void setRssi(int rssi){
        double filteredRssi = kf.filtering(rssi);
        this.rssi.add(Integer.toString(rssi));
        this.rssiKalman.add(String.format("%.3f",filteredRssi));
        this.distance.add(String.format("%.3f", getCurrentDistance(filteredRssi)));
        this.tick++;
    }

    // read config file and store values of desired devices.
    public static ArrayList<String[]> readConfig(String fileName){
        ArrayList<String[]> beaconList = new ArrayList<String[]>();
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName);
        try{
            if(file.exists()){
                Log.d("configFile", fileName);
                BufferedReader inFile = new BufferedReader(new FileReader(file));
                String sLine = null;
                int i = 1;
                while( (sLine = inFile.readLine()) != null ) {
                    String[] currentBeaconInfo = sLine.split(",");
                    Log.d("configFile", currentBeaconInfo[0]);
                    beaconList.add(currentBeaconInfo);
                    i++;
                }
                inFile.close();
            }
            else{
                Log.d("configFile", "no file");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return beaconList;
    }

    // To use sort method, implement compareTo method.
    // sort by beaconNumber.
    @Override
    public int compareTo(Beacon beacon) {
        if(beacon.beaconNumber < this.beaconNumber) return 1;
        else if(beacon.beaconNumber > this.beaconNumber) return -1;
        return 0;
    }

    // Getters for most recent value
    public String getCurrentRssi(){
        return this.rssi.get(this.tick -1);
    }
    public String getCurrentFilteredRssi(){
        return this.rssiKalman.get(this.tick - 1);
    }
    public String getCurrentDistance(){
        return this.distance.get(this.tick - 1);
    }

}
