package com.caijia.selectpicture.ui.adapter.itemDelegate;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

/**
 * 参考 https://github.com/sockeqwe/AdapterDelegates
 * https://github.com/Aspsine/IRecyclerView
 * Created by cai.jia on 2017/5/9 0009
 */

public abstract class ItemViewDelegate<Item, VH extends RecyclerView.ViewHolder> {

    abstract VH onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType);

    abstract void onBindViewHolder(List<?> dataSource, Item item,
                                   RecyclerView.Adapter adapter, VH holder, int position);

    void onBindViewHolder(List<?> dataSource, Item item, RecyclerView.Adapter adapter,
                          VH holder, int position, List<Object> payloads) {
    }

    Item getItem(List<?> dataSource, RecyclerView.Adapter adapter, VH holder, int position) {
        return dataSource == null || dataSource.isEmpty() ? null : (Item) dataSource.get(position % dataSource.size());
    }

    abstract boolean isForViewType(@NonNull Object item);

    void onViewRecycled(VH holder) {
    }

    boolean onFailedToRecycleView(VH holder) {
        return false;
    }

    void onViewAttachedToWindow(VH holder) {
    }

    void onViewDetachedFromWindow(VH holder) {
    }

    int createCacheViewHolderCount() {
        return 0;
    }

    int maxRecycledViews() {
        return 5;
    }

    void onAttachedToRecyclerView(RecyclerView recyclerView, int itemType) {
        RecyclerView.RecycledViewPool pool = recyclerView.getRecycledViewPool();
        pool.setMaxRecycledViews(itemType, maxRecycledViews());

        int cacheViewHolderCount = createCacheViewHolderCount();
        if (cacheViewHolderCount != 0) {
            if (itemType != -1) {
                for (int i = 0; i < cacheViewHolderCount; i++) {
                    RecyclerView.ViewHolder holder = recyclerView.getAdapter()
                            .createViewHolder(recyclerView, itemType);
                    pool.putRecycledView(holder);
                }
            }
        }
    }

    void onDetachedFromRecyclerView(RecyclerView recyclerView) {
    }

    int getSpanCount(GridLayoutManager layoutManager) {
        return 1;
    }
}
