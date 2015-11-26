/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg03_MedianDenoiseResidue;

// JAI-ImageIO is necessary to ensure TIFF files can be read. The current JAR version is for windows
// Replace the current divide-by-max approach to a divide by 20 or sth

import boofcv.alg.enhance.EnhanceImageOps;
import boofcv.alg.filter.blur.BlurImageOps;
import boofcv.alg.misc.GPixelMath;
import boofcv.alg.misc.ImageStatistics;
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

    BufferedImage InputImage = null;
    public BufferedImage DisplaySurface = null;

    public MedianNoiseExtractor(String FileName) throws IOException {
        InputImage = ImageIO.read(new File(FileName));
        GetNoiseMap();
    }

    public void GetNoiseMap() throws IOException {

        ImageFloat32 Original=new ImageFloat32(InputImage.getWidth(),InputImage.getHeight());
        ConvertBufferedImage.convertFrom(InputImage,Original);

        //Temporarily commented out to compile
        ImageFloat32 Test = new ImageFloat32(InputImage.getWidth(),InputImage.getHeight());
        ImageFloat32 MedianFiltered=BlurImageOps.median(Original, Test, 1);

        ImageFloat32 Noise=new ImageFloat32(InputImage.getWidth(),InputImage.getHeight());
        GPixelMath.diffAbs(Original, MedianFiltered, Noise);
        //GPixelMath.multiply(Noise, 5, Noise);

        int histogram[] = new int[65536];
        int MaxInd=0;
        int transform[] = new int[65536];
        ImageUInt16 NoiseUInt16 = new ImageUInt16(InputImage.getWidth(),InputImage.getHeight());
        ImageUInt16 NormNoiseUInt16 = new ImageUInt16(InputImage.getWidth(),InputImage.getHeight());
        GPixelMath.multiply(Noise, 10, Noise);


        ConvertImage.convert(Noise, NormNoiseUInt16);

        /*ImageStatistics.histogram(NoiseUInt16, histogram);
        for (int ii=0;ii<histogram.length;ii++){
            if (histogram[ii]!=0) MaxInd=ii;
        }

        float Ratio=65535/MaxInd;

        for (int ii=0;ii<transform.length;ii++){
            transform[ii]=Math.round(ii*Ratio);
            if (transform[ii]>65535) transform[ii]=65535;
        }


        //EnhanceImageOps.equalize(histogram, transform);
        //EnhanceImageOps.equalizeLocal(NoiseUInt16, 16, NormNoiseUInt16, histogram, transform);

        EnhanceImageOps.applyTransform(NoiseUInt16, transform, NormNoiseUInt16);
        */
      /*  for (int ii=0;ii<transform.length;ii++){
            System.out.print(histogram[ii] + " ");
        }
        System.out.println();
        for (int ii=0;ii<transform.length;ii++){
            System.out.print(transform[ii] + " ");
        }
        */

        DisplaySurface=new BufferedImage(InputImage.getWidth(),InputImage.getHeight(),BufferedImage.TYPE_INT_RGB);
        ConvertBufferedImage.convertTo(NormNoiseUInt16, DisplaySurface);

        //ImageIO.write(Output, "PNG", new File("/home/marzampoglou/Output.png"));
    }
}
