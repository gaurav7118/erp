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
public class AgedReceivableSubReport {

    List<CurrencyPojo> currencyPojos=new ArrayList<CurrencyPojo>();
    String customerCode = "";
    String customerName = "";
    String customerId = "";
    String currency = "";
    String basecurr = "";
    String date = "";
    String entryNumber = "";
    String creditlimit = "";
    String credit = "";
    String creditdays = "";
    Double totalinbase = 0.0;
    Double documentAmount = 0.0;
    String entryType = "";
    String exchangerate = "";
    String propaddr = "";
    Double amountDue1 = 0.0;
    Double amountDue2 = 0.0;
    Double amountDue3 = 0.0;
    List<CurrencyWiseGrandTotals> ctList = new ArrayList<CurrencyWiseGrandTotals>();
    String creditdaystitle="";
    private String salespersonid="";
    private String salespersoncode="";
    private String salespersonname="";
     
    //// To show all the intervals of aging report
    Double dueAmount1 = 0.0;
    Double dueAmount2 = 0.0;
    Double dueAmount3 = 0.0;
    Double dueAmount4 = 0.0;
    Double dueAmount5 = 0.0;
    Double dueAmount6 = 0.0;
    Double dueAmount7 = 0.0;
    Double dueAmount8 = 0.0;
    
    List<AgedReceivableSubReportCurrencyWiseAgeing> agedReceivableSubReportCurrencyWiseAgeings=new ArrayList<AgedReceivableSubReportCurrencyWiseAgeing>();

    public List<AgedReceivableSubReportCurrencyWiseAgeing> getAgedReceivableSubReportCurrencyWiseAgeings() {
        return agedReceivableSubReportCurrencyWiseAgeings;
    }

    public void setAgedReceivableSubReportCurrencyWiseAgeings(List<AgedReceivableSubReportCurrencyWiseAgeing> agedReceivableSubReportCurrencyWiseAgeings) {
        this.agedReceivableSubReportCurrencyWiseAgeings = agedReceivableSubReportCurrencyWiseAgeings;
    }

    public String getPropaddr() {
        return propaddr;
    }

    public void setPropaddr(String propaddr) {
        this.propaddr = propaddr;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getExchangerate() {
        return exchangerate;
    }

    public void setExchangerate(String exchangerate) {
        this.exchangerate = exchangerate;
    }

    public String getCreditdaystitle() {
        return creditdaystitle;
    }

    public void setCreditdaystitle(String creditdaystitle) {
        this.creditdaystitle = creditdaystitle;
    }

    public List<CurrencyWiseGrandTotals> getCtList() {
        return ctList;
    }

    public void setCtList(List<CurrencyWiseGrandTotals> ctList) {
        this.ctList = ctList;
    }

    public List<CurrencyPojo> getCurrencyPojos() {
        return currencyPojos;
    }

    public void setCurrencyPojos(List<CurrencyPojo> currencyPojos) {
        this.currencyPojos = currencyPojos;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getBasecurr() {
        return basecurr;
    }

    public void setBasecurr(String basecurr) {
        this.basecurr = basecurr;
    }

    public Double getTotalinbase() {
        return totalinbase;
    }

    public void setTotalinbase(Double totalinbase) {
        this.totalinbase = totalinbase;
    }

    public String getCreditdays() {
        return creditdays;
    }

    public void setCreditdays(String creditdays) {
        this.creditdays = creditdays;
    }

    public String getCreditlimit() {
        return creditlimit;
    }

    public void setCreditlimit(String creditlimit) {
        this.creditlimit = creditlimit;
    }

    public Double getDocumentAmount() {
        return documentAmount;
    }

    public void setDocumentAmount(Double DocumentAmount) {
        this.documentAmount = DocumentAmount;
    }

    public Double getAmountDue1() {
        return amountDue1;
    }

    public void setAmountDue1(Double amountDue1) {
        this.amountDue1 = amountDue1;
    }

    public Double getAmountDue2() {
        return amountDue2;
    }

    public void setAmountDue2(Double amountDue2) {
        this.amountDue2 = amountDue2;
    }

    public Double getAmountDue3() {
        return amountDue3;
    }

    public void setAmountDue3(Double amountDue3) {
        this.amountDue3 = amountDue3;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(String entryNumber) {
        this.entryNumber = entryNumber;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public String getSalespersoncode() {
        return salespersoncode;
    }

    public void setSalespersoncode(String salespersoncode) {
        this.salespersoncode = salespersoncode;
    }

    public String getSalespersonid() {
        return salespersonid;
    }

    public void setSalespersonid(String salespersonid) {
        this.salespersonid = salespersonid;
    }

    public String getSalespersonname() {
        return salespersonname;
    }

    public void setSalespersonname(String salespersonname) {
        this.salespersonname = salespersonname;
    }

    public Double getDueAmount1() {
        return dueAmount1;
    }

    public void setDueAmount1(Double dueAmount1) {
        this.dueAmount1 = dueAmount1;
    }

    public Double getDueAmount2() {
        return dueAmount2;
    }

    public void setDueAmount2(Double dueAmount2) {
        this.dueAmount2 = dueAmount2;
    }

    public Double getDueAmount3() {
        return dueAmount3;
    }

    public void setDueAmount3(Double dueAmount3) {
        this.dueAmount3 = dueAmount3;
    }

    public Double getDueAmount4() {
        return dueAmount4;
    }

    public void setDueAmount4(Double dueAmount4) {
        this.dueAmount4 = dueAmount4;
    }

    public Double getDueAmount5() {
        return dueAmount5;
    }

    public void setDueAmount5(Double dueAmount5) {
        this.dueAmount5 = dueAmount5;
    }

    public Double getDueAmount6() {
        return dueAmount6;
    }

    public void setDueAmount6(Double dueAmount6) {
        this.dueAmount6 = dueAmount6;
    }

    public Double getDueAmount7() {
        return dueAmount7;
    }

    public void setDueAmount7(Double dueAmount7) {
        this.dueAmount7 = dueAmount7;
    }

    public Double getDueAmount8() {
        return dueAmount8;
    }

    public void setDueAmount8(Double dueAmount8) {
        this.dueAmount8 = dueAmount8;
    }
    
    
}
