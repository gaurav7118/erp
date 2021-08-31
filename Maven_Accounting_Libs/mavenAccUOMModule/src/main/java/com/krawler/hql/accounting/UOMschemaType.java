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
public class UOMschemaType {
    private String ID;
    private String name;
    private UnitOfMeasure stockuom;
    private Company company;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UnitOfMeasure getStockuom() {
        return stockuom;
    }

    public void setStockuom(UnitOfMeasure stockuom) {
        this.stockuom = stockuom;
    }
    
    
}
