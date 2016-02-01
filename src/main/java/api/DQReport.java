package api;

import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by marzampoglou on 11/19/15.
 */
@Embedded
public class DQReport {
    public Boolean completed=false;
    public String Map;
    public double MaxValue;
    public double MinValue;
}
