package com.example.benjamin.firstapplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
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
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import java.util.concurrent.locks.Lock;


//import static android.app.PendingIntent.getActivity;
//import static android.provider.Telephony.Mms.Part.FILENAME;
import java.io.FileOutputStream;

import javax.net.ssl.HttpsURLConnection;

import static com.example.benjamin.firstapplication.R.id.map;
//import static com.example.benjamin.firstapplication.R.raw.busencoding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public GoogleMap mMap;
    public GoogleMap myMap;
    public Spinner routeSpinner, stopSpinner;
    public RouteOnItemSelectedListener routeListener;
    public StopOnItemSelectedListener stopListener;
    public Resources resources;
    public Handler handler;
    public String routeName;
    public int currentDirection;
    public String stopName;
    public Boolean bool = false;
    public HashMap<String,Bus> busses = new HashMap<String,Bus>();
    final public Object lock = new Object();
    public Object lock1; // = new Object();
    InputStream iss = null;
    InputStream is = null;
    private boolean paused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);

//        MapFragment mapFragmentR = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        mMap = mapFragment.getMapAsync();

        routeSpinner = (Spinner) findViewById(R.id.routeSpinner);
        stopSpinner = (Spinner) findViewById(R.id.stopSpinner);

        //set spinner listeners
        routeListener = new RouteOnItemSelectedListener();
        routeListener.setMapsClass(this);
        stopListener = new StopOnItemSelectedListener();
        stopListener.setMapsClass(this);
        stopListener.setGoogleMap(mMap);
        routeListener.setStopListener(stopListener);
        routeListener.setSpinner2(stopSpinner);

        routeSpinner.setOnItemSelectedListener(routeListener);
        stopSpinner.setOnItemSelectedListener(stopListener);

        final Context ctx = getApplicationContext();
        routeListener.setContext(ctx);

        List<String> toSpin1 = new ArrayList<String>();
        List<String> toSpin2 = new ArrayList<String>();

        busses = getBusHashMapRes();
        for (Bus b : busses.values()) {
            if(!toSpin1.contains(b.id + ": North")) {
                toSpin1.add(b.id + ": North");
                toSpin1.add(b.id + ": South");
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

//////////////////////////////////////////////////////////////////////////

        //Handler
        handler = new Handler() {

            @Override
            public void handleMessage(Message inputMessage) {

                startRetrieveFileThreadTimer(routeName, ctx);
                synchronized (lock1) {
//                    while (paused) {
//                        try {
//                            lock1.wait();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }


                    File file = new File(getFilesDir(), "busencoding");
                    FileInputStream bussesStream = null;
                    try {
                        bussesStream = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Utility.decodeToHashMap(bussesStream);
                    busses = Utility.getBusses();

                    Bus tmp = null;
                    clearBusLocations();
                    for (Bus b : busses.values()) {
                        if (b.id.equals(routeName) && b.direction == currentDirection) {
                            tmp = b;
//                        plotBusLocation(b);
                            try {
                                plotBusLocation(b, stopName);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    plotRoutePath(tmp);
                }
            }
        };
        this.startTimer();
//        setMapsOptions();
    }

    //OnMapReady
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng MTA = new LatLng(40.75, -73.9);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(MTA));

        mMap.getUiSettings().setRotateGesturesEnabled(false);
    }

    //Starts timer for updating the data base every minute
    public void startTimer() {
        Timer timer = new Timer(true);

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                handler.obtainMessage(0).sendToTarget();

            }
        },70000,70000);
    }

    //Method that starts the thread to pull a file from the server
    public void startRetrieveFileThread(String rID, Context ctx1) {

            final Context finalCtx = ctx1;
            final String RouteID = rID;
            final Thread retrieveFile = new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (routeListener.lock) {
                        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
                        try {
                            //Code for retrieving files from server
                            URL url = new URL("http://129.3.210.6:8080/MTABusServlet/MTABusServlet?RequestType=getBusses&RouteID=" + RouteID);
//                        URL url = new URL("http://74.79.83.251:8080/MTABusServlet/MTABusServlet?RequestType=getBusses&RouteID=" + RouteID);
//                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                            URLConnection urlConnection = url.openConnection();
                            String fileName = Utility.getFile2(urlConnection, "/Internal Storage/Android/data", "busencoding", false, finalCtx);
                            routeListener.onResume();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                        routeListener.onResume();

                }
            });
            retrieveFile.start();
    }

    public void startRetrieveFileThreadTimer(String rID, Context ctx1) {
        final Context finalCtx = ctx1;
        final String RouteID = rID;
        final Thread retrieveFileTimer = new Thread(new Runnable () {
            @Override
            public void run() {
                synchronized (lock1) {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
                    try {
                        //Code for retrieving files from server
//                        URL url = new URL("http://129.3.212.153:8080/MTABusServlet/MTABusServlet?RequestType=getBusses");
                        URL url = new URL("http://129.3.210.6:8080/MTABusServlet/MTABusServlet?RequestType=getBusses&RouteID=" + RouteID);
//                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        URLConnection urlConnection = url.openConnection();
                        String fileName = Utility.getFile2(urlConnection, "/Internal Storage/Android/data", "busencoding", false, finalCtx);

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    Resume();
                }
            }
        });
        retrieveFileTimer.start();
    }

    public void runBusThread (final double latitude, final double longitude, final Stop tmpStop, final Bus bus) {
        Thread retrieveETA = new Thread(new Runnable() {
            String dura;

            @Override
            public void run() {
                try {
                    URL urlToGet = new URL("https://maps.googleapis.com/maps/api/directions/json?origin=" +
                            latitude + "," + longitude + "&destination=" + tmpStop.stop_lat + "," + tmpStop.stop_lon
                            + "&transit_mode=bus&key=AIzaSyCiVgMTjGyglB74UdndS40xCNzCaUIcoz4");
                    HttpsURLConnection urlConnection = (HttpsURLConnection) urlToGet.openConnection();
                    urlConnection.getInputStream();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    String jsonString = new String();
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.contains("duration")) {
                            line = br.readLine();
                            break;
                        }

                        br.close();
                       // System.out.println(line);
                        dura = line; //.split(":")[1];
                        urlConnection.disconnect();
                        bus.setDuration(dura);
                    }
                } catch(MalformedURLException e){
                    e.printStackTrace();
                } catch(IOException e){
                    e.printStackTrace();
                }
//                bus.onResume();
            }
        });
        retrieveETA.start();
    }

    //Method to plot locations of buses on the map
    public void plotBusLocation (Bus bus, String stopID) throws MalformedURLException, InterruptedException {
        Stop stop = null;
        paused = true;
        LatLng MTA = new LatLng(bus.latitude, bus.longitude);
        lock1 = new Object();


        for (Stop s : bus.busRoute) {
           // System.out.println(bus.destinationName);
            if (stopID.equals(s.stop_name)) {
                stop = s;
                break;
            }
        }
        LatLng StopLL = new LatLng(stop.stop_lat, stop.stop_lon);

        bus.setCurrentMaps(this);
        bus.getExpectedArrivalTime(stop);

//        synchronized(lock1) {
//            while (paused) {
//                try {
//                    lock1.wait();
////                    Thread.currentThread().sleep(50);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        Thread.currentThread().sleep(500);
        plotBus2(MTA,stop,bus);
    }

    //Unnecessary partition of methods
    public void plotBus2 (LatLng MTA, Stop stop, Bus bus) {
        LatLng StopLL = new LatLng(stop.stop_lat, stop.stop_lon);
        String eta = bus.duration;
        if (eta != null) {
            mMap.addMarker(new MarkerOptions().position(MTA).title(bus.busID + ": " + eta));
        } else if (eta == null) {
            mMap.addMarker(new MarkerOptions().position(MTA).title("This bus is currently not en-route to the specified stop."));
        }
        mMap.addMarker(new MarkerOptions().position(StopLL).title(stop.stop_name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.getUiSettings().setRotateGesturesEnabled(false);
    }

    public void Resume() {
        synchronized (lock1) {
            paused = false;
            lock1.notifyAll();
        }
    }

    //Plots bus locations without the estimated arrival time
    public void plotBusLocation (Bus bus) {
        LatLng MTA = new LatLng(bus.latitude,bus.longitude);
        mMap.addMarker(new MarkerOptions().position(MTA));
    }

    //Clears the map of all points
    public void clearBusLocations () {
        mMap.clear();
    }

    //Method that is used to get the initial busses hashmap
    public HashMap<String, Bus> getBusHashMapRes () {
        resources = this.getResources();
        InputStream bussesStream;
        int bussesID = resources.getIdentifier("busencoding","raw",getPackageName());
        bussesStream = resources.openRawResource(bussesID);
        Utility.decodeToHashMap(bussesStream);
        HashMap<String, Bus> out = Utility.getBusses();
        return out;
    }

    //Plots both the north and south route paths obtained from two bus object traveling in different directions
    public void plotRoutePath (Bus b1, Bus b2) {
        List<MyPoint> busPoints1 = b1.route_shape.points;
        List<MyPoint> busPoints2 = b2.route_shape.points;
        Polyline path = mMap.addPolyline(new PolylineOptions().width(5).color(Color.RED));
        List<LatLng> busList = new ArrayList<>();
        for (int i = 0; i < busPoints1.size(); i++) {
          //  System.out.println();
            busList.add(new LatLng(busPoints1.get(i).shape_pt_lat,busPoints1.get(i).shape_pt_lon));
        }
        for (int i = 0; i < busPoints2.size(); i++) {
          //  System.out.println();
            busList.add(new LatLng(busPoints2.get(i).shape_pt_lat,busPoints2.get(i).shape_pt_lon));
        }
        path.setPoints(busList);
    }

    //Plots the route path for one direction obtained from one bus object
    public void plotRoutePath (Bus b) {
        List<MyPoint> busPoints = b.route_shape.points;
        Polyline path = mMap.addPolyline(new PolylineOptions().width(5).color(Color.RED));
        List<LatLng> busList = new ArrayList<>();
        for (int i = 0; i < busPoints.size(); i++) {
          //  System.out.println();
            busList.add(new LatLng(busPoints.get(i).shape_pt_lat,busPoints.get(i).shape_pt_lon));
        }
        path.setPoints(busList);
    }

    public HashMap<String, Bus> getInternalBusData () {
        File file = new File(getFilesDir(), "busencoding");
        FileInputStream bussesStream = null;
        try {
            bussesStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Utility.decodeToHashMap(bussesStream);
        HashMap<String, Bus> busHashMap = Utility.getBusses();
        return busHashMap;
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

    public void setDirection(int dir) { currentDirection = dir; }

    public void setStopName (String stop) {
        stopName = stop;
    }

    public void setBool (Boolean bo) {bool = bo;}

    public void setRotate() {
        mMap.getUiSettings().setRotateGesturesEnabled(false);
    }
}
