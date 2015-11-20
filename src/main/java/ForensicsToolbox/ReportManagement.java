package ForensicsToolbox;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.MongoClient;
import metadataExtraction.metadataExtractor;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import pkg01_DoubleQuantization.DQDetector;
import pkg06_ELA.JPEGELAExtractor;
import pkg07_waveletnoisemap.NoiseMapExtractor;
import pkg08_JPEGGhost.JPEGGhostExtractor;

import javax.imageio.ImageIO;

/**
 * Created by marzampoglou on 11/19/15.
 */
public class ReportManagement {
    static int NumberOfThreads=5; //DQ, Noise, Ghost, ELA, Metadata
    static long ComputationTimeoutLimit=30000;
    private static final ExecutorService threadpool = Executors.newFixedThreadPool(NumberOfThreads);

    public static void CreateReport(String URLIn, String FolderOut) {

        MongoClient mongoclient = new MongoClient("127.0.0.1", 27017);
        Morphia morphia = new Morphia();
        morphia.map(ForensicReport.class).map(DQReport.class);
        Datastore ds = new Morphia().createDatastore(mongoclient, "ForensicDatabase");
        ds.ensureCaps();


        String URLHash=null;
        try {
            URLHash = buildURLHash(URLIn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(URLHash);

        String BaseFolder=FolderOut+URLHash+"/";

        ForensicReport Report = ds.get(ForensicReport.class, URLHash);
        if (Report != null) {
            System.out.println("Exists");
            JsonParser parser=new JsonParser();
            JsonObject ExtractedMetadataReport=parser.parse(Report.MetadataReport).getAsJsonObject();
            System.out.println(ExtractedMetadataReport.toString());
        } else {
            Report = new ForensicReport();
            Report.id = URLHash;
            ds.save(Report);

            try {
                DownloadFile(URLIn,BaseFolder);
                Report.SourceImage=BaseFolder+"Raw";
                Report.DisplayImage=BaseFolder+"Display.jpg";
                Report.SourceURL= URLIn;
                ds.save(Report);
            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("ERROR: The requested URL does not respond or does not exist. Exiting.");
                ds.delete(ForensicReport.class, URLHash);
                return;
            }

            DQReport DQ=new DQReport();
            ELAReport ELA=new ELAReport();
            GhostReport Ghost=new GhostReport();
            NoiseDWReport NoiseDW=new NoiseDWReport();

            File dqOutputfile = new File(BaseFolder,"DQOutput.png");
            File DWNoiseOutputfile = new File(BaseFolder,"DWNoiseOutput.png");
            File ghostOutputfile;
            File ELAOutputfile = new File(BaseFolder,"ELAOutput.png");

            try {
            if (ImageIO.read(new File(Report.SourceImage)).getColorModel().hasAlpha()) {
                //If image has an alpha channel, then assume transparent PNG -No point in processing it
                BufferedImage TransparencyNotAccepted=ArtificialImages.TransparentPNGNotAccepted();
                ImageIO.write(TransparencyNotAccepted, "png", dqOutputfile);
                DQ.Map=dqOutputfile.getCanonicalPath();
                DQ.completed=true;
                Report.DQ_Report=DQ;
                ImageIO.write(TransparencyNotAccepted, "png", DWNoiseOutputfile);
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
                ds.save(Report);
            } else {
                Boolean DQSaved=false, NoiseDWSaved=false, GhostSaved=false, ELASaved=false;
                DQThread DQtask = new DQThread(Report.SourceImage,dqOutputfile);
                Future DQfuture = threadpool.submit(DQtask);
                NoiseDWThread NoiseDWtask = new NoiseDWThread(Report.SourceImage,DWNoiseOutputfile);
                Future NoiseDWfuture = threadpool.submit(NoiseDWtask);
                GhostThread Ghosttask = new GhostThread(Report.SourceImage,BaseFolder);
                Future Ghostfuture = threadpool.submit(Ghosttask);
                ELAThread ELAtask = new ELAThread(Report.SourceImage,ELAOutputfile);
                Future ELAfuture = threadpool.submit(ELAtask);
                Long startTime=System.currentTimeMillis();
                metadataExtractor metaExtractor;
                metaExtractor=new metadataExtractor(Report.SourceImage);
                Report.MetadataReport=metaExtractor.MetadataReport.toString();
                ds.save(Report);

                while (!DQfuture.isDone() | !NoiseDWfuture.isDone() | !Ghostfuture.isDone() | !ELAfuture.isDone()) {
                    Thread.sleep(100); //sleep for 1 millisecond before checking again
                    if (DQfuture.isDone() & !DQSaved){
                        Report.DQ_Report=(DQReport) DQfuture.get();
                        DQSaved=true;
                        ds.save(Report);
                        System.out.println("DQ Done");
                    }
                    if (NoiseDWfuture.isDone() & !NoiseDWSaved){
                        Report.NoiseDW_Report=(NoiseDWReport) NoiseDWfuture.get();
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
                    if ((System.currentTimeMillis()-startTime) > ComputationTimeoutLimit){
                        System.out.println("Computation timed out");
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
        }
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
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(URLIn.getBytes("UTF-8")); // Change this to "UTF-16" if needed
        byte[] digest = md.digest();
        String URLHash = String.format("%064x", new java.math.BigInteger(1, digest));
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
            DQDetector dqDetector;
            dqDetector = new DQDetector(SourceFile);
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
        public NoiseDWReport call() {
            NoiseDWReport output=null;
            try {
                output=NoiseDWCalculation();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }
        public NoiseDWReport NoiseDWCalculation() throws IOException {
            NoiseDWReport NoiseDW=new NoiseDWReport();
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
        public GhostThread(String SourceFile,String BaseFolder){
            this.SourceFile=SourceFile;
            this.BaseFolder=BaseFolder;
        }
        @Override
        public GhostReport call() {
            GhostReport output=null;
            try {
                output=GhostCalculation();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }
        public GhostReport GhostCalculation() throws IOException {
            File ghostOutputfile;
            GhostReport Ghost=new GhostReport();
            JPEGGhostExtractor ghostExtractor;
            ghostExtractor = new JPEGGhostExtractor(SourceFile);
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
            JPEGELAExtractor ELAExtractor;
            ELAExtractor = new JPEGELAExtractor(SourceFile);
            ImageIO.write(ELAExtractor.DisplaySurface, "png", Outputfile);
            ELA.Map = Outputfile.getCanonicalPath();
            ELA.MaxValue = ELAExtractor.ELAMax;
            ELA.MinValue = ELAExtractor.ELAMin;
            ELA.completed=true;
            return ELA;

        }
    }

    public static void main (String[] args) {
        String OutputFolder = "/home/marzampoglou/Pictures/ForensicsDatabase/";
        String URL="http://i.imgur.com/BGIRJUh.jpg";
        CreateReport(URL,OutputFolder );
    }

}
