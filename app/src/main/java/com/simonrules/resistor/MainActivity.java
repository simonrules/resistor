package com.simonrules.resistor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
    private BitmapProcessor mBitmapProcessor;
    private ImageView mImage;
    private ProgressBar mProgress;
    private Handler mHandler = new Handler();
    private int mProgressStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBitmapProcessor = new BitmapProcessor(BitmapFactory.decodeResource(getResources(),
                R.drawable.resistor4));

        mImage = (ImageView) findViewById(R.id.image);
        mImage.setImageBitmap(mBitmapProcessor.getColourBitmap());
        mImage.invalidate();

        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mProgress.setMax(4 * mBitmapProcessor.getHeight());
        mProgressStatus = 0;

        TextView statusText = (TextView) findViewById(R.id.statusText);
        statusText.setText("Image size: " + mBitmapProcessor.getWidth() + "x" +
                mBitmapProcessor.getHeight());

        Button processButton = (Button)findViewById(R.id.processButton);

        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processImage();
            }
        });
    }

    private void processImage() {
        new Thread(new Runnable() {
            public void run() {
                // Update the image
                mHandler.post(new Runnable() {
                    public void run() {
                        mImage.setImageBitmap(mBitmapProcessor.getScaledColourBitmap(mBitmapProcessor.getWidth() / 8, mBitmapProcessor.getHeight() / 8));
                        mImage.invalidate();
                    }
                });
            }
        }).start();
    }
}
