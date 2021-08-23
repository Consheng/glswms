package ykk.xc.com.glswms.bean;

import ykk.xc.com.glswms.bean.k3Bean.Department_App;
import ykk.xc.com.glswms.bean.k3Bean.ICItem_App;

/**
 * 条码bean
 * @author Administrator
 *
 */
public class BarcodeTable_App {

	private int id;
	/**
	 * 1：仓库
	 * 2：库区
	 * 3：货架
	 * 4：库位
	 * 5：容器
	 * 6：供应商
	 * 7：客户
	 * 8：部门
	 *
	 * 21：物料
	 * 22：物料包装
	 * 23：盘点表
	 * 31：生产订单
	 * 41：采购订单
	 * 42：收料通知单
	 * 51：销售订单
	 * 52：发货通知单
	 * 61：委外订单
	 * 71：资产卡片
	 * 81：其他入库单
	 */
	private int caseId;						// 生码类型
	private int relationBillId;				// 关联单据id
	private String relationBillNumber;		// 关联单据号
	private int relationBillEntryId;		// 关联单据分录id
	private int fitemId;					// 项目id
	private String fitemNumber;				// 项目代码
	private String fitemName;				// 项目名称
	private String barcode;					// 条码
	private double barcodeQty;				// 条码数量
	private String snCode;					// 序列号
	private String batchCode;				// 批次号
	private int printNumber;				// 打印次数
	private String createDateTime;			// 创建时间
	private String createrName;				// 创建人名称

	private ICItem_App icItem;
	private Department_App dept;

	// 临时字段，不存表
	private String relationObj; // 关联对象

	public BarcodeTable_App() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCaseId() {
		return caseId;
	}

	public void setCaseId(int caseId) {
		this.caseId = caseId;
	}

	public int getRelationBillId() {
		return relationBillId;
	}

	public void setRelationBillId(int relationBillId) {
		this.relationBillId = relationBillId;
	}

	public String getRelationBillNumber() {
		return relationBillNumber;
	}

	public void setRelationBillNumber(String relationBillNumber) {
		this.relationBillNumber = relationBillNumber;
	}

	public int getRelationBillEntryId() {
		return relationBillEntryId;
	}

	public void setRelationBillEntryId(int relationBillEntryId) {
		this.relationBillEntryId = relationBillEntryId;
	}

	public int getFitemId() {
		return fitemId;
	}

	public void setFitemId(int fitemId) {
		this.fitemId = fitemId;
	}

	public String getFitemNumber() {
		return fitemNumber;
	}

	public void setFitemNumber(String fitemNumber) {
		this.fitemNumber = fitemNumber;
	}

	public String getFitemName() {
		return fitemName;
	}

	public void setFitemName(String fitemName) {
		this.fitemName = fitemName;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public double getBarcodeQty() {
		return barcodeQty;
	}

	public void setBarcodeQty(double barcodeQty) {
		this.barcodeQty = barcodeQty;
	}

	public String getSnCode() {
		return snCode;
	}

	public void setSnCode(String snCode) {
		this.snCode = snCode;
	}

	public String getBatchCode() {
		return batchCode;
	}

	public void setBatchCode(String batchCode) {
		this.batchCode = batchCode;
	}

	public int getPrintNumber() {
		return printNumber;
	}

	public void setPrintNumber(int printNumber) {
		this.printNumber = printNumber;
	}

	public String getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
	}

	public String getCreaterName() {
		return createrName;
	}

	public void setCreaterName(String createrName) {
		this.createrName = createrName;
	}

	public ICItem_App getIcItem() {
		return icItem;
	}

	public void setIcItem(ICItem_App icItem) {
		this.icItem = icItem;
	}

	public Department_App getDept() {
		return dept;
	}

	public void setDept(Department_App dept) {
		this.dept = dept;
	}

	public String getRelationObj() {
		return relationObj;
	}

	public void setRelationObj(String relationObj) {
		this.relationObj = relationObj;
	}

}
