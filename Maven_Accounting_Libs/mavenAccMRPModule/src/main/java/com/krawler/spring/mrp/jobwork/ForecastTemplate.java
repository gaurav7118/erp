/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.mrp.jobwork;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import java.util.Date;
import java.util.Set;

public class ForecastTemplate {

    private String title;
    private String ID;
    private String forecastId;
    private String forecastType;   // 1=sales order 2 =invoice 3 =delivery order
    private String forecastMethod; // 1= Percent over last year 2= Last year to this year
    private String forecastYearHistory;
    private Date createdOn;
    private Date modifiedOn;
    private User createdby;
    private User modifiedby;
    private Date forecastYear;
    private Company company;

    public static final String TITLE = "title";
    public static final String FORECASTID = "forecastid";
    public static final String FORECASTYEARHISTORY = "forecastyearhistory";
    public static final String FORECASTYEAR = "forecastyear";
    public static final String FORECASTTYPE = "forecasttype";
    public static final String FORECASTMETHOD = "forecastmethod";
    public static final String FORECASTPRODUCT = "product";

    public static final String SALESORDER = "1";
    public static final String INVOICE = "2";
    public static final String DELIVERYORDER = "3";

    public static final String PERCENTOVERLASTYEAR = "1";
    public static final String LASTYEARTOTHISYEAR = "2";

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getForecastId() {
        return forecastId;
    }

    public void setForecastId(String forecastId) {
        this.forecastId = forecastId;
    }

    public String getForecastType() {
        return forecastType;
    }

    public void setForecastType(String forecastType) {
        this.forecastType = forecastType;
    }

    public String getForecastMethod() {
        return forecastMethod;
    }

    public void setForecastMethod(String forecastMethod) {
        this.forecastMethod = forecastMethod;
    }

    public String getForecastYearHistory() {
        return forecastYearHistory;
    }

    public void setForecastYearHistory(String forecastYearHistory) {
        this.forecastYearHistory = forecastYearHistory;
    }

    public Date getForecastYear() {
        return forecastYear;
    }

    public void setForecastYear(Date forecastYear) {
        this.forecastYear = forecastYear;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public User getCreatedby() {
        return createdby;
    }

    public void setCreatedby(User createdby) {
        this.createdby = createdby;
    }

    public User getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(User modifiedby) {
        this.modifiedby = modifiedby;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }
    private Set<ForecastProductMapping> forecastProductMappings;

    public Set<ForecastProductMapping> getForecastProductMappings() {
        return forecastProductMappings;
    }

    public void setForecastProductMappings(Set<ForecastProductMapping> forecastProductMappings) {
        this.forecastProductMappings = forecastProductMappings;
    }

}
