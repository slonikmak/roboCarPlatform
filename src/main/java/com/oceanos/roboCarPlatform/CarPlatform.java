package com.oceanos.roboCarPlatform;

import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;
import com.oceanos.ros.core.connections.SimpleRPiSerialConnection;
import com.oceanos.ros.core.connections.SimpleSerialConnection;
import com.oceanos.ros.core.connections.UDPServer;
import com.oceanos.ros.core.devices.RionCompass;
import com.oceanos.ros.core.devices.SimpleWebCamera;
import com.oceanos.ros.core.devices.rionCompass.CompassData;
import org.apache.http.MethodNotSupportedException;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @autor slonikmak on 26.11.2018.
 */
public class CarPlatform {

    static final String arduinoPort = "/dev/ttyUSB0";
    static final String compassPort = "/dev/ttyUSB1";
    static final String cameraPort = "/dev/video0";

    static Map<Long, List<String>> messages = new HashMap<>();

    static void addToList(String msg, long time){
        List<String> messageList = messages.get(time);
        if (messageList == null) {
            messageList = new ArrayList<>();
            messages.put(time, messageList);
        }
        messageList.add(msg);
        if (messageList.size()==3){
            System.out.println("-----START MESSAGE------"+time);
            for (String str :
                    messageList) {
                System.out.println(str);
            }
            System.out.println("-----END MESSAGE------"+time);
            messages.remove(time);
            //System.out.println("Messages size: "+messages.size() );
        }
    }

    static void processMessage(String msg){

        msg = msg.replace("$","").trim();
        //System.out.println("process message "+msg);
        String[] msgs = msg.split(";");
        int length = msgs.length;
        Long timeStamp = Long.parseLong(msgs[length-1]);
        for (int i = 0; i < length - 1; i++) {
            addToList(msgs[i], timeStamp);
        }


    }


    public static void main(String[] args) {
        long startTime = new Date().getTime();

        try {
            startCameraServer();
            //startCompassServer();
            startThrusterServer();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    static void startCameraServer() throws SocketException, UnknownHostException {
        SimpleWebCamera webCamera = new SimpleWebCamera(cameraPort);
        webCamera.setWebcamDriver(new V4l4jDriver());
        UDPServer cameraUdpServer = new UDPServer(4446);

        new Thread(()->{
            cameraUdpServer.start();
            webCamera.start();

            while (true){
                /*String base64 = webCamera.getBase64String();

                Map<String, Object> message = new HashMap<String, Object>();
                message.put("type", "image");
                message.put("webcam", "default");
                message.put("image", base64);
*/
                cameraUdpServer.sendData(webCamera.getData());

                   /* try {
                            simpleWebSocketServer.sendString(MAPPER.writeValueAsString(message));
                    } catch (JsonProcessingException e) {
                            //LOG.error(e.getMessage(), e);
                            e.printStackTrace();
                    }*/
            }
        }).start();

    }

    static void startCompassServer() throws SocketException, UnknownHostException {
        UDPServer compassUdpServer = new UDPServer(4447);

        new Thread(()->{
            SimpleRPiSerialConnection connection = new SimpleRPiSerialConnection(compassPort, 115200);
            RionCompass compass = new RionCompass(connection);
            compassUdpServer.start();
            try {
                compass.setOnRecive(data -> {
                    CompassData compassData = (CompassData)data;
                    //System.out.println(compassData.getHeading()+" "+compassData.getPitch()+" "+compassData.getRoll());
                    compassUdpServer.sendData(String.valueOf(compassData.getHeading()).getBytes());
                });
            } catch (MethodNotSupportedException e) {
                e.printStackTrace();
            }
            compass.start();
            while (true){}

        }).start();
    }

    static void startThrusterServer() throws SocketException, UnknownHostException {
        long startTime = new Date().getTime();

        SimpleSerialConnection thrusterConnection = new SimpleSerialConnection(arduinoPort, 115200);

        UDPServer thrusterServer = null;
        try {
            thrusterServer = new UDPServer(4448);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        thrusterConnection.setOnRecived(b -> {
            //System.out.println("from arduino: " + new String(b).replace("\n",""));
            String msg = new String(b).replace("\n","");
            if (msg.startsWith("$")) {
                System.out.println("from arduino: "+msg);
                //processMessage(msg);
            } else {
                System.out.println("from arduino: "+msg);
            }
        });

        thrusterServer.setOnRecived(d -> {

            long currTime = new Date().getTime() - startTime;

            String data = new String(d);
            String[] dataArr = data.split(",");

            double lx = Double.parseDouble(dataArr[0]);
            double ty = Double.parseDouble(dataArr[1]);

            if (ty != 0.){
                //lx = znak(lx)*convert(lx);
                ty = znak(ty) * convert(ty);
            }

            System.out.println("lx " + lx + " ty " + ty+","+currTime);
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

            String msg = "0,0,0," + (new Date().getTime() - startTime) + ";";
            if (speed > 0) {
                msg = left + "," + right + "," + dir + "," + currTime + ";";
            }


            System.out.println("To arduino: " + msg);
            //addToList(msg, currTime);
            try {
                msg += '\n';
                thrusterConnection.sendData(msg.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        UDPServer finalThrusterServer = thrusterServer;
        new Thread(()->{
            finalThrusterServer.start();
        }).start();

        new Thread(() -> thrusterConnection.start()).start();
    }

    static double convert(double x){
        return 0.8*x*x + 0.1*x + 0.1;
    }
    static int znak(double value){
        if (value >= 0) return 1;
        else  return -1;
    }
}
