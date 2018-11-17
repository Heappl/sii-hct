package com.example.androidjogquest;

import android.*;
import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MovementTracker extends Service implements android.location.LocationListener {
    private LocationManager mLocationManager = null;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private double addLongitude;
    private double addLatitude;
    private Location startLocation = null;
    private Random rand = new Random();
    private List<Location> path = new ArrayList<>();

    private double getMeterLong(Location loc) {
        Location second = new Location("");
        second.setLongitude(loc.getLongitude() + 0.01);
        second.setLatitude(loc.getLatitude());
        return Math.abs(second.distanceTo(loc)) / 100000.0;
    }

    private double getMeterLat(Location loc) {
        Location second = new Location("");
        second.setLatitude(loc.getLatitude() + 0.01);
        second.setLongitude(loc.getLongitude());
        return Math.abs(second.distanceTo(loc)) / 100000.0;
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
        }
    }

    @Override
    public void onCreate() {
        Notification notification = new Notification();
        startForeground(10, notification);
        requestLocationUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MovementTracker", "onStartCommand");

        if (path.size() > 0) {
            Intent locationIntent = new Intent("MovementTracker::Location");
            locationIntent.putExtra("path", serializePath());
            LocalBroadcastManager.getInstance(this).sendBroadcast(locationIntent);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("MovementTracker", "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("MovementTracker", "onBind");
        return null;
    }

    void requestLocationUpdates() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }
        Log.i("MovementTracker", "requestLocationUpdates");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 0, this);
    }

    private String serializePath() {
        String ret = "";
        for (Location loc : path) {
            if (ret.length() > 0) ret += ";";
            ret += String.valueOf(loc.getLongitude()) + "," + String.valueOf(loc.getLatitude());
        }
        return ret;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("MovementTracker", "onLocationChanged");

        path.add(location);

        Intent locationIntent = new Intent("MovementTracker::Location");
        locationIntent.putExtra("path", serializePath());
        LocalBroadcastManager.getInstance(this).sendBroadcast(locationIntent);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("MovementTracker", "onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i("MovementTracker", "onProviderEnabled " + provider);

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i("MovementTracker", "onProviderDisabled " + provider);
    }
}
