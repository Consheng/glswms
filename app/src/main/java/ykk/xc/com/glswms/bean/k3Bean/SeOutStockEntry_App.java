package ykk.xc.com.glswms.bean.k3Bean;

import java.io.Serializable;

/**
 * @Description:发货通知单表体
 *
 * @author 2019年5月10日 下午5:11:31
 */
public class SeOutStockEntry_App implements Serializable {
	private static final long serialVersionUID = 1L;

	private int fdetailId; // 唯一行号

	private int finterId;/* 通知单内码 */

	private int fentryId;/* 分录号 */

	private int fitemId;/* 产品代码 */

	private double fqty;/* 基本单位数量 */

	private double fcommitQty;/* 发货数量 */

	private double fprice;/* 单价 */

	private double famount;/* 金额 */

	private String forderInterId;/* 销售订单 */

	private String fdate;/* 日期 */

	private String fnote;/* 备注 */

	private double finvoiceQty;/* 开票数量 */

	private double fbcommitQty;/* 退货数量 */

	private int funitId;/* 单位 */

	private double fauxbcommitQty;/* 辅助退货数量 */

	private double fauxcommitQty;/* 辅助发货数量 */

	private double fauxinvoiceQty;/* 辅助开票数量 */

	private double fauxPrice;/* 单价 */

	private double fauxQty;/* 数量 */

	private int fsourceEntryId;/* 源单行号 */

	private String fmapNumber;/* 对应代码 */

	private String fmapName;/* 对应名称 */

	private int fauxPropId;/* 辅助属性 */

	private String fbatchNo;/* 批号 */

	private String fcheckDate;/* 审核日期 */

	private String fexplanation;/* 摘要 */

	private String ffetchadd;/* 交货地点 */

	private String ffetchDate;/* 交货日期 */

	private double fseccoefficient;/* 换算率 */

	private double fsecQty;/* 辅助数量 */

	private double fseccommitQty;/* 辅助执行数量 */

	private int fsourceTranType;/* 源单类型 */

	private int fsourceInterId;/* 源单内码 */

	private String fsourceBillNo;/* 源单单号 */

	private int fcontractInterId;/* 合同内码 */

	private int fcontractEntryId;/* 合同分录 */

	private String fcontractBillNo;/* 合同单号 */

	private int forderEntryId;/* 订单分录 */

	private String forderBillNo;/* 订单单号 */

	private int fstockId;/* 仓库 */

	private int fplanMode;/* 计划模式 */

	private String fmtoNo;/* 计划跟踪号 */

	private double fstockQty;/* 基本单位出库数量 */

	private double fauxStockQty;/* 出库数量 */

	private double fsecStockQty;/* 辅助单位出库数量 */

	private SeOutStock_App seOutStock;
	private ICItem_App icItem;
	private Unit_App unit;
	private Stock_App stock; // 仓库

	// 临时字段，不存表
	private double useableQty; // 可用数
	private int isCheck; // 是否选中
	
	public SeOutStockEntry_App() {
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

	public String getForderInterId() {
		return forderInterId;
	}

	public void setForderInterId(String forderInterId) {
		this.forderInterId = forderInterId;
	}

	public String getFdate() {
		return fdate;
	}

	public void setFdate(String fdate) {
		this.fdate = fdate;
	}

	public String getFnote() {
		return fnote;
	}

	public void setFnote(String fnote) {
		this.fnote = fnote;
	}

	public double getFinvoiceQty() {
		return finvoiceQty;
	}

	public void setFinvoiceQty(double finvoiceQty) {
		this.finvoiceQty = finvoiceQty;
	}

	public double getFbcommitQty() {
		return fbcommitQty;
	}

	public void setFbcommitQty(double fbcommitQty) {
		this.fbcommitQty = fbcommitQty;
	}

	public int getFunitId() {
		return funitId;
	}

	public void setFunitId(int funitId) {
		this.funitId = funitId;
	}

	public double getFauxbcommitQty() {
		return fauxbcommitQty;
	}

	public void setFauxbcommitQty(double fauxbcommitQty) {
		this.fauxbcommitQty = fauxbcommitQty;
	}

	public double getFauxcommitQty() {
		return fauxcommitQty;
	}

	public void setFauxcommitQty(double fauxcommitQty) {
		this.fauxcommitQty = fauxcommitQty;
	}

	public double getFauxinvoiceQty() {
		return fauxinvoiceQty;
	}

	public void setFauxinvoiceQty(double fauxinvoiceQty) {
		this.fauxinvoiceQty = fauxinvoiceQty;
	}

	public double getFauxPrice() {
		return fauxPrice;
	}

	public void setFauxPrice(double fauxPrice) {
		this.fauxPrice = fauxPrice;
	}

	public double getFauxQty() {
		return fauxQty;
	}

	public void setFauxQty(double fauxQty) {
		this.fauxQty = fauxQty;
	}

	public int getFsourceEntryId() {
		return fsourceEntryId;
	}

	public void setFsourceEntryId(int fsourceEntryId) {
		this.fsourceEntryId = fsourceEntryId;
	}

	public String getFmapNumber() {
		return fmapNumber;
	}

	public void setFmapNumber(String fmapNumber) {
		this.fmapNumber = fmapNumber;
	}

	public String getFmapName() {
		return fmapName;
	}

	public void setFmapName(String fmapName) {
		this.fmapName = fmapName;
	}

	public int getFauxPropId() {
		return fauxPropId;
	}

	public void setFauxPropId(int fauxPropId) {
		this.fauxPropId = fauxPropId;
	}

	public String getFbatchNo() {
		return fbatchNo;
	}

	public void setFbatchNo(String fbatchNo) {
		this.fbatchNo = fbatchNo;
	}

	public String getFcheckDate() {
		return fcheckDate;
	}

	public void setFcheckDate(String fcheckDate) {
		this.fcheckDate = fcheckDate;
	}

	public String getFexplanation() {
		return fexplanation;
	}

	public void setFexplanation(String fexplanation) {
		this.fexplanation = fexplanation;
	}

	public String getFfetchadd() {
		return ffetchadd;
	}

	public void setFfetchadd(String ffetchadd) {
		this.ffetchadd = ffetchadd;
	}

	public String getFfetchDate() {
		return ffetchDate;
	}

	public void setFfetchDate(String ffetchDate) {
		this.ffetchDate = ffetchDate;
	}

	public double getFseccoefficient() {
		return fseccoefficient;
	}

	public void setFseccoefficient(double fseccoefficient) {
		this.fseccoefficient = fseccoefficient;
	}

	public double getFsecQty() {
		return fsecQty;
	}

	public void setFsecQty(double fsecQty) {
		this.fsecQty = fsecQty;
	}

	public double getFseccommitQty() {
		return fseccommitQty;
	}

	public void setFseccommitQty(double fseccommitQty) {
		this.fseccommitQty = fseccommitQty;
	}

	public int getFsourceTranType() {
		return fsourceTranType;
	}

	public void setFsourceTranType(int fsourceTranType) {
		this.fsourceTranType = fsourceTranType;
	}

	public int getFsourceInterId() {
		return fsourceInterId;
	}

	public void setFsourceInterId(int fsourceInterId) {
		this.fsourceInterId = fsourceInterId;
	}

	public String getFsourceBillNo() {
		return fsourceBillNo;
	}

	public void setFsourceBillNo(String fsourceBillNo) {
		this.fsourceBillNo = fsourceBillNo;
	}

	public int getFcontractInterId() {
		return fcontractInterId;
	}

	public void setFcontractInterId(int fcontractInterId) {
		this.fcontractInterId = fcontractInterId;
	}

	public int getFcontractEntryId() {
		return fcontractEntryId;
	}

	public void setFcontractEntryId(int fcontractEntryId) {
		this.fcontractEntryId = fcontractEntryId;
	}

	public String getFcontractBillNo() {
		return fcontractBillNo;
	}

	public void setFcontractBillNo(String fcontractBillNo) {
		this.fcontractBillNo = fcontractBillNo;
	}

	public int getForderEntryId() {
		return forderEntryId;
	}

	public void setForderEntryId(int forderEntryId) {
		this.forderEntryId = forderEntryId;
	}

	public String getForderBillNo() {
		return forderBillNo;
	}

	public void setForderBillNo(String forderBillNo) {
		this.forderBillNo = forderBillNo;
	}

	public int getFstockId() {
		return fstockId;
	}

	public void setFstockId(int fstockId) {
		this.fstockId = fstockId;
	}

	public int getFplanMode() {
		return fplanMode;
	}

	public void setFplanMode(int fplanMode) {
		this.fplanMode = fplanMode;
	}

	public String getFmtoNo() {
		return fmtoNo;
	}

	public void setFmtoNo(String fmtoNo) {
		this.fmtoNo = fmtoNo;
	}

	public double getFstockQty() {
		return fstockQty;
	}

	public void setFstockQty(double fstockQty) {
		this.fstockQty = fstockQty;
	}

	public double getFauxStockQty() {
		return fauxStockQty;
	}

	public void setFauxStockQty(double fauxStockQty) {
		this.fauxStockQty = fauxStockQty;
	}

	public double getFsecStockQty() {
		return fsecStockQty;
	}

	public void setFsecStockQty(double fsecStockQty) {
		this.fsecStockQty = fsecStockQty;
	}

	public SeOutStock_App getSeOutStock() {
		return seOutStock;
	}

	public void setSeOutStock(SeOutStock_App seOutStock) {
		this.seOutStock = seOutStock;
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

	public double getUseableQty() {
		return useableQty;
	}

	public void setUseableQty(double useableQty) {
		this.useableQty = useableQty;
	}

	public int getIsCheck() {
		return isCheck;
	}

	public void setIsCheck(int isCheck) {
		this.isCheck = isCheck;
	}

	public Unit_App getUnit() {
		return unit;
	}

	public void setUnit(Unit_App unit) {
		this.unit = unit;
	}
	
	
}