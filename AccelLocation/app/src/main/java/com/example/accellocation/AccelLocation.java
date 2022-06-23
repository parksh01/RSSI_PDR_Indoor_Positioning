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
    public float accX, accY, accZ, prevaccX, prevaccY, prevaccZ;
    public float velX, velY, velZ, velbiasX, velbiasY, velbiasZ;
    public float dispX, dispY, dispZ;
    private float alpha = 0.8f;

    private long before, after;
    final private int tickThresh = 50;
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
            kf.add(new KalmanFilter(1, 4));
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            interval = (float)Math.abs(before - System.nanoTime()) / 1000000000;
            before = System.nanoTime();
            accX = sensorEvent.values[0];
            accY = sensorEvent.values[1];
            accZ = sensorEvent.values[2];

            this.tick++;

            if(tick < tickThresh){
                this.view.setText("tick : " + this.tick);
            }
            else{
                velX += accX * interval;
                velY += accY * interval;
                velZ += accZ * interval;

                dispX += velX * interval;
                dispY += velY * interval;
                dispZ += velZ * interval;
                this.view.setText("acc : (" + String.format("%.3f", accX) + ", " + String.format("%.3f", accY) + ", " + String.format("%.3f", accZ) + ")\n" +
                        "vel : (" + String.format("%.3f", velX) + ", " + String.format("%.3f", velY) + ", " + String.format("%.3f", velZ) + ")\n" +
                        "disp : (" + String.format("%.3f", dispX) + ", " + String.format("%.3f", dispY) + ", " + String.format("%.3f", dispZ) + ")");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
