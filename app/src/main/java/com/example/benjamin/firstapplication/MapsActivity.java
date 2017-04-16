package com.example.benjamin.firstapplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import static android.app.PendingIntent.getActivity;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public GoogleMap mMap;
    public Spinner routeSpinner, stopSpinner;
    public InputStream myStream;
    public Resources resources;
    public HashMap<String, Trip> tripHashMap = new HashMap<>();
    public Handler handler;
    public String routeName;
    public Boolean execute = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        routeSpinner = (Spinner) findViewById(R.id.routeSpinner);
        stopSpinner = (Spinner) findViewById(R.id.stopSpinner);

        //set spinner listeners
        RouteOnItemSelectedListener routeListener = new RouteOnItemSelectedListener();
        routeListener.setMapsClass(this);
        StopOnItemSelectedListener stopListener = new StopOnItemSelectedListener();
        stopListener.setMapsClass(this);
        stopListener.setGoogleMap(mMap);
        routeListener.setStopListener(stopListener);
//        stopListener.setRouteListener(routeListener);
        routeListener.setSpinner2(stopSpinner);

        //Obtain trips.txt file and parse into hashmap
//        resources = this.getResources();
//        InputStream textStream;
//        int textID = resources.getIdentifier("trips","raw",getPackageName());
//        textStream = resources.openRawResource(textID);
//        try {
//            HashMap<String, Trip> tripHashMap = Utility.parseTrips(textStream);
//            stopListener.setHashMap(tripHashMap);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

//        InputStream bussesStream;
//        int bussesID = resources.getIdentifier("vehiclemonitoring","raw",getPackageName());
//        bussesStream = resources.openRawResource(bussesID);
//        HashMap<String, Bus> busses = Utility.jsonParser(bussesStream);

        routeSpinner.setOnItemSelectedListener(routeListener);
        stopSpinner.setOnItemSelectedListener(stopListener);

        HashMap<String, Bus> busses = null;
        busses = getBusHashMapTmp();

//        try {
//            Utility.getBusHashMap();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        } catch (org.json.simple.parser.ParseException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        resources = this.getResources();
//        InputStream bussesStream;
//        int bussesID = resources.getIdentifier("busses","raw",getPackageName());
//        bussesStream = resources.openRawResource(bussesID);
//        Utility.decodeToHashMap(bussesStream);
//        busses = Utility.getBusses();

        List<String> toSpin1 = new ArrayList<String>();
        List<String> toSpin2 = new ArrayList<String>();

        for (Bus b : busses.values()) {
            if(!toSpin1.contains(b.id)) {
                toSpin1.add(b.id);
            }
        }

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,toSpin1);
        routeSpinner.setAdapter(adapter1);

        Bus tmp = busses.get(0);
        if (tmp != null) {
            for (Stop p : tmp.busRoute) {
                toSpin2.add(p.stop_name);
            }
        }
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,toSpin2);
        stopSpinner.setAdapter(adapter2);

        handler = new Handler() {
            @Override
            public void handleMessage(Message inputMessage) {
                HashMap<String, Bus> busses = getBusHashMapTmp();
                Bus tmp = null;
                clearBusLocations();
                for (Bus b : busses.values()) {
                    if (b.id.equals(routeName)) {
//                currentMapActivity.plotBusLocation(b, googlemap, stopID);
                        tmp = b;
                        plotBusLocation(b);
                    }
                }
                plotRoutePath(tmp);
//                execute = true;
            }
        };
        this.startTimer();


//        do {
//            if (execute == true) {
//                busses = getBusHashMapTmp();
//                tmp = null;
//                clearBusLocations();
//                for (Bus b : busses.values()) {
//                    if (b.id.equals(routeName)) {
////                currentMapActivity.plotBusLocation(b, googlemap, stopID);
//                        tmp = b;
//                        plotBusLocation(b);
//                    }
//                }
//                plotRoutePath(tmp);
//                execute = false;
//            }
//        } while (true);


//        MyTimerTask timerTask = new MyTimerTask();
//        Timer timer = new Timer(true);
//        timerTask.setMapsClass(this);
//        stopListener.setTimer(timerTask);

//        Message toHandler = handler.obtainMessage(1,timerTask);
//        toHandler.sendToTarget();
//        timer.scheduleAtFixedRate(timerTask,0,11000);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng MTA = new LatLng(40.75,-73.9);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(MTA));
    }


    public void startTimer() {
        Timer timer = new Timer(true);

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                handler.obtainMessage(0).sendToTarget();

            }
        },30000,60000);
    }


    public void plotBusLocation (Bus bus, String stopID) {
        Stop stop = null;
        LatLng MTA = new LatLng(bus.latitude,bus.longitude);
        for (Stop s : bus.busRoute) {
            if (stopID.equals(s.stop_id)) {
                stop = s;
                break;
            }
        }
        String eta = bus.getExpectedArrivalTime(stop);
        mMap.addMarker(new MarkerOptions().position(MTA).title(eta));
    }

    public void plotBusLocation (Bus bus) {
        LatLng MTA = new LatLng(bus.latitude,bus.longitude);
        mMap.addMarker(new MarkerOptions().position(MTA));
    }

    public void clearBusLocations () {
        mMap.clear();
    }

    public void plotSetBusLocations (HashMap<String, Bus> busses) {
        for (Bus b : busses.values()) {
            plotBusLocation (b);
        }
    }

    public HashMap<String, Bus> getBusHashMapTmp () {
        resources = this.getResources();
        InputStream bussesStream;
        int bussesID = resources.getIdentifier("testencoding","raw",getPackageName());
        bussesStream = resources.openRawResource(bussesID);
        Utility.decodeToHashMap(bussesStream);
        HashMap<String, Bus> out = Utility.getBusses();
        return out;
    }

    public void plotRoutePath (Bus b1, Bus b2) {
        List<MyPoint> busPoints1 = b1.route_shape.points;
        List<MyPoint> busPoints2 = b2.route_shape.points;
        Polyline path = mMap.addPolyline(new PolylineOptions().width(5).color(Color.RED));
        List<LatLng> busList = new ArrayList<>();
        for (int i = 0; i < busPoints1.size(); i++) {
            System.out.println();
            busList.add(new LatLng(busPoints1.get(i).shape_pt_lat,busPoints1.get(i).shape_pt_lon));
        }
        for (int i = 0; i < busPoints2.size(); i++) {
            System.out.println();
            busList.add(new LatLng(busPoints2.get(i).shape_pt_lat,busPoints2.get(i).shape_pt_lon));
        }
        path.setPoints(busList);
    }

    public void plotRoutePath (Bus b) {
        List<MyPoint> busPoints = b.route_shape.points;
        Polyline path = mMap.addPolyline(new PolylineOptions().width(5).color(Color.RED));
        List<LatLng> busList = new ArrayList<>();
        for (int i = 0; i < busPoints.size(); i++) {
            System.out.println();
            busList.add(new LatLng(busPoints.get(i).shape_pt_lat,busPoints.get(i).shape_pt_lon));
        }
        path.setPoints(busList);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        parent.getItemAtPosition(pos);
    }

    public Spinner getSpinner2 () {
        return stopSpinner;
    }

    public Spinner getSpinner1 () {
        return routeSpinner;
    }

    public void setRouteName (String route) {
        routeName = route;
    }
}
