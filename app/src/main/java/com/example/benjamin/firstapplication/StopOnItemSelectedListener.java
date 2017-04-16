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
//    HashMap<String, Trip> currentTripHashMap;
    public Stop currentBusRoute[];
    public String routeName;
    GoogleMap googlemap;
    MyTimerTask timerTask;

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String stopID = parent.getItemAtPosition(pos).toString();
        Bus tmp1 = null;
        Bus tmp0 = null;
        HashMap<String, Bus> busses = Utility.getBusses();

//        timerTask.setRouteName(routeName);
        currentMapActivity.clearBusLocations();
        for (Bus b : busses.values()) {
            if (b.id.equals(routeName)) {
//                currentMapActivity.plotBusLocation(b, googlemap, stopID);
                if (b.direction == 1) {
                    tmp1 = b;
                } else if (b.direction == 0) {
                    tmp0 = b;
                }
                currentMapActivity.plotBusLocation(b);
            }
        }
        if (tmp0 != null && tmp1 != null) {
            currentMapActivity.plotRoutePath(tmp0, tmp1);
        } else if (tmp0 == null) {
            currentMapActivity.plotRoutePath(tmp1);
        } else if (tmp1 == null) {
            currentMapActivity.plotRoutePath(tmp0);
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) { }

    public void setMapsClass (MapsActivity map) { currentMapActivity = map; }

    public void setCurrentRoute (Stop route[]) {
        currentBusRoute = route;
    }

    public void setGoogleMap (GoogleMap map) {
        googlemap = map;
    }

    public void setRouteName (String route) {
        routeName = route;
    }

    public void setTimer (MyTimerTask t) { timerTask = t; }

    public HashMap<String, Bus> getBusHashMap () {
        HashMap<String, Bus> a = Utility.getBusses();
        return a;
    }
}

