/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.FieldComboData;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.PricingBandMaster;
import java.util.Date;

public class ProductBrandDiscountDetails {

    private String ID;
    private PricingBandMaster pricingBandMaster;
    private KWLCurrency currency;
    private Date applicableDate;
    private boolean isCustomerCategory;
    private Customer customer;
    private MasterItem customerCategory;
//    private MasterItem productBrand;
    private FieldComboData productBrand;
    private String discountType; // 0 - 'Flat', 1 - 'Percentage'
    private double discountValue;
    private Company company;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Date getApplicableDate() {
        return applicableDate;
    }

    public void setApplicableDate(Date applicableDate) {
        this.applicableDate = applicableDate;
    }

    public FieldComboData getProductBrand() {
        return productBrand;
    }

    public void setProductBrand(FieldComboData productBrand) {
        this.productBrand = productBrand;
    }

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public MasterItem getCustomerCategory() {
        return customerCategory;
    }

    public void setCustomerCategory(MasterItem customerCategory) {
        this.customerCategory = customerCategory;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }

    public boolean isIsCustomerCategory() {
        return isCustomerCategory;
    }

    public void setIsCustomerCategory(boolean isCustomerCategory) {
        this.isCustomerCategory = isCustomerCategory;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public PricingBandMaster getPricingBandMaster() {
        return pricingBandMaster;
    }

    public void setPricingBandMaster(PricingBandMaster pricingBandMaster) {
        this.pricingBandMaster = pricingBandMaster;
    }
}