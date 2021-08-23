package ykk.xc.com.glswms.bean.k3Bean;

import java.io.Serializable;

/**
 * 部门信息	t_Department
 */
public class Department_App implements Serializable {
	private static final long serialVersionUID = 1L;

	private int fitemId;		// 内码id
	private String fnumber;		// 代码
	private String fname;		// 名称
	private int fdProperty;		// 部门属性 1070-车间,1071-非车间
	private int fdeleted;		// 是否删除
	
	public Department_App() {
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

	public int getFdProperty() {
		return fdProperty;
	}

	public void setFdProperty(int fdProperty) {
		this.fdProperty = fdProperty;
	}

	public int getFdeleted() {
		return fdeleted;
	}

	public void setFdeleted(int fdeleted) {
		this.fdeleted = fdeleted;
	}

	
	
}
