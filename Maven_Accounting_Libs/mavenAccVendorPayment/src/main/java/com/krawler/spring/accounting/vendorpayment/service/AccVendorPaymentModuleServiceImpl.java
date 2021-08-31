/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.vendorpayment.service;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.*;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.inventory.model.stockmovement.StockMovement;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.goodsreceipt.service.accGoodsReceiptModuleService;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.JournalEntryConstants;

import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.payment.accPaymentService;
import com.krawler.spring.accounting.receipt.accReceiptController;
import com.krawler.spring.accounting.salesorder.accSalesOrderService;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.accounting.vendorpayment.RepeatedPayment;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentController;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentControllerNew;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.CommonFnControllerService;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AccVendorPaymentModuleServiceImpl implements AccVendorPaymentModuleService, MessageSourceAware {

    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accVendorPaymentDAO accVendorPaymentobj;
    private MessageSource messageSource;
    private HibernateTransactionManager txnManager;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accPaymentDAO accPaymentDAOobj;
    private accPaymentService paymentService;
    private accJournalEntryDAO accJournalEntryobj;
    private fieldDataManager fieldDataManagercntrl;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private EnglishNumberToWords EnglishNumberToWordsOjb = new EnglishNumberToWords();
    private auditTrailDAO auditTrailObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accBankReconciliationDAO accBankReconciliationObj;
    private accAccountDAO accAccountDAOobj;
    
    private ImportHandler importHandler;
    private accSalesOrderService acSalesOrderServiceObj;
    private accCustomerDAO accCustomerDAOObj;
    private accVendorDAO accVendorDAOObj;
    private accCreditNoteDAO accCreditNoteDAOObj;
    private ImportDAO importDao;    
    private accMasterItemsDAO accMasterItemsDAOObj;
    private accGoodsReceiptModuleService accGoodsReceiptModuleService;    
    private fieldManagerDAO fieldManagerDAOobj;

    private CommonFnControllerService commonFnControllerService;
    private authHandlerDAO authHandlerDAOObj;
    private AccJournalEntryModuleService journalEntryModuleServiceobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    private accInvoiceDAO accInvoiceDAOobj;
    private accDebitNoteDAO accDebitNoteDAO;

    public void setAccDebitNoteDAO(accDebitNoteDAO accDebitNoteDAO) {
        this.accDebitNoteDAO = accDebitNoteDAO;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    
    public void setAccMasterItemsDAOObj(accMasterItemsDAO accMasterItemsDAOObj) {
        this.accMasterItemsDAOObj = accMasterItemsDAOObj;
    }
   
    public void setImportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setAcSalesOrderServiceObj(accSalesOrderService acSalesOrderServiceObj) {
        this.acSalesOrderServiceObj = acSalesOrderServiceObj;
    }

    public void setAccVendorDAOObj(accVendorDAO accVendorDAOObj) {
        this.accVendorDAOObj = accVendorDAOObj;
    }

    public void setAccCustomerDAOObj(accCustomerDAO accCustomerDAOObj) {
        this.accCustomerDAOObj = accCustomerDAOObj;
    }

    public void setAccCreditNoteDAOObj(accCreditNoteDAO accCreditNoteDAOObj) {
        this.accCreditNoteDAOObj = accCreditNoteDAOObj;
    }

    public void setImportDao(ImportDAO importDao) {
        this.importDao = importDao;
    }

    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }

    public void setaccPaymentService(accPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }

    public void setAccAccountDAOobj(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    
    public void setAccGoodsReceiptModuleService(accGoodsReceiptModuleService accGoodsReceiptModuleService) {
        this.accGoodsReceiptModuleService = accGoodsReceiptModuleService;
    }
    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
    
    public void setCommonFnControllerService(CommonFnControllerService commonFnControllerService) {
        this.commonFnControllerService = commonFnControllerService;
    }
    
    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }
    
    public void setJournalEntryModuleServiceobj(AccJournalEntryModuleService journalEntryModuleServiceobj) {
        this.journalEntryModuleServiceobj = journalEntryModuleServiceobj;
    }
    
      public void setKwlCommonTablesDAOObj(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }
    
     
    public HashMap<String, Object> saveVendorPayment(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        KwlReturnObject result = null;
        String Cardid = null;
        StringBuilder bno = new StringBuilder();
        String amountpayment = "";
        String netinword = "";
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        String billno = "";
        String person = "";
        boolean isEdit = false;
        boolean issuccess = false;
        String msg = "";
        int editCount = 0;
        boolean accexception = false;
        String paymentid = "";
        String repeatedid = "";
        String intervalType = null;
        int intervalUnit = 0;
        int noOfInvPost = 0;
        int noOfInvRemainPost = 0;
        Date sdate = null, nextdate = null, expdate = null;
        String AuditMsg = "", vendorEmailId = "",billingAddress="";
        String bankChargesAccid = "", bankInterestAccid = "";
        String bankChargeJeId = "", bankInterestJeId = "";
        Map<String, Object> oldPaymentPrmt = new HashMap<String, Object>();
        Map<String, Object> NewPaymentPrmt = new HashMap<String, Object>();
        Map<String, Object> newAuditKey = new HashMap<String, Object>(); 
        Map<String, Object> seqNumMap = new HashMap<String, Object>();
        String accountName = "";
        String accountaddress = "";
        JSONObject jobjDetails = new JSONObject();
        JSONArray jSONArrayAgainstInvoice = new JSONArray();
        JSONArray jArr = new JSONArray();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        int flag = 0;
        String jeEntryNo = "";
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        String companyid = "";
        String chequeNumber = "";
        String entryNumber = "";
        boolean exceptionChequeOrInv = false;
       
        try {
            entryNumber = paramJobj.optString("no");
            String sequenceformat = paramJobj.optString("sequenceformat") != null ? paramJobj.optString("sequenceformat") : "NA";
            companyid = paramJobj.optString(Constants.companyKey);

            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }

            String receiptid = paramJobj.optString("billid");
            double balaceAmount = 0.0;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("balaceAmount"))) {
                balaceAmount = Double.parseDouble(paramJobj.optString("balaceAmount"));
            }
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyResult.getEntityList().get(0);
            String countryID = company != null && company.getCountry() != null ? company.getCountry().getID() : "";
            Payment editPaymentObject = null;
            if (!StringUtil.isNullOrEmpty(receiptid)) {// for edit case
                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Payment.class.getName(), receiptid);
                editPaymentObject = (Payment) receiptObj.getEntityList().get(0);
                isEdit = editPaymentObject != null ? true : false;
            }
            
            boolean isEditedPendingDocument = StringUtil.isNullOrEmpty(paramJobj.optString("isEditedPendingDocument",null)) ? false : Boolean.parseBoolean(paramJobj.optString("isEditedPendingDocument"));
            boolean remainAtSameLevel = StringUtil.isNullOrEmpty(paramJobj.optString("remainAtSameLevel",null)) ? false : Boolean.parseBoolean(paramJobj.optString("remainAtSameLevel"));
            boolean isDiscountAppliedOnPaymentTerms = false;
            boolean isPostingDateCheck = false;
            JSONObject jObj = new JSONObject();
            if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                if (jObj.has(Constants.DISCOUNT_ON_PAYMENT_TERMS) && jObj.get(Constants.DISCOUNT_ON_PAYMENT_TERMS) != null && jObj.optBoolean(Constants.DISCOUNT_ON_PAYMENT_TERMS, false)) {
                    isDiscountAppliedOnPaymentTerms = true;
                }
                if (!StringUtil.isNullObject(jObj) && jObj.has(Constants.IS_POSTING_DATE_CHECK) && jObj.get(Constants.IS_POSTING_DATE_CHECK) != null && jObj.optBoolean(Constants.IS_POSTING_DATE_CHECK, false)) {
                    isPostingDateCheck = true;
                }
            }
            /*
             * Get line detail json array for GL, Invoice, Advance, CN/DN
             */
            String detailsJsonString = paramJobj.optString("Details");
            JSONArray jSONArray = new JSONArray(detailsJsonString);
            JSONArray jSONArrayAdvance = new JSONArray();
            JSONArray jSONArrayCNDN = new JSONArray();
            JSONArray jSONArrayGL = new JSONArray();
            JSONArray jSONArrayBal = new JSONArray();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                if (jSONObject.has("type") && jSONObject.optInt("type", 0) != 0 && jSONObject.has("enteramount") && jSONObject.optDouble("enteramount", 0.0) != 0) {
                    if (jSONObject.getInt("type") == Constants.AdvancePayment) {
                        jSONArrayAdvance.put(jSONObject);
                    } else if (jSONObject.getInt("type") == Constants.PaymentAgainstInvoice) {
                        jSONArrayAgainstInvoice.put(jSONObject);
                    } else if (jSONObject.getInt("type") == Constants.PaymentAgainstCNDN) {
                        jSONArrayCNDN.put(jSONObject);
                    } else if (jSONObject.getInt("type") == Constants.GLPayment) {
                        jSONArrayGL.put(jSONObject);
                    }
                }
                if (isDiscountAppliedOnPaymentTerms && jSONObject.getInt("type") == Constants.PaymentAgainstInvoice && jSONObject.optDouble("discountname", 0.0) > 0.0 && jSONObject.optDouble("enteramount", 0.0) == 0.0) {        //if 100% percent discount is applied even then the payment should be created the payment will be only created if discount on payment terms check is enabled in company preferences
                    jSONArrayAgainstInvoice.put(jSONObject);
                }
            }

            /*
             * Check Invoice or Cheque number already in process in other thread
             * otherwise save current processing invoice/cheque in temporary
             * table
             */
            try {
                String invoiceId = "";

                if (editPaymentObject != null) {//In edit case checks duplicate number
                    result = accVendorPaymentobj.getPaymentEditCount(entryNumber, companyid, receiptid);
                    editCount = result.getRecordTotalCount();
                    if (editCount > 0 && sequenceformat.equals("NA")) {
                        accexception = true;
                        throw new AccountingException(messageSource.getMessage("acc.payment.paymentno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                } else {//In add case check duplicate number
                    result = accVendorPaymentobj.getPaymentFromNo(entryNumber, companyid);
                    if (result.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                        accexception = true;
                        throw new AccountingException(messageSource.getMessage("acc.payment.paymentno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                    
                    //Check Deactivate Tax in New Transaction.
                    JSONObject taxJObj = new JSONObject();
                    taxJObj.put(Constants.detail, jSONArrayGL);
                    taxJObj.put(Constants.companyKey, companyid);
                    if (!fieldDataManagercntrl.isTaxActivated(taxJObj)) {
                        throw ServiceException.FAILURE(messageSource.getMessage("acc.tax.deactivated.tax.saveAlert", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                    }
                }
                if (company.getCountry().getID().equals(String.valueOf(Constants.indian_country_id))) {
                    boolean seqformat_oldflg = StringUtil.getBoolean(paramJobj.optString("seqformat_oldflag"));
                    SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getDateOnlyFormat();
                    Date billDate = formatter.parse(paramJobj.optString("creationdate"));
                    if ((!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA"))) {
                        seqNumMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_DEBITNOTE, sequenceformat, seqformat_oldflg, billDate);
                        String autoNum = (String) seqNumMap.get(Constants.AUTO_ENTRYNUMBER);
                        if (autoNum.length() > Constants.SequenceFormatMaxLength || !StringUtil.isSequenceFormatValid(autoNum)) {
                            throw new AccountingException(Constants.SequenceformatErrorMsg1 + autoNum + Constants.SequenceformatErrorMsg2 + Constants.SequenceFormatMaxLength + Constants.SequenceformatErrorMsg3);
                        }
                    } else if (!StringUtil.isNullOrEmpty(sequenceformat) && sequenceformat.equals("NA") || !StringUtil.isSequenceFormatValid(entryNumber)) {
                        if ((!StringUtil.isNullOrEmpty(sequenceformat) && entryNumber.length() > Constants.SequenceFormatMaxLength) || !StringUtil.isSequenceFormatValid(entryNumber)) {
                            throw new AccountingException(Constants.SequenceformatErrorMsg1 + entryNumber + Constants.SequenceformatErrorMsg2 + Constants.SequenceFormatMaxLength + Constants.SequenceformatErrorMsg3);
                        }

                    }
                }
                synchronized (this) {
                    status = txnManager.getTransaction(def);
                    KwlReturnObject resultPay = accCommonTablesDAO.getTransactionInTemp(entryNumber, companyid, Constants.Acc_Make_Payment_ModuleId);
                    if (resultPay.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                        accexception = true;
                        throw new AccountingException(messageSource.getMessage("acc.PO.selectedPamentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    } else {
                        accCommonTablesDAO.insertTransactionInTemp(entryNumber, companyid, Constants.Acc_Make_Payment_ModuleId);
                    }
                    for (int i = 0; i < jSONArrayAgainstInvoice.length(); i++) {
                        JSONObject invoiceJobj = jSONArrayAgainstInvoice.getJSONObject(i);
                        invoiceId = invoiceJobj.getString("documentid");
                        KwlReturnObject resultInv1 = accPaymentDAOobj.getInvoiceInTemp(invoiceId, companyid, Constants.Acc_Invoice_ModuleId);
                        if (resultInv1.getRecordTotalCount() > 0) {
                            throw new AccountingException("Selected invoice is already in process, please try after sometime.");
                        } else {
                            accPaymentDAOobj.insertInvoiceOrCheque(invoiceId, companyid, Constants.Acc_Invoice_ModuleId, "");
                        }
                    }
                    /*
                     * Check for invalid cheque number
                     */

                    KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                    CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

                    if (preferences.getChequeNoDuplicate() != Constants.ChequeNoIgnore) {// Ignore case 
                        checkForInvalidChequeNumber(paramJobj, editPaymentObject);                        
                    }
                    paramJobj.put("chequecduplicatepref", preferences.getChequeNoDuplicate());  // ERP-39302
                    if (!StringUtil.isNullOrEmpty(paramJobj.optString("paydetail"))) {
                        JSONObject obj = new JSONObject(paramJobj.optString("paydetail"));
                        chequeNumber = obj.optString("chequenumber");
                        String methodid = paramJobj.optString("pmtmethod");
                        KwlReturnObject result1 = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), methodid);
                        PaymentMethod payMethod = (PaymentMethod) result1.getEntityList().get(0);
                        if (StringUtil.isNullObject(payMethod)) {
                            throw new AccountingException(messageSource.getMessage("acc.field.vendorpayment.paymentmethod.doesnt.exists", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                        }
                        String bankId = payMethod.getAccount().getID();

                        KwlReturnObject resultInv = accPaymentDAOobj.getSearchChequeNoTemp(chequeNumber, companyid, Constants.Cheque_ModuleId, bankId);
                        if (resultInv.getRecordTotalCount() > 0) {
                            throw new AccountingException("Cheque Number : <b>" + chequeNumber + "</b> is already exist, Please enter different one");
                        } else {
                            accPaymentDAOobj.insertInvoiceOrCheque(chequeNumber, companyid, Constants.Cheque_ModuleId, bankId);
                        }
                    }
//                    accPaymentDAOobj.updateChequeNoInTemp(chequeNumber, companyid,invoiceId);
                    txnManager.commit(status);
                }
            } catch (Exception ex) {
                exceptionChequeOrInv = true;
                if (status != null) {
                    txnManager.rollback(status);
                }
                throw new AccountingException(ex.getMessage(), ex);
            }
            
            status = txnManager.getTransaction(def);
            if (!isEdit) {
                for (int i = 0; i < jSONArrayAgainstInvoice.length(); i++) {
                    JSONObject invoiceJobj = jSONArrayAgainstInvoice.getJSONObject(i);
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceJobj.getString("documentid"));
                    GoodsReceipt invoice = (GoodsReceipt) objItr.getEntityList().get(0);
                    if (invoice != null) {
                        double invoiceAmoutDue = 0;
                        boolean isInvoiceIsClaimed = (invoice.getBadDebtType() == Constants.Invoice_Claimed || invoice.getBadDebtType() == Constants.Invoice_Recovered);
                        double invoiceAmountDueAfterDiscount = 0;
                        if (isInvoiceIsClaimed) {
                            invoiceAmoutDue = invoice.getClaimAmountDue();
                        } else {
                            if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                                invoiceAmoutDue = invoice.getOpeningBalanceAmountDue();
                            } else {
                                invoiceAmoutDue = invoice.getInvoiceamountdue();
                                invoiceAmountDueAfterDiscount=invoiceJobj.optDouble("amountdueafterdiscount", 0.0);
                            }
                        }
                        double amountReceived = invoiceJobj.getDouble("enteramount");           //amount of Invoice
                        double adjustedRate = 1;
                        if (!StringUtil.isNullOrEmpty(invoiceJobj.optString("exchangeratefortransaction", "").toString())) {
                            adjustedRate = Double.parseDouble(invoiceJobj.get("exchangeratefortransaction").toString());
                        }
                        double paymentAmountReceivedTemp = invoiceAmoutDue * adjustedRate;
                        paymentAmountReceivedTemp = authHandler.round(paymentAmountReceivedTemp, companyid);
                        boolean isFullPayment = (paymentAmountReceivedTemp == amountReceived);
                        double discountAmount = invoiceJobj.optDouble("discountname", 0.0);
                        double discountAmountConverted = discountAmount;
                        discountAmount = authHandler.round(discountAmount, companyid);
                        if (isDiscountAppliedOnPaymentTerms && discountAmount > 0.0) {
                            isFullPayment = paymentAmountReceivedTemp == (amountReceived + discountAmount);
                        }
                        if (!isFullPayment) {                                               // Changes made for ERP-16382
                            double amountReceivedConverted = invoiceJobj.getDouble("enteramount");
                            double totalAmtReceivedAndDiscount = 0d;
                            if (!StringUtil.isNullOrEmpty(invoiceJobj.optString("exchangeratefortransaction", "").toString())) {
                                adjustedRate = Double.parseDouble(invoiceJobj.get("exchangeratefortransaction").toString());
                                amountReceivedConverted = amountReceived / adjustedRate;
                                discountAmountConverted = discountAmount / adjustedRate;
                                totalAmtReceivedAndDiscount = authHandler.round((amountReceivedConverted+discountAmountConverted),companyid);
                                amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                            } else {
                                amountReceivedConverted = authHandler.round(amountReceived, companyid);
                                totalAmtReceivedAndDiscount = authHandler.round((amountReceived+discountAmount),companyid);
                            }
                            amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                            invoiceAmoutDue = authHandler.round(invoiceAmoutDue, companyid);
                            if (invoiceAmoutDue < totalAmtReceivedAndDiscount) {
                                flag = 1;
                                TransactionStatus tempStatus = txnManager.getTransaction(def);
                                throw new AccountingException("Amount entered for invoice cannot be greater than it's amount due. Please reload the invoices.");
                            }
                        }
                    }
                }
            }
            
            if (isEdit) {
                setValuesForAuditTrialMessage(editPaymentObject, oldPaymentPrmt, newAuditKey);
            }

            JournalEntry oldBankChargeJE = editPaymentObject != null ? editPaymentObject.getJournalEntryForBankCharges() != null ? editPaymentObject.getJournalEntryForBankCharges() : null : null;
            JournalEntry oldBankInterestJE = editPaymentObject != null ? editPaymentObject.getJournalEntryForBankInterest() != null ? editPaymentObject.getJournalEntryForBankInterest() : null : null;
            JournalEntry oldImportServiceInvoicePaymentJE = editPaymentObject != null ? editPaymentObject.getImportServiceJE() : null;
            if (editPaymentObject != null) {
                editPaymentObject.setBankChargesAccount(null);
                editPaymentObject.setBankChargesAmount(0);
                editPaymentObject.setBankInterestAccount(null);
                editPaymentObject.setBankInterestAmount(0);
            }

            int beforeEditApprovalStatus=editPaymentObject!=null?editPaymentObject.getApprovestatuslevel():0;       //getting current approval status before updating
            /*
             * Create Payment Object
             */

            Payment payment = createPaymentObject(paramJobj, editPaymentObject);

            List mailParams = Collections.EMPTY_LIST;
            int approvalStatusLevel = 11;
            Double totalAmount = Double.parseDouble(paramJobj.optString("amount"));
            HashMap<String, Object> paymentApproveMap = new HashMap<String, Object>();
            List approvedlevel = null;
            paymentApproveMap.put("companyid", companyid);
            paymentApproveMap.put("level", 0);//Initialy it will be 0
            paymentApproveMap.put("totalAmount", String.valueOf(authHandler.round(totalAmount, companyid)));
            paymentApproveMap.put("currentUser", payment.getCreatedby());
            paymentApproveMap.put("fromCreate", true);
            paymentApproveMap.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
            boolean isMailApplicable = false;
            List list = new ArrayList();
            list.add(payment.getID());
            
          
            String postingDateStr = paramJobj.optString("postingDate" ,null);
            JSONObject columnprefObj = new JSONObject();
            boolean sendPendingDocumentsToNextLevel = false;

            List approvalJEList = null;
            String JENumber = "";
            String JEMsg = "";
            boolean isAuthorityToApprove = true;
            int level = 0;

            if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                columnprefObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
            }
            if (columnprefObj.has("sendPendingDocumentsToNextLevel") && columnprefObj.get("sendPendingDocumentsToNextLevel") != null && (Boolean) columnprefObj.get("sendPendingDocumentsToNextLevel") != false) {
                sendPendingDocumentsToNextLevel = true;
            }
            
             String userfullName = paramJobj.optString(Constants.username, null);   

            /*------If pending document is Edited & Check is activated from system preferences
             *---Then Edit will work same as while approving document
             */
              int levelOnWhichDocumentIsApproved = payment.getApprovestatuslevel();  
             
              /*If any Invoice is linked with more than one Payment & any of the payment is approved with partial amount of invoice 
             Then if Editing another pending payment then "remainAtSameLevel ->true 
             */
            if(!remainAtSameLevel){
              String userid = paramJobj.optString(Constants.userid, ""); 
            if (isEditedPendingDocument) {
             
                level = payment.getApprovestatuslevel();
                paymentApproveMap.put("fromCreate", false);
                paymentApproveMap.put("documentLevel", level);
                paymentApproveMap.put("currentUser", userid);
                if (sendPendingDocumentsToNextLevel) {

                    isMailApplicable = true;
                    paymentApproveMap.put("level", level);
                   
                } else {
                    paymentApproveMap.put("isEditedPendingDocumentWithCheckOff", true);
                }
            }
            
             
            
            approvedlevel = paymentService.approveMakePayment(list, paymentApproveMap, isMailApplicable);
            approvalStatusLevel = (Integer) approvedlevel.get(0);
            mailParams = (List) approvedlevel.get(1);
            payment.setApprovestatuslevel(approvalStatusLevel);
//            paymenthm.put("mailParams", mailParams);

              /*-----Start block for Edit pending Payment ------*/          
            if (isEditedPendingDocument) {

                if (sendPendingDocumentsToNextLevel) {
                    
                     HashMap<String, Object> globalParams = AccountingManager.getGlobalParamsJson(paramJobj);

               

                        HashMap approveJeMap = new HashMap();

                        approveJeMap.put("company", company);
                        approveJeMap.put("payment", payment);
                        approveJeMap.put("extraCompanyPreferences", extraCompanyPreferences);
                        approveJeMap.put("postingDateStr", postingDateStr);
                        approveJeMap.put("isEditedPendingDocument", isEditedPendingDocument);
                        approveJeMap.put("requestParams", globalParams);
                         approveJeMap.put("totalAmount", totalAmount);

                        approvalJEList = approveRelevantDocumentAttachedToMakePayment(approveJeMap);

                        JENumber = approvalJEList != null ? approvalJEList.get(0).toString() : "";

                        JEMsg = approvalJEList != null ? approvalJEList.get(1).toString() : "";
                                                    
                    
                    
               
                    KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                    CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

                    /*----Block will execute if "Allow Sending Approval Mail" check is activated from system preferences ------*/
                    if (approvalStatusLevel != Constants.NoAuthorityToApprove && preferences.isSendapprovalmail()) {

                        HashMap emailMap = new HashMap();
                        
                        emailMap.put("userName", userfullName);
                        emailMap.put("company", company);
                        emailMap.put("payment", payment);
                        emailMap.put("baseUrl", paramJobj.optString("baseUrl", ""));
                        emailMap.put("preferences", preferences);
                        emailMap.put("level", levelOnWhichDocumentIsApproved);
                        emailMap.put("totalAmount", String.valueOf(totalAmount));

                        sendApprovalMailIfAllowedFromSystemPreferences(emailMap);

                    }

                    /*--------Save Approval history Code--------  */
                    if (approvalStatusLevel != Constants.NoAuthorityToApprove) {

                        HashMap approvalHistoryMap = new HashMap();
                   

                        approvalHistoryMap.put("userid", userid);

                        approvalHistoryMap.put("company", company);
                        approvalHistoryMap.put("payment", payment);

                        saveApprovalHistory(approvalHistoryMap);
                        

                    } else if (approvalStatusLevel == Constants.NoAuthorityToApprove) {
                        /*----If User have no authority to approve------*/
                        isAuthorityToApprove = false;
                        
                    }

                } else if (approvalStatusLevel == Constants.NoAuthorityToApprove) {//If User is not authorised to approve
                    isAuthorityToApprove = false;
                   
                }

            }
            
        }
                                    
           /* -----------End Block of Edit pending Payment---------------- */
            
            if (payment != null && isEdit) {
                NewPaymentPrmt.put(Constants.PaymentNumber, payment.getPaymentNumber());
                NewPaymentPrmt.put(Constants.PaidTo, payment.getPaidTo() != null ? payment.getPaidTo().getValue() : "");
//                NewPaymentPrmt.put(Constants.CreationDate, payment.isIsOpeningBalencePayment() ? payment.getCreationDate() : payment.getJournalEntry().getEntryDate());
                NewPaymentPrmt.put(Constants.CreationDate, payment.getCreationDate());
                NewPaymentPrmt.put(Constants.Memo, payment.getMemo());
                if (payment.getPayDetail() != null && payment.getPayDetail() != null) {
                    int oldPaymentMethodType = payment.getPayDetail().getPaymentMethod().getDetailType();
                    String oldPaymentMethodTypeName = payment.getPayDetail().getPaymentMethod().getMethodName();
                    NewPaymentPrmt.put(Constants.PaymentMethodType, oldPaymentMethodTypeName);
                    if (oldPaymentMethodType == PaymentMethod.TYPE_BANK) {
                        Cheque oldCheck = payment.getPayDetail().getCheque();
                        NewPaymentPrmt.put(Constants.Cheque, oldCheck);
                        NewPaymentPrmt.put(Constants.ChequeNumber, oldCheck.getChequeNo());
                        NewPaymentPrmt.put(Constants.BankName, oldCheck.getBankName());
                        NewPaymentPrmt.put(Constants.CheckDate, oldCheck.getDueDate());
                    } else if (oldPaymentMethodType == PaymentMethod.TYPE_CARD) {
                        NewPaymentPrmt.put(Constants.Card, payment.getPayDetail().getCard());
                    }
                }
                AuditMsg = AccountingManager.BuildAuditTrialMessage(NewPaymentPrmt, oldPaymentPrmt, Constants.Acc_Make_Payment_ModuleId, newAuditKey);
            }
            
            /*
             * Create Journal Entry Object
             */
            JournalEntry journalEntry = journalEntryObject(paramJobj, editPaymentObject);
            JournalEntry oldJE = editPaymentObject != null ? editPaymentObject.getJournalEntry() : null;            
            String oldReevalJE = editPaymentObject != null ? editPaymentObject.getRevalJeId() : null;
            PayDetail oldPayDetail = editPaymentObject != null ? editPaymentObject.getPayDetail() : null;
            if (oldJE != null) {
                paramJobj.put("oldjeid", oldJE.getID());
            }
            if (approvalStatusLevel == 11) {
                journalEntry.setPendingapproval(0);
            } else {
                journalEntry.setPendingapproval(1);
            }

            payment.setJournalEntry(journalEntry);
            List<Payment> payments = new ArrayList<Payment>();
            payments.add(payment);

            /*
             * Save Journal Entry Object
             */
            accJournalEntryobj.saveJournalEntryByObject(journalEntry);
            boolean isFromEclaim = paramJobj.has(Constants.isFromEclaim) ? paramJobj.getBoolean(Constants.isFromEclaim) : false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("customfield"))) {
                JSONArray jcustomarray = new JSONArray(paramJobj.optString("customfield"));
                 if (jcustomarray.length() > 0 && isFromEclaim) {
                    jcustomarray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_Make_Payment_ModuleId, companyid, true);
                }
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    KwlReturnObject receiptAccJECustomData = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), journalEntry.getID());
                    AccJECustomData accJECustomData = (AccJECustomData) receiptAccJECustomData.getEntityList().get(0);
                    journalEntry.setAccBillInvCustomData(accJECustomData);
                }
            }
            
            /*
             * Adding custom field value on Global level coming from Eclaim
             */
            String customfield = paramJobj.optString("customfieldmap", null);
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                if (jcustomarray.length() > 0) {
                    if (jcustomarray.length() > 0) {
                        jcustomarray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_Make_Payment_ModuleId, companyid, true);
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                        customrequestParams.put("modulerecid",  journalEntry.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                        customrequestParams.put("companyid", companyid);
                        customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            Map<String, Object> customjeDataMap = new HashMap<String, Object>();
                            customjeDataMap.put("accjecustomdataref", journalEntry.getID());
                            customjeDataMap.put("jeid", journalEntry.getID());
                            customjeDataMap.put("istemplate", 1);
                            accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                        }
                    }
                }
            }
            
            Set<JournalEntryDetail> details = null;

            //Save Payment Object
            accVendorPaymentobj.savePaymentObject(payments);
            String paymentID = payments.get(0).getID();
            /**
             * Save GST History Customer/Vendor data.
             */
            if (payment.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                paramJobj.put("docid", paymentID);
                paramJobj.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                fieldDataManagercntrl.createRequestMapToSaveDocHistory(paramJobj);
            }
            //Get Recurring Details
            RepeatedPayment rpay = (RepeatedPayment) payment.getRepeatedPayment();
            if (rpay != null && isEdit) {
                repeatedid = rpay.getId();
                intervalUnit = rpay.getIntervalUnit();
                intervalType = rpay.getIntervalType();
                noOfInvPost = rpay.getNoOfpaymentspost();
                noOfInvRemainPost = rpay.getNoOfRemainpaymentspost();
                jobj.put("startdate", rpay.getStartDate());
                jobj.put("nextdate", rpay.getNextDate());
                jobj.put("expdate", rpay.getExpireDate());
            }

            /*
             * Edit Case - Deleting previous line level entried for payment 1.
             * Delete Advance Type Details 2. Delete Against Vendor Invoice
             * Details and Update respective Invoice's amount due 3. Delete
             * Against GL Details 4. Delete payment against CN/DN
             */

            //Delete TDSDetails
            if (extraCompanyPreferences.isTDSapplicable() && isEdit) {
                HashMap<String, String> hmData = new HashMap<String, String>();
                String res = "";
                ArrayList<String> resList = new ArrayList<String>();
                hmData.put("companyid", payment.getCompany().getCompanyID());
                if (payment.getAdvanceDetails().size() > 0) {
                    Set<AdvanceDetail> rowObj = payment.getAdvanceDetails();
                    for (AdvanceDetail advDetail : rowObj) {
                        resList.add("'" + advDetail.getId() + "'");
                    }
                    res = StringUtils.collectionToCommaDelimitedString(resList);
                    hmData.put("documentid", res);
                    hmData.put("documenttype", Constants.AdvancePayment + "");
                    accVendorPaymentobj.deleteTDSDetails(hmData);
                }
                if (payment.getRows().size() > 0) {
                    Set<PaymentDetail> rowObj = payment.getRows();
                    for (PaymentDetail advDetail : rowObj) {
                        resList.add("'" + advDetail.getID() + "'");
                    }
                    res = StringUtils.collectionToCommaDelimitedString(resList);
                    hmData.put("documentid", res);
                    hmData.put("documenttype", Constants.PaymentAgainstInvoice + "");
                    accVendorPaymentobj.deleteTDSDetails(hmData);
                }
                if (payment.getPaymentDetailOtherwises().size() > 0) {
                    Set<PaymentDetailOtherwise> rowObj = payment.getPaymentDetailOtherwises();
                    for (PaymentDetailOtherwise advDetail : rowObj) {
                        resList.add("'" + advDetail.getID() + "'");
                    }
                    res = StringUtils.collectionToCommaDelimitedString(resList);
                    hmData.put("documentid", res);
                    hmData.put("documenttype", Constants.GLPayment + "");
                    accVendorPaymentobj.deleteTDSDetails(hmData);
                }
                if (payment.getCreditNotePaymentDetails().size() > 0) {
                    Set<CreditNotePaymentDetails> rowObj = payment.getCreditNotePaymentDetails();
                    for (CreditNotePaymentDetails advDetail : rowObj) {
                        resList.add("'" + advDetail.getID() + "'");
                    }
                    res = StringUtils.collectionToCommaDelimitedString(resList);
                    hmData.put("documentid", res);
                    hmData.put("documenttype", Constants.PaymentAgainstCNDN + "");
                    accVendorPaymentobj.deleteTDSDetails(hmData);
                }
            }

            //Delete Advance Type Details
            Set<AdvanceDetail> advanceDetails = payment.getAdvanceDetails();
            if (payment.getAdvanceDetails() != null && !advanceDetails.isEmpty()) {
                updateReceiptAdvancePaymentAmountDue(payment, companyid, beforeEditApprovalStatus, countryID);
                accVendorPaymentobj.deleteAdvancePaymentsDetails(payment.getID(), payment.getCompany().getCompanyID());
                /**
                 * Delete GST tax class history.
                 */
                if (payment.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                    String idstring = "";
                    for (AdvanceDetail receiptAdvanceDetail : advanceDetails) {
                        idstring += "'" + receiptAdvanceDetail.getId() + "',";
                    }
                    if (idstring.length() > 1) {
                        idstring = idstring.substring(0, idstring.length() - 1);
                        fieldManagerDAOobj.deleteGstTaxClassDetails(idstring);
                    }
                }
            }

            // Delete Against Vendor Invoice Details and Update respective Invoice's amount due
            accVendorPaymentobj.deletePaymentsDetailsAndUpdateAmountDue(payment.getID(), payment.getCompany().getCompanyID(),beforeEditApprovalStatus);

            // Delete Against GL Details
            Set<PaymentDetailOtherwise> paymentDetailOtherwises = payment.getPaymentDetailOtherwises();
            if (payment.getPaymentDetailOtherwises() != null && !paymentDetailOtherwises.isEmpty()) {
                accVendorPaymentobj.deletePaymentsDetailsOtherwise(payment.getID());
            }
            //Delete payment against CN/DN
            accVendorPaymentobj.deletePaymentsAgainstCNDN(payment.getID(), payment.getCompany().getCompanyID(),beforeEditApprovalStatus);

            /*
             * Deleting Linking information of Make Payment while editing Make
             * Payment
             */
            if (isEdit) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("paymentid", payment.getID());
                accVendorPaymentobj.deleteLinkingInformationOfMP(requestParams);

                /*
                 * Deleting the mapping entries posted for recovering the
                 * invoices
                 */
                HashMap<String, Object> requestParamsForMapping = new HashMap();
                requestParamsForMapping.put(Constants.companyid, payment.getCompany().getCompanyID());
                requestParamsForMapping.put("paymentid", payment.getID());
                accGoodsReceiptobj.deleteBadDebtPurchaseInvoiceMapping(requestParamsForMapping);
            }

            /*
             * Adding Details
             */
                paramJobj.put("isDiscountAppliedOnPaymentTerms", isDiscountAppliedOnPaymentTerms);
                paramJobj.put("isEdit", isEdit);    //Passing isEdit flag as it used in journalEntryDetailObject to identify which account discount value will be added.ERM-981
            /*
             * Invoice Details Object
             */
            if (jSONArrayAgainstInvoice.length() > 0) {
                if (details == null) {
                    details = journalEntryDetailObject(paramJobj, jSONArrayAgainstInvoice, journalEntry, payment, Constants.PaymentAgainstInvoice);
                } else {
                    details.addAll(journalEntryDetailObject(paramJobj, jSONArrayAgainstInvoice, journalEntry, payment, Constants.PaymentAgainstInvoice));
                }
                //Save Invoice Details selected in payment
                HashSet paymentDetails = paymentDetailObject(paramJobj, jSONArrayAgainstInvoice, payment, Constants.PaymentAgainstInvoice);
                payment.setRows(paymentDetails);
            }

//            status = txnManager.getTransaction(def);

            /*
             * Advance Details Object
             */
            if (jSONArrayAdvance.length() > 0) {
                if (details == null) {
                    details = journalEntryDetailObject(paramJobj, jSONArrayAdvance, journalEntry, payment, Constants.AdvancePayment);
                } else {
                    details.addAll(journalEntryDetailObject(paramJobj, jSONArrayAdvance, journalEntry, payment, Constants.AdvancePayment));
                }

                //Save Advance Details selected in payment
                advanceDetails = advanceDetailObject(paramJobj, jSONArrayAdvance, payment, Constants.AdvancePayment);
                payment.setAdvanceDetails(advanceDetails);
            }

            // Against Invoice Details Object
//            if (jSONArrayAgainstInvoice.length() > 0) {
//                if (details == null) {
//                    details = journalEntryDetailObject(request, jSONArrayAgainstInvoice, journalEntry, payment, Constants.PaymentAgainstInvoice);
//                } else {
//                    details.addAll(journalEntryDetailObject(request, jSONArrayAgainstInvoice, journalEntry, payment, Constants.PaymentAgainstInvoice));
//                }
//                //Save Invoice Details selected in payment
//                HashSet paymentDetails = paymentDetailObject(request, jSONArrayAgainstInvoice, payment, Constants.PaymentAgainstInvoice);
//                payment.setRows(paymentDetails);
//            }

            /*
             * Against CN/DN Object
             */
            int sequencecounter = 0;
            Map<String, Object> counterMap = new HashMap<>();
            counterMap.put("counter", sequencecounter);
            if (jSONArrayCNDN.length() > 0) {
                if (details == null) {
                    details = journalEntryDetailObject(paramJobj, jSONArrayCNDN, journalEntry, payment, Constants.PaymentAgainstCNDN);
                } else {
                    details.addAll(journalEntryDetailObject(paramJobj, jSONArrayCNDN, journalEntry, payment, Constants.PaymentAgainstCNDN));
                }
                //Save CN / DN details selected in payment
                saveCNDNDetailObject(paramJobj, jSONArrayCNDN, payment, Constants.PaymentAgainstCNDN, counterMap);
            }

            /*
             * Against GL Object
             */
            if (jSONArrayGL.length() > 0) {
                if (details == null) {
                    details = journalEntryDetailObject(paramJobj, jSONArrayGL, journalEntry, payment, Constants.GLPayment);
                } else {
                    details.addAll(journalEntryDetailObject(paramJobj, jSONArrayGL, journalEntry, payment, Constants.GLPayment));
                }
            }
            /*
             * If Balace Amount is greater than Zero
             */
            if (balaceAmount > 0) {
                if (details == null) {
                    details = journalEntryDetailObject(paramJobj, jSONArrayBal, journalEntry, payment, Constants.BALACEAMOUNT);
                } else {
                    details.addAll(journalEntryDetailObject(paramJobj, jSONArrayBal, journalEntry, payment, Constants.BALACEAMOUNT));
                }
            }

            /*
             * Create Journal-Entry-Detail object for following cases 1. For
             * Bank Changes 2. For Bank Interest 3. Total Payment Amount
             */
            if (editPaymentObject != null) {
                editPaymentObject.setJournalEntryForBankCharges(null);
                editPaymentObject.setJournalEntryForBankInterest(null);
                editPaymentObject.setImportServiceJE(null);
            }

            sequencecounter = (Integer) counterMap.get("counter");
            if (details != null) {
                bankChargesAccid = paramJobj.optString("bankChargesCmb");
                if (!StringUtil.isNullOrEmpty(bankChargesAccid)) {      // Create JE for Bank charges
//                    sequencecounter++;
                    boolean paymentWithoutJe = false;
                    if (editPaymentObject != null) {
                        paymentWithoutJe = oldBankChargeJE != null ? false : true;
                    }
                    if (editPaymentObject == null || (editPaymentObject != null ? oldBankChargeJE != null : true)) {
                        JournalEntry bankChargesJournalEntry = journalEntryObjectBankCharges(paramJobj, editPaymentObject, sequencecounter, true, paymentWithoutJe, oldBankChargeJE, oldBankInterestJE);
                        bankChargeJeId = bankChargesJournalEntry.getID();
                        payment.setJournalEntryForBankCharges(bankChargesJournalEntry);
                        payments.add(payment);
                        if (editPaymentObject != null) {
                            jeEntryNo = ", " + bankChargesJournalEntry.getEntryNumber();
                        }
                        //ERP-18778
                        bankChargesJournalEntry.setIsmulticurrencypaymentje(journalEntry.isIsmulticurrencypaymentje());
                        bankChargesJournalEntry.setPaymentcurrencytopaymentmethodcurrencyrate(journalEntry.getPaymentcurrencytopaymentmethodcurrencyrate());
                        bankChargesJournalEntry.setTransactionId(paymentID);
                        bankChargesJournalEntry.setTransactionModuleid(Constants.Acc_Make_Payment_ModuleId);
                        if (approvalStatusLevel == 11) {
                            bankChargesJournalEntry.setPendingapproval(0);
                        } else {
                            bankChargesJournalEntry.setPendingapproval(1);
                        }
                        accJournalEntryobj.saveJournalEntryByObject(bankChargesJournalEntry);
                        Set<JournalEntryDetail> detail = null;
                        detail = (journalEntryDetailCommonObjectsForBankCharges(paramJobj, bankChargesJournalEntry, payment, true));
//                detail.addAll(journalEntryDetailCommonObjectsForBankCharges(request, bankChargesJournalEntry, receipt));
                        bankChargesJournalEntry.setDetails(detail);
                        accJournalEntryobj.saveJournalEntryDetailsSet(detail);
                        sequencecounter++;
                    }
                }
                bankInterestAccid = paramJobj.optString("bankInterestCmb");
                if (!StringUtil.isNullOrEmpty(bankInterestAccid)) {     //Create JE for bank interest
//                    sequencecounter++;
                    boolean paymentWithoutJe = false;
                    if (editPaymentObject != null) {
                        paymentWithoutJe = oldBankInterestJE != null ? false : true;
                    }
                    if (editPaymentObject == null || (editPaymentObject != null ? oldBankInterestJE != null : true)) {
                        JournalEntry bankInterestJournalEntry = journalEntryObjectBankCharges(paramJobj, editPaymentObject, sequencecounter, false, paymentWithoutJe, oldBankChargeJE, oldBankInterestJE);
                        payment.setJournalEntryForBankInterest(bankInterestJournalEntry);
                        payments.add(payment);
                        if (editPaymentObject != null) {
                            jeEntryNo += ", " + bankInterestJournalEntry.getEntryNumber();
                        }
                        //ERP-18778
                        bankInterestJournalEntry.setIsmulticurrencypaymentje(journalEntry.isIsmulticurrencypaymentje());
                        bankInterestJournalEntry.setPaymentcurrencytopaymentmethodcurrencyrate(journalEntry.getPaymentcurrencytopaymentmethodcurrencyrate());
                        bankInterestJournalEntry.setTransactionId(paymentID);
                        bankInterestJournalEntry.setTransactionModuleid(Constants.Acc_Make_Payment_ModuleId);
                        accJournalEntryobj.saveJournalEntryByObject(bankInterestJournalEntry);
                        if (approvalStatusLevel == 11) {
                            bankInterestJournalEntry.setPendingapproval(0);
                        } else {
                            bankInterestJournalEntry.setPendingapproval(1);
                        }

                        Set<JournalEntryDetail> detail = null;
                        detail = (journalEntryDetailCommonObjectsForBankCharges(paramJobj, bankInterestJournalEntry, payment, false));
//                detail.addAll(journalEntryDetailCommonObjectsForBankCharges(request, bankChargesJournalEntry, receipt));
                        bankInterestJournalEntry.setDetails(detail);
                        accJournalEntryobj.saveJournalEntryDetailsSet(detail);
                    }
                }
                details.addAll(journalEntryDetailCommonObjects(paramJobj, jSONArrayCNDN, journalEntry, payment, Constants.PaymentAgainstCNDN, editPaymentObject, oldBankChargeJE, oldBankInterestJE));
            }
            
            // Save JEDetail Object
            journalEntry.setDetails(details);
            journalEntry.setTransactionId(paymentID);
            journalEntry.setTransactionModuleid(Constants.Acc_Make_Payment_ModuleId);
            accJournalEntryobj.saveJournalEntryDetailsSet(details);
            
            
            /**
             * Checking whether isPostingDateCheck is enabled and the document
             * is not going for approval.If yes then we do not have to reconcile
             * the payment even if Cheque status is set to cleared. ERM-655 /
             * ERP-36202.
             */
            if (isPostingDateCheck && approvalStatusLevel != Constants.APPROVED_STATUS_LEVEL) {
                paramJobj.put("isNotToReconcil", true);
            }
            /*
             * Create PayDetail Object for - 1. Cheque Details
             */
            PayDetail payDetail = getPayDetailObject(paramJobj, editPaymentObject, payment);
            payment.setPayDetail(payDetail);

            /*
             * Revaluation JE Entry - If Payment Against Invoice which are
             * revaluated ***Parameter - oldReevalJE - Revaluation Journal Entry
             * ID 1. Revaluation Agianst invoice
             */
            counterMap.put("counter", sequencecounter);
            saveReevalJournalEntryObjects(paramJobj, jSONArrayAgainstInvoice, payment, Constants.PaymentAgainstInvoice, oldReevalJE, counterMap);

            // Get Vendor Name and Vendor Address
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("accid"))) {
                KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), paramJobj.optString("accid"));
                Account account = (Account) accresult.getEntityList().get(0);
                if (account != null) {
                    String accountid = account.getID();
                    result = accVendorPaymentobj.getaccountdetailsPayment(accountid);
                    if (result.getRecordTotalCount() > 0) {
                        Vendor vendor = (Vendor) result.getEntityList().get(0);
                        if(vendor.getAddress()!=null)
                        accountaddress = vendor.getAddress();
                        else
                            accountaddress="";
                    }
                    accountName = account.getName();
                    KwlReturnObject accresultVen = accountingHandlerDAOobj.getObject(Vendor.class.getName(), account.getID());
                    String vendorName = "";
                    if (!accresultVen.getEntityList().isEmpty()) {
                        Vendor vendor = (Vendor) accresultVen.getEntityList().get(0);
                        if (vendor != null) {
                            vendorName = vendor.getName();
                        }
                    }
                }
            }

            // Get Vendor or Customer Name/ Paid To value
            String payee = payment.getPayee();
            if (payment.getPaymentWindowType() == Constants.Make_Payment_to_Vendor && StringUtil.isNullOrEmpty(payee)) { // Against Vendor
                payee = payment.getVendor().getName();
                payee = StringUtil.DecodeText(payee);
            } else if (payment.getPaymentWindowType() == Constants.Make_Payment_to_Customer && StringUtil.isNullOrEmpty(payee)) {  // Against Customer
                payee = payment.getCustomer();
                KwlReturnObject resultCVAccount = accountingHandlerDAOobj.getObject(Customer.class.getName(), payee);
                Customer customer = (Customer) resultCVAccount.getEntityList().get(0);
                if (customer != null) {
                    payee = StringUtil.DecodeText(customer.getName());
                }
            } else {  // Against GL
                if (payment.getPaidTo() != null && StringUtil.isNullOrEmpty(payee)) {
                    payee = payment.getPaidTo().getValue();
                    //payee = StringUtil.DecodeText(payee);// Comment Because  ERP-9738
                }
            } 

            //To get the Vendor/Customer Billing Address 

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("accid"))) {
                //params to send to get billing address
                HashMap<String, Object> addressParams = new HashMap<String, Object>();
                addressParams.put("companyid", companyid);
                addressParams.put("isDefaultAddress", true);
                addressParams.put("isBillingAddress", true);
                if (payment.getPaymentWindowType() == Constants.Make_Payment_to_Vendor) {
                    addressParams.put("vendorid", paramJobj.optString("accid"));
                    VendorAddressDetails vendorAddressDetail = accountingHandlerDAOobj.getVendorAddressObj(addressParams);
                    vendorEmailId = vendorAddressDetail != null ? vendorAddressDetail.getEmailID() : "";
                    billingAddress = vendorAddressDetail != null ? vendorAddressDetail.getAddress(): "";

                } else if (payment.getPaymentWindowType() == Constants.Make_Payment_to_Customer) {
                    addressParams.put("customerid", paramJobj.optString("accid"));
                    CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                    vendorEmailId = customerAddressDetails != null ? customerAddressDetails.getEmailID() : "";
                    billingAddress = customerAddressDetails != null ? customerAddressDetails.getAddress() : "";
                }
            }

            // Start of Print cheque
            // Below code used to Format Amount for Print Cheque Case
            DecimalFormat df = new DecimalFormat("#,###,###,##0.00");
            String basecurrency = paramJobj.optString("currencyid");
            KwlReturnObject resultCurrency = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) resultCurrency.getEntityList().get(0);
            String amt = df.format(payment.getDepositAmount());
            netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(payment.getDepositAmount())), currency, countryLanguageId);
            hashMap.put("decimalFormat", df);
            hashMap.put("payment", payment);
            hashMap.put("paymentId", payment.getID());
            hashMap.put("accountaddress", accountaddress);
            hashMap.put("accountName", accountName);
            String[] tempArray = new String[]{"amountinword", netinword};
            hashMap.put("netinword", tempArray);
            String[] tempArray1 = new String[]{"accountName", payee};
            hashMap.put("payee", tempArray1);
            /*
            Getting transaction level Memo value
            */
            hashMap.put(Constants.memo, StringUtil.isNullOrEmpty(payment.getMemo())?"":payment.getMemo());
            hashMap.put("address", billingAddress);

            String action = "made";
            if (isEdit) {
                action = "updated";
            }
            paymentid = payment.getID();
            amountpayment = payment.getDepositAmount() + "";
            billno = payment.getPaymentNumber();
            String JENumBer = "";
            if (payment.getJournalEntry() != null) {
                JENumBer = payment.getJournalEntry().getEntryNumber();
            }
            boolean isChequePrint = false;
            String accNameForAudit ="";
            String chqno = "";
            issuccess = true;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isChequePrint"))) {
                isChequePrint = Boolean.parseBoolean(paramJobj.optString("isChequePrint"));
            }
            if (isChequePrint) {
                KwlReturnObject resultPayMethod = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod"));
                PaymentMethod payMethod = (PaymentMethod) resultPayMethod.getEntityList().get(0);

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                if (!StringUtil.isNullOrEmpty(payMethod.getID())) {
                    requestParams.put("bankid", payMethod.getID());
                }
//                DateFormat
                boolean isnewlayout = false;
                ChequeLayout chequeLayout=null;
                DateFormat DATE_FORMAT = new SimpleDateFormat(Constants.DEFAULT_FORMAT_CHECK);
                String prefixbeforamt="";
                String dateformat="";
                HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
                KwlReturnObject result1 = accPaymentDAOobj.getChequeLayout(requestParams);
                List listCheque = result1.getEntityList();
                Iterator itr = listCheque.iterator();
                JSONObject chequeobj = new JSONObject();
                while (itr.hasNext()) {
                    chequeLayout = (ChequeLayout) itr.next();
                    chequeobj = new JSONObject(chequeLayout.getCoordinateinfo());
                    jobjDetails.put("dateLeft", chequeobj.optString("dateLeft", "0"));
                    jobjDetails.put("nameLeft", chequeobj.optString("nameLeft", "0"));
                    jobjDetails.put("amtinwordLeft", chequeobj.optString("amtinwordLeft", "0"));
                    jobjDetails.put("amtinwordLeftLine2", chequeobj.optString("amtinwordLeftLine2", "0"));
                    jobjDetails.put("amtLeft", chequeobj.optString("amtLeft", "0"));
                    jobjDetails.put("dateTop", chequeobj.optString("dateTop", "0"));
                    jobjDetails.put("nameTop", chequeobj.optString("nameTop", "0"));
                    jobjDetails.put("amtinwordTop", chequeobj.optString("amtinwordTop", "0"));
                    jobjDetails.put("amtinwordTopLine2", chequeobj.optString("amtinwordTopLine2", "0"));
                    jobjDetails.put("amtTop", chequeobj.optString("amtTop", "0"));

                    /*
                     Get US related additional cheque co-ordinates 
                    */
                    if (chequeLayout.isActivateExtraFields()) {
                        if (!chequeobj.optString("memoLeft").equals("-1")) {
                            jobjDetails.put("memoLeft", chequeobj.optString("memoLeft", "0"));
                        }
                        if (!chequeobj.optString("memoTop").equals("-1")) {
                            jobjDetails.put("memoTop", chequeobj.optString("memoTop", "0"));
                        }
                        if (!chequeobj.optString("addressLine1Left").equals("-1")) {
                            jobjDetails.put("addressLine1Left", chequeobj.optString("addressLine1Left", "0"));
                        }
                        if (!chequeobj.optString("addressLine1Top").equals("-1")) {
                            jobjDetails.put("addressLine1Top", chequeobj.optString("addressLine1Top", "0"));
                        }
                        if (!chequeobj.optString("addressLine2Left").equals("-1")) {
                            jobjDetails.put("addressLine2Left", chequeobj.optString("addressLine2Left", "0"));
                        }
                        if (!chequeobj.optString("addressLine2Top").equals("-1")) {
                            jobjDetails.put("addressLine2Top", chequeobj.optString("addressLine2Top", "0"));
                        }
                        if (!chequeobj.optString("addressLine3Left").equals("-1")) {
                            jobjDetails.put("addressLine3Left", chequeobj.optString("addressLine3Left", "0"));
                        }
                        if (!chequeobj.optString("addressLine3Top").equals("-1")) {
                            jobjDetails.put("addressLine3Top", chequeobj.optString("addressLine3Top", "0"));
                        }
                        if (!chequeobj.optString("addressLine4Left").equals("-1")) {
                            jobjDetails.put("addressLine4Left", chequeobj.optString("addressLine4Left", "0"));
                        }
                        if (!chequeobj.optString("addressLine4Top").equals("-1")) {
                            jobjDetails.put("addressLine4Top", chequeobj.optString("addressLine4Top", "0"));
                        }
                    }
                    
                    if (!StringUtil.isNullObject(chequeLayout) && !StringUtil.isNullObject(chequeLayout.getDateFormat()) && !StringUtil.isNullObject(chequeLayout.getDateFormat().getJavaForm())) {
                        dateformat = chequeLayout.getDateFormat().getJavaForm();
                    } else {
                        KwlReturnObject kWLDateFormatResult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), Constants.DEFAULT_FORMATID_CHECK);
                        KWLDateFormat kWLDateFormat = (KWLDateFormat) kWLDateFormatResult.getEntityList().get(0);
                        dateformat = kWLDateFormat.getJavaForm();
//                        dateformat = paramJobj.getString(Constants.userdateformat);
                    }
               
                    /*
                     If 'AddCharacterInCheckDate' is true then don't remove '/' or '-' from check Date
                     */
                    if (!chequeLayout.isAddCharacterInCheckDate()) {
                        dateformat = dateformat.replaceAll("/", "");
                        dateformat = dateformat.replaceAll("-", "");
                    }
                    DATE_FORMAT = new SimpleDateFormat(dateformat);
                    prefixbeforamt = chequeLayout.getAppendcharacter();
                    isnewlayout = chequeLayout.isIsnewlayout();
                }
                
                //DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT" + sessionHandlerImpl.getTimeZoneDifference(request)));
                //     Date creationDate = new Date(request.getParameter("creationdate"));
                //  Date creationDate = payment.getChequeDate();

                //ERROR PRONE CODE
                boolean isFontStylePresent = chequeobj.has("fontStyle") && !StringUtil.isNullOrEmpty(chequeobj.getString("fontStyle")) ? true : false;
                String fontStyle = chequeobj.has("fontStyle") && !StringUtil.isNullOrEmpty(chequeobj.getString("fontStyle")) ? chequeobj.getString("fontStyle") : "";
                requestParams1.put("fontStyle", fontStyle);
                char fontStyleChar;
                if (fontStyle.equals("1")) {
                    fontStyleChar = 'b';
                } else if (fontStyle.equals("2")) {
                    fontStyleChar = 'i';
                } else {
                    fontStyleChar = 'p';
                }
                String date = DATE_FORMAT.format(payment.getPayDetail().getCheque().getDueDate());
                String formatted_date_with_spaces = "";
                if (dateformat.equalsIgnoreCase(Constants.MMMMddyyyy) ||(chequeLayout!=null && chequeLayout.isAddCharacterInCheckDate())) { //if Date format ='MMMM dd,yyyy'(January 23,2017) that's why no need to add space.
                    formatted_date_with_spaces = date;
                } else {
                    for (int i = 0; i < date.length(); i++) {
                        formatted_date_with_spaces += date.charAt(i);
                        formatted_date_with_spaces += isnewlayout ? "&nbsp;&nbsp;&nbsp;" : "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

                    }
                }
                String[] amount1 = hashMap.get("netinword") != null ? (String[]) hashMap.get("netinword") : null;
                String[] accName = hashMap.get("payee") != null ? (String[]) hashMap.get("payee") : null;
                jobjDetails.put("amount", prefixbeforamt+amt);
                String amount_first_line = "";
                String amount_second_line = "";
                String payAction = " Only.";
                String[] memo = {"memo", ""};
                String address = hashMap.get("address") != null ? (String) hashMap.get("address") : "";
                String memoInRequest = hashMap.get("memo") != null ? (String) hashMap.get("memo") : "";
                String[] addressArr = {"address", address};
                String address_Line1 = "", address_Line2 = "", address_Line3 = "", address_Line4 = "";
                if (chequeLayout != null && chequeLayout.isActivateExtraFields()) {
                    memo = new String[]{"memo", memoInRequest};
                    if (StringUtil.isNullOrEmpty(address)) {
                        jobjDetails.put("addressLine1", "");
                        jobjDetails.put("addressLine2", "");
                        jobjDetails.put("addressLine3", "");
                        jobjDetails.put("addressLine4", "");
                    }
                }
                if (amount1[1].length() > 34 && amount1[1].charAt(34) == ' ') {
                    amount_first_line = amount1[1].substring(0, 34);
                    amount_second_line = amount1[1].substring(34, amount1[1].length());
                    jobjDetails.put(amount1[0], amount_first_line);
                    jobjDetails.put("amountinword1", amount_second_line + payAction);
                } else if (amount1[1].length() > 34) {
                    amount_first_line = amount1[1].substring(0, 34);
                    amount_first_line = amount1[1].substring(0, amount_first_line.lastIndexOf(" "));
                    amount_second_line = amount1[1].substring(amount_first_line.length(), amount1[1].length());
                    jobjDetails.put(amount1[0], amount_first_line);
                    jobjDetails.put("amountinword1", amount_second_line + payAction);
                } else {
                    if (amount1[1].length() < 27) {
                        jobjDetails.put(amount1[0], amount1[1] + payAction);
                        jobjDetails.put("amountinword1", "");
                    } else {
                        jobjDetails.put(amount1[0], amount1[1]);
                        jobjDetails.put("amountinword1", payAction);
                    }
                }
                
                 if (!StringUtil.isNullOrEmpty(address) && chequeLayout != null && chequeLayout.isActivateExtraFields()) {
                    int len = address.length();
                    if (len <= 50) {
                        requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine1Txt", "'"));
                        requestParams1.put("fontString", addressArr[1]);
                        if (jobjDetails.has("addressLine1Left") && jobjDetails.has("addressLine1Top")) {
                            jobjDetails.put("addressLine1", authHandler.applyFontStyleForCheque(requestParams1));
                        } else {
                            jobjDetails.put("addressLine1", "");
                        }
                        jobjDetails.put("addressLine2", "");
                        jobjDetails.put("addressLine3", "");
                        jobjDetails.put("addressLine4", "");
                    } else if (len > 50 && len <= 100) {
                        address_Line1 = addressArr[1].substring(0, 50);
                        if (addressArr[1].charAt(50) == ' ' || addressArr[1].charAt(50) == '\n') {
                            address_Line1 = addressArr[1].substring(0, 50);
                        } else if (address_Line1.lastIndexOf(" ") > 0) {
                            address_Line1 = addressArr[1].substring(0, address_Line1.lastIndexOf(" "));
                        } else {
                            address_Line1 = addressArr[1].substring(0, address_Line1.lastIndexOf("\n"));
                        }
                        address_Line2 = addressArr[1].substring(address_Line1.length(), addressArr[1].length());
                        requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine1Txt", "'"));
                        requestParams1.put("fontString", address_Line1);
                        if (jobjDetails.has("addressLine1Left") && jobjDetails.has("addressLine1Top")) {
                            jobjDetails.put("addressLine1", authHandler.applyFontStyleForCheque(requestParams1));
                        } else {
                            jobjDetails.put("addressLine1", "");
                        }
                        requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine2Txt", "'"));
                        requestParams1.put("fontString", address_Line2);
                        if (jobjDetails.has("addressLine2Left") && jobjDetails.has("addressLine2Top")) {
                            jobjDetails.put("addressLine2", authHandler.applyFontStyleForCheque(requestParams1));
                        } else {
                            jobjDetails.put("addressLine2", "");
                        }

                        jobjDetails.put("addressLine3", "");
                        jobjDetails.put("addressLine4", "");

                    } else if (len > 100 && len <= 150) {
                        address_Line1 = addressArr[1].substring(0, 50);
                        if (addressArr[1].charAt(50) == ' ' || addressArr[1].charAt(50) == '\n') {
                            address_Line1 = addressArr[1].substring(0, 50);
                        } else if (address_Line1.lastIndexOf(" ") > 0) {
                            address_Line1 = addressArr[1].substring(0, address_Line1.lastIndexOf(" "));
                        } else {
                            address_Line1 = addressArr[1].substring(0, address_Line1.lastIndexOf("\n"));
                        }
                        address_Line2 = addressArr[1].substring(address_Line1.length(), 100);

                        if (addressArr[1].charAt(100) == ' ' || addressArr[1].charAt(100) == '\n') {
                            address_Line2 = addressArr[1].substring(address_Line1.length(), 100);
                        } else if (address_Line1.lastIndexOf(" ") > 0) {
                            address_Line2 = addressArr[1].substring(address_Line1.length(), (address_Line1 + address_Line2).lastIndexOf(" "));
                        } else {
                            address_Line2 = addressArr[1].substring(address_Line1.length(), (address_Line1 + address_Line2).lastIndexOf("\n"));
                        }
                        address_Line3 = addressArr[1].substring((address_Line2.length() + address_Line2.length()), addressArr[1].length());

                        requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine1Txt", "'"));
                        requestParams1.put("fontString", address_Line1);
                        if (jobjDetails.has("addressLine1Left") && jobjDetails.has("addressLine1Top")) {
                            jobjDetails.put("addressLine1", authHandler.applyFontStyleForCheque(requestParams1));
                        } else {
                            jobjDetails.put("addressLine1", "");
                        }
                        requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine2Txt", "'"));
                        requestParams1.put("fontString", address_Line2);
                        if (jobjDetails.has("addressLine2Left") && jobjDetails.has("addressLine2Top")) {
                            jobjDetails.put("addressLine2", authHandler.applyFontStyleForCheque(requestParams1));
                        } else {
                            jobjDetails.put("addressLine2", "");
                        }
                        requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine3Txt", "'"));
                        requestParams1.put("fontString", address_Line3);

                        if (jobjDetails.has("addressLine3Left") && jobjDetails.has("addressLine3Top")) {
                            jobjDetails.put("addressLine3", authHandler.applyFontStyleForCheque(requestParams1));
                        } else {
                            jobjDetails.put("addressLine3", "");
                        }

                        jobjDetails.put("addressLine4", "");

                    } else if (len > 150) {

                        address_Line1 = addressArr[1].substring(0, 50);
                        if (addressArr[1].charAt(50) == ' ' || addressArr[1].charAt(50) == '\n') {
                            address_Line1 = addressArr[1].substring(0, 50);
                        } else if (address_Line1.lastIndexOf(" ") > 0) {
                            address_Line1 = addressArr[1].substring(0, address_Line1.lastIndexOf(" "));
                        } else {
                            address_Line1 = addressArr[1].substring(0, address_Line1.lastIndexOf("\n"));
                        }
                        address_Line2 = addressArr[1].substring(address_Line1.length(), 100);

                        if (addressArr[1].charAt(100) == ' ' || addressArr[1].charAt(100) == '\n') {
                            address_Line2 = addressArr[1].substring(address_Line1.length(), 100);
                        } else if ((address_Line1 + address_Line2).lastIndexOf(" ") > 0) {
                            address_Line2 = addressArr[1].substring(address_Line1.length(), (address_Line1 + address_Line2).lastIndexOf(" "));
                        } else {
                            address_Line2 = addressArr[1].substring(address_Line1.length(), (address_Line1 + address_Line2).lastIndexOf("\n"));
                        }

                        address_Line3 = addressArr[1].substring((address_Line1.length() + address_Line2.length()), 150);

                        if (addressArr[1].charAt(150) == ' ' || addressArr[1].charAt(150) == '\n') {
                            address_Line3 = addressArr[1].substring((address_Line1.length() + address_Line2.length()), 150);
                        } else if ((address_Line1 + address_Line2 + address_Line3).lastIndexOf(" ") > 0) {
                            address_Line3 = addressArr[1].substring((address_Line1.length() + address_Line2.length()), (address_Line1 + address_Line2 + address_Line3).lastIndexOf(" "));
                        } else {
                            address_Line3 = addressArr[1].substring((address_Line1.length() + address_Line2.length()), (address_Line1 + address_Line2 + address_Line3).lastIndexOf("\n"));
                        }
                        address_Line4 = addressArr[1].substring((address_Line1.length() + address_Line2.length() + address_Line3.length()), addressArr[1].length());

                        requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine1Txt", "'"));
                        requestParams1.put("fontString", address_Line1);
                        if (jobjDetails.has("addressLine1Left") && jobjDetails.has("addressLine1Top")) {
                            jobjDetails.put("addressLine1", authHandler.applyFontStyleForCheque(requestParams1));
                        } else {
                            jobjDetails.put("addressLine1", "");
                        }
                        requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine2Txt", "'"));
                        requestParams1.put("fontString", address_Line2);
                        if (jobjDetails.has("addressLine2Left") && jobjDetails.has("addressLine2Top")) {
                            jobjDetails.put("addressLine2", authHandler.applyFontStyleForCheque(requestParams1));
                        } else {
                            jobjDetails.put("addressLine2", "");
                        }
                        requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine3Txt", "'"));
                        requestParams1.put("fontString", address_Line3);
                        if (jobjDetails.has("addressLine3Left") && jobjDetails.has("addressLine3Top")) {
                            jobjDetails.put("addressLine3", authHandler.applyFontStyleForCheque(requestParams1));
                        } else {
                            jobjDetails.put("addressLine3", "");
                        }
                        requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine4Txt", "'"));
                        requestParams1.put("fontString", address_Line4);
                        if (jobjDetails.has("addressLine4Left") && jobjDetails.has("addressLine4Top")) {
                            jobjDetails.put("addressLine4", authHandler.applyFontStyleForCheque(requestParams1));
                        } else {
                            jobjDetails.put("addressLine4", "");
                        }
                    }

                }

                /*
                 For memo formating and put in json
                 */
                if (chequeLayout != null && chequeLayout.isActivateExtraFields()) {
                    requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForMemoTxt", "'"));
                    requestParams1.put("fontString", memo[1]);
                    if (jobjDetails.has("memoLeft") && jobjDetails.has("memoTop")) {
                        jobjDetails.put(memo[0], authHandler.applyFontStyleForCheque(requestParams1));
                    } else {
                        jobjDetails.put(memo[0], "");
                    }
                }
                jobjDetails.put(accName[0], accName[1]);
                jobjDetails.put("date", formatted_date_with_spaces);
                jobjDetails.put("isnewlayout", isnewlayout);
                jobjDetails.put("activateExtraFields", (chequeLayout!=null?chequeLayout.isActivateExtraFields():false));

                accNameForAudit = accName[1];
                //for name
                if (chequeobj.has("dateFontSize") || isFontStylePresent) {
                    if (isFontStylePresent && !StringUtil.isNullOrEmpty(chequeobj.getString("dateFontSize"))) {
                        formatted_date_with_spaces = "<font size=" + chequeobj.getString("dateFontSize") + "><" + fontStyleChar + ">" + formatted_date_with_spaces + "</" + fontStyleChar + "></font> ";
                        jobjDetails.put("date", formatted_date_with_spaces);
                    } else if (!StringUtil.isNullOrEmpty(chequeobj.getString("dateFontSize"))) {
                        formatted_date_with_spaces = "<font size=" + chequeobj.getString("dateFontSize") + ">" + formatted_date_with_spaces + "</font> ";
                        jobjDetails.put("date", formatted_date_with_spaces);
                    } else {
                        formatted_date_with_spaces = "<" + fontStyleChar + ">" + formatted_date_with_spaces + "</" + fontStyleChar + ">";
                        jobjDetails.put("date", formatted_date_with_spaces);

                    }
                }
                //for name
                if (chequeobj.has("nameFontSize") || isFontStylePresent) {
                    if (isFontStylePresent && !StringUtil.isNullOrEmpty(chequeobj.getString("nameFontSize"))) {
                        accName[1] = "<font size=" + chequeobj.getString("nameFontSize") + "><" + fontStyleChar + ">" + accName[1] + "</" + fontStyleChar + "></font> ";
                        jobjDetails.put(accName[0], accName[1]);
                    } else if (!StringUtil.isNullOrEmpty(chequeobj.getString("nameFontSize"))) {
                        accName[1] = "<font size=" + chequeobj.getString("nameFontSize") + ">" + accName[1] + "</font> ";
                        jobjDetails.put(accName[0], accName[1]);
                    } else {
                        accName[1] = "<" + fontStyleChar + ">" + accName[1] + "</" + fontStyleChar + ">";
                        jobjDetails.put(accName[0], accName[1]);

                    }
                }

                //for amount in words
                if (chequeobj.has("amountInWordsFontSize") || isFontStylePresent) {
                    if (isFontStylePresent && !StringUtil.isNullOrEmpty(chequeobj.getString("amountInWordsFontSize"))) {
                        amount_first_line = "<font size=" + chequeobj.getString("amountInWordsFontSize") + "><" + fontStyleChar + ">" + amount_first_line + "</" + fontStyleChar + "></font> ";
                        amount_second_line = "<font size=" + chequeobj.getString("amountInWordsFontSize") + "><" + fontStyleChar + ">" + amount_second_line + " " + payAction + "</" + fontStyleChar + "></font> ";
                        if (amount1[1].length() > 34) {
                            jobjDetails.put(amount1[0], amount_first_line);
                            jobjDetails.put("amountinword1", amount_second_line);
                        } else if (amount1[1].length() < 27) {
                            amount1[1] = "<font size=" + chequeobj.getString("amountInWordsFontSize") + "><" + fontStyleChar + ">" + amount1[1] + " " + payAction + "</" + fontStyleChar + "></font> ";
                            jobjDetails.put(amount1[0], amount1[1]);
                            jobjDetails.put("amountinword1", "");
                        }
                    } else if (!StringUtil.isNullOrEmpty(chequeobj.getString("amountInWordsFontSize"))) {
                        amount_first_line = "<font size=" + chequeobj.getString("amountInWordsFontSize") + ">" + amount_first_line + "</font> ";
                        amount_second_line = "<font size=" + chequeobj.getString("amountInWordsFontSize") + ">" + amount_second_line + " " + payAction + "</font> ";
                        if (amount1[1].length() > 34) {
                            jobjDetails.put(amount1[0], amount_first_line);
                            jobjDetails.put("amountinword1", amount_second_line);
                        } else if (amount1[1].length() < 27) {
                            amount1[1] = "<font size=" + chequeobj.getString("amountInWordsFontSize") + ">" + amount1[1] + " " + payAction + "</font> ";
                            jobjDetails.put(amount1[0], amount1[1]);
                            jobjDetails.put("amountinword1", "");
                        }
                    } else {
                        amount_first_line = "<" + fontStyleChar + ">" + amount_first_line + "</" + fontStyleChar + ">";
                        amount_second_line = "<" + fontStyleChar + ">" + amount_second_line + " " + payAction + "</" + fontStyleChar + ">";
                        if (amount1[1].length() > 34) {
                            jobjDetails.put(amount1[0], amount_first_line);
                            jobjDetails.put("amountinword1", amount_second_line);
                        } else if (amount1[1].length() < 27) {
                            amount1[1] = "<" + fontStyleChar + ">" + amount1[1] + " " + payAction + "</" + fontStyleChar + ">";
                            jobjDetails.put(amount1[0], amount1[1]);
                            jobjDetails.put("amountinword1", "");
                        }
                    }
                }

                //for amount in number
                if (chequeobj.has("amountFontSize") || isFontStylePresent) {
                    if (isFontStylePresent && !StringUtil.isNullOrEmpty(chequeobj.getString("amountFontSize"))) {
                        amt = "<font size=" + chequeobj.getString("amountFontSize") + "><" + fontStyleChar + ">" + prefixbeforamt +amt + "</" + fontStyleChar + "></font> ";
                        jobjDetails.put("amount", amt);
                    } else if (!StringUtil.isNullOrEmpty(chequeobj.getString("amountFontSize"))) {
                        amt = "<font size=" + chequeobj.getString("amountFontSize") + ">" +  prefixbeforamt +amt + "</font> ";
                        jobjDetails.put("amount", amt);
                    } else {
                        amt = "<" + fontStyleChar + ">" + prefixbeforamt + amt + "</" + fontStyleChar + ">";
                        jobjDetails.put("amount", amt);

                    }
                }
 
                jArr.put(jobjDetails);                
                if (payDetail.getCheque().getChequeNo() != null) {
                    chqno = payDetail.getCheque().getChequeNo();
                }
                Map<String, Object> auditRequestParams = new HashMap<>();
                // auditRequestParams.put(auditSMS, status);
                auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
                KwlReturnObject result2 = accPaymentDAOobj.updateChequePrint(paymentid, companyid);
            }

            String moduleName = Constants.PAYMENT_MADE;
            //Send Mail when Purchase Requisition is generated or modified.
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), paramJobj.optString(Constants.companyKey));
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            if (documentEmailSettings != null) {
                boolean sendmail = false;
                boolean isEditMail = false;
                if (StringUtil.isNullOrEmpty(receiptid)) {
                    if (documentEmailSettings.isVendorPaymentGenerationMail()) { ////Create New Case
                        sendmail = true;
                    }
                } else {
                    isEditMail = true;
                    if (documentEmailSettings.isVendorPaymentUpdationMail()) { // edit case  
                        sendmail = true;
                    }
                }
                if (sendmail) {
                    String userMailId = "", userName = "",currentUserid="";
                    String createdByEmail = "";
                    String createdById = "";
                    HashMap<String, Object> requestParams = AccountingManager.getEmailNotificationParamsJson(paramJobj);
                    if (requestParams.containsKey("userfullName") && requestParams.get("userfullName") != null) {
                        userName = (String) requestParams.get("userfullName");
                    }
                    if (requestParams.containsKey("usermailid") && requestParams.get("usermailid") != null) {
                        userMailId = (String) requestParams.get("usermailid");
                    }
                    if(requestParams.containsKey(Constants.useridKey)&& requestParams.get(Constants.useridKey)!=null){
                        currentUserid=(String)requestParams.get(Constants.useridKey);
                    }
                    List<String> mailIds = new ArrayList();
                    if (!StringUtil.isNullOrEmpty(userMailId)) {
                        mailIds.add(userMailId);
                    }
                    /*
                     if Edit mail option is true then get userid and Email id of document creator.
                    */
                    if (isEditMail) {
                        if (editPaymentObject != null && editPaymentObject.getCreatedby() != null) {
                            createdByEmail = editPaymentObject.getCreatedby().getEmailID();
                            createdById = editPaymentObject.getCreatedby().getUserID();
                        }
                        /*
                         if current user userid == document creator userid then don't add creator email ID in List.
                         */
                        if (!StringUtil.isNullOrEmpty(createdByEmail) && !(currentUserid.equalsIgnoreCase(createdById))) {
                            mailIds.add(createdByEmail);
                        }
                    }
                    String[] temp = new String[mailIds.size()];
                    String[] tomailids = mailIds.toArray(temp);
                    String paymentNumber = entryNumber;
                    accountingHandlerDAOobj.sendSaveTransactionEmails(paymentNumber, moduleName, tomailids, userName, isEditMail, companyid);
                }
            }
            if (oldPayDetail != null) {
                accPaymentDAOobj.deletePayDetail(oldPayDetail.getID(), oldPayDetail.getCompany().getCompanyID());
                if (oldPayDetail.getCard() != null) {
                    Cardid = oldPayDetail.getCard().getID();
                }
                if (oldPayDetail.getCheque() != null) {
                    Cardid = oldPayDetail.getCheque().getID();
                }
                /**
                 * No need to delete existing cheque Edit case because are we
                 * are updating same cheque in edit case.
                 */
//                deleteChequeOrCard(Cardid, oldJE.getCompany().getCompanyID());// method used inside savePayment
            }
            if (oldJE != null) {
                deleteJEArray(oldJE.getID(), oldJE.getCompany().getCompanyID());
            }
            if (oldImportServiceInvoicePaymentJE != null) {
                deleteJEArray(oldImportServiceInvoicePaymentJE.getID(), oldImportServiceInvoicePaymentJE.getCompany().getCompanyID());
            }
            if (oldBankChargeJE != null) {          // delete bank charge JE.
                //Delete the Bank reconcialtion details linked to this JE
                Map<String, Object> bankReconMap = new HashMap<>();
                bankReconMap.put("oldjeid", oldBankChargeJE.getID());
                bankReconMap.put("companyId", oldBankChargeJE.getCompany().getCompanyID());
                deleteBankReconcilation(bankReconMap);
                deleteJEArray(oldBankChargeJE.getID(), oldBankChargeJE.getCompany().getCompanyID());
            }
            if (oldBankInterestJE != null) {          // delete bank Interest JE.
                Map<String, Object> bankReconMap = new HashMap<>();
                bankReconMap.put("oldjeid", oldBankInterestJE.getID());
                bankReconMap.put("companyId", oldBankInterestJE.getCompany().getCompanyID());
                deleteBankReconcilation(bankReconMap);
                deleteJEArray(oldBankInterestJE.getID(), oldBankInterestJE.getCompany().getCompanyID());
            }
            deleteTemporaryInvoicesEntries(jSONArrayAgainstInvoice, companyid);
            accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Make_Payment_ModuleId);
            accPaymentDAOobj.deleteUsedInvoiceOrCheque(chequeNumber, companyid);
            txnManager.commit(status);

            TransactionStatus AutoNoPayStatus = null;
            String bankChargeJeNo = "", bankInterestJeNo = "";
            String linkedDocuments = "";
            String fromLinkCombo = "";
            try {
                synchronized (this) {
                    DefaultTransactionDefinition def2 = new DefaultTransactionDefinition();
                    def2.setName("AutoNum_Tx");
                    def2.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    AutoNoPayStatus = txnManager.getTransaction(def2);
                    // Get JE Number
                    if (editPaymentObject == null) {
                        String jeentryNumber = "";
                        int jeIntegerPart = 0;
                        String jeDatePrefix = "";
                        String jeDateAfterPrefix = "";
                        String jeDateSuffix = "";
                        String jeSeqFormatId = "";

                        HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                        JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                        JEFormatParams.put("modulename", "autojournalentry");
                        JEFormatParams.put("companyid", companyid);
                        JEFormatParams.put("isdefaultFormat", true);

                        KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, journalEntry.getEntryDate());
                        jeentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                        jeIntegerPart = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                        jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                        jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        jeSeqFormatId = format.getID();

                        seqNumberMap.put(Constants.DOCUMENTID, journalEntry.getID());
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, jeSeqFormatId);
                        if (approvalStatusLevel == 11) {
                            JENumBer = accJournalEntryobj.UpdateJournalEntry(seqNumberMap);
                            journalEntry.setEntryNumber(jeentryNumber);
                            journalEntry.setSeqnumber(jeIntegerPart);
                            journalEntry.setDatePreffixValue(jeDatePrefix);
                            journalEntry.setDateAfterPreffixValue(jeDateAfterPrefix);
                            journalEntry.setDateSuffixValue(jeDateSuffix);
                            payment.setJournalEntry(journalEntry);
                        }
                    }
                    // Get Payment Number
                    boolean isFromOtherSource=paramJobj.optBoolean("isFromOtherSource", false);
                    if (!sequenceformat.equals("NA") && editPaymentObject == null && !isFromOtherSource) {
                        boolean seqformat_oldflag = StringUtil.getBoolean(paramJobj.optString("seqformat_oldflag"));
                        String nextAutoNo = "";
                        int nextAutoNoInt = 0;
                        String datePrefix = "";
                        String dateafterPrefix = "";
                        String dateSuffix = "";
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        if (seqformat_oldflag) {
                            nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_PAYMENT, sequenceformat);
                            seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextAutoNo);
                        } else {
//                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_PAYMENT, sequenceformat, seqformat_oldflag, payment.getJournalEntry().getEntryDate());
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_PAYMENT, sequenceformat, seqformat_oldflag, payment.getCreationDate());
                            nextAutoNo = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                            nextAutoNoInt = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                            datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                            dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                            dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        }

                        seqNumberMap.put(Constants.DOCUMENTID, payment.getID());
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        billno = accVendorPaymentobj.UpdatePaymentEntry(seqNumberMap);
                        payment.setPaymentNumber(nextAutoNo);
                        payment.setSeqnumber(nextAutoNoInt);
                        payment.setDatePreffixValue(datePrefix);
                        payment.setDateAfterPreffixValue(dateafterPrefix);
                        payment.setDateSuffixValue(dateSuffix);
                    }

                    if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap)) {
                        /*
                         * saving linking information in Make Payment & Credit
                         * Note If Payment is made with Credit Note
                         */
                        if (jSONArrayCNDN.length() > 0) {
                            JSONArray drAccArr = jSONArrayCNDN;
                            for (int i = 0; i < drAccArr.length(); i++) {

                                JSONObject jobj1 = drAccArr.getJSONObject(i);
                                String cnnoteid = jobj1.getString("documentid");

                                KwlReturnObject cnResult = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnnoteid);
                                CreditNote creditNote = (CreditNote) cnResult.getEntityList().get(0);

                                /*
                                 * Method is used to save linking informatio of
                                 * Make Payment * when linking with Credit Note
                                 */
                                String creditNoteNo = creditNote.getCreditNoteNumber();
                                saveLinkingInformationOfPaymentWithCN(creditNote, payment, billno);
                                linkedDocuments += creditNoteNo + " ,";

                            }
                            linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
                          //  fromLinkCombo = "Credit Note";
                            fromLinkCombo += "Credit Note (" + linkedDocuments+")" ;
                        }

                        /*
                         * #1 saving linking information in Make Payment &
                         * Purchase Invoice If Payment is made against Invoice
                         */
                        if (jSONArrayAgainstInvoice.length() > 0) {
                            JSONArray drAccArr = jSONArrayAgainstInvoice;
                            linkedDocuments="";
                            for (int i = 0; i < drAccArr.length(); i++) {

                                JSONObject jobj1 = drAccArr.getJSONObject(i);
                                String invoiceid = jobj1.getString("documentid");

                                KwlReturnObject grResult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceid);
                                GoodsReceipt invoice = (GoodsReceipt) grResult.getEntityList().get(0);

                                /*
                                 * Method is used to save linking informatio of
                                 * Make Payment when linking with Purchase
                                 * Invoice
                                 */
                                String invoiceNo = invoice.getGoodsReceiptNumber();
                                saveLinkingInformationOfPaymentWithInvoice(invoice, payment, billno);
                                linkedDocuments += invoiceNo + " ,";
                            }
                            linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
                            if (StringUtil.isNullOrEmpty(fromLinkCombo)) {
                                fromLinkCombo += " Purchase Invoice (" + linkedDocuments + ")";
                            } else {
                                fromLinkCombo += ", Purchase Invoice (" + linkedDocuments + ")";
                            }
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(bankChargesAccid)) {
                        if (editPaymentObject == null) {
                            String jeentryNumber = "";
                            int jeIntegerPart = 0;
                            String jeSeqFormatId = "";

                            HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                            JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                            JEFormatParams.put("modulename", "autojournalentry");
                            JEFormatParams.put("companyid", companyid);
                            JEFormatParams.put("isdefaultFormat", true);

                            KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                            SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, payment.getJournalEntryForBankCharges().getEntryDate());
//                             jeentryNumber = nextAutoNoTemp[0];  //next auto generated number
//                             jeIntegerPart = Integer.parseInt(nextAutoNoTemp[1]);
                            jeSeqFormatId = format.getID();
                            seqNumberMap.put(Constants.DOCUMENTID, payment.getJournalEntryForBankCharges().getID());
                            seqNumberMap.put(Constants.companyKey, companyid);
                            seqNumberMap.put(Constants.SEQUENCEFORMATID, jeSeqFormatId);

                            bankChargeJeNo = accJournalEntryobj.UpdateJournalEntry(seqNumberMap);
                            jeEntryNo += ", " + bankChargeJeNo;
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(bankInterestAccid)) {
                        if (editPaymentObject == null) {
                            String jeentryNumber = "";
                            int jeIntegerPart = 0;
                            String jeSeqFormatId = "";

                            HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                            JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                            JEFormatParams.put("modulename", "autojournalentry");
                            JEFormatParams.put("companyid", companyid);
                            JEFormatParams.put("isdefaultFormat", true);

                            KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                            SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, payment.getJournalEntryForBankInterest().getEntryDate());
//                            jeentryNumber = nextAutoNoTemp[0];  //next auto generated number
//                            jeIntegerPart = Integer.parseInt(nextAutoNoTemp[1]);
                            jeSeqFormatId = format.getID();
                            seqNumberMap.put(Constants.DOCUMENTID, payment.getJournalEntryForBankInterest().getID());
                            seqNumberMap.put(Constants.companyKey, companyid);
                            seqNumberMap.put(Constants.SEQUENCEFORMATID, jeSeqFormatId);
                            bankInterestJeNo = accJournalEntryobj.UpdateJournalEntry(seqNumberMap);
                            jeEntryNo += ", " + bankInterestJeNo;
                        }
                    }
                    /*
                     * If country is Malaysia then post the JE for Import Serice Invoices if such invoices are paid
                     */
                    int paymentCountryId=Integer.parseInt(payment.getCompany().getCountry().getID());
                    if ((paymentCountryId == Constants.malaysian_country_id) && payment.getRows()!=null && !payment.getRows().isEmpty()) {
                        postJEforImportServiceInvoices(payment,paramJobj);
                    }
                    txnManager.commit(AutoNoPayStatus);
                    
                    //Saving after the commit
                    if (paramJobj.optBoolean(Constants.isdefaultHeaderMap)) {
                        /*
                         * saving linking information in Make Payment & Credit
                         * Note If Payment is made with Credit Note
                         */
                        if (jSONArrayCNDN.length() > 0) {
                            JSONArray drAccArr = jSONArrayCNDN;
                            
                            for (int i = 0; i < drAccArr.length(); i++) {

                                JSONObject jobj1 = drAccArr.getJSONObject(i);
                                String cnnoteid = jobj1.getString("documentid");

                                KwlReturnObject cnResult = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnnoteid);
                                CreditNote creditNote = (CreditNote) cnResult.getEntityList().get(0);

                                /*
                                 * Method is used to save linking informatio of
                                 * Make Payment * when linking with Credit Note
                                 */
                                String creditNoteNo = creditNote.getCreditNoteNumber();
                                saveLinkingInformationOfPaymentWithCN(creditNote, payment, billno);
                                linkedDocuments += creditNoteNo + " ,";

                            }
                            linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
                            fromLinkCombo += "Credit Note (" +linkedDocuments+")" ;
                        }

                        /*
                         * #1 saving linking information in Make Payment & 
                         * Purchase Invoice If Payment is made against Invoice
                         */
                        if (jSONArrayAgainstInvoice.length() > 0) {
                            JSONArray drAccArr = jSONArrayAgainstInvoice;
                            linkedDocuments="";
                            for (int i = 0; i < drAccArr.length(); i++) {

                                JSONObject jobj1 = drAccArr.getJSONObject(i);
                                String invoiceid = jobj1.getString("documentid");

                                KwlReturnObject grResult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceid);
                                GoodsReceipt invoice = (GoodsReceipt) grResult.getEntityList().get(0);

                                /*
                                 * Method is used to save linking informatio of
                                 * Make Payment when linking with Purchase
                                 * Invoice
                                 */
                                String invoiceNo = invoice.getGoodsReceiptNumber();
                                saveLinkingInformationOfPaymentWithInvoice(invoice, payment, billno);
                                linkedDocuments += invoiceNo + " ,";
                            }
                            linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
                            if (StringUtil.isNullOrEmpty(fromLinkCombo)) {
                                fromLinkCombo += "Purchase Invoice (" + linkedDocuments + ")";
                            } else {
                                fromLinkCombo += ", Purchase Invoice (" + linkedDocuments + ")";
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                if (AutoNoPayStatus != null) {
                    txnManager.rollback(AutoNoPayStatus);
                }
                deleteTemporaryInvoicesEntries(jSONArrayAgainstInvoice, companyid);
                accPaymentDAOobj.deleteUsedInvoiceOrCheque(chequeNumber, companyid);
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Make_Payment_ModuleId);
                Logger.getLogger(accVendorPaymentControllerNew.class.getName()).log(Level.SEVERE, null, ex);
            }
            // End of Print cheque
            
            //========Rounding JE Related Code Start================
            if (jSONArrayAgainstInvoice.length() > 0) {
                try {
                    status = txnManager.getTransaction(def);
                    JSONArray drAccArr = jSONArrayAgainstInvoice;
                    for (int i = 0; i < drAccArr.length(); i++) {
                        JSONObject jobj1 = drAccArr.getJSONObject(i);
                        String invoiceid = jobj1.getString("documentid");
                        KwlReturnObject grResult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceid);
                        GoodsReceipt invoice = (GoodsReceipt) grResult.getEntityList().get(0);
                        paramJobj.put("goodsReceiptObj", invoice);
                        paramJobj.put("isEdit", isEdit);
                        paramJobj.put("paymentnumber", billno);
                        postRoundingJEOnPaymentSave(paramJobj);
                    }
                    txnManager.commit(status);
                } catch (JSONException | ServiceException | SessionExpiredException | AccountingException ex) {
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    Logger.getLogger(accVendorPaymentControllerNew.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            //========Rounding JE Related Code End================
            
            
            /*
             * Edit Case - 1. Deleted Old PayDetails - Delete Old Cheque and
             * Card details
             */
            /*
             * Preparing Audit trial message if document is linking at the time
             * of creating
             */
            String linkingMessages = "";
            if (!StringUtil.isNullOrEmpty(fromLinkCombo)) {
//                linkingMessages = " by Linking to " + fromLinkCombo + " " + linkedDocuments;
                linkingMessages = " for " + fromLinkCombo ;
            }
            String paymentNumber=payment.getPaymentNumber();
            jobj.put("paymentno", paymentNumber);
            /*
             * Inserting Entry in Audit trial when any document is unlinking
             * through Edit
             */
            Map<String, Object> auditRequestParams = new HashMap<>();
            // auditRequestParams.put(auditSMS, status);
            auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
            if (isChequePrint) {
                /*
                Add as payment number was getting added blank ERP-33390
                */
                auditTrailObj.insertAuditLog(AuditAction.MAKE_PAYMENT, "User " + paramJobj.optString(Constants.userfullname) + " has printed a cheque " + chqno + " for " + accNameForAudit + " in payment " + billno, auditRequestParams, paymentid);
            }
            if (approvalStatusLevel != 11) {
                String pendingforApproval = " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
                auditTrailObj.insertAuditLog("75", "User " + paramJobj.optString(Constants.userfullname) + " has " + action + " a Payment " + payment.getPaymentNumber() + linkingMessages + person + AuditMsg + pendingforApproval, auditRequestParams, payment.getID());
            } else {
                auditTrailObj.insertAuditLog("75", "User " + paramJobj.optString(Constants.userfullname) + " has " + action + " a Payment " + payment.getPaymentNumber() + linkingMessages + person + AuditMsg, auditRequestParams, payment.getID());
            }
            if (approvalStatusLevel != 11) {//pending for approval case
                String paymentNoteSaved = messageSource.getMessage("acc.payvoucher.save", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
                String butPendingForApproval = " " + messageSource.getMessage("acc.field.butpendingforApproval", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
                msg = paymentNoteSaved + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + billno + "</b>.";
            } else {
                msg = messageSource.getMessage("acc.payvoucher.save", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + billno + ",</b> " + messageSource.getMessage("acc.field.JENo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + JENumBer + "" + jeEntryNo + "</b>";   //"Payment information has been saved successfully";
            }
            jobj.put("pendingApproval", approvalStatusLevel != 11 );
            bno = new StringBuilder(billno);
            
            
   /* -- -----------Doing here Audit trail Entry & Preparing messages while Editing Pending Payment --------------*/
            if (!remainAtSameLevel) {
                
                if (isEditedPendingDocument) {

                    if (sendPendingDocumentsToNextLevel) {

                        if (isAuthorityToApprove) {

                            msg += "<br>";
                            // Audit log entry
                            //String action = "Make Payment ";
                            String auditaction = AuditAction.MAKE_PAYMENT_APPROVED;
                            auditTrailObj.insertAuditLog(auditaction, "User " + userfullName + " has Approved a " + action + payment.getPaymentNumber() + JENumber + " at Level-" + levelOnWhichDocumentIsApproved, auditRequestParams, payment.getID());
                            msg += "Make Payment has been approved successfully " + " by " + userfullName + " at Level " + levelOnWhichDocumentIsApproved + "." + JEMsg;
                        } else if (!isAuthorityToApprove) {//If user is unauthorized to aprrove
                            msg += messageSource.getMessage("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + levelOnWhichDocumentIsApproved + " and record will be available at this level for approval" + ".";
                        }
                    } else if (!isAuthorityToApprove) {//If user is unauthorized to aprrove
                        msg += messageSource.getMessage("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + levelOnWhichDocumentIsApproved + " and record will be available at this level for approval" + ".";
                    }

                }
            } else if (isEditedPendingDocument) {
                msg += "<br>";
                msg += "But remains in pending for approval";
            }
             
        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            deleteTemporaryInvoicesEntries(jSONArrayAgainstInvoice, companyid);
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Make_Payment_ModuleId);
                accPaymentDAOobj.deleteUsedInvoiceOrCheque(chequeNumber, companyid);

            } catch (Exception ee) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ee);
            }
            msg = ex.getMessage();
            issuccess = false;
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException AE) {
            if (status != null && !exceptionChequeOrInv) {
                txnManager.rollback(status);
            }
            deleteTemporaryInvoicesEntries(jSONArrayAgainstInvoice, companyid);
            try {
                status = txnManager.getTransaction(def);
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Make_Payment_ModuleId);
                txnManager.commit(status);
                accPaymentDAOobj.deleteUsedInvoiceOrCheque(chequeNumber, companyid);

            } catch (Exception ee) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ee);
            }
            msg = "" + AE.getMessage();
            if (AE.getMessage() == null) {
                msg = AE.getCause().getMessage();
            }
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, AE);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            deleteTemporaryInvoicesEntries(jSONArrayAgainstInvoice, companyid);
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Make_Payment_ModuleId);
                accPaymentDAOobj.deleteUsedInvoiceOrCheque(chequeNumber, companyid);

            } catch (Exception ee) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("paymentid", paymentid);
                jobj.put("repeatedid", repeatedid);
                jobj.put("intervalUnit", intervalUnit);
                jobj.put("intervalType", intervalType);
                jobj.put("noOfInvPost", noOfInvPost);
                jobj.put("noOfInvRemainPost", noOfInvRemainPost);
                jobj.put("sdate", sdate);
                jobj.put("nextdate", nextdate);
                jobj.put("expdate", expdate);
                jobj.put("data", jArr);
                jobj.put("isAccountingExe", accexception);
                jobj.put("billno", billno.lastIndexOf(",") >= 0 ? bno.deleteCharAt(bno.lastIndexOf(",")) : "");
                jobj.put("amount", amountpayment);
                jobj.put("address", accountaddress);
                jobj.put("accountName", accountName);
                jobj.put("billingEmail", vendorEmailId);                
                hashMap.put("jobj", jobj);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return hashMap;
    }
    
    
    
    /*--------Funtion is called while Editing Pending Payment --------- */
    public List approveRelevantDocumentAttachedToMakePayment(HashMap approveJeMap) throws ServiceException, AccountingException, JSONException, SessionExpiredException, ParseException {

        List approveJeMapList = new ArrayList();
        Payment payment = null;
        Company companyObj = null;
        String postingDateStr = "";
        double amount = 0;

        String jeIdForBankCharges = "";
        String jeIdForbankInterst = "";
        String jeIdForImportService = "";
        boolean isEditToApprove = false;//For now we have kept it false but check in approval case its value

        Map<String, Object> requestParams = new HashMap<String, Object>();

        if (approveJeMap.containsKey("payment") && approveJeMap.get("payment") != null) {
            payment = (Payment) approveJeMap.get("payment");
        }

        if (approveJeMap.containsKey("company") && approveJeMap.get("company") != null) {
            companyObj = (Company) approveJeMap.get("company");
        }

        if (approveJeMap.containsKey("postingDateStr") && approveJeMap.get("postingDateStr") != null) {
            postingDateStr = (String) approveJeMap.get("postingDateStr");
        }

        if (approveJeMap.containsKey("requestParams") && approveJeMap.get("requestParams") != null) {
            requestParams = (Map<String, Object>) approveJeMap.get("requestParams");
        }

        if (approveJeMap.containsKey("totalAmount") && approveJeMap.get("totalAmount") != null) {
            amount = (double) approveJeMap.get("totalAmount");
        }

        String companyid = companyObj.getCompanyID();
        String jeID = payment.getJournalEntry().getID();

        String JENumber = "";
        String JEMsg = "";

        DateFormat df = authHandler.getDateOnlyFormat();
        Date postingDate = null;
        if (!StringUtil.isNullOrEmpty(postingDateStr)) {
            postingDate = df.parse(postingDateStr);
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", companyid);
        Object exPrefObject = kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"columnPref"}, paramMap);
        JSONObject jObj = StringUtil.isNullObject(exPrefObject) ? new JSONObject() : new JSONObject(exPrefObject.toString());
        boolean isPostingDateCheck = false;
        if (!StringUtil.isNullObject(jObj) && jObj.has(Constants.IS_POSTING_DATE_CHECK) && jObj.get(Constants.IS_POSTING_DATE_CHECK) != null && jObj.optBoolean(Constants.IS_POSTING_DATE_CHECK, false)) {
            isPostingDateCheck = true;
        }

        requestParams.put("postingDate", postingDateStr);

        if (payment.getApprovestatuslevel() == 11) {//when final 
            if (StringUtil.isNullOrEmpty(payment.getJournalEntry().getEntryNumber())) {
                int isApproved = 0;
                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                JEFormatParams.put("companyid", companyid);
                JEFormatParams.put("isdefaultFormat", true);
                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                journalEntryModuleServiceobj.updateJEEntryNumberForNewJE(requestParams, payment.getJournalEntry(), companyid, format.getID(), isApproved);//jeDataMap
            } else {
                JSONObject jeJobj = new JSONObject();
                HashSet<JournalEntryDetail> details = new HashSet<JournalEntryDetail>();
                jeJobj.put("jeid", jeID);
                jeJobj.put("comapnyid", companyid);
                jeJobj.put("pendingapproval", 0);
                if (isPostingDateCheck && postingDate != null) {
                    jeJobj.put("entrydate", postingDate);
                }
                accJournalEntryobj.updateJournalEntry(jeJobj, details);
            }
            JENumber = " with JE No. " + payment.getJournalEntry().getEntryNumber();
            JEMsg = "<br/>" + "JE No : <b>" + payment.getJournalEntry().getEntryNumber() + "</b>";
        }
        if (payment.getJournalEntryForBankCharges() != null) {
            jeIdForBankCharges = payment.getJournalEntryForBankCharges().getID();
            JSONObject jeJobj = new JSONObject();
            HashSet<JournalEntryDetail> details = new HashSet<JournalEntryDetail>();
            jeJobj.put("jeid", jeIdForBankCharges);
            jeJobj.put("comapnyid", companyid);
            jeJobj.put("pendingapproval", 0);
            accJournalEntryobj.updateJournalEntry(jeJobj, details);
        }
        if (payment.getJournalEntryForBankInterest() != null) {
            //Update JE for Bank Interst
            jeIdForbankInterst = payment.getJournalEntryForBankInterest().getID();
            JSONObject jeJobj = new JSONObject();
            HashSet<JournalEntryDetail> details = new HashSet<JournalEntryDetail>();
            jeJobj.put("jeid", jeIdForbankInterst);
            jeJobj.put("comapnyid", companyid);
            jeJobj.put("pendingapproval", 0);
            accJournalEntryobj.updateJournalEntry(jeJobj, details);
        }
        /*
         * Updating Import Service JE
         */
        if (payment.getImportServiceJE() != null) {
            //Update JE for Import Service Invoices
            jeIdForImportService = payment.getImportServiceJE().getID();
            JSONObject jeJobj = new JSONObject();
            HashSet<JournalEntryDetail> details = new HashSet<JournalEntryDetail>();
            jeJobj.put("jeid", jeIdForImportService);
            jeJobj.put("comapnyid", companyid);
            jeJobj.put("pendingapproval", 0);
            accJournalEntryobj.updateJournalEntry(jeJobj, details);
        }

   
        if (!isEditToApprove && payment.getApprovestatuslevel() == Constants.APPROVED_STATUS_LEVEL) {              //deducting amount due at final level
            for (AdvanceDetail detail : payment.getAdvanceDetails()) {
                ReceiptAdvanceDetail receiptAdvanceDetail = detail.getReceiptAdvanceDetails();
                if (receiptAdvanceDetail != null) {
                    receiptAdvanceDetail.setAmountDue(receiptAdvanceDetail.getAmountDue() - (amount / detail.getExchangeratefortransaction()));         //converting to receipt currency
//                        receiptAdvanceDetail.setAmountDue(receiptAdvanceDetail.getAmountDue() - amount);
                }
            }
        }

        approveJeMapList.add(JENumber);
        approveJeMapList.add(JEMsg);

        return approveJeMapList;

    }
    
    
       
      /*-------Function to send approval mail if check "Allow Sending Approval Mail" is activated from system preferences---------*/
    public void sendApprovalMailIfAllowedFromSystemPreferences(HashMap emailMap) throws ServiceException {

        String userName = "";
        Company company = null;
        Payment payment = null;
        String baseUrl = "";
        CompanyAccountPreferences preferences = null;
        int level =0;
        String amountinbase=  "";

        if (emailMap.containsKey("userName") && emailMap.get("userName") != null) {
            userName = (String) emailMap.get("userName");
        }
        if (emailMap.containsKey("company") && emailMap.get("company") != null) {
            company = (Company) emailMap.get("company");
        }

        if (emailMap.containsKey("payment") && emailMap.get("payment") != null) {
            payment = (Payment) emailMap.get("payment");
        }

        if (emailMap.containsKey("baseUrl") && emailMap.get("baseUrl") != null) {
            baseUrl = (String) emailMap.get("baseUrl");
        }

        if (emailMap.containsKey("preferences") && emailMap.get("preferences") != null) {
            preferences = (CompanyAccountPreferences) emailMap.get("preferences");
        }
        
         if (emailMap.containsKey("level") && emailMap.get("level") != null) {
            level = (Integer) emailMap.get("level");
        }
         if (emailMap.containsKey("totalAmount") && emailMap.get("totalAmount") != null) {
               amountinbase=emailMap.get("totalAmount").toString();  
             }
       String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                String creatormail = company.getCreator().getEmailID();
                String fname = company.getCreator().getFirstName() == null ? "" : company.getCreator().getFirstName();
                String lname = company.getCreator().getLastName() == null ? "" : company.getCreator().getLastName();
                String creatorname = fname + " " + lname;
                String documentcreatoremail = (payment != null && payment.getCreatedby() != null) ? payment.getCreatedby().getEmailID() : "";
                String approvalpendingStatusmsg = "";
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                ArrayList<String> emailArray = new ArrayList<>();
                qdDataMap.put(Constants.companyKey, company.getCompanyID());
                qdDataMap.put("level", level);
                qdDataMap.put(Constants.moduleid,Constants.Acc_Make_Payment_ModuleId);
//                emailArray =commonFnControllerService.getUserApprovalEmail(qdDataMap);

                        emailArray.add(creatormail);
                        String[] emails ={};
                if (!StringUtil.isNullOrEmpty(documentcreatoremail) && !creatormail.equalsIgnoreCase(documentcreatoremail)) {
                    emailArray.add(documentcreatoremail);
                }
                        emails = emailArray.toArray(emails);
//                String[] emails = {creatormail};
                if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                    String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                    emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
                }
                if (level < 11) {
                    qdDataMap.put("totalAmount", amountinbase);
                approvalpendingStatusmsg=commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                }
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put("Number", payment.getPaymentNumber());
                mailParameters.put("userName", userName);
                mailParameters.put("emails", emails);
                mailParameters.put("moduleName", Constants.MAKE_PAYMENT);
                mailParameters.put("sendorInfo", sendorInfo);
                mailParameters.put("addresseeName", "All");
                mailParameters.put("companyid", company.getCompanyID());
                mailParameters.put("baseUrl", baseUrl);
                mailParameters.put("approvalstatuslevel", level);
                mailParameters.put("approvalpendingStatusmsg", approvalpendingStatusmsg);

                if (emails.length > 0) {
                    accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
                }
    }
    
    
    
    
     /*---Function to save approval history , If any document is approved at some level------   */
    public void saveApprovalHistory(HashMap approvalHistoryMap) throws ServiceException {

  
        Payment payment = null;
        Company companyObj = null;
        String userid = "";

        if (approvalHistoryMap.containsKey("payment") && approvalHistoryMap.get("payment") != null) {
            payment = (Payment) approvalHistoryMap.get("payment");
        }

        if (approvalHistoryMap.containsKey("company") && approvalHistoryMap.get("company") != null) {
            companyObj = (Company) approvalHistoryMap.get("company");
        }

        if (approvalHistoryMap.containsKey("userid") && approvalHistoryMap.get("userid") != null) {
            userid = (String) approvalHistoryMap.get("userid");
        }

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("transtype", Constants.MAKE_PAYMENT_APPROVAL);
        hashMap.put("transid", payment.getID());
        hashMap.put("approvallevel", payment.getApprovestatuslevel());//  If approvedLevel = 11 then its final Approval
        hashMap.put("remark", "");
        hashMap.put("userid", userid);
        hashMap.put("companyid", companyObj.getCompanyID());
        accountingHandlerDAOobj.updateApprovalHistory(hashMap);


    }
    
    /*---------- Function to create Bulk payment against different vendors--------------*/
    @Override
    public HashMap<String, Object> saveBulkVendorPayment(JSONObject paramJobj) {

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        JSONObject jobj = new JSONObject();
        String msg = "";
        String paymentNumber = "";
        String pendingpaymentNumber = "";
        String journalEntryNumber = "";
        String chequeNumber = "";
        String sequenceFormat = "";
        boolean isSuccess = true;
        boolean exceptionChequeOrInv = false;
        String entryNumber = paramJobj.optString("no");
        boolean isInvalidChequeNumber = false;
        boolean isSequenceFormatNA = false;

        HashMap<String, JSONArray> jsonArrayAgainstInvoice = new HashMap<String, JSONArray>();
        JSONArray invoicesAgainstParticularVendor = new JSONArray();
        JSONArray chequeJArr = new JSONArray();

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        String companyid = paramJobj.optString(Constants.companyKey);
        try {
            Payment editPaymentObject = null;
            boolean isEdit = false;
            int approvalStatusLevel = 11;
            String sequenceformat = paramJobj.optString("sequenceformat") != null ? paramJobj.optString("sequenceformat") : "NA";

            String detailsJsonString = paramJobj.optString("Details");
            JSONArray jsonArray = new JSONArray(detailsJsonString);
            JSONArray jSONArrayCNDN = new JSONArray();

            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;

            boolean isDiscountAppliedOnPaymentTerms = false;
            JSONObject jObj = new JSONObject();
            if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                if (jObj.has(Constants.DISCOUNT_ON_PAYMENT_TERMS) && jObj.get(Constants.DISCOUNT_ON_PAYMENT_TERMS) != null && jObj.optBoolean(Constants.DISCOUNT_ON_PAYMENT_TERMS, false)) {
                    isDiscountAppliedOnPaymentTerms = true;
                }
            }
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            /* --------- Preparing a Map on key vendor & data jsonarray to store all data of a particular vendor-----------*/
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jSONObject = jsonArray.getJSONObject(i);
                sequenceFormat = jSONObject.optString("sequenceformat", "");
                String vendorid = jSONObject.getString("accid");
                /**
                 * handle for below case:SDP-12577
                 * if two vendors having saving same cheque number then need to save two payments. 
                 */
                /* Problem will b raised when combination of NA & valid sequence Format*/
                if (sequenceFormat.equalsIgnoreCase("NA")) {
                    isSequenceFormatNA = true;
                    chequeNumber = jSONObject.optString("chequenumber", "");
                    String ven_chequeKey = chequeNumber + vendorid;
                    if (!jsonArrayAgainstInvoice.containsKey(ven_chequeKey)) {
                        jsonArrayAgainstInvoice.put(ven_chequeKey, new JSONArray());
                    }
                    jsonArrayAgainstInvoice.get(ven_chequeKey).put(jSONObject);

                } else {

                    if (!jsonArrayAgainstInvoice.containsKey(vendorid)) {
                        jsonArrayAgainstInvoice.put(vendorid, new JSONArray());
                    }
                    jsonArrayAgainstInvoice.get(vendorid).put(jSONObject);
                }

            }

            /*--------------- Looping for checking Invalid cheque Number if user enter manually---------- */
            if (isSequenceFormatNA) {
                try {
                    for (String personIdKey : jsonArrayAgainstInvoice.keySet()) {

                        invoicesAgainstParticularVendor = jsonArrayAgainstInvoice.get(personIdKey);
                        JSONObject invoiceJobj = new JSONObject();
                        String invoiceId = "";
                        String payDetail = "";
                        for (int i = 0; i < invoicesAgainstParticularVendor.length(); i++) {
                            invoiceJobj = invoicesAgainstParticularVendor.getJSONObject(i);
                            chequeNumber = invoiceJobj.optString("chequenumber");
                            payDetail = invoiceJobj.toString();

                        }

                        paramJobj.put("paydetail", payDetail);

                        KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

                        if (preferences.getChequeNoDuplicate() != Constants.ChequeNoIgnore) {// Ignore case 
                            checkForInvalidChequeNumber(paramJobj, editPaymentObject);                            
                        }
                        paramJobj.put("chequecduplicatepref",preferences.getChequeNoDuplicate());   // ERP-39302
                    }
                } catch (Exception ex) {

                    isSuccess = false;
                    isInvalidChequeNumber = true;
                    throw new AccountingException(ex.getMessage(), ex);
                }
            }
            int nextAutoNoIntpartForPM= 0;//int part of sequence number for Payment.
            int nextAutoNoIntpartforJE= 0;//int part of sequence number for JE.
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
            Map<String, Object> seqNumberMapJE = new HashMap<String, Object>();
            Map<String, Object> seqNumberMapPM = new HashMap<String, Object>();
            
            /* ----Looping on key Vendor
            
             Saving data ony by one against a Vendor
             ------------- */
            /**
             * moved transaction outside of loop to save bulk payment
             * at a time and if exception will occur it will reject the whole bulk trasanction
             **/
              status = txnManager.getTransaction(def);
                for (String personIdKey : jsonArrayAgainstInvoice.keySet()) {

                    invoicesAgainstParticularVendor = jsonArrayAgainstInvoice.get(personIdKey);
                 String linkingDocumentNumber=""; 

                    try {
                        String invoiceId = "";
                        String payDetail = "";

                        JSONObject invoiceJobj = new JSONObject();

                        synchronized (this) {
                            KwlReturnObject resultPay = accCommonTablesDAO.getTransactionInTemp(entryNumber, companyid, Constants.Acc_Make_Payment_ModuleId);
                            if (resultPay.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                                //accexception = true;
                                throw new AccountingException(messageSource.getMessage("acc.PO.selectedPamentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                            } else {
                                accCommonTablesDAO.insertTransactionInTemp(entryNumber, companyid, Constants.Acc_Make_Payment_ModuleId);
                            }
                            for (int i = 0; i < invoicesAgainstParticularVendor.length(); i++) {
                                invoiceJobj = invoicesAgainstParticularVendor.getJSONObject(i);
                                invoiceJobj.put("postdate", paramJobj.optString("postdate"));
                                paramJobj.put("accid", invoiceJobj.get("accid")); //vendorid
                                chequeNumber = invoiceJobj.optString("chequenumber");
                                invoiceId = invoiceJobj.getString("documentid");
                                payDetail = invoiceJobj.toString();
                            linkingDocumentNumber +=invoiceJobj.getString("documentno")+",";
                                KwlReturnObject resultInv1 = accPaymentDAOobj.getInvoiceInTemp(invoiceId, companyid, Constants.Acc_Invoice_ModuleId);
                                if (resultInv1.getRecordTotalCount() > 0) {
                                    throw new AccountingException("Selected invoice is already in process, please try after sometime.");
                                } else {
                                    accPaymentDAOobj.insertInvoiceOrCheque(invoiceId, companyid, Constants.Acc_Invoice_ModuleId, "");//pass bank id
                                }
                            }

                            paramJobj.put("paydetail", payDetail);


                            if (!StringUtil.isNullOrEmpty(chequeNumber)) {

                                String methodid = paramJobj.optString("pmtmethod");
                                KwlReturnObject result1 = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), methodid);
                                PaymentMethod payMethod = (PaymentMethod) result1.getEntityList().get(0);
                        if (StringUtil.isNullObject(payMethod)) {
                            throw new AccountingException(messageSource.getMessage("acc.field.vendorpayment.paymentmethod.doesnt.exists", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                        }
                                String bankId = payMethod.getAccount().getID();

                                KwlReturnObject resultInv = accPaymentDAOobj.getSearchChequeNoTemp(chequeNumber, companyid, Constants.Cheque_ModuleId, bankId);
                                if (resultInv.getRecordTotalCount() > 0) {
                                    throw new AccountingException("Cheque Number : <b>" + chequeNumber + "</b> is already exist, Please enter different one");
                                } else {
                                    accPaymentDAOobj.insertInvoiceOrCheque(chequeNumber, companyid, Constants.Cheque_ModuleId, bankId);
                                }
                            }
//                    accPaymentDAOobj.updateChequeNoInTemp(chequeNumber, companyid,invoiceId);
                        }
                    } catch (Exception ex) {
                        exceptionChequeOrInv = true;
                        isSuccess = false;
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                        throw new AccountingException(ex.getMessage(), ex);
                    }


                    /*--------- Code to check whether selected invoices have already amount due zero then selected invoices have been alraedy used to create Bulk Payment 
               
                     ------So reload invoice report------ */
                    if (!isEdit) {
                        for (int i = 0; i < invoicesAgainstParticularVendor.length(); i++) {
                            JSONObject invoiceJobj = invoicesAgainstParticularVendor.getJSONObject(i);
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceJobj.getString("documentid"));
                            GoodsReceipt invoice = (GoodsReceipt) objItr.getEntityList().get(0);
                            if (invoice != null) {
                                double invoiceAmoutDue = 0;
                                boolean isInvoiceIsClaimed = (invoice.getBadDebtType() == Constants.Invoice_Claimed || invoice.getBadDebtType() == Constants.Invoice_Recovered);
                                if (isInvoiceIsClaimed) {
                                    invoiceAmoutDue = invoice.getClaimAmountDue();
                                } else {
                                    if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                                        invoiceAmoutDue = invoice.getOpeningBalanceAmountDue();
                                    } else {
                                        invoiceAmoutDue = invoice.getInvoiceamountdue();
                                    }
                                }
                                double amountReceived = invoiceJobj.getDouble("enteramount");           //amount of Invoice
                                double adjustedRate = 1;
                                if (!StringUtil.isNullOrEmpty(invoiceJobj.optString("exchangeratefortransaction", "").toString())) {
                                    adjustedRate = Double.parseDouble(invoiceJobj.get("exchangeratefortransaction").toString());
                                }
                                double paymentAmountReceivedTemp = invoiceAmoutDue * adjustedRate;
                                paymentAmountReceivedTemp = authHandler.round(paymentAmountReceivedTemp, companyid);
                                boolean isFullPayment = (paymentAmountReceivedTemp == amountReceived);
                                double discountAmount = invoiceJobj.optDouble("discountname", 0.0);
                                discountAmount = authHandler.round(discountAmount, companyid);
                                double discountAmountConverted = discountAmount;
                                if (isDiscountAppliedOnPaymentTerms && discountAmount > 0.0) {
                                    isFullPayment = paymentAmountReceivedTemp == (amountReceived + discountAmount);
                                }
                                if (!isFullPayment) {                                               // Changes made for ERP-16382
                                    double amountReceivedConverted = invoiceJobj.getDouble("enteramount");
                                    double totalAmtReceivedAndDiscount = 0d;
                                    if (!StringUtil.isNullOrEmpty(invoiceJobj.optString("exchangeratefortransaction", "").toString())) {
                                        adjustedRate = Double.parseDouble(invoiceJobj.get("exchangeratefortransaction").toString());
                                        amountReceivedConverted = amountReceived / adjustedRate;
                                        discountAmountConverted = discountAmount / adjustedRate;
                                        totalAmtReceivedAndDiscount = authHandler.round((amountReceivedConverted + discountAmountConverted), companyid);
                                        amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                                    } else {
                                        amountReceivedConverted = authHandler.round(amountReceived, companyid);
                                    totalAmtReceivedAndDiscount = authHandler.round((amountReceived+discountAmount),companyid);
                                    }
                                    amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                                    invoiceAmoutDue = authHandler.round(invoiceAmoutDue, companyid);
                                    if (invoiceAmoutDue < totalAmtReceivedAndDiscount) {
                                        throw new AccountingException("Amount entered for invoice cannot be greater than it's amount due. Please reload the invoices.");
                                    }
                                }
                            }
                        }
                    }

                    /*
                     * Create Payment Object
                     */
                    Payment payment = createPaymentObject(paramJobj, editPaymentObject);
                    List mailParams = Collections.EMPTY_LIST;
                    
                    double totalAmount = 0;
                    /*
                    add to Calculate correct  total amount for particular vendor
                    */
                    for (int i = 0; i < invoicesAgainstParticularVendor.length(); i++) {
                        if (!invoicesAgainstParticularVendor.getJSONObject(i).optString("enteramount").equalsIgnoreCase("")) {
                            totalAmount += Double.parseDouble(invoicesAgainstParticularVendor.getJSONObject(i).optString("enteramount"));
                        }
                    }
//                     totalAmount = Double.parseDouble(paramJobj.optString("amount"));
                    HashMap<String, Object> paymentApproveMap = new HashMap<String, Object>();
                    List approvedlevel = null;
                    paymentApproveMap.put("companyid", companyid);
                    paymentApproveMap.put("level", 0);//Initialy it will be 0
                    paymentApproveMap.put("totalAmount", String.valueOf(authHandler.round(totalAmount, companyid)));
                    paymentApproveMap.put("currentUser", payment.getCreatedby());
                    paymentApproveMap.put("fromCreate", true);
                    paymentApproveMap.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                    boolean isMailApplicable = false;
                    List list = new ArrayList();
                    list.add(payment.getID());

                    /* Approval code */
                    approvedlevel = paymentService.approveMakePayment(list, paymentApproveMap, isMailApplicable);
                    approvalStatusLevel = (Integer) approvedlevel.get(0);
                    mailParams = (List) approvedlevel.get(1);
                    payment.setApprovestatuslevel(approvalStatusLevel);

                    /*
                     * Create Journal Entry Object
                     */
                    JournalEntry journalEntry = journalEntryObject(paramJobj, editPaymentObject);

                    if (approvalStatusLevel == 11) {
                        journalEntry.setPendingapproval(0);
                    } else {
                        journalEntry.setPendingapproval(1);
                    }

                    payment.setJournalEntry(journalEntry);
                    List<Payment> payments = new ArrayList<Payment>();
                    payments.add(payment);

                    /*
                     * Save Journal Entry Object
                     */
                    accJournalEntryobj.saveJournalEntryByObject(journalEntry);

                    /* Custom Field Code */
                    if (!StringUtil.isNullOrEmpty(paramJobj.optString("customfield"))) {
                        JSONArray jcustomarray = new JSONArray(paramJobj.optString("customfield"));

                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                        customrequestParams.put("modulerecid", journalEntry.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                        customrequestParams.put("companyid", companyid);
                        customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            KwlReturnObject receiptAccJECustomData = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), journalEntry.getID());
                            AccJECustomData accJECustomData = (AccJECustomData) receiptAccJECustomData.getEntityList().get(0);
                            journalEntry.setAccBillInvCustomData(accJECustomData);
                        }
                    }

                    Set<JournalEntryDetail> details = null;

                    //Save Payment Object
                    accVendorPaymentobj.savePaymentObject(payments);
                    String paymentID = payments.get(0).getID();

                    /*
                     * Adding Details
                     */

                    /*
                     * Invoice Details Object
                     */
                    paramJobj.put("isDiscountAppliedOnPaymentTerms", isDiscountAppliedOnPaymentTerms);
                    if (invoicesAgainstParticularVendor.length() > 0) {
                        if (details == null) {
                            details = journalEntryDetailObject(paramJobj, invoicesAgainstParticularVendor, journalEntry, payment, Constants.PaymentAgainstInvoice);
                        } else {
                            details.addAll(journalEntryDetailObject(paramJobj, invoicesAgainstParticularVendor, journalEntry, payment, Constants.PaymentAgainstInvoice));
                        }
                        //Save Invoice Details selected in payment
                        HashSet paymentDetails = paymentDetailObject(paramJobj, invoicesAgainstParticularVendor, payment, Constants.PaymentAgainstInvoice);
                        payment.setRows(paymentDetails);
                    }
                    double amount = 0;
                    for (int i = 0; i < invoicesAgainstParticularVendor.length(); i++) {
                        amount += Double.parseDouble((String) invoicesAgainstParticularVendor.getJSONObject(i).get("enteramount"));
                    }
                    paramJobj.put("amount", amount);
                    details.addAll(journalEntryDetailCommonObjects(paramJobj, jSONArrayCNDN, journalEntry, payment, Constants.PaymentAgainstCNDN, editPaymentObject, null, null));

                    // Save JEDetail Object
                    journalEntry.setDetails(details);
                    journalEntry.setTransactionId(paymentID);
                    journalEntry.setTransactionModuleid(Constants.Acc_Make_Payment_ModuleId);
                    accJournalEntryobj.saveJournalEntryDetailsSet(details);

                    /*
                     * Create PayDetail Object for - 1. Cheque Details
                     */
                    PayDetail payDetail = getPayDetailObject(paramJobj, editPaymentObject, payment);
                    payment.setPayDetail(payDetail);

                    boolean isChequePrint = false;
                JSONObject jobjDetails=new JSONObject();
                JSONObject extraParams=new JSONObject();
                    if (!StringUtil.isNullOrEmpty(paramJobj.optString("isChequePrint"))) {
                        isChequePrint = Boolean.parseBoolean(paramJobj.optString("isChequePrint"));
                    }

                    if (isChequePrint) {
                        // Get Vendor or Customer Name/ Paid To value
                        String payee = payment.getPayee();
                        if (payment.getPaymentWindowType() == Constants.Make_Payment_to_Vendor && StringUtil.isNullOrEmpty(payee)) { // Against Vendor
                            payee = payment.getVendor().getName();
                            payee = StringUtil.DecodeText(payee);
                        } else if (payment.getPaymentWindowType() == Constants.Make_Payment_to_Customer && StringUtil.isNullOrEmpty(payee)) {  // Against Customer
                            payee = payment.getCustomer();
                            KwlReturnObject resultCVAccount = accountingHandlerDAOobj.getObject(Customer.class.getName(), payee);
                            Customer customer = (Customer) resultCVAccount.getEntityList().get(0);
                            if (customer != null) {
                                payee = StringUtil.DecodeText(customer.getName());
                            }
                        } else {  // Against GL
                            if (payment.getPaidTo() != null && StringUtil.isNullOrEmpty(payee)) {
                                payee = payment.getPaidTo().getValue();
                            }
                        }
                    extraParams.put("netinword","");
                    extraParams.put("countryLanguageId",countryLanguageId);
                    extraParams.put("accountaddress","");
                    extraParams.put("accountName","");
                    extraParams.put("payee",payee);
                    extraParams.put("billingAddress","");
                    extraParams.put("isEdit",isEdit);
                    jobjDetails=getChequeDetialsJSON(paramJobj, payment,hashMap, extraParams);
                        chequeJArr.put(jobjDetails);
                    }
                    /* Deleting temporary entries from here*/
                    deleteTemporaryInvoicesEntries(invoicesAgainstParticularVendor, companyid);
                    accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Make_Payment_ModuleId);
                    accPaymentDAOobj.deleteUsedInvoiceOrCheque(chequeNumber, companyid);


                    /* ----------  Updating JE no & Payment NO---- */
                    TransactionStatus AutoNoPayStatus = null;
                    String JENumBer = "",jeentryNumber = "";
                    String billno = "", NextPaymentnumber = "";
                    try {
                        synchronized (this) {
                            DefaultTransactionDefinition def2 = new DefaultTransactionDefinition();
                            def2.setName("AutoNum_Tx");
                            def2.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                            AutoNoPayStatus = txnManager.getTransaction(def2);
                            // Get JE Number
                            if (editPaymentObject == null) {
                                int jeIntegerPart = 0;
                                String jeDatePrefix = "";
                                String jeDateAfterPrefix = "";
                                String jeDateSuffix = "";
                                String jeSeqFormatId = "";

                                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                JEFormatParams.put("modulename", "autojournalentry");
                                JEFormatParams.put("companyid", companyid);
                                JEFormatParams.put("isdefaultFormat", true);

                                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                if (nextAutoNoIntpartforJE == 0) {
                                    seqNumberMapJE = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, journalEntry.getEntryDate());
                                    jeentryNumber = (String) seqNumberMapJE.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                                    jeIntegerPart = Integer.parseInt((String) seqNumberMapJE.get(Constants.SEQNUMBER));
                                    jeDatePrefix = (String) seqNumberMapJE.get(Constants.DATEPREFIX);//Date Prefix Part
                                    jeDateAfterPrefix = (String) seqNumberMapJE.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                                    jeDateSuffix = (String) seqNumberMapJE.get(Constants.DATESUFFIX);//Date Suffix Part
                                    nextAutoNoIntpartforJE = jeIntegerPart;
                                }
                                jeentryNumber = accCompanyPreferencesObj.getNextAutoNumber_manually(seqNumberMapJE,nextAutoNoIntpartforJE);
                                jeSeqFormatId = format.getID();

                                seqNumberMapJE.put(Constants.DOCUMENTID, journalEntry.getID());
                                seqNumberMapJE.put(Constants.companyKey, companyid);
                                seqNumberMapJE.put(Constants.SEQUENCEFORMATID, jeSeqFormatId);
                                if (approvalStatusLevel == 11) {
                                    JENumBer = accJournalEntryobj.UpdateJournalEntry(seqNumberMapJE);
                                    journalEntry.setEntryNumber(jeentryNumber);
                                    journalEntry.setSeqnumber(nextAutoNoIntpartforJE);
                                    journalEntry.setDatePreffixValue(jeDatePrefix);
                                    journalEntry.setDateAfterPreffixValue(jeDateAfterPrefix);
                                    journalEntry.setDateSuffixValue(jeDateSuffix);
                                    payment.setJournalEntry(journalEntry);
                                }
                                nextAutoNoIntpartforJE++;
                            }
                            // Get Payment Number
                            if (!sequenceformat.equals("NA") && editPaymentObject == null) {
                                boolean seqformat_oldflag = StringUtil.getBoolean(paramJobj.optString("seqformat_oldflag"));
                            String nextAutoNo = "";
                            int nextAutoNoInt = 0;
                            String datePrefix = "";
                            String dateafterPrefix = "";
                            String dateSuffix = "";
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                if (seqformat_oldflag) {
                                    nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_PAYMENT, sequenceformat);
                                    seqNumberMapPM.put(Constants.AUTO_ENTRYNUMBER, nextAutoNo);
                                } else if(nextAutoNoIntpartForPM==0){
//                                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_PAYMENT, sequenceformat, seqformat_oldflag, payment.getJournalEntry().getEntryDate());
                                    seqNumberMapPM = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_PAYMENT, sequenceformat, seqformat_oldflag, payment.getCreationDate());
                                    nextAutoNo = (String) seqNumberMapPM.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                                    nextAutoNoInt = Integer.parseInt((String) seqNumberMapPM.get(Constants.SEQNUMBER));
                                    datePrefix = (String) seqNumberMapPM.get(Constants.DATEPREFIX);//Date Prefix Part
                                    dateafterPrefix = (String) seqNumberMapPM.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                                    dateSuffix = (String) seqNumberMapPM.get(Constants.DATESUFFIX);//Date Suffix Part
                                    nextAutoNoIntpartForPM = nextAutoNoInt;
                                }
                                /**
                                 * method to get next transaction number according manually.
                                 */
                                NextPaymentnumber = accCompanyPreferencesObj.getNextAutoNumber_manually(seqNumberMapPM,nextAutoNoIntpartForPM);
                                seqNumberMapPM.put(Constants.DOCUMENTID, payment.getID());
                                seqNumberMapPM.put(Constants.companyKey, companyid);
                                seqNumberMapPM.put(Constants.SEQUENCEFORMATID, sequenceformat);
                                billno = accVendorPaymentobj.UpdatePaymentEntry(seqNumberMapPM);
                                payment.setPaymentNumber(NextPaymentnumber);
                                payment.setSeqnumber(nextAutoNoIntpartForPM);
                                payment.setDatePreffixValue(datePrefix);
                                payment.setDateAfterPreffixValue(dateafterPrefix);
                                payment.setDateSuffixValue(dateSuffix);
                                nextAutoNoIntpartForPM++;
                            }

                            /*
                             * #1 saving linking information in Make Payment & Purchase
                             * Invoice If Payment is made against Invoice
                             * #2. Generating rounding JE when GR amountdue become zero
                             */
                            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap)) {
                                if (invoicesAgainstParticularVendor.length() > 0) {
                                    JSONArray drAccArr = invoicesAgainstParticularVendor;
                                    for (int i = 0; i < drAccArr.length(); i++) {

                                        JSONObject jobj1 = drAccArr.getJSONObject(i);
                                        String invoiceid = jobj1.getString("documentid");

                                        KwlReturnObject grResult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceid);
                                        GoodsReceipt invoice = (GoodsReceipt) grResult.getEntityList().get(0);

                                        /*
                                         * Method is used to save linking informatio
                                         * of Make Payment when linking with
                                         * Purchase Invoice
                                         */
                                        saveLinkingInformationOfPaymentWithInvoice(invoice, payment, NextPaymentnumber);

                                        paramJobj.put("goodsReceiptObj", invoice);
                                        paramJobj.put("isEdit", isEdit);
                                        paramJobj.put("paymentnumber", NextPaymentnumber);
                                        postRoundingJEOnPaymentSave(paramJobj);
                                    }

                                }
                            }
                            txnManager.commit(AutoNoPayStatus);

                            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap)) {//saving after the transaction has been committed in rest
                                if (invoicesAgainstParticularVendor.length() > 0) {
                                    JSONArray drAccArr = invoicesAgainstParticularVendor;
                                    for (int i = 0; i < drAccArr.length(); i++) {

                                        JSONObject jobj1 = drAccArr.getJSONObject(i);
                                        String invoiceid = jobj1.getString("documentid");

                                        KwlReturnObject grResult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceid);
                                        GoodsReceipt invoice = (GoodsReceipt) grResult.getEntityList().get(0);

                                        /*
                                         * Method is used to save linking informatio
                                         * of Make Payment when linking with
                                         * Purchase Invoice
                                         */
                                        saveLinkingInformationOfPaymentWithInvoice(invoice, payment, NextPaymentnumber);

                                        paramJobj.put("goodsReceiptObj", invoice);
                                        paramJobj.put("isEdit", isEdit);
                                        paramJobj.put("paymentnumber", NextPaymentnumber);
                                        postRoundingJEOnPaymentSave(paramJobj);
                                    }

                                }
                            }
                        }
                        /*
                        Added to get pending payment voucher while making bulk payment
                        */
                        if (approvalStatusLevel != 11) {
                            pendingpaymentNumber += NextPaymentnumber + " ,";
                        } else {
                            journalEntryNumber += jeentryNumber + " ,";
                            paymentNumber += NextPaymentnumber + " ,";
                        }

                    } catch (Exception ex) {
                        if (AutoNoPayStatus != null) {
                            txnManager.rollback(AutoNoPayStatus);
                        }
                        deleteTemporaryInvoicesEntries(invoicesAgainstParticularVendor, companyid);
                        accPaymentDAOobj.deleteUsedInvoiceOrCheque(chequeNumber, companyid);
                        accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Make_Payment_ModuleId);
                        Logger.getLogger(accVendorPaymentControllerNew.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if (linkingDocumentNumber.length() > 0) {
                        linkingDocumentNumber = linkingDocumentNumber.substring(0, linkingDocumentNumber.length() - 1);
                    }

              auditTrailObj.insertAuditLog("75", "User " + paramJobj.optString(Constants.userfullname) + " has made a Payment " + payment.getPaymentNumber() +" by Linking to Purchase Invoice(s) "+linkingDocumentNumber, auditRequestParams, payment.getID());  

                }
           /**
             * moved transaction outside of loop to save bulk payment
             * at a time and if exception will occur it will reject the whole bulk trasanction
             **/
            txnManager.commit(status);
//                msg = messageSource.getMessage("acc.payvoucher.save", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + paymentNumber.substring(0, paymentNumber.length() - 1) + ",</b> " + messageSource.getMessage("acc.field.JENo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + journalEntryNumber.substring(0, journalEntryNumber.length() - 1) + "" + "" + "</b>";   //"Payment information has been saved successfully"; 
               if (!pendingpaymentNumber.equalsIgnoreCase("")) {
                /*
                 pending for approval case
                 */
                if (!journalEntryNumber.equalsIgnoreCase("")) {
                    /*
                     This case will be executed while making bulk payement 
                     case:- if one  payemt voucher falls in pending approval and other doesn't fall in pending approval
                     */
                    msg = messageSource.getMessage("acc.payvoucher.save", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + paymentNumber.substring(0, paymentNumber.length() - 1) + ",</b> " + messageSource.getMessage("acc.field.JENo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + journalEntryNumber.substring(0, journalEntryNumber.length() - 1) + "" + "" + "</b>";   //"Payment information has been saved successfully"; 
                } else {
                    /*
                     This case will be executed while making bulk payement 
                     case:- if all payemt voucher falls in pending approval
                     */
                    msg = messageSource.getMessage("acc.payvoucher.save", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
                }
                String pendingforapproval = "<br/> and " + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + pendingpaymentNumber.substring(0, pendingpaymentNumber.length() - 1) + "</b> " + messageSource.getMessage("acc.field.ispendingforApproval", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
                msg += pendingforapproval;
            } else {
                /*
                 This case will be executed while making bulk payement 
                 case:- none of payemt voucher falls in pending approval 
                 */
                msg = messageSource.getMessage("acc.payvoucher.save", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + paymentNumber.substring(0, paymentNumber.length() - 1) + ",</b> " + messageSource.getMessage("acc.field.JENo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + journalEntryNumber.substring(0, journalEntryNumber.length() - 1) + "" + "" + "</b>";   //"Payment information has been saved successfully"; 
            }

        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            deleteTemporaryInvoicesEntries(invoicesAgainstParticularVendor, companyid);
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Make_Payment_ModuleId);
                accPaymentDAOobj.deleteUsedInvoiceOrCheque(chequeNumber, companyid);

            } catch (Exception ee) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ee);
            }
            msg = ex.getMessage();
            isSuccess = false;
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException jex) {
            isSuccess = false;
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, jex);
        } catch (AccountingException AE) {
            if (status != null && !exceptionChequeOrInv) {
                txnManager.rollback(status);
            }
            deleteTemporaryInvoicesEntries(invoicesAgainstParticularVendor, companyid);
            if (!isInvalidChequeNumber) {
                try {
                    status = txnManager.getTransaction(def);
                    accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Make_Payment_ModuleId);
                    txnManager.commit(status);
                    accPaymentDAOobj.deleteUsedInvoiceOrCheque(chequeNumber, companyid);

                } catch (Exception ee) {
                    Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ee);
                }
            }
            msg = "" + AE.getMessage();
            if (AE.getMessage() == null) {
                msg = AE.getCause().getMessage();
            }
            isSuccess = false;
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, AE);
        } catch (ParseException pex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, pex);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            deleteTemporaryInvoicesEntries(invoicesAgainstParticularVendor, companyid);
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Make_Payment_ModuleId);
                accPaymentDAOobj.deleteUsedInvoiceOrCheque(chequeNumber, companyid);

            } catch (Exception ee) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            isSuccess = false;
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", isSuccess);
                jobj.put("msg", msg);//msg
                jobj.put("data", chequeJArr);         //cheque data
                hashMap.put("jobj", jobj);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return hashMap;
    }

   public void postJEforImportServiceInvoices(Payment payment, JSONObject paramJobj) throws ServiceException, AccountingException, JSONException, SessionExpiredException {
        GoodsReceipt goodsReceipt = null;
        double invoiceAmountPaid = 0.0d;
        double taxAmountPaid = 0.0d;
        double invoiceTaxAmount = 0.0d;
        String companyId = payment.getCompany().getCompanyID();
        String gstInputAccountId = "";
        String gstOutputAccountId = "";
        KwlReturnObject accountReturnObject = accAccountDAOobj.getAccountFromName(payment.getCompany().getCompanyID(), Constants.MALAYSIAN_GST_INPUT_TAX);
        List accountResultList = accountReturnObject.getEntityList();
        if (!accountResultList.isEmpty()) {
            gstInputAccountId = ((Account) accountResultList.get(0)).getID();
        }
        accountReturnObject = accAccountDAOobj.getAccountFromName(payment.getCompany().getCompanyID(), Constants.MALAYSIAN_GST_OUTPUT_TAX);
        accountResultList = accountReturnObject.getEntityList();
        if (!accountResultList.isEmpty()) {
            gstOutputAccountId = ((Account) accountResultList.get(0)).getID();
        }

        Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);
        String jeentryNumber = "";
        String jeIntegerPart = "";
        String jeDatePrefix = "";
        String jeDateAfterPrefix = "";
        String jeDateSuffix = "";
        String jeSeqFormatId = "";
        boolean jeautogenflag = true;
        synchronized (this) {
            HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
            JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
            JEFormatParams.put("modulename", "autojournalentry");
            JEFormatParams.put("companyid", companyId);
            JEFormatParams.put("isdefaultFormat", true);

            KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
            SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, payment.getJournalEntry().getEntryDate());
            jeentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
            jeIntegerPart = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
            jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
            jeDateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
            jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
            jeSeqFormatId = format.getID();
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
        }
        jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
        jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
        jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
        jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
        jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
        jeDataMap.put("entrydate", payment.getJournalEntry().getEntryDate());
        jeDataMap.put("companyid", companyId);
        jeDataMap.put("memo", "");
        jeDataMap.put("currencyid", payment.getCurrency().getCurrencyID());

        KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
        JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
        if (payment.getApprovestatuslevel() == 11) {
            journalEntry.setPendingapproval(0);
        } else {
            journalEntry.setPendingapproval(1);
        }        
        journalEntry.setExternalCurrencyRate(payment.getExternalCurrencyRate());
        String jeid = journalEntry.getID();
        KwlReturnObject jedresult = null;

        Set<JournalEntryDetail> jeDetails = new HashSet<JournalEntryDetail>();
        JournalEntryDetail jeDetail = null;
        JSONObject jedjson = null;
        boolean paymentHaveImportServiceInvoice = false;
        for (PaymentDetail paymentDetail : payment.getRows()) {
            goodsReceipt = paymentDetail.getGoodsReceipt();
            if (goodsReceipt != null && goodsReceipt.isImportService()) {
                paymentHaveImportServiceInvoice = true;
                invoiceAmountPaid = paymentDetail.getAmount();
                taxAmountPaid = (invoiceAmountPaid * 6) / 100;
                taxAmountPaid = authHandler.round(taxAmountPaid, companyId);

                jedjson = new JSONObject();
                jedjson.put("srno", jeDetails.size() + 1);
                jedjson.put("companyid", companyId);
                jedjson.put("accountid", gstInputAccountId);
                jedjson.put("amount", taxAmountPaid);
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                jedjson.put("description", goodsReceipt.getGoodsReceiptNumber());
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jeDetail = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jeDetails.add(jeDetail);

                jedjson = new JSONObject();
                jedjson.put("srno", jeDetails.size() + 1);
                jedjson.put("companyid", companyId);
                jedjson.put("accountid", gstOutputAccountId);
                jedjson.put("amount", taxAmountPaid);
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedjson.put("description", goodsReceipt.getGoodsReceiptNumber());
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jeDetail = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jeDetails.add(jeDetail);
            }
        }
        if(paymentHaveImportServiceInvoice){
            journalEntry.setDetails(jeDetails);
            journalEntry.setTransactionId(payment.getID());
            journalEntry.setTransactionModuleid(Constants.Acc_Import_Service_Invoice_Payment_ModuleId);
            
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("customfield"))) {
                JSONArray jcustomarray = new JSONArray(paramJobj.optString("customfield"));
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", companyId);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    KwlReturnObject receiptAccJECustomData = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), journalEntry.getID());
                    AccJECustomData accJECustomData = (AccJECustomData) receiptAccJECustomData.getEntityList().get(0);
                    journalEntry.setAccBillInvCustomData(accJECustomData);
                }
            }
            
            payment.setImportServiceJE(journalEntry);
        }    
    }
    
    public void checkForInvalidChequeNumber(JSONObject paramJobj, Payment editPaymentObject) throws ServiceException, SessionExpiredException, AccountingException {
        try {
            String oldChequeNo = "";
            String companyid = paramJobj.optString(Constants.companyKey);;
            String methodid = "";
            if (paramJobj.optString("paymentMethodID") != null && !StringUtil.isNullOrEmpty(paramJobj.optString("paymentMethodID"))) { // SDP-3075 in case the Opening transcation "PaymnetMethodID" is passed
                methodid = paramJobj.optString("paymentMethodID");
            } else {
                methodid = paramJobj.optString("pmtmethod");
            }
            boolean isWarn = paramJobj.optString("isWarn") != null ? Boolean.parseBoolean(paramJobj.optString("isWarn")) : false;
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);

            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), methodid);
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            HashMap pdetailhm = new HashMap();
            pdetailhm.put("paymethodid", payMethod.getID());
            pdetailhm.put("companyid", companyid);
            BigInteger oldChqNoIntValue = new BigInteger("0");
            if (payMethod.getDetailType() != PaymentMethod.TYPE_CASH && paramJobj.optString("paydetail") != null) {

                JSONObject obj = new JSONObject(paramJobj.optString("paydetail"));
                if (editPaymentObject != null && editPaymentObject.getPayDetail() != null) {
                    if (editPaymentObject.getPayDetail().getCheque() != null) {
                        oldChequeNo = editPaymentObject.getPayDetail().getCheque().getChequeNo();
                        oldChqNoIntValue  = editPaymentObject.getPayDetail().getCheque().getSequenceNumber();
                    }
                }
                if (payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
                    BigInteger currentChequeNumber = new BigInteger("0");
                    boolean checkForNextSequenceNumberAlso = true;
                    boolean isChequeNumberInString = false;
                    if (extraCompanyPreferences != null) {
                        try {
                            currentChequeNumber = new BigInteger(obj.getString("chequenumber"));
                        } catch (Exception ex) {
                            checkForNextSequenceNumberAlso = false;
                            isChequeNumberInString = true;
                        }
                    } else {
                        checkForNextSequenceNumberAlso = false;
                    }
                    boolean isChequeNumberAvailable = false;
                    boolean isEditCaseButChqNoChanged = false;
                    if (!StringUtil.isNullOrEmpty(obj.optString("chequenumber", "")) && extraCompanyPreferences != null) {
                        try {
                            HashMap chequeNohm = new HashMap();
                            chequeNohm.put("companyId", companyid);
                            chequeNohm.put("sequenceNumber", currentChequeNumber);
                            chequeNohm.put("checkForNextSequenceNumberAlso", checkForNextSequenceNumberAlso);
                            chequeNohm.put("nextChequeNumber", obj.optString("chequenumber"));
                            chequeNohm.put("bankAccountId", payMethod.getAccount().getID());
                            chequeNohm.put("chequesequenceformatid", obj.optString("sequenceformat"));
                            JSONObject ChJobj = accCompanyPreferencesObj.isChequeNumberAvailable(chequeNohm);
                            isChequeNumberAvailable = ChJobj.optBoolean("isChequeNumberAvailable");
                            BigInteger chequesequencenumber =new BigInteger(ChJobj.optString("chequesequencenumber"));
//                            isChequeNumberAvailable = paymentService.isChequeNumberAvailable(chequeNohm);

                            
//                            if (!StringUtil.isNullOrEmpty(oldChequeNo)) {
//                                oldChqNoIntValue = new BigInteger(oldChequeNo);
//                            }

                            if (!oldChqNoIntValue.equals(chequesequencenumber)) {
                                isEditCaseButChqNoChanged = true;
                            }

                            if (isChequeNumberInString) {
                                if (!oldChequeNo.equals(obj.optString("chequenumber"))) {
                                    isEditCaseButChqNoChanged = true;
                                }
                            }
                        } catch (Exception ex) {
                            if (!oldChequeNo.equals(obj.optString("chequenumber"))) {
                                isEditCaseButChqNoChanged = true;
                            }
                        }
                    } else {
                        if (!oldChequeNo.equals(obj.optString("chequenumber"))) {
                            isEditCaseButChqNoChanged = true;
                        }
                    }
                    if (preferences.getChequeNoDuplicate() != Constants.ChequeNoIgnore) {
                        if (preferences.getChequeNoDuplicate() == Constants.ChequeNoBlock || (preferences.getChequeNoDuplicate() == Constants.ChequeNoWarn && isWarn)) {
                            if (!StringUtil.isNullOrEmpty(obj.optString("chequenumber")) && isChequeNumberAvailable && isEditCaseButChqNoChanged) {
                                String msgForException = "Cheque Number : <b>" + obj.getString("chequenumber") + "</b> is already exist. ";
                                if (isWarn) {
                                    msgForException +="System will generate next cheque number. ";
                                    throw new AccountingException(msgForException);
                                } else {
                                    throw new AccountingException(msgForException + "Please enter different one. ");
                                }
                            }
                            String chequeNumber = obj.optString("chequenumber");
                            chequeNumber = "'" + chequeNumber + "'";
                            HashMap<String, Object> requestMap = new HashMap();
                            requestMap.put("bankAccountId", payMethod.getAccount().getID());
                            requestMap.put("chequeNumber", chequeNumber);
                            KwlReturnObject resultRepeatedPaymentChequeDetails = accPaymentDAOobj.getRepeatedPaymentChequeDetailsForPaymentMethod(requestMap);
                            List RPCD = resultRepeatedPaymentChequeDetails.getEntityList();
                            if (RPCD.size() > 0) {
                                Object[] object = (Object[]) RPCD.get(0);
                                String paymentNumber = (String) object[1];
                                String msgForException = messageSource.getMessage("acc.field.ChequeNumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " : <b>" + obj.getString("chequenumber") + "</b> " + messageSource.getMessage("acc.recurringMP.chequeNoReserverd", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + paymentNumber + "</b>. ";
                                if (isWarn) {
                                    throw new AccountingException(msgForException);
                                } else {
                                    throw new AccountingException(msgForException + messageSource.getMessage("acc.recurringMP.enteranotherChequeNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException jex) {
            throw ServiceException.FAILURE(jex.getMessage(), jex);
        } catch (ServiceException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public void setValuesForAuditTrialMessage(Payment oldgrd, Map<String, Object> oldgreceipt, Map<String, Object> newAuditKey) throws SessionExpiredException {
        try {
            if (oldgrd != null) {
                //Receipt Number Change
                oldgreceipt.put(Constants.PaymentNumber, oldgrd.getPaymentNumber());
                newAuditKey.put(Constants.PaymentNumber, "Payment Number");
                //Received From Change
                oldgreceipt.put(Constants.PaidTo, oldgrd.getPaidTo() != null ? oldgrd.getPaidTo().getValue() : "");
                newAuditKey.put(Constants.PaidTo, "Paid To");
                //Creation Date
//                oldgreceipt.put(Constants.CreationDate, oldgrd.isIsOpeningBalencePayment() ? oldgrd.getCreationDate() : oldgrd.getJournalEntry().getEntryDate());
                oldgreceipt.put(Constants.CreationDate, oldgrd.getCreationDate());
                newAuditKey.put(Constants.CreationDate, "Creation Date");
                //Memo
                oldgreceipt.put(Constants.Memo, oldgrd.getMemo());
                newAuditKey.put(Constants.Memo, "Memo");
                if (oldgrd.getPayDetail() != null && oldgrd.getPayDetail() != null) {
                    int oldPaymentMethodType = oldgrd.getPayDetail().getPaymentMethod().getDetailType();
                    String oldPaymentMethodTypeName = oldgrd.getPayDetail().getPaymentMethod().getMethodName();
                    //PaymentMethodType
                    oldgreceipt.put(Constants.PaymentMethodType, oldPaymentMethodTypeName);
                    newAuditKey.put(Constants.PaymentMethodType, "Payment Method Type");
                    if (oldPaymentMethodType == PaymentMethod.TYPE_BANK) {
                        //Cheque
                        Cheque oldCheck = oldgrd.getPayDetail().getCheque();
                        oldgreceipt.put(Constants.Cheque, oldCheck);
                        newAuditKey.put(Constants.Cheque, "Cheque");
                        //Check Number
                        oldgreceipt.put(Constants.ChequeNumber, oldCheck.getChequeNo());
                        newAuditKey.put(Constants.ChequeNumber, "Cheque Number");
                        //Bank Name
                        oldgreceipt.put(Constants.BankName, oldCheck.getBankName());
                        newAuditKey.put(Constants.BankName, "Bank Name");
                        //Check Date
                        oldgreceipt.put(Constants.CheckDate, oldCheck.getDueDate());
                        newAuditKey.put(Constants.CheckDate, "Check Date");
                    } else if (oldPaymentMethodType == PaymentMethod.TYPE_CARD) {
                        // Card
                        oldgreceipt.put(Constants.Card, oldgrd.getPayDetail().getCard());
                        newAuditKey.put(Constants.Card, "Card");
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accVendorPaymentControllerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

     public Payment createPaymentObject(JSONObject paramJobj, Payment editPaymentObject) throws SessionExpiredException, ServiceException, AccountingException, JSONException {
        List list = new ArrayList();
        KwlReturnObject result = null;
        Invoice invoice = null;
        Customer cust = null;
        Vendor vend = null;
        Payment paymentObject = null;
        String sequenceformat = paramJobj.optString("sequenceformat") != null ? paramJobj.optString("sequenceformat") : "NA";
        int seqNumber = paramJobj.optString(Constants.SEQNUMBER, null) != null ? Integer.parseInt(paramJobj.optString(Constants.SEQNUMBER).toString()) : 0;
        String companyid = paramJobj.optString(Constants.companyKey);
        double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate"));
        String currencyid = paramJobj.optString(Constants.currencyKey);
        int receiptType = StringUtil.getInteger(paramJobj.optString("receipttype"));
        int actualReceiptType = StringUtil.getInteger(paramJobj.optString("actualReceiptType") != null ? paramJobj.optString("actualReceiptType") : "0");
        boolean isAdvancePayment = StringUtil.getBoolean(paramJobj.optString("isadvpayment"));
        String entryNumber = paramJobj.optString("no");
        String exciseunit = paramJobj.optString("exciseunit") != null ? paramJobj.optString("exciseunit") : "";
        String methodid = paramJobj.optString("pmtmethod");
        String payee = paramJobj.optString("payee");
        //ERP-39302
        JSONObject obj = null;
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("paydetail", ""))) {
            obj = new JSONObject(paramJobj.optString("paydetail", ""));
        } else {
            obj = new JSONObject();
        }
        String chequesequenceformat = obj.optString("sequenceformat") != null ? obj.optString("sequenceformat") : "NA";
        String chequeNumber = obj.optString("chequenumber");
        int chequecduplicatepref = paramJobj.optInt("chequecduplicatepref",1);
        double paymentCurrencyToPaymentMethodCurrencyRate = StringUtil.getDouble(paramJobj.optString("paymentCurrencyToPaymentMethodCurrencyExchangeRate"));
        sessionHandlerImpl sess = new sessionHandlerImpl();
        if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap)) { 
          sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
        }
        
        DateFormat df = authHandler.getDateFormatter(paramJobj);
        if (paramJobj.optBoolean(Constants.isdefaultHeaderMap)) {
          df = authHandler.getDateOnlyFormat();
        }
        Date creationDate = null;
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("creationdate"))) {
            try {
                creationDate = df.parse(paramJobj.optString("creationdate"));
            } catch (Exception ex) {
                throw ServiceException.FAILURE("createPaymentObject : " + ex.getMessage(), ex);
            }
        }
        boolean isIBGTypeTransaction = StringUtil.getBoolean(paramJobj.optString("isIBGTypeTransaction"));
        String ibgDetailsID = paramJobj.optString("ibgDetailsID");
        String ibgCode = paramJobj.optString("ibgCode");
        int bankType = !StringUtil.isNullOrEmpty(paramJobj.optString("bankType")) ? Integer.parseInt(paramJobj.optString("bankType")) : 0;
        boolean rcmApplicable = !StringUtil.isNullOrEmpty(paramJobj.optString("rcmApplicable")) ? Boolean.parseBoolean(paramJobj.optString("rcmApplicable")) : false;
        boolean advanceToVendor = paramJobj.optBoolean("advanceToVendor", false);
        HashMap paymenthm = new HashMap();
        boolean ismanydbcr = StringUtil.getBoolean(paramJobj.optString("ismanydbcr"));
        paymenthm.put("ismanydbcr", ismanydbcr);
        paymenthm.put("isadvancepayment", isAdvancePayment);
        paymenthm.put("receipttype", receiptType);
        paymenthm.put("actualReceiptType", actualReceiptType);

        if (isIBGTypeTransaction) {
            paymenthm.put("isIBGTypeTransaction", isIBGTypeTransaction);
            paymenthm.put("ibgDetailsID", ibgDetailsID);
            paymenthm.put("ibgCode", ibgCode);
            paymenthm.put("bankType", bankType);
        } else {
            paymenthm.put("isIBGTypeTransaction", false);
        }

        double bankCharges = 0;
        double bankInterest = 0;
        boolean onlyAdvance = StringUtil.getBoolean(paramJobj.optString("onlyAdvance"));
        String bankChargesAccid = paramJobj.optString("bankChargesCmb");
        String bankInterestAccid = paramJobj.optString("bankInterestCmb");
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("bankCharges")) && !StringUtil.isNullOrEmpty(bankChargesAccid)) {
            bankCharges = Double.parseDouble(paramJobj.optString("bankCharges"));
            paymenthm.put("bankCharges", bankCharges);
            paymenthm.put("bankChargesCmb", bankChargesAccid);
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("bankInterest")) && !StringUtil.isNullOrEmpty(bankInterestAccid)) {
            bankInterest = Double.parseDouble(paramJobj.optString("bankInterest"));
            paymenthm.put("bankInterest", bankInterest);
            paymenthm.put("bankInterestCmb", bankInterestAccid);
        }
        String paidToid = paramJobj.optString("paidToCmb");
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("paidToCmb")) && !StringUtil.isNullOrEmpty(paidToid)) {
            paymenthm.put("paidToCmb", paidToid);
        }

        boolean isCustomer = false;
        boolean isVendor = false;
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("accid"))) {
            KwlReturnObject custObj = accountingHandlerDAOobj.getObject(Customer.class.getName(), paramJobj.optString("accid"));
            if (custObj.getEntityList().get(0) != null) {
                cust = (Customer) custObj.getEntityList().get(0);
            }
            if (cust != null) {
                isCustomer = true;
            }

            KwlReturnObject vendObj = accountingHandlerDAOobj.getObject(Vendor.class.getName(), paramJobj.optString("accid"));
            if (vendObj.getEntityList().get(0) != null) {
                vend = (Vendor) vendObj.getEntityList().get(0);
            }
            if (vend != null) {
                isVendor = true;
            }
        }
        if (isCustomer) {
            paymenthm.put("customer", paramJobj.optString("accid"));
        } else if (isVendor) {
            paymenthm.put("vendorId", paramJobj.optString("accid"));
        }
        String createdby = paramJobj.optString(Constants.useridKey);
        String modifiedby = paramJobj.optString(Constants.useridKey);
        long createdon = System.currentTimeMillis();
        long updatedon = System.currentTimeMillis();

        synchronized (this) { //this block is used to generate auto sequence number if number is not duplicate
            String nextAutoNo = "";
            String nextAutoNoInt = "";
            boolean isFromOtherSource=paramJobj.optBoolean("isFromOtherSource", false);             //if this service is called from other source like import than the sequence number should not be generated instead it will take Receipt number from file
            int count = 0;
            if (editPaymentObject != null) {
                if (sequenceformat.equals("NA")) {
                    if (!entryNumber.equals(editPaymentObject.getPaymentNumber())) {
                        result = accVendorPaymentobj.getPaymentFromNo(entryNumber, companyid);
                        paymenthm.put("entrynumber", entryNumber);
                        paymenthm.put("autogenerated", entryNumber.equals(nextAutoNo));
                        count = result.getRecordTotalCount();
                    }
                    if (count > 0) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
            } else {
                if (!sequenceformat.equals("NA")) {
                    paymenthm.put(Constants.SEQFORMAT, sequenceformat);
                    paymenthm.put(Constants.SEQNUMBER, "");
                }
                if (sequenceformat.equals("NA")) {
                    paymenthm.put("entrynumber", entryNumber);
                } else {
                    if (isFromOtherSource) {        //this block will pass entry number from imported CSV.
                        paymenthm.put("entrynumber", entryNumber);
                        paymenthm.put(Constants.SEQNUMBER, seqNumber);
                        paymenthm.put(Constants.SEQFORMAT, sequenceformat);
                    } else {
                    paymenthm.put("entrynumber", "");
                }
                }
                paymenthm.put("autogenerated", sequenceformat.equals("NA") ? false : true);
            }
        }
        if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
            List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Make_Payment_ModuleId, entryNumber, companyid);
            if (!resultList.isEmpty()) {
                boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                String formatName = (String) resultList.get(1);
                if (!isvalidEntryNumber) {
                    throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
            }
        }
        /*
        * Checks in block case the cheque number is belongs to any sequence format or NA
        */
         if ((Constants.ChequeNoBlock == chequecduplicatepref) && (!StringUtil.isNullOrEmpty(chequeNumber))) {
             List resultList = accCompanyPreferencesObj.checksChequeNumberForSequenceNumber(chequeNumber, companyid);
             if (!resultList.isEmpty() && resultList.size() > 0) {
//                boolean isvalidEntryNumber = (Boolean) resultList.get(0);
//                String formatName = (String) resultList.get(1);
//                if (!isvalidEntryNumber) {
//                    throw new AccountingException(messageSource.getMessage("acc.common.enterchequenumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + chequeNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadofcheque", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
//                }
                 JSONObject resultJObj = new JSONObject(((resultList.get(2) != null) && (!StringUtil.isNullOrEmpty(resultList.get(2).toString()))) ? resultList.get(2).toString() : "{}");
                 if (resultJObj.length() > 0) {
                     String seqformatName = resultJObj.optString("formatName");
                     boolean isSeqnum = resultJObj.optBoolean("isSeqnum");
                     String seqformatId = resultJObj.optString("formatid");
                     if (chequesequenceformat.equalsIgnoreCase("NA")) {
                         //selected sequence formate is NA and cheque number is belongs to any sequence format.
                         if (isSeqnum) {
                             throw new AccountingException(messageSource.getMessage("acc.common.enterchequenumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + chequeNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ". " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + seqformatName + "</b> " + messageSource.getMessage("acc.common.insteadofcheque", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                         }
                     } else {
                         if (isSeqnum) {
                             //cheque number is belong to other than selected sequence format
                             if (!(chequesequenceformat.equals(seqformatId))) {
                                 throw new AccountingException(messageSource.getMessage("acc.common.enterchequenumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + chequeNumber + "</b> " + messageSource.getMessage("acc.common.notbelongstoselectedsequenceformat", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ". " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + seqformatName + "</b> " + messageSource.getMessage("acc.common.insteadofcheque", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                             }
                         } else {
                             //cheque number is not belong to any of the sequence format
                             throw new AccountingException(messageSource.getMessage("acc.common.enterchequenumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + chequeNumber + "</b> " + messageSource.getMessage("acc.common.notBelongstoAnyOfTheAutoSequenceFormat", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ". " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>NA</b> " + messageSource.getMessage("acc.common.insteadofcheque", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                         }
                     }
                 }
             }
         }

        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        currencyid = (paramJobj.optString("currencyid") == null ? currency.getCurrencyID() : paramJobj.optString("currencyid"));

        paymenthm.put("currencyid", currencyid);
        paymenthm.put("externalCurrencyRate", externalCurrencyRate);
        result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod"));
        PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);
        paymenthm.put("memo", paramJobj.optString("memo"));
        paymenthm.put("nonRefundable", !StringUtil.isNullOrEmpty(paramJobj.optString("NonRefundable")));
        paymenthm.put("cinno", !StringUtil.isNullOrEmpty(paramJobj.optString("cinno")) ? paramJobj.optString("cinno") : "");
        paymenthm.put("isLinkedToClaimedInvoice", !StringUtil.isNullOrEmpty(paramJobj.optString("isLinkedToClaimedInvoice")) ? Boolean.parseBoolean(paramJobj.optString("isLinkedToClaimedInvoice")) : false);
        paymenthm.put("companyid", companyid);
        paymenthm.put("createdby", createdby);
        paymenthm.put("modifiedby", modifiedby);
        paymenthm.put("creationDate", creationDate);
        paymenthm.put(Constants.Checklocktransactiondate, paramJobj.optString("creationdate"));//ERP-16800-Without parsing date
        paymenthm.put("createdon", createdon);
        paymenthm.put("updatedon", updatedon);
        paymenthm.put("PaymentCurrencyToPaymentMethodCurrencyRate", paymentCurrencyToPaymentMethodCurrencyRate);
        if (editPaymentObject != null) {
            paymenthm.put("paymentid", editPaymentObject.getID());
        }
        paymenthm.put("payee", payee);
        paymenthm.put("exciseunit", exciseunit);
        paymenthm.put("rcmApplicable", rcmApplicable);
        paymenthm.put("advanceToVendor", advanceToVendor);
        paymentObject = accVendorPaymentobj.getPaymentObj(paymenthm);


        return paymentObject;
    }

    public JournalEntry journalEntryObject(JSONObject paramJobj, Payment editPaymentObject) throws SessionExpiredException, ServiceException, AccountingException {
        JournalEntry journalEntry = null;
        try {
            String companyid = paramJobj.optString(Constants.companyKey);

            DateFormat df = authHandler.getDateOnlyFormat();
            String currencyid = paramJobj.optString(Constants.currencyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (paramJobj.optString("currencyid") == null ? currency.getCurrencyID() : paramJobj.optString("currencyid"));
            String methodid = paramJobj.optString("pmtmethod");
            sessionHandlerImpl sess = new sessionHandlerImpl();
            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap)) {
                sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
            }
            String createdby = paramJobj.optString(Constants.useridKey);
            boolean jeautogenflag = false;
            String jeSeqFormatId = "";
            Date entryDate = df.parse(paramJobj.optString("creationdate"));
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate"));
            double PaymentCurrencyToPaymentMethodCurrencyRate = StringUtil.getDouble(paramJobj.optString("paymentCurrencyToPaymentMethodCurrencyExchangeRate"));
            boolean ismulticurrencypaymentje = StringUtil.getBoolean(paramJobj.optString("ismulticurrencypaymentje"));
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            boolean isFromEclaim = paramJobj.has("isFromEclaim") ? paramJobj.getBoolean("isFromEclaim"): false;
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);

            if (editPaymentObject == null) {
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put("companyid", companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;

                    jeDataMap.put("entrynumber", "");
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                }
            } else if (editPaymentObject != null && editPaymentObject.getJournalEntry() != null) {
                JournalEntry entry = editPaymentObject.getJournalEntry();
                jeDataMap.put("entrynumber", entry.getEntryNumber());
                jeDataMap.put("autogenerated", entry.isAutoGenerated());
                jeDataMap.put(Constants.SEQFORMAT, entry.getSeqformat().getID());
                jeDataMap.put(Constants.SEQNUMBER, entry.getSeqnumber());
                jeDataMap.put(Constants.DATEPREFIX, entry.getDatePreffixValue());
                jeDataMap.put(Constants.DATEAFTERPREFIX, entry.getDateAfterPreffixValue());
                jeDataMap.put(Constants.DATESUFFIX, entry.getDateSuffixValue());
            }
            jeDataMap.put("PaymentCurrencyToPaymentMethodCurrencyRate", PaymentCurrencyToPaymentMethodCurrencyRate);
            jeDataMap.put("entrydate", entryDate);
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", paramJobj.optString("memo"));
            jeDataMap.put("currencyid", currencyid);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("ismulticurrencypaymentje", ismulticurrencypaymentje);
            jeDataMap.put("createdby", createdby);
            jeDataMap.put("isFromEclaim", isFromEclaim);
            journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);

        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return journalEntry;
    }
    /*
     * Update receipt advance amount due on payment with refund entry delete
     */
    
     public void updateReceiptAdvancePaymentAmountDue(Payment payment, String companyId, int beforeEditApprovalStatus, String countryId) throws JSONException, ServiceException {
        if (payment != null) {
            Set<AdvanceDetail> advanceDetails = payment.getAdvanceDetails();
            if (payment.getAdvanceDetails() != null && !advanceDetails.isEmpty()) {
                for (AdvanceDetail adv : advanceDetails) {
                    if (adv.getReceiptAdvanceDetails() != null) {
                        if (beforeEditApprovalStatus == Constants.APPROVED_STATUS_LEVEL) {
                            double finalAmtDue = authHandler.round((adv.getReceiptAdvanceDetails().getAmountDue() + (adv.getAmount() / adv.getExchangeratefortransaction())), companyId);
                            adv.getReceiptAdvanceDetails().setAmountDue(finalAmtDue);
                        }
                        if (adv != null && adv.getRevalJeId() != null) { //Deleting Refund JE Detail if any
                            accJournalEntryobj.deleteJEDtails(adv.getRevalJeId(), companyId);
                            accJournalEntryobj.deleteJE(adv.getRevalJeId(), companyId);
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(countryId) && countryId.equals(Constants.INDIA_COUNTRYID)) {
                        /**
                         * delete AdvanceDetailTermMap mapping while editing
                         * payment.
                         */
                        JSONObject reqPrams=new JSONObject();
                        reqPrams.put("advanceDetailid", adv.getId());
                        accVendorPaymentobj.deleteAdvanceDetailsTerm(reqPrams);
                    }
                }
            }
        }
    }

    public Set<JournalEntryDetail> journalEntryDetailObject(JSONObject paramJobj, JSONArray detailsJSONArray, JournalEntry journalEntry, Payment payment, int type) throws SessionExpiredException, ServiceException, AccountingException {
        double amount = 0;
        StringBuffer billno = new StringBuffer();
        Set jedetails = new HashSet();

        try {
            Account dipositTo = null;
            double amountDiff = 0;
            boolean rateDecreased = false;
            String companyid = paramJobj.optString(Constants.companyKey);
            boolean isFromEclaim = paramJobj.has("isFromEclaim") ? paramJobj.getBoolean("isFromEclaim") : false;
            boolean isDiscountAppliedOnPaymentTerms = paramJobj.optBoolean("isDiscountAppliedOnPaymentTerms", false);
            boolean isEdit = paramJobj.optBoolean("isEdit", false);
            /*
             * Spot Rate - externalCurrencyRate Values - 0 - If not entered
             */
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate"));
            String currencyid = paramJobj.optString(Constants.currencyKey);
            String methodid = paramJobj.optString("pmtmethod");
            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap)) {
                sessionHandlerImpl sess = new sessionHandlerImpl();
                sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
            }

            String jeid = null;
            if (journalEntry != null) {
                jeid = journalEntry.getID();
            }
            HashMap<String, JSONArray> jcustomarrayMap = new HashMap();
            payment.setJcustomarrayMap(jcustomarrayMap);
            double balaceAmount = 0.0;
            String accountIdComPreAdjPay = "";
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (paramJobj.optString("currencyid") == null ? currency.getCurrencyID() : paramJobj.optString("currencyid"));

            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            String countryID = preferences != null && preferences.getCompany() != null && preferences.getCompany().getCountry() != null ? preferences.getCompany().getCountry().getID() : "";
            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("balaceAmount"))) {
                balaceAmount = Double.parseDouble(paramJobj.optString("balaceAmount"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("accountIdComPreAdjPay"))) {
                accountIdComPreAdjPay = paramJobj.optString("accountIdComPreAdjPay");
            }
            String accountId = "";
            /*
             * Check if Customer/Vendor and set PaymentWindow type accordingly
             */
            boolean isCustomer = Boolean.parseBoolean(paramJobj.optString("iscustomer"));
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("accid"))) {
                if (isCustomer) {
                    KwlReturnObject resultCVAccount = accountingHandlerDAOobj.getObject(Customer.class.getName(), paramJobj.optString("accid"));
                    if (!resultCVAccount.getEntityList().isEmpty()) {
                        Customer customer = (Customer) resultCVAccount.getEntityList().get(0);
                        accountId = customer.getAccount().getID();
                        payment.setPaymentWindowType(2);
                    }
                } else {
                    KwlReturnObject resultCVAccount = accountingHandlerDAOobj.getObject(Vendor.class.getName(), paramJobj.optString("accid"));
                    if (!resultCVAccount.getEntityList().isEmpty()) {
                        Vendor vendor = (Vendor) resultCVAccount.getEntityList().get(0);
                        accountId = vendor.getAccount().getID();
                        payment.setPaymentWindowType(1);
                    }
                }
            } else {
                payment.setPaymentWindowType(3);
            }
            JSONArray jArr = new JSONArray();
            if (detailsJSONArray != null) {
                jArr = detailsJSONArray;
            }

            /*
             * Below block handled for 1. Against Invoice
             *
             */
            if (jArr.length() > 0 && type == Constants.PaymentAgainstInvoice) {
                amount = 0;
                String gstInputAccountId = "";
                KwlReturnObject accountReturnObject = accAccountDAOobj.getAccountFromName(companyid, Constants.MALAYSIAN_GST_INPUT_TAX);
                List accountResultList = accountReturnObject.getEntityList();
                if (!accountResultList.isEmpty()) {
                    gstInputAccountId = ((Account) accountResultList.get(0)).getID();
                }
                for (int i = 0; i < jArr.length(); i++) { // Changed for New Customer/Vendor Removed account dependency
                    JSONObject jobj = jArr.getJSONObject(i);
                    JSONArray jArray = new JSONArray();
                    jArray.put(jobj);
                    String accountIdForGst="";
                    double amountReceived = jobj.optDouble("enteramount", 0.0);// amount in receipt currency
                    double amountReceivedConverted = jobj.optDouble("enteramount", 0.0); // amount in invoice currency
                    double discountAmount = jobj.optDouble("discountname", 0.0);
                    /*
                     *
                     * Calculate Forex Gain/Loss Amount Diff and Generate JE
                     * Details with amount = Total Enterted Amount - Forex
                     * Amount.
                     *
                     */
                    KwlReturnObject resultGoodsReceipt = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), jobj.getString("documentid"));
                    GoodsReceipt goodsReceipt = (GoodsReceipt) resultGoodsReceipt.getEntityList().get(0);
                    double amountDiffforInv = authHandler.round(oldPaymentRowsAmount(paramJobj, jArray, currencyid, externalCurrencyRate), companyid);
                    double adjustedRate = 1.0;
                    if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(goodsReceipt.getCurrency().getCurrencyID()) && !goodsReceipt.getCurrency().getCurrencyID().equals(payment.getCurrency().getCurrencyID())) {
                        adjustedRate = jobj.optDouble("amountdue", 0) / jobj.optDouble("amountDueOriginal", 0);
                        amountReceivedConverted = amountReceived / adjustedRate;;
                        amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                    }
                    boolean isInvoiceIsClaimed = false;
                    isInvoiceIsClaimed = goodsReceipt.getBadDebtType() == Constants.Invoice_Claimed || goodsReceipt.getBadDebtType() == Constants.Invoice_Recovered;
                    double enterAmount = authHandler.round(jobj.optDouble("enteramount",0.0), companyid);
                    amount += authHandler.round(jobj.optDouble("enteramount",0.0), companyid);
                    boolean isTDSApplied = extraCompanyPreferences.isTDSapplicable();
                    if (isTDSApplied && jobj.has("appliedTDS") && !StringUtil.isNullOrEmpty(jobj.get("appliedTDS").toString())) {
                        JSONArray jedTdsJsonArray = jobj.has("appliedTDS") && !jobj.get("appliedTDS").equals("") ? (JSONArray) jobj.getJSONArray("appliedTDS") : new JSONArray();
                        if (jedTdsJsonArray.length() > 0) {
                            for (int iTds = 0; iTds < jedTdsJsonArray.length(); iTds++) {
                                JSONObject jedTdsJson = jedTdsJsonArray.getJSONObject(iTds);
                                if (jedTdsJson.has("tdsaccountid") && !StringUtil.isNullOrEmpty(jedTdsJson.getString("tdsaccountid"))) {
                                    JSONObject jedTds = new JSONObject();
                                    jedTds.put("srno", jedetails.size() + 1);
                                    jedTds.put("companyid", companyid);
                                    jedTds.put("amount", authHandler.round(jedTdsJson.getDouble("tdsamount"), companyid));
                                    jedTds.put("accountid", jedTdsJson.getString("tdsaccountid"));//Changed account Id                        
                                    jedTds.put("debit", false);
                                    jedTds.put("jeid", jeid);
                                    jedTds.put("description", "TDS Amount is Deducted");
                                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedTds);
                                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                    jedetails.add(jed);
                                    jedTdsJson.put("tdsjedid", jed.getID());
                                }
                            }
                        }
                    }

                    if (!isInvoiceIsClaimed) {
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", authHandler.round(((enterAmount - amountDiffforInv)+discountAmount), companyid));
                        jedjson.put("accountid", goodsReceipt.getAccount() != null ? goodsReceipt.getAccount().getID() : accountId);//Changed account Id
                        if (jobj.optDouble("gstCurrencyRate", 0.0) != 0.0) {
                            jedjson.put("gstCurrencyRate", jobj.optDouble("gstCurrencyRate", 0.0));
                            journalEntry.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate", 0.0));
                            jedjson.put("paymentType", type);
                        }
                        jedjson.put("forexGainLoss", amountDiffforInv);
                        jedjson.put("debit", true);
                        jedjson.put("jeid", jeid);
                        jedjson.put("description", jobj.optString("description"));
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);
                        String discountReceivedGoesToAccountId = preferences.getDiscountReceived()!= null ? preferences.getDiscountReceived().getID() : "";
                        String discountMasterAccountId = (goodsReceipt.getTermid() != null && goodsReceipt.getTermid().getDiscountName() != null) ? goodsReceipt.getTermid().getDiscountName().getAccount() : discountReceivedGoesToAccountId;
                        /**
                         * Checking whether discount field is edited or not if
                         * yes then discount is added in Discount Given goes to
                         * Account which is mapped in Company preferences else
                         * discount goes to account which is mapped in discount
                         * master. ERM-981
                         */
                        String discountAccountId = discountMasterAccountId;
                        boolean isDiscountFieldChanged = false;
                        if (isEdit) {
                            KwlReturnObject paymentDetailObj = accountingHandlerDAOobj.getObject(PaymentDetail.class.getName(), jobj.optString("rowdetailid", ""));
                            PaymentDetail paymentDetail = (PaymentDetail) paymentDetailObj.getEntityList().get(0);
                            isDiscountFieldChanged = paymentDetail != null ? paymentDetail.isDiscountFieldEdited() : false;
                            if(jobj.optBoolean("isDiscountFieldChanged")){
                                isDiscountFieldChanged = jobj.optBoolean("isDiscountFieldChanged");
                            }
                        }else{
                            isDiscountFieldChanged = jobj.optBoolean("isDiscountFieldChanged");
                        }
                        paramJobj.put("discountGivenGoesToAccountId", discountReceivedGoesToAccountId);
                        paramJobj.put("discountMasterAccountId", discountMasterAccountId);
                        paramJobj.put("isDiscountFieldChanged", isDiscountFieldChanged);
                        if(isDiscountFieldChanged){
                            discountAccountId = discountReceivedGoesToAccountId;
                        }
                        if (isDiscountAppliedOnPaymentTerms && !isInvoiceIsClaimed && (discountAmount > 0.0)) {
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", companyid);
                            jedjson.put("amount", authHandler.round(discountAmount, companyid));
                            jedjson.put("accountid", discountAccountId);
                            jedjson.put("debit", false);
                            jedjson.put("jeid", jeid);
                            jedjson.put("description", jobj.optString("description"));
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jedForDiscount = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jedForDiscount);
                        }
                        if ((!jobj.optString("documentno").equalsIgnoreCase("undefined")) && (!jobj.optString("documentno").equalsIgnoreCase(""))) {
                            billno.append(jobj.getString("documentno") + ",");
                        }
                        jArr.getJSONObject(i).put("rowjedid", jed.getID());
                        JSONArray jcustomarray = new JSONArray();

                        if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                            jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                            jcustomarrayMap.put(jed.getID(), jcustomarray);
                        }
                        jArr.getJSONObject(i).put("jedetail", jed.getID());

                        int count = jcustomarray.length();
                        
                        if (count > 0 && jcustomarray.optJSONObject(0).length() > 0) {
                            /*
                             * If Custom field is tagged then posting additional
                             * jedetail for payment method against invoice
                             * tagged with custom field. ERP-33860
                             */
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", companyid);
                            //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                            jedjson.put("amount", authHandler.round((jobj.optDouble("enteramount") - amountDiffforInv), companyid));
                            jedjson.put("accountid", dipositTo.getID());
                            jedjson.put("debit", false);
                            jedjson.put("jeid", jeid);
                            jedjson.put(Constants.ISSEPARATED, true);
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jed1 = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed1);
                            if (isDiscountAppliedOnPaymentTerms && !isInvoiceIsClaimed && (discountAmount > 0.0)) {
                                jedjson = new JSONObject();
                                jedjson.put("srno", jedetails.size() + 1);
                                jedjson.put("companyid", companyid);
                                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                                jedjson.put("amount", authHandler.round(discountAmount, companyid));
                                jedjson.put("accountid", discountAccountId);
                                jedjson.put("debit", false);
                                jedjson.put("jeid", jeid);
                                jedjson.put(Constants.ISSEPARATED, true);
                                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed2 = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jedetails.add(jed2);
                                if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", null))) {
                                    jcustomarrayMap.put(jed2.getID(), jcustomarray);
                                }
                            }
                                if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", null))) {
                                jcustomarrayMap.put(jed1.getID(), jcustomarray);
                                }
                                jArr.getJSONObject(i).put("methodjedid", jed1.getID());
                            }

                    } else {
                        // Logic of posting JE for claimed invoices
                        String badDebtRecoveredAccountId = extraCompanyPreferences.getGstBadDebtsRecoverPurchaseAccount();
                        KwlReturnObject accObj = accountingHandlerDAOobj.getObject(Account.class.getName(), badDebtRecoveredAccountId);
                        Account account = (Account) accObj.getEntityList().get(0);
                        if (account == null) {
                            throw new AccountingException("GST Bad Debt Recover Account is not available in database");
                        }
                        double amountPaidForInvoice = jobj.optDouble("enteramount") - amountDiffforInv;
                        boolean isGlobalLevelTax = false;
                         boolean isOpeningBalanceInvoice= goodsReceipt.isIsOpeningBalenceInvoice();
                        if (isOpeningBalanceInvoice) {
                            isGlobalLevelTax = true;
                        } else {
                            if (goodsReceipt.getTaxEntry() != null && goodsReceipt.getTaxEntry().getAmount() > 0) {
                                isGlobalLevelTax = true;
                            }
                        }
                        double taxAmountInInvoiceCurrency = 0d;
                        double taxAmountPaidInPaymentCurrency = 0d;
                        double taxAmountReceivedInInvoiceCurrency = 0d;
                        double totalTaxAmountReceivedInInvoiceCurrency = 0d;
                        double invoiceTotalAmountInInvoiceCurrency = isOpeningBalanceInvoice ? goodsReceipt.getOriginalOpeningBalanceAmount() :authHandler.round(goodsReceipt.getVendorEntry().getAmount(), companyid);
                        double invoiceAmountExcludingTaxInPaymentCurrency = 0.0;
                        double invoiceAmountReceivedExcludingTaxInInvoiceCurrency = 0.0;
                        amountPaidForInvoice = authHandler.round(amountPaidForInvoice, companyid);
                        if (isGlobalLevelTax) {
                            Tax gloabLevelTax = goodsReceipt.getTax();
                            if(goodsReceipt.isIsOpeningBalenceInvoice()){
                                accountIdForGst = gstInputAccountId;
                            } else {
                                accountIdForGst = gloabLevelTax.getAccount().getID();
                            }
                            taxAmountInInvoiceCurrency = authHandler.round(isOpeningBalanceInvoice?goodsReceipt.getTaxamount():goodsReceipt.getTaxEntry().getAmount(), companyid);
                            taxAmountPaidInPaymentCurrency = (taxAmountInInvoiceCurrency * amountPaidForInvoice) / invoiceTotalAmountInInvoiceCurrency;
                            taxAmountPaidInPaymentCurrency = authHandler.round(taxAmountPaidInPaymentCurrency, companyid);
                            taxAmountReceivedInInvoiceCurrency = (taxAmountInInvoiceCurrency * amountReceivedConverted) / invoiceTotalAmountInInvoiceCurrency;
                            taxAmountReceivedInInvoiceCurrency = authHandler.round(taxAmountReceivedInInvoiceCurrency, companyid);
                            totalTaxAmountReceivedInInvoiceCurrency += taxAmountReceivedInInvoiceCurrency;
                            invoiceAmountExcludingTaxInPaymentCurrency = amountPaidForInvoice - taxAmountPaidInPaymentCurrency;

                            JSONObject jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", companyid);
                            jedjson.put("accountid", badDebtRecoveredAccountId);
                            jedjson.put("amount", invoiceAmountExcludingTaxInPaymentCurrency);
                            jedjson.put("debit", true);
                            jedjson.put("jeid", jeid);
                            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jedForRecoverJe = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jedForRecoverJe);

                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", companyid);
                            jedjson.put("accountid", accountIdForGst);
                            jedjson.put("amount", taxAmountPaidInPaymentCurrency);
                            jedjson.put("debit", true);
                            jedjson.put("jeid", jeid);
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jedForTax = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jedForTax);

                            jArr.getJSONObject(i).put("rowjedid", jedForRecoverJe.getID());

                            if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                                JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                                jcustomarrayMap.put(jedForRecoverJe.getID(), jcustomarray);
                            }
                            jArr.getJSONObject(i).put("jedetail", jedForRecoverJe.getID());
                        } else {
                            JSONObject jedjson = new JSONObject();
                            invoiceAmountExcludingTaxInPaymentCurrency = amountPaidForInvoice;
                            for (GoodsReceiptDetail details : goodsReceipt.getRows()) {
                                taxAmountInInvoiceCurrency = details.getRowTaxAmount() + details.getRowTermTaxAmount();
                                taxAmountPaidInPaymentCurrency = (taxAmountInInvoiceCurrency * amountPaidForInvoice) / invoiceTotalAmountInInvoiceCurrency;
                                taxAmountPaidInPaymentCurrency = authHandler.round(taxAmountPaidInPaymentCurrency, companyid);
                                taxAmountReceivedInInvoiceCurrency = (taxAmountInInvoiceCurrency * amountReceivedConverted) / invoiceTotalAmountInInvoiceCurrency;
                                taxAmountReceivedInInvoiceCurrency = authHandler.round(taxAmountReceivedInInvoiceCurrency, companyid);
                                totalTaxAmountReceivedInInvoiceCurrency += taxAmountReceivedInInvoiceCurrency;
                                invoiceAmountExcludingTaxInPaymentCurrency -= taxAmountInInvoiceCurrency;

                                jedjson = new JSONObject();
                                jedjson.put("srno", jedetails.size() + 1);
                                jedjson.put("companyid", companyid);
                                jedjson.put("accountid", details.getTax().getAccount().getID());
                                jedjson.put("amount", taxAmountPaidInPaymentCurrency);
                                jedjson.put("debit", true);
                                jedjson.put("jeid", jeid);
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jedetails.add(jed);
                            }
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", companyid);
                            jedjson.put("accountid", badDebtRecoveredAccountId);
                            jedjson.put("amount", invoiceAmountExcludingTaxInPaymentCurrency);
                            jedjson.put("debit", true);
                            jedjson.put("jeid", jeid);
                            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jedForRecoveryJe = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jedForRecoveryJe);

                            if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                                JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                                jcustomarrayMap.put(jedForRecoveryJe.getID(), jcustomarray);
                            }
                            jArr.getJSONObject(i).put("jedetail", jedForRecoveryJe.getID());

                        }
                        invoiceAmountReceivedExcludingTaxInInvoiceCurrency = amountReceivedConverted - totalTaxAmountReceivedInInvoiceCurrency;
                        HashMap<String, Object> mappingObj = new HashMap<String, Object>();
                        mappingObj.put("companyId", companyid);
                        mappingObj.put("invoiceId", goodsReceipt.getID());
                        mappingObj.put("invoiceReceivedAmt", invoiceAmountReceivedExcludingTaxInInvoiceCurrency);
                        mappingObj.put("gstToRecover", totalTaxAmountReceivedInInvoiceCurrency);
                        mappingObj.put("recoveredDate", payment.getCreationDate());
                        mappingObj.put("badDebtType", 1);
                        mappingObj.put("paymentid", payment.getID());

                        KwlReturnObject mapResult = accGoodsReceiptobj.saveBadDebtInvoiceMapping(mappingObj);
                    }
                }
                /*
                 * Calculate Forex Gain/Loss Amount oldPaymentRowsAmount()
                 * function used only to calculated ForexExchange Gain/Loss
                 * Amount Add Single JEDetail entry with Forex amount for All
                 * Invoices only
                 */
                amountDiff = authHandler.round(oldPaymentRowsAmount(paramJobj, jArr, currencyid, externalCurrencyRate), companyid);
                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.common.forex", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
                if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                    rateDecreased = false;
                    if (amountDiff < 0) {
                        rateDecreased = true;
                    }
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    if (journalEntry.getGstCurrencyRate() != 0.0) {
                        jedjson.put("forexGainLoss", rateDecreased ? (-1 * amountDiff) : amountDiff);
                        jedjson.put("paymentType", type);
                    }
                    /*
                     * preferences.getForeignexchange().getID() - Set
                     * ForexExchange Account at company level
                     */
                    jedjson.put("accountid", preferences.getForeignexchange().getID());
                    jedjson.put("debit", rateDecreased ? false : true);
                    jedjson.put("jeid", jeid);
                    KwlReturnObject jedaddresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedaddresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            } else {
                amount = authHandler.round(Double.parseDouble(paramJobj.optString("amount","0.0")), companyid);
            }
            JSONObject jedjson = null;
            KwlReturnObject jedresult = null;
            JournalEntryDetail jed = null;
            JournalEntryDetail JEdeatilId = null;
            List payentOtherwiseList = new ArrayList();
            HashMap paymentdetailotherwise = new HashMap();

            /*
             * Below block handled for 1. Advance Payment 2. Against GL 3.
             * Against CN/DN
             *
             */

            if (type == Constants.AdvancePayment || type == Constants.GLPayment || type == Constants.PaymentAgainstCNDN) {//advance,GL,Cn
                JSONArray drAccArr = detailsJSONArray;
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    double forexgainloss = 0;

                    // ForexGainLoss if Payment Against CN/DN
                    if (type == Constants.PaymentAgainstCNDN) {
                        String transactionCurrencyId = jobj.optString("currencyidtransaction");
                        if (!StringUtil.isNullOrEmpty(transactionCurrencyId) && !transactionCurrencyId.equals(payment.getCurrency().getCurrencyID())) {
                            forexgainloss = cndnPaymentForexGailLossAmount(paramJobj, jobj, payment, transactionCurrencyId, currencyid, externalCurrencyRate);
                        } else if (transactionCurrencyId.equals(payment.getCurrency().getCurrencyID())) {
                            forexgainloss = cndnPaymentForexGailLossAmountForSameCurrency(paramJobj, jobj, payment, transactionCurrencyId, currencyid, externalCurrencyRate);
                        }
                    }
                    if (type == Constants.AdvancePayment && payment.getCustomer() != null && !StringUtil.isNullOrEmpty(jobj.optString("documentid",null))) {   // Refund type payment with Advance payment linked as reference
                        String transactionCurrencyId = jobj.optString("currencyidtransaction");
                        forexgainloss = RefundPaymentForexGailLossAmount(paramJobj, jobj, payment, transactionCurrencyId, currencyid, externalCurrencyRate);
                    }
                    boolean isdebit = jobj.has("isdebit") ? Boolean.parseBoolean(jobj.optString("isdebit","true")) : true;
                    if (type == Constants.GLPayment) {
                        isdebit = jobj.has("debit") ? Boolean.parseBoolean(jobj.optString("debit","true")) : true;
                    }
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    /*
                     * Calculate Forex Gain/Loss Amount Diff and Generate JE
                     * Details with amount = Total Enterted Amount - Forex
                     * Amount.
                     */

                    // If TDS is Applicable then adjust amount for JE Post
                    double enterAmount = authHandler.round(jobj.optDouble("enteramount",0.0), companyid);
                    boolean isTDSApplied = extraCompanyPreferences.isTDSapplicable();
                    if (isTDSApplied && jobj.has("appliedTDS") && !StringUtil.isNullOrEmpty(jobj.get("appliedTDS").toString())) {
                        JSONArray jedTdsJsonArray = jobj.has("appliedTDS") && !jobj.get("appliedTDS").equals("") ? jobj.getJSONArray("appliedTDS") : new JSONArray();
                        if (jedTdsJsonArray.length() > 0) {
                            for (int iTds = 0; iTds < jedTdsJsonArray.length(); iTds++) {
                                JSONObject jedTdsJson = jedTdsJsonArray.getJSONObject(iTds);
                                if (jedTdsJson.has("tdsaccountid") && !StringUtil.isNullOrEmpty(jedTdsJson.optString("tdsaccountid"))) {
                                    JSONObject jedTds = new JSONObject();
                                    jedTds.put("srno", jedetails.size() + 1);
                                    jedTds.put("companyid", companyid);
                                    jedTds.put("amount", authHandler.round(jedTdsJson.getDouble("tdsamount"), companyid));
                                    jedTds.put("accountid", jedTdsJson.getString("tdsaccountid"));//Changed account Id                        
                                    jedTds.put("debit", false);
                                    jedTds.put("jeid", jeid);
                                    jedTds.put("description", "TDS Amount is Deducted");
                                    KwlReturnObject jedresult2 = accJournalEntryobj.addJournalEntryDetails(jedTds);
                                    JournalEntryDetail jed2 = (JournalEntryDetail) jedresult2.getEntityList().get(0);
                                    jedetails.add(jed2);
                                    jedTdsJson.put("tdsjedid", jed2.getID());
                                }
                            }
                        }
                    }

                    double jedAmount = authHandler.round((enterAmount - forexgainloss), companyid);
                    jedjson.put("amount", jedAmount);
                    if (type == Constants.PaymentAgainstCNDN && jobj.optDouble("gstCurrencyRate", 0.0) != 0.0) {
                        jedjson.put("gstCurrencyRate", jobj.optDouble("gstCurrencyRate", 0.0));
                        journalEntry.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate", 0.0));
                        jedjson.put("paymentType", type);
                        jedjson.put("forexGainLoss", forexgainloss);
                    }
                    if (type == Constants.GLPayment) {
                        jedjson.put("accountid", jobj.optString("documentid"));//Changed account Id 
                    } else if (type == Constants.AdvancePayment) {
                        KwlReturnObject advacePaymentResult = accountingHandlerDAOobj.getObject(AdvanceDetail.class.getName(), jobj.optString("rowdetailid"));
                        AdvanceDetail advacePayment = (AdvanceDetail) advacePaymentResult.getEntityList().get(0);
                        /*
                         * Taking original account in edit and copy payment. Refer SDP-7867
                         */
                        if (advacePayment != null && advacePayment.getTotalJED() != null) {
                            jedjson.put("accountid", advacePayment.getTotalJED().getAccount() != null ? advacePayment.getTotalJED().getAccount().getID() : accountId);//Changed account Id
                        } else {
                            jedjson.put("accountid", accountId);//Changed account Id
                        }
                    } else if (type == Constants.PaymentAgainstCNDN) {
                        KwlReturnObject creditNoteResult = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), jobj.optString("documentid"));
                        CreditNote creditMemo = (CreditNote) creditNoteResult.getEntityList().get(0);
                        jedjson.put("accountid", creditMemo.getAccount() != null ? creditMemo.getAccount().getID() : accountId);//Changed account Id
                    }
                    jedjson.put("debit", isdebit);//true);
                    jedjson.put("jeid", jeid);
                    jedjson.put("description", jobj.optString("description"));
                    KwlReturnObject JEdeatilIdresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JEdeatilId = (JournalEntryDetail) JEdeatilIdresult.getEntityList().get(0);
                    jedetails.add(JEdeatilId);
                    if ((!jobj.optString("documentno").equalsIgnoreCase("undefined")) && (!jobj.optString("documentno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("documentno") + ",");
                    }
                    drAccArr.getJSONObject(i).put("rowjedid", JEdeatilId.getID());
                    JSONArray jcustomarray = null;
                    if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                        jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                        jcustomarrayMap.put(JEdeatilId.getID(), jcustomarray);
                    }
                    drAccArr.getJSONObject(i).put("jedetail", JEdeatilId.getID());
                    String paymentMethodJedId = "";
                    if (jcustomarray != null && jcustomarray.length() > 0 && jcustomarray.optJSONObject(0).length() > 0 && (type == Constants.AdvancePayment || type == Constants.GLPayment || type == Constants.PaymentAgainstCNDN)) {
                        /*
                         * If Custom field is tagged then posting additional
                         * jedetail for payment method against Advance
                         * Payment/GL Account/Debit Note tagged with custom
                         * field. ERP-33860
                         */
                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", jedAmount);
                        jedjson.put("accountid", dipositTo.getID());
                        jedjson.put("debit", !isdebit);
                        jedjson.put("jeid", jeid);
                        jedjson.put(Constants.ISSEPARATED, true);
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JournalEntryDetail jed1 = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed1);

                        paymentMethodJedId = jed1.getID();
                        jcustomarrayMap.put(paymentMethodJedId, jcustomarray);
                        drAccArr.getJSONObject(i).put("methodjedid", paymentMethodJedId);
                    }
                    if (preferences.getForeignexchange() == null) {
                        throw new AccountingException(messageSource.getMessage("acc.common.forex", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }

                    /*
                     * Add Single JEDetail entry with Forex amount for All CN/DN
                     * only
                     */
                    if (forexgainloss != 0 && preferences.getForeignexchange() != null && Math.abs(forexgainloss) >= 0.000001) {//Math.abs(forexgainloss) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                        rateDecreased = false;
                        if (forexgainloss < 0) {
                            rateDecreased = true;
                        }
                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", rateDecreased ? (-1 * forexgainloss) : forexgainloss);
                        if (type == Constants.PaymentAgainstCNDN && journalEntry.getGstCurrencyRate() != 0.0) {
                            jedjson.put("forexGainLoss", rateDecreased ? (-1 * forexgainloss) : forexgainloss);
                            jedjson.put("paymentType", type);
                        }
                        jedjson.put("accountid", preferences.getForeignexchange().getID());
                        jedjson.put("debit", rateDecreased ? false : true);
                        jedjson.put("jeid", jeid);
                        KwlReturnObject jedForexResult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedForexResult.getEntityList().get(0);
                        jedetails.add(jed);
                    }

                    double rowtaxamount = 0;
                    if (type == Constants.GLPayment) {
                        PaymentDetailOtherwise paymentDetailOtherwise = null;
                        String rowtaxid = jobj.optString("prtaxid");
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                        Tax rowtax = (Tax) txresult.getEntityList().get(0);
                        String appliedGst = jobj.optString("appliedGst", "");
                        KwlReturnObject gstresult = null;
                        
                        /*If Tax is not present in ERP side then we simply retrun with info code   */
                        Tax tax = null;
                        if (!StringUtil.isNullOrEmpty(appliedGst)) {
                            gstresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), appliedGst);
                            tax = (Tax) gstresult.getEntityList().get(0);
                            if (StringUtil.isNullObject(tax) && isFromEclaim) {
                                throw ServiceException.FAILURE("{\\\"success\\\": false,\\\"msg\\\": Selected tax code is not present in ERP., \\\"errorcode\\\": \\\"e350\\\"}", "erp30", false);
                            }
                        }
                        
                        if (rowtax == null || rowtaxid.equalsIgnoreCase("-1")) {
                            paymentdetailotherwise.put("amount", Double.parseDouble(jobj.optString("enteramount","0.0")));
                            paymentdetailotherwise.put("taxjedid", "");
                            paymentdetailotherwise.put("tax", rowtaxid.equalsIgnoreCase("-1") ? "None" : "");
                            paymentdetailotherwise.put("accountid", jobj.getString("documentid"));
                            paymentdetailotherwise.put("isdebit", isdebit);
                            paymentdetailotherwise.put("taxamount", rowtaxamount);
                            paymentdetailotherwise.put("tdsamount", jobj.optDouble("tdsamount", 0.0));
                            paymentdetailotherwise.put("description", jobj.optString("description"));
                            paymentdetailotherwise.put("payment", payment.getID());
                            if (jobj.has("srNoForRow")) {
                                int srNoForRow = StringUtil.isNullOrEmpty(jobj.optString("srNoForRow",null)) ? 0 : Integer.parseInt(jobj.optString("srNoForRow"));
                                paymentdetailotherwise.put("srNoForRow", srNoForRow);
                            }
                            if (gstresult !=null && !gstresult.getEntityList().isEmpty() && gstresult.getEntityList().get(0) != null) {
                                Tax gstAppliedObj = (Tax) gstresult.getEntityList().get(0);
                                if (gstAppliedObj != null) {
                                    paymentdetailotherwise.put("gstApplied", gstAppliedObj);
                                }
                            }
                            if (jobj.has("jedetail") && jobj.get("jedetail") != null) {
                                paymentdetailotherwise.put("jedetail", (String) jobj.get("jedetail"));
                            } else {
                                paymentdetailotherwise.put("jedetail", "");
                            }
                            result = accVendorPaymentobj.savePaymentDetailOtherwise(paymentdetailotherwise);
                            paymentdetailotherwise.clear();
                            paymentDetailOtherwise = (PaymentDetailOtherwise) result.getEntityList().get(0);
                            payentOtherwiseList.add(paymentDetailOtherwise.getID());

                            if (jobj.has("appliedTDS") && !StringUtil.isNullOrEmpty(jobj.optString("appliedTDS",null))) {   // TDS Applicable Flow Start with GL
                                JSONArray jsonArray = new JSONArray(jobj.getString("appliedTDS"));
                                JSONObject tdsDetails = new JSONObject();
                                tdsDetails.put("appliedTDS", jsonArray);
                                tdsDetails.put("documenttype", jobj.getString("type"));

                                HashMap<String, Object> tdsRequestParams = new HashMap();
                                tdsRequestParams.put("tdsDetailsJsonObj", tdsDetails);
                                tdsRequestParams.put("paymentDetailOtherwiseObj", paymentDetailOtherwise);
                                tdsRequestParams.put("companyObj", payment.getCompany());
                                HashSet tdsdetails = saveTDSdetailsRow(tdsRequestParams);
                                if (!tdsdetails.isEmpty() || tdsdetails.size() > 0) {
                                    paymentDetailOtherwise.setTdsdetails(tdsdetails);
                                }
                            }

                        } else {
                            rowtaxamount = Double.parseDouble(jobj.optString("taxamount","0.0"));
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", company.getCompanyID());
                            jedjson.put("amount", authHandler.formattedAmount(rowtaxamount, companyid));
                            jedjson.put("accountid", rowtax.getAccount().getID());
                            jedjson.put("debit", isdebit);//true);
                            jedjson.put("jeid", jeid);
                            jedjson.put("description", jobj.optString("description"));
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);
                            paymentdetailotherwise.put("amount", Double.parseDouble(jobj.optString("enteramount","0.0")));
                            paymentdetailotherwise.put("tdsamount", jobj.optDouble("tdsamount", 0.0));
                            paymentdetailotherwise.put("taxjedid", jed.getID());
                            paymentdetailotherwise.put("tax", rowtax.getID());
                            paymentdetailotherwise.put("accountid", jobj.optString("documentid"));
                            paymentdetailotherwise.put("isdebit", isdebit);
                            paymentdetailotherwise.put("taxamount", rowtaxamount);
                            paymentdetailotherwise.put("description", jobj.optString("description"));
                            paymentdetailotherwise.put("payment", payment.getID());
                            if (jobj.has("srNoForRow")) {
                                int srNoForRow = StringUtil.isNullOrEmpty(jobj.optString("srNoForRow",null)) ? 0 : Integer.parseInt(jobj.optString("srNoForRow","0"));
                                paymentdetailotherwise.put("srNoForRow", srNoForRow);
                            }
                            Tax gstAppliedObj = null;
                            if (gstresult !=null && !gstresult.getEntityList().isEmpty() && gstresult.getEntityList().get(0) != null) {
                                gstAppliedObj = (Tax) gstresult.getEntityList().get(0);
                            }
                            if (gstAppliedObj != null) {
                                paymentdetailotherwise.put("gstApplied", gstAppliedObj);
                            } else {
                                paymentdetailotherwise.put("gstApplied", rowtax);
                            }
                            if (jobj.has("jedetail") && jobj.get("jedetail") != null) {
                                paymentdetailotherwise.put("jedetail", (String) jobj.get("jedetail"));
                            } else {
                                paymentdetailotherwise.put("jedetail", "");
                            }
                            paymentdetailotherwise.put("taxjedetail", jed.getID());
                            result = accVendorPaymentobj.savePaymentDetailOtherwise(paymentdetailotherwise);
                            paymentdetailotherwise.clear();
                            paymentDetailOtherwise = (PaymentDetailOtherwise) result.getEntityList().get(0);
                            payentOtherwiseList.add(paymentDetailOtherwise.getID());
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            if (jcustomarray !=null && jcustomarray.length() > 0 && jcustomarray.optJSONObject(0).length() > 0) {
                                /*
                                 * If Custom field is tagged then posting
                                 * additional jedetail for payment method against tax
                                 * for Advance Payment/GL Account/Debit Note
                                 * tagged with custom field. ERP-34425
                                 */
                                jedjson = new JSONObject();
                                jedjson.put("srno", jedetails.size() + 1);
                                jedjson.put("companyid", companyid);
                                jedjson.put("amount", rowtaxamount);
                                jedjson.put("accountid", dipositTo.getID());
                                jedjson.put("debit", !isdebit);
                                jedjson.put("jeid", jeid);
                                jedjson.put(Constants.ISSEPARATED, true);
                                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail pmAmoutForTaxJed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jedetails.add(pmAmoutForTaxJed);
     
                                if (isFromEclaim) {
                                    jcustomarray =fieldDataManagercntrl.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_Make_Payment_ModuleId, companyid, false);
                                }
                                customrequestParams.put("customarray", jcustomarray);
                                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                                customrequestParams.put("modulerecid", jed.getID());
                                customrequestParams.put("recdetailId", paymentDetailOtherwise.getID());
                                customrequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                                customrequestParams.put("companyid", companyid);
                                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                    KwlReturnObject receiptAccJECustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), jed.getID());
                                    AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJECustomData.getEntityList().get(0);
                                    jed.setAccJEDetailCustomData(accJEDetailCustomData);
                                }
                                customrequestParams.put("modulerecid", pmAmoutForTaxJed.getID());
                                customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                    KwlReturnObject receiptAccJECustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), pmAmoutForTaxJed.getID());
                                    AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJECustomData.getEntityList().get(0);
                                    pmAmoutForTaxJed.setAccJEDetailCustomData(accJEDetailCustomData);
                                }
                            }

                        }
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        if (jcustomarray != null && jcustomarray.length() > 0) {
                            if (isFromEclaim) {
                                jcustomarray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_Make_Payment_ModuleId, companyid, false);
                            }
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                            customrequestParams.put("modulerecid", JEdeatilId.getID());
                            customrequestParams.put("recdetailId", paymentDetailOtherwise.getID());
                            customrequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                            customrequestParams.put("companyid", companyid);
                            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                KwlReturnObject receiptAccJECustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), JEdeatilId.getID());
                                AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJECustomData.getEntityList().get(0);
                                JEdeatilId.setAccJEDetailCustomData(accJEDetailCustomData);
                            }

                            if (!StringUtil.isNullOrEmpty(paymentMethodJedId)) {
                                /*
                                 * Mapping custom field for additional jedetail
                                 * of payment method for payment against GL
                                 * account
                                 */
                                customrequestParams.put("modulerecid", paymentMethodJedId);
                                customrequestParams.put("recdetailId", paymentDetailOtherwise.getID());
                                customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                    KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), jobj.optString("methodjedid"));
                                    AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                                    KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), jobj.optString("methodjedid"));
                                    JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                                    journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
                                }
                            }
                        }
                        
                        // For line level custom field come from eclaim
                        String customfield = paramJobj.optString("customfieldmap", null);
                        if (!StringUtil.isNullOrEmpty(customfield)) {
                            jcustomarray = new JSONArray(customfield);
                            if (jcustomarray.length() > 0) {
                                HashMap<String, Object> ClaimsCustomRequestParams = new HashMap<String, Object>();
                                jcustomarray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_Make_Payment_ModuleId, companyid, false);
                                ClaimsCustomRequestParams.put("customarray", jcustomarray);
                                ClaimsCustomRequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                                ClaimsCustomRequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                                ClaimsCustomRequestParams.put("modulerecid", JEdeatilId.getID());
                                ClaimsCustomRequestParams.put("recdetailId", paymentDetailOtherwise.getID());
                                ClaimsCustomRequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                                ClaimsCustomRequestParams.put("companyid", companyid);
                                ClaimsCustomRequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(ClaimsCustomRequestParams);
                                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                    KwlReturnObject receiptAccJECustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), JEdeatilId.getID());
                                    AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJECustomData.getEntityList().get(0);
                                    JEdeatilId.setAccJEDetailCustomData(accJEDetailCustomData);
                                }
                            }
                        }
                    }
                    if ((countryID.equalsIgnoreCase("" + Constants.indian_country_id))
                            && type == Constants.AdvancePayment && jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty((String) jobj.optString("LineTermdetails"))) {
                        JSONArray termsArray = new JSONArray(StringUtil.DecodeText((String) jobj.optString("LineTermdetails")));
                        double termamount = 0.0;
                        for (int j = 0; j < termsArray.length(); j++) {
                            JSONObject termObject = termsArray.getJSONObject(j);
                            jedjson = new JSONObject();
                            double termamt = termObject.optDouble("termamount");
                            if (payment.getPaymentWindowType()==2) {
                                /**
                                 * In refund case need to calculate Term Amount based on Enter amount
                                 */
                                termamt = enterAmount * termObject.optDouble("termpercentage") / 100;
                                termamt = authHandler.round(termamt, companyid);
                            }
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", companyid);
                            termamount += termamt;
                            jedjson.put("amount", termamt);
                            jedjson.put("accountid", termObject.get("payableaccountid"));
                            String payableaccountid = termObject.optString("payableaccountid","");
                            /**
                             * Throw exception with proper message. if payable account not present
                             */
                            if (StringUtil.isNullOrEmpty(payableaccountid)) {
                                throw new AccountingException(messageSource.getMessage("acc.common.advancepayableaccountnotmappedtoterm", new Object[]{termObject.optString("term")}, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                            }
                            if (!StringUtil.isNullOrEmpty(payment.getCustomer())) {
                                jedjson.put("debit", false);
                            } else {
                                jedjson.put("debit", true);
                            }
                            jedjson.put("jeid", jeid);
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jed1 = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed1);

                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", companyid);
                            jedjson.put("amount", termamt);
                            jedjson.put("accountid", termObject.get("accountid"));
                            if (!StringUtil.isNullOrEmpty(payment.getCustomer())) {
                                jedjson.put("debit", true);
                            } else {
                                jedjson.put("debit", false);
                            }
                            jedjson.put("jeid", jeid);
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed1 = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed1);
                        }
                        payment.setTaxAmount(termamount);
                    }
                    
                    if (countryID.equalsIgnoreCase("" + Constants.indian_country_id) && type == Constants.GLPayment && payment.isAdvanceToVendor()) {
                        JSONObject json = new JSONObject();
                        json.put(Constants.companyKey,companyid);
                        json.put("salesorpurchase", 1);
                        json.put("accountid", jobj.getString("documentid"));
                        KwlReturnObject resultObject = accAccountDAOobj.getLineLevelTerms(json);
                        if (resultObject != null && resultObject.getEntityList() != null && !resultObject.getEntityList().isEmpty()) {
                            LineLevelTerms outputTerm = (LineLevelTerms) resultObject.getEntityList().get(0);
                            if (outputTerm != null && outputTerm.getDefaultTerms()!=null && !StringUtil.isNullOrEmpty(outputTerm.getDefaultTerms().getOppositeTermId())) {
                                json = new JSONObject();
                                json.put(Constants.companyKey, companyid);
                                json.put("defaultterm", outputTerm.getDefaultTerms().getOppositeTermId());
                                resultObject = accAccountDAOobj.getLineLevelTerms(json);
                                if (resultObject != null && result.getEntityList() != null && !resultObject.getEntityList().isEmpty()) {
                                    LineLevelTerms inputTerm = (LineLevelTerms) resultObject.getEntityList().get(0);
                                    if (inputTerm != null) {
                                        if (inputTerm.getAccount() != null) {
                                            jedjson = new JSONObject();
                                            jedjson.put("srno", jedetails.size() + 1);
                                            jedjson.put("companyid", companyid);
                                            jedjson.put("amount", jedAmount);
                                            jedjson.put("accountid", inputTerm.getAccount().getID());
                                            jedjson.put("debit", true);
                                            jedjson.put("jeid", jeid);
                                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                            JournalEntryDetail jed1 = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                            jedetails.add(jed1);
                                        } else {
                                            throw new AccountingException(messageSource.getMessage("acc.common.termaccountnotmappedtoterm", new Object[]{inputTerm.getTerm()}, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                                        }
                                        if (inputTerm.getPayableAccount() != null) {
                                            jedjson = new JSONObject();
                                            jedjson.put("srno", jedetails.size() + 1);
                                            jedjson.put("companyid", companyid);
                                            jedjson.put("amount", jedAmount);
                                            jedjson.put("accountid", inputTerm.getPayableAccount().getID());
                                            jedjson.put("debit", false);
                                            jedjson.put("jeid", jeid);
                                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                            JournalEntryDetail jed1 = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                            jedetails.add(jed1);
                                        } else {
                                            throw new AccountingException(messageSource.getMessage("acc.common.advancepayableaccountnotmappedtoterm", new Object[]{inputTerm.getTerm()}, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                                        }
                                    }
                                }
                            }
                        } else {
                            throw new AccountingException(messageSource.getMessage("acc.common.accountnotmappedtoterm", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                        }
                    }
                }
            }
            /*
             * If balace Amount is Greater than Zero then balace amount is save
             * in respective selected account in company preferance.
             */
            if (type == Constants.BALACEAMOUNT) {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", company.getCompanyID());
                jedjson.put("amount", balaceAmount);
                jedjson.put("accountid", accountIdComPreAdjPay);
                jedjson.put("debit", true);//true);
                jedjson.put("jeid", jeid);
                jedjson.put("description", "");
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
            }
        } catch(AccountingException ex){
            throw new AccountingException(ex.getMessage());
        }catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return jedetails;
    }

   public HashSet paymentDetailObject(JSONObject paramJobj, JSONArray jSONArrayAgainstInvoice, Payment editPaymentObject, int type) throws SessionExpiredException, ServiceException, AccountingException {
        HashSet payDetails = null;
        Invoice invoice = null;
        try {

            String companyid = paramJobj.optString(Constants.companyKey);
            boolean isMultiDebit = StringUtil.getBoolean(paramJobj.optString("ismultidebit"));
            String methodid = paramJobj.optString("pmtmethod");
            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap)) {
                sessionHandlerImpl sess = new sessionHandlerImpl();
                sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
            }

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            JSONArray jArr = new JSONArray();
            if (jSONArrayAgainstInvoice != null) {
                jArr = jSONArrayAgainstInvoice;
            }
            payDetails = savePaymentRows(paramJobj, editPaymentObject, company, jArr, isMultiDebit, invoice, type);
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return payDetails;
    }

    public HashSet<AdvanceDetail> advanceDetailObject(JSONObject paramJobj, JSONArray jSONArrayAdvance, Payment payment, int type) throws SessionExpiredException, ServiceException, AccountingException {
        HashSet advanceDetails = null;
        try {
            advanceDetails = new HashSet<AdvanceDetail>();
            String companyid = paramJobj.optString(Constants.companyKey);
            Map<String, Object> counterMap = new HashMap<>();
            counterMap.put("counter", 0);//this is used to avoid JE Sequence Number
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            String countryID = preferences != null && preferences.getCompany() != null && preferences.getCompany().getCountry() != null ? preferences.getCompany().getCountry().getID() : "";
            for (int i = 0; i < jSONArrayAdvance.length(); i++) {
                JSONObject jobj = jSONArrayAdvance.getJSONObject(i);
                double amountReceived = jobj.getDouble("enteramount");
                double tdsamt = jobj.optDouble("tdsamount", 0.0);
                AdvanceDetail advanceDetail = new AdvanceDetail();
                advanceDetail.setId(StringUtil.generateUUID());
                advanceDetail.setCompany(payment.getCompany());
                advanceDetail.setPayment(payment);
                advanceDetail.setAmount(amountReceived);
                advanceDetail.setExchangeratefortransaction(1.0d);
                advanceDetail.setProductId(jobj.optString("productid", null));
                advanceDetail.setTdsamount(tdsamt);
                /*
                 * If Make Payment against Customer and used advance receipt
                 * against it then need to maintain receipt object for reference
                 */
                if (!StringUtil.isNullOrEmpty(jobj.optString("documentid", ""))) {
                    KwlReturnObject result = accountingHandlerDAOobj.getObject(ReceiptAdvanceDetail.class.getName(), jobj.getString("documentid"));
                    ReceiptAdvanceDetail receiptadvancedetail = (ReceiptAdvanceDetail) result.getEntityList().get(0);
                    advanceDetail.setReceiptAdvanceDetails(receiptadvancedetail);
                    double amountReceivedConverted = jobj.getDouble("enteramount");
                    if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(receiptadvancedetail.getReceipt().getCurrency().getCurrencyID()) && !receiptadvancedetail.getReceipt().getCurrency().getCurrencyID().equals(payment.getCurrency().getCurrencyID())) {
                        double adjustedRate = Double.parseDouble(jobj.optString("exchangeratefortransaction", "1.0").toString());//jobj.optDouble("amountdue",0)/jobj.optDouble("amountDueOriginal",0);
                        amountReceivedConverted = amountReceived / adjustedRate;
                        amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                        advanceDetail.setExchangeratefortransaction(adjustedRate);
                    }
                    //JE For Receipt which is of Opening Type
                    if (receiptadvancedetail != null) {
                        double exchangeratefortransaction = Double.parseDouble(jobj.optString("exchangeratefortransaction", "1"));
                        String basecurrency = paramJobj.optString(Constants.globalCurrencyKey);
                        double finalAmountReval = ReevalJournalEntryForAdvanceReceipt(paramJobj, receiptadvancedetail.getReceipt(), amountReceived, exchangeratefortransaction);
                        if (finalAmountReval != 0) {
                            /**
                             * added transactionID and transactionModuleID to
                             * Realised JE.
                             */
                            counterMap.put("transactionModuleid", receiptadvancedetail.getReceipt().isIsOpeningBalenceReceipt() ? Constants.Acc_opening_Receipt : Constants.Acc_Receive_Payment_ModuleId);
                            counterMap.put("transactionId", receiptadvancedetail.getReceipt().getID());
                            String revaljeid = PostJEFORReevaluation(paramJobj, finalAmountReval, companyid, preferences, basecurrency, advanceDetail.getRevalJeId(), counterMap);
                            advanceDetail.setRevalJeId(revaljeid);
                        }
                    }
                    if (payment.getApprovestatuslevel() == Constants.APPROVED_STATUS_LEVEL) {
                        receiptadvancedetail.setAmountDue(receiptadvancedetail.getAmountDue() - amountReceivedConverted);
                    }
                }

                /*
                 * Make Payment against vendor - amountdue is same as paid
                 * amount. User can link this advance amount aginst invoice Make
                 * Payment against customer - deposite/refund amount will no be
                 * used for other transactions. so need to set amountdue as 0
                 */
                if (payment.getVendor() != null) {
                    advanceDetail.setAmountDue(amountReceived);
                } else if (payment.getCustomer() != null && advanceDetail.getReceiptAdvanceDetails() == null) { // set amount due to refund payment as entrered amount if no document is linked to refund payment while creating
                    advanceDetail.setAmountDue(amountReceived);
                } else if (payment.getCustomer() != null) {
                    advanceDetail.setAmountDue(0);
                }
                if (jobj.has("jedetail") && jobj.get("jedetail") != null) {
                    KwlReturnObject resJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) jobj.get("jedetail"));
                    JournalEntryDetail jedObj = (JournalEntryDetail) resJED.getEntityList().get(0);
                    advanceDetail.setTotalJED(jedObj);
                }
                advanceDetails.add(advanceDetail);
                advanceDetail.setDescription(StringUtil.DecodeText(jobj.optString("description")));
                if (jobj.has("rowjedid")) {
                    advanceDetail.setROWJEDID(jobj.getString("rowjedid"));
                }
                if (jobj.has("srNoForRow")) {
                    int srNoForRow = StringUtil.isNullOrEmpty("srNoForRow") ? 0 : Integer.parseInt(jobj.getString("srNoForRow"));
                    advanceDetail.setSrNoForRow(srNoForRow);
                }
                HashMap<String, JSONArray> jcustomarrayMap = payment.getJcustomarrayMap();
                JSONArray jcustomarray = jcustomarrayMap.get(advanceDetail.getROWJEDID());
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                customrequestParams.put("modulerecid", advanceDetail.getROWJEDID());
                customrequestParams.put("recdetailId", advanceDetail.getId());
                customrequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), advanceDetail.getROWJEDID());
                    AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                    KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), advanceDetail.getROWJEDID());
                    JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                    journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
                }

                if (jobj.has("methodjedid") && !StringUtil.isNullOrEmpty(jobj.optString("methodjedid"))) {
                    /*
                     * Mapping custom field for additional jedetail of payment
                     * method for payment against Advance Payment
                     */
                    customrequestParams.put("modulerecid", jobj.optString("methodjedid"));
                    customrequestParams.put("recdetailId", advanceDetail.getId());
                    customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), jobj.optString("methodjedid"));
                        AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                        KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), jobj.optString("methodjedid"));
                        JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                        journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
                    }
                }

                if (jobj.has("appliedTDS") && !StringUtil.isNullOrEmpty(jobj.getString("appliedTDS"))) {   // TDS Applicable Flow Start in Advance Details...
                    JSONArray jsonArray = new JSONArray(jobj.getString("appliedTDS"));
                    JSONObject tdsDetails = new JSONObject();
                    tdsDetails.put("appliedTDS", jsonArray);
                    tdsDetails.put("documenttype", jobj.getString("type"));

                    HashMap<String, Object> tdsRequestParams = new HashMap();
                    tdsRequestParams.put("tdsDetailsJsonObj", tdsDetails);
                    tdsRequestParams.put("advanceDetailObj", advanceDetail);
                    tdsRequestParams.put("companyObj", payment.getCompany());
                    HashSet tdsdetails = saveTDSdetailsRow(tdsRequestParams);
                    if (!tdsdetails.isEmpty() || tdsdetails.size() > 0) {
                        advanceDetail.setTdsdetails(tdsdetails);
                    }
                }
                if ((countryID.equalsIgnoreCase("" + Constants.indian_country_id))
                        && jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty((String) jobj.optString("LineTermdetails"))) {
                    JSONArray termsArray = new JSONArray(StringUtil.DecodeText((String) jobj.optString("LineTermdetails")));
                    for (int j = 0; j < termsArray.length(); j++) {
                        HashMap<String, Object> purchaseOrderDetailsTermsMap = new HashMap<>();
                        JSONObject termObject = termsArray.getJSONObject(j);
                        if (termObject.has("termid")) {
                            purchaseOrderDetailsTermsMap.put("term", termObject.get("termid"));
                        }
                        if (termObject.has("termamount")) {
                            purchaseOrderDetailsTermsMap.put("termamount", termObject.get("termamount"));
                        }
                        if (termObject.has("termpercentage")) {
                            purchaseOrderDetailsTermsMap.put("termpercentage", termObject.get("termpercentage"));
                        }
                        if (termObject.has("purchasevalueorsalevalue")) {
                            purchaseOrderDetailsTermsMap.put("purchasevalueorsalevalue", termObject.get("purchasevalueorsalevalue"));
                        }
                        if (termObject.has("deductionorabatementpercent")) {
                            purchaseOrderDetailsTermsMap.put("deductionorabatementpercent", termObject.get("deductionorabatementpercent"));
                        }
                        if (termObject.has("assessablevalue")) {
                            purchaseOrderDetailsTermsMap.put("assessablevalue", termObject.get("assessablevalue"));
                        }
                        if (termObject.has("taxtype") && !StringUtil.isNullOrEmpty(termObject.getString("taxtype"))) {
                            purchaseOrderDetailsTermsMap.put("taxtype", termObject.getInt("taxtype"));
                            if (termObject.has("taxvalue") && !StringUtil.isNullOrEmpty(termObject.getString("taxvalue"))) {
                                if (termObject.getInt("taxtype") == 0) { // If Flat
                                    purchaseOrderDetailsTermsMap.put("termamount", termObject.getDouble("taxvalue"));
                                } else { // Else Percentage
                                    purchaseOrderDetailsTermsMap.put("termpercentage", termObject.getDouble("taxvalue"));
                                }
                            }
                        }
                        if (termObject.has("id")) {
                            purchaseOrderDetailsTermsMap.put("id", termObject.get("id"));
                        }
                        purchaseOrderDetailsTermsMap.put("advanceDetail", advanceDetail.getId());
                        purchaseOrderDetailsTermsMap.put("isDefault", termObject.optString("isDefault", "false"));
                        purchaseOrderDetailsTermsMap.put("productentitytermid", termObject.optString("productentitytermid"));
                        purchaseOrderDetailsTermsMap.put("userid", payment.getCreatedby().getUserID());
                        purchaseOrderDetailsTermsMap.put("product", termObject.opt("productid"));
                        purchaseOrderDetailsTermsMap.put("createdOn", new Date());

                        accVendorPaymentobj.saveAdvanceDetailsTermMap(purchaseOrderDetailsTermsMap);
                    }
                    double recTermAmount = jobj.optDouble("recTermAmount", 0.0);
                    advanceDetail.setTaxamount(recTermAmount);
                }
                if ((countryID.equalsIgnoreCase("" + Constants.indian_country_id))) {
                    /**
                     * Save GST History Customer/Vendor data.
                     */
                    jobj.put("detaildocid", advanceDetail.getId());
                    jobj.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                    fieldDataManagercntrl.createRequestMapToSaveTaxClassHistory(jobj);
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return advanceDetails;
    }

    public void saveCNDNDetailObject(JSONObject paramJobj, JSONArray jSONArrayAgainstCNDN, Payment payment, int type, Map<String, Object> counterMap) throws SessionExpiredException, ServiceException, AccountingException {
        try {
            boolean isAgainstDN = StringUtil.getBoolean(paramJobj.optString("isAgainstDN"));
            String methodid = paramJobj.optString("pmtmethod");
            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap)) {
                sessionHandlerImpl sess = new sessionHandlerImpl();
                sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
            }
            String companyid = paramJobj.optString(Constants.companyKey);
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyResult.getEntityList().get(0);
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            StringBuffer billno = new StringBuffer();
            if (type == Constants.PaymentAgainstCNDN) {
                JSONArray drAccArr = jSONArrayAgainstCNDN;
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    double paidncamount = Double.parseDouble(jobj.optString("enteramount","0.0"));
                    double tdsamt = jobj.optDouble("tdsamount", 0.0);
                    double amountdue = Double.parseDouble(jobj.optString("amountdue","0.0"));
                    String cnnoteid = jobj.optString("documentid");
                    String paymentId = payment.getID();
                    int srNoForRow = StringUtil.isNullOrEmpty(jobj.optString("srNoForRow",null)) ? 0 : Integer.parseInt(jobj.optString("srNoForRow","0"));
                    if ((!jobj.optString("documentno").equalsIgnoreCase("undefined")) && (!jobj.optString("documentno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("documentno") + ",");
                    }
                    KwlReturnObject cnResult = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnnoteid);
                    CreditNote creditNote = (CreditNote) cnResult.getEntityList().get(0);
                    String tocurrency = payment.getCurrency().getCurrencyID();
                    String fromcurrency = payment.getCurrency().getCurrencyID();
                    double exchangeratefortransaction = 1;
                    double amountinpaymentcurrency = amountdue;
                    double paidamountinpaymentcurrency = paidncamount;
                    if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(jobj.optString("currencyidtransaction", "")) && !jobj.optString("currencyidtransaction", "").equals(payment.getCurrency().getCurrencyID())) {
                        tocurrency = jobj.optString("currencyidtransaction", "");
                        fromcurrency = payment.getCurrency().getCurrencyID();
                        exchangeratefortransaction = Double.parseDouble(jobj.optString("exchangeratefortransaction", "1"));
                        amountdue = amountinpaymentcurrency / Double.parseDouble(jobj.optString("exchangeratefortransaction","1").toString());
                        amountdue = authHandler.round(amountdue, companyid);
                        paidncamount = paidamountinpaymentcurrency / Double.parseDouble(jobj.optString("exchangeratefortransaction","1").toString());
                        paidncamount = authHandler.round(paidncamount, companyid);
                    }
                    /*
                     * Amount paid against CN will be converted to base currency
                     * as per spot rate of CN
                     */
                    double cnExternalCurrencyRate = 0d;
                    Date cnCreationDate = null;
                    cnCreationDate = creditNote.getCreationDate();
                    if (creditNote.isIsOpeningBalenceCN()) {
                        cnExternalCurrencyRate = creditNote.getExchangeRateForOpeningTransaction();
                    } else {
                        cnExternalCurrencyRate = creditNote.getJournalEntry().getExternalCurrencyRate();
                    }
                    if (creditNote.isIsOpeningBalenceCN()) {
                        if (creditNote.isConversionRateFromCurrencyToBase()) {
                            cnExternalCurrencyRate = 1 / cnExternalCurrencyRate;
                        }
                    }
                    double amountPaidAgainstCNInPaymentCurrency = Double.parseDouble(jobj.optString("enteramount","0.0"));;
                    double amountPaidAgainstCNInBaseCurrency = amountPaidAgainstCNInPaymentCurrency;
                    KwlReturnObject bAmt = null;
                    HashMap<String, Object> requestParams = new HashMap();
                    requestParams.put(Constants.companyid, companyid);
                    requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, paidncamount, creditNote.getCurrency().getCurrencyID(), cnCreationDate, cnExternalCurrencyRate);
                    amountPaidAgainstCNInBaseCurrency = (Double) bAmt.getEntityList().get(0);
                    amountPaidAgainstCNInBaseCurrency = authHandler.round(amountPaidAgainstCNInBaseCurrency, companyid);
                    //Revalution Journal Entrey for Receipt to Customer for debit notes
                    ReevalJournalEntryCNDN(paramJobj, creditNote, preferences, exchangeratefortransaction, amountPaidAgainstCNInPaymentCurrency, counterMap);

                    if (isAgainstDN) {
                        KwlReturnObject cnjedresult = accPaymentDAOobj.updateDnAmount(cnnoteid, paidncamount);
                        KwlReturnObject opencnjedresult = accPaymentDAOobj.updateDnOpeningAmountDue(cnnoteid, paidncamount);
                        cnjedresult = accVendorPaymentobj.saveVendorDnPaymenyHistory(cnnoteid, paidncamount, amountdue, paymentId);
                    } else {// make payment against vendor credit note and also customer credit note.
                        KwlReturnObject cnjedresult = null;
                        if (payment.getApprovestatuslevel() == 11) {
                            cnjedresult = accPaymentDAOobj.updateCnAmount(cnnoteid, paidncamount);
                            KwlReturnObject opencnjedresult = accPaymentDAOobj.updateCnOpeningAmountDue(cnnoteid, paidncamount);
                            KwlReturnObject openingCnBaseAmtDueResult = accPaymentDAOobj.updateCnOpeningBaseAmountDue(cnnoteid, amountPaidAgainstCNInBaseCurrency);
                        }
                        HashMap<String, String> hashMapCnPayment = new HashMap<String, String>();
                        hashMapCnPayment.put("cnnoteid", cnnoteid);
                        hashMapCnPayment.put("paymentId", paymentId);
                        hashMapCnPayment.put("tdsamount", tdsamt + "");
                        hashMapCnPayment.put("originalamountdue", amountdue + "");
                        hashMapCnPayment.put("paidncamount", paidncamount + "");
                        hashMapCnPayment.put("tocurrency", tocurrency);
                        hashMapCnPayment.put("fromcurrency", fromcurrency);
                        hashMapCnPayment.put("exchangeratefortransaction", exchangeratefortransaction + "");
                        hashMapCnPayment.put("amountinpaymentcurrency", amountinpaymentcurrency + "");
                        hashMapCnPayment.put("paidamountinpaymentcurrency", paidamountinpaymentcurrency + "");
                        hashMapCnPayment.put("amountinbasecurrency", amountPaidAgainstCNInBaseCurrency + "");
                        hashMapCnPayment.put("description", jobj.optString("description"));
                        hashMapCnPayment.put("gstCurrencyRate", jobj.optString("gstCurrencyRate", "0.0"));
                        hashMapCnPayment.put("srNoForRow", "" + srNoForRow);
                        if (jobj.has("jedetail") && jobj.get("jedetail") != null) {
                            hashMapCnPayment.put("jedetail", (String) jobj.get("jedetail"));
                        } else {
                            hashMapCnPayment.put("jedetail", "");
                        }
                        cnjedresult = accVendorPaymentobj.saveVendorCnPaymenyHistory(hashMapCnPayment);

                        CreditNotePaymentDetails creditNotePaymentDetails = null;
                        List list = cnjedresult.getEntityList();
                        if (list != null && !list.isEmpty()) {
                            String uuid = (String) cnjedresult.getEntityList().get(0);
                            KwlReturnObject cnPaymentDetails = accountingHandlerDAOobj.getObject(CreditNotePaymentDetails.class.getName(), uuid);
                            creditNotePaymentDetails = (CreditNotePaymentDetails) cnPaymentDetails.getEntityList().get(0);
                        }

                        KwlReturnObject kwlTDSDetails = null;
                        if (jobj.has("appliedTDS") && !StringUtil.isNullOrEmpty(jobj.getString("appliedTDS"))) {   // TDS Applicable Flow Start in Credit Note
                            JSONArray jsonArray = new JSONArray(jobj.getString("appliedTDS"));
                            JSONObject tdsDetails = new JSONObject();
                            tdsDetails.put("appliedTDS", jsonArray);
                            tdsDetails.put("documenttype", jobj.getString("type"));

                            HashMap<String, Object> tdsRequestParams = new HashMap();
                            tdsRequestParams.put("tdsDetailsJsonObj", tdsDetails);
                            tdsRequestParams.put("creditNotePaymentDetailsObj", creditNotePaymentDetails);
                            tdsRequestParams.put("companyObj", payment.getCompany());
                            kwlTDSDetails = accVendorPaymentobj.saveTDSdetailsRow(tdsRequestParams);
                        }
                    }
                    String rowJeId = rowJeId = jobj.optString("rowjedid");
                    KwlReturnObject returnObject = accVendorPaymentobj.getVendorCnPayment(paymentId, cnnoteid);
                    List<CreditNotePaymentDetails> list = returnObject.getEntityList();
                    for (CreditNotePaymentDetails cnpd : list) {
                        String id = cnpd.getID();
                        HashMap<String, JSONArray> jcustomarrayMap = payment.getJcustomarrayMap();
                        JSONArray jcustomarray = jcustomarrayMap.get(rowJeId);
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                        customrequestParams.put("modulerecid", rowJeId);
                        customrequestParams.put("recdetailId", id);
                        customrequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                        customrequestParams.put("companyid", companyid);
                        customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), rowJeId);
                            AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                            KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), rowJeId);
                            JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                            journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
                        }
                        
                        if (jobj.has("methodjedid") && !StringUtil.isNullOrEmpty(jobj.optString("methodjedid"))) {
                              /*
                             * Mapping custom field for additional jedetail of
                             * payment method for payment against Credit Note
                             */
                            customrequestParams.put("modulerecid", jobj.optString("methodjedid"));
                            customrequestParams.put("recdetailId", id);
                            customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), jobj.optString("methodjedid"));
                                AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                                KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), jobj.optString("methodjedid"));
                                JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                                journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
    }

     public JournalEntry journalEntryObjectBankCharges(JSONObject paramJobj, Payment editPaymentObject, int counter, boolean isBankCharge, boolean paymentWithoutJe, JournalEntry oldBankChargeJE, JournalEntry oldBankInterestJE) throws SessionExpiredException, ServiceException, AccountingException {
        JournalEntry journalEntry = null;
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            DateFormat df = authHandler.getDateOnlyFormat();
            String currencyid = paramJobj.optString(Constants.currencyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (paramJobj.optString("currencyid") == null ? currency.getCurrencyID() : paramJobj.optString("currencyid"));
            String methodid = paramJobj.optString("pmtmethod");
            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap)) {
                sessionHandlerImpl sess = new sessionHandlerImpl();
                sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
            }
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate"));
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);
            if (paymentWithoutJe) {
                counter--;
            }
            if (editPaymentObject == null || paymentWithoutJe) {
                jeDataMap.put("entrynumber", "");
                jeDataMap.put("autogenerated", true);
            } else if (editPaymentObject != null && oldBankChargeJE != null && isBankCharge) {
                JournalEntry entry = oldBankChargeJE;
                jeDataMap.put("entrynumber", entry.getEntryNumber());
                jeDataMap.put("autogenerated", entry.isAutoGenerated());
                jeDataMap.put(Constants.SEQFORMAT, entry.getSeqformat().getID());
                jeDataMap.put(Constants.SEQNUMBER, entry.getSeqnumber());
                jeDataMap.put(Constants.DATEPREFIX, entry.getDatePreffixValue());
                jeDataMap.put(Constants.DATEAFTERPREFIX, entry.getDateAfterPreffixValue());
                jeDataMap.put(Constants.DATESUFFIX, entry.getDateSuffixValue());
            } else if (editPaymentObject != null && oldBankInterestJE != null && !isBankCharge) {
                JournalEntry entry = oldBankInterestJE;
                jeDataMap.put("entrynumber", entry.getEntryNumber());
                jeDataMap.put("autogenerated", entry.isAutoGenerated());
                jeDataMap.put(Constants.SEQFORMAT, entry.getSeqformat().getID());
                jeDataMap.put(Constants.SEQNUMBER, entry.getSeqnumber());
                jeDataMap.put(Constants.DATEPREFIX, entry.getDatePreffixValue());
                jeDataMap.put(Constants.DATEAFTERPREFIX, entry.getDateAfterPreffixValue());
                jeDataMap.put(Constants.DATESUFFIX, entry.getDateSuffixValue());
            }
            jeDataMap.put("entrydate", df.parse(paramJobj.optString("creationdate")));
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", paramJobj.optString("memo"));
            jeDataMap.put("currencyid", currencyid);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return journalEntry;
    }

    public Set<JournalEntryDetail> journalEntryDetailCommonObjectsForBankCharges(JSONObject paramJobj, JournalEntry journalEntry, Payment payment, boolean bankCharge) throws SessionExpiredException, ServiceException, AccountingException {
        double amount = 0;
        Set jedetails = new HashSet();
        try {
            KwlReturnObject jedresult = null;
            JSONObject jedjson = null;
            JournalEntryDetail jed = null;
            JournalEntryDetail JEdeatilId = null;
            Account dipositTo = null;
            String companyid = paramJobj.optString(Constants.companyKey);
            String currencyid = paramJobj.optString(Constants.currencyKey);
            double bankCharges = 0;
            double bankInterest = 0;
            String bankChargesAccid = paramJobj.optString("bankChargesCmb");
            String bankInterestAccid = paramJobj.optString("bankInterestCmb");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (paramJobj.optString("currencyid") == null ? currency.getCurrencyID() : paramJobj.optString("currencyid"));

            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();

            String jeid = null;
            if (journalEntry != null) {
                jeid = journalEntry.getID();
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("bankCharges")) && !StringUtil.isNullOrEmpty(bankChargesAccid)) {
                bankCharges = Double.parseDouble(paramJobj.optString("bankCharges"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("bankInterest")) && !StringUtil.isNullOrEmpty(bankInterestAccid)) {
                bankInterest = Double.parseDouble(paramJobj.optString("bankInterest"));
            }
            // amount = Double.parseDouble(request.getParameter("amount"));
            //All Fore
            if (bankCharge && bankCharges != 0) {
                amount += bankCharges;
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", bankCharges);
                jedjson.put("accountid", bankChargesAccid);
                jedjson.put("debit", true);            // receipt side charges
                jedjson.put("jeid", jeid);

                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedjson.put("jedid", JEdeatilId.getID());
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);

                jedetails.add(jed);
            }
            if (!bankCharge && bankInterest != 0) {
                amount += bankInterest;
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", bankInterest);
                jedjson.put("accountid", bankInterestAccid);
                jedjson.put("debit", true);    // receipt side charges
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedjson.put("jedid", JEdeatilId.getID());
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetails.add(jed);
            }
            if (amount != 0) {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", amount);
                jedjson.put("accountid", dipositTo.getID());
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                // jedetails.add(JEdeatilId);
                jedjson.put("jedid", JEdeatilId.getID());
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetails.add(jed);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return jedetails;
    }

    public Set<JournalEntryDetail> journalEntryDetailCommonObjects(JSONObject paramJobj, JSONArray detailsJSONArray, JournalEntry journalEntry, Payment payment, int type, Payment editPaymentObject, JournalEntry oldBankChargeJE, JournalEntry oldBankInterestJE) throws SessionExpiredException, ServiceException, AccountingException {
        double amount = 0;
        double amountIncludingBankCharges = 0;
        Set jedetails = new HashSet();
        try {
            JSONObject jedjson = null;
            JournalEntryDetail jed = null;
            Account dipositTo = null;
            String companyid = paramJobj.optString(Constants.companyKey);
            String currencyid = paramJobj.optString(Constants.currencyKey);
            double bankCharges = 0;
            double bankInterest = 0;
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", companyid);
            GlobalParams.put("gcurrencyid", currencyid);
            String bankChargesAccid = paramJobj.optString("bankChargesCmb");
            String bankInterestAccid = paramJobj.optString("bankInterestCmb");
            HashMap<String, JSONArray> Map1 = new HashMap();

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (paramJobj.optString("currencyid") == null ? currency.getCurrencyID() : paramJobj.optString("currencyid"));

            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();

            String jeid = null;
            if (journalEntry != null) {
                jeid = journalEntry.getID();
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("bankCharges")) && !StringUtil.isNullOrEmpty(bankChargesAccid)) {
                bankCharges = Double.parseDouble(paramJobj.optString("bankCharges"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("bankInterest")) && !StringUtil.isNullOrEmpty(bankInterestAccid)) {
                bankInterest = Double.parseDouble(paramJobj.optString("bankInterest"));
            }
            amount = Double.parseDouble(paramJobj.optString("amount"));
            amountIncludingBankCharges = amount;
            //All Fore
            if (bankCharges != 0 && (editPaymentObject != null && oldBankChargeJE == null)) {
                amountIncludingBankCharges += bankCharges;
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", bankCharges);
                jedjson.put("accountid", bankChargesAccid);
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetails.add(jed);
            }
            if (bankInterest != 0 && (editPaymentObject != null && oldBankInterestJE == null)) {
                amountIncludingBankCharges += bankInterest;
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", bankInterest);
                jedjson.put("accountid", bankInterestAccid);
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetails.add(jed);
            }
            if (amount != 0) {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", amountIncludingBankCharges);
                jedjson.put("accountid", dipositTo.getID());
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetails.add(jed);
                payment.setDeposittoJEDetail(jed);
                payment.setDepositAmount(amountIncludingBankCharges);       // put amount excluding bank charges
                try {
                    String transactionCurrency = payment.getCurrency() != null ? payment.getCurrency().getCurrencyID() : payment.getCompany().getCurrency().getCurrencyID();
                    KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, amountIncludingBankCharges, transactionCurrency, payment.getCreationDate(), journalEntry.getExternalCurrencyRate());
                    double depositamountinbase = (Double) baseAmount.getEntityList().get(0);
                    depositamountinbase = authHandler.round(depositamountinbase, companyid);
                    payment.setDepositamountinbase(depositamountinbase);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return jedetails;
    }

   public PayDetail getPayDetailObject(JSONObject paramJobj, Payment editPaymentObject, Payment payment) throws SessionExpiredException, ServiceException, AccountingException, ParseException {
        String oldChequeNo = "";
        String Cardid = "";
        PayDetail pdetail = null;

        try {
            boolean isWarn = paramJobj.optString("isWarn") != null ? Boolean.parseBoolean(paramJobj.optString("isWarn")) : false;
            String companyid = paramJobj.optString(Constants.companyKey);
            DateFormat df = authHandler.getDateOnlyFormat();
            String currencyid = paramJobj.optString(Constants.companyKey);
            String receiptid = paramJobj.optString("billid");
            String methodid = paramJobj.optString("pmtmethod");
            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap)) {
                sessionHandlerImpl sess = new sessionHandlerImpl();
                sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
            }
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (paramJobj.optString("currencyid") == null ? currency.getCurrencyID() : paramJobj.optString("currencyid"));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);

            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            HashMap pdetailhm = new HashMap();
            pdetailhm.put("paymethodid", payMethod.getID());
            pdetailhm.put("companyid", companyid);
            BigInteger oldChqNoIntValue = new BigInteger("0");
            BigInteger chequesequencenumber = new BigInteger("0");
            if (payMethod.getDetailType() != PaymentMethod.TYPE_CASH && !StringUtil.isNullOrEmpty(paramJobj.optString("paydetail"))) {

                JSONObject obj = new JSONObject(paramJobj.optString("paydetail"));
                if (payment.getPayDetail() != null) {
                    if (payment.getPayDetail().getCard() != null) {
                        Cardid = payment.getPayDetail().getCard().getID();
                    }
                    if (payment.getPayDetail().getCheque() != null) {
                        Cardid = payment.getPayDetail().getCheque().getID();
                        oldChequeNo = payment.getPayDetail().getCheque().getChequeNo();
                        oldChqNoIntValue  = editPaymentObject.getPayDetail().getCheque().getSequenceNumber();
                    }
                }
                if (payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
                    saveBankReconsilationDetails(paramJobj, payment);
                    obj.put(Constants.companyKey, companyid);
                    HashMap chequehm = new HashMap();
                    Map<String, Object> seqchequehm = new HashMap<>();
                    BigInteger nextSeqNumber = new BigInteger("0");
                    obj.put(Constants.companyKey, companyid);
                    String sequenceformat =  obj.optString("sequenceformat");
                    /**
                     * getNextChequeNumber method to generate next sequence number using
                     * sequence format,also saving the dateprefix and datesuffix in cheque table.
                     */
                    obj.put("ischequeduplicatepref", preferences.getChequeNoDuplicate());
                    if (!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA")) {
                        //Edit case- check pevious check number and new entered number are same then no need to generate next number else generate next number.
                        if (payment != null && payment.getPayDetail() != null && payment.getPayDetail().getCheque() != null && payment.getPayDetail().getCheque().getChequeNo() != "" && !payment.getPayDetail().getCheque().getChequeNo().equalsIgnoreCase(obj.optString("chequenumber"))) {
                            seqchequehm = accCompanyPreferencesObj.getNextChequeNumber(obj);
                        } else if(StringUtil.isNullOrEmpty(receiptid) && editPaymentObject==null){//create and copy case- generate next number.
                            seqchequehm = accCompanyPreferencesObj.getNextChequeNumber(obj);
                        }
                    }
                    boolean checkForNextSequenceNumberAlso = true;
                    boolean isChequeNumberInString = false;
                    /**
                     * No need to generate next number in edit case.
                     */
                    if (extraCompanyPreferences != null && StringUtil.isNullOrEmpty(receiptid) && editPaymentObject==null) {
                        try {// USER can enter String values also in such case exception will come
                            nextSeqNumber = new BigInteger(obj.getString("chequenumber"));
                            chequehm.put("sequenceNumber", nextSeqNumber);
                            // cheque whether Cheque Number exist or not if already exist then don't let it save
                        } catch (Exception ex) {
                            checkForNextSequenceNumberAlso = false;
                            isChequeNumberInString = true;
                        }
                    } else {
                        checkForNextSequenceNumberAlso = false;
                    }
                    boolean isChequeNumberAvailable = false;
                    boolean isEditCaseButChqNoChanged = false;
                    if (!StringUtil.isNullOrEmpty(obj.optString("chequenumber")) && extraCompanyPreferences != null) {
                        try {// OLD CHQ NO. can be String value also in such case exception will come

                            HashMap chequeNohm = new HashMap();
//                            nextSeqNumber = new BigInteger(obj.optString("chequenumber"));
                            chequeNohm.put("companyId", companyid);
                            chequeNohm.put("sequenceNumber", obj.optString("chequenumber"));
                            chequeNohm.put("checkForNextSequenceNumberAlso", checkForNextSequenceNumberAlso);
                            chequeNohm.put("nextChequeNumber", obj.optString("chequenumber"));
                            chequeNohm.put("bankAccountId", payMethod.getAccount().getID());
                            chequeNohm.put("chequesequenceformatid", obj.optString("sequenceformat").equals("NA") ? "" : obj.optString("sequenceformat"));  //ERP-37585
                            JSONObject ChJobj = accCompanyPreferencesObj.isChequeNumberAvailable(chequeNohm);
                            isChequeNumberAvailable = ChJobj.optBoolean("isChequeNumberAvailable");
                            chequesequencenumber =new BigInteger(ChJobj.optString("chequesequencenumber"));

                            
//                            if (!StringUtil.isNullOrEmpty(oldChequeNo)) {
//                                oldChqNoIntValue = new BigInteger(oldChequeNo);
//                            }


                            if (!oldChqNoIntValue.equals(chequesequencenumber)) {
                                isEditCaseButChqNoChanged = true;
                            }

                            if (isChequeNumberInString) {
                                if (!oldChequeNo.equals(obj.optString("chequenumber"))) {
                                    isEditCaseButChqNoChanged = true;
                                }
                            }
                        } catch (Exception ex) {
                            if (!oldChequeNo.equals(obj.optString("chequenumber"))) {
                                isEditCaseButChqNoChanged = true;
                            }
                        }
                    } else {
                        if (!oldChequeNo.equals(obj.optString("chequenumber"))) {
                            isEditCaseButChqNoChanged = true;
                        }
                    }
                    if (preferences.getChequeNoDuplicate() != Constants.ChequeNoIgnore) {

                        if (preferences.getChequeNoDuplicate() == Constants.ChequeNoBlock || (preferences.getChequeNoDuplicate() == Constants.ChequeNoWarn && isWarn)) {
                            if (!StringUtil.isNullOrEmpty(obj.optString("chequenumber")) && isChequeNumberAvailable && isEditCaseButChqNoChanged) {
                                String msgForException = "Cheque Number : <b>" + obj.getString("chequenumber") + "</b> is already exist. ";
                                if (isWarn) {
                                    throw new AccountingException(msgForException);
                                } else {
                                    throw new AccountingException(msgForException + "Please enter different one. ");
                                }
                            }
                            String chequeNumber = obj.optString("chequenumber");
                            chequeNumber = "'" + chequeNumber + "'";
                            HashMap<String, Object> requestMap = new HashMap();
                            requestMap.put("bankAccountId", payMethod.getAccount().getID());
                            requestMap.put("chequeNumber", chequeNumber);
                            KwlReturnObject resultRepeatedPaymentChequeDetails = accPaymentDAOobj.getRepeatedPaymentChequeDetailsForPaymentMethod(requestMap);
                            List RPCD = resultRepeatedPaymentChequeDetails.getEntityList();
                            if (RPCD.size() > 0) {
                                Object[] object = (Object[]) RPCD.get(0);
                                String paymentNumber = (String) object[1];
                                String msgForException = messageSource.getMessage("acc.field.ChequeNumber", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " : <b>" + obj.getString("chequenumber") + "</b> " + messageSource.getMessage("acc.recurringMP.chequeNoReserverd", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " <b>" + paymentNumber + "</b>. ";
                                if (isWarn) {
                                    throw new AccountingException(msgForException);
                                } else {
                                    throw new AccountingException(msgForException + messageSource.getMessage("acc.recurringMP.enteranotherChequeNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                                }
                            }
                        }
                    }
                    String chequeseqformatID = obj.optString("sequenceformat","");
                    chequehm.put("chequeno", obj.optString("chequenumber"));
                    chequehm.put("sequenceNumber", chequesequencenumber);
                    chequehm.put("companyId", companyid);
                    chequehm.put("createdFrom", 1);
                    chequehm.put("chequeID", Cardid);
                    chequehm.put(Constants.SEQFORMAT, chequeseqformatID);
                    if (!chequeseqformatID.equalsIgnoreCase("NA") && !chequeseqformatID.equalsIgnoreCase("")){
                        chequehm.put("isAutoGeneratedChequeNumber", true);
                    }
                    chequehm.put("bankAccount", (payMethod.getAccount() != null) ? payMethod.getAccount().getID() : "");
                    chequehm.put("description", StringUtil.DecodeText(obj.optString("description")));
                    chequehm.put("bankname", StringUtil.DecodeText(obj.optString("paymentthrough")));
                    chequehm.put("duedate", df.parse(obj.getString("postdate")));
                    /**
                     * getNextChequeNumber method to generate next sequence number using
                     * sequence format,also saving the dateprefix and datesuffix in cheque table.
                     */
                    if (seqchequehm.containsKey(Constants.AUTO_ENTRYNUMBER)) {
                        chequehm.put("chequeno", (String) seqchequehm.get(Constants.AUTO_ENTRYNUMBER));
                    }
                    if (seqchequehm.containsKey(Constants.SEQNUMBER)) {
                        chequehm.put("sequenceNumber", (String) seqchequehm.get(Constants.SEQNUMBER));
                    }
                    if (seqchequehm.containsKey(Constants.DATEPREFIX)) {
                        chequehm.put(Constants.DATEPREFIX, (String) seqchequehm.get(Constants.DATEPREFIX));
                    }
                    if (seqchequehm.containsKey(Constants.DATEAFTERPREFIX)) {
                        chequehm.put(Constants.DATEAFTERPREFIX, (String) seqchequehm.get(Constants.DATEAFTERPREFIX));
                    }
                    if (seqchequehm.containsKey(Constants.DATESUFFIX)) {
                        chequehm.put(Constants.DATESUFFIX, (String) seqchequehm.get(Constants.DATESUFFIX));
                    }
                    KwlReturnObject cqresult = accPaymentDAOobj.addCheque(chequehm);
                    Cheque cheque = (Cheque) cqresult.getEntityList().get(0);
                    pdetailhm.put("chequeid", cheque.getID());
                } else if (payMethod.getDetailType() == PaymentMethod.TYPE_CARD) {
                    HashMap cardhm = new HashMap();
                    cardhm.put("cardno", obj.getString("CardNo"));
                    cardhm.put("nameoncard", obj.getString("nameoncard"));
                    cardhm.put("expirydate", df.parse(obj.getString("expirydate")));
                    cardhm.put("cardtype", obj.getString("cardtype"));
                    cardhm.put("refno", obj.getString("refno"));
                    KwlReturnObject cdresult = accPaymentDAOobj.addCard(cardhm);
                    Card card = (Card) cdresult.getEntityList().get(0);
                    pdetailhm.put("cardid", card.getID());
                }
            }
            pdetail = accPaymentDAOobj.saveOrUpdatePayDetail(pdetailhm);
        } catch (JSONException | ServiceException jex) {
            throw ServiceException.FAILURE(jex.getMessage(), jex);
        } 
        return pdetail;
    }

    public void saveReevalJournalEntryObjects(JSONObject paramJobj, JSONArray detailsJSONArray, Payment payment, int type, String oldRevaluationJE, Map<String, Object> counterMap) throws SessionExpiredException, ServiceException, AccountingException {
        try {
            if (detailsJSONArray.length() > 0) {
                double finalAmountReval = 0;
                String basecurrency = paramJobj.optString(Constants.globalCurrencyKey);
                KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), payment.getCompany().getCompanyID());
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

                finalAmountReval = getReevalJournalEntryAmount(paramJobj, detailsJSONArray, payment, type);
                if (finalAmountReval != 0) {
                    /**
                     * added transactionID and transactionModuleID to Realised
                     * JE.
                     */
                    counterMap.put("transactionModuleid", payment.isIsOpeningBalencePayment() ? Constants.Acc_opening_Payment : Constants.Acc_Make_Payment_ModuleId);
                    counterMap.put("transactionId", payment.getID());
                    String revaljeid = PostJEFORReevaluation(paramJobj, finalAmountReval, payment.getCompany().getCompanyID(), preferences, basecurrency, oldRevaluationJE, counterMap);
                    payment.setRevalJeId(revaljeid);
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
    }
   
    public class EnglishNumberToWords {

        private final String[] tensNames = {
            "", " Ten", " Twenty", " Thirty", " Forty", " Fifty", " Sixty", " Seventy", " Eighty", " Ninety"
        };
        private final String[] numNames = {
            "", " One", " Two", " Three", " Four", " Five", " Six", " Seven", " Eight", " Nine", " Ten", " Eleven", " Twelve",
            " Thirteen", " Fourteen", " Fifteen", " Sixteen", " Seventeen", " Eighteen", " Nineteen"
        };

        private String convertLessThanOneThousand(int number) {
            String soFar;
            if (number % 100 < 20) {
                soFar = numNames[number % 100];
                number /= 100;
            } else {
                soFar = numNames[number % 10];
                number /= 10;
                soFar = tensNames[number % 10] + soFar;
                number /= 10;
            }
            if (number == 0) {
                return soFar;
            }
            return numNames[number] + " Hundred" + soFar;
        }

        private String convertLessOne(int number, KWLCurrency currency) {
            String soFar;
            String val = currency.getAfterDecimalName();
            if (number % 100 < 20) {
                soFar = numNames[number % 100];
                number /= 100;
            } else {
                soFar = numNames[number % 10];
                number /= 10;
                soFar = tensNames[number % 10] + soFar;
                number /= 10;
            }
            if (number == 0) {
                return " And " + soFar + " " + val;
            }
            return " And " + numNames[number] + " " + val + soFar;
        }

        public String convert(Double number, KWLCurrency currency, int countryLanguageId) {
            if (number == 0) {
                return "Zero";
            }
            String answer = "";

            if (countryLanguageId == Constants.OtherCountryLanguageId) { // For universal conversion of amount in words. i.e. in Billion,trillion etc
                answer = universalConvert(number, currency);
            } else if (countryLanguageId == Constants.CountryIndiaLanguageId) { // For Indian word format.ie. in lakhs, crores
                answer = indianConvert(number, currency);
            }
            return answer;
        }

        public String universalConvert(Double number, KWLCurrency currency) {
            boolean isNegative = false;
            if (number < 0) {
                isNegative = true;
                number = -1 * number;
            }
            String snumber = Double.toString(number);
            String mask = "000000000000.00";
            DecimalFormat df = new DecimalFormat(mask);
            snumber = df.format(number);
            int billions = Integer.parseInt(snumber.substring(0, 3));
            int millions = Integer.parseInt(snumber.substring(3, 6));
            int hundredThousands = Integer.parseInt(snumber.substring(6, 9));
            int thousands = Integer.parseInt(snumber.substring(9, 12));
            int fractions = Integer.parseInt(snumber.substring(13, 15));
            String tradBillions;
            switch (billions) {
                case 0:
                    tradBillions = "";
                    break;
                case 1:
                    tradBillions = convertLessThanOneThousand(billions) + " Billion ";
                    break;
                default:
                    tradBillions = convertLessThanOneThousand(billions) + " Billion ";
            }
            String result = tradBillions;

            String tradMillions;
            switch (millions) {
                case 0:
                    tradMillions = "";
                    break;
                case 1:
                    tradMillions = convertLessThanOneThousand(millions) + " Million ";
                    break;
                default:
                    tradMillions = convertLessThanOneThousand(millions) + " Million ";
            }
            result = result + tradMillions;

            String tradHundredThousands;
            switch (hundredThousands) {
                case 0:
                    tradHundredThousands = "";
                    break;
                case 1:
                    tradHundredThousands = "One Thousand ";
                    break;
                default:
                    tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " Thousand ";
            }
            result = result + tradHundredThousands;
            String tradThousand;
            tradThousand = convertLessThanOneThousand(thousands);
            result = result + tradThousand;
            String paises;
            switch (fractions) {
                case 0:
                    paises = "";
                    break;
                default:
                    paises = convertLessOne(fractions, currency);
            }
            result = result + paises; //to be done later
            result = result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
            if (isNegative) {
                result = "Minus " + result;
            }
//            result = result.substring(0, 1).toUpperCase() + result.substring(1).toLowerCase(); // Make first letter of operand capital.
            return result;
        }

        public String indianConvert(Double number, KWLCurrency currency) {
            boolean isNegative = false;
            if (number < 0) {
                isNegative = true;
                number = -1 * number;
            }
            String snumber = Double.toString(number);
            String mask = "000000000000000.00";  //ERP-17681
            DecimalFormat df = new DecimalFormat(mask);
            snumber = df.format(number);

            Long n = Long.parseLong(snumber.substring(0, 15));
            int fractions = Integer.parseInt(snumber.split("\\.").length != 0 ? snumber.split("\\.")[1] : "0");
            if (n == 0) {
                return "Zero";
            }
            String arr1[] = {"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
            String arr2[] = {"Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
            String unit[] = {"neel", "kharab", "Arab", "Crore", "Lakh", "Thousand", "Hundred", ""};
            long factor[] = {100000000000l, 100000000000l, 1000000000, 10000000, 100000, 1000, 100, 1};
            String answer = "", paises = "";
            if (n < 0) {
                answer = "Minus";
                n = -n;
            }
            int quotient, units, tens;
            for (int i = 0; i < factor.length; i++) {
                quotient = (int) (n / factor[i]);
                if (quotient > 0) {
                    if (quotient < 20) {
                        answer = answer + " " + arr1[quotient - 1];
                    } else {
                        units = quotient % 10;
                        tens = quotient / 10;
                        if (units > 0) {
                            answer = answer + " " + arr2[tens - 2] + " " + arr1[units - 1];
                        } else {
                            answer = answer + " " + arr2[tens - 2] + " ";
                        }
                    }
                    answer = answer + " " + unit[i];
                }
                n = n % factor[i];
            }
            switch (fractions) {
                case 0:
                    paises = "";
                    break;
                default:
                    paises = convertLessOne(fractions, currency);
            }
            answer = answer + paises; //to be done later
            return answer.trim();
        }
    }

    public void deleteChequeOrCard(String id, String companyid) throws ServiceException, AccountingException, SessionExpiredException {
        try {
            if (id != null) {
                accPaymentDAOobj.deleteCard(id, companyid);
                accPaymentDAOobj.deleteChequePermanently(id, companyid);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public void deleteJEArray(String oldjeid, String companyid) throws ServiceException, AccountingException, SessionExpiredException {
        try {      //delete old invoice
            JournalEntryDetail jed = null;
            if (!StringUtil.isNullOrEmpty(oldjeid)) {
                KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(oldjeid, companyid);
                List list = result.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    jed = (JournalEntryDetail) itr.next();
                    //Sagar - No need to revert entry from optimized table as entries are already reverted from calling main function in edit case.
                    result = accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
                }
                result = accJournalEntryobj.permanentDeleteJournalEntry(oldjeid, companyid);
                KwlReturnObject jedresult1 = accJournalEntryobj.deleteJECustomData(oldjeid);
            }
        } catch (Exception ex) {
            //Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    private void deleteBankReconcilation(Map<String, Object> requestParams) throws ServiceException {
        if (requestParams.containsKey("oldjeid")) {
            String reconsilationID = "";
            String unReconsilationID = "";
            String jeid = (String) requestParams.get("oldjeid");
            String companyid = (String) requestParams.get("companyId");

            //Deleting  BankReconciliationDetail
            KwlReturnObject reconsiledDetails = accBankReconciliationObj.getBRfromJE(jeid, companyid, true);
            if (reconsiledDetails.getRecordTotalCount() > 0) {
                List<BankReconciliationDetail> brd = reconsiledDetails.getEntityList();
                for (BankReconciliationDetail reconciliation : brd) {
                    accBankReconciliationObj.permenantDeleteBankReconciliationDetail(reconciliation.getID(), companyid);
                    reconsilationID = reconciliation.getBankReconciliation().getID();
                }
            }

            //Deleting  BankUnreconciliationDetail
            KwlReturnObject unReconsiledDetails = accBankReconciliationObj.getBankUnReconsiledfromJE(jeid, companyid, true);
            if (unReconsiledDetails.getRecordTotalCount() > 0) {
                List<BankUnreconciliationDetail> brd = unReconsiledDetails.getEntityList();
                for (BankUnreconciliationDetail reconciliation : brd) {
                    accBankReconciliationObj.permenantDeleteBankUnReconciliationDetail(reconciliation.getID(), companyid);
                    unReconsilationID = reconciliation.getBankReconciliation().getID();
                }
            }
            if (!StringUtil.isNullOrEmpty(reconsilationID)) {
                accBankReconciliationObj.deleteBankReconciliation(reconsilationID, companyid);
            }
            if (!StringUtil.isNullOrEmpty(unReconsilationID)) {
                accBankReconciliationObj.deleteBankReconciliation(unReconsilationID, companyid);
            }
        }
    }
    //If Exception occured or payment completed  then delete entry from temporary table

    public void deleteTemporaryInvoicesEntries(JSONArray jSONArrayAgainstInvoice, String companyid) {
        try {
            for (int i = 0; i < jSONArrayAgainstInvoice.length(); i++) {
                JSONObject invoiceJobj = jSONArrayAgainstInvoice.getJSONObject(i);
                String invoiceId = invoiceJobj.getString("documentid");
                accPaymentDAOobj.deleteUsedInvoiceOrCheque(invoiceId, companyid);
            }
        } catch (Exception ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    /*
     * saving linking information in Make Payment & Credit Note table If Payment
     * is made with Credit Note
     */

    public void saveLinkingInformationOfPaymentWithCN(CreditNote creditNote, Payment payment, String paymentNo) throws ServiceException {

        try {
            /*
             * Save Credit Note Linking & Make Paymnet Linking information in
             * linking table
             */
            HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
            requestParamsLinking.put("linkeddocid", payment.getID());
            requestParamsLinking.put("docid", creditNote.getID());
            requestParamsLinking.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
            requestParamsLinking.put("linkeddocno", paymentNo);
            requestParamsLinking.put("sourceflag", 0);
            KwlReturnObject result = accVendorPaymentobj.updateEntryInCreditNoteLinkingTable(requestParamsLinking);

            requestParamsLinking.put("linkeddocid", creditNote.getID());
            requestParamsLinking.put("docid", payment.getID());
            requestParamsLinking.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
            requestParamsLinking.put("linkeddocno", creditNote.getCreditNoteNumber());
            requestParamsLinking.put("sourceflag", 1);
            result = accVendorPaymentobj.savePaymentLinking(requestParamsLinking);

        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    /*
     * saving linking information in Make Payment & Purchase Invoice table If
     * Payment is made against Purchase Invoice
     */

    public void saveLinkingInformationOfPaymentWithInvoice(GoodsReceipt invoice, Payment payment, String paymentNo) throws ServiceException {

        try {
            /*
             * Save Purchase Invoice Linking & Make Paymnet Linking information
             * in linking table
             */
            HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
            requestParamsLinking.put("linkeddocid", payment.getID());
            requestParamsLinking.put("docid", invoice.getID());
            requestParamsLinking.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
            requestParamsLinking.put("linkeddocno", paymentNo);
            requestParamsLinking.put("sourceflag", 0);
            KwlReturnObject result = accGoodsReceiptobj.saveVILinking(requestParamsLinking);

            requestParamsLinking.put("linkeddocid", invoice.getID());
            requestParamsLinking.put("docid", payment.getID());
            requestParamsLinking.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
            requestParamsLinking.put("linkeddocno", invoice.getGoodsReceiptNumber());
            requestParamsLinking.put("sourceflag", 1);
            result = accVendorPaymentobj.savePaymentLinking(requestParamsLinking);

        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public double oldPaymentRowsAmount(JSONObject paramJobj, JSONArray jArr, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException {
        double amount = 0, actualAmount = 0;
        try {
            String companyid = paramJobj.optString("companyid");
            String basecurrency = paramJobj.optString(Constants.globalCurrencyKey);
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", paramJobj.optString(Constants.companyKey));
            GlobalParams.put("gcurrencyid", basecurrency);
            GlobalParams.put("dateformat", authHandler.getDateOnlyFormat());
            Date creationDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("creationdate"));
            for (int i = 0; i < jArr.length(); i++) {
                double ratio = 0;
                JSONObject jobj = jArr.getJSONObject(i);
                double newrate = 0.0;
                boolean revalFlag = false;
                KwlReturnObject result = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), jobj.getString("documentid"));
                GoodsReceipt gr = (GoodsReceipt) result.getEntityList().get(0);
                Double recinvamount = jobj.getDouble("enteramount");
                if (paramJobj.optBoolean("isDiscountAppliedOnPaymentTerms", false)) {
                    recinvamount = authHandler.round(jobj.optDouble("enteramount") + jobj.optDouble("discountname", 0.0), companyid);
                }
                boolean isopeningBalancePayment = jobj.optBoolean("isopeningBalancePayment", false);
                boolean isConversionRateFromCurrencyToBase = jobj.optBoolean("isConversionRateFromCurrencyToBase", false);
                double exchangeRate = 0d;
                Date goodsReceiptCreationDate = null;
                if (gr.isNormalInvoice()) {
                    exchangeRate = gr.getJournalEntry().getExternalCurrencyRate();
                } else {
                    exchangeRate = gr.getExchangeRateForOpeningTransaction();
                    if (gr.isConversionRateFromCurrencyToBase()) {
                        exchangeRate = 1 / exchangeRate;
                    }
                }
                goodsReceiptCreationDate = gr.getCreationDate();


                HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                invoiceId.put("invoiceid", gr.getID());
                invoiceId.put("companyid", paramJobj.optString(Constants.companyKey));
                result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                if (history != null) {
                    exchangeRate = history.getEvalrate();
                    newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
                    revalFlag = true;
                }
                result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
                KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
                String currid = currency.getCurrencyID();
                if (gr.getCurrency() != null) {
                    currid = gr.getCurrency().getCurrencyID();
                }

                KwlReturnObject bAmt = null;
                if (currid.equalsIgnoreCase(currencyid)) {
                    double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 0.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
                    if (exchangeRate != paymentExternalCurrencyRate) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                    }
                } else {
                    double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 0.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
                    if (exchangeRate != paymentExternalCurrencyRate) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                    }
                }
                double oldrate = (Double) bAmt.getEntityList().get(0);
                if (jobj.optDouble("exchangeratefortransaction", 0.0) != oldrate && jobj.optDouble("exchangeratefortransaction", 0.0) != 0.0 && Math.abs(jobj.optDouble("exchangeratefortransaction", 0.0) - oldrate) >= 0.000001) {
                    newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
                    ratio = oldrate - newrate;
                    /*
                     * Round PI amount old amount as well as entered payment amount to get exact value for calculation of gain loss JE
                     */
                    amount = (recinvamount - authHandler.round(authHandler.round(recinvamount / newrate, companyid) * oldrate, companyid)) / newrate;
                    /**
                     * rounding issue handle for amount.
                     */
//                    amount = authHandler.round(amount, companyid);
                    KwlReturnObject bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, amount, currencyid, creationDate, newrate);
                    actualAmount += authHandler.round((Double) bAmtActual.getEntityList().get(0), companyid);
                } else {
                    if (currid.equalsIgnoreCase(currencyid)) {
                        double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 0.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
                        if (exchangeRate != paymentExternalCurrencyRate) {
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                        }
                    } else {
                        double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 0.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
                        if (exchangeRate != paymentExternalCurrencyRate) {
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                        }
                    }
                    if (!revalFlag) {
                        newrate = (Double) bAmt.getEntityList().get(0);
                    }
                    if (Math.abs(jobj.optDouble("exchangeratefortransaction", 0.0) - oldrate) >= 0.000001) {
                        ratio = oldrate - newrate;
                    }
                    amount = recinvamount * ratio;
                    KwlReturnObject bAmtActual = null;
                    if (isopeningBalancePayment && isConversionRateFromCurrencyToBase) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, amount, currencyid, creationDate, externalCurrencyRate);
                    } else {
                        bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, amount, currencyid, creationDate, externalCurrencyRate);
                    }
                    actualAmount += authHandler.round((Double) bAmtActual.getEntityList().get(0), companyid);
                }
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("oldPaymentRowsAmount : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("oldPaymentRowsAmount : " + ex.getMessage(), ex);
        }
        return (actualAmount);
    }
public double cndnPaymentForexGailLossAmount(JSONObject paramJobj, JSONObject jobj, Payment payment, String transactionCurrencyId, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException, JSONException {
        String companyid = paramJobj.optString("companyid");
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("gcurrencyid", paramJobj.optString(Constants.currencyKey));
        requestParams.put("companyid", paramJobj.optString(Constants.companyKey));
        requestParams.put("dateformat", authHandler.getDateOnlyFormat());
//        double enterAmountPaymentCurrencyOld = 0;
//        double enterAmountTrancastionCurrencyNew = 0;
//        double amountdiff = 0;
        double amount = 0, actualAmount = 0;
        try {
            Date creationDate = authHandler.getDateOnlyFormat().parse(paramJobj.getString("creationdate"));
            double newrate = 0.0;
            double ratio = 0;
            boolean revalFlag = false;
//            Double enteramount = jobj.optDouble("enteramount",0.0);
            String documentId = jobj.optString("documentid");
            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), documentId);
            CreditNote creditMemo = (CreditNote) resultObject.getEntityList().get(0);
            JournalEntry je = creditMemo.getJournalEntry();
            Date creditNoteDate = null;
            if (creditMemo.isNormalCN()) {
                je = creditMemo.getJournalEntry();
                externalCurrencyRate = je.getExternalCurrencyRate();
            } else {
                externalCurrencyRate = creditMemo.getExchangeRateForOpeningTransaction();
                if (creditMemo.isConversionRateFromCurrencyToBase()) {
                    externalCurrencyRate = 1 / externalCurrencyRate;
                }
            }
            creditNoteDate = creditMemo.getCreationDate();

            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", creditMemo.getID());
            invoiceId.put("companyid", paramJobj.optString(Constants.companyKey));
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (history != null) {
                externalCurrencyRate = history.getEvalrate();
                newrate = jobj.optDouble("exchangeratefortransaction", 0.0); // Get entered exchange rate(for CN) against which payment is made
                revalFlag = true;
            }
            
            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJobj.optString(Constants.currencyKey));
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (creditMemo.getCurrency() != null) {
                currid = creditMemo.getCurrency().getCurrencyID();
            }
            // Calculate old exchange rate(for CN) against which payment is made
            if (currid.equalsIgnoreCase(currencyid)) {
                double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 0.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
                if (externalCurrencyRate != paymentExternalCurrencyRate) {
                    result = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, creditNoteDate, externalCurrencyRate, paymentExternalCurrencyRate);
                } else {
                    result = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, 1.0, currid, currencyid, creditNoteDate, externalCurrencyRate);
                }
            } else {
                double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 0.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
                if (externalCurrencyRate != paymentExternalCurrencyRate) {
                    result = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, creditNoteDate, externalCurrencyRate, paymentExternalCurrencyRate);
                } else {
                    result = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, 1.0, currid, currencyid, creditNoteDate, externalCurrencyRate);
                }
            }
            double oldrate = (Double) result.getEntityList().get(0);


            Double recinvamount = jobj.getDouble("enteramount");
            boolean isopeningBalanceRecceipt = jobj.optBoolean("isopeningBalanceRecceipt", false);
            boolean isConversionRateFromCurrencyToBase = jobj.optBoolean("isConversionRateFromCurrencyToBase", false);
            if (jobj.optDouble("exchangeratefortransaction", 0.0) != oldrate && jobj.optDouble("exchangeratefortransaction", 0.0) != 0.0 && Math.abs(jobj.optDouble("exchangeratefortransaction", 0.0) - oldrate) >= 0.000001) {
                newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
                ratio = oldrate - newrate;
                amount = (recinvamount - authHandler.round(authHandler.round(recinvamount / newrate, companyid) * oldrate, companyid)) / newrate;
                /**
                 * rounding issue handle for amount.
                 */
//                amount = authHandler.round(amount, companyid);
                KwlReturnObject bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creationDate, newrate);
                actualAmount += authHandler.round((Double) bAmtActual.getEntityList().get(0), companyid);
            } else {
                if (currid.equalsIgnoreCase(currencyid)) {
                    double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 0.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
                    if (externalCurrencyRate != paymentExternalCurrencyRate) {
                        result = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, creditNoteDate, externalCurrencyRate, paymentExternalCurrencyRate);
                    } else {
                        result = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, 1.0, currid, currencyid, creditNoteDate, externalCurrencyRate);
                    }
                } else {
                    double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 0.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
                    if (externalCurrencyRate != paymentExternalCurrencyRate) {
                        result = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, creditNoteDate, externalCurrencyRate, paymentExternalCurrencyRate);
                    } else {
                        result = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, 1.0, currid, currencyid, creditNoteDate, externalCurrencyRate);
                    }
                }
                if (!revalFlag) {
                    newrate = (Double) result.getEntityList().get(0);
                }
                if (Math.abs(jobj.optDouble("exchangeratefortransaction", 0.0) - oldrate) >= 0.000001) {
                    ratio = oldrate - newrate;
                }
                amount = recinvamount * ratio;
                KwlReturnObject bAmtActual = null;
                if (isopeningBalanceRecceipt && isConversionRateFromCurrencyToBase) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, currencyid, creationDate, externalCurrencyRate);
                } else {
                    bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creationDate, externalCurrencyRate);
                }
                actualAmount += (Double) bAmtActual.getEntityList().get(0);
            }
            
//            enterAmountTrancastionCurrencyNew = enteramount;
//            if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString())) {
//                enterAmountTrancastionCurrencyNew = enteramount / Double.parseDouble(jobj.get("exchangeratefortransaction").toString());
//                enterAmountTrancastionCurrencyNew = authHandler.round(enterAmountTrancastionCurrencyNew, companyid);
//                KwlReturnObject bAmtCurrencyFilter = null;
//                bAmtCurrencyFilter = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, enterAmountTrancastionCurrencyNew, transactionCurrencyId, currencyid, creditNoteDate, externalCurrencyRate);
//                enterAmountPaymentCurrencyOld = authHandler.round((Double) bAmtCurrencyFilter.getEntityList().get(0), companyid);
//                amountdiff = enteramount - enterAmountPaymentCurrencyOld;
//            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("cndnPaymentForexGailLossAmount : " + ex.getMessage(), ex);
        }
        return (actualAmount);
    }

    public double cndnPaymentForexGailLossAmountForSameCurrency(JSONObject paramJobj, JSONObject jobj, Payment payment, String transactionCurrencyId, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException, JSONException {
        double amount = 0, actualAmount = 0;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("gcurrencyid", paramJobj.optString(Constants.currencyKey));
        requestParams.put("companyid", paramJobj.optString(Constants.companyKey));
        requestParams.put("dateformat", authHandler.getDateOnlyFormat());
        try {

            double exchangeRate = 0d;
            double newrate = 0.0;
            Double enteramount = jobj.getDouble("enteramount");
            String documentId = jobj.getString("documentid");
            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), documentId);
            CreditNote creditMemo = (CreditNote) resultObject.getEntityList().get(0);
            JournalEntry je = creditMemo.getJournalEntry();
            Date creditNoteDate = null;
            if (creditMemo.isNormalCN()) {
                je = creditMemo.getJournalEntry();
                exchangeRate = je.getExternalCurrencyRate();
            } else {
                exchangeRate = creditMemo.getExchangeRateForOpeningTransaction();
                if (creditMemo.isConversionRateFromCurrencyToBase()) {
                    exchangeRate = 1 / exchangeRate;
                }
            }
            creditNoteDate = creditMemo.getCreationDate();
            double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 1.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", creditMemo.getID());
            invoiceId.put("companyid", paramJobj.optString(Constants.companyKey));
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (history != null) {
                externalCurrencyRate = history.getEvalrate();
            }

            KwlReturnObject bAmt = null;
            if (exchangeRate != paymentExternalCurrencyRate) {
                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, transactionCurrencyId, currencyid, creditNoteDate, exchangeRate, paymentExternalCurrencyRate);
            } else {
                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, 1.0, transactionCurrencyId, currencyid, creditNoteDate, exchangeRate);
            }
            double oldrate = (Double) bAmt.getEntityList().get(0);
            newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
            amount = (enteramount - (enteramount / newrate) * oldrate) / newrate;
            KwlReturnObject bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creditNoteDate, newrate);
            actualAmount += (Double) bAmtActual.getEntityList().get(0);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("cndnPaymentForexGailLossAmountForSameCurrency : " + ex.getMessage(), ex);
        }
        return (actualAmount);
    }

    @Override
    public double RefundPaymentForexGailLossAmount(JSONObject paramJobj, JSONObject jobj, Payment payment, String transactionCurrencyId, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException, JSONException, ParseException {
        String basecurrency = paramJobj.optString(Constants.currencyKey);
        String companyid = paramJobj.optString("companyid");
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("gcurrencyid", paramJobj.optString(Constants.currencyKey));
        requestParams.put("companyid", paramJobj.optString(Constants.companyKey));
        requestParams.put("dateformat", authHandler.getDateOnlyFormat());
        Date creationDate = null;
        if (paramJobj.has("creationdate")) { // Check added for SDP-5827 as in link case creation date is Null
            creationDate = authHandler.getDateOnlyFormat().parse(paramJobj.getString("creationdate"));
        } else {
              creationDate = payment.getCreationDate();
        }
        boolean revalFlag = false;
        double newrate = 0.0;
        double amount = 0;
        double amountdiff = 0;
        try {
            Date receiptDate = null;
            double paymentExternalCurrencyRate =  paramJobj.has("externalcurrencyrate")? paramJobj.optDouble("externalcurrencyrate",0.00) : payment.getExternalCurrencyRate() ;

            String documentId = jobj.getString("documentid");
            Double recinvamount = jobj.getDouble("enteramount");
            boolean isopeningBalancePayment = jobj.optBoolean("isopeningBalancePayment", false);
            boolean isConversionRateFromCurrencyToBase = jobj.optBoolean("isConversionRateFromCurrencyToBase", false);
            double ratio = 0;

            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(ReceiptAdvanceDetail.class.getName(), documentId);
            ReceiptAdvanceDetail rad = (ReceiptAdvanceDetail) resultObject.getEntityList().get(0);
            receiptDate = rad.getReceipt().getCreationDate();
            externalCurrencyRate = rad.getReceipt().getExternalCurrencyRate();

            //if Advance is Revaluated then we will revaluated rate
            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", rad.getReceipt().getID());
            invoiceId.put("companyid", paramJobj.optString(Constants.companyKey));
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (history != null) {
                externalCurrencyRate = history.getEvalrate();
                newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
                revalFlag = true;
            }

            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (rad.getReceipt().getCurrency() != null) {
                currid = rad.getReceipt().getCurrency().getCurrencyID();
            }
            /**
             * The Above code is commented for the SDP-4008 , The below code for forex gain calculations for Refund type payment is done ref to invoices gain/ loss calculations
             */
            KwlReturnObject bAmt = null;
            if (currid.equalsIgnoreCase(currencyid)) {
                
                if (externalCurrencyRate != paymentExternalCurrencyRate) {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, receiptDate, externalCurrencyRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, 1.0, currid, currencyid, receiptDate, externalCurrencyRate);
                }
            } else {
               
                if (externalCurrencyRate != paymentExternalCurrencyRate) {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, receiptDate, externalCurrencyRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, 1.0, currid, currencyid, receiptDate, externalCurrencyRate);
                }
            }
            double oldrate = (Double) bAmt.getEntityList().get(0);
            if (jobj.optDouble("exchangeratefortransaction", 0.0) != oldrate && jobj.optDouble("exchangeratefortransaction", 0.0) != 0.0 && Math.abs(jobj.optDouble("exchangeratefortransaction", 0.0) - oldrate) >= 0.000001) {
                newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
                ratio = oldrate - newrate;
                amount = (recinvamount - (recinvamount / newrate) * oldrate) / newrate;
                KwlReturnObject bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creationDate, newrate);
                amountdiff += authHandler.round((Double) bAmtActual.getEntityList().get(0), companyid);
            } else {
                if (currid.equalsIgnoreCase(currencyid)) {
                    if (externalCurrencyRate != paymentExternalCurrencyRate) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, receiptDate, externalCurrencyRate, paymentExternalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, 1.0, currid, currencyid, receiptDate, externalCurrencyRate);
                    }
                } else {
                    if (externalCurrencyRate != paymentExternalCurrencyRate) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, receiptDate, externalCurrencyRate, paymentExternalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, 1.0, currid, currencyid, receiptDate, externalCurrencyRate);
                    }
                }
                if (!revalFlag) {
                    newrate = (Double) bAmt.getEntityList().get(0);
                }
                if (Math.abs(jobj.optDouble("exchangeratefortransaction", 0.0) - oldrate) >= 0.000001) {
                    ratio = oldrate - newrate;
                }
                amount = recinvamount * ratio;
                KwlReturnObject bAmtActual = null;
                if (isopeningBalancePayment && isConversionRateFromCurrencyToBase) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, currencyid, creationDate, externalCurrencyRate);
                } else {
                    bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creationDate, externalCurrencyRate);
                }
                amountdiff += authHandler.round((Double) bAmtActual.getEntityList().get(0), companyid);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("cndnPaymentForexGailLossAmount : " + ex.getMessage(), ex);
        }
        return (amountdiff);
    }

    public HashSet saveTDSdetailsRow(HashMap<String, Object> tdsRequestParams) throws JSONException, ServiceException, UnsupportedEncodingException {
        HashSet tdsdetails = new HashSet();
        TDSRate tdsRate = null;
        Account account = null;
        Account tdsaccountidObj = null;
        MasterItem nop = null;

        JSONObject tdsDetailsJsonObj = null;
        PaymentDetail paymentdetail = null;
        AdvanceDetail advanceDetail = null;
        PaymentDetailOtherwise paymentDetailOtherwise = null;
        Company company = null;
        if (tdsRequestParams.containsKey("tdsDetailsJsonObj")) {
            tdsDetailsJsonObj = (JSONObject) tdsRequestParams.get("tdsDetailsJsonObj");
        }
        if (tdsRequestParams.containsKey("paymentDetailObj")) {
            paymentdetail = (PaymentDetail) tdsRequestParams.get("paymentDetailObj");
        }
        if (tdsRequestParams.containsKey("advanceDetailObj")) {
            advanceDetail = (AdvanceDetail) tdsRequestParams.get("advanceDetailObj");
        }
        if (tdsRequestParams.containsKey("paymentDetailOtherwiseObj")) {
            paymentDetailOtherwise = (PaymentDetailOtherwise) tdsRequestParams.get("paymentDetailOtherwiseObj");
        }
        if (tdsRequestParams.containsKey("companyObj")) {
            company = (Company) tdsRequestParams.get("companyObj");
        }
        JSONArray jsonArr = tdsDetailsJsonObj.getJSONArray("appliedTDS");
        for (int i = 0; i < jsonArr.length(); i++) {

            TdsDetails tdsDetails = new TdsDetails();
            JSONObject jsonObj = jsonArr.getJSONObject(i);
            String documenttype = tdsDetailsJsonObj.getString("documenttype");
            String ruleId = jsonObj.getString("ruleid");
            String accountid = jsonObj.getString("accountid");
            String natureofpayment = jsonObj.getString("natureofpayment");
            String documentdetail = jsonObj.getString("rowid");
            double tdspercentage = jsonObj.get("tdspercentage") != null ? jsonObj.getDouble("tdspercentage") : 0.0;
            double tdsamount = jsonObj.getDouble("tdsamount");
            double enteramount = jsonObj.getDouble("enteramount");
            double tdsAssessableAmount = (jsonObj.has("tdsAssessableAmount")&&!StringUtil.isNullObject(jsonObj.getString("tdsAssessableAmount")))?jsonObj.getDouble("tdsAssessableAmount"):jsonObj.getDouble("enteramount");
            String tdsjedid = jsonObj.getString("tdsjedid");
            String tdsaccountid = jsonObj.getString("tdsaccountid");
            if (!StringUtil.isNullOrEmpty(ruleId)) {
                KwlReturnObject tdsRateData = accountingHandlerDAOobj.getObject(TDSRate.class.getName(), Integer.parseInt(ruleId));
                tdsRate = (TDSRate) tdsRateData.getEntityList().get(0);
            }
            if (!StringUtil.isNullOrEmpty(accountid)) {
                KwlReturnObject accountData = accountingHandlerDAOobj.getObject(Account.class.getName(), accountid);
                account = (Account) accountData.getEntityList().get(0);
            }
            if (!StringUtil.isNullOrEmpty(tdsaccountid)) {
                KwlReturnObject accountData = accountingHandlerDAOobj.getObject(Account.class.getName(), tdsaccountid);
                tdsaccountidObj = (Account) accountData.getEntityList().get(0);
            }
            if (!StringUtil.isNullOrEmpty(natureofpayment)) {
                KwlReturnObject nopData = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), natureofpayment);
                nop = (MasterItem) nopData.getEntityList().get(0);
            }
            tdsDetails.setTdsid(StringUtil.generateUUID());
            tdsDetails.setCompany(company);
            tdsDetails.setPaymentdetail(paymentdetail);
            tdsDetails.setAdvanceDetail(advanceDetail);
            tdsDetails.setPaymentdetailotherwise(paymentDetailOtherwise);
            tdsDetails.setRuleid(tdsRate);
            tdsDetails.setAccount(account);
            tdsDetails.setJournalEntryDetail(tdsjedid);
            tdsDetails.setDocumenttype(documenttype);
            tdsDetails.setDocumentdetails(documentdetail);
            tdsDetails.setEnteramount(enteramount);
            tdsDetails.setTdsamount(tdsamount);
            tdsDetails.setTdspercentage(tdspercentage);
            tdsDetails.setIncludetaxamount(true);
            tdsDetails.setTdspayableaccount(tdsaccountidObj);
            tdsDetails.setTdsAssessableAmount(tdsAssessableAmount);
            tdsDetails.setNatureOfPayment(nop);
            tdsdetails.add(tdsDetails);
        }
        return tdsdetails;
    }

    public HashSet savePaymentRows(JSONObject paramJobj, Payment payment, Company company, JSONArray jArr, boolean isMultiDebit, Invoice invoice, int type) throws JSONException, ServiceException, UnsupportedEncodingException {
        HashSet pdetails = new HashSet();
        String companyid = company.getCompanyID();
        if (type == Constants.PaymentAgainstInvoice) {
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                PaymentDetail pd = new PaymentDetail();
                pd.setSrno(i + 1);
                pd.setID(StringUtil.generateUUID());
                double amountReceived = 0.0;
                double amountReceivedConverted = 0.0;
                double discountAmount = jobj.optDouble("discountname", 0.0);
                double discountAmountInInvoiceCurrecny = 0.0;
                boolean isDiscountFieldChanged = jobj.optBoolean("isDiscountFieldChanged", false);
                
                if (paramJobj.optBoolean("isEdit", false)) {
                    isDiscountFieldChanged = paramJobj.optBoolean("isDiscountFieldChanged");
                }
                amountReceived = jobj.getDouble("enteramount");// amount in payment currency
                amountReceivedConverted = jobj.getDouble("enteramount"); // amount in invoice currency
                
                double tdsamt = jobj.optDouble("tdsamount", 0.0);
                pd.setTdsamount(tdsamt);
                pd.setAmount(jobj.getDouble("enteramount"));
                pd.setDiscountAmount(discountAmount);
                pd.setDiscountFieldEdited(isDiscountFieldChanged);

                pd.setCompany(company);
                KwlReturnObject result = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), jobj.getString("documentid"));
                GoodsReceipt goodsReceipt = (GoodsReceipt) result.getEntityList().get(0);
                pd.setGoodsReceipt(goodsReceipt);
                if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(goodsReceipt.getCurrency().getCurrencyID()) && !goodsReceipt.getCurrency().getCurrencyID().equals(payment.getCurrency().getCurrencyID())) {
                    pd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
                    pd.setFromCurrency(goodsReceipt.getCurrency());
                    pd.setToCurrency(payment.getCurrency());
                    double adjustedRate = jobj.optDouble("amountdue", 0) / jobj.optDouble("amountDueOriginal", 0);
                    amountReceivedConverted = amountReceived / adjustedRate; 
                    amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid); 
                    discountAmountInInvoiceCurrecny = authHandler.round((discountAmount / adjustedRate), company.getCompanyID()); 
                    pd.setAmountInGrCurrency(amountReceivedConverted);
                    pd.setAmountDueInGrCurrency(jobj.optDouble("amountDueOriginal", 0));
                    pd.setAmountDueInPaymentCurrency(jobj.optDouble("amountdue", 0));
                } else {
                    pd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
                    pd.setFromCurrency(goodsReceipt.getCurrency());
                    pd.setToCurrency(payment.getCurrency());
                    amountReceivedConverted = authHandler.round(amountReceived, companyid);
                    discountAmountInInvoiceCurrecny = discountAmount;
                    pd.setAmountInGrCurrency(amountReceivedConverted);
                    pd.setAmountDueInGrCurrency(jobj.optDouble("amountDueOriginal", 0));
                    pd.setAmountDueInPaymentCurrency(jobj.optDouble("amountdue", 0));
                }
                pd.setDiscountAmountInInvoiceCurrency(discountAmountInInvoiceCurrecny);
                pd.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate", 0.0));
                pd.setDescription(StringUtil.DecodeText(jobj.optString("description")));
                if (invoice != null) {
                    pd.setInvoice(invoice);
                }
                pd.setPayment(payment);
                if (jobj.has("rowjedid")) {
                    pd.setROWJEDID(jobj.getString("rowjedid"));
                }
                if (jobj.has("jedetail") && jobj.get("jedetail") != null) {
                    KwlReturnObject resJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) jobj.get("jedetail"));
                    JournalEntryDetail jedObj = (JournalEntryDetail) resJED.getEntityList().get(0);
                    pd.setTotalJED(jedObj);
                }
                HashMap<String, JSONArray> jcustomarrayMap = payment.getJcustomarrayMap();
                if (jobj.has("srNoForRow")) {
                    int srNoForRow = StringUtil.isNullOrEmpty("srNoForRow") ? 0 : Integer.parseInt(jobj.getString("srNoForRow"));
                    pd.setSrNoForRow(srNoForRow);
                }
                JSONArray jcustomarray = jcustomarrayMap.get(pd.getROWJEDID());
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                customrequestParams.put("modulerecid", pd.getROWJEDID());
                customrequestParams.put("recdetailId", pd.getID());
                customrequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), pd.getROWJEDID());
                    AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                    KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), pd.getROWJEDID());
                    JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                    journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
                }


                if (jobj.has("methodjedid") && !StringUtil.isNullOrEmpty(jobj.optString("methodjedid"))) {
                    customrequestParams.clear();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                    customrequestParams.put("modulerecid", jobj.optString("methodjedid"));
                    customrequestParams.put("recdetailId", pd.getID());
                    customrequestParams.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                    customrequestParams.put("companyid", companyid);
                    customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                    customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), jobj.optString("methodjedid"));
                        AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                        KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), jobj.optString("methodjedid"));
                        JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                        journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
                    }
                }


                double amountReceivedConvertedInBaseCurrency = 0d;
                double discountAmountConvertedInBaseCurrency = 0d;
                double amountReceivedAndDiscountInBase = 0d;
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, company.getCompanyID());
                requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                /*
                 * Amount paid against Invoice will be converted to base
                 * currency as per spot rate of invoice
                 */
                double grExternalCurrencyRate = 0d;
                Date grCreationDate = null;
                grCreationDate = goodsReceipt.getCreationDate();
                if (goodsReceipt.isIsOpeningBalenceInvoice() && !goodsReceipt.isNormalInvoice()) {
                    grExternalCurrencyRate = goodsReceipt.getExchangeRateForOpeningTransaction();
                } else {
                    grExternalCurrencyRate = goodsReceipt.getJournalEntry().getExternalCurrencyRate();
                }
                String fromcurrencyid = goodsReceipt.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                KwlReturnObject bDiscountAmt = null;
                if (goodsReceipt.isIsOpeningBalenceInvoice() && !goodsReceipt.isNormalInvoice()) {
                    if (goodsReceipt.isConversionRateFromCurrencyToBase()) {
                        grExternalCurrencyRate = 1 / grExternalCurrencyRate;
                    }
                }
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountReceivedConverted, fromcurrencyid, grCreationDate, grExternalCurrencyRate);
                amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
                amountReceivedConvertedInBaseCurrency = authHandler.round(amountReceivedConvertedInBaseCurrency, companyid);
                pd.setAmountInBaseCurrency(amountReceivedConvertedInBaseCurrency);

                bDiscountAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, discountAmountInInvoiceCurrecny, fromcurrencyid, grCreationDate, grExternalCurrencyRate);
                discountAmountConvertedInBaseCurrency = (Double) bDiscountAmt.getEntityList().get(0);
                discountAmountConvertedInBaseCurrency = authHandler.round(discountAmountConvertedInBaseCurrency, companyid);
                double paymentJournalEntryExchangeRate = payment.getJournalEntry() != null ? payment.getJournalEntry().getExternalCurrencyRate() : 1;
                amountReceivedAndDiscountInBase = ((Double) bAmt.getEntityList().get(0) + (Double) bDiscountAmt.getEntityList().get(0));
                amountReceivedAndDiscountInBase = authHandler.round(amountReceivedAndDiscountInBase, companyid);
                
//                KwlReturnObject bAmountReceivedAndDiscount = null;
//                bAmountReceivedAndDiscount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, (amountReceivedConverted+discountAmountInInvoiceCurrecny), fromcurrencyid, grCreationDate, grExternalCurrencyRate);
//                amountReceivedAndDiscountInBase = (Double) bAmountReceivedAndDiscount.getEntityList().get(0);
                /**
                 * if payment currency is USD which is not a base currency and
                 * document currency is in Malaysia than the exchange rate is
                 * taken from payment's journal entry to convert the discount
                 * amount in base i.e is in SGD.
                 */
                if (paymentJournalEntryExchangeRate != 1 && (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(goodsReceipt.getCurrency().getCurrencyID()) && !goodsReceipt.getCurrency().getCurrencyID().equals(payment.getCurrency().getCurrencyID()))) {
                    bDiscountAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, discountAmount, fromcurrencyid, grCreationDate, paymentJournalEntryExchangeRate);
                    pd.setDiscountAmountInBase(authHandler.round((Double) bDiscountAmt.getEntityList().get(0), companyid));
                } else if (paymentJournalEntryExchangeRate != 1) {          //if payment currency us USD which is not a base currency and document currency is USD then getting the exchange rate from payment's journal entry to convert the discount amount in base i.e is in SGD.
                    bDiscountAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, discountAmountInInvoiceCurrecny, fromcurrencyid, grCreationDate, paymentJournalEntryExchangeRate);
                    pd.setDiscountAmountInBase(authHandler.round((Double) bDiscountAmt.getEntityList().get(0), companyid));
                } else {            //if payment currency is in SGD which is a base currency and document currency is USD then getting the exchange rate from line level exchange rate column to convert the discount amount in base currency i.e SGD.
                    pd.setDiscountAmountInBase(authHandler.round(discountAmountInInvoiceCurrecny * pd.getExchangeRateForTransaction(), companyid));
                }
                
                KwlReturnObject goodsReceiptResult = null;
                if (payment.getApprovestatuslevel() == 11) {
//                    goodsReceiptResult = updateInvoiceAmountDueAndReturnResult(goodsReceipt, payment, company, (amountReceivedConverted+discountAmountInInvoiceCurrecny), (amountReceivedConvertedInBaseCurrency+discountAmountConvertedInBaseCurrency));
                    goodsReceiptResult = updateInvoiceAmountDueAndReturnResult(goodsReceipt, payment, company, (amountReceivedConverted+discountAmountInInvoiceCurrecny), (amountReceivedAndDiscountInBase));
                }

                if (goodsReceiptResult != null && goodsReceiptResult.getEntityList() != null && goodsReceiptResult.getEntityList().size() > 0) {
                    GoodsReceipt gr = (GoodsReceipt) goodsReceiptResult.getEntityList().get(0);
                    if (gr.isIsOpeningBalenceInvoice() && gr.getOpeningBalanceAmountDue() == 0) {
                        try {
                            DateFormat df = authHandler.getDateOnlyFormatter(paramJobj);
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();
                            dataMap.put("amountduedate", df.parse(paramJobj.optString("creationdate")));
                            accGoodsReceiptobj.saveGoodsReceiptAmountDueZeroDate(gr, dataMap);
                        } catch (Exception ex) {
                            System.out.println("" + ex.getMessage());
                        }
                    } else if (gr.getInvoiceamountdue() == 0) {
                        try {
                            DateFormat df = authHandler.getDateOnlyFormatter(paramJobj);
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();
                            dataMap.put("amountduedate", df.parse(paramJobj.optString("creationdate")));
                            accGoodsReceiptobj.saveGoodsReceiptAmountDueZeroDate(gr, dataMap);
                        } catch (Exception ex) {
                            System.out.println("" + ex.getMessage());
                    }
                }
                }
                if (payment != null) {
                    double receiptAmountDue = payment.getOpeningBalanceAmountDue();
                    receiptAmountDue -= amountReceived;
                    payment.setOpeningBalanceAmountDue(receiptAmountDue);
                    payment.setOpeningBalanceBaseAmountDue(payment.getOpeningBalanceBaseAmountDue() - amountReceivedConvertedInBaseCurrency);
                }
                if (jobj.has("appliedTDS") && !StringUtil.isNullOrEmpty(jobj.getString("appliedTDS"))) {   // TDS Applicable Flow Start in Invoice Details...
                    JSONArray jsonArray = new JSONArray(jobj.getString("appliedTDS"));
                    JSONObject tdsDetails = new JSONObject();
                    tdsDetails.put("appliedTDS", jsonArray);
                    tdsDetails.put("documenttype", jobj.getString("type"));

                    HashMap<String, Object> tdsRequestParams = new HashMap();
                    tdsRequestParams.put("tdsDetailsJsonObj", tdsDetails);
                    tdsRequestParams.put("paymentDetailObj", pd);
                    tdsRequestParams.put("companyObj", company);
                    HashSet tdsdetails = saveTDSdetailsRow(tdsRequestParams);
                    if (!tdsdetails.isEmpty() || tdsdetails.size() > 0) {
                        pd.setTdsdetails(tdsdetails);
                    }
                }
                pdetails.add(pd);
            }
        }
        return pdetails;
    }

    public double ReevalJournalEntryForAdvanceReceipt(JSONObject paramJobj, Receipt receipt, double linkReceiptAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {
        double finalAmountReval = 0;
        try {
            String basecurrency = paramJobj.optString(Constants.globalCurrencyKey);
            double ratio = 0;
            double amountReval = 0;
            Date tranDate = null;
            double exchangeRate = 0.0;
            double exchangeRateReval = 0.0;
            double amountdue = linkReceiptAmount;
            Map<String, Object> GlobalParams = new HashMap<>();
            GlobalParams.put("companyid", paramJobj.optString(Constants.companyKey));
            GlobalParams.put("gcurrencyid", basecurrency);
            GlobalParams.put("dateformat", authHandler.getDateOnlyFormat());
            exchangeRate = receipt.getJournalEntry().getExternalCurrencyRate();
            exchangeRateReval = exchangeRate;
            tranDate = receipt.getCreationDate();
            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", receipt.getID());
            invoiceId.put("companyid", paramJobj.optString(Constants.companyKey));
            invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
            //Checking the document entery in revalution history if any for current rate
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (revalueationHistory != null) {
                exchangeRateReval = revalueationHistory.getEvalrate();
            }
            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (receipt.getCurrency() != null) {
                currid = receipt.getCurrency().getCurrencyID();
            }
            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            double oldrate = (Double) bAmt.getEntityList().get(0);
            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRateReval);
            double newrate = (Double) bAmt.getEntityList().get(0);
            ratio = oldrate - newrate;
            if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                exchangeratefortransaction = newrate;
            }
            double amountdueNew = amountdue / exchangeratefortransaction;
            amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
            amountReval = ratio * amountdueNew;
            finalAmountReval = finalAmountReval + amountReval;

        } catch (SessionExpiredException | ServiceException e) {
            throw ServiceException.FAILURE("ReevalJournalEntryForAdvanceReceipt : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }

    public String PostJEFORReevaluation(JSONObject paramJobj, double finalAmountReval, String companyid, CompanyAccountPreferences preferences, String basecurrency, String oldRevaluationJE, Map<String, Object> counterMap) {
        String jeid = "";
        try {
            String jeentryNumber = null;
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";
            boolean jeautogenflag = true;
            DateFormat df = authHandler.getDateOnlyFormat();
            /**
             * added Link Date to Realised JE. while link Advanced Payment to
             * Reevaluated Invoice. Use 'linkingdateString'
             */
            String creationDate = !StringUtil.isNullOrEmpty(paramJobj.optString("linkingdateString","")) ? paramJobj.optString("linkingdateString","") : paramJobj.optString("creationdate");
            Date entryDate = StringUtil.isNullOrEmpty(creationDate) ? new Date() : df.parse(creationDate);
            int counter = (Integer) counterMap.get("counter");
            synchronized (this) {
                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                JEFormatParams.put("modulename", "autojournalentry");
                JEFormatParams.put("companyid", companyid);
                JEFormatParams.put("isdefaultFormat", true);

                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                if (StringUtil.isNullOrEmpty(oldRevaluationJE)) {
                    String nextAutoNoTemp = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    int sequence = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                    sequence = sequence + counter;
                    String number = "" + sequence;
                    String action = "" + (sequence - counter);
                    nextAutoNoTemp.replaceAll(action, number);
                    jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
                    jeSeqFormatId = format.getID();
                    jeIntegerPart = String.valueOf(sequence);
                    jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    jeautogenflag = true;
                    counter++;
                    counterMap.put("counter", counter);
                } else if (!StringUtil.isNullOrEmpty(oldRevaluationJE)) {
                    KwlReturnObject result = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), oldRevaluationJE);
                    JournalEntry entry = (JournalEntry) result.getEntityList().get(0);
                    jeid = entry.getID();
                    jeentryNumber = entry.getEntryNumber();
                    jeSeqFormatId = entry.getSeqformat().getID();
                    jeIntegerPart = String.valueOf(entry.getSeqnumber());
                    jeDatePrefix = entry.getDatePreffixValue();
                    jeDateAfterPrefix = entry.getDateAfterPreffixValue();
                    jeDateSuffix = entry.getDateSuffixValue();
                    result = accJournalEntryobj.deleteJEDtails(oldRevaluationJE, companyid);
                    result = accJournalEntryobj.deleteJE(oldRevaluationJE, companyid);
                }
            }
            boolean creditDebitFlag = true;
            if (finalAmountReval < 0) {
                finalAmountReval = -(finalAmountReval);
                creditDebitFlag = false;
            }
            Map<String, Object> jeDataMapReval = AccountingManager.getGlobalParamsJson(paramJobj);
            jeDataMapReval.put("entrynumber", jeentryNumber);
            jeDataMapReval.put("autogenerated", jeautogenflag);
            jeDataMapReval.put(Constants.SEQFORMAT, jeSeqFormatId);
            jeDataMapReval.put(Constants.SEQNUMBER, jeIntegerPart);
            jeDataMapReval.put(Constants.DATEPREFIX, jeDatePrefix);
            jeDataMapReval.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
            jeDataMapReval.put(Constants.DATESUFFIX, jeDateSuffix);
            jeDataMapReval.put("entrydate", entryDate);
            jeDataMapReval.put("companyid", companyid);
            jeDataMapReval.put("currencyid", basecurrency);
            jeDataMapReval.put("isReval", 2);
            jeDataMapReval.put("transactionModuleid", counterMap.containsKey("transactionModuleid") ? counterMap.get("transactionModuleid") : 0);
            jeDataMapReval.put("transactionId", counterMap.get("transactionId"));
            Set jedetailsReval = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMapReval);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMapReval.put("jeid", jeid);
            JSONObject jedjsonreval = new JSONObject();
            jedjsonreval.put("srno", jedetailsReval.size() + 1);
            jedjsonreval.put("companyid", companyid);
            jedjsonreval.put("amount", finalAmountReval);//rateDecreased?(-1*amountDiff):
            jedjsonreval.put("accountid", preferences.getForeignexchange().getID());
            jedjsonreval.put("debit", creditDebitFlag ? false : true);
            jedjsonreval.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjsonreval);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetailsReval.add(jed);
             /*
             *  Featching Custom field/Dimension Data from Company prefrences.
             */
            String customfield="";
            String lineleveldimensions="";
            KwlReturnObject result =accJournalEntryobj.getRevaluationJECustomData(companyid);
            RevaluationJECustomData revaluationJECustomData= (result != null && result.getEntityList().size() > 0  && result.getEntityList().get(0)!= null) ? (RevaluationJECustomData) result.getEntityList().get(0): null;
            if(revaluationJECustomData != null){
                customfield=revaluationJECustomData.getCustomfield();
                lineleveldimensions=revaluationJECustomData.getLineleveldimensions();
            }
            
            /*        
             * Make dimensions entry
             */
            setDimensionForRevalJEDetail(lineleveldimensions, jed);
            
            String unrealised_accid = "";
            if (preferences.getUnrealisedgainloss() != null) {
                unrealised_accid = preferences.getUnrealisedgainloss().getID();
            } else {

                throw new AccountingException(messageSource.getMessage("acc.field.NoUnrealisedGain/Lossaccountfound", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
            }
            jedjsonreval = new JSONObject();
            jedjsonreval.put("companyid", companyid);
            jedjsonreval.put("srno", jedetailsReval.size() + 1);
            jedjsonreval.put("amount", finalAmountReval);
            jedjsonreval.put("accountid", unrealised_accid);
            jedjsonreval.put("debit", creditDebitFlag ? true : false);
            jedjsonreval.put("jeid", jeid);
            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjsonreval);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetailsReval.add(jed);
            /*        
             * Make dimensions entry
             */
            setDimensionForRevalJEDetail(lineleveldimensions, jed);
            
            jeDataMapReval.put("jedetails", jedetailsReval);
            jeDataMapReval.put("externalCurrencyRate", 0.0);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMapReval);
            /*        
             * Make custom field entry
             */

            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", jeid);
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    Map<String, Object> customjeDataMap = new HashMap<>();
                    customjeDataMap.put("accjecustomdataref", jeid);
                    customjeDataMap.put("jeid", jeid);
                    customjeDataMap.put("istemplate", journalEntry.getIstemplate());
                    customjeDataMap.put("isReval", journalEntry.getIsReval());
                    accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jeid;
    }

    /**
     * Description :This method is used to save Dimension For Reval JEDetail
     */
    public void setDimensionForRevalJEDetail(String lineleveldimensions, JournalEntryDetail jed) {
        try {
            if (!StringUtil.isNullOrEmpty(lineleveldimensions)) {
                JSONArray jcustomarray = new JSONArray(lineleveldimensions);
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                customrequestParams.put("modulerecid", jed.getID());
                customrequestParams.put("recdetailId", jed.getID());
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", jed.getCompany().getCompanyID());
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    //jed.setAccJEDetailCustomData((AccJEDetailCustomData) hibernateTemplate.get(AccJEDetailCustomData.class, jed.getID()));
                    JSONObject jedjsonreval = new JSONObject();
                    jedjsonreval.put("accjedetailcustomdata", jed.getID());
                    jedjsonreval.put("jedid", jed.getID());
                    accJournalEntryobj.updateJournalEntryDetails(jedjsonreval);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccVendorPaymentModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public double ReevalJournalEntryCNDN(JSONObject paramJobj, CreditNote creditNote, CompanyAccountPreferences preferences, double exchangeratefortransaction, double amountdue, Map<String, Object> counterMap) throws SessionExpiredException, ServiceException, AccountingException, JSONException {
        double finalAmountReval = 0;
        try {
            String basecurrency = paramJobj.optString(Constants.globalCurrencyKey);
            double ratio = 0;
            double amountReval = 0;
            Date tranDate = null;
            double exchangeRate = 0.0;
            double exchangeRateReval = 0.0;
            Map<String, Object> GlobalParams = new HashMap<>();
            GlobalParams.put("companyid", paramJobj.optString(Constants.companyKey));
            GlobalParams.put("gcurrencyid", basecurrency);
            GlobalParams.put("dateformat", authHandler.getDateFormatter(paramJobj));
            boolean isopeningBalanceInvoice = creditNote.isIsOpeningBalenceCN();
            tranDate = creditNote.getCreationDate();
            if (!creditNote.isNormalCN()) {
                exchangeRate = creditNote.getExchangeRateForOpeningTransaction();
                exchangeRateReval = exchangeRate;
            } else {
                exchangeRate = creditNote.getJournalEntry().getExternalCurrencyRate();
                exchangeRateReval = exchangeRate;
            }

            Map<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", creditNote.getID());
            invoiceId.put("companyid", paramJobj.optString(Constants.companyKey));
            invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
            //Checking the document entery in revalution history if any for current rate
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (revalueationHistory != null) {
                exchangeRateReval = revalueationHistory.getEvalrate();
            }
            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (creditNote.getCurrency() != null) {
                currid = creditNote.getCurrency().getCurrencyID();
            }
            KwlReturnObject bAmt = null;
            if (isopeningBalanceInvoice && creditNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            }
            double oldrate = (Double) bAmt.getEntityList().get(0);
            //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
            if (revalueationHistory == null && isopeningBalanceInvoice && creditNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRateReval);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRateReval);
            }
            double newrate = (Double) bAmt.getEntityList().get(0);
            ratio = oldrate - newrate;
            if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                exchangeratefortransaction = newrate;
            }

            double amountdueNew = amountdue / exchangeratefortransaction;
            amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
            amountReval = ratio * amountdueNew;
            finalAmountReval = amountReval;
            if (finalAmountReval != 0) {
                String oldRevaluationJE = creditNote.getRevalJeId();
                /**
                 * added transactionID and transactionModuleID to Realised JE.
                 */
                counterMap.put("transactionModuleid", creditNote.isIsOpeningBalenceCN() ? (creditNote.iscNForCustomer() ? Constants.Acc_opening_Customer_CreditNote : Constants.Acc_opening_Vendor_CreditNote) : Constants.Acc_Credit_Note_ModuleId);
                counterMap.put("transactionId", creditNote.getID());
                String revaljeid = PostJEFORReevaluation(paramJobj, finalAmountReval, preferences.getCompany().getCompanyID(), preferences, basecurrency, oldRevaluationJE, counterMap);
                creditNote.setRevalJeId(revaljeid);
                finalAmountReval = 0;
            }

        } catch (SessionExpiredException | ServiceException e) {
            throw ServiceException.FAILURE("saveReceipt : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }

    public void saveBankReconsilationDetails(JSONObject paramJobj, Payment payment) throws ServiceException {
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            DateFormat df = authHandler.getDateOnlyFormat();
            String currencyid = paramJobj.optString(Constants.currencyKey);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);
            String detailsJsonString = paramJobj.optString("Details");;
            JSONArray jSONArray = new JSONArray(detailsJsonString);
            JSONObject obj = new JSONObject(paramJobj.optString("paydetail"));

            if (paramJobj.optString("oldjeid") != null && !StringUtil.isNullOrEmpty((String) paramJobj.optString("oldjeid"))) {
                Map<String, Object> delReqMap = new HashMap<String, Object>();
                delReqMap.put("oldjeid", paramJobj.optString("oldjeid"));
                delReqMap.put("companyId", companyid);
                deleteBankReconcilation(delReqMap);  //deleting bank reconsilation info 
            }

            boolean bankReconsilationEntry = obj.getString("paymentstatus") != null ? obj.getString("paymentstatus").equals("Cleared") : false;
            boolean isNotToReconcil = paramJobj.optBoolean("isNotToReconcil", false);
            if (bankReconsilationEntry && !isNotToReconcil) {
                Map<String, Object> bankReconsilationMap = new HashMap<String, Object>();
                HashMap<String, Object> globalParams = AccountingManager.getGlobalParamsJson(paramJobj);
                String bankAccountId = payMethod.getAccount().getID();
                Date startDate = df.parse(df.format(Calendar.getInstance().getTime()));
                Date endDate = df.parse(df.format(Calendar.getInstance().getTime()));
                Date clearanceDate = df.parse(obj.getString("clearancedate"));
                bankReconsilationMap.put("bankAccountId", bankAccountId);
                bankReconsilationMap.put("startDate", startDate);//dont know the significance so have just put current date 
                bankReconsilationMap.put("endDate", endDate);//dont know the significance so have just put current date
                bankReconsilationMap.put("clearanceDate", clearanceDate);
                bankReconsilationMap.put("endingAmount", 0.0);
                bankReconsilationMap.put("companyId", companyid);
                bankReconsilationMap.put("clearingamount", payment.getDepositAmount());
                bankReconsilationMap.put("currencyid", currencyid);
                bankReconsilationMap.put("details", jSONArray);
                bankReconsilationMap.put("payment", payment);
                bankReconsilationMap.put("ismultidebit", true);
                bankReconsilationMap.put("createdby", paramJobj.optString(Constants.useridKey));
                bankReconsilationMap.put("checkCount", 1);      //As the discussion with Mayur B. and Sagar A. sir MP relates to check count
                bankReconsilationMap.put("depositeCount", 0);
                saveBankReconsilation(bankReconsilationMap, globalParams);
                Map<String, Object> auditRequestParams = new HashMap<>();
                auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
                auditTrailObj.insertAuditLog(AuditAction.BANK_RECONCILIATION_ADDED, "User " + paramJobj.optString(Constants.userfullname) + " has reconciled " + payment.getPaymentNumber(), auditRequestParams, companyid);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("savePayment : " + ex.getMessage(), ex);
        }
    }

    public double getReevalJournalEntryAmount(JSONObject paramJobj, JSONArray detailsJSONArray, Payment payment, int type) throws SessionExpiredException, ServiceException, AccountingException {
        double finalAmountReval = 0;
        try {
            if (detailsJSONArray.length() > 0) {
                String basecurrency = paramJobj.optString(Constants.globalCurrencyKey);
                String companyid = paramJobj.optString(Constants.companyKey);
                
                for (int i = 0; i < detailsJSONArray.length(); i++) {
                    JSONObject jobj = detailsJSONArray.getJSONObject(i);
                    double ratio = 0;
                    double amountReval = 0;
                    double amountdue = jobj.getDouble("enteramount");
                    Date tranDate = null;
                    double exchangeRate = 0.0;
                    double exchangeRateReval = 0.0;
                    double exchangeratefortransaction = jobj.optDouble("exchangeratefortransaction", 1.00);
                    HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
                    GlobalParams.put("companyid", paramJobj.optString(basecurrency));
                    GlobalParams.put("gcurrencyid", basecurrency);
                    GlobalParams.put("dateformat", authHandler.getDateOnlyFormat());
                    Date creationDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("creationdate"));
                    KwlReturnObject result = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), jobj.getString("documentid"));
                    GoodsReceipt gr = (GoodsReceipt) result.getEntityList().get(0);
                    boolean isopeningBalanceInvoice = gr.isIsOpeningBalenceInvoice();
                    tranDate = gr.getCreationDate();
                    if (!gr.isNormalInvoice()) {
                        exchangeRate = gr.getExchangeRateForOpeningTransaction();
                        exchangeRateReval = exchangeRate;
                    } else {
                        exchangeRate = gr.getJournalEntry().getExternalCurrencyRate();
                        exchangeRateReval = exchangeRate;
                    }

                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                    invoiceId.put("invoiceid", gr.getID());
                    invoiceId.put("companyid", companyid);
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    //Checking the document entery in revalution history if any for current rate
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (revalueationHistory != null) {
                        exchangeRateReval = revalueationHistory.getEvalrate();
                    }

                    result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
                    KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
                    String currid = currency.getCurrencyID();
                    if (gr.getCurrency() != null) {
                        currid = gr.getCurrency().getCurrencyID();
                    }
                    KwlReturnObject bAmt = null;
                    if (isopeningBalanceInvoice && gr.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
                    }
                    double oldrate = (Double) bAmt.getEntityList().get(0);
                    if (revalueationHistory == null && isopeningBalanceInvoice && gr.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
                    } else {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
                    }
                    double newrate = (Double) bAmt.getEntityList().get(0);
                    ratio = oldrate - newrate;
                    if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                        exchangeratefortransaction = newrate;
                    }

                    double amountdueNew = amountdue / exchangeratefortransaction;
                    amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
                    amountReval = ratio * amountdueNew;
                    finalAmountReval = authHandler.round((finalAmountReval + amountReval), companyid);
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }
    /*
     * Function to update invoiceamountdue and return KwlReturnObject
     */

 public KwlReturnObject updateInvoiceAmountDueAndReturnResult(GoodsReceipt goodsReceipt, Payment payment, Company company, double amountReceivedForGoodsReceipt, double baseAmountReceivedForGoodsReceipt) throws JSONException, ServiceException {
        KwlReturnObject result = null;
        if (goodsReceipt != null) {
            boolean isInvoiceIsClaimed = (goodsReceipt.getBadDebtType() == Constants.Invoice_Claimed || goodsReceipt.getBadDebtType() == Constants.Invoice_Recovered);
            Map<String, Object> greceipthm = new HashMap<String, Object>();
            greceipthm.put("grid", goodsReceipt.getID());;
            greceipthm.put("companyid", company.getCompanyID());
            if (isInvoiceIsClaimed) {
                greceipthm.put(Constants.claimAmountDue, goodsReceipt.getClaimAmountDue() - amountReceivedForGoodsReceipt);
            } else {
                double invoiceAmountDue = goodsReceipt.getOpeningBalanceAmountDue();
                invoiceAmountDue -= amountReceivedForGoodsReceipt;
                greceipthm.put("grid", goodsReceipt.getID());;
                greceipthm.put("companyid", company.getCompanyID());
                greceipthm.put("openingBalanceAmountDue", invoiceAmountDue);
                greceipthm.put(Constants.openingBalanceBaseAmountDue, goodsReceipt.getOpeningBalanceBaseAmountDue() - baseAmountReceivedForGoodsReceipt);
                greceipthm.put(Constants.invoiceamountdue, goodsReceipt.getInvoiceamountdue() - amountReceivedForGoodsReceipt);
                greceipthm.put(Constants.invoiceamountdueinbase, goodsReceipt.getInvoiceAmountDueInBase() - baseAmountReceivedForGoodsReceipt);
            }
            result = accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
        }
        return result;
    }

    private void saveBankReconsilation(Map<String, Object> requestParams, Map<String, Object> globalParams) throws ServiceException, JSONException, UnsupportedEncodingException {
        HashMap<String, Object> brMap = new HashMap<String, Object>();
        KwlReturnObject crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(globalParams, (Double) requestParams.get("clearingamount"), (String) requestParams.get("currencyid"), (Date) requestParams.get("clearanceDate"), 0);
        double clearingAmount = (Double) crresult.getEntityList().get(0);
        boolean isOpeningPayment = false;
        HashSet hs = new HashSet();
        String billid = "";
        String jeid = "";
        double amount = 0;
        if (requestParams.containsKey("isOpeningPayment")) {
            isOpeningPayment = Boolean.parseBoolean(requestParams.get("isOpeningPayment").toString());
        }
        if (requestParams.containsKey("oldjeid")) {
            deleteBankReconcilation(requestParams);
        }

        brMap.put("startdate", (Date) requestParams.get("startDate"));
        brMap.put("enddate", (Date) requestParams.get("endDate"));
        brMap.put("clearanceDate", (Date) requestParams.get("clearanceDate"));
        brMap.put("clearingamount", (0 - clearingAmount));
        brMap.put("endingamount", (Double) requestParams.get("endingAmount"));
        brMap.put("accountid", (String) requestParams.get("bankAccountId"));
        brMap.put("companyid", (String) requestParams.get("companyId"));
        brMap.put("checkCount", (Integer) requestParams.get("checkCount"));
        brMap.put("depositeCount", (Integer) requestParams.get("depositeCount"));
        brMap.put("createdby", (String) requestParams.get("createdby"));
        KwlReturnObject brresult = accBankReconciliationObj.addBankReconciliation(brMap);
        BankReconciliation br = (BankReconciliation) brresult.getEntityList().get(0);
        String brid = br.getID();
        Payment payment = (Payment) requestParams.get("payment");
        String accountName = "";
        int moduleID = 0;
        if (!isOpeningPayment) {
            JournalEntry entry = payment.getJournalEntry();
            Set details = entry.getDetails();
            Iterator iter = details.iterator();
            while (iter.hasNext()) {
                JournalEntryDetail d = (JournalEntryDetail) iter.next();
                if (!d.isDebit()) {
                    continue;
                }
                accountName += d.getAccount().getName() + ", ";
            }

            //Calculate the Amount.
            JSONArray jArr = (JSONArray) requestParams.get("details");
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.optDouble("enteramount", 0) != 0) {
                    KwlReturnObject crresult1 = accCurrencyDAOobj.getCurrencyToBaseAmount(globalParams, jobj.optDouble("enteramount",0.0), jobj.optString(Constants.currencyKey), (Date) requestParams.get("clearanceDate"), 0);
                    double amt = (Double) crresult1.getEntityList().get(0);
                    if (jobj.optBoolean("debit", true)) {
                        amount += amt;
                    } else {
                        amount -= amt;
                    }
                }
            }
            jeid = entry.getID();
            billid = null;
            moduleID = Constants.Acc_GENERAL_LEDGER_ModuleId;
        } else {
            jeid = null;
            billid = payment.getID();
            amount = clearingAmount;
            moduleID = Constants.Acc_Make_Payment_ModuleId;
            accountName = "";
        }
        accountName = accountName.substring(0, Math.max(0, accountName.length() - 2));

        HashMap<String, Object> brdMap = new HashMap<>();
        brdMap.put("companyid", (String) requestParams.get("companyId"));
        brdMap.put("amount", amount);
        brdMap.put("jeid", jeid);
        brdMap.put("accountname", accountName);
        brdMap.put("debit", false);
        brdMap.put("brid", brid);
        brdMap.put("transactionID", billid);
        brdMap.put("moduleID", moduleID);
        brdMap.put("isOpeningTransaction", isOpeningPayment);
        brdMap.put("reconcileDate", (Date) requestParams.get("clearanceDate"));

        KwlReturnObject brdresult1 = accBankReconciliationObj.addBankReconciliationDetail(brdMap);
        BankReconciliationDetail brd1 = (BankReconciliationDetail) brdresult1.getEntityList().get(0);
        hs.add(brd1);
    }
    
    /**
     * Description: Validate and Import Make Payment data
     *
     * @param paramJobj
     * @return JSONObject
     */
    @Override
    public JSONObject importMakePaymentJSON(JSONObject paramObj) {
        JSONObject jobj = new JSONObject();
        try {
            String doAction = paramObj.getString("do");

            if (doAction.compareToIgnoreCase("import") == 0) {
                jobj = importMakePaymentRecordsForCSV(paramObj);
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                String eParams = paramObj.getString("extraParams");
                JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);

                HashMap<String, Object> requestParams = importHandler.getImportRequestParams(paramObj);
                requestParams.put("extraParams", extraParams);
                requestParams.put("extraObj", null);
                requestParams.put("servletContext", paramObj.get("servletContext"));
                requestParams.put("customer", paramObj.optString("customer"));
                requestParams.put("vendor", paramObj.optString("vendor"));
                requestParams.put("GL", paramObj.optString("GL"));
                
                jobj = importHandler.validateFileData(requestParams);
                jobj.put(Constants.RES_success, true);
            }
        } catch (Exception ex) {
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(AccVendorPaymentModuleServiceImpl.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return jobj;
    }
    public JSONObject importMakePaymentRecordsForCSV(JSONObject requestJobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        JSONObject returnObj = new JSONObject();
        String msg = "";
        int total = 0, failed = 0;
        String fileName = requestJobj.getString("filename");
        String companyID = requestJobj.getString(Constants.companyKey);
        String masterPreference = requestJobj.getString("masterPreference");
        boolean issuccess = true;
        boolean isAlreadyExist = false;
        boolean isRecordFailed = false;
        FileInputStream fileInputStream = null;
        CsvReader csvReader = null;
        JSONObject paramJobj = new JSONObject();
        JSONObject paydetailObj = new JSONObject();
        JSONArray rows = new JSONArray();
        PaymentMethod paymentMethod = null;
        GoodsReceipt invoice = null;
        CreditNote creditNote = null;
        Account account = null;
        double totalAmountPay = 0;
        String exceptionMSg = "";
        String prevRecNo = "";
        double exchangeRateForTransaction = 0.0;
        int isAdvance = 0;
        String paidTo="";
        String paidToId="";
        String description="";
        String[] recarr = null;
        String previousEntryNo="";              //to store previous entry no
        String mismatchColumns = "";            //To store the names of mismatched global columns
        try {
            String dateFormat = null, dateFormatId = requestJobj.getString("dateFormat");
            String customerCheck = requestJobj.optString("customer");
            String vendorCheck = requestJobj.optString("vendor");
            String glCheck = requestJobj.optString("GL");
            boolean isCustomer = false;
            boolean isVendor = false;
            boolean isGL = false;
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {                          //Getting Date Format
                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }
            DateFormat df = new SimpleDateFormat(dateFormat);
            df.setLenient(false);

            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");

            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);     //getting company extra preferences
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            boolean isCurrencyCode = extrareferences.isCurrencyCode();

            String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            File filePath = new File(destinationDirectory + File.separator + fileName);
            fileInputStream = new FileInputStream(filePath);
            String delimiterType = requestJobj.getString("delimiterType");
            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);

            JSONObject resjson = new JSONObject(requestJobj.getString("resjson"));
            JSONArray jSONArray = resjson.getJSONArray("root");

            HashMap<String, Integer> columnConfig = new HashMap<>();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }

            int cnt = 0;
            int countRec = 1;
            StringBuilder failedRecords = new StringBuilder();
            HashMap currencyMap = getCurrencyMap(isCurrencyCode);                        
            
            KwlReturnObject result = null;
            int nocount = 0;
            if (!StringUtil.isNullOrEmpty(customerCheck)) {                     //checking import make payment against customer
                isCustomer = Boolean.parseBoolean(customerCheck);
            }
            if (!StringUtil.isNullOrEmpty(vendorCheck)) {                       //checking import make payment against vendor    
                isVendor = Boolean.parseBoolean(vendorCheck);
            }
            if (!StringUtil.isNullOrEmpty(glCheck)) {                           //checking import make payment against GL
                isGL = Boolean.parseBoolean(glCheck);
            }
            
            String failureMsg = "";
            String failureMsgFromSave = "";      
            List<String> failureMsgsList = new ArrayList<>();                   //List to store the failure Msgs as we have to append all the messages at last of each records.
            boolean glCheckFlag=false;                                          //This flag is used to check wether there is only alone GL entry in case of Make payment to Vendor.
            List <String []> recList=new ArrayList<>();                         //List to store the records as we have to append all the error messages at last as one payment can have multiple line level item.
            boolean globalFieldMismatchFlag = false;                            //This flag is use to check wether Global field is are mismatched or not.
            while (csvReader.readRecord()) {
                if(cnt>2){
                    recList.add(recarr);
                }
                failureMsg = "";
                recarr = csvReader.getValues();

                if (cnt == 0) {
                    failedRecords.append(acSalesOrderServiceObj.createCSVrecord(recarr)).append("\" \"");
                } else if (cnt == 1) {
                    failedRecords.append("\n").append(acSalesOrderServiceObj.createCSVrecord(recarr)).append("\"Error Message\"");
                }else {
                    try {                        
                        String currencyID = requestJobj.getString(Constants.globalCurrencyKey);

                        String entryNumber = "";
                        if (columnConfig.containsKey("billno")) {                                                   //columnConfig is in request To check  
                            entryNumber = recarr[(Integer) columnConfig.get("billno")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(entryNumber)) {
                                failureMsg += "Payment Number is not available. ";
                            }
                        } else {
                            failureMsg += "Payment Number column is not found. ";
                        }
                        
                        Date billDate = null;
                        if (columnConfig.containsKey("creationdate")) {
                            String receivePaymentDateStr = recarr[(Integer) columnConfig.get("creationdate")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(receivePaymentDateStr)) {
                                failureMsg += "Creation Date is not available. ";
                            } else {
                                try {
                                    billDate = df.parse(receivePaymentDateStr);
                                    
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect date format for Creation Date, Please specify values in " + dateFormat + " format. ";
                                }
                            }
                        } else {
                            failureMsg += "Creation Date column is not found. ";
                        }

                         /*
                         * To search vendor by vendor code if it is present create a vendor object
                         *  else show an error message
                         */                                                
                        String vendorId = "";
                        String vendorCode = "";
                        Vendor vendor = null;
                        if (StringUtil.isNullOrEmpty(vendorId) && isVendor) {
                            if (columnConfig.containsKey("acccode")) {
                                vendorCode = recarr[(Integer) columnConfig.get("acccode")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(vendorCode)) {
                                    KwlReturnObject retObj = accVendorDAOObj.getVendorByCode(vendorCode, companyID);
                                    if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                        vendor = (Vendor) retObj.getEntityList().get(0);
                                    }
                                    if (vendor != null) {
                                        vendorId = vendor.getID();
                                    } else {
                                        failureMsg += messageSource.getMessage("acc.field.VendorisnotfoundforVendorCode", null, Locale.forLanguageTag(Constants.language));
                                    }
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.VendorisnotfoundforVendorCode", null, Locale.forLanguageTag(Constants.language));
                                }
                            } else {
                                failureMsg += messageSource.getMessage("acc.field.VendorisnotfoundforVendorCode", null, Locale.forLanguageTag(Constants.language));
                            }
                        }
                        
                        /*
                         * Customer Name if customerID is empty it means
                         * customer is not found for given code. so need to
                         * search data on name
                         */

                        String customerID="";    
                        if (StringUtil.isNullOrEmpty(customerID) && isCustomer) {
                            if (columnConfig.containsKey("acccode")) {
                                String customerCode = recarr[(Integer) columnConfig.get("acccode")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(customerCode)) {
                                    Customer customer = null;
                                    KwlReturnObject retObj = accCustomerDAOObj.getCustomerByCode(customerCode, companyID);
                                    if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                        customer = (Customer) retObj.getEntityList().get(0);
                                    }
                                    if (customer != null) {
                                        customerID = customer.getID();
                                    } else {
                                        failureMsg += messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCode", null, Locale.forLanguageTag(Constants.language)) + ". ";
                                    }
                                } else {
                                    failureMsg += messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCode", null, Locale.forLanguageTag(Constants.language)) + ".";
                                }
                            } else {
                                failureMsg += messageSource.getMessage("acc.field.CustomerisnotfoundforCustomerCode", null, Locale.forLanguageTag(Constants.language)) + ".";
                            }
                        }
                                                
                        String memo = "";
                        if (columnConfig.containsKey(Constants.memo)) {
                            memo = recarr[(Integer) columnConfig.get(Constants.memo)].replaceAll("\"", "").trim();
                        }
                        
                       /*
                        * To check whether description column is present or not
                        */
                        
                        if (columnConfig.containsKey("description")) {
                            description = recarr[(Integer) columnConfig.get("description")].replaceAll("\"", "").trim();
                        }

                        /*
                         * if currencyname is empty it means currency is not
                         * found for given code. so need to search data on name
                         */
                        String currencyStr = "";
                        KWLCurrency currency = null;
                        if (isCurrencyCode ? columnConfig.containsKey("currencyCode") : columnConfig.containsKey("currencyname")) {
                            currencyStr = isCurrencyCode ? recarr[(Integer) columnConfig.get("currencyname")].replaceAll("\"", "").trim() : recarr[(Integer) columnConfig.get("currencyname")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(currencyStr)) {
                                currencyID = acSalesOrderServiceObj.getCurrencyId(currencyStr, currencyMap);
                                result = accPaymentDAOobj.getCurrency(currencyStr);
                                nocount = result.getRecordTotalCount();
                                List list1 = result.getEntityList();
                                if (nocount > 0) {
                                    currency = (KWLCurrency) list1.get(0);
                                }
                                if (StringUtil.isNullOrEmpty(currencyID)) {
                                    failureMsg += messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))) + ". ";
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    failureMsg += "Currency is not available. ";
                                }
                            }
                        } else {
                            if (!masterPreference.equalsIgnoreCase("1")) {
                                failureMsg += "Currency is not available. ";
                            }
                        }
                                                
                        
                        /*
                         * if DebitType is empty it means credit Type is not
                         * found
                         */
                        String debitTypeStr = "";
                        if (isGL) {
                            if (columnConfig.containsKey("debitType")) {
                                debitTypeStr = recarr[(Integer) columnConfig.get("debitType")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(debitTypeStr)) {
                                    failureMsg += "Type is not available. ";
                                }
                            } else {
                                failureMsg += "Type column is not found. ";
                            }
                        }

                        /*
                         * if exchangeratefortransaction is empty it means
                         * exchange rate for transaction is not found
                         */
                        double exchangeRate = 0;
                        String exchangeRateforTransaction = "";
                        if (columnConfig.containsKey("exchangeratefortransaction")) {
                            exchangeRateforTransaction = recarr[(Integer) columnConfig.get("exchangeratefortransaction")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(exchangeRateforTransaction)) {
                                failureMsg += "exchange rate for transaction is not available. ";
                            } else {
                                try {
                                    exchangeRate = Double.parseDouble(exchangeRateforTransaction);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for exchange rate for transaction. ";
                                }
                            }
                        } else {
                            failureMsg += "exchange rate for transaction column is not found. ";
                        }
                        
                        /*
                         * if exrate is empty it means exchange rate for
                         * document To Payment exchange rate is not found for
                         * given code. so need to search data on name
                         */
                        double documentToPaymentExchangeRate = 0;
                        String documentToPaymentExchangeRateStr = "";
                        if (columnConfig.containsKey("exrate")) {
                            documentToPaymentExchangeRateStr = recarr[(Integer) columnConfig.get("exrate")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(documentToPaymentExchangeRateStr)) {
                                failureMsg += "Document To Payment  exchange rate for transaction is not available. ";
                            } else {
                                try {
                                    documentToPaymentExchangeRate = Double.parseDouble(documentToPaymentExchangeRateStr);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for  document To Payment exchange rate for transaction. ";
                                }
                            }
                        } else {
                            failureMsg += "Document To Payment exchange rate for transaction column is not found. ";
                        }
                        
                        int type = 0;
                        String documenyTypeStr = "";
                        String documenyType = "";
                        if (columnConfig.containsKey("type")) {
                            documenyType = recarr[(Integer) columnConfig.get("type")].replaceAll("\"", "").trim();
                            if (documenyType.equalsIgnoreCase("Advanced/Deposit") || documenyType.equalsIgnoreCase("Advance / Deposit") || documenyType.equalsIgnoreCase("Advance/Deposit") || documenyType.equalsIgnoreCase("Advanced / Deposit")&& isVendor) {
                                documenyTypeStr = Constants.ADVANCE_REFUND;
                            } else if ((documenyType.equalsIgnoreCase("Refund/Deposit")  || documenyType.equalsIgnoreCase("Refund / Deposit")) && isCustomer) {
                                documenyTypeStr = Constants.ADVANCE_REFUND;
                            } else if (documenyType.equalsIgnoreCase("Invoice")) {
                                documenyTypeStr = Constants.INVOICEIMPORT;
                            } else if (documenyType.equalsIgnoreCase("Credit Note")) {
                                documenyTypeStr = Constants.CREDITNOTEIMPORT;
                            } else if (documenyType.equalsIgnoreCase("General Ledger Code")) {
                                documenyTypeStr = Constants.GLIMPORT;
                            }

                            if (StringUtil.isNullOrEmpty(documenyTypeStr)) {
                                failureMsg += "Document Type is not available. ";
                            } else {
                                try {
                                    type = Integer.parseInt(documenyTypeStr);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Document Type. ";
                                }
                            }
                        } else {
                            failureMsg += "Document Type column is not found. ";
                        }
                                                                        
                        /*
                         * if enteramount is empty for Enter Amount is not
                         * found for given code. so need to search data on name
                         */

                        double enterAmount = 0;
                        String enterAmountStr = "";
                        if (columnConfig.containsKey("enteramount")) {
                            enterAmountStr = recarr[(Integer) columnConfig.get("enteramount")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(enterAmountStr)) {
                                failureMsg += "Enter Amount is not available. ";
                            } else {
                                try {
                                    enterAmount = authHandler.roundQuantity(Double.parseDouble(enterAmountStr), companyID);
                                    if ((enterAmount < 0 && !isGL && !documenyTypeStr.equals(Constants.GLIMPORT)) || enterAmount == 0) {
                                        failureMsg += " Enter Amount should be greater than Zero. ";
                                    }
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for Enter Amount. ";
                                }
                            }
                        } else {
                            failureMsg += "Enter Amount column is not found. ";
                        }
                        
                        /*
                         * To check paymentmethod is empty or Column is present or not
                         */
                        String paymentMethodStr = "";
                        if (columnConfig.containsKey("paymentmethod")) {
                            paymentMethodStr = recarr[(Integer) columnConfig.get("paymentmethod")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(paymentMethodStr)) {
                                failureMsg += "Payment Method is not available. ";
                            }
                        } else {
                            failureMsg += "Payment Method column is not found. ";
                        }
                        
                     
                        /*
                         * To check tax Tax found for given code or not
                         * To check taxamount is present or not                             
                         */
                        
                        Tax rowtax = null;
                        double taxAmount = 0;
                        String taxAmountStr = "";
                        if (documenyTypeStr.equals(Constants.GLIMPORT)) {
                            if (columnConfig.containsKey("tax")) {
                                String taxCode = recarr[(Integer) columnConfig.get("tax")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(taxCode)) {
//                                    rowtax = acSalesOrderServiceObj.getGSTByCode(taxCode, companyID);
                                    Map taxMap = new HashMap<>();
                                    taxMap.put(Constants.companyKey, companyID);
                                    taxMap.put(Constants.TAXCODE, taxCode);
                                    ArrayList taxList = importHandler.getTax(taxMap);
                                    if (taxList.get(0) != null) {
                                        rowtax = (Tax) taxList.get(0);
                                        if (columnConfig.containsKey("taxamount")) {
                                            taxAmountStr = recarr[(Integer) columnConfig.get("taxamount")].replaceAll("\"", "").trim();
                                            if (!StringUtil.isNullOrEmpty(taxAmountStr)) {
                                                try {
                                                    taxAmount = authHandler.roundQuantity(Double.parseDouble(taxAmountStr), companyID);
                                                } catch (Exception ex) {
                                                    failureMsg += "Incorrect numeric value for taxamount. ";
                                                }
                                            }
                                        } else {
                                            failureMsg += "Taxamount column is not found. ";
                                        }
                                    } else if (!StringUtil.isNullOrEmpty((String) taxList.get(2))) {
                                        failureMsg += (String) taxList.get(2) + taxCode;
                                    }
                                }
                            }
                        }                                         

                        /*
                         * To check bankInterestCmb is present or not
                         */
                        Account bankInterestAccount = null;
                        String bankInterestCmbStr = "";
                        double bankInterestAmount = 0;
                        String bankInterestAmountStr = "";
                        if (columnConfig.containsKey("bankInterestCmb")) {
                            bankInterestCmbStr = recarr[(Integer) columnConfig.get("bankInterestCmb")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(bankInterestCmbStr)) {
                                result = accPaymentDAOobj.getAccountNameCount(bankInterestCmbStr, companyID);
                                nocount = result.getRecordTotalCount();
                                List accountNameList = result.getEntityList();
                                if (nocount > 0) {
                                    bankInterestAccount = (Account) accountNameList.get(0);
                                } else {
                                    isAlreadyExist = true;
                                    throw new AccountingException("Bank Interest Account '" + bankInterestCmbStr + "' is not available for Account.");
                                }
                                /*
                                 * To check bankInterest is present or not
                                 */
                                if (columnConfig.containsKey("bankInterest")) {
                                    bankInterestAmountStr = recarr[(Integer) columnConfig.get("bankInterest")].replaceAll("\"", "").trim();
                                    if (!StringUtil.isNullOrEmpty(bankInterestAmountStr) && !bankInterestAmountStr.equals("0") && !bankInterestAmountStr.equals("")) {
                                        try {
                                            bankInterestAmount = authHandler.roundQuantity(Double.parseDouble(bankInterestAmountStr), companyID);
                                        } catch (Exception ex) {
                                            failureMsg += "Incorrect numeric value for bank Interest. ";
                                        }
                                    } else {
                                        failureMsg += "Incorrect numeric value for bank Interest. ";
                                    }

                                } else {
                                    failureMsg += "Bank interest column is not found. ";
                                }
                            }
                        }
                        /*
                         * To check bankChargesCmb is present or not
                         */
                        Account bankChargeAccount = null;
                        String bankChargeCmbStr = "";
                        double bankChargeAmount = 0;
                        String bankChargeAmountStr = "";
                        if (columnConfig.containsKey("bankChargesCmb")) {
                            bankChargeCmbStr = recarr[(Integer) columnConfig.get("bankChargesCmb")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(bankChargeCmbStr)) {
                                result = accPaymentDAOobj.getAccountNameCount(bankChargeCmbStr, companyID);
                                nocount = result.getRecordTotalCount();
                                List list1 = result.getEntityList();
                                if (nocount > 0) {
                                    bankChargeAccount = (Account) list1.get(0);
                                } else {
                                    isAlreadyExist = true;
                                    throw new AccountingException("Account Name '" + bankChargeCmbStr + "' is not Available for Account.");
                                }
                                /*
                                 * To check bankChargesAmount is present or not
                                 */
                                
                                if (columnConfig.containsKey("bankCharges")) {
                                    bankChargeAmountStr = recarr[(Integer) columnConfig.get("bankCharges")].replaceAll("\"", "").trim();
                                    if (!StringUtil.isNullOrEmpty(bankChargeAmountStr) && !bankChargeAmountStr.equals("0") && !bankChargeAmountStr.equals("")) {
                                        try {
                                            bankChargeAmount = authHandler.roundQuantity(Double.parseDouble(bankChargeAmountStr), companyID);
                                        } catch (Exception ex) {
                                            failureMsg += "Incorrect numeric value for Bank Charge. ";
                                        }
                                    } else {
                                        failureMsg += "Incorrect numeric value for Bank Charge. ";
                                    }
                                } else {
                                    failureMsg += "bank charge column is not found. ";
                                }
                            }
                        }                       

                        /*
                         * To check  documentno is empty or not present if it is present then will check                          
                         * 1. To check transactionamount is empty or not present  
                         * 2. To check amount due is present or not
                         * 3. To check amountDueOriginal is present or not
                         */
                        
                        String documenyNoStr = "";
                        double transactionAmount = 0;
                        double amountDue = 0;
                        String amountDueStr = "";
                        String originalAmountDueStr = "";
                        double originalAmountDue = 0;                        
                        if (columnConfig.containsKey("documentno")) {
                            documenyNoStr = recarr[(Integer) columnConfig.get("documentno")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(documenyNoStr)) {
                                if (!isGL && !documenyTypeStr.equalsIgnoreCase(Constants.GLIMPORT)) {
                                    if (columnConfig.containsKey("transactionamount")) {
                                        String transactionAmountStr = recarr[(Integer) columnConfig.get("transactionamount")].replaceAll("\"", "").trim();
                                        if (StringUtil.isNullOrEmpty(transactionAmountStr)) {
                                            failureMsg += "Transaction Amount is not available. ";
                                        } else {
                                            try {
                                                transactionAmount = authHandler.roundQuantity(Double.parseDouble(transactionAmountStr), companyID);
                                                if(transactionAmount<=0){
                                                    failureMsg+="Transaction Amount should be greater than zero. ";
                                                }
                                            } catch (Exception ex) {
                                                failureMsg += "Incorrect numeric value for Transaction Amount. ";
                                            }
                                        }
                                    } else {
                                        failureMsg += "Transaction Amount column is not found. ";
                                    }
                                    if (columnConfig.containsKey("amountDue")) {
                                        amountDueStr = recarr[(Integer) columnConfig.get("amountDue")].replaceAll("\"", "").trim();
                                        if (StringUtil.isNullOrEmpty(amountDueStr)) {
                                            failureMsg += "Amount Due is not available. ";
                                        } else {
                                            try {
                                                amountDue = authHandler.roundQuantity(Double.parseDouble(amountDueStr), companyID);
                                                if (amountDue <= 0) {
                                                    failureMsg += "Amount Due should be greater than zero. ";
                                                }
                                            } catch (Exception ex) {
                                                failureMsg += "Incorrect numeric value for Amount Due. ";
                                            }
                                        }
                                    } else {
                                        failureMsg += "Amount Due column is not found. ";
                                    }

                                    if (columnConfig.containsKey("amountDueOriginal")) {
                                        originalAmountDueStr = recarr[(Integer) columnConfig.get("amountDueOriginal")].replaceAll("\"", "").trim();
                                        if (StringUtil.isNullOrEmpty(originalAmountDueStr)) {
                                            failureMsg += " Original Amount Due is not available. ";
                                        } else {
                                            try {
                                                originalAmountDue = authHandler.roundQuantity(Double.parseDouble(originalAmountDueStr), companyID);
                                                if(originalAmountDue<=0){
                                                    failureMsg+="Transaction Amount Due should be greater than zero. ";
                                                }
                                            } catch (Exception ex) {
                                                failureMsg += "Incorrect numeric value for Amount Due. ";
                                            }
                                        }
                                    } else {
                                        failureMsg += "Amount Due column is not found. ";
                                    }
                                }
                            } else if (!documenyTypeStr.equalsIgnoreCase(Constants.ADVANCE_REFUND)) {
                                failureMsg += "The value in document number column cannot be empty. ";
                            }
                        } else {
                            failureMsg += "The document number column is not found. ";
                        }
                        
                        result = accPaymentDAOobj.getPaymentFromBillNo(entryNumber, companyID);
                            nocount = result.getRecordTotalCount();
                            if (nocount > 0) {
                                isAlreadyExist = true;
                                failureMsg += "Payment number '" + entryNumber + "' already exists.";
                            }
                            result = accPaymentDAOobj.getPaymentMethodCount(paymentMethodStr, companyID);
                            nocount = result.getRecordTotalCount();
                            List paymentMethodList = result.getEntityList();

                            if (nocount > 0) {
                                paymentMethod = (PaymentMethod) paymentMethodList.get(0);
                            } else {
                                isAlreadyExist = true;
                                failureMsg += "Payment Method is not Available. ";
                            }
                            
                            String chequeNumberStr = "";
                            Date chequeDate = null;
                            Date clearanceDate = null;
                            String bankNameStr = "";
                            String chequeDes = "";
                            String chequeNumber = "";
                            String paymentStatus = "";
                            
                            /*
                            * if payment method is bank then only check for cheque no, date and bank name
                            */
                            if (paymentMethod != null && paymentMethod.getDetailType() == 2) {
                                /*
                                 * To check chequenumber is present or not
                                 */
                                if (columnConfig.containsKey("chequenumber")) {
                                    chequeNumberStr = recarr[(Integer) columnConfig.get("chequenumber")].replaceAll("\"", "").trim();                                    
                                } else {
                                    failureMsg += "Cheque Number column is not found. ";
                                }
                                
                                /*
                                 * To check chequedate is present or not
                                 */
                                if (columnConfig.containsKey("chequedate")) {
                                    String chequeDateStr = recarr[(Integer) columnConfig.get("chequedate")].replaceAll("\"", "").trim();

                                    if (StringUtil.isNullOrEmpty(chequeDateStr)) {
                                        failureMsg += "Cheque Date is not available. ";
                                    } else {
                                        try {
                                            chequeDate = df.parse(chequeDateStr);
                                        } catch (Exception ex) {
                                            failureMsg += "Incorrect Cheque Date. ";
                                        }
                                    }
                                } else {
                                    failureMsg += "Cheque Date column is not found. ";
                                }
                                
                                /*
                                 * To check bankname is present or not
                                 */
                                if (columnConfig.containsKey("bankname")) {
                                    bankNameStr = recarr[(Integer) columnConfig.get("bankname")].replaceAll("\"", "").trim();

                                    if (StringUtil.isNullOrEmpty(bankNameStr)) {
                                        failureMsg += "Bank Name is not available. ";
                                    }
                                } else {
                                    failureMsg += "Bank Name column is not found. ";
                                }
                                if (columnConfig.containsKey("chequenumber")) {
                                    chequeNumber = recarr[columnConfig.get("chequenumber")];
                                    if (StringUtil.isNullOrEmpty(chequeNumber)) {
                                        failureMsg += "Cheque number is not provided. So cannot be imported record "+entryNumber+".";
                                    } else {
                                        failureMsg += importHandler.isChequeNumberValidForImport(chequeNumber,false);
                                    }
                                } else {
                                        failureMsg += "Cheque number is not provided. So cannot be imported record "+entryNumber+".";
                                }

                                /*
                                 * To check Payment status is present or not
                                 */
                                if (columnConfig.containsKey("paymentstatus")) {
                                    paymentStatus = recarr[(Integer) columnConfig.get("paymentstatus")].replaceAll("\"", "").trim();

                                    if (StringUtil.isNullOrEmpty(paymentStatus)) {
                                        failureMsg += "Payment Status is not available. ";
                                    } else if (!(paymentStatus.equalsIgnoreCase("Cleared") || paymentStatus.equalsIgnoreCase("Uncleared"))) {
                                        failureMsg += "Invalid payment status. ";
                                    }
                                } else {
                                    failureMsg += "Payment Status column is not found. ";
                                }
                          
                                /*
                                 * To check chequedate is present or not
                                 */
                            if (paymentStatus.equalsIgnoreCase("Cleared")) {
                                if (columnConfig.containsKey("clearancedate")) {
                                    if (paymentStatus.equalsIgnoreCase("Cleared")) {
                                        paymentStatus="Cleared";                    //Converting it to proper case as in save method it is compared using equals method
                                        String clearanceDateStr = recarr[(Integer) columnConfig.get("clearancedate")].replaceAll("\"", "").trim();
                                        if (StringUtil.isNullOrEmpty(clearanceDateStr)) {
                                            failureMsg += "Clearance Date is not available. ";
                                        } else {
                                            try {
                                                clearanceDate = df.parse(clearanceDateStr);
                                                if (clearanceDate.before(chequeDate)) {
                                                    failureMsg += "Clearance Date cannot be less than Cheque Date. ";
                                                }
                                            } catch (Exception ex) {
                                                failureMsg += "Incorrect Clearance Date. ";
                                            }
                                        }
                                    }
                                } else {
                                    failureMsg += "Clearance Date column is not found. ";
                                }
                            }

                                if (columnConfig.containsKey("Chequedescription")) {
                                    chequeDes = recarr[(Integer) columnConfig.get("Chequedescription")].replaceAll("\"", "").trim();
                                }
                            }
                            
                            /*
                             * To check whether Paid to column  is  present or not
                             */
                            if (columnConfig.containsKey("paidto")) {
                                paidTo = recarr[(Integer) columnConfig.get("paidto")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(paidTo)) {
                                    String conditionColName="value";
                                    KwlReturnObject masterReturnObj = accMasterItemsDAOObj.getMasterItemByNameorID(companyID, paidTo, Constants.MASTERGROUP_PAIDTO, Constants.Acc_id, conditionColName);
                                    List list = masterReturnObj.getEntityList();
                                    if (!list.isEmpty()) {
                                        paidToId = list.get(0).toString();
                                    } else {
                                        failureMsg += "Paid To entry not found in master list. ";
                                    }
                                }
                            }                      
                             // To create custom field array
                            JSONArray customJArr = new JSONArray();
                            try {
                                customJArr = acSalesOrderServiceObj.createGlobalCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.Acc_Make_Payment_ModuleId);
                            } catch (Exception ex) {
                                failureMsg += ex.getMessage() != null ? ex.getMessage() : " ";
                            }
                            
                            exceptionMSg="";
                            try {
                                if (customJArr.length() > 0) {
                                    exceptionMSg = acSalesOrderServiceObj.isValidCustomFieldData(customJArr);
                                    if (!StringUtil.isNullOrEmpty(exceptionMSg)) {
                                        failureMsg += exceptionMSg;
                                    }
                                }
                               
                            } catch (Exception ex) {
                                failureMsg += "Invalid data entered in custom field.Please check date format,numeric value etc.";
                            }
                            
                            // For adding due date
                            // For getting exchange rate
                            exchangeRateForTransaction = acSalesOrderServiceObj.getExchangeRateForTransaction(requestJobj, billDate, currencyID);
                            if (currency != null && paymentMethod!=null && paymentMethod.getAccount()!=null && paymentMethod.getAccount().getCurrency()!=null) {
                                if (paymentMethod.getAccount().getCurrency().getCurrencyCode() == currency.getCurrencyCode()) {
                                    documentToPaymentExchangeRate = 1;
                                } else if ((paymentMethod.getAccount().getCurrency().getCurrencyID() == currencyID) && currency.getCurrencyID() != currencyID) {
                                    documentToPaymentExchangeRate = (1 / exchangeRateForTransaction);
                                }
                            }
                            
                        if ((!prevRecNo.equalsIgnoreCase(entryNumber)) || entryNumber.equalsIgnoreCase("")) {
                            prevRecNo = entryNumber;
                            StringBuilder ErrorMsgForAdvAndGL = new StringBuilder();
                            if (glCheckFlag) {                                  //if There is only GL entries in payment then glCheckFlag is false so we reject the record and add error message in log file
                            if (rows.length() > 0 && !isRecordFailed) {                                 //getting line level items row count
                                        paramJobj.put("Details", rows.toString());
                                        KwlReturnObject prevPaymentMethodObj = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod", ""));
                                        PaymentMethod prevPaymentMethod = (PaymentMethod) prevPaymentMethodObj.getEntityList().get(0);
                                        if (prevPaymentMethod != null && prevPaymentMethod.getDetailType() == 2) {
                                            paramJobj.put("paydetail", paydetailObj.toString());
                                        }
                                        paramJobj.put("amount", totalAmountPay);
                                        HashMap resultMap = saveVendorPayment(paramJobj);
                                        glCheckFlag = false;                                        
                                        if (!resultMap.isEmpty()) {
                                            if (resultMap.containsKey("jobj")) {
                                                JSONObject jobj = (JSONObject) resultMap.get("jobj");
                                                if (!jobj.optBoolean("success")) {
                                                    failureMsgFromSave += StringUtil.serverHTMLStripper(jobj.getString("msg"));
                                                    ErrorMsgForAdvAndGL.append(StringUtil.serverHTMLStripper(jobj.getString("msg")));
                                                }
                                            }
                                        }
                                }
                                totalAmountPay = 0;
                            } else if (rows.length() > 0) {                     //For first record glCheckFlag is always false but rows length is 0 so checking it because error message should not be appended for first record
                                failureMsgFromSave += messageSource.getMessage("acc.mp.noAdvInvNoteEntry", null, Locale.forLanguageTag(requestJobj.getString(Constants.language)));
                                ErrorMsgForAdvAndGL.append(" ");
                                ErrorMsgForAdvAndGL.append(messageSource.getMessage("acc.mp.noAdvInvNoteEntry", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))));
                            }
                            if (isAdvance > 1) {                    //is to check whether same receipt contains more than one advance line level item
                                failureMsgFromSave += " Make Payment of Advance type is already entered. You can not enter twice.";
                                ErrorMsgForAdvAndGL.append(" Make Payment of Advance type is already entered. You can not enter twice.");
                                isAdvance = 0;
                            }
                            if (!StringUtil.isNullOrEmpty(failureMsgFromSave)) {
                                failed += recList.size();
                                int msgCnt=0;
                                for (String[] rec : recList) {
                                    failedRecords.append("\n").append(acSalesOrderServiceObj.createCSVrecord(rec)).append("\"").append((failureMsgsList.get(msgCnt).replaceAll("\"", "") + ErrorMsgForAdvAndGL.toString()).trim()).append("\"");
                                    msgCnt++;
                                }
                                failureMsgFromSave = "";
                                isRecordFailed = false;
                                ErrorMsgForAdvAndGL=new StringBuilder();
                            }
                            
                            // reset variables
                            paramJobj = new JSONObject();
                            paydetailObj = new JSONObject();
                            rows = new JSONArray();
                            recList=new ArrayList<>();
                            failureMsgsList=new ArrayList<>();
                            totalAmountPay = 0;
                            isAlreadyExist = false;
                            globalFieldMismatchFlag = false;
                            mismatchColumns = "";

                            String sequenceFormatID = "NA";
                            boolean autogenerated = false;
                            boolean isFromOtherSource = false;
                            if (!StringUtil.isNullOrEmpty(entryNumber)) {
                                Map<String, String> sequenceNumberDataMap = new HashMap<String, String>();
                                sequenceNumberDataMap.put("moduleID", String.valueOf(Constants.Acc_Make_Payment_ModuleId));
                                sequenceNumberDataMap.put("entryNumber", entryNumber);
                                sequenceNumberDataMap.put("companyID", companyID);
                                List list = importHandler.checksEntryNumberForSequenceNumber(sequenceNumberDataMap);
                                if (!list.isEmpty()) {
                                    boolean isvalidEntryNumber = (Boolean) list.get(0);
                                    if (!isvalidEntryNumber) {
                                        String formatID = (String) list.get(2);
                                        int intSeq = (Integer) list.get(3);
                                        paramJobj.put(Constants.SEQNUMBER, intSeq);
                                        paramJobj.put(Constants.SEQFORMAT, formatID);
                                        autogenerated = true;
                                        sequenceFormatID = formatID;
                                        isFromOtherSource = true;
                                    }
                                }
                            }
                            paramJobj.put(Constants.companyKey, companyID);
                            paramJobj.put(Constants.globalCurrencyKey, requestJobj.optString(Constants.globalCurrencyKey));
                            paramJobj.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
                            paramJobj.put(Constants.userfullname, requestJobj.optString(Constants.userfullname));
                            paramJobj.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
                            paramJobj.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
                            paramJobj.put(Constants.timezonedifference, requestJobj.optString(Constants.timezonedifference));
                            paramJobj.put(Constants.language, Constants.language);
                            paramJobj.put(Constants.currencyKey, currencyID);
                            paramJobj.put("balaceAmount", 0);
                            paramJobj.put("bankCharges", bankChargeAmount);
                            if (bankChargeAccount != null) {
                                paramJobj.put("bankChargesCmb", bankChargeAccount.getID());
                            } else {
                                paramJobj.put("bankChargesCmb", "");
                            }
                            if (bankInterestAccount != null) {
                                paramJobj.put("bankInterestCmb", bankInterestAccount.getID());
                            } else {
                                paramJobj.put("bankInterestCmb", "");
                            }
                            paramJobj.put("bankInterest", bankInterestAmount);
                            paramJobj.put("billid", "");
                            paramJobj.put("receipttype", "1");
                            paramJobj.put("creationdate", billDate!=null?sdf.format(billDate):"");
                            paramJobj.put("currencyid", currencyID);
                            paramJobj.put("externalcurrencyrate", exchangeRateForTransaction);
                            paramJobj.put("isChequePrint", false);
                            paramJobj.put("isLinkedToClaimedInvoice", false);
                            if (isVendor) {
                                paramJobj.put("iscustomer", false);
                                paramJobj.put("accid", vendorId);
                            } else if (isCustomer) {
                                paramJobj.put("accid", customerID);
                                paramJobj.put("iscustomer", true);
                            }

                            paramJobj.put("ismulticurrencypaymentje", false);
                            paramJobj.put("memo", memo);
                            paramJobj.put("no", entryNumber);
                            paramJobj.put("paidToCmb", paidToId);
                            paramJobj.put("customfield", customJArr.toString());
                            paramJobj.put("PaymentCurrencyToPaymentMethodCurrencyRate", documentToPaymentExchangeRate);
                            paramJobj.put("paymentCurrencyToPaymentMethodCurrencyExchangeRate", documentToPaymentExchangeRate);
                            paramJobj.put("accountIdComPreAdjRec", "");
                            paramJobj.put("sequenceformat", sequenceFormatID);
                            paramJobj.put("autogenerated", autogenerated);
                            paramJobj.put("isFromOtherSource", isFromOtherSource);
                            paramJobj.put("paymentmethodaccname", paymentMethodStr);
                            paramJobj.put("pmtmethod", paymentMethod != null ? paymentMethod.getID() : "");

                            if (paymentMethod != null && paymentMethod.getDetailType() == 2) {
                                paydetailObj.put("chequenumber", chequeNumberStr);
                                paydetailObj.put("paymentstatus", paymentStatus);
                                paydetailObj.put("paymentthroughid", paymentMethod.getID());
                                paydetailObj.put("clearancedate", clearanceDate != null ? sdf.format(clearanceDate) : "");
                                paydetailObj.put("postdate", chequeDate != null ? sdf.format(chequeDate) : "");
                                paydetailObj.put("description", chequeDes);
                                paydetailObj.put("paymentthrough", bankNameStr);
                            }

                            Map<String, Object> requestParams = new HashMap<>();
                            requestParams.put(Constants.companyKey, companyID);
                            if (billDate != null) {
                                CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, billDate, false);
                            }
                        }   //end global details
                        
                        if ((prevRecNo.equalsIgnoreCase(entryNumber)) || entryNumber.equalsIgnoreCase("") && !globalFieldMismatchFlag) {
                            if (paramJobj.has("creationdate")) {
                                if (billDate!=null && !paramJobj.optString("creationdate").equals(sdf.format(billDate))) {
                                    mismatchColumns += "Date,";
                                }
                            }
                            if (paramJobj.has("accid") && isVendor) {
                                if (!paramJobj.optString("accid", "").equals(vendorId)) {
                                    mismatchColumns += "Vendor Code,";
                                }
                            }
                            if (paramJobj.has("accid") && isCustomer) {
                                if (!paramJobj.optString("accid", "").equals(customerID)) {
                                    mismatchColumns += "Customer Code,";
                                }
                            }
                            if (paramJobj.has(Constants.currencyKey)) {
                                if (!paramJobj.optString(Constants.currencyKey, "").equals(currencyID)) {
                                    mismatchColumns += "Currency,";
                                }
                            }
                            if (paramJobj.has("paymentmethodaccname")) {
                                if (!paramJobj.optString("paymentmethodaccname", "").equals(paymentMethodStr)) {
                                    mismatchColumns += " Payment Method,";
                                }
                            }
                            if (paramJobj.has("PaymentCurrencyToPaymentMethodCurrencyRate")) {
                                if (!(paramJobj.optDouble("PaymentCurrencyToPaymentMethodCurrencyRate", 0.0) == documentToPaymentExchangeRate))  {
                                    mismatchColumns += " Document Currency To Payment Method Currency exchange rate,";
                                }
                            }
                            if (paymentMethod != null && paymentMethod.getDetailType() == 2) {                                                                    
                                    if (paydetailObj.has("chequenumber")) {
                                        if (!paydetailObj.optString("chequenumber", "").equals(chequeNumberStr)) {
                                            mismatchColumns += " Cashier's Cheque/Cheque Number,";
                                        }
                                    }
                                    if (paydetailObj.has("paymentthrough")) {
                                        if (!paydetailObj.optString("paymentthrough", "").equals(bankNameStr)) {
                                            mismatchColumns += " Bank Name,";
                                        }
                                    }
                                    if (paydetailObj.has("postdate")) {
                                        if (chequeDate!=null && !paydetailObj.optString("postdate", "").equals(sdf.format(chequeDate))) {
                                            mismatchColumns += " Cheque Date,";
                                        }
                                    }
                                    if (paydetailObj.has("paymentstatus")) {
                                        if (!paydetailObj.optString("paymentstatus", "").equalsIgnoreCase(paymentStatus)) {
                                            mismatchColumns += " Payment Status,";
                                        }
                                    }
                                    if (paydetailObj.has("clearancedate")) {
                                        if (clearanceDate!=null && !paydetailObj.optString("clearancedate", "").equals(sdf.format(clearanceDate))) {
                                            mismatchColumns += " Clearance Date,";
                                        }
                                    }
                                    if (paydetailObj.has("description")) {
                                        if (!paydetailObj.optString("description", "").equals(chequeDes)) {
                                            mismatchColumns += " Reference Number/Description,";
                                        }
                                    }
                                }
                            if (paramJobj.has("memo")) {
                                if (!paramJobj.optString("memo", "").equals(memo)) {
                                    mismatchColumns += " Memo,";
                                }
                            }
                            if (paramJobj.has("paidToCmb")) {
                                if (!paramJobj.optString("paidToCmb", "").equals(paidToId)) {  
                                    mismatchColumns += " Paid To,";
                                }
                            }
                            if (paramJobj.has("bankInterestCmb")) {
                                String bankInterestAccountId = bankInterestAccount != null ? bankInterestAccount.getID() : "";
                                if (!paramJobj.optString("bankInterestCmb", "").equals(bankInterestAccountId)) {
                                    mismatchColumns += " Bank Interest Account,";
                                }
                            }
                            if (paramJobj.has("bankInterest")) {
                                if (!(paramJobj.optDouble("bankInterest", 0.0) == bankInterestAmount)) {
                                    mismatchColumns += " Bank Interest,";
                                }
                            }
                            if (paramJobj.has("bankChargesCmb")) {
                                String bankChargeAccountId = bankChargeAccount != null ? bankChargeAccount.getID() : "";
                                if (!paramJobj.optString("bankChargesCmb", "").equals(bankChargeAccountId)) {
                                    mismatchColumns += "  Bank Charges Account,";
                                }
                            }
                            if (paramJobj.has("bankCharges")) {
                                if (!(paramJobj.optDouble("bankCharges", 0.0) == bankChargeAmount)) {
                                    mismatchColumns += "  Bank Charges,";
                                }
                            }
                            if (!StringUtil.isNullOrEmpty(mismatchColumns)) {
                                globalFieldMismatchFlag = true;
                                failureMsg += " Following Global fields are mismatching: " + mismatchColumns.substring(0, mismatchColumns.length() - 1).trim();
                            }
                        }
                        
                        if (!StringUtil.isNullOrEmpty(failureMsg)) {
                            throw new AccountingException(failureMsg);
                        }
                        // For Line level details
                        // Add Custom fields details of line items
                        JSONArray lineCustomJArr = acSalesOrderServiceObj.createLineLevelCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.Acc_Make_Payment_ModuleId);
                        
                        
                        JSONObject detailData = new JSONObject();
                        if (!isCustomer) {
                            if (documenyTypeStr.equals(Constants.GLIMPORT)) {                  //For GL code
                                result = accPaymentDAOobj.getAccountNameCount(documenyNoStr, companyID);
                                nocount = result.getRecordTotalCount();
                                List glAccountList = result.getEntityList();
                                if (nocount > 0) {
                                    account = (Account) glAccountList.get(0); 
                                    if(!StringUtil.isNullOrEmpty(account.getUsedIn()) && !account.isWantToPostJe()){
                                        throw new AccountingException("Account Name'" + documenyNoStr + "' is not Available for Account as it is used in "+account.getUsedIn());
                                    } else if(!account.isActivate()){
                                        throw new AccountingException("Account Name '" + documenyNoStr + "' is not Available for Account as it is deactivated.");
                                    }
                                } else {
                                    isAlreadyExist = true;
                                    throw new AccountingException("Account Name '" + documenyNoStr + "' is not Available for Account.");
                                }
                            }
                        }
                        String receiptId = "";
                        String receiptCurrencyCode = "";                        
                        double refundReceiptAmount=0;
                        double refundReceiptAmountDue=0;
                        Date documentNoDate = null;
                        if (isCustomer) {
                            if (documenyTypeStr.equals(Constants.ADVANCE_REFUND)) {                  //for refund to customer   
                                if (!StringUtil.isNullOrEmpty(documenyNoStr)) {
                                    result = accPaymentDAOobj.getRefundNameCount(documenyNoStr, companyID, customerID);               //to confirm
                                    List<Object[]> refundReceiptList = result.getEntityList();
                                    if (refundReceiptList.size() > 0) {
                                        for (Object obj[] : refundReceiptList) {
                                            receiptId = obj[0].toString();
                                            receiptCurrencyCode = obj[1].toString();
                                            refundReceiptAmount = Double.parseDouble(obj[2].toString());
                                            refundReceiptAmountDue = Double.parseDouble(obj[3].toString());
                                            documentNoDate = (Date) obj[4];
                                        }
                                        if (currency !=null && receiptCurrencyCode.equalsIgnoreCase(currency.getCurrencyCode())) {
                                            exchangeRate = 1;
                                        }else{
                                            refundReceiptAmountDue=authHandler.round(refundReceiptAmountDue*exchangeRate,companyID);
                                        }
                                    }
                                    if (refundReceiptList.size() <= 0) {
                                        isAlreadyExist = true;
                                        throw new AccountingException("Receipt No '" + documenyNoStr + "' is not Available for Refund. ");
                                    }
                                    if (enterAmount > refundReceiptAmountDue) {
                                        throw new AccountingException("Enter Amount Should not be greater than  amount due. ");
                                    }
                                }
                            }
                        }
                        String currencysymboltransaction = "";
                        String paymentAccountCur = paymentMethod !=null ? paymentMethod.getAccount() != null ? paymentMethod.getAccount().getCurrency().getCurrencyID() :"" :"";
                        if (isVendor) {
                            if (documenyTypeStr.equals(Constants.INVOICEIMPORT)) {              //For invoice(GoodsReceipt)
                                JSONObject grDetails=new JSONObject();
                                grDetails.put("grNo", documenyNoStr);
                                grDetails.put(Constants.companyKey, companyID);
                                grDetails.put(Constants.vendorid, vendorId);
                                result = accGoodsReceiptobj.getGoodsReceiptCountForImport(grDetails);
                                nocount = result.getRecordTotalCount();
                                List grList = result.getEntityList();
                                if (nocount > 0) {
                                    invoice = (GoodsReceipt) grList.get(0);
                                    /*
                                     * 1) If Link document and payment currency is same then exchangeRate=1
                                     * 2) If Link document and payment currency is different then exchangeRate is take from user
                                     * 3) If Link Document is GL and addvance then it exchangeRate=1
                                     */
                                    boolean isOpeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                                    if (isOpeningBalanceInvoice) {
                                    if (currency != null && invoice.getCurrency().getCurrencyCode().equalsIgnoreCase(currency.getCurrencyCode())) {
                                        exchangeRate = 1;
                                            amountDue = invoice.getOpeningBalanceAmountDue();
                                        } else {
                                            amountDue = invoice.getOpeningBalanceBaseAmountDue();
                                        }
                                    } else if (currency != null && invoice.getCurrency().getCurrencyCode().equalsIgnoreCase(currency.getCurrencyCode())) {
                                        exchangeRate = 1;
                                        amountDue = invoice.getInvoiceamountdue();
                                    } else {
                                        amountDue = invoice.getInvoiceAmountDueInBase();                                    
                                    }
                                    currencysymboltransaction = invoice.getCurrency().getSymbol();
                                    documentNoDate = invoice.getCreationDate();
                                    String invoiceCur = invoice.getCurrency().getCurrencyID();
                                    if (!currencyID.equals(paymentAccountCur)) {
                                        if (!currencyID.equals(invoiceCur)) {
                                            throw new AccountingException(messageSource.getMessage("acc.import.makereceivepayment.differentCurrencyErrorMessage", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))));
                                        }
                                    }
                                } else {
                                    isAlreadyExist = true;
                                    String vendorName = vendor != null ? vendor.getName() : "";
                                    throw new AccountingException("Invoice number '" + documenyNoStr + "' is not Available for "+ vendorName +" Vendor or invoice is not present in system.");
                                }
                                    
                            }
                        }else if(isCustomer && documenyTypeStr.equals(Constants.INVOICEIMPORT)){
                            isAlreadyExist = true;
                            throw new AccountingException("You cannot Make Payment against Customer Invoice");
                        }
                        
                        if (documenyTypeStr.equals(Constants.CREDITNOTEIMPORT) && !isGL) {             //For Credit Note
                            if (isCustomer) {
                                result = accCreditNoteDAOObj.getCustomerCreditNoCount(documenyNoStr, companyID, customerID);
                            } else if (isVendor) {
                                result = accCreditNoteDAOObj.getVendorCreditNoCount(documenyNoStr, companyID, vendorId);
                            }
                            nocount = result.getRecordTotalCount();
                            List list11 = result.getEntityList();
                            if (nocount > 0) {
                                creditNote = (CreditNote) list11.get(0);
                                if (creditNote.isIsOpeningBalenceCN()) {
                                if (currency != null && creditNote.getCurrency().getCurrencyCode().equalsIgnoreCase(currency.getCurrencyCode())) {
                                    exchangeRate = 1;
                                        amountDue = creditNote.getOpeningBalanceAmountDue();
                                    } else {
                                        amountDue = creditNote.getOpeningBalanceBaseAmountDue();
                                }
                                } else if (currency != null && creditNote.getCurrency().getCurrencyCode().equalsIgnoreCase(currency.getCurrencyCode())) {
                                    exchangeRate = 1;
                                    amountDue = creditNote.getCnamountdue();
                                } else {
                                    amountDue = creditNote.getCnamountdue() * exchangeRate;
                                }
                                currencysymboltransaction = creditNote.getCurrency().getSymbol();
                                documentNoDate = creditNote.getCreationDate();
                                String creditNoteCur = creditNote.getCurrency().getCurrencyID();
                                if (!currencyID.equals(paymentAccountCur)) {
                                    if (!currencyID.equals(creditNoteCur)) {
                                        throw new AccountingException(messageSource.getMessage("acc.import.makereceivepayment.differentCurrencyErrorMessage", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))));
                                    }
                                }
                            } else {
                                isAlreadyExist = true;
                                throw new AccountingException("Credit Note number '" + documenyNoStr + "' is not Available. ");
                            }
                            
                        }      
                        
                        if ((documenyTypeStr.equals(Constants.CREDITNOTEIMPORT) || documenyTypeStr.equals(Constants.INVOICEIMPORT)) && enterAmount > amountDue) {
                            throw new AccountingException("Enter Amount Should not be greater than amount due for document no '"+documenyNoStr+"'.");
                        } else if (documenyTypeStr.equalsIgnoreCase("Refund/Deposit") && !StringUtil.isNullOrEmpty(documenyNoStr) && enterAmount > amountDue) {
                            throw new AccountingException("Enter Amount Should not be greater than amount due for document no '"+documenyNoStr+"'.");
                        }
                        
                        if(documentNoDate!=null && billDate.before(documentNoDate)){
                            throw new AccountingException("Payment creation date cannot be less than selected document(s) creation date.");
                        }
                       
                        if (isGL && !debitTypeStr.equalsIgnoreCase("debit")) {
                            totalAmountPay = totalAmountPay-enterAmount;
                        } else {
                            totalAmountPay += enterAmount;
                        }
                        
                        if (documenyTypeStr.equals(Constants.GLIMPORT)) {
                            totalAmountPay = totalAmountPay + (debitTypeStr.equalsIgnoreCase("debit") || StringUtil.isNullOrEmpty(debitTypeStr)? taxAmount : -taxAmount);
                            exchangeRate = 1;
                        }
                        detailData.put("type", documenyTypeStr);
                        if (!StringUtil.equal(documenyTypeStr, Constants.GLIMPORT) || isGL) {
                            glCheckFlag = true;
                        }
                        if ((isGL || documenyTypeStr.equals(Constants.GLIMPORT)) && (debitTypeStr.equalsIgnoreCase("debit") || StringUtil.isNullOrEmpty(debitTypeStr))) {
                            detailData.put("debit", true);
                        } else {
                            detailData.put("debit", false);
                        }
                        detailData.put("currencysymbol",currency!=null?currency.getSymbol():"");
                        detailData.put("currencyidtransaction", currencyID);
                        detailData.put("currencynametransaction", "");
                        detailData.put("currencysymboltransaction", currencysymboltransaction);
                        detailData.put("currencyname", "");
                        detailData.put("currencyid", currencyID);
                        detailData.put("payment", "");
                        detailData.put("documentno", documenyNoStr);
                        switch (documenyTypeStr) {
                            case Constants.GLIMPORT:
                                detailData.put("documentid", account!=null?account.getID():"");         //General Ledger Code
                                break;
                            case Constants.CREDITNOTEIMPORT:
                                detailData.put("documentid", creditNote!=null?creditNote.getID():"");      //Credit Note
                                break;
                            case Constants.INVOICEIMPORT:
                                detailData.put("documentid", invoice!=null?invoice.getID():"");         //Invoice
                                break;
                            case Constants.ADVANCE_REFUND:                                                  //refund
                                if (!isVendor) {
                                    detailData.put("documentid", receiptId);
                                } else if (isVendor) {                                 //advance                                   
                                    if (previousEntryNo.equals(entryNumber) || StringUtil.isNullOrEmpty(previousEntryNo)) {
                                        isAdvance++;
                                        isRecordFailed = isAdvance > 1 || isRecordFailed ? true : false;
                                    }
                                    previousEntryNo = entryNumber;
                                    exchangeRate = 1;
                                    detailData.put("documentid", "");
                                }
                                break;

                        }

                        if (documenyTypeStr.equals(Constants.INVOICEIMPORT) || documenyTypeStr.equals(Constants.CREDITNOTEIMPORT)) {       //Invoice / Credit Note
                            detailData.put("prtaxid", "");
                            detailData.put("appliedGst", "");
                            detailData.put("taxamount", "");
                            detailData.put("amountDueOriginal", originalAmountDue);
                            detailData.put("amountDueOriginalSaved", originalAmountDue);
                            detailData.put("amountdue", amountDue);
                            detailData.put("exchangeratefortransaction", exchangeRate);
                            detailData.put("transactionAmount", transactionAmount);
                        } else {
                            if (documenyTypeStr.equals(Constants.GLIMPORT)) {                  //GL
                                if (rowtax != null) {
                                    detailData.put("prtaxid", rowtax.getID());
                                    detailData.put("appliedGst", rowtax.getID());
                                    detailData.put("taxamount", taxAmount);
                                } else {
                                    detailData.put("prtaxid", "");
                                    detailData.put("appliedGst", "");
                                    detailData.put("taxamount", "");
                                }

                            } else {
                                detailData.put("prtaxid", "");
                                detailData.put("appliedGst", "");
                                detailData.put("taxamount", "");
                            }
                            detailData.put("amountDueOriginal", "");
                            detailData.put("amountDueOriginalSaved", "");
                            detailData.put("amountdue", "");
                            detailData.put("exchangeratefortransaction", exchangeRate);
                            detailData.put("transactionAmount", "");
                        }
                        
                        detailData.put("enteramount", enterAmount);
                        detailData.put("description", description);
                        detailData.put("isClaimedInvoice", false);
                        detailData.put("gstCurrencyRate", "0.0");
                        detailData.put("srNoForRow", countRec);
                        detailData.put("masterTypeValue", "0");
                        detailData.put("isOneToManyTypeOfTaxAccount", false);
                        detailData.put("claimedDate", "");
                        detailData.put("repaymentscheduleid", "");
                        detailData.put("rowdetailid", "");
                        detailData.put("isOpeningBalanceTransaction", "");
                        detailData.put("modified", true);
                        detailData.put(Constants.customfield, lineCustomJArr.toString());                                                
                        rows.put(detailData);                                                   //inserting line level items in rows json                               
                        countRec++;
                        failureMsgsList.add("");
                    } catch (Exception ex) {
                        isRecordFailed = true;
                        String errorMsg = ex.getMessage();
                        if (ex.getMessage() != null) {
                            errorMsg = ex.getMessage();
                        } else if (ex.getCause() != null) {
                            errorMsg = ex.getCause().getMessage();
                        }
                        failureMsgFromSave += " " + errorMsg;
                        failureMsgsList.add( " " + errorMsg);
                    }
                    total++;
                }
                cnt++;
            }
            recList.add(recarr);            //adding last record
                                    
            StringBuilder errorMsgForGLandAdvance = new StringBuilder();        //To store error message of GL and Advance for last record 
            if (!isAlreadyExist && !isRecordFailed) {                           //to confirm
                try {
                    if (glCheckFlag) {
                        paramJobj.put("Details", rows.toString());
                        if (paymentMethod != null && paymentMethod.getDetailType() == 2) {
                            paramJobj.put("paydetail", paydetailObj.toString());
                        }
                        paramJobj.put("amount", totalAmountPay);
                        HashMap resutlSaveMap = saveVendorPayment(paramJobj);                   //to save last record
                        if (!resutlSaveMap.isEmpty()) {
                            if (resutlSaveMap.containsKey("jobj")) {
                                JSONObject jobj = (JSONObject) resutlSaveMap.get("jobj");
                                if (!jobj.optBoolean("success")) {
                                    throw new AccountingException(StringUtil.serverHTMLStripper(jobj.getString("msg")));
                                }
                            }
                        }
                        totalAmountPay = 0;
                    } else {
                        throw new AccountingException(messageSource.getMessage("acc.mp.noAdvInvNoteEntry", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))));
                    }
                } catch (Exception ex) {
                    if (ex.getMessage() != null) {
                        failureMsgFromSave += " " + ex.getMessage();
                        errorMsgForGLandAdvance.append(" ");
                        errorMsgForGLandAdvance.append(ex.getMessage());
                        isRecordFailed = true;
                    }
                }                
            }
            if (isAdvance > 1) {                    //is to check whether same receipt contains more than one advance line level item for Last record
                failureMsgFromSave += " Make Payment of Advance type is already entered. You can not enter twice.";
                errorMsgForGLandAdvance.append(" Make Payment of Advance type is already entered. You can not enter twice.");
                isAdvance = 0;
            }
            if (!StringUtil.isNullOrEmpty(failureMsgFromSave)) {
                int msgCnt=0;
                failed += recList.size();
                for (String[] rec : recList) {
                    failedRecords.append("\n").append(acSalesOrderServiceObj.createCSVrecord(rec)).append("\"").append((failureMsgsList.get(msgCnt).replaceAll("\"", "")+errorMsgForGLandAdvance.toString()).trim()).append("\"");
                    msgCnt++;
                }
                failureMsgsList=new ArrayList<>();
                errorMsgForGLandAdvance = new StringBuilder();
            }
            
            if (failed > 0) {
                importHandler.createFailureFiles(fileName, failedRecords, ".csv");
            }
            
            int success = total - failed;
            if (total == 0) {
                msg = "Empty file.";
            } else if (success == 0) {
                msg = "Failed to import all the records.";
            } else if (success == total) {
                msg = "All records are imported successfully.";
            } else {
                msg = "Imported " + success + " record" + (success > 1 ? "s" : "") + " successfully";
                msg += (failed == 0 ? "." : " and failed to import " + failed + " record" + (failed > 1 ? "s" : "") + ".");
            }
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(AccVendorPaymentModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        }finally {
            fileInputStream.close();
            csvReader.close();
            // For saving import log
            saveImportLog(requestJobj, msg, total, failed, Constants.Acc_Make_Payment_ModuleId);
            try {
                returnObj.put(Constants.RES_success, issuccess);
                returnObj.put(Constants.RES_msg, msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", ImportLog.getActualFileName(fileName));
                returnObj.put("Module", Constants.Acc_Make_Payment_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(AccVendorPaymentModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }                        
        return returnObj;
    }
   @Transactional(propagation = Propagation.REQUIRED)
    public void saveImportLog(JSONObject requestJobj, String msg, int total, int failed, int moduleID) {
        try {
            HashMap<String, Object> logDataMap = new HashMap<>();
            logDataMap.put("FileName", ImportLog.getActualFileName(requestJobj.getString("filename")));
            logDataMap.put("StorageName", requestJobj.getString("filename"));
            logDataMap.put("Log", msg);
            logDataMap.put("Type", "csv");
            logDataMap.put("FailureFileType", failed > 0 ? "csv" : "");
            logDataMap.put("TotalRecs", total);
            logDataMap.put("Rejected", failed);
            logDataMap.put("Module", moduleID);
            logDataMap.put("ImportDate", new Date());
            logDataMap.put("User", requestJobj.getString(Constants.useridKey));
            logDataMap.put("Company", requestJobj.getString(Constants.companyKey));
            importDao.saveImportLog(logDataMap);
        } catch (JSONException | ServiceException | DataInvalidateException | TransactionException ex) {
            Logger.getLogger(AccVendorPaymentModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public HashMap getCurrencyMap(boolean isCurrencyCode) throws ServiceException {
        HashMap currencyMap = new HashMap();
        KwlReturnObject returnObject = accCurrencyDAOobj.getCurrencies(currencyMap);
        List currencyList = returnObject.getEntityList();

        if (currencyList != null && !currencyList.isEmpty()) {
            Iterator iterator = currencyList.iterator();
            while (iterator.hasNext()) {
                KWLCurrency currency = (KWLCurrency) iterator.next();
                if (isCurrencyCode) {
                    currencyMap.put(currency.getCurrencyCode(), currency.getCurrencyID());
                } else {
                    currencyMap.put(currency.getName(), currency.getCurrencyID());
                }
            }
        }
        return currencyMap;
    }
    
     /**
     * @param : paramJobj
     * @Desc : Method to post rounding JE when PI linked in Payment 
     * @throws : ServiceException
     * @Return : void
     */
    @Override
    public void postRoundingJEAfterLinkingInvoiceInPayment(JSONObject paramJobj) throws ServiceException {
        try {
            String paymentid = paramJobj.optString("paymentid", "");
            KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid);
            Payment payment = (Payment) paymentResult.getEntityList().get(0);
            String paymentNumber = payment!=null?payment.getPaymentNumber():"";
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
            
            JSONArray linkJSONArray = paramJobj.has("linkdetails") ? new JSONArray(paramJobj.getString("linkdetails")) : new JSONArray();
            for (int invCount = 0; invCount < linkJSONArray.length(); invCount++) {
                JSONObject obj = linkJSONArray.getJSONObject(invCount);
                int documenttype = Integer.parseInt(obj.optString("documentType"));
                if (documenttype == Constants.PaymentAgainstInvoice && obj.optDouble("linkamount", 0.0) != 0) {
                    String invoiceId = obj.optString("documentid", "");
                    KwlReturnObject grresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceId);
                    GoodsReceipt gr = (GoodsReceipt) grresult.getEntityList().get(0);
                    if ((gr.isIsOpeningBalenceInvoice() && gr.getOpeningBalanceAmountDue() == 0) || (gr.isNormalInvoice() && gr.getInvoiceamountdue() == 0)) {
                        //below method return Rounding JE if created, otherwise it returns null
                        paramJobj.put("goodsReceiptObj", gr);
                        JournalEntry roundingJE = accGoodsReceiptModuleService.createRoundingOffJE(paramJobj);
                        if (roundingJE != null) {
                             auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has linked Vendor Invoice " + gr.getGoodsReceiptNumber() + " with  Payment " + paymentNumber + ". Rounding JE "+roundingJE.getEntryNumber()+" posted.", auditRequestParams, roundingJE.getID());
                        }
                    }
                }
            }
        } catch (JSONException | NumberFormatException | ServiceException | SessionExpiredException | AccountingException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    /**
     * @param : paramJobj 
     * @Desc : Method to post rounding JE when Payment created with Invoice
     * @throws : JSONException, ServiceException, SessionExpiredException, AccountingException
     * @Return : void
     */
    public void postRoundingJEOnPaymentSave(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, AccountingException {
        GoodsReceipt gr = (GoodsReceipt) paramJobj.get("goodsReceiptObj");
        String grNumber = gr.getGoodsReceiptNumber();
        String  paymentnumber =  paramJobj.getString("paymentnumber");
        boolean isEditPayment = paramJobj.optBoolean("isEdit", false);
        String companyid = paramJobj.getString(Constants.companyKey);

        Map<String, Object> auditRequestParams = new HashMap<>();
        auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
        auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
        auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
        
        if ((gr.isIsOpeningBalenceInvoice() && gr.getOpeningBalanceAmountDue() == 0) || (gr.isNormalInvoice() && gr.getInvoiceamountdue() == 0)) {
            //below method return Rounding JE if created otherwise it returns null
            JournalEntry roundingJE = accGoodsReceiptModuleService.createRoundingOffJE(paramJobj);
            if (roundingJE != null) {
                String jeid = roundingJE.getID();
                String jenumber = roundingJE.getEntryNumber();
                if(isEditPayment){
                    auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_UPDATED, "User " + paramJobj.optString(Constants.userfullname) + " has knocked off Vendor Invoice " + grNumber + " against Payment " + paymentnumber + ". Rounding JE "+jenumber+" updated.", auditRequestParams, jeid); 
                } else{
                    auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has knocked off Vendor Invoice " + grNumber + " against Payment " + paymentnumber + ". Rounding JE "+jenumber+" posted.", auditRequestParams, jeid);
                }
            }
        } else if (isEditPayment) {//If amount due becomes non zero in edit case then we need to check wheather rounding JE was generated for this GR or not if yes then need to delete
            KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(gr.getID(), companyid);
            List<JournalEntry> jeList = jeResult.getEntityList();
            for (JournalEntry roundingJE : jeList) {
                String jeid = roundingJE.getID();
                String jenumber = roundingJE.getEntryNumber();
                deleteJEArray(roundingJE.getID(),companyid);
                auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_DELETED, "User " + paramJobj.optString(Constants.userfullname) + " has edited  Payment " + paymentnumber + ". Rounding JE "+jenumber+" deleted.", auditRequestParams, jeid);
            }
        }
    }
    
    /**
     * @param : paramJobj @Desc : Method to post rounding JE when Payment approved by User
     * @throws : JSONException, ServiceException, SessionExpiredException,AccountingException
     * @Return : void
     */
    @Override 
    public void postRoundingJEAfterPaymentApprove(JSONObject paramJobj) throws ServiceException {
        try {
            String paymentid = paramJobj.optString("billid", "");
            KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid);
            Payment payment = (Payment) paymentResult.getEntityList().get(0);
            
            //Here used method evictObj to remove current payment object from session.
            //It was giving different value from database
            
            accountingHandlerDAOobj.evictObj(payment);
            paymentResult = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid);
            payment = (Payment) paymentResult.getEntityList().get(0);
            
            if (payment != null && payment.getApprovestatuslevel() == 11) {//Code will execute for approved payment only
                String paymentNumber = payment != null ? payment.getPaymentNumber() : "";
                Map<String, Object> auditRequestParams = new HashMap<>();
                auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));

                for (PaymentDetail detail : payment.getRows()) {
                    GoodsReceipt gr = detail.getGoodsReceipt();
                    if (gr != null && ((gr.isIsOpeningBalenceInvoice() && gr.getOpeningBalanceAmountDue() == 0) || (gr.isNormalInvoice() && gr.getInvoiceamountdue() == 0))) {
                        paramJobj.put("goodsReceiptObj", gr);
                        JournalEntry roundingJE = accGoodsReceiptModuleService.createRoundingOffJE(paramJobj);
                        if (roundingJE != null) {
                            auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has approved Payment " + paymentNumber + ". Rounding JE " + roundingJE.getEntryNumber() + " posted.", auditRequestParams, roundingJE.getID());
                        }
                    }
                }
              
            }
        } catch (JSONException | NumberFormatException | ServiceException | SessionExpiredException | AccountingException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {ServiceException.class})
    public void postRoundingJEOnRevertDishonouredPayment(JSONObject paramJobj, Set<String> amountDueUpdatedInvoiceIDSet) throws ServiceException {
        try {
            String paymentNumber = paramJobj.optString("paymentNumber");
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));

            for (String invoiceID : amountDueUpdatedInvoiceIDSet) {
                KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceID);
                GoodsReceipt invoice = (GoodsReceipt) paymentResult.getEntityList().get(0);

                if (invoice != null && ((invoice.isIsOpeningBalenceInvoice() && invoice.getOpeningBalanceAmountDue() == 0) || (invoice.isNormalInvoice() && invoice.getInvoiceamountdue() == 0))) {
                    paramJobj.put("goodsReceiptObj", invoice);
                    JournalEntry roundingJE = accGoodsReceiptModuleService.createRoundingOffJE(paramJobj);
                    if (roundingJE != null) {
                        auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has approved Payment " + paymentNumber + ". Rounding JE " + roundingJE.getEntryNumber() + " posted.", auditRequestParams, roundingJE.getID());
                    }
                }
            }
        } catch (ServiceException | JSONException | SessionExpiredException | AccountingException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    public JSONObject getChequeDetialsJSON(JSONObject paramJobj, Payment payment, HashMap<String, Object> hashMap, JSONObject extraParams) throws JSONException, ServiceException {
        JSONObject jobjDetails = new JSONObject();
        DecimalFormat df = new DecimalFormat("#,###,###,##0.00");
        String basecurrency = paramJobj.optString("currencyid");
        KwlReturnObject resultCurrency = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
        KWLCurrency currency = (KWLCurrency) resultCurrency.getEntityList().get(0);
        String amt = df.format(payment.getDepositAmount());

        //========================================================================================
        int countryLanguageId = extraParams.optInt("countryLanguageId");
        String accountaddress = extraParams.optString("accountaddress");
        String accountName = extraParams.optString("accountName");
        String payee = extraParams.optString("payee");
        String billingAddress = extraParams.optString("billingAddress");
        boolean isEdit = extraParams.optBoolean("isEdit");
            //========================================================================================

        String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(payment.getDepositAmount())), currency, countryLanguageId);
        hashMap.put("decimalFormat", df);
        hashMap.put("payment", payment);
        hashMap.put("paymentId", payment.getID());
        hashMap.put("accountaddress", accountaddress);
        hashMap.put("accountName", accountName);
        String[] tempArray = new String[]{"amountinword", netinword};
        hashMap.put("netinword", tempArray);
        String[] tempArray1 = new String[]{"accountName", payee};
        hashMap.put("payee", tempArray1);
        /*
         Getting transaction level Memo value
         */
        hashMap.put(Constants.memo, StringUtil.isNullOrEmpty(payment.getMemo()) ? "" : payment.getMemo());
        hashMap.put("address", billingAddress);

        boolean isChequePrint = false;
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("isChequePrint"))) {
            isChequePrint = Boolean.parseBoolean(paramJobj.optString("isChequePrint"));
        }
        if (isChequePrint) {
            KwlReturnObject resultPayMethod = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) resultPayMethod.getEntityList().get(0);

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            if (!StringUtil.isNullOrEmpty(payMethod.getID())) {
                requestParams.put("bankid", payMethod.getID());
            }
            boolean isnewlayout = false;
            ChequeLayout chequeLayout = null;
            DateFormat DATE_FORMAT = new SimpleDateFormat(Constants.DEFAULT_FORMAT_CHECK);
            String prefixbeforamt = "";
            String dateformat = "";
            HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
            KwlReturnObject result1 = accPaymentDAOobj.getChequeLayout(requestParams);
            List listCheque = result1.getEntityList();
            Iterator itr = listCheque.iterator();
            JSONObject chequeobj = new JSONObject();
            while (itr.hasNext()) {
                chequeLayout = (ChequeLayout) itr.next();
                chequeobj = new JSONObject(chequeLayout.getCoordinateinfo());
                jobjDetails.put("dateLeft", chequeobj.optString("dateLeft", "0"));
                jobjDetails.put("nameLeft", chequeobj.optString("nameLeft", "0"));
                jobjDetails.put("amtinwordLeft", chequeobj.optString("amtinwordLeft", "0"));
                jobjDetails.put("amtinwordLeftLine2", chequeobj.optString("amtinwordLeftLine2", "0"));
                jobjDetails.put("amtLeft", chequeobj.optString("amtLeft", "0"));
                jobjDetails.put("dateTop", chequeobj.optString("dateTop", "0"));
                jobjDetails.put("nameTop", chequeobj.optString("nameTop", "0"));
                jobjDetails.put("amtinwordTop", chequeobj.optString("amtinwordTop", "0"));
                jobjDetails.put("amtinwordTopLine2", chequeobj.optString("amtinwordTopLine2", "0"));
                jobjDetails.put("amtTop", chequeobj.optString("amtTop", "0"));

                /*
                 Get US related additional cheque co-ordinates 
                 */
                if (chequeLayout.isActivateExtraFields()) {
                    if (!chequeobj.optString("memoLeft").equals("-1")) {
                        jobjDetails.put("memoLeft", chequeobj.optString("memoLeft", "0"));
                    }
                    if (!chequeobj.optString("memoTop").equals("-1")) {
                        jobjDetails.put("memoTop", chequeobj.optString("memoTop", "0"));
                    }
                    if (!chequeobj.optString("addressLine1Left").equals("-1")) {
                        jobjDetails.put("addressLine1Left", chequeobj.optString("addressLine1Left", "0"));
                    }
                    if (!chequeobj.optString("addressLine1Top").equals("-1")) {
                        jobjDetails.put("addressLine1Top", chequeobj.optString("addressLine1Top", "0"));
                    }
                    if (!chequeobj.optString("addressLine2Left").equals("-1")) {
                        jobjDetails.put("addressLine2Left", chequeobj.optString("addressLine2Left", "0"));
                    }
                    if (!chequeobj.optString("addressLine2Top").equals("-1")) {
                        jobjDetails.put("addressLine2Top", chequeobj.optString("addressLine2Top", "0"));
                    }
                    if (!chequeobj.optString("addressLine3Left").equals("-1")) {
                        jobjDetails.put("addressLine3Left", chequeobj.optString("addressLine3Left", "0"));
                    }
                    if (!chequeobj.optString("addressLine3Top").equals("-1")) {
                        jobjDetails.put("addressLine3Top", chequeobj.optString("addressLine3Top", "0"));
                    }
                    if (!chequeobj.optString("addressLine4Left").equals("-1")) {
                        jobjDetails.put("addressLine4Left", chequeobj.optString("addressLine4Left", "0"));
                    }
                    if (!chequeobj.optString("addressLine4Top").equals("-1")) {
                        jobjDetails.put("addressLine4Top", chequeobj.optString("addressLine4Top", "0"));
                    }
                }

                if (!StringUtil.isNullObject(chequeLayout) && !StringUtil.isNullObject(chequeLayout.getDateFormat()) && !StringUtil.isNullObject(chequeLayout.getDateFormat().getJavaForm())) {
                    dateformat = chequeLayout.getDateFormat().getJavaForm();
                } else {
                    KwlReturnObject kWLDateFormatResult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), Constants.DEFAULT_FORMATID_CHECK);
                    KWLDateFormat kWLDateFormat = (KWLDateFormat) kWLDateFormatResult.getEntityList().get(0);
                    dateformat = kWLDateFormat.getJavaForm();
                }

                /*
                 If 'AddCharacterInCheckDate' is true then don't remove '/' or '-' from check Date
                 */
                if (!chequeLayout.isAddCharacterInCheckDate()) {
                    dateformat = dateformat.replaceAll("/", "");
                    dateformat = dateformat.replaceAll("-", "");
                }
                DATE_FORMAT = new SimpleDateFormat(dateformat);
                prefixbeforamt = chequeLayout.getAppendcharacter();
                isnewlayout = chequeLayout.isIsnewlayout();
            }

            //ERROR PRONE CODE
            boolean isFontStylePresent = chequeobj.has("fontStyle") && !StringUtil.isNullOrEmpty(chequeobj.getString("fontStyle")) ? true : false;
            String fontStyle = chequeobj.has("fontStyle") && !StringUtil.isNullOrEmpty(chequeobj.getString("fontStyle")) ? chequeobj.getString("fontStyle") : "";
            requestParams1.put("fontStyle", fontStyle);
            char fontStyleChar;
            if (fontStyle.equals("1")) {
                fontStyleChar = 'b';
            } else if (fontStyle.equals("2")) {
                fontStyleChar = 'i';
            } else {
                fontStyleChar = 'p';
            }
            String date = DATE_FORMAT.format(payment.getPayDetail().getCheque().getDueDate());
            String formatted_date_with_spaces = "";
            if (dateformat.equalsIgnoreCase(Constants.MMMMddyyyy) || (chequeLayout != null && chequeLayout.isAddCharacterInCheckDate())) { //if Date format ='MMMM dd,yyyy'(January 23,2017) that's why no need to add space.
                formatted_date_with_spaces = date;
            } else {
                for (int i = 0; i < date.length(); i++) {
                    formatted_date_with_spaces += date.charAt(i);
                    formatted_date_with_spaces += isnewlayout ? "&nbsp;&nbsp;&nbsp;" : "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

                }
            }
            String[] amount1 = hashMap.get("netinword") != null ? (String[]) hashMap.get("netinword") : null;
            String[] accName = hashMap.get("payee") != null ? (String[]) hashMap.get("payee") : null;
            jobjDetails.put("amount", prefixbeforamt + amt);
            String amount_first_line = "";
            String amount_second_line = "";
            String payAction = " Only.";
            String[] memo = {"memo", ""};
            String address = hashMap.get("address") != null ? (String) hashMap.get("address") : "";
            String memoInRequest = hashMap.get("memo") != null ? (String) hashMap.get("memo") : "";
            String[] addressArr = {"address", address};
            String address_Line1 = "", address_Line2 = "", address_Line3 = "", address_Line4 = "";
            if (chequeLayout != null && chequeLayout.isActivateExtraFields()) {
                memo = new String[]{"memo", memoInRequest};
                if (StringUtil.isNullOrEmpty(address)) {
                    jobjDetails.put("addressLine1", "");
                    jobjDetails.put("addressLine2", "");
                    jobjDetails.put("addressLine3", "");
                    jobjDetails.put("addressLine4", "");
                }
            }
            if (amount1[1].length() > 34 && amount1[1].charAt(34) == ' ') {
                amount_first_line = amount1[1].substring(0, 34);
                amount_second_line = amount1[1].substring(34, amount1[1].length());
                jobjDetails.put(amount1[0], amount_first_line);
                jobjDetails.put("amountinword1", amount_second_line + payAction);
            } else if (amount1[1].length() > 34) {
                amount_first_line = amount1[1].substring(0, 34);
                amount_first_line = amount1[1].substring(0, amount_first_line.lastIndexOf(" "));
                amount_second_line = amount1[1].substring(amount_first_line.length(), amount1[1].length());
                jobjDetails.put(amount1[0], amount_first_line);
                jobjDetails.put("amountinword1", amount_second_line + payAction);
            } else {
                if (amount1[1].length() < 27) {
                    jobjDetails.put(amount1[0], amount1[1] + payAction);
                    jobjDetails.put("amountinword1", "");
                } else {
                    jobjDetails.put(amount1[0], amount1[1]);
                    jobjDetails.put("amountinword1", payAction);
                }
            }

            if (!StringUtil.isNullOrEmpty(address) && chequeLayout != null && chequeLayout.isActivateExtraFields()) {
                int len = address.length();
                if (len <= 50) {
                    requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine1Txt", "'"));
                    requestParams1.put("fontString", addressArr[1]);
                    if (jobjDetails.has("addressLine1Left") && jobjDetails.has("addressLine1Top")) {
                        jobjDetails.put("addressLine1", authHandler.applyFontStyleForCheque(requestParams1));
                    } else {
                        jobjDetails.put("addressLine1", "");
                    }
                    jobjDetails.put("addressLine2", "");
                    jobjDetails.put("addressLine3", "");
                    jobjDetails.put("addressLine4", "");
                } else if (len > 50 && len <= 100) {
                    address_Line1 = addressArr[1].substring(0, 50);
                    if (addressArr[1].charAt(50) == ' ' || addressArr[1].charAt(50) == '\n') {
                        address_Line1 = addressArr[1].substring(0, 50);
                    } else if (address_Line1.lastIndexOf(" ") > 0) {
                        address_Line1 = addressArr[1].substring(0, address_Line1.lastIndexOf(" "));
                    } else {
                        address_Line1 = addressArr[1].substring(0, address_Line1.lastIndexOf("\n"));
                    }
                    address_Line2 = addressArr[1].substring(address_Line1.length(), addressArr[1].length());
                    requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine1Txt", "'"));
                    requestParams1.put("fontString", address_Line1);
                    if (jobjDetails.has("addressLine1Left") && jobjDetails.has("addressLine1Top")) {
                        jobjDetails.put("addressLine1", authHandler.applyFontStyleForCheque(requestParams1));
                    } else {
                        jobjDetails.put("addressLine1", "");
                    }
                    requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine2Txt", "'"));
                    requestParams1.put("fontString", address_Line2);
                    if (jobjDetails.has("addressLine2Left") && jobjDetails.has("addressLine2Top")) {
                        jobjDetails.put("addressLine2", authHandler.applyFontStyleForCheque(requestParams1));
                    } else {
                        jobjDetails.put("addressLine2", "");
                    }

                    jobjDetails.put("addressLine3", "");
                    jobjDetails.put("addressLine4", "");

                } else if (len > 100 && len <= 150) {
                    address_Line1 = addressArr[1].substring(0, 50);
                    if (addressArr[1].charAt(50) == ' ' || addressArr[1].charAt(50) == '\n') {
                        address_Line1 = addressArr[1].substring(0, 50);
                    } else if (address_Line1.lastIndexOf(" ") > 0) {
                        address_Line1 = addressArr[1].substring(0, address_Line1.lastIndexOf(" "));
                    } else {
                        address_Line1 = addressArr[1].substring(0, address_Line1.lastIndexOf("\n"));
                    }
                    address_Line2 = addressArr[1].substring(address_Line1.length(), 100);

                    if (addressArr[1].charAt(100) == ' ' || addressArr[1].charAt(100) == '\n') {
                        address_Line2 = addressArr[1].substring(address_Line1.length(), 100);
                    } else if (address_Line1.lastIndexOf(" ") > 0) {
                        address_Line2 = addressArr[1].substring(address_Line1.length(), (address_Line1 + address_Line2).lastIndexOf(" "));
                    } else {
                        address_Line2 = addressArr[1].substring(address_Line1.length(), (address_Line1 + address_Line2).lastIndexOf("\n"));
                    }
                    address_Line3 = addressArr[1].substring((address_Line2.length() + address_Line2.length()), addressArr[1].length());

                    requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine1Txt", "'"));
                    requestParams1.put("fontString", address_Line1);
                    if (jobjDetails.has("addressLine1Left") && jobjDetails.has("addressLine1Top")) {
                        jobjDetails.put("addressLine1", authHandler.applyFontStyleForCheque(requestParams1));
                    } else {
                        jobjDetails.put("addressLine1", "");
                    }
                    requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine2Txt", "'"));
                    requestParams1.put("fontString", address_Line2);
                    if (jobjDetails.has("addressLine2Left") && jobjDetails.has("addressLine2Top")) {
                        jobjDetails.put("addressLine2", authHandler.applyFontStyleForCheque(requestParams1));
                    } else {
                        jobjDetails.put("addressLine2", "");
                    }
                    requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine3Txt", "'"));
                    requestParams1.put("fontString", address_Line3);

                    if (jobjDetails.has("addressLine3Left") && jobjDetails.has("addressLine3Top")) {
                        jobjDetails.put("addressLine3", authHandler.applyFontStyleForCheque(requestParams1));
                    } else {
                        jobjDetails.put("addressLine3", "");
                    }

                    jobjDetails.put("addressLine4", "");

                } else if (len > 150) {

                    address_Line1 = addressArr[1].substring(0, 50);
                    if (addressArr[1].charAt(50) == ' ' || addressArr[1].charAt(50) == '\n') {
                        address_Line1 = addressArr[1].substring(0, 50);
                    } else if (address_Line1.lastIndexOf(" ") > 0) {
                        address_Line1 = addressArr[1].substring(0, address_Line1.lastIndexOf(" "));
                    } else {
                        address_Line1 = addressArr[1].substring(0, address_Line1.lastIndexOf("\n"));
                    }
                    address_Line2 = addressArr[1].substring(address_Line1.length(), 100);

                    if (addressArr[1].charAt(100) == ' ' || addressArr[1].charAt(100) == '\n') {
                        address_Line2 = addressArr[1].substring(address_Line1.length(), 100);
                    } else if ((address_Line1 + address_Line2).lastIndexOf(" ") > 0) {
                        address_Line2 = addressArr[1].substring(address_Line1.length(), (address_Line1 + address_Line2).lastIndexOf(" "));
                    } else {
                        address_Line2 = addressArr[1].substring(address_Line1.length(), (address_Line1 + address_Line2).lastIndexOf("\n"));
                    }

                    address_Line3 = addressArr[1].substring((address_Line1.length() + address_Line2.length()), 150);

                    if (addressArr[1].charAt(150) == ' ' || addressArr[1].charAt(150) == '\n') {
                        address_Line3 = addressArr[1].substring((address_Line1.length() + address_Line2.length()), 150);
                    } else if ((address_Line1 + address_Line2 + address_Line3).lastIndexOf(" ") > 0) {
                        address_Line3 = addressArr[1].substring((address_Line1.length() + address_Line2.length()), (address_Line1 + address_Line2 + address_Line3).lastIndexOf(" "));
                    } else {
                        address_Line3 = addressArr[1].substring((address_Line1.length() + address_Line2.length()), (address_Line1 + address_Line2 + address_Line3).lastIndexOf("\n"));
                    }
                    address_Line4 = addressArr[1].substring((address_Line1.length() + address_Line2.length() + address_Line3.length()), addressArr[1].length());

                    requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine1Txt", "'"));
                    requestParams1.put("fontString", address_Line1);
                    if (jobjDetails.has("addressLine1Left") && jobjDetails.has("addressLine1Top")) {
                        jobjDetails.put("addressLine1", authHandler.applyFontStyleForCheque(requestParams1));
                    } else {
                        jobjDetails.put("addressLine1", "");
                    }
                    requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine2Txt", "'"));
                    requestParams1.put("fontString", address_Line2);
                    if (jobjDetails.has("addressLine2Left") && jobjDetails.has("addressLine2Top")) {
                        jobjDetails.put("addressLine2", authHandler.applyFontStyleForCheque(requestParams1));
                    } else {
                        jobjDetails.put("addressLine2", "");
                    }
                    requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine3Txt", "'"));
                    requestParams1.put("fontString", address_Line3);
                    if (jobjDetails.has("addressLine3Left") && jobjDetails.has("addressLine3Top")) {
                        jobjDetails.put("addressLine3", authHandler.applyFontStyleForCheque(requestParams1));
                    } else {
                        jobjDetails.put("addressLine3", "");
                    }
                    requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForAddressLine4Txt", "'"));
                    requestParams1.put("fontString", address_Line4);
                    if (jobjDetails.has("addressLine4Left") && jobjDetails.has("addressLine4Top")) {
                        jobjDetails.put("addressLine4", authHandler.applyFontStyleForCheque(requestParams1));
                    } else {
                        jobjDetails.put("addressLine4", "");
                    }
                }

            }

            /*
             For memo formating and put in json
             */
            if (chequeLayout != null && chequeLayout.isActivateExtraFields()) {
                requestParams1.put("fontSize", chequeobj.optString("selectFontSizeForMemoTxt", "'"));
                requestParams1.put("fontString", memo[1]);
                if (jobjDetails.has("memoLeft") && jobjDetails.has("memoTop")) {
                    jobjDetails.put(memo[0], authHandler.applyFontStyleForCheque(requestParams1));
                } else {
                    jobjDetails.put(memo[0], "");
                }
            }
            jobjDetails.put(accName[0], accName[1]);
            jobjDetails.put("date", formatted_date_with_spaces);
            jobjDetails.put("isnewlayout", isnewlayout);
            jobjDetails.put("activateExtraFields", (chequeLayout != null ? chequeLayout.isActivateExtraFields() : false));

            //for name
            if (chequeobj.has("dateFontSize") || isFontStylePresent) {
                if (isFontStylePresent && !StringUtil.isNullOrEmpty(chequeobj.getString("dateFontSize"))) {
                    formatted_date_with_spaces = "<font size=" + chequeobj.getString("dateFontSize") + "><" + fontStyleChar + ">" + formatted_date_with_spaces + "</" + fontStyleChar + "></font> ";
                    jobjDetails.put("date", formatted_date_with_spaces);
                } else if (!StringUtil.isNullOrEmpty(chequeobj.getString("dateFontSize"))) {
                    formatted_date_with_spaces = "<font size=" + chequeobj.getString("dateFontSize") + ">" + formatted_date_with_spaces + "</font> ";
                    jobjDetails.put("date", formatted_date_with_spaces);
                } else {
                    formatted_date_with_spaces = "<" + fontStyleChar + ">" + formatted_date_with_spaces + "</" + fontStyleChar + ">";
                    jobjDetails.put("date", formatted_date_with_spaces);

                }
            }
            //for name
            if (chequeobj.has("nameFontSize") || isFontStylePresent) {
                if (isFontStylePresent && !StringUtil.isNullOrEmpty(chequeobj.getString("nameFontSize"))) {
                    accName[1] = "<font size=" + chequeobj.getString("nameFontSize") + "><" + fontStyleChar + ">" + accName[1] + "</" + fontStyleChar + "></font> ";
                    jobjDetails.put(accName[0], accName[1]);
                } else if (!StringUtil.isNullOrEmpty(chequeobj.getString("nameFontSize"))) {
                    accName[1] = "<font size=" + chequeobj.getString("nameFontSize") + ">" + accName[1] + "</font> ";
                    jobjDetails.put(accName[0], accName[1]);
                } else {
                    accName[1] = "<" + fontStyleChar + ">" + accName[1] + "</" + fontStyleChar + ">";
                    jobjDetails.put(accName[0], accName[1]);

                }
            }

            //for amount in words
            if (chequeobj.has("amountInWordsFontSize") || isFontStylePresent) {
                if (isFontStylePresent && !StringUtil.isNullOrEmpty(chequeobj.getString("amountInWordsFontSize"))) {
                    amount_first_line = "<font size=" + chequeobj.getString("amountInWordsFontSize") + "><" + fontStyleChar + ">" + amount_first_line + "</" + fontStyleChar + "></font> ";
                    amount_second_line = "<font size=" + chequeobj.getString("amountInWordsFontSize") + "><" + fontStyleChar + ">" + amount_second_line + " " + payAction + "</" + fontStyleChar + "></font> ";
                    if (amount1[1].length() > 34) {
                        jobjDetails.put(amount1[0], amount_first_line);
                        jobjDetails.put("amountinword1", amount_second_line);
                    } else if (amount1[1].length() < 27) {
                        amount1[1] = "<font size=" + chequeobj.getString("amountInWordsFontSize") + "><" + fontStyleChar + ">" + amount1[1] + " " + payAction + "</" + fontStyleChar + "></font> ";
                        jobjDetails.put(amount1[0], amount1[1]);
                        jobjDetails.put("amountinword1", "");
                    }
                } else if (!StringUtil.isNullOrEmpty(chequeobj.getString("amountInWordsFontSize"))) {
                    amount_first_line = "<font size=" + chequeobj.getString("amountInWordsFontSize") + ">" + amount_first_line + "</font> ";
                    amount_second_line = "<font size=" + chequeobj.getString("amountInWordsFontSize") + ">" + amount_second_line + " " + payAction + "</font> ";
                    if (amount1[1].length() > 34) {
                        jobjDetails.put(amount1[0], amount_first_line);
                        jobjDetails.put("amountinword1", amount_second_line);
                    } else if (amount1[1].length() < 27) {
                        amount1[1] = "<font size=" + chequeobj.getString("amountInWordsFontSize") + ">" + amount1[1] + " " + payAction + "</font> ";
                        jobjDetails.put(amount1[0], amount1[1]);
                        jobjDetails.put("amountinword1", "");
                    }
                } else {
                    amount_first_line = "<" + fontStyleChar + ">" + amount_first_line + "</" + fontStyleChar + ">";
                    amount_second_line = "<" + fontStyleChar + ">" + amount_second_line + " " + payAction + "</" + fontStyleChar + ">";
                    if (amount1[1].length() > 34) {
                        jobjDetails.put(amount1[0], amount_first_line);
                        jobjDetails.put("amountinword1", amount_second_line);
                    } else if (amount1[1].length() < 27) {
                        amount1[1] = "<" + fontStyleChar + ">" + amount1[1] + " " + payAction + "</" + fontStyleChar + ">";
                        jobjDetails.put(amount1[0], amount1[1]);
                        jobjDetails.put("amountinword1", "");
                    }
                }
            }

            //for amount in number
            if (chequeobj.has("amountFontSize") || isFontStylePresent) {
                if (isFontStylePresent && !StringUtil.isNullOrEmpty(chequeobj.getString("amountFontSize"))) {
                    amt = "<font size=" + chequeobj.getString("amountFontSize") + "><" + fontStyleChar + ">" + prefixbeforamt + amt + "</" + fontStyleChar + "></font> ";
                    jobjDetails.put("amount", amt);
                } else if (!StringUtil.isNullOrEmpty(chequeobj.getString("amountFontSize"))) {
                    amt = "<font size=" + chequeobj.getString("amountFontSize") + ">" + prefixbeforamt + amt + "</font> ";
                    jobjDetails.put("amount", amt);
                } else {
                    amt = "<" + fontStyleChar + ">" + prefixbeforamt + amt + "</" + fontStyleChar + ">";
                    jobjDetails.put("amount", amt);
                }
            }
        }
        return jobjDetails;
    }
    
     @Override 
    public List<JSONObject> getOpeningBalanceReceiptJsonForReport(JSONObject paramJobj, List list, List<JSONObject> jsonObjectlist) {
        List<JSONObject> returnList = new ArrayList<JSONObject>();
        try {
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJobj.optString(Constants.globalCurrencyKey));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat onlyDateDF = authHandler.getOnlyDateFormat();
            String fileType = StringUtil.isNullOrEmpty(paramJobj.optString("filetype", null)) ? "" : paramJobj.optString("filetype");
            String companyid = paramJobj.optString(Constants.companyKey);
            DateFormat userdf = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj);
            boolean isExport = false;
            if (paramJobj.optString("isExport", null) != null) {
                isExport = Boolean.parseBoolean(paramJobj.optString("isExport"));
            }

            HashMap<String, Object> fieldrequestParamsGlobalLevel = new HashMap();
            HashMap<String, String> customFieldMapGlobalLevel = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMapGlobalLevel = new HashMap<String, String>();
            fieldrequestParamsGlobalLevel.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParamsGlobalLevel.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Make_Payment_ModuleId));
            HashMap<String, String> replaceFieldMapGlobalLevel = new HashMap<String, String>();
            HashMap<String, Integer> FieldMapGlobalLevel = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParamsGlobalLevel, replaceFieldMapGlobalLevel, customFieldMapGlobalLevel, customDateFieldMapGlobalLevel);

            Iterator itr = list.iterator();
            while (itr.hasNext()) {

                Payment payment = (Payment) itr.next();

                JSONObject obj = new JSONObject();
                obj.put("withoutinventory", false);
                obj.put("isOpeningBalanceTransaction", payment.isIsOpeningBalencePayment());
                obj.put("isNormalTransaction", payment.isNormalPayment());
                Vendor vendor = payment.getVendor();
                if (vendor != null) {
                    obj.put("address", vendor.getAddress());
                    obj.put("personemail", vendor.getEmail());
                } else {
                    obj.put("address", "");
                    obj.put("personemail", "");
                }
                obj.put("billid", payment.getID());
                obj.put("companyid", payment.getCompany().getCompanyID());
                obj.put("companyname", payment.getCompany().getCompanyName());
                obj.put("entryno", "");
                obj.put("journalentryid", "");
                obj.put("personid", vendor.getID());
                obj.put("billno", payment.getPaymentNumber());
                obj.put("isadvancepayment", payment.isIsadvancepayment());
                obj.put("isadvancefromvendor", false);
                obj.put("ismanydbcr", false);
                obj.put("bankCharges", 0.0);
                obj.put("bankChargesCmb", "");
                obj.put("bankInterest", 0.0);
                obj.put("bankInterestCmb", "");
                obj.put("paidToCmb", "");

                obj.put("advanceUsed", false);
                obj.put("advanceid", "");
                obj.put("advanceamount", 0.0);
                obj.put("billdate", authHandler.getDateOnlyFormat().format(payment.getCreationDate()));

                obj.put("amount", authHandler.formattedAmount(payment.getDepositAmount(), companyid));
                obj.put("amountinbase", authHandler.formattedAmount(payment.getOriginalOpeningBalanceBaseAmount(), companyid));
                obj.put("personname", vendor == null ? "" : vendor.getName());
                obj.put("memo", "");
                obj.put("deleted", payment.isDeleted());
                obj.put("currencysymbol", (payment.getCurrency() == null ? currency.getSymbol() : payment.getCurrency().getSymbol()));
                obj.put("externalcurrencyrate", payment.getExternalCurrencyRate());
                obj.put("currencyid", (payment.getCurrency() == null ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID()));
                obj.put("methodid", "");
                obj.put("detailtype", "");
                obj.put("paymentmethod", "");
                String jeNumber = "";
                String jeIds = "";
                String jeIdEntryDate = "";
                if (payment.getLinkDetailPayments() != null && !payment.getLinkDetailPayments().isEmpty()) {
                    Set<LinkDetailPayment> linkedDetailPayList = payment.getLinkDetailPayments();
                    for (LinkDetailPayment ldprow : linkedDetailPayList) {
                        if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                            jeIds += ldprow.getLinkedGainLossJE() + ",";
                            KwlReturnObject linkedje = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), ldprow.getLinkedGainLossJE());
                            JournalEntry linkedJEObject = (JournalEntry) linkedje.getEntityList().get(0);
                            jeNumber += linkedJEObject.getEntryNumber() + ",";
                            jeIdEntryDate += onlyDateDF.format(linkedJEObject.getEntryDate()) + ",";
                        }
                    }
                }
                if (!StringUtil.isNullOrEmpty(jeIds)) {
                    jeIds = jeIds.substring(0, jeIds.length() - 1);
                }
                if (!StringUtil.isNullOrEmpty(jeNumber)) {
                    jeNumber = jeNumber.substring(0, jeNumber.length() - 1);
                }
                if (!StringUtil.isNullOrEmpty(jeIdEntryDate)) {
                    jeIdEntryDate = jeIdEntryDate.substring(0, jeIdEntryDate.length() - 1);
                }
                obj.put("entryno", jeNumber);
                obj.put("journalentryid", jeIds);
                obj.put("journalentrydate", jeIdEntryDate);
                double amount = 0, linkedAmountDue = payment.getDepositAmount();
                if ((payment.getLinkDetailPaymentToCreditNote() != null && !payment.getLinkDetailPayments().isEmpty()) || (payment.getLinkDetailPaymentToCreditNote() != null && !payment.getLinkDetailPaymentToCreditNote().isEmpty())) {
                    linkedAmountDue = getPaymentAmountDue(payment);
                    obj.put("isLinked", true);
                    obj.put("otherwise", false);
                } else {
                    obj.put("otherwise", true);
                }
                obj.put("chequenumber", payment.getPayDetail() == null ? "" : (payment.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (payment.getPayDetail().getCheque() != null ? payment.getPayDetail().getCheque().getChequeNo() : "") : "");
                obj.put("paymentamountdue", authHandler.formattedAmount(linkedAmountDue, companyid));
                obj.put("paymentamountdueinbase", authHandler.formattedAmount(payment.getOpeningBalanceBaseAmountDue(), companyid));
                if (StringUtil.equal(fileType, "print") || StringUtil.equal(fileType, "pdf") || StringUtil.equal(fileType, "csv") || StringUtil.equal(fileType, "xls") || StringUtil.equal(fileType, "detailedXls")) {//method is called only for print case. no need to execute for other cases.
                    String usedDocumentNumbers = getDocumentNumbersUsedInPayment(payment);
                    obj.put("useddocumentnumber", usedDocumentNumbers);
                }

                // Add Custom field Values.
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                Detailfilter_names.add("companyid");
                Detailfilter_params.add(payment.getCompany().getCompanyID());
                Detailfilter_names.add("OpeningBalanceMakePaymentId");
                Detailfilter_params.add(payment.getID());
                Detailfilter_names.add("moduleId");
                Detailfilter_params.add(Constants.Acc_Make_Payment_ModuleId + "");
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                KwlReturnObject idcustresult = accJournalEntryobj.getOpeningBalancePaymentCustomData(invDetailRequestParams);
                if (idcustresult.getEntityList().size() > 0) {
                    OpeningBalanceMakePaymentCustomData balancePaymentCustomData = (OpeningBalanceMakePaymentCustomData) idcustresult.getEntityList().get(0);
                    AccountingManager.setCustomColumnValues(balancePaymentCustomData, FieldMapGlobalLevel, replaceFieldMapGlobalLevel, variableMap);
                    JSONObject params = new JSONObject();
                    params.put("companyid", companyid);
                    params.put("isExport", isExport);
                    params.put("userdf", userdf);
                    if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.browsertz, null))) {
                        params.put(Constants.browsertz, paramJobj.optString(Constants.browsertz));
                    }
                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMapGlobalLevel, customDateFieldMapGlobalLevel, obj, params);
                }
                returnList.add(obj);
            }
        } catch (JSONException ex) {
            returnList = null;
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            returnList = null;
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            returnList = null;
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        if (returnList != null) {
            jsonObjectlist.addAll(returnList);
        }
        return jsonObjectlist;
    }

    public String getDocumentNumbersUsedInPayment(Payment payment) {
        String usedDocumentNumbers = "";

        //Vendor Invoice number
        if (!payment.getRows().isEmpty()) {
            Set<PaymentDetail> rows = payment.getRows();
            for (PaymentDetail detail : rows) {
                if (detail.getGoodsReceipt() != null) {
                    usedDocumentNumbers += detail.getGoodsReceipt().getGoodsReceiptNumber() + ", ";
                }
            }
        }

        //Credit Note number
        if (!payment.getCreditNotePaymentDetails().isEmpty()) {
            Set<CreditNotePaymentDetails> creditNotePaymentDetails = payment.getCreditNotePaymentDetails();
            for (CreditNotePaymentDetails details : creditNotePaymentDetails) {
                if (details.getCreditnote() != null) {
                    usedDocumentNumbers += details.getCreditnote().getCreditNoteNumber() + ", ";
                }
            }
        }

        //Account code or number
        if (!payment.getPaymentDetailOtherwises().isEmpty()) {
            Set<PaymentDetailOtherwise> paymentDetailOtherwises = payment.getPaymentDetailOtherwises();
            for (PaymentDetailOtherwise detailOthrwise : paymentDetailOtherwises) {
                if (detailOthrwise.getAccount() != null) {
                    String acccode = detailOthrwise.getAccount().getAcccode() == null ? detailOthrwise.getAccount().getAccountName() : detailOthrwise.getAccount().getAcccode();
                    usedDocumentNumbers += acccode + ", ";
                }
            }
        }

        //Refund Receipt Number
        if (!StringUtil.isNullOrEmpty(payment.getCustomer()) && !payment.getAdvanceDetails().isEmpty()) {
            Set<AdvanceDetail> advanceDetails = payment.getAdvanceDetails();
            for (AdvanceDetail advanceDetail : advanceDetails) {
                if (advanceDetail.getReceiptAdvanceDetails() != null && advanceDetail.getReceiptAdvanceDetails().getReceipt() != null) {
                    usedDocumentNumbers += advanceDetail.getReceiptAdvanceDetails().getReceipt().getReceiptNumber() + ", ";
                }
            }
        }

        //Linked Vendor Invoice Number
        if (!payment.getLinkDetailPayments().isEmpty()) {
            Set<LinkDetailPayment> linkDetailPayments = payment.getLinkDetailPayments();
            for (LinkDetailPayment detailPayment : linkDetailPayments) {
                if (detailPayment.getGoodsReceipt() != null) {
                    usedDocumentNumbers += detailPayment.getGoodsReceipt().getGoodsReceiptNumber() + ", ";
                }
            }
        }

        //Linked Credit Note Number
        if (!payment.getLinkDetailPaymentToCreditNote().isEmpty()) {
            Set<LinkDetailPaymentToCreditNote> linkDetailPaymentToCreditNote = payment.getLinkDetailPaymentToCreditNote();
            for (LinkDetailPaymentToCreditNote detail : linkDetailPaymentToCreditNote) {
                if (detail.getCreditnote() != null) {
                    usedDocumentNumbers += detail.getCreditnote().getCreditNoteNumber() + ", ";
                }
            }
        }

        usedDocumentNumbers = usedDocumentNumbers.trim();
        if (usedDocumentNumbers.endsWith(",")) {
            usedDocumentNumbers = usedDocumentNumbers.substring(0, usedDocumentNumbers.length() - 1);
        }

        return usedDocumentNumbers;
    }

    public double getPaymentAmountDue(Payment payment) {
        Iterator itrRow1 = payment.getLinkDetailPayments().iterator();
        Iterator itrRow2 = payment.getLinkDetailPaymentToCreditNote().iterator();
        double amount = 0, linkedAmountDue = payment.getDepositAmount();
        if (payment.getLinkDetailPayments() != null && !payment.getLinkDetailPayments().isEmpty()) {
            while (itrRow1.hasNext()) {
                amount += ((LinkDetailPayment) itrRow1.next()).getAmount();
            }
        }
        if (payment.getLinkDetailPaymentToCreditNote() != null && !payment.getLinkDetailPaymentToCreditNote().isEmpty()) {
            while (itrRow2.hasNext()) {
                amount += ((LinkDetailPaymentToCreditNote) itrRow2.next()).getAmount();
            }
        }
        linkedAmountDue = (payment.getDepositAmount() - payment.getBankInterestAmount() - payment.getBankChargesAmount()) - amount;
        return linkedAmountDue;
    }

    public List<JSONObject> getBillingPaymentsJson(HashMap<String, Object> requestParams, List list, List<JSONObject> jsonlist) throws ServiceException {
        try {
            HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            String companyid = (String) requestParams.get("companyid");
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            Iterator itr = list.iterator();
            //JSONArray jArr=new JSONArray();
            while (itr.hasNext()) {
                JSONArray jArr1 = new JSONArray();
                Object[] row = (Object[]) itr.next();
                BillingPayment receipt = (BillingPayment) row[0];
                Account acc = (Account) row[1];
                JSONObject obj = new JSONObject();
                obj.put("withoutinventory", true);
                obj.put("billid", receipt.getID());
                obj.put("companyid", receipt.getCompany().getCompanyID());
                obj.put("companyname", receipt.getCompany().getCompanyName());
                obj.put("entryno", receipt.getJournalEntry().getEntryNumber());
                obj.put("journalentryid", receipt.getJournalEntry().getID());
                obj.put("personid", acc.getID());
                obj.put("billno", receipt.getBillingPaymentNumber());
                obj.put("ismanydbcr", receipt.isIsmanydbcr());
                obj.put("bankCharges", receipt.getBankChargesAmount());
                obj.put("bankChargesCmb", receipt.getBankChargesAccount() != null ? receipt.getBankChargesAccount().getID() : "");
                obj.put("bankInterest", receipt.getBankInterestAmount());
                obj.put("bankInterestCmb", receipt.getBankInterestAccount() != null ? receipt.getBankInterestAccount().getID() : "");
                obj.put("paidToCmb", receipt.getPaidTo() == null ? "" : receipt.getPaidTo().getID());
                obj.put("currencysymbol", (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getSymbol()));
                obj.put("externalcurrencyrate", receipt.getExternalCurrencyRate());
                obj.put("billdate", df.format(receipt.getJournalEntry().getEntryDate()));//receiptdate
                Iterator itrRow = receipt.getRows().iterator();

                rRequestParams.clear();
                filter_names.clear();
                filter_params.clear();
                filter_names.add("billingPayment.ID");
                filter_params.add(receipt.getID());
                rRequestParams.put("filter_names", filter_names);
                rRequestParams.put("filter_params", filter_params);
                KwlReturnObject pdoresult = accVendorPaymentobj.getBillingPaymentDetailOtherwise(rRequestParams);
                List<BillingPaymentDetailOtherwise> list1 = pdoresult.getEntityList();
                Iterator pdoRow = list1.iterator();
                double amount = 0;
                if (!receipt.getRows().isEmpty()) {
                    while (itrRow.hasNext()) {
                        amount += ((BillingPaymentDetail) itrRow.next()).getAmount();
                    }
                    obj.put("otherwise", false);
                } else if (pdoRow != null && list1.size() > 0) {
                    for (BillingPaymentDetailOtherwise billingPaymentDetailOtherwise : list1) {
                        if (receipt.getID().equals(billingPaymentDetailOtherwise.getBillingPayment().getID())) {
                            JSONObject obj1 = new JSONObject();
                            obj1.put("debitaccid", billingPaymentDetailOtherwise.getAccount().getID());
                            obj1.put("debitamt", billingPaymentDetailOtherwise.getAmount());
                            obj1.put("isdebit", billingPaymentDetailOtherwise.isIsdebit());
                            obj1.put("desc", billingPaymentDetailOtherwise.getDescription() != null ? billingPaymentDetailOtherwise.getDescription() : "");
                            obj1.put("prtaxid", billingPaymentDetailOtherwise.getTax() != null ? billingPaymentDetailOtherwise.getTax().getID() : "");
                            obj1.put("curamount", (billingPaymentDetailOtherwise.getAmount() + billingPaymentDetailOtherwise.getTaxamount()));
                            if (receipt.isIsmanydbcr()) {
                                if (billingPaymentDetailOtherwise.isIsdebit()) {
                                    amount += (billingPaymentDetailOtherwise.getAmount() + billingPaymentDetailOtherwise.getTaxamount());
                                } else {
                                    amount -= (billingPaymentDetailOtherwise.getAmount() + billingPaymentDetailOtherwise.getTaxamount());
                                }
                            } else {
                                amount = amount + (billingPaymentDetailOtherwise.getAmount() + billingPaymentDetailOtherwise.getTaxamount());
                            }
                            jArr1.put(obj1);
                        }
                    }

                    obj.put("otherwise", true);
                    obj.put("detailsjarr", jArr1);
                } else {
                    itrRow = receipt.getJournalEntry().getDetails().iterator();
                    while (itrRow.hasNext()) {
                        JournalEntryDetail jed = ((JournalEntryDetail) itrRow.next());
                        if (!jed.isDebit()) {
                            if (receipt.getDeposittoJEDetail() != null) {
                                amount = receipt.getDeposittoJEDetail().getAmount();
                            } else {
                                amount = jed.getAmount();
                            }
                        } else {
                            JSONObject obj1 = new JSONObject();
                            obj1.put("debitaccid", jed.getAccount().getID());
                            obj1.put("debitamt", jed.getAmount());
                            obj1.put("isdebit", jed.isDebit());
                            obj1.put("curamount", jed.getAmount());
                            obj1.put("desc", jed.getDescription() != null ? jed.getDescription() : "");
                            jArr1.put(obj1);
                        }
                    }
                    obj.put("otherwise", true);
                    obj.put("detailsjarr", jArr1);
                }
                obj.put("receipttype", "");
                KwlReturnObject result = accVendorPaymentobj.getBillingPaymentVendorNames(companyid, receipt.getID());
                List vNameList = result.getEntityList();
                Iterator vNamesItr = vNameList.iterator();
                String vendorNames = "";
                while (vNamesItr.hasNext()) {
                    String tempName = URLEncoder.encode((String) vNamesItr.next(), "UTF-8");
                    vendorNames += tempName;
                    vendorNames += ",";
                }
                vendorNames = vendorNames.substring(0, Math.max(0, vendorNames.length() - 1));
                KwlReturnObject vendorresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), acc.getID());
                Vendor vendor = (Vendor) vendorresult.getEntityList().get(0);
                obj.put("personemail", vendor != null ? vendor.getEmail() : "");
                obj.put("personname", vendorNames);
                obj.put(Constants.PERSONCODE, vendor.getAcccode());
                obj.put("memo", receipt.getMemo());
                obj.put("deleted", receipt.isDeleted());
                obj.put("paymentmethod", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getMethodName()));
                obj.put("amount", authHandler.formattedAmount(amount, companyid));
                obj.put("currencysymbol", (receipt.getCurrency() == null ? currency.getSymbol() : receipt.getCurrency().getSymbol()));
                obj.put("currencyid", (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID()));
                obj.put("methodid", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getID()));
                obj.put("detailtype", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getDetailType()));
                if (receipt.getPayDetail() != null) {
                    try {
                        obj.put("expirydate", (receipt.getPayDetail().getCard() == null ? "" : df.format(receipt.getPayDetail().getCard().getExpiryDate())));
                    } catch (IllegalArgumentException ae) {
                        obj.put("expirydate", "");
                    }
                    obj.put("refdetail", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getDescription()) : receipt.getPayDetail().getCard().getCardType()));
                    obj.put("refno", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getChequeNo()) : receipt.getPayDetail().getCard().getRefNo()));
                    obj.put("refname", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getBankName()) : receipt.getPayDetail().getCard().getCardHolder()));
                    if (receipt.getPayDetail().getCard() != null) {
                        obj.put("refcardno", receipt.getPayDetail().getCard().getCardNo());
                        //obj.put("refexpdate", receipt.getPayDetail().getCard().getExpiryDate());
                    }
                }
                obj.put("clearanceDate", "");
                obj.put("paymentStatus", false);
                if (receipt.getPayDetail() != null) {
                    KwlReturnObject clearanceDate = accBankReconciliationObj.getBRfromJE(receipt.getJournalEntry().getID(), receipt.getCompany().getCompanyID(), false);
                    if (clearanceDate != null && clearanceDate.getEntityList() != null && clearanceDate.getEntityList().size() > 0) {
                        BankReconciliationDetail brd = (BankReconciliationDetail) clearanceDate.getEntityList().get(0);
                        if (brd.getBankReconciliation().getClearanceDate() != null) {
                            obj.put("clearanceDate", df.format(brd.getBankReconciliation().getClearanceDate()));
                            obj.put("paymentStatus", true);
                        }
                    }
                }
                jsonlist.add(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getBillingPaymentsJson : " + ex.getMessage(), ex);
        }
        return jsonlist;
    }
  public List<JSONObject> getPaymentsJson(HashMap<String, Object> requestParams, List list, List<JSONObject> jsonlist) throws ServiceException {
        try {
            HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            String companyid = (String) requestParams.get("companyid");
            String currencyid = (String) requestParams.get("gcurrencyid");
            String fileType= "";
            boolean isExport=false;
            if(requestParams.containsKey("isExport") && requestParams.get("isExport")!=null){
                isExport= (boolean) requestParams.get("isExport");
            }
            if(requestParams.containsKey("fileType") && !StringUtil.isNullOrEmpty((String)requestParams.get("fileType"))){
                fileType= requestParams.get("fileType").toString();
            }
            
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Make_Payment_ModuleId,1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            HashMap<String, Object> fieldrequestParamsGlobalLevel = new HashMap();
            HashMap<String, String> customFieldMapGlobalLevel = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMapGlobalLevel = new HashMap<String, String>();
            fieldrequestParamsGlobalLevel.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParamsGlobalLevel.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Make_Payment_ModuleId));
            HashMap<String, String> replaceFieldMapGlobalLevel = new HashMap<String, String>();
            HashMap<String, Integer> FieldMapGlobalLevel = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParamsGlobalLevel, replaceFieldMapGlobalLevel, customFieldMapGlobalLevel, customDateFieldMapGlobalLevel);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = (DateFormat) requestParams.get("df");
            
            //Company Object            
            KwlReturnObject companyList = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = companyList!=null?(Company) companyList.getEntityList().get(0):null;
            
            Boolean indiaCheck=(company!=null && company.getCountry()!=null)?(company.getCountry().getID().equals(""+Constants.indian_country_id)):false; // India Country Check 
            
            Iterator itr = list.iterator();            
            while (itr.hasNext()) {

                /*
                 * If you are modifying in this method then you will need to
                 * modify on accReportsController.java - getIBGEntryJson()
                 * method AND on AccReportsServiceImpl.java getPaymentAmount()
                 * method
                 */

                JSONArray jArr1 = new JSONArray();
                Object[] row = (Object[]) itr.next();
                Payment payment = (Payment) row[0];
                Account acc = (Account) row[1];
                JSONObject obj = new JSONObject();
                obj.put("withoutinventory", false);
                Customer customer = null;
                if (payment.getCustomer() != null) {
                    KwlReturnObject custResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), payment.getCustomer());
                    customer = (Customer) custResult.getEntityList().get(0);
                }

                Vendor vendor = payment.getVendor();
                if (vendor != null) {
                    obj.put("address", vendor.getAddress());
                    obj.put("personemail", vendor.getEmail());
                } else {
                    obj.put("address", "");
                    obj.put("personemail", "");
                }
                obj.put("ischequeprinted", payment.isChequeprinted());
                obj.put("billid", payment.getID());
                obj.put("companyid", payment.getCompany().getCompanyID());
                obj.put("companyname", payment.getCompany().getCompanyName());
                String jeNumber = payment.getJournalEntry().getEntryNumber();
                if (payment.getJournalEntryForBankCharges() != null) {
                    jeNumber += "," + payment.getJournalEntryForBankCharges().getEntryNumber();                    
                }
                if (payment.getJournalEntryForBankInterest() != null) {
                    jeNumber += "," + payment.getJournalEntryForBankInterest().getEntryNumber();                    
                }
                
                if(payment.getLinkDetailPayments() != null && !payment.getLinkDetailPayments().isEmpty()) {
                    Set<LinkDetailPayment> linkedDetailPayList = payment.getLinkDetailPayments();
                    for(LinkDetailPayment ldprow : linkedDetailPayList) {
                        if(!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {                            
                            KwlReturnObject linkedje = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), ldprow.getLinkedGainLossJE());
                            JournalEntry linkedJEObject = (JournalEntry) linkedje.getEntityList().get(0);
                            jeNumber += "," + linkedJEObject.getEntryNumber();                            
                        }
                    }
                }
                if (payment.getApprovestatuslevel() == 11) {
                    obj.put("approvalstatusinfo", "Approved");
                }
                
                if (isExport) {
                    obj.put("isLinked", "No");
                } else {
                    obj.put("isLinked", false);
                }
                
                if (payment.getAdvanceDetails() != null && !payment.getAdvanceDetails().isEmpty()) { // Payment Details - Against Invoice
                    if ((payment.getLinkDetailPayments() != null && !payment.getLinkDetailPayments().isEmpty()) || (payment.getLinkDetailPaymentToCreditNote() != null && !payment.getLinkDetailPaymentToCreditNote().isEmpty()) || (payment.getLinkDetailPaymentsToAdvancePayment() != null && !payment.getLinkDetailPaymentsToAdvancePayment().isEmpty())) {
                        obj.put("isLinked", "Yes");
                    } else {
                        obj.put("isLinked", "No");
                    }
                }
                obj.put("entryno", jeNumber);
                obj.put("journalentryid", payment.getJournalEntry().getID());
                obj.put("isadvancepayment", payment.isIsadvancepayment());
                obj.put("ismanydbcr", payment.isIsmanydbcr());
                obj.put("isprinted", payment.isPrinted());
                obj.put("bankCharges", payment.getBankChargesAmount());
                obj.put("bankChargesCmb", payment.getBankChargesAccount() != null ? payment.getBankChargesAccount().getID() : "");
                obj.put("bankInterest", payment.getBankInterestAmount());
                obj.put("bankInterestCmb", payment.getBankInterestAccount() != null ? payment.getBankInterestAccount().getID() : "");
                obj.put("paidToCmb", payment.getPaidTo() == null ? "" : payment.getPaidTo().getID());
                obj.put("paidto", payment.getPaidTo() != null ? payment.getPaidTo().getValue() : "");  //to show the paid to option in grid
                obj.put("payee", !StringUtil.isNullOrEmpty(payment.getPayee()) ? payment.getPayee() : "");
                obj.put(Constants.SEQUENCEFORMATID, payment.getSeqformat() == null ? "" : payment.getSeqformat().getID());
                obj.put("paymentwindowtype", payment.getPaymentWindowType());
                boolean advanceUsed = false;
                if (payment.getAdvanceid() != null && !payment.getAdvanceid().isDeleted()) {
                    rRequestParams.clear();
                    filter_names.clear();
                    filter_params.clear();
                    filter_names.add("payment.ID");
                    filter_params.add(payment.getAdvanceid().getID());
                    rRequestParams.put("filter_names", filter_names);
                    rRequestParams.put("filter_params", filter_params);
                    KwlReturnObject grdresult = accVendorPaymentobj.getPaymentDetails(rRequestParams);
                    advanceUsed = grdresult.getEntityList().size() > 0 ? true : false;
                }
                Payment paymentObject=null;
                if(payment.getInvoiceAdvCndnType()==2||payment.getInvoiceAdvCndnType()==1){
                    paymentObject=accVendorPaymentobj.getPaymentObject(payment);
                    if(paymentObject!=null)
                        obj.put("cndnid",paymentObject.getID());
                }else if(payment.getInvoiceAdvCndnType()==3){
                    obj.put("cndnid",payment.getID());
                }
                obj.put("invoiceadvcndntype",payment.getInvoiceAdvCndnType());
                obj.put("cndnAndInvoiceId",!StringUtil.isNullOrEmpty(payment.getCndnAndInvoiceId())?payment.getCndnAndInvoiceId():"");

                obj.put("advanceUsed", advanceUsed);
                obj.put("advanceid", (payment.getAdvanceid() != null && !payment.getAdvanceid().isDeleted()) ? payment.getAdvanceid().getID() : "");
                obj.put("advanceamount", payment.getAdvanceamount());
                obj.put("receipttype", payment.getReceipttype());
                obj.put("personid", (vendor != null) ? vendor.getID() : acc.getID());
                obj.put(Constants.PERSONCODE,  StringUtil.DecodeText((vendor == null && customer == null) ? "" : ((vendor != null) ? (vendor.getAcccode() == null ? "" : vendor.getAcccode()) : (customer != null) ? (customer.getAcccode() == null ? "" : customer.getAcccode()) : "")));    //Used decoder to avoid '+' symbol at white/empty space between words.
//                obj.put("customervendorname", (vendor!=null)? vendor.getName() : (customer!=null)? customer.getName():"");
                obj.put("billno", payment.getPaymentNumber());
                obj.put("billdate", df.format(payment.getCreationDate()));//receiptdate

                rRequestParams.clear();
                filter_names.clear();
                filter_params.clear();
                filter_names.add("payment.ID");
                filter_params.add(payment.getID());
                rRequestParams.put("filter_names", filter_names);
                rRequestParams.put("filter_params", filter_params);
                KwlReturnObject pdoresult = accVendorPaymentobj.getPaymentDetailOtherwise(rRequestParams);
                List<PaymentDetailOtherwise> list1 = pdoresult.getEntityList();
                Iterator pdoRow = list1.iterator();

                Iterator itrRow = payment.getRows().iterator();
                double amount = 0, totaltaxamount = 0, linkedAmountDue = 0,amountDueInBase = 0,totaltdsamount = 0;  
                if (indiaCheck) {
                    totaltdsamount = getTotalTDSAmount(payment);
                    obj.put("totaltdsamount", authHandler.formattedAmount(totaltdsamount, companyid));
                }
                if(payment.getAdvanceDetails()!=null&&!payment.getAdvanceDetails().isEmpty()){
                    for(AdvanceDetail advanceDetail:payment.getAdvanceDetails()){
                       linkedAmountDue= advanceDetail.getAmountDue();
                       KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, linkedAmountDue, payment.getCurrency().getCurrencyID(), payment.getCreationDate(), payment.getJournalEntry().getExternalCurrencyRate());
                        amountDueInBase  = (Double) bAmt.getEntityList().get(0);
                       if(linkedAmountDue<=0){
                            obj.put("disableOtherwiseLinking", true);
                       }else{
                            obj.put("disableOtherwiseLinking", false);
                       }
                    }
                }
                
                if (!payment.getRows().isEmpty()) { // Payment Details - Against Invoice
                    amount = payment.getDepositAmount();
                    if (pdoRow != null && list1.size() > 0) {
                        for (PaymentDetailOtherwise paymentDetailOtherwise : list1) {
                            if (payment.getID().equals(paymentDetailOtherwise.getPayment().getID())) {
                                double taxamount = 0;
                                obj.put("isLinked", true);
                                if (paymentDetailOtherwise.getTax() != null) {
                                    taxamount = paymentDetailOtherwise.getTaxamount();
                                    totaltaxamount += taxamount;
                                }
                            }
                        }
                    }
                    obj.put("otherwise", false);
                    Payment mainPayment = payment.getAdvanceid();
                    if (payment.isIsadvancepayment()) {
                        obj.put("isLinked", true);//disabling edit for advance once linked
                    } else if (!payment.isIsadvancepayment() && mainPayment != null && mainPayment.getRows() != null && !mainPayment.getRows().isEmpty()) {
                        obj.put("isLinked", true);//disabling the edit functionality for advance with normal invoice and the advance payment is linked.
                        obj.put("linkedadvanceMsgFlag", true);
                    }

                } else if (pdoRow != null && list1.size() > 0) { // Payment Details Otherwise case
                    for (PaymentDetailOtherwise paymentDetailOtherwise : list1) {
                        if (payment.getID().equals(paymentDetailOtherwise.getPayment().getID())) {
                            double taxamount = 0;
                            if (paymentDetailOtherwise.getTax() != null) {
                                taxamount = paymentDetailOtherwise.getTaxamount();
                                if (paymentDetailOtherwise.isIsdebit()) {
                                    totaltaxamount += taxamount;
                                } else {
                                    totaltaxamount -= taxamount;
                                }
                            }
                            JSONObject obj1 = new JSONObject();
                            obj1.put("debitaccid", paymentDetailOtherwise.getAccount().getID());
                            obj1.put("debitaccname", paymentDetailOtherwise.getAccount().getName());
                            obj1.put("debitamt", paymentDetailOtherwise.getAmount());
                            obj1.put("isdebit", paymentDetailOtherwise.isIsdebit());
                            obj1.put("desc", paymentDetailOtherwise.getDescription() != null ? paymentDetailOtherwise.getDescription() : "");
                            obj1.put("prtaxid", paymentDetailOtherwise.getTax() != null ? paymentDetailOtherwise.getTax().getID() : "");
                            obj1.put("taxamount", taxamount);
                            obj1.put("curamount", (paymentDetailOtherwise.getAmount() + paymentDetailOtherwise.getTaxamount()));
                            amount = payment.getDepositAmount(); // ERP-10495,ERP-11038 (Amount not included for document type="Advance/Deposit")
                            // ## Get Custom Field Data 
                            Map<String, Object> variableMap = new HashMap<String, Object>();
                            HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                            ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                            Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                            Detailfilter_params.add(paymentDetailOtherwise.getID());
                            invDetailRequestParams.put("filter_names", Detailfilter_names);
                            invDetailRequestParams.put("filter_params", Detailfilter_params);
                            KwlReturnObject idcustresult = accVendorPaymentobj.getVendorPaymentCustomData(invDetailRequestParams);
                            if (idcustresult.getEntityList().size() > 0) {
                                AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                     String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                                    if (customFieldMap.containsKey(varEntry.getKey())) {
                                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
                                        FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                        if (fieldComboData != null) {
                                            obj1.put(varEntry.getKey(), coldata != null ? coldata : "");//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                                        }
                                    } else if (customDateFieldMap.containsKey(varEntry.getKey()) && isExport) {
                                        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                                        DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                                        Date dateFromDB=null;
                                        try {
                                            dateFromDB = defaultDateFormat.parse(coldata);
                                            coldata = df2.format(dateFromDB);

                                        } catch (Exception e) {
                                        }                                        
                                            obj1.put(varEntry.getKey(), coldata);
                                    } else {
                                        if (!StringUtil.isNullOrEmpty(coldata)) {
                                            obj1.put(varEntry.getKey(), coldata);
                                        }
                                    }
                                }
                            }
                            jArr1.put(obj1);
                        }
                    }
                    obj.put("disableOtherwiseLinking", true);//diable linkining functionaly for paymentGL
                    obj.put("otherwise", true);
                    obj.put("detailsjarr", jArr1);
                } else {
                    itrRow = payment.getJournalEntry().getDetails().iterator();
                    while (itrRow.hasNext()) {
                        JournalEntryDetail jed = ((JournalEntryDetail) itrRow.next());
                        if (!jed.isDebit()) { // If payment not against Debit. If Payment made against Credit.
                            if (payment.getDeposittoJEDetail() != null) {
                                amount = payment.getDeposittoJEDetail().getAmount();
                            } else {
                                amount = jed.getAmount();
                            }
                        } else {
                            JSONObject obj1 = new JSONObject();
                            obj1.put("debitaccid", jed.getAccount().getID());
                            obj1.put("debitamt", jed.getAmount());
                            obj1.put("isdebit", jed.isDebit());
                            obj1.put("curamount", jed.getAmount());
                            obj1.put("desc", jed.getDescription() != null ? jed.getDescription() : "");
                            jArr1.put(obj1);
                        }
                    }
                    // In Payment made against CN/DN, amountdue must be zero 
                    if(payment.getInvoiceAdvCndnType()==3 || payment.getReceipttype()==7) { //Against CN/DN
                        linkedAmountDue = 0;
                        obj.put("disableOtherwiseLinking", true);
                    }
                    obj.put("otherwise", true);
                    obj.put("detailsjarr", jArr1);
                }
                totaltaxamount = authHandler.round(totaltaxamount, companyid);
                amount = authHandler.round(amount, companyid);
                // Below subtraction from amount is done for ERP-18826
                if(payment.getBankChargesAmount() > 0 && payment.getJournalEntryForBankCharges()==null){
                    amount-= payment.getBankChargesAmount();
                }
                if(payment.getBankInterestAmount() > 0 && payment.getJournalEntryForBankInterest()==null){
                    amount-= payment.getBankInterestAmount();
                }
                amount+=totaltdsamount;  // ERP-28527 :TDS consider as Direct tax so it include in total amount.
                obj.put("amount", authHandler.formattedAmount(amount, companyid));
                obj.put("paymentamountdue", authHandler.formattedAmount(linkedAmountDue, companyid));
                obj.put("paymentamountdueinbase", authHandler.formattedAmount(amountDueInBase, companyid));
                obj.put("totaltaxamount", authHandler.formattedAmount(totaltaxamount, companyid));
                
                obj.put("amountBeforeTax", authHandler.formattedAmount((amount-totaltaxamount), companyid));

                String paycurrencyid = (payment.getCurrency() == null ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID());
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, paycurrencyid, payment.getCreationDate(), payment.getJournalEntry().getExternalCurrencyRate());
                double amountinbase = (Double) bAmt.getEntityList().get(0);
                obj.put("amountinbase", authHandler.formattedAmount(amountinbase, companyid));


                KwlReturnObject result = accVendorPaymentobj.getPaymentVendorNames(companyid, payment.getID());
                List vNameList = result.getEntityList();
                Iterator vNamesItr = vNameList.iterator();
                String vendorNames = "";
                while (vNamesItr.hasNext()) {
                    String tempName = URLEncoder.encode((String) vNamesItr.next(), "UTF-8");
                    vendorNames += tempName;
                    vendorNames += ",";
                }
                vendorNames = vendorNames.substring(0, Math.max(0, vendorNames.length() - 1));
                vendorNames = StringUtil.DecodeText(vendorNames);        //Used decoder to avoid '+' symbol at white/empty space between words. 
                if(Constants.isNewPaymentStructure){
                    obj.put("personname", (vendor==null&&customer==null) ? vendorNames : ((vendor != null) ? vendor.getName() : (customer != null) ? customer.getName() : ""));
                } else {
                    obj.put("personname", (payment.getReceipttype() == 9 || payment.getReceipttype() == 2) ? vendorNames : ((vendor != null) ? vendor.getName() : (customer != null) ? customer.getName() : ""));
                }
                obj.put("memo", payment.getMemo());
                if (payment.isIsDishonouredCheque()) {
                    obj.put("dishonoured","Dishonoured");
                }
                obj.put("deleted", payment.isDeleted());
                obj.put("currencysymbol", (payment.getCurrency() == null ? currency.getSymbol() : payment.getCurrency().getSymbol()));
                obj.put("currencycode", (payment.getCurrency() == null ? currency.getCurrencyCode() : payment.getCurrency().getCurrencyCode()));
                obj.put("externalcurrencyrate", payment.getExternalCurrencyRate());
                obj.put("paymentmethod", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getMethodName()));
                obj.put("chequenumber", payment.getPayDetail() == null ? "" : (payment.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (payment.getPayDetail().getCheque() != null ? payment.getPayDetail().getCheque().getChequeNo() : "") : "");
                obj.put("bankname", payment.getPayDetail() == null ? "" : (payment.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (payment.getPayDetail().getCheque() != null ? payment.getPayDetail().getCheque().getBankName() : "") : "");
                obj.put("chequedescription", payment.getPayDetail() == null ? "" : (payment.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (payment.getPayDetail().getCheque() != null ? (payment.getPayDetail().getCheque().getDescription() != null ? payment.getPayDetail().getCheque().getDescription() : "") : "") : "");
                obj.put("currencyid", (payment.getCurrency() == null ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID()));
                obj.put("methodid", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getID()));
                obj.put("detailtype", (payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod().getDetailType()));
                if (payment.getPayDetail() != null) {
                    try {
                        obj.put("expirydate", (payment.getPayDetail().getCard() == null ? "" : df.format(payment.getPayDetail().getCard().getExpiryDate())));
                    } catch (IllegalArgumentException ae) {
                        obj.put("expirydate", "");
                    }
                    obj.put("refdetail", (payment.getPayDetail().getCard() == null ? (payment.getPayDetail().getCheque() == null ? "" : payment.getPayDetail().getCheque().getDescription()) : payment.getPayDetail().getCard().getCardType()));

                    obj.put("refno", (payment.getPayDetail().getCard() == null ? (payment.getPayDetail().getCheque() == null ? "" : payment.getPayDetail().getCheque().getChequeNo()) : payment.getPayDetail().getCard().getRefNo()));
                    obj.put("refname", (payment.getPayDetail().getCard() == null ? (payment.getPayDetail().getCheque() == null ? "" : payment.getPayDetail().getCheque().getBankName()) : payment.getPayDetail().getCard().getCardHolder()));
                    if (payment.getPayDetail().getCard() != null) {
                        obj.put("refcardno", payment.getPayDetail().getCard().getCardNo());
                    }
//                }
                }
                obj.put("clearanceDate", "");
                obj.put("paymentStatus", false);
                if (payment.getPayDetail() != null) {
                    KwlReturnObject clearanceDate = accBankReconciliationObj.getBRfromJE(payment.getJournalEntry().getID(), payment.getCompany().getCompanyID(), false);
                    if (clearanceDate != null && clearanceDate.getEntityList() != null && clearanceDate.getEntityList().size() > 0) {
                        BankReconciliationDetail brd = (BankReconciliationDetail) clearanceDate.getEntityList().get(0);
                        if (brd.getBankReconciliation().getClearanceDate() != null) {
                            obj.put("clearanceDate", df.format(brd.getBankReconciliation().getClearanceDate()));
                            obj.put("paymentStatus", true);
                        }
                    }
                }

                if (payment.isIBGTypeTransaction()) {
                    obj.put("isIBGTypeTransaction", payment.isIBGTypeTransaction());
                    obj.put("ibgDetailsID", payment.getIbgreceivingbankdetails() == null ? "" : payment.getIbgreceivingbankdetails().getId());
                    obj.put("ibgCode", payment.getIbgCode());
                }
                
                if (StringUtil.equal(fileType, "print")||StringUtil.equal(fileType, "pdf") || StringUtil.equal(fileType, "csv")|| StringUtil.equal(fileType, "xls")|| StringUtil.equal(fileType, "detailedXls")) {//method is called only for print case. no need to execute for other cases.
                    String usedDocumentNumbers = getDocumentNumbersUsedInPayment(payment);
                    obj.put("useddocumentnumber", usedDocumentNumbers);
                }
                 
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                Detailfilter_names.add("companyid");
                Detailfilter_params.add(payment.getCompany().getCompanyID());
                Detailfilter_names.add("journalentryId");
                Detailfilter_params.add(payment.getJournalEntry().getID());
                Detailfilter_names.add("moduleId");
                Detailfilter_params.add(Constants.Acc_Make_Payment_ModuleId + "");
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                KwlReturnObject idcustresult = accVendorPaymentobj.getVendorPaymentGlobalCustomData(invDetailRequestParams);
                if (idcustresult.getEntityList().size() > 0) {
                    AccJECustomData jeCustom = (AccJECustomData) idcustresult.getEntityList().get(0);
                    AccountingManager.setCustomColumnValues(jeCustom, FieldMapGlobalLevel, replaceFieldMapGlobalLevel, variableMap);
                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                        String coldata = varEntry.getValue().toString();
                        if (customFieldMapGlobalLevel.containsKey(varEntry.getKey())) {
                            String value = "";
                            String Ids[] = coldata.split(",");
                            for (int i = 0; i < Ids.length; i++) {
                                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
                                FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                            if (fieldComboData != null) {
                                    value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                            }
                            }
                            if (!StringUtil.isNullOrEmpty(value)) {
                                value = value.substring(0, value.length() - 1);
                            }
                            obj.put(varEntry.getKey(), value);
                        } else if (customDateFieldMapGlobalLevel.containsKey(varEntry.getKey()) && isExport) {
                            DateFormat sdf = requestParams.containsKey(Constants.userdf)?(DateFormat)requestParams.get(Constants.userdf):new SimpleDateFormat("yyyy-MM-dd");                            
                            DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                            Date dateFromDB=null;
                            try {
                                dateFromDB = defaultDateFormat.parse(coldata);
                                coldata = sdf.format(dateFromDB);

                            } catch (Exception e) {
                            }                            
                                obj.put(varEntry.getKey(), coldata);                            
                        } else {
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                String[] coldataArray = coldata.split(",");
                                String Coldata = "";
                                for (int countArray = 0; countArray < coldataArray.length; countArray++) {
                                    Coldata += "'" + coldataArray[countArray] + "',";
                                }
                                Coldata = Coldata.substring(0, Coldata.length() - 1);
                                String ColValue = accAccountDAOobj.getfieldcombodatabyids(Coldata);
                                if (isExport) {
                                    coldata = Jsoup.parse(coldata).text();
                                }
                                obj.put(varEntry.getKey(), coldata);
                                obj.put(varEntry.getKey() + "_Values", ColValue);
                            }
                        }
                    }
                }

                jsonlist.add(obj);
            }
            //jobj.put("data", JArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getPaymentsJson : " + ex.getMessage(), ex);
        }
        return jsonlist;
    }
    
   @Override 
    public double getTotalTDSAmount(Payment payment) throws ServiceException {
        double totaltdsamt = 0;
        if (payment != null) {
            for (PaymentDetail paymentdetail : payment.getRows()) {
                totaltdsamt += paymentdetail != null ? paymentdetail.getTdsamount() : 0;
            }
            for (AdvanceDetail advanceDetail : payment.getAdvanceDetails()) {
                totaltdsamt += advanceDetail != null ? advanceDetail.getTdsamount() : 0;
            }
            for (CreditNotePaymentDetails creditnotepaymentdetail : payment.getCreditNotePaymentDetails()) {
                totaltdsamt += creditnotepaymentdetail != null ? creditnotepaymentdetail.getTdsamount() : 0;
            }
            for (PaymentDetailOtherwise paymentdetailotherwise : payment.getPaymentDetailOtherwises()) {
                totaltdsamt += paymentdetailotherwise != null ? paymentdetailotherwise.getTdsamount() : 0;
            }
        }
        return totaltdsamt;
    } 
   
   @Override 
   @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, SessionExpiredException.class, JSONException.class, AccountingException.class})
   public JSONObject deletePaymentPermanentJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        try {
            boolean advanceAmountFlag = StringUtil.getBoolean(paramJobj.optString("advanceAmountFlag"));
            boolean invoiceAmountFlag = StringUtil.getBoolean(paramJobj.optString("invoiceAmountFlag"));
            boolean cndnAmountFlag = StringUtil.getBoolean(paramJobj.optString("cndnAmountFlag"));
            
            HashMap<String, Object> RefundEntryFound = IsAdvancePaymentUsedInRefundReceipt(paramJobj);
            if (RefundEntryFound.isEmpty()) {
                HashMap<Integer, String> paymentHashMap = new HashMap<Integer, String>();
                JSONArray jArr = new JSONArray();
                if (paramJobj.optString("datainvoiceadvcndn",null) != null) {
                    JSONArray jSONArray = new JSONArray(paramJobj.optString("datainvoiceadvcndn","[{}]"));
                    String paymentid = "", paymentno = "", paymentIds = "";

                    for (int i = 0; i < jSONArray.length(); i++) {
                        JSONObject jObject = jSONArray.getJSONObject(i);
                        paymentid = StringUtil.DecodeText(jObject.optString("paymentID"));
                        int invoiceadvcndntypejson = !StringUtil.isNullOrEmpty(jObject.getString("invoiceadvcndntype")) ? Integer.parseInt(jObject.getString("invoiceadvcndntype")) : 0;
                        paymentHashMap.put(invoiceadvcndntypejson, paymentid);
                    }
                }
                
                String unableToDeletePaymentNos = "";
                if (paramJobj.optString("datainvoiceadvcndn",null) != null) {
                    if (paymentHashMap.containsKey(2) && !paymentHashMap.containsKey(3) && !paymentHashMap.containsKey(1)) {
                        unableToDeletePaymentNos = deletePaymentPermanent(paramJobj);
                    } else {
                        if (advanceAmountFlag && paymentHashMap.containsKey(2)) {
                            deletePaymentPermanent(paramJobj, paymentHashMap.get(2));
                        }
                        if (invoiceAmountFlag && paymentHashMap.containsKey(1)) {
                            deletePaymentPermanent(paramJobj, paymentHashMap.get(1));
                        }
                        if (cndnAmountFlag && paymentHashMap.containsKey(3)) {
                            deletePaymentPermanent(paramJobj, paymentHashMap.get(3));
                        }
                    }
                } else {
                    unableToDeletePaymentNos = deletePaymentPermanent(paramJobj);
                }
                if(!StringUtil.isNullOrEmpty(unableToDeletePaymentNos)){
                    unableToDeletePaymentNos = unableToDeletePaymentNos.substring(0, unableToDeletePaymentNos.length()-2);
                }
                issuccess = true;
                 if (StringUtil.isNullOrEmpty(unableToDeletePaymentNos)) {
                    msg = (StringUtil.isNullOrEmpty(unableToDeletePaymentNos)) ? messageSource.getMessage("acc.pay.del", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) : messageSource.getMessage("acc.ob.PMExcept", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " " + unableToDeletePaymentNos + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                } else {
                    msg = messageSource.getMessage("acc.ob.PMExcept", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " " + unableToDeletePaymentNos.substring(0, unableToDeletePaymentNos.length()) + " " + messageSource.getMessage("acc.field.deletedsuccessfully", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " " + messageSource.getMessage("acc.field.belongstolockingperiod", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                }
            } else {
                for (Map.Entry<String, Object> entry : RefundEntryFound.entrySet()) {
                    issuccess = false;
                    msg = entry.getValue().toString();
                }
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    } 
   
    public HashMap IsAdvancePaymentUsedInRefundReceipt(JSONObject paramJobj) {
        HashMap result = new HashMap();
        try {
            JSONArray jArr = new JSONArray(paramJobj.optString("data","[{}]"));
            String paymentid = "";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                paymentid = StringUtil.DecodeText(jobj.optString("billid"));
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid);
                Payment payment = (Payment) objItr.getEntityList().get(0);
                Set<AdvanceDetail> advanceDetails=payment.getAdvanceDetails();
                for (AdvanceDetail advanceDetail : advanceDetails) {
                    List<Object[]> advPayDetails = accVendorPaymentobj.getAdvancePaymentUsedInRefundReceipt(advanceDetail.getId());
                    if(advPayDetails.size()>0) {
                        Object[] row = advPayDetails.get(0);
                        result.put("flag", true);
                        result.put("msg", "Advance Payment #"+payment.getPaymentNumber()+" used against Refund/Deposit Receipt #"+row[0]);
                        break;
                    } else {
                        advPayDetails = accVendorPaymentobj.getAdvancePaymentLinkedWithRefundReceipt(paymentid, payment.getCompany().getCompanyID());
                        if(advPayDetails.size()!=0){
                            Object[] row = advPayDetails.get(0);
                            result.put("flag", true);
                            result.put("msg", "Advance Payment #" + payment.getPaymentNumber() + " is linked with Refund/Deposit Receipt #" + row[1]);
                            break;
                        }
                    }
                }
            }
        } catch(Exception ex) {
            Logger.getLogger(accVendorPaymentControllerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
  
  @Override  
    public String deletePaymentPermanent(JSONObject paramJobj) throws AccountingException, SessionExpiredException, ServiceException, ParseException {
        String unableToDeletePaymentNos = "";
        try {
            Map<String, Object> requestMap = AccountingManager.getGlobalParamsJson(paramJobj);
            String companyid = paramJobj.optString(Constants.companyKey);
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyResult.getEntityList().get(0);
            String countryID = company != null && company.getCountry() != null ? company.getCountry().getID() : "";
            JSONArray jArr = new JSONArray();
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("data",null))){
                jArr = new JSONArray(paramJobj.optString("data"));
            }
            String paymentid = "", paymentno = "",entryno="";
            KwlReturnObject result, result1;
            boolean isMassDelete = true; //flag for bulk delete
            if(jArr.length() == 1){
                isMassDelete = false;
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                paymentid = StringUtil.DecodeText(jobj.optString("billid"));
                paymentno = jobj.getString("billno");
                entryno=jobj.optString("entryno","");
                String journalentryids=jobj.optString("journalentryid");

                // To check whether the Payment is set as recurring and set as Parent payemnt for other payments.
                HashMap<String, Object> paramsForRepeatedPayment = new HashMap<String, Object>();
                paramsForRepeatedPayment.put("parentPaymentId", paymentid);
                KwlReturnObject details = accVendorPaymentobj.getRepeatePaymentDetails(paramsForRepeatedPayment);
                List detailsList = details.getEntityList();
                if(detailsList.size()>0){
                    throw new AccountingException(messageSource.getMessage("acc.alert.MpSetAsRecurringCanNotDeleted", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }
                
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid);
                Payment payment = (Payment) objItr.getEntityList().get(0);
                if (payment.getSalesReturn() != null) {
                    throw new AccountingException("Make Payment : "+payment.getPaymentNumber()+messageSource.getMessage("acc.payment.sales.return.deleted", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }
                
                /**
                 * Method to check the payment is Reconciled or not according to its JE id
                 */
                HashMap<String, Object> reqParams = new HashMap<String, Object>();
                reqParams.put("jeid", journalentryids);
                reqParams.put("companyid", companyid);
                boolean isPaymentReconciledFlag = accBankReconciliationObj.isRecordReconciled(reqParams);
                if (isPaymentReconciledFlag) {
                    if(isMassDelete){ //if bulk delete then append payment no
                        unableToDeletePaymentNos += paymentno + ", ";
                        continue;
                    } else{ //if single payment delete then throw proper message
                        throw new AccountingException(messageSource.getMessage("acc.reconcilation.Cannotdeletepayment", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)))+" "+"<b>"+paymentno+"</b>"+" "+messageSource.getMessage("acc.reconcilation.asitisreconciled", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                    }
                }
                //Delete TDSDetails              
                objItr = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) objItr.getEntityList().get(0);

                if (extraCompanyPreferences.isTDSapplicable()) {
                    HashMap<String, String> hmData = new HashMap<String, String>();
                    String res = "";
                    ArrayList<String> resList = new ArrayList<String>();
                    hmData.put("companyid", payment.getCompany().getCompanyID());
                    if (payment.getAdvanceDetails().size() > 0) {
                        Set<AdvanceDetail> rowObj = payment.getAdvanceDetails();
                        for (AdvanceDetail advDetail : rowObj) {
                            resList.add("'" + advDetail.getId() + "'");
                        }
                        res = StringUtils.collectionToCommaDelimitedString(resList);
                        hmData.put("documentid", res);
                        hmData.put("documenttype", Constants.AdvancePayment + "");
                        accVendorPaymentobj.deleteTDSDetails(hmData);
                    }
                    if (payment.getRows().size() > 0) {
                        Set<PaymentDetail> rowObj = payment.getRows();
                        for (PaymentDetail advDetail : rowObj) {
                            resList.add("'" + advDetail.getID() + "'");
                        }
                        res = StringUtils.collectionToCommaDelimitedString(resList);
                        hmData.put("documentid", res);
                        hmData.put("documenttype", Constants.PaymentAgainstInvoice + "");
                        accVendorPaymentobj.deleteTDSDetails(hmData);
                    }
                    if (payment.getPaymentDetailOtherwises().size() > 0) {
                        Set<PaymentDetailOtherwise> rowObj = payment.getPaymentDetailOtherwises();
                        for (PaymentDetailOtherwise advDetail : rowObj) {
                            resList.add("'" + advDetail.getID() + "'");
                        }
                        res = StringUtils.collectionToCommaDelimitedString(resList);
                        hmData.put("documentid", res);
                        hmData.put("documenttype", Constants.GLPayment + "");
                        accVendorPaymentobj.deleteTDSDetails(hmData);
                    }
                    if (payment.getCreditNotePaymentDetails().size() > 0) {
                        Set<CreditNotePaymentDetails> rowObj = payment.getCreditNotePaymentDetails();
                        for (CreditNotePaymentDetails advDetail : rowObj) {
                            resList.add("'" + advDetail.getID() + "'");
                        }
                        res = StringUtils.collectionToCommaDelimitedString(resList);
                        hmData.put("documentid", res);
                        hmData.put("documenttype", Constants.PaymentAgainstCNDN + "");
                        accVendorPaymentobj.deleteTDSDetails(hmData);
                    }
                }
                //Delete unrealised JE for Advance Payment
                if(!payment.getAdvanceDetails().isEmpty()){
                    accJournalEntryobj.permanentDeleteJournalEntryDetailReval(payment.getID(), companyid);
                    accJournalEntryobj.permanentDeleteJournalEntryReval(payment.getID(), companyid);
                    /**
                     * Deleting Reevaluation History of the payment being
                     * deleted.
                     */
                    JSONObject historyDeleteParams = new JSONObject();
                    historyDeleteParams.put("documentId", payment.getID());
                    historyDeleteParams.put("companyID", companyid);
                    accJournalEntryobj.deleteRevaluationHistory(historyDeleteParams);
                }     
                //Delete Realised JE which was posted aginst Invoice
                if (payment != null && payment.getRevalJeId() != null) {
                    result = accJournalEntryobj.deleteJEDtails(payment.getRevalJeId(), companyid);// 2 For realised JE
                    result = accJournalEntryobj.deleteJE(payment.getRevalJeId(), companyid);
                }
                /*
                    Delete Forex Gains/Loss JEs 
                */
                if(payment.getLinkDetailPayments()!= null && !payment.getLinkDetailPayments().isEmpty()) {
                    Set<LinkDetailPayment> linkedDetailPaymentList = payment.getLinkDetailPayments();
                    for(LinkDetailPayment ldprow : linkedDetailPaymentList) {
                        if(!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                            result = accJournalEntryobj.deleteJEDtails(ldprow.getLinkedGainLossJE(), companyid);
                            result = accJournalEntryobj.deleteJE(ldprow.getLinkedGainLossJE(), companyid);
                        }
                        //Deleting Revaluation journal entry 
                        if (ldprow != null && !StringUtil.isNullOrEmpty(ldprow.getRevalJeId())) {
                            accJournalEntryobj.deleteJEDtails(ldprow.getRevalJeId(), companyid);
                            accJournalEntryobj.deleteJE(ldprow.getRevalJeId(), companyid);
                        }
                        if (ldprow != null && !StringUtil.isNullOrEmpty(ldprow.getRevalJeIdPayment())) {
                            accJournalEntryobj.deleteJEDtails(ldprow.getRevalJeIdPayment(), companyid);
                            accJournalEntryobj.deleteJE(ldprow.getRevalJeIdPayment(), companyid);
                        }
                    }
                }
                if(payment.getLinkDetailPaymentToCreditNote()!= null && !payment.getLinkDetailPaymentToCreditNote().isEmpty()) {
                    Set<LinkDetailPaymentToCreditNote> linkedDetailPaymentList = payment.getLinkDetailPaymentToCreditNote();
                    for(LinkDetailPaymentToCreditNote ldprow : linkedDetailPaymentList) {
                        if(!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                            result = accJournalEntryobj.deleteJEDtails(ldprow.getLinkedGainLossJE(), companyid);
                            result = accJournalEntryobj.deleteJE(ldprow.getLinkedGainLossJE(), companyid);
                        }
                        //Deleting Revaluation journal entry 
                        if (ldprow != null && !StringUtil.isNullOrEmpty(ldprow.getRevalJeId())) {
                            accJournalEntryobj.deleteJEDtails(ldprow.getRevalJeId(), companyid);
                            accJournalEntryobj.deleteJE(ldprow.getRevalJeId(), companyid);
                        }
                        if (ldprow != null && !StringUtil.isNullOrEmpty(ldprow.getRevalJeIdPayment())) {
                            accJournalEntryobj.deleteJEDtails(ldprow.getRevalJeIdPayment(), companyid);
                            accJournalEntryobj.deleteJE(ldprow.getRevalJeIdPayment(), companyid);
                        }
                    }
                }
                
                /*
                 * Deleting Advance Payment entries for refund payment
                 */
                if (payment.getLinkDetailPaymentsToAdvancePayment() != null && !payment.getLinkDetailPaymentsToAdvancePayment().isEmpty()) {
                    Set<LinkDetailPaymentToAdvancePayment> linkedDetailPaymentList = payment.getLinkDetailPaymentsToAdvancePayment();
                    for (LinkDetailPaymentToAdvancePayment ldprow : linkedDetailPaymentList) {
                        // Delete Forex Gains/Loss JEs 
                        if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                            result = accJournalEntryobj.deleteJEDtails(ldprow.getLinkedGainLossJE(), companyid);
                            result = accJournalEntryobj.deleteJE(ldprow.getLinkedGainLossJE(), companyid);
                        }
                        // Deleting Revaluation journal entry
                        if (ldprow != null && !StringUtil.isNullOrEmpty(ldprow.getRevalJeId())) {
                            accJournalEntryobj.deleteJEDtails(ldprow.getRevalJeId(), companyid);
                            accJournalEntryobj.deleteJE(ldprow.getRevalJeId(), companyid);
                        }
                        if (ldprow != null && !StringUtil.isNullOrEmpty(ldprow.getRevalJeIdReceipt())) {
                            accJournalEntryobj.deleteJEDtails(ldprow.getRevalJeIdReceipt(), companyid);
                            accJournalEntryobj.deleteJE(ldprow.getRevalJeIdReceipt(), companyid);
                        }
                    }
                }
                /*
                 * Before deleting PaymentDetail and LinkDetailPayment Keeping
                 * id of Goodsrceipt utlized in Payment
                 */
                Set<String> grIDSet = new HashSet<>();
                if(payment!=null && payment.getApprovestatuslevel() == 11 && !payment.isDeleted()){
                    for(PaymentDetail pd:payment.getRows()){
                        if(pd.getGoodsReceipt()!=null){
                            grIDSet.add(pd.getGoodsReceipt().getID());
                        }
                    }
                    for(LinkDetailPayment pd:payment.getLinkDetailPayments()){
                        if(pd.getGoodsReceipt()!=null){
                            grIDSet.add(pd.getGoodsReceipt().getID());
                        }
                    }
                }
                
                /*
                 * Deleting Bad Debt Invoices Recovery entries
                 */
                HashMap<String, Object> requestParamsForMapping = new HashMap();
                requestParamsForMapping.put(Constants.companyid, companyid);
                requestParamsForMapping.put("paymentid", paymentid);
                accGoodsReceiptobj.deleteBadDebtPurchaseInvoiceMapping(requestParamsForMapping);
                if (payment != null) {
                    if (payment.getApprovestatuslevel() == 11) {
                        updateOpeningBalance(payment, companyid);
                        if (payment.getLinkDetailPaymentsToAdvancePayment() == null || payment.getLinkDetailPaymentsToAdvancePayment().isEmpty()&&!payment.isDeleted()) {
                            updateReceiptAdvancePaymentAmountDue(payment, companyid, countryID);
                        }
                        updateCNAmountDueForReceipt(payment, companyid);
                    }
                }
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("paymentid", paymentid);
                requestParams.put("companyid", companyid);
                requestParams.put("paymentno", paymentno);
                if (!StringUtil.isNullOrEmpty(paymentid)) {
                    DateFormat dateFormatForLock = authHandler.getDateOnlyFormat();
                    Date entryDateForLock = null;
                    if (jobj.has("billdate")) {
                        entryDateForLock = dateFormatForLock.parse(jobj.getString("billdate"));
                    }
                    if (entryDateForLock != null) {
                        requestParams.put("entrydate", entryDateForLock);
                        requestParams.put("df", dateFormatForLock);
                    }
                    /*
                     * If make Payment is deleted as temporary then payment.isdelete flag will be true and amount due isn't update.
                     * if payment.isdeleted is false amount due is update.
                     */
                    if (payment.getLinkDetailPayments() != null && !payment.getLinkDetailPayments().isEmpty()) {
                        if (!payment.isDeleted()) {
                            accVendorPaymentobj.deleteLinkDetailsAndUpdateAmountDue(requestMap,paymentid, companyid,false);
                        } else if (payment.isDeleted()) {
                            accVendorPaymentobj.deleteLinkPaymentsDetails(paymentid, companyid);
                        }
                    }
                    if (payment.getLinkDetailPaymentToCreditNote()!=null&&!payment.getLinkDetailPaymentToCreditNote().isEmpty()) {
                        if (!payment.isDeleted()) {
                            accVendorPaymentobj.deleteLinkPaymentDetailsToCreditNoteAndUpdateAmountDue(paymentid, companyid,false);
                        } else if (payment.isDeleted()) {
                            accVendorPaymentobj.deleteLinkPaymentToCreditNoteDetails(paymentid, companyid);
                        }
                    }
                    // for deleting link details of advance payment for refund receipt
                    if (payment.getLinkDetailPaymentsToAdvancePayment() != null && !payment.getLinkDetailPaymentsToAdvancePayment().isEmpty()) {
                        if (!payment.isDeleted()) {
                            accVendorPaymentobj.deleteLinkPaymentDetailsToAdvancePaymentAndUpdateAmountDue(paymentid, companyid,false);
                        } else if (payment.isDeleted()) {
                            accVendorPaymentobj.deleteLinkPaymentToAdvancePaymentDetails(paymentid, companyid);
                        }
                    }
                    
                        /*Deleting linking information of Make payment when it is deleted */
                        accVendorPaymentobj.deleteLinkingInformationOfMP(requestParams);
                        requestParams.put("approvalStatusLevel", payment.getApprovestatuslevel());           //adding payment approvalStatusLevel to requestParams to check in method deletePaymentPermanent
                        requestParams.put("journalentryids", journalentryids);
                        requestParams.put("isDeleted", payment.isDeleted());
                     /*
                     *ERP-40733
                     *Throwing accounting exception for Account locking period and get unable to delete numbers
                     */
                    try {
                        accVendorPaymentobj.deletePaymentPermanent(requestParams);
                    } catch (AccountingException ex) {
                        unableToDeletePaymentNos += paymentno + ", ";
                        continue;
                    }
                    if (payment.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                        /**
                         * delete GST history related data.
                         */
                        Set<AdvanceDetail> advanceDetails = payment.getAdvanceDetails();
                        if (payment.getAdvanceDetails() != null && !advanceDetails.isEmpty()) {
                            String idstring = "";
                            for (AdvanceDetail receiptAdvanceDetail : advanceDetails) {
                                idstring += "'" + receiptAdvanceDetail.getId() + "',";
                            }
                            if (idstring.length() > 1) {
                                idstring = idstring.substring(0, idstring.length() - 1);
                                fieldManagerDAOobj.deleteGstTaxClassDetails(idstring);
                            }
                            fieldManagerDAOobj.deleteGstDocHistoryDetails(paymentid);
                        }
                    }
                    
                    int countryid = company.getCountry() != null ? Integer.parseInt(company.getCountry().getID()) : 0;
                    
                    //Delete Rouding JEs if created against PI
                    String roundingJENo = "";
                    String roundingIDs = "";
                    String piIDs = "";
                    if (!grIDSet.isEmpty()) {
                        for (String piID : grIDSet) {
                            piIDs += piID + ",";
                        }
                        if (!StringUtil.isNullOrEmpty(piIDs)) {
                            piIDs = piIDs.substring(0, piIDs.length() - 1);
                        }
                        KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(piIDs, companyid);
                        List<JournalEntry> jeList = jeResult.getEntityList();
                        for (JournalEntry roundingJE : jeList) {
                            roundingJENo = roundingJE.getEntryNumber() + ",";
                            roundingIDs = roundingJE.getID() + ",";
                            deleteJEArray(roundingJE.getID(), companyid);
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                        roundingJENo = roundingJENo.substring(0, roundingJENo.length() - 1);
                    }
                    if (!StringUtil.isNullOrEmpty(roundingIDs)) {
                        roundingIDs = roundingIDs.substring(0, roundingIDs.length() - 1);
                    }
                    // For India Country 
                    boolean isVatSalesJE = false;
                    boolean isVatPurchaseJE = false;
                    if(countryid == Constants.indian_country_id){
                        JSONObject datamap = new JSONObject();
                        datamap.put("companyid",companyid);
                        datamap.put("table","InvoiceDetailTermsMap");
                        datamap.put("paymentid",paymentid);
                        datamap.put("istaxpaid",1);
                        datamap.put("termtype",IndiaComplianceConstants.LINELEVELTERMTYPE_VAT);
                        isVatSalesJE = accJournalEntryobj.checkIfJEisVatJE(datamap);
                        datamap.put("table","ReceiptDetailTermsMap");
                        isVatPurchaseJE = accJournalEntryobj.checkIfJEisVatJE(datamap);
                        
                        HashMap paramsHM = new HashMap();
                        paramsHM.put("paymentid", paymentid);
                        accInvoiceDAOobj.resetInvoiceTaxPaidFlag(paramsHM);
                        accGoodsReceiptobj.resetGoodsRecieptTaxPaidFlag(paramsHM);
                        accGoodsReceiptobj.resetGoodsRecieptTDSPaidFlag(paramsHM);
                        accDebitNoteDAO.resetDebitNoteTDSPaidFlag(paramsHM);
                        accVendorPaymentobj.resetAdvancePaymentTDSPaidFlag(paramsHM);
                        accGoodsReceiptobj.resetPurchaseReturnTaxPaidFlag(paramsHM);
                        accInvoiceDAOobj.resetSalesReturnTaxPaidFlag(paramsHM);
                    }
                    
                    StringBuffer journalEntryMsg = new StringBuffer();
                    if (!StringUtil.isNullOrEmpty(entryno)) {
                        if (isVatSalesJE || isVatPurchaseJE) {
                            journalEntryMsg.append(" " + messageSource.getMessage("acc.vat.payment.delete", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " " + entryno);
                        } else {
                            journalEntryMsg.append(" " + messageSource.getMessage("acc.je.payment.delete", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " " + entryno);
                        }
                    }
                    Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                    auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                    auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                    auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
                    auditTrailObj.insertAuditLog(AuditAction.MAKE_PAYMENT, "User " + paramJobj.optString(Constants.userfullname) + " has deleted a Payment Permanently " + paymentno + journalEntryMsg.toString(), auditRequestParams, paymentid);
                    if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                        auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_DELETED, "User " + paramJobj.optString(Constants.userfullname) + " has deleted a Payment Permanently " + paymentno + ". So Rounding JE No. " + roundingJENo + " deleted.", auditRequestParams, roundingIDs);
                    }
                }
            }

        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
        }
        return unableToDeletePaymentNos;
    }

   /**
     * Description: This Method is used to Update CREDIT NOTE Amount Due in case Delete(temporary/Permanent) 
     * @param payment
     * @param companyId
     * @throws ServiceException 
     */
  @Override
    public void updateCNAmountDueForReceipt(Payment payment, String companyId) throws ServiceException {
        if (payment != null && !payment.isDeleted()) {// if payment already temporary deleted then amountdue already updated. No need to update amountdue again for permament delete.
            String paymentid = payment.getID();
            KwlReturnObject cnhistoryresult = accVendorPaymentobj.getVendorCnPayment(paymentid);
            List<CreditNotePaymentDetails> cnHistoryList = cnhistoryresult.getEntityList();
            for (CreditNotePaymentDetails cnpd : cnHistoryList) {
                String cnnoteid = cnpd.getCreditnote().getID() != null ? cnpd.getCreditnote().getID() : "";
                Double cnpaidamount = cnpd.getAmountPaid();
                Double cnPaidAmountInBaseCurrency = cnpd.getAmountInBaseCurrency();
                KwlReturnObject cnjedresult = accVendorPaymentobj.updateCnAmount(cnnoteid, -cnpaidamount);
                KwlReturnObject opencnjedresult = accVendorPaymentobj.updateCnOpeningAmountDue(cnnoteid, -cnpaidamount);
                KwlReturnObject openingCnBaseAmtDueResult = accPaymentDAOobj.updateCnOpeningBaseAmountDue(cnnoteid, -cnPaidAmountInBaseCurrency);
                if (cnpd.getCreditnote() != null && !StringUtil.isNullOrEmpty(cnpd.getCreditnote().getRevalJeId())) {
                    accVendorPaymentobj.deleteJEDtails(cnpd.getCreditnote().getRevalJeId(), companyId);
                    accVendorPaymentobj.deleteJEEntry(cnpd.getCreditnote().getRevalJeId(), companyId);
                }
            }
        }
    }
   
    public void deletePaymentPermanent(JSONObject paramJobj, String paymentId) throws AccountingException, SessionExpiredException, ServiceException, ParseException {
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            String paymentid = "", paymentno = "";
            KwlReturnObject result;
            paymentid = paymentId;
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid);
            Payment payment = (Payment) objItr.getEntityList().get(0);
            if (payment != null && payment.getRevalJeId() != null) {
                result = accJournalEntryobj.deleteJEDtails(payment.getRevalJeId(), companyid);// 2 For realised JE
                result = accJournalEntryobj.deleteJE(payment.getRevalJeId(), companyid);
            }
            if (payment != null) {
                updateOpeningBalance(payment, companyid);
                String countryID = payment.getCompany().getCountry().getID();
                updateReceiptAdvancePaymentAmountDue(payment, companyid, countryID);
                updateCNAmountDueForReceipt(payment, companyid);
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("paymentid", paymentid);
            requestParams.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(paymentid)) {
                DateFormat dateFormatForLock = authHandler.getDateOnlyFormat();
                Date entryDateForLock = null;
                if (payment != null && payment.getCreationDate() != null) {
                    entryDateForLock = payment.getCreationDate();
                }
                if (entryDateForLock != null) {
                    requestParams.put("entrydate", entryDateForLock);
                    requestParams.put("df", dateFormatForLock);
                }
                accVendorPaymentobj.deletePaymentPermanent(requestParams);
                 Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
                auditTrailObj.insertAuditLog(AuditAction.MAKE_PAYMENT, "User " + paramJobj.optString(Constants.userfullname) + " has deleted a Payment Permanently " + paymentno, auditRequestParams, paymentid);
            }

        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
        }
    }
   
    
    /*
        Update receipt advance amount due on payment with refund entry delete
    */
   
   @Override 
     public void updateReceiptAdvancePaymentAmountDue(Payment payment, String companyId, String countryId) throws JSONException, ServiceException {
        if (payment != null) {
            Set<AdvanceDetail> advanceDetails = payment.getAdvanceDetails();
            if (payment.getAdvanceDetails() != null && !advanceDetails.isEmpty()) {
                for(AdvanceDetail adv : advanceDetails) {
                    if(adv.getReceiptAdvanceDetails()!=null) {
                        double finalAmtDue = authHandler.round((adv.getReceiptAdvanceDetails().getAmountDue()+(adv.getAmount()/adv.getExchangeratefortransaction())), companyId);
                        adv.getReceiptAdvanceDetails().setAmountDue(finalAmtDue);
                        if (adv != null && adv.getRevalJeId() != null) { //Deleting Refund JE Detail if any
                             accJournalEntryobj.deleteJEDtails(adv.getRevalJeId(), companyId);
                             accJournalEntryobj.deleteJE(adv.getRevalJeId(), companyId);
                        }
                    }
                     if (!StringUtil.isNullOrEmpty(countryId) && countryId.equals(Constants.INDIA_COUNTRYID)) {
                         /**
                          * delete AdvanceDetailTermMap mapping while editing
                          * payment.
                          */
                         JSONObject reqPrams = new JSONObject();
                         reqPrams.put("advanceDetailid", adv.getId());
                         accVendorPaymentobj.deleteAdvanceDetailsTerm(reqPrams);
                     }
                 }
             }
         }
     }
    
    /*
     * Update invoice due amount when payment is being deleted.
     */
  @Override 
    public void updateOpeningBalance(Payment payment, String companyId) throws JSONException, ServiceException {
        if (payment != null) {
            Set<PaymentDetail> paymentDetailSet = payment.getRows();
            if (paymentDetailSet != null && !payment.isDeleted()) { // if payment already temporary deleted then amountdue already updated. No need to update amountdue again for permament delete.
                Iterator itr = paymentDetailSet.iterator();
                while (itr.hasNext()) {
                    PaymentDetail row = (PaymentDetail) itr.next();
                    double discountAmtInInvoiceCurrency = row.getDiscountAmountInInvoiceCurrency();
                    double discountAmount = row.getDiscountAmount();
                    if (!row.getGoodsReceipt().isNormalInvoice() && row.getGoodsReceipt().isIsOpeningBalenceInvoice()) {
                        double amountPaid = row.getAmount() + row.getDiscountAmount();
//                        double amountPaidInBaseCurrency=row.getAmountInBaseCurrency();
                        GoodsReceipt goodsReceipt = row.getGoodsReceipt();
                        boolean isInvoiceIsClaimed = (goodsReceipt.getBadDebtType() == Constants.Invoice_Claimed || goodsReceipt.getBadDebtType() == Constants.Invoice_Recovered);
                        double invoiceAmountDue = goodsReceipt.getOpeningBalanceAmountDue();
                        double totalInvoiceAndDiscountAmtInInvoiceCurrency = row.getAmountInGrCurrency()+discountAmtInInvoiceCurrency;
                        HashMap<String, Object> requestParams = new HashMap();
                        requestParams.put(Constants.companyid, payment.getCompany().getCompanyID());
                        requestParams.put("gcurrencyid", payment.getCompany().getCurrency().getCurrencyID());
//                        double discountAmountInBase = authHandler.round(discountAmtInInvoiceCurrency*goodsReceipt.getExchangeRateForOpeningTransaction(),companyId);
                        double totalInvoiceAndDiscountAmtInBase = 0d;
                        if (goodsReceipt.isConversionRateFromCurrencyToBase()) {
                            totalInvoiceAndDiscountAmtInBase = authHandler.round(totalInvoiceAndDiscountAmtInInvoiceCurrency * goodsReceipt.getExchangeRateForOpeningTransaction(), companyId);
                        } else {
                            totalInvoiceAndDiscountAmtInBase = authHandler.round(totalInvoiceAndDiscountAmtInInvoiceCurrency / goodsReceipt.getExchangeRateForOpeningTransaction(), companyId);
                        }
                        double openingbalanceBaseAmountDue = goodsReceipt.getOpeningBalanceBaseAmountDue() + totalInvoiceAndDiscountAmtInBase;
                        if (row.getExchangeRateForTransaction() != 0) {
                            invoiceAmountDue += row.getAmountInGrCurrency()+discountAmtInInvoiceCurrency;
                        } else {
                            invoiceAmountDue += amountPaid;
                        }
                        Map<String, Object> greceipthm = new HashMap<String, Object>();
                        greceipthm.put("grid", goodsReceipt.getID());;
                        greceipthm.put("companyid", companyId);
                        if (isInvoiceIsClaimed) {
                            greceipthm.put(Constants.claimAmountDue, goodsReceipt.getClaimAmountDue() + row.getAmountInGrCurrency());
                        } else {
                            greceipthm.put("openingBalanceAmountDue", invoiceAmountDue);
                            greceipthm.put(Constants.openingBalanceBaseAmountDue, openingbalanceBaseAmountDue);
                            if (invoiceAmountDue != 0) {
                                greceipthm.put("amountduedate", "");
                            }
                        }
                        accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                    } else if(row.getGoodsReceipt().isNormalInvoice()){
                        double amountPaid = row.getAmount();
                        GoodsReceipt goodsReceipt = row.getGoodsReceipt();
                        boolean isInvoiceIsClaimed = (goodsReceipt.getBadDebtType() == Constants.Invoice_Claimed || goodsReceipt.getBadDebtType() == Constants.Invoice_Recovered);
                        Map<String, Object> greceipthm = new HashMap<String, Object>();
                        greceipthm.put("grid", goodsReceipt.getID());;
                        greceipthm.put("companyid", companyId);
                        if (isInvoiceIsClaimed) {
                            greceipthm.put(Constants.claimAmountDue, goodsReceipt.getClaimAmountDue() + row.getAmountInGrCurrency());
                        } else {
                            double amountPaidInInvoiceCurrecncy = row.getAmountInGrCurrency();
                            double totalInvoiceAndDiscountAmtInInvoiceCurrency = amountPaidInInvoiceCurrecncy+discountAmtInInvoiceCurrency;
                            double invoiceAmountDue = goodsReceipt.getInvoiceamountdue();
                            if (row.getExchangeRateForTransaction() != 0) {
                                if (amountPaidInInvoiceCurrecncy != 0) {
                                    invoiceAmountDue += (totalInvoiceAndDiscountAmtInInvoiceCurrency);
                                } else {
                                    invoiceAmountDue += ((amountPaid+discountAmount) / row.getExchangeRateForTransaction());
                                }
                            } else {
                                invoiceAmountDue += amountPaid;
                            }
                            greceipthm.put(Constants.invoiceamountdue, invoiceAmountDue);
//                            double amountPaidInBaseCurrency = row.getAmountInBaseCurrency();
                            double totalInvoiceAmtPaidAndDiscountInBase = 0d;
                            HashMap<String, Object> requestParams = new HashMap();
                            requestParams.put(Constants.companyid, companyId);
                            requestParams.put("gcurrencyid", goodsReceipt.getCurrency().getCurrencyID());
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalInvoiceAndDiscountAmtInInvoiceCurrency, goodsReceipt.getCurrency().getCurrencyID(), goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                            totalInvoiceAmtPaidAndDiscountInBase = ((Double) bAmt.getEntityList().get(0));
                            double invoiceBaseAmountDue = goodsReceipt.getInvoiceAmountDueInBase() + totalInvoiceAmtPaidAndDiscountInBase;
                            greceipthm.put(Constants.invoiceamountdueinbase, invoiceBaseAmountDue);
                            if (invoiceAmountDue != 0) {
                                greceipthm.put("amountduedate", "");
                            }
                        }
                        accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                    }
                }
            }
        }
    }
   
 @Override
 @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, SessionExpiredException.class, JSONException.class, AccountingException.class})
    public JSONObject deletePaymentTemporaryJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        try {
            String unableToDeletePaymentNos = deletePaymentTemporary(paramJobj);
            issuccess = true;
            if(StringUtil.isNullOrEmpty(unableToDeletePaymentNos)){
                msg = messageSource.getMessage("acc.pay.del", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));  //"Payment(s) has been deleted successfully";
            } else{
                msg = messageSource.getMessage("acc.ob.PMExcept", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)))+" "+unableToDeletePaymentNos.substring(0, unableToDeletePaymentNos.length()-2)+" "+messageSource.getMessage("acc.field.deletedsuccessfully", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)))+ " " +messageSource.getMessage("acc.field.belongstolockingperiod", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));   
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    } 
    
 @Override
    public String deletePaymentTemporary(JSONObject paramJobj) throws AccountingException, SessionExpiredException, ServiceException {
        String unableToDeletePaymentNos = "";
        try {
            
            Map<String, Object> requestMap = AccountingManager.getGlobalParamsJson(paramJobj);
            String companyid = paramJobj.optString(Constants.companyKey);
            JSONArray jArr = new JSONArray(paramJobj.optString("data","[{}]"));
            String paymentid = "", jeid = "", paymentno = "", entryno = "";
            KwlReturnObject result;
            int countryid = 0;
            boolean isPaymentReconciledFlag = false;
            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyObj.getEntityList().get(0);
            if (company.getCountry() != null && company.getCountry().getID() != null) {
                countryid = Integer.parseInt(company.getCountry().getID());
            }
            boolean isMassDelete = true; //flag for bulk delete
            if (jArr.length() == 1) {
                isMassDelete = false;
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                boolean isVatSalesJE = false;
                boolean isVatPurchaseJE = false;
                paymentid = StringUtil.DecodeText(jobj.optString("billid"));
                jeid = StringUtil.DecodeText(jobj.optString("journalentryid"));
                paymentno = jobj.getString("billno");
                entryno = jobj.optString("entryno", "");
                String roundingJENo = "";
                String roundingJEIds = "";
                boolean withoutinventory = Boolean.parseBoolean(jobj.optString("withoutinventory", "false"));
                if (withoutinventory) {
                    result = accVendorPaymentobj.deleteBillingPaymentEntry(paymentid, companyid);

                    result = accVendorPaymentobj.getJEFromBillingPayment(paymentid, companyid);
                    List list = result.getEntityList();
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        String jeId = ((BillingPayment) itr.next()).getJournalEntry().getID();
                        result = accJournalEntryobj.deleteJournalEntry(jeId, companyid);
                        //Delete entry from optimized table
                        accJournalEntryobj.deleteAccountJEs_optimized(jeId);
                    }
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(BillingPayment.class.getName(), paymentid);
                    BillingPayment billingPayment = (BillingPayment) objItr.getEntityList().get(0);
                    if (billingPayment != null && billingPayment.getRevalJeId() != null) {
                        result = accJournalEntryobj.deleteJournalEntry(billingPayment.getRevalJeId(), companyid);
                    }
                } else {
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid);
                    Payment payment = (Payment) objItr.getEntityList().get(0);
                    /**
                     * Method to check the payment is Reconciled or not
                     * according to its JE id
                     */
                    HashMap<String, Object> requestParamsForMapping = new HashMap();
                    requestParamsForMapping.put("jeid", jeid);
                    requestParamsForMapping.put("companyid", companyid);
                    isPaymentReconciledFlag = accBankReconciliationObj.isRecordReconciled(requestParamsForMapping);
                    if (isPaymentReconciledFlag) {
                        if (isMassDelete) { //if bulk delete then append payment no
                            unableToDeletePaymentNos += paymentno + ", ";
                            continue;
                        } else { //if single payment delete then throw proper message
                            throw new AccountingException(messageSource.getMessage("acc.reconcilation.Cannotdeletepayment", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " " + "<b>" + paymentno + "</b>" + " " + messageSource.getMessage("acc.reconcilation.asitisreconciled", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                        }
                    }
                    /*
                     * Before deleting PaymentDetail and LinkDetailPayment
                     * Keeping id of Goodsrceipt utlized in Payment
                     */
                    Set<String> grIDSet = new HashSet<>();
                    if (payment != null && payment.getApprovestatuslevel() == 11) {
                        for (PaymentDetail pd : payment.getRows()) {
                            if (pd.getGoodsReceipt() != null) {
                                grIDSet.add(pd.getGoodsReceipt().getID());
                            }
                        }
                        for (LinkDetailPayment pd : payment.getLinkDetailPayments()) {
                            if (pd.getGoodsReceipt() != null) {
                                grIDSet.add(pd.getGoodsReceipt().getID());
                            }
                        }
                    }

                    if (payment != null) {
                        if (payment.getApprovestatuslevel() == 11) {
                            updateOpeningBalance(payment, companyid);
                            if (payment.getLinkDetailPaymentsToAdvancePayment() == null || payment.getLinkDetailPaymentsToAdvancePayment().isEmpty()) {
                                updateReceiptAdvancePaymentAmountDue(payment, companyid, (countryid + ""));
                            }
                            updateCNAmountDueForReceipt(payment, companyid);
                        }
                        /**
                         * Setting deleteflag = true of Unrealized JE of Advance
                         * Type payment being deleted.
                         */
                        if (payment != null && payment.getAdvanceDetails() != null && !payment.getAdvanceDetails().isEmpty()) {
                            result = accJournalEntryobj.deleteJournalEntryReval(payment.getID(), companyid);
                        }
                    }
                    if (payment.getSalesReturn() != null) {
                        throw new AccountingException("Make Payment : " + payment.getPaymentNumber() + messageSource.getMessage("acc.payment.sales.return.deleted", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                    }
                    /*
                     * Checking if the JE is vat payment JE
                     */
                    if (countryid == Constants.indian_country_id) {
                        JSONObject datamap = new JSONObject();
                        datamap.put("companyid", companyid);
                        datamap.put("table", "InvoiceDetailTermsMap");
                        datamap.put("paymentid", paymentid);
                        datamap.put("istaxpaid", 1);
                        datamap.put("termtype", IndiaComplianceConstants.LINELEVELTERMTYPE_VAT);
                        isVatSalesJE = accJournalEntryobj.checkIfJEisVatJE(datamap);
                        datamap.put("table", "ReceiptDetailTermsMap");
                        isVatPurchaseJE = accJournalEntryobj.checkIfJEisVatJE(datamap);

                        HashMap paramsHM = new HashMap();
                        paramsHM.put("paymentid", paymentid);
                        accInvoiceDAOobj.resetInvoiceTaxPaidFlag(paramsHM);
                        accGoodsReceiptobj.resetGoodsRecieptTaxPaidFlag(paramsHM);
                        accGoodsReceiptobj.resetGoodsRecieptTDSPaidFlag(paramsHM);
                        accDebitNoteDAO.resetDebitNoteTDSPaidFlag(paramsHM);
                        accVendorPaymentobj.resetAdvancePaymentTDSPaidFlag(paramsHM);
                        accGoodsReceiptobj.resetPurchaseReturnTaxPaidFlag(paramsHM);
                        accInvoiceDAOobj.resetSalesReturnTaxPaidFlag(paramsHM);
                    }
                    if (payment.getLinkDetailPayments() != null && !payment.getLinkDetailPayments().isEmpty()) {
                        accVendorPaymentobj.deleteLinkDetailsAndUpdateAmountDue(requestMap, paymentid, companyid, true);
                    }
                    if (payment.getLinkDetailPaymentToCreditNote() != null && !payment.getLinkDetailPaymentToCreditNote().isEmpty()) {
                        accVendorPaymentobj.deleteLinkPaymentDetailsToCreditNoteAndUpdateAmountDue(paymentid, companyid, true);
                    }
                    /*
                     * Deleting Advance Payment entries for refund payment
                     */
                    if (payment.getLinkDetailPaymentsToAdvancePayment() != null && !payment.getLinkDetailPaymentsToAdvancePayment().isEmpty()) {
                        Set<LinkDetailPaymentToAdvancePayment> linkedDetailPaymentList = payment.getLinkDetailPaymentsToAdvancePayment();
                        for (LinkDetailPaymentToAdvancePayment ldprow : linkedDetailPaymentList) {
                            /*
                             * Code is commented beacause now we have deleted
                             * linked je while deleting receive payment as
                             * permanent
                             */
                            // Deleting Revaluation journal entry
                            if (ldprow != null && !StringUtil.isNullOrEmpty(ldprow.getRevalJeId())) {
                                accJournalEntryobj.deleteJEDtails(ldprow.getRevalJeId(), companyid);
                                accJournalEntryobj.deleteJE(ldprow.getRevalJeId(), companyid);
                            }
                            if (ldprow != null && !StringUtil.isNullOrEmpty(ldprow.getRevalJeIdReceipt())) {
                                accJournalEntryobj.deleteJEDtails(ldprow.getRevalJeIdReceipt(), companyid);
                                accJournalEntryobj.deleteJE(ldprow.getRevalJeIdReceipt(), companyid);
                            }
                        }

                        accVendorPaymentobj.deleteLinkPaymentDetailsToAdvancePaymentAndUpdateAmountDue(paymentid, companyid, true);
                    }
                    /*
                     * Deleting Bad Debt Invoices Recovery entries
                     */
                    requestParamsForMapping.put(Constants.companyid, companyid);
                    requestParamsForMapping.put("paymentid", paymentid);
                    accGoodsReceiptobj.deleteBadDebtPurchaseInvoiceMapping(requestParamsForMapping);
                    /*
                     *ERP-40733
                     *Throwing accounting exception for Account locking period and get unable to delete numbers
                     */
                    try {
                        result = accVendorPaymentobj.deletePaymentEntry(paymentid, companyid);
                    } catch (AccountingException ex) {
                        unableToDeletePaymentNos += paymentno + ", ";
                        continue;
                    }
                    result = accVendorPaymentobj.getJEFromPayment(paymentid);
                    List list = result.getEntityList();
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        String jeId = (String) itr.next();
                        result = accJournalEntryobj.deleteJournalEntry(jeId, companyid);
                        //Delete entry from optimized table
                        accJournalEntryobj.deleteAccountJEs_optimized(jeId);
                    }
                    /*
                     * If invoice is link to receive payment then it going to
                     * delete as temporary then set isdelete flag true in
                     * journal entry table.
                     */
                    if (payment.getLinkDetailPayments() != null && !payment.getLinkDetailPayments().isEmpty()) {
                        Set<LinkDetailPayment> linkedDetailPaymentList = payment.getLinkDetailPayments();
                        for (LinkDetailPayment ldprow : linkedDetailPaymentList) {
                            if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                                accJournalEntryobj.deleteJournalEntry(ldprow.getLinkedGainLossJE(), companyid);
                            }
                             /**
                             * Setting isdelete=true flag of revaluation JE's
                             * when receipt is temporary deleted.ERP-42090.
                             */
                            if (!StringUtil.isNullOrEmpty(ldprow.getRevalJeId())) {
                                accJournalEntryobj.deleteJournalEntry(ldprow.getRevalJeId(), companyid);
                            }
                            if (!StringUtil.isNullOrEmpty(ldprow.getRevalJeIdPayment())) {
                                accJournalEntryobj.deleteJournalEntry(ldprow.getRevalJeIdPayment(), companyid);
                            }
                        }
                    }
                    /*
                     * If Credit Note is link to receive payment then it going
                     * to delete as temporary then set isdelete flag true in
                     * journal entry table.
                     */

                    if (payment.getLinkDetailPaymentToCreditNote() != null && !payment.getLinkDetailPaymentToCreditNote().isEmpty()) {
                        Set<LinkDetailPaymentToCreditNote> linkedDetailPaymentList = payment.getLinkDetailPaymentToCreditNote();
                        for (LinkDetailPaymentToCreditNote ldprow : linkedDetailPaymentList) {
                            if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                                result = accJournalEntryobj.deleteJournalEntry(ldprow.getLinkedGainLossJE(), companyid);
                            }
                             /**
                             * Setting isdelete=true flag of revaluation JE's
                             * when receipt is temporary deleted.ERP-42090.
                             */
                            if (!StringUtil.isNullOrEmpty(ldprow.getRevalJeId())) {
                                accJournalEntryobj.deleteJournalEntry(ldprow.getRevalJeId(), companyid);
                            }
                            if (!StringUtil.isNullOrEmpty(ldprow.getRevalJeIdPayment())) {
                                accJournalEntryobj.deleteJournalEntry(ldprow.getRevalJeIdPayment(), companyid);
                            }
                        }
                    }
                    /*
                     * If Receive Pyament is link to receive payment then it
                     * going to delete as temporary then set isdelete flag true
                     * in journal entry table.
                     */
                    if (payment.getLinkDetailPaymentsToAdvancePayment() != null && !payment.getLinkDetailPaymentsToAdvancePayment().isEmpty()) {
                        Set<LinkDetailPaymentToAdvancePayment> linkedDetailPaymentList = payment.getLinkDetailPaymentsToAdvancePayment();
                        for (LinkDetailPaymentToAdvancePayment ldprow : linkedDetailPaymentList) {
                            // Delete Forex Gains/Loss JEs 
                            if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                                result = accJournalEntryobj.deleteJournalEntry(ldprow.getLinkedGainLossJE(), companyid);
                            }
                            /**
                             * Setting isdelete=true flag of revaluation JE's
                             * when receipt is temporary deleted.ERP-42090.
                             */
                            if (!StringUtil.isNullOrEmpty(ldprow.getRevalJeId())) {
                                accJournalEntryobj.deleteJournalEntry(ldprow.getRevalJeId(), companyid);
                            }
                            if (!StringUtil.isNullOrEmpty(ldprow.getRevalJeIdReceipt())) {
                                accJournalEntryobj.deleteJournalEntry(ldprow.getRevalJeIdReceipt(), companyid);
                            }
                        }
                    }


                    if (payment.getImportServiceJE() != null) {
                        result = accJournalEntryobj.deleteJournalEntry(payment.getImportServiceJE().getID(), companyid);
                    }
                    if (payment != null && payment.getRevalJeId() != null) {
                        result = accJournalEntryobj.deleteJournalEntry(payment.getRevalJeId(), companyid);
                    }

                    if (payment != null && payment.getPayDetail() != null && payment.getPayDetail().getCheque() != null) {
                        accPaymentDAOobj.deleteCheque(payment.getPayDetail().getCheque().getID(), companyid);
                    }

                    // delete Bank charges JE
                    result = accVendorPaymentobj.getBankChargeJEFromPayment(paymentid);
                    List list1 = result.getEntityList();
                    Iterator itr1 = list1.iterator();
                    while (itr1.hasNext()) {
                        String jeId = (String) itr1.next();
                        result = accJournalEntryobj.deleteJournalEntry(jeId, companyid);
                        //Delete entry from optimized table
                        accJournalEntryobj.deleteAccountJEs_optimized(jeId);
                    }

                    // delete Bank Interest JE
                    result = accVendorPaymentobj.getBankInterestJEFromPayment(paymentid);
                    List list2 = result.getEntityList();
                    Iterator itr2 = list2.iterator();
                    while (itr2.hasNext()) {
                        String jeId = (String) itr2.next();
                        result = accJournalEntryobj.deleteJournalEntry(jeId, companyid);
                        //Delete entry from optimized table
                        accJournalEntryobj.deleteAccountJEs_optimized(jeId);
                    }
                    //Delete Rouding JEs if created against PI
                    if (!grIDSet.isEmpty()) {
                        String piIDs = "";
                        for (String piID : grIDSet) {
                            piIDs = piID + ",";
                        }
                        if (!StringUtil.isNullOrEmpty(piIDs)) {
                            piIDs = piIDs.substring(0, piIDs.length() - 1);
                        }
                        KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(piIDs, companyid);
                        List<JournalEntry> jeList = jeResult.getEntityList();
                        for (JournalEntry roundingJE : jeList) {
                            roundingJENo = roundingJE.getEntryNumber() + ",";
                            roundingJEIds = roundingJE.getID() + ",";
                            deleteJEArray(roundingJE.getID(), companyid);
                        }
                        if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                            roundingJENo = roundingJENo.substring(0, roundingJENo.length() - 1);
                        }
                        if (!StringUtil.isNullOrEmpty(roundingJEIds)) {
                            roundingJEIds = roundingJEIds.substring(0, roundingJEIds.length() - 1);
                        }
                    }
                }
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("oldjeid", jeid);
                requestParams.put("companyId", companyid);
                requestParams.put("reqHeader", paramJobj.optString(Constants.reqHeader));
                requestParams.put("remoteAddress", paramJobj.optString(Constants.remoteIPAddress));
                deleteBankReconcilation(requestParams);
                StringBuffer journalEntryMsg = new StringBuffer();
                if (!StringUtil.isNullOrEmpty(entryno)) {
                    if (isVatSalesJE || isVatPurchaseJE) {
                        journalEntryMsg.append(" along with the VAT Payment JE No. " + entryno);
                    } else {
                        journalEntryMsg.append(" along with the JE No. " + entryno);
                    }
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("rejectPayment",null))) {
                    String userid = paramJobj.optString(Constants.useridKey);
                    String userName = paramJobj.optString(Constants.userfullname);
                    double amount = StringUtil.isNullOrEmpty(paramJobj.optString("amount",null)) ? 0 : Double.parseDouble(paramJobj.optString("amount","0"));
                    requestParams.clear();
                    requestParams.put("userid", userid);
                    requestParams.put("userName", userName);
                    requestParams.put("amount", amount);
                    requestParams.put("companyid", companyid);
                    boolean isRejected = rejectPendingMakePayment(requestParams, jArr);
                    paramJobj.put("isRejected", isRejected);
                } else {
                    Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                    auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                    auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                    auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
                    auditTrailObj.insertAuditLog(AuditAction.MAKE_PAYMENT, "User " + paramJobj.optString(Constants.userfullname) + " has deleted a Payment " + paymentno + journalEntryMsg.toString(), auditRequestParams, paymentid);
                    if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                        auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_DELETED, "User " + paramJobj.optString(Constants.userfullname) + " has deleted a Payment " + paymentno + ". So Rounding JE No. " + roundingJENo + " deleted.", auditRequestParams, roundingJEIds);
                    }
                }
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
        } catch (AccountingException ex) {
            throw new AccountingException(ex.getMessage());
        }
        return unableToDeletePaymentNos;
    }

    public boolean rejectPendingMakePayment(Map<String, Object> requestParams, JSONArray jArr) throws ServiceException {
        boolean isRejected = false;
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String currentUser = (String) requestParams.get("userid");
            String userFullName = (String) requestParams.get("userName");
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), currentUser);
            User user = (User) userResult.getEntityList().get(0);

            String actionMsg = "rejected";
            int level = 0;
            double amount = (!StringUtil.isNullObject(requestParams.get("amount")) && StringUtil.isNullOrEmpty(requestParams.get("amount").toString())) ? 0 : authHandler.round(Double.parseDouble(requestParams.get("amount").toString()), companyid);
            for (int i = 0; i < jArr.length(); i++) {
                boolean hasAuthorityToReject = false;
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String invid = StringUtil.DecodeText(jobj.optString("billid"));
                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(Payment.class.getName(), invid);
                    Payment payment = (Payment) cap.getEntityList().get(0);
                    HashMap<String, Object> invApproveMap = new HashMap<String, Object>();
                    level = payment.getApprovestatuslevel();
                    invApproveMap.put("companyid", companyid);
                    invApproveMap.put("level", level);
                    invApproveMap.put("totalAmount", String.valueOf(amount));
                    invApproveMap.put("currentUser", currentUser);
                    invApproveMap.put("fromCreate", false);
                    invApproveMap.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                    if (AccountingManager.isCompanyAdmin(user)) {
                        hasAuthorityToReject = true;
                    } else {
                        hasAuthorityToReject = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(invApproveMap);
                    }
                    if (hasAuthorityToReject) {
                        accPaymentDAOobj.rejectPendingmakePayment(payment.getID(), companyid);
                        isRejected = true;
                        // Maintain Approval History of Rejected Record
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("transtype", AuditAction.MAKE_PAYMENT_REJECTED);
                        hashMap.put("transid", payment.getID());
                        hashMap.put("approvallevel", Math.abs(payment.getApprovestatuslevel()));//  If approvedLevel = 11 then its final Approval
                        hashMap.put("remark", "");
                        hashMap.put("userid", currentUser);
                        hashMap.put("companyid", companyid);
                        hashMap.put("isrejected", true);
                        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                        auditTrailObj.insertAuditLog(AuditAction.MAKE_PAYMENT_REJECTED, "User " + userFullName + " " + actionMsg + " Make Payment " + payment.getPaymentNumber(), requestParams, payment.getID());
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        }
        return isRejected;
    }
    public JSONObject getReceiptJSON(Receipt receipt, Map<String, Object> requestParams) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject receiptObj = new JSONObject();
        String receiptNumber = "";
        if (requestParams.containsKey("receiptNumber") && requestParams.get("receiptNumber") != null) {
            receiptNumber = (String) requestParams.get("receiptNumber");
        }
        String vendor = "";
        if (requestParams.containsKey("vendor") && requestParams.get("vendor") != null) {
            vendor = (String) requestParams.get("vendor");
        }
        DateFormat df = authHandler.getDateOnlyFormat();
        receiptObj.put("billid", receipt.getID());
        receiptObj.put("companyid", receipt.getCompany().getCompanyID());
        receiptObj.put("withoutinventory", "");
        receiptObj.put("transactionNo", receiptNumber);
//                        receiptObj.put("date", (receipt.isIsOpeningBalenceReceipt()) ? (df.format(receipt.getCreationDate())) : df.format(receipt.getJournalEntry().getEntryDate()));
        receiptObj.put("date", df.format(receipt.getCreationDate()));
//                        receiptObj.put("linkingdate", (receipt.isIsOpeningBalenceReceipt()) ? (df.format(receipt.getCreationDate())) : df.format(receipt.getJournalEntry().getEntryDate()));
        receiptObj.put("linkingdate", df.format(receipt.getCreationDate()));
        receiptObj.put("journalEntryNo", (receipt.isIsOpeningBalenceReceipt()) ? "" : receipt.getJournalEntry().getEntryNumber());  //journal entry no
        receiptObj.put("mergedCategoryData", "Receipt For Refund");  //type of data
        if (receipt.getVendor() != null) {
            KwlReturnObject objItr2 = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
            Vendor vendor1 = (Vendor) objItr2.getEntityList().get(0);
            receiptObj.put("personname", vendor1.getName());
            receiptObj.put("personid", vendor1.getID());

        }
        String jeNumber = receipt.getJournalEntry().getEntryNumber();
        String jeIds = receipt.getJournalEntry().getID();
        String jeIdEntryDate = df.format(receipt.getJournalEntry().getEntryDate());
        receiptObj.put("isOpeningBalanceTransaction", receipt.isIsOpeningBalenceReceipt());
        receiptObj.put("isNormalTransaction", receipt.isNormalReceipt());
        receiptObj.put("companyid", receipt.getCompany().getCompanyID());
        receiptObj.put("companyname", receipt.getCompany().getCompanyName());
        receiptObj.put("entryno", jeNumber);
        receiptObj.put("journalentryid", jeIds);
        receiptObj.put("journalentrydate", jeIdEntryDate);
        receiptObj.put("billno", receipt.getReceiptNumber());
        receiptObj.put("isadvancepayment", receipt.isIsadvancepayment());
        receiptObj.put("isadvancefromvendor", receipt.isIsadvancefromvendor());
        receiptObj.put("ismanydbcr", receipt.isIsmanydbcr());
        receiptObj.put("isprinted", receipt.isPrinted());
        receiptObj.put("isDishonouredCheque", receipt.isIsDishonouredCheque());
        receiptObj.put("bankCharges", receipt.getBankChargesAmount());
        receiptObj.put("bankChargesCmb", receipt.getBankChargesAccount() != null ? receipt.getBankChargesAccount().getID() : "");
        receiptObj.put("bankInterest", receipt.getBankInterestAmount());
        receiptObj.put("bankInterestCmb", receipt.getBankInterestAccount() != null ? receipt.getBankInterestAccount().getID() : "");
        receiptObj.put("bankChargesAccCode", receipt.getBankChargesAccount() != null ? receipt.getBankChargesAccount().getAcccode() != null ? receipt.getBankChargesAccount().getAcccode() : "" : "");
        receiptObj.put("bankChargesAccName", receipt.getBankChargesAccount() != null ? receipt.getBankChargesAccount().getName() != null ? receipt.getBankChargesAccount().getName() : "" : "");
        receiptObj.put("bankInterestAccCode", receipt.getBankInterestAccount() != null ? receipt.getBankInterestAccount().getAcccode() != null ? receipt.getBankInterestAccount().getAcccode() : "" : "");
        receiptObj.put("bankInterestAccName", receipt.getBankInterestAccount() != null ? receipt.getBankInterestAccount().getName() != null ? receipt.getBankInterestAccount().getName() : "" : "");
        receiptObj.put("paidToCmb", receipt.getReceivedFrom() == null ? "" : receipt.getReceivedFrom().getID());
        receiptObj.put("paidto", receipt.getReceivedFrom() != null ? receipt.getReceivedFrom().getValue() : "");  //to show recived from option in grid
        receiptObj.put("paymentwindowtype", receipt.getPaymentWindowType());
        receiptObj.put(Constants.SEQUENCEFORMATID, receipt.getSeqformat() == null ? "" : receipt.getSeqformat().getID());
        return receiptObj;
    }
    
    /**
     * Code is moved from accVendorPaymentControllerNew.
     * @param paramJobj
     * @return
     * @throws ServiceException
     * @throws AccountingException 
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, AccountingException.class})
    public JSONObject rejectPaymentTemporaryJSON(JSONObject paramJobj) throws ServiceException,AccountingException{
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        String userName=paramJobj.optString(Constants.userfullname);
        try {
            String paymentid = "" ;
            JSONArray jArr = new JSONArray(paramJobj.opt("data").toString());
            int level=0;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject paymentJobj = jArr.getJSONObject(i);
                paymentid = StringUtil.DecodeText(paymentJobj.optString("billid"));
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid);
                Payment payment = (Payment) objItr.getEntityList().get(0);
                level=payment.getApprovestatuslevel();
            }
            deletePaymentTemporary(paramJobj);
//            boolean isRejected=(boolean) request.getAttribute("isRejected");
            boolean isRejected=paramJobj.optBoolean("isRejected");
            if (isRejected) {
                msg = messageSource.getMessage("acc.field.makepaymenthasbeenrejectedsuccessfully", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " by " + userName +" at Level " + level + ".";
            } else {
                msg = messageSource.getMessage("acc.vq.notAuthorisedToRejectThisRecord", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " at Level " + level + ".";
            }
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccVendorPaymentModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccVendorPaymentModuleServiceImpl.rejectPaymentTemporaryJSON" + ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccVendorPaymentModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccVendorPaymentModuleServiceImpl.rejectPaymentTemporaryJSON" + ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccVendorPaymentModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE("accVendorPaymentController.rejectPaymentTemporaryJSON" + ex.getMessage(), ex);
            }
        }
        return jobj;
    }
    
    /**
     * Code is moved from accVendorPaymentController
     * @param paramJobj
     * @return
     * @throws ServiceException
     * @throws AccountingException 
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, AccountingException.class})
    public JSONObject deleteOpeningPaymentPermanent(JSONObject paramJobj,String[] paymentid,String[] paymentno) throws ServiceException,AccountingException{
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            deleteOpeningPaymentPermanentnew(paramJobj,paymentid,paymentno);
            issuccess = true;
            msg = messageSource.getMessage("acc.pay.del", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));  //"Payment(s) has been deleted successfully";
        } catch (AccountingException accEx) {
            throw accEx;
        }catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccVendorPaymentModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccVendorPaymentModuleServiceImpl.deleteOpeningPaymentPermanent" + ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccVendorPaymentModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccVendorPaymentModuleServiceImpl.deleteOpeningPaymentPermanent" + ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorPaymentController.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE("accVendorPaymentController.deleteOpeningPaymentPermanent" + ex.getMessage(), ex);
            }
        }
        return jobj;
    }
    
    public void deleteOpeningPaymentPermanentnew(JSONObject paramJobj,String[] paymentid,String[] paymentno) throws AccountingException, SessionExpiredException, ServiceException, JSONException {
        Map<String, Object> requestMap = AccountingManager.getGlobalParamsJson(paramJobj);
        String companyid = paramJobj.optString(Constants.companyKey);
        HashMap<String,Object> reconcileMap = new HashMap<>();
        
        for (int count = 0; count < paymentid.length; count++) {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("paymentid", paymentid[count]);
            requestParams.put("companyid", companyid);
//             requestParams.put("paymentno",paymentno);
            try {
                if (!StringUtil.isNullOrEmpty(paymentid[count])) {
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid[count]);
                    Payment payment = (Payment) objItr.getEntityList().get(0);

                    if (payment != null) {
                        updateOpeningBalance(payment, companyid);
                    }
                    /**
                     * Checking whether the record being deleted is reconciled
                     * if yes showing not allowing user to delete the record and
                     * displaying "You can not delete <document number> as it is
                     * reconciled.If you want to delete, please un-reconcile
                     * it".ERP-33261.
                     */
                    reconcileMap.put("isOpeningDocument", true);
                    reconcileMap.put("transactionId", payment != null ? payment.getID() : "");
                    reconcileMap.put(Constants.companyKey, companyid);
                    boolean isReconciledFlag = accBankReconciliationObj.isRecordReconciled(reconcileMap);
                    if (isReconciledFlag) {
                        String paymentNumber = payment.getPaymentNumber();
                        throw new AccountingException(messageSource.getMessage("acc.reconcilation.Cannotdeletepayment", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " " + "<b>" + paymentNumber + "</b>" + " " + messageSource.getMessage("acc.reconcilation.asitisreconciled", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                    }
                    if (payment.getLinkDetailPayments() != null && !payment.getLinkDetailPayments().isEmpty()) {
                        accVendorPaymentobj.deleteLinkDetailsAndUpdateAmountDue(requestMap,payment.getID(), companyid,false);
                    }
                    if(payment.getLinkDetailPaymentToCreditNote()!=null && !payment.getLinkDetailPaymentToCreditNote().isEmpty()){
                        accVendorPaymentobj.deleteLinkPaymentDetailsToCreditNoteAndUpdateAmountDue(payment.getID(), companyid,false);
                    }
                    
                    //Delete unrealised JE for Opening Payment 
                    accJournalEntryobj.permanentDeleteJournalEntryDetailReval(paymentid[count], companyid);
                    accJournalEntryobj.permanentDeleteJournalEntryReval(paymentid[count], companyid);
                    /**
                     * Deleting Reevaluation History of the receipt being
                     * deleted.
                     */
                    JSONObject historyDeleteParams = new JSONObject();
                    historyDeleteParams.put("documentId", paymentid[count]);
                    historyDeleteParams.put("companyID", companyid);
                    accJournalEntryobj.deleteRevaluationHistory(historyDeleteParams);
                    accVendorPaymentobj.deletePaymentPermanent(requestParams);
                    requestParams.put("transactionID", paymentid[count]);
                    deleteBankReconcilationOfOpeningBalances(requestParams);
                    Map<String, Object> auditParamsMap = new HashMap();
                    auditParamsMap.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
                    auditParamsMap.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
                    auditParamsMap.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                    auditParamsMap.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
//                accVendorPaymentobj.deletePayments(paymentid, companyid);
                    auditTrailObj.insertAuditLog(AuditAction.OPENING_BALANCE_DELETED, "User " + paramJobj.optString(Constants.userfullname) + " has an deleted Opening Balance Payment Permanently " + paymentno[count], auditParamsMap, paymentid[count]);
                }

            }catch (ServiceException | JSONException ex) {
                throw new AccountingException(messageSource.getMessage("acc.pay1.excp1", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
            }
        }
    }
    
    private void deleteBankReconcilationOfOpeningBalances(Map<String, Object> requestParams) throws ServiceException {
        if (requestParams.containsKey("transactionID")) {
            String reconsilationID = "";
            String unReconsilationID = "";
            String transactionID = (String) requestParams.get("transactionID");
            String companyid = (String) requestParams.get("companyId");

            //Deleting  BankReconciliationDetail
            KwlReturnObject reconsiledDetails = accBankReconciliationObj.getBRWithoutJE(transactionID, companyid, Constants.Acc_Make_Payment_ModuleId);
            if (reconsiledDetails.getRecordTotalCount() > 0) {
                List<BankReconciliationDetail> brd = reconsiledDetails.getEntityList();
                for (BankReconciliationDetail reconciliation : brd) {
                    accBankReconciliationObj.permenantDeleteBankReconciliationDetail(reconciliation.getID(), companyid);
                    reconsilationID = reconciliation.getBankReconciliation().getID();
                }
            }

            //Deleting  BankUnreconciliationDetail
            KwlReturnObject unReconsiledDetails = accBankReconciliationObj.getBankUnReconsiledWithoutJE(transactionID, companyid, Constants.Acc_Make_Payment_ModuleId);
            if (unReconsiledDetails.getRecordTotalCount() > 0) {
                List<BankUnreconciliationDetail> brd = unReconsiledDetails.getEntityList();
                for (BankUnreconciliationDetail reconciliation : brd) {
                    accBankReconciliationObj.permenantDeleteBankUnReconciliationDetail(reconciliation.getID(), companyid);
                    unReconsilationID = reconciliation.getBankReconciliation().getID();
                }
            }
            if (!StringUtil.isNullOrEmpty(reconsilationID)) {
                accBankReconciliationObj.deleteBankReconciliation(reconsilationID, companyid);
            }
            if (!StringUtil.isNullOrEmpty(unReconsilationID)) {
                accBankReconciliationObj.deleteBankReconciliation(unReconsilationID, companyid);
            }
        }
    }
}
