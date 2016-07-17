package com.simonrules.resistor;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.resistor1);
        mWidth = mBitmap.getWidth();
        mHeight = mBitmap.getHeight();

        TextView width = (TextView) findViewById(R.id.width);
        TextView height = (TextView) findViewById(R.id.height);
        mImage = (ImageView) findViewById(R.id.image);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mProgress.setMax(4 * mHeight);
        mProgressStatus = 0;

        width.setText("" + mWidth);
        height.setText("" + mHeight);

        new Thread(new Runnable() {
            public void run() {
                bitmapToLuma();
                gaussianBlur();
                cannyEdgeDetect();
                findBoundingBox();
                locateColourBands();
                lumaToBitmap();

                // Update the image
                mHandler.post(new Runnable() {
                    public void run() {
                        mImage.setImageBitmap(mBitmap);
                        mImage.invalidate();
                    }
                });
            }
        }).start();
    }

    private void bitmapToLuma() {
        mLuma = new int[mWidth * mHeight];
        mBitmap.getPixels(mLuma, 0, mWidth, 0, 0, mWidth, mHeight);

        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int index = i * mWidth + j;
                mLuma[index] = (int)
                        (0.299 * Color.red(mLuma[index]) +
                                0.587 * Color.green(mLuma[index]) +
                                0.114 * Color.blue(mLuma[index]));
            }
        }
    }

    private void lumaToBitmap() {
		/*for (int i = 0; i < mHeight; i++) {
			for (int j = 0; j < mWidth; j++) {
				int index = i * mWidth + j;
				mLuma[index] = Color.rgb(mLuma[index], mLuma[index], mLuma[index]);
			}
		}*/
        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mBitmap.setPixels(mLuma, 0, mWidth, 0, 0, mWidth, mHeight);
    }

    private void gaussianBlur() {
        int[] newLuma = new int[mWidth * mHeight];

        // Horizontal pass
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int index = i * mWidth + j;

                if ((j > 3) && (j < mWidth-3)) {
                    int v = 0;
                    v += mLuma[index-3] * 6;
                    v += mLuma[index-2] * 61;
                    v += mLuma[index-1] * 242;
                    v += mLuma[index] * 383;
                    v += mLuma[index+1] * 242;
                    v += mLuma[index+2] * 61;
                    v += mLuma[index+3] * 6;
                    newLuma[index] = v / 1001;
                }
                else
                    newLuma[index] = mLuma[index];
            }

            // Update the progress bar
            mProgressStatus++;
            mHandler.post(new Runnable() {
                public void run() {
                    mProgress.setProgress(mProgressStatus);
                }
            });
        }

        mLuma = newLuma;
        newLuma = new int[mWidth * mHeight];

        // Vertical pass
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int index = i * mWidth + j;

                if ((i > 3) && (i < mHeight-3)) {
                    int v = 0;
                    v += mLuma[index-3*mWidth] * 6;
                    v += mLuma[index-2*mWidth] * 61;
                    v += mLuma[index-1*mWidth] * 242;
                    v += mLuma[index] * 383;
                    v += mLuma[index+1*mWidth] * 242;
                    v += mLuma[index+2*mWidth] * 61;
                    v += mLuma[index+3*mWidth] * 6;
                    newLuma[index] = v / 1001;
                }
                else
                    newLuma[index] = mLuma[index];
            }

            // Update the progress bar
            mProgressStatus++;
            mHandler.post(new Runnable() {
                public void run() {
                    mProgress.setProgress(mProgressStatus);
                }
            });
        }

        mLuma = newLuma;
    }

    private void cannyEdgeDetect() {
        // Compute derivatives using Sobel filter
        float[] d = new float[mWidth * mHeight];
        float[] theta = new float[mWidth * mHeight];
        for (int i = 1; i < mHeight-1; i++) {
            for (int j = 1; j < mWidth-1; j++) {
                int index = i * mWidth + j;

                // dx
                int dx = 0;
                dx += -1 * mLuma[index-mWidth-1];
                dx += -2 * mLuma[index-1];
                dx += -1 * mLuma[index+mWidth-1];

                dx += 1 * mLuma[index-mWidth+1];
                dx += 2 * mLuma[index+1];
                dx += 1 * mLuma[index+mWidth+1];

                // dy
                int dy = 0;
                dy += -1 * mLuma[index-mWidth-1];
                dy += -2 * mLuma[index-mWidth];
                dy += -1 * mLuma[index-mWidth+1];

                dy += 1 * mLuma[index+mWidth-1];
                dy += 2 * mLuma[index+mWidth];
                dy += 1 * mLuma[index+mWidth+1];

                // Compute magnitude
                d[index] = (float)Math.sqrt(dx * dx + dy * dy);

                // Compute theta
                theta[index] = 0;
                if(dx != 0)
                    theta[index] = (float)Math.atan(dy / dx);
            }

            // Update the progress bar
            mProgressStatus++;
            mHandler.post(new Runnable() {
                public void run() {
                    mProgress.setProgress(mProgressStatus);
                }
            });
        }

        // Clear mLuma
        for (int i = 0; i < mLuma.length; i++)
            mLuma[i] = 0;

        // Non-maximum suppression
        for (int i = 1; i < mHeight-1; i++) {
            for (int j = 1; j < mWidth-1; j++) {
                int index = i * mWidth + j;

                if (d[index] > T_LOW) {
                    // Determine theta prime
                    float deg = (float)Math.toDegrees(theta[index]);
                    if ((deg > 22.5f) && (deg <= 67.5f)) {
                        // / (45 degrees)
                        if (isGreatest(d[index], d[index-mWidth-1], d[index+mWidth+1]) && hysteresis(d, index))
                            mLuma[index] = 255;
                    }
                    else if ((deg > -22.5f) && (deg <= 22.5f)) {
                        // | (0 degrees)
                        if (isGreatest(d[index], d[index-1], d[index+1]) && hysteresis(d, index))
                            mLuma[index] = 255;
                    }
                    else if ((deg > -67.5f) && (deg <= -22.5f)) {
                        // \ (135 degrees)
                        if (isGreatest(d[index], d[index-mWidth+1], d[index+mWidth-1]) && hysteresis(d, index))
                            mLuma[index] = 255;
                    }
                    else {
                        // - (90 degrees)
                        if (isGreatest(d[index], d[index-mWidth], d[index+mWidth]) && hysteresis(d, index))
                            mLuma[index] = 255;
                    }
                }
            }

            // Update the progress bar
            mProgressStatus++;
            mHandler.post(new Runnable() {
                public void run() {
                    mProgress.setProgress(mProgressStatus);
                }
            });
        }
    }

    private boolean isGreatest(float value, float other1, float other2) {
        return ((value > other1) && (value > other2));
    }

    private boolean hysteresis(float[] d, int index) {
        // Pixel is > high threshold
        if (d[index] > T_HIGH)
            return true;

        // Surrounding pixel is > high threshold
        if ((d[index-mWidth-1] > T_HIGH) ||
                (d[index-mWidth] > T_HIGH) ||
                (d[index-mWidth+1] > T_HIGH) ||
                (d[index-1] > T_HIGH) ||
                (d[index+1] > T_HIGH) ||
                (d[index+mWidth-1] > T_HIGH) ||
                (d[index+mWidth] > T_HIGH) ||
                (d[index+mWidth+1] > T_HIGH))
            return true;

        return false;
    }

    private void findBoundingBox() {
        final int threshold = 5;
        int iFirst = -1, iLast = -1, count;
        mLeft = -1;
        mRight = -1;
        mTop = -1;
        mBottom = -1;

        for (int i = 0; i < mHeight; i++) {
            count = 0;
            for (int j = 0; j < mWidth; j++) {
                if (mLuma[i * mWidth + j] > 0)
                    count++;
            }
            if (count > threshold) {
                if (mTop < 0)
                    mTop = i;
                else
                    mBottom = i;
            }
        }

        for (int j = 0; j < mWidth; j++) {
            count = 0;
            for (int i = mTop; i < mBottom; i++) {
                if (mLuma[i * mWidth + j] > 0)
                    count++;
            }
            if (count > threshold) {
                if (mLeft < 0)
                    mLeft = j;
                else
                    mRight = j;
            }
        }

		/*for (int j = mLeft; j < mRight; j++) {
			mLuma[mTop * mWidth + j] = 255;
			mLuma[mBottom * mWidth + j] = 255;
		}

		for (int i = mTop; i < mBottom; i++) {
			mLuma[i * mWidth + mLeft] = 255;
			mLuma[i * mWidth + mRight] = 255;
		}*/
    }

    private void locateColourBands() {
        int pixel, r, g, b;

        // Reduce height of bounding box to 50%
        int h = (mBottom - mTop) / 2 + 1;
        int w = mRight - mLeft;
        mTop += (h / 2);
        mBottom -= (h / 2);

        int[] colourAvg = new int[w];
        int[] rAvg = new int[w];
        int[] gAvg = new int[w];
        int[] bAvg = new int[w];

        // Generate strip of colours
        int i = 0;
        for (int j = mLeft; j < mRight; j++) {
            r = g = b = 0;
            for (i = mTop; i < mBottom; i++) {
                pixel = mBitmap.getPixel(j, i);
                r += Color.red(pixel);
                g += Color.green(pixel);
                b += Color.blue(pixel);
            }
            rAvg[j - mLeft] = r / h;
            gAvg[j - mLeft] = g / h;
            bAvg[j - mLeft] = b / h;
            colourAvg[j - mLeft] = Color.rgb(r / h, g / h, b / h);
        }

        for (i = mTop; i < mBottom; i++) {
            for (int j = mLeft; j < mRight; j++) {
                mLuma[i * mWidth + j] = colourAvg[j - mLeft];
            }
        }

        int[] dr = new int[w];
        int[] dg = new int[w];
        int[] db = new int[w];

        // Generate gradient maps for different colours
        for (int j = 1; j < w-1; j++) {
            dr[j] = Math.abs(-2 * rAvg[j-1] + 2 * rAvg[j+1]);
            dg[j] = Math.abs(-2 * gAvg[j-1] + 2 * gAvg[j+1]);
            db[j] = Math.abs(-2 * bAvg[j-1] + 2 * bAvg[j+1]);
        }

        for (int j = 1; j < w-1; j++) {
            if ((dr[j] > T_LOW) && isGreatest(dr[j], dr[j-1], dr[j+1]))
                mLuma[200 * mWidth + j + mLeft] = Color.RED;
            if ((dg[j] > T_LOW) && isGreatest(dg[j], dg[j-1], dg[j+1]))
                mLuma[202 * mWidth + j + mLeft] = Color.GREEN;
            if ((db[j] > T_LOW) && isGreatest(db[j], db[j-1], db[j+1]))
                mLuma[204 * mWidth + j + mLeft] = Color.BLUE;
        }
    }

    private int mWidth, mHeight;
    private int mLeft, mRight, mTop, mBottom;
    private Bitmap mBitmap;
    int[] mLuma;
    private ImageView mImage;
    private ProgressBar mProgress;
    private Handler mHandler = new Handler();
    private int mProgressStatus;

    final static float T_LOW = 50.0f;
    final static float T_HIGH = 100.0f;
}
