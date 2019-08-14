package com.newstrange.loginrecord;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;

public class SelfiesAdapter extends RecyclerView.Adapter<SelfiesAdapter.ImageViewHolder> {

    private Context mContext;
    ArrayList<Bitmap> mPhotoList;
    ArrayList<String> mPhotoNameList;
    LayoutInflater layoutInflater;
    private int[] colorResId = {R.color.colorGreen, R.color.colorRed};


    public SelfiesAdapter(Context context, ArrayList<Bitmap> photos, ArrayList<String> photoNames) {
        mContext = context;
        layoutInflater = LayoutInflater.from(context);
        mPhotoList = photos;
        mPhotoNameList = photoNames;
    }

    @NonNull
    @Override
    public SelfiesAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = layoutInflater.inflate(R.layout.photo_thumbnail, viewGroup, false);
        ImageViewHolder holder = new ImageViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder imageViewHolder, int i) {

        Bitmap bitmap = mPhotoList.get(i);
        String photoName = mPhotoNameList.get(i);

        imageViewHolder.setData(bitmap, photoName); //->>
//        imageViewHolder.imageName.setText(photoName);
//        imageViewHolder.imageView.setImageBitmap(bitmap);
//
//        if (photoName.contains("giris"))
//            imageViewHolder.imageName.setTextColor(colorResId[0]);
//        else
//            imageViewHolder.imageName.setTextColor(colorResId[1]);

//        Glide.with(mContext)
//                .load(bitmap)
//                .centerInside()
//                .into(imageViewHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return mPhotoList.size();
    }


    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView imageName;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageName = itemView.findViewById(R.id.photo_name);
            imageView = itemView.findViewById(R.id.photo_image_view);
        }

        public void setData(Bitmap bitmap, String photoName) {
            this.imageView.setImageBitmap(bitmap);
            this.imageName.setText(photoName);

            if (photoName.contains("giris"))
//                this.imageName.setTextColor(colorResId[0]);
                this.imageName.setTextColor(mContext.getResources().getColor(colorResId[0]));
            else
//                this.imageName.setTextColor(colorResId[1]);
                this.imageName.setTextColor(mContext.getResources().getColor(colorResId[1]));

        }

    }
}