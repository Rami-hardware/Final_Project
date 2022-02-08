package com.example.phoneapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.data.Acceleration;
import com.mbientlab.metawear.module.Accelerometer;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.android.BtleService;

import bolts.Continuation;
import bolts.Task;


public class MainActivity extends AppCompatActivity implements IBaseGpsListener , ServiceConnection {
    private BtleService.LocalBinder serviceBinder;
    private MetaWearBoard board;
    private Accelerometer accelerometer;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Heart rate");
    private static final int PERMISSION_LOCATION = 1000;
    TextView tv_location;
    Button b_loction;
    TextView getData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, BtleService.class),
                this, Context.BIND_AUTO_CREATE);

        //get location
        tv_location = findViewById(R.id.tv_Loction);
        b_loction = findViewById(R.id.b_loction);
        getData = findViewById(R.id.getData);
        b_loction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //check for location permission
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION} , PERMISSION_LOCATION);
                }else{
                    showLocation();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_LOCATION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showLocation();
            }else {
                Toast.makeText(this, "Permission not Granteded!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void showLocation(){
        LocationManager locationManager = (LocationManager)  getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
        tv_location.setText("Loading Location");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER , 0 , 0  , this);
        }else{
            Toast.makeText(this, "enable GPS", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    private String hereLocation(Location Location){
        return  "let: " + Location.getLatitude() + "\n Lon" + Location.getLongitude();
    }

    @Override
    public void onLocationChanged(Location location) {
        tv_location.setText(hereLocation(location));
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
// Typecast the binder to the service's LocalBinder class
        serviceBinder = (BtleService.LocalBinder) iBinder;
        Log.i("freefall" , "service connected");
        retrieveBoard("00:11:22:33:FF:EE");
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
    public void retrieveBoard(final String macAdd) {

        final BluetoothManager btManager=
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice=
                btManager.getAdapter().getRemoteDevice(macAdd);

        // Create a MetaWear board object for the Bluetooth Device
        board= serviceBinder.getMetaWearBoard(remoteDevice);
        board.connectAsync().onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
               if(task.isFaulted()){
                   Log.i("Free Fall" , "Fail" + task.getError());
               }else{
                   Log.i("Free Fall" , "connected to " + macAdd);
               }
                Accelerometer accelerometer= board.getModule(Accelerometer.class);
                accelerometer.configure()
                        .odr(25f)       // Set sampling frequency to 25Hz, or closest valid ODR
                        .range(4f)      // Set data range to +/-4g, or closet valid range
                        .commit();
               return accelerometer.acceleration().addRouteAsync(new RouteBuilder() {
                   @Override
                   public void configure(RouteComponent routeComponent) {
                       routeComponent.stream(new Subscriber() {
                           @Override
                           public void apply(Data data, Object... env) {
                               System.out.println("+++++++++++++++++++++++++++++");
                               Log.i("MainActivity", data.value(Acceleration.class).toString());
                               System.out.println("+++++++++++++++++++++++++++++");
                           }
                       });
                   }
               }).continueWith(new Continuation<Route, Void>() {
                   @Override
                   public Void then(Task<Route> task) throws Exception {
                       accelerometer.acceleration().start();
                       accelerometer.start();
                       return null;
                   }
               });
            }
        });


    }
}