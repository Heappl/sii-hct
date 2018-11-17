package com.example.androidjogquest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.animation.Animation;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends RightHelper implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private Location mLastLocation = null;
    private List<SpaceJunk> junks = new ArrayList<SpaceJunk>();
    final double viewDistanceLat = 0.014;
    final double viewDistanceLong = 0.02;

    private List<Location> path = new ArrayList<>();

    MapsActivity() {
        junks.add(new SpaceJunk("SkyTower",
                new LatLng(51.089666308, 17.017166598),
                0.003, 0.005, "galaxy_0"));
        junks.add(new SpaceJunk("junkyard",
                new LatLng(51.059666308, 17.007166598),
                0.0005, 0.0007, "junk_ship_blue"));
        junks.add(new SpaceJunk("junkyard",
                new LatLng(51.059666308, 17.027166598),
                0.0005, 0.0007, "junk_ship_red"));
        junks.add(new SpaceJunk("base",
                new LatLng(51.099366308, 17.017466598),
                0.0005, 0.0007, "satellite"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
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
                        drawThings();
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

    Location getLast() {
        return path.get(path.size() - 1);
    }
    LatLng getLastLatLng() {
        return new LatLng(getLast().getLatitude(), getLast().getLongitude());
    }

    LatLngBounds getLastBounds() {
        LatLng min = new LatLng(getLast().getLatitude() - 0.0003, getLast().getLongitude() - 0.0005);
        LatLng max = new LatLng(getLast().getLatitude() + 0.0003, getLast().getLongitude() + 0.0005);
        return new LatLngBounds(min, max);
    }

    void setCamera() {
        Location last = getLast();
        LatLng from = new LatLng(last.getLatitude() - viewDistanceLong, last.getLongitude() - viewDistanceLong);
        LatLng to = new LatLng(last.getLatitude() + viewDistanceLat, last.getLongitude() + viewDistanceLat);
        LatLngBounds bounds = new LatLngBounds(from, to);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
    }

    void drawPath() {
        PolylineOptions polyline = new PolylineOptions().color(R.color.colorPath);
        for (Location loc : path) {
            polyline.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
        }
        mMap.addPolyline(polyline);
        mMap.addMarker(new MarkerOptions()
                .position(getLastLatLng())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ship_small)));
    }

    void drawNearbyObjects() {
        for (SpaceJunk junk : junks) {
            if (junk.getDistance(getLast(), viewDistanceLat, viewDistanceLong)) {
                String imageName = junk.getImageName();
                Resources resources = getResources();
                final int resourceId = resources.getIdentifier(imageName, "drawable", getPackageName());
                mMap.addGroundOverlay(new GroundOverlayOptions()
                        .positionFromBounds(junk.getBounds())
                        .image(BitmapDescriptorFactory.fromResource(resourceId))
                        .clickable(true));
            }
        }
    }

    void drawThings() {
        Log.i("Main", "drawThings");
        if ((mMap != null) && (path.size() > 0)) {
            mMap.clear();

            drawPath();
            if (path.size() < 2)
                setCamera();
            drawNearbyObjects();
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
        mMap.setOnMarkerClickListener(this);

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }
        drawThings();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        setCamera();
        return false;
    }
}
