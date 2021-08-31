/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;
import java.util.Comparator;
/**
 *
 * @author krawler
 */
public class CreditNoteTable implements Comparator {
    String accCode="";
    String accName="";
    String amount="";
    String narration="";
    int srNo;
    String gstAmount="";
    String total="";
    String qty = "";
    String uom = "";
    String desc  = "";
    String rate = "";

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

     

    public String getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(String gstAmount) {
        this.gstAmount = gstAmount;
    }

    public int getSrNo() {
        return srNo;
    }

    public void setSrNo(int srNo) {
        this.srNo = srNo;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public String getAccCode() {
        return accCode;
    }

    public void setAccCode(String accCode) {
        this.accCode = accCode;
    }

    public String getAccName() {
        return accName;
    }

    public void setAccName(String accName) {
        this.accName = accName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public int compare(Object obj1, Object obj2) {
       Integer p1 = ((CreditNoteTable) obj1).getSrNo();
       Integer p2 = ((CreditNoteTable) obj2).getSrNo();

       if (p1 > p2) {
           return 1;
       } else if (p1 < p2){
           return -1;
       } else {
           return 0;
       }
    }
}
