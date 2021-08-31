/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class CustomerVendorMapping {
    private String id;
    private Customer customeraccountid;
    private Vendor vendoraccountid;
    private boolean mappingflag;

    public Customer getCustomeraccountid() {
        return customeraccountid;
    }

    public void setCustomeraccountid(Customer customeraccountid) {
        this.customeraccountid = customeraccountid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Vendor getVendoraccountid() {
        return vendoraccountid;
    }

    public void setVendoraccountid(Vendor vendoraccountid) {
        this.vendoraccountid = vendoraccountid;
    }
    
    public boolean isMappingflag() {
        return mappingflag;
    }

    public void setMappingflag(boolean mappingflag) {
        this.mappingflag = mappingflag;
    }
}
