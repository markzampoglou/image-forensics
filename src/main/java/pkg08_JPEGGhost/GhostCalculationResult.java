package pkg08_JPEGGhost;

/**
 * Created by marzampoglou on 11/3/15.
 */
public class GhostCalculationResult {
    private int Quality;
    private float[][] difference;

    float ghostMin;
    float ghostMax;
    float ghostMean;

    public GhostCalculationResult(int Quality, float[][] difference) {
        this.Quality = Quality;
        this.difference = difference;
    }
    public int getQuality() {
        return Quality;
    }

    public float[][] getDifference() {
        return difference;
    }

}
