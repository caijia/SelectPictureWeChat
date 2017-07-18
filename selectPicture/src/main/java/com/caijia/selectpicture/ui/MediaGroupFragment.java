package com.caijia.selectpicture.ui;

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
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.caijia.selectpicture.R;
import com.caijia.selectpicture.bean.MediaGroup;
import com.caijia.selectpicture.ui.adapter.MediaGroupAdapter;
import com.caijia.selectpicture.ui.adapter.itemDelegate.MediaGroupItemDelegate;
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
        Animation animation = AnimationUtils.loadAnimation(getContext(), nextAnim);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (onAnimatorListener != null) {
                    onAnimatorListener.onAnimatorStart(enter);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (onAnimatorListener != null) {
                    onAnimatorListener.onAnimatorEnd(enter);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        return animation;
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
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        MediaGroupAdapter mAdapter = new MediaGroupAdapter(getContext(), onItemClickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        int spacing = DeviceUtil.dpToPx(getContext(), 0.5f);
        int color = ContextCompat.getColor(getContext(), R.color.color_ms_divider);
        recyclerView.addItemDecoration(new LineItemDecoration(LineItemDecoration.VERTICAL,spacing,color));
        recyclerView.setAdapter(mAdapter);

        if (groupList != null) {
            setRecyclerViewHeight(recyclerView);
            mAdapter.updateItems(groupList);
        }
        shadowView.setOnClickListener(this);
    }

    private void setRecyclerViewHeight(final RecyclerView view) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int viewMeasuredHeight = view.getMeasuredHeight();
                int maxHeight = DeviceUtil.getScreenHeight(view.getContext())
                        - DeviceUtil.dpToPx(view.getContext(), 48 * 2) //title bar , bottom bar
                        - DeviceUtil.getStatusBarHeight(view.getContext())// status bar
                        - DeviceUtil.dpToPx(view.getContext(), 48); //spacing
                if (viewMeasuredHeight > maxHeight) {
                    view.getLayoutParams().height = maxHeight;

                } else {
                    view.getLayoutParams().height = viewMeasuredHeight;
                }
                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
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

        void onAnimatorEnd(boolean enter);

        void onAnimatorStart(boolean enter);
    }

    public interface OnClickShadowListener {
        void onClickShadow(View view);
    }
}
