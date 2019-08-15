package com.newstrange.loginrecord;

import android.content.Context;
import android.content.Intent;

import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.MyViewHolder> {
    ArrayList<User> mUserList;
    LayoutInflater layoutInflater;
    private int[] colorResId = {R.color.colorGreen, R.color.colorRed};
    private Context mContext;

    public RecordsAdapter(Context mContext, ArrayList<User> users) {
        layoutInflater = LayoutInflater.from(mContext);
        mUserList = users;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public RecordsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = layoutInflater.inflate(R.layout.log_thumbnail, viewGroup, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecordsAdapter.MyViewHolder myViewHolder, int i) {
        User selectedUser = mUserList.get(i);
        Log.i("RECORDs - ADAPTER ", selectedUser.toString());
        myViewHolder.setData(selectedUser, i);
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView entering_imageView;
        TextView worker_name, worker_hour, worker_date;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            worker_name = itemView.findViewById(R.id.worker_name);
            worker_hour = itemView.findViewById(R.id.worker_hour);
            worker_date = itemView.findViewById(R.id.worker_date);
            entering_imageView = itemView.findViewById(R.id.entering_token);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(mContext, "ITEM CLICKED", Toast.LENGTH_SHORT).show();
                    User selectedUser = mUserList.get(getAdapterPosition());
                    Log.i("LOG ADAPTER", selectedUser.toString());
                    String image_id = selectedUser.getName() + "_" + selectedUser.getEntering() + "_" + selectedUser.getHour() + "_" + selectedUser.getDate();

                    Intent intent = new Intent(mContext, SelfieViewActivity.class);
                    intent.putExtra("image_name", image_id);
                    mContext.startActivity(intent);
                }
            });
        }

        public void setData(User selectedUser, int position) {
            Log.i("LOG ADAPTER", selectedUser.toString());
            this.worker_name.setText(selectedUser.getName());
            this.worker_hour.setText(selectedUser.getHour());
            this.worker_date.setText(selectedUser.getDate());
            String durum = selectedUser.getEntering();

            if (durum.equals(MainActivity.DURUM_GIRIS)) {

//                entering_imageView.setColorFilter(colorResId[0]);
                entering_imageView.setColorFilter(mContext.getResources().getColor(colorResId[0]),
                        PorterDuff.Mode.SRC_IN);
            } else if (durum.equals(MainActivity.DURUM_CIKIS)) {

//                entering_imageView.setColorFilter(colorResId[1]);
                entering_imageView.setColorFilter(mContext.getResources().getColor(colorResId[1]),
                        PorterDuff.Mode.SRC_IN);
            }
        }
    }

}
