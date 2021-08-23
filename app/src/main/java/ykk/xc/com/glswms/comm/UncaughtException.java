package ykk.xc.com.glswms.comm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;

import ykk.xc.com.glswms.LoginKt;


/**
 * 捕获全局异常,因为有的异常我们捕获不到
 *
 * @author ykk
 *
 */
public class UncaughtException implements UncaughtExceptionHandler {

    private final static String TAG = "UncaughtException";
    private static UncaughtException mUncaughtException;
    private UncaughtExceptionHandler mDefaultHandler;
    private MyApplication mApplication;
    private Context context;

    // 用来存储设备信息和异常信息
    private HashMap<String, String> infos = new HashMap<String, String>();
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private UncaughtException() {

    }

    /**
     * 同步方法，以免单例多线程环境下出现异常
     *
     * @return
     */
    public synchronized static UncaughtException getInstance() {
        if (mUncaughtException == null) {
            mUncaughtException = new UncaughtException();
        }
        return mUncaughtException;
    }

    /**
     * 初始化，把当前对象设置成UncaughtExceptionHandler处理器
     */
    public void init(MyApplication application) {
        this.mApplication = application;
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e(TAG, "uncaughtException thread : " + thread + "||name=" + thread.getName() + "||id=" + thread.getId() + "||exception=" + ex);
        printError(ex);

        if(!handlerException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            AlarmManager mgr = (AlarmManager) mApplication.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(mApplication, LoginKt.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("crash", true);

            PendingIntent restartIntent = PendingIntent.getActivity(mApplication, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis()+1000, restartIntent);
            // 杀死进程
            android.os.Process.killProcess(android.os.Process.myPid());
            // 退出
            System.exit(0);
            // 释放资源
            System.gc();
        }

    }

    /**
     * 在LogCat打印错误信息
     */
    private void printError(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        Log.e("TAG", "异常信息："+sb.toString());
    }

    /**
     * 错误处理，收集错误信息，发送错误报告等操作均在此完成
     */
    private boolean handlerException(Throwable ex) {
        if(ex == null) {
            return false;
        }
        // 自定义处理错误信息
    	saveCrashInfo2File(ex);

        return true;
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return  返回文件名称,便于将文件传送到服务器
     */
    private String saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();

//        long timestamp = System.currentTimeMillis();
        String versionName = getAppVersionName(context);
        String time = Comm.getSysDate(1);
        sb.append("\r -- " + time + " -- 版本信息："+versionName+" -- \r\n ");
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();

        String result = writer.toString();
        sb.append(result);
        try {
            String fileName = "exception.txt";

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = Comm.publicPaths+"crash/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                // 清空log日志
                File logFile = new File(path + fileName);
                if(logFile.isFile()) {
                    FileWriter fw1 = new FileWriter(logFile);
                    @SuppressWarnings("resource")
                    BufferedWriter bw1 = new BufferedWriter(fw1);
                    bw1.write("");
                }

                // 写入新的内容，如果在之前没有清空日志，每次都会叠加内容
                FileOutputStream fos = new FileOutputStream(path + fileName,true);
                fos.write(sb.toString().getBytes());
                fos.close();
            }

            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }

        return null;
    }

    /**
     * 得到本机的版本信息
     */
    private String getAppVersionName(Context context) {
        PackageManager pack;
        PackageInfo info;
        String versionName = "";
        try {
            pack = context.getPackageManager();
            info = pack.getPackageInfo(context.getPackageName(), 0);
//			appVersionCode = info.versionCode;
            versionName = info.versionName;
        } catch (Exception e) {
            Log.d("getAppVersionName(Context context)：", e.toString());
        }
        return versionName;
    }
}
