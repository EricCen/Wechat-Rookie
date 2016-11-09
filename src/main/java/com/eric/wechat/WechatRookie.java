package com.eric.wechat;

import com.eric.wechat.service.WeChatService;
import com.eric.wechat.service.WeChatServiceImpl;
import org.apache.log4j.Logger;

/**
 * Created by Eric Cen on 2016/11/9.
 */
public class WechatRookie {

    private static Logger LOGGER = Logger.getLogger(WechatRookie.class);

    private WeChatService weChatService = new WeChatServiceImpl();

    public WechatRookie(){
        System.setProperty("https.protocols","TLSv1");
        System.setProperty("jsse.enableSNIExtension", "false");
    }

    public void showQrCode(){
        String uuid = weChatService.getUUID();
        LOGGER.info("Get the UUID: " + uuid);
    }
}

