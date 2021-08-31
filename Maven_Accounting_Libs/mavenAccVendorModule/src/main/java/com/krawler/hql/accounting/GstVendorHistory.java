/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import java.util.Date;

/**
 *
 * @author Suhas C
 * 
 * Save Vendor GST related fields
 */
public class GstVendorHistory {

    private String id;
    private MasterItem GSTVendorType;
    private MasterItem GSTRegistrationType;
    private String gstin;
    private Date applyDate;
    private Vendor vendor;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MasterItem getGSTVendorType() {
        return GSTVendorType;
    }

    public void setGSTVendorType(MasterItem GSTVendorType) {
        this.GSTVendorType = GSTVendorType;
    }

    public MasterItem getGSTRegistrationType() {
        return GSTRegistrationType;
    }

    public void setGSTRegistrationType(MasterItem GSTRegistrationType) {
        this.GSTRegistrationType = GSTRegistrationType;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public Date getApplyDate() {
        return applyDate;
    }

    public void setApplyDate(Date applyDate) {
        this.applyDate = applyDate;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

}
