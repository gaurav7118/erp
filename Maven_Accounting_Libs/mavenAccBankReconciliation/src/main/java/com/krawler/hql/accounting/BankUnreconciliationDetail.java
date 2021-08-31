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
import java.util.Date;

/**
 *
 * @author krawler
 */
public class BankUnreconciliationDetail {

    private String ID;
    private double amount;
    private JournalEntry journalEntry;
    private Date unreconcileDate;
    private boolean deleted;
    private BankReconciliation bankReconciliation;
    private String accountnames;
    private boolean debit;
    private Company company;
    private String transactionID;
    private int moduleID;
    private boolean isOpeningTransaction;

    public boolean isIsOpeningTransaction() {
        return isOpeningTransaction;
    }

    public void setIsOpeningTransaction(boolean isOpeningTransaction) {
        this.isOpeningTransaction = isOpeningTransaction;
    }

    public int getModuleID() {
        return moduleID;
    }

    public void setModuleID(int moduleID) {
        this.moduleID = moduleID;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Date getUnreconcileDate() {
        return unreconcileDate;
    }

    public void setUnreconcileDate(Date unreconcileDate) {
        this.unreconcileDate = unreconcileDate;
    }

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

    public BankReconciliation getBankReconciliation() {
        return bankReconciliation;
    }

    public void setBankReconciliation(BankReconciliation bankReconciliation) {
        this.bankReconciliation = bankReconciliation;
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

    public JournalEntry getJournalEntry() {
        return journalEntry;
    }

    public void setJournalEntry(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
    }

    public String getAccountnames() {
        return accountnames;
    }

    public void setAccountnames(String accountnames) {
        this.accountnames = accountnames;
    }
}
