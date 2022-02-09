package com.example.phoneapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.builder.filter.Comparison;
import com.mbientlab.metawear.builder.filter.ThresholdOutput;
import com.mbientlab.metawear.builder.function.Function1;
import com.mbientlab.metawear.data.Acceleration;
import com.mbientlab.metawear.module.Accelerometer;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.android.BtleService;

import java.util.ArrayList;

import bolts.Continuation;
import bolts.Task;


public class MainActivity extends AppCompatActivity implements IBaseGpsListener {
    private static final int MY_PERMISSIONS_REQUEST_FINE = 2;

    private Button start,stop,addContacts;
    ListView lv;
    EditText edit;
    private SQLiteDatabase sql;
    String provider;
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
        Log.d("Before Permission Check", "onCreate: ");

        //SMS and GPS Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            int PERMISSION_ALL = 1;
            String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS};
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
//            return;
        }
        Log.d("After Permission Check", "onCreate:");

        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.end);
        addContacts = (Button) findViewById(R.id.addContacts);
        lv = (ListView) findViewById(R.id.lv);
        edit = (EditText) findViewById(R.id.edit);

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
        DBHelper dpHelper = new DBHelper(this);
        sql = dpHelper.getWritableDatabase();
        Cursor cursor = getAllContacts();
        final ArrayAdapter<String> arrayAdapter;

        Toast.makeText(getApplicationContext(),"Wear Your Helmet and Start Tracking",Toast.LENGTH_SHORT).show();


        start.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d("Start Button", "Pressed");
                String count = "SELECT count(*) FROM "+ContactContract.TABLE_NAME;
                Cursor mcursor = sql.rawQuery(count, null);
                mcursor.moveToFirst();
                int icount = mcursor.getInt(0);
                if(icount>0){
                    Toast.makeText(getApplicationContext(),"Safe riding! We track you for safety",Toast.LENGTH_SHORT).show();
                    Intent intent= new Intent(getApplicationContext(), Fall.class);
                    startService(intent);
                    System.out.println(startService(intent));
                }else{
                    Toast.makeText(getApplicationContext(),"Add at least one contact then try again",Toast.LENGTH_SHORT).show();
                }
                mcursor.close();
            }
        });
        stop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d("Stop Button", "Pressed");
                Intent intent= new Intent(getApplicationContext(), Fall.class);
                stopService(intent);
                System.out.println(String.valueOf(startService(intent)));
            }
        });

        ArrayList<String> list = new ArrayList<String>();
        addContacts.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ArrayList<String> list = new ArrayList<String>();
                Log.d("Adding Contacts", addContacts.toString());
                //Insert into db
                if(edit.getText().toString().length() != 10 ){
                    Toast.makeText(getApplicationContext(),"Please enter again!",Toast.LENGTH_SHORT).show();
                }else{
                    addNewContact(edit.getText().toString());
                    Toast.makeText(getApplicationContext(),"Contact Added",Toast.LENGTH_SHORT).show();
                    Cursor cursor = getAllContacts();
                    if (cursor.moveToFirst()){
                        do{
                            String data = cursor.getString(cursor.getColumnIndexOrThrow("contact"));
                            list.add(data);
                            // do what ever you want here
                        }while(cursor.moveToNext());
                    }
                    cursor.close();
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.add);
                    arrayAdapter.addAll(list);
                    lv.setAdapter(arrayAdapter);
                }
                edit.setText("");
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.v("long clicked","pos: " + i  + " long value is :"+l);
                //Toast.makeText(getApplicationContext(),lv.getItemAtPosition(i).toString(), Toast.LENGTH_LONG).show();
                removeContact(lv.getItemAtPosition(i).toString());
                //lv.remove(i);
                Object remove = lv.getAdapter().getItem(i);
                ArrayAdapter arrayAdapter1 = (ArrayAdapter)lv.getAdapter();
                arrayAdapter1.remove(remove);
                return false;
            }
        });
        if (cursor.moveToFirst()){
            do{
                String data = cursor.getString(cursor.getColumnIndexOrThrow("contact"));
                list.add(data);
                // do what ever you want here
            }while(cursor.moveToNext());
        }
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.add);
        arrayAdapter.addAll(list);
        lv.setAdapter(arrayAdapter);
    }

    public Cursor getAllContacts(){
        return sql.query(ContactContract.TABLE_NAME,null,null,null,null,null,ContactContract.COLUMN_CONTACT);
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



    public long addNewContact(String contact){
        ContentValues cv = new ContentValues();
        cv.put(ContactContract.COLUMN_CONTACT,contact);
        return sql.insert(ContactContract.TABLE_NAME,null,cv);
    }

    public void removeContact(String contact){
        sql.delete(ContactContract.TABLE_NAME, "contact"+"=?",new String[]{contact});
        Toast.makeText(getApplicationContext(),"Number Deleted!!",Toast.LENGTH_SHORT).show();
    }
}