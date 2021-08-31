/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author Suhas C
 *
 * This table is used for TDS/TCS feature of Indian GST It stores mapping of
 * invoice, its linked receipt, TDS/TCS JE posted for such invoices
 */
public class ReceiptInvoiceJEMapping {

    private String ID;
    private Invoice invoice;
    private Receipt receipt;
    private JournalEntry journalEntry;
    private JournalEntry gstAdjustment;
    private Company company;
    private double invoiceamountdue;
    private double invoiceamountdueinbase;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public JournalEntry getJournalEntry() {
        return journalEntry;
    }

    public void setJournalEntry(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public JournalEntry getGstAdjustment() {
        return gstAdjustment;
    }

    public void setGstAdjustment(JournalEntry gstAdjustment) {
        this.gstAdjustment = gstAdjustment;
    }

    public double getInvoiceamountdue() {
        return invoiceamountdue;
    }

    public void setInvoiceamountdue(double invoiceamountdue) {
        this.invoiceamountdue = invoiceamountdue;
    }

    public double getInvoiceamountdueinbase() {
        return invoiceamountdueinbase;
    }

    public void setInvoiceamountdueinbase(double invoiceamountdueinbase) {
        this.invoiceamountdueinbase = invoiceamountdueinbase;
    }

}
