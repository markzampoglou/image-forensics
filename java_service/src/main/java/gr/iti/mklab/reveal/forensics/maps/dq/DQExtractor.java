/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iti.mklab.reveal.forensics.maps.dq;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.transform.DftNormalization;

import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import javax.imageio.ImageIO;

import static gr.iti.mklab.reveal.forensics.util.Util.visualizeWithJet;
import static gr.iti.mklab.reveal.forensics.util.Util.rem;

import gr.iti.mklab.reveal.forensics.util.dctCoeffExtractor;

/**
 *
 * @author markzampoglou
 */
public final class DQExtractor {

    int[][] dcts; // The array of DCT Coefficients of the image
    int maxCoeffs = 15; //How many DCT coefficients to take into account
    // The sequence of DCT coefficients: zigzag order
    int[] coeff = {1, 9, 2, 3, 10, 17, 25, 18, 11, 4, 5, 12, 19, 26, 33, 41, 34, 27, 20, 13, 6, 7, 14, 21, 28, 35, 42, 49, 57, 50, 43, 36, 29, 22, 15, 8, 16, 23, 30, 37, 44, 51, 58, 59, 52, 45, 38, 31, 24, 32, 39, 46, 53, 60, 61, 54, 47, 40, 48, 55, 62, 63, 56, 64};
    public double[][] probabilityMap = null;
    public BufferedImage displaySurface = null;
    public double minProbValue = Double.MAX_VALUE;
    public double maxProbValue = -Double.MAX_VALUE;

    public interface CLibrary extends Library {

        CLibrary dctLib = (CLibrary) Native.loadLibrary((Platform.isWindows() ? "ExportDCT" : "ExportDCT"), CLibrary.class);
        int testInOut(int a);
        IntByReference getDCT(String FileName);
    }

    public DQExtractor(String fileName) throws IOException {
        String imageFormat = gr.iti.mklab.reveal.forensics.util.Util.getImageFormat(new File(fileName));
        try {
            if (imageFormat.equalsIgnoreCase("JPEG") | imageFormat.equalsIgnoreCase("JPG")) {
                dcts = getDCTCoeffsFromFile(fileName);
            }
            else {
                System.out.println("Not a JPEG image, getting DCT coefficients from pixel values (in case it is a resave from an older JPEG).");
                BufferedImage origImage;
                try {origImage = ImageIO.read(new File(fileName));
                    int[][] dcts2= dctCoeffExtractor.extractYDCT(origImage);
                    dcts =dcts2;
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Error err) {
            err.printStackTrace();
            System.out.println("Could not load native JPEGlib-based DCT extractor, getting DCT coefficients from pixel values.");
            BufferedImage OrigImage;
            try {OrigImage = ImageIO.read(new File(fileName));
                int[][] dcts2= dctCoeffExtractor.extractYDCT(OrigImage);
                dcts =dcts2;
            }
            catch
                    (IOException e) {
                e.printStackTrace();
            }
        }



        detectDQDiscontinuities();
    }

    public void detectDQDiscontinuities() {

        int imWidth = dcts.length;
        int imHeight = dcts[0].length;

        int[] p_h_avg = new int[maxCoeffs];
        int[] p_h_fft = new int[maxCoeffs];
        int[] p_final = new int[maxCoeffs];

        double[][] pTampered = new double[maxCoeffs][];
        double[][] pUntampered = new double[maxCoeffs][];

        for (int coeffIndex = 0; coeffIndex < maxCoeffs; coeffIndex++) {

            int coe = coeff[coeffIndex];
            int startY = coe % 8 - 1;
            if (startY == -1) {
                startY = 8;
            }
            int startX = (int) Math.floor((coe - 1) / 8);

            List<Integer> selectedCoeffs = new ArrayList<Integer>();
            for (int ii = startX; ii < imWidth; ii += 8) {
                for (int jj = startY; jj < imHeight; jj += 8) {
                    selectedCoeffs.add(dcts[ii][jj]);
                }
            }

            int minCoeffValue = Collections.min(selectedCoeffs);
            int maxCoeffValue = Collections.max(selectedCoeffs);
            int s_0;
            Double[] coeffHist = new Double[0];
            if (maxCoeffValue - minCoeffValue > 0) {
                //will be a power of 2 to allow for fft (zero padded)
                int trueHistRange = maxCoeffValue - minCoeffValue + 1;
                //int histLength = trueHistRange;
                int histLength = (int) Math.pow(2, Math.ceil(Math.log(trueHistRange) / Math.log(2)));

                coeffHist = new Double[histLength];

                for (int ii = 0; ii < coeffHist.length; ii++) {
                    coeffHist[ii] = 0.0;
                }

                for (Integer selectedCoeff : selectedCoeffs) {
                    coeffHist[selectedCoeff - minCoeffValue] += 1;
                }

                List<Double> coeffHistList = Arrays.asList(coeffHist);
                s_0 = coeffHistList.indexOf(Collections.max(coeffHistList));

                List<Double> h = new ArrayList<>();
                DescriptiveStatistics vals;
                for (int coeffInd = 1; coeffInd < coeffHistList.size(); coeffInd++) {
                    vals = new DescriptiveStatistics();
                    for (int leapInd = s_0; leapInd < coeffHistList.size(); leapInd += coeffInd) {
                        vals.addValue(coeffHistList.get(leapInd));
                    }
                    for (int leapInd = s_0 - coeffInd; leapInd >= 0; leapInd -= coeffInd) {
                        vals.addValue(coeffHistList.get(leapInd));
                    }
                    h.add(vals.getMean());
                }
                p_h_avg[coeffIndex] = (h.indexOf(Collections.max(h)));

                FastFourierTransformer fastFourierTransformer = new FastFourierTransformer(DftNormalization.STANDARD);
                Complex[] fft = fastFourierTransformer.transform(ArrayUtils.toPrimitive(coeffHist), TransformType.FORWARD);

                double[] power = new double[fft.length];
                for (int ii = 0; ii < power.length; ii++) {
                    power[ii] = fft[ii].abs();
                }

                //Find first local minimum, to bypass DC peak
                double DC = power[0];
                int FreqValley = 1;
                while (FreqValley < power.length - 1 & power[FreqValley] >= power[FreqValley + 1]) {
                    FreqValley++;
                }

                int maxFFTInd = 0;
                double maxFFTVal = 0;
                double minFFTVal = Double.MAX_VALUE;
                for (int ii = FreqValley; ii < power.length / 2; ii++) {
                    if (power[ii] > maxFFTVal) {
                        maxFFTInd = ii;
                        maxFFTVal = power[ii];
                    }
                    if (power[ii] < minFFTVal) {
                        minFFTVal = power[ii];
                    }
                }
                if (maxFFTInd == 0 | maxFFTVal < (DC / 5) | minFFTVal / maxFFTVal > 0.9) {
                    p_h_fft[coeffIndex] = 1;
                } else {
                    p_h_fft[coeffIndex] = Math.round(coeffHist.length / maxFFTInd);
                }

            } else {
                p_h_avg[coeffIndex] = 1;
                p_h_fft[coeffIndex] = 1;
                s_0 = 0;
            }
            if (p_h_avg[coeffIndex] < p_h_fft[coeffIndex]) {
                p_final[coeffIndex] = p_h_avg[coeffIndex];
            } else {
                p_final[coeffIndex] = p_h_fft[coeffIndex];
            }

            pTampered[coeffIndex] = new double[selectedCoeffs.size()];
            pUntampered[coeffIndex] = new double[selectedCoeffs.size()];
            int[] adjustedCoeffs = new int[selectedCoeffs.size()];
            int[] period_start = new int[selectedCoeffs.size()];
            int[] period;
            int[] num = new int[selectedCoeffs.size()];
            int[] denom = new int[selectedCoeffs.size()];
            double[] P_u = new double[selectedCoeffs.size()];
            double[] P_t = new double[selectedCoeffs.size()];

            if (p_final[coeffIndex] != 1) {
                for (int ii = 0; ii < adjustedCoeffs.length; ii++) {
                    adjustedCoeffs[ii] = selectedCoeffs.get(ii) - minCoeffValue;
                    period_start[ii] = adjustedCoeffs[ii] - rem(adjustedCoeffs[ii] - s_0, p_final[coeffIndex]);
                }
                for (int kk = 0; kk < selectedCoeffs.size(); kk++) {
                    if (period_start[kk] > s_0) {
                        period = new int[p_final[coeffIndex]];
                        for (int ii = 0; ii < p_final[coeffIndex]; ii++) {
                            period[ii] = period_start[kk] + ii;
                            if (period[ii] >= coeffHist.length) {
                                period[ii] = period[ii] - p_final[coeffIndex];
                            }
                        }
                        num[kk] = (int) coeffHist[adjustedCoeffs[kk]].doubleValue();
                        denom[kk] = 0;
                        for (int ll = 0; ll < period.length; ll++) {
                            denom[kk] = denom[kk] + (int) coeffHist[period[ll]].doubleValue();
                        }
                    } else {
                        period = new int[p_final[coeffIndex]];
                        for (int ii = 0; ii < p_final[coeffIndex]; ii++) {
                            period[ii] = period_start[kk] - ii;
                            if (period_start[kk] - p_final[coeffIndex] + 1 <= 0) {
                                if (period[ii] <= 0) {
                                    period[ii] = period[ii] + p_final[coeffIndex];
                                }
                            }
                        }
                        num[kk] = (int) coeffHist[adjustedCoeffs[kk]].doubleValue();
                        denom[kk] = 0;
                        for (int ll = 0; ll < period.length; ll++) {
                            denom[kk] = denom[kk] + (int) coeffHist[period[ll]].doubleValue();
                        }
                    }

                    P_u[kk] = ((double) num[kk] / denom[kk]);
                    P_t[kk] = (1.0 / p_final[coeffIndex]);
                    if (P_u[kk] + P_t[kk] != 0) {
                        pTampered[coeffIndex][kk] = P_t[kk] / (P_u[kk] + P_t[kk]);
                        pUntampered[coeffIndex][kk] = P_u[kk] / (P_u[kk] + P_t[kk]);

                    } else {
                        pTampered[coeffIndex][kk] = 0.5;
                        pUntampered[coeffIndex][kk] = 0.5;
                    }
                }

            } else {
                for (int kk = 0; kk < selectedCoeffs.size(); kk++) {
                    pTampered[coeffIndex][kk] = 0.5;
                    pUntampered[coeffIndex][kk] = 0.5;
                }
            }

        }
        double[] pTamperedOverall = new double[pTampered[0].length];
        double pTamperedProd;
        double pUntamperedProd;

        for (int locationIndex = 0; locationIndex < pTampered[0].length; locationIndex++) {
            pTamperedProd = 1;
            pUntamperedProd = 1;
            for (int coeffIndex = 0; coeffIndex < pTampered.length; coeffIndex++) {
                pTamperedProd = pTamperedProd * pTampered[coeffIndex][locationIndex];
                pUntamperedProd = pUntamperedProd * pUntampered[coeffIndex][locationIndex];
            }
            if (pTamperedProd + pUntamperedProd != 0) {
                pTamperedOverall[locationIndex] = pTamperedProd / (pTamperedProd + pUntamperedProd);
            } else {
                pTamperedOverall[locationIndex] = 0;
            }
        }

        int blocksH = imWidth / 8;
        int blocksV = imHeight / 8;
        double[][] outputMap = new double[blocksV][blocksH];
        for (int kk = 0; kk < pTamperedOverall.length; kk++) {
            outputMap[kk % blocksV][(int) Math.floor(kk / blocksV)] = pTamperedOverall[kk];
            if (pTamperedOverall[kk] > maxProbValue) {
                maxProbValue = pTamperedOverall[kk];
            }
            if (pTamperedOverall[kk] < minProbValue) {
                minProbValue = pTamperedOverall[kk];
            }
        }
        probabilityMap = outputMap;
        BufferedImage outputIm = visualizeWithJet(outputMap);
        // output
        displaySurface = outputIm;
    }


    public final int[][] getDCTCoeffsFromFile(String FileName) {

        System.out.println(CLibrary.dctLib.testInOut(3));

        IntByReference intFromCByRef;
        int[][] dctCoeffs =null;
        intFromCByRef = CLibrary.dctLib.getDCT(FileName);

        Pointer p = intFromCByRef.getPointer();
        int[] imageSize = new int[2];
        p.read(0, imageSize, 0, 2);
        int nBlocksX, nBlocksY;
        nBlocksX = (int) imageSize[0];
        nBlocksY = (int) imageSize[1];
        int[] intFromC = new int[nBlocksX * nBlocksY * 64];
        p.read(8, intFromC, 0, nBlocksX * nBlocksY * 64);

        dctCoeffs = new int[(nBlocksX) * 8][(nBlocksY) * 8]; //matlab structure, rows first

        int serialInd;
        for (int bxInd = 0; bxInd < nBlocksX; bxInd++) {
            for (int byInd = 0; byInd < nBlocksY; byInd++) {
                for (int ii = 0; ii < 8; ii++) {
                    for (int jj = 0; jj < 8; jj++) {
                        serialInd = (bxInd * (nBlocksY) + byInd) * 64 + ii * 8 + jj;
                        dctCoeffs[bxInd * 8 + ii][byInd * 8 + jj] = intFromC[serialInd];
                    }
                }
            }
        }


        return dctCoeffs;
    }


    /*--------------------------------------------------------------------------*/
    // Call this static method to see if jpeglib and the related 
    //infrastructure has been properly set up. The expected output from this 
    //method appears at the end


    public static void JPGDemo() {

        IntByReference intFromC;
        intFromC = CLibrary.dctLib.getDCT("src/main/resources/3.Flag.jpg");
        Pointer p = intFromC.getPointer();

        int[] ImageSize = new int[2];
        p.read(0, ImageSize, 0, 2);

        System.out.println();

        int NBlocksX, NBlocksY;
        NBlocksX = (int) ImageSize[0];
        NBlocksY = (int) ImageSize[1];

        System.out.println("NBlocks:" + NBlocksX + "x" + NBlocksY);
        System.out.println(NBlocksX * NBlocksY * 64 + " cefficients will be read by J");

        int[] IntFromC = new int[NBlocksX * NBlocksY * 64];
        p.read(8, IntFromC, 0, NBlocksX * NBlocksY * 64);

        System.out.println("Image size:" + ImageSize[0] + "x" + ImageSize[1]);
        System.out.println("Coefficients imported: " + IntFromC.length);

        int[][] dcts = new int[(NBlocksX) * 8][(NBlocksY) * 8]; //matlab structure, rows first

        int SerialInd;
        for (int BXInd = 0; BXInd < NBlocksX; BXInd++) {
            for (int BYInd = 0; BYInd < NBlocksY; BYInd++) {
                for (int ii = 0; ii < 8; ii++) {
                    for (int jj = 0; jj < 8; jj++) {
                        SerialInd = (BXInd * (NBlocksY) + BYInd) * 64 + ii * 8 + jj;
                        dcts[BXInd * 8 + ii][BYInd * 8 + jj] = IntFromC[SerialInd];
                    }
                }
            }
        }

        for (int ii = 0; ii < 8; ii++) {
            for (int jj = 0; jj < 16; jj++) {
                System.out.print((dcts[ii][jj] + " "));
            }
            System.out.println();
        }
        System.out.println();
        for (int ii = dcts.length - 8; ii < dcts.length; ii++) {
            for (int jj = dcts[1].length - 16; jj < dcts[1].length; jj++) {
                System.out.print((dcts[ii][jj] + " "));
            }
            System.out.println();
        }
    }

    public static void main (String[] args){
        /*try {
            DQExtractor dqExtractor=new DQExtractor("src/main/resources/3.Flag.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}

/* if JPEGDemo is present in the folder, output should be:

 NBlocks:194x130
 1614080 cefficients will be read by J
 Image size:194x130
 Coefficients imported: 1614080

 5 1 -2 0 0 0 0 0 5 2 0 0 0 0 0 0 
 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 
 0 0 0 0 0 0 0 0 -1 0 0 0 0 0 0 0 
 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 
 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 
 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 
 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 
 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 

 -58 -8 0 -3 -1 0 0 0 -49 0 0 0 0 0 0 0 
 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 
 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 
 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 
 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 
 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 
 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 
 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0

 */
