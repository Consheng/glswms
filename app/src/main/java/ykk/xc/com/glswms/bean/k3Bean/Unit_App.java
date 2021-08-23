package ykk.xc.com.glswms.bean.k3Bean;

import java.io.Serializable;

/**
 * 单位表	t_MeasureUnit
 * @author Administrator
 *
 */
public class Unit_App implements Serializable {
	private static final long serialVersionUID = 1L;

	private int fitemId;
	private String fnumber;
	private String fname;
	private String fdeleted;
	
	public Unit_App() {
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

	public String getFdeleted() {
		return fdeleted;
	}

	public void setFdeleted(String fdeleted) {
		this.fdeleted = fdeleted;
	}

	
}
