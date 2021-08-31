/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Date;

/**
 *
 * @author Swapnil K.
 */
public class JournalEntryUpdateHistory {

    private String ID;
    private Company company;
    private String journalEntryID;
    private String journalEntryDetailID;
    private String transactionID;
    private int transactionModuleID;
    private double oldAmountInBase;
    private double newAmountInBase;
    private Date updateDate;
    private double oldAmount;
    private double newAmount;
    private double exchangeRate;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getJournalEntryID() {
        return journalEntryID;
    }

    public void setJournalEntryID(String journalEntryID) {
        this.journalEntryID = journalEntryID;
    }

    public String getJournalEntryDetailID() {
        return journalEntryDetailID;
    }

    public void setJournalEntryDetailID(String journalEntryDetailID) {
        this.journalEntryDetailID = journalEntryDetailID;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public int getTransactionModuleID() {
        return transactionModuleID;
    }

    public void setTransactionModuleID(int transactionModuleID) {
        this.transactionModuleID = transactionModuleID;
    }

    public double getOldAmountInBase() {
        return oldAmountInBase;
    }

    public void setOldAmountInBase(double oldAmountInBase) {
        this.oldAmountInBase = oldAmountInBase;
    }

    public double getNewAmountInBase() {
        return newAmountInBase;
    }

    public void setNewAmountInBase(double newAmountInBase) {
        this.newAmountInBase = newAmountInBase;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public double getOldAmount() {
        return oldAmount;
    }

    public void setOldAmount(double oldAmount) {
        this.oldAmount = oldAmount;
    }

    public double getNewAmount() {
        return newAmount;
    }

    public void setNewAmount(double newAmount) {
        this.newAmount = newAmount;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

}
