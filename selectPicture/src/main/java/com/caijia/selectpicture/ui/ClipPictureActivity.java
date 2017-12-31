package com.caijia.selectpicture.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caijia.selectpicture.R;
import com.caijia.selectpicture.utils.DeviceUtil;
import com.caijia.selectpicture.utils.FileUtil;
import com.caijia.selectpicture.utils.StatusBarUtil;
import com.caijia.selectpicture.widget.ClipImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.caijia.selectpicture.utils.Constants.CLIP_IMAGE_NAME;
import static com.caijia.selectpicture.utils.Constants.IMAGE_SAVE_DIR;

/**
 * 裁剪照片
 * Created by cai.jia on 2017/7/18 0018
 */
public class ClipPictureActivity extends AppCompatActivity implements ClipImageView.OnClipBorderSizeChangedListener {

    public static final String CLIP_OUTPUT_IMAGE_PATH = "params:outputImagePath";
    private static final String CLIP_ASPECT_X = "params:aspectX";
    private static final String CLIP_ASPECT_Y = "params:aspectY";
    private static final String CLIP_TARGET_WIDTH = "params:targetWidth";
    private static final String CLIP_INPUT_IMAGE_PATH = "params:inputImagePath";
    private static final String CLIP_RESULT_NEED_SAMPLE = "params:clipResultNeedInSample";
    /**
     * 图片显示的最大宽度,如果不做限制,{@link #getSmallBitmap(String, int, int)}
     * 在高分辨率手机上,会导致不会采样,导致内存溢出
     */
    private static final int MAX_VISIBLE_WIDTH = 1080;
    private ClipImageView clipImageView;
    private TextView clipTv;
    private int sampleSize;
    private int imageOrientation;
    private int sourceWidth;
    private int sourceHeight;
    private int aspectX;
    private int aspectY;
    private int targetWidth;
    private int targetHeight;
    private String imagePath;
    private String outputPath;
    /**
     * 裁剪结果是否需要采样
     */
    private boolean clipResultNeedInSample;
    private ProgressBar clipProgress;

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    private int readPictureDegree(String path) {
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orientation;
    }

    private void setPictureDegree(String path, int orientation) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(orientation));
            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int imageOrientationToDegree(int imageOrientation) {
        int degree = 0;
        switch (imageOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
        }
        return degree;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_picture);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        int statusBarColor = ContextCompat.getColor(this, R.color.color_ms_primary_dark);
        StatusBarUtil.setTranslucentStatus(this);
        StatusBarUtil.setStatusBarPlaceColor(this, false, statusBarColor);
        StatusBarUtil.addStatusBarHeightMarginTop(toolbar);

        clipImageView = (ClipImageView) findViewById(R.id.clip_image_view);
        clipTv = (TextView) findViewById(R.id.clip_tv);
        clipProgress = (ProgressBar) findViewById(R.id.pbar_clip);
        clipImageView.setOnClipBorderSizeChangedListener(this);
        initClipParams();
    }

    public void clipImage(View view) {
        clipProgress.setVisibility(View.VISIBLE);
        clipTv.setEnabled(false);
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                return clipBigBitmap();
            }

            @Override
            protected void onPostExecute(String saveImagePath) {
                clipProgress.setVisibility(View.GONE);
                clipTv.setEnabled(true);
                sendClipImageResult(saveImagePath);
            }
        }.execute();
    }

    private String clipBigBitmap() {
        // 获取缩放位移后的矩阵值
        final float[] matrixValues = clipImageView.getClipMatrixValues();
        final float scale = matrixValues[Matrix.MSCALE_X];
        final float transX = matrixValues[Matrix.MTRANS_X];
        final float transY = matrixValues[Matrix.MTRANS_Y];

        // 获取在显示的图片中裁剪的位置
        final Rect border = clipImageView.getClipBorder();
        final int cropX = (int) (((-transX + border.left) / scale) * sampleSize);
        final int cropY = (int) (((-transY + border.top) / scale) * sampleSize);
        final int cropWidth = (int) ((border.width() / scale) * sampleSize);
        final int cropHeight = (int) ((border.height() / scale) * sampleSize);

        // 获取在旋转之前的裁剪位置
        final Rect srcRect = new Rect(cropX, cropY, cropX + cropWidth, cropY + cropHeight);
        final Rect clipRect = getRealRect(srcRect);

        // 裁剪
        BitmapRegionDecoder decoder = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        if (clipResultNeedInSample) {
            options.inSampleSize = computeSampleSize(cropWidth, cropHeight, targetWidth, targetHeight);
        }

        String saveImagePath = null;
        try {
            decoder = BitmapRegionDecoder.newInstance(imagePath, false);
            Bitmap bitmap = decoder.decodeRegion(clipRect, options);
            //保存至文件
            saveImagePath = saveBitmapToFile(bitmap);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (decoder != null && !decoder.isRecycled()) {
                decoder.recycle();
            }
        }
        return saveImagePath;
    }

    private void sendClipImageResult(String imagePath) {
        Intent i = new Intent();
        i.putExtra(CLIP_OUTPUT_IMAGE_PATH, imagePath);
        setResult(RESULT_OK, i);
        finish();
    }

    private String saveBitmapToFile(final Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        String saveFilePath = outputPath;
        if (saveFilePath == null) {
            String fileName = String.format("%s.jpg", CLIP_IMAGE_NAME);
            File file = FileUtil.createDiskCacheFile(getApplicationContext(), IMAGE_SAVE_DIR, fileName);
            saveFilePath = file.getAbsolutePath();
        }
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(saveFilePath));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
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
        if (saveFilePath != null && imageOrientation != ExifInterface.ORIENTATION_NORMAL) {
            setPictureDegree(saveFilePath, imageOrientation);
        }
        return saveFilePath;
    }

    private Rect getRealRect(Rect srcRect) {
        switch (imageOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return new Rect(srcRect.top, sourceHeight - srcRect.right,
                        srcRect.bottom, sourceHeight - srcRect.left);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return new Rect(sourceWidth - srcRect.right, sourceHeight - srcRect.bottom,
                        sourceWidth - srcRect.left, sourceHeight - srcRect.top);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return new Rect(sourceWidth - srcRect.bottom, srcRect.left,
                        sourceWidth - srcRect.top, srcRect.right);
            default:
                return srcRect;
        }
    }

    /**
     * 得到压缩后的图片
     *
     * @param imagePath    图片路径
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @return
     */
    public Bitmap getSmallBitmap(String imagePath, int targetWidth, int targetHeight) {
        imageOrientation = readPictureDegree(imagePath);
        boolean rotate = imageOrientation == ExifInterface.ORIENTATION_ROTATE_90 || imageOrientation == ExifInterface.ORIENTATION_ROTATE_270;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        sourceWidth = options.outWidth;
        sourceHeight = options.outHeight;

        int imageWidth = rotate ? options.outHeight : options.outWidth;
        int imageHeight = rotate ? options.outWidth : options.outHeight;

        sampleSize = computeSampleSize(imageWidth, imageHeight, targetWidth, targetHeight);
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        Bitmap srcBitmap = BitmapFactory.decodeFile(imagePath, options);
        Bitmap targetBitmap;
        if (imageOrientation == ExifInterface.ORIENTATION_NORMAL) {
            targetBitmap = srcBitmap;
        } else {
            final Matrix matrix = new Matrix();
            matrix.postRotate(imageOrientationToDegree(imageOrientation));
            targetBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, false);
            if (targetBitmap != srcBitmap && !srcBitmap.isRecycled()) {
                srcBitmap.recycle();
            }
        }
        return targetBitmap;
    }

    private int computeSampleSize(int imageWidth, int imageHeight,
                                  int targetWidth, int targetHeight) {
        int sampleSize = 1;
        while (imageWidth / sampleSize > targetWidth || imageHeight / sampleSize > targetHeight) {
            sampleSize = sampleSize << 1;
        }
        return sampleSize;
    }

    @Override
    public void onClipBorderSizeChanged(int clipBorderWidth, int clipBorderHeight) {
        if (imagePath != null) {
            final int targetWidth = MAX_VISIBLE_WIDTH;
            final int targetHeight = MAX_VISIBLE_WIDTH * aspectY / aspectX;
            Bitmap smallBitmap = getSmallBitmap(imagePath, targetWidth, targetHeight);
            clipImageView.setImageBitmap(smallBitmap);
        }
    }

    private void initClipParams() {
        Intent intent = getIntent();
        if (intent != null) {
            aspectX = intent.getIntExtra(CLIP_ASPECT_X, 1);
            aspectY = intent.getIntExtra(CLIP_ASPECT_Y, 1);
            targetWidth = intent.getIntExtra(CLIP_TARGET_WIDTH, DeviceUtil.getScreenWidth(this));
            targetHeight = aspectY * targetWidth / aspectX;
            imagePath = intent.getStringExtra(CLIP_INPUT_IMAGE_PATH);
            outputPath = intent.getStringExtra(CLIP_OUTPUT_IMAGE_PATH);
            clipResultNeedInSample = intent.getBooleanExtra(CLIP_RESULT_NEED_SAMPLE, false);
        }

        clipImageView.setClipBorderAspect((float) aspectX / aspectY);
    }

    public static class IntentBuilder {

        Intent i;

        public IntentBuilder(Context context) {
            i = new Intent(context, ClipPictureActivity.class);
        }

        public IntentBuilder aspectX(int aspectX) {
            i.putExtra(CLIP_ASPECT_X, aspectX);
            return this;
        }

        public IntentBuilder aspectY(int aspectY) {
            i.putExtra(CLIP_ASPECT_Y, aspectY);
            return this;
        }

        public IntentBuilder inputImagePath(String inputImagePath) {
            i.putExtra(CLIP_INPUT_IMAGE_PATH, inputImagePath);
            return this;
        }

        /**
         * 裁剪结果的文件保存路径,如果不设置,通过{@link #CLIP_OUTPUT_IMAGE_PATH}来取路径
         * 该值可能为null
         *
         * @param outputImagePath
         * @return
         */
        public IntentBuilder outputImagePath(String outputImagePath) {
            i.putExtra(CLIP_OUTPUT_IMAGE_PATH, outputImagePath);
            return this;
        }

        /**
         * 如果裁剪结果需要采样,设置targetWidth,默认为屏幕宽度
         * 使用该值时,请设置{@link #clipResultNeedSample(boolean)} 为true,否则无效
         *
         * @param targetWidth
         * @return
         */
        public IntentBuilder targetWidth(int targetWidth) {
            i.putExtra(CLIP_TARGET_WIDTH, targetWidth);
            return this;
        }

        /**
         * 裁剪结果是否需要采样
         *
         * @param need 默认为false
         * @return
         */
        public IntentBuilder clipResultNeedSample(boolean need) {
            i.putExtra(CLIP_RESULT_NEED_SAMPLE, need);
            return this;
        }

        public Intent build() {
            return i;
        }
    }
}
