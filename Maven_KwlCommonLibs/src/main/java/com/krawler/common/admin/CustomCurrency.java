/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author Krawler
 */
public class CustomCurrency {
    private String ID;
    private KWLCurrency currencyID;
    private String name;
    private String systemcurrencysymbol;
    private String systemcurrencycode;
    private String customcurrencysymbol;
    private String customcurrencycode;
    private String companyid;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
    public KWLCurrency getCurrencyID() {
        return currencyID;
    }

    public void setCurrencyID(KWLCurrency currencyID) {
        this.currencyID = currencyID;
    }

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }

   

    public String getCustomcurrencycode() {
        return customcurrencycode;
    }

    public void setCustomcurrencycode(String customcurrencycode) {
        this.customcurrencycode = customcurrencycode;
    }

    public String getCustomcurrencysymbol() {
        return customcurrencysymbol;
    }

    public void setCustomcurrencysymbol(String customcurrencysymbol) {
        this.customcurrencysymbol = customcurrencysymbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystemcurrencycode() {
        return systemcurrencycode;
    }

    public void setSystemcurrencycode(String systemcurrencycode) {
        this.systemcurrencycode = systemcurrencycode;
    }

    public String getSystemcurrencysymbol() {
        return systemcurrencysymbol;
    }

    public void setSystemcurrencysymbol(String systemcurrencysymbol) {
        this.systemcurrencysymbol = systemcurrencysymbol;
    }
    
}
