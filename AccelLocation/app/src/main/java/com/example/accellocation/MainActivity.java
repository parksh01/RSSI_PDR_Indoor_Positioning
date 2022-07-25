package com.example.accellocation;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import java.util.Arrays;
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

    Interpreter tflite;
    float[][][] input;
    float[][] output;
    final int sliceSize= 30;

    final Handler coordType2Handler = new Handler(){
        public void handleMessage(Message msg){
            coordType2.setText("(" + String.format("%.3f", coordX) + ", " + String.format("%.3f", coordY) + ")");
            float biggest = 0;
            int biggestIndex = -1;
            for(int i = 0;i<6;i++){
                if(biggest < output[0][i]){
                    biggest = output[0][i];
                    biggestIndex = i;
                }
            }
            switch(biggestIndex){
                case 0:
                    coordType2.setText("stop");
                    break;
                case 1:
                    coordType2.setText("stopLeft");
                    break;
                case 2:
                    coordType2.setText("stopRight");
                    break;
                case 3:
                    coordType2.setText("move");
                    break;
                case 4:
                    coordType2.setText("moveLeft");
                    break;
                case 5:
                    coordType2.setText("moveRight");
                    break;
            }
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

        tflite = getTfliteInterpreter("converted_model.tflite");
        input = new float[1][sliceSize][6];
        output = new float[1][6];
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
                        for(int i = 0;i<sliceSize - 1;i++){
                            input[0][i][0] = input[0][i+1][0];
                            input[0][i][1] = input[0][i+1][1];
                            input[0][i][2] = input[0][i+1][2];
                            input[0][i][3] = input[0][i+1][3];
                            input[0][i][4] = input[0][i+1][4];
                            input[0][i][5] = input[0][i+1][5];
                        }
                        input[0][sliceSize - 1][0] = accelLocation.accX;
                        input[0][sliceSize - 1][1] = accelLocation.accY;
                        input[0][sliceSize - 1][2] = accelLocation.accZ;
                        input[0][sliceSize - 1][3] = deviceDirection.velx;
                        input[0][sliceSize - 1][4] = deviceDirection.vely;
                        input[0][sliceSize - 1][5] = deviceDirection.velz;

                        Message msg = coordType2Handler.obtainMessage();

                        if((deviceDirection.tick > deviceDirection.tickThresh) && (accelLocation.tick > accelLocation.tickThresh)){
                            sensorLogWriter.addValue(accelLocation.accX, accelLocation.accY, accelLocation.accZ, deviceDirection.velx, deviceDirection.vely, deviceDirection.velz);
                            tflite.run(input, output);
                            coordType2Handler.sendMessage(msg);
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
}