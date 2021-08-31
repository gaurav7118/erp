/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class LinkDetailReceipt {
    private String ID;
    private int srno;
    private GoodsReceipt goodsReceipt;
    private Invoice invoice;
    private Receipt receipt;
    private double amount;//
    private Company company;
    private KWLCurrency fromCurrency;
    private KWLCurrency toCurrency;
    private double exchangeRateForTransaction;
    private double amountInInvoiceCurrency;
    private String linkedGainLossJE;
    private String linkedGSTJE;   // Used for advance and Invoice linking case (India)
    private String ROWJEDID;// Used only when custom 
    private Date receiptLinkDate;
    private String revalJeId;  //for maintaing relation between realised JE and Invoice 
    private String revalJeIdReceipt;  //for maintaing relation between realised JE and Receipt 
//    private AdvanceDetail advanceDetail;// Used only when custom 

    public String getLinkedGSTJE() {
        return linkedGSTJE;
    }

    public void setLinkedGSTJE(String linkedGSTJE) {
        this.linkedGSTJE = linkedGSTJE;
    }
    public Date getReceiptLinkDate() {
        return receiptLinkDate;
    }

    public void setReceiptLinkDate(Date receiptLinkDate) {
        this.receiptLinkDate = receiptLinkDate;
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

    public double getAmountInInvoiceCurrency() {
        return amountInInvoiceCurrency;
    }

    public void setAmountInInvoiceCurrency(double amountInInvoiceCurrency) {
        this.amountInInvoiceCurrency = amountInInvoiceCurrency;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

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

    public String getRevalJeIdReceipt() {
        return revalJeIdReceipt;
    }

    public void setRevalJeIdReceipt(String revalJeIdReceipt) {
        this.revalJeIdReceipt = revalJeIdReceipt;
    }
    
}
