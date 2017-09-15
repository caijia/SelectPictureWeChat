package com.caijia.selectpicture.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;


/**
 * Created by cai.jia on 2015/12/3.
 */
public class FileUtil {

    public static boolean isMountSdCard() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) &&
                !Environment.isExternalStorageRemovable();
    }

    public static File createDiskCacheFile(Context context,String dir, String fileName) {
        File diskCacheDir = createDiskCacheDir(context, dir);
        return new File(diskCacheDir, fileName);
    }

    private static String getCachePath(Context context) {
        return isMountSdCard() && context.getExternalCacheDir() != null
                ? context.getExternalCacheDir().getPath()
                : context.getCacheDir().getPath();
    }

    public static File createDiskCacheFile(Context context, String uniqueName) {
        String cachePath = getCachePath(context);
        return new File(cachePath + File.separator + uniqueName);
    }

    public static File createDiskCacheDir(Context context, String dir) {
        String cachePath = getCachePath(context);
        File dirFile = new File(cachePath + File.separator + dir);
        dirFile.mkdirs();
        return dirFile;
    }
}
