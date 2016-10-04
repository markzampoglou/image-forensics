package gr.iti.mklab.reveal.forensics.maps.ghost;

/**
 * Created by marzampoglou on 11/3/15.
 */

import gr.iti.mklab.reveal.forensics.util.Util;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;


public class GhostCalculationThread implements Callable<GhostCalculationResult>{

    private int quality;
    private BufferedImage imIn;
    private int[][][] OrigByteImage;
    private int maxImageSmallDimension;

    public GhostCalculationThread(int quality, BufferedImage imIn, int[][][] origByteImage, int maxImageSmallDimension) {
        this.quality = quality;
        this.imIn = imIn;
        this.OrigByteImage = origByteImage;
        this.maxImageSmallDimension =maxImageSmallDimension;
    }

    @Override
    /**
     * Returns an ImageDownloadResult object from where the BufferedImage object and the image identifier can be
     * obtained.
     */
    public GhostCalculationResult call() throws Exception {
        float[][] difference = calculateDifference();
        return new GhostCalculationResult(quality, difference);
    }

    public float[][] calculateDifference() {

        int newHeight, newWidth;
        float scaleFactor;

        int imageHeight= imIn.getHeight();
        int imageWidth= imIn.getWidth();
        if (imageHeight>imageWidth & imageWidth> maxImageSmallDimension *1.5) {
            newWidth= maxImageSmallDimension;
            scaleFactor=((float)imageWidth)/((float)newWidth);
            newHeight=Math.round(((float)imageHeight)/scaleFactor);
        } else if (imageWidth>imageHeight & imageHeight> maxImageSmallDimension *1.5)
        {
            newHeight= maxImageSmallDimension;
            scaleFactor=((float)imageHeight)/((float)newHeight);
            newWidth=Math.round(((float)imageWidth)/scaleFactor);
        } else {
            newHeight=imageHeight;
            newWidth=imageWidth;
            scaleFactor=1;
        }

        BufferedImage recompressedImage = Util.recompressImage(imIn, quality);
        //int[][][] RecompressedByteImage = Util.getRGBArray(recompressedImage);
        //float[][][] resizedImageDifference = Util.getImageDifference(OrigByteImage, RecompressedByteImage);
        float[][][] resizedImageDifference = Util.getResizedImageDifference(imIn, recompressedImage, newWidth, newHeight);


        int filterSize=Math.round(17 / scaleFactor);  //17 acc. to the paper
        if (filterSize<2){filterSize=2;}
        float[][][] smooth = Util.meanFilterThreeChannelImage(resizedImageDifference, filterSize);
        float[][] grayDifference = Util.convertToSingleChannel(smooth);
        //float ghostMin = Util.minDouble2DArray(grayDifference);
        //float ghostMax = Util.maxDouble2DArray(grayDifference);
        //float ghostMean = Util.SingleChannelMean(grayDifference);
        //JetImageDifference=Util.visualizeWithJet(Util.normalizeIm(MeanDifference));
        //JetImageDifference=(BufferedImage) JetImageDifference.getScaledInstance(newWidth,newHeight, Image.SCALE_FAST);

        if (newHeight<imageHeight) {
            grayDifference = Util.scaleDownArray(grayDifference, newWidth, newHeight);
        }

        return grayDifference;
        //System.out.println(quality);
    }


}
