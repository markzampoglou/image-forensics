package gr.iti.mklab.reveal.forensics.maps.copymove;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copy-move detection using the PatchMatch Algorithm
 *
 * | PatchMatch : A Randomized Correspondence Algorithm for Structural Image Editing
 * | by Connelly Barnes and Eli Shechtman and Adam Finkelstein and Dan B Goldman
 * | ACM Transactions on Graphics (Proc. SIGGRAPH), vol.28, aug-2009
 *
 * This is simply a modified version of the PatchMatch inpainting code by Xavier Philippeau
 * https://github.com/davidchatting/PatchMatch
 *
 *
 * Created by marzampoglou on 12/2/15.
 */

public class CloneDetect {
    //minimum allowed distance between matching patches is (image diagonal)/minDist
    int minDist=15;
    //number of loops per pyramid level for optimization
    int LoopMultiplier = 1;
    //number of Pyramid levels
    int pyramidLevels=3;

    //initial image
    ImageMap initial;

    // Nearest-Neighbor Field
    NNFNoMask nnf_TargetToSource;

    // patch radius
    int radius;

    // Pyramid of downsampled initial images
    List<ImageMap> pyramid;


    public BufferedImage findClones(BufferedImage input, int blockSize){
        ArrayList<NNFNoMask> nnfList = new ArrayList<>();

        // initial image
        this.initial = new ImageMap(input);

        // patch blockSize
        this.radius = blockSize;

        // working copies
        ImageMap source = initial;

        System.out.println("build pyramid of images...");

        // build pyramid of downscaled images
        this.pyramid = new ArrayList<ImageMap>();
        this.pyramid.add(source);
        //while(source.w>blockSize && source.h>blockSize) {
        for (int ii=0;ii<pyramidLevels-1;ii++) {
            source = source.downsample();
            this.pyramid.add(source);
            System.out.println("New level");
        }
        int maxLevel =this.pyramid.size();

        // The initial target is the same as the smallest source.
        // We consider that this target contains no masked pixels
        ImageMap target = source.copy();

        // for each level of the pyramid
        for(int level= maxLevel -1;level>=0;level--) {
            System.out.println("\n*** Processing -  Zoom 1:" + (1 << level) + " ***");
            // create Nearest-Neighbor Fields (direct and reverse)
            source = this.pyramid.get(level);
            target = source;

            System.out.println("initialize NNF...");
            if (level == maxLevel - 1) {
                // at first,  use random data as initial guess
                nnf_TargetToSource = new NNFNoMask(target, source, blockSize);
                nnf_TargetToSource.randomizeDist(minDist);
                NNFNoMask new_nnf = new NNFNoMask(target, source, blockSize);
                new_nnf.initialize(nnf_TargetToSource);
                new_nnf.minimizeDistance(LoopMultiplier *(level+ 1),minDist);
                nnf_TargetToSource=new_nnf;
            } else {
                // then, we use the rebuilt (upscaled) target
                // and reuse the previous NNF as initial guess
                //nnf_TargetToSource.randomize();
                NNFNoMask new_nnf = new NNFNoMask(target, source, blockSize);
                new_nnf.initialize(nnf_TargetToSource);
                new_nnf.minimizeDistance(LoopMultiplier *(level+ 1),minDist);
                nnf_TargetToSource = new_nnf;
            }
        }

        String output = "";

        try {
            File file = new File("/home/marzampoglou/Desktop/X.txt");

            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (int ii = 0; ii < nnf_TargetToSource.getField().length; ii++) {
                for (int jj = 0; jj < nnf_TargetToSource.getField()[0].length; jj++) {
                    output=output+nnf_TargetToSource.getField()[ii][jj][0] + " ";
                }
                output=output+"\n";
            }
            bw.write(output);
            bw.close();


            output="";
            file = new File("/home/marzampoglou/Desktop/Y.txt");
            fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);
            if (!file.exists()) {
                file.createNewFile();
            }

            for (int ii = 0; ii < nnf_TargetToSource.getField().length; ii++) {
                for (int jj = 0; jj < nnf_TargetToSource.getField()[0].length; jj++) {
                    output=output+nnf_TargetToSource.getField()[ii][jj][1] + " ";
                }
                output=output+"\n";
            }
            bw.write(output);
            bw.close();


            output="";
            file = new File("/home/marzampoglou/Desktop/Err.txt");
            fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);
            for (int ii = 0; ii < nnf_TargetToSource.getField().length; ii++) {
                for (int jj = 0; jj < nnf_TargetToSource.getField()[0].length; jj++) {
                    output=output+nnf_TargetToSource.getField()[ii][jj][2] + " ";
                }
                output=output+"\n";
            }
            bw.write(output);
            bw.close();

        }
        catch (Exception e){
            e.printStackTrace();
        }
        /*
        */

        BufferedImage out= drawGrey(nnf_TargetToSource.linearFilterField());
        nnf_TargetToSource.removeLowVar(7,2.5);

        //display(output);
        try {
            ImageIO.write(out, "PNG", new File("/home/marzampoglou/Pictures/Reveal/ManipulationOutput/linearfilt.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return drawGrey2(nnf_TargetToSource.getField());
    }

    private BufferedImage drawGrey(int[][] mapIn){
        BufferedImage imOut = new BufferedImage(mapIn.length,mapIn[0].length, BufferedImage.TYPE_INT_ARGB) ;
        for (int ii=0;ii<mapIn.length;ii++){
            for (int jj=0;jj<mapIn[0].length;jj++){
                //int tmpVal= (int) Math.round(Math.sqrt(Math.pow(ii-mapIn[ii][jj][0],2)+Math.pow(jj-mapIn[ii][jj][1],2)));
                int tmpVal= mapIn[ii][jj]; //[2]
                if (tmpVal>255) tmpVal=255;
                int value = 0xFF000000 | tmpVal << 16 | tmpVal << 8 | tmpVal;
                imOut.setRGB(ii, jj, value);
                //System.out.print(tmpVal + " ");
            }
            //System.out.println();
        }
        return imOut;
    }

    private BufferedImage drawGrey2(int[][][] mapIn){
        BufferedImage imOut = new BufferedImage(mapIn.length,mapIn[0].length, BufferedImage.TYPE_INT_ARGB) ;
        for (int ii=0;ii<mapIn.length;ii++){
            for (int jj=0;jj<mapIn[0].length;jj++){
                //int tmpVal= (int) Math.round(Math.sqrt(Math.pow(ii-mapIn[ii][jj][0],2)+Math.pow(jj-mapIn[ii][jj][1],2)));
                int tmpVal= mapIn[ii][jj][2];
                if (tmpVal>255) tmpVal=255;
                int value = 0xFF000000 | tmpVal << 16 | tmpVal << 8 | tmpVal;
                imOut.setRGB(ii, jj, value);
                //System.out.print(tmpVal + " ");
            }
            //System.out.println();
        }
        return imOut;
    }


    }



