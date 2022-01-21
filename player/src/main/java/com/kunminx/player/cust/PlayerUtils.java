package com.kunminx.player.cust;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/***
 * 播放器工具类
 */
public class PlayerUtils {

    public static final String TAG = PlayerUtils.class.getSimpleName();

    // currentTime要转换的long类型的时间
    public static String intToString(int currentTime, String formatType)
            throws ParseException {
        if(formatType==null){
            formatType= "mm:ss";
        }
        Date date = intToDate(currentTime, formatType); // int类型转成Date类型
        String strTime = dateToString(date, formatType); // date类型转成String
        return strTime;
    }

    // currentTime要转换的long类型的时间
    public static String floatToString(float currentTime, String formatType)
            throws ParseException {
        Log.i(TAG,"currentTime="+currentTime);
        if(formatType==null){
            formatType= "mm:ss";
        }
        Date date = intToDate((long)currentTime, formatType); // int类型转成Date类型
        String strTime = dateToString(date, formatType); // date类型转成String
        return strTime;
    }

    // strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
    // HH时mm分ss秒，
    // strTime的时间格式必须要与formatType的时间格式相同
    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType, Locale.CHINA);
        Date date = null;
        if(strTime==null){
            return null;
        }
        date = formatter.parse(strTime);
        return date;
    }

    // formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    // data Date类型的时间
    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType, Locale.CHINA).format(data);
    }

    // currentTime要转换的long类型的时间
    // formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    public static Date intToDate(long currentTime, String formatType)
            throws ParseException {
        Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
        Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
        return date;
    }

}
