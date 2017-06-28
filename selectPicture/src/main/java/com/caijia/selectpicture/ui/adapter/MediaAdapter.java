package com.caijia.selectpicture.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.caijia.selectpicture.bean.MediaBean;
import com.caijia.selectpicture.ui.adapter.itemDelegate.DefaultDelegationAdapter;
import com.caijia.selectpicture.ui.adapter.itemDelegate.ImageItemDelegate;
import com.caijia.selectpicture.ui.adapter.itemDelegate.VideoItemDelegate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cai.jia on 2017/6/22 0022
 */

public class MediaAdapter extends DefaultDelegationAdapter {

    private List<MediaBean> mediaList;

    public MediaAdapter(@NonNull Context context) {
        this.mediaList = new ArrayList<>();
        setDataSource(mediaList);
        delegateManager.addDelegate(new ImageItemDelegate(context,true));
        delegateManager.addDelegate(new VideoItemDelegate(context,true));
    }

    public void updateItems(List<MediaBean> items) {
        if (items == null) {
            return;
        }
        mediaList.clear();
        mediaList.addAll(items);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{

        void onItemClick(int position, MediaBean item);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
