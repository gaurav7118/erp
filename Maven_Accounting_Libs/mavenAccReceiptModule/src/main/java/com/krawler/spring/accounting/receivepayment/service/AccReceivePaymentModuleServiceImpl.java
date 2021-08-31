/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.receivepayment.service;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.CsvReader;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.invoice.service.AccInvoiceModuleService;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.receipt.accReceiptController;
import com.krawler.spring.accounting.receipt.accReceiptControllerNew;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DecimalFormat;
import com.krawler.spring.importFunctionality.ImportHandler;
import java.text.SimpleDateFormat;
import java.util.*;
import com.krawler.spring.accounting.salesorder.accSalesOrderService;
import java.util.logging.Level;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import java.util.logging.Logger;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import org.springframework.context.MessageSource;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import org.springframework.context.MessageSourceAware;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import com.krawler.spring.importFunctionality.ImportDAO;
import org.springframework.transaction.TransactionException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.CommonFnControllerService;
import com.krawler.spring.common.kwlCommonTablesDAO;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class AccReceivePaymentModuleServiceImpl implements AccReceivePaymentModuleService, MessageSourceAware {

    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accReceiptDAO accReceiptDAOobj;
    private MessageSource messageSource;
    private accCurrencyDAO accCurrencyobj;
    private ImportDAO importDao;
    public accCustomerDAO accCustomerDAOObj;
    private accBankReconciliationDAO accBankReconciliationObj;
    private HibernateTransactionManager txnManager;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private fieldDataManager fieldDataManagercntrl;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accPaymentDAO accPaymentDAOobj;
    public ImportHandler importHandler;
    private accVendorDAO accVendorDAOObj;
    private accSalesOrderService accSalesOrderServiceobj;
    private accInvoiceDAO accInvoiceDAOObj;
    private accJournalEntryDAO accJournalEntryobj;
    private accAccountDAO accAccountDAOobj;
    private auditTrailDAO auditTrailObj;
    private AccReceivePaymentModuleServiceImpl.EnglishNumberToWords EnglishNumberToWordsOjb = new AccReceivePaymentModuleServiceImpl.EnglishNumberToWords();
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private AccJournalEntryModuleService journalEntryModuleServiceobj;
    private authHandlerDAO authHandlerDAOObj;
    private accMasterItemsDAO accMasterItemsDAOObj;
    private fieldManagerDAO fieldManagerDAOobj;
    private AccInvoiceModuleService accInvoiceModuleService;
    private CommonFnControllerService commonFnControllerService;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    public void setAccMasterItemsDAOObj(accMasterItemsDAO accMasterItemsDAOObj) {
        this.accMasterItemsDAOObj = accMasterItemsDAOObj;
    }

    public authHandlerDAO getAuthHandlerDAOObj() {
        return authHandlerDAOObj;
    }

    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }

    public AccJournalEntryModuleService getJournalEntryModuleServiceobj() {
        return journalEntryModuleServiceobj;
    }

    public void setJournalEntryModuleServiceobj(AccJournalEntryModuleService journalEntryModuleServiceobj) {
        this.journalEntryModuleServiceobj = journalEntryModuleServiceobj;
    }
  
        
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setAccCustomerDAO(accCustomerDAO accCustomerDAOObj) {
        this.accCustomerDAOObj = accCustomerDAOObj;
    }

    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }

    public void setAccVendorDAO(accVendorDAO accVendorDAOObj) {
        this.accVendorDAOObj = accVendorDAOObj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
    }
    public void setAccSalesOrderServiceobj(accSalesOrderService accSalesOrderServiceobj) {
        this.accSalesOrderServiceobj = accSalesOrderServiceobj;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyobj) {
        this.accCurrencyobj = accCurrencyobj;
    }

    public void setAccInvoiceDAO(accInvoiceDAO accInvoiceDAOObj) {
        this.accInvoiceDAOObj = accInvoiceDAOObj;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setAccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAOObj;
    }
    
    public void setAccInvoiceModuleService(AccInvoiceModuleService accInvoiceModuleService) {
        this.accInvoiceModuleService = accInvoiceModuleService;
    }

    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
    
    public void setCommonFnControllerService(CommonFnControllerService commonFnControllerService) {
        this.commonFnControllerService = commonFnControllerService;
    }
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    @Override
    public HashMap<String, Object> saveCustomerReceipt(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        boolean isChequePrint = false;
        String msg = "";
        String paymentid = "";
        String billno = "";
        String advanceamount = "";
        KwlReturnObject result = null;
        boolean isEdit = false;
        int receipttype = -1;
        String AuditMsg = "", custVenEmailId = "";
        Map<String, Object> oldReceiptPrmt = new HashMap<String, Object>();
        Map<String, Object> NewReceiptPrmt = new HashMap<String, Object>();
        Map<String, Object> newAuditKey = new HashMap<String, Object>();
        Map<String, Object> seqNumMap = new HashMap<String, Object>();
        String amountpayment = "", accountaddress = "", accountName = "", JENumBer = "";
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        String jeEntryNo = "";
        String bankChargesAccid = "", bankInterestAccid = "";
        TransactionStatus status = null;
        JSONArray jSONArrayAgainstInvoice = new JSONArray();
        String companyid = "";
        Company company=null;
        int editCount = 0;
        boolean accexception = false;
        String entryNumber = "";
        boolean exceptionChequeOrInv = false;
        try {
            entryNumber = paramJobj.optString("no");

            if (!paramJobj.has(Constants.sequenceformat) || StringUtil.isNullOrEmpty(paramJobj.optString(Constants.sequenceformat, null))) {
                String sequenceformatid = null;
                Map<String, Object> sfrequestParams = new HashMap<String, Object>();
                sfrequestParams.put(Constants.companyKey, paramJobj.get(Constants.companyKey));
                sfrequestParams.put("modulename", SequenceFormat.RECEIVE_PAYMENT_MODULENAME);
                KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
                List<SequenceFormat> ll = seqFormatResult.getEntityList();
                if (ll.size()>0) {
                    SequenceFormat format = (SequenceFormat) ll.get(0);
                    sequenceformatid = format.getID();
                    paramJobj.put(Constants.sequenceformat, sequenceformatid);
                } else if(!StringUtil.isNullOrEmpty(entryNumber)){
                    paramJobj.put(Constants.sequenceformat, "NA");
                }
            }//end of sequenceformat
            
            if (StringUtil.isNullOrEmpty(paramJobj.optString(Constants.sequenceformat, null))) {
                JSONObject response = StringUtil.getErrorResponse("acc.common.erp33", paramJobj, "Sequence Format Details are missing.", messageSource);
                throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
            }
            
            String sequenceformat = paramJobj.getString(Constants.sequenceformat) != null ? paramJobj.getString(Constants.sequenceformat) : "NA";
            companyid = paramJobj.optString(Constants.companyKey);
            Receipt editReceiptObject = null;
            String receiptid = paramJobj.optString(Constants.billid);
            double balaceAmount = 0.0;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("balaceAmount",null))) {
                balaceAmount = Double.parseDouble(paramJobj.optString("balaceAmount"));
            }
            if (!StringUtil.isNullOrEmpty(receiptid)) {
                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                editReceiptObject = (Receipt) receiptObj.getEntityList().get(0);
                isEdit = editReceiptObject != null ? true : false;
            }
            String detailsJsonString = paramJobj.optString("Details");;
            JSONArray jSONArray = new JSONArray(detailsJsonString);
            JSONArray jSONArrayAdvance = new JSONArray();
            JSONArray jSONArrayLocalAdvance = new JSONArray();
            JSONArray jSONArrayExportAdvance = new JSONArray();
            JSONArray jSONArrayBal = new JSONArray();

            JSONArray jSONArrayCNDN = new JSONArray();
            JSONArray jSONArrayGL = new JSONArray();
            JSONArray jSONArrayLoanDisbursement = new JSONArray();

            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
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
            boolean isReceiptDateAfterTheDocumentDate=true;
            JSONObject isReceiptDateAfterTheDocumentDateParams=new JSONObject();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                if (jSONObject.has("type") && jSONObject.optInt("type", 0) != 0 && jSONObject.has("enteramount") && jSONObject.optDouble("enteramount", 0.0) != 0) {
                    if (jSONObject.getInt("type") == Constants.AdvancePayment) {
                        jSONArrayAdvance.put(jSONObject);
                    } else if (jSONObject.getInt("type") == Constants.PaymentAgainstInvoice) {
                        isReceiptDateAfterTheDocumentDateParams.put("documentDate", !StringUtil.isNullOrEmpty(jSONObject.optString("invoicecreationdate")) ? jSONObject.optString("invoicecreationdate") : jSONObject.optString("date"));	//ERP-36350
                        isReceiptDateAfterTheDocumentDateParams.put("receiptDate", paramJobj.opt("creationdate"));
                        isReceiptDateAfterTheDocumentDate = isReceiptDateAfterTheDocumentDate(isReceiptDateAfterTheDocumentDateParams);
                        jSONArrayAgainstInvoice.put(jSONObject);
                    } else if (jSONObject.getInt("type") == Constants.PaymentAgainstCNDN) {
                        isReceiptDateAfterTheDocumentDateParams.put("documentDate", StringUtil.DecodeText(jSONObject.optString("date")));
                        isReceiptDateAfterTheDocumentDateParams.put("receiptDate", paramJobj.opt("creationdate"));
                        isReceiptDateAfterTheDocumentDate = isReceiptDateAfterTheDocumentDate(isReceiptDateAfterTheDocumentDateParams);
                        jSONArrayCNDN.put(jSONObject);
                    } else if (jSONObject.getInt("type") == Constants.GLPayment) {
                        jSONArrayGL.put(jSONObject);
                    } else if (jSONObject.getInt("type") == Constants.LocalAdvanceTypePayment) {
                        jSONArrayLocalAdvance.put(jSONObject);
                    } else if (jSONObject.getInt("type") == Constants.ExportAdvanceTypePayment) {
                        jSONArrayExportAdvance.put(jSONObject);
                    } else if (jSONObject.getInt("type") == Constants.PaymentAgainstLoanDisbursement) {
                        jSONArrayLoanDisbursement.put(jSONObject);
                    }
                }
                if (isDiscountAppliedOnPaymentTerms && jSONObject.getInt("type") == Constants.PaymentAgainstInvoice && jSONObject.optDouble("discountname", 0.0) > 0.0 && jSONObject.optDouble("enteramount", 0.0) == 0.0) {
                    jSONArrayAgainstInvoice.put(jSONObject);
                }
            }
            
            if (!isReceiptDateAfterTheDocumentDate) {
                throw new AccountingException(messageSource.getMessage("acc.mp.dateCanNotBeOlder", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
            }

            try {
                if (editReceiptObject != null) {//In edit case checks duplicate number
                    result = accReceiptDAOobj.getReceiptEditCount(entryNumber, companyid, receiptid);
                    editCount = result.getRecordTotalCount();
                    if (editCount > 0 && sequenceformat.equals("NA")) {
                        accexception = true;
                        throw new AccountingException(messageSource.getMessage("acc.payment.receivepaymentno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                } else {//In add case check duplicate number
                    result = accReceiptDAOobj.getReceiptFromBillNo(entryNumber, companyid);
                    if (result.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                        accexception = true;
                        throw new AccountingException(messageSource.getMessage("acc.payment.receivepaymentno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                    //Check Deactivate Tax in New Transaction.
                    JSONObject taxJObj = new JSONObject();
                    taxJObj.put(Constants.detail, jSONArrayGL);
                    taxJObj.put(Constants.companyKey, companyid);
                    if (!fieldDataManagercntrl.isTaxActivated(taxJObj)) {
                        throw ServiceException.FAILURE(messageSource.getMessage("acc.tax.deactivated.tax.saveAlert", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                    }
                }
                KwlReturnObject cmpny = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                company = (Company) cmpny.getEntityList().get(0);
                if (company.getCountry().getID().equals(String.valueOf(Constants.indian_country_id))) {
                    if ((!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA"))) {
                        boolean seqformat_oldflg = StringUtil.getBoolean(paramJobj.optString("seqformat_oldflag"));
                        SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getDateOnlyFormat();
                        Date billDate = formatter.parse(paramJobj.optString("creationdate"));
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
                    KwlReturnObject resultPay = accCommonTablesDAO.getTransactionInTemp(entryNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);
                    if (resultPay.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                        accexception = true;
                        throw new AccountingException(messageSource.getMessage("acc.payment.selectedReceivePamentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    } else {
                        accCommonTablesDAO.insertTransactionInTemp(entryNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);
                    }

                    for (int i = 0; i < jSONArrayAgainstInvoice.length(); i++) {
                        JSONObject invoiceJobj = jSONArrayAgainstInvoice.getJSONObject(i);
                        String invoiceId = invoiceJobj.getString("documentid");
                        KwlReturnObject resultInv = accPaymentDAOobj.getInvoiceInTemp(invoiceId, companyid, Constants.Acc_Invoice_ModuleId);
                        if (resultInv.getRecordTotalCount() > 0) {
                            throw new AccountingException("Selected invoice is already in process, please try after sometime.");
                        } else {
                            accPaymentDAOobj.insertInvoiceOrCheque(invoiceId, companyid, Constants.Acc_Invoice_ModuleId, "");  //store invoice ids in temporary table.If maultiple user make a payment at same time then it throw an exception. 
                        }
                    }
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
                    JSONObject temp = jSONArrayAgainstInvoice.getJSONObject(i);
                    KwlReturnObject resultInvoice = accountingHandlerDAOobj.getObject(Invoice.class.getName(), temp.getString("documentid"));
                    Invoice invoice = (Invoice) resultInvoice.getEntityList().get(0);
                    boolean isInvoiceIsClaimed = (invoice.getBadDebtType() == Constants.Invoice_Claimed || invoice.getBadDebtType() == Constants.Invoice_Recovered);
                    double invoiceAmountDue = 0;
                    double invoiceAmountDueAfterDiscount = 0;
                    if (isInvoiceIsClaimed) {
                        invoiceAmountDue = invoice.getClaimAmountDue();
                    } else {
                        if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                            invoiceAmountDue = invoice.getOpeningBalanceAmountDue();
                        } else {
                            invoiceAmountDue = invoice.getInvoiceamountdue();
                            invoiceAmountDueAfterDiscount=temp.optDouble("amountdueafterdiscount", 0.0);
                        }
                    }
                    double amountEntered = temp.getDouble("enteramount");
                    double adjustedRate = 1;
                    if (!StringUtil.isNullOrEmpty(temp.optString("exchangeratefortransaction", "").toString())) {
                        adjustedRate = Double.parseDouble(temp.get("exchangeratefortransaction").toString());
                    }
                    double paymentAmountReceivedTemp = invoiceAmountDue * adjustedRate;
                    paymentAmountReceivedTemp = authHandler.round(paymentAmountReceivedTemp, companyid);
                    boolean isFullPayment = paymentAmountReceivedTemp == amountEntered;
                    double discountAmount = temp.optDouble("discountname", 0.0);
                    discountAmount = authHandler.round(discountAmount, companyid);
                    if (isDiscountAppliedOnPaymentTerms && discountAmount > 0.0) {
                        isFullPayment = paymentAmountReceivedTemp == (amountEntered + discountAmount);
                    }
                    if (!isFullPayment) {
                        double amountReceivedConverted = temp.getDouble("enteramount");
                        if (!StringUtil.isNullOrEmpty(temp.optString("exchangeratefortransaction", "").toString())) {
                            adjustedRate = Double.parseDouble(temp.get("exchangeratefortransaction").toString());
                            amountReceivedConverted = amountEntered / adjustedRate;
                            amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                        }
                        invoiceAmountDue = authHandler.round(invoiceAmountDue, companyid);
                        if (invoiceAmountDue < amountReceivedConverted) {
                            throw new AccountingException("Amount entered for invoice cannot be greater than it's amount due. Please reload the invoices.");
                        }
                    }
                }
            }

            if (isEdit) {
                setValuesForAuditTrialMessage(editReceiptObject, oldReceiptPrmt, newAuditKey);
            }            
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("creationdate"))) {
                Date billDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("creationdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, paramJobj, billDate);
            }
            
            JournalEntry oldBankChargeJE = editReceiptObject != null ? editReceiptObject.getJournalEntryForBankCharges() != null ? editReceiptObject.getJournalEntryForBankCharges() : null : null;
            JournalEntry oldBankInterestJE = editReceiptObject != null ? editReceiptObject.getJournalEntryForBankInterest() != null ? editReceiptObject.getJournalEntryForBankInterest() : null : null;
            if (editReceiptObject != null) {
                editReceiptObject.setBankChargesAccount(null);
                editReceiptObject.setBankChargesAmount(0);
                editReceiptObject.setBankInterestAccount(null);
                editReceiptObject.setBankInterestAmount(0);
            }
            Receipt receipt = createReceiptObject(paramJobj, editReceiptObject);
            int beforeEditApprovalStatus=editReceiptObject!=null?editReceiptObject.getApprovestatuslevel():0;         //getting current approval status before updating
            
            int approvalStatusLevel = 11;
            Double totalAmount = Double.parseDouble(paramJobj.optString("amount"));
            HashMap<String, Object> receiveApproveMap = new HashMap<String, Object>();
            List approvedlevel = null;
            receiveApproveMap.put(Constants.companyKey, companyid);
            receiveApproveMap.put("level", 0);//Initialy it will be 0
            receiveApproveMap.put("totalAmount", String.valueOf(authHandler.round(totalAmount, companyid)));
            receiveApproveMap.put("currentUser", receipt.getCreatedby());
            receiveApproveMap.put("fromCreate", true);
            receiveApproveMap.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
            boolean isMailApplicable = false;
            List list = new ArrayList();
            list.add(receipt.getID());
            approvedlevel = approveReceivePayment(list, receiveApproveMap, isMailApplicable);           //makes approval status to 1
            approvalStatusLevel = (Integer) approvedlevel.get(0);
            receipt.setApprovestatuslevel(approvalStatusLevel);

            if (receipt != null && isEdit) {
                NewReceiptPrmt.put(Constants.ReceiptNumber, receipt.getReceiptNumber());
                NewReceiptPrmt.put(Constants.ReceivedFrom, receipt.getReceivedFrom() != null ? receipt.getReceivedFrom().getValue() : "");
                NewReceiptPrmt.put(Constants.CreationDate, receipt.getCreationDate());
                NewReceiptPrmt.put(Constants.Memo, receipt.getMemo());
                if (receipt.getPayDetail() != null && receipt.getPayDetail() != null) {
                    int oldPaymentMethodType = receipt.getPayDetail().getPaymentMethod().getDetailType();
                    String oldPaymentMethodTypeName = receipt.getPayDetail().getPaymentMethod().getMethodName();
                    NewReceiptPrmt.put(Constants.PaymentMethodType, oldPaymentMethodTypeName);
                    if (oldPaymentMethodType == PaymentMethod.TYPE_BANK) {
                        Cheque oldCheck = receipt.getPayDetail().getCheque();
                        NewReceiptPrmt.put(Constants.Cheque, oldCheck);
                        NewReceiptPrmt.put(Constants.ChequeNumber, oldCheck.getChequeNo());
                        NewReceiptPrmt.put(Constants.BankName, oldCheck.getBankName());
                        NewReceiptPrmt.put(Constants.CheckDate, oldCheck.getDueDate());
                    } else if (oldPaymentMethodType == PaymentMethod.TYPE_CARD) {
                        NewReceiptPrmt.put(Constants.Card, receipt.getPayDetail().getCard());
                    }
                }

                /*
                 * Deleting Linking information of Receive Payment while editing
                 * Receive Payment
                 */
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("receiptid", receipt.getID());
                if (paramJobj.optBoolean(Constants.isMultiGroupCompanyFlag) == false) {
                    accReceiptDAOobj.deleteLinkingInformationOfRP(requestParams);
                } else {
                    deleteLinkingInfo(requestParams);
                }

                AuditMsg = AccountingManager.BuildAuditTrialMessage(NewReceiptPrmt, oldReceiptPrmt, Constants.Acc_Receive_Payment_ModuleId, newAuditKey);
            }
            /*
             * For Malaysian country, while receiving advance payment from
             * customer, GST may or may not be added. This causes problem in
             * edit case that is why we have set tax and tax amount null
             * initially. ERP-9810
             */

            receipt.setTax(null);
            receipt.setTaxAmount(0);
            JournalEntry journalEntry = journalEntryObject(paramJobj, editReceiptObject, receipt.getID());
            JournalEntry oldJE = editReceiptObject != null ? editReceiptObject.getJournalEntry() : null;
            String oldReevalJE = editReceiptObject != null ? editReceiptObject.getRevalJeId() : null;
            PayDetail oldPayDetail = editReceiptObject != null ? editReceiptObject.getPayDetail() : null;
            if (oldJE != null) {
                paramJobj.put("oldjeid", oldJE.getID());
            }
            
            if (approvalStatusLevel == Constants.APPROVED_STATUS_LEVEL) {
                journalEntry.setPendingapproval(0);
            } else {
                journalEntry.setPendingapproval(1);
            }
            
            receipt.setJournalEntry(journalEntry);
            List<Receipt> receipts = new ArrayList<Receipt>();
            receipts.add(receipt);
            accJournalEntryobj.saveJournalEntryByObject(journalEntry);

            if (!StringUtil.isNullOrEmpty(paramJobj.optString((Constants.customfield)))) {
                JSONArray jcustomarray = new JSONArray(paramJobj.optString(Constants.customfield));
                if (paramJobj.optBoolean(Constants.isSquatTransaction)) {
                    jcustomarray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_Receive_Payment_ModuleId, companyid, true);
                }
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    KwlReturnObject receiptAccJECustomData = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), journalEntry.getID());
                    AccJECustomData accJECustomData = (AccJECustomData) receiptAccJECustomData.getEntityList().get(0);
                    journalEntry.setAccBillInvCustomData(accJECustomData);
                }
            }
            Set<JournalEntryDetail> details = null;
            accReceiptDAOobj.saveReceiptObject(receipts);

            String receiptID = receipts.get(0).getID();
            /**
             * Save GST History Customer/Vendor data
             */
            if (receipt.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                paramJobj.put("docid", receiptID);
                paramJobj.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                fieldDataManagercntrl.createRequestMapToSaveDocHistory(paramJobj);
            }
            // Save line level details
            //Deleting previous payments
            Set<ReceiptAdvanceDetail> advanceDetails = receipt.getReceiptAdvanceDetails();
            if (receipt.getReceiptAdvanceDetails() != null && !advanceDetails.isEmpty()) {
                updateReceiptAdvancePaymentAmountDue(receipt, companyid,beforeEditApprovalStatus);
                accReceiptDAOobj.deleteReceiptAdvanceDetails(receipt.getID(), receipt.getCompany().getCompanyID());
                /**
                 * Delete GST tax class history
                 */
                if (receipt.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                    String idstring="";
                    for (ReceiptAdvanceDetail receiptAdvanceDetail : advanceDetails) {
                        idstring+="'"+receiptAdvanceDetail.getId()+"',";
                    }
                    if(idstring.length()>1){
                        idstring=idstring.substring(0, idstring.length()-1);
                        fieldManagerDAOobj.deleteGstTaxClassDetails(idstring);
                    }
                }
            }
            accReceiptDAOobj.deleteReceiptsDetailsAndUpdateAmountDue(receipt.getID(), receipt.getCompany().getCompanyID(),beforeEditApprovalStatus);
            Set<ReceiptDetailOtherwise> receiptDetailOtherwises = receipt.getReceiptDetailOtherwises();
            if (receipt.getReceiptDetailOtherwises() != null && !receiptDetailOtherwises.isEmpty()) {
                accReceiptDAOobj.deleteReceiptDetailsOtherwise(receipt.getID());
            }                   
            //Delete payment against CN/DN
            accReceiptDAOobj.deleteReceiptsAgainstCNDN(receipt.getID(), receipt.getCompany().getCompanyID(),beforeEditApprovalStatus);

            // Delete payment against disbursement
            accReceiptDAOobj.deleteReceiptsDetailsLoanAndUpdateAmountDue(receiptid, companyid);
            /*
             * Deleting the mapping entries posted for recovering the invoices
             */
            if (isEdit) {
                HashMap<String, Object> requestParamsForMapping = new HashMap();
                requestParamsForMapping.put(Constants.companyid, companyid);
                requestParamsForMapping.put("receiptid", receiptid);
                accInvoiceDAOObj.deleteBadDebtInvoiceMapping(requestParamsForMapping);
            }
            paramJobj.put("isDiscountAppliedOnPaymentTerms", isDiscountAppliedOnPaymentTerms);
            paramJobj.put("isEdit", isEdit);        //Passing isEdit flag as it used in journalEntryDetailObject to identify which account discount value will be added.ERM-981
            
            if (jSONArrayAgainstInvoice.length() > 0) {
                if (details == null) {
                    details = journalEntryDetailObject(paramJobj, jSONArrayAgainstInvoice, journalEntry, receipt, Constants.PaymentAgainstInvoice);
                } else {
                    details.addAll(journalEntryDetailObject(paramJobj, jSONArrayAgainstInvoice, journalEntry, receipt, Constants.PaymentAgainstInvoice));
                }
                //Save Invoice Details selected in payment
                HashSet receiptDetails = receiptDetailObject(paramJobj, jSONArrayAgainstInvoice, receipt, Constants.PaymentAgainstInvoice);
                receipt.setRows(receiptDetails);
            }
            // Saving loan payment details and corresponding JE details
            if (jSONArrayLoanDisbursement.length() > 0) {
                if (details == null) {
                    details = journalEntryDetailObject(paramJobj, jSONArrayLoanDisbursement, journalEntry, receipt, Constants.PaymentAgainstLoanDisbursement);
                } else {
                    details.addAll(journalEntryDetailObject(paramJobj, jSONArrayLoanDisbursement, journalEntry, receipt, Constants.PaymentAgainstLoanDisbursement));
                }
                //Save Loan Repayment Details selected in payment
                HashSet receiptDetailsLoan = saveReceiptDetailLoanObject(paramJobj, jSONArrayLoanDisbursement, receipt);
                receipt.setReceiptDetailsLoan(receiptDetailsLoan);
            }

            if (jSONArrayAdvance.length() > 0) {
                if (details == null) {
                    details = journalEntryDetailObject(paramJobj, jSONArrayAdvance, journalEntry, receipt, Constants.AdvancePayment);
                } else {
                    details.addAll(journalEntryDetailObject(paramJobj, jSONArrayAdvance, journalEntry, receipt, Constants.AdvancePayment));
                }

                //Save Advance Details selected in payment
                advanceDetails = advanceDetailObject(paramJobj, jSONArrayAdvance, receipt, Constants.AdvancePayment);
                receipt.setReceiptAdvanceDetails(advanceDetails);
            }
            if (jSONArrayExportAdvance.length() > 0) {
                if (details == null) {
                    details = journalEntryDetailObject(paramJobj, jSONArrayExportAdvance, journalEntry, receipt, Constants.ExportAdvanceTypePayment);
                } else {
                    details.addAll(journalEntryDetailObject(paramJobj, jSONArrayExportAdvance, journalEntry, receipt, Constants.ExportAdvanceTypePayment));
                }

                //Save Advance Details selected in payment
                advanceDetails = advanceDetailObject(paramJobj, jSONArrayExportAdvance, receipt, Constants.ExportAdvanceTypePayment);
                receipt.setReceiptAdvanceDetails(advanceDetails);
            }
            if (jSONArrayLocalAdvance.length() > 0) {
                if (details == null) {
                    details = journalEntryDetailObject(paramJobj, jSONArrayLocalAdvance, journalEntry, receipt, Constants.LocalAdvanceTypePayment);
                } else {
                    details.addAll(journalEntryDetailObject(paramJobj, jSONArrayLocalAdvance, journalEntry, receipt, Constants.LocalAdvanceTypePayment));
                }

                //Save Advance Details selected in payment
                advanceDetails = advanceDetailObject(paramJobj, jSONArrayLocalAdvance, receipt, Constants.LocalAdvanceTypePayment);
                receipt.setReceiptAdvanceDetails(advanceDetails);
            }

            int sequencecounter = 0;
            Map<String, Object> counterMap = new HashMap<>();
            counterMap.put("counter", sequencecounter);
            if (jSONArrayCNDN.length() > 0) {
                if (details == null) {
                    details = journalEntryDetailObject(paramJobj, jSONArrayCNDN, journalEntry, receipt, Constants.PaymentAgainstCNDN);
                } else {
                    details.addAll(journalEntryDetailObject(paramJobj, jSONArrayCNDN, journalEntry, receipt, Constants.PaymentAgainstCNDN));
                }
                //Save CN / DN details selected in payment
                saveCNDNDetailObject(paramJobj, jSONArrayCNDN, receipt, Constants.PaymentAgainstCNDN, counterMap);
            }
            if (jSONArrayGL.length() > 0) {
                if (details == null) {
                    details = journalEntryDetailObject(paramJobj, jSONArrayGL, journalEntry, receipt, Constants.GLPayment);
                } else {
                    details.addAll(journalEntryDetailObject(paramJobj, jSONArrayGL, journalEntry, receipt, Constants.GLPayment));
                }
            }
            /*
             * If Balace Amount is greater than Zero
             */
            if (balaceAmount > 0) {
                if (details == null) {
                    details = journalEntryDetailObject(paramJobj, jSONArrayBal, journalEntry, receipt, Constants.BALACEAMOUNT);
                } else {
                    details.addAll(journalEntryDetailObject(paramJobj, jSONArrayBal, journalEntry, receipt, Constants.BALACEAMOUNT));
                }
            }

            if (editReceiptObject != null) {
                editReceiptObject.setJournalEntryForBankCharges(null);
                editReceiptObject.setJournalEntryForBankInterest(null);
            }
            boolean paymentWithoutBankChargesJe = false;
            boolean paymentWithoutBankInterestJe = false;
            sequencecounter = (Integer) counterMap.get("counter");
            if (details != null) {
                bankChargesAccid = paramJobj.optString("bankChargesCmb");
                if (!StringUtil.isNullOrEmpty(bankChargesAccid)) {      // posting JE for Bank Charges
                    if (editReceiptObject != null) {
                        paymentWithoutBankChargesJe = oldBankChargeJE != null ? false : true;
                    }
                    JournalEntry bankChargesJournalEntry = journalEntryObjectBankCharges(paramJobj, editReceiptObject, sequencecounter, true, paymentWithoutBankChargesJe, oldBankChargeJE, oldBankInterestJE);
                    if (editReceiptObject != null && !StringUtil.isNullOrEmpty(bankChargesJournalEntry.getEntryNumber())) {
                        jeEntryNo += ", " + bankChargesJournalEntry.getEntryNumber();
                    }
                    bankChargesJournalEntry.setTransactionId(receiptID);
                    bankChargesJournalEntry.setTransactionModuleid(Constants.Acc_Receive_Payment_ModuleId);
                    //ERP-18778
                    bankChargesJournalEntry.setIsmulticurrencypaymentje(journalEntry.isIsmulticurrencypaymentje());
                    bankChargesJournalEntry.setPaymentcurrencytopaymentmethodcurrencyrate(journalEntry.getPaymentcurrencytopaymentmethodcurrencyrate());
                    receipt.setJournalEntryForBankCharges(bankChargesJournalEntry);
                    receipts.add(receipt);
                    if (approvalStatusLevel == Constants.APPROVED_STATUS_LEVEL) {
                        bankChargesJournalEntry.setPendingapproval(0);
                    } else {
                        bankChargesJournalEntry.setPendingapproval(1);
                    }
                    accJournalEntryobj.saveJournalEntryByObject(bankChargesJournalEntry);
                    Set<JournalEntryDetail> detail = null;
                    detail = (journalEntryDetailCommonObjectsForBankCharges(paramJobj, bankChargesJournalEntry, receipt, true));
                    bankChargesJournalEntry.setDetails(detail);
                    accJournalEntryobj.saveJournalEntryDetailsSet(detail);
                    if (editReceiptObject == null || paymentWithoutBankChargesJe) {
                        sequencecounter++;
                    }
                }
                bankInterestAccid = paramJobj.optString("bankInterestCmb");
                if (!StringUtil.isNullOrEmpty(bankInterestAccid)) {     //Create JE for bank interest
                    if (editReceiptObject != null) {
                        paymentWithoutBankInterestJe = oldBankInterestJE != null ? false : true;
                    }
                    JournalEntry bankInterestJournalEntry = journalEntryObjectBankCharges(paramJobj, editReceiptObject, sequencecounter, false, paymentWithoutBankInterestJe, oldBankChargeJE, oldBankInterestJE);
                    if (editReceiptObject != null && !StringUtil.isNullOrEmpty(bankInterestJournalEntry.getEntryNumber())) {
                        jeEntryNo += ", " + bankInterestJournalEntry.getEntryNumber();
                    }
                    receipt.setJournalEntryForBankInterest(bankInterestJournalEntry);
                    receipts.add(receipt);
                    bankInterestJournalEntry.setTransactionId(receiptID);
                    bankInterestJournalEntry.setTransactionModuleid(Constants.Acc_Receive_Payment_ModuleId);
                    //ERP-18778
                    bankInterestJournalEntry.setIsmulticurrencypaymentje(journalEntry.isIsmulticurrencypaymentje());
                    bankInterestJournalEntry.setPaymentcurrencytopaymentmethodcurrencyrate(journalEntry.getPaymentcurrencytopaymentmethodcurrencyrate());
                    if (approvalStatusLevel == Constants.APPROVED_STATUS_LEVEL) {
                        bankInterestJournalEntry.setPendingapproval(0);
                    } else {
                        bankInterestJournalEntry.setPendingapproval(1);
                    }
                    accJournalEntryobj.saveJournalEntryByObject(bankInterestJournalEntry);
                    Set<JournalEntryDetail> detail = null;
                    detail = (journalEntryDetailCommonObjectsForBankCharges(paramJobj, bankInterestJournalEntry, receipt, false));
                    bankInterestJournalEntry.setDetails(detail);
                    accJournalEntryobj.saveJournalEntryDetailsSet(detail);

                }
                details.addAll(journalEntryDetailCommonObjects(paramJobj, jSONArrayCNDN, journalEntry, receipt, Constants.PaymentAgainstCNDN));
            }
            journalEntry.setDetails(details);
            journalEntry.setTransactionId(receiptID);
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
            PayDetail payDetail = getPayDetailObject(paramJobj, editReceiptObject, receipt);
            receipt.setPayDetail(payDetail);

            /*
             * Revaluation JE Entry - If Payment Against Invoice which are
             * revaluated ***Parameter - oldReevalJE - Revaluation Journal Entry
             * ID 1. Revaluation Agianst invoice
             */
            counterMap.put("counter", sequencecounter);
            saveReevalJournalEntryObjects(paramJobj, jSONArrayAgainstInvoice, receipt, Constants.PaymentAgainstInvoice, oldReevalJE, counterMap);
            String customerName = "";
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("accid"))) {
                KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), paramJobj.getString("accid"));
                Account account = (Account) accresult.getEntityList().get(0);
                String accountid = paramJobj.optString("accid");
                result = accReceiptDAOobj.getaccountdetailsReceipt(accountid);
                if (result.getRecordTotalCount() > 0) {
                    Customer customer = (Customer) result.getEntityList().get(0);
                    accountaddress = customer.getBillingAddress();
                    customerName = customer.getName();
                }
                if (account != null) {
                    accountName = account.getName();
                }
            }
            String Cardid = "";
            DecimalFormat df = new DecimalFormat("#,###,###,##0.00");
            String basecurrency = paramJobj.optString(Constants.globalCurrencyKey);
            KwlReturnObject resultCurrency = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
            KWLCurrency currency = (KWLCurrency) resultCurrency.getEntityList().get(0);
            String amountreceipt = receipt.getDepositAmount() + "";

            String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(receipt.getDepositAmount())), currency, countryLanguageId);
            paymentid = receipt.getID();
            String[] amountnew = new String[]{"amount", String.valueOf(amountreceipt)};
            amountpayment = amountnew[1].intern();
            billno = receipt.getReceiptNumber();
            advanceamount = String.valueOf(receipt.getAdvanceamount());
            accountaddress = accountaddress;
            accountName = accountName;
            if (receipt.getJournalEntry().getEntryNumber() != null) {
                JENumBer = receipt.getJournalEntry().getEntryNumber();
            }
            receipttype = receipt.getReceipttype();
            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isChequePrint",null))) {
                isChequePrint = Boolean.parseBoolean(paramJobj.optString("isChequePrint"));
            }
            if (isChequePrint) {
                Date creationDate = new Date(paramJobj.optString("creationdate"));
                String date = DATE_FORMAT.format(creationDate);
                String[] amount = new String[]{"amount", String.valueOf(amountreceipt)};
                String[] amount1 = new String[]{"amountinword", netinword};
                String[] accName = new String[]{"accountName", customerName};
                jobjDetails.put(amount[0], amount[1]);
                jobjDetails.put(amount1[0], amount1[1]);
                jobjDetails.put(accName[0], accName[1]);
                jobjDetails.put("date", date);
                jArr.put(jobjDetails);
            }
            String action = "made";
            if (isEdit == true) {
                action = "updated";
            }
            issuccess = true;

            String moduleName = Constants.PAYMENT_RECEIVED;
            //Send Mail when Customer Receipt is generated or modified.
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), companyid);
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            if (documentEmailSettings != null) {
                boolean sendmail = false;
                boolean isEditMail = false;
                if (StringUtil.isNullOrEmpty(receiptid)) {
                    if (documentEmailSettings.isReceiptGenerationMail()) { ////Create New Case
                        sendmail = true;
                    }
                } else {
                    isEditMail = true;
                    if (documentEmailSettings.isReceiptUpdationMail()) { // edit case  
                        sendmail = true;
                    }
                }
                if (sendmail) {
                    String userMailId = "", userName = "", currentUserid = "";
                    String createdByEmail = "";
                    String createdById = "";
                    HashMap<String, Object> requestParams = AccountingManager.getEmailNotificationParamsJson(paramJobj);
                    if (requestParams.containsKey("userfullName") && requestParams.get("userfullName") != null) {
                        userName = (String) requestParams.get("userfullName");
                    }
                    if (requestParams.containsKey("usermailid") && requestParams.get("usermailid") != null) {
                        userMailId = (String) requestParams.get("usermailid");
                    }
                    if (requestParams.containsKey(Constants.useridKey) && requestParams.get(Constants.useridKey) != null) {
                        currentUserid = (String) requestParams.get(Constants.useridKey);
                    }
                    List<String> mailIds = new ArrayList();
                    if (!StringUtil.isNullOrEmpty(userMailId)) {
                        mailIds.add(userMailId);
                    }
                    /*
                     * if Edit mail option is true then get userid and Email id
                     * of document creator.
                     */
                    if (isEditMail) {
                        if (editReceiptObject != null && editReceiptObject.getCreatedby() != null) {
                            createdByEmail = editReceiptObject.getCreatedby().getEmailID();
                            createdById = editReceiptObject.getCreatedby().getUserID();
                        }
                        /*
                         * if current user userid == document creator userid
                         * then don't add creator email ID in List.
                         */
                        if (!StringUtil.isNullOrEmpty(createdByEmail) && !(currentUserid.equalsIgnoreCase(createdById))) {
                            mailIds.add(createdByEmail);
                        }
                    }
                    String[] temp = new String[mailIds.size()];
                    String[] tomailids = mailIds.toArray(temp);
                    String receiptNumber = entryNumber;
                    accountingHandlerDAOobj.sendSaveTransactionEmails(receiptNumber, moduleName, tomailids, userName, isEditMail, companyid);
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
                deleteChequeOrCard(Cardid, oldJE.getCompany().getCompanyID());// method used inside savePayment
            }
            if (oldJE != null) {
                deleteJEArray(oldJE.getID(), oldJE.getCompany().getCompanyID());
            }
            if (oldBankChargeJE != null) {          // delete bank charge JE.
                // Delete Bank Reconciliation records for old Bank Charge JE
                Map<String, Object> bankReconMap = new HashMap<>();
                bankReconMap.put("companyId", oldBankChargeJE.getCompany().getCompanyID());
                bankReconMap.put("oldjeid", oldBankChargeJE.getID());
                deleteBankReconcilation(bankReconMap);
                deleteJEArray(oldBankChargeJE.getID(), oldBankChargeJE.getCompany().getCompanyID());
            }
            if (oldBankInterestJE != null) {          // delete bank Interest JE.
                // Delete Bank Reconciliation records for old Bank Interest JE
                Map<String, Object> bankReconMap = new HashMap<>();
                bankReconMap.put("companyId", oldBankInterestJE.getCompany().getCompanyID());
                bankReconMap.put("oldjeid", oldBankInterestJE.getID());
                deleteBankReconcilation(bankReconMap);
                deleteJEArray(oldBankInterestJE.getID(), oldBankInterestJE.getCompany().getCompanyID());
            }
            deleteTemporaryInvoicesEntries(jSONArrayAgainstInvoice, companyid);
            accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);
            txnManager.commit(status);

            TransactionStatus AutoNoPayStatus = null;
            String bankChargeJeNo = "", bankInterestJeNo = "";
            String linkedDocuments = "";
            String fromLinkCombo = "";
            boolean avoidCommitTransactionForLinkingInfo=false;
            if (paramJobj.optBoolean(Constants.isMultiGroupCompanyFlag) || paramJobj.optBoolean(Constants.isdefaultHeaderMap)) {
                avoidCommitTransactionForLinkingInfo = true;
            }
            
            try {
                synchronized (this) {

                    DefaultTransactionDefinition def2 = new DefaultTransactionDefinition();
                    def2.setName("AutoNum_Tx");
                    def2.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    AutoNoPayStatus = txnManager.getTransaction(def2);

                    if (((editReceiptObject == null && beforeEditApprovalStatus==0) || (editReceiptObject!=null && beforeEditApprovalStatus!=11)) && StringUtil.isNullOrEmpty(journalEntry.getEntryNumber())) {
                        String jeentryNumber = "";
                        int jeIntegerPart = 0;
                        String jeDatePrefix = "";
                        String jeDateAfterPrefix = "";
                        String jeDateSuffix = "";
                        String jeSeqFormatId = "";
                        HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                        JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                        JEFormatParams.put("modulename", "autojournalentry");
                        JEFormatParams.put(Constants.companyKey, companyid);
                        JEFormatParams.put("isdefaultFormat", true);
                        KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, journalEntry.getEntryDate());
                        jeentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                        jeIntegerPart = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                        jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        jeDateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                        jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        jeSeqFormatId = format.getID();
                        seqNumberMap.put(Constants.DOCUMENTID, journalEntry.getID());
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, jeSeqFormatId);                        
                        if (approvalStatusLevel == Constants.APPROVED_STATUS_LEVEL) {
                        JENumBer = accJournalEntryobj.UpdateJournalEntry(seqNumberMap);
                        journalEntry.setEntryNumber(jeentryNumber);
                        journalEntry.setSeqnumber(jeIntegerPart);
                        journalEntry.setDatePreffixValue(jeDatePrefix);
                        journalEntry.setDateAfterPreffixValue(jeDateAfterPrefix);
                        journalEntry.setDateSuffixValue(jeDateSuffix);
                        receipt.setJournalEntry(journalEntry);
                    }
                    }
                    boolean isFromOtherSource=paramJobj.optBoolean("isFromOtherSource", false);
                    if (!sequenceformat.equals("NA") && editReceiptObject == null && !isFromOtherSource) {
                        boolean seqformat_oldflag = StringUtil.getBoolean(paramJobj.optString("seqformat_oldflag"));
                        String nextAutoNo = "";
                        int nextAutoNoInt = 0;
                        String datePrefix = "";
                        String dateafterPrefix = "";
                        String dateSuffix = "";
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        if (seqformat_oldflag) {
                            nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_RECEIPT, sequenceformat);
                            seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextAutoNo);
                        } else {
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_RECEIPT, sequenceformat, seqformat_oldflag, receipt.getCreationDate());
                            nextAutoNo = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                            nextAutoNoInt = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                            datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                            dateafterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                            dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        }

                        seqNumberMap.put(Constants.DOCUMENTID, receipt.getID());
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        billno = accReceiptDAOobj.UpdateReceiptEntry(seqNumberMap);
                        receipt.setReceiptNumber(nextAutoNo);
                        receipt.setSeqnumber(nextAutoNoInt);
                        receipt.setDatePreffixValue(datePrefix);
                        receipt.setDateAfterPreffixValue(dateafterPrefix);
                        receipt.setDateSuffixValue(dateSuffix);
                    }

                    /*
                     * saving linking information in Receive Payment & Debit
                     * Note If Payment is made with Debit Note
                     */
                    if(!avoidCommitTransactionForLinkingInfo){
                        if (jSONArrayCNDN.length() > 0) {

                            JSONArray drAccArr = jSONArrayCNDN;
                            for (int i = 0; i < drAccArr.length(); i++) {
                                JSONObject jobj1 = drAccArr.getJSONObject(i);
                                String dnnoteid = jobj1.getString("documentid");
                                KwlReturnObject cnResult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnnoteid);
                                DebitNote debitNote = (DebitNote) cnResult.getEntityList().get(0);

                                /*
                                 * Method is used to save linking informatio of
                                 * Received Payment
                                 *
                                 * when linking with Debit Note
                                 */
                                String debitNoteNo = debitNote.getDebitNoteNumber();
                                saveLinkingInformationOfPaymentWithDN(debitNote, receipt, billno);
                                linkedDocuments += debitNoteNo + " ,";
                            }
                            linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
                            fromLinkCombo +=" Debit Note("+linkedDocuments+")";
                        }

                    /*
                     * saving linking information in Receive Payment & Sales
                     * Invoice If Payment is made against Sales Invoice
                     */
                        if (jSONArrayAgainstInvoice.length() > 0) {
                            JSONArray drAccArr = jSONArrayAgainstInvoice;
                            linkedDocuments="";
                            for (int i = 0; i < drAccArr.length(); i++) {
                                JSONObject jobj1 = drAccArr.getJSONObject(i);
                                String invoiceid = jobj1.getString("documentid");
                                KwlReturnObject cnResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
                                Invoice invoice = (Invoice) cnResult.getEntityList().get(0);

                                /*
                                 * Method is used to save linking informatio of
                                 * Received Payment
                                 *
                                 * when linking with Debit Note
                                 */
                                if (invoice != null) {
                                    String invoiceNo = invoice.getInvoiceNumber();
                                    saveLinkingInformationOfPaymentWithSalesInvoice(invoice, receipt, billno);
                                    linkedDocuments += invoiceNo + " ,";
                                }
                            }
                            linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
                            if (StringUtil.isNullOrEmpty(fromLinkCombo)) {
                                fromLinkCombo += "Sales Invoice(" + linkedDocuments + ")";
                            } else {
                                fromLinkCombo += ", Sales Invoice(" + linkedDocuments + ")";
                            }
                        }
                        
                        if (!StringUtil.isNullOrEmpty(bankChargesAccid)) {
                            if (editReceiptObject == null || paymentWithoutBankChargesJe) {
                                String jeSeqFormatId = "";
                                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                                JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                                JEFormatParams.put("modulename", "autojournalentry");
                                JEFormatParams.put(Constants.companyKey, companyid);
                                JEFormatParams.put("isdefaultFormat", true);
                                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, receipt.getJournalEntryForBankCharges().getEntryDate());
                                jeSeqFormatId = format.getID();
                                seqNumberMap.put(Constants.DOCUMENTID, receipt.getJournalEntryForBankCharges().getID());
                                seqNumberMap.put(Constants.companyKey, companyid);
                                seqNumberMap.put(Constants.SEQUENCEFORMATID, jeSeqFormatId);
                                bankChargeJeNo = accJournalEntryobj.UpdateJournalEntry(seqNumberMap);
                                jeEntryNo += ", " + bankChargeJeNo;
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(bankInterestAccid)) {
                            if (editReceiptObject == null || paymentWithoutBankInterestJe) {
                                String jeSeqFormatId = "";
                                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                                JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                                JEFormatParams.put("modulename", "autojournalentry");
                                JEFormatParams.put(Constants.companyKey, companyid);
                                JEFormatParams.put("isdefaultFormat", true);
                                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, receipt.getJournalEntryForBankInterest().getEntryDate());
                                jeSeqFormatId = format.getID();
                                seqNumberMap.put(Constants.DOCUMENTID, receipt.getJournalEntryForBankInterest().getID());
                                seqNumberMap.put(Constants.companyKey, companyid);
                                seqNumberMap.put(Constants.SEQUENCEFORMATID, jeSeqFormatId);
                                bankInterestJeNo = accJournalEntryobj.UpdateJournalEntry(seqNumberMap);
                                jeEntryNo += ", " + bankInterestJeNo;
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(paramJobj.optString("accid"))) {
                            //params to send to get billing address
                            HashMap<String, Object> addressParams = new HashMap<String, Object>();
                            addressParams.put(Constants.companyKey, companyid);
                            addressParams.put("isDefaultAddress", true);
                            addressParams.put("isBillingAddress", true);
                            if (receipt.getPaymentWindowType() == Constants.Receive_Payment_from_Vendor) {
                                addressParams.put("vendorid", paramJobj.optString("accid"));
                                VendorAddressDetails vendorAddressDetail = accountingHandlerDAOobj.getVendorAddressObj(addressParams);
                                custVenEmailId = vendorAddressDetail != null ? vendorAddressDetail.getEmailID() : "";

                            } else if (receipt.getPaymentWindowType() == Constants.Receive_Payment_from_Customer) {
                                addressParams.put("customerid", paramJobj.optString("accid"));
                                CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                                custVenEmailId = customerAddressDetails != null ? customerAddressDetails.getEmailID() : "";
                            }
                        }
                    }
                    txnManager.commit(AutoNoPayStatus);
                    
                    //After committing inserting the linking info for MultiGroup
                    //Error: Cannot acquire lock exception
                    if (avoidCommitTransactionForLinkingInfo) {

                        if (jSONArrayAgainstInvoice.length() > 0) {
                            JSONArray drAccArr = jSONArrayAgainstInvoice;
                            linkedDocuments="";
                            for (int i = 0; i < drAccArr.length(); i++) {
                                JSONObject jobj1 = drAccArr.getJSONObject(i);
                                String invoiceid = jobj1.optString("documentid");
                                KwlReturnObject cnResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
                                Invoice invoice = (Invoice) cnResult.getEntityList().get(0);

                                /*
                                 * Method is used to save linking informatio of
                                 * Received Payment
                                 *
                                 * when linking with Debit Note
                                 */
                                if (invoice != null) {
                                    String invoiceNo = invoice.getInvoiceNumber();
                                    saveLinkingInformationOfPaymentWithSalesInvoiceNew(invoice, receipt, billno);
                                    linkedDocuments += invoiceNo + " ,";
                                }
                            }
                            linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
                            if (StringUtil.isNullOrEmpty(fromLinkCombo)) {
                                fromLinkCombo += " Sales Invoice (" + linkedDocuments + ")";
                            } else {
                                fromLinkCombo += ", Sales Invoice (" + linkedDocuments + ")";
                            }
                        }
                        
                        if (jSONArrayCNDN.length() > 0) {
                            JSONArray drAccArr = jSONArrayCNDN;
                            linkedDocuments="";
                            for (int i = 0; i < drAccArr.length(); i++) {
                                JSONObject jobj1 = drAccArr.getJSONObject(i);
                                String dnnoteid = jobj1.optString("documentid");
                                KwlReturnObject cnResult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnnoteid);
                                DebitNote debitNote = (DebitNote) cnResult.getEntityList().get(0);

                                /*
                                 * Method is used to save linking informatio of
                                 * Received Payment
                                 *
                                 * when linking with Debit Note
                                 */
                                String debitNoteNo = debitNote.getDebitNoteNumber();
                                saveLinkingInformationOfPaymentWithDN(debitNote, receipt, billno);
                                linkedDocuments += debitNoteNo + " ,";
                            }
                            linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
                            fromLinkCombo += "Debit Note ("+linkedDocuments+")";
                        }

                        if (!StringUtil.isNullOrEmpty(bankChargesAccid)) {
                            if (editReceiptObject == null || paymentWithoutBankChargesJe) {
                                String jeSeqFormatId = "";
                                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                                JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                                JEFormatParams.put("modulename", "autojournalentry");
                                JEFormatParams.put(Constants.companyKey, companyid);
                                JEFormatParams.put("isdefaultFormat", true);
                                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, receipt.getJournalEntryForBankCharges().getEntryDate());
                                jeSeqFormatId = format.getID();
                                seqNumberMap.put(Constants.DOCUMENTID, receipt.getJournalEntryForBankCharges().getID());
                                seqNumberMap.put(Constants.companyKey, companyid);
                                seqNumberMap.put(Constants.SEQUENCEFORMATID, jeSeqFormatId);
                                bankChargeJeNo = accJournalEntryobj.UpdateJournalEntry(seqNumberMap);
                                jeEntryNo += ", " + bankChargeJeNo;
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(bankInterestAccid)) {
                            if (editReceiptObject == null || paymentWithoutBankInterestJe) {
                                String jeSeqFormatId = "";
                                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                                JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                                JEFormatParams.put("modulename", "autojournalentry");
                                JEFormatParams.put(Constants.companyKey, companyid);
                                JEFormatParams.put("isdefaultFormat", true);
                                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, receipt.getJournalEntryForBankInterest().getEntryDate());
                                jeSeqFormatId = format.getID();
                                seqNumberMap.put(Constants.DOCUMENTID, receipt.getJournalEntryForBankInterest().getID());
                                seqNumberMap.put(Constants.companyKey, companyid);
                                seqNumberMap.put(Constants.SEQUENCEFORMATID, jeSeqFormatId);
                                bankInterestJeNo = accJournalEntryobj.UpdateJournalEntry(seqNumberMap);
                                jeEntryNo += ", " + bankInterestJeNo;
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(paramJobj.optString("accid"))) {
                            //params to send to get billing address
                            HashMap<String, Object> addressParams = new HashMap<String, Object>();
                            addressParams.put(Constants.companyKey, companyid);
                            addressParams.put("isDefaultAddress", true);
                            addressParams.put("isBillingAddress", true);
                            if (receipt.getPaymentWindowType() == Constants.Receive_Payment_from_Vendor) {
                                addressParams.put("vendorid", paramJobj.optString("accid"));
                                VendorAddressDetails vendorAddressDetail = accountingHandlerDAOobj.getVendorAddressObj(addressParams);
                                custVenEmailId = vendorAddressDetail != null ? vendorAddressDetail.getEmailID() : "";

                            } else if (receipt.getPaymentWindowType() == Constants.Receive_Payment_from_Customer) {
                                addressParams.put("customerid", paramJobj.optString("accid"));
                                CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                                custVenEmailId = customerAddressDetails != null ? customerAddressDetails.getEmailID() : "";
                            }
                        }
                    }//end of avoidCommitTransactionForLinkingInfo
                }
            } catch (Exception ex) {
                if (AutoNoPayStatus != null) {
                    txnManager.rollback(AutoNoPayStatus);
                }
                deleteTemporaryInvoicesEntries(jSONArrayAgainstInvoice, companyid);
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);
                Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //========Code for Rounding JE Started=============

            if (jSONArrayAgainstInvoice.length() > 0) {
                TransactionStatus roundingJEStatus = null;
                DefaultTransactionDefinition def2 = new DefaultTransactionDefinition();
                def2.setName("RoundingJE_Tx");
                def2.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                roundingJEStatus = txnManager.getTransaction(def2);
                try {
                    JSONArray drAccArr = jSONArrayAgainstInvoice;
                    for (int i = 0; i < drAccArr.length(); i++) {
                        JSONObject jobj1 = drAccArr.getJSONObject(i);
                        String invoiceid = jobj1.getString("documentid");
                        KwlReturnObject cnResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
                        Invoice invoice = (Invoice) cnResult.getEntityList().get(0);
                        if (invoice != null) {
                            paramJobj.put("salesInvoiceObj", invoice);
                            paramJobj.put("isEdit", isEdit);
                            paramJobj.put("receiptnumber", billno);
                            postRoundingJEOnReceiptSave(paramJobj);
                        }
                    }
                    txnManager.commit(roundingJEStatus);
                } catch (JSONException | ServiceException | SessionExpiredException | AccountingException | TransactionException ex) {
                    if (roundingJEStatus != null) {
                        txnManager.rollback(roundingJEStatus);
                    }
                    Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
                
            //========Code for Rounding JE Ended=============
            /*
             * Preparing Audit trial message if document is linking at the time
             * of creating
             */
            String linkingMessages = "";
            if (!StringUtil.isNullOrEmpty(fromLinkCombo)) {
               // linkingMessages = " by Linking to " + fromLinkCombo + " " + linkedDocuments;
                linkingMessages = " for " + fromLinkCombo;
            }
            /*
             * Preparing Audit trial message if receipt is to whether Customer
             * or Vendor or against GL Code
             */
            String receiptType = "";
            if (receipt.getPaymentWindowType() == Constants.Receive_Payment_from_Customer) {
                receiptType = " Customer ";
            } else if (receipt.getPaymentWindowType() == Constants.Receive_Payment_from_Vendor) {
                receiptType = " Vendor ";
            } else if (receipt.getPaymentWindowType() == Constants.Receive_Payment_against_GL_Code) {
                receiptType = " GL Code ";
            }
          
            /*
             * Inserting Entry in Audit trial when any document is unlinking
             * through Edit
             */
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));                       
            if (approvalStatusLevel != Constants.APPROVED_STATUS_LEVEL) {
                String pendingforApproval = " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
                auditTrailObj.insertAuditLog(AuditAction.RECEIVE_PAYMENT, "User " + paramJobj.optString(Constants.userfullname) + " has " + action + " a "+receiptType +" Receive Payment "+ billno + linkingMessages + AuditMsg + pendingforApproval, auditRequestParams, receipt.getID());
            } else {
                auditTrailObj.insertAuditLog(AuditAction.RECEIVE_PAYMENT, "User " + paramJobj.optString(Constants.userfullname) + " has " + action + " a "+receiptType +" Receive Payment "+ billno + linkingMessages + AuditMsg, auditRequestParams, receipt.getID());
            }
            if (approvalStatusLevel != 11) {//pending for approval case
                String receipNoteSaved = messageSource.getMessage("acc.payreceipt.save", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
                String butPendingForApproval = " " + messageSource.getMessage("acc.field.butpendingforApproval", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
                msg = receipNoteSaved + butPendingForApproval + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + billno + "</b>.";
            } else {
                msg = messageSource.getMessage("acc.payreceipt.save", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + billno + ",</b>" + messageSource.getMessage("acc.field.JENo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + JENumBer + "" + jeEntryNo + "</b>";   //"Receipt has been saved successfully";
            }
            jobj.put("pendingApproval", approvalStatusLevel != 11 );

        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            deleteTemporaryInvoicesEntries(jSONArrayAgainstInvoice, companyid);
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);
            } catch (Exception ee) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ee);
            }
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            deleteTemporaryInvoicesEntries(jSONArrayAgainstInvoice, companyid);
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);
            } catch (Exception ee) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ee);
            }
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (AccountingException AE) {
            if (status != null && !exceptionChequeOrInv) {
                txnManager.rollback(status);
            }
            deleteTemporaryInvoicesEntries(jSONArrayAgainstInvoice, companyid);
            try {
                status = txnManager.getTransaction(def);
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);
                txnManager.commit(status);
            } catch (Exception ee) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ee);
            }
            msg = "" + AE.getMessage();
            if (AE.getMessage() == null) {
                msg = AE.getCause().getMessage();
            }
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, AE);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            deleteTemporaryInvoicesEntries(jSONArrayAgainstInvoice, companyid);
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);
            } catch (Exception ee) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ee);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("paymentid", paymentid);
                jobj.put(Constants.data, jArr);
                jobj.put("billno", billno);
                jobj.put("isAccountingExe", accexception);
                jobj.put("advanceamount", advanceamount);
                jobj.put("amount", amountpayment);
                jobj.put("address", accountaddress);
                jobj.put("accountName", accountName);
                jobj.put("billingEmail", custVenEmailId);
                jobj.put("receipttype", receipttype);
                hashMap.put("jobj", jobj);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return hashMap;
    }
    
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void deleteLinkingInfo(HashMap<String, Object> requestParams) throws SessionExpiredException, ServiceException {
        accReceiptDAOobj.deleteLinkingInformationOfRP(requestParams);
    } 
    
    
    
    /*---------------- Creating Receive Payment in bulk----------*/
  @Override 
     public HashMap<String, Object> saveBulkCustomerReceipt(JSONObject paramJobj) {
     
        JSONObject jobj = new JSONObject();

        boolean issuccess = true;

        String msg = "";

        String billno = "";

        KwlReturnObject result = null;
        boolean isEdit = false;

        String JENumBer = "";
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        String jeEntryNo = "";
        TransactionStatus status = null;

        String companyid = "";

        String entryNumber = "";
        boolean exceptionChequeOrInv = false;
        String journalEntryNumber = "";
        String paymentNumber = "";
        String pendingpaymentNumber = "";

        HashMap<String, JSONArray> jsonArrayAgainstInvoice = new HashMap<String, JSONArray>();
        JSONArray invoicesAgainstParticularCustomer = new JSONArray();
        String chequeNumber = "";

        try {
            String receiptid = paramJobj.optString(Constants.billid);
            Receipt editReceiptObject = null;
            String sequenceformat = paramJobj.optString(Constants.sequenceformat) != null ? paramJobj.optString(Constants.sequenceformat) : "NA";      
            companyid = paramJobj.optString(Constants.companyKey);
            String detailsJsonString = paramJobj.optString("Details");
            JSONArray jsonArray = new JSONArray(detailsJsonString);
            JSONArray jSONArrayCNDN = new JSONArray();
            
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
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jSONObject = jsonArray.getJSONObject(i);
                chequeNumber = jSONObject.optString("chequenumber","NA");
                String customerid = jSONObject.getString("accid");
                String cust_chequeKey = chequeNumber+customerid;
                /**
                 * handle for below case:SDP-12577
                 * if two customers having saving same cheque number then need to save two payments. 
                 */
                /* Problem will b raised when combination of NA & valid sequence Format*/
                if (!chequeNumber.equalsIgnoreCase("NA")) {
                    if (!jsonArrayAgainstInvoice.containsKey(cust_chequeKey)) {
                        jsonArrayAgainstInvoice.put(cust_chequeKey, new JSONArray());
                    }
                        jsonArrayAgainstInvoice.get(cust_chequeKey).put(jSONObject);

                } else {
                    
                    if (!jsonArrayAgainstInvoice.containsKey(customerid)) {
                        jsonArrayAgainstInvoice.put(customerid, new JSONArray());
                    }
                    jsonArrayAgainstInvoice.get(customerid).put(jSONObject);
                }

            }

       
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));

         
            /* ----Looping on key Customer
            
             Saving data ony by one against a Customer
             ------------- */
            int approvalStatusLevel = 11;
            for (String personIdKey : jsonArrayAgainstInvoice.keySet()) {

                invoicesAgainstParticularCustomer = jsonArrayAgainstInvoice.get(personIdKey);

                String linkingDocumentNumber = "";
                try {
                    String invoiceId = "";
                    String payDetail = "";
                    //In add case check duplicate number
                    result = accReceiptDAOobj.getReceiptFromBillNo(entryNumber, companyid);
                    if (result.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                        //accexception = true;
                        throw new AccountingException(messageSource.getMessage("acc.payment.receivepaymentno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }

                    synchronized (this) {
                        status = txnManager.getTransaction(def);
                        KwlReturnObject resultPay = accCommonTablesDAO.getTransactionInTemp(entryNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);
                        if (resultPay.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                           // accexception = true;
                            throw new AccountingException(messageSource.getMessage("acc.payment.selectedReceivePamentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                        } else {
                            accCommonTablesDAO.insertTransactionInTemp(entryNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);
                        }

                        for (int i = 0; i < invoicesAgainstParticularCustomer.length(); i++) {
                            JSONObject invoiceJobj = invoicesAgainstParticularCustomer.getJSONObject(i);
                            invoiceId = invoiceJobj.getString("documentid");

                            linkingDocumentNumber += invoiceJobj.getString("documentno") + ",";
                            invoiceJobj.put("postdate", paramJobj.optString("postdate"));
                            paramJobj.put("accid", invoiceJobj.get("accid")); //vendorid
                            chequeNumber = invoiceJobj.optString("chequenumber");
                            invoiceId = invoiceJobj.getString("documentid");
                            invoiceJobj.put(Constants.sequenceformat, "NA");
                            payDetail = invoiceJobj.toString();
                            KwlReturnObject resultInv = accPaymentDAOobj.getInvoiceInTemp(invoiceId, companyid, Constants.Acc_Invoice_ModuleId);
                            if (resultInv.getRecordTotalCount() > 0) {
                                throw new AccountingException("Selected invoice is already in process, please try after sometime.");
                            } else {
                                accPaymentDAOobj.insertInvoiceOrCheque(invoiceId, companyid, Constants.Acc_Invoice_ModuleId, "");  //store invoice ids in temporary table.If maultiple user make a payment at same time then it throw an exception. 
                            }
                        }
                        paramJobj.put("paydetail", payDetail);
                        txnManager.commit(status);
                    }
                } catch (Exception ex) {
                    issuccess=false;
                    exceptionChequeOrInv = true;
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                    throw new AccountingException(ex.getMessage(), ex);
                }

                status = txnManager.getTransaction(def);

                /*--------- Code to check whether selected invoices have already amount due zero then selected invoices have been alraedy used to create Bulk Payment 
               
                 ------So reload invoice report------ */
                if (!isEdit) {
                    for (int i = 0; i < invoicesAgainstParticularCustomer.length(); i++) {
                        JSONObject temp = invoicesAgainstParticularCustomer.getJSONObject(i);
                        KwlReturnObject resultInvoice = accountingHandlerDAOobj.getObject(Invoice.class.getName(), temp.getString("documentid"));
                        Invoice invoice = (Invoice) resultInvoice.getEntityList().get(0);
                        boolean isInvoiceIsClaimed = (invoice.getBadDebtType() == Constants.Invoice_Claimed || invoice.getBadDebtType() == Constants.Invoice_Recovered);
                        double invoiceAmountDue = 0;
                        if (isInvoiceIsClaimed) {
                            invoiceAmountDue = invoice.getClaimAmountDue();
                        } else {
                            if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                                invoiceAmountDue = invoice.getOpeningBalanceAmountDue();
                            } else {
                                invoiceAmountDue = invoice.getInvoiceamountdue();
                            }
                        }
                        double amountEntered = temp.getDouble("enteramount");
                        double adjustedRate = 1;
                        if (!StringUtil.isNullOrEmpty(temp.optString("exchangeratefortransaction", "").toString())) {
                            adjustedRate = Double.parseDouble(temp.get("exchangeratefortransaction").toString());
                        }
                        double paymentAmountReceivedTemp = invoiceAmountDue * adjustedRate;
                        paymentAmountReceivedTemp = authHandler.round(paymentAmountReceivedTemp, companyid);
                        boolean isFullPayment = paymentAmountReceivedTemp == amountEntered;
                        double discountAmount = temp.optDouble("discountname", 0.0);
                        discountAmount = authHandler.round(discountAmount, companyid);
                        if (isDiscountAppliedOnPaymentTerms && discountAmount > 0.0) {
                            isFullPayment = paymentAmountReceivedTemp == (amountEntered + discountAmount);
                        }
                        if (!isFullPayment) {
                            double amountReceivedConverted = temp.getDouble("enteramount");
                            if (!StringUtil.isNullOrEmpty(temp.optString("exchangeratefortransaction", "").toString())) {
                                adjustedRate = Double.parseDouble(temp.get("exchangeratefortransaction").toString());
                                amountReceivedConverted = amountEntered / adjustedRate;
                                amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                            }
                            invoiceAmountDue = authHandler.round(invoiceAmountDue, companyid);
                            if (invoiceAmountDue < amountReceivedConverted) {
                                throw new AccountingException("Amount entered for invoice cannot be greater than it's amount due. Please reload the invoices.");
                            }
                        }
                    }
                }

        
                Receipt receipt = createReceiptObject(paramJobj, editReceiptObject);
                int beforeEditApprovalStatus = editReceiptObject != null ? editReceiptObject.getApprovestatuslevel() : 0;         //getting current approval status before updating

                List mailParams = Collections.EMPTY_LIST;
//                int approvalStatusLevel = 11;
                /*
                    add to Calculate correct  total amount for particular customer
                    */
                double totalAmount = 0;
                for (int i = 0; i < invoicesAgainstParticularCustomer.length(); i++) {
                    if (!invoicesAgainstParticularCustomer.getJSONObject(i).optString("enteramount").equalsIgnoreCase("")) {
                        totalAmount += Double.parseDouble((String) invoicesAgainstParticularCustomer.getJSONObject(i).get("enteramount"));
                    }
                }
                HashMap<String, Object> receiveApproveMap = new HashMap<String, Object>();
                List approvedlevel = null;
                receiveApproveMap.put(Constants.companyKey, companyid);
                receiveApproveMap.put("level", 0);//Initialy it will be 0
                receiveApproveMap.put("totalAmount", String.valueOf(authHandler.round(totalAmount, companyid)));
                receiveApproveMap.put("currentUser", receipt.getCreatedby());
                receiveApproveMap.put("fromCreate", true);
                receiveApproveMap.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                boolean isMailApplicable = false;
                List list = new ArrayList();
                list.add(receipt.getID());
                approvedlevel = approveReceivePayment(list, receiveApproveMap, isMailApplicable);           //makes approval status to 1
                approvalStatusLevel = (Integer) approvedlevel.get(0);
                mailParams = (List) approvedlevel.get(1);
                receipt.setApprovestatuslevel(approvalStatusLevel);

                receipt.setTax(null);
                receipt.setTaxAmount(0);

                JournalEntry journalEntry = journalEntryObject(paramJobj, editReceiptObject, receipt.getID());

                if (approvalStatusLevel == Constants.APPROVED_STATUS_LEVEL) {
                    journalEntry.setPendingapproval(0);
                } else {
                    journalEntry.setPendingapproval(1);
                }

                receipt.setJournalEntry(journalEntry);
                List<Receipt> receipts = new ArrayList<Receipt>();
                receipts.add(receipt);
                accJournalEntryobj.saveJournalEntryByObject(journalEntry);

                
                /* -------- Code for adding custom field----------*/
                if (!StringUtil.isNullOrEmpty(paramJobj.optString((Constants.customfield)))) {
                    JSONArray jcustomarray = new JSONArray(paramJobj.optString(Constants.customfield));
                    if (paramJobj.optBoolean(Constants.isSquatTransaction)) {
                        jcustomarray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_Receive_Payment_ModuleId, companyid, true);
                    }
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                    customrequestParams.put("modulerecid", journalEntry.getID());
                    customrequestParams.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                    customrequestParams.put(Constants.companyKey, companyid);
                    customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        KwlReturnObject receiptAccJECustomData = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), journalEntry.getID());
                        AccJECustomData accJECustomData = (AccJECustomData) receiptAccJECustomData.getEntityList().get(0);
                        journalEntry.setAccBillInvCustomData(accJECustomData);
                    }
                }

                Set<JournalEntryDetail> details = null;
                accReceiptDAOobj.saveReceiptObject(receipts);

                String receiptID = receipts.get(0).getID();
                
                paramJobj.put("isDiscountAppliedOnPaymentTerms", isDiscountAppliedOnPaymentTerms);
                if (invoicesAgainstParticularCustomer.length() > 0) {
                    if (details == null) {
                        details = journalEntryDetailObject(paramJobj, invoicesAgainstParticularCustomer, journalEntry, receipt, Constants.PaymentAgainstInvoice);
                    } else {
                        details.addAll(journalEntryDetailObject(paramJobj, invoicesAgainstParticularCustomer, journalEntry, receipt, Constants.PaymentAgainstInvoice));
                    }
                    //Save Invoice Details selected in payment
                    HashSet receiptDetails = receiptDetailObject(paramJobj, invoicesAgainstParticularCustomer, receipt, Constants.PaymentAgainstInvoice);
                    receipt.setRows(receiptDetails);
                }

                double amount = 0;
                for (int i = 0; i < invoicesAgainstParticularCustomer.length(); i++) {
                    amount += Double.parseDouble((String) invoicesAgainstParticularCustomer.getJSONObject(i).get("enteramount"));
                }
                paramJobj.put("amount", amount);

                details.addAll(journalEntryDetailCommonObjects(paramJobj, jSONArrayCNDN, journalEntry, receipt, Constants.PaymentAgainstCNDN));

                journalEntry.setDetails(details);
                journalEntry.setTransactionId(receiptID);
                accJournalEntryobj.saveJournalEntryDetailsSet(details);
                /*
                 * Create PayDetail Object for - 1. Cheque Details
                 */
                PayDetail payDetail = getPayDetailObject(paramJobj, editReceiptObject, receipt);
                receipt.setPayDetail(payDetail);

                String customerName = "";
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("accid"))) {
                    KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), paramJobj.getString("accid"));
                    Account account = (Account) accresult.getEntityList().get(0);
                    String accountid = paramJobj.optString("accid");
                    result = accReceiptDAOobj.getaccountdetailsReceipt(accountid);
                    if (result.getRecordTotalCount() > 0) {
                        Customer customer = (Customer) result.getEntityList().get(0);
                        //accountaddress = customer.getBillingAddress();
                        customerName = customer.getName();
                    }
                    if (account != null) {
                       // accountName = account.getName();
                    }
                }

                String moduleName = Constants.PAYMENT_RECEIVED;
                //Send Mail when Customer Receipt is generated or modified.
                DocumentEmailSettings documentEmailSettings = null;
                KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), companyid);
                documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
                if (documentEmailSettings != null) {
                    boolean sendmail = false;
                    boolean isEditMail = false;
                    if (StringUtil.isNullOrEmpty(receiptid)) {
                        if (documentEmailSettings.isReceiptGenerationMail()) { ////Create New Case
                            sendmail = true;
                        }
                    } else {
                        isEditMail = true;
                        if (documentEmailSettings.isReceiptUpdationMail()) { // edit case  
                            sendmail = true;
                        }
                    }
                    if (sendmail) {
                        String userMailId = "", userName = "", currentUserid = "";
                        String createdByEmail = "";
                        String createdById = "";
                        HashMap<String, Object> requestParams = AccountingManager.getEmailNotificationParamsJson(paramJobj);
                        if (requestParams.containsKey("userfullName") && requestParams.get("userfullName") != null) {
                            userName = (String) requestParams.get("userfullName");
                        }
                        if (requestParams.containsKey("usermailid") && requestParams.get("usermailid") != null) {
                            userMailId = (String) requestParams.get("usermailid");
                        }
                        if (requestParams.containsKey(Constants.useridKey) && requestParams.get(Constants.useridKey) != null) {
                            currentUserid = (String) requestParams.get(Constants.useridKey);
                        }
                        List<String> mailIds = new ArrayList();
                        if (!StringUtil.isNullOrEmpty(userMailId)) {
                            mailIds.add(userMailId);
                        }
                        /*
                         * if Edit mail option is true then get userid and Email id
                         * of document creator.
                         */
                        if (isEditMail) {
                            if (editReceiptObject != null && editReceiptObject.getCreatedby() != null) {
                                createdByEmail = editReceiptObject.getCreatedby().getEmailID();
                                createdById = editReceiptObject.getCreatedby().getUserID();
                            }
                            /*
                             * if current user userid == document creator userid
                             * then don't add creator email ID in List.
                             */
                            if (!StringUtil.isNullOrEmpty(createdByEmail) && !(currentUserid.equalsIgnoreCase(createdById))) {
                                mailIds.add(createdByEmail);
                            }
                        }
                        String[] temp = new String[mailIds.size()];
                        String[] tomailids = mailIds.toArray(temp);
                        String receiptNumber = entryNumber;
                        accountingHandlerDAOobj.sendSaveTransactionEmails(receiptNumber, moduleName, tomailids, userName, isEditMail, companyid);
                    }
                }

                
                /* --------- Deleting temporary entries---------*/
                deleteTemporaryInvoicesEntries(invoicesAgainstParticularCustomer, companyid);
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);
                txnManager.commit(status);

                /*----------- Generating next Receipt & Journal Entry Number------*/
                TransactionStatus AutoNoPayStatus = null;
              
                try {
                    synchronized (this) {

                        DefaultTransactionDefinition def2 = new DefaultTransactionDefinition();
                        def2.setName("AutoNum_Tx");
                        def2.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                        AutoNoPayStatus = txnManager.getTransaction(def2);

                        if (((editReceiptObject == null && beforeEditApprovalStatus == 0) || (editReceiptObject != null && beforeEditApprovalStatus != 11)) && StringUtil.isNullOrEmpty(journalEntry.getEntryNumber())) {
                            String jeentryNumber = "";
                            int jeIntegerPart = 0;
                            String jeDatePrefix = "";
                            String jeDateAfterPrefix = "";
                            String jeDateSuffix = "";
                            String jeSeqFormatId = "";
                            HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                            JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                            JEFormatParams.put("modulename", "autojournalentry");
                            JEFormatParams.put(Constants.companyKey, companyid);
                            JEFormatParams.put("isdefaultFormat", true);
                            KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                            SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, journalEntry.getEntryDate());
                            jeentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                            jeIntegerPart = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                            jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                            jeDateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                            jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                            jeSeqFormatId = format.getID();
                            seqNumberMap.put(Constants.DOCUMENTID, journalEntry.getID());
                            seqNumberMap.put(Constants.companyKey, companyid);
                            seqNumberMap.put(Constants.SEQUENCEFORMATID, jeSeqFormatId);
                            if (approvalStatusLevel == Constants.APPROVED_STATUS_LEVEL) {
                                JENumBer = accJournalEntryobj.UpdateJournalEntry(seqNumberMap);
                                journalEntry.setEntryNumber(jeentryNumber);
                                journalEntry.setSeqnumber(jeIntegerPart);
                                journalEntry.setDatePreffixValue(jeDatePrefix);
                                journalEntry.setDateAfterPreffixValue(jeDateAfterPrefix);
                                journalEntry.setDateSuffixValue(jeDateSuffix);
                                receipt.setJournalEntry(journalEntry);
                            }
                        }
                        boolean isFromOtherSource = paramJobj.optBoolean("isFromOtherSource", false);
                        if (!sequenceformat.equals("NA") && editReceiptObject == null && !isFromOtherSource) {
                            boolean seqformat_oldflag = StringUtil.getBoolean(paramJobj.optString("seqformat_oldflag"));
                            String nextAutoNo = "";
                            int nextAutoNoInt = 0;
                            String datePrefix = "";
                            String dateafterPrefix = "";
                            String dateSuffix = "";
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            if (seqformat_oldflag) {
                                nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_RECEIPT, sequenceformat);
                                seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextAutoNo);
                            } else {
//                                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_RECEIPT, sequenceformat, seqformat_oldflag, receipt.getJournalEntry().getEntryDate());
                                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_RECEIPT, sequenceformat, seqformat_oldflag, receipt.getCreationDate());
                                nextAutoNo = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                                nextAutoNoInt = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                                datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                                dateafterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                                dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                            }

                            seqNumberMap.put(Constants.DOCUMENTID, receipt.getID());
                            seqNumberMap.put(Constants.companyKey, companyid);
                            seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                            billno = accReceiptDAOobj.UpdateReceiptEntry(seqNumberMap);
                            receipt.setReceiptNumber(nextAutoNo);
                            receipt.setSeqnumber(nextAutoNoInt);
                            receipt.setDatePreffixValue(datePrefix);
                            receipt.setDateAfterPreffixValue(dateafterPrefix);
                            receipt.setDateSuffixValue(dateSuffix);
                        }

                        /*
                         * saving linking information in Receive Payment & Sales
                         * Invoice If Payment is made against Sales Invoice
                         */
                        if (invoicesAgainstParticularCustomer.length() > 0) {
                            JSONArray drAccArr = invoicesAgainstParticularCustomer;
                            for (int i = 0; i < drAccArr.length(); i++) {
                                JSONObject jobj1 = drAccArr.getJSONObject(i);
                                String invoiceid = jobj1.getString("documentid");
                                KwlReturnObject cnResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
                                Invoice invoice = (Invoice) cnResult.getEntityList().get(0);

                                /*
                                 * Method is used to save linking informatio of
                                 * Received Payment
                                 *
                                 * when linking with Debit Note
                                 */
                                if (invoice != null) {
                                    saveLinkingInformationOfPaymentWithSalesInvoice(invoice, receipt, billno);
                                }
                            }

                        }

                        if (!StringUtil.isNullOrEmpty(paramJobj.optString("accid"))) {
                            //params to send to get billing address
                            HashMap<String, Object> addressParams = new HashMap<String, Object>();
                            addressParams.put(Constants.companyKey, companyid);
                            addressParams.put("isDefaultAddress", true);
                            addressParams.put("isBillingAddress", true);
                            if (receipt.getPaymentWindowType() == Constants.Receive_Payment_from_Vendor) {
                                addressParams.put("vendorid", paramJobj.optString("accid"));
                                VendorAddressDetails vendorAddressDetail = accountingHandlerDAOobj.getVendorAddressObj(addressParams);
                               // custVenEmailId = vendorAddressDetail != null ? vendorAddressDetail.getEmailID() : "";

                            } else if (receipt.getPaymentWindowType() == Constants.Receive_Payment_from_Customer) {
                                addressParams.put("customerid", paramJobj.optString("accid"));
                                CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                               // custVenEmailId = customerAddressDetails != null ? customerAddressDetails.getEmailID() : "";
                            }
                        }
                        txnManager.commit(AutoNoPayStatus);

                        /*
                        Added to get pending payment receipt while making bulk payment
                        */
                        if (approvalStatusLevel != 11) {
                            pendingpaymentNumber += billno + " ,";
                        } else {
                        journalEntryNumber += JENumBer + " ,";
                        paymentNumber += billno + " ,";
                        }
                    }
                } catch (Exception ex) {
                    issuccess=false;
                    if (AutoNoPayStatus != null) {
                        txnManager.rollback(AutoNoPayStatus);
                    }
                    deleteTemporaryInvoicesEntries(invoicesAgainstParticularCustomer, companyid);
                    accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);
                    Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
                }

                //========Code for Rounding JE Started=============
                if (invoicesAgainstParticularCustomer.length() > 0) {
                    TransactionStatus roundingJEStatus = null;
                    DefaultTransactionDefinition def2 = new DefaultTransactionDefinition();
                    def2.setName("RoundingJE_Tx");
                    def2.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    roundingJEStatus = txnManager.getTransaction(def2);
                    try {
                        JSONArray drAccArr = invoicesAgainstParticularCustomer;
                        for (int i = 0; i < drAccArr.length(); i++) {
                            JSONObject jobj1 = drAccArr.getJSONObject(i);
                            String invoiceid = jobj1.getString("documentid");
                            KwlReturnObject cnResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
                            Invoice invoice = (Invoice) cnResult.getEntityList().get(0);
                            if (invoice != null) {
                                paramJobj.put("salesInvoiceObj", invoice);
                                paramJobj.put("isEdit", isEdit);
                                paramJobj.put("receiptnumber", billno);
                                postRoundingJEOnReceiptSave(paramJobj);
                            }
                        }
                        txnManager.commit(roundingJEStatus);
                    } catch (JSONException | ServiceException | SessionExpiredException | AccountingException | TransactionException ex) {
                        issuccess=false;
                        if (roundingJEStatus != null) {
                            txnManager.rollback(roundingJEStatus);
                        }
                        Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                //========Code for Rounding JE Ended=============
                if (linkingDocumentNumber.length() > 0) {
                    linkingDocumentNumber = linkingDocumentNumber.substring(0, linkingDocumentNumber.length() - 1);
                }

                auditTrailObj.insertAuditLog(AuditAction.RECEIVE_PAYMENT, "User " + paramJobj.optString(Constants.userfullname) + " has made a Customer Receive Payment " + billno + " by Linking to Sales Invoice(s) " + linkingDocumentNumber + "", auditRequestParams, receipt.getID());
            }
            
//               
//              msg = messageSource.getMessage("acc.payreceipt.save", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + paymentNumber.substring(0, paymentNumber.length() - 1) + ",</b>" + messageSource.getMessage("acc.field.JENo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + journalEntryNumber.substring(0, journalEntryNumber.length() - 1) + "" + jeEntryNo + "</b>";   //"Receipt has been saved successfully";
            if (!pendingpaymentNumber.equalsIgnoreCase("")) {
                /*
                 pending for approval case
                 */
                if (!journalEntryNumber.equalsIgnoreCase("")) {
                    /*
                     This case will be executed while making bulk payement 
                     if one  receipt falls in pending approval and other doesn't fall in pending approval
                     */
                    msg = messageSource.getMessage("acc.payreceipt.save", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + paymentNumber.substring(0, paymentNumber.length() - 1) + ",</b>" + messageSource.getMessage("acc.field.JENo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + journalEntryNumber.substring(0, journalEntryNumber.length() - 1) + "" + jeEntryNo + "</b>";   //"Receipt has been saved successfully";
                } else {
                    /*
                     This case will be executed while making bulk payement 
                     if all receipt falls in pending approval
                     */
                    msg = messageSource.getMessage("acc.payreceipt.save", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));   //"Receipt has been saved successfully";
                }
                String pendingforapproval = "<br/> and " + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + pendingpaymentNumber.substring(0, pendingpaymentNumber.length() - 1) + "</b> " + messageSource.getMessage("acc.field.ispendingforApproval", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
                msg += pendingforapproval;
            } else {
                /*
                 This case will be executed while making bulk payement 
                 none of receipt falls in pending approval 
                 */
                msg = messageSource.getMessage("acc.payreceipt.save", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + paymentNumber.substring(0, paymentNumber.length() - 1) + ",</b>" + messageSource.getMessage("acc.field.JENo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + journalEntryNumber.substring(0, journalEntryNumber.length() - 1) + "" + jeEntryNo + "</b>";   //"Receipt has been saved successfully";
            }
//            msg = messageSource.getMessage("acc.receipt.save", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + paymentNumber.substring(0, paymentNumber.length() - 1) + ",</b>" + messageSource.getMessage("acc.field.JENo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + journalEntryNumber.substring(0, journalEntryNumber.length() - 1) + "" + jeEntryNo + "</b>";   //"Receipt has been saved successfully";

        } catch (SessionExpiredException ex) {
            issuccess=false;
            if (status != null) {
                txnManager.rollback(status);
            }
            deleteTemporaryInvoicesEntries(invoicesAgainstParticularCustomer, companyid);
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);
            } catch (Exception ee) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ee);
            }
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            issuccess=false;
            if (status != null) {
                txnManager.rollback(status);
            }
            deleteTemporaryInvoicesEntries(invoicesAgainstParticularCustomer, companyid);
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);
            } catch (Exception ee) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ee);
            }
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (AccountingException AE) {
            issuccess=false;
            if (status != null && !exceptionChequeOrInv) {
                txnManager.rollback(status);
            }
            deleteTemporaryInvoicesEntries(invoicesAgainstParticularCustomer, companyid);
            try {
                status = txnManager.getTransaction(def);
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);
                txnManager.commit(status);
            } catch (Exception ee) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ee);
            }
            msg = "" + AE.getMessage();
            if (AE.getMessage() == null) {
                msg = AE.getCause().getMessage();
            }
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, AE);
        } catch (Exception ex) {
            issuccess=false;
            if (status != null) {
                txnManager.rollback(status);
            }
            deleteTemporaryInvoicesEntries(invoicesAgainstParticularCustomer, companyid);
            try {
                accCommonTablesDAO.deleteTransactionInTemp(entryNumber, companyid, Constants.Acc_Receive_Payment_ModuleId);
            } catch (Exception ee) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ee);
            }
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                hashMap.put("jobj", jobj);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return hashMap;
    }
       

    public void setValuesForAuditTrialMessage(Receipt oldgrd, Map<String, Object> oldgreceipt, Map<String, Object> newAuditKey) throws SessionExpiredException {
        try {
            if (oldgrd != null) {
                //Receipt Number Change
                oldgreceipt.put(Constants.ReceiptNumber, oldgrd.getReceiptNumber());
                newAuditKey.put(Constants.ReceiptNumber, "Receipt Number");
                //Received From Change
                oldgreceipt.put(Constants.ReceivedFrom, oldgrd.getReceivedFrom() != null ? oldgrd.getReceivedFrom().getValue() : "");
                newAuditKey.put(Constants.ReceivedFrom, "Received From");
                //Creation Date
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
            Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * Update payment advance amount due on receipt with refund entry delete
     */
    public void updateReceiptAdvancePaymentAmountDue(Receipt receipt, String companyId,int beforeEditApprovalStatus) throws JSONException, ServiceException {
        if (receipt != null) {
            Set<ReceiptAdvanceDetail> advanceDetails = receipt.getReceiptAdvanceDetails();
            if (receipt.getReceiptAdvanceDetails() != null && !advanceDetails.isEmpty()) {
                for (ReceiptAdvanceDetail adv : advanceDetails) {
                    if (adv.getAdvancedetailid() != null) {
                        /*
                         * amountReceived with positive sign as we have to add
                         * amount on delete transaction
                         */
                        double amountReceived = authHandler.round(adv.getAmount() / adv.getExchangeratefortransaction(), companyId);
                        if (receipt.getApprovestatuslevel() == Constants.APPROVED_STATUS_LEVEL || beforeEditApprovalStatus==Constants.APPROVED_STATUS_LEVEL) {
                            accountingHandlerDAOobj.updateAdvanceDetailAmountDueOnAmountReceived(adv.getAdvancedetailid(), amountReceived);
                        }
                    }
                    if (adv != null && adv.getRevalJeId() != null) { //Deleting Refund Revaluation JE 
                        accJournalEntryobj.deleteJEDtails(adv.getRevalJeId(), companyId);
                        accJournalEntryobj.deleteJE(adv.getRevalJeId(), companyId);
                    }
                }
                accReceiptDAOobj.deleteReceiptAdvanceDetails(receipt.getID(), receipt.getCompany().getCompanyID());
            }
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

            int n = Integer.parseInt(snumber.substring(0, 15));
            int fractions = Integer.parseInt(snumber.split("\\.").length != 0 ? snumber.split("\\.")[1] : "0");
            if (n == 0) {
                return "Zero";
            }
            String arr1[] = {"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
            String arr2[] = {"Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
            String unit[] = {"Arab", "Crore", "Lakh", "Thousand", "Hundred", ""};
            int factor[] = {1000000000, 10000000, 100000, 1000, 100, 1};
            String answer = "", paises = "";
            if (n < 0) {
                answer = "Minus";
                n = -n;
            }
            int quotient, units, tens;
            for (int i = 0; i < factor.length; i++) {
                quotient = n / factor[i];
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
            Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void saveLinkingInformationOfPaymentWithDN(DebitNote debitnote, Receipt receipt, String paymentNo) throws ServiceException {

        try {
            /*
             * Save Debit Note Linking & Receipt Paymnet Linking information in
             * linking table
             */
            HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
            requestParamsLinking.put("linkeddocid", receipt.getID());
            requestParamsLinking.put("docid", debitnote.getID());
            requestParamsLinking.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
            requestParamsLinking.put("linkeddocno", paymentNo);
            requestParamsLinking.put("sourceflag", 0);
            KwlReturnObject result = accReceiptDAOobj.updateEntryInDebitNoteLinkingTable(requestParamsLinking);

            requestParamsLinking.put("linkeddocid", debitnote.getID());
            requestParamsLinking.put("docid", receipt.getID());
            requestParamsLinking.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
            requestParamsLinking.put("linkeddocno", debitnote.getDebitNoteNumber());
            requestParamsLinking.put("sourceflag", 1);
            result = accReceiptDAOobj.saveReceiptLinking(requestParamsLinking);

        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public void saveLinkingInformationOfPaymentWithSalesInvoice(Invoice invoice, Receipt receipt, String paymentNo) throws ServiceException {

        try {
            /*
             * Save Debit Note Linking & Receipt Paymnet Linking information in
             * linking table
             */
            HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
            requestParamsLinking.put("linkeddocid", receipt.getID());
            requestParamsLinking.put("docid", invoice.getID());
            requestParamsLinking.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
            requestParamsLinking.put("linkeddocno", paymentNo);
            requestParamsLinking.put("sourceflag", 0);
            KwlReturnObject result = accInvoiceDAOObj.saveInvoiceLinking(requestParamsLinking);

            requestParamsLinking.put("linkeddocid", invoice.getID());
            requestParamsLinking.put("docid", receipt.getID());
            requestParamsLinking.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
            requestParamsLinking.put("linkeddocno", invoice.getInvoiceNumber());
            requestParamsLinking.put("sourceflag", 1);
            result = accReceiptDAOobj.saveReceiptLinking(requestParamsLinking);

        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    
 @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void saveLinkingInformationOfPaymentWithSalesInvoiceNew(Invoice invoice, Receipt receipt, String paymentNo) throws ServiceException {

        try {
            /*
             * Save Debit Note Linking & Receipt Paymnet Linking information in
             * linking table
             */
            HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
            requestParamsLinking.put("linkeddocid", receipt.getID());
            requestParamsLinking.put("docid", invoice.getID());
            requestParamsLinking.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
            requestParamsLinking.put("linkeddocno", paymentNo);
            requestParamsLinking.put("sourceflag", 0);
            KwlReturnObject result = accInvoiceDAOObj.saveInvoiceLinking(requestParamsLinking);

            requestParamsLinking.put("linkeddocid", invoice.getID());
            requestParamsLinking.put("docid", receipt.getID());
            requestParamsLinking.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
            requestParamsLinking.put("linkeddocno", invoice.getInvoiceNumber());
            requestParamsLinking.put("sourceflag", 1);
            result = accReceiptDAOobj.saveReceiptLinking(requestParamsLinking);

        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    } 
  
  
    public Receipt createReceiptObject(JSONObject paramJobj, Receipt editReceiptObject) throws SessionExpiredException, ServiceException, AccountingException {
        KwlReturnObject result = null;
        Customer cust = null;
        Vendor vend = null;
        Receipt receiptObject = null;
        try {
            String sequenceformat = paramJobj.getString(Constants.sequenceformat) != null ? paramJobj.getString(Constants.sequenceformat) : "NA";
            int seqNumber = paramJobj.optString(Constants.SEQNUMBER, null) != null ? Integer.parseInt(paramJobj.optString(Constants.SEQNUMBER).toString()) : 0;
            String companyid = paramJobj.optString(Constants.companyKey);
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate"));
            DateFormat df = authHandler.getDateFormatter(paramJobj);
            Date creationDate = null;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("creationdate",null))) {
                try {
                   //for squats & pos 
                    if (paramJobj.optBoolean(Constants.isSquatTransaction)||paramJobj.optBoolean(Constants.isdefaultHeaderMap)) {
                        DateFormat squatsdf = authHandler.getDateOnlyFormat();
                        creationDate = squatsdf.parse(paramJobj.getString("creationdate"));
                    } else {
                        creationDate = df.parse(paramJobj.getString("creationdate"));
                    }
                } catch (Exception ex) {
                    throw ServiceException.FAILURE("createReceiptObject : " + ex.getMessage(), ex);
                }
            }
            String entryNumber = paramJobj.optString("no");
            String methodid = paramJobj.optString("pmtmethod");
            double PaymentCurrencyToPaymentMethodCurrencyRate = StringUtil.getDouble(paramJobj.optString("paymentCurrencyToPaymentMethodCurrencyExchangeRate","1"));
            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap, false)) {
                sessionHandlerImpl sess = new sessionHandlerImpl();
                sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
            }
            int receiptType = StringUtil.getInteger(paramJobj.optString("receipttype"));
            int actualReceiptType = StringUtil.getInteger(paramJobj.optString("actualReceiptType") != null ? paramJobj.optString("actualReceiptType") : "0");
            String payDetailID = null;
            HashMap receipthm = new HashMap();
            boolean ismanydbcr = StringUtil.getBoolean(paramJobj.optString("ismanydbcr"));
            receipthm.put("ismanydbcr", ismanydbcr);
            receipthm.put("receipttype", receiptType);
            receipthm.put("actualReceiptType", actualReceiptType);
            double bankCharges = 0;
            double bankInterest = 0;
            String bankChargesAccid = paramJobj.optString("bankChargesCmb");
            String bankInterestAccid = paramJobj.optString("bankInterestCmb");

            String oldjeid = null;
            String Cardid = null;
            String oldChequeNo = "";

            String createdby = paramJobj.optString(Constants.useridKey);
            String modifiedby = paramJobj.optString(Constants.useridKey);
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("bankCharges",null)) && !StringUtil.isNullOrEmpty(bankChargesAccid)) {
                bankCharges = Double.parseDouble(paramJobj.optString("bankCharges","0"));
                receipthm.put("bankCharges", bankCharges);
                receipthm.put("bankChargesCmb", bankChargesAccid);
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("bankInterest",null)) && !StringUtil.isNullOrEmpty(bankInterestAccid)) {
                bankInterest = Double.parseDouble(paramJobj.optString("bankInterest","0"));
                receipthm.put("bankInterest", bankInterest);
                receipthm.put("bankInterestCmb", bankInterestAccid);
            }
            String paidToid = paramJobj.optString("paidToCmb",null);
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("paidToCmb",null)) && !StringUtil.isNullOrEmpty(paidToid)) {
                receipthm.put("paidToCmb", paidToid);
            }

            boolean isCustomer = false;
            boolean isVendor = false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("accid",null))) {
                KwlReturnObject custObj = accountingHandlerDAOobj.getObject(Customer.class.getName(), paramJobj.getString("accid"));
                if (custObj.getEntityList().get(0) != null) {
                    cust = (Customer) custObj.getEntityList().get(0);
                }
                if (cust != null) {
                    isCustomer = true;
                }


                KwlReturnObject vendObj = accountingHandlerDAOobj.getObject(Vendor.class.getName(), paramJobj.getString("accid"));
                if (vendObj.getEntityList().get(0) != null) {
                    vend = (Vendor) vendObj.getEntityList().get(0);
                }
                if (vend != null) {
                    isVendor = true;
                }
            }
            if (isCustomer) {
                receipthm.put("customerId", paramJobj.optString("accid"));
            } else if (isVendor) {
                receipthm.put("vendor", paramJobj.optString("accid"));
            }

            if (editReceiptObject != null) {// for edit case
                oldjeid = editReceiptObject.getJournalEntry().getID();
                JournalEntry jetemp = editReceiptObject.getJournalEntry();

                if (editReceiptObject.getPayDetail() != null) {
                    payDetailID = editReceiptObject.getPayDetail().getID();
                    if (editReceiptObject.getPayDetail().getCard() != null) {
                        Cardid = editReceiptObject.getPayDetail().getCard().getID();
                    }
                    if (editReceiptObject.getPayDetail().getCheque() != null) {
                        Cardid = editReceiptObject.getPayDetail().getCheque().getID();
                        oldChequeNo = editReceiptObject.getPayDetail().getCheque().getChequeNo();
                    }
                }
            }


            synchronized (this) { //this block is used to generate auto sequence number if number is not duplicate
                String nextAutoNo = "";
                String nextAutoNoInt = "";
                boolean isFromOtherSource=paramJobj.optBoolean("isFromOtherSource", false);             //if this service is called from other source like import than the sequence number should not be generated instead it will take Receipt number from file
                int count = 0;
                if (editReceiptObject != null) {
                    if (sequenceformat.equals("NA")) {
                        if (!entryNumber.equals(editReceiptObject.getReceiptNumber())) {
                            result = accReceiptDAOobj.getReceiptFromBillNo(entryNumber, companyid);
                            receipthm.put("entrynumber", entryNumber);
                            receipthm.put("autogenerated", entryNumber.equals(nextAutoNo));
                            count = result.getRecordTotalCount();
                        }
                        if (count > 0) {
                            throw new AccountingException(messageSource.getMessage("acc.field.Receiptnumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                        }
                    }
                } else {
                    if (!sequenceformat.equals("NA")) {
                        receipthm.put(Constants.SEQFORMAT, sequenceformat);
                        receipthm.put(Constants.SEQNUMBER, "");

                    }
                    if (sequenceformat.equals("NA")) {
                        receipthm.put("entrynumber", entryNumber);
                    } else {
                        if (isFromOtherSource) {        //this block will pass entry number from imported CSV.
                            receipthm.put("entrynumber", entryNumber);
                            receipthm.put(Constants.SEQNUMBER, seqNumber);
                            receipthm.put(Constants.SEQFORMAT, sequenceformat);
                        } else {
                        receipthm.put("entrynumber", "");
                    }
                    }

                    receipthm.put("autogenerated", sequenceformat.equals("NA") ? false : true);
                }
            }
            if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Receive_Payment_ModuleId, entryNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
            }


            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJobj.optString(Constants.currencyKey));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid = (paramJobj.getString(Constants.currencyKey) == null ? currency.getCurrencyID() : paramJobj.getString(Constants.currencyKey));

            receipthm.put(Constants.currencyKey, currencyid);
            receipthm.put("nonRefundable", !StringUtil.isNullOrEmpty(paramJobj.optString("NonRefundable")));
            receipthm.put("externalCurrencyRate", externalCurrencyRate);
            receipthm.put("memo", paramJobj.optString("memo"));
            receipthm.put(Constants.companyKey, company.getCompanyID());
            receipthm.put("createdby", createdby);
            receipthm.put("modifiedby", modifiedby);
            receipthm.put("creationDate", creationDate);
            receipthm.put(Constants.Checklocktransactiondate, paramJobj.optString("creationdate"));//ERP-16800-Without parsing date
            receipthm.put("createdon", createdon);
            receipthm.put("updatedon", updatedon);
            receipthm.put("isLinkedToClaimedInvoice", !StringUtil.isNullOrEmpty(paramJobj.optString("isLinkedToClaimedInvoice")) ? Boolean.parseBoolean(paramJobj.optString("isLinkedToClaimedInvoice")) : false);
            receipthm.put("PaymentCurrencyToPaymentMethodCurrencyRate", PaymentCurrencyToPaymentMethodCurrencyRate);
            //By default web application
            receipthm.put(Constants.generatedSource, (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.generatedSource, null))) ? Integer.parseInt(paramJobj.optString(Constants.generatedSource, Constants.RECORD_WEB_Application)) : null);
            if (editReceiptObject != null) {
                receipthm.put("receiptid", editReceiptObject.getID());
            }
            receiptObject = accReceiptDAOobj.getReceiptObj(receipthm);

        } catch (AccountingException ex) {
            throw new AccountingException(ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(accReceiptControllerNew.class.getName()).log(Level.SEVERE, null, ex);
        }
        return receiptObject;
    }

    public void saveReevalJournalEntryObjects(JSONObject paramJobj, JSONArray detailsJSONArray, Receipt receipt, int type, String oldRevaluationJE, Map<String, Object> counterMap) throws SessionExpiredException, ServiceException, AccountingException {
        try {
            if (detailsJSONArray.length() > 0) {
                double finalAmountReval = 0;
                String basecurrency = paramJobj.optString(Constants.globalCurrencyKey);
                KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), receipt.getCompany().getCompanyID());
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

                finalAmountReval = getReevalJournalEntryAmount(paramJobj, detailsJSONArray, receipt, type);
                if (finalAmountReval != 0) {
                    /**
                     * added transactionID and transactionModuleID to Realised
                     * JE.
                     */
                    counterMap.put("transactionModuleid", receipt.isIsOpeningBalenceReceipt() ? Constants.Acc_opening_Receipt : Constants.Acc_Receive_Payment_ModuleId);
                    counterMap.put("transactionId", receipt.getID());
                    String revaljeid = PostJEFORReevaluation(paramJobj, finalAmountReval, receipt.getCompany().getCompanyID(), preferences, basecurrency, oldRevaluationJE, counterMap);
                    receipt.setRevalJeId(revaljeid);
                    }
                }
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
    }

    public double getReevalJournalEntryAmount(JSONObject paramJobj, JSONArray detailsJSONArray, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        double finalAmountReval = 0;
        try {
            if (detailsJSONArray.length() > 0) {
                String basecurrency = paramJobj.optString(Constants.globalCurrencyKey);
                String companyid = paramJobj.optString(Constants.companyKey);
                for (int i = 0; i < detailsJSONArray.length(); i++) {
                    JSONObject jobj = detailsJSONArray.getJSONObject(i);
                    double ratio = 0;
                    double amountReval = 0;
                    Date tranDate = null;
                    double exchangeRate = 0.0;
                    double exchangeRateReval = 0.0;
                    double amountdue = jobj.optDouble("enteramount",0.0);
                    double exchangeratefortransaction = jobj.optDouble("exchangeratefortransaction", 1.00);
                    HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
                    GlobalParams.put(Constants.companyKey, companyid);
                    GlobalParams.put(Constants.globalCurrencyKey, basecurrency);
                    GlobalParams.put("dateformat", authHandler.getDateFormatter(paramJobj));
                    Date creationDate = authHandler.getDateOnlyFormat().parse(paramJobj.getString("creationdate"));
                    KwlReturnObject result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("documentid"));
                    Invoice invoice = (Invoice) result.getEntityList().get(0);
                    boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                    tranDate = invoice.getCreationDate();
                    if (!invoice.isNormalInvoice()) {
                        exchangeRate = invoice.getExchangeRateForOpeningTransaction();
                        exchangeRateReval = exchangeRate;
                    } else {
                        exchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
                        exchangeRateReval = exchangeRate;
                    }
                    
                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                    invoiceId.put("invoiceid", invoice.getID());
                    invoiceId.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
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
                    if (invoice.getCurrency() != null) {
                        currid = invoice.getCurrency().getCurrencyID();
                    }
                    //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
                    KwlReturnObject bAmt = null;
                    if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
                    } else {
                        bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
                    }

                    double oldrate = (Double) bAmt.getEntityList().get(0);
                    //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
                    if (revalueationHistory == null && isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
                    } else {
                        bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
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

    public String PostJEFORReevaluation(JSONObject paramJobj, double finalAmountReval, String companyid, CompanyAccountPreferences preferences, String basecurrency, String oldRevaluationJE, Map<String, Object> counterMap) {
        String jeid = "";
        try {
            String jeentryNumber = "";
            String jeSeqFormatId = "";
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            boolean jeautogenflag = false;
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
                JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                JEFormatParams.put(Constants.companyKey, companyid);
                JEFormatParams.put("isdefaultFormat", true);

                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                jeautogenflag = true;
                if (StringUtil.isNullOrEmpty(oldRevaluationJE)) {
                    String nextAutoNoTemp = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    int sequence = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                    jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    sequence = sequence + counter;
                    String number = "" + sequence;
                    String action = "" + (sequence - counter);
                    nextAutoNoTemp.replaceAll(action, number);
                    jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
                    jeSeqFormatId = format.getID();
                    jeIntegerPart = String.valueOf(sequence);
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
            jeDataMapReval.put(Constants.companyKey, companyid);
            jeDataMapReval.put(Constants.currencyKey, basecurrency);
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
            jedjsonreval.put(Constants.companyKey, companyid);
            jedjsonreval.put("amount", finalAmountReval);//rateDecreased?(-1*amountDiff):
            jedjsonreval.put("accountid", preferences.getForeignexchange().getID());
            jedjsonreval.put("debit", creditDebitFlag ? true : false);
            jedjsonreval.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjsonreval);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetailsReval.add(jed);
            /*
             * Featching Custom field/Dimension Data from Company prefrences.
             */
            String customfield = "";
            String lineleveldimensions = "";
            KwlReturnObject result = accJournalEntryobj.getRevaluationJECustomData(companyid);
            RevaluationJECustomData revaluationJECustomData = (result != null && result.getEntityList().size() > 0 && result.getEntityList().get(0) != null) ? (RevaluationJECustomData) result.getEntityList().get(0) : null;
            if (revaluationJECustomData != null) {
                customfield = revaluationJECustomData.getCustomfield();
                lineleveldimensions = revaluationJECustomData.getLineleveldimensions();
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
            jedjsonreval.put(Constants.companyKey, companyid);
            jedjsonreval.put("srno", jedetailsReval.size() + 1);
            jedjsonreval.put("amount", finalAmountReval);
            jedjsonreval.put("accountid", unrealised_accid);
            jedjsonreval.put("debit", creditDebitFlag ? false : true);
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
                customrequestParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
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
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
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
                customrequestParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put(Constants.companyKey, jed.getCompany().getCompanyID());
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    JSONObject jedjsonreval = new JSONObject();
                    jedjsonreval.put("accjedetailcustomdata", jed.getID());
                    jedjsonreval.put("jedid", jed.getID());
                    accJournalEntryobj.updateJournalEntryDetails(jedjsonreval);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccReceivePaymentModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public PayDetail getPayDetailObject(JSONObject paramJobj, Receipt editReceiptObject, Receipt receipt) throws SessionExpiredException, ServiceException, AccountingException {
        List list = new ArrayList();
        PayDetail pdetail = null;

        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            DateFormat df = authHandler.getDateOnlyFormatter(paramJobj);
            String currencyid = paramJobj.optString(Constants.currencyKey);
            String methodid = paramJobj.optString("pmtmethod");
            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap, false)) {
                sessionHandlerImpl sess = new sessionHandlerImpl();
                sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
            }
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (paramJobj.getString(Constants.currencyKey) == null ? currency.getCurrencyID() : paramJobj.getString(Constants.currencyKey));

            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            HashMap receipthm = new HashMap();
            receipthm.put("paymethodid", payMethod.getID());
            receipthm.put(Constants.companyKey, companyid);
            if (payMethod.getDetailType() != PaymentMethod.TYPE_CASH && paramJobj.optString("paydetail",null) != null) {

                JSONObject obj = new JSONObject(paramJobj.getString("paydetail"));
                if (payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
                    saveBankReconsilationDetails(paramJobj, receipt);
                    HashMap chequehm = new HashMap();
                    chequehm.put("chequeno", obj.optString("chequenumber"));
                    chequehm.put("companyId", companyid);
                    chequehm.put("createdFrom", 2);
                    chequehm.put("bankAccount", (payMethod.getAccount() != null) ? payMethod.getAccount().getID() : "");
                    chequehm.put("description", StringUtil.DecodeText(obj.optString("description")));
                    chequehm.put("bankname", StringUtil.DecodeText(obj.optString("paymentthrough")));
                    chequehm.put("duedate", df.parse(obj.getString("postdate")));
                    chequehm.put("bankmasteritemid", obj.optString("paymentthroughid", ""));
                    Map<String, Object> seqchequehm = new HashMap<>();
                    obj.put(Constants.companyKey, companyid);
                    String chequesequenceformat =  obj.optString(Constants.sequenceformat);
                    /**
                     * getNextChequeNumber method to generate next sequence number using
                     * sequence format,also saving the dateprefix and datesuffix in cheque table.
                     */
                    if (!StringUtil.isNullOrEmpty(chequesequenceformat) && !chequesequenceformat.equals("NA")) {    //SDP-10771
                        seqchequehm = accCompanyPreferencesObj.getNextChequeNumber(obj);
                    }
                  
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
                    receipthm.put("chequeid", cheque.getID());
                } else if (payMethod.getDetailType() == PaymentMethod.TYPE_CARD) {
                    HashMap cardhm = new HashMap();
                    cardhm.put("cardno", obj.getString("CardNo"));
                    cardhm.put("nameoncard", obj.getString("nameoncard"));
                    cardhm.put("expirydate", df.parse(obj.getString("expirydate")));
                    cardhm.put("cardtype", obj.getString("cardtype"));
                    cardhm.put("refno", obj.getString("refno"));
                    KwlReturnObject cdresult = accPaymentDAOobj.addCard(cardhm);
                    Card card = (Card) cdresult.getEntityList().get(0);
                    receipthm.put("cardid", card.getID());
                }
            }
            pdetail = accPaymentDAOobj.saveOrUpdatePayDetail(receipthm);
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return pdetail;
    }

    public void saveBankReconsilationDetails(JSONObject paramJobj, Receipt receipt) throws ServiceException {
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            DateFormat df = authHandler.getDateOnlyFormatter(paramJobj);
            String currencyid = paramJobj.optString(Constants.currencyKey);

            String detailsJsonString = paramJobj.optString("Details");;
            JSONArray jSONArray = new JSONArray(detailsJsonString);
            JSONObject obj = new JSONObject(paramJobj.optString("paydetail"));

            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            if (paramJobj.optString("oldjeid") != null && !StringUtil.isNullOrEmpty((String) paramJobj.optString("oldjeid"))) {
                Map<String, Object> delReqMap = new HashMap<String, Object>();
                delReqMap.put("oldjeid", paramJobj.optString("oldjeid"));
                delReqMap.put("companyId", companyid);
                deleteBankReconcilation(delReqMap);  //deleting bank reconsilation info 
            }
            boolean bankReconsilationEntry = obj.getString("paymentstatus") != null ? obj.getString("paymentstatus").equals("Cleared") : false;
            boolean isNotToReconcil = paramJobj.optBoolean("isNotToReconcil", false);
            if (bankReconsilationEntry && !isNotToReconcil) {
                String bankAccountId = payMethod.getAccount().getID();
                Date startDate = df.parse(df.format(Calendar.getInstance().getTime()));
                Date endDate = df.parse(df.format(Calendar.getInstance().getTime()));
                Date clearanceDate = df.parse(obj.getString("clearancedate"));

                HashMap<String, Object> globalParams = AccountingManager.getGlobalParamsJson(paramJobj);
                Map<String, Object> bankReconsilationMap = new HashMap<String, Object>();
                bankReconsilationMap.put("bankAccountId", bankAccountId);
                bankReconsilationMap.put("startDate", startDate);//dont know the significance so have just put current date 
                bankReconsilationMap.put("endDate", endDate);//dont know the significance so have just put current date
                bankReconsilationMap.put("clearanceDate", clearanceDate);
                bankReconsilationMap.put("endingAmount", 0.0);
                bankReconsilationMap.put("companyId", companyid);
                bankReconsilationMap.put("clearingamount", receipt.getDepositAmount());
                bankReconsilationMap.put(Constants.currencyKey, currencyid);
                bankReconsilationMap.put("details", jSONArray);
                bankReconsilationMap.put("receipt", receipt);
                bankReconsilationMap.put("ismultidebit", true);
                bankReconsilationMap.put("createdby", paramJobj.optString(Constants.useridKey));
                bankReconsilationMap.put("checkCount", 0);
                bankReconsilationMap.put("depositeCount", 1);   //As the discussion with Mayur B. and Sagar A. sir RP relates to deposit count
                saveBankReconsilation(bankReconsilationMap, globalParams);
                Map<String, Object> auditRequestParams = new HashMap<>();
                auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
                auditTrailObj.insertAuditLog(AuditAction.BANK_RECONCILIATION_ADDED, "User " + paramJobj.optString(Constants.userfullname) + " has reconciled " + receipt.getReceiptNumber(), auditRequestParams, companyid);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("savePayment : " + ex.getMessage(), ex);
        }
    }

    private void saveBankReconsilation(Map<String, Object> requestParams, Map<String, Object> globalParams) throws ServiceException, JSONException, UnsupportedEncodingException {
        HashMap<String, Object> brMap = new HashMap<String, Object>();
        KwlReturnObject crresult = accCurrencyobj.getCurrencyToBaseAmount(globalParams, (Double) requestParams.get("clearingamount"), (String) requestParams.get(Constants.currencyKey), (Date) requestParams.get("clearanceDate"), 0);
        double clearingAmount = (Double) crresult.getEntityList().get(0);

        if (requestParams.containsKey("oldjeid")) {
            deleteBankReconcilation(requestParams);
        }

        brMap.put("startdate", (Date) requestParams.get("startDate"));
        brMap.put("enddate", (Date) requestParams.get("endDate"));
        brMap.put("clearanceDate", (Date) requestParams.get("clearanceDate"));
        brMap.put("clearingamount", clearingAmount);
        brMap.put("endingamount", (Double) requestParams.get("endingAmount"));
        brMap.put("accountid", (String) requestParams.get("bankAccountId"));
        brMap.put(Constants.companyKey, (String) requestParams.get("companyId"));
        brMap.put("createdby", (String) requestParams.get("createdby"));
        brMap.put("checkCount", (Integer) requestParams.get("checkCount"));
        brMap.put("depositeCount", (Integer) requestParams.get("depositeCount"));
        KwlReturnObject brresult = accBankReconciliationObj.addBankReconciliation(brMap);
        BankReconciliation br = (BankReconciliation) brresult.getEntityList().get(0);
        String brid = br.getID();
        Receipt receipt = null;
        BillingReceipt breceipt = null;
        if (requestParams.containsKey("receipt")) {
            receipt = (Receipt) requestParams.get("receipt");
        } else {
            breceipt = (BillingReceipt) requestParams.get("breceipt");
        }
        JournalEntry entry = receipt != null ? receipt.getJournalEntry() : breceipt.getJournalEntry();
        Set details = entry.getDetails();
        Iterator iter = details.iterator();
        String accountName = "";
        while (iter.hasNext()) {
            JournalEntryDetail d = (JournalEntryDetail) iter.next();
            if (d.isDebit()) {
                continue;
            }
            accountName += d.getAccount().getName() + ", ";
        }
        accountName = accountName.substring(0, Math.max(0, accountName.length() - 2));
        HashSet hs = new HashSet();
        boolean isMultiDebit = Boolean.parseBoolean(requestParams.get("ismultidebit").toString());
        JSONArray jArr = (JSONArray) requestParams.get("details");
        double amount = 0;
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            if (jobj.optDouble("enteramount", 0) != 0) {
                KwlReturnObject crresult1 = accCurrencyobj.getCurrencyToBaseAmount(globalParams, (isMultiDebit ? jobj.optDouble("enteramount",0.0) : jobj.optDouble("payment",0.0)), jobj.optString(Constants.currencyKey), (Date) requestParams.get("clearanceDate"), 0);
                double amt = (Double) crresult1.getEntityList().get(0);
                if (jobj.optBoolean("debit", false)) {
                    amount -= amt;
                } else {
                    amount += amt;
                }
            }
        }
        HashMap<String, Object> brdMap = new HashMap<String, Object>();
        brdMap.put(Constants.companyKey, (String) requestParams.get("companyId"));
        brdMap.put("amount", amount);
        brdMap.put("jeid", entry.getID());
        brdMap.put("accountname", accountName);
        brdMap.put("debit", true);
        brdMap.put("brid", brid);
        KwlReturnObject brdresult = accBankReconciliationObj.addBankReconciliationDetail(brdMap);
        BankReconciliationDetail brd = (BankReconciliationDetail) brdresult.getEntityList().get(0);
        hs.add(brd);
    }

    public Set<JournalEntryDetail> journalEntryDetailCommonObjects(JSONObject paramJobj, JSONArray detailsJSONArray, JournalEntry journalEntry, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        double amount = 0;
        Set jedetails = new HashSet();
        try {
            JSONObject jedjson = null;
            JournalEntryDetail jed = null;
            Account dipositTo = null;
            String companyid = paramJobj.optString(Constants.companyKey);
            String currencyid = paramJobj.optString(Constants.currencyKey);
            HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put(Constants.companyKey, companyid);
            GlobalParams.put(Constants.globalCurrencyKey, currencyid);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (paramJobj.optString(Constants.currencyKey) == null ? currency.getCurrencyID() : paramJobj.optString(Constants.currencyKey));

            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();

            String jeid = null;
            if (journalEntry != null) {
                jeid = journalEntry.getID();
            }
            amount = Double.parseDouble(paramJobj.optString("amount","0.0"));
            // ERP-32829
            HashMap<String, Object> prefparams = new HashMap<>();
            prefparams.put("id", companyid);
            String transactionCurrency = receipt.getCurrency() != null ? receipt.getCurrency().getCurrencyID() : receipt.getCompany().getCurrency().getCurrencyID();
            if (amount != 0) {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put(Constants.companyKey, companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", amount);
                jedjson.put("accountid", dipositTo.getID());
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetails.add(jed);
                receipt.setDeposittoJEDetail(jed);
                receipt.setDepositAmount(amount);       // put amount excluding bank charges
                try {
                    KwlReturnObject baseAmount = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, amount, transactionCurrency, receipt.getCreationDate(), journalEntry.getExternalCurrencyRate());
                    double depositamountinbase = (Double) baseAmount.getEntityList().get(0);
                    depositamountinbase = authHandler.round(depositamountinbase, companyid);
                    receipt.setDepositamountinbase(depositamountinbase);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return jedetails;
    }

    public Set<JournalEntryDetail> journalEntryDetailCommonObjectsForBankCharges(JSONObject paramJobj, JournalEntry journalEntry, Receipt receipt, boolean bankCharge) throws SessionExpiredException, ServiceException, AccountingException {
        double amount = 0;
        Set jedetails = new HashSet();
        try {
            KwlReturnObject jedresult = null;
            JSONObject jedjson = null;
            JournalEntryDetail jed = null;
            JournalEntryDetail JEdeatilId = null;
            Account dipositTo = null;
            String companyid = paramJobj.optString(Constants.companyKey);
            double bankCharges = 0;
            double bankInterest = 0;
            String bankChargesAccid = paramJobj.optString("bankChargesCmb");
            String bankInterestAccid = paramJobj.optString("bankInterestCmb");
            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();

            String jeid = null;
            if (journalEntry != null) {
                jeid = journalEntry.getID();
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.getString("bankCharges")) && !StringUtil.isNullOrEmpty(bankChargesAccid)) {
                bankCharges = Double.parseDouble(paramJobj.getString("bankCharges"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.getString("bankInterest")) && !StringUtil.isNullOrEmpty(bankInterestAccid)) {
                bankInterest = Double.parseDouble(paramJobj.getString("bankInterest"));
            }
            // amount = Double.parseDouble(request.getParameter("amount"));
            //All Fore
            if (bankCharge && bankCharges != 0) {
                amount += bankCharges;
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put(Constants.companyKey, companyid);
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
                jedjson.put(Constants.companyKey, companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", bankInterest);
                jedjson.put("accountid", bankInterestAccid);
                jedjson.put("debit", true);    // receipt side charges
                jedjson.put("jeid", jeid);
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetails.add(jed);
            }
            if (amount != 0) {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put(Constants.companyKey, companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", amount);
                jedjson.put("accountid", dipositTo.getID());
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedjson.put("jedid", JEdeatilId.getID());
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetails.add(jed);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return jedetails;
    }

    public JournalEntry journalEntryObjectBankCharges(JSONObject paramJobj, Receipt editReceiptObject, int counter, boolean isBankCharge, boolean paymentWithoutJe, JournalEntry oldBankChargeJE, JournalEntry oldBankInterestJE) throws SessionExpiredException, ServiceException, AccountingException {
        JournalEntry journalEntry = null;
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            DateFormat df = authHandler.getDateOnlyFormatter(paramJobj);
            String currencyid = paramJobj.optString(Constants.currencyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (paramJobj.getString(Constants.currencyKey) == null ? currency.getCurrencyID() : paramJobj.getString(Constants.currencyKey));
            String methodid = paramJobj.optString("pmtmethod");
            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap, false)) {
                sessionHandlerImpl sess = new sessionHandlerImpl();
                sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
            }
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate"));
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);

            if (editReceiptObject == null || paymentWithoutJe) {
                jeDataMap.put("entrynumber", "");
                jeDataMap.put("autogenerated", true);
            } else if (editReceiptObject != null && oldBankChargeJE != null && isBankCharge) {
                JournalEntry entry = oldBankChargeJE;
                jeDataMap.put("entrynumber", entry.getEntryNumber());
                jeDataMap.put("autogenerated", entry.isAutoGenerated());
                jeDataMap.put(Constants.SEQFORMAT, entry.getSeqformat().getID());
                jeDataMap.put(Constants.SEQNUMBER, entry.getSeqnumber());
                jeDataMap.put(Constants.DATEPREFIX, entry.getDatePreffixValue());
                jeDataMap.put(Constants.DATESUFFIX, entry.getDateSuffixValue());
            } else if (editReceiptObject != null && oldBankInterestJE != null && !isBankCharge) {
                JournalEntry entry = oldBankInterestJE;
                jeDataMap.put("entrynumber", entry.getEntryNumber());
                jeDataMap.put("autogenerated", entry.isAutoGenerated());
                jeDataMap.put(Constants.SEQFORMAT, entry.getSeqformat().getID());
                jeDataMap.put(Constants.SEQNUMBER, entry.getSeqnumber());
                jeDataMap.put(Constants.DATEPREFIX, entry.getDatePreffixValue());
                jeDataMap.put(Constants.DATESUFFIX, entry.getDateSuffixValue());
            }
            jeDataMap.put("entrydate", df.parse(paramJobj.optString("creationdate")));
            jeDataMap.put(Constants.companyKey, company.getCompanyID());
            jeDataMap.put("memo", paramJobj.optString("memo"));
            jeDataMap.put(Constants.currencyKey, currencyid);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return journalEntry;
    }

    public Set<JournalEntryDetail> journalEntryDetailObject(JSONObject paramJobj, JSONArray detailsJSONArray, JournalEntry journalEntry, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        double amount = 0;
        StringBuffer billno = new StringBuffer();
        Set jedetails = new HashSet();
        try {
            Account dipositTo = null;
            double amountDiff = 0;
            boolean rateDecreased = false;
            String companyid = paramJobj.optString(Constants.companyKey);
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate"));
            String currencyid = paramJobj.optString(Constants.currencyKey);
            String methodid = paramJobj.optString("pmtmethod");
            boolean isDiscountAppliedOnPaymentTerms = paramJobj.optBoolean("isDiscountAppliedOnPaymentTerms", false);
            boolean isEdit = paramJobj.optBoolean("isEdit", false);
            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap, false)) {
                sessionHandlerImpl sess = new sessionHandlerImpl();
                sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
            }
          HashMap<String, Object> prefparams = new HashMap<>();
            prefparams.put("id", companyid);
            Object columnPref = kwlCommonTablesDAOObj.getRequestedObjectFields(IndiaComplianceCompanyPreferences.class, new String[]{"istaxonadvancereceipt"}, prefparams);
            boolean istaxonadvancereceipt = false;
            if (columnPref != null) {
                istaxonadvancereceipt = Boolean.parseBoolean(columnPref.toString());
            }
            double balaceAmount = 0.0;
            String accountIdComPreAdjRec = "";

            String jeid = null;
            if (journalEntry != null) {
                jeid = journalEntry.getID();
            }
            HashMap<String, JSONArray> jcustomarrayMap = new HashMap();
            receipt.setJcustomarrayMap(jcustomarrayMap);
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (paramJobj.getString(Constants.currencyKey) == null ? currency.getCurrencyID() : paramJobj.getString(Constants.currencyKey));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);

            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.getString("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();

            String accountId = "";
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("balaceAmount",null))) {
                balaceAmount = Double.parseDouble(paramJobj.getString("balaceAmount"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("accountIdComPreAdjRec",null))) {
                accountIdComPreAdjRec = paramJobj.getString("accountIdComPreAdjRec");
            }
            boolean isCustomer = Boolean.parseBoolean(paramJobj.optString("iscustomer"));
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("accid"))) {
                if (isCustomer) {
                    KwlReturnObject resultCVAccount = accountingHandlerDAOobj.getObject(Customer.class.getName(), paramJobj.optString("accid"));
                    if (!resultCVAccount.getEntityList().isEmpty()) {
                        Customer customer = (Customer) resultCVAccount.getEntityList().get(0);
                        accountId = customer.getAccount().getID();
                        receipt.setPaymentWindowType(1);
                    }
                } else {
                    KwlReturnObject resultCVAccount = accountingHandlerDAOobj.getObject(Vendor.class.getName(), paramJobj.optString("accid"));
                    if (!resultCVAccount.getEntityList().isEmpty()) {
                        Vendor vendor = (Vendor) resultCVAccount.getEntityList().get(0);
                        accountId = vendor.getAccount().getID();
                        receipt.setPaymentWindowType(2);
                    }
                }
            } else {
                receipt.setPaymentWindowType(3);
            }

            JSONArray jArr = new JSONArray();
            if (detailsJSONArray != null) {
                jArr = detailsJSONArray;
            }
            if (jArr.length() > 0 && type == Constants.PaymentAgainstInvoice) {
                amount = 0;
                /*
                 * SDP-4352/ERP-27671 gstOutputAccountId is id of 'GST Output'
                 * account.
                 */
                String gstOutputAccountId = "";
                KwlReturnObject accountReturnObject = accAccountDAOobj.getAccountFromName(companyid, Constants.MALAYSIAN_GST_OUTPUT_TAX);
                List accountResultList = accountReturnObject.getEntityList();
                if (!accountResultList.isEmpty()) {
                    gstOutputAccountId = ((Account) accountResultList.get(0)).getID();
                }
                for (int i = 0; i < jArr.length(); i++) { // Changed for New Customer/Vendor Removed account dependency
                    JSONObject jobj = jArr.getJSONObject(i);
                    String accountIdForGst = "";
                    double amountReceived = jobj.getDouble("enteramount");// amount in receipt currency
                    double amountReceivedConverted = jobj.getDouble("enteramount"); // amount in invoice currency
                    JSONArray jArray = new JSONArray();
                    jArray.put(jobj);
                    KwlReturnObject resultInvoice = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.optString("documentid"));
                    Invoice invoice = (Invoice) resultInvoice.getEntityList().get(0);
                    double adjustedRate = 1.0;
                    if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(invoice.getCurrency().getCurrencyID()) && !invoice.getCurrency().getCurrencyID().equals(receipt.getCurrency().getCurrencyID())) {
                        adjustedRate = jobj.optDouble("amountdue", 0) / jobj.optDouble("amountDueOriginal", 0);
                        amountReceivedConverted = amountReceived / adjustedRate;;
                        amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                    }
                    boolean isInvoiceIsClaimed = false;
                    isInvoiceIsClaimed = invoice.getBadDebtType() == Constants.Invoice_Claimed || invoice.getBadDebtType() == Constants.Invoice_Recovered;
                    double amountDiffforInv = authHandler.round(oldReceiptRowsAmount(paramJobj, jArray, currencyid, externalCurrencyRate), companyid);
                    rateDecreased = false;
                    if (amountDiffforInv < 0) {
                        rateDecreased = true;
                    }
                     amount += authHandler.round(jobj.getDouble("enteramount"), companyid);
                     double discountAmount = jobj.optDouble("discountname", 0.0);
                    if (!isInvoiceIsClaimed) {
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put(Constants.companyKey, companyid);
                        jedjson.put("amount", authHandler.round(((jobj.getDouble("enteramount") - amountDiffforInv)+discountAmount), companyid));
                        jedjson.put("accountid", invoice.getAccount() != null ? invoice.getAccount().getID() : accountId);
                        if (jobj.optDouble("gstCurrencyRate", 0.0) != 0.0) {
                            jedjson.put("gstCurrencyRate", jobj.optDouble("gstCurrencyRate", 0.0));
                            journalEntry.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate", 0.0));
                            jedjson.put("paymentType", type);
                        }
                        jedjson.put("forexGainLoss", amountDiffforInv);
                        jedjson.put("debit", false);
                        jedjson.put("jeid", jeid);
                        jedjson.put("description", jobj.optString("description"));
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);
                        String discountGivenGoesToAccountId=preferences.getDiscountGiven()!=null?preferences.getDiscountGiven().getID():"";
                        String discountMasterAccountId=(invoice.getTermid() != null && invoice.getTermid().getDiscountName()!=null) ? invoice.getTermid().getDiscountName().getAccount():discountGivenGoesToAccountId;
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
                            KwlReturnObject receiptDetailObj = accountingHandlerDAOobj.getObject(ReceiptDetail.class.getName(), jobj.optString("rowdetailid", ""));
                            ReceiptDetail receiptDetail = (ReceiptDetail) receiptDetailObj.getEntityList().get(0);
                            isDiscountFieldChanged = receiptDetail != null ? receiptDetail.isDiscountFieldEdited() : false;
                            if (jobj.optBoolean("isDiscountFieldChanged")) {
                                isDiscountFieldChanged = jobj.optBoolean("isDiscountFieldChanged");
                            }
                        } else {
                            isDiscountFieldChanged = jobj.optBoolean("isDiscountFieldChanged");
                        }
                        paramJobj.put("discountGivenGoesToAccountId",discountGivenGoesToAccountId);
                        paramJobj.put("discountMasterAccountId",discountMasterAccountId);
                        paramJobj.put("isDiscountFieldChanged", isDiscountFieldChanged);
                        if (isDiscountFieldChanged) {
                            discountAccountId = discountGivenGoesToAccountId;
                        }
                        
                        if (isDiscountAppliedOnPaymentTerms && !isInvoiceIsClaimed && (discountAmount > 0.0)) {
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put(Constants.companyKey, companyid);
                            jedjson.put("amount", authHandler.round(discountAmount, companyid));
                            jedjson.put("accountid", discountAccountId);

                            jedjson.put("debit", true);
                            jedjson.put("jeid", jeid);
                            jedjson.put("description", jobj.optString("description"));
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jedForDiscount = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jedForDiscount);
                        }
                        
                        if ((!jobj.optString("documentno").equalsIgnoreCase("undefined")) && (!jobj.optString("documentno").equalsIgnoreCase(""))) {
                            billno.append(jobj.optString("documentno") + ",");
                        }
                        jArr.getJSONObject(i).put("rowjedid", jed.getID());

                        JSONArray jcustomarray = new JSONArray();
                        if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.customfield, null))) {
                            jcustomarray = new JSONArray(jobj.optString(Constants.customfield, "[]"));
                            jcustomarrayMap.put(jed.getID(), jcustomarray);
                        }
                        jArr.getJSONObject(i).put("jedetail", jed.getID());

                        int count = jcustomarray.length();
                        if (count > 0 && jcustomarray.optJSONObject(0).length() > 0) {
                            /*
                             * If Custom field is tagged then posting additional
                             * jedetail for payment method against invoice
                             * tagged with custom field.
                             * ERP-33860
                             */
                                jedjson = new JSONObject();
                                jedjson.put("srno", jedetails.size() + 1);
                                jedjson.put(Constants.companyKey, companyid);
                                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                                jedjson.put("amount", authHandler.round((jobj.getDouble("enteramount") - amountDiffforInv), companyid));
                                jedjson.put("accountid", dipositTo.getID());
                                jedjson.put("debit", true);
                                jedjson.put("jeid", jeid);
                                jedjson.put(Constants.ISSEPARATED, true);
                                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed1 = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jedetails.add(jed1);
                                
                            if (isDiscountAppliedOnPaymentTerms && !isInvoiceIsClaimed && (discountAmount > 0.0)) {
                                jedjson = new JSONObject();
                                jedjson.put("srno", jedetails.size() + 1);
                                jedjson.put(Constants.companyKey, companyid);
                                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                                jedjson.put("amount", authHandler.round(discountAmount, companyid));
                                jedjson.put("accountid", discountAccountId);
                                jedjson.put("debit", true);         
                                jedjson.put("jeid", jeid);
                                jedjson.put(Constants.ISSEPARATED, true);
                                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed2 = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jedetails.add(jed2);
                                if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.customfield, null))) {
                                    jcustomarrayMap.put(jed2.getID(), jcustomarray);
                                }
                            }
                            if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.customfield, null))) {
                                jcustomarrayMap.put(jed1.getID(), jcustomarray);
                            }
                            jArr.getJSONObject(i).put("methodjedid", jed1.getID());
                        }
                        
                    } else {
                        // Logic of posting JE for claimed invoices
                        String badDebtRecoveredAccountId = extraCompanyPreferences.getGstBadDebtsRecoverAccount();
                        KwlReturnObject accObj = accountingHandlerDAOobj.getObject(Account.class.getName(), badDebtRecoveredAccountId);
                        Account account = (Account) accObj.getEntityList().get(0);
                        if (account == null) {
                            throw new AccountingException("GST Bad Debt Recover Account is not available in database");
                        }
                        double amountPaidForInvoice = jobj.getDouble("enteramount") - amountDiffforInv;
                        boolean isGlobalLevelTax = false;
                        boolean isOpeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                        if (isOpeningBalanceInvoice) {
                            isGlobalLevelTax = true;
                        } else {
                            if (invoice.getTaxEntry() != null && invoice.getTaxEntry().getAmount() > 0) {
                                isGlobalLevelTax = true;
                            }
                        }
                        double taxAmountInInvoiceCurrency = 0d;
                        double taxAmountReceivedInPaymentCurrency = 0d;
                        double taxAmountReceivedInInvoiceCurrency = 0d;
                        double totalTaxAmountReceivedInInvoiceCurrency = 0d;
                        double invoiceTotalAmountInInvoiceCurrency = authHandler.round(isOpeningBalanceInvoice ? invoice.getOriginalOpeningBalanceAmount() : invoice.getCustomerEntry().getAmount(), companyid);
                        double invoiceAmountExcludingTaxInPaymentCurrency = 0.0;
                        double invoiceAmountReceivedExcludingTaxInInvoiceCurrency = 0.0;
                        amountPaidForInvoice = authHandler.round(amountPaidForInvoice, companyid);
                        if (isGlobalLevelTax) {
                            Tax gloabLevelTax = invoice.getTax();
                            if (invoice.isIsOpeningBalenceInvoice()) {
                                accountIdForGst = gstOutputAccountId;
                            } else {
                                accountIdForGst = gloabLevelTax.getAccount().getID();
                            }
                            taxAmountInInvoiceCurrency = authHandler.round(isOpeningBalanceInvoice ? invoice.getTaxamount() : invoice.getTaxEntry().getAmount(), companyid);
                            taxAmountReceivedInPaymentCurrency = (taxAmountInInvoiceCurrency * amountPaidForInvoice) / invoiceTotalAmountInInvoiceCurrency;
                            taxAmountReceivedInPaymentCurrency = authHandler.round(taxAmountReceivedInPaymentCurrency, companyid);
                            taxAmountReceivedInInvoiceCurrency = (taxAmountInInvoiceCurrency * amountReceivedConverted) / invoiceTotalAmountInInvoiceCurrency;
                            taxAmountReceivedInInvoiceCurrency = authHandler.round(taxAmountReceivedInInvoiceCurrency, companyid);
                            totalTaxAmountReceivedInInvoiceCurrency += taxAmountReceivedInInvoiceCurrency;
                            invoiceAmountExcludingTaxInPaymentCurrency = amountPaidForInvoice - taxAmountReceivedInPaymentCurrency;

                            JSONObject jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put(Constants.companyKey, companyid);
                            jedjson.put("accountid", badDebtRecoveredAccountId);
                            jedjson.put("amount", invoiceAmountExcludingTaxInPaymentCurrency);
                            jedjson.put("debit", false);
                            jedjson.put("jeid", jeid);
                            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);

                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put(Constants.companyKey, companyid);
                            jedjson.put("accountid", accountIdForGst);
                            jedjson.put("amount", taxAmountReceivedInPaymentCurrency);
                            jedjson.put("debit", false);
                            jedjson.put("jeid", jeid);
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jedForRecoveryJe = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jedForRecoveryJe);

                            jArr.getJSONObject(i).put("rowjedid", jedForRecoveryJe.getID());
                            if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.customfield, "[]"))) {
                                JSONArray jcustomarray = new JSONArray(jobj.optString(Constants.customfield, "[]"));
                                jcustomarrayMap.put(jedForRecoveryJe.getID(), jcustomarray);
                            }
                            jArr.getJSONObject(i).put("jedetail", jedForRecoveryJe.getID());

                        } else {
                            JSONObject jedjson = new JSONObject();
                            invoiceAmountExcludingTaxInPaymentCurrency = amountPaidForInvoice;
                            for (InvoiceDetail details : invoice.getRows()) {
                                taxAmountInInvoiceCurrency = details.getRowTaxAmount()+details.getRowTermTaxAmount();
                                taxAmountReceivedInPaymentCurrency = (taxAmountInInvoiceCurrency * amountPaidForInvoice) / invoiceTotalAmountInInvoiceCurrency;
                                taxAmountReceivedInPaymentCurrency = authHandler.round(taxAmountReceivedInPaymentCurrency, companyid);
                                taxAmountReceivedInInvoiceCurrency = (taxAmountInInvoiceCurrency * amountReceivedConverted) / invoiceTotalAmountInInvoiceCurrency;
                                taxAmountReceivedInInvoiceCurrency = authHandler.round(taxAmountReceivedInInvoiceCurrency, companyid);
                                totalTaxAmountReceivedInInvoiceCurrency += taxAmountReceivedInInvoiceCurrency;
                                invoiceAmountExcludingTaxInPaymentCurrency -= taxAmountReceivedInPaymentCurrency;

                                jedjson = new JSONObject();
                                jedjson.put("srno", jedetails.size() + 1);
                                jedjson.put(Constants.companyKey, companyid);
                                jedjson.put("accountid", details.getTax().getAccount().getID());
                                jedjson.put("amount", taxAmountReceivedInPaymentCurrency);
                                jedjson.put("debit", false);
                                jedjson.put("jeid", jeid);
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jedetails.add(jed);
                            }
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put(Constants.companyKey, companyid);
                            jedjson.put("accountid", badDebtRecoveredAccountId);
                            jedjson.put("amount", invoiceAmountExcludingTaxInPaymentCurrency);
                            jedjson.put("debit", false);
                            jedjson.put("jeid", jeid);
                            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jedForRecoveryJe = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jedForRecoveryJe);

                            jArr.getJSONObject(i).put("rowjedid", jedForRecoveryJe.getID());
                            if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.customfield, null))) {
                                JSONArray jcustomarray = new JSONArray(jobj.optString(Constants.customfield, "[]"));
                                jcustomarrayMap.put(jedForRecoveryJe.getID(), jcustomarray);
                            }
                            jArr.getJSONObject(i).put("jedetail", jedForRecoveryJe.getID());
                        }
                        invoiceAmountReceivedExcludingTaxInInvoiceCurrency = amountReceivedConverted - totalTaxAmountReceivedInInvoiceCurrency;
                        HashMap<String, Object> mappingObj = new HashMap<String, Object>();
                        mappingObj.put("companyId", companyid);
                        mappingObj.put("invoiceId", invoice.getID());
                        mappingObj.put("invoiceReceivedAmt", invoiceAmountReceivedExcludingTaxInInvoiceCurrency);
                        mappingObj.put("recoveredDate", receipt.getCreationDate());
                        mappingObj.put("gstToRecover", totalTaxAmountReceivedInInvoiceCurrency);
                        mappingObj.put("badDebtType", 1);
                        mappingObj.put("receiptid", receipt.getID());
                        KwlReturnObject mapResult = accInvoiceDAOObj.saveBadDebtInvoiceMapping(mappingObj);
                    }

                }
                amountDiff = authHandler.round(oldReceiptRowsAmount(paramJobj, jArr, currencyid, externalCurrencyRate), companyid);

                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.receipt.forex", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
                if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                    if (amountDiff < 0) {
                        rateDecreased = true;
                    }
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put(Constants.companyKey, companyid);
                    jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jedjson.put("accountid", preferences.getForeignexchange().getID());
                    if (journalEntry.getGstCurrencyRate() != 0.0) {
                        jedjson.put("forexGainLoss", rateDecreased ? (-1 * amountDiff) : amountDiff);
                        jedjson.put("paymentType", type);
                    }
                    jedjson.put("debit", rateDecreased ? true : false);
                    jedjson.put("jeid", jeid);
                    JournalEntryDetail jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                    jedetails.add(jed);
                }
            } else {
                amount = authHandler.round(Double.parseDouble(paramJobj.optString("amount")), companyid);
            }

            if (jArr.length() > 0 && type == Constants.PaymentAgainstLoanDisbursement) {
                double amountDifference = 0.0;
                double enteredAmount = 0.0;
                double principalAmount = 0.0;
                double interestAmount = 0.0;
                double interestAmountInPaymentCurrency = 0.0;
                double principalInPaymentCurrency = 0.0;
                String transactionCurrencyId = "";
                for (int i = 0; i < jArr.length(); i++) { // Changed for New Customer/Vendor Removed account dependency
                    JSONObject jobj = jArr.getJSONObject(i);
                    transactionCurrencyId = jobj.getString("currencyidtransaction");
                    JSONArray jArray = new JSONArray();
                    jArray.put(jobj);
                    KwlReturnObject resultRepaymentDetails = accountingHandlerDAOobj.getObject(RepaymentDetails.class.getName(), jobj.getString("repaymentscheduleid"));
                    RepaymentDetails RD = (RepaymentDetails) resultRepaymentDetails.getEntityList().get(0);
                    enteredAmount = jobj.getDouble("enteramount");
                    interestAmount = RD.getInterest();
                    principalAmount = RD.getPrinciple();
                    interestAmountInPaymentCurrency = (interestAmount * enteredAmount) / (principalAmount + interestAmount);
                    principalInPaymentCurrency = enteredAmount - interestAmountInPaymentCurrency;
                    jobj.put("principalInPaymentCurrency", principalInPaymentCurrency);
                    amount += enteredAmount;
                    if (!StringUtil.isNullOrEmpty(transactionCurrencyId) && !StringUtil.isNullOrEmpty(currencyid)) {
                        if (transactionCurrencyId.equals(currencyid)) {
                            amountDifference = LoanRepaymentForexGailLossAmountSameCurrency(paramJobj, jobj, receipt, transactionCurrencyId, currencyid, externalCurrencyRate);
                        } else {
                            amountDifference = LoanRepaymentForexGailLossAmount(paramJobj, jobj, receipt, transactionCurrencyId, currencyid, externalCurrencyRate);
                        }
                    }

                    rateDecreased = false;
                    if (amountDifference < 0) {
                        rateDecreased = true;
                    }

                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put(Constants.companyKey, companyid);
                    jedjson.put("amount", principalInPaymentCurrency - amountDifference);
                    jedjson.put("accountid", RD.getDisbursement().getDebitaccount() != null ? RD.getDisbursement().getDebitaccount().getID() : accountId);
                    if (jobj.optDouble("gstCurrencyRate", 0.0) != 0.0) {
                        jedjson.put("gstCurrencyRate", jobj.optDouble("gstCurrencyRate", 0.0));
                        journalEntry.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate", 0.0));
                        jedjson.put("paymentType", type);
                    }
                    jedjson.put("forexGainLoss", amountDifference);
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    jedjson.put("description", jobj.optString("description"));
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);

                    JSONObject jedForInterestAmount = new JSONObject();
                    jedForInterestAmount.put("srno", jedetails.size() + 1);
                    jedForInterestAmount.put(Constants.companyKey, companyid);
                    jedForInterestAmount.put("amount", interestAmountInPaymentCurrency);
                    jedForInterestAmount.put("accountid", !StringUtil.isNullOrEmpty(extraCompanyPreferences.getLoanInterestAccount()) ? extraCompanyPreferences.getLoanInterestAccount() : accountId);
                    jedForInterestAmount.put("debit", false);
                    jedForInterestAmount.put("jeid", jeid);
                    jedForInterestAmount.put("description", "Interest Amount");
                    JournalEntryDetail jedForInterest = accJournalEntryobj.getJournalEntryDetails(jedForInterestAmount);
                    jedetails.add(jedForInterest);
                    if ((!jobj.getString("documentno").equalsIgnoreCase("undefined")) && (!jobj.getString("documentno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("documentno") + ",");
                    }
                    jArr.getJSONObject(i).put("rowjedid", jed.getID());

                    if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.customfield, "[]"))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString(Constants.customfield, "[]"));
                        jcustomarrayMap.put(jed.getID(), jcustomarray);
                    }
                    jArr.getJSONObject(i).put("jedetail", jed.getID());
                    if (preferences.getForeignexchange() == null) {
                        throw new AccountingException(messageSource.getMessage("acc.receipt.forex", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                    if (amountDifference != 0 && preferences.getForeignexchange() != null && Math.abs(amountDifference) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                        if (amountDifference < 0) {
                            rateDecreased = true;
                        }
                        JSONObject jedjsonnew = new JSONObject();
                        jedjsonnew.put("srno", jedetails.size() + 1);
                        jedjsonnew.put(Constants.companyKey, companyid);
                        jedjsonnew.put("amount", rateDecreased ? (-1 * amountDifference) : amountDifference);
                        jedjsonnew.put("accountid", preferences.getForeignexchange().getID());
                        if (journalEntry.getGstCurrencyRate() != 0.0) {
                            jedjsonnew.put("forexGainLoss", rateDecreased ? (-1 * amountDifference) : amountDifference);
                            jedjsonnew.put("paymentType", type);
                        }
                        jedjsonnew.put("debit", rateDecreased ? true : false);
                        jedjsonnew.put("jeid", jeid);
                        JournalEntryDetail jednew = accJournalEntryobj.getJournalEntryDetails(jedjsonnew);
                        jedetails.add(jednew);
                    }
                }
            }
            JSONObject jedjson = null;
            KwlReturnObject jedresult = null;
            JournalEntryDetail jed = null;
            JournalEntryDetail JEdeatilId = null;
            List receiptOtherwiseList = new ArrayList();
            HashMap receiptdetailotherwise = new HashMap();

            if (type == Constants.AdvancePayment || type == Constants.GLPayment || type == Constants.PaymentAgainstCNDN || type == Constants.LocalAdvanceTypePayment || type == Constants.ExportAdvanceTypePayment) {//advance,GL,Cn
                JSONArray drAccArr = detailsJSONArray;
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    boolean isdebit = jobj.has("isdebit") ? Boolean.parseBoolean(jobj.optString("isdebit")) : false;
                    if (type == Constants.GLPayment) {
                        isdebit = jobj.has("debit") ? Boolean.parseBoolean(jobj.optString("debit")) : false;
                    }
                    double forexgainloss = 0;
                    if (type == Constants.PaymentAgainstCNDN) {
                        String transactionCurrencyId = jobj.optString("currencyidtransaction");
                        if (!StringUtil.isNullOrEmpty(transactionCurrencyId) && !transactionCurrencyId.equals(receipt.getCurrency().getCurrencyID())) {
                            forexgainloss = cndnReceiptForexGailLossAmount(paramJobj, jobj, receipt, transactionCurrencyId, currencyid, externalCurrencyRate);
                        } else if (transactionCurrencyId.equals(receipt.getCurrency().getCurrencyID())) {
                            forexgainloss = cndnReceiptForexGailLossAmountForSameCurrency(paramJobj, jobj, receipt, transactionCurrencyId, currencyid, externalCurrencyRate);
                        }
                    }
                    if (type == Constants.AdvancePayment && receipt.getVendor() != null && !StringUtil.isNullOrEmpty(jobj.optString("documentid",null))) {   // Refund type payment
                        String transactionCurrencyId = jobj.optString("currencyidtransaction");
                        forexgainloss = RefundReceiptForexGailLossAmount(paramJobj, jobj, receipt, transactionCurrencyId, currencyid, externalCurrencyRate);
                    }
                    //        check the logic implemented after words from atul 

                    //**********************************Start***********************
                    double enteramount =  Double.parseDouble(jobj.optString("enteramount","0.0"));
                    if (type == Constants.AdvancePayment && (Integer.parseInt(company.getCountry().getID()) == Constants.malaysian_country_id) && Boolean.parseBoolean(paramJobj.getString("iscustomer")) && (!StringUtil.isNullOrEmpty(jobj.getString("documentid").toString()))) { //if advance payment done against customer for Malaysia Country
                        double totalAmount = Double.parseDouble(jobj.getString("enteramount"));
                        double typeAmountTax = 0;
                        String accountID = "";
                        String taxid = jobj.getString("documentid");
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), taxid);
                        Tax taxObject = (Tax) txresult.getEntityList().get(0);
                        Account account = taxObject.getAccount();
                        if (account != null) {
                            accountID = account.getID();
                        }
                        typeAmountTax = jobj.optDouble("taxamount", 0.0);   // Above code aommented as tax amount is editable.
                        if (taxObject != null) {
                            receipt.setTax(taxObject);
                            receipt.setTaxAmount(typeAmountTax);
                        }

                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put(Constants.companyKey, companyid);
                        jedjson.put("amount", totalAmount - typeAmountTax);
                        jedjson.put("accountid", accountId);
                        jedjson.put("debit", isdebit);//false);
                        jedjson.put("jeid", jeid);
                        jedjson.put("description", jobj.optString("description"));
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(JEdeatilId);
                        if (typeAmountTax != 0) {         // If taxamount is zero then je details will not be saved
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put(Constants.companyKey, companyid);
                            jedjson.put("amount", typeAmountTax);
                            jedjson.put("accountid", !StringUtil.isNullOrEmpty(accountID) ? accountID : jobj.getString("accountid"));
                            jedjson.put("debit", isdebit);//false);
                            jedjson.put("jeid", jeid);
                            jedjson.put("description", jobj.optString("description"));
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(JEdeatilId);
                        }
                    } else {//Normal Flow
                        enteramount = Double.parseDouble(jobj.optString("enteramount","0.0")) - forexgainloss;
                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put(Constants.companyKey, companyid);
                        jedjson.put("amount", enteramount);
                        if (type == Constants.GLPayment) {
                            jedjson.put("accountid", jobj.optString("documentid"));//Changed account Id 
                        } else if (type == Constants.AdvancePayment) {
                            KwlReturnObject advaceReceiptResult = accountingHandlerDAOobj.getObject(ReceiptAdvanceDetail.class.getName(), jobj.optString("rowdetailid"));
                            ReceiptAdvanceDetail advaceReceipt = (ReceiptAdvanceDetail) advaceReceiptResult.getEntityList().get(0);
                            /*
                             * Taking original account in edit and copy payment.
                             * Refer SDP-7867
                             */
                            if (advaceReceipt != null && advaceReceipt.getTotalJED() != null) {
                                jedjson.put("accountid", advaceReceipt.getTotalJED().getAccount() != null ? advaceReceipt.getTotalJED().getAccount().getID() : accountId);//Changed account Id
                            } else {
                                jedjson.put("accountid", accountId);//Changed account Id
                            }
                        } else if (type == Constants.PaymentAgainstCNDN) {
                            KwlReturnObject debitNoteResult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), jobj.getString("documentid"));
                            DebitNote debitMemo = (DebitNote) debitNoteResult.getEntityList().get(0);
                            jedjson.put("accountid", debitMemo.getAccount() != null ? debitMemo.getAccount().getID() : accountId);//Changed account Id
                        }
                        if (type == Constants.PaymentAgainstCNDN && jobj.optDouble("gstCurrencyRate", 0.0) != 0.0) {
                            jedjson.put("gstCurrencyRate", jobj.optDouble("gstCurrencyRate", 0.0));
                            journalEntry.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate", 0.0));
                            jedjson.put("paymentType", type);
                            jedjson.put("forexGainLoss", forexgainloss);
                        }
                        jedjson.put("debit", isdebit);//false);
                        jedjson.put("jeid", jeid);
                        jedjson.put("description", jobj.optString("description"));
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JEdeatilId = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(JEdeatilId);
                    }
                    if ((!jobj.optString("documentno").equalsIgnoreCase("undefined")) && (!jobj.optString("documentno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("documentno") + ",");
                    }
                    drAccArr.getJSONObject(i).put("rowjedid", JEdeatilId.getID());
                    JSONArray jcustomarray = null;
                    if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.customfield, ""))) {
                        jcustomarray = new JSONArray(jobj.optString(Constants.customfield, "[]"));
                        jcustomarrayMap.put(JEdeatilId.getID(), jcustomarray);
                    }
                    drAccArr.getJSONObject(i).put("jedetail", JEdeatilId.getID());
                    String paymentMethodJedId= "";
                    if (jcustomarray != null && jcustomarray.length() > 0 && jcustomarray.optJSONObject(0).length() > 0 && (type == Constants.AdvancePayment || type == Constants.GLPayment || type == Constants.PaymentAgainstCNDN)) {
                        /*
                         * If Custom field is tagged then posting additional
                         * jedetail for payment method against Advance Payment/GL Account/Debit Note tagged
                         * with custom field. ERP-33860
                         */
                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put(Constants.companyKey, companyid);
                        jedjson.put("amount", enteramount);
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
                    if (forexgainloss != 0 && preferences.getForeignexchange() != null && Math.abs(forexgainloss) >= 0.000001) {//Math.abs(forexgainloss) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                        rateDecreased = false;
                        if (forexgainloss < 0) {
                            rateDecreased = true;
                        }
                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put(Constants.companyKey, companyid);
                        jedjson.put("amount", rateDecreased ? (-1 * forexgainloss) : forexgainloss);
                        if (type == Constants.PaymentAgainstCNDN && journalEntry.getGstCurrencyRate() != 0.0) {
                            jedjson.put("forexGainLoss", rateDecreased ? (-1 * forexgainloss) : forexgainloss);
                            jedjson.put("paymentType", type);
                        }
                        jedjson.put("accountid", preferences.getForeignexchange().getID());
                        jedjson.put("debit", rateDecreased ? true : false);
                        jedjson.put("jeid", jeid);
                        jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                        jedetails.add(jed);
                    }

                    // Logic for saving line level details of Receipt against GL code.
                    double rowtaxamount = 0;
                    if (type == Constants.GLPayment) {//Otherwise for receive Payment
                        ReceiptDetailOtherwise receiptDetailOtherwise = null;
                        String rowtaxid = jobj.optString("prtaxid");
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                        Tax rowtax = (Tax) txresult.getEntityList().get(0);
                        String appliedGst = jobj.optString("appliedGst", "");
                        KwlReturnObject gstresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), appliedGst);

                        if (rowtax == null || rowtaxid.equalsIgnoreCase("-1")) {
                            receiptdetailotherwise.put("amount", Double.parseDouble(jobj.optString("enteramount","0.0")));
                            receiptdetailotherwise.put("taxjedid", "");
                            receiptdetailotherwise.put("tax", rowtaxid.equalsIgnoreCase("-1") ? "None" : "");
                            receiptdetailotherwise.put("accountid", jobj.optString("documentid"));
                            receiptdetailotherwise.put("isdebit", isdebit);
                            receiptdetailotherwise.put("taxamount", rowtaxamount);
                            receiptdetailotherwise.put("description", jobj.optString("description"));
                            receiptdetailotherwise.put("receipt", receipt.getID());
                            if (jobj.has("srNoForRow")) {
                                int srNoForRow = StringUtil.isNullOrEmpty(jobj.optString("srNoForRow",null)) ? 0 : Integer.parseInt(jobj.optString("srNoForRow","0"));
                                receiptdetailotherwise.put("srNoForRow", srNoForRow);
                            }
                            if (jobj.has("jedetail") && jobj.get("jedetail") != null) {
                                receiptdetailotherwise.put("jedetail", (String) jobj.get("jedetail"));
                            } else {
                                receiptdetailotherwise.put("jedetail", "");
                            }
                            if (gstresult!=null && gstresult.getEntityList().get(0) != null) {
                                Tax gstAppliedObj = (Tax) gstresult.getEntityList().get(0);
                                if (gstAppliedObj != null) {
                                    receiptdetailotherwise.put("gstApplied", gstAppliedObj);
                                }
                            }
                            result = accReceiptDAOobj.saveReceiptDetailOtherwise(receiptdetailotherwise);
                            receiptdetailotherwise.clear();
                            receiptDetailOtherwise = (ReceiptDetailOtherwise) result.getEntityList().get(0);
                            receiptOtherwiseList.add(receiptDetailOtherwise.getID());
                        } else {
                            rowtaxamount = Double.parseDouble(jobj.optString("taxamount","0.0"));
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put(Constants.companyKey, company.getCompanyID());
                            jedjson.put("amount", authHandler.formattedAmount(rowtaxamount, companyid));
                            jedjson.put("accountid", rowtax.getAccount().getID());
                            jedjson.put("debit", isdebit);//false);
                            jedjson.put("jeid", jeid);
                            jedjson.put("description", jobj.optString("description"));
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);

                            receiptdetailotherwise.put("amount", Double.parseDouble(jobj.optString("enteramount","0.0")));
                            receiptdetailotherwise.put("taxjedid", jed.getID());
                            receiptdetailotherwise.put("tax", rowtax.getID());
                            receiptdetailotherwise.put("accountid", jobj.optString("documentid"));
                            receiptdetailotherwise.put("isdebit", isdebit);
                            receiptdetailotherwise.put("taxamount", rowtaxamount);
                            receiptdetailotherwise.put("receipt", receipt.getID());
                            receiptdetailotherwise.put("description", jobj.optString("description"));
                            if (jobj.has("srNoForRow")) {
                                int srNoForRow = StringUtil.isNullOrEmpty(jobj.optString("srNoForRow",null)) ? 0 : Integer.parseInt(jobj.optString("srNoForRow"));
                                receiptdetailotherwise.put("srNoForRow", srNoForRow);
                            }
                            Tax gstAppliedObj = null;
                            if (gstresult!=null && gstresult.getEntityList().get(0) != null) {
                                gstAppliedObj = (Tax) gstresult.getEntityList().get(0);
                            }
                            if (gstAppliedObj != null) {
                                receiptdetailotherwise.put("gstApplied", gstAppliedObj);
                            } else {
                                receiptdetailotherwise.put("gstApplied", rowtax);
                            }
                            if (jobj.has("jedetail") && jobj.get("jedetail") != null) {
                                receiptdetailotherwise.put("jedetail", (String) jobj.get("jedetail"));
                            } else {
                                receiptdetailotherwise.put("jedetail", "");
                            }
                            receiptdetailotherwise.put("taxjedetail", jed.getID());
                            result = accReceiptDAOobj.saveReceiptDetailOtherwise(receiptdetailotherwise);
                            receiptdetailotherwise.clear();
                            receiptDetailOtherwise = (ReceiptDetailOtherwise) result.getEntityList().get(0);
                            receiptOtherwiseList.add(receiptDetailOtherwise.getID());
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.customfield, "")) && !(jobj.optString(Constants.customfield, "")).equals("[]") && !(jobj.optString(Constants.customfield, "")).equals("[{}]")) {
                                /*
                                 * If Custom field is tagged then posting
                                 * additional jedetail for payment method against tax
                                 * for Advance Payment/GL Account/Debit Note
                                 * tagged with custom field. ERP-34425
                                 */
                                jedjson = new JSONObject();
                                jedjson.put("srno", jedetails.size() + 1);
                                jedjson.put(Constants.companyKey, companyid);
                                jedjson.put("amount", rowtaxamount);
                                jedjson.put("accountid", dipositTo.getID());
                                jedjson.put("debit", !isdebit);
                                jedjson.put("jeid", jeid);
                                jedjson.put(Constants.ISSEPARATED, true);
                                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail pmAmoutForTaxJed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jedetails.add(pmAmoutForTaxJed);

                                jcustomarray = new JSONArray(jobj.optString(Constants.customfield, "[]"));
                                customrequestParams.put("customarray", jcustomarray);
                                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                                customrequestParams.put("modulerecid", jed.getID());
                                customrequestParams.put("recdetailId", receiptDetailOtherwise.getID());
                                customrequestParams.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                                customrequestParams.put(Constants.companyKey, companyid);
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
                        if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.customfield, ""))) {
                            jcustomarray = new JSONArray(jobj.optString(Constants.customfield, "[]"));
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                            customrequestParams.put("modulerecid", JEdeatilId.getID());
                            customrequestParams.put("recdetailId", receiptDetailOtherwise.getID());
                            customrequestParams.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                            customrequestParams.put(Constants.companyKey, companyid);
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
                                 * of payment method for receipt against GL
                                 * account
                                 */
                                customrequestParams.put("modulerecid", paymentMethodJedId);
                                customrequestParams.put("recdetailId", receiptDetailOtherwise.getID());
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
                    /**ERP-32829
                     * save tax jedetails
                     */
                    if (istaxonadvancereceipt && (receipt.getCompany().getCountry().getID().equalsIgnoreCase(""+Constants.indian_country_id)) &&
                            type == Constants.AdvancePayment && jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty((String) jobj.optString("LineTermdetails")) &&
                            jobj.has("rcmapplicable") && !jobj.optBoolean("rcmapplicable")) {
                        JSONArray termsArray = new JSONArray(StringUtil.DecodeText((String) jobj.optString("LineTermdetails")));
                        for (int j = 0; j < termsArray.length(); j++) {
                            JSONObject termObject = termsArray.getJSONObject(j);
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put(Constants.companyKey, companyid);
                            jedjson.put("amount", termObject.get("termamount"));
                            jedjson.put("accountid", termObject.get("accountid"));
                            jedjson.put("debit", false);
                            jedjson.put("jeid", jeid);
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jed1 = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed1);
                            
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put(Constants.companyKey, companyid);
                            jedjson.put("amount", termObject.get("termamount"));
                            jedjson.put("accountid", termObject.get("payableaccountid"));
                            String payableaccountid = termObject.optString("payableaccountid","");
                            /**
                             * Throw exception with proper message. if payable account not present
                             */
                            if (StringUtil.isNullOrEmpty(payableaccountid)) {
                                throw new AccountingException(messageSource.getMessage("acc.common.advancepayableaccountnotmappedtoterm", new Object[]{termObject.optString("term")}, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                            }
                            jedjson.put("debit", true);
                            jedjson.put("jeid", jeid);
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed1 = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed1);
                        }
                        receipt.setTaxAmount(0);
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
                jedjson.put(Constants.companyKey, company.getCompanyID());
                jedjson.put("amount", balaceAmount);
                jedjson.put("accountid", accountIdComPreAdjRec);
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jedjson.put("description", "");
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
            }
        } catch (AccountingException e) {
            throw new AccountingException(e.getMessage());
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return jedetails;
    }

    public double cndnReceiptForexGailLossAmount(JSONObject paramJobj, JSONObject jobj, Receipt receipt, String transactionCurrencyId, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException {
        String companyid = paramJobj.optString(Constants.companyKey);
        HashMap<String, Object> requestParams = new HashMap<String, Object>();

//        double enterAmountPaymentCurrencyOld = 0;
//        double enterAmountTrancastionCurrencyNew = 0;
//        double amountdiff = 0;
        double amount = 0, actualAmount = 0;
        try {
//            Double enteramount = jobj.optDouble("enteramount",0.0);
            String documentId = jobj.optString("documentid");
            requestParams.put(Constants.globalCurrencyKey, paramJobj.optString(Constants.currencyKey));
            requestParams.put(Constants.companyKey, companyid);
            requestParams.put("dateformat", authHandler.getDateOnlyFormatter(paramJobj));
            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), documentId);
            DebitNote debitNote = (DebitNote) resultObject.getEntityList().get(0);
            JournalEntry je = debitNote.getJournalEntry();
            Date creationDate = authHandler.getDateOnlyFormat().parse(paramJobj.getString("creationdate"));
            double newrate = 0.0;
            double ratio = 0;
            boolean revalFlag = false;
            Date creditNoteDate = null;
            if (debitNote.isNormalDN()) {
                je = debitNote.getJournalEntry();
                externalCurrencyRate = je.getExternalCurrencyRate();
            } else {
                externalCurrencyRate = debitNote.getExchangeRateForOpeningTransaction();
                if (debitNote.isConversionRateFromCurrencyToBase()) {
                    externalCurrencyRate = 1 / externalCurrencyRate;
                }
            }
            creditNoteDate = debitNote.getCreationDate();

            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", debitNote.getID());
            invoiceId.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (history != null) {
                externalCurrencyRate = history.getEvalrate();
                newrate = jobj.optDouble("exchangeratefortransaction", 0.0); // Get entered exchange rate(for DN) against which receipt is made
                revalFlag = true;
            }
            // Calculate old exchange rate(for DN) against which receipt is made
            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJobj.optString(Constants.currencyKey));
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (debitNote.getCurrency() != null) {
                currid = debitNote.getCurrency().getCurrencyID();
                }

            if (currid.equalsIgnoreCase(currencyid)) {
                double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 0.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
                if (externalCurrencyRate != paymentExternalCurrencyRate) {
                    result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, creditNoteDate, externalCurrencyRate, paymentExternalCurrencyRate);
                } else {
                    result = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0, currid, currencyid, creditNoteDate, externalCurrencyRate);
            }
            } else {
                double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 0.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
                if (externalCurrencyRate != paymentExternalCurrencyRate) {
                    result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, creditNoteDate, externalCurrencyRate, paymentExternalCurrencyRate);
                } else {
                    result = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, 1.0, currid, currencyid, creditNoteDate, externalCurrencyRate);
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
                KwlReturnObject bAmtActual = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creationDate, newrate);
                actualAmount += authHandler.round((Double) bAmtActual.getEntityList().get(0), companyid);
            } else {
                if (currid.equalsIgnoreCase(currencyid)) {
                    double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 0.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
                    if (externalCurrencyRate != paymentExternalCurrencyRate) {
                        result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, creditNoteDate, externalCurrencyRate, paymentExternalCurrencyRate);
                    } else {
                        result = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0, currid, currencyid, creditNoteDate, externalCurrencyRate);
                    }
                } else {
                    double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 0.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
                    if (externalCurrencyRate != paymentExternalCurrencyRate) {
                        result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, creditNoteDate, externalCurrencyRate, paymentExternalCurrencyRate);
                    } else {
                        result = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, 1.0, currid, currencyid, creditNoteDate, externalCurrencyRate);
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
                if (isopeningBalanceRecceipt && isConversionRateFromCurrencyToBase) {// if receipt is opening balance receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmtActual = accCurrencyobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, currencyid, creationDate, externalCurrencyRate);
                } else {
                    bAmtActual = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creationDate, externalCurrencyRate);
                }
                actualAmount += (Double) bAmtActual.getEntityList().get(0);
            }
//            if (!StringUtil.isNullOrEmpty(transactionCurrencyId) && !StringUtil.isNullOrEmpty(transactionCurrencyId) && !transactionCurrencyId.equals(currencyid)) {
//                enterAmountTrancastionCurrencyNew = enteramount;
//                if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString())) {
//                    enterAmountTrancastionCurrencyNew = enteramount / Double.parseDouble(jobj.get("exchangeratefortransaction").toString());
//                    enterAmountTrancastionCurrencyNew = authHandler.round(enterAmountTrancastionCurrencyNew, companyid);
//                    KwlReturnObject bAmtCurrencyFilter = null;
//                    bAmtCurrencyFilter = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, enterAmountTrancastionCurrencyNew, transactionCurrencyId, currencyid, creditNoteDate, externalCurrencyRate);
//                    enterAmountPaymentCurrencyOld = authHandler.round((Double) bAmtCurrencyFilter.getEntityList().get(0), companyid);
//                    amountdiff = enteramount - enterAmountPaymentCurrencyOld;
//                }
//            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("cndnReceiptForexGailLossAmount : " + ex.getMessage(), ex);
        }
        return (actualAmount);
    }

    private double oldReceiptRowsAmount(JSONObject paramJobj, JSONArray jArr, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException {
        double amount = 0, actualAmount = 0;
        KwlReturnObject result;
        try {
            String companyid = paramJobj.optString("compnayid");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
            requestParams.put(Constants.globalCurrencyKey, paramJobj.optString(Constants.currencyKey));
            Date creationDate = authHandler.getDateOnlyFormat().parse(paramJobj.getString("creationdate"));
            for (int i = 0; i < jArr.length(); i++) {
                double ratio = 0;
                JSONObject jobj = jArr.getJSONObject(i);
                Date invoiceCreationDate = new Date();

                double newrate = 0.0;
                boolean revalFlag = false;
                result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("documentid"));
                Invoice invoice = (Invoice) result.getEntityList().get(0);
                double exchangeRate = 0d;
                invoiceCreationDate = invoice.getCreationDate();
                if (!invoice.isNormalInvoice() && invoice.isIsOpeningBalenceInvoice()) {
                    exchangeRate = invoice.getExchangeRateForOpeningTransaction();
                    if (invoice.isConversionRateFromCurrencyToBase()) {
                        exchangeRate = 1 / exchangeRate;
                    }
                } else {
                    exchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
                }

                HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                invoiceId.put("invoiceid", invoice.getID());
                invoiceId.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
                result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                if (history != null) {
                    exchangeRate = history.getEvalrate();
                    newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
                    revalFlag = true;
                }
                result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJobj.optString(Constants.currencyKey));
                KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
                String currid = currency.getCurrencyID();
                if (invoice.getCurrency() != null) {
                    currid = invoice.getCurrency().getCurrencyID();
                }

                if (currid.equalsIgnoreCase(currencyid)) {
                    double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 0.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
                    if (exchangeRate != paymentExternalCurrencyRate) {
                        result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate, paymentExternalCurrencyRate);
                    } else {
                        result = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate);
                    }
                } else {
                    double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 0.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
                    if (exchangeRate != paymentExternalCurrencyRate) {
                        result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate, paymentExternalCurrencyRate);
                    } else {
                        result = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate);
                    }
                }
                double oldrate = (Double) result.getEntityList().get(0);
                Double recinvamount = jobj.getDouble("enteramount");
                if(paramJobj.optBoolean("isDiscountAppliedOnPaymentTerms",false)){
                    recinvamount = authHandler.round(jobj.optDouble("enteramount") + jobj.optDouble("discountname", 0.0), companyid);
                }
                
                
                // Invoice forex gain loss logic
                
                
                boolean isopeningBalanceRecceipt = jobj.optBoolean("isopeningBalanceRecceipt", false);
                boolean isConversionRateFromCurrencyToBase = jobj.optBoolean("isConversionRateFromCurrencyToBase", false);
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
                    KwlReturnObject bAmtActual = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creationDate, newrate);
                    actualAmount += authHandler.round((Double) bAmtActual.getEntityList().get(0), companyid);
                } else {
                    if (currid.equalsIgnoreCase(currencyid)) {
                        double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 0.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
                        if (exchangeRate != paymentExternalCurrencyRate) {
                            result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate, paymentExternalCurrencyRate);
                        } else {
                            result = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate);
                        }
                    } else {
                        double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 0.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
                        if (exchangeRate != paymentExternalCurrencyRate) {
                            result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate, paymentExternalCurrencyRate);
                        } else {
                            result = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate);
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
                    if (isopeningBalanceRecceipt && isConversionRateFromCurrencyToBase) {// if receipt is opening balance receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmtActual = accCurrencyobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, currencyid, creationDate, externalCurrencyRate);
                    } else {
                        bAmtActual = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creationDate, externalCurrencyRate);
                    }
                    actualAmount += (Double) bAmtActual.getEntityList().get(0);
                }
            }

        } catch (ParseException | JSONException ex) {
            throw ServiceException.FAILURE("oldReceiptRowsAmount : " + ex.getMessage(), ex);
        } 
        return (actualAmount);
    }

    public double cndnReceiptForexGailLossAmountForSameCurrency(JSONObject paramJobj, JSONObject jobj, Receipt receipt, String transactionCurrencyId, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException, JSONException {
        double amount = 0, actualAmount = 0;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.globalCurrencyKey, paramJobj.optString(Constants.companyKey));
        requestParams.put(Constants.companyKey, paramJobj.optString(Constants.currencyKey));
        requestParams.put("dateformat", authHandler.getDateOnlyFormatter(paramJobj));
        try {

            double exchangeRate = 0d;
            double newrate = 0.0;
            Double enteramount = jobj.getDouble("enteramount");
            String documentId = jobj.getString("documentid");
            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), documentId);
            DebitNote debitMemo = (DebitNote) resultObject.getEntityList().get(0);
            JournalEntry je = debitMemo.getJournalEntry();
            Date creditNoteDate = null;
            if (debitMemo.isNormalDN()) {
                je = debitMemo.getJournalEntry();
                exchangeRate = je.getExternalCurrencyRate();
            } else {
                exchangeRate = debitMemo.getExchangeRateForOpeningTransaction();
                if (debitMemo.isConversionRateFromCurrencyToBase()) {
                    exchangeRate = 1 / exchangeRate;
                }
            }
            creditNoteDate = debitMemo.getCreationDate();
            double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 1.0 : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));

            KwlReturnObject bAmt = null;
            if (exchangeRate != paymentExternalCurrencyRate) {
                bAmt = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, transactionCurrencyId, currencyid, creditNoteDate, exchangeRate, paymentExternalCurrencyRate);
            } else {
                bAmt = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0, transactionCurrencyId, currencyid, creditNoteDate, exchangeRate);
            }
            double oldrate = (Double) bAmt.getEntityList().get(0);
            newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
            amount = (enteramount - (enteramount / newrate) * oldrate) / newrate;
            KwlReturnObject bAmtActual = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creditNoteDate, newrate);
            actualAmount += (Double) bAmtActual.getEntityList().get(0);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("cndnReceiptForexGailLossAmountForSameCurrency : " + ex.getMessage(), ex);
        }
        return (actualAmount);
    }

    public double RefundReceiptForexGailLossAmount(JSONObject paramJobj, JSONObject jobj, Receipt receipt, String transactionCurrencyId, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException, JSONException, ParseException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        String companyId = paramJobj.optString(Constants.companyKey);
        requestParams.put(Constants.globalCurrencyKey, paramJobj.optString(Constants.currencyKey));
        requestParams.put(Constants.companyKey, companyId);
        requestParams.put("dateformat", authHandler.getDateOnlyFormatter(paramJobj));
        double amountdiff = 0;
        Date creationDate = null;
        if (paramJobj.getString("creationdate") != null) { // Check added for SDP-5827 as in link case creation date is Null
            creationDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("creationdate"));
        } else {
            creationDate = receipt.getCreationDate();
        }
        double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? receipt.getExternalCurrencyRate() : Double.parseDouble(paramJobj.optString("externalcurrencyrate"));
        boolean revalFlag = false;
        double newrate = 0.0;
        double amount = 0;
        try {
            Date paymentDate = null;
            String JE = "";
            String documentId = jobj.getString("documentid");
            Double recinvamount = jobj.getDouble("enteramount");
            boolean isopeningBalanceRecceipt = jobj.optBoolean("isopeningBalanceRecceipt", false);
            boolean isConversionRateFromCurrencyToBase = jobj.optBoolean("isConversionRateFromCurrencyToBase", false);
            double ratio = 0;


            KwlReturnObject resultObject = null;
            resultObject = accReceiptDAOobj.getPaymentInformationFromAdvanceDetailId(documentId, companyId);
            List list = resultObject.getEntityList();
            Object[] object = (Object[]) list.get(0);
            externalCurrencyRate = Double.parseDouble(object[0].toString());

            //if Advance is Revaluated then we will revaluated rate
            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", documentId);
            invoiceId.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
            KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
            RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
            if (history != null) {
                externalCurrencyRate = history.getEvalrate();
                newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
                revalFlag = true;
            }

            JE = object[1].toString();
            KwlReturnObject JEResult = accJournalEntryobj.getEntryDateFromJEId(JE, companyId);
            List JEList = JEResult.getEntityList();
            paymentDate = (Date) JEList.get(0);

            result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJobj.optString(Constants.currencyKey));
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            String currid = currency.getCurrencyID();
            if (object[4] != null) {
                currid = object[4].toString();
            }
            if (currid.equalsIgnoreCase(currencyid)) {
                if (externalCurrencyRate != paymentExternalCurrencyRate) {
                    result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, paymentDate, externalCurrencyRate, paymentExternalCurrencyRate);
                } else {
                    result = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0, currid, currencyid, paymentDate, externalCurrencyRate);
                }
            } else {
                if (externalCurrencyRate != paymentExternalCurrencyRate) {
                    result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, paymentDate, externalCurrencyRate, paymentExternalCurrencyRate);
                } else {
                    result = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, 1.0, currid, currencyid, paymentDate, externalCurrencyRate);
                }
            }
            double oldrate = (Double) result.getEntityList().get(0);

            if (jobj.optDouble("exchangeratefortransaction", 0.0) != oldrate && jobj.optDouble("exchangeratefortransaction", 0.0) != 0.0 && Math.abs(jobj.optDouble("exchangeratefortransaction", 0.0) - oldrate) >= 0.000001) {
                newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
                ratio = oldrate - newrate;
                amount = (recinvamount - (recinvamount / newrate) * oldrate) / newrate;
                KwlReturnObject bAmtActual = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creationDate, newrate);
                amountdiff += (Double) bAmtActual.getEntityList().get(0);
            } else {
                if (currid.equalsIgnoreCase(currencyid)) {

                    if (externalCurrencyRate != paymentExternalCurrencyRate) {
                        result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, paymentDate, externalCurrencyRate, paymentExternalCurrencyRate);
                    } else {
                        result = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0, currid, currencyid, paymentDate, externalCurrencyRate);
                    }
                } else {
                    if (externalCurrencyRate != paymentExternalCurrencyRate) {
                        result = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, currid, currencyid, paymentDate, externalCurrencyRate, paymentExternalCurrencyRate);
                    } else {
                        result = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, 1.0, currid, currencyid, paymentDate, externalCurrencyRate);
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
                if (isopeningBalanceRecceipt && isConversionRateFromCurrencyToBase) {// if receipt is opening balance receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmtActual = accCurrencyobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, currencyid, creationDate, externalCurrencyRate);
                } else {
                    bAmtActual = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creationDate, externalCurrencyRate);
                }
                amountdiff += (Double) bAmtActual.getEntityList().get(0);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("cndnPaymentForexGailLossAmount : " + ex.getMessage(), ex);
        }
        return (amountdiff);
    }

    public double LoanRepaymentForexGailLossAmountSameCurrency(JSONObject paramJobj, JSONObject jobj, Receipt receipt, String transactionCurrencyId, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException, JSONException {
        double amount = 0, actualAmount = 0;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.globalCurrencyKey, paramJobj.optString(Constants.currencyKey));
        requestParams.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
        requestParams.put("dateformat", authHandler.getDateOnlyFormatter(paramJobj));
        try {

            double exchangeRate = 0d;
            double newrate = 0.0;
            Double enteramount = jobj.getDouble("principalInPaymentCurrency");
            String documentId = jobj.getString("repaymentscheduleid");
            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(RepaymentDetails.class.getName(), documentId);
            RepaymentDetails RD = (RepaymentDetails) resultObject.getEntityList().get(0);
            JournalEntry je = RD.getDisbursement().getJournalEntry();
            Date DisbursementDate = je.getEntryDate();

            double paymentExternalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.getString("externalcurrencyrate")) ? 1.0 : Double.parseDouble(paramJobj.getString("externalcurrencyrate"));

            KwlReturnObject bAmt = null;
            if (exchangeRate != paymentExternalCurrencyRate) {
                bAmt = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(requestParams, 1.0, transactionCurrencyId, currencyid, DisbursementDate, exchangeRate, paymentExternalCurrencyRate);
            } else {
                bAmt = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0, transactionCurrencyId, currencyid, DisbursementDate, exchangeRate);
            }
            double oldrate = (Double) bAmt.getEntityList().get(0);
            newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
            amount = (enteramount - (enteramount / newrate) * oldrate) / newrate;
            KwlReturnObject bAmtActual = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, DisbursementDate, newrate);
            actualAmount += (Double) bAmtActual.getEntityList().get(0);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("LoanRepaymentForexGailLossAmountSameCurrency : " + ex.getMessage(), ex);
        }
        return (actualAmount);
    }

    public double LoanRepaymentForexGailLossAmount(JSONObject paramJobj, JSONObject jobj, Receipt receipt, String transactionCurrencyId, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();

        double enterAmountPaymentCurrencyOld = 0;
        double enterAmountTrancastionCurrencyNew = 0;
        double amountdiff = 0;
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            requestParams.put(Constants.globalCurrencyKey, paramJobj.optString(Constants.currencyKey));
            requestParams.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
            requestParams.put("dateformat", authHandler.getDateOnlyFormatter(paramJobj));
            Double enteramount = jobj.getDouble("enteramount");
            String documentId = jobj.getString("repaymentscheduleid");
            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(RepaymentDetails.class.getName(), documentId);
            RepaymentDetails RD = (RepaymentDetails) resultObject.getEntityList().get(0);
            JournalEntry je = RD.getDisbursement().getJournalEntry();
            Date DisbursementDate = je.getEntryDate();
            externalCurrencyRate = je.getExternalCurrencyRate();

            if (!StringUtil.isNullOrEmpty(transactionCurrencyId) && !StringUtil.isNullOrEmpty(transactionCurrencyId) && !transactionCurrencyId.equals(currencyid)) {
                enterAmountTrancastionCurrencyNew = enteramount;
                if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString())) {
                    double exchangeratefortransaction = Double.parseDouble(jobj.optString("exchangeratefortransaction", "1"));
                    enterAmountTrancastionCurrencyNew = enteramount / exchangeratefortransaction;
                    enterAmountTrancastionCurrencyNew = authHandler.round(enterAmountTrancastionCurrencyNew, companyid);
                    KwlReturnObject bAmtCurrencyFilter = null;
                    bAmtCurrencyFilter = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, enterAmountTrancastionCurrencyNew, transactionCurrencyId, currencyid, DisbursementDate, externalCurrencyRate);
                    enterAmountPaymentCurrencyOld = authHandler.round((Double) bAmtCurrencyFilter.getEntityList().get(0), companyid);
                    amountdiff = enteramount - enterAmountPaymentCurrencyOld;
                }
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("LoanRepaymentForexGailLossAmount : " + ex.getMessage(), ex);
        }
        return (amountdiff);
    }

    public void saveCNDNDetailObject(JSONObject paramJobj, JSONArray jSONArrayAgainstCNDN, Receipt receipt, int type, Map<String, Object> counterMap) throws SessionExpiredException, ServiceException, AccountingException {
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
            String person = "";
            StringBuffer billno = new StringBuffer();
            if (type == Constants.PaymentAgainstCNDN) {
                JSONArray drAccArr = jSONArrayAgainstCNDN;
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    double paidncamount = Double.parseDouble(jobj.optString("enteramount","0.0"));
                    double amountdue = Double.parseDouble(jobj.optString("amountdue","0.0"));
                    String dnnoteid = jobj.optString("documentid");
                    String receiptId = receipt.getID();
                    int srNoForRow = StringUtil.isNullOrEmpty(jobj.optString("srNoForRow",null)) ? 0 : Integer.parseInt(jobj.optString("srNoForRow"));
                    if ((!jobj.optString("documentno").equalsIgnoreCase("undefined")) && (!jobj.optString("documentno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("documentno") + ",");
                    }
                    KwlReturnObject cnResult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnnoteid);
                    DebitNote debitNote = (DebitNote) cnResult.getEntityList().get(0);
                    String tocurrency = receipt.getCurrency().getCurrencyID();
                    String fromcurrency = receipt.getCurrency().getCurrencyID();
                    double exchangeratefortransaction = 1;
                    double amountinpaymentcurrency = amountdue;
                    double paidamountinpaymentcurrency = paidncamount;
                    if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(jobj.optString("currencyidtransaction", "")) && !jobj.optString("currencyidtransaction", "").equals(receipt.getCurrency().getCurrencyID())) {
                        tocurrency = jobj.optString("currencyidtransaction", "");
                        fromcurrency = receipt.getCurrency().getCurrencyID();
                        exchangeratefortransaction = Double.parseDouble(jobj.optString("exchangeratefortransaction", "1"));
                        amountdue = amountinpaymentcurrency / Double.parseDouble(jobj.opt("exchangeratefortransaction").toString());
                        amountdue = authHandler.round(amountdue, companyid);
                        paidncamount = paidamountinpaymentcurrency / Double.parseDouble(jobj.get("exchangeratefortransaction").toString());
                        paidncamount = authHandler.round(paidncamount, companyid);
                    }
                    /*
                     * Amount received against DN will be converted to base
                     * currency as per spot rate of DN
                     */
                    double dnExternalCurrencyRate = 0d;
                    Date dnCreationDate = null;
                    dnCreationDate = debitNote.getCreationDate();
                    if (debitNote.isIsOpeningBalenceDN()) {
                        dnExternalCurrencyRate = debitNote.getExchangeRateForOpeningTransaction();
                    } else {
                        dnExternalCurrencyRate = debitNote.getJournalEntry().getExternalCurrencyRate();
                    }
                    if (debitNote.isIsOpeningBalenceDN()) {
                        if (debitNote.isConversionRateFromCurrencyToBase()) {
                            dnExternalCurrencyRate = 1 / dnExternalCurrencyRate;
                        }
                    }
                    double amountPaidAgainstDNInPaymentCurrency = Double.parseDouble(jobj.optString("enteramount","0.0"));
                    double amountPaidAgainstDNInBaseCurrency = amountPaidAgainstDNInPaymentCurrency;
                    KwlReturnObject bAmt = null;
                    HashMap<String, Object> requestParams = new HashMap();
                    requestParams.put(Constants.companyid, companyid);
                    requestParams.put(Constants.globalCurrencyKey, company.getCurrency().getCurrencyID());
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, paidncamount, debitNote.getCurrency().getCurrencyID(), dnCreationDate, dnExternalCurrencyRate);
                    amountPaidAgainstDNInBaseCurrency = (Double) bAmt.getEntityList().get(0);
                    amountPaidAgainstDNInBaseCurrency = authHandler.round(amountPaidAgainstDNInBaseCurrency, companyid);
                    //Revalution Journal Entrey for Receipt to Customer for debit notes
                    ReevalJournalEntryCNDN(paramJobj, debitNote, preferences, exchangeratefortransaction, amountPaidAgainstDNInPaymentCurrency, counterMap);

                    person = " Against debit note ";
                    if (isAgainstDN) {
                        KwlReturnObject cnjedresult = accReceiptDAOobj.updateCnAmount(dnnoteid, paidncamount);
                        KwlReturnObject cnopeningjedresult = accReceiptDAOobj.updateCnOpeningAmountDue(dnnoteid, paidncamount);
                        cnjedresult = accReceiptDAOobj.saveCustomerCnPaymenyHistory(dnnoteid, paidncamount, amountdue, receiptId);
                    } else {// make payment against vendor debit note and also customer credit note.
                        KwlReturnObject cnjedresult;
                        if (receipt.getApprovestatuslevel() == Constants.APPROVED_STATUS_LEVEL) {
                         cnjedresult = accReceiptDAOobj.updateDnAmount(dnnoteid, paidncamount);
                         KwlReturnObject opencnjedresult = accReceiptDAOobj.updateDnOpeningAmountDue(dnnoteid, paidncamount);
                         KwlReturnObject openingDnBaseAmtDueResult = accReceiptDAOobj.updateDnOpeningBaseAmountDue(dnnoteid, amountPaidAgainstDNInBaseCurrency);
                        }
                        HashMap<String, String> hashMapCnPayment = new HashMap<String, String>();
                        hashMapCnPayment.put("cnnoteid", dnnoteid);
                        hashMapCnPayment.put("paymentId", receiptId);
                        hashMapCnPayment.put("originalamountdue", amountdue + "");
                        hashMapCnPayment.put("paidncamount", paidncamount + "");
                        hashMapCnPayment.put("tocurrency", tocurrency);
                        hashMapCnPayment.put("fromcurrency", fromcurrency);
                        hashMapCnPayment.put("exchangeratefortransaction", exchangeratefortransaction + "");
                        hashMapCnPayment.put("amountinpaymentcurrency", amountinpaymentcurrency + "");
                        hashMapCnPayment.put("paidamountinpaymentcurrency", paidamountinpaymentcurrency + "");
                        hashMapCnPayment.put("amountinbasecurrency", amountPaidAgainstDNInBaseCurrency + "");
                        hashMapCnPayment.put("description", jobj.optString("description"));
                        hashMapCnPayment.put("gstCurrencyRate", jobj.optString("gstCurrencyRate", "0.0"));
                        hashMapCnPayment.put("srNoForRow", "" + srNoForRow);
                        if (jobj.has("jedetail") && jobj.get("jedetail") != null) {
                            hashMapCnPayment.put("jedetail", (String) jobj.get("jedetail"));
                        } else {
                            hashMapCnPayment.put("jedetail", "");
                        }
                        cnjedresult = accReceiptDAOobj.saveCustomerDnPaymenyHistory(hashMapCnPayment);
                    }
                    String rowJeId = rowJeId = jobj.optString("rowjedid");
                    KwlReturnObject returnObject = accReceiptDAOobj.getCustomerDnPayment(receiptId, dnnoteid);
                    List<DebitNotePaymentDetails> list = returnObject.getEntityList();
                    for (DebitNotePaymentDetails dnpd : list) {
                        String id = dnpd.getID() != null ? dnpd.getID() : "";
                        HashMap<String, JSONArray> jcustomarrayMap = receipt.getJcustomarrayMap();
                        JSONArray jcustomarray = jcustomarrayMap.get(rowJeId);
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                        customrequestParams.put("modulerecid", rowJeId);
                        customrequestParams.put("recdetailId", id);
                        customrequestParams.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                        customrequestParams.put(Constants.companyKey, companyid);
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
                             * payment method for receipt against Debit Note
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
            throw ServiceException.FAILURE("saveCNDNDetailObject : " + e.getMessage(), e);
        }
    }

    public double ReevalJournalEntryCNDN(JSONObject paramJobj, DebitNote debitNote, CompanyAccountPreferences preferences, double exchangeratefortransaction, double amountdue, Map<String, Object> counterMap) throws SessionExpiredException, ServiceException, AccountingException {
        double finalAmountReval = 0;
        try {
            String basecurrency = paramJobj.optString(Constants.globalCurrencyKey);
            double ratio = 0;
            double amountReval = 0;
            Date tranDate = null;
            double exchangeRate = 0.0;
            double exchangeRateReval = 0.0;
            Map<String, Object> GlobalParams = new HashMap<>();
            GlobalParams.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
            GlobalParams.put(Constants.globalCurrencyKey, basecurrency);
            GlobalParams.put("dateformat", authHandler.getDateFormatter(paramJobj));
            boolean isopeningBalanceInvoice = debitNote.isIsOpeningBalenceDN();
            tranDate = debitNote.getCreationDate();
            if (!debitNote.isNormalDN()) {
                exchangeRate = debitNote.getExchangeRateForOpeningTransaction();
                exchangeRateReval = exchangeRate;
            } else {
                exchangeRate = debitNote.getJournalEntry().getExternalCurrencyRate();
                exchangeRateReval = exchangeRate;
            }

            Map<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", debitNote.getID());
            invoiceId.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
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
            if (debitNote.getCurrency() != null) {
                currid = debitNote.getCurrency().getCurrencyID();
            }
            KwlReturnObject bAmt = null;
            if (isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            }
            double oldrate = (Double) bAmt.getEntityList().get(0);
            if (revalueationHistory == null && isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRateReval);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRateReval);
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
                String oldRevaluationJE = debitNote.getRevalJeId();
                /**
                 * added transactionID and transactionModuleID to Realised JE.
                 */
                counterMap.put("transactionModuleid", debitNote.isIsOpeningBalenceDN() ? (debitNote.isdNForVendor() ? Constants.Acc_opening_Vendor_DebitNote : Constants.Acc_opening_Customer_DebitNote) : Constants.Acc_Debit_Note_ModuleId);
                counterMap.put("transactionId", debitNote.getID());
                String revaljeid = PostJEFORReevaluation(paramJobj, finalAmountReval, preferences.getCompany().getCompanyID(), preferences, basecurrency, oldRevaluationJE, counterMap);
                debitNote.setRevalJeId(revaljeid);
                finalAmountReval = 0;
            }

        } catch (SessionExpiredException | ServiceException e) {
            throw ServiceException.FAILURE("saveReceipt : " + e.getMessage(), e);
        } catch (Exception e) {
        }
        return finalAmountReval;
    }

    public HashSet<ReceiptAdvanceDetail> advanceDetailObject(JSONObject paramJobj, JSONArray jSONArrayAdvance, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        HashSet advanceDetails = null;
        try {
            advanceDetails = new HashSet<ReceiptAdvanceDetail>();
            String companyid = paramJobj.optString(Constants.companyKey);
            Map<String, Object> counterMap = new HashMap<>();
            counterMap.put("counter", 0);//this is used to avoid JE Sequence Number
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            HashMap<String, Object> prefparams = new HashMap<>();
            prefparams.put("id", companyid);
            Object columnPref = kwlCommonTablesDAOObj.getRequestedObjectFields(IndiaComplianceCompanyPreferences.class, new String[]{"istaxonadvancereceipt"}, prefparams);
            boolean istaxonadvancereceipt = false;
            if (columnPref != null) {
                istaxonadvancereceipt = Boolean.parseBoolean(columnPref.toString());
            }
            for (int i = 0; i < jSONArrayAdvance.length(); i++) {
                JSONObject jobj = jSONArrayAdvance.getJSONObject(i);
                double amountReceived = jobj.getDouble("enteramount");
                KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) cmpresult.getEntityList().get(0);
                ReceiptAdvanceDetail advanceDetail = new ReceiptAdvanceDetail();
                advanceDetail.setId(StringUtil.generateUUID());
                advanceDetail.setCompany(company);
                advanceDetail.setExchangeratefortransaction(1.0d);
                advanceDetail.setReceipt(receipt);
                advanceDetail.setAmount(amountReceived);

                /*
                 * If Make Payment against Customer and used advance receipt
                 * against it then need to maintain receipt object for reference
                 */
                if (!StringUtil.isNullOrEmpty(jobj.optString("documentid", ""))) { // documentid will be non-NULL only in two cases - 1) RP against Customer- advance payment for malaysia  2) refund against vendor for any country
                    if (Boolean.parseBoolean(paramJobj.getString("iscustomer")) && Integer.parseInt(company.getCountry().getID()) == Constants.malaysian_country_id) {   // Advance payment against customer for malaysia country
                        String taxid = jobj.getString("documentid");
                        KwlReturnObject taxResult = accountingHandlerDAOobj.getObject(Tax.class.getName(), taxid);
                        Tax taxObj = (Tax) taxResult.getEntityList().get(0);
                        advanceDetail.setGST(taxObj);

                    } else {                             // Refund case against vendor for any country
                        String advanceDetailId = jobj.getString("documentid");
                        advanceDetail.setAdvancedetailid(advanceDetailId);
                        double amountReceivedConverted = jobj.getDouble("enteramount");
                        if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString())) {
                            double adjustedRate = Double.parseDouble(jobj.optString("exchangeratefortransaction", "1.0").toString());
                            amountReceivedConverted = amountReceived / adjustedRate;
                            amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                            advanceDetail.setExchangeratefortransaction(adjustedRate);
                        }
                        if (!StringUtil.isNullOrEmpty(advanceDetailId)) {
                            //JE For Receipt which is of Opening Type                       
                            double exchangeratefortransaction = Double.parseDouble(jobj.optString("exchangeratefortransaction", "1"));
                            String basecurrency = paramJobj.optString(Constants.globalCurrencyKey);
                            double finalAmountReval = ReevalJournalEntryForAdvancePayment(paramJobj, advanceDetailId, amountReceived, exchangeratefortransaction);
                            if (finalAmountReval != 0) {
                                /**
                                 * added transactionID and transactionModuleID
                                 * to Realised JE.
                                 */
                                counterMap.put("transactionModuleid", receipt.isIsOpeningBalenceReceipt() ? Constants.Acc_opening_Receipt : Constants.Acc_Receive_Payment_ModuleId);
                                counterMap.put("transactionId", receipt.getID());
                                String revaljeid = PostJEFORReevaluation(paramJobj, finalAmountReval, companyid, preferences, basecurrency, null, counterMap);
                                advanceDetail.setRevalJeId(revaljeid);
                            }
                        }

                        /*
                         * amountReceived with negative sign as we have to
                         * substract amount
                         */
                        if (receipt.getApprovestatuslevel() == Constants.APPROVED_STATUS_LEVEL) {
                            accountingHandlerDAOobj.updateAdvanceDetailAmountDueOnAmountReceived(advanceDetailId, (-amountReceivedConverted));
                        }
                    }
                }

                /*
                 * Receive Payment against customer - amountdue is same as paid
                 * amount. User can link this advance amount aginst invoice
                 * Receive Payment against vendor - deposite/refund amount will
                 * no be used for other transactions. so need to set amountdue
                 * as 0
                 */
                if (receipt.getCustomer() != null) {
                    advanceDetail.setAmountDue(amountReceived);
                } else if (receipt.getVendor() != null && StringUtil.isNullOrEmpty(advanceDetail.getAdvancedetailid())) { // if document is not linked to refund receipt amount due is amount entered in reciept
                    advanceDetail.setAmountDue(amountReceived);
                } else if (receipt.getVendor() != null) {
                    advanceDetail.setAmountDue(0);
                }
                advanceDetail.setAdvanceType(type);
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
                if (jobj.has("prtaxid")) {
                    String lineLevelTaxId = jobj.getString("prtaxid");
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), lineLevelTaxId);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax != null) {
                        advanceDetail.setTax(rowtax);
                        advanceDetail.setTaxamount(jobj.optDouble("taxamount", 0.0));
                    }
                }
                HashMap<String, JSONArray> jcustomarrayMap = receipt.getJcustomarrayMap();
                JSONArray jcustomarray = jcustomarrayMap.get(advanceDetail.getROWJEDID());
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                customrequestParams.put("modulerecid", advanceDetail.getROWJEDID());
                customrequestParams.put("recdetailId", advanceDetail.getId());
                customrequestParams.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
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
                     * method for payment against Advance payment
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
                // ERP-32829

                if (istaxonadvancereceipt && (receipt.getCompany().getCountry().getID().equalsIgnoreCase(""+Constants.indian_country_id)) &&
                        jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty((String) jobj.optString("LineTermdetails"))) {
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
                        purchaseOrderDetailsTermsMap.put("podetails", advanceDetail.getId());
                        /**
                         * ERP-32829 
                         */
                        purchaseOrderDetailsTermsMap.put("isDefault", termObject.optString("isDefault", "false"));
                        purchaseOrderDetailsTermsMap.put("productentitytermid", termObject.optString("productentitytermid"));
                        purchaseOrderDetailsTermsMap.put(Constants.useridKey, receipt.getCreatedby().getUserID());
                        purchaseOrderDetailsTermsMap.put("product", termObject.opt("productid"));
                        purchaseOrderDetailsTermsMap.put("createdOn", new Date());

                        accReceiptDAOobj.saveAdvanceDetailsTermMap(purchaseOrderDetailsTermsMap);
                    }
                    double recTermAmount=jobj.optDouble("recTermAmount", 0.0);
                    advanceDetail.setTaxamount(recTermAmount);
                    advanceDetail.setAmountDue(amountReceived);
                    if (jobj.has("productid")) {
                        String productId = jobj.optString("productid");
                        KwlReturnObject resJED = accountingHandlerDAOobj.getObject(Product.class.getName(), productId);
                        Product product = (Product) resJED.getEntityList().get(0);
                        if (product != null) {
                            advanceDetail.setProduct(product);
                        }

                    }
                    if ((receipt.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id))) {
                        /**
                         * Save GST History Customer/Vendor data
                         */
                        jobj.put("detaildocid", advanceDetail.getId());
                        jobj.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                        fieldDataManagercntrl.createRequestMapToSaveTaxClassHistory(jobj);
                    }
                }
            }

        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return advanceDetails;
    }

    public double ReevalJournalEntryForAdvancePayment(JSONObject paramJobj, String advancedetailID, double linkReceiptAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {
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
            GlobalParams.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
            GlobalParams.put(Constants.globalCurrencyKey, basecurrency);
            GlobalParams.put("dateformat", authHandler.getDateOnlyFormat());

            List<Object[]> advPayDetails = accReceiptDAOobj.getAdvancePaymentDetails(advancedetailID);
            if (!advPayDetails.isEmpty()) {
                Object[] objArray = (Object[]) advPayDetails.get(0);
                String paymentID = objArray[0].toString();
                String currencyID = objArray[1].toString();
                String JournalEnteryID = objArray[2].toString();
                KwlReturnObject resultObj = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), JournalEnteryID);
                JournalEntry journalEntry = (JournalEntry) resultObj.getEntityList().get(0);

                if (!StringUtil.isNullOrEmpty(paymentID) && !StringUtil.isNullOrEmpty(currencyID) && journalEntry != null) {
                    exchangeRate = journalEntry.getExternalCurrencyRate();
                    exchangeRateReval = exchangeRate;
                    tranDate = journalEntry.getEntryDate();
                    Map<String, Object> invoiceId = new HashMap<>();
                    invoiceId.put("invoiceid", paymentID);
                    invoiceId.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
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
                    if (!StringUtil.isNullOrEmpty(currencyID)) {
                        currid = currencyID;
                    }
                    //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
                    KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
                    double oldrate = (Double) bAmt.getEntityList().get(0);
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRateReval);
                    double newrate = (Double) bAmt.getEntityList().get(0);
                    ratio = oldrate - newrate;
                    if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                        exchangeratefortransaction = newrate;
                    }
                    double amountdueNew = amountdue / exchangeratefortransaction;
                    amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
                    amountReval = ratio * amountdueNew;
                    finalAmountReval = finalAmountReval + amountReval;
                }

            }

        } catch (Exception e) {
            throw ServiceException.FAILURE("ReevalJournalEntryForAdvancePayment : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }

    public HashSet saveReceiptDetailLoanObject(JSONObject paramJobj, JSONArray jSONArrayLoanDisbursement, Receipt receipt) throws SessionExpiredException, ServiceException, AccountingException {
        HashSet payDetails = null;
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            String methodid = paramJobj.optString("pmtmethod");
            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap)) {
                sessionHandlerImpl sess = new sessionHandlerImpl();
                sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
            }
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            JSONArray jArr = new JSONArray();
            if (jSONArrayLoanDisbursement != null) {
                jArr = jSONArrayLoanDisbursement;
            }
            payDetails = saveReceiptDetailsLoan(receipt, company, jArr);
        } catch (Exception e) {
            throw ServiceException.FAILURE("saveReceiptDetailLoanObject : " + e.getMessage(), e);
        }
        return payDetails;
    }

    private HashSet saveReceiptDetailsLoan(Receipt receipt, Company company, JSONArray jArr) throws JSONException, ServiceException, UnsupportedEncodingException {
        HashSet details = new HashSet();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            ReceiptDetailLoan rd = new ReceiptDetailLoan();
            rd.setSrno(i + 1);
            rd.setID(StringUtil.generateUUID());
            double amountReceived = jobj.getDouble("enteramount");// amount in receipt currency
            double amountReceivedConverted = jobj.getDouble("enteramount"); // amount in loan disbursement currency
            rd.setAmount(jobj.getDouble("enteramount"));
            rd.setCompany(company);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(RepaymentDetails.class.getName(), jobj.getString("repaymentscheduleid"));
            RepaymentDetails RepaymentDetailObject = (RepaymentDetails) result.getEntityList().get(0);
            double repaymentDetailAmountDue = RepaymentDetailObject.getAmountdue();
            KWLCurrency repaymentDetailCurrency = RepaymentDetailObject.getDisbursement().getCurrency();
            String repaymentDetailCurrencyId = repaymentDetailCurrency.getCurrencyID();
            if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(repaymentDetailCurrencyId) && !repaymentDetailCurrencyId.equals(receipt.getCurrency().getCurrencyID())) {
                double adjustedRate = jobj.optDouble("amountdue", 0) / jobj.optDouble("amountDueOriginal", 0);
                amountReceivedConverted = amountReceived / adjustedRate;
            }
            rd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
            rd.setFromCurrency(repaymentDetailCurrency);
            rd.setToCurrency(receipt.getCurrency());
            amountReceivedConverted = authHandler.round(amountReceivedConverted, company.getCompanyID());
            rd.setAmountInRepaymentDetailCurrency(amountReceivedConverted);
            rd.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate", 0.0));
            rd.setDescription(StringUtil.DecodeText(jobj.optString("description")));
            rd.setReceipt(receipt);
            rd.setRepaymentDetail(RepaymentDetailObject);
            if (jobj.has("rowjedid")) {
                rd.setROWJEDID(jobj.getString("rowjedid"));
            }
            if (jobj.has("srNoForRow")) {
                int srNoForRow = StringUtil.isNullOrEmpty("srNoForRow") ? 0 : Integer.parseInt(jobj.getString("srNoForRow"));
                rd.setSrNoForRow(srNoForRow);
            }
            if (jobj.has("jedetail") && jobj.get("jedetail") != null) {
                KwlReturnObject resJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) jobj.get("jedetail"));
                JournalEntryDetail jedObj = (JournalEntryDetail) resJED.getEntityList().get(0);
                rd.setTotalJED(jedObj);
            }

            double amountReceivedConvertedInBaseCurrency = 0d;
            HashMap<String, Object> requestParams = new HashMap();
            requestParams.put(Constants.companyid, company.getCompanyID());
            requestParams.put(Constants.globalCurrencyKey, company.getCurrency().getCurrencyID());
            double DisbursementExternalCurrencyRate = RepaymentDetailObject.getDisbursement().getJournalEntry().getExternalCurrencyRate();
            Date DisbursementCreationDate = RepaymentDetailObject.getDisbursement().getJournalEntry().getEntryDate();

            String fromcurrencyid = repaymentDetailCurrencyId;
            KwlReturnObject bAmt = null;
            bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountReceivedConverted, fromcurrencyid, DisbursementCreationDate, DisbursementExternalCurrencyRate);
            amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
            amountReceivedConvertedInBaseCurrency = authHandler.round(amountReceivedConvertedInBaseCurrency, company.getCompanyID());
            rd.setAmountInBaseCurrency(amountReceivedConvertedInBaseCurrency);

            RepaymentDetailObject.setAmountdue(repaymentDetailAmountDue - amountReceivedConverted);
            RepaymentDetailObject.setPaymentStatus(PaymentStatus.Paid);
            if (receipt != null) {
                double receiptAmountDue = receipt.getOpeningBalanceAmountDue();
                receiptAmountDue -= amountReceived;
                receipt.setOpeningBalanceAmountDue(receiptAmountDue);
                receipt.setOpeningBalanceBaseAmountDue(receipt.getOpeningBalanceBaseAmountDue() - amountReceivedConvertedInBaseCurrency);
            }
            details.add(rd);
        }
        return details;
    }

    public HashSet receiptDetailObject(JSONObject paramJobj, JSONArray jSONArrayAgainstInvoice, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        HashSet payDetails = null;
        GoodsReceipt goodsReceipt = null;
        try {

            String companyid = paramJobj.optString(Constants.companyKey);
            String methodid = paramJobj.optString("pmtmethod");
            if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap, false)) {
                sessionHandlerImpl sess = new sessionHandlerImpl();
                sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
            }
            HashMap<String, JSONArray> Map1 = new HashMap();

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            JSONArray jArr = new JSONArray();
            if (jSONArrayAgainstInvoice != null) {
                jArr = jSONArrayAgainstInvoice;
            }
            payDetails = saveReceiptRows(paramJobj, receipt, company, jArr, goodsReceipt, type);
        } catch (Exception e) {
            throw ServiceException.FAILURE("receiptDetailObject : " + e.getMessage(), e);
        }
        return payDetails;
    }

    private HashSet saveReceiptRows(JSONObject paramJobj, Receipt receipt, Company company, JSONArray jArr, GoodsReceipt greceipt, int type) throws JSONException, ServiceException, UnsupportedEncodingException {
        HashSet details = new HashSet();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            ReceiptDetail rd = new ReceiptDetail();
            rd.setSrno(i + 1);
            rd.setID(StringUtil.generateUUID());
            double amountReceived = 0.0;
            double amountReceivedConverted = 0.0;
            double discountAmount = jobj.optDouble("discountname", 0.0);
            double discountAmountInInvoiceCurrecny = 0.0;
            boolean isDiscountFieldChanged = jobj.optBoolean("isDiscountFieldChanged", false);
            
            if (paramJobj.optBoolean("isEdit", false)) {
                isDiscountFieldChanged = paramJobj.optBoolean("isDiscountFieldChanged");
            }
            amountReceived = jobj.getDouble("enteramount");// amount in receipt currency
            amountReceivedConverted = jobj.getDouble("enteramount"); // amount in invoice currency
            
            rd.setAmount(jobj.getDouble("enteramount"));
            rd.setDiscountAmount(discountAmount);
            rd.setDiscountFieldEdited(isDiscountFieldChanged);
            
            rd.setCompany(company);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("documentid"));
            Invoice invoice = (Invoice) result.getEntityList().get(0);
            boolean isClaimedInvoice = invoice.getBadDebtType() == Constants.Invoice_Claimed || invoice.getBadDebtType() == Constants.Invoice_Recovered;
            rd.setInvoice((Invoice) result.getEntityList().get(0));
            if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(invoice.getCurrency().getCurrencyID()) && !invoice.getCurrency().getCurrencyID().equals(receipt.getCurrency().getCurrencyID())) {
                rd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
                rd.setFromCurrency(invoice.getCurrency());
                rd.setToCurrency(receipt.getCurrency());
                double adjustedRate = jobj.optDouble("amountdue", 0) / jobj.optDouble("amountDueOriginal", 0);
                amountReceivedConverted = amountReceived / adjustedRate;;
                amountReceivedConverted = authHandler.round(amountReceivedConverted, company.getCompanyID());
                discountAmountInInvoiceCurrecny = authHandler.round((discountAmount / adjustedRate), company.getCompanyID());
                rd.setAmountInInvoiceCurrency(amountReceivedConverted);
                rd.setAmountDueInInvoiceCurrency(jobj.optDouble("amountDueOriginal", 0));
                rd.setAmountDueInPaymentCurrency(jobj.optDouble("amountdue", 0));
            } else {
                rd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
                rd.setFromCurrency(invoice.getCurrency());
                rd.setToCurrency(receipt.getCurrency());
                amountReceivedConverted = authHandler.round(amountReceived, company.getCompanyID());
                discountAmountInInvoiceCurrecny = discountAmount;
                rd.setAmountInInvoiceCurrency(amountReceivedConverted);
                rd.setAmountDueInInvoiceCurrency(jobj.optDouble("amountDueOriginal", 0));
                rd.setAmountDueInPaymentCurrency(jobj.optDouble("amountdue", 0));
            }
            rd.setDiscountAmountInInvoiceCurrency(discountAmountInInvoiceCurrecny);
            rd.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate", 0.0));
            rd.setDescription(StringUtil.DecodeText(jobj.optString("description")));
            if (greceipt != null) {
                rd.setGoodsReceipt(greceipt);
            }
            rd.setReceipt(receipt);
            if (jobj.has("rowjedid")) {
                rd.setROWJEDID(jobj.getString("rowjedid"));
            }
            if (jobj.has("srNoForRow")) {
                int srNoForRow = StringUtil.isNullOrEmpty("srNoForRow") ? 0 : Integer.parseInt(jobj.getString("srNoForRow"));
                rd.setSrNoForRow(srNoForRow);
            }
            if (jobj.has("jedetail") && jobj.get("jedetail") != null) {
                KwlReturnObject resJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) jobj.get("jedetail"));
                JournalEntryDetail jedObj = (JournalEntryDetail) resJED.getEntityList().get(0);
                rd.setTotalJED(jedObj);
            }
            HashMap<String, JSONArray> jcustomarrayMap = receipt.getJcustomarrayMap();
            String companyid = company.getCompanyID();
            JSONArray jcustomarray = jcustomarrayMap.get(rd.getROWJEDID());
            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
            customrequestParams.put("customarray", jcustomarray);
            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
            customrequestParams.put("modulerecid", rd.getROWJEDID());
            customrequestParams.put("recdetailId", rd.getID());
            customrequestParams.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
            customrequestParams.put(Constants.companyKey, companyid);
            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), rd.getROWJEDID());
                AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), rd.getROWJEDID());
                JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
            }
            
            if(jobj.has("methodjedid") && !StringUtil.isNullOrEmpty(jobj.optString("methodjedid"))) {
                customrequestParams.clear();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                customrequestParams.put("modulerecid", jobj.optString("methodjedid"));
                customrequestParams.put("recdetailId", rd.getID());
                customrequestParams.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
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
            details.add(rd);
            double amountReceivedConvertedInBaseCurrency = 0d;
//            double discountAmountConvertedInBaseCurrency = 0d;
//            double amountReceivedAndDiscountInBase = 0d;
            HashMap<String, Object> requestParams = new HashMap();
            requestParams.put(Constants.companyid, company.getCompanyID());
            requestParams.put(Constants.globalCurrencyKey, company.getCurrency().getCurrencyID());
            /*
             * Amount received against Invoice will be converted to base
             * currency as per spot rate of INvoice
             */
            double grExternalCurrencyRate = 0d;
            Date grCreationDate = null;
            grCreationDate = invoice.getCreationDate();
            if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                grExternalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
            } else {
                grExternalCurrencyRate = invoice.getJournalEntry().getExternalCurrencyRate();
            }
            String fromcurrencyid = invoice.getCurrency().getCurrencyID();
            KwlReturnObject bAmt = null;
            KwlReturnObject bDiscountAmt = null;

            if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                if (invoice.isConversionRateFromCurrencyToBase()) {
                    grExternalCurrencyRate = 1 / grExternalCurrencyRate;
                }
            }
            bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountReceivedConverted, fromcurrencyid, grCreationDate, grExternalCurrencyRate);
            amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
            amountReceivedConvertedInBaseCurrency = authHandler.round(amountReceivedConvertedInBaseCurrency, companyid);
            rd.setAmountInBaseCurrency(amountReceivedConvertedInBaseCurrency);
            
//            bDiscountAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, discountAmountInInvoiceCurrecny, fromcurrencyid, grCreationDate, grExternalCurrencyRate);
//            discountAmountConvertedInBaseCurrency = (Double) bDiscountAmt.getEntityList().get(0);
//            discountAmountConvertedInBaseCurrency = authHandler.round(discountAmountConvertedInBaseCurrency, companyid);
            double paymentJournalEntryExchangeRate = receipt.getJournalEntry() != null ? receipt.getJournalEntry().getExternalCurrencyRate() : 1;
           
//            amountReceivedAndDiscountInBase = ((Double) bAmt.getEntityList().get(0) + (Double) bDiscountAmt.getEntityList().get(0));
//            amountReceivedAndDiscountInBase = authHandler.round(amountReceivedAndDiscountInBase, companyid);
            /**
             * if payment currency is USD which is not a base currency and
             * document currency is in Malaysia than the exchange rate is taken
             * from payment's journal entry to convert the discount amount in
             * base i.e is in SGD.
             */
            if (paymentJournalEntryExchangeRate != 1 && (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(invoice.getCurrency().getCurrencyID()) && !invoice.getCurrency().getCurrencyID().equals(receipt.getCurrency().getCurrencyID()))) {
                bDiscountAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, discountAmount, fromcurrencyid, grCreationDate, paymentJournalEntryExchangeRate);
                rd.setDiscountAmountInBase(authHandler.round((Double) bDiscountAmt.getEntityList().get(0), companyid));
            } else if (paymentJournalEntryExchangeRate != 1) {      //if payment currency us USD which is not a base currency and document currency is USD then getting the exchange rate from payment's journal entry to convert the discount amount in base i.e is in SGD.
                bDiscountAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, discountAmountInInvoiceCurrecny, fromcurrencyid, grCreationDate, paymentJournalEntryExchangeRate);
                rd.setDiscountAmountInBase(authHandler.round((Double) bDiscountAmt.getEntityList().get(0), companyid));
            } else {        //if payment currency is in SGD which is a base currency and document currency is USD then getting the exchange rate from line level exchange rate column to convert the discount amount in base currency i.e SGD.
                rd.setDiscountAmountInBase(authHandler.round(discountAmountInInvoiceCurrecny * rd.getExchangeRateForTransaction(), companyid));
            }
            /*
             * Store the date on which the amount due has been set to zero
             */

            KwlReturnObject invoiceResult = null;
            if (receipt.getApprovestatuslevel() == Constants.APPROVED_STATUS_LEVEL) {        //if receipt is pending for approval the invoice amount due will not be updated
//               invoiceResult=updateInvoiceAmountDueAndReturnResult(invoice, receipt, company, (amountReceivedConverted+discountAmountInInvoiceCurrecny), (amountReceivedConvertedInBaseCurrency+discountAmountConvertedInBaseCurrency));
               invoiceResult=updateInvoiceAmountDueAndReturnResult(invoice, receipt, company, (amountReceivedConverted+discountAmountInInvoiceCurrecny));
            }
            if (!isClaimedInvoice) {
                if (invoiceResult != null && invoiceResult.getEntityList() != null && invoiceResult.getEntityList().size() > 0) {
                    Invoice inv = (Invoice) invoiceResult.getEntityList().get(0);
                    if (inv.isIsOpeningBalenceInvoice() && inv.getOpeningBalanceAmountDue() == 0) {
                        try {
                            DateFormat df = authHandler.getDateOnlyFormatter(paramJobj);
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();
                            dataMap.put("amountduedate", df.parse(paramJobj.optString("creationdate")));
                            accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                        } catch (Exception ex) {
                            System.out.println("" + ex.getMessage());
                        }
                    } else if (inv.getInvoiceamountdue() == 0) {
                        try {
                            DateFormat df = authHandler.getDateOnlyFormatter(paramJobj);
                            HashMap<String, Object> dataMap = new HashMap<String, Object>();
                            dataMap.put("amountduedate", df.parse(paramJobj.optString("creationdate")));
                            accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                        } catch (Exception ex) {
                            System.out.println("" + ex.getMessage());
                        }
                    }
                }
            }
            if (receipt != null) {
                double receiptAmountDue = receipt.getOpeningBalanceAmountDue();
                receiptAmountDue -= amountReceived;
                receipt.setOpeningBalanceAmountDue(receiptAmountDue);
                receipt.setOpeningBalanceBaseAmountDue(receipt.getOpeningBalanceBaseAmountDue() - amountReceivedConvertedInBaseCurrency);
            }
        }
        return details;
    }

    public KwlReturnObject updateInvoiceAmountDueAndReturnResult(Invoice invoice, Receipt receipt, Company company, double amountReceivedForInvoice) throws JSONException, ServiceException {
        KwlReturnObject result = null;
        String companyid = company.getCompanyID();
        if (invoice != null) {
            boolean isInvoiceIsClaimed = (invoice.getBadDebtType() == Constants.Invoice_Claimed || invoice.getBadDebtType() == Constants.Invoice_Recovered);
            JSONObject invjson = new JSONObject();
            invjson.put("invoiceid", invoice.getID());
            if(invoice.getShipDate()!=null){    //SDP-12208
                invjson.put("shipdate", invoice.getShipDate());                
            }
            invjson.put(Constants.companyKey, companyid);
            if (isInvoiceIsClaimed) {
                invjson.put(Constants.claimAmountDue, invoice.getClaimAmountDue() - amountReceivedForInvoice);
            } else {
                double invoiceExchangeRate = 1d;
                double finalInvoiceAmtDue = invoice.getInvoiceamountdue() - amountReceivedForInvoice;
                double invoiceAmountDue = invoice.getOpeningBalanceAmountDue();
                Map<String, Object> requestParams = new HashMap();
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", company.getCurrency() != null ? company.getCurrency().getCurrencyID() : "");
                invoiceAmountDue -= amountReceivedForInvoice;
                /**
                 * If invoice is an opening invoice then the exchange rate is
                 * taken from invoice table exchangerateforopeningtransaction
                 * and if it is normal invoice we take exchange rate from JE of
                 * invoice.ERP-40596.
                 */
                if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                    invoiceExchangeRate = invoice.getExchangeRateForOpeningTransaction();
                } else {
                    invoiceExchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
                }
                KwlReturnObject invoicebAmt = null;
                String invoicefromcurrencyid = invoice.getCurrency().getCurrencyID();
                /**
                 * If it is opening invoice and if Conversion rate is from
                 * currency to base check is enabled while creating opening
                 * invoice then we calculate amount in base by using
                 * getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate()
                 * method and if conversion rate is from currency to base check
                 * is unchecked and for normal we use getCurrencyToBaseAmount() method
                 * ERP-40596.
                 */
                if (invoice.isIsOpeningBalenceInvoice() && invoice.isConversionRateFromCurrencyToBase()) {// if Invoice is opening balance Invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                    invoicebAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, invoiceAmountDue, invoicefromcurrencyid, invoice.getCreationDate(), invoiceExchangeRate);
                } else {        //for normal invoice and if conversion rate is from currency to base for opening
                    invoicebAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, finalInvoiceAmtDue, invoicefromcurrencyid, invoice.getCreationDate(), invoiceExchangeRate);
                }
                double returnAmountInBaseAmountDue = authHandler.round(((Double) invoicebAmt.getEntityList().get(0)), companyid);

                invjson.put("openingBalanceAmountDue", invoiceAmountDue);
                invjson.put(Constants.openingBalanceBaseAmountDue, returnAmountInBaseAmountDue);
                invjson.put(Constants.invoiceamountdue, finalInvoiceAmtDue);
                invjson.put(Constants.invoiceamountdueinbase, returnAmountInBaseAmountDue);
            }
            result = accInvoiceDAOObj.updateInvoice(invjson, null);
        }
        return result;
    }

    public JournalEntry journalEntryObject(JSONObject paramJobj, Receipt editReceiptObject, String receiptID) throws SessionExpiredException, ServiceException, AccountingException {
        JournalEntry journalEntry = null;
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            DateFormat df = authHandler.getDateOnlyFormat();
            String currencyid = paramJobj.optString(Constants.currencyKey);
            String userid = paramJobj.optString(Constants.useridKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (paramJobj.getString(Constants.currencyKey)) == null ? currency.getCurrencyID() : paramJobj.getString((Constants.currencyKey));
            String methodid = paramJobj.getString(("pmtmethod"));
            double PaymentCurrencyToPaymentMethodCurrencyRate = StringUtil.getDouble(paramJobj.optString("paymentCurrencyToPaymentMethodCurrencyExchangeRate","1"));
            boolean ismulticurrencypaymentje = StringUtil.getBoolean(paramJobj.optString("ismulticurrencypaymentje"));
             if (!paramJobj.optBoolean(Constants.isdefaultHeaderMap, false)) {
                sessionHandlerImpl sess = new sessionHandlerImpl();
                sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
            }
            boolean jeautogenflag = false;
            String jeSeqFormatId = "";
            Date entryDate = df.parse(paramJobj.getString("creationdate"));
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate","1"));
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);

            if (editReceiptObject == null) {
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put(Constants.companyKey, companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;

                    jeDataMap.put("entrynumber", "");
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                }
            } else if (editReceiptObject != null && editReceiptObject.getJournalEntry() != null) {
                JournalEntry entry = editReceiptObject.getJournalEntry();
                jeDataMap.put("entrynumber", entry.getEntryNumber());
                jeDataMap.put("autogenerated", entry.isAutoGenerated());
                jeDataMap.put(Constants.SEQFORMAT, entry.getSeqformat().getID());
                jeDataMap.put(Constants.SEQNUMBER, entry.getSeqnumber());
                jeDataMap.put(Constants.DATEPREFIX, entry.getDatePreffixValue());
                jeDataMap.put(Constants.DATEAFTERPREFIX, entry.getDateAfterPreffixValue());
                jeDataMap.put(Constants.DATESUFFIX, entry.getDateSuffixValue());
            }
            jeDataMap.put("entrydate", entryDate);
            jeDataMap.put(Constants.companyKey, company.getCompanyID());
            jeDataMap.put("memo", paramJobj.optString("memo"));
            jeDataMap.put(Constants.currencyKey, currencyid);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("PaymentCurrencyToPaymentMethodCurrencyRate", PaymentCurrencyToPaymentMethodCurrencyRate);
            jeDataMap.put("ismulticurrencypaymentje", ismulticurrencypaymentje);
            jeDataMap.put("createdby", userid);
            jeDataMap.put("transactionId", receiptID);
            jeDataMap.put("transactionModuleid", Constants.Acc_Receive_Payment_ModuleId);
            journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return journalEntry;
    }

    /**
     * Description: Validate and Import Deliver Orders data
     *
     * @param paramJobj
     * @return JSONObject
     */
    @Override
    public JSONObject importReceivePaymentJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        try {
            String doAction = paramJobj.getString("do");

            if (doAction.compareToIgnoreCase("import") == 0) {
                jobj = importReceivePaymentRecordsForCSV(paramJobj);
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                String eParams = paramJobj.getString("extraParams");
                JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);

                HashMap<String, Object> requestParams = importHandler.getImportRequestParams(paramJobj);
                requestParams.put("extraParams", extraParams);
                requestParams.put("extraObj", null);
                requestParams.put("servletContext", paramJobj.get("servletContext"));
                requestParams.put("customer", paramJobj.optString("customer"));
                requestParams.put("vendor", paramJobj.optString("vendor"));
                requestParams.put("GL", paramJobj.optString("GL"));

                jobj = importHandler.validateFileData(requestParams);
                jobj.put(Constants.RES_success, true);
            }
        } catch (Exception ex) {
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(AccReceivePaymentModuleService.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return jobj;
    }

    public JSONObject importReceivePaymentRecordsForCSV(JSONObject requestJobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
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
        Invoice invoice = null;
        DebitNote debitNote = null;
        Account account = null;
        double totalAmountPay = 0;
        String exceptionMSg = "";
        String prevRecNo = "";
        double exchangeRateForTransaction = 0.0;
        int isAdvance = 0;
        String[] recarr = null;
        String receivedFrom = "";
        String receivedFromId = "";
        String description = "";
        String previousEntryNo = "";        //to store previous entry no
        String mismatchColumns = "";        //To store the names of mismatched global columns
        try {
            String dateFormat = null, dateFormatId = requestJobj.getString("dateFormat");
            String customerCheck = requestJobj.optString("customer");
            String vendorCheck = requestJobj.optString("vendor");
            String glCheck = requestJobj.optString("GL");
            boolean isCustomer = false;
            boolean isVendor = false;
            boolean isGL = false;
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }
            DateFormat df = new SimpleDateFormat(dateFormat);
            df.setLenient(false);

            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");

            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            Boolean isCurrencyCode = extrareferences.isCurrencyCode();
            
            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
            Company company = (Company) companyObj.getEntityList().get(0);
            String countryCode = company.getCountry().getID();

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
            HashMap currencyMap = accSalesOrderServiceobj.getCurrencyMap(isCurrencyCode);
            KwlReturnObject result = null;
            int nocount = 0;
            if (!StringUtil.isNullOrEmpty(customerCheck)) {
                isCustomer = Boolean.parseBoolean(customerCheck);
            }
            if (!StringUtil.isNullOrEmpty(vendorCheck)) {
                isVendor = Boolean.parseBoolean(vendorCheck);
            }
            if (!StringUtil.isNullOrEmpty(glCheck)) {
                isGL = Boolean.parseBoolean(glCheck);
            }
            String failureMsgFromSave = "";
            List<String> failureMsgsList = new ArrayList<>();                   //List to store the failure Msgs as we have to append all the messages at last of each records as one payment can have multiple line level item
            List <String []> recList=new ArrayList<>();                         //List to store the records as we have to append all the error messages at last as one payment can have multiple line level item
            boolean glCheckFlag=false;                                          //This flag used to check wether there is only alone GL entry in case of receive payment from customer
            boolean globalFieldMismatchFlag = false;                            //This flag is use to check wether Global field is are mismatched or not.
            while (csvReader.readRecord()) {
                if(cnt>2){
                    recList.add(recarr);
                }
                String failureMsg = "";
                recarr = csvReader.getValues();

                if (cnt == 0) {
                    failedRecords.append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\" \"");
                } else if (cnt == 1) {
                    failedRecords.append("\n").append(accSalesOrderServiceobj.createCSVrecord(recarr)).append("\"Error Message\"");
                } else {
                    try {
                        String currencyID = requestJobj.getString(Constants.globalCurrencyKey);

                        String entryNumber = "";
                        if (columnConfig.containsKey("billno")) {
                            entryNumber = recarr[(Integer) columnConfig.get("billno")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(entryNumber)) {
                                failureMsg += "Receipt Number is not available. ";
                            }
                        } else {
                            failureMsg += "Receipt Number column is not found. ";
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
                         * 2. Customer Name if customerID is empty it means
                         * customer is not found for given code. so need to
                         * search data on name
                         */

                        String customerID = "";
                        String accountID = "";
                        Customer customer = null;
                        if (StringUtil.isNullOrEmpty(customerID) && isCustomer) {
                            if (columnConfig.containsKey("acccode")) {
                                String customerCode = recarr[(Integer) columnConfig.get("acccode")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(customerCode)) {
                                    KwlReturnObject retObj = accCustomerDAOObj.getCustomerByCode(customerCode, companyID);
                                    if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                        customer = (Customer) retObj.getEntityList().get(0);
                                    }
                                    if (customer != null) {
                                        accountID = customer.getAccount().getID();
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
                        /*
                         * 3. Vendor Name if vendorID is empty it menas vendor
                         * is not found for given code. so need to serch data on
                         * name
                         */
                        String vendorId = "";
                        String vendorName = "";
                        if (StringUtil.isNullOrEmpty(vendorId) && isVendor) {
                            if (columnConfig.containsKey("acccode")) {
                                vendorName = recarr[(Integer) columnConfig.get("acccode")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(vendorName)) {
                                    Vendor vendor = null;
                                    KwlReturnObject retObj = accVendorDAOObj.getVendorByCode(vendorName, companyID);
                                    if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                        vendor = (Vendor) retObj.getEntityList().get(0);
                                    }
                                    if (vendor != null) {
                                        accountID = vendor.getAccount().getID();
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
                         * 4. if currencyname is empty it menas currency is not
                         * found for given code. so need to serch data on name
                         */
                        String currencyStr = "";
                        KWLCurrency currency = null;
                        if (isCurrencyCode ? columnConfig.containsKey("currencyCode") : columnConfig.containsKey("currencyname")) {
                            currencyStr = isCurrencyCode ? recarr[(Integer) columnConfig.get("currencyCode")].replaceAll("\"", "").trim() : recarr[(Integer) columnConfig.get("currencyname")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(currencyStr)) {
                                currencyID = accSalesOrderServiceobj.getCurrencyId(currencyStr, currencyMap);
                                result = accReceiptDAOobj.getCurrency(currencyStr);
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
                        }

                        /*
                         * 6. if creditType is empty it menas credit Type is not
                         * found for given code. so need to serch data on name
                         */


                        String creditTypeStr = "";
                        if (isGL) {
                            if (columnConfig.containsKey("creditType")) {
                                creditTypeStr = recarr[(Integer) columnConfig.get("creditType")].replaceAll("\"", "").trim();
                                if (StringUtil.isNullOrEmpty(creditTypeStr)) {
                                    failureMsg += "Type is not available. ";
                                }
                            } else {
                                failureMsg += "Type column is not found. ";
                            }
                        }

                        /*
                         * 7. if exchangeratefortransaction is empty it menas
                         * exchange rate for transaction is not found for given
                         * code. so need to serch data on name
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
                         * 8. if exrate is empty it menas exchange rate for
                         * document To Pament exchange rate is not found for
                         * given code. so need to search data on name
                         */


                        double documentToPamentExchangeRate = 0;
                        String documentToPamentExchangeRateStr = "";
                        if (columnConfig.containsKey("exrate")) {
                            documentToPamentExchangeRateStr = recarr[(Integer) columnConfig.get("exrate")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(documentToPamentExchangeRateStr)) {
                                failureMsg += "document To Payment  exchange rate for transaction is not available. ";
                            } else {
                                try {
                                    documentToPamentExchangeRate = Double.parseDouble(documentToPamentExchangeRateStr);
                                } catch (Exception ex) {
                                    failureMsg += "Incorrect numeric value for  document To Pament exchange rate for transaction. ";
                                }
                            }
                        } else {
                            failureMsg += "document To Payment exchange rate for transaction column is not found. ";
                        }


                        int type = 0;
                        String documenyTypeStr = "";
                        String documenyType = "";
                        if (columnConfig.containsKey("type")) {
                            documenyType = recarr[(Integer) columnConfig.get("type")].replaceAll("\"", "").trim();
                            if (documenyType.equalsIgnoreCase("Advanced/Deposit") || documenyType.equalsIgnoreCase("Advanced / Deposit") || documenyType.equalsIgnoreCase("Advance/Deposit") || documenyType.equalsIgnoreCase("Advance / Deposit") && isCustomer) {
                                documenyTypeStr = "1";
                            } else if (documenyType.equalsIgnoreCase("Refund/Deposit") && isVendor) {
                                documenyTypeStr = "1";
                            } else if (documenyType.equalsIgnoreCase("Invoice")) {
                                documenyTypeStr = "2";
                            } else if (documenyType.equalsIgnoreCase("Debit Note")) {
                                documenyTypeStr = "3";
                            } else if (documenyType.equalsIgnoreCase("General Ledger Code")) {
                                documenyTypeStr = "4";
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
                         * 11. if enteramount is empty for Enter Amount is not
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
                                    if ((enterAmount <= 0 && !isGL && !documenyTypeStr.equals(Constants.GLIMPORT)) || enterAmount == 0) {
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
                         * 12. if paymentmethod is empty for Payment Method is
                         * not found for given code. so need to search data on
                         * name
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

                        if (documenyTypeStr.equals("4")|| (documenyTypeStr.equals("" + Constants.AdvancePayment) && countryCode.equalsIgnoreCase("" + Constants.malaysian_country_id))) {
                            if (columnConfig.containsKey("tax")) {
                                String taxCode = recarr[(Integer) columnConfig.get("tax")].replaceAll("\"", "").trim();
                                if (!StringUtil.isNullOrEmpty(taxCode)) {
//                                    rowtax = accSalesOrderServiceobj.getGSTByCode(taxCode, companyID);
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
                         * if bankInterestCmb is empty it means bank Interest
                         * Account is not found for given code. so need to
                         * search data on name
                         */

                        Account bankInterestAccount = null;
                        String bankInterestCmbStr = "";
                        double bankInterestAmount = 0;
                        String bankInterestAmountStr = "";                        
                        if (columnConfig.containsKey("bankInterestCmb")) {
                            bankInterestCmbStr = recarr[(Integer) columnConfig.get("bankInterestCmb")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(bankInterestCmbStr)) {
                                result = accReceiptDAOobj.getAccountNameCount(bankInterestCmbStr, companyID);
                                nocount = result.getRecordTotalCount();
                                List accountNameList = result.getEntityList();
                                if (nocount > 0) {
                                    bankInterestAccount = (Account) accountNameList.get(0);
                                } else {
                                    isAlreadyExist = true;
                                    throw new AccountingException("Bank Interest Account '" + bankInterestCmbStr + "' is not availble for Account.");
                                }
                                
                                /*
                                 * if bankInterest is empty it means bank Interest
                                 * Amount is not found for given code. so need to search
                                 * data on name
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
                         * if bankChargesCmb is empty it means bank charge
                         * Account is not found for given code. so need to
                         * search data on name
                         */
                        Account bankChargeAccount = null;
                        String bankChargeCmbStr = "";
                        double bankChargeAmount = 0;
                        String chequeNumber = "";
                        String bankChargeAmountStr = "";
                        if (columnConfig.containsKey("bankChargesCmb")) {
                            bankChargeCmbStr = recarr[(Integer) columnConfig.get("bankChargesCmb")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(bankChargeCmbStr)) {
                                result = accReceiptDAOobj.getAccountNameCount(bankChargeCmbStr, companyID);
                                nocount = result.getRecordTotalCount();
                                List list1 = result.getEntityList();
                                if (nocount > 0) {
                                    bankChargeAccount = (Account) list1.get(0);
                                } else {
                                    isAlreadyExist = true;
                                    throw new AccountingException("Account Name '" + bankChargeCmbStr + "' is not Availble for Account.");
                                }
                                /*
                                 * if bankChargesCmb is empty it means bank charge
                                 * Amount is not found for given code. so need to search
                                 * data on name
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
                         * To check Cheque Number contains special characters or not.
                        */ 
                        if (columnConfig.containsKey("chequenumber")) {
                            chequeNumber = recarr[columnConfig.get("chequenumber")];
                            failureMsg += importHandler.isChequeNumberValidForImport(chequeNumber,false);
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
                                                if (transactionAmount <= 0) {
                                                    failureMsg += "Transaction Amount should be greater than zero. ";
                                                }
                                            } catch (Exception ex) {
                                                failureMsg += "Incorrect numeric value for Transaction Amount. ";
                                            }
                                        }
                                    } else {
                                        failureMsg += "Transaction Amount column is not found. ";
                                    }
                                    if (columnConfig.containsKey("amountDueOriginal")) {
                                        originalAmountDueStr = recarr[(Integer) columnConfig.get("amountDueOriginal")].replaceAll("\"", "").trim();
                                        if (StringUtil.isNullOrEmpty(originalAmountDueStr)) {
                                            failureMsg += " Transaction Amount Due is not available. ";
                                        } else {
                                            try {
                                                originalAmountDue = authHandler.roundQuantity(Double.parseDouble(originalAmountDueStr), companyID);
                                                if (originalAmountDue <= 0) {
                                                    failureMsg += "Transaction Amount Due should be greater than zero. ";
                                                }
                                            } catch (Exception ex) {
                                                failureMsg += "Incorrect numeric value for Transaction Amount Due. ";
                                            }
                                        }
                                    } else {
                                        failureMsg += "Transaction Amount Due column is not found. ";
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
                                }
                            } else if (!documenyTypeStr.equalsIgnoreCase(Constants.ADVANCE_REFUND)) {
                                failureMsg += "The value in document number column cannot be empty. ";
                            }
                        } else {
                            failureMsg += "Document number column is not found. ";
                        }
                        
                        double totalAmount = 0;
                        
                        result = accReceiptDAOobj.getReceiptFromBillNo(entryNumber, companyID);
                        nocount = result.getRecordTotalCount();
                        if (nocount > 0) {
                            isAlreadyExist = true;
                            failureMsg += "Receipt number '" + entryNumber + "' already exists. ";
                        }
                        result = accReceiptDAOobj.getPaymentMethodCount(paymentMethodStr, companyID);
                        nocount = result.getRecordTotalCount();
                        List paymentMethodList = result.getEntityList();

                        if (nocount > 0) {
                            paymentMethod = (PaymentMethod) paymentMethodList.get(0);
                        } else {
                            isAlreadyExist = true;
                            failureMsg += "Payment Method is not Availble. ";
                        }
                        String chequeNumberStr = "";
                        Date chequeDate = null;
                        Date clearanceDate = null;
                        String bankNameStr = "";
                        String bankNameMasterItemID = "";
                        String chequeDes = "";
                        String paymentStatus = "";

                        if (paymentMethod != null && paymentMethod.getDetailType() == 2) {

                            /*
                             * if chequenumber is empty it means
                             * chequenumber is not found for given code. so
                             * need to search data on name
                             */
                            if (columnConfig.containsKey("chequenumber")) {
                                chequeNumberStr = recarr[(Integer) columnConfig.get("chequenumber")].replaceAll("\"", "").trim();
                            } else {
                                failureMsg += "Cheque Number column is not found. ";
                            }

                            /*
                             * if chequedate is empty it means chequedate is
                             * not found for given code. so need to search
                             * data on name
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
                             * if chequedate is empty it means chequedate is
                             * not found for given code. so need to search
                             * data on name
                             */
                            if (columnConfig.containsKey("bankname")) {
                                bankNameStr = recarr[(Integer) columnConfig.get("bankname")].replaceAll("\"", "").trim();

                                if (!StringUtil.isNullOrEmpty(bankNameStr)) {
                                    KwlReturnObject returnObject = importDao.getBankNameMasterItemName(companyID, bankNameStr);
                                    if (returnObject.getEntityList().isEmpty()) {
                                        failureMsg += "Incorrect Bank Name type value for Bank Name. Please add new Bank Name as \"" + bankNameStr + "\" with other details.";
                                    } else {
                                        MasterItem masterItem = (MasterItem) returnObject.getEntityList().get(0);
                                        bankNameMasterItemID = masterItem.getID();
                                    }
                                } else {
                                    failureMsg += "Bank Name is not available. ";
                                }
                            } else {
                                failureMsg += "Bank Name column is not found. ";
                            }

                            /*
                             * if paymentstatus is empty it means Payment
                             * Status is not found for given code. so need
                             * to search data on name
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
                                                if(clearanceDate.before(chequeDate)){
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
                         * To check whether received from column  is  present or not
                         */
                        if (columnConfig.containsKey("paidToCmbValue")) {
                            receivedFrom = recarr[(Integer) columnConfig.get("paidToCmbValue")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(receivedFrom)) {
                                String conditionColName = "value";
                                KwlReturnObject masterReturnObj = accMasterItemsDAOObj.getMasterItemByNameorID(companyID, receivedFrom, Constants.MASTERGROUP_RECEIVEDFROM, Constants.Acc_id, conditionColName);
                                List list = masterReturnObj.getEntityList();
                                if (!list.isEmpty()) {
                                    receivedFromId = list.get(0).toString();
                                } else {
                                    failureMsg += "Received from entry not found in master list. ";
                                }
                            }
                        }

                        // To create custom field array
                        JSONArray customJArr = new JSONArray();
                            try {
                                customJArr = accSalesOrderServiceobj.createGlobalCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.Acc_Receive_Payment_ModuleId);
                            } catch (Exception ex) {
                                failureMsg += ex.getMessage() != null ? ex.getMessage() : " ";
                            }

                        exceptionMSg = "";
                        try {
                            if (customJArr.length() > 0) {
                                exceptionMSg = accSalesOrderServiceobj.isValidCustomFieldData(customJArr);
                                if (!StringUtil.isNullOrEmpty(exceptionMSg)) {
                                    failureMsg += exceptionMSg;
                                }
                            }

                        } catch (Exception ex) {
                            failureMsg += "Invalid data entered in custom field.Please check date format,numeric value etc.";
                        }

                            // For adding due date
                        // For getting exchange rate
                        exchangeRateForTransaction = accSalesOrderServiceobj.getExchangeRateForTransaction(requestJobj, billDate, currencyID);
                        if (currency != null && paymentMethod != null && paymentMethod.getAccount() != null && paymentMethod.getAccount().getCurrency() != null) {
                            if (paymentMethod.getAccount().getCurrency().getCurrencyCode() == currency.getCurrencyCode()) {
                                documentToPamentExchangeRate = 1;
                            } else if ((paymentMethod.getAccount().getCurrency().getCurrencyID() == currencyID) && currency.getCurrencyID() != currencyID) {
                                documentToPamentExchangeRate = (1 / exchangeRateForTransaction);
                            }
                        }
                        
                        if (!prevRecNo.equalsIgnoreCase(entryNumber)) {
                            prevRecNo = entryNumber;
                            totalAmount = 0;
                            StringBuilder ErrorMsgForAdvAndGL = new StringBuilder();
                            if (glCheckFlag) {                                  //if There is only GL entries in payment then glCheckFlag is false so we reject the record and add error message in log file
                                if (rows.length() > 0 && !isRecordFailed) {
                                    paramJobj.put("Details", rows.toString());
                                        KwlReturnObject prevPaymentMethodObj = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod", ""));
                                        PaymentMethod prevPaymentMethod = (PaymentMethod) prevPaymentMethodObj.getEntityList().get(0);
                                    if (prevPaymentMethod != null && prevPaymentMethod.getDetailType() == 2) {
                                        paramJobj.put("paydetail", paydetailObj.toString());
                                    }
                                    paramJobj.put("amount", totalAmountPay);
                                    HashMap resultMap = saveCustomerReceipt(paramJobj);
                                    glCheckFlag = false;                                    
                                    if (!resultMap.isEmpty()) {
                                        if (resultMap.containsKey("jobj")) {
                                            JSONObject jobj = (JSONObject) resultMap.get("jobj");
                                            if (!jobj.optBoolean("success")) {
                                                failureMsgFromSave += jobj.getString("msg");
                                                ErrorMsgForAdvAndGL.append(jobj.getString("msg"));
                                            }
                                        }
                                    }
                                }                                           
                                totalAmountPay = 0;
                            } else if (rows.length() > 0) {                     //For first record glCheckFlag is always false but rows length is 0 so checking it because error message should not be appended for first record
                                failureMsgFromSave += messageSource.getMessage("acc.rp.noAdvInvNoteEntry", null, Locale.forLanguageTag(requestJobj.getString(Constants.language)));
                                ErrorMsgForAdvAndGL.append(" ");
                                ErrorMsgForAdvAndGL.append(messageSource.getMessage("acc.rp.noAdvInvNoteEntry", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))));
                            }
                            if (isAdvance > 1) {                    //is to check whether same receipt contains more than one advance line level item
                                failureMsgFromSave += " Receive Payment of Advance type is already entered. You can not enter twice.";
                                ErrorMsgForAdvAndGL.append(" Receive Payment of Advance type is already entered. You can not enter twice.");
                                isAdvance = 0;
                            }
                            if (!StringUtil.isNullOrEmpty(failureMsgFromSave)) {
                                failed += recList.size();
                                int msgCnt = 0;
                                for (String[] rec : recList) {
                                    failedRecords.append("\n").append(accSalesOrderServiceobj.createCSVrecord(rec)).append("\"").append((failureMsgsList.get(msgCnt).replaceAll("\"", "") + ErrorMsgForAdvAndGL.toString()).trim()).append("\"");
                                    msgCnt++;
                                }
                                failureMsgFromSave = "";
                                isRecordFailed = false;
                                ErrorMsgForAdvAndGL = new StringBuilder();
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
                                sequenceNumberDataMap.put("moduleID", String.valueOf(Constants.Acc_Receive_Payment_ModuleId));
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
                            paramJobj.put(Constants.billid, "");
                            paramJobj.put("receipttype", "1");
                            paramJobj.put("creationdate", billDate!=null?sdf.format(billDate):"");
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
                            paramJobj.put("paidToCmb", receivedFromId);
                            paramJobj.put(Constants.customfield, customJArr.toString());
                            paramJobj.put("PaymentCurrencyToPaymentMethodCurrencyRate", documentToPamentExchangeRate);
                            paramJobj.put("paymentCurrencyToPaymentMethodCurrencyExchangeRate", documentToPamentExchangeRate);
                            paramJobj.put("accountIdComPreAdjRec", "");
                            paramJobj.put(Constants.sequenceformat, sequenceFormatID);
                            paramJobj.put("autogenerated", autogenerated);
                            paramJobj.put("isFromOtherSource", isFromOtherSource);
                            paramJobj.put("paymentmethodaccname", paymentMethodStr);
                            paramJobj.put("pmtmethod", paymentMethod!=null?paymentMethod.getID():"");

                            if (paymentMethod != null && paymentMethod.getDetailType() == 2) {
                                paydetailObj.put("chequenumber", chequeNumberStr);
                                paydetailObj.put("paymentstatus", paymentStatus);
                                paydetailObj.put("paymentthroughid", bankNameMasterItemID);
                                paydetailObj.put("clearancedate", clearanceDate != null ? sdf.format(clearanceDate) : "");
                                paydetailObj.put("postdate", chequeDate != null ? sdf.format(chequeDate) : "");
                                paydetailObj.put("description", chequeDes);
                                paydetailObj.put("paymentthrough", bankNameStr);
                            }

                            Map<String, Object> requestParams = new HashMap<>();
                            requestParams.put(Constants.companyKey, companyID);
                            CompanyPreferencesCMN.checkLockPeriod(accCompanyPreferencesObj, requestParams, billDate, false);
                        } // end global details

                        if (prevRecNo.equalsIgnoreCase(entryNumber) && !globalFieldMismatchFlag) {
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
                                if (!(paramJobj.optDouble("PaymentCurrencyToPaymentMethodCurrencyRate", 0.0) == documentToPamentExchangeRate))  {
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
                                if (!paramJobj.optString("paidToCmb", "").equals(receivedFromId)) {  
                                    mismatchColumns += " Received From,";
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
                        // Add Custom fields details of line items
                        JSONArray lineCustomJArr = accSalesOrderServiceobj.createLineLevelCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, Constants.Acc_Receive_Payment_ModuleId);
                        
                        JSONObject detailData = new JSONObject();
                        if (!isVendor) {
                            if (documenyTypeStr.equals("4")) {
                                result = accReceiptDAOobj.getAccountNameCount(documenyNoStr, companyID);
                                nocount = result.getRecordTotalCount();
                                List glAccountList = result.getEntityList();
                                if (nocount > 0) {
                                    account = (Account) glAccountList.get(0);
                                    if (!StringUtil.isNullOrEmpty(account.getUsedIn()) && !account.isWantToPostJe()) {
                                        throw new AccountingException("Account Name'" + documenyNoStr + "' is not Availble for Account as it is used in " + account.getUsedIn());
                                    } else if(!account.isActivate()){
                                        throw new AccountingException("Account Name '" + documenyNoStr + "' is not Available for Account as it is deactivated.");
                                    }
                                } else {
                                    isAlreadyExist = true;
                                    throw new AccountingException("Account Name '" + documenyNoStr + "' is not Availble for Account.");
                                }
                            }
                        }
                        String paymentId = "";
                        String paymentcurrencyCode = "";
                        double refundPaymentAmountDue=0;
                        Date documentNoDate = null;
                        if (isVendor) {
                            if (documenyTypeStr.equals("1")) {
                                if (!StringUtil.isNullOrEmpty(documenyNoStr)) {
                                    result = accReceiptDAOobj.getRefundNameCount(documenyNoStr, companyID, vendorId);
                                    List<Object[]> list1 = result.getEntityList();
                                    if (list1.size() > 0) {
                                        for (Object obj[] : list1) {
                                            paymentId = obj[0].toString();
                                            paymentcurrencyCode = obj[1].toString();
                                            refundPaymentAmountDue = Double.parseDouble(obj[3].toString());
                                            documentNoDate = (Date) obj[4];
                                        }
                                        if (currency!=null && paymentcurrencyCode.equalsIgnoreCase(currency.getCurrencyCode())) {
                                            exchangeRate = 1;
                                        }else{
                                            refundPaymentAmountDue=authHandler.round(refundPaymentAmountDue*exchangeRate,companyID);
                                        }
                                    }
                                    if (list1.size() < 0) {
                                        isAlreadyExist = true;
                                        throw new AccountingException("Payment No '" + documenyNoStr + "' is not Availble for Refund.");
                                    }
                                    if (enterAmount > refundPaymentAmountDue) {
                                        throw new AccountingException("Enter Amount Should not be greater than amount due for receipt No. '" + documenyNoStr + "'.");
                                    }
                                }
                            }
                        }
                        SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getDateOnlyFormat();
                        String currencysymboltransaction = "";
                        String invoicecreationdate = null;
                        String paymentAccountCur = paymentMethod !=null ? paymentMethod.getAccount() != null ? paymentMethod.getAccount().getCurrency().getCurrencyID() :"" :"";
                        if (isCustomer) {
                            if (documenyTypeStr.equals("2")) {
                                result = accInvoiceDAOObj.getInvoiceNoCount(documenyNoStr, companyID, customerID);
                                nocount = result.getRecordTotalCount();
                                List invoiceList = result.getEntityList();
                                if (nocount > 0) {
                                    invoice = (Invoice) invoiceList.get(0);
                                    /*
                                     * 1) If Link document and payment currency is same then exchangeRate=1
                                     * 2) If Link document and payment currency is different then exchangeRate is take from user
                                     * 3) If Link Document is GL and addvance then it exchangeRate=1
                                     */
                                    if (invoice.isIsOpeningBalenceInvoice()) {
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
                                    originalAmountDue = amountDue;
                                    if (invoice.isIsOpeningBalenceInvoice()) {
                                    invoicecreationdate = formatter.format(invoice.getCreationDate());
                                    } else {
                                        invoicecreationdate = formatter.format(invoice.getJournalEntry().getEntryDate());
                                    }
                                    detailData.put("invoicecreationdate",invoicecreationdate);
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
                                    String customerName = customer != null ? "for " + customer.getName() + " customer or invoice is not present in system " : "";
                                    throw new AccountingException("Invoice number '" + documenyNoStr + "' is not Availble "+ customerName);
                                }

                            }
                        }                        
                        if (documenyTypeStr.equals("3") && !isGL) {
                            String debitNoteCreationDate = null;
                            if (isVendor) {
                                result = accInvoiceDAOObj.getVendorDebitNoCount(documenyNoStr, companyID, vendorId);
                            } else if (isCustomer) {
                                result = accInvoiceDAOObj.getCustomerDebitNoCount(documenyNoStr, companyID, customerID);
                            }
                            nocount = result.getRecordTotalCount();
                            List list1 = result.getEntityList();
                            if (nocount > 0) {
                                debitNote = (DebitNote) list1.get(0);
                                if (debitNote.isIsOpeningBalenceDN()) {
                                    if (currency != null && debitNote.getCurrency().getCurrencyCode().equalsIgnoreCase(currency.getCurrencyCode())) {
                                    exchangeRate = 1;
                                        amountDue = debitNote.getOpeningBalanceAmountDue();
                                    } else {
                                        amountDue = debitNote.getOpeningBalanceBaseAmountDue();
                                }
                                } else if (currency != null && debitNote.getCurrency().getCurrencyCode().equalsIgnoreCase(currency.getCurrencyCode())) {
                                    exchangeRate = 1;
                                    amountDue = debitNote.getDnamountdue();
                                } else {
                                    amountDue = debitNote.getDnamountdue() * exchangeRate;
                                }
                                originalAmountDue = amountDue;
                                currencysymboltransaction = debitNote.getCurrency().getSymbol();
                                if (debitNote.isIsOpeningBalenceDN()) {
                                    debitNoteCreationDate = formatter.format(debitNote.getCreationDate());
                                } else {
                                    debitNoteCreationDate = formatter.format(debitNote.getJournalEntry().getEntryDate());
                                }
                                detailData.put("date", debitNoteCreationDate);
                                documentNoDate = debitNote.getCreationDate();
                                String debitNoteCur = debitNote.getCurrency().getCurrencyID();
                                if (!currencyID.equals(paymentAccountCur)) {
                                    if (!currencyID.equals(debitNoteCur)) {
                                        throw new AccountingException(messageSource.getMessage("acc.import.makereceivepayment.differentCurrencyErrorMessage", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))));
                                    }
                                }
                            } else {
                                isAlreadyExist = true;
                                throw new AccountingException("Debit Note number '" + documenyNoStr + "' is not Availble. ");
                            }
                        }

                        if ((documenyTypeStr.equals(Constants.CREDITNOTEIMPORT) || documenyTypeStr.equals(Constants.INVOICEIMPORT)) && enterAmount > amountDue) {
                            throw new AccountingException("Enter amount should not be greater than amount due for document no '"+documenyNoStr+"'.");
                        } else if (documenyTypeStr.equalsIgnoreCase("Refund/Deposit") && !StringUtil.isNullOrEmpty(documenyNoStr) && enterAmount > amountDue) {
                            throw new AccountingException("Enter amount should not be greater than amount due for document no '"+documenyNoStr+"'.");
                        }
                        
                        if(documentNoDate!=null && billDate.before(documentNoDate)){
                            throw new AccountingException("Receipt creation date cannot be less than selected document(s) creation date.");
                        }
                        
                        if (isGL && creditTypeStr.equalsIgnoreCase("debit")) {
                            totalAmountPay = totalAmountPay - enterAmount;
                        } else {
                            totalAmountPay += enterAmount;
                        }
                        
                        if (documenyTypeStr.equals("4")) {
                            totalAmountPay = totalAmountPay + (!creditTypeStr.equalsIgnoreCase("debit") ? taxAmount : -taxAmount);
                            exchangeRate = 1;
                        }
                        detailData.put("type", documenyTypeStr);
                        if (!StringUtil.equal(documenyTypeStr, "4") || isGL) {
                            glCheckFlag = true;
                        }
                        if ((isGL || documenyTypeStr.equals(Constants.GLIMPORT)) && creditTypeStr.equalsIgnoreCase("debit")) {
                            detailData.put("debit", true);
                        } else {
                            detailData.put("debit", false);
                        }
                        detailData.put("currencysymbol",currency!=null?currency.getSymbol():"");
                        detailData.put("currencyidtransaction", currencyID);
                        detailData.put("currencynametransaction", "");
                        detailData.put("currencysymboltransaction", currencysymboltransaction);
                        detailData.put("currencyname", "");
                        detailData.put(Constants.currencyKey, currencyID);
                        detailData.put("payment", "");
                        detailData.put("documentno", documenyNoStr);
                        if (documenyTypeStr.equals("4")) {
                            detailData.put("documentid", account!=null?account.getID():"");
                        }
                        if (documenyTypeStr.equals("2")) {
                            detailData.put("documentid", invoice!=null?invoice.getID():"");
                        }
                        if (documenyTypeStr.equals("3")) {
                            detailData.put("documentid", debitNote!=null?debitNote.getID():"");
                        }
                        if (documenyTypeStr.equals("1") && isVendor) {
                            detailData.put("documentid", paymentId);
                        }
                        if (documenyTypeStr.equals("1") && !isVendor) {
                            if (previousEntryNo.equals(entryNumber) || StringUtil.isNullOrEmpty(previousEntryNo)) {
                                isAdvance++;
                                isRecordFailed = isAdvance > 1 || isRecordFailed ? true : false;
                            }
                            previousEntryNo = entryNumber;
                            exchangeRate = 1;
                            detailData.put("documentid", "");
                        }
                        
                        if (documenyTypeStr.equals("2") || documenyTypeStr.equals("3")) {
                            detailData.put("prtaxid", "");
                            detailData.put("appliedGst", "");
                            detailData.put("taxamount", "");
                            detailData.put("amountDueOriginal", originalAmountDue);
                            detailData.put("amountDueOriginalSaved", originalAmountDue);
                            detailData.put("amountdue", amountDue);
                            detailData.put("exchangeratefortransaction", exchangeRate);
                            detailData.put("transactionAmount", transactionAmount);
                        } else {
                            if (documenyTypeStr.equals("4")) { 
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

                        rows.put(detailData);
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
            if (!isAlreadyExist && !isRecordFailed) {
                try {
                    if (glCheckFlag) {
                        paramJobj.put("Details", rows.toString());
                        if (paymentMethod != null && paymentMethod.getDetailType() == 2) {
                            paramJobj.put("paydetail", paydetailObj.toString());
                        }
                        paramJobj.put("amount", totalAmountPay);
                        HashMap resutlSaveMap = saveCustomerReceipt(paramJobj);
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
                        throw new AccountingException(messageSource.getMessage("acc.rp.noAdvInvNoteEntry", null, Locale.forLanguageTag(requestJobj.getString(Constants.language))));
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
                failureMsgFromSave += " Receive Payment of Advance type is already entered. You can not enter twice.";
                errorMsgForGLandAdvance.append(" Receive Payment of Advance type is already entered. You can not enter twice.");
                isAdvance = 0;
            }
            if (!StringUtil.isNullOrEmpty(failureMsgFromSave)) {
                int msgCnt=0;
                failed += recList.size();
                for (String[] rec : recList) {
                    failedRecords.append("\n").append(accSalesOrderServiceobj.createCSVrecord(rec)).append("\"").append((failureMsgsList.get(msgCnt).replaceAll("\"", "")+errorMsgForGLandAdvance.toString()).trim()).append("\"");
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

            Logger.getLogger(AccReceivePaymentModuleService.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            csvReader.close();

            // For saving import log
            saveImportLog(requestJobj, msg, total, failed, Constants.Acc_Receive_Payment_ModuleId);

            try {
                returnObj.put(Constants.RES_success, issuccess);
                returnObj.put(Constants.RES_msg, msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", ImportLog.getActualFileName(fileName));
                returnObj.put("Module", Constants.Acc_Delivery_Order_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(AccReceivePaymentModuleService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }

    public void saveImportLog(JSONObject requestJobj, String msg, int total, int failed, int moduleID) {
        DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
        ldef.setName("import_Tx");
        ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus lstatus = txnManager.getTransaction(ldef);

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
            txnManager.commit(lstatus);
        } catch (JSONException | ServiceException | DataInvalidateException | TransactionException ex) {
            txnManager.rollback(lstatus);
            Logger.getLogger(AccReceivePaymentModuleService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Please Note accounting exception should only be thrown in case of if JE posting date dose not belongs to lockin period.As further code is written only to handle AccountingException in case of lockin period.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, AccountingException.class})
     public List approvePendingReceivePayment(Map<String, Object> requestParams) throws ServiceException,AccountingException {
        List returnList = new ArrayList();
        try {

            String jeID = "";
            String jeIdForBankCharges = "";
            String jeIdForbankInterst = "";
            String msg = "";
            String companyid = (String) requestParams.get(Constants.companyKey);
            String userid = (String) requestParams.get(Constants.useridKey);
            String billid = (String) requestParams.get(Constants.billid);
            String remark = (String) requestParams.get("remark");
            String baseUrl = (String) requestParams.get("baseUrl");
            String userFullName = (String) requestParams.get("userName");
            double amount = (!StringUtil.isNullObject(requestParams.get("amount")) && !StringUtil.isNullOrEmpty(requestParams.get("amount").toString())) ? authHandler.round(Double.parseDouble(requestParams.get("amount").toString()), companyid) : 0;
            boolean isEditToApprove = (!StringUtil.isNullObject(requestParams.get("isEditToApprove")) && !StringUtil.isNullOrEmpty(requestParams.get("isEditToApprove").toString())) ? Boolean.parseBoolean(requestParams.get("isEditToApprove").toString()) : false;
            KwlReturnObject CQObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), billid);
            Receipt receipt = (Receipt) CQObj.getEntityList().get(0);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) returnObject.getEntityList().get(0);
            HashMap<String, Object> invApproveMap = new HashMap<String, Object>();
            int level = receipt.getApprovestatuslevel();
            invApproveMap.put(Constants.companyKey, companyid);
            invApproveMap.put("level", level);
            invApproveMap.put("totalAmount", String.valueOf(amount));
            invApproveMap.put("currentUser", userid);
            invApproveMap.put("fromCreate", false);
            invApproveMap.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("id", companyid);
            Object exPrefObject = kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"columnPref"}, paramMap);
            JSONObject jObj = StringUtil.isNullObject(exPrefObject) ? new JSONObject() : new JSONObject(exPrefObject.toString());
            boolean isPostingDateCheck = false;
            if (!StringUtil.isNullObject(jObj) && jObj.has(Constants.IS_POSTING_DATE_CHECK) && jObj.get(Constants.IS_POSTING_DATE_CHECK) != null && jObj.optBoolean(Constants.IS_POSTING_DATE_CHECK, false)) {
                isPostingDateCheck = true;
            }
            String psotingDateStr = (String) requestParams.get("postingDate");
            DateFormat df = authHandler.getDateOnlyFormat();
            Date postingDate = null;
            if (!StringUtil.isNullOrEmpty(psotingDateStr)) {
                postingDate = df.parse(psotingDateStr);
            }
            int approvedLevel = 0;
            String JENumber = "";
            String JEMsg = "";
            List list = new ArrayList();
            list.add(receipt.getID());
            List approvedLevelList = approveReceivePayment(list, invApproveMap, true);
            approvedLevel = (Integer) approvedLevelList.get(0);
            jeID = receipt.getJournalEntry().getID();

            if (approvedLevel == Constants.APPROVED_STATUS_LEVEL) {//when final 
                if (StringUtil.isNullOrEmpty(receipt.getJournalEntry().getEntryNumber())) {
                    int isApproved = 0;
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                    JEFormatParams.put(Constants.companyKey, companyid);
                    JEFormatParams.put("isdefaultFormat", true);
                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
//                    String JENumBer = journalEntryModuleServiceobj.updateJEEntryNumberForNewJE(requestParams, receipt.getJournalEntry(), companyid, format.getID(), isApproved);
                    String JENumBer="";
                    KwlReturnObject returnObj = journalEntryModuleServiceobj.updateJEEntryNumberForNewJE(requestParams, receipt.getJournalEntry(), companyid, format.getID(), isApproved);
                    if (returnObj.isSuccessFlag() && returnObj.getRecordTotalCount() > 0) {
                        JENumBer = (String) returnObj.getEntityList().get(0);
                    } else if (!returnObj.isSuccessFlag()) {
                        throw new AccountingException((String) returnObj.getEntityList().get(0));
                    }
                } else {
                    JSONObject jeJobj = new JSONObject();
                    HashSet<JournalEntryDetail> details = new HashSet<JournalEntryDetail>();
                    jeJobj.put("jeid", jeID);
                    jeJobj.put("comapnyid", companyid);
                    jeJobj.put("pendingapproval", 0);
                    if (isPostingDateCheck && postingDate!=null) {
                        jeJobj.put("entrydate", postingDate);
                    }
                    accJournalEntryobj.updateJournalEntry(jeJobj, details);
                }
                JENumber = " with JE No. " + receipt.getJournalEntry().getEntryNumber();
                JEMsg = "<br/>" + "JE No : <b>" + receipt.getJournalEntry().getEntryNumber() + "</b>";
            }
            if (receipt.getJournalEntryForBankCharges() != null) {
                jeIdForBankCharges = receipt.getJournalEntryForBankCharges().getID();
                JSONObject jeJobj = new JSONObject();
                HashSet<JournalEntryDetail> details = new HashSet<JournalEntryDetail>();
                jeJobj.put("jeid", jeIdForBankCharges);
                jeJobj.put("comapnyid", companyid);
                jeJobj.put("pendingapproval", 0);
                accJournalEntryobj.updateJournalEntry(jeJobj, details);
            }
            if (receipt.getJournalEntryForBankInterest() != null) {
                //Update JE for Bank Interst
                jeIdForbankInterst = receipt.getJournalEntryForBankInterest().getID();
                JSONObject jeJobj = new JSONObject();
                HashSet<JournalEntryDetail> details = new HashSet<JournalEntryDetail>();
                jeJobj.put("jeid", jeIdForbankInterst);
                jeJobj.put("comapnyid", companyid);
                jeJobj.put("pendingapproval", 0);
                accJournalEntryobj.updateJournalEntry(jeJobj, details);
            }
            if (!isEditToApprove && approvedLevel == Constants.APPROVED_STATUS_LEVEL) {
                for (ReceiptDetail detail : receipt.getRows()) {
                    Invoice invoice = detail.getInvoice();
                    double returnAmountInInvoiceCurrecny = 0;
//                    double returnAmountInBaseAmountDue = 0;
//                    double discountAmt=detail.getDiscountAmount();
                    double discountAmtInInvoiceCurrency = authHandler.round(detail.getDiscountAmount() / detail.getExchangeRateForTransaction(), companyid);
                    returnAmountInInvoiceCurrecny = detail.getAmountInInvoiceCurrency()+discountAmtInInvoiceCurrency;
//                    double returnAmountInReceiveCurrency = detail.getAmount()+discountAmt;
//                    KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, returnAmountInReceiveCurrency, receipt.getCurrency().getCurrencyID(), receipt.getCreationDate(), receipt.getJournalEntry().getExternalCurrencyRate());
//                    returnAmountInBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                    KwlReturnObject invoiceResult = updateInvoiceAmountDueAndReturnResult(invoice, receipt, company, returnAmountInInvoiceCurrecny);
                    /*
                     * Update the amountduedate while approving the payment
                     */
                    if (invoiceResult != null && invoiceResult.getEntityList() != null && invoiceResult.getEntityList().size() > 0) {
                        invoice = (Invoice) invoiceResult.getEntityList().get(0);
                        if ((invoice.isIsOpeningBalenceInvoice() && invoice.getOpeningBalanceAmountDue() == 0) || (!invoice.isIsOpeningBalenceInvoice() && invoice.getInvoiceamountdue() == 0 )) {
                            try {
                                HashMap<String, Object> dataMap = new HashMap<String, Object>();
                                dataMap.put("amountduedate", receipt!=null ? receipt.getCreationDate() : null);
                                accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(invoice, dataMap);
                            } catch (Exception ex) {
                                System.out.println("" + ex.getMessage());
                            }
                        }
                    }
                }

                for (DebitNotePaymentDetails detail : receipt.getDebitNotePaymentDetails()) {
                    double returnAmount = detail.getAmountPaid();;              
                    KwlReturnObject dnjedresult = accReceiptDAOobj.updateDnAmount(detail.getDebitnote().getID(), returnAmount);
                        KwlReturnObject dnopeningamountdueresult = accReceiptDAOobj.updateDnOpeningAmountDue(detail.getDebitnote().getID(), returnAmount);
                        KwlReturnObject dnopeningbaseamountdueresult = accReceiptDAOobj.updateDnOpeningBaseAmountDue(detail.getDebitnote().getID(), returnAmount);
                    }

                for (ReceiptAdvanceDetail receiptAdvanceDetail : receipt.getReceiptAdvanceDetails()) {                          //setting due amount of advance payment made                    
                    if (receiptAdvanceDetail != null) {
                        String advanceDetailId = receiptAdvanceDetail.getAdvancedetailid();
                        List<Object[]> advPayDetails = getAdvancePaymentDetails(advanceDetailId);
                        if (!advPayDetails.isEmpty()) {
                            double exchangerate = receiptAdvanceDetail.getExchangeratefortransaction() == 0 ? 1 : receiptAdvanceDetail.getExchangeratefortransaction();
                            double negativeAmount = -authHandler.round((receiptAdvanceDetail.getAmount() / exchangerate),companyid);    //ERP-39732 total amount of MP/RP was getting deducted from advanced type linked MP/RP                        
                            updateAdvanceDetailAmountDueOnAmountReceived(advanceDetailId, negativeAmount);                              // e.g. one linked MP/RP advanced(100SGD)  and other MP/RP advanced(200SGD) is not linked then total_amount(300SGD) of transanction was deducting from linked MP/RP(SGD100) 
                        }
                    }
                }
            }
            if (approvedLevel != Constants.NoAuthorityToApprove && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
                String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                String creatormail = company.getCreator().getEmailID();
                String documentcreatoremail = (receipt != null && receipt.getCreatedby() != null) ? receipt.getCreatedby().getEmailID() : "";
                 String approvalpendingStatusmsg = "";
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                ArrayList<String> emailArray = new ArrayList<>();
                qdDataMap.put(Constants.companyKey, companyid);
                qdDataMap.put("level", level);
                qdDataMap.put(Constants.moduleid,Constants.Acc_Receive_Payment_ModuleId);
//                emailArray =commonFnControllerService.getUserApprovalEmail(qdDataMap);
                        emailArray.add(creatormail);
                if (!StringUtil.isNullOrEmpty(documentcreatoremail) && !creatormail.equalsIgnoreCase(documentcreatoremail)) {
                    emailArray.add(documentcreatoremail);
                }
                        String[] emails ={};
                        emails = emailArray.toArray(emails);
                if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                    String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                    emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
                }
                if (receipt.getApprovestatuslevel() < 11) {
                  qdDataMap.put("totalAmount", String.valueOf(amount));
//                qdDataMap.put("level", creditNote.getApprovestatuslevel()+1);
                approvalpendingStatusmsg=commonFnControllerService.getApprovalstatusmsg(qdDataMap);
                }
                Map<String, Object> mailParameters = new HashMap();
                mailParameters.put("Number", receipt.getReceiptNumber());
                mailParameters.put("userName", userFullName);
                mailParameters.put("emails", emails);
                mailParameters.put("sendorInfo", sendorInfo);
                mailParameters.put("moduleName", Constants.RECEIVE_PAYMENT);
                mailParameters.put("addresseeName", "All");
                mailParameters.put("companyid", company.getCompanyID());
                mailParameters.put("baseUrl", baseUrl);
                mailParameters.put("approvalstatuslevel", receipt.getApprovestatuslevel());
                mailParameters.put("approvalpendingStatusmsg", approvalpendingStatusmsg);
                if (emails.length > 0) {
                    accountingHandlerDAOobj.sendApprovedEmails(mailParameters);
                }
            }
            // Save Approval History
            if (approvedLevel != Constants.NoAuthorityToApprove) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("transtype", Constants.RECEIVE_PAYMENT_APPROVAL);
                hashMap.put("transid", receipt.getID());
                hashMap.put("approvallevel", receipt.getApprovestatuslevel());//  If approvedLevel = 11 then its final Approval
                hashMap.put("remark", remark);
                hashMap.put(Constants.useridKey, userid);
                hashMap.put(Constants.companyKey, companyid);
                accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                // Audit log entry
                String action = "Receive Payment ";
                String auditaction = AuditAction.RECEIVE_PAYMENT_APPROVED;
                auditTrailObj.insertAuditLog(auditaction, "User " + userFullName + " has Approved a " + action + receipt.getReceiptNumber() + JENumber + " at Level-" + receipt.getApprovestatuslevel(), requestParams, receipt.getID());
                msg = "Receive Payment has been approved successfully " + " by " + userFullName + " at Level " + receipt.getApprovestatuslevel() + "." + JEMsg;
            };
            returnList.add(msg);
        } catch (AccountingException ae) {
           throw new AccountingException(ae.getMessage(), ae);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("approvePendingReceive Payment:" + ex.getMessage(), ex);
        }
        return returnList;
    }
     
    @Override
    public List approveReceivePayment(List receipt, HashMap<String, Object> cnApproveMap, boolean isMailApplicable) throws ServiceException {
        List returnList = new ArrayList();
        try {
            boolean hasAuthority = false;
            String companyid = "";

            List mailParamList = new ArrayList();
            int returnStatus;

            if (cnApproveMap.containsKey(Constants.companyKey) && cnApproveMap.get(Constants.companyKey) != null) {
                companyid = cnApproveMap.get(Constants.companyKey).toString();
            }
            String currentUser = "";
            if (cnApproveMap.containsKey("currentUser") && cnApproveMap.get("currentUser") != null) {
                currentUser = cnApproveMap.get("currentUser").toString();
            }
            int level = 0;
            if (cnApproveMap.containsKey("level") && cnApproveMap.get("level") != null) {
                level = Integer.parseInt(cnApproveMap.get("level").toString());
            }
            String amount = "";
            if (cnApproveMap.containsKey("totalAmount") && cnApproveMap.get("totalAmount") != null) {
                amount = cnApproveMap.get("totalAmount").toString();
            }
            boolean fromCreate = false;
            if (cnApproveMap.containsKey("fromCreate") && cnApproveMap.get("fromCreate") != null) {
                fromCreate = Boolean.parseBoolean(cnApproveMap.get("fromCreate").toString());
            }
            int moduleid = 0;
            if (cnApproveMap.containsKey(Constants.moduleid) && cnApproveMap.get(Constants.moduleid) != null) {
                moduleid = Integer.parseInt(cnApproveMap.get(Constants.moduleid).toString());
            }

            if (!fromCreate) {
                String thisUser = currentUser;
                KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), thisUser);
                User user = (User) userclass.getEntityList().get(0);

                if (AccountingManager.isCompanyAdmin(user)) {
                    hasAuthority = true;
                } else {
                    hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(cnApproveMap);
                }
            } else {
                hasAuthority = true;
            }

            if (hasAuthority) {
                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");
                int approvalStatus = 11;
                String cnID = (String) receipt.get(0);
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                qdDataMap.put(Constants.companyKey, companyid);
                qdDataMap.put("level", level + 1);
                qdDataMap.put(Constants.moduleid, moduleid);
                KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
                Iterator itr = flowresult.getEntityList().iterator();
                String fromName = "User";
                while (itr.hasNext()) {
                    Object[] row = (Object[]) itr.next();
                    HashMap<String, Object> recMap = new HashMap();
                    String rule = "";
                    if (row[2] != null) {
                        rule = row[2].toString();
                    }
                    rule = rule.replaceAll("[$$]+", amount);
                    if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && Boolean.parseBoolean(engine.eval(rule).toString()))) {
                        boolean hasApprover = Boolean.parseBoolean(row[3].toString());
                        approvalStatus = level + 1;
                        recMap.put("ruleid", row[0].toString());
                        recMap.put("fromName", fromName);
                        recMap.put("hasApprover", hasApprover);
                        mailParamList.add(recMap);
                    }
                }
                accReceiptDAOobj.approvePendingReceivePayment(cnID, companyid, approvalStatus);
                returnStatus = approvalStatus;
            } else {
                returnStatus = Constants.NoAuthorityToApprove; //if not have approval permission then return one fix value like 999
            }
            returnList.add(returnStatus);
            returnList.add(mailParamList);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return returnList;
    }
    @Override
    public JSONObject getReceivePaymentApprovalPendingJsonData(JSONObject obj, Double totalAmount, int approvallevel, String companyid, String userid, String userName) throws ServiceException {
        try {
            double amountInBase = authHandler.round(totalAmount, companyid);
            String approvalStatus = "";
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            String multipleRuleids="";
            if (approvallevel < 0) {//will be negartive for rejected
                approvalStatus = "Rejected";
            } else if (approvallevel < 11) {//will be less than 11 for pending record 
                String ruleid = "", userRoleName = "";
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                qdDataMap.put(Constants.companyKey, companyid);
                qdDataMap.put("level", approvallevel);
                qdDataMap.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
                Iterator ruleitr = flowresult.getEntityList().iterator();
                while (ruleitr.hasNext()) {
                    Object[] rulerow = (Object[]) ruleitr.next();
                    ruleid = rulerow[0].toString();
                    int appliedUpon = Integer.parseInt(rulerow[5].toString());
                    String rule = "";
                    if (rulerow[2] != null) {
                        rule = rulerow[2].toString();
                    }
                    if (appliedUpon == Constants.Total_Amount) {
                        /*
                         Added to get condition of approval rule i.e set when creating approval rule 
                         */
                        rule = rule.replaceAll("[$$]+", String.valueOf(amountInBase));
                    }
                    /*
                     Added to check if record falls in total amount approval rule 
                     */
                    if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && appliedUpon == Constants.Total_Amount && Boolean.parseBoolean(engine.eval(rule).toString()))) {
                        multipleRuleids += ruleid + ",";
                    }
                }
                /*
                 Added to get multiple ruleid if record falls in multiple approval rule 
                 */
                String[] multipleRuleidsArray = multipleRuleids.split(",");
                for (int multiRule = 0; multiRule < multipleRuleidsArray.length; multiRule++) {
                    ruleid = multipleRuleidsArray[multiRule];
                    if (!StringUtil.isNullOrEmpty(ruleid)) {
                        qdDataMap.put("ruleid", ruleid);
                        KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(qdDataMap);
                        Iterator useritr = userResult.getEntityList().iterator();
                        while (useritr.hasNext()) {
                            Object[] userrow = (Object[]) useritr.next();
                            String approvalName = userrow[1].toString();
                            String roleName = "Company User";
                            /*
                             Addded so duplicate approve's can be eleminated 
                             */
                            if (userRoleName.contains(approvalName)) {
                                continue;
                            }
                            userRoleName += roleName + " " + approvalName + ",";
                        }
                    }
                }
                if (!StringUtil.isNullOrEmpty(userRoleName)) {
                    userRoleName = userRoleName.substring(0, userRoleName.length() - 1);
                }
                approvalStatus = "Pending Approval" + (StringUtil.isNullOrEmpty(userRoleName) ? "" : " by " + userRoleName) + " at Level - " + approvallevel;
            } else {
                approvalStatus = "Approved";
            }
            obj.put("approvalstatusinfo", approvalStatus);
            obj.put("approvalLevel", approvallevel);

            KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
            User user = (User) userclass.getEntityList().get(0);
            boolean hasApprovalAuthority = false;
            if (AccountingManager.isCompanyAdmin(user)) {
                hasApprovalAuthority = true;
            } else {
                HashMap<String, Object> cnApproveMap = new HashMap<String, Object>();
                cnApproveMap.put(Constants.companyKey, companyid);
                cnApproveMap.put("level", approvallevel);
                cnApproveMap.put("totalAmount", String.valueOf(amountInBase));
                cnApproveMap.put("currentUser", userid);
                cnApproveMap.put("fromCreate", false);
                cnApproveMap.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                hasApprovalAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(cnApproveMap);
            }
            obj.put("hasApprovalAuthority", hasApprovalAuthority);

            int nextApprovalLevel = 11;
//            ScriptEngineManager mgr = new ScriptEngineManager();
//            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put(Constants.companyKey, companyid);
            qdDataMap.put("level", approvallevel + 1);
            qdDataMap.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
            List<Object[]> approvalRuleItr = flowresult.getEntityList();
            if (approvalRuleItr!=null && approvalRuleItr.size()>0) {   
               for (Object[] rowObj : approvalRuleItr) {
                   String rule = "";
                   if (rowObj[2] != null) {
                       rule = rowObj[2].toString();
                   }
                   rule = rule.replaceAll("[$$]+", String.valueOf(amountInBase));
                   if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && Boolean.parseBoolean(engine.eval(rule).toString()))) {
                       nextApprovalLevel = approvallevel + 1;
                   }
               }
            }
            obj.put("isFinalLevelApproval", nextApprovalLevel == Constants.APPROVED_STATUS_LEVEL ? true : false);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getReceivePaymentJson : " + ex.getMessage(), ex);
        }
        return obj;
    }
    @Override
    public List getAdvancePaymentDetails(String receiptadvancedetailid) throws ServiceException {
        List advancePaymentDetailsList = new ArrayList();
        try {
            if (!StringUtil.isNullOrEmpty(receiptadvancedetailid)) {
                advancePaymentDetailsList = accReceiptDAOobj.getAdvancePaymentDetails(receiptadvancedetailid);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getAdvancePaymentDetails : " + ex.getMessage(), ex);
        }
        return advancePaymentDetailsList;
    }
    /*
    * Implemented this fuction to access it form services layer and update the Advance make payment to Vender's due amount 
    */
    @Override
    public void updateAdvanceDetailAmountDueOnAmountReceived(String advanceDetailId, double amountReceived) throws ServiceException {
        accountingHandlerDAOobj.updateAdvanceDetailAmountDueOnAmountReceived(advanceDetailId, amountReceived);
    } 
    
    @Override
    public boolean rejectPendingReceivePayment(Map<String, Object> requestParams, JSONArray jArr) throws ServiceException {
        boolean isRejected = false;
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String currentUser = (String) requestParams.get(Constants.useridKey);
            String userFullName = (String) requestParams.get("userName");
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), currentUser);
            User user = (User) userResult.getEntityList().get(0);

            String actionMsg = "rejected";
            int level = 0;
            double amount = (!StringUtil.isNullObject(requestParams.get("amount")) && StringUtil.isNullOrEmpty(requestParams.get("amount").toString())) ? 0 : authHandler.round(Double.parseDouble(requestParams.get("amount").toString()), Constants.AMOUNT_DIGIT_AFTER_DECIMAL);
            for (int i = 0; i < jArr.length(); i++) {
                boolean hasAuthorityToReject = false;
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.billid))) {
                    String invid = StringUtil.DecodeText(jobj.optString(Constants.billid));
                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(Receipt.class.getName(), invid);
                    Receipt receipt = (Receipt) cap.getEntityList().get(0);
                    HashMap<String, Object> invApproveMap = new HashMap<String, Object>();
                    level = receipt.getApprovestatuslevel();
                    invApproveMap.put(Constants.companyKey, companyid);
                    invApproveMap.put("level", level);
                    invApproveMap.put("totalAmount", String.valueOf(amount));
                    invApproveMap.put("currentUser", currentUser);
                    invApproveMap.put("fromCreate", false);
                    invApproveMap.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                    if (AccountingManager.isCompanyAdmin(user)) {
                        hasAuthorityToReject = true;
                    } else {
                        hasAuthorityToReject = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(invApproveMap);
                    }
                    if (hasAuthorityToReject) {
                        accReceiptDAOobj.rejectPendingReceivePayment(receipt.getID(), companyid);
                        isRejected = true;
                        // Maintain Approval History of Rejected Record
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("transtype", AuditAction.RECEIVE_PAYMENT_REJECTED);
                        hashMap.put("transid", receipt.getID());
                        hashMap.put("approvallevel", Math.abs(receipt.getApprovestatuslevel()));//  If approvedLevel = 11 then its final Approval
                        hashMap.put("remark", "");
                        hashMap.put(Constants.useridKey, currentUser);
                        hashMap.put(Constants.companyKey, companyid);
                        hashMap.put("isrejected", true);
                        accountingHandlerDAOobj.updateApprovalHistory(hashMap);
                        auditTrailObj.insertAuditLog(AuditAction.RECEIVE_PAYMENT_REJECTED, "User " + userFullName + " " + actionMsg + " Receive Payment " + receipt.getReceiptNumber(), requestParams, receipt.getID());
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        }
        return isRejected;
    }
    
    @Override
    public JSONObject checkInvoiceKnockedOffDuringReceivePaymentPending(Map<String, Object> requestParams) {
        JSONObject obj = new JSONObject();
        boolean issuccessInv = true;
        boolean issuccessCredit = true;
        boolean issuccessReceipt = true;
        String companyid = requestParams.get(Constants.companyKey).toString();

        try {
            int totalInvoicesLinked = 0;
            String billid = (String) requestParams.get(Constants.billid);//This is mandatory parameter
            KwlReturnObject CQObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), billid);
            Receipt receipt = (Receipt) CQObj.getEntityList().get(0);

            //For Innvoice
            /*
             * ERP-39732 changes made to make consistent method for checking knockoff invoices, CN/DN , advanced/refund when approving pending MP/RP 
             * Changes required in case of approving MP/RP adv to MP/RP refund , while checking for knockoff linked MP/RP converted amountdue is wrong for foreign currency
             * e.g. linked invoice in USD and transaction currency is SGD then converting to USD and comparing entered and due amount
             */ 
            for (ReceiptDetail detail : receipt.getRows()) {
                totalInvoicesLinked++;

                double invoiceAmtDue;
                double enteredAmount = detail.getAmount();
                double invKnockedOffAmountInInvCurr = authHandler.round((enteredAmount/detail.getExchangeRateForTransaction()), companyid);
                if (detail.getInvoice() != null && detail.getInvoice().isNormalInvoice()) {
                    invoiceAmtDue = detail.getInvoice() != null ? detail.getInvoice().getInvoiceamountdue() : 0;
                } else {
                    invoiceAmtDue = detail.getInvoice() != null ? detail.getInvoice().getOpeningBalanceAmountDue() : 0;
                }
                if (invoiceAmtDue >= invKnockedOffAmountInInvCurr) {
                    issuccessInv = true;
                } else {
                    issuccessInv = false;
                }
            }
            //For Debit Note
            /*
             * ERP-39732 changes made to make consistent method for checking knockoff invoices, CN/DN , advanced/refund when approving pending MP/RP 
             * Changes required in case of approving MP/RP adv to MP/RP refund , while checking for knockoff linked MP/RP converted amountdue is wrong for foreign currency
             * when linked CN/DN is in MYR and transaction currency is USD then converting to MYR and then comparing entered and due amount
             */ 
            for (DebitNotePaymentDetails detail : receipt.getDebitNotePaymentDetails()) {
                totalInvoicesLinked++;
                double debKnockedOffAmountInDNCurr = 0;
                debKnockedOffAmountInDNCurr = detail.getAmountPaid();   //converted paid amount in linked CN/DN currency
                double debitAmtDue = 0;
                if (detail.getDebitnote() != null && detail.getDebitnote().isNormalDN()) {                                      //For normal Debit Note
                    
                    debitAmtDue = authHandler.round(detail.getDebitnote() != null ? detail.getDebitnote().getDnamountdue() : 0, companyid);      //converting due amount in base currency
                } else {
                    debitAmtDue = detail.getDebitnote() != null ? detail.getDebitnote().getOpeningBalanceAmountDue() : 0;   //For Opening Debit Note
                }
                if (debitAmtDue >= debKnockedOffAmountInDNCurr) {
                    issuccessCredit = true;
                } else {
                    issuccessCredit = false;
                }
            }
            //For advance Receipt Payment
            /*
             * ERP-39732 changes made to make consistent method for checking knockoff invoices, CN/DN , advanced/refund when approving pending MP/RP 
             * Changes required in case of approving MP/RP adv to MP/RP refund , while checking for knockoff linked MP/RP converted amountdue is wrong for foreign currency
             * e.g. linked MP/RP advance is in USD and transaction currency is MYR then converting to USD and comparing entered and due in USD
             */ 
            for (ReceiptAdvanceDetail receiptAdvanceDetail : receipt.getReceiptAdvanceDetails()) {

                double receiptKnockedOffAmountInReceiptCurrency = 0.0;

                double enteredAmount = receiptAdvanceDetail.getAmount();
                receiptKnockedOffAmountInReceiptCurrency = authHandler.round((enteredAmount/receiptAdvanceDetail.getExchangeratefortransaction()), companyid);  //converting enetered amount in linked MP/RP created currency
                String advanceDetailId = receiptAdvanceDetail.getAdvancedetailid();
                double receiptAmtDue = 0;
                if (!StringUtil.isNullOrEmpty(advanceDetailId)) {           //refund case
                    KwlReturnObject resultObject = null;
                    resultObject = accReceiptDAOobj.getPaymentInformationFromAdvanceDetailId(advanceDetailId, companyid);
                    List list = resultObject.getEntityList();
                    Object[] object = (Object[]) list.get(0);
 
                    receiptAmtDue = authHandler.round(Double.parseDouble(object[7].toString()), companyid); //amount due in created currency
                } else {               //advance case

                    receiptAmtDue = authHandler.round(receiptAdvanceDetail.getAmountDue(), companyid);
                }                

                if (receiptAmtDue >= receiptKnockedOffAmountInReceiptCurrency) {    //amountdue and knockoffamt both are in same currency
                    issuccessReceipt = true;
                } else {
                    issuccessReceipt = false;
                }
            }
        } catch (Exception ex) {
            issuccessCredit = false;
            issuccessInv = false;
            issuccessReceipt = false;
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                obj.put("issuccessCredit", issuccessCredit);
                obj.put("issuccessInv", issuccessInv);
                obj.put("issuccessReceipt", issuccessReceipt);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return obj;
    }
    
      public HashMap IsAdvanceReceiptUsedInRefundPayment(JSONObject paramJObj) {
        HashMap result = new HashMap();
        try {
            JSONArray jArr = new JSONArray(paramJObj.optString(Constants.data,"[{}]"));
            String receiptid = "", receiptno = "";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                receiptno = jobj.optString(Constants.billid);
                receiptid = StringUtil.DecodeText(receiptno);
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                Set<ReceiptAdvanceDetail> advanceDetails=receipt.getReceiptAdvanceDetails();
                for (ReceiptAdvanceDetail advanceDetail : advanceDetails) {
                    List<Object[]> advPayDetails = accReceiptDAOobj.getAdvanceReceiptUsedInRefundPayment(advanceDetail.getId());
                    if(advPayDetails.size()>0) {
                        Object[] row = advPayDetails.get(0);
                        result.put("flag", true);
                        result.put("msg", "Advance Receipt #"+receipt.getReceiptNumber()+" used against Refund/Deposit Payment #"+row[0]);
                        break;
                    } else {
                        advPayDetails = accReceiptDAOobj.getAdvanceReceiptLinkedWithRefundPayment(receiptid,receipt.getCompany().getCompanyID());
                        if(advPayDetails.size()>0){
                            Object[] row = advPayDetails.get(0);
                            result.put("flag", true);
                            result.put("msg", "Advance Receipt #"+receipt.getReceiptNumber()+" is linked with Refund/Deposit Payment #"+row[1]);
                            break;
                        }    
                    }    
                }
            }
        } catch(Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    /**
     * Added @Transactional instead of txnmanager.
     * @param paramJobj
     * @return
     * @throws ServiceException
     * @throws AccountingException 
     */
    @Override 
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {JSONException.class, ServiceException.class, AccountingException.class})
    public JSONObject deleteReceiptForEdit(JSONObject paramJObj) throws ServiceException, JSONException, AccountingException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        String unableToDeleteReceiptNos = "";
        try {
            boolean advanceAmountFlag = StringUtil.getBoolean(paramJObj.optString("advanceAmountFlag"));
            boolean invoiceAmountFlag = StringUtil.getBoolean(paramJObj.optString("invoiceAmountFlag"));
            boolean cndnAmountFlag = StringUtil.getBoolean(paramJObj.optString("cndnAmountFlag"));
//            HashMap<String, Object> RefundEntryFound = IsAdvanceReceiptUsedInRefundPayment(request);
            HashMap<String, Object> RefundEntryFound = IsAdvanceReceiptUsedInRefundPayment(paramJObj);
            if (RefundEntryFound.isEmpty()) {
                HashMap<Integer, String> paymentHashMap = new HashMap<Integer, String>();
                if (paramJObj.optString("datainvoiceadvcndn",null) != null) {
                    JSONArray jSONArray = new JSONArray(paramJObj.optString("datainvoiceadvcndn","[{}]"));
                    String paymentid = "";

                    for (int i = 0; i < jSONArray.length(); i++) {
                        JSONObject jObject = jSONArray.getJSONObject(i);
                        paymentid = StringUtil.DecodeText(jObject.optString("paymentID"));
                        int invoiceadvcndntypejson = !StringUtil.isNullOrEmpty(jObject.getString("invoiceadvcndntype")) ? Integer.parseInt(jObject.getString("invoiceadvcndntype")) : 0;
                        paymentHashMap.put(invoiceadvcndntypejson, paymentid);
                    }
                }


                if (paramJObj.optString("datainvoiceadvcndn",null) != null) {
                    if (paymentHashMap.containsKey(2) && !paymentHashMap.containsKey(3) && !paymentHashMap.containsKey(1)) {
//                        deleteReceiptPermanent(request);
                      unableToDeleteReceiptNos = deleteReceiptPermanent(paramJObj);
                    } else {
                        if (advanceAmountFlag && paymentHashMap.containsKey(2)) {
//                            deleteReceiptPermanent(request, paymentHashMap.get(2));
                            deleteReceiptPermanent(paramJObj, paymentHashMap.get(2));
                        }
                        if (invoiceAmountFlag && paymentHashMap.containsKey(1)) {
//                            deleteReceiptPermanent(request, paymentHashMap.get(1));
                            deleteReceiptPermanent(paramJObj, paymentHashMap.get(1));
                        }
                        if (cndnAmountFlag && paymentHashMap.containsKey(3)) {
//                            deleteReceiptPermanent(request, paymentHashMap.get(3));
                            deleteReceiptPermanent(paramJObj, paymentHashMap.get(3));
                        }
                    }
                } else {
                    unableToDeleteReceiptNos = deleteReceiptPermanent(paramJObj);
                }
                issuccess = true;
                if (StringUtil.isNullOrEmpty(unableToDeleteReceiptNos)) {
                    msg = messageSource.getMessage("acc.receipt.del", null, Locale.forLanguageTag(paramJObj.getString(Constants.language)));   //"Receipt(s) has been deleted successfully";
                } else {
                    msg = messageSource.getMessage("acc.ob.RPExcept", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))) + " " + unableToDeleteReceiptNos.substring(0, unableToDeleteReceiptNos.length() - 2) + " " + messageSource.getMessage("acc.field.deletedsuccessfully", null, Locale.forLanguageTag(paramJObj.optString(Constants.language))) + " " + messageSource.getMessage("acc.field.belongstolockingperiod", null, Locale.forLanguageTag(paramJObj.optString(Constants.language)));
                }
            } else {
                for (Map.Entry<String, Object> entry : RefundEntryFound.entrySet()) {
                    issuccess = false;
                    msg = entry.getValue().toString();
                }
            }
        }catch (AccountingException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccReceiptPaymentModuleServiceImpl.deleteReceiptForEdit : " + ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE("" + ex.getMessage(), ex);
            }
        }
        return jobj;
    }
    
  public void deleteReceiptPermanent(JSONObject paramJObj,String receiptId) throws AccountingException, SessionExpiredException, ServiceException, ParseException, JSONException {
        try{
            String companyid = paramJObj.optString(Constants.companyKey);
            String receiptid = "", receiptno = "";
            KwlReturnObject result;

                receiptid = receiptId;
               KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                     Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                     if(receipt!=null && receipt.getRevalJeId()!=null){
                         result = accJournalEntryobj.deleteJEDtails(receipt.getRevalJeId(), companyid);// 2 For realised JE
                         result = accJournalEntryobj.deleteJE(receipt.getRevalJeId(), companyid);
                     }  
                     if(receipt!=null){
                         updateOpeningBalance(receipt,companyid);
                         JSONObject params = new JSONObject();
                           updateReceiptAdvancePaymentAmountDue(receipt, companyid,params);
                           updateDNAmountDueForReceipt(receipt, companyid);
                    }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
             requestParams.put("receiptid",receiptid);
             requestParams.put(Constants.companyKey,companyid);
             if (!StringUtil.isNullOrEmpty(receiptid)) {   
                 DateFormat dateFormatForLock = authHandler.getDateOnlyFormat();
                 Date entryDateForLock = null;
                 if (receipt!=null&&receipt.getCreationDate()!=null) {
                     entryDateForLock = receipt.getCreationDate();
                 }
                 if (entryDateForLock != null) {
                     requestParams.put("entrydate", entryDateForLock);
                     requestParams.put("df", dateFormatForLock);
                 }
                 accReceiptDAOobj.deleteReceiptPermanent(requestParams);              
                 Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                 auditRequestParams.put(Constants.reqHeader, paramJObj.getString(Constants.reqHeader));
                 auditRequestParams.put(Constants.remoteIPAddress, paramJObj.getString(Constants.remoteIPAddress));
                 auditRequestParams.put(Constants.useridKey, paramJObj.getString(Constants.useridKey));
                 auditTrailObj.insertAuditLog(AuditAction.RECEIPT_DELETED, "User " + paramJObj.getString(Constants.userfullname) + " has deleted a Receipt permanently "  +receiptno , auditRequestParams, receiptid);
          
            }
        
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null,Locale.forLanguageTag(paramJObj.getString(Constants.language))));
        }
    }  
    
      /*
        Update payment advance amount due on receipt with refund entry delete
    */
    @Override
    public void updateReceiptAdvancePaymentAmountDue(Receipt receipt, String companyId,JSONObject params) throws JSONException, ServiceException {
        /**
         * If we don't want to delete entry from receiptadvancedetail entry then
         * we have to pass isToDeleteReceiptAdvanceDetails flag as false.By
         * default we delete entry fromreceiptadvancedetail.Hence setting
         * default isToDeleteReceiptAdvanceDetails as true.ERP-39299.
         */
        boolean isToDeleteReceiptAdvanceDetails = params.optBoolean("isToDeleteReceiptAdvanceDetails", true);       
        if (receipt != null) {
            Set<ReceiptAdvanceDetail> advanceDetails = receipt.getReceiptAdvanceDetails();
            if (receipt.getReceiptAdvanceDetails() != null && !advanceDetails.isEmpty()) {
                for (ReceiptAdvanceDetail adv : advanceDetails) {
                    if (adv.getAdvancedetailid() != null) {
                        /*
                         * amountReceived with positive sign as we have to add
                         * amount on delete transaction
                         */
                        double amountReceived = adv.getAmount() / adv.getExchangeratefortransaction();
                        accountingHandlerDAOobj.updateAdvanceDetailAmountDueOnAmountReceived(adv.getAdvancedetailid(), amountReceived);
                    }
                    if (adv != null && adv.getRevalJeId() != null) { //Deleting Refund Revaluation JE 
                        accJournalEntryobj.deleteJEDtails(adv.getRevalJeId(), companyId);
                        accJournalEntryobj.deleteJE(adv.getRevalJeId(), companyId);
                    }
                }
                /**
                 * If we don't want to delete ReceiptAdvanceDetails table entry
                 * then we need to pass isToDeleteReceiptAdvanceDetails flag as
                 * false.ERP-39299.
                 */
                if(isToDeleteReceiptAdvanceDetails){
                    accReceiptDAOobj.deleteReceiptAdvanceDetails(receipt.getID(), receipt.getCompany().getCompanyID());
                }
            }
        }
    }
    
   /*
     * Update invoice due amount when payment is being deleted.
     */
    @Override
    public void updateOpeningBalance(Receipt receipt, String companyId) throws JSONException, ServiceException {
        if (receipt != null) {
            Set<ReceiptDetail> receiptDetailSet = receipt.getRows();
            if (receiptDetailSet != null && !receipt.isDeleted()) { // if receipt already temporary deleted then amountdue already updated. No need to update amountdue again for permament delete.
                for (ReceiptDetail row:receiptDetailSet) {
                    double discountAmtInInvoiceCurrency = row.getDiscountAmountInInvoiceCurrency();
                    double discountAmount = row.getDiscountAmount();
                    if (!row.getInvoice().isNormalInvoice() && row.getInvoice().isIsOpeningBalenceInvoice()) {
                        double amountPaid = row.getAmount() + row.getDiscountAmount();
//                        double amountReceivedInBaseCurrency = row.getAmountInBaseCurrency();
                        Invoice invoice = row.getInvoice();
                        boolean isInvoiceIsClaimed = (invoice.getBadDebtType() == Constants.Invoice_Claimed || invoice.getBadDebtType() == Constants.Invoice_Recovered);
                        HashMap<String, Object> requestParams = new HashMap();
                        requestParams.put(Constants.companyid, companyId);
                        requestParams.put(Constants.globalCurrencyKey, receipt.getCompany().getCurrency().getCurrencyID());
//                            double openingbalanceBaseAmountDue = amountReceivedInBaseCurrency;
                            double invoiceAmountDue = invoice.getOpeningBalanceAmountDue();
                            double totalInvoiceAndDiscountAmtInInvoiceCurrency = row.getAmountInInvoiceCurrency()+discountAmtInInvoiceCurrency;
//                            double discountAmountInBase = authHandler.round(discountAmtInInvoiceCurrency*invoice.getExchangeRateForOpeningTransaction(),companyId);
                            double totalInvoiceAndDiscountAmtInBase = 0d;
                            if (invoice.isConversionRateFromCurrencyToBase()) {
                                totalInvoiceAndDiscountAmtInBase = authHandler.round(totalInvoiceAndDiscountAmtInInvoiceCurrency * invoice.getExchangeRateForOpeningTransaction(), companyId);
                            } else {
                                totalInvoiceAndDiscountAmtInBase = authHandler.round(totalInvoiceAndDiscountAmtInInvoiceCurrency / invoice.getExchangeRateForOpeningTransaction(), companyId);
                            }
                            double invoiceBaseAmountDue = invoice.getOpeningBalanceBaseAmountDue() + totalInvoiceAndDiscountAmtInBase;
                            if (row.getExchangeRateForTransaction() != 0) {
                                invoiceAmountDue += amountPaid / row.getExchangeRateForTransaction();
                            } else {
                                invoiceAmountDue += amountPaid;
                            }
                        JSONObject invjson = new JSONObject();
                        invjson.put("invoiceid", invoice.getID());
                        if(invoice.getShipDate()!=null){    //SDP-12208
                            invjson.put("shipdate", invoice.getShipDate());
                        }
                        invjson.put(Constants.companyKey, companyId);
                        if (isInvoiceIsClaimed) {
                            invjson.put(Constants.claimAmountDue, invoice.getClaimAmountDue() + row.getAmountInInvoiceCurrency());
                        } else {
                            invjson.put("openingBalanceAmountDue", invoiceAmountDue);
                            if (invoiceAmountDue != 0) {
                                invjson.put("amountduedate", "");
                            }
                            invjson.put(Constants.openingBalanceBaseAmountDue, invoiceBaseAmountDue);
                        }
                        accInvoiceDAOObj.updateInvoice(invjson, null);
                    } else if (row.getInvoice().isNormalInvoice()){
                        double amountPaid = row.getAmount();
                        double amountPaidInInvoiceCurrecncy = row.getAmountInInvoiceCurrency();
//                        double amountReceivedInBaseCurrency = row.getAmountInBaseCurrency();
                        Invoice invoice = row.getInvoice();
                        boolean isInvoiceIsClaimed = (invoice.getBadDebtType() == Constants.Invoice_Claimed || invoice.getBadDebtType() == Constants.Invoice_Recovered);
                        JSONObject invjson = new JSONObject();
                        invjson.put("invoiceid", invoice.getID());
                        if(invoice.getShipDate()!=null){    //SDP-12208
                            invjson.put("shipdate", invoice.getShipDate());
                        }
                        invjson.put(Constants.companyKey, companyId);
                        if (isInvoiceIsClaimed) {
                            invjson.put(Constants.claimAmountDue, invoice.getClaimAmountDue() + row.getAmountInInvoiceCurrency());
                        } else {
                            double invoiceAmountDue = invoice.getInvoiceamountdue();
                            double totalInvoiceAmtPaidAndDiscountInBase = 0d;
                            double totalInvoiceAndDiscountAmtInInvoiceCurrency = amountPaidInInvoiceCurrecncy+discountAmtInInvoiceCurrency;
                            HashMap<String, Object> requestParams = new HashMap();
                            requestParams.put(Constants.companyid, companyId);
                            requestParams.put(Constants.globalCurrencyKey, invoice.getCurrency().getCurrencyID());
                            KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, totalInvoiceAndDiscountAmtInInvoiceCurrency, invoice.getCurrency().getCurrencyID(), invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                            totalInvoiceAmtPaidAndDiscountInBase = ((Double) bAmt.getEntityList().get(0));
                            double invoiceBaseAmountDue = invoice.getInvoiceAmountDueInBase() + totalInvoiceAmtPaidAndDiscountInBase;
                            if (row.getExchangeRateForTransaction() != 0) {
                                if (amountPaidInInvoiceCurrecncy != 0) {
                                    invoiceAmountDue += ((amountPaid+discountAmount) / row.getExchangeRateForTransaction());
                                } else {
                                    invoiceAmountDue += (totalInvoiceAndDiscountAmtInInvoiceCurrency);
                                }
                            } else {
                                invoiceAmountDue += amountPaid;
                            }
                            invjson.put(Constants.invoiceamountdue, invoiceAmountDue);
                            invjson.put(Constants.invoiceamountdueinbase, invoiceBaseAmountDue);
                            if (invoiceAmountDue != 0) {
                                invjson.put("amountduedate", "");
                            }
                        }
                        accInvoiceDAOObj.updateInvoice(invjson, null);
                    }
                }
            }
        }
    }
    
    @Override  
     public String deleteReceiptPermanent(JSONObject paramJObj) throws AccountingException, SessionExpiredException, ServiceException, ParseException, JSONException {
           String unableToDeleteReceiptNos = "";
         try {
            String companyid = paramJObj.optString(Constants.companyKey);
            JSONArray jArr = new JSONArray(paramJObj.optString(Constants.data,"[{}]"));
            String receiptid = "", receiptno = "",entryno="",jeid="";
            KwlReturnObject result;
            HashMap<String,Object> reconcileMap = new HashMap<>();
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                receiptid = StringUtil.DecodeText(jobj.optString(Constants.billid));
                receiptno = jobj.optString("billno");
                entryno = jobj.optString("entryno", "");
                jeid = jobj.optString("journalentryid");
                String roundingJENo = "";
                String roundingJEIds = "";
                /**
                 * Method to check the payment is Reconciled or not according to
                 * its JE id
                 */
                reconcileMap.put("jeid", jeid);
                reconcileMap.put(Constants.companyKey, companyid);
                boolean isReconciledFlag = accBankReconciliationObj.isRecordReconciled(reconcileMap);
                if (isReconciledFlag) {
                    throw new AccountingException(messageSource.getMessage("acc.reconcilation.Cannotdeletepayment", null,Locale.forLanguageTag(paramJObj.getString("language"))) + " " + "<b>" + receiptno + "</b>" + " " + messageSource.getMessage("acc.reconcilation.asitisreconciled", null,Locale.forLanguageTag(paramJObj.getString("language"))));
                }
                accBankReconciliationObj.deleteUnReconciliationRecords(reconcileMap);
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                
                /**
                 * Need to check TDS / TCS JE posted for receipt
                 */
                if (receipt.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("receiptid", receiptid);
                    KwlReturnObject kwlReturnObject = accReceiptDAOobj.getReceiptInvoiceJEMapping(jSONObject);
                    if (!kwlReturnObject.getEntityList().isEmpty() && kwlReturnObject.getEntityList().size() > 0 && kwlReturnObject.getEntityList().get(0) != null) {
                        throw new AccountingException(messageSource.getMessage("acc.reconcilation.Cannotdeletepayment", null, Locale.forLanguageTag(paramJObj.getString("language"))) + " " + "<b>" + receiptno + "</b>" + " " + messageSource.getMessage("acc.tdstcs.linked", null, Locale.forLanguageTag(paramJObj.getString("language"))));
                    }
                }

 
               //Delete unrealised JE for Advance Receipt
                if(!receipt.getReceiptAdvanceDetails().isEmpty()){
                    accJournalEntryobj.permanentDeleteJournalEntryDetailReval(receipt.getID(), companyid);
                    accJournalEntryobj.permanentDeleteJournalEntryReval(receipt.getID(), companyid);
                    /**
                     * Deleting Reevaluation History of the receipt being
                     * deleted.
                     */
                    JSONObject paramJobj = new JSONObject();
                    paramJobj.put("documentId", receipt.getID());
                    paramJobj.put("companyID", companyid);
                    accJournalEntryobj.deleteRevaluationHistory(paramJobj);
                    
                }
                
                
                //Delete Realised JE which was posted aginst Invoice
                if (receipt != null && receipt.getRevalJeId() != null) {
                    result = accJournalEntryobj.deleteJEDtails(receipt.getRevalJeId(), companyid);// 2 For realised JE
                    result = accJournalEntryobj.deleteJE(receipt.getRevalJeId(), companyid);
                }
                
                /*
                    Delete Forex Gains/Loss JEs 
                */
                if(receipt.getLinkDetailReceipts() != null && !receipt.getLinkDetailReceipts().isEmpty()) {
                    Set<LinkDetailReceipt> linkedDetailReceiptList = receipt.getLinkDetailReceipts();
                    for(LinkDetailReceipt ldprow : linkedDetailReceiptList) {
                        if(!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                            result = accJournalEntryobj.deleteJEDtails(ldprow.getLinkedGainLossJE(), companyid);
                            result = accJournalEntryobj.deleteJE(ldprow.getLinkedGainLossJE(), companyid);
                        }
                        //Deleting Revaluation journal entry
                        if (ldprow != null && !StringUtil.isNullOrEmpty(ldprow.getRevalJeId())) {
                            accJournalEntryobj.deleteJEDtails(ldprow.getRevalJeId(), companyid);
                            accJournalEntryobj.deleteJE(ldprow.getRevalJeId(), companyid);
                        }
                        if (ldprow != null && !StringUtil.isNullOrEmpty(ldprow.getRevalJeIdReceipt())) {
                            accJournalEntryobj.deleteJEDtails(ldprow.getRevalJeIdReceipt(), companyid);
                            accJournalEntryobj.deleteJE(ldprow.getRevalJeIdReceipt(), companyid);
                        }
                        /**
                         * Delete JE posted for GST Linking
                         */
                        if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGSTJE())) {
                            result = accJournalEntryobj.deleteJEDtails(ldprow.getLinkedGSTJE(), companyid);
                            result = accJournalEntryobj.deleteJE(ldprow.getLinkedGSTJE(), companyid);
                        }
                    }
                }
                
                
                if(receipt.getLinkDetailReceiptsToDebitNote() != null && !receipt.getLinkDetailReceiptsToDebitNote().isEmpty()) {
                    Set<LinkDetailReceiptToDebitNote> linkedDetailReceiptList = receipt.getLinkDetailReceiptsToDebitNote();
                    for(LinkDetailReceiptToDebitNote ldprow : linkedDetailReceiptList) {
                        if(!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                            result = accJournalEntryobj.deleteJEDtails(ldprow.getLinkedGainLossJE(), companyid);
                            result = accJournalEntryobj.deleteJE(ldprow.getLinkedGainLossJE(), companyid);
                        }
                        //Deleting Revaluation journal entry
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
                 * Deleting Advance Payment entries for refund receipt
                 */
                if (receipt.getLinkDetailReceiptsToAdvancePayment() != null && !receipt.getLinkDetailReceiptsToAdvancePayment().isEmpty()) {
                    Set<LinkDetailReceiptToAdvancePayment> linkedDetailReceiptList = receipt.getLinkDetailReceiptsToAdvancePayment();
                    for (LinkDetailReceiptToAdvancePayment ldprow : linkedDetailReceiptList) {
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
                Set<String> invoiceIDSet = new HashSet<>();
                if (receipt != null && receipt.getApprovestatuslevel() == 11) {
                    for (ReceiptDetail rd : receipt.getRows()) {
                        if (rd.getInvoice() != null) {
                            invoiceIDSet.add(rd.getInvoice().getID());
                        }
                    }
                    for (LinkDetailReceipt rd : receipt.getLinkDetailReceipts()) {
                        if (rd.getInvoice() != null) {
                            invoiceIDSet.add(rd.getInvoice().getID());
                        }
                    }
                }
                /*
                 * Deleting Bad Debt Invoices Recovery entries
                 */
                HashMap<String, Object> requestParamsForMapping = new HashMap();
                requestParamsForMapping.put(Constants.companyid, companyid);
                requestParamsForMapping.put("receiptid", receiptid);
                accInvoiceDAOObj.deleteBadDebtInvoiceMapping(requestParamsForMapping);
                if (receipt != null) {
                    //will update Debit note, Advance payment amount, opening balance and loan amount due only when approved recipt is deleted                     
                    if (receipt.getApprovestatuslevel() == Constants.APPROVED_STATUS_LEVEL) {                    
                        updateOpeningBalance(receipt, companyid);
                        if (receipt.getLinkDetailReceiptsToAdvancePayment() == null || receipt.getLinkDetailReceiptsToAdvancePayment().isEmpty()) {
                            JSONObject params = new JSONObject();
                            updateReceiptAdvancePaymentAmountDue(receipt, companyid,params);
                        }
                        updateDNAmountDueForReceipt(receipt, companyid);
                        updateReceiptLoanAmountDue(receipt, companyid, true);
                    }
                }
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("receiptid", receiptid);
                requestParams.put(Constants.companyKey, companyid);
                requestParams.put("receiptno", receiptno);
                if (!StringUtil.isNullOrEmpty(receiptid)) {
                    DateFormat dateFormatForLock = authHandler.getDateOnlyFormat();
                    Date entryDateForLock = null;
                    if (jobj.has(Constants.BillDate)) {
                        entryDateForLock = dateFormatForLock.parse(jobj.getString(Constants.BillDate));
                    }
                    if (entryDateForLock != null) {
                        requestParams.put("entrydate", entryDateForLock);
                        requestParams.put("df", dateFormatForLock);
                    }
                    /*
                     * If Receive Payment is deleted as temporary then receipt.isdelete flag will be true and amount due isn't update.
                     * if receipt.isdeleted is false amount due is update.
                     */
                    Map<String, Object> requestMap = AccountingManager.getGlobalParamsJson(paramJObj);
                    if (receipt.getLinkDetailReceipts() != null && !receipt.getLinkDetailReceipts().isEmpty()) {
                        if (!receipt.isDeleted()) {
                            accReceiptDAOobj.deleteLinkReceiptsDetailsAndUpdateAmountDue(requestMap,receiptid, companyid, false);
                        } else {
                            accReceiptDAOobj.deleteLinkReceiptDetails(receiptid, companyid);
                        }
                    }
                    /*
                     * If Receive Payment is deleted as temporary then receipt.isdelete flag will be true and amount due isn't update.
                     * if receipt.isdeleted is false amount due is update.
                     */
                    if (receipt.getLinkDetailReceiptsToDebitNote() != null && !receipt.getLinkDetailReceiptsToDebitNote().isEmpty()) {
                        if (!receipt.isDeleted()) {
                            accReceiptDAOobj.deleteLinkReceiptsDetailsToDebitNoteAndUpdateAmountDue(receiptid, companyid, false);
                        } else {
                            accReceiptDAOobj.deleteLinkReceiptToDebitNoteDetails(receiptid, companyid);
                        }
                    }
                    /*
                     * If Receive Payment is deleted as temporary then receipt.isdelete flag will be true and amount due isn't update.
                     * if receipt.isdeleted is false amount due is update.
                     * for deleting link details of advance payment for refund receipt
                     */
                    if (receipt.getLinkDetailReceiptsToAdvancePayment() != null && !receipt.getLinkDetailReceiptsToAdvancePayment().isEmpty()) {
                        if (!receipt.isDeleted()) {
                        accReceiptDAOobj.deleteLinkReceiptsDetailsToAdvancePaymentAndUpdateAmountDue(receiptid, companyid,false);
                        }else{
                        accReceiptDAOobj.deleteLinkReceiptToAdvancePaymentDetails(receiptid, companyid);
                        }
                    }
                    /*
                     * If Receive Payment is deleted as permanent then receipt.
                     * Need to delete linking information with SO ie need to
                     * delete records from solinking table.
                     */
                    if (receipt.getReceiptAdvanceDetails() != null && !receipt.getReceiptAdvanceDetails().isEmpty()) {
                       Set<ReceiptAdvanceDetail> advanceDetails = receipt.getReceiptAdvanceDetails();
                        if (receipt.getReceiptAdvanceDetails() != null && !advanceDetails.isEmpty()) {
                            String idstring = "";
                            for (ReceiptAdvanceDetail receiptAdvanceDetail : advanceDetails) {
                                idstring += "'" + receiptAdvanceDetail.getId() + "',";
                            }
                            if (idstring.length() > 1) {
                                idstring = idstring.substring(0, idstring.length() - 1);
                               accReceiptDAOobj.deleteLinkReceiptToSalesOrder(idstring, companyid);
                            }

                        }
                        
                    }

//                    if(true){
//                        throw new AccountingException("My Exception");
//                    }
                    /* Deleting Linking iformation of Received Payment from Linking table it it is deleted*/
                    
                    accReceiptDAOobj.deleteLinkingInformationOfRP(requestParams);
                    /*
                     *ERP-40733
                     *Throwing accounting exception for Account locking period and get unable to delete numbers
                     */
                    try {
                        accReceiptDAOobj.deleteReceiptPermanent(requestParams);
                    } catch (AccountingException ex) {
                        unableToDeleteReceiptNos += receiptno + ", ";
                        continue;
                    }
                    if (receipt.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                        Set<ReceiptAdvanceDetail> advanceDetails = receipt.getReceiptAdvanceDetails();
                        if (receipt.getReceiptAdvanceDetails() != null && !advanceDetails.isEmpty()) {
                            String idstring = "";
                            for (ReceiptAdvanceDetail receiptAdvanceDetail : advanceDetails) {
                                idstring += "'" + receiptAdvanceDetail.getId() + "',";
                            }
                            if (idstring.length() > 1) {
                                idstring = idstring.substring(0, idstring.length() - 1);
                                fieldManagerDAOobj.deleteGstTaxClassDetails(idstring);
                            }
                            fieldManagerDAOobj.deleteGstDocHistoryDetails(receiptid);
                        }
                    }
                    //Delete Rouding JEs if created against PI
                    if (!invoiceIDSet.isEmpty()) {
                        String invIDs = "";
                        for (String invID : invoiceIDSet) {
                            invIDs += invID + ",";
                        }
                        if (!StringUtil.isNullOrEmpty(invIDs)) {
                            invIDs = invIDs.substring(0, invIDs.length() - 1);
                        }
                        KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(invIDs, companyid);
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
                    
                    StringBuffer journalEntryMsg = new StringBuffer();
                    if (!StringUtil.isNullOrEmpty(entryno)) {
                        journalEntryMsg.append(" along with the JE No. " + entryno);
                    }
                    Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                    auditRequestParams.put(Constants.reqHeader, paramJObj.getString(Constants.reqHeader));
                    auditRequestParams.put(Constants.remoteIPAddress, paramJObj.getString(Constants.remoteIPAddress));
                    auditRequestParams.put(Constants.useridKey, paramJObj.getString(Constants.useridKey));
                    
                    auditTrailObj.insertAuditLog(AuditAction.RECEIPT_DELETED, "User " + paramJObj.getString(Constants.userfullname) + " has deleted a Receipt permanently " + receiptno+journalEntryMsg.toString(), auditRequestParams, receiptid);
                    if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                        auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_DELETED, "User " + paramJObj.getString(Constants.userfullname) + " has deleted a Receipt Permanently " + receiptno + ". So Rounding JE No. " + roundingJENo + " deleted.", auditRequestParams, roundingJEIds);
                    }

                }
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null,Locale.forLanguageTag(paramJObj.getString(Constants.language))));
        }
      return unableToDeleteReceiptNos;   
    } 
    
 @Override 
     public void updateReceiptLoanAmountDue(Receipt receipt, String companyId, boolean isPermanentDelete) throws JSONException, ServiceException {
        if (receipt != null) {
            Set<ReceiptDetailLoan> details = receipt.getReceiptDetailsLoan();
            if (details != null && !details.isEmpty()) {
                for (ReceiptDetailLoan loanDetail : details) {
                    RepaymentDetails RD = loanDetail.getRepaymentDetail();
                    RD.setAmountdue(RD.getAmountdue() + loanDetail.getAmountInRepaymentDetailCurrency());
                    RD.setPaymentStatus(PaymentStatus.Unpaid);
                    if (isPermanentDelete) {
                        accReceiptDAOobj.deleteReceiptDetailsLoan(receipt.getID(), companyId);
                    }
                }
            }
        }
    }

    /**
     * Description: This Method is used to Update DEBIT NOTE amount due for receipt in case Delete(temporary/Permanent) 
     * @param receipt
     * @param companyId 
     */
   @Override 
    public void updateDNAmountDueForReceipt(Receipt receipt, String companyId) throws ServiceException {
        if (receipt != null && !receipt.isDeleted()) { // if receipt already temporary deleted then amountdue already updated. No need to update amountdue again for permament delete.
            String receiptId = receipt.getID();
            KwlReturnObject dnhistoryresult = accReceiptDAOobj.getCustomerDnPayment(receiptId);
            List<DebitNotePaymentDetails> dnHistoryList = dnhistoryresult.getEntityList();
            for (DebitNotePaymentDetails dnpd : dnHistoryList) {
                String dnid = dnpd.getDebitnote().getID() != null ? dnpd.getDebitnote().getID() : "";
                Double dnpaidamount = dnpd.getAmountPaid();
                Double dnPaidAmountInBaseCurrency = dnpd.getAmountInBaseCurrency();
                KwlReturnObject dnjedresult = accReceiptDAOobj.updateDnAmount(dnid, -dnpaidamount);
                KwlReturnObject opendnjedresult = accReceiptDAOobj.updateDnOpeningAmountDue(dnid, -dnpaidamount);
                KwlReturnObject openingdnBaseAmtDueResult = accReceiptDAOobj.updateDnOpeningBaseAmountDue(dnid, -dnPaidAmountInBaseCurrency);
                if (dnpd.getDebitnote() != null && !StringUtil.isNullOrEmpty(dnpd.getDebitnote().getRevalJeId())) {
                    accReceiptDAOobj.deleteJEDtails(dnpd.getDebitnote().getRevalJeId(), companyId);
                    accReceiptDAOobj.deleteJEEntry(dnpd.getDebitnote().getRevalJeId(), companyId);
                }
            }
        }
    }
   
    /**
     * @param : paramJobj
     * @Desc : Method to post rounding JE when PI linked in Payment 
     * @throws : ServiceException
     * @Return : void
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor={ServiceException.class})
    public void postRoundingJEAfterLinkingInvoiceInReceipt(JSONObject paramJobj) throws ServiceException {
        try {
            String paymentid = paramJobj.optString("paymentid", "");
            KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), paymentid);
            Receipt receipt = (Receipt) paymentResult.getEntityList().get(0);
            String receiptNumber = receipt!=null?receipt.getReceiptNumber():"";
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
                    KwlReturnObject grresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceId);
                    Invoice invoice = (Invoice) grresult.getEntityList().get(0);
                    if ((invoice.isIsOpeningBalenceInvoice() && invoice.getOpeningBalanceAmountDue() == 0) || (invoice.isNormalInvoice() && invoice.getInvoiceamountdue() == 0)) {
                        //below method return Rounding JE if created, otherwise it returns null
                        paramJobj.put("salesInvoiceObj", invoice);
                        JournalEntry roundingJE = accInvoiceModuleService.createRoundingOffJE(paramJobj);
                        if (roundingJE != null) {
                             auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has linked Vendor Invoice " + invoice.getInvoiceNumber() + " with  Receipt " + receiptNumber + ". Rounding JE "+roundingJE.getEntryNumber()+" posted.", auditRequestParams, roundingJE.getID());
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
    @Override
    public void postRoundingJEOnReceiptSave(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, AccountingException {
        Invoice invoice = (Invoice) paramJobj.get("salesInvoiceObj");
        String invNumber = invoice.getInvoiceNumber();
        String  receiptnumber =  paramJobj.getString("receiptnumber");
        boolean isEditReceipt = paramJobj.optBoolean("isEdit", false);
        String companyid = paramJobj.getString(Constants.companyKey);

        Map<String, Object> auditRequestParams = new HashMap<>();
        auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
        auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
        auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
        
        if ((invoice.isIsOpeningBalenceInvoice() && invoice.getOpeningBalanceAmountDue() == 0) || (invoice.isNormalInvoice() && invoice.getInvoiceamountdue() == 0)) {
            //below method return Rounding JE if created otherwise it returns null
            JournalEntry roundingJE = accInvoiceModuleService.createRoundingOffJE(paramJobj);
            if (roundingJE != null) {
                String jeid = roundingJE.getID();
                String jenumber = roundingJE.getEntryNumber();
                if(isEditReceipt){
                    auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_UPDATED, "User " + paramJobj.optString(Constants.userfullname) + " has knocked off Sales Invoice " + invNumber + " against Receipt " + receiptnumber + ". Rounding JE "+jenumber+" updated.", auditRequestParams, jeid); 
                } else{
                    auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has knocked off Sales Invoice " + invNumber + " against Receipt " + receiptnumber + ". Rounding JE "+jenumber+" posted.", auditRequestParams, jeid);
                }
            }
        } else if (isEditReceipt) {//If amount due becomes non zero in edit case then we need to check wheather rounding JE was generated for this GR or not if yes then need to delete
            KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(invoice.getID(), companyid);
            List<JournalEntry> jeList = jeResult.getEntityList();
            for (JournalEntry roundingJE : jeList) {
                String jeid = roundingJE.getID();
                String jenumber = roundingJE.getEntryNumber();
                deleteJEArray(roundingJE.getID(),companyid);
                auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_DELETED, "User " + paramJobj.optString(Constants.userfullname) + " has edited  Receipt " + receiptnumber + ". Rounding JE "+jenumber+" deleted.", auditRequestParams, jeid);
            }
        }
    }
    
    /**
     * @param : paramJobj @Desc : Method to post rounding JE when Payment approved by User
     * @throws : JSONException, ServiceException, SessionExpiredException,AccountingException
     * @Return : void
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {ServiceException.class})
    public void postRoundingJEAfterReceiptApprove(JSONObject paramJobj) throws ServiceException {
        try {
            String paymentid = paramJobj.optString(Constants.billid, "");
            KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), paymentid);
            Receipt receipt = (Receipt) paymentResult.getEntityList().get(0);

            //Here used method evictObj to remove current payment object from session.
            //It was giving different value from database

            accountingHandlerDAOobj.evictObj(receipt);
            paymentResult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), paymentid);
            receipt = (Receipt) paymentResult.getEntityList().get(0);

            if (receipt != null && receipt.getApprovestatuslevel() == 11) {//Code will execute for approved payment only
                String receiptNumber = receipt != null ? receipt.getReceiptNumber() : "";
                Map<String, Object> auditRequestParams = new HashMap<>();
                auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));

                for (ReceiptDetail detail : receipt.getRows()) {
                    Invoice invoice = detail.getInvoice();
                    if (invoice != null && ((invoice.isIsOpeningBalenceInvoice() && invoice.getOpeningBalanceAmountDue() == 0) || (invoice.isNormalInvoice() && invoice.getInvoiceamountdue() == 0))) {
                        paramJobj.put("salesInvoiceObj", invoice);
                        JournalEntry roundingJE = accInvoiceModuleService.createRoundingOffJE(paramJobj);
                        if (roundingJE != null) {
                            auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has approved Receipt " + receiptNumber + ". Rounding JE " + roundingJE.getEntryNumber() + " posted.", auditRequestParams, roundingJE.getID());
                        }
                    }
                }
            }
        } catch (ServiceException | JSONException | SessionExpiredException | AccountingException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }   
    /**
     * @param : paramJobj @Desc : Method to post rounding JE when Payment approved by User
     * @throws : JSONException, ServiceException, SessionExpiredException,AccountingException
     * @Return : void
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {ServiceException.class})
    public void postRoundingJEOnRevertDishonouredReceipt(JSONObject paramJobj, Set<String> amountDueUpdatedInvoiceIDSet) throws ServiceException {
        try {
            String receiptNumber = paramJobj.optString("reciptNumber");
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));

            for (String invoiceID : amountDueUpdatedInvoiceIDSet) {
                KwlReturnObject paymentResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceID);
                Invoice invoice = (Invoice) paymentResult.getEntityList().get(0);

                if (invoice != null && ((invoice.isIsOpeningBalenceInvoice() && invoice.getOpeningBalanceAmountDue() == 0) || (invoice.isNormalInvoice() && invoice.getInvoiceamountdue() == 0))) {
                    paramJobj.put("salesInvoiceObj", invoice);
                    JournalEntry roundingJE = accInvoiceModuleService.createRoundingOffJE(paramJobj);
                    if (roundingJE != null) {
                        auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has approved Receipt " + receiptNumber + ". Rounding JE " + roundingJE.getEntryNumber() + " posted.", auditRequestParams, roundingJE.getID());
                    }
                }
            }
        } catch (ServiceException | JSONException | SessionExpiredException | AccountingException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    
  @Override  
    public JSONObject linkReceiptToDocumentsJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            boolean isRefundTransaction = (!StringUtil.isNullOrEmpty(paramJobj.optString("isRefundTransaction",null))) ? Boolean.parseBoolean(paramJobj.optString("isRefundTransaction")) : false;
            String ReceiptJENumber="";
            List li = linkReceiptToDocuments(paramJobj);            
            issuccess = true;
            if (li.size()!= 0 && li.get(0) != null) {
                     ReceiptJENumber= (String) li.get(0);
            }
            txnManager.commit(status);
            status=null;
            // success msg for refund transaction
            if (isRefundTransaction) {
                msg = messageSource.getMessage("acc.field.paymentInformationHasBeenLinkedtoAdvancePaymentSuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language")));
            } else {
                msg = messageSource.getMessage("acc.field.PaymentinformationhasbeenLinkedtoCIAndDNsuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language")));
                if (!StringUtil.isNullOrEmpty(ReceiptJENumber)) {
                    msg += "JE No.:" + "<b>" + ReceiptJENumber + "<b>";
                }
            }
            
            
            status = txnManager.getTransaction(def);
            //========Code for Rounding JE Start=============
            try {
                postRoundingJEAfterLinkingInvoiceInReceipt(paramJobj);
            } catch (ServiceException | TransactionException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
           txnManager.commit(status);
            
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    } 
    
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor={ServiceException.class,SessionExpiredException.class,AccountingException.class})
    public List linkReceiptToDocuments(JSONObject paramJobj) throws ServiceException, SessionExpiredException, AccountingException {
        List result = new ArrayList();
        List linkedInvoicesList = new ArrayList();
        List linkedNotesList = new ArrayList();
        List linkedAdvancePaymentList = new ArrayList();
        try {

            JSONArray invoiceArray = new JSONArray();
            JSONArray noteArray = new JSONArray();
            JSONArray advancePaymentArray = new JSONArray();
            JSONArray linkJSONArray = paramJobj.optString("linkdetails",null) != null ? new JSONArray(paramJobj.optString("linkdetails","[{}]")) : new JSONArray();
            Map<String,Object> counterMap=new HashMap<>();
            counterMap.put("counter", 0);
            for (int i = 0; i < linkJSONArray.length(); i++) {
                JSONObject obj = linkJSONArray.getJSONObject(i);
                int documenttype = Integer.parseInt(obj.optString("documentType"));
                if (documenttype == Constants.PaymentAgainstInvoice && obj.optDouble("linkamount", 0.0) != 0) {
                    invoiceArray.put(obj);
                } else if (documenttype == Constants.PaymentAgainstCNDN && obj.optDouble("linkamount", 0.0) != 0) {
                    noteArray.put(obj);
                } else if (documenttype == Constants.AdvancePayment && obj.optDouble("linkamount", 0.0) != 0) { // record of advance payment type added for linking to refund receipt
                    advancePaymentArray.put(obj);
                }
            }

            if (invoiceArray.length() > 0) {
                linkedInvoicesList = linkReceiptToInvoices(paramJobj, invoiceArray, counterMap);
            }

        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        result.addAll(linkedInvoicesList);
        result.addAll(linkedNotesList);
        result.addAll(linkedAdvancePaymentList);
        return result;
    }
  
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor={ServiceException.class,SessionExpiredException.class,AccountingException.class})
    public double ReevalJournalEntryForOpeningReceipt(JSONObject paramJobj, Receipt receipt, double linkReceiptAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {
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
            GlobalParams.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
            GlobalParams.put(Constants.globalCurrencyKey, basecurrency);
            GlobalParams.put("dateformat", authHandler.getDateOnlyFormat());
            Date creationDate = receipt.getCreationDate();
            boolean isopeningBalanceInvoice = receipt.isIsOpeningBalenceReceipt();
            tranDate = receipt.getCreationDate();
            if (!receipt.isNormalReceipt()) {
                exchangeRate = receipt.getExchangeRateForOpeningTransaction();
                exchangeRateReval = exchangeRate;
            } else {
                exchangeRate = receipt.getJournalEntry().getExternalCurrencyRate();
                exchangeRateReval = exchangeRate;
            }
            HashMap<String, Object> invoiceId = new HashMap<String, Object>();
            invoiceId.put("invoiceid", receipt.getID());
            invoiceId.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
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
            KwlReturnObject bAmt = null;
            if (isopeningBalanceInvoice && receipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            }

            double oldrate = (Double) bAmt.getEntityList().get(0);
            if (revalueationHistory == null && isopeningBalanceInvoice && receipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            }

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
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return finalAmountReval;
    } 
   
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor={ServiceException.class,SessionExpiredException.class,Exception.class})
    public List linkReceiptToInvoices(JSONObject paramJobj, JSONArray invoiceArray, Map<String, Object> counterMap) throws ServiceException, SessionExpiredException {
        List result = new ArrayList();
        GoodsReceipt greceipt = null;
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            String paymentid = paramJobj.optString("paymentid");
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), paymentid);
            Receipt payment = (Receipt) receiptObj.getEntityList().get(0);
            String paymentno = payment.getReceiptNumber();

            boolean isopeningBalancePayment = payment.isIsOpeningBalenceReceipt();


            JSONArray jArr = new JSONArray();
            String customerId = "";
            String linkedInvoiceids = "";
            String linkedInvoicenos = "";
            String JENumber="";
            for (int k = 0; k < invoiceArray.length(); k++) {//creating a hash map with payment and their linked invoice
                JSONObject jSONObject = invoiceArray.getJSONObject(k);
                if (jSONObject.optDouble("linkamount", 0) != 0) {
                    String invoiceId = jSONObject.optString("documentid", "");
                    double invAmount = jSONObject.optDouble("linkamount", 0);
                    double exchangeratefortransaction = jSONObject.optDouble("exchangeratefortransaction", 1);
                    double amountdue = jSONObject.optDouble("amountdue", 0);
                    double amountDueOriginal = jSONObject.optDouble("amountDueOriginal", 0);

                    KwlReturnObject grresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceId);
                    Invoice gr = (Invoice) grresult.getEntityList().get(0);
                    if (linkedInvoiceids.equals("") && gr != null) {
                        linkedInvoiceids += gr.getID();
                        linkedInvoicenos += gr.getInvoiceNumber();
                    } else if (gr != null) {
                        linkedInvoiceids += "," + gr.getID();
                        linkedInvoicenos += "," + gr.getInvoiceNumber();
                    }
                    customerId = gr.getCustomer().getID();
                    JSONObject jobj = new JSONObject();
                    jobj.put("enteramount", invAmount);
                    jobj.put("documentid", invoiceId);
                    jobj.put("isopeningBalancePayment", isopeningBalancePayment);
                    jobj.put("isConversionRateFromCurrencyToBase", payment.isConversionRateFromCurrencyToBase());
                    jobj.put("exchangeratefortransaction", exchangeratefortransaction);
                    jobj.put("amountdue", amountdue);
                    jobj.put("amountDueOriginal", amountDueOriginal);
                    jArr.put(jobj);

                    /*
                     * Method is used to save linking informatio of Advance
                     * Received Payment
                     *
                     * when linking with Debit Note
                     */
                    saveLinkingInformationOfPaymentWithSalesInvoice(gr, payment, payment.getReceiptNumber());
                }
            }
            HashMap<String, Object> paymenthm = new HashMap<String, Object>();
            HashMap<String, Object> linkDetails = saveLinkedReceiptDetails(paramJobj, payment, company, jArr, greceipt, Constants.AdvancePayment, counterMap);
            paymenthm.put("receiptid", payment.getID());
            paymenthm.put("linkDetails", linkDetails.get("linkDetailPayment"));
            JENumber= linkDetails.containsKey("JournalEntries")&& !StringUtil.isNullOrEmpty((String)linkDetails.get("JournalEntries"))?(String) linkDetails.get("JournalEntries"):"";
            paymenthm.put("customerId", customerId);
            accReceiptDAOobj.saveReceipt(paymenthm);
            
            Map<String, Object> auditRequestParams = new HashMap<String, Object>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
            
            auditTrailObj.insertAuditLog(AuditAction.LINKEDRECEIPT, "User " + paramJobj.optString(Constants.userfullname) + " has linked Receipt " + paymentno + " to Sales Invoice(s) " + linkedInvoicenos, auditRequestParams, linkedInvoiceids);
            result.add(JENumber);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }
    
     /*
        To implement below method we referred oldReceiptRowsAmount() which is used to calculate Forex Gains/Loss for invoices  
    */
//    @Transactional(propagation = Propagation.REQUIRED,rollbackFor={ServiceException.class})
    public double checkFxGainLossOnLinkInvoices(Invoice inv, double newInvoiceExchageRate, double paymentExchangeRate, double recinvamount, String paymentCurrency, String baseCurrency, String companyid) throws ServiceException {
        double amount = 0;
        HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
        GlobalParams.put(Constants.companyKey, companyid);
        GlobalParams.put(Constants.globalCurrencyKey, baseCurrency);
        double goodsReceiptExchangeRate = 0d;
        Date goodsReceiptCreationDate = null;
        boolean isopeningBalanceInvoice = inv.isIsOpeningBalenceInvoice();
        if (inv.isNormalInvoice()) {
            goodsReceiptExchangeRate = inv.getJournalEntry().getExternalCurrencyRate();
        } else {
            goodsReceiptExchangeRate = inv.getExchangeRateForOpeningTransaction();
            if (isopeningBalanceInvoice && inv.isConversionRateFromCurrencyToBase()) {//converting rate to Base to Other Currency Rate
                goodsReceiptExchangeRate = 1 / goodsReceiptExchangeRate;
            }
        }
        goodsReceiptCreationDate = inv.getCreationDate();

        boolean revalFlag = false;

        HashMap<String, Object> invoiceId = new HashMap<String, Object>();
        invoiceId.put("invoiceid", inv.getID());
        invoiceId.put(Constants.companyKey, companyid);
        KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
        RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
        if (history != null) {
            goodsReceiptExchangeRate = history.getEvalrate();
            revalFlag = true;
        }
        String currid = inv.getCurrency().getCurrencyID();
        KwlReturnObject bAmt = null;
        if (currid.equalsIgnoreCase(paymentCurrency)) {
            double paymentExternalCurrencyRate = paymentExchangeRate;
            if (goodsReceiptExchangeRate != paymentExternalCurrencyRate) {
                bAmt = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate, paymentExternalCurrencyRate);
            } else {
                bAmt = accCurrencyobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate);
            }
        } else {
            double paymentExternalCurrencyRate = paymentExchangeRate;
            if (goodsReceiptExchangeRate != paymentExternalCurrencyRate) {
                bAmt = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate, paymentExternalCurrencyRate);
            } else {
                bAmt = accCurrencyobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, paymentCurrency, goodsReceiptCreationDate, goodsReceiptExchangeRate);
            }
        }
        double oldrate = (Double) bAmt.getEntityList().get(0);
        double newrate = 0.0;
        double ratio = 0;
        if (newInvoiceExchageRate != oldrate && newInvoiceExchageRate != 0.0
                && Math.abs(newInvoiceExchageRate - oldrate) >= 0.000001) {
            newrate = newInvoiceExchageRate;
            ratio = oldrate - newrate;
            amount = (recinvamount - (recinvamount / newrate) * oldrate);
        }
        return amount;
    }
    
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor={ServiceException.class,SessionExpiredException.class,Exception.class,JSONException.class,AccountingException.class,ParseException.class})
     private HashMap<String ,Object> saveLinkedReceiptDetails(JSONObject paramJobj, Receipt receipt, Company company, JSONArray jArr, GoodsReceipt greceipt,int type,Map<String,Object> counterMap) throws JSONException, ServiceException, AccountingException, SessionExpiredException, ParseException {
        HashSet linkdetails = new HashSet();
        HashMap<String, Object> linkDetails = new HashMap<String, Object>();
        Date maxLinkingDate = null;
        String JENumbers="";
        String companyid=company.getCompanyID();
        String linkingdate = paramJobj.optString("linkingdate");
        DateFormat df = authHandler.getDateOnlyFormat();
        if (!StringUtil.isNullOrEmpty(linkingdate)) {
            maxLinkingDate = df.parse(linkingdate);
        }
        HashMap<String, Object> prefparams = new HashMap<>();
        prefparams.put("id", companyid);
        Object columnPref = kwlCommonTablesDAOObj.getRequestedObjectFields(IndiaComplianceCompanyPreferences.class, new String[]{"istaxonadvancereceipt"}, prefparams);
        boolean istaxonadvancereceipt = false;
        if (columnPref != null) {
            istaxonadvancereceipt = Boolean.parseBoolean(columnPref.toString());
        }
        KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), company.getCompanyID());
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0); 
        if (type==Constants.AdvancePayment) {
            String baseCurrency = paramJobj.optString(Constants.globalCurrencyKey);
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            LinkDetailReceipt rd = new LinkDetailReceipt();
            rd.setSrno(i + 1);
            rd.setID(StringUtil.generateUUID());
            double amountReceived = jobj.getDouble("enteramount");// amount in receipt currency
            double amountReceivedConverted = jobj.getDouble("enteramount"); // amount in invoice currency
            rd.setAmount(jobj.getDouble("enteramount"));
            rd.setCompany(company);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.optString("documentid"));
            Invoice invoice = (Invoice) result.getEntityList().get(0);
            rd.setInvoice((Invoice) result.getEntityList().get(0));
            double invoiceamountduebeforelink=invoice.getInvoiceAmountDueInBase();
            //JE For Invoice which is Linked to Invoice
            double exchangeRateforTransaction=jobj.optDouble("exchangeratefortransaction",1.0);
            if(invoice!=null){
                double finalAmountReval=ReevalJournalEntryForOpeningInvoice(paramJobj, invoice,amountReceived,exchangeRateforTransaction);
                if (finalAmountReval != 0) {
                    String basecurrency =paramJobj.optString(Constants.globalCurrencyKey);
                    /**
                     * added transactionID and transactionModuleID to Realised
                     * JE.
                     */
                    counterMap.put("transactionModuleid", invoice.isIsOpeningBalenceInvoice() ? Constants.Acc_opening_Sales_Invoice : Constants.Acc_Invoice_ModuleId);
                    counterMap.put("transactionId", invoice.getID());
                    String revaljeid = PostJEFORReevaluation(paramJobj, finalAmountReval, invoice.getCompany().getCompanyID(), preferences, basecurrency, null,counterMap);
                    rd.setRevalJeId(revaljeid);
                 }
            }
            //JE For Receipt which is of Opening Type
            if (receipt != null && (receipt.isIsOpeningBalenceReceipt() || (!receipt.getReceiptAdvanceDetails().isEmpty()))) {
                String basecurrency = paramJobj.optString(Constants.globalCurrencyKey);
                double finalAmountReval = ReevalJournalEntryForOpeningReceipt(paramJobj, receipt, amountReceived, exchangeRateforTransaction);
                if (finalAmountReval != 0) {
                    /**
                     * added transactionID and transactionModuleID to Realised
                     * JE.
                     */
                    counterMap.put("transactionModuleid", receipt.isIsOpeningBalenceReceipt() ? Constants.Acc_opening_Receipt : Constants.Acc_Receive_Payment_ModuleId);
                    counterMap.put("transactionId", receipt.getID());
                    String revaljeid = PostJEFORReevaluation(paramJobj, finalAmountReval, receipt.getCompany().getCompanyID(), preferences, basecurrency, null,counterMap);
                    rd.setRevalJeIdReceipt(revaljeid);
                }
            }
            if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(invoice.getCurrency().getCurrencyID()) && !invoice.getCurrency().getCurrencyID().equals(receipt.getCurrency().getCurrencyID())) {
                    rd.setExchangeRateForTransaction(exchangeRateforTransaction);
                    rd.setFromCurrency(invoice.getCurrency());
                    rd.setToCurrency(receipt.getCurrency());
                    // adjusted exchange rate used to handle case like ERP-34884
                    double adjustedRate = exchangeRateforTransaction;
                    if(jobj.optDouble("amountdue", 0) !=0 && jobj.optDouble("amountDueOriginal", 0)!=0){
                       adjustedRate = jobj.optDouble("amountdue", 0) / jobj.optDouble("amountDueOriginal", 0); 
                    }
                    amountReceivedConverted = amountReceived / adjustedRate;
                    amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                    rd.setAmountInInvoiceCurrency(amountReceivedConverted);
                }else{
                    rd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
                    rd.setFromCurrency(invoice.getCurrency());
                    rd.setToCurrency(receipt.getCurrency());
                    amountReceivedConverted = authHandler.round(amountReceived, companyid);
                    rd.setAmountInInvoiceCurrency(amountReceivedConverted);
                }
            if (greceipt != null) {
                rd.setGoodsReceipt(greceipt);
            }
            rd.setReceipt(receipt);
            if (jobj.has("rowjedid")) {
                rd.setROWJEDID(jobj.getString("rowjedid"));
            }          
            Date linkingDate = new Date();
            Date invoiceDate = invoice.getCreationDate();
            Date receiptDate = receipt.getCreationDate();
            
            Date maxDate = null;
            if (maxLinkingDate != null) {
                maxDate = maxLinkingDate;
            } else {
                List<Date> datelist = new ArrayList<Date>();
                datelist.add(linkingDate);
                datelist.add(invoiceDate);
                datelist.add(receiptDate);
                Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                maxDate = datelist.get(datelist.size() - 1);
            }
            
            rd.setReceiptLinkDate(maxDate);
            HashMap<String, JSONArray> jcustomarrayMap = receipt.getJcustomarrayMap();
            JSONArray jcustomarray = jcustomarrayMap.get(rd.getROWJEDID());
            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
            customrequestParams.put("customarray", jcustomarray);
            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
            customrequestParams.put("modulerecid", rd.getROWJEDID());
            customrequestParams.put("recdetailId", rd.getID());
            customrequestParams.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
            customrequestParams.put(Constants.companyKey, companyid);
            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), rd.getROWJEDID());
                AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), rd.getROWJEDID());
                JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
            }
            double amountReceivedConvertedInBaseCurrency = 0d;
            HashMap<String, Object> requestParams = new HashMap();
            requestParams.put(Constants.companyid, company.getCompanyID());
            requestParams.put(Constants.globalCurrencyKey, company.getCurrency().getCurrencyID());
            double externalCurrencyRate = 1d;
            boolean isopeningBalanceRCP = receipt.isIsOpeningBalenceReceipt();
            Date rcpCreationDate = null;
            rcpCreationDate = receipt.getCreationDate();
            if (isopeningBalanceRCP) {
                externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
            } else {
                externalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
            }
            String fromcurrencyid = receipt.getCurrency().getCurrencyID();
            KwlReturnObject bAmt = null;
            if (isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountReceived, fromcurrencyid, rcpCreationDate, externalCurrencyRate);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountReceived, fromcurrencyid, rcpCreationDate, externalCurrencyRate);
            }
            amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
            amountReceivedConvertedInBaseCurrency = authHandler.round(amountReceivedConvertedInBaseCurrency, companyid);
            /*
                Calculate Invoice amountdue on current amount received and exchangerate
            */
//            double invoiceExternalCurrencyRate = 1d;
//            boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
//            Date invoiceCreationDate = null;
//            invoiceCreationDate = invoice.getCreationDate();
//            if (isopeningBalanceInvoice) {
//                invoiceExternalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
//            } else {
//                invoiceExternalCurrencyRate = invoice.getJournalEntry().getExternalCurrencyRate();
//            }
//            String invoicefromcurrencyid = invoice.getCurrency().getCurrencyID();
//            KwlReturnObject invoicebAmt = null;
//            if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
//                invoicebAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountReceivedConverted, invoicefromcurrencyid, invoiceCreationDate, invoiceExternalCurrencyRate);
//            } else {
//                invoicebAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountReceivedConverted, invoicefromcurrencyid, invoiceCreationDate, invoiceExternalCurrencyRate);
//            }
//            double invoiceamountReceivedConvertedInBaseCurrency = (Double) invoicebAmt.getEntityList().get(0);
//            invoiceamountReceivedConvertedInBaseCurrency = authHandler.round(invoiceamountReceivedConvertedInBaseCurrency, companyid);
            KwlReturnObject invoiceResult = updateInvoiceAmountDueAndReturnResult(invoice, receipt, company, amountReceivedConverted);
            if (invoiceResult != null && invoiceResult.getEntityList() != null && invoiceResult.getEntityList().size() > 0) {
                Invoice inv = (Invoice) invoiceResult.getEntityList().get(0);
                if (inv.isIsOpeningBalenceInvoice() && inv.getOpeningBalanceAmountDue() == 0) {
                    try {
                        HashMap<String, Object> dataMap = new HashMap<String, Object>();
                        dataMap.put("amountduedate", maxDate);
                        accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                    } catch (Exception ex) {
                        System.out.println("" + ex.getMessage());
                    }
                } else if (inv.getInvoiceamountdue() == 0) {
                    try {
                        HashMap<String, Object> dataMap = new HashMap<String, Object>();
                        dataMap.put("amountduedate",  maxDate);
                        accInvoiceDAOObj.saveInvoiceAmountDueZeroDate(inv, dataMap);
                    } catch (Exception ex) {
                        System.out.println("" + ex.getMessage());
                    }
                }
            }
            
                            
            /*
                Start gains/loss calculation
                Calculate Gains/Loss if Invoice exchange rate changed at the time of linking with advance payment
            */
            
            if(isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()){
                externalCurrencyRate=1/externalCurrencyRate;
            }
            if (preferences.getForeignexchange() == null) {
                throw new AccountingException(messageSource.getMessage("acc.common.forex", null, Locale.forLanguageTag(paramJobj.getString("language"))));
            }
            double amountDiff = checkFxGainLossOnLinkInvoices(invoice, Double.parseDouble(jobj.optString("exchangeratefortransaction","1")),externalCurrencyRate, amountReceived, receipt.getCurrency().getCurrencyID(), baseCurrency, company.getCompanyID());
            if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                boolean rateDecreased = false;
                if (amountDiff < 0) {
                    rateDecreased = true;
                }
                JournalEntry journalEntry = null;
                Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);
                int counter = 0;
                if (counterMap.containsKey("counter")) {
                    counter = (Integer) counterMap.get("counter");
                }
                String jeentryNumber = null;
                String jeDatePrefix = "";
                String jeDateAfterPrefix = "";
                String jeDateSuffix = "";
                boolean jeautogenflag = false;
                String jeSeqFormatId = "";
                Date entryDate = null;
                if (maxLinkingDate != null) {
                    entryDate = new Date(maxLinkingDate.getTime());
                } else {
                    entryDate = new Date();
                }
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put(Constants.companyKey, companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                    String nextAutoNoTemp = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    int sequence = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));
                    jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
            
                    
                    sequence = sequence + counter;
                    String number = "" + sequence;
                    String action = "" + (sequence - counter);
                    nextAutoNoTemp.replaceAll(action, number);
                    jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;

                    jeDataMap.put("entrynumber", jeentryNumber);
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, number);
                    jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                    counter++;
                    counterMap.put("counter", counter);
                }
                jeDataMap.put("entrydate",entryDate); // ERP-8987    SDP-2944 FW: Linking Date for JE
                jeDataMap.put(Constants.companyKey, companyid);
                jeDataMap.put("memo", "Exchange Gains/Loss posted against Advance Receipt '"+receipt.getReceiptNumber()+"' linked to Invoice '"+invoice.getInvoiceNumber()+"'");
                jeDataMap.put(Constants.currencyKey, receipt.getCurrency().getCurrencyID());
                jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                jeDataMap.put("isexchangegainslossje", true);
                jeDataMap.put("transactionId",receipt.getID());
                jeDataMap.put("transactionModuleid", Constants.Acc_Receive_Payment_ModuleId);
                journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
                accJournalEntryobj.saveJournalEntryByObject(journalEntry);

                boolean isDebit = rateDecreased ? true : false;
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", 1);
                jedjson.put(Constants.companyKey, companyid);
                jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                jedjson.put("accountid", preferences.getForeignexchange().getID());
                jedjson.put("debit", isDebit);
                jedjson.put("jeid", journalEntry.getID());
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                Set<JournalEntryDetail> detail = new HashSet();
                detail.add(jed);

                jedjson = new JSONObject();
                jedjson.put("srno", 2);
                jedjson.put(Constants.companyKey, companyid);
                jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                jedjson.put("accountid", invoice.getAccount().getID());
                jedjson.put("debit", !isDebit);
                jedjson.put("jeid", journalEntry.getID());
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                detail.add(jed);
                journalEntry.setDetails(detail);
                accJournalEntryobj.saveJournalEntryDetailsSet(detail);

                rd.setLinkedGainLossJE(journalEntry.getID());
            }

            /**
             * Post JE for Linking case for India Invoice
             */
            /**
             * Need to calculate amount on which adjustment JE need to be post
             * Formula is : 1) minus invoice amount excluding tax and its amount
             * due i.e. invoiceadjustlimitamt 2) Take link Amount i.e.
             * amountReceivedConverted 3) If invoiceadjustlimitamt<=0 then no
             * need to post JE 4) If invoiceadjustlimitamt <
             * amountReceivedConverted then calculate amount for JE using
             * invoiceadjustlimitamt 5) If invoiceadjustlimitamt >
             * amountReceivedConverted then calculate amount for JE using
             * amountReceivedConverted
             */
            double invoiceamtinbase = invoice.getExcludingGstAmountInBase();
            double invoiceadjustedamountbeforelink = invoice.getInvoiceamountinbase() - invoiceamountduebeforelink;
            double invoiceadjustlimitamt = invoiceamtinbase - invoiceadjustedamountbeforelink;
            double amountforadjustment = amountReceivedConverted;
            if (invoiceadjustlimitamt <= 0) {
                /**
                 * No Need to post JE
                 */
                amountforadjustment = 0d;
            } else if (invoiceadjustlimitamt < amountReceivedConverted) {
                amountforadjustment = invoiceadjustlimitamt;
            } else {
                amountforadjustment = amountReceivedConverted;
            }
            Set<ReceiptAdvanceDetail> advanceDetails = receipt.getReceiptAdvanceDetails();
            List<ReceiptAdvanceDetailTermMap> advDetailTermMaps = new LinkedList<>();
            if (company.getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                if (!advanceDetails.isEmpty()) {
                    for (ReceiptAdvanceDetail advDetail : advanceDetails) {
                        String advId = advDetail.getId();
                        /**
                         * Get term details from Advance
                         */
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("adId", advId);
                        KwlReturnObject kwlObject = accReceiptDAOobj.getAdvanceDetailsTerm(jSONObject);
                        advDetailTermMaps = kwlObject.getEntityList();
                    }
                }
            }
            if (amountforadjustment>0 && istaxonadvancereceipt && company.getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id) && receipt!=null && receipt.getReceiptAdvanceDetails()!=null 
                    && !invoice.isRcmapplicable() && !advDetailTermMaps.isEmpty()) {
                JournalEntry journalEntry = null;
                Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);
                int counter = 0;
                if (counterMap.containsKey("counter")) {
                    counter = (Integer) counterMap.get("counter");
                }
                String jeentryNumber = null;
                String jeDatePrefix = "";
                String jeDateAfterPrefix = "";
                String jeDateSuffix = "";
                boolean jeautogenflag = false;
                String jeSeqFormatId = "";
                Date entryDate = null;
                if (maxLinkingDate != null) {
                    entryDate = new Date(maxLinkingDate.getTime());
                } else {
                    entryDate = new Date();
                }
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put(Constants.companyKey, companyid);
                    JEFormatParams.put("isdefaultFormat", true);
                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                    String nextAutoNoTemp = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    int sequence = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                    jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    sequence = sequence + counter;
                    String number = "" + sequence;
                    String action = "" + (sequence - counter);
                    nextAutoNoTemp.replaceAll(action, number);
                    jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;
                    jeDataMap.put("entrynumber", jeentryNumber);
                    if (StringUtil.isNullOrEmpty(JENumbers)) {
                        JENumbers = jeentryNumber;
                    } else {
                        JENumbers = jeentryNumber != "" ? JENumbers.concat("," + jeentryNumber) : "";
                    }
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, number);
                    jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                    counter++;
                    counterMap.put("counter", counter);
                }
                jeDataMap.put("entrydate", entryDate); // ERP-8987    SDP-2944 FW: Linking Date for JE
                jeDataMap.put(Constants.companyKey, companyid);
                jeDataMap.put("memo", "GST Adjustment posted against Advance Receipt '" + receipt.getReceiptNumber() + "' linked to Invoice '" + invoice.getInvoiceNumber() + "'");
                jeDataMap.put(Constants.currencyKey, receipt.getCurrency().getCurrencyID());
                jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                jeDataMap.put("transactionId", receipt.getID());
                jeDataMap.put("transactionModuleid", Constants.Acc_Receive_Payment_ModuleId);
                journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
                accJournalEntryobj.saveJournalEntryByObject(journalEntry);

                /**
                 * need to get receipt details and its GST details
                 */
                if (!advanceDetails.isEmpty()) {
                    Set<JournalEntryDetail> detail = new HashSet();
                    for (ReceiptAdvanceDetail advanceDetail : advanceDetails) {
                        String adId = advanceDetail.getId();
                        /**
                         * Get term details from Advance
                         */
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("adId", adId);
                        KwlReturnObject kro = accReceiptDAOobj.getAdvanceDetailsTerm(jSONObject);
                        List<ReceiptAdvanceDetailTermMap> advanceDetailTermMaps = kro.getEntityList();
                        int srcount = 1;
                        for (ReceiptAdvanceDetailTermMap receiptAdvanceDetailTermMap : advanceDetailTermMaps) {
                            double termamount = 0d;//receiptAdvanceDetailTermMap.getTermamount();
                            double percentage=receiptAdvanceDetailTermMap.getPercentage();
                            String gstAdvpayableAcc = receiptAdvanceDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPayableAccount().getID();
                            String gstAcc = receiptAdvanceDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID();
                            /**
                             * Calculate Amount Formula is written based on
                             * calculate gst amount by applying percentage on
                             * amount excluding tax amount Example : 
                             * Advance amount = 1000+50 =1050
                             * Invoice Amount = 500+25=525
                             * Linking JE amount = Calculate GST amount on 500 not on 525
                             */
                            double amount = amountforadjustment * percentage / 100;
                            termamount = authHandler.round(amount, companyid);
                            JSONObject jedjson = new JSONObject();
                            jedjson.put("srno", srcount);
                            jedjson.put(Constants.companyKey, companyid);
                            jedjson.put("amount", termamount);
                            jedjson.put("accountid", gstAcc);
                            jedjson.put("debit", true);
                            jedjson.put("jeid", journalEntry.getID());
                            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            detail.add(jed);
                            srcount++;

                            jedjson = new JSONObject();
                            jedjson.put("srno", srcount);
                            jedjson.put(Constants.companyKey, companyid);
                            jedjson.put("amount", termamount);
                            jedjson.put("accountid", gstAdvpayableAcc);
                            jedjson.put("debit", false);
                            jedjson.put("jeid", journalEntry.getID());
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            detail.add(jed);
                            srcount++;
                        }
                    }
                    journalEntry.setDetails(detail);
                    accJournalEntryobj.saveJournalEntryDetailsSet(detail);
                    rd.setLinkedGSTJE(journalEntry.getID());
                }
            }
            // End Gains/Loss Calculation

            for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
                advanceDetail.setAmountDue(advanceDetail.getAmountDue() - amountReceived);
            }
            if (receipt != null && isopeningBalanceRCP) {
                double receiptAmountDue = receipt.getOpeningBalanceAmountDue();
                    receiptAmountDue -= amountReceived;
                    receipt.setOpeningBalanceAmountDue(receiptAmountDue);
                    receipt.setOpeningBalanceBaseAmountDue(receipt.getOpeningBalanceBaseAmountDue() - amountReceivedConvertedInBaseCurrency);
                    
                    HashMap<String, Object> receipthm = new HashMap<String, Object>();
                    receipthm.put("receiptid", receipt.getID());
                    receipthm.put("openingBalanceAmountDue", receipt.getOpeningBalanceAmountDue());
                    receipthm.put(Constants.openingBalanceBaseAmountDue, receipt.getOpeningBalanceBaseAmountDue());
                    accReceiptDAOobj.saveReceipt(receipthm);
                }
                linkdetails.add(rd);
                linkDetails.put("linkDetailPayment", linkdetails);

            }
            linkDetails.put("JournalEntries", JENumbers);
        }
        return linkDetails;
    }
    
//    @Transactional(propagation = Propagation.REQUIRED,rollbackFor={ServiceException.class,AccountingException.class})
     public double ReevalJournalEntryForOpeningInvoice(JSONObject paramJobj, Invoice invoice, double linkInvoiceAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {
        double finalAmountReval = 0;
        try {
            String basecurrency = paramJobj.optString(Constants.globalCurrencyKey);
            double ratio = 0;
            double amountReval = 0;
            Date tranDate = null;
            double exchangeRate = 0.0;
            double exchangeRateReval = 0.0;
            double amountdue = linkInvoiceAmount;
            Map<String, Object> GlobalParams = new HashMap<>();
            GlobalParams.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
            GlobalParams.put(Constants.globalCurrencyKey, basecurrency);
            GlobalParams.put("dateformat", authHandler.getDateOnlyFormat());
            Date creationDate = invoice.getCreationDate();
            boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
            tranDate = invoice.getCreationDate();
            if (!invoice.isNormalInvoice()) {
                exchangeRate = invoice.getExchangeRateForOpeningTransaction();
                exchangeRateReval = exchangeRate;
            } else {
                exchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
                exchangeRateReval = exchangeRate;
            }
            Map<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", invoice.getID());
            invoiceId.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
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
            if (invoice.getCurrency() != null) {
                currid = invoice.getCurrency().getCurrencyID();
            }
            KwlReturnObject bAmt = null;
            if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            }

            double oldrate = (Double) bAmt.getEntityList().get(0);
            if (revalueationHistory == null && isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            }

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
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }  
   
//    @Transactional(propagation = Propagation.REQUIRED,rollbackFor={ServiceException.class,SessionExpiredException.class,Exception.class,JSONException.class,AccountingException.class,ParseException.class})  
     public double ReevalJournalEntryForOpeningDebiteNote(JSONObject paramJobj, DebitNote debitNote, double linkInvoiceAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {
        double finalAmountReval = 0;
        try {
            String basecurrency = paramJobj.optString(Constants.globalCurrencyKey);
            double ratio = 0;
            double amountReval = 0;
            Date tranDate = null;
            double exchangeRate = 0.0;
            double exchangeRateReval = 0.0;
            double amountdue = linkInvoiceAmount;
            Map<String, Object> GlobalParams = new HashMap<>();
            GlobalParams.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
            GlobalParams.put(Constants.globalCurrencyKey, basecurrency);
            GlobalParams.put("dateformat", authHandler.getDateOnlyFormat());
            Date creationDate = debitNote.getCreationDate();
            boolean isopeningBalanceInvoice = debitNote.isIsOpeningBalenceDN();
            tranDate = debitNote.getCreationDate();
            if (!debitNote.isNormalDN()) {
                exchangeRate = debitNote.getExchangeRateForOpeningTransaction();
                exchangeRateReval = exchangeRate;
            } else {
                exchangeRate = debitNote.getJournalEntry().getExternalCurrencyRate();
                exchangeRateReval = exchangeRate;
            }
            HashMap<String, Object> invoiceId = new HashMap<>();
            invoiceId.put("invoiceid", debitNote.getID());
            invoiceId.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
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
            if (debitNote.getCurrency() != null) {
                currid = debitNote.getCurrency().getCurrencyID();
            }
            KwlReturnObject bAmt = null;
            if (isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
            }

            double oldrate = (Double) bAmt.getEntityList().get(0);
            if (revalueationHistory == null && isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
            }

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
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return finalAmountReval;
    }
    
//     @Transactional(propagation = Propagation.REQUIRED,rollbackFor={ServiceException.class}) 
     public double checkFxGainLossOnLinkdebitNote(DebitNote dn, double newInvoiceExchageRate, double paymentExchangeRate, double receivedDnAmount, String paymentCurrency, String baseCurrency, String companyid) throws ServiceException {
        double amount = 0;
        HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
        GlobalParams.put(Constants.companyKey, companyid);
        GlobalParams.put(Constants.globalCurrencyKey, baseCurrency);
        double dnExchangeRate = 0d;
        Date dnCreationDate = null;
        boolean isopeningBalanceDn = dn.isIsOpeningBalenceDN();
        if (dn.isNormalDN()) {
            dnExchangeRate = dn.getJournalEntry().getExternalCurrencyRate();
        } else {
            dnExchangeRate = dn.getExchangeRateForOpeningTransaction();
             if(isopeningBalanceDn && dn.isConversionRateFromCurrencyToBase()){//converting rate to Base to Other Currency Rate
                dnExchangeRate=1/dnExchangeRate;
            }
        }
        dnCreationDate = dn.getCreationDate();
        
        Map<String, Object> documentMap = new HashMap<>();
        documentMap.put("invoiceid", dn.getID());
        documentMap.put(Constants.companyKey, companyid);
        KwlReturnObject result = accJournalEntryobj.getRevalInvoiceId(documentMap);
        RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
        if (history != null) {
            dnExchangeRate = history.getEvalrate();
        }
        String currid = dn.getCurrency().getCurrencyID();
        KwlReturnObject bAmt = null;
        if (currid.equalsIgnoreCase(paymentCurrency)) {
                double paymentExternalCurrencyRate = paymentExchangeRate;
                if (dnExchangeRate != paymentExternalCurrencyRate) {
                    bAmt = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, paymentCurrency, dnCreationDate, dnExchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, paymentCurrency, dnCreationDate, dnExchangeRate);
                }
        } else {

                double paymentExternalCurrencyRate = paymentExchangeRate;
                if (dnExchangeRate != paymentExternalCurrencyRate) {
                    bAmt = accCurrencyobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, paymentCurrency, dnCreationDate, dnExchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, paymentCurrency, dnCreationDate, dnExchangeRate);
                }
        }
        double oldrate = (Double) bAmt.getEntityList().get(0);
        double newrate = 0.0;
        if (newInvoiceExchageRate != oldrate && newInvoiceExchageRate != 0.0 && Math.abs(newInvoiceExchageRate - oldrate) >= 0.000001) {
            newrate = newInvoiceExchageRate;
            amount = (receivedDnAmount - (receivedDnAmount / newrate) * oldrate);
        }
        return amount;
    }
     
  @Override   
    public KwlReturnObject checkTransactionsForDiscountOnPaymentTerms(JSONObject paramJobj) throws ServiceException {
        KwlReturnObject result = accReceiptDAOobj.checkTransactionsForDiscountOnPaymentTerms(paramJobj);
        return result;
    }
    
    public boolean isReceiptDateAfterTheDocumentDate(JSONObject params) throws ServiceException, SessionExpiredException {
        boolean returnValue = false;
        DateFormat sdf1=authHandler.getDateOnlyFormat();
        try {
            if (!StringUtil.isNullOrEmpty(params.optString("documentDate", "")) && !StringUtil.isNullOrEmpty(params.optString("receiptDate", ""))) {
                Date documentDate =  sdf1.parse(params.optString("documentDate"));
                Date receiptDate = sdf1.parse(params.optString("receiptDate"));
                if (!documentDate.after(receiptDate)) {
                    returnValue = true;
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("isReceiptDateAfterTheDocumentDate : " + ex.getMessage(), ex);
        }
        return returnValue;
    }
 
 @Override   
   public JSONObject deleteReceiptMerged(JSONObject paramJObj) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("R_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
           String unableToDeleteReceiptNos = deleteReceiptTemporary(paramJObj);
            txnManager.commit(status);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(unableToDeleteReceiptNos)) {
                msg = messageSource.getMessage("acc.receipt.del", null, Locale.forLanguageTag(paramJObj.getString(Constants.language)));   //"Receipt(s) has been deleted successfully";
            } else {
                msg = messageSource.getMessage("acc.ob.RPExcept", null, Locale.forLanguageTag(paramJObj.optString(Constants.language)))+" "+unableToDeleteReceiptNos.substring(0, unableToDeleteReceiptNos.length()-2)+" "+messageSource.getMessage("acc.field.deletedsuccessfully", null, Locale.forLanguageTag(paramJObj.optString(Constants.language)))+ " " +messageSource.getMessage("acc.field.belongstolockingperiod", null, Locale.forLanguageTag(paramJObj.optString(Constants.language)));   
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
 
 @Override
  public String deleteReceiptTemporary(JSONObject paramJObj) throws AccountingException, SessionExpiredException, ServiceException,JSONException {
      String unableToDeleteReceiptNos = "";
      try {
            String receiptsJson = paramJObj.optString(Constants.data,null);
            String companyid = paramJObj.optString(Constants.companyKey);
            JSONArray jArr = new JSONArray(receiptsJson);
            Map<String, Object> requestMap = AccountingManager.getGlobalParamsJson(paramJObj);
            String receiptid = "", jeid, receiptno = "",entryno="";
            KwlReturnObject result;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                receiptid = StringUtil.DecodeText(jobj.optString(Constants.billid));
                jeid = StringUtil.DecodeText(jobj.optString("journalentryid"));
                receiptno = jobj.getString("billno");
                entryno = jobj.optString("entryno", "");
                String roundingJENo = "";
                String roundingJEIds = "";
                {

                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                    Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                    
                    /*
                     * Before deleting PaymentDetail and LinkDetailPayment
                     * Keeping id of Goodsrceipt utlized in Payment
                     */
                    Set<String> invoiceIDSet = new HashSet<>();
                    if (receipt != null && receipt.getApprovestatuslevel() == 11) {
                        for (ReceiptDetail rd : receipt.getRows()) {
                            if (rd.getInvoice() != null) {
                                invoiceIDSet.add(rd.getInvoice().getID());
                            }
                        }
                        for (LinkDetailReceipt rd : receipt.getLinkDetailReceipts()) {
                            if (rd.getInvoice() != null) {
                                invoiceIDSet.add(rd.getInvoice().getID());
                            }
                        }
                    }
                    if (receipt != null) {
                        if (receipt.getApprovestatuslevel() == Constants.APPROVED_STATUS_LEVEL) {
                            updateOpeningBalance(receipt, companyid);
                             if (receipt.getLinkDetailReceiptsToAdvancePayment() == null || receipt.getLinkDetailReceiptsToAdvancePayment().isEmpty()) {
                                JSONObject params = new JSONObject();
                                /**
                                 * Passing isToDeleteReceiptAdvanceDetails flag
                                  * as false as we don't have to delete entry
                                  * from receiptadvancedetail table while
                                  * deleting records temporary.ERP-39299.
                                 */
                                params.put("isToDeleteReceiptAdvanceDetails",false);
                                updateReceiptAdvancePaymentAmountDue(receipt, companyid,params);
                            }
                            updateReceiptLoanAmountDue(receipt, companyid, false);
                            updateDNAmountDueForReceipt(receipt, companyid);
                        }
                        /**
                         * Setting deleteflag = true of Unrealized JE of Advance
                         * Type Receipt being deleted.
                         */
                        if (receipt != null && receipt.getReceiptAdvanceDetails() != null && !receipt.getReceiptAdvanceDetails().isEmpty()) {
                            result = accJournalEntryobj.deleteJournalEntryReval(receipt.getID(), companyid);
                        }
                    }
                    if (receipt.getLinkDetailReceipts()!=null&&!receipt.getLinkDetailReceipts().isEmpty()) {
                        accReceiptDAOobj.deleteLinkReceiptsDetailsAndUpdateAmountDue(requestMap,receiptid,companyid,true);
                    }
                    if (receipt.getLinkDetailReceipts()!=null&&!receipt.getLinkDetailReceipts().isEmpty()) {
                        accReceiptDAOobj.deleteLinkReceiptsDetailsToDebitNoteAndUpdateAmountDue(receiptid,companyid,true);
                    }
                    /*
                     * Deleting Advance Payment entries for refund receipt
                     */
                    if (receipt.getLinkDetailReceiptsToAdvancePayment() != null && !receipt.getLinkDetailReceiptsToAdvancePayment().isEmpty()) {
                        Set<LinkDetailReceiptToAdvancePayment> linkedDetailReceiptList = receipt.getLinkDetailReceiptsToAdvancePayment();
                        for (LinkDetailReceiptToAdvancePayment ldprow : linkedDetailReceiptList) {
                            /*
                             * Code is commented beacause now we have deleted linked je while deleting receive payment as permanent 
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
                        
                        accReceiptDAOobj.deleteLinkReceiptsDetailsToAdvancePaymentAndUpdateAmountDue(receiptid, companyid,true);
                    }
                    
                    /*
                     * Deleting Bad Debt Invoices Recovery entries
                     */
                    HashMap<String, Object> requestParamsForMapping = new HashMap();
                    requestParamsForMapping.put(Constants.companyid, companyid);
                    requestParamsForMapping.put("receiptid", receiptid);
                    accInvoiceDAOObj.deleteBadDebtInvoiceMapping(requestParamsForMapping);
                    /*
                     *ERP-40733
                     *Throwing accounting exception for Account locking period and get unable to delete numbers
                     */
                    try {
                        result = accReceiptDAOobj.deleteReceiptEntry(receiptid, companyid);
                    } catch (AccountingException ex) {
                        unableToDeleteReceiptNos += receiptno + ", ";
                        continue;
                    }
                    result = accReceiptDAOobj.getJEFromReceipt(receiptid);
                    List list = result.getEntityList();
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        String jeId = (String) itr.next();
                        result = accJournalEntryobj.deleteJournalEntry(jeId, companyid);
                        //Delete entry from optimized table
                        accJournalEntryobj.deleteAccountJEs_optimized(jeId);
                    }
                    /*
                     * If invoice is link to receive payment then it going to delete as temporary then set isdelete flag true
                     * in journal entry table.
                     */
                    
                    if (receipt.getLinkDetailReceipts() != null && !receipt.getLinkDetailReceipts().isEmpty()) {
                        Set<LinkDetailReceipt> linkedDetailReceiptList = receipt.getLinkDetailReceipts();
                        for (LinkDetailReceipt ldprow : linkedDetailReceiptList) {
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
                            if (!StringUtil.isNullOrEmpty(ldprow.getRevalJeIdReceipt())) {
                                accJournalEntryobj.deleteJournalEntry(ldprow.getRevalJeIdReceipt(), companyid);
                            }
                        }
                    }
                    /*
                     * If Debit Note is link to receive payment then it going to delete as temporary then set isdelete flag true
                     * in journal entry table.
                     */

                    if (receipt.getLinkDetailReceiptsToDebitNote() != null && !receipt.getLinkDetailReceiptsToDebitNote().isEmpty()) {
                        Set<LinkDetailReceiptToDebitNote> linkedDetailReceiptList = receipt.getLinkDetailReceiptsToDebitNote();
                        for (LinkDetailReceiptToDebitNote ldprow : linkedDetailReceiptList) {
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
                            if (!StringUtil.isNullOrEmpty(ldprow.getRevalJeIdReceipt())) {
                                accJournalEntryobj.deleteJournalEntry(ldprow.getRevalJeIdReceipt(), companyid);
                            }
                        }
                    }
                    
                    /*
                     * For Refund
                     * If Make Payment is link to receive payment then it going to delete as temporary then set isdelete flag true
                     * in journal entry table.
                     */
                    
                    if (receipt.getLinkDetailReceiptsToAdvancePayment() != null && !receipt.getLinkDetailReceiptsToAdvancePayment().isEmpty()) {
                        Set<LinkDetailReceiptToAdvancePayment> linkedDetailReceiptList = receipt.getLinkDetailReceiptsToAdvancePayment();
                        for (LinkDetailReceiptToAdvancePayment ldprow : linkedDetailReceiptList) {
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
                            if (!StringUtil.isNullOrEmpty(ldprow.getRevalJeIdReceipt())) {
                                accJournalEntryobj.deleteJournalEntry(ldprow.getRevalJeIdReceipt(), companyid);
                            }
                        }
                    }

                    //Handle DisHonoured Cheque JEs
                    if (receipt.isIsDishonouredCheque()) {
                        result = accReceiptDAOobj.getDisHonouredJEFromReceipt(receiptid);
                        List dhlist = result.getEntityList();
                        Iterator dhitr = dhlist.iterator();
                        while (dhitr.hasNext()) {
                            String jeId = (String) dhitr.next();
                            result = accJournalEntryobj.deleteJournalEntry(jeId, companyid);
                            //Delete entry from optimized table
                            accJournalEntryobj.deleteAccountJEs_optimized(jeId);
                        }
                    }
                    // delete Bank charges JE
                    result = accReceiptDAOobj.getBankChargeJEFromReceipt(receiptid);
                    List list1 = result.getEntityList();
                    Iterator itr1 = list1.iterator();
                    while (itr1.hasNext()) {
                        String jeId = (String) itr1.next();
                        result = accJournalEntryobj.deleteJournalEntry(jeId, companyid);
                        //Delete entry from optimized table
                        accJournalEntryobj.deleteAccountJEs_optimized(jeId);
                    }

                    // delete Bank Interest JE
                    result = accReceiptDAOobj.getBankInterestJEFromReceipt(receiptid);
                    List list2 = result.getEntityList();
                    Iterator itr2 = list2.iterator();
                    while (itr2.hasNext()) {
                        String jeId = (String) itr2.next();
                        result = accJournalEntryobj.deleteJournalEntry(jeId, companyid);
                        //Delete entry from optimized table
                        accJournalEntryobj.deleteAccountJEs_optimized(jeId);
                    }
                    if (receipt != null && receipt.getRevalJeId() != null) {
                        result = accJournalEntryobj.deleteJournalEntry(receipt.getRevalJeId(), companyid);
                    }
                    
                    HashMap<String, Object> writeOffMap = new HashMap<String, Object>();
                    writeOffMap.put("receiptid", receiptid);
                    writeOffMap.put(Constants.companyKey, companyid);
                    result = accReceiptDAOobj.getReceiptWriteOffJEs(writeOffMap);
                    List<ReceiptWriteOff> writeOffList = result.getEntityList();
                    for (ReceiptWriteOff RWO : writeOffList) {
                        jeid = RWO.getJournalEntry().getID();
                        result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                    }

                    result = accReceiptDAOobj.getReverseReceiptWriteOffJEs(writeOffMap);
                    writeOffList = result.getEntityList();
                    for (ReceiptWriteOff RWO : writeOffList) {
                        jeid = RWO.getReversejournalEntry().getID();
                        result = accJournalEntryobj.deleteJournalEntry(jeid, companyid);
                    }
                     //Delete Rouding JEs if created against SI
                    if (!invoiceIDSet.isEmpty()) {
                        String invIDs = "";
                        for (String invID : invoiceIDSet) {
                             invIDs = invID+",";
                        }
                        if (!StringUtil.isNullOrEmpty(invIDs)) {
                            invIDs = invIDs.substring(0, invIDs.length() - 1);
                        }
                        KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(invIDs, companyid);
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
                requestParams.put("remoteAddress", paramJObj.optString(Constants.remoteIPAddress));
                requestParams.put("reqHeader", paramJObj.optString(Constants.reqHeader));
                /**
                 * Method to check the payment is Reconciled or not according to
                 * its JE id
                 */
                requestParams.put("jeid", jeid);
                requestParams.put(Constants.companyKey, companyid);
                boolean isReconciledFlag = accBankReconciliationObj.isRecordReconciled(requestParams);
                if (isReconciledFlag) {
                    throw new AccountingException(messageSource.getMessage("acc.reconcilation.Cannotdeletepayment", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))) + " " + "<b>" + receiptno + "</b>" + " " + messageSource.getMessage("acc.reconcilation.asitisreconciled", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))));
                }
                deleteBankReconcilation(requestParams);
                StringBuffer journalEntryMsg = new StringBuffer();
                if (!StringUtil.isNullOrEmpty(entryno)) {
                    journalEntryMsg.append(" along with the JE No. " + entryno);
                }
                if (paramJObj.optString("rejectReceipt",null) != null && !paramJObj.optString("rejectReceipt").equals("")) {
                    String userid = paramJObj.optString(Constants.useridKey);
                    String userName = paramJObj.optString(Constants.userfullname);
                    double amount = StringUtil.isNullOrEmpty(paramJObj.optString("amount",null)) ? 0 : Double.parseDouble(paramJObj.optString("amount","0"));
                    requestParams.clear();
                    requestParams.put(Constants.useridKey, userid);
                    requestParams.put("userName", userName);
                    requestParams.put("amount", amount);
                    requestParams.put(Constants.companyKey, companyid);
                    boolean isRejected = rejectPendingReceivePayment(requestParams, jArr);
                    paramJObj.put("isRejected", isRejected);
                } else {
                    Map<String, Object> auditParamsMap = new HashMap();
                    auditParamsMap.put(Constants.companyKey, paramJObj.optString(Constants.companyKey));
                    auditParamsMap.put(Constants.useridKey, paramJObj.optString(Constants.useridKey));
                    auditParamsMap.put(Constants.remoteIPAddress, paramJObj.optString(Constants.remoteIPAddress));
                    auditParamsMap.put(Constants.reqHeader, paramJObj.optString(Constants.reqHeader));
                    auditTrailObj.insertAuditLog(AuditAction.RECEIPT_DELETED, "User " + paramJObj.optString(Constants.userfullname) + " has deleted a Receipt " + receiptno + journalEntryMsg.toString(), auditParamsMap, receiptid);
                     if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                        auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_DELETED, "User " + paramJObj.optString(Constants.userfullname)+ " has deleted a Receipt " + receiptno + ". So Rounding JE No. " + roundingJENo + " deleted.", auditParamsMap, roundingJEIds);
                    }
                }
            }
        }catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))));
        }
       return unableToDeleteReceiptNos ;
    }  
  
}
