package com.example.pdrbystep;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    SensorManager sensorManager;
    Sensor stepCountSensor;
    Sensor accelSensor;
    Sensor gyroSensor;

    int currentStepsAccel;
    int currentStepsStepSensor;
    int currentStepsCombine;
    double currentAccel, prevAccel;
    double currentStepTime, prevStepTime;
    double currentStepTimeStepSensor, currentStepTimeAccel;
    double stepThresh;
    int stepIntervalThresh = 300;
    int steptick = 0;
    double currentAngleVel, prevAngleVel;
    double currentAngle;
    double timeBefore, timeAfter;
    double locx = 0, locy = 0;

    Button resetButton;
    TextView stepCountView;
    TextView stepCountViewStepSensor;
    TextView stepCountViewCombine;
    TextView currentAngleDisplay;
    TextView locationDisplay;
    EditText inputStepThresh;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI Elements.
        resetButton = (Button)findViewById(R.id.resetButton);
        stepCountView = (TextView)findViewById(R.id.stepCountView);
        stepCountViewStepSensor = (TextView)findViewById(R.id.stepCountViewStepSensor);
        stepCountViewCombine = (TextView)findViewById(R.id.stepCountViewCombine);
        inputStepThresh = (EditText)findViewById(R.id.editStepThresh);
        currentAngleDisplay = (TextView)findViewById(R.id.currentAngleDisplay);
        locationDisplay = (TextView)findViewById(R.id.locationDisplay);

        // Ask for permission
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){

            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        // Use step sensor.
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (stepCountSensor == null) {
            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_SHORT).show();
        }

        // Use Accelerometer.
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if(accelSensor == null){
            Toast.makeText(this, "No Accelerometer", Toast.LENGTH_SHORT).show();
        }

        // Use Gyroscope
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if(gyroSensor == null){
            Toast.makeText(this, "No Gyroscope", Toast.LENGTH_SHORT).show();
        }
        currentAngle = 0;

        // Reset button.
        resetButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                currentStepsAccel = 0;
                currentStepsStepSensor = 0;
                stepCountView.setText(String.valueOf(currentStepsAccel));
                stepCountViewStepSensor.setText(String.valueOf(currentStepsStepSensor));
            }
        });
    }
    public void onStart() {
        super.onStart();
        if(stepCountSensor !=null) {
            sensorManager.registerListener(this,stepCountSensor,SensorManager.SENSOR_DELAY_FASTEST);
        }
        if(accelSensor != null){
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        if(gyroSensor != null){
            sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        timeBefore = timeAfter;
        timeAfter = System.nanoTime();

        // Detect step by step detector.
        if(sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
            if(sensorEvent.values[0]==1.0f){
                currentStepsStepSensor++;
                currentStepsCombine++;
                currentStepTimeStepSensor = System.currentTimeMillis();
                stepCountViewStepSensor.setText(String.valueOf(currentStepsStepSensor));
                locx -= Math.sin(currentAngle * Math.PI);
                locy += Math.cos(currentAngle * Math.PI);
            }

        }

        // Detect step by accelerometer.
        if(sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            // get Accel threshold value.
            if ( inputStepThresh.getText().toString().length() == 0 ) {
                stepThresh = 1.0;
            }
            else {
                stepThresh = Double.parseDouble(inputStepThresh.getText().toString());
            }

            // and detect walk by the threshold, with accel value.
            prevAccel = currentAccel;
            prevStepTime = currentStepTime;
            currentAccel = Math.sqrt(sensorEvent.values[0]*sensorEvent.values[0] + sensorEvent.values[1]*sensorEvent.values[1] + sensorEvent.values[2]*sensorEvent.values[2]);
            if(Math.abs(prevAccel - currentAccel) > stepThresh){
                currentStepTime = System.currentTimeMillis();
                if(Math.abs(prevStepTime - currentStepTime) > stepIntervalThresh){
                    currentStepsAccel++;
                    currentStepsCombine++;
                    currentStepTimeAccel = currentStepTime;
                    stepCountView.setText(String.valueOf(currentStepsAccel));
                    locx -= Math.sin(currentAngle * Math.PI);
                    locy += Math.cos(currentAngle * Math.PI);
                }
            }
        }

        // Combining both sensors
        if(Math.abs(currentStepTimeAccel - currentStepTimeStepSensor) < stepIntervalThresh){
            currentStepsCombine--;
            locx += Math.sin(currentAngle * Math.PI);
            locy -= Math.cos(currentAngle * Math.PI);
            currentStepTimeAccel = 100000;
            stepCountViewCombine.setText(String.valueOf(currentStepsCombine));
            locationDisplay.setText(String.format("%.3f", locx) + ", " + String.format("%.3f", locy));
        }
        else{
            stepCountViewCombine.setText(String.valueOf(currentStepsCombine));
            locationDisplay.setText(String.format("%.3f", locx) + ", " + String.format("%.3f", locy));
        }

        // Calculate orientation of device by its rotation velocity.
        if(sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            prevAngleVel = currentAngleVel;
            double velx = sensorEvent.values[0];
            double vely = sensorEvent.values[1];
            double velz = sensorEvent.values[2];
            double currentAngleVel = Math.sqrt(vely*vely + velz*velz);
            if(((Math.abs(vely) > Math.abs(velz)) && (vely < 0)) || ((Math.abs(vely) < Math.abs(velz)) && (velz < 0))){
                currentAngleVel *= (-1);
            }
            currentAngle += (prevAngleVel + currentAngleVel) * ((timeAfter - timeBefore)/1000000000) / 2.0;
            currentAngleDisplay.setText(String.format("%.3f", currentAngle) + "pi");
        }

        Log.d("angle", String.format("%.3f", Math.sin(currentAngle * Math.PI)) + ", " + String.format("%.3f", Math.cos(currentAngle * Math.PI)));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}