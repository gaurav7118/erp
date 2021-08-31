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

/**
 *
 * @author krawler-user
 */
public class ReceiptDetail {

    private String ID;
    private int srno;
    private Invoice invoice;
    private GoodsReceipt goodsReceipt;
    private Receipt receipt;
    private double amount;
    private Company company;
    private KWLCurrency fromCurrency;
    private KWLCurrency toCurrency;
    private double exchangeRateForTransaction;
    private String ROWJEDID;// Used only when custom 
    private double gstCurrencyRate;//Only used if the country is Singapore and the base currency is not SGD.
    private double amountInInvoiceCurrency;
    private double amountDueInInvoiceCurrency;
    private double amountDueInPaymentCurrency;
    private double amountInBaseCurrency;
    private double amountDueInBaseCurrency;
    private double exchangeRateCurrencyToBase;
    private String description;
    int srNoForRow;
    JournalEntryDetail totalJED; // To map ReceiptDetail to related JED 
    private double discountAmount;
    private double discountAmountInInvoiceCurrency;
    private double discountAmountInBase;
    private boolean discountFieldEdited;            //Used to identify whether discount field was edited at the time of payment creation because we need to hit the same disocunt account at the time of editing payment

    public int getSrNoForRow() {
        return srNoForRow;
    }

    public void setSrNoForRow(int srNoForRow) {
        this.srNoForRow = srNoForRow;
    }

    public String getROWJEDID() {
        return ROWJEDID;
    }

    public void setROWJEDID(String ROWJEDID) {
        this.ROWJEDID = ROWJEDID;
    }

    public GoodsReceipt getGoodsReceipt() {
        return goodsReceipt;
    }

    public void setGoodsReceipt(GoodsReceipt goodsReceipt) {
        this.goodsReceipt = goodsReceipt;
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

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
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

	public double getGstCurrencyRate() {
        return gstCurrencyRate;
    }

    public void setGstCurrencyRate(double gstCurrencyRate) {
        this.gstCurrencyRate = gstCurrencyRate;
    }

    public double getAmountDueInBaseCurrency() {
        return amountDueInBaseCurrency;
    }

    public void setAmountDueInBaseCurrency(double amountDueInBaseCurrency) {
        this.amountDueInBaseCurrency = amountDueInBaseCurrency;
    }

    public double getAmountDueInInvoiceCurrency() {
        return amountDueInInvoiceCurrency;
    }

    public void setAmountDueInInvoiceCurrency(double amountDueInInvoiceCurrency) {
        this.amountDueInInvoiceCurrency = amountDueInInvoiceCurrency;
    }

    public double getAmountDueInPaymentCurrency() {
        return amountDueInPaymentCurrency;
    }

    public void setAmountDueInPaymentCurrency(double amountDueInPaymentCurrency) {
        this.amountDueInPaymentCurrency = amountDueInPaymentCurrency;
    }

    public double getAmountInBaseCurrency() {
        return amountInBaseCurrency;
    }

    public void setAmountInBaseCurrency(double amountInBaseCurrency) {
        this.amountInBaseCurrency = amountInBaseCurrency;
    }

    public double getAmountInInvoiceCurrency() {
        return amountInInvoiceCurrency;
    }

    public void setAmountInInvoiceCurrency(double amountInInvoiceCurrency) {
        this.amountInInvoiceCurrency = amountInInvoiceCurrency;
    }

    public double getExchangeRateCurrencyToBase() {
        return exchangeRateCurrencyToBase;
    }

    public void setExchangeRateCurrencyToBase(double exchangeRateCurrencyToBase) {
        this.exchangeRateCurrencyToBase = exchangeRateCurrencyToBase;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public JournalEntryDetail getTotalJED() {
        return totalJED;
    }

    public void setTotalJED(JournalEntryDetail totalJED) {
        this.totalJED = totalJED;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getDiscountAmountInBase() {
        return discountAmountInBase;
    }

    public void setDiscountAmountInBase(double discountAmountInBase) {
        this.discountAmountInBase = discountAmountInBase;
    }

    public boolean isDiscountFieldEdited() {
        return discountFieldEdited;
    }

    public void setDiscountFieldEdited(boolean discountFieldEdited) {
        this.discountFieldEdited = discountFieldEdited;
    }

    public double getDiscountAmountInInvoiceCurrency() {
        return discountAmountInInvoiceCurrency;
    }

    public void setDiscountAmountInInvoiceCurrency(double discountAmountInInvoiceCurrency) {
        this.discountAmountInInvoiceCurrency = discountAmountInInvoiceCurrency;
    }
    
}
