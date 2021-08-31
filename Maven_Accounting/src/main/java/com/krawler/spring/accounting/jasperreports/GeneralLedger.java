/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

import java.util.Map;
/**
 *
 * @author krawler
 */
public class GeneralLedger {

    String acccode = "";
    String aliascode = "";
    String accname = "";
    String date = "";
    String voucherno = "";
    String JEnumber = "";
    String desc = "";
    String onlydesc = "";
    String erate = "";
    String type = "";
    String name = "";
    String memo = "";
    String payer = "";
    String payDescription = "";
    String decimalCount = "";
    double docamount = 0.0;
    double debit = 0.0;
    double credit = 0.0;
    double opening = 0.0;
    double closing = 0.0;
    double period = 0.0;
    double balance = 0.0;
    String balanceString = "";
    double openingBalanceofAccount = 0.0;   //This is used to Store Opening Balance of Account for SubLedger Report.
    String headerString = "";               //This is used to Main Group String for SubLedger Report.
    String innerString = "";                //This is used to Sub Group String for SubLedger Report.
    String accountGroupID = "";
    String currencyName = "";               //This is used to show currency name
    double debitAmtInAccCurrency = 0.0;
    double creditAmtInAccCurrency = 0.0;
    String personID = "";
    String personName = "";
    double transactionTypePerson = 0;
    Map<String, String> customFieldData;
    String costCenterName = "";
    String salesPersonName = "";
    String gstCode = "";
    Map<String, String> lineLevelCustomFieldData;
    
    
    public String getDecimalCount() {
        return decimalCount;
    }

    public void setDecimalCount(String decimalCount) {
        this.decimalCount = decimalCount;
    }
    
    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getBalanceString() {
        return balanceString;
    }

    public void setBalanceString(String balanceString) {
        this.balanceString = balanceString;
    }

    public double getTransactionTypePerson() {
        return transactionTypePerson;
    }
    
    public void setTransactionTypePerson(double transactionTypePerson) {
        this.transactionTypePerson = transactionTypePerson;
    }

    public String getPersonID() {
        return personID;
    }

    public void setPersonID(String personID) {
        this.personID = personID;
    }
    
    public double getCreditAmtInAccCurrency() {
        return creditAmtInAccCurrency;
    }

    public void setCreditAmtInAccCurrency(double creditAmtInAccCurrency) {
        this.creditAmtInAccCurrency = creditAmtInAccCurrency;
    }

    public double getDebitAmtInAccCurrency() {
        return debitAmtInAccCurrency;
    }

    public void setDebitAmtInAccCurrency(double debitAmtInAccCurrency) {
        this.debitAmtInAccCurrency = debitAmtInAccCurrency;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getOnlydesc() {
        return onlydesc;
    }

    public void setOnlydesc(String onlydesc) {
        this.onlydesc = onlydesc;
    }

    public String getJEnumber() {
        return JEnumber;
    }

    public void setJEnumber(String JEnumber) {
        this.JEnumber = JEnumber;
    }

    public String getPayDescription() {
        return payDescription;
    }

    public void setPayDescription(String payDescription) {
        this.payDescription = payDescription;
    }

    public String getPayer() {
        return payer;
    }

    public void setPayer(String payer) {
        this.payer = payer;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getErate() {
        return erate;
    }

    public void setErate(String erate) {
        this.erate = erate;
    }

    public double getClosing() {
        return closing;
    }

    public void setClosing(double closing) {
        this.closing = closing;
    }

    public double getOpening() {
        return opening;
    }

    public void setOpening(double opening) {
        this.opening = opening;
    }

    public double getPeriod() {
        return period;
    }

    public void setPeriod(double period) {
        this.period = period;
    }

    public String getAcccode() {
        return acccode;
    }

    public void setAcccode(String acccode) {
        this.acccode = acccode;
    }

    public String getAccname() {
        return accname;
    }

    public void setAccname(String accname) {
        this.accname = accname;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getDebit() {
        return debit;
    }

    public void setDebit(double debit) {
        this.debit = debit;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public double getDocamount() {
        return docamount;
    }

    public void setDocamount(double docamount) {
        this.docamount = docamount;
    }

    public String getVoucherno() {
        return voucherno;
    }

    public void setVoucherno(String voucherno) {
        this.voucherno = voucherno;
    }

    public String getAliascode() {
        return aliascode;
    }

    public String getAccountGroupID() {
        return accountGroupID;
    }

    public void setAccountGroupID(String accountGroupID) {
        this.accountGroupID = accountGroupID;
    }

    public void setAliascode(String aliascode) {
        this.aliascode = aliascode;
    }
    
    public String getHeaderString() {
        return headerString;
}

    public void setHeaderString(String headerString) {
        this.headerString = headerString;
    }

    public String getInnerString() {
        return innerString;
    }

    public void setInnerString(String innerString) {
        this.innerString = innerString;
    }

    public double getOpeningBalanceofAccount() {
        return openingBalanceofAccount;
    }

    public void setOpeningBalanceofAccount(double openingBalanceofAccount) {
        this.openingBalanceofAccount = openingBalanceofAccount;
    }
    
    public Map<String, String> getCustomFieldData() {
        return customFieldData;
    }

    public void setCustomFieldData(Map<String, String> customFieldData) {
        this.customFieldData = customFieldData;
    }
    
    public String getCostCenterName() {
        return costCenterName;
    }

    public void setCostCenterName(String costCenterName) {
        this.costCenterName = costCenterName;
    }

    public String getSalesPersonName() {
        return salesPersonName;
    }

    public void setSalesPersonName(String salesPersonName) {
        this.salesPersonName = salesPersonName;
    }

    public String getGstCode() {
        return gstCode;
    }

    public void setGstCode(String gstCode) {
        this.gstCode = gstCode;
    }

    public Map<String, String> getLineLevelCustomFieldData() {
        return lineLevelCustomFieldData;
    }

    public void setLineLevelCustomFieldData(Map<String, String> lineLevelCustomFieldData) {
        this.lineLevelCustomFieldData = lineLevelCustomFieldData;
    }
    
}
