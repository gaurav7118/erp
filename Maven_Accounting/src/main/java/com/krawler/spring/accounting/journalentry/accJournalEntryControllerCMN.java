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
package com.krawler.spring.accounting.journalentry;


import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.Tax1099Category;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.*;
import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.handlers.StorageHandler;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.CommonFnController;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.mail.MessagingException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.ss.usermodel.*;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */ 
public class accJournalEntryControllerCMN extends MultiActionController implements JournalEntryConstants, MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accJournalEntryDAO accJournalEntryobj;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private accVendorDAO accVendorDAOobj;
    private accTaxDAO accTaxObj;
    private accDebitNoteDAO accDebitNoteobj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private accAccountDAO accAccountDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private MessageSource messageSource;
    private fieldDataManager fieldDataManagercntrl;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    public ImportHandler importHandler;
    private ImportDAO importDao;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private authHandlerDAO authHandlerDAOObj;
    private accProductDAO accProductObj;
    private AccJournalEntryModuleService journalEntryModuleServiceobj;
    private  ImportJournalEntry importJournalEntry;
    private AccJournalEntryService accJournalEntryService;
    
    public AccJournalEntryService getAccJournalEntryService() {
        return accJournalEntryService;
    }

    public void setAccJournalEntryService(AccJournalEntryService accJournalEntryService) {
        this.accJournalEntryService = accJournalEntryService;
    }
    
    public AccJournalEntryModuleService getJournalEntryModuleServiceobj() {
        return journalEntryModuleServiceobj;
    }

    public void setJournalEntryModuleServiceobj(AccJournalEntryModuleService journalEntryModuleServiceobj) {
        this.journalEntryModuleServiceobj = journalEntryModuleServiceobj;
    }
    
    @Override
    public void setMessageSource(MessageSource ms) {
            this.messageSource=ms;
    }
     
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }
    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }
    public void setaccVendorDAO(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    } 
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }
    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    public void setaccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAO) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAO;
    }
    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setImportJournalEntry(ImportJournalEntry importJournalEntry) {
        this.importJournalEntry = importJournalEntry;
    }
    //Need to uncomment for report optimization related code
//    public ModelAndView saveCurrencyExchange(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj=new JSONObject();
//        String msg="";
//        boolean issuccess = false;
//
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("Currency_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//
//        TransactionStatus status = txnManager.getTransaction(def);
//        try {
//            boolean dateexist = false;
//            dateexist = saveCurrencyExchange(request);
//            jobj.put( CurrencyContants.DATEEXIST,dateexist);
//            txnManager.commit(status);
//            issuccess = true;
//            msg = messageSource.getMessage("acc.curex.update", null, RequestContextUtils.getLocale(request));   //"Currency Exchange Rate has been updated successfully";
//        } catch (SessionExpiredException ex) {
//            txnManager.rollback(status);
//            msg = ex.getMessage();
//            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (Exception ex) {
//            txnManager.rollback(status);
//            msg = ""+ex.getMessage();
//            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
//        } finally{
//            try {
//                jobj.put( CurrencyContants.SUCCESS,issuccess);
//                jobj.put( CurrencyContants.MSG,msg);
//            } catch (JSONException ex) {
//                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return new ModelAndView(CurrencyContants.JSONVIEW,CurrencyContants.MODEL, jobj.toString());
//    }
//
//    public boolean saveCurrencyExchange(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
//        try {
//            boolean updateRate = (request.getParameter("changerate")==null?false:Boolean.parseBoolean(request.getParameter("changerate")));
//            JSONArray jArr = new JSONArray(request.getParameter(CurrencyContants.DATA));
//            String companyid = sessionHandlerImpl.getCompanyid(request);            
//            for (int i = 0; i < jArr.length(); i++) {
//                JSONObject jobj = jArr.getJSONObject(i);
//                //Need to send currencyid from client side
//                String currencyid = jobj.getString("tocurrency");
//                Date appDate = null;
//                if(StringUtil.isNullOrEmpty(jobj.getString(CurrencyContants.APPLYDATE))){
//                    throw new AccountingException(messageSource.getMessage("acc.curex.excp1", null, RequestContextUtils.getLocale(request)));
//                }
//                else{
//                    appDate = authHandler.getDateOnlyFormatter(request).parse(StringUtil.DecodeText(jobj.optString(CurrencyContants.APPLYDATE)));
//                }
//                Calendar applyDate = Calendar.getInstance();
//                applyDate.setTime(appDate);
//                String erid = StringUtil.DecodeText(jobj.optString(CurrencyContants.ID));
//                Map<String, Object> filterParams = new HashMap<String, Object>();
//                filterParams.put( CurrencyContants.ERID,erid);
//                filterParams.put( CurrencyContants.APPLYDATE,appDate);
//                filterParams.put( COMPANYID,companyid);
//                KwlReturnObject result = accCurrencyDAOobj.getExchangeRateDetails(filterParams, false);
//                List list = result.getEntityList();
//                Map<String, Object> erdMap = new HashMap<String, Object>();
//                double newRate = Double.parseDouble(StringUtil.DecodeText(jobj.optString(CurrencyContants.EXCHANGERATE)));
//                if(StringUtil.isNullOrEmpty(jobj.getString(CurrencyContants.EXCHANGERATE))){
//                    throw new AccountingException(messageSource.getMessage("acc.curex.excp2", null, RequestContextUtils.getLocale(request)));
//                }
//                else{
//                    erdMap.put(CurrencyContants.EXCHANGERATE, newRate);
//                }
//                ExchangeRateDetails erd;
//                KwlReturnObject erdresult;
//                if (list.size() > 0 && !updateRate) {
//                    return true;
//                } else {
//                    updateExistingJEsAmount(currencyid, companyid, appDate, erid, newRate);
//                    if (list.size() <= 0) {
//                        //throw new AccountingException("Can not change edit the Exchange Rate.");
//                        erdMap.put(CurrencyContants.APPLYDATE,applyDate.getTime());
//                        erdMap.put(CurrencyContants.ERID,erid);
//                        erdMap.put(COMPANYID,companyid);
//                        erdresult = accCurrencyDAOobj.addExchangeRateDetails(erdMap);
//                    } else {
//                        erd = (ExchangeRateDetails) list.get(0);
//                        erdMap.put(CurrencyContants.ERDID,erd.getID());
//                        erdresult = accCurrencyDAOobj.updateExchangeRateDetails(erdMap);
//                    }
//                    erd = (ExchangeRateDetails) erdresult.getEntityList().get(0);
//                }
//            }
//        } catch (UnsupportedEncodingException ex) {
//            throw ServiceException.FAILURE("Can't extract the records. <br>Encoding not supported", ex);
//        } catch (JSONException ex) {
//            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
//            throw ServiceException.FAILURE("saveCurrencyExchange : " + ex.getMessage(), ex);
//        } catch (ParseException ex) {
//            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
//            throw ServiceException.FAILURE("saveCurrencyExchange : " + ex.getMessage(), ex);
//        }  catch (Exception ex) {
//            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
//            throw ServiceException.FAILURE("saveCurrencyExchange : " + ex.getMessage(), ex);
//        }
//        return false;
//    }
//    
//    public void updateExistingJEsAmount(String currencyid, String companyid, Date fromDate, String erid, double newRate) throws ServiceException {   
//        try{
////            Date startDate = authHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
//            KwlReturnObject result = accJournalEntryobj.getOldCurrencyRateAndDate(companyid, fromDate, erid);
//            List li = result.getEntityList();
//            if (!li.isEmpty()) {
//                if(li.get(0)!=null) {
//                    ExchangeRateDetails erd = (ExchangeRateDetails) li.get(0);
//                    Date toDate = (Date) li.get(1);
//                    double oldRate = erd.getExchangeRate();
//                    
//                    HashMap<String, Object> requestParamsForThread = new HashMap<String, Object>();
//                    requestParamsForThread.put("currencyid", currencyid);
////                    requestParamsForThread.put("gcurrencyid", gcurrencyid);
//                    requestParamsForThread.put("companyid", companyid);
//                    requestParamsForThread.put("startDate", fromDate);
//                    requestParamsForThread.put("endDate", toDate);
//                    requestParamsForThread.put("oldRate", oldRate);
//                    requestParamsForThread.put("newRate", newRate);
//                    currencyRateChangeHandler.add(requestParamsForThread);
//                    if(!currencyRateChangeHandler.isWorking){
//                        Thread t = new Thread(currencyRateChangeHandler);
//                        t.start();
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
//            throw ServiceException.FAILURE("getOldCurrencyRateAndDate : " + ex.getMessage(), ex);
//        }
//    }
    
    public ModelAndView eliminateJournalEntries(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JEC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            eliminateJournalEntries(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.je1.eliminate", null, RequestContextUtils.getLocale(request));   //"Journal Entry(s) has been eliminated successfully.";
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, "accJournalEntryControllerCMN.eliminateJournalEntries", ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, "accJournalEntryControllerCMN.eliminateJournalEntries", ex);
        } finally{
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, "accJournalEntryControllerCMN.eliminateJournalEntries", ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public void eliminateJournalEntries(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
//        ArrayList params = new ArrayList();
        try {
            JSONArray jArr = new JSONArray(request.getParameter(DATA));        
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("journalentryid"))) {
                    String jeid = StringUtil.DecodeText(jobj.optString("journalentryid"));
                    accJournalEntryobj.eliminateJournalEntry(jeid);
                }
            }
        } catch (JSONException ex) {
            throw new AccountingException("Cannot extract data from client");
        }
    }

    public ModelAndView deleteJournalEntries(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        StringBuilder linkedtransaction=new StringBuilder();
        boolean iswarning=false;
        boolean issuccess = false;
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("servletContext", this.getServletContext());
            deleteJournalEntries(paramJobj, linkedtransaction);
            if (!linkedtransaction.toString().equals("")) {
                iswarning = true;
                msg = linkedtransaction.toString();
            } else {
                msg = messageSource.getMessage("acc.je1.del", null, RequestContextUtils.getLocale(request));   //"Journal Entry has been deleted successfully";
            }
            issuccess = true;
        } catch (JSONException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, "accJournalEntryControllerCMN.deleteJournalEntries", ex);
        } catch (AccountingException ex) {
            msg = ex.getMessage();
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, "accJournalEntryControllerCMN.deleteJournalEntries", ex);
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, "accJournalEntryControllerCMN.deleteJournalEntries", ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
                jobj.put(WARNING,iswarning);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, "accJournalEntryControllerCMN.deleteJournalEntries", ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteJournalEntries(JSONObject paramJobj,StringBuilder linkedTransaction) throws SessionExpiredException, AccountingException, ServiceException, JSONException {
        JSONArray jArr = new JSONArray(paramJobj.optString(Constants.data, "[{}]"));
        String companyid = paramJobj.optString(Constants.companyKey);
        String flg = paramJobj.optString("jeFlag", null);
        boolean flag = StringUtil.isNullOrEmpty(flg) ? false : Boolean.parseBoolean(flg);
        int countryid = 0;
        KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) companyObj.getEntityList().get(0);
        if (company.getCountry() != null && company.getCountry().getID() != null) {
            countryid = Integer.parseInt(company.getCountry().getID());
        }
        StringBuilder exceptEntryno = new StringBuilder();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            try {
                accJournalEntryService.deleteJournalEntry(paramJobj, jobj, countryid, companyid, flag);
            } catch (AccountingException ex) {
                if (jArr.length() == 1) {          //When user select single JE for deletion which is linked to other Transaction(s); 
                    linkedTransaction.append(ex.getMessage()); 
                } else {
                    exceptEntryno.append(jobj.optString("entryno","")).append(","); // Append the journal entry number(s) which are not to be deleted when multiple JE selected for deletion.
                }
            }
        }
        if (!exceptEntryno.toString().equals("")) {  
            linkedTransaction.append(messageSource.getMessage("acc.alert.deletejournalentry", new Object[]{exceptEntryno.substring(0, exceptEntryno.length() - 1)}, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
        }
    }
    public ModelAndView getTax1099JE(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            DateFormat df = authHandler.getDateOnlyFormat(request);
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(CURDATE, df.parse(request.getParameter(CURDATE)));
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(GCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put(SS, request.getParameter(SS));
            requestParams.put( DATEFORMAT,df);
            JSONArray DataJArr = getTax1099JEDetails(requestParams);
            String start = request.getParameter(START);
            String limit = request.getParameter(LIMIT);
            JSONArray jArr1 = new JSONArray();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                int st = Integer.parseInt(start);
                int ed = Math.min(DataJArr.length(), st + Integer.parseInt(limit));
                for (int i = st; i < ed; i++) {
                    jArr1.put(DataJArr.getJSONObject(i));
                }
            }
            jobj.put("data", jArr1);
            jobj.put("count", DataJArr.length());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accJournalEntryController.getJournalEntryDetails : "+ex.getMessage();
        } finally {
            try {
                jobj.put( SUCCESS,issuccess);
                jobj.put( MSG,msg);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getTax1099JEDetails(Map<String, Object> request) throws ServiceException {
        JSONArray jArr=new JSONArray();
        try {
            String ss=request.get(SS)==null?null:(String)request.get(SS);
            KwlReturnObject venresult = accVendorDAOobj.get1099EligibleVendor((String)request.get(COMPANYID),ss);
//            Iterator itr = venresult.getEntityList().iterator();
//            while(itr.hasNext()) {
//                Vendor vendor = (Vendor) itr.next();
           List<Vendor> vendorList = venresult.getEntityList();
            if(vendorList!=null && !vendorList.isEmpty()){
                for(Vendor vendor:vendorList){
                    KwlReturnObject jedresult = accJournalEntryobj.getTax1099AccJE((String)request.get(COMPANYID),(Date)request.get(CURDATE),vendor.getID());
//                    Iterator jeditr = jedresult.getEntityList().iterator();
//                    while(jeditr.hasNext()) {//                    getTax1099AccCategory
//                        Object[] row = (Object[]) jeditr.next();
                    List<Object[]> jedList = jedresult.getEntityList();
                    if(jedList!=null && !jedList.isEmpty()){
                        for(Object[] row:jedList){
                            JournalEntryDetail jed = (JournalEntryDetail) row[0];
                            request.put(ACCOUNTID, jed.getAccount().getID());
                            request.put(AMOUNT, (Double)row[1]);
                            JSONObject obj = new JSONObject();
                            KwlReturnObject catresult = accTaxObj.getTax1099AccCategory(request);
    //                        Iterator catitr = catresult.getEntityList().iterator();
    //                        if(catitr.hasNext()) {
    //                            Tax1099Category taxCat = (Tax1099Category) catitr.next();
                            List<Tax1099Category> taxCatList = catresult.getEntityList();
                            if(taxCatList!=null && !taxCatList.isEmpty()){
                                for(Tax1099Category taxCat:taxCatList){
                                    obj.put(CATEGORYID, taxCat.getID());
                                    obj.put(CATEGORYNAME, taxCat.getCategory() );
                                    obj.put(ABOVETHRESHOLD, (Double)row[1]>taxCat.getThresholdValue() );
                                }
                            }
                            obj.put(SRNO, jed.getSrno());
                            obj.put(ACCOUNTID, jed.getAccount().getID());
                            obj.put(ACCOUNTNAME, jed.getAccount().getName());
                            obj.put(PERSONID, vendor.getID());
                            obj.put(PERSONNAME, vendor.getName());
                            obj.put(AMOUNT, (Double)row[1]);

                            jArr.put(obj);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getJournalEntryDetails : "+ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView saveJournalEntry(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="",jeid = "", JENumber = "", creditNoteDebitNoteId = "";
        boolean issuccess = false;
        KwlReturnObject resultForJe = null;
        KwlReturnObject result;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            boolean isedit = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("jeedit"))) {
                isedit = Boolean.parseBoolean(request.getParameter("jeedit"));
            }
            String sequenceformatCN=request.getParameter("sequenceformatCN");
            String numberCN=request.getParameter("entrynoCN");
            String sequenceformatDN = request.getParameter("sequenceformatDN");
            String numberDN = request.getParameter("entrynoDN");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            Date entryDate= df.parse(request.getParameter("entrydate"));
            List li = saveJournalEntry(request);
            String entryNumber = request.getParameter("entryno");
            String sequenceformat=request.getParameter("sequenceformat");
            boolean jeedit = request.getParameter("jeedit") != null ? Boolean.parseBoolean(request.getParameter("jeedit")):false;
            issuccess = true;
            if(li.get(2) != null) {
                JENumber = (String) li.get(2);
            }
            txnManager.commit(status);
            KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), (String)li.get(0));
            JournalEntry JE = (JournalEntry) jeresult.getEntityList().get(0); 
            String jeId=JE.getID();
            TransactionStatus AutoNoStatus = null;
            try {
                synchronized (this) {
                    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
                    def1.setName("AutoNum_Tx");
                    def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    AutoNoStatus = txnManager.getTransaction(def1);
                    if (sequenceformat.equals("NA") && !jeedit) {
                        resultForJe = accJournalEntryobj.getJECount(entryNumber, companyid);
                        while (resultForJe.getRecordTotalCount() > 0) {
                            entryNumber = entryNumber + "-1";
                            resultForJe = accJournalEntryobj.getJECount(entryNumber, companyid);
                        }
                        JENumber = accJournalEntryobj.updateJEEntryNumberForNA(jeId, entryNumber);
                    }
                    if (!jeedit && (!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA"))) {  //Post New JE with auto generated Entry No.
//                        status = txnManager.getTransaction(def);
                        Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
//                        entryNumber = journalEntryModuleServiceobj.updateJEEntryNumberForNewJE(jeDataMap, JE, companyid, sequenceformat, JE.getPendingapproval());
                        KwlReturnObject returnObj = journalEntryModuleServiceobj.updateJEEntryNumberForNewJE(jeDataMap, JE, companyid, sequenceformat, JE.getPendingapproval());
                        if(returnObj.isSuccessFlag() && returnObj.getRecordTotalCount()>0){
                            entryNumber = (String) returnObj.getEntityList().get(0);
                        }
                        JENumber = entryNumber;

                    }
                    if(!jeedit && !StringUtil.isNullOrEmpty(JENumber)) {
                        auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User "+sessionHandlerImpl.getUserFullName(request) + " added new Journal Entry transaction: "+JENumber, request, jeId);
                    }
                    
                    boolean isnewcninjeEdit = (request.getAttribute("isnewcninjeEdit") != null)?(boolean)request.getAttribute("isnewcninjeEdit"):false;
                    boolean seqformat_oldflag = StringUtil.getBoolean(request.getAttribute("seqformat_oldflag") + "");
                    String nextCNAutoNo = "";
                    String creditNoteNumBer = (String) li.get(6);
                    String cnID = (String) li.get(3);

                    if (sequenceformatCN.equals("NA") && StringUtil.isNullOrEmpty(creditNoteNumBer)) {     // create new case checks duplicate
                        result = accCreditNoteDAOobj.getCNFromNoteNo(numberCN, companyid);
                        while (result.getRecordTotalCount() > 0) {
                            numberCN = numberCN + "-1";
                            result = accCreditNoteDAOobj.getCNFromNoteNo(numberCN, companyid);
                        }
                        creditNoteNumBer = accCreditNoteDAOobj.updateCNEntryNumberForNA(cnID, numberCN);

                    }
                    if (StringUtil.isNullOrEmpty(creditNoteNumBer) && !isnewcninjeEdit) {
                        if (request.getAttribute("autoGenerateCNDN") == null) {

                            if (!StringUtil.isNullOrEmpty(sequenceformatCN) && !sequenceformatCN.equals("NA")) {
                                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                if (seqformat_oldflag) {
                                    nextCNAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_CREDITNOTE, sequenceformatCN);
                                    seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextCNAutoNo);
                                } else {
                                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_CREDITNOTE, sequenceformatCN, seqformat_oldflag, entryDate);
                                }
                                seqNumberMap.put(Constants.DOCUMENTID, cnID);
                                seqNumberMap.put(Constants.companyKey, companyid);
                                seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformatCN);
                                creditNoteNumBer = accCreditNoteDAOobj.updateCreditNoteEntryNumber(seqNumberMap);
                            }
                        } else {
                            String[] format = accJournalEntryobj.getNextAutoNumber_modified(companyid, "autocreditmemo");
                            KwlReturnObject curSeqObj = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), format[2]);
                            SequenceFormat sequenceFormat = (SequenceFormat) curSeqObj.getEntityList().get(0);
                            nextCNAutoNo = format[0];
                            int nextAutoNoInt = Integer.parseInt(format[1]);
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextCNAutoNo);
                            seqNumberMap.put(Constants.SEQNUMBER, nextAutoNoInt);
                            seqNumberMap.put(Constants.DOCUMENTID, cnID);
                            seqNumberMap.put(Constants.companyKey, companyid);
                            seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformatCN);
                            creditNoteNumBer = accCreditNoteDAOobj.updateCreditNoteEntryNumber(seqNumberMap);
                        }
                    }
                    if(!isedit && !StringUtil.isNullOrEmpty(creditNoteNumBer)) {
                        auditTrailObj.insertAuditLog(AuditAction.CREDIT_NOTE_CREATED, "User "+sessionHandlerImpl.getUserFullName(request) +" has recorded new Credit Note "+creditNoteNumBer, request, cnID);
                        creditNoteDebitNoteId += "<br>CN No: <b>"+creditNoteNumBer+"</b>";
                    }

                    String dbId = (String) li.get(4);
                    String nextDNAutoNo = "";
                    String debitNoteNumBer = (String) li.get(5);
//                    synchronized (this) {
                        if (sequenceformatDN.equals("NA") && StringUtil.isNullOrEmpty(debitNoteNumBer)) {     // create new case checks duplicate
                            result = accDebitNoteobj.getDNFromNoteNo(numberDN, companyid);
                            while (result.getRecordTotalCount() > 0) {
                                numberDN = numberDN + "-1";
                                result = accDebitNoteobj.getDNFromNoteNo(numberDN, companyid);
                            }
                            debitNoteNumBer = accDebitNoteobj.updateDNEntryNumberForNA(dbId, numberDN);

                        }
                        if (StringUtil.isNullOrEmpty(debitNoteNumBer) && !isnewcninjeEdit) {
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            if (request.getAttribute("autoGenerateCNDN") == null) {

                                if (!StringUtil.isNullOrEmpty(sequenceformatDN) && !sequenceformatDN.equals("NA")) {
                                    if (seqformat_oldflag) {
                                        nextCNAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_DEBITNOTE, sequenceformatDN);
                                        seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextCNAutoNo);
                                    } else {
                                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_DEBITNOTE, sequenceformatDN, seqformat_oldflag, entryDate);
                                    }
                                    seqNumberMap.put(Constants.DOCUMENTID, dbId);
                                    seqNumberMap.put(Constants.companyKey, companyid);
                                    seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformatDN);
                                    debitNoteNumBer = accDebitNoteobj.updateDeditEntryNumber(seqNumberMap);
                                }
                            } else {
                                String[] format = accJournalEntryobj.getNextAutoNumber_modified(companyid, "autocreditmemo");
                                KwlReturnObject curSeqObj = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), format[2]);
                                SequenceFormat sequenceFormat = (SequenceFormat) curSeqObj.getEntityList().get(0);
                                nextDNAutoNo = format[0];
                                int nextAutoNoInt = Integer.parseInt(format[1]);
                                seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextDNAutoNo);
                                seqNumberMap.put(Constants.SEQNUMBER, nextAutoNoInt);
                                seqNumberMap.put(Constants.DOCUMENTID, dbId);
                                seqNumberMap.put(Constants.companyKey, companyid);
                                seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformatDN);
                                debitNoteNumBer = accDebitNoteobj.updateDeditEntryNumber(seqNumberMap);
                            }
                    }
                    if(!isedit && !StringUtil.isNullOrEmpty(debitNoteNumBer)) {
                        auditTrailObj.insertAuditLog("80", "User " + sessionHandlerImpl.getUserFullName(request) + " has recorded a new Debit Note " + debitNoteNumBer, request, dbId);
                        creditNoteDebitNoteId += "<br>DN No: <b>"+debitNoteNumBer+"</b>";
                    }
                    if(jeedit && !StringUtil.isNullOrEmpty(JENumber)) {//In case of edit JE
                        String withCNDN = "";
                        CreditNote cn = null;
                        DebitNote dn = null;
                        List cnList = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnID).getEntityList();
                        List dnList =  accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dbId).getEntityList();
                        if(cnList != null && cnList.size()>0 && dnList != null && dnList.size()>0) {
                            cn = (CreditNote) cnList.get(0);
                            dn = (DebitNote) dnList.get(0);
                            withCNDN += " for CN : "+creditNoteNumBer+", DN : "+debitNoteNumBer;
                            creditNoteDebitNoteId += "<br>CN No: <b>" + creditNoteNumBer + "</b><br>DN No: <b>" + debitNoteNumBer + "</b>";
                        }
                        auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User "+sessionHandlerImpl.getUserFullName(request) + " updated Journal Entry transaction: "+JENumber+withCNDN, request, jeId);
                    }
//                    }
                    txnManager.commit(AutoNoStatus);
                }
            } catch (Exception ex) {
                if (AutoNoStatus != null) {
                    txnManager.rollback(AutoNoStatus);
                }
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            } 
            msg = messageSource.getMessage("acc.je1.save", null, RequestContextUtils.getLocale(request))+ "<br/>JE No: <b>"+JENumber+"</b>";   //"Journal Entry has been saved successfully. /n JE No: ";
            msg +=creditNoteDebitNoteId;        //success msg with CN & DN number
            //boolean jeedit = request.getParameter("jeedit")!=null;
            jeid = li.get(0)!=null?(String) li.get(0):"";
            if(jeedit) {
                String oldjeid = (String) li.get(1);
                status = txnManager.getTransaction(def);
                deleteJEArray(oldjeid,companyid);
                txnManager.commit(status);
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
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
    
    public List saveJournalEntry(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        JournalEntry je = null;
        List ll = new ArrayList();
        CreditNote creditnote = null;
        KwlReturnObject result;
        HashSet jeDetails=null;
        try {
            String companyid =  sessionHandlerImpl.getCompanyid(request);
            String createdby = sessionHandlerImpl.getUserid(request);
            String currencyid = "";
            currencyid = (request.getParameter("currencyid")==null?sessionHandlerImpl.getCurrencyID(request):request.getParameter("currencyid"));
            String jeid = "";
            String oldjeid = "";
            int typeValue=1;// 1 - Normal JE Entry, 2 - Partly JE Entry, 3- Fund  Transfer 
            int partlyJeEntryWithCnDn=1;// 1 - for Partly JE Entry with CN DN 
            boolean jeedit = false;
            String jeId=request.getParameter("jeid");
            String sequenceformat=request.getParameter("sequenceformat");
            String entryNumber = request.getParameter("entryno");
            String sequenceformatCN=request.getParameter("sequenceformatCN");
            String numberCN=request.getParameter("entrynoCN");
            boolean seqformat_oldflagCN=Boolean.parseBoolean(request.getParameter("seqformat_oldflagCN"));
            String sequenceformatDN = request.getParameter("sequenceformatDN");
            String numberDN = request.getParameter("entrynoDN");
            boolean seqformat_oldflagDN = Boolean.parseBoolean(request.getParameter("seqformat_oldflagDN"));
            
            if(sequenceformat.equals("NA")){ //if(StringUtil.isNullOrEmpty(entryNumber)){
               if(StringUtil.isNullOrEmpty(entryNumber) )
                throw new AccountingException(messageSource.getMessage("acc.je1.excp2", null, RequestContextUtils.getLocale(request)));
            }
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("jeedit"))) {
                jeedit = Boolean.parseBoolean(request.getParameter("jeedit"));
                
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("typevalue"))) {
                typeValue = Integer.parseInt(request.getParameter("typevalue"));
                
            }
            if (sequenceformat.equals("NA") && jeedit) {  // 
                KwlReturnObject resultForJe = accJournalEntryobj.getJECountForEdit(entryNumber, companyid,jeId);
                int nocount = resultForJe.getRecordTotalCount();
                if (nocount > 0) {
                    throw new AccountingException("Journal entry number '" + entryNumber + "' already exists.");
                }
            }
//            if(sequenceformat.equals("NA") && !jeedit ) { 
//                 result = accJournalEntryobj.getJECount(entryNumber, companyid);
//                int nocount = result.getRecordTotalCount();
//                if (nocount > 0) {
//                    throw new AccountingException("Journal entry number '" + entryNumber + "' already exists.");
//                }
//            }
          
            if ( sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_GENERAL_LEDGER_ModuleId, entryNumber, companyid);
                if (!list.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) list.get(0);
                    String formatName = (String) list.get(1);
                    if (!isvalidEntryNumber) {
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
            
        
            double externalCurrencyRate= StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String nextJEAutoNo = "";
            Map<String,Object> jeDataMap = AccountingManager.getGlobalParams(request);
//            synchronized (this) {
//                if (!jeedit) {//For new case only
//                    if (!sequenceformat.equals("NA") && !StringUtil.isNullOrEmpty(sequenceformat)) { 
//                        boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
//                        String nextAutoNoInt = "";
//                        if (seqformat_oldflag) {
//                            nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY, sequenceformat);
//                        } else {
//                            String[] nextAutoNoTemp = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, sequenceformat, seqformat_oldflag);
//                            nextJEAutoNo = nextAutoNoTemp[0];
//                            nextAutoNoInt = nextAutoNoTemp[1];
//                            jeDataMap.put(Constants.SEQFORMAT, sequenceformat);
//                            jeDataMap.put(Constants.SEQNUMBER, nextAutoNoInt);
//                            entryNumber = nextJEAutoNo;
//                        }
//
//                    }
////                else {
////                    //To do - SEQ FOEMAT need     to test when this is called.
////                    nextJEAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_JOURNALENTRY);
////                }
//                }
//            }
            
            String costCenterId = request.getParameter(CCConstants.REQ_costcenter);
            
            jeDataMap.put("companyid", companyid);
            jeDataMap.put("createdby", createdby);
            jeDataMap.put("entrydate", df.parse(request.getParameter("entrydate")));
            jeDataMap.put("memo", request.getParameter("memo"));
            if (jeedit) {
                jeDataMap.put("entrynumber", entryNumber);
            } else {
                jeDataMap.put("entrynumber", "");
            }
            jeDataMap.put("autogenerated", sequenceformat.equals("NA") ?  false : true); //nextJEAutoNo.equals(entryNumber));
            jeDataMap.put("currencyid", request.getParameter("currencyid"));
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("typevalue", typeValue);
            jeDataMap.put("partlyJeEntryWithCnDn", partlyJeEntryWithCnDn);
            if(jeedit) {
                oldjeid = request.getParameter("jeid");
                try{
                    //Delete entry from optimized table
                    KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), oldjeid);
                    JournalEntry oldjournalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                    if (oldjournalEntry != null) {
                        if (oldjournalEntry.getSeqformat() != null) {
                            jeDataMap.put(Constants.SEQFORMAT, oldjournalEntry.getSeqformat().getID());
                        }
                        jeDataMap.put(Constants.SEQNUMBER, oldjournalEntry.getSeqnumber());
                        jeDataMap.put(Constants.DATEPREFIX, oldjournalEntry.getDatePreffixValue());
                        jeDataMap.put(Constants.DATEAFTERPREFIX, oldjournalEntry.getDateAfterPreffixValue());
                        jeDataMap.put(Constants.DATESUFFIX, oldjournalEntry.getDateSuffixValue());
                        if (!sequenceformat.equals("NA")) {
                            jeDataMap.put("entrynumber", oldjournalEntry.getEntryNumber());
                        }
                        jeDataMap.put("autogenerated", oldjournalEntry.isAutoGenerated());
                    }

                    result = accCreditNoteDAOobj.getCNFromJE(oldjeid, companyid);
                    List list = result.getEntityList();

                    if (!list.isEmpty()) {
                        CreditNote oldCreditNote = (CreditNote) list.get(0);
                        request.setAttribute("cnseqnumber", oldCreditNote != null ? oldCreditNote.getSeqnumber() : 0);
                        if (!StringUtil.isNullObject(oldCreditNote)) {
                            accCreditNoteDAOobj.deletePartyJournalCN(oldCreditNote.getID(), companyid);
                        }
                    } else {
                        request.setAttribute("isnewcninjeEdit", false);
                    }

                    result = accDebitNoteobj.getDNFromJE(oldjeid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        DebitNote oldDebitNote = (DebitNote) list.get(0);
                        request.setAttribute("dnseqnumber", oldDebitNote != null ? oldDebitNote.getSeqnumber() : 0);
                        if (!StringUtil.isNullObject(oldDebitNote)) {
                            accDebitNoteobj.deletePartyJournalDN(oldDebitNote.getID(), companyid);
                        }
                    } else {
                        request.setAttribute("isnewdninjeEdit", true);
                    }
                    accJournalEntryobj.deleteOnEditAccountJEs_optimized(oldjeid);
                } catch(ServiceException ex){
                    throw new AccountingException("Journal entry details are already in use.");
                }
                
//                jeDataMap.put("jeid", jeid);
                jeDataMap.put("jeisedit", true);                
                
            }
            if(!StringUtil.isNullOrEmpty(costCenterId)){
                jeDataMap.put(CCConstants.JSON_costcenterid, costCenterId);
            }
//            jeDataMap.put("jedetails", jeDetails);
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
            je = (JournalEntry) jeresult.getEntityList().get(0);
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            KwlReturnObject jedresult = accJournalEntryobj.getJEDset(jArr, companyid,je);
            jeDetails = (HashSet) jedresult.getEntityList().get(0);
            je.setDetails(jeDetails);
            accJournalEntryobj.saveJournalEntryDetailsSet(jeDetails);
            jeid = je.getID();
            ll.add(jeid);
            ll.add(oldjeid);
            ll.add(entryNumber);
            
            /*
             * Make custom field entry at line level
             */
            //ERP-11528 
            double amount=0.0;
            for (Iterator<JournalEntryDetail> jEDIterator = jeDetails.iterator(); jEDIterator.hasNext();) {
                JournalEntryDetail jed = jEDIterator.next();
                int srno = 0;
                amount=jed.getAmount();
                if (jed.getSrno() != 0) {
                    srno = jed.getSrno();
                    JSONObject jobj = jArr.getJSONObject(--srno);
                    if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
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
                            JSONObject jedjson = new JSONObject();
                            jedjson.put("accjedetailcustomdata", jed.getID());
                            jedjson.put("jedid", jed.getID());
                            jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                        }
                    }
                }
            }

            /*
             *  Make custom field entry
             */
            String customfield = request.getParameter("customfield");
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", jeid);
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    Map<String,Object> customjeDataMap = new HashMap<String,Object>();
                    customjeDataMap.put("accjecustomdataref", jeid);
                    customjeDataMap.put("jeid", jeid);
                    jeresult = accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                }
            }
            
            //Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);
            
            boolean partyJournalFlag = true;
            //Save CN against Vendor
            
            JSONObject jobj = new JSONObject();
            request.setAttribute("externalcurrencyrate", externalCurrencyRate);
            request.setAttribute("otherwise", true);
            request.setAttribute("sequenceformat", sequenceformatCN!=null?sequenceformatCN:"");
            request.setAttribute("creationdate", request.getParameter("entrydate"));
            request.setAttribute("number", numberCN);
            request.setAttribute("currencyid", currencyid);
            request.setAttribute("seqformat_oldflag", seqformat_oldflagCN);
            request.setAttribute("memo",request.getParameter("memo"));            
//            request.setAttribute("cntype", 4);
            
         //TO do -Check its working or not   
            /*
            KwlReturnObject resultPayable = accountingHandlerDAOobj.getObject(Group.class.getName(), Group.ACCOUNTS_PAYABLE);
            Group groupPaybles = (Group) resultPayable.getEntityList().get(0);
            KwlReturnObject resultBillsPayable = accountingHandlerDAOobj.getObject(Group.class.getName(), Group.BILLS_PAYABLE);
            Group groupBillsPaybles = (Group) resultPayable.getEntityList().get(0);
            KwlReturnObject resultReceivable = accountingHandlerDAOobj.getObject(Group.class.getName(), Group.ACCOUNTS_RECEIVABLE);
            Group groupReceivable = (Group) resultReceivable.getEntityList().get(0);
            Set<String> groupSetPaybles = new HashSet<String>();
            Set<String> groupSetBillsPaybles = new HashSet<String>();
            Set<String> groupSetReceivables = new HashSet<String>();
            groupSetPaybles.add(Group.ACCOUNTS_PAYABLE);
            groupSetBillsPaybles.add(Group.BILLS_PAYABLE);
            groupSetReceivables.add(Group.ACCOUNTS_RECEIVABLE);
            if (groupPaybles != null) {
                Iterator itr = groupPaybles.getChildren().iterator();
                while (itr.hasNext()) {
                    Group childGroup = (Group) itr.next();
                    groupSetPaybles.add(childGroup.getID());
                }
            }
            if (groupBillsPaybles != null) {
                Iterator itr = groupBillsPaybles.getChildren().iterator();
                while (itr.hasNext()) {
                    Group childGroup = (Group) itr.next();
                    groupSetBillsPaybles.add(childGroup.getID());
                }
            }
            if (groupReceivable != null) {
                Iterator itr = groupReceivable.getChildren().iterator();
                while (itr.hasNext()) {
                    Group childGroup = (Group) itr.next();
                    groupSetReceivables.add(childGroup.getID());
                }
            }*/
            
            KwlReturnObject resultPayable = accountingHandlerDAOobj.getObject(Group.class.getName(), Group.ACCOUNTS_PAYABLE);
            Group groupPaybles = (Group) resultPayable.getEntityList().get(0);
            KwlReturnObject resultBillsPayable = accountingHandlerDAOobj.getObject(Group.class.getName(), Group.BILLS_PAYABLE);
            Group groupBillsPaybles = (Group) resultPayable.getEntityList().get(0);
            KwlReturnObject resultReceivable = accountingHandlerDAOobj.getObject(Group.class.getName(), Group.ACCOUNTS_RECEIVABLE);
            Group groupReceivable = (Group) resultReceivable.getEntityList().get(0);
            Set<String> groupSetPaybles = new HashSet<String>();
            Set<String> groupSetBillsPaybles = new HashSet<String>();
            Set<String> groupSetReceivables = new HashSet<String>();
            groupSetPaybles.add(Group.ACCOUNTS_PAYABLE);
            groupSetBillsPaybles.add(Group.BILLS_PAYABLE);
            groupSetReceivables.add(Group.ACCOUNTS_RECEIVABLE);
            if (groupPaybles != null) {
                Iterator itr = groupPaybles.getChildren().iterator();
                while (itr.hasNext()) {
                    Group childGroup = (Group) itr.next();
                    groupSetPaybles.add(childGroup.getID());
            }           
            }
            if (groupBillsPaybles != null) {
                Iterator itr = groupBillsPaybles.getChildren().iterator();
                while (itr.hasNext()) {
                    Group childGroup = (Group) itr.next();
                    groupSetBillsPaybles.add(childGroup.getID());
                }
            }
            if (groupReceivable != null) {
                        Iterator itr = groupReceivable.getChildren().iterator();
                        while (itr.hasNext()) {
                            Group childGroup = (Group) itr.next();
                            groupSetReceivables.add(childGroup.getID());
                        }
            }
            for (Object obj : jeDetails) {
                JournalEntryDetail journalEntryDetail = (JournalEntryDetail) obj;
                if (!journalEntryDetail.isDebit()) {
                    request.setAttribute("accid", journalEntryDetail.getAccount().getID());
                    request.setAttribute("customerVendorId", journalEntryDetail.getCustomerVendorId());
                    request.setAttribute("amount", journalEntryDetail.getAmount());
                    request.setAttribute("customerentry", journalEntryDetail.getID());
                  //  Group group = journalEntryDetail.getAccount().getGroup();
                   // String groupId = group.getID();
                //    Set<String> groupSet = new HashSet<String>();
                    int personType = journalEntryDetail.getAccountpersontype();
                    if (personType == 2) {//Vendor
                        request.setAttribute("cntype", 4);
                    } else if (personType == 1) {//Customer
                        request.setAttribute("cntype", 2);
                    }
                }
            }
            

            CreditNote creditNote = saveCreditNote(request, jobj, je);
            ll.add(creditNote.getID());
            boolean isedit = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("jeedit"))) {
                isedit = Boolean.parseBoolean(request.getParameter("jeedit"));
            }
            boolean isnewcninjeEdit = request.getAttribute("isnewcninjeEdit") != null;
            boolean seqformat_oldflag = StringUtil.getBoolean(request.getAttribute("seqformat_oldflag") + "");
            String nextCNAutoNo = "";
            String creditNoteNumBer = "";
//           synchronized (this){
//                 if (sequenceformatCN.equals("NA") && !isedit) {     // create new case checks duplicate
//                        result = accCreditNoteDAOobj.getCNFromNoteNo(numberCN, companyid);
//                        while (result.getRecordTotalCount() > 0) {
//                            numberCN = numberCN + "-1";
//                            result = accCreditNoteDAOobj.getCNFromNoteNo(numberCN, companyid);
//                        }
//                        creditNoteNumBer = accCreditNoteDAOobj.updateCNEntryNumberForNA(creditNote.getID(), numberCN);
//
//                    }
//                if (!isedit && !isnewcninjeEdit) {
//                    if (request.getAttribute("autoGenerateCNDN") == null) {
//
//                        if (!StringUtil.isNullOrEmpty(sequenceformatCN) && !sequenceformatCN.equals("NA")) {
//                            if (seqformat_oldflag) {
//                                nextCNAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_CREDITNOTE, sequenceformatCN);
//                            } else {
//                                String[] nextAutoNoTemp = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_CREDITNOTE, sequenceformatCN, seqformat_oldflag);
//                                nextCNAutoNo = nextAutoNoTemp[0];
//                                int nextAutoNoInt = Integer.parseInt(nextAutoNoTemp[1]);
//                                creditNoteNumBer = accCreditNoteDAOobj.updateCreditNoteEntryNumber(sequenceformatCN, nextCNAutoNo, nextAutoNoInt, creditNote.getID(), companyid);
//                            }
//                        }
//                    } else {
//                        String[] format = accJournalEntryobj.getNextAutoNumber_modified(companyid, "autocreditmemo");
//                        KwlReturnObject curSeqObj = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), format[2]);
//                        SequenceFormat sequenceFormat = (SequenceFormat) curSeqObj.getEntityList().get(0);
//                        nextCNAutoNo = format[0];
//                        int nextAutoNoInt = Integer.parseInt(format[1]);
//                        creditNoteNumBer = accCreditNoteDAOobj.updateCreditNoteEntryNumber(sequenceformatCN, nextCNAutoNo, nextAutoNoInt, creditNote.getID(), companyid);
//                    }
//                }
//           }
            request.setAttribute("sequenceformat", sequenceformatDN != null ? sequenceformatDN : "");
            request.setAttribute("number", numberDN);
            request.setAttribute("seqformat_oldflag", seqformat_oldflagDN);
            //Save DN against Vendor
            for (Object obj : jeDetails) {
                JournalEntryDetail journalEntryDetail = (JournalEntryDetail) obj;
                if (journalEntryDetail.isDebit() && journalEntryDetail.getAccount() != null && journalEntryDetail.getAccount().getGroup() != null) {
                    request.setAttribute("accid", journalEntryDetail.getAccount().getID());
                    request.setAttribute("customerVendorId", journalEntryDetail.getCustomerVendorId());
                    request.setAttribute("amount", journalEntryDetail.getAmount());
                    request.setAttribute("vendorentry", journalEntryDetail.getID());
                 //   Group group = journalEntryDetail.getAccount().getGroup();
                    int personType = journalEntryDetail.getAccountpersontype();
                   // String groupId = group.getID();
                    if (personType == 2) {//Vendor
                        request.setAttribute("cntype", 2);
                    } else if (personType == 1) {//Customer
                        request.setAttribute("cntype", 4);
                    }
                }
            }
            DebitNote debitNote = saveDebitNote(request, je);
            ll.add(debitNote.getID());
            ll.add(debitNote.getDebitNoteNumber());
            ll.add(creditNote.getCreditNoteNumber());
            String nextDNAutoNo="";
            String debitNoteNumBer = "";
//            synchronized (this){
//                 if (sequenceformatDN.equals("NA") && !isedit) {     // create new case checks duplicate
//                        result = accDebitNoteobj.getDNFromNoteNo(numberDN, companyid);
//                        while (result.getRecordTotalCount() > 0) {
//                            numberDN = numberDN + "-1";
//                            result = accDebitNoteobj.getDNFromNoteNo(numberDN, companyid);
//                        }
//                        debitNoteNumBer = accDebitNoteobj.updateDNEntryNumberForNA(debitNote.getID(), numberDN);
//
//                    }
//                if (!isedit && !isnewcninjeEdit) {
//                    if (request.getAttribute("autoGenerateCNDN") == null) {
//
//                        if (!StringUtil.isNullOrEmpty(sequenceformatDN) && !sequenceformatDN.equals("NA")) {
//                            if (seqformat_oldflag) {
//                                nextCNAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_DEBITNOTE, sequenceformatDN);
//                            } else {
//                                String[] nextAutoNoTemp = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_DEBITNOTE, sequenceformatDN, seqformat_oldflag);
//                                nextDNAutoNo = nextAutoNoTemp[0];
//                                int nextAutoNoInt = Integer.parseInt(nextAutoNoTemp[1]);
//                               debitNoteNumBer = accDebitNoteobj.updateDeditEntryNumber(sequenceformatDN, nextDNAutoNo, nextAutoNoInt, debitNote.getID(), companyid);
//                            }
//                        }
//                    } else {
//                        String[] format = accJournalEntryobj.getNextAutoNumber_modified(companyid, "autocreditmemo");
//                        KwlReturnObject curSeqObj = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), format[2]);
//                        SequenceFormat sequenceFormat = (SequenceFormat) curSeqObj.getEntityList().get(0);
//                        nextDNAutoNo = format[0];
//                        int nextAutoNoInt = Integer.parseInt(format[1]);
//                         debitNoteNumBer = accDebitNoteobj.updateDeditEntryNumber(sequenceformatDN, nextDNAutoNo, nextAutoNoInt, debitNote.getID(), companyid);
//                    }
//                }
//            }
            
//            auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User "+sessionHandlerImpl.getUserFullName(request) + " added new journal entry transaction: "+je.getEntryNumber(), request, je.getID());
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveJournalEntry : "+ex.getMessage(), ex);
//            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveJournalEntry : "+ex.getMessage(), ex);
        }
        return ll;
    }

    
    public ModelAndView saveOldCreditAndDebitNotes(HttpServletRequest request, HttpServletResponse response){
        JSONArray jSONArray=new JSONArray();
        JSONObject jobj=new JSONObject();
        String msg="",id = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
           jSONArray= saveOldCreditAndDebitNotes(request);
            issuccess = true;
             msg = messageSource.getMessage("acc.field.CreditNoteandDebitNotehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));   //"Journal Entry has been saved successfully";
            txnManager.commit(status);
//        } catch (SessionExpiredException ex) {
//            txnManager.rollback(status);
//            msg = ex.getMessage();
//            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("data", jSONArray);
                jobj.put("msg", msg);
                jobj.put("id", id);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray saveOldCreditAndDebitNotes(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONArray jArr;
        JSONArray jsonSavedDateils=new JSONArray();
        HashSet jeDetails=null;
        KwlReturnObject result = null;
        JSONArray dataJArr = new JSONArray();
        String companyid =  null;
        try {
            companyid =  sessionHandlerImpl.getCompanyid(request); 
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            KwlReturnObject resultPayable = accountingHandlerDAOobj.getObject(Group.class.getName(), Group.ACCOUNTS_PAYABLE);
            Group groupPaybles = (Group) resultPayable.getEntityList().get(0);
            KwlReturnObject resultBillsPayable = accountingHandlerDAOobj.getObject(Group.class.getName(), Group.BILLS_PAYABLE);
            Group groupBillsPaybles = (Group) resultPayable.getEntityList().get(0);
            KwlReturnObject resultReceivable = accountingHandlerDAOobj.getObject(Group.class.getName(), Group.ACCOUNTS_RECEIVABLE);
            Group groupReceivable = (Group) resultReceivable.getEntityList().get(0);
            Set<String> groupSetPaybles = new HashSet<String>();
            Set<String> groupSetBillsPaybles = new HashSet<String>();
            Set<String> groupSetReceivables = new HashSet<String>();
            groupSetPaybles.add(Group.ACCOUNTS_PAYABLE);
            groupSetBillsPaybles.add(Group.BILLS_PAYABLE);
            groupSetReceivables.add(Group.ACCOUNTS_RECEIVABLE);
            if (groupPaybles != null) {
                Iterator itr = groupPaybles.getChildren().iterator();
                while (itr.hasNext()) {
                    Group childGroup = (Group) itr.next();
                    groupSetPaybles.add(childGroup.getID());
                }
            }
            if (groupBillsPaybles != null) {
                Iterator itr = groupBillsPaybles.getChildren().iterator();
                while (itr.hasNext()) {
                    Group childGroup = (Group) itr.next();
                    groupSetBillsPaybles.add(childGroup.getID());
                }
            }
            if (groupReceivable != null) {
                        Iterator itr = groupReceivable.getChildren().iterator();
                        while (itr.hasNext()) {
                            Group childGroup = (Group) itr.next();
                            groupSetReceivables.add(childGroup.getID());
                        }
            }
            HashMap<String, Object> requestParams=new HashMap<String, Object>();
            requestParams.put("cndnPendingFlag", true);
            requestParams.put("typeValue", 2);
            requestParams.put("companyid", companyid);
            result = accJournalEntryobj.getJournalEntry(requestParams);
            List list=result.getEntityList();
            CreditNote creditNote;
            Iterator itr = list.iterator();
            while(itr.hasNext()) {
               JournalEntry journalEntry= (JournalEntry)itr.next();
                HashMap<String, Object> requestParamsDetails = new HashMap<String, Object>();
            requestParamsDetails.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParamsDetails.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            requestParamsDetails.put("dateformat", authHandler.getDateOnlyFormat(request));
            requestParamsDetails.put("journalentryid", request.getParameter("journalentryid"));
             
            HashMap<String, Object> jeRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("journalEntry.ID");
            filter_params.add(journalEntry.getID());
            order_by.add("debit");
            order_type.add("desc");
            jeRequestParams.put("filter_names", filter_names);
            jeRequestParams.put("filter_params", filter_params);
            jeRequestParams.put("order_by", order_by);
            jeRequestParams.put("order_type", order_type);
            KwlReturnObject jedresult = accJournalEntryobj.getJournalEntryDetails(jeRequestParams);
            Iterator itrIterator = jedresult.getEntityList().iterator();

            while(itrIterator.hasNext()) {
                JournalEntryDetail entry = (JournalEntryDetail) itrIterator.next();
                currencyid=entry.getJournalEntry().getCurrency()==null?currency.getCurrencyID(): entry.getJournalEntry().getCurrency().getCurrencyID();
                JSONObject obj = new JSONObject();
                obj.put("srno", entry.getSrno());
                obj.put("accountid", entry.getAccount().getID());
                String accname = StringUtil.isNullOrEmpty(entry.getAccount().getAcccode())?entry.getAccount().getName():"["+entry.getAccount().getAcccode()+"] "+entry.getAccount().getName();
                obj.put("accountname", accname);
                obj.put("currencysymbol",entry.getJournalEntry().getCurrency()==null?currency.getSymbol(): entry.getJournalEntry().getCurrency().getSymbol());
                obj.put("description",entry.getDescription());
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParamsDetails, entry.getAmount(), currencyid, entry.getJournalEntry().getEntryDate(),entry.getJournalEntry().getExternalCurrencyRate());
                if(entry.isDebit()==true) {
                    obj.put("debit", "Debit");
//                    obj.put("d_amount", CompanyHandler.getCurrencyToBaseAmount(session,request,entry.getAmount(),currencyid,entry.getJournalEntry().getEntryDate()));
                    obj.put("d_amount", bAmt.getEntityList().get(0));
                } else {
                    obj.put("debit", "Credit");
//                    obj.put("c_amount",CompanyHandler.getCurrencyToBaseAmount(session,request,entry.getAmount(),currencyid,entry.getJournalEntry().getEntryDate()));
                    obj.put("c_amount", bAmt.getEntityList().get(0));
                }
                dataJArr.put(obj);
            }

            jedresult = accJournalEntryobj.getJEDsetCNDN(dataJArr, companyid);
            jeDetails = (HashSet) jedresult.getEntityList().get(0);
            
  
            
            boolean partyJournalFlag = true;
            //Save CN against Vendor
             double externalCurrencyRate= StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            String sequenceformatCN=request.getParameter("sequenceformatCN");
            String numberCN=request.getParameter("entrynoCN");
            boolean seqformat_oldflagCN=Boolean.parseBoolean(request.getParameter("seqformat_oldflagCN"));
            JSONObject jobj = new JSONObject();
             DateFormat df = authHandler.getDateOnlyFormat(request);
            request.setAttribute("externalcurrencyrate", externalCurrencyRate);
            request.setAttribute("otherwise", true);
            request.setAttribute("sequenceformat", sequenceformatCN!=null?sequenceformatCN:"");
            request.setAttribute("creationdate", request.getParameter("entrydate")!=null?request.getParameter("entrydate"):df.format(journalEntry.getEntryDate()));
            request.setAttribute("number", numberCN);
            request.setAttribute("currencyid", currencyid);
            request.setAttribute("seqformat_oldflag", seqformat_oldflagCN);
            request.setAttribute("memo",request.getParameter("memo"));
            request.setAttribute("autoGenerateCNDN",true);
            
            for(Object obj:jeDetails){
                JournalEntryDetail journalEntryDetail=(JournalEntryDetail)obj;
                if (!journalEntryDetail.isDebit()) {
                    request.setAttribute("accid", journalEntryDetail.getAccount().getID());
                    request.setAttribute("amount", journalEntryDetail.getAmount());
                    Group group = journalEntryDetail.getAccount().getGroup();
                    String groupId = group.getID();
                    Set<String> groupSet = new HashSet<String>();
                    if(groupSetPaybles.contains(groupId)||groupSetBillsPaybles.contains(groupId)){
                        request.setAttribute("cntype", 4);
                    }else if(groupSetReceivables.contains(groupId)){
                        request.setAttribute("cntype", 2);
                    }
                }
            }
            JSONObject jSONObject=new JSONObject();
            creditNote=saveCreditNote(request, jobj, journalEntry);
            
            jSONObject.put("JeId", journalEntry.getID());
            jSONObject.put("CreditNoteId", creditNote.getID());
            jSONObject.put("CreditNoteno", creditNote.getCreditNoteNumber());
            String sequenceformatDN=request.getParameter("sequenceformatDN");
            String numberDN=request.getParameter("entrynoDN");
            boolean seqformat_oldflagDN=Boolean.parseBoolean(request.getParameter("seqformat_oldflagDN"));
            request.setAttribute("sequenceformat", sequenceformatDN!=null?sequenceformatDN:"");
            request.setAttribute("number", numberDN);
            request.setAttribute("seqformat_oldflag", seqformat_oldflagDN);
            request.setAttribute("autoGenerateCNDN",true);
            //Save DN against Vendor
            for(Object obj:jeDetails){
                JournalEntryDetail journalEntryDetail=(JournalEntryDetail)obj;
                if(journalEntryDetail.isDebit()&&journalEntryDetail.getAccount()!=null&&journalEntryDetail.getAccount().getGroup()!=null){
                    request.setAttribute("accid", journalEntryDetail.getAccount().getID());
                    request.setAttribute("amount", journalEntryDetail.getAmount());
                    Group group = journalEntryDetail.getAccount().getGroup();
                    String groupId = group.getID();
                    if(groupSetPaybles.contains(groupId)||groupSetBillsPaybles.contains(groupId)){
                        request.setAttribute("cntype", 2);
                    }else if(groupSetReceivables.contains(groupId)){
                        request.setAttribute("cntype", 4);
                    }
                }
            }

           DebitNote debitNote= saveDebitNote(request,journalEntry);
            jSONObject.put("DebitNoteId", debitNote.getID());
            jSONObject.put("DebitNoteIdno", debitNote.getDebitNoteNumber());
          jsonSavedDateils.put(jSONObject);      
          }
//            
//            
            
            
        }  catch (JSONException ex) {
            Logger.getLogger(accJournalEntryControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accJournalEntryControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }  
        
        return jsonSavedDateils;
    }
     
    
  public void deleteJEArray(String oldjeid,String companyid) throws ServiceException, AccountingException, SessionExpiredException {
      try{      //delete old invoice
          JournalEntryDetail jed=null;
            if (!StringUtil.isNullOrEmpty(oldjeid)) {                
                KwlReturnObject   result = accJournalEntryobj.getJournalEntryDetail(oldjeid, companyid);
                List list =  result.getEntityList();
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
  
  public CreditNote saveCreditNote(HttpServletRequest request,JSONObject returnJobj, JournalEntry je) throws ServiceException, SessionExpiredException, AccountingException {
        CreditNote creditnote = null;
        KwlReturnObject result;
        try {            
            boolean reloadInventory = false;//Flag used to reload inventory on Client Side If CN type equals to "Return" or "Defective"
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            double externalCurrencyRate= request.getAttribute("externalcurrencyrate")!=null?(Double)request.getAttribute("externalcurrencyrate"):1.0;
            boolean otherwise = request.getAttribute("otherwise")!=null;
            boolean isnewcninjeEdit = request.getAttribute("isnewcninjeEdit")!=null;
            String sequenceformat=(String)request.getAttribute("sequenceformat");
            String entryNumber = (String)request.getAttribute("number");
            boolean isedit=false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("jeedit"))) {
                isedit = Boolean.parseBoolean(request.getParameter("jeedit"));                
            }
            HashMap<String,Object> GlobalParams = AccountingManager.getGlobalParams(request);

            Date creationDate = df.parse((String)request.getAttribute("creationdate"));
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
           
            currencyid = (request.getAttribute("currencyid")==null?kwlcurrency.getCurrencyID():(String)request.getAttribute("currencyid"));
            curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            if (isedit && !isnewcninjeEdit) {
                result = accCreditNoteDAOobj.getCNFromNoteNo(entryNumber, companyid);
                int count = result.getRecordTotalCount();
                if (count > 0) {
                    throw new AccountingException("Credit Note number '" + entryNumber + "' already exists.");
                }
            }
            HashMap<String,Object> credithm = new HashMap<String,Object>();
            boolean seqformat_oldflag = StringUtil.getBoolean(request.getAttribute("seqformat_oldflag")+"");
            String nextCNAutoNo = "";
            String nextAutoNoInt = "";
            
            String autoGenerateCNDN="";
            if (isedit && !isnewcninjeEdit) {
                if(StringUtil.isNullOrEmpty(sequenceformat) || sequenceformat.equalsIgnoreCase("NA")){
                    credithm.put("entrynumber", entryNumber);
                    credithm.put("autogenerated", false);
                } else {
                    int seqnumber= (Integer)request.getAttribute("cnseqnumber");
                    String datePrefix = request.getAttribute(Constants.DATEPREFIX)!=null?request.getAttribute(Constants.DATEPREFIX).toString():"";
                    String dateSuffix = request.getAttribute(Constants.DATESUFFIX)!=null?request.getAttribute(Constants.DATESUFFIX).toString():"";
                    credithm.put("entrynumber", entryNumber);
                    credithm.put(Constants.SEQFORMAT, sequenceformat);
                    credithm.put(Constants.SEQNUMBER, seqnumber);
                    credithm.put("autogenerated", true);
                    credithm.put(Constants.DATEPREFIX, datePrefix);
                    credithm.put(Constants.DATESUFFIX, dateSuffix);
                }               
            } else {
                if (request.getAttribute("autoGenerateCNDN") == null) {
//                    if (!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA")) {
//                        if (seqformat_oldflag) {
//                            nextCNAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_CREDITNOTE, sequenceformat);
//                        } else {
//                            String[] nextAutoNoTemp = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_CREDITNOTE, sequenceformat, seqformat_oldflag);
//                            nextCNAutoNo = nextAutoNoTemp[0];
//                            nextAutoNoInt = nextAutoNoTemp[1];
//                            credithm.put(Constants.SEQFORMAT, sequenceformat);
//                            credithm.put(Constants.SEQNUMBER, nextAutoNoInt);
//                        }
//                    }
                    credithm.put("entrynumber", "");
                    credithm.put("autogenerated", !sequenceformat.equals("NA") ? true : false);
                } else {
//                    String[] format = accJournalEntryobj.getNextAutoNumber_modified(companyid, "autocreditmemo");
                    credithm.put("entrynumber", "");
//                    credithm.put(Constants.SEQNUMBER, format[1]);
//                    KwlReturnObject curSeqObj = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), format[2]);
//                    SequenceFormat sequenceFormat = (SequenceFormat) curSeqObj.getEntityList().get(0);
//                    credithm.put(Constants.SEQFORMAT, format[2]);
                    credithm.put("autogenerated", true);
                }
            }       
            if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Credit_Note_ModuleId, entryNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request))+" <b>"+entryNumber+"</b> "+messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request))+" <b>"+formatName+"</b>. "+messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request))+" <b>"+formatName+"</b> "+messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
            credithm.put("memo", request.getAttribute("memo")!=null?(request.getAttribute("memo")+""):"");
            credithm.put("companyid", company.getCompanyID());
            credithm.put("currencyid", currencyid);

            Long seqNumber = null;
            result = accCreditNoteDAOobj.getCNSequenceNo(companyid, creationDate);
            List list = result.getEntityList();
            if (!list.isEmpty()) {
                seqNumber = (Long) list.get(0);
            }
            credithm.put("sequence", seqNumber.intValue());

            credithm.put("journalentryid", je.getID());                  
            List CNlist = new ArrayList();
            double cnamount = 0.0;
            double cnamountdue = 0.0;
            int cntype = (request.getAttribute("cntype")!=null&&!request.getAttribute("cntype").toString().equals(""))?(Integer)request.getAttribute("cntype"):1;
            if(cntype == 4) {//CN against vendor
                credithm.put("vendorid", request.getAttribute("customerVendorId").toString());
            } else {
                credithm.put("customerid", request.getAttribute("customerVendorId").toString());
            }
            
            if(request.getAttribute("accid")!=null){
               credithm.put("accountId", request.getAttribute("accid").toString());
            }
            
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            KwlReturnObject baseAmount = null;
            double cnamountinbase = 0.0;
            if(otherwise && (cntype == 2 || cntype == 4)) {//Credit note otherwise
                credithm.put("otherwise", true);
                credithm.put("openflag", true);
                cnamount = (Double)request.getAttribute("amount");
                cnamountdue = cnamount;
                credithm.put("cnamount", cnamount);
                /*
                * Saving the total amount in base currency 
                */
                baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, cnamount, currencyid, creationDate, externalCurrencyRate);
                cnamountinbase = (Double) baseAmount.getEntityList().get(0);
                cnamountinbase = authHandler.round(cnamountinbase, companyid);
                credithm.put("cnamountinbase", cnamountinbase);
                credithm.put("cnamountdue", cnamountdue);
                credithm.put("cntype", cntype);
                credithm.put("externalCurrencyRate", externalCurrencyRate);
                CNlist = saveCreditNoteRowsOW2(GlobalParams, request, company, currency, je, preferences, externalCurrencyRate);                
            }
            
            Double totalAmount = (Double) CNlist.get(0);
            Double discAccAmount = (Double) CNlist.get(1);
            HashSet<CreditNoteDetail> cndetails = (HashSet<CreditNoteDetail>) CNlist.get(2);
            reloadInventory = (Boolean) CNlist.get(4);
            returnJobj.put("reloadInventory", reloadInventory);  
            
            double termTotalAmount = 0;
            HashMap<String, Double> termAcc = new HashMap<String, Double>();
            String cnTerms = request.getParameter("invoicetermsmap");
            if(!StringUtil.isNullOrEmpty(cnTerms)) {
                JSONArray termsArr = new JSONArray(cnTerms);
                for(int cnt=0; cnt<termsArr.length(); cnt++) {
                    double termamount = termsArr.getJSONObject(cnt).getDouble("termamount");
                    termTotalAmount += termamount;
                    if(termAcc.containsKey(termsArr.getJSONObject(cnt).getString("glaccount"))) {
                        double tempAmount=termAcc.get(termsArr.getJSONObject(cnt).getString("glaccount"));
                        termAcc.put(termsArr.getJSONObject(cnt).getString("glaccount"), termamount+tempAmount);
                    }
                    else{    
                        termAcc.put(termsArr.getJSONObject(cnt).getString("glaccount"), termamount);
                    }
                }
            }
            totalAmount+=termTotalAmount;
            credithm.put("approvestatuslevel", 11);
            credithm.put("customerentry", request.getAttribute("customerentry")!=null?(request.getAttribute("customerentry")+""):"");
            credithm.put("creationDate", creationDate);
            result = accCreditNoteDAOobj.addCreditNote(credithm);
            creditnote = (CreditNote)result.getEntityList().get(0);

            credithm.put("cnid", creditnote.getID());
            Iterator itr = cndetails.iterator();
            while (itr.hasNext()) {
                CreditNoteDetail cnd = (CreditNoteDetail) itr.next();
                cnd.setCreditNote(creditnote);
            }
            credithm.put("cndetails", cndetails);

            result = accCreditNoteDAOobj.updateCreditNote(credithm);
            creditnote = (CreditNote)result.getEntityList().get(0);
            
//            //Add entry in optimized table
//            accJournalEntryobj.saveAccountJEs_optimized(jeid);
            
//            auditTrailObj.insertAuditLog(AuditAction.CREDIT_NOTE_CREATED, "User "+sessionHandlerImpl.getUserFullName(request) +" has recorded new Credit Note "+creditnote.getCreditNoteNumber(), request, creditnote.getID());
            
            if (preferences.isInventoryAccountingIntegration() && !preferences.isWithInvUpdate()) {
                JSONArray productArray = new JSONArray();
                if (!StringUtil.isNullOrEmpty(request.getParameter("productdetails"))) {
                    JSONArray jArr = new JSONArray(request.getParameter("productdetails"));
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject jobj = jArr.getJSONObject(i);
                        KwlReturnObject proresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString("productid"));
                        Product product = (Product) proresult.getEntityList().get(0);

                        JSONObject productObject = new JSONObject();
                        productObject.put("itemQuantity", jobj.getDouble("remquantity"));
                        productObject.put("itemCode", product.getProductid());
                        productArray.put(productObject);

                    }
                    if(productArray.length()>0){
                        
                        String sendDateFormat = "yyyy-MM-dd";
                        DateFormat dateformat = new SimpleDateFormat(sendDateFormat);
                        Date date = df.parse(request.getAttribute("creationdate")+"");
                        String stringDate = dateformat.format(date);

                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("deliveryDate", stringDate);
                        jSONObject.put("dateFormat", sendDateFormat);                        
                        jSONObject.put("details", productArray);
                        jSONObject.put("orderNumber", entryNumber);                        

                        String url = this.getServletContext().getInitParameter("inventoryURL");
                        CommonFnController cfc = new CommonFnController();
                        cfc.updateInventoryLevel(request, jSONObject, url, "17");
                    }                        
                }
            }
             cnTerms = request.getParameter("invoicetermsmap");
//            session.saveOrUpdate(creditnote);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveCreditNote : "+ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveCreditNote : "+ex.getMessage(), ex);
        }
        return creditnote;
    }
  
  private List saveCreditNoteRowsOW2(HashMap GlobalParams, HttpServletRequest request, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences, double externalCurrencyRate) throws JSONException, ServiceException, SessionExpiredException, ParseException, AccountingException {
        List resultlist = new ArrayList();
        double cnamount = 0.0;
        HashSet cndetails = new HashSet();
        HashSet jedetails = new HashSet();

        JournalEntryDetail jed;
        boolean reloadInventory = false;

        int i = 0; 
        String details = request.getParameter("details");
        if (!StringUtil.isNullOrEmpty(details)) {
            JSONArray jArr = new JSONArray(details);
            for (int iter = 0; iter < jArr.length(); iter++) {
                JSONObject jobj = jArr.getJSONObject(iter);

                CreditNoteDetail row = new CreditNoteDetail();
                String CreditNoteDetailID = StringUtil.generateUUID();
                row.setID(CreditNoteDetailID);
                row.setSrno(i + 1);
                row.setTotalDiscount(0.00);
                row.setCompany(company);
                row.setMemo(jobj.optString("desc"));

                String sales_accid = "";
                int cntype = StringUtil.isNullOrEmpty(request.getParameter("cntype")) ? 1 : Integer.parseInt(request.getParameter("cntype"));
//                if (cntype == 4 || cntype == 2) {//CN against vendor
//                    if (!StringUtil.isNullOrEmpty(jobj.optString("accountid"))) {
//                        sales_accid = jobj.optString("accountid");
//                    } else {
//                        throw new AccountingException("No debit account selected.");
//                    }
//                } else {
//                    KwlReturnObject dscresult = this.accAccountDAOobj.getAccountFromName(company.getCompanyID(), Constants.SALES_ACCOUNT);
//                    List ll = dscresult.getEntityList();
//
//                    if (ll.size() == 1) {
//                        sales_accid = ((Account) ll.get(0)).getID();
//                    } else {
//                        throw new AccountingException("No sales account found.");
//                    }
//                }
                cnamount += Double.parseDouble(jobj.optString("dramount"));//jobj.getDouble("discamount");
                cndetails.add(row);

//                JSONObject jedjson = new JSONObject();
//                jedjson.put("srno", jedetails.size() + 1);
//                jedjson.put("companyid", company.getCompanyID());
//                jedjson.put("amount", (Double.parseDouble(jobj.optString("dramount"))));
//                jedjson.put("accountid", sales_accid);
//                jedjson.put("debit", true);
//                jedjson.put("jeid", je.getID());
//                jedjson.put("description", jobj.optString("desc"));
//                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                jedetails.add(jed);

            }
        }else{
            CreditNoteDetail row = new CreditNoteDetail();
            String CreditNoteDetailID = StringUtil.generateUUID();
            row.setID(CreditNoteDetailID);
            row.setSrno(i+1);
            row.setTotalDiscount(0.00);
            row.setCompany(company);
            row.setMemo(request.getAttribute("memo")+"");

            String sales_accid = "";
            int cntype = (request.getAttribute("cntype")!=null&&!request.getAttribute("cntype").toString().equals(""))?Integer.parseInt(request.getAttribute("cntype")+""):1;
//            if(cntype == 4 || cntype == 2) {//CN against vendor
//                if(!StringUtil.isNullOrEmpty(request.getParameter("reverseaccid"))) {
//                    sales_accid = request.getParameter("reverseaccid");
//                } else {
//                    throw new AccountingException("No debit account selected.");
//                }
//            } else {
//                KwlReturnObject dscresult = this.accAccountDAOobj.getAccountFromName(company.getCompanyID(), Constants.SALES_ACCOUNT);
//                List ll = dscresult.getEntityList();
//
//                if(ll.size() == 1) {
//                    sales_accid = ((Account) ll.get(0)).getID();
//                } else {
//                    throw new AccountingException("No sales account found.");
//                }
//            }
            if(request.getAttribute("amount")!=null)
                cnamount = Double.parseDouble(request.getAttribute("amount").toString());//jobj.getDouble("discamount");

    //        JSONObject discjson = new JSONObject();
    //        discjson.put("discount", cnamount);
    //        discjson.put("inpercent", false);
    ////        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, cnamount, currencyid, null, externalCurrencyRate);
    //        discjson.put("originalamount", 0);//(Double) bAmt.getEntityList().get(0));
    //        discjson.put("companyid", company.getCompanyID());
    //        dscresult = accDiscountobj.addDiscount(discjson);
    //        Discount discount = (Discount) dscresult.getEntityList().get(0);
    //        row.setDiscount(discount);
            cndetails.add(row);

//            JSONObject jedjson = new JSONObject();
//            jedjson.put("srno", jedetails.size()+1);
//            jedjson.put("companyid", company.getCompanyID());
//            jedjson.put("amount", (cnamount));
//            jedjson.put("accountid", sales_accid);
//            jedjson.put("debit", true);
//            jedjson.put("jeid", je.getID());
//            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//            jedetails.add(jed);
        }
        resultlist.add(cnamount);  //resultlist.add(totalAmount + totalTax);
        resultlist.add(cnamount);
        resultlist.add(cndetails);
        resultlist.add(jedetails);
        resultlist.add(reloadInventory);
        return resultlist;
    }
    
   public DebitNote saveDebitNote(HttpServletRequest request,JournalEntry je) throws SessionExpiredException, ServiceException, AccountingException {
        DebitNote debitnote = null;
        List list = new ArrayList();
        KwlReturnObject result;
        try {
            double externalCurrencyRate= StringUtil.getDouble(request.getAttribute("externalcurrencyrate")+"");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String sequenceformat=request.getAttribute("sequenceformat")+"";
            DateFormat df = authHandler.getDateOnlyFormat(request);
            HashMap<String,Object> GlobalParams = new HashMap<String, Object>();
            GlobalParams.put("companyid", companyid);
            GlobalParams.put("gcurrencyid", currencyid);
            GlobalParams.put("dateformat", df);
            String customfield =request.getParameter("customfield");
            boolean otherwise = request.getAttribute("otherwise")!=null;
            boolean isnewdninjeEdit = request.getAttribute("isnewdninjeEdit")!=null;
            boolean isedit = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("jeedit"))) {
                isedit = Boolean.parseBoolean(request.getParameter("jeedit"));
            }
            Date creationDate = df.parse(request.getAttribute("creationdate")+"");
//            CompanyAccountPreferences preferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

//            Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

//            KWLCurrency kwlcurrency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

//            DebitNote debitnote = new DebitNote();
            String entryNumber = request.getAttribute("number")+"";
            currencyid=(request.getAttribute("currencyid")==null?kwlcurrency.getCurrencyID():request.getAttribute("currencyid")+"");
//            KWLCurrency currency=(KWLCurrency)session.get(KWLCurrency.class,currencyid);
            curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

//            String q="from DebitNote where debitNoteNumber=? and company.companyID=?";
//            if(!HibernateUtil.executeQuery(session, q, new Object[]{entryNumber, AuthHandler.getCompanyid(request)}).isEmpty())
//                throw new AccountingException("Debit note number '" + entryNumber + "' already exists.");
            if (isedit && !isnewdninjeEdit) {
                result = accDebitNoteobj.getDNFromNoteNo(entryNumber, companyid);
                int count = result.getRecordTotalCount();
                if (count > 0) {
                    throw new AccountingException("Debit Note number '" + entryNumber + "' already exists.");
                }
            }
            HashMap<String,Object> dnhm = new HashMap<String,Object>();
            boolean seqformat_oldflag = StringUtil.getBoolean(request.getAttribute("seqformat_oldflag")+"");
            String nextDNAutoNo = "";
            String nextAutoNoInt = "";
            
            if (isedit && !isnewdninjeEdit) {//Edit case of DN with party je 
                if (StringUtil.isNullOrEmpty(sequenceformat) || sequenceformat.equalsIgnoreCase("NA")) {
                    dnhm.put("entrynumber", entryNumber);
                    dnhm.put("autogenerated", false);
                } else {
                    int seqnumber = (Integer) request.getAttribute("dnseqnumber");
                    String datePrefix = request.getAttribute(Constants.DATEPREFIX)!=null?request.getAttribute(Constants.DATEPREFIX).toString():"";
                    String dateSuffix = request.getAttribute(Constants.DATESUFFIX)!=null?request.getAttribute(Constants.DATESUFFIX).toString():"";
                    dnhm.put("entrynumber", entryNumber);
                    dnhm.put(Constants.SEQFORMAT, sequenceformat);
                    dnhm.put(Constants.SEQNUMBER, seqnumber);
                    dnhm.put(Constants.DATEPREFIX, datePrefix);
                    dnhm.put(Constants.DATESUFFIX, dateSuffix);
                    dnhm.put("autogenerated", true);
                }
            } else { //newly created case of DN with party JE
                if (request.getAttribute("autoGenerateCNDN") == null) {
//                    if (!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equals("NA")) {
//                        if (seqformat_oldflag) {
//                            nextDNAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_DEBITNOTE, sequenceformat);
//                        } else {
//                            String[] nextAutoNoTemp = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_DEBITNOTE, sequenceformat, seqformat_oldflag);
//                            nextDNAutoNo = nextAutoNoTemp[0];
//                            nextAutoNoInt = nextAutoNoTemp[1];
//                            dnhm.put(Constants.SEQFORMAT, sequenceformat);
//                            dnhm.put(Constants.SEQNUMBER, nextAutoNoInt);
//                        }
//                    }
                    dnhm.put("entrynumber", "");
                    dnhm.put("autogenerated", !sequenceformat.equals("NA") ? true : false);
                } else {
//                    String[] format = accJournalEntryobj.getNextAutoNumber_modified(companyid, "autodebitnote");
                    dnhm.put("entrynumber", "");
//                    dnhm.put(Constants.SEQNUMBER, format[1]);
//                    KwlReturnObject curSeqObj = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), format[2]);
//                    SequenceFormat sequenceFormat = (SequenceFormat) curSeqObj.getEntityList().get(0);
//                    dnhm.put(Constants.SEQFORMAT, format[2]);
                    dnhm.put("autogenerated", !sequenceformat.equals("NA") ? true : false);
                }
            }    
           if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Debit_Note_ModuleId, entryNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request))+" <b>"+entryNumber+"</b> "+messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request))+" <b>"+formatName+"</b>. "+messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request))+" <b>"+formatName+"</b> "+messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
            dnhm.put("memo", request.getAttribute("memo")!=null?(request.getAttribute("memo")+""):"");
            dnhm.put("companyid", companyid);
            dnhm.put("currencyid", currencyid);

            Long seqNumber = null;
//            String query = "select count(dn.ID) from DebitNote dn inner join dn.journalEntry je  where dn.company.companyID=? and je.entryDate<=?";
//            List list = HibernateUtil.executeQuery(session, query, new Object[]{AuthHandler.getCompanyid(request), AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate"))});//
            result = accDebitNoteobj.getDNSequenceNo(companyid, creationDate);
            List li = result.getEntityList();
            if (!li.isEmpty()) {
                seqNumber = (Long) li.get(0);
            }
            dnhm.put("sequence", seqNumber.intValue());
            dnhm.put("journalentryid", je.getID());
            
            List DNlist = new ArrayList();
            double dnamount = 0.0;
            double dnamountdue = 0.0;
            int cntype = StringUtil.isNullOrEmpty(request.getAttribute("cntype")+"")?1:Integer.parseInt(request.getAttribute("cntype")+"");
            if(cntype == 4) {//CN against vendor
                dnhm.put("customerid", request.getAttribute("customerVendorId").toString());
            } else {
                dnhm.put("vendorid", request.getAttribute("customerVendorId").toString());
            }
            if(request.getAttribute("accid")!=null){
               dnhm.put("accountId", request.getAttribute("accid").toString());
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            KwlReturnObject baseAmount = null;
            double dnamountinbase = 0.0;
            if(otherwise  && (cntype == 2||cntype == 4)) {//Debit note otherwise
                dnhm.put("otherwise", true);
                dnhm.put("openflag", true);
                dnamount = Double.parseDouble(request.getAttribute("amount")+"");
                dnamountdue = dnamount;
                dnhm.put("dnamount", dnamount);
                /*
                * Saving the total amount in base currency 
                */
                baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, dnamount, currencyid, creationDate, externalCurrencyRate);
                dnamountinbase = (Double) baseAmount.getEntityList().get(0);
                dnamountinbase = authHandler.round(dnamountinbase, companyid);
                dnhm.put("dnamountinbase", dnamountinbase);
                dnhm.put("dnamountdue", dnamountdue);
                dnhm.put("cntype", cntype);
                dnhm.put("externalCurrencyRate", externalCurrencyRate);
                DNlist = saveDebitNoteRowsOW(GlobalParams, request, company, currency, je, preferences, externalCurrencyRate);                
            } 
            Double totalAmount = (Double) DNlist.get(0);
            Double discAccAmount = (Double) DNlist.get(1);
            HashSet<DebitNoteDetail> dndetails = (HashSet<DebitNoteDetail>) DNlist.get(2);              
            /*
             * If invoice terms applied then add mapping in against invoice
             */
            double termTotalAmount = 0;
            HashMap<String, Double> termAcc = new HashMap<String, Double>();
            String dnTerms = request.getParameter("invoicetermsmap");
            if(!StringUtil.isNullOrEmpty(dnTerms)) {
                JSONArray termsArr = new JSONArray(dnTerms);
                for(int cnt=0; cnt<termsArr.length(); cnt++) {
                    double termamount = termsArr.getJSONObject(cnt).getDouble("termamount");
                    termTotalAmount += termamount;
                    if(termAcc.containsKey(termsArr.getJSONObject(cnt).getString("glaccount"))) {
                        double tempAmount=termAcc.get(termsArr.getJSONObject(cnt).getString("glaccount"));
                        termAcc.put(termsArr.getJSONObject(cnt).getString("glaccount"), termamount+tempAmount);
                    }
                    else{    
                        termAcc.put(termsArr.getJSONObject(cnt).getString("glaccount"), termamount);
                    }
                }
            }
            totalAmount+=termTotalAmount;
            dnhm.put("vendorentry", request.getAttribute("vendorentry")!=null?(request.getAttribute("vendorentry")+""):"");
            dnhm.put("approvestatuslevel", 11);
            dnhm.put("creationDate", creationDate);
            result = accDebitNoteobj.addDebitNote(dnhm);
            debitnote = (DebitNote)result.getEntityList().get(0);
            
            dnhm.put("dnid", debitnote.getID());
            Iterator itr = dndetails.iterator();
            while (itr.hasNext()) {
                DebitNoteDetail cnd = (DebitNoteDetail) itr.next();
                cnd.setDebitNote(debitnote);
            }
            dnhm.put("dndetails", dndetails);

//            session.saveOrUpdate(debitnote);
            result = accDebitNoteobj.updateDebitNote(dnhm);
            debitnote = (DebitNote)result.getEntityList().get(0);
            
            //Add entry in optimized table
//            accJournalEntryobj.saveAccountJEs_optimized(jeid);
            
            list.add(debitnote);
            if (preferences.isInventoryAccountingIntegration() && !preferences.isWithInvUpdate()) {
                JSONArray productArray = new JSONArray();
                if (!StringUtil.isNullOrEmpty(request.getParameter("productdetails"))) {
                    JSONArray jArr = new JSONArray(request.getParameter("productdetails"));
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject jobj = jArr.getJSONObject(i);
                        KwlReturnObject proresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString("productid"));
                        Product product = (Product) proresult.getEntityList().get(0);

                        JSONObject productObject = new JSONObject();
                        productObject.put("itemQuantity", jobj.getDouble("remquantity")*(-1));
                        productObject.put("itemCode", product.getProductid());
                        productArray.put(productObject);

                    }
                    if(productArray.length()>0){
                        
                        String sendDateFormat = "yyyy-MM-dd";
                        DateFormat dateformat = new SimpleDateFormat(sendDateFormat);
                        Date date = df.parse(request.getAttribute("creationdate")+"");
                        String stringDate = dateformat.format(date);

                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("deliveryDate", stringDate);
                        jSONObject.put("dateFormat", sendDateFormat);                        
                        jSONObject.put("details", productArray);
                        jSONObject.put("orderNumber", entryNumber);                        

                        String url = this.getServletContext().getInitParameter("inventoryURL");
                        CommonFnController cfc = new CommonFnController();
                        cfc.updateInventoryLevel(request, jSONObject, url , "17");
                    }                        
                }
            }
            dnTerms = request.getParameter("invoicetermsmap");
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveDebitNote : "+ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveDebitNote : "+ex.getMessage(), ex);
        }
        
//         auditTrailObj.insertAuditLog("80", "User " + sessionHandlerImpl.getUserFullName(request) + " has recorded a new Debit Note " + debitnote.getDebitNoteNumber(), request, debitnote.getID());
         
        return debitnote;
    } 
   
   private List saveDebitNoteRowsOW(HashMap GlobalParams, HttpServletRequest request, Company company, KWLCurrency currency, JournalEntry je, CompanyAccountPreferences preferences, double externalCurrencyRate) throws JSONException, ServiceException, SessionExpiredException, ParseException, AccountingException {
        List resultlist = new ArrayList();
        double cnamount = 0.0;
        HashSet cndetails = new HashSet();
        HashSet jedetails = new HashSet();

        JournalEntryDetail jed;
        boolean reloadInventory = false;

         int i = 0;
         String details = request.getParameter("details");
         if (!StringUtil.isNullOrEmpty(details)) {
             JSONArray jArr = new JSONArray(details);
             for (int iter = 0; iter < jArr.length(); iter++) {
                 JSONObject jobj = jArr.getJSONObject(iter);
                 DebitNoteDetail row = new DebitNoteDetail();
                 String DebitNoteDetailID = StringUtil.generateUUID();
                 row.setID(DebitNoteDetailID);
                 row.setSrno(i + 1);
                 row.setTotalDiscount(0.00);
                 row.setCompany(company);
                 row.setMemo(jobj.optString("desc"));
                 String purchase_accid = "";
                 int cntype = StringUtil.isNullOrEmpty(request.getAttribute("cntype")+"") ? 1 : Integer.parseInt(request.getAttribute("cntype")+"");
//                 if (cntype == 4 || cntype == 2) {//DN against customer
//                     if (!StringUtil.isNullOrEmpty(jobj.optString("accountid"))) {
//                         purchase_accid = jobj.optString("accountid");
//                     } else {
//                         throw new AccountingException("No credit account selected.");
//                     }
//                 } else {
//                     KwlReturnObject dscresult = this.accAccountDAOobj.getAccountFromName(company.getCompanyID(), Constants.PURCHASE_ACCOUNT);
//                     List ll = dscresult.getEntityList();
//                     if (ll.size() == 1) {
//                         purchase_accid = ((Account) ll.get(0)).getID();
//                     } else {
//                         throw new AccountingException("No Purchase account found.");
//                     }
//                 }
                 cnamount += Double.parseDouble(jobj.optString("dramount"));//jobj.getDouble("discamount");        
                 cndetails.add(row);

//                 JSONObject jedjson = new JSONObject();
//                 jedjson.put("srno", jedetails.size() + 1);
//                 jedjson.put("companyid", company.getCompanyID());
//                 jedjson.put("amount", (Double.parseDouble(jobj.optString("dramount"))));
//                 jedjson.put("accountid", purchase_accid);
//                 jedjson.put("debit", false);
//                 jedjson.put("jeid", je.getID());
//                 jedjson.put("description", jobj.optString("desc"));
//                 KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//                 jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//                 jedetails.add(jed);
                 i++;
             }

         }else{
            DebitNoteDetail row = new DebitNoteDetail();
            String DebitNoteDetailID = StringUtil.generateUUID();
            row.setID(DebitNoteDetailID);
            row.setSrno(i+1);
            row.setTotalDiscount(0.00);
            row.setCompany(company);
            row.setMemo(request.getAttribute("memo")+"");
            String purchase_accid = "";
            int cntype = StringUtil.isNullOrEmpty(request.getAttribute("cntype")+"")?1:Integer.parseInt(request.getAttribute("cntype")+"");
//            if(cntype == 4 || cntype == 2) {//DN against customer
//                if(!StringUtil.isNullOrEmpty(request.getParameter("reverseaccid"))) {
//                    purchase_accid = request.getParameter("reverseaccid");
//                } else {
//                    throw new AccountingException("No credit account selected.");
//                }
//            } else {
//            KwlReturnObject dscresult = this.accAccountDAOobj.getAccountFromName(company.getCompanyID(), Constants.PURCHASE_ACCOUNT);
//            List ll = dscresult.getEntityList();
//            if(ll.size() == 1) {
//                purchase_accid = ((Account) ll.get(0)).getID();
//            } else {
//                throw new AccountingException("No Purchase account found.");
//            }
//            }
            cnamount = Double.parseDouble(request.getAttribute("amount")+"");//jobj.getDouble("discamount");

    //        JSONObject discjson = new JSONObject();
    //        discjson.put("discount", cnamount);
    //        discjson.put("inpercent", false);
    ////        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, cnamount, currencyid, null, externalCurrencyRate);
    //        discjson.put("originalamount", 0);//(Double) bAmt.getEntityList().get(0));
    //        discjson.put("companyid", company.getCompanyID());
    //        dscresult = accDiscountobj.addDiscount(discjson);
    //        Discount discount = (Discount) dscresult.getEntityList().get(0);
    //        row.setDiscount(discount);
            cndetails.add(row);

//            JSONObject jedjson = new JSONObject();
//            jedjson.put("srno", jedetails.size()+1);
//            jedjson.put("companyid", company.getCompanyID());
//            jedjson.put("amount", (cnamount));
//            jedjson.put("accountid", purchase_accid);
//            jedjson.put("debit", false);
//            jedjson.put("jeid", je.getID());
//            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
//            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
//            jedetails.add(jed);
            }
            resultlist.add(cnamount);  //resultlist.add(totalAmount + totalTax);
            resultlist.add(cnamount);
            resultlist.add(cndetails);
            resultlist.add(jedetails);
            resultlist.add(reloadInventory);
            return resultlist;
    }
   
    /**
     * Description: Used to import custom Journal Entry and Refund Journal Entry
     * @param request
     * @param response
     * @return ModelAndView
     * @throws ServiceException 
     */
    public ModelAndView importjournalEntry(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String doAction = request.getParameter("do");
            boolean isRefundJournalEntryImport = (request.getParameter("isRefundJournalEntryImport") == null ? false : Boolean.parseBoolean(request.getParameter("isRefundJournalEntryImport")));

            if (doAction.compareToIgnoreCase("getMapXLS") == 0) {
                String xlsFileTmpPath = StorageHandler.GetDocStorePath();
                System.out.println("A(( Upload XLS start : " + new Date());
                jobj.put("success", true);
                FileItemFactory factory = new DiskFileItemFactory(4096, new File(System.getProperty("java.io.tmpdir")));  //"/tmp"));
                ServletFileUpload upload = new ServletFileUpload(factory);
                upload.setSizeMax(10000000);
                List fileItems = upload.parseRequest(request);
                Iterator i = fileItems.iterator();
                String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "xlsfiles";
                String fileName = null;
                String fileid = UUID.randomUUID().toString();
                fileid = fileid.replaceAll("-", ""); // To append UUID without "-" [SK]
                String Ext = "";
                while (i.hasNext()) {
                    java.io.File destDir = new java.io.File(destinationDirectory);
                    if (!destDir.exists()) { // Create xls file's folder if not present
                        destDir.mkdirs();
                    }

                    FileItem fi = (FileItem) i.next();
                    if (fi.isFormField()) {
                        continue;
                    }
                    fileName = fi.getName();
                    if (fileName.contains(".")) {
                        Ext = fileName.substring(fileName.lastIndexOf("."));
                        int startIndex = fileName.contains("\\") ? (fileName.lastIndexOf("\\") + 1) : 0;
                        fileName = fileName.substring(startIndex, fileName.lastIndexOf("."));
                    }

                    if (fileName.length() > 28) { // To fixed Mysql ERROR 1103 (42000): Incorrect table name
                        throw new DataInvalidateException("Filename is too long, use upto 28 characters.");
                    }
                    fi.write(new File(destinationDirectory, fileName + "_" + fileid + Ext));
                }

                FileInputStream fs =new FileInputStream(destinationDirectory + StorageHandler.GetFileSeparator() + fileName + "_" + fileid + Ext);
                Workbook wb = WorkbookFactory.create(fs);
                int count = wb.getNumberOfSheets();
                JSONArray jArr = new JSONArray();
                for (int x = 0; x < count; x++) {
                    JSONObject obj = new JSONObject();
                    obj.put("name", wb.getSheetName(x));
                    obj.put("index", x);
                    jArr.put(obj);
                }
                jobj.put("file", destinationDirectory + StorageHandler.GetFileSeparator() + fileName + "_" + fileid + Ext);
                jobj.put("filename", fileName + "_" + fileid + Ext);
                jobj.put("data", jArr);
                jobj.put("msg", "File has been successfully uploaded");
                jobj.put("lsuccess", true);
                jobj.put("valid", true);

                if (isRefundJournalEntryImport) {
                    jobj = importXLSRefundJournalEntryRecords(request, jobj);
                } else {
                    jobj = importXLSjournalEntryRecords(request, jobj);
                }
                
                issuccess = true;
            } else if (doAction.compareToIgnoreCase("getMapCSV") == 0) {
                jobj = importHandler.getMappingCSVHeader(request);
                jobj = importCSVjournalEntryRecords(request, jobj);
                issuccess = true;
            }
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

    /**
     * Description: Method is used to import Custom Journal Entry records.
     *
     * @param <request> used to get parameter from session
     * @param <jobj> used to get filename and file path which we have importing
     * @return JSONObject
     * @throws AccountingException
     * @throws IOException
     * @throws SessionExpiredException
     * @throws JSONException
     */
    public JSONObject importXLSjournalEntryRecords(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus tranStatus = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        int total = 0, failed = 0;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String currencyId = sessionHandlerImpl.getCurrencyID(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("filename");

        String saleAccID = (request.getParameter("saleAccID") == null ? "" : request.getParameter("saleAccID"));
        String commissionAccID = (request.getParameter("commissionAccID") == null ? "" : request.getParameter("commissionAccID"));
        String commissionGSTAccID = (request.getParameter("commissionGSTAccID") == null ? "" : request.getParameter("commissionGSTAccID"));
        String paymentGatewayChargeAccID = (request.getParameter("paymentGatewayChargeAccID") == null ? "" : request.getParameter("paymentGatewayChargeAccID"));
        String amountToSellerAccID = (request.getParameter("amountToSellerAccID") == null ? "" : request.getParameter("amountToSellerAccID"));

        JSONObject returnObj = new JSONObject();

        try {
            // get selected dateformat
            String dateFormat = "yyyy-MM-dd";

            int sheetNo = 0; // to import data at sheet no 1

            FileInputStream fs = new FileInputStream(jobj.getString("file"));

            Workbook wb = WorkbookFactory.create(fs);
            Sheet sheet = wb.getSheetAt(sheetNo);

            StringBuilder failedRecords = new StringBuilder();

            List<String> customFieldArrayList = new ArrayList<>();
            customFieldArrayList.add("Date From");
            customFieldArrayList.add("Date To");
            customFieldArrayList.add("Vendor ID");
            customFieldArrayList.add("Vendor");
            customFieldArrayList.add("Membership Type");
            customFieldArrayList.add("Order Number");
            customFieldArrayList.add("Order Date");
            customFieldArrayList.add("Order Completion Date");
            customFieldArrayList.add("Payment Method");
            customFieldArrayList.add("Status");

            List headArrayList = new ArrayList();
            headArrayList.add("Order Number");
            headArrayList.add("Invoice Number");
            headArrayList.add("Order Date");
            headArrayList.add("Order Completion Date");
            headArrayList.add("Amount");
            headArrayList.add("Payment Method");
            headArrayList.add("Status");

            failedRecords.append(createCSVrecord(headArrayList.toArray())).append("\"Error Message\"");

            HashMap<String, Integer> firstHeaderColumnConfig = new HashMap<>();
            HashMap<String, Integer> secondHeaderColumnConfig = new HashMap<>();
            boolean isFirstHeader = false, isSecondHeader = false, isSecondHeaderExist = false, isFirstHeaderRecFailed = false;
            String firstHeaderRecFailedMsg = "";
            Date fromDate = null, toDate = null, createdAt = null;
            String vendorID = "", vendor = "", membershipType = "";

            double amount = 0;
            String invoiceNumber = "", paymentMethod = "", status = "", orderNumber = "";
            Date orderDate = null, orderCompletionDate = null;

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    int maxCol = row.getLastCellNum();

                    List recarr = new ArrayList();
                    for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                        Cell cell = row.getCell(cellcount);

                        if (cell != null) {
                            recarr.add(cell);
                        } else {
                            recarr.add("");
                        }
                    }

                    try {
                        if (isFirstHeader) {
                            if (firstHeaderColumnConfig.containsKey("dateFrom")) {
                                Cell cell = row.getCell((Integer) firstHeaderColumnConfig.get("dateFrom"));

                                if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) {
                                    isFirstHeaderRecFailed = true;
                                    throw new AccountingException("Date From is not available");
                                } else {
                                    try {
                                        fromDate = cell.getDateCellValue();
                                    } catch (Exception ex) {
                                        isFirstHeaderRecFailed = true;
                                        throw new AccountingException("Incorrect date format for Date From, Please specify values in " + dateFormat + " format.");
                                    }
                                }
                            } else {
                                isFirstHeaderRecFailed = true;
                                throw new AccountingException("Date From column is not found.");
                            }

                            if (firstHeaderColumnConfig.containsKey("dateTo")) {
                                Cell cell = row.getCell((Integer) firstHeaderColumnConfig.get("dateTo"));

                                if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) {
                                    isFirstHeaderRecFailed = true;
                                    throw new AccountingException("Date To is not available.");
                                } else {
                                    try {
                                        toDate = cell.getDateCellValue();
                                    } catch (Exception ex) {
                                        isFirstHeaderRecFailed = true;
                                        throw new AccountingException("Incorrect date format for Date To, Please specify values in " + dateFormat + " format.");
                                    }
                                }
                            } else {
                                isFirstHeaderRecFailed = true;
                                throw new AccountingException("Date To column is not found.");
                            }

                            if (firstHeaderColumnConfig.containsKey("vendorID")) {
                                Cell cell = row.getCell((Integer) firstHeaderColumnConfig.get("vendorID"));

                                if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) {
                                    isFirstHeaderRecFailed = true;
                                    throw new AccountingException("Vendor ID is not available");
                                } else {
                                    vendorID = importHandler.getCellValue(cell);
                                }
                            } else {
                                isFirstHeaderRecFailed = true;
                                throw new AccountingException("Vendor ID column is not found.");
                            }

                            if (firstHeaderColumnConfig.containsKey("vendor")) {
                                Cell cell = row.getCell((Integer) firstHeaderColumnConfig.get("vendor"));

                                if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) {
                                    isFirstHeaderRecFailed = true;
                                    throw new AccountingException("Vendor is not available");
                                } else {
                                    vendor = importHandler.getCellValue(cell);
                                }
                            } else {
                                isFirstHeaderRecFailed = true;
                                throw new AccountingException("Vendor column is not found.");
                            }

                            if (firstHeaderColumnConfig.containsKey("membershipType")) {
                                Cell cell = row.getCell((Integer) firstHeaderColumnConfig.get("membershipType"));

                                if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) {
                                    isFirstHeaderRecFailed = true;
                                    throw new AccountingException("Membership Type is not available");
                                } else {
                                    membershipType = importHandler.getCellValue(cell);
                                }
                            } else {
                                isFirstHeaderRecFailed = true;
                                throw new AccountingException("Membership Type column is not found.");
                            }

                            if (firstHeaderColumnConfig.containsKey("createdAt")) {
                                Cell cell = row.getCell((Integer) firstHeaderColumnConfig.get("createdAt"));

                                if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) {
                                    isFirstHeaderRecFailed = true;
                                    throw new AccountingException("Created At is not available.");
                                } else {
                                    try {
                                        createdAt = cell.getDateCellValue();
                                    } catch (Exception ex) {
                                        isFirstHeaderRecFailed = true;
                                        throw new AccountingException("Incorrect date format for Created At, Please specify values in " + dateFormat + " format.");
                                    }
                                }
                            } else {
                                isFirstHeaderRecFailed = true;
                                throw new AccountingException("Created At column is not found.");
                            }

                            isFirstHeader = false;
                            continue;
                        }

                        // for getting header index
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            Cell cell = row.getCell(cellcount);

                            // for first header and getting its index
                            if (cell != null && !isFirstHeader && !isSecondHeader) {
                                if (cell.getStringCellValue().trim().equalsIgnoreCase("Date From")) {
                                    firstHeaderColumnConfig.put("dateFrom", cellcount);
                                    int nextCount = 1;
                                    cell = row.getCell(cellcount + nextCount);

                                    if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Date To")) {
                                        firstHeaderColumnConfig.put("dateTo", cellcount + nextCount);
                                        nextCount++;
                                        cell = row.getCell(cellcount + nextCount);

                                        if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Vendor ID")) {
                                            firstHeaderColumnConfig.put("vendorID", cellcount + nextCount);
                                            nextCount++;
                                            cell = row.getCell(cellcount + nextCount);

                                            if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Vendor")) {
                                                firstHeaderColumnConfig.put("vendor", cellcount + nextCount);
                                                nextCount++;
                                                cell = row.getCell(cellcount + nextCount);

                                                if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Membership Type")) {
                                                    firstHeaderColumnConfig.put("membershipType", cellcount + nextCount);
                                                    nextCount++;
                                                    cell = row.getCell(cellcount + nextCount);

                                                    if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Created At")) {
                                                        firstHeaderColumnConfig.put("createdAt", cellcount + nextCount);
                                                        isFirstHeader = true;
                                                        isFirstHeaderRecFailed = false;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            }

                            // for second header and getting its index
                            if (cell != null && !isSecondHeader) {
                                if (cell.getStringCellValue().trim().equalsIgnoreCase("Order Number")) {
                                    secondHeaderColumnConfig.put("orderNumber", cellcount);
                                    int nextCount = 1;
                                    cell = row.getCell(cellcount + nextCount);

                                    if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Invoice Number")) {
                                        secondHeaderColumnConfig.put("invoiceNumber", cellcount + nextCount);
                                        nextCount++;
                                        cell = row.getCell(cellcount + nextCount);

                                        if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Order Date")) {
                                            secondHeaderColumnConfig.put("orderDate", cellcount + nextCount);
                                            nextCount++;
                                            cell = row.getCell(cellcount + nextCount);

                                            if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Order Completion Date")) {
                                                secondHeaderColumnConfig.put("orderCompletionDate", cellcount + nextCount);
                                                nextCount++;
                                                cell = row.getCell(cellcount + nextCount);

                                                if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Amount")) {
                                                    secondHeaderColumnConfig.put("amount", cellcount + nextCount);
                                                    nextCount++;
                                                    cell = row.getCell(cellcount + nextCount);

                                                    if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Payment Method")) {
                                                        secondHeaderColumnConfig.put("paymentMethod", cellcount + nextCount);
                                                        nextCount++;
                                                        cell = row.getCell(cellcount + nextCount);

                                                        if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Status")) {
                                                            secondHeaderColumnConfig.put("status", cellcount + nextCount);
                                                            isSecondHeader = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (isFirstHeader || (isSecondHeader && !isSecondHeaderExist)) {
                            if (isSecondHeader) {
                                isSecondHeaderExist = true;
                            }

                            continue;
                        } else {
                            if (isSecondHeaderExist) {
                                if (secondHeaderColumnConfig.containsKey("orderNumber")) {
                                    Cell cell = row.getCell((Integer) secondHeaderColumnConfig.get("orderNumber"));

                                    if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) { // 1st Blank cell
                                        int nextCount = 1;
                                        cell = row.getCell((Integer) secondHeaderColumnConfig.get("orderNumber") + nextCount);
                                        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) { // 2nd Blank cell
                                            nextCount++;
                                            cell = row.getCell((Integer) secondHeaderColumnConfig.get("orderNumber") + nextCount);
                                            if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) { // 3rd Blank cell
                                                nextCount++;
                                                cell = row.getCell((Integer) secondHeaderColumnConfig.get("orderNumber") + nextCount);
                                                if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell)) && importHandler.getCellValue(cell).equalsIgnoreCase("Total")) { // 4th cell contains 'Total'
                                                    isSecondHeaderExist = false;
                                                    isSecondHeader = false;
                                                    continue;
                                                } else if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) { // 4th Blank cell
                                                    nextCount++;
                                                    cell = row.getCell((Integer) secondHeaderColumnConfig.get("orderNumber") + nextCount);
                                                    if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) { // 5th Blank cell
                                                        nextCount++;
                                                        cell = row.getCell((Integer) secondHeaderColumnConfig.get("orderNumber") + nextCount);
                                                        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) { // 6th Blank cell
                                                            nextCount++;
                                                            cell = row.getCell((Integer) secondHeaderColumnConfig.get("orderNumber") + nextCount);
                                                            if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) { // 7th Blank cell
                                                                continue;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (isSecondHeaderExist) {
                                            total++;
                                            if (isFirstHeaderRecFailed) {
                                                throw new AccountingException(firstHeaderRecFailedMsg);
                                            }
                                            throw new AccountingException("Order Number is not available.");
                                        }
                                    } else {
                                        total++;
                                        if (isFirstHeaderRecFailed) {
                                            throw new AccountingException(firstHeaderRecFailedMsg);
                                        }
                                        orderNumber = importHandler.getCellValue(cell);
                                    }
                                } else {
                                    total++;
                                    if (isFirstHeaderRecFailed) {
                                        throw new AccountingException(firstHeaderRecFailedMsg);
                                    }
                                    throw new AccountingException("Order Number column is not found.");
                                }

                                if (secondHeaderColumnConfig.containsKey("invoiceNumber")) {
                                    Cell cell = row.getCell((Integer) secondHeaderColumnConfig.get("invoiceNumber"));

                                    if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) {
                                        throw new AccountingException("Invoice Number is not available.");
                                    } else {
                                        invoiceNumber = importHandler.getCellValue(cell);
                                    }
                                } else {
                                    throw new AccountingException("Invoice Number column is not found.");
                                }

                                if (secondHeaderColumnConfig.containsKey("orderDate")) {
                                    Cell cell = row.getCell((Integer) secondHeaderColumnConfig.get("orderDate"));

                                    if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) {
                                        throw new AccountingException("Order Date is not available.");
                                    } else {
                                        try {
                                            orderDate = cell.getDateCellValue();
                                        } catch (Exception ex) {
                                            throw new AccountingException("Incorrect date format for Order Date, Please specify values in " + dateFormat + " format.");
                                        }
                                    }
                                } else {
                                    throw new AccountingException("Order Date column is not found.");
                                }

                                if (secondHeaderColumnConfig.containsKey("orderCompletionDate")) {
                                    Cell cell = row.getCell((Integer) secondHeaderColumnConfig.get("orderCompletionDate"));

                                    if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) {
                                        throw new AccountingException("Order Completion Date is not available.");
                                    } else {
                                        try {
                                            orderCompletionDate = cell.getDateCellValue();
                                        } catch (Exception ex) {
                                            throw new AccountingException("Incorrect date format for Order Completion Date, Please specify values in " + dateFormat + " format.");
                                        }
                                    }
                                } else {
                                    throw new AccountingException("Order Completion Date column is not found.");
                                }

                                if (secondHeaderColumnConfig.containsKey("amount")) {
                                    Cell cell = row.getCell((Integer) secondHeaderColumnConfig.get("amount"));

                                    if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) {
                                        throw new AccountingException("Amount is not available.");
                                    } else {
                                        try {
                                            amount = Double.parseDouble(importHandler.getCellValue(cell));
                                        } catch (Exception ex) {
                                            throw new AccountingException("Incorrect numeric value for Amount, Please ensure that value type of Amount matches with the Amount.");
                                        }
                                    }
                                } else {
                                    throw new AccountingException("Amount column is not found.");
                                }

                                if (secondHeaderColumnConfig.containsKey("paymentMethod")) {
                                    Cell cell = row.getCell((Integer) secondHeaderColumnConfig.get("paymentMethod"));

                                    if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) {
                                        throw new AccountingException("Payment Method is not available.");
                                    } else {
                                        paymentMethod = importHandler.getCellValue(cell);
                                    }
                                } else {
                                    throw new AccountingException("Payment Method column is not found.");
                                }

                                if (secondHeaderColumnConfig.containsKey("status")) {
                                    Cell cell = row.getCell((Integer) secondHeaderColumnConfig.get("status"));

                                    if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) {
                                        throw new AccountingException("Status is not available.");
                                    } else {
                                        status = importHandler.getCellValue(cell);
                                    }
                                } else {
                                    throw new AccountingException("Status column is not found.");
                                }

                                KwlReturnObject result = accJournalEntryobj.getJECount(invoiceNumber, companyid);
                                int nocount = result.getRecordTotalCount();
                                if (nocount > 0) {
                                    throw new AccountingException("Journal entry number '" + invoiceNumber + "' already exists.");
                                }
                                if (!status.equalsIgnoreCase("Complete")) {
                                    throw new AccountingException("Status of Journal entry number '" + invoiceNumber + "' is not Complete.");
                                }

                                // For create custom field array
                                JSONArray customJArr = new JSONArray();
                                for (int count = 0; count < customFieldArrayList.size(); count++) {
                                    HashMap<String, Object> requestParams = new HashMap<>();
                                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                    requestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_GENERAL_LEDGER_ModuleId, customFieldArrayList.get(count)));

                                    KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
                                    FieldParams params = null;
                                    if (fieldParamsResult.getEntityList() != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                        params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                    } else {
                                        throw new AccountingException("Custom Field for '" + customFieldArrayList.get(count) + "' column is not found.");
                                    }

                                    JSONObject customJObj = new JSONObject();
                                    customJObj.put("fieldid", params.getId());
                                    customJObj.put("filedid", params.getId());
                                    customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                    customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                    customJObj.put("xtype", params.getFieldtype());

                                    String fieldComboDataStr = "";
                                    if (params.getFieldtype() == 3) { // if field of date type
                                        if (fromDate != null && customFieldArrayList.get(count).equalsIgnoreCase("Date From")) {
                                            customJObj.put("Col" + params.getColnum(), fromDate.getTime());
                                            customJObj.put("fieldDataVal", fromDate.getTime());
                                        } else if (toDate != null && customFieldArrayList.get(count).equalsIgnoreCase("Date To")) {
                                            customJObj.put("Col" + params.getColnum(), toDate.getTime());
                                            customJObj.put("fieldDataVal", toDate.getTime());
                                        } else if (orderDate != null && customFieldArrayList.get(count).equalsIgnoreCase("Order Date")) {
                                            customJObj.put("Col" + params.getColnum(), orderDate.getTime());
                                            customJObj.put("fieldDataVal", orderDate.getTime());
                                        } else if (orderCompletionDate != null && customFieldArrayList.get(count).equalsIgnoreCase("Order Completion Date")) {
                                            customJObj.put("Col" + params.getColnum(), orderCompletionDate.getTime());
                                            customJObj.put("fieldDataVal", orderCompletionDate.getTime());
                                        }
                                    } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                        if (!StringUtil.isNullOrEmpty(paymentMethod) && customFieldArrayList.get(count).equalsIgnoreCase("Payment Method")) {
                                            requestParams = new HashMap<>();
                                            requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                            requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), paymentMethod, 0));


                                            fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                            if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                                FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                                fieldComboDataStr += fieldComboData.getId() + ",";
                                            }
                                        }

                                        if (!StringUtil.isNullOrEmpty(membershipType) && customFieldArrayList.get(count).equalsIgnoreCase("Membership Type")) {
                                            requestParams = new HashMap<>();
                                            requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                            requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), membershipType, 0));


                                            fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                            if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                                FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                                fieldComboDataStr += fieldComboData.getId() + ",";
                                            }
                                        }

                                        if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                            customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                        } else {
                                            continue;
                                        }
                                    } else {
                                        if (!StringUtil.isNullOrEmpty(vendorID) && customFieldArrayList.get(count).equalsIgnoreCase("Vendor ID")) {
                                            customJObj.put("Col" + params.getColnum(), vendorID);
                                            customJObj.put("fieldDataVal", vendorID);
                                        } else if (!StringUtil.isNullOrEmpty(vendor) && customFieldArrayList.get(count).equalsIgnoreCase("Vendor")) {
                                            customJObj.put("Col" + params.getColnum(), vendor);
                                            customJObj.put("fieldDataVal", vendor);
                                        } else if (!StringUtil.isNullOrEmpty(orderNumber) && customFieldArrayList.get(count).equalsIgnoreCase("Order Number")) {
                                            customJObj.put("Col" + params.getColnum(), orderNumber);
                                            customJObj.put("fieldDataVal", orderNumber);
                                        } else if (!StringUtil.isNullOrEmpty(status) && customFieldArrayList.get(count).equalsIgnoreCase("Status")) {
                                            customJObj.put("Col" + params.getColnum(), status);
                                            customJObj.put("fieldDataVal", status);
                                        }
                                    }

                                    customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());

                                    customJArr.put(customJObj);
                                }

                                String customfield = customJArr.toString();

                                double commission = 0;
                                double commissionGST = 0;
                                if (membershipType.equalsIgnoreCase("Professional") || membershipType.equalsIgnoreCase("Seller")) { // calculte commission and commissoinGST for Membership Type - "Professional" OR "Seller"
                                    commission = amount * 0.13;
                                    commission = authHandler.round(commission, companyid);

                                    commissionGST = (amount * 0.13) * (0.07);
                                    commissionGST = authHandler.round(commissionGST, companyid);
                                } else if (membershipType.equalsIgnoreCase("Deluxe") || membershipType.equalsIgnoreCase("BSSeller")) { // calculte commission and commissoinGST for Membership Type - "Deluxe" OR "BSSeller"
                                    commission = amount * 0.12;
                                    commission = authHandler.round(commission, companyid);

                                    commissionGST = (amount * 0.12) * (0.07);
                                    commissionGST = authHandler.round(commissionGST, companyid);
                                } else if (membershipType.equalsIgnoreCase("Basic") || membershipType.equalsIgnoreCase("SpecialBSSeller")) { // calculte commission and commissoinGST for Membership Type - "Basic" OR "SpecialBSSeller"
                                    commission = amount * 0.10;
                                    commission = authHandler.round(commission, companyid);

                                    commissionGST = (amount * 0.10) * (0.07);
                                    commissionGST = authHandler.round(commissionGST, companyid);
                                } else if (membershipType.equalsIgnoreCase("9percent")) { // calculte commission and commissoinGST for Membership Type - "9percent"
                                    commission = amount * 0.09;
                                    commission = authHandler.round(commission, companyid);

                                    commissionGST = (amount * 0.09) * (0.07);
                                    commissionGST = authHandler.round(commissionGST, companyid);
                                } else if (membershipType.equalsIgnoreCase("BugisStreet") || membershipType.equalsIgnoreCase("BSO")) { // calculte commission and commissoinGST for Membership Type - "BugisStreet" OR "BSO"
                                    commission = 0;

                                    commissionGST = 0;
                                }

                                double paymentGatewayCharge = 0;
                                if (paymentMethod.equalsIgnoreCase("Visa") || paymentMethod.equalsIgnoreCase("MasterCard")) {
                                    paymentGatewayCharge = amount * 0.023;
                                    paymentGatewayCharge = authHandler.round(paymentGatewayCharge, companyid);
                                } else if (paymentMethod.equalsIgnoreCase("eNETS")) {
                                    paymentGatewayCharge = amount * 0.025;
                                    paymentGatewayCharge = authHandler.round(paymentGatewayCharge, companyid);

                                    if (1.50 > paymentGatewayCharge) {
                                        paymentGatewayCharge = 1.50;
                                    }
                                }

                                double amountToSeller = amount - commission - commissionGST - paymentGatewayCharge;

                                // for save journal entry
                                JSONArray jArr = new JSONArray();
                                JSONObject jObj = new JSONObject();

                                // DR Commission
                                jObj.put("debit", true);
                                jObj.put("accountid", commissionAccID);
                                jObj.put("customerVendorId", commissionAccID);
                                jObj.put("description", "");
                                jObj.put("accountpersontype", 0);
                                jObj.put("rowid", 1);
                                jObj.put("amount", commission);
                                jObj.put("customfield", "[{}]");
                                jArr.put(jObj);

                                jObj = new JSONObject();
                                // DR Commission GST
                                jObj.put("debit", true);
                                jObj.put("accountid", commissionGSTAccID);
                                jObj.put("customerVendorId", commissionGSTAccID);
                                jObj.put("description", "");
                                jObj.put("accountpersontype", 0);
                                jObj.put("rowid", 2);
                                jObj.put("amount", commissionGST);
                                jObj.put("customfield", "[{}]");
                                jArr.put(jObj);

                                jObj = new JSONObject();
                                // DR Payment Gateway Charge
                                jObj.put("debit", true);
                                jObj.put("accountid", paymentGatewayChargeAccID);
                                jObj.put("customerVendorId", paymentGatewayChargeAccID);
                                jObj.put("description", "");
                                jObj.put("accountpersontype", 0);
                                jObj.put("rowid", 3);
                                jObj.put("amount", paymentGatewayCharge);
                                jObj.put("customfield", "[{}]");
                                jArr.put(jObj);

                                jObj = new JSONObject();
                                // DR Amount to seller
                                jObj.put("debit", true);
                                jObj.put("accountid", amountToSellerAccID);
                                jObj.put("customerVendorId", amountToSellerAccID);
                                jObj.put("description", "");
                                jObj.put("accountpersontype", 0);
                                jObj.put("rowid", 4);
                                jObj.put("amount", amountToSeller);
                                jObj.put("customfield", "[{}]");
                                jArr.put(jObj);

                                jObj = new JSONObject();
                                // Cr Sale
                                jObj.put("debit", false);
                                jObj.put("accountid", saleAccID);
                                jObj.put("customerVendorId", saleAccID);
                                jObj.put("description", "");
                                jObj.put("accountpersontype", 0);
                                jObj.put("rowid", 5);
                                jObj.put("amount", amount);
                                jObj.put("customfield", "[{}]");
                                jArr.put(jObj);

                                Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                                jeDataMap.put("companyid", companyid);
                                jeDataMap.put("createdby", userId);
                                jeDataMap.put("entrydate", createdAt);
                                jeDataMap.put("memo", "");
                                jeDataMap.put("entrynumber", invoiceNumber);
                                jeDataMap.put("autogenerated", false);
                                jeDataMap.put("currencyid", currencyId);
                                jeDataMap.put("externalCurrencyRate", 0.0);
                                jeDataMap.put("typevalue", 1);
                                jeDataMap.put("istemplate", 0);

                                KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                                JournalEntry je = (JournalEntry) jeresult.getEntityList().get(0);
                                KwlReturnObject jedresult = accJournalEntryobj.getJEDset(jArr, companyid, je);
                                HashSet jeDetails = (HashSet) jedresult.getEntityList().get(0);
                                je.setDetails(jeDetails);
                                String jeid = je.getID();

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
                                        accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                                    }
                                }

                                //Insert new entries again in optimized table.
                                accJournalEntryobj.saveAccountJEs_optimized(jeid);

                                Double jeAmount = 0.0;
                                int level = 0;

                                Set<JournalEntryDetail> jeDetail = je.getDetails();
                                for (JournalEntryDetail journalEntryDetail : jeDetail) {
                                    if (journalEntryDetail.isDebit()) { // As Debit and credit amount for JE are same , any one type can be picked for calculating amount
                                        jeAmount = jeAmount + journalEntryDetail.getAmount();
                                    }
                                }
                                int journalEntryType = je.getTypeValue();
                                String currentUserId = sessionHandlerImpl.getUserid(request);
                                if (journalEntryType != 2) { // Currently , Party Journal Entry is excluded from the approval rules. 
                                    int approvalStatusLevel = approveJE(je, sessionHandlerImpl.getCompanyid(request), level, String.valueOf(jeAmount), request, true, currentUserId);
                                }

                            }
                        }
                    } catch (Exception ex) {
                        if (isSecondHeaderExist) {
                            failed++;
                        }

                        String errorMsg = ex.getMessage();
                        if (!isFirstHeader) {
                            failedRecords.append("\n").append(createCSVrecord(recarr.toArray())).append("\"").append(errorMsg.replaceAll("\"", "")).append("\"");
                        } else {
                            isFirstHeader = false;
                            firstHeaderRecFailedMsg = errorMsg.replaceAll("\"", "");
                        }
                    }
                }
            }

            if (failed > 0) {
                createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = messageSource.getMessage("acc.field.Emptyfile", null, RequestContextUtils.getLocale(request));
            } else if (success == 0) {
                msg = messageSource.getMessage("acc.rem.169", null, RequestContextUtils.getLocale(request));
            } else if (success == total) {
                msg = messageSource.getMessage("acc.rem.168", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request))+ " " + success + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + messageSource.getMessage("acc.field.successfully.", null, RequestContextUtils.getLocale(request));
                msg += (failed == 0 ? "." : " "+messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request))+ " " + failed + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failed > 1 ? "s" : "") + ".");
            }

            try {
                txnManager.commit(tranStatus);
            } catch (Exception ex) {
                commitedEx = true;
                throw ex;
            }
        } catch (Exception ex) {
            if (!commitedEx) { // if exception occurs during commit then dont call rollback
                txnManager.rollback(tranStatus);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);

            try {
                // Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "xls");
                logDataMap.put("FailureFileType", failed > 0 ? "csv" : "");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Acc_GENERAL_LEDGER_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);

                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
                returnObj.put("Module", Constants.Acc_GENERAL_LEDGER_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
    
    /**
     * Description: Method is used to import Custom Journal Entry records for CSV.
     *
     * @param <request> used to get parameter from session
     * @param <jobj> used to get filename and file path which we have importing
     * @return JSONObject
     * @throws AccountingException
     * @throws IOException
     * @throws SessionExpiredException
     * @throws JSONException
     */
    public JSONObject importCSVjournalEntryRecords(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus tranStatus = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        int total = 0, failed = 0;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String currencyId = sessionHandlerImpl.getCurrencyID(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("name");
        String delimiterType = request.getParameter("delimiterType");
        CsvReader csvReader = null;

        String saleAccID = (request.getParameter("saleAccID") == null ? "" : request.getParameter("saleAccID"));
        String commissionAccID = (request.getParameter("commissionAccID") == null ? "" : request.getParameter("commissionAccID"));
        String commissionGSTAccID = (request.getParameter("commissionGSTAccID") == null ? "" : request.getParameter("commissionGSTAccID"));
        String paymentGatewayChargeAccID = (request.getParameter("paymentGatewayChargeAccID") == null ? "" : request.getParameter("paymentGatewayChargeAccID"));
        String amountToSellerAccID = (request.getParameter("amountToSellerAccID") == null ? "" : request.getParameter("amountToSellerAccID"));

        JSONObject returnObj = new JSONObject();

        try {
            // get selected dateformat
            String dateFormat = null, dateFormatId = request.getParameter("dateFormat");
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }

            DateFormat df = new SimpleDateFormat(dateFormat);
            df.setLenient(false);

            FileInputStream fs = new FileInputStream(jobj.getString("FilePath"));
            csvReader = new CsvReader(new InputStreamReader(fs), delimiterType);

            StringBuilder failedRecords = new StringBuilder();

            List<String> customFieldArrayList = new ArrayList<>();
            customFieldArrayList.add("Date From");
            customFieldArrayList.add("Date To");
            customFieldArrayList.add("Vendor ID");
            customFieldArrayList.add("Vendor");
            customFieldArrayList.add("Membership Type");
            customFieldArrayList.add("Order Number");
            customFieldArrayList.add("Order Date");
            customFieldArrayList.add("Order Completion Date");
            customFieldArrayList.add("Payment Method");
            customFieldArrayList.add("Status");

            List headArrayList = new ArrayList();
            headArrayList.add("Order Number");
            headArrayList.add("Invoice Number");
            headArrayList.add("Order Date");
            headArrayList.add("Order Completion Date");
            headArrayList.add("Amount");
            headArrayList.add("Payment Method");
            headArrayList.add("Status");

            failedRecords.append(createCSVrecord(headArrayList.toArray())).append("\"Error Message\"");

            HashMap<String, Integer> firstHeaderColumnConfig = new HashMap<>();
            HashMap<String, Integer> secondHeaderColumnConfig = new HashMap<>();
            boolean isFirstHeader = false, isSecondHeader = false, isSecondHeaderExist = false, isFirstHeaderRecFailed = false;
            String firstHeaderRecFailedMsg = "";
            Date fromDate = null, toDate = null, createdAt = null;
            String vendorID = "", vendor = "", membershipType = "";

            double amount = 0;
            String invoiceNumber = "", paymentMethod = "", status = "", orderNumber = "";
            Date orderDate = null, orderCompletionDate = null;

            while (csvReader.readRecord() ) {
                String[] recarr = csvReader.getValues();

                try {
                    if (isFirstHeader) {
                        if (firstHeaderColumnConfig.containsKey("dateFrom")) {
                            String cell = recarr[(Integer) firstHeaderColumnConfig.get("dateFrom")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(cell)) {
                                isFirstHeaderRecFailed = true;
                                throw new AccountingException("Date From is not available");
                            } else {
                                try {
                                    fromDate = df.parse(cell);
                                } catch (Exception ex) {
                                    isFirstHeaderRecFailed = true;
                                    throw new AccountingException("Incorrect date format for Date From, Please specify values in " + dateFormat + " format.");
                                }
                            }
                        } else {
                            isFirstHeaderRecFailed = true;
                            throw new AccountingException("Date From column is not found.");
                        }

                        if (firstHeaderColumnConfig.containsKey("dateTo")) {
                            String cell = recarr[(Integer) firstHeaderColumnConfig.get("dateTo")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(cell)) {
                                isFirstHeaderRecFailed = true;
                                throw new AccountingException("Date To is not available.");
                            } else {
                                try {
                                    toDate = df.parse(cell);
                                } catch (Exception ex) {
                                    isFirstHeaderRecFailed = true;
                                    throw new AccountingException("Incorrect date format for Date To, Please specify values in " + dateFormat + " format.");
                                }
                            }
                        } else {
                            isFirstHeaderRecFailed = true;
                            throw new AccountingException("Date To column is not found.");
                        }

                        if (firstHeaderColumnConfig.containsKey("vendorID")) {
                            String cell = recarr[(Integer) firstHeaderColumnConfig.get("vendorID")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(cell)) {
                                isFirstHeaderRecFailed = true;
                                throw new AccountingException("Vendor ID is not available");
                            } else {
                                vendorID = cell;
                            }
                        } else {
                            isFirstHeaderRecFailed = true;
                            throw new AccountingException("Vendor ID column is not found.");
                        }

                        if (firstHeaderColumnConfig.containsKey("vendor")) {
                            String cell = recarr[(Integer) firstHeaderColumnConfig.get("vendor")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(cell)) {
                                isFirstHeaderRecFailed = true;
                                throw new AccountingException("Vendor is not available");
                            } else {
                                vendor = cell;
                            }
                        } else {
                            isFirstHeaderRecFailed = true;
                            throw new AccountingException("Vendor column is not found.");
                        }

                        if (firstHeaderColumnConfig.containsKey("membershipType")) {
                            String cell = recarr[(Integer) firstHeaderColumnConfig.get("membershipType")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(cell)) {
                                isFirstHeaderRecFailed = true;
                                throw new AccountingException("Membership Type is not available");
                            } else {
                                membershipType = cell;
                            }
                        } else {
                            isFirstHeaderRecFailed = true;
                            throw new AccountingException("Membership Type column is not found.");
                        }

                        if (firstHeaderColumnConfig.containsKey("createdAt")) {
                            String cell = recarr[(Integer) firstHeaderColumnConfig.get("createdAt")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(cell)) {
                                isFirstHeaderRecFailed = true;
                                throw new AccountingException("Created At is not available.");
                            } else {
                                try {
                                    createdAt = df.parse(cell);
                                } catch (Exception ex) {
                                    isFirstHeaderRecFailed = true;
                                    throw new AccountingException("Incorrect date format for Created At, Please specify values in " + dateFormat + " format.");
                                }
                            }
                        } else {
                            isFirstHeaderRecFailed = true;
                            throw new AccountingException("Created At column is not found.");
                        }

                        isFirstHeader = false;
                        continue;
                    }

                    // for getting header index
                    for (int cellcount = 0; cellcount < csvReader.getColumnCount(); cellcount++) {
                        String cell = recarr[cellcount].replaceAll("\"", "").trim();

                        // for first header and getting its index
                        if (cell != null && !isFirstHeader && !isSecondHeader) {
                            if (cell.equalsIgnoreCase("Date From")) {
                                firstHeaderColumnConfig.put("dateFrom", cellcount);
                                int nextCount = 1;
                                cell = recarr[cellcount + nextCount].replaceAll("\"", "").trim();

                                if (cell != null && cell.equalsIgnoreCase("Date To")) {
                                    firstHeaderColumnConfig.put("dateTo", cellcount + nextCount);
                                    nextCount++;
                                    cell = recarr[cellcount + nextCount].replaceAll("\"", "").trim();

                                    if (cell != null && cell.equalsIgnoreCase("Vendor ID")) {
                                        firstHeaderColumnConfig.put("vendorID", cellcount + nextCount);
                                        nextCount++;
                                        cell = recarr[cellcount + nextCount].replaceAll("\"", "").trim();

                                        if (cell != null && cell.equalsIgnoreCase("Vendor")) {
                                            firstHeaderColumnConfig.put("vendor", cellcount + nextCount);
                                            nextCount++;
                                            cell = recarr[cellcount + nextCount].replaceAll("\"", "").trim();

                                            if (cell != null && cell.equalsIgnoreCase("Membership Type")) {
                                                firstHeaderColumnConfig.put("membershipType", cellcount + nextCount);
                                                nextCount++;
                                                cell = recarr[cellcount + nextCount].replaceAll("\"", "").trim();

                                                if (cell != null && cell.equalsIgnoreCase("Created At")) {
                                                    firstHeaderColumnConfig.put("createdAt", cellcount + nextCount);
                                                    isFirstHeader = true;
                                                    isFirstHeaderRecFailed = false;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }

                        // for second header and getting its index
                        if (cell != null && !isSecondHeader) {
                            if (cell.equalsIgnoreCase("Order Number")) {
                                secondHeaderColumnConfig.put("orderNumber", cellcount);
                                int nextCount = 1;
                                cell = recarr[cellcount + nextCount].replaceAll("\"", "").trim();

                                if (cell != null && cell.equalsIgnoreCase("Invoice Number")) {
                                    secondHeaderColumnConfig.put("invoiceNumber", cellcount + nextCount);
                                    nextCount++;
                                    cell = recarr[cellcount + nextCount].replaceAll("\"", "").trim();

                                    if (cell != null && cell.equalsIgnoreCase("Order Date")) {
                                        secondHeaderColumnConfig.put("orderDate", cellcount + nextCount);
                                        nextCount++;
                                        cell = recarr[cellcount + nextCount].replaceAll("\"", "").trim();

                                        if (cell != null && cell.equalsIgnoreCase("Order Completion Date")) {
                                            secondHeaderColumnConfig.put("orderCompletionDate", cellcount + nextCount);
                                            nextCount++;
                                            cell = recarr[cellcount + nextCount].replaceAll("\"", "").trim();

                                            if (cell != null && cell.equalsIgnoreCase("Amount")) {
                                                secondHeaderColumnConfig.put("amount", cellcount + nextCount);
                                                nextCount++;
                                                cell = recarr[cellcount + nextCount].replaceAll("\"", "").trim();

                                                if (cell != null && cell.equalsIgnoreCase("Payment Method")) {
                                                    secondHeaderColumnConfig.put("paymentMethod", cellcount + nextCount);
                                                    nextCount++;
                                                    cell = recarr[cellcount + nextCount].replaceAll("\"", "").trim();

                                                    if (cell != null && cell.equalsIgnoreCase("Status")) {
                                                        secondHeaderColumnConfig.put("status", cellcount + nextCount);
                                                        isSecondHeader = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isFirstHeader || (isSecondHeader && !isSecondHeaderExist)) {
                        if (isSecondHeader) {
                            isSecondHeaderExist = true;
                        }

                        continue;
                    } else {
                        if (isSecondHeaderExist) {
                            if (secondHeaderColumnConfig.containsKey("orderNumber")) {
                                String cell = recarr[(Integer) secondHeaderColumnConfig.get("orderNumber")].replaceAll("\"", "").trim();

                                if (StringUtil.isNullOrEmpty(cell)) { // 1st Blank cell
                                    int nextCount = 1;
                                    cell = recarr[(Integer) secondHeaderColumnConfig.get("orderNumber") + nextCount].replaceAll("\"", "").trim();
                                    if (StringUtil.isNullOrEmpty(cell)) { // 2nd Blank cell
                                        nextCount++;
                                        cell = recarr[(Integer) secondHeaderColumnConfig.get("orderNumber") + nextCount].replaceAll("\"", "").trim();
                                        if (StringUtil.isNullOrEmpty(cell)) { // 3rd Blank cell
                                            nextCount++;
                                            cell = recarr[(Integer) secondHeaderColumnConfig.get("orderNumber") + nextCount].replaceAll("\"", "").trim();
                                            if (!StringUtil.isNullOrEmpty(cell) && cell.equalsIgnoreCase("Total")) { // 4th cell contains 'Total'
                                                isSecondHeaderExist = false;
                                                isSecondHeader = false;
                                                continue;
                                            } else if (StringUtil.isNullOrEmpty(cell)) { // 4th Blank cell
                                                nextCount++;
                                                cell = recarr[(Integer) secondHeaderColumnConfig.get("orderNumber") + nextCount].replaceAll("\"", "").trim();
                                                if (StringUtil.isNullOrEmpty(cell)) { // 5th Blank cell
                                                    nextCount++;
                                                    cell = recarr[(Integer) secondHeaderColumnConfig.get("orderNumber") + nextCount].replaceAll("\"", "").trim();
                                                    if (StringUtil.isNullOrEmpty(cell)) { // 6th Blank cell
                                                        nextCount++;
                                                        cell = recarr[(Integer) secondHeaderColumnConfig.get("orderNumber") + nextCount].replaceAll("\"", "").trim();
                                                        if (StringUtil.isNullOrEmpty(cell)) { // 7th Blank cell
                                                            continue;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (isSecondHeaderExist) {
                                        total++;
                                        if (isFirstHeaderRecFailed) {
                                            throw new AccountingException(firstHeaderRecFailedMsg);
                                        }
                                        throw new AccountingException("Order Number is not available.");
                                    }
                                } else {
                                    total++;
                                    if (isFirstHeaderRecFailed) {
                                        throw new AccountingException(firstHeaderRecFailedMsg);
                                    }
                                    orderNumber = cell;
                                }
                            } else {
                                total++;
                                if (isFirstHeaderRecFailed) {
                                    throw new AccountingException(firstHeaderRecFailedMsg);
                                }
                                throw new AccountingException("Order Number column is not found.");
                            }

                            if (secondHeaderColumnConfig.containsKey("invoiceNumber")) {
                                String cell = recarr[(Integer) secondHeaderColumnConfig.get("invoiceNumber")].replaceAll("\"", "").trim();

                                if (StringUtil.isNullOrEmpty(cell)) {
                                    throw new AccountingException("Invoice Number is not available.");
                                } else {
                                    invoiceNumber = cell;
                                }
                            } else {
                                throw new AccountingException("Invoice Number column is not found.");
                            }

                            if (secondHeaderColumnConfig.containsKey("orderDate")) {
                                String cell = recarr[(Integer) secondHeaderColumnConfig.get("orderDate")].replaceAll("\"", "").trim();

                                if (StringUtil.isNullOrEmpty(cell)) {
                                    throw new AccountingException("Order Date is not available.");
                                } else {
                                    try {
                                        orderDate = df.parse(cell);
                                    } catch (Exception ex) {
                                        throw new AccountingException("Incorrect date format for Order Date, Please specify values in " + dateFormat + " format.");
                                    }
                                }
                            } else {
                                throw new AccountingException("Order Date column is not found.");
                            }

                            if (secondHeaderColumnConfig.containsKey("orderCompletionDate")) {
                                String cell = recarr[(Integer) secondHeaderColumnConfig.get("orderCompletionDate")].replaceAll("\"", "").trim();

                                if (StringUtil.isNullOrEmpty(cell)) {
                                    throw new AccountingException("Order Completion Date is not available.");
                                } else {
                                    try {
                                        orderCompletionDate = df.parse(cell);
                                    } catch (Exception ex) {
                                        throw new AccountingException("Incorrect date format for Order Completion Date, Please specify values in " + dateFormat + " format.");
                                    }
                                }
                            } else {
                                throw new AccountingException("Order Completion Date column is not found.");
                            }

                            if (secondHeaderColumnConfig.containsKey("amount")) {
                                String cell = recarr[(Integer) secondHeaderColumnConfig.get("amount")].replaceAll("\"", "").trim();

                                if (StringUtil.isNullOrEmpty(cell)) {
                                    throw new AccountingException("Amount is not available.");
                                } else {
                                    try {
                                        amount = Double.parseDouble(cell);
                                    } catch (Exception ex) {
                                        throw new AccountingException("Incorrect numeric value for Amount, Please ensure that value type of Amount matches with the Amount.");
                                    }
                                }
                            } else {
                                throw new AccountingException("Amount column is not found.");
                            }

                            if (secondHeaderColumnConfig.containsKey("paymentMethod")) {
                                String cell = recarr[(Integer) secondHeaderColumnConfig.get("paymentMethod")].replaceAll("\"", "").trim();

                                if (StringUtil.isNullOrEmpty(cell)) {
                                    throw new AccountingException("Payment Method is not available.");
                                } else {
                                    paymentMethod = cell;
                                }
                            } else {
                                throw new AccountingException("Payment Method column is not found.");
                            }

                            if (secondHeaderColumnConfig.containsKey("status")) {
                                String cell = recarr[(Integer) secondHeaderColumnConfig.get("status")].replaceAll("\"", "").trim();

                                if (StringUtil.isNullOrEmpty(cell)) {
                                    throw new AccountingException("Status is not available.");
                                } else {
                                    status = cell;
                                }
                            } else {
                                throw new AccountingException("Status column is not found.");
                            }

                            KwlReturnObject result = accJournalEntryobj.getJECount(invoiceNumber, companyid);
                            int nocount = result.getRecordTotalCount();
                            if (nocount > 0) {
                                throw new AccountingException("Journal entry number '" + invoiceNumber + "' already exists.");
                            }
                            if (!status.equalsIgnoreCase("Complete")) {
                                throw new AccountingException("Status of Journal entry number '" + invoiceNumber + "' is not Complete.");
                            }

                            // For create custom field array
                            JSONArray customJArr = new JSONArray();
                            for (int count = 0; count < customFieldArrayList.size(); count++) {
                                HashMap<String, Object> requestParams = new HashMap<>();
                                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                requestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_GENERAL_LEDGER_ModuleId, customFieldArrayList.get(count)));

                                KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
                                FieldParams params = null;
                                if (fieldParamsResult.getEntityList() != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                    params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                } else {
                                    throw new AccountingException("Custom Field for '" + customFieldArrayList.get(count) + "' column is not found.");
                                }

                                JSONObject customJObj = new JSONObject();
                                customJObj.put("fieldid", params.getId());
                                customJObj.put("filedid", params.getId());
                                customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                customJObj.put("xtype", params.getFieldtype());

                                String fieldComboDataStr = "";
                                if (params.getFieldtype() == 3) { // if field of date type
                                    if (fromDate != null && customFieldArrayList.get(count).equalsIgnoreCase("Date From")) {
                                        customJObj.put("Col" + params.getColnum(), fromDate.getTime());
                                        customJObj.put("fieldDataVal", fromDate.getTime());
                                    } else if (toDate != null && customFieldArrayList.get(count).equalsIgnoreCase("Date To")) {
                                        customJObj.put("Col" + params.getColnum(), toDate.getTime());
                                        customJObj.put("fieldDataVal", toDate.getTime());
                                    } else if (orderDate != null && customFieldArrayList.get(count).equalsIgnoreCase("Order Date")) {
                                        customJObj.put("Col" + params.getColnum(), orderDate.getTime());
                                        customJObj.put("fieldDataVal", orderDate.getTime());
                                    } else if (orderCompletionDate != null && customFieldArrayList.get(count).equalsIgnoreCase("Order Completion Date")) {
                                        customJObj.put("Col" + params.getColnum(), orderCompletionDate.getTime());
                                        customJObj.put("fieldDataVal", orderCompletionDate.getTime());
                                    }
                                } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                    if (!StringUtil.isNullOrEmpty(paymentMethod) && customFieldArrayList.get(count).equalsIgnoreCase("Payment Method")) {
                                        requestParams = new HashMap<>();
                                        requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                        requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), paymentMethod, 0));


                                        fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                        if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                            FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                            fieldComboDataStr += fieldComboData.getId() + ",";
                                        }
                                    }

                                    if (!StringUtil.isNullOrEmpty(membershipType) && customFieldArrayList.get(count).equalsIgnoreCase("Membership Type")) {
                                        requestParams = new HashMap<>();
                                        requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                        requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), membershipType, 0));


                                        fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                        if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                            FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                            fieldComboDataStr += fieldComboData.getId() + ",";
                                        }
                                    }

                                    if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                        customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                        customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                    } else {
                                        continue;
                                    }
                                } else {
                                    if (!StringUtil.isNullOrEmpty(vendorID) && customFieldArrayList.get(count).equalsIgnoreCase("Vendor ID")) {
                                        customJObj.put("Col" + params.getColnum(), vendorID);
                                        customJObj.put("fieldDataVal", vendorID);
                                    } else if (!StringUtil.isNullOrEmpty(vendor) && customFieldArrayList.get(count).equalsIgnoreCase("Vendor")) {
                                        customJObj.put("Col" + params.getColnum(), vendor);
                                        customJObj.put("fieldDataVal", vendor);
                                    } else if (!StringUtil.isNullOrEmpty(orderNumber) && customFieldArrayList.get(count).equalsIgnoreCase("Order Number")) {
                                        customJObj.put("Col" + params.getColnum(), orderNumber);
                                        customJObj.put("fieldDataVal", orderNumber);
                                    } else if (!StringUtil.isNullOrEmpty(status) && customFieldArrayList.get(count).equalsIgnoreCase("Status")) {
                                        customJObj.put("Col" + params.getColnum(), status);
                                        customJObj.put("fieldDataVal", status);
                                    }
                                }

                                customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());

                                customJArr.put(customJObj);
                            }

                            String customfield = customJArr.toString();

                            double commission = 0;
                            double commissionGST = 0;
                            if (membershipType.equalsIgnoreCase("Professional") || membershipType.equalsIgnoreCase("Seller")) { // calculte commission and commissoinGST for Membership Type - "Professional" OR "Seller"
                                commission = amount * 0.13;
                                commission = authHandler.round(commission, companyid);

                                commissionGST = (amount * 0.13) * (0.07);
                                commissionGST = authHandler.round(commissionGST, companyid);
                            } else if (membershipType.equalsIgnoreCase("Deluxe") || membershipType.equalsIgnoreCase("BSSeller")) { // calculte commission and commissoinGST for Membership Type - "Deluxe" OR "BSSeller"
                                commission = amount * 0.12;
                                commission = authHandler.round(commission, companyid);

                                commissionGST = (amount * 0.12) * (0.07);
                                commissionGST = authHandler.round(commissionGST, companyid);
                            } else if (membershipType.equalsIgnoreCase("Basic") || membershipType.equalsIgnoreCase("SpecialBSSeller")) { // calculte commission and commissoinGST for Membership Type - "Basic" OR "SpecialBSSeller"
                                commission = amount * 0.10;
                                commission = authHandler.round(commission, companyid);

                                commissionGST = (amount * 0.10) * (0.07);
                                commissionGST = authHandler.round(commissionGST, companyid);
                            } else if (membershipType.equalsIgnoreCase("9percent")) { // calculte commission and commissoinGST for Membership Type - "9percent"
                                commission = amount * 0.09;
                                commission = authHandler.round(commission, companyid);

                                commissionGST = (amount * 0.09) * (0.07);
                                commissionGST = authHandler.round(commissionGST, companyid);
                            } else if (membershipType.equalsIgnoreCase("BugisStreet") || membershipType.equalsIgnoreCase("BSO")) { // calculte commission and commissoinGST for Membership Type - "BugisStreet" OR "BSO"
                                commission = 0;

                                commissionGST = 0;
                            }

                            double paymentGatewayCharge = 0;
                            if (paymentMethod.equalsIgnoreCase("Visa") || paymentMethod.equalsIgnoreCase("MasterCard")) {
                                paymentGatewayCharge = amount * 0.023;
                                paymentGatewayCharge = authHandler.round(paymentGatewayCharge, companyid);
                            } else if (paymentMethod.equalsIgnoreCase("eNETS")) {
                                paymentGatewayCharge = amount * 0.025;
                                paymentGatewayCharge = authHandler.round(paymentGatewayCharge, companyid);

                                if (1.50 > paymentGatewayCharge) {
                                    paymentGatewayCharge = 1.50;
                                }
                            }

                            double amountToSeller = amount - commission - commissionGST - paymentGatewayCharge;

                            // for save journal entry
                            JSONArray jArr = new JSONArray();
                            JSONObject jObj = new JSONObject();

                            // DR Commission
                            jObj.put("debit", true);
                            jObj.put("accountid", commissionAccID);
                            jObj.put("customerVendorId", commissionAccID);
                            jObj.put("description", "");
                            jObj.put("accountpersontype", 0);
                            jObj.put("rowid", 1);
                            jObj.put("amount", commission);
                            jObj.put("customfield", "[{}]");
                            jArr.put(jObj);

                            jObj = new JSONObject();
                            // DR Commission GST
                            jObj.put("debit", true);
                            jObj.put("accountid", commissionGSTAccID);
                            jObj.put("customerVendorId", commissionGSTAccID);
                            jObj.put("description", "");
                            jObj.put("accountpersontype", 0);
                            jObj.put("rowid", 2);
                            jObj.put("amount", commissionGST);
                            jObj.put("customfield", "[{}]");
                            jArr.put(jObj);

                            jObj = new JSONObject();
                            // DR Payment Gateway Charge
                            jObj.put("debit", true);
                            jObj.put("accountid", paymentGatewayChargeAccID);
                            jObj.put("customerVendorId", paymentGatewayChargeAccID);
                            jObj.put("description", "");
                            jObj.put("accountpersontype", 0);
                            jObj.put("rowid", 3);
                            jObj.put("amount", paymentGatewayCharge);
                            jObj.put("customfield", "[{}]");
                            jArr.put(jObj);

                            jObj = new JSONObject();
                            // DR Amount to seller
                            jObj.put("debit", true);
                            jObj.put("accountid", amountToSellerAccID);
                            jObj.put("customerVendorId", amountToSellerAccID);
                            jObj.put("description", "");
                            jObj.put("accountpersontype", 0);
                            jObj.put("rowid", 4);
                            jObj.put("amount", amountToSeller);
                            jObj.put("customfield", "[{}]");
                            jArr.put(jObj);

                            jObj = new JSONObject();
                            // Cr Sale
                            jObj.put("debit", false);
                            jObj.put("accountid", saleAccID);
                            jObj.put("customerVendorId", saleAccID);
                            jObj.put("description", "");
                            jObj.put("accountpersontype", 0);
                            jObj.put("rowid", 5);
                            jObj.put("amount", amount);
                            jObj.put("customfield", "[{}]");
                            jArr.put(jObj);

                            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                            jeDataMap.put("companyid", companyid);
                            jeDataMap.put("createdby", userId);
                            jeDataMap.put("entrydate", createdAt);
                            jeDataMap.put("memo", "");
                            jeDataMap.put("entrynumber", invoiceNumber);
                            jeDataMap.put("autogenerated", false);
                            jeDataMap.put("currencyid", currencyId);
                            jeDataMap.put("externalCurrencyRate", 0.0);
                            jeDataMap.put("typevalue", 1);
                            jeDataMap.put("istemplate", 0);

                            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                            JournalEntry je = (JournalEntry) jeresult.getEntityList().get(0);
                            KwlReturnObject jedresult = accJournalEntryobj.getJEDset(jArr, companyid, je);
                            HashSet jeDetails = (HashSet) jedresult.getEntityList().get(0);
                            je.setDetails(jeDetails);
                            String jeid = je.getID();

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
                                    accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                                }
                            }

                            //Insert new entries again in optimized table.
                            accJournalEntryobj.saveAccountJEs_optimized(jeid);

                            Double jeAmount = 0.0;
                            int level = 0;

                            Set<JournalEntryDetail> jeDetail = je.getDetails();
                            for (JournalEntryDetail journalEntryDetail : jeDetail) {
                                if (journalEntryDetail.isDebit()) { // As Debit and credit amount for JE are same , any one type can be picked for calculating amount
                                    jeAmount = jeAmount + journalEntryDetail.getAmount();
                                }
                            }
                            int journalEntryType = je.getTypeValue();
                            String currentUserId = sessionHandlerImpl.getUserid(request);
                            if (journalEntryType != 2) { // Currently , Party Journal Entry is excluded from the approval rules. 
                                int approvalStatusLevel = approveJE(je, sessionHandlerImpl.getCompanyid(request), level, String.valueOf(jeAmount), request, true, currentUserId);
                            }

                        }
                    }
                } catch (Exception ex) {
                    if (isSecondHeaderExist) {
                        failed++;
                    }

                    String errorMsg = ex.getMessage();
                    if (!isFirstHeader) {
                        failedRecords.append("\n").append(createCSVrecord(recarr)).append("\"").append(errorMsg.replaceAll("\"", "")).append("\"");
                    } else {
                        isFirstHeader = false;
                        firstHeaderRecFailedMsg = errorMsg.replaceAll("\"", "");
                    }
                }

            }

            if (failed > 0) {
                createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = messageSource.getMessage("acc.field.Emptyfile", null, RequestContextUtils.getLocale(request));
            } else if (success == 0) {
                msg = messageSource.getMessage("acc.rem.169", null, RequestContextUtils.getLocale(request));
            } else if (success == total) {
                msg = messageSource.getMessage("acc.rem.168", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request))+ " " + success + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + messageSource.getMessage("acc.field.successfully.", null, RequestContextUtils.getLocale(request));
                msg += (failed == 0 ? "." : " "+messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request))+ " " + failed + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failed > 1 ? "s" : "") + ".");
            }

            try {
                txnManager.commit(tranStatus);
            } catch (Exception ex) {
                commitedEx = true;
                throw ex;
            }
        } catch (Exception ex) {
            if (!commitedEx) { // if exception occurs during commit then dont call rollback
                txnManager.rollback(tranStatus);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);

            try {
                // Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("FailureFileType", failed > 0 ? "csv" : "");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Acc_GENERAL_LEDGER_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);

                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
                returnObj.put("Module", Constants.Acc_GENERAL_LEDGER_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
    
    public JSONObject importXLSRefundJournalEntryRecords(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        int total = 0, failed = 0;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String currencyId = sessionHandlerImpl.getCurrencyID(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("filename");

        JSONObject returnObj = new JSONObject();

        try {
            int sheetNo = 0; // to import data at sheet no 1

            FileInputStream fs = new FileInputStream(jobj.getString("file"));

            Workbook wb = WorkbookFactory.create(fs);
//            HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
            Sheet sheet = wb.getSheetAt(sheetNo);

            StringBuilder failedRecords = new StringBuilder();

            List<String> customFieldArrayList = new ArrayList<String>();
            customFieldArrayList.add("Date From");
            customFieldArrayList.add("Date To");
            customFieldArrayList.add("Vendor ID");
            customFieldArrayList.add("Vendor");
            customFieldArrayList.add("Order Number");
            customFieldArrayList.add("Order Date");
            customFieldArrayList.add("Refund Closed Date");

            List headArrayList = new ArrayList();
            headArrayList.add("Order Number");
            headArrayList.add("Invoice Number");
            headArrayList.add("Order Date");
            headArrayList.add("Refund Date");
            headArrayList.add("Refund Closed Date");
            headArrayList.add("Remark");

            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\"");
            int maxCol = 0;

            HashMap<String, Integer> firstHeaderColumnConfig = new HashMap<String, Integer>();
            HashMap<String, Integer> secondHeaderColumnConfig = new HashMap<String, Integer>();
            boolean isFirstHeader = false, isSecondHeader = false, isSecondHeaderExist = false;
            Date fromDate = null, toDate = null, createdAt = null;
            String vendorID = "", vendor = "", membershipType = "";

            int orderNumber = 0;
            double amount = 0;
            String invoiceNumber = "", paymentMethod = "", type = "", remark = "";
            Date orderDate = null, refundDate = null, refundClosedDate = null;

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    maxCol = row.getLastCellNum();

                    List recarr = new ArrayList();

                    try {
                        if (isFirstHeader) {

                            if (firstHeaderColumnConfig.containsKey("dateFrom")) {
                                Cell cell = row.getCell((Integer) firstHeaderColumnConfig.get("dateFrom"));

                                if (cell == null) {
                                    throw new AccountingException("Date From is not available");
                                }

                                fromDate = cell.getDateCellValue();
                            } else {
                                throw new AccountingException("Date From column is not found.");
                            }

                            if (firstHeaderColumnConfig.containsKey("dateTo")) {
                                Cell cell = row.getCell((Integer) firstHeaderColumnConfig.get("dateTo"));

                                if (cell == null) {
                                    throw new AccountingException("Date To is not available.");
                                }

                                toDate = cell.getDateCellValue();
                            } else {
                                throw new AccountingException("Date To column is not found.");
                            }

                            if (firstHeaderColumnConfig.containsKey("vendorID")) {
                                Cell cell = row.getCell((Integer) firstHeaderColumnConfig.get("vendorID"));

                                if (cell == null) {
                                    throw new AccountingException("vendor ID is not available");
                                }

                                switch (cell.getCellType()) {
                                    case Cell.CELL_TYPE_NUMERIC:
                                        vendorID = Integer.toString((int) cell.getNumericCellValue());
                                        break;
                                    case Cell.CELL_TYPE_STRING:
                                        vendorID = cell.getStringCellValue().trim();
                                        break;
                                }

                            } else {
                                throw new AccountingException("vendor ID column is not found.");
                            }

                            if (firstHeaderColumnConfig.containsKey("vendor")) {
                                Cell cell = row.getCell((Integer) firstHeaderColumnConfig.get("vendor"));

                                if (cell == null) {
                                    throw new AccountingException("vendor is not available");
                                }

                                vendor = cell.getStringCellValue().trim();
                            } else {
                                throw new AccountingException("vendor column is not found.");
                            }

                            if (firstHeaderColumnConfig.containsKey("createdAt")) {
                                Cell cell = row.getCell((Integer) firstHeaderColumnConfig.get("createdAt"));

                                if (cell == null) {
                                    throw new AccountingException("Created At is not available.");
                                }

                                createdAt = cell.getDateCellValue();
                            } else {
                                throw new AccountingException("Created At column is not found.");
                            }

                            isFirstHeader = false;
                            continue;
                        }

                        // for getting header index
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            Cell cell = row.getCell(cellcount);

                            // for first header and getting its index
                            if (cell != null && !isFirstHeader && !isSecondHeader) {
                                if (cell.getStringCellValue().trim().equalsIgnoreCase("Date From")) {
                                    firstHeaderColumnConfig.put("dateFrom", cellcount);
                                    int nextCount = 1;
                                    cell = row.getCell(cellcount + nextCount);

                                    if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Date To")) {
                                        firstHeaderColumnConfig.put("dateTo", cellcount + nextCount);
                                        nextCount++;
                                        cell = row.getCell(cellcount + nextCount);

                                        if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Vendor ID")) {
                                            firstHeaderColumnConfig.put("vendorID", cellcount + nextCount);
                                            nextCount++;
                                            cell = row.getCell(cellcount + nextCount);

                                            if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Vendor")) {
                                                firstHeaderColumnConfig.put("vendor", cellcount + nextCount);
                                                nextCount++;
                                                cell = row.getCell(cellcount + nextCount);

                                                if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Created At")) {
                                                    firstHeaderColumnConfig.put("createdAt", cellcount + nextCount);
                                                    isFirstHeader = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }

                            }

                            // for second header and getting its index
                            if (cell != null && !isSecondHeader) {
                                if (cell.getStringCellValue().trim().equalsIgnoreCase("Order Number")) {
                                    secondHeaderColumnConfig.put("orderNumber", cellcount);
                                    int nextCount = 1;
                                    cell = row.getCell(cellcount + nextCount);

                                    if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Invoice Number")) {
                                        secondHeaderColumnConfig.put("invoiceNumber", cellcount + nextCount);
                                        nextCount++;
                                        cell = row.getCell(cellcount + nextCount);

                                        if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Order Date")) {
                                            secondHeaderColumnConfig.put("orderDate", cellcount + nextCount);
                                            nextCount++;
                                            cell = row.getCell(cellcount + nextCount);

                                            if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Refund Date")) {
                                                secondHeaderColumnConfig.put("refundDate", cellcount + nextCount);
                                                nextCount++;
                                                cell = row.getCell(cellcount + nextCount);

                                                if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Refund Closed Date")) {
                                                    secondHeaderColumnConfig.put("refundClosedDate", cellcount + nextCount);
                                                    nextCount++;
                                                    cell = row.getCell(cellcount + nextCount);

                                                    if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Remark")) {
                                                        secondHeaderColumnConfig.put("remark", cellcount + nextCount);
                                                        isSecondHeader = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }

                        if (isFirstHeader || (isSecondHeader && !isSecondHeaderExist)) {
                            if (isSecondHeader) {
                                isSecondHeaderExist = true;
                            }

                            continue;
                        } else {
                            if (isSecondHeaderExist) {

                                // to read record for generation of failure file of import
                                for (int recCount = 0; recCount < secondHeaderColumnConfig.size(); recCount++) {
                                    Cell cell = row.getCell(recCount);

                                    if (cell != null) {
                                        switch (cell.getCellType()) {
                                            case Cell.CELL_TYPE_NUMERIC:
                                                if (DateUtil.isCellDateFormatted(cell)) {
                                                    Date date1 = DateUtil.getJavaDate(cell.getNumericCellValue());
                                                    DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                                    recarr.add(sdf.format(date1));
                                                } else {
                                                    recarr.add(Integer.toString((int) cell.getNumericCellValue()));
                                                }
                                                break;
                                            case Cell.CELL_TYPE_STRING:
                                                recarr.add(cell.getStringCellValue().trim());
                                                break;
                                        }
                                    } else {
                                        recarr.add("");
                                    }
                                }

                                if (secondHeaderColumnConfig.containsKey("orderNumber")) {
                                    Cell cell = row.getCell((Integer) secondHeaderColumnConfig.get("orderNumber"));

                                    if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                        int nextCount = 1;
                                        cell = row.getCell((Integer) secondHeaderColumnConfig.get("orderNumber") + nextCount);
                                        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                            nextCount++;
                                            cell = row.getCell((Integer) secondHeaderColumnConfig.get("orderNumber") + nextCount);
                                            if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK || cell.getStringCellValue().trim().equalsIgnoreCase("Total")) {
                                                isSecondHeaderExist = false;
                                                isSecondHeader = false;
                                                continue;
                                            }
                                        }
                                        if (isSecondHeaderExist) {
                                            throw new AccountingException("Order Number is not available.");
                                        }
                                    }

                                    orderNumber = (int) cell.getNumericCellValue();
                                } else {
                                    throw new AccountingException("Order Number column is not found.");
                                }

                                total++;

                                if (secondHeaderColumnConfig.containsKey("invoiceNumber")) {
                                    Cell cell = row.getCell((Integer) secondHeaderColumnConfig.get("invoiceNumber"));

                                    if (cell == null || StringUtil.isNullOrEmpty(cell.getStringCellValue().trim())) {
                                        throw new AccountingException("Invoice Number is not available.");
                                    }

                                    invoiceNumber = cell.getStringCellValue().trim();
                                } else {
                                    throw new AccountingException("Invoice Number column is not found.");
                                }

                                if (secondHeaderColumnConfig.containsKey("orderDate")) {
                                    Cell cell = row.getCell((Integer) secondHeaderColumnConfig.get("orderDate"));

                                    if (cell == null) {
                                        throw new AccountingException("Order Date is not available.");
                                    }

                                    orderDate = cell.getDateCellValue();
                                } else {
                                    throw new AccountingException("Order Date column is not found.");
                                }

                                if (secondHeaderColumnConfig.containsKey("refundDate")) {
                                    Cell cell = row.getCell((Integer) secondHeaderColumnConfig.get("refundDate"));

                                    if (cell == null) {
                                        throw new AccountingException("Refund Date is not available.");
                                    }

                                    refundDate = cell.getDateCellValue();
                                } else {
                                    throw new AccountingException("Refund Date column is not found.");
                                }

                                if (secondHeaderColumnConfig.containsKey("refundClosedDate")) {
                                    Cell cell = row.getCell((Integer) secondHeaderColumnConfig.get("refundClosedDate"));

                                    if (cell == null) {
                                        throw new AccountingException("Refund Closed Date is not available.");
                                    }

                                    refundClosedDate = cell.getDateCellValue();
                                } else {
                                    throw new AccountingException("Refund Closed Date column is not found.");
                                }

                                if (secondHeaderColumnConfig.containsKey("remark")) {
                                    Cell cell = row.getCell((Integer) secondHeaderColumnConfig.get("remark"));

                                    if (cell != null) {
                                        remark = cell.getStringCellValue().trim();
                                    }

                                } else {
                                    throw new AccountingException("Remark column is not found.");
                                }

                                KwlReturnObject result = accJournalEntryobj.getJECount(invoiceNumber, companyid);
                                int nocount = result.getRecordTotalCount();
                                if (nocount > 0) {
                                    throw new AccountingException("Journal entry number '" + invoiceNumber + "' already exists.");
                                }

                                // For create custom field array
                                JSONArray customJArr = new JSONArray();
                                for (int count = 0; count < customFieldArrayList.size(); count++) {
                                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                    requestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_GENERAL_LEDGER_ModuleId, customFieldArrayList.get(count)));

                                    KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
                                    FieldParams params = null;
                                    if (fieldParamsResult.getEntityList() != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                        params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                    } else {
                                        throw new AccountingException("Create Custom Field for " + customFieldArrayList.get(count) + " column is not found.");
                                    }

                                    JSONObject customJObj = new JSONObject();
                                    customJObj.put("fieldid", params.getId());
                                    customJObj.put("filedid", params.getId());
                                    customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                    customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                    customJObj.put("xtype", params.getFieldtype());

                                    if (params.getFieldtype() == 3) { // if field of date type
                                        if (fromDate != null && customFieldArrayList.get(count).equalsIgnoreCase("Date From")) {
                                            customJObj.put("Col" + params.getColnum(), fromDate.getTime());
                                            customJObj.put("fieldDataVal", fromDate.getTime());
                                        } else if (toDate != null && customFieldArrayList.get(count).equalsIgnoreCase("Date To")) {
                                            customJObj.put("Col" + params.getColnum(), toDate.getTime());
                                            customJObj.put("fieldDataVal", toDate.getTime());
                                        } else if (orderDate != null && customFieldArrayList.get(count).equalsIgnoreCase("Order Date")) {
                                            customJObj.put("Col" + params.getColnum(), orderDate.getTime());
                                            customJObj.put("fieldDataVal", orderDate.getTime());
                                        } else if (refundClosedDate != null && customFieldArrayList.get(count).equalsIgnoreCase("Refund Closed Date")) {
                                            customJObj.put("Col" + params.getColnum(), refundClosedDate.getTime());
                                            customJObj.put("fieldDataVal", refundClosedDate.getTime());
                                        }
                                    } else {
                                        if (!StringUtil.isNullOrEmpty(vendorID) && customFieldArrayList.get(count).equalsIgnoreCase("Vendor ID")) {
                                            customJObj.put("Col" + params.getColnum(), vendorID);
                                            customJObj.put("fieldDataVal", vendorID);
                                        } else if (!StringUtil.isNullOrEmpty(vendor) && customFieldArrayList.get(count).equalsIgnoreCase("Vendor")) {
                                            customJObj.put("Col" + params.getColnum(), vendor);
                                            customJObj.put("fieldDataVal", vendor);
                                        } else if (orderNumber != 0 && customFieldArrayList.get(count).equalsIgnoreCase("Order Number")) {
                                            customJObj.put("Col" + params.getColnum(), orderNumber);
                                            customJObj.put("fieldDataVal", orderNumber);
                                        }
                                    }

                                    customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());

                                    customJArr.put(customJObj);
                                }

                                String customfield = customJArr.toString();

//                                String entryNo = invoiceNumber.replace("R", "");
                                if (!invoiceNumber.substring(0, 1).equalsIgnoreCase("R")) {
                                    throw new AccountingException("Invoice Number should be start with 'R'.");
                                }
                                String entryNo = invoiceNumber.substring(1);

                                JSONArray jArr = new JSONArray();
                                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                requestParams.put("companyID", companyid);
                                requestParams.put("entryNumber", entryNo);
                                KwlReturnObject jeresult = accJournalEntryobj.getJEforRefund(requestParams);

                                if (jeresult.getEntityList() != null && !jeresult.getEntityList().isEmpty()) {
                                    JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

                                    Set<JournalEntryDetail> journalEntryDetails = journalEntry.getDetails();
                                    for (JournalEntryDetail journalEntryDetail : journalEntryDetails) {
                                        JSONObject object = new JSONObject();
                                        object.put("accountid", journalEntryDetail.getAccount().getID());
                                        object.put("amount", journalEntryDetail.getAmount());
                                        object.put("description", journalEntryDetail.getDescription());
                                        object.put("debit", (journalEntryDetail.isDebit()) ? false : true);
                                        object.put("srno", journalEntryDetail.getSrno());
                                        jArr.put(object);
                                    }

//                                    KwlReturnObject jedresult = accJournalEntryobj.getJEDset(jArr, companyid);
//                                    HashSet jeDetails = (HashSet) jedresult.getEntityList().get(0);

                                    double externalCurrencyRate = journalEntry.getExternalCurrencyRate();
                                    DateFormat df = authHandler.getDateOnlyFormat(request);

                                    Map<String, Object> jeDataMap = new HashMap<String, Object>();
                                    jeDataMap.put("df", df);
                                    jeDataMap.put("companyid", companyid);
                                    jeDataMap.put("entrydate", refundDate); // new Date());
                                    jeDataMap.put("memo", remark); // StringUtil.isNullOrEmpty(journalEntry.getMemo()) ? "" : journalEntry.getMemo());
                                    jeDataMap.put("entrynumber", invoiceNumber);
                                    jeDataMap.put("currencyid", (journalEntry.getCurrency() != null) ? journalEntry.getCurrency().getCurrencyID() : null);
                                    jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                                    jeDataMap.put("reversejournalentry", journalEntry.getID().toString());
                                    jeDataMap.put("isreverseje", true);
//                                    jeDataMap.put("jedetails", jeDetails);

                                    KwlReturnObject rjeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                                    JournalEntry je = (JournalEntry) rjeresult.getEntityList().get(0);
                                    KwlReturnObject jedresult = accJournalEntryobj.getJEDset(jArr, companyid,je);
                                    HashSet jeDetails = (HashSet) jedresult.getEntityList().get(0);
                                    je.setDetails(jeDetails);
                                    accJournalEntryobj.updateReverseJournalEntryValue(journalEntry, je.getID().toString());
                                    auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User " + sessionHandlerImpl.getUserFullName(request) + " has reversed journal entry " + je.getEntryNumber(), request, je.getID());

                                    // to save custom fields
                                    if (!StringUtil.isNullOrEmpty(customfield)) {
                                        JSONArray jcustomarray = new JSONArray(customfield);
                                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                                        customrequestParams.put("customarray", jcustomarray);
                                        customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                                        customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                                        customrequestParams.put("modulerecid", je.getID());
                                        customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                        customrequestParams.put("companyid", companyid);
                                        customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                            Map<String, Object> customjeDataMap = new HashMap<String, Object>();
                                            customjeDataMap.put("accjecustomdataref", je.getID());
                                            customjeDataMap.put("jeid", je.getID());
                                            customjeDataMap.put("istemplate", 0);
                                            jeresult = accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                                        }
                                    }

                                    // Insert new entries again in optimized table.
                                    accJournalEntryobj.saveAccountJEs_optimized(je.getID());
                                } else {
                                    throw new AccountingException("Journal entry number not exists.");
                                }
                            }
                        }
                    } catch (Exception ex) {
                        if (isSecondHeaderExist) {
                            failed++;
                        }

                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) {
                        }
                        failedRecords.append("\n" + createCSVrecord(recarr.toArray()) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                }
            }

            if (failed > 0) {
                createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = messageSource.getMessage("acc.field.Emptyfile", null, RequestContextUtils.getLocale(request));
            } else if (success == 0) {
                msg = messageSource.getMessage("acc.rem.169", null, RequestContextUtils.getLocale(request));
            } else if (success == total) {
                msg = messageSource.getMessage("acc.rem.168", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request))+ " " + success + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + messageSource.getMessage("acc.field.successfully.", null, RequestContextUtils.getLocale(request));
                msg += (failed == 0 ? "." : " "+messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request))+ " " + failed + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failed > 1 ? "s" : "") + ".");
            }

            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                commitedEx = true;
                throw ex;
            }
        } catch (Exception ex) {
            if (!commitedEx) { // if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);

            try {
                // Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "xls");
                logDataMap.put("FailureFileType", failed>0?"csv":"");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Acc_GENERAL_LEDGER_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);

                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
                returnObj.put("Module", Constants.Acc_GENERAL_LEDGER_ModuleId);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }

    public static String getActualFileName(String storageName) {
        String ext = storageName.substring(storageName.lastIndexOf("."));
        String actualName = storageName.substring(0, storageName.lastIndexOf("_"));
        actualName = actualName + ext;
        return actualName;
    }

    public String createCSVrecord(Object[] listArray) {
        String rec = "";
        for (int i = 0; i < listArray.length; i++) { // Discard columns id at index 0 and isvalid,invalidColumns, validationlog at last 3 indexes.
            rec += "\"" + (listArray[i] == null ? "" : listArray[i].toString().replaceAll("\"", "")) + "\",";
        }
        return rec;
    }

    public void createFailureFiles(String filename, StringBuilder failedRecords, String ext) {
        String destinationDirectory;
        try {
            if (ext.equals(".xls")) {
                destinationDirectory = storageHandlerImpl.GetDocStorePath() + "xlsfiles";
            } else {
                destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            }                            
            if (StringUtil.isNullOrEmpty(ext)) {
                ext = filename.substring(filename.lastIndexOf("."));
            }
            filename = filename.substring(0, filename.lastIndexOf("."));

            java.io.FileOutputStream failurefileOut = new java.io.FileOutputStream(destinationDirectory + "/" + filename + ImportLog.failureTag + ext);
            failurefileOut.write(failedRecords.toString().getBytes());
            failurefileOut.flush();
            failurefileOut.close();
        } catch (Exception ex) {
            System.out.println("\nError file write [success/failed] " + ex);
        }
    }
    
    public int approveJE(JournalEntry JE, String companyid, int level, String amount, HttpServletRequest request, boolean fromCreate, String currentUser) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException {
        boolean hasAuthority = false;
        if (!fromCreate) { // check if the currently logged in user has authority or not to approve pending JE...but only in case when this method is called from approveJournalEntry method 
            String thisUser = currentUser;
            KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), thisUser);
            User user = (User) userclass.getEntityList().get(0);
            if (AccountingManager.isCompanyAdmin(user)) {
                hasAuthority = true;
            } else {
                hasAuthority = accJournalEntryobj.checkForRule(level, companyid, amount, thisUser);
            }
        } else {
            hasAuthority = true;
        }
        if (hasAuthority) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            String requisitionApprovalSubject = "Journal Entry: %s - Approval Notification";
            String requisitionApprovalHtmlMsg = "<html><head><title>Deskera Accounting - Your Deskera Account</title></head><style type='text/css'>"
                    + "a:link, a:visited, a:active {\n"
                    + " 	color: #03C;"
                    + "}\n"
                    + "body {\n"
                    + "	font-family: Arial, Helvetica, sans-serif;"
                    + "	color: #000;"
                    + "	font-size: 13px;"
                    + "}\n"
                    + "</style><body>"
                    + "<p>Hi All,</p>"
                    + "<p></p>"
                    + "<p>%s has created journal entry %S and sent it to you for approval.</p>"
                    + "<p>Please review and approve it (Journal Entry Number: %s).</p>"
                    + "<p>Company Name:- %s</p>"
                    + "<p>Please check on Url:- %s</p>"
                    + "<p></p>"
                    + "<p>Thanks</p>"
                    + "<p>This is an auto generated email. Do not reply<br>";
            String requisitionApprovalPlainMsg = "Hi All,\n\n"
                    + "%s has created journal entry %S and sent it to you for approval.\n"
                    + "Please review and approve it (Journal Entry Number: %s).\n\n"
                    + "Company Name:- %s \n"
                    + "Please check on Url:- %s \n\n"
                    + "Thanks\n\n"
                    + "This is an auto generated email. Do not reply\n";
            int approvalStatus = 11;
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            String jeNumber = JE.getEntryNumber();
            String jeID = JE.getID();
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put("companyid", companyid);
            qdDataMap.put("level", level + 1);
            qdDataMap.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
            Iterator itr = flowresult.getEntityList().iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                String rule = row[2].toString();
                String creator = (!StringUtil.isNullOrEmpty(row[6].toString())) ? row[6].toString() : "";
                boolean sendForApproval = false;
                String[] creators = creator.split(",");
                for (int i = 0; i < creators.length; i++) {
                    if (creators[i].equals(sessionHandlerImpl.getUserid(request))) {
                        sendForApproval = true;
                        break;
                    }
                }
                rule = rule.replaceAll("[$$]+", amount);
                /*
                 * send mail to approvers on any of one below condition is true
                 * - 1 - If level exist and rule is not set 2 - If level and
                 * expression rule exist 3 - If creator rule exist
                 */
                if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && Boolean.parseBoolean(engine.eval(rule).toString())) || sendForApproval) {
                    // send emails
                    try {
                        if (Boolean.parseBoolean(row[3].toString()) && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
                            String fromName = "User";
                            //String fromEmailId = Constants.ADMIN_EMAILID;
                            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                            Company company = (Company) returnObject.getEntityList().get(0);
                            String fromEmailId = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                            fromName = sessionHandlerImpl.getUserName(request);
//                            String fromEmailId1 = StringUtil.isNullOrEmpty(JE.getCompany().getEmailID()) ? authHandlerDAOObj.getSysEmailIdByCompanyID(JE.getCompany().getCompanyID()) : JE.getCompany().getEmailID();
//                            if (!StringUtil.isNullOrEmpty(fromEmailId1)) {
//                                fromEmailId = fromEmailId1;
//                            }
                            String pageURL = URLUtil.getPageURL(request, loginpageFull);
                            String subject = String.format(requisitionApprovalSubject, jeNumber);
                            String htmlMsg = String.format(requisitionApprovalHtmlMsg, fromName, jeNumber, jeNumber, company.getCompanyName(), pageURL);
                            String plainMsg = String.format(requisitionApprovalPlainMsg, fromName, jeNumber, jeNumber, company.getCompanyName(), pageURL);
                            ArrayList<String> emailArray = new ArrayList<String>();
                            String[] emails = {};
                            qdDataMap.put("ruleid", row[0].toString());
                            KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(qdDataMap);
                            Iterator useritr = userResult.getEntityList().iterator();
                            while (useritr.hasNext()) {
                                Object[] userrow = (Object[]) useritr.next();
                                emailArray.add(userrow[3].toString());
                            }
                            emails = emailArray.toArray(emails);
                            if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                                String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                                emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
                            }
                            if (emails.length > 0) {
                                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                                SendMailHandler.postMail(emails, subject, htmlMsg, plainMsg, fromEmailId, smtpConfigMap);
                            }
                        }
                    } catch (MessagingException ex) {
                        Logger.getLogger(JournalEntry.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    approvalStatus = level + 1;
                }
            }
            accJournalEntryobj.approvePendingJE(jeID, companyid, approvalStatus);
            return approvalStatus;
        } else {
            return Constants.NoAuthorityToApprove; // It return fixed value 999 which indecates that current logged in user has no authority to approve the transaction
        }
    }
    
    public ModelAndView activateDeactivateJournalEntries(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JEC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            msg = activateDeactivateJournalEntries(request);
            txnManager.commit(status);
            issuccess = true;
            //msg = messageSource.getMessage("acc.je1.updt", null, RequestContextUtils.getLocale(request));   //"Journal Entry has been updated successfully";
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, "accJournalEntryControllerCMN.activateDeactivateJournalEntries", ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, "accJournalEntryControllerCMN.activateDeactivateJournalEntries", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (Exception ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, "accJournalEntryControllerCMN.activateDeactivateJournalEntries", ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public String activateDeactivateJournalEntries(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        String msg = "";
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("repeatedid"))) {
                    String repeateid  = jobj.getString("repeatedid");
                    boolean isactivate = jobj.optBoolean("isactivate");
                    boolean ispendingapproval = jobj.optBoolean("ispendingapproval");
                    if(ispendingapproval){
                        accJournalEntryobj.approveRecurringJE(repeateid, false);    //Journal Entry Approved here
                        msg = messageSource.getMessage("acc.recurringjeApproval.approved", null, RequestContextUtils.getLocale(request));
                    } else {
                        accJournalEntryobj.activateDeactivateJournalEntry(repeateid, isactivate);
                        msg = messageSource.getMessage("acc.recurringjeUpdate.approved", null, RequestContextUtils.getLocale(request));                       
                    }
                }//if
            }//for            
        } catch(Exception ex){//try
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, "accJournalEntryControllerCMN.activateDeactivateJournalEntries", ex);
        }//catch
        return msg;
    }//method-end
    
    public ModelAndView importJournalEntryForAll(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));

            String doAction = request.getParameter("do");
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());

            if (doAction.compareToIgnoreCase("import") == 0) {
                System.out.println("A(( Import start : " + new Date());
                JSONObject datajobj = new JSONObject();
                JSONObject resjson = new JSONObject(request.getParameter("resjson").toString());
                JSONArray resjsonJArray = resjson.getJSONArray("root");

                String filename = request.getParameter("filename");
                datajobj.put("filename", filename);

                String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
                File filepath = new File(destinationDirectory + "/" + filename);
                datajobj.put("FilePath", filepath);

                datajobj.put("resjson", resjsonJArray);
                String exceededLimit = request.getParameter("exceededLimit");
                if (exceededLimit.equalsIgnoreCase("Yes")) {//If file contains records more than 1500 then Import file in background using thread
                    String logId = importJournalEntry.addPendingImportLog(requestParams);
                    requestParams.put("logId", logId);
                    requestParams.put("importflag", Constants.importproductcsv);
                    requestParams.put("jobj", datajobj);
                    requestParams.put("currencyId", sessionHandlerImpl.getCurrencyID(request));
                    requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    requestParams.put("userId", sessionHandlerImpl.getUserid(request));
                    requestParams.put("masterPreference", request.getParameter("masterPreference"));
                    requestParams.put("jeDataMap", AccountingManager.getGlobalParams(request));
                    requestParams.put("dateFormatId", request.getParameter("dateFormat"));
                    requestParams.put("locale", RequestContextUtils.getLocale(request));
                    
                    importJournalEntry.add(requestParams);
                    if (!importJournalEntry.isIsworking()) {
                        Thread t = new Thread(importJournalEntry);
                        t.start();
                    }
                    jobj.put("success", true);
                    jobj.put("exceededLimit", "yes");
                
                } else {
                    jobj = importJournalEntryRecordsForAll(request, datajobj);
                }

                //jobj = importJournalEntryRecordsForAll(request, datajobj);
                System.out.println("A(( Import end : " + new Date());
            } else if (doAction.compareToIgnoreCase("importXLS") == 0) {
                System.out.println("A(( Import start : " + new Date());
                JSONObject datajobj = new JSONObject();
                JSONObject resjson = new JSONObject(request.getParameter("resjson").toString().replaceAll("\\n", "").trim());
                JSONArray resjsonJArray = resjson.getJSONArray("root");

                String filename = request.getParameter("filename");
                datajobj.put("filename", filename);

                String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "xlsfiles";
                File filepath = new File(destinationDirectory + "/" + filename);
                datajobj.put("FilePath", filepath);

                datajobj.put("resjson", resjsonJArray);
                jobj = importJournalEntryRecordsForAllXLS(request, datajobj);
                System.out.println("A(( Import end : " + new Date());
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                System.out.println("A(( Validation start : " + new Date());
                jobj = importHandler.validateFileData(requestParams);
                System.out.println("A(( Validation end : " + new Date());
            }
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject importJournalEntryRecordsForAll(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        FileInputStream fileInputStream = null;
        int total = 0, failed = 0;
        String currencyId = sessionHandlerImpl.getCurrencyID(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("filename");
        String delimiterType = request.getParameter("delimiterType");
        String masterPreference = request.getParameter("masterPreference");
        String prevJENo = "";
        boolean isAlreadyExist = false;
        boolean isRecordFailed = false;
        String customfield = "";
        CsvReader csvReader = null;

        JSONObject returnObj = new JSONObject();

        try {
            HashMap<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            JSONArray jeDetailArr = new JSONArray();
            String[] recarr = null;
            StringBuilder failureMsg = new StringBuilder();
            
            String dateFormat = null, dateFormatId = request.getParameter("dateFormat");
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {

                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }
            /**
             * getting number of digits after decimal points from company
             * preferences
             */
            int digitsAfterDecimalForcompany = 2;
            if (Constants.CompanyPreferencePrecisionMap.containsKey(companyid)) {
                digitsAfterDecimalForcompany = (Integer) Constants.CompanyPreferencePrecisionMap.get(companyid).get(Constants.amountdecimalforcompany);
            }
            DateFormat df = new SimpleDateFormat(dateFormat);
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));

            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);
            String record = "";
            int cnt = 0;
            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            Boolean isCurrencyCode=extrareferences.isCurrencyCode();
            StringBuilder failedRecords = new StringBuilder();
            StringBuilder failedPrevRecords = new StringBuilder();

            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
//            List headArrayList = new ArrayList();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
//                headArrayList.add(jSONObject.get("csvheader"));
                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }

//            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\"");
            HashMap currencyMap = getCurrencyMap(isCurrencyCode);

            while (csvReader.readRecord()) {
                recarr = csvReader.getValues();
                failureMsg.setLength(0);
                if (cnt == 0) {
                    failedRecords.append(createCSVrecord(recarr) + "\"Error Message\"");
                }
                if (cnt != 0) {
                    try {
                        currencyId = sessionHandlerImpl.getCurrencyID(request);

                        String entryNo = "";
                        if (columnConfig.containsKey("entryno")) {
                            entryNo = recarr[(Integer) columnConfig.get("entryno")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(entryNo)) {
                                entryNo = entryNo.replaceAll("\"", "");
                            } else {
                                failureMsg.append("Entry Number is not available.");
                            }
                        } else {
                            failureMsg.append("Entry Number column is not found.");
                        }
                        
                        Date entryDate = null;
                        if (columnConfig.containsKey("entrydate")) {
                            String entryDateStr = recarr[(Integer) columnConfig.get("entrydate")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(entryDateStr)) {
                                failureMsg.append("Entry Date is not available");
                            } else {
                                entryDate = df.parse(entryDateStr);
                            }
                        } else {
                            failureMsg.append("Entry Date column is not found.");
                        }
                        
                        String memo = "";
                        if (columnConfig.containsKey("memo")) {
                            memo = recarr[(Integer) columnConfig.get("memo")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(memo)) {
                                memo = memo.replaceAll("\"", "");
                            }
                        }
                        
                        String description = "";
                        if (columnConfig.containsKey("description")) {
                            description = recarr[(Integer) columnConfig.get("description")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(description)) {
                                description = description.replaceAll("\"", "");
                            }
                        }
                        
                        String accountID = "";
                        String appliedGstTaxID = "";
                        String gstTaxCode = "";
                        Tax tax = null;
                        if (columnConfig.containsKey("accountcode")) {
                            String accountCode = recarr[(Integer) columnConfig.get("accountcode")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(accountCode)) {
                                Account account = getAccount(accountCode, companyid, false);
                                if (account != null) {
                                    accountID = account.getID();
                                    if (!StringUtil.isNullOrEmpty(account.getUsedIn()) && !account.isWantToPostJe()) {
                                        failureMsg.append("The account code (" + accountCode + ") is a control account. " + messageSource.getMessage("acc.JournalEntry.import.ControlAccount", null, RequestContextUtils.getLocale(request)));
                                    }
                                } else {
                                    failureMsg.append("Account Code is not found for " + accountCode);
                                }
                                if (account != null && account.getMastertypevalue() == Constants.ACCOUNT_MASTERTYPE_GST) {
                                    /*
                                     * The params send through hashmap taxFromAccountParams to add new params and make function reusable.
                                     */
                                    HashMap<String, Object> taxFromAccountParams = new HashMap();
                                    taxFromAccountParams.put("accountid", account.getID());
                                    taxFromAccountParams.put("companyid", companyid);
                                    KwlReturnObject taxResult = accAccountDAOobj.getTaxesFromAccountId(taxFromAccountParams);
                                    int taxResultCount = taxResult.getEntityList().size();
                                    if (taxResultCount == 0) {
                                        failureMsg.append("Account " + accountCode + " is having master type GST. So please provide correct account.");
                                    }
                                    if (columnConfig.containsKey("appliedGst")) {
                                        gstTaxCode = recarr[(Integer) columnConfig.get("appliedGst")].replaceAll("\"", "").trim();
                                        if (!StringUtil.isNullOrEmpty(gstTaxCode)) {
                                            KwlReturnObject retObj = accAccountDAOobj.getTaxFromCode(companyid, gstTaxCode);
                                            if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                                tax = (Tax) retObj.getEntityList().get(0);
                                            }
                                            if (tax == null) {
                                                failureMsg.append("Tax Code is not found for code ");
                                            } else {
                                                appliedGstTaxID = tax.getID();
                                            }
                                        }
                                    }

                                }
                                if (account != null&&!account.isActivate()) {
                                    failureMsg.append("Account Code '" + accountCode + "' are already deactivated. Record cannot be imported.");
                                }
                            } else {
                                if (columnConfig.containsKey("accountname")) {
                                    String accountName = recarr[(Integer) columnConfig.get("accountname")].replaceAll("\"", "").trim();
                                    if (!StringUtil.isNullOrEmpty(accountName)) {
                                        Account account = getAccount(accountName, companyid, true);
                                        if (account != null) {
                                            accountID = account.getID();
                                            if (!StringUtil.isNullOrEmpty(account.getUsedIn()) && !account.isWantToPostJe()) {
                                                failureMsg.append("The account code (" + accountCode + ") is a control account. " + messageSource.getMessage("acc.JournalEntry.import.ControlAccount", null, RequestContextUtils.getLocale(request)));
                                            }
                                            if(account.getMastertypevalue()==Constants.ACCOUNT_MASTERTYPE_GST){
                                                /*
                                                 * The params send through hashmap taxFromAccountParams to add new params and make function reusable.
                                                 */
                                                HashMap<String, Object> taxFromAccountParams = new HashMap();
                                                taxFromAccountParams.put("accountid", account.getID());
                                                taxFromAccountParams.put("companyid", companyid);
                                                KwlReturnObject taxResult = accAccountDAOobj.getTaxesFromAccountId(taxFromAccountParams);
                                                int taxResultCount=taxResult.getEntityList().size();
                                                if(taxResultCount == 0){
                                                    failureMsg.append("Account " +accountName+ " is having master type GST. So please provide correct account.");
                                                }
                                                if (columnConfig.containsKey("appliedGst")) {
                                                    gstTaxCode = recarr[(Integer) columnConfig.get("appliedGst")].replaceAll("\"", "").trim();
                                                    if (!StringUtil.isNullOrEmpty(gstTaxCode)) {
                                                        KwlReturnObject retObj = accAccountDAOobj.getTaxFromCode(companyid, gstTaxCode);
                                                        if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                                            tax = (Tax) retObj.getEntityList().get(0);
                                                        }
                                                        if (tax == null) {
                                                            failureMsg.append("Tax Code is not found for code ");
                                                        } else {
                                                            appliedGstTaxID = tax.getID();
                                                        }
                                                    }
                                                }
                                            }
                                            if (!account.isActivate()) {
                                                failureMsg.append("Account Code '" + accountCode + "' are already deactivated. Record cannot be imported.");
                                            }
                                        } else {
                                            failureMsg.append("Account Name is not found for " + accountName);
                                        }
                                    } else {
                                        failureMsg.append("Account is not available");
                                    }
                                } else {
                                    failureMsg.append("Account is not available");
                                }
                            }
                        } else {
                            failureMsg.append("Account Code column is not found.");
                        }
                        
                        if (isCurrencyCode?columnConfig.containsKey("currencyCode"):columnConfig.containsKey("currencyName")) {
                            String currencyStr = isCurrencyCode?recarr[(Integer) columnConfig.get("currencyCode")].replaceAll("\"", "").trim():recarr[(Integer) columnConfig.get("currencyName")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(currencyStr)) {
                                currencyId = getCurrencyId(currencyStr, currencyMap);

                                if (StringUtil.isNullOrEmpty(currencyId)) {
                                    failureMsg.append(messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, RequestContextUtils.getLocale(request)));
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    failureMsg.append("Currency is not available.");
                                }
                            }
                        }

                        String debitAmount = "";
                        String digitsAfterDecimalInDebit="0";
                        String digitsAfterDecimalIncredit="0";
                        if (columnConfig.containsKey("d_amount")) {
                            debitAmount = recarr[(Integer) columnConfig.get("d_amount")].replaceAll("\"", "").trim();
                            /**
                             * getting the number of digits after decimal point
                             */
                            digitsAfterDecimalInDebit = authHandler.getNumberOfdigitsAfterDecimal(debitAmount, companyid);

                            if (Integer.valueOf(digitsAfterDecimalInDebit) > Integer.valueOf(digitsAfterDecimalForcompany)) {
                                failureMsg.append(messageSource.getMessage("acc.JEimport.maximum", null, RequestContextUtils.getLocale(request)) + " " + Integer.valueOf(digitsAfterDecimalForcompany) + " " + messageSource.getMessage("acc.JEimport.areallowedforthiscompany", null, RequestContextUtils.getLocale(request)));
                            }
                            if (StringUtil.isNullOrEmpty(debitAmount)) {
                                //throw new AccountingException("Debit Amount is not available");
                                debitAmount="0";
                            }
                        } else {
                            failureMsg.append("Debit Amount column is not found.");
                        }

                        String creditAmount = "";
                        if (columnConfig.containsKey("c_amount")) {
                            creditAmount = recarr[(Integer) columnConfig.get("c_amount")].replaceAll("\"", "").trim();
                            /**
                             * getting the number of digits after decimal point
                             */
                            digitsAfterDecimalIncredit = authHandler.getNumberOfdigitsAfterDecimal(creditAmount, companyid);

                            if (Integer.valueOf(digitsAfterDecimalIncredit) > Integer.valueOf(digitsAfterDecimalForcompany)) {
                                failureMsg.append(messageSource.getMessage("acc.JEimport.maximum", null, RequestContextUtils.getLocale(request)) + " " + Integer.valueOf(digitsAfterDecimalForcompany) + " " + messageSource.getMessage("acc.JEimport.areallowedforthiscompany", null, RequestContextUtils.getLocale(request)));
                            }
                            if (StringUtil.isNullOrEmpty(creditAmount)) {
                               // throw new AccountingException("Credit Amount is not available");
                                creditAmount="0";
                            }
                        } else {
                            failureMsg.append("Credit Amount column is not found.");
                        }
                        
//                        if (Double.parseDouble(debitAmount) == 0 && Double.parseDouble(creditAmount) == 0) {
//                            throw new AccountingException("Amount in both Debit and Credit columns are not allowed 0/Blank  for " + entryNo);
//                        }
                        
                        if (Double.parseDouble(debitAmount) > 0 && Double.parseDouble(creditAmount) > 0) {
                            failureMsg.append("Amount in both Debit and Credit columns are not allowed for " + entryNo);
                        }

                        String exchangeRate = "0.0";
                        if (columnConfig.containsKey("exchangeRate")) {
                            exchangeRate = recarr[(Integer) columnConfig.get("exchangeRate")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(exchangeRate)) {
                                failureMsg.append("Exchange Rate is not available");
                            }
                        }

                        isAlreadyExist = false;
                        KwlReturnObject result = accJournalEntryobj.getJECount(entryNo, companyid);
                        int nocount = result.getRecordTotalCount();
                        if (nocount > 0) {
                            isAlreadyExist = true;
                            failureMsg.append("Journal entry number '" + entryNo + "' already exists.");
                        }
                        
                        /*
                         IF JE entry no belongs to Sequense Format then create document through Seq. Format.
                         */
                        boolean autogenerated = false;
                        int moduleId = Constants.Acc_GENERAL_LEDGER_ModuleId;
                        Map<String, String> sequenceNumberDataMap = new HashMap<String, String>();
                        sequenceNumberDataMap.put("moduleID", String.valueOf(moduleId));
                        sequenceNumberDataMap.put("entryNumber", entryNo);
                        sequenceNumberDataMap.put("companyID", companyid);
                        sequenceNumberDataMap.put("isFromImport", "true");
                        List list = importHandler.checksEntryNumberForSequenceNumber(sequenceNumberDataMap);
//                        List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_GENERAL_LEDGER_ModuleId, entryNo, companyid);
                        if (!list.isEmpty()) {
                            boolean isvalidEntryNumber = (Boolean) list.get(0);
                            String formatName = (String) list.get(1);
                            if (!isvalidEntryNumber) {
//                                throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " " + entryNo + " " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " " + formatName + ". ");
                                String formatID = (String) list.get(2);
                                int intSeq = (Integer) list.get(3);
                                jeDataMap.put(Constants.SEQNUMBER, intSeq);
                                jeDataMap.put(Constants.SEQFORMAT, formatID);
                                autogenerated = true;
                            }
                        }

                        if (!prevJENo.equalsIgnoreCase(entryNo)) {
                            prevJENo = entryNo;
                            
                            // for saving JE
                            if (jeDetailArr.length() > 0 && !isRecordFailed) {
                                try {
                                    double c_Amount = 0;
                                    double d_Amount = 0;
                                    for (int jedCnt = 0; jedCnt < jeDetailArr.length(); jedCnt++) {
                                        JSONObject jeDetailObj = jeDetailArr.getJSONObject(jedCnt);
                                      if (jeDetailObj.getBoolean("debit")) {
                                            d_Amount += authHandler.round(jeDetailObj.getDouble("amount"), companyid);
                                        } else {
                                            c_Amount += authHandler.round(jeDetailObj.getDouble("amount"), companyid);
                                        }
                                    }
                                    d_Amount = authHandler.round(d_Amount, companyid);
                                    c_Amount = authHandler.round(c_Amount, companyid);
                                     
                                     /*
                                     If Debit and credit amount is zero.We can't save Nil Transaction.
                                     */
                                    if (d_Amount == 0 && c_Amount == 0) {
                                        failureMsg.append("Nil transaction cannot be saved for " + entryNo);
                                    }
                                     
                                    if (d_Amount != c_Amount) {
                                        failureMsg.append(messageSource.getMessage("acc.msgbox.25", null, RequestContextUtils.getLocale(request)));
                                    }

                                    saveJE(request, jeDataMap, jeDetailArr, customfield);
                                } catch (Exception ex) {
                                    failed++;
                                    String errorMsg = ex.getMessage(), invalidColumns = "";
                                    try {
                                        JSONObject errorLog = new JSONObject(errorMsg);
                                        errorMsg = errorLog.getString("errorMsg");
                                        invalidColumns = errorLog.getString("invalidColumns");
                                    } catch (JSONException jex) {
                                    }
                                    failedPrevRecords.append(errorMsg.replaceAll("\"", ""));
                                    failedRecords.append(failedPrevRecords);
                                }
                            }
                            
                            isRecordFailed = false;
                            jeDetailArr = new JSONArray();
                            jeDataMap = AccountingManager.getGlobalParams(request);
                            
                            jeDataMap.put("companyid", companyid);
                            jeDataMap.put("createdby", userId);
                            jeDataMap.put("entrydate", entryDate);
                            jeDataMap.put("memo", memo);
                            jeDataMap.put("entrynumber", entryNo);
                            jeDataMap.put("autogenerated", autogenerated);
                            jeDataMap.put("currencyid", currencyId);
                            jeDataMap.put("externalCurrencyRate", (Double.parseDouble(exchangeRate) > 0)? (1/Double.parseDouble(exchangeRate)) : 0.0); // externalCurrencyRate = 1/exchangeRate  => For Other Currency to Base Currency exchange rate.
                            jeDataMap.put("typevalue", 1);
                            jeDataMap.put("istemplate", 0);
                            
                            // For create custom field array
                            JSONArray customJArr = new JSONArray();
                            for (int i = 0; i < jSONArray.length(); i++) {
                                JSONObject jSONObject = jSONArray.getJSONObject(i);

                                if (jSONObject.optBoolean("customflag", false) && !jSONObject.optBoolean("isLineItem", false)) {
                                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                    requestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_GENERAL_LEDGER_ModuleId, jSONObject.getString("columnname")));

                                    KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
                                    FieldParams params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                    if (jSONObject.getInt("csvindex") > recarr.length - 1) { // (csv) arrayindexoutofbound when last custom column value is empty.
                                        continue;
                                    }

                                    if (!StringUtil.isNullOrEmpty(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim())) {
                                        JSONObject customJObj = new JSONObject();
                                        customJObj.put("fieldid", params.getId());
                                        customJObj.put("filedid", params.getId());
                                        customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                        customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                        customJObj.put("xtype", params.getFieldtype());

                                        String fieldComboDataStr = "";
                                        if (params.getFieldtype() == 3) { // if field of date type
                                            String dateStr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim();
                                            customJObj.put("Col" + params.getColnum(), df.parse(dateStr).getTime());
                                            customJObj.put("fieldDataVal", df.parse(dateStr).getTime());
                                        } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                            String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                                            for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                                                requestParams = new HashMap<String, Object>();
                                                requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                                requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), fieldComboDataArr[dataArrIndex], 0));


                                                fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                                if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                                    FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                                    fieldComboDataStr += fieldComboData.getId() + ",";
                                                }
                                            }

                                            if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                                customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                                customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            } else {
                                                throw new DataInvalidateException(params.getFieldlabel()+" entry not found in master list for "+ params.getFieldlabel()+" dropdown.");
                                            }
                                        } else if (params.getFieldtype() == 11) { // if field of check box type 
                                            customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                                            customJObj.put("fieldDataVal", Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                                        } else if (params.getFieldtype() == 12) { // if field of check list type
                                            requestParams = new HashMap<String, Object>();
                                            requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                                            requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), 0));


                                            fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                            List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();

                                            String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                                            int dataArrIndex = 0;

                                            for (FieldComboData fieldComboData : fieldComboDataList) {
                                                dataArrIndex = 0;
                                                while (fieldComboDataArr.length > dataArrIndex && fieldComboDataArr[dataArrIndex] != null) {
                                                    if (fieldComboData.getValue().equals(fieldComboDataArr[dataArrIndex])) {
                                                        fieldComboDataStr += fieldComboData.getId() + ",";
                                                    }
                                                    dataArrIndex++;
                                                }
                                            }

                                            if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                                customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                                customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            } else {
                                                continue;
                                            }
                                        } else {
                                            customJObj.put("Col" + params.getColnum(), recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                                            customJObj.put("fieldDataVal", recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                                        }

                                        customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());

                                        customJArr.put(customJObj);
                                    }
                                }
                            }

                            customfield = customJArr.toString();
                        }
                        
                        failedPrevRecords = new StringBuilder();
                        failedPrevRecords.append("\n" + createCSVrecord(recarr));
                        
                        /*
                         If any failure then Skip that JE
                        */
                        if (!StringUtil.isNullOrEmpty(failureMsg.toString())) {
                            throw new AccountingException(failureMsg.toString());
                        }
                        
                        // for JE Details
                        JSONObject jeDetailObj = new JSONObject();
                        
                        if (Double.parseDouble(debitAmount) > 0) {
                            jeDetailObj.put("debit", true);
                            jeDetailObj.put("amount", debitAmount);
                        } else {
                            jeDetailObj.put("debit", false);
                            jeDetailObj.put("amount", creditAmount);
                        }
                        
                        jeDetailObj.put("accountid", accountID);
                        jeDetailObj.put("customerVendorId", accountID);
                        jeDetailObj.put("description", description);
                        jeDetailObj.put("accountpersontype", 0);
                        jeDetailObj.put("appliedGst", appliedGstTaxID);//Tag GST tax Code for GST account.
//                        jeDetailObj.put("rowid", 1);
                        
                        // Add Custom fields details of line items
                        // For create custom field array
                        JSONArray lineCustomJArr = new JSONArray();
                        for (int i = 0; i < jSONArray.length(); i++) {
                            JSONObject jSONObject = jSONArray.getJSONObject(i);

                            if (jSONObject.optBoolean("customflag", false) && jSONObject.optBoolean("isLineItem", false)) {
                                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                requestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_GENERAL_LEDGER_ModuleId, jSONObject.getString("columnname")));

                                KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
                                FieldParams params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                if (jSONObject.getInt("csvindex") > recarr.length - 1) { // (csv) arrayindexoutofbound when last custom column value is empty.
                                    continue;
                                }

                                if (!StringUtil.isNullOrEmpty(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim())) {
                                    JSONObject customJObj = new JSONObject();
                                    customJObj.put("fieldid", params.getId());
                                    customJObj.put("filedid", params.getId());
                                    customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                    customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                    customJObj.put("xtype", params.getFieldtype());

                                    String fieldComboDataStr = "";
                                    if (params.getFieldtype() == 3) { // if field of date type
                                        String dateStr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim();
                                        customJObj.put("Col" + params.getColnum(), df.parse(dateStr).getTime());
                                        customJObj.put("fieldDataVal", df.parse(dateStr).getTime());
                                    } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                        String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                                        for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                                            requestParams = new HashMap<String, Object>();
                                            requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                            requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), fieldComboDataArr[dataArrIndex], 0));


                                            fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                            if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                                FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                                fieldComboDataStr += fieldComboData.getId() + ",";
                                            }
                                        }

                                        if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                            customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                        } else {
                                            continue;
                                        }
                                    } else if (params.getFieldtype() == 11) { // if field of check box type 
                                        customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                                        customJObj.put("fieldDataVal", Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                                    } else if (params.getFieldtype() == 12) { // if field of check list type
                                        requestParams = new HashMap<String, Object>();
                                        requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                                        requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), 0));


                                        fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                        List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();

                                        String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                                        int dataArrIndex = 0;

                                        for (FieldComboData fieldComboData : fieldComboDataList) {
                                            if (fieldComboDataArr.length > dataArrIndex && fieldComboDataArr[dataArrIndex] != null && fieldComboDataArr[dataArrIndex].replaceAll("\"", "").trim().equalsIgnoreCase("true")) {
                                                fieldComboDataStr += fieldComboData.getId() + ",";
                                            }
                                            dataArrIndex++;
                                        }

                                        if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                            customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                        } else {
                                            continue;
                                        }
                                    } else {
                                        customJObj.put("Col" + params.getColnum(), recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                                        customJObj.put("fieldDataVal", recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                                    }

                                    customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());

                                    lineCustomJArr.put(customJObj);
                                }
                            }
                        }
                        
                        jeDetailObj.put("customfield", lineCustomJArr.toString());
                        jeDetailArr.put(jeDetailObj);

                    } catch (Exception ex) {
                        failed++;
                        isRecordFailed = true;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) {
                    }
                        failedRecords.append("\n" + createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    total++;
                }
                cnt++;
            }
            
            // save je for last record
            if (!isAlreadyExist && jeDetailArr.length() > 0 && !isRecordFailed) {
                try {
                    double c_Amount = 0;
                    double d_Amount = 0;
                    for (int jedCnt = 0; jedCnt < jeDetailArr.length(); jedCnt++) {
                        JSONObject jeDetailObj = jeDetailArr.getJSONObject(jedCnt);
                        if (jeDetailObj.getBoolean("debit")) {
                            d_Amount += authHandler.round(jeDetailObj.getDouble("amount"), companyid);
                        } else {
                            c_Amount += authHandler.round(jeDetailObj.getDouble("amount"), companyid);
                        }
                    }

                    d_Amount = authHandler.round(d_Amount, companyid);
                    c_Amount = authHandler.round(c_Amount, companyid);
                    
                    if (d_Amount != c_Amount) {
                        throw new AccountingException(messageSource.getMessage("acc.msgbox.25", null, RequestContextUtils.getLocale(request)));
                    }
                    
                    saveJE(request, jeDataMap, jeDetailArr, customfield);
                } catch (Exception ex) {
                    failed++;
                    String errorMsg = ex.getMessage(), invalidColumns = "";
                    try {
                        JSONObject errorLog = new JSONObject(errorMsg);
                        errorMsg = errorLog.getString("errorMsg");
                        invalidColumns = errorLog.getString("invalidColumns");
                    } catch (JSONException jex) {
                    }
                    failedRecords.append("\n" + createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                }
            }

            if (failed > 0) {
                createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = messageSource.getMessage("acc.field.Emptyfile", null, RequestContextUtils.getLocale(request));
            } else if (success == 0) {
//                issuccess = false;
                msg = messageSource.getMessage("acc.rem.169", null, RequestContextUtils.getLocale(request));
            } else if (success == total) {
                msg = messageSource.getMessage("acc.rem.168", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request))+ " " + success + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s " : " ") + messageSource.getMessage("acc.field.successfully.", null, RequestContextUtils.getLocale(request));
                msg += (failed == 0 ? "." : " "+messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request))+ " " + failed + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failed > 1 ? "s" : "") + ".");
            }

            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                commitedEx = true;
                throw ex;
            }
        } catch (Exception ex) {
            if (!commitedEx) { // if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            csvReader.close();

            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);

            try {
                // Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("FailureFileType", failed>0?"csv":"");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Acc_GENERAL_LEDGER_ModuleId);
                try{
                    logDataMap.put("ImportDate", authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()))); 
                }catch(ParseException pe){
                    logDataMap.put("ImportDate", new Date());
                }
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);

                String tableName = importDao.getTableName(fileName);
                importDao.removeFileTable(tableName); // Remove table after importing all records

                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
    public JSONObject importJournalEntryRecordsForAllXLS(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status =null;
//        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
//        FileInputStream fileInputStream = null;
        int total = 0, failed = 0;
        String currencyId = sessionHandlerImpl.getCurrencyID(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("filename");
        String delimiterType = request.getParameter("delimiterType");
        String masterPreference = request.getParameter("masterPreference");
        String prevJENo = "";
        boolean typeXLSFile=(request.getParameter("typeXLSFile")!=null &&request.getParameter("typeXLSFile")!="")?Boolean.valueOf(request.getParameter("typeXLSFile")):false;           
        boolean isAlreadyExist = false;
        boolean isRecordFailed = false;
        String customfield = "";
//        CsvReader csvReader = null;
    
        JSONObject returnObj = new JSONObject();

        try {
            status = txnManager.getTransaction(def);
            HashMap<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            JSONArray jeDetailArr = new JSONArray();
//            String[] recarr = null;

            String dateFormat = null, dateFormatId = request.getParameter("dateFormat");
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {

                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }
            int sheetNo = Integer.parseInt(request.getParameter("sheetindex"));
            FileInputStream fs = new FileInputStream(jobj.getString("FilePath"));
            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            Boolean isCurrencyCode=extrareferences.isCurrencyCode();
            Workbook wb = WorkbookFactory.create(fs);
//            HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
            Sheet sheet = wb.getSheetAt(sheetNo);

            DateFormat df = new SimpleDateFormat(dateFormat);
//            fileInputStream = new FileInputStream(jobj.getString("FilePath"));

//            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);
            String record = "";
            int cnt = 0;

            StringBuilder failedRecords = new StringBuilder();
            StringBuilder failedPrevRecords = new StringBuilder();
            StringBuilder failureMsg = new StringBuilder();

            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
//            List headArrayList = new ArrayList();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
//                headArrayList.add(jSONObject.get("csvheader"));
                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }
            List recarr = new ArrayList();
            List recarr2 = new ArrayList();
//            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\"");
            HashMap currencyMap = getCurrencyMap(isCurrencyCode);
            int maxCol = 0;
            for (int j = 0; j <= sheet.getLastRowNum(); j++) {
                recarr = new ArrayList();
                recarr2 = new ArrayList();
                Row row = sheet.getRow(j);
                if(row == null){
                    continue;
                }
                if (j == 0) {
                    maxCol = row.getLastCellNum();
                    List recarr1 = new ArrayList();
                    for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                        Cell cell = row.getCell(cellcount);

                        if (cell != null) {
                            if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                                cell.setCellValue(cell.getStringCellValue().replaceAll("\n", ""));
                            } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                String CellStringValue = Double.toString((Double) cell.getNumericCellValue()).replaceAll("\n", "");
                                cell.setCellValue(Double.parseDouble(CellStringValue)); //Parsed to Doouble as getnumericCellValue returns double by Default
                            }
                            recarr1.add(cell);
                        } else {
                            recarr1.add("");
                        }
                    }
                    failedRecords.append(createCSVrecord(recarr1.toArray()) + "\"Error Message\"");
                }
                if (cnt != 0) {
                    failureMsg.setLength(0);
                    for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                        Cell cell = row.getCell(cellcount);

                        if (cell != null) {
                            if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                                cell.setCellValue(cell.getStringCellValue().replaceAll("\n", ""));
                            } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                String CellStringValue = Double.toString((Double) cell.getNumericCellValue()).replaceAll("\n", "");
                                cell.setCellValue(Double.parseDouble(CellStringValue)); //Parsed to Doouble as getnumericCellValue returns double by Default
                            }
                            recarr.add(cell);
                            recarr2.add(cell);
                        } else {
                            recarr.add("");
                            recarr2.add("");
                        }
                    }
                    try {
                        currencyId = sessionHandlerImpl.getCurrencyID(request);

                        String entryNo = "";
                        if (columnConfig.containsKey("entryno")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("entryno"));
                            if (cell == null) {
                                failureMsg.append("Entry Number is not available");
                            } else {
                                switch (cell.getCellType()) {
                                    case Cell.CELL_TYPE_NUMERIC:
                                        entryNo = Integer.toString((int) cell.getNumericCellValue());
                                        break;
                                    case Cell.CELL_TYPE_STRING:
                                        entryNo = cell.getStringCellValue().trim();
                                        break;
                                }
//                                if (!StringUtil.isNullOrEmpty(entryNo) && entryNo.contains(",")) {
//                                    throw new AccountingException("Invalid Entry Number, please remove comma from it.");
//                                }
                            }
                            if(StringUtil.isNullOrEmpty(entryNo)){
                                continue;
                            }
                        } else {
                            failureMsg.append("Entry Number column is not found.");
                        }
                        
                        Date entryDate = null;
                        String entryDateStr = "";
                        if (columnConfig.containsKey("entrydate")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("entrydate"));
                            if (cell == null) {
                                failureMsg.append("Entry Date is not available");
                            } else {
                                switch (cell.getCellType()) {
                                    case Cell.CELL_TYPE_STRING:
                                        entryDateStr = cell.getStringCellValue();
                                        break;
                                }                               
                                if (StringUtil.isNullOrEmpty(entryDateStr)) {
                                    entryDate=cell.getDateCellValue();
//                                    throw new AccountingException("Entry Date is not available");
                                } else {
                                    entryDate = df.parse(entryDateStr);
                                }
                                if (entryDate!=null && !StringUtil.isNullOrEmpty(entryDate.toString()) && entryDate.toString().contains(",")) {
                                    failureMsg.append("Invalid Date Number, please remove comma from it.");
                                }
                            }
                        } else {
                            failureMsg.append("Entry Date column is not found.");
                        }
                        String memo = "";
                        if (columnConfig.containsKey("memo")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("memo"));
                            if (cell == null) {
                                failureMsg.append("memo is not available");
                            } else {
                                switch (cell.getCellType()) {
                                    case Cell.CELL_TYPE_NUMERIC:
                                        memo = Integer.toString((int) cell.getNumericCellValue());
                                        break;
                                    case Cell.CELL_TYPE_STRING:
                                        memo = cell.getStringCellValue().trim();
                                        break;
                                }
//                                if (!StringUtil.isNullOrEmpty(memo) && memo.contains(",")) {
//                                    throw new AccountingException("Invalid memo, please remove comma from it.");
//                                }
                            }
                        } else {
                            failureMsg.append("memo column is not found.");
                        }
                        String description = "";
                        if (columnConfig.containsKey("description")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("description"));
                            if (cell == null) {
                                failureMsg.append("description is not available");
                            } else {
                                switch (cell.getCellType()) {
                                    case Cell.CELL_TYPE_NUMERIC:
                                        description = Integer.toString((int) cell.getNumericCellValue());
                                        break;
                                    case Cell.CELL_TYPE_STRING:
                                        description = cell.getStringCellValue().trim();
                                        break;
                                }
//                                if (!StringUtil.isNullOrEmpty(description) && description.contains(",")) {
//                                    throw new AccountingException("Invalid description, please remove comma from it.");
//                                }
                            }
                        } else {
                            failureMsg.append("description column is not found.");
                        }

                        String accountID = "";
                        String appliedGstTaxID = "";
                        if (columnConfig.containsKey("accountcode")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("accountcode"));
                            if (cell == null) {
                                Cell cell1 = row.getCell((Integer) columnConfig.get("accountname"));
                                if (cell1 == null) {
                                    failureMsg.append("account name is not available");
                                } else {
                                    String accountname = "";
                                    switch (cell1.getCellType()) {
                                        case Cell.CELL_TYPE_NUMERIC:
                                            accountname = Integer.toString((int) cell1.getNumericCellValue());
                                            break;
                                        case Cell.CELL_TYPE_STRING:
                                            accountname = cell1.getStringCellValue().trim();
                                            break;
                                    }
//                                    if (!StringUtil.isNullOrEmpty(accountname) && accountname.contains(",")) {
//                                        throw new AccountingException("Invalid account name, please remove comma from it.");
//                                    }
                                    if (!StringUtil.isNullOrEmpty(accountname)) {
                                        Account account = getAccount(accountname, companyid, true);
                                        if (account != null) {
                                            accountID = account.getID();
                                            if (account.getMastertypevalue() == Constants.ACCOUNT_MASTERTYPE_GST) {
                                                /*
                                                 * The params send through hashmap taxFromAccountParams to add new params and make function reusable.
                                                 */
                                                HashMap<String, Object> taxFromAccountParams = new HashMap();
                                                taxFromAccountParams.put("accountid", account.getID());
                                                taxFromAccountParams.put("companyid", companyid);
                                                KwlReturnObject taxResult = accAccountDAOobj.getTaxesFromAccountId(taxFromAccountParams);
                                                int taxResultCount = taxResult.getEntityList().size();
                                                if (taxResultCount == 0) {
                                                    failureMsg.append("Account " + accountname + " is having master type GST. So please provide correct account.");
                                                }
                                                
                                                String gstTaxCode = "";
                                                Tax tax=null;
                                                if (columnConfig.containsKey("appliedGst")) {
                                                    Cell cell2 = row.getCell((Integer) columnConfig.get("appliedGst"));

                                                    switch (cell2.getCellType()) {
                                                        case Cell.CELL_TYPE_NUMERIC:
                                                            gstTaxCode = Integer.toString((int) cell2.getNumericCellValue());
                                                            break;
                                                        case Cell.CELL_TYPE_STRING:
                                                            gstTaxCode = cell2.getStringCellValue().trim();
                                                            break;
                                                    }

                                                    if (!StringUtil.isNullOrEmpty(gstTaxCode) && !gstTaxCode.equalsIgnoreCase("N/A")) {
                                                        KwlReturnObject retObj = accAccountDAOobj.getTaxFromCode(companyid, gstTaxCode);
                                                        if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                                            tax = (Tax) retObj.getEntityList().get(0);
                                                        }
                                                        if (tax == null) {
                                                            failureMsg.append("Tax Code is not found for code ");
                                                        } else {
                                                            appliedGstTaxID = tax.getID();
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            failureMsg.append("Account Name is not found for " + accountname);
                                        }
                                    } else {
                                        failureMsg.append("Account is not available");
                                    }
                                }
                            } else {
                                String accountCode = "";
                                switch (cell.getCellType()) {
                                    case Cell.CELL_TYPE_NUMERIC:
                                        accountCode = Integer.toString((int) cell.getNumericCellValue());
                                        break;
                                    case Cell.CELL_TYPE_STRING:
                                        accountCode = cell.getStringCellValue().trim();
                                        break;
                                }

//                                if (!StringUtil.isNullOrEmpty(accountCode) && accountCode.contains(",")) {
//                                    throw new AccountingException("Invalid account code, please remove comma from it.");
//                                }
                                if (!StringUtil.isNullOrEmpty(accountCode)) {
                                    Account account = getAccount(accountCode, companyid, false);
                                    if (account != null) {
                                        accountID = account.getID();
                                        if (account.getMastertypevalue() == Constants.ACCOUNT_MASTERTYPE_GST) {
                                            /*
                                             * The params send through hashmap taxFromAccountParams to add new params and make function reusable.
                                             */
                                            HashMap<String, Object> taxFromAccountParams = new HashMap();
                                            taxFromAccountParams.put("accountid", account.getID());
                                            taxFromAccountParams.put("companyid", companyid);
                                            KwlReturnObject taxResult = accAccountDAOobj.getTaxesFromAccountId(taxFromAccountParams);
                                            int taxResultCount = taxResult.getEntityList().size();
                                            if (taxResultCount == 0) {
                                                failureMsg.append("Account " + accountCode + " is having master type GST. Such accounts can be used in Tax Master, So cannot be used to post Manual JE. Please provide another account which do not have Master Type GST.");
                                            }
                                            
                                            /*
                                             GST applied tax code 
                                             */
                                            String gstTaxCode = "";
                                            Tax tax = null;
                                            if (columnConfig.containsKey("appliedGst")) {
                                                Cell cell2 = row.getCell((Integer) columnConfig.get("appliedGst"));

                                                switch (cell2.getCellType()) {
                                                    case Cell.CELL_TYPE_NUMERIC:
                                                        gstTaxCode = Integer.toString((int) cell2.getNumericCellValue());
                                                        break;
                                                    case Cell.CELL_TYPE_STRING:
                                                        gstTaxCode = cell2.getStringCellValue().trim();
                                                        break;
                                                }
                                                if (!StringUtil.isNullOrEmpty(gstTaxCode)) {
                                                    KwlReturnObject retObj = accAccountDAOobj.getTaxFromCode(companyid, gstTaxCode);
                                                    if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                                        tax = (Tax) retObj.getEntityList().get(0);
                                                    }
                                                    if (tax == null) {
                                                        failureMsg.append("Tax Code is not found for code ");
                                                    } else {
                                                        appliedGstTaxID = tax.getID();
                                                    }
                                                }
                                            }
                                        }
                                        if (!account.isActivate()) {
                                            failureMsg.append("Account Code '" +  accountCode + "' are already deactivated. Record cannot be imported.");
                                        }
                                        if (!StringUtil.isNullOrEmpty(account.getUsedIn()) && !account.isWantToPostJe()) {
                                            failureMsg.append("The account code (" + accountCode + ") is a control account. " + messageSource.getMessage("acc.JournalEntry.import.ControlAccount", null, RequestContextUtils.getLocale(request)));
                                        }
                                    } else {
                                        failureMsg.append("Account Code is not found for " + accountCode);
                                    }
                                }
                            }
                        } else {
                            failureMsg.append("account code column is not found.");
                        }

                        String currencyStr = "";
                        if (isCurrencyCode?columnConfig.containsKey("currencyCode"):columnConfig.containsKey("currencyName")) {
                            Cell cell = isCurrencyCode?row.getCell((Integer) columnConfig.get("currencyCode")):row.getCell((Integer) columnConfig.get("currencyName"));
                            if (cell == null) {
                                failureMsg.append("Currency is not available");
                            } else {
                                switch (cell.getCellType()) {
                                    case Cell.CELL_TYPE_NUMERIC:
                                        currencyStr = Integer.toString((int) cell.getNumericCellValue());
                                        break;
                                    case Cell.CELL_TYPE_STRING:
                                        currencyStr = cell.getStringCellValue().trim();
                                        break;
                                }
//                                if (!StringUtil.isNullOrEmpty(description) && description.contains(",")) {
//                                    throw new AccountingException("Invalid Currency, please remove comma from it.");
//                                }
                                if (!StringUtil.isNullOrEmpty(currencyStr)) {
                                    currencyId = getCurrencyId(currencyStr, currencyMap);

                                    if (StringUtil.isNullOrEmpty(currencyId)) {
                                        failureMsg.append(messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, RequestContextUtils.getLocale(request)));
                                    }
                                } else {
                                    if (!masterPreference.equalsIgnoreCase("1")) {
                                        failureMsg.append("Currency is not available.");
                                    }
                                }
                            }
                        } else {
                            failureMsg.append("Currency column is not found.");
                        }

                        String debitAmount = "0";
                        if (columnConfig.containsKey("d_amount")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("d_amount"));
                            if (cell == null) {
                               // throw new AccountingException("Debit Amount is not available");
                                debitAmount = "0";
                            } else {
                                switch (cell.getCellType()) {
                                    case Cell.CELL_TYPE_NUMERIC:
                                        debitAmount = Double.toString((double) cell.getNumericCellValue());
                                        break;
                                    case Cell.CELL_TYPE_STRING:
                                        debitAmount = cell.getStringCellValue().trim().equals("")?"0":cell.getStringCellValue().trim();
                                        break;
                                    case Cell.CELL_TYPE_BLANK:
                                        debitAmount = "0";
                                        break;
                                }
//                                if (!StringUtil.isNullOrEmpty(debitAmount) && debitAmount.contains(",")) {
//                                    throw new AccountingException("Invalid Amount, please remove comma from it.");
//                                }
                            }
                        } else {
                            failureMsg.append(" Debit Amount column is not found.");
                        }

                        String creditAmount = "0";
                        if (columnConfig.containsKey("c_amount")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("c_amount"));
                            if (cell == null) {
                               // throw new AccountingException("Amount is not available");
                                creditAmount = "0";
                            } else {
                                switch (cell.getCellType()) {
                                    case Cell.CELL_TYPE_NUMERIC:
                                        creditAmount = Double.toString((double) cell.getNumericCellValue());
                                        break;
                                    case Cell.CELL_TYPE_STRING:
                                        creditAmount = cell.getStringCellValue().trim().equals("")?"0":cell.getStringCellValue().trim();
                                        break;
                                     case  Cell.CELL_TYPE_BLANK:
                                         creditAmount = "0";
                                        break;
                                }
//                                if (!StringUtil.isNullOrEmpty(creditAmount) && creditAmount.contains(",")) {
//                                    throw new AccountingException("Invalid amount, please remove comma from it.");
//                                }
                            }
                        } else {
                            failureMsg.append("Amount column is not found.");
                        }

//                        if (Double.parseDouble(debitAmount) == 0 && Double.parseDouble(creditAmount) == 0) {
//                            throw new AccountingException("Amount in both Debit and Credit columns are not allowed 0/Blank for " + entryNo);
//                        }
                        
                        if (Double.parseDouble(debitAmount) > 0 && Double.parseDouble(creditAmount) > 0) {
                            failureMsg.append("Amount in both Debit and Credit columns are not allowed for " + entryNo);
                        }                        

                        String exchangeRate = "0.0";
                        if (columnConfig.containsKey("exchangeRate")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("exchangeRate"));
                            if (cell == null) {
                                failureMsg.append("Exchange Rate is not available");
                            } else {
                                switch (cell.getCellType()) {
                                    case Cell.CELL_TYPE_NUMERIC:
                                        exchangeRate = Double.toString((double) cell.getNumericCellValue());
                                        break;
                                    case Cell.CELL_TYPE_STRING:
                                        exchangeRate = cell.getStringCellValue().trim();
                                        break;
                                    case Cell.CELL_TYPE_BLANK:
                                        failureMsg.append("Exchange Rate is not available");
                                }
//                                if (!StringUtil.isNullOrEmpty(exchangeRate) && exchangeRate.contains(",")) {
//                                    throw new AccountingException("Invalid exchangeRate, please remove comma from it.");
//                                }
                            }
                        } else {
                            failureMsg.append("exchangeRate column is not found.");
                        }

                        isAlreadyExist = false;
                        KwlReturnObject result = accJournalEntryobj.getJECount(entryNo, companyid);
                        int nocount = result.getRecordTotalCount();
                        if (nocount > 0) {
                            isAlreadyExist = true;
                            failureMsg.append("Journal entry number '" + entryNo + "' already exists.");
                        }

                        /*
                        IF JE entry no belongs to Sequense Format then create document through Seq. Format.
                        */
                        boolean autogenerated = false;
                        int moduleId = Constants.Acc_GENERAL_LEDGER_ModuleId;
                        Map<String, String> sequenceNumberDataMap = new HashMap<String, String>();
                        sequenceNumberDataMap.put("moduleID", String.valueOf(moduleId));
                        sequenceNumberDataMap.put("entryNumber", entryNo);
                        sequenceNumberDataMap.put("companyID", companyid);
                        sequenceNumberDataMap.put("isFromImport", "true");
                        List list = importHandler.checksEntryNumberForSequenceNumber(sequenceNumberDataMap);
//                        List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_GENERAL_LEDGER_ModuleId, entryNo, companyid);
                        if (!list.isEmpty()) {
                            boolean isvalidEntryNumber = (Boolean) list.get(0);
                            String formatName = (String) list.get(1);
                            if (!isvalidEntryNumber) {
//                                throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " " + entryNo + " " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " " + formatName + ". ");
                                String formatID = (String) list.get(2);
                                int intSeq = (Integer) list.get(3);
                                jeDataMap.put(Constants.SEQNUMBER, intSeq);
                                jeDataMap.put(Constants.SEQFORMAT, formatID);
                                autogenerated = true;
                            }
                        }
                        
                        if (!prevJENo.equalsIgnoreCase(entryNo)) {
                            prevJENo = entryNo;
                            
                            // for saving JE
                            if (jeDetailArr.length() > 0 && !isRecordFailed) {
                                try {
                                    double c_Amount = 0;
                                    double d_Amount = 0;
                                    for (int jedCnt = 0; jedCnt < jeDetailArr.length(); jedCnt++) {
                                        JSONObject jeDetailObj = jeDetailArr.getJSONObject(jedCnt);
                                        if (jeDetailObj.getBoolean("debit")) {
                                            d_Amount += authHandler.round(jeDetailObj.getDouble("amount"), companyid);
                                        } else {
                                            c_Amount += authHandler.round(jeDetailObj.getDouble("amount"), companyid);
                                        }
                                    }
                                    d_Amount = authHandler.round(d_Amount, companyid);
                                    c_Amount = authHandler.round(c_Amount, companyid);
                                    
                                    /*
                                    If Debit and credit amount is zero.We can't save Nil Transaction.
                                    */
                                    if (d_Amount == 0 && c_Amount == 0) {
                                        throw new AccountingException("Nil transaction cannot be saved for " + entryNo);
                                    }
                                    if (d_Amount != c_Amount) {
                                        throw new AccountingException(messageSource.getMessage("acc.msgbox.25", null, RequestContextUtils.getLocale(request)));
                                    }

                                    saveJE(request, jeDataMap, jeDetailArr, customfield);
                                } catch (Exception ex) {
                                    failed++;
                                    String errorMsg = ex.getMessage(), invalidColumns = "";
                                    try {
                                        JSONObject errorLog = new JSONObject(errorMsg);
                                        errorMsg = errorLog.getString("errorMsg");
                                        invalidColumns = errorLog.getString("invalidColumns");
                                    } catch (JSONException jex) {
                                    }
                                    failedPrevRecords.append(errorMsg.replaceAll("\"", "")+"\"");
                                    failedRecords.append(failedPrevRecords);
                                }
                            }

                            isRecordFailed = false;
                            jeDetailArr = new JSONArray();
                            jeDataMap = AccountingManager.getGlobalParams(request);

                            jeDataMap.put("companyid", companyid);
                            jeDataMap.put("createdby", userId);
                            jeDataMap.put("entrydate", entryDate);
                            jeDataMap.put("memo", memo);
                            jeDataMap.put("entrynumber", entryNo);
                            jeDataMap.put("autogenerated", autogenerated);//For Seq. Format
                            jeDataMap.put("currencyid", currencyId);
                            jeDataMap.put("externalCurrencyRate", (Double.parseDouble(exchangeRate) > 0) ? (1 / Double.parseDouble(exchangeRate)) : 0.0); // externalCurrencyRate = 1/exchangeRate  => For Other Currency to Base Currency exchange rate.
                            jeDataMap.put("typevalue", 1);
                            jeDataMap.put("istemplate", 0);

                            // For create custom field array
                            customfield = "";
                            JSONArray customJArr = new JSONArray();
                            for (int k = 0; k < jSONArray.length(); k++) {
                                JSONObject jSONObject = jSONArray.getJSONObject(k);
                                if (jSONObject.optBoolean("customflag", false) && !jSONObject.optBoolean("isLineItem", false)) { // customflag=true : Custom Field
                                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                    requestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_GENERAL_LEDGER_ModuleId, jSONObject.getString("columnname")));
                                    KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
                                    FieldParams params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                    if (recarr.get(jSONObject.getInt("csvindex")) != null && !StringUtil.isNullOrEmpty(recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim())) {
                                        JSONObject customJObj = new JSONObject();
                                        customJObj.put("fieldid", params.getId());
                                        customJObj.put("filedid", params.getId());
                                        customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                        customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                        customJObj.put("xtype", params.getFieldtype());
                                        String fieldComboDataStr = "";
                                        if (params.getFieldtype() == 3) { // if field of date typed
                                            Cell cell = row.getCell(jSONObject.getInt("csvindex"));
                                            if (cell != null) {
//                                                entryDate = df.parse(entryDateStr);
//                                                customJObj.put("Col" + params.getColnum(), df.parse(cell.getStringCellValue()));
//                                                customJObj.put("fieldDataVal", df.parse(cell.getStringCellValue()));

                                                customJObj.put("Col" + params.getColnum(), cell.getDateCellValue().getTime());
                                                customJObj.put("fieldDataVal", cell.getDateCellValue().getTime());
                                            }
                                        } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                            String[] fieldComboDataArr = recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim().split(";");
                                            for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                                                requestParams = new HashMap<String, Object>();
                                                String value = fieldComboDataArr[dataArrIndex];
                                                String[] valueArray = value.split("\\."); // Trim value on decimal point.
                                                String trimmedValue = valueArray[0];

                                                requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                                requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), value, 0));
                                                fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                                if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                                    FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                                    fieldComboDataStr += fieldComboData.getId() + ",";
                                                } else {
                                                    requestParams.clear();
                                                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                                    requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), trimmedValue, 0));
                                                    fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                                    if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                                        FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                                        fieldComboDataStr += fieldComboData.getId() + ",";
                                                    }
                                                }
                                            }
                                            if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                                customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                                customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            } else {
                                                throw new DataInvalidateException(params.getFieldlabel()+" entry not found in master list for "+ params.getFieldlabel()+" dropdown.");
                                            }
                                        } else if (params.getFieldtype() == 11) { // if field of check box type 
                                            customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim()));
                                            customJObj.put("fieldDataVal", Boolean.parseBoolean(recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim()));
                                        } else if (params.getFieldtype() == 12) { // if field of check list type
                                            requestParams = new HashMap<String, Object>();
                                            requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                                            requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), 0));
                                            fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                            List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();
                                            String[] fieldComboDataArr = recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim().split(";");
                                            int dataArrIndex = 0;
                                            for (FieldComboData fieldComboData : fieldComboDataList) {
                                                if (fieldComboDataArr.length != dataArrIndex && fieldComboDataArr[dataArrIndex] != null && fieldComboDataArr[dataArrIndex].replaceAll("\"", "").trim().equalsIgnoreCase("true")) {
                                                    fieldComboDataStr += fieldComboData.getId() + ",";
                                                }
                                                dataArrIndex++;
                                            }
                                            if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                                customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                                customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            } else {
                                                continue;
                                            }
                                        } else {
                                            customJObj.put("Col" + params.getColnum(), recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim());
                                            customJObj.put("fieldDataVal", recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim());
                                        }
                                        customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());
                                        customJArr.put(customJObj);
                                    }
                                }
                            }

                            customfield = customJArr.toString();
                        }

                        failedPrevRecords = new StringBuilder();
                        //commenting this
                        failedPrevRecords.append("\n" + createCSVrecord(recarr2.toArray()) + "\"");
                        
                        /*
                         If any failure then Skip that JE
                        */
                        if (!StringUtil.isNullOrEmpty(failureMsg.toString())) {
                            throw new AccountingException(failureMsg.toString());
                        }

                        // for JE Details
                        JSONObject jeDetailObj = new JSONObject();

                        if (Double.parseDouble(debitAmount) > 0) {
                            jeDetailObj.put("debit", true);
                            jeDetailObj.put("amount", debitAmount);
                        } else {
                            jeDetailObj.put("debit", false);
                            jeDetailObj.put("amount", creditAmount);
                        }

                        jeDetailObj.put("accountid", accountID);
                        jeDetailObj.put("customerVendorId", accountID);
                        jeDetailObj.put("description", description);
                        jeDetailObj.put("accountpersontype", 0);
                        jeDetailObj.put("appliedGst", appliedGstTaxID);//Tag GST tax Code for GST account.
//                        jeDetailObj.put("rowid", 1);

                        // Add Custom fields details of line items
                        // For create custom field array
                        JSONArray lineCustomJArr = new JSONArray();
                        for (int k = 0; k < jSONArray.length(); k++) {
                            JSONObject jSONObject = jSONArray.getJSONObject(k);
                            if (jSONObject.optBoolean("customflag", false) && jSONObject.optBoolean("isLineItem", false)) { // customflag=true : Custom Field
                                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                requestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_GENERAL_LEDGER_ModuleId, jSONObject.getString("columnname")));
                                KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
                                FieldParams params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                if (recarr.get(jSONObject.getInt("csvindex")) != null && !StringUtil.isNullOrEmpty(recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim())) {
                                    JSONObject customJObj = new JSONObject();
                                    customJObj.put("fieldid", params.getId());
                                    customJObj.put("filedid", params.getId());
                                    customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                    customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                    customJObj.put("xtype", params.getFieldtype());
                                    String fieldComboDataStr = "";
                                    if (params.getFieldtype() == 3) { // if field of date typed
                                        Cell cell = row.getCell(jSONObject.getInt("csvindex"));
                                        if (cell != null) {
//                                            customJObj.put("Col" + params.getColnum(), df.parse(cell.getStringCellValue()));
//                                            customJObj.put("fieldDataVal", df.parse(cell.getStringCellValue()));
                                            customJObj.put("Col" + params.getColnum(), cell.getDateCellValue().getTime());
                                            customJObj.put("fieldDataVal", cell.getDateCellValue().getTime());
                                        }
                                    } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                        String[] fieldComboDataArr = recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim().split(";");
                                        for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                                            requestParams = new HashMap<String, Object>();
                                            String value = fieldComboDataArr[dataArrIndex];
                                            String[] valueArray = value.split("\\."); // Trim value on decimal point.
                                            String trimmedValue = valueArray[0];

                                            requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                            requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), value, 0));
                                            fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);

                                            if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                                FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                                fieldComboDataStr += fieldComboData.getId() + ",";
                                            } else {
                                                requestParams.clear();
                                                requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                                requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), trimmedValue, 0));
                                                fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                                if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                                    FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                                    fieldComboDataStr += fieldComboData.getId() + ",";
                                                }
                                            }
                                        }
                                        if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                            customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                        } else {
                                            continue;
                                        }
                                    } else if (params.getFieldtype() == 11) { // if field of check box type 
                                        customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim()));
                                        customJObj.put("fieldDataVal", Boolean.parseBoolean(recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim()));
                                    } else if (params.getFieldtype() == 12) { // if field of check list type
                                        requestParams = new HashMap<String, Object>();
                                        requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                                        requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), 0));
                                        fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                        List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();
                                        String[] fieldComboDataArr = recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim().split(";");
                                        int dataArrIndex = 0;
                                        for (FieldComboData fieldComboData : fieldComboDataList) {
                                            if (fieldComboDataArr.length != dataArrIndex && fieldComboDataArr[dataArrIndex] != null && fieldComboDataArr[dataArrIndex].replaceAll("\"", "").trim().equalsIgnoreCase("true")) {
                                                fieldComboDataStr += fieldComboData.getId() + ",";
                                            }
                                            dataArrIndex++;
                                        }
                                        if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                            customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                        } else {
                                            continue;
                                        }
                                    } else {
                                        customJObj.put("Col" + params.getColnum(), recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim());
                                        customJObj.put("fieldDataVal", recarr.get(jSONObject.getInt("csvindex")).toString().replaceAll("\"", "").trim());
                                    }
                                    customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());
                                    lineCustomJArr.put(customJObj);
                                }
                            }
                        }

                        jeDetailObj.put("customfield", lineCustomJArr.toString());
                        jeDetailArr.put(jeDetailObj);

                    } catch (Exception ex) {
                        failed++;
                        isRecordFailed = true;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) {
                        }
                        failedRecords.append("\n" + createCSVrecord(recarr2.toArray()) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    total++;
                }
                cnt++;
            }

            // save je for last record
            if (!isAlreadyExist && jeDetailArr.length() > 0 && !isRecordFailed) {
                try {
                    double c_Amount = 0;
                    double d_Amount = 0;
                    for (int jedCnt = 0; jedCnt < jeDetailArr.length(); jedCnt++) {
                        JSONObject jeDetailObj = jeDetailArr.getJSONObject(jedCnt);
                        if (jeDetailObj.getBoolean("debit")) {
                            d_Amount += authHandler.round(jeDetailObj.getDouble("amount"), companyid);
                        } else {
                            c_Amount += authHandler.round(jeDetailObj.getDouble("amount"), companyid);
                        }
                    }

                    d_Amount = authHandler.round(d_Amount, companyid);
                    c_Amount = authHandler.round(c_Amount, companyid);

                    if (d_Amount != c_Amount) {
                        throw new AccountingException(messageSource.getMessage("acc.msgbox.25", null, RequestContextUtils.getLocale(request)));
                    }

                    saveJE(request, jeDataMap, jeDetailArr, customfield);
                } catch (Exception ex) {
                    failed++;
                    String errorMsg = ex.getMessage(), invalidColumns = "";
                    try {
                        JSONObject errorLog = new JSONObject(errorMsg);
                        errorMsg = errorLog.getString("errorMsg");
                        invalidColumns = errorLog.getString("invalidColumns");
                    } catch (JSONException jex) {
                    }
                    failedRecords.append("\n" + createCSVrecord(recarr2.toArray()) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    recarr2 = new ArrayList();
                }
            }
            if (failed > 0) {
                if (typeXLSFile) {
                    createFailureFiles(fileName, failedRecords, ".xls");
                } else {
                    createFailureFiles(fileName, failedRecords, ".csv");
                }
            }

            int success = total - failed;
            if (total == 0) {
                msg = messageSource.getMessage("acc.field.Emptyfile", null, RequestContextUtils.getLocale(request));
            } else if (success == 0) {
//                issuccess = false;
                msg = messageSource.getMessage("acc.rem.169", null, RequestContextUtils.getLocale(request));
            } else if (success == total) {
                msg = messageSource.getMessage("acc.rem.168", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request))+ " " + success + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s " : " ") + messageSource.getMessage("acc.field.successfully.", null, RequestContextUtils.getLocale(request));
                msg += (failed == 0 ? "." : " "+messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request))+ " " + failed + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failed > 1 ? "s" : "") + ".");
            }

                txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
//            fileInputStream.close();
//            csvReader.close();
  
            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = null;

            try {
                // Insert Integration log
                lstatus=txnManager.getTransaction(ldef);
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "xls");
                logDataMap.put("FailureFileType", failed>0?(typeXLSFile?"xls":"csv"):"");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Acc_GENERAL_LEDGER_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);

                String tableName = importDao.getTableName(fileName);
                importDao.removeFileTable(tableName); // Remove table after importing all records

                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
    private void saveJE(HttpServletRequest request, HashMap<String, Object> jeDataMap, JSONArray jeDetailArr, String customfield) throws AccountingException {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
//            jeDataMap.put("jedetails", jeDetails);
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
            JournalEntry je = (JournalEntry) jeresult.getEntityList().get(0);

            KwlReturnObject jedresult = accJournalEntryobj.getJEDset(jeDetailArr, companyid,je);
            HashSet jeDetails = (HashSet) jedresult.getEntityList().get(0);
            je.setDetails(jeDetails);
            String jeid = je.getID();
            
            
            /************************************** For saving custom fields **********************************************/
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", jeid);
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    Map<String, Object> customjeDataMap = new HashMap<String, Object>();
                    customjeDataMap.put("accjecustomdataref", jeid);
                    customjeDataMap.put("jeid", jeid);
                    customjeDataMap.put("istemplate", 0);
                    jeresult = accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                }
            }
            
            // Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

            Double jeAmount = 0.0;
            int level = 0;
            String controlAccounts = "Control Account(s): ";

            Set<JournalEntryDetail> jeDetail = je.getDetails();
            for (JournalEntryDetail journalEntryDetail : jeDetail) {
                if (journalEntryDetail.isDebit()) { // As Debit and credit amount for JE are same , any one type can be picked for calculating amount
                    jeAmount = jeAmount + journalEntryDetail.getAmount();
                }
                if( journalEntryDetail.getAccount() != null && !StringUtil.isNullOrEmpty(journalEntryDetail.getAccount().getUsedIn()) ){
                    controlAccounts += journalEntryDetail.getAccount().getAccountName() + (!StringUtil.isNullOrEmpty(journalEntryDetail.getAccount().getAcccode()) ? " ("+journalEntryDetail.getAccount().getAcccode()+")":"")+",";
                }
            }
            
              for (JournalEntryDetail jed : je.getDetails()) {
                int srno = 0;
                if (jed.getSrno() != 0) {
                    srno = jed.getSrno();
                    JSONObject jobj = jeDetailArr.getJSONObject(--srno);
                    if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId); // Constants.Acc_JEDetail_recdetailId
                        customrequestParams.put("modulerecid", jed.getID());
                        customrequestParams.put("recdetailId", jed.getID());
                        customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                        customrequestParams.put("companyid", companyid);
                        customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            // jed.setAccJEDetailCustomData((AccJEDetailCustomData) hibernateTemplate.get(AccJEDetailCustomData.class, jed.getID()));
                            JSONObject jedjson = new JSONObject();
                            jedjson.put("accjedetailcustomdata", jed.getID());
                            jedjson.put("jedid", jed.getID());
                            jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                        }
                    }
                }
            }
              
            int journalEntryType = je.getTypeValue();
            String currentUserId = sessionHandlerImpl.getUserid(request);
            int approvalStatusLevel =11;
            if (journalEntryType != 2) { // Currently , Party Journal Entry is excluded from the approval rules. 
                approvalStatusLevel = approveJE(je, sessionHandlerImpl.getCompanyid(request), level, String.valueOf(jeAmount), request, true, currentUserId);
            }
            
            String action = "added new";
            String template = "";
            
            auditTrailObj.insertAuditLog(AuditAction.JOURNAL_ENTRY_MADE, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + template + " Journal Entry " + je.getEntryNumber() + (approvalStatusLevel != 11 ? " " + messageSource.getMessage("acc.field.whichispendingforApproval", null, null) : "") + (!controlAccounts.equals("Control Account(s): ") ? ". " + controlAccounts.substring(0, controlAccounts.length() - 1) : "") + Constants.auditMessageViaImport, request, jeid);
            
        } catch (Exception ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException(ex.getMessage());
        }
    }
    
    public HashMap getCurrencyMap(boolean isCurrencyCode) throws ServiceException {
        HashMap currencyMap = new HashMap();
        KwlReturnObject returnObject = accProductObj.getCurrencies();
        List currencyList = returnObject.getEntityList();
        if (currencyList != null && !currencyList.isEmpty()) {
            Iterator iterator = currencyList.iterator();
            while (iterator.hasNext()) {
                KWLCurrency currency = (KWLCurrency) iterator.next();
                if(isCurrencyCode){
                    currencyMap.put(currency.getCurrencyCode(), currency.getCurrencyID());
                }else{
                currencyMap.put(currency.getName(), currency.getCurrencyID());
                }
            }
        }
        return currencyMap;
    }
    
    private String getCurrencyId(String currencyName, HashMap currencyMap) {
        String currencyId = "";
        if (currencyMap != null && currencyMap.containsKey(currencyName)) {
            currencyId = currencyMap.get(currencyName).toString();
        }
        return currencyId;
    }
    
    private Account getAccount(String accountCode, String companyID, boolean isByAccountName) throws AccountingException {
        Account account = null;
        try {
            if (!StringUtil.isNullOrEmpty(accountCode) && !StringUtil.isNullOrEmpty(companyID)) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("company.companyID");
                filter_params.add(companyID);
                filter_names.add("deleted");
                filter_params.add(false);
                if (isByAccountName) {
                    filter_names.add("name");
                    filter_params.add(accountCode);
                } else {
                    filter_names.add("acccode");
                    filter_params.add(accountCode);
                }

                requestParams.put("filter_names", filter_names);
                requestParams.put("filter_params", filter_params);

                KwlReturnObject retObj = accAccountDAOobj.getAccount(requestParams);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    account = (Account) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Account");
        }
        return account;
    }
    
    public ModelAndView deleteRecurringJournalEntryRule(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("data", request.getParameter("data"));

            //Below Params Added for Audit Trial Entry
            requestParams.put("reqHeader", request.getHeader("x-real-ip"));
            requestParams.put("remoteAddress", request.getRemoteAddr());
            requestParams.put("userid", sessionHandlerImpl.getUserid(request));
            requestParams.put("userFullName", sessionHandlerImpl.getUserFullName(request));
            requestParams.put("locale", RequestContextUtils.getLocale(request));

            msg = deleteRecurringJournalEntryRule(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, "accJournalEntryControllerCMN.deleteRecurringInvoiceRule", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (Exception ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, "accJournalEntryControllerCMN.deleteRecurringInvoiceRule", ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteRecurringJournalEntryRule(HashMap<String, Object> requestParams) throws ServiceException, SessionExpiredException {
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Delete_RJE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        Locale locale = null;
        if(requestParams.containsKey("locale")){
            locale = (Locale)requestParams.get("locale");
        }
        try {
            String nonDeletedRepeatedJEs = "";
            JSONArray dataArray = new JSONArray();
            String userFullName = (String) requestParams.get("userFullName");
            dataArray = new JSONArray((String) requestParams.get("data"));
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject obj = dataArray.getJSONObject(i);
                String invoicenumber = obj.optString("invoicenumber", "");
                String invoiceid = obj.getString("invoiceid");
                String repeateid = obj.getString("repeatedid");
                try {
                    status = txnManager.getTransaction(def);

                    //repeatJE is foreign key in JournalEntry so setting it null for removing dependency before deleting Repeated JE 
                    accJournalEntryobj.updateToNullRepeatedJEOfJournalEntry(invoiceid, repeateid);

                    //Deleting entry from RepeateJEMemo as it is redundant after deleting RepeatedJEJE
                    accJournalEntryobj.DelRepeateJEMemo(repeateid, "RepeatedJEID.id");

                    //Finally Deleting Repeated JE Record From Recurring / Pending Recurring Tab
                    accJournalEntryobj.deleteRepeatedJE(repeateid);

                    auditTrailObj.insertAuditLog(AuditAction.REPEATED_JE_DELETE, "User " + userFullName + " has deleted a recurring Journal Entry " + invoicenumber, requestParams, repeateid);
                    txnManager.commit(status);
                } catch (Exception ex) {
                    nonDeletedRepeatedJEs += nonDeletedRepeatedJEs.equals("") ? invoicenumber : "," + invoicenumber;
                    txnManager.rollback(status);
                    Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (!StringUtil.isNullOrEmpty(nonDeletedRepeatedJEs)) {
                msg = "Except Rerord(s) " + nonDeletedRepeatedJEs + " all selected records have been deleted successfully.";
            } else {
                msg = messageSource.getMessage("acc.commo.Allselectedrecord(s)havebeendeletedsuccessfully", null, locale);
            }

        } catch (Exception ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("deleteRecurringJERule: " + ex.getMessage(), ex);
        }
        return msg;
    }
    /**
     * Function to post JE for TDS and TCS type of Payment-Invoice adjustment
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView saveManualJournalEntries(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            params.put(Constants.currencyKey, sessionHandlerImpl.getCurrencyID(request));
            JSONObject jSONObject = accJournalEntryService.postJournalEntry(params);
            String JENos = jSONObject.optString("JENos");
            if (JENos.length() > 1) {
                JENos = JENos.substring(0, JENos.length() - 1);
            }
            issuccess = true;
            msg = messageSource.getMessage("acc.ManualJEposting", null, RequestContextUtils.getLocale(request));
            msg += " " + JENos;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accJournalEntryControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
