package com.caijia.selectpicture.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;
    private int spacing;
    private boolean includeEdge;
    private Rect bounds;
    private Paint paint;

    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    public GridSpacingItemDecoration(int spacing, boolean includeEdge, @ColorInt int color) {
        this.spacing = spacing;
        this.includeEdge = includeEdge;
        bounds = new Rect();
        paint = new Paint();
        paint.setColor(color);
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        if (spanCount == 0 || paint == null || bounds == null) {
            return;
        }

        canvas.save();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            parent.getDecoratedBoundsWithMargins(child, bounds);
            int translationX = Math.round(child.getTranslationX());
            int translationY = Math.round(child.getTranslationY());

            //draw left rect
            int lLeft = bounds.left + translationX;
            int lTop = bounds.top + translationY;
            int lRight = child.getLeft() + translationX;
            int lBottom = bounds.bottom + translationY;
            canvas.drawRect(lLeft, lTop, lRight, lBottom, paint);

            //draw top rect
            int tLeft = child.getLeft() + translationX;
            int tTop = bounds.top + translationY;
            int tRight = child.getRight() + translationX;
            int tBottom = child.getTop() + translationY;
            canvas.drawRect(tLeft, tTop, tRight, tBottom, paint);

            //draw bottom rect
            int bLeft = child.getLeft() + translationX;
            int bTop = child.getBottom() + translationY;
            int bRight = child.getRight() + translationX;
            int bBottom = bounds.bottom + translationY;
            canvas.drawRect(bLeft, bTop, bRight, bBottom, paint);

            //draw right rect
            int rLeft = child.getRight() + translationX;
            int rTop = bounds.top + translationY;
            int rRight = bounds.right + translationX;
            int rBottom = bounds.bottom + translationY;

            //最后一项右边只有边框为spacing
            if (i == childCount - 1 && (i + 1) % spanCount != 0) {
                rRight = rLeft + spacing;
            }
            canvas.drawRect(rLeft, rTop, rRight, rBottom, paint);
        }
        canvas.restore();
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager == null || !(layoutManager instanceof GridLayoutManager)) {
            return;
        }
        GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
        spanCount = gridLayoutManager.getSpanCount();
        if (spanCount == 0) {
            return;
        }

        int position = parent.getChildAdapterPosition(view); // item position
//        int spanSize = gridLayoutManager.getSpanSizeLookup().getSpanSize(position);
        int column = position % spanCount; // item column

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

            if (position < spanCount) { // top edge
                outRect.top = spacing;
            }
            outRect.bottom = spacing; // item bottom
        } else {
            outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
            outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            if (position >= spanCount) {
                outRect.top = spacing; // item top
            }
        }
    }
}