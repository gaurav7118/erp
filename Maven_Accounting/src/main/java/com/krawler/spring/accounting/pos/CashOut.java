/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.pos;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.hql.accounting.MasterItem;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class CashOut {
    
    private String ID;
    private MasterItem reason;
    private double amount;
    private Company company;
    private Date transactionDate;
    private long transactionDateinLong;
    private String storeid;
    private User userid;
    private boolean isdeposit;   //To check deposit type or cash out type

    public boolean isIsdeposit() {
        return isdeposit;
    }

    public void setIsdeposit(boolean isdeposit) {
        this.isdeposit = isdeposit;
    }


    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public long getTransactionDateinLong() {
        return transactionDateinLong;
    }

    public void setTransactionDateinLong(long transactionDateinLong) {
        this.transactionDateinLong = transactionDateinLong;
    }

    public User getUserid() {
        return userid;
    }

    public void setUserid(User userid) {
        this.userid = userid;
    }

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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getStoreid() {
        return storeid;
    }

    public void setStoreid(String storeid) {
        this.storeid = storeid;
    }

    public MasterItem getReason() {
        return reason;
    }

    public void setReason(MasterItem reason) {
        this.reason = reason;
    }
    
}
