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

// 偵測手機方向(假設:偏移45度的時候提示換邊)

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private File csvFile;
    private FileWriter csvWriter;
    private SensorManager sensorManager;
    private final ArrayList<String> sensorDataList = new ArrayList<>();
    private static final int MAX_DATA_SIZE = 750;

    private final float[] accelerometerData = new float[3];
    private final float[] gravityData = new float[3];
    private final float[] gyroscopeData = new float[3];
    private final float[] linearAccelerationData = new float[3];
    private final float[] rotationVectorData = new float[5];

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), "Accelerometer");
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), "Gravity");
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), "Gyroscope");
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), "Linear Acceleration");
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), "Rotation Vector");

        csvFile = new File(getExternalFilesDir(null), "sensor_data.csv");

        try
        {
            csvWriter = new FileWriter(csvFile);
            csvWriter.append("Timestamp,Accelerometer_X,Accelerometer_Y,Accelerometer_Z,Gravity_X,Gravity_Y,Gravity_Z,Gyroscope_X,Gyroscope_Y,Gyroscope_Z,LinearAccel_X,LinearAccel_Y,LinearAccel_Z,Rotation_X,Rotation_Y,Rotation_Z,Rotation_cos,HeadingAccuracy\n");
            csvWriter.flush();
            Log.e("MainActivity", "CSV header written");
        } catch (IOException e) {
            Log.e("MainActivity", "Error creating CSV file", e);
        }
    }

    private void registerSensor(Sensor sensor, String sensorName)
    {
        if (sensor != null)
        {
            // 0.02秒蒐集一次資料
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
            Log.i("MainActivity", sensorName + " sensor registered.");
        }
        else
        {
            Log.e("MainActivity", sensorName + " sensor is not available on this device.");
            Toast.makeText(this, sensorName + " sensor is not available on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        int sensorType = event.sensor.getType();
        float[] values = event.values;
        long currentTime = System.currentTimeMillis();

        // 處理其他感測器數據
        switch (sensorType)
        {
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

        try
        {
            csvWriter.append(data).append("\n");
            csvWriter.flush();
            Log.d("MainActivity", "Data written to CSV: " + data);
        }
        catch (IOException e)
        {
            Log.e("MainActivity", "Error writing to CSV file", e);
        }

        sensorDataList.add(data);
        if (sensorDataList.size() > MAX_DATA_SIZE)
        {
            Log.e("Size", "remove");
            sensorDataList.remove(1);  // 保留表頭，移除第一筆數據
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.e("MainActivity", "onPause called");
        closeCsvWriter();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.e("MainActivity", "onResume called");
        // 重新註冊感測器監聽器
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), "Accelerometer");
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), "Gravity");
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), "Gyroscope");
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), "Linear Acceleration");
        registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), "Rotation Vector");

        // 重新打開 CSV 文件
        try
        {
            csvWriter = new FileWriter(csvFile, true);
            Log.e("MainActivity", "CSV file opened in append mode");
        }
        catch (IOException e)
        {
            Log.e("MainActivity", "Error opening CSV file", e);
        }
    }

    private void closeCsvWriter()
    {
        if (csvWriter != null)
        {
            try
            {
                csvWriter.flush();
                csvWriter.close();
                Log.e("MainActivity", "CSV file closed");
            }
            catch (IOException e)
            {
                Log.e("MainActivity", "Error closing CSV file", e);
            }
        }
    }
}
