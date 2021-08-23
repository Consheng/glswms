package ykk.xc.com.glswms.comm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import ykk.xc.com.glswms.R;
import ykk.xc.com.glswms.basics.PublicInputDialog;
import ykk.xc.com.glswms.basics.PublicInputDialog2;
import ykk.xc.com.glswms.bean.User;
import ykk.xc.com.glswms.util.JsonUtil;
import ykk.xc.com.glswms.util.LoadingDialog;

/**
 * 父类Fragment
 */
public abstract class BaseFragment extends Fragment {
	public static final String DECODED_CONTENT_KEY = "codedContent"; // 扫一扫：内容（键）
	public static final String DECODED_BITMAP_KEY = "codedBitmap"; // 扫一扫：图片（键）
	public static final int CAMERA_SCAN = 0x0000; // 扫一扫：请求值
	public Activity parentActivity;
	private LoadingDialog parentLoadDialog;
	protected Unbinder mBinder;

	public abstract View setLayoutResID(LayoutInflater inflater, ViewGroup container);
	public void initView(){}
	public void initData(){}
	public void setListener(){}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		parentActivity = getActivity();

		View view = setLayoutResID(inflater, container);
		UncaughtException.getInstance().setContext(parentActivity);
		mBinder = ButterKnife.bind(this, view);

//		initView();
//		initData();
//		setListener();
		return view;
	}

	// 因为使用了Kotlin的原因，ButterKnife注解不用写了，需要以下这种写法，他们才能得到值
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		initView();
		initData();
		setListener();
	}

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
		return parentActivity.getSharedPreferences(xmlName, Context.MODE_PRIVATE);
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
	 * @param putKey
	 * @param putValue
	 */
	public void setXmlValues(SharedPreferences spf, String putKey,
							 String putValue) {
		spf.edit().putString(putKey, putValue).commit();
	}

	/**
	 * 把User对象存到本地xml文件中
	 * @param user
	 */
	public void saveUserToXml(User user) {
		SharedPreferences sp = parentActivity.getSharedPreferences(getResStr(R.string.saveUser), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		String json = JsonUtil.objectToString(user);
		editor.putString("strUser", json);
		editor.commit();
	}
	/**
	 * 显示xml中User对象
	 */
	public User showUserByXml() {
		SharedPreferences sp = parentActivity.getSharedPreferences(getResStr(R.string.saveUser), Context.MODE_PRIVATE);
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
		SharedPreferences sp = parentActivity.getSharedPreferences(xmlName, Context.MODE_PRIVATE);
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
		SharedPreferences sp = parentActivity.getSharedPreferences(xmlName, Context.MODE_PRIVATE);
		String json = sp.getString(key, "");
		if(json.length() == 0) {
			return null;
		}
		return JsonUtil.stringToObject(json, cls);
	}

	/**
	 * 得到string.xml中的值
	 */
	public String getResStr(int idName) {
		return parentActivity.getResources().getString(idName);
	}

	/**
	 * 将ImageView图片转换为圆角图片
	 *
	 * @param bitmap
	 *            原图片
	 * @param pix
	 *            截取比例，如果是8,则圆角半径是宽高的1/8,如果是2,则是圆形图片
	 * @return 截取后的bitmap
	 */
	public Bitmap toRoundComer(Bitmap bitmap, float pix) {
		Bitmap retBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(retBitmap);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawRoundRect(rectF, bitmap.getWidth() / pix, bitmap.getHeight()
				/ pix, paint);

		paint.setXfermode(new PorterDuffXfermode(
				android.graphics.PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return retBitmap;
	}

	/**
	 * Toast打印
	 */
	public void toasts(String str) {
		Toast.makeText(parentActivity, str, Toast.LENGTH_LONG).show();
	}

	/**
	 * 字符串不能为空的终极判断 父方法
	 *
	 * @param obj
	 * @return
	 */
	public String isNULLS(Object obj) {
		return (obj != null && !obj.equals("null")) ? obj.toString() : "";
	}
	public String isNULLS(JSONObject json, String key) throws JSONException {
		return json.has(key) ? json.getString(key) : "";
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
	public double parseDouble(Object obj) {
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
	public double parseDouble(Object obj, double defaVal) {
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
	public int parseInt(Object obj) {
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
	 * 打开页面然后传值
	 * @param context2    打开的页面
	 * @param bundle  传值
	 */
	public void show(Class<?> context2,Bundle bundle){
		Intent intent = new Intent();
		intent.setClass(getActivity(), context2);
		if(bundle!=null){
			intent.putExtras(bundle);
		}
		startActivity(intent);
	}

	/**
	 * 打开页面然后可以得到回调的值
	 * @param context2    打开的页面
	 * @param code 返回值的状态码
	 * @param bundle  传值
	 */
	public void showForResult(Fragment fragment, Class<?> context2 , int code , Bundle bundle){
		Intent intent = new Intent();
		intent.setClass(getActivity(), context2);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if(bundle!=null){
			intent.putExtras(bundle);
		}
		if(fragment != null) {
			fragment.startActivityForResult(intent, code);
		} else {
			startActivityForResult(intent, code);
		}
	}

	/**
	 * 打开页面然后可以得到回调的值
	 * @param context2    打开的页面
	 * @param code 返回值的状态码
	 * @param bundle  传值
	 */
	public void showForResult(Class<?> context2 , int code , Bundle bundle){
		Intent intent = new Intent();
		intent.setClass(getActivity(), context2);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if(bundle!=null){
			intent.putExtras(bundle);
		}
		startActivityForResult(intent, code);
	}

	/**
	 * 刷新	发起跳转的onActivity
	 */
	public void setResults() {
		Intent intent = new Intent();
		intent.putExtra("isRefresh", true);
		parentActivity.setResult(Activity.RESULT_OK, intent);
	}
	public void setResults(String str) {
		Intent intent = new Intent();
		intent.putExtra("resultValue", str);
		parentActivity.setResult(Activity.RESULT_OK, intent);
	}
	public void setResults(Bundle bundle) {
		Intent intent = new Intent();
		intent.putExtras(bundle);
		parentActivity.setResult(Activity.RESULT_OK, intent);
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
	public void hideSoftInputMode(Activity context, EditText edit) {
		if (android.os.Build.VERSION.SDK_INT <= 10) {// 3.0以下使用
			edit.setInputType(InputType.TYPE_NULL);
		} else {// 3.0以上使用
			context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			try {
				Class<EditText> cls = EditText.class;
				Method setFocus;
				setFocus = cls.getMethod("setShowSoftInputOnFocus",
						boolean.class);
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
		parentLoadDialog = new LoadingDialog(parentActivity, loadText, true);
	}
	public void showLoadDialog(String loadText, boolean isCancel) {
		parentLoadDialog = new LoadingDialog(parentActivity, loadText, isCancel);
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
	public void showInputDialog(String hintName, String showInfo, String val, String inputType, int codes) {
		Bundle bundle = new Bundle();
		bundle.putString("hintName", hintName);
		bundle.putString("showInfo", showInfo);
		bundle.putString("value", val);
		// 0:表示数字，0.0：表示有小数点，+0：表示全部为数字都是正整数，none:调用系统输入键盘
		bundle.putString("inputType", inputType);
		showForResult(PublicInputDialog.class, codes, bundle);
	}

	/**
	 *
	 * @param hintName
	 * @param val
	 * @param val2
	 * @param codes
	 */
	public void showInputDialog2(String hintName, String itemName, String itemName2, String val, String val2, int codes) {
		Bundle bundle = new Bundle();
		bundle.putString("hintName", hintName);
		bundle.putString("itemName", itemName);
		bundle.putString("itemName2", itemName2);
		bundle.putString("value", val);
		bundle.putString("value2", val2);
		showForResult(PublicInputDialog2.class, codes, bundle);
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
		AlertDialog.Builder build = new AlertDialog.Builder(parentActivity);
		build.setIcon(R.drawable.caution);
		build.setTitle("系统提示");
		build.setMessage(message);
		build.setNegativeButton("知道了", null);
		build.setCancelable(false);
		build.show();
	}

}
