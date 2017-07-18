package com.caijia.selectpicturewechat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.caijia.selectpicture.bean.MediaBean;
import com.caijia.selectpicture.ui.ClipPictureActivity;
import com.caijia.selectpicture.ui.SelectMediaActivity;
import com.caijia.selectpicture.utils.MediaType;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int SELECT_MEDIA_RQ = 6961;
    TextView resultTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                Intent i = SelectMediaActivity.getIntent(this, MediaType.IMAGE);
                startActivityForResult(i, SELECT_MEDIA_RQ);
                break;
            }

            case R.id.clip_image_btn:{
                Intent i = new Intent(this, ClipPictureActivity.class);
                startActivity(i);
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

        if (requestCode == SELECT_MEDIA_RQ) {
            Bundle args = data.getExtras();
            if (args != null) {
                MediaBean mediaBean = args.getParcelable(SelectMediaActivity.RESULT_MEDIA);
                if (mediaBean != null) {
                    resultTv.setText(mediaBean.toString());
                }
            }
        }
    }
}
