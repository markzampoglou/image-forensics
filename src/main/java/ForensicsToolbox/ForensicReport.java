package ForensicsToolbox;

import com.google.gson.JsonObject;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import javax.jdo.*;

import javax.xml.crypto.Data;

/**
 * Created by marzampoglou on 11/19/15.
 */
@Entity
public class ForensicReport {
    public @Id String id;
    public String status="";
    public String SourceImage="";
    public String DisplayImage="";
    public String SourceURL="";
    public String MetadataStringReport ="{completed: false}";
    public Object MetadataObjectReport=null;
    public @Embedded ThumbnailReport Thumbnail_Report = new ThumbnailReport();
    public @Embedded DQReport DQ_Report=new DQReport();
    public @Embedded NoiseDWReport NoiseDW_Report=new NoiseDWReport();
    public @Embedded GhostReport Ghost_Report=new GhostReport();
    public @Embedded ELAReport ELA_Report=new ELAReport();
    public @Embedded BLKReport BLK_Report=new BLKReport();
    public @Embedded MedianNoiseReport MedianNoise_Report=new MedianNoiseReport();
}
