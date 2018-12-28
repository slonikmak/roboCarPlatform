package com.oceanos.roboCarPlatform;

import com.oceanos.ros.core.connections.SimpleSerialConnection;

import java.io.IOException;

/**
 * @autor slonikmak on 11.12.2018.
 */
public class RpiSerialTest {
    public static void main(String[] args) {
        SimpleSerialConnection connection = new SimpleSerialConnection("/dev/ttyUSB0", 115200);
        connection.setOnRecived(b-> System.out.println(new String(b)));

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                System.out.println("exit");
                connection.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        connection.start();
    }
}
