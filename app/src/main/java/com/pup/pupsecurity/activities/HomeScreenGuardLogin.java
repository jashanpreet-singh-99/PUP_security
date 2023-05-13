package com.pup.pupsecurity.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.pup.pupsecurity.R;
import com.pup.pupsecurity.service.GuardLocationService;
import com.pup.pupsecurity.utils.Config;
import com.pup.pupsecurity.utils.PreferenceManager;

import java.util.Objects;

import static com.pup.pupsecurity.utils.Config.TAG_GUARD;
import static com.pup.pupsecurity.utils.Config.TAG_SERVICE;

public class HomeScreenGuardLogin extends Activity {

    private EditText passwordGuard;
    private Button   loginBtn;

    private DatabaseReference drRole;
    private FirebaseUser      user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen_guard_login);
        initView();
    }

    private void initView() {
        passwordGuard = this.findViewById(R.id.guard_password_number);
        loginBtn      = this.findViewById(R.id.login_guard);

        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        drRole = FirebaseDatabase.getInstance().getReference();

        onClick();
    }

    private void onClick() {
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Config.TAG_GUARD, ("+91" + passwordGuard.getText().toString()));
                if (("+91" + passwordGuard.getText().toString()).equals(user.getPhoneNumber())) {
                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                new AlertDialog.Builder(HomeScreenGuardLogin.this)
                                        .setTitle("Incorrect Phone Number")
                                        .setMessage("The entered password is invalid recheck and try again.")
                                        .setCancelable(true).show();
                            } else {
                                drRole.child("GuardLocation").child(Objects.requireNonNull(user.getPhoneNumber())).child("token").setValue(Objects.requireNonNull(task.getResult()).getToken());
                                drRole.child("GuardLocation").child(user.getPhoneNumber()).child("active").setValue(1);
                                drRole.child("Info").child(user.getPhoneNumber()).child("active").setValue(1);
                                PreferenceManager.setBoolean(getBaseContext(), Config.KEY_GUARD_ON_DUTY, true);
                                Log.d(TAG_GUARD, "Number : " + user.getPhoneNumber());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    getApplicationContext().startForegroundService(new Intent(getApplicationContext(), GuardLocationService.class));
                                } else {
                                    getApplicationContext().startService(new Intent(getApplicationContext(), GuardLocationService.class));
                                    Log.d(TAG_SERVICE, "Service " + PreferenceManager.getBoolean(getBaseContext(), Config.KEY_IS_SERVICE_RUNNING));
                                }
                                startActivity(new Intent(getApplicationContext(), HomeScreenGuard.class));
                                finish();
                            }
                        }
                    });
                } else {
                    new AlertDialog.Builder(HomeScreenGuardLogin.this)
                            .setTitle("Incorrect Phone Number")
                            .setMessage("The entered password is invalid recheck and try again.")
                            .setCancelable(true).show();
                }
            }
        });
    }
}
