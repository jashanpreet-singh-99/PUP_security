package com.pup.pupsecurity.activities;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pup.pupsecurity.R;

import java.util.Objects;

public class SOSMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap   map;
    private ImageButton callSOSBtn;

    private String sosContact;
    private String sosName;
    private LatLng sosLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_map);
        getIntentData();
        initView();
    }//onCreate

    /**
     * Get Extra Data with the Intent
     */
    private void getIntentData(){
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;

        sosName     = bundle.getString("name");
        sosContact  = bundle.getString("contact");
        sosLocation = new LatLng(Float.parseFloat(Objects.requireNonNull(bundle.getString("lat"))), Float.parseFloat(Objects.requireNonNull(bundle.getString("long"))));
    }//getIntentData

    /**
     * Method to Initialize objects
     */
    private void initView() {
        callSOSBtn = this.findViewById(R.id.call_sos_btn);

        initMap();
        onClick();
    }//initView

    /**
     * Initialize the Map
     */
    private void initMap() {
        if (map == null) {
            SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            assert supportMapFragment != null;
            supportMapFragment.getMapAsync(this);
        }
    }//initMap

    /**
     * Method for Listeners
     */
    private void onClick() {
        callSOSBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + sosContact));
                startActivity(intent);
            }
        });
    }//onClick

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.addMarker(new MarkerOptions().position(sosLocation).title(sosName));
        map.moveCamera(CameraUpdateFactory.newLatLng(sosLocation));
        map.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
    }//onMapReady
}
