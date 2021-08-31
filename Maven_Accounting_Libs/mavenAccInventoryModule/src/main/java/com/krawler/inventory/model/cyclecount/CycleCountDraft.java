/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.cyclecount;

import com.krawler.common.admin.Company;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.store.Store;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
class CycleCountDraft {
    
    private String id;
    private Date businessDate;
    private Store store;
//    private Location location;
    private Company company;
    private Set<CycleCount> cycleCountSet;

    public CycleCountDraft() {
        this.cycleCountSet = new HashSet<CycleCount>();
    }

    public CycleCountDraft(Date businessDate, Store store, Location location, Company company) {
        this();
        this.businessDate = businessDate;
        this.store = store;
//        this.location = location;
        this.company = company;
    }

    public Date getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(Date businessDate) {
        this.businessDate = businessDate;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Set<CycleCount> getCycleCountSet() {
        return cycleCountSet;
    }

    public void setCycleCountSet(Set<CycleCount> cycleCountSet) {
        this.cycleCountSet = cycleCountSet;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
//
//    public Location getLocation() {
//        return location;
//    }
//
//    public void setLocation(Location location) {
//        this.location = location;
//    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }
    
}
