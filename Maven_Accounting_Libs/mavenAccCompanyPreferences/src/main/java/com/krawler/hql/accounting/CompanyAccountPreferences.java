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
import com.krawler.common.admin.CompanyType;
import java.util.Date;

public class CompanyAccountPreferences {

    private String ID;
    private Date financialYearFrom;
    private Date firstFinancialYearFrom;
    private Date GSTApplicableDate;
    private Date bookBeginningFrom;
    private Account discountGiven;
    private Account discountReceived;
    private Account shippingCharges;
//    private Account otherCharges;
    private Account cashAccount;
    private Account foreignexchange;
    private Account unrealisedgainloss;
    private Account depereciationAccount;
    private String journalEntryNumberFormat;
    private int journalEntryNumberFormatStartFrom;
    private String creditNoteNumberFormat;
    private int creditNoteNumberFormatStartFrom;
    private String invoiceNumberFormat;
    private int invoiceNumberFormatStartFrom;
    private String receiptNumberFormat;
    private int receiptNumberFormatStartFrom;
    private String debitNoteNumberFormat;
    private int debitNoteNumberFormatStartFrom;
    private String purchaseOrderNumberFormat;
    private int purchaseOrderNumberFormatStartFrom;
    private String salesOrderNumberFormat;
    private int salesOrderNumberFormatStartFrom;
    private String goodsReceiptNumberFormat;
//     private Account cashoutACCForPOS;  //Select credit account for POS App
     private String paymentMethod;  //Select credit account for POS App
    private int goodsReceiptNumberFormatStartFrom;
    private String cashSaleNumberFormat;
    private int cashSaleNumberFormatStartFrom;
    private String cashPurchaseNumberFormat;
    private int cashPurchaseNumberFormatStartFrom;
    private String paymentNumberFormat;
    private int paymentNumberFormatStartFrom;
    private String billingInvoiceNumberFormat;
    private int billingInvoiceNumberFormatStartFrom;
    private String billingReceiptNumberFormat;
    private int billingReceiptNumberFormatStartFrom;
    private String billingCashSaleNumberFormat;
    private int billingCashSaleNumberFormatStartFrom;
    private String billingGoodsReceiptNumberFormat;
    private int billingGoodsReceiptNumberFormatStartFrom;
    private String billingPaymentNumberFormat;
    private int billingPaymentNumberFormatStartFrom;
    private String billingCashPurchaseNumberFormat;
    private int billingCashPurchaseNumberFormatStartFrom;
    private String billingCreditNoteNumberFormat;
    private int billingCreditNoteNumberFormatStartFrom;
    private String billingDebitNoteNumberFormat;
    private int billingDebitNoteNumberFormatStartFrom;
    private String billingPurchaseOrderNumberFormat;
    private int billingPurchaseOrderNumberFormatStartFrom;
    private String billingSalesOrderNumberFormat;
    private int billingSalesOrderNumberFormatStartFrom;
    private boolean emailInvoice;
    private boolean withoutInventory;
    private boolean withInvUpdate;
    private boolean withoutTax1099;
    private boolean setupDone;
    private CompanyType companyType;
    private Company company;
    private String quotationNumberFormat;
    private int quotationNumberFormatStartFrom;
    private String venQuotationNumberFormat;
    private int venQuotationNumberFormatStartFrom;
    private String requisitionNumberFormat;
    private int requisitionNumberFormatStartFrom;
    private String rfqNumberFormat; //Request for Quotation Format
    private int rfqNumberFormatStartFrom; //Request for Quotation Format
    private boolean currencyChange;
    private boolean countryChange;
    private String productidNumberFormat;
    private int productidNumberFormatStartFrom;
    private String gstNumber;
    private String companyUEN;
    private MasterItem industryCode;
    private String iafVersion;
    private String taxNumber;
    private Account expenseAccount;
    private Account customerdefaultaccount;
    private Account vendordefaultaccount;
    private Account liabilityAccount;
    private Account roundingDifferenceAccount;
    private boolean editTransaction;
    private boolean editLinkedTransactionQuantity;
    private boolean editLinkedTransactionPrice;
    private boolean deleteTransaction;
    private boolean showchild;
    private String deliveryOrderNumberFormat;
    private int deliveryOrderNumberFormatStartFrom;
    private String salesReturnNumberFormat;
    private int salesReturnNumberFormatStartFrom;
    private String goodsReceiptOrderNumberFormat;
    private int goodsReceiptOrderNumberFormatStartFrom;
    private String descriptionType;
    private String pdffooter;
    private String pdfheader;
    private String pdfpretext;
    private String pdfposttext;
    private boolean inventoryAccountingIntegration;
    private int negativestock;
    private int custcreditcontrol;
    private int chequeNoDuplicate;
    private boolean accountsWithCode;
    private String purchaseReturnNumberFormat;
    private int purchaseReturnNumberFormatStartFrom;
    private int custbudgetcontrol;
    private boolean partNumber;
    private boolean showLeadingZero;
    private boolean dependentField;
    private boolean updateInvLevel;
    private boolean qaApprovalFlow;
    private boolean doClosedStatus = false;
    private boolean ishtmlproddesc;
    private boolean editso;
    private boolean showprodserial;  //Use to Set Default Value Checked Of checkbox to on off the product location and warehouse functionality
    private boolean islocationcompulsory;  //Use to Set Default Value Checked Of checkbox to on off the product location and warehouse functionality
    private boolean iswarehousecompulsory;  //Use to Set Default Value Checked Of checkbox to on off the product location and warehouse functionality
    private boolean isBatchCompulsory;  //Use to Set Default Value Checked Of checkbox to on off the product location and warehouse functionality
    private boolean isSerialCompulsory;
    private boolean memo;
    private boolean sendapprovalmail;
    private String approvalEmails;
    private boolean DOSettings; //Use to Set Default Value Checked Of checkbox Generate DO in CI 
    private boolean GRSettings;//Use to Generate GR in VI
    private Integer quotationindecimalformat; // to display quantity in decimal or integer... if 0 then decimal, 1 then integer
    private boolean shipDateConfiguration; // Used to set 'Due Date' depend on 'Ship Date'
    private boolean unitPriceConfiguration; // Used to show 'Unit Price' in GR/DO/PR/SR
    private int viewDashboard;
    private int amountdigitafterdecimal;
    private int quantitydigitafterdecimal;
    private int unitpricedigitafterdecimal;
    private int uomconversionratedigitafterdecimal;
    private int currencyratedigitafterdecimal;
    private boolean viewDetailsPerm;
    private boolean isrowcompulsory;  //Use to Set Default Value Checked Of checkbox to on off the product row functionality
    private boolean israckcompulsory;  //Use to Set Default Value Checked Of checkbox to on off the product rack functionality
    private boolean isbincompulsory;  //Use to Set Default Value Checked Of checkbox to on off the product bin functionality
    private boolean filterProductByCustomerCategory;  
    private int productSortingFlag;  //Sort By Product as per Name or Product ID
    private int negativeStockSO; // negative stock for SO
    private int negativeStockSICS; // negative stock for SI/CS
    private int negativeStockPR; // negative stock for PR
    private boolean AmountInIndianWord;
    private int inventoryValuationType; // 0- Periodic, 1- Perpetual
    private String theme;  //CSS Class used for extjs theme
    private Account stockadjustmentaccount; //Use to Set default value for Stock Adjustment for Perpetual Inventory valuation  
    private Account inventoryaccount;      //Use to Set default value for Inventory Account for Perpetual Inventory valuation
    private Account cogsaccount;   //Use to Set default value for Cost of Goods Sold Account for Perpetual Inventory valuation
    private boolean qaApprovalFlowInDO;
    private boolean showMarginButton; // flag for Show or Hide Margin Button in Invoice/Sales/Quotation create form //ERM-76
    
    public Account getStockadjustmentaccount() {
        return stockadjustmentaccount;
    }

    public void setStockadjustmentaccount(Account stockadjustmentaccount) {
        this.stockadjustmentaccount = stockadjustmentaccount;
    }

    public Account getInventoryaccount() {
        return inventoryaccount;
    }

    public void setInventoryaccount(Account inventoryaccount) {
        this.inventoryaccount = inventoryaccount;
    }

    public Account getCogsaccount() {
        return cogsaccount;
    }

    public void setCogsaccount(Account cogsaccount) {
        this.cogsaccount = cogsaccount;
    }    
    
    /*
     Working - updateStockAdjustmentEntries :
     If Update Price for Stock OUT transactions is set to true, then SA OUT price will be updated based on the valuation method (Can be updated more than once)
     If Update Price for Stock OUT transactions is set to false, then SA OUT price will be updated based on the valuation method (Only once)
     */
    
    private boolean updateStockAdjustmentEntries;
    /*
     * here, constructor has been defined with amountdigitafterdecimal=2 as
     * default value and quantitydigitafterdecimal=4 with default value So ,
     * whenever a new object for CompanyAccountPreferences will be created ,
     * default values for these parametes will be as mentioned above.
     */
    public CompanyAccountPreferences() {
        super();
        this.amountdigitafterdecimal = 2;
        this.quantitydigitafterdecimal = 4;
        this.unitpricedigitafterdecimal = 2;
        this.uomconversionratedigitafterdecimal = 6;
        this.currencyratedigitafterdecimal = 2;
        this.editTransaction = true;
        this.deleteTransaction = true;
        this.accountsWithCode = true;
        this.negativestock=1; //default is block case case
        this.custcreditcontrol = 2; //default is warn case
        this.chequeNoDuplicate = 0; //default is ingore case
        this.custbudgetcontrol = 2;  //default is warn case
        this.negativeStockPR = 1;   //default is block case case
        this.negativeStockSICS=1;   //default is block case case
        this.negativeStockSO=1;     //default is block case case
    }

    public int getUnitpricedigitafterdecimal() {
        return unitpricedigitafterdecimal;
    }

    public void setUnitpricedigitafterdecimal(int unitpricedigitafterdecimal) {
        this.unitpricedigitafterdecimal = unitpricedigitafterdecimal;
    }

    public int getUomconversionratedigitafterdecimal() {
        return uomconversionratedigitafterdecimal;
    }

    public void setUomconversionratedigitafterdecimal(int uomconversionratedigitafterdecimal) {
        this.uomconversionratedigitafterdecimal = uomconversionratedigitafterdecimal;
    }

    public int getCurrencyratedigitafterdecimal() {
        return currencyratedigitafterdecimal;
    }

    public void setCurrencyratedigitafterdecimal(int currencyratedigitafterdecimal) {
        this.currencyratedigitafterdecimal = currencyratedigitafterdecimal;
    }

    
    public boolean isUnitPriceConfiguration() {
        return unitPriceConfiguration;
    }

    public void setUnitPriceConfiguration(boolean unitPriceConfiguration) {
        this.unitPriceConfiguration = unitPriceConfiguration;
    }

    public boolean isShipDateConfiguration() {
        return shipDateConfiguration;
    }

    public void setShipDateConfiguration(boolean shipDateConfiguration) {
        this.shipDateConfiguration = shipDateConfiguration;
    }

    public Integer getQuotationindecimalformat() {
        return quotationindecimalformat;
    }

    public void setQuotationindecimalformat(Integer quotationindecimalformat) {
        this.quotationindecimalformat = quotationindecimalformat;
    }

    public boolean isIsSerialCompulsory() {
        return isSerialCompulsory;
    }

    public void setIsSerialCompulsory(boolean isSerialCompulsory) {
        this.isSerialCompulsory = isSerialCompulsory;
    }

    public boolean isMemo() {
        return memo;
    }

    public void setMemo(boolean memo) {
        this.memo = memo;
    }

    public String getApprovalEmails() {
        return approvalEmails;
    }

    public void setApprovalEmails(String approvalEmails) {
        this.approvalEmails = approvalEmails;
    }

    public boolean isSendapprovalmail() {
        return sendapprovalmail;
    }

    public void setSendapprovalmail(boolean sendapprovalmail) {
        this.sendapprovalmail = sendapprovalmail;
    }

    public boolean isEditso() {
        return editso;
    }

    public void setEditso(boolean editso) {
        this.editso = editso;
    }

    public boolean isShowprodserial() {
        return showprodserial;
    }

    public void setShowprodserial(boolean showprodserial) {
        this.showprodserial = showprodserial;
    }

    public int getInvoiceNumberFormatStartFrom() {
        return invoiceNumberFormatStartFrom;
    }

    public void setInvoiceNumberFormatStartFrom(int invoiceNumberFormatStartFrom) {
        this.invoiceNumberFormatStartFrom = invoiceNumberFormatStartFrom;
    }

    public int getSalesOrderNumberFormatStartFrom() {
        return salesOrderNumberFormatStartFrom;
    }

    public void setSalesOrderNumberFormatStartFrom(int salesOrderNumberFormatStartFrom) {
        this.salesOrderNumberFormatStartFrom = salesOrderNumberFormatStartFrom;
    }

    public boolean isEditLinkedTransactionPrice() {
        return editLinkedTransactionPrice;
    }

    public void setEditLinkedTransactionPrice(boolean editLinkedTransactionPrice) {
        this.editLinkedTransactionPrice = editLinkedTransactionPrice;
    }

    public boolean isEditLinkedTransactionQuantity() {
        return editLinkedTransactionQuantity;
    }

    public void setEditLinkedTransactionQuantity(boolean editLinkedTransactionQuantity) {
        this.editLinkedTransactionQuantity = editLinkedTransactionQuantity;
    }

    public boolean isPartNumber() {
        return partNumber;
    }

    public void setPartNumber(boolean partNumber) {
        this.partNumber = partNumber;
    }

    public boolean isShowchild() {
        return showchild;
    }

    public boolean isShowLeadingZero() {
        return showLeadingZero;
    }

    public boolean isDOSettings() {
        return DOSettings;
    }

    public boolean getDOSettings() {
        return DOSettings;
    }

    public boolean getGRSettings() {
        return GRSettings;
    }

    public void setDOSettings(boolean DOSettings) {
        this.DOSettings = DOSettings;
    }

    public boolean isGRSettings() {
        return GRSettings;
    }

    public void setGRSettings(boolean GRSettings) {
        this.GRSettings = GRSettings;
    }

    public void setShowLeadingZero(boolean showLeadingZero) {
        this.showLeadingZero = showLeadingZero;
    }

    public void setShowchild(boolean showchild) {
        this.showchild = showchild;
    }

    public boolean isDeleteTransaction() {
        return deleteTransaction;
    }

    public int getCustbudgetcontrol() {
        return custbudgetcontrol;
    }

    public void setCustbudgetcontrol(int custbudgetcontrol) {
        this.custbudgetcontrol = custbudgetcontrol;
    }

    public void setDeleteTransaction(boolean deleteTransaction) {
        this.deleteTransaction = deleteTransaction;
    }

    public boolean isEditTransaction() {
        return editTransaction;
    }

    public void setEditTransaction(boolean editTransaction) {
        this.editTransaction = editTransaction;
    }

    public Account getExpenseAccount() {
        return expenseAccount;
    }

    public void setExpenseAccount(Account expenseAccount) {
        this.expenseAccount = expenseAccount;
    }

    public Account getLiabilityAccount() {
        return liabilityAccount;
    }

    public void setLiabilityAccount(Account liabilityAccount) {
        this.liabilityAccount = liabilityAccount;
    }

    public void setIafVersion(String iafVersion) {
        this.iafVersion = iafVersion;
    }

    public String getIafVersion() {
        return iafVersion;
    }

    public String getCompanyUEN() {
        return companyUEN;
    }

    public void setCompanyUEN(String companyUEN) {
        this.companyUEN = companyUEN;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public String getProductidNumberFormat() {
        return productidNumberFormat;
    }

    public void setProductidNumberFormat(String productidNumberFormat) {
        this.productidNumberFormat = productidNumberFormat;
    }

    public boolean isCurrencyChange() {
        return currencyChange;
    }

    public void setCurrencyChange(boolean currencyChange) {
        this.currencyChange = currencyChange;
    }

    public boolean isCountryChange() {
        return countryChange;
    }

    public void setCountryChange(boolean countryChange) {
        this.countryChange = countryChange;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Date getBookBeginningFrom() {
        return bookBeginningFrom;
    }

    public void setBookBeginningFrom(Date bookBeginningFrom) {
        this.bookBeginningFrom = bookBeginningFrom;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getFinancialYearFrom() {
        return financialYearFrom;
    }

    public void setFinancialYearFrom(Date financialYearFrom) {
        this.financialYearFrom = financialYearFrom;
    }

    public Date getFirstFinancialYearFrom() {
        return firstFinancialYearFrom;
    }

    public void setFirstFinancialYearFrom(Date firstFinancialYearFrom) {
        this.firstFinancialYearFrom = firstFinancialYearFrom;
    }

    public Account getDiscountGiven() {
        return discountGiven;
    }

    public void setDiscountGiven(Account discountGiven) {
        this.discountGiven = discountGiven;
    }

    public Account getDiscountReceived() {
        return discountReceived;
    }

    public void setDiscountReceived(Account discountReceived) {
        this.discountReceived = discountReceived;
    }

//    public Account getOtherCharges() {
//        return otherCharges;
//    }
//
//    public void setOtherCharges(Account otherCharges) {
//        this.otherCharges = otherCharges;
//    }

    public Account getShippingCharges() {
        return shippingCharges;
    }

    public void setShippingCharges(Account shippingCharges) {
        this.shippingCharges = shippingCharges;
    }

    public Account getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(Account cashAccount) {
        this.cashAccount = cashAccount;
    }

    public String getCreditNoteNumberFormat() {
        return creditNoteNumberFormat;
    }

    public void setCreditNoteNumberFormat(String creditNoteNumberFormat) {
        this.creditNoteNumberFormat = creditNoteNumberFormat;
    }

    public String getInvoiceNumberFormat() {
        return invoiceNumberFormat;
    }

    public void setInvoiceNumberFormat(String invoiceNumberFormat) {
        this.invoiceNumberFormat = invoiceNumberFormat;
    }

    public String getJournalEntryNumberFormat() {
        return journalEntryNumberFormat;
    }

    public void setJournalEntryNumberFormat(String journalEntryNumberFormat) {
        this.journalEntryNumberFormat = journalEntryNumberFormat;
    }

    public String getReceiptNumberFormat() {
        return receiptNumberFormat;
    }

    public void setReceiptNumberFormat(String receiptNumberFormat) {
        this.receiptNumberFormat = receiptNumberFormat;
    }

    public String getDebitNoteNumberFormat() {
        return debitNoteNumberFormat;
    }

    public void setDebitNoteNumberFormat(String debitNoteNumberFormat) {
        this.debitNoteNumberFormat = debitNoteNumberFormat;
    }

    public String getPaymentNumberFormat() {
        return paymentNumberFormat;
    }

    public void setPaymentNumberFormat(String paymentNumberFormat) {
        this.paymentNumberFormat = paymentNumberFormat;
    }

    public String getPurchaseOrderNumberFormat() {
        return purchaseOrderNumberFormat;
    }

    public void setPurchaseOrderNumberFormat(String purchaseOrderNumberFormat) {
        this.purchaseOrderNumberFormat = purchaseOrderNumberFormat;
    }

    public String getGoodsReceiptNumberFormat() {
        return goodsReceiptNumberFormat;
    }

    public void setGoodsReceiptNumberFormat(String goodsReceiptNumberFormat) {
        this.goodsReceiptNumberFormat = goodsReceiptNumberFormat;
    }

    public String getSalesOrderNumberFormat() {
        return salesOrderNumberFormat;
    }

    public void setSalesOrderNumberFormat(String salesOrderNumberFormat) {
        this.salesOrderNumberFormat = salesOrderNumberFormat;
    }

    public String getCashPurchaseNumberFormat() {
        return cashPurchaseNumberFormat;
    }

    public void setCashPurchaseNumberFormat(String cashPurchaseNumberFormat) {
        this.cashPurchaseNumberFormat = cashPurchaseNumberFormat;
    }

    public String getCashSaleNumberFormat() {
        return cashSaleNumberFormat;
    }

    public void setCashSaleNumberFormat(String cashSaleNumberFormat) {
        this.cashSaleNumberFormat = cashSaleNumberFormat;
    }

    public String getBillingInvoiceNumberFormat() {
        return billingInvoiceNumberFormat;
    }

    public void setBillingInvoiceNumberFormat(String billingInvoiceNumberFormat) {
        this.billingInvoiceNumberFormat = billingInvoiceNumberFormat;
    }

    public String getBillingReceiptNumberFormat() {
        return billingReceiptNumberFormat;
    }

    public void setBillingReceiptNumberFormat(String billingReceiptNumberFormat) {
        this.billingReceiptNumberFormat = billingReceiptNumberFormat;
    }

    public String getSalesReturnNumberFormat() {
        return salesReturnNumberFormat;
    }

    public void setSalesReturnNumberFormat(String salesReturnNumberFormat) {
        this.salesReturnNumberFormat = salesReturnNumberFormat;
    }

    public boolean isEmailInvoice() {
        return emailInvoice;
    }

    public void setEmailInvoice(boolean emailInvoice) {
        this.emailInvoice = emailInvoice;
    }

    public boolean isWithoutInventory() {
        return withoutInventory;
    }

    public boolean isWithInvUpdate() {
        return withInvUpdate;
    }

    public void setWithoutInventory(boolean withoutInventory) {
        this.withoutInventory = withoutInventory;
    }

    public void setWithInvUpdate(boolean withInvUpdate) {
        this.withInvUpdate = withInvUpdate;
    }

    public boolean isWithoutTax1099() {
        return withoutTax1099;
    }

    public void setWithoutTax1099(boolean withoutTax1099) {
        this.withoutTax1099 = withoutTax1099;
    }

    public String getBillingCashPurchaseNumberFormat() {
        return billingCashPurchaseNumberFormat;
    }

    public void setBillingCashPurchaseNumberFormat(String billingCashPurchaseNumberFormat) {
        this.billingCashPurchaseNumberFormat = billingCashPurchaseNumberFormat;
    }

    public String getBillingCashSaleNumberFormat() {
        return billingCashSaleNumberFormat;
    }

    public void setBillingCashSaleNumberFormat(String billingCashSaleNumberFormat) {
        this.billingCashSaleNumberFormat = billingCashSaleNumberFormat;
    }

    public String getBillingGoodsReceiptNumberFormat() {
        return billingGoodsReceiptNumberFormat;
    }

    public void setBillingGoodsReceiptNumberFormat(String billingGoodsReceiptNumberFormat) {
        this.billingGoodsReceiptNumberFormat = billingGoodsReceiptNumberFormat;
    }

    public String getBillingPaymentNumberFormat() {
        return billingPaymentNumberFormat;
    }

    public void setBillingPaymentNumberFormat(String billingPaymentNumberFormat) {
        this.billingPaymentNumberFormat = billingPaymentNumberFormat;
    }

    public String getBillingCreditNoteNumberFormat() {
        return billingCreditNoteNumberFormat;
    }

    public void setBillingCreditNoteNumberFormat(String billingCreditNoteNumberFormat) {
        this.billingCreditNoteNumberFormat = billingCreditNoteNumberFormat;
    }

    public String getBillingDebitNoteNumberFormat() {
        return billingDebitNoteNumberFormat;
    }

    public void setBillingDebitNoteNumberFormat(String billingDebitNoteNumberFormat) {
        this.billingDebitNoteNumberFormat = billingDebitNoteNumberFormat;
    }

    public String getBillingPurchaseOrderNumberFormat() {
        return billingPurchaseOrderNumberFormat;
    }

    public void setBillingPurchaseOrderNumberFormat(String billingPurchaseOrderNumberFormat) {
        this.billingPurchaseOrderNumberFormat = billingPurchaseOrderNumberFormat;
    }

    public String getBillingSalesOrderNumberFormat() {
        return billingSalesOrderNumberFormat;
    }

    public void setBillingSalesOrderNumberFormat(String billingSalesOrderNumberFormat) {
        this.billingSalesOrderNumberFormat = billingSalesOrderNumberFormat;
    }

    public Account getForeignexchange() {
        return foreignexchange;
    }

    public void setForeignexchange(Account foreignexchange) {
        this.foreignexchange = foreignexchange;
    }

    public Account getDepereciationAccount() {
        return depereciationAccount;
    }

    public void setDepereciationAccount(Account depereciationAccount) {
        this.depereciationAccount = depereciationAccount;
    }

    public boolean isSetupDone() {
        return setupDone;
    }

    public void setSetupDone(boolean setupDone) {
        this.setupDone = setupDone;
    }

    public CompanyType getCompanyType() {
        return companyType;
    }

    public void setCompanyType(CompanyType companyType) {
        this.companyType = companyType;
    }

    public String getQuotationNumberFormat() {
        return quotationNumberFormat;
    }

    public void setQuotationNumberFormat(String quotationNumberFormat) {
        this.quotationNumberFormat = quotationNumberFormat;
    }

    public String getVenQuotationNumberFormat() {
        return venQuotationNumberFormat;
    }

    public void setVenQuotationNumberFormat(String venQuotationNumberFormat) {
        this.venQuotationNumberFormat = venQuotationNumberFormat;
    }

    public String getRequisitionNumberFormat() {
        return requisitionNumberFormat;
    }

    public void setRequisitionNumberFormat(String requisitionNumberFormat) {
        this.requisitionNumberFormat = requisitionNumberFormat;
    }

    public String getRfqNumberFormat() {
        return rfqNumberFormat;
    }

    public void setRfqNumberFormat(String rfqNumberFormat) {
        this.rfqNumberFormat = rfqNumberFormat;
    }

    public String getDeliveryOrderNumberFormat() {
        return deliveryOrderNumberFormat;
    }

    public void setDeliveryOrderNumberFormat(String deliveryOrderNumberFormat) {
        this.deliveryOrderNumberFormat = deliveryOrderNumberFormat;
    }

    public String getGoodsReceiptOrderNumberFormat() {
        return goodsReceiptOrderNumberFormat;
    }

    public void setGoodsReceiptOrderNumberFormat(String goodsReceiptOrderNumberFormat) {
        this.goodsReceiptOrderNumberFormat = goodsReceiptOrderNumberFormat;
    }

    public String getDescriptionType() {
        return descriptionType;
    }

    public void setDescriptionType(String descriptionType) {
        this.descriptionType = descriptionType;
    }

    public Account getRoundingDifferenceAccount() {
        return roundingDifferenceAccount;
    }

    public void setRoundingDifferenceAccount(Account roundingDifferenceAccount) {
        this.roundingDifferenceAccount = roundingDifferenceAccount;
    }

    public boolean isInventoryAccountingIntegration() {
        return inventoryAccountingIntegration;
    }

    public void setInventoryAccountingIntegration(boolean inventoryAccountingIntegration) {
        this.inventoryAccountingIntegration = inventoryAccountingIntegration;
    }

    public String getPdffooter() {
        return pdffooter;
    }

    public void setPdffooter(String pdffooter) {
        this.pdffooter = pdffooter;
    }

    public String getPdfheader() {
        return pdfheader;
    }

    public void setPdfheader(String pdfheader) {
        this.pdfheader = pdfheader;
    }

    public String getPdfposttext() {
        return pdfposttext;
    }

    public void setPdfposttext(String pdfposttext) {
        this.pdfposttext = pdfposttext;
    }

    public String getPdfpretext() {
        return pdfpretext;
    }

    public void setPdfpretext(String pdfpretext) {
        this.pdfpretext = pdfpretext;
    }

    public int getNegativestock() {
        return negativestock;
    }

    public void setNegativestock(int negativestock) {
        this.negativestock = negativestock;
    }

    public int getCustcreditcontrol() {
        return custcreditcontrol;
    }

    public void setCustcreditcontrol(int custcreditcontrol) {
        this.custcreditcontrol = custcreditcontrol;
    }

    public int getviewDashboard() {
        return viewDashboard;
    }

    public void setviewDashboard(int viewDashboard) {
        this.viewDashboard = viewDashboard;
    }

    public boolean isAccountsWithCode() {
        return accountsWithCode;
    }

    public void setAccountsWithCode(boolean accountsWithCode) {
        this.accountsWithCode = accountsWithCode;
    }

    public int getBillingCashPurchaseNumberFormatStartFrom() {
        return billingCashPurchaseNumberFormatStartFrom;
    }

    public void setBillingCashPurchaseNumberFormatStartFrom(int billingCashPurchaseNumberFormatStartFrom) {
        this.billingCashPurchaseNumberFormatStartFrom = billingCashPurchaseNumberFormatStartFrom;
    }

    public int getBillingCashSaleNumberFormatStartFrom() {
        return billingCashSaleNumberFormatStartFrom;
    }

    public void setBillingCashSaleNumberFormatStartFrom(int billingCashSaleNumberFormatStartFrom) {
        this.billingCashSaleNumberFormatStartFrom = billingCashSaleNumberFormatStartFrom;
    }

    public int getBillingCreditNoteNumberFormatStartFrom() {
        return billingCreditNoteNumberFormatStartFrom;
    }

    public void setBillingCreditNoteNumberFormatStartFrom(int billingCreditNoteNumberFormatStartFrom) {
        this.billingCreditNoteNumberFormatStartFrom = billingCreditNoteNumberFormatStartFrom;
    }

    public int getBillingDebitNoteNumberFormatStartFrom() {
        return billingDebitNoteNumberFormatStartFrom;
    }

    public void setBillingDebitNoteNumberFormatStartFrom(int billingDebitNoteNumberFormatStartFrom) {
        this.billingDebitNoteNumberFormatStartFrom = billingDebitNoteNumberFormatStartFrom;
    }

    public int getBillingGoodsReceiptNumberFormatStartFrom() {
        return billingGoodsReceiptNumberFormatStartFrom;
    }

    public void setBillingGoodsReceiptNumberFormatStartFrom(int billingGoodsReceiptNumberFormatStartFrom) {
        this.billingGoodsReceiptNumberFormatStartFrom = billingGoodsReceiptNumberFormatStartFrom;
    }

    public int getBillingInvoiceNumberFormatStartFrom() {
        return billingInvoiceNumberFormatStartFrom;
    }

    public void setBillingInvoiceNumberFormatStartFrom(int billingInvoiceNumberFormatStartFrom) {
        this.billingInvoiceNumberFormatStartFrom = billingInvoiceNumberFormatStartFrom;
    }

    public int getBillingPaymentNumberFormatStartFrom() {
        return billingPaymentNumberFormatStartFrom;
    }

    public void setBillingPaymentNumberFormatStartFrom(int billingPaymentNumberFormatStartFrom) {
        this.billingPaymentNumberFormatStartFrom = billingPaymentNumberFormatStartFrom;
    }

    public int getBillingPurchaseOrderNumberFormatStartFrom() {
        return billingPurchaseOrderNumberFormatStartFrom;
    }

    public void setBillingPurchaseOrderNumberFormatStartFrom(int billingPurchaseOrderNumberFormatStartFrom) {
        this.billingPurchaseOrderNumberFormatStartFrom = billingPurchaseOrderNumberFormatStartFrom;
    }

    public int getBillingReceiptNumberFormatStartFrom() {
        return billingReceiptNumberFormatStartFrom;
    }

    public void setBillingReceiptNumberFormatStartFrom(int billingReceiptNumberFormatStartFrom) {
        this.billingReceiptNumberFormatStartFrom = billingReceiptNumberFormatStartFrom;
    }

    public int getBillingSalesOrderNumberFormatStartFrom() {
        return billingSalesOrderNumberFormatStartFrom;
    }

    public void setBillingSalesOrderNumberFormatStartFrom(int billingSalesOrderNumberFormatStartFrom) {
        this.billingSalesOrderNumberFormatStartFrom = billingSalesOrderNumberFormatStartFrom;
    }

    public int getCashPurchaseNumberFormatStartFrom() {
        return cashPurchaseNumberFormatStartFrom;
    }

    public void setCashPurchaseNumberFormatStartFrom(int cashPurchaseNumberFormatStartFrom) {
        this.cashPurchaseNumberFormatStartFrom = cashPurchaseNumberFormatStartFrom;
    }

    public int getCashSaleNumberFormatStartFrom() {
        return cashSaleNumberFormatStartFrom;
    }

    public void setCashSaleNumberFormatStartFrom(int cashSaleNumberFormatStartFrom) {
        this.cashSaleNumberFormatStartFrom = cashSaleNumberFormatStartFrom;
    }

    public int getCreditNoteNumberFormatStartFrom() {
        return creditNoteNumberFormatStartFrom;
    }

    public void setCreditNoteNumberFormatStartFrom(int creditNoteNumberFormatStartFrom) {
        this.creditNoteNumberFormatStartFrom = creditNoteNumberFormatStartFrom;
    }

    public int getDebitNoteNumberFormatStartFrom() {
        return debitNoteNumberFormatStartFrom;
    }

    public void setDebitNoteNumberFormatStartFrom(int debitNoteNumberFormatStartFrom) {
        this.debitNoteNumberFormatStartFrom = debitNoteNumberFormatStartFrom;
    }

    public int getDeliveryOrderNumberFormatStartFrom() {
        return deliveryOrderNumberFormatStartFrom;
    }

    public void setDeliveryOrderNumberFormatStartFrom(int deliveryOrderNumberFormatStartFrom) {
        this.deliveryOrderNumberFormatStartFrom = deliveryOrderNumberFormatStartFrom;
    }

    public int getGoodsReceiptNumberFormatStartFrom() {
        return goodsReceiptNumberFormatStartFrom;
    }

    public void setGoodsReceiptNumberFormatStartFrom(int goodsReceiptNumberFormatStartFrom) {
        this.goodsReceiptNumberFormatStartFrom = goodsReceiptNumberFormatStartFrom;
    }

    public int getGoodsReceiptOrderNumberFormatStartFrom() {
        return goodsReceiptOrderNumberFormatStartFrom;
    }

    public void setGoodsReceiptOrderNumberFormatStartFrom(int goodsReceiptOrderNumberFormatStartFrom) {
        this.goodsReceiptOrderNumberFormatStartFrom = goodsReceiptOrderNumberFormatStartFrom;
    }

    public int getJournalEntryNumberFormatStartFrom() {
        return journalEntryNumberFormatStartFrom;
    }

    public void setJournalEntryNumberFormatStartFrom(int journalEntryNumberFormatStartFrom) {
        this.journalEntryNumberFormatStartFrom = journalEntryNumberFormatStartFrom;
    }

    public int getPaymentNumberFormatStartFrom() {
        return paymentNumberFormatStartFrom;
    }

    public void setPaymentNumberFormatStartFrom(int paymentNumberFormatStartFrom) {
        this.paymentNumberFormatStartFrom = paymentNumberFormatStartFrom;
    }

    public int getProductidNumberFormatStartFrom() {
        return productidNumberFormatStartFrom;
    }

    public void setProductidNumberFormatStartFrom(int productidNumberFormatStartFrom) {
        this.productidNumberFormatStartFrom = productidNumberFormatStartFrom;
    }

    public int getPurchaseOrderNumberFormatStartFrom() {
        return purchaseOrderNumberFormatStartFrom;
    }

    public void setPurchaseOrderNumberFormatStartFrom(int purchaseOrderNumberFormatStartFrom) {
        this.purchaseOrderNumberFormatStartFrom = purchaseOrderNumberFormatStartFrom;
    }

    public int getPurchaseReturnNumberFormatStartFrom() {
        return purchaseReturnNumberFormatStartFrom;
    }

    public void setPurchaseReturnNumberFormatStartFrom(int purchaseReturnNumberFormatStartFrom) {
        this.purchaseReturnNumberFormatStartFrom = purchaseReturnNumberFormatStartFrom;
    }

    public int getQuotationNumberFormatStartFrom() {
        return quotationNumberFormatStartFrom;
    }

    public void setQuotationNumberFormatStartFrom(int quotationNumberFormatStartFrom) {
        this.quotationNumberFormatStartFrom = quotationNumberFormatStartFrom;
    }

    public int getReceiptNumberFormatStartFrom() {
        return receiptNumberFormatStartFrom;
    }

    public void setReceiptNumberFormatStartFrom(int receiptNumberFormatStartFrom) {
        this.receiptNumberFormatStartFrom = receiptNumberFormatStartFrom;
    }

    public int getRequisitionNumberFormatStartFrom() {
        return requisitionNumberFormatStartFrom;
    }

    public void setRequisitionNumberFormatStartFrom(int requisitionNumberFormatStartFrom) {
        this.requisitionNumberFormatStartFrom = requisitionNumberFormatStartFrom;
    }

    public int getRfqNumberFormatStartFrom() {
        return rfqNumberFormatStartFrom;
    }

    public void setRfqNumberFormatStartFrom(int rfqNumberFormatStartFrom) {
        this.rfqNumberFormatStartFrom = rfqNumberFormatStartFrom;
    }

    public int getSalesReturnNumberFormatStartFrom() {
        return salesReturnNumberFormatStartFrom;
    }

    public void setSalesReturnNumberFormatStartFrom(int salesReturnNumberFormatStartFrom) {
        this.salesReturnNumberFormatStartFrom = salesReturnNumberFormatStartFrom;
    }

    public int getVenQuotationNumberFormatStartFrom() {
        return venQuotationNumberFormatStartFrom;
    }

    public void setVenQuotationNumberFormatStartFrom(int venQuotationNumberFormatStartFrom) {
        this.venQuotationNumberFormatStartFrom = venQuotationNumberFormatStartFrom;
    }

    public String getPurchaseReturnNumberFormat() {
        return purchaseReturnNumberFormat;
    }

    public void setPurchaseReturnNumberFormat(String purchaseReturnNumberFormat) {
        this.purchaseReturnNumberFormat = purchaseReturnNumberFormat;
    }

    public boolean isUpdateInvLevel() {
        return updateInvLevel;
    }

    public void setUpdateInvLevel(boolean updateInvLevel) {
        this.updateInvLevel = updateInvLevel;
    }

    public boolean isQaApprovalFlow() {
        return qaApprovalFlow;
    }

    public void setQaApprovalFlow(boolean qaApprovalFlow) {
        this.qaApprovalFlow = qaApprovalFlow;
    }

    public boolean isDoClosedStatus() {
        return doClosedStatus;
    }

    public void setDoClosedStatus(boolean doClosedStatus) {
        this.doClosedStatus = doClosedStatus;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public boolean isIshtmlproddesc() {
        return ishtmlproddesc;
    }

    public void setIshtmlproddesc(boolean ishtmlproddesc) {
        this.ishtmlproddesc = ishtmlproddesc;
    }

    public Account getCustomerdefaultaccount() {
        return customerdefaultaccount;
    }

    public void setCustomerdefaultaccount(Account customerdefaultaccount) {
        this.customerdefaultaccount = customerdefaultaccount;
    }

    public Account getVendordefaultaccount() {
        return vendordefaultaccount;
    }

    public void setVendordefaultaccount(Account vendordefaultaccount) {
        this.vendordefaultaccount = vendordefaultaccount;
    }

    public boolean isIsBatchCompulsory() {
        return isBatchCompulsory;
    }

    public void setIsBatchCompulsory(boolean isBatchCompulsory) {
        this.isBatchCompulsory = isBatchCompulsory;
    }

    public Account getUnrealisedgainloss() {
        return unrealisedgainloss;
    }

    public void setUnrealisedgainloss(Account unrealisedgainloss) {
        this.unrealisedgainloss = unrealisedgainloss;
    }

    public boolean isDependentField() {
        return dependentField;
    }

    public void setDependentField(boolean dependentField) {
        this.dependentField = dependentField;
    }

    public int getAmountdigitafterdecimal() {
        return amountdigitafterdecimal;
    }

    public void setAmountdigitafterdecimal(int amountdigitafterdecimal) {
        this.amountdigitafterdecimal = amountdigitafterdecimal;
    }

    public int getQuantitydigitafterdecimal() {
        return quantitydigitafterdecimal;
    }

    public void setQuantitydigitafterdecimal(int quantitydigitafterdecimal) {
        this.quantitydigitafterdecimal = quantitydigitafterdecimal;
    }

    public int getViewDashboard() {
        return viewDashboard;
    }

    public void setViewDashboard(int viewDashboard) {
        this.viewDashboard = viewDashboard;
    }

    public boolean isIslocationcompulsory() {
        return islocationcompulsory;
    }

    public void setIslocationcompulsory(boolean islocationcompulsory) {
        this.islocationcompulsory = islocationcompulsory;
    }

    public boolean isIswarehousecompulsory() {
        return iswarehousecompulsory;
    }

    public void setIswarehousecompulsory(boolean iswarehousecompulsory) {
        this.iswarehousecompulsory = iswarehousecompulsory;
    }

    public boolean isViewDetailsPerm() {
        return viewDetailsPerm;
    }

    public void setViewDetailsPerm(boolean viewDetailsPerm) {
        this.viewDetailsPerm = viewDetailsPerm;
    }

    public boolean isIsbincompulsory() {
        return isbincompulsory;
    }

    public void setIsbincompulsory(boolean isbincompulsory) {
        this.isbincompulsory = isbincompulsory;
    }

    public boolean isIsrackcompulsory() {
        return israckcompulsory;
    }

    public void setIsrackcompulsory(boolean israckcompulsory) {
        this.israckcompulsory = israckcompulsory;
    }

    public boolean isIsrowcompulsory() {
        return isrowcompulsory;
    }

    public void setIsrowcompulsory(boolean isrowcompulsory) {
        this.isrowcompulsory = isrowcompulsory;
    }

    public boolean isFilterProductByCustomerCategory() {
        return filterProductByCustomerCategory;
    }

    public void setFilterProductByCustomerCategory(boolean filterProductByCustomerCategory) {
        this.filterProductByCustomerCategory = filterProductByCustomerCategory;
    }

    public int getChequeNoDuplicate() {
        return chequeNoDuplicate;
    }

    public void setChequeNoDuplicate(int chequeNoDuplicate) {
        this.chequeNoDuplicate = chequeNoDuplicate;
    }

    public int getNegativeStockSO() {
        return negativeStockSO;
    }

    public void setNegativeStockSO(int negativeStockSO) {
        this.negativeStockSO = negativeStockSO;
    }

    public int getNegativeStockSICS() {
        return negativeStockSICS;
    }

    public void setNegativeStockSICS(int negativeStockSICS) {
        this.negativeStockSICS = negativeStockSICS;
    }

    public int getNegativeStockPR() {
        return negativeStockPR;
    }

    public void setNegativeStockPR(int negativeStockPR) {
        this.negativeStockPR = negativeStockPR;
    }

    public int getProductSortingFlag() {
        return productSortingFlag;
    }

    public void setProductSortingFlag(int productSortingFlag) {
        this.productSortingFlag = productSortingFlag;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public int getInventoryValuationType() {
        return inventoryValuationType;
    }

    public void setInventoryValuationType(int inventoryValuationType) {
        this.inventoryValuationType = inventoryValuationType;
    }
    
    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * @return the industryCode
     */
    public MasterItem getIndustryCode() {
        return industryCode;
    }

    /**
     * @param industryCode the industryCode to set
     */
    public void setIndustryCode(MasterItem industryCode) {
        this.industryCode = industryCode;
    }
    public boolean isUpdateStockAdjustmentEntries() {
        return updateStockAdjustmentEntries;
    }

    public void setUpdateStockAdjustmentEntries(boolean updateStockAdjustmentEntries) {
        this.updateStockAdjustmentEntries = updateStockAdjustmentEntries;
    }   

    public boolean isQaApprovalFlowInDO() {
        return qaApprovalFlowInDO;
    }

    public void setQaApprovalFlowInDO(boolean qaApprovalFlowInDO) {
        this.qaApprovalFlowInDO = qaApprovalFlowInDO;
    }
    
    public boolean isShowMarginButton() {
        return showMarginButton;
    }

    public void setShowMarginButton(boolean showMarginButton) {
        this.showMarginButton = showMarginButton;
    }

    public Date getGSTApplicableDate() {
        return GSTApplicableDate;
}

    public void setGSTApplicableDate(Date GSTApplicableDate) {
        this.GSTApplicableDate = GSTApplicableDate;
    }
}
