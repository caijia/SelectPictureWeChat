package com.caijia.selectpicture.ui.adapter.itemDelegate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.caijia.adapterdelegate.ItemViewDelegate;
import com.caijia.selectpicture.R;
import com.caijia.selectpicture.bean.MediaBean;
import com.caijia.selectpicture.utils.ImageLoader;

import java.util.List;

import static com.caijia.selectpicture.utils.MediaType.IMAGE;

/**
 * 图片类型
 * Created by cai.jia on 2017/6/28 0028
 */

public class ImageItemDelegate extends ItemViewDelegate<MediaBean, ImageItemDelegate.ImageVH> {

    /**
     * 是否可以多选
     */
    private boolean canMultiSelect;
    private int shadowColorNormal;
    private int shadowColorSelect;

    public ImageItemDelegate(Context context,boolean canMultiSelect) {
        this.canMultiSelect = canMultiSelect;
        shadowColorNormal = ContextCompat.getColor(context, R.color.color_ms_shadow_normal);
        shadowColorSelect = ContextCompat.getColor(context, R.color.color_ms_shadow_select);
    }

    @Override
    public ImageVH onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_select_image, parent, false);
        return new ImageVH(view,((RecyclerView)parent).getAdapter());
    }

    @Override
    public boolean isForViewType(@NonNull Object item) {
        return item instanceof MediaBean && ((MediaBean) item).getMediaType() == IMAGE;
    }

    @Override
    public void onBindViewHolder(List<?> dataSource, MediaBean item, RecyclerView.Adapter adapter,
                          ImageVH holder, int position) {
        ImageLoader.getInstance().loadImage(item.getPath(),
                holder.imageView, R.drawable.ic_sm_image_default_bg);

        holder.setItem(item);
        if (canMultiSelect) {
            holder.selectTv.setOnClickListener(holder);
        }
        holder.selectTv.setSelected(item.isSelect());
        holder.shadowView.setBackgroundColor(item.isSelect() ? shadowColorSelect : shadowColorNormal);
        holder.selectTv.setVisibility(canMultiSelect ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(holder);
    }

    @Override
    public void onBindViewHolder(List<?> dataSource, MediaBean item, RecyclerView.Adapter adapter,
                          ImageVH holder, int position, List<Object> payloads) {
        holder.selectTv.setSelected(item.isSelect());
        holder.shadowView.setBackgroundColor(item.isSelect() ? shadowColorSelect : shadowColorNormal);
        holder.selectTv.setVisibility(canMultiSelect ? View.VISIBLE : View.GONE);
    }

    static class ImageVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        View shadowView;
        TextView selectTv;
        private MediaBean item;
        private RecyclerView.Adapter adapter;

        ImageVH(View itemView, RecyclerView.Adapter adapter) {
            super(itemView);
            this.adapter = adapter;
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            shadowView = itemView.findViewById(R.id.shadow_view);
            selectTv = (TextView) itemView.findViewById(R.id.select_tv);
        }

        public void setItem(MediaBean item) {
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            if (item == null) {
                return;
            }

            if (v == selectTv) {
                item.setSelect(!item.isSelect());
                adapter.notifyItemChanged(getAdapterPosition(), item);

            } else if (v == itemView) {
                //preview

            }
        }
    }
}
