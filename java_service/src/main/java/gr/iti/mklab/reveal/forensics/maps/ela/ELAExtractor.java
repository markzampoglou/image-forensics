/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iti.mklab.reveal.forensics.maps.ela;

import gr.iti.mklab.reveal.forensics.util.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author markzampoglou
 */
public class ELAExtractor {

    public BufferedImage displaySurface = null;
    public double elaMin;
    public double elaMax;

    public ELAExtractor(String fileName) throws IOException {
        getJPEGELA(fileName);
    }

    private BufferedImage getJPEGELA(String fileName) throws IOException {

        int quality=75;
        int displayMultiplier=20;

        BufferedImage origImage;
        origImage = ImageIO.read(new File(fileName));

        BufferedImage recompressedImage = Util.recompressImage(origImage, quality);
        float[][][] imageDifference = Util.getImageDifference(origImage, recompressedImage);
        elaMin =Util.minDouble3DArray(imageDifference);
        elaMax =Util.maxDouble3DArray(imageDifference);

        int[][][] intDifference = new int[imageDifference.length][imageDifference[0].length][imageDifference[0][0].length];


        for (int ii=0;ii<imageDifference.length;ii++){
            for (int jj=0;jj<imageDifference[0].length;jj++){
                for (int kk=0;kk<imageDifference[0][0].length;kk++){
                    intDifference[ii][jj][kk]=(int) Math.sqrt(imageDifference[ii][jj][kk])*displayMultiplier;
                    if (intDifference[ii][jj][kk]>255){
                        intDifference[ii][jj][kk]=255;
                    }
                }
            }
        }



        displaySurface =Util.getBufferedIm(intDifference);

        return origImage;
    }
}
