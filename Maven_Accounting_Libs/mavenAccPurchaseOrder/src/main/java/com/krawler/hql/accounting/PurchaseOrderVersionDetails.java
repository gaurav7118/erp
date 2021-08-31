/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler-user
 */
public class PurchaseOrderVersionDetails {

    private String ID;
    private int srno;
    private PurchaseOrderVersion purchaseOrderVersion;
    private Product product;
    private double quantity;
    private UnitOfMeasure uom;
    private double baseuomquantity;
    private double baseuomrate;
    private double rate;
    private double rateincludegst;
    private String remark;
    private Company company;
    private String shelfLocation;
    private Tax tax;
    private double discount;
    private int discountispercent;
    private String salesorderdetailid;
    private VendorQuotationDetail vqdetail;
    private String purchaseRequisitionDetailId;
    private PurchaseOrderVersionDetailsCustomData poVersionDetailCustomData;
    private double rowTaxAmount;
    private double rowTermAmount;
    private double OtherTermNonTaxableAmount;
    private String description;
    private int qastatus; // 0 = 0 means item approved which is by default, 1 = 1 means item is pending for QA approval, 2 = 2 means item is rejected by QA.  
    private String qastatusremark;
    private String supplierpartnumber;
    private String dependentType;
    private String inouttime;
    private String showquantity;
    private String invstoreid;
    private String invlocid;
    private String priceSource;
    private double balanceqty;   //this is used to identify renmaning quantity after purchase return linked with GR order which is created by linking PO 
    private String permit;
    private boolean isLineItemClosed;//flag for identifying whether podetail is Closed manually or not value->'F' means not closed
    private  double mrpIndia;
    private  String exciseValuationType;
    private  String vatValuationType;
    private  UnitOfMeasure reportingUOMExcise;
    private  UnitOfMeasure reportingUOMVAT;
    private  UOMschemaType reportingSchemaTypeExcise;
    private  UOMschemaType reportingSchemaVAT;
    private String workorderdetailid;
    private double lineLevelTermAmount;
    private BOMDetail bomcode;

    public BOMDetail getBomcode() {
        return bomcode;
    }

    public void setBomcode(BOMDetail bomcode) {
        this.bomcode = bomcode;
    }
    public boolean isIsLineItemClosed() {
        return isLineItemClosed;
    }

    public void setIsLineItemClosed(boolean isLineItemClosed) {
        this.isLineItemClosed = isLineItemClosed;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public PurchaseOrderVersion getPurchaseOrderVersion() {
        return purchaseOrderVersion;
    }

    public void setPurchaseOrderVersion(PurchaseOrderVersion purchaseOrderVersion) {
        this.purchaseOrderVersion = purchaseOrderVersion;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getRateincludegst() {
        return rateincludegst;
    }

    public void setRateincludegst(double rateincludegst) {
        this.rateincludegst = rateincludegst;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public UnitOfMeasure getUom() {
        return uom;
    }

    public void setUom(UnitOfMeasure uom) {
        this.uom = uom;
    }

    public double getBaseuomquantity() {
        return baseuomquantity;
    }

    public void setBaseuomquantity(double baseuomquantity) {
        this.baseuomquantity = baseuomquantity;
    }

    public double getBaseuomrate() {
        return baseuomrate;
    }

    public void setBaseuomrate(double baseuomrate) {
        this.baseuomrate = baseuomrate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public int getDiscountispercent() {
        return discountispercent;
    }

    public void setDiscountispercent(int discountispercent) {
        this.discountispercent = discountispercent;
    }

    public PurchaseOrderVersionDetailsCustomData getPoVersionDetailCustomData() {
        return poVersionDetailCustomData;
    }

    public void setPoVersionDetailCustomData(PurchaseOrderVersionDetailsCustomData poVersionDetailCustomData) {
        this.poVersionDetailCustomData = poVersionDetailCustomData;
    }

    public String getSalesorderdetailid() {
        return salesorderdetailid;
    }

    public void setSalesorderdetailid(String salesorderdetailid) {
        this.salesorderdetailid = salesorderdetailid;
    }

    public double getRowTaxAmount() {
        return rowTaxAmount;
    }

    public void setRowTaxAmount(double rowTaxAmount) {
        this.rowTaxAmount = rowTaxAmount;
    }

    public double getRowTermAmount() {
        return rowTermAmount;
    }
    public void setRowTermAmount(double rowTermAmount) {
        this.rowTermAmount = rowTermAmount;
    }
    public double getOtherTermNonTaxableAmount() {
        return OtherTermNonTaxableAmount;
    }

    public void setOtherTermNonTaxableAmount(double OtherTermNonTaxableAmount) {
        this.OtherTermNonTaxableAmount = OtherTermNonTaxableAmount;
    }
    public VendorQuotationDetail getVqdetail() {
        return vqdetail;
    }

    public void setVqdetail(VendorQuotationDetail vqdetail) {
        this.vqdetail = vqdetail;
    }

    public int getQastatus() {
        return qastatus;
    }

    public void setQastatus(int qastatus) {
        this.qastatus = qastatus;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }

    public String getQastatusremark() {
        return qastatusremark;
    }

    public void setQastatusremark(String qastatusremark) {
        this.qastatusremark = qastatusremark;
    }

    public String getSupplierpartnumber() {
        return supplierpartnumber;
    }

    public void setSupplierpartnumber(String supplierpartnumber) {
        this.supplierpartnumber = supplierpartnumber;
    }

    public String getDependentType() {
        return dependentType;
    }

    public void setDependentType(String dependentType) {
        this.dependentType = dependentType;
    }

    public String getInouttime() {
        return inouttime;
    }

    public void setInouttime(String inouttime) {
        this.inouttime = inouttime;
    }

    public String getShowquantity() {
        return showquantity;
    }

    public void setShowquantity(String showquantity) {
        this.showquantity = showquantity;
    }
    public String getInvstoreid() {
        return invstoreid;
    }

    public void setInvstoreid(String invstoreid) {
        this.invstoreid = invstoreid;
    }

    public String getInvlocid() {
        return invlocid;
    }

    public void setInvlocid(String invlocid) {
        this.invlocid = invlocid;
    }

    public String getPriceSource() {
        return priceSource;
    }

    public void setPriceSource(String priceSource) {
        this.priceSource = priceSource;
    }

    public double getBalanceqty() {
        return balanceqty;
    }

    public void setBalanceqty(double balanceqty) {
        this.balanceqty = balanceqty;
    }
        
    public String getPermit() {
        return permit;
    }

    public void setPermit(String permit) {
        this.permit = permit;
    }  
    public String getPurchaseRequisitionDetailId() {
        return purchaseRequisitionDetailId;
    }

    public void setPurchaseRequisitionDetailId(String purchaseRequisitionDetailId) {
        this.purchaseRequisitionDetailId = purchaseRequisitionDetailId;
    }

    public String getExciseValuationType() {
        return exciseValuationType;
    }

    public void setExciseValuationType(String exciseValuationType) {
        this.exciseValuationType = exciseValuationType;
    }

    public double getMrpIndia() {
        return mrpIndia;
    }

    public void setMrpIndia(double mrpIndia) {
        this.mrpIndia = mrpIndia;
    }

    public UOMschemaType getReportingSchemaTypeExcise() {
        return reportingSchemaTypeExcise;
    }

    public void setReportingSchemaTypeExcise(UOMschemaType reportingSchemaTypeExcise) {
        this.reportingSchemaTypeExcise = reportingSchemaTypeExcise;
    }

    public UOMschemaType getReportingSchemaVAT() {
        return reportingSchemaVAT;
    }

    public void setReportingSchemaVAT(UOMschemaType reportingSchemaVAT) {
        this.reportingSchemaVAT = reportingSchemaVAT;
    }

    public UnitOfMeasure getReportingUOMExcise() {
        return reportingUOMExcise;
    }

    public void setReportingUOMExcise(UnitOfMeasure reportingUOMExcise) {
        this.reportingUOMExcise = reportingUOMExcise;
    }

    public UnitOfMeasure getReportingUOMVAT() {
        return reportingUOMVAT;
    }

    public void setReportingUOMVAT(UnitOfMeasure reportingUOMVAT) {
        this.reportingUOMVAT = reportingUOMVAT;
    }

    public String getVatValuationType() {
        return vatValuationType;
    }

    public void setVatValuationType(String vatValuationType) {
        this.vatValuationType = vatValuationType;
    }

    public String getWorkorderdetailid() {
        return workorderdetailid;
    }

    public void setWorkorderdetailid(String workorderdetailid) {
        this.workorderdetailid = workorderdetailid;
    }

    public double getLineLevelTermAmount() {
        return lineLevelTermAmount;
    }

    public void setLineLevelTermAmount(double lineLevelTermAmount) {
        this.lineLevelTermAmount = lineLevelTermAmount;
    }
}
