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
public class LinkDetailPaymentToAdvancePayment {

    private String ID;
    private int srno;
    private Receipt receipt;
    private Payment payment;
    private double amount;
    private Company company;
    private KWLCurrency fromCurrency;
    private KWLCurrency toCurrency;
    private double exchangeRateForTransaction;
    private double amountInPaymentCurrency;
    private String linkedGainLossJE;
    private Date paymentLinkDate;
    private String revalJeId; // for maintaing relation between realised JE and debit note 
    private String revalJeIdReceipt; // for maintaing relation between realised JE and Receipt

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

    public double getAmountInPaymentCurrency() {
        return amountInPaymentCurrency;
    }

    public void setAmountInPaymentCurrency(double amountInPaymentCurrency) {
        this.amountInPaymentCurrency = amountInPaymentCurrency;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
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

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public Date getPaymentLinkDate() {
        return paymentLinkDate;
    }

    public void setPaymentLinkDate(Date paymentLinkDate) {
        this.paymentLinkDate = paymentLinkDate;
    }

    public String getRevalJeId() {
        return revalJeId;
    }

    public void setRevalJeId(String revalJeId) {
        this.revalJeId = revalJeId;
    }

    public String getRevalJeIdReceipt() {
        return revalJeIdReceipt;
    }

    public void setRevalJeIdReceipt(String revalJeIdReceipt) {
        this.revalJeIdReceipt = revalJeIdReceipt;
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
}