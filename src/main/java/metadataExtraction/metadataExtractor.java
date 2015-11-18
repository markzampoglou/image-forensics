package metadataExtraction;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.File;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;





/**
 * Created by marzampoglou on 11/18/15.
 */
public class metadataExtractor {
    public String MetadataReport = null;

    public metadataExtractor(String FileName) throws IOException, ImageProcessingException {
        GetMetadata(FileName);
    }

    private String GetMetadata(String FileName) throws IOException, ImageProcessingException {

        com.drew.metadata.Metadata metadata = ImageMetadataReader.readMetadata(new File(FileName));
        JsonObject JSONObj=new JsonObject();
        JsonObject JSONChild;


        for (Directory directory : metadata.getDirectories()) {
            System.out.println(directory.getName());
            JSONChild = new JsonObject();
            for (Tag tag : directory.getTags()) {
                System.out.println(tag.getTagName() + " - " + tag.getDescription());
                JSONChild.addProperty(tag.getTagName(),tag.getDescription());
            }
            JSONObj.add(directory.getName(),JSONChild);
        }

        System.out.println("----------------------------------------------------------");

        System.out.println(JSONObj.toString());


        return "Done";
    }
}
