package org.techtown.rssimeasureapp;

public class KalmanFilter {
    public int errorCovariance = 0;
    public double errorCovarianceRssi;
    public boolean initialized = false;
    public double processNoise = 0.005;
    public double measurementNoise = 20;
    public double predictedRSSI = 0;

    public int filtering(int rssi){
        int priorRSSI;
        double priorErrorCovariance;
        double kalmanGain;

        if(!this.initialized){
            this.initialized = true;
            priorRSSI = rssi;
            priorErrorCovariance = 1;
        }
        else{ // if this.initialized == true
            priorRSSI = (int) this.predictedRSSI;
            priorErrorCovariance = this.errorCovariance + this.processNoise;
        }

        kalmanGain = priorErrorCovariance / (priorErrorCovariance + this.measurementNoise);
        this.predictedRSSI = priorRSSI + (kalmanGain * (rssi - priorRSSI));
        this.errorCovarianceRssi = (1 - kalmanGain) * priorErrorCovariance;

        return (int)this.predictedRSSI;
    }
}
