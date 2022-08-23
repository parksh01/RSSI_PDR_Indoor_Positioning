package com.example.accellocation;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class SensorLogWriter {
    private ArrayList<Float> accelX;
    private ArrayList<Float> accelY;
    private ArrayList<Float> accelZ;
    private ArrayList<Float> angleX;
    private ArrayList<Float> angleY;
    private ArrayList<Float> angleZ;
    private ArrayList<String> tag;
    // stop, stopLeft, stopRight, move, moveLeft, moveRight
    public String currentTag;

    private String extension = ".csv";
    private int tick;

    private ArrayList<Float> coordX;
    private ArrayList<Float> coordY;

    SensorLogWriter(){
        this.accelX = new ArrayList<Float>();
        this.accelY = new ArrayList<Float>();
        this.accelZ = new ArrayList<Float>();
        this.angleX = new ArrayList<Float>();
        this.angleY = new ArrayList<Float>();
        this.angleZ = new ArrayList<Float>();
        this.tag = new ArrayList<String>();
        this.currentTag = "stop";
        this.tick = 0;

        this.coordX = new ArrayList<Float>();
        this.coordY = new ArrayList<Float>();
    }
    public void addValue(float accelX, float accelY, float accelZ, float angleX, float angleY, float angleZ){
        this.accelX.add(accelX);
        this.accelY.add(accelY);
        this.accelZ.add(accelZ);
        this.angleX.add(angleX);
        this.angleY.add(angleY);
        this.angleZ.add(angleZ);
        this.tag.add(this.currentTag);
        this.tick++;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void generate(Context context) {
        String filename = "Sensor Data - " + LocalDate.now() + "-" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH시 mm분 ss초")) + this.extension;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), filename);
        try{
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file, false);
            String str = "accX,accY,accZ,angleX,angleY,angleZ,label\n";
            for(int i = 0;i<this.tick;i++){
                str += Float.toString(this.accelX.get(i)) + ",";
                str += Float.toString(this.accelY.get(i)) + ",";
                str += Float.toString(this.accelZ.get(i)) + ",";
                str += Float.toString(this.angleX.get(i)) + ",";
                str += Float.toString(this.angleY.get(i)) + ",";
                str += Float.toString(this.angleZ.get(i)) + ",";
                str += this.tag.get(i) + "\n";
            }
            writer.write(str);
            writer.close();
            Toast.makeText(context, "Sensor Log Generated", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addValueCoord(float x, float y){
        this.coordX.add(x);
        this.coordY.add(y);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void generateCoordLog(Context context){
        String filename = "CoordLog - " + LocalDate.now() + "-" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH시 mm분 ss초")) + this.extension;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), filename);
        try{
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file, false);
            String str = "coordX,coordY\n";
            for(int i=0;i<this.coordX.size();i++){
                str += Float.toString(this.coordX.get(i)) + ",";
                str += Float.toString(this.coordY.get(i)) + "\n";
            }
            writer.write(str);
            writer.close();
            Toast.makeText(context, "Coord Log Generated", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
