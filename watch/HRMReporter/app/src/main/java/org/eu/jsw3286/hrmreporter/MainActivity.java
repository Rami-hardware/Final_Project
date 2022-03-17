package org.eu.jsw3286.hrmreporter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.Heartmonitor.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class MainActivity extends Activity implements SensorEventListener {

    DatabaseReference databaseReference;

    private TextView mHR;
    private TextView tAccu;
    private TextView txtConn;
    private ActivityMainBinding binding;
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private SharedPreferences sharedPref;
    private String currentAddr = "";
    private boolean connected = true;

    private boolean state = false;

    private ConnectivityManager connectivityManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BODY_SENSORS}, 100);
        }

        mHR = binding.HR;
        tAccu = binding.tAccu;
        txtConn = binding.txtConnected;

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        mHR.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick() {
                Log.d("BPMLabel", "Dblclicked");
            }
        });

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 0.3F;
        getWindow().setAttributes(lp);

        bindProcessToWifi();
        startMeasure();
        final int Id =  5959;
        txtConn.setText(String.valueOf(Id));
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child("faisal@faisal1com");
        databaseReference.child("watchId").setValue(Id);
    }


    private void startMeasure() {
        if (state) return;
        boolean sensorRegistered = mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_UI);
        Log.d("Sensor Status", "Sensor registered: " + (sensorRegistered ? "y" : "n") + state);
        state = sensorRegistered;

    }


    private void stopMeasure() {
        if (!state) return;
        mSensorManager.unregisterListener(this);
        Log.d("Sensor Status", "Sensor unregistered");
        mHR.setText("---");
        state = false;

        connectivityManager.releaseNetworkRequest(null);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float mHeartRateFloat = sensorEvent.values[0];
        int mHeartRate = Math.round(mHeartRateFloat);
        mHR.setText(Integer.toString((mHeartRate)));
        Log.d("HeartRate", "HeartRate test" + mHeartRate);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child("faisal@faisal1com");
        databaseReference.child("HeartRate").setValue(mHeartRate);
        tAccu.setText(Integer.toString(sensorEvent.accuracy));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        tAccu.setText(Integer.toString(i));
    }

    @Override
    protected void onPause() {
        this.unbindProcessFromWifi();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.bindProcessToWifi();
        this.startMeasure();
    }

    private void bindProcessToWifi() {
        NetworkCallback networkCallback = new NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                // The Wi-Fi network has been acquired, bind it to use this network by default
                connectivityManager.bindProcessToNetwork(network);

            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                // The Wi-Fi network has been disconnected
            }
        };
        connectivityManager.requestNetwork(
                new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build(),
                networkCallback
        );
    }

    private void unbindProcessFromWifi() {
        connectivityManager.bindProcessToNetwork(null);
    }
}