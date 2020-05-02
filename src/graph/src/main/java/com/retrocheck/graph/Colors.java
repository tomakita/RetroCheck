package com.retrocheck.graph;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

/**
 * Contains a method to generate N visually distinct colors and helper methods.
 *
 * @author Melinda Green, with some additions by me.
 */
public class Colors {
    private Colors() {
    } // To disallow instantiation.

    private final static float
            U_OFF = .436f,
            V_OFF = .615f;
    private static final long RAND_SEED = Instant.now().toEpochMilli();
    //private static Random rand = new Random(RAND_SEED);
    // we don't want to use the same seed every time
    private static Random rand = new Random();

    public static String toHex(Color color) {
        return "#"+Integer.toHexString(color.getRGB()).substring(2);
    }

    /*
     * Returns an array of ncolors RGB triplets such that each is as unique from the rest as possible
     * and each color has at least one component greater than minComponent and one less than maxComponent.
     * Use min == 1 and max == 0 to include the full RGB color range.
     *
     * Warning: O N^2 algorithm blows up fast for more than 100 colors.
     */
    public static Color[] generateVisuallyDistinctColors(int ncolors, float minComponent, float maxComponent) {//, Set<String> existingColorsHex) {
        //rand.setSeed(RAND_SEED); // So that we get consistent results for each combination of inputs

        float[][] yuv = new float[ncolors][3];

        // initialize array with random colors
        for (int got = 0; got < ncolors; ) {
            System.arraycopy(randYUVinRGBRange(minComponent, maxComponent), 0, yuv[got++], 0, 3);
        }
        // continually break up the worst-fit color pair until we get tired of searching
        for (int c = 0; c < ncolors * 1000; c++) {
            float worst = 8888;
            int worstID = 0;
            for (int i = 1; i < yuv.length; i++) {
                for (int j = 0; j < i; j++) {
                    float dist = sqrdist(yuv[i], yuv[j]);
                    if (dist < worst) {
                        worst = dist;
                        worstID = i;
                    }
                }
            }
            float[] best = randYUVBetterThan(worst, minComponent, maxComponent, yuv);
            if (best == null)
                break;
            else
                yuv[worstID] = best;
        }

        Color[] rgbs = new Color[yuv.length];
        for (int i = 0; i < yuv.length; i++) {
            float[] rgb = new float[3];
            yuv2rgb(yuv[i][0], yuv[i][1], yuv[i][2], rgb);
            rgbs[i] = new Color(rgb[0], rgb[1], rgb[2]);
            //System.out.println(rgb[i][0] + "\t" + rgb[i][1] + "\t" + rgb[i][2]);
        }

//        // check rgbs for matches in input list -- if any, recurse on the # of duplicates.
//        long duplicates = Arrays.stream(rgbs).filter(rgb -> existingColorsHex.contains(Colors.toHex(rgb))).count();
//
//        // then merge back into rgbs.

        return rgbs;
    }

    // From http://en.wikipedia.org/wiki/YUV#Mathematical_derivations_and_formulas
    public static void yuv2rgb(float y, float u, float v, float[] rgb) {
        rgb[0] = 1 * y + 0 * u + 1.13983f * v;
        rgb[1] = 1 * y + -.39465f * u + -.58060f * v;
        rgb[2] = 1 * y + 2.03211f * u + 0 * v;
    }

    private static float[] randYUVinRGBRange(float minComponent, float maxComponent) {
        while (true) {
            float y = rand.nextFloat(); // * YFRAC + 1-YFRAC);
            float u = rand.nextFloat() * 2 * U_OFF - U_OFF;
            float v = rand.nextFloat() * 2 * V_OFF - V_OFF;
            float[] rgb = new float[3];
            yuv2rgb(y, u, v, rgb);
            float r = rgb[0], g = rgb[1], b = rgb[2];
            if (0 <= r && r <= 1 &&
                    0 <= g && g <= 1 &&
                    0 <= b && b <= 1 &&
                    (r > minComponent || g > minComponent || b > minComponent) && // don't want all dark components
                    (r < maxComponent || g < maxComponent || b < maxComponent)) // don't want all light components

                return new float[]{y, u, v};
        }
    }

    private static float sqrdist(float[] a, float[] b) {
        float sum = 0;
        for (int i = 0; i < a.length; i++) {
            float diff = a[i] - b[i];
            sum += diff * diff;
        }
        return sum;
    }

    private static float[] randYUVBetterThan(float bestDistSqrd, float minComponent, float maxComponent, float[][] in) {
        for (int attempt = 1; attempt < 100 * in.length; attempt++) {
            float[] candidate = randYUVinRGBRange(minComponent, maxComponent);
            boolean good = true;
            for (int i = 0; i < in.length; i++)
                if (sqrdist(candidate, in[i]) < bestDistSqrd)
                    good = false;
            if (good)
                return candidate;
        }
        return null; // after a bunch of passes, couldn't find a candidate that beat the best.
    }
}
