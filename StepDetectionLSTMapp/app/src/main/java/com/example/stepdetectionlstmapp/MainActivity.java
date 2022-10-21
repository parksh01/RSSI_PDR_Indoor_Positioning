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
import java.util.Timer;
import java.util.TimerTask;

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
    float[][][] motionClassifyInput;
    float[][] motionClassifyOutput;

    // Variables for PDR
    float stepDist;
    Pedestrian pedestrian;
    boolean isStop;

    // Handler for PDR
    final Handler stepDetectHandler = new Handler() {
        public void handleMessage(Message msg) {
            pedestrian.walk(stepDist, gyroListener.azimuth);
            stepDisplay.setText(String.format("%.2f", pedestrian.x) + ", " + String.format("%.2f", pedestrian.y));
        }
    };

    // Handler for UI element (Motion Classify)
    final Handler coordType2Handler = new Handler(){
        public void handleMessage(Message msg){
            float biggest = 0;
            int biggestIndex = -1;
            for(int i = 0;i<6;i++){
                if(biggest < motionClassifyOutput[0][i]){
                    biggest = motionClassifyOutput[0][i];
                    biggestIndex = i;
                }
            }

            switch(biggestIndex){
                case 0:
                    currentMotionDisplay.setText("stop");
                    isStop = true;
                    break;
                case 1:
                    currentMotionDisplay.setText("stopLeft");
                    isStop = true;
                    break;
                case 2:
                    currentMotionDisplay.setText("stopRight");
                    isStop = true;
                    break;
                case 3:
                    currentMotionDisplay.setText("move");
                    isStop = false;
                    break;
                case 4:
                    currentMotionDisplay.setText("moveLeft");
                    isStop = false;
                    break;
                case 5:
                    currentMotionDisplay.setText("moveRight");
                    isStop = false;
                    break;
            }
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
        motionClassifyInput = new float[1][sliceSize][6];
        motionClassifyOutput = new float[1][6];

        // Enable sensor.
        accelManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelSensor = accelManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        accelListener = new AccelometerListener(stepDisplay, stepDetectHandler, stepDetectorLSTM);
        accelManager.registerListener(accelListener, accelSensor, SensorManager.SENSOR_DELAY_GAME);

        gyroManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        gyroSensor = gyroManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gyroListener = new GyroListener(angleDisplay);
        gyroManager.registerListener(gyroListener, gyroSensor, SensorManager.SENSOR_DELAY_GAME);

        // Implement Run button
        runButton = findViewById(R.id.runButton);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accelListener.isStepDetectOn = !accelListener.isStepDetectOn;
                gyroListener.isGyroOn = !gyroListener.isGyroOn;
            }
        });

        // Implement Log generation button
        logGenButton = findViewById(R.id.logGenButton);
        logGenButton.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                pedestrian.generateLog();
            }
        });

        // Motion Classify by LSTM
        Timer scheduler = new Timer();
        TimerTask task = new TimerTask(){
            @Override
            public void run() {
                // Preparing input data
                for(int i = 0;i<sliceSize - 1;i++){
                    motionClassifyInput[0][i][0] = motionClassifyInput[0][i+1][0];
                    motionClassifyInput[0][i][1] = motionClassifyInput[0][i+1][1];
                    motionClassifyInput[0][i][2] = motionClassifyInput[0][i+1][2];
                    motionClassifyInput[0][i][3] = motionClassifyInput[0][i+1][3];
                    motionClassifyInput[0][i][4] = motionClassifyInput[0][i+1][4];
                    motionClassifyInput[0][i][5] = motionClassifyInput[0][i+1][5];
                }
                motionClassifyInput[0][sliceSize - 1][0] = (float) accelListener.accX;
                motionClassifyInput[0][sliceSize - 1][1] = (float) accelListener.accY;
                motionClassifyInput[0][sliceSize - 1][2] = (float) accelListener.accZ;
                motionClassifyInput[0][sliceSize - 1][3] = (float) gyroListener.rotX;
                motionClassifyInput[0][sliceSize - 1][4] = (float) gyroListener.rotY;
                motionClassifyInput[0][sliceSize - 1][5] = (float) gyroListener.rotZ;

                // Then run the neural network
                motionClassifyLSTM.run(motionClassifyInput, motionClassifyOutput);

                Log.d("motionOutput", accelListener.accX + ", "
                        + accelListener.accY + ", "
                        + accelListener.accZ + ", "
                        + gyroListener.rotX + ", "
                        + gyroListener.rotY + ", "
                        + gyroListener.rotZ);

                // After getting the result, update the UI element.
                Message msg = coordType2Handler.obtainMessage();
                coordType2Handler.sendMessage(msg);
            }
        };
        scheduler.scheduleAtFixedRate(task, 0, 10);

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

    // Sensor listener for Accelometer
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
                        if(!isStop){
                            this.status = true;
                            this.stepCount++;
                            Message msg = handler.obtainMessage();
                            handler.sendMessage(msg);
                            this.lock = true;
                        }
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

    // Listener for Gyroscope
    private class GyroListener implements SensorEventListener{
        public double rotX, rotY, rotZ;
        public double angleX, angleY, angleZ;
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
                rotX = sensorEvent.values[0];
                rotY = sensorEvent.values[1];
                rotZ = sensorEvent.values[2];
                if(!isInit){
                    isInit = true;
                    timeAfter = System.nanoTime();
                    angleX = rotX;
                    angleY = rotY;
                    angleZ = rotZ;
                }
                else {
                    timeBefore = timeAfter;
                    timeAfter = System.nanoTime();
                    angleX += sensorEvent.values[0] * ((timeAfter - timeBefore));
                    angleY += sensorEvent.values[1] * ((timeAfter - timeBefore));
                    angleZ += sensorEvent.values[2] * ((timeAfter - timeBefore));
                    this.azimuth = Math.sqrt(angleY * angleY + angleZ * angleZ) / 1000000000;
                    display.setText(String.format("%.3f", this.azimuth / Math.PI) + "pi");
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

    // Class for pedestrian.
    // keeps variables related to coordinate and implemented method related to PDR
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