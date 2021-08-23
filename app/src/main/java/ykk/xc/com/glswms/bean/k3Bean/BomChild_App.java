package ykk.xc.com.glswms.bean.k3Bean;

import java.io.Serializable;

/**
 *  Bom子表 ( ICBOMCHILD )
 */
public class BomChild_App implements Serializable {
	private static final long serialVersionUID = 1L;

	private int finterId;		// BOM主表id
	private int fentryId;		// 分录id
	private int fitemId;		// 物料id
	private double fqty;		// 用料数量

	// 临时字段，不存表
	private double ptQty;		// SCRK_ScanRecordEntry_App表中的( fqty )字段
	private double smSumQty;	// SCRK_ScanRecordEntry_App表中的( smSumQty )字段
	private double remainQty;	// SCRK_ScanRecordEntry_App表中的( remainQty )字段
	private int scrkScanRecordEntryId; // SCRK_ScanRecordEntry_App表中的( id )字段
	
	private Bom_App bom;
	private ICItem_App icItem;
	
	public BomChild_App() {
		super();
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

	public Bom_App getBom() {
		return bom;
	}

	public void setBom(Bom_App bom) {
		this.bom = bom;
	}

	public ICItem_App getIcItem() {
		return icItem;
	}

	public void setIcItem(ICItem_App icItem) {
		this.icItem = icItem;
	}

	public double getPtQty() {
		return ptQty;
	}

	public void setPtQty(double ptQty) {
		this.ptQty = ptQty;
	}

	public double getSmSumQty() {
		return smSumQty;
	}

	public void setSmSumQty(double smSumQty) {
		this.smSumQty = smSumQty;
	}

	public double getRemainQty() {
		return remainQty;
	}

	public void setRemainQty(double remainQty) {
		this.remainQty = remainQty;
	}

	public int getScrkScanRecordEntryId() {
		return scrkScanRecordEntryId;
	}

	public void setScrkScanRecordEntryId(int scrkScanRecordEntryId) {
		this.scrkScanRecordEntryId = scrkScanRecordEntryId;
	}

}