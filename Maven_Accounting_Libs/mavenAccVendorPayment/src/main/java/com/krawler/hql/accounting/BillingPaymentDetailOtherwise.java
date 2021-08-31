/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class BillingPaymentDetailOtherwise {

    private String ID;
    private BillingPayment billingPayment;
    private Account account;
    private boolean isdebit;
    private String taxJedId;
    private double amount;
    private double taxamount;
    private String description;
    private Tax tax;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public boolean isIsdebit() {
        return isdebit;
    }

    public void setIsdebit(boolean isdebit) {
        this.isdebit = isdebit;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public BillingPayment getBillingPayment() {
        return billingPayment;
    }

    public void setBillingPayment(BillingPayment billingPayment) {
        this.billingPayment = billingPayment;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public String getTaxJedId() {
        return taxJedId;
    }

    public void setTaxJedId(String taxJeId) {
        this.taxJedId = taxJeId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public double getTaxamount() {
        return taxamount;
    }

    public void setTaxamount(double taxamount) {
        this.taxamount = taxamount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
