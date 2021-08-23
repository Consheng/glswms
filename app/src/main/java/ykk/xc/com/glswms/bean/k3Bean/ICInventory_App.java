package ykk.xc.com.glswms.bean.k3Bean;

import java.io.Serializable;

/**
 * 即时库存表 ( ICInventory )
 */
public class ICInventory_App implements Serializable {
	private static final long serialVersionUID = 1L;

	private int fitemId;			// 物料id
	private String fbatchNo;		// 批次号
	private int fstockId;			// 仓库id
	private int fstockPlaceId;		// 仓位id
	private double fqty;			// 库存数量

	private ICItem_App icItem;
	private Stock_App stock;
	private StockPlace_App stockPlace;

	// 临时字段,不存表
	private boolean check; // 是否选中
	
	public ICInventory_App() {
		super();
	}

	public int getFitemId() {
		return fitemId;
	}

	public void setFitemId(int fitemId) {
		this.fitemId = fitemId;
	}

	public String getFbatchNo() {
		return fbatchNo;
	}

	public void setFbatchNo(String fbatchNo) {
		this.fbatchNo = fbatchNo;
	}

	public int getFstockId() {
		return fstockId;
	}

	public void setFstockId(int fstockId) {
		this.fstockId = fstockId;
	}

	public int getFstockPlaceId() {
		return fstockPlaceId;
	}

	public void setFstockPlaceId(int fstockPlaceId) {
		this.fstockPlaceId = fstockPlaceId;
	}

	public double getFqty() {
		return fqty;
	}

	public void setFqty(double fqty) {
		this.fqty = fqty;
	}

	public ICItem_App getIcItem() {
		return icItem;
	}

	public void setIcItem(ICItem_App icItem) {
		this.icItem = icItem;
	}

	public Stock_App getStock() {
		return stock;
	}

	public void setStock(Stock_App stock) {
		this.stock = stock;
	}

	public StockPlace_App getStockPlace() {
		return stockPlace;
	}

	public void setStockPlace(StockPlace_App stockPlace) {
		this.stockPlace = stockPlace;
	}

	public boolean isCheck() {
		return check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}
	
	

}