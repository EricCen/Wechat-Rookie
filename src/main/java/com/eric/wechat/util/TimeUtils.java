package com.eric.wechat.util;

import java.util.Date;

/**
 * Created by Eric Cen on 2016/11/10.
 */
public class TimeUtils {
    public static int getCurrentUnixTime(){
        return (int)(new Date().getTime() / 1000);
    }
}
