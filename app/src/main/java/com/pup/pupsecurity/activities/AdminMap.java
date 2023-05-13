package com.pup.pupsecurity.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pup.pupsecurity.R;
import com.pup.pupsecurity.model.InfoDataModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.pup.pupsecurity.utils.Config.TAG_ADMIN;

public class AdminMap extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private ImageButton callBtn;

    private DatabaseReference drRole;

    private GoogleMap map;

    private ArrayList<InfoDataModel> guardList   = new ArrayList<>();
    private ArrayList<Marker>        guardMarker = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_map);
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, 23);
        initView();
    }//onCreate

    /**
     * Initialize the objects
     */
    private void initView() {
        callBtn = this.findViewById(R.id.call_sos_btn);

        drRole = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        Log.d(TAG_ADMIN, "Number : " + user.getPhoneNumber());
        initMap();
        onClick();
    }//initView

    /**
     * Initialize the Listener for the objects
     */
    private void onClick() {
        callBtn.setVisibility(View.GONE);
    }//onClick

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
     * Check for permissions
     * @param permission requested permission
     * @param requestCode request code for the permission
     * @return granted or not
     */
    public boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(AdminMap.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(AdminMap.this, new String[] { permission }, requestCode);
            return false;
        }
        else {
            Toast.makeText(AdminMap.this, "Permission already granted", Toast.LENGTH_SHORT).show();
            return true;
        }
    }//checkPermission

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 23) {
             for (int i = 0,len = permissions.length; i < len; i++) {
                 String permission = permissions[i];
                 if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                     boolean perm = checkPermission(permission, requestCode);
                     if (! perm) {
                         Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                         Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                         intent.setData(uri);
                         startActivityForResult(intent, 23);
                     } else if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)) {
                         checkPermission(permission, requestCode);
                     }
                 }
             }
        }
    }//onRequestPermissionsResult

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.359754f,76.449062f), 18.0f));
        getGuardLocationData();
    }//onMapReady

    /**
     * place the marker for the guards on the map.
     */
    private void postGuardMapMarkers() {
        for(Marker m : guardMarker) {
            m.remove();
        }
        for (int i = 0 ; i < guardList.size() ; i++) {
            InfoDataModel guard = guardList.get(i);
            Marker m = map.addMarker(new MarkerOptions()
                    .position(new LatLng(Float.parseFloat(guard.getLatitude()), Float.parseFloat(guard.getLongitude())))
                    .snippet("guard")
                    .title(guard.getImage())
                    .icon(BitmapDescriptorFactory.defaultMarker((BitmapDescriptorFactory.HUE_ORANGE)))
            );
            m.setTag(i);
            guardMarker.add(m);
        }
    }//postGuardMapMarkers

    /**
     * Get Data related to location of the guards
     */
    private void getGuardLocationData() {
        drRole.child("GuardLocation").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                guardList.clear();
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());
                    try {
                        Date date = sdf.parse((String) Objects.requireNonNull(data.child("time").getValue()));
                        assert date != null;
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.MINUTE, -10);
                        if (calendar.getTime().before(date)) {
                            guardList.add(new InfoDataModel(
                                    data.child("latitude").getValue() + "",
                                    data.child("longitude").getValue() + "",
                                    data.getKey(),
                                    "none",
                                    (String) data.child("time").getValue()
                            ));
                            Log.d(TAG_ADMIN, "Guard Active " + data.getKey());
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    postGuardMapMarkers();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG_ADMIN, "Unable to Fetch Guard List");
            }
        });
    }//getGuardLocationData

    @Override
    public boolean onMarkerClick(Marker marker) {
        int pos = (int) marker.getTag();
        if (pos < 0) {
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + guardList.get(pos).getContact()));
        startActivity(intent);
        return false;
    }//onMarkerClick
}
