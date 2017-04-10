package com.example.benjamin.firstapplication;

public class Stop {
	
    int location_type;
    String stop_id, stop_name;
    double stop_lat, stop_lon;
    
    public Stop (String s_id, String s_n, double s_lat, double s_lon,
            int loc_t) {
        stop_id = s_id;
        stop_name = s_n;
        stop_lat = s_lat;
        stop_lon = s_lon;
        location_type = loc_t;
    }
    
}
