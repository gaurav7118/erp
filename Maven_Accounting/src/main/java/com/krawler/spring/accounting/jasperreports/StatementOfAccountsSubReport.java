/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author krawler
 */
public class StatementOfAccountsSubReport {
    
    String date="";
    String transactionId="";
    String jeId="";
    String debit="";
    String credit="";
    String balance="0.00";
    String currency="";
    String customer="";
    String amountDueCurrent="0.00";
    String amountDue1="0.00";
    String amountDue2="0.00";
    String amountDue3="0.00";
    String amountDue4="0.00";
    String amountDue5="0.00";
    String totalAmountDue="0.00";
    String amount1Header="";
    String amount2Header="";
    String amount3Header="";
    String amount4Header="";
    String amount5Header="";
    String amountCurrHeader="";
    String ageingCurrency="";
    String baseCurrency="";
    String outstandingBalance="";
    String customerAddress="";
    boolean pageBreak=true;
    boolean outstandingFlag=false;
    List<SOABalanceOutstandingPojo> sOABalanceOutstandingPojos=new ArrayList<SOABalanceOutstandingPojo>();
    String basecurrencysymbol = "";
    String uem = "";
    String gstRegNo = "";
    String headerdate = "";
    String imagepath = "";
    String customercode = "";
    String attn = "";
    String customeracccode = "";
    String period = "";
    String terms = "";
    String amount = "";
    String baseamount= "";
    String custorven= "";
    String creditordebit= "";
    String negativesign1= "";
    String negativesign2= "";
    String negativesign3= "";
    String tutiondeposite= "";
    String boardingdeposite= ""; //THIS TWO ARE HCIS COMPANY SPECIFIC FIELDS.
    String companyregno = "";
    String companyPhone = "";
    String companyFax = "";
    String companyEmail = "";
    String custCurrency = "";
    String orderNo = "";
    String bankDetails = "";
    String daysOutstanding = "";
    double invoicAmountDue = 0.00;
    double duration = 30;
    String memo ="";
    List<AgeingTableForSOA> ageingTableData = new ArrayList<AgeingTableForSOA>();
    boolean lastRecord = false;
    String businessPerson = " ";// Customer or Vendor
    String registrationNo="";

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
 
    public String getCompanyregno() {
        return companyregno;
    }

    public void setCompanyregno(String companyregno) {
        this.companyregno = companyregno;
    }
    public String getBoardingdeposite() {
        return boardingdeposite;
    }

    public void setBoardingdeposite(String boardingdeposite) {
        this.boardingdeposite = boardingdeposite;
    }

    public String getTutiondeposite() {
        return tutiondeposite;
    }

    public void setTutiondeposite(String tutiondeposite) {
        this.tutiondeposite = tutiondeposite;
    }

    public String getCreditordebit() {
        return creditordebit;
    }

    public void setCreditordebit(String creditordebit) {
        this.creditordebit = creditordebit;
    }

    public String getCustorven() {
        return custorven;
    }

    public void setCustorven(String custorven) {
        this.custorven = custorven;
    }

    public String getNegativesign1() {
        return negativesign1;
    }

    public void setNegativesign1(String negativesign1) {
        this.negativesign1 = negativesign1;
    }

    public String getNegativesign2() {
        return negativesign2;
    }

    public void setNegativesign2(String negativesign2) {
        this.negativesign2 = negativesign2;
    }

    public String getNegativesign3() {
        return negativesign3;
    }

    public void setNegativesign3(String negativesign3) {
        this.negativesign3 = negativesign3;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBaseamount() {
        return baseamount;
    }

    public void setBaseamount(String baseamount) {
        this.baseamount = baseamount;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public String getCustomeracccode() {
        return customeracccode;
    }

    public void setCustomeracccode(String customeracccode) {
        this.customeracccode = customeracccode;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getAttn() {
        return attn;
    }

    public void setAttn(String attn) {
        this.attn = attn;
    }

    public String getCustomercode() {
        return customercode;
    }

    public void setCustomercode(String customercode) {
        this.customercode = customercode;
    }

    public String getImagepath() {
        return imagepath;
    }

    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }

    public String getBasecurrencysymbol() {
        return basecurrencysymbol;
    }

    public void setBasecurrencysymbol(String basecurrencysymbol) {
        this.basecurrencysymbol = basecurrencysymbol;
    }

    public String getGstRegNo() {
        return gstRegNo;
    }

    public void setGstRegNo(String gstRegNo) {
        this.gstRegNo = gstRegNo;
    }

    public String getHeaderdate() {
        return headerdate;
    }

    public void setHeaderdate(String headerdate) {
        this.headerdate = headerdate;
    }

    public String getUem() {
        return uem;
    }

    public void setUem(String uem) {
        this.uem = uem;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDebit() {
        return debit;
    }

    public void setDebit(String debit) {
        this.debit = debit;
    }

    public String getJeId() {
        return jeId;
    }

    public void setJeId(String jeId) {
        this.jeId = jeId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getAmountDueCurrent() {
        return amountDueCurrent;
    }

    public void setAmountDueCurrent(String amountDueCurrent) {
        this.amountDueCurrent = amountDueCurrent;
    }
    
    
    public String getAmountDue1() {
        return amountDue1;
    }

    public void setAmountDue1(String amountDue1) {
        this.amountDue1 = amountDue1;
    }

    public String getAmountDue2() {
        return amountDue2;
    }

    public void setAmountDue2(String amountDue2) {
        this.amountDue2 = amountDue2;
    }

    public String getAmountDue3() {
        return amountDue3;
    }

    public void setAmountDue3(String amountDue3) {
        this.amountDue3 = amountDue3;
    }

    public String getAmountDue4() {
        return amountDue4;
    }

    public void setAmountDue4(String amountDue4) {
        this.amountDue4 = amountDue4;
    }

    public String getAmountDue5() {
        return amountDue5;
    }

    public void setAmountCurrHeader(String amountCurrHeader) {
        this.amountCurrHeader = amountCurrHeader;
    }

    public String getAmountCurrHeader() {
        return amountCurrHeader;
    }

    public void setAmountDue5(String amountDue5) {
        this.amountDue5 = amountDue5;
    }
    
    public String getAmount1Header() {
        return amount1Header;
    }

    public void setAmount1Header(String amount1Header) {
        this.amount1Header = amount1Header;
    }

    public String getAmount2Header() {
        return amount2Header;
    }

    public void setAmount2Header(String amount2Header) {
        this.amount2Header = amount2Header;
    }

    public String getAmount3Header() {
        return amount3Header;
    }

    public void setAmount3Header(String amount3Header) {
        this.amount3Header = amount3Header;
    }

    public String getAmount4Header() {
        return amount4Header;
    }

    public void setAmount4Header(String amount4Header) {
        this.amount4Header = amount4Header;
    }

    public String getAmount5Header() {
        return amount5Header;
    }

    public void setAmount5Header(String amount5Header) {
        this.amount5Header = amount5Header;
    }
    
    public String getAgeingCurrency() {
        return ageingCurrency;
    }

    public void setAgeingCurrency(String ageingCurrency) {
        this.ageingCurrency = ageingCurrency;
    }
    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }
    
    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }
    public String getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(String outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public boolean isPageBreak() {
        return pageBreak;
    }

    public void setPageBreak(boolean pageBreak) {
        this.pageBreak = pageBreak;
    }
    public boolean isOutstandingFlag() {
        return outstandingFlag;
    }

    public void setOutstandingFlag(boolean outstandingFlag) {
        this.outstandingFlag = outstandingFlag;
    }

    public List<SOABalanceOutstandingPojo> getsOABalanceOutstandingPojos() {
        return sOABalanceOutstandingPojos;
    }

    public void setsOABalanceOutstandingPojos(List<SOABalanceOutstandingPojo> sOABalanceOutstandingPojos) {
        this.sOABalanceOutstandingPojos = sOABalanceOutstandingPojos;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String CompanyEmail) {
        this.companyEmail = CompanyEmail;
    }

    public String getCompanyFax() {
        return companyFax;
    }

    public void setCompanyFax(String CompanyFax) {
        this.companyFax = CompanyFax;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public String getCustCurrency() {
        return custCurrency;
    }

    public void setCustCurrency(String custCurrency) {
        this.custCurrency = custCurrency;
    }

    public List<AgeingTableForSOA> getAgeingTableData() {
        return ageingTableData;
    }

    public void setAgeingTableData(List<AgeingTableForSOA> ageingTableData) {
        this.ageingTableData = ageingTableData;
    }

    public boolean isLastRecord() {
        return lastRecord;
    }

    public void setLastRecord(boolean lastRecord) {
        this.lastRecord = lastRecord;
    }

    public String getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(String bankDetails) {
        this.bankDetails = bankDetails;
    }

    public String getDaysOutstanding() {
        return daysOutstanding;
    }

    public void setDaysOutstanding(String daysOutstanding) {
        this.daysOutstanding = daysOutstanding;
    }

    public double getInvoicAmountDue() {
        return invoicAmountDue;
    }

    public void setInvoicAmountDue(double invoicAmountDue) {
        this.invoicAmountDue = invoicAmountDue;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getBusinessPerson() {
        return businessPerson;
}

    public void setBusinessPerson(String businessPerson) {
        this.businessPerson = businessPerson;
    }

    public String getTotalAmountDue() {
        return totalAmountDue;
    }

    public void setTotalAmountDue(String totalAmountDue) {
        this.totalAmountDue = totalAmountDue;
    }

}
