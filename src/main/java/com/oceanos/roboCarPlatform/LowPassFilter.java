package com.oceanos.roboCarPlatform;

/**
 * @autor slonikmak on 28.12.2018.
 */
public class LowPassFilter {
    double val = 0;
    double K = 0;

    public LowPassFilter(double Koeff){
        this.K = Koeff;
    }

    public double calc(double value){
        val = val*(1-K)+value*K;
        return val;
    }
}
