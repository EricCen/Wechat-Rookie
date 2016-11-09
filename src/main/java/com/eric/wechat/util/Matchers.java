package com.eric.wechat.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Eric Cen on 2016/11/10.
 */
public class Matchers {
    public static String match(String regex, String str){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        if(matcher.find()){
            return matcher.group(1);
        }
        return null;
    }
}
