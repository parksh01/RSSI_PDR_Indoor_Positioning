package com.example.accellocation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import java.util.ArrayList;

import uk.me.berndporr.iirj.Butterworth;


public class AccelLocation implements SensorEventListener{
    TextView view;
    Context context;
    SensorManager manager;
    AccelLocation accelLocation;
    int tick;
    public float accX, accY, accZ, prevAccX, prevAccY, prevAccZ;
    public float velX, velY, velZ, prevVelX, prevVelY, prevVelZ;
    public float dispX, dispY, dispZ;
    private float alpha = 0.8f;

    private long before, after;
    final private int tickThresh = 300;
    private float interval;
    Butterworth butterworth;
    ArrayList<KalmanFilter> kf;

    AccelLocation(TextView view, SensorManager manager, AccelLocation accelLocation, Context context){
        this.view = view;
        this.context = context;
        this.manager = manager;
        this.accelLocation = accelLocation;
        this.tick = 0;
        this.butterworth = new Butterworth();
        butterworth.highPass(4, 500, 200);
        Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        kf = new ArrayList<KalmanFilter>();
        for(int i=0;i<3;i++){
            kf.add(new KalmanFilter(0.1, 0.15));
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            interval = (float)Math.abs(before - System.nanoTime()) / 1000000000;
            before = System.nanoTime();
            prevAccX = accX;
            prevAccY = accY;
            prevAccZ = accZ;
            accX = (float) kf.get(0).filtering(sensorEvent.values[0]);
            accY = (float) kf.get(1).filtering(sensorEvent.values[1]);
            accZ = (float) kf.get(2).filtering(sensorEvent.values[2]);

            this.tick++;

            if(tick < tickThresh){
                this.view.setText("tick : " + this.tick);
            }
            else{
                prevVelX = velX;
                prevVelY = velY;
                prevVelZ = velZ;
                velX += (prevAccX + accX) * interval / 2;
                velY += (prevAccY + accY) * interval / 2;
                velZ += (prevAccZ + accZ) * interval / 2;

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
