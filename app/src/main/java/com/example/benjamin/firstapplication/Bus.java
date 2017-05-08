package com.example.benjamin.firstapplication;

//import java.time.LocalDateTime;
import android.os.Process;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import javax.json.JsonObject;
import javax.net.ssl.HttpsURLConnection;

public class Bus {

    public float longitude, latitude, latitudeOfLastPolling, longitudeOfLastPolling;
    public String id, destinationName, origin, busID;
    public String expectedArrivalTime, expectedDepartureTime;
    public int direction;
    public Stop busRoute[];
    public Shape route_shape;
    public Stop tmpStop;
    public String duration;
    private Object lock1;
    MapsActivity currentMapActivity;
    private boolean paused;
    ArrayList<MyPoint> combinedStops;

    public Bus(float lo, float la, String i, String DN, String ex, String t, int d) {
        longitude = lo;
        latitude = la;
        id = i;
        destinationName = DN;
        expectedArrivalTime = ex;
        expectedDepartureTime = t;
        direction = d;
    }

    public void runBusThread () {
        Thread retrieveETA = new Thread(new Runnable() {

            @Override
            public void run() {
                String waypoints = "";
//                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                try {
//                    for (MyPoint p : combinedStops) {
//                        waypoints = waypoints + "|" + Double.toString(p.shape_pt_lat) + "," + Double.toString(p.shape_pt_lon);
//                    }
                        URL urlToGet = new URL("https://maps.googleapis.com/maps/api/directions/json?origin=" +
                                latitude + "," + longitude + "&destination=" + tmpStop.stop_lat + "," + tmpStop.stop_lon
                                + "&transit_mode=bus&key=AIzaSyCiVgMTjGyglB74UdndS40xCNzCaUIcoz4");
                        HttpsURLConnection urlConnection = (HttpsURLConnection) urlToGet.openConnection();
                    int response = urlConnection.getResponseCode();
                    System.out.println(response);
                    urlConnection.getInputStream();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.contains("duration")) {
                            line = br.readLine();
                            break;
                        }
                    }

                    br.close();
                    System.out.println(line);
                    String myString = line.split(":")[1].split(",")[0];
                    duration = myString.substring(2, myString.length()-1);

                    System.out.println(duration);
                    urlConnection.disconnect();
                    setDuration(duration);

                    } catch(MalformedURLException e){
                        e.printStackTrace();
                    } catch(IOException e){
                        e.printStackTrace();
                    }
//                currentMapActivity.Resume();
//                onResume();
            }
        });
        retrieveETA.start();
    }

    public void getExpectedArrivalTime(Stop s) throws MalformedURLException, InterruptedException {
        String estimatedArrivalTime = null;
        int stopIndex = -1;
        int nextStopIndex = -1;
        int destinationIndex = -1;
        double totalDistance = 0;
        double totalDistanceToStop = 0;
        LatLng originLL = new LatLng(latitude, longitude);
        LatLng destinationLL = new LatLng(s.stop_lat, s.stop_lon);
        tmpStop = s;
        lock1 = new Object();
        paused = true;

            //Find the index of stop, s, in the busRoute array.
            for (int j = 0; j < busRoute.length; j++) {
                if (s.stop_name.equals(busRoute[j].stop_name)) {
                    stopIndex = j;
                }
            }

            //Make sure the stop, s, is contained in the busRoute.
            if (stopIndex >= 0) {

                combinedStops = combineRouteWithShape(busRoute, route_shape);
                double distancesBetweenStops[] = new double[combinedStops.size()];
                for (int i = 0; i < combinedStops.size() - 1; i++) {
                    LatLng latLngBusRoute2 = new LatLng(combinedStops.get(i + 1).shape_pt_lat, combinedStops.get(i + 1).shape_pt_lon);
                    LatLng latLngBusRoute1 = new LatLng(combinedStops.get(i).shape_pt_lat, combinedStops.get(i).shape_pt_lon);
                    double tmp = Math.sqrt(Math.pow(latLngBusRoute2.latitude - latLngBusRoute1.latitude, 2) +
                            Math.pow(latLngBusRoute2.longitude - latLngBusRoute1.longitude, 2));
                    distancesBetweenStops[i] = tmp;
                }

                double tmpDistances[] = new double[combinedStops.size()];
                for (int i = 0; i <= combinedStops.size() - 1; i++) {
                    tmpDistances[i] = Math.sqrt(Math.pow(latitude - combinedStops.get(i).shape_pt_lat, 2)
                            + Math.pow(longitude - combinedStops.get(i).shape_pt_lon, 2));
                }
                int busIndex = minIndex(tmpDistances);

                double distanceToNextPoint = 0;
                int indexOfNextPoint = -1;
                if (busIndex == 0) {
                    indexOfNextPoint = 1;
                } else if (busIndex == combinedStops.size() - 1) {
                    indexOfNextPoint = combinedStops.size() - 2;
                } else if (tmpDistances[busIndex - 1] > tmpDistances[busIndex + 1]) {
                    indexOfNextPoint = busIndex + 1;
                } else if (tmpDistances[busIndex - 1] < tmpDistances[busIndex + 1]) {
                    indexOfNextPoint = busIndex;
                }
                for (int i = 0; i <= combinedStops.size() - 1; i++) {
                    if (s.stop_name.equals(combinedStops.get(i).shape_id)) {
                        stopIndex = i;
                    }
                }

                if (indexOfNextPoint <= stopIndex) {
//                    currentMapActivity.runBusThread(latitude,longitude,tmpStop,this);
                    runBusThread();
                } else if (indexOfNextPoint > stopIndex) {
                    duration = null;
                    setDuration(duration);
                }
        }
//    }
//		return estimatedArrivalTime;
    }

    public void setDuration(String dura) {
        duration = dura;
    }

    public ArrayList<MyPoint> combineRouteWithShape (Stop[] stops, Shape routeShape) {
        ArrayList<MyPoint> out = new ArrayList<MyPoint>();
        for (int i = 0; i <= routeShape.points.size()-1; i++) {
            out.add(routeShape.points.get(i));
        }
        for (Stop s : stops) {
            out = recurseList(out,s);
        }
        return out;
    }

    public ArrayList<MyPoint> recurseList (ArrayList<MyPoint> in, Stop s) {
        double[] tmpdist = new double[in.size()];
        for (int i = 0; i < in.size(); i++) {
            MyPoint tmppoint = in.get(i);
            tmpdist[i] = Math.sqrt(Math.pow(s.stop_lat - tmppoint.shape_pt_lat, 2) + Math.pow(s.stop_lon - tmppoint.shape_pt_lon, 2));
        }
        int index = minIndex(tmpdist);
        if (index == 0) {
            in.add(1, new MyPoint(s.stop_name, s.stop_lat, s.stop_lon, 0));
        } else if (index == in.size()-1) {
            in.add(in.size()-2, new MyPoint(s.stop_name, s.stop_lat, s.stop_lon, 0));
        } else if (tmpdist[index - 1] > tmpdist[index + 1]) {
            in.add(index,new MyPoint(s.stop_name, s.stop_lat, s.stop_lon, 0));
        } else if (tmpdist[index - 1] < tmpdist[index + 1]) {
            in.add(index-1,new MyPoint(s.stop_name, s.stop_lat, s.stop_lon, 0));
        }
        return in;
    }

    public int minIndex (double[] a) {
        double tmp1 = 0;
        double tmp2 = 1000000000;
        int out = 0;
        for (int i = 0; i <= a.length-1; i++) {
            tmp1 = a[i];
            if (tmp1 < tmp2) {
                tmp2 = tmp1;
                out = i;
            }
        }
        return out;
    }

    final public void onResume() {
        synchronized (lock1) {
            paused = false;
            lock1.notifyAll();
        }
    }

    public void setCurrentMaps(MapsActivity map) {
        currentMapActivity = map;
    }
}
