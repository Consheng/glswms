package ykk.xc.com.glswms.bean;

import java.io.Serializable;

import ykk.xc.com.glswms.bean.k3Bean.Customer_App;
import ykk.xc.com.glswms.bean.k3Bean.Department_App;
import ykk.xc.com.glswms.bean.k3Bean.Supplier_App;
import ykk.xc.com.glswms.comm.Comm;

/**
 * Wms 本地的出入库	主表
 * @author Administrator
 *
 */
public class ICStockBill_App implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private String pdaNo;				// 本地生产的流水号
	/* 单据类型( QTRK:其他入库，QTCK:其他出库，CGRK:采购入库，CGSHRK:采购收货入库，CGSHRK_BTOR：采购入库退货，
	 		  SCRK:生产入库，SCDB:生产调拨，SKCPDB:锁库成品调拨，SCCLDB:生产材料调拨，SCRKDB:生产入库调拨， SCLL:生产领料，ZYDB:自由调拨，
	 		  XSCK:销售出库，XSCK_BTOR:销售出库_红字，XSZX:销售装箱出库，DBD:调拨单，WWCK:委外出库，WWRK:委外入库，STRK：受托入库,STLL:受托领料
	 		  XSJH:销售拣货(不更新库存单据)，CKZJ:出库质检(不更新库存单据)，CGFH:仓管复核(不更新库存单据),PYRK:盘盈入库，PKCK：盘亏出库)
	 		  DS_XSCK:电商销售出库，DS_XSCK_BTOR:电商销售出库_红字
	*/
	private String billType;
	private char billStatus;			// 单据业务状态 (A：创建，B：审核，C：关闭)
	private String fdate;				// 操作日期
	private int ftranType;				// 单据类型
	private int frob;					// 红蓝字(1:蓝字，-1红字)
	private int fsupplyId;				// 供应商id
	private int fdeptId;				// 部门id
	private int fcustId;				// 客户id
	private int fempId;					// 业务员id
	private int fsmanagerId;			// 保管人id
	private int fmanagerId;				// 负责人id
	private int ffmanagerId;			// 验收人id
	private int fbillerId;				// 制单人id
	private int fselTranType;			// 源单类型

	private String yewuMan;				// 业务员
	private String baoguanMan;			// 保管人
	private String fuzheMan;			// 负责人
	private String yanshouMan;			// 验收人
	private int createUserId;			// 创建人id
	private String createDate;			// 创建日期
	private int isToK3;					// 是否提交到K3
	private String k3Number;			// k3返回的单号
	private String expressNo;			// 快递单号
	private String expressCompany;		// 快递公司
	private int childSize;				// 分录个数

	private Supplier_App supplier;			// 供应商对象
	private Department_App department;	// 部门对象
	private Customer_App cust;	// 客户对象

	// 临时字段，不存表
	private boolean showButton; 		// 是否显示操作按钮
	private String createUserName;		// 创建人
	private String strSourceNo; 		// 对应的源单单号
	
	public ICStockBill_App() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPdaNo() {
		return pdaNo;
	}

	public void setPdaNo(String pdaNo) {
		this.pdaNo = pdaNo;
	}

	public String getBillType() {
		return billType;
	}

	public void setBillType(String billType) {
		this.billType = billType;
	}

	public char getBillStatus() {
		return billStatus;
	}

	public void setBillStatus(char billStatus) {
		this.billStatus = billStatus;
	}

	public String getFdate() {
		return fdate;
	}

	public void setFdate(String fdate) {
		this.fdate = fdate;
	}

	public int getFtranType() {
		return ftranType;
	}

	public void setFtranType(int ftranType) {
		this.ftranType = ftranType;
	}

	public int getFrob() {
		return frob;
	}

	public void setFrob(int frob) {
		this.frob = frob;
	}

	public int getFsupplyId() {
		return fsupplyId;
	}

	public void setFsupplyId(int fsupplyId) {
		this.fsupplyId = fsupplyId;
	}

	public int getFdeptId() {
		return fdeptId;
	}

	public void setFdeptId(int fdeptId) {
		this.fdeptId = fdeptId;
	}

	public int getFcustId() {
		return fcustId;
	}

	public void setFcustId(int fcustId) {
		this.fcustId = fcustId;
	}

	public int getFempId() {
		return fempId;
	}

	public void setFempId(int fempId) {
		this.fempId = fempId;
	}

	public int getFsmanagerId() {
		return fsmanagerId;
	}

	public void setFsmanagerId(int fsmanagerId) {
		this.fsmanagerId = fsmanagerId;
	}

	public int getFmanagerId() {
		return fmanagerId;
	}

	public void setFmanagerId(int fmanagerId) {
		this.fmanagerId = fmanagerId;
	}

	public int getFfmanagerId() {
		return ffmanagerId;
	}

	public void setFfmanagerId(int ffmanagerId) {
		this.ffmanagerId = ffmanagerId;
	}

	public int getFbillerId() {
		return fbillerId;
	}

	public void setFbillerId(int fbillerId) {
		this.fbillerId = fbillerId;
	}

	public int getFselTranType() {
		return fselTranType;
	}

	public void setFselTranType(int fselTranType) {
		this.fselTranType = fselTranType;
	}

	public String getYewuMan() {
		return yewuMan;
	}

	public void setYewuMan(String yewuMan) {
		this.yewuMan = yewuMan;
	}

	public String getBaoguanMan() {
		return baoguanMan;
	}

	public void setBaoguanMan(String baoguanMan) {
		this.baoguanMan = baoguanMan;
	}

	public String getFuzheMan() {
		return fuzheMan;
	}

	public void setFuzheMan(String fuzheMan) {
		this.fuzheMan = fuzheMan;
	}

	public String getYanshouMan() {
		return yanshouMan;
	}

	public void setYanshouMan(String yanshouMan) {
		this.yanshouMan = yanshouMan;
	}

	public int getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(int createUserId) {
		this.createUserId = createUserId;
	}

	public String getCreateUserName() {
		return createUserName;
	}

	public void setCreateUserName(String createUserName) {
		this.createUserName = createUserName;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public int getIsToK3() {
		return isToK3;
	}

	public void setIsToK3(int isToK3) {
		this.isToK3 = isToK3;
	}

	public String getK3Number() {
		return k3Number;
	}

	public void setK3Number(String k3Number) {
		this.k3Number = k3Number;
	}

	public String getExpressNo() {
		return expressNo;
	}

	public void setExpressNo(String expressNo) {
		this.expressNo = expressNo;
	}

	public String getExpressCompany() {
		return expressCompany;
	}

	public void setExpressCompany(String expressCompany) {
		this.expressCompany = expressCompany;
	}

	public int getChildSize() {
		return childSize;
	}

	public void setChildSize(int childSize) {
		this.childSize = childSize;
	}

	public Supplier_App getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier_App supplier) {
		this.supplier = supplier;
	}

	public Department_App getDepartment() {
		return department;
	}

	public void setDepartment(Department_App department) {
		this.department = department;
	}

	public Customer_App getCust() {
		return cust;
	}

	public void setCust(Customer_App cust) {
		this.cust = cust;
	}

	public boolean isShowButton() {
		return showButton;
	}

	public void setShowButton(boolean showButton) {
		this.showButton = showButton;
	}

	public String getStrSourceNo() {
		// 存在大写的逗号（,）,且大于1
		if(Comm.isNULLS(strSourceNo).indexOf(",") > -1 && Comm.isNULLS(strSourceNo).length() > 0) {
			return strSourceNo.substring(0, strSourceNo.length()-1);
		}
		return strSourceNo;
	}

	public void setStrSourceNo(String strSourceNo) {
		this.strSourceNo = strSourceNo;
	}

	
}
