/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.User;

/**
 *
 * @author krawler
 */
public class QuotationTermMap {

    private String id;
    private Quotation quotation;
    private InvoiceTermsSales term;
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

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public Quotation getQuotation() {
        return quotation;
    }

    public void setQuotation(Quotation quotation) {
        this.quotation = quotation;
    }

    public InvoiceTermsSales getTerm() {
        return term;
    }

    public void setTerm(InvoiceTermsSales term) {
        this.term = term;
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
    public double getTermAmountExcludingTax() {
        return termAmountExcludingTax;
    }

    public void setTermAmountExcludingTax(double termAmountExcludingTax) {
        this.termAmountExcludingTax = termAmountExcludingTax;
    }
    public double getTermamountinbase() {
        return termamountinbase;
    }

    public void setTermamountinbase(double termamountinbase) {
        this.termamountinbase = termamountinbase;
    }

    public double getTermAmountExcludingTaxInBase() {
        return termAmountExcludingTaxInBase;
    }

    public void setTermAmountExcludingTaxInBase(double termAmountExcludingTaxInBase) {
        this.termAmountExcludingTaxInBase = termAmountExcludingTaxInBase;
    }

}
