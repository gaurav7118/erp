/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.hql.accounting;

import java.util.Date;

/**
 *
 * @author krawler
 */
public class BankTransactionDetails {

    private Date date;
    private double amount;
    private String payee;
    private String description;
    private String reference;
    private String chequeNumber;

    public BankTransactionDetails() {
    }

    public BankTransactionDetails(Date date, double amount, String payee, String description, String references, String chequeNumber) {
        this.date = date;
        this.amount = amount;
        this.payee = payee;
        this.description = description;
        this.reference = references;
        this.chequeNumber = chequeNumber;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getChequeNumber() {
        return chequeNumber;
    }

    public void setChequeNumber(String chequeNumber) {
        this.chequeNumber = chequeNumber;
    }
}
