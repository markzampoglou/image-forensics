package gr.iti.mklab.reveal.forensics.maps.blocking;

import gr.iti.mklab.reveal.forensics.util.Util;
import org.apache.commons.lang3.ArrayUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by marzampoglou on 11/24/15.
 */
public class BlockingExtractor {

    public BufferedImage displaySurface = null;
    public double blkmin;
    public double blkmax;

    // This thresh is used to remove extremely strong edges:
    // block edges are definitely going to be weak
    int diffThresh =50;
    // Accumulator size. Larger may overcome small splices, smaller may not
    // aggregate enough.
    int accuSize =33;

    public BlockingExtractor(String fileName) throws IOException {
        getBLKArtifact(fileName);
    }

    private void getBLKArtifact(String fileName) throws IOException {

        BufferedImage origImage;
        origImage = ImageIO.read(new File(fileName));
        int[][][] origByteImage = Util.getRGBArray(origImage);
        int[][] y = new int[origByteImage[0].length][origByteImage[0][0].length];
        for (int ii = 0; ii < y.length; ii++) {
            for (int jj = 0; jj < y[0].length; jj++) {
                //Follow MATLAB's (ITU-R BT.601) conversion
                y[ii][jj] = (int) Math.round((65.481 * ((float) origByteImage[0][ii][jj] / 255) + 128.553 * ((float) origByteImage[1][ii][jj] / 255) + 24.966 * ((float) origByteImage[2][ii][jj]) / 255));
            }
        }

        int[][] paddedYX = Util.mirrorPadImage(y, 0, (int) Math.floor(accuSize / 2));
        int[][] im2DiffX = new int[paddedYX.length][paddedYX[0].length];
        for (int ii = 1; ii < paddedYX.length - 1; ii++) {
            for (int jj = 0; jj < paddedYX[0].length; jj++) {
                im2DiffX[ii][jj] = Math.abs(2 * paddedYX[ii][jj] - paddedYX[ii + 1][jj] - paddedYX[ii - 1][jj]);
                if (Math.abs(im2DiffX[ii][jj]) > diffThresh)
                    im2DiffX[ii][jj] = diffThresh;
            }
        }
        for (int jj = 0; jj < paddedYX[0].length; jj++) {
            im2DiffX[0][jj] = im2DiffX[1][jj];
            im2DiffX[im2DiffX.length - 1][jj] = im2DiffX[im2DiffX.length - 2][jj];
        }
        int[][] summedV = Util.sumFilterSingleChannelVert(im2DiffX, accuSize);
        int[][] summedVPadded = Util.mirrorPadImage(summedV, (int) Math.floor(accuSize / 2), 0);
        int[][] midV = Util.medianFilterSingleChannelHorz(summedVPadded, accuSize);
        int[][] eV = Util.get2DArrayDifference(summedV, midV);
        int[][] paddedeV = Util.mirrorPadImage(eV, 16, 0);
        int[][] vertMid = new int[eV.length][eV[0].length];       
        for (int ii = 16; ii < paddedeV.length - 16; ii++) {
            for (int jj = 0; jj < paddedeV[0].length; jj++) {
                int[] temp_ste8v = new int[5];
                int counterv = 0;
                for (int step = -16; step <= 16; step += 8) {
                    temp_ste8v[counterv] = paddedeV[ii + step][jj];
                    counterv = counterv + 1;
                }
                Arrays.sort(temp_ste8v);
                vertMid[ii - 16][jj] =  temp_ste8v[3]; 
            }
        }

        int[][] paddedYY = Util.mirrorPadImage(y, (int) Math.floor(accuSize / 2), 0);
        int[][] im2DiffY = new int[paddedYY.length][paddedYY[0].length];
        for (int ii = 0; ii < paddedYY.length; ii++) {
            for (int jj = 1; jj < paddedYY[0].length-1; jj++) {
                im2DiffY[ii][jj] = Math.abs(2 * paddedYY[ii][jj] - paddedYY[ii][jj+1] - paddedYY[ii][jj-1]);
                if (Math.abs(im2DiffY[ii][jj]) > diffThresh)
                    im2DiffY[ii][jj] = diffThresh;
            }
        }
        for (int ii = 0; ii < paddedYY.length; ii++) {
            im2DiffY[ii][0] = im2DiffY[ii][1];
            im2DiffY[ii][im2DiffY[0].length - 1] = im2DiffY[ii][im2DiffY[0].length - 2];
        }
        int[][] summedH = Util.sumFilterSingleChannelHorz(im2DiffY, accuSize);
        int[][] summedHPadded = Util.mirrorPadImage(summedH, 0, (int) Math.floor(accuSize / 2));
        int[][] midH = Util.medianFilterSingleChannelVert(summedHPadded, accuSize);
        int[][] eH = Util.get2DArrayDifference(summedH, midH);
        int[][] paddedeH = Util.mirrorPadImage(eH, 0, 16);
        int[][] horzMid = new int[eH.length][eH[0].length];
        for (int ii = 0; ii < paddedeH.length; ii++) {
            for (int jj = 16; jj < paddedeH[0].length-16; jj++) {
                int[] temp_ste8 = new int[5];
                int counter = 0;
                for (int Step = -16; Step <= 16; Step += 8) {
                    temp_ste8[counter] = paddedeH[ii][jj + Step];
                    counter = counter + 1;
                }
                Arrays.sort(temp_ste8);
                horzMid[ii][jj-16] = temp_ste8[3];   
            }
        }
        int[][] blockDiff=Util.get2DArraySum(horzMid, vertMid);
        float[][] blk= blockProcess(blockDiff);
        double[][] normBLK=Util.normalizeIm(blk);
        
        // Output
        displaySurface = Util.visualizeWithJet(normBLK);
        blkmax =Util.maxDouble2DArray(blk);
        blkmin =Util.minDouble2DArray(blk);
        }

    private float[][] blockProcess(int[][] ImIn){
        float[][] outArray= new float[(int)Math.ceil((ImIn.length-1)/8)][(int)Math.ceil((ImIn[0].length-1)/8)];
        int [][] blockLine = new int[8][ImIn[0].length];
        int [][] block= new int[8][8];
        for(int ii=0;ii<ImIn.length-8;ii=ii+8){
            System.arraycopy(ImIn,ii,blockLine,0,8);
            for(int jj=0;jj<ImIn[0].length-8;jj=jj+8){
                for (int row=0;row<8;row++) {
                    System.arraycopy(blockLine[row], jj, block[row], 0, 8);
                    outArray[(ii-1)/8][(jj-1)/8]=BlockFun(block);
                }
            }
        }
        return outArray;
    }

    private int BlockFun(int[][] block){
        int[] rowSum=new int[6];
        int[] colSum=new int[6];
        int[] rowEdge=new int[2];
        int[] colEdge=new int[2];

        int max1, max2, min1, min2;
        for(int ii=1;ii<7;ii++){
            for (int jj=1;jj<7;jj++){
                rowSum[ii-1]=rowSum[ii-1]+block[ii][jj];
                colSum[jj-1]=colSum[jj-1]+block[ii][jj];
            }
            rowEdge[0]=rowEdge[0]+block[ii][0];
            rowEdge[1]=rowEdge[1]+block[ii][7];
        }
        for (int jj=1;jj<7;jj++){
            colEdge[0]=colEdge[0]+block[0][jj];
            colEdge[1]=colEdge[1]+block[7][jj];
        }
        max1=Collections.max(Arrays.asList(ArrayUtils.toObject(rowSum)));
        max2=Collections.max(Arrays.asList(ArrayUtils.toObject(colSum)));
        if (rowEdge[0]<rowEdge[1]) min1=rowEdge[0]; else min1=rowEdge[1];
        if (colEdge[0]<colEdge[1]) min2=colEdge[0]; else min2=colEdge[1];
        return max1 + max2 - min1 - min2;
    }
}
