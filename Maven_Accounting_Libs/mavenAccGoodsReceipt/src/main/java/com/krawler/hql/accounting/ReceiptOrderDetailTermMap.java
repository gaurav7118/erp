/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.User;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class ReceiptOrderDetailTermMap {

    private String id;
    private GoodsReceiptOrderDetails grodetail;
    private LineLevelTerms term;
    private double termamount;
    private double percentage;
    private double assessablevalue;
    private Integer deleted;
    private Date createdOn;
    private User creator;
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

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public GoodsReceiptOrderDetails getGrodetail() {
        return grodetail;
    }

    public void setGrodetail(GoodsReceiptOrderDetails grodetail) {
        this.grodetail = grodetail;
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
    public double getAssessablevalue() {
        return assessablevalue;
    }

    public void setAssessablevalue(double assessablevalue) {
        this.assessablevalue = assessablevalue;
    }
}
