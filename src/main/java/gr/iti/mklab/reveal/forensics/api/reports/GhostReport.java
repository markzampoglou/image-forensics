package gr.iti.mklab.reveal.forensics.api.reports;

import java.util.ArrayList;
import java.util.List;
import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by marzampoglou on 11/19/15.
 */
@Embedded
public class GhostReport {
    public Boolean completed=false;
    public List<String> maps = new ArrayList();
    public String gifMaps;
    public List<Integer> qualities = new ArrayList();
    public List<Float> differences = new ArrayList();
    public List<Float> minValues = new ArrayList();
    public List<Float> maxValues = new ArrayList();
    public int minQuality;
    public int maxQuality;
}
