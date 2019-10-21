package com.newstrange.loginrecord;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences mPreferences;
    private StorageReference mStorageRef;
    private static FirebaseDatabase mDatabaseIns;

    public static final String USERNAME = "username";
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private Button mSend_Button;
    private Button mEnter_button;
    private Button mExit_button;

    final String[] months = {"", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    Calendar calendar = Calendar.getInstance();

    private static Bitmap mPhoto;

    public static final String CASE_ENTRY = "entry";
    public static final String CASE_EXIT = "exit";

    private static String mCase = "entry";
    private static String mName = null;

    String date = calendar.get(Calendar.DATE) + "." + months[calendar.get(Calendar.MONTH) + 1] + "." + calendar.get(Calendar.YEAR);

    private ImageView mImageView;
    private View mPopupInputDialogView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (mDatabaseIns == null) {
            mDatabaseIns = FirebaseDatabase.getInstance();
            mDatabaseIns.setPersistenceEnabled(true);
        }

        mStorageRef = FirebaseStorage.getInstance().getReference();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mImageView = findViewById(R.id.show_photo_before_sent);
        mEnter_button = findViewById(R.id.enter_button);
        mExit_button = findViewById(R.id.exit_button);
        mSend_Button = findViewById(R.id.send_button);
        mSend_Button.setVisibility(View.INVISIBLE);


        mSend_Button.setOnClickListener(this);
        mEnter_button.setOnClickListener(this);
        mExit_button.setOnClickListener(this);


        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mName = mPreferences.getString(USERNAME, null);

        if (mName != null) {
            Log.i("MAINACTIVITY", mName);
        } else {
            mPopupInputDialogView = new View(getApplicationContext());
            showCustomDialog(mPopupInputDialogView);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(USERNAME, mName);
    }

    public void showCustomDialog(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.setTitle("User Login");
        dialog.setContentView(R.layout.popup_name_input_dialog);
        dialog.setCancelable(false);
        final Button btnLogin = dialog.findViewById(R.id.ok_button);
        final EditText username = dialog.findViewById(R.id.name);
        String checkName = mPreferences.getString(USERNAME, null);
        if (checkName != null) {
            username.setText(checkName);
            btnLogin.setEnabled(checkName != null);
        }

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = username.getText().toString().trim();
                btnLogin.setEnabled(!name.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = username.getText().toString().trim();
                mPreferences.edit().putString(USERNAME, name).commit();
                mName = name;
                Toast.makeText(MainActivity.this, mName, Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });
        dialog.show();
    }


    private void uploadImage() {
        //if there is a file to upload
        if (mPhoto != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            String currentTime = getCurrentTime();

            //create new user for keeping photo storage IDS in database
            User user = new User(mName, currentTime, date, mCase);

            if (mCase == CASE_ENTRY) {

                DatabaseReference user_details = mDatabaseIns.getReference("Entry");
                String userId = user_details.push().getKey();
                user_details.child(userId).setValue(user);

            } else if (mCase == CASE_EXIT) {

                DatabaseReference user_details = mDatabaseIns.getReference("Exit");
                String userId = user_details.push().getKey();
                user_details.child(userId).setValue(user);
            }

            String image_id = mName + "_" + mCase + "_" + currentTime + "_" + date;

            // mPhoto UNIQUE ID : first save image IDs to database to get the images from admin panel
            DatabaseReference user_details = mDatabaseIns.getReference("storeIDs");
            String photoID = user_details.push().getKey();
            user_details.child(photoID).setValue(image_id);

            // then save images to storage
            Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            StorageReference photosRef = mStorageRef.child("images/" + image_id + ".jpeg");
            UploadTask uploadTask = photosRef.putBytes(data);
            photosRef.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successful
                            //hide the progress dialog
                            progressDialog.dismiss();

                            //and display a success toast
                            Toast.makeText(getApplicationContext(), "File uploaded", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successful
                            //hide the progress dialog
                            progressDialog.dismiss();

                            //and display error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculate progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //display percentage in progress dialog
                            progressDialog.setMessage("Uploading " + ((int) progress) + "%...");
                        }
                    });
        }
        //if there is no any file
        else {
            Toast.makeText(getApplicationContext(), "Please take a selfie", Toast.LENGTH_LONG).show();
        }
    }

    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY); //
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        String time = hours + "." + minutes + "." + seconds;
        return time;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.change_name) {
            showCustomDialog(mPopupInputDialogView);
        } else if (id == R.id.admin) {
            Intent intent = new Intent(getApplicationContext(), AdminLoginPanel.class);
            startActivity(intent);
            finish();
        } else if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v == mEnter_button) {
            mCase = CASE_ENTRY;
            takePictureFromCamera();
        } else if (v == mExit_button) {
            mCase = CASE_EXIT;
            takePictureFromCamera();
        } else if (v == mSend_Button) {
            uploadImage();
        }
    }

    private void takePictureFromCamera() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                // Capture image with camera
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Uri u = data.getData();
            mSend_Button.setVisibility(View.VISIBLE);

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            mPhoto = bitmap;
            mImageView.setImageBitmap(bitmap);
        }
    }
}
