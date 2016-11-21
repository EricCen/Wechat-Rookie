package com.eric.wechat.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Eric Cen on 2016/11/9.
 */
public class Configuration {
    private Properties properties;
    private static Configuration instance = new Configuration();
    private static final String LOCATION = "config.properties";

    private Configuration(){
        this.properties = new Properties();
        load(LOCATION);
    }

    public static Configuration getInstance(){
       if(instance == null){
           instance = new Configuration();
       }
        return instance;
    }

    public void load(String location){
        InputStream is = Configuration.class.getClassLoader().getResourceAsStream(location);
        try{
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Can't load properties from class path");
        }
    }

    public String get(String property){
        return (String)properties.get(property);
    }
}
