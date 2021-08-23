package ykk.xc.com.glswms.comm;

import okhttp3.MediaType;


/**
 * 网络访问地址类
 * @author ykk
 *
 */
public class Consts {
	public static String mIp;// 服务器ip地址
	public static String mPort;// 服务器端口

	/**
	 * 服务器的地址
	 */
//	public static final String getURL(String param) {
//		return "http://"+mIp+":"+mPort+"/bswms/"+param;
//	}
//	public static final String getApkURL() {	return "http://"+mIp+":"+mPort+"/apks/bswms.apk"; }

	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");


	/**
	 * 构造(get,set)
	 * ip：192.168.3.214
	 * 端口：8080
	 */
	public static void setIp(String ip) {
		mIp = ip;
	}

	public static String getIp() {
		return mIp;
	}

	public static void setPort(String port) {
		mPort = port;
	}

	public static String getPort() {
		return mPort;
	}

}