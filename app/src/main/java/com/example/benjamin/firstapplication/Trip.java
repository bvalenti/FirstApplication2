/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.benjamin.firstapplication;

import java.util.ArrayList;

/**
 *
 * @author bill
 */
public class Trip {
    
    String route_id, service_id, trip_id, trip_headsign, shape_id;
    int direction_id;
    
    ArrayList<String> route;
    
    public Trip (String r_id, String se_id, String t_id, String t_hs,
            int d_id, String sh_id) {
        route_id = r_id;
        service_id = se_id;
        trip_id = t_id;
        trip_headsign = t_hs;
        direction_id = d_id;
        shape_id = sh_id;
        route = new ArrayList();
    }
    
}
