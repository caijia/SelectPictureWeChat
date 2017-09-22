package com.caijia.selectpicture.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by cai.jia on 2017/7/21 0021
 */

public class ClipImageView extends AppCompatImageView implements
        ScaleGestureDetector.OnScaleGestureListener, MoveGestureDetector.OnMoveGestureListener {

    private Xfermode xfermode;
    private Paint paint;
    private int shadowColor;
    private int clipBorderColor;
    private int clipBorderStrokeWidth;
    private int clipBorderMargin;
    private float clipBorderAspect;
    private Rect clipBorder;
    private ScaleGestureDetector scaleGesture;
    private MoveGestureDetector moveGesture;
    private ValueAnimator transAnimator;
    private float initScale;
    private float minScale;
    private float maxScale;
    private Matrix imageMatrix = new Matrix();
    private int duration = 180;
    private Runnable adjustClipBorderAspectTask = new Runnable() {
        @Override
        public void run() {
            adjustClipBorderAspect();
        }
    };
    private int pointerCount;
    private int cornerWidth = 81;
    private int cornerHeight = 9;
    private int centerLineHeight = 2;
    private int centerLineColor = Color.parseColor("#66ffffff");
    private int clipBorderGradientColor = Color.parseColor("#30ffffff");
    private int clipBorderGradientWidth = 30;

    public ClipImageView(Context context) {
        this(context, null);
    }

    public ClipImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.MATRIX);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);

        shadowColor = Color.parseColor("#bb000000");
        clipBorderColor = Color.WHITE;
        clipBorderStrokeWidth = dpToPx(1.5f);
        clipBorderMargin = dpToPx(27);
        clipBorderAspect = 1;
        cornerWidth = dpToPx(27);
        cornerHeight = dpToPx(2.5f);
        centerLineHeight = dpToPx(1);
        centerLineColor = Color.parseColor("#aaeeeeee");
        clipBorderGradientColor = Color.parseColor("#20444444");
        clipBorderGradientWidth = dpToPx(10);

        xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
        clipBorder = new Rect();

        moveGesture = new MoveGestureDetector(context, this);
        scaleGesture = new ScaleGestureDetector(context, this);
    }

    private int dpToPx(float dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics()));
    }

    public void setClipBorderAspect(float clipBorderAspect) {
        this.clipBorderAspect = clipBorderAspect;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int width = getWidth();
        int height = getHeight();
        clipBorder.left = clipBorderMargin;
        clipBorder.right = width - clipBorderMargin;
        int clipBorderHeight = (int) (clipBorder.width() / clipBorderAspect);
        clipBorder.top = (height - clipBorderHeight) / 2;
        clipBorder.bottom = clipBorder.top + clipBorderHeight;

        if (listener != null) {
            listener.onClipBorderSizeChanged(clipBorder.width(), clipBorder.height());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        pointerCount = event.getPointerCount();
        scaleGesture.onTouchEvent(event);
        moveGesture.onTouchEvent(event);
        return true;
    }

    @Override
    public void setImageURI(@Nullable Uri uri) {
        super.setImageURI(uri);
        post(adjustClipBorderAspectTask);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        post(adjustClipBorderAspectTask);
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        post(adjustClipBorderAspectTask);
    }

    /**
     * 图片经过平移后,如果图片没有包含裁剪框,则平移调整
     * 图片经过缩放后,如果图片没有包含裁剪框,缩放到初始值
     *
     * @param bigToSmall true表示缩小到缩放初始值,false表示放大到初始值
     */
    private void adjustTranslateAndScale(boolean bigToSmall) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        final float[] matrixValues = new float[9];
        imageMatrix.getValues(matrixValues);
        float scale = matrixValues[Matrix.MSCALE_X];
        float postScale = 0;
        float scalePx = 1;
        float scalePy = 1;
        RectF drawableBounds;
        if (bigToSmall ? scale > initScale : scale < initScale) {
            postScale = initScale / scale;
            Matrix matrix = new Matrix();
            matrix.setValues(matrixValues);
            RectF bounds = getDrawableBounds(imageMatrix);
            float leftDistance = scaleGesture.getFocusX() - bounds.left;
            float topDistance = scaleGesture.getFocusY() - bounds.top;
            scalePx = bounds.width() / leftDistance;
            scalePy = bounds.height() / topDistance;
            matrix.postScale(postScale, postScale, bounds.left + leftDistance,
                    bounds.top + topDistance);
            matrix.getValues(matrixValues);
            drawableBounds = getDrawableBounds(matrix);
        } else {
            drawableBounds = getDrawableBounds(imageMatrix);
        }
        float[] transXY = computeBorderXY(matrixValues, drawableBounds);
        smoothScaleAndTranslate(transXY[0], transXY[1], postScale,scalePx,scalePy);
    }

    private float[] computeBorderXY(float[] matrixValues, RectF drawableBounds) {
        float transX = matrixValues[Matrix.MTRANS_X];
        float transY = matrixValues[Matrix.MTRANS_Y];
        float needTransX = 0;
        float needTransY = 0;
        if (transX > clipBorder.left) {
            //需要向左平移
            needTransX = clipBorder.left - transX;
        }

        if (transX + drawableBounds.width() < clipBorder.right) {
            //需要向右平移
            needTransX = clipBorder.right - (transX + drawableBounds.width());
        }

        if (transY > clipBorder.top) {
            //需要向上平移
            needTransY = clipBorder.top - transY;
        }

        if (transY + drawableBounds.height() < clipBorder.bottom) {
            //需要向下平移
            needTransY = clipBorder.bottom - (transY + drawableBounds.height());
        }
        return new float[]{needTransX, needTransY};
    }

    private void smoothScale(float sourceScale, float targetScale, final float x, final float y) {
        transAnimator = ValueAnimator.ofFloat(sourceScale, targetScale);
        transAnimator.setDuration(duration);
        final float[] preValue = {sourceScale};
        transAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (float) animation.getAnimatedValue();
                float postScale = scale / preValue[0];
                imageMatrix.postScale(postScale, postScale, x, y);
                setImageMatrix(imageMatrix);
                preValue[0] = scale;
            }
        });
        transAnimator.start();
    }

    private void smoothScaleAndTranslate(final float translateX, final float translateY,
                                         final float scale,final float scalePx,final float scalePy) {
        if (translateX != 0 || translateY != 0) {
            final float[] preValue = {0, 0, 1}; //0 transX,1 transY,2 scale
            float[] startEnd = computeStartEnd(translateX, translateY, scale);
            transAnimator = ValueAnimator.ofFloat(startEnd[0], startEnd[1]);
            transAnimator.setDuration(duration);
            transAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float fraction = animation.getAnimatedFraction();
                    float currentX = translateX * fraction;
                    float currentY = translateY * fraction;
                    float dx = currentX - preValue[0];
                    float dy = currentY - preValue[1];
                    imageMatrix.postTranslate(dx, dy);
                    if (scale > 0) {
                        float currentScale = 1 + (scale - 1) * fraction; //start + (end - start)*fraction
                        float dScale = currentScale / preValue[2];
                        RectF bounds = getDrawableBounds(imageMatrix);
                        imageMatrix.postScale(dScale, dScale, bounds.left + bounds.width() / scalePx,
                                bounds.top + bounds.height() / scalePy);
                        preValue[2] = currentScale;
                    }
                    setImageMatrix(imageMatrix);
                    preValue[0] = currentX;
                    preValue[1] = currentY;
                }
            });
            transAnimator.start();
        }
    }

    private float[] computeStartEnd(float translateX, float translateY, float scale) {
        float start = scale > 0 ? 1 : 0;
        float end = scale > 0 ? scale : (translateX != 0 ? translateX : translateY);
        return new float[]{start, end};
    }

    public
    @Nullable
    Bitmap clip() {
        final Drawable drawable = getDrawable();
        final Bitmap originalBitmap = ((BitmapDrawable) drawable).getBitmap();

        final float[] matrixValues = new float[9];
        imageMatrix.getValues(matrixValues);
        final float scale = matrixValues[Matrix.MSCALE_X] * drawable.getIntrinsicWidth() / originalBitmap.getWidth();
        final float transX = (int) matrixValues[Matrix.MTRANS_X];
        final float transY = (int) matrixValues[Matrix.MTRANS_Y];

        final int cropX = (int) ((-transX + clipBorder.left) / scale);
        final int cropY = (int) ((-transY + clipBorder.top) / scale);
        final int cropWidth = (int) (clipBorder.width() / scale);
        final int cropHeight = (int) (clipBorder.height() / scale);
        if (cropX < 0 || cropY < 0 || cropX + cropWidth > originalBitmap.getWidth()
                || cropY + cropHeight > originalBitmap.getHeight()) {
            return null;
        }
        return Bitmap.createBitmap(originalBitmap, cropX, cropY, cropWidth,cropHeight);
    }

    private RectF getDrawableBounds(Matrix matrix) {
        RectF rect = new RectF();
        Drawable d = getDrawable();
        if (null != d) {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rect);
        }
        return rect;
    }

    /**
     * 图片适应裁剪框的宽高比
     */
    private void adjustClipBorderAspect() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        int dWidth = drawable.getIntrinsicWidth();
        int dHeight = drawable.getIntrinsicHeight();

        int cWidth = clipBorder.width();
        int cHeight = clipBorder.height();

        int vWidth = getWidth();
        int vHeight = getHeight();

        float drawableAspect = (float) dWidth / dHeight;
        float scaleWidth, scaleHeight;
        float scale;
        if (cWidth > cHeight * drawableAspect) {
            scaleWidth = cWidth;
            scale = scaleWidth / dWidth;
            scaleHeight = dHeight * scale;

        } else {
            scaleHeight = cHeight;
            scale = scaleHeight / dHeight;
            scaleWidth = dWidth * scale;
        }

        float xOffset = (vWidth - scaleWidth) / 2;
        float yOffset = (vHeight - scaleHeight) / 2;

        imageMatrix.reset();
        imageMatrix.postScale(scale, scale);
        imageMatrix.postTranslate(xOffset, yOffset);
        setImageMatrix(imageMatrix);

        initScale = scale;
        minScale = scale / 2;
        maxScale = scale * 4;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        canvas.save();
        canvas.saveLayer(0, 0, width, height, paint, Canvas.ALL_SAVE_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(shadowColor);
        canvas.drawRect(0, 0, width, height, paint);

        paint.setXfermode(xfermode);
        paint.setColor(clipBorderColor);
        canvas.drawRect(clipBorder, paint);
        canvas.restore();

        //透明度边框
        paint.setXfermode(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(clipBorderGradientWidth);
        paint.setColor(clipBorderGradientColor);
        int halfGradientWidth = clipBorderGradientWidth / 2;
        clipBorder.inset(halfGradientWidth, halfGradientWidth);
        canvas.drawRect(clipBorder, paint);
        clipBorder.inset(-halfGradientWidth, -halfGradientWidth);

        //边框
        paint.setStrokeWidth(clipBorderStrokeWidth);
        paint.setColor(clipBorderColor);
        clipBorder.inset(halfGradientWidth, halfGradientWidth);
        canvas.drawRect(clipBorder, paint);
        clipBorder.inset(-halfGradientWidth, -halfGradientWidth);

        //画中间线条
        paint.setStrokeWidth(centerLineHeight);
        paint.setColor(centerLineColor);
        clipBorder.inset(halfGradientWidth, halfGradientWidth);
        int dWidth = clipBorder.width() / 3;
        int dHeight = clipBorder.height() / 3;
        canvas.drawLine(clipBorder.left, clipBorder.top + dHeight, clipBorder.right,
                clipBorder.top + dHeight, paint);
        canvas.drawLine(clipBorder.left, clipBorder.top + dHeight * 2, clipBorder.right,
                clipBorder.top + dHeight * 2, paint);

        canvas.drawLine(clipBorder.left, clipBorder.top + dHeight * 2, clipBorder.right,
                clipBorder.top + dHeight * 2, paint);

        canvas.drawLine(clipBorder.left + dWidth, clipBorder.top, clipBorder.left + dWidth,
                clipBorder.bottom, paint);

        canvas.drawLine(clipBorder.left + dWidth * 2, clipBorder.top, clipBorder.left + dWidth * 2,
                clipBorder.bottom, paint);
        clipBorder.inset(-halfGradientWidth, -halfGradientWidth);

        //画四个角
        paint.setStrokeWidth(cornerHeight);
        paint.setColor(clipBorderColor);
        int halfCornerHeight = cornerHeight / 2;
        int offset = halfGradientWidth - cornerHeight + halfCornerHeight;
        clipBorder.inset(offset, offset);

        //左上角
        canvas.drawLine(clipBorder.left - halfCornerHeight, clipBorder.top,
                clipBorder.left + cornerWidth, clipBorder.top, paint);
        canvas.drawLine(clipBorder.left, clipBorder.top,
                clipBorder.left, clipBorder.top + cornerWidth, paint);

        //右上角
        canvas.drawLine(clipBorder.right + halfCornerHeight, clipBorder.top,
                clipBorder.right - cornerWidth, clipBorder.top, paint);
        canvas.drawLine(clipBorder.right, clipBorder.top,
                clipBorder.right, clipBorder.top + cornerWidth, paint);

        //左下角
        canvas.drawLine(clipBorder.left - halfCornerHeight, clipBorder.bottom,
                clipBorder.left + cornerWidth, clipBorder.bottom, paint);
        canvas.drawLine(clipBorder.left, clipBorder.bottom,
                clipBorder.left, clipBorder.bottom - cornerWidth, paint);

        //右下角
        canvas.drawLine(clipBorder.right + halfCornerHeight, clipBorder.bottom,
                clipBorder.right - cornerWidth, clipBorder.bottom, paint);
        canvas.drawLine(clipBorder.right, clipBorder.bottom,
                clipBorder.right, clipBorder.bottom - cornerWidth, paint);
        clipBorder.inset(-offset, -offset);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        final float[] matrixValues = new float[9];
        imageMatrix.getValues(matrixValues);
        float scale = matrixValues[Matrix.MSCALE_X];
        float willScale = scale * scaleFactor;
        if (willScale > maxScale) {
            scaleFactor = maxScale / scale;
        }

        if (willScale < minScale) {
            scaleFactor = minScale / scale;
        }
        imageMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
        setImageMatrix(imageMatrix);
        return true;
    }

    private boolean isRunning() {
        return transAnimator != null && (transAnimator.isRunning() || transAnimator.isStarted());
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        boolean isRunning = isRunning();
        return !isRunning && pointerCount > 1;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    @Override
    public void onMoveGestureScroll(float dx, float dy, float distanceX, float distanceY) {
        boolean isRunning = isRunning();
        if (isRunning || scaleGesture.isInProgress()) {
            return;
        }
        imageMatrix.postTranslate(dx, dy);
        setImageMatrix(imageMatrix);
    }

    @Override
    public void onMoveGestureUpOrCancel(MotionEvent e) {
        adjustTranslateAndScale(false);
    }

    @Override
    public void onMoveGestureDoubleTap(MotionEvent e) {
        //大于原始缩放值的2倍,自动缩放到初始值,反之,缩放到初始值的2倍
        final float[] matrixValues = new float[9];
        imageMatrix.getValues(matrixValues);
        float scale = matrixValues[Matrix.MSCALE_X];
        if (scale >= initScale * 2 - 0.05f) {
            adjustTranslateAndScale(true);
        } else {
            smoothScale(scale, initScale * 2, e.getX(), e.getY());
        }
    }

    @Override
    public boolean onMoveGestureBeginTap(MotionEvent event) {
        //如果返回false,将不会收到双击事件,点击事件发生在图片显示区域,则开启双击事件检测
        RectF bounds = getDrawableBounds(imageMatrix);
        return bounds.contains(event.getX(), event.getY());
    }

    public float[] getClipMatrixValues() {
        final float[] matrixValues = new float[9];
        imageMatrix.getValues(matrixValues);
        return matrixValues;
    }

    public Rect getClipBorder() {
        return clipBorder;
    }

    public interface OnClipBorderSizeChangedListener{

        void onClipBorderSizeChanged(int clipBorderWidth, int clipBorderHeight);
    }

    private OnClipBorderSizeChangedListener listener;

    public void setOnClipBorderSizeChangedListener(OnClipBorderSizeChangedListener listener) {
        this.listener = listener;
    }
}
