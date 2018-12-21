package com.oceanos.roboCarPlatform;

import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * @autor slonikmak on 11.12.2018.
 */
public class Car {
    private Consumer<Car> consumer;

    private int left = 0;
    private int right = 0;
    private int direction = 1;
    private double heading = 0;
    private boolean running = true;

    public void setConsumer(Consumer<Car> consumer){
        this.consumer = consumer;
    }

    public void setThruster(int left, int right, int dir){
        this.left = left;
        this.right = right;
        this.direction = dir;

        System.out.println("Set thruster "+left+" "+right+" "+dir);

        this.consumer.accept(this);
    }

    public void setHeading(double heading){
        this.heading = heading;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getDirection() {
        return direction;
    }

    public double getHeading() {
        return heading;
    }

    public void startCompassCalibration() throws InterruptedException {
        running = true;
        for (int i = 0; i < 1600; i++) {
            if (running){
                setThruster(0,70,1);
                Thread.sleep(20);
                setThruster(0,0,1);
                Thread.sleep(20);
            } else {
                break;
            }
        }
    }

    public void stop(){
        running = false;
        setThruster(0,0,0);
    }

    public void goToHeading(double destHeading, float kp, float ki, float kd){
        System.out.println("-------Going To Heading "+destHeading+", Kp"+kp+", ki"+ki+", kd"+kd+"--------");
        running = true;
        PID pid = new PID(kp, ki, kd);
        new Thread(()->{
            PidLogManager logManager = new PidLogManager();
           while (running){
               double diffHeading = getDeviation(heading, destHeading);

               System.out.println("heading: "+heading);

               logManager.addData(diffHeading);
               //if (diffHeading <5 && diffHeading >-5) stop();

               double result = pid.calc((float) Math.abs(diffHeading/180f));

               int resultValue = (int) (result*100);
               if (resultValue>100) resultValue = 100;

               if (diffHeading>5) {
                   setThruster((int) resultValue, 0, 0);
               } else if (diffHeading<-5) {
                   setThruster(0, resultValue, 0);
                   //setThruster((int) resultValue, 0, 1);
               } else {
                   setThruster(0, 0, 0);
               }
               System.out.println("PID TEST: diff: "+diffHeading+" PID result: "+result+" left: "+left+" right: "+right);
               try {
                   Thread.sleep(20);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
           logManager.saveData(Paths.get("log.txt"));


        }).start();
    }

    public void followHeading(double destHeading, int speed, float kp, float ki, float kd){
        System.out.println("-------Follow Heading "+destHeading+" speed "+speed+"--------");
        running = true;
        PID pid = new PID(kp, ki, kd);
        new Thread(()->{
            while (running){
                double diffHeading = destHeading-heading;

                //if (diffHeading <5 && diffHeading >-5) stop();

                double result = pid.calc((float) Math.abs(diffHeading));


                double resultValue = (1 - result)*speed;
                //if (resultValue>100) resultValue = 100;

                System.out.println("PID TEST: diff: "+diffHeading+" left: "+left+" right: "+right+" result: "+resultValue);

                if (diffHeading>5) {
                    setThruster(speed, (int) resultValue, 0);
                } else if (diffHeading<-5) {
                    setThruster((int) resultValue, speed, 0);
                } else setThruster(speed,speed,0);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public double getDeviation(double heading, double destHeading){
        double deviation = destHeading - heading;
        if (deviation > 180) {
            deviation -= 180;
            deviation = (180 - deviation) * -1;
        }
        if (deviation < -180) {
            deviation += 360;
        }
        // Инвертируем
        //deviation *= -1;
        return deviation;
    }
}
