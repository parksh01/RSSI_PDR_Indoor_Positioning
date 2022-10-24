package org.techtown.rssimeasureapp;

import static java.lang.Math.*;

import java.util.ArrayList;

public class Triangulation {
    public static double[] CalculateCoordinate(ArrayList<Float> currentDistance, ArrayList<Beacon> beacon) { // distance between each beacon and my device.
        double[] val = new double[2];
        if(currentDistance.size() == 3) {
            // Beacon #1 is on 0,Vy
            // Beacon #2 is on U,0
            // Beacon #3 is on Vx, Vy
            double r1 = currentDistance.get(0);
            double r2 = currentDistance.get(1);
            double r3 = currentDistance.get(2);
            double U = beacon.get(1).X;
            double Vx = beacon.get(2).X;
            double Vy = beacon.get(2).Y;
            double x = (r3*r3 - r1*r1 + Vx*Vx) / (-2*Vx);
            double y = (r3*r3 - r2*r2 + (2*Vx - 2*U)*x - Vx*Vx + U*U - Vy*Vy) / (-2*Vy);
            val[0] = x;
            val[1] = y;
        }
        else{
            // Placeholder.
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
