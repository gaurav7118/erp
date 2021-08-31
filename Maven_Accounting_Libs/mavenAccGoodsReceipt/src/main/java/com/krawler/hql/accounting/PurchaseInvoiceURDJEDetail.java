/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * Authot - Rahul Bhawar
 *
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Set;

/**
 * Used for store Purchase Invoice Tax Journal Entry Details for Un-Registered Vendor 
 * To check daily limit cross if cross then Tax JE details should be updated for that Bill Date
 * if not then tax JE Details delete for that Bill Date
 * 
 * 
 
 Account Name  
                               Amount Debit       Amount Credit      Description
  Advance CGST             Dr   360.00                               Debit Tax JE Details  (Payable Account)
  Advance SGST             Dr   360.00                               Debit Tax JE Details  (Payable Account)
  Purchases                Dr   6,000.00                             Debit JE Details   
  
                 To Output CGST                    360.00            Credit Tax JE Details  
                 To Output SGST                    360.00            Credit Tax JE Details   
                 To Trade Creditors                6,000.00          Credit JE Details   
*
* @author Rahul Bhawar
 */
public class PurchaseInvoiceURDJEDetail {

    private String ID;
    private double invoiceAmountInBase; // Invocie Amount in base currency
    private long billdate; // creation Date
    private Company company;  // Company id
    private GoodsReceiptDetail goodsReceiptDetail; // Purchase Invoice Details ID
    private JournalEntryDetail entryDetaildebit; // Journal Entry detail ID of type Debit
    private JournalEntryDetail entryDetailcredit;// Journal Entry detail ID type Credit 
    private LineLevelTerms term; // Line level Term id
    private double termamountInBase;  // Term Amount in Base currency

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getInvoiceAmountInBase() {
        return invoiceAmountInBase;
    }

    public void setInvoiceAmountInBase(double invoiceAmountInBase) {
        this.invoiceAmountInBase = invoiceAmountInBase;
    }

    public long getBilldate() {
        return billdate;
    }

    public void setBilldate(long billdate) {
        this.billdate = billdate;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public GoodsReceiptDetail getGoodsReceiptDetail() {
        return goodsReceiptDetail;
    }

    public void setGoodsReceiptDetail(GoodsReceiptDetail goodsReceiptDetail) {
        this.goodsReceiptDetail = goodsReceiptDetail;
    }

    public JournalEntryDetail getEntryDetaildebit() {
        return entryDetaildebit;
    }

    public void setEntryDetaildebit(JournalEntryDetail entryDetaildebit) {
        this.entryDetaildebit = entryDetaildebit;
    }

    public JournalEntryDetail getEntryDetailcredit() {
        return entryDetailcredit;
    }

    public void setEntryDetailcredit(JournalEntryDetail entryDetailcredit) {
        this.entryDetailcredit = entryDetailcredit;
    }

    public LineLevelTerms getTerm() {
        return term;
    }

    public void setTerm(LineLevelTerms term) {
        this.term = term;
    }

    public double getTermamountInBase() {
        return termamountInBase;
    }

    public void setTermamountInBase(double termamountInBase) {
        this.termamountInBase = termamountInBase;
    }

}
