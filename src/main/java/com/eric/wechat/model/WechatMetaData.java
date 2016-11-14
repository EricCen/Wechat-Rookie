package com.eric.wechat.model;

import com.blade.kit.json.JSONObject;
import com.eric.wechat.config.Constants;
import com.eric.wechat.util.TimeUtils;

/**
 * Created by Eric Cen on 2016/11/14.
 */
public class WechatMetaData {
    private String base_url, redirect_url,webpush_url = Constants.BASE_URL;
    private String uuid;
    private String skey;
    private String syncKeyStr;
    private String wxsid;
    private String wxuin;
    private String pass_ticket;
    private String deviceId = "e" + TimeUtils.getCurrentUnixTime();

    private String cookie;

    private JSONObject baseRequest;
    private JSONObject syncKey;
    private JSONObject user;

    public WechatMetaData() {

    }

    public String getBase_url() {
        return base_url;
    }

    public void setBase_url(String base_url) {
        this.base_url = base_url;
    }

    public String getRedirect_url() {
        return redirect_url;
    }

    public void setRedirect_url(String redirect_url) {
        this.redirect_url = redirect_url;
    }

    public String getWebpush_url() {
        return webpush_url;
    }

    public void setWebpush_url(String webpush_url) {
        this.webpush_url = webpush_url;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSkey() {
        return skey;
    }

    public void setSkey(String skey) {
        this.skey = skey;
    }

    public String getSyncKeyStr() {
        return syncKeyStr;
    }

    public void setSyncKeyStr(String syncKeyStr) {
        this.syncKeyStr = syncKeyStr;
    }

    public String getWxsid() {
        return wxsid;
    }

    public void setWxsid(String wxsid) {
        this.wxsid = wxsid;
    }

    public String getWxuin() {
        return wxuin;
    }

    public void setWxuin(String wxuin) {
        this.wxuin = wxuin;
    }

    public String getPass_ticket() {
        return pass_ticket;
    }

    public void setPass_ticket(String pass_ticket) {
        this.pass_ticket = pass_ticket;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public JSONObject getBaseRequest() {
        return baseRequest;
    }

    public void setBaseRequest(JSONObject baseRequest) {
        this.baseRequest = baseRequest;
    }

    public JSONObject getSyncKey() {
        return syncKey;
    }

    public void setSyncKey(JSONObject syncKey) {
        this.syncKey = syncKey;
    }

    public JSONObject getUser() {
        return user;
    }

    public void setUser(JSONObject user) {
        this.user = user;
    }
}
