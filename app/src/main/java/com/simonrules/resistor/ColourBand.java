package com.simonrules.resistor;

import android.graphics.Color;

public class ColourBand {
    public enum Colour {
        BLACK,
        BROWN,
        RED,
        ORANGE,
        YELLOW,
        GREEN,
        BLUE,
        VIOLET,
        GREY,
        WHITE,
        GOLD,
        SILVER;

    }

    ColourBand(int colour) {

    }

    private double distance(int colourA, int colourB) {
        double dr = Color.red(colourA) - Color.red(colourB);
        double dg = Color.green(colourA) - Color.green(colourB);
        double db = Color.blue(colourA) - Color.blue(colourB);

        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    /*final int BAND_BLACK = Color.parseColor("#000000");
    final int BAND_BROWN = Color.parseColor("#774400");
    final int BAND_RED = Color.parseColor("#FF0000");
    final int BAND_ORANGE = Color.parseColor("#FF7700");
    final int BAND_YELLOW = Color.parseColor("#FFFF00");
    final int BAND_GREEN = Color.parseColor("#00FF00");
    final int BAND_BLUE = Color.parseColor("#0000FF");
    final int BAND_VIOLET = Color.parseColor("#800080");
    final int BAND_GREY = Color.parseColor("#666666");
    final int BAND_WHITE = Color.parseColor("#FFFFFF");
    final int BAND_GOLD = Color.parseColor("#FFD700");
    final int BAND_SILVER = Color.parseColor("#C0C0C0");*/



    private int bandColour;

    public ColourBand(int color) {
        // Determine closest colour in euclidean distance
        for (:
             ) {
            
        }
    }

    public int getColour() {
        return bandColour;
    }
}
