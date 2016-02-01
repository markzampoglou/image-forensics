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
import maps.dwnoise.NoiseMapExtractor;
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
import maps.mediannoise.MmedianNoiseExtractor;
import maps.ela.ELAExtractor;
import maps.ghost.GhostExtractor;
import maps.blocking.BlockingExtractor;
import util.ArtificialImages;

import javax.imageio.ImageIO;

/**
 * Created by marzampoglou on 11/19/15.
 */
public class ReportManagement {
    static int NumberOfThreads=7; //DQ, Noise, Ghost, ELA, Metadata, BLK, MedianNoise
    static long ComputationTimeoutLimit=60000;
    static int MaxGhostImageSmallDimension=768;
    static int numGhostThreads=5;
    private static ExecutorService threadpool;

    public static String DownloadURL(String URLIn, String FolderOut, String MongoHostIP) {

        MongoClient mongoclient = new MongoClient(MongoHostIP, 27017);
        Morphia morphia = new Morphia();
        morphia.map(ForensicReport.class).map(DQReport.class);
        Datastore ds = new Morphia().createDatastore(mongoclient, "ForensicDatabase");
        ds.ensureCaps();

        String URLHash = null;
        try {
            URLHash = buildURLHash(URLIn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(URLHash);

        String BaseFolder = FolderOut + URLHash + "/";

        ForensicReport Report = ds.get(ForensicReport.class, URLHash);
        if (Report != null) {
            System.out.println("Exists");
            //JsonParser parser = new JsonParser();
            //JsonObject ExtractedMetadataReport = parser.parse(Report.MetadataStringReport).getAsJsonObject();
            //System.out.println(ExtractedMetadataReport.toString());
        } else {
            Report = new ForensicReport();
            Report.id = URLHash;
            //ds.save(Report);
            try {
                File WriteFolder=new File(BaseFolder);
                if (!WriteFolder.exists())
                    WriteFolder.mkdirs();
                DownloadFile(URLIn, BaseFolder);
                Report.SourceImage = BaseFolder + "Raw";
                Report.DisplayImage = BaseFolder + "Display.jpg";
                Report.SourceURL = URLIn;
                Report.status = "Downloaded";
                ds.save(Report);
            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("ERROR: The requested URL does not respond or does not exist. Exiting.");
                //ds.delete(ForensicReport.class, URLHash);
                return "URL_ERROR";
            }
        }
        mongoclient.close();
        return URLHash;
    }

    public static String CreateReport(String URLHash, String MongoHostIP, String FolderOut, int MaxGhostImageSmallDimension, int numGhostThreads, long ComputationTimeoutLimit) {
        return ReportCalculation(URLHash, MongoHostIP, FolderOut, MaxGhostImageSmallDimension, numGhostThreads, ComputationTimeoutLimit);
    }

    public static String CreateReport(String URLHash, String MongoHostIP, String FolderOut) {
        return ReportCalculation(URLHash, MongoHostIP, FolderOut, MaxGhostImageSmallDimension, numGhostThreads, ComputationTimeoutLimit);
    }

    public static String ReportCalculation(String URLHash, String MongoHostIP, String FolderOut, int MaxGhostImageSmallDimension, int numGhostThreads, long ComputationTimeoutLimit){
        String  OutMessage="COMPLETEDSUCCESSFULLY";
        threadpool = Executors.newFixedThreadPool(NumberOfThreads);
        MongoClient mongoclient = new MongoClient(MongoHostIP, 27017);        Morphia morphia = new Morphia();
        morphia.map(ForensicReport.class).map(DQReport.class);
        Datastore ds = new Morphia().createDatastore(mongoclient, "ForensicDatabase");
        ds.ensureCaps();
        String BaseFolder = FolderOut + URLHash + "/";
        ForensicReport Report = ds.get(ForensicReport.class, URLHash);
        if (Report == null) {
            return "HASHNOTFOUND";
        }
        if (Report.status.equalsIgnoreCase("Processing")) {
            return "ALREADYPROCESSING";
        }
        if (Report.status.equalsIgnoreCase("Done")) {
            return "PROCESSINGALREADYCOMPLETE";
        }
            Report.status="Processing";
            DQReport DQ=new DQReport();
            ELAReport ELA=new ELAReport();
            GhostReport Ghost=new GhostReport();
            DWNoiseReport NoiseDW=new DWNoiseReport();
            BlockingReport BLK=new BlockingReport();
            MedianNoiseReport MedianNoise=new MedianNoiseReport();
            GPSReport GPS = new GPSReport();

            File DQOutputfile = new File(BaseFolder,"DQOutput.png");
            File DWNoiseOutputfile = new File(BaseFolder,"DWNoiseOutput.png");
            File ghostOutputfile;
            File ELAOutputfile = new File(BaseFolder,"ELAOutput.png");
            File BLKOutputfile = new File(BaseFolder,"BLKOutput.png");
            File MedianNoiseOutputFile = new File(BaseFolder, "MedianNoiseOutput.png");

            try {
            if (ImageIO.read(new File(Report.SourceImage)).getColorModel().hasAlpha()) {
                //If image has an alpha channel, then assume transparent PNG -No point in processing it
                BufferedImage TransparencyNotAccepted= ArtificialImages.TransparentPNGNotAccepted();
                ImageIO.write(TransparencyNotAccepted, "png", DQOutputfile);
                DQ.Map=DQOutputfile.getCanonicalPath();
                DQ.completed=true;
                Report.DQ_Report=DQ;
                ImageIO.write(TransparencyNotAccepted, "png", DQOutputfile);
                NoiseDW.Map = DWNoiseOutputfile.getCanonicalPath();
                NoiseDW.completed=true;
                Report.NoiseDW_Report=NoiseDW;
                ghostOutputfile=new File(BaseFolder, "GhostOutput" + String.format("%02d", 0) + ".png");
                ImageIO.write(TransparencyNotAccepted, "png", ghostOutputfile);
                Ghost.Maps.add(ghostOutputfile.getCanonicalPath());
                Ghost.Differences.add((float) 0.0);
                Ghost.Qualities.add(0);
                Ghost.MinValues.add((float) 0.0);
                Ghost.MaxValues.add((float) 0.0);
                Ghost.completed=true;
                Report.Ghost_Report=Ghost;
                ImageIO.write(TransparencyNotAccepted, "png", DWNoiseOutputfile);
                ELA.Map = ELAOutputfile.getCanonicalPath();
                ELA.completed=true;
                Report.ELA_Report=ELA;
                ImageIO.write(TransparencyNotAccepted, "png", DQOutputfile);
                BLK.Map=BLKOutputfile.getCanonicalPath();
                BLK.completed=true;
                Report.BLK_Report=BLK;
                ImageIO.write(TransparencyNotAccepted, "png", BLKOutputfile);
                MedianNoise.Map=MedianNoiseOutputFile.getCanonicalPath();
                MedianNoise.completed=true;
                Report.MedianNoise_Report=MedianNoise;
                ds.save(Report);
            } else {
                Boolean DQSaved=false, NoiseDWSaved=false, GhostSaved=false, ELASaved=false, BLKSaved=false, MedianNoiseSaved=false;
                DQThread DQtask = new DQThread(Report.SourceImage,DQOutputfile);
                Future DQfuture = threadpool.submit(DQtask);
                NoiseDWThread NoiseDWtask = new NoiseDWThread(Report.SourceImage,DWNoiseOutputfile);
                Future NoiseDWfuture = threadpool.submit(NoiseDWtask);
                GhostThread Ghosttask = new GhostThread(Report.SourceImage,BaseFolder, MaxGhostImageSmallDimension, numGhostThreads);
                Future Ghostfuture = threadpool.submit(Ghosttask);
                ELAThread ELAtask = new ELAThread(Report.SourceImage,ELAOutputfile);
                Future ELAfuture = threadpool.submit(ELAtask);
                BLKThread BLKtask = new BLKThread(Report.SourceImage,BLKOutputfile);
                Future BLKfuture = threadpool.submit(BLKtask);
                MedianNoiseThread MedianNoisetask = new MedianNoiseThread(Report.SourceImage,MedianNoiseOutputFile);
                Future MedianNoisefuture = threadpool.submit(MedianNoisetask);

                Long startTime=System.currentTimeMillis();
                MetadataExtractor metaExtractor;
                metaExtractor=new MetadataExtractor(Report.SourceImage);
                JsonObject MetadataReport=metaExtractor.MetadataReport;
                MetadataReport.addProperty("completed", true);
                Report.MetadataStringReport = MetadataReport.toString();
                ds.save(Report);

                GPSExtractor GPSEx=new GPSExtractor(MetadataReport);
                GPS.completed=true;
                GPS.exists=GPSEx.exists;
                GPS.latitude=GPSEx.latitude;
                GPS.longitude=GPSEx.longitude;
                Report.GPS_Report=GPS;
                ds.save(Report);

                ThumbnailReport Thumbnail=new ThumbnailReport();
                ThumbnailExtractor thumbExtractor;
                thumbExtractor = new ThumbnailExtractor(Report.SourceImage);
                Thumbnail.NumberOfThumbnails=thumbExtractor.NumberOfThumbnails;
                File ThumbFile;
                for (int ThumbInd=0; ThumbInd<thumbExtractor.NumberOfThumbnails;ThumbInd++){
                    ThumbFile = new File(BaseFolder,"Thumbnail" + String.valueOf(ThumbInd) + ".png");
                    ImageIO.write(thumbExtractor.Thumbnails.get(ThumbInd), "png", ThumbFile);
                    Thumbnail.ThumbnailList.add(ThumbFile.getCanonicalPath());
                }
                Report.Thumbnail_Report=Thumbnail;
                ds.save(Report);

                while (!DQfuture.isDone() | !NoiseDWfuture.isDone() | !Ghostfuture.isDone() | !ELAfuture.isDone() | !BLKfuture.isDone() | !MedianNoisefuture.isDone()) {
                    Thread.sleep(100); //sleep for 1 millisecond before checking again
                    if (DQfuture.isDone() & !DQSaved){
                        Report.DQ_Report=(DQReport) DQfuture.get();
                        DQSaved=true;
                        ds.save(Report);
                        System.out.println("DQ Done");
                    }
                    if (NoiseDWfuture.isDone() & !NoiseDWSaved){
                        Report.NoiseDW_Report=(DWNoiseReport) NoiseDWfuture.get();
                        NoiseDWSaved=true;
                        ds.save(Report);
                        System.out.println("NoiseDW Done");
                    }
                    if (Ghostfuture.isDone() & !GhostSaved){
                        Report.Ghost_Report=(GhostReport) Ghostfuture.get();
                        GhostSaved=true;
                        ds.save(Report);
                        System.out.println("Ghost Done");
                    }
                    if (ELAfuture.isDone() & !ELASaved){
                        Report.ELA_Report=(ELAReport) ELAfuture.get();
                        ELASaved=true;
                        ds.save(Report);
                        System.out.println("ELA Done");
                    }
                    if (BLKfuture.isDone() & !BLKSaved){
                        Report.BLK_Report=(BlockingReport) BLKfuture.get();
                        BLKSaved=true;
                        ds.save(Report);
                        System.out.println("BLK Done");
                    }
                    if (MedianNoisefuture.isDone() & !MedianNoiseSaved){
                        Report.MedianNoise_Report=(MedianNoiseReport) MedianNoisefuture.get();
                        MedianNoiseSaved=true;
                        ds.save(Report);
                        System.out.println("Median Noise Done");
                    }
                    if ((System.currentTimeMillis()-startTime) > ComputationTimeoutLimit){
                        System.out.println("Computation timed out");
                        OutMessage="TIMEDOUT";
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
        Report.status="Done";
        ds.save(Report);
        System.out.println("Will now close mongodb connection");
        mongoclient.close();
        return OutMessage;
        }


    public static ForensicReport GetReport(String URLHash, String MongoHostIP){
        MongoClient mongoclient = new MongoClient(MongoHostIP, 27017);
        Morphia morphia = new Morphia();
        morphia.map(ForensicReport.class).map(DQReport.class);
        Datastore ds = new Morphia().createDatastore(mongoclient, "ForensicDatabase");
        ds.ensureCaps();
        ForensicReport Report = ds.get(ForensicReport.class, URLHash);
        if (Report!=null) {
            JsonParser parser = new JsonParser();
            JsonObject tmpJson = parser.parse(Report.MetadataStringReport).getAsJsonObject();
            GsonBuilder builder = new GsonBuilder();
            Report.MetadataObjectReport = builder.create().fromJson(tmpJson, Object.class);
        }
        mongoclient.close();
        return Report;
    }

    private static void DownloadFile(String URLIn, String FolderOut) throws IOException {
        URL ImageURL = new URL(URLIn);
        File LocalDir = new File(FolderOut);
        LocalDir.mkdir();
        File ImageFile = new File (FolderOut,"Raw");
        InputStream inputStream = null;
        URLConnection urlConnection = null;
        int noOfBytes = 0;
        byte[] byteChunk = new byte[4096];
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        urlConnection = ImageURL.openConnection();
        urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        urlConnection.connect();
        inputStream = urlConnection.getInputStream();
        while ((noOfBytes = inputStream.read(byteChunk)) > 0) {
            byteOutputStream.write(byteChunk, 0, noOfBytes);
        }
        OutputStream outputStream = new FileOutputStream(ImageFile);
        byteOutputStream.writeTo(outputStream);
        outputStream.close();

        BufferedImage DownloadedImage=ImageIO.read(ImageFile);
        ImageIO.write(DownloadedImage, "JPEG", new File(FolderOut , "Display.jpg"));
    }


    static String buildURLHash(String URLIn) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        //Build a hash based on the URL -would be better to build it based on the file content itself, but that might cause
        // synchronization issues while waiting for the file to download
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(URLIn.getBytes("UTF-8")); // Change this to "UTF-16" if needed
        byte[] digest = md.digest();
        String URLHash = String.format("%032x", new java.math.BigInteger(1, digest));
        return URLHash;
    }

    private static class DQThread implements Callable {
        String SourceFile="";
        File Outputfile=null;
        public DQThread(String SourceFile,File Outputfile){
            this.SourceFile=SourceFile;
            this.Outputfile=Outputfile;
        }
        @Override
        public DQReport call() {
            DQReport output=null;
            try {
                output=DQCalculation();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }
        public DQReport DQCalculation() throws IOException {
            DQReport DQ=new DQReport();
            DQExtractor dqDetector;
            dqDetector = new DQExtractor(SourceFile);
            ImageIO.write(dqDetector.DisplaySurface, "png", Outputfile);
            DQ.Map = Outputfile.getCanonicalPath();
            DQ.MaxValue = dqDetector.maxProbValue;
            DQ.MinValue = dqDetector.minProbValue;
            DQ.completed=true;
            return DQ;
        }
    }

    private static class NoiseDWThread implements Callable {
        String SourceFile="";
        File Outputfile=null;
        public NoiseDWThread(String SourceFile,File Outputfile){
            this.SourceFile=SourceFile;
            this.Outputfile=Outputfile;
        }
        @Override
        public DWNoiseReport call() {
            DWNoiseReport output=null;
            try {
                output=NoiseDWCalculation();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }
        public DWNoiseReport NoiseDWCalculation() throws IOException {
            DWNoiseReport NoiseDW=new DWNoiseReport();
            NoiseMapExtractor noiseExtractor;
            noiseExtractor = new NoiseMapExtractor(SourceFile);
            ImageIO.write(noiseExtractor.DisplaySurface, "png", Outputfile);
            NoiseDW.Map = Outputfile.getCanonicalPath();
            NoiseDW.MaxValue = noiseExtractor.maxNoiseValue;
            NoiseDW.MinValue = noiseExtractor.minNoiseValue;
            NoiseDW.completed=true;
            return NoiseDW;
        }
    }

    private static class GhostThread implements Callable {
        String SourceFile="";
        String BaseFolder="";
        int MaxGhostImageSmallDimension;
        int numGhostThreads;
        public GhostThread(String SourceFile,String BaseFolder, int MaxGhostImageSmallDimension, int numGhostThreads){
            this.SourceFile=SourceFile;
            this.BaseFolder=BaseFolder;
            this.MaxGhostImageSmallDimension= MaxGhostImageSmallDimension;
            this.numGhostThreads=numGhostThreads;
        }
        @Override
        public GhostReport call() {
            GhostReport output=null;
            try {
                output=GhostCalculation(MaxGhostImageSmallDimension, numGhostThreads);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }
        public GhostReport GhostCalculation(int MaxImageSmallDimension,int numThreads) throws IOException {
            File ghostOutputfile;
            GhostReport Ghost=new GhostReport();
            GhostExtractor ghostExtractor;
            ghostExtractor = new GhostExtractor(SourceFile, MaxImageSmallDimension, numThreads);
            BufferedImage GhostMap;
            for (int GhostMapInd=0;GhostMapInd<ghostExtractor.GhostMaps.size();GhostMapInd++) {
                ghostOutputfile=new File(BaseFolder, "GhostOutput" + String.format("%02d", GhostMapInd) + ".png");
                GhostMap=ghostExtractor.GhostMaps.get(GhostMapInd);
                ImageIO.write(GhostMap, "png", ghostOutputfile);
                Ghost.Maps.add(ghostOutputfile.getCanonicalPath());
                Ghost.Differences = ghostExtractor.AllDifferences;
                Ghost.MinQuality = ghostExtractor.QualityMin;
                Ghost.MaxQuality = ghostExtractor.QualityMax;
                Ghost.Qualities = ghostExtractor.GhostQualities;
                Ghost.MinValues = ghostExtractor.GhostMin;
                Ghost.MaxValues = ghostExtractor.GhostMax;
            }
            Ghost.completed=true;
            return Ghost;
        }
    }

    private static class ELAThread implements Callable {
        String SourceFile="";
        File Outputfile=null;
        public ELAThread(String SourceFile,File Outputfile){
            this.SourceFile=SourceFile;
            this.Outputfile=Outputfile;
        }
        @Override
        public ELAReport call() {
            ELAReport output=null;
            try {
                output=ELACalculation();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }
        public ELAReport ELACalculation() throws IOException {
            ELAReport ELA=new ELAReport();
            ELAExtractor ELAExtractor;
            ELAExtractor = new ELAExtractor(SourceFile);
            ImageIO.write(ELAExtractor.DisplaySurface, "png", Outputfile);
            ELA.Map = Outputfile.getCanonicalPath();
            ELA.MaxValue = ELAExtractor.ELAMax;
            ELA.MinValue = ELAExtractor.ELAMin;
            ELA.completed=true;
            return ELA;
        }
    }

    private static class BLKThread implements Callable {
        String SourceFile="";
        File Outputfile=null;
        public BLKThread(String SourceFile,File Outputfile){
            this.SourceFile=SourceFile;
            this.Outputfile=Outputfile;
        }
        @Override
        public BlockingReport call() {
            BlockingReport output=null;
            try {
                output=BLKCalculation();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }
        public BlockingReport BLKCalculation() throws IOException {
            BlockingReport BLK=new BlockingReport();
            BlockingExtractor BLKExtractor;
            BLKExtractor = new BlockingExtractor(SourceFile);
            ImageIO.write(BLKExtractor.DisplaySurface, "png", Outputfile);
            BLK.Map = Outputfile.getCanonicalPath();
            BLK.MaxValue = BLKExtractor.BLKMax;
            BLK.MinValue = BLKExtractor.BLKMin;
            BLK.completed=true;
            return BLK;
        }
    }

    private static class MedianNoiseThread implements Callable {
        String SourceFile="";
        File Outputfile=null;
        public MedianNoiseThread(String SourceFile,File Outputfile){
            this.SourceFile=SourceFile;
            this.Outputfile=Outputfile;
        }
        @Override
        public MedianNoiseReport call() {
            MedianNoiseReport output=null;
            try {
                output=MedianNoiseCalculation();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }
        public MedianNoiseReport MedianNoiseCalculation() throws IOException {
            MedianNoiseReport MedianNoise=new MedianNoiseReport();
            MmedianNoiseExtractor MedianExtractor;
            MedianExtractor = new MmedianNoiseExtractor(SourceFile);
            ImageIO.write(MedianExtractor.DisplaySurface, "png", Outputfile);
            MedianNoise.Map = Outputfile.getCanonicalPath();
            MedianNoise.completed=true;
            return MedianNoise;
        }
    }

    public static void main (String[] args) {
        String OutputFolder = "/home/marzampoglou/Pictures/Reveal/ManipulationOutput/";
        //String URL="";
        String Hash1;//=DownloadURL("https://dl.dropboxusercontent.com/u/67895186/DSCF3065_X-E2_manip%2B.jpg", OutputFolder);
        Hash1=DownloadURL("http://160.40.51.26/projects/Reveal/imgs/example1_big.jpg",OutputFolder, "127.0.0.1");
        CreateReport(Hash1, "127.0.0.1", OutputFolder);

/*        try {
            ThumbnailExtractor ex=new ThumbnailExtractor("/home/marzampoglou/Desktop/img_1771.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedImage buf = JPEGMetaData.getThumbnail(new File("/home/marzampoglou/Desktop/img_1771.jpg"));
            ImageIO.write(buf,"JPEG",new File("/home/marzampoglou/Desktop/thumb.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
*/

        //String Hash2=DownloadURL("http://www.lincolnvscadillac.com/forum/attachment.php?attachmentid=37425&stc=1&d=1220640009", OutputFolder);
        //CreateReport(Hash2, OutputFolder);
/*        String Hash3=DownloadURL("http://de.trinixy.ru/pics4/20100318/podborka_14.jpg", OutputFolder);
        CreateReport(Hash3, OutputFolder);
        System.out.println(Hash3);
        String Hash4=DownloadURL("http://batona.net/uploads/posts/2014-01/1390536866_005.jpg", OutputFolder);
        CreateReport(Hash4, OutputFolder);
        System.out.println(Hash4);
*/
        //ForensicReport Report = GetReport("79b16f4bced02b565416b7aeaea32db13a3590b32835bfcf3c5d6bc765948a3e");
        //System.out.println(Report.MetadataObjectReport.toString());
        /*
        try {

            //NoiseMapExtractor ex = new NoiseMapExtractor("/home/marzampoglou/Pictures/Reveal/ManipulationOutput/79b16f4bced02b565416b7aeaea32db13a3590b32835bfcf3c5d6bc765948a3e/Raw");
            MetadataExtractor meta =new MetadataExtractor("/home/marzampoglou/Pictures/Reveal/ManipulationOutput/79b16f4bced02b565416b7aeaea32db13a3590b32835bfcf3c5d6bc765948a3e/Raw");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        }
    */
    }
}
