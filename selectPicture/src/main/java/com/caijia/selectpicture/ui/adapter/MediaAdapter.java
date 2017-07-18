package com.caijia.selectpicture.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.caijia.adapterdelegate.LoadMoreDelegationAdapter;
import com.caijia.selectpicture.bean.MediaBean;
import com.caijia.selectpicture.ui.adapter.itemDelegate.ImageItemDelegate;
import com.caijia.selectpicture.ui.adapter.itemDelegate.TakePictureItemDelegate;
import com.caijia.selectpicture.ui.adapter.itemDelegate.VideoItemDelegate;

/**
 * Created by cai.jia on 2017/6/22 0022
 */

public class MediaAdapter extends LoadMoreDelegationAdapter {

    private OnItemClickListener onItemClickListener;

    public MediaAdapter(@NonNull Context context,boolean canMultiSelect,
                        TakePictureItemDelegate.OnTakePictureListener takePictureListener) {
        super(false, null);
        delegateManager.addDelegate(new ImageItemDelegate(context, canMultiSelect));
        delegateManager.addDelegate(new VideoItemDelegate(context, canMultiSelect));
        delegateManager.addDelegate(new TakePictureItemDelegate(takePictureListener));
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, MediaBean item);
    }
}
