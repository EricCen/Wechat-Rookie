package com.eric.wechat;

import com.eric.wechat.service.WeChatService;
import com.eric.wechat.service.WeChatServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

import static org.mockito.Mockito.when;

/**
 * Created by Eric Cen on 2016/11/9.
 */
public class WeChatServiceTest {

    private WeChatService weChatService;

    @Before
    public void setUp(){
        System.setProperty("https.protocols","TLSv1");
        System.setProperty("jsse.enableSNIExtension", "false");
        this.weChatService = Mockito.mock(WeChatService.class);
    }

    @Test
    public void testGetUuid(){
        when(weChatService.getUUID()).thenReturn("test");
        String uuid = weChatService.getUUID();
        Assert.assertEquals(uuid, "test");
    }

    @Test
    public void testGetQrCodeImage(){
        WeChatService realWeChatService = new WeChatServiceImpl();
        String uuid = realWeChatService.getUUID();
        File qrCodeImage = new File("testQrCode.jpg");
        if(qrCodeImage.exists()){
            qrCodeImage.delete();
        }
        realWeChatService.getQrCodeImage(qrCodeImage,uuid);

    }

}
