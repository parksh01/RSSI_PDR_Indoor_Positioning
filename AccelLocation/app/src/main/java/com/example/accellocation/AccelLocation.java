package com.example.accellocation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import java.util.ArrayList;


public class AccelLocation implements SensorEventListener{
    TextView view;
    Context context;
    SensorManager manager;
    int tick;
    public float accX, accY, accZ, prevAccX, prevAccY, prevAccZ, accel, prevAccel;
    public float velX, velY, velZ, prevVelX, prevVelY, prevVelZ, spd, prevSpd;
    public float dispX, dispY, dispZ;

    private long before;
    final private int tickThresh = 300;
    final private int detectThresh = 10;
    private float interval;
    ArrayList<KalmanFilter> kf;

    final private int sensorType = Sensor.TYPE_LINEAR_ACCELERATION;
    private ArrayList<Float> recentVal;

    AccelLocation(TextView view, SensorManager manager, Context context){
        this.view = view;
        this.context = context;
        this.manager = manager;
        this.tick = 0;
        this.recentVal = new ArrayList<Float>();

        Sensor sensor = manager.getDefaultSensor(sensorType);
        kf = new ArrayList<KalmanFilter>();
        for(int i=0;i<3;i++){
            kf.add(new KalmanFilter(1, 10));
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == sensorType){
            interval = (float) Math.abs(before - System.nanoTime()) / 1000000000;
            before = System.nanoTime();
            prevAccX = accX;
            prevAccY = accY;
            prevAccZ = accZ;
            prevAccel = accel;
            accX = sensorEvent.values[0];
            accY = sensorEvent.values[1];
            accZ = sensorEvent.values[2];
            accel = (float) Math.sqrt(accX * accX + accY * accY + accZ * accZ);
            if(recentVal.size() > detectThresh) {
                recentVal.remove(0);
            }
            recentVal.add((float) Math.sqrt(accX*accX + accY*accY + accZ*accZ));
            this.tick++;

            if(tick < tickThresh){
                this.view.setText("tick : " + this.tick);
            }
            else{
                prevVelX = velX;
                prevVelY = velY;
                prevVelZ = velZ;
                prevSpd = spd;
                velX = velX + (prevAccX + accX) * interval / 2;
                velY = velY + (prevAccY + accY) * interval / 2;
                velZ = velZ + (prevAccZ + accZ) * interval / 2;
                spd = spd + (prevAccel + accel) * interval / 2;
                // While not moving
                if(Statistics.stdev(recentVal) < 0.05){
                    velX = 0; velY = 0; velZ = 0; spd = 0;
                }

                dispX += (prevVelX + velX) * interval / 2;
                dispY += (prevVelY + velY) * interval / 2;
                dispZ += (prevVelZ + velZ) * interval / 2;
                this.view.setText("acc : (" + String.format("%.3f", accX) + ", " + String.format("%.3f", accY) + ", " + String.format("%.3f", accZ) + ")\n" +
                        "vel : (" + String.format("%.3f", velX) + ", " + String.format("%.3f", velY) + ", " + String.format("%.3f", velZ) + ")\n" +
                        "disp : (" + String.format("%.3f", dispX) + ", " + String.format("%.3f", dispY) + ", " + String.format("%.3f", dispZ) + ")");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void clear() {
        accX = 0; accY = 0; accZ = 0; prevAccX = 0; prevAccY = 0; prevAccZ = 0;
        velX = 0; velY = 0; velZ = 0; prevVelX = 0; prevVelY = 0; prevVelZ = 0;
        dispX = 0; dispY = 0; dispZ = 0;
        tick = 0;
    }
}
