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
 * @author markzampoglou
 */
public class ForensicAnalysis {

    public String DQ_Lin_Output;
    public String Noise_Mahdian_Output;
    public List<String> GhostOutput = new ArrayList();
    //public String GhostGIFOutput;
    public List<Integer> GhostQualities = new ArrayList();

    public double DQ_Lin_MaxValue;
    public double DQ_Lin_MinValue;
    public double Noise_Mahdian_MaxValue;
    public double Noise_Mahdian_MinValue;

    public List<Double> GhostDifferences = new ArrayList();
    public List<Double> Ghost_MinValues = new ArrayList();
    public List<Double> Ghost_MaxValues = new ArrayList();
    public int GhostMinQuality;
    public int GhostMaxQuality;
}


//Algorithm descriptions:

// Double Quantization analysis detects the traces of consecutive JPEG compressions on an image.
// This method can only work on JPEG images. Images stored in other formats will not be processed.
// When a splice is placed in a JPEG image, and the result is re-saved as JPEG, the untampered
// blocks will bear the traces two consecutive compressions, while the spliced region will (possibly)
// appear to have been compressed only once. This effect may remain visible even when the splice
// also originates from a JPEG image, since most editing operations destroy the original JPEG traces
// on the splice. In the output map, high values (~=1) correspond to high probability of a single compression
// for the corresponding block, while low values (~=0) correspond to low probability of single compression
// Localized red areas in an otherwise blue image are very likely to contain splices. Sporadic variations and
// values around 0.5 (green) should not be taken into account. For more details, see: Lin, Zhouchen, Junfeng 
// He, Xiaoou Tang, and Chi-Keung Tang. "Fast, automatic and fine-grained tampered JPEG image detection via 
// DCT coefficient analysis." Pattern Recognition 42, no. 11 (2009): 2492-2501.

// High-frequency noise patterns differ depending the origin of the image. This method detects the local variance
// of high-frequency information on an image. In the resulting output maps, whether values are high or low is
// irrelevant. What is significant is the presence of localized consistent differences in noise variance values.
// Since high-frequency noise can be affected by the image content, comparisons should be made between visually
// similar areas (e.g. edges to edges, smooth areas to smooth areas). High-frequency noise patterns is not a
// precise detection method, and unless extremely clear patterns appear, it should be used in conjunction with
// other detectors. For more details, see: Mahdian, Babak, and Stanislav Saic. "Using noise inconsistencies for 
// blind image forensics." Image and Vision Computing 27, no. 10 (2009): 1497-1503.

// JPEG ghosts are based on the premise that, when a splice is taken from a JPEG image and placed in another one,
// of different quality, traces of the original JPEG compression can be found. In order to detect them, the image
// is recompressed in all possible quality levels, and each result is subtracted from the original. If the image
// contains a splice, a "Ghost" (i.e. a gap) should appear at the quality level that the splice was originally 
// compressed. In reality, Ghosts often appear as regions of high difference, in contrast to the rest of the image.
// This approach is vulnerable to variations in image content, as high localized differences due to edges will appear
// at various quality levels. However, an entire localized region -corresponding to a scene object- that stands out
// in contrast to the entire rest of the image, is a very strong indicator of tampering. For more details, see:
// Farid, Hany. "Exposing digital forgeries from JPEG ghosts." Information Forensics and Security, IEEE Transactions 
// on 4, no. 1 (2009): 154-160.