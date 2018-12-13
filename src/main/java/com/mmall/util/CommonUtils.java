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

    /**String转int，如果为Null就返回默认值
     *
     * @param str
     * @param def
     * @return
     */
    public static int strToInteger(String str, int def){
        String strAfterTrim = trim(str);
        if(!isEmpty(str)){
            return Integer.parseInt(strAfterTrim);
        }
        return def;
    }

    public static String intToString(Integer value){
        if(value==null){
            return "";
        }
        return value.toString();
    }
}
