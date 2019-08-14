package com.newstrange.loginrecord;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ListSelfiesFragment extends Fragment {

    private FirebaseStorage mStorageIns;
    private FirebaseDatabase mDatabaseIns;

    private RecyclerView mRecyclerView;
    private SelfiesAdapter mAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mDatabaseIns = FirebaseDatabase.getInstance(); // DATABASE
        mStorageIns = FirebaseStorage.getInstance(); // STORAGE

        final View view = inflater.inflate(R.layout.fragment_list_selfies, container, false);
        mRecyclerView = view.findViewById(R.id.selfies_recyclerview);

        //initialize lists
        final ArrayList<String> mPhotoIds = new ArrayList<>();
        final ArrayList<Bitmap> mPhotoArrayList = new ArrayList<>();

         mAdapter = new SelfiesAdapter(getActivity(), mPhotoArrayList, mPhotoIds);

        GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        //get names
        DatabaseReference usersPhotoRef = mDatabaseIns.getReference().child("storeIDs");

        usersPhotoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    final long ONE_MEGABYTE = 1024 * 1024;
                    for (DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()) {

                        String image_name = uniqueKeySnapshot.getValue(String.class);
                        Log.i("IMAGE_NAME", image_name);
                        mPhotoIds.add(image_name);
                        String photoId = image_name + ".jpeg";
                        StorageReference photoRef = mStorageIns.getReference().child("images/" + photoId);
                        photoRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                mPhotoArrayList.add(bitmap);
                                Log.i("GET_BITMAP", String.valueOf(mPhotoArrayList.size()));

                                mAdapter.notifyDataSetChanged();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("SELFIEFRAGMENT", e.getMessage());
                            }
                        });
                        Log.i("SELFIES", String.valueOf(mPhotoArrayList.size()));
                    }

                    TextView textView = view.findViewById(R.id.list_photos_text);
                    textView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("SELFIEFRAGMENT", databaseError.getMessage());
            }
        });
        return view;
    }
}

