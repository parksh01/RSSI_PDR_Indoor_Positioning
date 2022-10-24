package org.techtown.rssimeasureapp;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class ParticleFilter {
    class Particle implements Comparable<Particle>{
        public double x;
        public double y;
        public double weight;

        Particle(double x, double y, double weight){
            this.x = x;
            this.y = y;
            this.weight = weight;
        }
        Particle(double x, double y){
            this.x = x;
            this.y = y;
            this.weight = 0.0;
        }
        @Override
        public int compareTo(Particle particle) {
            if(particle.weight < this.weight) return 1;
            else if(particle.weight > this.weight) return -1;
            return 0;
        }
    }
    // Particles will be generated in this dimension.
    float dimX, dimY;
    // Particles will be generated as many as this value.
    int particleCount;
    // Particle : [x, y, weight]
    ArrayList<Particle> particles;
    // Particle move boundary
    double bound;

    public ParticleFilter(int particleCount, double bound){
        this.particleCount = particleCount;
        this.bound = bound;
        this.particles = new ArrayList<Particle>();
    }
    public void setDimension(ArrayList<Beacon> beacon){
        if(beacon.size()==3 || beacon.size()==4){
            this.dimX = (float) beacon.get(1).X;
            this.dimY = (float) beacon.get(2).Y;
        }
    }

    public void initParticles(){
        for(int i = 0;i<particleCount;i++){
            this.particles.add(new Particle(Math.random()*dimX, Math.random()*dimY));
        }
    }

    public Double getMedian(ArrayList<Double> values) {
        ArrayList<Double> temp = values;
        Collections.sort(temp);
        return temp.get((temp.size()) / 2);
    }

    public double[] filtering(double[] inputCoordinate){
        double[] val = new double[2];

        // Prediction
        for(int i = 0;i<this.particleCount;i++){
            particles.get(i).x += (Math.random() * this.bound - (this.bound/2.0));
            particles.get(i).y += (Math.random() * this.bound - (this.bound/2.0));
            particles.get(i).weight = 0.0;
        }

        // Update - The particle closer to measured coordinate gets higher weight.
        for(int i = 0;i<this.particleCount;i++){
            particles.get(i).weight = giveWeight(particles.get(i), inputCoordinate);
        }

        for(int i=0;i<particles.size();i++){
            Log.d("particle", String.format("%.3f", particles.get(i).x) + ',' + String.format("%.3f", particles.get(i).y) + '/' + String.format("%.3f", particles.get(i).weight));
        }

        // Resampling
        Collections.sort(particles, Collections.reverseOrder());

        // Filtered value will be the particle's coordination which has biggest weight.
        val[0] = particles.get(0).x;
        val[1] = particles.get(0).y;
        // Finally, perform resampling.
        ArrayList<Particle> newParticle = new ArrayList<Particle>();
        for(int i = 0;i<particleCount;i++){
            int picked = randomChoice(particles);
            newParticle.add(new Particle(particles.get(picked).x, particles.get(picked).y, 0.0));
        }
        particles.clear();
        particles = (ArrayList<Particle>)newParticle.clone();

        return val;
    }

    private double giveWeight(Particle particle, double[] inputCoordinate){
        return (1.0 / (Math.sqrt(Math.pow(particle.x - inputCoordinate[0], 2.0) + Math.pow(particle.y - inputCoordinate[1], 2.0))));
    }

    private int randomChoice(ArrayList<Particle> particles) {
        // First, get the array of cumulated weights.
        ArrayList<Double> cumWeights = new ArrayList<Double>();
        cumWeights.add(0.0);
        for(int i = 0;i<particles.size();i++){
            cumWeights.add(cumWeights.get(i) + particles.get(i).weight);
        }
        // And pick the random weight.
        double pick = Math.random() * cumWeights.get(cumWeights.size() - 1);
        int pickedParticle = -1;
        for(int i = 0;i<particles.size();i++){
            pickedParticle = i;
            if(cumWeights.get(i) <= pick && pick < cumWeights.get(i + 1)){
                break;
            }
        }

        return pickedParticle;
    }
}
