package gr.iti.mklab.reveal.forensics.maps.copymove;

import java.util.Random;

/**
 * Nearest-Neighbor Field (see PatchMatch algorithm) 
 * 
 * @author Xavier Philippeau
 *
 */
public class NNFNoMask {

	// image
	ImageMap input, output;

	//  patch size
	int S;

	// Nearest-Neighbor Field 1 pixel = { x_target, y_target, distance_scaled }
	int[][][] field;

	// random generator
	Random random = new Random(0);

	// constructor
	public NNFNoMask(ImageMap input, ImageMap output, int patchsize) {
		this.input = input;
		this.output= output;
		this.S = patchsize;	
	}
	
	// initialize field with random values
	public void randomize() {
		// field
		this.field = new int[input.w][input.h][3];
		
		for(int y=0;y<input.h;y++) {
			for(int x=0;x<input.w;x++) {
				field[x][y][0] = random.nextInt(output.w);
				field[x][y][1] = random.nextInt(output.h);
				field[x][y][2] = ImageMap.dSCALE;
			}
		}
		initialize();
	}

	public void randomizeDist(int minDist) {
		// field
		this.field = new int[input.w][input.h][3];
		double imageDiagonal=Math.sqrt(input.w *input.w +input.h *input.h);
		int minPixelDistance=(int) Math.ceil(imageDiagonal/minDist);

		int X,Y, dist;
		for(int y=0;y<input.h;y++) {
			for(int x=0;x<input.w;x++) {
				X=random.nextInt(output.w);
				Y=random.nextInt(output.h);
				dist=(X-x)*(X-x)+(Y-y)*(Y-y);
				while (dist<minPixelDistance*minPixelDistance){
					X=random.nextInt(output.w);
					Y=random.nextInt(output.h);
					dist=(X-x)*(X-x)+(Y-y)*(Y-y);
				}

				field[x][y][0] = X;
				field[x][y][1] = Y;
				field[x][y][2] = ImageMap.dSCALE;
			}
		}
		initialize();
	}

	// initialize field from an existing (possibily smaller) NNF
	public void initialize(NNFNoMask nnf) {
		// field
		this.field = new int[input.w][input.h][3];
		
		int fx = input.w /nnf.input.w;
		int fy = input.h /nnf.input.h;
		//System.out.println("nnf upscale by "+fx+"x"+fy+" : "+nnf.input.w+","+nnf.input.h+" -> "+input.w+","+input.h);
		for(int y=0;y<input.h;y++) {
			for(int x=0;x<input.w;x++) {
				int xlow = Math.min(x/fx, nnf.input.w -1);
				int ylow = Math.min(y/fy, nnf.input.h -1);
				field[x][y][0] = nnf.field[xlow][ylow][0]*fx;  
				field[x][y][1] = nnf.field[xlow][ylow][1]*fy;
				field[x][y][2] = ImageMap.dSCALE;
			}
		}
		initialize();
	}
	
	// compute initial value of the distance term
	private void initialize() {
		for(int y=0;y<input.h;y++) {
			for(int x=0;x<input.w;x++) {
				field[x][y][2] = distance(x,y,  field[x][y][0],field[x][y][1]);

				// if the distance is INFINITY (all pixels masked ?), try to find a better link
				int iter=0, maxretry=20;
				while( field[x][y][2] == ImageMap.dSCALE && iter<maxretry) {
					field[x][y][0] = random.nextInt(output.w);
					field[x][y][1] = random.nextInt(output.h);
					field[x][y][2] = distance(x,y,  field[x][y][0],field[x][y][1]);
					iter++;
				}
			}
		}
	}

	// multi-pass NN-field minimization (see "PatchMatch" - page 4)
	public void minimize(int pass) {

		int min_x=0, min_y=0, max_x=input.w -1, max_y=input.h -1;

		// multi-pass minimization
		for(int i=0;i<pass;i++) {
			System.out.print(".");

			// scanline order
			for(int y=min_y;y<max_y;y++)
				for(int x=min_x;x<=max_x;x++)
					if (field[x][y][2]>0) minimizeLink(x,y,+1);

			// reverse scanline order
			for(int y=max_y;y>=min_y;y--)
				for(int x=max_x;x>=min_x;x--)
					if (field[x][y][2]>0) minimizeLink(x,y,-1);
		}
	}


	// multi-pass NN-field minimization for matches above a distance threshold (modification by Markos Zampoglou)
	public void minimizeDistance(int pass, int minDist) {

		double imageDiagonal=Math.sqrt(input.w *input.w +input.h *input.h);
		int minPixelDistance=(int) Math.ceil(imageDiagonal/minDist);

		int min_x=0, min_y=0, max_x=input.w -1, max_y=input.h -1;

		// multi-pass minimization
		for(int i=0;i<pass;i++) {
			System.out.print(".");

			// scanline order
			for(int y=min_y;y<max_y;y++)
				for(int x=min_x;x<=max_x;x++)
					if (field[x][y][2]>0) minimizeLinkDistance(x, y, +1, minPixelDistance);

			// reverse scanline order
			for(int y=max_y;y>=min_y;y--)
				for(int x=max_x;x>=min_x;x--)
					if (field[x][y][2]>0) minimizeLinkDistance(x, y, -1, minPixelDistance);
		}
	}


	// minimize a single link (see "PatchMatch" - page 4)
	public void minimizeLink(int x, int y, int dir) {
		int xp,yp,dp;
		
		//Propagation Left/Right
		if (x-dir>0 && x-dir<input.w) {
			xp = field[x-dir][y][0]+dir;
			yp = field[x-dir][y][1];
			dp = distance(x,y, xp,yp);
			if (dp<field[x][y][2]) {
				field[x][y][0] = xp;
				field[x][y][1] = yp;
				field[x][y][2] = dp;
			}
		}
		
		//Propagation Up/Down
		if (y-dir>0 && y-dir<input.h) {
			xp = field[x][y-dir][0];
			yp = field[x][y-dir][1]+dir;
			dp = distance(x,y, xp,yp);
			if (dp<field[x][y][2]) {
				field[x][y][0] = xp;
				field[x][y][1] = yp;
				field[x][y][2] = dp;
			}
		}
		
		//Random search
		int wi=output.w, xpi=field[x][y][0], ypi=field[x][y][1];
		while(wi>0) {
			xp = xpi + random.nextInt(2*wi)-wi;
			yp = ypi + random.nextInt(2*wi)-wi;
			xp = Math.max(0, Math.min(output.w -1, xp ));
			yp = Math.max(0, Math.min(output.h -1, yp ));
			
			dp = distance(x,y, xp,yp);
			if (dp<field[x][y][2]) {
				field[x][y][0] = xp;
				field[x][y][1] = yp;
				field[x][y][2] = dp;
			}
			wi/=2;
		}
	}

	// minimize a single link by finding a match beyond a certain distance (modification by Markos Zampoglou)
	public void minimizeLinkDistance(int x, int y, int dir, int minDist) {
		int xp,yp,dp;

		//Propagation Left/Right
		if (x-dir>0 && x-dir<input.w) {
			xp = field[x-dir][y][0]+dir;
			yp = field[x-dir][y][1];
			dp = distance(x,y, xp,yp);
			if (((xp-x)*(xp-x)+(yp-y)*(yp-y)>minDist*minDist) & (dp < field[x][y][2])) {
					field[x][y][0] = xp;
					field[x][y][1] = yp;
					field[x][y][2] = dp;
				}
			}


		//Propagation Up/Down
		if (y-dir>0 && y-dir<input.h) {
			xp = field[x][y-dir][0];
			yp = field[x][y-dir][1]+dir;
			dp = distance(x,y, xp,yp);
			if (((xp-x)*(xp-x)+(yp-y)*(yp-y)>minDist*minDist) & (dp<field[x][y][2])) {
				field[x][y][0] = xp;
				field[x][y][1] = yp;
				field[x][y][2] = dp;
			}
		}

		//Random search
		int wi=output.w, xpi=field[x][y][0], ypi=field[x][y][1];
		while(wi>0) {
			xp = xpi + random.nextInt(2*wi)-wi;
			yp = ypi + random.nextInt(2*wi)-wi;
			xp = Math.max(0, Math.min(output.w -1, xp ));
			yp = Math.max(0, Math.min(output.h -1, yp ));

			dp = distance(x,y, xp,yp);
			if (((xp-x)*(xp-x)+(yp-y)*(yp-y)>minDist*minDist) & (dp<field[x][y][2])) {
				field[x][y][0] = xp;
				field[x][y][1] = yp;
				field[x][y][2] = dp;
			}
			wi/=2;
		}
	}


	// compute distance between two patch 
	public int distance(int x,int y, int xp,int yp) {
		return ImageMap.distance(input, x, y, output, xp, yp, S);
	}
	
	public int[][][] getField() {
		return field;
	}

	public void removeLowVar(int varBlockSize, double varThreshold){
		double[][] varMap=input.localVariance(varBlockSize);
		for (int ii=0; ii<field.length;ii++){
			for (int jj=0; jj<field[0].length;jj++){
				if (varMap[ii][jj]<varThreshold) {
					field[ii][jj][2] = input.dSCALE;
				}
			}
		}
	}

    public int[][] linearFilterField(){
        int[][] fieldX = new int[input.w][input.h];
        int[][] fieldY = new int[input.w][input.h];
        for (int ii=0; ii<fieldX.length;ii++){
            for (int jj=0; jj<fieldX[0].length;jj++){
                fieldX[ii][jj] = field[ii][jj][0];
            }
        }
        for (int ii=0; ii<fieldX.length;ii++){
            for (int jj=0; jj<fieldX[0].length;jj++){
                fieldY[ii][jj] = field[ii][jj][1];
            }
        }

        int[][] fieldXHorz = new int[input.w][input.h];
        int[][] fieldXVert = new int[input.w][input.h];
        int[][] fieldYHorz = new int[input.w][input.h];
        int[][] fieldYVert = new int[input.w][input.h];

        for (int ii=0; ii<fieldX.length-1;ii++){
            for (int jj=0; jj<fieldX[0].length;jj++){
                fieldXHorz[ii][jj] = Math.abs(fieldX[ii+1][jj] - fieldX[ii][jj]);
            }
        }
        for (int jj=0; jj<fieldX[0].length;jj++){
            fieldXHorz[fieldX.length-1][jj] = fieldXHorz[fieldX.length-2][jj];
        }
        for (int ii=0; ii<fieldX.length;ii++){
            for (int jj=0; jj<fieldX[0].length-1;jj++){
                fieldXVert[ii][jj] = Math.abs(fieldX[ii][jj+1] - fieldX[ii][jj]);
            }
        }
        for (int ii=0; ii<fieldX[0].length;ii++){
            fieldXVert[ii][fieldX[0].length-1] = fieldXHorz[ii][fieldX[0].length-2];
        }

        for (int ii=0; ii<fieldY.length-1;ii++){
            for (int jj=0; jj<fieldY[0].length;jj++){
                fieldYHorz[ii][jj] = Math.abs(fieldY[ii+1][jj] - fieldY[ii][jj]);
            }
        }
        for (int jj=0; jj<fieldY[0].length;jj++){
            fieldXHorz[fieldY.length-1][jj] = fieldYHorz[fieldY.length-2][jj];
        }
        for (int ii=0; ii<fieldY.length;ii++){
            for (int jj=0; jj<fieldY[0].length-1;jj++){
                fieldYVert[ii][jj] = Math.abs(fieldY[ii][jj+1] - fieldY[ii][jj]);
            }
        }
        for (int ii=0; ii<fieldY[0].length;ii++){
            fieldYVert[ii][fieldY[0].length-1] = fieldYHorz[ii][fieldY[0].length-2];
        }

        int[][] sumAmplitude=new int[input.w][input.h];
        for (int ii=0; ii<sumAmplitude.length;ii++){
            for (int jj=0; jj<sumAmplitude[0].length;jj++){
                sumAmplitude[ii][jj] = sumAmplitude[ii][jj]+ fieldXHorz[ii][jj] + fieldYHorz[ii][jj] + fieldXVert[ii][jj] + fieldYVert[ii][jj];
            }
        }

        return sumAmplitude;
    }
}
