package com.example.accellocation;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    Button startButton;
    TextView coordinateDisplay;
    TextView directionDisplay;
    TextView coordType2;

    SensorManager AccelManager, RotManager;
    AccelLocation accelLocation;
    DeviceDirection deviceDirection;

    SensorLogWriter sensorLogWriter;

    boolean buttonSwitchToggle;

    float coordX = 0;
    float coordY = 0;
    float interval = 0;
    float before;
    final Handler coordType2Handler = new Handler(){
        public void handleMessage(Message msg){
            coordType2.setText("(" + String.format("%.3f", coordX) + ", " + String.format("%.3f", coordY) + ")");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button)findViewById(R.id.button);
        coordinateDisplay = (TextView)findViewById(R.id.coordinateDisplay);
        directionDisplay = (TextView)findViewById(R.id.directionDisplay);
        coordType2 = (TextView)findViewById(R.id.coordinateType2Display);

        AccelManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        RotManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelLocation = new AccelLocation(coordinateDisplay, AccelManager, this.getApplicationContext());
        deviceDirection = new DeviceDirection(directionDisplay, RotManager, this.getApplicationContext());

        sensorLogWriter = new SensorLogWriter();

        buttonSwitchToggle = false;

    }

    public void onStartButtonClick(View view) {
        buttonSwitchToggle = !buttonSwitchToggle;
        Timer scheduler = new Timer();
        if(buttonSwitchToggle){
            Sensor AccelSensor = AccelManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            Sensor RotationVectorSensor = RotManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            // shorter interval makes smaller error.
            boolean accelCheck = AccelManager.registerListener(accelLocation, AccelSensor, SensorManager.SENSOR_DELAY_GAME);
            boolean directionCheck = RotManager.registerListener(deviceDirection, RotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);
            if(!accelCheck | !directionCheck){
                Toast.makeText(this.getApplicationContext(), "가속도 센서를 지원하지 않음", Toast.LENGTH_LONG).show();
            }
            else{
                TimerTask task = new TimerTask(){
                    @Override
                    public void run() {
                        interval = (float) Math.abs(before - System.nanoTime()) / 1000000000;
                        before = System.nanoTime();
                        coordX += Math.sin(deviceDirection.totalRot) * accelLocation.spd * interval;
                        coordY += Math.cos(deviceDirection.totalRot) * accelLocation.spd * interval;
                        Message msg = coordType2Handler.obtainMessage();
                        coordType2Handler.sendMessage(msg);
                        if((deviceDirection.tick > deviceDirection.tickThresh) && (accelLocation.tick > accelLocation.tickThresh)){
                            sensorLogWriter.addValue(accelLocation.accX, accelLocation.accY, accelLocation.accZ, deviceDirection.velx, deviceDirection.vely, deviceDirection.velz);
                        }
                    }
                };
                scheduler.scheduleAtFixedRate(task, 0, 10);
            }
        }
        else{
            AccelManager.unregisterListener(accelLocation);
            RotManager.unregisterListener(deviceDirection);
            accelLocation.clear();
            deviceDirection.clear();
            scheduler.cancel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onLogButtonClick(View view) {
        sensorLogWriter.generate(getApplicationContext());
    }

    public void onStopClick(View view) {
        sensorLogWriter.currentTag = "stop";
    }
    public void onStopLeftClick(View view) {
        sensorLogWriter.currentTag = "stopLeft";
    }
    public void onStopRightClick(View view) {
        sensorLogWriter.currentTag = "stopRight";
    }

    public void onMoveClick(View view) {
        sensorLogWriter.currentTag = "move";
    }
    public void onMoveLeftClick(View view) {
        sensorLogWriter.currentTag = "moveLeft";
    }
    public void onMoveRightClick(View view) {
        sensorLogWriter.currentTag = "moveRight";
    }
}