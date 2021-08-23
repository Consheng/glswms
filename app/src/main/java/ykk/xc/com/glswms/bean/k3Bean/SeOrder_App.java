package ykk.xc.com.glswms.bean.k3Bean;

import java.io.Serializable;

/**
 * 销售订单表 ( SeOrder )
 */

public class SeOrder_App implements Serializable {
	private static final long serialVersionUID = 1L;

	private int finterId;				// 订单内码
	private String fbillNo;				// 编 号
	private int fcustId;				// 客户id
	private int fdeptId;				// 部门id
	private int ftranType;				// 单据类型
	private int fstatus;				// 状态
	private int fempId;					// 业务员
	private int fbillerId;				// 制单
	private int fcheckerId;				// 审核人
	private int fmangerId;				// 主管
	private int fclosed;				// 是否关闭
	private String fdate;				// 日期
	private String ffetchStyle;			// 交货方式
	private String ffetchDate;			// 交货日期
	private String ffetchAdd;			// 交货地点
	private String fconsignee;			// 收货方
	private String fnote;				// 备注
	private String fexplanation;		// 摘要
	private String orderType;			// 订单类型 ( 01：按单生产，02：备货型生产）

	private Customer_App cust;
	private Department_App department;

	// 临时字段，不存表
	private String printExpressNo;	// 出库后打印的快递单
	
	public SeOrder_App() {
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

	public int getFcustId() {
		return fcustId;
	}

	public void setFcustId(int fcustId) {
		this.fcustId = fcustId;
	}

	public int getFdeptId() {
		return fdeptId;
	}

	public void setFdeptId(int fdeptId) {
		this.fdeptId = fdeptId;
	}

	public String getFdate() {
		return fdate;
	}

	public void setFdate(String fdate) {
		this.fdate = fdate;
	}

	public String getFfetchStyle() {
		return ffetchStyle;
	}

	public void setFfetchStyle(String ffetchStyle) {
		this.ffetchStyle = ffetchStyle;
	}

	public String getFfetchDate() {
		return ffetchDate;
	}

	public void setFfetchDate(String ffetchDate) {
		this.ffetchDate = ffetchDate;
	}

	public String getFfetchAdd() {
		return ffetchAdd;
	}

	public void setFfetchAdd(String ffetchAdd) {
		this.ffetchAdd = ffetchAdd;
	}

	public int getFtranType() {
		return ftranType;
	}

	public void setFtranType(int ftranType) {
		this.ftranType = ftranType;
	}

	public int getFstatus() {
		return fstatus;
	}

	public void setFstatus(int fstatus) {
		this.fstatus = fstatus;
	}

	public int getFempId() {
		return fempId;
	}

	public void setFempId(int fempId) {
		this.fempId = fempId;
	}

	public int getFbillerId() {
		return fbillerId;
	}

	public void setFbillerId(int fbillerId) {
		this.fbillerId = fbillerId;
	}

	public int getFcheckerId() {
		return fcheckerId;
	}

	public void setFcheckerId(int fcheckerId) {
		this.fcheckerId = fcheckerId;
	}

	public int getFmangerId() {
		return fmangerId;
	}

	public void setFmangerId(int fmangerId) {
		this.fmangerId = fmangerId;
	}

	public int getFclosed() {
		return fclosed;
	}

	public void setFclosed(int fclosed) {
		this.fclosed = fclosed;
	}

	public String getFconsignee() {
		return fconsignee;
	}

	public void setFconsignee(String fconsignee) {
		this.fconsignee = fconsignee;
	}

	public String getFnote() {
		return fnote;
	}

	public void setFnote(String fnote) {
		this.fnote = fnote;
	}

	public String getFexplanation() {
		return fexplanation;
	}

	public void setFexplanation(String fexplanation) {
		this.fexplanation = fexplanation;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public Customer_App getCust() {
		return cust;
	}

	public void setCust(Customer_App cust) {
		this.cust = cust;
	}

	public Department_App getDepartment() {
		return department;
	}

	public void setDepartment(Department_App department) {
		this.department = department;
	}

	public String getPrintExpressNo() {
		return printExpressNo;
	}

	public void setPrintExpressNo(String printExpressNo) {
		this.printExpressNo = printExpressNo;
	}

	
}