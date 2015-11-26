package ForensicsToolbox;

import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by marzampoglou on 11/25/15.
 */
@Embedded
public class BLKReport {
    public Boolean completed=false;
    public String Map;
    public double MaxValue;
    public double MinValue;
}
