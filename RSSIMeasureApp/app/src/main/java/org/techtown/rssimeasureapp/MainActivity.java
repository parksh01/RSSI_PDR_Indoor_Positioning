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

import java.io.File;
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

    // RSSI to Distance mapping coefficients.
    EditText RSSItoDist_A, RSSItoDist_A_back;
    EditText RSSItoDist_n, RSSItoDist_n_back;

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

        // Required Permission : Location, File Write
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MODE_PRIVATE);

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

        // storing form into SharedPreferences.
        String RSSItoDist_A_value = PrefManager.getString(this, "RSSItoDist_A_front", "-51.216");
        RSSItoDist_A = findViewById(R.id.RSSItoDist_A);
        RSSItoDist_A.setText(RSSItoDist_A_value);

        String RSSItoDist_n_value = PrefManager.getString(this, "RSSItoDist_n_front", "2.261");
        RSSItoDist_n = findViewById(R.id.RSSItoDist_n);
        RSSItoDist_n.setText(RSSItoDist_n_value);

        String RSSItoDist_A_back_value = PrefManager.getString(this, "RSSItoDist_A_back", "-52.165");
        RSSItoDist_A_back = findViewById(R.id.RSSItoDist_A_back);
        RSSItoDist_A_back.setText(RSSItoDist_A_back_value);

        String RSSItoDist_n_back_value = PrefManager.getString(this, "RSSItoDist_n_back", "1.988");
        RSSItoDist_n_back = findViewById(R.id.RSSItoDist_n_back);
        RSSItoDist_n_back.setText(RSSItoDist_n_back_value);

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
    }

    @Override
    protected void onPause() {
        // Store RSSI to distance coefficients for further use.
        super.onPause();
        PrefManager.setString(this, "RSSItoDist_A_front", RSSItoDist_A.getText().toString());
        PrefManager.setString(this, "RSSItoDist_n_front", RSSItoDist_n.getText().toString());
        PrefManager.setString(this, "RSSItoDist_A_back", RSSItoDist_A_back.getText().toString());
        PrefManager.setString(this, "RSSItoDist_n_back", RSSItoDist_n_back.getText().toString());
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
                    boolean isSorted = true;
                    boolean isBack = false;
                    if (
                        // filters only desired BLE devices (beacons)
                            getString(R.string.BeaconAddress01).equals(device.getAddress()) ||
                            getString(R.string.BeaconAddress02).equals(device.getAddress()) ||
                            getString(R.string.BeaconAddress03).equals(device.getAddress()) ||
                            getString(R.string.BeaconAddress04).equals(device.getAddress())
                    ) {
                        float RSSItoDist_A_value = 0;
                        float RSSItoDist_n_value = 0;
                        float RSSItoDist_A_value_back = 0;
                        float RSSItoDist_n_value_back = 0;
                        if (RSSItoDist_A.getText().toString().length() != 0 && RSSItoDist_n.getText().toString().length() != 0) {
                            RSSItoDist_A_value = Float.parseFloat(RSSItoDist_A.getText().toString());
                            RSSItoDist_n_value = Float.parseFloat(RSSItoDist_n.getText().toString());
                        }
                        if (RSSItoDist_A_back.getText().toString().length() != 0 && RSSItoDist_n_back.getText().toString().length() != 0) {
                            RSSItoDist_A_value_back = Float.parseFloat(RSSItoDist_A_back.getText().toString());
                            RSSItoDist_n_value_back = Float.parseFloat(RSSItoDist_n_back.getText().toString());
                        }
                        // get beacon number from MAC address
                        String beaconNumber = "Unknown";
                        int beaconOrder = 0;
                        if(getString(R.string.BeaconAddress01).equals(device.getAddress())){
                            beaconNumber = "Beacon #1";
                            beaconOrder = 1;
                            isBack = Beacon01isBack.isChecked();
                        }
                        else if(getString(R.string.BeaconAddress02).equals(device.getAddress())){
                            beaconNumber = "Beacon #2";
                            beaconOrder = 2;
                            isBack = Beacon02isBack.isChecked();
                        }
                        else if(getString(R.string.BeaconAddress03).equals(device.getAddress())){
                            beaconNumber = "Beacon #3";
                            beaconOrder = 3;
                            isBack = Beacon03isBack.isChecked();
                        }

                        // Update beacon list.
                        // first, check if the beacon is already discovered. (if not, index is -1)
                        int index = adapter.address.indexOf(device.getAddress());
                        int beaconIndex = 0;
                        for(int i = 0;i<adapter.beacon.size();i++){
                            if(adapter.beacon.get(i).beaconNumber == beaconOrder){
                                beaconIndex = i;
                                break;
                            }
                        }


                        // if there is new beacon discovered, add it to the device list.
                        if (index == -1) {
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(getApplicationContext(), "'근처 기기'를 허용해주세요", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if(!isBack){
                                adapter.addItem(beaconNumber, device.getAddress(), Integer.toString(rssi), RSSItoDist_A_value, RSSItoDist_n_value);
                            }
                            else{
                                adapter.addItem(beaconNumber, device.getAddress(), Integer.toString(rssi), RSSItoDist_A_value_back, RSSItoDist_n_value_back);
                            }
                            adapter.beacon.add(new Beacon(device.getAddress(), RSSItoDist_A_value, RSSItoDist_n_value, RSSItoDist_A_value_back, RSSItoDist_n_value_back, beaconOrder, !isBack));
                            adapter.beacon.get(adapter.beacon.size()-1).setRssi(rssi);

                            // if there are multiple beacons already discovered, sort it.
                            Collections.sort(adapter.beacon);
                            if(adapter.getCount() >= 2){
                                for(int i = 0;i < adapter.getCount() - 1;i++){
                                    if(adapter.device.get(i).compareTo(adapter.device.get(i+1)) > 0){
                                        isSorted = false;
                                    }
                                }
                            }
                            if(isSorted == false){
                                adapter.sort();
                            }
                        }
                        // if there is a beacon already in the device list, update it.
                        else {
                            adapter.beacon.get(index).setRssi(rssi);
                            if(!isBack) {
                                adapter.setItem(beaconNumber, device.getAddress(), Integer.toString(rssi), RSSItoDist_A_value, RSSItoDist_n_value, index);
                            }
                            else{
                                adapter.setItem(beaconNumber, device.getAddress(), Integer.toString(rssi), RSSItoDist_A_value_back, RSSItoDist_n_value_back, index);
                            }
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