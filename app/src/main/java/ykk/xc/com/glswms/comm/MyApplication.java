package ykk.xc.com.glswms.comm;

import android.app.Application;
import android.content.Context;

/**
 * 主Application
 * @author ykk
 */
public class MyApplication extends Application {
	public static Context context;
//	private static MyApplication application;

	@Override
	public void onCreate() {
		super.onCreate();
		// 全局捕获异常
		UncaughtException mUncaughtException = UncaughtException.getInstance();
		mUncaughtException.init(this);
		context = this;
//		application = this;
	}

	public static Context getContext() {
		return context;
	}


}
