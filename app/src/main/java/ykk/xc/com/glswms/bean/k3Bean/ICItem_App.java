package ykk.xc.com.glswms.bean.k3Bean;

import java.io.Serializable;

/**
 *  物料表
 */
public class ICItem_App implements Serializable {
	private static final long serialVersionUID = 1L;

	/* K3物料id */
	private int fitemId;
	private String fmodel;			// 物料规格型号
	private String fnumber;			// 物料代码
	private String fname;			// 物料名称
	private String ffullName;		// 物料全称
	private int fdeleted;			// 是否禁用
	private int ferpclsId;			// 物料属性id 1:外购,2:自制,3:委外加工
	private int funitId;			// 物料主计量单位id
	private int batchManager;		// 是否启用批次号
	private int snManager;		// 是否启用序列号
	private int fdefaultLoc;		// 默认仓库id
	private int fspId;				// 默认库位id
	private int icItemTypeId;		// 产品类型id ( 20013：主产品，20014：副产品，2000003：赠品 )
	private String barcode;			// 条码
	private String custNumber;		// 客户编码
	private String custDescribe;	// 客户描述
	private int boxQty;				// 箱数

	private Unit_App unit;
	private Stock_App stock; // 仓库
	private StockPlace_App stockPos; // 仓库

	// 临时字段，不加表
	private boolean check; // 是否选中

	public ICItem_App() {
		super();
	}

	public int getFitemId() {
		return fitemId;
	}

	public void setFitemId(int fitemId) {
		this.fitemId = fitemId;
	}

	public String getFmodel() {
		return fmodel;
	}

	public void setFmodel(String fmodel) {
		this.fmodel = fmodel;
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

	public String getFfullName() {
		return ffullName;
	}

	public void setFfullName(String ffullName) {
		this.ffullName = ffullName;
	}

	public int getFdeleted() {
		return fdeleted;
	}

	public void setFdeleted(int fdeleted) {
		this.fdeleted = fdeleted;
	}

	public int getFerpclsId() {
		return ferpclsId;
	}

	public void setFerpclsId(int ferpclsId) {
		this.ferpclsId = ferpclsId;
	}

	public int getFunitId() {
		return funitId;
	}

	public void setFunitId(int funitId) {
		this.funitId = funitId;
	}

	public int getBatchManager() {
		return batchManager;
	}

	public void setBatchManager(int batchManager) {
		this.batchManager = batchManager;
	}

	public int getSnManager() {
		return snManager;
	}

	public void setSnManager(int snManager) {
		this.snManager = snManager;
	}

	public int getFdefaultLoc() {
		return fdefaultLoc;
	}

	public void setFdefaultLoc(int fdefaultLoc) {
		this.fdefaultLoc = fdefaultLoc;
	}

	public int getFspId() {
		return fspId;
	}

	public void setFspId(int fspId) {
		this.fspId = fspId;
	}

	public int getIcItemTypeId() {
		return icItemTypeId;
	}

	public void setIcItemTypeId(int icItemTypeId) {
		this.icItemTypeId = icItemTypeId;
	}

	public Unit_App getUnit() {
		return unit;
	}

	public void setUnit(Unit_App unit) {
		this.unit = unit;
	}

	public Stock_App getStock() {
		return stock;
	}

	public void setStock(Stock_App stock) {
		this.stock = stock;
	}

	public StockPlace_App getStockPos() {
		return stockPos;
	}

	public void setStockPos(StockPlace_App stockPos) {
		this.stockPos = stockPos;
	}

	public boolean isCheck() {
		return check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getCustNumber() {
		return custNumber;
	}

	public void setCustNumber(String custNumber) {
		this.custNumber = custNumber;
	}

	public String getCustDescribe() {
		return custDescribe;
	}

	public void setCustDescribe(String custDescribe) {
		this.custDescribe = custDescribe;
	}

	public int getBoxQty() {
		return boxQty;
	}

	public void setBoxQty(int boxQty) {
		this.boxQty = boxQty;
	}
	
}