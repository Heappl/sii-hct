package com.example.androidjogquest;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.util.HashMap;

public class RightHelper extends FragmentActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private final int sPermissionCode = 8882;
    private final HashMap<String, String> continueWithRequest = new HashMap<>();
    private final HashMap<String, PermissionHandler> onPermissionGranted = new HashMap<>();

    private void requestContinuations()
    {
        continueWithRequest.put(Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private void requestPermission(String permission, PermissionHandler handler) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            Log.i("MainActivity", "should show request permission rationale for " + permission + ": ignoring");
        }
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, sPermissionCode);
            onPermissionGranted.put(permission, handler);
        }
        handler.onPermissionGranted();
    }

    public interface PermissionHandler {
        public void onPermissionGranted();
    }

    public void requestLocationPermission(PermissionHandler handler) {
        Log.i("MainActivity", "requestLocationPermission");
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, handler);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("MainActivity", "onRequestPermissionsResult " + requestCode);
        if (requestCode != sPermissionCode) return;
        for (int i = 0; i < permissions.length; ++i) {
            if ((grantResults[i] != PackageManager.PERMISSION_GRANTED)
                    && continueWithRequest.containsKey(permissions[i])) {
                requestPermission(continueWithRequest.get(permissions[i]), onPermissionGranted.get(permissions[i]));
                onPermissionGranted.remove(permissions[i]);
            }
        }
    }
}
