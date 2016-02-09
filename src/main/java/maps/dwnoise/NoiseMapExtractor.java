/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maps.dwnoise;

// JAI-ImageIO is necessary to ensure TIFF files can be read. The current JAR version is for windows
// Replace the current divide-by-max approach to a divide by 20 or sth
import util.Util;
import util.dwt.DWT;
import util.dwt.Wavelet;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author markzampoglou
 */
public class NoiseMapExtractor {

    int numThreads=6;

    BufferedImage inputImage = null;

    public double[][] noiseMap = null;
    public BufferedImage displaySurface = null;
    public double maxNoiseValue = -Double.MAX_VALUE;
    public double minNoiseValue = Double.MAX_VALUE;

    public NoiseMapExtractor(String FileName) throws IOException {
        inputImage = ImageIO.read(new File(FileName));
        getNoiseMap();
    }

    public void getNoiseMap() throws IOException {

        long begin;

        BufferedImage img = inputImage;
        byte[] byteImage;

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


        begin = System.currentTimeMillis();

        for (int ii = 0; ii < imWidth; ii++) {
            //System.out.println(ii);
            try {
                imgColumn = new double[imHeight];
                System.arraycopy(imgYAsArray[ii], 0, imgColumn, 0, imHeight);
                waveletColumn = DWT.transform(imgColumn, Wavelet.Daubechies, 8, columnFilterScale, DWT.Direction.forward);

                System.arraycopy(waveletColumn, imHeight / 2, filteredImgYAsArray[ii], 0, imHeight / 2);
            } catch (Exception ex) {
                Logger.getLogger(NoiseMapExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        double[] waveletRow;
        for (int jj = 0; jj < imHeight / 2; jj++) {
            try {
                imgRow = new double[imWidth];
                for (int ii = 0; ii < imWidth; ii++) {
                    imgRow[ii] = filteredImgYAsArray[ii][jj];
                }
                waveletRow = DWT.transform(imgRow, Wavelet.Daubechies, 8, rowFilterScale, DWT.Direction.forward);
                for (int ii = 0; ii < imWidth / 2; ii++) {
                    doubleFilteredImgYAsArray[ii][jj] = waveletRow[ii + imWidth / 2];
                }
            } catch (Exception ex) {
                Logger.getLogger(NoiseMapExtractor.class.getName()).log(Level.SEVERE, null, ex);
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
            noiseMap = new double[1][1];
            noiseMap[0][0]=0;
            byte[][] byteOutput = new byte[1][1];
            byteOutput[0][0]=0;
            BufferedImage outputImage = Util.createJetVisualization(byteOutput);
            displaySurface = outputImage;
            return;
        }


        double[][] outBlockMap = Util.medianFilter(blockNoiseVar, medianFilterSize);

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        double colMin, colMax;

        for (double[] outBlockMap1 : outBlockMap) {
            List b = Arrays.asList(ArrayUtils.toObject(outBlockMap1));
            colMin = (double) Collections.min(b);
            if (colMin < min) {
                min = colMin;
            }
            colMax = (double) Collections.max(b);
            if (colMax > max) {
                max = colMax;
            }
        }
        minNoiseValue = min;
        maxNoiseValue = max;

        noiseMap = outBlockMap;

        double spread = max - min;

        byte[][] byteOutput = new byte[outBlockMap.length][outBlockMap[0].length];

        for (int ii = 0; ii < outBlockMap.length; ii++) {
            for (int jj = 0; jj < outBlockMap[0].length; jj++) {
                byteOutput[ii][jj] = (byte) Math.round(((outBlockMap[ii][jj] - min) / spread) * 63);
            }
        }
        BufferedImage outputImage = Util.createJetVisualization(byteOutput);

        displaySurface = outputImage;
    }
}
