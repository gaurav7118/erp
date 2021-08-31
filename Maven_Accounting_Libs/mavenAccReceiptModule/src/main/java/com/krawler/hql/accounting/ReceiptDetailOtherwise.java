
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class ReceiptDetailOtherwise {

    private String ID;
    private Receipt receipt;
    private Account account;
    private boolean isdebit;
    private String taxJedId;
    private double amount;
    private double taxamount;
    private String description;
    private Tax tax;
    int srNoForRow;
    private Tax gstapplied;
    JournalEntryDetail totalJED; // To map ReceiptDetailOtherwise to related JED 
    JournalEntryDetail gstJED; // To map GST to related JED

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

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
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
    
    public String getRDOtherwiseDescription() {
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
    
}
