/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.pos;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class PaymentDetailPos {
    private String ID;
    private int paymenttype;
    private String paymentmethodname;
    private double amount;
    private Company company;
    private Date transactionDate;
    private long transactionDateinLong;
    private String locationid;
    private String invoiceid;
    private String receiptid;
    private User userid;

    public static final int Cash = 0;
    public static final int Card = 1;
    public static final int Cheque = 2;
    public static final int GiftCard = 3;
    public static final String Payment_Method__Type = "paymentmethodtype";
    public static final String Payment_Method__Name = "paymentmethodname";
    public static final String RECEIPT_ID = "receiptid";
    public static final String INVOICE_ID = "invoiceid";
    public static final String BEFORE_CLOSE_FLAG = "beforecloseflag";
    public static final String IS_SUMMATION_FLAG = "issummationflag";

    public String getInvoiceid() {
        return invoiceid;
    }

    public void setInvoiceid(String invoiceid) {
        this.invoiceid = invoiceid;
    }

    public String getReceiptid() {
        return receiptid;
    }

    public void setReceiptid(String receiptid) {
        this.receiptid = receiptid;
    }
    
    
    
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public long getTransactionDateinLong() {
        return transactionDateinLong;
    }

    public void setTransactionDateinLong(long transactionDateinLong) {
        this.transactionDateinLong = transactionDateinLong;
    }

    public String getLocationid() {
        return locationid;
    }

    public void setLocationid(String locationid) {
        this.locationid = locationid;
    }

    public String getPaymentmethodname() {
        return paymentmethodname;
    }

    public void setPaymentmethodname(String paymentmethodname) {
        this.paymentmethodname = paymentmethodname;
    }

    public int getPaymenttype() {
        return paymenttype;
    }

    public void setPaymenttype(int paymenttype) {
        this.paymenttype = paymenttype;
    }

    public User getUserid() {
        return userid;
    }

    public void setUserid(User userid) {
        this.userid = userid;
    }
    
}
