/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class ReceiptReportJasper {

    String date = "";
    String voucherno = "";
    String code = "";
    String name = "";
    String paymentmethod = "";
    String chequenumber = "";
    String docnumber = "";
    String gainloss = "";
    String adjusted = "";
    String amount = "";
    String amountinbase = "";
    String totalamount = "";
    String totalamountinbase = "";
    String totalamountinbasecurr = "";

    public String getTotalamountinbasecurr() {
        return totalamountinbasecurr;
    }

    public void setTotalamountinbasecurr(String totalamountinbasecurr) {
        this.totalamountinbasecurr = totalamountinbasecurr;
    }

    public String getTotalamountinbase() {
        return totalamountinbase;
    }

    public void setTotalamountinbase(String totalamountinbase) {
        this.totalamountinbase = totalamountinbase;
    }

    public String getAmountinbase() {
        return amountinbase;
    }

    public void setAmountinbase(String amountinbase) {
        this.amountinbase = amountinbase;
    }

    public String getAdjusted() {
        return adjusted;
    }

    public void setAdjusted(String adjusted) {
        this.adjusted = adjusted;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getChequenumber() {
        return chequenumber;
    }

    public void setChequenumber(String chequenumber) {
        this.chequenumber = chequenumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDocnumber() {
        return docnumber;
    }

    public void setDocnumber(String docnumber) {
        this.docnumber = docnumber;
    }

    public String getGainloss() {
        return gainloss;
    }

    public void setGainloss(String gainloss) {
        this.gainloss = gainloss;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPaymentmethod() {
        return paymentmethod;
    }

    public void setPaymentmethod(String paymentmethod) {
        this.paymentmethod = paymentmethod;
    }

    public String getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(String totalamount) {
        this.totalamount = totalamount;
    }

    public String getVoucherno() {
        return voucherno;
    }

    public void setVoucherno(String voucherno) {
        this.voucherno = voucherno;
    }
}
