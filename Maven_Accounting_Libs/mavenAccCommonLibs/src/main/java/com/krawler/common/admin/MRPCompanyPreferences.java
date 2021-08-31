/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class MRPCompanyPreferences {
    private String id;
    private Company company;
    private int autoGenPurchaseType;
    private int woInventoryUpdateType;
    private int mrpProductComponentType;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    
    public int getAutoGenPurchaseType() {
        return autoGenPurchaseType;
    }

    public void setAutoGenPurchaseType(int autoGenPurchaseType) {
        this.autoGenPurchaseType = autoGenPurchaseType;
    }

    public int getWoInventoryUpdateType() {
        return woInventoryUpdateType;
    }

    public void setWoInventoryUpdateType(int woInventoryUpdateType) {
        this.woInventoryUpdateType = woInventoryUpdateType;
    }
    
    public void setmrpProductComponentType(int mrpProductComponentType) {
        this.mrpProductComponentType = mrpProductComponentType;
    }
    
    public int getmrpProductComponentType() {
        return mrpProductComponentType;
    }
    
}
