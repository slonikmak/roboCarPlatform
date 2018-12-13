package com.oceanos.roboCarPlatform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @autor slonikmak on 13.12.2018.
 */
public class PidLogManager {
    private Map<Long, Double> dataMap = new HashMap<>();
    private Long startTime;

    public PidLogManager(){
        startTime = new Date().getTime();
    }

    public void addData(Double data){
        dataMap.put((new Date().getTime()-startTime), data);
    }

    public void saveData(Path file){
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Long, Double> e :
                dataMap.entrySet()) {
            builder.append(e.getKey()).append(",").append(e.getValue()).append("\n");
        }
        try {
            Files.write(file, builder.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

