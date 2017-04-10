/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.benjamin.firstapplication;

/**
 *
 * @author bill
 */
public class MyPoint {
    
    public String shape_id;
    public double shape_pt_lat, shape_pt_lon;
    public int shape_pt_sequence;
    
    public MyPoint(String s_id, double s_lat, double s_lon, int s_s){
        shape_id = s_id;
        shape_pt_lat = s_lat;
        shape_pt_lon = s_lon;
        shape_pt_sequence = s_s;
    }
    
}
