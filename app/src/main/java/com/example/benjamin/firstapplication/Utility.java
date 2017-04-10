/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.benjamin.firstapplication;

//import android.util.JsonReader;

//import org.json.JSONArray;
//import org.json.JSONObject;

import android.icu.text.RelativeDateTimeFormatter;
import android.icu.text.SimpleDateFormat;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.HashMap;
import javax.net.ssl.HttpsURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
//import java.time.ZonedDateTime;
//import java.time.format.*;
//import java.util.ArrayList;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import javax.json.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public final class Utility {

    private static Timer apiPoller;
    private static TimerTask apiPollerTask;
    public static int updateModelExecuteCount;

    private Utility() {
    }
    
    //parse shapes file into HashMap<shape_id, Shape>
//    public static HashMap parseShapes (String fn) throws FileNotFoundException {
//        HashMap<String, Shape> shapes = new HashMap();
//        Shape s = null;
//        File f = new File(fn);
//        Scanner fs = new Scanner(f);
//        String buffer = fs.nextLine();
//
//        while (fs.hasNextLine()) {
//            buffer = fs.nextLine();
//            String buffer_s[] = buffer.split(",");
//            if (s != null && !buffer_s[0].equals(s.shape_id))
//                shapes.put(s.shape_id, s);
//            if (s == null || !s.shape_id.equals(buffer_s[0]))
//                s = new Shape(buffer_s[0]);
//            MyPoint p = new MyPoint(buffer_s[0], Double.parseDouble(buffer_s[1]),
//                Double.parseDouble(buffer_s[2]), Integer.parseInt(buffer_s[3]));
//            s.points.add(p);
//            if (!fs.hasNextLine())
//                shapes.put(s.shape_id, s);
//        }
//        return shapes;
//    }

    // associate correct trip information with bus objects, return updated 
    // HashMap of Busses
    public static HashMap assignTrips(HashMap<String, Bus> busses,
            HashMap<String, Trip> trips, HashMap<String, Stop> stops,
            HashMap<String, Shape> shapes) {
        for (Bus b : busses.values()) {
            for (Trip t : trips.values()) {
                if (!b.id.contains("SBS")) {
                    // Regular bus
                    if (t.route_id.equals(b.id)
                            && t.direction_id == b.direction) {
                        Stop route[] = new Stop[t.route.size()];
                        for (int i = 0; i < route.length; i++) {
                            route[i] = stops.get(t.route.get(i));
                        }
                        b.busRoute = route;
                        b.route_shape = shapes.get(t.shape_id);
                    }
                } else {
                    // Select Bus Service
                    if (t.trip_headsign.equals(b.destinationName)
                            && t.direction_id == b.direction) {
                        Stop route[] = new Stop[t.route.size()];
                        for (int i = 0; i < route.length; i++) {
                            route[i] = stops.get(t.route.get(i));
                        }
                        b.busRoute = route;
                        b.route_shape = shapes.get(t.shape_id);
                    }
                }
            }
        }

        return busses;
    }

    // parses trips in txt file and returns hashmap of <trip_id, Trip>
//    public static HashMap parseTrips(String fn) throws FileNotFoundException {
//        HashMap<String, Trip> hm = new HashMap();
//        Trip t = null;
//        File f = new File(fn);
//        Scanner fs = new Scanner(f);
//        String buffer = fs.nextLine();
//
//        while (fs.hasNextLine()) {
//            buffer = fs.nextLine();
//            String buffer_s[] = buffer.split(",");
//            t = new Trip(buffer_s[0], buffer_s[1], buffer_s[2], buffer_s[3],
//                    Integer.parseInt(buffer_s[4]), buffer_s[5]);
//            hm.put(t.trip_id, t);
//        }
//
//        return hm;
//    }

    //Edited to take InputStream
    public static HashMap parseTrips(InputStream IS) throws FileNotFoundException {
        HashMap<String, Trip> hm = new HashMap();
        Trip t = null;
        String buffer;
        BufferedReader reader = new BufferedReader(new InputStreamReader(IS));
        try {
            buffer = reader.readLine();
            while ((buffer = reader.readLine()) != null ) {
                String buffer_s[] = buffer.split(",");
                t = new Trip(buffer_s[0], buffer_s[1], buffer_s[2], buffer_s[3],
                        Integer.parseInt(buffer_s[4]), buffer_s[5]);
                hm.put(t.trip_id, t);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hm;
    }


    // parses Stops from txt file and returns HashMap<stop_id, Stop>
    public static HashMap parseStops(String fn) throws FileNotFoundException {
        HashMap<String, Stop> hm = new HashMap();
        Stop s = null;
        File f = new File(fn);
        Scanner fs = new Scanner(f);
        String buffer = fs.nextLine();

        while (fs.hasNextLine()) {
            buffer = fs.nextLine();
            String buffer_s[] = buffer.split(",");
            s = new Stop(buffer_s[0], buffer_s[1],
                    Double.parseDouble(buffer_s[3]),
                    Double.parseDouble(buffer_s[4]),
                    Integer.parseInt(buffer_s[7]));
            hm.put(s.stop_id, s);
        }

        return hm;
    }

    // parses Stop Times from txt file and returns HashMap<trip_id, Trip>
    public static HashMap parseStopTimes(String fn,
            HashMap<String, Trip> trips) throws FileNotFoundException {
        File f = new File(fn);
        Scanner fs = new Scanner(f);
        String buffer = fs.nextLine();

        while (fs.hasNextLine()) {
            buffer = fs.nextLine();
            String buffer_s[] = buffer.split(",");
            String trip_id = buffer_s[0];
            String stop_id = buffer_s[3];
            trips.get(trip_id).route.add(stop_id);
        }

        return trips;
    }

    // opens connection to MTA api
    public static HttpURLConnection openMTAApiConnection() throws MalformedURLException, IOException {
        //URL urlToGet = new URL("https://bustime.mta.info/api/siri/vehicle-monitoring.json?key=7a22c3e8-61a7-40ff-9d54-714e36f56880");
        URL urlToGet = new URL("http://api.prod.obanyc.com/api/siri/vehicle-monitoring.json?key=7a22c3e8-61a7-40ff-9d54-714e36f56880");

        return (HttpURLConnection) urlToGet.openConnection();
    }

    // opens connection to Google Geocoding api
    public static HttpsURLConnection openGoogleApiConnection(String address)
            throws MalformedURLException, IOException {
        address = address.replace(" ", "%20");
        URL urlToGet = new URL("https://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyDVO746CwOhnxOo6KQOrEL1L6as-Ag_sKw&address=" + address);
        System.out.println("URL = " + urlToGet.toString());
        return (HttpsURLConnection) urlToGet.openConnection();
    }

    // downloads File and returns File Name
    public static String getFile(URLConnection conn, String saveLocation,
            String fileName, boolean https) throws MalformedURLException, IOException {
        int responseCode;
        if (https) {
            responseCode = ((HttpsURLConnection) conn).getResponseCode();
        } else {
            responseCode = ((HttpURLConnection) conn).getResponseCode();
        }
        if (responseCode == 200) {
            InputStream inputStream = conn.getInputStream();
            String saveFilePath = saveLocation + File.separator + fileName;
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
            int bytesRead = -1;
            byte[] buffer = new byte[1024];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
        }
        if (https) {
            ((HttpsURLConnection) conn).disconnect();
        } else {
            ((HttpURLConnection) conn).disconnect();
        }

        return fileName;
    }

    // begins apiPoller task executed regularly to update vehicle-data
//    public static void startupTasks() {
//        apiPoller = new Timer();
//        apiPollerTask = new UpdateModel();
//        apiPoller.schedule(apiPollerTask, 0, 70000);
//    }

    // stops the apiPoller task that is regularly executed
    public static void stopApiPoller() {
        apiPollerTask.cancel();
        apiPoller.cancel();
    }

    public static JsonObject createJsonObject(String fn) throws FileNotFoundException {
        File f = new File(fn);
        FileReader fr = new FileReader(f);
        JsonReader reader = Json.createReader(fr);
        return reader.readObject();
    }

    public static HashMap jsonParser(String fn) throws IOException, ParseException, org.json.simple.parser.ParseException {
        //String fn = Utility.getFile("http://bustime.mta.info/api/siri/vehicle-monitoring.json?key=7a22c3e8-61a7-40ff-9d54-714e36f56880", "C:/Users/dt817/OneDrive/Documents" , "jsonFile.json");
        HashMap<String, Bus> busses = new HashMap();
//        SimpleDateFormat formatter = SimpleDateFormat.ofPattern("dd MMM uu  hh:mm");
//        String buffer;
//        BufferedReader reader = new BufferedReader(new InputStreamReader(IS));
//        buffer = reader.readLine();

        JSONParser parser = new JSONParser();
        Object objfile = parser.parse(new FileReader(fn));//Add your file location before fn: Ex. "C://steve/"+fn

        JSONObject obj = (JSONObject) objfile;//START OBJECT

        JSONObject siri = (JSONObject) obj.get("Siri");//SIRI OBJECT
        JSONObject serviceDelivery = (JSONObject) siri.get("ServiceDelivery");//SERVICE DELIVERY OBJECT
        JSONArray vmd = (JSONArray) serviceDelivery.get("VehicleMonitoringDelivery");
        JSONObject vmdP = (JSONObject) vmd.get(0);//VEHICLE MONITORING DELIVERY OBJECT
        JSONArray va = (JSONArray) vmdP.get("VehicleActivity"); //VEHICLE ACTIVTY ARRAY

        for (int i = 0; i < va.size(); i++) {
            JSONObject mvj = (JSONObject) va.get(i);

            JSONObject mvjP = (JSONObject) mvj.get("MonitoredVehicleJourney");//MONITORED VEHICLE JOURNEY OBJECT

            JSONObject vl = (JSONObject) mvjP.get("VehicleLocation");//VEHICLE LOCATION OBJECT

            JSONObject mc = (JSONObject) mvjP.get("MonitoredCall");//MONITORED CALL OBJECT

            String busIDRoute = mvjP.get("PublishedLineName").toString();
            if (busIDRoute.charAt(0) == 'M') {
                int direction = Integer.parseInt(mvjP.get("DirectionRef").toString());
                String destinationName = mvjP.get("DestinationName").toString();
                float longitude = Float.parseFloat(vl.get("Longitude").toString());
                float latitude = Float.parseFloat(vl.get("Latitude").toString());
                String busID = mvjP.get("VehicleRef").toString();
                if (!mc.containsKey("ExpectedArrivalTime") || !mc.containsKey("ExpectedDepartureTime")) {
                    continue;
                }
//                String expectedArrivalTime = formatter.format(ZonedDateTime.parse(mc.get("ExpectedArrivalTime").toString()));
                // System.out.println(expectedArrivalTime);
//                String expectedDepartureTime = formatter.format(ZonedDateTime.parse(mc.get("ExpectedDepartureTime").toString()));
                //if(longitude != 0 && latitude != 0 && busID != null && destinationName != null && expectedArrivalTime != null && expectedDepartureTime != null && direction != 0){
                Bus newBus = new Bus(longitude, latitude, busIDRoute, destinationName, null, null, direction);
                busses.put(busID, newBus);
                // }

            }
        }

        return busses;

    }

}
