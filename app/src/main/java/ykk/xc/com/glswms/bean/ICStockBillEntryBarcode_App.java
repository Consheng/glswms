package ykk.xc.com.glswms.bean;

import java.io.Serializable;

/**
 * Wms 本地的出入库	记录条码和批次号
 * @author Administrator
 *
 */
public class ICStockBillEntryBarcode_App implements Serializable {
	private static final long serialVersionUID = 1L;

	/*id*/
	private int id;
	private int parentId;
	private String barcode; // 条码号
	private String batchCode; // 批次号
	private String snCode; // 序列号
	private double fqty; // 使用数
	private char isUniqueness; // 条码是否唯一：Y是，N否
	private int againUse; // 是否再次使用，场景，当外购入库保存为0，仓库收料操作为1，一个条码在外购入库用完还可以在仓库收料操作页面扫码，之后就不能扫
	private String createDate; // 创建日期
	private String createUserName; // 创建人名称
	private String billType; // 主表的单据类型
	
	public ICStockBillEntryBarcode_App() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getBatchCode() {
		return batchCode;
	}

	public void setBatchCode(String batchCode) {
		this.batchCode = batchCode;
	}

	public String getSnCode() {
		return snCode;
	}

	public void setSnCode(String snCode) {
		this.snCode = snCode;
	}

	public double getFqty() {
		return fqty;
	}

	public void setFqty(double fqty) {
		this.fqty = fqty;
	}

	public char getIsUniqueness() {
		return isUniqueness;
	}

	public void setIsUniqueness(char isUniqueness) {
		this.isUniqueness = isUniqueness;
	}

	public int getAgainUse() {
		return againUse;
	}

	public void setAgainUse(int againUse) {
		this.againUse = againUse;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getCreateUserName() {
		return createUserName;
	}

	public void setCreateUserName(String createUserName) {
		this.createUserName = createUserName;
	}

	public String getBillType() {
		return billType;
	}

	public void setBillType(String billType) {
		this.billType = billType;
	}

	

	
}
