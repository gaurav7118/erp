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
import java.math.BigInteger;
import java.util.Date;

/**
 *
 * @author training
 */
public class Cheque {

    private String ID;
    private String chequeNo;
    private String bankName;
    private String description;
    private MasterItem BankMasterItem;
    private Date dueDate;
    private Company company;
    private int createdFrom;// 1 - created from Make Payment; 2 - created From Receive Payment; 3 - created from fund transfer(JOURNAL ENTRY)
    private boolean chequeNoAutoGenetated;
    private boolean deleteFlag;
    private BigInteger sequenceNumber;
    private Account bankAccount; // payment method account id of pay detail
    private ChequeSequenceFormat seqformat;
    private String datePreffixValue;//Only to store Date Preffix part of sequence format
    private String dateSuffixValue;//Only to store Date Sufefix part of sequence format
    private String dateAfterPreffixValue;//Only to store Date After Prefix part of sequence format

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getChequeNo() {
        return chequeNo;
    }

    public void setChequeNo(String chequeNo) {
        this.chequeNo = chequeNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MasterItem getBankMasterItem() {
        return BankMasterItem;
    }

    public void setBankMasterItem(MasterItem BankMasterItem) {
        this.BankMasterItem = BankMasterItem;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public int getCreatedFrom() {
        return createdFrom;
    }

    public void setCreatedFrom(int createdFrom) {
        this.createdFrom = createdFrom;
    }

    public BigInteger getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(BigInteger sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public boolean isChequeNoAutoGenetated() {
        return chequeNoAutoGenetated;
    }

    public void setChequeNoAutoGenetated(boolean chequeNoAutoGenetated) {
        this.chequeNoAutoGenetated = chequeNoAutoGenetated;
    }

    public boolean isDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public Account getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(Account bankAccount) {
        this.bankAccount = bankAccount;
    }

    public ChequeSequenceFormat getSeqformat() {
        return seqformat;
    }

    public void setSeqformat(ChequeSequenceFormat seqformat) {
        this.seqformat = seqformat;
    }

    public String getDatePreffixValue() {
        return datePreffixValue;
    }

    public void setDatePreffixValue(String datePreffixValue) {
        this.datePreffixValue = datePreffixValue;
    }

    public String getDateSuffixValue() {
        return dateSuffixValue;
    }

    public void setDateSuffixValue(String dateSuffixValue) {
        this.dateSuffixValue = dateSuffixValue;
    }

    public String getDateAfterPreffixValue() {
        return dateAfterPreffixValue;
    }

    public void setDateAfterPreffixValue(String dateAfterPreffixValue) {
        this.dateAfterPreffixValue = dateAfterPreffixValue;
    }
    
}
