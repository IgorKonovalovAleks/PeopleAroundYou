package com.itsamsung.stdigor.peoplearoundyou;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class BackGroundLocationListener implements LocationListener {

    LocationListenerActivity parent;
    public boolean able;

    public BackGroundLocationListener(LocationListenerActivity parent) {
        this.parent = parent;
        able = true;
        Log.d("BGLL", "started");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("BGLL", location.getLatitude() + " " + location.getLongitude());
        parent.locationChanged(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {
        parent.providerEnabled(provider);
        able = true;
    }

    @Override
    public void onProviderDisabled(String provider) {
        parent.providerDisabled(provider);
        able = false;
    }
}
