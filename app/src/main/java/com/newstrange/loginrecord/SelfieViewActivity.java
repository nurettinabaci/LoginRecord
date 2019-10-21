package com.newstrange.loginrecord;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SelfieViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String imageName = getIntent().getStringExtra("image_name");

        final ImageView image_view = findViewById(R.id.image_view);
        TextView image_name = findViewById(R.id.img_id);
        image_name.setText(imageName);

        long ONE_MEGABYTE = 1024 * 1024;
//        final ArrayList<Bitmap> mPhotoArrayList = new ArrayList<>();
        FirebaseStorage mStorageIns = FirebaseStorage.getInstance();
        StorageReference photoRef = mStorageIns.getReference().child("images/" + imageName + ".jpeg");

        photoRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                image_view.setImageBitmap(bitmap);
                Glide.with(getApplicationContext()).load(bitmap).into(image_view);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("SELFIEFRAGMENT", e.getMessage());
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent myIntent = new Intent(getApplicationContext(), AdminPanel.class);
        startActivityForResult(myIntent, 0);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id==android.R.id.home) {
            Intent myIntent = new Intent(getApplicationContext(), AdminPanel.class);
            startActivityForResult(myIntent, 0);
        }
        return super.onOptionsItemSelected(item);
    }
}
