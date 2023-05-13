package com.pup.pupsecurity.activities;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pup.pupsecurity.R;
import com.pup.pupsecurity.utils.Config;
import com.pup.pupsecurity.utils.PreferenceManager;

import java.util.Objects;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.pup.pupsecurity.utils.Config.KEY_ADMIN_SUBSCRIBE;
import static com.pup.pupsecurity.utils.Config.KEY_REGISTERED;
import static com.pup.pupsecurity.utils.Config.TAG_REGISTER;

public class SplashScreen extends Activity {

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initView();
    }//onCreate

    /**
     * Initialize Values to Object and Views
     */
    private void initView() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        if (!checkPermission()) {
            requestPermission();
        } else {
            selectCorrectActivity();
        }
    }//initView

    /**
     * Method select which next activity to call based on your account
     * if new user RegistrationScreen Activity
     * if Admin    HomeScreenAdmin    Activity
     * if User     HomeScreenUser     Activity
     * if Guard    HomeScreenGuard    Activity
     */
    private void selectCorrectActivity() {
        Log.d(Config.TAG_SPLASH, "Registered User ?" + PreferenceManager.getBoolean(getApplicationContext(), KEY_REGISTERED));
        if ( user == null || !PreferenceManager.getBoolean(getApplicationContext(), KEY_REGISTERED)) {
            startActivity(new Intent(getApplicationContext(), RegistrationScreen.class));
            Log.d(Config.TAG_SPLASH, "Opening Register Page");
            finish();
        } else {
            DatabaseReference drRole = FirebaseDatabase.getInstance().getReference().child("Info").child(Objects.requireNonNull(user.getPhoneNumber()));
            drRole.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String value = dataSnapshot.child("role").getValue(String.class);
                    assert value != null;
                    PreferenceManager.setString(getApplicationContext(), Config.KEY_ROLE, value);
                    if (value.equals("admin")) {
                        startActivity(new Intent(getApplicationContext(), HomeScreenAdmin.class));
                        Log.d(Config.TAG_SPLASH, "Opening Admin Home Page");
                        if (!PreferenceManager.getBoolean(getApplicationContext(), KEY_ADMIN_SUBSCRIBE)) {
                            FirebaseMessaging.getInstance().subscribeToTopic(Config.FCM_TOPIC).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        PreferenceManager.setBoolean(getApplicationContext(), KEY_ADMIN_SUBSCRIBE, true);
                                    } else {
                                        PreferenceManager.setBoolean(getApplicationContext(), KEY_ADMIN_SUBSCRIBE, false);
                                    }
                                }
                            });
                        }
                        finish();
                    } else if (value.equals("client")) {
                        startActivity(new Intent(getApplicationContext(), HomeScreenUser.class));
                        Log.d(Config.TAG_SPLASH, "Opening Client Home Page");
                        finish();
                    } else {
                        int isActive = 0;
                        try {
                            isActive = dataSnapshot.child("active").getValue(Integer.class);
                        } catch (Exception e) {
                            Log.e(TAG_REGISTER, "ERROR new User " + e);
                        }
                        Log.d(Config.TAG_SPLASH, "Opening " + isActive);
                        if (isActive == 1) {
                            startActivity(new Intent(getApplicationContext(), HomeScreenGuard.class));
                            Log.d(Config.TAG_SPLASH, "Opening Guard Login Page");
                            finish();
                        } else {
                            startActivity(new Intent(getApplicationContext(), HomeScreenGuardLogin.class));
                            Log.d(Config.TAG_SPLASH, "Opening Guard Login Page");
                            finish();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    new AlertDialog.Builder(SplashScreen.this)
                            .setTitle("Data Fetch Failed")
                            .setMessage("Unable to Fetch data. Check Internet Connection and try again.")
                            .setCancelable(true).show();
                }
            });
        }
    }//selectCorrectActivity

    /**
     * Method check runtime PERMISSIONS required by the app.
     * @return true if granted permission
     */
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }//checkPermission

    /**
     * Method to request PERMISSIONS
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE}, Config.PERMISSION_REQUEST_CODE);
    }//requestPermission

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Config.PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (locationAccepted && storageAccepted) {
                    selectCorrectActivity();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                            requestPermissions(new String[]{ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE}, Config.PERMISSION_REQUEST_CODE);
                        }
                    }
                }
            }
        }
    }//onRequestPermissionsResult
}
