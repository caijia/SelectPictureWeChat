package com.caijia.selectpicture.ui.adapter.itemDelegate;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caijia.adapterdelegate.ItemViewDelegate;
import com.caijia.selectpicture.R;
import com.caijia.selectpicture.bean.MediaBean;
import com.caijia.selectpicture.utils.MediaType;

import java.util.List;

/**
 * 拍照类型
 * Created by cai.jia on 2017/7/18 0018
 */
public class TakePictureItemDelegate extends ItemViewDelegate<MediaBean,
        TakePictureItemDelegate.TakePictureVH> {

    private OnTakePictureListener takePictureListener;

    public TakePictureItemDelegate(OnTakePictureListener takePictureListener) {
        this.takePictureListener = takePictureListener;
    }

    @Override
    public TakePictureVH onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_take_picture, parent, false);
        return new TakePictureVH(view, takePictureListener);
    }

    @Override
    public void onBindViewHolder(List<?> dataSource, MediaBean item, RecyclerView.Adapter adapter,
                                 TakePictureVH holder, int position) {
        holder.setItem(item);
        holder.itemView.setOnClickListener(holder);
    }

    @Override
    public boolean isForViewType(@NonNull Object item) {
        return item instanceof MediaBean && ((MediaBean) item).getMediaType() == MediaType.CAMERA;
    }

    public interface OnTakePictureListener {
        void onTakePicture();
    }

    static class TakePictureVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView takePictureTv;
        private MediaBean item;
        private OnTakePictureListener takePictureListener;

        public TakePictureVH(View itemView, OnTakePictureListener takePictureListener) {
            super(itemView);
            this.takePictureListener = takePictureListener;
            takePictureTv = (TextView) itemView.findViewById(R.id.take_picture_tv);
        }

        @Override
        public void onClick(View v) {
            if (item == null || item.getMediaType() != MediaType.CAMERA) {
                return;
            }

            if (takePictureListener != null) {
                takePictureListener.onTakePicture();
            }
        }

        public void setItem(MediaBean item) {
            this.item = item;
        }
    }
}
