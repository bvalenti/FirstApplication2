package com.example.benjamin.firstapplication;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.PendingIntent.getActivity;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public GoogleMap mMap;
    public Spinner routeSpinner, stopSpinner;
    public InputStream myStream;
    public Resources resources;
    public HashMap<String, Trip> tripHashMap = new HashMap<>();

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
        resources = this.getResources();
        InputStream textStream;
        int textID = resources.getIdentifier("trips","raw",getPackageName());
        textStream = resources.openRawResource(textID);
        try {
            HashMap<String, Trip> tripHashMap = Utility.parseTrips(textStream);
            stopListener.setHashMap(tripHashMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//        InputStream bussesStream;
        int bussesID = resources.getIdentifier("vehiclemonitoring","raw",getPackageName());
//        bussesStream = resources.openRawResource(bussesID);
//        HashMap<String, Bus> busses = Utility.jsonParser(bussesStream);


        routeSpinner.setOnItemSelectedListener(routeListener);
        stopSpinner.setOnItemSelectedListener(stopListener);

        HashMap<String, Bus> busses = null;
        try {
            busses = getBusHashMap();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> toSpin1 = new ArrayList<String>();
        List<String> toSpin2 = new ArrayList<String>();

        for (int i = 1; i <= 116; i++) {
            toSpin1.add("M" + Integer.toString(i));
//            toSpin2.add("M" + Integer.toString(i));
        }
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,toSpin1);
        routeSpinner.setAdapter(adapter1);

        Bus tmp = null;
        for (Bus b : busses.values()) {
            System.out.println(b.id);
           if (b.id.equals("M1")) {
               tmp = b;
               break;
           }
        }

        if (tmp != null) {
            for (Stop p : tmp.busRoute) {
                toSpin2.add(p.stop_name);
            }
        }

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,toSpin2);
        stopSpinner.setAdapter(adapter2);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng MTA = new LatLng(40.75,-73.9);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(MTA));

//        Bus a = new Bus((float) -73.5, (float) 40.9,"","","","",1);
//        Bus b = new Bus((float) -73.7, (float) 41.0,"","","","",1);
//
//        plotBusLocation(a,mMap);
//        plotBusLocation(b,mMap);
    }

    public void plotBusLocation (Bus bus, GoogleMap googleMap, String stopID) {
        Stop stop = null;
        LatLng MTA = new LatLng(bus.latitude,bus.longitude);
        for (Stop s : bus.busRoute) {
            if (stopID.equals(s.stop_id)) {
                stop = s;
                break;
            }
        }
        String eta = bus.getExpectedArrivalTime(stop);
        googleMap.addMarker(new MarkerOptions().position(MTA).title(eta));
    }

    public void plotBusLocation (Bus bus, GoogleMap googleMap) {
        LatLng MTA = new LatLng(bus.latitude,bus.longitude);
        googleMap.addMarker(new MarkerOptions().position(MTA));
    }

    public void plotSetBusLocations (HashMap<String, Bus> busses, GoogleMap googleMap) {
        for (Bus b : busses.values()) {
            plotBusLocation (b, googleMap);
        }
    }

    public HashMap<String, Bus> getBusHashMap () throws ParseException, org.json.simple.parser.ParseException, IOException {
            HashMap<String, Bus> busse = Utility.jsonParser("C:\\Users\\Benjamin\\Desktop\\busses.json");
        return busse;
    }

    public void refineStopSpinner (String routeID) {

    }

    public void plotRoutePath (Bus b, GoogleMap googleMap) {
        List busPoints = b.route_shape.points;
        Polyline path = googleMap.addPolyline(new PolylineOptions().width(5).color(Color.RED));
        path.setPoints(busPoints);
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

}
