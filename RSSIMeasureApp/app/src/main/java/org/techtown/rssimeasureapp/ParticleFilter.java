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
    float bound;

    public ParticleFilter(int particleCount, float bound){
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

    public float[] filtering(float[] inputCoordinate){
        float[] val = new float[2];

        // Prediction
        for(int i = 0;i<this.particleCount;i++){
            particles.get(i).x += (Math.random() * this.bound - (this.bound/2));
            particles.get(i).y += (Math.random() * this.bound - (this.bound/2));
            particles.get(i).weight = 0.0;
        }

        // Update - The particle closer to measured coordinate gets higher weight.
        for(int i = 0;i<this.particleCount;i++){
            particles.get(i).weight = (1.0 / (Math.sqrt(Math.pow(particles.get(i).x - inputCoordinate[0], 2.0) + Math.pow(particles.get(i).y - inputCoordinate[1], 2.0))));
        }

        // Resampling
        // While resampling, weights smaller than median will be discarded
        ArrayList<Double> weights = new ArrayList<Double>();
        for(int i = 0;i<this.particleCount;i++){
            weights.add(particles.get(i).weight);
        }
        double median = getMedian(weights);
        for(int i = 0;i<this.particleCount;i++){
            if(weights.get(i) < median){
                particles.remove(i);
            }
        }
        // Filtered value will be the particle's coordination which has biggest weight.
        Collections.sort(particles);
        val[0] = (float) particles.get(particles.size() - 1).x;
        val[1] = (float) particles.get(particles.size() - 1).y;

        // Implementing weighted random choice
        ArrayList<Particle> newParticle = new ArrayList<Particle>();
        for(int i = 0;i<particleCount;i++){
            newParticle.add(randomChoice(particles));
        }
        particles.clear();
        particles = (ArrayList<Particle>)newParticle.clone();

        return val;
    }

    private Particle randomChoice(ArrayList<Particle> particles) {
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
            if(cumWeights.get(i) <= pick && pick < cumWeights.get(i + 1)){
                pickedParticle = i;
                break;
            }
        }

        return particles.get(pickedParticle);
    }
}
