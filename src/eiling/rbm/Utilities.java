package eiling.rbm;

/**
 * Created by Bernhard on 28.02.14.
 */
public class Utilities {

    private static double calcMSE(ImageView view1, int[] pix2) {

        int[] pix1 = view1.getPixels();
        int mse = 0;

        for (int i = 0; i < pix1.length; i++) {
            int p1 = (pix1[i] & 0xff);
            int p2 = (pix2[i] & 0xff);

            int error = p1 - p2;
            mse += error * error;
        }
        mse /= pix1.length;
        return mse;
    }

    public static double calcPSNR(ImageView view1, int[] pix2) {
        double mse = calcMSE(view1, pix2);
        double psnr = 10.0d * Math.log10(65025.0d / mse);
        return psnr;
    }

    public static int[] generateNegativeImage(int[] pixels1, int[] pixels2) {
        int[] negPixels = new int[pixels1.length];
        for (int i = 0; i < pixels1.length; i++) {
            int negVal = (pixels1[i] & 0xff) - (pixels2[i] & 0xff) + 128;
            negPixels[i] = 0xFF000000 + ((negVal & 0xff) << 16) + ((negVal & 0xff) << 8)
                    + (negVal & 0xff);
        }
        return negPixels;
    }

    public static double getContrastRatio(ImageView view) {
        int[] pixels = view.getPixels();

        int average = 0;
        for(int pixel : pixels) {
            int val = (pixel & 0xff0000) >> 16;
            average += val;
        }
        average /= pixels.length;

        double ratio = 0;
        for(int pixel : pixels) {
            int val = (pixel & 0xff0000) >> 16;
            int delta = val - average;
            ratio += delta * delta;
        }
        ratio /= pixels.length;
        ratio = Math.sqrt(ratio);

        return ratio;
    }
}
