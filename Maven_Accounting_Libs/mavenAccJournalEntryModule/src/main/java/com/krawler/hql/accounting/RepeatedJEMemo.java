/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

public class RepeatedJEMemo {

    private String id;
    private RepeatedJE RepeatedJEID;
    private int count;
    private String memo;
    private String RepeatedInvoiceID;
    private String RepeatedSOID;
    private String RepeatedPaymentId;
    
    public RepeatedJE getRepeatedJEID() {
        return RepeatedJEID;
    }

    public void setRepeatedJEID(RepeatedJE RepeatedJEID) {
        this.RepeatedJEID = RepeatedJEID;
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

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getRepeatedInvoiceID() {
        return RepeatedInvoiceID;
    }

    public void setRepeatedInvoiceID(String RepeatedInvoiceID) {
        this.RepeatedInvoiceID = RepeatedInvoiceID;
    }

    public String getRepeatedSOID() {
        return RepeatedSOID;
    }

    public void setRepeatedSOID(String RepeatedSOID) {
        this.RepeatedSOID = RepeatedSOID;
    }

    public String getRepeatedPaymentId() {
        return RepeatedPaymentId;
    }

    public void setRepeatedPaymentId(String RepeatedPaymentId) {
        this.RepeatedPaymentId = RepeatedPaymentId;
    }    
    
}
