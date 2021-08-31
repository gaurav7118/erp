/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockout;

import com.krawler.common.admin.Company;
import com.krawler.inventory.model.store.Store;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
public class StockAdjustmentDraft implements Serializable{
    
    private String id;
    private String name;
    private String description;
    private Store store;
    private Date businessDate;
    private Company company;
    private Set<StockAdjustment> stockAdjustments;

    public StockAdjustmentDraft(Company company, String name, String description, Store store, Date businessDate) {
        this.company = company;
        this.name = name;
        this.description = description;
        this.store = store;
        this.businessDate = businessDate;
        this.stockAdjustments = new HashSet();
    }

    public StockAdjustmentDraft() {
    }

    public Date getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(Date businessDate) {
        this.businessDate = businessDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Set<StockAdjustment> getStockAdjustments() {
        return stockAdjustments;
    }

    public void setStockAdjustments(Set<StockAdjustment> stockAdjustments) {
        this.stockAdjustments = stockAdjustments;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StockAdjustmentDraft other = (StockAdjustmentDraft) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
    
}
