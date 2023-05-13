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
import com.pup.pupsecurity.adapter.UserListAdapter;
import com.pup.pupsecurity.model.UserData;
import com.pup.pupsecurity.utils.Config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static com.pup.pupsecurity.utils.Config.TAG_ADMIN;


public class AdminUserList extends Activity {

    private RecyclerView   userListRecycleView;
    private ImageButton    addNewUser;
    private ImageView      userImage;
    private RelativeLayout dialogAddUser;
    private EditText       userNameTxtView;
    private EditText       userContactTxtView;
    private Button         saveUserDataBtn;

    private DatabaseReference drRole;
    private StorageReference  storageReference;

    private Bitmap userBitmap;

    private ArrayList<UserData> userList = new ArrayList<>();
    private UserListAdapter     userListAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_list);
        Log.d(TAG_ADMIN, "data :");
        initView();
    }//onCreate

    /**
     * Initialize the Objects
     */
    private void initView() {
        userListRecycleView = this.findViewById(R.id.client_list_view);

        addNewUser          = this.findViewById(R.id.add_new_user);
        dialogAddUser       = this.findViewById(R.id.new_user_dialog);
        saveUserDataBtn     = this.findViewById(R.id.dialog_save_btn);
        userNameTxtView     = this.findViewById(R.id.dialog_user_name);
        userContactTxtView  = this.findViewById(R.id.dialog_user_contact);
        userImage           = this.findViewById(R.id.user_image);

        drRole           = FirebaseDatabase.getInstance().getReference("Info");
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://pup-security.appspot.com").child("client");

        userListAdapter = new UserListAdapter(userList, getApplicationContext());
        userListRecycleView.setLayoutManager(new LinearLayoutManager(this));
        userListRecycleView.setAdapter(userListAdapter);
        onClick();
    }//initView

    /**
     * Initialize the Listener for the objects
     */
    private void onClick() {
        addNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddUser.setVisibility(View.VISIBLE);
                addNewUser.setVisibility(View.GONE);
            }
        });

        dialogAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddUser.setVisibility(View.GONE);
                addNewUser.setVisibility(View.VISIBLE);
            }
        });

        saveUserDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence conc = userContactTxtView.getText();
                CharSequence name = userNameTxtView.getText();
                if (conc.length() == 10 && name.length() > 0) {
                    dialogAddUser.setVisibility(View.GONE);
                    addNewUser.setVisibility(View.VISIBLE);
                    userNameTxtView.setText("");
                    userContactTxtView.setText("");
                    String contact = "+91" + conc;
                    drRole.child(contact).child("name").setValue(String.valueOf(name));
                    drRole.child(contact).child("role").setValue("client");
                    uploadImageToServer(contact);
                } else {
                    dialogAddUser.setVisibility(View.GONE);
                    addNewUser.setVisibility(View.VISIBLE);
                    new AlertDialog.Builder(AdminUserList.this)
                            .setTitle("Invalid  Data")
                            .setMessage("Try again later with correct data.")
                            .setCancelable(true).show();
                }
            }
        });

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        Log.d(TAG_ADMIN, "data :");
        drRole.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                int count = 0;
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    if (Objects.equals(data.child("role").getValue(String.class), "client")) {
                        userList.add(new UserData(
                                data.child("name").getValue(String.class),
                                "none",
                                data.getKey()));
                        downloadImageUser(data.getKey(), count);
                        count++;
                    }
                }
                userListAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG_ADMIN, "Error : ");
            }
        });
    }//onClick

    /**
     * fetch Image link of the User from FireBase Storage
     * @param contact  contact of the user
     * @param count    index of the user whose image we need to fetch in the user data list
     */
    private void downloadImageUser(String contact, final int count) {
        storageReference.child(contact + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                userList.get(count).setImage(uri.toString());
                userListAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG_ADMIN, "Unable to fetch Image : " + e);
                userList.get(count).setImage("none");
            }
        });
    }//downloadImageUser

    /**
     * Select image from the device
     */
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Config.PICK_IMAGE_REQUEST);
    }//selectImage

    /**
     * Convert image into circular image
     * @param bitmap input rectangular image
     * @return output circular image
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
     * Upload image to FireBase Storage
     * @param contact contact of the user
     */
    private void uploadImageToServer(String contact){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image .. ");
        progressDialog.show();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        userBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
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
            Uri guardImagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), guardImagePath);
                userBitmap = getCircularBitmap(bitmap);
                userImage.setImageBitmap(userBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }//onActivityResult
}
