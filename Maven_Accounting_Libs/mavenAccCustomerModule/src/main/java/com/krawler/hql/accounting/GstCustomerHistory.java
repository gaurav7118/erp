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
 * Save Customer GST related fields
 */

public class GstCustomerHistory {

    private String id;
    private MasterItem GSTCustomerType;
    private MasterItem GSTRegistrationType;
    private String gstin;
    private Customer customer;
    private Date applyDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MasterItem getGSTCustomerType() {
        return GSTCustomerType;
    }

    public void setGSTCustomerType(MasterItem GSTCustomerType) {
        this.GSTCustomerType = GSTCustomerType;
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Date getApplyDate() {
        return applyDate;
    }

    public void setApplyDate(Date applyDate) {
        this.applyDate = applyDate;
    }

}
