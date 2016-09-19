package gr.iti.mklab.reveal.util;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by kandreadou on 2/2/15.
 */
public class Configuration {

    public static String INDEX_SERVICE_HOST;
    public static String STREAM_MANAGER_SERVICE_HOST;
    public static String MONGO_HOST;
    public static boolean ADD_SOCIAL_MEDIA;
    public static String MANIPULATION_REPORT_PATH;
    public static boolean PUBLISH_RABBITMQ;
    public static int NUM_GHOST_THREADS;
    public static long FORENSIC_PROCESS_TIMEOUT;
    public static int MAX_GHOST_IMAGE_SMALL_DIM;
    public static String HTTP_HOST;

    public static void load(String file) throws ConfigurationException {
        PropertiesConfiguration conf = new PropertiesConfiguration(file);
        INDEX_SERVICE_HOST = conf.getString("indexServiceHost");
        STREAM_MANAGER_SERVICE_HOST = conf.getString("streamManagerServiceHost");
        MONGO_HOST = conf.getString("mongoHost");
        HTTP_HOST=conf.getString("httpHost");
    }

    public static void load(InputStream stream) throws ConfigurationException, IOException {
        Properties conf = new Properties();
        conf.load(stream);
    
        INDEX_SERVICE_HOST = conf.getProperty("indexServiceHost");
        STREAM_MANAGER_SERVICE_HOST = conf.getProperty("streamManagerServiceHost");
        MONGO_HOST = conf.getProperty("mongoHost");
        ADD_SOCIAL_MEDIA = Boolean.valueOf(conf.getProperty("getSocialMedia"));
        MANIPULATION_REPORT_PATH = conf.getProperty("manipulationReportPath");
        PUBLISH_RABBITMQ = Boolean.parseBoolean(conf.getProperty("publish"));
        NUM_GHOST_THREADS=Integer.parseInt(conf.getProperty("numGhostThreads", "3"));
        FORENSIC_PROCESS_TIMEOUT=Long.parseLong(conf.getProperty("ForensicProcessTimeout", "30000"));
        MAX_GHOST_IMAGE_SMALL_DIM=Integer.parseInt(conf.getProperty("MaxGhostImageSmallDimension", "768"));
        HTTP_HOST=conf.getProperty("httpHost");
    }
}
