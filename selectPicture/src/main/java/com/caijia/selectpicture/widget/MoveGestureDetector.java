package com.caijia.selectpicture.widget;

import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by cai.jia on 2017/7/25 0025
 */

public class MoveGestureDetector {

    private static final int TAP = 10;

    private float initialMotionX;
    private float initialMotionY;
    private int activePointerId;
    private float lastTouchX;
    private float lastTouchY;
    private int touchSlop;
    private boolean isBeginDragged;

    private int doubleTapSlop;
    private int doubleTapTimeout;
    private int doubleTapMinTime = 40;
    private Handler handler;
    private MotionEvent previousDownEvent;
    private MotionEvent previousUpEvent;
    private boolean inTapRegion;

    private OnMoveGestureListener listener;

    public MoveGestureDetector(Context context, OnMoveGestureListener listener) {
        ViewConfiguration viewConfig = ViewConfiguration.get(context);
        touchSlop = viewConfig.getScaledTouchSlop();
        doubleTapSlop = viewConfig.getScaledDoubleTapSlop();
        doubleTapTimeout = ViewConfiguration.getDoubleTapTimeout();
        handler = new Handler();
        this.listener = listener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                initialMotionX = lastTouchX = event.getX(0);
                initialMotionY = lastTouchY = event.getY(0);
                activePointerId = event.getPointerId(0);

                boolean hasTapMessage = handler.hasMessages(TAP);
                if (hasTapMessage) {
                    handler.removeMessages(TAP);
                }
                if (hasTapMessage && isDoubleTap(previousDownEvent, previousUpEvent, event)) {
                    //double tap
                    if (listener != null) {
                        listener.onMoveGestureDoubleTap(event);
                    }

                }else{
                    if (listener != null && listener.onMoveGestureBeginTap(event))
                    handler.sendEmptyMessageDelayed(TAP, doubleTapTimeout);
                }
                if (previousDownEvent != null) {
                    previousDownEvent.recycle();
                }
                previousDownEvent = MotionEvent.obtain(event);
                inTapRegion = true;
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
                removeTap();
                int pointerIndex = event.getActionIndex();
                activePointerId = event.getPointerId(pointerIndex);
                lastTouchX = event.getX(pointerIndex);
                lastTouchY = event.getY(pointerIndex);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                int pointerIndex = event.findPointerIndex(activePointerId);
                if (pointerIndex < 0) {
                    return false;
                }

                float x = event.getX(pointerIndex);
                float y = event.getY(pointerIndex);
                float dx = x - lastTouchX;
                float dy = y - lastTouchY;
                float distanceX = x - initialMotionX;
                float distanceY = y - initialMotionY;
                if (!isBeginDragged && Math.hypot(Math.abs(distanceX), Math.abs(distanceY)) > touchSlop) {
                    isBeginDragged = true;
                }
                if (listener != null) {
                    listener.onMoveGestureScroll(dx, dy, distanceX, distanceY);
                }
                lastTouchX = x;
                lastTouchY = y;

                if (inTapRegion && Math.hypot(Math.abs(distanceX), Math.abs(distanceY)) > touchSlop) {
                    removeTap();
                }

                double squareDistance = Math.hypot(Math.abs(distanceX), Math.abs(distanceY));
                if (squareDistance > touchSlop || squareDistance > doubleTapSlop) {
                    handler.removeMessages(TAP);
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:{
                removeTap();
                activePointerId = -1;
                isBeginDragged = false;
                if (listener != null) {
                    listener.onMoveGestureUpOrCancel(event);
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                activePointerId = -1;
                isBeginDragged = false;
                if (listener != null) {
                    listener.onMoveGestureUpOrCancel(event);
                }
                if (previousUpEvent != null) {
                    previousUpEvent.recycle();
                }
                previousUpEvent = MotionEvent.obtain(event);
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                removeTap();
                int pointerIndex = event.getActionIndex();
                if (event.getPointerId(pointerIndex) == activePointerId) {
                    int newIndex = pointerIndex == 0 ? 1 : 0;
                    activePointerId = event.getPointerId(newIndex);
                    lastTouchX = event.getX(newIndex);
                    lastTouchY = event.getY(newIndex);
                }
                break;
            }
        }
        return false;
    }

    //源码判断android双击事件机制
    //双击事件,第一次down发送一个tap消息(延时一个双击事件超出的时间)，第二次down检查是否有tap消息,
    //两次down之间的距离是否小于双击事件最大距离,第一次up和第二次down是否小于双击事件超出的时间
    private boolean isDoubleTap(MotionEvent firstDown, MotionEvent firstUp, MotionEvent secondDown) {
        if (previousDownEvent == null || previousUpEvent == null) {
            return false;
        }

        final long deltaTime = secondDown.getEventTime() - firstUp.getEventTime();
        if (deltaTime > doubleTapTimeout || deltaTime < doubleTapMinTime) {
            return false;
        }

        int deltaX = (int) firstDown.getX() - (int) secondDown.getX();
        int deltaY = (int) firstDown.getY() - (int) secondDown.getY();
        return Math.hypot(Math.abs(deltaX),Math.abs(deltaY)) < doubleTapSlop;
    }

    private void removeTap() {
        handler.removeMessages(TAP);
        inTapRegion = false;
    }

    public interface OnMoveGestureListener{

        void onMoveGestureScroll(float dx, float dy, float distanceX, float distanceY);

        void onMoveGestureUpOrCancel(MotionEvent event);

        void onMoveGestureDoubleTap(MotionEvent event);

        boolean onMoveGestureBeginTap(MotionEvent event);
    }
}
