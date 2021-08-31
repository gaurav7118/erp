/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

/**
 *
 * @author krawler
 */
public class BankAccountCOAMapping {
    
    private String ID;
    private String bankID;
    private String bankAccountName;
    private String bankAccountNumber;
    private Account deskeraAccount;
    private String bankAccountDetails;
    private Company company;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getBankID() {
        return bankID;
    }

    public void setBankID(String bankID) {
        this.bankID = bankID;
    }

    public String getBankAccountName() {
        return bankAccountName;
    }

    public void setBankAccountName(String bankAccountName) {
        this.bankAccountName = bankAccountName;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public Account getDeskeraAccount() {
        return deskeraAccount;
    }

    public void setDeskeraAccount(Account deskeraAccount) {
        this.deskeraAccount = deskeraAccount;
    }

    public String getBankAccountDetails() {
        return bankAccountDetails;
    }
    
    public JSONObject getBankAccountDetailsJson() throws JSONException {
        JSONObject bankAccountDetailsJobj = new JSONObject();
        if (!StringUtil.isNullOrEmpty(bankAccountDetails)) {
            bankAccountDetailsJobj = new JSONObject(bankAccountDetails);
        }
        return bankAccountDetailsJobj;
    }

    public void setBankAccountDetails(String bankAccountDetails) {
        this.bankAccountDetails = bankAccountDetails;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
