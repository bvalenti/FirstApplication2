package com.example.benjamin.firstapplication;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.google.android.gms.maps.GoogleMap;

import java.util.HashMap;

/**
 * Created by Benjamin on 3/30/2017.
 */

public class StopOnItemSelectedListener implements OnItemSelectedListener {

    MapsActivity currentMapActivity;
//    RouteOnItemSelectedListener currentRouteListener;
    HashMap<String, Trip> currentTripHashMap;
    public Stop currentBusRoute[];
    public String routeName;
    GoogleMap googlemap;

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String stopID = parent.getItemAtPosition(pos).toString();

        HashMap<String, Bus> busses = getBusHashMap();

        for (Bus b : busses.values()) {
            if (b.id.equals(routeName)) {
                currentMapActivity.plotBusLocation(b, googlemap, stopID);
            }
        }


    }

    public void onNothingSelected(AdapterView<?> arg0) {
    }

    public void setMapsClass (MapsActivity map) {
        currentMapActivity = map;
    }

    public void setHashMap (HashMap<String, Trip> hm) {
        currentTripHashMap = hm;
    }

    public void setCurrentRoute (Stop route[]) {
        currentBusRoute = route;
    }

    public void setGoogleMap (GoogleMap map) {
        googlemap = map;
    }

    public void setRouteName (String route) {
        routeName = route;
    }

    public HashMap<String, Bus> getBusHashMap () {
        return null;
    }

}

