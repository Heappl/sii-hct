package com.example.androidjogquest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends RightHelper implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location mLastLocation = null;

    private List<Location> path = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        requestLocationPermission(new PermissionHandler() {
            @Override
            public void onPermissionGranted() {
                startMovementTracker();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Log.i("Main", "onReceive");
                        path = deserializePath(intent.getStringExtra("path"));
                        markPosition();
                    }
                }, new IntentFilter("MovementTracker::Location"));
    }

    private List<Location> deserializePath(String s) {
        Log.i("Main", "deserializePath: " + s);
        List<Location> ret = new ArrayList<>();
        for (String locRepr : s.split(";")) {
            String[] currLocVals = locRepr.split(",");
            Location currLoc = new Location("");
            currLoc.setLongitude(Double.valueOf(currLocVals[0]));
            currLoc.setLatitude(Double.valueOf(currLocVals[1]));
            ret.add(currLoc);
        }
        return ret;
    }

    private void startMovementTracker() {
        Log.i("Main", "startMovementTracker");
        startService(new Intent(this, MovementTracker.class));
    }

    void markPosition() {
        Log.i("Main", "markPosition");
        if ((mMap != null) && (path.size() > 0)) {
            mMap.clear();
            PolylineOptions polyline = new PolylineOptions();
            Double maxLat = -Double.MAX_VALUE;
            Double minLat = Double.MAX_VALUE;
            Double maxLong = -Double.MAX_VALUE;
            Double minLong = Double.MAX_VALUE;
            Log.i("Main", "markPosition : " + path.size());
            for (Location loc : path) {
                maxLat = Math.max(maxLat, loc.getLatitude());
                minLat = Math.min(minLat, loc.getLatitude());
                maxLong = Math.max(maxLong, loc.getLongitude());
                minLong = Math.min(minLong, loc.getLongitude());
                polyline.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
            }
            LatLngBounds bounds = new LatLngBounds(new LatLng(minLat, minLong), new LatLng(maxLat, maxLong));
            mMap.addPolyline(polyline);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("Main", "onMapReady");
        mMap = googleMap;
        markPosition();
    }
}
