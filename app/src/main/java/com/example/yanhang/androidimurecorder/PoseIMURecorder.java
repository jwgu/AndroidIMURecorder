package com.example.yanhang.androidimurecorder;

/**
 * Created by yanhang on 1/9/17.
 */


import android.content.Intent;
import android.hardware.SensorEvent;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.RunnableFuture;

import static android.content.ContentValues.TAG;

public class PoseIMURecorder {
    private static final float mulNanoToSec = 1000000000;

    private final static String LOG_TAG = PoseIMURecorder.class.getName();

    MainActivity parent_;

    public static final int SENSOR_COUNT = 6;
    public static final int GYROSCOPE = 0;
    public static final int ACCELEROMETER = 1;
    public static final int MAGNETOMETER = 2;
    public static final int LINEAR_ACCELERATION = 3;
    public static final int GRAVITY = 4;
    public static final int ROTATION_VECTOR = 5;

    private FileWriter[] file_writers_ = new FileWriter[SENSOR_COUNT];
    private Vector<Vector<String>> data_buffers_ = new Vector<Vector<String> >();
    private String[] default_file_names_ = {"gyro.txt", "acce.txt", "magnet.txt", "linacce.txt", "gravity.txt", "orientation.txt"};

    public PoseIMURecorder(String path, MainActivity parent){
        parent_ = parent;
        Calendar file_timestamp = Calendar.getInstance();
        String header = "# Created at " + file_timestamp.getTime().toString() + "\n";
        try {
            for(int i=0; i<SENSOR_COUNT; ++i) {
                file_writers_[i] = createFile(path + "/" + default_file_names_[i], header);
                data_buffers_.add(new Vector<String>());
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void writeBufferToFile(FileWriter writer, Vector<String> buffer) throws IOException{
        for (String line : buffer) {
            writer.write(line);
        }
        writer.close();
    }

    public void endFiles(){
        try {
            for(int i=0; i<SENSOR_COUNT; ++i){
                writeBufferToFile(file_writers_[i], data_buffers_.get(i));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public Boolean addRecord(SensorEvent event, int type){
        if(type < 0 && type >= SENSOR_COUNT){
            return false;
        }
        float[] values = event.values;
        long timestamp = event.timestamp;
        if(type != ROTATION_VECTOR){
            data_buffers_.get(type).add(String.format(Locale.US,"%d %.6f %.6f %.6f\n", timestamp, values[0], values[1], values[2]));
        }else{
            data_buffers_.get(type).add(String.format(Locale.US,"%d %.6f %.6f %.6f %.6f\n", timestamp, values[3], values[0], values[1], values[2]));
        }
        return true;
    }

    private FileWriter createFile(String path, String header) throws IOException{
        File file = new File(path);
        FileWriter writer = new FileWriter(file);
        Intent scan_intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scan_intent.setData(Uri.fromFile(file));
        parent_.sendBroadcast(scan_intent);
        if(header != null && header.length() != 0) {
            writer.append(header);
        }
        return writer;
    }
}