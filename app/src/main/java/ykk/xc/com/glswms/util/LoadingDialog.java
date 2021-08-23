package ykk.xc.com.glswms.util;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.TextView;

import ykk.xc.com.glswms.R;

/**
 * 一个加载框的
 * ykk
 */
public class LoadingDialog {

	private Dialog dialog = null;

	public LoadingDialog(Activity context, String mMsg, boolean isCancel) {
		View view = context.getLayoutInflater().inflate(
				R.layout.loading_dialog, null);
		TextView message = (TextView) view
				.findViewById(R.id.tv_msg);
		message.setText(mMsg);
		dialog = new Dialog(context, R.style.loadDialog);
		dialog.setContentView(view);
	 	dialog.setCancelable(isCancel); // 点击返回键是否销毁
		dialog.setCanceledOnTouchOutside(false); // 默认点击屏幕外部不销毁
		dialog.show();
	}

	/**
	 * 设置对话框点击返回键是否销毁
	 *
	 * @param flag
	 */
	public void setCancelable(boolean flag) {
		dialog.setCancelable(flag);
	}

	/**
	 * 设置对话框点击屏幕是否销毁
	 *
	 * @param flag
	 */
	public void setCanceledOnTouchOutside(boolean flag) {
		dialog.setCanceledOnTouchOutside(flag);
	}
	/**
	 * 显示对话框
	 */
	public void show() {
		dialog.show();
	}
	/**
	 * 销毁对话框
	 */
	public void dismiss() {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
	}

	public boolean isShowing() {
		if (dialog != null) {
			return dialog.isShowing();
		}
		return false;
	}
}
