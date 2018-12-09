package com.oceanos.roboCarPlatform;

import com.oceanos.ros.core.connections.SimpleSerialConnection;

import java.io.IOException;

public class SerialTest {

    public static void main(String[] args) {
        SimpleSerialConnection connection = new SimpleSerialConnection("COM7", 115200);

        connection.setOnRecived(b-> System.out.print(new String(b)));

        new Thread(()->{
            connection.start();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(10);
                    connection.sendData(("0,0,0,"+i+";\n").getBytes());
                    System.out.println("send "+i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(1000);
                connection.sendData(("0,0,0,100"+";\n").getBytes());
                System.out.println("send 100");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true){}
        }).start();




    }
}
