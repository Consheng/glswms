package ykk.xc.com.glswms.util;

import android.view.View;
import android.view.View.OnClickListener;

import java.util.Calendar;

/**
 * 防止部分手机点击按钮不能快速响应的,导致响应多次方法
 * @author yu
 *
 */
public abstract class CheckDoubleClickListener implements OnClickListener {
	// 两次点击按钮之间间隔时间不能少于2000毫秒
	private static final int MIN_CLICK_TIME = 2000; // 间隔时间
	private static long lastClickTime = 0;  // 得到点击的时间
	private static int lastButtonId = -1; // 点击的view的Id

	public abstract void checkDoubleClick(View view);

	@Override
	public void onClick(View view) {
		long curClickTime = Calendar.getInstance().getTimeInMillis();
		int buttonId = view.getId();
		// 这里是防止点击A然后点击B的时候也要等2000毫秒后，如果不同id就应该默认跳过2000毫秒。
		if(lastButtonId != buttonId) {
			lastClickTime = 0;
		}

		if((curClickTime - lastClickTime) >= MIN_CLICK_TIME) {
			// 超过间隔时间，lastClickTime为当前点击时间
			lastClickTime = curClickTime;
			lastButtonId = buttonId;
			checkDoubleClick(view);

		}
	}
}
