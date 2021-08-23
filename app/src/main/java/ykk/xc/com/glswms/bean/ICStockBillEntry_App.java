package ykk.xc.com.glswms.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ykk.xc.com.glswms.bean.k3Bean.ICItem_App;
import ykk.xc.com.glswms.bean.k3Bean.StockPlace_App;
import ykk.xc.com.glswms.bean.k3Bean.Stock_App;
import ykk.xc.com.glswms.bean.k3Bean.Unit_App;
import ykk.xc.com.glswms.comm.Comm;

/**
 * Wms 本地的出入库	Entry表
 * @author Administrator
 *
 */
public class ICStockBillEntry_App implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id; 					//
	private int icstockBillId;			// 主表id
	private int fitemId;				// 物料id
	private int fentryId;				// 分录id
	private int fdcStockId;				// 调入仓库id
	private int fdcSPId;				// 调入库位id
	private int fscStockId;				// 调出仓库id
	private int fscSPId;				// 调出库位id
	private double fqty;				// 数量
	private double fprice;				// 单价
	private double ftaxRate;			// 税率
	private double ftaxPrice;			// 含税单价
	private int free;					// 是否免费
	private int funitId;				// 单位id
	private int fsourceInterId;			// 来源内码id
	private int fsourceEntryId;			// 来源分录id
	private int fsourceTranType;		// 来源类型
	private String fsourceBillNo;		// 来源单号
	private double fsourceQty;			// 来源单数量
	private int forderInterId;			// 来源订单id
	private String forderBillNo;		// 来源订单号
	private int forderEntryId;			// 来源订单分录id
	private int fdetailId; 				// 来源分类唯一行标识
	private String remark;				// 备注

	private ICStockBill_App icstockBill;
	private Stock_App stock;
	private StockPlace_App stockPlace;
	private Stock_App stock2;
	private StockPlace_App stockPlace2;
	private ICItem_App icItem;
	private Unit_App unit;

	// 临时字段，不存表
	private boolean showButton; 		// 是否显示操作按钮
	private String smBatchCode; // 扫码的批次号
	private String smSnCode; // 扫码的序列号
	private double smQty;	// 扫码后计算出的数
	private String strBatchCode; // 拼接的批次号
	private String strBarcode;	// 用于显示拼接的条码号
	private double inventoryNowQty; // 当前扫码的可用库存数
	private double inStockQty; // 调入仓库库存
	private double outStockQty; // 调出仓库库存
	private double mulNum;		// 乘数，Bom子项的数量
	private double ptQty;	// SCRK_ScanRecordEntry_App表中的( fqty )字段
	private double wptQty;	// SCRK_ScanRecordEntry_App表中的( smSumQty )字段
	private int scrkScanRecordEntryId; // SCRK_ScanRecordEntry_App表中的( id )字段

	private ICStockBillEntry_App sourceThis; // 来源本身对象
	private List<ICStockBillEntryBarcode_App> icstockBillEntryBarcodes; // 条码记录
	
	public ICStockBillEntry_App() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIcstockBillId() {
		return icstockBillId;
	}

	public void setIcstockBillId(int icstockBillId) {
		this.icstockBillId = icstockBillId;
	}

	public int getFitemId() {
		return fitemId;
	}

	public void setFitemId(int fitemId) {
		this.fitemId = fitemId;
	}

	public int getFentryId() {
		return fentryId;
	}

	public void setFentryId(int fentryId) {
		this.fentryId = fentryId;
	}

	public int getFdcStockId() {
		return fdcStockId;
	}

	public void setFdcStockId(int fdcStockId) {
		this.fdcStockId = fdcStockId;
	}

	public int getFdcSPId() {
		return fdcSPId;
	}

	public void setFdcSPId(int fdcSPId) {
		this.fdcSPId = fdcSPId;
	}

	public int getFscStockId() {
		return fscStockId;
	}

	public void setFscStockId(int fscStockId) {
		this.fscStockId = fscStockId;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public int getFscSPId() {
		return fscSPId;
	}

	public void setFscSPId(int fscSPId) {
		this.fscSPId = fscSPId;
	}

	public double getFqty() {
		return fqty;
	}

	public void setFqty(double fqty) {
		this.fqty = fqty;
	}

	public double getFprice() {
		return fprice;
	}

	public void setFprice(double fprice) {
		this.fprice = fprice;
	}

	public double getFtaxRate() {
		return ftaxRate;
	}

	public void setFtaxRate(double ftaxRate) {
		this.ftaxRate = ftaxRate;
	}

	public double getFtaxPrice() {
		return ftaxPrice;
	}

	public void setFtaxPrice(double ftaxPrice) {
		this.ftaxPrice = ftaxPrice;
	}

	public int getFree() {
		return free;
	}

	public void setFree(int free) {
		this.free = free;
	}

	public int getFunitId() {
		return funitId;
	}

	public void setFunitId(int funitId) {
		this.funitId = funitId;
	}

	public int getFsourceInterId() {
		return fsourceInterId;
	}

	public void setFsourceInterId(int fsourceInterId) {
		this.fsourceInterId = fsourceInterId;
	}

	public int getFsourceEntryId() {
		return fsourceEntryId;
	}

	public void setFsourceEntryId(int fsourceEntryId) {
		this.fsourceEntryId = fsourceEntryId;
	}

	public int getFsourceTranType() {
		return fsourceTranType;
	}

	public void setFsourceTranType(int fsourceTranType) {
		this.fsourceTranType = fsourceTranType;
	}

	public String getFsourceBillNo() {
		return fsourceBillNo;
	}

	public void setFsourceBillNo(String fsourceBillNo) {
		this.fsourceBillNo = fsourceBillNo;
	}

	public double getFsourceQty() {
		return fsourceQty;
	}

	public void setFsourceQty(double fsourceQty) {
		this.fsourceQty = fsourceQty;
	}

	public int getForderInterId() {
		return forderInterId;
	}

	public void setForderInterId(int forderInterId) {
		this.forderInterId = forderInterId;
	}

	public String getForderBillNo() {
		return forderBillNo;
	}

	public void setForderBillNo(String forderBillNo) {
		this.forderBillNo = forderBillNo;
	}

	public int getForderEntryId() {
		return forderEntryId;
	}

	public void setForderEntryId(int forderEntryId) {
		this.forderEntryId = forderEntryId;
	}

	public int getFdetailId() {
		return fdetailId;
	}

	public void setFdetailId(int fdetailId) {
		this.fdetailId = fdetailId;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Stock_App getStock() {
		return stock;
	}

	public void setStock(Stock_App stock) {
		this.stock = stock;
	}

	public Stock_App getStock2() {
		return stock2;
	}

	public void setStock2(Stock_App stock2) {
		this.stock2 = stock2;
	}

	public ICItem_App getIcItem() {
		return icItem;
	}

	public void setIcItem(ICItem_App icItem) {
		this.icItem = icItem;
	}

	public ICStockBill_App getIcstockBill() {
		return icstockBill;
	}

	public void setIcstockBill(ICStockBill_App icstockBill) {
		this.icstockBill = icstockBill;
	}

	public Unit_App getUnit() {
		return unit;
	}

	public void setUnit(Unit_App unit) {
		this.unit = unit;
	}

	public boolean isShowButton() {
		return showButton;
	}

	public void setShowButton(boolean showButton) {
		this.showButton = showButton;
	}

	public String getSmBatchCode() {
		return smBatchCode;
	}

	public void setSmBatchCode(String smBatchCode) {
		this.smBatchCode = smBatchCode;
	}

	public String getSmSnCode() {
		return smSnCode;
	}

	public void setSmSnCode(String smSnCode) {
		this.smSnCode = smSnCode;
	}

	public String getStrBatchCode() {
		// 存在大写的逗号（,）,且大于1
		if(Comm.isNULLS(strBatchCode).indexOf(",") > -1 && Comm.isNULLS(strBatchCode).length() > 0) {
			return strBatchCode.substring(0, strBatchCode.length()-1);
		}
		return strBatchCode;
	}

	public void setStrBatchCode(String strBatchCode) {
		this.strBatchCode = strBatchCode;
	}

	public String getStrBarcode() {
		return strBarcode;
	}

	public void setStrBarcode(String strBarcode) {
		this.strBarcode = strBarcode;
	}

	public double getInventoryNowQty() {
		return inventoryNowQty;
	}

	public void setInventoryNowQty(double inventoryNowQty) {
		this.inventoryNowQty = inventoryNowQty;
	}

	public ICStockBillEntry_App getSourceThis() {
		return sourceThis;
	}

	public void setSourceThis(ICStockBillEntry_App sourceThis) {
		this.sourceThis = sourceThis;
	}

	public List<ICStockBillEntryBarcode_App> getIcstockBillEntryBarcodes() {
		if(icstockBillEntryBarcodes == null) {
			icstockBillEntryBarcodes = new ArrayList<>();
		}
		return icstockBillEntryBarcodes;
	}

	public void setIcstockBillEntryBarcodes(List<ICStockBillEntryBarcode_App> icstockBillEntryBarcodes) {
		this.icstockBillEntryBarcodes = icstockBillEntryBarcodes;
	}
	
	public double getInStockQty() {
		return inStockQty;
	}

	public void setInStockQty(double inStockQty) {
		this.inStockQty = inStockQty;
	}

	public double getOutStockQty() {
		return outStockQty;
	}

	public void setOutStockQty(double outStockQty) {
		this.outStockQty = outStockQty;
	}

	public double getSmQty() {
		return smQty;
	}

	public void setSmQty(double smQty) {
		this.smQty = smQty;
	}

	public StockPlace_App getStockPlace() {
		return stockPlace;
	}

	public void setStockPlace(StockPlace_App stockPlace) {
		this.stockPlace = stockPlace;
	}

	public StockPlace_App getStockPlace2() {
		return stockPlace2;
	}

	public void setStockPlace2(StockPlace_App stockPlace2) {
		this.stockPlace2 = stockPlace2;
	}

	public double getMulNum() {
		return mulNum;
	}

	public void setMulNum(double mulNum) {
		this.mulNum = mulNum;
	}

	public double getPtQty() {
		return ptQty;
	}

	public void setPtQty(double ptQty) {
		this.ptQty = ptQty;
	}

	public double getWptQty() {
		return wptQty;
	}

	public void setWptQty(double wptQty) {
		this.wptQty = wptQty;
	}

	public int getScrkScanRecordEntryId() {
		return scrkScanRecordEntryId;
	}

	public void setScrkScanRecordEntryId(int scrkScanRecordEntryId) {
		this.scrkScanRecordEntryId = scrkScanRecordEntryId;
	}

}
