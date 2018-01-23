package com.caijia.selectpicture.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.caijia.selectpicture.R;
import com.caijia.selectpicture.bean.MediaBean;
import com.caijia.selectpicture.bean.MediaGroup;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.caijia.selectpicture.utils.MediaType.IMAGE;
import static com.caijia.selectpicture.utils.MediaType.IMAGE_VIDEO;
import static com.caijia.selectpicture.utils.MediaType.VIDEO;

/**
 * 得到手机里面的图片和视频
 * Created by cai.jia on 2016/4/7 0007.
 */
public class MediaManager {

    private static volatile MediaManager instance = null;

    private MediaManager() {

    }

    public static MediaManager getInstance() {
        if (instance == null) {
            synchronized (MediaManager.class) {
                if (instance == null) {
                    instance = new MediaManager();
                }
            }
        }
        return instance;
    }

    public void getLocalMedia(final Context context, final int mediaType,
                              final OnGetLocalMediaListener listener) {
        String allImageText = context.getResources().getString(R.string.sm_all_image);
        String allVideoText = context.getResources().getString(R.string.sm_all_video);
        String imageVideoText = context.getResources().getString(R.string.sm_all_image_video);
        getLocalMedia(context,mediaType,allImageText,allVideoText,imageVideoText,listener);
    }

    /**
     * 查询手机里面的所有图片或视频
     *
     * @param context
     * @param listener
     */
    public void getLocalMedia(final Context context, final int mediaType,final String allImageText,
                              final String allVideoText,final String imageVideoText,
                              final OnGetLocalMediaListener listener) {
        new AsyncTask<Void, Void, List<Object>>() {

            @Override
            protected void onPostExecute(List<Object> list) {
                if (listener != null) {
                    List<MediaBean> allMedia = new ArrayList<>();
                    allMedia.addAll((Collection<? extends MediaBean>) list.get(0));

                    List<MediaGroup> allMediaGroup = new ArrayList<>();
                    allMediaGroup.addAll((Collection<? extends MediaGroup>) list.get(1));
                    listener.onGetMediaFinish(allMedia,allMediaGroup);
                }
            }

            @Override
            protected List<Object> doInBackground(Void... params) {
                List<Object> list = new ArrayList<>();
                switch (mediaType) {
                    case IMAGE: {
                        List<MediaBean> imageList = queryImage(context);
                        List<MediaGroup> imageGroupList = toMediaGroup(IMAGE, imageList);
                        if (!imageList.isEmpty()) {
                            imageGroupList.add(0,new MediaGroup(true,IMAGE,allImageText, imageList));
                        }
                        list.add(imageList);
                        list.add(imageGroupList);
                        return list;
                    }

                    case VIDEO: {
                        List<MediaBean> videoList = queryVideo(context);
                        List<MediaGroup> videoGroupList = toMediaGroup(VIDEO, videoList);
                        if (!videoList.isEmpty()) {
                            videoGroupList.add(0,new MediaGroup(true,VIDEO,allVideoText, videoList));
                        }
                        list.add(videoList);
                        list.add(videoGroupList);
                        return list;
                    }

                    case IMAGE_VIDEO: {
                        List<MediaBean> imageList = queryImage(context);
                        List<MediaBean> videoList = queryVideo(context);

                        List<MediaBean> mediaList = new ArrayList<>();
                        mediaList.addAll(imageList);
                        mediaList.addAll(videoList);
                        Collections.sort(mediaList);

                        List<MediaGroup> groupList = new ArrayList<>();
                        list.add(mediaList);
                        if (!mediaList.isEmpty()) {
                            groupList.add(0,new MediaGroup(true,IMAGE,imageVideoText, mediaList));
                        }
                        if (!videoList.isEmpty()) {
                            groupList.add(groupList.size(),new MediaGroup(false,VIDEO,allVideoText, videoList));
                        }
                        groupList.addAll(toMediaGroup(IMAGE,imageList));
                        list.add(groupList);
                        return list;
                    }
                }

                return list;
            }
        }.execute();
    }

    private List<MediaBean> queryImage(Context context) {
        return queryImage(context, null);
    }

    /**
     * 查询所有图片(去除gif)
     * //"image/png","image/jpeg","image/jpg"
     * @param context
     * @return
     */
    public List<MediaBean> queryImage(Context context,String filePath) {
        List<MediaBean> list = new ArrayList<>();
        filePath = TextUtils.isEmpty(filePath) ? "" : filePath;

        String selection;
        String[] selectionArgs;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            selection = String.format("%s>? and %s>? and %s>? and %s!=? and %s"
                            + (TextUtils.isEmpty(filePath) ? "!=" : "=") + "?",
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.WIDTH,
                    MediaStore.Images.Media.HEIGHT,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.DATA);
            selectionArgs = new String[]{"0", "0", "0","image/gif",filePath};

        } else {
            selection = String.format("%s>? and %s!=? and %s"
                            + (TextUtils.isEmpty(filePath) ? "!=" : "=") + "?",
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.DATA);
            selectionArgs = new String[]{"0","image/gif",filePath};
        }

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                selection,
                selectionArgs,
                MediaStore.Images.Media.DATE_MODIFIED + " desc");

        File file;
        MediaBean bean;
        boolean api16High = Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
        while (cursor != null && cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
            long dateModified = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
            String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));

            file = new File(path);
            if (api16High && file.exists()
                    || file.exists() && (!api16High && !isDestroyedImageFile(path))) {
                bean = new MediaBean(dateModified,0, size, fileName, path, IMAGE);
                list.add(bean);
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return list;
    }

    private List<MediaBean> queryVideo(Context context) {
        List<MediaBean> list = new ArrayList<>();

        String selection = String.format("%s>? and %s>?",
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE);
        String[] selectionArgs = new String[]{"0", "0"};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                selection,
                selectionArgs,
                MediaStore.Video.Media.DATE_MODIFIED + " desc");

        MediaBean bean;
        while (cursor != null && cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
            long dateModified = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED));
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
            String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
            bean = new MediaBean(dateModified,duration, size, fileName, path, VIDEO);
            list.add(bean);
        }

        if (cursor != null) {
            cursor.close();
        }

        return list;
    }

    /**
     * 图片文件是否损坏
     *
     * @param imagePath
     * @return
     */
    private boolean isDestroyedImageFile(String imagePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        return options.outHeight <= 0 || options.outWidth <= 0;
    }

    private List<MediaGroup> toMediaGroup(int mediaType,List<MediaBean> pathList) {
        List<MediaGroup> groupList = new ArrayList<>();
        if (pathList == null || pathList.isEmpty()) {
            return groupList;
        }

        Map<String, List<MediaBean>> map = new HashMap<>();
        for (MediaBean bean : pathList) {
            String parentPath = new File(bean.getPath()).getParentFile().getAbsolutePath();

            if (map.containsKey(parentPath)) {
                List<MediaBean> list = map.get(parentPath);
                list.add(bean);

            } else {
                List<MediaBean> list = new ArrayList<>();
                list.add(bean);
                map.put(parentPath, list);
            }
        }

        for (Map.Entry<String, List<MediaBean>> entry : map.entrySet()) {
            MediaGroup group = new MediaGroup();
            String parentPath = entry.getKey();
            group.setMediaType(mediaType);
            group.setAbsolutePath(parentPath);
            group.setGroupName(parentPath.substring(parentPath.lastIndexOf(File.separator) + 1));
            group.setMediaList(entry.getValue());
            groupList.add(group);
        }
        return groupList;
    }

    public void addToMediaGroup(int mediaType, List<MediaGroup> groupList, MediaBean bean) {
        if (groupList == null) {
            return;
        }
        String parentPath = new File(bean.getPath()).getParentFile().getAbsolutePath();
        boolean isExistGroup = false;
        for (MediaGroup mediaGroup : groupList) {
            if (TextUtils.equals(parentPath, mediaGroup.getAbsolutePath())) {
                MediaBean mediaBean = mediaGroup.getFirst();
                boolean addToFirst = mediaBean != null && mediaBean.getMediaType() != MediaType.CAMERA;
                mediaGroup.addMediaBean(addToFirst ? 0 : 1, bean);
                isExistGroup = true;
                break;
            }
        }

        if (!isExistGroup) {
            List<MediaBean> mediaBeanList = new ArrayList<>();
            mediaBeanList.add(bean);
            MediaGroup group = new MediaGroup();
            group.setMediaType(mediaType);
            group.setAbsolutePath(parentPath);
            group.setGroupName(parentPath.substring(parentPath.lastIndexOf(File.separator) + 1));
            group.setMediaList(mediaBeanList);
            groupList.add(group);

        }

        //第一组是包含所有图片的
        if (!groupList.isEmpty()) {
            MediaGroup firstGroup = groupList.get(0);
            List<MediaBean> mediaList = firstGroup.getMediaList();
            boolean isCamera = mediaList != null && !mediaList.isEmpty() && mediaList.get(0).getMediaType() == MediaType.CAMERA;
            mediaList.add(isCamera ? 1 : 0, bean);
        }
    }

    public interface OnGetLocalMediaListener {
        /**
         * @param list      手机里面的图片和视频
         * @param groupList 手机里面的图片和视频分组
         */
        void onGetMediaFinish(@NonNull List<MediaBean> list, @NonNull List<MediaGroup> groupList);
    }
}
