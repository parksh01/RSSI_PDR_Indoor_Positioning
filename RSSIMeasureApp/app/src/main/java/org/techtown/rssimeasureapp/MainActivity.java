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
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    // To display measured RSSI values.
    ListView listView;
    ListItemAdapter adapter;

    // RSSI to Distance mapping coefficients.
    EditText RSSItoDist_A;
    EditText RSSItoDist_n;

    // Counting input ticks for each detected devices
    ArrayList<Integer> tick = new ArrayList<Integer>();

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

        String RSSItoDist_A_value = PrefManager.getString(this, "RSSItoDist_A", "-70");
        RSSItoDist_A = findViewById(R.id.RSSItoDist_A);
        RSSItoDist_A.setText(RSSItoDist_A_value);

        String Kalman_n_value = PrefManager.getString(this, "RSSItoDist_n", "2");
        RSSItoDist_n = findViewById(R.id.RSSItoDist_n);
        RSSItoDist_n.setText(Kalman_n_value);
    }

    @Override
    protected void onPause() {
        // Store RSSI to distance coefficients for further use.
        super.onPause();
        PrefManager.setString(this, "RSSItoDist_A", RSSItoDist_A.getText().toString());
        PrefManager.setString(this, "RSSItoDist_n", RSSItoDist_n.getText().toString());
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
                    if (
                        // filters only desired BLE devices (beacons)
                            getString(R.string.BeaconAddress01).equals(device.getAddress()) ||
                            getString(R.string.BeaconAddress02).equals(device.getAddress()) ||
                            getString(R.string.BeaconAddress03).equals(device.getAddress()) ||
                            getString(R.string.BeaconAddress04).equals(device.getAddress())
                    ) {
                        float RSSItoDist_A_value = 0;
                        float RSSItoDist_n_value = 0;
                        if (RSSItoDist_A.getText().toString().length() != 0 && RSSItoDist_n.getText().toString().length() != 0) {
                            RSSItoDist_A_value = Float.parseFloat(RSSItoDist_A.getText().toString());
                            RSSItoDist_n_value = Float.parseFloat(RSSItoDist_n.getText().toString());
                        }

                        // Update beacon list.
                        // first, check if the beacon is already discovered. (if not, index is -1)
                        int index = adapter.address.indexOf(device.getAddress());

                        // get beacon number from MAC address
                        String beaconNumber = "Unknown";
                        if(getString(R.string.BeaconAddress01).equals(device.getAddress())){
                            beaconNumber = "Beacon #1";
                        }
                        else if(getString(R.string.BeaconAddress02).equals(device.getAddress())){
                            beaconNumber = "Beacon #2";
                        }
                        else if(getString(R.string.BeaconAddress03).equals(device.getAddress())){
                            beaconNumber = "Beacon #3";
                        }

                        // if there is new beacon discovered, add it to the device list.
                        if (index == -1) {
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(getApplicationContext(), "'근처 기기'를 허용해주세요", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            adapter.addItem(beaconNumber, device.getAddress(), Integer.toString(rssi), RSSItoDist_A_value, RSSItoDist_n_value);
                        }
                        // if there is a beacon already in the device list, update it.
                        else {
                            adapter.setItem(beaconNumber, device.getAddress(), Integer.toString(rssi), RSSItoDist_A_value, RSSItoDist_n_value, index);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

    public void onScanClicked(View view) {
        Toast.makeText(this, "Scan Start", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "'근처 기기'를 허용해주세요", Toast.LENGTH_SHORT).show();

            return;
        }
        bluetoothAdapter.startLeScan(leScanCallback);
    }

    public void onStopClicked(View view) {
        Toast.makeText(this, "Scan Stop", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "'근처 기기'를 허용해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
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

                double stdev_distance = 0;
                double avg_distance = 0;
                double sum_distance = 0;

                // get raw data
                String str = "rssi,rssiKarman,distance" + '\n';
                for(int j = 0; j < adapter.rssi.get(i).size(); j++){
                    str += adapter.rssi.get(i).get(j);
                    sum_rssi += Integer.parseInt(adapter.rssi.get(i).get(j));
                    str += ',';

                    str += adapter.rssiKalman.get(i).get(j);
                    sum_rssiKalman += Integer.parseInt(adapter.rssiKalman.get(i).get(j));
                    str += ',';

                    str += adapter.distance.get(i).get(j);
                    sum_distance += Double.parseDouble(adapter.distance.get(i).get(j));
                    str += '\n';
                }

                // get average
                str += "avg(rssi),avg(rssiKalman),avg(distance)\n";
                avg_rssi = (double)sum_rssi / (double)adapter.rssi.get(i).size();
                avg_rssiKalman = (double)sum_rssiKalman / (double)adapter.rssiKalman.get(i).size();
                avg_distance = sum_distance / (double)adapter.distance.get(i).size();
                str += "" + avg_rssi + ',' + avg_rssiKalman + ',' + avg_distance + '\n';

                // get standard deviation
                for(int j = 0; j < adapter.rssi.get(i).size(); j++){
                    stdev_rssi += Math.pow(avg_rssi - (double)Integer.parseInt(adapter.rssi.get(i).get(j)), 2);
                    stdev_rssiKalman += Math.pow(avg_rssiKalman - (double)Integer.parseInt(adapter.rssiKalman.get(i).get(j)), 2);
                    stdev_distance += Math.pow(avg_distance - Double.parseDouble(adapter.distance.get(i).get(j)), 2);
                }
                stdev_rssi = Math.sqrt(stdev_rssi / (double)adapter.rssi.get(i).size());
                stdev_rssiKalman = Math.sqrt(stdev_rssiKalman / (double)adapter.rssiKalman.get(i).size());
                stdev_distance = Math.sqrt(stdev_distance / (double)adapter.distance.get(i).size());

                str += "stdev(rssi),stdev(rssiKalman),stdev(distance)\n";
                str += "" + stdev_rssi + "," + stdev_rssiKalman + "," + stdev_distance;

                // close the file
                writer.write(str);
                writer.close();
            } catch (IOException e) {

            }
        }
        Toast.makeText(this, "Log Generated", Toast.LENGTH_SHORT).show();
    }

    public void onClearClicked(View view) {
        tick.clear();
        adapter.clear();
        adapter.notifyDataSetChanged();
    }
}