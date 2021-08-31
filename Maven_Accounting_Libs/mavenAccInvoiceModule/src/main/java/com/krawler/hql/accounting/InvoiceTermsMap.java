package com.krawler.hql.accounting;

import com.krawler.common.admin.User;

public class InvoiceTermsMap {

    private String id;
    private Invoice invoice;
    private InvoiceTermsSales term;
    private InvoiceDetail invoicedetails;//To calculate line level term amount & term tax //SDP-12509
    private double termamount;
    private double percentage;
    private Integer deleted;
    private Long createdOn;
    private User creator;
    private double termtaxamount;
    private double termtaxamountinbase;
    private double termAmountExcludingTax;
    private Tax termtax;
    private double termAmountExcludingTaxInBase;
    private double termamountinbase;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public InvoiceTermsSales getTerm() {
        return term;
    }

    public void setTerm(InvoiceTermsSales term) {
        this.term = term;
    }
    
    
    public InvoiceDetail getInvoicedetails() {
        return invoicedetails;
    }

    public void setInvoicedetails(InvoiceDetail invoicedetails) {
        this.invoicedetails = invoicedetails;
    }
    
    public double getTermamount() {
        return termamount;
    }

    public void setTermamount(double termamount) {
        this.termamount = termamount;
    }
    
    public double getTermtaxamount() {
        return termtaxamount;
    }

    public void setTermtaxamount(double termtaxamount) {
        this.termtaxamount = termtaxamount;
    }
    
    public double getTermtaxamountinbase() {
        return termtaxamountinbase;
    }

    public void setTermtaxamountinbase(double termtaxamountinbase) {
        this.termtaxamountinbase = termtaxamountinbase;
    }
    
    public Tax getTermtax() {
        return termtax;
    }

    public void setTermtax(Tax termtax) {
        this.termtax = termtax;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }
    
    public double getTermAmountExcludingTax() {
        return termAmountExcludingTax;
    }

    public void setTermAmountExcludingTax(double termAmountExcludingTax) {
        this.termAmountExcludingTax = termAmountExcludingTax;
    }
    
    public double getTermAmountExcludingTaxInBase() {
        return termAmountExcludingTaxInBase;
    }

    public void setTermAmountExcludingTaxInBase(double termAmountExcludingTaxInBase) {
        this.termAmountExcludingTaxInBase = termAmountExcludingTaxInBase;
    }

    public double getTermamountinbase() {
        return termamountinbase;
    }

    public void setTermamountinbase(double termamountinbase) {
        this.termamountinbase = termamountinbase;
    }
}
