package gr.iti.mklab.reveal.forensics.api.reports;

import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by marzampoglou on 1/19/16.
 */
@Embedded
public class GPSReport {
    public Boolean completed=false;
    public Boolean exists=false;
    public double latitude;
    public double longitude;
}
