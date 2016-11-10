package com.eric.wechat.service;

import java.io.File;

/**
 * Created by Eric Cen on 2016/11/9.
 */
public interface WeChatService {
    public String getUUID();
    public void getQrCodeImage(File file, String uuid);
}
