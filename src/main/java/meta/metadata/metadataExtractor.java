package meta.metadata;

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
public class metadataExtractor {
    public JsonObject MetadataReport = null;

    public metadataExtractor(String FileName) throws IOException, ImageProcessingException {
        this.MetadataReport=GetMetadata(FileName);
    }

    private JsonObject GetMetadata(String FileName) throws IOException, ImageProcessingException {
        com.drew.metadata.Metadata metadata = ImageMetadataReader.readMetadata(new File(FileName));
        JsonObject JSONObj;
/*
        JsonObject JSONChild;
        JSONObj=new JsonObject();
        for (Directory directory : meta.metadata.getDirectories()) {
            //System.out.println(directory.getName());
            JSONChild = new JsonObject();
            for (Tag tag : directory.getTags()) {
                //System.out.println(tag.getTagName() + " - " + tag.getDescription());
                JSONChild.addProperty(tag.getTagName(),tag.getDescription());
            }
            JSONObj.add(directory.getName(),JSONChild);
        }
        //System.out.println(JSONObj.toString());
*/
        JSONObj=new JsonObject();
        JsonObject JSONDirChild;
        JsonArray JsonDirArray=new JsonArray();
        JsonArray JsonTagArray;
        JsonObject JSONTagChild;

        JSONObj.addProperty("name","metadataArray");
        for (Directory directory : metadata.getDirectories()) {
            JSONDirChild = new JsonObject();
            JSONDirChild.addProperty("name", directory.getName());
            JsonTagArray=new JsonArray();
            for (Tag tag : directory.getTags()) {
                JSONTagChild=new JsonObject();
                JSONTagChild.addProperty("name",tag.getTagName());
                JSONTagChild.addProperty("value", tag.getDescription());
                JsonTagArray.add(JSONTagChild);
            }
            if (directory.getTagCount()==0){
                JSONTagChild=new JsonObject();
                JSONTagChild.addProperty("name","REVEAL warning");
                JSONTagChild.addProperty("value", "This category exists but is empty. It is possible that its content was erased.");
                JsonTagArray.add(JSONTagChild);
            }
            JSONDirChild.add("values", JsonTagArray);
            JsonDirArray.add(JSONDirChild);
        }
        JSONObj.add("values",JsonDirArray);

        //System.out.println(JSONObj2.toString());
        //System.out.println("----------------------------------------------------------");
        //System.out.println(JSONObj.toString());



        return JSONObj;
    }

    private JsonObject ReformatNameValue(JsonObject InputElement){
        JsonObject OutputElement=new JsonObject();

        return null;
    }
}