package gr.iti.mklab.reveal.forensics.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
  * @author olgapapa
  * Create Masks
  */

public class GridsMaps {
	int j = 0;

	
	public int[][][] getPmasks() throws NumberFormatException, IOException {		
		int[][][] pmask = new int[59][10][10];
		
			for (int k=0;k<59; k++){
				for (int x=0;x<10;x++){						
					j=0;
					for (int y= k*10; y < k*10+10; y++){			          
				            pmask[k][x][j]=GridsPMasks.pMask[x][y];
				            j=j+1;
					}
				}
			}
		return pmask;
	}
	
	public int[][][] getMMasks(){
		int[][][] mmask = new int[59][10][10];
		
		for (int k=0;k<59; k++){
			for (int x=0;x<10;x++){						
				j=0;
				for (int y= k*10; y < k*10+10; y++){			          
					mmask[k][x][j]= GridsMMask.mMask[x][y];
			            j=j+1;
				}
			}
		}
		return mmask;
	}
	
	public int[] getMaskWhite() {		
		int[] maskwhite = new int[]{10, 30, 50, 70, 90, 20, 40, 60, 80, 12, 30, 50, 70, 88, 15, 28, 45, 64, 79, 85, 12, 30, 50, 70, 88, 20, 40, 60, 80, 10, 30, 50, 70, 90, 20, 40, 60, 80, 12, 30, 50, 70, 88, 15, 21, 36, 55, 72, 85, 12, 30, 50, 70, 88, 20, 40, 60, 80};    		
		return maskwhite;
	}    	
}
