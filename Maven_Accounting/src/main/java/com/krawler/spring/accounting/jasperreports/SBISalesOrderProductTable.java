/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class SBISalesOrderProductTable {
                String sNo = "";
		String partNo = "";
		String desc = "";
		String qty = "";
		String unitSRP = "";
		String totalSRP = "";
		String unitCost = "";
		String totalCost = "";
		String vendor = "";
		String poNo = "";
		String doNo = "";
		String invoiceNo = "";
		String remarks = "";
                String margin = "";
                String marginPercent = "";
                String exchangerate = "";

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDoNo() {
        return doNo;
    }

    public void setDoNo(String doNo) {
        this.doNo = doNo;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getPoNo() {
        return poNo;
    }

    public void setPoNo(String poNo) {
        this.poNo = poNo;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getsNo() {
        return sNo;
    }

    public void setsNo(String sNo) {
        this.sNo = sNo;
    }

    public String getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }

    public String getTotalSRP() {
        return totalSRP;
    }

    public void setTotalSRP(String totalSRP) {
        this.totalSRP = totalSRP;
    }

    public String getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(String unitCost) {
        this.unitCost = unitCost;
    }

    public String getUnitSRP() {
        return unitSRP;
    }

    public void setUnitSRP(String unitSRP) {
        this.unitSRP = unitSRP;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getMargin() {
        return margin;
    }
    public void setMargin(String margin) {
        this.margin = margin;
    }
    public String getMarginPercent() {
        return marginPercent;
    }
    public void setMarginPercent(String marginPercent) {
        this.marginPercent = marginPercent;
    }
     public String getExchangerate() {
        return exchangerate;
    }
    public void setExchangerate(String exchangerate) {
        this.exchangerate = exchangerate;
    }
}
