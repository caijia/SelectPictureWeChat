package com.caijia.selectpicture.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 代码来源:
 * http://www.jianshu.com/p/a44c119d6ef7
 */
public class StatusBarUtil {

    /**
     * 设置沉浸式状态栏
     */
    public static void setImmersiveStatusBar(Activity activity,int statusBarPlaceColor) {
        setTranslucentStatus(activity);
        if (statusBarPlaceColor == Color.WHITE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || isMIUI() || isFlyme()) {
                setStatusBarFontIconDark(activity,true);

            } else {
                statusBarPlaceColor = 0xffcccccc;
                setStatusBarPlaceColor(activity,statusBarPlaceColor);
            }
        }
    }

    public static void setStatusBarPlaceColor(Activity activity,int statusColor) {
        setStatusBarPlaceColor(activity, true, statusColor);
    }

    /**
     * 加入一个与状态栏等高的View到 android.R.id.content的布局中
     * @param activity
     * @param front 状态栏等高的View是在最上层 默认为true
     * @param statusColor 状态栏颜色
     */
    public static void setStatusBarPlaceColor(Activity activity,boolean front,int statusColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //add status bar view
            ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
            View statusView = new View(activity);
            statusView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    getStatusBarHeight(activity)));
            statusView.setBackgroundColor(statusColor);
            rootView.addView(statusView, front ? rootView.getChildCount() : 0);
        }
    }

    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    /**
     * 为View增加一个状态栏高度的MarginTop
     * @param view
     */
    public static void addStatusBarHeightMarginTop(View view) {
        if (view == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int statusBarHeight = getStatusBarHeight(view.getContext());
            ViewGroup.LayoutParams p = view.getLayoutParams();
            if (p instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) p).topMargin = statusBarHeight;
            }
        }
    }

    /**
     * 为View增加一个状态栏高度的paddingTop
     * @param view
     */
    public static void addStatusBarHeightPaddingTop(View view) {
        if (view == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int statusBarHeight = getStatusBarHeight(view.getContext());
            ViewGroup.LayoutParams p = view.getLayoutParams();
            p.height = p.height + statusBarHeight;
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop() + statusBarHeight,
                    view.getRight(), view.getBottom());
        }
    }

    /**
     * 设置状态栏透明
     */
    public static void setTranslucentStatus(Activity activity) {
        // 5.0以上系统状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 设置Android状态栏的字体颜色，状态栏为亮色的时候字体和图标是黑色，状态栏为暗色的时候字体和图标为白色
     *
     * @param isDark 状态栏字体是否为深色
     */
    public static void setStatusBarFontIconDark(Activity activity,boolean isDark) {
        // 小米MIUI
        try {
            Window window = activity.getWindow();
            Class clazz = activity.getWindow().getClass();
            Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            if (isDark) {    //状态栏亮色且黑色字体
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag);
            } else {       //清除黑色字体
                extraFlagField.invoke(window, 0, darkModeFlag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 魅族FlymeUI
        try {
            Window window = activity.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (isDark) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            window.setAttributes(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // android6.0+系统
        // 这个设置和在xml的style文件中用这个<item name="android:windowLightStatusBar">true</item>属性是一样的
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isDark) {
                activity.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    public static boolean isMIUI() {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
    }

    public static boolean isFlyme() {
        try {
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }

    private static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }
}
