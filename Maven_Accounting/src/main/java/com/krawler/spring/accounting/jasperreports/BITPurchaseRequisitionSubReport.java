/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class BITPurchaseRequisitionSubReport {
    String ID="";
    String desc = "";
    String qty = "";
    String amount = "";
    String rate = "";
    String quotationNo = "";
    String totalAmount = "";
    String srno = "";
    String item = "";
    String reqtodeliveredon="";
     public String getReqtodeliveredon(){
        return reqtodeliveredon;
    }
    public void setReqtodeliveredon(String reqtodeliveredon){
        this.reqtodeliveredon = reqtodeliveredon;
    }
     public String getItem(){
        return item;
    }
    public void setItem(String item){
        this.item = item;
    }
    public String getSrno(){
        return srno;
    }
    public void setSrno(String srno){
        this.srno = srno;
    }
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getQuotationNo() {
        return quotationNo;
    }

    public void setQuotationNo(String quotationNo) {
        this.quotationNo = quotationNo;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

}
