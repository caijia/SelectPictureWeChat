package com.caijia.selectpicture.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.caijia.selectpicture.R;
import com.caijia.selectpicture.bean.MediaBean;
import com.caijia.selectpicture.bean.MediaGroup;
import com.caijia.selectpicture.ui.adapter.MediaAdapter;
import com.caijia.selectpicture.ui.adapter.itemDelegate.ImageItemDelegate;
import com.caijia.selectpicture.ui.adapter.itemDelegate.MediaGroupItemDelegate;
import com.caijia.selectpicture.ui.adapter.itemDelegate.TakePictureItemDelegate;
import com.caijia.selectpicture.utils.CameraHelper;
import com.caijia.selectpicture.utils.DeviceUtil;
import com.caijia.selectpicture.utils.FileUtil;
import com.caijia.selectpicture.utils.MediaManager;
import com.caijia.selectpicture.utils.MediaType;
import com.caijia.selectpicture.widget.GridSpacingItemDecoration;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.caijia.selectpicture.utils.MediaType.IMAGE;
import static com.caijia.selectpicture.utils.MediaType.IMAGE_VIDEO;
import static com.caijia.selectpicture.utils.MediaType.VIDEO;

/**
 * Created by cai.jia on 2017/6/22 0022
 */

public class SelectMediaActivity extends AppCompatActivity implements
        MediaManager.OnGetLocalMediaListener, View.OnClickListener,
        MediaGroupFragment.OnAnimatorListener, MediaGroupItemDelegate.OnItemClickListener,
        MediaGroupFragment.OnClickShadowListener, MediaAdapter.OnItemClickListener,
        TakePictureItemDelegate.OnTakePictureListener, ImageItemDelegate.OnImageSelectedListener {

    public static final String RESULT_MEDIA = "result:media";
    public static final String RESULT_MULTI_MEDIA = "result:multi_media";
    private static final int REQ_CAMERA = 201;
    private static final String TAG_PICTURE_GROUP = "tag:picture_group";
    private static final String PARAMS_MEDIA_TYPE = "params:media_type";
    private static final String PARAMS_MULTI_SELECT = "params:can_multi_select";
    private static final String PARAMS_HAS_CAMERA = "params:has_camera";
    private static final String PARAMS_CLIP_IMAGE = "params:is_clip_image";
    private static final String PARAMS_MAX_SELECT_NUM = "params:max_select_num";

    /**
     * 选择类型,图片或视频
     */
    private int mediaType = IMAGE;

    /**
     * 是否可以多选
     */
    private boolean canMultiSelect;

    /**
     * 是否需要裁剪
     */
    private boolean isClipImage;

    /**
     * 多选时最大选择数量
     */
    private int maxSelectNum;

    /**
     * 是否有照相功能
     */
    private boolean hasCamera;
    private File takePictureSaveFile;
    private MediaAdapter mMediaAdapter;
    private TextView titleTv;
    private TextView selectPictureGroupTv;
    private FrameLayout pictureGroupContainer;
    private TextView tvMultiSelect;

    private List<MediaGroup> groupList;
    private MediaGroupFragment groupFragment;
    private boolean isShowDialog;
    /**
     * 选中的Items
     */
    private List<MediaBean> selectedItems;

    @Override
    public void onTakePicture() {
        String fileName = String.format("%s.jpg", UUID.randomUUID().toString().replaceAll("-", ""));
        takePictureSaveFile = FileUtil.createDiskCacheFile(this, "takePicture", fileName);
        if (takePictureSaveFile == null) {
            return;
        }
        CameraHelper.getInstance().takePicture(this, takePictureSaveFile.getAbsolutePath(), REQ_CAMERA);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_media);
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            mediaType = intent.getExtras().getInt(PARAMS_MEDIA_TYPE);
            canMultiSelect = intent.getExtras().getBoolean(PARAMS_MULTI_SELECT);
            isClipImage = intent.getExtras().getBoolean(PARAMS_CLIP_IMAGE);
            maxSelectNum = intent.getExtras().getInt(PARAMS_MAX_SELECT_NUM);
            hasCamera = intent.getExtras().getBoolean(PARAMS_HAS_CAMERA);
        }

        tvMultiSelect = (TextView) findViewById(R.id.tv_multi_select);
        tvMultiSelect.setVisibility(canMultiSelect ? View.VISIBLE : View.GONE);
        selectPictureGroupTv = (TextView) findViewById(R.id.select_picture_group_tv);
        pictureGroupContainer = (FrameLayout) findViewById(R.id.picture_group_fragment_container);

        titleTv = (TextView) findViewById(R.id.title_tv);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mMediaAdapter = new MediaAdapter(this, canMultiSelect, maxSelectNum, this, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        int spacing = DeviceUtil.dpToPx(this, 2);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spacing, true, Color.TRANSPARENT));
        recyclerView.setAdapter(mMediaAdapter);
        mMediaAdapter.setOnItemClickListener(this);

        loadMedia();
        selectPictureGroupTv.setOnClickListener(this);
        tvMultiSelect.setOnClickListener(this);
    }

    private void loadMedia() {
        MediaManager.getInstance().getLocalMedia(this, mediaType, this);
    }

    @Override
    public void onGetMediaFinish(List<MediaBean> list, List<MediaGroup> groupList) {
        if (hasCamera) {
            //第一项为照相图标
            if (groupList != null && !groupList.isEmpty()) {
                MediaGroup mediaGroup = groupList.get(0);
                MediaBean mediaBean = mediaGroup.getFirst();
                if (mediaBean != null && mediaBean.getMediaType() != MediaType.CAMERA) {
                    mediaGroup.addMediaBean(0, new MediaBean(MediaType.CAMERA));
                }
            }
            list.add(0, new MediaBean(MediaType.CAMERA));
        }

        this.groupList = groupList;
        mMediaAdapter.updateItems(list);
    }

    @Override
    public void onClick(View v) {
        if (v == selectPictureGroupTv) {
            toggleFragment();

        } else if (v == tvMultiSelect) {
            //多选确定
            Intent i = new Intent();
            i.putParcelableArrayListExtra(RESULT_MULTI_MEDIA, (ArrayList<? extends Parcelable>) selectedItems);
            setResult(RESULT_OK, i);
            finish();
        }
    }

    private void toggleFragment() {
        if (groupFragment == null) {
            groupFragment = (MediaGroupFragment) getSupportFragmentManager()
                    .findFragmentByTag(TAG_PICTURE_GROUP);
        }
        if (groupFragment == null || !groupFragment.isAdded()) {
            groupFragment = MediaGroupFragment.getInstance(groupList);
            setGroupFragmentListener();
            FragmentTransaction transaction = getTransaction();
            transaction
                    .add(R.id.picture_group_fragment_container, groupFragment, TAG_PICTURE_GROUP)
                    .commitNowAllowingStateLoss();
            isShowDialog = true;

        } else {
            setGroupFragmentListener();
            if (groupFragment.isVisible() && !groupFragment.isRemoving()) {
                FragmentTransaction transaction = getTransaction();
                transaction.hide(groupFragment).commitNowAllowingStateLoss();
                isShowDialog = false;

            } else {
                FragmentTransaction transaction = getTransaction();
                transaction.show(groupFragment).commitNowAllowingStateLoss();
                isShowDialog = true;
            }
        }
    }

    private FragmentTransaction getTransaction() {
        return getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.translate_enter_from_bottom_animation,
                        R.anim.translate_exit_to_bottom_animation);
    }

    private void setGroupFragmentListener() {
        groupFragment.setOnAnimatorEndListener(this);
        groupFragment.setOnClickShadowListener(this);
        groupFragment.setOnItemClickListener(this);
    }

    @Override
    public void onAnimatorEnd(boolean enter) {
        if (!enter) {
            pictureGroupContainer.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void onAnimatorStart(boolean enter) {
        if (enter) {
            pictureGroupContainer.setBackgroundColor(Color.parseColor("#66000000"));
        }
    }

    @Override
    public void onItemClick(int position, MediaGroup group) {
        toggleFragment();
        titleTv.setText(group.getGroupName());
        selectPictureGroupTv.setText(group.getGroupName());
        mMediaAdapter.updateItems(group.getMediaList());
    }

    @Override
    public void onClickShadow(View view) {
        toggleFragment();
    }

    @Override
    public void onBackPressed() {
        if (isShowDialog) {
            toggleFragment();

        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQ_CAMERA: {
                if (takePictureSaveFile == null) {
                    return;
                }
                Uri contentUri = Uri.fromFile(takePictureSaveFile);
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
                sendBroadcast(mediaScanIntent);

                if (!isClipImage) {
                    //照相完成,不动态更新相册
                    sendMediaBean(new MediaBean(takePictureSaveFile.getPath(), MediaType.IMAGE));
                    finish();

                }else{
                    //裁剪
                    clipImage(takePictureSaveFile.getPath());
                }
                break;
            }
        }
    }

    private void clipImage(String imagePath) {
        Intent i = ClipPictureActivity.getIntent(this, imagePath);
        startActivity(i);
    }

    private void sendMediaBean(MediaBean item) {
        Intent i = new Intent();
        i.putExtra(RESULT_MEDIA, item);
        setResult(RESULT_OK, i);
    }

    @Override
    public void onItemClick(int position, MediaBean item,List<MediaBean> selectedItems) {
        if (!isClipImage || item.getMediaType() != MediaType.IMAGE) {
            sendMediaBean(item);
            finish();

        }

        if (isClipImage && item.getMediaType() == MediaType.IMAGE) {
            //裁剪
            clipImage(item.getPath());
        }
    }

    @Override
    public void onImageSelected(List<MediaBean> selectedItems) {
        this.selectedItems = selectedItems;
        int size = selectedItems.size();
        tvMultiSelect.setText(size > 0
                ? MessageFormat.format("确定({0}/{1})", size, maxSelectNum)
                : "确定");
        tvMultiSelect.setEnabled(size > 0);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({VIDEO, IMAGE, IMAGE_VIDEO})
    public @interface FilterMediaType {

    }

    public static class IntentBuilder {

        Intent i;

        public IntentBuilder(Context context) {
            i = new Intent(context, SelectMediaActivity.class);
        }

        public IntentBuilder mediaType(@FilterMediaType int type) {
            i.putExtra(PARAMS_MEDIA_TYPE, type);
            return this;
        }

        public IntentBuilder canMultiSelect(boolean canMultiSelect) {
            i.putExtra(PARAMS_MULTI_SELECT, canMultiSelect);
            return this;
        }

        public IntentBuilder hasCamera(boolean hasCamera) {
            i.putExtra(PARAMS_HAS_CAMERA, hasCamera);
            return this;
        }

        public IntentBuilder isClipImage(boolean isClipImage) {
            i.putExtra(PARAMS_CLIP_IMAGE, isClipImage);
            return this;
        }

        public IntentBuilder maxSelectNum(int maxSelectNum) {
            i.putExtra(PARAMS_MAX_SELECT_NUM, maxSelectNum);
            return this;
        }

        public Intent build() {
            return i;
        }
    }
}
