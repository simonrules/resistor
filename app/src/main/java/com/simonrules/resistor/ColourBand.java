package com.simonrules.resistor;

import android.graphics.Color;

public class ColourBand {
    public enum Colour {
        BLACK(Color.parseColor("#000000"), true),
        GREY(Color.parseColor("#666666"), true),
        WHITE(Color.parseColor("#FFFFFF"), true),
        SILVER(Color.parseColor("#C0C0C0"), true),
        BROWN(Color.parseColor("#774400"), false),
        RED(Color.parseColor("#FF0000"), false),
        ORANGE(Color.parseColor("#FF7700"), false),
        YELLOW(Color.parseColor("#FFFF00"), false),
        GREEN(Color.parseColor("#00FF00"), false),
        BLUE(Color.parseColor("#0000FF"), false),
        VIOLET(Color.parseColor("#800080"), false),
        GOLD(Color.parseColor("#FFD700"), false);

        private final int colour;
        private final boolean isGrey;

        Colour(int value, boolean grey) {
            colour = value;
            isGrey = grey;
        }

        public int getColour() {
            return colour;
        }

        public boolean isGrey() {
            return isGrey;
        }
    }

    private Colour colour;

    /*
     * Iterate through all Colour enums and determine the closest colour.
     */
    ColourBand(int colourToMatch) {
        float[] hsv = new float[3];
        Color.colorToHSV(colourToMatch, hsv);
        double hueA = hsv[0];
        double valueA = hsv[2];
        colour = null;

        // Determine if this is a shade of grey and match on lightness if so,
        // because the hue is meaningless.
        if (isShadeOfGrey(colourToMatch)) {
            double min = 1.0; // value max is 1.0
            for (Colour c: Colour.values()) {
                // Skip if not a shade of grey
                if (!c.isGrey()) {
                    continue;
                }

                Color.colorToHSV(c.getColour(), hsv);
                double valueB = hsv[2];

                double dist = Math.abs(valueA - valueB);
                if (dist < min) {
                    min = dist;
                    colour = c;
                }
            }
        } else {
            // Match based on hue
            double min = 180.0; // hue max is 180.0
            for (Colour c : Colour.values()) {
                // Skip if a shade of grey
                if (c.isGrey()) {
                    continue;
                }

                Color.colorToHSV(c.getColour(), hsv);
                double hueB = hsv[0];

                double dist = hueDistance(hueA, hueB);
                if (dist < min) {
                    min = dist;
                    colour = c;
                }
            }
        }
    }

    /*
     * Returns true if the colour is close to a shade of grey (meaning the red, green
     * and blue components differ by less than 5/256 from the average).
     */
    private boolean isShadeOfGrey(int colour) {
        int r = Color.red(colour);
        int g = Color.green(colour);
        int b = Color.blue(colour);

        int average = (r + g + b) / 3;

        return ((Math.abs(r - average) < 5) &&
                (Math.abs(g - average) < 5) &&
                (Math.abs(b - average) < 5));
    }

    /*
     * Returns the distance between the hues in HSV colour space as a value
     * between 0 and 180 degrees.
     */
    private double hueDistance(double hueA, double hueB) {
        double distance = Math.abs(hueA - hueB);

        if (distance > 180.0)
            return 360.0 - distance;
        else
            return distance;
    }

    private double euclideanDistance(int colourA, int colourB) {
        double dr = Color.red(colourA) - Color.red(colourB);
        double dg = Color.green(colourA) - Color.green(colourB);
        double db = Color.blue(colourA) - Color.blue(colourB);

        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    public Colour getColour() {
        return colour;
    }
}
