package com.example.benjamin.firstapplication;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

/**
 * Created by Benjamin on 3/30/2017.
 */

public class MyOnItemSelectedListener implements OnItemSelectedListener {

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String busID = parent.getItemAtPosition(pos).toString();
//        Toast.makeText(parent.getContext(),busID, Toast.LENGTH_SHORT).show();



//        Toast.makeText(parent.getContext(),
//                "OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
//                Toast.LENGTH_SHORT).show();
    }


    public void onNothingSelected(AdapterView<?> arg0) {
    }

}