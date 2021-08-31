/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class ConsolidationData {
    private String ID;
    private double stakeInPercentage;
    private Company childCompany;
    private Company company;//    parent company

    public Company getChildCompany() {
        return childCompany;
    }

    public void setChildCompany(Company childCompany) {
        this.childCompany = childCompany;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getStakeInPercentage() {
        return stakeInPercentage;
    }

    public void setStakeInPercentage(double stakeInPercentage) {
        this.stakeInPercentage = stakeInPercentage;
    }
}
