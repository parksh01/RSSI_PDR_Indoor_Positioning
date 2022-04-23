package org.techtown.rssimeasureapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListItemAdapter extends BaseAdapter {
    ArrayList<String> device = new ArrayList<String>();
    ArrayList<String> address = new ArrayList<String>(); // each devices are distinguished by its MAC address.
    ArrayList<ArrayList<String>> rssi = new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<String>> rssiKalman = new ArrayList<ArrayList<String>>();
    ArrayList<String> distance = new ArrayList<String>();
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
        rssiText.setText(this.rssi.get(position).get(this.rssi.get(position).size() - 1) + " / filtered : " + this.rssiKalman.get(position).get(this.rssiKalman.get(position).size() - 1));

        return convertView;
    }

    public void addItem(String device, String address, String rssi, String rssiKalman){
        this.device.add(device);
        this.address.add(address);
        ArrayList<String> temp = new ArrayList<String>();
        temp.add(rssi);
        this.rssi.add(temp);
        temp = new ArrayList<String>();
        temp.add(rssiKalman);
        this.rssiKalman.add(temp);
        this.distance.add("" + Triangulation.RssiToDistance(Integer.parseInt(rssi)));
    }
    public void setItem(String device, String address, String rssi, String rssiKalman, int index){
        this.device.set(index, device);
        this.address.set(index, address);
        this.rssi.get(index).add(rssi);
        this.rssiKalman.get(index).add(rssiKalman);
        this.distance.set(index, "" + Triangulation.RssiToDistance(Integer.parseInt(rssi)));
    }
}
