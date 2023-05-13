package com.pup.pupsecurity.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pup.pupsecurity.R;
import com.pup.pupsecurity.utils.Config;
import com.pup.pupsecurity.utils.PreferenceManager;

import static com.pup.pupsecurity.utils.Config.KEY_SOS_ID;
import static com.pup.pupsecurity.utils.Config.TAG_SERVICE_USER;

public class UserLocationService extends Service implements LocationListener {

    private LatLng          currentLocation;
    private LocationManager locationManager;

    private DatabaseReference drLocation;

    private static final int T_MINUTES = 1000 * 10;

    private boolean isCompleted = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }//onBind

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Notification notification = new Notification.Builder(getApplicationContext(), Config.NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("Punjabi University SOS Request")
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.drawable.asset_map)
                    .setChannelId(createNotificationChannel())
                    .setOngoing(true)
                    .build();
            startForeground(7898, notification);
        }
        FirebaseApp.initializeApp(getApplicationContext());
    }//onCreate

    /**
     * Used to create Notification for the Foreground service
     * @return channel id of the newly created notification
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        NotificationChannel chan = new NotificationChannel("UserLocation", "User SOS Request", NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor( Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert service != null;
        service.createNotificationChannel(chan);
        return "UserLocation";
    }//createNotificationChannel

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initView();
        getLocation();
        return START_STICKY;
    }//onStartCommand

    /**
     * Method to Initialize objects
     */
    private void initView() {
        drLocation = FirebaseDatabase.getInstance().getReference();
        drLocation.child("SOS").child(String.valueOf(PreferenceManager.getInt(getBaseContext(), KEY_SOS_ID))).child("completed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot != null)
                        isCompleted = (dataSnapshot.getValue(Integer.class) == 1);
                } catch (Exception e) {
                    Log.e(Config.TAG_USER, "Error unable to fecth new SOS complete status. Nothing too serious");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }//initView

    /**
     * used to initialize the location update request from the location manager
     */
    private void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        assert locationManager != null;
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,  10 * 1000, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10 * 1000, 0, this);
    }//getLocation

    /**
     * Update info of location of guard after 2 minute interval
     * @param location new location of the device
     */
    @Override
    public void onLocationChanged(Location location)  {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        Location gps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location net = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Log.d(TAG_SERVICE_USER, "Location : changed");
        Location bestLocation;
        bestLocation = isBetterLocation(gps, net);
        bestLocation = isBetterLocation(bestLocation, location);
        if (bestLocation != null) {
            uploadLocationOverServer(bestLocation);
        }
    }//onLocationChanged

    /**
     * Used to send location info to server
     * @param location the current best location
     */
    private void uploadLocationOverServer(Location location) {
        LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (isCompleted) {
            stopService(new Intent(getBaseContext(), UserLocationService.class));
            return;
        }
        Log.d(TAG_SERVICE_USER, "Location : " + location);
        if (!newLocation.equals(currentLocation)) {
            currentLocation = newLocation;
            String sosID = String.valueOf(PreferenceManager.getInt(getBaseContext(), KEY_SOS_ID));
            drLocation.child("SOS").child(sosID).child("latitude").setValue(newLocation.latitude);
            drLocation.child("SOS").child(sosID).child("longitude").setValue(newLocation.longitude);
            Log.d(TAG_SERVICE_USER, "Location : " + location.getLongitude() + " : " + location.getLatitude());
        }
        Log.d(TAG_SERVICE_USER, "Location : Same LOC");
    }//uploadLocationOverServer

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }//onStatusChanged

    @Override
    public void onProviderEnabled(String provider) {

    }//onProviderEnabled

    @Override
    public void onProviderDisabled(String provider) {

    }//onProviderDisabled

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG_SERVICE_USER, "Service Destroyed");
    }//onDestroy
}
