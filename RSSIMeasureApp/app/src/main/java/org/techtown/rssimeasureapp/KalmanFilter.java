package org.techtown.rssimeasureapp;

public class KalmanFilter {
    public boolean initialized;

    public double errorCovariance; //
    public double errorCovarianceRssi; // P_t
    public double processNoise; // Q
    public double measurementNoise; // R
    public double predictedRSSI; // s_t

    public double priorRSSI; // s'_t
    public double priorErrorCovariance; // P'_t

    public double kalmanGain; // K_t

    public KalmanFilter(){
        this.errorCovariance = 0;
        this.initialized = false;
        this.processNoise = 10;
        this.measurementNoise = 10;
        this.predictedRSSI = 0;
    }

    public int filtering(int rssi){ // rssi : m_t (current measured RSSI value)
        // Update
        if(!this.initialized){
            this.initialized = true;
            this.priorRSSI = (double)rssi;
            this.priorErrorCovariance = 1;
        }
        else {
            this.priorRSSI = this.predictedRSSI;
            this.priorErrorCovariance = this.errorCovariance + this.processNoise;
        }

        this.kalmanGain = this.priorErrorCovariance / (this.priorErrorCovariance + this.measurementNoise);
        this.errorCovarianceRssi = (1 - this.kalmanGain) * this.priorErrorCovariance;

        // Prediction
        this.predictedRSSI = this.priorRSSI + (this.kalmanGain * ((double)rssi - this.priorRSSI));
        return (int)this.predictedRSSI;
    }
}
