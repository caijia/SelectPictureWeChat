package com.caijia.selectpicturewechat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialcamera.MaterialCamera;
import com.caijia.selectpicture.bean.MediaBean;
import com.caijia.selectpicture.ui.SelectMediaActivity;
import com.caijia.selectpicture.utils.MediaType;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int CAMERA_RQ = 6969;
    private final static int SELECT_MEDIA_RQ = 6961;
    TextView resultTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button localPicBtn = (Button) findViewById(R.id.local_picture_btn);
        localPicBtn.setOnClickListener(this);

        Button recordVideoBtn = (Button) findViewById(R.id.record_video_btn);
        recordVideoBtn.setOnClickListener(this);

        resultTv = (TextView) findViewById(R.id.result_tv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.local_picture_btn: {
                Intent i = SelectMediaActivity.getIntent(this, MediaType.IMAGE_VIDEO);
                startActivityForResult(i, SELECT_MEDIA_RQ);
                break;
            }

            case R.id.record_video_btn: {
                File saveFolder = new File(Environment.getExternalStorageDirectory(),
                        "Bst" + File.separator + "RecordVideo");
                saveFolder.mkdirs();
                new MaterialCamera(this)
                        .saveDir(saveFolder)
                        .showPortraitWarning(false)
                        .labelRetry(R.string.record_retry)
                        .labelConfirm(R.string.record_confirm)
                        .countdownMinutes(5f)
                        .start(CAMERA_RQ);
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

        // Received recording or error from MaterialCamera
        if (requestCode == CAMERA_RQ) {
            resultTv.setText(data.getDataString());
            Uri contentUri = Uri.fromFile(new File(data.getDataString().replaceFirst("file://","")));
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,contentUri);
            sendBroadcast(mediaScanIntent);

        } else if (requestCode == SELECT_MEDIA_RQ) {
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
