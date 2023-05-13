package com.pup.pupsecurity.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
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
import com.pup.pupsecurity.utils.Config;
import com.pup.pupsecurity.utils.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.pup.pupsecurity.utils.Config.TAG_REGISTER;

public class RegistrationScreen extends Activity {

    private Button              getNumberBtn;
    private Button              checkOTPBtn;
    private EditText            phoneNumberTxtView;
    private EditText            otpTxtView;
    private LinearLayout        otpLay;
    private LinearLayout        numberLay;
    private TextView            titleTxtView;
    private TextView            subTxtView;
    private ProgressBar         progressBar;
    private ProgressBar         progressBarOTP;
    private LottieAnimationView animationView;
    private LinearLayout        infoLayout;
    private LinearLayout        roleLayout;
    private EditText            userNameTxtView;
    private Button              submitInfoBtn;
    private Button              roleGuardBtn;
    private Button              roleUserBtn;


    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser user;

    private Boolean manualOTP = false;

    private String verificationID = "";
    private String selectedRole   = "";

    private ArrayList<infoRecord> record = new ArrayList<>();

    /**
     * Custom data model class (java beans)
     */
    private class infoRecord {

        private String contact;
        private String role;

        private infoRecord(String contact, String role) {
            this.contact = contact;
            this.role = role;
        }

        public String getContact() {
            return contact;
        }

    }//infoRecord

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
    }// onCreate

    /**
     * Method to Initialize objects
     */
    private void initView() {
        getNumberBtn        = this.findViewById(R.id.get_otp_btn);
        checkOTPBtn         = this.findViewById(R.id.check_otp_btn);
        otpTxtView          = this.findViewById(R.id.otp_view);
        phoneNumberTxtView  = this.findViewById(R.id.phone_number_view);
        otpLay              = this.findViewById(R.id.otp_lay);
        numberLay           = this.findViewById(R.id.number_lay);
        titleTxtView        = this.findViewById(R.id.title_text);
        subTxtView          = this.findViewById(R.id.sub_text);
        animationView       = this.findViewById(R.id.animation_object);
        progressBar         = this.findViewById(R.id.progress_bar);
        progressBarOTP      = this.findViewById(R.id.progress_bar_otp);
        infoLayout          = this.findViewById(R.id.info_lay);
        roleLayout          = this.findViewById(R.id.role_lay);
        userNameTxtView     = this.findViewById(R.id.user_name_view);
        submitInfoBtn       = this.findViewById(R.id.save_info_btn);
        roleGuardBtn        = this.findViewById(R.id.guard_btn);
        roleUserBtn         = this.findViewById(R.id.user_btn);

        firebaseAuth      = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.signOut();
        }
        databaseReference = FirebaseDatabase.getInstance().getReference("Info");
        onClick();
    }// initView

    /**
     * Method for Listeners
     */
    private void onClick() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                record.clear();
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    record.add(new infoRecord(data.getKey(), data.child("role").getValue(String.class)));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        getNumberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence Number = phoneNumberTxtView.getText();
                if (Number.length() == 10) {
                    getOTP("+91"+Number);
                    getNumberBtn.setVisibility(GONE);
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    new AlertDialog.Builder(RegistrationScreen.this)
                            .setTitle("Invalid Input")
                            .setMessage("The Phone Number Entered is Not a Valid Number. Recheck the Number or try Again")
                            .setCancelable(true).show();
                }
            }
        });

        phoneNumberTxtView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start == 9) {
                    Log.d(TAG_REGISTER, "close keyboard");
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    assert inputManager != null;
                    inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        otpTxtView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start == 5) {
                    Log.d(TAG_REGISTER, "close keyboard");
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    assert inputManager != null;
                    inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        checkOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence otpCode = otpTxtView.getText();
                manualOTP = true;
                if (!verificationID.equals("") && otpCode.length() > 0 ) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, String.valueOf(otpCode));
                    signInWithPhoneAuthCredential(credential);
                    progressBarOTP.setVisibility(View.VISIBLE);
                    checkOTPBtn.setVisibility(GONE);
                } else {
                    new AlertDialog.Builder(RegistrationScreen.this)
                            .setTitle("Invalid Input")
                            .setMessage("The OTP Entered is Not a Valid OTP. Recheck the OTP or try Again")
                            .setCancelable(true).show();
                }
            }
        });

        submitInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userNameTxtView.getText().length() > 0) {
                    roleLayout.setVisibility(View.VISIBLE);
                    if (userNameTxtView.getVisibility() == VISIBLE) {
                        userNameTxtView.setVisibility(GONE);
                        if (selectedRole.equals("")) {
                            selectedRole = "select one";
                        }
                    } else {
                        if (selectedRole.equals("select one")) {
                            selectedRole = "";
                        }
                    }
                    if (selectedRole.equals("guard") || selectedRole.equals("client")) {
                         {
                             saveNewInfo();
                        }
                    } else if (selectedRole.equals("")) {
                        new AlertDialog.Builder(RegistrationScreen.this)
                                .setTitle("Select Role")
                                .setMessage("Select either of the given Role. Select Guard if you are security personal otherwise select User.")
                                .setCancelable(true).show();
                    }
                } else {
                    new AlertDialog.Builder(RegistrationScreen.this)
                            .setTitle("Invalid Username")
                            .setMessage("Input a Valid Username.")
                            .setCancelable(true).show();
                }
            }
        });

        roleGuardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedRole = "guard";
                roleGuardBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                roleGuardBtn.setTextColor(getResources().getColor(R.color.colorWhite));
                roleUserBtn.setBackgroundTintList(null);
                roleUserBtn.setTextColor(getResources().getColor(R.color.colorSecondaryText));
            }
        });

        roleUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedRole = "client";
                roleUserBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                roleUserBtn.setTextColor(getResources().getColor(R.color.colorWhite));
                roleGuardBtn.setBackgroundTintList(null);
                roleGuardBtn.setTextColor(getResources().getColor(R.color.colorSecondaryText));
            }
        });
    }// onClick

    /**
     * Register new User in FireBase Database
     */
    private void saveNewInfo() {
        selectImage();
    }//saveNewInfo

    /**
     * Method to Request One Time Password
     * @param phoneNumber contact of the user.
     */
    private void getOTP(String phoneNumber){
        Log.d(TAG_REGISTER, "Number Entered : " + phoneNumber);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                onVerifyCallBack);
    }//getOTP

    /**
     * Method to complete the sign in by verifying OTP of user
     * if successful restart from SplashScreen
     * @param credential UserData FireBase Object
     */
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user = Objects.requireNonNull(task.getResult()).getUser();
                            assert user != null;
                            Log.d(TAG_REGISTER, "Completed the verification : " + user.getPhoneNumber());
                            boolean foundUser = false;
                            for (infoRecord info: record) {
                                if (Objects.equals(user.getPhoneNumber(), info.getContact())) {
                                    Log.d(TAG_REGISTER, "Old user" + info.getContact());
                                    PreferenceManager.setBoolean(getApplicationContext(), Config.KEY_REGISTERED,true);
                                    startActivity(new Intent(getApplicationContext(), SplashScreen.class));
                                    finish();
                                    foundUser = true;
                                }
                            }
                            if (!foundUser) {
                                infoLayout.setVisibility(View.VISIBLE);
                                otpLay.setVisibility(GONE);
                                animationView.setAnimation("register_user.json");
                                animationView.playAnimation();
                                titleTxtView.setText(getResources().getString(R.string.user_information));
                                subTxtView.setText("Enter your official name as registered in University.\n Also Select appropriate role.");
                                PreferenceManager.setBoolean(getApplicationContext(), Config.KEY_REGISTERED,false);
                            }
                        } else {
                            Log.e(TAG_REGISTER, "Verification Failed");
                            new AlertDialog.Builder(RegistrationScreen.this)
                                    .setTitle("Verification Failed")
                                    .setMessage("Incorrect OTP. Try again later.")
                                    .setCancelable(true).show();
                        }
                    }
                });
    }//signInWithPhoneAuthCredential

    /**
     * Listener which respond if the verification status change
     * Check for auto Sign in using GPS
     * Else change to OTP sent state
     */
    PhoneAuthProvider.OnVerificationStateChangedCallbacks onVerifyCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            if (!manualOTP) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
                Log.d(TAG_REGISTER, "AUTO Signed IN ");
            }
        }
        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Log.d(TAG_REGISTER, "Signed IN Failed ");
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                new AlertDialog.Builder(RegistrationScreen.this)
                        .setTitle("Invalid Input")
                        .setMessage("The Phone Number Entered is Not a Valid Number. Recheck the Number or try Again")
                        .setCancelable(true).show();
            } else {
                new AlertDialog.Builder(RegistrationScreen.this)
                        .setTitle("OTP Timeout")
                        .setMessage("Unable to Verify the OTP. Try Again Later.")
                        .setCancelable(true).show();
            }
        }
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            verificationID = s;
            otpLay.setVisibility(View.VISIBLE);
            numberLay.setVisibility(GONE);
            animationView.setAnimation("otp.json");
            animationView.playAnimation();
            titleTxtView.setText(getResources().getString(R.string.check_otp));
            subTxtView.setText("Enter the one time password received through SMS.\nCheck your messaging app.");
        }
    };

    /**
     * Change UI if pressed back on OTP verification Screen
     */
    @Override
    public void onBackPressed() {
        if (numberLay.getVisibility() == GONE && infoLayout.getVisibility() == GONE) {
            numberLay.setVisibility(View.VISIBLE);
            getNumberBtn.setVisibility(View.VISIBLE);
            progressBar.setVisibility(GONE);
            otpTxtView.setText("");
            progressBarOTP.setVisibility(GONE);
            checkOTPBtn.setVisibility(VISIBLE);
            otpLay.setVisibility(GONE);
            titleTxtView.setText(getResources().getString(R.string.registration));
            subTxtView.setText("Enter your phone number to verify \nyour account");
            animationView.setAnimation("phone_register.json");
            animationView.playAnimation();
        }
    }//onBackPressed

    /**
     * Select Image From the device
     */
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Config.PICK_IMAGE_REQUEST);
    }//selectImage

    /**
     * Convert Image to circular Bitmap
     * @param bitmap rect or sq image
     * @return circular bitmap image
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
     * Used to upload image to fireBase Storage
     * @param contact User`s Registered Phone Number
     * @param imageBitmap User selected Image
     */
    private void uploadImageToServer(String contact, Bitmap imageBitmap){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image .. ");
        progressDialog.show();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
        byte[] data = baos.toByteArray();
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://pup-security.appspot.com").child(selectedRole);
        storageReference.child(contact + ".png").putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                databaseReference.child(Objects.requireNonNull(user.getPhoneNumber())).child("role").setValue(selectedRole);
                databaseReference.child(Objects.requireNonNull(user.getPhoneNumber())).child("name").setValue(userNameTxtView.getText().toString());
                PreferenceManager.setBoolean(getApplicationContext(), Config.KEY_REGISTERED,true);
                startActivity(new Intent(getApplicationContext(), SplashScreen.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e(TAG_REGISTER, "Error " + e );
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setMessage("Uploading " + (int) progress + "%");
            }
        });
    }//uploadImageToServer

    public static Bitmap scaleBitmap(Bitmap bitmap, int wantedWidth, int wantedHeight) {
        Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix m = new Matrix();
        m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
        canvas.drawBitmap(bitmap, m, new Paint());
        return output;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Config.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null ) {
            Uri guardImagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), guardImagePath);
                Bitmap ImageBitmap = getCircularBitmap(scaleBitmap(bitmap, 200, 200));
                uploadImageToServer(user.getPhoneNumber(), ImageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }//onActivityResult
}
