package com.oceanos.roboCarPlatform;

/**
 * @autor slonikmak on 10.12.2018.
 */
public class PID {
    /**
     * Интервал расчетов.
     */
    double DT;

    double Kp = 2, Ki = 1, Kd = 0;
    double lastError;

    double value;

    public PID(double value, long DT, double Kp, double Ki, double Kd) {
        this.DT = (float) DT / 1000;
        this.Kp = Kp;
        this.Ki = Ki;
        this.Kd = Kd;
        this.value = value;
    }

    public void setValue(double value) {

        this.value = value;
    }
    public double getValue(){
        return value;
    }

    public void setCoefficients(double Kp, double Ki, double Kd) {
        this.Kp = Kp;
        this.Ki = Ki;
        this.Kd = Kd;
    }

    double P = 0;
    double D = 0;
    double I = 0;
    double correction;

    public double update(double inputValue) {

        P = 0;
        D = 0;
        I = 0;

        P = (value - inputValue);
        I = (I + (value - inputValue) * DT);
        D = (((value - inputValue) - lastError) / DT);
        lastError = value - inputValue;
        correction = (((Kp * P) + (Ki * I) + (Kd * D)) * DT);

        return correction;

    }
}
