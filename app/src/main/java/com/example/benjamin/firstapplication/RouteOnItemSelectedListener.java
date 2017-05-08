package com.example.benjamin.firstapplication;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Created by Benjamin on 3/30/2017.
 */

public class RouteOnItemSelectedListener implements OnItemSelectedListener {

    MapsActivity currentMapActivity;
    HashMap<String, Bus> currentHashMap;
    Spinner spinner2ref;
    StopOnItemSelectedListener currentStopListener;
    public Resources resources;
    Context ctx;
    public Object lock;
    private boolean paused;

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String routeID = parent.getItemAtPosition(pos).toString();
        String rID = routeID.split(":")[0];
        paused = true;
        lock = new Object();
//        Toast.makeText(parent.getContext(),routeID,Toast.LENGTH_SHORT).show();

        currentMapActivity.startRetrieveFileThread(rID, ctx);

//        currentMapActivity.getInternalBusData();
        synchronized(lock) {
            while (paused) {
                try {
//                    Thread.currentThread().wait();
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
//        }

            HashMap<String, Bus> busses = currentMapActivity.getInternalBusData();

            List<String> toSpin = new ArrayList<String>();
            Bus tmp = null;

            if (routeID.split(":")[1].equals(" North")) {
                for (Bus b : busses.values()) {
                System.out.println(b.id);
                    if (b.id.equals(rID) && b.direction == 1) {
                        tmp = b;
                        break;
                    }
                }
            } else if (routeID.split(":")[1].equals(" South")) {
                for (Bus b : busses.values()) {
                System.out.println(b.id);
                    if (b.id.equals(rID) && b.direction == 0) {
                        tmp = b;
                        break;
                    }
                }
            }
            if (tmp != null) {
                for (Stop p : tmp.busRoute) {
                    toSpin.add(p.stop_name);
                }
            }

            System.out.println(tmp);
            currentStopListener.setRouteName(routeID);
            currentMapActivity.setRouteName(rID);
            currentMapActivity.setDirection(tmp.direction);
            currentStopListener.setCurrentRoute(tmp.busRoute);
            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(currentMapActivity, android.R.layout.simple_spinner_item, toSpin);
            spinner2ref.setAdapter(adapter2);
        }
//    }

    public void onNothingSelected(AdapterView<?> arg0) {}

    public void onResume() {
        synchronized (lock) {
            paused = false;
            lock.notifyAll();
        }
    }

    public void setMapsClass (MapsActivity map) {
        currentMapActivity = map;
    }

    public void setSpinner2 (Spinner spin2) {
        spinner2ref = spin2;
    }

    public HashMap<String, Bus> getBusHashMap () {
        HashMap<String, Bus> a = Utility.getBusses();
        return a;
    }

    public void setStopListener (StopOnItemSelectedListener stopListener) {
        currentStopListener = stopListener;
    }

    public void setContext(Context cx) {
        ctx = cx;
    }

    public void setMapsRotate () {
        currentMapActivity.setRotate();
    }
}
