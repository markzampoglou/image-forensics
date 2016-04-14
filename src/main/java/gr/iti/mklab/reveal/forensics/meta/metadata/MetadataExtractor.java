package gr.iti.mklab.reveal.forensics.meta.metadata;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;

import java.io.File;
import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Created by marzampoglou on 11/18/15.
 */
public class MetadataExtractor {
    public JsonObject metadataReport = null;

    public MetadataExtractor(String FileName) throws IOException, ImageProcessingException {
        this.metadataReport = getMetadata(FileName);
    }

    private JsonObject getMetadata(String FileName) throws IOException, ImageProcessingException {
        com.drew.metadata.Metadata metadata = ImageMetadataReader.readMetadata(new File(FileName));
        JsonObject jsonObject;

/*
        JsonObject JSONChild;
        jsonObject=new JsonObject();
        for (Directory directory : meta.metadata.getDirectories()) {
            //System.out.println(directory.getName());
            JSONChild = new JsonObject();
            for (Tag tag : directory.getTags()) {
                //System.out.println(tag.getTagName() + " - " + tag.getDescription());
                JSONChild.addProperty(tag.getTagName(),tag.getDescription());
            }
            jsonObject.add(directory.getName(),JSONChild);
        }
        //System.out.println(jsonObject.toString());
*/
        jsonObject=new JsonObject();
        JsonObject jsonChild;
        JsonArray jsonDirArray=new JsonArray();
        JsonArray jsonTagArray;
        JsonObject jsonTagChild;

        boolean runningExifIFD0;
        boolean foundExifIFD0=false;
        boolean foundCopyright=false;

        jsonObject.addProperty("name", "metadataArray");
        for (Directory directory : metadata.getDirectories()) {
            jsonChild = new JsonObject();
            jsonChild.addProperty("name", directory.getName());
            if (directory.getName().equalsIgnoreCase("Exif IFD0")) {
                runningExifIFD0 = true;
                foundExifIFD0=true;
                System.out.println("IFD0 found");
            } else
            {
                runningExifIFD0 = false;
            }

            jsonTagArray=new JsonArray();
            for (Tag tag : directory.getTags()) {
                jsonTagChild =new JsonObject();
                jsonTagChild.addProperty("name", tag.getTagName());
                jsonTagChild.addProperty("value", tag.getDescription());
                jsonTagArray.add(jsonTagChild);
                if (tag.getTagName().equalsIgnoreCase("Copyright")){
                    foundCopyright=true;
                }
            }
            if (runningExifIFD0 & !foundCopyright) {
                jsonTagChild =new JsonObject();
                jsonTagChild.addProperty("name", "Copyright");
                jsonTagChild.addProperty("value", "None found.");
                jsonTagArray.add(jsonTagChild);
                System.out.println("Copyright added manually");
            }
            if (directory.getTagCount()==0){
                jsonTagChild =new JsonObject();
                jsonTagChild.addProperty("name", "Info");
                jsonTagChild.addProperty("value", "This category exists but is empty.");
                jsonTagArray.add(jsonTagChild);
            }
            jsonChild.add("values", jsonTagArray);
            jsonDirArray.add(jsonChild);
        }
        if (!foundExifIFD0) {
            System.out.println("IFD0 not found, added manually");
            jsonChild = new JsonObject();
            jsonChild.addProperty("name", "Exif IFD0");
            jsonTagArray=new JsonArray();
            jsonTagChild =new JsonObject();
            jsonTagChild.addProperty("name", "Copyright");
            jsonTagChild.addProperty("value", "None found.");
            jsonTagArray.add(jsonTagChild);
            jsonChild.add("values", jsonTagArray);
            jsonDirArray.add(jsonChild);
        }
        jsonObject.add("values", jsonDirArray);

        //System.out.println(JSONObj2.toString());
        //System.out.println("----------------------------------------------------------");
        //System.out.println(jsonObject.toString());



        return jsonObject;
    }

    private JsonObject ReformatNameValue(JsonObject InputElement){
        JsonObject OutputElement=new JsonObject();

        return null;
    }

}