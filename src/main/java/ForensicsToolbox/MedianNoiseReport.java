package ForensicsToolbox;

import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by marzampoglou on 11/19/15.
 */
@Embedded
public class MedianNoiseReport {
    public Boolean completed=false;
    public String Map;
}
