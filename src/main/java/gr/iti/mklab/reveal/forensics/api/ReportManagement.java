package gr.iti.mklab.reveal.forensics.api;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import gr.iti.mklab.reveal.forensics.api.reports.*;

import com.drew.imaging.ImageProcessingException; 

import gr.iti.mklab.reveal.forensics.maps.dwnoisevar.DWNoiseVarExtractor;
import gr.iti.mklab.reveal.forensics.meta.metadata.MetadataExtractor;
import gr.iti.mklab.reveal.forensics.meta.thumbnail.ThumbnailExtractor;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.MongoClient;

import gr.iti.mklab.reveal.forensics.meta.gps.GPSExtractor;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.omg.CORBA.portable.ApplicationException;

import gr.iti.mklab.reveal.forensics.maps.dq.DQExtractor;
import gr.iti.mklab.reveal.forensics.maps.mediannoise.MedianNoiseExtractor;
import gr.iti.mklab.reveal.forensics.maps.ela.ELAExtractor;
import gr.iti.mklab.reveal.forensics.maps.ghost.GhostExtractor;
import gr.iti.mklab.reveal.forensics.maps.grids.GridsExtractor;
import gr.iti.mklab.reveal.forensics.maps.blocking.BlockingExtractor;
import gr.iti.mklab.reveal.forensics.util.ArtificialImages;
import javax.imageio.ImageIO;

// Logger to suppress MongoDB message flood
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Created by marzampoglou on 11/19/15.
 */
public class ReportManagement {
    static int numberOfThreads =7; //DQ, Noise, Ghost, ELA, Metadata, BLK, MedianNoise
    static long computationTimeoutLimit =60000;
    static int maxGhostImageSmallDimension =768;
    static int numGhostThreads=5;
    
    private static String url;

    // Suppress MongoDB logging
    static Logger root = (Logger) LoggerFactory
            .getLogger(Logger.ROOT_LOGGER_NAME);
    static {
        root.setLevel(Level.WARN);
    }
        
    public static String downloadURL(String urlIn, String folderOut, String mongoHostIP) throws IOException {
    	System.out.println("downloadURL");
    	String imgHash = null;
    	byte[] data = null;
        MongoClient mongoclient = new MongoClient(mongoHostIP, 27017);
       // System.out.println("mongoHostIP :: " + mongoHostIP);
        Morphia morphia = new Morphia();
        morphia.map(ForensicReport.class).map(dqReport.class);
        Datastore ds = new Morphia().createDatastore(mongoclient, "ForensicDatabase");
        ds.ensureCaps();
        
        // connect to URL and get input stream
        URL imageURL = new URL(urlIn);
        File localDir = new File(folderOut);
        localDir.mkdir();
       
       try{ 
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
	         // hash creation from image file  
		        try {
		        	System.out.println("Start MD5 Digest");
		        	data = byteOutputStream.toByteArray();
					MessageDigest md = MessageDigest.getInstance("MD5");
					md.update(data);
					byte[] hash = md.digest();
					imgHash = String.format("%032x", new java.math.BigInteger(1, hash));
					System.out.println("Hash : " + imgHash);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					 e.printStackTrace();
				}
	        
		        String baseFolder = folderOut + imgHash + "/";
		        ForensicReport report = ds.get(ForensicReport.class, imgHash);		       
		        // check if hash exist
		        if (report != null) {
		            System.out.println("Exists");    
		        }else{  		        	
		        	// if hash does not exist in database, then download the image
		        	 report = new ForensicReport();
		             report.id = imgHash;            
		             try {
			             File writeFolder=new File(baseFolder);			           
			             if (!writeFolder.exists())
			                 writeFolder.mkdirs();	             
			             	File imageFile = new File (baseFolder,"Raw");			             	
			             	OutputStream outputStream = new FileOutputStream(imageFile);
			             	        		             	             
			             	byteOutputStream.writeTo(outputStream);
			             	outputStream.close();
			             	BufferedImage downloadedImage=ImageIO.read(imageFile);            	
			                ImageIO.write(downloadedImage, "JPEG", new File(baseFolder , "Display.jpg"));
			             	// store in database image information
			             	report.sourceImage = baseFolder + "Raw";
			                report.displayImage = baseFolder + "Display.jpg";
			                report.sourceURL = urlIn;
			                report.status = "Downloaded";
			                ds.save(report);
			            } catch (Exception e) {			               
			                System.out.println("ERROR: The requested URL does not respond or does not exist. Exiting.");
                            mongoclient.close();
			                return "URL_ERROR";
			            }
		        } 
	     } catch (Exception e) {
	        System.out.println("ERROR1: The requested URL does not respond or does not exist. Exiting.");
            mongoclient.close();
	        return "URL_ERROR";
	    }
        mongoclient.close();
        return imgHash;
    }    
    
    
    public static String createReport(String urlHash, String mongoHostIP, String folderOut, int maxGhostImageSmallDimension, int numGhostThreads, long computationTimeoutLimit) throws UnknownHostException {
        return reportCalculation(urlHash, mongoHostIP, folderOut, maxGhostImageSmallDimension, numGhostThreads, computationTimeoutLimit);
    }

    public static String createReport(String urlHash, String mongoHostIP, String folderOut) throws UnknownHostException {
    	 return reportCalculation(urlHash, mongoHostIP, folderOut, maxGhostImageSmallDimension, numGhostThreads, computationTimeoutLimit);
    }

    public static String reportCalculation(String urlHash, String mongoHostIP, String folderOut, int maxGhostImageSmallDimension, int numGhostThreads, long computationTimeoutLimit) throws UnknownHostException{
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
        	System.out.println("start processing");
            report.status="Processing";
            dqReport dqReport=new dqReport();
            ELAReport elaReport=new ELAReport();
            GhostReport ghostReport =new GhostReport();
            DWNoiseReport dwNoiseReport=new DWNoiseReport();
            BlockingReport blockingReport=new BlockingReport();
            MedianNoiseReport medianNoiseReport=new MedianNoiseReport();
            GPSReport gpsReport = new GPSReport();
            GridsNormalReport gridsReport =new GridsNormalReport();
            GridsInversedReport gridsInversedReport=new GridsInversedReport();
            
            File dqOutputfile = new File(baseFolder,"DQOutput.png");
            File dwNoiseOutputfile = new File(baseFolder,"DWNoiseOutput.png");
            File ghostOutputfile;
            File elaOutputfile = new File(baseFolder,"ELAOutput.png");
            File blkOutputfile = new File(baseFolder,"BLKOutput.png");
            File medianNoiseOutputFile = new File(baseFolder, "MedianNoiseOutput.png");
            File gridsOutputFile = new File(baseFolder, "GridsOutput.png");
            File gridsInversedOutputFile = new File(baseFolder, "GridsInversedOutput.png");
            try {
            if (ImageIO.read(new File(report.sourceImage)).getColorModel().hasAlpha()) {
            	System.out.println("If image has an alpha channel, then assume transparent PNG -No point in processing it");
                //If image has an alpha channel, then assume transparent PNG -No point in processing it
                BufferedImage transparentPNGNotAccepted= ArtificialImages.transparentPNGNotAccepted();
                ImageIO.write(transparentPNGNotAccepted, "png", dqOutputfile);
                System.out.println(" dqOutputfile.getCanonicalPath() 1 :: " + dqOutputfile.getCanonicalPath());
                dqReport.map = dqOutputfile.getCanonicalPath();
                dqReport.completed=true;
                report.dqReport =dqReport;
                ImageIO.write(transparentPNGNotAccepted, "png", dwNoiseOutputfile);
                System.out.println(" dwNoiseOutputfile.getCanonicalPath() :: " + dwNoiseOutputfile.getCanonicalPath());
                dwNoiseReport.map = dwNoiseOutputfile.getCanonicalPath();
                dwNoiseReport.completed=true;
                report.dwNoiseReport =dwNoiseReport;
                ghostOutputfile=new File(baseFolder, "GhostOutput" + String.format("%02d", 0) + ".png");
                ImageIO.write(transparentPNGNotAccepted, "png", ghostOutputfile);
                ghostReport.maps.add(ghostOutputfile.getCanonicalPath());
                System.out.println(" ghostOutputfile.getCanonicalPath() :: " + ghostOutputfile.getCanonicalPath());
                ghostReport.differences.add((float) 0.0);
                ghostReport.qualities.add(0);
                ghostReport.minValues.add((float) 0.0);
                ghostReport.maxValues.add((float) 0.0);
                ghostReport.completed=true;
                report.ghostReport = ghostReport;
                ImageIO.write(transparentPNGNotAccepted, "png", elaOutputfile);
                System.out.println(" elaOutputfile.getCanonicalPath() :: " + elaOutputfile.getCanonicalPath());
                elaReport.map = elaOutputfile.getCanonicalPath();
                elaReport.completed=true;
                report.elaReport =elaReport;
                ImageIO.write(transparentPNGNotAccepted, "png", blkOutputfile);
                blockingReport.map = blkOutputfile.getCanonicalPath();
                System.out.println(" blkOutputfile.getCanonicalPath() :: " +  blkOutputfile.getCanonicalPath());
                blockingReport.completed=true;
                report.blockingReport =blockingReport;
                ImageIO.write(transparentPNGNotAccepted, "png", medianNoiseOutputFile);
                medianNoiseReport.map =medianNoiseOutputFile.getCanonicalPath();
                System.out.println(" medianNoiseOutputFile.getCanonicalPath() :: " +  medianNoiseOutputFile.getCanonicalPath());
                medianNoiseReport.completed=true;
                report.medianNoiseReport =medianNoiseReport;                
                ImageIO.write(transparentPNGNotAccepted, "png", gridsOutputFile);
                gridsReport.map =gridsOutputFile.getCanonicalPath();
                System.out.println(" gridsOutputFile.getCanonicalPath() :: " +  gridsOutputFile.getCanonicalPath());                
                ImageIO.write(transparentPNGNotAccepted, "png", gridsInversedOutputFile);
                gridsInversedReport.map =gridsInversedOutputFile.getCanonicalPath();
                System.out.println(" gridsInversedOutputFile.getCanonicalPath() :: " +  gridsInversedOutputFile.getCanonicalPath());                
                gridsReport.completed=true;
                gridsInversedReport.completed=true;
                report.gridsReport = gridsReport;
                report.gridsInversedReport = gridsInversedReport;
                ds.save(report);
            } else {
            	
                Boolean dqSaved =false, noiseDWSaved=false, ghostSaved=false, elaSaved =false, blkSaved =false, medianNoiseSaved =false, gridsSaved = false;
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
                GridsThread gridsThread = new GridsThread(report.sourceImage,gridsOutputFile, gridsInversedOutputFile);
                Future gridsFuture = threadpool.submit(gridsThread);
              //  GridsInversedThread gridsInversedThread = new GridsInversedThread(report.sourceImage,gridsInversedOutputFile);
              //  Future gridsInversedFuture = threadpool.submit(gridsInversedThread);
                
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
                File thumbFile = null;
                for (int thumbInd=0; thumbInd<thumbnailExtractor.numberOfThumbnails;thumbInd++){
                    thumbFile = new File(baseFolder,"Thumbnail" + String.valueOf(thumbInd) + ".png");
                    ImageIO.write(thumbnailExtractor.thumbnails.get(thumbInd), "png", thumbFile);
                    thumbnail.thumbnailList.add(thumbFile.getCanonicalPath());
                }
                          
                report.thumbnailReport =thumbnail;
                ds.save(report);

                while (!dqFuture.isDone() | !noiseDWFuture.isDone() | 
                			!ghostFuture.isDone() | 
                			!elaFuture.isDone() | 
                			!blkFuture.isDone() | 
                			!medianNoiseFuture.isDone() |
                			!gridsFuture.isDone() ){
                    Thread.sleep(100); //sleep for 1 millisecond before checking again
                    if (dqFuture.isDone() & !dqSaved){
                        report.dqReport =(dqReport) dqFuture.get();
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
                    if (gridsFuture.isDone() & !gridsSaved){
                        GridsBothReport gridsBothReport=(GridsBothReport) gridsFuture.get();
                        report.gridsReport = gridsBothReport.gridsNormalReport;
                        report.gridsReport.completed = true;
                        report.gridsInversedReport = gridsBothReport.gridsInversedReport;
                        report.gridsInversedReport.completed = true;
                        gridsSaved =true;
                        ds.save(report);
                        System.out.println("GridsReport Done");
                    }                  
                    if ((System.currentTimeMillis()-startTime) > computationTimeoutLimit){
                    	System.out.println("Computation timed out");
                        outMessage="TIMEDOUT";
                        // kill if timeout
                        dqFuture.cancel(true);
                        noiseDWFuture.cancel(true);
                        ghostFuture.cancel(true);
                        blkFuture.cancel(true);
                        medianNoiseFuture.cancel(true);
                        elaFuture.cancel(true);   
                        gridsFuture.cancel(true);                      
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


    public static ForensicReport getReport(String urlHash, String mongoHostIP) throws UnknownHostException{
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
    
   
    public static ForensicReportBase64 getBase64(String urlHash, String mongoHostIP) throws UnknownHostException{
    	System.out.println("Create base64 for hash " + urlHash);
        MongoClient mongoclient = new MongoClient(mongoHostIP, 27017);
        Morphia morphia = new Morphia();
        morphia.map(ForensicReport.class).map(dqReport.class);
        Datastore ds = new Morphia().createDatastore(mongoclient, "ForensicDatabase");
        ds.ensureCaps();
        ForensicReportBase64 reportBase64 = new ForensicReportBase64();
        ForensicReport report = ds.get(ForensicReport.class, urlHash);
     
        if (report!=null) {        	      	
	        if (report.displayImage!=null){	        	
	        	byte[] displayImageInByte;
				BufferedImage displayImage;
				try {
					displayImage = ImageIO.read(new File(report.displayImage));				
	    			ByteArrayOutputStream displayImagebuffer = new ByteArrayOutputStream();
	    			ImageIO.write(displayImage, "png", displayImagebuffer);
	    			displayImagebuffer.flush();
	    			displayImageInByte = displayImagebuffer.toByteArray();
	    			displayImagebuffer.close();
	    			ScalingReport dataScale = new ScalingReport();
	    				try {	    					
	    					dataScale = scale(displayImageInByte, 500, 500);
	    					String displayImageBase64String = Base64.getEncoder().encodeToString(dataScale.scaledByte);
	    	    			reportBase64.displayImageBase64 = "data:image/jpeg;base64," + displayImageBase64String;	    
	    	    			reportBase64.widthdisplayImage = dataScale.width;
	    				} catch (ApplicationException e) {
	    					// TODO Auto-generated catch block
	    					e.printStackTrace();
	    					System.out.println("Scalling exception");
	    					String displayImageBase64String = Base64.getEncoder().encodeToString(displayImageInByte);
	    	    			reportBase64.displayImageBase64 = "data:image/jpeg;base64," + displayImageBase64String;
	    	    			reportBase64.widthdisplayImage = displayImage.getWidth();
	    				}		    		
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Display image exception");
				}	
	        }
	        if (report.thumbnailReport.numberOfThumbnails>0) {	        	
	            for (int ThumbInd = 0; ThumbInd < report.thumbnailReport.thumbnailList.size(); ThumbInd++) {
	            	try {
		            	byte[] thumbInByte;
						BufferedImage thumbImage;
						thumbImage = ImageIO.read(new File(report.thumbnailReport.thumbnailList.get(ThumbInd)));	
						ByteArrayOutputStream thumbbuffer = new ByteArrayOutputStream();
						ImageIO.write(thumbImage, "png", thumbbuffer);
						thumbbuffer.flush();
						thumbInByte = thumbbuffer.toByteArray();
						thumbbuffer.close();
						ScalingReport thumbScale = new ScalingReport();
						try {
		    					thumbScale = scale(thumbInByte, 500, 500);
		    					String thumbBase64String = Base64.getEncoder().encodeToString(thumbScale.scaledByte);
				    			reportBase64.thumbBase64.add("data:image/jpeg;base64," + thumbBase64String);
				    			reportBase64.widththumb.add(thumbScale.width);
		    				} catch (ApplicationException e) {
		    					// TODO Auto-generated catch block
		    					e.printStackTrace();
		    					System.out.println("Scalling exception");
		    					String thumbBase64String = Base64.getEncoder().encodeToString(thumbInByte);
				    			reportBase64.thumbBase64.add("data:image/jpeg;base64," + thumbBase64String);
				    			reportBase64.widththumb.add(thumbImage.getWidth());
		    				}		    			
	            	} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	                
	            }
	        }
	        if (report.dqReport.completed){
	        		byte[] dqInByte;
	    			BufferedImage dqImage;
					try {
						dqImage = ImageIO.read(new File(report.dqReport.map));				
		    			ByteArrayOutputStream dqbuffer = new ByteArrayOutputStream();
		    			ImageIO.write(dqImage, "png", dqbuffer);
		    			dqbuffer.flush();
		    			dqInByte = dqbuffer.toByteArray();
		    			dqbuffer.close();
		    			ScalingReport dqScale = new ScalingReport();
		    				try {
		    					dqScale = scale(dqInByte, 130, 130);
		    					String dqBase64String = Base64.getEncoder().encodeToString(dqScale.scaledByte);
				    			reportBase64.dqBase64 = "data:image/jpeg;base64," + dqBase64String;
				    			reportBase64.widthdq = dqScale.width;
		    				} catch (ApplicationException e) {
		    					// TODO Auto-generated catch block
		    					e.printStackTrace();
		    					System.out.println("Scalling exception");
		    					String dqBase64String = Base64.getEncoder().encodeToString(dqInByte);
				    			reportBase64.dqBase64 = "data:image/jpeg;base64," + dqBase64String;
				    			reportBase64.widthdq = dqImage.getWidth();
		    				}			    			
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				
	        }
	        
	        if (report.dwNoiseReport.completed){  
	    		byte[] dwNoiseInByte;
				BufferedImage dwNoiseImage;
				try {
					dwNoiseImage = ImageIO.read(new File(report.dwNoiseReport.map));				
	    			ByteArrayOutputStream dwNoisebuffer = new ByteArrayOutputStream();
	    			ImageIO.write(dwNoiseImage, "png", dwNoisebuffer);
	    			dwNoisebuffer.flush();
	    			dwNoiseInByte = dwNoisebuffer.toByteArray();
	    			dwNoisebuffer.close();
	    			ScalingReport dwNoiseScale = new ScalingReport();
	    				try {
	    					dwNoiseScale = scale(dwNoiseInByte, 130, 130);
	    					String dwNoiseBase64String = Base64.getEncoder().encodeToString(dwNoiseScale.scaledByte);
	    	    			reportBase64.dwNoiseBase64 = "data:image/jpeg;base64," + dwNoiseBase64String;
	    	    			reportBase64.widthdwNoise = dwNoiseScale.width;
	    				} catch (ApplicationException e) {
	    					// TODO Auto-generated catch block
	    					e.printStackTrace();
	    					System.out.println("Scalling exception");
	    					String dwNoiseBase64String = Base64.getEncoder().encodeToString(dwNoiseInByte);
	    	    			reportBase64.dwNoiseBase64 = "data:image/jpeg;base64," + dwNoiseBase64String;
	    	    			reportBase64.widthdwNoise = dwNoiseImage.getWidth();
	    				}	    		
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
	        }
	        if (report.elaReport.completed){  
	    		byte[] elaInByte;
				BufferedImage elaImage;
				try {
					elaImage = ImageIO.read(new File(report.elaReport.map));				
	    			ByteArrayOutputStream elabuffer = new ByteArrayOutputStream();
	    			ImageIO.write(elaImage, "png", elabuffer);
	    			elabuffer.flush();
	    			elaInByte = elabuffer.toByteArray();
	    			elabuffer.close();
	    			ScalingReport elaScale = new ScalingReport();
	    				try {
	    					elaScale = scale(elaInByte, 130, 130);
	    					String elaBase64String = Base64.getEncoder().encodeToString(elaScale.scaledByte);
	    	    			reportBase64.elaBase64 = "data:image/jpeg;base64," + elaBase64String;
	    	    			reportBase64.widthela = elaScale.width;
	    				} catch (ApplicationException e) {
	    					// TODO Auto-generated catch block
	    					e.printStackTrace();
	    					System.out.println("Scalling exception");
	    					String elaBase64String = Base64.getEncoder().encodeToString(elaInByte);
	    	    			reportBase64.elaBase64 = "data:image/jpeg;base64," + elaBase64String;
	    	    			reportBase64.widthela = elaImage.getWidth();
	    				}	    			
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
	        }
	        if (report.blockingReport.completed){      		
	    		byte[] blockingInByte;
				BufferedImage blockingImage;
				try {
					blockingImage = ImageIO.read(new File(report.blockingReport.map));				
	    			ByteArrayOutputStream blockingbuffer = new ByteArrayOutputStream();
	    			ImageIO.write(blockingImage, "png", blockingbuffer);
	    			blockingbuffer.flush();
	    			blockingInByte = blockingbuffer.toByteArray();
	    			blockingbuffer.close();
	    			ScalingReport blockingScale = new ScalingReport();
    				try {
    					blockingScale = scale(blockingInByte, 130, 130);
    					String blockingBase64String = Base64.getEncoder().encodeToString(blockingScale.scaledByte);
    	    			reportBase64.blockingBase64 = "data:image/jpeg;base64," + blockingBase64String;
    	    			reportBase64.widthblocking = blockingScale.width;
    				} catch (ApplicationException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    					System.out.println("Scalling exception");
    					String blockingBase64String = Base64.getEncoder().encodeToString(blockingInByte);
    	    			reportBase64.blockingBase64 = "data:image/jpeg;base64," + blockingBase64String;
    	    			reportBase64.widthblocking = blockingImage.getWidth();
    				}	    			
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
	        }
	        if (report.medianNoiseReport.completed){      		
	    		byte[] medianNoiseInByte;
				BufferedImage medianNoiseImage;
				try {
					medianNoiseImage = ImageIO.read(new File(report.medianNoiseReport.map));				
	    			ByteArrayOutputStream medianNoisebuffer = new ByteArrayOutputStream();
	    			ImageIO.write(medianNoiseImage, "png", medianNoisebuffer);
	    			medianNoisebuffer.flush();
	    			medianNoiseInByte = medianNoisebuffer.toByteArray();
	    			medianNoisebuffer.close();
	    			ScalingReport medianNoiseScale = new ScalingReport();
	    			try {
	    				medianNoiseScale = scale(medianNoiseInByte, 130, 130);
	    				String medianNoiseBase64String = Base64.getEncoder().encodeToString(medianNoiseScale.scaledByte);
		    			reportBase64.medianNoiseBase64 = "data:image/jpeg;base64," + medianNoiseBase64String;
		    			reportBase64.widthmedianNoise = medianNoiseScale.width;
    				} catch (ApplicationException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    					System.out.println("Scalling exception");
    					String medianNoiseBase64String = Base64.getEncoder().encodeToString(medianNoiseInByte);
    	    			reportBase64.medianNoiseBase64 = "data:image/jpeg;base64," + medianNoiseBase64String;
    	    			reportBase64.widthmedianNoise = medianNoiseImage.getWidth();
    				}		    		
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
	        }
	        if (report.ghostReport.completed){      		
	    		
				try {
					 for (int GhostInd = 0; GhostInd < report.ghostReport.maps.size(); GhostInd++) {
						 	byte[] ghostInByte;
							BufferedImage ghostImage;
							ghostImage = ImageIO.read(new File(report.ghostReport.maps.get(GhostInd)));	
							ByteArrayOutputStream ghostbuffer = new ByteArrayOutputStream();
							ImageIO.write(ghostImage, "png", ghostbuffer);
							ghostbuffer.flush();
							ghostInByte = ghostbuffer.toByteArray();
			    			ghostbuffer.close();
			    			ScalingReport ghostScale = new ScalingReport();
			    			try {
			    				ghostScale = scale(ghostInByte, 130, 130);
			    				String ghostBase64String = Base64.getEncoder().encodeToString(ghostScale.scaledByte);
				    			reportBase64.ghostBase64.add("data:image/jpeg;base64," + ghostBase64String);
				    			reportBase64.widthghost.add(ghostScale.width);
		    				} catch (ApplicationException e) {
		    					// TODO Auto-generated catch block
		    					e.printStackTrace();
		    					System.out.println("Scalling exception");
		    					String ghostBase64String = Base64.getEncoder().encodeToString(ghostInByte);
				    			reportBase64.ghostBase64.add("data:image/jpeg;base64," + ghostBase64String);
				    			reportBase64.widthghost.add(ghostImage.getWidth());
		    				}				    			
		                }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
	        }
	        
	        if (report.gridsReport.completed){
	    		byte[] gridsInByte, gridsInvInByte;
				BufferedImage gridsImage, gridsInvImage;
				String gridsBase64String, gridsInvBase64String;
				try {
					// GRIDS
					gridsImage = ImageIO.read(new File(report.gridsReport.map));
	    			ByteArrayOutputStream gridsbuffer = new ByteArrayOutputStream();
	    			ImageIO.write(gridsImage, "png", gridsbuffer);
	    			gridsbuffer.flush();
	    			gridsInByte = gridsbuffer.toByteArray();
	    			gridsbuffer.close();
	    			ScalingReport gridsScale = new ScalingReport();
    				try {
    					gridsScale = scale(gridsInByte, 130, 130);
    					gridsBase64String = Base64.getEncoder().encodeToString(gridsScale.scaledByte);
    	    			reportBase64.gridsBase64 = "data:image/jpeg;base64," + gridsBase64String;
    	    			reportBase64.widthgrids = gridsScale.width;
    				} catch (ApplicationException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    					System.out.println("Scalling exception");
    					gridsBase64String = Base64.getEncoder().encodeToString(gridsInByte);
    	    			reportBase64.gridsBase64 = "data:image/jpeg;base64," + gridsBase64String;
    	    			reportBase64.widthgrids = gridsImage.getWidth();
    				}	  
    				
    				// GRIDS INVERSED
    				gridsInvImage = ImageIO.read(new File(report.gridsInversedReport.map));
	    			ByteArrayOutputStream gridsInvbuffer = new ByteArrayOutputStream();
	    			ImageIO.write(gridsInvImage, "png", gridsInvbuffer);
	    			gridsInvbuffer.flush();
	    			gridsInvInByte = gridsInvbuffer.toByteArray();
	    			gridsInvbuffer.close();
	    			ScalingReport gridsInvScale = new ScalingReport();
    				try {
    					gridsInvScale = scale(gridsInvInByte, 130, 130);
    					gridsInvBase64String = Base64.getEncoder().encodeToString(gridsInvScale.scaledByte);
    	    			reportBase64.gridsInversedBase64 = "data:image/jpeg;base64," + gridsInvBase64String;
    	    			reportBase64.widthgridsInversed = gridsInvScale.width;
    				} catch (ApplicationException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    					System.out.println("Scalling exception");
    					gridsInvBase64String = Base64.getEncoder().encodeToString(gridsInvInByte);
    	    			reportBase64.gridsInversedBase64 = "data:image/jpeg;base64," + gridsInvBase64String;
    	    			reportBase64.widthgridsInversed = gridsInvImage.getWidth();
    				}	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
	        }
	        
	    }else{
        	System.out.println("report is empty");
        }
        mongoclient.close();
        return reportBase64;
    }
    
    public static ScalingReport scale(byte[] fileData, int width, int height) throws ApplicationException {
    	ByteArrayInputStream in = new ByteArrayInputStream(fileData);
    	ScalingReport imagedata = new ScalingReport();
    	try {
    		
    		BufferedImage img = ImageIO.read(in);
    		
    		if (img.getHeight() > img.getWidth()){
    			if (img.getHeight() > height){
    				width = (height * img.getWidth())/ img.getHeight();
    				Image scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    	    		BufferedImage imageBuff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    	    		imageBuff.getGraphics().drawImage(scaledImage, 0, 0, new Color(0,0,0), null);

    	    		ByteArrayOutputStream buffer = new ByteArrayOutputStream();    	    		
    	    		ImageIO.write(imageBuff, "jpg", buffer);   
    	    		imagedata.scaledByte =  buffer.toByteArray();
    	    		imagedata.width = width;
    	    		imagedata.height = height;
    			}else {
    				imagedata.scaledByte = fileData;
    				imagedata.width = img.getWidth();
    	    		imagedata.height = img.getHeight();
    			}
    		}else{
    			if (img.getWidth() > width){
    				height = (width * img.getHeight())/ img.getWidth(); 
    				
    				Image scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    	    		BufferedImage imageBuff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    	    		imageBuff.getGraphics().drawImage(scaledImage, 0, 0, new Color(0,0,0), null);

    	    		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    	    		ImageIO.write(imageBuff, "jpg", buffer);
    	    		imagedata.scaledByte =  buffer.toByteArray();
    	    		imagedata.width = width;
    	    		imagedata.height = height;
    			}else{
    				imagedata.scaledByte = fileData;
    				imagedata.width = img.getWidth() ;
    	    		imagedata.height = img.getHeight();
    			}
    		}
    		return imagedata; 
    	} catch (IOException e) {
    		throw new ApplicationException("IOException in scale", null);
    	}
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
            ByteArrayOutputStream dqbytes = new ByteArrayOutputStream();
            ImageIO.write(dqDetector.displaySurface, "png", dqbytes);
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
            ByteArrayOutputStream noisebytes = new ByteArrayOutputStream();
            ImageIO.write(noiseExtractor.displaySurface, "png", noisebytes);
           
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
            byte[] imageInByte; 
            String ghostBase64; // convert to base64 the image file
            GhostReport ghostReport=new GhostReport();
            GhostExtractor ghostExtractor;
            ghostExtractor = new GhostExtractor(sourceFile, maxImageSmallDimension, numThreads);
            BufferedImage ghostMap;
            for (int ghostMapInd=0;ghostMapInd<ghostExtractor.ghostMaps.size();ghostMapInd++) {
                ghostOutputfile=new File(baseFolder, "GhostOutput" + String.format("%02d", ghostMapInd) + ".png");
                ghostMap=ghostExtractor.ghostMaps.get(ghostMapInd);
                ImageIO.write(ghostMap, "png", ghostOutputfile);
                ByteArrayOutputStream ghostbytes = new ByteArrayOutputStream();
                ImageIO.write(ghostMap, "png", ghostbytes);
                imageInByte = ghostbytes.toByteArray();
             
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
            ByteArrayOutputStream elabytes = new ByteArrayOutputStream();
            ImageIO.write(elaExtractor.displaySurface, "png", elabytes);
              
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
            ByteArrayOutputStream blockbytes = new ByteArrayOutputStream();
            ImageIO.write(blockingExtractor.displaySurface, "png", blockbytes);
                  
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
            ByteArrayOutputStream medianNoisebytes = new ByteArrayOutputStream();
            ImageIO.write(medianNoiseExtractor.displaySurface, "png", medianNoisebytes);       
        
            medianNoiseReport.map = outputFile.getCanonicalPath();
            medianNoiseReport.completed=true;
            return medianNoiseReport;
        }
    }
    
    private static class GridsThread implements Callable {
        String sourceFile ="";
        File outputFileGI =null;
        File outputFileG= null;
        public GridsThread(String sourceFile,File outputFileG, File outputFileGI){
            this.sourceFile =sourceFile;
            this.outputFileG = outputFileG;
            this.outputFileGI = outputFileGI;
        }
        @Override
        public GridsBothReport call() {
        	GridsBothReport output=null;
            try {
                output= gridsCalculation();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }
        public GridsBothReport gridsCalculation() throws IOException {
        	GridsBothReport gridsBothReport =new GridsBothReport();
        	GridsExtractor gridsExtractor;
        	gridsExtractor = new GridsExtractor(sourceFile);
            ImageIO.write(gridsExtractor.displaySurfaceG, "png", outputFileG);
            ByteArrayOutputStream gridsbytes = new ByteArrayOutputStream();
            ImageIO.write(gridsExtractor.displaySurfaceG, "png", gridsbytes);
            
            ImageIO.write(gridsExtractor.displaySurfaceGI, "png", outputFileGI);
            ByteArrayOutputStream gridsInvbytes = new ByteArrayOutputStream();
            ImageIO.write(gridsExtractor.displaySurfaceGI, "png", gridsInvbytes);
                  
            gridsBothReport.mapG = outputFileG.getCanonicalPath();
            gridsBothReport.mapGI = outputFileGI.getCanonicalPath();
            gridsBothReport.maxValueG = gridsExtractor.gridsmaxG;
            gridsBothReport.minValueG = gridsExtractor.gridsminG;
            gridsBothReport.maxValueG = gridsExtractor.gridsmaxGI;
            gridsBothReport.minValueG = gridsExtractor.gridsminGI;
            gridsBothReport.completed=true;

            gridsBothReport.gridsNormalReport.map=outputFileG.getCanonicalPath();
            gridsBothReport.gridsNormalReport.maxValue = gridsExtractor.gridsmaxG;
            gridsBothReport.gridsNormalReport.minValue = gridsExtractor.gridsminG;
            gridsBothReport.gridsNormalReport.completed=true;

            gridsBothReport.gridsInversedReport.map=outputFileGI.getCanonicalPath();
            gridsBothReport.gridsInversedReport.maxValue = gridsExtractor.gridsmaxGI;
            gridsBothReport.gridsInversedReport.minValue = gridsExtractor.gridsminGI;
            gridsBothReport.gridsInversedReport.completed=true;
            return gridsBothReport;
        }
    }
    

    public static void main (String[] args) throws IOException {
    	
    	if (args.length == 1){			
			url = args[0];					
		}else{
			System.out.println("Wrong number of arguments");
			url ="http://160.40.50.109:8080/example6_big.jpg";
		}
       // String OutputFolder = "/home/marzampoglou/Pictures/Reveal/ManipulationOutput/";
       // String Hash1=downloadURL("http://160.40.51.26/projects/Reveal/imgs/example6_big.jpg", OutputFolder, "127.0.0.1");
       // String OutputFolder = "D:\\Reveal\\image-forensics-local-data\\ManipulationOutput\\";
        // String Hash1=downloadURL(url, OutputFolder, "127.0.0.1");
        //System.out.println("OutputFolder " + OutputFolder);
        //System.out.println("Hash1 " + Hash1);
     //  createReport(Hash1, "127.0.0.1", OutputFolder);
    }

    public static String getMeta(String path) {
        // Get the metadata from a local file
        // This code is used in certain side projects
        // and is not part of the main REVEAL functionalities
        String Hash1;

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
