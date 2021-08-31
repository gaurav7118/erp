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
package com.krawler.spring.accounting.debitnote;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptCMN;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.receipt.accReceiptControllerNew;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author Pandurang
 */
public class accDebitNoteControllerCMN extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accDebitNoteDAO accDebitNoteobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accTaxDAO accTaxObj;
    private auditTrailDAO auditTrailObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private fieldDataManager fieldDataManagercntrl;
    private exportMPXDAOImpl exportDaoObj;
    private String successView;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private MessageSource messageSource;
    public ImportHandler importHandler;
    public accBankReconciliationDAO accBankReconciliationDAOObj;
    private ImportDAO importDao;
    public accCustomerDAO accCustomerDAOObj;
    private accVendorDAO accVendorDAOObj;
    private accDebitNoteService accDebitNoteService;
    private accAccountDAO accAccountDAOobj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccJournalEntryModuleService journalEntryModuleServiceobj;
    private authHandlerDAO authHandlerDAOObj;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private accGoodsReceiptCMN accGoodsReceiptCommon;
    private accPaymentDAO accPaymentDAOobj;

    public class SortDebitNoteDetail implements Comparator<DebitNoteDetail> {

        @Override
        public int compare(DebitNoteDetail DND1, DebitNoteDetail DND2) {
            if (DND1.getSrno() > DND2.getSrno()) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setAccCustomerDAO(accCustomerDAO accCustomerDAOObj) {
        this.accCustomerDAOObj = accCustomerDAOObj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }

    public void setAccVendorDAO(accVendorDAO accVendorDAOObj) {
        this.accVendorDAOObj = accVendorDAOObj;
    }

    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setaccDiscountDAO(accDiscountDAO accDiscountobj) {
        this.accDiscountobj = accDiscountobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setaccDebitNoteService(accDebitNoteService accDebitNoteService) {
        this.accDebitNoteService = accDebitNoteService;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public AccJournalEntryModuleService getJournalEntryModuleServiceobj() {
        return journalEntryModuleServiceobj;
    }

    public void setJournalEntryModuleServiceobj(AccJournalEntryModuleService journalEntryModuleServiceobj) {
        this.journalEntryModuleServiceobj = journalEntryModuleServiceobj;
    }

    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }

    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }

    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }

    public void setaccGoodsReceiptCMN(accGoodsReceiptCMN accGoodsReceiptCommon) {
        this.accGoodsReceiptCommon = accGoodsReceiptCommon;
    }
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }
    
    public void setAccBankReconciliationDAOObj(accBankReconciliationDAO accBankReconciliationDAOObj) {
        this.accBankReconciliationDAOObj = accBankReconciliationDAOObj;
    }

public ModelAndView linkDebitNote(HttpServletRequest request, HttpServletResponse response) {
    JSONObject jobj = new JSONObject();
    boolean issuccess = false;
    String msg = "";
    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setName("SP_Tx");
    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus status = txnManager.getTransaction(def);
    try {
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        List li = linkDebitNote(request, "", true);   // "true" flag is passed for inserting Audit Trial entry ( ERP-18558 )
        issuccess = true;
        msg = messageSource.getMessage("acc.field.DebitNotehasbeenLinkedtoVendorInvoicesuccessfully", null, RequestContextUtils.getLocale(request));
        txnManager.commit(status);
        
        //below code is for Rounding off JE Generation if needed
        status = txnManager.getTransaction(def);
        try{
            accDebitNoteService.postRoundingJEAfterLinkingInvoiceInDebitNote(paramJobj);
            txnManager.commit(status);
        } catch(ServiceException ex){
            if(status!=null){
                txnManager.rollback(status);
            }
            Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
    } catch (AccountingException ex) {
        if (status != null) {
            txnManager.rollback(status);
        }
        msg = ex.getMessage();
        Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
    } catch (SessionExpiredException ex) {
        txnManager.rollback(status);
        msg = ex.getMessage();
        Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ServiceException ex) {
        txnManager.rollback(status);
        msg = ex.getMessage();
        Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
    } catch (Exception ex) {
        txnManager.rollback(status);
        msg = "" + ex.getMessage();
        Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
        try {
            jobj.put("success", issuccess);
            jobj.put("msg", msg);
        } catch (JSONException ex) {
            Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    return new ModelAndView("jsonView", "model", jobj.toString());
}

public List linkDebitNote(HttpServletRequest request, String creditNoteId, Boolean isInsertAudTrail) throws ServiceException, SessionExpiredException, JSONException, AccountingException {
    List result = new ArrayList();
    List linkedInvoicesList = new ArrayList();
    List linkedNotesList = new ArrayList();
    try {
        JSONArray invoiceArray = new JSONArray();
        JSONArray noteArray = new JSONArray();
        JSONArray linkJSONArray = request.getParameter("linkdetails") != null ? new JSONArray(request.getParameter("linkdetails")) : new JSONArray();
        Map<String, Object> counterMap = new HashMap<>();
        counterMap.put("counter", 0);
        for (int i = 0; i < linkJSONArray.length(); i++) {
            JSONObject obj = linkJSONArray.getJSONObject(i);
            int documenttype = Integer.parseInt(obj.optString("documentType"));
            if (documenttype == Constants.CreditNoteOtherwise && obj.optDouble("linkamount", 0.0) != 0) {
                invoiceArray.put(obj);
            } else if (documenttype == Constants.CreditNoteAgainstDebitNote && obj.optDouble("linkamount", 0.0) != 0) {
                noteArray.put(obj);
            }
        }

        if (invoiceArray.length() > 0) {
            linkedInvoicesList = linkDebitNoteToInvoices(request, invoiceArray, creditNoteId, isInsertAudTrail, counterMap);
        }
        if (noteArray.length() > 0) {
            linkedNotesList = linkDebitNoteToCreditNote(request, noteArray, creditNoteId, isInsertAudTrail, counterMap);
        }

    } catch (AccountingException ex){
        throw new AccountingException(ex.getMessage(),ex);
    } catch (SessionExpiredException ex) {
        throw ServiceException.FAILURE(ex.getMessage(), ex);
    } catch (Exception ex) {
        Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        throw ServiceException.FAILURE(ex.getMessage(), ex);
    }
    result.addAll(linkedInvoicesList);
    result.addAll(linkedNotesList);
    return result;
}

public List linkDebitNoteToInvoices(HttpServletRequest request, JSONArray invoiceArray, String debitNoteId, Boolean isInsertAudTrail, Map<String, Object> counterMap) throws ServiceException, SessionExpiredException, AccountingException {
    List result = new ArrayList();
    String companyid = sessionHandlerImpl.getCompanyid(request);
        DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
        def1.setName("DB_Tx");
        def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = null;
    try {
        String cnid = "";
        int counter = 0;
        if (!StringUtil.isNullOrEmpty(debitNoteId)) {
            cnid = debitNoteId;
        } else {
            cnid = request.getParameter("cnid");
        }

//        Date maxLinkingDate = null;
//        String linkingdate = (String) request.getParameter("linkingdate");
//        DateFormat dateformat = authHandler.getDateOnlyFormat();
//        if (!StringUtil.isNullOrEmpty(linkingdate)) {
//            try {
//                maxLinkingDate = dateformat.parse(linkingdate);
//            } catch (ParseException ex) {
//                Logger.getLogger(accDebitNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }

        String entryNumber = request.getParameter("number");
        String amounts[] = request.getParameter("amounts").split(",");
        String invoiceDetails = request.getParameter("invoicedetails");

        String baseCurrency = sessionHandlerImpl.getCurrencyID(request);
        for (int k = 0; k < invoiceArray.length(); k++) {
            JSONObject jobj = invoiceArray.getJSONObject(k);

            if (StringUtil.isNullOrEmpty(jobj.getString("documentid"))) {
                continue;
            }

            double usedcnamount = 0d;
            double useddnamountInBase = 0d;
            double typeFigure = 0d;
            int typeOfFigure = 1;
            usedcnamount = jobj.optDouble("linkamount", 0.0d);
//            if (!StringUtil.isNullOrEmpty(amounts[k])) {
//                usedcnamount = Double.parseDouble((String) amounts[k]);
//            } else {
//                usedcnamount = 0;
//            }

            if (usedcnamount == 0) {
                continue;
            }
            if (!StringUtil.isNullOrEmpty(jobj.optString("typeFigure"))) {
                typeFigure = jobj.optDouble("typeFigure", 0.0);
            }
            if (!StringUtil.isNullOrEmpty(jobj.optString("typeOfFigure"))) {
                typeOfFigure = jobj.optInt("typeOfFigure", 1);
            }
            try {
                synchronized (this) {
                    /*
                     * Checks duplicate number for simultaneous transactions
                     */
                    status = txnManager.getTransaction(def1);
                    KwlReturnObject resultInv1 = accPaymentDAOobj.getInvoiceInTemp(jobj.getString("documentid"), companyid, Constants.Acc_Vendor_Invoice_ModuleId);
                    if (resultInv1.getRecordTotalCount() > 0) {
                        throw new AccountingException("Selected invoice is already in process, please try after sometime.");
                    } else {
                        accPaymentDAOobj.insertInvoiceOrCheque(jobj.getString("documentid"), companyid, Constants.Acc_Vendor_Invoice_ModuleId, "");
                    }
                    txnManager.commit(status);
//                    status = null;
                }
            } catch (Exception ex) {
                txnManager.rollback(status);
                throw new AccountingException(ex.getMessage(), ex);
            }
            
            
            KwlReturnObject grresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), jobj.getString("documentid"));
            GoodsReceipt goodsReceipt = (GoodsReceipt) grresult.getEntityList().get(0);

            KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), cnid);
            DebitNote debitNote = (DebitNote) cnObj.getEntityList().get(0);

            grresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) grresult.getEntityList().get(0);

            Set<DebitNoteDetail> newdebitNoteDetails = new HashSet<DebitNoteDetail>();

            double cnamountdue = debitNote.getDnamountdue();
            double goodsReceiptAmountDue = 0; // Opening transaction record
            if (goodsReceipt.isIsOpeningBalenceInvoice() && !goodsReceipt.isNormalInvoice()) {
                goodsReceiptAmountDue = goodsReceipt.getOpeningBalanceAmountDue();
            } else {
                goodsReceiptAmountDue = goodsReceipt.getInvoiceamountdue();
            }

            if (!debitNote.isOpenflag() || cnamountdue <= 0) {
                throw new AccountingException(messageSource.getMessage("acc.field.DebitNotehasbeenalreadyutilized", null, RequestContextUtils.getLocale(request)));
            }
            
            /**
             * SDP-13710 For Debit Note Link Transaction, PI Amount Due Converted into
             * Base Currency.
             */
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
            KwlReturnObject bAmt = null;
            if (!goodsReceipt.isIsOpeningBalenceInvoice()) {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, goodsReceiptAmountDue, debitNote.getCurrency().getCurrencyID(), goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
            } else {
                double exchangeRateForInvoice = goodsReceipt.getExchangeRateForOpeningTransaction();
                if (goodsReceipt.isConversionRateFromCurrencyToBase()) {
                    exchangeRateForInvoice = 1 / exchangeRateForInvoice;
                }
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, goodsReceiptAmountDue, debitNote.getCurrency().getCurrencyID(), goodsReceipt.getCreationDate(), exchangeRateForInvoice);
            }
            goodsReceiptAmountDue = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
            
            /**
             * Enter Amount Converted into Base Currency.
             */
            double exchangeRateForCN = 0.0;
            if (!debitNote.isIsOpeningBalenceDN()) {
                exchangeRateForCN = debitNote.getExternalCurrencyRate();
            } else {
                exchangeRateForCN = debitNote.getExchangeRateForOpeningTransaction();
                if (debitNote.isConversionRateFromCurrencyToBase()) {
                    exchangeRateForCN = 1 / exchangeRateForCN;
                }
            }
            /**
             * if ExchangeRateForDN is 1 then no need to convert into Base
             * Currency.
             */
            if (exchangeRateForCN == 1.0) {
                useddnamountInBase = usedcnamount;
            } else {
                KwlReturnObject bAmtUsedAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, usedcnamount, debitNote.getCurrency().getCurrencyID(), debitNote.getCreationDate(), exchangeRateForCN);
                useddnamountInBase = authHandler.round((Double) bAmtUsedAmount.getEntityList().get(0), companyid);
            }
            /**
             * I have comment this code due to some scenario are interrupt.
             * below condition for checks simultaneous transaction,
             */
//            if(goodsReceiptAmountDue < useddnamountInBase){
//                 throw new AccountingException(messageSource.getMessage("acc.field.alreadyknock_off", null, RequestContextUtils.getLocale(request)));
//            }
            double amountReceived = usedcnamount;           //amount of DN 
            double amountReceivedConverted = usedcnamount;
            double adjustedRate = 1;
            double exchangeRateforTransaction = jobj.optDouble("exchangeratefortransaction", 1.0);

            if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(goodsReceipt.getCurrency().getCurrencyID()) && !goodsReceipt.getCurrency().getCurrencyID().equals(debitNote.getCurrency().getCurrencyID())) {
                 // adjusted exchange rate used to handle case like ERP-34884
                adjustedRate = exchangeRateforTransaction;
                if (jobj.optDouble("amountdue", 0) != 0 && jobj.optDouble("amountDueOriginal", 0) != 0) {
                    adjustedRate = jobj.optDouble("amountdue", 0) / jobj.optDouble("amountDueOriginal", 0);
                }
                amountReceivedConverted = amountReceived / adjustedRate;
                amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
            } else {
                amountReceivedConverted = authHandler.round(amountReceived, companyid);
            }
            DebitNoteDetail dndetailObj = new DebitNoteDetail();
            Date maxDate = null;    //ERP-32348
            Date maxLinkingDate = null;
            
            if (jobj.has("linkingdate") && jobj.get("linkingdate") != null) {
                String linkingdate = (String) jobj.get("linkingdate");
                DateFormat dateformat = authHandler.getDateOnlyFormat();
                if (!StringUtil.isNullOrEmpty(linkingdate)) {
                    try {
                        maxLinkingDate = dateformat.parse(linkingdate);
                    } catch (ParseException ex) {
                        Logger.getLogger(accDebitNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            //For invoices and otherwise cases entered amount is the amountERP-36411
            if ((debitNote.getDntype() == DebitNote.DEBITNOTE_AGAINST_INVOICE || debitNote.getDntype() == DebitNote.DEBITNOTE_OTHERWISE) && typeFigure == 0.0) {
                typeFigure = jobj.optDouble("typeFigure", usedcnamount);
            }
            if (debitNote.getDntype() != 3 && debitNote.isOpenflag() && debitNote.getDnamount() == debitNote.getDnamountdue()) {//If DN is used for first time.
                Set<DebitNoteDetail> debitNoteDetails = (Set<DebitNoteDetail>) debitNote.getRows();
                Iterator itr = debitNoteDetails.iterator();
                while (itr.hasNext()) {
                    dndetailObj = (DebitNoteDetail) itr.next();

                    if (goodsReceipt != null) {
                        dndetailObj.setGoodsReceipt(goodsReceipt);

                        /*
                            * code to save linking date of DN and GR. This
                            * linking date used while calculating due amount of
                            * DN in Aged/SOA report
                            */
                        Date linkingDate = new Date();
//                        Date grDate = goodsReceipt.isIsOpeningBalenceInvoice() ? goodsReceipt.getCreationDate() : goodsReceipt.getJournalEntry().getEntryDate();
//                        Date dnDate = debitNote.isIsOpeningBalenceDN() ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate();
                        Date grDate = goodsReceipt.getCreationDate();
                        Date dnDate = debitNote.getCreationDate();
                        if (maxLinkingDate != null) {
                            maxDate = maxLinkingDate;
                        } else {
                            List<Date> datelist = new ArrayList<Date>();
                            datelist.add(linkingDate);
                            datelist.add(grDate);
                            datelist.add(dnDate);
                            Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                            maxDate = datelist.get(datelist.size()-1);
                            //maxDate = Math.max(Math.max(linkingDate.getTime(), grDate.getTime()), dnDate.getTime());
                        }
                        dndetailObj.setGrLinkDate(maxDate);
                    }
                    double gsOriginalAmt = 0d;
                    if (goodsReceipt.isNormalInvoice()) {
                        gsOriginalAmt = goodsReceipt.getVendorEntry().getAmount();
                    } else {
                        gsOriginalAmt = goodsReceipt.getOriginalOpeningBalanceAmount();
                    }


                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", usedcnamount);       //enter amount for invoice
                    discjson.put("amountinInvCurrency", amountReceivedConverted);        //amount in invoice currency
                    discjson.put("inpercent", false);
                    //        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, cnamount, currencyid, null, externalCurrencyRate);
                    discjson.put("originalamount", gsOriginalAmt);//(Double) bAmt.getEntityList().get(0));
                    discjson.put("companyid", company.getCompanyID());
                    discjson.put("typeOfFigure", typeOfFigure);
                    discjson.put("typeFigure", typeFigure);
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    dndetailObj.setDiscount(discount);
                    dndetailObj.setExchangeRateForTransaction(exchangeRateforTransaction);
                    newdebitNoteDetails.add(dndetailObj);
                }
            } else {
                Set<DebitNoteDetail> cndetails = (Set<DebitNoteDetail>) debitNote.getRows();
                Iterator itr = cndetails.iterator();
                int i = 0;
                while (itr.hasNext()) {
                    dndetailObj = (DebitNoteDetail) itr.next();
                    newdebitNoteDetails.add(dndetailObj);
                    i++;
                }

                dndetailObj = new DebitNoteDetail();

                if (goodsReceipt != null) {
                    dndetailObj.setGoodsReceipt(goodsReceipt);

                    /*
                        * code to save linking date of DN and GR. This linking
                        * date used while calculating due amount of DN in
                        * Aged/SOA report
                        */
                    Date linkingDate = new Date();
//                    Date grDate = goodsReceipt.isIsOpeningBalenceInvoice() ? goodsReceipt.getCreationDate() : goodsReceipt.getJournalEntry().getEntryDate();
//                    Date dnDate = debitNote.isIsOpeningBalenceDN() ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate();
                    Date grDate = goodsReceipt.getCreationDate();
                    Date dnDate = debitNote.getCreationDate();
                    if (maxLinkingDate != null) {
                        maxDate = maxLinkingDate;
                    } else {
                        List<Date> datelist = new ArrayList<Date>();
                        datelist.add(linkingDate);
                        datelist.add(grDate);
                        datelist.add(dnDate);
                        Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                        maxDate = datelist.get(datelist.size() - 1);
//                        maxDate = Math.max(Math.max(linkingDate.getTime(), grDate.getTime()), dnDate.getTime());
                    }
                    dndetailObj.setGrLinkDate(maxDate);
                }

                dndetailObj.setSrno(i + 1);
                dndetailObj.setTotalDiscount(0.00);
                dndetailObj.setCompany(company);
                dndetailObj.setMemo("");
                dndetailObj.setDebitNote(debitNote);
                dndetailObj.setID(UUID.randomUUID().toString());

                double gsOriginalAmt = 0d;;
                if (goodsReceipt.isNormalInvoice()) {
                    gsOriginalAmt = goodsReceipt.getVendorEntry().getAmount();
                } else {// for only opening balance goods receipts
                    gsOriginalAmt = goodsReceipt.getOriginalOpeningBalanceAmount();
                }
                double gsOriginalAmtConverted = gsOriginalAmt;
                gsOriginalAmtConverted = gsOriginalAmt / adjustedRate;
                JSONObject discjson = new JSONObject();
                discjson.put("discount", usedcnamount);  //enter amount for invoice
                discjson.put("amountinInvCurrency", amountReceivedConverted);        //amount in invoice currency
                discjson.put("inpercent", false);
                //        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, cnamount, currencyid, null, externalCurrencyRate);
                discjson.put("originalamount", gsOriginalAmt);//(Double) bAmt.getEntityList().get(0));
                discjson.put("companyid", companyid);
                discjson.put("typeOfFigure", typeOfFigure);
                discjson.put("typeFigure", typeFigure);
                KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                Discount discount = (Discount) dscresult.getEntityList().get(0);
                dndetailObj.setDiscount(discount);
                dndetailObj.setExchangeRateForTransaction(exchangeRateforTransaction);
                newdebitNoteDetails.add(dndetailObj);
            }

            double amountDue = cnamountdue - usedcnamount;
            double externalCurrencyRate = 1d;
            boolean isopeningBalanceCN = debitNote.isIsOpeningBalenceDN();
            Date cnCreationDate = null;
            cnCreationDate = debitNote.getCreationDate();
            externalCurrencyRate = (isopeningBalanceCN && !debitNote.isNormalDN()) ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
//            if (!debitNote.isNormalDN() && debitNote.isIsOpeningBalenceDN()) {
//                cnCreationDate = isopeningBalanceCN ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate();
//                externalCurrencyRate = isopeningBalanceCN ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
//            } else {
//                cnCreationDate = isopeningBalanceCN ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate();
//                externalCurrencyRate = isopeningBalanceCN ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
//            }
            String fromcurrencyid = debitNote.getCurrency().getCurrencyID();            
            if (isopeningBalanceCN && debitNote.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
            }
            double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);

            HashMap<String, Object> debithm = new HashMap<String, Object>();
            debithm.put("dndetails", newdebitNoteDetails);
            debithm.put("dnid", debitNote.getID());
            debithm.put("dnamountdue", amountDue);
            debithm.put("openingBalanceAmountDue", amountDue);
            debithm.put(Constants.openingBalanceBaseAmountDue, totalBaseAmountDue);
            debithm.put("openflag", (cnamountdue - usedcnamount) <= 0 ? false : true);
            if (request.getAttribute("entrynumber") != null) {
                debithm.put("entrynumber", request.getAttribute("entrynumber"));
            }
            if (request.getAttribute("autogenerated") != null) {
                debithm.put("autogenerated", request.getAttribute("autogenerated"));

            }
            if (request.getAttribute("seqformat") != null) {
                debithm.put(Constants.SEQFORMAT, request.getAttribute("seqformat"));

            }
            if (request.getAttribute("seqnumber") != null) {
                debithm.put(Constants.SEQNUMBER, request.getAttribute("seqnumber"));

            }
            if (request.getAttribute(Constants.DATEPREFIX) != null) {
                debithm.put(Constants.DATEPREFIX, request.getAttribute(Constants.DATEPREFIX));
            }
            if (request.getAttribute(Constants.DATESUFFIX) != null) {
                debithm.put(Constants.DATESUFFIX, request.getAttribute(Constants.DATESUFFIX));
            }

            KwlReturnObject result1 = accDebitNoteobj.updateDebitNote(debithm);

            // Update Invoice base amount due. We have to consider Invoice currency rate to calculate.
            externalCurrencyRate = 1d;
            boolean isopeningBalanceINV = goodsReceipt.isIsOpeningBalenceInvoice();
            Date noteCreationDate = null;
            noteCreationDate = debitNote.getCreationDate();
            externalCurrencyRate = (isopeningBalanceCN && !debitNote.isNormalDN()) ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
//            if (!goodsReceipt.isNormalInvoice() && isopeningBalanceINV) {
//                noteCreationDate = isopeningBalanceCN ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate();
//                externalCurrencyRate = isopeningBalanceCN ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
//            } else {
//                noteCreationDate = isopeningBalanceCN ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate();
//                externalCurrencyRate = isopeningBalanceCN ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
//            }
            fromcurrencyid = debitNote.getCurrency().getCurrencyID();
            if (isopeningBalanceINV && goodsReceipt.isConversionRateFromCurrencyToBase()) {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, usedcnamount, fromcurrencyid, noteCreationDate, externalCurrencyRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, usedcnamount, fromcurrencyid, noteCreationDate, externalCurrencyRate);
            }

            totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);
            KwlReturnObject invoiceResult = updateInvoiceAmountDueAndReturnResult(goodsReceipt, company, amountReceivedConverted, totalBaseAmountDue);
            if (invoiceResult != null && invoiceResult.getEntityList() != null && invoiceResult.getEntityList().size() > 0) {
                GoodsReceipt gr = (GoodsReceipt) invoiceResult.getEntityList().get(0);
                if (gr.isIsOpeningBalenceInvoice() && gr.getOpeningBalanceAmountDue() == 0) {
                    try {
                        HashMap<String, Object> dataMap = new HashMap<String, Object>();
                        if (debitNote.isIsOpeningBalenceDN() && debitNote != null && debitNote.getCreationDate() != null) {
                            dataMap.put("amountduedate", maxDate);
                            accGoodsReceiptobj.saveGoodsReceiptAmountDueZeroDate(gr, dataMap);
                        } else if (!debitNote.isIsOpeningBalenceDN() && debitNote != null && debitNote.getJournalEntry() != null && debitNote.getJournalEntry().getCreatedOn() != null) {
                            dataMap.put("amountduedate", maxDate);
                            accGoodsReceiptobj.saveGoodsReceiptAmountDueZeroDate(gr, dataMap);
                        }
                    } catch (Exception ex) {
                        System.out.println("" + ex.getMessage());
                    }
                } else if (gr.getInvoiceamountdue() == 0) {
                    try {
                        HashMap<String, Object> dataMap = new HashMap<String, Object>();
                        if (debitNote.isIsOpeningBalenceDN() && debitNote != null && debitNote.getCreationDate() != null) {
                            dataMap.put("amountduedate", maxDate);
                            accGoodsReceiptobj.saveGoodsReceiptAmountDueZeroDate(gr, dataMap);
                        } else if (!debitNote.isIsOpeningBalenceDN() && debitNote != null && debitNote.getJournalEntry() != null && debitNote.getJournalEntry().getCreatedOn() != null) {
                            dataMap.put("amountduedate", maxDate);
                            accGoodsReceiptobj.saveGoodsReceiptAmountDueZeroDate(gr, dataMap);
                        }
                    } catch (Exception ex) {
                        System.out.println("" + ex.getMessage());
                    }
                }
            }
            if (isInsertAudTrail) {
                auditTrailObj.insertAuditLog(AuditAction.DABIT_NOTE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has linked Debit Note " + debitNote.getDebitNoteNumber() + " with Vendor Invoice " + goodsReceipt.getGoodsReceiptNumber() + ".", request, debitNote.getID());
            }

            /*
                * Start gains/loss calculation Calculate Gains/Loss if Invoice
                * exchange rate changed at the time of linking Note
                */
            String debitid = request.getParameter("noteid");
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            if (preferences.getForeignexchange() == null) {
                throw new AccountingException(messageSource.getMessage("acc.common.forex", null, RequestContextUtils.getLocale(request)));
            }
            if (isopeningBalanceCN && debitNote.isConversionRateFromCurrencyToBase()) {
                externalCurrencyRate = 1 / externalCurrencyRate;
                externalCurrencyRate = externalCurrencyRate;
            }
            Map<String, Object> mapForForexGainLoss = new HashMap<>();
            mapForForexGainLoss.put("dn", debitNote);
            mapForForexGainLoss.put("gr", goodsReceipt);
            mapForForexGainLoss.put("basecurreny", sessionHandlerImpl.getCurrencyID(request));
            mapForForexGainLoss.put("companyid", sessionHandlerImpl.getCompanyid(request));
//            mapForForexGainLoss.put("creationdate", debitNote.isIsOpeningBalenceDN() ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate());
            mapForForexGainLoss.put("creationdate", debitNote.getCreationDate());
            mapForForexGainLoss.put("exchangeratefortransaction", exchangeRateforTransaction);
            mapForForexGainLoss.put("recinvamount", usedcnamount);
            mapForForexGainLoss.put("externalcurrencyrate", externalCurrencyRate);
            mapForForexGainLoss.put("dateformat", authHandler.getDateOnlyFormat(request));
            double amountDiff = getForexGainLossForDebitNote(mapForForexGainLoss);
            if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                boolean rateDecreased = false;
                if (amountDiff < 0) {
                    rateDecreased = true;
                }
                JournalEntry journalEntry = null;
                Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);

                String jeentryNumber = null;
                boolean jeautogenflag = false;
                String jeSeqFormatId = "";
                String datePrefix = "";
                String dateAfterPrefix = "";
                String dateSuffix = "";
                Date entryDate = null;
                if (maxLinkingDate != null) {
                    entryDate = new Date(maxLinkingDate.getTime());
                } else {
                    entryDate = new Date();
                }
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
                    String nextAutoNoTemp = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    int sequence = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                    datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    dateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Suffix Part
                    dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
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
                    jeDataMap.put(Constants.DATEPREFIX, datePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, dateAfterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, dateSuffix);
                }
                jeDataMap.put("entrydate", entryDate);   
                jeDataMap.put("companyid", companyid);
                jeDataMap.put("memo", "Exchange Gains/Loss posted against Debit Note '" + debitNote.getDebitNoteNumber() + "' linked to Invoice '" + goodsReceipt.getGoodsReceiptNumber() + "'");
                jeDataMap.put("currencyid", debitNote.getCurrency().getCurrencyID());
                jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                jeDataMap.put("isexchangegainslossje", true);
                jeDataMap.put("transactionId",debitNote.getID() );
                jeDataMap.put("transactionModuleid", Constants.Acc_Debit_Note_ModuleId);
                journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
                accJournalEntryobj.saveJournalEntryByObject(journalEntry);

                boolean isDebit = rateDecreased ? false : true;
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", 1);
                jedjson.put("companyid", companyid);
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
                jedjson.put("companyid", companyid);
                jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                jedjson.put("accountid", goodsReceipt.getAccount().getID());
                jedjson.put("debit", !isDebit);
                jedjson.put("jeid", journalEntry.getID());
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                detail.add(jed);
                journalEntry.setDetails(detail);
                accJournalEntryobj.saveJournalEntryDetailsSet(detail);

                // Save link JE iformation in DN details
                dndetailObj.setLinkedGainLossJE(journalEntry.getID());
//                newdebitNoteDetails.add(dndetailObj);
                debithm.put("dndetails", newdebitNoteDetails);
                KwlReturnObject result2 = accDebitNoteobj.updateDebitNote(debithm);
                counter++;

            }
            // End of Gain/loss calculations

            //JE For Receipt which is of Opening Type
            
            if (counterMap.containsKey("counter")) {
                counter = (Integer) counterMap.get("counter");
            }
            counterMap.put("counter", counter);
            if (debitNote != null && (debitNote.isIsOpeningBalenceDN() || debitNote.isOtherwise())) {
                String basecurrency = sessionHandlerImpl.getCurrencyID(request);
                double finalAmountReval = ReevalJournalEntryForDebitNote(request, debitNote, amountReceived, exchangeRateforTransaction);
                if (finalAmountReval != 0) {
                    /**
                     * added transactionID and transactionModuleID to Realised
                     * JE.
                     */
                    counterMap.put("transactionModuleid", debitNote.isIsOpeningBalenceDN() ? (debitNote.isdNForVendor() ? Constants.Acc_opening_Vendor_DebitNote : Constants.Acc_opening_Customer_DebitNote) : Constants.Acc_Debit_Note_ModuleId);
                    counterMap.put("transactionId", debitNote.getID());
                    String revaljeid = PostJEFORReevaluation(request, finalAmountReval, companyid, preferences, basecurrency, dndetailObj.getRevalJeId(), counterMap);
                    dndetailObj.setRevalJeId(revaljeid);
                }
            }
            //JE For Debit which is Linked to Receipt
            counter = (Integer) counterMap.get("counter");
            counterMap.put("counter", counter);
            if (goodsReceipt != null) {
                double finalAmountReval = ReevalJournalEntryForGoodsReceipt(request, goodsReceipt, amountReceived, exchangeRateforTransaction);
                if (finalAmountReval != 0) {
                    String basecurrency = sessionHandlerImpl.getCurrencyID(request);
                    /**
                     * added transactionID and transactionModuleID to Realised
                     * JE.
                     */
                    counterMap.put("transactionModuleid", goodsReceipt.isIsOpeningBalenceInvoice() ? Constants.Acc_opening_Prchase_Invoice : Constants.Acc_Vendor_Invoice_ModuleId);
                    counterMap.put("transactionId", goodsReceipt.getID());
                    String revaljeid = PostJEFORReevaluation(request, -(finalAmountReval), companyid, preferences, basecurrency, dndetailObj.getRevalJeIdInvoice(), counterMap);
                    dndetailObj.setRevalJeIdInvoice(revaljeid);

                }
            }

            /*
                * saving linking informaion of DebitNote while linking with
                * purchase invoice
                */

            if (goodsReceipt != null) {
                HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                requestParamsLinking.put("linkeddocid", cnid);
                requestParamsLinking.put("docid", goodsReceipt.getID());
                requestParamsLinking.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                requestParamsLinking.put("linkeddocno", entryNumber);
                requestParamsLinking.put("sourceflag", 0);
                KwlReturnObject result3 = accGoodsReceiptobj.saveVILinking(requestParamsLinking);


                /*
                    * saving linking informaion of Purchase Invoice while
                    * linking with DebitNote
                    */

                requestParamsLinking.put("linkeddocid", goodsReceipt.getID());
                requestParamsLinking.put("docid", cnid);
                requestParamsLinking.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                requestParamsLinking.put("linkeddocno", goodsReceipt.getGoodsReceiptNumber());
                requestParamsLinking.put("sourceflag", 1);
                result1 = accDebitNoteobj.saveDebitNoteLinking(requestParamsLinking);
            }   
        }
    } catch (AccountingException ex) {
        throw new AccountingException(ex.getMessage(), ex);
    } catch (SessionExpiredException ex) {
        throw ServiceException.FAILURE(ex.getMessage(), ex);
    } catch (Exception ex) {
        Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        throw ServiceException.FAILURE(ex.getMessage(), ex);
    }
    finally{
        status = txnManager.getTransaction(def1);
        deleteTemporaryInvoicesEntries(invoiceArray, companyid);
        txnManager.commit(status);
    }
    return result;

}

public List linkDebitNoteToCreditNote(HttpServletRequest request, JSONArray noteArray, String debitNoteId, Boolean isInsertAudTrail, Map<String, Object> counterMap) throws ServiceException, SessionExpiredException {
    //public List linkDebitNoteToInvoices(HttpServletRequest request, String debitNoteId, Boolean isInsertAudTrail) throws ServiceException, SessionExpiredException, JSONException, AccountingException {
    List result = new ArrayList();
    try {
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String cnid = "";
        int counter = 0;
        if (!StringUtil.isNullOrEmpty(debitNoteId)) {
            cnid = debitNoteId;
        } else {
            cnid = request.getParameter("cnid");
        }

//        Date maxLinkingDate = null;
//        String linkingdate = (String) request.getParameter("linkingdate");
//        DateFormat dateformat = authHandler.getDateOnlyFormat();
//        if (!StringUtil.isNullOrEmpty(linkingdate)) {
//            try {
//                maxLinkingDate = dateformat.parse(linkingdate);
//            } catch (ParseException ex) {
//                Logger.getLogger(accDebitNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }

        String entryNumber = request.getParameter("number");
        String amounts[] = request.getParameter("amounts").split(",");
        String invoiceDetails = request.getParameter("invoicedetails");

        String baseCurrency = sessionHandlerImpl.getCurrencyID(request);
        for (int k = 0; k < noteArray.length(); k++) {
            JSONObject jobj = noteArray.getJSONObject(k);

            if (StringUtil.isNullOrEmpty(jobj.getString("documentid"))) {
                continue;
            }

            double usedcnamount = 0d;
            double typeFigure = 0d;
            int typeOfFigure = 1;
            usedcnamount = jobj.optDouble("linkamount", 0.0d);
//            if (!StringUtil.isNullOrEmpty(amounts[k])) {
//                usedcnamount = Double.parseDouble((String) amounts[k]);
//            } else {
//                usedcnamount = 0;
//            }

            if (usedcnamount == 0) {
                continue;
            }
            if (!StringUtil.isNullOrEmpty(jobj.optString("typeFigure"))) {
                typeFigure = jobj.optDouble("typeFigure", 0.0);
            }
            if (!StringUtil.isNullOrEmpty(jobj.optString("typeOfFigure"))) {
                typeOfFigure = jobj.optInt("typeOfFigure", 1);
            }
            KwlReturnObject grresult = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), jobj.getString("documentid"));
            CreditNote creditNote = (CreditNote) grresult.getEntityList().get(0);

            KwlReturnObject cnObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), cnid);
            DebitNote debitNote = (DebitNote) cnObj.getEntityList().get(0);

            grresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) grresult.getEntityList().get(0);

            Set<DebitNoteDetail> newdebitNoteDetails = new HashSet<DebitNoteDetail>();

            double cnamountdue = debitNote.getDnamountdue();

            if (!debitNote.isOpenflag() || cnamountdue <= 0) {
                throw new AccountingException(messageSource.getMessage("acc.field.DebitNotehasbeenalreadyutilized", null, RequestContextUtils.getLocale(request)));
            }

            double amountReceived = usedcnamount;           //amount of DN 
            double amountReceivedConverted = usedcnamount;
            double adjustedRate = 1;

            if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(creditNote.getCurrency().getCurrencyID()) && !creditNote.getCurrency().getCurrencyID().equals(debitNote.getCurrency().getCurrencyID())) {
                adjustedRate = Double.parseDouble(jobj.get("exchangeratefortransaction").toString());
                amountReceivedConverted = amountReceived / adjustedRate;
                amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
            } else {
                amountReceivedConverted = authHandler.round(amountReceived, companyid);
            }
            DebitNoteDetail dndetailObj = new DebitNoteDetail();
            Date maxDate = null;
            Date maxLinkingDate = null;
            if (jobj.has("linkingdate") && jobj.get("linkingdate") != null) {
                String linkingdate = (String) jobj.get("linkingdate");
                DateFormat dateformat = authHandler.getDateOnlyFormat();
                if (!StringUtil.isNullOrEmpty(linkingdate)) {
                    try {
                        maxLinkingDate = dateformat.parse(linkingdate);
                    } catch (ParseException ex) {
                        Logger.getLogger(accDebitNoteServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if (debitNote.getDntype() != 3 && debitNote.isOpenflag() && debitNote.getDnamount() == debitNote.getDnamountdue()) {//If DN is used for first time.
                Set<DebitNoteDetail> debitNoteDetails = (Set<DebitNoteDetail>) debitNote.getRows();
                Iterator itr = debitNoteDetails.iterator();
                while (itr.hasNext()) {
                    dndetailObj = (DebitNoteDetail) itr.next();

                    if (creditNote != null) {
                        dndetailObj.setCreditNoteId(creditNote.getID());

                        /*
                            * code to save linking date of DN and GR. This
                            * linking date used while calculating due amount of
                            * DN in Aged/SOA report
                            */
                        Date linkingDate = new Date();
                        Date grDate = creditNote.getCreationDate();
                        Date dnDate = debitNote.getCreationDate();
                        if (maxLinkingDate != null) {
                            maxDate = maxLinkingDate;
                        } else {
                            List<Date> datelist = new ArrayList<Date>();
                            datelist.add(linkingDate);
                            datelist.add(grDate);
                            datelist.add(dnDate);
                            Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                            maxDate = datelist.get(datelist.size()-1);
                            //maxDate = Math.max(Math.max(linkingDate.getTime(), grDate.getTime()), dnDate.getTime());
                        }
                        dndetailObj.setGrLinkDate(maxDate);
                    }
                    double gsOriginalAmt = 0d;
                    if (creditNote.isNormalCN()) {
                        gsOriginalAmt = creditNote.getCustomerEntry().getAmount();
                    } else {
                        gsOriginalAmt = creditNote.getCnamountdue();
                    }


                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", usedcnamount);       //enter amount for invoice
                    discjson.put("amountinInvCurrency", amountReceivedConverted);        //amount in invoice currency
                    discjson.put("inpercent", false);
                    //        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, cnamount, currencyid, null, externalCurrencyRate);
                    discjson.put("originalamount", gsOriginalAmt);//(Double) bAmt.getEntityList().get(0));
                    discjson.put("companyid", company.getCompanyID());
                    discjson.put("typeOfFigure", typeOfFigure);
                    discjson.put("typeFigure", typeFigure);
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    dndetailObj.setDiscount(discount);
                    dndetailObj.setExchangeRateForTransaction(adjustedRate);
                    newdebitNoteDetails.add(dndetailObj);
                }
            } else {
                Set<DebitNoteDetail> cndetails = (Set<DebitNoteDetail>) debitNote.getRows();
                Iterator itr = cndetails.iterator();
                int i = 0;
                while (itr.hasNext()) {
                    dndetailObj = (DebitNoteDetail) itr.next();
                    newdebitNoteDetails.add(dndetailObj);
                    i++;
                }

                dndetailObj = new DebitNoteDetail();

                if (creditNote != null) {
                    dndetailObj.setCreditNoteId(creditNote.getID());

                    /*
                        * code to save linking date of DN and GR. This linking
                        * date used while calculating due amount of DN in
                        * Aged/SOA report
                        */
                    Date linkingDate = new Date();
//                    Date grDate = creditNote.isIsOpeningBalenceCN() ? creditNote.getCreationDate() : creditNote.getJournalEntry().getEntryDate();
//                    Date dnDate = debitNote.isIsOpeningBalenceDN() ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate();
                    Date grDate = creditNote.getCreationDate();
                    Date dnDate = debitNote.getCreationDate();
                    if (maxLinkingDate != null) {
                        maxDate = maxLinkingDate;
                    } else {
                        List<Date> datelist = new ArrayList<Date>();
                        datelist.add(linkingDate);
                        datelist.add(grDate);
                        datelist.add(dnDate);
                        Collections.sort(datelist); //Sort the Date object & get the dates in ASC order. Pick-up the last record as Max Date
                        maxDate = datelist.get(datelist.size() - 1);
//                        maxDate = Math.max(Math.max(linkingDate.getTime(), grDate.getTime()), dnDate.getTime());
                    }
                    dndetailObj.setGrLinkDate(maxDate);
                }

                dndetailObj.setSrno(i + 1);
                dndetailObj.setTotalDiscount(0.00);
                dndetailObj.setCompany(company);
                dndetailObj.setMemo("");
                dndetailObj.setDebitNote(debitNote);
                dndetailObj.setID(UUID.randomUUID().toString());

                double gsOriginalAmt = 0d;;
                if (creditNote.isNormalCN()) {
                    gsOriginalAmt = creditNote.getCustomerEntry().getAmount();
                } else {// for only opening balance goods receipts
                    gsOriginalAmt = creditNote.getCnamountdue();
                }
                double gsOriginalAmtConverted = gsOriginalAmt;
                gsOriginalAmtConverted = gsOriginalAmt / adjustedRate;
                JSONObject discjson = new JSONObject();
                discjson.put("discount", usedcnamount);  //enter amount for invoice
                discjson.put("amountinInvCurrency", amountReceivedConverted);        //amount in invoice currency
                discjson.put("inpercent", false);
                //        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, cnamount, currencyid, null, externalCurrencyRate);
                discjson.put("originalamount", gsOriginalAmt);//(Double) bAmt.getEntityList().get(0));
                discjson.put("companyid", companyid);
                discjson.put("typeOfFigure", typeOfFigure);
                discjson.put("typeFigure", typeFigure);
                KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                Discount discount = (Discount) dscresult.getEntityList().get(0);
                dndetailObj.setDiscount(discount);
                dndetailObj.setExchangeRateForTransaction(adjustedRate);
                newdebitNoteDetails.add(dndetailObj);
            }

            double amountDue = cnamountdue - usedcnamount;
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            double externalCurrencyRate = 1d;
            boolean isopeningBalanceCN = debitNote.isIsOpeningBalenceDN();
            Date cnCreationDate = null;
                cnCreationDate = debitNote.getCreationDate();
                externalCurrencyRate = (isopeningBalanceCN && !debitNote.isNormalDN()) ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
//            if (!debitNote.isNormalDN() && debitNote.isIsOpeningBalenceDN()) {
//                cnCreationDate = isopeningBalanceCN ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate();
//                externalCurrencyRate = isopeningBalanceCN ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
//            } else {
//                cnCreationDate = isopeningBalanceCN ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate();
//                externalCurrencyRate = isopeningBalanceCN ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
//            }
            String fromcurrencyid = debitNote.getCurrency().getCurrencyID();
            KwlReturnObject bAmt = null;
            if (isopeningBalanceCN && debitNote.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountDue, fromcurrencyid, cnCreationDate, externalCurrencyRate);
            }
            double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);

            HashMap<String, Object> debithm = new HashMap<String, Object>();
            debithm.put("dndetails", newdebitNoteDetails);
            debithm.put("dnid", debitNote.getID());
            debithm.put("dnamountdue", amountDue);
            debithm.put("openingBalanceAmountDue", amountDue);
            debithm.put(Constants.openingBalanceBaseAmountDue, totalBaseAmountDue);
            debithm.put("openflag", (cnamountdue - usedcnamount) <= 0 ? false : true);
            if (request.getAttribute("entrynumber") != null) {
                debithm.put("entrynumber", request.getAttribute("entrynumber"));
            }
            if (request.getAttribute("autogenerated") != null) {
                debithm.put("autogenerated", request.getAttribute("autogenerated"));

            }
            if (request.getAttribute("seqformat") != null) {
                debithm.put(Constants.SEQFORMAT, request.getAttribute("seqformat"));

            }
            if (request.getAttribute("seqnumber") != null) {
                debithm.put(Constants.SEQNUMBER, request.getAttribute("seqnumber"));

            }
            if (request.getAttribute(Constants.DATEPREFIX) != null) {
                debithm.put(Constants.DATEPREFIX, request.getAttribute(Constants.DATEPREFIX));
            }
            if (request.getAttribute(Constants.DATESUFFIX) != null) {
                debithm.put(Constants.DATESUFFIX, request.getAttribute(Constants.DATESUFFIX));
            }

            KwlReturnObject result1 = accDebitNoteobj.updateDebitNote(debithm);

            // Update Invoice base amount due. We have to consider Invoice currency rate to calculate.
            externalCurrencyRate = 1d;
            boolean isopeningBalanceINV = creditNote.isIsOpeningBalenceCN();
            Date noteCreationDate = null;
            externalCurrencyRate = (isopeningBalanceCN && !debitNote.isNormalDN()) ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
            noteCreationDate = debitNote.getCreationDate();
//            if (!creditNote.isNormalCN() && isopeningBalanceINV) {
//                noteCreationDate = isopeningBalanceCN ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate();
//                externalCurrencyRate = isopeningBalanceCN ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
//            } else {
//                noteCreationDate = isopeningBalanceCN ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate();
//                externalCurrencyRate = isopeningBalanceCN ? debitNote.getExchangeRateForOpeningTransaction() : debitNote.getJournalEntry().getExternalCurrencyRate();
//            }
            fromcurrencyid = debitNote.getCurrency().getCurrencyID();
            if (isopeningBalanceINV && creditNote.isConversionRateFromCurrencyToBase()) {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, usedcnamount, fromcurrencyid, noteCreationDate, externalCurrencyRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, usedcnamount, fromcurrencyid, noteCreationDate, externalCurrencyRate);
            }

            totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);
            KwlReturnObject invoiceResult = updateCreditNoteAmountDueAndReturnResult(creditNote, company, amountReceivedConverted, totalBaseAmountDue);
//            if (invoiceResult != null && invoiceResult.getEntityList() != null && invoiceResult.getEntityList().size() > 0) {
//                GoodsReceipt gr = (GoodsReceipt) invoiceResult.getEntityList().get(0);
//                if (gr.isIsOpeningBalenceInvoice() && gr.getOpeningBalanceAmountDue() == 0) {
//                    try {
//                        HashMap<String, Object> dataMap = new HashMap<String, Object>();
//                        if (debitNote.isIsOpeningBalenceDN() && debitNote != null && debitNote.getCreationDate() != null) {
//                            dataMap.put("amountduedate", new Date(maxDate));
//                            accGoodsReceiptobj.saveGoodsReceiptAmountDueZeroDate(gr, dataMap);
//                        } else if (!debitNote.isIsOpeningBalenceDN() && debitNote != null && debitNote.getJournalEntry() != null && debitNote.getJournalEntry().getCreatedOn() != null) {
//                            dataMap.put("amountduedate", new Date(maxDate));
//                            accGoodsReceiptobj.saveGoodsReceiptAmountDueZeroDate(gr, dataMap);
//                        }
//                    } catch (Exception ex) {
//                        System.out.println("" + ex.getMessage());
//                    }
//                } else if (gr.getInvoiceamountdue() == 0) {
//                    try {
//                        HashMap<String, Object> dataMap = new HashMap<String, Object>();
//                        if (debitNote.isIsOpeningBalenceDN() && debitNote != null && debitNote.getCreationDate() != null) {
//                            dataMap.put("amountduedate", new Date(maxDate));
//                            accGoodsReceiptobj.saveGoodsReceiptAmountDueZeroDate(gr, dataMap);
//                        } else if (!debitNote.isIsOpeningBalenceDN() && debitNote != null && debitNote.getJournalEntry() != null && debitNote.getJournalEntry().getCreatedOn() != null) {
//                            dataMap.put("amountduedate", new Date(maxDate));
//                            accGoodsReceiptobj.saveGoodsReceiptAmountDueZeroDate(gr, dataMap);
//                        }
//                    } catch (Exception ex) {
//                        System.out.println("" + ex.getMessage());
//                    }
//                }
//            }
            if (isInsertAudTrail) {
                auditTrailObj.insertAuditLog(AuditAction.DABIT_NOTE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has linked Debit Note " + debitNote.getDebitNoteNumber() + " with Credit Note " + creditNote.getCreditNoteNumber() + ".", request, debitNote.getID());
            }

            /*
                * Start gains/loss calculation Calculate Gains/Loss if Invoice
                * exchange rate changed at the time of linking Note
                */
            String debitid = request.getParameter("noteid");
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            if (preferences.getForeignexchange() == null) {
                throw new AccountingException(messageSource.getMessage("acc.common.forex", null, RequestContextUtils.getLocale(request)));
            }
            if (isopeningBalanceCN && debitNote.isConversionRateFromCurrencyToBase()) {
                externalCurrencyRate = 1 / externalCurrencyRate;
            }

            Map<String, Object> mapForForexGainLoss = new HashMap<>();
            mapForForexGainLoss.put("dn", debitNote);
            mapForForexGainLoss.put("cn", creditNote);
            mapForForexGainLoss.put("basecurreny", baseCurrency);
            mapForForexGainLoss.put("companyid", sessionHandlerImpl.getCompanyid(request));
//            mapForForexGainLoss.put("creationdate", debitNote.isIsOpeningBalenceDN() ? debitNote.getCreationDate() : debitNote.getJournalEntry().getEntryDate());
            mapForForexGainLoss.put("creationdate", debitNote.getCreationDate());
            mapForForexGainLoss.put("exchangeratefortransaction", Double.parseDouble(jobj.optString("exchangeratefortransaction", "1")));
            mapForForexGainLoss.put("recinvamount", usedcnamount);
            mapForForexGainLoss.put("externalcurrencyrate", externalCurrencyRate);
            mapForForexGainLoss.put("dateformat", authHandler.getDateOnlyFormat(request));
            double amountDiff = checkFxGainLossOnLinkCreditNote(mapForForexGainLoss);
            if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                boolean rateDecreased = false;
                if (amountDiff < 0) {
                    rateDecreased = true;
                }
                JournalEntry journalEntry = null;
                Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);

                String jeentryNumber = null;
                boolean jeautogenflag = false;
                String jeSeqFormatId = "";
                String datePrefix = "";
                String dateAfterPrefix = "";
                String dateSuffix = "";
                Date entryDate = null;
                if (maxLinkingDate != null) {
                    entryDate = new Date(maxLinkingDate.getTime());
                } else {
                    entryDate = new Date();
                }
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
                    String nextAutoNoTemp = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    int sequence = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                    datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    dateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
                    dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
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
                    jeDataMap.put(Constants.DATEPREFIX, datePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, dateAfterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, dateSuffix);
                }
                jeDataMap.put("entrydate", entryDate);
                jeDataMap.put("companyid", companyid);
                jeDataMap.put("memo", "Exchange Gains/Loss posted against Debit Note '" + debitNote.getDebitNoteNumber() + "' linked to Credit Note '" + creditNote.getCreditNoteNumber() + "'");
                jeDataMap.put("currencyid", debitNote.getCurrency().getCurrencyID());
                jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                jeDataMap.put("isexchangegainslossje", true);
                jeDataMap.put("transactionId",debitNote.getID() );
                jeDataMap.put("transactionModuleid", Constants.Acc_Debit_Note_ModuleId);
                journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
                accJournalEntryobj.saveJournalEntryByObject(journalEntry);

                boolean isDebit = rateDecreased ? false : true;
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", 1);
                jedjson.put("companyid", companyid);
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
                jedjson.put("companyid", companyid);
                jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                jedjson.put("accountid", creditNote.getAccount().getID());
                jedjson.put("debit", !isDebit);
                jedjson.put("jeid", journalEntry.getID());
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                detail.add(jed);
                journalEntry.setDetails(detail);
                accJournalEntryobj.saveJournalEntryDetailsSet(detail);

                // Save link JE iformation in DN details
                dndetailObj.setLinkedGainLossJE(journalEntry.getID());
                newdebitNoteDetails.add(dndetailObj);
                debithm.put("dndetails", newdebitNoteDetails);
                KwlReturnObject result2 = accDebitNoteobj.updateDebitNote(debithm);
                counter++;

            }
            // End of Gain/loss calculations

            //JE For Receipt which is of Opening Type
            double exchangeRateforTransaction = jobj.optDouble("exchangeratefortransaction", 1.0);
            if (counterMap.containsKey("counter")) {
                counter = (Integer) counterMap.get("counter");
            }
            counterMap.put("counter", counter);
            if (debitNote != null && (debitNote.isIsOpeningBalenceDN() || debitNote.isOtherwise())) {
                String basecurrency = sessionHandlerImpl.getCurrencyID(request);
                double finalAmountReval = ReevalJournalEntryForDebitNote(request, debitNote, amountReceived, exchangeRateforTransaction);
                if (finalAmountReval != 0) {
                    /**
                     * added transactionID and transactionModuleID to Realised
                     * JE.
                     */
                    counterMap.put("transactionModuleid", debitNote.isIsOpeningBalenceDN() ? (debitNote.isdNForVendor() ? Constants.Acc_opening_Vendor_DebitNote : Constants.Acc_opening_Customer_DebitNote) : Constants.Acc_Debit_Note_ModuleId);
                    counterMap.put("transactionId", debitNote.getID());
                    String revaljeid = PostJEFORReevaluation(request, finalAmountReval, companyid, preferences, basecurrency, dndetailObj.getRevalJeId(), counterMap);
                    dndetailObj.setRevalJeId(revaljeid);
                }
            }
            //JE For Debit which is Linked to Receipt
            counter = (Integer) counterMap.get("counter");
            counterMap.put("counter", counter);
            if (creditNote != null) {
                double finalAmountReval = ReevalJournalEntryForCreditNote(request, creditNote, amountReceived, exchangeRateforTransaction);
                if (finalAmountReval != 0) {
                    String basecurrency = sessionHandlerImpl.getCurrencyID(request);
                    /**
                     * added transactionID and transactionModuleID to Realised
                     * JE.
                     */
                    counterMap.put("transactionModuleid", creditNote.isIsOpeningBalenceCN() ? (creditNote.iscNForCustomer() ? Constants.Acc_opening_Customer_CreditNote : Constants.Acc_opening_Vendor_CreditNote) : Constants.Acc_Credit_Note_ModuleId);
                    counterMap.put("transactionId", creditNote.getID());
                    String revaljeid = PostJEFORReevaluation(request, -(finalAmountReval), companyid, preferences, basecurrency, dndetailObj.getRevalJeIdInvoice(), counterMap);
                    dndetailObj.setRevalJeIdInvoice(revaljeid);

                }
            }

            /*
                * saving linking informaion of DebitNote while linking with
                * purchase invoice
                */

            if (creditNote != null) {
                HashMap<String, Object> requestParamsLinking = new HashMap<>();
                requestParamsLinking.put("linkeddocid", creditNote.getID());
                requestParamsLinking.put("docid", cnid);
                requestParamsLinking.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
                requestParamsLinking.put("linkeddocno", creditNote.getCreditNoteNumber());
                requestParamsLinking.put("sourceflag", 1);
                KwlReturnObject result3 = accDebitNoteobj.saveDebitNoteLinking(requestParamsLinking);


                /*
                    * saving linking informaion of Credit Note while linking
                    * with DebitNote
                    */
                requestParamsLinking.put("linkeddocid", cnid);
                requestParamsLinking.put("docid", creditNote.getID());
                requestParamsLinking.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
                requestParamsLinking.put("linkeddocno", entryNumber);
                requestParamsLinking.put("sourceflag", 0);
                result1 = accCreditNoteDAOobj.saveCreditNoteLinking(requestParamsLinking);
                
            }
        }
    } catch (SessionExpiredException ex) {
        throw ServiceException.FAILURE(ex.getMessage(), ex);
    } catch (Exception ex) {
        Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        throw ServiceException.FAILURE(ex.getMessage(), ex);
    }
    return result;

}

/*
    * Check forex gain loss for Debit Note linked with Credit Note
    */
public double checkFxGainLossOnLinkCreditNote(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException {
    double amount = 0, actualAmount = 0;
    try {
        String basecurrency = requestParams.get("basecurreny").toString();
        String companyid = requestParams.get("companyid").toString();
        DateFormat dateformat = (DateFormat) requestParams.get("dateformat");
        HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
        GlobalParams.put("companyid", companyid);
        GlobalParams.put("gcurrencyid", basecurrency);
        GlobalParams.put("dateformat", dateformat);
        Date creationDate = (Date) requestParams.get("creationdate");
        DebitNote dn = (DebitNote) requestParams.get("dn");
        String currencyid = dn.getCurrency().getCurrencyID();
        CreditNote creditNote = (CreditNote) requestParams.get("cn");
        double exchangeratefortransaction = (double) requestParams.get("exchangeratefortransaction");
        double recinvamount = (double) requestParams.get("recinvamount");
        double ratio = 0;
        double newrate = 0.0;
        boolean revalFlag = false;
        boolean isopeningBalanceInvoice = creditNote.isIsOpeningBalenceCN();

        boolean isopeningBalancePayment = dn.isIsOpeningBalenceDN();
        boolean isConversionRateFromCurrencyToBase = dn.isConversionRateFromCurrencyToBase();
        double externalCurrencyRate = (double) requestParams.get("externalcurrencyrate");
        double exchangeRate = 0d;
        Date goodsReceiptCreationDate = null;
        if (creditNote.isNormalCN()) {
            exchangeRate = creditNote.getJournalEntry().getExternalCurrencyRate();
//            goodsReceiptCreationDate = creditNote.getJournalEntry().getEntryDate();
        } else {
            exchangeRate = creditNote.getExchangeRateForOpeningTransaction();
            if (creditNote.isConversionRateFromCurrencyToBase()) {
                exchangeRate = 1 / exchangeRate;
            }
        }
        goodsReceiptCreationDate = creditNote.getCreationDate();


        HashMap<String, Object> invoiceId = new HashMap<String, Object>();
        invoiceId.put("invoiceid", creditNote.getID());
        invoiceId.put("companyid", companyid);
        KwlReturnObject result = null;
        result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
        RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
        if (history != null) {
            exchangeRate = history.getEvalrate();
            newrate = exchangeratefortransaction;
            revalFlag = true;
        }
//                }
        result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
        KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
        String currid = currency.getCurrencyID();
        if (creditNote.getCurrency() != null) {
            currid = creditNote.getCurrency().getCurrencyID();
        }

        KwlReturnObject bAmt = null;
        if (currid.equalsIgnoreCase(currencyid)) {
            double paymentExternalCurrencyRate = externalCurrencyRate;
            // If document is revaluated then document from same currency are linked on same rate i.e revaluation rate. 
            if (exchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
            } else {
                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
            }
        } else {
            double paymentExternalCurrencyRate = externalCurrencyRate;
            if (exchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
            } else {
                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
            }
        }
        double oldrate = (Double) bAmt.getEntityList().get(0);
        if (exchangeratefortransaction != oldrate && exchangeratefortransaction != 0.0 && Math.abs(exchangeratefortransaction - oldrate) >= 0.000001) {
            newrate = exchangeratefortransaction;
            ratio = oldrate - newrate;
            amount = (recinvamount - (recinvamount / newrate) * oldrate) / newrate;
            KwlReturnObject bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, amount, currencyid, creationDate, newrate);
            actualAmount += (Double) bAmtActual.getEntityList().get(0);
        } else {
            if (currid.equalsIgnoreCase(currencyid)) {
                double paymentExternalCurrencyRate = externalCurrencyRate;
                if (exchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                }
            } else {
                double paymentExternalCurrencyRate = externalCurrencyRate;
                if (exchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                }
            }
            if (!revalFlag) {
                newrate = (Double) bAmt.getEntityList().get(0);
            }
            if (Math.abs(exchangeratefortransaction - oldrate) >= 0.000001) {
                ratio = oldrate - newrate;
            }
            amount = recinvamount * ratio;
            KwlReturnObject bAmtActual = null;
            if (isopeningBalancePayment && isConversionRateFromCurrencyToBase) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, amount, currencyid, creationDate, externalCurrencyRate);
            } else {
                bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, amount, currencyid, creationDate, externalCurrencyRate);
            }
            actualAmount += (Double) bAmtActual.getEntityList().get(0);
        }

    } catch (Exception ex) {
        throw ServiceException.FAILURE("getForexGainLossFordebitNote : " + ex.getMessage(), ex);
    }
    return (actualAmount);
}
/*
    * Check forex gain loss for Debit Note linked with Goods Receipt
    */

public double getForexGainLossForDebitNote(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException {
    double amount = 0, actualAmount = 0;
    try {
        String basecurrency = requestParams.get("basecurreny").toString();
        String companyid = requestParams.get("companyid").toString();
        DateFormat dateformat = (DateFormat) requestParams.get("dateformat");
        HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
        GlobalParams.put("companyid", companyid);
        GlobalParams.put("gcurrencyid", basecurrency);
        GlobalParams.put("dateformat", dateformat);
        Date creationDate = (Date) requestParams.get("creationdate");
        DebitNote dn = (DebitNote) requestParams.get("dn");
        String currencyid = dn.getCurrency().getCurrencyID();
        GoodsReceipt gr = (GoodsReceipt) requestParams.get("gr");
        double exchangeratefortransaction = (double) requestParams.get("exchangeratefortransaction");
        double recinvamount = (double) requestParams.get("recinvamount");
        double ratio = 0;
        double newrate = 0.0;
        boolean revalFlag = false;
        boolean isopeningBalanceInvoice = gr.isIsOpeningBalenceInvoice();

        boolean isopeningBalancePayment = dn.isIsOpeningBalenceDN();
        boolean isConversionRateFromCurrencyToBase = dn.isConversionRateFromCurrencyToBase();
        double externalCurrencyRate = (double) requestParams.get("externalcurrencyrate");
        double exchangeRate = 0d;
        Date goodsReceiptCreationDate = null;
        if (gr.isNormalInvoice()) {
            exchangeRate = gr.getJournalEntry().getExternalCurrencyRate();
//            goodsReceiptCreationDate = gr.getJournalEntry().getEntryDate();
        } else {
            exchangeRate = gr.getExchangeRateForOpeningTransaction();
            if (gr.isConversionRateFromCurrencyToBase()) {
                exchangeRate = 1 / exchangeRate;
            }
        }
        goodsReceiptCreationDate = gr.getCreationDate();


        HashMap<String, Object> invoiceId = new HashMap<String, Object>();
        invoiceId.put("invoiceid", gr.getID());
        invoiceId.put("companyid", companyid);
        KwlReturnObject result = null;
        result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
        RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
        if (history != null) {
            exchangeRate = history.getEvalrate();
            newrate = exchangeratefortransaction;
            revalFlag = true;
        }
//                }
        result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
        KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
        String currid = currency.getCurrencyID();
        if (gr.getCurrency() != null) {
            currid = gr.getCurrency().getCurrencyID();
        }

        KwlReturnObject bAmt = null;
        if (currid.equalsIgnoreCase(currencyid)) {
            double paymentExternalCurrencyRate = externalCurrencyRate;
            // If document is revaluated then document from same currency are linked on same rate i.e revaluation rate. 
            if (exchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
            } else {
                bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
            }
        } else {
            double paymentExternalCurrencyRate = externalCurrencyRate;
            if (exchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
            } else {
                bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
            }
        }
        double oldrate = (Double) bAmt.getEntityList().get(0);
        if (exchangeratefortransaction != oldrate && exchangeratefortransaction != 0.0 && Math.abs(exchangeratefortransaction - oldrate) >= 0.000001) {
            newrate = exchangeratefortransaction;
            ratio = oldrate - newrate;
            amount = (recinvamount - (recinvamount / newrate) * oldrate) / newrate;
            KwlReturnObject bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, amount, currencyid, creationDate, newrate);
            actualAmount += (Double) bAmtActual.getEntityList().get(0);
        } else {
            if (currid.equalsIgnoreCase(currencyid)) {
                double paymentExternalCurrencyRate = externalCurrencyRate;
                if (exchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                }
            } else {
                double paymentExternalCurrencyRate = externalCurrencyRate;
                if (exchangeRate != paymentExternalCurrencyRate && !revalFlag) {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherWithDiffRates(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate, paymentExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(GlobalParams, 1.0, currid, currencyid, goodsReceiptCreationDate, exchangeRate);
                }
            }
            if (!revalFlag) {
                newrate = (Double) bAmt.getEntityList().get(0);
            }
            if (Math.abs(exchangeratefortransaction - oldrate) >= 0.000001) {
                ratio = oldrate - newrate;
            }
            amount = recinvamount * ratio;
            KwlReturnObject bAmtActual = null;
            if (isopeningBalancePayment && isConversionRateFromCurrencyToBase) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, amount, currencyid, creationDate, externalCurrencyRate);
            } else {
                bAmtActual = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, amount, currencyid, creationDate, externalCurrencyRate);
            }
            actualAmount += (Double) bAmtActual.getEntityList().get(0);
        }

    } catch (Exception ex) {
        throw ServiceException.FAILURE("getForexGainLossFordebitNote : " + ex.getMessage(), ex);
    }
    return (actualAmount);
}

/*
    * Revalaution Entery for Goods Receipt
    */
public double ReevalJournalEntryForGoodsReceipt(HttpServletRequest request, GoodsReceipt goodsReceipt, double linkInvoiceAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {
    double finalAmountReval = 0;
    try {
        String basecurrency = sessionHandlerImpl.getCurrencyID(request);
        double ratio = 0;
        double amountReval = 0;
        String revalId = null;
        Date tranDate = null;
        double exchangeRate = 0.0;
        double exchangeRateReval = 0.0;
        double amountdue = linkInvoiceAmount;
        Map<String, Object> GlobalParams = new HashMap<>();
        GlobalParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
        GlobalParams.put("gcurrencyid", basecurrency);
        GlobalParams.put("dateformat", authHandler.getDateOnlyFormat(request));
        Date creationDate = goodsReceipt.getCreationDate();
        boolean isopeningBalanceInvoice = goodsReceipt.isIsOpeningBalenceInvoice();
        tranDate = goodsReceipt.getCreationDate();
        if (!goodsReceipt.isNormalInvoice()) {
            exchangeRate = goodsReceipt.getExchangeRateForOpeningTransaction();
            exchangeRateReval = exchangeRate;
        } else {
            exchangeRate = goodsReceipt.getJournalEntry().getExternalCurrencyRate();
            exchangeRateReval = exchangeRate;
//            tranDate = goodsReceipt.getJournalEntry().getEntryDate();
        }
        HashMap<String, Object> invoiceId = new HashMap<>();
        invoiceId.put("invoiceid", goodsReceipt.getID());
        invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
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
        if (goodsReceipt.getCurrency() != null) {
            currid = goodsReceipt.getCurrency().getCurrencyID();
        }
        //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
        KwlReturnObject bAmt = null;
        if (isopeningBalanceInvoice && goodsReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
        } else {
            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
        }

        double oldrate = (Double) bAmt.getEntityList().get(0);
        //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
        if (revalueationHistory == null && isopeningBalanceInvoice && goodsReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
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
        finalAmountReval = finalAmountReval + amountReval;
    } catch (SessionExpiredException | ServiceException e) {
        throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
    }
    return finalAmountReval;
}

/*
    * Revalaution Entery for Credit Note
    */
public double ReevalJournalEntryForCreditNote(HttpServletRequest request, CreditNote creditNote, double linkInvoiceAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {

    double finalAmountReval = 0;
    try {
        String basecurrency = sessionHandlerImpl.getCurrencyID(request);
        double ratio = 0;
        double amountReval = 0;
        String revalId = null;
        Date tranDate = null;
        double exchangeRate = 0.0;
        double exchangeRateReval = 0.0;
        double amountdue = linkInvoiceAmount;
        Map<String, Object> GlobalParams = new HashMap<>();
        GlobalParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
        GlobalParams.put("gcurrencyid", basecurrency);
        GlobalParams.put("dateformat", authHandler.getDateOnlyFormat(request));
        Date creationDate = creditNote.getCreationDate();
        boolean isopeningBalanceInvoice = creditNote.isIsOpeningBalenceCN();
        tranDate = creditNote.getCreationDate();
        if (!creditNote.isNormalCN()) {
            exchangeRate = creditNote.getExchangeRateForOpeningTransaction();
            exchangeRateReval = exchangeRate;
        } else {
            exchangeRate = creditNote.getJournalEntry().getExternalCurrencyRate();
            exchangeRateReval = exchangeRate;
//            tranDate = creditNote.getJournalEntry().getEntryDate();
        }
        HashMap<String, Object> invoiceId = new HashMap<>();
        invoiceId.put("invoiceid", creditNote.getID());
        invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
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
        //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
        KwlReturnObject bAmt = null;
        if (isopeningBalanceInvoice && creditNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
        } else {
            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
        }

        double oldrate = (Double) bAmt.getEntityList().get(0);
        //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
        if (revalueationHistory == null && isopeningBalanceInvoice && creditNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
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
        finalAmountReval = finalAmountReval + amountReval;
    } catch (SessionExpiredException | ServiceException e) {
        throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
    }
    return finalAmountReval;
}

public double ReevalJournalEntryForDebitNote(HttpServletRequest request, DebitNote debitNote, double linkInvoiceAmount, double exchangeratefortransaction) throws SessionExpiredException, ServiceException, AccountingException {
    double finalAmountReval = 0;
    try {
        String basecurrency = sessionHandlerImpl.getCurrencyID(request);
        double ratio = 0;
        double amountReval = 0;
        Date tranDate = null;
        double exchangeRate = 0.0;
        double exchangeRateReval = 0.0;
        double amountdue = linkInvoiceAmount;
        Map<String, Object> GlobalParams = new HashMap<>();
        GlobalParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
        GlobalParams.put("gcurrencyid", basecurrency);
        GlobalParams.put("dateformat", authHandler.getDateOnlyFormat(request));
        Date creationDate = debitNote.getCreationDate();
        boolean isopeningBalanceInvoice = debitNote.isIsOpeningBalenceDN();
        tranDate = debitNote.getCreationDate();
        if (!debitNote.isNormalDN()) {
            exchangeRate = debitNote.getExchangeRateForOpeningTransaction();
            exchangeRateReval = exchangeRate;
        } else {
            exchangeRate = debitNote.getJournalEntry().getExternalCurrencyRate();
            exchangeRateReval = exchangeRate;
//            tranDate = debitNote.getJournalEntry().getEntryDate();
        }
        HashMap<String, Object> invoiceId = new HashMap<>();
        invoiceId.put("invoiceid", debitNote.getID());
        invoiceId.put("companyid", sessionHandlerImpl.getCompanyid(request));
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
        //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
        KwlReturnObject bAmt = null;
        if (isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
        } else {
            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
        }

        double oldrate = (Double) bAmt.getEntityList().get(0);
        //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
        if (revalueationHistory == null && isopeningBalanceInvoice && debitNote.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
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
        finalAmountReval = finalAmountReval + amountReval;
    } catch (SessionExpiredException | ServiceException e) {
        throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
    }
    return finalAmountReval;
}

/*
    * Function to Post JE For Re-valuation
    */
public String PostJEFORReevaluation(HttpServletRequest request, double finalAmountReval, String companyid, CompanyAccountPreferences preferences, String basecurrency, String oldRevaluationJE, Map<String, Object> dataMap) {
    String jeid = "";
    try {
        String jeentryNumber = "";
        String jeSeqFormatId = "";
        String jeIntegerPart = "";
        String datePrefix = "";
        String dateAfterPrefix = "";
        String dateSuffix = "";
        DateFormat df = authHandler.getDateOnlyFormat(request);
        /**
         * added Link Date to Realised JE. while link Otherwise CN/DN to
         * Reevaluated Invoice.
         */
        String creationDate = !StringUtil.isNullObject(request.getParameter("linkingdate")) ? request.getParameter("linkingdate") : request.getParameter("creationdate");
        Date jeCreationDate = StringUtil.isNullOrEmpty(creationDate) ? new Date() : df.parse(creationDate);
        boolean jeautogenflag = false;
        int counter = (Integer) dataMap.get("counter");
        synchronized (this) {
            Map<String, Object> JEFormatParams = new HashMap<>();
            JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
            JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
            JEFormatParams.put("companyid", companyid);
            JEFormatParams.put("isdefaultFormat", true);

            KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
            SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, jeCreationDate);
            jeautogenflag = true;
            if (StringUtil.isNullOrEmpty(oldRevaluationJE)) {
                String nextAutoNoTemp = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                dateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
                dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                int sequence = Integer.parseInt((String) seqNumberMap.get(Constants.SEQNUMBER));
                sequence = sequence + counter;
                String number = "" + sequence;
                String action = "" + (sequence - counter);
                nextAutoNoTemp.replaceAll(action, number);
                jeentryNumber = nextAutoNoTemp.replaceAll(action, number);  //next auto generated number
                jeIntegerPart = String.valueOf(sequence);
                jeSeqFormatId = format.getID();
                counter++;
                dataMap.put("counter", counter);
            } else if (!StringUtil.isNullOrEmpty(oldRevaluationJE)) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), oldRevaluationJE);
                JournalEntry entry = (JournalEntry) result.getEntityList().get(0);
                jeid = entry.getID();
                jeentryNumber = entry.getEntryNumber();
                jeSeqFormatId = entry.getSeqformat().getID();
                jeIntegerPart = String.valueOf(entry.getSeqnumber());
                datePrefix = entry.getDatePreffixValue();
                dateAfterPrefix = entry.getDateAfterPreffixValue();
                dateSuffix = entry.getDateSuffixValue();
                result = accJournalEntryobj.deleteJEDtails(oldRevaluationJE, companyid);
                result = accJournalEntryobj.deleteJE(oldRevaluationJE, companyid);
            }
        }
        boolean creditDebitFlag = true;
        if (finalAmountReval < 0) {
            finalAmountReval = -(finalAmountReval);
            creditDebitFlag = false;
        }
        
        Map<String, Object> jeDataMapReval = AccountingManager.getGlobalParams(request);
        jeDataMapReval.put("entrynumber", jeentryNumber);
        jeDataMapReval.put("autogenerated", jeautogenflag);
        jeDataMapReval.put(Constants.SEQFORMAT, jeSeqFormatId);
        jeDataMapReval.put(Constants.SEQNUMBER, jeIntegerPart);
        jeDataMapReval.put(Constants.DATEPREFIX, datePrefix);
        jeDataMapReval.put(Constants.DATEAFTERPREFIX, dateAfterPrefix);
        jeDataMapReval.put(Constants.DATESUFFIX, dateSuffix);
        jeDataMapReval.put("entrydate", jeCreationDate);
        jeDataMapReval.put("companyid", companyid);
        //jeDataMapReval.put("memo", "Realised Gain/Loss");
        jeDataMapReval.put("currencyid", basecurrency);
        jeDataMapReval.put("isReval", 2);
        jeDataMapReval.put("transactionModuleid", dataMap.containsKey("transactionModuleid") ? dataMap.get("transactionModuleid") : 0);
        jeDataMapReval.put("transactionId", dataMap.get("transactionId"));     
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
                jedjsonreval = new JSONObject();
                jedjsonreval.put("accjedetailcustomdata", jed.getID());
                jedjsonreval.put("jedid", jed.getID());
                jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjsonreval);
            }
        }
        
        String unrealised_accid = "";
        if (preferences.getUnrealisedgainloss() != null) {
            unrealised_accid = preferences.getUnrealisedgainloss().getID();
        } else {
            throw new AccountingException(messageSource.getMessage("acc.field.NoUnrealisedGain/Lossaccountfound", null, RequestContextUtils.getLocale(request)));
        }
        jedjsonreval = new JSONObject();
        jedjsonreval.put("companyid", companyid);
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
                jedjsonreval = new JSONObject();
                jedjsonreval.put("accjedetailcustomdata", jed.getID());
                jedjsonreval.put("jedid", jed.getID());
                jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjsonreval);
            }
        }
        
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
        Logger.getLogger(accDebitNoteController.class.getName()).log(Level.SEVERE, null, ex);
    }
    return jeid;
}

/*
    * Function to update goodsreceipt amount due and return the result
    */
public KwlReturnObject updateInvoiceAmountDueAndReturnResult(GoodsReceipt goodsReceipt, Company company, double amountReceivedForGoodsReceipt, double baseAmountReceivedForGoodsReceipt) throws JSONException, ServiceException {
    KwlReturnObject result = null;
    if (goodsReceipt != null) {
        double invoiceAmountDue = goodsReceipt.getOpeningBalanceAmountDue();
        invoiceAmountDue -= amountReceivedForGoodsReceipt;
        Map<String, Object> greceipthm = new HashMap<String, Object>();
        greceipthm.put("grid", goodsReceipt.getID());;
        greceipthm.put("companyid", company.getCompanyID());
        greceipthm.put("openingBalanceAmountDue", invoiceAmountDue);
        greceipthm.put(Constants.openingBalanceBaseAmountDue, goodsReceipt.getOpeningBalanceBaseAmountDue() - baseAmountReceivedForGoodsReceipt);
        greceipthm.put(Constants.invoiceamountdue, goodsReceipt.getInvoiceamountdue() - amountReceivedForGoodsReceipt);
        greceipthm.put(Constants.invoiceamountdueinbase, goodsReceipt.getInvoiceAmountDueInBase() - baseAmountReceivedForGoodsReceipt);
        result = accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
    }
    return result;
}

/*
    * Function to update Credit note amount due and return KwlReturnObject
    */
public KwlReturnObject updateCreditNoteAmountDueAndReturnResult(CreditNote creditNote, Company company, double amountReceivedForInvoice, double amountReceivedInBaseCurrencyForInvoice) throws JSONException, ServiceException {
    KwlReturnObject result = null;
    if (creditNote != null) {
        double debitNoteAmountDue = creditNote.getOpeningBalanceAmountDue();
        debitNoteAmountDue -= amountReceivedForInvoice;
        HashMap<String, Object> dnhm = new HashMap<>();
        dnhm.put("cnid", creditNote.getID());
        dnhm.put("companyid", company.getCompanyID());
        dnhm.put("openingBalanceAmountDue", debitNoteAmountDue);
        dnhm.put(Constants.openingBalanceBaseAmountDue, creditNote.getOriginalOpeningBalanceBaseAmount() - amountReceivedInBaseCurrencyForInvoice);
        dnhm.put("cnamountdue", creditNote.getCnamountdue() - amountReceivedForInvoice);
//        dnhm.put("cnamountinbase", creditNote.getCnamountinbase() - amountReceivedInBaseCurrencyForInvoice);
        result = accCreditNoteDAOobj.updateCreditNote(dnhm);
    }
    return result;
}

public void updateOpeningInvoiceAmountDue(String debitNoteId, String companyId) throws JSONException, ServiceException {

    KwlReturnObject dnObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), debitNoteId);
    if (!dnObj.getEntityList().isEmpty()) {
        DebitNote debitNote = (DebitNote) dnObj.getEntityList().get(0);
        Set<DebitNoteDetail> debitNoteDetails = debitNote.getRows();
        if (debitNoteDetails != null && !debitNote.isDeleted()) {// if debit note already temporary deleted then amountdue already updated. No need to update amountdue again for permament delete.
            Iterator itr = debitNoteDetails.iterator();
            while (itr.hasNext()) {
                DebitNoteDetail debitNoteDetail = (DebitNoteDetail) itr.next();
                if (debitNoteDetail.getGoodsReceipt() != null && !debitNoteDetail.getGoodsReceipt().isNormalInvoice() && debitNoteDetail.getGoodsReceipt().isIsOpeningBalenceInvoice()) {
//                        double amountPaid = debitNoteDetail.getDiscount().getDiscountValue();
                    double amountPaid = debitNoteDetail.getDiscount().getAmountinInvCurrency();
                    GoodsReceipt goodsReceipt = debitNoteDetail.getGoodsReceipt();
                    HashMap<String, Object> requestParams = new HashMap();
                    requestParams.put(Constants.companyid, companyId);
                    requestParams.put("gcurrencyid", goodsReceipt.getCompany().getCurrency().getCurrencyID());
                    double externalCurrencyRate = 0d;
                    externalCurrencyRate = goodsReceipt.getExchangeRateForOpeningTransaction();
                    String fromcurrencyid = goodsReceipt.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    if (goodsReceipt.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountPaid, fromcurrencyid, goodsReceipt.getCreationDate(), externalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, goodsReceipt.getCreationDate(), externalCurrencyRate);
                    }
                    double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);

                    double invoiceAmountDue = goodsReceipt.getOpeningBalanceAmountDue();
                    invoiceAmountDue += amountPaid;
                    Map<String, Object> greceipthm = new HashMap<String, Object>();
                    greceipthm.put("grid", goodsReceipt.getID());;
                    greceipthm.put("companyid", companyId);
                    greceipthm.put("openingBalanceAmountDue", invoiceAmountDue);
                    greceipthm.put(Constants.openingBalanceBaseAmountDue, goodsReceipt.getOpeningBalanceBaseAmountDue() + totalBaseAmountDue);
                    if (invoiceAmountDue != 0) {
                        greceipthm.put("amountduedate", "");
                    }
                    accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                } else if (debitNoteDetail.getGoodsReceipt() != null && debitNoteDetail.getGoodsReceipt().isNormalInvoice() && !debitNoteDetail.getGoodsReceipt().isIsOpeningBalenceInvoice()) {
//                        double amountPaid = debitNoteDetail.getDiscount().getDiscountValue();
                    double amountPaid = debitNoteDetail.getDiscount().getAmountinInvCurrency();
                    GoodsReceipt goodsReceipt = debitNoteDetail.getGoodsReceipt();
                    double invoiceAmountDue = goodsReceipt.getInvoiceamountdue();
                    invoiceAmountDue += amountPaid;
                    Map<String, Object> greceipthm = new HashMap<String, Object>();
                    greceipthm.put("grid", goodsReceipt.getID());;
                    greceipthm.put("companyid", companyId);
                    greceipthm.put(Constants.invoiceamountdue, invoiceAmountDue);
                    JournalEntry je = goodsReceipt.getJournalEntry();
                    HashMap<String, Object> requestParams = new HashMap();
                    requestParams.put(Constants.companyid, companyId);
                    requestParams.put("gcurrencyid", goodsReceipt.getCompany().getCurrency().getCurrencyID());
                    String fromcurrencyid = goodsReceipt.getCurrency().getCurrencyID();
                    if (je != null) {
//                        KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoiceAmountDue, fromcurrencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                        KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoiceAmountDue, fromcurrencyid, goodsReceipt.getCreationDate(), je.getExternalCurrencyRate());
                        double invoiceamountdueinbase = (Double) baseAmount.getEntityList().get(0);
                        greceipthm.put(Constants.invoiceamountdueinbase, invoiceamountdueinbase);
                    }
                    if (invoiceAmountDue != 0) {
                        greceipthm.put("amountduedate", "");
                    }
                    accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                }
            }
        }
    }
}

public ModelAndView getDebitNoteLinkedDocumnets(HttpServletRequest request, HttpServletResponse response) {
    JSONObject jobj = new JSONObject();
    String msg = "";
    boolean issuccess = false;
    try {
        jobj = getDebitNoteLinkedDocumnets(request);
        issuccess = true;
    } catch (ServiceException ex) {
        msg = ex.getMessage();
        Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
    } catch (Exception ex) {
        msg = "accGoodsReceiptController.getDebitNoteGR:" + ex.getMessage();
        Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
    } finally {
        try {
            jobj.put("success", issuccess);
            jobj.put("msg", msg);
        } catch (JSONException ex) {
            Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    return new ModelAndView("jsonView", "model", jobj.toString());
}

public JSONObject getDebitNoteLinkedDocumnets(HttpServletRequest request) throws ServiceException, SessionExpiredException {
    JSONObject jobj = new JSONObject();
    JSONArray jArray = new JSONArray();
    HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
    DateFormat df = authHandler.getDateOnlyFormat(request);
    try {
        String dNoteId = request.getParameter("noteId");
        KwlReturnObject result = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dNoteId);
        DebitNote dn = (DebitNote) result.getEntityList().get(0);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        if (dn != null) {
            Set<DebitNoteDetail> rows = dn.getRows();
            Set<DebitNoteDetail> Debitnotedetail = new TreeSet<DebitNoteDetail>(new SortDebitNoteDetail());
            Debitnotedetail.addAll(rows);
            for (DebitNoteDetail detail : Debitnotedetail) {
                if (detail.getGoodsReceipt() != null) {
                    JSONObject obj = new JSONObject();
                    obj.put("linkdetailid", detail.getID());

                    /*
                        * Checked Null for Invoice linked with Debit Note at
                        * the time of edit Debit Note which was created without
                        * selection any Invoice
                        */

                    obj.put("billid", detail.getGoodsReceipt() != null ? detail.getGoodsReceipt().getID() : "");
                    obj.put("billno", detail.getGoodsReceipt() != null ? detail.getGoodsReceipt().getGoodsReceiptNumber() : "");
                    obj.put("documentid", detail.getGoodsReceipt() != null ? detail.getGoodsReceipt().getID() : "");
                    obj.put("documentno", detail.getGoodsReceipt() != null ? detail.getGoodsReceipt().getGoodsReceiptNumber() : "");
                    obj.put("type", "Invoice");
                    if (detail.getGoodsReceipt() != null) {
                        obj.put("taxamount", detail.getGoodsReceipt().getTaxEntry() == null ? 0 : detail.getGoodsReceipt().getTaxEntry().getAmount());
                    }

                    Discount disc = detail.getDiscount();
                    double exchangeratefortransaction = detail.getExchangeRateForTransaction();
                    double invoiceReturnedAmt = 0d;
                    if (disc != null) {
                        obj.put("linkamount", authHandler.round(disc.getDiscountValue(), companyid));
                        invoiceReturnedAmt = disc.getAmountinInvCurrency();
                    } else {
                        obj.put("linkamount", 0);
                    }
                    obj.put("linkingdate", detail.getGrLinkDate() != null ? df.format(detail.getGrLinkDate()) : "");
                    double amountdue = 0;
                    double amountDueOriginal = 0;
                    if (detail.getGoodsReceipt() != null && detail.getGoodsReceipt().isIsOpeningBalenceInvoice() && !detail.getGoodsReceipt().isNormalInvoice()) {
                        amountdue = detail.getGoodsReceipt().getOpeningBalanceAmountDue();
                        amountDueOriginal = detail.getGoodsReceipt().getOriginalOpeningBalanceAmount();
                        obj.put("amount", detail.getGoodsReceipt().getOriginalOpeningBalanceAmount());
                    } else {
                        if (Constants.InvoiceAmountDueFlag && detail.getGoodsReceipt() != null) {
                            List ll = accGoodsReceiptCommon.getInvoiceDiscountAmountInfo(requestParams, detail.getGoodsReceipt());
                            amountdue = (Double) ll.get(5);
                        } else {
                            if (detail.getGoodsReceipt() != null) {
                                List ll = accGoodsReceiptCommon.getGRAmountDue(requestParams, detail.getGoodsReceipt());
                                amountdue = (Double) ll.get(5);
                            }

                        }
                        amountDueOriginal = detail.getGoodsReceipt() != null ? detail.getGoodsReceipt().getVendorEntry().getAmount() : 0;
                        obj.put("amount", detail.getGoodsReceipt() != null ? detail.getGoodsReceipt().getVendorEntry().getAmount() : 0);
                    }

                    amountdue = amountdue + invoiceReturnedAmt;// added invoiceReturnedAmt to show original value which was at time of creation.
                    amountdue = amountdue * exchangeratefortransaction;
                    obj.put("amountdue", authHandler.round(amountdue, companyid));
                    obj.put("amountDueOriginal", authHandler.round(amountDueOriginal, companyid));
                    obj.put("exchangeratefortransaction", exchangeratefortransaction);
                    obj.put("currencysymbol", detail.getGoodsReceipt() != null ? detail.getGoodsReceipt().getCurrency().getSymbol() : "");
                    obj.put("currencysymboltransaction", detail.getGoodsReceipt() != null ? detail.getGoodsReceipt().getCurrency().getSymbol() : "");
                    obj.put("currencysymbolpayment", dn.getCurrency().getSymbol());
                    if (detail.getGoodsReceipt() != null) {
//                        obj.put("invoicedate", detail.getGoodsReceipt().isIsOpeningBalenceInvoice() ? df.format(detail.getGoodsReceipt().getCreationDate()) : df.format(detail.getGoodsReceipt().getJournalEntry().getEntryDate()));
                        obj.put("invoicedate", df.format(detail.getGoodsReceipt().getCreationDate()));
                        //In Debite Note Js Side Rec Name is Date . 
//                        obj.put("date", detail.getGoodsReceipt().isIsOpeningBalenceInvoice() ? df.format(detail.getGoodsReceipt().getCreationDate()) : df.format(detail.getGoodsReceipt().getJournalEntry().getEntryDate()));
                        obj.put("date", df.format(detail.getGoodsReceipt().getCreationDate()));
                    }
                    obj.put("typeFigure", detail.getDiscount().getTypeFigure());    // Figure calculated resctive to invoice amount due
                    obj.put("typeOfFigure", detail.getDiscount().getTypeOfFigure()); // Criteria of calculation for amount - Flat or Percentage
                    jArray.put(obj);
                } else if (!StringUtil.isNullOrEmpty(detail.getCreditNoteId())) {

                    result = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), detail.getCreditNoteId());
                    CreditNote creditNote = (CreditNote) result.getEntityList().get(0);
                    JSONObject obj = new JSONObject();
                    obj.put("linkdetailid", detail.getID());
                    /*
                        * Checked Null for Invoice linked with Debit Note at
                        * the time of edit Debit Note which was created without
                        * selection any Invoice
                        */

                    obj.put("billid", creditNote != null ? creditNote.getID() : "");
                    obj.put("billno", creditNote != null ? creditNote.getCreditNoteNumber() : "");
                    obj.put("documentid", creditNote != null ? creditNote.getID() : "");
                    obj.put("documentno", creditNote != null ? creditNote.getCreditNoteNumber() : "");

                    obj.put("type", "Credit Note");
                    Set<CreditNoteTaxEntry> cntaxEnteries = creditNote.getCnTaxEntryDetails();
                    double taxAmount = 0.0;
                    for (CreditNoteTaxEntry debitNoteTaxEntry : cntaxEnteries) {
                        taxAmount += debitNoteTaxEntry.getAmount();
                    }
                    obj.put("linkingdate", detail.getGrLinkDate() != null ? df.format(detail.getGrLinkDate()) : "");
                    obj.put("taxamount", taxAmount);
                    Discount disc = detail.getDiscount();
                    double exchangeratefortransaction = detail.getExchangeRateForTransaction();
                    double invoiceReturnedAmt = 0d;
                    if (disc != null) {
                        obj.put("linkamount", authHandler.round(disc.getDiscountValue(), companyid));
                        invoiceReturnedAmt = disc.getAmountinInvCurrency();
                    } else {
                        obj.put("linkamount", 0);
                    }
                    
                    double amountdue = 0;
                    double amountDueOriginal = 0;
                    if (creditNote != null && creditNote.isIsOpeningBalenceCN() && !creditNote.isNormalCN()) {
                        amountdue = creditNote.getOpeningBalanceAmountDue();
                        amountDueOriginal = creditNote.getCnamountdue();
                        obj.put("amount", creditNote.getCnamountdue());
                    } else {
//                        if(Constants.InvoiceAmountDueFlag && detail.getGoodsReceipt()!=null) {
//                            List ll = accGoodsReceiptCommon.getInvoiceDiscountAmountInfo(requestParams, detail.getGoodsReceipt());
//                            amountdue = (Double) ll.get(5);
//                        } else {
//                            if (detail.getGoodsReceipt() != null) {
//                                List ll = accGoodsReceiptCommon.getGRAmountDue(requestParams, detail.getGoodsReceipt());
//                                amountdue = (Double) ll.get(5);
//                            }
//
//                        }
                        amountdue = creditNote.getCnamountdue();
                        amountDueOriginal = creditNote != null ? creditNote.getCustomerEntry().getAmount() : 0;
                        obj.put("amount", creditNote != null ? creditNote.getCustomerEntry().getAmount() : 0);
                    }

                    amountdue = amountdue + invoiceReturnedAmt;// added invoiceReturnedAmt to show original value which was at time of creation.
                    amountdue = amountdue * exchangeratefortransaction;
                    obj.put("amountdue", authHandler.round(amountdue, companyid));
                    obj.put("amountDueOriginal", authHandler.round(amountDueOriginal, companyid));
                    obj.put("exchangeratefortransaction", exchangeratefortransaction);
                    obj.put("currencysymbol", creditNote != null ? creditNote.getCurrency().getSymbol() : "");
                    obj.put("currencysymboltransaction", creditNote != null ? creditNote.getCurrency().getSymbol() : "");
                    obj.put("currencysymbolpayment", dn.getCurrency().getSymbol());
                    if (creditNote != null) {
//                        obj.put("invoicedate", creditNote.isIsOpeningBalenceCN() ? df.format(creditNote.getCreationDate()) : df.format(creditNote.getJournalEntry().getEntryDate()));
                        obj.put("invoicedate", df.format(creditNote.getCreationDate()));
                        //In Debite Note Js Side Rec Name is Date . 
//                        obj.put("date", creditNote.isIsOpeningBalenceCN() ? df.format(creditNote.getCreationDate()) : df.format(creditNote.getJournalEntry().getEntryDate()));
                        obj.put("date", df.format(creditNote.getCreationDate()));
                    }
                    obj.put("typeFigure", detail.getDiscount().getTypeFigure());    // Figure calculated resctive to invoice amount due
                    obj.put("typeOfFigure", detail.getDiscount().getTypeOfFigure()); // Criteria of calculation for amount - Flat or Percentage
                    jArray.put(obj);

                }
            }
        }
        jobj.put("data", jArray);
    } catch (JSONException ex) {
        Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
    }
    return jobj;
}

public ModelAndView unlinkDebitNote(HttpServletRequest request, HttpServletResponse response) {
    JSONObject jobj = new JSONObject();
    boolean issuccess = false;
    String msg = "";
    DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    def.setName("SP_Tx");
    def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    TransactionStatus status = txnManager.getTransaction(def);
    try {
        List li = unlinkDebitNoteFromTransactions(request);
        issuccess = true;
        msg = messageSource.getMessage("acc.dn.unlinkedFromPurchaseInvoice", null, RequestContextUtils.getLocale(request));
        txnManager.commit(status);
    } catch (SessionExpiredException ex) {
        txnManager.rollback(status);
        msg = ex.getMessage();
        Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
    } catch (Exception ex) {
        txnManager.rollback(status);
        msg = "" + ex.getMessage();
        Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
        try {
            jobj.put("success", issuccess);
            jobj.put("msg", msg);
        } catch (JSONException ex) {
            Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    return new ModelAndView("jsonView", "model", jobj.toString());
}

public List unlinkDebitNoteFromTransactions(HttpServletRequest request) throws ServiceException, SessionExpiredException {
    List result = new ArrayList();
    try {
        //Unlink Invoices
        result = unlinkDebitNoteFromInvoice(request);
        //Unlink Credit Notes
        result = unlinkDebitNoteFromCreditNote(request);
    } catch (SessionExpiredException ex) {
        throw ServiceException.FAILURE(ex.getMessage(), ex);
    } catch (Exception ex) {
        Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
    }
    return result;
}
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

public List unlinkDebitNoteFromInvoice(HttpServletRequest request) throws ServiceException, SessionExpiredException {
    List result = new ArrayList();
    try {
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String gcurrenyid = sessionHandlerImpl.getCurrencyID(request);
        String dnid = request.getParameter("cnid");

        KwlReturnObject dnKWLObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnid);
        DebitNote dn = (DebitNote) dnKWLObj.getEntityList().get(0);

        String dnnumber = dn.getDebitNoteNumber();
        String linkedInvoiceids = "";
        String linkedInvoicenos = "";

        //Get details of those invoices which are still linked to DN
        List<String> linkedDetailInvoice = new ArrayList();
        JSONArray linkJSONArray = request.getParameter("linkdetails") != null ? new JSONArray(request.getParameter("linkdetails")) : new JSONArray();
        for (int k = 0; k < linkJSONArray.length(); k++) {
            JSONObject jSONObject = linkJSONArray.getJSONObject(k);
            String linkId = jSONObject.optString("linkdetailid", "");
            linkedDetailInvoice.add(linkId);
        }

        String linkedDetailIDs = "";
        for (String invID : linkedDetailInvoice) {
            linkedDetailIDs = linkedDetailIDs.concat("'").concat(invID).concat("',");
        }
        if (!StringUtil.isNullOrEmpty(linkedDetailIDs.toString())) {
            linkedDetailIDs = linkedDetailIDs.substring(0, linkedDetailIDs.length() - 1);
        }

        boolean allInvoicesUnlinked = false;
        if (linkJSONArray.length() == 0) {
            allInvoicesUnlinked = true;
        }
        double dnExternalCurrencyRate = 1d;
        boolean isopeningBalanceCN = dn.isIsOpeningBalenceDN();
        Date dnCreationDate = null;
        dnCreationDate = dn.getCreationDate();
        if (isopeningBalanceCN) {
            dnExternalCurrencyRate = dn.getExchangeRateForOpeningTransaction();
        } else {
//            dnCreationDate = dn.getJournalEntry().getEntryDate();
            dnExternalCurrencyRate = dn.getJournalEntry().getExternalCurrencyRate();
        }

        // get list of invoices which are un-linked
        KwlReturnObject dndetailResult = accDebitNoteobj.getDeletedLinkedInvoices(dn, linkedDetailIDs, companyid);
        List<DebitNoteDetail> details = dndetailResult.getEntityList();
        Double totalAmountUsedByInvoices = 0.0;
        Set<String> grIDSet = new HashSet<>();
        //update invocie amount due after unlinking
        for (DebitNoteDetail debitNoteDetail : details) {
            if (debitNoteDetail.getGoodsReceipt() != null && !debitNoteDetail.getGoodsReceipt().isNormalInvoice() && debitNoteDetail.getGoodsReceipt().isIsOpeningBalenceInvoice()) {
                double amountPaid = debitNoteDetail.getDiscount().getAmountinInvCurrency();
                totalAmountUsedByInvoices += debitNoteDetail.getDiscount().getDiscount();
                GoodsReceipt invObj = debitNoteDetail.getGoodsReceipt();
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, companyid);
                requestParams.put("gcurrencyid", invObj.getCompany().getCurrency().getCurrencyID());
                double externalCurrencyRate = 0d;
                externalCurrencyRate = invObj.getExchangeRateForOpeningTransaction();
                String fromcurrencyid = invObj.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (invObj.isConversionRateFromCurrencyToBase()) {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountPaid, fromcurrencyid, invObj.getCreationDate(), externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, invObj.getCreationDate(), externalCurrencyRate);
                }
                double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                double invoiceAmountDue = invObj.getOpeningBalanceAmountDue();
                invoiceAmountDue += amountPaid;

                Map<String, Object> greceipthm = new HashMap<String, Object>();
                greceipthm.put("grid", invObj.getID());;
                greceipthm.put("companyid", companyid);
                greceipthm.put("openingBalanceAmountDue", invoiceAmountDue);
                greceipthm.put(Constants.openingBalanceBaseAmountDue, invObj.getOpeningBalanceBaseAmountDue() + totalBaseAmountDue);
                if (invoiceAmountDue != 0) {
                    greceipthm.put("amountduedate", "");
                }
                accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                linkedInvoicenos += invObj.getGoodsReceiptNumber() + ",";
                grIDSet.add(invObj.getID());
            } else if (debitNoteDetail.getGoodsReceipt() != null && debitNoteDetail.getGoodsReceipt().isNormalInvoice() && !debitNoteDetail.getGoodsReceipt().isIsOpeningBalenceInvoice()) {
                GoodsReceipt invoice = debitNoteDetail.getGoodsReceipt();
                //Amount of invoice  in invoice currency
                double amountPaid = debitNoteDetail.getDiscount().getAmountinInvCurrency();
                
                //Amount of invoice  in base currency
                double amountPaidInBase = 0;
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, companyid);
                requestParams.put("gcurrencyid", gcurrenyid);
//                KwlReturnObject grAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, invoice.getCurrency().getCurrencyID(), invoice.getJournalEntry().getEntryDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                KwlReturnObject grAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, invoice.getCurrency().getCurrencyID(), invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                if(grAmtInBaseResult !=null){
                    amountPaidInBase += authHandler.round((Double) grAmtInBaseResult.getEntityList().get(0), companyid);
                }
                double invoiceAmountDue = invoice.getInvoiceamountdue() + amountPaid;
                double invoiceAmountDueInbase = invoice.getInvoiceAmountDueInBase() + amountPaidInBase;
                Map<String, Object> greceipthm = new HashMap<String, Object>();
                greceipthm.put("grid", invoice.getID());;
                greceipthm.put("companyid", companyid);
                greceipthm.put(Constants.invoiceamountdue, invoiceAmountDue);
                greceipthm.put(Constants.invoiceamountdueinbase, invoiceAmountDueInbase);
                if (invoiceAmountDue != 0) {
                    greceipthm.put("amountduedate", "");
                }
                accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                
                linkedInvoicenos += invoice.getGoodsReceiptNumber() + ",";
                totalAmountUsedByInvoices += debitNoteDetail.getDiscount().getDiscount();
                grIDSet.add(invoice.getID());
            }
            // check if Forex/Gain Loss JE generated. If yes then need to delete JE too
            if (debitNoteDetail.getLinkedGainLossJE() != null && !debitNoteDetail.getLinkedGainLossJE().isEmpty()) {
                deleteJEArray(debitNoteDetail.getLinkedGainLossJE(), companyid);
            }

            //Deletng Relasiesd JE for Unlinked Invoice
            if (debitNoteDetail != null && !StringUtil.isNullOrEmpty(debitNoteDetail.getRevalJeId())) {
                accJournalEntryobj.deleteJEDtails(debitNoteDetail.getRevalJeId(), companyid);
                accJournalEntryobj.deleteJE(debitNoteDetail.getRevalJeId(), companyid);
            }
            if (debitNoteDetail != null && !StringUtil.isNullOrEmpty(debitNoteDetail.getRevalJeIdInvoice())) {
                accJournalEntryobj.deleteJEDtails(debitNoteDetail.getRevalJeIdInvoice(), companyid);
                accJournalEntryobj.deleteJE(debitNoteDetail.getRevalJeIdInvoice(), companyid);
            }
        }

        if (!StringUtil.isNullOrEmpty(linkedInvoicenos)) {
            linkedInvoicenos = linkedInvoicenos.substring(0, linkedInvoicenos.length() - 1);
        }

        //Delete Rouding JEs if created against PI
        String roundingJENo = "";
        String roundingIDs = "";
        String piIDs = "";
        for (String piID : grIDSet) {
            piIDs = piID+",";
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

        if (!StringUtil.isNullOrEmpty(roundingJENo)) {
            roundingJENo = roundingJENo.substring(0, roundingJENo.length() - 1);
        }
        if (!StringUtil.isNullOrEmpty(roundingIDs)) {
            roundingIDs = roundingIDs.substring(0, roundingIDs.length() - 1);
        }
        // Update debit note details
        HashMap<String, Object> debithm = new HashMap();
        Double dnAmountDue = dn.getOpeningBalanceAmountDue();
        dnAmountDue = dnAmountDue + totalAmountUsedByInvoices;
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
        requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
        String fromcurrencyid = dn.getCurrency().getCurrencyID();
        KwlReturnObject bAmt = null;
        if (isopeningBalanceCN && dn.isConversionRateFromCurrencyToBase()) {
            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, dnAmountDue, fromcurrencyid, dnCreationDate, dnExternalCurrencyRate);
        } else {
            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, dnAmountDue, fromcurrencyid, dnCreationDate, dnExternalCurrencyRate);
        }
        double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);

        debithm.put("dnid", dn.getID());
        debithm.put("dnamountdue", dnAmountDue);
        debithm.put("openingBalanceAmountDue", dnAmountDue);
        debithm.put(Constants.openingBalanceBaseAmountDue, totalBaseAmountDue);
        debithm.put("openflag", (dnAmountDue) <= 0 ? false : true);

        accDebitNoteobj.deleteSelectedLinkedInvoices(dn.getID(), linkedDetailIDs, companyid, "");//Empty parameter(i.e id of invoices linked in debit note) for invoices deleted at the time of unlinking Invoices from Debit Note. 

        // If all invoices linked to DN are un-linked, created one entry for dn details 
        HashSet<DebitNoteDetail> dndetails = new HashSet<DebitNoteDetail>();
        if (allInvoicesUnlinked) {
            getDNDetails(dndetails, companyid);
            for (DebitNoteDetail dndetail : dndetails) {
                dndetail.setDebitNote(dn);
            }
            debithm.put("dnid", dn.getID());
            debithm.put("dndetails", dndetails);
        }
        /*
            * Deleting linking information of Debit Note while unlinking transaction
            */
        accDebitNoteobj.deleteLinkingInformationOfDN(debithm);
        accDebitNoteobj.updateDebitNote(debithm);
        if(!StringUtil.isNullOrEmpty(linkedInvoicenos)){
            auditTrailObj.insertAuditLog(AuditAction.LINKEDRECEIPT, "User " + sessionHandlerImpl.getUserFullName(request) + " has Unlinked Debit Note " + dnnumber + " from Purchase Invoice(s) " + linkedInvoicenos, request, linkedInvoiceids);
        }
        if(!StringUtil.isNullOrEmpty(roundingJENo)){
            auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) +" "+ messageSource.getMessage("acc.roundingje.unlinkeddn", null, RequestContextUtils.getLocale(request)) +" "+dnnumber +" "+messageSource.getMessage("acc.roundingje.frompurchaseinvoice", null, RequestContextUtils.getLocale(request))+" "+ linkedInvoicenos +"." +messageSource.getMessage("acc.roundingje.roundingje", null, RequestContextUtils.getLocale(request))+" "+roundingJENo+messageSource.getMessage("acc.roundingje.roundingjedelted", null, RequestContextUtils.getLocale(request))+".", request, roundingIDs);
        }
        
    } catch (SessionExpiredException ex) {
        throw ServiceException.FAILURE(ex.getMessage(), ex);
    } catch (Exception ex) {
        Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
    }
    return result;
}

public List unlinkDebitNoteFromCreditNote(HttpServletRequest request) throws ServiceException, SessionExpiredException {
    List result = new ArrayList();
    try {
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String dnid = request.getParameter("cnid");

        KwlReturnObject dnKWLObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnid);
        DebitNote dn = (DebitNote) dnKWLObj.getEntityList().get(0);

        String dnnumber = dn.getDebitNoteNumber();
        String linkedCreditNoteids = "";
        String linkedCreditNotenos = "";

        //Get details of those invoices which are still linked to DN
        List<String> linkedDetailInvoice = new ArrayList();
        JSONArray linkJSONArray = request.getParameter("linkdetails") != null ? new JSONArray(request.getParameter("linkdetails")) : new JSONArray();
        for (int k = 0; k < linkJSONArray.length(); k++) {
            JSONObject jSONObject = linkJSONArray.getJSONObject(k);
            String linkId = jSONObject.optString("linkdetailid", "");
            linkedDetailInvoice.add(linkId);
        }

        String linkedDetailIDs = "";
        for (String invID : linkedDetailInvoice) {
            linkedDetailIDs = linkedDetailIDs.concat("'").concat(invID).concat("',");
        }
        if (!StringUtil.isNullOrEmpty(linkedDetailIDs.toString())) {
            linkedDetailIDs = linkedDetailIDs.substring(0, linkedDetailIDs.length() - 1);
        }

        boolean allInvoicesUnlinked = false;
        if (linkJSONArray.length() == 0) {
            allInvoicesUnlinked = true;
        }
        double dnExternalCurrencyRate = 1d;
        boolean isopeningBalanceCN = dn.isIsOpeningBalenceDN();
        Date dnCreationDate = null;
        dnCreationDate = dn.getCreationDate();
        if (isopeningBalanceCN) {
            dnExternalCurrencyRate = dn.getExchangeRateForOpeningTransaction();
        } else {
//            dnCreationDate = dn.getJournalEntry().getEntryDate();
            dnExternalCurrencyRate = dn.getJournalEntry().getExternalCurrencyRate();
        }

        // get list of invoices which are un-linked
        KwlReturnObject dndetailResult = accDebitNoteobj.getDeletedLinkedCreditNotes(dn, linkedDetailIDs, companyid);
        List<DebitNoteDetail> details = dndetailResult.getEntityList();
        Double totalAmountUsedByInvoices = 0.0;

        //update invocie amount due after unlinking
        for (DebitNoteDetail debitNoteDetail : details) {
            KwlReturnObject cnKWLObj = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), debitNoteDetail.getCreditNoteId());
            CreditNote creditNote = (CreditNote) cnKWLObj.getEntityList().get(0);
            if (creditNote != null && !creditNote.isNormalCN() && creditNote.isIsOpeningBalenceCN()) {
                double amountPaid = debitNoteDetail.getDiscount().getAmountinInvCurrency();
                totalAmountUsedByInvoices += debitNoteDetail.getDiscount().getDiscount();
//                    GoodsReceipt invObj = debitNoteDetail.getGoodsReceipt();
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, companyid);
                requestParams.put("gcurrencyid", creditNote.getCompany().getCurrency().getCurrencyID());
                double externalCurrencyRate = 0d;
                externalCurrencyRate = creditNote.getExchangeRateForOpeningTransaction();
                String fromcurrencyid = creditNote.getCurrency().getCurrencyID();
                KwlReturnObject bAmt = null;
                if (creditNote.isConversionRateFromCurrencyToBase()) {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountPaid, fromcurrencyid, creditNote.getCreationDate(), externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, creditNote.getCreationDate(), externalCurrencyRate);
                }
                double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                double invoiceAmountDue = creditNote.getOpeningBalanceAmountDue();
                invoiceAmountDue += amountPaid;

                HashMap<String, Object> cnhm = new HashMap<>();
                cnhm.put("cnid", creditNote.getID());;
                cnhm.put("companyid", companyid);
                cnhm.put("openingBalanceAmountDue", invoiceAmountDue);
                cnhm.put(Constants.openingBalanceBaseAmountDue, creditNote.getOpeningBalanceBaseAmountDue() + totalBaseAmountDue);
                cnhm.put("cnamountdue", creditNote.getCnamountdue() + amountPaid);
                if (invoiceAmountDue != 0) {
                    cnhm.put("amountduedate", "");
                }

                 accCreditNoteDAOobj.updateCreditNote(cnhm);
                linkedCreditNotenos += creditNote.getCreditNoteNumber() + ",";

            } else if (creditNote != null && creditNote.isNormalCN() && !creditNote.isIsOpeningBalenceCN()) {
                double amountPaid = debitNoteDetail.getDiscount().getAmountinInvCurrency();
                totalAmountUsedByInvoices += debitNoteDetail.getDiscount().getDiscount();
//                    GoodsReceipt invoice = debitNoteDetail.getGoodsReceipt();
                double invoiceAmountDue = creditNote.getCnamountdue();
                invoiceAmountDue += amountPaid;
                HashMap<String, Object> cnhm = new HashMap<>();
                cnhm.put("cnid", creditNote.getID());
                cnhm.put("companyid", companyid);
                cnhm.put("cnamountdue", invoiceAmountDue);
                if (invoiceAmountDue != 0) {
                    cnhm.put("amountduedate", "");
                }
                accCreditNoteDAOobj.updateCreditNote(cnhm);
                linkedCreditNotenos += creditNote.getCreditNoteNumber() + ",";
            }
            // check if Forex/Gain Loss JE generated. If yes then need to delete JE too
            if (debitNoteDetail.getLinkedGainLossJE() != null && !debitNoteDetail.getLinkedGainLossJE().isEmpty()) {
                deleteJEArray(debitNoteDetail.getLinkedGainLossJE(), companyid);
            }

            //Deletng Relasiesd JE for Unlinked Invoice
            if (debitNoteDetail != null && !StringUtil.isNullOrEmpty(debitNoteDetail.getRevalJeId())) {
                accJournalEntryobj.deleteJEDtails(debitNoteDetail.getRevalJeId(), companyid);
                accJournalEntryobj.deleteJE(debitNoteDetail.getRevalJeId(), companyid);
            }
            if (debitNoteDetail != null && !StringUtil.isNullOrEmpty(debitNoteDetail.getRevalJeIdInvoice())) {
                accJournalEntryobj.deleteJEDtails(debitNoteDetail.getRevalJeIdInvoice(), companyid);
                accJournalEntryobj.deleteJE(debitNoteDetail.getRevalJeIdInvoice(), companyid);
            }
        }

        if (!StringUtil.isNullOrEmpty(linkedCreditNotenos)) {
            linkedCreditNotenos = linkedCreditNotenos.substring(0, linkedCreditNotenos.length() - 1);
        }

        // Update debit note details
        HashMap<String, Object> debithm = new HashMap();
        Double dnAmountDue = dn.getOpeningBalanceAmountDue();
        dnAmountDue = dnAmountDue + totalAmountUsedByInvoices;
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
        requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
        String fromcurrencyid = dn.getCurrency().getCurrencyID();
        KwlReturnObject bAmt = null;
        if (isopeningBalanceCN && dn.isConversionRateFromCurrencyToBase()) {
            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, dnAmountDue, fromcurrencyid, dnCreationDate, dnExternalCurrencyRate);
        } else {
            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, dnAmountDue, fromcurrencyid, dnCreationDate, dnExternalCurrencyRate);
        }
        double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);

        debithm.put("dnid", dn.getID());
        debithm.put("dnamountdue", dnAmountDue);
        debithm.put("openingBalanceAmountDue", dnAmountDue);
        debithm.put(Constants.openingBalanceBaseAmountDue, totalBaseAmountDue);
        debithm.put("openflag", (dnAmountDue) <= 0 ? false : true);

        accDebitNoteobj.deleteSelectedLinkedCreditNotes(dn.getID(), linkedDetailIDs, companyid, "");//Empty parameter(i.e id of invoices linked in debit note) for invoices deleted at the time of unlinking Invoices from Debit Note. 

        // If all invoices linked to DN are un-linked, created one entry for dn details 
        HashSet<DebitNoteDetail> dndetails = new HashSet<DebitNoteDetail>();
        if (allInvoicesUnlinked) {
            getDNDetails(dndetails, companyid);
            for (DebitNoteDetail dndetail : dndetails) {
                dndetail.setDebitNote(dn);
            }
            debithm.put("dnid", dn.getID());
            debithm.put("dndetails", dndetails);
        }
        /*
            * Deleting linking information of Debit Note while unlinking transaction
            */
        accDebitNoteobj.deleteLinkingInformationOfDNAginstCN(debithm);
        accDebitNoteobj.updateDebitNote(debithm);
        if(!StringUtil.isNullOrEmpty(linkedCreditNotenos)){
            auditTrailObj.insertAuditLog(AuditAction.LINKEDRECEIPT, "User " + sessionHandlerImpl.getUserFullName(request) + " has Unlinked Debit Note " + dnnumber + " from Credit Notes(s) " + linkedCreditNotenos, request, linkedCreditNoteids);
        }
        
    } catch (SessionExpiredException ex) {
        throw ServiceException.FAILURE(ex.getMessage(), ex);
    } catch (Exception ex) {
        Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
    }
    return result;
}


/**
 * 
 * @param request
 * @param response
 * @return 
 */


 public ModelAndView deleteDebitNotesPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("DN_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String linkedNotes = deleteDebitNotesPermanent(request);
            if(!StringUtil.isNullOrEmpty(linkedNotes)){
                linkedNotes = linkedNotes.substring(0,linkedNotes.length()-1);
            }
            txnManager.commit(status);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedNotes)) {
                msg = messageSource.getMessage("acc.debitN.del", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.ob.DNExcept", null, RequestContextUtils.getLocale(request)) + " " +  linkedNotes + " " + messageSource.getMessage("acc.field.deletedsuccessfully", null, RequestContextUtils.getLocale(request)) + " " + messageSource.getMessage("acc.field.usedintransactionorlockingperiod", null, RequestContextUtils.getLocale(request));   //"Sales Order has been deleted successfully;
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + (ex.getMessage() != null ? ex.getMessage() : ex.getCause().getMessage());
            Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accDebitNoteControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteDebitNotesPermanent(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException, ParseException {
        String linkedNotes="";
        boolean isMassDelete = true; //flag for bulk delete
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            /**
             * Get country id from company
             */
            KwlReturnObject comp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) comp.getEntityList().get(0);
            int countryid = Integer.parseInt(company.getCountry().getID());
            String dnid = "", dnno = "",entryno="";
            if(jArr.length() == 1){
                isMassDelete = false;
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                dnid = StringUtil.DecodeText(jobj.getString("noteid"));
                dnno = jobj.getString("noteno");
                entryno = jobj.getString("entryno");
                Date entryDateForLock = null;
                DateFormat dateFormatForLock = authHandler.getDateOnlyFormat(request);
                if (jobj.has("date")) {
                    entryDateForLock = dateFormatForLock.parse(jobj.getString("date"));
                }
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("dnid", dnid);
                requestParams.put("companyid", companyid);
                requestParams.put("dnno", dnno);
                if (!StringUtil.isNullOrEmpty(dnid)) {

                    KwlReturnObject dnObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnid);
                    DebitNote debitNote = (DebitNote) dnObj.getEntityList().get(0);
                    
                    //Deleteing Unrealised Entery for Debit Note
                    accJournalEntryobj.permanentDeleteJournalEntryDetailReval(dnid, companyid);
                    accJournalEntryobj.permanentDeleteJournalEntryReval(dnid, companyid);
                    
                    KwlReturnObject result;
                    
                    //Delete Realised JE which was posted aginst Invoice
                    if (debitNote != null && debitNote.getRows() != null) {
                        Set<DebitNoteDetail> debitNoteDetailSet = debitNote.getRows();
                        for (DebitNoteDetail debitNoteDetail : debitNoteDetailSet) {
                            /**
                             * When we create Debit Note against Purchase Invoice
                             * and if Purchase invoice is reevaluated then the reval
                             * je id i.e realised je id is saved in
                             * revaljeidinvoice column.And if we created Debit
                             * Note otherwise and then it is reevaluated and then
                             * we externally link it with Purchase Invoice then the
                             * realised JE id is saved in revaljeid column in
                             * dndetails table.
                             */
                            String revalJEID = "";
                            /**
                             * If Invoice is reevaluated.
                             */
                            if (!StringUtil.isNullOrEmpty(debitNoteDetail.getRevalJeIdInvoice())) {
                                revalJEID = debitNoteDetail.getRevalJeIdInvoice();
                                result = accJournalEntryobj.deleteJEDtails(revalJEID, companyid);// For realised JE
                                result = accJournalEntryobj.deleteJE(revalJEID, companyid);
                            } 
                            /**
                             * If DN is reevaluated.
                             */
                            if (!StringUtil.isNullOrEmpty(debitNoteDetail.getRevalJeId())) {
                                revalJEID = debitNoteDetail.getRevalJeId();
                                result = accJournalEntryobj.deleteJEDtails(revalJEID, companyid);// For realised JE
                                result = accJournalEntryobj.deleteJE(revalJEID, companyid);
                            }
                        }
                    }
                    if (entryDateForLock != null) {
                        requestParams.put("entrydate", entryDateForLock);
                        requestParams.put("df", dateFormatForLock);
                    }
                    /**
                     * Delete Debit Note Overcharge/ Undercharge Line Level Term(Tax) details
                     * ERP-36971
                     */
                    if (countryid == Constants.USA_country_id && (debitNote.getDntype() == 5 || debitNote.getDntype() == Constants.DebitNoteForOvercharge)) {
                        accDebitNoteobj.deleteDebitNoteDetailTermMapAgainstDebitNote(dnid, companyid);
                    }
                    /* 
                     * For Malaysian company delete parmanent debit note against vendor 
                     */
                    if (debitNote.getDntype() == 5) {
                        accJournalEntryobj.permanentDeleteDebitNoteAgainstCustomerGst(dnid, companyid);
                    }
                    if (debitNote.getDntype() == Constants.DebitNoteForOvercharge) {
                        accDebitNoteobj.deleteDebitNoteForOverchargeDetails(dnid, companyid);//Debit Note For Overcharge.
                    }
                    
                    /**
                     * Method to check the payment is Reconciled or not according to its JE id
                     */
                    requestParams.put("jeid", debitNote.getJournalEntry() != null ? debitNote.getJournalEntry().getID() : "");
                    boolean isReconciledFlag = accBankReconciliationDAOObj.isRecordReconciled(requestParams);
                    if(isReconciledFlag){
                        linkedNotes += debitNote.getDebitNoteNumber()+",";
                        if(isMassDelete){ //if bulk delete then only append document no
                            continue;
                        } else{ //if single document delete then throw exception with proper message
                            if(!StringUtil.isNullOrEmpty(linkedNotes)){
                                linkedNotes = linkedNotes.substring(0, linkedNotes.length()-1);
                            }
                            throw new AccountingException(messageSource.getMessage("acc.reconcilation.Cannotdeletepayment", null, RequestContextUtils.getLocale(request))+" "+"<b>"+ linkedNotes + " " +"</b>"+messageSource.getMessage("acc.reconcilation.asitisreconciled", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                    // check for is DN Linked with Payment.
                    boolean isNoteLinkedWithPayment = accDebitNoteService.isNoteLinkedWithPayment(dnid);
                    boolean isNoteLinkedWithAdvancePayment = accDebitNoteService.isNoteLinkedWithAdvancePayment(dnid);
                    if (isNoteLinkedWithPayment || isNoteLinkedWithAdvancePayment) {
                        //throw new AccountingException(messageSource.getMessage("acc.field.SelectedDebitNoteisLinkedWithPaymentsoitcannotbedelete", null, RequestContextUtils.getLocale(request)));
                        linkedNotes+=debitNote.getDebitNoteNumber()+",";
                        continue;
                    }else if(accDebitNoteService.isDebitNoteLinkedWithCreditNote(dnid, companyid)==true){
                        linkedNotes+=debitNote.getDebitNoteNumber()+",";
                        continue;
                    }
                    
                    // check for is CN created from Sales Return
                    
                    
                    if (debitNote.getPurchaseReturn() != null) {
                        //throw new AccountingException("Debit Note : "+debitNote.getDebitNoteNumber()+" is created from purchase return so please delete Purchase Return for deleting it.");
                        linkedNotes+=debitNote.getDebitNoteNumber()+",";
                        continue;
                    }
                    // delete foreign gain loss JE
                    List resultJe = accDebitNoteobj.getForeignGainLossJE(dnid, companyid);
                    if (resultJe.size() > 0 && resultJe.get(0) != null) {
                        Iterator itr = resultJe.iterator();
                        while (itr.hasNext()) {
                            Object object = itr.next();
                            String jeid = object != null ? object.toString() : "";
                            deleteJEArray(jeid, companyid);
                        }
                    }
                    if(debitNote.getApprovestatuslevel()==11){//For pending approval DN we did not need to update invoice amount. It is only needed for DN whose approval level is 11.
                        accDebitNoteService.updateOpeningInvoiceAmountDue(dnid, companyid);
                    }
                    updateCreditNoteAmountDue(dnid, companyid);
                    /*
                     * Before deleting DebitNoteDetail Keeping id of Goodsrceipt
                     * utlized in Payment
                     */
                    Set<String> grIDSet = new HashSet<>();
                    if (debitNote.getApprovestatuslevel() == 11 && !debitNote.isDeleted()) {
                        for (DebitNoteDetail dnd : debitNote.getRows()) {
                            if (dnd.getGoodsReceipt() != null) {
                                grIDSet.add(dnd.getGoodsReceipt().getID());
                            }
                        }
                    }
                    //Delete Rouding JEs if created against PI
                    String roundingJENo = "";
                    String roundingIDs = "";
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
                            roundingIDs = roundingJE.getID() + ",";
                            accDebitNoteService.deleteJEArray(roundingJE.getID(), companyid);
                        }
                       if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                            roundingJENo = roundingJENo.substring(0, roundingJENo.length() - 1);
                        }
                       if (!StringUtil.isNullOrEmpty(roundingIDs)) {
                            roundingIDs = roundingIDs.substring(0, roundingIDs.length() - 1);
                        }
                    }
                    accDebitNoteobj.deleteLinkingInformationOfDN(requestParams);
                    /*ERP-40734
                     *To check that the Debit note is belongs to Locked accoundting period
                     *If it is belong to locked accounting period it will throw the exception
                     */
                    try {
                    accDebitNoteobj.deleteDebitNotesPermanent(requestParams);
                    } catch (AccountingException ex) {
                        linkedNotes += dnno+ ",";
                        continue;
                    }
                    StringBuilder journalEntryMsg = new StringBuilder();
                    if (!StringUtil.isNullOrEmpty(entryno)) {
                        journalEntryMsg.append(" along with the JE No. ").append(entryno);
                    }
                    auditTrailObj.insertAuditLog(AuditAction.DEBIT_NOTE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Debit Note Permanently " + dnno+journalEntryMsg.toString(), request, dnid);
                    
                    if(!StringUtil.isNullOrEmpty(roundingJENo)){
                        auditTrailObj.insertAuditLog(AuditAction.ROUNDING_OFF_JE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Debit Note "+dnno+" Permanently. So Rounding JE No. " + roundingJENo +" deleted.", request, roundingIDs); 
                    }
                }
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)));
        }
        return linkedNotes;
    }
    
    /**
     *  update credit Note Amount due which are linked in Debit Note on Deletion.
     * @param debitNoteId
     * @param companyId
     * @throws JSONException
     * @throws ServiceException 
     */
    public void updateCreditNoteAmountDue(String debitNoteId, String companyId) throws JSONException, ServiceException {

        KwlReturnObject dnObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), debitNoteId);
        if (!dnObj.getEntityList().isEmpty()) {
            DebitNote debitNote = (DebitNote) dnObj.getEntityList().get(0);
            Set<DebitNoteDetail> debitNoteDetails = debitNote.getRows();
            if (debitNoteDetails != null && !debitNote.isDeleted()) {// if debit note already temporary deleted then amountdue already updated. No need to update amountdue again for permament delete.
                Iterator itr = debitNoteDetails.iterator();
                while (itr.hasNext()) {
                    DebitNoteDetail debitNoteDetail = (DebitNoteDetail) itr.next();
                    if (!StringUtil.isNullOrEmpty(debitNoteDetail.getCreditNoteId())) {
                        KwlReturnObject userResult = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), debitNoteDetail.getCreditNoteId());
                        CreditNote creditNote = (CreditNote) userResult.getEntityList().get(0);
                        if (creditNote != null && !creditNote.isNormalCN() && creditNote.isIsOpeningBalenceCN()) {
                            double amountPaid = debitNoteDetail.getDiscount().getAmountinInvCurrency();
                            HashMap<String, Object> requestParams = new HashMap();
                            requestParams.put(Constants.companyid, companyId);
                            requestParams.put("gcurrencyid", creditNote.getCompany().getCurrency().getCurrencyID());
                            double externalCurrencyRate = 0d;
                            externalCurrencyRate = creditNote.getExchangeRateForOpeningTransaction();
                            String fromcurrencyid = creditNote.getCurrency().getCurrencyID();
                            KwlReturnObject bAmt = null;
                            if (creditNote.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountPaid, fromcurrencyid, creditNote.getCreationDate(), externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, creditNote.getCreationDate(), externalCurrencyRate);
                            }
                            double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);

                            double debitNoteAmountDue = creditNote.getOpeningBalanceAmountDue();
                            debitNoteAmountDue += amountPaid;

                            HashMap<String, Object> cnhm = new HashMap<>();
                            cnhm.put("cnid", creditNote.getID());
                            cnhm.put("companyid", creditNote.getCompany().getCompanyID());
                            cnhm.put("openingBalanceAmountDue", debitNoteAmountDue);
                            cnhm.put(Constants.openingBalanceBaseAmountDue, creditNote.getOriginalOpeningBalanceBaseAmount() + totalBaseAmountDue);
                            if (debitNoteAmountDue != 0) {
                                cnhm.put("amountduedate", "");
                            }
                            accCreditNoteDAOobj.updateCreditNote(cnhm);

                        } else if (creditNote != null && creditNote.isNormalCN() && !creditNote.isIsOpeningBalenceCN()) {
//                        double amountPaid = debitNoteDetail.getDiscount().getDiscountValue();
                            double amountPaid = debitNoteDetail.getDiscount().getAmountinInvCurrency();
                            double creditNoteAmountDue = creditNote.getCnamountdue();
                            creditNoteAmountDue += amountPaid;
                            HashMap<String, Object> cnhm = new HashMap<>();
                            cnhm.put("cnid", creditNote.getID());
                            cnhm.put("companyid", creditNote.getCompany().getCompanyID());
                            cnhm.put("cnamountdue", creditNoteAmountDue);
                            cnhm.put("openingBalanceAmountDue", creditNoteAmountDue);
                            
                            JournalEntry je = creditNote.getJournalEntry();
                            HashMap<String, Object> requestParams = new HashMap();
                            requestParams.put(Constants.companyid, companyId);
                            requestParams.put("gcurrencyid", creditNote.getCompany().getCurrency().getCurrencyID());
                            String fromcurrencyid = creditNote.getCurrency().getCurrencyID();
                            if (je != null) {
//                                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, creditNoteAmountDue, fromcurrencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, creditNoteAmountDue, fromcurrencyid, creditNote.getCreationDate(), je.getExternalCurrencyRate());
                                double invoiceamountdueinbase = (Double) baseAmount.getEntityList().get(0);
                                cnhm.put("cnamountinbase", invoiceamountdueinbase);
                            }
                            if (creditNoteAmountDue != 0) {
                                cnhm.put("amountduedate", "");
                            }
                            accCreditNoteDAOobj.updateCreditNote(cnhm);
                        }
                    }
                }
            }
        }
    }
    
private void getDNDetails(HashSet<DebitNoteDetail> dndetails, String companyId) throws ServiceException {

    KwlReturnObject result = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
    Company company = (Company) result.getEntityList().get(0);

    DebitNoteDetail row = new DebitNoteDetail();
    String DebitNoteDetailID = StringUtil.generateUUID();
    row.setID(DebitNoteDetailID);
    row.setSrno(1);
    row.setTotalDiscount(0.00);
    row.setCompany(company);
    row.setMemo("");
    dndetails.add(row);
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
        }
    } catch (Exception ex) {
        //Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        throw ServiceException.FAILURE(ex.getMessage(), ex);
    }
}
}
