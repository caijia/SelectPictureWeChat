package com.caijia.selectpicture.ui.itemDelegate;

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
import com.caijia.selectpicture.utils.ToastManager;

import java.util.ArrayList;
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

    /**
     * 多选时最大选择数量
     */
    private int maxSelectNum;

    private int shadowColorNormal;
    private int shadowColorSelect;

    private OnItemSelectedListener itemSelectedListener;
    private OnItemClickListener itemClickListener;
    private List<MediaBean> sourceData;

    public VideoItemDelegate(Context context, boolean canMultiSelect, int maxSelectNum,
                             OnItemSelectedListener itemSelectedListener,
                             OnItemClickListener itemClickListener) {
        this.canMultiSelect = canMultiSelect;
        this.maxSelectNum = maxSelectNum;
        this.itemSelectedListener = itemSelectedListener;
        this.itemClickListener = itemClickListener;
        shadowColorNormal = ContextCompat.getColor(context, R.color.color_ms_shadow_normal);
        shadowColorSelect = ContextCompat.getColor(context, R.color.color_ms_shadow_select);
    }

    @Override
    public VideoVH onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_select_video, parent, false);
        return new VideoVH(view,((RecyclerView)parent).getAdapter(),maxSelectNum,sourceData,
                itemSelectedListener,itemClickListener);
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
        holder.setExtraInfo(item);
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

    public void setSourceData(List<MediaBean> sourceData) {
        this.sourceData = sourceData;
    }

    static class VideoVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageView;
        private View shadowView;
        private TextView selectTv;
        private TextView videoTime;
        private MediaBean item;
        private RecyclerView.Adapter adapter;
        private List<MediaBean> dataSource;
        private int maxSelectNum;
        private List<MediaBean> selectedItems;
        private OnItemSelectedListener itemSelectedListener;
        private OnItemClickListener itemClickListener;

        VideoVH(View itemView, RecyclerView.Adapter adapter, int maxSelectNum,
                List<MediaBean> sourceData, OnItemSelectedListener itemSelectedListener,
                OnItemClickListener itemClickListener) {
            super(itemView);
            this.adapter = adapter;
            this.maxSelectNum = maxSelectNum;
            this.itemSelectedListener = itemSelectedListener;
            this.itemClickListener = itemClickListener;
            this.dataSource = sourceData;
            selectedItems = new ArrayList<>();
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            shadowView = itemView.findViewById(R.id.shadow_view);
            selectTv = (TextView) itemView.findViewById(R.id.select_tv);
            videoTime = (TextView) itemView.findViewById(R.id.video_time_tv);
        }

        public void setExtraInfo(MediaBean item) {
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            if (item == null) {
                return;
            }

            //多选选择框
            if (v == selectTv) {
                if (selectIsValid()) {
                    item.setSelect(!item.isSelect());
                    if (itemSelectedListener != null) {
                        itemSelectedListener.onItemSelected(getSelectedItems());
                    }
                    adapter.notifyItemChanged(getAdapterPosition(), item);

                }else{
                    ToastManager.getInstance(v.getContext()).showToast("超出最大选择数量");
                }

            } else if (v == itemView) {
                //单选
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(getAdapterPosition(),item,getSelectedItems());
                }
            }
        }

        /**
         * 选择是否合法
         * @return
         */
        private boolean selectIsValid(){
            if (dataSource == null) {
                return false;
            }

            boolean select = item.isSelect();
            int selectedNum = 0;
            for (MediaBean bean : dataSource) {
                if (bean != null && bean.isSelect()) {
                    selectedNum++;
                }

                if (selectedNum >= maxSelectNum) {
                    break;
                }
            }

            if (!select && selectedNum >= maxSelectNum) {
                return false;
            }

            return true;
        }

        private List<MediaBean> getSelectedItems() {
            if (dataSource == null) {
                return selectedItems;
            }
            selectedItems.clear();
            for (Object o : dataSource) {
                if (o != null && o instanceof MediaBean) {
                    MediaBean bean = (MediaBean) o;
                    if (bean.isSelect()) {
                        selectedItems.add(bean);
                    }
                }
            }
            return selectedItems;
        }
    }

    public interface OnItemSelectedListener{

        void onItemSelected(List<MediaBean> selectedItems);
    }

    public interface OnItemClickListener {

        /**
         * @param position
         * @param item 当前点击的item
         * @param selectedItems 多选时选中的item
         */
        void onItemClick(int position, MediaBean item, List<MediaBean> selectedItems);
    }
}
