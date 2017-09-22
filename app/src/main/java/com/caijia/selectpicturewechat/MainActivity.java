package com.caijia.selectpicturewechat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.caijia.selectpicture.bean.MediaBean;
import com.caijia.selectpicture.ui.ClipPictureActivity;
import com.caijia.selectpicture.ui.SelectMediaActivity;
import com.caijia.selectpicture.utils.ImageLoader;
import com.caijia.selectpicture.utils.MediaType;

import static com.caijia.selectpicture.ui.ClipPictureActivity.CLIP_OUTPUT_IMAGE_PATH;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int SELECT_MEDIA_RQ = 6961;
    private static final int REQ_CLIP_IMAGE = 2000;
    TextView resultTv;
    ImageView ivClipResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivClipResult = (ImageView) findViewById(R.id.iv_clip_result);
        Button localPicBtn = (Button) findViewById(R.id.local_picture_btn);
        Button clipImageBtn = (Button) findViewById(R.id.clip_image_btn);
        localPicBtn.setOnClickListener(this);
        clipImageBtn.setOnClickListener(this);
        resultTv = (TextView) findViewById(R.id.result_tv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.local_picture_btn: {
                Intent i = new SelectMediaActivity.IntentBuilder(this)
                        .mediaType(MediaType.IMAGE)
                        .canMultiSelect(false)
                        .maxSelectNum(6)
                        .hasCamera(true)
                        .build();
                startActivityForResult(i, SELECT_MEDIA_RQ);
                break;
            }

            case R.id.clip_image_btn:{
                Intent i1 = new SelectMediaActivity.IntentBuilder(this)
                        .mediaType(MediaType.IMAGE_VIDEO)
                        .canMultiSelect(true)
                        .maxSelectNum(6)
                        .hasCamera(true)
                        .build();
                startActivity(i1);
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }

        switch (requestCode) {
            case REQ_CLIP_IMAGE:{
                Bundle args = data.getExtras();
                if (args != null) {
                    String imagePath = args.getString(CLIP_OUTPUT_IMAGE_PATH);
                    ImageLoader.getInstance().loadImage(imagePath,ivClipResult,false,R.drawable.ic_sm_image_default_bg);
                }
                break;
            }

            case SELECT_MEDIA_RQ:{
                Bundle args = data.getExtras();
                if (args != null) {
                    MediaBean mediaBean = args.getParcelable(SelectMediaActivity.RESULT_MEDIA);
                    if (mediaBean != null) {
                        resultTv.setText(mediaBean.getPath());
                        Intent i = new ClipPictureActivity.IntentBuilder(this)
                                .aspectX(1)
                                .aspectY(1)
                                .inputImagePath(mediaBean.getPath())
                                .build();
                        startActivityForResult(i, REQ_CLIP_IMAGE);
                    }
                }
                break;
            }
        }
    }
}
