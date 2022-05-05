package org.techtown.rssimeasureapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JavaConsole {
    public static void main(String[] args) throws IOException {
        String fileLocation = "C:\\ParkingLot\\ParkingLot\\RSSICurveFitting\\inputdata\\front\\";
        for(int i = 1;i<=7;i++){
            KalmanFilter kf = new KalmanFilter();
            String fileName = "" + i + "m front.csv";
            System.out.println(fileLocation + fileName);
            BufferedReader reader = new BufferedReader(
                    new FileReader(fileLocation + fileName)
            );

            String fileNameOutput = "kf\\" + i + "m front(kalman).csv";
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(fileLocation + fileNameOutput)
            );
            String str;
            while ((str = reader.readLine()) != null) {
                System.out.println(kf.filtering(Integer.parseInt(str)));
                writer.write("" + kf.filtering(Integer.parseInt(str)) + '\n');
            }
            kf = null;
            reader.close();
            writer.close();
        }
    }
}
