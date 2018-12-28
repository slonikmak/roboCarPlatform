package com.oceanos.roboCarPlatform;

import com.oceanos.ros.core.connections.UDPServer;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * @autor slonikmak on 11.12.2018.
 */
public class Car {
    private Consumer<Car> consumer;

    private int left = 0;
    private int right = 0;
    private int wSpeed = 0;
    private int direction = 1;
    private double heading = 0;
    private boolean running = true;
    private long startTime;
    private long headingDurationTime;

    private UDPServer server;

    public Car(){
        startTime = System.currentTimeMillis();
        headingDurationTime = System.currentTimeMillis();
        try {
            server = new UDPServer(4449);
            new Thread(()->{
                server.start();
            }).start();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


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
        heading = Math.round(heading);
        int deltaHeading = (int) Math.round(getDeviation(this.heading, heading));
        //System.out.println("DELTA HEADING: "+deltaHeading);
        long currTime = System.currentTimeMillis();
        long deltaTime = currTime - headingDurationTime;
        //System.out.println("DELTA TIME: "+deltaTime);
        wSpeed = (int) (deltaHeading*1000/deltaTime);
        this.heading = heading;
        headingDurationTime = currTime;
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
            PidLogManager logManager = new PidLogManager(server);
           while (running){
               double diffHeading = getDeviation(heading, destHeading);

               //System.out.println("heading: "+heading);


               //if (diffHeading <5 && diffHeading >-5) stop();

               double result = pid.getOutput(Math.abs(diffHeading));

               int resultValue = (int) result*100;
               if (resultValue>100) resultValue = 100;
               if (resultValue<0) resultValue = 0;

               if (diffHeading>5) {
                   setThruster(resultValue, 0, 0);
                   logManager.addData(diffHeading, result, (double) wSpeed);
               } else if (diffHeading<-5) {
                   setThruster(0, resultValue, 0);
                   logManager.addData(diffHeading, result*(-1), (double) wSpeed);
                   //setThruster((int) resultValue, 0, 1);
               } else {
                   setThruster(0, 0, 0);
                   logManager.addData(diffHeading, 0d, (double) wSpeed);
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
        System.out.println("--------Follow Heading "+destHeading+" speed "+speed+"--------");
        running = true;
        //PID pid = new PID(kp, ki, kd);
        MiniPID pid = new MiniPID(kp,ki,kd);
        int minSpeed = 10;
        new Thread(()->{
            PidLogManager logManager = new PidLogManager(server);

            while (running){
                long time = System.currentTimeMillis();
                double diffHeading = getDeviation(heading, destHeading);

                //if (diffHeading <5 && diffHeading >-5) stop();
                double error = Math.abs(diffHeading);
                /*if (diffHeading >90) error = 1;
                else error = (Math.abs(diffHeading)/90);*/
                double pidResult = Math.abs(pid.getOutput(error));
                double result = 100*(1-pidResult);

                System.out.println("PIDRESULT: "+pidResult);
                if (result<minSpeed) result = minSpeed;
                if (result>speed) result = speed;
                double resultValue = result;

                //double resultValue = (1 - result)*(speed-minSpeed)+minSpeed;
                //if (resultValue>100) resultValue = 100;


                System.out.println("PID TEST: diff: "+diffHeading+" left: "+left+" right: "+right+" result: "+resultValue);

                if (diffHeading>10) {
                    setThruster(speed, (int) resultValue, 0);
                    logManager.addData(diffHeading, resultValue, (double) wSpeed);
                } else if (diffHeading<-10) {
                    setThruster((int) resultValue, speed, 0);
                    logManager.addData(diffHeading, resultValue*(-1), (double) wSpeed);
                } else {
                    setThruster(speed,speed,0);
                    logManager.addData(diffHeading, 0d, (double) wSpeed);
                }

                long sleepTime = 20-(System.currentTimeMillis()-time);
                try {
                    Thread.sleep(sleepTime);
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

    public double getwSpeed(){
        return wSpeed;
    }
}
