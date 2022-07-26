package org.techtown.rssimeasureapp;

import static java.lang.Math.*;

import java.util.ArrayList;

public class Triangulation {
    public double x, y; // location of my device
    // coordinations of each beacon are denoted as :
    // Beacon 1 = (0, 0)
    // Beacon 2 = (1, 0)
    // Beacon 3 = (0, 1)
    // Beacon 4 = (1, 1)

    public static float[] CalculateCoordinate(ArrayList<Float> currentDistance, ArrayList<Beacon> beacon) { // distance between each beacon and my device.
        float[] val = new float[2];
        if(currentDistance.size() == 3) {
            float r1 = currentDistance.get(0);
            float r2 = currentDistance.get(1);
            float r3 = currentDistance.get(2);
            float U = (float) beacon.get(1).X;
            float Vx = (float) beacon.get(2).X;
            float Vy = (float) beacon.get(2).Y;
            float x = (r1*r1 - r2*r2 + U*U) / (2*U);
            float y = (r1*r1 - r3*r3 + Vx*Vx + Vy*Vy - 2*Vx*x) / (2*Vy);
            val[0] = x;
            val[1] = y;
        }
        else{
            val[0] = (float) 1.2345;
            val[1] = (float) 1.2345;
        }
        return val;
    }

    public static double RssiToDistance(int rssi, double A, double n){
        return Math.pow(10, ((double)rssi-A)/((-10)*n));
    }

    public static double RssiToDistance(double rssi, double A, double n){
        return Math.pow(10, (rssi-A)/((-10)*n));
    }
}
