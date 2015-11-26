/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ForensicsToolbox;

//import Utils.GifSequenceWriter;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
//import org.apache.commons.io.FileUtils;
import com.google.gson.JsonObject;
import metadataExtraction.metadataExtractor;
import pkg01_DoubleQuantization.DQDetector;
import pkg06_ELA.JPEGELAExtractor;
import pkg07_waveletnoisemap.NoiseMapExtractor;
import pkg08_JPEGGhost.JPEGGhostExtractor;

/**
 *
 * @author markzampoglou
 */
public class ToolboxAPI {

    public static GhostAnalysis getImageGhost(String ImageLocation, String OutputPath) throws MalformedURLException, IOException {
        GhostAnalysis AnalysisResult = new GhostAnalysis();

        URL ImageURL = new URL(ImageLocation);
        File LocalDir = new File(OutputPath);

        File ImageFile = File.createTempFile("REVEAL_", null, LocalDir);
        //FileUtils.copyURLToFile(ImageURL, ImageFile);

        InputStream inputStream = null;
        URLConnection urlConnection = null;
        int noOfBytes = 0;
        byte[] byteChunk = new byte[4096];
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        urlConnection = ImageURL.openConnection();
        urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        // We need to set cookies as below.
        //urlConnection.addRequestProperty("Cookie", _mSharePointSession.cookieNedToken);

        urlConnection.connect();

        inputStream = urlConnection.getInputStream();

        while ((noOfBytes = inputStream.read(byteChunk)) > 0) {
            byteOutputStream.write(byteChunk, 0, noOfBytes);
        }
        OutputStream outputStream = new FileOutputStream(ImageFile);
        byteOutputStream.writeTo(outputStream);

        outputStream.close();
        ImageFile.deleteOnExit();
        //System.out.println(ImageFile.getCanonicalPath());

        //String imageFormat = Utils.Util.GetImageFormat(ImageFile);
        String InputFileName = ImageFile.getCanonicalPath();

        /*
         String[] Input1 = {ImageFile.getCanonicalPath()};
         String[] Input2 = {ImageFile.getCanonicalPath()};
         String[] Input3 = {ImageFile.getCanonicalPath()};
         */
        JPEGGhostExtractor ImageGhosts;

        if (ImageIO.read(new File(InputFileName)).getColorModel().hasAlpha()) {
            File ghostOutputfile = File.createTempFile("REVEAL_", ".png", LocalDir);
            ImageIO.write(TransparentPNGNotAccepted(), "png", ghostOutputfile);
            AnalysisResult.GhostOutput.add(ghostOutputfile.getCanonicalPath());
            AnalysisResult.GhostDifferences.add((float) 0.0);
            AnalysisResult.GhostMinQuality = 0;
            AnalysisResult.GhostMaxQuality = 0;
            AnalysisResult.GhostQualities.add(0);
            AnalysisResult.Ghost_MinValues.add((float) 0.0);
            AnalysisResult.Ghost_MaxValues.add((float) 0.0);
                // Gif writing disabled for now - server too slow                
            //File ghostOutputGIFfile = File.createTempFile("REVEAL_", ".gif", LocalDir);
            //ImageIO.write(TransparentPNGNotAccepted(), "gif", ghostOutputGIFfile);
            //AnalysisResult.GhostGIFOutput=ghostOutputGIFfile.getCanonicalPath();
            return AnalysisResult;
        }

        try {
            ImageGhosts = new JPEGGhostExtractor(InputFileName);
            try {
                for (BufferedImage GhostMap : ImageGhosts.GhostMaps) {
                    File ghostOutputfile = File.createTempFile("REVEAL_", ".png", LocalDir);
                    ImageIO.write(GhostMap, "png", ghostOutputfile);
                    AnalysisResult.GhostOutput.add(ghostOutputfile.getCanonicalPath());
                }
                AnalysisResult.GhostDifferences = ImageGhosts.AllDifferences;
                AnalysisResult.GhostMinQuality = ImageGhosts.QualityMin;
                AnalysisResult.GhostMaxQuality = ImageGhosts.QualityMax;
                AnalysisResult.GhostQualities = ImageGhosts.GhostQualities;
                AnalysisResult.Ghost_MinValues = ImageGhosts.GhostMin;
                AnalysisResult.Ghost_MaxValues = ImageGhosts.GhostMax;
                // Gif writing disabled for now - server too slow
                //File GhostAnimGIF = File.createTempFile("REVEAL_", ".gif", LocalDir);
                //GifSequenceWriter.writeFromList(Utils.Util.AddJPEGQualitiesToList(ImageGhosts.AllGhostMaps,ImageGhosts.AllGhostQualities,ImageGhosts.AllGhostMin,ImageGhosts.AllGhostMax, ImageGhosts.GhostQualities), GhostAnimGIF,200);
                //AnalysisResult.GhostGIFOutput=GhostAnimGIF.getCanonicalPath();
            } catch (IOException ex) {
                Logger.getLogger(pkg07_waveletnoisemap.Main.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (Throwable e) {
            Logger.getLogger(ToolboxAPI.class.getName()).log(Level.SEVERE, null, e);
        }

        return AnalysisResult;
    }


    public static DQAnalysis getImageDQ(String ImageLocation, String OutputPath) throws MalformedURLException, IOException {

        DQAnalysis AnalysisResult = new DQAnalysis();

        URL ImageURL = new URL(ImageLocation);
        File LocalDir = new File(OutputPath);

        File ImageFile = File.createTempFile("REVEAL_", null, LocalDir);
        //FileUtils.copyURLToFile(ImageURL, ImageFile);

        InputStream inputStream = null;
        URLConnection urlConnection = null;
        int noOfBytes = 0;
        byte[] byteChunk = new byte[4096];
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        urlConnection = ImageURL.openConnection();
        urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        // We need to set cookies as below.
        //urlConnection.addRequestProperty("Cookie", _mSharePointSession.cookieNedToken);

        urlConnection.connect();

        inputStream = urlConnection.getInputStream();

        while ((noOfBytes = inputStream.read(byteChunk)) > 0) {
            byteOutputStream.write(byteChunk, 0, noOfBytes);
        }
        OutputStream outputStream = new FileOutputStream(ImageFile);
        byteOutputStream.writeTo(outputStream);

        outputStream.close();
        ImageFile.deleteOnExit();
        //System.out.println(ImageFile.getCanonicalPath());

        String imageFormat = Utils.Util.GetImageFormat(ImageFile);
        String InputFileName = ImageFile.getCanonicalPath();

        /*
         String[] Input1 = {ImageFile.getCanonicalPath()};
         String[] Input2 = {ImageFile.getCanonicalPath()};
         String[] Input3 = {ImageFile.getCanonicalPath()};
         */
        DQDetector dqDetector;
        NoiseMapExtractor noiseExtractor;
        JPEGGhostExtractor ImageGhosts;

        if (ImageIO.read(new File(InputFileName)).getColorModel().hasAlpha()) {
            File dqOutputfile = File.createTempFile("REVEAL_", ".png", LocalDir);
            ImageIO.write(TransparentPNGNotAccepted(), "png", dqOutputfile);
            AnalysisResult.DQ_Lin_Output = dqOutputfile.getCanonicalPath();
            AnalysisResult.DQ_Lin_MaxValue = 0;
            AnalysisResult.DQ_Lin_MinValue = 0;
            return AnalysisResult;
        }

        if (imageFormat.equalsIgnoreCase("JPEG") | imageFormat.equalsIgnoreCase("JPG")) { // (false) { //
            try {
                dqDetector = new DQDetector(InputFileName);
                try {
                    File dqOutputfile = File.createTempFile("REVEAL_", ".png", LocalDir);
                    ImageIO.write(dqDetector.DisplaySurface, "png", dqOutputfile);
                    AnalysisResult.DQ_Lin_Output = dqOutputfile.getCanonicalPath();
                    AnalysisResult.DQ_Lin_MaxValue = dqDetector.maxProbValue;
                    AnalysisResult.DQ_Lin_MinValue = dqDetector.minProbValue;
                } catch (IOException ex) {
                    Logger.getLogger(pkg07_waveletnoisemap.Main.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (Throwable e) {
                System.out.println("DCT-based analysis failed with:");
                System.out.println(e.getMessage());
                if (e.getMessage().startsWith("The specified module could not be found.")) {
                    System.out.println("(are the native .dll/.so libraries in place?)");
                }
                Logger.getLogger(ToolboxAPI.class.getName()).log(Level.SEVERE, null, e);
            }
        } else {
            File dqOutputfile = File.createTempFile("REVEAL_", ".png", LocalDir);
            ImageIO.write(NotAJPEG(), "png", dqOutputfile);
            AnalysisResult.DQ_Lin_Output = dqOutputfile.getCanonicalPath();
            AnalysisResult.DQ_Lin_MaxValue = 0;
            AnalysisResult.DQ_Lin_MinValue = 0;
        }

        return AnalysisResult;

    }

    public static NoiseMahdianAnalysis getImageMahdianNoise(String ImageLocation, String OutputPath) throws MalformedURLException, IOException {

        NoiseMahdianAnalysis AnalysisResult = new NoiseMahdianAnalysis();

        URL ImageURL = new URL(ImageLocation);
        File LocalDir = new File(OutputPath);

        File ImageFile = File.createTempFile("REVEAL_", null, LocalDir);
        //FileUtils.copyURLToFile(ImageURL, ImageFile);

        InputStream inputStream = null;
        URLConnection urlConnection = null;
        int noOfBytes = 0;
        byte[] byteChunk = new byte[4096];
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        urlConnection = ImageURL.openConnection();
        urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        // We need to set cookies as below.
        //urlConnection.addRequestProperty("Cookie", _mSharePointSession.cookieNedToken);

        urlConnection.connect();

        inputStream = urlConnection.getInputStream();

        while ((noOfBytes = inputStream.read(byteChunk)) > 0) {
            byteOutputStream.write(byteChunk, 0, noOfBytes);
        }
        OutputStream outputStream = new FileOutputStream(ImageFile);
        byteOutputStream.writeTo(outputStream);

        outputStream.close();
        ImageFile.deleteOnExit();
        //System.out.println(ImageFile.getCanonicalPath());

        String imageFormat = Utils.Util.GetImageFormat(ImageFile);
        String InputFileName = ImageFile.getCanonicalPath();

        /*
         String[] Input1 = {ImageFile.getCanonicalPath()};
         String[] Input2 = {ImageFile.getCanonicalPath()};
         String[] Input3 = {ImageFile.getCanonicalPath()};
         */
        NoiseMapExtractor noiseExtractor;

        if (ImageIO.read(new File(InputFileName)).getColorModel().hasAlpha()) {
            File noiseOutputfile = File.createTempFile("REVEAL_", ".png", LocalDir);
            ImageIO.write(TransparentPNGNotAccepted(), "png", noiseOutputfile);
            AnalysisResult.Noise_Mahdian_Output = noiseOutputfile.getCanonicalPath();
            AnalysisResult.Noise_Mahdian_MaxValue = 0;
            AnalysisResult.Noise_Mahdian_MinValue = 0;

            return AnalysisResult;
        }

        try {
            noiseExtractor = new NoiseMapExtractor(InputFileName);
            try {
                File noiseOutputfile = File.createTempFile("REVEAL_", ".png", LocalDir);
                ImageIO.write(noiseExtractor.DisplaySurface, "png", noiseOutputfile);
                AnalysisResult.Noise_Mahdian_Output = noiseOutputfile.getCanonicalPath();
                AnalysisResult.Noise_Mahdian_MaxValue = noiseExtractor.maxNoiseValue;
                AnalysisResult.Noise_Mahdian_MinValue = noiseExtractor.minNoiseValue;
            } catch (IOException ex) {
                Logger.getLogger(pkg07_waveletnoisemap.Main.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (Throwable e) {
            Logger.getLogger(ToolboxAPI.class.getName()).log(Level.SEVERE, null, e);
        }
        return AnalysisResult;
    }

    public static ELAAnalysis getImageJPEGELA(String ImageLocation, String OutputPath) throws MalformedURLException, IOException {

        ELAAnalysis AnalysisResult = new ELAAnalysis();

        URL ImageURL = new URL(ImageLocation);
        File LocalDir = new File(OutputPath);

        File ImageFile = File.createTempFile("REVEAL_", null, LocalDir);
        //FileUtils.copyURLToFile(ImageURL, ImageFile);

        InputStream inputStream = null;
        URLConnection urlConnection = null;
        int noOfBytes = 0;
        byte[] byteChunk = new byte[4096];
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        urlConnection = ImageURL.openConnection();
        urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        // We need to set cookies as below.
        //urlConnection.addRequestProperty("Cookie", _mSharePointSession.cookieNedToken);

        urlConnection.connect();

        inputStream = urlConnection.getInputStream();

        while ((noOfBytes = inputStream.read(byteChunk)) > 0) {
            byteOutputStream.write(byteChunk, 0, noOfBytes);
        }
        OutputStream outputStream = new FileOutputStream(ImageFile);
        byteOutputStream.writeTo(outputStream);

        outputStream.close();
        ImageFile.deleteOnExit();

        String InputFileName = ImageFile.getCanonicalPath();

        JPEGELAExtractor ELAExtractor;

        if (ImageIO.read(new File(InputFileName)).getColorModel().hasAlpha()) {
            File ELAOutputfile = File.createTempFile("REVEAL_", ".png", LocalDir);
            ImageIO.write(TransparentPNGNotAccepted(), "png", ELAOutputfile);
            AnalysisResult.ELA_Output = ELAOutputfile.getCanonicalPath();
            AnalysisResult.ELA_MaxValue = 0;
            AnalysisResult.ELA_MinValue = 0;

            return AnalysisResult;
        }

        try {
            ELAExtractor = new JPEGELAExtractor(InputFileName);

                File ELAOutputfile = File.createTempFile("REVEAL_", ".png", LocalDir);
                ImageIO.write(ELAExtractor.DisplaySurface, "png", ELAOutputfile);
                AnalysisResult.ELA_Output = ELAOutputfile.getCanonicalPath();
                AnalysisResult.ELA_MaxValue = ELAExtractor.ELAMax;
                AnalysisResult.ELA_MinValue = ELAExtractor.ELAMin;

        } catch (Throwable e) {
            Logger.getLogger(ToolboxAPI.class.getName()).log(Level.SEVERE, null, e);
        }
        return AnalysisResult;
    }



    public static ForensicAnalysis analyzeImage(String ImageLocation, String OutputPath) throws MalformedURLException, IOException {

        ForensicAnalysis AnalysisResult = new ForensicAnalysis();

        URL ImageURL = new URL(ImageLocation);
        File LocalDir = new File(OutputPath);

        File ImageFile = File.createTempFile("REVEAL_", null, LocalDir);
        //FileUtils.copyURLToFile(ImageURL, ImageFile);

        InputStream inputStream = null;
        URLConnection urlConnection = null;
        int noOfBytes = 0;
        byte[] byteChunk = new byte[4096];
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        urlConnection = ImageURL.openConnection();
        urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        // We need to set cookies as below.
        //urlConnection.addRequestProperty("Cookie", _mSharePointSession.cookieNedToken);

        urlConnection.connect();

        inputStream = urlConnection.getInputStream();

        while ((noOfBytes = inputStream.read(byteChunk)) > 0) {
            byteOutputStream.write(byteChunk, 0, noOfBytes);
        }
        OutputStream outputStream = new FileOutputStream(ImageFile);
        byteOutputStream.writeTo(outputStream);

        outputStream.close();
        ImageFile.deleteOnExit();
        //System.out.println(ImageFile.getCanonicalPath());

        String imageFormat = Utils.Util.GetImageFormat(ImageFile);
        String InputFileName = ImageFile.getCanonicalPath();

        /*
         String[] Input1 = {ImageFile.getCanonicalPath()};
         String[] Input2 = {ImageFile.getCanonicalPath()};
         String[] Input3 = {ImageFile.getCanonicalPath()};
         */
        DQDetector dqDetector;
        NoiseMapExtractor noiseExtractor;
        JPEGGhostExtractor ImageGhosts;

        if (ImageIO.read(new File(InputFileName)).getColorModel().hasAlpha()) {
            File dqOutputfile = File.createTempFile("REVEAL_", ".png", LocalDir);
            ImageIO.write(TransparentPNGNotAccepted(), "png", dqOutputfile);
            AnalysisResult.DQ_Lin_Output = dqOutputfile.getCanonicalPath();
            AnalysisResult.DQ_Lin_MaxValue = 0;
            AnalysisResult.DQ_Lin_MinValue = 0;
            File noiseOutputfile = File.createTempFile("REVEAL_", ".png", LocalDir);
            ImageIO.write(TransparentPNGNotAccepted(), "png", noiseOutputfile);
            AnalysisResult.Noise_Mahdian_Output = noiseOutputfile.getCanonicalPath();
            AnalysisResult.Noise_Mahdian_MaxValue = 0;
            AnalysisResult.Noise_Mahdian_MinValue = 0;

            File ghostOutputfile = File.createTempFile("REVEAL_", ".png", LocalDir);
            ImageIO.write(TransparentPNGNotAccepted(), "png", ghostOutputfile);
            AnalysisResult.GhostOutput.add(ghostOutputfile.getCanonicalPath());
            AnalysisResult.GhostDifferences.add((float) 0.0);
            AnalysisResult.GhostMinQuality = 0;
            AnalysisResult.GhostMaxQuality = 0;
            AnalysisResult.GhostQualities.add(0);
            AnalysisResult.Ghost_MinValues.add((float) 0.0);
            AnalysisResult.Ghost_MaxValues.add((float) 0.0);

                //File ghostOutputGIFfile = File.createTempFile("REVEAL_", ".gif", LocalDir);
            //ImageIO.write(TransparentPNGNotAccepted(), "gif", ghostOutputGIFfile);
            //AnalysisResult.GhostGIFOutput=ghostOutputGIFfile.getCanonicalPath();
            return AnalysisResult;
        }

        if (imageFormat.equalsIgnoreCase("JPEG") | imageFormat.equalsIgnoreCase("JPG")) { // (false) { //
            try {
                dqDetector = new DQDetector(InputFileName);
                try {
                    File dqOutputfile = File.createTempFile("REVEAL_", ".png", LocalDir);
                    ImageIO.write(dqDetector.DisplaySurface, "png", dqOutputfile);
                    AnalysisResult.DQ_Lin_Output = dqOutputfile.getCanonicalPath();
                    AnalysisResult.DQ_Lin_MaxValue = dqDetector.maxProbValue;
                    AnalysisResult.DQ_Lin_MinValue = dqDetector.minProbValue;
                } catch (IOException ex) {
                    Logger.getLogger(pkg07_waveletnoisemap.Main.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (Throwable e) {
                System.out.println("DCT-based analysis failed with:");
                System.out.println(e.getMessage());
                if (e.getMessage().startsWith("The specified module could not be found.")) {
                    System.out.println("(are the native .dll/.so libraries in place?)");
                }
                Logger.getLogger(ToolboxAPI.class.getName()).log(Level.SEVERE, null, e);
            }
        } else {
            File dqOutputfile = File.createTempFile("REVEAL_", ".png", LocalDir);
            ImageIO.write(NotAJPEG(), "png", dqOutputfile);
            AnalysisResult.DQ_Lin_Output = dqOutputfile.getCanonicalPath();
            AnalysisResult.DQ_Lin_MaxValue = 0;
            AnalysisResult.DQ_Lin_MinValue = 0;
        }

        try {
            noiseExtractor = new NoiseMapExtractor(InputFileName);
            try {
                File noiseOutputfile = File.createTempFile("REVEAL_", ".png", LocalDir);
                ImageIO.write(noiseExtractor.DisplaySurface, "png", noiseOutputfile);
                AnalysisResult.Noise_Mahdian_Output = noiseOutputfile.getCanonicalPath();
                AnalysisResult.Noise_Mahdian_MaxValue = noiseExtractor.maxNoiseValue;
                AnalysisResult.Noise_Mahdian_MinValue = noiseExtractor.minNoiseValue;
            } catch (IOException ex) {
                Logger.getLogger(pkg07_waveletnoisemap.Main.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (Throwable e) {
            Logger.getLogger(ToolboxAPI.class.getName()).log(Level.SEVERE, null, e);
        }

        try {
            ImageGhosts = new JPEGGhostExtractor(InputFileName);
            try {
                for (BufferedImage GhostMap : ImageGhosts.GhostMaps) {
                    File ghostOutputfile = File.createTempFile("REVEAL_", ".png", LocalDir);
                    ImageIO.write(GhostMap, "png", ghostOutputfile);
                    AnalysisResult.GhostOutput.add(ghostOutputfile.getCanonicalPath());
                }
                AnalysisResult.GhostDifferences = ImageGhosts.AllDifferences;
                AnalysisResult.GhostMinQuality = ImageGhosts.QualityMin;
                AnalysisResult.GhostMaxQuality = ImageGhosts.QualityMax;
                AnalysisResult.GhostQualities = ImageGhosts.GhostQualities;
                AnalysisResult.Ghost_MinValues = ImageGhosts.GhostMin;
                AnalysisResult.Ghost_MaxValues = ImageGhosts.GhostMax;
                //File GhostAnimGIF = File.createTempFile("REVEAL_", ".gif", LocalDir);
                //GifSequenceWriter.writeFromList(Utils.Util.AddJPEGQualitiesToList(ImageGhosts.AllGhostMaps,ImageGhosts.AllGhostQualities,ImageGhosts.AllGhostMin,ImageGhosts.AllGhostMax, ImageGhosts.GhostQualities), GhostAnimGIF,200);
                //AnalysisResult.GhostGIFOutput=GhostAnimGIF.getCanonicalPath();
            } catch (IOException ex) {
                Logger.getLogger(pkg07_waveletnoisemap.Main.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (Throwable e) {
            Logger.getLogger(ToolboxAPI.class.getName()).log(Level.SEVERE, null, e);
        }

        return AnalysisResult;
    }

    public static JsonObject getImageMetadata(String ImageLocation, String OutputPath) throws MalformedURLException, IOException {

        JsonObject MetadataOutput=null;

        URL ImageURL = new URL(ImageLocation);
        File LocalDir = new File(OutputPath);

        File ImageFile = File.createTempFile("REVEAL_", null, LocalDir);
        //FileUtils.copyURLToFile(ImageURL, ImageFile);

        InputStream inputStream = null;
        URLConnection urlConnection = null;
        int noOfBytes = 0;
        byte[] byteChunk = new byte[4096];
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        urlConnection = ImageURL.openConnection();
        urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        // We need to set cookies as below.
        //urlConnection.addRequestProperty("Cookie", _mSharePointSession.cookieNedToken);

        urlConnection.connect();
        inputStream = urlConnection.getInputStream();
        while ((noOfBytes = inputStream.read(byteChunk)) > 0) {
            byteOutputStream.write(byteChunk, 0, noOfBytes);
        }
        OutputStream outputStream = new FileOutputStream(ImageFile);
        byteOutputStream.writeTo(outputStream);

        outputStream.close();
        ImageFile.deleteOnExit();

        String InputFileName = ImageFile.getCanonicalPath();


        try {
            metadataExtractor MetadataExtractor = new metadataExtractor(InputFileName);
            MetadataOutput=MetadataExtractor.MetadataReport;
        } catch (Throwable e) {
            Logger.getLogger(ToolboxAPI.class.getName()).log(Level.SEVERE, null, e);
        }
        return MetadataOutput;
    }

    public static void main(String[] args) throws MalformedURLException {

        long start = System.nanoTime();
        if (args.length == 0) {
            args = new String[1];
            args[0] = "http://i.imgur.com/BGIRJUh.jpg"; //DQ + Ghost
            //args[0] = "http://www.lincolnvscadillac.com/forum/attachment.php?attachmentid=37425&stc=1&d=1220640009"; //DQ + Ghost
            //args[0] = "http://de.trinixy.ru/pics4/20100318/podborka_14.jpg"; //Ghost
            //args[0] = "http://batona.net/uploads/posts/2014-01/1390536866_005.jpg"; //Mahdian+Ghost
            //args[0] = "http://36.media.tumblr.com/ce4acc665131ab979447ebae51ad97cc/tumblr_nhx89lgiUQ1sfx3flo1_1280.jpg"; //Mahdian
            //args[0] = "http://cdn.trinixy.ru/pics2/20070615/79.jpg"; //Mahdian + Ghost
            //args[0] = "http://worth1000.s3.amazonaws.com/submissions/712000/712415_7ffa_1024x2000.jpg";

            args[0] = "file:/media/marzampoglou/3TB/markzampoglou/ImageForensics/Datasets/Ruben/Reveal_Image_Manipulation_Dataset-2015-08-21/Reveal Image Manipulation Dataset/03_Fuji_a/DSCF3065_X-E2_manip+.jpg";
            //args[0] = "https://dl.dropboxusercontent.com/u/67895186/IMG_20150812_105958_Nex4_manip%2B.jpg";

            // does not work online:
            //args[0] = "http://www.gannett-cdn.com/-mm-/55a8014f604ae574fe5f883dde154b25c3a0599f/c=168-0-857-689&r=x408&c=405x405/local/-/media/2015/05/28/USATODAY/USATODAY/635684167819994087-WALLANCE.JPG";            
            //args[0] = "http://vignette4.wikia.nocookie.net/fantendo/images/6/6e/Small-mario.png/revision/latest?cb=20120718024112"; //Transparency
            //args[0] = "http://www.codeproject.com/KB/buttons/GdipButton/GdipButton1.PNG"; //Not JPEG
            //args[0] = "http://www.gannett-cdn.com/-mm-/55a8014f604ae574fe5f883dde154b25c3a0599f/c=168-0-857-689&r=x408&c=405x405/local/-/media/2015/05/28/USATODAY/USATODAY/635684167819994087-WALLANCE.JPG";
            //args[0] = "http://www.codeproject.com/KB/buttons/GdipButton/GdipButton1.PNG"; //Not JPEG
            //args[0]="file:" + System.getProperty("user.dir")+ "/target/classes/1.Ferguson.jpg";
            //args[0] = "file:/media/marzampoglou/3TB/markzampoglou/ImageForensics/Datasets/Wild Web Dataset/WildWeb/KerryFonda1/1.jpg";

            //args[0] = "file:/media/marzampoglou/3TB/markzampoglou/ImageForensics/Datasets/Anastasia/Anastasia Images/Sp/crowd_fake.jpg";

        } else {
            if (!(args[0].contains("file:") | args[0].contains("p://"))) {
                args[0] = "file:" + System.getProperty("user.dir") + "/" + args[0];
            }
        }

        //System.out.println("Looking for: \"" + args[0] + "\"");

        //String FileURL = (new File(args[0]).getAbsoluteFile().toURI().toURL()).toExternalForm();
        String FileURL = args[0];

        /*
         try {
         ForensicAnalysis anal = analyzeImage(FileURL,"./");

         System.out.println("DQ analysis: " + anal.DQ_Lin_Output + ":" + anal.DQ_Lin_MinValue + ":" + anal.DQ_Lin_MaxValue);
         System.out.println("DWT noise analysis: " + anal.Noise_Mahdian_Output + ":" + anal.Noise_Mahdian_MinValue + ":" + anal.Noise_Mahdian_MaxValue);
         System.out.println("Ghost qualities: " + anal.GhostMinQuality + ":" + anal.GhostMaxQuality);
         System.out.println("Ghost analysis: ");
         for (int ii = 0; ii < anal.GhostOutput.size(); ii++) {
         System.out.println(anal.GhostOutput.get(ii) + ":" + anal.GhostQualities.get(ii) + ":" + anal.Ghost_MinValues.get(ii) + ":" + anal.Ghost_MaxValues.get(ii) + ":");
         }
         System.out.println("Ghost GIF: " + anal.GhostGIFOutput);
         } catch (Throwable ex) {
         Logger.getLogger(ToolboxAPI.class.getName()).log(Level.SEVERE, null, ex);
         }        
         */
        try {
            //DQAnalysis analDQ = getImageDQ(FileURL, "./");
            JsonObject metaD=getImageMetadata(FileURL,"./");

            /*
            start = System.nanoTime();
            DQAnalysis analDQ = getImageDQ(FileURL, "./");
            System.out.println("DQ finished " + String.valueOf(System.nanoTime() - start));
            start = System.nanoTime();
            GhostAnalysis analGhost = getImageGhost(FileURL, "./");
            System.out.println("Ghost finished " + String.valueOf(System.nanoTime() - start));
            start = System.nanoTime();
            NoiseMahdianAnalysis analMahdian = getImageMahdianNoise(FileURL, "./");
            System.out.println("Mahdian finished " + String.valueOf(System.nanoTime() - start));
            start = System.nanoTime();
            ELAAnalysis analELA = getImageJPEGELA(FileURL, "./");
            System.out.println("ELA finished " + String.valueOf(System.nanoTime() - start));
            System.out.println("DQ analysis: " + analDQ.DQ_Lin_Output + ":" + analDQ.DQ_Lin_MinValue + ":" + analDQ.DQ_Lin_MaxValue);
            System.out.println("ELA analysis: " + analELA.ELA_Output+ ":" + analELA.ELA_MaxValue + ":" + analELA.ELA_MinValue);
            System.out.println("DWT noise analysis: " + analMahdian.Noise_Mahdian_Output + ":" + analMahdian.Noise_Mahdian_MinValue + ":" + analMahdian.Noise_Mahdian_MaxValue);
            System.out.println("Ghost qualities: " + analGhost.GhostMinQuality + ":" + analGhost.GhostMaxQuality);
            System.out.println("Ghost analysis: ");
            for (int ii = 0; ii < analGhost.GhostOutput.size(); ii++) {
                System.out.println(analGhost.GhostOutput.get(ii) + ":" + analGhost.GhostQualities.get(ii) + ":" + analGhost.Ghost_MinValues.get(ii) + ":" + analGhost.Ghost_MaxValues.get(ii) + ":");
            }
            */


            //System.out.println("Ghost GIF: " + analGhost.GhostGIFOutput);
        } catch (Throwable ex) {
            Logger.getLogger(ToolboxAPI.class.getName()).log(Level.SEVERE, null, ex);
        }

        long elapsedTime = System.nanoTime() - start;
        //System.out.println(elapsedTime);
    }

    static BufferedImage NotAJPEG() {
        String text = "Analysis not ";

        /*
         Because font metrics is based on a graphics context, we need to create
         a small, temporary image so we can ascertain the width and height
         of the final image
         */
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        Font font = new Font("Arial", Font.PLAIN, 48);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text) + 13;
        int height = (int) Math.round(fm.getHeight() * 3.7);
        g2d.dispose();

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.WHITE);
        g2d.drawString("Analysis not", 13, 10 + fm.getAscent());
        g2d.drawString("possible for", 13, 10 + 2 * fm.getAscent());
        g2d.drawString("non-JPEG", 13, 10 + 3 * fm.getAscent());
        g2d.drawString("images", 13, 10 + 4 * fm.getAscent());
        g2d.dispose();
        return img;
    }

    static BufferedImage TransparentPNGNotAccepted() {
        String text = "PNG files with ";

        /*
         Because font metrics is based on a graphics context, we need to create
         a small, temporary image so we can ascertain the width and height
         of the final image
         */
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        Font font = new Font("Arial", Font.PLAIN, 48);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text) + 13;
        int height = (int) Math.round(fm.getHeight() * 3.7);
        g2d.dispose();

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.WHITE);
        g2d.drawString("Analysis not", 13, 10 + fm.getAscent());
        g2d.drawString("possible for", 13, 10 + 2 * fm.getAscent());
        g2d.drawString("images with", 13, 10 + 3 * fm.getAscent());
        g2d.drawString("transparency", 13, 10 + 4 * fm.getAscent());
        g2d.dispose();
        return img;
    }
}
