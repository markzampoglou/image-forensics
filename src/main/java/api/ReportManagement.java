package api;

import java.awt.image.BufferedImage;
import java.io.*;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


import api.reports.*;
import com.drew.imaging.ImageProcessingException;
import maps.dwnoisevar.DWNoiseVarExtractor;
import meta.metadata.MetadataExtractor;
import meta.thumbnail.ThumbnailExtractor;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.MongoClient;
import meta.gps.GPSExtractor;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import maps.dq.DQExtractor;
import maps.mediannoise.MedianNoiseExtractor;
import maps.ela.ELAExtractor;
import maps.ghost.GhostExtractor;
import maps.blocking.BlockingExtractor;
import util.ArtificialImages;

import javax.imageio.ImageIO;

/**
 * Created by marzampoglou on 11/19/15.
 */
public class ReportManagement {
    static int numberOfThreads =7; //DQ, Noise, Ghost, ELA, Metadata, BLK, MedianNoise
    static long computationTimeoutLimit =60000;
    static int maxGhostImageSmallDimension =768;
    static int numGhostThreads=5;

    public static String downloadURL(String urlIn, String folderOut, String mongoHostIP) {

        MongoClient mongoclient = new MongoClient(mongoHostIP, 27017);
        Morphia morphia = new Morphia();
        morphia.map(ForensicReport.class).map(dqReport.class);
        Datastore ds = new Morphia().createDatastore(mongoclient, "ForensicDatabase");
        ds.ensureCaps();

        String urlHash = null;
        try {
            urlHash = buildURLHash(urlIn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(urlHash);

        String baseFolder = folderOut + urlHash + "/";

        ForensicReport report = ds.get(ForensicReport.class, urlHash);
        if (report != null) {
            System.out.println("Exists");
            //JsonParser parser = new JsonParser();
            //JsonObject ExtractedMetadataReport = parser.parse(report.metadataStringReport).getAsJsonObject();
            //System.out.println(ExtractedMetadataReport.toString());
        } else {
            report = new ForensicReport();
            report.id = urlHash;
            //ds.save(report);
            try {
                File writeFolder=new File(baseFolder);
                if (!writeFolder.exists())
                    writeFolder.mkdirs();
                downloadFile(urlIn, baseFolder);
                report.sourceImage = baseFolder + "Raw";
                report.displayImage = baseFolder + "Display.jpg";
                report.sourceURL = urlIn;
                report.status = "Downloaded";
                ds.save(report);
            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("ERROR: The requested URL does not respond or does not exist. Exiting.");
                //ds.delete(ForensicReport.class, urlHash);
                return "URL_ERROR";
            }
        }
        mongoclient.close();
        return urlHash;
    }

    public static String createReport(String urlHash, String mongoHostIP, String folderOut, int maxGhostImageSmallDimension, int numGhostThreads, long computationTimeoutLimit) {
        return reportCalculation(urlHash, mongoHostIP, folderOut, maxGhostImageSmallDimension, numGhostThreads, computationTimeoutLimit);
    }

    public static String createReport(String urlHash, String mongoHostIP, String folderOut) {
        return reportCalculation(urlHash, mongoHostIP, folderOut, maxGhostImageSmallDimension, numGhostThreads, computationTimeoutLimit);
    }

    public static String reportCalculation(String urlHash, String mongoHostIP, String folderOut, int maxGhostImageSmallDimension, int numGhostThreads, long computationTimeoutLimit){
        String  outMessage="COMPLETEDSUCCESSFULLY";
        ExecutorService threadpool = Executors.newFixedThreadPool(numberOfThreads);
        MongoClient mongoclient = new MongoClient(mongoHostIP, 27017);
        Morphia morphia = new Morphia();
        morphia.map(ForensicReport.class).map(dqReport.class);
        Datastore ds = new Morphia().createDatastore(mongoclient, "ForensicDatabase");
        ds.ensureCaps();
        String baseFolder = folderOut + urlHash + "/";
        ForensicReport report = ds.get(ForensicReport.class, urlHash);
        if (report == null) {
            return "HASHNOTFOUND";
        }
        if (report.status.equalsIgnoreCase("Processing")) {
            return "ALREADYPROCESSING";
        }
        if (report.status.equalsIgnoreCase("Done")) {
            return "PROCESSINGALREADYCOMPLETE";
        }
            report.status="Processing";
            dqReport dqReport=new dqReport();
            ELAReport elaReport=new ELAReport();
            GhostReport ghostReport =new GhostReport();
            DWNoiseReport dwNoiseReport=new DWNoiseReport();
            BlockingReport blockingReport=new BlockingReport();
            MedianNoiseReport medianNoiseReport=new MedianNoiseReport();
            GPSReport gpsReport = new GPSReport();

            File dqOutputfile = new File(baseFolder,"DQOutput.png");
            File dwNoiseOutputfile = new File(baseFolder,"DWNoiseOutput.png");
            File ghostOutputfile;
            File elaOutputfile = new File(baseFolder,"ELAOutput.png");
            File blkOutputfile = new File(baseFolder,"BLKOutput.png");
            File medianNoiseOutputFile = new File(baseFolder, "MedianNoiseOutput.png");

            try {
            if (ImageIO.read(new File(report.sourceImage)).getColorModel().hasAlpha()) {
                //If image has an alpha channel, then assume transparent PNG -No point in processing it
                BufferedImage transparentPNGNotAccepted= ArtificialImages.transparentPNGNotAccepted();
                ImageIO.write(transparentPNGNotAccepted, "png", dqOutputfile);
                dqReport.map = dqOutputfile.getCanonicalPath();
                dqReport.completed=true;
                report.dqReport =dqReport;
                ImageIO.write(transparentPNGNotAccepted, "png", dqOutputfile);
                dwNoiseReport.map = dwNoiseOutputfile.getCanonicalPath();
                dwNoiseReport.completed=true;
                report.dwNoiseReport =dwNoiseReport;
                ghostOutputfile=new File(baseFolder, "GhostOutput" + String.format("%02d", 0) + ".png");
                ImageIO.write(transparentPNGNotAccepted, "png", ghostOutputfile);
                ghostReport.maps.add(ghostOutputfile.getCanonicalPath());
                ghostReport.differences.add((float) 0.0);
                ghostReport.qualities.add(0);
                ghostReport.minValues.add((float) 0.0);
                ghostReport.maxValues.add((float) 0.0);
                ghostReport.completed=true;
                report.ghostReport = ghostReport;
                ImageIO.write(transparentPNGNotAccepted, "png", dwNoiseOutputfile);
                elaReport.map = elaOutputfile.getCanonicalPath();
                elaReport.completed=true;
                report.elaReport =elaReport;
                ImageIO.write(transparentPNGNotAccepted, "png", dqOutputfile);
                blockingReport.map = blkOutputfile.getCanonicalPath();
                blockingReport.completed=true;
                report.blockingReport =blockingReport;
                ImageIO.write(transparentPNGNotAccepted, "png", blkOutputfile);
                medianNoiseReport.map =medianNoiseOutputFile.getCanonicalPath();
                medianNoiseReport.completed=true;
                report.medianNoiseReport =medianNoiseReport;
                ds.save(report);
            } else {
                Boolean dqSaved =false, noiseDWSaved=false, ghostSaved=false, elaSaved =false, blkSaved =false, medianNoiseSaved =false;
                DQThread dqThread = new DQThread(report.sourceImage, dqOutputfile);
                Future dqFuture = threadpool.submit(dqThread);
                noiseDWThread noiseDWThread = new noiseDWThread(report.sourceImage, dwNoiseOutputfile);
                Future noiseDWFuture = threadpool.submit(noiseDWThread);
                GhostThread ghostThread = new GhostThread(report.sourceImage,baseFolder, maxGhostImageSmallDimension, numGhostThreads);
                Future ghostFuture = threadpool.submit(ghostThread);
                ELAThread elaThread = new ELAThread(report.sourceImage, elaOutputfile);
                Future elaFuture = threadpool.submit(elaThread);
                BLKThread blkThread = new BLKThread(report.sourceImage, blkOutputfile);
                Future blkFuture = threadpool.submit(blkThread);
                MedianNoiseThread medianNoiseThread = new MedianNoiseThread(report.sourceImage,medianNoiseOutputFile);
                Future medianNoiseFuture = threadpool.submit(medianNoiseThread);

                Long startTime=System.currentTimeMillis();
                MetadataExtractor metaExtractor;
                metaExtractor=new MetadataExtractor(report.sourceImage);
                JsonObject metadataReport=metaExtractor.metadataReport;
                metadataReport.addProperty("completed", true);
                report.metadataStringReport = metadataReport.toString();
                ds.save(report);

                GPSExtractor gpsExtractor=new GPSExtractor(metadataReport);
                gpsReport.completed=true;
                gpsReport.exists=gpsExtractor.exists;
                gpsReport.latitude=gpsExtractor.latitude;
                gpsReport.longitude=gpsExtractor.longitude;
                report.gpsReport =gpsReport;
                ds.save(report);

                ThumbnailReport thumbnail=new ThumbnailReport();
                ThumbnailExtractor thumbnailExtractor;
                thumbnailExtractor = new ThumbnailExtractor(report.sourceImage);
                thumbnail.numberOfThumbnails =thumbnailExtractor.numberOfThumbnails;
                File thumbFile;
                for (int thumbInd=0; thumbInd<thumbnailExtractor.numberOfThumbnails;thumbInd++){
                    thumbFile = new File(baseFolder,"Thumbnail" + String.valueOf(thumbInd) + ".png");
                    ImageIO.write(thumbnailExtractor.thumbnails.get(thumbInd), "png", thumbFile);
                    thumbnail.thumbnailList.add(thumbFile.getCanonicalPath());
                }
                report.thumbnailReport =thumbnail;
                ds.save(report);

                while (!dqFuture.isDone() | !noiseDWFuture.isDone() | !ghostFuture.isDone() | !elaFuture.isDone() | !blkFuture.isDone() | !medianNoiseFuture.isDone()) {
                    Thread.sleep(100); //sleep for 1 millisecond before checking again
                    if (dqFuture.isDone() & !dqSaved){
                        report.dqReport =(api.reports.dqReport) dqFuture.get();
                        dqSaved =true;
                        ds.save(report);
                        System.out.println("dqReport Done");
                    }
                    if (noiseDWFuture.isDone() & !noiseDWSaved){
                        report.dwNoiseReport =(DWNoiseReport) noiseDWFuture.get();
                        noiseDWSaved=true;
                        ds.save(report);
                        System.out.println("DWNoiseReport Done");
                    }
                    if (ghostFuture.isDone() & !ghostSaved){
                        report.ghostReport =(GhostReport) ghostFuture.get();
                        ghostSaved=true;
                        ds.save(report);
                        System.out.println("Ghost Done");
                    }
                    if (elaFuture.isDone() & !elaSaved){
                        report.elaReport =(ELAReport) elaFuture.get();
                        elaSaved =true;
                        ds.save(report);
                        System.out.println("elaReport Done");
                    }
                    if (blkFuture.isDone() & !blkSaved){
                        report.blockingReport =(BlockingReport) blkFuture.get();
                        blkSaved =true;
                        ds.save(report);
                        System.out.println("blockingReport Done");
                    }
                    if (medianNoiseFuture.isDone() & !medianNoiseSaved){
                        report.medianNoiseReport =(MedianNoiseReport) medianNoiseFuture.get();
                        medianNoiseSaved =true;
                        ds.save(report);
                        System.out.println("Median Noise Done");
                    }
                    if ((System.currentTimeMillis()-startTime) > computationTimeoutLimit){
                        System.out.println("Computation timed out");
                        outMessage="TIMEDOUT";
                        break;
                    }
                }
                threadpool.shutdown();
            }
            }
            catch (Exception e) {
                threadpool.shutdown();
                e.printStackTrace();
            }
        report.status="Done";
        ds.save(report);
        System.out.println("Will now close mongodb connection");
        mongoclient.close();
        return outMessage;
        }


    public static ForensicReport getReport(String urlHash, String mongoHostIP){
        MongoClient mongoclient = new MongoClient(mongoHostIP, 27017);
        Morphia morphia = new Morphia();
        morphia.map(ForensicReport.class).map(dqReport.class);
        Datastore ds = new Morphia().createDatastore(mongoclient, "ForensicDatabase");
        ds.ensureCaps();
        ForensicReport report = ds.get(ForensicReport.class, urlHash);
        if (report!=null) {
            JsonParser parser = new JsonParser();
            JsonObject tmpJson = parser.parse(report.metadataStringReport).getAsJsonObject();
            GsonBuilder builder = new GsonBuilder();
            report.metadataObjectReport = builder.create().fromJson(tmpJson, Object.class);
        }
        mongoclient.close();
        return report;
    }

    private static void downloadFile(String urlIn, String folderOut) throws IOException {
        URL imageURL = new URL(urlIn);
        File localDir = new File(folderOut);
        localDir.mkdir();
        File imageFile = new File (folderOut,"Raw");
        InputStream inputStream = null;
        URLConnection urlConnection = null;
        int noOfBytes = 0;
        byte[] byteChunk = new byte[4096];
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        urlConnection = imageURL.openConnection();
        urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        urlConnection.connect();
        inputStream = urlConnection.getInputStream();
        while ((noOfBytes = inputStream.read(byteChunk)) > 0) {
            byteOutputStream.write(byteChunk, 0, noOfBytes);
        }
        OutputStream outputStream = new FileOutputStream(imageFile);
        byteOutputStream.writeTo(outputStream);
        outputStream.close();

        BufferedImage downloadedImage=ImageIO.read(imageFile);
        ImageIO.write(downloadedImage, "JPEG", new File(folderOut , "Display.jpg"));
    }


    static String buildURLHash(String urlIn) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        //Build a hash based on the URL -would be better to build it based on the file content itself, but that might cause
        // synchronization issues while waiting for the file to download
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(urlIn.getBytes("UTF-8")); // Change this to "UTF-16" if needed
        byte[] digest = md.digest();
        String urlHash = String.format("%032x", new java.math.BigInteger(1, digest));
        return urlHash;
    }

    private static class DQThread implements Callable {
        String sourceFile ="";
        File outputFile =null;
        public DQThread(String SourceFile,File outputFile){
            this.sourceFile =SourceFile;
            this.outputFile = outputFile;
        }
        @Override
        public dqReport call() {
            dqReport output=null;
            try {
                output= dqCalculation();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }
        public dqReport dqCalculation() throws IOException {
            dqReport dqReport=new dqReport();
            DQExtractor dqDetector;
            dqDetector = new DQExtractor(sourceFile);
            ImageIO.write(dqDetector.displaySurface, "png", outputFile);
            dqReport.map = outputFile.getCanonicalPath();
            dqReport.maxValue = dqDetector.maxProbValue;
            dqReport.minvalue = dqDetector.minProbValue;
            dqReport.completed=true;
            return dqReport;
        }
    }

    private static class noiseDWThread implements Callable {
        String sourceFile ="";
        File outputFile =null;
        public noiseDWThread(String sourceFile, File outputFile){
            this.sourceFile =sourceFile;
            this.outputFile = outputFile;
        }
        @Override
        public DWNoiseReport call() {
            DWNoiseReport output=null;
            try {
                output= noiseDWCalculation();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }
        public DWNoiseReport noiseDWCalculation() throws IOException {
            DWNoiseReport dwNoiseReport=new DWNoiseReport();
            DWNoiseVarExtractor noiseExtractor;
            noiseExtractor = new DWNoiseVarExtractor(sourceFile);
            ImageIO.write(noiseExtractor.displaySurface, "png", outputFile);
            dwNoiseReport.map = outputFile.getCanonicalPath();
            dwNoiseReport.maxvalue = noiseExtractor.maxNoiseValue;
            dwNoiseReport.minValue = noiseExtractor.minNoiseValue;
            dwNoiseReport.completed=true;
            return dwNoiseReport;
        }
    }

    private static class GhostThread implements Callable {
        String sourceFile ="";
        String baseFolder ="";
        int maxGhostImageSmallDimension;
        int numGhostThreads;
        public GhostThread(String sourceFile,String baseFolder, int maxGhostImageSmallDimension, int numGhostThreads){
            this.sourceFile =sourceFile;
            this.baseFolder =baseFolder;
            this.maxGhostImageSmallDimension = maxGhostImageSmallDimension;
            this.numGhostThreads=numGhostThreads;
        }
        @Override
        public GhostReport call() {
            GhostReport output=null;
            try {
                output= ghostCalculation(maxGhostImageSmallDimension, numGhostThreads);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }
        public GhostReport ghostCalculation(int maxImageSmallDimension, int numThreads) throws IOException {
            File ghostOutputfile;
            GhostReport ghostReport=new GhostReport();
            GhostExtractor ghostExtractor;
            ghostExtractor = new GhostExtractor(sourceFile, maxImageSmallDimension, numThreads);
            BufferedImage ghostMap;
            for (int ghostMapInd=0;ghostMapInd<ghostExtractor.ghostMaps.size();ghostMapInd++) {
                ghostOutputfile=new File(baseFolder, "GhostOutput" + String.format("%02d", ghostMapInd) + ".png");
                ghostMap=ghostExtractor.ghostMaps.get(ghostMapInd);
                ImageIO.write(ghostMap, "png", ghostOutputfile);
                ghostReport.maps.add(ghostOutputfile.getCanonicalPath());
                ghostReport.differences = ghostExtractor.allDifferences;
                ghostReport.minQuality = ghostExtractor.qualityMin;
                ghostReport.maxQuality = ghostExtractor.qualityMax;
                ghostReport.qualities = ghostExtractor.ghostQualities;
                ghostReport.minValues = ghostExtractor.ghostMin;
                ghostReport.maxValues = ghostExtractor.ghostMax;
            }
            ghostReport.completed=true;
            return ghostReport;
        }
    }

    private static class ELAThread implements Callable {
        String sourceFile ="";
        File outputFile =null;
        public ELAThread(String SourceFile,File outputFile){
            this.sourceFile =SourceFile;
            this.outputFile = outputFile;
        }
        @Override
        public ELAReport call() {
            ELAReport output=null;
            try {
                output= elaCalculation();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }
        public ELAReport elaCalculation() throws IOException {
            ELAReport elaReport=new ELAReport();
            ELAExtractor elaExtractor;
            elaExtractor = new ELAExtractor(sourceFile);
            ImageIO.write(elaExtractor.displaySurface, "png", outputFile);
            elaReport.map = outputFile.getCanonicalPath();
            elaReport.maxValue = elaExtractor.elaMax;
            elaReport.minvalue = elaExtractor.elaMin;
            elaReport.completed=true;
            return elaReport;
        }
    }

    private static class BLKThread implements Callable {
        String sourceFile ="";
        File outputFile =null;
        public BLKThread(String sourceFile,File outputFile){
            this.sourceFile =sourceFile;
            this.outputFile = outputFile;
        }
        @Override
        public BlockingReport call() {
            BlockingReport output=null;
            try {
                output= blkCalculation();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }
        public BlockingReport blkCalculation() throws IOException {
            BlockingReport blockingReport=new BlockingReport();
            BlockingExtractor blockingExtractor;
            blockingExtractor = new BlockingExtractor(sourceFile);
            ImageIO.write(blockingExtractor.displaySurface, "png", outputFile);
            blockingReport.map = outputFile.getCanonicalPath();
            blockingReport.maxValue = blockingExtractor.blkmax;
            blockingReport.minValue = blockingExtractor.blkmin;
            blockingReport.completed=true;
            return blockingReport;
        }
    }

    private static class MedianNoiseThread implements Callable {
        String sourceFile ="";
        File outputFile =null;
        public MedianNoiseThread(String sourceFile,File outputFile){
            this.sourceFile =sourceFile;
            this.outputFile = outputFile;
        }
        @Override
        public MedianNoiseReport call() {
            MedianNoiseReport output=null;
            try {
                output= medianNoiseCalculation();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }
        public MedianNoiseReport medianNoiseCalculation() throws IOException {
            MedianNoiseReport medianNoiseReport=new MedianNoiseReport();
            MedianNoiseExtractor medianNoiseExtractor;
            medianNoiseExtractor = new MedianNoiseExtractor(sourceFile);
            ImageIO.write(medianNoiseExtractor.displaySurface, "png", outputFile);
            medianNoiseReport.map = outputFile.getCanonicalPath();
            medianNoiseReport.completed=true;
            return medianNoiseReport;
        }
    }

    public static void main (String[] args) {
        String OutputFolder = "/home/marzampoglou/Pictures/Reveal/ManipulationOutput/";
        //String URL="";
        String Hash1=downloadURL("http://160.40.51.26/projects/Reveal/imgs/example1_big.jpg", OutputFolder, "127.0.0.1");
        //Hash1=downloadURL("https://dl.dropboxusercontent.com/u/67895186/Tp_D_CND_M_N_ani00018_sec00096_00138.tif",OutputFolder, "127.0.0.1");
        createReport(Hash1, "127.0.0.1", OutputFolder);

        /*
        MetadataExtractor metaExtractor;

        try {
            metaExtractor=new MetadataExtractor(args[0]);
            JsonObject metadataReport=metaExtractor.metadataReport;
            metadataReport.addProperty("completed", true);
            String metadataStringReport = metadataReport.toString();
            try(  PrintWriter out = new PrintWriter( "filename.txt" )  ){
                out.println( metadataStringReport );
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        }
        */

    }
    public static String getMeta(String path) {
        //String OutputFolder = "/home/marzampoglou/Pictures/Reveal/ManipulationOutput/";
        //String URL="";
        String Hash1;//=downloadURL("https://dl.dropboxusercontent.com/u/67895186/DSCF3065_X-E2_manip%2B.jpg", OutputFolder);
        //Hash1=downloadURL("https://dl.dropboxusercontent.com/u/67895186/Tp_D_CND_M_N_ani00018_sec00096_00138.tif",OutputFolder, "127.0.0.1");
        //createReport(Hash1, "127.0.0.1", OutputFolder);

        MetadataExtractor metaExtractor;
        String metadataStringReport = "";
        try {
            metaExtractor = new MetadataExtractor(path);
            JsonObject metadataReport = metaExtractor.metadataReport;
            metadataReport.addProperty("completed", true);
            metadataStringReport = metadataReport.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        }
        return metadataStringReport;
    }
}
