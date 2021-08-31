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
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author krawler-user
 */
public class JournalEntryDetail {

    private String ID;
    private int srno;
    private double amount;
    private JournalEntry journalEntry;
//    private Set<AccJEDetailCustomData> accJEDetailCustomData = new HashSet<AccJEDetailCustomData>();
    private AccJEDetailCustomData accJEDetailCustomData;
    private AccJEDetailsProductCustomData accJEDetailsProductCustomData;
    private Account account;
    private boolean debit;
    private Company company;
    private int accountpersontype; //1-Customer , 2-Vendor
    private String customerVendorId = "";
    private String description;
    private double gstCurrencyRate;//Only used if the country is Singapore and the base currency is not SGD.
    private double forexGainLoss;//Only used if the country is Singapore and the base currency is not SGD.
    private int paymentType;//Only used if the country is Singapore and the base currency is not SGD.
    private boolean bankcharge;         //Flag maintain to get amount is bank charges type or Cr/Dr.
    private double exchangeRateForTransaction; // Default vallue - 1 . This will be account currency to JE currency exchange rate . In case of Fund Transafer JE
    private Tax gstapplied;
    private boolean roundingDifferenceDetail;
    private boolean isSeparated; // ERP-33860 Used to find additional jedetail's of payment method 
    private String mainjedid; // ERP-33860 Used to find additional jedetail's of payment method/Customer or Vendor Account 
    private double amountinbase; // amount in base
    public boolean isRoundingDifferenceDetail() {
        return roundingDifferenceDetail;
    }

    public void setRoundingDifferenceDetail(boolean roundingDifferenceDetail) {
        this.roundingDifferenceDetail = roundingDifferenceDetail;
    }

    public boolean isIsSeparated() {
        return isSeparated;
    }

    public void setIsSeparated(boolean isSeparated) {
        this.isSeparated = isSeparated;
    }

    public String getMainjedid() {
        return mainjedid;
    }

    public void setMainjedid(String mainjedid) {
        this.mainjedid = mainjedid;
    }
    
    public boolean isBankcharge() {
        return bankcharge;
    }

    public void setBankcharge(boolean bankcharge) {
        this.bankcharge = bankcharge;
    }

    public String getDescription() {
        return description;
    }

    public String getJEDDescription() {
        return getDescription();
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public JournalEntry getJournalEntry() {
        return journalEntry;
    }

    public void setJournalEntry(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isDebit() {
        return debit;
    }

    public void setDebit(boolean debit) {
        this.debit = debit;
    }

    public AccJEDetailCustomData getAccJEDetailCustomData() {
        return accJEDetailCustomData;
    }

    public void setAccJEDetailCustomData(AccJEDetailCustomData accJEDetailCustomData) {
        this.accJEDetailCustomData = accJEDetailCustomData;
    }

    public AccJEDetailsProductCustomData getAccJEDetailsProductCustomData() {
        return accJEDetailsProductCustomData;
    }

    public void setAccJEDetailsProductCustomData(AccJEDetailsProductCustomData accJEDetailsProductCustomData) {
        this.accJEDetailsProductCustomData = accJEDetailsProductCustomData;
    }

    public int getAccountpersontype() {
        return accountpersontype;
    }

    public void setAccountpersontype(int accountpersontype) {
        this.accountpersontype = accountpersontype;
    }

    public String getCustomerVendorId() {
        return customerVendorId;
    }

    public void setCustomerVendorId(String customerVendorId) {
        this.customerVendorId = customerVendorId;
    }
    
    public double getGstCurrencyRate() {
        return gstCurrencyRate;
    }

    public void setGstCurrencyRate(double gstCurrencyRate) {
        this.gstCurrencyRate = gstCurrencyRate;
    }

    public double getForexGainLoss() {
        return forexGainLoss;
    }

    public void setForexGainLoss(double forexGainLoss) {
        this.forexGainLoss = forexGainLoss;
    }

    public int getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
    }

    public double getExchangeRateForTransaction() {
        return exchangeRateForTransaction;
    }

    public void setExchangeRateForTransaction(double exchangeRateForTransaction) {
        this.exchangeRateForTransaction = exchangeRateForTransaction;
    }

    public Tax getGstapplied() {
        return gstapplied;
    }

    public void setGstapplied(Tax gstapplied) {
        this.gstapplied = gstapplied;
    }
    public double getAmountinbase() {
        return amountinbase;
    }
    public void setAmountinbase(double amountinbase) {
        this.amountinbase = amountinbase;
    }
    
}
