/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
//import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author markzampoglou
 */
public class Util {

    public static BufferedImage recompressImage(BufferedImage imageIn, int quality) {
        //Apply in-memory JPEG compression to a BufferedImage given a quality setting (0-100)
        //and return the resulting BufferedImage

        float fQuality = (float) (quality / 100.0);

        //File OutputFile = new File("TestOutput.jpg");
        BufferedImage outputImage = null;
        try {
            ImageWriter writer;
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
            writer = iter.next();
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(fQuality);

            byte[] imageInByte;
            try ( //ImageOutputStream ios = ImageIO.createImageOutputStream(OutputFile);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(baos);
                //writer.setOutput(ios);
                writer.setOutput(mcios);
                IIOImage tmpImage = new IIOImage(imageIn, null, null);
                writer.write(null, tmpImage, iwp);
                writer.dispose();
                baos.flush();
                imageInByte = baos.toByteArray();
            }
            InputStream in = new ByteArrayInputStream(imageInByte);
            outputImage = ImageIO.read(in);
        } catch (Exception ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return outputImage;
    }

    //possible 10-fold speed increase in:
    //http://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image
    //(but ensure all major bases are covered)
    public static int[][][] getRGBArray(BufferedImage imageIn) {

        int ImW = imageIn.getWidth();
        int ImH = imageIn.getHeight();
        Color tmpColor;
        int[][][] rgbValues = new int[3][ImW][ImH];

        for (int ii = 0; ii < ImW; ii++) {
            for (int jj = 0; jj < ImH; jj++) {
                tmpColor = new Color(imageIn.getRGB(ii, jj));
                rgbValues[0][ii][jj] = tmpColor.getRed();
                rgbValues[1][ii][jj] = tmpColor.getGreen();
                rgbValues[2][ii][jj] = tmpColor.getBlue();
            }
        }
        return rgbValues;
    }

    public static BufferedImage createImFromArray(int[][][] rgbvalues) {

        int ImW = rgbvalues[0].length;
        int ImH = rgbvalues[0][0].length;
        BufferedImage ImageOut = new BufferedImage(ImW, ImH, 5); //5 for PNG;

        Color tmpColor;

        for (int ii = 0; ii < ImW; ii++) {
            for (int jj = 0; jj < ImH; jj++) {
                tmpColor = new Color((int) Math.round(rgbvalues[0][ii][jj]), (int) Math.round(rgbvalues[1][ii][jj]), (int) Math.round(rgbvalues[2][ii][jj]));
                ImageOut.setRGB(ii, jj, tmpColor.getRGB());
            }
        }
        return ImageOut;
    }

    public static float[][][] calculateImageDifference(int[][][] Image1, int[][][] Image2) {
        int Dim1 = Image1.length;
        int Dim2 = Image1[0].length;
        int Dim3 = Image1[0][0].length;
        float[][][] ImageDifference = new float[Dim1][Dim2][Dim3];
        for (int ii = 0; ii < Dim1; ii++) {
            for (int jj = 0; jj < Dim2; jj++) {
                for (int kk = 0; kk < Dim3; kk++) {
                    ImageDifference[ii][jj][kk] = (float) ((Image1[ii][jj][kk] - Image2[ii][jj][kk]) * (Image1[ii][jj][kk] - Image2[ii][jj][kk]));
                }
            }
        }
        return ImageDifference;
    }

    public static float[][][] calculateResizedImageDifference(BufferedImage image1, BufferedImage image2, int newWidth, int newHeight){
        float[][][] outputMap=new float[3][newWidth][newHeight];
        float widthModifier=(float)image1.getWidth()/newWidth;
        float heightModifier=(float)image1.getHeight()/newHeight;

        Color tmpColor1, tmpColor2;

        for (int ii = 0; ii < newHeight; ii++) {
            for (int jj = 0; jj < newWidth; jj++) {
                try {
                    tmpColor1 = new Color(image1.getRGB(Math.round(jj * widthModifier),Math.round(ii * heightModifier)));
                    tmpColor2 = new Color(image2.getRGB(Math.round(jj * widthModifier),Math.round(ii * heightModifier)));
                    outputMap[0][jj][ii] = (float) (tmpColor1.getRed() - tmpColor2.getRed()) * (tmpColor1.getRed() - tmpColor2.getRed());
                    outputMap[1][jj][ii] = (float) (tmpColor1.getGreen() - tmpColor2.getGreen()) * (tmpColor1.getGreen() - tmpColor2.getGreen());
                    outputMap[2][jj][ii] = (float) (tmpColor1.getBlue() - tmpColor2.getBlue()) * (tmpColor1.getBlue() - tmpColor2.getBlue());
                } catch(Exception e) {
                    System.out.println(newHeight + " " + newWidth + " " + image1.getHeight() + " " + image1.getWidth() + " " + ii + " " + jj + " " + Math.round(ii * heightModifier) + " " + Math.round(jj * widthModifier) + " " + heightModifier + " " + widthModifier);
                    e.printStackTrace();
                    return outputMap;
                }
            }
        }
        return outputMap;

        //System.out.println(heightModifier);
        //System.out.println(widthModifier);

    }

    public static float[][] meanFilterSingleChan(float[][] imIn, int meanFilterSize) {
        // Mean filter a 2D double array
        // meanFilterSize should be odd
        // Careful: this is the mean of the ABS values!
        int offset = (meanFilterSize - 1) / 2;
        int filterElements = meanFilterSize * meanFilterSize;
        int imWidth = imIn.length;
        int imHeight = imIn[0].length;
        DescriptiveStatistics blockValues;

        float[][] filteredImage = new float[imWidth - 2 * offset][imHeight - 2 * offset];

        float sum;
        for (int ii = offset; ii <= imWidth - meanFilterSize + offset; ii = ii + 1) {
            sum = 0;
            for (int B_ii = ii - offset; B_ii < ii + offset + 1; B_ii++) {
                for (int B_jj = 0; B_jj < 2 * offset + 1; B_jj++) {
                    sum = sum + imIn[B_ii][B_jj];
                }
            }
            filteredImage[ii - offset][0] = sum / filterElements;

            for (int jj = offset + 1; jj <= imHeight - meanFilterSize + offset; jj = jj + 1) {
                for (int B_ii = ii - offset; B_ii < ii + offset + 1; B_ii++) {
                    sum = sum - imIn[B_ii][jj - offset - 1];
                    sum = sum + imIn[B_ii][jj + offset];
                }
                filteredImage[ii - offset][jj - offset] = sum / filterElements;
                /*                blockValues = new DescriptiveStatistics();
                 for (int B_ii = ii - offset; B_ii < ii + offset + 1; B_ii++) {
                 for (int B_jj = jj - offset; B_jj < jj + offset + 1; B_jj++) {
                 blockValues.addValue(Math.abs(imIn[B_ii][B_jj]));
                 }
                 }
                 filteredImage[ii - offset][jj - offset] = blockValues.getMean();
                 */
            }
        }
        return filteredImage;
    }

    public static int[][] sumFilterSingleChanVert(int[][] imIn, int filterSize) {
        // Mean filter a 2D double array
        // filterSize should be odd
        // Careful: this is the mean of the ABS values!
        int offset = (filterSize - 1) / 2;
        int imWidth = imIn.length;
        int imHeight = imIn[0].length;

        int[][] filteredImage = new int[imWidth][imHeight - 2 * offset];

        int sum;
        for (int ii = 0; ii < imWidth ; ii++) {
            sum = 0;
            for (int B_jj = 0; B_jj < 2 * offset + 1; B_jj++) {
                    sum = sum + imIn[ii][B_jj];
            }
            filteredImage[ii][0] = sum;

            for (int jj = offset + 1; jj <= imHeight - filterSize + offset; jj = jj + 1) {
                sum = sum - imIn[ii][jj - offset - 1];
                sum = sum + imIn[ii][jj + offset];
                filteredImage[ii][jj - offset] = sum;
            }
        }
        return filteredImage;
    }

    public static int[][] sumFilterSingleChanHorz(int[][] imIn, int filterSize) {
        // Mean filter a 2D double array
        // filterSize should be odd
        // Careful: this is the mean of the ABS values!
        int offset = (filterSize - 1) / 2;
        int imWidth = imIn.length;
        int imHeight = imIn[0].length;

        int[][] filteredImage = new int[imWidth-2*offset][imHeight];

        int Sum;
        for (int jj = 0; jj < imHeight ; jj++) {
            Sum = 0;
            for (int B_ii = 0; B_ii < 2 * offset + 1; B_ii++) {
                Sum = Sum + imIn[B_ii][jj];
            }
            filteredImage[0][jj] = Sum;

            for (int ii = offset + 1; ii <= imWidth - filterSize + offset; ii = ii + 1) {
                Sum = Sum - imIn[ii - offset - 1][jj];
                Sum = Sum + imIn[ii + offset][jj];
                filteredImage[ii - offset][jj] = Sum;
            }
        }
        return filteredImage;
    }

    public static int[][] medianFilterSingleChanVert(int[][] imIn, int filterSize) {
        // Mean filter a 2D double array
        // filterSize should be odd
        // Careful: this is the mean of the ABS values!
        int offset = (filterSize - 1) / 2;
        int imWidth = imIn.length;
        int imHeight = imIn[0].length;

        int[][] filteredImage = new int[imWidth][imHeight - 2 * offset];

        ArrayList<Integer> neighborhood;
        ArrayList<Integer> sortedNeighborhood;


        for (int ii = 0; ii < imWidth; ii++) {
            neighborhood = new ArrayList<>();
            for (int N_jj=0; N_jj<2*offset+1;N_jj++) {
                neighborhood.add(imIn[ii][N_jj]);
            }
            sortedNeighborhood=new ArrayList<>(neighborhood);
            Collections.sort(sortedNeighborhood);
            filteredImage[ii][0] = sortedNeighborhood.get(offset + 1);

            for (int jj = offset+1; jj <= imHeight - filterSize+offset; jj++) {
                neighborhood.remove(0);
                neighborhood.add(imIn[ii][jj + offset]);
                sortedNeighborhood=new ArrayList<>(neighborhood);
                Collections.sort(sortedNeighborhood);
                filteredImage[ii][jj-offset] = sortedNeighborhood.get(offset+1);
            }
        }
        return filteredImage;
    }

    public static int[][] medianFilterSingleChanHorz(int[][] imIn, int FilterSize) {
        // Mean filter a 2D double array
        // FilterSize should be odd
        // Careful: this is the mean of the ABS values!
        int offset = (FilterSize - 1) / 2;
        int imWidth = imIn.length;
        int imHeight = imIn[0].length;

        int[][] filteredImage = new int[imWidth - 2 * offset][imHeight];

        ArrayList<Integer> neighborhood;
        ArrayList<Integer> sortedNeighborhood;


        for (int jj = 0; jj < imHeight; jj++) {
            neighborhood = new ArrayList<>();
            for (int N_ii=0; N_ii<2*offset+1;N_ii++) {
                neighborhood.add(imIn[N_ii][jj]);
            }
            sortedNeighborhood=new ArrayList<>(neighborhood);
            Collections.sort(sortedNeighborhood);
            filteredImage[0][jj] = sortedNeighborhood.get(offset + 1);

            for (int ii = offset+1; ii <= imWidth - FilterSize+offset; ii++) {
                neighborhood.remove(0);
                neighborhood.add(imIn[ii + offset][jj]);
                sortedNeighborhood=new ArrayList<>(neighborhood);
                Collections.sort(sortedNeighborhood);
                filteredImage[ii-offset][jj] = sortedNeighborhood.get(offset+1);
            }
        }
        return filteredImage;
    }


    public static float[][][] meanFilterRGB(float[][][] ImIn, int meanFilterSize) {
        int offset = (meanFilterSize - 1) / 2;
        float[][][] filteredIm = new float[3][ImIn[0].length - 2 * offset][ImIn[0][0].length - 2 * offset];

        filteredIm[0] = meanFilterSingleChan(ImIn[0], meanFilterSize);
        filteredIm[1] = meanFilterSingleChan(ImIn[1], meanFilterSize);
        filteredIm[2] = meanFilterSingleChan(ImIn[2], meanFilterSize);

        return filteredIm;
    }

    public static float[][] meanChannelImage(float[][][] imIn) {
        int imWidth = imIn[0].length;
        int imHeight = imIn[0][0].length;
        float[][] outIm = new float[imWidth][imHeight];

        for (int ii = 0; ii < imWidth; ii++) {
            for (int jj = 0; jj < imHeight; jj++) {
                outIm[ii][jj] = (imIn[0][ii][jj] + imIn[1][ii][jj] + imIn[2][ii][jj]) / 3;
            }
        }

        return outIm;
    }

    public static float SingleChannelMean(float[][] imIn) {
        int imWidth = imIn.length;
        int imHeight = imIn[0].length;
        float mean = 0;
        /*
         for (int kkkk = imHeight-5; kkkk < imHeight; kkkk++) {
         for (int llll = imWidth-5; llll < imWidth; llll++) {
         System.out.print(imIn[llll][kkkk] + " ");
         }
         System.out.println();
         }
         System.out.println("----");
         */
        for (int ii = 0; ii < imWidth; ii++) {
            for (int jj = 0; jj < imHeight; jj++) {
                mean = mean + imIn[ii][jj];
            }
        }
        mean = mean / (imWidth * imHeight);

        return mean;
    }

    public static List<Integer> getArrayLocalMinima(float[] valuesIn) {
        List<Integer> minima = new ArrayList();

        for (int ii = 1; ii < valuesIn.length - 1; ii++) {
            //System.out.println(valuesIn[ii - 1]);
            if ((valuesIn[ii - 1] > valuesIn[ii]) & (valuesIn[ii + 1] > valuesIn[ii])) {
                //System.out.println(ii);
                minima.add(ii);
            }
        }
        //System.out.println(valuesIn[valuesIn.length - 2]);
        //System.out.println(valuesIn[valuesIn.length - 1]);
        return minima;
    }

    public static double[][] normalizeIm(float[][] imIn) {
        int imWidth = imIn.length;
        int imHeight = imIn[0].length;
        double imOut[][] = new double[imWidth][imHeight];

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        double colMin, colMax;

        for (float[] imInRow : imIn) {
            List b = Arrays.asList(ArrayUtils.toObject(imInRow));
            colMin = (float) Collections.min(b);
            if (colMin < min) {
                min = colMin;
            }
            colMax = (float) Collections.max(b);
            if (colMax > max) {
                max = colMax;
            }
        }
        double spread = max - min;

        for (int ii = 0; ii < imWidth; ii++) {
            for (int jj = 0; jj < imHeight; jj++) {
                imOut[ii][jj] = (imIn[ii][jj] - min) / spread;
            }
        }
        return imOut;
    }

    public static BufferedImage visualizeWithJet(double[][] ImIn) {
        int imWidth = ImIn.length;
        int imHeight = ImIn[0].length;
        BufferedImage outIm = new BufferedImage(imWidth, imHeight, 5);
        Color rgb;

        double[][] map = JetMap.colorMap;

        byte bytevalue;

        for (int ii = 0; ii < imWidth; ii++) {
            for (int jj = 0; jj < imHeight; jj++) {
                bytevalue = (byte) Math.round(ImIn[ii][jj] * 63);
                rgb = new Color((float) map[bytevalue][0], (float) map[bytevalue][1], (float) map[(byte) Math.round(ImIn[ii][jj]) * 63][2]);
                outIm.setRGB(ii, jj, rgb.getRGB());
            }
        }
        return outIm;
    }

    public static int rem(int x, int y) {
        int out;
        if (y != 0) {
            int n = (int) Math.floor(x / y);
            out = x - n * y;
        } else {
            out = 0;
        }
        if (x * out < 0) {
            out = out * -1;
        }
        return out;
    }

    public static double[][] medianFilter(double[][] imIn, int medianFilterSize) {
        // Median filter a 2D double array
        // medianFilterSize should be odd
        // Careful: this is the mean of the ABS values!
        int offset = (medianFilterSize - 1) / 2;
        int imWidth = imIn.length;
        int imHeight = imIn[0].length;
        DescriptiveStatistics blockValues;

        double[][] filteredImage = new double[imWidth - 2 * offset][imHeight - 2 * offset];

        for (int ii = offset; ii <= imWidth - medianFilterSize + offset; ii = ii + 1) {
            for (int jj = offset; jj <= imHeight - medianFilterSize + offset; jj = jj + 1) {
                blockValues = new DescriptiveStatistics();
                for (int B_ii = ii - offset; B_ii < ii + offset + 1; B_ii++) {
                    for (int B_jj = jj - offset; B_jj < jj + offset + 1; B_jj++) {
                        blockValues.addValue(Math.abs(imIn[B_ii][B_jj]));
                    }
                }
                filteredImage[ii - offset][jj - offset] = blockValues.getPercentile(50);
            }
        }
        return filteredImage;
    }

    public static BufferedImage createJetVisualization(byte[][] inputGrayImage) {
        double[][] map = JetMap.colorMap;
        BufferedImage outImage = new BufferedImage(inputGrayImage.length, inputGrayImage[0].length, 5); //5 for PNG;
        Color rgb;
        //System.out.println(inputGrayImage.length);
        //System.out.println(inputGrayImage[0].length);

        for (int ii = 0; ii < inputGrayImage.length; ii++) {
            for (int jj = 0; jj < inputGrayImage[ii].length; jj++) {
                rgb = new Color((float) map[inputGrayImage[ii][jj]][0], (float) map[inputGrayImage[ii][jj]][1], (float) map[inputGrayImage[ii][jj]][2]);
                outImage.setRGB(ii, jj, rgb.getRGB());
            }
        }
        return outImage;
    }

    public static double[][] blockNoiseVar(double[][] inputIm, int blockSize) {
        int blockedWidth = (int) Math.floor(inputIm.length / blockSize) * blockSize;
        int blockedHeight = (int) Math.floor(inputIm[0].length / blockSize) * blockSize;
        double[][] blockedIm = new double[blockedWidth / blockSize][blockedHeight / blockSize];

        DescriptiveStatistics blockValues;
        for (int ii = 0; ii < blockedWidth; ii = ii + blockSize) {
            for (int jj = 0; jj < blockedHeight; jj = jj + blockSize) {
                blockValues = new DescriptiveStatistics();
                for (int B_ii = ii; B_ii < ii + blockSize; B_ii++) {
                    for (int B_jj = jj; B_jj < jj + blockSize; B_jj++) {
                        blockValues.addValue(Math.abs(inputIm[B_ii][B_jj]));
                    }
                }
                blockedIm[ii / blockSize][jj / blockSize] = Math.sqrt(blockValues.getPercentile(50) / 0.6745);
            }
        }
        return blockedIm;
    }

    public static double[][] blockVar(double[][] inputIm, int blockSize) {
        int blockedWidth = (int) Math.floor(inputIm.length / blockSize) * blockSize;
        int blockedHeight = (int) Math.floor(inputIm[0].length / blockSize) * blockSize;
        double[][] blockedIm = new double[blockedWidth / blockSize][blockedHeight / blockSize];

        DescriptiveStatistics blockValues;
        for (int ii = 0; ii < blockedWidth; ii = ii + blockSize) {
            for (int jj = 0; jj < blockedHeight; jj = jj + blockSize) {
                blockValues = new DescriptiveStatistics();
                for (int B_ii = ii; B_ii < ii + blockSize; B_ii++) {
                    for (int B_jj = jj; B_jj < jj + blockSize; B_jj++) {
                        blockValues.addValue(Math.abs(inputIm[B_ii][B_jj]));
                    }
                }
                blockedIm[ii / blockSize][jj / blockSize] = blockValues.getPopulationVariance();
                //System.out.print(blockedIm[ii / blockSize][jj / blockSize] + " ");
            }
            //System.out.println();
        }
        return blockedIm;
    }

    public static String getImageFormat(File inputImage) throws IOException {
        String format = null;
        ImageInputStream iis = ImageIO.createImageInputStream(inputImage);
        Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
        if (!iter.hasNext()) {
            throw new RuntimeException("No readers found! I don't think this is an image file");
        }
        ImageReader reader = iter.next();
        format = reader.getFormatName();

        return format;
    }

    public static float minDouble2DArray(float[][] arrayIn) {
        float min = Float.MAX_VALUE;
        float colMin;

        for (float[] arrayInRow : arrayIn) {
            List b = Arrays.asList(ArrayUtils.toObject(arrayInRow));
            colMin = (float) Collections.min(b);
            if (colMin < min) {
                min = colMin;
            }
        }
        return min;
    }

    public static float maxDouble2DArray(float[][] arrayIn) {
        float max = -Float.MAX_VALUE;
        float colMax;
        for (float[] arrayInRow : arrayIn) {
            List b = Arrays.asList(ArrayUtils.toObject(arrayInRow));
            colMax = (float) Collections.max(b);
            if (colMax > max) {
                max = colMax;
            }
        }
        return max;
    }

    public static float minDouble3DArray(float[][][] arrayIn) {
        float min = Float.MAX_VALUE;
        float colMin;

        for (float[][] twoDInRow : arrayIn) {
            for (float[] arrayInRow : twoDInRow) {
                List b = Arrays.asList(ArrayUtils.toObject(arrayInRow));
                colMin = (float) Collections.min(b);
                if (colMin < min) {
                    min = colMin;
                }
            }
        }
        return min;
    }

    public static double maxDouble3DArray(float[][][] arrayIn) {
        double max = -Double.MAX_VALUE;
        double colMax;

        for (float[][] twoDInRow : arrayIn) {
            for (float[] arrayInRow : twoDInRow) {
                List b = Arrays.asList(ArrayUtils.toObject(arrayInRow));
                colMax = (float) Collections.max(b);
                if (colMax > max) {
                    max = colMax;
                }
            }
        }
        return max;
    }

    public static BufferedImage addTextToImage(BufferedImage imIn, String text, int manualImageHeight){
        
        //use manualImageHeight=0 to automatically detect height - set an manualImageHeight value to insert multiple lines of equal height.

        int imageHeight;
        if (manualImageHeight!=0) {
            imageHeight=manualImageHeight;
        } else {
            imageHeight=imIn.getHeight();
        }
        
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        Font font = new Font("Arial", Font.PLAIN, (int) Math.round(imageHeight*0.1));
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        //int width = fm.stringWidth(text);
        int height = fm.getHeight();

        g2d.dispose();
        
        
        BufferedImage outIm=new BufferedImage( imIn.getWidth(), imIn.getHeight()+height, BufferedImage.TYPE_INT_RGB);;
        for (int ii=0; ii<imIn.getWidth();ii++){
        for (int jj=0; jj<imIn.getHeight();jj++){
            outIm.setRGB(ii, jj, imIn.getRGB(ii, jj));
        }
        }

        /*
           Because font metrics is based on a graphics context, we need to create
           a small, temporary image so we can ascertain the width and height
           of the final image
         */

        g2d = outIm.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, 5, outIm.getHeight()-Math.round(height*0.2));
        g2d.dispose();
        
        return outIm;
    }

    public static List <BufferedImage> addJPEGQualitiesToList(List<BufferedImage> imIn, List<Integer> qualities, List<Double> minValues, List<Double> maxValues, List<Integer> localMinGhostQualities){
    List <BufferedImage> outList = new ArrayList();
         BufferedImage tmpImage;
         
        int ImageHeight=imIn.get(0).getHeight();
        if (ImageHeight>imIn.get(0).getWidth()) {
            ImageHeight=(int)Math.round(imIn.get(0).getWidth());
        }
        
         
        for (int ii=0;ii<imIn.size();ii++){
            tmpImage= addTextToImage(imIn.get(ii), "JPEG Quality: " + String.valueOf(qualities.get(ii)), ImageHeight);
            tmpImage= addTextToImage(tmpImage, "Min Value: " + String.format("%.2f", minValues.get(ii)), ImageHeight);
            tmpImage= addTextToImage(tmpImage, "Max Value: " + String.format("%.2f", maxValues.get(ii)), ImageHeight);
            outList.add(tmpImage);
            if (localMinGhostQualities.contains(qualities.get(ii)) | qualities.get(ii)==100){
                for (int jj=0;jj<4;jj++){
                    outList.add(tmpImage);
                }
            }
    }
    
    return outList;
    }

    public static float[][] shrinkMap(float[][] origMap, int newWidth, int newHeight){
        // shrink an input map using nearest neighbour interpolation

        float[][] outputMap=new float[newWidth][newHeight];
        float widthModifier=(float)origMap.length/newWidth;
        float heightModifier=(float)origMap[0].length/newHeight;
        //System.out.println(heightModifier);
        //System.out.println(widthModifier);

        for (int ii=0;ii<newWidth;ii++) {
            for (int jj=0;jj<newHeight;jj++) {
                try {
                    outputMap[ii][jj] = origMap[Math.round(ii * widthModifier)][Math.round(jj * heightModifier)];
                }catch (Exception e) {
                    System.out.println(outputMap.length + " " + outputMap[0].length + " " + origMap.length + " " + origMap[0].length + " " + ii + " " + jj + " " + Math.round(ii * heightModifier) + " " + Math.round(jj * widthModifier) + " " + heightModifier + " " + widthModifier);
                }
            }
        }
        return outputMap;
    }

    public static int[][] mirrorPadImage(int[][] imOrig, int padWidth, int padHeight){

        int[][] paddedY = new int[imOrig.length+2*padWidth][imOrig[0].length];
        for (int ii=padWidth;ii<imOrig.length+padWidth;ii++){
            for (int jj=0;jj<paddedY[0].length;jj++){
                paddedY[ii][jj]=imOrig[ii-padWidth][jj];
            }
        }
        //mirror
        for (int ii=0;ii<padWidth;ii++){
            for (int jj=0;jj<paddedY[0].length;jj++){
                paddedY[ii][jj]=paddedY[2*padWidth-ii][jj];
                paddedY[paddedY.length-ii-1][jj]=paddedY[paddedY.length-2*padWidth+ii-1][jj];
            }
        }

        int[][] padded= new int[imOrig.length+2*padWidth][imOrig[0].length+2*padHeight];

        for (int ii=0;ii<padded.length;ii++){
            for (int jj=padHeight;jj<paddedY[0].length+padHeight;jj++){
                padded[ii][jj]=paddedY[ii][jj-padHeight];
            }
        }
        //mirror
        for (int ii=0;ii<paddedY.length;ii++){
            for (int jj=0;jj<padHeight;jj++){
                padded[ii][jj]=padded[ii][2*padHeight-jj];
                padded[ii][padded[0].length-jj-1]=padded[ii][padded[0].length-2*padHeight+jj-1];
            }
        }

        return padded;
    }

    public static int[][] subtractImage(int[][] Im1, int[][] Im2){

        int[][] imOut = new int[Im1.length][Im1[0].length];

        for (int ii=0;ii<Im1.length;ii++){
            for (int jj=0;jj<Im1[0].length;jj++){
                imOut[ii][jj]=Im1[ii][jj]-Im2[ii][jj];
            }
        }
        return imOut;
    }

    public static int[][] addImage(int[][] Im1, int[][] Im2){

        int[][] imOut = new int[Im1.length][Im1[0].length];
        for (int ii=0;ii<Im1.length;ii++){
            for (int jj=0;jj<Im1[0].length;jj++){
                imOut[ii][jj]=Im1[ii][jj]+Im2[ii][jj];
            }
        }
        return imOut;
    }


}
