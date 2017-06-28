package com.caijia.selectpicture.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
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
import com.caijia.selectpicture.ui.adapter.itemDelegate.MediaGroupItemDelegate;
import com.caijia.selectpicture.utils.DeviceUtil;
import com.caijia.selectpicture.utils.MediaManager;
import com.caijia.selectpicture.widget.GridSpacingItemDecoration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static com.caijia.selectpicture.utils.MediaType.IMAGE;
import static com.caijia.selectpicture.utils.MediaType.IMAGE_VIDEO;
import static com.caijia.selectpicture.utils.MediaType.VIDEO;

/**
 * Created by cai.jia on 2017/6/22 0022
 */

public class SelectMediaActivity extends AppCompatActivity implements
        MediaManager.OnGetLocalMediaListener, View.OnClickListener,
        MediaGroupFragment.OnAnimatorListener, MediaGroupItemDelegate.OnItemClickListener,
        MediaGroupFragment.OnClickShadowListener, MediaAdapter.OnItemClickListener {

    public static final String RESULT_MEDIA = "result:media";
    private static final String TAG_PICTURE_GROUP = "tag:picture_group";
    private static final String PARAMS_MEDIA_TYPE = "params:media_type";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({VIDEO, IMAGE, IMAGE_VIDEO})
    public @interface MediaType {

    }

    private MediaAdapter mAdapter;
    private TextView titleTv;
    private TextView selectPictureGroupTv;
    private FrameLayout pictureGroupContainer;
    private List<MediaGroup> groupList;
    private MediaGroupFragment groupFragment;
    private boolean isShowDialog;
    private int mediaType = IMAGE_VIDEO;

    public static Intent getIntent(Context context,@MediaType int type) {
        Intent i = new Intent(context, SelectMediaActivity.class);
        i.putExtra(PARAMS_MEDIA_TYPE, type);
        return i;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_media);
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            mediaType = intent.getExtras().getInt(PARAMS_MEDIA_TYPE);
        }

        selectPictureGroupTv = (TextView) findViewById(R.id.select_picture_group_tv);
        pictureGroupContainer = (FrameLayout) findViewById(R.id.picture_group_fragment_container);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        titleTv = (TextView) findViewById(R.id.title_tv);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_sm_camera_video);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setSupportActionBar(toolbar);

        mAdapter = new MediaAdapter(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        int spacing = DeviceUtil.dpToPx(this, 2);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spacing, true, Color.TRANSPARENT));
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);

        loadMedia();
        selectPictureGroupTv.setOnClickListener(this);
    }

    private void loadMedia() {
        MediaManager.getInstance().getLocalMedia(this,mediaType,this);
    }

    @Override
    public void onGetMediaFinish(List<MediaBean> list, List<MediaGroup> groupList) {
        this.groupList = groupList;
        mAdapter.updateItems(list);
    }

    @Override
    public void onClick(View v) {
        if (v == selectPictureGroupTv) {
            toggleFragment();
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
        mAdapter.updateItems(group.getMediaList());
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
    public void onItemClick(int position, MediaBean item) {
        Intent i = new Intent();
        i.putExtra(RESULT_MEDIA, item);
        setResult(RESULT_OK, i);
        finish();
    }
}
