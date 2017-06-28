package com.caijia.selectpicture.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.caijia.selectpicture.bean.MediaGroup;
import com.caijia.selectpicture.ui.adapter.itemDelegate.DefaultDelegationAdapter;
import com.caijia.selectpicture.ui.adapter.itemDelegate.MediaGroupItemDelegate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cai.jia on 2017/6/22 0022
 */

public class MediaGroupAdapter extends DefaultDelegationAdapter{

    private List<MediaGroup> mediaList;

    public MediaGroupAdapter(@NonNull Context context,
                             @Nullable MediaGroupItemDelegate.OnItemClickListener itemClickListener) {
        this.mediaList = new ArrayList<>();
        setDataSource(mediaList);
        delegateManager.addDelegate(new MediaGroupItemDelegate(itemClickListener));
    }

    public void updateItems(List<MediaGroup> items) {
        if (items == null) {
            return;
        }
        mediaList.clear();
        mediaList.addAll(items);
        notifyDataSetChanged();
    }
}
