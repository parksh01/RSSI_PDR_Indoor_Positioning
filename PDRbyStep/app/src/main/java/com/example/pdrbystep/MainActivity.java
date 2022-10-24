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
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    SensorManager sensorManager;
    Sensor stepCountSensor;
    Sensor accelSensor;
    Sensor gyroSensor;

    int currentStepsAccel;
    int currentStepsStepSensor;
    int currentStepsCombine = 1;
    double currentAccel, prevAccel;
    double currentStepTime, prevStepTime;
    double currentStepTimeStepSensor, currentStepTimeAccel;
    double stepThresh;
    int stepIntervalThresh = 300;
    int steptick = 0;
    double currentAngleVel, prevAngleVel;
    double currentAngle;
    double timeBefore, timeAfter;
    double locx = 0, locy = 1.0;
    ArrayList<Double> locxLog, locyLog;
    ArrayList<Float> accelxLog, accelyLog, accelzLog, isStep;

    // for detecting step change
    int beforeStep;
    int currentStep;

    Button resetButton;
    TextView stepCountView;
    TextView stepCountViewStepSensor;
    TextView stepCountViewCombine;
    TextView currentAngleDisplay;
    TextView locationDisplay;
    EditText inputStepThresh;
    Button logButton;
    Button accelLogButton;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locxLog = new ArrayList<Double>();
        locyLog = new ArrayList<Double>();
        accelxLog = new ArrayList<Float>();
        accelyLog = new ArrayList<Float>();
        accelzLog = new ArrayList<Float>();
        isStep = new ArrayList<Float>();

        // UI Elements.
        resetButton = (Button)findViewById(R.id.resetButton);
        stepCountView = (TextView)findViewById(R.id.stepCountView);
        stepCountViewStepSensor = (TextView)findViewById(R.id.stepCountViewStepSensor);
        stepCountViewCombine = (TextView)findViewById(R.id.stepCountViewCombine);
        inputStepThresh = (EditText)findViewById(R.id.editStepThresh);
        currentAngleDisplay = (TextView)findViewById(R.id.currentAngleDisplay);
        locationDisplay = (TextView)findViewById(R.id.locationDisplay);
        logButton = (Button)findViewById(R.id.logButton);
        accelLogButton = (Button)findViewById(R.id.accelLogButton);

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

        // Step log Button
        logButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                String filename = "CoordLogPDR - " + LocalDate.now() + "-" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH시 mm분 ss초")) + ".csv";
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), filename);
                try{
                    if(!file.exists()){
                        file.createNewFile();
                    }
                    FileWriter writer = new FileWriter(file, false);
                    String str = "locX,locY\n";
                    for(int i=0;i<locxLog.size();i++){
                        str += (String.format("%.3f", locxLog.get(i)) + ",");
                        str += (String.format("%.3f", locyLog.get(i)) + "\n");
                    }
                    writer.write(str);
                    writer.close();
                    Toast.makeText(getApplicationContext(), "Coord Log Generated", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Accelerometer log Button
        accelLogButton.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                String filename = "accelLog - " + LocalDate.now() + "-" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH시 mm분 ss초")) + ".csv";
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), filename);
                try{
                    if(!file.exists()){
                        file.createNewFile();
                    }
                    FileWriter writer = new FileWriter(file, false);
                    String str = "accelX,accelY,accelZ,label\n";
                    for(int i=0;i<isStep.size();i++){
                        str += (String.format("%.3f", accelxLog.get(i)) + ",");
                        str += (String.format("%.3f", accelyLog.get(i)) + ",");
                        str += (String.format("%.3f", accelzLog.get(i)) + ",");
                        str += (String.format("%.3f", isStep.get(i)) + "\n");
                    }
                    writer.write(str);
                    writer.close();
                    Toast.makeText(getApplicationContext(), "accel Log Generated", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

        beforeStep = currentStep;
        currentStep = Integer.parseInt(stepCountViewCombine.getText().toString());

        // Detect step by step detector.
        // Only log when step is detected by step detector.
        if(sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
            if(sensorEvent.values[0]==1.0f){
                currentStepsStepSensor++;
                currentStepsCombine++;
                currentStepTimeStepSensor = System.currentTimeMillis();
                stepCountViewStepSensor.setText(String.valueOf(currentStepsStepSensor));
                locx -= Math.sin(currentAngle * Math.PI);
                locy += Math.cos(currentAngle * Math.PI);

                // Mark 'Stepped' on log data.
                isStep.set(isStep.size()-1, 1.0f);
            }

        }

        // Detect step by accelerometer.
        if(sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            // first, log accelerometer data.
            logAccel(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);

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

        // Log each step
        if(currentStep != beforeStep){
            logStep(locx, locy);
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
            currentAngle += ((prevAngleVel + currentAngleVel) * ((timeAfter - timeBefore)/1000000000) / 2.0) * (2.0/1.8);
            currentAngleDisplay.setText(String.format("%.3f", currentAngle) + "pi");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // Logging each step
    public void logStep(double x, double y){
        locxLog.add(x);
        locyLog.add(y);
    }

    public void logAccel(float x, float y, float z){
        accelxLog.add(x);
        accelyLog.add(y);
        accelzLog.add(z);
        isStep.add(0.0f);
    }
}