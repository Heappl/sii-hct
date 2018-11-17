package com.example.androidjogquest;

import android.location.Location;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class SpaceJunk {
    String name, imageName;
    LatLng position;
    double latSize;
    double longSize;

    SpaceJunk(String name, LatLng position, double latSize, double longSize, String resourceName) {
        this.name = name;
        this.position = position;
        this.latSize = latSize;
        this.longSize = longSize;
        this.imageName = resourceName;
    }

    public String getImageName() {
        return this.imageName;
    }

    public boolean getDistance(Location last, double latDist, double longDist) {
        return (position.latitude - last.getLatitude() < latDist)
                && (position.longitude - last.getLongitude() < longDist);
    }

    public LatLngBounds getBounds() {
        LatLng skytower_min = new LatLng(position.latitude - latSize, position.longitude - longSize);
        LatLng skytower_max = new LatLng(position.latitude + latSize, position.longitude + longSize);
        return new LatLngBounds(skytower_min, skytower_max);
    }

    public LatLng getPosition() {
        return position;
    }

    public String getTitle() {
        return name;
    }
}
