package com.oceanos.roboCarPlatform;

/**
 * @autor slonikmak on 12.12.2018.
 */
public class TestPid {
    private float Kp;
    private float Ki;
    private float Kd;
    private float errorOld = 0;
    private float errorOld2 = 0;
    private float resultOld = 0;

    public TestPid(float Kp, float Ki, float Kd){
        this.Kp = Kp;
        this.Ki = Ki;
        this.Kd = Kd;
    }

    public float calc(float headingDiff){
        float errorFunc = headingDiff/180;
        float result = resultOld + Kp*(errorFunc - errorOld) + Ki*(errorFunc + errorOld)/2;

        if (result>1) result = 1;
        if (result<1) result = 0;

        resultOld = result;
        errorOld = errorFunc;
        return result;
    }
}
