package com.caijia.selectpicture.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.UUID;

import static com.caijia.selectpicture.utils.Constants.IMAGE_SAVE_DIR;

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

    /**
     * 拍照后在{@link Activity#onActivityResult(int, int, Intent)} 接收照相后的图片,并且差入相册
     *
     * @param context             上下文
     * @param takePictureSaveFile 照相后的图片
     * @return 照相后的图片
     */
    public
    @Nullable
    String insertImage(Context context, File takePictureSaveFile) {
        if (takePictureSaveFile == null) {
            return null;
        }

        String fileName = takePictureSaveFile.getName();
        String filePath = takePictureSaveFile.getAbsolutePath();
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), filePath, fileName, "");
            Uri contentUri = Uri.fromFile(takePictureSaveFile);
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filePath;
    }

    public void takePicture(@NonNull Activity activity, int requestCode) {
        String fileName = String.format("%s.jpg", UUID.randomUUID().toString().replaceAll("-", ""));
        File takePictureSaveFile = FileUtil.createPictureDiskFile(IMAGE_SAVE_DIR, fileName);
        if (takePictureSaveFile == null) {
            return;
        }
        takePicture(activity, takePictureSaveFile.getAbsolutePath(), requestCode);
    }

    public void takePicture(@NonNull Fragment fragment, int requestCode) {
        String fileName = String.format("%s.jpg", UUID.randomUUID().toString().replaceAll("-", ""));
        File takePictureSaveFile = FileUtil.createPictureDiskFile(IMAGE_SAVE_DIR, fileName);
        if (takePictureSaveFile == null) {
            return;
        }
        takePicture(fragment, takePictureSaveFile.getAbsolutePath(), requestCode);
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
