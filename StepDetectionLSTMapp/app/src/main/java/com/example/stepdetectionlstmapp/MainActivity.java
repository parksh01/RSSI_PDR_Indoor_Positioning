package com.example.stepdetectionlstmapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("HandlerLeak")
public class MainActivity extends AppCompatActivity {
    // UI elements
    TextView stepStatus;
    Button runButton;

    // Sensors
    SensorManager accelManager;
    Sensor accelSensor;
    AccelometerListener accelListener;

    // Variables for ML based step detection.
    Interpreter tflite;
    final int sliceSize= 30;

    boolean isStepDetectOn = false;

    final Handler coordType2Handler = new Handler(){
        public void handleMessage(Message msg){
            stepStatus.setText(accelListener.stepCount + "");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load ML model
        tflite = getTfliteInterpreter("stepdetectLSTM.tflite");

        // Enable sensor.
        accelManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelSensor = accelManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        accelListener = new AccelometerListener();
        accelManager.registerListener(accelListener, accelSensor, SensorManager.SENSOR_DELAY_GAME);

        // Assign UI elements and functions
        stepStatus = findViewById(R.id.stepStatus);
        runButton = findViewById(R.id.runButton);
        runButton.setOnClickListener(new View.OnClickListener() {
            Timer scheduler;
            @Override
            public void onClick(View view) {
                isStepDetectOn = !isStepDetectOn;
                if(isStepDetectOn){
                    accelListener.init(22);
                    scheduler = new Timer();
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            // Update UI element
                            Message msg = coordType2Handler.obtainMessage();
                            coordType2Handler.sendMessage(msg);
                        }
                    };
                    scheduler.scheduleAtFixedRate(task, 0, 10);
                }
                else{
                    if(scheduler != null){
                        scheduler.cancel();
                    }
                }
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
        float[][][] input;
        float[][] output;
        boolean status;
        int stepCount;
        int tick;
        int thresh;
        boolean lock;

        AccelometerListener(){
            this.input = new float[1][sliceSize][3];
            this.output = new float[1][2];
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
            accX = event.values[0];
            accY = event.values[1];
            accZ = event.values[2];

            // Prepare input data for ML model.
            for(int i = 0;i<sliceSize - 1;i++){
                input[0][i][0] = input[0][i+1][0];
                input[0][i][1] = input[0][i+1][1];
                input[0][i][2] = input[0][i+1][2];
            }
            input[0][sliceSize - 1][0] = (float) this.accX;
            input[0][sliceSize - 1][1] = (float) this.accY;
            input[0][sliceSize - 1][2] = (float) this.accZ;

            // Run the model.
            tflite.run(this.input, this.output);
            float biggest = 0;
            int biggestIndex = -1;
            for(int i = 0;i<2;i++){
                if(biggest < this.output[0][i]){
                    biggest = this.output[0][i];
                    biggestIndex = i;
                }
            }
            if(!this.lock){
                if(biggestIndex == 0 && this.status == false){
                    this.status = true;
                    this.stepCount++;
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

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}