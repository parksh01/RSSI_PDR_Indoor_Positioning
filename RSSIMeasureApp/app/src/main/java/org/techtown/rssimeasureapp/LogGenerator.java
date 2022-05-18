package org.techtown.rssimeasureapp;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class LogGenerator {
    private ArrayList<ArrayList<String>> distanceData = new ArrayList<ArrayList<String>>();
    private ArrayList<Long> timeStamp = new ArrayList<Long>();
    Timer scheduler;
    LocalTime startTime;
    int size;
    int dataCollected;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public LogGenerator(int size) {
        for(int i=0;i<size;i++){
            distanceData.add(new ArrayList<String>());
        }
        startTime = null;
        this.size = size;
        this.dataCollected = 0;
    }

    // Generating RSSI log from each beacon
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void generateBeaconLog(ListItemAdapter adapter){
        for(int i = 0; i < adapter.getCount(); i++){
            // File name is MAC address of device and current time.
            String fileTitle = adapter.address.get(i).replace(":", "") + "-" + LocalDate.now() + "-" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH시 mm분 ss초")) + ".csv";
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileTitle);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter writer = new FileWriter(file, false);
                double stdev_rssi = 0;
                double avg_rssi = 0;
                int sum_rssi = 0;

                double stdev_rssiKalman = 0;
                double avg_rssiKalman = 0;
                double sum_rssiKalman = 0;

                double stdev_distance = 0;
                double avg_distance = 0;
                double sum_distance = 0;

                // get raw data
                String str = "rssi,rssiKarman,distance" + '\n';
                for(int j = 0; j < adapter.rssi.get(i).size(); j++){
                    str += adapter.rssi.get(i).get(j);
                    sum_rssi += Integer.parseInt(adapter.rssi.get(i).get(j));
                    str += ',';

                    str += adapter.rssiKalman.get(i).get(j);
                    sum_rssiKalman += Double.parseDouble(adapter.rssiKalman.get(i).get(j));
                    str += ',';

                    str += adapter.distance.get(i).get(j);
                    sum_distance += Double.parseDouble(adapter.distance.get(i).get(j));
                    str += '\n';
                }

                // get average
                str += "avg(rssi),avg(rssiKalman),avg(distance)\n";
                avg_rssi = (double)sum_rssi / (double)adapter.rssi.get(i).size();
                avg_rssiKalman = (double)sum_rssiKalman / (double)adapter.rssiKalman.get(i).size();
                avg_distance = sum_distance / (double)adapter.distance.get(i).size();
                str += "" + avg_rssi + ',' + avg_rssiKalman + ',' + avg_distance + '\n';

                // get standard deviation
                for(int j = 0; j < adapter.rssi.get(i).size(); j++){
                    stdev_rssi += Math.pow(avg_rssi - (double)Integer.parseInt(adapter.rssi.get(i).get(j)), 2);
                    stdev_rssiKalman += Math.pow(avg_rssiKalman - Double.parseDouble(adapter.rssiKalman.get(i).get(j)), 2);
                    stdev_distance += Math.pow(avg_distance - Double.parseDouble(adapter.distance.get(i).get(j)), 2);
                }
                stdev_rssi = Math.sqrt(stdev_rssi / (double)adapter.rssi.get(i).size());
                stdev_rssiKalman = Math.sqrt(stdev_rssiKalman / (double)adapter.rssiKalman.get(i).size());
                stdev_distance = Math.sqrt(stdev_distance / (double)adapter.distance.get(i).size());

                str += "stdev(rssi),stdev(rssiKalman),stdev(distance)\n";
                str += "" + stdev_rssi + "," + stdev_rssiKalman + "," + stdev_distance;

                // close the file
                writer.write(str);
                writer.close();
            } catch (IOException e) {

            }
        }
    }

    // Starting logging distance data from each beacon.
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startLogging(ListItemAdapter adapter, int interval){
        if(startTime == null){
            startTime = LocalTime.now();
        }
        scheduler = new Timer();
        TimerTask task = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                for(int i=0;i<size;i++){
                    String beaconName = "Beacon #" + (i + 1);
                    if(adapter.device.contains(beaconName)){
                        distanceData.get(i).add(adapter.getTopItem(adapter.distance.get(i)));
                    }
                    else{
                        distanceData.get(i).add("null");
                    }
                    timeStamp.add(ChronoUnit.MILLIS.between(startTime, LocalTime.now()));
                }
                Log.v("test", "" + (ChronoUnit.MILLIS.between(startTime, LocalTime.now()) + "/" + distanceData.get(0).get(distanceData.get(0).size() - 1) + "/" + distanceData.get(1).get(distanceData.get(1).size() - 1) + "/" + distanceData.get(2).get(distanceData.get(2).size() - 1)));
                dataCollected++;
            }
        };
        scheduler.scheduleAtFixedRate(task, interval, interval);
    }

    public void stopLogging(){
        scheduler.cancel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void generateDistanceLog(ListItemAdapter adapter){
        // make new file
        String fileTitle = "Distance Log - " + LocalDate.now() + "-" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH시 mm분 ss초")) + ".csv";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileTitle);
        try{
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file, false);

            // Write log on file.
            // First row is device name.
            String str = "time,";
            for(int i= 0;i<size;i++){
                str += "Beacon #" + (i + 1);
                if(i != size - 1){
                    str += ',';
                }
                else{
                    str += '\n';
                }
            }

            // and write raw data
            for(int i = 0;i<dataCollected;i++){
                str += timeStamp.get(i) + ',';
                for(int j = 0;j<size;j++){
                    str += distanceData.get(j).get(i);
                    if(j != size - 1){
                        str += ',';
                    }
                    else{
                        str += '\n';
                    }
                }
            }

            // Finalize the file.
            writer.write(str);
            writer.close();

        } catch(IOException e){

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void clear() {
        startTime = null;
        for(int i=0;i<size;i++){
            distanceData.get(i).clear();
        }
        timeStamp.clear();
        dataCollected = 0;
    }
}
