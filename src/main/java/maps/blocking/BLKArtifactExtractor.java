package maps.blocking;

import util.Util;
import org.apache.commons.lang3.ArrayUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by marzampoglou on 11/24/15.
 */
public class BLKArtifactExtractor {

    public BufferedImage DisplaySurface = null;
    public double BLKMin;
    public double BLKMax;

    // This thresh is used to remove extremely strong edges:
    // block edges are definitely going to be weak
    int DiffThresh=50;
    // Accumulator size. Larger may overcome small splices, smaller may not
    // aggregate enough.
    int AC=33;

    public BLKArtifactExtractor(String FileName) throws IOException {
        GetBLKArtifact(FileName);
    }

    private void GetBLKArtifact(String FileName) throws IOException {

        BufferedImage OrigImage;
        OrigImage = ImageIO.read(new File(FileName));
        int[][][] OrigByteImage = Util.GetRGBArray(OrigImage);
        int[][] Y = new int[OrigByteImage[0].length][OrigByteImage[0][0].length];
        for (int ii = 0; ii < Y.length; ii++) {
            for (int jj = 0; jj < Y[0].length; jj++) {
                //Follow MATLAB's (ITU-R BT.601) conversion
                Y[ii][jj] = (int) Math.round((65.481 * ((float) OrigByteImage[0][ii][jj] / 255) + 128.553 * ((float) OrigByteImage[1][ii][jj] / 255) + 24.966 * ((float) OrigByteImage[2][ii][jj]) / 255));
            }
        }

        int[][] PaddedYX = Util.MirrorPadImage(Y, 0, (int) Math.floor(AC / 2));
        int[][] Im2DiffX = new int[PaddedYX.length][PaddedYX[0].length];
        for (int ii = 1; ii < PaddedYX.length - 1; ii++) {
            for (int jj = 0; jj < PaddedYX[0].length; jj++) {
                Im2DiffX[ii][jj] = Math.abs(2 * PaddedYX[ii][jj] - PaddedYX[ii + 1][jj] - PaddedYX[ii - 1][jj]);
                if (Math.abs(Im2DiffX[ii][jj]) > DiffThresh)
                    Im2DiffX[ii][jj] = DiffThresh;
            }
        }
        for (int jj = 0; jj < PaddedYX[0].length; jj++) {
            Im2DiffX[0][jj] = Im2DiffX[1][jj];
            Im2DiffX[Im2DiffX.length - 1][jj] = Im2DiffX[Im2DiffX.length - 2][jj];
        }
        int[][] summedV = Util.SumFilterSingleChanVert(Im2DiffX, AC);
        int[][] summedVPadded = Util.MirrorPadImage(summedV, (int) Math.floor(AC / 2),0);
        int[][] midV = Util.MedianFilterSingleChanHorz(summedVPadded, AC);
        int[][] eV = Util.SubtractImage(summedV, midV);
        int[][] PaddedeV = Util.MirrorPadImage(eV, 16,0);
        int[][] VertMid = new int[eV.length][eV[0].length];
        ArrayList<Integer> Step8;
        for (int ii = 16; ii < PaddedeV.length - 16; ii++) {
            for (int jj = 0; jj < PaddedeV[0].length; jj++) {
                Step8 = new ArrayList<>();
                for (int Step = -16; Step <= 16; Step += 8) {
                    Step8.add(PaddedeV[ii + Step][jj]);
                }
                Collections.sort(Step8);
                VertMid[ii - 16][jj] = Step8.get(3);
            }
        }

        int[][] PaddedYY = Util.MirrorPadImage(Y, (int) Math.floor(AC / 2),0);
        int[][] Im2DiffY = new int[PaddedYY.length][PaddedYY[0].length];
        for (int ii = 0; ii < PaddedYY.length; ii++) {
            for (int jj = 1; jj < PaddedYY[0].length-1; jj++) {
                Im2DiffY[ii][jj] = Math.abs(2 * PaddedYY[ii][jj] - PaddedYY[ii][jj+1] - PaddedYY[ii][jj-1]);
                if (Math.abs(Im2DiffY[ii][jj]) > DiffThresh)
                    Im2DiffY[ii][jj] = DiffThresh;
            }
        }
        for (int ii = 0; ii < PaddedYY.length; ii++) {
            Im2DiffY[ii][0] = Im2DiffY[ii][1];
            Im2DiffY[ii][Im2DiffY[0].length - 1] = Im2DiffY[ii][Im2DiffY[0].length - 2];
        }
        int[][] summedH = Util.SumFilterSingleChanHorz(Im2DiffY, AC);
        int[][] summedHPadded = Util.MirrorPadImage(summedH, 0, (int) Math.floor(AC / 2));
        int[][] midH = Util.MedianFilterSingleChanVert(summedHPadded, AC);
        int[][] eH = Util.SubtractImage(summedH, midH);
        int[][] PaddedeH = Util.MirrorPadImage(eH, 0, 16);
        int[][] HorzMid = new int[eH.length][eH[0].length];
        //ArrayList<Integer> Step8;
        for (int ii = 0; ii < PaddedeH.length; ii++) {
            for (int jj = 16; jj < PaddedeH[0].length-16; jj++) {
                Step8 = new ArrayList<>();
                for (int Step = -16; Step <= 16; Step += 8) {
                    Step8.add(PaddedeH[ii][jj+ Step]);
                }
                Collections.sort(Step8);
                HorzMid[ii][jj-16] = Step8.get(3);
            }
        }
        int[][] BlockDiff=Util.AddImage(HorzMid, VertMid);
        float[][] BLK=BlockProcess(BlockDiff);
        double[][] NormBLK=Util.NormalizeIm(BLK);
        DisplaySurface = Util.VisualizeWithJet(NormBLK);
        BLKMax=Util.MaxDouble2DArray(BLK);
        BLKMin=Util.MinDouble2DArray(BLK);
        /*
        SaveAnyImage(Im2DiffX,"a. Im2DiffX");
        SaveAnyImage(summedV,"b. summedV");
        SaveAnyImage(midV,"c. midV");
        SaveAnyImage(eV,"d. eV");
        SaveAnyImage(VertMid,"e. VertMid");
        SaveAnyImage(PaddedYY,"f. PaddedYY");
        SaveAnyImage(Im2DiffY,"g. Im2DiffY");
        SaveAnyImage(summedH,"h. summedH");
        SaveAnyImage(midH,"i. midH");
        SaveAnyImage(eH,"j. eH");
        SaveAnyImage(HorzMid,"k. HorzMid");
        SaveAnyImage(BlockDiff,"l. BlockDiff");
        SaveAnyImage(BLK,"m. BLK");
        */
        }

        private void SaveAnyImage(int[][] ImIn,String FileName){
        int minmin=100000;
        int maxmax=-100000;
        int[][] IntDispArray=new int[ImIn.length][ImIn[0].length];
        for (int ii=0; ii<IntDispArray.length; ii++) {
            for (int jj = 0; jj < IntDispArray[0].length; jj++) {
                IntDispArray[ii][jj]=ImIn[ii][jj];
            }
        }

        for (int ii=0; ii<IntDispArray.length; ii++) {
            for (int jj = 0; jj < IntDispArray[0].length; jj++) {
                if (IntDispArray[ii][jj]<minmin) {
                    minmin=IntDispArray[ii][jj];
                }
                if (IntDispArray[ii][jj]>maxmax) {
                    maxmax=IntDispArray[ii][jj];
                }
            }
        }

        for (int ii=0; ii<IntDispArray.length; ii++) {
            for (int jj = 0; jj < IntDispArray[0].length; jj++) {
                IntDispArray[ii][jj]=Math.round((IntDispArray[ii][jj]-(float)minmin)/((float)maxmax-minmin)*63);
                if (IntDispArray[ii][jj]<0){
                    IntDispArray[ii][jj]=63;
                }
            }
        }

        byte[][] DispArray=new byte[IntDispArray.length][IntDispArray[0].length];
        for (int ii=0; ii<IntDispArray.length; ii++) {
            for (int jj = 0; jj < IntDispArray[0].length; jj++) {
                DispArray[ii][jj]=(byte)IntDispArray[ii][jj];
            }
        }
        BufferedImage DispJet=Util.createJetVisualization(DispArray);
            try {
                ImageIO.write(DispJet,"PNG",new File("/home/marzampoglou/"+FileName + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    private float[][] BlockProcess (int[][] ImIn){
        float[][] OutArray= new float[(int)Math.ceil((ImIn.length-1)/8)][(int)Math.ceil((ImIn[0].length-1)/8)];
        int [][] BlockLine = new int[8][ImIn[0].length];
        int [][] Block= new int[8][8];
        for(int ii=0;ii<ImIn.length-8;ii=ii+8){
            System.arraycopy(ImIn,ii,BlockLine,0,8);
            for(int jj=0;jj<ImIn[0].length-8;jj=jj+8){
                for (int row=0;row<8;row++) {
                    System.arraycopy(BlockLine[row], jj, Block[row], 0, 8);
                    OutArray[(ii-1)/8][(jj-1)/8]=BlockFun(Block);
                }
            }
        }
        return OutArray;
    }

    private int BlockFun(int[][] Block){
        int[] RowSum=new int[6];
        int[] ColSum=new int[6];
        int[] RowEdge=new int[2];
        int[] ColEdge=new int[2];

        ArrayList tmp;
        int Max1, Max2, Min1, Min2;
        for(int ii=1;ii<7;ii++){
            for (int jj=1;jj<7;jj++){
                RowSum[ii-1]=RowSum[ii-1]+Block[ii][jj];
                ColSum[jj-1]=ColSum[jj-1]+Block[ii][jj];
            }
            RowEdge[0]=RowEdge[0]+Block[ii][0];
            RowEdge[1]=RowEdge[1]+Block[ii][7];
        }
        for (int jj=1;jj<7;jj++){
            ColEdge[0]=ColEdge[0]+Block[0][jj];
            ColEdge[1]=ColEdge[1]+Block[7][jj];
        }
        Max1=Collections.max(Arrays.asList(ArrayUtils.toObject(RowSum)));
        Max2=Collections.max(Arrays.asList(ArrayUtils.toObject(ColSum)));
        if (RowEdge[0]<RowEdge[1]) Min1=RowEdge[0]; else Min1=RowEdge[1];
        if (ColEdge[0]<ColEdge[1]) Min2=ColEdge[0]; else Min2=ColEdge[1];
        return Max1 + Max2 - Min1 - Min2;
    }
}
