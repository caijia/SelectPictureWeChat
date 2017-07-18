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
import java.util.Locale;

import static com.caijia.selectpicture.utils.MediaType.VIDEO;
import static java.lang.String.format;

/**
 * 视频类型
 * Created by cai.jia on 2017/6/28 0028
 */
public class VideoItemDelegate extends ItemViewDelegate<MediaBean, VideoItemDelegate.VideoVH> {

    /**
     * 是否可以多选
     */
    private boolean canMultiSelect;
    private int shadowColorNormal;
    private int shadowColorSelect;

    public VideoItemDelegate(Context context,boolean canMultiSelect) {
        this.canMultiSelect = canMultiSelect;
        shadowColorNormal = ContextCompat.getColor(context, R.color.color_ms_shadow_normal);
        shadowColorSelect = ContextCompat.getColor(context, R.color.color_ms_shadow_select);
    }

    @Override
    public VideoVH onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_select_video, parent, false);
        return new VideoVH(view,((RecyclerView)parent).getAdapter());
    }

    @Override
    public boolean isForViewType(@NonNull Object item) {
        return item instanceof MediaBean && ((MediaBean) item).getMediaType() == VIDEO;
    }

    @Override
    public void onBindViewHolder(List<?> dataSource, MediaBean item, RecyclerView.Adapter adapter,
                          VideoVH holder, int position) {
        ImageLoader.getInstance().loadImage(item.getPath(),
                holder.imageView, R.drawable.ic_sm_image_default_bg);
        holder.videoTime.setText(toTime(item.getDuration()));
        holder.setItem(item);
        if (canMultiSelect) {
            holder.selectTv.setOnClickListener(holder);
        }
        holder.selectTv.setSelected(item.isSelect());
        holder.shadowView.setBackgroundColor(item.isSelect() ? shadowColorSelect : shadowColorNormal);
        holder.selectTv.setVisibility(canMultiSelect ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(holder);
    }

    private String toTime(long duration) {
        int totalSecond = (int) (duration / 1000) + (duration % 1000 < 500 ? 0 : 1);
        int h = totalSecond / 3600;
        int m = totalSecond % 3600 / 60;
        int s = totalSecond % 3600 % 60;
        if (h > 0) {
            return String.format(Locale.CHINESE,"%02d:%02d:%02d", h, m, s);

        }else{
            return format(Locale.CHINESE,"%02d:%02d", m, s);
        }
    }

    @Override
    public void onBindViewHolder(List<?> dataSource, MediaBean item, RecyclerView.Adapter adapter,
                          VideoVH holder, int position, List<Object> payloads) {
        holder.selectTv.setSelected(item.isSelect());
        holder.shadowView.setBackgroundColor(item.isSelect() ? shadowColorSelect : shadowColorNormal);
        holder.selectTv.setVisibility(canMultiSelect ? View.VISIBLE : View.GONE);
    }

    static class VideoVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        View shadowView;
        TextView selectTv;
        TextView videoTime;
        private MediaBean item;
        private RecyclerView.Adapter adapter;

        VideoVH(View itemView, RecyclerView.Adapter adapter) {
            super(itemView);
            this.adapter = adapter;
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            shadowView = itemView.findViewById(R.id.shadow_view);
            selectTv = (TextView) itemView.findViewById(R.id.select_tv);
            videoTime = (TextView) itemView.findViewById(R.id.video_time_tv);
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
