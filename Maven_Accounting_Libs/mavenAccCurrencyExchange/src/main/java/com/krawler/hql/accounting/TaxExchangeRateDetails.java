/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Date;

public class TaxExchangeRateDetails {

    private String ID;
    private Date applyDate;
    private double exchangeRate;
    private TaxExchangeRate exchangeratelink;
    private Company company;
    private Date toDate;
    private long exchangeorder;  //Max exchange order indicates latest exchange rate for a day
    private double foreignToBaseExchangeRate;

    public long getExchangeorder() {
        return exchangeorder;
    }

    public void setExchangeorder(long exchangeorder) {
        this.exchangeorder = exchangeorder;
    }
   
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

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public TaxExchangeRate getExchangeratelink() {
        return exchangeratelink;
    }

    public void setExchangeratelink(TaxExchangeRate exchangeratelink) {
        this.exchangeratelink = exchangeratelink;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public double getForeignToBaseExchangeRate() {
        return foreignToBaseExchangeRate;
    }

    public void setForeignToBaseExchangeRate(double foreignToBaseExchangeRate) {
        this.foreignToBaseExchangeRate = foreignToBaseExchangeRate;
    }
}
