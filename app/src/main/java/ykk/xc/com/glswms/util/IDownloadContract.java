package ykk.xc.com.glswms.util;

import android.content.Context;

import java.io.File;

/**
 * 定义回调接口
 */

public interface IDownloadContract {

    interface View {
        void showUpdate(String version);
        void showProgress(int progress);
        void showFail(String msg);
        void showComplete(File file);
    }

    interface Presenter{
        void checkUpdate(String local);
//        void downApk(Context context);
        void downApk(Context context, String url);
        void unbind(Context context);
    }
}
