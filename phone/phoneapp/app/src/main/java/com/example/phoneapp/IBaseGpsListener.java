package com.example.phoneapp;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public interface IBaseGpsListener extends LocationListener,  GpsStatus.Listener {
    public void onLocationChanged(Location Location);
    public void onProviderDisabled(String provider);
    public void onProviderEnabled(String provider);
    public void onStatusChanged(String provider , int status , Bundle extras);
    public void onGpsStatusChanged(int event);
}
