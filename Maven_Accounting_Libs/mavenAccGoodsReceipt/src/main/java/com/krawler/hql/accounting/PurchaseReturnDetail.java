/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ProductBatch;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.Product;

/**
 *
 * @author krawler
 */
public class PurchaseReturnDetail {

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
    private PurchaseReturn purchaseReturn;
    private GoodsReceiptDetail videtails;
    private GoodsReceiptOrderDetails grdetails;
    private Inventory inventory;
    private String partno;
    PurchaseReturnDetailCustomDate purchaseReturnDetailCustomDate;
    PurchaseReturnDetailProductCustomData purchaseReturnDetailProductCustomData;
    private String invstoreid;
    private ProductBatch batch;
    private String invlocid;
    private MasterItem reason;
    private double rateincludegst; // If Transcation including GST save Unit price including GST
    private Tax tax;
    private double rowTaxAmount;
    private double discount;
    private int discountispercent;
    private double rate;
    private String priceSource;
    private double rowtermamount;
    private double OtherTermNonTaxableAmount;
    private JournalEntryDetail inventoryJEdetail;
    private JournalEntryDetail purchasesJEDetail;
    private double tdsAssessableAmount;
    private MasterItem natureOfPayment;
    private int tdsRuleId;
    private double tdsRate;
    private double tdsLineAmount;
    private Account tdsPayableAccount;// Used for INDIA country
    private String pricingBandMasterid;
    private boolean isUserModifiedTaxAmount;//ERM-1085 - To identify row tax amount is user modified or system calculated according to adaptive rounding algo.
    
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

    public PurchaseReturn getPurchaseReturn() {
        return purchaseReturn;
    }

    public void setPurchaseReturn(PurchaseReturn purchaseReturn) {
        this.purchaseReturn = purchaseReturn;
    }

    public GoodsReceiptOrderDetails getGrdetails() {
        return grdetails;
    }

    public void setGrdetails(GoodsReceiptOrderDetails grdetails) {
        this.grdetails = grdetails;
    }

    public ProductBatch getBatch() {
        return batch;
    }

    public void setBatch(ProductBatch batch) {
        this.batch = batch;
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

    public GoodsReceiptDetail getVidetails() {
        return videtails;
    }

    public void setVidetails(GoodsReceiptDetail videtails) {
        this.videtails = videtails;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public PurchaseReturnDetailCustomDate getPurchaseReturnDetailCustomDate() {
        return purchaseReturnDetailCustomDate;
    }

    public void setPurchaseReturnDetailCustomDate(PurchaseReturnDetailCustomDate purchaseReturnDetailCustomDate) {
        this.purchaseReturnDetailCustomDate = purchaseReturnDetailCustomDate;
    }

    public PurchaseReturnDetailProductCustomData getPurchaseReturnDetailProductCustomData() {
        return purchaseReturnDetailProductCustomData;
    }

    public void setPurchaseReturnDetailProductCustomData(PurchaseReturnDetailProductCustomData purchaseReturnDetailProductCustomData) {
        this.purchaseReturnDetailProductCustomData = purchaseReturnDetailProductCustomData;
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

    public MasterItem getReason() {
        return reason;
    }

    public void setReason(MasterItem reason) {
        this.reason = reason;
    }

    public double getRowTaxAmount() {
        return rowTaxAmount;
    }

    public void setRowTaxAmount(double rowTaxAmount) {
        this.rowTaxAmount = rowTaxAmount;
    }

    public double getRateincludegst() {
        return rateincludegst;
    }

    public void setRateincludegst(double rateincludegst) {
        this.rateincludegst = rateincludegst;
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

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
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

    public JournalEntryDetail getPurchasesJEDetail() {
        return purchasesJEDetail;
    }

    public void setPurchasesJEDetail(JournalEntryDetail purchasesJEDetail) {
        this.purchasesJEDetail = purchasesJEDetail;
    }

    public double getTdsAssessableAmount() {
        return tdsAssessableAmount;
    }

    public void setTdsAssessableAmount(double tdsAssessableAmount) {
        this.tdsAssessableAmount = tdsAssessableAmount;
    }

    public MasterItem getNatureOfPayment() {
        return natureOfPayment;
    }

    public void setNatureOfPayment(MasterItem natureOfPayment) {
        this.natureOfPayment = natureOfPayment;
    }

    public int getTdsRuleId() {
        return tdsRuleId;
    }

    public void setTdsRuleId(int tdsRuleId) {
        this.tdsRuleId = tdsRuleId;
    }

    public double getTdsRate() {
        return tdsRate;
    }

    public void setTdsRate(double tdsRate) {
        this.tdsRate = tdsRate;
    }

    public double getTdsLineAmount() {
        return tdsLineAmount;
    }

    public void setTdsLineAmount(double tdsLineAmount) {
        this.tdsLineAmount = tdsLineAmount;
    }

    public Account getTdsPayableAccount() {
        return tdsPayableAccount;
    }

    public void setTdsPayableAccount(Account tdsPayableAccount) {
        this.tdsPayableAccount = tdsPayableAccount;
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
