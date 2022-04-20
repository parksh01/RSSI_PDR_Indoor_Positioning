package org.techtown.rssimeasureapp;

public class JavaConsoleTest {
    public static void main(String[] args){
        TriangulationLocation loc = new TriangulationLocation(0.76, 0.99, 0.76, 0.42);
        System.out.println(loc.x + ", " + loc.y);

        TriangulationLocation loc2 = new TriangulationLocation(0.76, 0.99);
        System.out.println(loc2.x + ", " + loc2.y);
    }
}
