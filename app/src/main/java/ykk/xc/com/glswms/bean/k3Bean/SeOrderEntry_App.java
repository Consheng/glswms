package ykk.xc.com.glswms.bean.k3Bean;

import java.io.Serializable;
import java.util.List;

import ykk.xc.com.glswms.bean.ICStockBillEntryBarcode_App;

/**
 * 销售订单分录  ( SeOrderEntry )
 * @author Administrator
 *
 */
public class SeOrderEntry_App implements Serializable {
	private static final long serialVersionUID = 1L;

	private int fdetailId;				// 销售订单分录明细内码
	private int finterId;				// 订单内码
	private int fentryId;				// 分录号
	private int fitemId;				// 产品代码
	private int funitId;				// 单位
	private int fmrpClosed;				// 行业务关闭状态
	private double fqty;				// 基本单位数量
	private double fprice;				// 单价
	private double famount;				// 金额
	private double fcommitQty;			// 执行数量
	private double fstockQty;			// 出库数量
	private double lockQty;				// 锁库数量
	private String fnote;				// 备注

	private SeOrder_App seOrder;
	private ICItem_App icItem;
	private Unit_App unit;

	// 临时字段，不存表
	private int free; 					// 是否赠品，1:是，0:否
	private double useableQty; 			// 可用数
	private double realQty; 			// 实际数
	private int isCheck; 				// 是否选中
	private int isFocus; 				// 对焦到扫码行
	private String xsckInfo; 			// 拼接的销售出库单信息( id,单号，分录Id ),用_隔开
	private String expressCompany; 		// 快递公司
	private String icItemClassesName; 	// 产品类别名称（脚垫，坐垫）
	private List<ICStockBillEntryBarcode_App> icstockBillEntryBarcodes; // 条码记录

	private Stock_App lockStock;		// 锁库仓库
	private StockPlace_App lockStockPos; // 锁库库位
	
	public SeOrderEntry_App() {
		super();
	}

	public int getFdetailId() {
		return fdetailId;
	}

	public void setFdetailId(int fdetailId) {
		this.fdetailId = fdetailId;
	}

	public int getFinterId() {
		return finterId;
	}

	public void setFinterId(int finterId) {
		this.finterId = finterId;
	}

	public int getFentryId() {
		return fentryId;
	}

	public void setFentryId(int fentryId) {
		this.fentryId = fentryId;
	}

	public int getFitemId() {
		return fitemId;
	}

	public void setFitemId(int fitemId) {
		this.fitemId = fitemId;
	}

	public double getFqty() {
		return fqty;
	}

	public void setFqty(double fqty) {
		this.fqty = fqty;
	}

	public double getFcommitQty() {
		return fcommitQty;
	}

	public void setFcommitQty(double fcommitQty) {
		this.fcommitQty = fcommitQty;
	}

	public double getFprice() {
		return fprice;
	}

	public void setFprice(double fprice) {
		this.fprice = fprice;
	}

	public double getFamount() {
		return famount;
	}

	public void setFamount(double famount) {
		this.famount = famount;
	}

	public String getFnote() {
		return fnote;
	}

	public void setFnote(String fnote) {
		this.fnote = fnote;
	}

	public int getFunitId() {
		return funitId;
	}

	public void setFunitId(int funitId) {
		this.funitId = funitId;
	}

	public double getFstockQty() {
		return fstockQty;
	}

	public void setFstockQty(double fstockQty) {
		this.fstockQty = fstockQty;
	}

	public double getLockQty() {
		return lockQty;
	}

	public void setLockQty(double lockQty) {
		this.lockQty = lockQty;
	}

	public int getFmrpClosed() {
		return fmrpClosed;
	}

	public void setFmrpClosed(int fmrpClosed) {
		this.fmrpClosed = fmrpClosed;
	}

	public SeOrder_App getSeOrder() {
		return seOrder;
	}

	public void setSeOrder(SeOrder_App seOrder) {
		this.seOrder = seOrder;
	}

	public ICItem_App getIcItem() {
		return icItem;
	}

	public void setIcItem(ICItem_App icItem) {
		this.icItem = icItem;
	}

	public int getFree() {
		return free;
	}

	public void setFree(int free) {
		this.free = free;
	}

	public double getUseableQty() {
		return useableQty;
	}

	public void setUseableQty(double useableQty) {
		this.useableQty = useableQty;
	}

	public double getRealQty() {
		return realQty;
	}

	public void setRealQty(double realQty) {
		this.realQty = realQty;
	}

	public int getIsCheck() {
		return isCheck;
	}

	public void setIsCheck(int isCheck) {
		this.isCheck = isCheck;
	}

	public int getIsFocus() {
		return isFocus;
	}

	public void setIsFocus(int isFocus) {
		this.isFocus = isFocus;
	}

	public String getXsckInfo() {
		return xsckInfo;
	}

	public void setXsckInfo(String xsckInfo) {
		this.xsckInfo = xsckInfo;
	}

	public String getExpressCompany() {
		return expressCompany;
	}

	public void setExpressCompany(String expressCompany) {
		this.expressCompany = expressCompany;
	}

	public String getIcItemClassesName() {
		return icItemClassesName;
	}

	public void setIcItemClassesName(String icItemClassesName) {
		this.icItemClassesName = icItemClassesName;
	}

	public List<ICStockBillEntryBarcode_App> getIcstockBillEntryBarcodes() {
		return icstockBillEntryBarcodes;
	}

	public void setIcstockBillEntryBarcodes(List<ICStockBillEntryBarcode_App> icstockBillEntryBarcodes) {
		this.icstockBillEntryBarcodes = icstockBillEntryBarcodes;
	}

	public Stock_App getLockStock() {
		return lockStock;
	}

	public void setLockStock(Stock_App lockStock) {
		this.lockStock = lockStock;
	}

	public StockPlace_App getLockStockPos() {
		return lockStockPos;
	}

	public void setLockStockPos(StockPlace_App lockStockPos) {
		this.lockStockPos = lockStockPos;
	}

	public Unit_App getUnit() {
		return unit;
	}

	public void setUnit(Unit_App unit) {
		this.unit = unit;
	}

}