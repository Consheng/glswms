package ykk.xc.com.glswms.util;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import ykk.xc.com.glswms.R;
import ykk.xc.com.glswms.comm.Comm;
import ykk.xc.com.glswms.comm.Consts;

/**
 * 更新管理
 */
public class UpdateManager {
    private Activity mContext;
    //提示语
//    private String updateMsg = "有最新的软件包哦，亲快下载吧~";
    //服务端包的地址url
    private String apkUrl; // 上个页面传来的下载路径
    private Dialog noticeDialog;
    private Dialog downloadDialog;
    /* 下载包安装路径 */
    private String savePath = Comm.publicPaths + "/updateFile/";
    private String saveFileName = savePath + "glswms.apk";
    /* 进度条与通知ui刷新的handler和msg常量 */
    private ProgressBar mProgress;
    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;
    private int progress;
    private Thread downLoadThread;
    private boolean interceptFlag = false;

    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);
                    break;
                case DOWN_OVER:
                    installApk();
                    break;
                default:
                    break;
            }
        };
    };
    public UpdateManager(Activity context, String url) {
        this.mContext = context;
        this.apkUrl = url;
    }
    //外部接口让主Activity调用
    public void checkUpdateInfo(){
//        showNoticeDialog();
        showDownloadDialog();
    }

    //这个方法暂时不用，放在提示更新的那个页面
//    private void showNoticeDialog(){
//        AlertDialog.Builder builder = new Builder(mContext);
//        builder.setTitle("软件更新");
//        builder.setMessage(Comm.tempUpRemark);
//        builder.setPositiveButton("下载", new OnClickListener() {
//            @Override
//            public void delClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                showDownloadDialog();
//            }
//        });
//        builder.setNegativeButton("取消", new OnClickListener() {
//            @Override
//            public void delClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        noticeDialog = builder.create();
//        noticeDialog.show();
//        noticeDialog.setCancelable(false);
//        noticeDialog.setCanceledOnTouchOutside(false);
//    }

    /**
     * 显示下载的进度
     */
    private void showDownloadDialog(){
        Builder builder = new Builder(mContext);

        builder.setTitle("软件更新");
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.progress, null);
        mProgress = (ProgressBar)v.findViewById(R.id.progress);
        builder.setView(v);
        // 开发员用的，长按进度条，就关闭下载框
//        mProgress.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                downloadDialog.dismiss();
//                interceptFlag = true;
//                return false;
//            }
//        });
        // 如果用户点击取消就销毁掉这个系统
        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                mContext.finish();
                dialog.dismiss();
                interceptFlag = true;
            }
        });
        downloadDialog = builder.create();
        downloadDialog.show();
        downloadDialog.setCancelable(false);
        downloadDialog.setCanceledOnTouchOutside(false);
        downloadApk();
    }

    /**
     * 线程下载apk文件
     */
    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(apkUrl);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();
                File file = new File(savePath);
                if(!file.exists()){
                    file.mkdirs();
                }
                String apkFile = saveFileName;
                File fApkFile = new File(apkFile);
                if(!fApkFile.exists()) {
                    fApkFile.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(fApkFile);
                int count = 0;
                byte buf[] = new byte[1024];
                do{
                    int numread = is.read(buf);
                    count += numread;
                    progress =(int)(((float)count / length) * 100);
                    //更新进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if(numread <= 0){
                        //下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf,0,numread);
                }while(!interceptFlag);//点击取消就停止下载.
                fos.close();
                is.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    };

    /**
     * 下载apk
     */
    private void downloadApk(){
        downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }
    /**
     * 安装apk
     */
    private void installApk(){
        File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }
}