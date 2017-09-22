package com.caijia.selectpicturewechat;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by cai.jia on 2017/9/20 0020.
 */

public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }
}
