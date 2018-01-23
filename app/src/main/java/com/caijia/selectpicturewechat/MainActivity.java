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
import com.caijia.selectpicture.utils.ToastManager;

import java.util.List;

import static com.caijia.selectpicture.ui.ClipPictureActivity.CLIP_OUTPUT_IMAGE_PATH;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int SELECT_MEDIA_RQ = 6961;
    private static final int REQ_CLIP_IMAGE = 2000;
    private static final int SELECT_MULTI_IMAGE = 9099;
    TextView resultTv;
    ImageView ivClipResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivClipResult = (ImageView) findViewById(R.id.iv_clip_result);
        Button localPicBtn = (Button) findViewById(R.id.local_picture_btn);
        Button lookPic = (Button) findViewById(R.id.btn_only_look);
        Button selectMultiImage = (Button) findViewById(R.id.select_multi_image);
        localPicBtn.setOnClickListener(this);
        selectMultiImage.setOnClickListener(this);
        lookPic.setOnClickListener(this);
        resultTv = (TextView) findViewById(R.id.result_tv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.local_picture_btn: {
                Intent i = new SelectMediaActivity.IntentBuilder(this)
                        .mediaType(MediaType.IMAGE)
                        .canMultiSelect(false)
                        .hasCamera(true)
                        .build();
                startActivityForResult(i, SELECT_MEDIA_RQ);
                break;
            }

            case R.id.select_multi_image:{
                Intent i1 = new SelectMediaActivity.IntentBuilder(this)
                        .mediaType(MediaType.IMAGE)  //选择图片
                        .canMultiSelect(true)       //是否可以多选
                        .maxSelectNum(6)     //多选最大值
                        .hasCamera(true)    //是否可以照相,图片列表第一个为照相按钮
                        .selectedItems(selectedItems)  //初始化选中图片
                        .onlyLook(isOnlyLook) //会自动变为单选模式，并且不能点击图片
                        .build();
                startActivityForResult(i1,SELECT_MULTI_IMAGE);
                break;
            }

            case R.id.btn_only_look:
                isOnlyLook = true;
                break;
        }
    }

    private boolean isOnlyLook;

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
                    selectedItems = args.getParcelableArrayList(SelectMediaActivity.RESULT_MULTI_MEDIA);
                    if (selectedItems != null && !selectedItems.isEmpty()) {
                        MediaBean bean = selectedItems.get(0);
                        resultTv.setText(bean.getPath());
                        Intent i = new ClipPictureActivity.IntentBuilder(this)
                                .aspectX(1)
                                .aspectY(1)
                                .inputImagePath(bean.getPath())
                                .build();
                        startActivityForResult(i, REQ_CLIP_IMAGE);
                    }
                }
                break;
            }

            case SELECT_MULTI_IMAGE:{
                Bundle args = data.getExtras();
                if (args != null) {
                    selectedItems = args.getParcelableArrayList(SelectMediaActivity.RESULT_MULTI_MEDIA);
                    ToastManager.getInstance(this).showToast("逗逼你选了" + selectedItems.size() + "张图没给你显示");
                }
                break;
            }
        }
    }

    private List<MediaBean> selectedItems;
}
