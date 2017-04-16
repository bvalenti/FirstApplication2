package com.example.benjamin.firstapplication;

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
//    StopOnItemSelectedListener currentStopListener;
    HashMap<String, Bus> currentHashMap;
    Spinner spinner2ref;
    StopOnItemSelectedListener currentStopListener;
    public Resources resources;

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String routeID = parent.getItemAtPosition(pos).toString();
//        Toast.makeText(parent.getContext(),routeID,Toast.LENGTH_SHORT).show();

        HashMap<String, Bus> busses = Utility.getBusses();
        List<String> toSpin = new ArrayList<String>();
        Bus tmp = null;
        for (Bus b : busses.values()) {
            System.out.println(b.id);
           if (b.id.equals(routeID)) {
               tmp = b;
               break;
           }
        }
        if (tmp != null) {
            for (Stop p : tmp.busRoute) {
                toSpin.add(p.stop_name);
            }
        }
        currentStopListener.setRouteName(routeID);
        currentMapActivity.setRouteName(routeID);
        currentStopListener.setCurrentRoute(tmp.busRoute);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(currentMapActivity,android.R.layout.simple_spinner_item,toSpin);
        spinner2ref.setAdapter(adapter2);
    }

    public void onNothingSelected(AdapterView<?> arg0) {
    }

    public void setMapsClass (MapsActivity map) {
        currentMapActivity = map;
    }

    public void setSpinner2 (Spinner spin2) {
        spinner2ref = spin2;
    }

    public void refine () {}

    public HashMap<String, Bus> getBusHashMap () {
        HashMap<String, Bus> a = Utility.getBusses();
        return a;
    }

    public void setStopListener (StopOnItemSelectedListener stopListener) {
        currentStopListener = stopListener;
    }
}
