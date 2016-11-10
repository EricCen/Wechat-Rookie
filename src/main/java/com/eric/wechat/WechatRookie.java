package com.eric.wechat;

import com.eric.wechat.service.WeChatService;
import com.eric.wechat.service.WeChatServiceImpl;
import com.eric.wechat.util.TimeUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

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
        File qrCodeImage  = new File("QrCode.jpg");
        weChatService.getQrCodeImage(qrCodeImage,uuid);
        LOGGER.info("Get the UUID: " + uuid);
        if(qrCodeImage != null && qrCodeImage.exists() && qrCodeImage.isFile()){
            try {
                HttpServer qrCodeServer = HttpServer.create();
                qrCodeServer.bind(new InetSocketAddress(8099),20);
                qrCodeServer.createContext("/wechatLogin", new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                      String realLoginUrl = "https://login.weixin.qq.com/qrcode/" + uuid +
                              "?t=webwx&_=" + String.valueOf(TimeUtils.getCurrentUnixTime());
                      System.out.println(realLoginUrl);
                      exchange.sendResponseHeaders(200,realLoginUrl.length()*2);
                      exchange.getResponseBody().write(("<script>window.location=\"" + realLoginUrl + "\"</script>").getBytes());
                    }
                });
                qrCodeServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

