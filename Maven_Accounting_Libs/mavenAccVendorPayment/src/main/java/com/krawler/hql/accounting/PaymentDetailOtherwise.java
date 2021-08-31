/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import java.util.Set;

/**
 *
 * @author krawler
 */
public class PaymentDetailOtherwise {

    private String ID;
    private Payment payment;
    private Account account;
    private boolean isdebit;
    private String taxJedId;
    private double amount;
    private double taxamount;
    private double tdsamount;//For India Country Specific
    private String description;
    private Tax tax;
    int srNoForRow;
    private Tax gstapplied;
    JournalEntryDetail totalJED; // To map PaymentDetailOtherwise to related JED 
    JournalEntryDetail gstJED; // To map GST to related JED
    private Set<TdsDetails> tdsdetails;

    public int getSrNoForRow() {
        return srNoForRow;
    }

    public void setSrNoForRow(int srNoForRow) {
        this.srNoForRow = srNoForRow;
    }

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

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
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
    
    public String getPDOtherwiseDescription() {
        return getDescription();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Tax getGstapplied() {
        return gstapplied;
    }

    public void setGstapplied(Tax gstapplied) {
        this.gstapplied = gstapplied;
    }
    
    public JournalEntryDetail getGstJED() {
        return gstJED;
    }

    public void setGstJED(JournalEntryDetail gstJED) {
        this.gstJED = gstJED;
    }

    public JournalEntryDetail getTotalJED() {
        return totalJED;
    }

    public void setTotalJED(JournalEntryDetail totalJED) {
        this.totalJED = totalJED;
    }
    public double getTdsamount() {
        return tdsamount;
    }
    public void setTdsamount(double tdsamount) {
        this.tdsamount = tdsamount;
    }
    
    public Set<TdsDetails> getTdsdetails() {
        return tdsdetails;
    }

    public void setTdsdetails(Set<TdsDetails> tdsdetails) {
        this.tdsdetails = tdsdetails;
    }
    
}
