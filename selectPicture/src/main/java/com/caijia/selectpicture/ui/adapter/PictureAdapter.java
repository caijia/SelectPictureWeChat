package com.caijia.selectpicture.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.caijia.selectpicture.R;
import com.caijia.selectpicture.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cai.jia on 2017/6/22 0022
 */

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.PictureVH>{

    private Context context;
    private List<String> pictureList;

    public PictureAdapter(@NonNull Context context) {
        this.context = context;
        this.pictureList = new ArrayList<>();
    }

    @Override
    public PictureVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_picture, parent, false);
        return new PictureVH(view);
    }

    @Override
    public void onBindViewHolder(PictureVH holder, int position) {
        ImageLoader.getInstance().loadImage(pictureList.get(position),
                holder.imageView,R.drawable.select_picture_default_bg);
    }

    @Override
    public int getItemCount() {
        return pictureList == null ? 0 : pictureList.size();
    }

    static class PictureVH extends RecyclerView.ViewHolder{
        ImageView imageView;
        PictureVH(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.image_view);
        }
    }

    public void updateItems(List<String> items) {
        if (items == null) {
            return;
        }
        pictureList.clear();
        pictureList.addAll(items);
        notifyDataSetChanged();
    }
}
