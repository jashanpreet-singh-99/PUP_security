package com.pup.pupsecurity.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pup.pupsecurity.R;
import com.pup.pupsecurity.adapter.GuardListAdapter;
import com.pup.pupsecurity.model.InfoDataModel;
import com.pup.pupsecurity.utils.Config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static com.pup.pupsecurity.utils.Config.TAG_ADMIN;


public class AdminGuardList extends Activity {

    private ImageButton    addNewGuard;
    private RelativeLayout dialogAddGuard;
    private ImageView      guardImage;
    private EditText       guardNameTxtView;
    private EditText       guardContactTxtView;
    private Button         saveGuardDataBtn;

    private DatabaseReference drRole;
    private StorageReference  storageReference;

    private ArrayList<InfoDataModel> guardList = new ArrayList<>();
    private GuardListAdapter guardListAdapter;

    private Bitmap guardBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_guard_list);
        Log.d(TAG_ADMIN, "data :");
        initView();
    }

    /**
     * Initialize Values to Object and Views
     */
    private void initView() {
        RecyclerView guardListRecycleView = this.findViewById(R.id.guard_list_view);
        addNewGuard          = this.findViewById(R.id.add_new_guard);
        dialogAddGuard       = this.findViewById(R.id.new_guard_dialog);
        saveGuardDataBtn     = this.findViewById(R.id.dialog_save_btn);
        guardNameTxtView     = this.findViewById(R.id.dialog_guard_name);
        guardContactTxtView  = this.findViewById(R.id.dialog_guard_contact);
        guardImage           = this.findViewById(R.id.guard_image);

        drRole               = FirebaseDatabase.getInstance().getReference("GuardLocation");
        guardListAdapter     = new GuardListAdapter(guardList, getApplicationContext());

        guardListRecycleView.setLayoutManager(new LinearLayoutManager(this));
        guardListRecycleView.setAdapter(guardListAdapter);
        guardBitmap = getCircularBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.asset_user));
        guardImage.setImageBitmap(guardBitmap);

        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://pup-security.appspot.com").child("guard");
        onClick();
    }//initView

    /**
     * Method for Listeners
     */
    private void onClick() {
        addNewGuard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddGuard.setVisibility(View.VISIBLE);
                addNewGuard.setVisibility(View.GONE);
            }
        });

        dialogAddGuard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddGuard.setVisibility(View.GONE);
                addNewGuard.setVisibility(View.VISIBLE);
            }
        });

        guardImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        saveGuardDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence conc = guardContactTxtView.getText();
                CharSequence name = guardNameTxtView.getText();
                if (conc.length() == 10 && name.length() > 0) {
                    dialogAddGuard.setVisibility(View.GONE);
                    addNewGuard.setVisibility(View.VISIBLE);
                    guardNameTxtView.setText("");
                    guardContactTxtView.setText("");
                    String contact = "+91" + conc;
                    drRole.child(contact).child("name").setValue(String.valueOf(name));
                    drRole.child(contact).child("role").setValue("guard");
                    uploadImageToServer(contact);
                } else {
                    dialogAddGuard.setVisibility(View.GONE);
                    addNewGuard.setVisibility(View.VISIBLE);
                    new AlertDialog.Builder(AdminGuardList.this)
                            .setTitle("Invalid  Data")
                            .setMessage("Try again later with correct data.")
                            .setCancelable(true).show();
                }
            }
        });

        Log.d(TAG_ADMIN, "data :");
        drRole.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                guardList.clear();
                int count = 0;
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    guardList.add(new InfoDataModel(
                            data.child("latitude").getValue() + "",
                            data.child("longitude").getValue() + "",
                            data.getKey(),
                            "wait",
                            data.child("time").getValue(String.class)
                    ));
                    downloadImageGuard( data.getKey(), count);
                    count ++;
                }
                guardListAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG_ADMIN, "Error : ");
            }
        });
    }//onClick

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
                guardListAdapter.notifyDataSetChanged();
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
     * Select image from the Device
     */
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Config.PICK_IMAGE_REQUEST);
    }//selectImage

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Config.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null ) {
            Uri guardImagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), guardImagePath);
                guardBitmap = getCircularBitmap(bitmap);
                guardImage.setImageBitmap(guardBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }//onActivityResult

    /**
     * Convert Image into circular Bitmap Image
     * @param bitmap Input Rectangular Bitmap
     * @return  Circular Bitmap
     */
    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output;
        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        float r;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }//getCircularBitmap

    /**
     * Upload Image to fireBase Storage
     * @param contact Contact of the new User
     */
    private void uploadImageToServer(String contact){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image .. ");
        progressDialog.show();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        guardBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        storageReference.child(contact + ".png").putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e(TAG_ADMIN, "Error " + e );
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setMessage("Uploading " + (int) progress + "%");
            }
        });
    }//uploadImageToServer
}
