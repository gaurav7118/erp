/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.User;
import java.util.Date;

public class AccountOpeningTransaction {

    private String ID;
    private String transactionNumber;
    private String memo;
    private double depositAmount;
    private PayDetail payDetail;
    private Company company;
    private KWLCurrency currency;
    private double externalCurrencyRate;
    private Date creationDate;
    private Account account;
    private User createdby;
    private User modifiedby;
    private long createdon;
    private long updatedon;
    private double depositamountinbase;
    private boolean isPayment;
    private double exchangeRateForOpeningTransaction;
    private boolean conversionRateFromCurrencyToBase;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public User getCreatedby() {
        return createdby;
    }

    public void setCreatedby(User createdby) {
        this.createdby = createdby;
    }

    public long getCreatedon() {
        return createdon;
    }

    public void setCreatedon(long createdon) {
        this.createdon = createdon;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }

    public double getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(double depositAmount) {
        this.depositAmount = depositAmount;
    }

    public double getDepositamountinbase() {
        return depositamountinbase;
    }

    public void setDepositamountinbase(double depositamountinbase) {
        this.depositamountinbase = depositamountinbase;
    }

    public double getExternalCurrencyRate() {
        return externalCurrencyRate;
    }

    public void setExternalCurrencyRate(double externalCurrencyRate) {
        this.externalCurrencyRate = externalCurrencyRate;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public User getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(User modifiedby) {
        this.modifiedby = modifiedby;
    }

    public PayDetail getPayDetail() {
        return payDetail;
    }

    public void setPayDetail(PayDetail payDetail) {
        this.payDetail = payDetail;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public long getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(long updatedon) {
        this.updatedon = updatedon;
    }

    public boolean isIsPayment() {
        return isPayment;
    }

    public void setIsPayment(boolean isPayment) {
        this.isPayment = isPayment;
    }

    public boolean isConversionRateFromCurrencyToBase() {
        return conversionRateFromCurrencyToBase;
    }

    public void setConversionRateFromCurrencyToBase(boolean conversionRateFromCurrencyToBase) {
        this.conversionRateFromCurrencyToBase = conversionRateFromCurrencyToBase;
    }

    public double getExchangeRateForOpeningTransaction() {
        return exchangeRateForOpeningTransaction;
    }

    public void setExchangeRateForOpeningTransaction(double exchangeRateForOpeningTransaction) {
        this.exchangeRateForOpeningTransaction = exchangeRateForOpeningTransaction;
    }
}