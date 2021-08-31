/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class FinanceDetailsSubReport {

    String accountname = "";
    String entryno = "";
    String entrydate = "";
    String remmitto = "";
    Double amount = 0.0;
    String currency = "";
    String basecurrency = "";
    String currencyword = "";
    String duedate = "";
    String project = "";
    Double ccy = 0.0;

    public String getAccountname() {
        return accountname;
    }

    public void setAccountname(String accountname) {
        this.accountname = accountname;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getBasecurrency() {
        return basecurrency;
    }

    public void setBasecurrency(String basecurrency) {
        this.basecurrency = basecurrency;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDuedate() {
        return duedate;
    }

    public void setDuedate(String duedate) {
        this.duedate = duedate;
    }

    public String getEntrydate() {
        return entrydate;
    }

    public void setEntrydate(String entrydate) {
        this.entrydate = entrydate;
    }

    public String getEntryno() {
        return entryno;
    }

    public void setEntryno(String entryno) {
        this.entryno = entryno;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getRemmitto() {
        return remmitto;
    }

    public void setRemmitto(String remmitto) {
        this.remmitto = remmitto;
    }

    public Double getCcy() {
        return ccy;
    }

    public void setCcy(Double ccy) {
        this.ccy = ccy;
    }

    public String getCurrencyword() {
        return currencyword;
    }

    public void setCurrencyword(String currencyword) {
        this.currencyword = currencyword;
    }
    
    
}
