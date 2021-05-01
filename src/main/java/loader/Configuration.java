package loader;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class Configuration {
    private File file;
    private Properties properties;
    public Configuration() throws Exception {
        file = new File("D:\\Java_laba\\ServerAndClientsChat\\src\\main\\java\\loader\\config.properties");
        properties = new Properties();
        properties.load(new FileReader(file));
    }
    public String getHostname(){
        return properties.getProperty("hostname");
    }
    public int getPort(){
        return Integer.parseInt(properties.getProperty("port"));
    }
    public String getType(){
        return properties.getProperty("typeOfConnection");
    }
    public int getTimer(){ return Integer.parseInt(properties.getProperty("timeout"));}

}
