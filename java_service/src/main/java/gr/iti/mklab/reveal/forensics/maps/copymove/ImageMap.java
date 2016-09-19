package gr.iti.mklab.reveal.forensics.maps.copymove;

import gr.iti.mklab.reveal.forensics.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Wrapper/Helper for Masked RGB BufferedImage 
 * 
 * @author Xavier Philippeau
 *
 */
public class ImageMap {

	// image data
	private BufferedImage image;
	public final int w, h;

	// the maximum value returned by MaskedImage.distance()
	public static final int dSCALE = 65535;

	// array for converting distance to similarity
	public static final double[] similarity;

	static {
		// build similarity curve such that similarity[0%]=0.999 and similarity[4%]=0.5
		double s_zero=0.999;
		double t_halfmax=0.10;

		double x = (s_zero-0.5)*2;
		double invtanh = 0.5*Math.log((1+x)/(1-x));
		double coef = invtanh/t_halfmax;

		similarity = new double[dSCALE +1];
		for(int i=0;i<similarity.length;i++) {
			double t = (double)i/similarity.length;
			similarity[i] = 0.5-0.5*Math.tanh(coef*(t-t_halfmax));
		}
	}

	// construct from existing BufferedImage and mask
	public ImageMap(BufferedImage image) {
		this.image = image;
		this.w =image.getWidth();
		this.h =image.getHeight();
	}

	// construct empty image
	public ImageMap(int width, int height) {
		this.w =width;
		this.h =height;
		this.image = new BufferedImage(w, h,BufferedImage.TYPE_INT_RGB);
	}
		
	public BufferedImage getBufferedImage() {
		return image;
	}
	public int getSample(int x, int y, int band) {
		return image.getRaster().getSample(x, y, band);
	}
	public void setSample(int x, int y, int band, int value) {
		image.getRaster().setSample(x, y, band, value);
	}

	// distance between two patches in two images
	public static int distance(ImageMap source,int xs,int ys, ImageMap target,int xt,int yt, int S) {
		long distance=0, wsum=0, ssdmax = 10*255*255;
		// for each pixel in the source patch
		for(int dy=-S;dy<=S;dy++) {
			for(int dx=-S;dx<=S;dx++) {
				wsum+=ssdmax;
				int xks=xs+dx, yks=ys+dy;
				if (xks<0 || xks>=source.w) {distance+=ssdmax; continue;}
				if (yks<0 || yks>=source.h) {distance+=ssdmax; continue;}
				// corresponding pixel in the target patch
				int xkt=xt+dx, ykt=yt+dy;
				if (xkt<0 || xkt>=target.w) {distance+=ssdmax; continue;}
				if (ykt<0 || ykt>=target.h) {distance+=ssdmax; continue;}
				// SSD distance between pixels (each value is in [0,255^2])
				long ssd=0;
				// value distance (weight for R/G/B components = 3/6/1)
				for(int band=0;band<3;band++) {
					int weight = (band==0)?3:(band==1)?6:1;
					double diff2 = (source.getSample(xks, yks, band) - target.getSample(xkt, ykt, band))*(source.getSample(xks, yks, band) - target.getSample(xkt, ykt, band)); // Value
					ssd += weight*diff2;
				}
				// add pixel distance to global patch distance
				distance += ssd;
			}
		}
		return (int)(dSCALE *distance/wsum);
	}

    public static int distance2(ImageMap source,int xs,int ys, ImageMap target,int xt,int yt, int S) {
        long distance=0, wsum=0;
        double ssdmax = 1.9;
        // for each pixel in the source patch
        for(int dy=-S;dy<=S;dy++) {
            for(int dx=-S;dx<=S;dx++) {
                wsum+=ssdmax;
                int xks=xs+dx, yks=ys+dy;
                if (xks<0 || xks>=source.w) {distance+=ssdmax; continue;}
                if (yks<0 || yks>=source.h) {distance+=ssdmax; continue;}
                // corresponding pixel in the target patch
                int xkt=xt+dx, ykt=yt+dy;
                if (xkt<0 || xkt>=target.w) {distance+=ssdmax; continue;}
                if (ykt<0 || ykt>=target.h) {distance+=ssdmax; continue;}
                // SSD distance between pixels (each value is in [0,255^2])
                long ssd=0;
                double s_RGB=source.getSample(xks, yks, 0)+source.getSample(xks, yks, 1)+source.getSample(xks, yks, 2);
                double s_r=source.getSample(xks, yks, 0)/s_RGB;
                double s_g=source.getSample(xks, yks, 1)/s_RGB;
                double s_b=source.getSample(xks, yks, 2)/s_RGB;
                double t_RGB=source.getSample(xks, yks, 0)+source.getSample(xks, yks, 1)+source.getSample(xks, yks, 2);
                double t_r=source.getSample(xks, yks, 0)/t_RGB;
                double t_g=source.getSample(xks, yks, 1)/t_RGB;
                double t_b=source.getSample(xks, yks, 2)/t_RGB;
                double diff2 = (s_r - t_r)*(s_r - t_r) + (s_g - t_g)*(s_g - t_g) + (s_b - t_b)*(s_b - t_b); // Value
                ssd += diff2;
                // add pixel distance to global patch distance
                distance += ssd;
            }
        }
        return (int)(dSCALE *distance/wsum);
    }

	// Helper for BufferedImage resize
	public static BufferedImage resize(BufferedImage input, int newwidth, int newheight) {
		BufferedImage out = new BufferedImage(newwidth, newheight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = out.createGraphics();
		java.awt.Image scaled = input.getScaledInstance(newwidth, newheight, java.awt.Image.SCALE_SMOOTH);
		g.drawImage(scaled, 0, 0, out.getWidth(), out.getHeight(), null);
		g.dispose();
		return out;
	}

	// return a copy of the image
	public ImageMap copy() {
		boolean[][] newmask= new boolean[w][h];
		BufferedImage newimage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		newimage.createGraphics().drawImage(image, 0, 0, null);
		return new ImageMap(newimage);
	}
	
	// return a downsampled image (factor 1/2)
	public ImageMap downsample() {
		int newW= w /2, newH= h /2;
		// Binomial coefficient kernels
		int[] kernelEven = new int[] {1,5,10,10,5,1}; 
		int[] kernelOdd = new int[] {1,4,6,4,1};
		int[] kernelx = (w %2==0)?kernelEven:kernelOdd;
		int[] kernely = (h %2==0)?kernelEven:kernelOdd;

		ImageMap newimage = new ImageMap(newW, newH);
		for(int y=0,ny=0;y< h -1;y+=2,ny++) {
			for(int x=0,nx=0;x< w -1;x+=2,nx++) {
				long r=0,g=0,b=0,ksum=0,masked=0,total=0;
				for(int dy=0;dy<kernely.length;dy++) {
					int yk=y+dy-2;
					if (yk<0 || yk>= h) continue;
					for(int dx=0;dx<kernelx.length;dx++) {
						int xk = x+dx-2;
						if (xk<0 || xk>= w) continue;
						
						total++;
						int k = kernelx[dx]*kernely[dy];
						r+= k*this.getSample(xk, yk, 0);
						g+= k*this.getSample(xk, yk, 1);
						b+= k*this.getSample(xk, yk, 2);
						ksum+=k;
					}
				}
				if (ksum>0) {
					newimage.setSample(nx, ny, 0, (int)((double)r/ksum+0.5));
					newimage.setSample(nx, ny, 1, (int)((double)g/ksum+0.5));
					newimage.setSample(nx, ny, 2, (int)((double)b/ksum+0.5));
				}
			}
		}
		return newimage;
	}

	// return an upscaled image
	public ImageMap upscale(int newW,int newH) {
		ImageMap newimage = new ImageMap(newW, newH);
		newimage.image = resize(this.image, newW, newH);
		return newimage;
	}

    //return a local variance map
    public double[][] localVariance(int blockSize){
        double[][] grayscaleIm = new double[w][h];
        double[][] outputVariance = new double[w][h];

        int R,G,B;
        for (int ii = 0; ii < w; ii++) {
            for (int jj = 0; jj < h; jj++) {
                R = this.getSample(ii, jj, 0);
                G = this.getSample(ii, jj, 1);
                B = this.getSample(ii, jj, 2);
                grayscaleIm[ii][jj] = 0.2989 * R + 0.5870 * G + 0.1140 * B;
            }
        }
        double[][] blockVar=Util.blockVar(grayscaleIm, blockSize);
        for (int ii=0; ii<(int)Math.floor(((float) w)/blockSize)*blockSize;ii++){
            for (int jj=0; jj<(int)Math.floor(((float) h)/blockSize)*blockSize;jj++){
                outputVariance[ii][jj] = blockVar[(int)Math.floor(((float)ii)/blockSize)][(int)Math.floor(((float)jj)/blockSize)];
            }
        }
        return outputVariance;
    }

}
