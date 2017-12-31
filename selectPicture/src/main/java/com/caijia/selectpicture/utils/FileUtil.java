package com.caijia.selectpicture.utils;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;

import java.io.File;

import static android.os.Environment.DIRECTORY_PICTURES;


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

    /**
     * 创建图片文件,在有些手机里，如果图片文件不放在{@link Environment#getExternalStoragePublicDirectory(String}
     * {@link Environment#DIRECTORY_PICTURES} 这里将扫描不出来
     * @param dir
     * @param fileName
     * @return
     */
    public static @Nullable File createPictureDiskFile(String dir, String fileName) {
        if (isMountSdCard()) {
            File dirFile;
            try {
                dirFile = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES), dir);
                dirFile.mkdirs();

            } catch (Exception e) {
                dirFile = new File(Environment.getExternalStorageDirectory(), dir);
                dirFile.mkdirs();

            }
            return new File(dirFile, fileName);
        }
        return null;
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

    /**
     * 递归删除文件和文件夹
     * @param file    要删除的根目录
     */
    public static void deleteFile(File file){
        if(file.isFile()){
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                deleteFile(f);
            }
            file.delete();
        }
    }
}
