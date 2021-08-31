/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class TaxInvoiceSubReport {
    
    String sNo ="";
    String noOfPkg ="";
    String productDescription ="";
    String code;
    String name;
    String qty ="";
    String uPrice ="";
    String productAmount ="";
    String amountCurrency ="";
    String uom ="";
    String podono = "";
    String condition = "";
    String costome = "";
    String discountamount="";
    String discountpercent="";
    String taxCode = "";
    String taxamount = "";
    String rateLabel = "";
    String actualQuantity = "";

    public String getRateLabel() {
        return rateLabel;
    }

    public void setRateLabel(String rateLabel) {
        this.rateLabel = rateLabel;
    }
    
    public String getCostome() {
        return costome;
    }

    public void setCostome(String costome) {
        this.costome = costome;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getPodono() {
        return podono;
    }

    public void setPodono(String podono) {
        this.podono = podono;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNoOfPkg() {
        return noOfPkg;
    }

    public void setNoOfPkg(String noOfPkg) {
        this.noOfPkg = noOfPkg;
    }

    public String getProductAmount() {
        return productAmount;
    }

    public void setProductAmount(String productAmount) {
        this.productAmount = productAmount;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getsNo() {
        return sNo;
    }

    public void setsNo(String sNo) {
        this.sNo = sNo;
    }

    public String getuPrice() {
        return uPrice;
    }

    public void setuPrice(String uPrice) {
        this.uPrice = uPrice;
    }
    
    public String getAmountCurrency() {
        return amountCurrency;
    }

    public void setAmountCurrency(String amountCurrency) {
        this.amountCurrency = amountCurrency;
    }

    public String getDiscountamount() {
        return discountamount;
    }

    public void setDiscountamount(String discountamount) {
        this.discountamount = discountamount;
    }

    public String getDiscountpercent() {
        return discountpercent;
    }

    public void setDiscountpercent(String discountpercent) {
        this.discountpercent = discountpercent;
    }
    
    public String getTaxCode() {
        return taxCode;
}

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getTaxamount() {
        return taxamount;
    }

    public void setTaxamount(String taxamount) {
        this.taxamount = taxamount;
    }

    public String getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(String actualQuantity) {
        this.actualQuantity = actualQuantity;
    }
    
}
