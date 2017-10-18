package com.caijia.selectpicture.ui;

import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.caijia.selectpicture.R;
import com.caijia.selectpicture.bean.MediaGroup;
import com.caijia.selectpicture.ui.adapter.MediaGroupAdapter;
import com.caijia.selectpicture.ui.itemDelegate.MediaGroupItemDelegate;
import com.caijia.selectpicture.utils.DeviceUtil;
import com.caijia.selectpicture.widget.LineItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cai.jia on 2017/6/22 0022
 */

public class MediaGroupFragment extends Fragment implements View.OnClickListener {

    private static final String MEDIA_GROUP = "params:picture_group";
    public OnClickShadowListener onClickShadowListener;
    private MediaGroupItemDelegate.OnItemClickListener onItemClickListener;
    private View shadowView;
    private LinearLayout llGroupRoot;
    private OnAnimatorListener onAnimatorListener;
    private List<MediaGroup> groupList;

    public static MediaGroupFragment getInstance(List<MediaGroup> groupList) {
        MediaGroupFragment f = new MediaGroupFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(MEDIA_GROUP, (ArrayList<? extends Parcelable>) groupList);
        f.setArguments(args);
        return f;
    }

    public void setOnItemClickListener(MediaGroupItemDelegate.OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            groupList = args.getParcelableArrayList(MEDIA_GROUP);
        }
    }

    @Override
    public Animation onCreateAnimation(int transit, final boolean enter, int nextAnim) {
        if (nextAnim > 0) {
            Animation animation = AnimationUtils.loadAnimation(getContext(), nextAnim);
            if (onAnimatorListener != null && animation != null) {
                onAnimatorListener.onFragmentCreateAnimation(animation, enter);
            }
            return animation;

        }else{
            return null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_media_group, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        shadowView = view.findViewById(R.id.shadow_view);
        llGroupRoot = (LinearLayout) view.findViewById(R.id.ll_group_root);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        MediaGroupAdapter mAdapter = new MediaGroupAdapter(getContext(), onItemClickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        int spacing = DeviceUtil.dpToPx(getContext(), 0.5f);
        int color = ContextCompat.getColor(getContext(), R.color.color_ms_divider);
        recyclerView.addItemDecoration(new LineItemDecoration(LineItemDecoration.VERTICAL,spacing,color));
        recyclerView.setAdapter(mAdapter);

        if (groupList != null) {
            mAdapter.updateItems(groupList);
            setRecyclerViewHeight(recyclerView,savedInstanceState);
        }
        shadowView.setOnClickListener(this);
    }

    private int recyclerViewHeight;
    private static final String RECYCLER_VIEW_MAX_HEIGHT = "params:recyclerViewHeight";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(RECYCLER_VIEW_MAX_HEIGHT,recyclerViewHeight);
    }

    private void setRecyclerViewHeight(final RecyclerView view, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            int recyclerViewHeight = savedInstanceState.getInt(RECYCLER_VIEW_MAX_HEIGHT);
            view.getLayoutParams().height = recyclerViewHeight;
            view.requestLayout();

        }else{
            view.post(new Runnable() {
                @Override
                public void run() {
                    int viewMeasuredHeight = view.getMeasuredHeight();
                    int maxHeight = llGroupRoot.getMeasuredHeight()
                            - DeviceUtil.dpToPx(view.getContext(), 48) //title bar
                            - DeviceUtil.dpToPx(view.getContext(), 48); //spacing

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        maxHeight = maxHeight - DeviceUtil.getStatusBarHeight(getContext()); //status bar
                    }

                    if (viewMeasuredHeight < maxHeight) {
                        view.getLayoutParams().height = viewMeasuredHeight;
                    } else {
                        view.getLayoutParams().height = maxHeight;
                    }

                    recyclerViewHeight = view.getLayoutParams().height;
                    view.requestLayout();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if (v == shadowView) {
            if (onClickShadowListener != null) {
                onClickShadowListener.onClickShadow(v);
            }
        }
    }

    public void setOnAnimatorEndListener(OnAnimatorListener animatorListener) {
        this.onAnimatorListener = animatorListener;
    }

    public void setOnClickShadowListener(OnClickShadowListener onClickListener) {
        this.onClickShadowListener = onClickListener;
    }

    public interface OnAnimatorListener {

        void onFragmentCreateAnimation(Animation animation , boolean enter);
    }

    public interface OnClickShadowListener {
        void onClickShadow(View view);
    }
}
