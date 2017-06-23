package com.caijia.selectpicture.ui;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.caijia.selectpicture.R;
import com.caijia.selectpicture.bean.PictureGroup;
import com.caijia.selectpicture.utils.DeviceUtil;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

/**
 * Created by cai.jia on 2017/6/22 0022
 */

public class PictureGroupDialog extends DialogFragment implements View.OnClickListener {

    private static final String PICTURE_GROUP = "params:picture_group";
    private static final String TAG_PICTURE_GROUP_FRAGMENT = "tag:f_picture_group";
    private FrameLayout fragmentContainer;
    private View dismissSpace;

    private List<PictureGroup> groupList;
    private TextView selPicGroupTv;

    public static PictureGroupDialog getInstance(List<PictureGroup> pictureGroups) {
        PictureGroupDialog f = new PictureGroupDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList(PICTURE_GROUP, (ArrayList<? extends Parcelable>) pictureGroups);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.PictureGroupDialogTheme);
        Bundle args = getArguments();
        if (args != null) {
            groupList = args.getParcelableArrayList(PICTURE_GROUP);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_picture_group, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        selPicGroupTv = (TextView) view.findViewById(R.id.select_picture_group_tv);
        fragmentContainer = (FrameLayout) view.findViewById(R.id.fragment_container);
        dismissSpace = view.findViewById(R.id.dismiss_space);

        setFragmentContainerHeight(groupList.size());
        setDismissSpaceHeight();

        selPicGroupTv.setOnClickListener(this);
        dismissSpace.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Window window = getDialog().getWindow();
        if (window == null) {
            return;
        }
        WindowManager.LayoutParams params = window.getAttributes();
        params.dimAmount = 0;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        getDialog().setCanceledOnTouchOutside(false);
        window.addFlags(FLAG_DIM_BEHIND | FLAG_NOT_FOCUSABLE);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, WRAP_CONTENT);
    }

    private void setDimAmount(float dimAmount) {
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setDimAmount(dimAmount);
        }
    }

    private void toggleFragment() {
        PictureGroupFragment groupFragment = (PictureGroupFragment) getChildFragmentManager()
                .findFragmentByTag(TAG_PICTURE_GROUP_FRAGMENT);
        if (groupFragment == null) {
            groupFragment = PictureGroupFragment.getInstance(groupList);
            setVisibility(true);
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, groupFragment, TAG_PICTURE_GROUP_FRAGMENT)
                    .commitNowAllowingStateLoss();

        } else {
            if (groupFragment.isVisible() && !groupFragment.isRemoving()) {
                setVisibility(false);
                getChildFragmentManager()
                        .beginTransaction()
                        .hide(groupFragment)
                        .commitNowAllowingStateLoss();

            }else{
                setVisibility(true);
                getChildFragmentManager()
                        .beginTransaction()
                        .show(groupFragment)
                        .commitNowAllowingStateLoss();
            }
        }
    }

    private void setVisibility(boolean visible) {
        if (visible) {
            setDimAmount(0.4f);

        }else{
            setDimAmount(0f);
        }
    }

    private void setFragmentContainerHeight(int itemCount) {
        int itemHeight = DeviceUtil.dpToPx(getContext(), 96);
        int maxHeight = DeviceUtil.getScreenHeight(getContext())
                - DeviceUtil.dpToPx(getContext(), 48 * 2) //title bar , bottom bar
                - DeviceUtil.getStatusBarHeight(getContext())// status bar
                - DeviceUtil.dpToPx(getContext(), 48); //spacing
        if (itemCount * itemHeight > maxHeight) {
            fragmentContainer.getLayoutParams().height = maxHeight;
        } else {
            fragmentContainer.getLayoutParams().height = itemCount * itemHeight;
        }
    }

    private void setDismissSpaceHeight() {
        dismissSpace.getLayoutParams().height = DeviceUtil.getScreenHeight(getContext())
                - fragmentContainer.getLayoutParams().height
                - DeviceUtil.dpToPx(getContext(), 48)//bottom bar;
                - DeviceUtil.getStatusBarHeight(getContext());// status bar
    }

    @Override
    public void onClick(View v) {
        if (v == selPicGroupTv) {
            toggleFragment();

        } else if (v == dismissSpace) {
            toggleFragment();
        }
    }
}
