/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import java.util.Date;

/**
 *
 * @author Pandurang
 */
public class RevaluationTime {

    private String ID;
    private int accountType;
    private int month;
    private int year;
    private Date revalDate;
    private User userid;
    private Company company;
    private int currencyId;
    /*
     * Reval id used for mapping revaluation history 
     */
    private String revalId;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Date getRevalDate() {
        return revalDate;
    }

    public void setRevalDate(Date revalDate) {
        this.revalDate = revalDate;
    }
    
    public Company getCompany() {
        return company;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(int currencyId) {
        this.currencyId = currencyId;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public User getUserid() {
        return userid;
    }

    public void setUserid(User userid) {
        this.userid = userid;
    }
    
    
    /**
     * @return the revalId
     */
    public String getRevalId() {
        return revalId;
    }

    /**
     * @param revalId the revalId to set
     */
    public void setRevalId(String revalId) {
        this.revalId = revalId;
    }
}
