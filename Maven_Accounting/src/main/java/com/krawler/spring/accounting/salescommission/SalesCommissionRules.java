/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.salescommission;

import com.krawler.common.admin.Company;

public class SalesCommissionRules {
    private String id;
    private int schemaType;//1.Percentage 2.Flat
    private double amount;
    private String rulesDescription;
    private Company company;
    private SalesCommissionSchemaMaster schemaMaster;
    
    public static final String ID = "id";
    public static final String RULEID = "ruleid";
    public static final String SCHEMATYPE = "schemaType";
    public static final String SCHEMATYPEID = "schemaTypeId";
    public static final String AMOUNT = "amount";
    public static final String RULEDESCRIPTION = "ruledescription";
    public static final String SCEHMAMASTER = "scehmaMaster";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSchemaType() {
        return schemaType;
    }

    public void setSchemaType(int schemaType) {
        this.schemaType = schemaType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getRulesDescription() {
        return rulesDescription;
    }

    public void setRulesDescription(String rulesDescription) {
        this.rulesDescription = rulesDescription;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public SalesCommissionSchemaMaster getSchemaMaster() {
        return schemaMaster;
    }

    public void setSchemaMaster(SalesCommissionSchemaMaster schemaMaster) {
        this.schemaMaster = schemaMaster;
    }

}
