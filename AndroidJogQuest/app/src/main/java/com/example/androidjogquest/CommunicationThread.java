package com.example.androidjogquest;


import android.media.Image;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.io.InvalidClassException;
import java.lang.InterruptedException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CommunicationThread implements Runnable {
    List<LatLng> positions = new ArrayList<>();
    List<LatLng> other_player_positions = new ArrayList<>();

    private String createReq() {
        List<LatLng> positions = getPositions();
        if (positions.isEmpty()) {
            return null;
        }
        try {
            String jsonString = new JSONArray()
                    .put(2)
                    .put(positions.get(positions.size() - 1).latitude)
                    .put(positions.get(positions.size() - 1).longitude)
                    .toString();
            return jsonString;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void run() {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(SocketType.REQ);
        if (socket.connect("tcp://10.254.38.30:5555")) {
            Log.i("Comms", "connected");
        } else {
            Log.e("Comms", "Couldn't connect");
            return;
        }

        try {

            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(1000);

                String req = createReq();
                if (req == null) {
                    continue;
                }
                Log.i("Comms", req);
                if (socket.send(req)) {
                    Log.i("Comms", "sent");
                } else {
                    Log.i("Comms", "couldn't send");
                    continue;
                }
                String response = socket.recvStr();
                JSONObject obj = new JSONObject(response);
                double latitude = Double.parseDouble(obj.getJSONArray("1").getString(0));
                double longitude = Double.parseDouble(obj.getJSONArray("1").getString(1));
                Log.i("Comms", "receive: " + latitude + ", " + longitude);
                pushOtherPosition(new LatLng(latitude, longitude));
            }
        } catch (Throwable exception) {
            System.err.println(exception);
        }
        socket.close();
        context.term();
    }

    private synchronized void pushOtherPosition(LatLng other) {
        other_player_positions.add(other);
    }

    public synchronized List<LatLng> getOtherPath() {
        List<LatLng> ret = this.other_player_positions;
        this.other_player_positions = new ArrayList<>();
        return ret;
    }

    private synchronized List<LatLng> getPositions() {
        if (this.positions.isEmpty()) {
            return new ArrayList<>();
        }
        List<LatLng> ret = this.positions;
        this.positions = new ArrayList<>();
        this.positions.add(ret.get(ret.size() - 1));
        return ret;
    }

    public synchronized void pushPosition(LatLng position) {
        this.positions.add(position);
        Log.i("Comms", position.toString());
        Log.i("Comms", this.positions.size() + "");
    }
}
