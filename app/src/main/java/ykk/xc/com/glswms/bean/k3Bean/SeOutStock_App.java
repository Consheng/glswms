package ykk.xc.com.glswms.bean.k3Bean;

import java.io.Serializable;

/**
 * @Description:发货通知表
 *
 * @author 2019年5月10日 下午5:10:39
 */
public class SeOutStock_App implements Serializable {
	private static final long serialVersionUID = 1L;

	private int finterId;/* 通知单内码 */

	private String fbillNo;/* 编 号 */

	private int ftranType;/* 单据类型 */

	private int fsalType;/* 销售方式 */

	private int fcustId;/* 购货单位 */

	private String fdate;/* 日期 */

	private int fstockId;/* 收货仓库 */

	private String fadd;/* 地址 */

	private String fnote;/* 退料原因 */

	private int fempId;/* 业务员 */

	private int fcheckerId;/* 审核人 */

	private int fbillerId;/* 制单人 */

	private int fmanagerId;/* 主管人 */

	private int fclosed;/* 关闭状态，0未关闭，1关闭 */

	private int finvoiceClosed;/* 发票关闭 */

	private int fdeptId;/* 部门 */

	private int fsettleId;/* 结算方式 */

	private int ftranStatus;/* 传输状态 */

	private double fexchangerate;/* 汇 率: */

	private int fcurrencyId;/* 币 别 */

	private int fstatus;/* 状态 ( 0：未审核，1：审核，2：k3自己下推部分到出库，3：结案 ) */

	private int fcancellation;/* 作废 */

	private int fcurcheckLevel;/* 当前审核级别 */

	private int frelatebrId;/* 订货机构 */

	private String fcheckDate;/* 审核日期 */

	private String fexplanation;/* 摘要 */

	private String ffetchadd;/* 交货地点 */

	private int fselTranType;/* 源单类型 */

	private int fchildren;/* 关联标识 */

	private int fbrId;/* 制单机构 */

	private int fareaps;/* 销售范围 */

	private int fmanageType;/* 保税监管类型 */

	private int fexchangerateType;/* 汇率类型 */

	private int fprintCount; /* 打印次数 */

	private int deliveryWay;	// 发货方式( 发货运:990664），送货:990665 )

	private Department_App dept;
	private Emp emp;
	private Customer_App cust;

	/* 临时字段,不存表 */
	
	public SeOutStock_App() {
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

	public int getFtranType() {
		return ftranType;
	}

	public void setFtranType(int ftranType) {
		this.ftranType = ftranType;
	}

	public int getFsalType() {
		return fsalType;
	}

	public void setFsalType(int fsalType) {
		this.fsalType = fsalType;
	}

	public int getFcustId() {
		return fcustId;
	}

	public void setFcustId(int fcustId) {
		this.fcustId = fcustId;
	}

	public String getFdate() {
		return fdate;
	}

	public void setFdate(String fdate) {
		this.fdate = fdate;
	}

	public int getFstockId() {
		return fstockId;
	}

	public void setFstockId(int fstockId) {
		this.fstockId = fstockId;
	}

	public String getFadd() {
		return fadd;
	}

	public void setFadd(String fadd) {
		this.fadd = fadd;
	}

	public String getFnote() {
		return fnote;
	}

	public void setFnote(String fnote) {
		this.fnote = fnote;
	}

	public int getFempId() {
		return fempId;
	}

	public void setFempId(int fempId) {
		this.fempId = fempId;
	}

	public int getFcheckerId() {
		return fcheckerId;
	}

	public void setFcheckerId(int fcheckerId) {
		this.fcheckerId = fcheckerId;
	}

	public int getFbillerId() {
		return fbillerId;
	}

	public void setFbillerId(int fbillerId) {
		this.fbillerId = fbillerId;
	}

	public int getFmanagerId() {
		return fmanagerId;
	}

	public void setFmanagerId(int fmanagerId) {
		this.fmanagerId = fmanagerId;
	}

	public int getFclosed() {
		return fclosed;
	}

	public void setFclosed(int fclosed) {
		this.fclosed = fclosed;
	}

	public int getFinvoiceClosed() {
		return finvoiceClosed;
	}

	public void setFinvoiceClosed(int finvoiceClosed) {
		this.finvoiceClosed = finvoiceClosed;
	}

	public int getFdeptId() {
		return fdeptId;
	}

	public void setFdeptId(int fdeptId) {
		this.fdeptId = fdeptId;
	}

	public int getFsettleId() {
		return fsettleId;
	}

	public void setFsettleId(int fsettleId) {
		this.fsettleId = fsettleId;
	}

	public int getFtranStatus() {
		return ftranStatus;
	}

	public void setFtranStatus(int ftranStatus) {
		this.ftranStatus = ftranStatus;
	}

	public Double getFexchangerate() {
		return fexchangerate;
	}

	public void setFexchangerate(Double fexchangerate) {
		this.fexchangerate = fexchangerate;
	}

	public int getFcurrencyId() {
		return fcurrencyId;
	}

	public void setFcurrencyId(int fcurrencyId) {
		this.fcurrencyId = fcurrencyId;
	}

	public int getFstatus() {
		return fstatus;
	}

	public void setFstatus(int fstatus) {
		this.fstatus = fstatus;
	}

	public int getFcancellation() {
		return fcancellation;
	}

	public void setFcancellation(int fcancellation) {
		this.fcancellation = fcancellation;
	}

	public int getFcurcheckLevel() {
		return fcurcheckLevel;
	}

	public void setFcurcheckLevel(int fcurcheckLevel) {
		this.fcurcheckLevel = fcurcheckLevel;
	}

	public int getFrelatebrId() {
		return frelatebrId;
	}

	public void setFrelatebrId(int frelatebrId) {
		this.frelatebrId = frelatebrId;
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

	public int getFseltranType() {
		return fselTranType;
	}

	public void setFseltranType(int fselTranType) {
		this.fselTranType = fselTranType;
	}

	public int getFchildren() {
		return fchildren;
	}

	public void setFchildren(int fchildren) {
		this.fchildren = fchildren;
	}

	public int getFbrId() {
		return fbrId;
	}

	public void setFbrId(int fbrId) {
		this.fbrId = fbrId;
	}

	public int getFareaps() {
		return fareaps;
	}

	public void setFareaps(int fareaps) {
		this.fareaps = fareaps;
	}

	public int getFmanageType() {
		return fmanageType;
	}

	public void setFmanageType(int fmanageType) {
		this.fmanageType = fmanageType;
	}

	public int getFexchangerateType() {
		return fexchangerateType;
	}

	public void setFexchangerateType(int fexchangerateType) {
		this.fexchangerateType = fexchangerateType;
	}

	public int getFprintCount() {
		return fprintCount;
	}

	public void setFprintCount(int fprintCount) {
		this.fprintCount = fprintCount;
	}

	public int getDeliveryWay() {
		return deliveryWay;
	}

	public void setDeliveryWay(int deliveryWay) {
		this.deliveryWay = deliveryWay;
	}

	public Department_App getDept() {
		return dept;
	}

	public void setDept(Department_App dept) {
		this.dept = dept;
	}

	public Emp getEmp() {
		return emp;
	}

	public void setEmp(Emp emp) {
		this.emp = emp;
	}

	public Customer_App getCust() {
		return cust;
	}

	public void setCust(Customer_App cust) {
		this.cust = cust;
	}
	
	

}
	