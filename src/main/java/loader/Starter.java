package loader;

import client.ClientStart;
import server.ServerStart;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Properties;

public class Starter {
    public static void main(String[] args) throws Exception {
        ArrayList <String> properties = getProperties();
        new Thread(new ClientStart()).start();
        new Thread(new ServerStart()).start();
    }
    public static ArrayList<String> getProperties() throws Exception {
        File file = new File("src\\main\\java\\loader\\config.properties");
        Properties properties = new Properties();
        properties.load(new FileReader(file));
        ArrayList<String> arrayList = new ArrayList<>();
        for (String key : properties.stringPropertyNames()){
            arrayList.add(key);
        }
        return arrayList;
    }
}
