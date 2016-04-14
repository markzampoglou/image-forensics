/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iti.mklab.reveal.forensics.maps.mediannoise;

// JAI-ImageIO is necessary to ensure TIFF files can be read. The current JAR version is for windows
// Replace the current divide-by-max approach to a divide by 20 or sth

import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.alg.misc.GPixelMath;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.core.image.ConvertImage;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageUInt16;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


/*
 * @author markzampoglou
 */
public class MedianNoiseExtractor {

    BufferedImage inputImage = null;
    public BufferedImage displaySurface = null;

    public MedianNoiseExtractor(String fileName) throws IOException {
        inputImage = ImageIO.read(new File(fileName));
        getNoiseMap();
    }

    public void getNoiseMap() throws IOException {

        ImageFloat32 original=new ImageFloat32(inputImage.getWidth(), inputImage.getHeight());
        ConvertBufferedImage.convertFrom(inputImage,original);

        //Temporarily commented out to compile
        ImageFloat32 test = new ImageFloat32(inputImage.getWidth(), inputImage.getHeight());
        ImageFloat32 medianFiltered=BlurImageOps.median(original, test, 1);

        ImageFloat32 noise=new ImageFloat32(inputImage.getWidth(), inputImage.getHeight());
        GPixelMath.diffAbs(original, medianFiltered, noise);
        //GPixelMath.multiply(noise, 5, noise);

        int histogram[] = new int[65536];
        int maxInd=0;
        int transform[] = new int[65536];
        ImageUInt16 noiseUInt16 = new ImageUInt16(inputImage.getWidth(), inputImage.getHeight());
        ImageUInt16 normNoiseUInt16 = new ImageUInt16(inputImage.getWidth(), inputImage.getHeight());
        GPixelMath.multiply(noise, 10, noise);


        ConvertImage.convert(noise, normNoiseUInt16);

        /*ImageStatistics.histogram(noiseUInt16, histogram);
        for (int ii=0;ii<histogram.length;ii++){
            if (histogram[ii]!=0) maxInd=ii;
        }

        float Ratio=65535/maxInd;

        for (int ii=0;ii<transform.length;ii++){
            transform[ii]=Math.round(ii*Ratio);
            if (transform[ii]>65535) transform[ii]=65535;
        }


        //EnhanceImageOps.equalize(histogram, transform);
        //EnhanceImageOps.equalizeLocal(noiseUInt16, 16, normNoiseUInt16, histogram, transform);

        EnhanceImageOps.applyTransform(noiseUInt16, transform, normNoiseUInt16);
        */
      /*  for (int ii=0;ii<transform.length;ii++){
            System.out.print(histogram[ii] + " ");
        }
        System.out.println();
        for (int ii=0;ii<transform.length;ii++){
            System.out.print(transform[ii] + " ");
        }
        */

        displaySurface =new BufferedImage(inputImage.getWidth(), inputImage.getHeight(),BufferedImage.TYPE_INT_RGB);
        ConvertBufferedImage.convertTo(normNoiseUInt16, displaySurface);

        //ImageIO.write(Output, "PNG", new File("/home/marzampoglou/Output.png"));
    }
}
