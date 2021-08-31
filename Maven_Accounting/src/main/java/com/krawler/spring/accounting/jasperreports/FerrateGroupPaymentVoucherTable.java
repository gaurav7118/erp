/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class FerrateGroupPaymentVoucherTable {
    String sno="";
    String desc="";
    String amount="";
    String gstamount="";
    String totalamount="";
    String gstpercent="";
    String currencysymbol="";
    String tax="";
    String project="";
    String grandtotal="";

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrencysymbol() {
        return currencysymbol;
    }

    public void setCurrencysymbol(String currencysymbol) {
        this.currencysymbol = currencysymbol;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getGrandtotal() {
        return grandtotal;
    }

    public void setGrandtotal(String grandtotal) {
        this.grandtotal = grandtotal;
    }

    public String getGstamount() {
        return gstamount;
    }

    public void setGstamount(String gstamount) {
        this.gstamount = gstamount;
    }

    public String getGstpercent() {
        return gstpercent;
    }

    public void setGstpercent(String gstpercent) {
        this.gstpercent = gstpercent;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public String getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(String totalamount) {
        this.totalamount = totalamount;
    }
    
    
   
}
