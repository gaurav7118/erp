/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class DebitNoteTaxEntry {

    private String ID;
    private DebitNote debitNote;
    private Account account;
    private boolean isForDetailsAccount;
    private String taxJedId;
    private double amount;
    private double taxamount;
    private String description;
    private Tax tax;
    private Company company;
    private double rateIncludingGst;
    private MasterItem reason;
    private double gstCurrencyRate;//Only used if the country is Singapore and the base currency is not SGD.
    JournalEntryDetail totalJED; // To map DebitNoteTaxEntry to related JED 
    JournalEntryDetail gstJED; // To map GST to related JED

    private boolean debitForMultiCNDN;  //used for allow credit or debit against accounts at grid .
    private int srNoForRow;
    private boolean TDSAmount; // For Indian countries, to treat amount as TDS amount
    private String productid; // Product ID to be used for calculating GST term amount for india
    private double termAmount; // Term amount to be stored for indian country

    public double getTermAmount() {
        return termAmount;
    }

    public void setTermAmount(double termAmount) {
        this.termAmount = termAmount;
    }
    
    public int getSrNoForRow() {
        return srNoForRow;
    }

    public void setSrNoForRow(int srNoForRow) {
        this.srNoForRow = srNoForRow;
    }
    public boolean isDebitForMultiCNDN() {
        return debitForMultiCNDN;
    }

    public void setDebitForMultiCNDN(boolean debitForMultiCNDN) {
        this.debitForMultiCNDN = debitForMultiCNDN;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
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

    public DebitNote getDebitNote() {
        return debitNote;
    }

    public void setDebitNote(DebitNote debitNote) {
        this.debitNote = debitNote;
    }

    public String getDescription() {
        return description;
    }

    public String getDNDDescription() {
        return getDescription();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIsForDetailsAccount() {
        return isForDetailsAccount;
    }

    public void setIsForDetailsAccount(boolean isForDetailsAccount) {
        this.isForDetailsAccount = isForDetailsAccount;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public String getTaxJedId() {
        return taxJedId;
    }

    public void setTaxJedId(String taxJedId) {
        this.taxJedId = taxJedId;
    }

    public MasterItem getReason() {
        return reason;
    }

    public void setReason(MasterItem reason) {
        this.reason = reason;
    }

    public double getTaxamount() {
        return taxamount;
    }

    public void setTaxamount(double taxamount) {
        this.taxamount = taxamount;
    }

    public double getGstCurrencyRate() {
        return gstCurrencyRate;
    }

    public void setGstCurrencyRate(double gstCurrencyRate) {
        this.gstCurrencyRate = gstCurrencyRate;
    }

    public JournalEntryDetail getGstJED() {
        return gstJED;
    }

    public void setGstJED(JournalEntryDetail gstJED) {
        this.gstJED = gstJED;
    }

    public JournalEntryDetail getTotalJED() {
        return totalJED;
    }

    public void setTotalJED(JournalEntryDetail totalJED) {
        this.totalJED = totalJED;
    }

    /**
     * @return the rateIncludingGst
     */
    public double getRateIncludingGst() {
        return rateIncludingGst;
    }

    /**
     * @param rateIncludingGst the rateIncludingGst to set
     */
    public void setRateIncludingGst(double rateIncludingGst) {
        this.rateIncludingGst = rateIncludingGst;
    }
    
    public boolean isTDSAmount() {
        return TDSAmount;
    }

    public void setTDSAmount(boolean TDSAmount) {
        this.TDSAmount = TDSAmount;
    }

    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }
    
}
