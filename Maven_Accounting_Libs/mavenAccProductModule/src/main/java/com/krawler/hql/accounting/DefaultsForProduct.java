/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.hql.accounting.*;

/**
 *
 * @author krawler
 */
public class DefaultsForProduct {

    private UnitOfMeasure unitOfMeasure;
    private Producttype producttype;
    private Account paccount;
    private Account saccount;

    public Account getPaccount() {
        return paccount;
    }

    public void setPaccount(Account paccount) {
        this.paccount = paccount;
    }

    public Producttype getProducttype() {
        return producttype;
    }

    public void setProducttype(Producttype producttype) {
        this.producttype = producttype;
    }

    public Account getSaccount() {
        return saccount;
    }

    public void setSaccount(Account saccount) {
        this.saccount = saccount;
    }

    public UnitOfMeasure getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }
}
