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
public class SupplierExciseDetails {
    
    private String id;
    private String goodsReceiptDetailsId;
    private InvoiceDetail invoicedetails;
    private int utilizedQuantity;
    private int actualQuantity;
    private Vendor vendor;
    private MasterItem natureOfPurchase;
    private Company company;

    public int getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(int actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getGoodsReceiptDetailsId() {
        return goodsReceiptDetailsId;
    }

    public void setGoodsReceiptDetailsId(String goodsReceiptDetailsId) {
        this.goodsReceiptDetailsId = goodsReceiptDetailsId;
    }

    public InvoiceDetail getInvoicedetails() {
        return invoicedetails;
    }

    public void setInvoicedetails(InvoiceDetail invoicedetails) {
        this.invoicedetails = invoicedetails;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MasterItem getNatureOfPurchase() {
        return natureOfPurchase;
    }

    public void setNatureOfPurchase(MasterItem natureOfPurchase) {
        this.natureOfPurchase = natureOfPurchase;
    }

    public int getUtilizedQuantity() {
        return utilizedQuantity;
    }

    public void setUtilizedQuantity(int utilizedQuantity) {
        this.utilizedQuantity = utilizedQuantity;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }
    
    
}
