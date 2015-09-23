/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

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

    public static BufferedImage RecompressImage(BufferedImage ImageIn, int Quality) {
        //Apply in-memory JPEG compression to a BufferedImage given a Quality setting (0-100)
        //and return the resulting BufferedImage

        float FQuality = (float) (Quality / 100.0);

        //File OutputFile = new File("TestOutput.jpg");
        BufferedImage OutputImage = null;
        try {
            ImageWriter writer;
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
            writer = iter.next();
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(FQuality);

            byte[] imageInByte;
            try ( //ImageOutputStream ios = ImageIO.createImageOutputStream(OutputFile);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(baos);
                //writer.setOutput(ios);
                writer.setOutput(mcios);
                IIOImage tmptmpImage = new IIOImage(ImageIn, null, null);
                writer.write(null, tmptmpImage, iwp);
                writer.dispose();
                baos.flush();
                imageInByte = baos.toByteArray();
            }
            InputStream in = new ByteArrayInputStream(imageInByte);
            OutputImage = ImageIO.read(in);
        } catch (Exception ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return OutputImage;
    }

    //possible 10-fold speed increase in:
    //http://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image
    //(but ensure all major bases are covered)
    public static int[][][] GetRGBArray(BufferedImage ImageIn) {

        int ImW = ImageIn.getWidth();
        int ImH = ImageIn.getHeight();
        Color tmpColor;
        int[][][] RGBValues = new int[3][ImW][ImH];

        for (int ii = 0; ii < ImW; ii++) {
            for (int jj = 0; jj < ImH; jj++) {
                tmpColor = new Color(ImageIn.getRGB(ii, jj));
                RGBValues[0][ii][jj] = tmpColor.getRed();
                RGBValues[1][ii][jj] = tmpColor.getGreen();
                RGBValues[2][ii][jj] = tmpColor.getBlue();
            }
        }
        return RGBValues;
    }

    public static BufferedImage CreateImFromArray(int[][][] RGBValues) {

        int ImW = RGBValues[0].length;
        int ImH = RGBValues[0][0].length;
        BufferedImage ImageOut = new BufferedImage(ImW, ImH, 5); //5 for PNG;

        Color tmpColor;

        for (int ii = 0; ii < ImW; ii++) {
            for (int jj = 0; jj < ImH; jj++) {
                tmpColor = new Color((int) Math.round(RGBValues[0][ii][jj]), (int) Math.round(RGBValues[1][ii][jj]), (int) Math.round(RGBValues[2][ii][jj]));
                ImageOut.setRGB(ii, jj, tmpColor.getRGB());
            }
        }
        return ImageOut;
    }

    public static double[][][] CalculateImageDifference(int[][][] Image1, int[][][] Image2) {
        int Dim1 = Image1.length;
        int Dim2 = Image1[0].length;
        int Dim3 = Image1[0][0].length;
        double[][][] ImageDifference = new double[Dim1][Dim2][Dim3];
        for (int ii = 0; ii < Dim1; ii++) {
            for (int jj = 0; jj < Dim2; jj++) {
                for (int kk = 0; kk < Dim3; kk++) {
                    ImageDifference[ii][jj][kk] = (double) ((Image1[ii][jj][kk] - Image2[ii][jj][kk]) * (Image1[ii][jj][kk] - Image2[ii][jj][kk]));
                }
            }
        }
        return ImageDifference;
    }

    public static double[][] MeanFilterSingleChan(double[][] ImIn, int MeanFilterSize) {
        // Mean filter a 2D double array
        // MeanFilterSize should be odd
        // Careful: this is the mean of the ABS values!
        int Offset = (MeanFilterSize - 1) / 2;
        int FilterElements = MeanFilterSize * MeanFilterSize;
        int ImWidth = ImIn.length;
        int ImHeight = ImIn[0].length;
        DescriptiveStatistics BlockValues;

        double[][] FilteredImage = new double[ImWidth - 2 * Offset][ImHeight - 2 * Offset];

        double Sum;
        for (int ii = Offset; ii <= ImWidth - MeanFilterSize + Offset; ii = ii + 1) {
            Sum = 0;
            for (int B_ii = ii - Offset; B_ii < ii + Offset + 1; B_ii++) {
                for (int B_jj = 0; B_jj < 2 * Offset + 1; B_jj++) {
                    Sum = Sum + ImIn[B_ii][B_jj];
                }
            }
            FilteredImage[ii - Offset][0] = Sum / FilterElements;

            for (int jj = Offset + 1; jj <= ImHeight - MeanFilterSize + Offset; jj = jj + 1) {
                for (int B_ii = ii - Offset; B_ii < ii + Offset + 1; B_ii++) {
                    Sum = Sum - ImIn[B_ii][jj - Offset - 1];
                    Sum = Sum + ImIn[B_ii][jj + Offset];
                }
                FilteredImage[ii - Offset][jj - Offset] = Sum / FilterElements;
                /*                BlockValues = new DescriptiveStatistics();
                 for (int B_ii = ii - Offset; B_ii < ii + Offset + 1; B_ii++) {
                 for (int B_jj = jj - Offset; B_jj < jj + Offset + 1; B_jj++) {
                 BlockValues.addValue(Math.abs(ImIn[B_ii][B_jj]));
                 }
                 }
                 FilteredImage[ii - Offset][jj - Offset] = BlockValues.getMean();
                 */
            }
        }
        return FilteredImage;
    }

    public static double[][][] MeanFilterRGB(double[][][] ImIn, int MeanFilterSize) {
        int Offset = (MeanFilterSize - 1) / 2;
        double[][][] FilteredIm = new double[3][ImIn[0].length - 2 * Offset][ImIn[0][0].length - 2 * Offset];

        FilteredIm[0] = MeanFilterSingleChan(ImIn[0], MeanFilterSize);
        FilteredIm[1] = MeanFilterSingleChan(ImIn[1], MeanFilterSize);
        FilteredIm[2] = MeanFilterSingleChan(ImIn[2], MeanFilterSize);

        return FilteredIm;
    }

    public static double[][] MeanChannelImage(double[][][] ImIn) {
        int ImWidth = ImIn[0].length;
        int ImHeight = ImIn[0][0].length;
        double[][] OutIm = new double[ImWidth][ImHeight];

        for (int ii = 0; ii < ImWidth; ii++) {
            for (int jj = 0; jj < ImHeight; jj++) {
                OutIm[ii][jj] = (ImIn[0][ii][jj] + ImIn[1][ii][jj] + ImIn[2][ii][jj]) / 3;
            }
        }

        return OutIm;
    }

    public static double SingleChannelMean(double[][] ImIn) {
        int ImWidth = ImIn.length;
        int ImHeight = ImIn[0].length;
        double Mean = 0;
        /*
         for (int kkkk = ImHeight-5; kkkk < ImHeight; kkkk++) {
         for (int llll = ImWidth-5; llll < ImWidth; llll++) {
         System.out.print(ImIn[llll][kkkk] + " ");
         }
         System.out.println();
         }
         System.out.println("----");
         */
        for (int ii = 0; ii < ImWidth; ii++) {
            for (int jj = 0; jj < ImHeight; jj++) {
                Mean = Mean + ImIn[ii][jj];
            }
        }
        Mean = Mean / (ImWidth * ImHeight);

        return Mean;
    }

    public static List<Integer> GetArrayLocalMinima(double[] ValuesIn) {
        List<Integer> Minima = new ArrayList();

        for (int ii = 1; ii < ValuesIn.length - 1; ii++) {
            //System.out.println(ValuesIn[ii - 1]);
            if ((ValuesIn[ii - 1] > ValuesIn[ii]) & (ValuesIn[ii + 1] > ValuesIn[ii])) {
                //System.out.println(ii);
                Minima.add(ii);
            }
        }
        //System.out.println(ValuesIn[ValuesIn.length - 2]);
        //System.out.println(ValuesIn[ValuesIn.length - 1]);
        return Minima;
    }

    public static double[][] NormalizeIm(double[][] ImIn) {
        int ImWidth = ImIn.length;
        int ImHeight = ImIn[0].length;
        double ImOut[][] = new double[ImWidth][ImHeight];

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        double colMin, colMax;

        for (double[] ImInRow : ImIn) {
            List b = Arrays.asList(ArrayUtils.toObject(ImInRow));
            colMin = (double) Collections.min(b);
            if (colMin < min) {
                min = colMin;
            }
            colMax = (double) Collections.max(b);
            if (colMax > max) {
                max = colMax;
            }
        }
        double spread = max - min;

        for (int ii = 0; ii < ImWidth; ii++) {
            for (int jj = 0; jj < ImHeight; jj++) {
                ImOut[ii][jj] = (ImIn[ii][jj] - min) / spread;
            }
        }
        return ImOut;
    }

    public static BufferedImage VisualizeWithJet(double[][] ImIn) {
        int ImWidth = ImIn.length;
        int ImHeight = ImIn[0].length;
        BufferedImage OutIm = new BufferedImage(ImWidth, ImHeight, 5);
        Color RGB;

        double[][] Map = JetMap.ColorMap;

        byte bytevalue;

        for (int ii = 0; ii < ImWidth; ii++) {
            for (int jj = 0; jj < ImHeight; jj++) {
                bytevalue = (byte) Math.round(ImIn[ii][jj] * 63);
                RGB = new Color((float) Map[bytevalue][0], (float) Map[bytevalue][1], (float) Map[(byte) Math.round(ImIn[ii][jj]) * 63][2]);
                OutIm.setRGB(ii, jj, RGB.getRGB());
            }
        }

        return OutIm;
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

    public static double[][] MedianFilter(double[][] ImIn, int MedianFilterSize) {
        // Median filter a 2D double array
        // MedianFilterSize should be odd
        // Careful: this is the mean of the ABS values!
        int Offset = (MedianFilterSize - 1) / 2;
        int ImWidth = ImIn.length;
        int ImHeight = ImIn[0].length;
        DescriptiveStatistics BlockValues;

        double[][] FilteredImage = new double[ImWidth - 2 * Offset][ImHeight - 2 * Offset];

        for (int ii = Offset; ii <= ImWidth - MedianFilterSize + Offset; ii = ii + 1) {
            for (int jj = Offset; jj <= ImHeight - MedianFilterSize + Offset; jj = jj + 1) {
                BlockValues = new DescriptiveStatistics();
                for (int B_ii = ii - Offset; B_ii < ii + Offset + 1; B_ii++) {
                    for (int B_jj = jj - Offset; B_jj < jj + Offset + 1; B_jj++) {
                        BlockValues.addValue(Math.abs(ImIn[B_ii][B_jj]));
                    }
                }
                FilteredImage[ii - Offset][jj - Offset] = BlockValues.getPercentile(50);
            }
        }
        return FilteredImage;
    }

    public static BufferedImage createJetVisualization(byte[][] InputGrayImage) {
        double[][] Map = JetMap.ColorMap;
        BufferedImage outImage = new BufferedImage(InputGrayImage.length, InputGrayImage[0].length, 5); //5 for PNG;
        Color RGB;
        //System.out.println(InputGrayImage.length);
        //System.out.println(InputGrayImage[0].length);

        for (int ii = 0; ii < InputGrayImage.length; ii++) {
            for (int jj = 0; jj < InputGrayImage[ii].length; jj++) {
                RGB = new Color((float) Map[InputGrayImage[ii][jj]][0], (float) Map[InputGrayImage[ii][jj]][1], (float) Map[InputGrayImage[ii][jj]][2]);
                outImage.setRGB(ii, jj, RGB.getRGB());
            }
        }

        return outImage;
    }

    public static double[][] BlockVar(double[][] InputIm, int BlockSize) {
        int BlockedWidth = (int) Math.floor(InputIm.length / BlockSize) * BlockSize;
        int BlockedHeight = (int) Math.floor(InputIm[0].length / BlockSize) * BlockSize;
        double[][] BlockedIm = new double[BlockedWidth / BlockSize][BlockedHeight / BlockSize];

        DescriptiveStatistics BlockValues;
        for (int ii = 0; ii < BlockedWidth; ii = ii + BlockSize) {
            for (int jj = 0; jj < BlockedHeight; jj = jj + BlockSize) {
                BlockValues = new DescriptiveStatistics();
                for (int B_ii = ii; B_ii < ii + BlockSize; B_ii++) {
                    for (int B_jj = jj; B_jj < jj + BlockSize; B_jj++) {
                        BlockValues.addValue(Math.abs(InputIm[B_ii][B_jj]));
                    }
                }
                BlockedIm[ii / BlockSize][jj / BlockSize] = Math.sqrt(BlockValues.getPercentile(50) / 0.6745);
            }
        }
        return BlockedIm;
    }

    public static String GetImageFormat(File InputImage) throws IOException {
        String format = null;
        ImageInputStream iis = ImageIO.createImageInputStream(InputImage);
        Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
        if (!iter.hasNext()) {
            throw new RuntimeException("No readers found! I don't think this is an image file");
        }
        ImageReader reader = iter.next();
        format = reader.getFormatName();

        return format;
    }

    public static double MinDouble2DArray(double[][] ArrayIn) {
        double min = Double.MAX_VALUE;
        double colMin;

        for (double[] ArrayInRow : ArrayIn) {
            List b = Arrays.asList(ArrayUtils.toObject(ArrayInRow));
            colMin = (double) Collections.min(b);
            if (colMin < min) {
                min = colMin;
            }
        }
        return min;
    }

    public static double MaxDouble2DArray(double[][] ArrayIn) {
        double max = -Double.MAX_VALUE;
        double colMax;

        for (double[] ArrayInRow : ArrayIn) {
            List b = Arrays.asList(ArrayUtils.toObject(ArrayInRow));
            colMax = (double) Collections.max(b);
            if (colMax > max) {
                max = colMax;
            }
        }
        return max;
    }

    public static BufferedImage AddTextToImage(BufferedImage ImIn, String Text, int ManualImageHeight){
        
        //use ManualImageHeight=0 to automatically detect height - set an ManualImageHeight value to insert multiple lines of equal height.

        int ImageHeight;
        if (ManualImageHeight!=0) {
            ImageHeight=ManualImageHeight;
        } else {
            ImageHeight=ImIn.getHeight();
        }
        
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        Font font = new Font("Arial", Font.PLAIN, (int) Math.round(ImageHeight*0.1));
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        //int width = fm.stringWidth(Text);

        int height = fm.getHeight();

        g2d.dispose();
        
        
        BufferedImage OutIm=new BufferedImage( ImIn.getWidth(), ImIn.getHeight()+height, BufferedImage.TYPE_INT_RGB);;
        for (int ii=0; ii<ImIn.getWidth();ii++){
        for (int jj=0; jj<ImIn.getHeight();jj++){
            OutIm.setRGB(ii, jj, ImIn.getRGB(ii, jj));
        }
        }

        /*
           Because font metrics is based on a graphics context, we need to create
           a small, temporary image so we can ascertain the width and height
           of the final image
         */

        g2d = OutIm.createGraphics();
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
        g2d.drawString(Text, 5, OutIm.getHeight()-Math.round(height*0.2));
        g2d.dispose();
        
        return OutIm;
    }

    public static List <BufferedImage> AddJPEGQualitiesToList(List<BufferedImage> ImIn, List<Integer> Qualities, List<Double> MinValues, List<Double> MaxValues, List<Integer> LocalMinGhostQualities){
    List <BufferedImage> OutList = new ArrayList();
         BufferedImage tmpImage;
         
        int ImageHeight=ImIn.get(0).getHeight();
        if (ImageHeight>ImIn.get(0).getWidth()) {
            ImageHeight=(int)Math.round(ImIn.get(0).getWidth());
        }
        
         
        for (int ii=0;ii<ImIn.size();ii++){
            tmpImage=AddTextToImage(ImIn.get(ii),"JPEG Quality: " + String.valueOf(Qualities.get(ii)),ImageHeight);
            tmpImage=AddTextToImage(tmpImage,"Min Value: " + String.format("%.2f", MinValues.get(ii)),ImageHeight);
            tmpImage=AddTextToImage(tmpImage,"Max Value: " + String.format("%.2f", MaxValues.get(ii)),ImageHeight);
            OutList.add(tmpImage);
            if (LocalMinGhostQualities.contains(Qualities.get(ii)) | Qualities.get(ii)==100){
                for (int jj=0;jj<4;jj++){
                    OutList.add(tmpImage);
                }
            }
    }
    
    return OutList;
    }
}
