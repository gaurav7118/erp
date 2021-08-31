/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.customreports;

import java.util.List;

/**
 *
 * @author krawler
 */
public class EWayRecordDetails {

    public int itemNo;
    public String productName;
    public String productDesc;
    public int hsnCode;
    public double quantity;
    public String qtyUnit;
    public double taxableAmount;
    public double sgstRate;
    public double cgstRate;
    public double igstRate;
    public double cessRate;

    public int getItemNo() {
        return itemNo;
    }

    public void setItemNo(int itemNo) {
        this.itemNo = itemNo;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public int getHsnCode() {
        return hsnCode;
    }

    public void setHsnCode(int hsnCode) {
        this.hsnCode = hsnCode;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getQtyUnit() {
        return qtyUnit;
    }

    public void setQtyUnit(String qtyUnit) {
        this.qtyUnit = qtyUnit;
    }

    public double getTaxableAmount() {
        return taxableAmount;
    }

    public void setTaxableAmount(double taxableAmount) {
        this.taxableAmount = taxableAmount;
    }

    public double getSgstRate() {
        return sgstRate;
    }

    public void setSgstRate(double sgstRate) {
        this.sgstRate = sgstRate;
    }

    public double getCgstRate() {
        return cgstRate;
    }

    public void setCgstRate(double cgstRate) {
        this.cgstRate = cgstRate;
    }

    public double getIgstRate() {
        return igstRate;
    }

    public void setIgstRate(double igstRate) {
        this.igstRate = igstRate;
    }

    public double getCessRate() {
        return cessRate;
    }

    public void setCessRate(double cessRate) {
        this.cessRate = cessRate;
    }

    @Override
    public String toString() {
        return "{" + "\"itemNo\":" + itemNo + ",\"productName\":\"" + productName + "\",\"productDesc\":\"" + productDesc + "\",\"hsnCode\":" + hsnCode + ",\"quantity\":" + quantity + ",\"qtyUnit\":" + qtyUnit + 
                ",\"taxableAmount\":" + taxableAmount + ",\"sgstRate\":" + sgstRate + ",\"cgstRate\":" + cgstRate + ",\"igstRate\":" + igstRate + ",\"cessRate\":" + cessRate +  '}';
    }
    
}
