/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ForensicsToolbox;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author marzampoglou
 */
public class GhostAnalysis {
    public List<String> GhostOutput = new ArrayList();
    //public String GhostGIFOutput;
    public List<Integer> GhostQualities = new ArrayList();

    public List<Double> GhostDifferences = new ArrayList();
    public List<Double> Ghost_MinValues = new ArrayList();
    public List<Double> Ghost_MaxValues = new ArrayList();
    public int GhostMinQuality;
    public int GhostMaxQuality;
}
