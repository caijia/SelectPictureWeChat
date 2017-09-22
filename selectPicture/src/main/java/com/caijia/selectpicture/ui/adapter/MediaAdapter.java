package com.caijia.selectpicture.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.caijia.adapterdelegate.LoadMoreDelegationAdapter;
import com.caijia.selectpicture.bean.MediaBean;
import com.caijia.selectpicture.ui.itemDelegate.ImageItemDelegate;
import com.caijia.selectpicture.ui.itemDelegate.TakePictureItemDelegate;
import com.caijia.selectpicture.ui.itemDelegate.VideoItemDelegate;

import java.util.List;

/**
 * Created by cai.jia on 2017/6/22 0022
 */

public class MediaAdapter extends LoadMoreDelegationAdapter {

    private OnItemClickListener onItemClickListener;
    private OnItemSelectedListener onItemSelectedListener;

    private ImageItemDelegate imageItemDelegate;
    private VideoItemDelegate videoItemDelegate;

    /**
     * 多选时要遍历所有选中的item
     */
    public void setSourceData(List<MediaBean> sourceData) {
        if (imageItemDelegate != null) {
            imageItemDelegate.setSourceData(sourceData);
        }

        if (videoItemDelegate != null) {
            videoItemDelegate.setSourceData(sourceData);
        }
    }

    public MediaAdapter(@NonNull Context context, boolean canMultiSelect, int maxSelectNum,
                        TakePictureItemDelegate.OnTakePictureListener takePictureListener) {
        super(false, null);
        imageItemDelegate = new ImageItemDelegate(context, canMultiSelect, maxSelectNum,
                new ImageItemDelegate.OnImageSelectedListener() {
                    @Override
                    public void onImageSelected(List<MediaBean> selectedItems) {
                        if (onItemSelectedListener != null) {
                            onItemSelectedListener.onItemSelected(selectedItems);
                        }
                    }
                },
                new ImageItemDelegate.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position, MediaBean item, List<MediaBean> selectedItems) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(position, item, selectedItems);
                        }
                    }
                });

        videoItemDelegate = new VideoItemDelegate(context, canMultiSelect, maxSelectNum,
                new VideoItemDelegate.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(List<MediaBean> selectedItems) {
                        if (onItemSelectedListener != null) {
                            onItemSelectedListener.onItemSelected(selectedItems);
                        }
                    }
                },
                new VideoItemDelegate.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position, MediaBean item, List<MediaBean> selectedItems) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(position, item, selectedItems);
                        }
                    }
                });

        //图片
        delegateManager.addDelegate(imageItemDelegate);
        //视频
        delegateManager.addDelegate(videoItemDelegate);
        //照相
        delegateManager.addDelegate(new TakePictureItemDelegate(takePictureListener));
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    public interface OnItemClickListener {

        /**
         * @param position
         * @param item          当前点击的item
         * @param selectedItems 多选时选中的item,不是多选时返回为null
         */
        void onItemClick(int position, MediaBean item, List<MediaBean> selectedItems);
    }

    public interface OnItemSelectedListener {

        /**
         * 多选选中
         *
         * @param selectedItems 选中items
         */
        void onItemSelected(List<MediaBean> selectedItems);
    }
}
