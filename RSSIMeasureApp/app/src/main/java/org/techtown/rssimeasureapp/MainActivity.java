package org.techtown.rssimeasureapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    ListView listView;
    ListItemAdapter adapter;

    // uses Kalman filter to correct errors on RSSI values. (for RSSI of each beacons)
    ArrayList<KalmanFilter> kf = new ArrayList<KalmanFilter>();

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

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bleCheck(bluetoothAdapter);

        listView = findViewById(R.id.BTlist);
        adapter = new ListItemAdapter();
        listView.setAdapter(adapter);
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
                    if(
                            // filters only desired BLE devices (beacons)
                            getString(R.string.BeaconAddress01).equals(device.getAddress()) ||
                            getString(R.string.BeaconAddress02).equals(device.getAddress()) ||
                            getString(R.string.BeaconAddress03).equals(device.getAddress())
                    ){
                        // updates information of each beacons on ListView
                        int index = adapter.address.indexOf(device.getAddress());
                        // if there is new beacon discovered, add it to the device list.
                        if(index == -1){
                            kf.add(new KalmanFilter());
                            adapter.addItem(device.getName(), device.getAddress(), Integer.toString(rssi), Integer.toString(rssi));
                        }
                        // if there is a beacon already in the device list, update it.
                        else{
                            int filteredRSSI = kf.get(index).filtering(rssi);
                            adapter.setItem(device.getName(), device.getAddress(), Integer.toString(rssi), Integer.toString(filteredRSSI),index);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

    public void onScanClicked(View view) {
        Toast.makeText(this, "Scan Start", Toast.LENGTH_SHORT).show();
        bluetoothAdapter.startLeScan(leScanCallback);
    }

    public void onStopClicked(View view) {
        Toast.makeText(this, "Scan Stop", Toast.LENGTH_SHORT).show();
        bluetoothAdapter.stopLeScan(leScanCallback);
        adapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onLogClicked(View view) {
        // Generate Log File
        for(int i = 0; i < adapter.getCount(); i++){
            // File name is MAC address of device and current time.
            String fileTitle = adapter.address.get(i).replace(":", "") + "-" + LocalDate.now() + "-" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH시 mm분 ss초")) + ".csv";
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileTitle);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter writer = new FileWriter(file, false);
                double stdev_rssi = 0;
                double avg_rssi = 0;
                int sum_rssi = 0;
                double stdev_rssiKalman = 0;
                double avg_rssiKalman = 0;
                int sum_rssiKalman = 0;

                // get raw data
                String str = "rssi,rssiKarman" + '\n';
                for(int j = 0; j < adapter.rssi.get(i).size(); j++){
                    str += adapter.rssi.get(i).get(j);
                    sum_rssi += Integer.parseInt(adapter.rssi.get(i).get(j));
                    str += ',';
                    str += adapter.rssiKalman.get(i).get(j);
                    sum_rssiKalman += Integer.parseInt(adapter.rssiKalman.get(i).get(j));
                    str += '\n';
                }

                // get average
                str += "avg(rssi),avg(rssiKalman)\n";
                avg_rssi = (double)sum_rssi / (double)adapter.rssi.get(i).size();
                avg_rssiKalman = (double)sum_rssiKalman / (double)adapter.rssiKalman.get(i).size();
                str += "" + avg_rssi + ',' + avg_rssiKalman + '\n';

                // get standard deviation
                for(int j = 0; j < adapter.rssi.get(i).size(); j++){
                    stdev_rssi += Math.pow(avg_rssi - (double)Integer.parseInt(adapter.rssi.get(i).get(j)), 2);
                    stdev_rssiKalman += Math.pow(avg_rssiKalman - (double)Integer.parseInt(adapter.rssiKalman.get(i).get(j)), 2);
                }
                stdev_rssi = Math.sqrt(stdev_rssi / (double)adapter.rssi.get(i).size());
                stdev_rssiKalman = Math.sqrt(stdev_rssiKalman / (double)adapter.rssiKalman.get(i).size());

                str += "stdev(rssi),stdev(rssiKalman)\n";
                str += "" + stdev_rssi + "," + stdev_rssiKalman;

                // close the file
                writer.write(str);
                writer.close();
            } catch (IOException e) {

            }
        }
        Toast.makeText(this, "Log Generated", Toast.LENGTH_SHORT).show();
    }

    public void onClearClicked(View view) {
        kf.clear();
        adapter.clear();
        adapter.notifyDataSetChanged();
    }
}