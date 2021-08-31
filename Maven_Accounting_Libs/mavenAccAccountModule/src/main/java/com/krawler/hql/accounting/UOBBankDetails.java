/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

public class UOBBankDetails{
    
    private String ID;
    private String originatingBICCode;
    private String currencyCode;
    private String originatingAccountNumber;
    private String originatingAccountName;
    private String ultimateOriginatingCustomer;
    private Company company;
    private Account account;
    private String UOBCompanyID;//used for auto populate UOB companyID while generating the IBG file

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getOriginatingAccountName() {
        return originatingAccountName;
    }

    public void setOriginatingAccountName(String originatingAccountName) {
        this.originatingAccountName = originatingAccountName;
    }

    public String getOriginatingAccountNumber() {
        return originatingAccountNumber;
    }

    public void setOriginatingAccountNumber(String originatingAccountNumber) {
        this.originatingAccountNumber = originatingAccountNumber;
    }

    public String getOriginatingBICCode() {
        return originatingBICCode;
    }

    public void setOriginatingBICCode(String originatingBICCode) {
        this.originatingBICCode = originatingBICCode;
    }

    public String getUltimateOriginatingCustomer() {
        return ultimateOriginatingCustomer;
    }

    public void setUltimateOriginatingCustomer(String ultimateOriginatingCustomer) {
        this.ultimateOriginatingCustomer = ultimateOriginatingCustomer;
    }

    public String getUOBCompanyID() {
        return UOBCompanyID;
    }

    public void setUOBCompanyID(String UOBCompanyID) {
        this.UOBCompanyID = UOBCompanyID;
    }
}

