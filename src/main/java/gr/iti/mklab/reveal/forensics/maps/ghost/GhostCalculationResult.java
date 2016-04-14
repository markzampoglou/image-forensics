package gr.iti.mklab.reveal.forensics.maps.ghost;

/**
 * Created by marzampoglou on 11/3/15.
 */
public class GhostCalculationResult {
    private int quality;
    private float[][] difference;

    float ghostMin;
    float ghostMax;
    float ghostMean;

    public GhostCalculationResult(int quality, float[][] difference) {
        this.quality = quality;
        this.difference = difference;
    }
    public int getQuality() {
        return quality;
    }

    public float[][] getDifference() {
        return difference;
    }

}
