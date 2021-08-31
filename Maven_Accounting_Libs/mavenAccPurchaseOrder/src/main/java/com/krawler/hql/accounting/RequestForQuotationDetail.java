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
public class RequestForQuotationDetail {

    private String ID;
    private int srno;
    private RequestForQuotation requestforquotation;
    private Product product;
    private double quantity;
    private UnitOfMeasure uom;
    private double baseuomquantity;
    private double baseuomrate;
    private double rate;
    private String remark;
    private PurchaseRequisition prid;
    private Company company;
    RequestForQuotationDetailCustomData requestForQuotationDetailCustomData;
    private String pricingBandMasterid;
    private String priceSource;
//    private Tax tax;
//    private double discount;
//    private int discountispercent;

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

    public RequestForQuotation getRequestforquotation() {
        return requestforquotation;
    }

    public void setRequestforquotation(RequestForQuotation requestforquotation) {
        this.requestforquotation = requestforquotation;
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

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public PurchaseRequisition getPrid() {
        return prid;
    }

    public void setPrid(PurchaseRequisition prid) {
        this.prid = prid;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public RequestForQuotationDetailCustomData getRequestForQuotationDetailCustomData() {
        return requestForQuotationDetailCustomData;
    }

    public void setRequestForQuotationDetailCustomData(RequestForQuotationDetailCustomData requestForQuotationDetailCustomData) {
        this.requestForQuotationDetailCustomData = requestForQuotationDetailCustomData;
    }

    public String getPricingBandMasterid() {
        return pricingBandMasterid;
    }

    public void setPricingBandMasterid(String pricingBandMasterid) {
        this.pricingBandMasterid = pricingBandMasterid;
    }

    /**
     * @return the priceSource
     */
    public String getPriceSource() {
        return priceSource;
    }

    /**
     * @param priceSource the priceSource to set
     */
    public void setPriceSource(String priceSource) {
        this.priceSource = priceSource;
    }
}
