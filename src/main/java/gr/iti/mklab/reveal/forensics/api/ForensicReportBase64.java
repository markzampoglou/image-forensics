package gr.iti.mklab.reveal.forensics.api;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author olgapapa
 *
 */

public class ForensicReportBase64 {

	public String displayImageBase64 = "";
	public int widthdisplayImage;
	public List<String> thumbBase64 = new ArrayList();
	public List<Integer> widththumb = new ArrayList();
	public String dqBase64="";
	public int widthdq;
    public String dwNoiseBase64 ="";
    public int widthdwNoise;
    public List<String> ghostBase64 = new ArrayList();
    public List<Integer> widthghost = new ArrayList();
    public String elaBase64 ="";
    public int widthela;
    public String blockingBase64 ="";
    public int widthblocking;
    public String medianNoiseBase64 = "";
    public int widthmedianNoise;
    public String gridsBase64 ="";
    public int widthgrids;
    public String gridsInversedBase64 = "";
    public int widthgridsInversed;
}
