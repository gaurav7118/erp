/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.FieldComboData;
import com.krawler.common.admin.FieldParams;

/**
 *
 * @author krawler
 */
public class DistributeBalance {
    private String id;
    private Account accountid;
    private FieldComboData comboid;
    private double openingbal;
    private FieldParams field;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Account getAccountid() {
        return accountid;
    }

    public void setAccountid(Account accountid) {
        this.accountid = accountid;
    }

    public FieldComboData getComboid() {
        return comboid;
    }

    public void setComboid(FieldComboData comboid) {
        this.comboid = comboid;
    }

    public double getOpeningbal() {
        return openingbal;
    }

    public void setOpeningbal(double openingbal) {
        this.openingbal = openingbal;
    }
    
    public FieldParams getField() {
        return field;
    }

    public void setField(FieldParams field) {
        this.field = field;
    }
}
