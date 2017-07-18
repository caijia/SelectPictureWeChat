package com.caijia.selectpicture.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by cai.jia on 2017/7/18 0018
 */

public class ClipImageLayout  extends FrameLayout{

    public ClipImageLayout(@NonNull Context context) {
        this(context,null);
    }

    public ClipImageLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ClipImageLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ClipImageLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private int horizontalMargin;
    private int boundsWidth;

    private void init(Context context, AttributeSet attrs) {
        setWillNotDraw(false);
        horizontalMargin = 120;
        boundsWidth = 3;

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setAntiAlias(true);
        shadowPaint.setColor(Color.parseColor("#44000000"));

        boundsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boundsPaint.setAntiAlias(true);
        boundsPaint.setColor(Color.WHITE);
        boundsPaint.setStyle(Paint.Style.STROKE);
        boundsPaint.setStrokeWidth(boundsWidth);
    }

    private Paint shadowPaint;
    private Paint boundsPaint;

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        int width = getWidth();
        int height = getHeight();
        int lightWidth = width - horizontalMargin * 2;
        int lightHeight = lightWidth;

        int lightRectLeft = (width - lightWidth) / 2;
        int lightRectTop = (height - lightHeight) / 2;
        int lightRectRight = lightRectLeft + lightWidth;
        int lightRectBottom = lightRectTop + lightHeight;

        //draw top edge shadow
        int topEdgeBottom = (height - lightHeight) / 2;
        canvas.drawRect(0, 0, width, topEdgeBottom,shadowPaint);

        //draw left edge shadow
        int leftEdgeTop = (height - lightHeight) / 2;
        int leftEdgeRight = (width - lightWidth) / 2;
        int leftEdgeBottom = (height - lightHeight) / 2 + lightHeight;
        canvas.drawRect(0,leftEdgeTop,leftEdgeRight,leftEdgeBottom,shadowPaint);

        //draw right edge shadow
        int rightEdgeLeft = (width - lightWidth) / 2 + lightWidth;
        int rightEdgeTop = (height - lightHeight) / 2;
        int rightEdgeBottom = (height - lightHeight) / 2 + lightHeight;
        canvas.drawRect(rightEdgeLeft,rightEdgeTop,width,rightEdgeBottom,shadowPaint);

        //draw bottom edge shadow
        int bottomEdgeTop = (height - lightHeight) / 2 + lightHeight;
        canvas.drawRect(0, bottomEdgeTop, width, height,shadowPaint);

        //draw center rect
        canvas.drawRect(lightRectLeft,lightRectTop,lightRectRight,lightRectBottom,boundsPaint);
    }
}
