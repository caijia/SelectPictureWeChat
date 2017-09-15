package com.caijia.selectpicture.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.caijia.selectpicture.R;
import com.caijia.selectpicture.widget.ClipImageView;

import java.io.File;
import java.io.IOException;

/**
 * 裁剪照片
 * Created by cai.jia on 2017/7/18 0018
 */
public class ClipPictureActivity extends AppCompatActivity {

    public static final String CLIP_BITMAP = "params:clip_bitmap";
    private static final String CLIP_IMAGE_PATH = "params:image_path";

    private ClipImageView clipImageView;
    private String imagePath;

    public static Intent getIntent(Context context,String imagePath) {
        Intent i = new Intent(context, ClipPictureActivity.class);
        i.putExtra(CLIP_IMAGE_PATH, imagePath);
        return i;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_picture);
        clipImageView = (ClipImageView) findViewById(R.id.clip_image_view);

        if (getIntent() != null && getIntent().getExtras() != null) {
            imagePath = getIntent().getExtras().getString(CLIP_IMAGE_PATH);
        }

        if (imagePath != null) {
            clipImageView.setImageURI(Uri.fromFile(new File(imagePath)));
        }
    }

    public void clipImage(View view) {
        Bitmap bitmap = clipImageView.clip();
        Intent i = new Intent();
        i.putExtra(CLIP_BITMAP, bitmap);
        setResult(RESULT_OK, i);
        finish();
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
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
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("clipImage", "error");
        }
        return degree;
    }
}
