package com.oceanos.roboCarPlatform;

import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;
import com.oceanos.ros.core.connections.SimpleRPiSerialConnection;
import com.oceanos.ros.core.connections.SimpleSerialConnection;
import com.oceanos.ros.core.connections.UDPServer;
import com.oceanos.ros.core.devices.RionCompass;
import com.oceanos.ros.core.devices.SimpleWebCamera;
import com.oceanos.ros.core.devices.rionCompass.CompassData;
import com.oceanos.ros.core.devices.rionCompass.Rion3DCompassCommand;
import com.oceanos.ros.messages.MessageProcessor;
import com.oceanos.ros.messages.compass.CompassMessages;
import org.apache.http.MethodNotSupportedException;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @autor slonikmak on 26.11.2018.
 */
public class CarPlatform {

    static final String arduinoPort = "/dev/ttyUSB0";
    static final String compassPort = "/dev/ttyUSB1";
    static final String cameraPort = "/dev/video0";

    static SimpleSerialConnection thrusterSerialConnection;
    static UDPServer compassUdpServer;

    static long startTime;
    static Car car;
    static MessageProcessor messageProcessor;

    static Map<Long, List<String>> messages = new HashMap<>();

    static void addToList(String msg, long time) {
        List<String> messageList = messages.get(time);
        if (messageList == null) {
            messageList = new ArrayList<>();
            messages.put(time, messageList);
        }
        messageList.add(msg);
        if (messageList.size() == 3) {
            System.out.println("-----START MESSAGE------" + time);
            for (String str :
                    messageList) {
                System.out.println(str);
            }
            System.out.println("-----END MESSAGE------" + time);
            messages.remove(time);
            //System.out.println("Messages size: "+messages.size() );
        }
    }

    static void processMessage(String msg) {

        msg = msg.replace("$", "").trim();
        //System.out.println("process message "+msg);
        String[] msgs = msg.split(";");
        int length = msgs.length;
        Long timeStamp = Long.parseLong(msgs[length - 1]);
        for (int i = 0; i < length - 1; i++) {
            addToList(msgs[i], timeStamp);
        }
    }

    public static void main(String[] args) {
        car = new Car();
        messageProcessor = new MessageProcessor();
        try {
            startCameraServer();
            startCompassServer();
            startThrusterServer();
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    static void startCameraServer() throws SocketException, UnknownHostException {
        SimpleWebCamera webCamera = new SimpleWebCamera(cameraPort);
        webCamera.setWebcamDriver(new V4l4jDriver());
        UDPServer cameraUdpServer = new UDPServer(4446);
        new Thread(() -> {
            cameraUdpServer.start();
            webCamera.start();
            while (true) {
                cameraUdpServer.sendData(webCamera.getData());
            }
        }).start();
    }

    static void startCompassServer() throws SocketException, UnknownHostException {
        SimpleSerialConnection connection = new SimpleSerialConnection(compassPort, 115200);
        compassUdpServer = new UDPServer(4447);
        RionCompass compass = new RionCompass(connection);
        try {
            compass.setOnRecive(data -> {
                CompassData compassData = (CompassData) data;
                //System.out.println("from compass"+compassData.getHeading()+" "+compassData.getPitch()+" "+compassData.getRoll());
                compassUdpServer.sendData(("compass,"+ compassData.getHeading()).getBytes());
                car.setHeading(compassData.getHeading());
            });
        } catch (MethodNotSupportedException e) {
            e.printStackTrace();
        }

        messageProcessor.addConsumer(CompassMessages.START_CALIBRATION.getName(), (s)->{
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

        messageProcessor.addConsumer(CompassMessages.START_HEADING_STREAM.getName(),(s)->{
            compass.startStreamHeading();
        });

        compass.start();
        new Thread(compassUdpServer::start).start();


    }

    static void startThrusterServer() throws SocketException, UnknownHostException {
        startTime = new Date().getTime();

        thrusterSerialConnection = new SimpleSerialConnection(arduinoPort, 115200);

        car.setConsumer(c->{
            Long currTime = new Date().getTime() - startTime;
            String msg1 = "0,0,0," + currTime + ";";
            if (c.getLeft() > 0 || c.getRight()>0) {
                msg1 = c.getLeft() + "," + c.getRight() + "," + c.getDirection() + "," + currTime + ";";
            }
            System.out.println("To arduino: " + msg1);
            try {
                msg1 += '\n';
                thrusterSerialConnection.sendData(msg1.getBytes());
                compassUdpServer.sendData(("thruster,"+c.getLeft()+","+c.getRight()).getBytes());

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        messageProcessor.addConsumer("stop", msg->{
            car.stop();
        });

        messageProcessor.addConsumer("goToHeading", msg ->{
            //double targetHeading = Double.parseDouble(msg);
            String[] values = msg.split(",");
            car.goToHeading(Double.parseDouble(values[0]), Float.parseFloat(values[1]), Float.parseFloat(values[2]), Float.parseFloat(values[3]));
        });

        messageProcessor.addConsumer("followHeading", msg->{
            String[] values = msg.split(",");
            car.followHeading(Double.parseDouble(values[0]), (int) Double.parseDouble(values[4]), Float.parseFloat(values[1]), Float.parseFloat(values[2]), Float.parseFloat(values[3]));
        });

        messageProcessor.addConsumer("thruster", msg->{
            long currTime = new Date().getTime() - startTime;
            String[] dataArr = msg.split(",");
            double lx = Double.parseDouble(dataArr[0]);
            double ty = Double.parseDouble(dataArr[1]);
            if (ty != 0.) {
                //lx = znak(lx)*convert(lx);
                ty = znak(ty) * convert(ty);
            }
            System.out.println("lx " + lx + " ty " + ty + "," + currTime);
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

        UDPServer thrusterServer = null;
        try {
            thrusterServer = new UDPServer(4448);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        thrusterSerialConnection.setOnRecived(b -> {
            String msg = new String(b).replace("\n", "");
            System.out.println("from arduino: " + msg);

        });

        thrusterServer.setOnRecived(d -> {
            String data = new String(d);
            messageProcessor.processMessage(data);
        });

        UDPServer finalThrusterServer = thrusterServer;
        new Thread(() -> {
            finalThrusterServer.start();
        }).start();

        new Thread(() -> thrusterSerialConnection.start()).start();
    }

    static double convert(double x) {
        return 0.8 * x * x + 0.1 * x + 0.1;
    }

    static int znak(double value) {
        if (value >= 0) return 1;
        else return -1;
    }
}
