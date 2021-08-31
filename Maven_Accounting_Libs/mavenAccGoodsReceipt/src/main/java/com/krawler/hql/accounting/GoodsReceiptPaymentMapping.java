/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.hql.accounting;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.LocationBatchDocumentMapping;
import com.krawler.common.admin.User;

/**
 *
 * @author krawler
 * Mapping Between Advance Payments & Goods Receipt For TDS Calculation in Goods Receipt
 */
public class GoodsReceiptPaymentMapping {
    private String ID;
    private String paymentid;
    private double tdsAmount;
    private double tdsAmountDue;
    private GoodsReceipt goodsreceiptid;
    private KWLCurrency currency;

    public String getID() {
        return ID;
    }
    public void setID(String ID) {
        this.ID = ID;
    }

    public String getPaymentid() {
        return paymentid;
    }
    public void setPaymentid(String paymentid) {
        this.paymentid = paymentid;
    }

    public GoodsReceipt getGoodsreceiptid() {
        return goodsreceiptid;
    }
    public void setGoodsreceiptid(GoodsReceipt goodsreceiptid) {
        this.goodsreceiptid = goodsreceiptid;
    }

    public double getTdsAmount() {
        return tdsAmount;
    }
    public void setTdsAmount(double tdsAmount) {
        this.tdsAmount = tdsAmount;
    }

    public double getTdsAmountDue() {
        return tdsAmountDue;
    }

    public void setTdsAmountDue(double tdsAmountDue) {
        this.tdsAmountDue = tdsAmountDue;
    }

    public KWLCurrency getCurrency() {
        return currency;
    }
    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }
}
