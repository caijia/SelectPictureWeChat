package com.caijia.selectpicture.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.caijia.adapterdelegate.LoadMoreDelegationAdapter;
import com.caijia.selectpicture.ui.itemDelegate.MediaGroupItemDelegate;

/**
 * Created by cai.jia on 2017/6/22 0022
 */

public class MediaGroupAdapter extends LoadMoreDelegationAdapter{

    public MediaGroupAdapter(@NonNull Context context,
                             @Nullable MediaGroupItemDelegate.OnItemClickListener itemClickListener) {
        super(false, null);
        delegateManager.addDelegate(new MediaGroupItemDelegate(context,itemClickListener));
    }
}
