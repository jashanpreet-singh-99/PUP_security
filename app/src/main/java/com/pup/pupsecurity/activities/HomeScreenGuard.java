package com.pup.pupsecurity.activities;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pup.pupsecurity.R;
import com.pup.pupsecurity.adapter.SOSListAdapter;
import com.pup.pupsecurity.model.SOSDataModel;
import com.pup.pupsecurity.utils.Config;
import com.pup.pupsecurity.utils.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import static com.pup.pupsecurity.utils.Config.TAG_GUARD;

public class HomeScreenGuard extends Activity {

    private DatabaseReference drSOS;
    private DatabaseReference drRole;
    private FirebaseUser      user;
    private StorageReference  storageReference;

    private RecyclerView        recyclerView;
    private LottieAnimationView animationView;
    private TextView            noAlertTxtView;
    private ImageButton         logOutBtn;

    private SOSListAdapter sosListAdapter;

    private ArrayList<SOSDataModel> sosList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen_guard);
        initView();
    }//onCreate

    /**
     * Method to Initialize objects
     */
    private void initView() {
        recyclerView   = this.findViewById(R.id.sos_list_view);
        animationView  = this.findViewById(R.id.animation_object);
        noAlertTxtView = this.findViewById(R.id.no_alert_txtView);
        logOutBtn      = this.findViewById(R.id.log_out);

        drSOS            = FirebaseDatabase.getInstance().getReference("SOS");
        drRole = FirebaseDatabase.getInstance().getReference();
        user             = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://pup-security.appspot.com").child("user");


        sosListAdapter = new SOSListAdapter(sosList, getApplicationContext(), true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(sosListAdapter);

        getDataFromServer();
        onClick();
    }//initView

    private void onClick() {
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drRole.child("GuardLocation").child(Objects.requireNonNull(user.getPhoneNumber())).child("active").setValue(0);
                drRole.child("Info").child(user.getPhoneNumber()).child("active").setValue(0);
                PreferenceManager.setBoolean(getBaseContext(), Config.KEY_GUARD_ON_DUTY, false);
                finish();
            }
        });
    }

    /**
     * Fetch image link of the sos users
     * @param contact contact of the sos requesting user
     * @param count   index of the sos requesting user in the sos data list
     */
    private void downloadImageUser(String contact, final int count) {
        storageReference.child(contact + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                sosList.get(count).setImage(uri.toString());
                sosListAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG_GUARD, "Unable to fetch Image : " + e);
                sosList.get(count).setImage("none");
            }
        });
    }

    /**
     * get recent SOS Data from the server
     */
    private void getDataFromServer() {
        drSOS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sosList.clear();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR, -1);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());
                int count = 0;
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    int completed = 0;
                    try {
                        completed = Integer.parseInt(data.child("completed").getValue() + "");
                    } catch (Exception e) {
                        Log.e(TAG_GUARD, "error " + e);
                    }
                    if ( completed == 0) {
                        boolean deployed = false;
                        for (DataSnapshot gData: data.child("guards").getChildren()) {
                            if (Objects.equals(gData.getKey(), user.getPhoneNumber())) {
                                deployed = true;
                                break;
                            }
                        }
                        if (deployed) {
                            sosList.add(new SOSDataModel(
                                    data.getKey(),
                                    data.child("contact").getValue()+"",
                                    data.child("latitude").getValue() + "",
                                    data.child("longitude").getValue() + "",
                                    "none",
                                    completed,
                                    data.child("extra").getValue() + "",
                                    (String) data.child("time").getValue(),
                                    data.child("type").getValue() + "",
                                    new String[0]
                            ));
                            downloadImageUser(data.getKey(), count);
                            count++;
                            Log.e(TAG_GUARD, "List + " + data.getKey() + " : " + data.child("longitude").getValue());
                        }
                    }
                }
                sosListAdapter.notifyDataSetChanged();
                if (sosList.size() == 0) {
                    animationView.setVisibility(View.VISIBLE);
                    noAlertTxtView.setVisibility(View.VISIBLE);
                } else {
                    animationView.setVisibility(View.GONE);
                    noAlertTxtView.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG_GUARD, "Data Update error");
            }
        });
    }//getDataFromServer
}
