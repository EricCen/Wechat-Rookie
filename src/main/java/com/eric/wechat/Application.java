package com.eric.wechat;

import com.eric.wechat.config.Configuration;

/**
 * Created by Eric Cen on 2016/11/9.
 */
public class Application {
    public static void main(String[] args){
        Configuration configuration = Configuration.getInstance();
        configuration.load("config.properties");

    }
}
