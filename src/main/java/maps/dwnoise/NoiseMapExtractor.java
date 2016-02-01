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

    BufferedImage InputImage = null;

    public double[][] NoiseMap = null;
    public BufferedImage DisplaySurface = null;
    public double maxNoiseValue = -Double.MAX_VALUE;
    public double minNoiseValue = Double.MAX_VALUE;

    public NoiseMapExtractor(String FileName) throws IOException {
        InputImage = ImageIO.read(new File(FileName));
        GetNoiseMap();
    }

    public void GetNoiseMap() throws IOException {

        long Begin;

        BufferedImage img = InputImage;
        byte[] byteImage;

        int ImWidth, ImHeight;
        if (img.getWidth() % 2 == 0) {
            ImWidth = img.getWidth();
        } else {
            ImWidth = (img.getWidth() - 1);
        }
        if (img.getHeight() % 2 == 0) {
            ImHeight = img.getHeight();
        } else {
            ImHeight = (img.getHeight() - 1);
        }

        int ColumnFilterScale = (int) (Math.log(ImHeight) / Math.log(2)) - 1;
        int RowFilterScale = (int) (Math.log(ImWidth) / Math.log(2)) - 1;

        double[][] ImgYAsArray = new double[ImWidth][ImHeight];
        double[][] FilteredImgYAsArray = new double[ImWidth][ImHeight / 2];
        double[][] DoubleFilteredImgYAsArray = new double[ImWidth / 2][ImHeight / 2];
        double[] ImgColumn, ImgRow;
        Color tmpcolor;
        double R, G, B;

        for (int ii = 0; ii < ImWidth; ii++) {
            for (int jj = 0; jj < ImHeight; jj++) {
                tmpcolor = new Color(img.getRGB(ii, jj));
                R = tmpcolor.getRed();
                G = tmpcolor.getGreen();
                B = tmpcolor.getBlue();
                ImgYAsArray[ii][jj] = 0.2989 * R + 0.5870 * G + 0.1140 * B;
            }
        }

        double[] WaveletColumn;


        Begin = System.currentTimeMillis();

        for (int ii = 0; ii < ImWidth; ii++) {
            //System.out.println(ii);
            try {
                ImgColumn = new double[ImHeight];
                System.arraycopy(ImgYAsArray[ii], 0, ImgColumn, 0, ImHeight);
                WaveletColumn = DWT.transform(ImgColumn, Wavelet.Daubechies, 8, ColumnFilterScale, DWT.Direction.forward);

                System.arraycopy(WaveletColumn, ImHeight / 2, FilteredImgYAsArray[ii], 0, ImHeight / 2);
            } catch (Exception ex) {
                Logger.getLogger(NoiseMapExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        double[] WaveletRow;
        for (int jj = 0; jj < ImHeight / 2; jj++) {
            try {
                ImgRow = new double[ImWidth];
                for (int ii = 0; ii < ImWidth; ii++) {
                    ImgRow[ii] = FilteredImgYAsArray[ii][jj];
                }
                WaveletRow = DWT.transform(ImgRow, Wavelet.Daubechies, 8, RowFilterScale, DWT.Direction.forward);
                for (int ii = 0; ii < ImWidth / 2; ii++) {
                    DoubleFilteredImgYAsArray[ii][jj] = WaveletRow[ii + ImWidth / 2];
                }
            } catch (Exception ex) {
                Logger.getLogger(NoiseMapExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        int BlockSize = 8;
        double[][] BlockMap = Util.BlockNoiseVar(DoubleFilteredImgYAsArray, BlockSize);

        int MedianFilterSize = 7;
        if (MedianFilterSize>BlockMap.length) {
            MedianFilterSize = BlockMap.length-1;
        }
        if (MedianFilterSize>BlockMap[0].length) {
            MedianFilterSize = BlockMap[0].length-1;
        }
        if (MedianFilterSize<5) {
            minNoiseValue = 0;
            maxNoiseValue = 0;
            NoiseMap = new double[1][1];
            NoiseMap[0][0]=0;
            byte[][] ByteOutput = new byte[1][1];
            ByteOutput[0][0]=0;
            BufferedImage OutputImage = Util.createJetVisualization(ByteOutput);
            DisplaySurface = OutputImage;
            return;
        }


        double[][] OutBlockMap = Util.MedianFilter(BlockMap, MedianFilterSize);

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        double colMin, colMax;

        for (double[] OutBlockMap1 : OutBlockMap) {
            List b = Arrays.asList(ArrayUtils.toObject(OutBlockMap1));
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

        NoiseMap = OutBlockMap;

        double spread = max - min;

        byte[][] ByteOutput = new byte[OutBlockMap.length][OutBlockMap[0].length];

        for (int ii = 0; ii < OutBlockMap.length; ii++) {
            for (int jj = 0; jj < OutBlockMap[0].length; jj++) {
                ByteOutput[ii][jj] = (byte) Math.round(((OutBlockMap[ii][jj] - min) / spread) * 63);
            }
        }
        BufferedImage OutputImage = Util.createJetVisualization(ByteOutput);

        DisplaySurface = OutputImage;
    }
}
