/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.KWLCurrency;

public class TaxExchangeRate {

    private String ID;
    private KWLCurrency fromCurrency;
    private KWLCurrency toCurrency;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public KWLCurrency getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(KWLCurrency fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public KWLCurrency getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(KWLCurrency toCurrency) {
        this.toCurrency = toCurrency;
    }
}
