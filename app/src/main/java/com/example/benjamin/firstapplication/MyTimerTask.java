package com.example.benjamin.firstapplication;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.TimerTask;

/**
 * Created by Benjamin on 4/12/2017.
 */

public class MyTimerTask extends TimerTask {

    public String routeName;
    MapsActivity currentMapActivity;

    @Override
    public void run() {
        Bus tmp = null;
//        Looper.prepare();

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            Handler mHandler = new Handler() {
//                public void handleMessage (Message msg) {
//
//                }
//            };
//            Looper.loop();
//            currentMapActivity.handler;
        if (routeName != null) {
//        try {
//            Utility.getBusHashMap();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        } catch (org.json.simple.parser.ParseException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

            HashMap<String, Bus> busses = currentMapActivity.getBusHashMapTmp();

            currentMapActivity.clearBusLocations();
            for (Bus b : busses.values()) {
                if (b.id.equals(routeName)) {
//                currentMapActivity.plotBusLocation(b, googlemap, stopID);
                    tmp = b;
                    currentMapActivity.plotBusLocation(b);
                }
            }
            currentMapActivity.plotRoutePath(tmp);
        }
    }

    public void setMapsClass (MapsActivity map) {
        currentMapActivity = map;
    }

    public void setRouteName (String route) {
        routeName = route;
    }
}
