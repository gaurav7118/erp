/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

public class UOBReceivingDetails{
    
    private String id;
    private String receivingBankAccountNumber;
    private String receivingAccountName;
    private String receivingBICCode;
    private String endToEndId;
    private String mandateId;
    private String purposeCode;
    private String ultimatePayerOrBeneficiaryName;
    private String customerReference;
    private String currencyCode;
    private Customer customer;
    private MasterItem customerBankAccountType;
    private Company company;
    private MasterItem bankName;//ERP-31397 - Saved Bank details against customer IBG details
    private String receivingBranchCode;//ERP-31397 - Saved Branch code against customer IBG details
    private String receivingBankCode;//ERP-31582
    private boolean activated;
    
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public MasterItem getCustomerBankAccountType() {
        return customerBankAccountType;
    }

    public void setCustomerBankAccountType(MasterItem customerBankAccountType) {
        this.customerBankAccountType = customerBankAccountType;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    public String getEndToEndId() {
        return endToEndId;
    }

    public void setEndToEndId(String endToEndId) {
        this.endToEndId = endToEndId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMandateId() {
        return mandateId;
    }

    public void setMandateId(String mandateId) {
        this.mandateId = mandateId;
    }

    public String getPurposeCode() {
        return purposeCode;
    }

    public void setPurposeCode(String purposeCode) {
        this.purposeCode = purposeCode;
    }

    public String getReceivingAccountName() {
        return receivingAccountName;
    }

    public void setReceivingAccountName(String receivingAccountName) {
        this.receivingAccountName = receivingAccountName;
    }

    public String getReceivingBICCode() {
        return receivingBICCode;
    }

    public void setReceivingBICCode(String receivingBICCode) {
        this.receivingBICCode = receivingBICCode;
    }

    public String getReceivingBankAccountNumber() {
        return receivingBankAccountNumber;
    }

    public void setReceivingBankAccountNumber(String receivingBankAccountNumber) {
        this.receivingBankAccountNumber = receivingBankAccountNumber;
    }

    public String getUltimatePayerOrBeneficiaryName() {
        return ultimatePayerOrBeneficiaryName;
    }

    public void setUltimatePayerOrBeneficiaryName(String ultimatePayerOrBeneficiaryName) {
        this.ultimatePayerOrBeneficiaryName = ultimatePayerOrBeneficiaryName;
    }
    
    public MasterItem getBankName() {
        return bankName;
    }

    public void setBankName(MasterItem bankName) {
        this.bankName = bankName;
    }

    public String getReceivingBranchCode() {
        return receivingBranchCode;
    }

    public void setReceivingBranchCode(String receivingBranchCode) {
        this.receivingBranchCode = receivingBranchCode;
    }

    public String getReceivingBankCode() {
        return receivingBankCode;
    }

    public void setReceivingBankCode(String receivingBankCode) {
        this.receivingBankCode = receivingBankCode;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }
}
