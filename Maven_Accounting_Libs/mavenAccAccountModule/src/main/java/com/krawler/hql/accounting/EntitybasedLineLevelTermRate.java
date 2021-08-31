/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.FieldComboData;
import java.util.Date;

/**
 *
 * @author Suhas Chaware
 * ERP-32829 
 * This table contains all entity >> Shipped Location wise GST rates with
 * Applied date
 */
public class EntitybasedLineLevelTermRate {

    private String id;
    private LineLevelTerms lineLevelTerms;   // GST master
    private FieldComboData entity;        // Entity dimension
    private FieldComboData shippedLoc1;  // To location dimension
    private FieldComboData shippedLoc2;
    private FieldComboData shippedLoc3;
    private FieldComboData shippedLoc4;
    private FieldComboData shippedLoc5;
    private Date appliedDate;
    private double percentage;
    private double termAmount;
    private int taxType; // Tax Type = 0 for flat amount or = 1 if it is in percentage.
    private GSTCessRuleType cessType; 
    private double valuationAmount;
    private boolean isMerchantExporter;//For India Country Only.

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LineLevelTerms getLineLevelTerms() {
        return lineLevelTerms;
    }

    public void setLineLevelTerms(LineLevelTerms lineLevelTerms) {
        this.lineLevelTerms = lineLevelTerms;
    }

    public FieldComboData getEntity() {
        return entity;
    }

    public void setEntity(FieldComboData entity) {
        this.entity = entity;
    }

    public Date getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(Date appliedDate) {
        this.appliedDate = appliedDate;
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

    public int getTaxType() {
        return taxType;
    }

    public void setTaxType(int taxType) {
        this.taxType = taxType;
    }

    public FieldComboData getShippedLoc1() {
        return shippedLoc1;
    }

    public void setShippedLoc1(FieldComboData shippedLoc1) {
        this.shippedLoc1 = shippedLoc1;
    }

    public FieldComboData getShippedLoc2() {
        return shippedLoc2;
    }

    public void setShippedLoc2(FieldComboData shippedLoc2) {
        this.shippedLoc2 = shippedLoc2;
    }

    public FieldComboData getShippedLoc3() {
        return shippedLoc3;
    }

    public void setShippedLoc3(FieldComboData shippedLoc3) {
        this.shippedLoc3 = shippedLoc3;
    }

    public FieldComboData getShippedLoc4() {
        return shippedLoc4;
    }

    public void setShippedLoc4(FieldComboData shippedLoc4) {
        this.shippedLoc4 = shippedLoc4;
    }

    public FieldComboData getShippedLoc5() {
        return shippedLoc5;
    }

    public void setShippedLoc5(FieldComboData shippedLoc5) {
        this.shippedLoc5 = shippedLoc5;
    }

    public GSTCessRuleType getCessType() {
        return cessType;
    }

    public void setCessType(GSTCessRuleType cessType) {
        this.cessType = cessType;
    }

    public double getValuationAmount() {
        return valuationAmount;
    }

    public void setValuationAmount(double valuationAmount) {
        this.valuationAmount = valuationAmount;
    }

    public boolean isIsMerchantExporter() {
        return isMerchantExporter;
    }

    public void setIsMerchantExporter(boolean isMerchantExporter) {
        this.isMerchantExporter = isMerchantExporter;
    }
  
}
