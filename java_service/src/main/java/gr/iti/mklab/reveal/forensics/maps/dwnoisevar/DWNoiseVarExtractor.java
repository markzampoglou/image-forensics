/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iti.mklab.reveal.forensics.maps.dwnoisevar;

// JAI-ImageIO is necessary to ensure TIFF files can be read. The current JAR version is for windows
// Replace the current divide-by-max approach to a divide by 20 or sth
import gr.iti.mklab.reveal.forensics.util.Util;
import gr.iti.mklab.reveal.forensics.util.dwt.DWT;
import gr.iti.mklab.reveal.forensics.util.dwt.Wavelet;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author markzampoglou
 */
public class DWNoiseVarExtractor {

    int numThreads=6;

    BufferedImage inputImage = null;

    public float[][] noiseMap = null;
    public BufferedImage displaySurface = null;
    public double maxNoiseValue = -Double.MAX_VALUE;
    public double minNoiseValue = Double.MAX_VALUE;

    public DWNoiseVarExtractor(String FileName) throws IOException {
        inputImage = ImageIO.read(new File(FileName));
        getNoiseMap();
    }

    public void getNoiseMap() throws IOException {

        BufferedImage img = inputImage;
     
        int imWidth, imHeight;
        if (img.getWidth() % 2 == 0) {
            imWidth = img.getWidth();
        } else {
            imWidth = (img.getWidth() - 1);
        }
        if (img.getHeight() % 2 == 0) {
            imHeight = img.getHeight();
        } else {
            imHeight = (img.getHeight() - 1);
        }

        int columnFilterScale = (int) (Math.log(imHeight) / Math.log(2)) - 1;
        int rowFilterScale = (int) (Math.log(imWidth) / Math.log(2)) - 1;

        double[][] imgYAsArray = new double[imWidth][imHeight];
        double[][] filteredImgYAsArray = new double[imWidth][imHeight / 2];
        double[][] doubleFilteredImgYAsArray = new double[imWidth / 2][imHeight / 2];
        double[] imgColumn, imgRow;
        Color tmpcolor;
        double R, G, B;

        for (int ii = 0; ii < imWidth; ii++) {
            for (int jj = 0; jj < imHeight; jj++) {
                tmpcolor = new Color(img.getRGB(ii, jj));
                R = tmpcolor.getRed();
                G = tmpcolor.getGreen();
                B = tmpcolor.getBlue();
                imgYAsArray[ii][jj] = 0.2989 * R + 0.5870 * G + 0.1140 * B;
            }
        }

        double[] waveletColumn;        
        RealMatrix rm = new Array2DRowRealMatrix(imgYAsArray);        
        	for (int ii = 0; ii < imWidth; ii++) {       
                  try {                	
                      imgColumn = new double[imHeight];
                      imgColumn = rm.getRow(ii);
                      //Long startTime1 = System.currentTimeMillis();  
                      waveletColumn = DWT.transform(imgColumn, Wavelet.Daubechies, 8, columnFilterScale, DWT.Direction.forward);                 
                      System.arraycopy(waveletColumn, imHeight / 2, filteredImgYAsArray[ii], 0, imHeight / 2);                 
                  } catch (Exception ex) {
                      Logger.getLogger(DWNoiseVarExtractor.class.getName()).log(Level.SEVERE, null, ex);
                  }
              } 
        	
		  double[] waveletRow;  
	      RealMatrix rm2 = new Array2DRowRealMatrix(filteredImgYAsArray);              
	       for (int jj = 0; jj < imHeight / 2; jj++) {
	               try {
	                   imgRow = new double[imWidth];                        
	                   imgRow = rm2.getColumn(jj);
	                   waveletRow = DWT.transform(imgRow, Wavelet.Daubechies, 8, rowFilterScale, DWT.Direction.forward);
	                   for (int ii = 0; ii < imWidth / 2; ii++) {
	                       doubleFilteredImgYAsArray[ii][jj] = waveletRow[ii + imWidth / 2];
	                   }
	               } catch (Exception ex) {
	                   Logger.getLogger(DWNoiseVarExtractor.class.getName()).log(Level.SEVERE, null, ex);
	               }
	           }

        int blockSize = 8;
        double[][] blockNoiseVar = Util.blockNoiseVar(doubleFilteredImgYAsArray, blockSize);

        int medianFilterSize = 7;
        if (medianFilterSize>blockNoiseVar.length) {
            medianFilterSize = blockNoiseVar.length-1;
        }
        if (medianFilterSize>blockNoiseVar[0].length) {
            medianFilterSize = blockNoiseVar[0].length-1;
        }
        if (medianFilterSize<5) {
            minNoiseValue = 0;
            maxNoiseValue = 0;
            noiseMap = new float[1][1];
            noiseMap[0][0]=0;
            double[][] artificialOutput = new double[1][1];
            artificialOutput [0][0]=0;
            BufferedImage outputImage = Util.visualizeWithJet(artificialOutput);
            displaySurface = outputImage;
            return;
        }

        float[][] outBlockMap = Util.medianFilterSingleChannelImage(blockNoiseVar, medianFilterSize);

        minNoiseValue = Util.minDouble2DArray(outBlockMap);
        maxNoiseValue = Util.maxDouble2DArray(outBlockMap);
        noiseMap = outBlockMap;
        double[][] normalizedMap=Util.normalizeIm(outBlockMap);
        BufferedImage outputImage = Util.visualizeWithJet(normalizedMap);
        // output
        displaySurface = outputImage;
    }
}
