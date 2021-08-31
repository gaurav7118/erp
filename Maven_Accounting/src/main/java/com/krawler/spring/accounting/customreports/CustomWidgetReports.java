/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.customreports;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;

public class CustomWidgetReports {

    private String ID;
    private String reportName;
    private String customReports;
    private String searchCriteria;
    private User createdby;
    private User modifiedby;
    private long createdon;
    private long updatedon;
    private Company company;
    private int filterAppend; // 0- OR    1- AND
    private boolean deleted;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getCustomReports() {
        return customReports;
    }

    public void setCustomReports(String customReports) {
        this.customReports = customReports;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getSearchCriteria() {
        return searchCriteria;
    }

    public void setSearchCriteria(String searchCriteria) {
        this.searchCriteria = searchCriteria;
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

    public long getCreatedon() {
        return createdon;
    }

    public void setCreatedon(long createdon) {
        this.createdon = createdon;
    }

    public User getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(User modifiedby) {
        this.modifiedby = modifiedby;
    }

    public long getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(long updatedon) {
        this.updatedon = updatedon;
    }

    public int getFilterAppend() {
        return filterAppend;
    }

    public void setFilterAppend(int filterAppend) {
        this.filterAppend = filterAppend;
    }
    
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}