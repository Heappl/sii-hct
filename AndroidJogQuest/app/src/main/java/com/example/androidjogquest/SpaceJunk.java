package com.example.androidjogquest;

import android.location.Location;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class SpaceJunk {
    String name, imageName, rewardName;
    LatLng position;
    double latSize;
    double longSize;

    SpaceJunk(String name, LatLng position, double latSize, double longSize, String resourceName,
              String rewardName) {
        this.name = name;
        this.position = position;
        this.latSize = latSize;
        this.longSize = longSize;
        this.imageName = resourceName;
        this.rewardName = rewardName;
    }

    public String getImageName() {
        return this.imageName;
    }
    public String getRewardName() {
        return this.rewardName;
    }

    public boolean getDistance(Location last, double distance) {
        float[] results = new float[1];
        Location.distanceBetween(position.latitude, position.longitude,
                last.getLatitude(), last.getLongitude(), results);
        return results[0] < distance;
    }

    public LatLngBounds getBounds() {
        if (getTitle().equals("base")) {
            position = new LatLng(position.latitude, position.longitude + 0.0001);
        }
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
