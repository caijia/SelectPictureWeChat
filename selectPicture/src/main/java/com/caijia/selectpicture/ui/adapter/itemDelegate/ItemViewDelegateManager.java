package com.caijia.selectpicture.ui.adapter.itemDelegate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.widget.Space;
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

public class ItemViewDelegateManager {

    private SparseArrayCompat<ItemViewDelegate> delegates;
    private LayoutInflater layoutInflater;

    public ItemViewDelegateManager() {
        delegates = new SparseArrayCompat<>();
    }

    public void addDelegate(@NonNull ItemViewDelegate delegate) {
        int viewType = delegates.size();
        delegates.put(viewType, delegate);
    }

    public void addDelegate(int viewType, @NonNull ItemViewDelegate delegate) {
        delegates.put(viewType, delegate);
    }

    public void removeDelegate(int viewType) {
        delegates.remove(viewType);
    }

    public void removeDelegate(@NonNull ItemViewDelegate delegate) {
        int index = delegates.indexOfValue(delegate);
        if (index != -1) {
            delegates.removeAt(index);
        }
    }

    public int getItemViewType(Object item) {
        if (item == null) {
            return -1;
        }
        int delegatesCount = delegates.size();
        for (int i = 0; i < delegatesCount; i++) {
            ItemViewDelegate delegate = delegates.valueAt(i);
            if (delegate.isForViewType(item)) {
                return delegates.keyAt(i);
            }
        }
        return -1;
    }

    public ItemViewDelegate findItemDelegate(Object item) {
        return getDelegateForViewType(getItemViewType(item));
    }

    public
    @Nullable
    ItemViewDelegate getDelegateForViewType(int viewType) {
        return delegates.get(viewType);
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        ItemViewDelegate delegate = getDelegateForViewType(viewType);
        if (delegate == null) {
            return new EmptyViewHolder(parent.getContext());
        }
        return delegate.onCreateViewHolder(layoutInflater,parent, viewType);
    }

    public void onBindViewHolder(List<?> dataSource, RecyclerView.Adapter adapter,
                                 RecyclerView.ViewHolder holder, int position) {
        ItemViewDelegate delegate = getDelegateForViewType(holder.getItemViewType());
        if (delegate != null) {
            Object item = delegate.getItem(dataSource, adapter, holder, position);
            delegate.onBindViewHolder(dataSource, item, adapter, holder, position);
        }
    }

    public void onBindViewHolder(List<?> dataSource, RecyclerView.Adapter adapter,
                                 RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        if (payloads != null && !payloads.isEmpty()) {
            ItemViewDelegate delegate = getDelegateForViewType(holder.getItemViewType());
            if (delegate != null) {
                Object item = delegate.getItem(dataSource, adapter, holder, position);
                delegate.onBindViewHolder(dataSource, item, adapter, holder, position, payloads);
            }

        } else {
            onBindViewHolder(dataSource, adapter, holder, position);
        }
    }

    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        ItemViewDelegate delegate = getDelegateForViewType(holder.getItemViewType());
        if (delegate != null) {
            delegate.onViewRecycled(holder);
        }
    }

    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        ItemViewDelegate delegate = getDelegateForViewType(holder.getItemViewType());
        if (delegate != null) {
            delegate.onFailedToRecycleView(holder);
        }
        return false;
    }

    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        ItemViewDelegate delegate = getDelegateForViewType(holder.getItemViewType());
        if (delegate != null) {
            delegate.onViewAttachedToWindow(holder);
        }
    }

    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        ItemViewDelegate delegate = getDelegateForViewType(holder.getItemViewType());
        if (delegate != null) {
            delegate.onViewDetachedFromWindow(holder);
        }
    }

    public void onAttachedToRecyclerView(final RecyclerView recyclerView, final List<?> dataSource) {
        int delegatesCount = delegates.size();
        for (int i = 0; i < delegatesCount; i++) {
            ItemViewDelegate delegate = delegates.valueAt(i);
            int itemType = delegates.keyAt(i);
            if (delegate != null) {
                delegate.onAttachedToRecyclerView(recyclerView,itemType);
            }
        }

        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (dataSource != null) {
                        Object item = dataSource.get(position);
                        int itemViewType = getItemViewType(item);
                        ItemViewDelegate delegate = getDelegateForViewType(itemViewType);
                        if (delegate != null) {
                            return delegate.getSpanCount(gridLayoutManager);
                        }
                    }
                    return spanSizeLookup.getSpanSize(position);
                }
            });
        }
    }

    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        int delegatesCount = delegates.size();
        for (int i = 0; i < delegatesCount; i++) {
            ItemViewDelegate delegate = delegates.valueAt(i);
            if (delegate != null) {
                delegate.onDetachedFromRecyclerView(recyclerView);
            }
        }
    }

    private static class EmptyViewHolder extends RecyclerView.ViewHolder {

        public EmptyViewHolder(Context context) {
            super(new Space(context));
        }
    }
}
