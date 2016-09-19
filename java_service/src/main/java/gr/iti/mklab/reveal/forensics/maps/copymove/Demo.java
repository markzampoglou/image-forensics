package gr.iti.mklab.reveal.forensics.maps.copymove;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

public class Demo {
	
	// display widget
	static JLabel jlabel;

	public static void display(BufferedImage bImg) {
		if (jlabel==null) {
			int h = bImg.getHeight();
			int w = bImg.getWidth();

			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			JPanel panneau = new JPanel();
			JLabel label0 = new JLabel();
			panneau.add(label0);
			JScrollPane scrollPane = new JScrollPane(panneau);
			frame.getContentPane().add(scrollPane);
			frame.setSize(w + 32, h + 64);
			frame.setVisible(true);
			label0.setIcon(new ImageIcon());
			
			jlabel = label0;
		}
		((ImageIcon)jlabel.getIcon()).setImage(bImg);
		jlabel.repaint();
	}
	
	public static BufferedImage loadImage(String filename) {
		BufferedImage image;
		try {
			BufferedImage input = ImageIO.read( new File(filename) );
			// convert to RGB format
			image = new BufferedImage(input.getWidth(),input.getHeight(),ColorSpace.TYPE_RGB);
	        ((Graphics2D) image.getGraphics()).drawImage(input,0,0,null);
		} catch (Exception e) {
			throw new RuntimeException("Error loading Image file '"+filename+"' : "+e.getMessage());
		}
		return image;
	}
	
	public static void main(String[] args) {
 /*
		BufferedImage input = loadImage("Au_ani_10104.jpg");
		BufferedImage maskimage = loadImage("Au_ani_10104_Mask.png");

		// generate mask array from mask image
		int w=maskimage.getWidth(), h=maskimage.getHeight();
		boolean[][] mask = new boolean[w][h];
		for(int y=0;y<h;y++)
			for(int x=0;x<w;x++)
				mask[x][y]=(maskimage.getRGB(x, y)!=0xFF000000);
		
		// overwrite image, to see the mask in RED
		w=input.getWidth(); h=input.getHeight();
		for(int y=0;y<h;y++)
			for(int x=0;x<w;x++)
				if (mask[x][y]) input.setRGB(x, y, 0xFFFF0000);
	
		display(input);
		BufferedImage output = new Inpaint().inpaint(input, mask, 2);
		display(output);
		*/
		BufferedImage imIn = null;
		try {
            //imIn = ImageIO.read(new File("iran_missile_test_2008.jpg"));
            //imIn = ImageIO.read(new File("crowd_fake2.jpg"));
			imIn = ImageIO.read(new File("1st2.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedImage output=new CloneDetect().findClones(imIn, 3);
		//display(output);
		try {
			ImageIO.write(output,"PNG",new File("/home/marzampoglou/Pictures/Reveal/ManipulationOutput/diffMap.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("\nDONE.");
	}
}
