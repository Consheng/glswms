package ykk.xc.com.glswms.bean.k3Bean;

import java.io.Serializable;

/**
 * 仓位	( 对应表：t_StockPlace )
 * @author Administrator
 *
 */
public class StockPlace_App implements Serializable {
	private static final long serialVersionUID = 1L;

	private int fspId;				// 仓位id
	private String fnumber;			// 仓位代码
	private String fname;			// 仓位名称
	private int fspGroupId;			// 仓位组id
	private int fdeleted;			// 是否禁用

	// 临时字段，不加表
	private boolean check; // 是否选中
	private Stock_App stock;
	
	public StockPlace_App() {
		super();
	}

	public int getFspId() {
		return fspId;
	}

	public void setFspId(int fspId) {
		this.fspId = fspId;
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

	public Stock_App getStock() {
		return stock;
	}

	public void setStock(Stock_App stock) {
		this.stock = stock;
	}
	
}
