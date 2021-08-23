package ykk.xc.com.glswms.bean.k3Bean;

import java.io.Serializable;

public class Emp implements Serializable {

	private int fitemId;

	private String fname;

	private String fnumber;

	public Emp() {
		super();
	}

	public int getFitemId() {
		return fitemId;
	}

	public String getFname() {
		return fname;
	}

	public String getFnumber() {
		return fnumber;
	}

	public void setFitemId(int fitemId) {
		this.fitemId = fitemId;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public void setFnumber(String fnumber) {
		this.fnumber = fnumber;
	}

	@Override
	public String toString() {
		return "Emp [fitemId=" + fitemId + ", fname=" + fname + ", fnumber=" + fnumber + "]";
	}


}
