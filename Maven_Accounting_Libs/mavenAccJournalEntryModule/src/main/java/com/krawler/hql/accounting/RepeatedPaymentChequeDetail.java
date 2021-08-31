/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import java.util.Date;


public class RepeatedPaymentChequeDetail {
    private String id;
    private String RepeatedPaymentID;
    private int count;
    private Date chequeDate;
    private String chequeNumber;
    
    public String getRepeatedPaymentID() {
        return RepeatedPaymentID;
    }

    public void setRepeatedPaymentID(String RepeatedPaymentID) {
        this.RepeatedPaymentID = RepeatedPaymentID;
    }

    public Date getChequeDate() {
        return chequeDate;
    }

    public void setChequeDate(Date chequeDate) {
        this.chequeDate = chequeDate;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChequeNumber() {
        return chequeNumber;
    }

    public void setChequeNumber(String chequeNumber) {
        this.chequeNumber = chequeNumber;
    }
    
}
