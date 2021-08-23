package ykk.xc.com.glswms.bean.k3Bean;

import java.io.Serializable;

/**
 *  Bom主表 ( ICBOM )
 */
public class Bom_App implements Serializable {
	private static final long serialVersionUID = 1L;

	private int finterId;			// BOM id
	private int fitemId;			// BOM 物料id
	private String fbomNumber;		// BOM 物料代码
	
	private ICItem_App icItem;
	
	public Bom_App() {
		super();
	}
	
	public int getFinterId() {
		return finterId;
	}

	public void setFinterId(int finterId) {
		this.finterId = finterId;
	}

	public int getFitemId() {
		return fitemId;
	}

	public void setFitemId(int fitemId) {
		this.fitemId = fitemId;
	}

	public String getFbomNumber() {
		return fbomNumber;
	}

	public void setFbomNumber(String fbomNumber) {
		this.fbomNumber = fbomNumber;
	}

	public ICItem_App getIcItem() {
		return icItem;
	}

	public void setIcItem(ICItem_App icItem) {
		this.icItem = icItem;
	}
	
}