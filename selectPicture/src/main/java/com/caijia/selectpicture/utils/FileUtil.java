package com.caijia.selectpicture.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;


/**
 * Created by cai.jia on 2015/12/3.
 */
public class FileUtil {

    public static boolean isMountSdCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) &&
                !Environment.isExternalStorageRemovable();
    }

    public static File createDiskCacheFile(Context context,String dir, String fileName) {
        File diskCacheDir = createDiskCacheDir(context, dir);
        return new File(diskCacheDir, fileName);
    }

    public static File createDiskCacheFile(Context context, String uniqueName) {
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Environment.isExternalStorageRemovable() ?
                        context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    public static File createDiskCacheDir(Context context, String dir) {
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Environment.isExternalStorageRemovable() ?
                        context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();

        File dirFile = new File(cachePath + File.separator + dir);
        dirFile.mkdirs();
        return dirFile;
    }
}
