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
	public List<String> thumbBase64 = new ArrayList();
	public String dqBase64="";
    public String dwNoiseBase64 ="";
    public List<String> ghostBase64 = new ArrayList();
    public String elaBase64 ="";
    public String blockingBase64 ="";
    public String medianNoiseBase64 = "";   
}
