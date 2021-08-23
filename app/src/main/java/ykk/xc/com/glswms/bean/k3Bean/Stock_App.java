package ykk.xc.com.glswms.bean.k3Bean;

import java.io.Serializable;

/**
 * 仓库	( 对应表：t_Stock )
 * @author Administrator
 *
 */
public class Stock_App implements Serializable {
	private static final long serialVersionUID = 1L;

	private int fitemId;			// 仓库id
	private String fnumber;			// 仓库代码
	private String fname;			// 仓库名称
	private int fisStockMgr;		// 启用仓位组管理 0：不启用，1：启用
	private int fspGroupId;			// 仓位组id
	private int fdeleted;			// 是否禁用

	// 临时字段，不加表
	private boolean check; // 是否选中
	
	public Stock_App() {
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

	public int getFisStockMgr() {
		return fisStockMgr;
	}

	public void setFisStockMgr(int fisStockMgr) {
		this.fisStockMgr = fisStockMgr;
	}
	
	public int getFspGroupId() {
		return fspGroupId;
	}

	public void setFspGroupId(int fspGroupId) {
		this.fspGroupId = fspGroupId;
	}

	public int getFdeleted() {
		return fdeleted;
	}

	public void setFdeleted(int fdeleted) {
		this.fdeleted = fdeleted;
	}

	public boolean isCheck() {
		return check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}


}
