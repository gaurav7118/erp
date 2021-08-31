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

import com.krawler.common.admin.KWLCurrency;

/**
 *
 * @author krawler-user
 */
public class DebitNotePaymentDetails {

    private String ID;
    private int srno;
    private DebitNote debitnote;
    private Receipt receipt;
    private double amountDue;
    private double amountPaid;
    private KWLCurrency fromCurrency;
    private KWLCurrency toCurrency;
    private double exchangeRateForTransaction;
    private double amountInReceiptCurrency;
    private double paidAmountInReceiptCurrency;
    private double amountInBaseCurrency;
    private double paidAmountDueInBaseCurrency;
    private double exchangeRateCurrencyToBase;
    private String description;
    private double gstCurrencyRate;
    JournalEntryDetail totalJED; // To map ReceiptDetail to related JED 

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(double amountDue) {
        this.amountDue = amountDue;
    }

    public double getAmountInBaseCurrency() {
        return amountInBaseCurrency;
    }

    public void setAmountInBaseCurrency(double amountInBaseCurrency) {
        this.amountInBaseCurrency = amountInBaseCurrency;
    }

    public double getAmountInReceiptCurrency() {
        return amountInReceiptCurrency;
    }

    public void setAmountInReceiptCurrency(double amountInReceiptCurrency) {
        this.amountInReceiptCurrency = amountInReceiptCurrency;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public DebitNote getDebitnote() {
        return debitnote;
    }

    public void setDebitnote(DebitNote debitnote) {
        this.debitnote = debitnote;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getExchangeRateCurrencyToBase() {
        return exchangeRateCurrencyToBase;
    }

    public void setExchangeRateCurrencyToBase(double exchangeRateCurrencyToBase) {
        this.exchangeRateCurrencyToBase = exchangeRateCurrencyToBase;
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

    public double getGstCurrencyRate() {
        return gstCurrencyRate;
    }

    public void setGstCurrencyRate(double gstCurrencyRate) {
        this.gstCurrencyRate = gstCurrencyRate;
    }

    public double getPaidAmountDueInBaseCurrency() {
        return paidAmountDueInBaseCurrency;
    }

    public void setPaidAmountDueInBaseCurrency(double paidAmountDueInBaseCurrency) {
        this.paidAmountDueInBaseCurrency = paidAmountDueInBaseCurrency;
    }

    public double getPaidAmountInReceiptCurrency() {
        return paidAmountInReceiptCurrency;
    }

    public void setPaidAmountInReceiptCurrency(double paidAmountInReceiptCurrency) {
        this.paidAmountInReceiptCurrency = paidAmountInReceiptCurrency;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public KWLCurrency getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(KWLCurrency toCurrency) {
        this.toCurrency = toCurrency;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public JournalEntryDetail getTotalJED() {
        return totalJED;
    }

    public void setTotalJED(JournalEntryDetail totalJED) {
        this.totalJED = totalJED;
    }
    
}
