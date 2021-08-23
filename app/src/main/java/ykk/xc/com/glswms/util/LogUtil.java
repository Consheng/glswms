package ykk.xc.com.glswms.util;

import android.util.Log;

/**
 * 日期：2018-11-27 15:21
 * 描述：打印的工具类（针对打印不全的问题）
 * 作者：ykk
 */
public class LogUtil {
    /**
     * 截断输出日志
     * @param msg
     */
    public static void e(String tag, String msg) {
        int segmentSize = 3 * 1024;
        long length = msg.length();
        if (length <= segmentSize ) {// 长度小于等于限制直接打印
            Log.e(tag, msg);
        }else {
            while (msg.length() > segmentSize ) {// 循环分段打印日志
                String logContent = msg.substring(0, segmentSize);
                msg = msg.replace(logContent, "");
                Log.e(tag, logContent);
            }
            Log.e(tag, msg);// 打印剩余日志
        }
    }
}
