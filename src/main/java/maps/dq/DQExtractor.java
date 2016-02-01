/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maps.dq;

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

import static util.Util.VisualizeWithJet;
import static util.Util.rem;

import util.DCTCoeffExtractor;

/**
 *
 * @author markzampoglou
 */
public final class DQExtractor {

    int[][] DCTs; // The array of DCT Coefficients of the image
    int MaxCoeffs = 15; //How many DCT coefficients to take into account
    // The sequence of DCT coefficients: zigzag order
    int[] coeff = {1, 9, 2, 3, 10, 17, 25, 18, 11, 4, 5, 12, 19, 26, 33, 41, 34, 27, 20, 13, 6, 7, 14, 21, 28, 35, 42, 49, 57, 50, 43, 36, 29, 22, 15, 8, 16, 23, 30, 37, 44, 51, 58, 59, 52, 45, 38, 31, 24, 32, 39, 46, 53, 60, 61, 54, 47, 40, 48, 55, 62, 63, 56, 64};
    public double[][] ProbabilityMap = null;
    public BufferedImage DisplaySurface = null;
    public double minProbValue = Double.MAX_VALUE;
    public double maxProbValue = -Double.MAX_VALUE;

    public interface CLibrary extends Library {

        CLibrary JPEGlib = (CLibrary) Native.loadLibrary((Platform.isWindows() ? "ExportDCT" : "ExportDCT"), CLibrary.class);
        int testInOut(int a);
        IntByReference GetDCT(String FileName);
    }

    public DQExtractor(String FileName) throws IOException {
        String imageFormat = util.Util.GetImageFormat(new File(FileName));
        try {
            if (imageFormat.equalsIgnoreCase("JPEG") | imageFormat.equalsIgnoreCase("JPG")) {
                DCTs = GetDCTCoeffsFromFile(FileName);
            }
            else {
                System.out.println("Not a JPEG image, getting DCT coefficients from pixel values (in case it is a resave from an older JPEG).");
                BufferedImage OrigImage;
                try {OrigImage = ImageIO.read(new File(FileName));
                    int[][] DCTs2=DCTCoeffExtractor.ExtractYDCT(OrigImage);
                    DCTs=DCTs2;
                }catch
                 (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Error err) {
        System.out.println("Could not load native JPEGlib-based DCT extractor, getting DCT coefficients from pixel values.");
        BufferedImage OrigImage;
        try {OrigImage = ImageIO.read(new File(FileName));
            int[][] DCTs2=DCTCoeffExtractor.ExtractYDCT(OrigImage);
            DCTs=DCTs2;
        }
        catch
                (IOException e) {
            e.printStackTrace();
        }
        }



        DetectDQDiscontinuities();
    }

    public void DetectDQDiscontinuities() {

        int ImWidth = DCTs.length;
        int ImHeight = DCTs[0].length;

        int[] p_h_avg = new int[MaxCoeffs];
        int[] p_h_fft = new int[MaxCoeffs];
        int[] p_final = new int[MaxCoeffs];

        double[][] P_tampered = new double[MaxCoeffs][];
        double[][] P_untampered = new double[MaxCoeffs][];

        for (int coeffIndex = 0; coeffIndex < MaxCoeffs; coeffIndex++) {

            int coe = coeff[coeffIndex];
            int StartY = coe % 8 - 1;
            if (StartY == -1) {
                StartY = 8;
            }
            int StartX = (int) Math.floor((coe - 1) / 8);

            List<Integer> selectedCoeffs = new ArrayList();
            for (int ii = StartX; ii < ImWidth; ii += 8) {
                for (int jj = StartY; jj < ImHeight; jj += 8) {
                    selectedCoeffs.add(DCTs[ii][jj]);
                }
            }

            int minCoeffValue = Collections.min(selectedCoeffs);
            int maxCoeffValue = Collections.max(selectedCoeffs);
            int s_0;
            Double[] CoeffHist = new Double[0];
            if (maxCoeffValue - minCoeffValue > 0) {
                //will be a power of 2 to allow for FFT (zero padded)
                int TrueHistRange = maxCoeffValue - minCoeffValue + 1;
                //int HistLength = TrueHistRange;
                int HistLength = (int) Math.pow(2, Math.ceil(Math.log(TrueHistRange) / Math.log(2)));

                CoeffHist = new Double[HistLength];

                boolean NonZeroValue = false;

                for (int ii = 0; ii < CoeffHist.length; ii++) {
                    CoeffHist[ii] = 0.0;
                }

                for (Integer selectedCoeff : selectedCoeffs) {
                    CoeffHist[selectedCoeff - minCoeffValue] += 1;
                }

                List<Double> CoeffHistList = Arrays.asList(CoeffHist);
                s_0 = CoeffHistList.indexOf(Collections.max(CoeffHistList));

                List<Double> H = new ArrayList<>();
                DescriptiveStatistics vals;
                for (int coeffInd = 1; coeffInd < CoeffHistList.size(); coeffInd++) {
                    vals = new DescriptiveStatistics();
                    for (int leapInd = s_0; leapInd < CoeffHistList.size(); leapInd += coeffInd) {
                        vals.addValue(CoeffHistList.get(leapInd));
                    }
                    for (int leapInd = s_0 - coeffInd; leapInd >= 0; leapInd -= coeffInd) {
                        vals.addValue(CoeffHistList.get(leapInd));
                    }
                    H.add(vals.getMean());
                }
                p_h_avg[coeffIndex] = (H.indexOf(Collections.max(H)));

                FastFourierTransformer FFTransformer = new FastFourierTransformer(DftNormalization.STANDARD);
                Complex[] FFT = FFTransformer.transform(ArrayUtils.toPrimitive(CoeffHist), TransformType.FORWARD);

                double[] Power = new double[FFT.length];
                for (int ii = 0; ii < Power.length; ii++) {
                    Power[ii] = FFT[ii].abs();
                }

                //Find first local minimum, to bypass DC peak
                double DC = Power[0];
                int FreqValley = 1;
                while (FreqValley < Power.length - 1 & Power[FreqValley] >= Power[FreqValley + 1]) {
                    FreqValley++;
                }

                int MaxFFTInd = 0;
                double MaxFFTVal = 0;
                double MinFFTVal = Double.MAX_VALUE;
                for (int ii = FreqValley; ii < Power.length / 2; ii++) {
                    if (Power[ii] > MaxFFTVal) {
                        MaxFFTInd = ii;
                        MaxFFTVal = Power[ii];
                    }
                    if (Power[ii] < MinFFTVal) {
                        MinFFTVal = Power[ii];
                    }
                }
                if (MaxFFTInd == 0 | MaxFFTVal < (DC / 5) | MinFFTVal / MaxFFTVal > 0.9) {
                    p_h_fft[coeffIndex] = 1;
                } else {
                    p_h_fft[coeffIndex] = Math.round(CoeffHist.length / MaxFFTInd);
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

            P_tampered[coeffIndex] = new double[selectedCoeffs.size()];
            P_untampered[coeffIndex] = new double[selectedCoeffs.size()];
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
                            //if (period_start[kk] + p_final[coeffIndex] - 1 > CoeffHist.length) {
                                if (period[ii] >= CoeffHist.length) {
                                    period[ii] = period[ii] - p_final[coeffIndex];
                                }
                            //}
                        }
                        num[kk] = (int) CoeffHist[adjustedCoeffs[kk]].doubleValue();
                        denom[kk] = 0;
                        for (int ll = 0; ll < period.length; ll++) {
                            //System.out.println(period[ll] + " " + CoeffHist.length);
                            denom[kk] = denom[kk] + (int) CoeffHist[period[ll]].doubleValue();
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
                        num[kk] = (int) CoeffHist[adjustedCoeffs[kk]].doubleValue();
                        denom[kk] = 0;
                        for (int ll = 0; ll < period.length; ll++) {
                            denom[kk] = denom[kk] + (int) CoeffHist[period[ll]].doubleValue();
                        }
                    }

                    P_u[kk] = ((double) num[kk] / denom[kk]);
                    P_t[kk] = (1.0 / p_final[coeffIndex]);
                    if (P_u[kk] + P_t[kk] != 0) {
                        P_tampered[coeffIndex][kk] = P_t[kk] / (P_u[kk] + P_t[kk]);
                        P_untampered[coeffIndex][kk] = P_u[kk] / (P_u[kk] + P_t[kk]);

                    } else {
                        P_tampered[coeffIndex][kk] = 0.5;
                        P_untampered[coeffIndex][kk] = 0.5;
                    }
                }

            } else {
                for (int kk = 0; kk < selectedCoeffs.size(); kk++) {
                    P_tampered[coeffIndex][kk] = 0.5;
                    P_untampered[coeffIndex][kk] = 0.5;
                }
            }

        }
        double[] P_tampered_Overall = new double[P_tampered[0].length];
        double P_tampered_prod;
        double P_untampered_prod;

        for (int locationIndex = 0; locationIndex < P_tampered[0].length; locationIndex++) {
            P_tampered_prod = 1;
            P_untampered_prod = 1;
            for (int coeffIndex = 0; coeffIndex < P_tampered.length; coeffIndex++) {
                P_tampered_prod = P_tampered_prod * P_tampered[coeffIndex][locationIndex];
                P_untampered_prod = P_untampered_prod * P_untampered[coeffIndex][locationIndex];
            }
            if (P_tampered_prod + P_untampered_prod != 0) {
                P_tampered_Overall[locationIndex] = P_tampered_prod / (P_tampered_prod + P_untampered_prod);
            } else {
                P_tampered_Overall[locationIndex] = 0;
            }
        }

        int BlocksH = ImWidth / 8;
        int BlocksV = ImHeight / 8;
        double[][] OutputMap = new double[BlocksV][BlocksH];
        for (int kk = 0; kk < P_tampered_Overall.length; kk++) {
            OutputMap[kk % BlocksV][(int) Math.floor(kk / BlocksV)] = P_tampered_Overall[kk];
            if (P_tampered_Overall[kk] > maxProbValue) {
                maxProbValue = P_tampered_Overall[kk];
            }
            if (P_tampered_Overall[kk] < minProbValue) {
                minProbValue = P_tampered_Overall[kk];
            }
        }

        ProbabilityMap = OutputMap;

        BufferedImage OutputIm = VisualizeWithJet(OutputMap);
        DisplaySurface = OutputIm;
    }



    public final int[][] GetDCTCoeffsFromFile(String FileName) {
        IntByReference intFromC;
        int[][] DCTCoeffs=null;
        intFromC = CLibrary.JPEGlib.GetDCT(FileName);
        Pointer p = intFromC.getPointer();
        int[] ImageSize = new int[2];
        p.read(0, ImageSize, 0, 2);
        int NBlocksX, NBlocksY;
        NBlocksX = (int) ImageSize[0];
        NBlocksY = (int) ImageSize[1];
        int[] IntFromC = new int[NBlocksX * NBlocksY * 64];
        p.read(8, IntFromC, 0, NBlocksX * NBlocksY * 64);

        DCTCoeffs = new int[(NBlocksX) * 8][(NBlocksY) * 8]; //matlab structure, rows first

        int SerialInd;
        for (int BXInd = 0; BXInd < NBlocksX; BXInd++) {
            for (int BYInd = 0; BYInd < NBlocksY; BYInd++) {
                for (int ii = 0; ii < 8; ii++) {
                    for (int jj = 0; jj < 8; jj++) {
                        SerialInd = (BXInd * (NBlocksY) + BYInd) * 64 + ii * 8 + jj;
                        DCTCoeffs[BXInd * 8 + ii][BYInd * 8 + jj] = IntFromC[SerialInd];
                    }
                }
            }
        }


        return DCTCoeffs;
    }


    /*--------------------------------------------------------------------------*/
    // Call this static method to see if jpeglib and the related 
    //infrastructure has been properly set up. The expected output from this 
    //method appears at the end

    /*
    public static void JPGDemo() {

        IntByReference intFromC;
        intFromC = CLibrary.JPEGlib.GetDCT("JPEGDemo.jpg");
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

        int[][] DCTs = new int[(NBlocksX) * 8][(NBlocksY) * 8]; //matlab structure, rows first

        int SerialInd;
        for (int BXInd = 0; BXInd < NBlocksX; BXInd++) {
            for (int BYInd = 0; BYInd < NBlocksY; BYInd++) {
                for (int ii = 0; ii < 8; ii++) {
                    for (int jj = 0; jj < 8; jj++) {
                        SerialInd = (BXInd * (NBlocksY) + BYInd) * 64 + ii * 8 + jj;
                        DCTs[BXInd * 8 + ii][BYInd * 8 + jj] = IntFromC[SerialInd];
                    }
                }
            }
        }

        for (int ii = 0; ii < 8; ii++) {
            for (int jj = 0; jj < 16; jj++) {
                System.out.print((DCTs[ii][jj] + " "));
            }
            System.out.println();
        }
        System.out.println();
        for (int ii = DCTs.length - 8; ii < DCTs.length; ii++) {
            for (int jj = DCTs[1].length - 16; jj < DCTs[1].length; jj++) {
                System.out.print((DCTs[ii][jj] + " "));
            }
            System.out.println();
        }
    }
    */
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
