package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.myapplication.databinding.ActivityMainBinding;

public class MainActivity extends Activity implements SensorEventListener {

    private TextView mTextView;
    private ActivityMainBinding binding;
    private SensorManager sensorManager;
    private Sensor heartRateSensor;
    private boolean isHeartRateSensorAvaliable;
    private SensorEvent sensorEvent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mTextView = findViewById(R.id.text2);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)!=null){
            heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            isHeartRateSensorAvaliable = true;
            onSensorChanged(sensorEvent);
            Log.i("found" , isHeartRateSensorAvaliable + "?");
        }else {mTextView.setText("Is not avaliable");
            isHeartRateSensorAvaliable = false;
            Log.i("found" , isHeartRateSensorAvaliable + "?");
        }
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        mTextView.setText(sensorEvent.values[0]+ "%");
        Log.i("change" , "bepe");
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (isHeartRateSensorAvaliable) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
        @Override
        protected void onPause() {
            super.onPause();
            if (isHeartRateSensorAvaliable) {
                sensorManager.unregisterListener(this);
            }
        }
}

