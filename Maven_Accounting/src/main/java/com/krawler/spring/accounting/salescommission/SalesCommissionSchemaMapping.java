/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.accounting.salescommission;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.FieldComboData;

public class SalesCommissionSchemaMapping{
    private String id;
    private SalesCommissionSchemaMaster schemaMaster;
    private FieldComboData masterItem;
    private Company company;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SalesCommissionSchemaMaster getSchemaMaster() {
        return schemaMaster;
    }

    public void setSchemaMaster(SalesCommissionSchemaMaster schemaMaster) {
        this.schemaMaster = schemaMaster;
    }

    public FieldComboData getMasterItem() {
        return masterItem;
    }

    public void setMasterItem(FieldComboData masterItem) {
        this.masterItem = masterItem;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
}