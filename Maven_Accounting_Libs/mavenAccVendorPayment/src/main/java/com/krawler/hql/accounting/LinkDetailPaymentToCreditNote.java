/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class LinkDetailPaymentToCreditNote {
    private String ID;
    private int srno;
    private CreditNote creditnote;
    private Payment payment;
    private double amount;//
    private Company company;
    private KWLCurrency fromCurrency;
    private KWLCurrency toCurrency;
    private double exchangeRateForTransaction;
    private double amountInCNCurrency;
    private String linkedGainLossJE;
    private Date paymentLinkDate;
    private String revalJeId;  //for maintaing relation between realised JE and Creadit Note 
    private String revalJeIdPayment; //for maintaing relation between realised JE and Payment 

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAmountInCNCurrency() {
        return amountInCNCurrency;
    }

    public void setAmountInCNCurrency(double amountInCNCurrency) {
        this.amountInCNCurrency = amountInCNCurrency;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public CreditNote getCreditnote() {
        return creditnote;
    }

    public void setCreditnote(CreditNote creditnote) {
        this.creditnote = creditnote;
    }

    public double getExchangeRateForTransaction() {
        return exchangeRateForTransaction;
    }

    public void setExchangeRateForTransaction(double exchangeRateForTransaction) {
        this.exchangeRateForTransaction = exchangeRateForTransaction;
    }

    public KWLCurrency getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(KWLCurrency fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public String getLinkedGainLossJE() {
        return linkedGainLossJE;
    }

    public void setLinkedGainLossJE(String linkedGainLossJE) {
        this.linkedGainLossJE = linkedGainLossJE;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Date getPaymentLinkDate() {
        return paymentLinkDate;
    }

    public void setPaymentLinkDate(Date paymentLinkDate) {
        this.paymentLinkDate = paymentLinkDate;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public KWLCurrency getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(KWLCurrency toCurrency) {
        this.toCurrency = toCurrency;
    }

    public String getRevalJeId() {
        return revalJeId;
    }

    public void setRevalJeId(String revalJeId) {
        this.revalJeId = revalJeId;
    }

    public String getRevalJeIdPayment() {
        return revalJeIdPayment;
    }

    public void setRevalJeIdPayment(String revalJeIdPayment) {
        this.revalJeIdPayment = revalJeIdPayment;
    }
    
}

