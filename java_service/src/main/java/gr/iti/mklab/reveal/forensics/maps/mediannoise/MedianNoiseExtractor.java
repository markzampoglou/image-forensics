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
import gr.iti.mklab.reveal.forensics.util.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


/*
 * @author markzampoglou
 */
public class MedianNoiseExtractor {

    BufferedImage inputImage = null;
    public BufferedImage displaySurface = null, displaySurface_temp = null;
    public int sc_width = 600;
    public int sc_height = 600;

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
        ImageUInt16 normNoiseUInt16 = new ImageUInt16(inputImage.getWidth(), inputImage.getHeight());
        GPixelMath.multiply(noise, 10, noise);
        ConvertImage.convert(noise, normNoiseUInt16);     
        displaySurface_temp =new BufferedImage(inputImage.getWidth(), inputImage.getHeight(),BufferedImage.TYPE_INT_RGB);
        ConvertBufferedImage.convertTo(normNoiseUInt16, displaySurface_temp);
        
        if (displaySurface_temp.getHeight() > displaySurface_temp.getWidth()){
			if (displaySurface_temp.getHeight() > sc_height){
				sc_width = (sc_height * displaySurface_temp.getWidth())/ displaySurface_temp.getHeight();
				displaySurface = Util.scaleImage(displaySurface_temp, sc_width, sc_height);
			}else{
				displaySurface = displaySurface_temp;
			}
		}else{
			if (displaySurface_temp.getWidth() > sc_width){
				sc_height = (sc_width * displaySurface_temp.getHeight())/ displaySurface_temp.getWidth(); 
				displaySurface = Util.scaleImage(displaySurface_temp, sc_width, sc_height);				
			}else{
				displaySurface = displaySurface_temp;
			}
		}
    }
}
