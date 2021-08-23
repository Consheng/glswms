package ykk.xc.com.glswms.bean.k3Bean;

import java.io.Serializable;

/**
 * 供应商
 * @author Administrator
 *
 */
public class Supplier_App implements Serializable {
	private static final long serialVersionUID = 1L;

	private int fitemId;			// id
	private String fnumber;			// 供应商代码
	private String fname;			// 供应商名称
	private int fdeleted;			// 是否禁用

	public Supplier_App() {
		super();
	}

	public int getFitemId() {
		return fitemId;
	}

	public void setFitemId(int fitemId) {
		this.fitemId = fitemId;
	}

	public String getFnumber() {
		return fnumber;
	}

	public void setFnumber(String fnumber) {
		this.fnumber = fnumber;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public int getFdeleted() {
		return fdeleted;
	}

	public void setFdeleted(int fdeleted) {
		this.fdeleted = fdeleted;
	}

	
	
}
