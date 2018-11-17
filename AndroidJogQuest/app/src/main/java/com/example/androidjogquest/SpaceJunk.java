package com.example.androidjogquest;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class SpaceJunk {


    public double getDistance(Location last) {
        return 0;
    }

    public LatLngBounds getBounds() {
        LatLng skytower_min = new LatLng(51.086666308, 17.014166598);
        LatLng skytower_max = new LatLng(51.092666308, 17.020166598);
        return new LatLngBounds(skytower_min, skytower_max);
    }

    public LatLng getPosition() {
        LatLng skytower = new LatLng(51.089666308, 17.017166598);
        return skytower;
    }

    public String getTitle() {
        return "SkyTower";
    }
}
