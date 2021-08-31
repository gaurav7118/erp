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
public class PurchaseRequisitionDetail {

    private String ID;
    private int srno;
    private PurchaseRequisition purchaserequisition;
    private Product product;
    private double quantity;
    private UnitOfMeasure uom;
    private double baseuomquantity;
    private double baseuomrate;
    private double rate;
    private String remark;
    private String productdescription;
    private String approverremark;
    private Company company;
    private Tax tax;
    private double discount;
    private int discountispercent;
    PurchaseRequisitionDetailCustomData purchaseRequisitionDetailCustomData;
    private String priceSource;
    private String workorderdetailid;
    private double balanceqty;//Balance Quantity will update if linking like PR->PO
    private String pricingBandMasterid;

    public double getBalanceqty() {
        return balanceqty;
    }

    public void setBalanceqty(double balanceqty) {
        this.balanceqty = balanceqty;
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

    public PurchaseRequisition getPurchaserequisition() {
        return purchaserequisition;
    }

    public void setPurchaserequisition(PurchaseRequisition purchaserequisition) {
        this.purchaserequisition = purchaserequisition;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
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

    public String getProductdescription() {
        return productdescription;
    }

    public void setProductdescription(String productdescription) {
        this.productdescription = productdescription;
    }

    public String getApproverremark() {
        return approverremark;
    }

    public void setApproverremark(String approverremark) {
        this.approverremark = approverremark;
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

    public PurchaseRequisitionDetailCustomData getPurchaseRequisitionDetailCustomData() {
        return purchaseRequisitionDetailCustomData;
    }

    public void setPurchaseRequisitionDetailCustomData(PurchaseRequisitionDetailCustomData purchaseRequisitionDetailCustomData) {
        this.purchaseRequisitionDetailCustomData = purchaseRequisitionDetailCustomData;
    }

    public String getPriceSource() {
        return priceSource;
    }

    public void setPriceSource(String priceSource) {
        this.priceSource = priceSource;
    }

    public String getWorkorderdetailid() {
        return workorderdetailid;
    }

    public void setWorkorderdetailid(String workorderdetailid) {
        this.workorderdetailid = workorderdetailid;
    }

    public String getPricingBandMasterid() {
        return pricingBandMasterid;
    }

    public void setPricingBandMasterid(String pricingBandMasterid) {
        this.pricingBandMasterid = pricingBandMasterid;
    }
}

