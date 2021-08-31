/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.pos;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;

/**
 *
 * @author krawler
 */
public class CashDenominations {

    private String ID;
    private String locationid;
    private KWLCurrency currency;
    private int currencydenomination;
    private Company company;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getLocationid() {
        return locationid;
    }

    public void setLocationid(String locationid) {
        this.locationid = locationid;
    }
    
    public int getCurrencydenomination() {
        return currencydenomination;
    }

    public void setCurrencydenomination(int currencydenomination) {
        this.currencydenomination = currencydenomination;
    }


    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }
    
}
