package com.mmall.util;

public class CommonUtils {

    public static boolean isEmpty(String str)
    {
        return ((str == null) || (str.trim().length() == 0));
    }

    public static String trim(String parameter)
    {
        return null == parameter ? null : parameter.trim();
    }

    public static Integer strToInteger(String str){
        String strAfterTrim = trim(str);
        if(str!=null){
            return new Integer(strAfterTrim);
        }
        return null;
    }
}
