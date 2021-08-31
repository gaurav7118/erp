/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.FieldComboData;

public class MultiEntityMapping {

    private String id;
    private FieldComboData multiEntity;
    private String gstNumber;
    private String taxNumber;
    private String companyBRN;
    private MasterItem industryCode;
    private Company company;
    private int gstSubmissionPeriod;// 0 -  Monthly, 1 - Quarterly.

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FieldComboData getMultiEntity() {
        return multiEntity;
    }

    public void setMultiEntity(FieldComboData multiEntity) {
        this.multiEntity = multiEntity;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public String getCompanyBRN() {
        return companyBRN;
    }

    public void setCompanyBRN(String companyBRN) {
        this.companyBRN = companyBRN;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    /**
     * @return the industryCode
     */
    public MasterItem getIndustryCode() {
        return industryCode;
    }

    /**
     * @param industryCode the industryCode to set
     */
    public void setIndustryCode(MasterItem industryCode) {
        this.industryCode = industryCode;
    }

    public int getGstSubmissionPeriod() {
        return gstSubmissionPeriod;
    }

    public void setGstSubmissionPeriod(int gstSubmissionPeriod) {
        this.gstSubmissionPeriod = gstSubmissionPeriod;
    }
}
