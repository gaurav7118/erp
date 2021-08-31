/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import java.util.Date;
import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class InvoiceWriteOff {
    private String ID;
    private Invoice invoice;
    private JournalEntry journalEntry;                // JE posted while writing off
    private JournalEntry reversejournalEntry;         // JE posted while recovering
    private double writtenOffAmountInInvoiceCurrency;
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

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public JournalEntry getJournalEntry() {
        return journalEntry;
    }

    public void setJournalEntry(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
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

    public double getWrittenOffAmountInInvoiceCurrency() {
        return writtenOffAmountInInvoiceCurrency;
    }

    public void setWrittenOffAmountInInvoiceCurrency(double writtenOffAmountInInvoiceCurrency) {
        this.writtenOffAmountInInvoiceCurrency = writtenOffAmountInInvoiceCurrency;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getMemo() {
        return memo;
    }

    public boolean isIsRecovered() {
        return isRecovered;
    }

    public void setIsRecovered(boolean isRecovered) {
        this.isRecovered = isRecovered;
    }

    public JournalEntry getReversejournalEntry() {
        return reversejournalEntry;
    }

    public void setReversejournalEntry(JournalEntry reversejournalEntry) {
        this.reversejournalEntry = reversejournalEntry;
    }
   
 
}
