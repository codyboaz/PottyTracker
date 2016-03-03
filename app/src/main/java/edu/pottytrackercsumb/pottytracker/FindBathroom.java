package edu.pottytrackercsumb.pottytracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class FindBathroom extends AppCompatActivity {

    static final LatLng csumb = new LatLng(36.650945, -121.790773);
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_bathroom);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        Marker CalSt = map.addMarker(new MarkerOptions().position(csumb).title("CSUMB").snippet("CSUMB Campus"));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(csumb, 15));

        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);


    }

}
