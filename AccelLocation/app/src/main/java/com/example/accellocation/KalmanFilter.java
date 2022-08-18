package com.example.accellocation;

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
        this.errorCovariance = 0; // it don't has any vector. so let it 0.
        this.initialized = false;
        this.processNoise = 1;
        this.measurementNoise = 4;
        this.predictedRSSI = 0;
    }
    public KalmanFilter(double processNoise, double measurementNoise){
        this.errorCovariance = 0; // it don't has any vector. so let it 0.
        this.initialized = false;
        this.processNoise = processNoise;
        this.measurementNoise = measurementNoise;
        this.predictedRSSI = 0;
    }
    public KalmanFilter(double errCov, double processNoise, double measurementNoise){
        this.errorCovariance = errCov;
        this.initialized = false;
        this.processNoise = processNoise;
        this.measurementNoise = measurementNoise;
        this.predictedRSSI = 0;
    }

    public double filtering(double val){ // rssi : m_t (current measured RSSI value)
        // Update
        if(!this.initialized){
            this.initialized = true;
            this.priorRSSI = (double)val;
            this.priorErrorCovariance = 1;
        }
        else {
            this.priorRSSI = this.predictedRSSI;
            this.priorErrorCovariance = this.errorCovariance + this.processNoise;
        }

        this.kalmanGain = this.priorErrorCovariance / (this.priorErrorCovariance + this.measurementNoise);
        this.errorCovarianceRssi = (1 - this.kalmanGain) * this.priorErrorCovariance;

        // Prediction
        this.predictedRSSI = this.priorRSSI + (this.kalmanGain * ((double)val - this.priorRSSI));
        return this.predictedRSSI;
    }
    public double filter(double val){
        return filtering(val);
    }
}
