package gr.iti.mklab.reveal.forensics.api;

import gr.iti.mklab.reveal.forensics.api.reports.*;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;


/**
 * Created by marzampoglou on 11/19/15.
 */
@Entity
public class ForensicReport {
    public @Id String id;
    public String status="";
    public String sourceImage ="";
    public String displayImage ="";
    public String sourceURL ="";
    public String metadataStringReport ="{completed: false}";
    public Object metadataObjectReport =null;
    public @Embedded
    ThumbnailReport thumbnailReport = new ThumbnailReport();
    public @Embedded
    dqReport dqReport =new dqReport();
    public @Embedded
    DWNoiseReport dwNoiseReport =new DWNoiseReport();
    public @Embedded
    GhostReport ghostReport =new GhostReport();
    public @Embedded
    ELAReport elaReport =new ELAReport();
    public @Embedded
    BlockingReport blockingReport =new BlockingReport();
    public @Embedded
    MedianNoiseReport medianNoiseReport =new MedianNoiseReport();
    public @Embedded
    GPSReport gpsReport =new GPSReport();	
    public @Embedded
    GridsNormalReport gridsReport =new GridsNormalReport();
    public @Embedded
    GridsInversedReport gridsInversedReport =new GridsInversedReport();
}
