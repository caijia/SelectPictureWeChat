package com.caijia.selectpicture.ui.itemDelegate;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.caijia.adapterdelegate.ItemViewDelegate;
import com.caijia.selectpicture.R;
import com.caijia.selectpicture.bean.MediaGroup;
import com.caijia.selectpicture.utils.ImageLoader;
import com.caijia.selectpicture.utils.MediaType;

import java.util.List;

/**
 * 多媒体组类型
 * Created by cai.jia on 2017/6/28 0028
 */
public class MediaGroupItemDelegate extends ItemViewDelegate<MediaGroup,MediaGroupItemDelegate.MediaGroupVH> {

    public MediaGroupItemDelegate(@Nullable OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public MediaGroupVH onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_select_media_group, parent, false);
        return new MediaGroupVH(view,((RecyclerView)parent).getAdapter(),onItemClickListener);
    }

    @Override
    public void onBindViewHolder(List<?> dataSource, MediaGroup item, RecyclerView.Adapter adapter,
                          MediaGroupVH holder, int position) {
        String imagePath = item.getFirst() == null ? "" : item.getFirst().getPath();
        ImageLoader.getInstance().loadImage(imagePath,
                holder.imageView, R.drawable.ic_sm_image_default_bg);
        holder.groupNameTv.setText(item.getGroupName());
        holder.pictureCountTv.setText(String.format("%d张", item.getSize()));
        holder.videoBgIv.setVisibility(
                item.getMediaType() == MediaType.VIDEO ? View.VISIBLE : View.GONE);
        holder.selectTv.setSelected(item.isSelect());

        holder.setItem(item);
        holder.setDataSource(dataSource);
        holder.itemView.setOnClickListener(holder);
    }

    @Override
    public boolean isForViewType(@NonNull Object item) {
        return item instanceof MediaGroup;
    }

    static class MediaGroupVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        ImageView videoBgIv;
        TextView groupNameTv;
        TextView pictureCountTv;
        TextView selectTv;
        private MediaGroup item;
        private RecyclerView.Adapter adapter;
        private List<?> dataSource;
        private OnItemClickListener onItemClickListener;

        public MediaGroupVH(View itemView, RecyclerView.Adapter adapter,
                            OnItemClickListener onItemClickListener) {
            super(itemView);
            this.adapter = adapter;
            this.onItemClickListener = onItemClickListener;
            imageView = (ImageView)itemView.findViewById(R.id.image_view);
            videoBgIv = (ImageView)itemView.findViewById(R.id.video_bg_iv);
            groupNameTv = (TextView)itemView.findViewById(R.id.group_name_tv);
            pictureCountTv = (TextView)itemView.findViewById(R.id.picture_count_tv);
            selectTv = (TextView)itemView.findViewById(R.id.select_tv);
        }

        public void setItem(MediaGroup item) {
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            if (item == null || adapter == null || dataSource == null || dataSource.isEmpty()) {
                return;
            }

            for (Object o : dataSource) {
                if (o instanceof MediaGroup) {
                    MediaGroup group = (MediaGroup) o;
                    group.setSelect(o == item);
                }
            }
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(getAdapterPosition(), item);
            }
            adapter.notifyDataSetChanged();
        }

        public void setDataSource(List<?> dataSource) {
            this.dataSource = dataSource;
        }
    }

    public interface OnItemClickListener{

        void onItemClick(int position, MediaGroup group);
    }

    private OnItemClickListener onItemClickListener;

}
