/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import java.util.Date;

/**
 *
 * @author krawler
 */
public class ConsolidationExchangeRateDetails {
   private String ID;
   private double exchangeRate;
   private Date applyDate;
   private ConsolidationData consolidationData;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Date getApplyDate() {
        return applyDate;
    }

    public void setApplyDate(Date applyDate) {
        this.applyDate = applyDate;
    }

    public ConsolidationData getConsolidationData() {
        return consolidationData;
    }

    public void setConsolidationData(ConsolidationData consolidationData) {
        this.consolidationData = consolidationData;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
}
