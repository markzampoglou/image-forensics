package maps.ghost;

/**
 * Created by marzampoglou on 11/3/15.
 */

import util.Util;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;


public class GhostCalculation implements Callable<GhostCalculationResult>{

    private int Quality;
    private BufferedImage ImIn;
    private int[][][] OrigByteImage;
    private int MaxImageSmallDimension;

    public GhostCalculation(int Quality, BufferedImage ImIn, int[][][] OrigByteImage, int MaxImageSmallDimension) {
        this.Quality = Quality;
        this.ImIn = ImIn;
        this.OrigByteImage = OrigByteImage;
        this.MaxImageSmallDimension=MaxImageSmallDimension;
    }

    @Override
    /**
     * Returns an ImageDownloadResult object from where the BufferedImage object and the image identifier can be
     * obtained.
     */
    public GhostCalculationResult call() throws Exception {
        float[][] difference = calculateDifference();
        return new GhostCalculationResult(Quality, difference);
    }

    public float[][] calculateDifference() {

        int NewHeight, NewWidth;
        float ScaleFactor;

        int ImageHeight=ImIn.getHeight();
        int ImageWidth=ImIn.getWidth();
        if (ImageHeight>ImageWidth & ImageWidth> MaxImageSmallDimension*1.5) {
            NewWidth= MaxImageSmallDimension;
            ScaleFactor=((float)ImageWidth)/((float)NewWidth);
            NewHeight=Math.round(((float)ImageHeight)/ScaleFactor);
        } else if (ImageWidth>ImageHeight & ImageHeight> MaxImageSmallDimension*1.5)
        {
            NewHeight= MaxImageSmallDimension;
            ScaleFactor=((float)ImageHeight)/((float)NewHeight);
            NewWidth=Math.round(((float)ImageWidth)/ScaleFactor);
        } else {
            NewHeight=ImageHeight;
            NewWidth=ImageWidth;
            ScaleFactor=1;
        }


        BufferedImage RecompressedImage = Util.RecompressImage(ImIn, Quality);
        //int[][][] RecompressedByteImage = Util.GetRGBArray(RecompressedImage);
        //float[][][] ImageDifference = Util.CalculateImageDifference(OrigByteImage, RecompressedByteImage);
        float[][][] ImageDifference = Util.CalculateResizedImageDifference(ImIn, RecompressedImage, NewWidth, NewHeight);


        int FilterSize=Math.round(17 / ScaleFactor);  //17 acc. to the paper
        if (FilterSize<2){FilterSize=2;}
        float[][][] Smooth = Util.MeanFilterRGB(ImageDifference, FilterSize);
        float[][] GrayDifference = Util.MeanChannelImage(Smooth);
        float GhostMin = Util.MinDouble2DArray(GrayDifference);
        float GhostMax = Util.MaxDouble2DArray(GrayDifference);
        float GhostMean = Util.SingleChannelMean(GrayDifference);
        //JetImageDifference=Util.VisualizeWithJet(Util.NormalizeIm(MeanDifference));
        //JetImageDifference=(BufferedImage) JetImageDifference.getScaledInstance(NewWidth,NewHeight, Image.SCALE_FAST);

        if (NewHeight<ImageHeight) {
            GrayDifference = Util.ShrinkMap(GrayDifference, NewWidth, NewHeight);
        }

        return GrayDifference;
        //System.out.println(Quality);
    }


}
