package com.simonrules.resistor;

import android.graphics.Bitmap;
import android.graphics.Color;

public class Resistor {
    public Resistor(Bitmap b) {
        bitmap = b;

        generateColourStrip();
    }

    private void generateColourStrip() {
        final float[] kernel = {1.0f, 10.0f, 45.0f, 120.0f, 210.0f, 252.0f, 210.0f, 120.0f, 45.0f, 10.0f, 1.0f};
        final int kernelSum = 1024;

        // Create bitmap scaled in height to match the kernel size
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), kernel.length, true);

        // Loop through each vertical image strip
        int[] rgbPixels = new int[scaled.getWidth()];
        int rAccum, gAccum, bAccum;
        int rMin = 255, gMin = 255, bMin = 255;
        int rMax = 0, gMax = 0, bMax = 0;
        for (int x = 0; x < scaled.getWidth(); x++) {
            rAccum = 0;
            gAccum = 0;
            bAccum = 0;

            // Weight each colour, giving preference to central band
            for (int y = 0; y < kernel.length; y++) {
                int c = scaled.getPixel(x, y);

                rAccum += (int)(kernel[y] * (float)Color.red(c));
                gAccum += (int)(kernel[y] * (float)Color.green(c));
                bAccum += (int)(kernel[y] * (float)Color.blue(c));
            }

            // Store each generated colour
            int r = rAccum / kernelSum;
            int g = gAccum / kernelSum;
            int b = bAccum / kernelSum;

            // Keep track of min/max values
            if (rMin > r) {
                rMin = r;
            } else if (rMax < r) {
                rMax = r;
            }
            if (gMin > g) {
                gMin = g;
            } else if (gMax < g) {
                gMax = g;
            }
            if (bMin > b) {
                bMin = b;
            } else if (bMax < b) {
                bMax = b;
            }

            rgbPixels[x] = Color.rgb(r, g, b);
            ColourBand colourBand = new ColourBand(rgbPixels[x]);
            System.out.println(colourBand.getColour());
        }

        // Obtain lowest of the three maximum r, g, b values for scaling
        int high = getMin(rMax, gMax, bMax);
        int low = getMax(rMin, gMin, bMin);
        // scaleFactor >= 1 and is used to scale up the dynamic range
        float scaleFactor = 255.0f / (float)(high - low);

        // Scale the RGB values to increase the dynamic range
        for (int x = 0; x < rgbPixels.length; x++) {
            int r = Color.red(rgbPixels[x]);
            int g = Color.green(rgbPixels[x]);
            int b = Color.blue(rgbPixels[x]);

            r = (int)((float)(r - low) * scaleFactor);
            g = (int)((float)(g - low) * scaleFactor);
            b = (int)((float)(b - low) * scaleFactor);

            rgbPixels[x] = Color.rgb(r, g, b);
        }

        Bitmap rgbBitmap = Bitmap.createBitmap(rgbPixels, scaled.getWidth(), 1, Bitmap.Config.ARGB_8888);
        Bitmap rgbBitmapScaled = Bitmap.createScaledBitmap(rgbBitmap, scaled.getWidth(), 11, false);
        Bitmap rgbBitmapCropped = Bitmap.createBitmap(rgbBitmapScaled, 110, 0, 30, 11);
    }

    private int getMin(int a, int b, int c) {
        if (a < b) {
            if (a < c) {
                return a;
            } else {
                return c;
            }
        } else {
            if (b < c) {
                return b;
            } else {
                return c;
            }
        }
    }

    private int getMax(int a, int b, int c) {
        if (a > b) {
            if (a > c) {
                return a;
            } else {
                return c;
            }
        } else {
            if (b > c) {
                return b;
            } else {
                return c;
            }
        }
    }

    private Bitmap bitmap;
}
