/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.User;

/**
 *
 * @author krawler
 */
public class SalesOrderDetailTermMap {

    private String id;
    private SalesOrderDetail salesOrderDetail;
    private LineLevelTerms term;
    private double termamount;
    private double percentage;
    private Integer deleted;
    private double assessablevalue;
    private Long createdOn;
    private User creator;
    private Product product;
    private double purchaseValueOrSaleValue;
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

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public SalesOrderDetail getSalesOrderDetail() {
        return salesOrderDetail;
    }

    public void setSalesOrderDetail(SalesOrderDetail salesOrderDetail) {
        this.salesOrderDetail = salesOrderDetail;
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
}
