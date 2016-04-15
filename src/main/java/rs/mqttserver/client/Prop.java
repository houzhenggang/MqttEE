package rs.mqttserver.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Prop {

	final static Logger log = Logger.getLogger(Prop.class);
	
    private static Prop instance = null;
    private Properties properties;
    InputStream input;

    protected Prop() throws IOException{
    	log.info("Loading properties...");
    	String configFile = System.getProperty("user.dir")+ File.separator + "config.properties";
    	properties = new Properties();
    	input = new FileInputStream(configFile);
    	properties.load(input);
    	log.info("Properties initialized");
    }

    public synchronized static Prop getInstance() {
        if(instance == null) {
            try {
                instance = new Prop();
            } catch (IOException e) {
                log.error(e.getMessage(),e);
            }
        }
        return instance;
    }

    public synchronized String getValue(String key) {
        return properties.getProperty(key);
    }

}