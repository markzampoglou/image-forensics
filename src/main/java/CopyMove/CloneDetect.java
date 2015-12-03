package CopyMove;

import com.sun.javafx.iio.ImageStorage;

import javax.imageio.ImageIO;
import javax.lang.model.element.VariableElement;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
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
    //minimum allowed distance between patches is (image diagonal)/minDist
    int minDist=10;
    //number of loops for optimization
    int numLoops=1;
    //number of Pyramid levels
    int pyramidLevels=4;

    //initial image
    ImageMap initial;

    // Nearest-Neighbor Field
    NNF_NoMask nnf_TargetToSource;

    // patch radius
    int radius;

    // Pyramid of downsampled initial images
    List<ImageMap> pyramid;


    public BufferedImage FindClones(BufferedImage input, int blockSize){
        ArrayList<NNF_NoMask> NNFList = new ArrayList<>();

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
        //while(source.W>blockSize && source.H>blockSize) {
        for (int ii=0;ii<pyramidLevels-1;ii++) {
            source = source.downsample();
            this.pyramid.add(source);
            System.out.println("New level");
        }
        int maxlevel=this.pyramid.size();

        // The initial target is the same as the smallest source.
        // We consider that this target contains no masked pixels
        ImageMap target = source.copy();

        // for each level of the pyramid
        for(int level=maxlevel-1;level>=0;level--) {
            System.out.println("\n*** Processing -  Zoom 1:" + (1 << level) + " ***");
            // create Nearest-Neighbor Fields (direct and reverse)
            source = this.pyramid.get(level);
            target = source;

            System.out.println("initialize NNF...");
            if (level == maxlevel - 1) {
                // at first,  use random data as initial guess
                nnf_TargetToSource = new NNF_NoMask(target, source, blockSize);
                nnf_TargetToSource.randomizeDist(minDist);
                NNF_NoMask new_nnf = new NNF_NoMask(target, source, blockSize);
                new_nnf.initialize(nnf_TargetToSource);
                new_nnf.minimizeDistance(numLoops*(level+1), minDist);
                nnf_TargetToSource=new_nnf;
            } else {
                // then, we use the rebuilt (upscaled) target
                // and reuse the previous NNF as initial guess
                //nnf_TargetToSource.randomize();
                NNF_NoMask new_nnf = new NNF_NoMask(target, source, blockSize);
                new_nnf.initialize(nnf_TargetToSource);
                new_nnf.minimizeDistance(numLoops*(level+1), minDist);
                nnf_TargetToSource = new_nnf;
            }
        }

        double[][] varMap=source.LocalVariance(blockSize);


        for (int ii=0;ii<nnf_TargetToSource.getField().length;ii++){
            for (int jj=0;jj<nnf_TargetToSource.getField()[0].length;jj++){
                System.out.print(nnf_TargetToSource.getField()[ii][jj][0]+" ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
        for (int ii=0;ii<nnf_TargetToSource.getField().length;ii++){
            for (int jj=0;jj<nnf_TargetToSource.getField()[0].length;jj++){
                System.out.print(nnf_TargetToSource.getField()[ii][jj][1]+" ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
        for (int ii=0;ii<nnf_TargetToSource.getField().length;ii++){
            for (int jj=0;jj<nnf_TargetToSource.getField()[0].length;jj++){
                System.out.print(nnf_TargetToSource.getField()[ii][jj][2]+" ");
            }
            System.out.println();
        }



        return DrawGrey(nnf_TargetToSource.getField());
    }

    private BufferedImage DrawGrey(int[][][] MapIn){
        System.out.println("------------------------");
        BufferedImage ImOut = new BufferedImage(MapIn.length,MapIn[0].length, BufferedImage.TYPE_BYTE_GRAY) ;
        for (int ii=0;ii<MapIn.length;ii++){
            for (int jj=0;jj<MapIn[0].length;jj++){
                //int tmpVal= (int) Math.round(Math.sqrt(Math.pow(ii-MapIn[ii][jj][0],2)+Math.pow(jj-MapIn[ii][jj][1],2)));
                int tmpVal= MapIn[ii][jj][2];
                if (tmpVal>255) tmpVal=255;
                int value = tmpVal << 16 | tmpVal << 8 | tmpVal;
                ImOut.setRGB(ii,jj,value);
                System.out.print(tmpVal + " ");
            }
            System.out.println();
        }
        return ImOut;
    }


    }



