/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.accounting.salescommission;

import com.krawler.common.admin.Company;
import com.krawler.hql.accounting.MasterItem;

public class SalesCommissionRuleCondition{
    private String id;
    private int commissionType;//1.Amount 2.Brand/Product Category 3.Payment Term 4.Margin
    private double lowerLimit;
    private double upperLimit;
    private String categoryId;
    private int marginCondition;
    private Company company;
    private SalesCommissionRules commissionRules;
    
    public static final String ID = "id";
    public static final String CONDITIONID = "conditionId";
    public static final String COMMISSIONTYPE = "commissiontype";
    public static final String LOWERLIMIT = "lowerlimit";
    public static final String UPPERLIMIT = "upperlimit";
    public static final String CATEGORYID = "categoryid";
    public static final String CATEGORYNAME = "categoryname";
    public static final String MARGINCONDITION = "marginCondition";
    public static final String SALESCOMMISSIONRULES = "salesCommissionRules";
    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCommissionType() {
        return commissionType;
    }

    public void setCommissionType(int commissionType) {
        this.commissionType = commissionType;
    }

    public double getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(double lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public double getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(double upperLimit) {
        this.upperLimit = upperLimit;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public int getMarginCondition() {
        return marginCondition;
    }

    public void setMarginCondition(int marginCondition) {
        this.marginCondition = marginCondition;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public SalesCommissionRules getCommissionRules() {
        return commissionRules;
    }

    public void setCommissionRules(SalesCommissionRules commissionRules) {
        this.commissionRules = commissionRules;
    }
}