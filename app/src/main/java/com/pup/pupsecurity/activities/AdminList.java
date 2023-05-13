package com.pup.pupsecurity.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.pup.pupsecurity.adapter.AdminListAdapter;
import com.pup.pupsecurity.model.UserData;
import com.pup.pupsecurity.utils.Config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static com.pup.pupsecurity.utils.Config.TAG_ADMIN;


public class AdminList extends Activity {

    private ImageButton     addNewAdmin;
    private ImageView       adminImage;
    private RelativeLayout  dialogAddAdmin;
    private EditText        adminNameTxtView;
    private EditText        adminContactTxtView;
    private Button          saveAdminDataBtn;

    private DatabaseReference drRole;
    private StorageReference  storageReference;

    private ArrayList<UserData> adminList = new ArrayList<>();
    private AdminListAdapter    adminListAdapter;

    private Bitmap adminBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_admin_list);
        initView();
    }//onCreate

    /**
     * Method to Initialize objects
     */
    private void initView() {
        RecyclerView adminListRecycleView = this.findViewById(R.id.admin_list_view);

        addNewAdmin         = this.findViewById(R.id.add_new_admin);
        dialogAddAdmin      = this.findViewById(R.id.new_admin_dialog);
        saveAdminDataBtn    = this.findViewById(R.id.dialog_save_btn);
        adminNameTxtView    = this.findViewById(R.id.dialog_admin_name);
        adminContactTxtView = this.findViewById(R.id.dialog_admin_contact);
        adminImage          = this.findViewById(R.id.admin_image);

        drRole = FirebaseDatabase.getInstance().getReference("Info");
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://pup-security.appspot.com").child("admin");

        adminListAdapter = new AdminListAdapter(adminList, getApplicationContext());
        adminListRecycleView.setLayoutManager(new LinearLayoutManager(this));
        adminListRecycleView.setAdapter(adminListAdapter);
        onClick();
    }//initView

    /**
     * Method for Listeners
     */
    private void onClick() {
        addNewAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddAdmin.setVisibility(View.VISIBLE);
                addNewAdmin.setVisibility(View.GONE);
            }
        });

        dialogAddAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddAdmin.setVisibility(View.GONE);
                addNewAdmin.setVisibility(View.VISIBLE);
            }
        });

        saveAdminDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence conc = adminContactTxtView.getText();
                CharSequence name = adminNameTxtView.getText();
                if (conc.length() == 10 && name.length() > 0 & adminBitmap != null) {
                    dialogAddAdmin.setVisibility(View.GONE);
                    addNewAdmin.setVisibility(View.VISIBLE);
                    adminNameTxtView.setText("");
                    adminContactTxtView.setText("");
                    String contact = "+91" + conc;
                    drRole.child(contact).child("name").setValue(String.valueOf(name));
                    drRole.child(contact).child("role").setValue("admin");
                    uploadImageToServer(contact);
                } else {
                    dialogAddAdmin.setVisibility(View.GONE);
                    addNewAdmin.setVisibility(View.VISIBLE);
                    new AlertDialog.Builder(AdminList.this)
                            .setTitle("Invalid  Data")
                            .setMessage("Try again later with correct data.")
                            .setCancelable(true).show();
                }
            }
        });

        adminImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        Log.d(TAG_ADMIN, "data :");
        drRole.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adminList.clear();
                int count = 0;
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    if (Objects.equals(data.child("role").getValue(String.class), "admin")) {
                        adminList.add(new UserData(
                                data.child("name").getValue(String.class),
                                "none",
                                data.getKey()));
                        downloadImageAdmin(data.getKey(), count);
                        count++;
                    }
                }
                adminListAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG_ADMIN, "Error : ");
            }
        });
    }//onClick

    /**
     * Download Images of the Admin for the list
     * @param contact contact of the Admin
     * @param count   index of the admin in the admin list
     */
    private void downloadImageAdmin(String contact, final int count) {
        storageReference.child(contact + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                adminList.get(count).setImage(uri.toString());
                adminListAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG_ADMIN, "Unable to fetch Image : " + e);
                adminList.get(count).setImage("none");
            }
        });
    }//downloadImageAdmin

    /**
     * Used to select Image from the device.
     */
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Config.PICK_IMAGE_REQUEST);
    }//selectImage

    /**
     * Get circular Bitmap form rectangular Bitmap
     * @param bitmap Input rectangular Bitmap
     * @return       output circular Bitmap
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
     * Upload Image onto the FireBase Storage
     * @param contact contact of the new user whose image is to be updated.
     */
    private void uploadImageToServer(String contact){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image .. ");
        progressDialog.show();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        adminBitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Config.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null ) {
            Uri adminImagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), adminImagePath);
                adminBitmap = getCircularBitmap(bitmap);
                adminImage.setImageBitmap(adminBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }//onActivityResult
}
