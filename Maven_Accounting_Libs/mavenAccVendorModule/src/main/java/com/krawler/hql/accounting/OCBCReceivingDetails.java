/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

public class OCBCReceivingDetails {

    private String id;
    private String bankCode;
    private String accountNumber;
    private String ultimateCreditorName;
    private String ultimateDebtorName;
    private String remittanceAdviceVia;//E - Email, F - Fax
    private String remittanceAdviceSendDetails;
    private Vendor vendor;
    private MasterItem paidTo;
    private Company company;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getUltimateCreditorName() {
        return ultimateCreditorName;
    }

    public void setUltimateCreditorName(String ultimateCreditorName) {
        this.ultimateCreditorName = ultimateCreditorName;
    }

    public String getUltimateDebtorName() {
        return ultimateDebtorName;
    }

    public void setUltimateDebtorName(String ultimateDebtorName) {
        this.ultimateDebtorName = ultimateDebtorName;
    }

    public String getRemittanceAdviceVia() {
        return remittanceAdviceVia;
    }

    public void setRemittanceAdviceVia(String remittanceAdviceVia) {
        this.remittanceAdviceVia = remittanceAdviceVia;
    }

    public String getRemittanceAdviceSendDetails() {
        return remittanceAdviceSendDetails;
    }

    public void setRemittanceAdviceSendDetails(String remittanceAdviceSendDetails) {
        this.remittanceAdviceSendDetails = remittanceAdviceSendDetails;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public MasterItem getPaidTo() {
        return paidTo;
    }

    public void setPaidTo(MasterItem paidTo) {
        this.paidTo = paidTo;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

}
