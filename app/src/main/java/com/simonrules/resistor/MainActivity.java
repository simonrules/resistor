package com.simonrules.resistor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBitmapProcessor = new BitmapProcessor(BitmapFactory.decodeResource(getResources(),
                R.drawable.resistor4));

        mImage = (ImageView) findViewById(R.id.image);
        mImage.setImageBitmap(mBitmapProcessor.getColourBitmap());
        mImage.invalidate();

        TextView statusText = (TextView) findViewById(R.id.statusText);
        statusText.setText("Image size: " + mBitmapProcessor.getWidth() + "x" +
                mBitmapProcessor.getHeight());

        Bitmap small = mBitmapProcessor.getScaledColourBitmap(mBitmapProcessor.getWidth() / 8, mBitmapProcessor.getHeight() / 8);
        for (int x = 0; x < small.getWidth(); x++) {
            ColourBand colourBand = new ColourBand(small.getPixel(x, 3));
            System.out.println(colourBand.getColour());
        }

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
                        mImage.setImageBitmap(mBitmapProcessor.getScaledColourBitmap(mBitmapProcessor.getWidth() / 16, mBitmapProcessor.getHeight() / 16));
                        mImage.invalidate();
                    }
                });
            }
        }).start();
    }
}
