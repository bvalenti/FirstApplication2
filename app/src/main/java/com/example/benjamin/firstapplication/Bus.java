package com.example.benjamin.firstapplication;

//import java.time.LocalDateTime;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;

public class Bus {

    public float longitude, latitude, latitudeOfLastPolling, longitudeOfLastPolling;
    public String id, destinationName, origin, busID;
    public String expectedArrivalTime, expectedDepartureTime;
    public int direction;
    public Stop busRoute[];
    public Shape route_shape;

    public Bus(){};

    public Bus(float lo, float la, String i, String DN, String ex, String t, int d) {
        longitude = lo;
        latitude = la;
        id = i;
        destinationName = DN;
        // origin= O;
        expectedArrivalTime = ex;
        expectedDepartureTime = t;
        direction = d;
    }

    public String getExpectedArrivalTime(Stop s) {
		String estimatedArrivalTime = null;
		double distancesBetweenStops[] = new double[busRoute.length];
		int stopIndex = -1;
		int nextStopIndex = -1;
        LatLng points[];

		//Returns the MTA provided arrival time if s is the next stop.
		if (s.stop_id.compareTo(destinationName) == 1) {
			estimatedArrivalTime = expectedArrivalTime;

		//Case where s is not the next stop.
		} else if (s.stop_id.compareTo(destinationName) != 1) {

			//Find the index of stop, s, in the busRoute array.
			for (int j = 0; j < busRoute.length; j++) {
				if (s.stop_id.compareTo(busRoute[j].stop_id) == 0) {
					stopIndex = j;
					break;
				}
			}

			//Make sure the stop, s, is contained in the busRoute.
			if (stopIndex >= 0) {

//                busRoute
//                route_shape[];
//                longitude, latitude,
//                longitudeOfLastPolling, latitudeOfLastPolling
                ArrayList<MyPoint> combinedPositions = combineRouteWithShape(busRoute, route_shape);
//                combinedPositions

				//Converting latitude-longitude pairs to easting-northing pairs and find distances between stops
				//contained in busRoute.
				for (int i = 0; i < busRoute.length-1; i++) {
					LatLng latLngBusRoute2 = new LatLng(busRoute[i + 1].stop_lat, busRoute[i + 1].stop_lon);
					LatLng latLngBusRoute1 = new LatLng(busRoute[i].stop_lat, busRoute[i].stop_lon);
                    double tmp = Math.sqrt(Math.pow(latLngBusRoute2.latitude - latLngBusRoute1.latitude,2) +
                            Math.pow(latLngBusRoute2.longitude - latLngBusRoute1.longitude,2));
                    distancesBetweenStops[i] = tmp;
//					distancesBetweenStops[i] = latLngBusRoute2.distance(latLngBusRoute1);
				}

				//Find the index of the next bus stop in the array busRoute.
				for (int j = 0; j <= busRoute.length; j++) {
					if (destinationName.compareTo(busRoute[j].stop_id) == 0) {
						nextStopIndex = j;
						break;
					}
				}

				//Find distance between the bus and the next stop.
				LatLng latLngNextStop = new LatLng(busRoute[nextStopIndex].stop_lat, busRoute[nextStopIndex].stop_lon);
				LatLng latLngBus = new LatLng(latitude, longitude);
				LatLng latLngBusLastPolling = new LatLng(latitudeOfLastPolling, longitudeOfLastPolling);

                double distanceFromBusToFirstStop = Math.sqrt(Math.pow(latLngBus.latitude - latLngNextStop.latitude,2) +
                        Math.pow(latLngBus.longitude - latLngNextStop.longitude,2));

//				double distanceFromBusToFirstStop = latLngBus.distance(latLngNextStop);

				//The bus speed in meters/minute.
                double averageBusVelocity = Math.sqrt(Math.pow(latLngBus.latitude - latLngBusLastPolling.latitude,2) +
                        Math.pow(latLngBus.longitude - latLngBusLastPolling.longitude,2));

//                double averageBusVelocity = latLngBus.distance(latLngBusLastPolling);

				//Sum distances along the bus route from the bus to stop, s.
				double totalDistanceToStop = distanceFromBusToFirstStop;
				for (int i = nextStopIndex; i <= stopIndex; i++) {
					totalDistanceToStop = totalDistanceToStop + distancesBetweenStops[i];
				}

				totalDistanceToStop = Math.round(totalDistanceToStop * 10.0)/10.0;
				averageBusVelocity = Math.round(averageBusVelocity * 10.0)/10.0;

				double timeToAdd = (double) Math.round(100 * totalDistanceToStop/averageBusVelocity)/100;
				String timeToAddString = Double.toString(timeToAdd);
				int timeToAddMinutes = Integer.parseInt(timeToAddString.split("\\.")[0]);
				int timeToAddSeconds = Integer.parseInt(timeToAddString.split("\\.")[1]);
				timeToAddSeconds = timeToAddSeconds * 60/100;

                Calendar c = Calendar.getInstance();
                int seconds = c.get(Calendar.SECOND) + timeToAddSeconds;
                int minutes = c.get(Calendar.MINUTE) + timeToAddMinutes;
                int hours = c.get(Calendar.HOUR);
//				estimatedArrivalTime = LocalTime.now().plusMinutes(timeToAddMinutes).plusSeconds(timeToAddSeconds);
                estimatedArrivalTime = Integer.toString(hours) + ":" + Integer.toString(minutes) + ":" + Integer.toString(seconds);
			}
		}
		return estimatedArrivalTime;
	}

    public ArrayList<MyPoint> combineRouteWithShape (Stop[] stops, Shape routeShape) {
        ArrayList<MyPoint> out = null;
//        ArrayList<LatLng> out = null;
        double[] tmpdist = null;

        for (int i = 0; i <= routeShape.points.size(); i++) {
            out.add(routeShape.points.get(i));
//            out.add(new MyPoint(routeShape.points.get(i).shape_pt_lat,routeShape.points.get(i).shape_pt_lon));
        }

        for (Stop s : stops) {
            for (int i = 0; i < routeShape.points.size(); i++) {
                MyPoint tmppoint = routeShape.points.get(i);
                tmpdist[i] = Math.sqrt(Math.pow(s.stop_lat - tmppoint.shape_pt_lat,2) + Math.pow(s.stop_lon - tmppoint.shape_pt_lon,2));
            }
            int index = minIndex(tmpdist);

            if (index == routeShape.points.size()) {
                if (tmpdist[index-1] >= tmpdist[0]) {
                    out.add(0,new MyPoint(s.stop_id,s.stop_lat,s.stop_lon,0));
                }  else {
                    out.add(new MyPoint(s.stop_id,s.stop_lat,s.stop_lon,0));
                }
            } else if (index == 0) {
                if (tmpdist[index+1] >= tmpdist[routeShape.points.size()]) {
                    out.add(0,new MyPoint(s.stop_id,s.stop_lat,s.stop_lon,0));
                } else {
                    out.add(new MyPoint(s.stop_id,s.stop_lat,s.stop_lon,0));
                }
                out.add(index-1,new MyPoint(s.stop_id,s.stop_lat,s.stop_lon,0));
            } else if (tmpdist[index-1] > tmpdist[index+1]) {
                out.add(index,new MyPoint(s.stop_id,s.stop_lat,s.stop_lon,0));
            } else if (tmpdist[index+1] > tmpdist[index-1]) {
                out.add(index-1,new MyPoint(s.stop_id,s.stop_lat,s.stop_lon,0));
            }
        }
        return out;
    }

    public int minIndex (double[] a) {
        double tmp1 = 0;
        double tmp2 = 1000000000;
        int out = 0;
        for (int i = 0; i <= a.length; i++) {
            tmp1 = a[i];
            if (tmp1 < tmp2) {
                tmp2 = tmp1;
                out = i;
            }
        }
        return out;
    }
}
