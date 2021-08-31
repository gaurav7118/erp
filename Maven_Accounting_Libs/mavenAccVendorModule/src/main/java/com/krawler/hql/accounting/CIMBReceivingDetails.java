/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class CIMBReceivingDetails {
    private String id;
    private String collectionAccountNumber;
    private String collectionAccountName;
    private String giroBICCode;
    private String referenceNumber;
    private Vendor vendor;
    private MasterItem masterItem;
    private Company company;
    private String emailForGiro;
    
    public String getCollectionAccountName() {
        return collectionAccountName;
    }

    public void setCollectionAccountName(String collectionAccountName) {
        this.collectionAccountName = collectionAccountName;
    }

    public String getCollectionAccountNumber() {
        return collectionAccountNumber;
    }

    public void setCollectionAccountNumber(String collectionAccountNumber) {
        this.collectionAccountNumber = collectionAccountNumber;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getGiroBICCode() {
        return giroBICCode;
    }

    public void setGiroBICCode(String giroBICCode) {
        this.giroBICCode = giroBICCode;
    }

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

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public String getEmailForGiro() {
        return emailForGiro;
    }

    public void setEmailForGiro(String emailForGiro) {
        this.emailForGiro = emailForGiro;
    }
    
}
