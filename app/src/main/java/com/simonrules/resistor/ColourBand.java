package com.simonrules.resistor;

import android.graphics.Color;

public class ColourBand {
    public enum Colour {
        BLACK(Color.parseColor("#000000")),
        BROWN(Color.parseColor("#774400")),
        RED(Color.parseColor("#FF0000")),
        ORANGE(Color.parseColor("#FF7700")),
        YELLOW(Color.parseColor("#FFFF00")),
        GREEN(Color.parseColor("#00FF00")),
        BLUE(Color.parseColor("#0000FF")),
        VIOLET(Color.parseColor("#800080")),
        GREY(Color.parseColor("#666666")),
        WHITE(Color.parseColor("#FFFFFF")),
        GOLD(Color.parseColor("#FFD700")),
        SILVER(Color.parseColor("#C0C0C0"));

        private final int colour;

        Colour(int value) {
            colour = value;
        }

        public int getColour() {
            return colour;
        }
    }

    private Colour colour;

    /*
     * Iterate through all Colour enums and determine the closest colour in
     * euclidean distance.
     */
    ColourBand(int colourToMatch) {
        colour = null;

        double min = 9999.9;
        for (Colour c: Colour.values()) {
            double dist = distance(c.getColour(), colourToMatch);
            if (dist < min) {
                min = dist;
                colour = c;
            }
        }
    }

    private double distance(int colourA, int colourB) {
        double dr = Color.red(colourA) - Color.red(colourB);
        double dg = Color.green(colourA) - Color.green(colourB);
        double db = Color.blue(colourA) - Color.blue(colourB);

        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    public Colour getColour() {
        return colour;
    }
}
