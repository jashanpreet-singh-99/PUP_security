package com.pup.pupsecurity.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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
import com.pup.pupsecurity.service.UserLocationService;
import com.pup.pupsecurity.utils.Config;
import com.pup.pupsecurity.utils.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.pup.pupsecurity.utils.Config.TAG_GUARD;
import static com.pup.pupsecurity.utils.Config.TAG_USER;

public class HomeScreenUser extends FragmentActivity implements LocationListener,OnMapReadyCallback {

    private ImageButton     sendSOSBtn;
    private RelativeLayout  loadingLayout;
    private LocationManager locationManager;
    private Marker          currentLocationMarker ;
    private GoogleMap       map;
    private EditText        extraMessage;

    private RelativeLayout  sosBtn1;
    private RelativeLayout  sosBtn2;
    private RelativeLayout  sosBtn3;
    private RelativeLayout  sosBtn4;
    private RelativeLayout  sosBtn5;

    private TextView sosTxt1;
    private TextView sosTxt2;
    private TextView sosTxt3;
    private TextView sosTxt4;
    private TextView sosTxt5;

    private RelativeLayout sosList;
    private RelativeLayout sosControl;

    private int sosType = 0;

    private ArrayList<RelativeLayout> sosBtnArray = new ArrayList<>();
    private ArrayList<TextView> sosTxtArray = new ArrayList<>();

    private DatabaseReference  drRole;
    private FirebaseUser       user;

    private boolean sosMode = false;

    private static final int T_MINUTES = 1000 * 30;

    private int sosCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen_user);
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, 23);
        initView();
    }//onCreate

    @Override
    protected void onStart() {
        super.onStart();
        sosList.setVisibility(View.VISIBLE);
        sosControl.setVisibility(View.VISIBLE);
    }

    /**
     * Method to Initialize objects
     */
    private void initView() {
        sendSOSBtn    = this.findViewById(R.id.send_sos_user);
        loadingLayout = this.findViewById(R.id.loading_screen);

        sosBtn1  = this.findViewById(R.id.sos_btn_1);
        sosBtn2  = this.findViewById(R.id.sos_btn_2);
        sosBtn3  = this.findViewById(R.id.sos_btn_3);
        sosBtn4  = this.findViewById(R.id.sos_btn_4);
        sosBtn5  = this.findViewById(R.id.sos_btn_5);

        sosTxt1  = this.findViewById(R.id.sos_txt_1);
        sosTxt2  = this.findViewById(R.id.sos_txt_2);
        sosTxt3  = this.findViewById(R.id.sos_txt_3);
        sosTxt4  = this.findViewById(R.id.sos_txt_4);
        sosTxt5  = this.findViewById(R.id.sos_txt_5);

        sosList     = this.findViewById(R.id.sos_list);
        sosControl  = this.findViewById(R.id.sos_controls);

        extraMessage = this.findViewById(R.id.extra_message_sos_user);

        sosBtnArray.add(sosBtn1);
        sosBtnArray.add(sosBtn2);
        sosBtnArray.add(sosBtn3);
        sosBtnArray.add(sosBtn4);
        sosBtnArray.add(sosBtn5);

        sosTxtArray.add(sosTxt1);
        sosTxtArray.add(sosTxt2);
        sosTxtArray.add(sosTxt3);
        sosTxtArray.add(sosTxt4);
        sosTxtArray.add(sosTxt5);

        drRole           = FirebaseDatabase.getInstance().getReference();
        user             = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        Log.d(TAG_USER, "Number : " + user.getPhoneNumber());
        initMap();
        fetchSosCount();
        onClick();
    }//initView

    private void fetchSosCount() {
        drRole.child("sosCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sosCount = dataSnapshot.getValue(Integer.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                new AlertDialog.Builder(HomeScreenUser.this)
                        .setTitle(" Data Error")
                        .setMessage("Unable to fetch data from the server. Recheck your internet connection.")
                        .setCancelable(true).show();
            }
        });
    }

    /**
     * Method for Listeners
     */
    private void onClick() {
        sendSOSBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sosType > 0) {
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    assert vibrator != null;
                    vibrator.vibrate(1000);
                    sosMode = true;
                    getLocation();
                    Log.d(TAG_GUARD, "Number : " + user.getPhoneNumber());
                    PreferenceManager.setInt(getBaseContext(), Config.KEY_SOS_ID, sosCount);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        getApplicationContext().startForegroundService(new Intent(getApplicationContext(), UserLocationService.class));
                    } else {
                        getApplicationContext().startService(new Intent(getApplicationContext(), UserLocationService.class));
                    }
                    sosList.setVisibility(View.GONE);
                    sosControl.setVisibility(View.GONE);
                } else {
                    new AlertDialog.Builder(HomeScreenUser.this)
                            .setTitle("Select SOS Type")
                            .setMessage("Select either of the given SOS so as to receive appropriate help.")
                            .setCancelable(true).show();
                }
            }
        });

        sosBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sosType > 0) {
                    sosBtnArray.get(sosType-1).setBackgroundTintList(null);
                    sosTxtArray.get(sosType-1).setTextColor(getResources().getColor(R.color.colorSecondaryText));
                }
                sosBtn1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                sosTxt1.setTextColor(Color.WHITE);
                sosType = 1;
            }
        });

        sosBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sosType > 0) {
                    sosBtnArray.get(sosType-1).setBackgroundTintList(null);
                    sosTxtArray.get(sosType-1).setTextColor(getResources().getColor(R.color.colorSecondaryText));
                }
                sosBtn2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                sosTxt2.setTextColor(Color.WHITE);
                sosType = 2;
            }
        });

        sosBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sosType > 0) {
                    sosBtnArray.get(sosType-1).setBackgroundTintList(null);
                    sosTxtArray.get(sosType-1).setTextColor(getResources().getColor(R.color.colorSecondaryText));
                }
                sosBtn3.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                sosTxt3.setTextColor(Color.WHITE);
                sosType = 3;
            }
        });

        sosBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sosType > 0) {
                    sosBtnArray.get(sosType-1).setBackgroundTintList(null);
                    sosTxtArray.get(sosType-1).setTextColor(getResources().getColor(R.color.colorSecondaryText));
                }
                sosBtn4.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                sosTxt4.setTextColor(Color.WHITE);
                sosType = 4;
            }
        });

        sosBtn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sosType > 0) {
                    sosBtnArray.get(sosType-1).setBackgroundTintList(null);
                    sosTxtArray.get(sosType-1).setTextColor(getResources().getColor(R.color.colorSecondaryText));
                }
                sosBtn5.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                sosTxt5.setTextColor(Color.WHITE);
                sosType = 5;
            }
        });

    }//onClick

    /**
     * Method to Initialize map
     */
    private void initMap() {
        if (map == null) {
            SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            assert supportMapFragment != null;
            supportMapFragment.getMapAsync(this);
        }
    }//initMap

    /**
     * Initialize the location manager and the update request for location.
     */
    private void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        assert locationManager != null;
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, T_MINUTES, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, T_MINUTES, 0, this);

    }//getLocation

    /**
     * check permission if not allowed till now
     * @param permission  permission to check or allow
     * @param requestCode request code for the permission
     * @return permission allowed or not granted
     */
    public boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(HomeScreenUser.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(HomeScreenUser.this, new String[] { permission }, requestCode);
            return false;
        }
        else {
            Toast.makeText(HomeScreenUser.this, "Permission already granted", Toast.LENGTH_SHORT).show();
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
    public void onLocationChanged(Location location) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        Location gps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location net = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location bestLocation;
        bestLocation = isBetterLocation(gps, net);
        bestLocation = isBetterLocation(bestLocation, location);
        if (bestLocation != null) {
            uploadLocationOverServer(bestLocation);
        }
        locationManager.removeUpdates(this);
    }//onLocationChanged

    private void uploadLocationOverServer(Location location) {
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        Log.d(TAG_USER, "Location : " + location.getLongitude() + " : " + location.getLatitude());
        if (sosMode) {
            drRole.child("SOS").child(String.valueOf(sosCount)).child("contact").setValue(user.getPhoneNumber());
            drRole.child("SOS").child(String.valueOf(sosCount)).child("longitude").setValue(location.getLongitude());
            drRole.child("SOS").child(String.valueOf(sosCount)).child("latitude").setValue(location.getLatitude());
            Date time = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());
            drRole.child("SOS").child(String.valueOf(sosCount)).child("time").setValue(sdf.format(time));
            Log.d(TAG_USER, "SOS Mode.");
            drRole.child("SOS").child(String.valueOf(sosCount)).child("type").setValue(sosType);
            drRole.child("SOS").child(String.valueOf(sosCount)).child("extra").setValue(extraMessage.getText().toString());
            drRole.child("SOS").child(String.valueOf(sosCount)).child("completed").setValue(0);
            drRole.child("sosCount").setValue(sosCount + 1);
            sendNotificationFCM(user.getPhoneNumber());
        }
        if (currentLocationMarker != null){
            currentLocationMarker.remove();
        }
        currentLocationMarker = map.addMarker(new MarkerOptions().position(currentLocation).title("Current").snippet(user.getPhoneNumber()).icon(BitmapDescriptorFactory.defaultMarker((BitmapDescriptorFactory.HUE_CYAN))));
        currentLocationMarker.setTag(-1);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17.0f));
        if (loadingLayout.getVisibility() == View.VISIBLE) {
            loadingLayout.setVisibility(View.GONE);
        }
    }

        /**
         * send notification using json data object using volley.
         * @param phoneNum contact of the user who need sos request.
         */
    private void sendNotificationFCM(String phoneNum) {
        RequestQueue mRequestQue = Volley.newRequestQueue(this);

        JSONObject json = new JSONObject();
        try {
            json.put("to", "/topics/" + Config.FCM_TOPIC);
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", "SOS Alert");
            notificationObj.put("body", "SOS Request By " + phoneNum + ". Check App for Location information of the SOS Request.");
            json.put("notification", notificationObj);

            String URL = "https://fcm.googleapis.com/fcm/send";

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG_USER, "Notification Sent to FCM");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG_USER, "Unable to send Notification");
                }
            })
             {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", Config.FCM_KEY);
                    return header;
                }
            };
            mRequestQue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }//sendNotificationFCM

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        getLocation();
    }//onMapReady

    /**
     * Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected Location isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return location;
        }
        if (location == null) {
            return currentBestLocation;
        }

        long     timeDelta           = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > T_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -T_MINUTES;
        boolean isNewer              = timeDelta > 0;

        if (isSignificantlyNewer) {
            return location;
        } else if (isSignificantlyOlder) {
            return currentBestLocation;
        }

        int     accuracyDelta               = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate              = accuracyDelta > 0;
        boolean isMoreAccurate              = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        if (isMoreAccurate) {
            return location;
        } else if (isNewer && !isLessAccurate) {
            return location;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return location;
        }
        return currentBestLocation;
    }//isBetterLocation

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }//isSameProvider


}
