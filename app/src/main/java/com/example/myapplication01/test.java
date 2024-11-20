package com.example.myapplication01;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class test extends AppCompatActivity implements SensorEventListener {

    private File csvFile;
    private FileWriter csvWriter;
    private SensorManager sensorManager;
    private final ArrayList<String> sensorDataList = new ArrayList<>();
    private static final int MAX_DATA_SIZE = 10;

    private final float[] accelerometerData = new float[3];
    private final float[] gravityData = new float[3];
    private final float[] gyroscopeData = new float[3];
    private final float[] linearAccelerationData = new float[3];
    private final float[] rotationVectorData = new float[5];

//    private boolean isFlat = false;  // 判斷手機是否平放的標誌
//    private long lastTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), "Accelerometer");
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), "Gravity");
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), "Gyroscope");
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), "Linear Acceleration");
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), "Rotation Vector");

        csvFile = new File(getExternalFilesDir(null), "sensor_data.csv");

        try {
            csvWriter = new FileWriter(csvFile);
            csvWriter.append("Timestamp,Accelerometer_X,Accelerometer_Y,Accelerometer_Z,Gravity_X,Gravity_Y,Gravity_Z,Gyroscope_X,Gyroscope_Y,Gyroscope_Z,LinearAccel_X,LinearAccel_Y,LinearAccel_Z,Rotation_X,Rotation_Y,Rotation_Z,Rotation_cos,HeadingAccuracy\n");
            csvWriter.flush();
            Log.e("MainActivity", "CSV header written");
        } catch (IOException e) {
            Log.e("MainActivity", "Error creating CSV file", e);
        }
    }

    private void registerSensor(Sensor sensor, String sensorName) {
        if (sensor != null) {
//            0.02秒蒐集一次資料
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
            Log.i("MainActivity", sensorName + " sensor registered.");
        } else {
            Log.e("MainActivity", sensorName + " sensor is not available on this device.");
            Toast.makeText(this, sensorName + " sensor is not available on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        float[] values = event.values;

        // 使用重力感測器來判斷手機是否平放
        /*
        if (sensorType == Sensor.TYPE_GRAVITY) {
            isFlat = Math.abs(values[0]) < 1.5 && Math.abs(values[1]) < 1.5 && Math.abs(values[2] - 9.8) < 1.5;
            if (isFlat) {
                Log.e("MainActivity", "Device is flat; data collection paused.");
                return; // 手機平放時停止數據收集
            }
            gravityData[0] = values[0];
            gravityData[1] = values[1];
            gravityData[2] = values[2];
        }
        */

        // 每0.1秒記錄一筆數據
        long currentTime = System.currentTimeMillis();
//        if (currentTime - lastTimestamp < 100) {
//            return;
//        }
//        lastTimestamp = currentTime;

        // 處理其他感測器數據
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerData[0] = values[0];
                accelerometerData[1] = values[1];
                accelerometerData[2] = values[2];
                break;
            case Sensor.TYPE_GRAVITY:
                gravityData[0] = values[0];
                gravityData[1] = values[1];
                gravityData[2] = values[2];
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroscopeData[0] = values[0];
                gyroscopeData[1] = values[1];
                gyroscopeData[2] = values[2];
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                linearAccelerationData[0] = values[0];
                linearAccelerationData[1] = values[1];
                linearAccelerationData[2] = values[2];
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                rotationVectorData[0] = values[0];
                rotationVectorData[1] = values[1];
                rotationVectorData[2] = values[2];
                rotationVectorData[3] = values.length > 3 ? values[3] : Float.NaN;
                rotationVectorData[4] = values.length > 4 ? values[4] : Float.NaN;
                break;
        }

        String data = currentTime + "," +
                accelerometerData[0] + "," + accelerometerData[1] + "," + accelerometerData[2] + "," +
                gravityData[0] + "," + gravityData[1] + "," + gravityData[2] + "," +
                gyroscopeData[0] + "," + gyroscopeData[1] + "," + gyroscopeData[2] + "," +
                linearAccelerationData[0] + "," + linearAccelerationData[1] + "," + linearAccelerationData[2] + "," +
                rotationVectorData[0] + "," + rotationVectorData[1] + "," + rotationVectorData[2] + "," +
                (!Float.isNaN(rotationVectorData[3]) ? rotationVectorData[3] : "N/A") + "," +
                (!Float.isNaN(rotationVectorData[4]) ? rotationVectorData[4] : "N/A");

        try {
            csvWriter.append(data).append("\n");
            csvWriter.flush();
            Log.d("MainActivity", "Data written to CSV: " + data);
        } catch (IOException e) {
            Log.e("MainActivity", "Error writing to CSV file", e);
        }

        sensorDataList.add(data);
        if (sensorDataList.size() > MAX_DATA_SIZE) {
            Log.e("Size", "remove");
            sensorDataList.remove(1);  // 保留表頭，移除第一筆數據
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("MainActivity", "onPause called");
        closeCsvWriter();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("MainActivity", "onResume called");
        // 重新註冊感測器監聽器
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), "Accelerometer");
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), "Gravity");
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), "Gyroscope");
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), "Linear Acceleration");
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), "Rotation Vector");

        // 重新打開 CSV 文件
        try {
            csvWriter = new FileWriter(csvFile, true);
            Log.e("MainActivity", "CSV file opened in append mode");
        } catch (IOException e) {
            Log.e("MainActivity", "Error opening CSV file", e);
        }
    }

    private void closeCsvWriter() {
        if (csvWriter != null) {
            try {
                csvWriter.flush();
                csvWriter.close();
                Log.e("MainActivity", "CSV file closed");
            } catch (IOException e) {
                Log.e("MainActivity", "Error closing CSV file", e);
            }
        }
    }
}






//package com.example.myapplication01;
//
//import android.content.Context;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//
//public class MainActivity extends AppCompatActivity implements SensorEventListener {
//
//    private FileWriter csvWriter;
//    private SensorManager sensorManager;
//    private TextView accelerometerTextView, gravityTextView, gyroscopeTextView, linearAccelerationTextView, rotationVectorTextView;
//
//    private float[] accelerometerData = new float[3];
//    private float[] gravityData = new float[3];
//    private float[] gyroscopeData = new float[3];
//    private float[] linearAccelerationData = new float[3];
//    private float[] rotationVectorData = new float[5];
//
//    private boolean isFlat = false;  // 判斷手機是否平放的標誌
//    private long lastTimestamp = 0;  // 上次記錄的時間戳
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        accelerometerTextView = findViewById(R.id.accelerometer_textview);
//        gravityTextView = findViewById(R.id.gravity_textview);
//        gyroscopeTextView = findViewById(R.id.gyroscope_textview);
//        linearAccelerationTextView = findViewById(R.id.linear_acceleration_textview);
//        rotationVectorTextView = findViewById(R.id.rotation_vector_textview);
//
//        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//
//        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), "Accelerometer");
//        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), "Gravity");
//        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), "Gyroscope");
//        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), "Linear Acceleration");
//        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), "Rotation Vector");
//
//        // CSV
//        try {
//            File csvFile = new File(getExternalFilesDir(null), "sensor_data.csv");
//            csvWriter = new FileWriter(csvFile);
//            csvWriter.append("Timestamp,Accelerometer_X,Accelerometer_Y,Accelerometer_Z,Gravity_X,Gravity_Y,Gravity_Z,Gyroscope_X,Gyroscope_Y,Gyroscope_Z,LinearAccel_X,LinearAccel_Y,LinearAccel_Z,Rotation_X,Rotation_Y,Rotation_Z,Rotation_cos,HeadingAccuracy\n");
//        } catch (IOException e) {
//            Log.e("MainActivity", "Error creating CSV file", e);
//        }
//    }
//
//    private void registerSensor(Sensor sensor, String sensorName) {
//        if (sensor != null) {
//            // 設置為每秒更新一次的頻率
//            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL); //改為每秒收集一次數據
//            Log.i("MainActivity", sensorName + " sensor registered.");
//        } else {
//            Log.e("MainActivity", sensorName + " sensor is not available on this device.");
//            Toast.makeText(this, sensorName + " sensor is not available on this device.", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        int sensorType = event.sensor.getType();
//        float[] values = event.values;
//
//        // 每秒一次的時間間隔控制
//        long currentTime = System.currentTimeMillis();
//        if (currentTime - lastTimestamp < 1000) {
//            return;
//        }
//        lastTimestamp = currentTime;
//
//        switch (sensorType) {
//            case Sensor.TYPE_GRAVITY:
//                gravityData = values.clone();
//                isFlat = Math.abs(values[0]) < 1.5 && Math.abs(values[1]) < 1.5 && Math.abs(values[2] - 9.8) < 1.5; //判斷是否平放
//                if (gravityTextView != null) {
//                    gravityTextView.setText("Gravity:\n X: " + values[0] + "\n Y: " + values[1] + "\n Z: " + values[2]);
//                }
//                break;
//            case Sensor.TYPE_ACCELEROMETER:
//                accelerometerData = values.clone();
//                if (isFlat) return; // 如果手機平放，停止數據收集
//                if (accelerometerTextView != null) {
//                    accelerometerTextView.setText("Accelerometer:\n X: " + values[0] + "\n Y: " + values[1] + "\n Z: " + values[2]);
//                }
//                break;
//            case Sensor.TYPE_GYROSCOPE:
//                gyroscopeData = values.clone();
//                if (gyroscopeTextView != null) {
//                    gyroscopeTextView.setText("Gyroscope:\n X: " + values[0] + "\n Y: " + values[1] + "\n Z: " + values[2]);
//                }
//                break;
//            case Sensor.TYPE_LINEAR_ACCELERATION:
//                linearAccelerationData = values.clone();
//                if (linearAccelerationTextView != null) {
//                    linearAccelerationTextView.setText("Linear Acceleration:\n X: " + values[0] + "\n Y: " + values[1] + "\n Z: " + values[2]);
//                }
//                break;
//            case Sensor.TYPE_ROTATION_VECTOR:
//                rotationVectorData = values.clone();
//                if (rotationVectorTextView != null) {
//                    rotationVectorTextView.setText("Rotation Vector:\n X*sin(θ/2): " + values[0] +
//                            "\n Y*sin(θ/2): " + values[1] +
//                            "\n Z*sin(θ/2): " + values[2] +
//                            "\n cos(θ/2): " + (values.length > 3 ? values[3] : "N/A") +
//                            "\n Heading Accuracy: " + (values.length > 4 ? values[4] : "N/A"));
//                }
//                break;
//        }
//
//        // 寫入數據
//        try {
//            csvWriter.append(currentTime + "," +
//                    accelerometerData[0] + "," + accelerometerData[1] + "," + accelerometerData[2] + "," +
//                    gravityData[0] + "," + gravityData[1] + "," + gravityData[2] + "," +
//                    gyroscopeData[0] + "," + gyroscopeData[1] + "," + gyroscopeData[2] + "," +
//                    linearAccelerationData[0] + "," + linearAccelerationData[1] + "," + linearAccelerationData[2] + "," +
//                    rotationVectorData[0] + "," + rotationVectorData[1] + "," + rotationVectorData[2] + "," +
//                    (rotationVectorData.length > 3 ? rotationVectorData[3] : "N/A") + "," +
//                    (rotationVectorData.length > 4 ? rotationVectorData[4] : "N/A") + "\n");
//            csvWriter.flush();
//        } catch (IOException e) {
//            Log.e("MainActivity", "Error writing to CSV file", e);
//        }
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        sensorManager.unregisterListener(this);
//        if (csvWriter != null) {
//            try {
//                csvWriter.close();
//            } catch (IOException e) {
//                Log.e("MainActivity", "Error closing CSV file", e);
//            }
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
//        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
//        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
//        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
//        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
//    }
//
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        sensorManager.unregisterListener(this);
//    }
//}
