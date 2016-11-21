package com.eric.wechat.listener;


import com.blade.kit.json.JSONObject;
import com.eric.wechat.model.WechatMetaData;
import com.eric.wechat.service.WeChatService;
import org.apache.log4j.Logger;

/**
 * Created by Eric Cen on 2016/11/15.
 */
public class WechatListener {
    private static Logger LOGGER = Logger.getLogger(WechatListener.class);

    int playWeChat = 0;
    public void start(final WeChatService weChatService, final WechatMetaData wechatMetaData){
        new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("Launch Message Listener Mode....");
                weChatService.choiceSyncLine(wechatMetaData);
                while(true){
                    int[] arr = weChatService.syncCheck(wechatMetaData);
                    LOGGER.info("retcode=" + arr[0] + "selector=" + arr[1]);
                    if(arr[0] == 1100){
                        LOGGER.info("You have logout in your phone, goodbye!");
                        break;
                    }
                    if(arr[0] == 0){
                        if(arr[1] == 2){
                            JSONObject data = weChatService.webwxsync(wechatMetaData);
                            weChatService.handleMsg(wechatMetaData,data);
                        }
                    }
                }
            }
        }).start();
    }
}
