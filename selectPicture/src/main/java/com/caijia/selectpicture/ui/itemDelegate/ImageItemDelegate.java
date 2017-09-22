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

    /**
     * 多选时最大选择数量
     */
    private int maxSelectNum;

    private int shadowColorNormal;
    private int shadowColorSelect;

    private OnImageSelectedListener onImageSelectedListener;
    private OnItemClickListener onItemClickListener;
    private List<MediaBean> sourceData;

    public ImageItemDelegate(Context context, boolean canMultiSelect, int maxSelectNum,
                             OnImageSelectedListener onImageSelectedListener,
                             OnItemClickListener onItemClickListener) {
        this.canMultiSelect = canMultiSelect;
        this.maxSelectNum = maxSelectNum;
        this.onImageSelectedListener = onImageSelectedListener;
        this.onItemClickListener = onItemClickListener;
        shadowColorNormal = ContextCompat.getColor(context, R.color.color_ms_shadow_normal);
        shadowColorSelect = ContextCompat.getColor(context, R.color.color_ms_shadow_select);
    }

    @Override
    public ImageVH onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_select_image, parent, false);
        return new ImageVH(view,((RecyclerView)parent).getAdapter(),maxSelectNum,sourceData,
                onImageSelectedListener, onItemClickListener);
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

        holder.setExtraInfo(item);
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

    public void setSourceData(List<MediaBean> sourceData) {
        this.sourceData = sourceData;
    }

    static class ImageVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageView;
        private View shadowView;
        private TextView selectTv;
        private MediaBean item;
        private List<MediaBean> dataSource;
        private int maxSelectNum;
        private RecyclerView.Adapter adapter;
        private OnImageSelectedListener onImageSelectedListener;
        private OnItemClickListener onItemClickListener;
        private List<MediaBean> selectedImages;

        ImageVH(View itemView, RecyclerView.Adapter adapter, int maxSelectNum,
                List<MediaBean> sourceData, OnImageSelectedListener onImageSelectedListener,
                OnItemClickListener onItemClickListener) {
            super(itemView);
            this.adapter = adapter;
            this.maxSelectNum = maxSelectNum;
            this.onImageSelectedListener = onImageSelectedListener;
            this.onItemClickListener = onItemClickListener;
            this.dataSource = sourceData;
            selectedImages = new ArrayList<>();
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            shadowView = itemView.findViewById(R.id.shadow_view);
            selectTv = (TextView) itemView.findViewById(R.id.select_tv);
        }

        public void setExtraInfo(MediaBean item) {
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            if (item == null) {
                return;
            }

            if (v == selectTv) {
                if (selectIsValid()) {
                    item.setSelect(!item.isSelect());
                    if (onImageSelectedListener != null) {
                        onImageSelectedListener.onImageSelected(getSelectedImages());
                    }
                    adapter.notifyItemChanged(getAdapterPosition(), item);

                }else{
                    ToastManager.getInstance(v.getContext()).showToast("超出最大选择数量");
                }

            } else if (v == itemView) {
                //多选时预览,单选时返回
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(getAdapterPosition(),item,getSelectedImages());
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

        private List<MediaBean> getSelectedImages() {
            if (dataSource == null) {
                return selectedImages;
            }
            selectedImages.clear();
            for (Object o : dataSource) {
                if (o != null && o instanceof MediaBean) {
                    MediaBean bean = (MediaBean) o;
                    if (bean.isSelect()) {
                        selectedImages.add(bean);
                    }
                }
            }
            return selectedImages;
        }
    }

    public interface OnImageSelectedListener{

        void onImageSelected(List<MediaBean> selectedItems);
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
