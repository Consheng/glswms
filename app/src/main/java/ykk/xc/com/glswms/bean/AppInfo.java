package ykk.xc.com.glswms.bean;
/**
 * App版本和更新说明
 */
public class AppInfo {
	private int appVersion; // 更新版本
	private String appRemark; // 更新说明

	public int getAppVersion() {
		return appVersion;
	}
	public String getAppRemark() {
		return appRemark;
	}
	public void setAppVersion(int appVersion) {
		this.appVersion = appVersion;
	}
	public void setRppRemark(String appRemark) {
		this.appRemark = appRemark;
	}
}
