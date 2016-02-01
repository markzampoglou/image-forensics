/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maps.ghost;

import util.Util;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 *
 * @author markzampoglou
 */
public class JPEGGhostExtractor {

    public List<BufferedImage> GhostMaps = new ArrayList();
    public List<Integer> GhostQualities = new ArrayList();
    public List<Float> GhostMin = new ArrayList();
    public List<Float> GhostMax = new ArrayList();

    public List<Float> AllDifferences = new ArrayList();

    public int QualityMin = 65;
    public int QualityMax = 100;

    public List<BufferedImage> AllGhostMaps = new ArrayList();
    public List<Integer> AllGhostQualities = new ArrayList();
    public List<Float> AllGhostMin = new ArrayList();
    public List<Float> AllGhostMax = new ArrayList();


    private int MaxImageSmallDimension =768;
    private int numThreads=4;

    public JPEGGhostExtractor(String FileName, int MaxImageSmallDimension,int numThreads) throws IOException {
        this.MaxImageSmallDimension=MaxImageSmallDimension;
        this.numThreads=numThreads;
        GetJPEGGhost(FileName);
    }

    public JPEGGhostExtractor(String FileName) throws IOException {
        GetJPEGGhost(FileName);
    }



    private void GetJPEGGhost(String FileName) throws IOException {
        BufferedImage OrigImage;
        OrigImage = ImageIO.read(new File(FileName));
        int[][][] OrigByteImage = Util.GetRGBArray(OrigImage);
        BufferedImage RecompressedImage;
        int[][][] RecompressedByteImage = null;
        float[][][] ImageDifference;
        float[][][] Smooth;
        List<float[][]> DifferenceMaps = new ArrayList();
        float Differences[] = new float[QualityMax - QualityMin + 1];
        List<Integer> LocalMinima;
        BufferedImage JetImageDifference;
        
        float[][] MeanDifference;

        int NewHeight, NewWidth;
        float ScaleFactor;

        int ImageHeight=OrigImage.getHeight();
        int ImageWidth=OrigImage.getWidth();


        GhostCalculator calculator = new GhostCalculator(numThreads, MaxImageSmallDimension, OrigImage, OrigByteImage);
        int submittedCounter = 0;
        int completedCounter = 0;
        int failedCounter = 0;

        long start=System.currentTimeMillis();

        int totalGhosts=QualityMax-QualityMin+1;
        int currentQuality=QualityMin;


        GhostCalculationResult tmpGhostOutput;
        int tmpInd;
        float[][] tmpDifference;



        while (true) {
            // if there are more task to submit and the downloader can accept more tasks then submit

            while (submittedCounter < totalGhosts && calculator.canAcceptMoreTasks()) {
                calculator.submitGhostTask(currentQuality);
                //System.out.println("Added :" + String.valueOf(currentQuality));
                submittedCounter++;
                currentQuality++;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // if are submitted tasks that are pending completion ,try to consume
            if (completedCounter + failedCounter < submittedCounter) {
                //System.out.println("Pending tasks exist");
                try {
                    tmpGhostOutput=calculator.getGhostCalculationResult();
                    if (tmpGhostOutput!=null) {
                        //System.out.println("successfully consumed:  "+ String.valueOf(tmpGhostOutput.getQuality()));
                        completedCounter++;
                        tmpInd=tmpGhostOutput.getQuality()-QualityMin;
                        tmpDifference=tmpGhostOutput.getDifference();
                        DifferenceMaps.add(tmpDifference);
                        AllGhostMin.add(Util.MinDouble2DArray(tmpDifference));
                        AllGhostMax.add(Util.MaxDouble2DArray(tmpDifference));
                        Differences[tmpInd] = Util.SingleChannelMean(tmpDifference);
                    }
                    //System.out.println(completedCounter + " ghosts completed!");
                } catch (Exception e) {
                    failedCounter++;
                    System.out.println("Error thrown!");
                    System.out.println(e.getMessage());
                }
            }
            else {
                //System.out.println("No pending tasks: completed" + String.valueOf(completedCounter) + "failed" + String.valueOf(failedCounter) + String.valueOf(submittedCounter));
            }

            // if all tasks have been consumed then break;
            if (completedCounter + failedCounter == totalGhosts) {
                calculator.shutDown();
                //System.out.println("done");
                break;
            }

        }

        //System.out.println(System.currentTimeMillis()-start);

        /********************************************************************************************
        /*
        /*This code normalizes images by the pixel mean across different qualities, as suggested
        /*in the paper. However, it gives consistently worse results, so was dropped for now.
        /*

        NewWidth=DifferenceMaps.get(0).length;
        NewHeight=DifferenceMaps.get(0)[0].length;
        float[][] meanDifference = new float[NewWidth][NewHeight];
        for (int MapInd=0;MapInd<DifferenceMaps.size();MapInd++){
            System.out.println(DifferenceMaps.get(MapInd).length);
            System.out.println(DifferenceMaps.get(MapInd)[0].length);
            System.out.println(NewWidth);
            System.out.println(NewHeight);

            for (int ii=0;ii<NewWidth;ii++){
            for (int jj=0;jj<NewHeight;jj++) {
                meanDifference[ii][jj]=meanDifference[ii][jj]+DifferenceMaps.get(MapInd)[ii][jj];
            }
            }
        }
        tmpDifference = new float[NewWidth][NewHeight];
        for (int MapInd=0;MapInd<DifferenceMaps.size();MapInd++){
            tmpDifference=DifferenceMaps.get(MapInd);
            for (int ii=0;ii<NewWidth;ii++){
                for (int jj=0;jj<NewHeight;jj++) {
                    tmpDifference[ii][jj]=tmpDifference[ii][jj]/meanDifference[ii][jj];
                }
            }
            DifferenceMaps.set(MapInd, tmpDifference);
        }
        ***************************************************************************************/

        LocalMinima = Util.GetArrayLocalMinima(Differences);
        LocalMinima.add(Differences.length - 1); //Always add the difference from the 100% image


        for (Integer LocalMinima1 : LocalMinima) {
            GhostMaps.add(Util.VisualizeWithJet(Util.NormalizeIm(DifferenceMaps.get(LocalMinima1))));
            GhostMin.add(AllGhostMin.get(LocalMinima1));
            GhostMax.add(AllGhostMax.get(LocalMinima1));
            GhostQualities.add(LocalMinima1 + QualityMin);
            AllDifferences.add(Differences[LocalMinima1]);
        }


        /*
        adding all ghost maps for animated GIF

        for (Integer GhostInd=0;GhostInd<DifferenceMaps.size();GhostInd++) {
            
            AllGhostMaps.add(Util.VisualizeWithJet(Util.NormalizeIm(DifferenceMaps.get(GhostInd))));
            AllGhostQualities.add(GhostInd+QualityMin);
            AllDifferences.add(Differences[GhostInd]);
        }
        */
        //BufferedImage OutputImage = Util.CreateImFromArray(Smooth);


    }
}
