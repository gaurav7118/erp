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
package com.krawler.spring.accounting.bankreconciliation;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.sun.corba.se.impl.orbutil.closure.Constant;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

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
 * @author krawler
 */
public class accBankReconciliationController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accBankReconciliationDAO accBankReconciliationObj;
    private String successView;
    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private ImportHandler importHandler;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccBankReconciliationModuleService accBankReconciliationModuleService;

    public void setAccBankReconciliationModuleService(AccBankReconciliationModuleService accBankReconciliationModuleService) {
        this.accBankReconciliationModuleService = accBankReconciliationModuleService;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
    }
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    
    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public ModelAndView saveBankReconciliation(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, JSONException, UnsupportedEncodingException, ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        JSONArray debitArray = new JSONArray();
        JSONArray creditArray = new JSONArray();
        JSONArray unrecondebitArray = new JSONArray();
        JSONArray unreconcreditArray = new JSONArray();
        String accountname="", journalEntryNO="",alreadyExistJournalEntryNO="";
        double clearedAmount =0;
        boolean recordExists = false;
        boolean issuccess = false;
        boolean isConcileReport = StringUtil.isNullOrEmpty(request.getParameter("isConcileReport"))?false:Boolean.parseBoolean(request.getParameter("isConcileReport"));
        boolean isImport = StringUtil.isNullOrEmpty(request.getParameter("isImport"))?false:Boolean.parseBoolean(request.getParameter("isImport"));
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String accountid = request.getParameter("accid");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BRecnl_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        /*
         * Here we are maintaining 2 arrays for deposit and cheque details. if any record is already reconciled/unreconciled
         * Then we are not adding it in the function of saveBankReconciliation() and showing proper message that this records
         * are skipped as they are already saved in the system. Clearing amount is adjusted for same records with the help of 
         * variable clearedAmount.
         */
        JSONArray jArr = new JSONArray(request.getParameter("d_details"));
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject depositejobj = jArr.getJSONObject(i);
            String jeid = depositejobj.getString("d_journalentryid");
            boolean recordExist = accBankReconciliationObj.isAlreadyReconcile(accountid, companyid, jeid, isConcileReport);
            if (recordExist) {
                alreadyExistJournalEntryNO += StringUtil.DecodeText(depositejobj.optString("d_entryno")) + ", ";
                clearedAmount += depositejobj.getDouble("d_amount");
                recordExists = true;
            } else {
                accountname += StringUtil.DecodeText(depositejobj.optString("d_accountname")) + ", ";
                journalEntryNO += StringUtil.DecodeText(depositejobj.optString("d_entryno")) + ", ";
                debitArray.put(depositejobj);
            }
        }
        
            JSONArray jArr2 = new JSONArray(request.getParameter("c_details"));
            for (int i = 0; i < jArr2.length(); i++) {
                JSONObject checkjobj = jArr2.getJSONObject(i);
                String jeid = checkjobj.getString("c_journalentryid");
                boolean recordExist = accBankReconciliationObj.isAlreadyReconcile(accountid, companyid, jeid, isConcileReport);
                if (recordExist) {
                    alreadyExistJournalEntryNO += StringUtil.DecodeText(checkjobj.optString("c_entryno")) + ", ";
                    clearedAmount -= checkjobj.getDouble("c_amount");
                    recordExists = true;
                } else {
                    accountname += StringUtil.DecodeText(checkjobj.optString("c_accountname")) + " ,";
                    journalEntryNO += StringUtil.DecodeText(checkjobj.optString("c_entryno")) + ", ";
                    creditArray.put(checkjobj);
                }
            }
        
        //============ Code to save unreconciled records into BankReconciliationHistory Table =============//
        //as per discussion with Vaibhav Patil Implemented !isImport case as for import the bankreconciliation history case is not handled need to remove this at the time of handling import cases.
         if (!isImport) {
            JSONArray jArr3 = new JSONArray(request.getParameter("ud_details"));    //Un-reconciled Deposits
            for (int i = 0; i < jArr3.length(); i++) {
                JSONObject depositejobj = jArr3.getJSONObject(i);
                accountname += StringUtil.DecodeText(depositejobj.optString("d_accountname")) + ", ";
                //            journalEntryNO += StringUtil.DecodeText(depositejobj.optString("d_entryno")) + ", ";
                unrecondebitArray.put(depositejobj);
            }

            JSONArray jArr4 = new JSONArray(request.getParameter("uc_details"));    //Un-reconciled Checks
            for (int i = 0; i < jArr4.length(); i++) {
                JSONObject checkjobj = jArr4.getJSONObject(i);
                accountname += StringUtil.DecodeText(checkjobj.optString("c_accountname")) + " ,";
                //            journalEntryNO += StringUtil.DecodeText(checkjobj.optString("c_entryno")) + ", ";
                unreconcreditArray.put(checkjobj);
            }
        }
        //============ Code to save unreconciled records into BankReconciliationHistory Table =============//
                
        try {

            if (isConcileReport) {
                deleteBankReconciliation(request);
                msg = messageSource.getMessage("acc.br.uncon", null, RequestContextUtils.getLocale(request));
            }

            saveBankReconciliation(request, debitArray, creditArray, unrecondebitArray, unreconcreditArray, clearedAmount);

            if (!isConcileReport) {
                if (recordExists) {
                    msg += messageSource.getMessage("acc.bankReconcile.except", null, RequestContextUtils.getLocale(request)) + " " + (java.util.Arrays.toString(alreadyExistJournalEntryNO.split(","))) + " " + messageSource.getMessage("acc.bankReconcile.successfully", null, RequestContextUtils.getLocale(request));
                } else {
                    msg = messageSource.getMessage("acc.br.save", null, RequestContextUtils.getLocale(request));   //"Bank Reconciliation Entry has been saved successfully.";
                }
                /**
                 * Removing last comma from journalEntryNO
                 */
                if (!StringUtil.isNullOrEmpty(journalEntryNO)) {
                    journalEntryNO = journalEntryNO.substring(0, journalEntryNO.length() - 2);
                }
                auditTrailObj.insertAuditLog(AuditAction.BANK_RECONCILIATION_ADDED, "User " + sessionHandlerImpl.getUserFullName(request) + " has reconciled " + (java.util.Arrays.toString(journalEntryNO.split(","))) , request, companyid);
            }
            issuccess = true;
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveBankReconciliation(HttpServletRequest request, JSONArray dArr, JSONArray cArr, JSONArray unrecondebitArray, JSONArray unreconcreditArray, double clearedAmount) throws SessionExpiredException, ServiceException, AccountingException {
        try {
            boolean isConcileReport = StringUtil.isNullOrEmpty(request.getParameter("isConcileReport"))?false:Boolean.parseBoolean(request.getParameter("isConcileReport"));
            String docID = request.getParameter("docID");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String createdby = sessionHandlerImpl.getUserid(request);
            String accountid = request.getParameter("accid");
            String stdate = request.getParameter("startdate");
            boolean isImport = StringUtil.isNullOrEmpty(request.getParameter("isImport"))?false:Boolean.parseBoolean(request.getParameter("isImport"));
            Date startdate = null;
            if (!StringUtil.isNullOrEmpty(stdate)) {
                startdate = authHandler.getDateOnlyFormat().parse(request.getParameter("startdate"));
            }
            
            Date enddate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate"));
            Date clearanceDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("clearanceDate"));
            double clearingAmount = Double.parseDouble(request.getParameter("clearingbalance"));
            String reconcilenumber = !StringUtil.isNullOrEmpty(request.getParameter("reconcilenumber")) ? request.getParameter("reconcilenumber") : "";
            String sequenceFormat = StringUtil.isNullOrEmpty(request.getParameter("sequenceformat")) ? "NA" : request.getParameter("sequenceformat");
            int autonumbermodule = isConcileReport ? StaticValues.AUTONUM_UNRECONCILENO : StaticValues.AUTONUM_RECONCILENO;
            
            double bankStmtBalanceinAcc = 0, clearedChecksAmountinAcc = 0, clearedDepositsAmountinAcc = 0, unclearedChecksAmountinAcc = 0, unclearedDepositsAmountinAcc = 0; 
            double bankBookBalanceinAcc = !StringUtil.isNullOrEmpty(request.getParameter("bankBookBalanceinAcc")) ? Double.parseDouble(request.getParameter("bankBookBalanceinAcc")) : 0;
            HashMap<String, Object> brMap = new HashMap<>();
            brMap.put("startdate", startdate);
            brMap.put("enddate", enddate);
            //Wa are deducting the cleared amount entry from total Clearing amount for already reconciled items.
            brMap.put("clearingamount", clearingAmount-clearedAmount); 
            brMap.put("clearanceDate", clearanceDate);
            brMap.put("accountid", accountid);
            brMap.put("companyid", companyid);
            brMap.put("createdby", createdby);
            brMap.put("isUnreconsile", isConcileReport);
            brMap.put("checkCount", cArr.length());
            brMap.put("depositeCount", dArr.length());
            long createdon = System.currentTimeMillis();
            brMap.put("createdon", createdon);
            KwlReturnObject brresult = accBankReconciliationObj.addBankReconciliation(brMap);
            BankReconciliation br = (BankReconciliation) brresult.getEntityList().get(0);
            String brid = br.getID();
            
            HashSet hs = new HashSet();
            HashSet hus = new HashSet();
            if (!isConcileReport) {
                for (int i = 0; i < dArr.length(); i++) {   //Seleced Deposits from Bank Reconciliation Report
                    JSONObject jobj = dArr.getJSONObject(i);
                    HashMap<String, Object> brdMap = new HashMap<>();                    
                    Date ddate = (StringUtil.isNullOrEmpty(jobj.optString("d_date"))) ? null : authHandler.getDateOnlyFormat().parse(jobj.optString("d_date"));
                    Date chequeDate = (StringUtil.isNullOrEmpty(jobj.optString("chequedate"))) ? null : authHandler.getDateOnlyFormat().parse(jobj.optString("chequedate"));
                    brdMap.put("companyid", companyid);              
                    brdMap.put("reconcileDate", clearanceDate);
                    brdMap.put("date", ddate);   //Date
                    brdMap.put("accountname", StringUtil.DecodeText(jobj.optString("d_accountname")));  //Customer Name / Vendor Name
                    brdMap.put("paidto", jobj.optString("paidto"));   //Received From / Paid To
                    brdMap.put("chequeno", jobj.optString("chequeno"));   //Cheque No
                    brdMap.put("chequedate", chequeDate);   //Cheque Date                                    
                    brdMap.put("description", StringUtil.DecodeText(jobj.optString("description")));   //Reference No. / Description
                    brdMap.put("entryno", jobj.optString("d_entryno"));   //JE No
                    brdMap.put("jeid", StringUtil.DecodeText(jobj.optString("d_journalentryid")));  //JE ID       
                    brdMap.put("transactionID", jobj.optString("billid", ""));  //Transaction ID
                    brdMap.put("transactionNumber", jobj.optString("transactionID", ""));  //Transaction Number
                    brdMap.put("transcurrsymbol", jobj.optString("currencysymbol"));  //Document Currency Symbol
                    brdMap.put("amountintransactioncurrency", jobj.optDouble("d_amountintransactioncurrency"));  //Amount in Document Currency
                    brdMap.put("accountcurrencysymbol", jobj.optString("accountcurrencysymbol"));  //Account Currency Symbol                 
                    brdMap.put("amountinacc", jobj.optDouble("d_amountinacc"));  //Amount in Account Currency
                    brdMap.put("amount", jobj.optDouble("d_amount"));  //Amount in Base Currency
                    brdMap.put("clearedstatus", Constants.CLEARED_DEPOSITS); 
                    brdMap.put("reportname", Constants.BANK_RECONCILIATION_REPORT);
                    brdMap.put("debit", true);                    
                    brdMap.put("brid", brid);
                    brdMap.put("moduleID", jobj.optInt("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId));
                    brdMap.put("isOpeningTransaction", jobj.optBoolean("isOpeningTransaction", false));
                    
                    KwlReturnObject brdresult = accBankReconciliationObj.addBankReconciliationDetail(brdMap);
                    BankReconciliationDetail brd = (BankReconciliationDetail) brdresult.getEntityList().get(0);
                    hs.add(brd);
                    
                    clearedDepositsAmountinAcc += jobj.getDouble("d_amountinacc");

                    //Maintain Reconciliation History Details
                    accBankReconciliationObj.addBankReconciliationDetailsHistory(brdMap);
                }
                for (int i = 0; i < cArr.length(); i++) {   //Seleced Checks from Bank Reconciliation Report
                    JSONObject jobj = cArr.getJSONObject(i);
                    HashMap<String, Object> brdMap = new HashMap<>();
                    Date ddate =  isImport ? ((StringUtil.isNullOrEmpty(jobj.optString("d_date"))) ? null : authHandler.getDateOnlyFormat().parse(jobj.optString("d_date"))) : ((StringUtil.isNullOrEmpty(jobj.optString("d_date"))) ? null : authHandler.getDateOnlyFormat().parse(jobj.optString("c_date")));
                    Date chequeDate = (StringUtil.isNullOrEmpty(jobj.optString("chequedate"))) ? null : authHandler.getDateOnlyFormat().parse(jobj.optString("chequedate"));
                    
                    brdMap.put("companyid", companyid);
                    brdMap.put("reconcileDate", clearanceDate);
                    brdMap.put("date", ddate);   //Date
                    brdMap.put("accountname", StringUtil.DecodeText(jobj.optString("c_accountname")));  //Customer Name / Vendor Name
                    brdMap.put("paidto", jobj.optString("paidto"));   //Received From / Paid To
                    brdMap.put("chequeno", jobj.optString("chequeno"));   //Cheque No
                    brdMap.put("chequedate", chequeDate);   //Cheque Date    
                    brdMap.put("description", jobj.optString("description"));   //Reference No. / Description
                    brdMap.put("entryno", jobj.optString("c_entryno"));   //JE No
                    brdMap.put("jeid", StringUtil.DecodeText(jobj.optString("c_journalentryid")));  //JE ID     
                    brdMap.put("transactionID", jobj.optString("billid", ""));  //Transaction ID
                    brdMap.put("transactionNumber", jobj.optString("transactionID", ""));  //Transaction Number
                    brdMap.put("transcurrsymbol", jobj.optString("currencysymbol"));  //Document Currency Symbol
                    brdMap.put("amountintransactioncurrency", isImport ? jobj.optDouble("d_amountintransactioncurrency",0.0) : jobj.optDouble("c_amountintransactioncurrency",0.0));  //Amount in Document Currency
                    brdMap.put("accountcurrencysymbol", jobj.optString("accountcurrencysymbol"));  //Account Currency Symbol           
                    brdMap.put("amountinacc", isImport ? jobj.optDouble("d_amountinacc",0.0) : jobj.optDouble("c_amountinacc",0.0));  //Amount in Account Currency
                    brdMap.put("amount", jobj.optDouble("c_amount"));  //Amount in Base Currency
                    brdMap.put("clearedstatus", Constants.CLEARED_CHECKS);   
                    brdMap.put("reportname", Constants.BANK_RECONCILIATION_REPORT);
                    brdMap.put("debit", false);                    
                    brdMap.put("brid", brid);
                    brdMap.put("moduleID", jobj.optInt("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId));
                    brdMap.put("isOpeningTransaction", jobj.optBoolean("isOpeningTransaction", false));
                    
                    KwlReturnObject brdresult = accBankReconciliationObj.addBankReconciliationDetail(brdMap);
                    BankReconciliationDetail brd = (BankReconciliationDetail) brdresult.getEntityList().get(0);
                    hs.add(brd);                    
                    clearedChecksAmountinAcc += isImport ? jobj.optDouble("d_amountinacc",0.0) : jobj.optDouble("c_amountinacc", 0.0);
                    //Maintain Reconciliation History Details
                    accBankReconciliationObj.addBankReconciliationDetailsHistory(brdMap);
                }
                
                for (int i = 0; i < unrecondebitArray.length(); i++) {   //Non-Seleced Deposits from Bank Reconciliation Report
                    JSONObject jobj = unrecondebitArray.getJSONObject(i);
                    HashMap<String, Object> brdMap = new HashMap<>();
                    Date ddate = (StringUtil.isNullOrEmpty(jobj.optString("d_date"))) ? null : authHandler.getDateOnlyFormat().parse(jobj.optString("d_date"));
                    Date chequeDate = (StringUtil.isNullOrEmpty(jobj.optString("chequedate"))) ? null : authHandler.getDateOnlyFormat().parse(jobj.optString("chequedate"));
                    
                    brdMap.put("companyid", companyid);
                    brdMap.put("reconcileDate", clearanceDate);
                    brdMap.put("date", ddate);   //Date
                    brdMap.put("accountname", StringUtil.DecodeText(jobj.optString("d_accountname")));  //Customer Name / Vendor Name
                    brdMap.put("paidto", jobj.optString("paidto"));   //Received From / Paid To
                    brdMap.put("chequeno", jobj.optString("chequeno"));   //Cheque No
                    brdMap.put("chequedate", chequeDate);   //Cheque Date    
                    brdMap.put("description", jobj.optString("description"));   //Reference No. / Description
                    brdMap.put("entryno", jobj.optString("d_entryno"));   //JE No
                    brdMap.put("jeid", StringUtil.DecodeText(jobj.optString("d_journalentryid")));  //JE ID     
                    brdMap.put("transactionID", jobj.optString("billid", ""));  //Transaction ID
                    brdMap.put("transactionNumber", jobj.optString("transactionID", ""));  //Transaction Number
                    brdMap.put("transcurrsymbol", jobj.optString("currencysymbol"));  //Document Currency Symbol
                    brdMap.put("amountintransactioncurrency", jobj.getDouble("d_amountintransactioncurrency"));  //Amount in Document Currency
                    brdMap.put("accountcurrencysymbol", jobj.optString("accountcurrencysymbol"));  //Account Currency Symbol   
                    brdMap.put("amountinacc", jobj.getDouble("d_amountinacc"));  //Amount in Account Currency
                    brdMap.put("amount", jobj.getDouble("d_amount"));  //Amount in Base Currency
                    brdMap.put("clearedstatus", Constants.UNCLEARED_DEPOSITS); 
                    brdMap.put("reportname", Constants.BANK_RECONCILIATION_REPORT);
                    brdMap.put("debit", true);
                    brdMap.put("brid", brid);
                    brdMap.put("moduleID", jobj.optInt("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId));
                    brdMap.put("isOpeningTransaction", jobj.optBoolean("isOpeningTransaction", false));
                    unclearedDepositsAmountinAcc += jobj.getDouble("d_amountinacc");
                    //Maintain Reconciliation History Details
                    accBankReconciliationObj.addBankReconciliationDetailsHistory(brdMap);
                }
                for (int i = 0; i < unreconcreditArray.length(); i++) { //Non-Seleced Checks from Bank Reconciliation Report
                    JSONObject jobj = unreconcreditArray.getJSONObject(i);
                    HashMap<String, Object> brdMap = new HashMap<>();
                    Date ddate = (StringUtil.isNullOrEmpty(jobj.optString("c_date"))) ? null : authHandler.getDateOnlyFormat().parse(jobj.optString("c_date"));
                    Date chequeDate = (StringUtil.isNullOrEmpty(jobj.optString("chequedate"))) ? null : authHandler.getDateOnlyFormat().parse(jobj.optString("chequedate"));
                    
                    brdMap.put("companyid", companyid);
                    brdMap.put("reconcileDate", clearanceDate);
                    brdMap.put("date", ddate);   //Date
                    brdMap.put("accountname", StringUtil.DecodeText(jobj.optString("c_accountname")));  //Customer Name / Vendor Name
                    brdMap.put("paidto", jobj.optString("paidto"));   //Received From / Paid To
                    brdMap.put("chequeno", jobj.optString("chequeno"));   //Cheque No
                    brdMap.put("chequedate", chequeDate);   //Cheque Date    
                    brdMap.put("description", jobj.optString("description"));   //Reference No. / Description
                    brdMap.put("entryno", jobj.optString("c_entryno"));   //JE No
                    brdMap.put("jeid", StringUtil.DecodeText(jobj.optString("c_journalentryid")));  //JE ID     
                    brdMap.put("transactionID", jobj.optString("billid", ""));  //Transaction ID
                    brdMap.put("transactionNumber", jobj.optString("transactionID", ""));  //Transaction Number
                    brdMap.put("transcurrsymbol", jobj.optString("currencysymbol"));  //Document Currency Symbol
                    brdMap.put("amountintransactioncurrency", jobj.getDouble("c_amountintransactioncurrency"));  //Amount in Document Currency
                    brdMap.put("accountcurrencysymbol", jobj.optString("accountcurrencysymbol"));  //Account Currency Symbol   
                    brdMap.put("amountinacc", jobj.getDouble("c_amountinacc"));  //Amount in Account Currency
                    brdMap.put("amount", jobj.getDouble("c_amount"));  //Amount in Base Currency
                    brdMap.put("clearedstatus", Constants.UNCLEARED_CHECKS);  
                    brdMap.put("reportname", Constants.BANK_RECONCILIATION_REPORT);
                    brdMap.put("debit", false);
                    brdMap.put("brid", brid);
                    brdMap.put("moduleID", jobj.optInt("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId));
                    brdMap.put("isOpeningTransaction", jobj.optBoolean("isOpeningTransaction", false));
                    
                    unclearedChecksAmountinAcc += jobj.getDouble("c_amountinacc");
                    //Maintain Reconciliation History Details
                    accBankReconciliationObj.addBankReconciliationDetailsHistory(brdMap);
                }
            } else {    //========================  View Reconcile Report  =========================
                for (int i = 0; i < dArr.length(); i++) {   //Selected Deposits from View Reconciled Reports
                    JSONObject jobj = dArr.getJSONObject(i);
                    HashMap<String, Object> brdMap = new HashMap<>();
                    Date ddate = (StringUtil.isNullOrEmpty(jobj.optString("d_date"))) ? null : authHandler.getDateOnlyFormat().parse(jobj.optString("d_date"));
                    Date chequeDate = (StringUtil.isNullOrEmpty(jobj.optString("chequedate"))) ? null : authHandler.getDateOnlyFormat().parse(jobj.optString("chequedate"));
                    
                    brdMap.put("companyid", companyid);
                    brdMap.put("reconcileDate", clearanceDate);
                    brdMap.put("date", ddate);   //Date
                    brdMap.put("accountname", StringUtil.DecodeText(jobj.optString("d_accountname")));  //Customer Name / Vendor Name
                    brdMap.put("paidto", jobj.optString("paidto"));   //Received From / Paid To
                    brdMap.put("chequeno", jobj.optString("chequeno"));   //Cheque No
                    brdMap.put("chequedate", chequeDate);   //Cheque Date    
                    brdMap.put("description", jobj.optString("description"));   //Reference No. / Description
                    brdMap.put("entryno", jobj.optString("d_entryno"));   //JE No
                    brdMap.put("jeid", StringUtil.DecodeText(jobj.optString("d_journalentryid")));  //JE ID     
                    brdMap.put("transactionID", jobj.optString("billid", ""));  //Transaction ID
                    brdMap.put("transactionNumber", jobj.optString("transactionID", ""));  //Transaction Number
                    brdMap.put("transcurrsymbol", jobj.optString("currencysymbol"));  //Document Currency Symbol
                    brdMap.put("amountintransactioncurrency", jobj.getDouble("d_amountintransactioncurrency"));  //Amount in Document Currency
                    brdMap.put("accountcurrencysymbol", jobj.optString("accountcurrencysymbol"));  //Account Currency Symbol 
                    brdMap.put("amountinacc", jobj.getDouble("d_amountinacc"));  //Amount in Account Currency
                    brdMap.put("amount", jobj.getDouble("d_amount"));  //Amount in Base Currency
                    brdMap.put("clearedstatus", Constants.CLEARED_DEPOSITS);     
                    brdMap.put("reportname", Constants.VIEW_RECONCILIATION_REPORT);
                    brdMap.put("debit", true);
                    brdMap.put("brid", brid);
                    brdMap.put("moduleID", jobj.optInt("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId));
                    brdMap.put("isOpeningTransaction", jobj.optBoolean("isOpeningTransaction", false));
                    KwlReturnObject brdresult = accBankReconciliationObj.addBankUnreconciliationDetail(brdMap);
                    BankUnreconciliationDetail burd = (BankUnreconciliationDetail) brdresult.getEntityList().get(0);
                    hus.add(burd);                    
                    clearedDepositsAmountinAcc += jobj.getDouble("d_amountinacc");
                    //Maintain Reconciliation History Details
                    accBankReconciliationObj.addBankReconciliationDetailsHistory(brdMap);
                }
                for (int i = 0; i < cArr.length(); i++) {    //Selected Checks from View Reconciled Reports
                    JSONObject jobj = cArr.getJSONObject(i);
                    HashMap<String, Object> brdMap = new HashMap<>();
                    Date ddate = (StringUtil.isNullOrEmpty(jobj.optString("c_date"))) ? null : authHandler.getDateOnlyFormat().parse(jobj.optString("c_date"));
                    Date chequeDate = (StringUtil.isNullOrEmpty(jobj.optString("chequedate"))) ? null : authHandler.getDateOnlyFormat().parse(jobj.optString("chequedate"));
                    
                    brdMap.put("companyid", companyid);
                    brdMap.put("reconcileDate", clearanceDate);
                    brdMap.put("date", ddate);   //Date
                    brdMap.put("accountname", StringUtil.DecodeText(jobj.optString("c_accountname")));  //Customer Name / Vendor Name
                    brdMap.put("paidto", jobj.optString("paidto"));   //Received From / Paid To
                    brdMap.put("chequeno", jobj.optString("chequeno"));   //Cheque No
                    brdMap.put("chequedate", chequeDate);   //Cheque Date    
                    brdMap.put("description", jobj.optString("description"));   //Reference No. / Description
                    brdMap.put("entryno", jobj.optString("c_entryno"));   //JE No
                    brdMap.put("jeid", StringUtil.DecodeText(jobj.optString("c_journalentryid")));  //JE ID     
                    brdMap.put("transactionID", jobj.optString("billid", ""));  //Transaction ID
                    brdMap.put("transactionNumber", jobj.optString("transactionID", ""));  //Transaction Number
                    brdMap.put("transcurrsymbol", jobj.optString("currencysymbol"));  //Document Currency Symbol
                    brdMap.put("amountintransactioncurrency", jobj.getDouble("c_amountintransactioncurrency"));  //Amount in Document Currency
                    brdMap.put("accountcurrencysymbol", jobj.optString("accountcurrencysymbol"));  //Account Currency Symbol 
                    brdMap.put("amountinacc", jobj.getDouble("c_amountinacc"));  //Amount in Account Currency
                    brdMap.put("amount", jobj.getDouble("c_amount"));  //Amount in Base Currency
                    brdMap.put("clearedstatus", Constants.CLEARED_CHECKS);     
                    brdMap.put("reportname", Constants.VIEW_RECONCILIATION_REPORT);
                    brdMap.put("debit", false);
                    brdMap.put("brid", brid);
                    brdMap.put("moduleID", jobj.optInt("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId));
                    brdMap.put("isOpeningTransaction", jobj.optBoolean("isOpeningTransaction", false));
                    KwlReturnObject brdresult = accBankReconciliationObj.addBankUnreconciliationDetail(brdMap);
                    BankUnreconciliationDetail burd = (BankUnreconciliationDetail) brdresult.getEntityList().get(0);
                    hus.add(burd);                    
                    clearedChecksAmountinAcc += jobj.optDouble("c_amountinacc",0);
                    //Maintain Reconciliation History Details
                    accBankReconciliationObj.addBankReconciliationDetailsHistory(brdMap);
                }
                
                for (int i = 0; i < unrecondebitArray.length(); i++) {      //Non-Selected Deposits from View Reconciled Reports
                    JSONObject jobj = unrecondebitArray.getJSONObject(i);
                    HashMap<String, Object> brdMap = new HashMap<>();
                    Date ddate = (StringUtil.isNullOrEmpty(jobj.optString("d_date"))) ? null : authHandler.getDateOnlyFormat().parse(jobj.optString("d_date"));
                    Date chequeDate = (StringUtil.isNullOrEmpty(jobj.optString("chequedate"))) ? null : authHandler.getDateOnlyFormat().parse(jobj.optString("chequedate"));
                    
                    brdMap.put("companyid", companyid);
                    brdMap.put("reconcileDate", clearanceDate);
                    brdMap.put("date", ddate);   //Date
                    brdMap.put("accountname", StringUtil.DecodeText(jobj.optString("d_accountname")));  //Customer Name / Vendor Name
                    brdMap.put("paidto", jobj.optString("paidto"));   //Received From / Paid To
                    brdMap.put("chequeno", jobj.optString("chequeno"));   //Cheque No
                    brdMap.put("chequedate", chequeDate);   //Cheque Date    
                    brdMap.put("description", jobj.optString("description"));   //Reference No. / Description
                    brdMap.put("entryno", jobj.optString("d_entryno"));   //JE No
                    brdMap.put("jeid", StringUtil.DecodeText(jobj.optString("d_journalentryid")));  //JE ID     
                    brdMap.put("transactionID", jobj.optString("billid", ""));  //Transaction ID
                    brdMap.put("transactionNumber", jobj.optString("transactionID", ""));  //Transaction Number
                    brdMap.put("transcurrsymbol", jobj.optString("currencysymbol"));  //Document Currency Symbol
                    brdMap.put("amountintransactioncurrency", jobj.getDouble("d_amountintransactioncurrency"));  //Amount in Document Currency
                    brdMap.put("accountcurrencysymbol", jobj.optString("accountcurrencysymbol"));  //Account Currency Symbol    
                    brdMap.put("amountinacc", jobj.getDouble("d_amountinacc"));  //Amount in Account Currency
                    brdMap.put("amount", jobj.getDouble("d_amount"));  //Amount in Base Currency
                    brdMap.put("clearedstatus", Constants.UNCLEARED_DEPOSITS);  
                    brdMap.put("reportname", Constants.VIEW_RECONCILIATION_REPORT);
                    brdMap.put("debit", true);
                    brdMap.put("brid", brid);
                    brdMap.put("moduleID", jobj.optInt("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId));
                    brdMap.put("isOpeningTransaction", jobj.optBoolean("isOpeningTransaction", false));
                    unclearedDepositsAmountinAcc += jobj.getDouble("d_amountinacc");
                    //Maintain Reconciliation History Details
                    accBankReconciliationObj.addBankReconciliationDetailsHistory(brdMap);
                }
                for (int i = 0; i < unreconcreditArray.length(); i++) {     //Non-Selected Checks from View Reconciled Reports
                    JSONObject jobj = unreconcreditArray.getJSONObject(i);
                    HashMap<String, Object> brdMap = new HashMap<>();
                    Date ddate = (StringUtil.isNullOrEmpty(jobj.optString("c_date"))) ? null : authHandler.getDateOnlyFormat().parse(jobj.optString("c_date"));
                    Date chequeDate = (StringUtil.isNullOrEmpty(jobj.optString("chequedate"))) ? null : authHandler.getDateOnlyFormat().parse(jobj.optString("chequedate"));
                    
                    brdMap.put("companyid", companyid);
                    brdMap.put("reconcileDate", clearanceDate);
                    brdMap.put("date", ddate);   //Date
                    brdMap.put("accountname", StringUtil.DecodeText(jobj.optString("c_accountname")));  //Customer Name / Vendor Name
                    brdMap.put("paidto", jobj.optString("paidto"));   //Received From / Paid To
                    brdMap.put("chequeno", jobj.optString("chequeno"));   //Cheque No
                    brdMap.put("chequedate", chequeDate);   //Cheque Date    
                    brdMap.put("description", jobj.optString("description"));   //Reference No. / Description
                    brdMap.put("entryno", jobj.optString("c_entryno"));   //JE No
                    brdMap.put("jeid", StringUtil.DecodeText(jobj.optString("c_journalentryid")));  //JE ID     
                    brdMap.put("transactionID", jobj.optString("billid", ""));  //Transaction ID
                    brdMap.put("transactionNumber", jobj.optString("transactionID", ""));  //Transaction Number
                    brdMap.put("transcurrsymbol", jobj.optString("currencysymbol"));  //Document Currency Symbol
                    brdMap.put("amountintransactioncurrency", jobj.getDouble("c_amountintransactioncurrency"));  //Amount in Document Currency
                    brdMap.put("accountcurrencysymbol", jobj.optString("accountcurrencysymbol"));  //Account Currency Symbol    
                    brdMap.put("amountinacc", jobj.getDouble("c_amountinacc"));  //Amount in Account Currency
                    brdMap.put("amount", jobj.getDouble("c_amount"));  //Amount in Base Currency
                    brdMap.put("clearedstatus", Constants.UNCLEARED_CHECKS);  
                    brdMap.put("reportname", Constants.VIEW_RECONCILIATION_REPORT);
                    brdMap.put("debit", false);
                    brdMap.put("brid", brid);
                    brdMap.put("moduleID", jobj.optInt("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId));
                    brdMap.put("isOpeningTransaction", jobj.optBoolean("isOpeningTransaction", false));
                    unclearedChecksAmountinAcc += jobj.getDouble("c_amountinacc");
                    //Maintain Reconciliation History Details
                    accBankReconciliationObj.addBankReconciliationDetailsHistory(brdMap);
                }
            }
            
            //Save Bank Book Balance
            br.setBankBookBalanceinAcc(bankBookBalanceinAcc);
            
            //Save Cleared Checks & Deposits
            br.setClearedChecksAmountinAcc(clearedChecksAmountinAcc);
            br.setClearedDepositsAmountinAcc(clearedDepositsAmountinAcc);
            
            //Save Uncleared Checks & Deposits
            br.setUnclearedChecksAmountinAcc(unclearedChecksAmountinAcc);
            br.setUnclearedDepositsAmountinAcc(-unclearedDepositsAmountinAcc);
            
            if(!isConcileReport){   //Bank Reconciliation report
                bankStmtBalanceinAcc = bankBookBalanceinAcc + unclearedChecksAmountinAcc - unclearedDepositsAmountinAcc;
                br.setBankStmtBalanceinAcc(bankStmtBalanceinAcc);
            } else {                //View Reconciliation report
                bankStmtBalanceinAcc = bankBookBalanceinAcc + clearedChecksAmountinAcc - clearedDepositsAmountinAcc;
                br.setBankStmtBalanceinAcc(bankStmtBalanceinAcc);
            }                     
            accBankReconciliationObj.updateBankReconciliation(br);
            
            if(!StringUtil.isNullOrEmpty(docID)){
                accBankReconciliationObj.updateBankReconciliationDocuments(docID, brid);
            }
            
            //From here auto number generation logic start
            synchronized (this) {
                if (sequenceFormat.equalsIgnoreCase("NA")) {     // Manually entered Reconcile No. If reconcile no. is already exist then we will append '-1' to this reconcile no. & will save as latest reconcile no.
                    int count = accBankReconciliationObj.searchReconcileNo(reconcilenumber, companyid, isConcileReport).getRecordTotalCount();
                    while (count > 0) {
                        //reconcilenumber = reconcilenumber + "-1";
                        //count = accBankReconciliationObj.searchReconcileNo(reconcilenumber, companyid, isConcileReport).getRecordTotalCount();
                        if(isConcileReport){
                            throw new AccountingException("Un-Reconcile No \""+reconcilenumber+"\" is already exists.");
                        } else{
                            throw new AccountingException("Reconcile No \""+reconcilenumber+"\" is already exists."); 
                        }
                    }
                    br.setReconcilenumber(reconcilenumber);
                    accBankReconciliationObj.updateBankReconciliation(br);
                } else {    //Auto generated Reconcile No. If reconcile no. is already exist then we will throw exception
                    int count = accBankReconciliationObj.searchReconcileNo(reconcilenumber, companyid, isConcileReport).getRecordTotalCount();
                    if (count > 0){ 
                        if(isConcileReport){
                            throw new AccountingException("Un-Reconcile No \""+reconcilenumber+"\" is already exists.");
                        } else{
                            throw new AccountingException("Reconcile No \""+reconcilenumber+"\" is already exists.");    
                        }
                    }
                    KwlReturnObject capresult = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), sequenceFormat);
                    SequenceFormat format = (SequenceFormat) capresult.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, autonumbermodule, sequenceFormat, false, br.getClearanceDate());
                    reconcilenumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    String nextAutoNoInt = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    String datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    String dateafterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                    String dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    br.setAutoGenerated(true);
                    br.setReconcilenumber(reconcilenumber);
                    br.setSeqformat(format);
                    br.setSeqnumber(Integer.parseInt(nextAutoNoInt));
                    br.setDatePreffixValue(datePrefix);
                    br.setDateAfterPreffixValue(dateafterPrefix);
                    br.setDateSuffixValue(dateSuffix);
                    accBankReconciliationObj.updateBankReconciliation(br);
                }
            }
            
            JSONObject paramJobj = new JSONObject();
            paramJobj.put(Constants.companyKey,companyid);
            paramJobj.put("accountid",accountid);
            
            deleteBankReconciliationDraft(paramJobj);
//            brMap.put("id", brid);
//            brMap.put("brdetails", hs);
//            brMap.put("burdetails", hus);
//            brresult = accBankReconciliationObj.updateBankReconciliation(brMap);
//            br = (BankReconciliation) brresult.getEntityList().get(0);
        } /*catch (UnsupportedEncodingException e) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), e);
        } */catch (ParseException ex) {
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveBankReconciliation : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveBankReconciliation : " + ex.getMessage(), ex);
        }
    }
    
    public ModelAndView saveBankReconciliationDraft(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String companyid = paramJobj.optString(Constants.companyKey,"");
            String accountid = paramJobj.optString("accountid","");

            KwlReturnObject resultObj = null;

            long createdon = System.currentTimeMillis();
            long updatedon = createdon;
            double clearedChecksAmountinAcc = 0, clearedDepositsAmountinAcc = 0, unclearedChecksAmountinAcc = 0, unclearedDepositsAmountinAcc = 0;

            deleteBankReconciliationDraft(paramJobj);
            
            String selectRecs = paramJobj.optString("selectedrec", "{}");
            JSONObject selectedRecsJobj = new JSONObject(selectRecs);

            JSONArray ldetails = selectedRecsJobj.optJSONArray("ldetails");
            JSONArray rdetails = selectedRecsJobj.optJSONArray("rdetails");
            JSONArray uldetails = selectedRecsJobj.optJSONArray("uldetails");
            JSONArray urdetails = selectedRecsJobj.optJSONArray("urdetails");
            StringBuilder jeIdBuilder = new StringBuilder();
            double clearedAmount =0;
            double clearingAmount = !StringUtil.isNullOrEmpty(request.getParameter("clearingbalance")) ? Double.parseDouble(request.getParameter("clearingbalance")) :0.0;
            JSONObject detailsObj = null;
            
            for (int i = 0; i < ldetails.length(); i++) {
                detailsObj = ldetails.optJSONObject(i);
                String jeid = detailsObj.optString("d_journalentryid", "");
                boolean recordExist = accBankReconciliationObj.isAlreadyReconcile(accountid, companyid,jeid , false);
                if (recordExist) {
                    clearedAmount += detailsObj.optDouble("d_amount", 0.0);
                } else {
                    if (!StringUtil.isNullOrEmpty(jeid)) {
                        jeIdBuilder.append("'").append(jeid).append("',");
                    }
                    clearedDepositsAmountinAcc += detailsObj.optDouble("d_amountinacc", 0);
                }
            }
          
            for (int i = 0; i < rdetails.length(); i++) {
                detailsObj = rdetails.optJSONObject(i);
                String jeid = detailsObj.optString("c_journalentryid", "");
                boolean recordExist = accBankReconciliationObj.isAlreadyReconcile(accountid, companyid, jeid, false);
                if (recordExist) {
                    clearedAmount -= detailsObj.optDouble("c_amount", 0.0);
                } else {
                    if (!StringUtil.isNullOrEmpty(jeid)) {
                        jeIdBuilder.append("'").append(jeid).append("',");
                    }
                    clearedChecksAmountinAcc += detailsObj.optDouble("c_amountinacc", 0);
                }
            }
            
            for (int i = 0; i < uldetails.length(); i++) {
                detailsObj = uldetails.optJSONObject(i);
                unclearedDepositsAmountinAcc += detailsObj.optDouble("d_amountinacc", 0);
            }
            
            for (int i = 0; i < urdetails.length(); i++) {
                detailsObj = urdetails.optJSONObject(i);
                unclearedChecksAmountinAcc += detailsObj.optDouble("c_amountinacc", 0);
            }
            
            String jeIds = jeIdBuilder.toString();
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                jeIds = jeIds.substring(0, jeIds.length() - 1);
            }
            
            paramJobj.put("createdon", createdon);
            paramJobj.put("updatedon", updatedon);
            paramJobj.put("clearedChecksAmountinAcc", clearedChecksAmountinAcc);
            paramJobj.put("clearedDepositsAmountinAcc", clearedDepositsAmountinAcc);
            paramJobj.put("unclearedDepositsAmountinAcc", -unclearedDepositsAmountinAcc);
            paramJobj.put("unclearedChecksAmountinAcc", unclearedChecksAmountinAcc);
            paramJobj.put("depositsreconciled", ldetails.length());
            paramJobj.put("paymentsreconciled", rdetails.length());
            paramJobj.put("clearingamount", clearingAmount-clearedAmount);

            resultObj = accBankReconciliationObj.saveBankReconciliationDraft(paramJobj);
            List<BankReconcilationDraft> list = resultObj.getEntityList();

            if (list != null && list.size() > 0 && !StringUtil.isNullOrEmpty(jeIds)) {

//                BankReconcilationDraft draft = list.get(0);
                String draftId = list.get(0).getID();
                paramJobj.put("draftId", draftId);
                paramJobj.put("jeIds", jeIds);

                accBankReconciliationObj.updateDraftedJournalEntries(paramJobj);

                jobj.put("success", true);
                jobj.put("valid", true);
                jobj.put("draftId", draftId);
            } else {
                jobj.put("success", false);
            }
            issuccess = true;
            msg = "Draft saved successfully.";
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getBankReconcilationDrafts(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            DateFormat df= authHandler.getOnlyDateFormat();
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);

            KwlReturnObject resultObj = null;
            
            resultObj = accBankReconciliationObj.getBankReconcilationDrafts(paramJobj);
            List<BankReconcilationDraft> list = resultObj.getEntityList();
            JSONArray dataArr = new JSONArray();
            JSONObject dataObj = null;
            for (BankReconcilationDraft draft : list) {
                dataObj = new JSONObject();
                dataObj.put("id", draft.getID());
                dataObj.put("accountname", draft.getAccount().getAccountName());
                dataObj.put("accountid", draft.getAccount().getID());
                dataObj.put("currencysymbol", draft.getAccount().getCurrency() !=null ? draft.getAccount().getCurrency().getSymbol() : "");
                dataObj.put("description", draft.getDescription());

                dataObj.put("fromdate", draft.getFromdate() !=null ? df.format(draft.getFromdate()):"");
                dataObj.put("todate", draft.getTodate() !=null ? df.format(draft.getTodate()):"");
                dataObj.put("newstatementbalance", draft.getNewstatementbalance());
                dataObj.put("createdby", draft.getCreatedby().getFullName());
                dataObj.put("createdon", draft.getCreatedon());
                dataObj.put("updatedon", draft.getUpdatedon());
                dataObj.put("bankBookBalanceinAcc", draft.getBankBookBalanceinAcc());
                dataObj.put("bankStmtBalanceinAcc", draft.getBankStmtBalanceinAcc());
                dataObj.put("clearedChecksAmountinAcc", draft.getClearedChecksAmountinAcc());
                dataObj.put("clearedDepositsAmountinAcc", draft.getClearedDepositsAmountinAcc());
                dataObj.put("unclearedChecksAmountinAcc", draft.getUnclearedChecksAmountinAcc());
                dataObj.put("unclearedDepositsAmountinAcc", draft.getUnclearedDepositsAmountinAcc());
                dataObj.put("paymentsreconciled", draft.getPaymentsReconciled());
                dataObj.put("depositsreconciled", draft.getDepositsReconciled());
                dataObj.put("clearingamount", draft.getClearingAmount());
                
                paramJobj.put("draftId",draft.getID());
                resultObj = accBankReconciliationObj.getDraftedJournalEntries(paramJobj);
                if(resultObj !=null && resultObj.getEntityList() !=null && resultObj.getEntityList().size() > 0){
                    dataObj.put("jeIds", resultObj.getEntityList().get(0));
                }
                
                dataArr.put(dataObj);
            }
            jobj.put("data", dataArr);
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView deleteBankReconcilationDrafts(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);

            issuccess = deleteBankReconciliationDraft(paramJobj);
            if (issuccess) {
                msg = "Draft deleted successfully.";
            }
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public boolean deleteBankReconciliationDraft(JSONObject paramJobj) throws ServiceException {
        boolean issuccess = false;
        try {
            KwlReturnObject resultObj = null;
            
            resultObj = accBankReconciliationObj.getBankReconcilationDrafts(paramJobj);
            List<BankReconcilationDraft> list = resultObj.getEntityList();
            
            for (BankReconcilationDraft draft : list) {
                paramJobj.put("oldDraftId", draft.getID());
                accBankReconciliationObj.updateDraftedJournalEntries(paramJobj);
            }
            
            resultObj = accBankReconciliationObj.deleteBankReconciliationDraft(paramJobj);
            if (resultObj.getRecordTotalCount() > 0) {
                issuccess = true;
//                msg = "Draft deleted successfully.";
            }
            paramJobj.remove("oldDraftId");
            
        } catch (Exception e) {
            throw ServiceException.FAILURE("getBankReconcilationDrafts : " + e.getMessage(), e);
        }
        return issuccess;
    }

    public ModelAndView getBankReconciliation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("accountid", request.getParameter("accid"));
            KwlReturnObject result = accBankReconciliationObj.getBankReconciliation(requestParams);
            List list = result.getEntityList();

            JSONArray jArr = getBankReconciliationJson(request, list);
            jobj.put("data", jArr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getBankReconciliationJson(HttpServletRequest request, List list) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                BankReconciliation entry = (BankReconciliation) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", entry.getID());
                obj.put("startdate", df.format(entry.getStartDate()));
                obj.put("enddate", df.format(entry.getEndDate()));
                obj.put("clearanceDate", entry.getClearanceDate() == null ? "" : df.format(entry.getClearanceDate()));
                obj.put("clearingbalance", entry.getClearingAmount());
                obj.put("accountname", entry.getAccount().getName());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getBankReconciliationJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView deleteBankReconciliation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BRecnl_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteBankReconciliation(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.br.uncon", null, RequestContextUtils.getLocale(request));   //"Bank Reconciliation has been unreconciled successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteBankReconciliation(HttpServletRequest request) throws ServiceException, SessionExpiredException, ParseException {
        try {
            JSONArray LjArr = new JSONArray(request.getParameter("d_details"));
            JSONArray RjArr = new JSONArray(request.getParameter("c_details"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            Date unconcileDate = authHandler.getDateFormatter(request).parse(request.getParameter("unconcileDate"));
            String accountname="", journalEntryNO="";
            for (int i = 0; i < LjArr.length(); i++) {
                JSONObject jobj = LjArr.getJSONObject(i);
                String brid = StringUtil.DecodeText(jobj.optString("id"));
                try {
                    accountname += StringUtil.DecodeText( jobj.optString("d_accountname")) + ", ";
                    journalEntryNO += StringUtil.DecodeText( jobj.optString("d_entryno")) + ", ";
                    accBankReconciliationObj.permenantDeleteBankReconciliationDetail(brid,  companyid);
                } catch (ServiceException ex) {
                    throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
                }
            }
            for (int i = 0; i < RjArr.length(); i++) {
                JSONObject jobj = RjArr.getJSONObject(i);
                String brid = StringUtil.DecodeText(jobj.optString("id"));
                try {
                    accountname += StringUtil.DecodeText( jobj.optString("c_accountname")) + ", "; 
                    journalEntryNO += StringUtil.DecodeText( jobj.optString("c_entryno")) + ", ";
                    accBankReconciliationObj.permenantDeleteBankReconciliationDetail(brid, companyid);
                } catch (ServiceException ex) {
                    throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
                }
            }
            /**
             * Removing last comma from journalEntryNO
             */
            if (!StringUtil.isNullOrEmpty(journalEntryNO)) {
                journalEntryNO = journalEntryNO.substring(0, journalEntryNO.length() - 2);
            }
          auditTrailObj.insertAuditLog(AuditAction.BANK_RECONCILIATION_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has Unreconciled "+(java.util.Arrays.toString(journalEntryNO.split(","))), request, companyid); 
        } catch (JSONException ex) {
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("deleteBankReconciliation : " + ex.getMessage(), ex);
        }
    }
    
    public ModelAndView getAttachDocuments(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        Boolean success = false;
        JSONObject jobj = new JSONObject();              
        JSONArray jSONArray=new JSONArray();
        JSONObject finalJSONObject=new JSONObject();
        int count=0;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String invoiceID=request.getParameter("id");
            String start=request.getParameter("start");
            String limit=request.getParameter("limit");
            HashMap<String,Object> hashMap=new HashMap<String, Object>();
            hashMap.put("reconcileID",invoiceID);
            hashMap.put("companyid",companyid);
            hashMap.put("start",start);
            hashMap.put("limit",limit);
            KwlReturnObject object=accBankReconciliationObj.getBankReconcilationDocuments(hashMap);
            
            Iterator iterator=object.getEntityList().iterator();
            while (iterator.hasNext()) {
                Object[] obj=(Object[])iterator.next();
                JSONObject jSONObject=new JSONObject();                                
                jSONObject.put("docname", obj[0]);
                jSONObject.put("docid", obj[2]);
                jSONObject.put("doctypeid", obj[1]);
                jSONArray.put(jSONObject);                                                
                count++;
            }
            
            finalJSONObject.put("count", count);
            finalJSONObject.put("data",jSONArray);
            success = true;            
        } catch (ServiceException ex) {
            success = false;
            msg = ex.getMessage();            
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            success = false;            
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } finally {
            try {                
                jobj.put("data", finalJSONObject);
                jobj.put("valid", success);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public ModelAndView deleteDocument(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        Boolean success = false;
        JSONObject jobj = new JSONObject();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String docID=request.getParameter("docid");
            if(!StringUtil.isNullOrEmpty(docID)){
                KwlReturnObject object=accBankReconciliationObj.deleteBankReconcilationDocument(docID);
                success = true;
                msg = object.getMsg();
                txnManager.commit(status);
            }                        
        } catch (ServiceException ex) {
            success = false;
            msg = ex.getMessage();
            txnManager.rollback(status);
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            success = false;
            txnManager.rollback(status);
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } finally {
            try {
                
                JSONObject jobj1 = new JSONObject();
                jobj1.put("msg", msg);
                jobj1.put("success", success);
                jobj.append("data", jobj1);
                jobj.put("valid", true);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
     public ModelAndView attachDocuments(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        String docID = "";
        Boolean success = false;
        JSONObject jobj = new JSONObject();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            docID=uploadDoc(request);
            success = true;
            msg = messageSource.getMessage("acc.invoiceList.bt.fileUploadedSuccess", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (ServiceException ex) {
            success = false;
            msg = ex.getMessage();
            txnManager.rollback(status);
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            success = false;
            txnManager.rollback(status);
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } finally {
            try {
                jobj.put("success", success);
                jobj.put("msg", msg);
                jobj.put("docID", docID);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

    public String uploadDoc(HttpServletRequest request)
            throws ServiceException {
        try {
            String result = "";
            Boolean fileflag = false;
            String fileName = "";
            boolean isUploaded;
            String Ext;
            String filename ="";
            final String sep = StorageHandler.GetFileSeparator();
            DiskFileUpload fu = new DiskFileUpload();
            java.util.List fileItems = null;
            FileItem fi = null;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            try {
                fileItems = fu.parseRequest(request);
            } catch (FileUploadException e) {
                throw ServiceException.FAILURE("ProfileHandler.updateProfile", e);
            }
            java.util.HashMap arrParam = new java.util.HashMap();
            for (java.util.Iterator k = fileItems.iterator(); k.hasNext();) {
                fi = (FileItem) k.next();
                arrParam.put(fi.getFieldName(), fi.getString());
                if (!fi.isFormField()) {
                    if (fi.getSize() != 0) {
                        fileflag = true;
                        fileName = new String(fi.getName().getBytes());
                    }
                }
            }

            if (fileflag) {
                try {
                    String storePath = StorageHandler.GetDocStorePath();
                    File destDir = new File(storePath);
                    if (!destDir.exists()) {
                        destDir.mkdirs();
                    }
                    int doccount = 0;
                    fu = new DiskFileUpload();
                    fu.setSizeMax(-1);
                    fu.setSizeThreshold(4096);
                    fu.setRepositoryPath(storePath);
                    for (Iterator i = fileItems.iterator(); i.hasNext();) {
                        fi = (FileItem) i.next();
                        if (!fi.isFormField() && fi.getSize() != 0 && doccount < 3) {
                            Ext = "";
                            doccount++;//ie 8 fourth file gets attached				
                            filename = UUID.randomUUID().toString();
                            try {
                                fileName = new String(fi.getName().getBytes(), "UTF8");
                                if (fileName.contains(".")) {
                                    Ext = fileName.substring(fileName.lastIndexOf("."));
                                }
                                if (fi.getSize() != 0) {
                                    isUploaded = true;
                                    File uploadFile = new File(storePath + sep
                                            + filename + Ext);
                                    fi.write(uploadFile);

                                    BankReconcilationDocuments document=new BankReconcilationDocuments();
                                    document.setDocID(filename);
                                    document.setDocName(fileName);
                                    document.setDocType("");
                                    
                                    BankReconcilationDocumentCompMap brDocumentMap=new BankReconcilationDocumentCompMap();
                                    brDocumentMap.setDocument(document);                                    
                                    KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                                    Company company = (Company) cmp.getEntityList().get(0);
                                    brDocumentMap.setCompany(company);
                                    brDocumentMap.setReconcileID(UUID.randomUUID().toString());                                    
                                    
                                    HashMap<String,Object> hashMap=new HashMap<String, Object>();
                                    hashMap.put("BankReconcilationDocuments",document);
                                    hashMap.put("BankReconcilationDocumentMapping", brDocumentMap);
                                    accBankReconciliationObj.saveBankReconcilationDocuments(hashMap);
                                } else {
                                    isUploaded = false;
                                }
                            } catch (Exception e) {
                                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, e);
                                throw ServiceException.FAILURE("accInvoiceControllerCMN.uploadDoc", e);
                            }
                        }
                    }                    
                } catch (Exception ex) {
                    Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
                    throw ServiceException.FAILURE("accInvoiceControllerCMN.uploadDoc", ex);
                }
            }           
        return filename;
        } catch (Exception ex) {
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accInvoiceControllerCMN.uploadDoc", ex);
        }
       
    }
    
     public ModelAndView getBankReconciliationHistory(HttpServletRequest request, HttpServletResponse response) {
      JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String accid=request.getParameter("accid");
            String companyid=sessionHandlerImpl.getCompanyid(request);
            Date startDate = (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) ? authHandler.getDateOnlyFormat().parse(request.getParameter("stdate")) : null;
            Date endDate = (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) ? authHandler.getDateOnlyFormat().parse(request.getParameter("enddate")) : null;
            int action = !StringUtil.isNullOrEmpty(request.getParameter("action")) ? Integer.parseInt(request.getParameter("action")) : 0;  //0-All, 1-Reconcile, 2-Un-reconcile
            Boolean actionType =  (action==0) ? null : ((action==1) ? false : true);
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put(Constants.companyKey, companyid);
            //params.put(Constants.Acc_Accountid, accid);
            params.put("action", actionType);
            params.put("startdate", startDate);
            params.put("enddate", endDate);
            KwlReturnObject result = accBankReconciliationObj.getBankReconciliationHistory(params);
            List list = result.getEntityList();

            JSONArray jArr = getBankReconciliationHistoryJson(request, list);
            jobj.put("data", jArr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getBankReconciliationHistoryJson(HttpServletRequest request, List list) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            Iterator itr = list.iterator();
            while(itr.hasNext()) {
                BankReconciliation entry = (BankReconciliation) itr.next();
                if(StringUtil.isNullOrEmpty(entry.getReconcilenumber())){
                    continue;       //To Show only Reconciled Records in Reconciled Details Report we need to check whether this record having reconcile no.or not. We can directly reconcile the records from entry form. We are not maintaining its reconcile no.
                }
                JSONObject obj = new JSONObject();
                boolean withoutinventory = true;
                obj.put("billid", entry.getID());
                obj.put("action", entry.isDeleted()?"Un-Reconcile":"Reconcile");
                obj.put("clearanceDate", entry.getClearanceDate() == null ? "" : df.format(entry.getClearanceDate()));
                obj.put("checkcount", entry.getCheckCount());
                obj.put("depositecount", entry.getDepositeCount());
                obj.put("clearingamount", entry.getClearingAmount());
                obj.put("createdby", entry.getCreatedby()==null?"":StringUtil.getFullName(entry.getCreatedby()));
                obj.put("brstartdate", entry.getStartDate() == null ? "" : df.format(entry.getStartDate()));
                obj.put("brenddate", entry.getEndDate() == null ? "" : df.format(entry.getEndDate()));
                obj.put("accountid", entry.getAccount().getID());
                obj.put("accountname", entry.getAccount()!=null ? entry.getAccount().getName() : "");
                obj.put("currencyid", entry.getAccount()!=null ? entry.getAccount().getCurrency().getCurrencyID() : "");
                obj.put("currencysymbol", entry.getAccount()!=null ? entry.getAccount().getCurrency().getSymbol() : "");
                obj.put("reconcileno", entry.getReconcilenumber());
                obj.put("basecurrency", entry.getCompany().getCurrency().getSymbol());
                
                double clearedChecksAmountinAcc = entry.getClearedChecksAmountinAcc();
                double clearedDepositsAmountinAcc = entry.getClearedDepositsAmountinAcc();
                double unclearedChecksAmountinAcc = entry.getUnclearedChecksAmountinAcc();
                double unclearedDepositsAmountinAcc = entry.getUnclearedDepositsAmountinAcc();
                double bankBookBalanceinAcc = entry.getBankBookBalanceinAcc();
                double bankStmtBalanceinAcc = entry.getBankStmtBalanceinAcc();
                obj.put("clearedDepositsAmountinAcc", clearedDepositsAmountinAcc);
                obj.put("clearedChecksAmountinAcc", clearedChecksAmountinAcc);
                obj.put("unclearedDepositsAmountinAcc", unclearedDepositsAmountinAcc);
                obj.put("unclearedChecksAmountinAcc", unclearedChecksAmountinAcc);
                obj.put("bankBookBalanceinAcc", bankBookBalanceinAcc);
                obj.put("bankStmtBalanceinAcc", bankStmtBalanceinAcc);
                
                //Get Attachment Details
                String companyid = entry.getCompany().getCompanyID();
                String invoiceID = entry.getID();
                String start = request.getParameter("start");
                String limit = request.getParameter("limit");
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("reconcileID", invoiceID);
                hashMap.put("companyid", companyid);
                hashMap.put("start", start);
                hashMap.put("limit", limit);
                KwlReturnObject object = accBankReconciliationObj.getBankReconcilationDocuments(hashMap);
                if(object!=null && object.getRecordTotalCount()>0){
                    obj.put("attachdoc", object.getRecordTotalCount());                    
                }
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getBankReconciliationHistoryJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView importBankReconciliation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));
            String companyid = sessionHandlerImpl.getCompanyid(request);

            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", companyid);
            boolean updateExistingRecordFlag = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("updateExistingRecordFlag"))) {
                updateExistingRecordFlag = Boolean.FALSE.parseBoolean(request.getParameter("updateExistingRecordFlag"));
            }
            String doAction = request.getParameter("do");
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("tzdiff", sessionHandlerImpl.getTimeZoneDifference(request));
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());
            requestParams.put("companyid", companyid);
            requestParams.put("moduleName", Constants.Acc_BankReconciliation_modulename);
            requestParams.put("moduleid", Constants.Bank_Reconciliation_ModuleId);
            requestParams.put("isActivateToDateforExchangeRates", extraPref.isActivateToDateforExchangeRates());//variable needs while fetching exchange rate
            requestParams.put("isCurrencyCode", extraPref.isCurrencyCode());
            if (updateExistingRecordFlag) {
                requestParams.put("allowDuplcateRecord", updateExistingRecordFlag);
            }

            if (doAction.compareToIgnoreCase("import") == 0 || doAction.compareToIgnoreCase("xlsImport") == 0) {

                requestParams.put("action", doAction);
                requestParams.put("updateExistingRecordFlag", updateExistingRecordFlag);
                System.out.println("A(( Import start : " + new Date());
                JSONObject datajobj = new JSONObject();
                JSONObject resjson = new JSONObject(request.getParameter("resjson").toString().replaceAll("\\n", "").trim());
                JSONArray resjsonJArray = resjson.getJSONArray("root");

                String filename = request.getParameter("filename");
                datajobj.put("filename", filename);

                String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
                File filepath = new File(destinationDirectory + "/" + filename);
                datajobj.put("FilePath", filepath);

                datajobj.put("resjson", resjsonJArray);

                String dateFormatId = request.getParameter("dateFormat");
                requestParams.put("dateFormat", dateFormatId);
                requestParams.put("importflag", Constants.importproductcsv);
                Date applyDate = authHandler.getDateOnlyFormatter(request).parse(authHandler.getDateOnlyFormatter(request).format(new Date()));
                requestParams.put("jobj", datajobj);
                requestParams.put("ApplyDate", applyDate);
                /*
                 * we are importing the actual file into the system
                 */
                System.out.println("A(( Import start : " + new Date());
                jobj = importHandler.importBankReconciliationCSV(requestParams);
                System.out.println("A(( Import end : " + new Date());
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                /*
                 * we are validating the file which we have to import in the
                 * system.
                 */
                System.out.println("A(( Validation start : " + new Date());
                jobj = importHandler.validateFileData(requestParams);
                System.out.println("A(( Validation end : " + new Date());
            } 
        } catch (Exception ex) {
            try {
                jobj.put("success", false);
                jobj.put("msg", "" + ex.getMessage());
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException jex) {
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    //ERM-734
    public ModelAndView getLastReconcileAmountAndDate(HttpServletRequest request, HttpServletResponse response) {
        JSONObject returnJobj = new JSONObject();
        boolean issuccess = false;
        double summationLastReconciledAmount=0;

        try {
            JSONObject requestJson = StringUtil.convertRequestToJsonObject(request);
            DateFormat userdateFormat = authHandler.getUserDateFormatterWithoutTimeZone(requestJson);
            String accid = requestJson.optString("accountid");
            String companyid = requestJson.optString(Constants.companyKey);

            KwlReturnObject bankresult = accBankReconciliationObj.getLastReconcileAmountAndDate(accid, companyid);
            if (bankresult != null && bankresult.getEntityList().size() > 0) {
                List<Object[]> banklist = bankresult.getEntityList();
                for (Object[] obj : banklist) {
                    if (obj[0] != null && obj.length > 0) {
                        returnJobj.put("lastReconciledDate", userdateFormat.format(obj[0]));
                    }
                    if (obj[1] != null && obj.length > 1) {
//                        summationLastReconciledAmount += Double.valueOf(obj[1].toString());
                        returnJobj.put("lastReconciledAmount", obj[1].toString());
                    }
                    issuccess = true;
                }
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                returnJobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", returnJobj.toString());
    } 
}