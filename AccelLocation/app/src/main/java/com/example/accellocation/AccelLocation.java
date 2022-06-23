package com.example.accellocation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import uk.me.berndporr.iirj.Butterworth;


public class AccelLocation implements SensorEventListener{
    TextView view;
    Context context;
    SensorManager manager;
    AccelLocation accelLocation;
    int tick;
    public float accX, accY, accZ, accbiasX, accbiasY, accbiasZ;
    public float velX, velY, velZ;
    public float dispX, dispY, dispZ;
    private float alpha = 0.8f;

    private long before, after;
    final private int tickThresh = 50;
    Butterworth butterworth;

    AccelLocation(TextView view, SensorManager manager, AccelLocation accelLocation, Context context){
        this.view = view;
        this.context = context;
        this.manager = manager;
        this.accelLocation = accelLocation;
        this.tick = 0;
        this.butterworth = new Butterworth();
        butterworth.highPass(4, 400, 100);
        Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            before = System.nanoTime();
            accX = sensorEvent.values[0];
            accY = sensorEvent.values[1];
            accZ = sensorEvent.values[2];
            after = System.nanoTime();

            velX += accX * Math.abs(after - before) / 1000;
            velY += accY * Math.abs(after - before) / 1000;
            velZ += accZ * Math.abs(after - before) / 1000;

            dispX += velX * Math.abs(after - before) / 1000;
            dispY += velY * Math.abs(after - before) / 1000;
            dispZ += velZ * Math.abs(after - before) / 1000;
            this.view.setText("acc : (" + String.format("%.3f", accX) + ", " + String.format("%.3f", accY) + ", " + String.format("%.3f", accZ) + ")\n" +
                "vel : (" + String.format("%.3f", velX) + ", " + String.format("%.3f", velY) + ", " + String.format("%.3f", velZ) + ")\n" +
                "disp : (" + String.format("%.3f", dispX) + ", " + String.format("%.3f", dispY) + ", " + String.format("%.3f", dispZ) + ")\n");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
