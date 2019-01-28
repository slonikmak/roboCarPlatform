package com.oceanos.roboCarPlatform;

import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;
import com.oceanos.ros.core.connections.SimpleSerialConnection;
import com.oceanos.ros.core.connections.UDPServer;
import com.oceanos.ros.core.devices.RionCompass;
import com.oceanos.ros.core.devices.SimpleWebCamera;
import com.oceanos.ros.core.devices.ThrusterController;
import com.oceanos.ros.core.devices.rionCompass.CompassData;
import com.oceanos.ros.messages.MessageServer;
import com.oceanos.ros.messages.compass.CompassMessages;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @autor slonikmak on 26.11.2018.
 */
public class CarPlatform {

    static final String arduinoPort = "/dev/ttyUSB1";
    static final String compassPort = "/dev/ttyUSB0";
    static final String cameraPort = "/dev/video0";

    //static SimpleSerialConnection thrusterSerialConnection;
    static ThrusterController thrusterController;
    static UDPServer compassUdpServer;

    static long startTime;
    static Car car;
    static MessageServer messageServer;

    public static void main(String[] args) {
        car = new Car();
        try {
            messageServer = new MessageServer(4447);
            startCameraServer();
            startCompassServer();
            startThrusterServer();
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    static void startCameraServer() throws SocketException, UnknownHostException {
        SimpleWebCamera webCamera = new SimpleWebCamera(cameraPort, new V4l4jDriver());
        UDPServer cameraUdpServer = new UDPServer(4446);
        new Thread(() -> {
            cameraUdpServer.start();
            webCamera.start();
            while (true) {
                byte[] data = webCamera.getData();
                //System.out.println("CAMERA DATA SIZE "+data.length);
                cameraUdpServer.sendData(data);
            }
        }).start();
    }

    static void startCompassServer() throws SocketException, UnknownHostException {
        SimpleSerialConnection connection = new SimpleSerialConnection(compassPort, 115200);
        RionCompass compass = new RionCompass(connection);
        compass.setOnRecived(data -> {
            CompassData compassData = (CompassData) data;
            System.out.println("from compass"+compassData.getHeading()+" "+compassData.getPitch()+" "+compassData.getRoll());
            car.setHeading(compassData.getHeading());
            //compassUdpServer.sendData(("compass,"+ car.getHeading()+","+car.getwSpeed()).getBytes());
            messageServer.sendMessage("compass", String.valueOf(car.getHeading()), String.valueOf(car.getwSpeed()));
        });

        messageServer.addConsumer(CompassMessages.START_CALIBRATION.getName(), (s)->{
            new Thread(()->{
                compass.startCalibration();
                try {
                    car.startCompassCalibration();
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                compass.saveCalibration();
            }).start();
        });

        messageServer.addConsumer(CompassMessages.START_HEADING_STREAM.getName(),(s)->{
            System.out.println("START HEADING STREAM!!!!!!!!!");
            compass.startStreamHeading();
        });

        compass.start();
        //new Thread(compassUdpServer::start).start();


    }

    static void startThrusterServer() {
        startTime = new Date().getTime();

        //thrusterSerialConnection = new SimpleSerialConnection(arduinoPort, 115200);
        thrusterController = new ThrusterController(arduinoPort, 115200);

        car.setConsumer(c->{
            Long currTime = new Date().getTime() - startTime;
            String msg1 = "0,0,0," + currTime + ";";
            if (c.getLeft() > 0 || c.getRight()>0) {
                msg1 = c.getLeft() + "," + c.getRight() + "," + c.getDirection() + "," + currTime + ";";
                thrusterController.sendData(c.getLeft(), c.getRight(), c.getDirection(), currTime);
            } else {
                thrusterController.sendData(0, 0, 0, currTime);
            }
            System.out.println("To arduino: " + msg1);
            //msg1 += '\n';
            //thrusterSerialConnection.sendData(msg1.getBytes());
            //thrusterController.sendData(c.getLeft(), c.getRight(), c.getDirection(), currTime);
            //compassUdpServer.sendData(("thruster,"+c.getLeft()+","+c.getRight()).getBytes());
            messageServer.sendMessage("thruster_callback", String.valueOf(c.getLeft()), String.valueOf(c.getRight()));
        });

        messageServer.addConsumer("stop", msg->{
            car.stop();
        });

        messageServer.addConsumer("goToHeading", msg ->{
            //double targetHeading = Double.parseDouble(msg);
            String[] values = msg.split(",");
            car.goToHeading(Double.parseDouble(values[0]), Float.parseFloat(values[1]), Float.parseFloat(values[2]), Float.parseFloat(values[3]));
        });

        messageServer.addConsumer("followHeading", msg->{
            String[] values = msg.split(",");
            car.followHeading(Double.parseDouble(values[0]), (int) Double.parseDouble(values[4]), Float.parseFloat(values[1]), Float.parseFloat(values[2]), Float.parseFloat(values[3]));
        });

        messageServer.addConsumer("thruster", msg->{

            long currTime = new Date().getTime() - startTime;
            String[] dataArr = msg.split(",");
           /* try {
                double lx = Double.parseDouble(dataArr[0]);
                double ty = Double.parseDouble(dataArr[1]);
            } catch (NumberFormatException e){
                System.out.println("thruster recived "+msg);
                return;
            }*/
            double lx = Double.parseDouble(dataArr[0]);
            double ty = Double.parseDouble(dataArr[1]);
            /*if (ty != 0.) {
                //lx = znak(lx)*convert(lx);
                ty = znak(ty) * convert(ty);
            }*/
            //System.out.println("lx " + lx + " ty " + ty + "," + currTime);
            //processMessage("lx " + lx + ", ty " + ty+","+currTime);
            int left = 0;
            int right = 0;
            int dir = 0;
            int speed = (int) Math.abs((ty * 100));
            if (ty > 0) {
                dir = 1;
            }
            if (lx < 0) {
                left = (int) (speed * (1 - Math.abs(lx)));
                right = Math.abs(speed);
            }
            if (lx > 0) {
                left = Math.abs(speed);
                right = (int) (speed * (1 - Math.abs(lx)));
            }
            if (lx == 0) {
                left = Math.abs(speed);
                right = Math.abs(speed);
            }

            car.setThruster(left, right, dir);
            /**/
        });

        //UDPServer thrusterServer = null;
        /*try {
            thrusterServer = new UDPServer(4448);
        } catch (SocketException e) {
            e.printStackTrace();
        }*/

        thrusterController.setOnRecived(b -> {
            String msg = new String(b).replace("\n", "");
            System.out.println("from arduino: " + msg);

        });

        thrusterController.start();

        /*thrusterServer.setOnRecived(d -> {
            String data = new String(d);
            messageServer.processMessage(data);
        });*/
/*
        UDPServer finalThrusterServer = thrusterServer;
        new Thread(() -> {
            finalThrusterServer.start();
        }).start();*/

        //new Thread(() -> thrusterSerialConnection.start()).start();
    }

    static double convert(double x) {
        return 0.8 * x * x + 0.1 * x + 0.1;
    }

    static int znak(double value) {
        if (value >= 0) return 1;
        else return -1;
    }
}
