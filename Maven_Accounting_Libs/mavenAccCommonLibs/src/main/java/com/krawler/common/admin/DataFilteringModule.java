/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class DataFilteringModule {

    private String id;
    private User user;
    private Company company;
    private boolean customerInvoice;
    private boolean salesOrder;
    private boolean customerQuotation;
    private boolean deliveryOrder;

    public boolean isDeliveryOrder() {
        return deliveryOrder;
    }

    public void setDeliveryOrder(boolean deliveryOrder) {
        this.deliveryOrder = deliveryOrder;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isCustomerInvoice() {
        return customerInvoice;
    }

    public void setCustomerInvoice(boolean customerInvoice) {
        this.customerInvoice = customerInvoice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isCustomerQuotation() {
        return customerQuotation;
    }

    public void setCustomerQuotation(boolean customerQuotation) {
        this.customerQuotation = customerQuotation;
    }

    public boolean isSalesOrder() {
        return salesOrder;
    }

    public void setSalesOrder(boolean salesOrder) {
        this.salesOrder = salesOrder;
    }
}
