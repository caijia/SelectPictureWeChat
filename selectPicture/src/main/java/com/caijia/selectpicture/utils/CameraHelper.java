package com.caijia.selectpicture.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.io.File;

/**
 * Created by cai.jia on 2017/7/18 0018
 */

public class CameraHelper {

    private static volatile CameraHelper instance;

    private CameraHelper() {

    }

    public static CameraHelper getInstance() {
        if (instance == null) {
            synchronized (CameraHelper.class) {
                if (instance == null) {
                    instance = new CameraHelper();
                }
            }
        }
        return instance;
    }

    public void takePicture(@NonNull Activity activity, String filePath, int requestCode) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(filePath);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        activity.startActivityForResult(intent, requestCode);
    }

    public void takePicture(@NonNull Fragment fragment, String filePath, int requestCode) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(filePath);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        fragment.startActivityForResult(intent, requestCode);
    }
}
