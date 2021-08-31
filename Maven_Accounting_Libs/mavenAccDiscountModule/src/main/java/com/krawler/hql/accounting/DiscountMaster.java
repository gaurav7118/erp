/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

public class DiscountMaster {

    private String id;
    private String dmid;
    private String name;
    private String description;
    private Company company;
    private String account;
    private boolean discounttype;
    private double value;
//    private Set<DiscountMasterDetails> discountMasterDetails;
//
//    public Set<DiscountMasterDetails> getDiscountMasterDetails() {
//        return discountMasterDetails;
//    }
//
//    public void setDiscountMasterDetails(Set<DiscountMasterDetails> discountMasterDetails) {
//        this.discountMasterDetails = discountMasterDetails;
//    }       

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getDmid() {
        return dmid;
    }

    public void setDmid(String dmid) {
        this.dmid = dmid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isDiscounttype() {
        return discounttype;
    }

    public void setDiscounttype(boolean discounttype) {
        this.discounttype = discounttype;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
