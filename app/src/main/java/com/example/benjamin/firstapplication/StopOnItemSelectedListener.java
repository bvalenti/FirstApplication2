package com.example.benjamin.firstapplication;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.google.android.gms.maps.GoogleMap;

import java.net.MalformedURLException;
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
        Bus tmp = null;
//        Bus tmp1 = null;
//        Bus tmp0 = null;
        HashMap<String, Bus> busses = Utility.getBusses();
        String rName = routeName.split(":")[0];
        currentMapActivity.setStopName(stopID);
//        timerTask.setRouteName(routeName);
        currentMapActivity.clearBusLocations();

        if (routeName.split(":")[1].equals(" North")) {
            for (Bus b : busses.values()) {
                if (b.id.equals(rName) && b.direction == 1) {
                    tmp = b;
//                    currentMapActivity.plotBusLocation(b);
                    try {
                        currentMapActivity.plotBusLocation(b,stopID);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        } else if (routeName.split(":")[1].equals(" South")) {
            for (Bus b : busses.values()) {
                if (b.id.equals(rName) && b.direction == 0) {
                    tmp = b;
//                    currentMapActivity.plotBusLocation(b);
                    try {
                        currentMapActivity.plotBusLocation(b,stopID);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (tmp != null) {
            currentMapActivity.plotRoutePath(tmp);
        }
        currentMapActivity.setStopName(stopID);
//        this.onItemSelected(parent,view,pos,id);
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

