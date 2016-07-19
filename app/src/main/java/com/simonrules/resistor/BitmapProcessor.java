package com.simonrules.resistor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;

/**
 * The BitmapProcessor contains operations for adjusting Bitmaps.
 */
public class BitmapProcessor {
    private int mWidth, mHeight;
    int[] mColour;
    int[] mLuma;
    Rect mRect;

    final float T_LOW = 50.0f;
    final float T_HIGH = 100.0f;

    public BitmapProcessor(Bitmap bitmap) {
        mWidth = bitmap.getWidth();
        mHeight = bitmap.getHeight();

        mColour = new int[mWidth * mHeight];
        bitmap.getPixels(mColour, 0, mWidth, 0, 0, mWidth, mHeight);
        computeLuma();
        gaussianBlurLuma();
        cannyEdgeDetectLuma();
        findBoundingBox();
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public Bitmap getColourBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(mColour, mWidth, mHeight, Bitmap.Config.ARGB_8888);

        return bitmap;
    }

    public Bitmap getLumaBitmap() {
        int[] pixels = new int[mWidth * mHeight];

        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int index = i * mWidth + j;
                pixels[index] = Color.rgb(mLuma[index], mLuma[index], mLuma[index]);
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(pixels, mWidth, mHeight, Bitmap.Config.ARGB_8888);

        return bitmap;
    }

    /*
     * This method converts an RGB bitmap into an array of ints containing only the
     * luma (brightness) data. The brightness values are values of 0-255 stored in ints.
     */
    private void computeLuma() {
        mLuma = new int[mWidth * mHeight];

        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int index = i * mWidth + j;
                mLuma[index] = (int)
                        (0.299 * Color.red(mColour[index]) +
                         0.587 * Color.green(mColour[index]) +
                         0.114 * Color.blue(mColour[index]));
            }
        }
    }

    private void gaussianBlurLuma() {
        int[] newLuma = new int[mWidth * mHeight];

        // Horizontal pass
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int index = i * mWidth + j;

                if ((j > 3) && (j < mWidth - 3)) {
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
        }

        mLuma = newLuma;
        newLuma = new int[mWidth * mHeight];

        // Vertical pass
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int index = i * mWidth + j;

                if ((i > 3) && (i < mHeight-3)) {
                    int v = 0;
                    v += mLuma[index - 3 * mWidth] * 6;
                    v += mLuma[index - 2 * mWidth] * 61;
                    v += mLuma[index - 1 * mWidth] * 242;
                    v += mLuma[index] * 383;
                    v += mLuma[index + 1 * mWidth] * 242;
                    v += mLuma[index + 2 * mWidth] * 61;
                    v += mLuma[index + 3 * mWidth] * 6;
                    newLuma[index] = v / 1001;
                }
                else
                    newLuma[index] = mLuma[index];
            }
        }

        mLuma = newLuma;
    }

    private void cannyEdgeDetectLuma() {
        // Compute derivatives using Sobel filter
        float[] d = new float[mWidth * mHeight];
        float[] theta = new float[mWidth * mHeight];
        for (int i = 1; i < mHeight - 1; i++) {
            for (int j = 1; j < mWidth - 1; j++) {
                int index = i * mWidth + j;

                // dx
                int dx = 0;
                dx += -1 * mLuma[index - mWidth - 1];
                dx += -2 * mLuma[index - 1];
                dx += -1 * mLuma[index + mWidth - 1];

                dx += 1 * mLuma[index - mWidth + 1];
                dx += 2 * mLuma[index + 1];
                dx += 1 * mLuma[index + mWidth + 1];

                // dy
                int dy = 0;
                dy += -1 * mLuma[index - mWidth - 1];
                dy += -2 * mLuma[index - mWidth];
                dy += -1 * mLuma[index - mWidth + 1];

                dy += 1 * mLuma[index + mWidth - 1];
                dy += 2 * mLuma[index + mWidth];
                dy += 1 * mLuma[index + mWidth + 1];

                // Compute magnitude
                d[index] = (float)Math.sqrt(dx * dx + dy * dy);

                // Compute theta
                theta[index] = 0;
                if(dx != 0)
                    theta[index] = (float)Math.atan(dy / dx);
            }
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
                        if (isGreatest(d[index], d[index - mWidth - 1], d[index + mWidth + 1]) && hysteresis(d, index))
                            mLuma[index] = 255;
                    }
                    else if ((deg > -22.5f) && (deg <= 22.5f)) {
                        // | (0 degrees)
                        if (isGreatest(d[index], d[index - 1], d[index + 1]) && hysteresis(d, index))
                            mLuma[index] = 255;
                    }
                    else if ((deg > -67.5f) && (deg <= -22.5f)) {
                        // \ (135 degrees)
                        if (isGreatest(d[index], d[index - mWidth + 1], d[index + mWidth - 1]) && hysteresis(d, index))
                            mLuma[index] = 255;
                    }
                    else {
                        // - (90 degrees)
                        if (isGreatest(d[index], d[index - mWidth], d[index + mWidth]) && hysteresis(d, index))
                            mLuma[index] = 255;
                    }
                }
            }
        }
    }

    /*
     * Returns true if value is the greatest of the three values
     */
    private boolean isGreatest(float value, float other1, float other2) {
        return ((value > other1) && (value > other2));
    }

    private boolean hysteresis(float[] d, int index) {
        // Pixel is > high threshold
        if (d[index] > T_HIGH)
            return true;

        // Surrounding pixel is > high threshold
        if ((d[index - mWidth - 1] > T_HIGH) ||
                (d[index - mWidth] > T_HIGH) ||
                (d[index - mWidth + 1] > T_HIGH) ||
                (d[index - 1] > T_HIGH) ||
                (d[index + 1] > T_HIGH) ||
                (d[index + mWidth - 1] > T_HIGH) ||
                (d[index + mWidth] > T_HIGH) ||
                (d[index + mWidth + 1] > T_HIGH))
            return true;

        return false;
    }

    /*
     * This method walks the image from each side (in from the top, in from the left, etc)
     * and stops when it finds a number of pixels in the scanline exceeding the threshold.
     * It then uses this to determine a bounding box.
     */
    private void findBoundingBox() {
        final int threshold = 5;
        int i, j, count;
        mRect = new Rect();

        // Walk from top
        for (i = 0; i < mHeight; i++) {
            count = 0;
            for (j = 0; j < mWidth; j++) {
                if (mLuma[i * mWidth + j] > 0)
                    count++;
            }
            if (count > threshold) {
                mRect.top = i;
                break;
            }
        }

        // Walk from bottom
        for (i = mHeight - 1; i >= 0; i--) {
            count = 0;
            for (j = 0; j < mWidth; j++) {
                if (mLuma[i * mWidth + j] > 0)
                    count++;
            }
            if (count > threshold) {
                mRect.bottom = i;
                break;
            }
        }

        // Walk from left
        for (j = 0; j < mWidth; j++) {
            count = 0;
            for (i = mRect.top; i < mRect.bottom; i++) {
                if (mLuma[i * mWidth + j] > 0)
                    count++;
            }
            if (count > threshold) {
                mRect.left = j;
                break;
            }
        }

        // Walk from right
        for (j = mWidth - 1; j >= 0; j--) {
            count = 0;
            for (i = mRect.top; i < mRect.bottom; i++) {
                if (mLuma[i * mWidth + j] > 0)
                    count++;
            }
            if (count > threshold) {
                mRect.right = j;
                break;
            }
        }

        // Draw bounding box in luma
		for (j = mRect.left; j < mRect.right; j++) {
			mLuma[mRect.top * mWidth + j] = 255;
			mLuma[mRect.bottom * mWidth + j] = 255;
		}

		for (i = mRect.top; i < mRect.bottom; i++) {
			mLuma[i * mWidth + mRect.left] = 255;
			mLuma[i * mWidth + mRect.right] = 255;
		}
    }

    private void locateColourBands() {
        int pixel, r, g, b;

        // Reduce height of bounding box to 50%
        int h = mRect.height() / 2;
        int w = mRect.width();
        mRect.top += (h / 2);
        mRect.bottom -= (h / 2);

        int[] colourAvg = new int[w];
        int[] rAvg = new int[w];
        int[] gAvg = new int[w];
        int[] bAvg = new int[w];

        // Generate strip of colours
        int i = 0;
        for (int j = mRect.left; j < mRect.right; j++) {
            r = g = b = 0;
            for (i = mRect.top; i < mRect.bottom; i++) {
                pixel = mColour[i * mWidth + j];
                r += Color.red(pixel);
                g += Color.green(pixel);
                b += Color.blue(pixel);
            }
            rAvg[j - mRect.left] = r / h;
            gAvg[j - mRect.left] = g / h;
            bAvg[j - mRect.left] = b / h;
            colourAvg[j - mRect.left] = Color.rgb(r / h, g / h, b / h);
        }

        for (i = mRect.top; i < mRect.bottom; i++) {
            for (int j = mRect.left; j < mRect.right; j++) {
                mLuma[i * mWidth + j] = colourAvg[j - mRect.left];
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
                mLuma[200 * mWidth + j + mRect.left] = Color.RED;
            if ((dg[j] > T_LOW) && isGreatest(dg[j], dg[j-1], dg[j+1]))
                mLuma[202 * mWidth + j + mRect.left] = Color.GREEN;
            if ((db[j] > T_LOW) && isGreatest(db[j], db[j-1], db[j+1]))
                mLuma[204 * mWidth + j + mRect.left] = Color.BLUE;
        }
    }
}
