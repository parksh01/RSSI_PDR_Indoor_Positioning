package org.techtown.rssimeasureapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    ListView listView;
    ListItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);

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
                        if(index == -1){
                            adapter.addItem(device.getName(), device.getAddress(), Integer.toString(rssi));
                        }
                        else{
                            adapter.device.set(index, device.getName());
                            adapter.address.set(index, device.getAddress());
                            adapter.rssi.set(index, Integer.toString(rssi));
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


}