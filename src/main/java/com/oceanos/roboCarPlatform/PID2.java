package com.oceanos.roboCarPlatform;

/**
 * @autor slonikmak on 21.01.2019.
 */
public class PID2 {
    double kP;
    double kI;
    double kD;

    double integral = 0;
    double derivative;
    double previous_error = 0;

    public PID2(double kP, double kI, double kD){
        this.kD = kD;
        this.kI = kI;
        this.kP = kP;
    }

    public double getOutput(double error){
        this.integral += (error*.02); // Integral is increased by the error*time (which is .02 seconds using normal IterativeRobot)
        derivative = (error - this.previous_error) / .02;
        previous_error = error;
        return kP*error + kI*this.integral + kD*derivative;
    }

    //error = setpoint - gyro.getAngle(); // Error = Target - Actual

}
