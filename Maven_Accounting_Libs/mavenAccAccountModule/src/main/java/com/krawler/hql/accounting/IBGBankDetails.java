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

public class IBGBankDetails {

    private String ID;
    private int ibgbank; // '1' for 'Development Bank Of Singapore'
    private String bankCode;
    private String branchCode;
    private String accountNumber;
    private String accountName;
    private String sendersCompanyID;
    private double bankDailyLimit;
    private Company company;
    private Account account;

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

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public double getBankDailyLimit() {
        return bankDailyLimit;
    }

    public void setBankDailyLimit(double bankDailyLimit) {
        this.bankDailyLimit = bankDailyLimit;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public int getIbgbank() {
        return ibgbank;
    }

    public void setIbgbank(int ibgbank) {
        this.ibgbank = ibgbank;
    }

    public String getSendersCompanyID() {
        return sendersCompanyID;
    }

    public void setSendersCompanyID(String sendersCompanyID) {
        this.sendersCompanyID = sendersCompanyID;
    }
}
