package com.eric.wechat;

import com.eric.wechat.service.WeChatService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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
}
