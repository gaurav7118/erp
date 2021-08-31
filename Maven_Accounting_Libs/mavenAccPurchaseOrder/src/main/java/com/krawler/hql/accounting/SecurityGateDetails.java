/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class SecurityGateDetails {
    
    private String ID;
    private int srno;
    private SecurityGateEntry securityGateEntry;
    private Product product;
    private double quantity;
    private UnitOfMeasure uom;
    private double baseuomquantity;
    private double baseuomrate;
    private double rate;
    private double rateincludegst;
    private String remark;
    private PurchaseOrderDetail podetail;
    private Company company;
    private String shelfLocation;
    private Tax tax;
    private double discount;
    private int discountispercent;
    
    private double rowTaxAmount;
    private SecurityGateDetailsCustomData sgeDetailCustomData;
    private double rowTermAmount;
     private double OtherTermNonTaxableAmount;
    private String description;
    
    private String supplierpartnumber;
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
    private double lineLevelTermAmount;
     
    public SecurityGateDetailsCustomData getSgeDetailCustomData() {
        return sgeDetailCustomData;
    }

    public void setSgeDetailCustomData(SecurityGateDetailsCustomData sgeDetailCustomData) {
        this.sgeDetailCustomData = sgeDetailCustomData;
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

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }

    public String getSupplierpartnumber() {
        return supplierpartnumber;
    }

    public void setSupplierpartnumber(String supplierpartnumber) {
        this.supplierpartnumber = supplierpartnumber;
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


    public double getLineLevelTermAmount() {
        return lineLevelTermAmount;
    }

    public void setLineLevelTermAmount(double lineLevelTermAmount) {
        this.lineLevelTermAmount = lineLevelTermAmount;
    }

    /**
     * @return the securityGateEntry
     */
    public SecurityGateEntry getSecurityGateEntry() {
        return securityGateEntry;
    }

    /**
     * @param securityGateEntry the securityGateEntry to set
     */
    public void setSecurityGateEntry(SecurityGateEntry securityGateEntry) {
        this.securityGateEntry = securityGateEntry;
    }

    /**
     * @return the podetail
     */
    public PurchaseOrderDetail getPodetail() {
        return podetail;
    }

    /**
     * @param podetail the podetail to set
     */
    public void setPodetail(PurchaseOrderDetail podetail) {
        this.podetail = podetail;
    }
}
