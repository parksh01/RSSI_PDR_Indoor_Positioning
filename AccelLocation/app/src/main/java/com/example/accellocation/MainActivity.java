package com.example.accellocation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button startButton;
    TextView coordinateDisplay;

    SensorManager manager;
    AccelLocation accelLocation;

    boolean buttonSwitchToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button)findViewById(R.id.button);
        coordinateDisplay = (TextView)findViewById(R.id.coordinateDisplay);

        manager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelLocation = new AccelLocation(coordinateDisplay, manager, this.getApplicationContext());

        buttonSwitchToggle = false;

    }

    public void onStartButtonClick(View view) {
        buttonSwitchToggle = !buttonSwitchToggle;
        if(buttonSwitchToggle){
            Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            // shorter interval makes smaller error.
            boolean accelCheck = manager.registerListener(accelLocation, sensor, SensorManager.SENSOR_DELAY_FASTEST);
            if(!accelCheck){
                Toast.makeText(this.getApplicationContext(), "가속도 센서를 지원하지 않음", Toast.LENGTH_LONG).show();
            }
        }
        else{
            manager.unregisterListener(accelLocation);
            accelLocation.clear();
        }
    }
}