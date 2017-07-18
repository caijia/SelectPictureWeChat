package com.caijia.selectpicture.widget;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.Px;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

public class LineItemDecoration extends RecyclerView.ItemDecoration {

    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;
    private static final int KEY_IGNORE_DIVIDER = 1991419;
    private final Rect mBounds = new Rect();
    /**
     * Current orientation. Either {@link #HORIZONTAL} or {@link #VERTICAL}.
     */
    private int mOrientation;
    private int spacing;
    private Paint paint;
    private Paint bordPaint;
    private int bordColor;
    private int bordWidth;
    private boolean hasDecorTag;

    /**
     * Creates a divider {@link RecyclerView.ItemDecoration} that can be used with a
     *
     * @param orientation Divider orientation. Should be {@link #HORIZONTAL} or {@link #VERTICAL}.
     */
    public LineItemDecoration(int orientation, int spacing, @ColorInt int color) {
        this(orientation, spacing, color,false);
    }

    public LineItemDecoration(int orientation, int spacing, @ColorInt int color,boolean hasDecorTag) {
        this(orientation, spacing, color, 0, 0, hasDecorTag);
    }

    public LineItemDecoration(int orientation, int spacing, @ColorInt int color,
                              @ColorInt int bordColor, @Px int bordWidth) {
        this(orientation, spacing, color, bordColor, bordWidth, false);
    }

    /**
     * @param orientation Divider orientation
     * @param spacing     Divider spacing
     * @param color       Divider color
     * @param bordColor   Divider bordColor
     * @param bordWidth   Divider bordWidth
     * @param hasDecorTag 如果设置为true,应该在adapter里面为itemView设置tag为true表示该item会绘制分割线
     */
    public LineItemDecoration(int orientation, int spacing, @ColorInt int color,
                              @ColorInt int bordColor, @Px int bordWidth, boolean hasDecorTag) {
        setOrientation(orientation);
        this.spacing = spacing;
        this.bordColor = bordColor;
        this.bordWidth = bordWidth;
        this.hasDecorTag = hasDecorTag;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setColor(color);

        bordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bordPaint.setAntiAlias(true);
        bordPaint.setColor(bordColor);
    }

    /**
     * Sets the orientation for this divider. This should be called if
     * {@link RecyclerView.LayoutManager} changes orientation.
     *
     * @param orientation {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException(
                    "Invalid orientation. It should be either HORIZONTAL or VERTICAL");
        }
        mOrientation = orientation;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (parent.getLayoutManager() == null) {
            return;
        }
        if (mOrientation == VERTICAL) {
            drawVertical(c, parent, state);
        } else {
            drawHorizontal(c, parent, state);
        }
    }

    @SuppressLint("NewApi")
    private void drawVertical(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        canvas.save();
        final int left;
        final int right;
        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            canvas.clipRect(left, parent.getPaddingTop(), right,
                    parent.getHeight() - parent.getPaddingBottom());
        } else {
            left = 0;
            right = parent.getWidth();
        }

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);
            Object o = state.get(KEY_IGNORE_DIVIDER + position);
            boolean ignore = o != null && o instanceof Boolean && !(boolean) o;
            if (hasDecorTag && ignore) {
                continue;
            }

            parent.getDecoratedBoundsWithMargins(child, mBounds);
            final int bottom = mBounds.bottom + Math.round(ViewCompat.getTranslationY(child));
            final int top = bottom - spacing;
            canvas.drawRect(left, top, right, bottom, paint);
            if (bordWidth != 0 && bordColor != 0) {
                canvas.drawRect(left, top, right, top + bordWidth, bordPaint);
                canvas.drawRect(left, bottom - bordWidth, right, bottom, bordPaint);
            }
        }
        canvas.restore();
    }

    @SuppressLint("NewApi")
    private void drawHorizontal(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        canvas.save();
        final int top;
        final int bottom;
        if (parent.getClipToPadding()) {
            top = parent.getPaddingTop();
            bottom = parent.getHeight() - parent.getPaddingBottom();
            canvas.clipRect(parent.getPaddingLeft(), top,
                    parent.getWidth() - parent.getPaddingRight(), bottom);
        } else {
            top = 0;
            bottom = parent.getHeight();
        }

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);
            Object o = state.get(KEY_IGNORE_DIVIDER + position);
            boolean ignore = o != null && o instanceof Boolean && !(boolean) o;
            if (hasDecorTag && ignore) {
                continue;
            }

            parent.getLayoutManager().getDecoratedBoundsWithMargins(child, mBounds);
            final int right = mBounds.right + Math.round(ViewCompat.getTranslationX(child));
            final int left = right - spacing;
            canvas.drawRect(left, top, right, bottom, paint);
            if (bordWidth != 0 && bordColor != 0) {
                canvas.drawRect(left, top, left + bordWidth, bottom, bordPaint);
                canvas.drawRect(right - bordWidth, top, right, bottom, bordPaint);
            }
        }
        canvas.restore();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        Object tag = view.getTag();
        boolean ignore = tag == null || (tag instanceof Boolean && !(boolean) tag);
        if (hasDecorTag && ignore) {
            state.put(KEY_IGNORE_DIVIDER + position, false);

        } else {
            if (mOrientation == VERTICAL) {
                outRect.set(0, 0, 0, spacing);
            } else {
                outRect.set(0, 0, spacing, 0);
            }
        }
    }
}
