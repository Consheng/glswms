package ykk.xc.com.glswms.comm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import ykk.xc.com.glswms.R;
import ykk.xc.com.glswms.basics.PublicInputDialog;
import ykk.xc.com.glswms.bean.User;
import ykk.xc.com.glswms.util.JsonUtil;
import ykk.xc.com.glswms.util.LoadingDialog;

/**
 * 父类Activity
 *
 * @author yu
 *
 */
public abstract class BaseDialogActivity extends AppCompatActivity {
	private BaseDialogActivity mContext = this;
	private long temptime;
	private LoadingDialog parentLoadDialog;
	protected Unbinder mBinder;

	public abstract int setLayoutResID();
	public void initView(){}
	public void initData(){}
	public void setListener(){}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(setLayoutResID());
		ActivityCollector.addActivity(this);
		UncaughtException.getInstance().setContext(mContext);

		mBinder = ButterKnife.bind(this);

		initView();
		initData();
		setListener();
	}



	/**
	 * 捕获返回按键的动作
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 使用系统时间来判读两次点击的间隔
        if((keyCode == KeyEvent.KEYCODE_BACK)&&(event.getAction() == KeyEvent.ACTION_DOWN)) {
				if(System.currentTimeMillis() - temptime > 2000) {// 2s内再次选择back键有效
					System.out.println(Toast.LENGTH_LONG);
					toasts("再按一次返回键退出系统");
					temptime = System.currentTimeMillis();
				} else {
					ActivityCollector.finishAll();
					System.exit(0); //凡是非零都表示异常退出!0表示正常退出!
				}
				return true;
        }
        return super.onKeyDown(keyCode, event);
    }

	@Override
	protected void onDestroy() {
		mBinder.unbind(); // 取消绑定
		ActivityCollector.removeActivity(this); // remove当前的Activity
		super.onDestroy();
	}

	/**
	 * 执行HOME键方法
	 */
	public void execHOME() {
		// 按返回键不退出程序，程序最小化
		Intent intent = new Intent();
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.HOME");
		startActivity(intent);
	}

	/**
	 * 按返回键不退出程序，程序最小化（好像不能用）
	 */
	// @Override
	// public boolean dispatchKeyEvent(KeyEvent event) {
	// if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
	// Intent intent = new Intent();
	// intent.setAction("android.intent.action.MAIN");
	// intent.addCategory("android.intent.category.HOME");
	// startActivity(intent);
	// }
	// return false;
	// };

	/**
	 * 退出的方法
	 */
	public void exit() {
//		if (!isExist) {
//			isExist = true;
//			Toast.makeText(getApplicationContext(), "再按一次返回键退出系统",
//					Toast.LENGTH_SHORT).show();
//			mHandle.sendEmptyMessageDelayed(0, 2000);
//		} else {
//			ExitApplication.getInstance().exit();
//			ActivityCollector.finishAll();
//			// 杀死进程
//        	android.os.Process.killProcess(android.os.Process.myPid());
//        	// 退出
//        	System.exit(0);
//        	// 释放资源
//        	System.gc();
//		}
	}

	/** ------------------自定义方法-------------------- */

	/**
	 * 说明:(得到TextView控件的值) 作者: y1
	 */
	public String getValues(TextView txt) {
		return txt.getText().toString();
	}

	/** 说明:(得到EditText控件的值) */
	public String getValues(EditText edit) {
		//		return edit.getText().toString();
		return edit.getText().toString().split(",")[0]; // 扫描特定的二维码（条码,字符串）用逗号隔开
	}

	/** 说明:(得到Button控件的值) */
	public String getValues(Button btn) {
		return btn.getText().toString();
	}

	/**
	 * 设置EditText 控件的值（把把光标显示到最前）
	 */
	public void setTexts(EditText edit, String value) {
		edit.setText(value);
		edit.setSelection(value.length());
	}

	/**
	 * 得到xml文件
	 */
	public SharedPreferences spf(String xmlName) {
		return getSharedPreferences(xmlName, Context.MODE_PRIVATE);
	}

	/**
	 * 删除（xml）指定键值
	 */
	public void spfRemove(String key, String xmlName) {
		SharedPreferences.Editor editor = spf(xmlName).edit();
		editor.remove(key);
		editor.commit();
	}

	/**
	 * 得到xml中的value
	 *
	 * @param key
	 * @return
	 */
	public String getXmlValues(SharedPreferences spf, String key) {
		return spf.getString(key, "");
	}

	/**
	 * 得到session
	 */
	public String getSession() {
		SharedPreferences spfOther = spf(getResStr(R.string.saveOther));
		return spfOther.getString("session", "");
	}

	/**
	 * 得到url
	 * @param param
	 * @return
	 */
	public String getURL(String param) {
		SharedPreferences spfConfig = spf(getResStr(R.string.saveConfig));
		String ip = spfConfig.getString("ip", "192.168.3.198");
		String port = spfConfig.getString("port", "8080");
		return "http://"+ip+":"+port+"/gls/"+param;
	}

	public boolean getXmlValues2(SharedPreferences spf, String key) {
		return spf.getBoolean(key, false);
	}

	/**
	 * 设置xml中的值
	 *
	 * @param spf
	 * @param putKey
	 * @param putValue
	 */
	public void setXmlValues(SharedPreferences spf, String putKey, String putValue) {
		spf.edit().putString(putKey, putValue).commit();
	}

    /**
     * 得到string.xml中的值
     */
    public String getResStr(int idName) {
        return getResources().getString(idName);
    }

	/**
	 * 把User对象存到本地xml文件中
	 * @param user
	 */
	public void saveUserToXml(User user) {
		SharedPreferences sp = mContext.getSharedPreferences(getResStr(R.string.saveUser), MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		String json = JsonUtil.objectToString(user);
		editor.putString("strUser", json);
		editor.commit();
	}

	/**
	 * 显示xml中User对象
	 */
	public User showUserByXml() {
		SharedPreferences sp = mContext.getSharedPreferences(getResStr(R.string.saveUser), MODE_PRIVATE);
		String json = sp.getString("strUser", "");
		if(json.length() == 0) {
			return null;
		}
		return JsonUtil.stringToObject(json, User.class);
	}
	/**
	 * 把对象存到本地xml文件中
	 * @param object
	 * @param key
	 * @param xmlName
	 */
	public void saveObjectToXml(Object object, String key, String xmlName) {
		SharedPreferences sp = mContext.getSharedPreferences(xmlName, MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		String json = JsonUtil.objectToString(object);
		editor.putString(key, json);
		editor.commit();
	}

	/**
	 * 显示xml中对象
	 * @param cls
	 * @param key
	 * @param xmlName
	 * @param <T>
	 * @return
	 */
	public <T> T showObjectByXml(Class<T> cls, String key, String xmlName) {
		SharedPreferences sp = mContext.getSharedPreferences(xmlName, MODE_PRIVATE);
		String json = sp.getString(key, "");
		if(json.length() == 0) {
			return null;
		}
		return JsonUtil.stringToObject(json, cls);
	}

	/**
	 * Toast打印
	 */
	public void toasts(String str) {
		Toast.makeText(mContext, str, Toast.LENGTH_LONG).show();
	}

	/**
	 * 字符串不能为空的终极判断 父方法
	 *
	 * @param obj
	 * @return
	 */
	public String isNULLS(String obj) {
		return (obj != null && !obj.equals("null")) ? obj.toString() : "";
	}
	public String isNULLS(JSONObject json, String key) throws JSONException {
		return json.has(key) ? isNULLS(json.getString(key)) : "";
	}

	/**
	 * 说明:(字符串不能为空的终极判断 ) 作者: y1
	 */
	public String isNULL2(String obj, String defaVal) {
		String result = isNULLS(obj);
		return result.length() > 0 ? result : defaVal;
	}

	/**
	 * 数字字符串转成Double 如果Double.parseDouble(null);这个是报空指针异常
	 */
	public double parseDouble(String obj) {
		try {
			String result = isNULLS(obj);
			return result.length() > 0 ? Double.parseDouble(result) : 0;
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	public double parseDouble(JSONObject json, String key) {
		try {
			String result = isNULLS(json, key);
			return result.length() > 0 ? Double.parseDouble(result) : 0;
		} catch (Exception e) {
			return 0;
		}
	}
	public double parseDouble(String obj, double defaVal) {
		try {
			String result = isNULLS(obj);
			return result.length() > 0 ? Double.parseDouble(result) : defaVal;
		} catch (NumberFormatException e) {
			return defaVal;
		}
	}

	/**
	 * 数字字符串转成Int 如果Integer.parseInt(null);这个是报空指针异常
	 */
	public int parseInt(String obj) {
		try {
			String result = isNULLS(obj);
			return result.length() > 0 ? Integer.parseInt(result) : 0;
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	public int parseInt(JSONObject json, String key) {
		try {
			String result = isNULLS(json, key);
			return result.length() > 0 ? Integer.parseInt(result) : 0;
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * 设置view的间距
	 */
	public void setMargins(View view, int left, int top, int right, int bottom) {
		if(view.getLayoutParams() instanceof MarginLayoutParams) {
			MarginLayoutParams p = (MarginLayoutParams) view.getLayoutParams();
			p.setMargins(left, top, right, bottom);
			view.requestLayout();
		}
	}

	/**
	 * 比UUID还牛的随机生成数
	 * 10位随机数，当前时间	组成
	 */
	public String randomUUID(Context context) {
		// 生产8位随机验证码
		StringBuffer code = new StringBuffer();
		Random rand = new Random();// 随机生成类
		for (int i = 0; i < 10; i++) {
			code.append(rand.nextInt(10));
		}
		rand = null;
		return code.toString()+Comm.getSysDate(8);
	}

	/**
	 * 打开页面的方法
	 * @param aClass    打开的页面
	 * @param bundle  传值
	 */
	public void show(Class<?> aClass, Bundle bundle){
		Intent intent = new Intent();
		intent.setClass(mContext, aClass);
		if(bundle!=null){
			intent.putExtras(bundle);
		}
		mContext.startActivity(intent);
	}

	/**
	 * 打开页面然后可以得到回调的值
	 * @param aClass    打开的页面
	 * @param code 返回值的状态码
	 * @param bundle  传值
	 */
	public void showForResult(Class<?> aClass , int code , Bundle bundle){
		Intent intent = new Intent();
		intent.setClass(mContext, aClass);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if(bundle!=null){
			intent.putExtras(bundle);
		}
		mContext.startActivityForResult(intent, code);
	}

	/**
	 * 刷新	发起跳转的onActivity
	 */
	public void setResults(Activity context) {
		Intent intent = new Intent();
		intent.putExtra("isRefresh", true);
		context.setResult(RESULT_OK, intent);
	}
	public void setResults(Activity context, String str) {
		Intent intent = new Intent();
		intent.putExtra("resultValue", str);
		context.setResult(RESULT_OK, intent);
	}
	public void setResults(Activity context, Class<?> t) {
		Intent intent = new Intent();
		intent.putExtra("obj", t);
		context.setResult(RESULT_OK, intent);
	}
	public void setResults(Activity context, Bundle bundle) {
		Intent intent = new Intent();
		intent.putExtras(bundle);
		context.setResult(RESULT_OK, intent);
	}

	/**
	 * 显示输入法
	 */
	public void showKeyboard(View v) {
		InputMethodManager imm = (InputMethodManager) v.getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
	}

	/**
	 * 隐藏输入法
	 */
	public void hideKeyboard(View v) {
		InputMethodManager imm = (InputMethodManager) v.getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
	}

	/**
	 * 得到焦点后不谈出软键盘 只显示光标不显示软键盘
	 */
	public void hideSoftInputMode(EditText edit) {
		if (android.os.Build.VERSION.SDK_INT <= 10) {// 3.0以下使用
			edit.setInputType(InputType.TYPE_NULL);
		} else {// 3.0以上使用
			mContext.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			try {
				Class<EditText> cls = EditText.class;
				Method setFocus;
				setFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
				setFocus.setAccessible(true);
				setFocus.invoke(edit, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 关闭Handler
	 */
	public void closeHandler(Handler mHandle) {
		if(mHandle != null) {
			mHandle.removeCallbacksAndMessages(null);
		}
	}

	/**
	 * 显示加载框
	 * @param loadText
	 */
	public void showLoadDialog(String loadText) {
		parentLoadDialog = new LoadingDialog(mContext, loadText, true);
	}
	public void showLoadDialog(String loadText, boolean isCancel) {
		parentLoadDialog = new LoadingDialog(mContext, loadText, isCancel);
	}

	public void hideLoadDialog() {
		if (parentLoadDialog != null) {
			parentLoadDialog.dismiss();
			parentLoadDialog = null;
		}
	}

	/**
	 * 设置View是否可用和改变背景色
	 */
	public void setEnables(View vw, int drawableId, boolean isEnable) {
		vw.setEnabled(isEnable);
		vw.setBackgroundResource(drawableId);
	}

	/**
	 * 打开通用的输入dialog
	 * @param hintName
	 * @param val
	 * @param inputType 0:表示数字, 0.0表示有小数点， none:调用系统输入键盘
	 * @param codes
	 */
	public void showInputDialog(String hintName, String val, String inputType, int codes) {
		Bundle bundle = new Bundle();
		bundle.putString("hintName", hintName);
		bundle.putString("value", val);
		// 0:表示数字, 0.0表示有小数点， none:调用系统输入键盘
		bundle.putString("inputType", inputType);
		showForResult(PublicInputDialog.class, codes, bundle);
	}

	/**
	 * 强行获取焦点
	 * @param view
	 */
	public void setFocusable(View view) {
		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		view.requestFocus();
	}

	/**
	 * 提示框
	 * @param message
	 * @return
	 */
	public void showWarnDialog(String message) {
		AlertDialog.Builder build = new AlertDialog.Builder(mContext);
		build.setIcon(R.drawable.caution);
		build.setTitle("系统提示");
		build.setMessage(message);
		build.setNegativeButton("知道了", null);
		build.setCancelable(false);
		build.show();
	}

}
