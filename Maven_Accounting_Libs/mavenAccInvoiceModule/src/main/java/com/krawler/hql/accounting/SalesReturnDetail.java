/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ProductBatch;

/**
 *
 * @author krawler
 */
public class SalesReturnDetail {

    private String ID;
    private int srno;
    private Product product;
    private double actualQuantity;
    private double returnQuantity;
    private UnitOfMeasure uom;
    private double baseuomquantity;
    private double baseuomreturnquantity;
    private double baseuomrate;
    private String description;
    private String remark;
    private Company company;
    private SalesReturn salesReturn;
    private DeliveryOrderDetail dodetails;
    private InvoiceDetail cidetails;
    private Inventory inventory;
    private String partno;
    SalesReturnDetailCustomData salesReturnDetailCustomData;
    SalesReturnDetailProductCustomData salesReturnDetailProductCustomData;
    private String invstoreid;
    private String invlocid;
    private ProductBatch batch;
    private MasterItem reason;
    private Tax tax;
    private double rateincludegst; // If Transcation including GST save Unit price including GST
    private double rowTaxAmount;
    private double discount;
    private int discountispercent;
    private double rate;
    private double previousIssueCount;   //previous count value column to store the value of the reusable count
    private String priceSource;
    private double rowtermamount;
    private double OtherTermNonTaxableAmount;
    private JournalEntryDetail inventoryJEdetail;
    private JournalEntryDetail costOfGoodsSoldJEdetail;
    private String discountJson;                                                //Used to store json of discount masters applied in Price band screen for each row ERM-68;
    private String pricingBandMasterid;
    private boolean isUserModifiedTaxAmount;//ERM-1085 - To identify row tax amount is user modified or system calculated according to adaptive rounding algo.

    public String getDiscountJson() {
        return discountJson;
    }

    public void setDiscountJson(String discountJson) {
        this.discountJson = discountJson;
    }
    
    public double getRowtermamount() {
        return rowtermamount;
    }

    public void setRowtermamount(double rowtermamount) {
        this.rowtermamount = rowtermamount;
    }
    public double getOtherTermNonTaxableAmount() {
        return OtherTermNonTaxableAmount;
    }

    public void setOtherTermNonTaxableAmount(double OtherTermNonTaxableAmount) {
        this.OtherTermNonTaxableAmount = OtherTermNonTaxableAmount;
    }

    public double getPreviousIssueCount() {
        return previousIssueCount;
    }

    public void setPreviousIssueCount(double previousIssueCount) {
        this.previousIssueCount = previousIssueCount;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(double actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DeliveryOrderDetail getDodetails() {
        return dodetails;
    }

    public void setDodetails(DeliveryOrderDetail dodetails) {
        this.dodetails = dodetails;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public String getPartno() {
        return partno;
    }

    public void setPartno(String partno) {
        this.partno = partno;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public double getReturnQuantity() {
        return returnQuantity;
    }

    public void setReturnQuantity(double returnQuantity) {
        this.returnQuantity = returnQuantity;
    }

    public SalesReturn getSalesReturn() {
        return salesReturn;
    }

    public void setSalesReturn(SalesReturn salesReturn) {
        this.salesReturn = salesReturn;
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

    public double getBaseuomreturnquantity() {
        return baseuomreturnquantity;
    }

    public void setBaseuomreturnquantity(double baseuomreturnquantity) {
        this.baseuomreturnquantity = baseuomreturnquantity;
    }

    public double getBaseuomrate() {
        return baseuomrate;
    }

    public void setBaseuomrate(double baseuomrate) {
        this.baseuomrate = baseuomrate;
    }

    public InvoiceDetail getCidetails() {
        return cidetails;
    }

    public void setCidetails(InvoiceDetail cidetails) {
        this.cidetails = cidetails;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public SalesReturnDetailCustomData getSalesReturnDetailCustomData() {
        return salesReturnDetailCustomData;
    }

    public void setSalesReturnDetailCustomData(SalesReturnDetailCustomData salesReturnDetailCustomData) {
        this.salesReturnDetailCustomData = salesReturnDetailCustomData;
    }

    public SalesReturnDetailProductCustomData getSalesReturnDetailProductCustomData() {
        return salesReturnDetailProductCustomData;
    }

    public void setSalesReturnDetailProductCustomData(SalesReturnDetailProductCustomData salesReturnDetailProductCustomData) {
        this.salesReturnDetailProductCustomData = salesReturnDetailProductCustomData;
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

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public MasterItem getReason() {
        return reason;
    }

    public void setReason(MasterItem reason) {
        this.reason = reason;
    }

    public double getRateincludegst() {
        return rateincludegst;
    }

    public void setRateincludegst(double rateincludegst) {
        this.rateincludegst = rateincludegst;
    }
    
    public double getRowTaxAmount() {
        return rowTaxAmount;
    }

    public void setRowTaxAmount(double rowTaxAmount) {
        this.rowTaxAmount = rowTaxAmount;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
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

    public ProductBatch getBatch() {
        return batch;
    }

    public void setBatch(ProductBatch batch) {
        this.batch = batch;
    }

    public String getPriceSource() {
        return priceSource;
    }

    public void setPriceSource(String priceSource) {
        this.priceSource = priceSource;
    }

    public JournalEntryDetail getInventoryJEdetail() {
        return inventoryJEdetail;
    }

    public void setInventoryJEdetail(JournalEntryDetail inventoryJEdetail) {
        this.inventoryJEdetail = inventoryJEdetail;
    }

    public JournalEntryDetail getCostOfGoodsSoldJEdetail() {
        return costOfGoodsSoldJEdetail;
    }

    public void setCostOfGoodsSoldJEdetail(JournalEntryDetail costOfGoodsSoldJEdetail) {
        this.costOfGoodsSoldJEdetail = costOfGoodsSoldJEdetail;
    }

    public String getPricingBandMasterid() {
        return pricingBandMasterid;
    }

    public void setPricingBandMasterid(String pricingBandMasterid) {
        this.pricingBandMasterid = pricingBandMasterid;
    }
    
    public boolean isIsUserModifiedTaxAmount() {
        return isUserModifiedTaxAmount;
    }

    public void setIsUserModifiedTaxAmount(boolean isUserModifiedTaxAmount) {
        this.isUserModifiedTaxAmount = isUserModifiedTaxAmount;
    }
    
}
