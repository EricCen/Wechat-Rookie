package com.eric.wechat.service;

import com.eric.wechat.config.Constants;
import com.eric.wechat.model.WechatContact;
import com.eric.wechat.model.WechatMetaData;
import com.eric.wechat.util.Matchers;
import com.eric.wechat.util.TimeUtils;
import okhttp3.*;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by Eric Cen on 2016/11/9.
 */
public class WeChatServiceImpl implements WeChatService {

    private static Logger LOGGER = Logger.getLogger(WeChatServiceImpl.class);

    private OkHttpClient okHttpClient;

    public WeChatServiceImpl(){
        this.okHttpClient = new OkHttpClient();
    }

    @Override
    public String getUUID() {
        HttpUrl url = new HttpUrl.Builder()
                     .scheme("https")
                     .host("login.weixin.qq.com")
                     .addPathSegment("jslogin")
                     .addQueryParameter("appid","wx782c26e4c19acffb")
                     .addQueryParameter("fun", "new")
                     .addQueryParameter("lang", "zh_CN")
                     .addQueryParameter("_", String.valueOf(TimeUtils.getCurrentUnixTime()))
                     .build();
        Request request = new Request.Builder()
                          .url(url)
                          .build();

        Response response;
        ResponseBody responseBody;
        try {
            response = okHttpClient.newCall(request).execute();
            responseBody = response.body();
            String responseString = responseBody.string();
            if(responseString != null && !responseString.isEmpty()){
                String code = Matchers.match("window.QRLogin.code = (\\d+);", responseString);
                if(null != code){
                    if(Constants.HTTP_OK.equals(code)){
                        return Matchers.match("window.QRLogin.uuid = \"(.*)\";", responseString);
                    } else {
                        throw new RuntimeException("Invalid Status Code:" + code);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Fail to get UUID");
    }

    @Override
    public void getQrCodeImage(File file, String uuid) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("login.weixin.qq.com")
                .addPathSegment("qrcode")
                .addPathSegment(uuid)
                .addQueryParameter("t", "webwx")
                .addQueryParameter("_", String.valueOf(TimeUtils.getCurrentUnixTime()))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response;
        try {
            response = okHttpClient.newCall(request).execute();
            InputStream is = response.body().byteStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            OutputStream os = new FileOutputStream(file);
            int data;
            while((data = bis.read()) > -1){
                os.write(data);
            }
            os.flush();
            os.close();
            bis.close();
            is.close();
        } catch (IOException e) {
            throw new RuntimeException("Fail to get WeChat QrCode image.");
        }
    }

    @Override
    public String loginWechat(int tip, String uuid, WechatMetaData wechatMetaData) {
        HttpUrl url = new HttpUrl.Builder()
                         .scheme("https")
                         .host("login.weixin.qq.com")
                         .addPathSegment("cgi-bin")
                         .addPathSegment("mmwebwx-bin")
                         .addPathSegment("login")
                         .addQueryParameter("tip", String.valueOf(tip))
                         .addQueryParameter("uuid",uuid)
                         .addQueryParameter("_",String.valueOf(TimeUtils.getCurrentUnixTime()))
                         .build();
        LOGGER.info("Waiting for login wechat");

        Request request = new Request.Builder()
                              .url(url)
                              .build();

        String code = null;
        try {
            Response response = okHttpClient.newCall(request).execute();
            ResponseBody responseBody = response.body();
            String responseString = responseBody.string();
            if(responseString == null){
                throw new RuntimeException("Fail to Scan QR Code");
            }
            code = Matchers.match("window.code=(\\d+);", responseString);
            if(null == code){
                throw new RuntimeException("Fail to Scan QR Code");
            } else if("201".equals(code)){
                LOGGER.info("Scan QR Code successfully, please click login in your smart phone.");
            } else if(code.equals("200")){
                LOGGER.info("Login.....");
                String pm = Matchers.match("window.redirect_uri=\"(\\S+?)\";", responseString);
                String redirect_url = pm + "&fun=new";
                wechatMetaData.setRedirect_url(redirect_url);
                String base_url = redirect_url.substring(0, redirect_url.lastIndexOf("/"));
                wechatMetaData.setBase_url(base_url);

                LOGGER.info("redirect_url=" + redirect_url);
                LOGGER.info("base_url=" + base_url);

            } else if(code.equals("408")){
                throw new RuntimeException("Login Timeout!");
            } else {
                LOGGER.info("Scan code = " + code);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return code;
    }

    public void wxInit(WechatMetaData wechatMetaData){

    }

    @Override
    public void openStatusNotify(WechatMetaData wechatMetaData) {

    }

    @Override
    public WechatContact getContact(WechatMetaData wechatMetaData) {
        return null;
    }


}
