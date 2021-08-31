/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class GoodsReceiptDetailPaymentMapping {
    private String ID; 
    private GoodsReceiptDetail grdetails; 
    private ExpenseGRDetail erdetails; 
    private String payment; 
    private double advanceAdjustedAmount; 
    private double PaymentAmount;
    private Company company;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public GoodsReceiptDetail getGrdetails() {
        return grdetails;
    }

    public void setGrdetails(GoodsReceiptDetail grdetails) {
        this.grdetails = grdetails;
    }

    public ExpenseGRDetail getErdetails() {
        return erdetails;
    }

    public void setErdetails(ExpenseGRDetail erdetails) {
        this.erdetails = erdetails;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public double getAdvanceAdjustedAmount() {
        return advanceAdjustedAmount;
    }

    public void setAdvanceAdjustedAmount(double advanceAdjustedAmount) {
        this.advanceAdjustedAmount = advanceAdjustedAmount;
    }

    public double getPaymentAmount() {
        return PaymentAmount;
    }

    public void setPaymentAmount(double PaymentAmount) {
        this.PaymentAmount = PaymentAmount;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
}
