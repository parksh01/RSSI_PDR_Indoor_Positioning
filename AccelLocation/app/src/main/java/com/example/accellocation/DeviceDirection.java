package com.example.accellocation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceDirection implements SensorEventListener {
    TextView view;
    Context context;
    SensorManager manager;
    int tick;
    final private int sensorType = Sensor.TYPE_GYROSCOPE;
    private ArrayList<Float> recentVal;

    public float velx, vely, velz;
    public float prevVelx, prevVely, prevVelz;
    public float rotx, roty, rotz;
    public float prevRotx, prevRoty, prevRotz;
    private float interval;
    private long before;

    final private int tickThresh = 300;

    DeviceDirection(TextView view, SensorManager manager, Context context){
        this.view = view;
        this.context = context;
        this.manager = manager;
        this.tick = 0;
        this.recentVal = new ArrayList<Float>();
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        interval = (float) Math.abs(before - System.nanoTime()) / 1000000000;
        before = System.nanoTime();
        prevVelx = velx;
        prevVely = vely;
        prevVelz = velz;
        velx = sensorEvent.values[0];
        vely = sensorEvent.values[1];
        velz = sensorEvent.values[2];
        this.tick++;

        if(tick < tickThresh){
            this.view.setText("tick : " + this.tick);
        }
        else {
            prevRotx = rotx;
            prevRoty = roty;
            prevRotz = rotz;
            rotx = rotx + (prevVelx + velx) * interval / 2;
            roty = roty + (prevVely + vely) * interval / 2;
            rotz = rotz + (prevVelz + velz) * interval / 2;

            this.view.setText("x : " + String.format("%.3f", rotx / 3.14159) + "pi" + '\n' +
                    "y : " + String.format("%.3f", roty / 3.14159) + "pi" + '\n' +
                    "z : " + String.format("%.3f", rotz / 3.14159) + "pi" + '\n');
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    public void clear(){
        this.tick = 0;
        this.rotx = 0;
        this.roty = 0;
        this.rotz = 0;
        this.prevRotx = 0;
        this.prevRoty = 0;
        this.prevRotz = 0;
        this.velx = 0;
        this.vely = 0;
        this.velz = 0;
        this.prevVelx = 0;
        this.prevVely = 0;
        this.prevVelz = 0;
    }
}
