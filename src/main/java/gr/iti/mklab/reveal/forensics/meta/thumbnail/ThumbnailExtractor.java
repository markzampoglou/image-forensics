package gr.iti.mklab.reveal.forensics.meta.thumbnail;

import gr.iti.mklab.reveal.forensics.util.ThumbnailExtractor.image.jpeg.JPEGMetaData;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by marzampoglou on 12/3/15.
 */
public class ThumbnailExtractor {
    public List<BufferedImage> thumbnails =new ArrayList<>();
    public int numberOfThumbnails =0;

    public ThumbnailExtractor(String FileName) {
        try {
            getThumbnails(FileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getThumbnails(String FileName) throws IOException {
        File input = new File(FileName);
        ImageInputStream iis = ImageIO.createImageInputStream(input);
        Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
        if (!iter.hasNext())  throw new IOException();
        ImageReader reader=iter.next();
        reader.setInput(iis);

        System.out.println(reader.getNumImages(true));
        numberOfThumbnails =reader.getNumThumbnails(0);
        if (numberOfThumbnails !=0) {
            BufferedImage thumb;
            for (int ii = 0; ii < numberOfThumbnails; ii++) {
                thumb = reader.readThumbnail(0, ii);
                thumbnails.add(thumb);
            }
            iis.close();
        } else {
            BufferedImage thumb = JPEGMetaData.getThumbnail(new File(FileName));
            if (thumb!=null){
                numberOfThumbnails =1;
                thumbnails.add(thumb);
            } else {
                numberOfThumbnails =0;
            }
        }
    }
}
