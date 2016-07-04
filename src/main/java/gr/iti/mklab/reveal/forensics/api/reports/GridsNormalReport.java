package gr.iti.mklab.reveal.forensics.api.reports;

import org.mongodb.morphia.annotations.Embedded;


@Embedded
public class GridsNormalReport {
    public Boolean completed=false;
    public String map;
    public double maxValue;
    public double minValue;

}
