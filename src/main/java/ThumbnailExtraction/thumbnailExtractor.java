package ThumbnailExtraction;

import Utils.ThumbnailExtractor.image.jpeg.JPEGMetaData;
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
public class thumbnailExtractor {
    public List<BufferedImage> Thumbnails =new ArrayList<>();
    public int NumberOfThumbnails=0;

    public thumbnailExtractor(String FileName) {
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
        NumberOfThumbnails=reader.getNumThumbnails(0);
        if (NumberOfThumbnails!=0) {
            BufferedImage thumb;
            for (int ii = 0; ii < NumberOfThumbnails; ii++) {
                thumb = reader.readThumbnail(0, ii);
                Thumbnails.add(thumb);
            }
            iis.close();
        } else {
            BufferedImage thumb = JPEGMetaData.getThumbnail(new File(FileName));
            if (thumb!=null){
                NumberOfThumbnails=1;
                Thumbnails.add(thumb);
            } else {
                NumberOfThumbnails=0;
            }
        }
    }
}
