package org.techtown.rssimeasureapp;

import static java.lang.Math.*;

public class Triangulation {
    public double x, y; // location of my device
    // coordinations of each beacon are denoted as :
    // Beacon 1 = (0, 0)
    // Beacon 2 = (1, 0)
    // Beacon 3 = (0, 1)
    // Beacon 4 = (1, 1)

    public void CalculateCoordinate(double r1, double r2, double r3, double r4) { // distance between each beacon and my device.
        double x1 = (r2*r2 - r1*r1 - 1)/(-2);
        double x2 = (r3*r3 - r4*r4 - 1)/(-2);
        double y1 = (r3*r3 - r2*r2 - 1)/(-2);
        double y2 = (r4*r4 - r1*r1 - 1)/(-2);

        x = (x1+x2)/2;
        y = (y1+y2)/2;
    }

    public void CalculateCoordinate(double r1, double r2) { // distance between each beacon and my device.
        x = (r2*r2 - r1*r1 - 1)/(-2);
        y = (r1*(sin(acos(x/r1))));
    }

    public Triangulation(){
        x = 0;
        y = 0;
    }
    public Triangulation(double r1, double r2, double r3, double r4){
        CalculateCoordinate(r1, r2, r3, r4);
    }
    public Triangulation(double r1, double r2){
        CalculateCoordinate(r1, r2);
    }

    public static double RssiToDistance(int rssi, double A, double n){
        return Math.pow(10, ((double)rssi-A)/((-10)*n));
    }

    public static double RssiToDistance(double rssi, double A, double n){
        return Math.pow(10, (rssi-A)/((-10)*n));
    }
}
