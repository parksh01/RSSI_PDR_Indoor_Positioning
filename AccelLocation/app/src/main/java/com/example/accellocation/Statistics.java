package com.example.accellocation;

import java.util.ArrayList;

public class Statistics {
    static public float stdev(ArrayList<Float> val){
        float avg = 0;
        for(int i = 0; i < val.size(); i++){
            avg += val.get(i);
        }
        avg = avg / val.size();

        float stdev = 0;
        for(int i = 0; i< val.size(); i++){
            stdev += (avg - val.get(i)) * (avg - val.get(i));
        }
        stdev = (float) Math.sqrt(stdev / val.size());
        return stdev;
    }
}
