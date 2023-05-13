package com.pup.pupsecurity.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
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
import com.pup.pupsecurity.adapter.GuardDeployedAdapter;
import com.pup.pupsecurity.adapter.GuardSelectionListAdapter;
import com.pup.pupsecurity.interfaces.OnGuardSelected;
import com.pup.pupsecurity.model.InfoDataModel;
import com.pup.pupsecurity.model.SOSDataModel;
import com.pup.pupsecurity.utils.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.pup.pupsecurity.utils.Config.TAG_ADMIN;

public class AdminDetailedSOS extends Activity implements OnGuardSelected {

    private SOSDataModel data;

    private boolean     historyView = false;

    private ProgressBar loadingBar;
    private ImageView   sosImage;
    private TextView    contactTxtView;
    private TextView    timeTxtView;
    private TextView    extraTxtView;
    private TextView    typeTxtView;

    private ImageButton locationBtn;
    private ImageButton callBtn;
    private ImageButton addGuradBtn;
    private ImageButton completeBtn;

    private RelativeLayout guardSelectionList;
    private ImageButton    closeBtn;
    private ImageButton    backBtn;
    private RecyclerView   guardSelectionRecyclerView;
    private RecyclerView   guardPostedSOSView;

    private DatabaseReference drSOS;
    private DatabaseReference drRole;
    private StorageReference  storageReference;

    private GuardSelectionListAdapter guardSelectionListAdapter;
    private GuardDeployedAdapter      guardDeployedAdapter;
    private ArrayList<InfoDataModel> guardList          = new ArrayList<>();
    private ArrayList<InfoDataModel> guardSOSList       = new ArrayList<>();

    private int guardDeployed = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_sos_deatil);
        data = (SOSDataModel) getIntent().getSerializableExtra("data");
        historyView = getIntent().getBooleanExtra("history_mode", false);
        assert data != null;
        guardDeployed = data.getDeployedGuard().length;
        initView();
    }

    private void initView() {
        loadingBar     = this.findViewById(R.id.load_image);
        sosImage       = this.findViewById(R.id.sos_image);
        contactTxtView = this.findViewById(R.id.sos_contact);
        timeTxtView    = this.findViewById(R.id.sos_name);
        extraTxtView   = this.findViewById(R.id.extra_message);
        typeTxtView    = this.findViewById(R.id.sos_type);

        locationBtn = this.findViewById(R.id.location_sos_btn);
        callBtn     = this.findViewById(R.id.call_sos_btn);
        addGuradBtn = this.findViewById(R.id.add_guard_sos_btn);
        completeBtn = this.findViewById(R.id.complete_sos_btn);

        guardSelectionRecyclerView = this.findViewById(R.id.guard_list_view);
        guardPostedSOSView         = this.findViewById(R.id.guard_deployed_view);

        guardSelectionList = this.findViewById(R.id.guard_list_selection);
        closeBtn           = this.findViewById(R.id.close_btn);
        backBtn            = this.findViewById(R.id.back_to_sos_list);

        drSOS            = FirebaseDatabase.getInstance().getReference("SOS");
        drRole               = FirebaseDatabase.getInstance().getReference("GuardLocation");
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://pup-security.appspot.com").child("guard");

        guardSelectionListAdapter = new GuardSelectionListAdapter(guardList, guardSOSList, getBaseContext(), this);
        guardSelectionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        guardSelectionRecyclerView.setAdapter(guardSelectionListAdapter);


        guardPostedSOSView.setLayoutManager(new LinearLayoutManager(this));
        guardDeployedAdapter = new GuardDeployedAdapter(guardSOSList, getBaseContext(),this);
        guardPostedSOSView.setAdapter(guardDeployedAdapter);

        contactTxtView.setText(data.getContact());
        typeTxtView.setText(data.getType());
        timeTxtView.setText(data.getTime());
        extraTxtView.setText(data.getExtraMessage());
        Glide.with(getBaseContext()).asBitmap().load(data.getImage()).into(new BitmapImageViewTarget(sosImage){
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                super.onResourceReady(resource, transition);
                loadingBar.setVisibility(View.GONE);
            }
        });
        onClick();

        if (historyView) {
            addGuradBtn.setVisibility(View.GONE);
            completeBtn.setVisibility(View.GONE);
            callBtn.setVisibility(View.GONE);
            locationBtn.setVisibility(View.GONE);
        }
    }

    private void onClick() {
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), SOSMap.class);
                Bundle bundle = new Bundle();
                bundle.putString("lat", data.getLatitude());
                bundle.putString("long", data.getLongitude());
                bundle.putString("contact", data.getContact());
                bundle.putString("name", data.getContact());
                intent.putExtras(bundle);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + data.getContact()));
                startActivity(intent);
            }
        });

        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drSOS.child(data.getId()).child("completed").setValue(1);
                finish();
            }
        });

        addGuradBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardSelectionList.setVisibility(View.VISIBLE);
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardSelectionList.setVisibility(View.GONE);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        drRole.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                guardList.clear();
                int count = 0;
                for (DataSnapshot data: dataSnapshot.getChildren()) {

                    if (data.child("active").getValue(Integer.class) == 1) {
                        guardList.add(new InfoDataModel(
                                data.child("latitude").getValue() + "",
                                data.child("longitude").getValue() + "",
                                data.getKey(),
                                "wait",
                                data.child("time").getValue(String.class),
                                data.child("token").getValue(String.class)
                        ));
                        downloadImageGuard( data.getKey(), count);
                        count ++;
                    }
                }
                guardSelectionListAdapter.notifyDataSetChanged();
                deployedGuardDataFilter();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG_ADMIN, "Error : ");
            }
        });
    }

    private void deployedGuardDataFilter() {
        drSOS.child(data.getId()).child("guards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                guardSOSList.clear();
                int dGuards = (int) dataSnapshot.getChildrenCount();
                Log.d(TAG_ADMIN, "count deployed : " + dGuards);
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    for (InfoDataModel info: guardList) {
                        Log.d(TAG_ADMIN, "count deployed : " + data.getKey());
                        if (info.getContact().equals(data.getKey())){
                            guardSOSList.add(info);
                        }
                    }
                }
                guardDeployedAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG_ADMIN, "Error : ");
            }
        });
    }

    @Override
    public void onGuardSelected(String contact, int index) {
        for (InfoDataModel info: guardSOSList){
            if (contact.equals(info.getContact())) {
                return;
            }
        }
        sendNotificationFCM(data.getContact(), guardList.get(index).getToken());
        drSOS.child(data.getId()).child("guards").child(contact).setValue(contact);
        guardSOSList.add(guardList.get(index));
        guardDeployedAdapter.notifyDataSetChanged();
        guardSelectionListAdapter.notifyDataSetChanged();
        guardDeployed++;
        guardSelectionList.setVisibility(View.GONE);

    }

    @Override
    public void removeGuardSelected(int index, String contact) {
        if (historyView) {
            return;
        }
        drSOS.child(data.getContact()).child("guards").child(contact).removeValue();
        guardSOSList.remove(index);
        guardDeployedAdapter.notifyDataSetChanged();
        guardSelectionListAdapter.notifyDataSetChanged();
    }

    /**
     *  Download Image of the Guards
     * @param contact contact number of the guard
     * @param count   index of the guard in the guard data list
     */
    private void downloadImageGuard(String contact, final int count) {
        storageReference.child(contact + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                guardList.get(count).setImage(uri.toString());
                guardSelectionListAdapter.notifyDataSetChanged();
                guardDeployedAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG_ADMIN, "Unable to fetch Image : " + e);
                guardList.get(count).setImage("none");
            }
        });
    }//downloadImageGuard

    /**
     * send notification using json data object using volley.
     * @param phoneNum contact of the user who need sos request.
     */
    private void sendNotificationFCM(String phoneNum, String token) {
        RequestQueue mRequestQue = Volley.newRequestQueue(this);

        JSONObject json = new JSONObject();
        try {
            JSONArray receiver = new JSONArray();
            receiver.put(token);
            json.put("to", token);
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", "SOS Alert");
            notificationObj.put("body", "SOS Request By " + phoneNum + ". Check App for Location information of the SOS Request.");
            json.put("notification", notificationObj);

            String URL = "https://fcm.googleapis.com/fcm/send";
            Log.d(TAG_ADMIN, "Notification Sent to FCM :" + token + " " + json);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG_ADMIN, "Notification Sent to FCM "+ response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG_ADMIN, "Unable to send Notification");
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
}
