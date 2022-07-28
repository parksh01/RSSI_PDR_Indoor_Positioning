package org.techtown.rssimeasureapp;

import static java.lang.Math.*;

import java.util.ArrayList;

public class Triangulation {
    public static double[] CalculateCoordinate(ArrayList<Float> currentDistance, ArrayList<Beacon> beacon) { // distance between each beacon and my device.
        double[] val = new double[2];
        if(currentDistance.size() == 3) {
            // Beacon #1 is on 0,0
            // Beacon #2 is on U,0
            // Beacon #3 is on Vx, Vy
            double r1 = currentDistance.get(0);
            double r2 = currentDistance.get(1);
            double r3 = currentDistance.get(2);
            double U = beacon.get(1).X;
            double Vx = beacon.get(2).X;
            double Vy = beacon.get(2).Y;
            double x = (r1*r1 - r2*r2 + U*U) / (2*U);
            double y = (r1*r1 - r3*r3 + Vx*Vx + Vy*Vy - 2*Vx*x) / (2*Vy);
            val[0] = x;
            val[1] = y;
        }
        else if(currentDistance.size() == 4){
            // Beacon #1 is on 0, 0
            // Beacon #2 is on U, 0
            // Beacon #3 is on 0, V
            // Beacon #4 is on U, V
            double U = beacon.get(1).X;
            double V = beacon.get(2).Y;
            double r1 = currentDistance.get(0);
            double r2 = currentDistance.get(1);
            double r3 = currentDistance.get(2);
            double r4 = currentDistance.get(3);

            double x1 = (r2*r2 - r1*r1 - U*U) / (-2 * U);
            double y1 = Math.sqrt(r1*r1 - x1*x1);

            double y2 = (r3*r3 - r1*r1 - V*V) / (-2 * V);
            double x2 = Math.sqrt(r1*r1 - y2*y2);

            double x3 = (r4*r4 - r3*r3 - U*U) / (-2 * U);
            double y3 = Math.sqrt(r3*r3 - x3*x3) + V;

            double y4 = (r4*r4 - r2*r2 - V*V) / (-2 * V);
            double x4 = Math.sqrt(r2*r2 - y4*y4) + U;

            val[0] = (x1+x2+x3+x4)/4;
            val[1] = (y1+y2+y3+y4)/4;
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
