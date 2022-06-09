package org.techtown.rssimeasureapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.CollationElementIterator;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    // To display measured RSSI values.
    ListView listView;
    ListItemAdapter adapter;
    ArrayList<String[]> beaconList;

    // Beacon coordinates and front/back
    EditText Beacon01coordinates, Beacon02coordinates, Beacon03coordinates;
    CheckBox Beacon01isBack, Beacon02isBack, Beacon03isBack;

    // Log Generator
    LogGenerator logGen;
    int timeInterval;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Required Permission : Location, File Write, File Read
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MODE_PRIVATE);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                MODE_PRIVATE
                );

        // Android 12 Requires additional permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT}, 1);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, 1);
        }

        // Activate bluetooth
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bleCheck(bluetoothAdapter);

        listView = findViewById(R.id.BTlist);
        adapter = new ListItemAdapter();
        listView.setAdapter(adapter);

        // Log Generator
        logGen = new LogGenerator(3);
        timeInterval = 200;

        // storing values related to beacon coordinates.
        Beacon01coordinates = findViewById(R.id.Beacon1Coordinate);
        Beacon01coordinates.setText(PrefManager.getString(this, "Beacon01coordinates", "0,0"));

        Beacon02coordinates = findViewById(R.id.Beacon2Coordinate);
        Beacon02coordinates.setText(PrefManager.getString(this, "Beacon02coordinates", "0,0"));

        Beacon03coordinates = findViewById(R.id.Beacon3Coordinate);
        Beacon03coordinates.setText(PrefManager.getString(this, "Beacon03coordinates", "0,0"));

        Beacon01isBack = findViewById(R.id.Beacon1isBack);
        Beacon01isBack.setChecked(PrefManager.getBoolean(this,"Beacon01isChecked", false));

        Beacon02isBack = findViewById(R.id.Beacon2isBack);
        Beacon02isBack.setChecked(PrefManager.getBoolean(this,"Beacon02isChecked", false));

        Beacon03isBack = findViewById(R.id.Beacon3isBack);
        Beacon03isBack.setChecked(PrefManager.getBoolean(this,"Beacon03isChecked", false));

        // read values from config file.
        beaconList = Beacon.readConfig("BeaconConfig.csv");
    }

    @Override
    protected void onPause() {
        // Store RSSI to distance coefficients for further use.
        super.onPause();
        PrefManager.setString(this, "Beacon01coordinates", Beacon01coordinates.getText().toString());
        PrefManager.setString(this, "Beacon02coordinates", Beacon02coordinates.getText().toString());
        PrefManager.setString(this, "Beacon03coordinates", Beacon03coordinates.getText().toString());
        PrefManager.setBoolean(this, "Beacon01isChecked", Beacon01isBack.isChecked());
        PrefManager.setBoolean(this, "Beacon02isChecked", Beacon02isBack.isChecked());
        PrefManager.setBoolean(this, "Beacon03isChecked", Beacon03isBack.isChecked());
    }

    private void bleCheck(BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter == null) {
            // If device don't support bluetooth, just turn off.
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                // if bluetooth is not turned on, prompt to turn it on.
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "'근처 기기'를 허용해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(i);
            }
        }
    }

    // Scan bluetooth devices
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                public void run() {
                    boolean isBack = false;
                    boolean newSignal = false;
                    int currentBeacon = -1;
                    for(int i=0;i<beaconList.size();i++){
                        if(beaconList.get(i)[0].equals(device.getAddress())){
                            newSignal = true;
                            currentBeacon = i;
                            break;
                        }
                    }
                    if (newSignal) {
                        // get beacon number from MAC address
                        String beaconNumber = "Unknown";
                        int beaconOrder = currentBeacon + 1;

                        // Update beacon list.
                        // first, check if the beacon is already discovered. (if not, index is -1)
                        int beaconIndex = -1;
                        for(int i = 0;i<adapter.beacon.size();i++){
                            if(adapter.beacon.get(i).beaconNumber == beaconOrder){
                                beaconIndex = i;
                                break;
                            }
                        }

                        // if there is new beacon discovered, add it to the device list.
                        if (beaconIndex == -1) {
                            float RSSItoDist_A_value = Float.parseFloat(beaconList.get(currentBeacon)[1]);
                            float RSSItoDist_n_value = Float.parseFloat(beaconList.get(currentBeacon)[2]);
                            float RSSItoDist_A_value_back = Float.parseFloat(beaconList.get(currentBeacon)[3]);
                            float RSSItoDist_n_value_back = Float.parseFloat(beaconList.get(currentBeacon)[4]);
                            if(beaconList.get(currentBeacon)[5].equals("1")){
                                isBack = false;
                            } else if (beaconList.get(currentBeacon)[5].equals("0")) {
                                isBack = true;
                            }
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(getApplicationContext(), "'근처 기기'를 허용해주세요", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            adapter.beacon.add(new Beacon(device.getAddress(), RSSItoDist_A_value, RSSItoDist_n_value, RSSItoDist_A_value_back, RSSItoDist_n_value_back, beaconOrder, !isBack));
                            adapter.beacon.get(adapter.beacon.size()-1).setRssi(rssi);

                            // if there are multiple beacons already discovered, sort it.
                            Collections.sort(adapter.beacon);
                        }
                        // if there is a beacon already in the device list, update it.
                        else {
                            adapter.beacon.get(beaconIndex).setRssi(rssi);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onScanClicked(View view) {
        Toast.makeText(this, "Scan Start", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "'근처 기기'를 허용해주세요", Toast.LENGTH_SHORT).show();

            return;
        }
        bluetoothAdapter.startLeScan(leScanCallback);
        logGen.startLogging(adapter, timeInterval);
    }

    public void onStopClicked(View view) {
        Toast.makeText(this, "Scan Stop", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "'근처 기기'를 허용해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        bluetoothAdapter.stopLeScan(leScanCallback);
        adapter.notifyDataSetChanged();
        logGen.stopLogging();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onLogClicked(View view) {
        // Generate Log File
        logGen.generateBeaconLog(adapter);
        logGen.generateDistanceLog(adapter);
        Toast.makeText(this, "Log Generated", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClearClicked(View view) {
        adapter.clear();
        adapter.notifyDataSetChanged();
        logGen.clear();
    }
}