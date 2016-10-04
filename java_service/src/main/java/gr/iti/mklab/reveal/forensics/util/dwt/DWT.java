package gr.iti.mklab.reveal.forensics.util.dwt;

/**
 * Copyright 2014 Mark Bishop This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details: http://www.gnu.org/licenses
 *
 * The author makes no warranty for the accuracy, completeness, safety, or
 * usefulness of any information provided and does not represent that its use
 * would not infringe privately owned right.
 */
import java.io.File;
import java.util.ArrayList;

import org.ojalgo.access.Access2D.Builder;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.BasicMatrix.Factory;
import org.ojalgo.matrix.PrimitiveMatrix;

/**
 * Class responsibility: Provide methods for discrete wavelet transforms for
 * signals having length that is an even power of two.
 */
public class DWT {

    /**
     * @param signal a double[] with length = even power of two
     * @param wavelet Haar, Daubechies, etc. (see OrthogonalFilters.java)
     * @param order e.g Daubechies 8 has order = 8
     * @param L coarsest scale to include in the transform
     * @param direction forward = transform, reverse = inverse transform
     * @return forward or reverse discrete wavelet transform
     * @throws Exception Invalid wavelet parameters
     */
    public static double[] transform(double[] signal, Wavelet wavelet,
            int order, int L, Direction direction) throws Exception {
        if (direction == Direction.forward) {
            return forwardDwt(signal, wavelet, order, L);
        } else {
            return inverseDwt(signal, wavelet, order, L);
        }
    }

    public static enum Direction {

        forward, reverse
    }

    /**
     *
     * @param signal a double[] with length = even power of two
     * @param wavelet Haar, Daubechies, etc.
     * @param order e.g Daubechies 8 has order = 8
     * @param L coarsest scale
     * @return Forward DWT
     * @throws Exception Invalid wavelet parameters
     */
    public static double[] forwardDwt(double[] signal, Wavelet wavelet,
            int order, int L) throws Exception {

        long begin, timer1 = 0, timer2 = 0, timer3 = 0, timer4 = 0, timer5 = 0, timer6 = 0;
        int n = signal.length;
        if (!isValidChoices(wavelet, order, L, n)) {
            throw new Exception(
                    "Invalid wavelet /order/scale/signal-length combination.");
        }
        double[] dWT = MatrixOps.deepCopy(signal);
        int log2n = (int) (Math.log(n) / Math.log(2));
        int iterations = log2n - L;
        int subLength = n;
        double[] H = OrthogonalFilters.getLowPass(wavelet, order);
        double[] G = OrthogonalFilters.getHighPass(H);

        //original code replaced with ojalgo implementation -up to 4-fold speed increase
        
       // int row1 = 0, row2 = 0, col1 = 0, col2 = 0;
        double[][] QMF = makeQMFMatrix(subLength, H, G);

        final Factory<PrimitiveMatrix> QMFFactory = PrimitiveMatrix.FACTORY;
        final Builder<PrimitiveMatrix> QMFBuilder = QMFFactory.getBuilder(QMF.length, QMF[0].length);
        for (int jj = 0; jj < QMF.length; jj++) {
            for (int ii = 0; ii < QMF[0].length; ii++) {
                QMFBuilder.set(ii, jj, QMF[jj][ii]);
            }
        }
        final BasicMatrix QMFBM = QMFBuilder.build();
        for (int i = 0; i < iterations; i++) {
            subLength = n / (int) (Math.pow(2, i));
            double[] subResult = new double[subLength];
            subResult = subCopy(dWT, subResult, subLength);
            final Factory<PrimitiveMatrix> subResultFactory = PrimitiveMatrix.FACTORY;
            final Builder<PrimitiveMatrix> subResultBuilder = subResultFactory.getBuilder(1, subResult.length);
            for (int jj = 0; jj < subResult.length; jj++) {
                subResultBuilder.set(0, jj, subResult[jj]);
            }

            final BasicMatrix subResultBM = subResultBuilder.build();
            final BasicMatrix tempBM = QMFBM.multiplyLeft(subResultBM);
            double[] temp = new double[QMF.length];//MatrixOps.multiply(QMF, subResult);
            for (int jj = 0; jj < tempBM.count(); jj++) {
                temp[jj] = (double) tempBM.get(jj);
            }
            dWT = subCopy(temp, dWT, subLength);
           //  row1 = QMF.length;
           // col1 = QMF[0].length;
           // row2 = subResult.length;
           // col2 = subResult.length;
        }        
        return dWT;
    }

    /**
     * Parameters should be the same values used during the forward transform.
     *
     * @param signal a double[] with length = even power of two
     * @param wavelet Haar, Daubechies, etc.
     * @param order e.g Daubechies 8 has order = 8
     * @param L coarsest scale
     * @return reconstructed signal by inverse DWT
     * @throws Exception a
     */
    public static double[] inverseDwt(double[] signal, Wavelet wavelet,
            int order, int L) throws Exception {
        int n = signal.length;
        if (!isValidChoices(wavelet, order, L, n)) {
            throw new Exception(
                    "Invalid wavelet /order/scale/signal-length combination.");
        }
        int log2n = (int) (Math.log(n) / Math.log(2));
        int subLength;
        double[] preserveCopy = new double[signal.length];
        preserveCopy = subCopy(signal, preserveCopy, signal.length);
        double[] H = OrthogonalFilters.getLowPass(wavelet, order);
        double[] G = OrthogonalFilters.getHighPass(H);
        for (int i = L + 1; i <= log2n; i++) {
            subLength = (int) (Math.pow(2, i));
            double[][] QMF = makeQMFMatrix(subLength, H, G);
            QMF = MatrixOps.transpose(QMF);
            double[] subResult = new double[subLength];
            subCopy(signal, subResult, subLength);
            subResult = MatrixOps.multiply(QMF, subResult);
            signal = subCopy(subResult, signal, subLength);
        }
        double[] iDWT = new double[n];
        iDWT = subCopy(signal, iDWT, n);
        signal = preserveCopy;
        return iDWT;
    }

    /**
     * Make a quadrature mirror matrix
     *
     * @param scale
     * @param H low pass filter
     * @param G high pass filter
     * @return QMF[scale][scale]
     */
    private static double[][] makeQMFMatrix(int scale, double[] H, double[] G) {
        int filterLen = H.length;
        int skip = 0;
        double[][] QMF = new double[scale][scale];
        for (int i = 0; i < (scale / 2); i++) {
            for (int j = 0; j < filterLen; j++) {
                int location = j + skip;
                if (location > scale - 1)// wrap
                {
                    location = location - (scale);
                }
                QMF[i][location] = H[j];
            }
            skip += 2;
        }
        skip = scale - 1;
        for (int i = scale - 1; i >= (scale / 2); i--) {
            for (int j = filterLen - 1; j >= 0; j--) {
                int location = -j + skip;
                if (location < 0) {
                    location += scale;
                }
                QMF[i][location] = G[filterLen - j - 1];
            }
            skip -= 2;
        }
        return QMF;
    }

    /**
     * Multi-resolution analysis See: Mallet, A Wavelet Tour of Signal
     * Processing, the Sparse Way, 2008, pp. 170-172
     *
     * @param signal a double[] with length = even power of two
     * @param wavelet Haar, Daubechies, etc.
     * @param order e.g Daubechies 8 has order = 8
     * @param L coarsest scale
     * @return An ArrayList(Object) result such that: result.get(0) is an
     * ArrayList(double[]) holding multi-resolution scale data from the finest
     * scale to the coarsest scale terminating with the approximation curve; and
     * result.get(1) is an array of j values corresponding to the scales used to
     * create the data where: scale = 2^-j and the terminating value is set to
     * "0" for the approximation scale.
     * @throws Exception Invalid wavelet parameters
     */
    public static ArrayList<Object> mRA(double[] signal, Wavelet wavelet,
            int order, int L) throws Exception {
        ArrayList<Object> result = new ArrayList<Object>();
        int n = signal.length;
        if (!isValidChoices(wavelet, order, L, n)) {
            throw new Exception(
                    "Invalid wavelet /order/scale/signal-length combination.");
        }
        int J = (int) (Math.log(n) / Math.log(2));
        double[] dwt = forwardDwt(signal, wavelet, order, L);
        ArrayList<double[]> mRA = new ArrayList<double[]>();
        for (int j = (J - 1); j >= L; j--) {
            double[] w = new double[n];
            int[] dyad = dyad(j);
            for (int k = dyad[0]; k <= dyad[dyad.length - 1]; k++) {
                w[k - 1] = dwt[k - 1];
            }
            mRA.add(inverseDwt(w, wavelet, order, L));
        }
        // All frequencies lower than those revealed at L
        double[] w = new double[n];
        int limit = (int) Math.pow(2, L);
        for (int i = 0; i < limit; i++) {
            w[i] = dwt[i];
        }
        mRA.add(inverseDwt(w, wavelet, order, L));

        int[] scalesUsed = new int[mRA.size()];
        int scaleCounter = 0;
        for (int j = (J - 1); j >= L; j--) {
            int[] dyad = DWT.dyad(j);
            scalesUsed[scaleCounter] = (int) (Math.log(dyad.length) / Math
                    .log(2));
            scaleCounter++;
        }
        // Next line: 0 is a dummy value for the approximation carrying all
        // lower frequencies corresponding to scales larger than 2^-L
        scalesUsed[scaleCounter] = 0;
        result.add(mRA);
        result.add(scalesUsed);
        return result;
    }

    /**
     *
     * @param k To be explained
     * @return An int[2^k] holding values from 2^k +1 to 2^(k + 1) e.g. dyad(2)
     * = {5, 6, 7, 8,}, dyad(3) = {9, 10, 11, 12, 13, 14, 15, 16}
     */
    public static int[] dyad(int k) {
        int[] dyad = null;
        if (k == 0) {
            dyad = new int[1];
            dyad[0] = 2;
            return dyad;
        }
        int lower = (int) Math.pow(2, k);
        int upper = (int) Math.pow(2, k + 1);
        int dyadLength = upper - lower;
        dyad = new int[dyadLength];
        for (int i = lower; i < upper; i++) {
            dyad[i - lower] = i + 1;
        }
        return dyad;
    }

    /**
     * Example: If source = {5, 6, 7}, destination = {1, 2, 3, 4}, and count = 2
     * Then result = {5, 6, 3, 4}
     *
     * @return the first count elements in source overwrite the first count
     * elements in the result.
     */
    private static double[] subCopy(double[] source, double[] destination,
            int count) {
        for (int i = 0; i < count; i++) {
            destination[i] = source[i];
        }
        return destination;
    }

    /**
     *
     * @param x the sequence to pad
     * @return If necessary, expanded sequence such that its length is an even
     * power of 2 by adding additional zero values.
     */
    public static double[] padPow2(double[] x) {
        int sizeIn = x.length;
        double log2N = Math.log(sizeIn) / Math.log(2);
        double ceiling = Math.ceil(log2N);
        if (log2N < ceiling) {
            log2N = ceiling;
            int sizePad = (int) Math.pow(2, log2N);
            double[] padX = new double[sizePad];
            for (int i = 0; i < sizePad; i++) {
                if (i < sizeIn) {
                    padX[i] = x[i];
                } else {
                    padX[i] = 0;
                }
            }
            return padX;
        } else {
            return x;
        }
    }

    /**
     *
     * @param xy A double[][] where xy[0] = x and xy[1] = f(x)
     * @return If necessary, expanded sequence such that its length is an even
     * power of 2 by adding additional zero values.
     */
    public static double[][] padPow2(double[][] xy) {
        int sizeIn = xy[0].length;
        double log2N = Math.log(sizeIn) / Math.log(2);
        double ceiling = Math.ceil(log2N);
        if (log2N < ceiling) {
            log2N = ceiling;
            int sizePad = (int) Math.pow(2, log2N);
            double[][] padXY = new double[2][sizePad];
            double dx = padXY[0][1] - padXY[0][0];
            for (int i = 0; i < sizePad; i++) {
                if (i < sizeIn) {
                    padXY[0][i] = xy[0][i];
                    padXY[1][i] = xy[1][i];
                } else {
                    padXY[0][i] = padXY[0][i - 1] + dx;
                    padXY[1][i] = 0;
                }
            }
            return padXY;
        } else {
            return xy;
        }
    }

    /**
     * Wavelet/param/scale/size sanity check
     *
     * @param wavelet To be explained
     * @param order To be explained
     * @param L To be explained
     * @param signalLength To be explained
     * @return true only if the wavelet/order exists in OrthogonalFileters.java
     * and the signal is long enough to be analyzed at the chosen coarsest scale
     * L.
     */
    private static boolean isValidChoices(Wavelet wavelet, int order, int L,
            int signalLength) {
        try {
            ArrayList<Integer> validParams = OrthogonalFilters
                    .validParameters(wavelet);
            boolean isValid = false;
            for (int i = 0; i < validParams.size(); i++) {
                if (validParams.get(i) == Integer.valueOf(order)) {
                    ArrayList<Integer> validScales;
                    validScales = OrthogonalFilters.validScales(order, wavelet,
                            signalLength);

                    for (int j = 0; j < validScales.size(); j++) {
                        if (validScales.get(j) == Integer.valueOf(L)) {
                            isValid = true;
                            break;
                        }
                    }
                    if (isValid) {
                        break;
                    }
                }
            }
            return isValid;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**
     * This method may be used to test the reliability of perfect reconstruction
     * using each of the orthogonal filters provided in OrthogonalFilters.java
     * for all valid orders provided.
     *
     * @throws Exception Invalid wavelet parameters (be sure your test signal is
     * long enough)
     */
    public static void relativeErrors() throws Exception {
        File file = FileOps.openDialog(System.getProperty("user.dir"));
        double[][] testFile = MatrixOps.transpose((double[][]) FileOps
                .openMatrix(file));
        double[] testFn = testFile[1];
        int fnLen = testFn.length;
        double[] idwt;
        double norm = MatrixOps.vector2Norm(testFn);
        for (Wavelet wavelet : Wavelet.values()) {
            ArrayList<Integer> params = OrthogonalFilters
                    .validParameters(wavelet);
            for (Integer param : params) {
                Integer coarsest = OrthogonalFilters.validScales(param,
                        wavelet, fnLen).get(0);
                double[] dwt = transform(testFn, wavelet, param, coarsest,
                        Direction.forward);
                idwt = transform(dwt, wavelet, param, coarsest,
                        Direction.reverse);
                double resid = MatrixOps.vector2Norm(MatrixOps.add(testFn,
                        MatrixOps.scale(-1.0, idwt)));
                double relResid = resid / norm;
                StringBuilder sb = new StringBuilder("Testing ");
                sb.append(wavelet + " " + param + " Relative residual: \n");
                sb.append(relResid + "\n\n");
                System.out.print(sb.toString());
                // The relative residuals should be near zero
            }
        }
    }

}
