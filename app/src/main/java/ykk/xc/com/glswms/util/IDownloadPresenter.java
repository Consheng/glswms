package ykk.xc.com.glswms.util;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.io.File;

import ykk.xc.com.glswms.comm.Consts;


public class IDownloadPresenter implements IDownloadContract.Presenter {

    private IDownloadContract.View view;
    private ServiceConnection conn;

    public IDownloadPresenter(IDownloadContract.View view) {
        this.view = view;
    }

    /**
     * 请求网络
     * 获取网络版本号
     * 获取成功后与本地版本号比对
     * 符合更新条件就控制view弹窗
     */
    @Override
    public void checkUpdate(String local) {
        //假设获取得到最新版本
        //一般还要和忽略的版本做比对。。这里就不累赘了
        String version = "2.0";
//        if (!ignore.equals(version) && !ignore.equals(local)) {
//            view.showUpdate(version);
////        }
    }

    /**
     * 模拟网络下载
     */
    @Override
    public void downApk(Context context, final String url) {
//        final String url = Consts.getApkURL();
        if (conn == null)
            conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    DownloadService.DownloadBinder binder = (DownloadService.DownloadBinder) service;
                    DownloadService myService = binder.getService();
                    myService.downApk(url, new DownloadService.DownloadCallback() {
                        @Override
                        public void onPrepare() {

                        }

                        @Override
                        public void onProgress(int progress) {
                            view.showProgress(progress);
                        }

                        @Override
                        public void onComplete(File file) {
                            view.showComplete(file);
                        }

                        @Override
                        public void onFail(String msg) {
                            view.showFail(msg);
                        }
                    });
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    //意味中断，较小发生，酌情处理
                }
            };
        Intent intent = new Intent(context,DownloadService.class);
        context.bindService(intent, conn, Service.BIND_AUTO_CREATE);
    }

    @Override
    public void unbind(Context context) {
        context.unbindService(conn);
    }
}
