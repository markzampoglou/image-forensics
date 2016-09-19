package gr.iti.mklab.reveal.forensics.api.reports;

import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by marzampoglou on 11/19/15.
 */
@Embedded
public class ELAReport {
    public Boolean completed=false;
    public String map;
    public double maxValue;
    public double minvalue;
}
