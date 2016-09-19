/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iti.mklab.reveal.forensics.maps.ghost;

import gr.iti.mklab.reveal.forensics.util.Util;

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
public class GhostExtractor {

    public List<BufferedImage> ghostMaps = new ArrayList();
    public List<Integer> ghostQualities = new ArrayList();
    public List<Float> ghostMin = new ArrayList();
    public List<Float> ghostMax = new ArrayList();

    public List<Float> allDifferences = new ArrayList();

    public int qualityMin = 65;
    public int qualityMax = 100;

    public List<BufferedImage> allGhostMaps = new ArrayList();
    public List<Integer> allGhostQualities = new ArrayList();
    public List<Float> allGhostMin = new ArrayList();
    public List<Float> allGhostMax = new ArrayList();


    private int maxImageSmallDimension =768;
    private int numThreads=4;

    public GhostExtractor(String fileName, int maxImageSmallDimension, int numThreads) throws IOException {
        this.maxImageSmallDimension =maxImageSmallDimension;
        this.numThreads=numThreads;
        getJPEGGhost(fileName);
    }

    public GhostExtractor(String fileName) throws IOException {
        getJPEGGhost(fileName);
    }



    private void getJPEGGhost(String fileName) throws IOException {
        BufferedImage origImage;
        origImage = ImageIO.read(new File(fileName));
        int[][][] origByteImage = Util.getRGBArray(origImage);
        BufferedImage recompressedImage;
        int[][][] recompressedByteImage = null;
        float[][][] imageDifference;
        float[][][] smooth;
        List<float[][]> differenceMaps = new ArrayList();
        float differences[] = new float[qualityMax - qualityMin + 1];
        List<Integer> localMinima;
        BufferedImage jetImageDifference;
        
        float[][] meanDifference;

        int newHeight, newWidth;
        float scaleFactor;

        int imageHeight=origImage.getHeight();
        int imageWidth=origImage.getWidth();


        GhostThreadManager calculator = new GhostThreadManager(numThreads, maxImageSmallDimension, origImage, origByteImage);
        int submittedCounter = 0;
        int completedCounter = 0;
        int failedCounter = 0;

        long start=System.currentTimeMillis();

        int totalGhosts= qualityMax - qualityMin +1;
        int currentQuality= qualityMin;


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
                        tmpInd=tmpGhostOutput.getQuality()- qualityMin;
                        tmpDifference=tmpGhostOutput.getDifference();
                        differenceMaps.add(tmpDifference);
                        allGhostMin.add(Util.minDouble2DArray(tmpDifference));
                        allGhostMax.add(Util.maxDouble2DArray(tmpDifference));
                        differences[tmpInd] = Util.SingleChannelMean(tmpDifference);
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

        newWidth=differenceMaps.get(0).length;
        newHeight=differenceMaps.get(0)[0].length;
        float[][] meanDifference = new float[newWidth][newHeight];
        for (int MapInd=0;MapInd<differenceMaps.size();MapInd++){
            System.out.println(differenceMaps.get(MapInd).length);
            System.out.println(differenceMaps.get(MapInd)[0].length);
            System.out.println(newWidth);
            System.out.println(newHeight);

            for (int ii=0;ii<newWidth;ii++){
            for (int jj=0;jj<newHeight;jj++) {
                meanDifference[ii][jj]=meanDifference[ii][jj]+differenceMaps.get(MapInd)[ii][jj];
            }
            }
        }
        tmpDifference = new float[newWidth][newHeight];
        for (int MapInd=0;MapInd<differenceMaps.size();MapInd++){
            tmpDifference=differenceMaps.get(MapInd);
            for (int ii=0;ii<newWidth;ii++){
                for (int jj=0;jj<newHeight;jj++) {
                    tmpDifference[ii][jj]=tmpDifference[ii][jj]/meanDifference[ii][jj];
                }
            }
            differenceMaps.set(MapInd, tmpDifference);
        }
        ***************************************************************************************/

        localMinima = Util.getArrayLocalMinima(differences);
        localMinima.add(differences.length - 1); //Always add the difference from the 100% image


        for (Integer localMinimum : localMinima) {
            ghostMaps.add(Util.visualizeWithJet(Util.normalizeIm(differenceMaps.get(localMinimum))));
            ghostMin.add(allGhostMin.get(localMinimum));
            ghostMax.add(allGhostMax.get(localMinimum));
            ghostQualities.add(localMinimum + qualityMin);
            allDifferences.add(differences[localMinimum]);
        }


        /*
        adding all ghost maps for animated GIF

        for (Integer GhostInd=0;GhostInd<differenceMaps.size();GhostInd++) {
            
            allGhostMaps.add(Util.visualizeWithJet(Util.normalizeIm(differenceMaps.get(GhostInd))));
            allGhostQualities.add(GhostInd+qualityMin);
            allDifferences.add(differences[GhostInd]);
        }
        */
        //BufferedImage OutputImage = Util.getBufferedIm(smooth);


    }
}
