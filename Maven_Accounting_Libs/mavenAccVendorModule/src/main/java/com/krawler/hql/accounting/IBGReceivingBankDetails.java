/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author Atul
 */
public class IBGReceivingBankDetails {

    private String id;
    private String receivingBankCode;
    private String receivingBankName;
    private String receivingBranchCode;
    private String receivingAccountNumber;
    private String receivingAccountName;
    private Vendor vendor;
    private MasterItem masterItem;
    private Company company;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MasterItem getMasterItem() {
        return masterItem;
    }

    public void setMasterItem(MasterItem masterItem) {
        this.masterItem = masterItem;
    }

    public String getReceivingAccountName() {
        return receivingAccountName;
    }

    public void setReceivingAccountName(String receivingAccountName) {
        this.receivingAccountName = receivingAccountName;
    }

    public String getReceivingAccountNumber() {
        return receivingAccountNumber;
    }

    public void setReceivingAccountNumber(String receivingAccountNumber) {
        this.receivingAccountNumber = receivingAccountNumber;
    }

    public String getReceivingBankCode() {
        return receivingBankCode;
    }

    public void setReceivingBankCode(String receivingBankCode) {
        this.receivingBankCode = receivingBankCode;
    }

    public String getReceivingBankName() {
        return receivingBankName;
    }

    public void setReceivingBankName(String receivingBankName) {
        this.receivingBankName = receivingBankName;
    }

    public String getReceivingBranchCode() {
        return receivingBranchCode;
    }

    public void setReceivingBranchCode(String receivingBranchCode) {
        this.receivingBranchCode = receivingBranchCode;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
