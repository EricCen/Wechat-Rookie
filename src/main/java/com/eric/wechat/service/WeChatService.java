package com.eric.wechat.service;

import com.eric.wechat.model.WechatContact;
import com.eric.wechat.model.WechatMetaData;

import java.io.File;

/**
 * Created by Eric Cen on 2016/11/9.
 */
public interface WeChatService {
    String getUUID();
    void getQrCodeImage(File file, String uuid);
    String loginWechat(int tip, String uuid, WechatMetaData wechatMetaData);
    void wxInit(WechatMetaData wechatMetaData);
    void openStatusNotify(WechatMetaData wechatMetaData);
    WechatContact getContact(WechatMetaData wechatMetaData);
    void login(WechatMetaData wechatMetaData);
    void getGroup(WechatMetaData wechatMetaData, WechatContact wechatContact);
}
