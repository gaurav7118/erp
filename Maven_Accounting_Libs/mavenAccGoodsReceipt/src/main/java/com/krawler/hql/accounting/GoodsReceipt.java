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

import com.krawler.common.admin.*;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

public class GoodsReceipt {

    private String ID;
    private String goodsReceiptNumber;
    //  private String vendorInvoiceNumber;
    private boolean autoGenerated;
    private String billFrom;
    private String shipFrom;
    private Date dueDate;
    private Date shipDate;
    private String memo;
    private Discount discount;
    private JournalEntry journalEntry;
    private boolean isExpenseType;
    private Set<GoodsReceiptDetail> rows;
    private Set<ExpenseGRDetail> expenserows;
    private JournalEntryDetail vendorEntry;
    private JournalEntryDetail shipEntry;
    private JournalEntryDetail otherEntry;
    private JournalEntryDetail taxEntry;
    private JournalEntryDetail roundingAdjustmentEntry;
    private Company company;
    private KWLCurrency currency;
    private ExchangeRateDetails exchangeRateDetail;
    private double externalCurrencyRate;
    private boolean deleted;
    private Tax tax;
    private Vendor vendor;
    private String shipvia;
    private String fob;
    private int pendingapproval;
    private Projreport_Template templateid;
//    private ModuleTemplate moduletemplateid;
    private boolean favourite;
    private boolean fixedAssetInvoice;
    private boolean capitalGoodsAcquired;
    private boolean retailPurchase;
    private boolean importService;
    private User approver;
    private int approvallevel;
    private int istemplate;
    private double shiplength;
    private String postText;
    private String billTo;
    private String shipTo;
    private int seqnumber;//Only to store integer part of sequence format
    private SequenceFormat seqformat;
    private String datePreffixValue;//Only to store Date Preffix part of sequence format
    private String dateSuffixValue;//Only to store Date Sufefix part of sequence format
    private String dateAfterPreffixValue;//Only to store Date After Prefix part of sequence format
    private Term termid;
    private Account account;
    private boolean isOpeningBalenceInvoice;// invoice which is being create from account opening balance button in customer/vendor creation form will have this flag true;
    private boolean normalInvoice;// invoice which is not an opening balance invoice is normal invoice. default value is true.
    private double originalOpeningBalanceAmount;
    private double originalOpeningBalanceBaseAmount;
    private double openingBalanceAmountDue;
    private double openingBalanceBaseAmountDue;
    private double exchangeRateForOpeningTransaction;// in case of base currency its value will be 1;
    private boolean gstIncluded;
    private double invoiceamountdue;
    private int approvestatuslevel;    
    private boolean isFromPOS; // Is PI generated from POS App for cashout entry
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
    private Date partyInvoiceDate;
    private String partyInvoiceNumber;
    private Date creationDate;// this field is added at 6 March 2014 for getting creation date of invoice in case of opening balance invoice.for normal invoices invoice creation date is getting from Journal Entry.
    private MasterItem masterSalesPerson;
    private User createdby;
    private User modifiedby;
    private long createdon;
    private long updatedon;
    private String RMCDApprovalNo;
    private String invoicetype;
    private String formtype;//For India Country Only.
    private boolean gtaapplicable;//For India Country Only.
    private String formseriesno;//For India Country Only.
    private String formno;//For India Country Only.
    private Date formdate;//For India Country Only.
    private double formamount;//For India Country Only.
    private String formstatus;//For India Country Only.
    private boolean selfBilledInvoice;
    private boolean cashtransaction;
    private BillingShippingAddresses billingShippingAddresses;
    private MasterItem masterAgent;
    private boolean printed;
    private Set<GoodsReceipt> landedInvoice;
    private int badDebtType;// badDebtType == 0 if invoice is not to come in recover tab; badDebtType == 1 if invoice to come in recover tab after claiming it
    private int claimedPeriod;// months in number like - 0 for jan
    private Date debtClaimedDate;
//    private Date debtRecoveredDate;
    private OpeningBalanceVendorInvoiceCustomData openingBalanceVendorInvoiceCustomData;
    private PayDetail payDetail;
    private double gstCurrencyRate;//Only used if the country is Singapore and the base currency is not SGD.
     private boolean isconsignment;
    private boolean isMRPJobWorkIN;   // Flag for MRP JOB Work Invoice
    private Boolean termsincludegst; // flag of calculated the GST amount on Basic or on adding other terms amount
    private RepeatedInvoices repeateInvoice;
    private GoodsReceipt parentInvoice;
    private boolean isOpenInPR;
    private boolean isOpenInGR;
    private boolean isOpenPayment; // Used when payment done for invoices

 
    private Date amountDueDate; // used to store the date of transaction on which amountdue of goodsreceipt is set to 0.
    private double invoiceAmount;
    private double invoiceAmountInBase;
    private double invoiceAmountDueInBase;//normal invoice amount due in base
    private double discountAmount;
    private double discountAmountInBase;
    private double claimAmountDue;       // ERP-19263 . Amount to recover after claiming
    private boolean isExciseInvoice;
    private String defaultnatureOfPurchase;
    private String manufacturerType;
    private int isCenvatAdjust;
    private String excisepaidJE;
    private double tdsRate;
    private double tdsAmount;
    private int tdsMasterRateRuleId;//For India, To store TDS Master Rate Rule ID
    private double TotalAdvanceTDSAdjustmentAmt;//For India, TDS Amount of Advance Payments.
    private int tdsPaidFlag;
    private String tdsPayment;
    private int tdsInterestPaidFlag;// To Verify whether TDS Interest is paid or not.
    private String tdsInterestPayment;// If paid then respective TDS Interest Payment id.
    private Set<GoodsReceiptPaymentMapping> advancepaymentrows;//For India, rows of mapping between Goods Receipt & Advance Payment.
    private double taxamount; //ERP-27671 - storing tax amount in invoice in invoice currency
    private double excludingGstAmount;  //ERP-27671 - storing amount excluding tax in invoice in invoice currency
    private double excludingGstAmountInBase; //ERP-27671 - storing amount excluding tax in invoice in base currency
    private double taxamountinbase;  //ERP-27671 - storing tax amount in invoice in base currency
    private String supplierInvoiceNo;//SDP-4510
    private boolean isEmailSent;//flag to update Email Icon    
    private LandingCostCategory landingCostCategory; // Landing Cost Of Category in Credit Purchase Invoice Only
    private Set<LccManualWiseProductAmount> lccmanualwiseproductamount;
    private boolean isJobWorkOutInv;
    private boolean isIndGSTApplied; //ERP-32829 
    private boolean applyTaxToTerms;
    private boolean isTDSApplicable;
    private String importDeclarationNo;
    private boolean isRoundingAdjustmentApplied;
    private boolean isDropshipDocument;//True ,If DropShip Type PI
    private JournalEntry landedInvoiceJE;                  
    private boolean isMerchantExporter;
    private boolean isCreditable;//Is Creditable field used to know whether the particular transactions is applicable for Credit or not for Indonesia.
    private boolean isDraft;// flag for identify draft
    
    public boolean isIsDraft() {
        return isDraft;
    }

    public void setIsDraft(boolean isDraft) {
        this.isDraft = isDraft;
    }

    public boolean isIsDropshipDocument() {
        return isDropshipDocument;
    }

    public void setIsDropshipDocument(boolean isDropshipDocument) {
        this.isDropshipDocument = isDropshipDocument;
    }

    public boolean isIsRoundingAdjustmentApplied() {
        return isRoundingAdjustmentApplied;
    }

    public void setIsRoundingAdjustmentApplied(boolean isRoundingAdjustmentApplied) {
        this.isRoundingAdjustmentApplied = isRoundingAdjustmentApplied;
    }

    public JournalEntryDetail getRoundingAdjustmentEntry() {
        return roundingAdjustmentEntry;
    }

    public void setRoundingAdjustmentEntry(JournalEntryDetail roundingAdjustmentEntry) {
        this.roundingAdjustmentEntry = roundingAdjustmentEntry;
    }
    
    public JournalEntry getLandedInvoiceJE() {
        return landedInvoiceJE;
    }

    public void setLandedInvoiceJE(JournalEntry landedInvoiceJE) {
        this.landedInvoiceJE = landedInvoiceJE;
    }
    
    public boolean isIsIndGSTApplied() {
        return isIndGSTApplied;
    }

    public void setIsIndGSTApplied(boolean isIndGSTApplied) {
        this.isIndGSTApplied = isIndGSTApplied;
    }

    public boolean isIsJobWorkOutInv() {
        return isJobWorkOutInv;
    }

    public void setIsJobWorkOutInv(boolean isJobWorkOutInv) {
        this.isJobWorkOutInv = isJobWorkOutInv;
    }
    public boolean isIsEmailSent() {
        return isEmailSent;
    }

    public void setIsEmailSent(boolean isEmailSent) {
        this.isEmailSent = isEmailSent;
    }
    
    public GoodsReceipt() {
        isOpenInPR = true;
        isOpenInGR = true;
    }
    public boolean isIsOpenPayment() {
        return isOpenPayment;
    }

    public void setIsOpenPayment(boolean isOpenPayment) {
        this.isOpenPayment = isOpenPayment;
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
    
    public PayDetail getPayDetail() {
        return payDetail;
    }

    public boolean isIsOpenInPR() {
        return isOpenInPR;
    }

    public void setIsOpenInPR(boolean isOpenInPR) {
        this.isOpenInPR = isOpenInPR;
    }

    public void setPayDetail(PayDetail payDetail) {
        this.payDetail = payDetail;
    }

    public Set<GoodsReceipt> getLandedInvoice() {
        return landedInvoice;
    }

    public void setLandedInvoice(Set<GoodsReceipt> landedInvoice) {
        this.landedInvoice = landedInvoice;
    }

//    public GoodsReceipt getLandedInvoice() {
//        return landedInvoice;
//    }
//
//    public void setLandedInvoice(GoodsReceipt landedInvoice) {
//        this.landedInvoice = landedInvoice;
//    }

    public boolean isPrinted() {
        return printed;
    }

    public void setPrinted(boolean printed) {
        this.printed = printed;
    }

    public MasterItem getMasterAgent() {
        return masterAgent;
    }

    public void setMasterAgent(MasterItem masterAgent) {
        this.masterAgent = masterAgent;
    }

    public BillingShippingAddresses getBillingShippingAddresses() {
        return billingShippingAddresses;
    }

    public void setBillingShippingAddresses(BillingShippingAddresses billingShippingAddresses) {
        this.billingShippingAddresses = billingShippingAddresses;
    }

    public boolean isCashtransaction() {
        return cashtransaction;
    }

    public void setCashtransaction(boolean cashtransaction) {
        this.cashtransaction = cashtransaction;
    }

    public Term getTermid() {
        return termid;
    }

    public void setTermid(Term termid) {
        this.termid = termid;
    }

    public int getSeqnumber() {
        return seqnumber;
    }

    public void setSeqnumber(int seqnumber) {
        this.seqnumber = seqnumber;
    }

    public SequenceFormat getSeqformat() {
        return seqformat;
    }

    public void setSeqformat(SequenceFormat seqformat) {
        this.seqformat = seqformat;
    }

    public String getBillTo() {
        return billTo;
    }

    public void setBillTo(String billTo) {
        this.billTo = billTo;
    }

    public String getShipTo() {
        return shipTo;
    }

    public void setShipTo(String shipTo) {
        this.shipTo = shipTo;
    }

    public int getIstemplate() {
        return istemplate;
    }

    public void setIstemplate(int istemplate) {
        this.istemplate = istemplate;
    }
    /*
     * public String getVendorInvoiceNumber() { return vendorInvoiceNumber; }
     *
     * public void setVendorInvoiceNumber(String vendorInvoiceNumber) {
     * this.vendorInvoiceNumber = vendorInvoiceNumber;
    }
     */

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public boolean isAutoGenerated() {
        return autoGenerated;
    }

    public void setAutoGenerated(boolean autoGenerated) {
        this.autoGenerated = autoGenerated;
    }

    public int getApprovallevel() {
        return approvallevel;
    }

    public void setApprovallevel(int approvallevel) {
        this.approvallevel = approvallevel;
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

    public String getBillFrom() {
        return billFrom;
    }

    public void setBillFrom(String billFrom) {
        this.billFrom = billFrom;
    }

    public String getShipFrom() {
        return shipFrom;
    }

    public void setShipFrom(String shipFrom) {
        this.shipFrom = shipFrom;
    }

    public String getGoodsReceiptNumber() {
        return goodsReceiptNumber;
    }

    public void setGoodsReceiptNumber(String goodsReceiptNumber) {
        this.goodsReceiptNumber = goodsReceiptNumber;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getShipDate() {
        return shipDate;
    }

    public void setShipDate(Date shipDate) {
        this.shipDate = shipDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public boolean isSelfBilledInvoice() {
        return selfBilledInvoice;
    }

    public void setSelfBilledInvoice(boolean selfBilledInvoice) {
        this.selfBilledInvoice = selfBilledInvoice;
    }

    public String getRMCDApprovalNo() {
        return RMCDApprovalNo;
    }

    public void setRMCDApprovalNo(String RMCDApprovalNo) {
        this.RMCDApprovalNo = RMCDApprovalNo;
    }

    public JournalEntryDetail getOtherEntry() {
        return otherEntry;
    }

    public void setOtherEntry(JournalEntryDetail otherEntry) {
        this.otherEntry = otherEntry;
    }

    public Set<GoodsReceiptDetail> getRows() {
        Set<GoodsReceiptDetail> tSet = null;
        if (rows != null) {
            tSet = new TreeSet<>();
            tSet.addAll(rows);// Added set in TreeSet to order data according to Sr No.
        }
        return tSet;
    }

    public void setRows(Set<GoodsReceiptDetail> rows) {
        this.rows = rows;
    }

    public JournalEntryDetail getShipEntry() {
        return shipEntry;
    }

    public void setShipEntry(JournalEntryDetail shipEntry) {
        this.shipEntry = shipEntry;
    }

    public JournalEntryDetail getVendorEntry() {
        return vendorEntry;
    }

    public void setVendorEntry(JournalEntryDetail vendorEntry) {
        this.vendorEntry = vendorEntry;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public JournalEntryDetail getTaxEntry() {
        return taxEntry;
    }

    public void setTaxEntry(JournalEntryDetail taxEntry) {
        this.taxEntry = taxEntry;
    }

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }

    public ExchangeRateDetails getExchangeRateDetail() {
        return exchangeRateDetail;
    }

    public void setExchangeRateDetail(ExchangeRateDetails exchangeRateDetail) {
        this.exchangeRateDetail = exchangeRateDetail;
    }

    public double getExternalCurrencyRate() {
        return externalCurrencyRate;
    }

    public void setExternalCurrencyRate(double externalCurrencyRate) {
        this.externalCurrencyRate = externalCurrencyRate;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public Set<ExpenseGRDetail> getExpenserows() {
        Set<ExpenseGRDetail> tSet = null;
        if (expenserows != null) {
            tSet = new TreeSet<>();
            tSet.addAll(expenserows);// Added set in TreeSet to order data according to Sr No.
        }
        return tSet;
    }

    public void setExpenserows(Set<ExpenseGRDetail> expenserows) {
        this.expenserows = expenserows;
    }

    public boolean isIsExpenseType() {
        return isExpenseType;
    }

    public void setIsExpenseType(boolean isExpenseType) {
        this.isExpenseType = isExpenseType;
    }

    public String getFob() {
        return fob;
    }

    public void setFob(String fob) {
        this.fob = fob;
    }

    public String getShipvia() {
        return shipvia;
    }

    public GoodsReceipt getParentInvoice() {
        return parentInvoice;
    }

    public void setParentInvoice(GoodsReceipt parentInvoice) {
        this.parentInvoice = parentInvoice;
    }

    public void setShipvia(String shipvia) {
        this.shipvia = shipvia;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public boolean isFixedAssetInvoice() {
        return fixedAssetInvoice;
    }

    public void setFixedAssetInvoice(boolean fixedAssetInvoice) {
        this.fixedAssetInvoice = fixedAssetInvoice;
    }

    public int getPendingapproval() {
        return pendingapproval;
    }

    public void setPendingapproval(int pendingapproval) {
        this.pendingapproval = pendingapproval;
    }

    public Projreport_Template getTemplateid() {
        return templateid;
    }

    public void setTemplateid(Projreport_Template templateid) {
        this.templateid = templateid;
    }

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public double getExchangeRateForOpeningTransaction() {
        return exchangeRateForOpeningTransaction;
    }

    public void setExchangeRateForOpeningTransaction(double exchangeRateForOpeningTransaction) {
        this.exchangeRateForOpeningTransaction = exchangeRateForOpeningTransaction;
    }

    public boolean isConversionRateFromCurrencyToBase() {
        return conversionRateFromCurrencyToBase;
    }

    public void setConversionRateFromCurrencyToBase(boolean conversionRateFromCurrencyToBase) {
        this.conversionRateFromCurrencyToBase = conversionRateFromCurrencyToBase;
    }

    public boolean isIsOpeningBalenceInvoice() {
        return isOpeningBalenceInvoice;
    }

    public void setIsOpeningBalenceInvoice(boolean isOpeningBalenceInvoice) {
        this.isOpeningBalenceInvoice = isOpeningBalenceInvoice;
    }

    public boolean isNormalInvoice() {
        return normalInvoice;
    }

    public void setNormalInvoice(boolean normalInvoice) {
        this.normalInvoice = normalInvoice;
    }

    public double getOpeningBalanceAmountDue() {
        return openingBalanceAmountDue;
    }

    public void setOpeningBalanceAmountDue(double openingBalanceAmountDue) {
        this.openingBalanceAmountDue = openingBalanceAmountDue;
    }

    public double getOriginalOpeningBalanceAmount() {
        return originalOpeningBalanceAmount;
    }

    public void setOriginalOpeningBalanceAmount(double originalOpeningBalanceAmount) {
        this.originalOpeningBalanceAmount = originalOpeningBalanceAmount;
    }

    public double getOriginalOpeningBalanceBaseAmount() {
        return originalOpeningBalanceBaseAmount;
    }

    public void setOriginalOpeningBalanceBaseAmount(double originalOpeningBalanceBaseAmount) {
        this.originalOpeningBalanceBaseAmount = originalOpeningBalanceBaseAmount;
    }

    public double getOpeningBalanceBaseAmountDue() {
        return openingBalanceBaseAmountDue;
    }

    public void setOpeningBalanceBaseAmountDue(double openingBalanceBaseAmountDue) {
        this.openingBalanceBaseAmountDue = openingBalanceBaseAmountDue;
    }
    
    public Date getPartyInvoiceDate() {
        return partyInvoiceDate;
    }

    public void setPartyInvoiceDate(Date partyInvoiceDate) {
        this.partyInvoiceDate = partyInvoiceDate;
    }

    public String getPartyInvoiceNumber() {
        return partyInvoiceNumber;
    }

    public void setPartyInvoiceNumber(String partyInvoiceNumber) {
        this.partyInvoiceNumber = partyInvoiceNumber;
    }

    public MasterItem getMasterSalesPerson() {
        return masterSalesPerson;
    }

    public void setMasterSalesPerson(MasterItem masterSalesPerson) {
        this.masterSalesPerson = masterSalesPerson;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
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

    public boolean isGstIncluded() {
        return gstIncluded;
    }

    public void setGstIncluded(boolean gstIncluded) {
        this.gstIncluded = gstIncluded;
    }

    public double getShiplength() {
        return shiplength;
    }

    public void setShiplength(double shiplength) {
        this.shiplength = shiplength;
    }

    public String getInvoicetype() {
        return invoicetype;
    }

    public void setInvoicetype(String invoicetype) {
        this.invoicetype = invoicetype;
    }

    public boolean isCapitalGoodsAcquired() {
        return capitalGoodsAcquired;
    }

    public void setCapitalGoodsAcquired(boolean capitalGoodsAcquired) {
        this.capitalGoodsAcquired = capitalGoodsAcquired;
    }
    
    public double getInvoiceamountdue() {
        return invoiceamountdue;
    }

    public void setInvoiceamountdue(double invoiceamountdue) {
        this.invoiceamountdue = invoiceamountdue;
    }

    public int getBadDebtType() {
        return badDebtType;
    }

    public void setBadDebtType(int badDebtType) {
        this.badDebtType = badDebtType;
    }

    public int getClaimedPeriod() {
        return claimedPeriod;
    }

    public void setClaimedPeriod(int claimedPeriod) {
        this.claimedPeriod = claimedPeriod;
    }

    public Date getDebtClaimedDate() {
        return debtClaimedDate;
    }

    public void setDebtClaimedDate(Date debtClaimedDate) {
        this.debtClaimedDate = debtClaimedDate;
    }

//    public Date getDebtRecoveredDate() {
//        return debtRecoveredDate;
//    }
//
//    public void setDebtRecoveredDate(Date debtRecoveredDate) {
//        this.debtRecoveredDate = debtRecoveredDate;
//    }

    public boolean isImportService() {
        return importService;
    }

    public void setImportService(boolean importService) {
        this.importService = importService;
    }

    public OpeningBalanceVendorInvoiceCustomData getOpeningBalanceVendorInvoiceCustomData() {
        return openingBalanceVendorInvoiceCustomData;
    }

    public void setOpeningBalanceVendorInvoiceCustomData(OpeningBalanceVendorInvoiceCustomData openingBalanceVendorInvoiceCustomData) {
        this.openingBalanceVendorInvoiceCustomData = openingBalanceVendorInvoiceCustomData;
    }

	public double getGstCurrencyRate() {
        return gstCurrencyRate;
    }

    public void setGstCurrencyRate(double gstCurrencyRate) {
        this.gstCurrencyRate = gstCurrencyRate;
    }

    public boolean isIsconsignment() {
        return isconsignment;
    }

    public void setIsconsignment(boolean isconsignment) {
        this.isconsignment = isconsignment;
    }

    public boolean isIsMRPJobWorkIN() {
        return isMRPJobWorkIN;
    }

    public void setIsMRPJobWorkIN(boolean isMRPJobWorkIN) {
        this.isMRPJobWorkIN = isMRPJobWorkIN;
    }
  
    public boolean isRetailPurchase() {
        return retailPurchase;
    }

    public void setRetailPurchase(boolean retailPurchase) {
        this.retailPurchase = retailPurchase;
    }

    public Boolean getTermsincludegst() {
        return termsincludegst;
    }

    public void setTermsincludegst(Boolean termsincludegst) {
        this.termsincludegst = termsincludegst;
    }

    public RepeatedInvoices getRepeateInvoice() {
        return repeateInvoice;
    }

    public void setRepeateInvoice(RepeatedInvoices repeateInvoice) {
        this.repeateInvoice = repeateInvoice;
    }
    
    public int getApprovestatuslevel() {
        return approvestatuslevel;
    }

    public void setApprovestatuslevel(int approvestatuslevel) {
        this.approvestatuslevel = approvestatuslevel;
    }   
     public boolean isIsOpenInGR() {
        return isOpenInGR;
    }

    public void setIsOpenInGR(boolean isOpenInGR) {
        this.isOpenInGR = isOpenInGR;
    }
    
    public Date getAmountDueDate() {
        return amountDueDate;
    }

    public void setAmountDueDate(Date amountDueDate) {
        this.amountDueDate = amountDueDate;
    }

    public double getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(double invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public double getInvoiceAmountInBase() {
        return invoiceAmountInBase;
    }

    public void setInvoiceAmountInBase(double invoiceAmountInBase) {
        this.invoiceAmountInBase = invoiceAmountInBase;
    }
    public double getInvoiceAmountDueInBase() {
        return invoiceAmountDueInBase;
    }

    public void setInvoiceAmountDueInBase(double invoiceAmountDueInBase) {
        this.invoiceAmountDueInBase = invoiceAmountDueInBase;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getDiscountAmountInBase() {
        return discountAmountInBase;
    }

    public void setDiscountAmountInBase(double discountAmountInBase) {
        this.discountAmountInBase = discountAmountInBase;
    }

    public double getClaimAmountDue() {
        return claimAmountDue;
    }

    public void setClaimAmountDue(double claimAmountDue) {
        this.claimAmountDue = claimAmountDue;
    }

    public boolean isIsExciseInvoice() {
        return isExciseInvoice;
    }

    public void setIsExciseInvoice(boolean isExciseInvoice) {
        this.isExciseInvoice = isExciseInvoice;
    }

    public String getDefaultnatureOfPurchase() {
        return defaultnatureOfPurchase;
    }

    public void setDefaultnatureOfPurchase(String defaultnatureOfPurchase) {
        this.defaultnatureOfPurchase = defaultnatureOfPurchase;
    }
    
    public String getManufacturerType() {
        return manufacturerType;
    }

    public void setManufacturerType(String manufacturerType) {
        this.manufacturerType = manufacturerType;
    }

    public String getFormtype() {
        return formtype;
    }
    public void setFormtype(String formtype) {
        this.formtype = formtype;
    }
    
    public boolean isGtaapplicable() {
        return gtaapplicable;
    }

    public void setGtaapplicable(boolean gtaapplicable) {
        this.gtaapplicable = gtaapplicable;
    }
    
    public String getFormno() {
        return formno;
    }
    public void setFormno(String formno) {
        this.formno = formno;
    }
    public Date getFormdate() {
        return formdate;
    }
    public void setFormdate(Date formdate) {
        this.formdate = formdate;
    }
    public String getFormseriesno() {
        return formseriesno;
    }
    public void setFormseriesno(String formseriesno) {
        this.formseriesno = formseriesno;
    }
    public double getFormamount() {
        return formamount;
    }
    public void setFormamount(double formamount) {
        this.formamount = formamount;
    }
    public String getFormstatus() {
        return formstatus;
    }
    public void setFormstatus(String formstatus) {
        this.formstatus = formstatus;
    }

//    public ModuleTemplate getModuletemplateid() {
//        return moduletemplateid;
//    }
//
//    public void setModuletemplateid(ModuleTemplate moduletemplateid) {
//        this.moduletemplateid = moduletemplateid;
//    }
     public boolean isIsFromPOS() {
        return isFromPOS;
    }

    public void setIsFromPOS(boolean isFromPOS) {
        this.isFromPOS = isFromPOS;
    }

    public String getExcisepaidJE() {
        return excisepaidJE;
    }

    public void setExcisepaidJE(String excisepaidJE) {
        this.excisepaidJE = excisepaidJE;
    }

    public int getIsCenvatAdjust() {
        return isCenvatAdjust;
    }

    public void setIsCenvatAdjust(int isCenvatAdjust) {
        this.isCenvatAdjust = isCenvatAdjust;
    }
    
    public String getDateAfterPreffixValue() {
        return dateAfterPreffixValue;
    }

    public void setDateAfterPreffixValue(String dateAfterPreffixValue) {
        this.dateAfterPreffixValue = dateAfterPreffixValue;
    }

    public double getTdsRate() {
        return tdsRate;
    }

    public void setTdsRate(double tdsRate) {
        this.tdsRate = tdsRate;
    }

    public double getTdsAmount() {
        return tdsAmount;
    }

    public void setTdsAmount(double tdsAmount) {
        this.tdsAmount = tdsAmount;
    }
    
    /**
     * @return the taxamount
     */
    public double getTaxamount() {
        return taxamount;
    }

    /**
     * @param taxamount the taxamount to set
     */
    public void setTaxamount(double taxamount) {
        this.taxamount = taxamount;
    }

    /**
     * @return the taxamountinbase
     */
    public double getTaxamountinbase() {
        return taxamountinbase;
    }

    /**
     * @param taxamountinbase the taxamountinbase to set
     */
    public void setTaxamountinbase(double taxamountinbase) {
        this.taxamountinbase = taxamountinbase;
    }

    /**
     * @return the excludingGstAmount
     */
    public double getExcludingGstAmount() {
        return excludingGstAmount;
    }

    /**
     * @param excludingGstAmount the excludingGstAmount to set
     */
    public void setExcludingGstAmount(double excludingGstAmount) {
        this.excludingGstAmount = excludingGstAmount;
    }

    /**
     * @return the excludingGstAmountInBase
     */
    public double getExcludingGstAmountInBase() {
        return excludingGstAmountInBase;
    }

    /**
     * @param excludingGstAmountInBase the excludingGstAmountInBase to set
     */
    public void setExcludingGstAmountInBase(double excludingGstAmountInBase) {
        this.excludingGstAmountInBase = excludingGstAmountInBase;
    }

    public String getSupplierInvoiceNo() {
        return supplierInvoiceNo;
    }

    public void setSupplierInvoiceNo(String supplierInvoiceNo) {
        this.supplierInvoiceNo = supplierInvoiceNo;
    }

    public Set<GoodsReceiptPaymentMapping> getAdvancepaymentrows() {
        return advancepaymentrows;
    }
    public void setAdvancepaymentrows(Set<GoodsReceiptPaymentMapping> advancepaymentrows) {
        this.advancepaymentrows = advancepaymentrows;
    }

    public double getTotalAdvanceTDSAdjustmentAmt() {
        return TotalAdvanceTDSAdjustmentAmt;
    }
    public void setTotalAdvanceTDSAdjustmentAmt(double TotalAdvanceTDSAdjustmentAmt) {
        this.TotalAdvanceTDSAdjustmentAmt = TotalAdvanceTDSAdjustmentAmt;
    }

    public LandingCostCategory getLandingCostCategory() {
        return landingCostCategory;
    }

    public void setLandingCostCategory(LandingCostCategory landingCostCategory) {
        this.landingCostCategory = landingCostCategory;
    }

    public int getTdsPaidFlag() {
        return tdsPaidFlag;
    }

    public void setTdsPaidFlag(int tdsPaidFlag) {
        this.tdsPaidFlag = tdsPaidFlag;
    }

    public String getTdsPayment() {
        return tdsPayment;
    }

    public void setTdsPayment(String tdsPayment) {
        this.tdsPayment = tdsPayment;
    }

    public int getTdsMasterRateRuleId() {
        return tdsMasterRateRuleId;
    }
    public void setTdsMasterRateRuleId(int tdsMasterRateRuleId) {
        this.tdsMasterRateRuleId = tdsMasterRateRuleId;
    }

    public Set<LccManualWiseProductAmount> getLccmanualwiseproductamount() {
        return lccmanualwiseproductamount;
    }

    public void setLccmanualwiseproductamount(Set<LccManualWiseProductAmount> lccmanualwiseproductamount) {
        this.lccmanualwiseproductamount = lccmanualwiseproductamount;
    }

    public String getTdsInterestPayment() {
        return tdsInterestPayment;
    }
    public void setTdsInterestPayment(String tdsInterestPayment) {
        this.tdsInterestPayment = tdsInterestPayment;
    }
    public int getTdsInterestPaidFlag() {
        return tdsInterestPaidFlag;
    }
    public void setTdsInterestPaidFlag(int tdsInterestPaidFlag) {
        this.tdsInterestPaidFlag = tdsInterestPaidFlag;
    }
    
    public boolean isApplyTaxToTerms() {
        return applyTaxToTerms;
}

    public void setApplyTaxToTerms(boolean applyTaxToTerms) {
        this.applyTaxToTerms = applyTaxToTerms;
    }

    public boolean isIsTDSApplicable() {
        return isTDSApplicable;
    }

    public void setIsTDSApplicable(boolean isTDSApplicable) {
        this.isTDSApplicable = isTDSApplicable;
    }

    public String getImportDeclarationNo() {
        return importDeclarationNo;
    }

    public void setImportDeclarationNo(String importDeclarationNo) {
        this.importDeclarationNo = importDeclarationNo;
    }

    public boolean isIsMerchantExporter() {
        return isMerchantExporter;
    }

    public void setIsMerchantExporter(boolean isMerchantExporter) {
        this.isMerchantExporter = isMerchantExporter;
    }

    public boolean isIsCreditable() {
        return isCreditable;
    }

    public void setIsCreditable(boolean isCreditable) {
        this.isCreditable = isCreditable;
    }
    
}