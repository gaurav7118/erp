/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class TransactionPojo {
    private String transactionId;
    private String transactionDate;
    private String transactionNumber;
    private String transactionAccCode;
    private String transactionAccName;
    private String transactionDebitAmount;
    private String transactionCreditAmount;
    private String transactionNarration;
    private String transactionTotal;
    private String transactionCurrencyCode;
    private boolean printFlag;

    public TransactionPojo() {
    }

    public String getTransactionCurrencyCode() {
        return transactionCurrencyCode;
    }

    public void setTransactionCurrencyCode(String transactionCurrencyCode) {
        this.transactionCurrencyCode = transactionCurrencyCode;
    }

    public String getTransactionTotal() {
        return transactionTotal;
    }

    public void setTransactionTotal(String transactionTotal) {
        this.transactionTotal = transactionTotal;
    }

    public boolean isPrintFlag() {
        return printFlag;
    }

    public void setPrintFlag(boolean printFlag) {
        this.printFlag = printFlag;
    }

    public String getTransactionAccCode() {
        return transactionAccCode;
    }

    public void setTransactionAccCode(String transactionAccCode) {
        this.transactionAccCode = transactionAccCode;
    }

    public String getTransactionAccName() {
        return transactionAccName;
    }

    public void setTransactionAccName(String transactionAccName) {
        this.transactionAccName = transactionAccName;
    }

    public String getTransactionCreditAmount() {
        return transactionCreditAmount;
    }

    public void setTransactionCreditAmount(String transactionCreditAmount) {
        this.transactionCreditAmount = transactionCreditAmount;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionDebitAmount() {
        return transactionDebitAmount;
    }

    public void setTransactionDebitAmount(String transactionDebitAmount) {
        this.transactionDebitAmount = transactionDebitAmount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionNarration() {
        return transactionNarration;
    }

    public void setTransactionNarration(String transactionNarration) {
        this.transactionNarration = transactionNarration;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }
}
