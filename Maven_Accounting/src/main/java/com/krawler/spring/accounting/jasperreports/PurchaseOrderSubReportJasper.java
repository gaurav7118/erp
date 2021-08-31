/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class PurchaseOrderSubReportJasper {
    
       String sno ="";
       String pname ="";
       String pdesc ="";
       String aquantity ="";
       String kgs ="";
       String quantity ="";
       String bgs ="";
       String currency ="";
       String rate ="";
       String amount ="";

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAquantity() {
        return aquantity;
    }

    public void setAquantity(String aquantity) {
        this.aquantity = aquantity;
    }

    public String getBgs() {
        return bgs;
    }

    public void setBgs(String bgs) {
        this.bgs = bgs;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getKgs() {
        return kgs;
    }

    public void setKgs(String kgs) {
        this.kgs = kgs;
    }

    public String getPdesc() {
        return pdesc;
    }

    public void setPdesc(String pdesc) {
        this.pdesc = pdesc;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }
       
       
}
