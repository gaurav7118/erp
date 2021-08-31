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
public class PurchaseReturnTermsMap {
    
    private String id;
    private PurchaseReturn purchasereturn;
    private InvoiceTermsSales term;
    private double termamount;
    private double termamountinbase;
    private double percentage;
    private Integer deleted;
    private Long createdOn;
    private User creator;
    private Tax termtax;
    private double termtaxamount;
    private double termtaxamountinbase;
    private double termAmountExcludingTax;
    private double termAmountExcludingTaxInBase;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PurchaseReturn getPurchasereturn() {
        return purchasereturn;
    }

    public void setPurchasereturn(PurchaseReturn purchasereturn) {
        this.purchasereturn = purchasereturn;
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
