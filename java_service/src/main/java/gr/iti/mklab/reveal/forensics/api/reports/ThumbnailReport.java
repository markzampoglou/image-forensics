package gr.iti.mklab.reveal.forensics.api.reports;

import org.mongodb.morphia.annotations.Embedded;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marzampoglou on 12/3/15.
 */
@Embedded
public class ThumbnailReport {
    public int numberOfThumbnails =0;
    public List<String> thumbnailList = new ArrayList();
}
