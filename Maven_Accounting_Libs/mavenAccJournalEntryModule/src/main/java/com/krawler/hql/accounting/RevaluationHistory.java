/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.User;
import java.util.Date;

/**
 *
 * @author Malhari
 */
public class RevaluationHistory {

    private String ID;
    private String revalid;
    private String invoiceid;
    private KWLCurrency currency;
    private int moduleid;
    private double evalrate;
    private double amount;  //for document amount which is to revaluate
    private double currentRate; //for Exchange rate before revaluation
    private Date evaldate;
    private User userid;
    private Company company;
    private boolean deleted;    //flag for deleted invoices after reevaluation
    private boolean isRealised; //flag for maintaing realised invoice id 
    private double profitloss; //for maintaing profit loss for perticular invoice
    private String accountid; //storing account for each transaction

    public User getUserid() {
        return userid;
    }

    public void setUserid(User userid) {
        this.userid = userid;
    }

    public Date getEvaldate() {
        return evaldate;
    }

    public void setEvaldate(Date evaldate) {
        this.evaldate = evaldate;
    }
    private int issaveeval;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getRevalid() {
        return revalid;
    }

    public void setRevalid(String revalid) {
        this.revalid = revalid;
    }

    public String getInvoiceid() {
        return invoiceid;
    }

    public void setInvoiceid(String invoiceid) {
        this.invoiceid = invoiceid;
    }

    public int getModuleid() {
        return moduleid;
    }

    public void setModuleid(int moduleid) {
        this.moduleid = moduleid;
    }

    public double getEvalrate() {
        return evalrate;
    }

    public void setEvalrate(double evalrate) {
        this.evalrate = evalrate;
    }

    public int getIssaveeval() {
        return issaveeval;
    }

    public void setIssaveeval(int issaveeval) {
        this.issaveeval = issaveeval;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isIsRealised() {
        return isRealised;
    }

    public void setIsRealised(boolean isRealised) {
        this.isRealised = isRealised;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getAccountid() {
        return accountid;
    }

    public void setAccountid(String accountid) {
        this.accountid = accountid;
    }

    public double getProfitloss() {
        return profitloss;
    }

    public void setProfitloss(double profitloss) {
        this.profitloss = profitloss;
    }

    public double getCurrentRate() {
        return currentRate;
    }

    public void setCurrentRate(double currentRate) {
        this.currentRate = currentRate;
    }

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }
    
}
