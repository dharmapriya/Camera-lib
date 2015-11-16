package com.ivy.android.ivycamera;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.ivy.sd.camera.CameraActivity;


public class MainActivity extends ActionBarActivity {
    private Button imgcapture;
    private ImageView displayimg;
    private String imgpathsaved = "";
    // Test Abbas
    // Test Gp
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgcapture = (Button) findViewById(R.id.imgcapture);
        displayimg = (ImageView) findViewById(R.id.displayimg);
        try {
            if (getIntent().hasExtra("displayimg")) {
                imgpathsaved = getIntent().getStringExtra("displayimg");
            } else {
                imgpathsaved = "";
            }
        } catch (NullPointerException e) {
            imgpathsaved = "";
        }
        imgcapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(i);
                finish();

            }
        });
        if (imgpathsaved.equals("")) {

        } else {
            displayimg.setImageURI(Uri.parse(imgpathsaved));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
