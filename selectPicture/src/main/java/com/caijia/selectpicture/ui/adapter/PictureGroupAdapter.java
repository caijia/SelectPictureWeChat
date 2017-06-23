package com.caijia.selectpicture.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.caijia.selectpicture.R;
import com.caijia.selectpicture.bean.PictureGroup;
import com.caijia.selectpicture.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cai.jia on 2017/6/22 0022
 */

public class PictureGroupAdapter  extends RecyclerView.Adapter<PictureGroupAdapter.PictureGroupVH>{

    private Context context;
    private List<PictureGroup> pictureList;

    public PictureGroupAdapter(@NonNull Context context) {
        this.context = context;
        this.pictureList = new ArrayList<>();
    }

    @Override
    public PictureGroupVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_picture_group, parent, false);
        return new PictureGroupVH(view,onItemClickListener);
    }

    @Override
    public void onBindViewHolder(PictureGroupVH holder, int position) {
        PictureGroup item = pictureList.get(position);

        ImageLoader.getInstance().loadImage(item.getFirstPicture(),
                holder.imageView, R.drawable.select_picture_default_bg);
        holder.groupNameTv.setText(item.getGroupName());
        holder.pictureCountTv.setText(String.format("%d张", item.getImageCount()));

        holder.setItem(item);
        holder.itemView.setOnClickListener(holder);
    }

    @Override
    public int getItemCount() {
        return pictureList == null ? 0 : pictureList.size();
    }

    static class PictureGroupVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView groupNameTv;
        TextView pictureCountTv;
        PictureGroup item;
        OnItemClickListener onItemClickListener;

        PictureGroupVH(View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);
            this.onItemClickListener = onItemClickListener;
            imageView = (ImageView)itemView.findViewById(R.id.image_view);
            groupNameTv = (TextView)itemView.findViewById(R.id.group_name_tv);
            pictureCountTv = (TextView)itemView.findViewById(R.id.picture_count_tv);
        }

        void setItem(PictureGroup item) {
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            if (item == null) {
                return;
            }

            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(getAdapterPosition(), item);
            }
        }
    }

    public void updateItems(List<PictureGroup> items) {
        if (items == null) {
            return;
        }
        pictureList.clear();
        pictureList.addAll(items);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{

        void onItemClick(int position, PictureGroup group);
    }

    public OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
