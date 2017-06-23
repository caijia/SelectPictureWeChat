package com.caijia.selectpicture.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.caijia.selectpicture.bean.PictureGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 得到手机里面的图片
 * Created by cai.jia on 2016/4/7 0007.
 */
public class PictureManager {

    private static volatile PictureManager instance = null;

    private PictureManager() {

    }

    public static PictureManager getInstance() {
        if (instance == null) {
            synchronized (PictureManager.class) {
                if (instance == null) {
                    instance = new PictureManager();
                }
            }
        }
        return instance;
    }

    /**
     * 查询手机里面的所有图片(size > 0)
     *
     * @param context
     * @param listener
     */
    public void getLocalPicture(final Context context, final OnGetLocalPictureListener listener) {
        new AsyncTask<Void, Void, List<String>>() {

            @Override
            protected void onPostExecute(List<String> list) {
                if (listener != null) {
                    listener.onGetPictureFinish(list, toPictureGroup(list));
                }
            }

            @Override
            protected List<String> doInBackground(Void... params) {
                return queryImage(context);
            }
        }.execute();
    }

    private List<String> queryImage(Context context) {
        long start = System.currentTimeMillis();
        List<String> pathList = new ArrayList<>();

        String selection;
        String[] selectionArgs;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            selection = String.format("%s=? and %s>? and %s>? and %s>?",
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.WIDTH,
                    MediaStore.Images.Media.HEIGHT);
            selectionArgs = new String[]{"image/jpeg", "0", "0", "0"};

        }else{
            selection = String.format("%s=? and %s>?",
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.SIZE);
            selectionArgs = new String[]{"image/jpeg", "0"};
        }

        Cursor cursor = context.getContentResolver().query(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                            selection,
                            selectionArgs,
                            MediaStore.Images.Media.DATE_MODIFIED + " desc");

        File file;
        boolean api16High = Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
        while (cursor != null && cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            file = new File(path);
            if (api16High && file.exists()
                    || file.exists() && (!api16High && !isDestroyedImageFile(path))) {
                pathList.add(path);
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        long end = System.currentTimeMillis();
        Log.v("time", String.valueOf(end - start));
        return pathList;
    }

    private void queryVideo(Context context) {
        String []projection = { MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA};
        String orderBy = MediaStore.Video.Media.DISPLAY_NAME;
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//        context.getContentResolver().query(uri,projection, orderBy);
    }

    /**
     * 图片文件是否损坏
     * @param imagePath
     * @return
     */
    private boolean isDestroyedImageFile(String imagePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        return options.outHeight <= 0 || options.outWidth <= 0;
    }

    public List<PictureGroup> toPictureGroup(List<String> pathList) {
        List<PictureGroup> groupList = new ArrayList<>();
        if (pathList == null || pathList.isEmpty()) {
            return groupList;
        }

        Map<String, List<String>> map = new HashMap<>();
        for (String path : pathList) {
            String parentPath = new File(path).getParentFile().getAbsolutePath();
            if (map.containsKey(parentPath)) {
                List<String> list = map.get(parentPath);
                list.add(path);
            } else {
                List<String> list = new ArrayList<>();
                list.add(path);
                map.put(parentPath, list);
            }
        }

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            PictureGroup group = new PictureGroup();
            String parentPath = entry.getKey();
            group.setGroupName(parentPath.substring(parentPath.lastIndexOf(File.separator) + 1));
            group.setPicturePaths(entry.getValue());
            groupList.add(group);
        }
        return groupList;
    }

    public interface OnGetLocalPictureListener {
        /**
         * @param list      手机里面的图片
         * @param groupList 手机里面的图片分组
         */
        void onGetPictureFinish(List<String> list, List<PictureGroup> groupList);
    }
}
