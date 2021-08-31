/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.PricingBandMaster;
import com.krawler.common.admin.Company;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class ProductDiscountMapping {

    private String id;
    private Product product;
    private PricingBandMaster pricingBandMaster;
    private DiscountMaster discountMaster;
    private Company company;
    private Date applicableDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public PricingBandMaster getPricingBandMaster() {
        return pricingBandMaster;
    }

    public void setPricingBandMaster(PricingBandMaster pricingBandMaster) {
        this.pricingBandMaster = pricingBandMaster;
    }

    public DiscountMaster getDiscountMaster() {
        return discountMaster;
    }

    public void setDiscountMaster(DiscountMaster discountMaster) {
        this.discountMaster = discountMaster;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getApplicableDate() {
        return applicableDate;
    }

    public void setApplicableDate(Date applicableDate) {
        this.applicableDate = applicableDate;
    }
    
}
