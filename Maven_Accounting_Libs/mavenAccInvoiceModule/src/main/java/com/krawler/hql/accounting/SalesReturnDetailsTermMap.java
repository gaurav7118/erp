
package com.krawler.hql.accounting;

import com.krawler.common.admin.User;
import com.krawler.hql.accounting.InvoiceTermsSales;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.SalesReturnDetail;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class SalesReturnDetailsTermMap {
    private String id;
    private SalesReturnDetail salesreturndetail;
    private LineLevelTerms term;
    private double termamount;
    private double percentage;
    private Integer deleted;
    private Date createdOn;
    private User creator;
    private Product product;
    private double assessablevalue;
    private double purchaseValueOrSaleValue;
    private int taxPaidFlag;
    private JournalEntry taxPaymentJE;
    private String taxMakePayment;
    private double deductionOrAbatementPercent;
    private int taxType; // Tax Type = 0 for flat amount or = 1 if it is in percentage.
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
    public double getAssessablevalue() {
        return assessablevalue;
    }

    public void setAssessablevalue(double assessablevalue) {
        this.assessablevalue = assessablevalue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SalesReturnDetail getSalesreturndetail() {
        return salesreturndetail;
    }

    public void setSalesreturndetail(SalesReturnDetail salesreturndetail) {
        this.salesreturndetail = salesreturndetail;
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

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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
    
}
