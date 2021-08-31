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
package com.krawler.spring.accounting.reports;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountController;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.account.accAccountHandler;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptCMN;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.invoice.accInvoiceControllerCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryController;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import static com.krawler.spring.accounting.account.accAccountHandler.getTotalOpeningBalance;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.creditnote.accCreditNoteController;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteController;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.invoice.*;
import com.krawler.spring.accounting.receipt.accReceiptController;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentController;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import java.text.DecimalFormat;
import java.text.ParseException;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.krawler.spring.common.kwlCommonTablesDAO;
import org.apache.commons.lang.time.DateUtils;
/**
 *
 * @author pandurang
 */
public class accReevaluationReportController extends MultiActionController implements MessageSourceAware{
      
    private HibernateTransactionManager txnManager;
    private accReceiptDAO accReceiptDao;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accInvoiceCMN accInvoiceCommon;
    private accJournalEntryDAO accJournalEntryobj;
    private accAccountDAO accAccountDAOobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private accGoodsReceiptDAO accGoodsReceiptDAOObj;
    private accGoodsReceiptCMN accGoodsReceiptCommon;
    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private exportMPXDAOImpl exportDaoObj;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private accDebitNoteDAO accDebitNoteobj;
    private accVendorPaymentDAO accVendorPaymentobj;
    private fieldDataManager fieldDataManagercntrl;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
   
    public kwlCommonTablesDAO getKwlCommonTablesDAOObj() {
        return kwlCommonTablesDAOObj;
    }
    
    public void setKwlCommonTablesDAOObj(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }
    
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }   
    public void setaccReceiptDAO(accReceiptDAO accReceiptDao) {
		this.accReceiptDao = accReceiptDao;
    }
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
     public void setAccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptDAOObj){
        this.accGoodsReceiptDAOObj = accGoodsReceiptDAOObj;
    }
    public void setaccGoodsReceiptCMN(accGoodsReceiptCMN accGoodsReceiptCommon) {
        this.accGoodsReceiptCommon = accGoodsReceiptCommon;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    @Override
    public void setMessageSource(MessageSource ms) {
            this.messageSource=ms;
    }
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj){
        this.auditTrailObj = auditTrailDAOObj;
    }  
    
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    
    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }
    
    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }
    
    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }
    
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public ModelAndView getAccountsForRevaluation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("JE_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status = txnManager.getTransaction(def);
        try {
            jobj= getAccountForRevaluationJSON(request, false);
//            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
//            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
//            txnManager.rollback(status);
            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    @Transactional(propagation = Propagation.REQUIRED)
    private JSONObject getAccountForRevaluationJSON(HttpServletRequest request, boolean isExport) throws SessionExpiredException, JSONException, ServiceException, AccountingException, ParseException {
        JSONObject jobj = new JSONObject();
        boolean noactivity = false;
        double endingBalanceSummary = 0;
        boolean accMapFlag = false;
        request.setAttribute("isExport", isExport);
        HashMap<String, Object> requestParams = accAccountHandler.getRequestMap(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        requestParams.put("companyid", companyid);
        String startdate = request.getParameter("startdate");
        String enddate = request.getParameter("enddate");
        if (requestParams.containsKey("acctypes") && requestParams.get("acctypes") != null && StringUtil.equal(requestParams.get("acctypes").toString(), "3")) {
            noactivity = true;
        }
        accMapFlag = request.getParameter("accMapFlag") != null ? Boolean.parseBoolean(request.getParameter("accMapFlag").toString()) : false;
        if (accMapFlag) {
            requestParams.put(Constants.companyKey, request.getParameter("childcompanyid"));
        }
        String tocurrencyid = "";
        String revalCurrencyRate = "";
        int exchangeRateType = 0;//0 = Forign exchange 1=Base Rate
        CompanyAccountPreferences preferences;
        Map<String, Object> requestParamsPref = new HashMap<String, Object>();
        requestParamsPref.put("id", companyid);
        KwlReturnObject resultComp = accCompanyPreferencesObj.getCompanyPreferences(requestParamsPref);
        preferences = (CompanyAccountPreferences) resultComp.getEntityList().get(0);
        if (preferences.getUnrealisedgainloss() == null) {
            throw new AccountingException("No Unrealised Gain/Loss account found.");
        }
        boolean expanderRequest = Boolean.parseBoolean(request.getParameter("expanderrequest"));
        int accTypeId = Integer.parseInt(request.getParameter("accTypeId"));
        requestParams.put("ignore", null);
        List list = null;
        KwlReturnObject result = null;
        if (accTypeId == Constants.VENDOR || accTypeId == Constants.CUSTOMER) {
            if (accTypeId == Constants.VENDOR) {
                result = accAccountDAOobj.getVendorForCombo(requestParams);
                list = result.getEntityList();

            } else if (accTypeId == Constants.CUSTOMER) {
                result = accAccountDAOobj.getCustomerForCombo(requestParams);
                list = result.getEntityList();

            }

            if (!expanderRequest) {
                jobj = getAccountJsonForRevaluation(request, list, accCurrencyDAOobj, noactivity, startdate, enddate, tocurrencyid, revalCurrencyRate, preferences);
            } else {
                jobj = getAccountInvoicesJsonForRevaluation(request, accCurrencyDAOobj, noactivity);//, tocurrencyid, revalCurrencyRate
            }

        }

        if (accTypeId == Group.ACCOUNTTYPE_BANK || accTypeId == Constants.CashInHandAccountTye) {
            String revaluateJson = request.getParameter("reevalueData");
            if (!StringUtil.isNullOrEmpty(revaluateJson)) {
                JSONArray jArr = new JSONArray();
                 String revalid = UUID.randomUUID().toString();
                JSONArray jArrForCurrency = new JSONArray(revaluateJson);
                for (int i = 0; i < jArrForCurrency.length(); i++) {
                    tocurrencyid = jArrForCurrency.getJSONObject(i).get("tocurrencyid").toString();
                    revalCurrencyRate = jArrForCurrency.getJSONObject(i).get("newexchangerate").toString();
                    exchangeRateType = Integer.parseInt(jArrForCurrency.getJSONObject(i).get("exchangeratetype").toString());
                    if (exchangeRateType == 1) {
                        revalCurrencyRate = Double.toString(1 / (Double.parseDouble(revalCurrencyRate)));
                    }

                    if (accTypeId == Constants.CashInHandAccountTye) {//for Cash in hand type accounts
                        boolean ignoreGLAccounts = true;
                        boolean ignoreGSTAccounts = true;
                        boolean ignorecustomers = true;
                        boolean ignoreBankAccounts = true;
                        boolean ignorevendors = true;
                        requestParams.put("ignoreBankAccounts", ignoreBankAccounts);
                        requestParams.put("ignoreGLAccounts", ignoreGLAccounts);
                        requestParams.put("ignoreGSTAccounts", ignoreGSTAccounts);
                        requestParams.put("ignorecustomers", ignorecustomers);
                        requestParams.put("ignorevendors", ignorevendors);
                        requestParams.put("transactionCurrency", tocurrencyid);
                        result = accAccountDAOobj.getAccountsForCombo(requestParams);
                        list = result.getEntityList();
                    } else {//For Bank Accounts
                        boolean ignoreGLAccounts = true;
                        boolean ignoreGSTAccounts = true;
                        boolean ignoreCashAccounts = true;
                        boolean ignorecustomers = true;
                        boolean ignorevendors = true;
                        requestParams.put("ignoreGLAccounts", ignoreGLAccounts);
                        requestParams.put("ignoreGSTAccounts", ignoreGSTAccounts);
                        requestParams.put("ignoreCashAccounts", ignoreCashAccounts);
                        requestParams.put("ignorecustomers", ignorecustomers);
                        requestParams.put("ignorevendors", ignorevendors);
                        requestParams.put("transactionCurrency", tocurrencyid);
                        result = accAccountDAOobj.getAccountsForCombo(requestParams);
                        list = result.getEntityList();
                    }
                    jArr = getRevaluationForAccount(request, list, accCurrencyDAOobj, revalid, startdate, enddate, tocurrencyid, revalCurrencyRate, preferences,jArr);
                }
                jobj.put("data", jArr);
            }
        }
//            JSONArray jSONArray = jobj.getJSONArray("data");

        if (!expanderRequest) {
            jobj.put("endingBalanceSummary", endingBalanceSummary);
        }
        jobj.put("totalCount", result.getRecordTotalCount());

        return jobj;
    }

    private double[] getCustomersBalanceDetails(HttpServletRequest request, String customerid, String accountid, String startdate, String enddate, String revalcurrencyId, String revalCurrencyRate, String revalid, User userDetails,Company company) throws ServiceException, SessionExpiredException {
         double returnBalance[] = {0, 0, 0, 0,0};
        try {
            HashMap<String, Object> requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            HashMap<String, Object> requestParamsForReval = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            String tocurrencyid = sessionHandlerImpl.getCurrencyID(request);
            boolean isExport = !StringUtil.isNullObject(request.getAttribute("isExport"))?Boolean.parseBoolean(request.getAttribute("isExport").toString()):false;
            RevaluationHistory revalueationHistory = null;
            requestParams.put("nondeleted", "true");
            requestParams.put("deleted", "false");
            requestParams.put(InvoiceConstants.accid, customerid);
            requestParams.put(Constants.REQ_startdate, startdate);
            requestParams.put(Constants.REQ_enddate, enddate);
            requestParams.put("currencyfilterfortrans", revalcurrencyId);
            /**
             * Passing getRecordBasedOnJEDate as params to fetch record based on JE date rather than document creation date 
             * ERM-655.
             */
            requestParams.put("getRecordBasedOnJEDate", true);

            requestParamsForReval.put("nondeleted", "true");
            requestParamsForReval.put("deleted", "false");
            requestParamsForReval.put(InvoiceConstants.accid, customerid);
            requestParamsForReval.put(Constants.REQ_startdate, startdate);
            requestParamsForReval.put(Constants.REQ_enddate, enddate);
            requestParamsForReval.put("currencyfilterfortrans", revalcurrencyId);
            requestParamsForReval.put("isRevalue", true);
            double amountdue = 0;
            double profitloss = 0;
            String oldRevalId = "";
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = company.getCompanyID();

            /*
             * Normal Customer Invoices
             */
            double baseamountdue = 0;
            double revalamountdue = 0;
            double revalamountdueold = 0;
            KwlReturnObject result = accInvoiceDAOobj.getInvoices(requestParams);
            if (result.getEntityList() != null) {
                List list = result.getEntityList();
                for (Object object : list) {
                    revalueationHistory = new RevaluationHistory();
                    Invoice invoice = (Invoice) object;
                    double existingRate = 0;
                    double newBaseAmount = 0;
                    double newCurrentRate = 0.0;
                    
                    HashMap<String, Object> reqParams = new HashMap();
                    reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                    reqParams.put(Constants.companyKey, companyid);
                    existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(invoice.getID(),reqParams);
                    newCurrentRate = existingRate;
                    if (Constants.InvoiceAmountDueFlag) {
                        List ll = accInvoiceCommon.getInvoiceDiscountAmountInfo(requestParams, invoice);
                        amountdue = amountdue + (Double) ll.get(0);
                        newBaseAmount = (Double) ll.get(0);
                    } else {
                        List ll = accInvoiceCommon.getAmountDue_Discount(requestParams, invoice);
                        amountdue = amountdue + (Double) ll.get(0);
                        newBaseAmount = (Double) ll.get(0);
                    }
                    if (newBaseAmount <= 0) {
                        continue;
                    }
                    double exchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
                    Map<String, Object> invoiceId = new HashMap<>();
                    invoiceId.put("invoiceid", invoice.getID());
                    invoiceId.put("companyid", companyid);
                    //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null && history.isIsRealised()) {
                            exchangeRate = history.getEvalrate();
                        }
                    newCurrentRate = newCurrentRate == 0 ? exchangeRate : newCurrentRate;
                    String fromcurrencyid = invoice.getCurrency().getCurrencyID();
//                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, invoice.getJournalEntry().getEntryDate(), exchangeRate);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, invoice.getCreationDate(), exchangeRate);
                    //KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                    double baseamountduenew = (Double) bAmt.getEntityList().get(0);
                    baseamountdue = baseamountdue + (Double) bAmt.getEntityList().get(0);

//                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, invoice.getJournalEntry().getEntryDate(), (Double.parseDouble(revalCurrencyRate)));
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, invoice.getCreationDate(), (Double.parseDouble(revalCurrencyRate)));
                    double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                    revalamountdue = revalamountdue + revalamountduenew;
                    //Calculating for old revalue
                    double InvoiceGainLoss = 0;
                    if (existingRate > 0) {
//                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, invoice.getJournalEntry().getEntryDate(), existingRate);
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, invoice.getCreationDate(), existingRate);
                        double existingAmountdue = (Double) bAmt.getEntityList().get(0);
                        revalamountdueold = revalamountdueold + (Double) bAmt.getEntityList().get(0);
                        InvoiceGainLoss = (revalamountduenew - existingAmountdue);
                        profitloss = profitloss + authHandler.round((revalamountduenew - existingAmountdue),companyid);
                    } else {
                        InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                        profitloss = profitloss + authHandler.round((revalamountduenew - baseamountduenew),companyid);
                    }
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null) {
                        history.setIsRealised(true);
                        oldRevalId = history.getRevalid();
                        try {
                            accJournalEntryobj.addRevalHistory(history);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!isExport) {
                        revalueationHistory.setRevalid(revalid);
                        revalueationHistory.setInvoiceid(invoice.getID());
                        revalueationHistory.setModuleid(Constants.Acc_Invoice_ModuleId);
                        revalueationHistory.setEvalrate(Double.parseDouble(revalCurrencyRate));
                        revalueationHistory.setAmount(newBaseAmount);
                        revalueationHistory.setEvaldate(df.parse(enddate));
                        revalueationHistory.setUserid(userDetails);
                        revalueationHistory.setCurrency(invoice.getCurrency());
                        revalueationHistory.setCompany(company);
                        revalueationHistory.setProfitloss(InvoiceGainLoss);
                        revalueationHistory.setAccountid(accountid);
                        revalueationHistory.setCurrentRate(newCurrentRate);
                        try {
                            accJournalEntryobj.addRevalHistory(revalueationHistory);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            /*
             *              Opening Invoices
             */
                    
            requestParams.put("customerid", customerid);
            requestParams.put("accountId", accountid);
            requestParams.put("isAccountInvoices", true);
            requestParams.put("onlyAmountDue", true);
            result = accInvoiceDAOobj.getOpeningBalanceInvoices(requestParams);
            if (result.getEntityList() != null) {
//            Iterator itr = result.getEntityList().iterator();
                List invoiceList = result.getEntityList();
                for (Object object : invoiceList) {
                    revalueationHistory = new RevaluationHistory();
                    Invoice invoice = (Invoice) object;
                    double existingRate = 0;
                    double newBaseAmount = 0;
                    double newCurrentRate = 0.0;
                    
                    HashMap<String, Object> reqParams = new HashMap();
                    reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                    reqParams.put(Constants.companyKey, companyid);
                    existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(invoice.getID(),reqParams);
                    newCurrentRate = existingRate;

                    newBaseAmount = invoice.getOpeningBalanceAmountDue();
                    amountdue = amountdue + newBaseAmount;

                    double exchangeRate = invoice.getExchangeRateForOpeningTransaction();
                    boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                    if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase() && newCurrentRate == 0) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        newCurrentRate = 1 / exchangeRate;
                    }
                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                    invoiceId.put("invoiceid", invoice.getID());
                    invoiceId.put("companyid", companyid);
                    //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null && history.isIsRealised()) {
                        exchangeRate = history.getEvalrate();
                    }
                    newCurrentRate = newCurrentRate == 0 ? exchangeRate : newCurrentRate;
                    String fromcurrencyid = invoice.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    double baseamountduenew = 0d;
                    if (Constants.OpeningBalanceBaseAmountFlag) {
                        baseamountduenew = invoice.getOpeningBalanceBaseAmountDue();
                    } else {

                        if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, newBaseAmount, fromcurrencyid, invoice.getCreationDate(), exchangeRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, invoice.getCreationDate(), exchangeRate);
                        }
                        baseamountduenew = (Double) bAmt.getEntityList().get(0);
                    }
                    //KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                    baseamountdue = baseamountdue + baseamountduenew;

                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, invoice.getCreationDate(), (Double.parseDouble(revalCurrencyRate)));
                    double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                    revalamountdue = revalamountdue + revalamountduenew;
                    //Calculating for old revalue
                    double InvoiceGainLoss = 0;
                    if (existingRate > 0) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, invoice.getCreationDate(), existingRate);
                        double existingAmountdue = (Double) bAmt.getEntityList().get(0);
                        revalamountdueold = revalamountdueold + (Double) bAmt.getEntityList().get(0);
                        InvoiceGainLoss = (revalamountduenew - existingAmountdue);
                        profitloss = profitloss + authHandler.round((revalamountduenew - existingAmountdue),companyid);
                    } else {
                        InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                        profitloss = profitloss + authHandler.round((revalamountduenew - baseamountduenew),companyid);
                    }
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null) {
                        history.setIsRealised(true);
                        oldRevalId = history.getRevalid();
                        try {
                            accJournalEntryobj.addRevalHistory(history);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!isExport) {
                        revalueationHistory.setRevalid(revalid);
                        revalueationHistory.setInvoiceid(invoice.getID());
                        revalueationHistory.setModuleid(Constants.Acc_Invoice_ModuleId);
                        revalueationHistory.setEvalrate(Double.parseDouble(revalCurrencyRate));
                        revalueationHistory.setAmount(newBaseAmount);
                        revalueationHistory.setEvaldate(df.parse(enddate));
                        revalueationHistory.setUserid(userDetails);
                        revalueationHistory.setCurrency(invoice.getCurrency());
                        revalueationHistory.setCompany(company);
                        revalueationHistory.setProfitloss(InvoiceGainLoss);
                        revalueationHistory.setAccountid(accountid);
                        revalueationHistory.setCurrentRate(newCurrentRate);
                        try {
                            accJournalEntryobj.addRevalHistory(revalueationHistory);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

            }        
            /*
             * Opening Receipts
             */
            RevaluationHistory revaluationHistory = null;
            requestParams = null;
            requestParams = accReceiptController.getReceiptRequestMap(request);
            requestParams.put("currencyfilterfortrans", revalcurrencyId);
            requestParams.put("customerid", customerid);
            requestParams.put("onlyAmountDue", true);
            requestParams.put("isAccountReceipts", true);
            // get opening balance receipts created from opening balance button.
            result = accReceiptDao.getOpeningBalanceReceipts(requestParams);
            if (result.getEntityList() != null) {
                List<Receipt> receiptsList = result.getEntityList();
                for (Receipt receipt : receiptsList) {
                    revaluationHistory = new RevaluationHistory();
                    double existingRate = 0;
                    double newBaseAmount = 0;
                    double newCurrentRate = 0.0;
                    HashMap<String, Object> reqParams = new HashMap();
                    reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                    reqParams.put(Constants.companyKey, companyid);
                    existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(receipt.getID(),reqParams);
                    newCurrentRate = existingRate;
                    newBaseAmount = receipt.getOpeningBalanceAmountDue();
                    amountdue = amountdue + newBaseAmount;
                    double exchangeRate = receipt.getExchangeRateForOpeningTransaction();
                    boolean isopeningBalanceInvoice = receipt.isIsOpeningBalenceReceipt();
                    if (isopeningBalanceInvoice && receipt.isConversionRateFromCurrencyToBase() && newCurrentRate == 0) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        newCurrentRate = 1 / exchangeRate;
                    }
                    Map<String, Object> invoiceId = new HashMap<>();
                    invoiceId.put("invoiceid", receipt.getID());
                    invoiceId.put("companyid", companyid);
                    //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null && history.isIsRealised()) {
                        exchangeRate = history.getEvalrate();
                    }
                    newCurrentRate = newCurrentRate == 0 ? exchangeRate : newCurrentRate;
                    String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    double baseamountduenew = 0d;
                    if (Constants.OpeningBalanceBaseAmountFlag) {
                        baseamountduenew = receipt.getOpeningBalanceBaseAmountDue();
                    } else {
                        if (isopeningBalanceInvoice && receipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, newBaseAmount, fromcurrencyid, receipt.getCreationDate(), exchangeRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, receipt.getCreationDate(), exchangeRate);
                        }
                        baseamountduenew = (Double) bAmt.getEntityList().get(0);
                    }
                    //KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                    baseamountdue = baseamountdue + baseamountduenew;
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, receipt.getCreationDate(), (Double.parseDouble(revalCurrencyRate)));
                    double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                    revalamountdue = revalamountdue + revalamountduenew;
                    //Calculating for old revalue
                    double InvoiceGainLoss = 0;
                    if (existingRate > 0) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, receipt.getCreationDate(), existingRate);
                        double existingAmountdue = (Double) bAmt.getEntityList().get(0);
                        revalamountdueold = revalamountdueold + (Double) bAmt.getEntityList().get(0);
                        InvoiceGainLoss = (revalamountduenew - existingAmountdue);
                        profitloss = profitloss + authHandler.round((-(revalamountduenew - existingAmountdue)),companyid);
                    } else {
                        InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                        profitloss = profitloss + authHandler.round((-(revalamountduenew - baseamountduenew)),companyid);
                    }
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null) {
                        history.setIsRealised(true);
                        oldRevalId = history.getRevalid();
                        try {
                            accJournalEntryobj.addRevalHistory(history);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!isExport) {
                        revaluationHistory.setRevalid(revalid);
                        revaluationHistory.setInvoiceid(receipt.getID());
                        revaluationHistory.setModuleid(Constants.Acc_opening_Receipt);
                        revaluationHistory.setEvalrate(Double.parseDouble(revalCurrencyRate));
                        revaluationHistory.setAmount(newBaseAmount);
                        revaluationHistory.setCurrency(receipt.getCurrency());
                        revaluationHistory.setEvaldate(df.parse(enddate));
                        revaluationHistory.setUserid(userDetails);
                        revaluationHistory.setCompany(company);
                        revaluationHistory.setProfitloss(-(InvoiceGainLoss)); //For Receipt it reverse than the Invoice.
                        revaluationHistory.setAccountid(accountid);
                        revaluationHistory.setCurrentRate(newCurrentRate);
                        try {
                            accJournalEntryobj.addRevalHistory(revaluationHistory);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            /*
             * Advance Customer Receipts
             */
            requestParams = null;
            requestParams = accReceiptController.getReceiptRequestMap(request);
            requestParams.put("currencyfilterfortrans", revalcurrencyId);
            requestParams.put("customerid", customerid);
            requestParams.put("nondeleted", "true");
            requestParams.put("deleted", "false");
            requestParams.put(Constants.REQ_enddate, enddate);
            requestParams.put("onlyAdvAmountDue", true);
             /**
             * Passing getRecordBasedOnJEDate as params to fetch record based on JE date rather than document creation date as documents are revalued on the basis of JE date
             * ERM-655.
             */
            requestParams.put("isToFetchRecordLessEndDate", true);
            requestParams.put("getRecordBasedOnJEDate", true);
            result = accReceiptDao.getReceipts(requestParams);
            List paymentsList = result.getEntityList();
//            Iterator itr = paymentsList.iterator();
            for (Object object : paymentsList) {
                Object[] row = (Object[]) object;
                Receipt receipt = (Receipt) row[0];
                Account acc = (Account) row[1];
                revaluationHistory = new RevaluationHistory();
                double existingRate = 0;
                double newBaseAmount = 0;
                double newCurrentRate = 0.0;
                HashMap<String, Object> reqParams = new HashMap();
                reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                reqParams.put(Constants.companyKey, companyid);
                existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(receipt.getID(),reqParams);
                newCurrentRate = existingRate;
                newBaseAmount=0;
                for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
                     newBaseAmount += advanceDetail.getAmountDue();
                }
                amountdue = amountdue + newBaseAmount;
                double exchangeRate = receipt.getJournalEntry().getExternalCurrencyRate();
//                Date transDate=receipt.getJournalEntry().getEntryDate();
                Date transDate=receipt.getCreationDate();
                Map<String, Object> invoiceId = new HashMap<>();
                invoiceId.put("invoiceid", receipt.getID());
                invoiceId.put("companyid", companyid);
                //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                if (history != null && history.isIsRealised()) {
                    exchangeRate = history.getEvalrate();
                }
                newCurrentRate = newCurrentRate == 0 ? exchangeRate : newCurrentRate;
                String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                double baseamountduenew = 0d;
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, transDate, exchangeRate);
                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                baseamountdue = baseamountdue + baseamountduenew;
                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, (Double.parseDouble(revalCurrencyRate)));
                double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                revalamountdue = revalamountdue + revalamountduenew;
                //Calculating for old revalue
                double InvoiceGainLoss = 0;
                if (existingRate > 0) {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, existingRate);
                    double existingAmountdue = (Double) bAmt.getEntityList().get(0);
                    revalamountdueold = revalamountdueold + (Double) bAmt.getEntityList().get(0);
                    InvoiceGainLoss = (revalamountduenew - existingAmountdue);
                    profitloss = profitloss + authHandler.round((-(revalamountduenew - existingAmountdue)),companyid);
                } else {
                    InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                    profitloss = profitloss + authHandler.round((-(revalamountduenew - baseamountduenew)),companyid);
                }
                invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                if (history != null) {
                    history.setIsRealised(true);
                    oldRevalId = history.getRevalid();
                    try {
                        accJournalEntryobj.addRevalHistory(history);
                    } catch (AccountingException ex) {
                        Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (!isExport) {
                    revaluationHistory.setRevalid(revalid);
                    revaluationHistory.setInvoiceid(receipt.getID());
                    revaluationHistory.setModuleid(Constants.Acc_Receive_Payment_ModuleId);
                    revaluationHistory.setEvalrate(Double.parseDouble(revalCurrencyRate));
                    revaluationHistory.setAmount(newBaseAmount);
                    revaluationHistory.setEvaldate(df.parse(enddate));
                    revaluationHistory.setUserid(userDetails);
                    revaluationHistory.setCurrency(receipt.getCurrency());
                    revaluationHistory.setCompany(company);
                    revaluationHistory.setProfitloss(-(InvoiceGainLoss)); //For Receipt it reverse than the Invoice.
                    revaluationHistory.setAccountid(accountid);
                    revaluationHistory.setCurrentRate(newCurrentRate);
                    try {
                        accJournalEntryobj.addRevalHistory(revaluationHistory);
                    } catch (AccountingException ex) {
                        Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            
            /*
             *  Opening Credit Note
             */
            requestParams = null;
            requestParams = accCreditNoteController.getCreditNoteMap(request);
            requestParams.put("nondeleted", "true");
            requestParams.put("deleted", "false");
            requestParams.put(Constants.REQ_startdate, startdate);
            requestParams.put(Constants.REQ_enddate, enddate);
            requestParams.put("currencyfilterfortrans", revalcurrencyId);
            requestParams.put("customerid", customerid);
            requestParams.put("onlyAmountDue", true);
            requestParams.put("isAccountCNs", true);
            // get opening balance receipts created from opening balance button.
            result = accCreditNoteDAOobj.getOpeningBalanceCNs(requestParams);
            if (result.getEntityList() != null) {
                List<CreditNote> creditNotesList = result.getEntityList();
                for (CreditNote creditNote : creditNotesList) {
                    revaluationHistory = new RevaluationHistory();
                    double existingRate = 0;
                    double newBaseAmount = 0;
                    double newCurrentRate = 0.0;
                    HashMap<String, Object> reqParams = new HashMap();
                    reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                    reqParams.put(Constants.companyKey, companyid);
                    existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(creditNote.getID(),reqParams);
                    newCurrentRate = existingRate;
                    newBaseAmount = creditNote.getOpeningBalanceAmountDue();
                    amountdue = amountdue + newBaseAmount;
                    double exchangeRate = creditNote.getExchangeRateForOpeningTransaction();
                    boolean isopeningBalanceInvoice = creditNote.isIsOpeningBalenceCN();
                    if (isopeningBalanceInvoice && creditNote.isConversionRateFromCurrencyToBase() && newCurrentRate == 0) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        newCurrentRate = 1 / exchangeRate;
                    }
                    Map<String, Object> invoiceId = new HashMap<>();
                    invoiceId.put("invoiceid", creditNote.getID());
                    invoiceId.put("companyid", companyid);
                    //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null && history.isIsRealised()) {
                        exchangeRate = history.getEvalrate();
                    }
                    newCurrentRate = newCurrentRate == 0 ? exchangeRate : newCurrentRate;
                    String fromcurrencyid = creditNote.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    double baseamountduenew = 0d;
                    if (Constants.OpeningBalanceBaseAmountFlag) {
                        baseamountduenew = creditNote.getOpeningBalanceBaseAmountDue();
                    } else {
                        if (isopeningBalanceInvoice && creditNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, newBaseAmount, fromcurrencyid, creditNote.getCreationDate(), exchangeRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, creditNote.getCreationDate(), exchangeRate);
                        }
                        baseamountduenew = (Double) bAmt.getEntityList().get(0);
                    }
                    //KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                    baseamountdue = baseamountdue + baseamountduenew;

                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, creditNote.getCreationDate(), (Double.parseDouble(revalCurrencyRate)));
                    double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                    revalamountdue = revalamountdue + revalamountduenew;
                    //Calculating for old revalue
                    double InvoiceGainLoss = 0;
                    if (existingRate > 0) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, creditNote.getCreationDate(), existingRate);
                        double existingAmountdue = (Double) bAmt.getEntityList().get(0);
                        revalamountdueold = revalamountdueold + (Double) bAmt.getEntityList().get(0);
                        InvoiceGainLoss = (revalamountduenew - existingAmountdue);
                        profitloss = profitloss + authHandler.round((-(revalamountduenew - existingAmountdue)),companyid);
                    } else {
                        InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                        profitloss = profitloss + authHandler.round((-(revalamountduenew - baseamountduenew)),companyid);
                    }
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null) {
                        history.setIsRealised(true);
                        oldRevalId = history.getRevalid();
                        try {
                            accJournalEntryobj.addRevalHistory(history);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!isExport) {
                        revaluationHistory.setRevalid(revalid);
                        revaluationHistory.setInvoiceid(creditNote.getID());
                        revaluationHistory.setModuleid(Constants.Acc_opening_Customer_CreditNote);
                        revaluationHistory.setEvalrate(Double.parseDouble(revalCurrencyRate));
                        revaluationHistory.setAmount(newBaseAmount);
                        revaluationHistory.setEvaldate(df.parse(enddate));
                        revaluationHistory.setUserid(userDetails);
                        revaluationHistory.setCompany(company);
                        revaluationHistory.setCurrency(creditNote.getCurrency());
                        revaluationHistory.setProfitloss(-(InvoiceGainLoss));  // Negative as we have alreday recived money
                        revaluationHistory.setAccountid(accountid);
                        revaluationHistory.setCurrentRate(newCurrentRate);
                        try {
                            accJournalEntryobj.addRevalHistory(revaluationHistory);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            /*
             * Opening Debit Note of Customer
             */
            requestParams = null;
            requestParams = accDebitNoteController.gettDebitNoteMap(request);
            requestParams.put("nondeleted", "true");
            requestParams.put("deleted", "false");
            requestParams.put(Constants.REQ_startdate, startdate);
            requestParams.put(Constants.REQ_enddate, enddate);
            requestParams.put("currencyfilterfortrans", revalcurrencyId);
            requestParams.put("customerid", customerid);
            requestParams.put("onlyAmountDue", true);
            requestParams.put("isAccountDNs", true);
            // get opening balance receipts created from opening balance button.
            result = accDebitNoteobj.getOpeningBalanceCustomerDNs(requestParams);
            if (result.getEntityList() != null) {
                List<DebitNote> debitNotesList = result.getEntityList();
                for (DebitNote debitNote : debitNotesList) {
                    revaluationHistory = new RevaluationHistory();
                    double existingRate = 0;
                    double newBaseAmount = 0;
                    double newCurrentRate = 0.0;
                    HashMap<String, Object> reqParams = new HashMap();
                    reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                    reqParams.put(Constants.companyKey, companyid);
                    existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(debitNote.getID(),reqParams);
                    newCurrentRate = existingRate;
                    newBaseAmount = debitNote.getOpeningBalanceAmountDue();
                    amountdue = amountdue + newBaseAmount;
                    double exchangeRate = debitNote.getExchangeRateForOpeningTransaction();
                    boolean isopeningBalanceInvoice = debitNote.isIsOpeningBalenceDN();
                    if (isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase() && newCurrentRate == 0) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        newCurrentRate = 1 / exchangeRate;
                    }
                    Map<String, Object> invoiceId = new HashMap<>();
                    invoiceId.put("invoiceid", debitNote.getID());
                    invoiceId.put("companyid", companyid);
                    //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null && history.isIsRealised()) {
                        exchangeRate = history.getEvalrate();
                    }
                    newCurrentRate = newCurrentRate == 0 ? exchangeRate : newCurrentRate;
                    String fromcurrencyid = debitNote.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    double baseamountduenew = 0d;
                    if (Constants.OpeningBalanceBaseAmountFlag) {
                        baseamountduenew = debitNote.getOpeningBalanceBaseAmountDue();
                    } else {

                        if (isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, newBaseAmount, fromcurrencyid, debitNote.getCreationDate(), exchangeRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, debitNote.getCreationDate(), exchangeRate);
                        }
                        baseamountduenew = (Double) bAmt.getEntityList().get(0);
                    }
                    //KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                    baseamountdue = baseamountdue + baseamountduenew;

                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, debitNote.getCreationDate(), (Double.parseDouble(revalCurrencyRate)));
                    double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                    revalamountdue = revalamountdue + revalamountduenew;
                    //Calculating for old revalue
                    double InvoiceGainLoss = 0;
                    if (existingRate > 0) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, debitNote.getCreationDate(), existingRate);
                        double existingAmountdue = (Double) bAmt.getEntityList().get(0);
                        revalamountdueold = revalamountdueold + (Double) bAmt.getEntityList().get(0);
                        InvoiceGainLoss = (revalamountduenew - existingAmountdue);
                        profitloss = profitloss + authHandler.round((revalamountduenew - existingAmountdue),companyid);
                    } else {
                        InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                        profitloss = profitloss + authHandler.round((revalamountduenew - baseamountduenew),companyid);
                    }
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null) {
                        history.setIsRealised(true);
                        oldRevalId = history.getRevalid();
                        try {
                            accJournalEntryobj.addRevalHistory(history);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!isExport) {
                        revaluationHistory.setRevalid(revalid);
                        revaluationHistory.setInvoiceid(debitNote.getID());
                        revaluationHistory.setModuleid(Constants.Acc_opening_Customer_DebitNote);
                        revaluationHistory.setEvalrate(Double.parseDouble(revalCurrencyRate));
                        revaluationHistory.setAmount(newBaseAmount);
                        revaluationHistory.setEvaldate(df.parse(enddate));
                        revaluationHistory.setUserid(userDetails);
                        revaluationHistory.setCompany(company);
                        revaluationHistory.setCurrency(debitNote.getCurrency());
                        revaluationHistory.setProfitloss(InvoiceGainLoss);  //Reverase case than invoice
                        revaluationHistory.setAccountid(accountid);
                        revaluationHistory.setCurrentRate(newCurrentRate);
                        try {
                            accJournalEntryobj.addRevalHistory(revaluationHistory);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }

            /*
             * Normal Debit Note of Customers
             */
            requestParams = null;
            requestParams = accDebitNoteController.gettDebitNoteMap(request);
            requestParams.put("nondeleted", "true");
            requestParams.put("deleted", "false");
            requestParams.put(Constants.REQ_startdate, startdate);
            requestParams.put(Constants.REQ_enddate, enddate);
            requestParams.put("currencyfilterfortrans", revalcurrencyId);
            requestParams.put("customerVendorID", customerid);
            requestParams.put("cntype", 4);
            // get opening balance receipts created from opening balance button.
            requestParams.put("isNewUI", false);
            requestParams.put("isNoteForPayment", false);
             /**
             * Passing getRecordBasedOnJEDate as params to fetch record based on JE date rather than document creation date as documents are revalued on the basis of JE date
             * ERM-655.
             */
            requestParams.put("getRecordBasedOnJEDate", true);
            requestParams.put("isToFetchRecordLessEndDate", true);
            result = accDebitNoteobj.getDebitNoteMerged(requestParams);
            if (result.getEntityList() != null) {
                List<DebitNote> debitNotesList = result.getEntityList();
                Iterator DebitNoteitr = debitNotesList.iterator();
                while (DebitNoteitr.hasNext()) {
                    Object[] row = (Object[]) DebitNoteitr.next();
                    boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                    KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), (String) row[1]);
                    DebitNote debitNote = (DebitNote) resultObject.getEntityList().get(0);
                    JournalEntry je = debitNote.getJournalEntry();
                    revaluationHistory = new RevaluationHistory();
                    double existingRate = 0;
                    double newBaseAmount = 0;
                    Date debitNoteDate = null;
                    double externalCurrencyRate = 0d;
//                    debitNoteDate = je.getEntryDate();
                    debitNoteDate = debitNote.getCreationDate();
                    externalCurrencyRate = je.getExternalCurrencyRate();
                    double newCurrentRate = 0.0;
                    HashMap<String, Object> reqParams = new HashMap();
                    reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                    reqParams.put(Constants.companyKey, companyid);
                    existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(debitNote.getID(),reqParams);
                    newCurrentRate = existingRate;
                    newBaseAmount = debitNote.isOtherwise() ? debitNote.getDnamountdue() : 0;
                    if (newBaseAmount <= 0) {
                        continue;
                    }
                    amountdue = amountdue + newBaseAmount;
                    double exchangeRate = externalCurrencyRate;
                    Map<String, Object> invoiceId = new HashMap<>();
                    invoiceId.put("invoiceid", debitNote.getID());
                    invoiceId.put("companyid", companyid);
                    //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null && history.isIsRealised()) {
                        exchangeRate = history.getEvalrate();
                    }
                    newCurrentRate = newCurrentRate == 0 ? exchangeRate : newCurrentRate;
                    String fromcurrencyid = debitNote.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    double baseamountduenew = 0d;
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, debitNoteDate, exchangeRate);
                    baseamountduenew = (Double) bAmt.getEntityList().get(0);
                    baseamountdue = baseamountdue + newBaseAmount;
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, debitNoteDate, (Double.parseDouble(revalCurrencyRate)));
                    double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                    revalamountdue = revalamountdue + revalamountduenew;
                    //Calculating for old revalue
                    double InvoiceGainLoss = 0;
                    if (existingRate > 0) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, debitNoteDate, existingRate);
                        double existingAmountdue = (Double) bAmt.getEntityList().get(0);
                        revalamountdueold = revalamountdueold + (Double) bAmt.getEntityList().get(0);
                        InvoiceGainLoss = (revalamountduenew - existingAmountdue);
                        profitloss = profitloss + authHandler.round((revalamountduenew - existingAmountdue),companyid);
                    } else {
                        InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                        profitloss = profitloss + authHandler.round((revalamountduenew - baseamountduenew),companyid);
                    }
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null) {
                        history.setIsRealised(true);
                        oldRevalId = history.getRevalid();
                        try {
                            accJournalEntryobj.addRevalHistory(history);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!isExport) {
                        revaluationHistory.setRevalid(revalid);
                        revaluationHistory.setInvoiceid(debitNote.getID());
                        revaluationHistory.setModuleid(Constants.Acc_Debit_Note_ModuleId);
                        revaluationHistory.setEvalrate(Double.parseDouble(revalCurrencyRate));
                        revaluationHistory.setAmount(newBaseAmount);
                        revaluationHistory.setEvaldate(df.parse(enddate));
                        revaluationHistory.setUserid(userDetails);
                        revaluationHistory.setCompany(company);
                        revaluationHistory.setCurrency(debitNote.getCurrency());
                        revaluationHistory.setProfitloss(InvoiceGainLoss);  //Reverase case than invoice
                        revaluationHistory.setAccountid(accountid);
                        revaluationHistory.setCurrentRate(newCurrentRate);
                        try {
                            accJournalEntryobj.addRevalHistory(revaluationHistory);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            /*
             * Normal Credit Note of Customers
             */
            requestParams = null;
            requestParams = accCreditNoteController.getCreditNoteMap(request);
            requestParams.put("nondeleted", "true");
            requestParams.put("deleted", "false");
            requestParams.put(Constants.REQ_startdate, startdate);
            requestParams.put(Constants.REQ_enddate, enddate);
            requestParams.put("currencyfilterfortrans", revalcurrencyId);
            requestParams.put("customerVendorID", customerid);
            requestParams.put("cntype", 1);
            // get opening balance receipts created from opening balance button.
            requestParams.put("isNewUI", false);
            requestParams.put("isNoteForPayment", false);
             /**
             * Passing getRecordBasedOnJEDate as params to fetch record based on JE date rather than document creation date as documents are revalued on the basis of JE date
             * ERM-655.
             */
            requestParams.put("getRecordBasedOnJEDate", true);
            requestParams.put("isToFetchRecordLessEndDate", true);
            result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
            if (result.getEntityList() != null) {
                List creditNoteList = result.getEntityList();
//                Iterator creditNoteitr = creditNoteList.iterator();
                for (Object object : creditNoteList) {
                    Object[] row = (Object[]) object;
                    boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                    KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) row[1]);
                    CreditNote creditNote = (CreditNote) resultObject.getEntityList().get(0);
                    JournalEntry je = creditNote.getJournalEntry();
                    revaluationHistory = new RevaluationHistory();
                    double existingRate = 0;
                    double newBaseAmount = 0;
                    Date debitNoteDate = null;
                    double externalCurrencyRate = 0d;
                    double newCurrentRate = 0.0;
//                    debitNoteDate = je.getEntryDate();
                    debitNoteDate = creditNote.getCreationDate();
                    externalCurrencyRate = je.getExternalCurrencyRate();
                    HashMap<String, Object> reqParams = new HashMap();
                    reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                    reqParams.put(Constants.companyKey, companyid);
                    existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(creditNote.getID(),reqParams);
                    newCurrentRate = existingRate;
                    newBaseAmount = creditNote.isOtherwise() ? creditNote.getCnamountdue() : 0;
                    amountdue = amountdue + newBaseAmount;
                    double exchangeRate = externalCurrencyRate;
                    Map<String, Object> invoiceId = new HashMap<>();
                    invoiceId.put("invoiceid", creditNote.getID());
                    invoiceId.put("companyid", companyid);
                    //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null && history.isIsRealised()) {
                        exchangeRate = history.getEvalrate();
                    }
                    newCurrentRate = newCurrentRate == 0 ? exchangeRate : newCurrentRate;
                    String fromcurrencyid = creditNote.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    double baseamountduenew = 0d;
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, debitNoteDate, exchangeRate);
                    baseamountduenew = (Double) bAmt.getEntityList().get(0);
                    baseamountdue = baseamountdue + newBaseAmount;
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, debitNoteDate, (Double.parseDouble(revalCurrencyRate)));
                    double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                    revalamountdue = revalamountdue + revalamountduenew;
                    //Calculating for old revalue
                    double InvoiceGainLoss = 0;
                    if (existingRate > 0) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, debitNoteDate, existingRate);
                        double existingAmountdue = (Double) bAmt.getEntityList().get(0);
                        revalamountdueold = revalamountdueold + (Double) bAmt.getEntityList().get(0);
                        InvoiceGainLoss = (revalamountduenew - existingAmountdue);
                        profitloss = profitloss + authHandler.round((-(revalamountduenew - existingAmountdue)),companyid);
                    } else {
                        InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                        profitloss = profitloss + authHandler.round((-(revalamountduenew - baseamountduenew)),companyid);
                    }
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null) {
                        history.setIsRealised(true);
                        oldRevalId = history.getRevalid();
                        try {
                            accJournalEntryobj.addRevalHistory(history);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!isExport) {
                        revaluationHistory.setRevalid(revalid);
                        revaluationHistory.setInvoiceid(creditNote.getID());
                        revaluationHistory.setModuleid(Constants.Acc_Credit_Note_ModuleId);
                        revaluationHistory.setEvalrate(Double.parseDouble(revalCurrencyRate));
                        revaluationHistory.setAmount(newBaseAmount);
                        revaluationHistory.setEvaldate(df.parse(enddate));
                        revaluationHistory.setUserid(userDetails);
                        revaluationHistory.setCurrency(creditNote.getCurrency());
                        revaluationHistory.setCompany(company);
                        revaluationHistory.setProfitloss(-(InvoiceGainLoss));  // Negative as we have alreday recived money
                        revaluationHistory.setAccountid(accountid);
                        revaluationHistory.setCurrentRate(newCurrentRate);
                        try {
                            accJournalEntryobj.addRevalHistory(revaluationHistory);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }

            returnBalance[0] = amountdue;
            returnBalance[1] = baseamountdue;
            returnBalance[2] = revalamountdue;
            returnBalance[3] = revalamountdueold;
            returnBalance[4] = profitloss;
//            returnBalance[5] = oldRevalId;
            request.setAttribute("oldRevalId", oldRevalId);

        } catch (Exception ex) {
            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
        }   
            
        return returnBalance;

    }

    private JSONArray getRevaluationForAccount(HttpServletRequest request, List list, accCurrencyDAO accCurrencyDAOobj,String revalid, String startdate, String enddate, String revalcurrencyId, String revalCurrencyRate, CompanyAccountPreferences preferences,JSONArray jArr) throws SessionExpiredException, ServiceException, ParseException {
        try {
            String currencyid = "";
            int accTypeId = Integer.parseInt(request.getParameter("accTypeId"));
           
            KwlReturnObject result = accountingHandlerDAOobj.getObject(User.class.getName(), sessionHandlerImpl.getUserid(request));
            User userDetails = (User) result.getEntityList().get(0);
            result = accountingHandlerDAOobj.getObject(Company.class.getName(), sessionHandlerImpl.getCompanyid(request));
            Company company = (Company) result.getEntityList().get(0);
            for (Object object : list) {
                String customerid = null;
                String customername = null;
                String accountid = null;
                String customercode = null;
                Account account = null;
                double totalBalance[] = {0, 0, 0, 0, 0};
		Object[] accobj = (Object[])object;     //SDP-11231
                //account = (Account) object;
                accountid = (String)accobj[0];  //account.getID();
                KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), accountid);
                account = (Account) accresult.getEntityList().get(0);
                if (account != null) {
                    //account = (Account) object;
                    accountid = account.getID();
                    customercode = account.getAccountCode();
                    if (accTypeId == Group.ACCOUNTTYPE_BANK || accTypeId == Constants.CashInHandAccountTye) {
                        totalBalance = getBankBalanceDetails(request, accountid, account, startdate, enddate, revalcurrencyId, revalCurrencyRate, revalid, userDetails, company, preferences);
                    }
                    if (totalBalance[0] == 0) {
                        continue;
                    }

                    JSONObject obj = new JSONObject();
                    obj.put("origianlamount", totalBalance[0]);
                    obj.put("baseamount", totalBalance[1]);
                    obj.put("revalamount", totalBalance[2]);
                    if (totalBalance[3] == 0) {
                        obj.put("profitloss", totalBalance[4]);
                        obj.put("preamount", "-");
                    } else {
                        obj.put("profitloss", totalBalance[4]);
                        obj.put("preamount", totalBalance[3]);
                    }
                    obj.put("revalid", revalid);

                    obj.put("accid", customerid);
                    obj.put("exchangeValue", revalCurrencyRate);
                    if (accTypeId == Constants.CUSTOMER || accTypeId == Constants.VENDOR) {
                        obj.put("accname", customername);
                    } else {
                        obj.put("accname", (!StringUtil.isNullOrEmpty(account.getName())) ? account.getName() : (!StringUtil.isNullOrEmpty(account.getAcccode()) ? account.getAcccode() : ""));
                    }
                    KWLCurrency accountCurrency = account.getCurrency();
                    currencyid = accountCurrency == null ? "" : accountCurrency.getCurrencyID();
                    obj.put("currencyid", currencyid);
                    obj.put("currencysymbol", (accountCurrency == null ? "" : accountCurrency.getSymbol()));
                    obj.put("currencyname", (accountCurrency == null ? "" : accountCurrency.getName()));
                    obj.put("acccode", customercode);
                    jArr.put(obj);
                }
            }
           
        } catch (JSONException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getAccountJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    private JSONObject getAccountJsonForRevaluation(HttpServletRequest request, List list, accCurrencyDAO accCurrencyDAOobj, boolean noactivity, String startdate, String enddate, String revalcurrencyId, String revalCurrencyRate, CompanyAccountPreferences preferences) throws SessionExpiredException, ServiceException, ParseException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
       try {
            String currencyid = "";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            int  accTypeId =Integer.parseInt(request.getParameter("accTypeId"));
            String revalid = UUID.randomUUID().toString();
            double  openbalanceSummary = 0, presentbalanceSummary = 0;
            KwlReturnObject result = accountingHandlerDAOobj.getObject(User.class.getName(), sessionHandlerImpl.getUserid(request));
            User userDetails = (User) result.getEntityList().get(0);
            result = accountingHandlerDAOobj.getObject(Company.class.getName(), sessionHandlerImpl.getCompanyid(request));
            Company company = (Company) result.getEntityList().get(0);
            String oldRevalId="";
            for (Object object : list) {
                String customerid = null;
                String customername = null;
                String accountid = null;
                String customercode = null;
                Account account = null;
                boolean continueforCustomer=false;
                double totalBalance[] = {0, 0, 0, 0,0};
                int exchangeRateType = 0;//0 = Forign exchange 1=Base Rate
                String revaluateJson = request.getParameter("reevalueData");
                if (!StringUtil.isNullOrEmpty(revaluateJson)) {
                    double profitloss=0.0,baseamount=0.0,revalamount=0.0,preamount=0.0,origianlamount=0.0;
                    JSONArray jArrForCurrency = new JSONArray(revaluateJson);
                    for (int i = 0; i < jArrForCurrency.length(); i++) {
                        String tocurrencyid = jArrForCurrency.getJSONObject(i).get("tocurrencyid").toString();
                        revalCurrencyRate = jArrForCurrency.getJSONObject(i).get("newexchangerate").toString();
                        exchangeRateType = Integer.parseInt(jArrForCurrency.getJSONObject(i).get("exchangeratetype").toString());
                        if (exchangeRateType == 1) {
                            revalCurrencyRate = Double.toString(1 / (Double.parseDouble(revalCurrencyRate)));
                        }


                        if (accTypeId == Constants.CUSTOMER || accTypeId == Constants.VENDOR) {
                            Object[] row = (Object[]) object;
//                            if (accTypeId == Constants.CUSTOMER) {
//                                customerid = (String) row[0].toString();
//                                customername = (String) row[1].toString();
//                                accountid = (String) row[2].toString();
//                                customercode = (row[3] != null) ? (String) row[3].toString() : "";
//                            } else if (accTypeId == Constants.VENDOR) {
                                customerid = (String) row[0].toString();
                                customername = (String) row[1].toString();
                                accountid = (String) row[2].toString();
                                customercode = (row[3] != null) ? (String) row[3].toString() : "";
//                            }
                            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(Account.class.getName(), accountid);
                            account = (Account) curresult.getEntityList().get(0);
                        } else {
                            account = (Account) object;
                            accountid = account.getID();
                            customercode = account.getAccountCode();
                        }
                        if (accTypeId == Constants.CUSTOMER) {
                            totalBalance = getCustomersBalanceDetails(request, customerid, accountid, startdate, enddate, tocurrencyid, revalCurrencyRate, revalid, userDetails, company);
                            oldRevalId = !StringUtil.isNullObject(request.getAttribute("oldRevalId")) ? request.getAttribute("oldRevalId").toString() : "";
                        } else if (accTypeId == Constants.VENDOR) {
                            totalBalance = getVendorBalanceDetails(request, customerid, accountid, startdate, enddate, tocurrencyid, revalCurrencyRate, revalid, userDetails, company);
                            oldRevalId = !StringUtil.isNullObject(request.getAttribute("oldRevalId")) ? request.getAttribute("oldRevalId").toString() : "";
                        } else if (accTypeId == Group.ACCOUNTTYPE_BANK || accTypeId == Constants.CashInHandAccountTye) {
                            totalBalance = getBankBalanceDetails(request, accountid, account, startdate, enddate, revalcurrencyId, revalCurrencyRate, revalid, userDetails, company, preferences);
                        }
                        if (totalBalance[0] == 0) {
                            continueforCustomer = true;
                            continue;
                        }
                        origianlamount = +totalBalance[0];
                        baseamount += totalBalance[1];
                        revalamount += totalBalance[2];
                        profitloss += totalBalance[4];
                        preamount += totalBalance[3];
                        continueforCustomer = false;


                        if (continueforCustomer) {
                        continue;
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("origianlamount", origianlamount);
                    obj.put("baseamount", baseamount);
                    obj.put("revalamount", revalamount);
                    if (totalBalance[3] == 0) {
                        obj.put("profitloss", profitloss);
                        obj.put("preamount", "-");
                    } else {
                        obj.put("profitloss", profitloss);
                        obj.put("preamount", preamount);
                    }
                    obj.put("revalid", revalid);

                    obj.put("accid", customerid);
                    obj.put("exchangeValue", revalCurrencyRate);
                    if (accTypeId == Constants.CUSTOMER || accTypeId == Constants.VENDOR) {
                        obj.put("accname", customername);
                    } else {
                        obj.put("accname", (!StringUtil.isNullOrEmpty(account.getName())) ? account.getName() : (!StringUtil.isNullOrEmpty(account.getAcccode()) ? account.getAcccode() : ""));
                    }
                    KWLCurrency accountCurrency = account.getCurrency();
                    currencyid = accountCurrency == null ? "" : accountCurrency.getCurrencyID();

                    obj.put("currencyid", currencyid);
                    obj.put("currencysymbol", (accountCurrency == null ? "" : accountCurrency.getSymbol()));
                    obj.put("currencyname", (accountCurrency == null ? "" : accountCurrency.getName()));

                    obj.put("acccode", customercode);
                    obj.put("oldRevalId", oldRevalId);
                    if (request.getParameter("type") != null && request.getParameter("type").equals(Constants.detailedXls)) {
                        JSONObject rowjobj = new JSONObject();
                        rowjobj = obj;
                        JSONArray DataRowsArr = null;
                        request.setAttribute("accountid", customerid);
                        JSONObject tempObj = getAccountInvoicesJsonForRevaluation(request, accCurrencyDAOobj, noactivity); //revalcurrencyId, revalCurrencyRate
                        if (tempObj.has("data") && tempObj.get("data") != null) {
                            DataRowsArr = tempObj.getJSONArray("data");
                        }
                        jArr.put(obj);
                        rowjobj.put("type", "");
                        for (int j = 0; j < DataRowsArr.length(); j++) {
                            JSONObject tempjobj = new JSONObject();
                            tempjobj = DataRowsArr.getJSONObject(j);
                            DateFormat df = authHandler.getGlobalDateFormat();
                            if (tempjobj.has("subprofitloss")) {
                                tempjobj.put("subprofitloss", authHandler.formattedAmount(tempjobj.optDouble("subprofitloss", 0.0), companyid));
                            }
                            if (tempjobj.has("documentamount")) {
                                tempjobj.put("documentamount", authHandler.formattedAmount(tempjobj.optDouble("documentamount", 0.0), companyid));
                            }
                            jArr.put(tempjobj);
                        }

                    } else {
                        jArr.put(obj);
                    }
                 }
                 }
                
            }    
            jobj.put("data", jArr);
            jobj.put("openbalanceSummary", openbalanceSummary);
            jobj.put("presentbalanceSummary", presentbalanceSummary);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getAccountJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }
 public JSONObject getAccountInvoicesJsonForRevaluation(HttpServletRequest request, accCurrencyDAO accCurrencyDAOobj, boolean noactivity) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {            
            KwlReturnObject bAmt, presentBaseAmount;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = "";
            int accTypeId = Integer.parseInt(request.getParameter("accTypeId"));
            String accId = request.getParameter("accountid");
            String type = request.getParameter("type") != null ? (String) request.getParameter("type") : null;
            if(!StringUtil.isNullOrEmpty(type) && request.getAttribute("accountid") != null){
                accId = (String) request.getAttribute("accountid");
            }
            // String startdate = request.getParameter("startdate");     , String revalcurrencyId, String revalCurrencyRate
            String enddate = request.getParameter("enddate");
            DateFormat df = authHandler.getDateOnlyFormat(request);
            int exchangeRateType = 0;//0 = Forign exchange 1=Base Rate
            String revaluateJson = request.getParameter("reevalueData");
            if (!StringUtil.isNullOrEmpty(revaluateJson)) {
                JSONArray jArrForCurrency = new JSONArray(revaluateJson);
                for(int i=0;i<jArrForCurrency.length();i++){
                
                String revalcurrencyId = jArrForCurrency.getJSONObject(i).get("tocurrencyid").toString();
                String revalCurrencyRate = jArrForCurrency.getJSONObject(i).get("newexchangerate").toString();
                exchangeRateType = Integer.parseInt(jArrForCurrency.getJSONObject(i).get("exchangeratetype").toString());
                if (exchangeRateType == 1) {
                    revalCurrencyRate = Double.toString(1 / (Double.parseDouble(revalCurrencyRate)));
                }
                KwlReturnObject currObject = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), revalcurrencyId);
                KWLCurrency currency = (KWLCurrency) currObject.getEntityList().get(0);
                String revalaCurrencyCode = "";
                if (currency != null) {
                    revalaCurrencyCode = currency.getCurrencyCode();
                }
                if (accTypeId == Constants.CUSTOMER) {
                    HashMap<String, Object> requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
                    HashMap<String, Object> requestParamsForReval = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
                    requestParams.put("nondeleted", "true");
                    requestParams.put("deleted", "false");
                    requestParams.put(InvoiceConstants.accid, accId);
                    //requestParams.put(Constants.REQ_startdate, startdate);
                    requestParams.put(Constants.REQ_enddate, enddate);
                    requestParams.put("currencyfilterfortrans", revalcurrencyId);
                    /**
                     * Passing getRecordBasedOnJEDate as params to fetch record
                     * based on JE date rather than document creation date as documents are revalued on the basis of JE date
                     * ERM-655.
                     */
                    requestParams.put("getRecordBasedOnJEDate", true);

                    requestParamsForReval.put("nondeleted", "true");
                    requestParamsForReval.put("deleted", "false");
                    requestParamsForReval.put(InvoiceConstants.accid, accId);
                    //requestParamsForReval.put(Constants.REQ_startdate, startdate);
                    requestParamsForReval.put(Constants.REQ_enddate, enddate);
                    requestParamsForReval.put("currencyfilterfortrans", revalcurrencyId);
                    requestParamsForReval.put("isRevalue", true);
                    KwlReturnObject result = accInvoiceDAOobj.getInvoices(requestParams);
                    if (result.getEntityList() != null) {
                        Iterator itr1 = result.getEntityList().iterator();
                        while (itr1.hasNext()) {
                            Invoice invoice = (Invoice) itr1.next();
                            double newBaseAmount = 0;
                            if (Constants.InvoiceAmountDueFlag) {
                                List ll = accInvoiceCommon.getInvoiceDiscountAmountInfo(requestParams, invoice);
                                newBaseAmount = (Double) ll.get(0);
                            } else {
                                List ll = accInvoiceCommon.getAmountDue_Discount(requestParams, invoice);
                                newBaseAmount = (Double) ll.get(0);
                            }
                            double existingRate = 0;
                            try {
                                HashMap<String, Object> reqParams = new HashMap();
                                reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                                reqParams.put(Constants.companyKey, companyid);
                                existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(invoice.getID(), reqParams);
                            } catch (Exception ex) {
                                Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            String fromcurrencyid = invoice.getCurrency().getCurrencyID();
                            String tocurrencyid = invoice.getCompany().getCurrency().getCurrencyID();
//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());                        //Converting into base [PS]
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());                        //Converting into base [PS]
                            // KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                            double baseamountduenew = (Double) bAmt.getEntityList().get(0);
//                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, invoice.getJournalEntry().getEntryDate(), (Double.parseDouble(revalCurrencyRate)));
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, invoice.getCreationDate(), (Double.parseDouble(revalCurrencyRate)));
                            double revalamountduenew = (Double) bAmt.getEntityList().get(0);

                            double InvoiceGainLoss = 0.0;
                            //Call for reval
                            if (existingRate > 0) {
//                                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, invoice.getJournalEntry().getEntryDate(), existingRate);
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, invoice.getCreationDate(), existingRate);
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                            if (baseamountduenew > 0) {
                                JSONObject obj = new JSONObject();
                                obj.put("billno", invoice.getInvoiceNumber());
                                obj.put("entryno", invoice.getJournalEntry().getEntryNumber());
                                obj.put("type", InvoiceGainLoss > 0 ? "Cr" : "Dr");
//                                obj.put("date", df.format(invoice.getJournalEntry().getEntryDate()));
                                obj.put("date", df.format(invoice.getCreationDate()));
                                obj.put("revalrate", (Double.parseDouble(revalCurrencyRate)));
                                obj.put("currentrate", existingRate > 0 ? existingRate : invoice.getJournalEntry().getExternalCurrencyRate());
                                obj.put("revalcurrencycode", revalaCurrencyCode);
                                obj.put("documentamount", newBaseAmount);
                                obj.put("amount", baseamountduenew);
                                obj.put("revalamount", revalamountduenew);
                                obj.put("subprofitloss", authHandler.round(InvoiceGainLoss, companyid));
                                jArr.put(obj);
                            }
                        }
                    }
                    /**
                     * *
                     * Opening Customer Invoices
                     */
                    requestParams.put("customerid", accId);
                    requestParams.put("isAccountInvoices", true);
                    requestParams.put("excludeNormalInv", true);

                    result = accInvoiceDAOobj.getOpeningBalanceInvoices(requestParams);
                    Iterator itr1 = result.getEntityList().iterator();
                    while (itr1.hasNext()) {
                        Invoice invoice = (Invoice) itr1.next();

                        double transactionRate = 0.0;
                        boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                        Date transDate = invoice.getCreationDate();
                        transactionRate = invoice.getExchangeRateForOpeningTransaction();
                        double newBaseAmount = 0.0;
                        newBaseAmount = invoice.getOpeningBalanceAmountDue();
                        double existingRate = 0;
                        try {
                            HashMap<String, Object> reqParams = new HashMap();
                            reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                            reqParams.put(Constants.companyKey, companyid);
                            existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(invoice.getID(),reqParams);
                        } catch (Exception ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        String fromcurrencyid = invoice.getCurrency().getCurrencyID();
                        String tocurrencyid = invoice.getCompany().getCurrency().getCurrencyID();
                        double baseamountduenew = 0d;
                        if (Constants.OpeningBalanceBaseAmountFlag) {
                            baseamountduenew = invoice.getOpeningBalanceBaseAmountDue();
                        } else {
                            if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base [PS]
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base [PS]
                            }
                            // KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                            baseamountduenew = (Double) bAmt.getEntityList().get(0);
                        }
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, (Double.parseDouble(revalCurrencyRate)));
                        double revalamountduenew = (Double) bAmt.getEntityList().get(0);

                        double InvoiceGainLoss = 0.0;
                        double existingAmountdue = 0.0;
                        //Call for reval
                        if (existingRate > 0) {
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, existingRate);
                            baseamountduenew = (Double) bAmt.getEntityList().get(0);
                        }
                        InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                        if (baseamountduenew > 0) {
                            JSONObject obj = new JSONObject();
                            obj.put("billno", invoice.getInvoiceNumber());
                            obj.put("entryno", "-");
                            obj.put("type", InvoiceGainLoss > 0 ? "Cr" : "Dr");
                            obj.put("revalrate", (Double.parseDouble(revalCurrencyRate)));
                            obj.put("currentrate", existingRate > 0 ? existingRate : (invoice.isConversionRateFromCurrencyToBase() ? 1 / transactionRate : transactionRate));
                            obj.put("revalcurrencycode", revalaCurrencyCode);
                            obj.put("documentamount", newBaseAmount);
                            obj.put("date", df.format(invoice.getCreationDate()));
                            obj.put("amount", baseamountduenew);
                            obj.put("revalamount", revalamountduenew);
                            obj.put("subprofitloss", authHandler.round(InvoiceGainLoss,companyid));
                            jArr.put(obj);
                        }
                    }


                    /*
                     * Opening Customer Receipt
                     */
                    requestParams.clear();
                    requestParams = accReceiptController.getReceiptRequestMap(request);
                    requestParams.put("currencyfilterfortrans", revalcurrencyId);
                    requestParams.put("customerid", accId);
                    requestParams.put("isAccountReceipts", true);
                    // get opening balance receipts created from opening balance button.
                    result = accReceiptDao.getOpeningBalanceReceipts(requestParams);
                    if (result.getEntityList() != null) {
                        List<Receipt> receiptsList = result.getEntityList();
                        for (Receipt receipt : receiptsList) {
                        double transactionRate=0.0;
                            boolean isopeningBalanceInvoice = receipt.isIsOpeningBalenceReceipt();
                           Date transDate=receipt.getCreationDate();
                            transactionRate=receipt.getExchangeRateForOpeningTransaction();
                        double newBaseAmount=0.0;
                        newBaseAmount =receipt.getOpeningBalanceAmountDue();
                            double existingRate = 0;
                            try {
                                HashMap<String, Object> reqParams = new HashMap();
                                reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                                reqParams.put(Constants.companyKey, companyid);
                                existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(receipt.getID(),reqParams);
                            } catch (Exception ex) {
                                Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                            String tocurrencyid = receipt.getCompany().getCurrency().getCurrencyID();
                            double baseamountduenew = 0d;
                            if (Constants.OpeningBalanceBaseAmountFlag) {
                                baseamountduenew = receipt.getOpeningBalanceBaseAmountDue();
                            } else {
                                if (isopeningBalanceInvoice && receipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base [PS]
                                } else {
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base [PS]
                                }
                                // KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, (Double.parseDouble(revalCurrencyRate)));
                            double revalamountduenew = (Double) bAmt.getEntityList().get(0);

                            double InvoiceGainLoss = 0.0;
                            if (existingRate > 0) {
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, existingRate);
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                            if (baseamountduenew > 0) {
                                JSONObject obj = new JSONObject();
                                obj.put("billno", receipt.getReceiptNumber());
                                obj.put("entryno", "-");
                                obj.put("type", InvoiceGainLoss > 0 ? "Dr" : "Cr");
                                obj.put("revalrate", (Double.parseDouble(revalCurrencyRate)));
                                obj.put("currentrate", existingRate > 0 ? existingRate : (receipt.isConversionRateFromCurrencyToBase() ? 1 / transactionRate : transactionRate));
                                obj.put("revalcurrencycode", revalaCurrencyCode);
                                obj.put("documentamount", newBaseAmount);
                                obj.put("date", df.format(receipt.getCreationDate()));
                                obj.put("amount", baseamountduenew);
                                obj.put("revalamount", revalamountduenew);
                                obj.put("subprofitloss", authHandler.round((-(InvoiceGainLoss)),companyid));
                                jArr.put(obj);
                            }
                        }
                    }
                    /*
                     * Advance customer Receipt
                     */
                    requestParams.clear();
                    requestParams = accReceiptController.getReceiptRequestMap(request);
                    requestParams.put("currencyfilterfortrans", revalcurrencyId);
                    requestParams.put("customerid", accId);
                    requestParams.put("nondeleted", "true");
                    requestParams.put("deleted", "false");
                    requestParams.put(Constants.REQ_enddate, enddate);
                    requestParams.put("onlyAdvAmountDue", true);
                    /**
                     * Passing getRecordBasedOnJEDate as params to fetch record
                     * based on JE date rather than document creation date as documents are revalued on the basis of JE date
                     * ERM-655.
                     */
                    requestParams.put("isToFetchRecordLessEndDate", true);
                    requestParams.put("getRecordBasedOnJEDate", true);
                    result = accReceiptDao.getReceipts(requestParams);
                    List paymentsList = result.getEntityList();
                    Iterator itr = paymentsList.iterator();
                    while (itr.hasNext()) {
                        Object[] row = (Object[]) itr.next();
                        Receipt receipt = (Receipt) row[0];
                        Account acc = (Account) row[1];
                        double newBaseAmount = 0.0;
                        double existingRate = 0;
//                        Date transDate = receipt.getJournalEntry().getEntryDate();
                        Date transDate = receipt.getCreationDate();
                        double transactionRate = receipt.getJournalEntry().getExternalCurrencyRate();
                        try {
                            HashMap<String, Object> reqParams = new HashMap();
                            reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                            reqParams.put(Constants.companyKey, companyid);
                            existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(receipt.getID(),reqParams);
                        } catch (Exception ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
                            newBaseAmount += advanceDetail.getAmountDue();
                        }
                        String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                        String tocurrencyid = receipt.getCompany().getCurrency().getCurrencyID();
                        double baseamountduenew = 0d;
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base [PS]
                        baseamountduenew = (Double) bAmt.getEntityList().get(0);
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, (Double.parseDouble(revalCurrencyRate)));
                        double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                        double InvoiceGainLoss = 0;
                        if (existingRate > 0) {
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, existingRate);
                            baseamountduenew = (Double) bAmt.getEntityList().get(0);
                        }
                        InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                        if (baseamountduenew > 0) {
                            JSONObject obj = new JSONObject();
                            obj.put("billno", receipt.getReceiptNumber());
                            obj.put("entryno", receipt.getJournalEntry().getEntryNumber());
                            obj.put("type", InvoiceGainLoss > 0 ? "Dr" : "Cr");
                            obj.put("revalrate", (Double.parseDouble(revalCurrencyRate)));
                            obj.put("currentrate", existingRate > 0 ? existingRate : (receipt.isConversionRateFromCurrencyToBase() ? 1 / transactionRate : transactionRate));
                            obj.put("revalcurrencycode", revalaCurrencyCode);
                            obj.put("documentamount", newBaseAmount);
//                            obj.put("date", df.format(receipt.getJournalEntry().getEntryDate()));
                            obj.put("date", df.format(receipt.getCreationDate()));
                            obj.put("amount", baseamountduenew);
                            obj.put("revalamount", revalamountduenew);
                            obj.put("subprofitloss", authHandler.round((-(InvoiceGainLoss)),companyid));
                            jArr.put(obj);
                        }
                    }
                    /*
                     * Opening Credit Note
                     */
                    requestParams.clear();
                    requestParams = accCreditNoteController.getCreditNoteMap(request);
                    requestParams.put("nondeleted", "true");
                    requestParams.put("deleted", "false");
                    requestParams.put("excludeNormalInv", true);
                    requestParams.put("currencyfilterfortrans", revalcurrencyId);
                    requestParams.put("customerid", accId);
                    requestParams.put("isAccountCNs", true);
                    // get opening balance receipts created from opening balance button.
                    result = accCreditNoteDAOobj.getOpeningBalanceCNs(requestParams);
                    if (result.getEntityList() != null) {
                        List<CreditNote> list = result.getEntityList();
                        for (CreditNote creditNote : list) {
                            double transactionRate = 0.0;
                            boolean isopeningBalanceInvoice = creditNote.isIsOpeningBalenceCN();
                            Date transDate = creditNote.getCreationDate();
                            transactionRate = creditNote.getExchangeRateForOpeningTransaction();
                            double newBaseAmount = 0.0;
                            newBaseAmount = creditNote.getOpeningBalanceAmountDue();
                            double existingRate = 0;
                            try {
                                HashMap<String, Object> reqParams = new HashMap();
                                reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                                reqParams.put(Constants.companyKey, companyid);
                                existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(creditNote.getID(),reqParams);
                            } catch (Exception ex) {
                                Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            String fromcurrencyid = creditNote.getCurrency().getCurrencyID();
                            String tocurrencyid = creditNote.getCompany().getCurrency().getCurrencyID();
                            double baseamountduenew = 0d;
                            if (Constants.OpeningBalanceBaseAmountFlag) {
                                baseamountduenew = creditNote.getOpeningBalanceBaseAmountDue();
                            } else {
                                if (isopeningBalanceInvoice && creditNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base [PS]
                                } else {
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base [PS]
                                }
                                // KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, (Double.parseDouble(revalCurrencyRate)));
                            double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                            double InvoiceGainLoss = 0.0;
                            if (existingRate > 0) {
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, existingRate);
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                            if (baseamountduenew > 0) {
                                JSONObject obj = new JSONObject();
                                obj.put("billno", creditNote.getCreditNoteNumber());
                                obj.put("entryno", "-");
                                obj.put("type", InvoiceGainLoss > 0 ? "Dr" : "Cr");
                                obj.put("revalrate", (Double.parseDouble(revalCurrencyRate)));
                                obj.put("currentrate", existingRate > 0 ? existingRate : (creditNote.isConversionRateFromCurrencyToBase() ? 1 / transactionRate : transactionRate));
                                obj.put("revalcurrencycode", revalaCurrencyCode);
                                obj.put("documentamount", newBaseAmount);
                                obj.put("date", df.format(creditNote.getCreationDate()));
                                obj.put("amount", baseamountduenew);
                                obj.put("revalamount", revalamountduenew);
                                obj.put("subprofitloss", authHandler.round((-(InvoiceGainLoss)),companyid));
                                jArr.put(obj);
                            }
                        }
                    }

                    /*
                     * Opening Debit Note
                     */
                    requestParams.clear();
                    requestParams = accDebitNoteController.gettDebitNoteMap(request);
                    requestParams.put("nondeleted", "true");
                    requestParams.put("deleted", "false");
                    requestParams.put("excludeNormalInv", true);
                    requestParams.put("currencyfilterfortrans", revalcurrencyId);
                    requestParams.put("customerid", accId);
                    requestParams.put("isAccountDNs", true);
                    // get opening balance receipts created from opening balance button.
                    result = accDebitNoteobj.getOpeningBalanceCustomerDNs(requestParams);
                    if (result.getEntityList() != null) {
                        List<DebitNote> debitNotesList = result.getEntityList();
                        for (DebitNote debitNote : debitNotesList) {
                            double transactionRate = 0.0;
                            boolean isopeningBalanceInvoice = debitNote.isIsOpeningBalenceDN();
                            Date transDate = debitNote.getCreationDate();
                            transactionRate = debitNote.getExchangeRateForOpeningTransaction();
                            double newBaseAmount = 0.0;
                            newBaseAmount = debitNote.getOpeningBalanceAmountDue();
                            double existingRate = 0;
                            try {
                                HashMap<String, Object> reqParams = new HashMap();
                                reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                                reqParams.put(Constants.companyKey, companyid);
                                existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(debitNote.getID(),reqParams);
                            } catch (Exception ex) {
                                Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            String fromcurrencyid = debitNote.getCurrency().getCurrencyID();
                            String tocurrencyid = debitNote.getCompany().getCurrency().getCurrencyID();
                            double baseamountduenew = 0d;
                            if (Constants.OpeningBalanceBaseAmountFlag) {
                                baseamountduenew = debitNote.getOpeningBalanceBaseAmountDue();
                            } else {
                                if (isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base [PS]
                                } else {
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base [PS]
                                }
                                // KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, (Double.parseDouble(revalCurrencyRate)));
                            double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                            double InvoiceGainLoss = 0.0;
                            if (existingRate > 0) {
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, existingRate);
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                            if (baseamountduenew > 0) {
                                JSONObject obj = new JSONObject();
                                obj.put("billno", debitNote.getDebitNoteNumber());
                                obj.put("entryno", "-");
                                obj.put("type", InvoiceGainLoss > 0 ? "Cr" : "Dr");
                                obj.put("revalrate", (Double.parseDouble(revalCurrencyRate)));
                                obj.put("currentrate", existingRate > 0 ? existingRate : (debitNote.isConversionRateFromCurrencyToBase() ? 1 / transactionRate : transactionRate));
                                obj.put("revalcurrencycode", revalaCurrencyCode);
                                obj.put("documentamount", newBaseAmount);
                                obj.put("date", df.format(debitNote.getCreationDate()));
                                obj.put("amount", baseamountduenew);
                                obj.put("revalamount", revalamountduenew);
                                obj.put("subprofitloss", authHandler.round(InvoiceGainLoss,companyid));
                                jArr.put(obj);
                            }
                        }
                    }
                    /*
                     * Normal Debit Note for Customer
                     */
                    requestParams.clear();
                    requestParams = accDebitNoteController.gettDebitNoteMap(request);
                    requestParams.put("nondeleted", "true");
                    requestParams.put("deleted", "false");
                    requestParams.put("excludeNormalInv", true);
                    requestParams.put("currencyfilterfortrans", revalcurrencyId);
                    requestParams.put("customerid", accId);
                    requestParams.put("isAccountDNs", true);
                    requestParams.put("customerVendorID", accId);
                    requestParams.put("cntype", 4);
                    requestParams.put("isNewUI", false);
                    requestParams.put("isNoteForPayment", false);
                    // get opening balance receipts created from opening balance button.
                    requestParams.put("isNewUI", false);
                    requestParams.put("isNoteForPayment", false);
                    /**
                     * Passing getRecordBasedOnJEDate as params to fetch record
                     * based on JE date rather than document creation date as documents are revalued on the basis of JE date
                     * ERM-655.
                     */
                    requestParams.put("getRecordBasedOnJEDate", true);
                    requestParams.put("isToFetchRecordLessEndDate", true);
                    result = accDebitNoteobj.getDebitNoteMerged(requestParams);
                    if (result.getEntityList() != null) {
                        List debitNotesList = result.getEntityList();
                        Iterator DebitNoteitr = debitNotesList.iterator();
                        while (DebitNoteitr.hasNext()) {
                            Object[] row = (Object[]) DebitNoteitr.next();
                            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), (String) row[1]);
                            DebitNote debitNote = (DebitNote) resultObject.getEntityList().get(0);
                            JournalEntry je = debitNote.getJournalEntry();
                            double transactionRate = 0.0;
//                            Date transDate = debitNote.getJournalEntry().getEntryDate();
                            Date transDate = debitNote.getCreationDate();
                            transactionRate = debitNote.getJournalEntry().getExternalCurrencyRate();
                            double newBaseAmount = 0.0;
                            newBaseAmount = debitNote.isOtherwise() ? debitNote.getDnamountdue() : 0;
                            double existingRate = 0;
                            try {
                                HashMap<String, Object> reqParams = new HashMap();
                                reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                                reqParams.put(Constants.companyKey, companyid);
                                existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(debitNote.getID(),reqParams);
                            } catch (Exception ex) {
                                Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            String fromcurrencyid = debitNote.getCurrency().getCurrencyID();
                            String tocurrencyid = debitNote.getCompany().getCurrency().getCurrencyID();
                            double baseamountduenew = 0d;
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);
                            baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, (Double.parseDouble(revalCurrencyRate)));
                            double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                            double InvoiceGainLoss = 0.0;
                            if (existingRate > 0) {
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, existingRate);
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                            if (baseamountduenew > 0) {
                                JSONObject obj = new JSONObject();
                                obj.put("billno", debitNote.getDebitNoteNumber());
                                obj.put("entryno", je.getEntryNumber());
                                obj.put("type", InvoiceGainLoss > 0 ? "Cr" : "Dr");
                                obj.put("revalrate", (Double.parseDouble(revalCurrencyRate)));
                                obj.put("currentrate", existingRate > 0 ? existingRate : transactionRate);
                                obj.put("revalcurrencycode", revalaCurrencyCode);
                                obj.put("documentamount", newBaseAmount);
//                                obj.put("date", df.format(je.getEntryDate()));
                                obj.put("date", df.format(debitNote.getCreationDate()));
                                obj.put("amount", baseamountduenew);
                                obj.put("revalamount", revalamountduenew);
                                obj.put("subprofitloss", authHandler.round(InvoiceGainLoss,companyid));
                                jArr.put(obj);
                            }
                        }
                    }
                    /*
                     * Normal Credit Note for Customer
                     */
                    requestParams.clear();
                    requestParams = accCreditNoteController.getCreditNoteMap(request);
                    requestParams.put("nondeleted", "true");
                    requestParams.put("deleted", "false");
                    requestParams.put("excludeNormalInv", true);
                    requestParams.put("currencyfilterfortrans", revalcurrencyId);
                    requestParams.put("customerVendorID", accId);
                    requestParams.put("cntype", 1);
                    requestParams.put("isNewUI", false);
                    requestParams.put("isNoteForPayment", false);
                    /**
                     * Passing getRecordBasedOnJEDate as params to fetch record
                     * based on JE date rather than document creation date as documents are revalued on the basis of JE date
                     * ERM-655.
                     */
                    requestParams.put("getRecordBasedOnJEDate", true);
                    requestParams.put("isToFetchRecordLessEndDate", true);
                    // get opening balance receipts created from opening balance button.
                    result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                    if (result.getEntityList() != null) {
                        List creditNotesList = result.getEntityList();
                        Iterator creditNoteitr = creditNotesList.iterator();
                        while (creditNoteitr.hasNext()) {
                            Object[] row = (Object[]) creditNoteitr.next();
                            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) row[1]);
                            CreditNote creditNote = (CreditNote) resultObject.getEntityList().get(0);
                            JournalEntry je = creditNote.getJournalEntry();
                            double transactionRate = 0.0;
//                            Date transDate = je.getEntryDate();
                            Date transDate = creditNote.getCreationDate();
                            transactionRate = je.getExternalCurrencyRate();
                            double newBaseAmount = 0.0;
                            newBaseAmount = creditNote.isOtherwise() ? creditNote.getCnamountdue() : 0;
                            double existingRate = 0;
                            try {
                                HashMap<String, Object> reqParams = new HashMap();
                                reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                                reqParams.put(Constants.companyKey, companyid);
                                existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(creditNote.getID(),reqParams);
                            } catch (Exception ex) {
                                Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            String fromcurrencyid = creditNote.getCurrency().getCurrencyID();
                            String tocurrencyid = creditNote.getCompany().getCurrency().getCurrencyID();
                            double baseamountduenew = 0d;
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base [PS]
                            baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, (Double.parseDouble(revalCurrencyRate)));
                            double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                            double InvoiceGainLoss = 0.0;
                            if (existingRate > 0) {
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, existingRate);
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                            if (baseamountduenew > 0) {
                                JSONObject obj = new JSONObject();
                                obj.put("billno", creditNote.getCreditNoteNumber());
                                obj.put("entryno", je.getEntryNumber());
                                obj.put("type", InvoiceGainLoss > 0 ? "Dr" : "Cr");
                                obj.put("revalrate", (Double.parseDouble(revalCurrencyRate)));
                                obj.put("currentrate", existingRate > 0 ? existingRate : transactionRate);
                                obj.put("revalcurrencycode", revalaCurrencyCode);
                                obj.put("documentamount", newBaseAmount);
//                                obj.put("date", df.format(je.getEntryDate()));
                                obj.put("date", df.format(creditNote.getCreationDate()));
                                obj.put("amount", baseamountduenew);
                                obj.put("revalamount", revalamountduenew);
                                obj.put("subprofitloss", authHandler.round((-(InvoiceGainLoss)),companyid));
                                jArr.put(obj);
                            }
                        }
                    }


                } else if (accTypeId == Constants.VENDOR) {
                    HashMap<String, Object> requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
                    HashMap<String, Object> requestParamsForReval = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
                    requestParams.put("nondeleted", "true");
                    requestParams.put("deleted", "false");
                    requestParams.put(InvoiceConstants.accid, accId);
                    //requestParams.put(Constants.REQ_startdate, startdate);
                    requestParams.put(Constants.REQ_enddate, enddate);
                    requestParams.put("currencyfilterfortrans", revalcurrencyId);
                    /**
                     * Passing getRecordBasedOnJEDate as params to fetch record
                     * based on JE date rather than document creation date as documents are revalued on the basis of JE date
                     * ERM-655.
                     */
                    requestParams.put("getRecordBasedOnJEDate", true);

                    requestParamsForReval.put("nondeleted", "true");
                    requestParamsForReval.put("deleted", "false");
                    requestParamsForReval.put(InvoiceConstants.accid, accId);
                    //requestParamsForReval.put(Constants.REQ_startdate, startdate);
                    requestParamsForReval.put(Constants.REQ_enddate, enddate);
                    requestParamsForReval.put("currencyfilterfortrans", revalcurrencyId);
                    requestParamsForReval.put("isRevalue", true);
                    KwlReturnObject result = accGoodsReceiptDAOObj.getGoodsReceipts(requestParams);
                    if (result.getEntityList() != null) {
                        Iterator itr1 = result.getEntityList().iterator();
                        while (itr1.hasNext()) {
                            double newBaseAmount = 0.0;
                            GoodsReceipt goodsReceipt = (GoodsReceipt) itr1.next();
                            double existingRate = 0;
                            try {
                                HashMap<String, Object> reqParams = new HashMap();
                                reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                                reqParams.put(Constants.companyKey, companyid);
                                existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(goodsReceipt.getID(),reqParams);
                            } catch (Exception ex) {
                                Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            if (goodsReceipt.isIsExpenseType()) {
                                List ll = accGoodsReceiptCommon.getExpGRAmountDue(requestParams, goodsReceipt);
                                newBaseAmount = (Double) ll.get(1);
                            } else {
                                if (Constants.InvoiceAmountDueFlag) {
                                    List ll = accGoodsReceiptCommon.getInvoiceDiscountAmountInfo(requestParams, goodsReceipt);
                                    newBaseAmount = (Double) ll.get(1);
                                } else {
                                    List ll = accGoodsReceiptCommon.getGRAmountDue(requestParams, goodsReceipt);
                                    newBaseAmount = (Double) ll.get(1);
                                }
                            }
                            String fromcurrencyid = goodsReceipt.getCurrency().getCurrencyID();
                            String tocurrencyid = goodsReceipt.getCompany().getCurrency().getCurrencyID();
//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());                        //Converting into base [PS]
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());                        //Converting into base [PS]
                            //KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                            double baseamountduenew = (Double) bAmt.getEntityList().get(0);
//                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), (Double.parseDouble(revalCurrencyRate)));
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getCreationDate(), (Double.parseDouble(revalCurrencyRate)));
                            double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                            double InvoiceGainLoss = 0;
                            double existingAmountdue = 0.0;
                            //Call for reval
                            if (existingRate > 0) {
//                                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), existingRate);
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getCreationDate(), existingRate);
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                            if (baseamountduenew > 0) {
                                JSONObject obj = new JSONObject();
                                obj.put("billno", goodsReceipt.getGoodsReceiptNumber());
                                obj.put("entryno", goodsReceipt.getJournalEntry().getEntryNumber());
                                obj.put("type", InvoiceGainLoss > 0 ? "Dr" : "Cr");
                                obj.put("revalrate", (Double.parseDouble(revalCurrencyRate)));
                                obj.put("currentrate", existingRate > 0 ? existingRate : goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                obj.put("revalcurrencycode", revalaCurrencyCode);
                                obj.put("documentamount", newBaseAmount);
//                                obj.put("date", df.format(goodsReceipt.getJournalEntry().getEntryDate()));
                                obj.put("date", df.format(goodsReceipt.getCreationDate()));
                                obj.put("amount", baseamountduenew);
                                obj.put("revalamount", revalamountduenew);
                                obj.put("subprofitloss", authHandler.round(InvoiceGainLoss,companyid));
                                jArr.put(obj);
                            }

                        }

                    }

                    /*
                     * Opening Vendor Invoices
                     */
                    requestParams.put("vendorid", accId);
                    requestParams.put("isAccountInvoices", true);
                    requestParams.put("excludeNormalInv", true);

                    result = accGoodsReceiptDAOObj.getOpeningBalanceInvoices(requestParams);
                    if (result.getEntityList() != null) {
                        Iterator itr1 = result.getEntityList().iterator();
                        while (itr1.hasNext()) {
                            double newBaseAmount = 0.0;
                            KwlReturnObject getObj = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), (String) itr1.next());
                            GoodsReceipt goodsReceipt = (GoodsReceipt) getObj.getEntityList().get(0);
                            double existingRate = 0;
                            Date transDate = goodsReceipt.getCreationDate();
                            boolean isopeningBalanceInvoice = goodsReceipt.isIsOpeningBalenceInvoice();
                            double transactionRate = goodsReceipt.getExchangeRateForOpeningTransaction();
                            try {
                                HashMap<String, Object> reqParams = new HashMap();
                                reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                                reqParams.put(Constants.companyKey, companyid);
                                existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(goodsReceipt.getID(),reqParams);
                            } catch (Exception ex) {
                                Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            newBaseAmount = goodsReceipt.getOpeningBalanceAmountDue();
                            String fromcurrencyid = goodsReceipt.getCurrency().getCurrencyID();
                            String tocurrencyid = goodsReceipt.getCompany().getCurrency().getCurrencyID();
                            double baseamountduenew = 0d;
                            if (Constants.OpeningBalanceBaseAmountFlag) {
                                baseamountduenew = goodsReceipt.getOpeningBalanceBaseAmountDue();
                            } else {
                                if (isopeningBalanceInvoice && goodsReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base 
                                } else {
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base [PS]
                                }
                                //KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, (Double.parseDouble(revalCurrencyRate)));
                            double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                            double InvoiceGainLoss = 0;
                            double existingAmountdue = 0.0;
                            //Call for reval
                            if (existingRate > 0) {
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, existingRate);
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                            if (baseamountduenew > 0) {
                                JSONObject obj = new JSONObject();
                                obj.put("billno", goodsReceipt.getGoodsReceiptNumber());
                                obj.put("entryno", "-");
                                obj.put("type", InvoiceGainLoss > 0 ? "Dr" : "Cr");
                                obj.put("revalrate", (Double.parseDouble(revalCurrencyRate)));
                                obj.put("currentrate", existingRate > 0 ? existingRate : (goodsReceipt.isConversionRateFromCurrencyToBase() ? 1 / transactionRate : transactionRate));
                                obj.put("revalcurrencycode", revalaCurrencyCode);
                                obj.put("documentamount", newBaseAmount);
                                obj.put("date", df.format(goodsReceipt.getCreationDate()));
                                obj.put("amount", baseamountduenew);
                                obj.put("revalamount", revalamountduenew);
                                obj.put("subprofitloss", authHandler.round(InvoiceGainLoss,companyid));
                                jArr.put(obj);
                            }

                        }

                    }

                    /*
                     * Opening Vendor Payment
                     */
                    requestParams.clear();
                    requestParams = accVendorPaymentController.getPaymentMap(request);
                    requestParams.put("nondeleted", "true");
                    requestParams.put("deleted", "false");
                    requestParams.put(Constants.REQ_enddate, enddate);
                    requestParams.put("currencyfilterfortrans", revalcurrencyId);
                    requestParams.put("vendorid", accId);
                    requestParams.put("isAccountPayments", true);
                    requestParams.put("excludeNormalInv", true);

                    result = accVendorPaymentobj.getOpeningBalancePayments(requestParams);
                    if (result.getEntityList() != null) {
                        List<Payment> paymentsList = result.getEntityList();
                        for (Payment payment : paymentsList) {
                            double newBaseAmount = 0.0;
                            double existingRate = 0;
                            Date transDate = payment.getCreationDate();
                            boolean isopeningBalanceInvoice = payment.isIsOpeningBalencePayment();
                            double transactionRate = payment.getExchangeRateForOpeningTransaction();
                            try {
                                HashMap<String, Object> reqParams = new HashMap();
                                reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                                reqParams.put(Constants.companyKey, companyid);
                                existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(payment.getID(),reqParams);
                            } catch (Exception ex) {
                                Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            newBaseAmount = payment.getOpeningBalanceAmountDue();
                            String fromcurrencyid = payment.getCurrency().getCurrencyID();
                            String tocurrencyid = payment.getCompany().getCurrency().getCurrencyID();
                            double baseamountduenew = 0d;
                            if (Constants.OpeningBalanceBaseAmountFlag) {
                                baseamountduenew = payment.getOpeningBalanceBaseAmountDue();
                            } else {
                                if (isopeningBalanceInvoice && payment.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base 
                                } else {
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base [PS]
                                }
                                //KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, (Double.parseDouble(revalCurrencyRate)));
                            double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                            double InvoiceGainLoss = 0;
                            if (existingRate > 0) {
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, existingRate);
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                            if (baseamountduenew > 0) {
                                JSONObject obj = new JSONObject();
                                obj.put("billno", payment.getPaymentNumber());
                                obj.put("entryno", "-");
                                obj.put("type", InvoiceGainLoss > 0 ? "Cr" : "Dr");
                                obj.put("revalrate", (Double.parseDouble(revalCurrencyRate)));
                                obj.put("currentrate", existingRate > 0 ? existingRate : (payment.isConversionRateFromCurrencyToBase() ? 1 / transactionRate : transactionRate));
                                obj.put("revalcurrencycode", revalaCurrencyCode);
                                obj.put("documentamount", newBaseAmount);
                                obj.put("date", df.format(payment.getCreationDate()));
                                obj.put("amount", baseamountduenew);
                                obj.put("revalamount", revalamountduenew);
                                obj.put("subprofitloss", authHandler.round((-(InvoiceGainLoss)),companyid));
                                jArr.put(obj);
                            }

                        }

                    }
                    /*
                     * Opening Vendor Payment
                     */
                    requestParams.clear();
                    requestParams = accVendorPaymentController.getPaymentMap(request);
                    requestParams.put("nondeleted", "true");
                    requestParams.put("deleted", "false");
                    requestParams.put(Constants.REQ_enddate, enddate);
                    requestParams.put("currencyfilterfortrans", revalcurrencyId);
                    requestParams.put("vendorid", accId);
                    requestParams.put("onlyAdvAmountDue", true);
                    /**
                     * Passing getRecordBasedOnJEDate as params to fetch record
                     * based on JE date rather than document creation date as documents are revalued on the basis of JE date
                     * ERM-655.
                     */
                    requestParams.put("isToFetchRecordLessEndDate", true);
                    requestParams.put("getRecordBasedOnJEDate", true);
                    result = accVendorPaymentobj.getPayments(requestParams);
                    List paymentsList = result.getEntityList();
                    Iterator itr = paymentsList.iterator();
                    while (itr.hasNext()) {
                        Object[] row = (Object[]) itr.next();
                        Payment payment = (Payment) row[0];
                        Account acc = (Account) row[1];
                        double newBaseAmount = 0.0;
                        double existingRate = 0;
//                        Date transDate = payment.getJournalEntry().getEntryDate();
                        Date transDate = payment.getCreationDate();
                        double transactionRate = payment.getJournalEntry().getExternalCurrencyRate();
                        try {
                            HashMap<String, Object> reqParams = new HashMap();
                            reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                            reqParams.put(Constants.companyKey, companyid);
                            existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(payment.getID(),reqParams);
                        } catch (Exception ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        for (AdvanceDetail advanceDetail : payment.getAdvanceDetails()) {
                            newBaseAmount += advanceDetail.getAmountDue();
                        }
                        String fromcurrencyid = payment.getCurrency().getCurrencyID();
                        String tocurrencyid = payment.getCompany().getCurrency().getCurrencyID();
                        double baseamountduenew = 0d;
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base [PS]
                        baseamountduenew = (Double) bAmt.getEntityList().get(0);
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, (Double.parseDouble(revalCurrencyRate)));
                        double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                        double InvoiceGainLoss = 0;
                        if (existingRate > 0) {
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, existingRate);
                            baseamountduenew = (Double) bAmt.getEntityList().get(0);
                        }
                        InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                        if (baseamountduenew > 0) {
                            JSONObject obj = new JSONObject();
                            obj.put("billno", payment.getPaymentNumber());
                            obj.put("entryno", payment.getJournalEntry().getEntryNumber());
                            obj.put("type", InvoiceGainLoss > 0 ? "Cr" : "Dr");
                            obj.put("revalrate", (Double.parseDouble(revalCurrencyRate)));
                            obj.put("currentrate", existingRate > 0 ? existingRate : (payment.isConversionRateFromCurrencyToBase() ? 1 / transactionRate : transactionRate));
                            obj.put("revalcurrencycode", revalaCurrencyCode);
                            obj.put("documentamount", newBaseAmount);
//                            obj.put("date", df.format(payment.getJournalEntry().getEntryDate()));
                            obj.put("date", df.format(payment.getCreationDate()));
                            obj.put("amount", baseamountduenew);
                            obj.put("revalamount", revalamountduenew);
                            obj.put("subprofitloss", authHandler.round((-(InvoiceGainLoss)),companyid));
                            jArr.put(obj);
                        }
                    }
                    /*
                     * Opening Balance CNs
                     */
                    requestParams.clear();
                    requestParams = accCreditNoteController.getCreditNoteMap(request);
                    requestParams.put("nondeleted", "true");
                    requestParams.put("deleted", "false");
//            requestParams.put(Constants.REQ_startdate, startdate);
                    requestParams.put(Constants.REQ_enddate, enddate);
                    requestParams.put("currencyfilterfortrans", revalcurrencyId);
                    requestParams.put("vendorid", accId);
//            requestParams.put("isAccountCNs", true);

                    result = accCreditNoteDAOobj.getOpeningBalanceVendorCNs(requestParams);
                    if (result.getEntityList() != null) {
                        List<CreditNote> CreditNotesList = result.getEntityList();
                        for (CreditNote creditNote : CreditNotesList) {
                            double newBaseAmount = 0.0;
                            double existingRate = 0;
                            Date transDate = creditNote.getCreationDate();
                            boolean isopeningBalanceInvoice = creditNote.isIsOpeningBalenceCN();
                            double transactionRate = creditNote.getExchangeRateForOpeningTransaction();
                            try {
                                HashMap<String, Object> reqParams = new HashMap();
                                reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                                reqParams.put(Constants.companyKey, companyid);
                                existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(creditNote.getID(),reqParams);
                            } catch (Exception ex) {
                                Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            newBaseAmount = creditNote.getOpeningBalanceAmountDue();
                            String fromcurrencyid = creditNote.getCurrency().getCurrencyID();
                            String tocurrencyid = creditNote.getCompany().getCurrency().getCurrencyID();
                            double baseamountduenew = 0d;
                            if (Constants.OpeningBalanceBaseAmountFlag) {
                                baseamountduenew = creditNote.getOpeningBalanceBaseAmountDue();
                            } else {
                                if (isopeningBalanceInvoice && creditNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base 
                                } else {
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base [PS]
                                }
                                //KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, (Double.parseDouble(revalCurrencyRate)));
                            double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                            double InvoiceGainLoss = 0;
                            if (existingRate > 0) {
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, existingRate);
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                            if (baseamountduenew > 0) {
                                JSONObject obj = new JSONObject();
                                obj.put("billno", creditNote.getCreditNoteNumber());
                                obj.put("entryno", "-");
                                obj.put("type", InvoiceGainLoss > 0 ? "Cr" : "Dr");
                                obj.put("revalrate", (Double.parseDouble(revalCurrencyRate)));
                                obj.put("currentrate", existingRate > 0 ? existingRate : (creditNote.isConversionRateFromCurrencyToBase() ? 1 / transactionRate : transactionRate));
                                obj.put("revalcurrencycode", revalaCurrencyCode);
                                obj.put("documentamount", newBaseAmount);
                                obj.put("date", df.format(creditNote.getCreationDate()));
                                obj.put("amount", baseamountduenew);
                                obj.put("revalamount", revalamountduenew);
                                obj.put("subprofitloss", authHandler.round(InvoiceGainLoss,companyid));
                                jArr.put(obj);
                            }

                        }

                    }
                    /*
                     * Opening Debit Note
                     */
                    /*
                     * Opening Balance DNs
                     */
                    requestParams.clear();
                    requestParams = accDebitNoteController.gettDebitNoteMap(request);
                    requestParams.put("nondeleted", "true");
                    requestParams.put("deleted", "false");
//            requestParams.put(Constants.REQ_startdate, startdate);
                    requestParams.put(Constants.REQ_enddate, enddate);
                    requestParams.put("currencyfilterfortrans", revalcurrencyId);
                    requestParams.put("vendorid", accId);
                    requestParams.put("excludeNormalInv", true);

                    result = accDebitNoteobj.getOpeningBalanceDNs(requestParams);
                    if (result.getEntityList() != null) {
                        List<DebitNote> debitNotesList = result.getEntityList();
                        for (DebitNote debitNote : debitNotesList) {
                            double newBaseAmount = 0.0;
                            double existingRate = 0;
                            Date transDate = debitNote.getCreationDate();
                            boolean isopeningBalanceInvoice = debitNote.isIsOpeningBalenceDN();
                            double transactionRate = debitNote.getExchangeRateForOpeningTransaction();
                            try {
                                HashMap<String, Object> reqParams = new HashMap();
                                reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                                reqParams.put(Constants.companyKey, companyid);
                                existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(debitNote.getID(),reqParams);
                            } catch (Exception ex) {
                                Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            newBaseAmount = debitNote.getOpeningBalanceAmountDue();
                            String fromcurrencyid = debitNote.getCurrency().getCurrencyID();
                            String tocurrencyid = debitNote.getCompany().getCurrency().getCurrencyID();
                            double baseamountduenew = 0d;
                            if (Constants.OpeningBalanceBaseAmountFlag) {
                                baseamountduenew = debitNote.getOpeningBalanceBaseAmountDue();
                            } else {
                                if (isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base 
                                } else {
                                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base [PS]
                                }
                                //KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, (Double.parseDouble(revalCurrencyRate)));
                            double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                            double InvoiceGainLoss = 0;
                            if (existingRate > 0) {
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, existingRate);
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                            if (baseamountduenew > 0) {
                                JSONObject obj = new JSONObject();
                                obj.put("billno", debitNote.getDebitNoteNumber());
                                obj.put("entryno", "-");
                                obj.put("type", InvoiceGainLoss > 0 ? "Cr" : "Dr");
                                obj.put("revalrate", (Double.parseDouble(revalCurrencyRate)));
                                obj.put("currentrate", existingRate > 0 ? existingRate : (debitNote.isConversionRateFromCurrencyToBase() ? 1 / transactionRate : transactionRate));
                                obj.put("revalcurrencycode", revalaCurrencyCode);
                                obj.put("documentamount", newBaseAmount);
                                obj.put("date", df.format(debitNote.getCreationDate()));
                                obj.put("amount", baseamountduenew);
                                obj.put("revalamount", revalamountduenew);
                                obj.put("subprofitloss", authHandler.round((-(InvoiceGainLoss)),companyid));
                                jArr.put(obj);
                            }

                        }

                    }
                    /*
                     * Normal Debit Note agiant Vendor
                     */
                    requestParams.clear();
                    requestParams = accDebitNoteController.gettDebitNoteMap(request);
                    requestParams.put("nondeleted", "true");
                    requestParams.put("deleted", "false");
                    requestParams.put(Constants.REQ_enddate, enddate);
                    requestParams.put("currencyfilterfortrans", revalcurrencyId);
                    requestParams.put("customerVendorID", accId);
                    requestParams.put("cntype", 1); //Normal Debit Note agianst Vendor
                    requestParams.put("isNewUI", false);
                    requestParams.put("isNoteForPayment", false);
                    /**
                     * Passing getRecordBasedOnJEDate as params to fetch record
                     * based on JE date rather than document creation date as documents are revalued on the basis of JE date
                     * ERM-655.
                     */
                    requestParams.put("getRecordBasedOnJEDate", true);
                    requestParams.put("isToFetchRecordLessEndDate", true);
                    // get opening balance receipts created from opening balance button.
                    result = accDebitNoteobj.getDebitNoteMerged(requestParams);
                    if (result.getEntityList() != null) {
                        List debitNotesList = result.getEntityList();
                        Iterator debitNoteitr = debitNotesList.iterator();
                        while (debitNoteitr.hasNext()) {
                            Object[] row = (Object[]) debitNoteitr.next();
                            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), (String) row[1]);
                            DebitNote debitNote = (DebitNote) resultObject.getEntityList().get(0);
                            JournalEntry je = debitNote.getJournalEntry();
                            double newBaseAmount = 0.0;
                            double existingRate = 0;
//                            Date transDate = je.getEntryDate();
                            Date transDate = debitNote.getCreationDate();
                            double transactionRate = je.getExternalCurrencyRate();
                            try {
                                HashMap<String, Object> reqParams = new HashMap();
                                reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                                reqParams.put(Constants.companyKey, companyid);
                                existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(debitNote.getID(),reqParams);
                            } catch (Exception ex) {
                                Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            newBaseAmount = debitNote.getDnamountdue();
                            String fromcurrencyid = debitNote.getCurrency().getCurrencyID();
                            String tocurrencyid = debitNote.getCompany().getCurrency().getCurrencyID();
                            double baseamountduenew = 0d;
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base [PS]
                            baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, (Double.parseDouble(revalCurrencyRate)));
                            double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                            double InvoiceGainLoss = 0;
                            if (existingRate > 0) {
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, existingRate);
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                            if (baseamountduenew > 0) {
                                JSONObject obj = new JSONObject();
                                obj.put("billno", debitNote.getDebitNoteNumber());
                                obj.put("entryno", je.getEntryNumber());
                                obj.put("type", InvoiceGainLoss > 0 ? "Cr" : "Dr");
                                obj.put("revalrate", (Double.parseDouble(revalCurrencyRate)));
                                obj.put("currentrate", existingRate > 0 ? existingRate : transactionRate);
                                obj.put("revalcurrencycode", revalaCurrencyCode);
                                obj.put("documentamount", newBaseAmount);
//                                obj.put("date", df.format(je.getEntryDate()));
                                obj.put("date", df.format(debitNote.getCreationDate()));
                                obj.put("amount", baseamountduenew);
                                obj.put("revalamount", revalamountduenew);
                                obj.put("subprofitloss", authHandler.round((-(InvoiceGainLoss)),companyid));
                                jArr.put(obj);
                            }

                        }

                    }
                    /*
                     * Normal Credit Note for Vendor
                     */
                    requestParams.clear();
                    requestParams = accCreditNoteController.getCreditNoteMap(request);
                    requestParams.put("nondeleted", "true");
                    requestParams.put("deleted", "false");
                    requestParams.put(Constants.REQ_enddate, enddate);
                    requestParams.put("currencyfilterfortrans", revalcurrencyId);
                    requestParams.put("customerVendorID", accId);
                    requestParams.put("cntype", 4); //Credit Note agianst Vendor
                    requestParams.put("isNewUI", false);
                    requestParams.put("isNoteForPayment", false);
                    /**
                     * Passing getRecordBasedOnJEDate as params to fetch record
                     * based on JE date rather than document creation date as documents are revalued on the basis of JE date
                     * ERM-655.
                     */
                    requestParams.put("getRecordBasedOnJEDate", true);
                    requestParams.put("isToFetchRecordLessEndDate", true);
                    // get opening balance receipts created from opening balance button.
                    result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                    if (result.getEntityList() != null) {
                        List creditNotesList = result.getEntityList();
                        Iterator creditNoteitr = creditNotesList.iterator();
                        while (creditNoteitr.hasNext()) {
                            Object[] row = (Object[]) creditNoteitr.next();
                            KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) row[1]);
                            CreditNote creditNote = (CreditNote) resultObject.getEntityList().get(0);
                            JournalEntry je = creditNote.getJournalEntry();
                            double newBaseAmount = 0.0;
                            double existingRate = 0;
//                            Date transDate = je.getEntryDate();
                            Date transDate = creditNote.getCreationDate();
                            double transactionRate = je.getExternalCurrencyRate();
                            try {
                                HashMap<String, Object> reqParams = new HashMap();
                                reqParams.put(Constants.REQ_enddate, df.parse(enddate));
                                reqParams.put(Constants.companyKey, companyid);
                                existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(creditNote.getID(),reqParams);
                            } catch (Exception ex) {
                                Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            newBaseAmount = creditNote.isOtherwise() ? creditNote.getCnamountdue() : 0;
                            String fromcurrencyid = creditNote.getCurrency().getCurrencyID();
                            String tocurrencyid = creditNote.getCompany().getCurrency().getCurrencyID();
                            double baseamountduenew = 0d;
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, transDate, transactionRate);                        //Converting into base [PS]
                            baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, (Double.parseDouble(revalCurrencyRate)));
                            double revalamountduenew = (Double) bAmt.getEntityList().get(0);
                            double InvoiceGainLoss = 0;
                            if (existingRate > 0) {
                                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, existingRate);
                                baseamountduenew = (Double) bAmt.getEntityList().get(0);
                            }
                            InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                            if (baseamountduenew > 0) {
                                JSONObject obj = new JSONObject();
                                obj.put("billno", creditNote.getCreditNoteNumber());
                                obj.put("entryno", je.getEntryNumber());
                                obj.put("type", InvoiceGainLoss > 0 ? "Dr" : "Cr");
                                obj.put("revalrate", (Double.parseDouble(revalCurrencyRate)));
                                obj.put("currentrate", existingRate > 0 ? existingRate : transactionRate);
                                obj.put("revalcurrencycode", revalaCurrencyCode);
                                obj.put("documentamount", newBaseAmount);
//                                obj.put("date", df.format(je.getEntryDate()));
                                obj.put("date", df.format(creditNote.getCreationDate()));
                                obj.put("amount", baseamountduenew);
                                obj.put("revalamount", revalamountduenew);
                                obj.put("subprofitloss", authHandler.round(InvoiceGainLoss,companyid));
                                jArr.put(obj);
                            }

                        }

                    }
                }
            }
            }     
           jobj.put("data", jArr);
        } catch (JSONException ex) {
            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getAccountJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }
     public ModelAndView ReevaluationHistoryReport(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "", jeid = "";
        boolean issuccess = false;
        long count=0;
        try {            
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String startdate = request.getParameter("startdate");
            String enddate = request.getParameter("enddate");
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            Map<String, Object> RevalDataMap = new HashMap<String, Object>();
            RevalDataMap.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                RevalDataMap.put("start", start);
                RevalDataMap.put("limit", limit);
            }
            RevalDataMap.put("startdate", startdate);
            RevalDataMap.put("enddate", enddate);
            RevalDataMap.put(Constants.df, authHandler.getDateOnlyFormat());
            KwlReturnObject result =accJournalEntryobj.ReevaluationHistoryReport(RevalDataMap);
            List<RevaluationHistory> list =(List<RevaluationHistory>)result.getEntityList();
            JSONArray DataJArr = ReevaluationHistoryReportJson(request, list);
            count=result.getRecordTotalCount();
            jobj.put("data", DataJArr);
            jobj.put("count", count);
            issuccess = true;
            
         } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     public JSONArray ReevaluationHistoryReportJson(HttpServletRequest request, List<RevaluationHistory> list) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        Map<String, Object> filterMap = new HashMap();
        KWLCurrency currency = null;
        String[] params = new String[2];
        try {
            DateFormat df = authHandler.getDateFormatter(request);
            for (RevaluationHistory revalHistory : list) {
                JSONObject obj = new JSONObject();
                KwlReturnObject curresult = null;
                String id = revalHistory.getInvoiceid();
                filterMap.put("id", id);
                obj.put("amount", revalHistory.getAmount());
                obj.put("evalrate", revalHistory.getEvalrate());
                obj.put("entrydate", df.format(revalHistory.getEvaldate()));
                int moduleid = revalHistory.getModuleid();
                Object[] docObj = null;
                switch (moduleid) {
                    case 2: // Sales Invoice
                    case 112: // Opening Sales Invoice.
                        curresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), id);
                        Invoice invoice = (Invoice) curresult.getEntityList().get(0);
                        if (!StringUtil.isNullObject(invoice)) {
                            obj.put("billno", invoice.getInvoiceNumber());
                            obj.put("currencyid", StringUtil.isNullObject(invoice.getJournalEntry()) ? invoice.getCurrency().getCurrencyID() : invoice.getJournalEntry().getCurrency().getCurrencyID());
                            obj.put("currencysymbol", StringUtil.isNullObject(invoice.getJournalEntry()) ? invoice.getCurrency().getSymbol() : invoice.getJournalEntry().getCurrency().getSymbol());
                            obj.put("currencyname", invoice.getCurrency().getName());
                        }
                        break;
//                    case 3:
//                        curresult = accountingHandlerDAOobj.getObject(BillingInvoice.class.getName(), id);
//                        BillingInvoice billingInvoice = (BillingInvoice) curresult.getEntityList().get(0);
//                        if (!StringUtil.isNullObject(billingInvoice)) {
//                            obj.put("billno", billingInvoice.getBillingInvoiceNumber());
//                            obj.put("currencyid", billingInvoice.getCurrency().getCurrencyID());
//                            obj.put("currencysymbol", StringUtil.isNullObject(billingInvoice.getJournalEntry()) ? billingInvoice.getCurrency().getSymbol() : billingInvoice.getJournalEntry().getCurrency().getSymbol());
//                            obj.put("currencyname", StringUtil.isNullObject(billingInvoice.getJournalEntry()) ? billingInvoice.getCurrency().getName() : billingInvoice.getJournalEntry().getCurrency().getName());
//                        }
//                        break;
                    case 6: // for Purchase Invoice
                    case 113: // opening Purchase Invoice
                        curresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), id);
                        GoodsReceipt goodsReceipt = (GoodsReceipt) curresult.getEntityList().get(0);
                        if (!StringUtil.isNullObject(goodsReceipt)) {
                            obj.put("billno", goodsReceipt.getGoodsReceiptNumber());
                            obj.put("currencysymbol", StringUtil.isNullObject(goodsReceipt.getJournalEntry()) ? goodsReceipt.getCurrency().getSymbol() : goodsReceipt.getJournalEntry().getCurrency().getSymbol());
                            obj.put("currencyid", goodsReceipt.getCurrency().getCurrencyID());
                            obj.put("currencyname", StringUtil.isNullObject(goodsReceipt.getJournalEntry()) ? goodsReceipt.getCurrency().getName() : goodsReceipt.getJournalEntry().getCurrency().getName());
                        }
                        break;
//                    case 7:
//                        curresult = accountingHandlerDAOobj.getObject(BillingGoodsReceipt.class.getName(), id);
//                        BillingGoodsReceipt billingGoodsReceipt = (BillingGoodsReceipt) curresult.getEntityList().get(0);
//                        if (!StringUtil.isNullObject(billingGoodsReceipt)) {
//                            obj.put("billno", billingGoodsReceipt.getBillingGoodsReceiptNumber());
//                            obj.put("currencyid", billingGoodsReceipt.getCurrency().getCurrencyID());
//                            obj.put("currencysymbol", StringUtil.isNullObject(billingGoodsReceipt.getJournalEntry()) ? billingGoodsReceipt.getCurrency().getSymbol() : billingGoodsReceipt.getJournalEntry().getCurrency().getSymbol());
//                            obj.put("currencyname", StringUtil.isNullObject(billingGoodsReceipt.getJournalEntry()) ? billingGoodsReceipt.getCurrency().getName() : billingGoodsReceipt.getJournalEntry().getCurrency().getName());
//                        }
//                        break;
                    case 24://for bank and cash in hand type is not handled for account revaluation report. reason- single je is posted.
                        curresult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), id);
                        JournalEntry journalEntry = (JournalEntry) curresult.getEntityList().get(0);
                        if (journalEntry != null) {
                            obj.put("currencyid", journalEntry.getCurrency().getCurrencyID());
                            obj.put("billno", journalEntry.getEntryNumber());
                            obj.put("currencysymbol", journalEntry.getCurrency().getSymbol());
                            obj.put("currencyname", journalEntry.getCurrency().getName());
                        }

                        break;

                    case 10: // for Debit Note
                    case 118: // Opening Customer Debit Note
                    case 119: // Opening Vendor Debit Note
                        params[0] = Constants.debitNoteNumber;
                        params[1] = Constants.currency;
                        docObj = (Object[]) kwlCommonTablesDAOObj.getRequestedObjectFields(DebitNote.class, params, filterMap);
                        break;
                    case 12: // for credit note
                    case 116: // Opening Customer Credit Note
                    case 117: // Opening Vendor Credit Note
                        params[0] = Constants.creditNoteNumber;
                        params[1] = Constants.currency;
                        docObj = (Object[]) kwlCommonTablesDAOObj.getRequestedObjectFields(CreditNote.class, params, filterMap);
                        break;
                    case 14: // for Make Payment note
                    case 115: // Opening Payment
                        params[0] = Constants.paymentNumber;
                        params[1] = Constants.currency;
                        docObj = (Object[]) kwlCommonTablesDAOObj.getRequestedObjectFields(Payment.class, params, filterMap);
                        break;
                    case 16: // for Receipt Note
                    case 114: // Opening Receipt Note
                        params[0] = Constants.receiptNumber;
                        params[1] = Constants.currency;
                        docObj = (Object[]) kwlCommonTablesDAOObj.getRequestedObjectFields(Receipt.class, params, filterMap);
                        break;
                }
                /**
                 * to use of Hibernate Projection for getting required fields
                 * from class name.
                 */
                if (!StringUtil.isNullObject(docObj)) {
                    currency = (KWLCurrency) docObj[1];
                    if (!StringUtil.isNullObject(currency)) {
                        obj.put(Constants.billno, docObj[0]);
                        obj.put(Constants.currencyKey, currency.getCurrencyID());
                        obj.put(Constants.currencysymbol, currency.getSymbol());
                        obj.put(Constants.currencyname, currency.getName());
                    }
                }
                jArr.put(obj);
            }

        }  catch (Exception ex) {
            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    private double[] getVendorBalanceDetails(HttpServletRequest request, String customerid, String accountid, String startdate, String enddate, String revalcurrencyId, String revalCurrencyRate, String revalid, User userDetails,Company company) throws ServiceException, SessionExpiredException {
        double returnBalance[] = {0, 0, 0, 0,0};
        try {

            HashMap<String, Object> requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            HashMap<String, Object> requestParamsForReval = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            String tocurrencyid = sessionHandlerImpl.getCurrencyID(request);
            boolean isExport = !StringUtil.isNullObject(request.getAttribute("isExport"))?Boolean.parseBoolean(request.getAttribute("isExport").toString()):false;
            RevaluationHistory revalueationHistory = null;
            requestParams.put("nondeleted", "true");
            requestParams.put("deleted", "false");
            requestParams.put(InvoiceConstants.accid, customerid);
            requestParams.put(Constants.REQ_startdate, startdate);
            requestParams.put(Constants.REQ_enddate, enddate);
            requestParams.put("currencyfilterfortrans", revalcurrencyId);
             /**
             * Passing getRecordBasedOnJEDate as params to fetch record based on JE date rather than document creation date as documents are revalued on the basis of JE date
             * ERM-655.
             */
            requestParams.put("getRecordBasedOnJEDate", true);            

            requestParamsForReval.put("nondeleted", "true");
            requestParamsForReval.put("deleted", "false");
            requestParamsForReval.put(InvoiceConstants.accid, customerid);
            requestParamsForReval.put(Constants.REQ_startdate, startdate);
            requestParamsForReval.put(Constants.REQ_enddate, enddate);
            requestParamsForReval.put("currencyfilterfortrans", revalcurrencyId);
            requestParamsForReval.put("isRevalue", true);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            double amountdue = 0;
            double profitloss = 0;
            double newBaseAmount = 0;
            String companyid = company.getCompanyID();
            double baseamountdue = 0;
            double baseamountduenew = 0;
            double revalamountdue = 0;
            double revalamountduenew = 0;
            double revalamountdueold = 0;
            String oldRevalId = "";
            KwlReturnObject result = accGoodsReceiptDAOObj.getGoodsReceipts(requestParams);
            if (result.getEntityList() != null) {
                List goodsReceiptList = result.getEntityList();
                for (Object object : goodsReceiptList) {
                    GoodsReceipt goodsReceipt = (GoodsReceipt) object;
                    if (goodsReceipt.isIsExpenseType()) {
                        List ll = accGoodsReceiptCommon.getExpGRAmountDue(requestParams, goodsReceipt);
                        amountdue = amountdue + (Double) ll.get(1);
                        newBaseAmount = (Double) ll.get(1);
                    } else {
                        if (Constants.InvoiceAmountDueFlag) {
                            List ll = accGoodsReceiptCommon.getInvoiceDiscountAmountInfo(requestParams, goodsReceipt);
                            amountdue = amountdue + (Double) ll.get(1);
                            newBaseAmount = (Double) ll.get(1);
                        } else {
                            List ll = accGoodsReceiptCommon.getGRAmountDue(requestParams, goodsReceipt);
                            amountdue = amountdue + (Double) ll.get(1);
                            newBaseAmount = (Double) ll.get(1);
                        }
                    }
                    if (newBaseAmount <= 0) {
                        continue;
                    }
                    revalueationHistory = new RevaluationHistory();
                    double existingRate = 0;
                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                    double newCurrentRate = 0.0;
                    try {
                        invoiceId.put("invoiceid", goodsReceipt.getID());
                        invoiceId.put("companyid", companyid);
                        result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                        RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                        if (history != null) {
                            DateFormat dateFormat = (requestParams.containsKey(Constants.df) && requestParams.get(Constants.df) != null) ? (DateFormat) requestParams.get(Constants.df) : df;
                            Date lastRevalDate = history.getEvaldate();
                            Date currentRevalDate = dateFormat.parse(enddate);
                            if (!StringUtil.isNullOrEmpty(enddate) && !StringUtil.isNullObject(history.getEvaldate()) && (DateUtils.isSameDay(lastRevalDate, currentRevalDate))) {
                                existingRate = history.getCurrentRate();
                            } else {
                                existingRate = history.getEvalrate();
                            }
                            newCurrentRate = existingRate;
                        }
//                        existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(goodsReceipt.getID());
                    } catch (Exception ex) {
                        Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    JournalEntry entry = goodsReceipt.getJournalEntry();
                    double exchangeRate = entry.getExternalCurrencyRate();
//                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
//                    invoiceId.put("invoiceid", goodsReceipt.getID());
//                    invoiceId.put("companyid", companyid);
                    //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
//                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null && history.isIsRealised()) {
                        exchangeRate = history.getEvalrate();
                    }
                    newCurrentRate = newCurrentRate == 0 ? exchangeRate : newCurrentRate;
                    String fromcurrencyid = goodsReceipt.getCurrency().getCurrencyID();
//                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, entry.getEntryDate(), exchangeRate);                        //Converting into base [PS]
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, goodsReceipt.getCreationDate(), exchangeRate);                        //Converting into base [PS]
                    //KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                    baseamountduenew = (Double) bAmt.getEntityList().get(0);
                    baseamountdue = baseamountdue + baseamountduenew;

//                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, entry.getEntryDate(), (Double.parseDouble(revalCurrencyRate)));
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getCreationDate(), (Double.parseDouble(revalCurrencyRate)));
                    revalamountduenew = (Double) bAmt.getEntityList().get(0);
                    revalamountdue = revalamountdue + revalamountduenew;
                    double InvoiceGainLoss = 0;
                    //Call for reval
                    if (existingRate > 0) {
//                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, entry.getEntryDate(), existingRate);
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getCreationDate(), existingRate);
                        double existingAmountdue = (Double) bAmt.getEntityList().get(0);
                        revalamountdueold = revalamountdueold + existingAmountdue;
                        InvoiceGainLoss = (revalamountduenew - existingAmountdue);;
                        profitloss = profitloss + authHandler.round((revalamountduenew - existingAmountdue),companyid);
                    } else {
                        InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                        profitloss = profitloss + authHandler.round((revalamountduenew - baseamountduenew),companyid);
                    }
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null) {
                        history.setIsRealised(true);
                        oldRevalId = history.getRevalid();
                        try {
                            KwlReturnObject jeresult = accJournalEntryobj.addRevalHistory(history);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!isExport) {
                        revalueationHistory.setRevalid(revalid);
                        revalueationHistory.setInvoiceid(goodsReceipt.getID());
                        revalueationHistory.setModuleid(Constants.Acc_Vendor_Invoice_ModuleId);
                        revalueationHistory.setEvalrate(Double.parseDouble(revalCurrencyRate));
                        revalueationHistory.setAmount(newBaseAmount);
                        revalueationHistory.setEvaldate(df.parse(enddate));
                        revalueationHistory.setUserid(userDetails);
                        revalueationHistory.setCurrency(goodsReceipt.getCurrency());
                        revalueationHistory.setCompany(company);
                        revalueationHistory.setProfitloss(InvoiceGainLoss);
                        revalueationHistory.setAccountid(accountid);
                        revalueationHistory.setCurrentRate(newCurrentRate);
                        try {
                            accJournalEntryobj.addRevalHistory(revalueationHistory);
                        } catch (Exception ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }
               
            }
            /*
             *  Opening Vendor Invoices
             */
            requestParams.put("vendorid", customerid);
            requestParams.put("accountId", accountid);
            requestParams.put("isAccountInvoices", true);
            requestParams.put("onlyamountdue", true);
            requestParams.put("excludeNormalInv", true);
            
            result = accGoodsReceiptDAOObj.getOpeningBalanceInvoices(requestParams);
            if (result.getEntityList() != null) {
                List goodsReceiptList = result.getEntityList();
                for (Object object : goodsReceiptList) {
                    KwlReturnObject getObj = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), (String) object);
                    GoodsReceipt goodsReceipt = (GoodsReceipt) getObj.getEntityList().get(0);
                    newBaseAmount = goodsReceipt.getOpeningBalanceAmountDue();
                    amountdue = amountdue + newBaseAmount;

                    if (newBaseAmount <= 0) {
                        continue;
                    }
                    revalueationHistory = new RevaluationHistory();
                    double existingRate = 0;
                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                    double newCurrentRate = 0.0;
                    try {
                        invoiceId.put("invoiceid", goodsReceipt.getID());
                        invoiceId.put("companyid", companyid);
                        result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                        RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                        if(history!=null){
                            DateFormat dateFormat = (requestParams.containsKey(Constants.df) && requestParams.get(Constants.df) != null) ? (DateFormat) requestParams.get(Constants.df) : df;
                            Date lastRevalDate = history.getEvaldate();
                            Date currentRevalDate = dateFormat.parse(enddate);
                            if (!StringUtil.isNullOrEmpty(enddate) && !StringUtil.isNullObject(history.getEvaldate()) && (DateUtils.isSameDay(lastRevalDate, currentRevalDate))) {
                                existingRate = history.getCurrentRate();
                            } else {
                                existingRate = history.getEvalrate();
                            }
                            newCurrentRate = existingRate;
                        }
//                        existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(goodsReceipt.getID());
                    } catch (Exception ex) {
                        Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    double exchangeRate = goodsReceipt.getExchangeRateForOpeningTransaction();
                    boolean isopeningBalanceInvoice = goodsReceipt.isIsOpeningBalenceInvoice();
                    if (isopeningBalanceInvoice && goodsReceipt.isConversionRateFromCurrencyToBase() && newCurrentRate == 0) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        newCurrentRate = 1 / exchangeRate;
                    }
//                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
//                    invoiceId.put("invoiceid", goodsReceipt.getID());
//                    invoiceId.put("companyid", companyid);
                    //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
//                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null && history.isIsRealised()) {
                        exchangeRate = history.getEvalrate();
                    }
                    newCurrentRate = newCurrentRate == 0 ? exchangeRate : newCurrentRate;
                    String fromcurrencyid = goodsReceipt.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    if (Constants.OpeningBalanceBaseAmountFlag) {
                        baseamountduenew = goodsReceipt.getOpeningBalanceBaseAmountDue();
                    } else {
                        if (history == null && isopeningBalanceInvoice && goodsReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, newBaseAmount, fromcurrencyid, goodsReceipt.getCreationDate(), exchangeRate);                        //Converting into base [PS]
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, goodsReceipt.getCreationDate(), exchangeRate);                        //Converting into base [PS]
                        }
                        //KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                        baseamountduenew = (Double) bAmt.getEntityList().get(0);
                    }
                    baseamountdue = baseamountdue + baseamountduenew;

                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getCreationDate(), (Double.parseDouble(revalCurrencyRate)));
                    revalamountduenew = (Double) bAmt.getEntityList().get(0);
                    revalamountdue = revalamountdue + revalamountduenew;
                    double InvoiceGainLoss = 0;
                    //Call for reval
                    if (existingRate > 0) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getCreationDate(), existingRate);
                        double existingAmountdue = (Double) bAmt.getEntityList().get(0);
                        revalamountdueold = revalamountdueold + existingAmountdue;
                        InvoiceGainLoss = (revalamountduenew - existingAmountdue);;
                        profitloss = profitloss + authHandler.round((revalamountduenew - existingAmountdue),companyid);
                    } else {
                        InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                        profitloss = profitloss + authHandler.round((revalamountduenew - baseamountduenew),companyid);
                    }
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null) {
                        history.setIsRealised(true);
                        oldRevalId = history.getRevalid();
                        try {
                            accJournalEntryobj.addRevalHistory(history);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!isExport) {
                        revalueationHistory.setRevalid(revalid);
                        revalueationHistory.setInvoiceid(goodsReceipt.getID());
                        revalueationHistory.setModuleid(Constants.Acc_Vendor_Invoice_ModuleId);
                        revalueationHistory.setEvalrate(Double.parseDouble(revalCurrencyRate));
                        revalueationHistory.setAmount(newBaseAmount);
                        revalueationHistory.setEvaldate(df.parse(enddate));
                        revalueationHistory.setUserid(userDetails);
                        revalueationHistory.setCurrency(goodsReceipt.getCurrency());
                        revalueationHistory.setCompany(company);
                        revalueationHistory.setProfitloss(InvoiceGainLoss);
                        revalueationHistory.setAccountid(accountid);
                        revalueationHistory.setCurrentRate(newCurrentRate);
                        try {
                            accJournalEntryobj.addRevalHistory(revalueationHistory);
                        } catch (Exception ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            
            /*
             * Opening Balance Payments
             */
            requestParams = null;
            requestParams = accVendorPaymentController.getPaymentMap(request);
            requestParams.put("nondeleted", "true");
            requestParams.put("deleted", "false");
            requestParams.put(InvoiceConstants.accid, customerid);
            requestParams.put(Constants.REQ_startdate, startdate);
            requestParams.put(Constants.REQ_enddate, enddate);
            requestParams.put("currencyfilterfortrans", revalcurrencyId);
            requestParams.put("vendorid", customerid);
            requestParams.put("isAccountPayments", true);
            requestParams.put("onlyAmountDue", true);
            requestParams.put("excludeNormalInv", true);

            result = accVendorPaymentobj.getOpeningBalancePayments(requestParams);
            if (result.getEntityList() != null) {
                List<Payment> paymentsList = result.getEntityList();
                for (Payment payment : paymentsList) {
                    newBaseAmount = payment.getOpeningBalanceAmountDue();
                    amountdue = amountdue + newBaseAmount;

                    if (newBaseAmount <= 0) {
                        continue;
                    }
                    revalueationHistory = new RevaluationHistory();
                    double existingRate = 0;
                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                    double newCurrentRate = 0.0;
                    try {
                         invoiceId.put("invoiceid", payment.getID());
                        invoiceId.put("companyid", companyid);
                        result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                        RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                        if (history != null) {
                            DateFormat dateFormat = (requestParams.containsKey(Constants.df) && requestParams.get(Constants.df) != null) ? (DateFormat) requestParams.get(Constants.df) : df;
                            Date lastRevalDate = history.getEvaldate();
                            Date currentRevalDate = dateFormat.parse(enddate);
                            if (!StringUtil.isNullOrEmpty(enddate) && !StringUtil.isNullObject(history.getEvaldate()) && (DateUtils.isSameDay(lastRevalDate, currentRevalDate))) {
                                existingRate = history.getCurrentRate();
                            } else {
                                existingRate = history.getEvalrate();
                            }
                            newCurrentRate = existingRate;
                        }
//                        existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(payment.getID());
                    } catch (Exception ex) {
                        Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    double exchangeRate = payment.getExchangeRateForOpeningTransaction();
                    boolean isopeningBalanceInvoice = payment.isIsOpeningBalencePayment();
                    if (isopeningBalanceInvoice && payment.isConversionRateFromCurrencyToBase() && newCurrentRate == 0) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        newCurrentRate = 1 / exchangeRate;
                    }
//                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
//                    invoiceId.put("invoiceid", payment.getID());
//                    invoiceId.put("companyid", companyid);
                    //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
//                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null && history.isIsRealised()) {
                        exchangeRate = history.getEvalrate();
                    }
                    newCurrentRate = newCurrentRate == 0 ? exchangeRate : newCurrentRate;
                    String fromcurrencyid = payment.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    if (Constants.OpeningBalanceBaseAmountFlag) {
                        baseamountduenew = payment.getOpeningBalanceBaseAmountDue();
                    } else {
                        if (history == null && isopeningBalanceInvoice && payment.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, newBaseAmount, fromcurrencyid, payment.getCreationDate(), exchangeRate);                        //Converting into base [PS]
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, payment.getCreationDate(), exchangeRate);                        //Converting into base [PS]
                        }
                        //KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                        baseamountduenew = (Double) bAmt.getEntityList().get(0);
                    }
                    baseamountdue = baseamountdue + baseamountduenew;
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, payment.getCreationDate(), (Double.parseDouble(revalCurrencyRate)));
                    revalamountduenew = (Double) bAmt.getEntityList().get(0);
                    revalamountdue = revalamountdue + revalamountduenew;
                    double InvoiceGainLoss = 0;
                    //Call for reval
                    if (existingRate > 0) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, payment.getCreationDate(), existingRate);
                        double existingAmountdue = (Double) bAmt.getEntityList().get(0);
                        revalamountdueold = revalamountdueold + existingAmountdue;
                        InvoiceGainLoss = (revalamountduenew - existingAmountdue);;
                        profitloss = profitloss + authHandler.round((-(revalamountduenew - existingAmountdue)),companyid);
                    } else {
                        InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                        profitloss = profitloss + authHandler.round((-(revalamountduenew - baseamountduenew)),companyid);
                    }
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null) {
                        history.setIsRealised(true);
                        oldRevalId = history.getRevalid();
                        try {
                            accJournalEntryobj.addRevalHistory(history);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!isExport) {
                        revalueationHistory.setRevalid(revalid);
                        revalueationHistory.setInvoiceid(payment.getID());
                        revalueationHistory.setModuleid(Constants.Acc_opening_Payment);
                        revalueationHistory.setEvalrate(Double.parseDouble(revalCurrencyRate));
                        revalueationHistory.setAmount(newBaseAmount);
                        revalueationHistory.setEvaldate(df.parse(enddate));
                        revalueationHistory.setUserid(userDetails);
                        revalueationHistory.setCurrency(payment.getCurrency());
                        revalueationHistory.setCompany(company);
                        revalueationHistory.setProfitloss(-(InvoiceGainLoss));
                        revalueationHistory.setAccountid(accountid);
                        revalueationHistory.setCurrentRate(newCurrentRate);
                        try {
                            accJournalEntryobj.addRevalHistory(revalueationHistory);
                        } catch (Exception ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            /*
             * Advance Payments
             */
            requestParams = null;
            requestParams = accVendorPaymentController.getPaymentMap(request);
            requestParams.put("nondeleted", "true");
            requestParams.put("deleted", "false");
            requestParams.put(InvoiceConstants.accid, customerid);
            requestParams.put(Constants.REQ_startdate, startdate);
            requestParams.put(Constants.REQ_enddate, enddate);
            requestParams.put("currencyfilterfortrans", revalcurrencyId);
            requestParams.put("vendorid", customerid);
            requestParams.put("onlyAdvAmountDue", true);
            /**
             * Passing getRecordBasedOnJEDate as params to fetch record based on
             * JE date rather than document creation date as documents are revalued on the basis of JE date ERM-655.
             */
            requestParams.put("isToFetchRecordLessEndDate", true);
            requestParams.put("getRecordBasedOnJEDate", true);
            result = accVendorPaymentobj.getPayments(requestParams);
            List paymentsList = result.getEntityList();
            for (Object object : paymentsList) {
                Object[] row = (Object[]) object;
                Payment payment = (Payment) row[0];
                newBaseAmount = 0;
                for (AdvanceDetail advanceDetail : payment.getAdvanceDetails()) {
                    newBaseAmount += advanceDetail.getAmountDue();
                }
                amountdue = amountdue + newBaseAmount;
                revalueationHistory = new RevaluationHistory();
                double existingRate = 0;
                Map<String, Object> invoiceId = new HashMap<>();
                double newCurrentRate = 0.0;
                try {
                    invoiceId.put("invoiceid", payment.getID());
                    invoiceId.put("companyid", companyid);
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null) {
                        DateFormat dateFormat = (requestParams.containsKey(Constants.df) && requestParams.get(Constants.df) != null) ? (DateFormat) requestParams.get(Constants.df) : df;
                        Date lastRevalDate = history.getEvaldate();
                        Date currentRevalDate = dateFormat.parse(enddate);
                        if (!StringUtil.isNullOrEmpty(enddate) && !StringUtil.isNullObject(history.getEvaldate()) && (DateUtils.isSameDay(lastRevalDate, currentRevalDate))) {
                            existingRate = history.getCurrentRate();
                        } else {
                            existingRate = history.getEvalrate();
                        }
                        newCurrentRate = existingRate;
                    }
//                    existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(payment.getID());
                } catch (Exception ex) {
                    Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                }
                JournalEntry entry = payment.getJournalEntry();
                double exchangeRate = entry.getExternalCurrencyRate();
//                Date transDate = entry.getEntryDate();
                Date transDate = payment.getCreationDate();
//                Map<String, Object> invoiceId = new HashMap<>();
//                invoiceId.put("invoiceid", payment.getID());
//                invoiceId.put("companyid", companyid);
                //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
//                result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                if (history != null && history.isIsRealised()) {
                    exchangeRate = history.getEvalrate();
                }
                newCurrentRate = newCurrentRate == 0 ? exchangeRate : newCurrentRate;
                String fromcurrencyid = payment.getCurrency().getCurrencyID();
                KwlReturnObject bAmt=accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, transDate, exchangeRate);                        //Converting into base [PS]
                baseamountduenew = (Double) bAmt.getEntityList().get(0);
             
                baseamountdue = baseamountdue + baseamountduenew;
                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, (Double.parseDouble(revalCurrencyRate)));
                revalamountduenew = (Double) bAmt.getEntityList().get(0);
                revalamountdue = revalamountdue + revalamountduenew;
                double InvoiceGainLoss = 0;
                //Call for reval
                if (existingRate > 0) {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, transDate, existingRate);
                    double existingAmountdue = (Double) bAmt.getEntityList().get(0);
                    revalamountdueold = revalamountdueold + existingAmountdue;
                    InvoiceGainLoss = (revalamountduenew - existingAmountdue);;
                    profitloss = profitloss + authHandler.round((-(revalamountduenew - existingAmountdue)),companyid);
                } else {
                    InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                    profitloss = profitloss + authHandler.round((-(revalamountduenew - baseamountduenew)),companyid);
                }
                invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                if (history != null) {
                    history.setIsRealised(true);
                    oldRevalId = history.getRevalid();
                    try {
                        accJournalEntryobj.addRevalHistory(history);
                    } catch (AccountingException ex) {
                        Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (!isExport) {
                    revalueationHistory.setRevalid(revalid);
                    revalueationHistory.setInvoiceid(payment.getID());
                    revalueationHistory.setModuleid(Constants.Acc_Make_Payment_ModuleId);
                    revalueationHistory.setEvalrate(Double.parseDouble(revalCurrencyRate));
                    revalueationHistory.setAmount(newBaseAmount);
                    revalueationHistory.setEvaldate(df.parse(enddate));
                    revalueationHistory.setUserid(userDetails);
                    revalueationHistory.setCurrency(payment.getCurrency());
                    revalueationHistory.setCompany(company);
                    revalueationHistory.setProfitloss(-(InvoiceGainLoss));
                    revalueationHistory.setAccountid(accountid);
                    revalueationHistory.setCurrentRate(newCurrentRate);
                    try {
                        accJournalEntryobj.addRevalHistory(revalueationHistory);
                    } catch (Exception ex) {
                        Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
            
            
            /*
             * Opening Balance DNs
             */
            requestParams = null;
            requestParams = accDebitNoteController.gettDebitNoteMap(request);
            requestParams.put("nondeleted", "true");
            requestParams.put("deleted", "false");
            requestParams.put(Constants.REQ_startdate, startdate);
            requestParams.put(Constants.REQ_enddate, enddate);
            requestParams.put("currencyfilterfortrans", revalcurrencyId);
            requestParams.put("vendorid", customerid);
            requestParams.put("onlyAmountDue", true);
            requestParams.put("excludeNormalInv", true);

            result = accDebitNoteobj.getOpeningBalanceDNs(requestParams);
            if (result.getEntityList() != null) {
                List<DebitNote> debitNotesList = result.getEntityList();
                for (DebitNote debitNote : debitNotesList) {
                    newBaseAmount = debitNote.getOpeningBalanceAmountDue();
                    amountdue = amountdue + newBaseAmount;
                    if (newBaseAmount <= 0) {
                        continue;
                    }
                    revalueationHistory = new RevaluationHistory();
                    double existingRate = 0;
                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                    double newCurrentRate = 0.0;
                    try {
                        invoiceId.put("invoiceid", debitNote.getID());
                        invoiceId.put("companyid", companyid);
                        result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                        RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                        if(history!=null){
                            DateFormat dateFormat = (requestParams.containsKey(Constants.df) && requestParams.get(Constants.df) != null) ? (DateFormat) requestParams.get(Constants.df) : df;
                            Date lastRevalDate = history.getEvaldate();
                            Date currentRevalDate = dateFormat.parse(enddate);
                            if (!StringUtil.isNullOrEmpty(enddate) && !StringUtil.isNullObject(history.getEvaldate()) && (DateUtils.isSameDay(lastRevalDate, currentRevalDate))) {
                                existingRate = history.getCurrentRate();
                            } else {
                                existingRate = history.getEvalrate();
                            }
                            newCurrentRate = existingRate;
                        }
//                        existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(debitNote.getID());
                    } catch (Exception ex) {
                        Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    double exchangeRate = debitNote.getExchangeRateForOpeningTransaction();
                    boolean isopeningBalanceInvoice = debitNote.isIsOpeningBalenceDN();
                    if (isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase() && newCurrentRate == 0) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        newCurrentRate = 1 / exchangeRate;
                    }
//                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
//                    invoiceId.put("invoiceid", debitNote.getID());
//                    invoiceId.put("companyid", companyid);
                    //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
//                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null && history.isIsRealised()) {
                        exchangeRate = history.getEvalrate();
                    }
                    newCurrentRate = newCurrentRate == 0 ? exchangeRate : newCurrentRate;
                    String fromcurrencyid = debitNote.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    if (Constants.OpeningBalanceBaseAmountFlag) {
                        baseamountduenew = debitNote.getOpeningBalanceBaseAmountDue();
                    } else {
                        if (history == null && isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, newBaseAmount, fromcurrencyid, debitNote.getCreationDate(), exchangeRate);                        //Converting into base [PS]
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, debitNote.getCreationDate(), exchangeRate);                        //Converting into base [PS]
                        }
                        //KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                        baseamountduenew = (Double) bAmt.getEntityList().get(0);
                    }
                    baseamountdue = baseamountdue + baseamountduenew;

                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, debitNote.getCreationDate(), (Double.parseDouble(revalCurrencyRate)));
                    revalamountduenew = (Double) bAmt.getEntityList().get(0);
                    revalamountdue = revalamountdue + revalamountduenew;
                    double InvoiceGainLoss = 0;
                    //Call for reval
                    if (existingRate > 0) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, debitNote.getCreationDate(), existingRate);
                        double existingAmountdue = (Double) bAmt.getEntityList().get(0);
                        revalamountdueold = revalamountdueold + existingAmountdue;
                        InvoiceGainLoss = (revalamountduenew - existingAmountdue);;
                        profitloss = profitloss + authHandler.round((-(revalamountduenew - existingAmountdue)),companyid);
                    } else {
                        InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                        profitloss = profitloss + authHandler.round((-(revalamountduenew - baseamountduenew)),companyid);
                    }
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null) {
                        history.setIsRealised(true);
                        oldRevalId = history.getRevalid();
                        try {
                            accJournalEntryobj.addRevalHistory(history);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!isExport) {
                        revalueationHistory.setRevalid(revalid);
                        revalueationHistory.setInvoiceid(debitNote.getID());
                        revalueationHistory.setModuleid(Constants.Acc_opening_Vendor_DebitNote);
                        revalueationHistory.setEvalrate(Double.parseDouble(revalCurrencyRate));
                        revalueationHistory.setAmount(newBaseAmount);
                        revalueationHistory.setEvaldate(df.parse(enddate));
                        revalueationHistory.setUserid(userDetails);
                        revalueationHistory.setCurrency(debitNote.getCurrency());
                        revalueationHistory.setCompany(company);
                        revalueationHistory.setProfitloss(-(InvoiceGainLoss));
                        revalueationHistory.setAccountid(accountid);
                        revalueationHistory.setCurrentRate(newCurrentRate);
                        try {
                            accJournalEntryobj.addRevalHistory(revalueationHistory);
                        } catch (Exception ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            /*
             * Opening Balance CNs
             */
            requestParams = null;
            requestParams = accCreditNoteController.getCreditNoteMap(request);
            requestParams.put("nondeleted", "true");
            requestParams.put("deleted", "false");
            requestParams.put(Constants.REQ_startdate, startdate);
            requestParams.put(Constants.REQ_enddate, enddate);
            requestParams.put("currencyfilterfortrans", revalcurrencyId);
            requestParams.put("onlyAmountDue", true);
            requestParams.put("vendorid", customerid);
//            requestParams.put("isAccountCNs", true);

            result = accCreditNoteDAOobj.getOpeningBalanceVendorCNs(requestParams);
            if (result.getEntityList() != null) {
                List<CreditNote> CreditNotesList = result.getEntityList();
                for (CreditNote creditNote : CreditNotesList) {
                    newBaseAmount = creditNote.getOpeningBalanceAmountDue();
                    amountdue = amountdue + newBaseAmount;
                    if (newBaseAmount <= 0) {
                        continue;
                    }
                    revalueationHistory = new RevaluationHistory();
                    double existingRate = 0;
                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                    double newCurrentRate = 0.0;
                    try {
                        invoiceId.put("invoiceid", creditNote.getID());
                        invoiceId.put("companyid", companyid);
                        result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                        RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                          if(history!=null){
                            DateFormat dateFormat = (requestParams.containsKey(Constants.df) && requestParams.get(Constants.df) != null) ? (DateFormat) requestParams.get(Constants.df) : df;
                            Date lastRevalDate = history.getEvaldate();
                            Date currentRevalDate = dateFormat.parse(enddate);
                            if (!StringUtil.isNullOrEmpty(enddate) && !StringUtil.isNullObject(history.getEvaldate()) && (DateUtils.isSameDay(lastRevalDate, currentRevalDate))) {
                                existingRate = history.getCurrentRate();
                            } else {
                                existingRate = history.getEvalrate();
                            }
                            newCurrentRate = existingRate;
                        }
//                        existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(creditNote.getID());
                    } catch (Exception ex) {
                        Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    double exchangeRate = creditNote.getExchangeRateForOpeningTransaction();
                    boolean isopeningBalanceInvoice = creditNote.isIsOpeningBalenceCN();
                    if (isopeningBalanceInvoice && creditNote.isConversionRateFromCurrencyToBase() && newCurrentRate == 0) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        newCurrentRate = 1 / exchangeRate;
                    }
//                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
//                    invoiceId.put("invoiceid", creditNote.getID());
//                    invoiceId.put("companyid", companyid);
                    //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
//                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null && history.isIsRealised()) {
                        exchangeRate = history.getEvalrate();
                    }
                    newCurrentRate = newCurrentRate == 0 ? exchangeRate : newCurrentRate;
                    String fromcurrencyid = creditNote.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    if (Constants.OpeningBalanceBaseAmountFlag) {
                        baseamountduenew = creditNote.getOpeningBalanceBaseAmountDue();
                    } else {
                        if (history == null && isopeningBalanceInvoice && creditNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, newBaseAmount, fromcurrencyid, creditNote.getCreationDate(), exchangeRate);                        //Converting into base [PS]
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, creditNote.getCreationDate(), exchangeRate);                        //Converting into base [PS]
                        }
                        //KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, tocurrencyid, goodsReceipt.getJournalEntry().getEntryDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                        baseamountduenew = (Double) bAmt.getEntityList().get(0);
                    }
                    baseamountdue = baseamountdue + baseamountduenew;

                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, creditNote.getCreationDate(), (Double.parseDouble(revalCurrencyRate)));
                    revalamountduenew = (Double) bAmt.getEntityList().get(0);
                    revalamountdue = revalamountdue + revalamountduenew;
                    double InvoiceGainLoss = 0;
                    //Call for reval
                    if (existingRate > 0) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, creditNote.getCreationDate(), existingRate);
                        double existingAmountdue = (Double) bAmt.getEntityList().get(0);
                        revalamountdueold = revalamountdueold + existingAmountdue;
                        InvoiceGainLoss = (revalamountduenew - existingAmountdue);;
                        profitloss = profitloss + authHandler.round((revalamountduenew - existingAmountdue),companyid);
                    } else {
                        InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                        profitloss = profitloss + authHandler.round((revalamountduenew - baseamountduenew),companyid);
                    }
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null) {
                        history.setIsRealised(true);
                        oldRevalId = history.getRevalid();
                        try {
                            KwlReturnObject jeresult = accJournalEntryobj.addRevalHistory(history);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!isExport) {
                        revalueationHistory.setRevalid(revalid);
                        revalueationHistory.setInvoiceid(creditNote.getID());
                        revalueationHistory.setModuleid(Constants.Acc_opening_Vendor_CreditNote);
                        revalueationHistory.setEvalrate(Double.parseDouble(revalCurrencyRate));
                        revalueationHistory.setAmount(newBaseAmount);
                        revalueationHistory.setEvaldate(df.parse(enddate));
                        revalueationHistory.setUserid(userDetails);
                        revalueationHistory.setCurrency(creditNote.getCurrency());
                        revalueationHistory.setCompany(company);
                        revalueationHistory.setProfitloss(InvoiceGainLoss);
                        revalueationHistory.setAccountid(accountid);
                        revalueationHistory.setCurrentRate(newCurrentRate);
                        try {
                            accJournalEntryobj.addRevalHistory(revalueationHistory);
                        } catch (Exception ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            /*
             * Normal Debit Note of Vendors
             */
            requestParams = null;
            requestParams = accDebitNoteController.gettDebitNoteMap(request);
            requestParams.put("nondeleted", "true");
            requestParams.put("deleted", "false");
            requestParams.put(Constants.REQ_startdate, startdate);
            requestParams.put(Constants.REQ_enddate, enddate);
            requestParams.put("currencyfilterfortrans", revalcurrencyId);
            requestParams.put("customerVendorID", customerid);
            requestParams.put("cntype", 1);
            // get opening balance receipts created from opening balance button.
            requestParams.put("isNewUI", false);
            requestParams.put("isNoteForPayment", false);
             /**
             * Passing getRecordBasedOnJEDate as params to fetch record based on JE date rather than document creation date as documents are revalued on the basis of JE date
             * ERM-655.
             */
            requestParams.put("getRecordBasedOnJEDate", true);
            requestParams.put("isToFetchRecordLessEndDate", true);
            result = accDebitNoteobj.getDebitNoteMerged(requestParams);
            if (result.getEntityList() != null) {
                List debitNotesList = result.getEntityList();
//                Iterator DebitNoteitr = debitNotesList.iterator();
                for (Object object : debitNotesList) {
                    Object[] row = (Object[]) object;
                    KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), (String) row[1]);
                    DebitNote debitNote = (DebitNote) resultObject.getEntityList().get(0);
                    JournalEntry je = debitNote.getJournalEntry();
                    revalueationHistory = new RevaluationHistory();
                    double existingRate = 0;
                    newBaseAmount = 0;
                    Date debitNoteDate = null;
                    double externalCurrencyRate = 0d;
//                    debitNoteDate = je.getEntryDate();
                    debitNoteDate = debitNote.getCreationDate();
                    externalCurrencyRate = je.getExternalCurrencyRate();
                    double newCurrentRate = 0.0;
                    Map<String, Object> invoiceId = new HashMap<>();
                    invoiceId.put("invoiceid", debitNote.getID());
                    invoiceId.put("companyid", companyid);
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                     if(history != null) {
                        DateFormat dateFormat = (requestParams.containsKey(Constants.df) && requestParams.get(Constants.df) != null) ? (DateFormat) requestParams.get(Constants.df) : df;
                        Date lastRevalDate = history.getEvaldate();
                        Date currentRevalDate = dateFormat.parse(enddate);
                        if (!StringUtil.isNullOrEmpty(enddate) && !StringUtil.isNullObject(history.getEvaldate()) && (DateUtils.isSameDay(lastRevalDate, currentRevalDate))) {
                            existingRate = history.getCurrentRate();
                        } else {
                            existingRate = history.getEvalrate();
                        }
                        newCurrentRate = existingRate;
                    }
//                    existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(debitNote.getID());
                    newBaseAmount = debitNote.isOtherwise() ? debitNote.getDnamountdue() : 0;
                    amountdue = amountdue + newBaseAmount;
                    double exchangeRate = externalCurrencyRate;
//                    Map<String, Object> invoiceId = new HashMap<>();
//                    invoiceId.put("invoiceid", debitNote.getID());
//                    invoiceId.put("companyid", companyid);
                    //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
//                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null && history.isIsRealised()) {
                        exchangeRate = history.getEvalrate();
                    }
                    newCurrentRate = newCurrentRate == 0 ? exchangeRate : newCurrentRate;
                    String fromcurrencyid = debitNote.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    baseamountduenew = 0d;
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, debitNoteDate, exchangeRate);
                    baseamountduenew = (Double) bAmt.getEntityList().get(0);
                    baseamountdue = baseamountdue + newBaseAmount;
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, debitNoteDate, (Double.parseDouble(revalCurrencyRate)));
                    revalamountduenew = (Double) bAmt.getEntityList().get(0);
                    revalamountdue = revalamountdue + revalamountduenew;
                    //Calculating for old revalue
                    double InvoiceGainLoss = 0;
                    if (existingRate > 0) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, debitNoteDate, existingRate);
                        double existingAmountdue = (Double) bAmt.getEntityList().get(0);
                        revalamountdueold = revalamountdueold + (Double) bAmt.getEntityList().get(0);
                        InvoiceGainLoss = (revalamountduenew - existingAmountdue);
                        profitloss = profitloss + authHandler.round((-(revalamountduenew - existingAmountdue)),companyid);
                    } else {
                        InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                        profitloss = profitloss + authHandler.round((-(revalamountduenew - baseamountduenew)),companyid);
                    }
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null) {
                        history.setIsRealised(true);
                        oldRevalId = history.getRevalid();
                        try {
                            accJournalEntryobj.addRevalHistory(history);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!isExport) {
                        revalueationHistory.setRevalid(revalid);
                        revalueationHistory.setInvoiceid(debitNote.getID());
                        revalueationHistory.setModuleid(Constants.Acc_Debit_Note_ModuleId);
                        revalueationHistory.setEvalrate(Double.parseDouble(revalCurrencyRate));
                        revalueationHistory.setAmount(newBaseAmount);
                        revalueationHistory.setEvaldate(df.parse(enddate));
                        revalueationHistory.setUserid(userDetails);
                        revalueationHistory.setCurrency(debitNote.getCurrency());
                        revalueationHistory.setCompany(company);
                        revalueationHistory.setProfitloss(-(InvoiceGainLoss));  //Reverase case than invoice
                        revalueationHistory.setAccountid(accountid);
                        revalueationHistory.setCurrentRate(newCurrentRate);
                        try {
                            accJournalEntryobj.addRevalHistory(revalueationHistory);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            /*
             * Normal Credit Note of Vendors
             */
            requestParams = null;
            requestParams = accCreditNoteController.getCreditNoteMap(request);
            requestParams.put("nondeleted", "true");
            requestParams.put("deleted", "false");
            requestParams.put(Constants.REQ_startdate, startdate);
            requestParams.put(Constants.REQ_enddate, enddate);
            requestParams.put("currencyfilterfortrans", revalcurrencyId);
            requestParams.put("customerVendorID", customerid);
            requestParams.put("cntype", 4); //Credit Note agianst Vendor
            // get opening balance receipts created from opening balance button.
            requestParams.put("isNewUI", false);
            requestParams.put("isNoteForPayment", false);
             /**
             * Passing getRecordBasedOnJEDate as params to fetch record based on JE date rather than document creation date as documents are revalued on the basis of JE date
             * ERM-655.
             */
            requestParams.put("getRecordBasedOnJEDate", true);
            requestParams.put("isToFetchRecordLessEndDate", true);
            result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
            if (result.getEntityList() != null) {
                List creditNotesList = result.getEntityList();
//                Iterator CreditNoteitr = creditNotesList.iterator();
                for (Object object : creditNotesList) {
                    Object[] row = (Object[]) object;
                    boolean withoutinventory = Boolean.parseBoolean((String) row[0]);
                    KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) row[1]);
                    CreditNote creditNote = (CreditNote) resultObject.getEntityList().get(0);
                    JournalEntry je = creditNote.getJournalEntry();
                    revalueationHistory = new RevaluationHistory();
                    double existingRate = 0;
                    newBaseAmount = 0;
                    Date debitNoteDate = null;
                    double externalCurrencyRate = 0d;
                    double newCurrentRate = 0.0;
//                    debitNoteDate = je.getEntryDate();
                    debitNoteDate = creditNote.getCreationDate();
                    externalCurrencyRate = je.getExternalCurrencyRate();
                    Map<String, Object> invoiceId = new HashMap<>();
                    invoiceId.put("invoiceid", creditNote.getID());
                    invoiceId.put("companyid", companyid);
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null) {
                        DateFormat dateFormat = (requestParams.containsKey(Constants.df) && requestParams.get(Constants.df) != null) ? (DateFormat) requestParams.get(Constants.df) : df;
                        Date lastRevalDate = history.getEvaldate();
                        Date currentRevalDate = dateFormat.parse(enddate);
                        if (!StringUtil.isNullOrEmpty(enddate) && !StringUtil.isNullObject(history.getEvaldate()) && (DateUtils.isSameDay(lastRevalDate, currentRevalDate))) {
                            existingRate = history.getCurrentRate();
                        } else {
                            existingRate = history.getEvalrate();
                        }
                        newCurrentRate = existingRate;
                    }
//                    existingRate = accJournalEntryobj.getRevalHistoryRateForInvoice(creditNote.getID());
                    newBaseAmount = creditNote.isOtherwise() ? creditNote.getCnamountdue() : 0;
                    amountdue = amountdue + newBaseAmount;
                    double exchangeRate = externalCurrencyRate;
//                    Map<String, Object> invoiceId = new HashMap<>();
//                    invoiceId.put("invoiceid", creditNote.getID());
//                    invoiceId.put("companyid", companyid);
                    //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
//                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null && history.isIsRealised()) {
                        exchangeRate = history.getEvalrate();
                    }
                    newCurrentRate = newCurrentRate == 0 ? exchangeRate : newCurrentRate;
                    String fromcurrencyid = creditNote.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    baseamountduenew = 0d;
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, debitNoteDate, exchangeRate);
                    baseamountduenew = (Double) bAmt.getEntityList().get(0);
                    baseamountdue = baseamountdue + newBaseAmount;
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, debitNoteDate, (Double.parseDouble(revalCurrencyRate)));
                    revalamountduenew = (Double) bAmt.getEntityList().get(0);
                    revalamountdue = revalamountdue + revalamountduenew;
                    //Calculating for old revalue
                    double InvoiceGainLoss = 0;
                    if (existingRate > 0) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, newBaseAmount, fromcurrencyid, tocurrencyid, debitNoteDate, existingRate);
                        double existingAmountdue = (Double) bAmt.getEntityList().get(0);
                        revalamountdueold = revalamountdueold + (Double) bAmt.getEntityList().get(0);
                        InvoiceGainLoss = (revalamountduenew - existingAmountdue);
                        profitloss = profitloss + authHandler.round((revalamountduenew - existingAmountdue),companyid);
                    } else {
                        InvoiceGainLoss = (revalamountduenew - baseamountduenew);
                        profitloss = profitloss + authHandler.round((revalamountduenew - baseamountduenew),companyid);
                    }
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (history != null) {
                        history.setIsRealised(true);
                        oldRevalId = history.getRevalid();
                        try {
                            accJournalEntryobj.addRevalHistory(history);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!isExport) {
                        revalueationHistory.setRevalid(revalid);
                        revalueationHistory.setInvoiceid(creditNote.getID());
                        revalueationHistory.setModuleid(Constants.Acc_Credit_Note_ModuleId);
                        revalueationHistory.setEvalrate(Double.parseDouble(revalCurrencyRate));
                        revalueationHistory.setAmount(newBaseAmount);
                        revalueationHistory.setEvaldate(df.parse(enddate));
                        revalueationHistory.setUserid(userDetails);
                        revalueationHistory.setCurrency(creditNote.getCurrency());
                        revalueationHistory.setCompany(company);
                        revalueationHistory.setProfitloss(InvoiceGainLoss);  //Reverase case than invoice
                        revalueationHistory.setAccountid(accountid);
                        revalueationHistory.setCurrentRate(newCurrentRate);
                        try {
                            accJournalEntryobj.addRevalHistory(revalueationHistory);
                        } catch (AccountingException ex) {
                            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            returnBalance[0] = amountdue;
            returnBalance[1] = baseamountdue;
            returnBalance[2] = revalamountdue;
            returnBalance[3] = revalamountdueold;
            returnBalance[4] = profitloss;
            request.setAttribute("oldRevalId", oldRevalId);
        } catch (Exception ex) {
            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnBalance;
    }
    
    private double[] getBankBalanceDetails(HttpServletRequest request, String accountid, Account account, String startdate, String enddate, String revalcurrencyId, String revalCurrencyRate, String revalid, User userDetails, Company company, CompanyAccountPreferences preferences) throws ServiceException, SessionExpiredException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        HashMap<String, Object> requestParamsForReval = new HashMap<String, Object>();
        String baseCurrency = sessionHandlerImpl.getCurrencyID(request);
        boolean isExport = !StringUtil.isNullObject(request.getAttribute("isExport"))?Boolean.parseBoolean(request.getAttribute("isExport").toString()):false;
        requestParams.put("accountid", accountid);
        requestParams.put("enddate", enddate);
        requestParams.put("currencyfilterfortrans", revalcurrencyId);
        requestParams.put("gcurrencyid", baseCurrency);
        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));

        requestParamsForReval.put("nondeleted", "true");
        requestParamsForReval.put("deleted", "false");
        requestParamsForReval.put("accid", accountid);
        requestParamsForReval.put(Constants.REQ_startdate, startdate);
        requestParamsForReval.put(Constants.REQ_enddate, enddate);
        requestParamsForReval.put("currencyfilterfortrans", revalcurrencyId);
        requestParamsForReval.put("isRevalue", true);
        double amountdue = 0;
        double profitloss = 0;
        double returnBalance[] = {0, 0, 0, 0, 0};
        double baseamountdue = 0;
        double revalamountdue = 0;
        double revalamountdueold = 0;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            if (account.getCurrency().getCurrencyID().equalsIgnoreCase(baseCurrency) == true) {
                return returnBalance;
            }
            double openingBalanceInBase = accInvoiceCommon.getOpeningBalanceOfAccount(request, account, false, null);
//            CompanyAccountPreferences preferences;
//            Map<String, Object> requestParamsAccount = new HashMap<String, Object>();
//            requestParamsAccount.put("id", sessionHandlerImpl.getCompanyid(request));
//            KwlReturnObject prefresult = accCompanyPreferencesObj.getCompanyPreferences(requestParamsAccount);
//            preferences = (CompanyAccountPreferences) prefresult.getEntityList().get(0);
            Date date = preferences.getBookBeginningFrom();
            Date AccountCreationDate = account.getCreationDate();
            KwlReturnObject openingAmount = null;
            Date dateBefore = new Date(date.getTime() - 1 * 24 * 3600 * 1000);
            boolean accountHasOpeningTransactions = accInvoiceCommon.accountHasOpeningTransactions(request, account, false, null);
            if (date.before(AccountCreationDate)) {
                openingAmount = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, openingBalanceInBase, account.getCurrency().getCurrencyID(), AccountCreationDate, 0);//converting account currency to Base Currency.
            } else {
                if (dateBefore != null) {
                    openingAmount = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, openingBalanceInBase, account.getCurrency().getCurrencyID(), dateBefore, 0);//converting account currency to Base Currency.
                } else {
                    throw new AccountingException("Book Beginning  date cannot be empty.");//Financial year date cannot be empty."
                }
            }
            double openingBalance = 0.0;
            if (accountHasOpeningTransactions) {
                openingBalance = (Double) openingAmount.getEntityList().get(0);
            } else {
                openingBalance = account.getOpeningBalance();
            }
            // double openingBalance=account.getOpeningBalance();

            KwlReturnObject result = accJournalEntryobj.getJournalEntryDetailsForBank(requestParams);
            if (date.before(AccountCreationDate)) {
                openingAmount = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, openingBalance, account.getCurrency().getCurrencyID(), account.getCompany().getCurrency().getCurrencyID(), AccountCreationDate, (Double.parseDouble(revalCurrencyRate)));
            } else {
                openingAmount = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, openingBalance, account.getCurrency().getCurrencyID(), account.getCompany().getCurrency().getCurrencyID(), dateBefore, (Double.parseDouble(revalCurrencyRate)));
            }
            double openingBalanceReval = (Double) openingAmount.getEntityList().get(0);
            profitloss = profitloss + authHandler.round((openingBalanceReval - openingBalanceInBase),companyid);
            if (result.getEntityList() != null) {
                List list = result.getEntityList();
                for (Object object : list) {
                    JournalEntryDetail jed = (JournalEntryDetail) object;
                    double newBaseAmount = 0;
                    newBaseAmount = jed.getAmount();
                    double revalAmount = 0;
                    double exchangeRate = jed.getJournalEntry().getExternalCurrencyRate();
                    String fromcurrencyid = jed.getJournalEntry().getCurrency().getCurrencyID();
                    String tocurrencyid = jed.getCompany().getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;//if Same currency then use - getOneCurrencyToOther()
                    if (fromcurrencyid.equalsIgnoreCase(jed.getAccount().getCurrency().getCurrencyID())) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, newBaseAmount, fromcurrencyid, jed.getAccount().getCurrency().getCurrencyID(), jed.getJournalEntry().getEntryDate(), exchangeRate);                        //Converting into base [PS]
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, newBaseAmount, fromcurrencyid, jed.getAccount().getCurrency().getCurrencyID(), jed.getJournalEntry().getEntryDate(), exchangeRate);                        //Converting into base [PS] 
                    }

                    double amountAccountCurrency = 0;
                    if (jed.getJournalEntry().getIsReval() > 0) {
                        amountAccountCurrency = 0;
                    } else {
                        if (!StringUtil.isNullOrEmpty(fromcurrencyid) && jed != null && jed.getAccount() != null && jed.getAccount().getCurrency() != null && fromcurrencyid.equals(jed.getAccount().getCurrency().getCurrencyID())) {
                            amountAccountCurrency = jed.getAmount(); // if transactionCurrencyID=accountCurrencyID, then don't convert the amount
                        } else if ((jed.getJournalEntry().getPaymentcurrencytopaymentmethodcurrencyrate() != 1) || ((jed.getJournalEntry().getPaymentcurrencytopaymentmethodcurrencyrate() == 1) && jed.getJournalEntry().isIsmulticurrencypaymentje())) {    // If this JE is for that payment, which is having payment currency and payment method currency different.
                            double baseCurrencyToPaymentCurrencyConvertedAmount = jed.getAmount();   // This amount is already saved in payment currency only. 
                            double baseCurrencyToPaymentMethodCurrencyConvertedAmount = baseCurrencyToPaymentCurrencyConvertedAmount * jed.getJournalEntry().getPaymentcurrencytopaymentmethodcurrencyrate();
                            amountAccountCurrency = baseCurrencyToPaymentMethodCurrencyConvertedAmount;
                            amountAccountCurrency = authHandler.round(amountAccountCurrency, companyid);
                            /*
                             * ERP-16072 When JE is fund transafer JE with line
                             * level exchange rate is applicable for account
                             * currency to JE currency, amount in account
                             * currency will be calculated according to user
                             * given exchange rate.
                             */
                        } else if (jed.getJournalEntry().getTypeValue() == Constants.FundTransfer_Journal_Entry && jed.getExchangeRateForTransaction() != 0 && jed.getExchangeRateForTransaction() != -1) {
                            amountAccountCurrency = jed.getAmount() / jed.getExchangeRateForTransaction();
                            amountAccountCurrency = authHandler.round(amountAccountCurrency, companyid);
                        } else {
                            amountAccountCurrency = (Double) bAmt.getEntityList().get(0);
                            amountAccountCurrency = authHandler.round(amountAccountCurrency, companyid);
                        }
                    }
                    revalAmount = authHandler.round(amountAccountCurrency, companyid);
                    if (jed.isDebit()) {
                        amountdue += authHandler.round(amountAccountCurrency, companyid);
                    } else {
                        amountdue -= authHandler.round(amountAccountCurrency, companyid);
                    }

                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, newBaseAmount, fromcurrencyid, jed.getJournalEntry().getEntryDate(), exchangeRate);                        //Converting into base [PS]
                    double baseamountduenew = 0.0;
                    if (jed.isDebit()) {
                        baseamountduenew = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                    } else {
                        baseamountduenew = -(authHandler.round((Double) bAmt.getEntityList().get(0), companyid));
                    }
                    baseamountdue = baseamountdue + baseamountduenew;//if Same currency then use - getOneCurrencyToOther()
                    if (jed.getAccount().getCurrency().getCurrencyID().equalsIgnoreCase(tocurrencyid)) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParamsForReval, revalAmount, jed.getAccount().getCurrency().getCurrencyID(), tocurrencyid, jed.getJournalEntry().getEntryDate(), (Double.parseDouble(revalCurrencyRate)));
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParamsForReval, revalAmount, jed.getAccount().getCurrency().getCurrencyID(), tocurrencyid, jed.getJournalEntry().getEntryDate(), (Double.parseDouble(revalCurrencyRate)));
                    }
                    double revalamountduenew = 0;
                    if (jed.getJournalEntry().getIsReval() <= 0) {//not adding forgin amount while re-evaluation  
                        if (jed.isDebit()) {
                            revalamountduenew = (Double) bAmt.getEntityList().get(0);
                        } else {
                            revalamountduenew = -((Double) bAmt.getEntityList().get(0));
                        }
                    }
                    revalamountdue = revalamountdue + revalamountduenew;
                    profitloss = profitloss + authHandler.round((revalamountduenew - baseamountduenew),companyid);
                }
                if (profitloss != 0 && !isExport) {
                    RevaluationHistory revalueationHistory = new RevaluationHistory();
                    revalueationHistory.setRevalid(revalid);
                    revalueationHistory.setInvoiceid(""); //we are setting JE ID inthis case while posting journal entery
                    revalueationHistory.setModuleid(Constants.Acc_GENERAL_LEDGER_ModuleId); //For Bank Case module ID is journal
                    revalueationHistory.setEvalrate(Double.parseDouble(revalCurrencyRate));
                    revalueationHistory.setAmount(baseamountdue + openingBalanceInBase);
                    revalueationHistory.setEvaldate(df.parse(enddate));
                    revalueationHistory.setUserid(userDetails);
                    revalueationHistory.setCurrency(account.getCurrency());
                    revalueationHistory.setCompany(company);
                    revalueationHistory.setProfitloss(profitloss);
                    revalueationHistory.setAccountid(accountid);
                    accJournalEntryobj.addRevalHistory(revalueationHistory);
                }
                returnBalance[0] = amountdue + openingBalance;  //   openingBalance;
                returnBalance[1] = baseamountdue + openingBalanceInBase;
                returnBalance[2] = revalamountdue + openingBalanceReval;
                returnBalance[3] = revalamountdueold;
                returnBalance[4] = profitloss;
            } else {
                returnBalance[0] = 0;
                returnBalance[1] = 0;
                returnBalance[2] = 0;
                returnBalance[3] = 0;
                returnBalance[4] = 0;
            }
        } catch (AccountingException ex) {
            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnBalance;
    }
    
        public ModelAndView saveRevaluationJournalEntry(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "", jeid = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = saveRevaluationJournalEntry(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.je1.save", null, RequestContextUtils.getLocale(request));   //"Journal Entry has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            
            msg = "" + ex.getMessage();
            if(msg.equalsIgnoreCase("No Unrealised Gain/Loss account found.")){
                txnManager.commit(status);   
            }else{
                txnManager.rollback(status);
            }    
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("id", jeid);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveRevaluationJournalEntry(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException, ParseException {
        JournalEntry je = null;
        List ll = new ArrayList();
        try {
            //for loop for all records
            String accountId = "";
            String revalid = "";
            String Auditmsg = "";
            JSONArray jsonArr = new JSONArray();
            DateFormat df = authHandler.getDateOnlyFormat(request);
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String currencyId = request.getParameter("currencyId");
            int accTypeId = Integer.parseInt(request.getParameter("accTypeId"));           
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String userid = sessionHandlerImpl.getUserid(request);
//            KwlReturnObject accResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyId);
//            if (accResult != null) {
//                KWLCurrency currency = (KWLCurrency) accResult.getEntityList().get(0);
//                currencyName = currency.getName();
//            }
            KwlReturnObject dscresult=null;
            List accll=null;
            String foreign_exchange_accid="";
            String unrealised_accid = "";
            CompanyAccountPreferences preferences;
            Date revalDate= df.parse(request.getParameter("enddate"));
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", companyid);
            KwlReturnObject resultComp = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            preferences = (CompanyAccountPreferences) resultComp.getEntityList().get(0);
            String currencyIdsDel[]= currencyId.split(",");
            JSONObject jobjVal = jArr.getJSONObject(0);
            String accountname=accTypeId==1?"Accounts Payables":(accTypeId==2?"Accounts Receivables":(accTypeId == 4 ? "Cash In Hand" :"Bank"));
            String revalIdCh = jobjVal.getString("revalid");
            
            /*
             * delete history while re-evaluation for same date
             */ 
            if (accTypeId != Group.ACCOUNTTYPE_BANK && accTypeId != Constants.CashInHandAccountTye) {
                /**
                 * Added Account type for deleting 'Account Revaluation History'. 
                 */
                KwlReturnObject resultDeleted = accJournalEntryobj.deleteRevalhistoryEntry(revalDate, currencyIdsDel, revalIdCh, accTypeId);
                List allDeletedLists = resultDeleted.getEntityList();
                for (Iterator it = allDeletedLists.iterator(); it.hasNext();) {
                    List<Object[]> deletedJeList = (List) it.next();
//                List<Object[]> deletedJeList = resultDeleted.getEntityList();
                    JournalEntry deletedJe = null;
                    if (deletedJeList != null && !deletedJeList.isEmpty()) {
                        for (Object[] row : deletedJeList) {
                            KwlReturnObject resultObjectJe = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), (String) row[0]);
                            deletedJe = (JournalEntry) resultObjectJe.getEntityList().get(0);
                            auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "Revaluation journal entry : " + row[1] + " of account " + accountname + " has been deleted ", request, (String) row[0]);
                        }
                    }
                }
            }
            if(accTypeId!=Group.ACCOUNTTYPE_BANK && accTypeId!=Constants.CashInHandAccountTye){
                if(preferences.getUnrealisedgainloss()!=null){
                    unrealised_accid =preferences.getUnrealisedgainloss().getID();
                }else{
                   
                            JSONObject jobj = jArr.getJSONObject(0);
                            revalid = jobj.getString("revalid");
                            if(revalid!=null || !revalid.equals("")){
                            accJournalEntryobj.deleteRevalEntry(revalid);
                            }
                        throw new AccountingException("No Unrealised Gain/Loss account found.");        
                }    
            }else{              
                if(preferences.getForeignexchange()!=null){
                    foreign_exchange_accid =preferences.getForeignexchange().getID();
                }else{
                    dscresult = this.accAccountDAOobj.getAccountFromName(companyid, Constants.FOREIGN_EXCHANGE);
                    accll = dscresult.getEntityList();
                    if (accll.size() == 1) {
                    foreign_exchange_accid = ((Account) accll.get(0)).getID();
                    } else {
                            JSONObject jobj = jArr.getJSONObject(0);
                            revalid = jobj.getString("revalid");
                            if(revalid!=null || !revalid.equals("")){
                                accJournalEntryobj.deleteRevalEntry(revalid);
                            }
                        throw new AccountingException("No Foreign Exchange account found.");
                    }
                }
            }
            JSONObject jobj = jArr.getJSONObject(0);
            revalid=jobj.getString("revalid");
            if(!StringUtil.isNullOrEmpty(revalid))
            {                   
                HashMap<String,Object> revalId = new HashMap<String, Object>();
                revalId.put("revalid",revalid);    
                revalId.put("companyid", sessionHandlerImpl.getCompanyid(request));
                KwlReturnObject result=accJournalEntryobj.getFromRevalID(revalId);
                if(result!=null){   
                    String formatid = "";
                    String preFix = "";
                    String suffix = "";
                    String jeDatePrefix = "";
                    String jeDateAfterPrefix = "";
                    String jeDateSuffix = "";
                    boolean showleadingZero = false;
                    int numberofdigit = 0;
                    String jeEntryNumber = "";
                    int curtSeqNumber = 0;
                    
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put("companyid", companyid);
                    JEFormatParams.put("isdefaultFormat", true);
                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                       
                    if(format!=null){
                        formatid=format.getID();
                        showleadingZero=format.isShowleadingzero();
                        numberofdigit=format.getNumberofdigit();
                        preFix=format.getPrefix();
                        suffix=format.getSuffix();
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, formatid, false, new Date());                        
                        curtSeqNumber = Integer.parseInt((String)seqNumberMap.get(Constants.SEQNUMBER));  
                        jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
                        jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    }
                    
                    String customfield = request.getParameter("customfield");
                    String lineleveldimensions = request.getParameter("lineleveldimensions");
                    List<RevaluationHistory> list=result.getEntityList();
                    for(RevaluationHistory revaluationHistory:list){                    
                        accountId=revaluationHistory.getAccountid();
                        if (format != null && (format.isDateBeforePrefix() || format.isDateAfterPrefix() || format.isShowDateFormatAfterSuffix())) {
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, formatid, false, revaluationHistory.getEvaldate());
                            jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                            jeDateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
                            jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        }
                        
                        boolean checkduplicate=true;
                        while (checkduplicate) { //create number and check it is duplicate or no if yes then in
                            String tempNumber = String.valueOf(curtSeqNumber);
                            if (showleadingZero) {
                                while (tempNumber.length() < numberofdigit) {
                                    tempNumber = "0" + tempNumber;
                                }
                            }
                            jeEntryNumber = jeDatePrefix + preFix + jeDateAfterPrefix + tempNumber + suffix +jeDateSuffix;

                            kwlObj = accJournalEntryobj.getJECount(jeEntryNumber, companyid);
                            int nocount = kwlObj.getRecordTotalCount();
                            if (nocount > 0) {
                                checkduplicate = true;
                                curtSeqNumber++;
                            } else {
                                checkduplicate = false;
                            }
                        }                                                 
                        String jeid = "";
                        String oldjeid = "";

        //            JSONArray jArr = new JSONArray(request.getParameter("detail"));            
                        double amount=revaluationHistory.getProfitloss();
                        double exchangeValue=revaluationHistory.getEvalrate();
                        Date entryDate=revaluationHistory.getEvaldate();
                     

                        double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));


                        String costCenterId = request.getParameter(CCConstants.REQ_costcenter);
                        Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                        jeDataMap.put("companyid", companyid);
                        jeDataMap.put("entrydate", entryDate);
                        jeDataMap.put("memo", "");
                        jeDataMap.put("entrynumber", jeEntryNumber);
                        jeDataMap.put("transactionModuleid", revaluationHistory.getModuleid());
                        jeDataMap.put("transactionId", revaluationHistory.getInvoiceid());
                        jeDataMap.put("autogenerated", true);
                        jeDataMap.put(Constants.SEQNUMBER, curtSeqNumber);
                        jeDataMap.put(Constants.SEQFORMAT, formatid);
                        jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                        jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                        jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                        jeDataMap.put("currencyid", currencyid);
                         if(accTypeId==Group.ACCOUNTTYPE_BANK || accTypeId==Constants.CashInHandAccountTye){//For Bank Account Only
                             jeDataMap.put("isReval", 2);
                         }else{
                             jeDataMap.put("isReval", 1);
                         }
                        
        //            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);

                        if (!StringUtil.isNullOrEmpty(costCenterId)) {
                            jeDataMap.put(CCConstants.JSON_costcenterid, costCenterId);
                        }
                       
                        String revalInvoiceID=revaluationHistory.getInvoiceid();
                        jeDataMap.put("revalInvoiceId", revalInvoiceID);
                        KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                        je = (JournalEntry) jeresult.getEntityList().get(0);
                      
                        Set jedetails = new HashSet();
                        double newamount = amount;
                        if (amount < 0) {
                            newamount = -(amount);
                        }
                        boolean creditDebitFalg = false;
                        if (accTypeId == Constants.VENDOR) {
                            if (amount > 0) {
                                creditDebitFalg = false;
                            } else {
                                creditDebitFalg = true;
                            }
                        } else if (accTypeId == Constants.CUSTOMER) {
                            if (amount > 0) {
                                creditDebitFalg = true;
                            } else {
                                creditDebitFalg = false;
                            }
                        } else if (accTypeId == Group.ACCOUNTTYPE_BANK || accTypeId == Constants.CashInHandAccountTye) {
                            if (amount > 0) {
                                creditDebitFalg = true;
                            } else {
                                creditDebitFalg = false;
                            }
                        }

                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", newamount);
                        jedjson.put("accountid", accountId);
                        jedjson.put("debit", creditDebitFalg);
                        jedjson.put("description", "");
                        jedjson.put("jeid", je.getID());
                        KwlReturnObject jedresult  = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);
                         if (!StringUtil.isNullOrEmpty(lineleveldimensions)) {
                            JSONArray jcustomarray = new JSONArray(lineleveldimensions);
                            HashMap<String, Object> customrequestParams = new HashMap<>();
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                            customrequestParams.put("modulerecid", jed.getID());
                            customrequestParams.put("recdetailId", jed.getID());
                            customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                            customrequestParams.put("companyid", companyid);
                            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                //jed.setAccJEDetailCustomData((AccJEDetailCustomData) hibernateTemplate.get(AccJEDetailCustomData.class, jed.getID()));
                                jedjson = new JSONObject();
                                jedjson.put("accjedetailcustomdata", jed.getID());
                                jedjson.put("jedid", jed.getID());
                                jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                            }
                        }
                        
                        if (accTypeId == Constants.VENDOR) {
                            if (amount > 0) {
                                creditDebitFalg = true;
                            } else {
                                creditDebitFalg = false;
                            }
                        } else if (accTypeId == Constants.CUSTOMER) {
                            if (amount > 0) {
                                creditDebitFalg = false;
                            } else {
                                creditDebitFalg = true;
                            }
                        } else if (accTypeId == Group.ACCOUNTTYPE_BANK || accTypeId == Constants.CashInHandAccountTye) {
                            if (amount > 0) {
                                creditDebitFalg = false;
                            } else {
                                creditDebitFalg = true;
                            }
                        }
                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put("companyid", companyid);
                        jedjson.put("amount", newamount);
                        if (accTypeId == Group.ACCOUNTTYPE_BANK || accTypeId == Constants.CashInHandAccountTye) {//For Bank Account Only
                            jedjson.put("accountid", foreign_exchange_accid);
                        } else {
                            jedjson.put("accountid", unrealised_accid);
                        } 
                        jedjson.put("debit", creditDebitFalg);
                        jedjson.put("description", "");
                        jedjson.put("jeid", je.getID());
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);
                        
                        if (!StringUtil.isNullOrEmpty(lineleveldimensions)) {
                            JSONArray jcustomarray = new JSONArray(lineleveldimensions);
                            HashMap<String, Object> customrequestParams = new HashMap<>();
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                            customrequestParams.put("modulerecid", jed.getID());
                            customrequestParams.put("recdetailId", jed.getID());
                            customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                            customrequestParams.put("companyid", companyid);
                            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                //jed.setAccJEDetailCustomData((AccJEDetailCustomData) hibernateTemplate.get(AccJEDetailCustomData.class, jed.getID()));
                                jedjson = new JSONObject();
                                jedjson.put("accjedetailcustomdata", jed.getID());
                                jedjson.put("jedid", jed.getID());
                                accJournalEntryobj.updateJournalEntryDetails(jedjson);
                            }
                        }
////                            jedresult = accJournalEntryobj.getJEDsetForRevaluation(companyid, accountId, foreign_exchange_accid, amount, accTypeId);
////                        } else {
////                            jedresult = accJournalEntryobj.getJEDsetForRevaluation(companyid, accountId, unrealised_accid, amount, accTypeId);
////                        }
//
//                        HashSet jeDetails = (HashSet) jedresult.getEntityList().get(0);
                        jeDataMap.put("jeid", je.getID());
                        jeDataMap.put("jedetails", jedetails);
                        jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
                        jeid = je.getID();
                        
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
                                customjeDataMap.put("istemplate", 0);
                                if (accTypeId == Group.ACCOUNTTYPE_BANK || accTypeId == Constants.CashInHandAccountTye) {//For Bank Account Only
                                    customjeDataMap.put("isReval", 2);
                                } else {
                                    customjeDataMap.put("isReval", 1);
                                }
                               accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                            }
                        }
                        
                        ll.add(jeid);
                        ll.add(oldjeid);
                        
                        //Insert new entries again in optimized table.
                        accJournalEntryobj.saveAccountJEs_optimized(jeid);
                         if(accTypeId==Group.ACCOUNTTYPE_BANK || accTypeId==Constants.CashInHandAccountTye){//For Bank Account Only
                             revaluationHistory.setInvoiceid(jeid);
                         }
                        KwlReturnObject accResult = accountingHandlerDAOobj.getObject(Account.class.getName(), accountId);
                        Account account = (Account) accResult.getEntityList().get(0);
                        String currencyName= revaluationHistory.getCurrency() != null ? revaluationHistory.getCurrency().getName() : "";
                        /**
                         * Insert Revaluation Details in Audit Trail.
                         */
                        String documentNo = "";
                        HashMap<String, Object> requestParam = new HashMap<>();
                        requestParam.put("invoiceId", revaluationHistory.getInvoiceid());
                        requestParam.put("companyId", companyid);
                        requestParam.put("moduleId", revaluationHistory.getModuleid());
                        jsonArr = getRevaluationDocumentNumber(requestParam); //method for getting document number
                        documentNo = jsonArr.getJSONObject(0).optString("billno");
                        String revaluationDate = authHandler.getUserDateFormatterWithoutTimeZone(request).format(revaluationHistory.getEvaldate());
                        Auditmsg = "Revaluation Journal Entry : "  + je.getEntryNumber() + " for account " + accountname  + " against Document : " + documentNo + " for amount "+ authHandler.round(amount, companyid)  + "  has been posted for currency " + currencyName + " with exchange rate " + exchangeValue  + " on revaluation date "  + revaluationDate;
                        auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, Auditmsg, request, je.getID());
                        //auditTrailObj.insertAuditLog(AuditAction.ACCOUNT_REVALUATION_UPDATED, " has Revaluated Accounts : "+accountname , request, companyid);                        
                        curtSeqNumber++;
                    }     
              
                }
            }
                Calendar cal = Calendar.getInstance();
                cal.setTime(revalDate);
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                
                String currencyIds[]= currencyId.split(",");
                for(int currncyCount=0; currncyCount < currencyIds.length ; currncyCount ++){
                    Map<String, Object> RevalDataMap = AccountingManager.getGlobalParams(request);
                    RevalDataMap.put("company", companyid);
                    RevalDataMap.put("month",month);
                    RevalDataMap.put("year",year);
                    RevalDataMap.put("userid", userid);
                    RevalDataMap.put("accountType", accTypeId);
                    RevalDataMap.put("revalDate", revalDate);
                    RevalDataMap.put("revalId", revalid);
                    RevalDataMap.put("currencyId", currencyIds[currncyCount]);
                    accJournalEntryobj.saveRevalTime(RevalDataMap);
                }
            if (!StringUtil.isNullOrEmpty(revalid)) {
                accJournalEntryobj.updateRevaluationFlag(revalid);
            }
            
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveJournalEntry : " + ex.getMessage(), ex);
//            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveJournalEntry : " + ex.getMessage(), ex);
        }
        return ll;
    }
    public JSONArray getRevaluationDocumentNumber(HashMap<String, Object> requestMap) throws ServiceException, JSONException{
        JSONArray jArr = new JSONArray();
        int moduleid = (Integer) requestMap.get("moduleId");
        String CompanyId = (String) requestMap.get("companyId");
        String InvoiceId = (String) requestMap.get("invoiceId");
        String billNo = "";
        KwlReturnObject result = null;
        JSONObject obj = new JSONObject();
        String[] params = new String[1];
        Map<String, Object> filterMap = new HashMap();
        filterMap.put("id", InvoiceId);
        Object docObj = null;
        switch (moduleid) {
            case 2: // Sales Invoice
            case 112: // Opening Sales Invoice.
                result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), InvoiceId);
                Invoice invoice = (Invoice) result.getEntityList().get(0);
                if (!StringUtil.isNullObject(invoice)) {
                    obj.put("billno", invoice.getInvoiceNumber());
                }
                break;

            case 6: // for Purchase Invoice
            case 113: // opening Purchase Invoice
                result = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), InvoiceId);
                GoodsReceipt goodsReceipt = (GoodsReceipt) result.getEntityList().get(0);
                if (!StringUtil.isNullObject(goodsReceipt)) {
                    obj.put("billno", goodsReceipt.getGoodsReceiptNumber());
                }
                break;
            case 24://for bank and cash in hand type is not handled for account revaluation report. reason- single je is posted.
                result = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), InvoiceId);
                JournalEntry journalEntry = (JournalEntry) result.getEntityList().get(0);
                if (journalEntry != null) {
                    obj.put("billno", journalEntry.getEntryNumber());
                }
                break;
            case 10: // for Debit Note
            case 118: // Opening Customer Debit Note
            case 119: // Opening Vendor Debit Note
                params[0] = Constants.debitNoteNumber;
                docObj = kwlCommonTablesDAOObj.getRequestedObjectFields(DebitNote.class, params, filterMap);
                break;
            case 12: // for credit note
            case 116: // Opening Customer Credit Note
            case 117: // Opening Vendor Credit Note
                params[0] = Constants.creditNoteNumber;
                docObj = kwlCommonTablesDAOObj.getRequestedObjectFields(CreditNote.class, params, filterMap);
                break;
            case 14: // for Make Payment note
            case 115: // Opening Payment
                params[0] = Constants.paymentNumber;
                docObj = kwlCommonTablesDAOObj.getRequestedObjectFields(Payment.class, params, filterMap);
                break;
            case 16: // for Receipt Note
            case 114: // Opening Receipt Note
                params[0] = Constants.receiptNumber;
                docObj = kwlCommonTablesDAOObj.getRequestedObjectFields(Receipt.class, params, filterMap);
                break;
        }
        /**
         * to use of Hibernate Projection for getting required fields from class
         * name.
         */
        if (!StringUtil.isNullObject(docObj)) {
            billNo = (String) docObj;
            if (!StringUtil.isNullOrEmpty(billNo)) {
                obj.put(Constants.billno, billNo);
            }
        }
        jArr.put(obj);

        return jArr;
    }
    public ModelAndView exportAccountsReEvaluationReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            JSONArray DataJArr = new JSONArray();
            String startDate = request.getParameter(Constants.REQ_startdate);
            String endDate = request.getParameter(Constants.REQ_enddate);
            boolean consolidateFlag = request.getParameter("consolidateFlag")!=null?Boolean.parseBoolean(request.getParameter("consolidateFlag")):false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids")!=null)?request.getParameter("companyids").split(","):sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid")!=null)?request.getParameter("gcurrencyid"):sessionHandlerImpl.getCurrencyID(request);                        
            String companyid = "";
            for(int cnt=0; cnt<companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);

                Map<String, Object> RevalDataMap = new HashMap<String, Object>();
                RevalDataMap.put("companyid", companyid);
                RevalDataMap.put("startdate", startDate);
                RevalDataMap.put("enddate", endDate);
                RevalDataMap.put(Constants.df, authHandler.getDateOnlyFormat());
                
                KwlReturnObject result =accJournalEntryobj.ReevaluationHistoryReport(RevalDataMap);
                DataJArr=ReevaluationHistoryReportJson(request,result.getEntityList());
                
            }
            jobj.put(Constants.RES_data, DataJArr);
            jobj.put(Constants.RES_count, DataJArr.length());
            issuccess = true;
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                exportDaoObj.processRequest(request, response, jobj);
            } else {
                exportDaoObj.processRequest(request, response, jobj);
            }
        } catch (Exception ex) {
            msg += ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView-empty", "model", jobj.toString());
    }
    
    public ModelAndView exportAccountRevaluationXls(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String view = "jsonView_ex";
        try{
           boolean export=true;
           jobj= getAccountForRevaluationJSON(request, export);
           String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            jobj.put("isFromDocumentDesigner",true);
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView saveRevaluationJECustomData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "", jeid = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = saveRevaluationJECustomData(request);
            issuccess = true;
            msg ="Custom field/Dimension Saved Successfully."; // messageSource.getMessage("acc.je1.save", null, RequestContextUtils.getLocale(request));   //"Journal Entry has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            txnManager.rollback(status);
            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("id", jeid);
            } catch (JSONException ex) {
                Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveRevaluationJECustomData(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException, ParseException {
        List ll = new ArrayList();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userid = sessionHandlerImpl.getUserid(request);
            String customfield=request.getParameter("customfield");
            String lineleveldimensions=request.getParameter("lineleveldimensions");
            String id=request.getParameter("id");
            Map<String, Object> RevalJECustomDataMap = new HashMap<>();
            RevalJECustomDataMap.put("company", companyid);
            RevalJECustomDataMap.put("customfield",customfield);
            RevalJECustomDataMap.put("lineleveldimensions",lineleveldimensions);
            RevalJECustomDataMap.put("id",id);
            RevalJECustomDataMap.put("userid", userid);
            accJournalEntryobj.saveRevaluationJECustomData(RevalJECustomDataMap);
        } catch (SessionExpiredException | ServiceException ex) {
            throw ServiceException.FAILURE("saveJournalEntry : " + ex.getMessage(), ex);
        }
        return ll;
    }
    
    public ModelAndView getRevaluationJECustomData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "", jeid = "";
        boolean issuccess = false;
        try {
            JSONObject detailObj = getRevaluationJECustomData(request);
            jobj.put("data", detailObj);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("id", jeid);
            } catch (JSONException ex) {
                Logger.getLogger(accReevaluationReportController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
     public JSONObject getRevaluationJECustomData(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException, ParseException {
        JSONObject jObj = new JSONObject();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject result =accJournalEntryobj.getRevaluationJECustomData(companyid);
            RevaluationJECustomData revaluationJECustomData= (result != null && result.getEntityList().size() > 0 && result.getEntityList().get(0)!= null) ? (RevaluationJECustomData) result.getEntityList().get(0): null;
            jObj.put("id", revaluationJECustomData.getID());
            if(!StringUtil.isNullOrEmpty(revaluationJECustomData.getCustomfield())){
                jObj.put("customfield", revaluationJECustomData.getCustomfield());
            }
            if(!StringUtil.isNullOrEmpty(revaluationJECustomData.getLineleveldimensions())){
                jObj.put("lineleveldimensions", revaluationJECustomData.getLineleveldimensions());
            }
        } catch (SessionExpiredException | JSONException |ServiceException ex) {
            throw ServiceException.FAILURE("saveJournalEntry : " + ex.getMessage(), ex);
        }
        return jObj;
    }
    
}
