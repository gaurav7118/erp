/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class ContraPaymentVoucherSubReport {
    	String accountName= "";
	String accountCode= "";
	String amount= "";
	String gstTaxable= "";
	String description= "";
	String dimensionValue= "";
	String totalAmount= "";
	String dimentionName= "";
	String dbtax= "";
	String crtax= "";
	String credit= "";
        String debit= "";
	String paymentId= "";
	String classDimension= "";
        String colon="";
        String amountInWords="";

    public String getAmountInWords() {
        return amountInWords;
    }

    public void setAmountInWords(String amountInWords) {
        this.amountInWords = amountInWords;
    }

    public String getColon() {
        return colon;
    }

    public void setColon(String colon) {
        this.colon = colon;
    }
    
    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getCrtax() {
        return crtax;
    }

    public void setCrtax(String crtax) {
        this.crtax = crtax;
    }

    public String getDbtax() {
        return dbtax;
    }

    public void setDbtax(String dbtax) {
        this.dbtax = dbtax;
    }

    public String getDebit() {
        return debit;
    }

    public void setDebit(String debit) {
        this.debit = debit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDimensionValue() {
        return dimensionValue;
    }

    public void setDimensionValue(String dimensionValue) {
        this.dimensionValue = dimensionValue;
    }

    public String getDimentionName() {
        return dimentionName;
    }

    public void setDimentionName(String dimentionName) {
        this.dimentionName = dimentionName;
    }

    public String getGstTaxable() {
        return gstTaxable;
    }

    public void setGstTaxable(String gstTaxable) {
        this.gstTaxable = gstTaxable;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getClassDimension() {
        return classDimension;
    }

    public void setClassDimension(String classDimension) {
        this.classDimension = classDimension;
    }
}
