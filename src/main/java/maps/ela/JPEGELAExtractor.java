/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maps.ela;

import util.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author markzampoglou
 */
public class JPEGELAExtractor {

    public BufferedImage DisplaySurface = null;
    public double ELAMin;
    public double ELAMax;

    public JPEGELAExtractor(String FileName) throws IOException {
        GetJPEGELA(FileName);
    }

    private BufferedImage GetJPEGELA(String FileName) throws IOException {

        int Quality=75;
        int DisplayMultiplier=20;

        BufferedImage OrigImage;
        OrigImage = ImageIO.read(new File(FileName));
        int[][][] OrigByteImage = Util.GetRGBArray(OrigImage);

        BufferedImage RecompressedImage = Util.RecompressImage(OrigImage, Quality);
        int[][][] RecompressedByteImage = Util.GetRGBArray(RecompressedImage);
        float[][][] ImageDifference = Util.CalculateImageDifference(OrigByteImage, RecompressedByteImage);
        ELAMin=Util.MinDouble3DArray(ImageDifference);
        ELAMax=Util.MaxDouble3DArray(ImageDifference);

        int[][][] IntDifference = new int[ImageDifference.length][ImageDifference[0].length][ImageDifference[0][0].length];


        for (int ii=0;ii<ImageDifference.length;ii++){
            for (int jj=0;jj<ImageDifference[0].length;jj++){
                for (int kk=0;kk<ImageDifference[0][0].length;kk++){
                    IntDifference[ii][jj][kk]=(int) Math.sqrt(ImageDifference[ii][jj][kk])*DisplayMultiplier;
                    if (IntDifference[ii][jj][kk]>255){
                        IntDifference[ii][jj][kk]=255;
                    }
                }
            }
        }



        DisplaySurface =Util.CreateImFromArray(IntDifference);

        return OrigImage;
    }
}
