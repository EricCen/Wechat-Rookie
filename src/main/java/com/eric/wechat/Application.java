package com.eric.wechat;

import com.eric.wechat.config.Configuration;
import com.eric.wechat.config.Constants;

/**
 * Created by Eric Cen on 2016/11/9.
 */
public class Application {
    public static void main(String[] args){
        Configuration configuration = Configuration.getInstance();
        configuration.load("config.properties");
        WechatRookie wechatRookie = new WechatRookie();
        wechatRookie.showQrCode();
        while(!Constants.HTTP_OK.equals(wechatRookie.waitForLogin())){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        wechatRookie.start();
    }
}
