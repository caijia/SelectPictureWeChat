package com.caijia.selectpicture.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Created by cai.jia on 2016/11/22.
 */
public class ToastManager {

    private static ToastManager instance;
    private Toast toast;

    private ToastManager(Context context) {
        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }

    public static ToastManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ToastManager.class) {
                if (instance == null) {
                    instance = new ToastManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public void showToast(@Nullable String text, int duration) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        toast.setText(text);
        toast.setDuration(duration);
        toast.show();
    }

    public void showToast(@Nullable String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        toast.setText(text);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}
