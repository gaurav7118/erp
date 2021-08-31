/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;

public class ProductPriceRule {

    private String ID;
    private double lowerlimit;
    private double upperlimit;
    private int percentageType;
    private double amount;
    private Company company;
    private MasterItem category;
    private int increamentordecreamentType;
    private int priceType;
    private int ruleType;
    private KWLCurrency currency;
    private int basedOn; // '1' - Existing Price, '2' - Average Cost, '3' - Most Recent Cost, '4' - Initial Purchase Price

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public MasterItem getCategory() {
        return category;
    }

    public void setCategory(MasterItem category) {
        this.category = category;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public int getIncreamentordecreamentType() {
        return increamentordecreamentType;
    }

    public void setIncreamentordecreamentType(int increamentordecreamentType) {
        this.increamentordecreamentType = increamentordecreamentType;
    }

    public double getLowerlimit() {
        return lowerlimit;
    }

    public void setLowerlimit(double lowerlimit) {
        this.lowerlimit = lowerlimit;
    }

    public int getPercentageType() {
        return percentageType;
    }

    public void setPercentageType(int percentageType) {
        this.percentageType = percentageType;
    }

    public int getPriceType() {
        return priceType;
    }

    public void setPriceType(int priceType) {
        this.priceType = priceType;
    }

    public int getRuleType() {
        return ruleType;
    }

    public void setRuleType(int ruleType) {
        this.ruleType = ruleType;
    }

    public double getUpperlimit() {
        return upperlimit;
    }

    public void setUpperlimit(double upperlimit) {
        this.upperlimit = upperlimit;
    }

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }

    public int getBasedOn() {
        return basedOn;
    }

    public void setBasedOn(int basedOn) {
        this.basedOn = basedOn;
    }
}