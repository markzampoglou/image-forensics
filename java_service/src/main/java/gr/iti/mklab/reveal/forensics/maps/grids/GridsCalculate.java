package gr.iti.mklab.reveal.forensics.maps.grids;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import gr.iti.mklab.reveal.forensics.maps.grids.GridsExtractor.PossiblePoints;
import gr.iti.mklab.reveal.forensics.util.Util;



public class GridsCalculate {
	
	
	/**
	 * Scale resulted array to original image size
	 * @param final_result
	 * @param kx
	 * @param ky
	 * @param ImW
	 * @param ImH
	 * @param sgrid
	 * @return
	 * @throws IOException
	 */
	public static double[][] RescaleToImageResult(double[][] final_result, int kx, int ky, int ImW, int ImH, int sgrid) throws IOException{	
		
		 double[][] result =  new double[ImH][ImW];
		 int a,b;
		 double[][] final_result_temp =  final_result.clone();
	     int nn = 0, mm = 0;
		 for (int x=0; x < kx; x++){
				for (int y=0; y < ky; y++){	
					 a=(x)*(sgrid*8);
				     b=(y)*(sgrid*8);
				     for ( mm=a;mm < a + sgrid*8; mm++){				    	
                   	 for (nn = b; nn < b + sgrid*8; nn++){                       		 
                   		 	result[mm][nn] =  final_result_temp[x][y];                     		 
                   	 }
				     }					
				}
		}					 
		RealMatrix rm = new Array2DRowRealMatrix(result);
    	double[] resultCol = rm.getColumn(nn - 1);	    
    	int cntt = 0;
		 
		 for (int x=nn; x < ImW; x++){
			 cntt = cntt + 1;			 
				for (int y=0; y < ImH; y++){						
					result[y][x] =  resultCol[y];
				}
		 }		
		 RealMatrix rm1 = new Array2DRowRealMatrix(result);
		 double[] resultRow = rm1.getRow(mm - 1);		 
		 for (int x = mm; x < ImH; x++){
				for (int y = 0; y < ImW; y++){	
					result[x][y] =  resultRow[y];
				}
		 } 
		 return result;
	}
	
	/**
	 * Symmetric filter
	 * @param valuearray
	 * @param filter
	 * @return
	 * @throws IOException
	 */
	
	public static double[][] filter5dSymmetric(double[][] valuearray, double[][] filter) throws IOException{
	    	
	      	double[][] newarray = new double[valuearray.length+4][valuearray[0].length+4];
	    	double[][] filtered = new double[valuearray.length][valuearray[0].length];	  
	    	RealMatrix rm = new Array2DRowRealMatrix(valuearray);
	    	double[] imgcol0 = rm.getColumn(0);
	    	double[] imgcol1 = rm.getColumn(1);	    	
	    	double[] imgcoll = rm.getColumn(valuearray[0].length - 2);
	    	double[] imgcolll = rm.getColumn(valuearray[0].length - 1);	    	
	    	double[] imagrow0 = rm.getRow(0);
	    	double[] imagrow1 = rm.getRow(1);
	    	double[] imagrowl = rm.getRow(valuearray.length - 2);
	    	double[] imagrowll = rm.getRow(valuearray.length - 1);
	
	    	int imgcol_cnt = 0;
	    	for (int i=2;i< newarray.length - 2;i++){
	    		newarray[i][0] =  imgcol1[imgcol_cnt];    		
	    		newarray[i][1] =  imgcol0[imgcol_cnt];
	    		newarray[i][newarray[0].length - 2] =  imgcolll[imgcol_cnt];    		
	    		newarray[i][newarray[0].length - 1] =  imgcoll[imgcol_cnt];
	    		imgcol_cnt = imgcol_cnt + 1;			
	    	}    	
	    	
	    	int imgrow_cnt = 0;
	    	for (int i=2;i< newarray[0].length - 2;i++){    		
	    		newarray[0][i] =  imagrow1[imgrow_cnt];    		
	    		newarray[1][i] =  imagrow0[imgrow_cnt];
	    		newarray[newarray.length - 2][i] =  imagrowll[imgrow_cnt];    		
	    		newarray[newarray.length - 1][i] =  imagrowl[imgrow_cnt];
	    		imgrow_cnt = imgrow_cnt + 1;			
	    	}
	    	
	    	newarray[0][0] = newarray[0][3];
	    	newarray[0][1] = newarray[0][2];
	    	newarray[1][0] = newarray[1][3];
	    	newarray[1][1] = newarray[1][2];	    	
	    	newarray[newarray.length - 2][0] = newarray[newarray.length - 2][3];
	    	newarray[newarray.length - 2][1] = newarray[newarray.length - 2][2];
	    	newarray[newarray.length - 1][0] = newarray[newarray.length - 1][3];
	    	newarray[newarray.length - 1][1] = newarray[newarray.length - 1][2];	    	
	    	newarray[0][newarray[0].length - 2] = newarray[0][newarray[0].length - 3];
	    	newarray[0][newarray[0].length - 1] = newarray[0][newarray[0].length - 4];
	    	newarray[1][newarray[0].length - 2] = newarray[1][newarray[0].length - 3];
	    	newarray[1][newarray[0].length - 1] = newarray[1][newarray[0].length - 4];	    	
	    	newarray[newarray.length - 2][newarray[0].length - 2] = newarray[newarray.length - 2][newarray[0].length - 3];
	    	newarray[newarray.length - 2][newarray[0].length - 1] = newarray[newarray.length - 2][newarray[0].length - 4];
	    	newarray[newarray.length - 1][newarray[0].length - 2] = newarray[newarray.length - 1][newarray[0].length - 3];
	    	newarray[newarray.length - 1][newarray[0].length - 1] = newarray[newarray.length - 1][newarray[0].length - 4];
	    	   	
	    	for (int i=2;i< valuearray.length+2;i++){
				for (int j=2;j< valuearray[0].length+2;j++){				
					newarray[i][j] = valuearray[i-2][j-2];
				}
	    	}
	    	
	    	double[][] temparray = new double[5][5];	    	
	    	for (int i=0;i< valuearray.length;i++){
	    		for (int j=0;j< valuearray[0].length;j++){
					int temp_row = 0, temp_col = 0;
					double sum = 0;
					 for (int mm=i;mm<i+5;mm++){
						  temp_col = 0;
	                	 for (int nn = j; nn < j+5; nn++){  
	                		 temparray[temp_row][temp_col] = newarray[mm][nn] * filter[temp_row][temp_col];
	                		 sum = sum + temparray[temp_row][temp_col];                		
	                		 temp_col = temp_col + 1;
	                	}       
	                	 temp_row = temp_row +1;
	                 } 
					 filtered[i][j] = sum;	
				}
			}   	
			return filtered;    	
	    }
	
	
	/**
	 * Transpose image array
	 * @param array
	 * @param ImW
	 * @param ImH
	 * @return
	 */
    static double[][] transpose(double[][] array, int ImW, int ImH) {
        double[][] transposedMatrix = new double[ImW][ImH];
        for(int i=0;i<ImW;i++) {
        	   for(int j=0;j<ImH;j++) {
        	      transposedMatrix[i][j] = array[j][i];
        	   }
        	}
        return transposedMatrix;
    }
    
    /**
     * filters the multidimensional array "valuearray" with the
	 * multidimensional filter "filter".  The result has the same size as "valuearray".
     * Mirror padding 
     */
    
    public static double[][] filter5d(double[][] valuearray, double[][] filter) throws IOException{
    	
    	double[][] newarray = new double[valuearray.length+4][valuearray[0].length+4];
    	double[][] filtered = new double[valuearray.length][valuearray[0].length];    	
    	for (int i=2;i< valuearray.length+2;i++){
			for (int j=2;j< valuearray[0].length+2;j++){
				newarray[i][j] = valuearray[i-2][j-2];
			}
    	}    	
    	for (int i=0;i< 2;i++){
			for (int j=0;j< 2;j++){
				newarray[i][j] = 0;
			}
    	}    	
    	for (int i=valuearray.length+2;i< valuearray.length+ 4; i++){
			for (int j=valuearray[0].length+2;j< valuearray[0].length+4;j++){
				newarray[i][j] = 0;
			}
    	}
    	double[][] temparray = new double[5][5];    	
    	for (int i=0;i< valuearray.length;i++){
			for (int j=0;j< valuearray[0].length;j++){
				int temp_row = 0, temp_col = 0;
				double sum = 0;
				 for (int mm=i;mm<i+5;mm++){
					  temp_col = 0;
                	 for (int nn = j; nn < j+5; nn++){  
                		 temparray[temp_row][temp_col] = newarray[mm][nn] * filter[temp_row][temp_col];
                		 sum = sum + temparray[temp_row][temp_col];                		
                		 temp_col = temp_col + 1;
                	}       
                	 temp_row = temp_row +1;
                 } 
				 filtered[i][j] = sum;			
			}
    	}   	
		return filtered;    	
    }
    
    /**
     * Predict Possible Points
     * 
     */
    
    public static List<PossiblePoints> predictPossiblePoints(double[][] kscores, int[][] KscoresC, double[][] Kpre) throws IOException{
    	 
    	double[][] A = new double[kscores[0].length][kscores[1].length];
	   	for (int i=0; i< kscores[0].length - 5; i++){
	   		for (int j=0; j <  kscores[1].length - 5 ; j++){
	   			A[i][j] = kscores[i][j] + kscores[i + 4][j + 4] - kscores[i + 4][j]  - kscores[i][j + 4];
	   		}    	
	   	}
	   	List<Integer> rows = new ArrayList<Integer>(Arrays.asList(0 ,1 , 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3));
	   	List<Integer> cols = new ArrayList<Integer>(Arrays.asList(0 ,0 , 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3));	   	
	   	ArrayList<PossiblePoints> listPossiblePoints = new ArrayList<>();
	   	double temp_kscores;
	   	double temp_fit;
	   	double temp_meanfit;	   	
	   	for (int i = 0; i < rows.size(); i++) { 
	   		int r = rows.get(i);
	   	    int c= cols.get(i);
	   	    temp_kscores = kscores[r][c] / 2f;	   	   	    
	   	    if (A[r][c] > 0){
	   	      	if (KscoresC[r][c] == 1){     	    		
	   	    		temp_fit = Kpre[r][c] - Kpre[r + 4][c + 4];;
	   	       		temp_meanfit = (temp_fit + temp_kscores) / 2 ;
	   	       		PossiblePoints temp = new PossiblePoints(r, c , r + 4, c + 4, temp_kscores, temp_fit, temp_meanfit);	   	       		
	   	        	listPossiblePoints.add(temp);   	    		
	   	    	}else{
	   	    		temp_fit = Kpre[r + 4][c + 4] - Kpre[r][c];
	   	       		temp_meanfit = (temp_fit + temp_kscores) / 2 ;
	   	       		PossiblePoints temp = new PossiblePoints(r + 4, c + 4, r, c ,  temp_kscores, temp_fit, temp_meanfit);
	   	    		listPossiblePoints.add(temp);    	    		
	   	    	}    	    	
	   	    }else{
	   	       	if (KscoresC[r][c + 4] == 1){
	   	    		temp_fit = Kpre[r][c + 4] - Kpre[r + 4][c];
	   	       		temp_meanfit = (temp_fit + temp_kscores) / 2 ;
	   	       		PossiblePoints temp = new PossiblePoints(r, c + 4, r + 4, c, temp_kscores, temp_fit, temp_meanfit);
	   	        	listPossiblePoints.add(temp);      	        	
	   	    	}else{
	   	    		temp_fit = Kpre[r + 4][c] - Kpre[r][c + 4];
	   	       		temp_meanfit = (temp_fit + temp_kscores) / 2 ;
	   	       		PossiblePoints temp = new PossiblePoints(r + 4, c , r, c + 4,  temp_kscores, temp_fit, temp_meanfit);
	   	    		listPossiblePoints.add(temp);    	    		
	   	    	}
	   	    }    	    
	   	}   	   	
		return listPossiblePoints;    	
   }    
    
    
    
   public static Object[] characterizeblocks(double[][] MeanContent2, 
												double[][] MeanStrongEdge, 
												double[][][] hsbValues, 
												int blk_idx, int blk_idy, 
												double[][][] MeanInSpace, 
												double[][] diff_Mean_Best_scale_grids, 
												double[][] diff_Mean_Best_scale_gridsInv, 
												List<PossiblePoints> PossiblePointslist, 
												int kx, int ky, int sgrid) throws IOException{
	    	
		    	int xlength = (int) Math.ceil(blk_idx/sgrid);
		    	int ylength = (int) Math.ceil(blk_idy/sgrid);
				double[][] uniform = new double[xlength][ylength];
				double[] uniformAll = new double[xlength*ylength];		
				double[][] tempMeanInSpace = new double[kx][ky];
				double[]   meanMeanInSpace = new double[16];
				
				for (int pp=0;pp<16;pp++){
					for (int i=0;i<kx;i++){
						for (int j=0;j<ky;j++){				
							tempMeanInSpace[i][j] = MeanInSpace[pp][i][j];
						}				
					}
					meanMeanInSpace[pp] =  Util.getAverage(tempMeanInSpace);			
				}	
			
				for (int i=0;i<kx;i++){
					for (int j=0;j<ky;j++){	
						for (int pp=0;pp<16;pp++){
							 if (MeanInSpace[pp][i][j]< meanMeanInSpace[pp] *0.2){
				                uniform[i][j] = uniform[i][j] + 1;		              
							 }							 
						}					 
					}
				}				
			
				int counter_uniform = 0;
				for (int j=0;j<ky;j++){	
						for (int i=0;i<kx;i++){
							 uniformAll[counter_uniform] = uniform[i][j];						
				             counter_uniform = counter_uniform + 1;		             
					}				
				}
					
				double std = Util.getStdDev(uniformAll);
				
				// 5x5 filter
				double H[][] = new double[][]{
					{0.0400, 0.0400, 0.0400, 0.0400, 0.0400},
					{0.0400, 0.0400, 0.0400, 0.0400, 0.0400},
					{0.0400, 0.0400, 0.0400, 0.0400, 0.0400},
					{0.0400, 0.0400, 0.0400, 0.0400, 0.0400},
					{0.0400, 0.0400, 0.0400, 0.0400, 0.0400}
				};
					
				double[][] filtered = new double[uniform.length][uniform[0].length];
				filtered = filter5d(uniform, H);				
					
				double meanv = Util.getAverage(filtered);		   		
		    	PossiblePoints posPoints, posPoints1;
		    	double[][] MeanInSpaceTemp = new double[kx][ky]; 
		    	int p1, p2, bg = 0;
		    	double conf;
		    	for (int f=0; f < 16; f++){			    		
		    		posPoints = PossiblePointslist.get(f);
		    		p1 = posPoints.getApoint1();
		    		p2 = posPoints.getApoint2();
			    		if ((p1 == 3) && (p2 == 3)){
			    			bg = f;
			    		}
		    	}				    	
				double[][] MeanInSpacefiltered = new double[MeanInSpaceTemp.length][MeanInSpaceTemp[0].length];
				double[][] bestgrid = new double[MeanInSpaceTemp.length][MeanInSpaceTemp[0].length];
				double[] confscores = new double[16];
		    	if (bg == 15){
		    		for (int i=0;i<kx;i++){
		    			for (int j=0;j<ky;j++){	
		    				MeanInSpaceTemp[i][j] = MeanInSpace[15][i][j];			    				
		    			}
		    		}			    		
		    		MeanInSpacefiltered = filter5d(MeanInSpaceTemp, H);
		    		bestgrid = Util.normalizeIm(MeanInSpacefiltered);				    
		    	}else if (bg == 0){
		    		double max = 0;
		    		for (int f=0; f < 16; f++){
			    		posPoints1 = PossiblePointslist.get(f);
		    			conf = posPoints1.getConfScore(); 			    			
		    			if (conf > max){
		    				max = conf;
		    				bg = f;
		    			}				    		
		    		}			    		
		    		for (int i=0;i<kx;i++){
		    			for (int j=0;j<ky;j++){	
		    				MeanInSpaceTemp[i][j] = MeanInSpace[bg][i][j];			    				
		    			}
		    		}
		    		MeanInSpacefiltered = filter5d(MeanInSpaceTemp, H);
		    		bestgrid = Util.normalizeIm(MeanInSpacefiltered);			    		
		    	}else{
		    		for (int i=0;i<kx;i++){
		    			for (int j=0;j<ky;j++){	
		    				MeanInSpaceTemp[i][j] = MeanInSpace[bg][i][j];			    				
		    			}			    			
		    		}
		    		MeanInSpacefiltered = filter5d(MeanInSpaceTemp, H);
		    		bestgrid = Util.normalizeIm(MeanInSpacefiltered);			    	
		    	}

		    	// block based homogenous
		    	for (int f=0; f < 16; f++){
		    		posPoints1 = PossiblePointslist.get(f);
	    			confscores[f] = posPoints1.getConfScore(); 
	    		}				    	
		    	
		    	int homb;
		    	if ((Util.getMean(confscores) > 0.4) || bg != 15){
		    		homb = 0;
		    	}else{
		    		homb = 1;
		    	}
						
		    	double Ithres = std / meanv;					    
		    	if (Ithres >  1.5){
		    		for (int i=0;i<filtered.length;i++){
		    			for (int j=0;j<filtered[0].length;j++){	
		    				
		    				if (( meanv + std) > filtered[i][j]){
		    					filtered[i][j] = 0;
		    				}else{
		    					filtered[i][j] = homb;
		    				}
		    			}
		    		}
		    	}else{
		    		for (int i=0;i<filtered.length;i++){
		    			for (int j=0;j<filtered[0].length;j++){	
		    				
		    				if (( meanv + (std/2d)) > filtered[i][j]){
		    					filtered[i][j] = 0;
		    				}else{
		    					filtered[i][j] = homb;
		    				}
		    			}
		    		}
		    	}	
					    	
		       	double[][] hom = new double[kx][ky];
		    	double[][] MeanStrongEdge2 = new double[kx][ky];
		    	double[][] V_im2 = new double[kx][ky];
		    	double[][] V_imOver = new double[kx][ky];
		    	double[][] V_imUndr = new double[kx][ky];
		    	int[][] notuse  = new int[kx][ky];
		    	for (int[] row: notuse)
	        		Arrays.fill(row,  0);		    	
					    	
		    	int c = sgrid;
		    	int cc = 8 * sgrid;
		    	int a, b;
					    	
		    	for (int i=0;i<kx;i++){
	    			for (int j=0;j<ky;j++){		    				
	    				// MeanContent2 - populate hom 
	    				if (MeanContent2[i][j] <= 4){
	    					hom[i][j] = 1;
	    					notuse[i][j] = 3;
	    				}else{
	    					hom[i][j] = 0;
	    				}		    				
	    				  a = i*sgrid; 
	    			      b = j*sgrid; 
	    			  
	    			      double[] temp_array = new double[sgrid*sgrid];
	                      int temp_count = 0;
	                      double sum_meanStrong = 0;
	                      double tempMeanStrongEdge2 =0;
	                      for (int mm=a;mm<a + c;mm++){
	                     	 for (int nn = b; nn < b + c; nn++){                    		
	                     		 temp_array[temp_count] = MeanStrongEdge[mm][nn];
	                     		 sum_meanStrong = sum_meanStrong + temp_array[temp_count];		                     		
	                     		 temp_count = temp_count + 1;
	                     	 }       
	                      }  	                   
	                      tempMeanStrongEdge2 =  sum_meanStrong / (double) (temp_count); 	                    
	                      if (tempMeanStrongEdge2 < 0.5){
	                    	 MeanStrongEdge2[i][j] = 0;
	                     }else{
	                    	 MeanStrongEdge2[i][j] = 1;
	                    	 notuse[i][j] = 2;
	                     }
	                      a=i*8*sgrid;
	                      b=j*8*sgrid;
	                      double[] temp_arrayV = new double[8*sgrid*8*sgrid];
	                      double sum_meanV = 0;
	                      int temp_countV = 0;
	                      for (int nn = b; nn < b + cc; nn++){  
	                    	  for (int mm = a; mm < a + cc;mm++){			                     	                   		
		                     		 temp_arrayV[temp_countV] = hsbValues[2][nn][mm];			                     		
		                     		 sum_meanV = sum_meanV + temp_arrayV[temp_countV];		                     		
		                     		 temp_countV = temp_countV + 1;
		                     	 }       
		                      } 
	                      V_im2[i][j]= sum_meanV / (double) (temp_countV);
	                      if (V_im2[i][j] >= 245){
	                    	  V_imOver[i][j] = 300;
	                      }else{
	                    	  V_imOver[i][j] = 0;		                    	
	                      }
	                      if (V_im2[i][j] < 15){
	                    	  V_imUndr[i][j] = 300;
	                      }else{		             
	                    	  V_imUndr[i][j] = 0;
	                     }
	             	}	    			
		    	}			    	
				
		    	V_imOver = Util.normalizeIm(V_imOver);
		    	V_imUndr = Util.normalizeIm(V_imUndr);
		 
		    	// Possibilities *******************//			    	  
		    	int notused  = 0;
		    	for (int i=0;i<kx;i++){
	    			for (int j=0;j<ky;j++){	
	    				if ( V_imUndr[i][j] == 1 ||  V_imOver[i][j] == 1)
	                    	  notuse[i][j] = 1; 
	    				
	    				if (notuse[i][j] == 1)
	    					filtered[i][j] = 1;
	    				
	    				if (notuse[i][j] != 0)
	    					notused = notused + 1;
	    			}
		    	}			  
					    	
		    	int touse= kx * ky - notused; 
		    	if (touse == 0){
			    	for (int i=0;i<kx;i++){
			    		for (int j=0;j<ky;j++){	
			    			if ((hom[i][j] == 1) && (filtered[i][j] != 1))
			    				notuse[i][j] = 0;   				
			    		}
			    	}	
		    	}					    	
		    	double[][] diff_Mean_Best_scale_temp_grids = diff_Mean_Best_scale_grids.clone();
		    	double[][] diff_Mean_Best_scale_temp_gridsInv = diff_Mean_Best_scale_gridsInv.clone();
		    	double [][] imageFgrids = new double[kx][ky];
		    	double [][] imageFgridsInv = new double[kx][ky];
		    	double meandiff = Util.getAverage(diff_Mean_Best_scale_grids);
					    	
		    	int row = (int) blk_idx / sgrid;
		    	int col = (int) blk_idy /sgrid;
		    	
		    	boolean gridsalg = true;
		    	boolean gridsInvalg = true;
					    
		    	for (int i = 0; i < row; i++){
		    		for (int j = 0; j < col; j++){			    			
		    			if (gridsalg){		    				
			    				if (filtered[i][j] == 1)
			    					diff_Mean_Best_scale_temp_grids[i][j] = 0;			    			
				    			if ((diff_Mean_Best_scale_temp_grids[i][j] < meandiff) &&  homb == 1)					    				
				    				diff_Mean_Best_scale_temp_grids[i][j] = 0;
				    			
				    			if ((i == 0) || (i == row - 1) || (j == 0) || (j == col -1)){
				    				imageFgrids[i][j] = diff_Mean_Best_scale_temp_grids[i][j] * (bestgrid[i][j]);
				    			}else{
				    				imageFgrids[i][j] = diff_Mean_Best_scale_temp_grids[i][j] * (1 - bestgrid[i][j]);
				    			}
		    			}
		    			if (gridsInvalg){
		    					imageFgridsInv[i][j] = diff_Mean_Best_scale_temp_gridsInv[i][j] * (1 - bestgrid[i][j]);
		    			}
		    		}
		    	}
					  			  
		    	
		    	// GRIDS
		    	double[][] E_grids = new double[imageFgrids.length][imageFgrids[0].length];
		    	E_grids = filter5d(imageFgrids, H);
		    	double[] unintresting_grids = new double[touse];
				Arrays.fill(unintresting_grids,  0); 
		    	
				// GRIDS Inversed
				double[][] E_gridsInv = new double[imageFgridsInv.length][imageFgridsInv[0].length];
				E_gridsInv = filter5d(imageFgridsInv, H);
				double[] unintresting_gridsInv = new double[touse];
				Arrays.fill(unintresting_gridsInv,  0); 
				
				int counta = 0;								
				for (int i=0;i<kx;i++){
	    			for (int j=0;j<ky;j++){	
	    				if (notuse[i][j] == 0){
	    					unintresting_grids[counta] = E_grids[i][j];
	    					unintresting_gridsInv[counta] = E_gridsInv[i][j];
	    					counta = counta + 1;
	    				}		    				
	    			}
	    		}			
						
				double meanuninteresting_grids = Util.getMean(unintresting_grids);	
				double meanuninteresting_gridsInv = Util.getMean(unintresting_gridsInv);	
				for (int i=0;i<kx;i++){
	    			for (int j=0;j<ky;j++){			    				
	    				if ((notuse[i][j] == 2) || (filtered[i][j] == 1)) 
	    					filtered[i][j] = 0;	    				
	    				if (gridsalg){
			    				if ((notuse[i][j] == 1) || (imageFgrids[i][j] < meanuninteresting_grids)) 
			    					imageFgrids[i][j] = meanuninteresting_grids;
			    				
			    				if ((filtered[i][j] == 1) && (imageFgrids[i][j] < meanuninteresting_grids) || (notuse[i][j] == 3) && (filtered[i][j] == 1))
			    					imageFgrids[i][j] = meanuninteresting_grids;
			    				}
	    				
	    				if (gridsInvalg){
		    					if ((notuse[i][j] == 1) || (imageFgridsInv[i][j] > meanuninteresting_gridsInv)) 
		    						imageFgridsInv[i][j] = meanuninteresting_gridsInv;
			    				
			    				if ((filtered[i][j] == 1) && (imageFgridsInv[i][j] > meanuninteresting_gridsInv) || (notuse[i][j] == 3) && (filtered[i][j] == 1))
			    					imageFgridsInv[i][j] = meanuninteresting_gridsInv;		    				
	    				}
	    			}
				}					
							
				double[][] Eresult_grids = new double[imageFgrids.length][imageFgrids[0].length];					
				Eresult_grids = filter5dSymmetric(imageFgrids, H);	
				double[][] Eresult_gridsInv = new double[imageFgridsInv.length][imageFgridsInv[0].length];					
				Eresult_gridsInv = filter5dSymmetric(imageFgridsInv, H);
				
		    	return new Object[]{Eresult_grids, Eresult_gridsInv};    	
	}
	
	
	
	public static Object[] scores_pick_variables( Object[] inblock, int sgrid, 
													int blk_idx, int blk_idy, 
													List<PossiblePoints> PossiblePointslist, 
													int kx, int ky, int bg_gridsInv, int nblocks) throws IOException{
    	
   	 	double[][][][] BlockScoreALL_temp = new double[nblocks][nblocks][blk_idx][blk_idy];
   	 	BlockScoreALL_temp = (double[][][][]) inblock[2];
      	int p,q;
      	PossiblePoints posPoints;
      	double[][][] BlockScore = new double[16][blk_idx][blk_idy];
		   	for (int i=0; i < 16;i++){
		   		posPoints = PossiblePointslist.get(i);
		   		p = posPoints.getApoint1();
		   		q = posPoints.getApoint2();
		   		for (int h=0; h < blk_idx;  h++){
						for (int g=0; g < blk_idy;  g++){
							BlockScore[i][h][g] = BlockScoreALL_temp[p][q][h][g]  / (double) 255;
						}
					}  	
		   	}  
		   	
	   	int b,a,d,c;
	   	double MeanInSpace[][][] =  new double[16][kx][ky];
	   	double sum = 0;
	   	for (int r=0;r<16;r++){
	   		for (int i=0;i<kx;i++){
	   			for (int j=0;j<ky;j++){
	   				 a = (i)*sgrid;
	                    b = a + (sgrid );
	                    c = (j)*sgrid;
	                    d = c + (sgrid); 	                    
	                    double[] temp_array = new double[sgrid*sgrid];
	                    int temp_count = 0;
	                     sum = 0;
	                    for (int mm=a;mm<b;mm++){
	                    	for (int nn = c; nn < d; nn++){                    		
	                   		 	 temp_array[temp_count] = BlockScore[r][mm][nn];
	                      		 sum = sum + temp_array[temp_count];
	                      		 temp_count = temp_count + 1;                    
	                   	 	}       
	                    }    
	                    MeanInSpace[r][i][j] = sum / (double) (temp_count); 
	               }
	   		}    		
	   	}   	
	   	double[] odd = new double[16];
	   	double[][] MeanOfAllGrids = new double[kx][ky];
	   	for (int i=0;i<kx;i++){
				for (int j=0;j<ky;j++){	   
						for (int r=0;r<16;r++){
				    			odd[r] = MeanInSpace[r][i][j];
						}
		    			MeanOfAllGrids[i][j] = Util.getMean(odd);
		    	}
		}  			
		double[][] BestGrid = new double[kx][ky];
		double[][] BestGridInv = new double[kx][ky];
		double[][] diff_Mean_Best_grids = new double[kx][ky];
		double[][] diff_Mean_Best_gridsInv = new double[kx][ky];		
		
		for (int i=0;i<kx;i++){
			for (int j=0;j<ky;j++){	
				BestGrid[i][j] = MeanInSpace[15][i][j];
				diff_Mean_Best_grids[i][j]= MeanOfAllGrids[i][j] - BestGrid[i][j];				
				BestGridInv[i][j] = MeanInSpace[bg_gridsInv][i][j];
				diff_Mean_Best_gridsInv[i][j]= MeanOfAllGrids[i][j] - BestGridInv[i][j];
			}
		}		
		double[][] diff_Mean_Best_scale =  new double[kx][ky];		
		diff_Mean_Best_scale = Util.normalizeIm(diff_Mean_Best_grids);		
		double[][] diff_Mean_Best_scale_gridsInv =  new double[kx][ky];		
		diff_Mean_Best_scale_gridsInv = Util.normalizeIm(diff_Mean_Best_gridsInv);	
		return new Object[]{MeanInSpace, diff_Mean_Best_scale, diff_Mean_Best_scale_gridsInv};    	
   }
	
	
	public static Object[] inblockpatterns2(double[][][] YCbCrValues, int nblocks, int blk_idx, int blk_idy, int block, int bins) throws IOException{		 
	 	double Kscores[][] = new double[nblocks + 1][nblocks + 1];
	 	int KscoresC[][] = new int[nblocks + 1][nblocks + 1];
	 	double[][][][] BlockScoreALL = new double[nblocks][nblocks][blk_idx][blk_idy];    	
        for (int p = 0; p < nblocks;p++){
        	for (int q = 0; q < nblocks; q++ ){    	    
        		int zmatsize = (int) Math.ceil(blk_idx*blk_idy);
            	double[] zmat1 = new double[zmatsize];
            	double[] zmat2 = new double[zmatsize];            	
            	int Ax = 0, Ex = 0, Ay = 0, Ey, counter = 0;
            	double A, B, C, D, E, F, G, H;    
            	double zmarrem = 0;
    	
			    	for (int j= 0; j < blk_idx; j++){
			    		Ay = j*8+p;
			    		Ey = Ay + 4;
			    		for (int i=0; i<blk_idy; i++){
			    			Ax = i*8+q;
			    			A = YCbCrValues[0][Ax][Ay];
			    			B = YCbCrValues[0][Ax + 1][Ay]; 
			    			C = YCbCrValues[0][Ax][Ay + 1];
			    			D = YCbCrValues[0][Ax+1][Ay+1];
			    		    			
			    			Ex = Ax + 4;
			    			E = YCbCrValues[0][Ex][Ey];
			    			F = YCbCrValues[0][Ex + 1][Ey];
			    			G = YCbCrValues[0][Ex][Ey + 1];
			    			H = YCbCrValues[0][Ex+1][Ey+1];
			    		    zmat1[counter] = Math.abs(A - B - C + D);
			    			zmat2[counter] = Math.abs(E - F - G + H);    			
			    			zmarrem = zmat2[counter] - zmat1[counter];
			    		
			    			if (zmarrem <= 0){
			    				BlockScoreALL[0][0][j][i] = 0;
			    			}else{
			    				BlockScoreALL[p][q][j][i] = zmat2[counter] - zmat1[counter];
			    			}
			    			counter = counter + 1;			    			
			    		} 
			    	}
			    	
			    	// create histograms of zmat
			    	 int[] hist1 = Util.createhistogram(zmat1, bins);
			    	 int[] hist2 = Util.createhistogram(zmat2, bins); 			    	 
			         int scale = counter;
			         double[] hist1norm = Util.normalize(hist1, scale);
			    	 double[] hist2norm = Util.normalize(hist2, scale);
			    
			    	 double k = 0;
			    	 double ktemp;
			    	 for (int l=0;l<hist1.length;l++){
			    		  ktemp=hist1norm[l] - hist2norm[l];		    		  
			    	      k = k + Math.abs(ktemp);    		 
			    	 }			    	 
			    	 double findCorrectA = hist1[0] + hist1[1];
			    	 double findCorrectB = hist2[0] + hist2[1];
			    	 int Corrent = 0;
			    	 if (findCorrectA > findCorrectB){
			    		 Corrent = 1;
			    	 }else{
			    		 Corrent = 0;
			    	 }			 	  		        		       
								    if (k >= 2){
							        	Kscores[p][q] = 0;
							        	KscoresC[p][q] = Corrent;
							        }else{
							        	Kscores[p][q] = k;
							        	KscoresC[p][q] = Corrent;
							        }
        	}        	
    	}     
		return new Object[]{Kscores, KscoresC, BlockScoreALL};  
	}
	
}
