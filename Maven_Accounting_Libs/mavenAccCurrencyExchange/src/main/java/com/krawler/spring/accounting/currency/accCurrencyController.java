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
package com.krawler.spring.accounting.currency;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.ImportLog;
import static com.krawler.common.admin.ImportLog.getActualFileName;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.CustomCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.authHandler.authHandler;
import static com.krawler.spring.accounting.currency.CurrencyContants.FROMCURRENCY;
import static com.krawler.spring.accounting.currency.CurrencyContants.ID;
import static com.krawler.spring.accounting.currency.CurrencyContants.NEWEXCHANGERATE;
import static com.krawler.spring.accounting.currency.CurrencyContants.TOCURRENCY;
import static com.krawler.spring.accounting.currency.CurrencyContants.TOCURRENCYID;
import static com.krawler.spring.accounting.currency.CurrencyContants.TRANSACTIONDATE;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accCurrencyController extends MultiActionController implements CurrencyContants, MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accCurrencyDAO accCurrencyDAOobj;
    private AccTaxCurrencyExchangeDAO accTaxCurrencyExchangeDAOobj;
    private String successView;
    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;
    public ImportHandler importHandler;
    private ImportDAO importDao;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private exportMPXDAOImpl exportDaoObj;

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

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setAccTaxCurrencyExchangeDAO(AccTaxCurrencyExchangeDAO accTaxCurrencyExchangeDAOobj) {
        this.accTaxCurrencyExchangeDAOobj = accTaxCurrencyExchangeDAOobj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    public void setImportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }
    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
  
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    
    public ModelAndView saveCurrencyExchange(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String exchangerate = "";
        String currencycode = "";
        String applydate = "";
        String todate = "";
        boolean issuccess = false;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Currency_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        JSONArray jArr = new JSONArray(request.getParameter(DATA));
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj1 = jArr.getJSONObject(i);
            exchangerate += jobj1.getString(EXCHANGERATE)+",";
            currencycode += jobj1.getString(CURRENCYCODE)+",";
            applydate += jobj1.getString(APPLYDATE)+",";
            todate += jobj1.getString(TODATE)+",";
        }
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            boolean dateexist = false;
            dateexist = saveCurrencyExchange(request);
            jobj.put(DATEEXIST, dateexist);
            if(dateexist==false){
             auditTrailObj.insertAuditLog(AuditAction.CURRENCY_EXCHANGE_ADDED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated "+(java.util.Arrays.toString(currencycode.split(",")))+" "+" to rate "+(java.util.Arrays.toString(exchangerate.split(",")))+" on date "+(java.util.Arrays.toString(applydate.split(","))) , request, companyid); 
            }
            txnManager.commit(status);
             issuccess = true;
            msg = messageSource.getMessage("acc.curex.update", null, RequestContextUtils.getLocale(request));   //"Currency Exchange Rate has been updated successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }

    public boolean saveCurrencyExchange(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        try {
            boolean updateRate = (request.getParameter("changerate") == null ? false : Boolean.parseBoolean(request.getParameter("changerate")));
            JSONArray jArr = new JSONArray(request.getParameter(DATA));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                Date appDate = null;
                if (StringUtil.isNullOrEmpty(jobj.getString(APPLYDATE))) {
                    throw new AccountingException(messageSource.getMessage("acc.curex.excp1", null, RequestContextUtils.getLocale(request)));
                } else {
                    appDate = authHandler.getDateOnlyFormat(request).parse(StringUtil.DecodeText(jobj.optString(APPLYDATE)));
                }
                Date toDateVal = null;
                if (StringUtil.isNullOrEmpty(jobj.getString(TODATE))) {
                    throw new AccountingException(messageSource.getMessage("acc.curex.exception2", null, RequestContextUtils.getLocale(request)));
                } else {
                    toDateVal = authHandler.getDateOnlyFormat(request).parse(StringUtil.DecodeText(jobj.optString(TODATE)));
                }
                Calendar applyDate = Calendar.getInstance();
                applyDate.setTime(appDate);
                Calendar toDate = Calendar.getInstance();
                toDate.setTime(toDateVal);
                String erid = StringUtil.DecodeText(jobj.optString(ID));
                Map<String, Object> filterParams = new HashMap<String, Object>();
                filterParams.put(ERID, erid);
                filterParams.put(APPLYDATE, appDate);
                filterParams.put(TODATE, toDateVal);
                filterParams.put(COMPANYID, companyid);
                KwlReturnObject result = accCurrencyDAOobj.getExchangeRateDetails(filterParams, false);
                List list = result.getEntityList();
                Map<String, Object> erdMap = new HashMap<String, Object>();
                if (StringUtil.isNullOrEmpty(jobj.getString(EXCHANGERATE))) {
                    throw new AccountingException(messageSource.getMessage("acc.curex.excp2", null, RequestContextUtils.getLocale(request)));
                } else {
                    erdMap.put(EXCHANGERATE, Double.parseDouble(StringUtil.DecodeText(jobj.optString(EXCHANGERATE))));
                }
                if (jobj.has(FOREIGNTOBASEEXCHANGERATE) && !StringUtil.isNullOrEmpty(jobj.getString(FOREIGNTOBASEEXCHANGERATE))) {
                    erdMap.put(FOREIGNTOBASEEXCHANGERATE, Double.parseDouble(StringUtil.DecodeText(jobj.optString(FOREIGNTOBASEEXCHANGERATE))));
                }
                ExchangeRateDetails erd;
                KwlReturnObject erdresult;
                if (list.size() > 0 && !updateRate) {
                    return true;
                } else {
                    if (list.size() <= 0) {
                        //throw new AccountingException("Can not change edit the Exchange Rate.");
                        erdMap.put(APPLYDATE, authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(applyDate.getTime())));
                        erdMap.put(TODATE, authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(toDate.getTime())));
                        erdMap.put(ERID, erid);
                        erdMap.put(COMPANYID, companyid);
                        erdresult = accCurrencyDAOobj.addExchangeRateDetails(erdMap);
                    } else {
                        erd = (ExchangeRateDetails) list.get(0);
                        erdMap.put(ERDID, erd.getID());
                        erdresult = accCurrencyDAOobj.updateExchangeRateDetails(erdMap);
                    }
                    erd = (ExchangeRateDetails) erdresult.getEntityList().get(0);
                }
            }
        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE("Can't extract the records. <br>Encoding not supported", ex);
        }*/ catch (JSONException ex) {
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveCurrencyExchange : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveCurrencyExchange : " + ex.getMessage(), ex);
        }
        return false;
    }

    public ModelAndView saveCurrencyExchangeDetail(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Currency_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {

            saveCurrencyExchangeDetail(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.curex.update", null, RequestContextUtils.getLocale(request));   //"Currency Exchange Rate has been updated successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }

    public void saveCurrencyExchangeDetail(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        try {

            String companyid = sessionHandlerImpl.getCompanyid(request);
            Date appDate = null;
            Date toDate = null;
            String erid = "", exchangeRate = "";
            if (StringUtil.isNullOrEmpty(request.getParameter(APPLYDATE))) {
                throw new AccountingException(messageSource.getMessage("acc.curex.excp1", null, RequestContextUtils.getLocale(request)));
            } else {
                appDate = authHandler.getDateOnlyFormat(request).parse(StringUtil.DecodeText(request.getParameter(APPLYDATE)));
            }

            if (StringUtil.isNullOrEmpty(request.getParameter(TODATE))) {
                throw new AccountingException(messageSource.getMessage("acc.curex.exception2", null, RequestContextUtils.getLocale(request)));
            } else {
                toDate = authHandler.getDateOnlyFormat(request).parse(StringUtil.DecodeText(request.getParameter(TODATE)));
            }
            
            if (StringUtil.isNullOrEmpty(request.getParameter(ID))) {
                throw new AccountingException("Exchange Rate ID not found");
            } else {
                erid = request.getParameter(ID);
            }

            if (StringUtil.isNullOrEmpty(request.getParameter(EXCHANGERATE))) {
                throw new AccountingException("Exchange Rate not found");
            } else {
                exchangeRate = request.getParameter(EXCHANGERATE);
            }

            Map<String, Object> erdMap = new HashMap<String, Object>();
            erdMap.put(APPLYDATE, appDate);
            erdMap.put(TODATE, toDate);
            erdMap.put(ERID, erid);
            erdMap.put(COMPANYID, companyid);
            erdMap.put(EXCHANGERATE, Double.parseDouble(StringUtil.DecodeText(exchangeRate)));
            accCurrencyDAOobj.addExchangeRateDetails(erdMap);

        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE("Can't extract the records. <br>Encoding not supported", ex);
        }*/ catch (ParseException ex) {
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveCurrencyExchange : " + ex.getMessage(), ex);
        }

    }

    public ModelAndView getCurrencyExchange(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(TRANSACTIONDATE, request.getParameter(TRANSACTIONDATE));
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(FROMCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
            String toCurrencyid = request.getParameter(TOCURRENCYID);
            if (!StringUtil.isNullOrEmpty(toCurrencyid)) {
                requestParams.put(TOCURRENCYID, request.getParameter(TOCURRENCYID));
            }

            ExtraCompanyPreferences extraCompanyPreferencesObj = null;
            Map<String, Object> requestParamsExtra = new HashMap<String, Object>();
            requestParamsExtra.put("id", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject resultExtra = accCurrencyDAOobj.getExtraCompanyPreferencestoCheckBaseCurrency(requestParamsExtra);
            if (!resultExtra.getEntityList().isEmpty()) {
                extraCompanyPreferencesObj = (ExtraCompanyPreferences) resultExtra.getEntityList().get(0);
            }
            boolean isOnlyBaceCurrencyflag = false;
            if (extraCompanyPreferencesObj != null) {
                if (extraCompanyPreferencesObj.isOnlyBaseCurrency()) {
                    isOnlyBaceCurrencyflag = true;
                }
            }
           
            KwlReturnObject result = accCurrencyDAOobj.getCurrencyExchange(requestParams);            
            List list = result.getEntityList();
            
            JSONArray jArr = getCurrencyExchangeJson(request, list, isOnlyBaceCurrencyflag);
            jobj.put(DATA, jArr);
            jobj.put(COUNT, jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }

    public JSONArray getCurrencyExchangeJson(HttpServletRequest request, List<ExchangeRate> list, boolean isOnlyBaceCurrencyflag) throws SessionExpiredException, ParseException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            Date transactiondate = null;
            String date = request.getParameter(TRANSACTIONDATE) == null ? null : request.getParameter(TRANSACTIONDATE);
            boolean isCurrencyExchangeWindow = StringUtil.isNullOrEmpty(request.getParameter("iscurrencyexchangewindow")) ? false : Boolean.parseBoolean(request.getParameter("iscurrencyexchangewindow"));
            requestParams.put("isCurrencyExchangeWindow",isCurrencyExchangeWindow);
            String isAddAll = request.getParameter("isAll");
            if (!StringUtil.isNullOrEmpty(date)) {  //ERP-9230 : //Used same date formatter which have used to save currency exchange
                transactiondate = authHandler.getDateOnlyFormat(request).parse(date); 
            }
            DateFormat df = authHandler.getDateOnlyFormat(request);
//            Iterator itr = list.iterator(); 
//            while (itr.hasNext()) {
//                ExchangeRate ER = (ExchangeRate) itr.next();
            JSONObject obj = new JSONObject();
//            if (!StringUtil.isNullOrEmpty(isAddAll)) {
//                obj.put(TOCURRENCYID, "1234");
//                obj.put(TOCURRENCY, "All");
//                jArr.put(obj);
//            }
            if (list != null && !list.isEmpty()) {
                for (ExchangeRate ER : list) {
                    String erID = ER.getID();
                    //                ExchangeRateDetails erd=CompanyHandler.getExcDetailID(session,request,null,transactiondate,erID);
                    KwlReturnObject erdresult = accCurrencyDAOobj.getExcDetailID(requestParams, null, transactiondate, erID);
                    ExchangeRateDetails erd = (ExchangeRateDetails) erdresult.getEntityList().get(0);
                    boolean isMaxNearestExchangeRate=Boolean.parseBoolean(erdresult.getEntityList().get(1).toString());
                    obj = new JSONObject();
                    if (erd != null) {
                        
                        if (isOnlyBaceCurrencyflag && !erd.getExchangeratelink().getFromCurrency().getCurrencyID().equals(erd.getExchangeratelink().getToCurrency().getCurrencyID())) {
                            continue;
                        }
                     
                        obj.put(ID, erd.getExchangeratelink().getID());
                        obj.put(APPLYDATE, df.format(erd.getApplyDate()));
                        obj.put(TODATE, erd.getToDate()!=null ? df.format(erd.getToDate()) : null);
                        obj.put(EXCHANGERATE, erd.getExchangeRate());
                        obj.put(NEWEXCHANGERATE, erd.getExchangeRate());
                        obj.put(FROMCURRENCY, erd.getExchangeratelink().getFromCurrency().getName());
                        obj.put(SYMBOL, erd.getExchangeratelink().getToCurrency().getSymbol());
                        obj.put(HTMLCODE, erd.getExchangeratelink().getToCurrency().getHtmlcode());
                        obj.put(CURRENCYCODE, erd.getExchangeratelink().getToCurrency().getCurrencyCode());
                        obj.put(TOCURRENCY, erd.getExchangeratelink().getToCurrency().getName());
                        obj.put(TOCURRENCYID, erd.getExchangeratelink().getToCurrency().getCurrencyID());
                        obj.put(FROMCURRENCYID, erd.getExchangeratelink().getFromCurrency().getCurrencyID());
                        obj.put(COMPANYID, erd.getCompany().getCompanyID());
                        obj.put(ISMAXNEARESTEXCHANGERATE, isMaxNearestExchangeRate);
                        obj.put(FOREIGNTOBASEEXCHANGERATE, erd.getForeignToBaseExchangeRate());
                        jArr.put(obj);
                    }
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCurrencyExchangeJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView getCurrencyExchangeList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {

            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(ERID, request.getParameter("currencyid"));

            KwlReturnObject result = accCurrencyDAOobj.getExchangeRateDetails(requestParams, true);
            List list = result.getEntityList();

            JSONArray jArr = getCurrencyExchangeListJson(request, list);
            jobj.put(DATA, jArr);
            jobj.put(COUNT, jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }

    public JSONArray getCurrencyExchangeListJson(HttpServletRequest request, List<ExchangeRateDetails> list) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
//            Iterator itr = list.iterator();
//            while(itr.hasNext()) {
//                            ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
            if (list != null && !list.isEmpty()) {
                for (ExchangeRateDetails erd : list) {
                    JSONObject obj = new JSONObject();
                    obj.put(ID, erd.getExchangeratelink().getID());
                    obj.put(APPLYDATE, authHandler.getDateOnlyFormat(request).format(erd.getApplyDate()));
                    if (erd.getToDate() != null) {
                        obj.put(TODATE, authHandler.getDateOnlyFormat(request).format(erd.getToDate()));
                    }
                    obj.put(EXCHANGERATE, erd.getExchangeRate());
                    obj.put(FROMCURRENCY, erd.getExchangeratelink().getFromCurrency().getName());
                    obj.put(SYMBOL, erd.getExchangeratelink().getToCurrency().getSymbol());
                    obj.put(HTMLCODE, erd.getExchangeratelink().getToCurrency().getHtmlcode());
                    obj.put(TOCURRENCY, erd.getExchangeratelink().getToCurrency().getName());
                    obj.put(TOCURRENCYID, erd.getExchangeratelink().getToCurrency().getCurrencyID());
                    obj.put(FROMCURRENCYID, erd.getExchangeratelink().getFromCurrency().getCurrencyID());
                    obj.put(FOREIGNTOBASEEXCHANGERATE, erd.getForeignToBaseExchangeRate());
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCurrencyExchangeListJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView getDefaultCurrencyExchange(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put(TRANSACTIONDATE, null);

            if (request.getParameter("currencyid") != null) {
                requestParams.put("gcurrencyid", request.getParameter("currencyid"));
            }
            KwlReturnObject result = accCurrencyDAOobj.getDefaultCurrencyExchange(requestParams);
            List list = result.getEntityList();

            JSONArray jArr = getDefaultCurrencyExchangeJson(request, list);
            jobj.put(DATA, jArr);
            jobj.put(COUNT, jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }

    public JSONArray getDefaultCurrencyExchangeJson(HttpServletRequest request, List<Object[]> list) throws SessionExpiredException, ServiceException, ParseException {
        JSONArray jArr = new JSONArray();
         Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            Date transactiondate = null;
            boolean downloadexchangerateflag=(request.getParameter("downloadexchangerate")!=null)?Boolean.parseBoolean(request.getParameter("downloadexchangerate")):false;
            String date = request.getParameter(TRANSACTIONDATE) == null ? null : request.getParameter(TRANSACTIONDATE);
            DateFormat df = authHandler.getDateOnlyFormat(request);
        try {
            if (!StringUtil.isNullOrEmpty(date)) { 
                transactiondate = authHandler.getDateOnlyFormat(request).parse(date); 
            }
            requestParams.put("downloadexchangerateflag",downloadexchangerateflag);
            if (list != null && !list.isEmpty()) {
                for (Object[] row : list) {
                    DefaultExchangeRate er = (DefaultExchangeRate) row[0];
                    DefaultExchangeRateDetails erd = (DefaultExchangeRateDetails) row[1];
                    JSONObject obj = new JSONObject();
                    obj.put(ID, er.getID());
                    KwlReturnObject erdresult = accCurrencyDAOobj.getExcDetailID(requestParams, null, transactiondate, er.getID());
                    ExchangeRateDetails erd1 = (ExchangeRateDetails) erdresult.getEntityList().get(0);
                    if (er.getFromCurrency().getCurrencyID().equals(er.getToCurrency().getCurrencyID())) {
                        obj.put(APPLYDATE, authHandler.getDateFormatter(request).format(new Date(1, 1, 1)));
                        obj.put(TODATE, authHandler.getDateFormatter(request).format(new Date(1, 1, 31)));
                    } else {
                        obj.put(APPLYDATE,erd1!=null?df.format(erd1.getApplyDate()): authHandler.getDateFormatter(request).format(new Date()));
                        obj.put(TODATE, erd1!=null?(erd1.getToDate()!=null ? df.format(erd1.getToDate()) : null):authHandler.getDateFormatter(request).format(new Date()));
                    }
                    obj.put(EXCHANGERATE,erd1!=null?erd1.getExchangeRate():erd.getExchangeRate());
                    obj.put(NEWEXCHANGERATE,erd1!=null?erd1.getExchangeRate(): erd.getExchangeRate());
                    obj.put(FOREIGNTOBASEEXCHANGERATE,erd1!=null?erd1.getForeignToBaseExchangeRate():erd.getForeignToBaseExchangeRate());
                    obj.put(FROMCURRENCY,erd1!=null? erd1.getExchangeratelink().getFromCurrency().getName():er.getFromCurrency().getName());
                    obj.put(SYMBOL,erd1!=null? erd1.getExchangeratelink().getToCurrency().getSymbol():er.getToCurrency().getSymbol());
                    obj.put(HTMLCODE,erd1!=null? erd1.getExchangeratelink().getToCurrency().getHtmlcode():er.getToCurrency().getHtmlcode());
                    obj.put(TOCURRENCY,erd1!=null?erd1.getExchangeratelink().getToCurrency().getName(): er.getToCurrency().getName());
                    obj.put(TOCURRENCYID, erd1!=null?erd1.getExchangeratelink().getToCurrency().getCurrencyID():er.getToCurrency().getCurrencyID());
                    obj.put(FROMCURRENCYID, erd1!=null?erd1.getExchangeratelink().getFromCurrency().getCurrencyID():er.getFromCurrency().getCurrencyID());
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getDefaultCurrencyExchangeJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    /*
     * public Double getCurrencyToBaseAmount(HttpServletRequest request, Double
     * Amount, String currencyid, String companyid, Date transactiondate) throws
     * ServiceException { if(Amount != 0) { try { HashMap<String, Object>
     * requestParams = new HashMap<String, Object>();
     * requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
     * requestParams.put("gcurrencyid", AuthHandler.getCurrencyID(request));
     *
     * KwlReturnObject result = accCurrencyDAOobj.getExcDetailID(requestParams,
     * currencyid, transactiondate, null); List list = result.getEntityList();
     * if(!list.isEmpty()) { Iterator itr = list.iterator(); ExchangeRateDetails
     * erd = (ExchangeRateDetails) itr.next(); Double rate=
     * erd.getExchangeRate(); Amount= Amount/rate; } } catch (ServiceException
     * ex) {
     * Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE,
     * null, ex); throw ex; } catch (SessionExpiredException ex) {
     * Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE,
     * null, ex); throw ServiceException.FAILURE(ex.getMessage(), ex); } }
     * return Amount; }
     */
    public ModelAndView getCurrency(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap currencyMap = new HashMap();
            KwlReturnObject result = accCurrencyDAOobj.getCurrencies(currencyMap);
            List list = result.getEntityList();

            JSONArray jArr = getCurrenciesJson(list);
            jobj.put(DATA, jArr);
            jobj.put(COUNT, jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }

    public JSONArray getCurrenciesJson(List<Object[]> list) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            if (list != null && !list.isEmpty()) {
                Iterator iterator = list.iterator();
                while (iterator.hasNext()) {
                    KWLCurrency currency = (KWLCurrency) iterator.next();
                    JSONObject obj = new JSONObject();
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("name", currency.getName());
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCurrenciesJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    //Save custom currency symbol and symbol which is use in Document designer 
    public ModelAndView saveCustomCurrency(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, JSONException {
        JSONObject jobj = new JSONObject();
        boolean issuccess=false;
        String msg = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = request.getParameter("currencyid");
            String name = request.getParameter("name");
            String customcurrencycode = StringUtil.DecodeText(request.getParameter("customcurrencycode"));
            String customcurrencysymbol =  StringUtil.DecodeText(request.getParameter("customcurrencysymbol"));
            String systemcurrencycode = request.getParameter("systemcode");
            String systemcurrencysymbol = request.getParameter("systemsymbol");
            KwlReturnObject result = accCurrencyDAOobj.getCustomCurrencies(companyid,currencyid);
            List list = result.getEntityList();
           
            Map<String, Object> customCurrencyMap = new HashMap<String, Object>();
            KwlReturnObject erdresult;
            if (list.size() <= 0) {
                customCurrencyMap.put("currencyid", currencyid);
                customCurrencyMap.put("name", name);
                customCurrencyMap.put("customcurrencycode", customcurrencycode);
                customCurrencyMap.put("customcurrencysymbol", customcurrencysymbol);
                customCurrencyMap.put("systemcurrencysymbol", systemcurrencysymbol);
                customCurrencyMap.put("systemcurrencycode", systemcurrencycode);
                customCurrencyMap.put("companyid", companyid);
                erdresult = accCurrencyDAOobj.addCustomCurrency(customCurrencyMap);
            } else {
                CustomCurrency erd = (CustomCurrency) list.get(0);
                customCurrencyMap.put("id", erd.getID());
                customCurrencyMap.put("customcurrencysymbol", customcurrencysymbol);
                customCurrencyMap.put("customcurrencycode", customcurrencycode);
                erdresult = accCurrencyDAOobj.updateCustomCurrency(customCurrencyMap);
            }
            issuccess =true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                msg = "Custom Currency Symbol and Code has been saved successfully";
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }
    public ModelAndView deleteCustomCurrency(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, JSONException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {
            String currencyid = request.getParameter("currencyid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            accCurrencyDAOobj.deleteCurrencySymbol(currencyid, companyid);
            issuccess = true;
            jobj.put(SUCCESS, issuccess);
        } catch (Exception ex) {
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }
    

    public ModelAndView getCustomCurrency(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject result = accCurrencyDAOobj.getCustomCurrencies(companyid);
            List list = result.getEntityList();
            if (list.isEmpty()) {
                result = accCurrencyDAOobj.getCustomCurrencies("");
                list = result.getEntityList();
            }
            JSONArray jArr = getCustomCurrencyJson(list);
            jobj.put(DATA, jArr);
            jobj.put(COUNT, jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }

 public JSONArray getCustomCurrencyJson(List<CustomCurrency> list) throws SessionExpiredException, ServiceException, UnsupportedEncodingException {
        JSONArray jArr = new JSONArray();
        try {
            if (list != null && !list.isEmpty()) {
                for (CustomCurrency customcurrency : list) {
                    JSONObject obj = new JSONObject();
                    obj.put("currencyid", customcurrency.getCurrencyID().getCurrencyID());
                    obj.put("name", StringUtil.DecodeText(customcurrency.getName()));
                    obj.put("systemcurrencysymbol", customcurrency.getSystemcurrencysymbol());
                    obj.put("systemcurrencycode", customcurrency.getSystemcurrencycode());
                    obj.put("customcurrencysymbol", customcurrency.getCustomcurrencysymbol());
                    obj.put("customcurrencycode", customcurrency.getCustomcurrencycode());
                    jArr.put(obj);

                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getCurrenciesJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    //-Neeraj Dwivedi saving applydate (financial year)  for currencyexchange if financial year is changed.
    public ModelAndView saveApplyDateforExchangeRate(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            boolean exchangesaveflag = saveApplyDateforExchangeRate(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.curex.update", null, RequestContextUtils.getLocale(request));   //"Currency Exchange Rate has been updated successfully";
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }

    public boolean saveApplyDateforExchangeRate(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException, UnsupportedEncodingException {
        try {
        Map<String, Object> filterParams = new HashMap<String, Object>();
        Map<String, Object> erdMap = new HashMap<String, Object>();
        String companyid = sessionHandlerImpl.getCompanyid(request);
        Date appDate = null;
        Date toDateVal = null;
        String erid = "", exchangeRate = "";
        Double exchangerate = 0.0;
        KwlReturnObject erdresult;
            
        ExtraCompanyPreferences extraCompanyPreferencesObj = null;
        Map<String, Object> requestParamsExtra = new HashMap<String, Object>();
        requestParamsExtra.put("id", sessionHandlerImpl.getCompanyid(request));
        KwlReturnObject resultExtra = accCurrencyDAOobj.getExtraCompanyPreferencestoCheckBaseCurrency(requestParamsExtra);
        if (!resultExtra.getEntityList().isEmpty()) {
            extraCompanyPreferencesObj = (ExtraCompanyPreferences) resultExtra.getEntityList().get(0);
        }
        boolean isActivateToDateforExchangeRates = false;
        if (extraCompanyPreferencesObj != null) {
            if (extraCompanyPreferencesObj.isActivateToDateforExchangeRates()) {
                isActivateToDateforExchangeRates = true;
            }
        }
            
        Calendar toDate = Calendar.getInstance();
        if (StringUtil.isNullOrEmpty(request.getParameter(APPLYDATE))) {
            throw new AccountingException(messageSource.getMessage("acc.curex.excp1", null, RequestContextUtils.getLocale(request)));
        } else {
            appDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter(APPLYDATE));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter(TODATE)) && isActivateToDateforExchangeRates ) {
            toDateVal = authHandler.getDateOnlyFormat(request).parse(request.getParameter(TODATE));
            toDate.setTime(toDateVal);
        }
        Calendar applyDate = Calendar.getInstance();
        applyDate.setTime(appDate);
            
            //getting the exchange rate details
        KwlReturnObject result = accCurrencyDAOobj.getExchangeRateLinkids(companyid);
        List listexchangeratedetailsid = result.getEntityList();

        for (int varcount = 0; varcount < listexchangeratedetailsid.size(); varcount++) {
            String erdid = (String) listexchangeratedetailsid.get(varcount);
            filterParams.put(ERID, erdid);
//                filterParams.put("applydate", appDate);
            filterParams.put("companyid", companyid);
            
            KwlReturnObject resultlist = accCurrencyDAOobj.getExchangeRateonMinApplyDate(filterParams);//getting the exchangerate on the minimum value of applydate
            List listobj = resultlist.getEntityList();
            
            if (listobj.size() > 0) {
                if (listobj.get(0) != null) {
                    exchangerate = (Double) listobj.get(0);
                }
            }
            erdMap.put(APPLYDATE, authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(applyDate.getTime())));
            if (toDateVal != null && isActivateToDateforExchangeRates) {
                erdMap.put(TODATE, authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(toDate.getTime())));
            }
            erdMap.put(ERID, erdid);
            erdMap.put(COMPANYID, companyid);
            erdMap.put(EXCHANGERATE, exchangerate);
            erdresult = accCurrencyDAOobj.addExchangeRateDetails(erdMap);
        }
        } catch (ParseException ex) {
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveApplyDateforExchangeRate : " + ex.getMessage(), ex);
        }
        return true;
    }

       public ModelAndView importCurrencyExchange(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            String companyid=sessionHandlerImpl.getCompanyid(request);
            extraParams.put("Company",companyid);
            String doAction = request.getParameter("do");
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            boolean isActivateToDate=extraCompanyPreferences.isActivateToDateforExchangeRates();
            requestParams.put("isActivateToDate",isActivateToDate);
            requestParams.put("servletContext", this.getServletContext());

            if (doAction.compareToIgnoreCase("import") == 0) {

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
                jobj = importCurrencyExchangeRecords(request, datajobj);
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
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    JSONObject importCurrencyExchangeRecords(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
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
        String moduleName = !StringUtil.isNullOrEmpty(request.getParameter("moduleName"))?request.getParameter("moduleName"):null;
        int count = 1;
        JSONObject returnObj = new JSONObject();
        String failureMsg = "";
        String dateFormat = "yyyy-MM-dd";
        try {

            DateFormat df = new SimpleDateFormat(dateFormat);
            int sheetNo = Integer.parseInt(request.getParameter("sheetindex"));
            FileInputStream fs = new FileInputStream(jobj.getString("FilePath"));

            Workbook wb = WorkbookFactory.create(fs);
//            HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
            Sheet sheet = wb.getSheetAt(sheetNo);

            int cnt = 0;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            boolean isCurrencyCode=extraCompanyPreferences.isCurrencyCode();
            HashMap<String, Integer> columnConfig = new HashMap<>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }
            List failureArr = new ArrayList();
            List failureColumnArr = new ArrayList();
            List recarr = new ArrayList();
            Map CurrencyMap = getCurrencyMap(isCurrencyCode);
            int maxCol = 0;
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                failureMsg = "";
                Map<Integer, Object> invalidColumn = new HashMap<>();
                Row row = sheet.getRow(i);
                if (i == 0) {                             //header for xls file
                    maxCol = row.getLastCellNum();
                    recarr = new ArrayList();
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
                        } else {
                            recarr.add("");
                        }
                    }
//                    failedRecords.append(createCSVrecord(recarr.toArray()) + "\"Error Message\"");
                    ArrayList failureRecArr = new ArrayList();
                    failureRecArr.addAll(recarr);
                    failureRecArr.add("Error Message");
                    failureArr.add(failureRecArr);
                    failureColumnArr.add(invalidColumn);
                }
                if (cnt != 0) {                      //data for xls file      
                    recarr = new ArrayList();
                    for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                        Cell cell = row.getCell(cellcount);

                        if (cell != null) {
                            if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                                cell.setCellValue(cell.getStringCellValue().replaceAll("\n", ""));
                            } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                String CellStringValue = Double.toString((Double) cell.getNumericCellValue()).replaceAll("\n", "");
                                cell.setCellValue(Double.parseDouble(CellStringValue)); //Parsed to Doouble as getnumericCellValue returns double by Default
                                if (DateUtil.isCellDateFormatted(cell)) {
                                     cell.setCellValue(cell.getDateCellValue());
                                }
                            }
                            recarr.add(cell);
                        } else {
                            recarr.add("");
                        }
                    }

                    try {

                        String Currencycode = "";
                        String ErCode = "";
                       if (columnConfig.containsKey("currencyCode")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("currencyCode"));

                            if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                invalidColumn.put((Integer) columnConfig.get("currencyCode"), "Invalid");
                                failureMsg += "Currency Code  is not available.";
                                throw new AccountingException(failureMsg);
                            } else {
                                switch (cell.getCellType()) {
                                    case Cell.CELL_TYPE_NUMERIC:
                                        Currencycode = Integer.toString((int) cell.getNumericCellValue());
                                        break;
                                    case Cell.CELL_TYPE_STRING:
                                        Currencycode = cell.getStringCellValue().trim();
                                        break;
                                }
                                String toCurrencyid = getCurrencyId(Currencycode, CurrencyMap);
                                if (StringUtil.isNullOrEmpty(toCurrencyid)) {
                                    invalidColumn.put((Integer) columnConfig.get("currencyCode"), "Invalid");
                                    failureMsg += "Currency Code entry for " + Currencycode + " is not present in Currency Code List.";
                                    throw new AccountingException(failureMsg);
                                } else {
                                    Map<String, Object> currencyMap = new HashMap<String, Object>();
                                    if (!StringUtil.isNullOrEmpty(currencyId)) {
                                        currencyMap.put(FROMCURRENCYID, currencyId);
                                    }
                                    if (!StringUtil.isNullOrEmpty(toCurrencyid)) {
                                        currencyMap.put(TOCURRENCYID, toCurrencyid);
                                    }
                                    if(!StringUtil.isNullOrEmpty(moduleName) && moduleName.equalsIgnoreCase(Constants.Tax_Currency_Exchange)){
                                        KwlReturnObject currencyrateresult = accTaxCurrencyExchangeDAOobj.getTaxCurrencyExchange(currencyMap);
                                        TaxExchangeRate ER = (TaxExchangeRate) currencyrateresult.getEntityList().get(0);
                                        ErCode = ER.getID();
                                    }else{
                                        KwlReturnObject currencyrateresult = accCurrencyDAOobj.getCurrencyExchange(currencyMap);
                                        ExchangeRate ER = (ExchangeRate) currencyrateresult.getEntityList().get(0);
                                        ErCode = ER.getID();
                                    }
                                }
                            }
                        } else {
                            invalidColumn.put((Integer) columnConfig.get("currencyCode"), "Invalid");
                            failureMsg += "Currency Code  column is not found.";
                            throw new AccountingException(failureMsg);
                        }
                       // 'Is Foreign to Base Exchange Rate' Column
                       String isForeignToBaseExchangeRate = "";
                       if (columnConfig.containsKey("isForeignToBaseExchangeRate")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("isForeignToBaseExchangeRate"));
                            if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                invalidColumn.put((Integer) columnConfig.get("isForeignToBaseExchangeRate"), "Invalid");
                                failureMsg += "'Is Foreign to Base Exchange Rate' is not available.";
                                throw new AccountingException(failureMsg);
                            } else {
                                switch (cell.getCellType()) {
                                    case Cell.CELL_TYPE_BOOLEAN:
                                        isForeignToBaseExchangeRate = Boolean.toString((boolean) cell.getBooleanCellValue());
                                        break;
                                    case Cell.CELL_TYPE_STRING:
                                        isForeignToBaseExchangeRate = cell.getStringCellValue().trim();
                                        break;
                                }
                                if (StringUtil.isNullOrEmpty(isForeignToBaseExchangeRate)) {
                                    invalidColumn.put((Integer) columnConfig.get("currencyCode"), "Invalid");
                                    failureMsg += "'Is Foreign to Base Exchange Rate' is not available.";
                                    throw new AccountingException(failureMsg);
                                } 
                            }
                        } else {
                            isForeignToBaseExchangeRate="No";
                        }

                        String exchangerate = "";
                       String foreigntobaseexchangerate = "";
                        if (columnConfig.containsKey("ExcghangeRate")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("ExcghangeRate"));

                            if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                invalidColumn.put((Integer) columnConfig.get("ExcghangeRate"), "Invalid");
                                failureMsg += "Currency Rate  is not available.";
                                 throw new AccountingException(failureMsg);
                            } else {
                                switch (cell.getCellType()) {
                                    case Cell.CELL_TYPE_NUMERIC:
                                        exchangerate = Double.toString(cell.getNumericCellValue());
                                        break;
                                    case Cell.CELL_TYPE_STRING:
                                        exchangerate = cell.getStringCellValue().trim();
                                        break;
                                }
                                if (!StringUtil.isNullOrEmpty(exchangerate)) {
                                    try {
                                        // Calculation of exchange rate and reverse exchange rate
                                        // Do not change the rounding decimal places as we have allowed user to enter 14 decimal places of exchange rate in import case.
                                        double exchangeRate = Double.parseDouble(exchangerate);
                                        exchangeRate=authHandler.round(exchangeRate, Constants.EXCHANGERATE_DIGIT_AFTER_DECIMAL_FOR_IMPORT);
                                        double foreignToBaseExchangeRate = exchangeRate!=0?(1/exchangeRate):0;
                                        foreignToBaseExchangeRate=authHandler.round(foreignToBaseExchangeRate, Constants.EXCHANGERATE_DIGIT_AFTER_DECIMAL_FOR_IMPORT);
                                        if(isForeignToBaseExchangeRate.equalsIgnoreCase("Yes")||isForeignToBaseExchangeRate.equalsIgnoreCase("True")||isForeignToBaseExchangeRate.equalsIgnoreCase("T")||isForeignToBaseExchangeRate.equalsIgnoreCase("1")){
                                            double tempValue=exchangeRate;
                                            exchangeRate=foreignToBaseExchangeRate;
                                            foreignToBaseExchangeRate=tempValue;
                                        }
                                        exchangerate=Double.toString(exchangeRate);
                                        foreigntobaseexchangerate=Double.toString(foreignToBaseExchangeRate);
                                    } catch (Exception ex) {
                                        invalidColumn.put((Integer) columnConfig.get("ExcghangeRate"), "Invalid");
                                        failureMsg += "Incorrect numeric value for Exchange Rate, Please ensure that value type of Exchange Rate matches with the Exchange Rate.";
                                        throw new AccountingException(failureMsg);
                                    }
                                }
                            }
                        } else {
                            invalidColumn.put((Integer) columnConfig.get("ExcghangeRate"), "Invalid");
                            failureMsg += "Currency Rate  column is not found.";
                             throw new AccountingException(failureMsg);
                        }
                        String appliedform = "";
                        Date appliedformDate = null;
                        Calendar applyformDate = null;
                        if (columnConfig.containsKey("AppliedForm")) {
                            Cell cell = row.getCell((Integer) columnConfig.get("AppliedForm"));

                            if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                invalidColumn.put((Integer) columnConfig.get("AppliedForm"), "Invalid");
                                failureMsg += "Applied Form is not available.";
                                 throw new AccountingException(failureMsg);
                            } else {
                                Date date = null;
                                switch (cell.getCellType()) {
                                    case Cell.CELL_TYPE_NUMERIC:
                                        appliedform = Double.toString(cell.getNumericCellValue());
                                        if (DateUtil.isCellDateFormatted(cell)) {
                                            date = DateUtil.getJavaDate(Double.parseDouble(appliedform));
                                        }
                                        break;
                                    case Cell.CELL_TYPE_STRING:
                                        appliedform = cell.getStringCellValue().trim();
                                        date = df.parse(appliedform);
                                        break;
                                }
                                if (!StringUtil.isNullOrEmpty(appliedform)) {
                                    try {
                                        Object vDataValue = df.parse(df.format(date));
                                    } catch (Exception ex) {
                                        invalidColumn.put((Integer) columnConfig.get("AppliedForm"), "Invalid");
                                        failureMsg += "Incorrect date format for  Applied Form , Please specify values in " + dateFormat + " format.";
                                        throw new AccountingException(failureMsg);
                                    }
                                }

                                appliedformDate = df.parse(df.format(date));
                                applyformDate = Calendar.getInstance();
                                applyformDate.setTime(appliedformDate);
                            }
                        } else {
                            invalidColumn.put((Integer) columnConfig.get("AppliedForm"), "Invalid");
                            failureMsg += "Applied Form column is not found.";
                             throw new AccountingException(failureMsg);
                        }
                        String appliedto = "";
                        Date appliedtoDate = null;
                        Calendar applytoDate = null;
                        if (extraCompanyPreferences.isActivateToDateforExchangeRates()) {
                            if (columnConfig.containsKey("AppliedTo")) {
                                Cell cell = row.getCell((Integer) columnConfig.get("AppliedTo"));

                                if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                                    invalidColumn.put((Integer) columnConfig.get("AppliedTo"), "Invalid");
                                    failureMsg += "Applied To is not available.";
                                     throw new AccountingException(failureMsg);
                                } else {
                                    Date date = null;
                                    switch (cell.getCellType()) {
                                        case Cell.CELL_TYPE_NUMERIC:
                                            appliedto = Integer.toString((int) cell.getNumericCellValue());
                                            if (DateUtil.isCellDateFormatted(cell)) {
                                                date = DateUtil.getJavaDate(Double.parseDouble(appliedto));
                                            }
                                            break;
                                        case Cell.CELL_TYPE_STRING:
                                            appliedto = cell.getStringCellValue().trim();
                                            date = df.parse(appliedto);
                                            break;
                                    }
                                    if (!StringUtil.isNullOrEmpty(appliedto)) {
                                        try {
                                            Object vDataValue = df.parse(df.format(date));
                                        } catch (Exception ex) {
                                            invalidColumn.put((Integer) columnConfig.get("AppliedTo"), "Invalid");
                                            failureMsg += "Incorrect date format for  Applied To , Please specify values in " + dateFormat + " format.";
                                            throw new AccountingException(failureMsg);
                                        }
                                    }
                                    appliedtoDate = df.parse(df.format(date));
                                    applytoDate = Calendar.getInstance();
                                    applytoDate.setTime(appliedtoDate);
                                }
                            } else {
                                invalidColumn.put((Integer) columnConfig.get("AppliedTo"), "Invalid");
                                failureMsg += "Applied To  column is not found.";
                                 throw new AccountingException(failureMsg);
                            }
                        } else {
                            applytoDate = Calendar.getInstance();  //ToDateforExchangeRates is not Activated
                            applytoDate.setTime(appliedformDate);
                            applytoDate.add(Calendar.DATE, 30);
                        }
                        if ((applyformDate.getTime()).compareTo((applytoDate.getTime())) > 0) {
                            invalidColumn.put((Integer) columnConfig.get("AppliedTo"), "Invalid");
                            failureMsg += " Applied From date can't be greater than  Applied To date.";
                            throw new AccountingException(failureMsg);
                        }
                        Map<String, Object> filterParams = new HashMap<String, Object>();
                        filterParams.put(ERID, ErCode);
                        filterParams.put(APPLYDATE, applyformDate.getTime());
                        filterParams.put(TODATE, applytoDate.getTime());
                        filterParams.put(COMPANYID, companyid);
                        if (!StringUtil.isNullOrEmpty(moduleName) && moduleName.equalsIgnoreCase(Constants.Tax_Currency_Exchange)) {
                            TaxExchangeRateDetails erd;
                            KwlReturnObject result = accTaxCurrencyExchangeDAOobj.getTaxExchangeRateDetails(filterParams, false);
                            List list = result.getEntityList();
                            Map<String, Object> erdMap = new HashMap<String, Object>();
                            erdMap.put(EXCHANGERATE, Double.parseDouble(exchangerate));
                            erdMap.put(FOREIGNTOBASEEXCHANGERATE, Double.parseDouble(foreigntobaseexchangerate));
                            if (list.size() <= 0) {
                                erdMap.put(APPLYDATE, applyformDate.getTime());
                                erdMap.put(TODATE, applytoDate.getTime());
                                erdMap.put(ERID, ErCode);
                                erdMap.put(COMPANYID, companyid);
                                accTaxCurrencyExchangeDAOobj.addTaxExchangeRateDetails(erdMap);
                            } else {
                                erd = (TaxExchangeRateDetails) list.get(0);
                                erdMap.put(ERDID, erd.getID());
                                KwlReturnObject erdresult = accTaxCurrencyExchangeDAOobj.updateTaxExchangeRateDetails(erdMap);
                            }
                        } else {
                            ExchangeRateDetails erd;
                            Map<String, Object> erdMap = new HashMap<String, Object>();
                            KwlReturnObject result = accCurrencyDAOobj.getExchangeRateDetails(filterParams, false);
                            List list = result.getEntityList();
                            erdMap.put(EXCHANGERATE, Double.parseDouble(exchangerate));
                            erdMap.put(FOREIGNTOBASEEXCHANGERATE, Double.parseDouble(foreigntobaseexchangerate));
                            if (list.size() <= 0) {
                                erdMap.put(APPLYDATE, applyformDate.getTime());
                                erdMap.put(TODATE, applytoDate.getTime());
                                erdMap.put(ERID, ErCode);
                                erdMap.put(COMPANYID, companyid);
                                accCurrencyDAOobj.addExchangeRateDetails(erdMap);
                            } else {
                                erd = (ExchangeRateDetails) list.get(0);
                                erdMap.put(ERDID, erd.getID());
                                KwlReturnObject erdresult = accCurrencyDAOobj.updateExchangeRateDetails(erdMap);
                            }
                        }
                    } catch (Exception ex) {
                        failed++;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) {
                            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, jex);
                        }
//                        failedRecords.append("\n" + accProductModuleService.createCSVrecord(recarr.toArray()) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                        ArrayList failureRecArr = new ArrayList();
                        failureRecArr.addAll(recarr);
                        failureRecArr.add(errorMsg.replaceAll("\"", ""));
                        failureArr.add(failureRecArr);

                        failureColumnArr.add(invalidColumn);
                    }
                    total++;
                }
                cnt++;
            }
            if (failed > 0) {

                createFailureXlsFiles(fileName, failureArr, ".xls", failureColumnArr);
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
            if (!commitedEx) { //if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
//            fileInputStream.close();
//            br.close();

            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);

            try {
                //Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "xls");
                logDataMap.put("FailureFileType", failed>0?"xls":"");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                if (!StringUtil.isNullOrEmpty(moduleName) && moduleName.equalsIgnoreCase(Constants.Tax_Currency_Exchange)) {
                    logDataMap.put("Module", Constants.Tax_Currency_Exchange_ModuleId);
                }else{
                    logDataMap.put("Module", Constants.Currency_Exchange_ModuleId);
                }
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);

                String tableName = importDao.getTableName(fileName);
                importDao.removeFileTable(tableName); // Remove table after importing all records

                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
                if (!StringUtil.isNullOrEmpty(moduleName) && moduleName.equalsIgnoreCase(Constants.Tax_Currency_Exchange)) {
                    returnObj.put("Module", Constants.Tax_Currency_Exchange_ModuleId);
                }else{
                    returnObj.put("Module", Constants.Currency_Exchange_ModuleId);
                }
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;

    }

    public void createFailureXlsFiles(String filename, List failureArr, String ext, List failureColumnArr) {
        try {
            int rownum = 0;
            int cellnum = 0;
           String dateFormat = "yyyy-MM-dd";
            DateFormat df = new SimpleDateFormat(dateFormat);
            if (StringUtil.isNullOrEmpty(ext)) {
                ext = filename.substring(filename.lastIndexOf("."));
            }
            filename = filename.substring(0, filename.lastIndexOf("."));

            String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "xlsfiles";
            Workbook wb = new HSSFWorkbook();
            Sheet sheet = wb.createSheet("Sheet-1");
            Cell cell = null;

            CellStyle style = wb.createCellStyle();
            Font font = wb.createFont();

            for (int rowCnt = 0; rowCnt < failureArr.size(); rowCnt++) {
                List recarr = (List) failureArr.get(rowCnt);
                Map<Integer, Object> failureColumnMap = (Map<Integer, Object>) failureColumnArr.get(rowCnt);
                Row headerRow = sheet.createRow(rownum++);
                cellnum = 0;

                for (int cellCnt = 0; cellCnt < recarr.size(); cellCnt++) {
                    cell = headerRow.createCell(cellnum++);
                    if (recarr.get(cellCnt) instanceof Cell) {
                        Cell givenCell = (Cell) recarr.get(cellCnt);

                        switch (givenCell.getCellType()) {
                            case Cell.CELL_TYPE_NUMERIC:
                               if (DateUtil.isCellDateFormatted(givenCell)) {
                                    cell.setCellValue(df.format(givenCell.getDateCellValue()));
                                }
                                else{
                                  cell.setCellValue(givenCell.getNumericCellValue());   
                                }
                                break;
                            case Cell.CELL_TYPE_STRING:
                                cell.setCellValue(givenCell.getStringCellValue());
                                break;
                            default:
                                cell.setCellValue("");
                                break;
                        }
                    } else {
                        cell.setCellValue((String) recarr.get(cellCnt));
                    }

                    if (failureColumnMap.containsKey(cellCnt) && failureColumnMap.get(cellCnt).toString().equalsIgnoreCase("Invalid")) {
                        font.setColor(HSSFColor.RED.index);
                        style.setFont(font);
                        cell.setCellStyle(style);
                    }
                }
            }

            FileOutputStream fos = new FileOutputStream(destinationDirectory + "/" + filename + ImportLog.failureTag + ext);
            wb.write(fos);
            fos.flush();
            fos.close();
        } catch (Exception ex) {
            System.out.println("\nError file write [success/failed] " + ex);
        }
    }
    private String getCurrencyId(String currencyName, Map currencyMap) {
        String currencyId = "";
        if (currencyMap != null && currencyMap.containsKey(currencyName)) {
            currencyId = currencyMap.get(currencyName).toString();
        }
        return currencyId;
    }
    
    public HashMap getCurrencyMap(boolean isCurrencyCode) throws ServiceException {
        HashMap currencyMap = new HashMap();
        KwlReturnObject returnObject = accCurrencyDAOobj.getCurrencies(currencyMap);
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
    
    public ModelAndView saveConsolidation(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        boolean issucess = false;
        String msg="";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SaveCons_Tx");
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("consolidationid", request.getParameter("consolidationid"));
            requestParams.put("childcompanyid", request.getParameter("childcompanyid"));
            requestParams.put("stakeinpercentage", request.getParameter("stakeinpercentage"));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accCurrencyDAOobj.saveConsolidation(requestParams);
            issucess = result.isSuccessFlag();
            msg = messageSource.getMessage("acc.conslodation.consolidationdatasaved", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (SessionExpiredException | ServiceException | TransactionException ex) {
            issucess = false;
            msg=ex.getMessage();
            txnManager.rollback(status);
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jobj.put("success", issucess);
            jobj.put("msg", msg);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView saveConsolidationExchangeRateDetails(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        boolean issucess = false;
        String msg="";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SaveCons_Tx");
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray(request.getParameter(DATA));
            for(int i=0;i<jArr.length();i++){
                HashMap<String, Object> requestParams = new HashMap<>();
                JSONObject obj= jArr.getJSONObject(i);
                double exchareRate= Double.parseDouble(StringUtil.DecodeText(obj.optString(EXCHANGERATE)));
                Date appDate = authHandler.getDateOnlyFormat(request).parse(StringUtil.DecodeText(obj.optString(APPLYDATE)));
                String consolidationid = StringUtil.DecodeText(obj.optString("id"));
                
                Map filterMap = new HashMap();
                filterMap.put("consolidationid", consolidationid);
                filterMap.put("applydate", appDate);
                    
                KwlReturnObject result = accCurrencyDAOobj.getConsolidationExchangeRate(filterMap);
                if(result!=null && !result.getEntityList().isEmpty()){// If exchangerate already available on the applied date then update the given record
                    ConsolidationExchangeRateDetails cerd=(ConsolidationExchangeRateDetails) result.getEntityList().get(0);
                    if(cerd!=null){
                        requestParams.put("id", cerd.getID());  
                    }
                }
                requestParams.put(EXCHANGERATE, exchareRate);   
                requestParams.put(APPLYDATE, appDate);   
                requestParams.put("consolidationid", consolidationid);   
                accCurrencyDAOobj.saveConsolidationExchangeRateDetails(requestParams);
            }
            issucess = true;
            msg = messageSource.getMessage("acc.conslodation.consolidationdatasaved", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (SessionExpiredException | ServiceException | TransactionException | ParseException ex ) {
            issucess = false;
            msg=ex.getMessage();
            txnManager.rollback(status);
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jobj.put("success", issucess);
            jobj.put("msg", msg);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getConsolidation(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        JSONArray array= new JSONArray();
        boolean issucess = false;
        String msg="";
        try {
            String companyid=sessionHandlerImpl.getCompanyid(request);
            Map requestMap=new HashMap();
            requestMap.put("companyid", companyid);
            KwlReturnObject result = accCurrencyDAOobj.getConsolidation(requestMap);
            if(result!=null && !result.getEntityList().isEmpty()){
                List<ConsolidationData> consolidationDatas = result.getEntityList();
                for(ConsolidationData data: consolidationDatas){
                    JSONObject object = new  JSONObject();
                    object.put("id", data.getID());
                    object.put("subdomainid", data.getChildCompany()!=null?data.getChildCompany().getCompanyID():"");
                    object.put("companyname", data.getChildCompany()!=null?data.getChildCompany().getCompanyName():"");
                    object.put("subdomainname", data.getChildCompany()!=null?data.getChildCompany().getSubDomain():"");
                    object.put("stakeinpercentage", data.getStakeInPercentage());
                    array.put(object);
                }
            }
            issucess=true;
            jobj.put("data",array);
            jobj.put("count",array.length());
        } catch (SessionExpiredException | ServiceException | TransactionException ex) {
            issucess = false;
            msg=ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jobj.put("success", issucess);
            jobj.put("msg", msg);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getConsolidationReportGenerationData(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        JSONArray array= new JSONArray();
        boolean issucess = false;
        String msg="";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            array=getConsolidationReportGenerationData(paramJobj);
            issucess=true;
            jobj.put("data",array);
            jobj.put("count",array.length());
        } catch (SessionExpiredException | ServiceException | TransactionException ex) {
            issucess = false;
            msg=ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jobj.put("success", issucess);
            jobj.put("msg", msg);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView exportConsolidationReportGenerationData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            JSONArray DataJArr = getConsolidationReportGenerationData(paramJobj);
            jobj.put("data", DataJArr);

            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public JSONArray getConsolidationReportGenerationData(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONArray array = new JSONArray();
        String companyid = paramJobj.getString(Constants.companyKey);
        String selectedSubdomains = paramJobj.optString("selectedSubdomains", "");
        Map requestMap = new HashMap();
        requestMap.put("selectedSubdomains", selectedSubdomains);
        requestMap.put("companyid", companyid);
        KwlReturnObject result = accCurrencyDAOobj.getConsolidation(requestMap);
        if (result != null && !result.getEntityList().isEmpty()) {
            List<ConsolidationData> consolidationDatas = result.getEntityList();
            for (ConsolidationData data : consolidationDatas) {
                JSONObject object = new JSONObject();
                object.put("id", data.getID());
                object.put("stakeinpercentage", data.getStakeInPercentage());
                if (data.getChildCompany() != null) {
                    object.put("subdomainid", data.getChildCompany().getCompanyID());
                    object.put("subdomainname", data.getChildCompany().getSubDomain());
                    object.put("companyname", data.getChildCompany().getCompanyName());
                    object.put("currencyname", data.getChildCompany().getCurrency().getName());
                } else {
                    object.put("subdomainid", "");
                    object.put("subdomainname", "");
                    object.put("companyname", "");
                    object.put("currencyname", "");
                }
                Map filterMap = new HashMap();
                filterMap.put("consolidationid", data.getID());
                KwlReturnObject result1 = accCurrencyDAOobj.getConsolidationExchangeRate(filterMap);
                if (result1 != null && !result1.getEntityList().isEmpty()) {
                    ConsolidationExchangeRateDetails cerd = (ConsolidationExchangeRateDetails) result1.getEntityList().get(0);
                    object.put("applydate", cerd.getApplyDate());
                    object.put("exchangerate", cerd.getExchangeRate());
                }
                array.put(object);
            }
        }
        return array;
    }
    
    public ModelAndView getConsolidationExchangeHistory(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        JSONArray array = new JSONArray();
        boolean issucess = false;
        String msg = "";
        try {
            Map filterMap= new HashMap();
            filterMap.put("consolidationid", request.getParameter("consolidationid"));
            KwlReturnObject result = accCurrencyDAOobj.getConsolidationExchangeRate(filterMap);
            if (result != null && !result.getEntityList().isEmpty()) {
                List<ConsolidationExchangeRateDetails> list=result.getEntityList();
                for (ConsolidationExchangeRateDetails cerd : list) {
                    JSONObject object= new JSONObject();
                    object.put("id", cerd.getID());
                    object.put("applydate", cerd.getApplyDate());
                    object.put("exchangerate", cerd.getExchangeRate());
                    array.put(object);
                }
            }
            issucess = true;
            jobj.put("data", array);
            jobj.put("count", array.length());
        } catch (ServiceException | TransactionException ex) {
            issucess = false;
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jobj.put("success", issucess);
            jobj.put("msg", msg);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
