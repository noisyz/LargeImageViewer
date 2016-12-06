package com.noisyz.largeimageviewer;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.noisyz.largeimageview.LargeImageView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LargeImageView largeImageView = (LargeImageView) findViewById(R.id.image);
        largeImageView.setImageFromAssets("very_large.jpg");
    }
}
