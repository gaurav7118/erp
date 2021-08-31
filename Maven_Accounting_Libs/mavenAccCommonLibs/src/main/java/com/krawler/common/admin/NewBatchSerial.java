/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

import java.util.Date;

/**
 *
 * @author krawler
 */
public class NewBatchSerial {

    private String id;
    private String serialname;
    private Date expfromdate;
    private Date exptodate;
    private double quantity;
    private double quantitydue;
    private NewProductBatch batch;
    private Company company;
    private String product;
    private boolean ispurchase;
    private boolean isopening;
    private int transactiontype;
    private boolean ispurchasereturn;
    private double lockquantity;
    private double consignquantity;
    private boolean isconsignment;
   private QaApprovalStatus qaApprovalstatus;
    private boolean isForconsignment;
    private String asset;
    private String skufield;
//    private RequestApprovalStatus requestApprovalStatus;//for Consignment Approval Flow
//    private User approver;//for Consignment Approval Flow
    private int wastageQuantityType; // 0 - 'Flat', 1 - 'Percentage'
    private double wastageQuantity;
    
    public String getSkufield() {
        return skufield;
    }

    public void setSkufield(String skufield) {
        this.skufield = skufield;
    }
    
    
    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public NewProductBatch getBatch() {
        return batch;
    }

    public void setBatch(NewProductBatch batch) {
        this.batch = batch;
    }

   public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getExpfromdate() {
        return expfromdate;
    }

    public void setExpfromdate(Date expfromdate) {
        this.expfromdate = expfromdate;
    }

    public Date getExptodate() {
        return exptodate;
    }

    public void setExptodate(Date exptodate) {
        this.exptodate = exptodate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isIsopening() {
        return isopening;
    }

    public void setIsopening(boolean isopening) {
        this.isopening = isopening;
    }

    public boolean isIspurchase() {
        return ispurchase;
    }

    public void setIspurchase(boolean ispurchase) {
        this.ispurchase = ispurchase;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getQuantitydue() {
        return quantitydue;
    }

    public void setQuantitydue(double quantitydue) {
        this.quantitydue = quantitydue;
    }

    public String getSerialname() {
        return serialname;
    }

    public void setSerialname(String serialname) {
        this.serialname = serialname;
    }

    public int getTransactiontype() {
        return transactiontype;
    }

    public void setTransactiontype(int transactiontype) {
        this.transactiontype = transactiontype;
    }

    public boolean isIspurchasereturn() {
        return ispurchasereturn;
    }

    public void setIspurchasereturn(boolean ispurchasereturn) {
        this.ispurchasereturn = ispurchasereturn;
    }

    public double getLockquantity() {
        return lockquantity;
    }

    public void setLockquantity(double lockquantity) {
        this.lockquantity = lockquantity;
    }

    public double getConsignquantity() {
        return consignquantity;
    }

    public void setConsignquantity(double consignquantity) {
        this.consignquantity = consignquantity;
    }

    public boolean isIsconsignment() {
        return isconsignment;
    }

    public void setIsconsignment(boolean isconsignment) {
        this.isconsignment = isconsignment;
    }

    public boolean isIsForconsignment() {
        return isForconsignment;
    }

    public void setIsForconsignment(boolean isForconsignment) {
        this.isForconsignment = isForconsignment;
    }

    public QaApprovalStatus getQaApprovalstatus() {
        return qaApprovalstatus;
    }

    public void setQaApprovalstatus(QaApprovalStatus qaApprovalstatus) {
        this.qaApprovalstatus = qaApprovalstatus;
    }

//    public RequestApprovalStatus getRequestApprovalStatus() {
//        return requestApprovalStatus;
//    }
//
//    public void setRequestApprovalStatus(RequestApprovalStatus requestApprovalStatus) {
//        this.requestApprovalStatus = requestApprovalStatus;
//    }
//
//    public User getApprover() {
//        return approver;
//    }
//
//    public void setApprover(User approver) {
//        this.approver = approver;
//    }

    public double getWastageQuantity() {
        return wastageQuantity;
    }

    public void setWastageQuantity(double wastageQuantity) {
        this.wastageQuantity = wastageQuantity;
    }

    public int getWastageQuantityType() {
        return wastageQuantityType;
    }

    public void setWastageQuantityType(int wastageQuantityType) {
        this.wastageQuantityType = wastageQuantityType;
    }
}

