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
    TextView directionDisplay;

    SensorManager AccelManager, RotManager;
    AccelLocation accelLocation;
    DeviceDirection deviceDirection;

    boolean buttonSwitchToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button)findViewById(R.id.button);
        coordinateDisplay = (TextView)findViewById(R.id.coordinateDisplay);
        directionDisplay = (TextView)findViewById(R.id.directionDisplay);

        AccelManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        RotManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelLocation = new AccelLocation(coordinateDisplay, AccelManager, this.getApplicationContext());
        deviceDirection = new DeviceDirection(directionDisplay, RotManager, this.getApplicationContext());

        buttonSwitchToggle = false;

    }

    public void onStartButtonClick(View view) {
        buttonSwitchToggle = !buttonSwitchToggle;
        if(buttonSwitchToggle){
            Sensor AccelSensor = AccelManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            Sensor RotationVectorSensor = RotManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            // shorter interval makes smaller error.
            boolean accelCheck = AccelManager.registerListener(accelLocation, AccelSensor, SensorManager.SENSOR_DELAY_GAME);
            boolean directionCheck = RotManager.registerListener(deviceDirection, RotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);
            if(!accelCheck | !directionCheck){
                Toast.makeText(this.getApplicationContext(), "가속도 센서를 지원하지 않음", Toast.LENGTH_LONG).show();
            }
        }
        else{
            AccelManager.unregisterListener(accelLocation);
            RotManager.unregisterListener(deviceDirection);
            accelLocation.clear();
            deviceDirection.clear();
        }
    }
}