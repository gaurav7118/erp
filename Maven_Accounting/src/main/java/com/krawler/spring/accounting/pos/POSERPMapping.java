/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.pos;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;

/**
 *
 * @author krawler
 */
public class POSERPMapping {
    private String ID;
    private String walkinCustomer; // Walkin customer is default used when customer value is not passed from POS
    private String cashOutAccountId; //When we do make payment against GL then this accountid id is used for cashout
    private String depositAccountId; //when we deposit cash amount in bank then this deposit account is used
    private String paymentMethodId; // Payment method is used to determine the payment type and is used in multi payment method
    private String storeid; // This is the store id passed not location(i.e.Outlet)
    private String invoiceSequenceFormat;  // Used to determine invoice sequenceformat for store
    private String deliveryOrderSequenceFormat; // Used to determine delivery order sequenceformat for store
    private String salesreturnSequenceFormat;// Used to determine salesreturn sequenceformat for store
    private String creditnoteSequenceFormat;// Used to determine credit note sequenceformat for store
    private String makePaymentSequenceFormat;// Used to determine make payment sequenceformat for store
    private String receivePaymentSequenceFormat;// Used to determine receive payment sequenceformat for store
    private String salesOrderSequenceFormat;// Used to determine sales order sequenceformat for store
    private boolean isCloseRegisterMultipleTimes;   // Allow to user open register multiple times in POS APP 
    private long createdon; // mapping creation date in long
    private long updatedon;
    private Company company;
    private User userid;
    
    public final static String WalkinCustomer ="walkinCustomer";
    public final static String WalkinCustomer_Name ="walkinCustomerName";
    public final static String PAYMENT_METHOD_ID ="paymentMethodId";
    public final static String PAYMENT_METHOD_NAME ="paymentMethodName";
    public final static String PAYMENT_METHOD_TYPE ="paymentMethodType";
    public final static String StoreId = "storeid";
    public final static String Store_Name = "storeName";
    public final static String  isAllowCloseRegisterMultipleTimes= "allowcloseregistermultipletimesFlag";
    public final static String  CASHOUT_ACCOUNT_ID="cashOutAccountId";
    public final static String  CASHOUT_ACCOUNT_Name="cashOutAccountName";
    public final static String  DEPOSIT_ACCOUNT_ID="depositAccountId";
    public final static String  DEPOSIT_ACCOUNT_Name="depositAccountName";
    public final static String  CN_SEQUENCEFORMAT="cnsequenceformat";
    public final static String  CN_SEQUENCEFORMAT_NAME="cnsequenceformatName";
    public final static String  INVOICE_SEQUENCEFORMAT="invoicesequenceformat";
    public final static String  INVOICE_SEQUENCEFORMAT_NAME="invoicesequenceformatName";
    public final static String  DO_SEQUENCEFORMAT="dosequenceformat";
    public final static String  DO_SEQUENCEFORMAT_NAME="dosequenceformatName";
    public final static String  SALESRETRUN_SEQUENCEFORMAT="srsequenceformat";
    public final static String  SALESRETRUN_SEQUENCEFORMAT_NAME="srsequenceformatName";
    public final static String  MAKEPAYMENT_SEQUENCEFORMAT="mpsequenceformat";
    public final static String  MAKEPAYMENT_SEQUENCEFORMAT_NAME="mpsequenceformatName";
    public final static String  RECEIVEPAYMENT_SEQUENCEFORMAT="rpsequenceformat";
    public final static String  RECEIVEPAYMENT_SEQUENCEFORMAT_NAME="rpsequenceformatName";
    public final static String  SALESORDER_SEQUENCEFORMAT="salesordersequenceformat";
    public final static String  SALESORDER_SEQUENCEFORMAT_NAME="salesordersequenceformatName";
    public final static String  IS_SAVE="isSave";
    public final static String  CREATED_ON="createdon";
    public final static String  UPDATED_ON="updatedon";
    

    public String getDepositAccountId() {
        return depositAccountId;
    }

    public void setDepositAccountId(String depositAccountId) {
        this.depositAccountId = depositAccountId;
    }

    public String getSalesOrderSequenceFormat() {
        return salesOrderSequenceFormat;
    }

    public void setSalesOrderSequenceFormat(String salesOrderSequenceFormat) {
        this.salesOrderSequenceFormat = salesOrderSequenceFormat;
    }

    public long getCreatedon() {
        return createdon;
    }

    public void setCreatedon(long createdon) {
        this.createdon = createdon;
    }

    public long getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(long updatedon) {
        this.updatedon = updatedon;
    }

    public String getCreditnoteSequenceFormat() {
        return creditnoteSequenceFormat;
    }

    public void setCreditnoteSequenceFormat(String creditnoteSequenceFormat) {
        this.creditnoteSequenceFormat = creditnoteSequenceFormat;
    }

    public String getDeliveryOrderSequenceFormat() {
        return deliveryOrderSequenceFormat;
    }

    public void setDeliveryOrderSequenceFormat(String deliveryOrderSequenceFormat) {
        this.deliveryOrderSequenceFormat = deliveryOrderSequenceFormat;
    }

    public String getInvoiceSequenceFormat() {
        return invoiceSequenceFormat;
    }

    public void setInvoiceSequenceFormat(String invoiceSequenceFormat) {
        this.invoiceSequenceFormat = invoiceSequenceFormat;
    }

    public String getMakePaymentSequenceFormat() {
        return makePaymentSequenceFormat;
    }

    public void setMakePaymentSequenceFormat(String makePaymentSequenceFormat) {
        this.makePaymentSequenceFormat = makePaymentSequenceFormat;
    }

    public String getReceivePaymentSequenceFormat() {
        return receivePaymentSequenceFormat;
    }

    public void setReceivePaymentSequenceFormat(String receivePaymentSequenceFormat) {
        this.receivePaymentSequenceFormat = receivePaymentSequenceFormat;
    }

    public String getSalesreturnSequenceFormat() {
        return salesreturnSequenceFormat;
    }

    public void setSalesreturnSequenceFormat(String salesreturnSequenceFormat) {
        this.salesreturnSequenceFormat = salesreturnSequenceFormat;
    }
    
    
    public String getStoreid() {
        return storeid;
    }

    public void setStoreid(String storeid) {
        this.storeid = storeid;
    }

    public User getUserid() {
        return userid;
    }

    public void setUserid(User userid) {
        this.userid = userid;
    }
    
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isIsCloseRegisterMultipleTimes() {
        return isCloseRegisterMultipleTimes;
    }

    public void setIsCloseRegisterMultipleTimes(boolean isCloseRegisterMultipleTimes) {
        this.isCloseRegisterMultipleTimes = isCloseRegisterMultipleTimes;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getCashOutAccountId() {
        return cashOutAccountId;
    }

    public void setCashOutAccountId(String cashOutAccountId) {
        this.cashOutAccountId = cashOutAccountId;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getWalkinCustomer() {
        return walkinCustomer;
    }

    public void setWalkinCustomer(String walkinCustomer) {
        this.walkinCustomer = walkinCustomer;
    }
}
