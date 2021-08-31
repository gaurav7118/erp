/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.accounting.salescommission;

import com.krawler.common.admin.Company;

public class SalesCommissionSchemaMaster {

    private String id;
    private String schemaMaster;
    private Company company;
    
    public static final String ID = "id";
    public static final String SCHEMAMASTERID = "schemaMasterId";
    public static final String SCHEMAMASTERNAME = "schemaMaster";
    public static final String RULESDETAIL = "rulesDetail";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSchemaMaster() {
        return schemaMaster;
    }

    public void setSchemaMaster(String schemaMaster) {
        this.schemaMaster = schemaMaster;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
}
