/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author Pandurang
 */
public class UOMSchema {
    private String ID;
    private UnitOfMeasure purchaseuom;
    private UnitOfMeasure salesuom;
    private UnitOfMeasure orderuom;
    private UnitOfMeasure transferuom;
    private UnitOfMeasure baseuom;
    private double baseuomrate;
    private double rateperuom;
    private UOMNature uomnature;
    private UOMschemaType uomschematype;
    private Company company;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getBaseuomrate() {
        return baseuomrate;
    }

    public void setBaseuomrate(double baseuomrate) {
        this.baseuomrate = baseuomrate;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public UnitOfMeasure getPurchaseuom() {
        return purchaseuom;
    }

    public void setPurchaseuom(UnitOfMeasure purchaseuom) {
        this.purchaseuom = purchaseuom;
    }

    public UnitOfMeasure getSalesuom() {
        return salesuom;
    }

    public void setSalesuom(UnitOfMeasure salesuom) {
        this.salesuom = salesuom;
    }

    public UnitOfMeasure getBaseuom() {
        return baseuom;
    }

    public void setBaseuom(UnitOfMeasure baseuom) {
        this.baseuom = baseuom;
    }
    
    public UOMNature getUomnature() {
        return uomnature;
    }

    public void setUomnature(UOMNature uomnature) {
        this.uomnature = uomnature;
    }

    public UOMschemaType getUomschematype() {
        return uomschematype;
    }

    public void setUomschematype(UOMschemaType uomschematype) {
        this.uomschematype = uomschematype;
    }
    
     public double getRateperuom() {
        return rateperuom;
    }

    public void setRateperuom(double rateperuom) {
        this.rateperuom = rateperuom;
    }

    public UnitOfMeasure getOrderuom() {
        return orderuom;
    }

    public void setOrderuom(UnitOfMeasure orderuom) {
        this.orderuom = orderuom;
    }

    public UnitOfMeasure getTransferuom() {
        return transferuom;
    }

    public void setTransferuom(UnitOfMeasure transferuom) {
        this.transferuom = transferuom;
    }
    
}
