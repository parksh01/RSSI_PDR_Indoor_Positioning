package org.techtown.rssimeasureapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class ListItemAdapter extends BaseAdapter {
    Context context;

    public ArrayList<Beacon> beacon = new ArrayList<Beacon>();

    @Override
    public int getCount() {
        return beacon.size();
    }

    @Override
    public Object getItem(int i) {
        return beacon.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        context = parent.getContext();

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        TextView deviceText = convertView.findViewById(R.id.device);
        TextView addressText = convertView.findViewById(R.id.address);
        TextView rssiText = convertView.findViewById(R.id.rssi);

        deviceText.setText("Beacon #" + Integer.toString(this.beacon.get(position).beaconNumber));
        addressText.setText(this.beacon.get(position).MACaddress);
        rssiText.setText(this.beacon.get(position).rssi.get(this.beacon.get(position).tick - 1) +
                " / kf : " +
                this.beacon.get(position).rssiKalman.get(this.beacon.get(position).tick - 1) +
                " / d : " +
                this.beacon.get(position).distance.get(this.beacon.get(position).tick - 1) +
                " tick : " +
                Integer.toString(this.beacon.get(position).tick));

        return convertView;
    }

    public void clear() {
        beacon.clear();
    }
}
