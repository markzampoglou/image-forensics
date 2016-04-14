package gr.iti.mklab.reveal.forensics.meta.gps;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Created by marzampoglou on 1/19/16.
 */
public class GPSExtractor {
    public boolean exists=false;
    public double latitude=Double.MAX_VALUE;
    public double longitude=Double.MAX_VALUE;

    public GPSExtractor(JsonObject metadata) {
        getGPSData(metadata);
    }

    private void getGPSData(JsonObject metadata) {
        System.out.println("getGPSData called");
        JsonArray jsonArray = metadata.getAsJsonArray("values");
        for (int fieldInd=0; fieldInd < jsonArray.size(); fieldInd++){
            JsonObject child = jsonArray.get(fieldInd).getAsJsonObject();
            if (child.get("name").getAsString().equalsIgnoreCase("GPS")){
                JsonArray gpsObject = child.getAsJsonArray("values");
                System.out.println("Found GPS data");
                String latitudeRef=null;
                String longitudeRef=null;
                String latitudeString=null;
                String longitudeString=null;
                for (int gpsFieldInd=0; gpsFieldInd<gpsObject.size();gpsFieldInd++){
                    if (gpsObject.get(gpsFieldInd).getAsJsonObject().get("name").getAsString().equalsIgnoreCase("GPS Latitude Ref")){
                        latitudeRef=gpsObject.get(gpsFieldInd).getAsJsonObject().get("value").getAsString();
                    }
                    if (gpsObject.get(gpsFieldInd).getAsJsonObject().get("name").getAsString().equalsIgnoreCase("GPS Longitude Ref")){
                        longitudeRef=gpsObject.get(gpsFieldInd).getAsJsonObject().get("value").getAsString();
                    }
                    if (gpsObject.get(gpsFieldInd).getAsJsonObject().get("name").getAsString().equalsIgnoreCase("GPS Latitude")){
                        if (!gpsObject.get(gpsFieldInd).getAsJsonObject().get("value").isJsonNull()) {
                            latitudeString = gpsObject.get(gpsFieldInd).getAsJsonObject().get("value").getAsString();
                        }
                    }
                    if (gpsObject.get(gpsFieldInd).getAsJsonObject().get("name").getAsString().equalsIgnoreCase("GPS Longitude")){
                        if (!gpsObject.get(gpsFieldInd).getAsJsonObject().get("value").isJsonNull()) {
                            longitudeString = gpsObject.get(gpsFieldInd).getAsJsonObject().get("value").getAsString();
                        }
                    }
                }
                if ((latitudeRef!=null) &&  (longitudeRef!=null) &&  (latitudeString!=null) && (longitudeString!=null)) {
                        String[] strArray = latitudeString.split("[°\"']");
                        latitude=Math.abs(Double.parseDouble(strArray[0])) + Double.parseDouble(strArray[1]) / 60 + Double.parseDouble(strArray[2]) / 3600;
                        String[] strArray2 = longitudeString.split("[°\"']");
                        longitude=Math.abs(Double.parseDouble(strArray2[0])) + Double.parseDouble(strArray2[1]) / 60 + Double.parseDouble(strArray2[2]) / 3600;
                    if (latitudeRef.equalsIgnoreCase("S")) latitude=-latitude;
                    if (longitudeRef.equalsIgnoreCase("W")) longitude=-longitude;
                }
            }
            }

        if (latitude!=Double.MAX_VALUE && longitude!=Double.MAX_VALUE) {
            System.out.println("GPS data extracted");
            exists=true;
        } else {
            System.out.println("GPS extraction failed");
            latitude=0;
            longitude=0;
        }

    }
}
