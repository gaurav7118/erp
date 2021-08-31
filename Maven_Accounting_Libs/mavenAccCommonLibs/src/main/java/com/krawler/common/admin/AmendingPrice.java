/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class AmendingPrice {

    private String ID;
    private User UserID;
    private Company CompanyID;
    private boolean CInvoice;
    private boolean VInvoice;
    private boolean SalesOrder;
    private boolean PurchaseOrder;
    private boolean VendorQuotation;
    private boolean CustomerQuotation;
    private boolean BlockAmendingPrice;

    public boolean isBlockAmendingPrice() {
        return BlockAmendingPrice;
    }

    public void setBlockAmendingPrice(boolean BlockAmendingPrice) {
        this.BlockAmendingPrice = BlockAmendingPrice;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public boolean isCInvoice() {
        return CInvoice;
    }

    public void setCInvoice(boolean CInvoice) {
        this.CInvoice = CInvoice;
    }

    public Company getCompanyID() {
        return CompanyID;
    }

    public void setCompanyID(Company CompanyID) {
        this.CompanyID = CompanyID;
    }

    public boolean isCustomerQuotation() {
        return CustomerQuotation;
    }

    public void setCustomerQuotation(boolean CustomerQuotation) {
        this.CustomerQuotation = CustomerQuotation;
    }

    public boolean isPurchaseOrder() {
        return PurchaseOrder;
    }

    public void setPurchaseOrder(boolean PurchaseOrder) {
        this.PurchaseOrder = PurchaseOrder;
    }

    public boolean isSalesOrder() {
        return SalesOrder;
    }

    public void setSalesOrder(boolean SalesOrder) {
        this.SalesOrder = SalesOrder;
    }

    public User getUserID() {
        return UserID;
    }

    public void setUserID(User UserID) {
        this.UserID = UserID;
    }

    public boolean isVInvoice() {
        return VInvoice;
    }

    public void setVInvoice(boolean VInvoice) {
        this.VInvoice = VInvoice;
    }

    public boolean isVendorQuotation() {
        return VendorQuotation;
    }

    public void setVendorQuotation(boolean VendorQuotation) {
        this.VendorQuotation = VendorQuotation;
    }
}
