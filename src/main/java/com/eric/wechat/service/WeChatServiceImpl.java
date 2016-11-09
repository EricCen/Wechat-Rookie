package com.eric.wechat.service;

import com.eric.wechat.config.Constants;
import com.eric.wechat.util.Matchers;
import com.eric.wechat.util.TimeUtils;
import okhttp3.*;

import java.io.IOException;

/**
 * Created by Eric Cen on 2016/11/9.
 */
public class WeChatServiceImpl implements WeChatService {

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
}