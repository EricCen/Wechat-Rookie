package com.eric.wechat.config;

import com.eric.wechat.model.WechatContact;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Eric Cen on 2016/11/9.
 */
public class Constants {
    public static final String WECHAT_JS_LOGIN_URL = "https://login.weixin.qq.com/jslogin";
    public static final String HTTP_OK = "200";
    public static final String BASE_URL = "https://webpush2.weixin.qq.com/cgi-bin/mmwebwx-bin";
    public static WechatContact CONTACT;
    public static final List<String> FILTER_USERS = Arrays.asList("newsapp", "fmessage", "filehelper", "weibo", "qqmail",
            "fmessage", "tmessage", "qmessage", "qqsync", "floatbottle", "lbsapp", "shakeapp", "medianote", "qqfriend",
            "readerapp", "blogapp", "facebookapp", "masssendapp", "meishiapp", "feedsapp", "voip", "blogappweixin",
            "weixin", "brandsessionholder", "weixinreminder", "wxid_novlwrv3lqwv11", "gh_22b87fa7cb3c", "officialaccounts",
            "notification_messages", "wxid_novlwrv3lqwv11", "gh_22b87fa7cb3c", "wxitil", "userexperience_alarm",
            "notification_messages");
}
