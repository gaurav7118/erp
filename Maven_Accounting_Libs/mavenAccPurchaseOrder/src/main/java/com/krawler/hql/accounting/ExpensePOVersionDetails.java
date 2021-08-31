/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class ExpensePOVersionDetails {
    private String ID;
    private int srno;
    private double rate;
    private double amount;
    private String description;
    private boolean isdebit;
    private double rateIncludingGst;
    private double rowTaxAmount;
    private Company company;    
    private PurchaseOrderVersion purchaseOrderVersion;
    private ExpensePOVersionDetailCustomData expensePOVersionDetailCustomData;
    private Tax tax;
    private Account account;
    private Discount discount;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public boolean isIsdebit() {
        return isdebit;
    }

    public void setIsdebit(boolean isdebit) {
        this.isdebit = isdebit;
    }

    public PurchaseOrderVersion getPurchaseOrderVersion() {
        return purchaseOrderVersion;
    }

    public void setPurchaseOrderVersion(PurchaseOrderVersion purchaseOrderVersion) {
        this.purchaseOrderVersion = purchaseOrderVersion;
    }

    public ExpensePOVersionDetailCustomData getExpensePOVersionDetailCustomData() {
        return expensePOVersionDetailCustomData;
    }

    public void setExpensePOVersionDetailCustomData(ExpensePOVersionDetailCustomData expensePOVersionDetailCustomData) {
        this.expensePOVersionDetailCustomData = expensePOVersionDetailCustomData;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getRateIncludingGst() {
        return rateIncludingGst;
    }

    public void setRateIncludingGst(double rateIncludingGst) {
        this.rateIncludingGst = rateIncludingGst;
    }

    public double getRowTaxAmount() {
        return rowTaxAmount;
    }

    public void setRowTaxAmount(double rowTaxAmount) {
        this.rowTaxAmount = rowTaxAmount;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }
   
}
