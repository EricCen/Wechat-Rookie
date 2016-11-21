package com.eric.wechat.service;

import com.blade.kit.DateKit;
import com.blade.kit.FileKit;
import com.blade.kit.StringKit;
import com.blade.kit.http.HttpRequest;
import com.blade.kit.json.JSONArray;
import com.blade.kit.json.JSONKit;
import com.blade.kit.json.JSONObject;
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
        String url = wechatMetaData.getBase_url() + "/webwxinit?r=" + TimeUtils.getCurrentUnixTime() + "&pass_ticket="
                     + wechatMetaData.getPass_ticket() + "&skey=" + wechatMetaData.getSkey();
        JSONObject body = new JSONObject();
        body.put("BaseRequest", wechatMetaData.getBaseRequest());
        Request request = new Request.Builder()
                          .url(url)
                          .header("Cookie", wechatMetaData.getCookie() == null?"":wechatMetaData.getCookie())
                .post(RequestBody.create(MediaType.parse("application/json;charset=utf-8"),body.toString()))
                .build();

        try(Response response = okHttpClient.newCall(request).execute()){
            String response_string = response.body().string();
            if(response_string == null && response_string.isEmpty()){
                throw new RuntimeException("Fail to Initialize Wechat");
            }
            JSONObject jsonObject = JSONKit.parseObject(response_string);
            if(null != jsonObject){
                JSONObject baseResponse = jsonObject.get("BaseResponse").asJSONObject();
                if(null != baseResponse){
                    int ret = baseResponse.getInt("Ret", -1);
                    if(ret == 0){
                        wechatMetaData.setSyncKey(jsonObject.get("SyncKey").asJSONObject());
                        wechatMetaData.setUser(jsonObject.get("User").asJSONObject());

                        StringBuffer synckey = new StringBuffer();
                        JSONArray list = wechatMetaData.getSyncKey().get("List").asArray();
                        for (int i = 0, len = list.size(); i < len; i++) {
                            JSONObject item = list.get(i).asJSONObject();
                            synckey.append("|" + item.getInt("Key", 0) + "_" + item.getInt("Val", 0));
                        }
                        wechatMetaData.setSyncKeyStr(synckey.substring(1));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openStatusNotify(WechatMetaData wechatMetaData) {
        String url = wechatMetaData.getBase_url() + "/webwxstatusnotify?lang=zh_CN&pass_ticket="
                                                    + wechatMetaData.getPass_ticket();
        JSONObject body = new JSONObject();
        body.put("BaseRequest", wechatMetaData.getBaseRequest());
        body.put("Code", 3);
        body.put("FromUserName", wechatMetaData.getUser().getString("UserName"));
        body.put("ToUserName", wechatMetaData.getUser().getString("UserName"));
        body.put("ClientMsgId", TimeUtils.getCurrentUnixTime());

        Request request = new Request.Builder()
                              .url(url)
                              .header("Cookie", wechatMetaData.getCookie() == null? "":wechatMetaData.getCookie())
                .post(RequestBody.create(MediaType.parse("application/json;charset=utf-8"), body.toString()))
                .build();
        try(Response response = okHttpClient.newCall(request).execute()){
            String response_String = response.body().string();
            if(response_String == null && response_String.isEmpty()){
                throw new RuntimeException("Fail to enable status notify");
            }
            JSONObject jsonObject = JSONKit.parseObject(response_String);
            JSONObject baseResponse = jsonObject.get("BaseResponse").asJSONObject();
            if(null != baseResponse){
                int ret = baseResponse.getInt("Ret", -1);
                if(ret != 0){
                    throw new RuntimeException("Fail to enable status notify, ret code = " + ret);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public WechatContact getContact(WechatMetaData wechatMetaData) {
        String url = wechatMetaData.getBase_url() + "/webwxgetcontact?pass_ticket=" + wechatMetaData.getPass_ticket() + "&skey="
                + wechatMetaData.getSkey() + "&r=" + TimeUtils.getCurrentUnixTime();
        JSONObject body = new JSONObject();
        body.put("BaseRequest", wechatMetaData.getBaseRequest());

        Request request = new Request.Builder()
                              .url(url)
                              .header("Cookie", wechatMetaData.getCookie() == null?"":wechatMetaData.getCookie())
                              .post(RequestBody.create(MediaType.parse("application/json;charset=utf-8"),body.toString()))
                              .build();

        try(Response response = okHttpClient.newCall(request).execute()){
            String response_String = response.body().string();
            if(response_String == null && response_String.isEmpty()){
                throw new RuntimeException("Fail to receive contact list.");
            }
            WechatContact wechatContact = new WechatContact();
            JSONObject jsonObject = JSONKit.parseObject(response_String);
            JSONObject BaseResponse = jsonObject.get("BaseResponse").asJSONObject();
            if (null != BaseResponse) {
                int ret = BaseResponse.getInt("Ret", -1);
                if (ret == 0) {
                    JSONArray memberList = jsonObject.get("MemberList").asArray();
                    LOGGER.info(memberList);
                    JSONArray contactList = new JSONArray();

                    if (null != memberList) {
                        for (int i = 0, len = memberList.size(); i < len; i++) {
                            JSONObject contact = memberList.get(i).asJSONObject();
                            // pulibc account/service account
                            if (contact.getInt("VerifyFlag", 0) == 8) {
                                continue;
                            }
                            // special contact
                            if (Constants.FILTER_USERS.contains(contact.getString("UserName"))) {
                                continue;
                            }
                            // Group conversation
                            if (contact.getString("UserName").indexOf("@@") != -1) {
                                continue;
                            }
                            // Yourself
                            if (contact.getString("UserName").equals(wechatMetaData.getUser().getString("UserName"))) {
                                continue;
                            }
                            contactList.add(contact);
                        }

                        wechatContact.setContactList(contactList);
                        wechatContact.setMemberList(memberList);

                        this.getGroup(wechatMetaData, wechatContact);

                        return wechatContact;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    @Override
    public void login(WechatMetaData wechatMetaData) {
       String redirect_url = wechatMetaData.getRedirect_url();
       Request request = new Request.Builder()
                             .url(redirect_url)
                             .build();



       try(Response response = okHttpClient.newCall(request).execute()) {
           String response_string = response.body().string();
           wechatMetaData.setCookie(request.header("Cookie"));
           if (response_string == null && response_string.isEmpty()) {
               throw new RuntimeException("Fail to login Wechat");
           }
           wechatMetaData.setSkey(Matchers.match("<skey>(\\S+)</skey>", response_string));
           wechatMetaData.setWxsid(Matchers.match("<wxsid>(\\S+)</wxsid>", response_string));
           wechatMetaData.setWxuin(Matchers.match("<wxuin>(\\S+)</wxuin>", response_string));
           wechatMetaData.setPass_ticket(Matchers.match("<pass_ticket>(\\S+)</pass_ticket>", response_string));

           JSONObject baseRequest = new JSONObject();
           baseRequest.put("Uin", wechatMetaData.getWxuin());
           baseRequest.put("Sid", wechatMetaData.getWxsid());
           baseRequest.put("Skey", wechatMetaData.getSkey());
           baseRequest.put("DeviceID", wechatMetaData.getDeviceId());
           wechatMetaData.setBaseRequest(baseRequest);
       } catch (IOException e) {
           e.printStackTrace();
       }
    }

    @Override
    public void getGroup(WechatMetaData wechatMetaData, WechatContact wechatContact) {
        String url = wechatMetaData.getBase_url() + "/webwxbatchgetcontact?type=ex&pass_ticket=" + wechatMetaData.getPass_ticket()
                       + "&skey=" + wechatMetaData.getSkey() + "&r=" + TimeUtils.getCurrentUnixTime();
        JSONObject body = new JSONObject();
        body.put("BaseRequest", wechatMetaData.getBaseRequest());
        Request request = new Request.Builder()
                              .url(url)
                             .header("Cookie", wechatMetaData.getCookie() == null ? "" : wechatMetaData.getCookie())
                             .post(RequestBody.create(MediaType.parse("application/json;charset=utf-8"), body.toString()))
                             .build();
        LOGGER.info(request.toString());

        try(Response response = okHttpClient.newCall(request).execute()){
            String response_string = response.body().string();
            if(response_string == null && response_string.isEmpty()){
                throw new RuntimeException("Fail to receive Group");
            }

            JSONObject jsonObject = JSONKit.parseObject(response_string);
            JSONObject baseResponse = jsonObject.get("BaseResponse").asJSONObject();
            if(null != baseResponse){
                int ret  = baseResponse.getInt("Ret", -1);
                if(ret == 0){
                    JSONArray memberList = jsonObject.get("MemberList").asArray();
                    JSONArray contactList = new JSONArray();

                    if(null != memberList){
                        for(int i = 0; i < memberList.size(); i++){
                            JSONObject contact = memberList.get(i).asJSONObject();

                            //Public Account or Service Account
                            if(contact.getInt("VerifyFlag", 0) == 8){
                                continue;
                            }

                            //Group Conversation
                            if(Constants.FILTER_USERS.contains(contact.getString("UserName"))){
                                continue;
                            }

                            //Yourself
                            if(contact.getString("UserName").equals(wechatMetaData.getUser().getString("UserName"))){
                                continue;
                            }

                            contactList.add(contact);

                            wechatContact.setContactList(contactList);
                            wechatContact.setMemberList(memberList);
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void choiceSyncLine(WechatMetaData wechatMetaData) {
        boolean enabled = false;
        for(String syncUrl : Constants.SYNC_HOST){
            int[] res = this.syncCheck(syncUrl, wechatMetaData);
            if(res[0] == 0){
                String url = "https://" + syncUrl + "/cgi-bin/mmwebwx-bin";
                wechatMetaData.setWebpush_url(url);
                LOGGER.info("选择线路："+ syncUrl);
                enabled = true;
                break;
            }
        }
        if(!enabled){
            throw new RuntimeException("Fail to sync!");
        }
    }

    @Override
    public int[] syncCheck(WechatMetaData wechatMetadata) {
        return syncCheck(null, wechatMetadata);
    }

    @Override
    public JSONObject webwxsync(WechatMetaData metaData) {
        String url = metaData.getBase_url() + "/webwxsync?skey=" + metaData.getSkey() + "&sid=" + metaData.getWxsid();
        JSONObject body = new JSONObject();
        body.put("BaseRequest", metaData.getBaseRequest());
        body.put("SyncKey", metaData.getSyncKey());
        body.put("rr", DateKit.getCurrentUnixTime());

        Request request = new Request.Builder()
                                     .url(url)
                                     .header("Cookie", metaData.getCookie() == null ? "" : metaData.getCookie())
                                    .post(RequestBody.create(MediaType.parse("application/json;charset=utf-8"), body.toString()))
                                    .build();

        try(Response response = okHttpClient.newCall(request).execute()){
            String response_string = response.body().string();
            if (StringKit.isBlank(response_string)) {
                throw new RuntimeException("Fail to sync");
            }

            JSONObject jsonObject = JSONKit.parseObject(response_string);
            JSONObject BaseResponse = jsonObject.get("BaseResponse").asJSONObject();
            if (null != BaseResponse) {
                int ret = BaseResponse.getInt("Ret", -1);
                if (ret == 0) {
                    metaData.setSyncKey(jsonObject.get("SyncKey").asJSONObject());
                    StringBuffer synckey = new StringBuffer();
                    JSONArray list = metaData.getSyncKey().get("List").asArray();
                    for (int i = 0, len = list.size(); i < len; i++) {
                        JSONObject item = list.get(i).asJSONObject();
                        synckey.append("|" + item.getInt("Key", 0) + "_" + item.getInt("Val", 0));
                    }
                    metaData.setSyncKeyStr(synckey.substring(1));
                    return jsonObject;
                }
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void handleMsg(WechatMetaData wechatMetaData, JSONObject data) {
        if(data == null){
            return;
        }
        JSONArray addMsgList = data.get("AddMsgList").asArray();
        for(int i = 0, length = addMsgList.size(); i < length; i++){
            LOGGER.info("You have got a new message, please check it.");
            JSONObject msg = addMsgList.get(i).asJSONObject();
            int msgType = msg.getInt("MsgType", 0);
            String name = getUserRemarkName(msg.getString("FromUserName"));
            String content = msg.getString("Content");

            if(msgType == 51){
                LOGGER.info("Succeed to catch Wechat initialization message");
            } else if(msgType == 1){
                if (Constants.FILTER_USERS.contains(msg.getString("ToUserName"))) {
                    continue;
                /*} else if (msg.getString("FromUserName").equals(wechatMetaData.getUser().getString("UserName"))) {
                    continue;*/
                } else if (msg.getString("ToUserName").indexOf("@@") != -1) {
                    String[] peopleContent = content.split(":<br/>");
                    LOGGER.info("|" + name + "| " + peopleContent[0] + ":\n" + peopleContent[1].replace("<br/>", "\n"));
                } else {
                    LOGGER.info(name + ": " + content);
                   /* String ans = robot.talk(content);*/
                    webwxsendmsg(wechatMetaData, "Fuck you", msg.getString("FromUserName"));
                    LOGGER.info("自动回复 " + "Fuck you");
                }
            }else if (msgType == 3) {
                String imgDir = Constants.config.get("app.img_path");
                String msgId = msg.getString("MsgId");
                FileKit.createDir(imgDir, false);
                String imgUrl = wechatMetaData.getBase_url() + "/webwxgetmsgimg?MsgID=" + msgId + "&skey=" + wechatMetaData.getSkey() + "&type=slave";
                HttpRequest.get(imgUrl).header("Cookie", wechatMetaData.getCookie()).receive(new File(imgDir + "/" + msgId+".jpg"));
                webwxsendmsg(wechatMetaData, "二蛋还不支持图片呢", msg.getString("FromUserName"));
            } else if (msgType == 34) {
               /* webwxsendmsg(wechatMeta, "二蛋还不支持语音呢", msg.getString("FromUserName"));*/
            } else if (msgType == 42) {
                LOGGER.info(name + " 给你发送了一张名片:");
                LOGGER.info("=========================");
            }
        }

    }

    private void webwxsendmsg(WechatMetaData meta, String content, String to) {
        String url = meta.getBase_url() + "/webwxsendmsg?lang=zh_CN&pass_ticket=" + meta.getPass_ticket();
        JSONObject body = new JSONObject();

        String clientMsgId = DateKit.getCurrentUnixTime() + StringKit.getRandomNumber(5);
        JSONObject Msg = new JSONObject();
        Msg.put("Type", 1);
        Msg.put("Content", content);
        Msg.put("FromUserName", meta.getUser().getString("UserName"));
        Msg.put("ToUserName", to);
        Msg.put("LocalID", clientMsgId);
        Msg.put("ClientMsgId", clientMsgId);

        body.put("BaseRequest", meta.getBaseRequest());
        body.put("Msg", Msg);

        Request request = new Request.Builder()
                              .url(url)
                              .header("Cookie", meta.getCookie() == null?"":meta.getCookie())
                              .post(RequestBody.create(MediaType.parse("application/json;charset=utf-8"), body.toString()))
                              .build();

        try(Response response = okHttpClient.newCall(request).execute()){
            LOGGER.info("Response: " + response.body().string());
            LOGGER.info("Message sent.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getUserRemarkName(String username){
        String name = "Unknown User Name";
        for(int i = 0, length=Constants.CONTACT.getMemberList().size(); i < length; i++){
            JSONObject member = Constants.CONTACT.getMemberList().get(i).asJSONObject();
            if(member.getString("UserName").equals(username)){
                if(StringKit.isNotBlank(member.getString("RemarkName"))){
                    name = member.getString("RemarkName");
                }else{
                    name = member.getString("NickName");
                }
                break;
            }
        }
        return name;
    }

    public int[] syncCheck(String url, WechatMetaData wechatMetaData){
        if(null == url){
            url = wechatMetaData.getWebpush_url() + "/synccheck";
        } else {
            url = "https://" + url + "/cgi-bin/mmwebwx-bin/synccheck";
        }

        HttpUrl httpUrl = HttpUrl.parse(url)
                              .newBuilder()
                              .addQueryParameter("r", String.valueOf(TimeUtils.getCurrentUnixTime() + StringKit.getRandomNumber(5)))
                              .addQueryParameter("skey", wechatMetaData.getSkey())
                              .addQueryParameter("uin", wechatMetaData.getWxuin())
                              .addQueryParameter("sid", wechatMetaData.getWxsid())
                              .addQueryParameter("deviceid", wechatMetaData.getDeviceId())
                              .addEncodedQueryParameter("synckey", wechatMetaData.getSyncKeyStr().replace("|", "%7C"))
                              .addQueryParameter("_", String.valueOf(System.currentTimeMillis()))
                              .build();

        Request request = new Request.Builder()
                             .url(httpUrl)
                             .header("Cookie", wechatMetaData.getCookie() == null?"":wechatMetaData.getCookie())
                             .build();

        int[] arr = new int[]{-1, -1};
        try(Response response = okHttpClient.newCall(request).execute()){
            String response_string = response.body().string();

            if(StringKit.isBlank(response_string)){
                return arr;
            }

            String retCode = Matchers.match("retcode:\"(\\d+)\",",response_string);
            String selector = Matchers.match("selector:\"(\\d+)\"}", response_string);
            if(null != retCode && null != selector){
                arr[0] = Integer.parseInt(retCode);
                arr[1] = Integer.parseInt(selector);
                return arr;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return arr;
    }


}
