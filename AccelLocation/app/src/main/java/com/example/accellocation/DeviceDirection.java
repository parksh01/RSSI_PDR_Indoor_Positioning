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
    public int tick;
    private ArrayList<Float> recentVal;

    public float velx, vely, velz;
    public float prevVelx, prevVely, prevVelz;
    public float rotx, roty, rotz;
    public float prevRotx, prevRoty, prevRotz;
    private float interval;
    private long before;

    public float deltaRot, prevDeltaRot, totalRot;

    final public int tickThresh = 300;

    DeviceDirection(TextView view, SensorManager manager, Context context){
        this.view = view;
        this.context = context;
        this.manager = manager;
        this.tick = 0;
        this.recentVal = new ArrayList<Float>();
        this.totalRot = 0;
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        interval = (float) Math.abs(before - System.nanoTime()) / 1000000000;
        before = System.nanoTime();
        prevVelx = velx;
        prevVely = vely;
        prevVelz = velz;
        prevDeltaRot = deltaRot;
        velx = sensorEvent.values[0];
        vely = sensorEvent.values[1];
        velz = sensorEvent.values[2];
        deltaRot = (float) Math.sqrt(vely*vely + velz*velz);
        if(((Math.abs(vely) > Math.abs(velz)) && (vely < 0)) || ((Math.abs(vely) < Math.abs(velz)) && (velz < 0))){
            deltaRot *= (-1);
        }
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
            totalRot = totalRot + (prevDeltaRot + deltaRot) * interval / 2;

            this.view.setText("rotx : " + String.format("%.3f", rotx / 3.14159) + "pi / " +
                    "roty : " + String.format("%.3f", roty / 3.14159) + "pi / " +
                    "rotz : " + String.format("%.3f", rotz / 3.14159) + "pi" + '\n' +
                    "velx : " + String.format("%.3f", velx / 3.14159) + "pi / " +
                    "vely : " + String.format("%.3f", vely / 3.14159) + "pi / " +
                    "velz : " + String.format("%.3f", velz / 3.14159) + "pi " + '\n' +
                    "rot : " + String.format("%.3f", totalRot / 3.14159) + "pi ");
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
