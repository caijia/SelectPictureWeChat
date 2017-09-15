package com.caijia.selectpicture.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.caijia.adapterdelegate.LoadMoreDelegationAdapter;
import com.caijia.selectpicture.bean.MediaBean;
import com.caijia.selectpicture.ui.adapter.itemDelegate.ImageItemDelegate;
import com.caijia.selectpicture.ui.adapter.itemDelegate.TakePictureItemDelegate;
import com.caijia.selectpicture.ui.adapter.itemDelegate.VideoItemDelegate;

import java.util.List;

/**
 * Created by cai.jia on 2017/6/22 0022
 */

public class MediaAdapter extends LoadMoreDelegationAdapter {

    private OnItemClickListener onItemClickListener;

    public MediaAdapter(@NonNull Context context, boolean canMultiSelect, int maxSelectNum,
                        TakePictureItemDelegate.OnTakePictureListener takePictureListener,
                        ImageItemDelegate.OnImageSelectedListener imageSelectedListener) {
        super(false, null);

        //图片
        delegateManager.addDelegate(new ImageItemDelegate(context, canMultiSelect, maxSelectNum,
                imageSelectedListener, new ImageItemDelegate.OnItemClickListener() {
            @Override
            public void onItemClick(int position, MediaBean item, List<MediaBean> selectedItems) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(position, item, selectedItems);
                }
            }
        }));

        //视频
        delegateManager.addDelegate(new VideoItemDelegate(context, canMultiSelect));

        //照相
        delegateManager.addDelegate(new TakePictureItemDelegate(takePictureListener));
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {

        /**
         * @param position
         * @param item 当前点击的item
         * @param selectedItems 多选时选中的item,不是多选时返回为null
         */
        void onItemClick(int position, MediaBean item, List<MediaBean> selectedItems);
    }
}
