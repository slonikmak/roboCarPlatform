package com.oceanos.roboCarPlatform;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class SerialTest2 {
    public static void main(String[] args) {
        SerialPort comPort = SerialPort.getCommPort("COM7");
        comPort.openPort();

        comPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
            @Override
            public void serialEvent(SerialPortEvent event)
            {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    return;
                byte[] newData = new byte[comPort.bytesAvailable()];
                int numRead = comPort.readBytes(newData, newData.length);
                System.out.println("Read " + new String(newData));
            }
        });

        new Thread(()->{
            for (int i = 0; i < 20; i++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                byte[] to = ("0,0,0,"+i+";"+"\n").getBytes();
                comPort.writeBytes(to, to.length);
            }

            //comPort.closePort();
        }).start();



        //comPort.closePort();
    }
}
