package api;

import java.util.ArrayList;
import java.util.List;
import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by marzampoglou on 11/19/15.
 */
@Embedded
public class GhostReport {
    public Boolean completed=false;
    public List<String> Maps = new ArrayList();
    public String GIFMaps;
    public List<Integer> Qualities = new ArrayList();
    public List<Float> Differences = new ArrayList();
    public List<Float> MinValues = new ArrayList();
    public List<Float> MaxValues = new ArrayList();
    public int MinQuality;
    public int MaxQuality;
}
