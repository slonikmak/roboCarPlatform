package com.oceanos.roboCarPlatform;

import com.oceanos.ros.core.connections.UDPServer;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @autor slonikmak on 13.12.2018.
 */
public class PidLogManager {
    private Map<Long, Double[]> dataMap = new TreeMap<>();
    private Long startTime;
    private UDPServer server;

    public PidLogManager(UDPServer server){
        startTime = new Date().getTime();
        this.server = server;
    }

    public void addData(Double... data){
        long time = (new Date().getTime()-startTime);
        dataMap.put(time, data);
        StringBuilder builder = new StringBuilder();
        builder.append(time).append(",");
        for (int i = 0; i < data.length; i++) {
            builder.append(data[i]);
            if (i != data.length-1) builder.append(",");
        }
        server.sendData(builder.toString().getBytes());
    }

    public void saveData(Path file){
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Long, Double[]> e :
                dataMap.entrySet()) {
            builder.append(e.getKey()).append(",");
            for (int i = 0; i < e.getValue().length; i++) {
                builder.append(e.getValue()[i]).append(",");
            }
            builder.append("\n");
        }
        try {
            Files.write(file, builder.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

