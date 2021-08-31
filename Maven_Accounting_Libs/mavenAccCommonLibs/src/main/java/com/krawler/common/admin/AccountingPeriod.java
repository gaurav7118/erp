/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class AccountingPeriod {

    private String id;
    private String periodName;
    private Date startDate;
    private Date endDate;
    private AccountingPeriod subPeriodOf;
    private int periodType; //1- Year , 2-Quarter , 3-Month
    private boolean peridClosed;
    private boolean arTransactionClosed;

    private boolean apTransactionClosed;
    private boolean allGLTransactionClosed;
    private Company company;

    private int periodFormat;
    private int yearInPeriodName;
    private User user;
    
    public static final int AccountingPeriod_YEAR = 1;
    public static final int AccountingPeriod_QUARTER = 2;
    public static final int AccountingPeriod_MONTH = 3;
    public static final int AccountingPeriod_FULL_YEAR_SETUP = 4;
    
    public static final int ACCOUNT_RECEIVABLE_LOCK = 1;
    public static final int ACCOUNT_PAYABLE_LOCK = 2;
    public static final int All_GL_TRANSACTION_LOCK = 3;
    public static final int PERIOD_LOCK = 4;
    
    public static final String SAVE_CUSTOMER_QUOTATION = "saveQuotation";
    public static final String SAVE_SALES_ORDER = "saveSalesOrder";
    public static final String UPDATE_SALES_ORDER = "updateLinkedSalesOrder";
    public static final String SAVE_SALES_RETURN = "saveSalesReturn";
    public static final String SAVE_RECEIVE_PAYMENT = "getReceiptObj";
    public static final String SAVE_DELIVERY_ORDER = "saveDeliveryOrder";
     public static final String ADD_CREDIT_NOTE = "addCreditNote";
      public static final String UPDATE_CREDIT_NOTE = "updateCreditNote";
    public static final String SAVE_INVOICE = "addInvoice";
   
    public static Set<String> MethodSetForAccountReceivable = new HashSet();
    static {
        MethodSetForAccountReceivable.add(SAVE_CUSTOMER_QUOTATION);
        MethodSetForAccountReceivable.add(SAVE_SALES_ORDER);
        MethodSetForAccountReceivable.add(SAVE_SALES_RETURN);
        MethodSetForAccountReceivable.add(SAVE_RECEIVE_PAYMENT);
        MethodSetForAccountReceivable.add(SAVE_DELIVERY_ORDER);
        MethodSetForAccountReceivable.add(ADD_CREDIT_NOTE);
        MethodSetForAccountReceivable.add(UPDATE_CREDIT_NOTE);
        MethodSetForAccountReceivable.add(SAVE_INVOICE);
        MethodSetForAccountReceivable.add(UPDATE_SALES_ORDER);
    }
    
    
    public static final String SAVE_GOODS_RECEIPT = "saveGoodsReceiptOrder";
    public static final String SAVE_PURCHASE_ORDER = "savePurchaseOrder";
    public static final String UPDATE_PURCHASE_ORDER = "updateLinkedPurchaseOrder";
    public static final String SAVE_PURCHASE_RETURN = "savePurchaseReturn";
    public static final String SAVE_MAKE_PAYMENT = "getPaymentObj";
    public static final String SAVE_VENDOR_QUOTATION = "saveVendorQuotation";
    public static final String SAVE_PURCHASE_REQUSITION = "savePurchaseRequisition";
    public static final String ADD_DEBIT_NOTE = "addDebitNote";
    public static final String UPDATE_DEBIT_NOTE = "updateDebitNote";
    public static final String ADD_PURCHASE_INVOICE = "addGoodsReceipt";
    
   
   public static Set<String> MethodSetForAccountPayable = new HashSet();    
    static {
        MethodSetForAccountPayable.add(SAVE_GOODS_RECEIPT);
        MethodSetForAccountPayable.add(SAVE_PURCHASE_ORDER);
        MethodSetForAccountPayable.add(SAVE_PURCHASE_RETURN);
        MethodSetForAccountPayable.add(SAVE_MAKE_PAYMENT);
        MethodSetForAccountPayable.add(SAVE_VENDOR_QUOTATION);
        MethodSetForAccountPayable.add(SAVE_PURCHASE_REQUSITION);
        MethodSetForAccountPayable.add(ADD_DEBIT_NOTE);
        MethodSetForAccountPayable.add(UPDATE_DEBIT_NOTE);
        MethodSetForAccountPayable.add(ADD_PURCHASE_INVOICE);
        MethodSetForAccountPayable.add(UPDATE_PURCHASE_ORDER);
    }
    
    public static Set<String> MethodSetForAllGeneralLedger = new HashSet();    
    public static final String SAVE_JOURNAL_ENTRY = "saveJournalEntry";
    public static final String SAVE_REVERSE_JOURNAL_ENTRY = "saveReverseJournalEntry";
    static {
        MethodSetForAllGeneralLedger.add(SAVE_JOURNAL_ENTRY);
	MethodSetForAllGeneralLedger.add(SAVE_REVERSE_JOURNAL_ENTRY);
        MethodSetForAllGeneralLedger.add(SAVE_MAKE_PAYMENT);
        MethodSetForAllGeneralLedger.add(ADD_DEBIT_NOTE);
        MethodSetForAllGeneralLedger.add(UPDATE_DEBIT_NOTE);
        MethodSetForAllGeneralLedger.add(ADD_PURCHASE_INVOICE);

        MethodSetForAllGeneralLedger.add(SAVE_RECEIVE_PAYMENT);
        MethodSetForAllGeneralLedger.add(ADD_CREDIT_NOTE);
        MethodSetForAllGeneralLedger.add(UPDATE_CREDIT_NOTE);
        MethodSetForAllGeneralLedger.add(SAVE_INVOICE);
    }
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getPeriodFormat() {
        return periodFormat;
    }

    public void setPeriodFormat(int periodFormat) {
        this.periodFormat = periodFormat;
    }

    public int getYearInPeriodName() {
        return yearInPeriodName;
    }

    public void setYearInPeriodName(int yearInPeriodName) {
        this.yearInPeriodName = yearInPeriodName;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AccountingPeriod getSubPeriodOf() {
        return subPeriodOf;
    }

    public void setSubPeriodOf(AccountingPeriod subPeriodOf) {
        this.subPeriodOf = subPeriodOf;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getPeriodType() {
        return periodType;
    }

    public void setPeriodType(int periodType) {
        this.periodType = periodType;
    }

    public boolean isPeridClosed() {
        return peridClosed;
    }

    public void setPeridClosed(boolean peridClosed) {
        this.peridClosed = peridClosed;
    }

    public boolean isArTransactionClosed() {
        return arTransactionClosed;
    }

    public void setArTransactionClosed(boolean arTransactionClosed) {
        this.arTransactionClosed = arTransactionClosed;
    }

    public boolean isApTransactionClosed() {
        return apTransactionClosed;
    }

    public void setApTransactionClosed(boolean apTransactionClosed) {
        this.apTransactionClosed = apTransactionClosed;
    }

    public boolean isAllGLTransactionClosed() {
        return allGLTransactionClosed;
    }

    public void setAllGLTransactionClosed(boolean allGLTransactionClosed) {
        this.allGLTransactionClosed = allGLTransactionClosed;
    }

}
