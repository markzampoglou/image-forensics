/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg08_JPEGGhost;

import Utils.Util;
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
public class JPEGGhost {

    public List<BufferedImage> GhostMaps = new ArrayList();
    public List<Integer> GhostQualities = new ArrayList();
    public List<Double> GhostMin = new ArrayList();
    public List<Double> GhostMax = new ArrayList();

    public List<Double> AllDifferences = new ArrayList();

    public int QualityMin = 50;
    public int QualityMax = 100;

    public List<BufferedImage> AllGhostMaps = new ArrayList();
    public List<Integer> AllGhostQualities = new ArrayList();
    public List<Double> AllGhostMin = new ArrayList();
    public List<Double> AllGhostMax = new ArrayList();
    
    
    public JPEGGhost(String FileName) throws IOException {
        GetJPEGGhost(FileName);
    }

    private BufferedImage GetJPEGGhost(String FileName) throws IOException {

        BufferedImage OrigImage;
        OrigImage = ImageIO.read(new File(FileName));
        int[][][] OrigByteImage = Util.GetRGBArray(OrigImage);
        BufferedImage RecompressedImage;
        int[][][] RecompressedByteImage = null;
        double[][][] ImageDifference, Smooth;
        List<double[][]> DifferenceMaps = new ArrayList();
        double Differences[] = new double[QualityMax - QualityMin + 1];
        List<Integer> LocalMinima;

        
        
        double[][] MeanDifference;

        for (int Quality = QualityMin; Quality <= QualityMax; Quality++) {
            RecompressedImage = Util.RecompressImage(OrigImage, Quality);
            RecompressedByteImage = Util.GetRGBArray(RecompressedImage);
            ImageDifference = Util.CalculateImageDifference(OrigByteImage, RecompressedByteImage);
            Smooth = Util.MeanFilterRGB(ImageDifference, 17); //17 acc. to the paper
            MeanDifference = Util.MeanChannelImage(Smooth);
            DifferenceMaps.add(MeanDifference);
            AllGhostMin.add(Util.MinDouble2DArray(MeanDifference));
            AllGhostMax.add(Util.MaxDouble2DArray(MeanDifference));
            Differences[Quality - QualityMin] = Util.SingleChannelMean(DifferenceMaps.get(Quality - QualityMin));
        }
        LocalMinima = Util.GetArrayLocalMinima(Differences);
        LocalMinima.add(Differences.length - 1); //Always add the difference from the 100% image, as an ELA metric


        for (Integer LocalMinima1 : LocalMinima) {
            GhostMaps.add(Util.VisualizeWithJet(Util.NormalizeIm(DifferenceMaps.get(LocalMinima1))));
            GhostMin.add(AllGhostMin.get(LocalMinima1));
            GhostMax.add(AllGhostMax.get(LocalMinima1));
            GhostQualities.add(LocalMinima1 + QualityMin);
            AllDifferences.add(Differences[LocalMinima1]);
        }

        for (Integer GhostInd=0;GhostInd<DifferenceMaps.size();GhostInd++) {
            
            AllGhostMaps.add(Util.VisualizeWithJet(Util.NormalizeIm(DifferenceMaps.get(GhostInd))));
            AllGhostQualities.add(GhostInd+QualityMin);
            AllDifferences.add(Differences[GhostInd]);
        }
        
        
        //BufferedImage OutputImage = Util.CreateImFromArray(Smooth);
        return OrigImage;

    }
}
