/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class DealerExciseTerms {
    private String id;
    private double percentage;
    private double dutyAmount;//Duty amount of Dealer Excise Details as it is set to editable.
    private double manuImpDutyAmount;//Manufacturer/Importer Duty amount of Dealer Excise Details as it is set to editable.
    private Company company;
    private DealerExciseDetails dealerExciseDetails;
    private LineLevelTerms lineLevelTerm;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public DealerExciseDetails getDealerExciseDetails() {
        return dealerExciseDetails;
    }

    public void setDealerExciseDetails(DealerExciseDetails dealerExciseDetails) {
        this.dealerExciseDetails = dealerExciseDetails;
    }

    public LineLevelTerms getLineLevelTerm() {
        return lineLevelTerm;
    }

    public void setLineLevelTerm(LineLevelTerms lineLevelTerm) {
        this.lineLevelTerm = lineLevelTerm;
    }

    public double getDutyAmount() {
        return dutyAmount;
    }
    public void setDutyAmount(double dutyAmount) {
        this.dutyAmount = dutyAmount;
    }
    public double getManuImpDutyAmount() {
        return manuImpDutyAmount;
    }
    public void setManuImpDutyAmount(double manuImpDutyAmount) {
        this.manuImpDutyAmount = manuImpDutyAmount;
    }
}
