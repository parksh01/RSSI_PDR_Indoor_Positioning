package com.example.accellocation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AccelLocation implements SensorEventListener{
    TextView view;
    public float accX, accY, accZ;
    private float alpha = 0.8f;

    AccelLocation(TextView view){
        this.view = view;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            accX = sensorEvent.values[0];
            accY = sensorEvent.values[1];
            accZ = sensorEvent.values[2];
            this.view.setText("(" + String.format("%.3f", accX) + ", "
                    + String.format("%.3f", accY) + ", "
                    + String.format("%.3f", accZ) + ")");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
