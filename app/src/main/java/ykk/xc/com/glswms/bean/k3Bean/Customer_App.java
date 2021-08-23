package ykk.xc.com.glswms.bean.k3Bean;

import java.io.Serializable;

/**
 * @Description:客户
 */
public class Customer_App implements Serializable{
	private static final long serialVersionUID = 1L;

	private int fitemId;		// 内码id
	private String fname;		// 客户名称
	private String fnumber;		// 客户代码
	private int fdeleted;		// 是否删除

	public Customer_App() {
		super();
	}

	public int getFitemId() {
		return fitemId;
	}

	public void setFitemId(int fitemId) {
		this.fitemId = fitemId;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public String getFnumber() {
		return fnumber;
	}

	public void setFnumber(String fnumber) {
		this.fnumber = fnumber;
	}

	public int getFdeleted() {
		return fdeleted;
	}

	public void setFdeleted(int fdeleted) {
		this.fdeleted = fdeleted;
	}

	
}
