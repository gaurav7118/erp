package com.krawler.hql.accounting;

/**
 *
 * @author Prasad Shinde
 */
public class JournalEntryReportDTO {

    private String entryNumber;
    private String entryDate;
    private String memo;
    private String transactionID;
    private String transactionDetails;
    private String accountName;
    private String accountCode;
    private String createdby;
    private String jeapprover;
    private String description;
    private String reference;
    private String project;
    private String class_field;
    private String exchangeRate;
    private Double debitAmount;
    private Double creditAmount;
    private Double debitAmountInBase;
    private Double creditAmountInBase;
    private String currencyCode;
    private String customField;
    private String cheque = "";
    private String bank = "";
    private Double taxaAmountInBase = 0.0;
    private String customFiled1 ;
    private String customFiled2 ;
    private String customFiled3 ;
    public String getClass_field() {
        return class_field;
    }

    public void setClass_field(String class_field) {
        this.class_field = class_field;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTransactionDetails() {
        return transactionDetails;
    }

    public void setTransactionDetails(String transactionDetails) {
        this.transactionDetails = transactionDetails;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(Double creditAmount) {
        this.creditAmount = creditAmount;
    }

    public Double getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(Double debitAmount) {
        this.debitAmount = debitAmount;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }

    public String getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(String entryNumber) {
        this.entryNumber = entryNumber;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getJeapprover() {
        return jeapprover;
    }

    public void setJeapprover(String jeapprover) {
        this.jeapprover = jeapprover;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Double getCreditAmountInBase() {
        return creditAmountInBase;
    }

    public void setCreditAmountInBase(Double creditAmountInBase) {
        this.creditAmountInBase = creditAmountInBase;
    }

    public Double getDebitAmountInBase() {
        return debitAmountInBase;
    }

    public void setDebitAmountInBase(Double debitAmountInBase) {
        this.debitAmountInBase = debitAmountInBase;
    }

    public String getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getCustomField() {
        return customField;
    }

    public void setCustomField(String customField) {
        this.customField = customField;
    }

    public Double getTaxaAmountInBase() {
        return taxaAmountInBase;
}

    public void setTaxaAmountInBase(Double taxaAmountInBase) {
        this.taxaAmountInBase = taxaAmountInBase;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getCheque() {
        return cheque;
    }

    public void setCheque(String cheque) {
        this.cheque = cheque;
    }

    public String getCustomFiled1() {
        return customFiled1;
    }

    public void setCustomFiled1(String customFiled1) {
        this.customFiled1 = customFiled1;
    }

    public String getCustomFiled2() {
        return customFiled2;
    }

    public void setCustomFiled2(String customFiled2) {
        this.customFiled2 = customFiled2;
    }

    public String getCustomFiled3() {
        return customFiled3;
    }

    public void setCustomFiled3(String customFiled3) {
        this.customFiled3 = customFiled3;
    }
    
}
