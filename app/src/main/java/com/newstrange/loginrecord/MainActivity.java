package com.newstrange.loginrecord;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;

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

    public static final String USERNAME = "username";
    private StorageReference mStorageRef;
    private static FirebaseDatabase mDatabaseIns;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE_REQUEST = 234;

    private Button mSendButton;
    private Button mEnter_button;
    private Button mExit_button;

    final String[] months = {"", "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
            "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"};

    Calendar takvim = Calendar.getInstance();

    //  private static Uri filePath;
    private static Bitmap mPhoto;

    public static final String DURUM_GIRIS = "giris";
    public static final String DURUM_CIKIS = "cikis";

    private static String mDurum = "giris";
    private static String mName = null;

    String tarih = takvim.get(Calendar.DATE) + "." + months[takvim.get(Calendar.MONTH) + 1] + "." + takvim.get(Calendar.YEAR);

    private ImageView mImageView;
    private View mPopupInputDialogView;

//metodun içinde saati böyle alıcaz String currentTime = getCurrentTime();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // FIREBASE storage
        // FIREBASE database
        if (mDatabaseIns == null) {
            mDatabaseIns = FirebaseDatabase.getInstance();
            mDatabaseIns.setPersistenceEnabled(true);
//            mDatabaseIns.setPersistenceEnabled(true); // disk persistance(offline)
        }

        mStorageRef = FirebaseStorage.getInstance().getReference();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mImageView = findViewById(R.id.show_photo_before_sent);
        mEnter_button = findViewById(R.id.enter_button);
        mExit_button = findViewById(R.id.exit_button);
        mSendButton = findViewById(R.id.send_button);
        mSendButton.setVisibility(View.INVISIBLE);


        mSendButton.setOnClickListener(this);
        mEnter_button.setOnClickListener(this);
        mExit_button.setOnClickListener(this);


        if (savedInstanceState != null) {
            mName = savedInstanceState.getString(USERNAME);
//            Toast.makeText(MainActivity.this, mName, Toast.LENGTH_SHORT).show();
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
        dialog.setTitle("Kullanıcı Girişi");
        dialog.setContentView(R.layout.popup_name_input_dialog);
        dialog.setCancelable(false);
        final Button btnLogin = dialog.findViewById(R.id.ok_button);
        final EditText username = dialog.findViewById(R.id.name);
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
            User user = new User(mName, currentTime, tarih, mDurum);

            if (mDurum == DURUM_GIRIS) {

                DatabaseReference user_details = mDatabaseIns.getReference("Giriş");
                String userId = user_details.push().getKey();
                user_details.child(userId).setValue(user);

            } else if (mDurum == DURUM_CIKIS) {

                DatabaseReference user_details = mDatabaseIns.getReference("Çıkış");
                String userId = user_details.push().getKey();
                user_details.child(userId).setValue(user);
            }

            String image_id = mName + "_" + mDurum + "_" + currentTime + "_" + tarih;


            // mPhoto UNIQUE ID : first save image IDs to database to get the images from admin panel
            DatabaseReference user_details = mDatabaseIns.getReference("storeIDs");
            String photoID = user_details.push().getKey();
            user_details.child(photoID).setValue(image_id);


            // then save images to storage
            Bitmap bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            StorageReference riversRef = mStorageRef.child("images/" + image_id + ".jpeg"); // png is better
            UploadTask uploadTask = riversRef.putBytes(data);
            riversRef.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying a success toast
                            Toast.makeText(getApplicationContext(), "Dosya yüklendi", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Yükleniyor " + ((int) progress) + "%...");
                        }
                    });
        }
        //if there is no any file
        else {
            Toast.makeText(getApplicationContext(), "Lütfen resim çekin", Toast.LENGTH_LONG).show();
        }

        //////////  ESKİSİ - galeriden seçme
/*
 if (filePath != null) {
 //displaying a progress dialog while upload is going on
 final ProgressDialog progressDialog = new ProgressDialog(this);
 progressDialog.setTitle("Uploading");
 progressDialog.show();

 String currentTime = getCurrentTime();

 //create new user for only database storage
 User user = new User(mName, currentTime, tarih);

 if (mDurum == DURUM_GIRIS) {

 DatabaseReference user_details = mDatabaseIns.getReference("Giriş");
 String userId = user_details.push().getKey();
 user_details.child(userId).setValue(user);

 } else if (mDurum == DURUM_CIKIS) {

 DatabaseReference user_details = mDatabaseIns.getReference("Çıkış");
 String userId = user_details.push().getKey();
 user_details.child(userId).setValue(user);
 }


 // create name of image to upload to storage
 String image_id = mName + "_" + mDurum + "_" + currentTime + "_" + tarih;


 // mPhoto UNIQUE ID : first save image IDs to database to get the images from admin panel
 DatabaseReference user_details = mDatabaseIns.getReference("storeIDs");
 String photoID = user_details.push().getKey();
 user_details.child(photoID).setValue(image_id);

 // then save images to storage
 StorageReference riversRef = mStorageRef.child("images/" + image_id + ".png");
 riversRef.putFile(mPhoto)
 .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
@Override public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//if the upload is successfull
//hiding the progress dialog
progressDialog.dismiss();

//and displaying a success toast
Toast.makeText(getApplicationContext(), "Dosya yüklendi", Toast.LENGTH_LONG).show();
}
})
 .addOnFailureListener(new OnFailureListener() {
@Override public void onFailure(@NonNull Exception exception) {
//if the upload is not successfull
//hiding the progress dialog
progressDialog.dismiss();

//and displaying error message
Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
}
})
 .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
@Override public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//calculating progress percentage
double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

//displaying percentage in progress dialog
progressDialog.setMessage("Yükleniyor " + ((int) progress) + "%...");
}
});
 }
 //if there is not any file
 else {
 Toast.makeText(getApplicationContext(), "Lütfen bir resim seçin", Toast.LENGTH_LONG).show();
 }
 */
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
//            showFileChooser(); // gallery
            mDurum = DURUM_GIRIS;
            takePictureFromCamera(); // camera

        } else if (v == mExit_button) {
//            showFileChooser(); // gallery
            mDurum = DURUM_CIKIS;
            takePictureFromCamera(); // camera

        } else if (v == mSendButton) {
            uploadImage();
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Resim seç"), PICK_IMAGE_REQUEST);
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
                ////
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
                ////
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
//        //FROM GALLERY
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            filePath = data.getData();
//            Log.i("MAINACTIVTTY", filePath.toString());
//            mSendButton.setVisibility(View.VISIBLE);
//
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
//                mImageView.setImageBitmap(bitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Uri u = data.getData();
            mSendButton.setVisibility(View.VISIBLE);

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            mPhoto = bitmap;
            mImageView.setImageBitmap(bitmap);

        }


    }
}