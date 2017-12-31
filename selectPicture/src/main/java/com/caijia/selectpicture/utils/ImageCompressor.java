package com.caijia.selectpicture.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.IntRange;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 图片压缩工具
 * Created by cai.jia on 2017/12/30.
 */

public class ImageCompressor {

    private static final String IMAGE_COMPRESS_DIR = "compressImage";
    private static volatile ImageCompressor instance;

    private ImageCompressor() {

    }

    public static ImageCompressor getInstance() {
        if (instance == null) {
            synchronized (ImageCompressor.class) {
                if (instance == null) {
                    instance = new ImageCompressor();
                }
            }
        }
        return instance;
    }

    /**
     * 压缩单张图片,压缩过程已在子线程进行,回调在主线程
     *
     * @param context                       上下文
     * @param filePath                      文件路劲
     * @param config                        文件质量{@link android.graphics.Bitmap.Config#RGB_565}，{@link android.graphics.Bitmap.Config#ARGB_8888}
     * @param compressQuality               压缩文件质量0-100,越小压缩越狠
     * @param targetWidth                   按照此宽度进行压缩用于计算{@link android.graphics.BitmapFactory.Options#inSampleSize}
     * @param targetHeight                  按照此高度进行压缩 {@link android.graphics.BitmapFactory.Options#inSampleSize}
     * @param onImageCompressFinishListener 压缩完成回调
     */
    public void compress(Context context, String filePath, Bitmap.Config config,
                         @IntRange(from = 0, to = 100) int compressQuality, int targetWidth, int targetHeight,
                         OnImageCompressFinishListener onImageCompressFinishListener) {
        List<String> filePaths = new ArrayList<>();
        filePaths.add(filePath);
        compress(context, filePaths, config, compressQuality, targetWidth, targetHeight, onImageCompressFinishListener);
    }

    /**
     * 压缩多张图片,压缩过程已在子线程进行,回调在主线程
     *
     * @param context                       上下文
     * @param filePaths                     文件路劲集合
     * @param config                        文件质量{@link android.graphics.Bitmap.Config#RGB_565}，{@link android.graphics.Bitmap.Config#ARGB_8888}
     * @param compressQuality               压缩文件质量0-100,越小压缩越狠
     * @param targetWidth                   按照此宽度进行压缩用于计算{@link android.graphics.BitmapFactory.Options#inSampleSize}
     * @param targetHeight                  按照此高度进行压缩 {@link android.graphics.BitmapFactory.Options#inSampleSize}
     * @param onImageCompressFinishListener 压缩完成回调
     */
    public void compress(Context context, List<String> filePaths, Bitmap.Config config,
                         @IntRange(from = 0, to = 100) int compressQuality, int targetWidth, int targetHeight,
                         OnImageCompressFinishListener onImageCompressFinishListener) {
        PostExecuteResult params = new PostExecuteResult(context, filePaths, config,
                compressQuality, targetWidth, targetHeight, onImageCompressFinishListener);
        new ImageCompressAsyncTask(this).execute(params);
    }

    public Bitmap compressBitmap(String filePath, Bitmap.Config config, int targetWidth, int targetHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        int inSampleSize = computeSampleSize(bitmapWidth, bitmapHeight, targetWidth, targetHeight);
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        options.inPreferredConfig = config;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 删除压缩后保存的文件
     * @param context
     */
    public void deleteCompressImage(Context context) {
        File compressImageDir = FileUtil.createDiskCacheDir(context, IMAGE_COMPRESS_DIR);
        FileUtil.deleteFile(compressImageDir);
    }

    private int computeSampleSize(int imageWidth, int imageHeight,
                                  int targetWidth, int targetHeight) {
        int sampleSize = 1;
        while (imageWidth / sampleSize > targetWidth || imageHeight / sampleSize > targetHeight) {
            sampleSize = sampleSize << 1;
        }
        return sampleSize;
    }

    private String saveBitmapToFile(Context context, Bitmap bitmap, int quality) {
        if (bitmap == null) {
            return null;
        }

        String fileName = String.format("%s.jpg", UUID.randomUUID().toString().replace("-", ""));
        File file = FileUtil.createDiskCacheFile(context, IMAGE_COMPRESS_DIR, fileName);
        String saveFilePath = file.getAbsolutePath();
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(saveFilePath));
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            saveFilePath = null;

        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
                saveFilePath = null;
            }
        }
        return saveFilePath;
    }

    public interface OnImageCompressFinishListener {

        void onImageCompressFinish(List<String> filePaths);
    }

    private static class ImageCompressAsyncTask extends AsyncTask<PostExecuteResult, Void, PostExecuteResult> {

        WeakReference<ImageCompressor> ref;

        ImageCompressAsyncTask(ImageCompressor compressor) {
            this.ref = new WeakReference<>(compressor);
        }

        @Override
        protected PostExecuteResult doInBackground(PostExecuteResult... params) {
            List<String> saveFilePaths = new ArrayList<>();
            PostExecuteResult param = params[0];
            List<String> filePaths = param.filePaths;
            if (filePaths != null && !filePaths.isEmpty()) {
                for (String filePath : filePaths) {
                    ImageCompressor imageCompressor = ref.get();
                    if (imageCompressor != null) {
                        Bitmap bitmap = imageCompressor.compressBitmap(filePath, param.config, param.targetWidth, param.targetHeight);
                        String saveFilePath = imageCompressor.saveBitmapToFile(param.context, bitmap, param.compressQuality);
                        if (!TextUtils.isEmpty(saveFilePath)) {
                            saveFilePaths.add(saveFilePath);
                        }
                    }
                }
            }
            param.saveFilePaths = saveFilePaths;
            return param;
        }

        @Override
        protected void onPostExecute(PostExecuteResult result) {
            if (result != null && result.onImageCompressFinishListener != null) {
                result.onImageCompressFinishListener.onImageCompressFinish(result.saveFilePaths);
            }
        }
    }

    private static class PostExecuteResult {
        Context context;
        List<String> saveFilePaths;
        List<String> filePaths;
        Bitmap.Config config;
        int compressQuality;
        int targetWidth;
        int targetHeight;
        OnImageCompressFinishListener onImageCompressFinishListener;

        PostExecuteResult(Context context, List<String> filePaths,
                          Bitmap.Config config, int compressQuality, int targetWidth,
                          int targetHeight, OnImageCompressFinishListener onImageCompressFinishListener) {
            this.context = context;
            this.filePaths = filePaths;
            this.config = config;
            this.compressQuality = compressQuality;
            this.targetWidth = targetWidth;
            this.targetHeight = targetHeight;
            this.onImageCompressFinishListener = onImageCompressFinishListener;
        }
    }
}
