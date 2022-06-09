package org.techtown.rssimeasureapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

    public static ArrayList<Beacon> readConfig(String fileName){
        ArrayList<Beacon> beaconList = new ArrayList<Beacon>();
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
                    boolean isFront = true;
                    if(currentBeaconInfo[5].equals("1")){
                        isFront = true;
                    }
                    else if(currentBeaconInfo[5].equals("0")){
                        isFront = false;
                    }
                    beaconList.add(new Beacon(currentBeaconInfo[0],
                            Double.parseDouble(currentBeaconInfo[1]),
                            Double.parseDouble(currentBeaconInfo[2]),
                            Double.parseDouble(currentBeaconInfo[3]),
                            Double.parseDouble(currentBeaconInfo[4]), i,isFront));
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

    @Override
    public int compareTo(Beacon beacon) {
        if(beacon.beaconNumber < this.beaconNumber) return 1;
        else if(beacon.beaconNumber > this.beaconNumber) return -1;
        return 0;
    }
}
