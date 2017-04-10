/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.benjamin.firstapplication;

import android.graphics.Point;
import java.util.ArrayList;

/**
 *
 * @author bill
 */
public class Shape {
    
    public String shape_id;
    public ArrayList<MyPoint> points;
    
    public Shape (String s_id){
        shape_id = s_id;
        points = new ArrayList();
    }
    
}
