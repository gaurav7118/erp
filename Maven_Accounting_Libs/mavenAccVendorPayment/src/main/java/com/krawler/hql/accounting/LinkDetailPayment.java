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
public class LinkDetailPayment {
    private String ID;
    private int srno;
    private GoodsReceipt goodsReceipt;
    private Invoice invoice;
    private Payment payment;
    private double amount;//
    private Company company;
    private KWLCurrency fromCurrency;
    private KWLCurrency toCurrency;
    private double exchangeRateForTransaction;
    private double amountInGrCurrency;
    private String linkedGainLossJE;    
    private String ROWJEDID;// Used only when custom 
    private Date paymentLinkDate;
    private String revalJeId;  //for maintaing relation between realised JE and Invoice 
    private String revalJeIdPayment; //for maintaing relation between realised JE and Payment 
//    private AdvanceDetail advanceDetail;// Used only when custom 
    private String linkedGSTJE;   // Used for advance and Invoice linking case (India)

    public Date getPaymentLinkDate() {
        return paymentLinkDate;
    }

    public void setPaymentLinkDate(Date paymentLinkDate) {
        this.paymentLinkDate = paymentLinkDate;
    }
        
    public String getROWJEDID() {
        return ROWJEDID;
    }

    public void setROWJEDID(String ROWJEDID) {
        this.ROWJEDID = ROWJEDID;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public GoodsReceipt getGoodsReceipt() {
        return goodsReceipt;
    }

    public void setGoodsReceipt(GoodsReceipt goodsReceipt) {
        this.goodsReceipt = goodsReceipt;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
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

    public KWLCurrency getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(KWLCurrency toCurrency) {
        this.toCurrency = toCurrency;
    }

    public double getAmountInGrCurrency() {
        return amountInGrCurrency;
    }

    public void setAmountInGrCurrency(double amountInGrCurrency) {
        this.amountInGrCurrency = amountInGrCurrency;
    }

//    public AdvanceDetail getAdvanceDetail() {
//        return advanceDetail;
//    }
//
//    public void setAdvanceDetail(AdvanceDetail advanceDetail) {
//        this.advanceDetail = advanceDetail;
//    }

    public String getLinkedGainLossJE() {
        return linkedGainLossJE;
}

    public void setLinkedGainLossJE(String linkedGainLossJE) {
        this.linkedGainLossJE = linkedGainLossJE;
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
    
    public String getLinkedGSTJE() {
        return linkedGSTJE;
    }

    public void setLinkedGSTJE(String linkedGSTJE) {
        this.linkedGSTJE = linkedGSTJE;
    }
}
