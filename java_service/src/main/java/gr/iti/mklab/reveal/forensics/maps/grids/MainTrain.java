package gr.iti.mklab.reveal.forensics.maps.grids;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

import gr.iti.mklab.reveal.forensics.util.GridsMaps;

/**
 * Created on 30/3/2015.
 */
public class MainTrain  {    
    
	public static int scale = 1;
	public static double[][] averageMatrixE;
	public static double[][] averageMatrixContours;

    public static Object[] extract(BufferedImage image, int  blk_idx, int blk_idy) throws IOException {
    	
    	int originalWidth =  image.getWidth();
		int originalHeight = image.getHeight();
    		
        int width = 600;
        int height = 600;

        image = scaleImage(image, width, height);
        image = get8BitRGBImage(image);

        GridsMaps myPMasks = new GridsMaps();

        double[][] smapF = createSmap(image, myPMasks.getPmasks(), myPMasks.getMaskWhite());
        double[][] ThresSmall = filterF(smapF);
        double ThresImg = 0;
        double[] ThresBig = new double[6];
       
        Arrays.fill(ThresBig, 0d);
        for (int ii = 0; ii < ThresSmall.length; ii++) {
            for (int jj = 0; jj < ThresSmall[ii].length; jj++) {
                ThresSmall[ii][jj] /= 100.0;
                ThresBig[ii] += ThresSmall[ii][jj];
            }
            ThresBig[ii] /= 6;
            ThresImg += ThresBig[ii];
        }
        ThresImg /= 6;    
        
        double[][] smapfinal = filteringMethodF(smapF, ThresSmall, ThresBig, ThresImg);
           	Object[] tempPaintingEdges = PaintimgEdges(smapfinal, myPMasks.getMMasks(), scale);
        	double[][] e = (double[][]) tempPaintingEdges[0];
        			//int[][] edge = (int[][]) tempPaintingEdges[1];
        	double[][] contours = (double[][]) tempPaintingEdges[2];
           	double[][] eResized = arrayresize(e,  originalWidth, originalHeight);
           			//	double[][] edgeResized = arrayresize(edge,  originalWidth, originalHeight);
        	double[][] contoursResized = arrayresize(contours,  originalWidth, originalHeight);
        	 
        	averageMatrixE = new double[blk_idx][blk_idy]; 
        	averageMatrixContours = new double[blk_idx][blk_idy]; 
	      
	      int x_step= 0 ;
	      for (int i=0; i< blk_idx; i++){
	    	  int y_step = 0;
	    	  for (int j = 0; j < blk_idy; j ++){	    		 
	    		  averageMatrixE[i][j] = cellNeighborsAverage(eResized, y_step, x_step, 8, 0);
	    		  averageMatrixContours[i][j] = cellNeighborsAverage(contoursResized, y_step, x_step, 8, 1);
	    		  y_step = y_step + 8;	    	
	    	  }
	    	  x_step = x_step + 8;	    	 
	      }
	      return new Object[]{averageMatrixE, averageMatrixContours};
	    }
    
    private static double cellNeighborsAverage(double[][] matrix, int tempcol, int temprow, int step, int binary)
    {
        // Ignore center cell And tack Neighbors 
    	double sum = 0;
    	double avg = 0;
    	int totalelements =  step*step;
    	for (int k= temprow; k < temprow + step; k++ ){
    		for (int l = tempcol; l <tempcol + step; l++){
    			sum = sum + matrix[k][l];
    		}    		
    	}    	
    	avg =  sum / (double) totalelements;   
    	if ((avg > 0.5) && (binary == 1 )){
    		return 1;
    	}else if ((avg <= 0.5) && (binary == 1 )){
    		return 0;
    	}else{
    		return avg;
    	}
    }

        
   public static double[][] arrayresize(int[][] ar, int widthsc, int heightsc) throws IOException{
      	
	    	double[][] resizedarray = new double[heightsc][widthsc];
	       	Object[] temp_h;
	    	Object[] temp_w;    	
      		temp_w = calculateindecesAndWeights( widthsc, ar.length);
    		temp_h = calculateindecesAndWeights( heightsc, ar.length);    		
    		int[][] indecesw = (int[][]) temp_w[0];        	
        	double[][] weightsw = (double[][]) temp_w[1];        	
        	int[][] indecesh = (int[][]) temp_h[0];        	
        	double[][] weightsh = (double[][]) temp_h[1];        	
        	for (int i =0 ; i < heightsc; i++){
	    		for (int j =0; j< widthsc; j++){	    			
	    			resizedarray[i][j] = ar[indecesh[i][1]-1][indecesw[j][1]-1];
	    		}
	    	}  	
    	return resizedarray;  	
    }
    
    public static double[][] arrayresize(double[][] ar, int widthsc, int heightsc) throws IOException{
    	
		  	double[][] resizedarray = new double[heightsc][widthsc];
	       	Object[] temp_h;
	    	Object[] temp_w;       
    		temp_w = calculateindecesAndWeights( widthsc, ar.length);
    		temp_h = calculateindecesAndWeights( heightsc, ar.length);    		
    		int[][] indecesw = (int[][]) temp_w[0];        	
        	double[][] weightsw = (double[][]) temp_w[1];        	
        	int[][] indecesh = (int[][]) temp_h[0];        	
        	double[][] weightsh = (double[][]) temp_h[1];        	
        	for (int i =0 ; i < heightsc; i++){
	    		for (int j =0; j< widthsc; j++){	    			
	    			resizedarray[i][j] = ar[indecesh[i][1]-1][indecesw[j][1]-1];	    		
	    		}
	    	}  	
    	return resizedarray;	
    }
    
    public static Object[] calculateindecesAndWeights(int dimenstion, int orig_dim ){
    	
    	double[] u = new double[dimenstion];
    	int[] left = new int[dimenstion];
    	int temp;
    	double temp2, tempw1, tempw2, tempw3, sumweights;
    	int kernel_width = 1;
    	
     	double scaleh =  (double) dimenstion / orig_dim ;
    	
    	for (int x = 0; x< dimenstion; x++){
    		temp = x + 1;    	
    		u[x] = temp/scaleh + 0.5 * (1 - 1/scaleh);
    		temp2 = (double) (u[x] - kernel_width/2d);
    		left[x] = (int) Math.floor(temp2);
    	}
       	
    	int[][] indeces = new int[dimenstion][3];    	
    	double[][] weights = new double[dimenstion][3];
    	
    	for (int k = 0; k < dimenstion; k++){    		
    		indeces[k][0] = left[k];
    		indeces[k][1] = indeces[k][0] + 1;
    		indeces[k][2] = indeces[k][0] + 2;    	 		
    		tempw1 = u[k] - indeces[k][0];
    		tempw2 = u[k] - indeces[k][1];
    		tempw3 = u[k] - indeces[k][2];
    		
    		if (indeces[k][0] < 1){
    			indeces[k][0] = 1;
    		}else if (indeces[k][0] >orig_dim){
    			indeces[k][0] = orig_dim - 1;
    		}   			
    		
    		if (tempw1 >= (-0.5) && (tempw1 < 0.5)){
    			weights[k][0] = 1;
    		}else{
    			weights[k][0] = 0;
    		}
    		if (tempw2 >= (-0.5) && (tempw2 < 0.5)){
    			weights[k][1] = 1;
    		}else{
    			weights[k][1] = 0;
    		}
    		if (tempw3 >= (-0.5) && (tempw3 < 0.5)){
    			weights[k][2] = 1;
    		}else{
    			weights[k][2] = 0;
    		}
    		
    		sumweights = weights[k][0] + weights[k][1] + weights[k][2];
    		weights[k][0] = (double) weights[k][0]/sumweights;
    		weights[k][1] = (double) weights[k][1]/sumweights;
    		weights[k][2] = (double) weights[k][0]/sumweights;
    		
    	}   
    	return new Object[]{indeces, weights};  
    }
    
    private static Object[] PaintimgEdges(double[][] smapfinal , int[][][] mmask, int scale){
    	double[][] e = new double[600][600];
    	double[][] contours = new double[600][600];
    	int[][] edge = new int[600][600];
    	 int stepX = 0, blocks = 0, countx = 0, i = 0, j = 0, maskid;
    	
    	 if (scale==1){ 
    		 	 blocks=3600;
    		     stepX=60;
    	 }    	 
    	 for (int a=0;a<stepX;a++){
    		 for (int b=0;b<stepX;b++){
    			 i = 0;
    				for (int x= a*10; x < a*10+10; x++){	
    					j = 0;
    					for (int y= b*10; y < b*10+10; y++){
    						maskid = (int) smapfinal[countx][0];    						
    						edge[x][y] = mmask[maskid][i][j];
    						if (maskid == 58){
    							contours[x][y] = 0;
    						}else{
    							contours[x][y] = 1;
    						}
    						e[x][y] = smapfinal[countx][1];    						
    						 j = j + 1;
    					}    					
    					i = i + 1;
    				}       				
    				countx = countx + 1;
    		 }    		 
    	 }    	
    	 return new Object[]{e, edge, contours};
    }
    
    private static double[][] createSmap(BufferedImage img, int[][][] pmasks, int[] whiteMasks) throws IOException{
    	
        int width = img.getWidth();
        int height = img.getHeight();

        double[][] smap = new double[(height / 10) * (width / 10)][2];
        int pixel, i, j, winMask, counter = 0;
        double w, maxR, TempW, TempB;
        int[][] myTile = new int[10][10];
        for (int a = 0; a < height; a += 10) {
            for (int b = 0; b < width; b += 10) {
                i = 0;
                for (int x = a; x < a + 10; x++) {
                    j = 0;
                    for (int y = b; y < b + 10; y++) {
                        pixel = img.getRGB(y, x);
                        myTile[j][i] = (int)Math.round(0.299d * ((pixel >> 16) & 0xff) + 0.5870d * ((pixel >> 8) & 0xff) + 0.1140d * ((pixel) & 0xff));
                         j++;
                    } 
                    i++;
                }
                
                maxR = 0;
                winMask = 58;
                for (int k = 0; k < 58; k++) {
                    TempW = 0;
                    TempB = 0;
                    for (int x = 0; x < 10; x++) {
                        for (int y = 0; y < 10; y++) {
                            if (pmasks[k][x][y] > 0)
                                TempW += myTile[x][y];
                            else
                                TempB += myTile[x][y];
                        }
                    }
                    TempW /= whiteMasks[k];
                    TempB /= (100 - whiteMasks[k]);
                    w = ((Math.abs(TempW - TempB) * 100) / 255.0);
                    if (w > maxR){
                        maxR = w;
                        winMask = k;
                    }
                }
                smap[counter][0] = winMask;
                smap[counter][1] = maxR;
                counter++;
            }
        }
        return smap;
    }

    private static double[][] filterF(double[][] smapF){
        int blocks = smapF.length;
        int blocks6 = blocks/6;
        int step = (int) Math.sqrt(blocks);
        int step6 = step / 6;
        int z, start, end;
        double[][] smallAreas = new double[6][6];
        for (int a = 0; a < 6; a++) {
            start = a * blocks6;
            end = start + blocks6;
            for (int x = start; x < end; x+=step) {
                for (int y = 0; y < step6; y++) {
                    z = x + y;
                    if (a < 3){
                        smallAreas[0][a*2] += smapF[z][1];
                        smallAreas[0][a*2 + 1] += smapF[z + step6][1];

                        smallAreas[1][a*2] += smapF[z + 2*step6][1];
                        smallAreas[1][a*2 + 1] += smapF[z + 3*step6][1];

                        smallAreas[2][a*2] += smapF[z + 4*step6][1];
                        smallAreas[2][a*2 + 1] += smapF[z + 5*step6][1];
                    } else {
                        smallAreas[3][(a-3)*2] += smapF[z][1];
                        smallAreas[3][(a-3)*2 + 1] += smapF[z + step6][1];

                        smallAreas[4][(a-3)*2] += smapF[z + 2*step6][1];
                        smallAreas[4][(a-3)*2 + 1] += smapF[z + 3*step6][1];

                        smallAreas[5][(a-3)*2] += smapF[z + 4*step6][1];
                        smallAreas[5][(a-3)*2 + 1] += smapF[z + 5*step6][1];
                    }
                }
            }
        }

        return smallAreas;
    }

    private static double[][] filteringMethodF(double[][] smap, double[][] ThresSmall, double[] ThresBig, double ThresImg){
        int blocks = smap.length;
        int blocks6 = blocks/6;
        int step = (int) Math.sqrt(blocks);
        int step6 = step/6;
    
        for (int x = 0; x < 6; x++) {
            if ((ThresBig[x] < ThresImg) && (ThresImg<10)){
                ThresBig[x] = ThresImg; 
            } else {
               if  ((ThresBig[x]>= ThresImg) && (ThresImg<5)){
			        ThresBig[x] = 5; 
			   }
            }

            for (int y = 0; y < 6; y++) {
                if (ThresSmall[x][y] < ThresBig[x]) {
                    if (ThresBig[x] < 5) {
                        ThresSmall[x][y] = ThresBig[x] + 1;
                    } else {
                        ThresSmall[x][y] = ThresBig[x];
                    }
                }
            }
        }
        int start, end, k, l;
        for (int a = 0; a < 6; a++) {
            start = a * blocks6;
            end = start + blocks6;
            for (int x = start; x < end; x+=60) {
                for (int y = 0; y < step6; y++) {
                    k = x + y;
                    if (a < 3) {
                        l = a * 2;
                        if (smap[k][1]  < ThresSmall[0][l])
                            smap[k][0]  = 58;
                        if (smap[k + step6][1]  < ThresSmall[0][l + 1])
                            smap[k + step6][0]  = 58;
                        if (smap[k + 2*step6][1]  < ThresSmall[1][l])
                            smap[k + 2*step6][0]  = 58;
                        if (smap[k + 3*step6][1]  < ThresSmall[1][l + 1])
                            smap[k + 3*step6][0]  = 58;
                        if (smap[k + 4*step6][1]  < ThresSmall[2][l])
                            smap[k + 4*step6][0]  = 58;
                        if (smap[k + 5*step6][1]  < ThresSmall[2][l + 1])
                            smap[k + 5*step6][0]  = 58;
                    } else {
                        l = (a - 3) * 2;
                        if (smap[k][1] < ThresSmall[3][l])
                            smap[k][0] = 58;
                        if (smap[k + step6][1] < ThresSmall[3][l + 1])
                            smap[k + step6][0] = 58;
                        if (smap[k + 2*step6][1] < ThresSmall[4][l])
                            smap[k + 2*step6][0]  = 58;
                        if (smap[k + 3*step6][1] < ThresSmall[4][l + 1])
                            smap[k + 3*step6][0]  = 58;
                        if (smap[k + 4*step6][1] < ThresSmall[5][l])
                            smap[k + 4*step6][0]  = 58;
                        if (smap[k + 5*step6][1] < ThresSmall[5][l + 1])
                            smap[k + 5*step6][0]  = 58;
                    }
                }
            }
        }
        return smap;
    }
   
    public static BufferedImage scaleImage(BufferedImage image, int maxSideLength) {
        assert (maxSideLength > 0);
        double originalWidth = image.getWidth();
        double originalHeight = image.getHeight();
        double scaleFactor = 0.0;
        if (originalWidth > originalHeight) {
            scaleFactor = ((double) maxSideLength / originalWidth);
        } else {
            scaleFactor = ((double) maxSideLength / originalHeight);
        }
        if (scaleFactor < 1 && (int) Math.round(originalWidth * scaleFactor) > 1 && (int) Math.round(originalHeight * scaleFactor) > 1) {
            BufferedImage img = new BufferedImage((int) Math.round(originalWidth * scaleFactor), (int) Math.round(originalHeight * scaleFactor), image.getType());
            Graphics g = img.getGraphics();
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, img.getWidth(), img.getHeight(), null);
            return img;
        } else
            return image;
    }
 
    public static BufferedImage scaleImage(BufferedImage image, int width, int height) {
        assert (width > 0 && height > 0);
        // create image of new size
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // fast scale (Java 1.4 & 1.5)
        Graphics g = img.getGraphics();
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, img.getWidth(), img.getHeight(), null);
        return img;
    }
    
    /**
     * Check if the image is fail safe for color based features that are actually using 8 bits per pixel RGB.
     *
     * @param bufferedImage
     * @return
     */
    public static BufferedImage get8BitRGBImage(BufferedImage bufferedImage) {
        // check if it's (i) RGB and (ii) 8 bits per pixel.
        if (bufferedImage.getType() != ColorSpace.TYPE_RGB || bufferedImage.getSampleModel().getSampleSize(0) != 8) {
            BufferedImage img = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            img.getGraphics().drawImage(bufferedImage, 0, 0, null);
            bufferedImage = img;
        }
        return bufferedImage;
    }
    
    /* @Override
    public byte[] getByteArrayRepresentation() {
        return SerializationUtils.toByteArray(feature);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in) {
        feature = SerializationUtils.toDoubleArray(in);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        feature = SerializationUtils.toDoubleArray(in, offset, length);
    }

    @Override
    public double[] getFeatureVector() {
        return feature;
    }

    @Override
    public double getDistance(LireFeature f) {
        if (!(f instanceof ACCID)) throw new UnsupportedOperationException("Wrong descriptor.");
        return MetricsUtils.jsd(feature, ((ACCID) f).feature);
    }

    @Override
    public String getFeatureName() {
        return "ACCID";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_ACCID;
    } */
}
