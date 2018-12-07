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

        new Thread(new Runnable() {
            long startTime;
            Map<Long, List<String>> messages = new HashMap<>();

            void processMessage(String msg){

                msg = msg.replace("$","").replace(";","").replace(" ","").trim();
                //System.out.println("process message "+msg);
                String[] dataArray = msg.split(",");
                int length = dataArray.length;
                Long timeStamp = Long.parseLong(dataArray[length-1]);
                //List<String> messageList = messages.computeIfAbsent(timeStamp, k -> new ArrayList<>());
                List<String> messageList = messages.get(timeStamp);
                if (messageList == null) {
                    messageList = new ArrayList<>();
                    messages.put(timeStamp, messageList);
                }
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < length - 1; i++) {
                    builder.append(dataArray[i]);
                    builder.append(" ");
                }
                messageList.add(builder.toString());
                if (messageList.size()==4){
                    System.out.println("-----START MESSAGE------"+timeStamp);
                    for (String str :
                            messageList) {
                        System.out.println(str);
                    }
                    System.out.println("-----END MESSAGE------"+timeStamp);
                    messages.remove(timeStamp);
                    System.out.println("Messages size: "+messages.size() );
                }

            }

            @Override
            public void run() {
                startTime = new Date().getTime();


                //SimpleRPiSerialConnection thrusterConnection = new SimpleRPiSerialConnection(arduinoPort, 9600);
                SimpleSerialConnection thrusterConnection = new SimpleSerialConnection(arduinoPort, 9600);

                UDPServer thrusterServer = null;
                try {
                    thrusterServer = new UDPServer(4448);
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }

                thrusterConnection.setOnRecived(b -> {
                    //System.out.println("from arduino: " + new String(b));
                    String msg = new String(b);
                    if (msg.startsWith("$")) {
                        //System.out.println("from arduino: "+msg);
                        processMessage(msg);
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

                    //System.out.println("lx " + lx + " ty " + ty);
                    processMessage("lx " + lx + ", ty " + ty+","+currTime);

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

               /* double l = 0;
                double t = 0;
                double r = 0;
                double b = 0;
                if (lx<0) {
                    r = Math.abs(lx);
                } else {
                    l = lx;
                }
                if (ty<0){
                    b = Math.abs(ty);
                } else {
                    t = Math.abs(ty);
                }
                String msg = Math.abs(l)+","+Math.abs(t)+","+Math.abs(b)+","+Math.abs(r)+","+(new Date().getTime()-startTime)+";";*/

                    //System.out.println("To arduino: " + msg);
                    processMessage(msg);
                    try {
                        thrusterConnection.sendData(msg.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });
                thrusterConnection.start();
                thrusterServer.start();
                while (true) {

                }
            }
        }).start();
    }

    static double convert(double x){
        return 0.8*x*x + 0.1*x + 0.1;
    }
    static int znak(double value){
        if (value >= 0) return 1;
        else  return -1;
    }
}
