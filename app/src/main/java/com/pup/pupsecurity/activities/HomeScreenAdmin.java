package com.pup.pupsecurity.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pup.pupsecurity.R;
import com.pup.pupsecurity.utils.Config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class HomeScreenAdmin extends Activity {

    private RelativeLayout guardBtn;
    private RelativeLayout userBtn;
    private RelativeLayout sosBtn;
    private RelativeLayout mapBtn;
    private RelativeLayout adminBtn;
    private RelativeLayout sosHstBtn;
    private TextView       activeGuardTxtView;
    private TextView       sosNumberTxtView;

    private DatabaseReference fireBaseDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen_admin);
        initView();
    }//onCreate

    /**
     * Method to Initialize objects
     */
    private void initView() {
        guardBtn  = this.findViewById(R.id.guard_btn);
        userBtn   = this.findViewById(R.id.user_btn);
        sosBtn    = this.findViewById(R.id.sos_btn);
        mapBtn    = this.findViewById(R.id.map_btn);
        adminBtn  = this.findViewById(R.id.admin_btn);
        sosHstBtn = this.findViewById(R.id.history_btn);

        activeGuardTxtView = this.findViewById(R.id.active_guard_no);
        sosNumberTxtView   = this.findViewById(R.id.sos_no);

        fireBaseDatabase = FirebaseDatabase.getInstance().getReference();
        onClick();
    }//initView

    /**
     * Method to register objects Listeners
     */
    private void onClick() {
        guardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AdminGuardList.class));
            }
        });

        userBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AdminUserList.class));
            }
        });

        sosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                Intent intent = new Intent(getApplicationContext(), AdminSOSList.class);
                bundle.putBoolean("recent", true);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AdminMap.class));
            }
        });

        sosHstBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                Intent intent = new Intent(getApplicationContext(), AdminSOSList.class);
                bundle.putBoolean("recent", false);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        adminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AdminList.class));
            }
        });

        fireBaseDatabase.child("GuardLocation").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());
                    try {
                        Date date = sdf.parse((String) Objects.requireNonNull(data.child("time").getValue()));
                        assert date != null;
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.MINUTE, -10);
                        if (calendar.getTime().before(date)) {
                            count++;
                        }
                    } catch (ParseException e) {
                        Log.e(Config.TAG_SPLASH, "ERROR GUARD " + e);
                    }
                }
                activeGuardTxtView.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                activeGuardTxtView.setText("-");
            }
        });

        fireBaseDatabase.child("SOS").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    int completed = 1;
                    try {
                        completed = Integer.parseInt(Objects.requireNonNull(data.child("completed").getValue()) + "");
                    } catch (Exception e) {
                        Log.e(Config.TAG_ADMIN, "Error " + e);
                    }
                    if (completed == 0) {
                        count++;
                    }
                }
                sosNumberTxtView.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                sosNumberTxtView.setText("-");
            }
        });
    }//onClick
}
