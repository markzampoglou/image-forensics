package gr.iti.mklab.reveal.forensics.maps.grids;

import javax.imageio.ImageIO;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import gr.iti.mklab.reveal.forensics.util.Util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by olgapapa on 06/21/15.
 */
public class GridsExtractor {

    public BufferedImage displaySurfaceG = null, displaySurfaceG_temp = null;
    public BufferedImage displaySurfaceGI = null, displaySurfaceGI_temp = null;
    public double gridsminG;
    public double gridsmaxG;
    public double gridsminGI;
    public double gridsmaxGI;
    
    // Number of grids
    //public static int sgrid = 3;
    public static int vgaThres = 307200;
    public static int sc_width = 600;
    public static int sc_height = 600;
    
    // number of bins 
    public static int bins = 40;
    // Number of blocks 
    public static int nblocks = 8;
    // This threshold is used to remove extremely strong edges:
    // block edges are definitely going to be weak
    int diffThresh =50;
    // Accumulator size. Larger may overcome small splices, smaller may not
    // aggregate enough.
    int accuSize =33;
    
    public static int sgrid = 1;

    public GridsExtractor(String fileName) throws IOException {
    	getGrids(fileName);
    }
    
    public  int calcGrid(int vga){
    	  int sgrids = 2;
    	  if (vga >= vgaThres){
          	sgrids = 3;
          } else if ((2d/3d) * vgaThres <= vga){
          	sgrids = 3;
          } 
		return sgrids;   	
    }
    
    private void getGrids(String fileName) throws IOException {
    	
        BufferedImage origImage;
        origImage = ImageIO.read(new File(fileName));         
        int ImW = origImage.getWidth();
        int ImH = origImage.getHeight();          
        int vga = ImW * ImH;
        sgrid = calcGrid(vga);
        Color tmpColor;
        int[][][] rgbValues = new int[3][ImW][ImH];
        float[] hsb;
        double[][][] hsbValues = new double[3][ImW][ImH];  
        double[] YCbCr;
        double[][][] YCbCrValues = new double[3][ImW][ImH];       
        for (int ii = 0; ii < ImW; ii++) {
            for (int jj = 0; jj < ImH; jj++) {
            	// Get rgb values
            	tmpColor = new Color(origImage.getRGB(ii, jj));
                rgbValues[0][ii][jj] = tmpColor.getRed();
                rgbValues[1][ii][jj] = tmpColor.getGreen();
                rgbValues[2][ii][jj] = tmpColor.getBlue();              
                // conver to HSB
                hsb = Color.RGBtoHSB(rgbValues[0][ii][jj], rgbValues[1][ii][jj], rgbValues[2][ii][jj] , null);
                hsbValues[0][ii][jj] = hsb[0]; // hue  
                hsbValues[1][ii][jj] = hsb[1]; // saturation  
                hsbValues[2][ii][jj] = hsb[2] * 255; // brightness ** multiple * 255 in order to be compatible with matlab              
                // Convert RGB to YCbCr
                YCbCr = Util.RGBtoYCbCr(rgbValues[0][ii][jj], rgbValues[1][ii][jj], rgbValues[2][ii][jj]);
                YCbCrValues[0][ii][jj] = YCbCr[0]; // Y 
                YCbCrValues[1][ii][jj] = YCbCr[1]; // Cb  
                YCbCrValues[2][ii][jj] = YCbCr[2]; // Cr            
            }           
        }   
               
		// calculate blocks size
        int blk_idx = (int) Math.ceil((ImH/nblocks)-1);        		
        int blk_idy = (int) Math.ceil((ImW/nblocks)-1);        	        
        int kx = (int) Math.ceil(blk_idx/sgrid);
        int ky = (int) Math.ceil(blk_idy/sgrid);     
        double Kscores[][] = new double[nblocks + 1][nblocks + 1];
        int KscoresC[][] = new int[nblocks + 1][nblocks + 1];
        double Kpre[][] = new double[nblocks][nblocks];        
     
        Object[] inblock = null;        
        int countblocks = 0;        
        inblock = GridsCalculate.inblockpatterns2(YCbCrValues, 8, blk_idx, blk_idy, countblocks, bins);   
        Kscores = (double[][]) inblock[0];
        KscoresC = (int[][]) inblock[1];       
            
        // predict: input KscoresC (0,1 array) sum rows and column
        for (int r = 0; r < nblocks; r++){
        	KscoresC[r][nblocks] = Util.getRowTotalInt(KscoresC, r);        	
        	 for (int c=0; c < nblocks; c++){   
        		if (r == 0){ 
        			KscoresC[nblocks][c] = Util.getColumnTotalInt(KscoresC,  c);  
        		}        		
        	 	Kpre[r][c] = (double) (KscoresC[r][nblocks]  + KscoresC[nblocks][c]) / (double) 16;
        	 }
        }
        
        // Calculate possible points
        List<PossiblePoints> PossiblePointslist = GridsCalculate.predictPossiblePoints(Kscores, KscoresC, Kpre);       
        Collections.sort(PossiblePointslist, new Comparator<PossiblePoints>() {
            public int compare(PossiblePoints u1, PossiblePoints u2) {
              return Double.valueOf(u1.fit).compareTo(Double.valueOf(u2.fit));
            }
          });   
        
         Object[] AvgOutput = MainTrain.extract(origImage, blk_idx, blk_idy);
         double[][] meanContent = (double[][]) AvgOutput[0];
         double[][] MeanStrongEdge = (double[][]) AvgOutput[1];
        
         	double[][] MeanContent2 =  new double[kx][ky];	        
	        int a,b,ccc;
	        double sum_mean2;
	        for (int i=0;i<kx;i++){
				for (int j=0;j<ky;j++){
	        		      a=(i)*sgrid;
	        		      b=(j)*sgrid;
	        		      ccc=sgrid;	        		      
	        		      double[] temp_array = new double[sgrid*sgrid];
	                      int temp_count = 0;
	                      sum_mean2 = 0;
	                      for (int mm=a;mm<a + ccc;mm++){
	                     	 for (int nn = b; nn < b + ccc; nn++){                    		
	                     		 temp_array[temp_count] = meanContent[mm][nn];
	                     		 sum_mean2 = sum_mean2 + temp_array[temp_count];
	                       		 temp_count = temp_count + 1;
	                     	 }       
	                      }    
	                      MeanContent2[i][j]= sum_mean2 / (double) (temp_count); 
	             	}		
	        }	        
	        
	   // Calculate bg - different between grids and inversed grids
	     	int bg_gridsInv = 0;	    		
	    	PossiblePoints posPointss, posPoints1;
		    	int p1, p2;
		    	double conf;    		
	    		for (int f=0; f < 16; f++){			    		
	    			posPointss = PossiblePointslist.get(f);
		    		p1 = posPointss.getApoint1();
		    		p2 = posPointss.getApoint2();
			    		if ((p1 == 3) && (p2 == 3)){
			    			bg_gridsInv = f;
			    		}
		    	}		    		
	    		for (int f=0; f < 16; f++){	
	    			if (bg_gridsInv == 0){
			    		double max = 0;
			    		for (int ff=0; ff < 16; ff++){
				    		posPoints1 = PossiblePointslist.get(ff);
			    			conf = posPoints1.getConfScore(); 			    			
			    			if (conf > max){
			    				max = conf;
			    				bg_gridsInv = ff;
			    			}				    		
			    		}		
	    			}	   		
	    		}    
	    		
       Object[] scores_pick_variables_temp =  GridsCalculate.scores_pick_variables(inblock, sgrid, blk_idx, blk_idy, PossiblePointslist, kx, ky, bg_gridsInv, nblocks);  
 
       double MeanInSpace[][][] =  new double[16][kx][ky];
       double[][] diff_Mean_Best_scale_grids =  new double[kx][ky];    
       double[][] diff_Mean_Best_scale_gridsInv =  new double[kx][ky]; 
       
       MeanInSpace = (double[][][]) scores_pick_variables_temp[0]; 
       diff_Mean_Best_scale_grids = (double[][]) scores_pick_variables_temp[1]; 
       diff_Mean_Best_scale_gridsInv = (double[][]) scores_pick_variables_temp[2]; 
       
       Object[] result = GridsCalculate.characterizeblocks(MeanContent2, MeanStrongEdge, hsbValues, blk_idx,blk_idy, MeanInSpace, diff_Mean_Best_scale_grids, diff_Mean_Best_scale_gridsInv, PossiblePointslist, kx, ky, sgrid);
       
       double[][] gridsResChar = new double[kx][ky];
       double[][] gridsInversedResChar = new double[kx][ky];
       gridsResChar = (double[][]) result[0];
       gridsInversedResChar = (double[][]) result[1];
       
       double[][] gridsResCharScaled = GridsCalculate.RescaleToImageResult(gridsResChar, kx, ky,  ImW, ImH, sgrid);
       double[][] gridsInversedResCharScaled = GridsCalculate.RescaleToImageResult(gridsInversedResChar, kx, ky,  ImW, ImH, sgrid);
       
       double[][] gridsResCharNorm =  Util.normalizeIm(gridsResCharScaled);
       double[][] gridsInversedResCharNorm =  Util.normalizeIm(gridsInversedResCharScaled);
       
       double[][] gridsResCharCW = GridsCalculate.transpose(gridsResCharNorm, ImW, ImH);
       double[][] gridsInversedResCharCW = GridsCalculate.transpose(gridsInversedResCharNorm, ImW, ImH);       
           
       displaySurfaceG_temp = Util.visualizeWithJet(gridsResCharCW);
       displaySurfaceGI_temp = Util.visualizeWithJet(gridsInversedResCharCW);
       
       // scale result in order to save time
       // The scaled map is same as the original
       if (displaySurfaceG_temp.getHeight() > displaySurfaceG_temp.getWidth()){
			if (displaySurfaceG_temp.getHeight() > sc_height){
				sc_width = (sc_height * displaySurfaceG_temp.getWidth())/ displaySurfaceG_temp.getHeight();
				displaySurfaceG = Util.scaleImage(displaySurfaceG_temp, sc_width, sc_height);
				displaySurfaceGI = Util.scaleImage(displaySurfaceGI_temp, sc_width, sc_height);
			}else{
				displaySurfaceG = displaySurfaceG_temp;
				displaySurfaceGI = displaySurfaceGI_temp;
			}
		}else{
			if (displaySurfaceG_temp.getWidth() > sc_width){
				sc_height = (sc_width * displaySurfaceG_temp.getHeight())/ displaySurfaceG_temp.getWidth(); 
				displaySurfaceG = Util.scaleImage(displaySurfaceG_temp, sc_width, sc_height);	
				displaySurfaceGI = Util.scaleImage(displaySurfaceGI_temp, sc_width, sc_height);
			}else{
				displaySurfaceG = displaySurfaceG_temp;
				displaySurfaceGI = displaySurfaceGI_temp;
			}
		}       
       gridsmaxG =Util.maxDouble2DArray(gridsResCharScaled);
       gridsminG =Util.minDouble2DArray(gridsResCharScaled);
       gridsmaxGI =Util.maxDouble2DArray(gridsInversedResCharScaled);
       gridsminGI =Util.minDouble2DArray(gridsInversedResCharScaled);
              
  }    
	 
   static class PossiblePoints {
    	public int Apoint1;
    	public int Apoint2;
    	public int Epoint1;
    	public int Epoint2;
    	
    	public double ConfScore;    	
    	public double fit;
    	public double meanfit;
	    
	    
	    public PossiblePoints(int Apoint1, int Apoint2,
	    		int Epoint1, int Epoint2,
	    						double ConfScore, double fit, double meanfit) {
	    	this.Apoint1 = Apoint1;
	    	this.Apoint2 = Apoint2;
	    	this.Epoint1 = Epoint1;
	    	this.Epoint2 = Epoint2;
	    	this.ConfScore = ConfScore;
	    	this.fit = fit;
	    	this.meanfit = meanfit;
	    	
	    }
	    
	    public int getApoint1() {
			return Apoint1;
		}
	    
	    public int getApoint2() {
			return Apoint2;
		}
	    
	    public int getEpoint1() {
			return Epoint1;
		}
	    
	    public int getEpoint2() {
			return Epoint2;
		}
	    
	    public Double getmeanfit() {
			return meanfit;
		}
	    
	    public Double getfit() {
			return fit;
		}
	    
	    public Double getConfScore() {
			return ConfScore;
		}

	    public String toString() {
		return "Apoint1 = " + this.Apoint1 + ", Apoint2 = " + this.Apoint2 + ", Epoint1 = " + this.Epoint1
				+ ", Epoint2 = " + this.Epoint2 + ", ConfScore = " + this.ConfScore + ", fit = " + this.fit + ", meanfit = " + this.meanfit;
	    }	   
	} 
    
    
 
  
    
    public static void main (String [] args)
            throws Exception {	
    	 //String fileName = "D:/Reveal/Grids/InvforOlga/example16_big.jpg";    	
    	 Long startTime=System.currentTimeMillis();
    /*	 String root_path = "D:/Reveal/Grids/EvalExample/";
    	 String filename = root_path + "example.txt";
    	 Path file = Paths.get(filename);
    	 Stream<String> lines = Files.lines( file, StandardCharsets.UTF_8 );	
    	 int counter = 0;
         for( String line : (Iterable<String>) lines::iterator )
         {	
        	 counter = counter + 1;
        	 System.out.println("Image : " + line + " " + counter);
        	 getGrids(root_path + line);
        	 
         }*/
    	 Long endTime=System.currentTimeMillis();
    	 System.out.println("Time elapsed:: " + (endTime - startTime));
    }
}
