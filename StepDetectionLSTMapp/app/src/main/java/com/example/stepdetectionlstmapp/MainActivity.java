package com.example.stepdetectionlstmapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@SuppressLint("HandlerLeak")
public class MainActivity extends AppCompatActivity {
    // UI elements
    TextView stepDisplay;
    TextView angleDisplay;
    TextView currentMotionDisplay;
    Button runButton;
    Button logGenButton;

    // Sensors
    SensorManager accelManager;
    Sensor accelSensor;
    AccelometerListener accelListener;

    SensorManager gyroManager;
    Sensor gyroSensor;
    GyroListener gyroListener;

    // Variables for ML based step detection and motion classification.
    Interpreter stepDetectorLSTM;
    Interpreter motionClassifyLSTM;
    final int sliceSize= 30;

    // Variables for PDR
    float stepDist;
    Pedestrian pedestrian;

    // Handler for PDR
    final Handler stepDetectHandler = new Handler() {
        public void handleMessage(Message msg) {
            pedestrian.walk(stepDist, gyroListener.azimuth);
            stepDisplay.setText(String.format("%.2f", pedestrian.x) + ", " + String.format("%.2f", pedestrian.y));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Assign UI elements and functions
        stepDisplay = findViewById(R.id.stepStatus);
        angleDisplay = findViewById(R.id.rotvecDisplay);
        currentMotionDisplay = findViewById(R.id.currentMotionDisplay);

        // Set Step length;
        pedestrian = new Pedestrian();
        stepDist = 0.7f;

        // Load ML model
        stepDetectorLSTM = getTfliteInterpreter("stepdetectLSTM.tflite");
        motionClassifyLSTM = getTfliteInterpreter("motionClassifyLSTM.tflite");

        // Enable sensor.
        accelManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelSensor = accelManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        accelListener = new AccelometerListener(stepDisplay, stepDetectHandler, stepDetectorLSTM);
        accelManager.registerListener(accelListener, accelSensor, SensorManager.SENSOR_DELAY_GAME);

        gyroManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        gyroSensor = gyroManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gyroListener = new GyroListener(angleDisplay);
        gyroManager.registerListener(gyroListener, gyroSensor, SensorManager.SENSOR_DELAY_GAME);

        runButton = findViewById(R.id.runButton);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accelListener.isStepDetectOn = !accelListener.isStepDetectOn;
                gyroListener.isGyroOn = !gyroListener.isGyroOn;
            }
        });

        logGenButton = findViewById(R.id.logGenButton);
        logGenButton.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                pedestrian.generateLog();
            }
        });
    }

    // Functions related to loading ML model.
    private Interpreter getTfliteInterpreter(String modelPath){
        try{
            return new Interpreter(loadModelFile(MainActivity.this, modelPath));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private class AccelometerListener implements SensorEventListener {
        public double accX, accY, accZ;
        float[][][] stepDetectInput;
        float[][] stepDetectOutput;
        boolean status;
        int stepCount;
        int tick;
        int thresh;
        boolean lock;
        Interpreter tfmodel;

        boolean isStepDetectOn;
        TextView display;
        Handler handler;

        AccelometerListener(TextView display, Handler handler, Interpreter tfmodel){
            this.stepDetectInput = new float[1][sliceSize][3];
            this.stepDetectOutput = new float[1][2];
            this.isStepDetectOn = false;
            this.display = display;
            this.handler = handler;
            this.tfmodel = tfmodel;
        }

        public void init(){
            this.stepCount = 0;
            this.tick = 0;
            this.thresh = 10;
        }
        public void init(int thresh){
            this.stepCount = 0;
            this.tick = 0;
            this.thresh = thresh;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(this.isStepDetectOn){
                accX = event.values[0];
                accY = event.values[1];
                accZ = event.values[2];

                // Prepare input data for ML model.
                for(int i = 0;i<sliceSize - 1;i++){
                    stepDetectInput[0][i][0] = stepDetectInput[0][i+1][0];
                    stepDetectInput[0][i][1] = stepDetectInput[0][i+1][1];
                    stepDetectInput[0][i][2] = stepDetectInput[0][i+1][2];
                }
                stepDetectInput[0][sliceSize - 1][0] = (float) this.accX;
                stepDetectInput[0][sliceSize - 1][1] = (float) this.accY;
                stepDetectInput[0][sliceSize - 1][2] = (float) this.accZ;

                // Run the model.
                tfmodel.run(this.stepDetectInput, this.stepDetectOutput);
                float biggest = 0;
                int biggestIndex = -1;
                for(int i = 0;i<2;i++){
                    if(biggest < this.stepDetectOutput[0][i]){
                        biggest = this.stepDetectOutput[0][i];
                        biggestIndex = i;
                    }
                }
                if(!this.lock){
                    // If step is detected, do something.
                    if(biggestIndex == 0 && this.status == false){
                        this.status = true;
                        this.stepCount++;
                        Message msg = handler.obtainMessage();
                        handler.sendMessage(msg);
                        this.lock = true;
                    }
                    if(biggestIndex == 1){
                        this.status = false;
                    }
                }
                else{
                    if(this.tick < this.thresh){
                        this.tick++;
                    }
                    else{
                        this.tick = 0;
                        this.lock = false;
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    private class GyroListener implements SensorEventListener{
        public double rotX, rotY, rotZ;
        TextView display;
        boolean isGyroOn;
        private double azimuth;
        long timeBefore, timeAfter;
        boolean isInit;

        GyroListener(TextView display){
            this.display = display;
            this.isGyroOn = false;
            this.isInit = false;
            rotX = 0.0f;
            rotY = 0.0f;
            rotZ = 0.0f;
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if(this.isGyroOn){
                if(!isInit){
                    isInit = true;
                    timeAfter = System.nanoTime();
                    rotX = sensorEvent.values[0];
                    rotY = sensorEvent.values[1];
                    rotZ = sensorEvent.values[2];
                }
                else {
                    timeBefore = timeAfter;
                    timeAfter = System.nanoTime();
                    rotX += sensorEvent.values[0] * ((timeAfter - timeBefore));
                    rotY += sensorEvent.values[1] * ((timeAfter - timeBefore));
                    rotZ += sensorEvent.values[2] * ((timeAfter - timeBefore));
                    this.azimuth = Math.sqrt(rotY * rotY + rotZ * rotZ) / 1000000000;
                    display.setText(String.format("%.3f", this.azimuth / Math.PI) + "pi");
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

    private class Pedestrian{
        double x, y;
        ArrayList<Double> xlog, ylog;
        Pedestrian(){
            this.x = 0.0f;
            this.y = 0.0f;
            this.xlog = new ArrayList<Double>();
            this.ylog = new ArrayList<Double>();
        }
        void walk(float stepWidth, double azimuth){
            x += Math.cos(azimuth) * stepWidth;
            y += Math.sin(azimuth) * stepWidth;
            xlog.add(x);
            ylog.add(y);
        }
        void init(){
            this.x = 0.0f;
            this.y = 0.0f;
            xlog.clear();
            ylog.clear();
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        void generateLog(){
            String filename = "LSTM_PDR - " + LocalDate.now() + "-" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH시 mm분 ss초")) + ".csv";
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), filename);
            try{
                if(!file.exists()){
                    file.createNewFile();
                }
                FileWriter writer = new FileWriter(file, false);
                String str = "x,y\n0.000,0.000\n";
                for(int i=0;i<xlog.size();i++){
                    str += (String.format("%.3f", xlog.get(i)) + ",");
                    str += (String.format("%.3f", ylog.get(i)) + "\n");
                }
                writer.write(str);
                writer.close();
                Toast.makeText(getApplicationContext(), "LSTM_PDR Log Generated", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}