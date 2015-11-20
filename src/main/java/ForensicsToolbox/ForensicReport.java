package ForensicsToolbox;

import com.google.gson.JsonObject;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;

/**
 * Created by marzampoglou on 11/19/15.
 */
@Entity
public class ForensicReport {
    public @Id String id;
    public String SourceImage;
    public String DisplayImage;
    public String SourceURL;
    public String MetadataReport;
    public @Embedded DQReport DQ_Report=new DQReport();
    @Embedded NoiseDWReport NoiseDW_Report=new NoiseDWReport();
    @Embedded GhostReport Ghost_Report=new GhostReport();
    @Embedded ELAReport ELA_Report=new ELAReport();
}
