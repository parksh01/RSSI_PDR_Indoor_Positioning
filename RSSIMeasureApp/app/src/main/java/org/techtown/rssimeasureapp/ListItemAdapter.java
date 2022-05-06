package org.techtown.rssimeasureapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class ListItemAdapter extends BaseAdapter {
    ArrayList<String> device = new ArrayList<String>();
    ArrayList<String> address = new ArrayList<String>(); // each devices are distinguished by its MAC address.
    ArrayList<ArrayList<String>> rssi = new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<String>> rssiKalman = new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<String>> distance = new ArrayList<ArrayList<String>>();
    ArrayList<String> tick = new ArrayList<String>();
    Context context;

    @Override
    public int getCount() {
        return address.size();
    }

    @Override
    public Object getItem(int i) {
        return address.get(i);
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

        deviceText.setText(this.device.get(position));
        addressText.setText(this.address.get(position));
        rssiText.setText(this.rssi.get(position).get(this.rssi.get(position).size() - 1) +
                " / kf : " +
                this.rssiKalman.get(position).get(this.rssiKalman.get(position).size() - 1) +
                " / d : " +
                this.distance.get(position).get(this.distance.get(position).size() - 1) +
                " tick : " +
                this.tick.get(position));
        return convertView;
    }

    public void addItem(String device, String address, String rssi, String rssiKalman, String distance, String tick){
        ArrayList<String> temp;

        this.device.add(device);
        this.address.add(address);

        temp = new ArrayList<String>();
        temp.add(rssi);
        this.rssi.add(temp);

        temp = new ArrayList<String>();
        temp.add(rssiKalman);
        this.rssiKalman.add(temp);

        temp = new ArrayList<String>();
        temp.add(distance);
        this.distance.add(temp);

        this.tick.add(tick);
    }
    public void setItem(String device, String address, String rssi, String rssiKalman, String distance, String tick, int index){
        this.device.set(index, device);
        this.address.set(index, address);
        this.rssi.get(index).add(rssi);
        this.rssiKalman.get(index).add(rssiKalman);
        this.distance.get(index).add(distance);
        this.tick.set(index, tick);
    }

    public void clear() {
        device.clear();
        address.clear();
        rssi.clear();
        rssiKalman.clear();
        distance.clear();
        tick.clear();
    }
}
