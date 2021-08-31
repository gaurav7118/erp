/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class BankBook {
    String date="";
    String voucherno="";
    String particulars="";
    String accname="";
    String acccode="";
    String baseCurr="";
    String accCurr="";
    String transCurr="";
    String vouchertotal="";
    String openingbalance="";
    String receipt="";
    String payment="";
    String balanceInBaseCurr="";
    String balanceInAccCurr="";

    public String getAcccode() {
        return acccode;
    }

    public void setAcccode(String acccode) {
        this.acccode = acccode;
    }

    public String getTransCurr() {
        return transCurr;
    }

    public void setTransCurr(String transCurr) {
        this.transCurr = transCurr;
    }

    public String getAccname() {
        return accname;
    }

    public void setAccname(String accname) {
        this.accname = accname;
    }

    public String getBalanceInAccCurr() {
        return balanceInAccCurr;
    }

    public void setBalanceInAccCurr(String balanceInAccCurr) {
        this.balanceInAccCurr = balanceInAccCurr;
    }

    public String getBalanceInBaseCurr() {
        return balanceInBaseCurr;
    }

    public void setBalanceInBaseCurr(String balanceInBaseCurr) {
        this.balanceInBaseCurr = balanceInBaseCurr;
    }
    
    public String getAccCurr() {
        return accCurr;
    }

    public void setAccCurr(String accCurr) {
        this.accCurr = accCurr;
    }
    
    public String getBaseCurr() {
        return baseCurr;
    }

    public void setBaseCurr(String baseCurr) {
        this.baseCurr = baseCurr;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOpeningbalance() {
        return openingbalance;
    }

    public void setOpeningbalance(String openingbalance) {
        this.openingbalance = openingbalance;
    }

    public String getParticulars() {
        return particulars;
    }

    public void setParticulars(String particulars) {
        this.particulars = particulars;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    public String getVoucherno() {
        return voucherno;
    }

    public void setVoucherno(String voucherno) {
        this.voucherno = voucherno;
    }
    
    public String getVouchertotal() {
        return vouchertotal;
}

    public void setVouchertotal(String vouchertotal) {
        this.vouchertotal = vouchertotal;
    }
    
}
