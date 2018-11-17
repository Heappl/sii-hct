package com.example.androidjogquest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapsActivity extends RightHelper
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnGroundOverlayClickListener, Runnable {

    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private List<SpaceJunk> junks = new ArrayList<>();
    private List<GroundOverlay> overlays = new ArrayList<>();
    private List<AreaControl> areas = new ArrayList<>();
    final double viewDistanceLat = 0.014;
    final double viewDistanceLong = 0.02;
    final double distance = 3000;
    Location lastLocation = null;

    private HashMap<String, SpaceJunk> idToJunk = new HashMap<>();
    Marker marker = null;
    Circle range = null;
    Circle otherRange = null;
    Marker otherMarker = null;

    private List<Location> path = new ArrayList<>();

    private CommunicationThread communication = new CommunicationThread();
    private Thread communicationThread = new Thread(communication);

    MapsActivity() {
        junks.add(new SpaceJunk("SkyTower",
                new LatLng(51.089666308, 17.017166598),
                0.003, 0.005, "galaxy_0", "skytower"));
        junks.add(new SpaceJunk("junkyard",
                new LatLng(51.099666308, 17.007166598),
                0.0005, 0.0007, "junk_ship_blue", "cat1"));
        junks.add(new SpaceJunk("junkyard",
                new LatLng(51.069666308, 17.027166598),
                0.0005, 0.0007, "junk_ship_red",  "cat2"));
        junks.add(new SpaceJunk("junkyard",
                new LatLng(51.088666308, 17.018166598),
                0.0005, 0.0007, "junk_ship_red",  "cat3"));
        junks.add(new SpaceJunk("base",
                new LatLng(51.089666308, 17.019466598),
                0.0005, 0.0007, "satellite", "cat3"));
        junks.add(new SpaceJunk("commsat",
                new LatLng(51.109666308, 17.016466598),
                0.0005, 0.0007, "satellite", "cat1"));
        junks.add(new SpaceJunk("kronos",
                new LatLng(51.109666308, 17.025466598),
                0.001, 0.0016, "planet", "planet"));
        areas.add(new AreaControl(
                new LatLng(51.109062, 17.0023325),
                new double[]{0.0523, -0.08, 0.0503, -0.06, 0.051, -0.047, 0.031, -0.027, 0.0423,-0.1},
                Color.BLUE));
        areas.add(new AreaControl(
                new LatLng(51.109062, 17.0023325),
                new double[]{0.0123, 0.08, 0.0103, 0.06, 0.021, 0.047, 0.011, 0.027, -0.0123, 0.1},
                Color.RED));
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
                        drawPath();
                        updatePosition();
                        path.clear();
                        drawNearbyObjects();
                        drawOther();
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
        return lastLocation;
    }
    LatLng getLastLatLng() {
        return new LatLng(getLast().getLatitude(), getLast().getLongitude());
    }

    void setCamera() {
        Location last = getLast();
        if (last == null) {
            return;
        }
        LatLng from = new LatLng(last.getLatitude() - viewDistanceLong, last.getLongitude() - viewDistanceLong);
        LatLng to = new LatLng(last.getLatitude() + viewDistanceLat, last.getLongitude() + viewDistanceLat);
        LatLngBounds bounds = new LatLngBounds(from, to);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
    }

    void drawPath() {
        if (path.isEmpty()) {
            return;
        }
        lastLocation = path.get(path.size() - 1);
        PolylineOptions polyline = new PolylineOptions().color(R.color.colorPath);
        for (Location loc : path) {
            polyline.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
        }
        mMap.addPolyline(polyline);
    }

    void drawNearbyObjects() {
        if (getLast() == null) {
            return;
        }
        if (overlays.isEmpty()) {
            for (SpaceJunk junk : junks) {
                String imageName = junk.getImageName();
                Resources resources = getResources();
                final int resourceId = resources.getIdentifier(imageName, "drawable", getPackageName());
                GroundOverlay overlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                        .positionFromBounds(junk.getBounds())
                        .image(BitmapDescriptorFactory.fromResource(resourceId))
                        .clickable(true)
                        .visible(junk.getDistance(getLast(), distance)));
                idToJunk.put(overlay.getId(), junk);
                overlays.add(overlay);
            }
        } else {
            for (int i = 0; i < junks.size(); ++i) {
                GroundOverlay overlay = overlays.get(i);
                SpaceJunk junk = junks.get(i);
                overlay.setPositionFromBounds(junk.getBounds());
                overlay.setVisible(junk.getDistance(getLast(), distance));
            }
        }
    }

    void updatePosition() {
        if (getLast() == null) {
            return;
        }
        if (marker == null) {
            marker = mMap.addMarker(new MarkerOptions()
                    .position(getLastLatLng())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ship_small)));
            range = mMap.addCircle(new CircleOptions()
                    .center(getLastLatLng())
                    .radius(distance)
                    .strokeColor(Color.BLUE));
            setCamera();
        } else {
            marker.setPosition(getLastLatLng());
            range.setCenter(getLastLatLng());
        }
        communication.pushPosition(getLastLatLng());
    }

    void drawOther() {
        List<LatLng> other = communication.getOtherPath();
        if (other.isEmpty()) {
            return;
        }
        Log.i("Main", "Other: " + other.get(other.size() - 1).toString());

        LatLng otherLatLng = other.get(other.size() - 1);
        float[] results = new float[1];
        Location.distanceBetween(otherLatLng.latitude, otherLatLng.longitude,
                getLast().getLatitude(), getLast().getLongitude(), results);

        if (otherMarker == null) {
            otherMarker = mMap.addMarker(new MarkerOptions()
                    .position(otherLatLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ship_small))
                    .title("Enemy player")
                    .visible(results[0] < distance));
            otherRange = mMap.addCircle(new CircleOptions()
                    .center(otherLatLng)
                    .radius(distance)
                    .strokeColor(Color.RED)
                    .visible(results[0] < distance));
        } else {
            otherMarker.setPosition(otherLatLng);
            otherRange.setCenter(other.get(other.size() - 1));
            otherMarker.setVisible(results[0] < distance);
            otherRange.setVisible(results[0] < distance);
        }
    }

    void drawAreas() {
        for (AreaControl area : areas) {
            mMap.addPolygon(new PolygonOptions().addAll(
                    area.getPolygon()).strokeColor(area.getColor()).fillColor(area.getFillColor()));
        }
    }

    void drawThings() {
        Log.i("Main", "drawThings");
        drawNearbyObjects();
        drawAreas();
        drawOther();

        new Handler().postDelayed(this, 1000);
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
        mMap.setOnGroundOverlayClickListener(this);

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
        drawPath();
        updatePosition();
        drawThings();
        communicationThread.start();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        setCamera();
        return false;
    }

    @Override
    public void onGroundOverlayClick(GroundOverlay groundOverlay) {
        SpaceJunk junk = idToJunk.get(groundOverlay.getId());
        if (junk == null) {
            Log.e("Main", "NO JUNK");
            return;
        }
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.text);

        text.setText(junk.getTitle());
        ImageView image = (ImageView) layout.findViewById(R.id.image);
        final int catId = getResources().getIdentifier(junk.getRewardName(), "drawable", getPackageName());
        Bitmap icon = BitmapFactory.decodeResource(getResources(), catId);
        image.setImageBitmap(icon);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    @Override
    public void run() {
        drawThings();
    }
}
