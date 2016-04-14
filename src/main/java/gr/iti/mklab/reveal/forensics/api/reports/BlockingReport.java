package gr.iti.mklab.reveal.forensics.api.reports;

import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by marzampoglou on 11/25/15.
 */
@Embedded
public class BlockingReport {
    public Boolean completed=false;
    public String map;
    public double maxValue;
    public double minValue;
}
