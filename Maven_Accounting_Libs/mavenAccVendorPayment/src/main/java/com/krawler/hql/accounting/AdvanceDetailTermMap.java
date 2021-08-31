/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.User;

/**
 * Class is used to save linking information of Advance Payment and Line level
 * terms. It is used when in GST is applicable.
 *
 * @author swapnil.khandre
 */
public class AdvanceDetailTermMap {

    private String id;
    private String advanceDetail; // Advance Payment
    private String paymentDetailOtherwise; //  Payment against GL code
    private double termamount;
    private double percentage;
    private Integer deleted;
    private Long createdOn;
    private User creator;
    private double purchaseValueOrSaleValue;
    private double deductionOrAbatementPercent;
    private double assessablevalue;
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

    public double getTermamount() {
        return termamount;
    }

    public void setTermamount(double termamount) {
        this.termamount = termamount;
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

    public double getAssessablevalue() {
        return assessablevalue;
    }

    public String getAdvanceDetail() {
        return advanceDetail;
    }

    public void setAdvanceDetail(String advanceDetail) {
        this.advanceDetail = advanceDetail;
    }

    public void setAssessablevalue(double assessablevalue) {
        this.assessablevalue = assessablevalue;
    }

    public int getTaxType() {
        return taxType;
    }

    public void setTaxType(int taxType) {
        this.taxType = taxType;
    }

    public String getPaymentDetailOtherwise() {
        return paymentDetailOtherwise;
    }

    public void setPaymentDetailOtherwise(String paymentDetailOtherwise) {
        this.paymentDetailOtherwise = paymentDetailOtherwise;
    }
}
