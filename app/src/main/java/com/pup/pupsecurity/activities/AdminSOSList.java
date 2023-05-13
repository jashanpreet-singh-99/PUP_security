package com.pup.pupsecurity.activities;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pup.pupsecurity.R;
import com.pup.pupsecurity.adapter.SOSListAdapter;
import com.pup.pupsecurity.model.InfoDataModel;
import com.pup.pupsecurity.model.SOSDataModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.pup.pupsecurity.utils.Config.TAG_ADMIN;

public class AdminSOSList extends Activity {

    private TextView noSOSAlertTxtView;

    private DatabaseReference drSOS;
    private StorageReference  storageReference;

    private SOSListAdapter sosListAdapter;

    private ArrayList<SOSDataModel> sosList = new ArrayList<>();

    private Boolean checkRecent = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_list);
        getIntentData();
        initView();
    }//onCreate

    /**
     * Get Extra data from the Intent.
     */
    private void getIntentData() {
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        checkRecent = bundle.getBoolean("recent");
    }//getIntentData

    /**
     * Initialize the Objects
     */
    private void initView() {
        RecyclerView recyclerView = this.findViewById(R.id.sos_list_view);

        noSOSAlertTxtView = this.findViewById(R.id.no_sos_alert_txt_view);

        drSOS            = FirebaseDatabase.getInstance().getReference("SOS");
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://pup-security.appspot.com").child("client");

        sosListAdapter = new SOSListAdapter(sosList, getApplicationContext(), !checkRecent);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(sosListAdapter);

        getDataFromServer();
    }//initView

    /**
     * Fetch Data from the Server about the recent SOS.
     */
    private void getDataFromServer() {
        drSOS.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sosList.clear();
                int count = 0;
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    int completed = 1;
                    try {
                        completed = Integer.parseInt(data.child("completed").getValue() + "");
                    } catch (Exception e) {
                        Log.e(TAG_ADMIN, "Error in data fetch " + e);
                    }
                    if (checkRecent) {
                        if ( completed == 0) {
                            sosList.add(new SOSDataModel(
                                    data.getKey()+"",
                                    data.child("contact").getValue() + "",
                                    data.child("latitude").getValue() + "",
                                    data.child("longitude").getValue() + "",
                                    "none",
                                    completed,
                                    data.child("extra").getValue() + "",
                                    (String) data.child("time").getValue(),
                                    data.child("type").getValue() + "",
                                    new String[0]
                            ));
                            downloadImageGuard(data.child("contact").getValue()+"", count);
                            count++;
                            Log.e(TAG_ADMIN, "List + " + data.getKey() + " : " + data.child("longitude").getValue());
                        }
                    } else {
                        sosList.add(new SOSDataModel(
                                data.getKey()+"",
                                data.child("contact").getValue() + "",
                                data.child("latitude").getValue() + "",
                                data.child("longitude").getValue() + "",
                                "none",
                                completed,
                                data.child("extra").getValue() + "",
                                (String) data.child("time").getValue(),
                                data.child("type").getValue() + "",
                                new String[0]
                        ));
                        downloadImageGuard(data.child("contact").getValue()+"", count);
                        count++;
                        Log.e(TAG_ADMIN, "List + " + data.getKey() + " : " + data.child("longitude").getValue());
                    }
                }
                sosListAdapter.notifyDataSetChanged();
                if (sosList.size() == 0) {
                    noSOSAlertTxtView.setVisibility(View.VISIBLE);
                } else {
                    noSOSAlertTxtView.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG_ADMIN, "Data Update error");
            }
        });
    }//getDataFromServer

    /**
     * Fetch Download link of the image of the user requested sos from the FireBase Storage.
     * @param contact contact of the sos requesting user
     * @param count   index of the sos requesting user in the sos list
     */
    private void downloadImageGuard(final String contact, final int count) {
        storageReference.child(contact + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                sosList.get(count).setImage(uri.toString());
                sosListAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.e(TAG_ADMIN, "Unable to fetch Image : " + contact + " " + e);
                sosList.get(count).setImage("none");
            }
        });
    }//downloadImageGuard
}
