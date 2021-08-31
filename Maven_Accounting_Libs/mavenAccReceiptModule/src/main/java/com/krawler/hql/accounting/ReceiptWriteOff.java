/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class ReceiptWriteOff {
    private String ID;
    private Receipt receipt;
    private JournalEntry journalEntry;                // JE posted while writing off
    private JournalEntry reversejournalEntry;         // JE posted while recovering
    private double writtenOffAmountInReceiptCurrency;
    private double writtenOffAmountInBaseCurrency;
    private Date writeOffDate;
    private Company company;
    private String memo;
    private boolean isRecovered;                      // Will be true when recovered.

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isIsRecovered() {
        return isRecovered;
    }

    public void setIsRecovered(boolean isRecovered) {
        this.isRecovered = isRecovered;
    }

    public JournalEntry getJournalEntry() {
        return journalEntry;
    }

    public void setJournalEntry(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public JournalEntry getReversejournalEntry() {
        return reversejournalEntry;
    }

    public void setReversejournalEntry(JournalEntry reversejournalEntry) {
        this.reversejournalEntry = reversejournalEntry;
    }

    public Date getWriteOffDate() {
        return writeOffDate;
    }

    public void setWriteOffDate(Date writeOffDate) {
        this.writeOffDate = writeOffDate;
    }

    public double getWrittenOffAmountInBaseCurrency() {
        return writtenOffAmountInBaseCurrency;
    }

    public void setWrittenOffAmountInBaseCurrency(double writtenOffAmountInBaseCurrency) {
        this.writtenOffAmountInBaseCurrency = writtenOffAmountInBaseCurrency;
    }

    public double getWrittenOffAmountInReceiptCurrency() {
        return writtenOffAmountInReceiptCurrency;
    }

    public void setWrittenOffAmountInReceiptCurrency(double writtenOffAmountInReceiptCurrency) {
        this.writtenOffAmountInReceiptCurrency = writtenOffAmountInReceiptCurrency;
    }
    
    
}
