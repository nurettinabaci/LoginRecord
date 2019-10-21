package com.newstrange.loginrecord;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListRecordsFragment extends Fragment {

    RecyclerView mRecyclerView;

    private FirebaseDatabase mDatabaseIns;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_list_records, container, false);

        mDatabaseIns = FirebaseDatabase.getInstance();

        mRecyclerView = view.findViewById(R.id.records_recyclerview);

        DatabaseReference user_details = mDatabaseIns.getReference();

        final ArrayList<User> workerArray = new ArrayList<>();

        // take entered workers list
        DatabaseReference enteredUsersRef = user_details.child("Entry");
        enteredUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()) {
                    //Loop 1 to go through all the child nodes of users
                    User user = uniqueKeySnapshot.getValue(User.class);
                    Log.i("RECORDSFRAGMENT", user.toString());
                    workerArray.add(user);
                }
                TextView textView = view.findViewById(R.id.list_records_text);
                textView.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        Log.i("RECORDSFRAGMENT", String.valueOf(workerArray.size()));

        // take exited workers list
        DatabaseReference exitedUsersRef = user_details.child("Exit");
        exitedUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()) {
                    //Loop 1 to go through all the child nodes of users
                    User user = uniqueKeySnapshot.getValue(User.class);
                    Log.i("RECORDSFRAGMENT", user.toString());
                    workerArray.add(user);
                }

                RecordsAdapter recordsAdapter = new RecordsAdapter(getActivity(), workerArray);
                mRecyclerView.setAdapter(recordsAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        return view;
    }


}
