/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class TrailBalanceJasper {
    
    String period="";
    String name="";
    String phone="";
    String fax="";
    String email="";
    String dateRange="";
    String currencyinword="";
    String debitTotal1="";
    String debitTotal2="";
    String debitTotal3="";
    String creditTotal1="";
    String creditTotal2="";
    String creditTotal3="";

    public String getCurrencyinword() {
        return currencyinword;
    }

    public void setCurrencyinword(String currencyinword) {
        this.currencyinword = currencyinword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreditTotal1() {
        return creditTotal1;
    }

    public void setCreditTotal1(String creditTotal1) {
        this.creditTotal1 = creditTotal1;
    }

    public String getCreditTotal2() {
        return creditTotal2;
    }

    public void setCreditTotal2(String creditTotal2) {
        this.creditTotal2 = creditTotal2;
    }

    public String getCreditTotal3() {
        return creditTotal3;
    }

    public void setCreditTotal3(String creditTotal3) {
        this.creditTotal3 = creditTotal3;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public String getDebitTotal1() {
        return debitTotal1;
    }

    public void setDebitTotal1(String debitTotal1) {
        this.debitTotal1 = debitTotal1;
    }

    public String getDebitTotal2() {
        return debitTotal2;
    }

    public void setDebitTotal2(String debitTotal2) {
        this.debitTotal2 = debitTotal2;
    }

    public String getDebitTotal3() {
        return debitTotal3;
    }

    public void setDebitTotal3(String debitTotal3) {
        this.debitTotal3 = debitTotal3;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    
    
}
