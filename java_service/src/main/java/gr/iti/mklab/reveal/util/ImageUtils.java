package gr.iti.mklab.reveal.util;

import java.awt.image.BufferedImage;
import java.util.regex.Pattern;

/**
 * Created by kandreadou on 2/10/15.
 */
public class ImageUtils {

    private final static int MIN_CONTENT_LENGTH = 20000;
    private final static int MIN_WIDTH = 400;
    private final static int MIN_HEIGHT = 400;

    private final static Pattern imagePattern = Pattern.compile("([^\\s]+(\\.(?i)(jpg|png|gif|bmp|jpeg|tiff))$)");


    public static boolean checkContentHeaders(int contentLength, String contentType) {
        return contentLength > MIN_CONTENT_LENGTH && contentType.startsWith("image");
    }

    public static boolean checkImage(BufferedImage img) {
        return img != null && img.getWidth() >= MIN_WIDTH && img.getHeight() >= MIN_HEIGHT;
    }

    public static boolean isImageBigEnough(int width, int height) {
        return width >= MIN_WIDTH && height >= MIN_HEIGHT;
    }

    public static boolean isImageSmall(int width, int height) {
        return width > 0 && width < MIN_WIDTH && height > 0 && height < MIN_HEIGHT;
    }

    public static boolean isImageUrl(String uri) {
        return imagePattern.matcher(uri).matches();
    }

}
