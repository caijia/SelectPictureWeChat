package com.caijia.selectpicture.ui;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caijia.selectpicture.R;
import com.caijia.selectpicture.bean.PictureGroup;
import com.caijia.selectpicture.ui.adapter.PictureGroupAdapter;
import com.caijia.selectpicture.utils.PictureManager;
import com.caijia.selectpicture.widget.LineItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cai.jia on 2017/6/22 0022
 */

public class PictureGroupFragment extends Fragment implements PictureManager.OnGetLocalPictureListener {

    private static final String PICTURE_GROUP = "params:picture_group";

    private PictureGroupAdapter mAdapter;
    private List<PictureGroup> groupList;
    private PictureGroupAdapter.OnItemClickListener onItemClickListener;

    public static PictureGroupFragment getInstance(List<PictureGroup> pictureGroups) {
        PictureGroupFragment f = new PictureGroupFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(PICTURE_GROUP, (ArrayList<? extends Parcelable>) pictureGroups);
        f.setArguments(args);
        return f;
    }

    public void setOnItemClickListener(PictureGroupAdapter.OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            groupList = args.getParcelableArrayList(PICTURE_GROUP);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_picture_group, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mAdapter = new PictureGroupAdapter(getContext());
        mAdapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new LineItemDecoration(getContext(),LineItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        if (groupList != null) {
            mAdapter.updateItems(groupList);

        }else{
            loadPicture();
        }
    }

    private void loadPicture() {
        PictureManager.getInstance().getLocalPicture(getContext(),this);
    }

    @Override
    public void onGetPictureFinish(List<String> list, List<PictureGroup> groupList) {
        if (groupList != null) {
            mAdapter.updateItems(groupList);
        }
    }
}
