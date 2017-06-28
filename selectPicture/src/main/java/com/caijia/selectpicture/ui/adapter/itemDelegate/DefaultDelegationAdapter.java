package com.caijia.selectpicture.ui.adapter.itemDelegate;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 *
 * 参考 https://github.com/sockeqwe/AdapterDelegates
 * https://github.com/Aspsine/IRecyclerView
 * Created by cai.jia on 2017/5/12 0012
 */

public class DefaultDelegationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ItemViewDelegateManager delegateManager;

    private List<?> dataSource;

    public DefaultDelegationAdapter() {
        this(new ItemViewDelegateManager());
    }

    public DefaultDelegationAdapter(@NonNull ItemViewDelegateManager delegateManager) {
        this.delegateManager = delegateManager;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return delegateManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        delegateManager.onBindViewHolder(dataSource, this, holder, position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        delegateManager.onBindViewHolder(dataSource, this, holder, position, payloads);
    }

    @Override
    public int getItemCount() {
        return dataSource == null ? 0 : dataSource.size();
    }

    public Object getItem(int position) {
        return dataSource == null || dataSource.isEmpty() ? null : dataSource.get(position % dataSource.size());
    }

    public List<?> getDataSource() {
        return dataSource;
    }

    public void setDataSource(List<?> dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int getItemViewType(int position) {
        return delegateManager.getItemViewType(getItem(position));
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        delegateManager.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        return delegateManager.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        delegateManager.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        delegateManager.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        delegateManager.onAttachedToRecyclerView(recyclerView,dataSource);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }
}
