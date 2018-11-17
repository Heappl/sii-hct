package com.example.androidjogquest;

import android.annotation.SuppressLint;
import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class AreaControl {
    List<LatLng> polygon = new ArrayList<>();
    int color;

    public AreaControl(LatLng latLng, double[] doubles, int color) {
        for (int i = 0; i < doubles.length / 2; i++) {
            polygon.add(new LatLng(latLng.latitude + doubles[2 * i + 0], latLng.longitude + doubles[2 * i + 1]));
        }
        this.color = color;
    }

    List<LatLng> getPolygon() {
        return polygon;
    }

    public int getColor() {
        return color;
    }

    @SuppressLint("NewApi")
    public int getFillColor() {
        if (color == Color.RED) {
            return 0x6FF0000;
        } else {
            return 0x60000FF;
        }
    }
}
