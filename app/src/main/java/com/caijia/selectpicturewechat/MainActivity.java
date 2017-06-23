package com.caijia.selectpicturewechat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.caijia.selectpicture.ui.PictureActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button localPicBtn = (Button) findViewById(R.id.local_picture_btn);
        localPicBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.local_picture_btn:{
                Intent i = new Intent(this, PictureActivity.class);
                startActivity(i);
                break;
            }
        }
    }
}
