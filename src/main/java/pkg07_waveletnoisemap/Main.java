/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg07_waveletnoisemap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author markzampoglou
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //String InputImage = "canonxt_kodakdcs330_sub_01.tif";
        String InputImage = args[0];//"canonxt_kodakdcs330_sub_01.tif";
        BufferedImage ReceivedImage = null;
        try {
            NoiseMapExtractor noiseExtractor = new NoiseMapExtractor(InputImage);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
