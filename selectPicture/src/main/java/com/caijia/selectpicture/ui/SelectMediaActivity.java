package com.caijia.selectpicture.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.caijia.selectpicture.R;
import com.caijia.selectpicture.bean.MediaBean;
import com.caijia.selectpicture.bean.MediaGroup;
import com.caijia.selectpicture.ui.adapter.MediaAdapter;
import com.caijia.selectpicture.ui.itemDelegate.MediaGroupItemDelegate;
import com.caijia.selectpicture.ui.itemDelegate.TakePictureItemDelegate;
import com.caijia.selectpicture.utils.CameraHelper;
import com.caijia.selectpicture.utils.DeviceUtil;
import com.caijia.selectpicture.utils.FileUtil;
import com.caijia.selectpicture.utils.MediaManager;
import com.caijia.selectpicture.utils.MediaType;
import com.caijia.selectpicture.utils.StatusBarUtil;
import com.caijia.selectpicture.widget.GridSpacingItemDecoration;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.caijia.selectpicture.utils.Constants.IMAGE_SAVE_DIR;
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
        TakePictureItemDelegate.OnTakePictureListener, MediaAdapter.OnItemSelectedListener {

    public static final String RESULT_MEDIA = "result:media";
    public static final String RESULT_MULTI_MEDIA = "result:multi_media";
    private static final int REQ_CAMERA = 201;
    private static final String TAG_PICTURE_GROUP = "tag:picture_group";
    private static final String PARAMS_MEDIA_TYPE = "params:media_type";
    private static final String PARAMS_MULTI_SELECT = "params:can_multi_select";
    private static final String PARAMS_HAS_CAMERA = "params:has_camera";
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
    private RelativeLayout selectPictureGroupRl;
    private FrameLayout pictureGroupContainer;
    private TextView tvMultiSelect;
    private LinearLayout bottomBarLl;

    private List<MediaGroup> groupList;
    private MediaGroupFragment groupFragment;
    private boolean isShowDialog;
    /**
     * 选中的Items
     */
    private List<MediaBean> selectedItems;
    private ValueAnimator shadowAlphaAnimator;

    @Override
    public void onTakePicture() {
        String fileName = String.format("%s.jpg", UUID.randomUUID().toString().replaceAll("-", ""));
        takePictureSaveFile = FileUtil.createPictureDiskFile(IMAGE_SAVE_DIR, fileName);
        if (takePictureSaveFile == null) {
            return;
        }
        CameraHelper.getInstance().takePicture(this, takePictureSaveFile.getAbsolutePath(), REQ_CAMERA);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_media);
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

        int statusBarColor = ContextCompat.getColor(this, R.color.color_ms_primary_dark);
        StatusBarUtil.setTranslucentStatus(this);
        StatusBarUtil.setStatusBarPlaceColor(this, false, statusBarColor);
        StatusBarUtil.addStatusBarHeightMarginTop(toolbar);

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            mediaType = intent.getExtras().getInt(PARAMS_MEDIA_TYPE);
            canMultiSelect = intent.getExtras().getBoolean(PARAMS_MULTI_SELECT);
            maxSelectNum = intent.getExtras().getInt(PARAMS_MAX_SELECT_NUM);
            hasCamera = intent.getExtras().getBoolean(PARAMS_HAS_CAMERA);
        }

        tvMultiSelect = (TextView) findViewById(R.id.tv_multi_select);
        tvMultiSelect.setVisibility(canMultiSelect ? View.VISIBLE : View.GONE);
        selectPictureGroupTv = (TextView) findViewById(R.id.select_picture_group_tv);
        selectPictureGroupRl = (RelativeLayout) findViewById(R.id.select_picture_group_rl);
        pictureGroupContainer = (FrameLayout) findViewById(R.id.picture_group_fragment_container);
        titleTv = (TextView) findViewById(R.id.title_tv);
        bottomBarLl = (LinearLayout) findViewById(R.id.bottom_bar_ll);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mMediaAdapter = new MediaAdapter(this, canMultiSelect, maxSelectNum, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        int spacing = DeviceUtil.dpToPx(this, 2);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spacing, true, Color.TRANSPARENT));
        recyclerView.setAdapter(mMediaAdapter);
        mMediaAdapter.setOnItemClickListener(this);
        mMediaAdapter.setOnItemSelectedListener(this);

        loadMedia();
        selectPictureGroupRl.setOnClickListener(this);
        tvMultiSelect.setOnClickListener(this);
    }

    private void loadMedia() {
        String title = "";
        switch (mediaType) {
            case IMAGE: {
                title = getResources().getString(R.string.sm_all_image);
                break;
            }

            case VIDEO: {
                title = getResources().getString(R.string.sm_all_video);
                break;
            }

            case IMAGE_VIDEO: {
                title = getResources().getString(R.string.sm_all_image_video);
                break;
            }
        }
        titleTv.setText(title);
        selectPictureGroupTv.setText(title);
        MediaManager.getInstance().getLocalMedia(this, mediaType, this);
    }

    @Override
    public void onGetMediaFinish(@NonNull List<MediaBean> list, @NonNull List<MediaGroup> groupList) {
        bottomBarLl.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
        if (hasCamera) {
            //第一项为照相图标
            if (!groupList.isEmpty()) {
                MediaGroup mediaGroup = groupList.get(0);
                MediaBean mediaBean = mediaGroup.getFirst();
                if (mediaBean != null && mediaBean.getMediaType() != MediaType.CAMERA) {
                    mediaGroup.addMediaBean(0, new MediaBean(MediaType.CAMERA));
                }
            }
            list.add(0, new MediaBean(MediaType.CAMERA));
        }

        this.groupList = groupList;
        mMediaAdapter.setSourceData(list);
        mMediaAdapter.updateItems(list);
    }

    @Override
    public void onClick(View v) {
        if (v == selectPictureGroupRl) {
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
            System.out.println(11);
        }
        if (groupFragment == null || !groupFragment.isAdded()) {
            groupFragment = MediaGroupFragment.getInstance(groupList);
            setGroupFragmentListener();
            FragmentTransaction transaction = getTransaction();
            transaction
                    .add(R.id.picture_group_fragment_container, groupFragment, TAG_PICTURE_GROUP)
                    .commitNowAllowingStateLoss();
            isShowDialog = true;
            System.out.println(22);

        } else {
            setGroupFragmentListener();
            if (isShowDialog) {
                FragmentTransaction transaction = getTransaction();
                transaction.hide(groupFragment).commitNowAllowingStateLoss();
                isShowDialog = false;
                System.out.println(33);

            } else {
                FragmentTransaction transaction = getTransaction();
                transaction.show(groupFragment).commitNowAllowingStateLoss();
                isShowDialog = true;
                System.out.println(44);
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
    public void onFragmentCreateAnimation(Animation animation, final boolean enter) {
        long duration = animation.getDuration();
        if (shadowAlphaAnimator != null) {
            shadowAlphaAnimator.cancel();
        }
        shadowAlphaAnimator = ValueAnimator.ofFloat(enter ? 0 : 1, enter ? 1 : 0);
        shadowAlphaAnimator.setDuration(duration);
        shadowAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                pictureGroupContainer.setAlpha(alpha);
            }
        });
        shadowAlphaAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                pictureGroupContainer.setBackgroundColor(Color.parseColor("#66000000"));
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                pictureGroupContainer.setAlpha(enter ? 1 : 0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        shadowAlphaAnimator.start();
    }

    /**
     * 点击图片组item
     *
     * @param position
     * @param group
     */
    @Override
    public void onItemClick(int position, MediaGroup group) {
        toggleFragment();
        titleTv.setText(group.getGroupName());
        selectPictureGroupTv.setText(group.getGroupName());
        mMediaAdapter.updateItems(group.getMediaList());
    }

    @Override
    public void onClickShadow(View view) {
        if (isShowDialog) {
            toggleFragment();
        }
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

                String fileName = takePictureSaveFile.getName();
                String filePath = takePictureSaveFile.getAbsolutePath();
                try {
                    MediaStore.Images.Media.insertImage(getContentResolver(), filePath, fileName, "");
                    Uri contentUri = Uri.fromFile(takePictureSaveFile);
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                //照相完成,不动态更新相册
                sendMediaBean(new MediaBean(takePictureSaveFile.getPath(), MediaType.IMAGE));
                finish();
                break;
            }
        }
    }

    private void sendMediaBean(MediaBean item) {
        Intent i = new Intent();
        i.putExtra(RESULT_MEDIA, item);
        setResult(RESULT_OK, i);
    }

    /**
     * 点击图片item
     *
     * @param position
     * @param item          当前点击的item
     * @param selectedItems 多选时选中的item,不是多选时返回为null
     */
    @Override
    public void onItemClick(int position, MediaBean item, List<MediaBean> selectedItems) {
        if (!canMultiSelect) {
            sendMediaBean(item);
            finish();
        }
    }

    /**
     * 多选选中回调
     *
     * @param selectedItems 选中items
     */
    @Override
    public void onItemSelected(List<MediaBean> selectedItems) {
        this.selectedItems = selectedItems;
        int size = selectedItems.size();
        tvMultiSelect.setText(size > 0
                ? MessageFormat.format(getString(R.string.select_pic_positive_multi), size, maxSelectNum)
                : getString(R.string.select_pic_positive));
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

        public IntentBuilder maxSelectNum(int maxSelectNum) {
            i.putExtra(PARAMS_MAX_SELECT_NUM, maxSelectNum);
            return this;
        }

        public Intent build() {
            return i;
        }
    }
}
