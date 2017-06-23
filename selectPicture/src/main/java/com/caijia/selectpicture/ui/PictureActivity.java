package com.caijia.selectpicture.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.caijia.selectpicture.R;
import com.caijia.selectpicture.bean.PictureGroup;
import com.caijia.selectpicture.ui.adapter.PictureAdapter;
import com.caijia.selectpicture.utils.DeviceUtil;
import com.caijia.selectpicture.utils.PictureManager;
import com.caijia.selectpicture.widget.GridSpacingItemDecoration;

import java.util.List;

/**
 * Created by cai.jia on 2017/6/22 0022
 */

public class PictureActivity extends AppCompatActivity implements PictureManager.OnGetLocalPictureListener{

    private static final String TAG_PICTURE_GROUP = "tag:picture_group";

    private PictureAdapter mAdapter;
    private TextView titleTv;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        titleTv = (TextView) findViewById(R.id.title_tv);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle("");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setSupportActionBar(toolbar);

        mAdapter = new PictureAdapter(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        int spacing = DeviceUtil.dpToPx(this, 2);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spacing,true, Color.TRANSPARENT));
        recyclerView.setAdapter(mAdapter);

        loadPicture();
    }

    private void loadPicture() {
        PictureManager.getInstance().getLocalPicture(this,this);
    }

    PictureGroupDialog groupDialog;

    @Override
    public void onGetPictureFinish(List<String> list, List<PictureGroup> groupList) {
        mAdapter.updateItems(list);

        groupDialog = (PictureGroupDialog) getSupportFragmentManager()
                .findFragmentByTag(TAG_PICTURE_GROUP);
        if (groupDialog == null) {
            groupDialog = PictureGroupDialog.getInstance(groupList);
        }
        groupDialog.show(getSupportFragmentManager(),TAG_PICTURE_GROUP);
    }
}
