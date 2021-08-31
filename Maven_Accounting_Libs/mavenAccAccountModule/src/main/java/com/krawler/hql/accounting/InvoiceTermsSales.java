/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;

public class InvoiceTermsSales {

    private String id;
    private String term;
    private Account account;
    private Company company;
    private Integer sign;
    private String category;
    private Integer includegst;
    private Integer includeprofit;
    private String formula;
    private Integer supressamount;
    private Integer deleted;
    private Long createdOn;
    private User creator;
    private boolean salesOrPurchase;
    private double percentage;
    private double termAmount;
    private int termType; 
    private String formulaids;
    private double purchaseValueOrSaleValue;
    private double deductionOrAbatementPercent;
    private int taxType; // Tax Type = 0 for flat amount or = 1 if it is in percentage.
    private boolean isDefault;//For India Country Only.
    private boolean isTermActive;//isTermActive=T term is active isTermActive=false term is deactive 
    private int termSequence; 
    private boolean includeInTDSCalculation;//For India Country Only.

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Integer getSign() {
        return sign;
    }

    public void setSign(Integer sign) {
        this.sign = sign;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getIncludegst() {
        return includegst;
    }

    public void setIncludegst(Integer includegst) {
        this.includegst = includegst;
    }

    public Integer getIncludeprofit() {
        return includeprofit;
    }

    public void setIncludeprofit(Integer includeprofit) {
        this.includeprofit = includeprofit;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public Integer getSupressamount() {
        return supressamount;
    }

    public void setSupressamount(Integer supressamount) {
        this.supressamount = supressamount;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public boolean isSalesOrPurchase() {
        return salesOrPurchase;
    }

    public void setSalesOrPurchase(boolean salesOrPurchase) {
        this.salesOrPurchase = salesOrPurchase;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public double getTermAmount() {
        return termAmount;
    }

    public void setTermAmount(double termAmount) {
        this.termAmount = termAmount;
    }

    public int getTermType() {
        return termType;
    }

    public void setTermType(int termType) {
        this.termType = termType;
    }

    public String getFormulaids() {
        return formulaids;
    }

    public void setFormulaids(String formulaids) {
        this.formulaids = formulaids;
    }

    public double getPurchaseValueOrSaleValue() {
        return purchaseValueOrSaleValue;
    }

    public void setPurchaseValueOrSaleValue(double purchaseValueOrSaleValue) {
        this.purchaseValueOrSaleValue = purchaseValueOrSaleValue;
    }

    public double getDeductionOrAbatementPercent() {
        return deductionOrAbatementPercent;
    }

    public void setDeductionOrAbatementPercent(double deductionOrAbatementPercent) {
        this.deductionOrAbatementPercent = deductionOrAbatementPercent;
    }

    public int getTaxType() {
        return taxType;
    }

    public void setTaxType(int taxType) {
        this.taxType = taxType;
    }
    
    public boolean isIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public int getTermSequence() {
        return termSequence;
    }

    public void setTermSequence(int termSequence) {
        this.termSequence = termSequence;
    }

    public boolean isIncludeInTDSCalculation() {
        return includeInTDSCalculation;
    }

    public void setIncludeInTDSCalculation(boolean includeInTDSCalculation) {
        this.includeInTDSCalculation = includeInTDSCalculation;
    }
    
    public boolean isIsTermActive() {
        return isTermActive;
    }

    public void setIsTermActive(boolean isTermActive) {
        this.isTermActive = isTermActive;
    }
}
