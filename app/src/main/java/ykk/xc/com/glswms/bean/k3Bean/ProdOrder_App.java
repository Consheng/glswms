package ykk.xc.com.glswms.bean.k3Bean;

import java.io.Serializable;

/**
 * 生产任务单
 */
public class ProdOrder_App implements Serializable {
	private static final long serialVersionUID = 1L;

	private int finterId;				// 生产订单id
	private String fbillNo;				// 生产订单号
	private String fconfirmDate;		// 制单日期
	private String fcheckDate;			// 审核日期
	private int fworkShop;				// 生产车间id
	private int fitemId;				// 物料id
	private int funitId;				// 单位id
	private double fqty;				// 数量
	private double fauxInHighLimitQty;	// 超收上限数量
	private int fsourceInterId;			// 源单id
	private int fsourceEntryId;			// 源单分录id
	private String fsourceBillNo;		// 来源单号
	private int forderInterId;			// 销售订单id
	private int fstatus;				// 生产任务单单据状态（ 0：计划，1：下达，3：结案）
	private int fclosed;				// 是否关闭0：未关闭，1：已关闭
	private String fnote;				// 备注
	private String orderType;			// 订单类型 ( 01：按单生产，02：备货型生产）
	private String barcode;				// 条码

	private ICItem_App icItem;
	private Unit_App unit;
	private Department_App dept; // 部门对象

	// 临时字段，不存表
	private int isCheck; // 是否选中
	private String strBarcode; // 拼接的单号
	private double useableQty; // 可用数量

	public ProdOrder_App() {
		super();
	}

	public int getFinterId() {
		return finterId;
	}

	public void setFinterId(int finterId) {
		this.finterId = finterId;
	}

	public String getFbillNo() {
		return fbillNo;
	}

	public void setFbillNo(String fbillNo) {
		this.fbillNo = fbillNo;
	}

	public String getFconfirmDate() {
		return fconfirmDate;
	}

	public void setFconfirmDate(String fconfirmDate) {
		this.fconfirmDate = fconfirmDate;
	}

	public String getFcheckDate() {
		return fcheckDate;
	}

	public void setFcheckDate(String fcheckDate) {
		this.fcheckDate = fcheckDate;
	}

	public int getFworkShop() {
		return fworkShop;
	}

	public void setFworkShop(int fworkShop) {
		this.fworkShop = fworkShop;
	}

	public int getFitemId() {
		return fitemId;
	}

	public void setFitemId(int fitemId) {
		this.fitemId = fitemId;
	}

	public int getFunitId() {
		return funitId;
	}

	public void setFunitId(int funitId) {
		this.funitId = funitId;
	}

	public String getFnote() {
		return fnote;
	}

	public void setFnote(String fnote) {
		this.fnote = fnote;
	}

	public double getFqty() {
		return fqty;
	}

	public void setFqty(double fqty) {
		this.fqty = fqty;
	}

	public double getFauxInHighLimitQty() {
		return fauxInHighLimitQty;
	}

	public void setFauxInHighLimitQty(double fauxInHighLimitQty) {
		this.fauxInHighLimitQty = fauxInHighLimitQty;
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

	public String getFsourceBillNo() {
		return fsourceBillNo;
	}

	public void setFsourceBillNo(String fsourceBillNo) {
		this.fsourceBillNo = fsourceBillNo;
	}

	public int getForderInterId() {
		return forderInterId;
	}

	public void setForderInterId(int forderInterId) {
		this.forderInterId = forderInterId;
	}

	public int getFstatus() {
		return fstatus;
	}

	public void setFstatus(int fstatus) {
		this.fstatus = fstatus;
	}

	public int getFclosed() {
		return fclosed;
	}

	public void setFclosed(int fclosed) {
		this.fclosed = fclosed;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public ICItem_App getIcItem() {
		return icItem;
	}

	public void setIcItem(ICItem_App icItem) {
		this.icItem = icItem;
	}

	public Unit_App getUnit() {
		return unit;
	}

	public void setUnit(Unit_App unit) {
		this.unit = unit;
	}

	public Department_App getDept() {
		return dept;
	}

	public void setDept(Department_App dept) {
		this.dept = dept;
	}

	public int getIsCheck() {
		return isCheck;
	}

	public void setIsCheck(int isCheck) {
		this.isCheck = isCheck;
	}

	public String getStrBarcode() {
		return strBarcode;
	}

	public void setStrBarcode(String strBarcode) {
		this.strBarcode = strBarcode;
	}

	public double getUseableQty() {
		return useableQty;
	}

	public void setUseableQty(double useableQty) {
		this.useableQty = useableQty;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

}
