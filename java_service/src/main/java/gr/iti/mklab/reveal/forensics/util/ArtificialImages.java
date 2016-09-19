package gr.iti.mklab.reveal.forensics.util;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by marzampoglou on 11/19/15.
 */
public class ArtificialImages {
    public static BufferedImage transparentPNGNotAccepted() {
        String text = "PNG files with ";

        /*
         Because font metrics is based on a graphics context, we need to create
         a small, temporary image so we can ascertain the width and height
         of the final image
         */
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        Font font = new Font("Arial", Font.PLAIN, 48);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text) + 13;
        int height = (int) Math.round(fm.getHeight() * 3.7);
        g2d.dispose();

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.WHITE);
        g2d.drawString("Analysis not", 13, 10 + fm.getAscent());
        g2d.drawString("possible for", 13, 10 + 2 * fm.getAscent());
        g2d.drawString("images with", 13, 10 + 3 * fm.getAscent());
        g2d.drawString("transparency", 13, 10 + 4 * fm.getAscent());
        g2d.dispose();
        return img;
    }
}
