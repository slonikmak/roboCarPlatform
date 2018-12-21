package com.oceanos.roboCarPlatform;

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

    public PID(double Kp, double Ki, double Kd){
        this.Kp = Kp;
        this.Ki = Ki;
        this.Kd = Kd;
    }

    public double calc(float headingDiff){
        double errorFunc = headingDiff;
        //System.out.println(headingDiff+ " error_func "+errorFunc);
        double result = resultOld + Kp*(errorFunc - errorOld) + Ki*(errorFunc + errorOld)/2 + Kd*(errorFunc-2*errorOld+errorOld2);
        double result2 = headingDiff*Kp;
        //System.out.println("result2 "+result2);

        /*double p = Kp*errorFunc;
        double i = lastI+Ki*errorFunc;
        double d = Kd*(errorFunc-errorOld);

        double result = p+i+d;*/

        if (result>1) result = 1;
        if (result<0) result = 0;

        resultOld = result;
        errorOld2 = errorOld;
        errorOld = errorFunc;
        return result;
    }
}
