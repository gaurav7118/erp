/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

import java.util.Date;

/**
 *
 * @author krawler
 */
public class TaxPeriod {

    private String id;
    private String periodName;
    private Date startDate;
    private Date endDate;
    private TaxPeriod subPeriodOf;
    private int periodType; // 1= Year; 2 = Quarter; 3-Monthly 
    private Company company;
    private int periodFormat;
    private int yearInPeriodName;
    
    public final static int PERIODTYPE_YEAR=1;
    public final static int PERIODTYPE_QUARTER=2;
    public final static int PERIODTYPE_MONTHLY=3;
    public final static int PERIODTYPE_FULLYEAR=4;
    
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public int getPeriodType() {
        return periodType;
    }

    public void setPeriodType(int periodType) {
        this.periodType = periodType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    public TaxPeriod getSubPeriodOf() {
        return subPeriodOf;
    }

    public void setSubPeriodOf(TaxPeriod subPeriodOf) {
        this.subPeriodOf = subPeriodOf;
    }
    
    public int getPeriodFormat() {
        return periodFormat;
    }

    public void setPeriodFormat(int periodFormat) {
        this.periodFormat = periodFormat;
    }

    public int getYearInPeriodName() {
        return yearInPeriodName;
    }

    public void setYearInPeriodName(int yearInPeriodName) {
        this.yearInPeriodName = yearInPeriodName;
    }
    
}

