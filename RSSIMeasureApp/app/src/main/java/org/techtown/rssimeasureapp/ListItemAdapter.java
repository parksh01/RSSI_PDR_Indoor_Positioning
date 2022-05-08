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

    ArrayList<KalmanFilter> kf = new ArrayList<KalmanFilter>();

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

    public void addItem(String device, String address, String rssi, float RSSItoDist_A_value, float RSSItoDist_n_value){
        ArrayList<String> temp;

        this.device.add(device);
        this.address.add(address);

        temp = new ArrayList<String>();
        temp.add(rssi);
        this.rssi.add(temp);
        this.rssiKalman.add(temp);

        temp = new ArrayList<String>();
        temp.add(String.format("%.3f", Triangulation.RssiToDistance(Double.parseDouble(rssi), RSSItoDist_A_value, RSSItoDist_n_value)));
        this.distance.add(temp);

        this.tick.add("1");

        this.kf.add(new KalmanFilter());
    }
    public void setItem(String device, String address, String rssi, float RSSItoDist_A_value, float RSSItoDist_n_value, int index){
        double filteredRSSI = kf.get(index).filtering(Integer.parseInt(rssi));

        this.device.set(index, device);
        this.address.set(index, address);
        this.rssi.get(index).add(rssi);
        this.rssiKalman.get(index).add(String.format("%.3f", filteredRSSI));
        this.distance.get(index).add(String.format("%.3f", Triangulation.RssiToDistance(filteredRSSI, RSSItoDist_A_value, RSSItoDist_n_value)));
        this.tick.set(index, "" + (Integer.parseInt(this.tick.get(index)) + 1));
    }

    public void clear() {
        device.clear();
        address.clear();
        rssi.clear();
        rssiKalman.clear();
        distance.clear();
        tick.clear();
        kf.clear();
    }
}
