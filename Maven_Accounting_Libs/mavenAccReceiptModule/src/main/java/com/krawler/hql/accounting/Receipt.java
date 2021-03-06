/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import java.util.Date;
import com.krawler.common.admin.User;
import com.krawler.utils.json.base.JSONArray;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author krawler-user
 */
public class Receipt {

    private String ID;
    private String receiptNumber;
    private boolean autoGenerated;
    private String memo;
    private JournalEntry journalEntry;
    private JournalEntry journalEntryForBankCharges;
    private JournalEntry journalEntryForBankInterest;
    private JournalEntry disHonouredChequeJe;
    private boolean isDishonouredCheque;
    private JournalEntryDetail deposittoJEDetail;
    private double depositAmount;
    private Set<ReceiptDetail> rows;
    private PayDetail payDetail;
    private Company company;
    private KWLCurrency currency;
    private double externalCurrencyRate;
    private int receipttype;
    private String vendor;
    private boolean deleted;
    private boolean contraentry;
    private boolean ismanydbcr;//Used for many debit and credit
    private boolean isadvancepayment;//Used for advance payment
    private Receipt advanceid;//Used for advance payment
    private double advanceamount;//Used for advance payment
    private int advanceamounttype;//Used for advance payment == 0 for default, 1 Local, 2 Export
    private boolean isadvancefromvendor;//Used for advance payment from vendor
    private double bankChargesAmount;
    private double bankInterestAmount;
    private Account bankInterestAccount;
    private Account bankChargesAccount;
    private boolean isOpeningBalenceReceipt;// Receipt created for account opening balance 
    private boolean normalReceipt;// Receipt which is not an opening balance Receipt is normal Receipt. default value is true.
    private double openingBalanceAmountDue;
    private double openingBalanceBaseAmountDue; // Store amount due in base currency;
    private double originalOpeningBalanceBaseAmount; // Store depositAmount (opening balance transactions) in base currency;
    private Date creationDate;// this field is added at 6 March 2014 for getting creation date of Receipt in case of opening balance invoice.for normal Receipt Receipt creation date is getting from Journal Entry.
    private Date chequeDate;// this field is added for opening balance receipt.
    private String chequeNumber;// this field is added for opening balance receipt.
    private String drawnOn;// this field is added for opening balance receipt.
    private Customer customer;// this field is added for opening balance receipt.
    private Account account;// this field is added for opening balance receipt.
    private double exchangeRateForOpeningTransaction;// this field is added for opening balance receipt. in case of base currency its value will be 1;
    /*
     * @ conversionRateFromCurrencyToBase - before date 30th July 2014
     * exchangeRateForOpeningTransaction field was saving value - base to
     * currency exchange rate so for previous transactions
     * isConversionRateFromCurrencyToBase flag value will be False. But for new
     * Transactions from Date 18th July 2014 isConversionRateFromCurrencyToBase
     * Flag value will be true. and exchangeRateForOpeningTransaction will
     * contain value - Currency to Base exchange rate.
     */
    private boolean conversionRateFromCurrencyToBase;
    private boolean nonRefundable;
    private int seqnumber;//Only to store integer part of sequence format
    private SequenceFormat seqformat;
    private String datePreffixValue;//Only to store Date Preffix part of sequence format
    private String dateSuffixValue;//Only to store Date Sufefix part of sequence format
    private String dateAfterPreffixValue;//Only to store Date After Prefix part of sequence format
    private String revalJeId;  //for maintaing relation between realised JE and payment 
    private MasterItem receivedFrom;//Used for storing froms whom payment is received.
    private User createdby;
    private User modifiedby;
    private long createdon;
    private long updatedon;
    private boolean printed;
    private OpeningBalanceReceiptCustomData openingBalanceReceiptCustomData;
    private boolean isCnDnAndInvoicePayment;
    private String cndnAndInvoiceId;//Used for linked cndn payment
    private int invoiceAdvCndnType;//Used for linked cndn payment
    private Tax tax;
    private double taxAmount;
    private boolean linkedToClaimedInvoice;
    private int paymentWindowType;//Used differentiate between payment types---payment Against Vendor = 1,payment Against Customer = 2,Aginst GL = 3
    private boolean mainPaymentForCNDNFlag;
    private Set<LinkDetailReceipt> linkDetailReceipts;
    private Set<LinkDetailReceiptToDebitNote> linkDetailReceiptsToDebitNote;
    private Set<ReceiptAdvanceDetail> receiptAdvanceDetails;
    private Set<ReceiptDetailOtherwise> receiptDetailOtherwises;
    private Set<DebitNotePaymentDetails> debitNotePaymentDetails;
    HashMap<String, JSONArray> jcustomarrayMap = new HashMap();
    private double paymentcurrencytopaymentmethodcurrencyrate;
    String lmsReceiptID; //Used for saving LMS synced receipt ID 
    private double depositamountinbase;  // to be used for normal receipt
    private boolean isWrittenOff;
    private Set<ReceiptDetailLoan> receiptDetailsLoan;
    private Set<LinkDetailReceiptToAdvancePayment> linkDetailReceiptsToAdvancePayment; // advance payment details link to refund type receipt
    private boolean isEmailSent;//flag to update Email Icon
    private int approvestatuslevel;//its value vary from 1 to 11. If it is 11 then it not pending approval
    private int generatedSource;//0=web-application,1=MobileApps,2=POS

    public int getGeneratedSource() {
        return generatedSource;
    }

    public void setGeneratedSource(int generatedSource) {
        this.generatedSource = generatedSource;
    }
    
    /**
     * @return the approvestatuslevel
     */
    public int getApprovestatuslevel() {
        return approvestatuslevel;
    }
    /**
     * @param approvestatuslevel the approvestatuslevel to set
     */
    public void setApprovestatuslevel(int approvestatuslevel) {
        this.approvestatuslevel = approvestatuslevel;
    }

    public boolean isIsEmailSent() {
        return isEmailSent;
    }

    public void setIsEmailSent(boolean isEmailSent) {
        this.isEmailSent = isEmailSent;
    }
    
    public String getDatePreffixValue() {
        return datePreffixValue;
    }

    public void setDatePreffixValue(String datePreffixValue) {
        this.datePreffixValue = datePreffixValue;
    }

    public String getDateSuffixValue() {
        return dateSuffixValue;
    }

    public void setDateSuffixValue(String dateSuffixValue) {
        this.dateSuffixValue = dateSuffixValue;
    }
    
    public String getLmsReceiptID() {
        return lmsReceiptID;
    }

    public void setLmsReceiptID(String lmsReceiptID) {
        this.lmsReceiptID = lmsReceiptID;
    }

    public boolean isPrinted() {
        return printed;
    }

    public void setPrinted(boolean printed) {
        this.printed = printed;
    }

    public MasterItem getReceivedFrom() {
        return receivedFrom;
    }

    public boolean isLinkedToClaimedInvoice() {
        return linkedToClaimedInvoice;
    }

    public void setLinkedToClaimedInvoice(boolean linkedToClaimedInvoice) {
        this.linkedToClaimedInvoice = linkedToClaimedInvoice;
    }

    

    public void setReceivedFrom(MasterItem receivedFrom) {
        this.receivedFrom = receivedFrom;
    }

    public SequenceFormat getSeqformat() {
        return seqformat;
    }

    public void setSeqformat(SequenceFormat seqformat) {
        this.seqformat = seqformat;
    }

    public int getSeqnumber() {
        return seqnumber;
    }

    public void setSeqnumber(int seqnumber) {
        this.seqnumber = seqnumber;
    }

    public double getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(double depositAmount) {
        this.depositAmount = depositAmount;
    }

    public JournalEntryDetail getDeposittoJEDetail() {
        return deposittoJEDetail;
    }

    public void setDeposittoJEDetail(JournalEntryDetail deposittoJEDetail) {
        this.deposittoJEDetail = deposittoJEDetail;
    }

    public boolean isIsadvancefromvendor() {
        return isadvancefromvendor;
    }

    public void setIsadvancefromvendor(boolean isadvancefromvendor) {
        this.isadvancefromvendor = isadvancefromvendor;
    }

    public boolean isIsmanydbcr() {
        return ismanydbcr;
    }

    public void setIsmanydbcr(boolean ismanydbcr) {
        this.ismanydbcr = ismanydbcr;
    }

    public double getAdvanceamount() {
        return advanceamount;
    }

    public void setAdvanceamount(double advanceamount) {
        this.advanceamount = advanceamount;
    }

    public Receipt getAdvanceid() {
        return advanceid;
    }

    public void setAdvanceid(Receipt advanceid) {
        this.advanceid = advanceid;
    }
    
    public int getAdvanceamounttype() {
        return advanceamounttype;
    }

    public void setAdvanceamounttype(int advanceamounttype) {
        this.advanceamounttype = advanceamounttype;
    }
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Set<ReceiptDetail> getRows() {
        return rows;
    }

    public void setRows(Set<ReceiptDetail> rows) {
        this.rows = rows;
    }

    public JournalEntry getJournalEntry() {
        return journalEntry;
    }

    public void setJournalEntry(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public boolean isAutoGenerated() {
        return autoGenerated;
    }

    public void setAutoGenerated(boolean autoGenerated) {
        this.autoGenerated = autoGenerated;
    }

    public PayDetail getPayDetail() {
        return payDetail;
    }

    public void setPayDetail(PayDetail payDetail) {
        this.payDetail = payDetail;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isContraentry() {
        return contraentry;
    }

    public void setContraentry(boolean contraentry) {
        this.contraentry = contraentry;
    }

    public boolean isIsadvancepayment() {
        return isadvancepayment;
    }

    public void setIsadvancepayment(boolean isadvancepayment) {
        this.isadvancepayment = isadvancepayment;
    }

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }

    public double getExternalCurrencyRate() {
        return externalCurrencyRate;
    }

    public void setExternalCurrencyRate(double externalCurrencyRate) {
        this.externalCurrencyRate = externalCurrencyRate;
    }

    public int getReceipttype() {
        return receipttype;
    }

    public void setReceipttype(int receipttype) {
        this.receipttype = receipttype;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Account getBankChargesAccount() {
        return bankChargesAccount;
    }

    public void setBankChargesAccount(Account bankChargesAccount) {
        this.bankChargesAccount = bankChargesAccount;
    }

    public double getBankChargesAmount() {
        return bankChargesAmount;
    }

    public void setBankChargesAmount(double bankChargesAmount) {
        this.bankChargesAmount = bankChargesAmount;
    }

    public Account getBankInterestAccount() {
        return bankInterestAccount;
    }

    public void setBankInterestAccount(Account bankInterestAccount) {
        this.bankInterestAccount = bankInterestAccount;
    }

    public double getBankInterestAmount() {
        return bankInterestAmount;
    }

    public void setBankInterestAmount(double bankInterestAmount) {
        this.bankInterestAmount = bankInterestAmount;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isIsOpeningBalenceReceipt() {
        return isOpeningBalenceReceipt;
    }

    public void setIsOpeningBalenceReceipt(boolean isOpeningBalenceReceipt) {
        this.isOpeningBalenceReceipt = isOpeningBalenceReceipt;
    }

    public boolean isNormalReceipt() {
        return normalReceipt;
    }

    public void setNormalReceipt(boolean normalReceipt) {
        this.normalReceipt = normalReceipt;
    }

    public double getOpeningBalanceAmountDue() {
        return openingBalanceAmountDue;
    }

    public void setOpeningBalanceAmountDue(double openingBalanceAmountDue) {
        this.openingBalanceAmountDue = openingBalanceAmountDue;
    }

    public double getOpeningBalanceBaseAmountDue() {
        return openingBalanceBaseAmountDue;
    }

    public void setOpeningBalanceBaseAmountDue(double openingBalanceBaseAmountDue) {
        this.openingBalanceBaseAmountDue = openingBalanceBaseAmountDue;
    }

    public double getOriginalOpeningBalanceBaseAmount() {
        return originalOpeningBalanceBaseAmount;
    }

    public void setOriginalOpeningBalanceBaseAmount(double originalOpeningBalanceBaseAmount) {
        this.originalOpeningBalanceBaseAmount = originalOpeningBalanceBaseAmount;
    }
    
    public Date getChequeDate() {
        return chequeDate;
    }

    public void setChequeDate(Date chequeDate) {
        this.chequeDate = chequeDate;
    }

    public String getChequeNumber() {
        return chequeNumber;
    }

    public void setChequeNumber(String chequeNumber) {
        this.chequeNumber = chequeNumber;
    }

    public String getDrawnOn() {
        return drawnOn;
    }

    public void setDrawnOn(String drawnOn) {
        this.drawnOn = drawnOn;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public boolean isConversionRateFromCurrencyToBase() {
        return conversionRateFromCurrencyToBase;
    }

    public void setConversionRateFromCurrencyToBase(boolean conversionRateFromCurrencyToBase) {
        this.conversionRateFromCurrencyToBase = conversionRateFromCurrencyToBase;
    }

    public double getExchangeRateForOpeningTransaction() {
        return exchangeRateForOpeningTransaction;
    }

    public void setExchangeRateForOpeningTransaction(double exchangeRateForOpeningTransaction) {
        this.exchangeRateForOpeningTransaction = exchangeRateForOpeningTransaction;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getRevalJeId() {
        return revalJeId;
    }

    public void setRevalJeId(String revalJeId) {
        this.revalJeId = revalJeId;
    }

    public User getCreatedby() {
        return createdby;
    }

    public void setCreatedby(User createdby) {
        this.createdby = createdby;
    }

    public long getCreatedon() {
        return createdon;
    }

    public void setCreatedon(long createdon) {
        this.createdon = createdon;
    }

    public User getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(User modifiedby) {
        this.modifiedby = modifiedby;
    }

    public long getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(long updatedon) {
        this.updatedon = updatedon;
    }

    public OpeningBalanceReceiptCustomData getOpeningBalanceReceiptCustomData() {
        return openingBalanceReceiptCustomData;
    }

    public void setOpeningBalanceReceiptCustomData(OpeningBalanceReceiptCustomData openingBalanceReceiptCustomData) {
        this.openingBalanceReceiptCustomData = openingBalanceReceiptCustomData;
    }
    
      public String getCndnAndInvoiceId() {
        return cndnAndInvoiceId;
    }

    public void setCndnAndInvoiceId(String cndnAndInvoiceId) {
        this.cndnAndInvoiceId = cndnAndInvoiceId;
    }

    public boolean isIsCnDnAndInvoicePayment() {
        return isCnDnAndInvoicePayment;
    }

    public void setIsCnDnAndInvoicePayment(boolean isCnDnAndInvoicePayment) {
        this.isCnDnAndInvoicePayment = isCnDnAndInvoicePayment;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }
    
    public int getInvoiceAdvCndnType() {
        return invoiceAdvCndnType;
    }

    public void setInvoiceAdvCndnType(int invoiceAdvCndnType) {
        this.invoiceAdvCndnType = invoiceAdvCndnType;
    }

    public boolean isMainPaymentForCNDNFlag() {
        return mainPaymentForCNDNFlag;
    }

    public void setMainPaymentForCNDNFlag(boolean mainPaymentForCNDNFlag) {
        this.mainPaymentForCNDNFlag = mainPaymentForCNDNFlag;
    }

    public int getPaymentWindowType() {
        return paymentWindowType;
    }

    public void setPaymentWindowType(int paymentWindowType) {
        this.paymentWindowType = paymentWindowType;
    }

    public Set<LinkDetailReceipt> getLinkDetailReceipts() {
        return linkDetailReceipts;
    }

    public void setLinkDetailReceipts(Set<LinkDetailReceipt> linkDetailReceipts) {
        this.linkDetailReceipts = linkDetailReceipts;
    }

    public Set<ReceiptAdvanceDetail> getReceiptAdvanceDetails() {
        return receiptAdvanceDetails;
    }

    public void setReceiptAdvanceDetails(Set<ReceiptAdvanceDetail> receiptAdvanceDetails) {
        this.receiptAdvanceDetails = receiptAdvanceDetails;
    }

    public Set<ReceiptDetailOtherwise> getReceiptDetailOtherwises() {
        return receiptDetailOtherwises;
    }

    public void setReceiptDetailOtherwises(Set<ReceiptDetailOtherwise> receiptDetailOtherwises) {
        this.receiptDetailOtherwises = receiptDetailOtherwises;
    }

    public HashMap<String, JSONArray> getJcustomarrayMap() {
        return jcustomarrayMap;
    }

    public void setJcustomarrayMap(HashMap<String, JSONArray> jcustomarrayMap) {
        this.jcustomarrayMap = jcustomarrayMap;
    }
    
    public JournalEntry getJournalEntryForBankCharges() {
        return journalEntryForBankCharges;
}

    public void setJournalEntryForBankCharges(JournalEntry journalEntryForBankCharges) {
        this.journalEntryForBankCharges = journalEntryForBankCharges;
    }

    public JournalEntry getJournalEntryForBankInterest() {
        return journalEntryForBankInterest;
}

    public void setJournalEntryForBankInterest(JournalEntry journalEntryForBankInterest) {
        this.journalEntryForBankInterest = journalEntryForBankInterest;
    }

    public double getPaymentcurrencytopaymentmethodcurrencyrate() {
        return paymentcurrencytopaymentmethodcurrencyrate;
}

    public void setPaymentcurrencytopaymentmethodcurrencyrate(double paymentcurrencytopaymentmethodcurrencyrate) {
        this.paymentcurrencytopaymentmethodcurrencyrate = paymentcurrencytopaymentmethodcurrencyrate;
    }

    public Set<DebitNotePaymentDetails> getDebitNotePaymentDetails() {
        return debitNotePaymentDetails;
    }

    public void setDebitNotePaymentDetails(Set<DebitNotePaymentDetails> debitNotePaymentDetails) {
        this.debitNotePaymentDetails = debitNotePaymentDetails;
    }

    public JournalEntry getDisHonouredChequeJe() {
        return disHonouredChequeJe;
    }

    public void setDisHonouredChequeJe(JournalEntry disHonouredChequeJe) {
        this.disHonouredChequeJe = disHonouredChequeJe;
    }
    
    public boolean isIsDishonouredCheque() {
        return isDishonouredCheque;
    }

    public boolean getIsDishonouredCheque() {
        return isDishonouredCheque;
    }
    
    public void setIsDishonouredCheque(boolean isDishonouredCheque) {
        this.isDishonouredCheque = isDishonouredCheque;
    }

    public double getDepositamountinbase() {
        return depositamountinbase;
    }

    public void setDepositamountinbase(double depositamountinbase) {
        this.depositamountinbase = depositamountinbase;
    }
    
    public Set<LinkDetailReceiptToDebitNote> getLinkDetailReceiptsToDebitNote() {
        return linkDetailReceiptsToDebitNote;
    }

    public void setLinkDetailReceiptsToDebitNote(Set<LinkDetailReceiptToDebitNote> linkDetailReceiptsToDebitNote) {
        this.linkDetailReceiptsToDebitNote = linkDetailReceiptsToDebitNote;
    }

    public boolean isIsWrittenOff() {
        return isWrittenOff;
    }

    public void setIsWrittenOff(boolean isWrittenOff) {
        this.isWrittenOff = isWrittenOff;
    }

    public Set<ReceiptDetailLoan> getReceiptDetailsLoan() {
        return receiptDetailsLoan;
    }

    public void setReceiptDetailsLoan(Set<ReceiptDetailLoan> receiptDetailsLoan) {
        this.receiptDetailsLoan = receiptDetailsLoan;
    }

    /**
     * @return the nonRefundable
     */
    public boolean isNonRefundable() {
        return nonRefundable;
    }

    /**
     * @param nonRefundable the nonRefundable to set
     */
    public void setNonRefundable(boolean nonRefundable) {
        this.nonRefundable = nonRefundable;
    }

    public Set<LinkDetailReceiptToAdvancePayment> getLinkDetailReceiptsToAdvancePayment() {
        return linkDetailReceiptsToAdvancePayment;
    }

    public void setLinkDetailReceiptsToAdvancePayment(Set<LinkDetailReceiptToAdvancePayment> linkDetailReceiptsToAdvancePayment) {
        this.linkDetailReceiptsToAdvancePayment = linkDetailReceiptsToAdvancePayment;
    }    
    public String getDateAfterPreffixValue() {
        return dateAfterPreffixValue;
    }

    public void setDateAfterPreffixValue(String dateAfterPreffixValue) {
        this.dateAfterPreffixValue = dateAfterPreffixValue;
    }
}
