package com.oceanos.roboCarPlatform;

import java.util.Date;

/**
 * @autor slonikmak on 12.12.2018.
 */
public class PID {
    private double Kp;
    private double Ki;
    private double Kd;
    private double errorOld = 0;
    private double errorOld2 = 0;
    private double resultOld = 0;
    private double lastI = 0;
    private double iMin = -0.5;
    private double iMax = 0.5;
    private double iSum = 0;

    private long lastTime;

    public PID(double Kp, double Ki, double Kd){
        this.Kp = Kp;
        this.Ki = Ki;
        this.Kd = Kd;
        lastTime = new Date().getTime();
    }

    public double getOutput(double headingDiff){

        //long currTime = new Date().getTime();

        //System.out.println("Time period "+(currTime-lastTime));

        //lastTime = currTime;

        double errorFunc = headingDiff;
        //System.out.println(headingDiff+ " error_func "+errorFunc);

        //Version 1
        double result = resultOld + Kp*(errorFunc - errorOld) + Ki*(errorFunc + errorOld)/2 + Kd*(errorFunc-2*errorOld+errorOld2);
        double result2 = headingDiff*Kp;
        //System.out.println("result2 "+result2);

        //version 2
        /*double p = Kp*errorFunc;
        double i = lastI+Ki*errorFunc;
        double d = Kd*(errorFunc-errorOld);
        double result = p+i+d;*/

        //Version 3
        /*double p = Kp*errorFunc;
        iSum = iSum+errorFunc;
        if (iSum<iMin) iSum = iMin;
        if (iSum>iMax) iSum = iMax;
        double i = Ki*iSum;
        double d = Kd*(errorFunc-errorOld);
        double result = i+p+d;*/

        if (result>1) result = 1;
        if (result<0) result = 0;

        resultOld = result;
        errorOld2 = errorOld;
        errorOld = errorFunc;
        return result;
    }
}
