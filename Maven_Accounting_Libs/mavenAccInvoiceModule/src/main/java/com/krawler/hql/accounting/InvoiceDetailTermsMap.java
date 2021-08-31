package com.krawler.hql.accounting;

import com.krawler.common.admin.User;

public class InvoiceDetailTermsMap {

    private String id;
    private InvoiceDetail invoicedetail;
    private LineLevelTerms term;
    private double termamount;
    private double percentage;
    private double assessablevalue;
    private Integer deleted;
    private Long createdOn;
    private User creator;
    private double purchaseValueOrSaleValue;
    private double deductionOrAbatementPercent;
    private int taxType; // Tax Type = 0 for flat amount or = 1 if it is in percentage.
    private int taxPaidFlag;
    private int creditAvailedFlag;
    private int creditAvailedFlagServiceTax;
    private JournalEntry taxPaymentJE;
    private String taxMakePayment;
    private boolean isGSTApplied;//For India Country Only.
    private EntitybasedLineLevelTermRate entitybasedLineLevelTermRate;
    public EntitybasedLineLevelTermRate getEntitybasedLineLevelTermRate() {
        return entitybasedLineLevelTermRate;
    }

    public void setEntitybasedLineLevelTermRate(EntitybasedLineLevelTermRate entitybasedLineLevelTermRate) {
        this.entitybasedLineLevelTermRate = entitybasedLineLevelTermRate;
    }

    public boolean isIsGSTApplied() {
        return isGSTApplied;
    }

    public void setIsGSTApplied(boolean isGSTApplied) {
        this.isGSTApplied = isGSTApplied;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public LineLevelTerms getTerm() {
        return term;
    }

    public void setTerm(LineLevelTerms term) {
        this.term = term;
    }

    public double getTermamount() {
        return termamount;
    }

    public void setTermamount(double termamount) {
        this.termamount = termamount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
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

    public InvoiceDetail getInvoicedetail() {
        return invoicedetail;
    }

    public void setInvoicedetail(InvoiceDetail invoicedetail) {
        this.invoicedetail = invoicedetail;
    }

    public double getAssessablevalue() {
        return assessablevalue;
    }
    public void setAssessablevalue(double assessablevalue) {
        this.assessablevalue = assessablevalue;
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

    public int getTaxPaidFlag() {
        return taxPaidFlag;
    }

    public void setTaxPaidFlag(int taxPaidFlag) {
        this.taxPaidFlag = taxPaidFlag;
    }

    public JournalEntry getTaxPaymentJE() {
        return taxPaymentJE;
    }

    public void setTaxPaymentJE(JournalEntry taxPaymentJE) {
        this.taxPaymentJE = taxPaymentJE;
    }

    public String getTaxMakePayment() {
        return taxMakePayment;
    }

    public void setTaxMakePayment(String taxMakePayment) {
        this.taxMakePayment = taxMakePayment;
    }

    public int getCreditAvailedFlag() {
        return creditAvailedFlag;
    }

    public void setCreditAvailedFlag(int creditAvailedFlag) {
        this.creditAvailedFlag = creditAvailedFlag;
    }

    public int getCreditAvailedFlagServiceTax() {
        return creditAvailedFlagServiceTax;
    }
    public void setCreditAvailedFlagServiceTax(int creditAvailedFlagServiceTax) {
        this.creditAvailedFlagServiceTax = creditAvailedFlagServiceTax;
    }
}
