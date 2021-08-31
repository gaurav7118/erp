/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.currency;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.currency.service.AccTaxCurrencyExchangeService;
import static com.krawler.spring.accounting.currency.CurrencyContants.APPLYDATE;
import static com.krawler.spring.accounting.currency.CurrencyContants.COMPANYID;
import static com.krawler.spring.accounting.currency.CurrencyContants.COUNT;
import static com.krawler.spring.accounting.currency.CurrencyContants.CURRENCYCODE;
import static com.krawler.spring.accounting.currency.CurrencyContants.DATA;
import static com.krawler.spring.accounting.currency.CurrencyContants.DATEEXIST;
import static com.krawler.spring.accounting.currency.CurrencyContants.ERID;
import static com.krawler.spring.accounting.currency.CurrencyContants.EXCHANGERATE;
import static com.krawler.spring.accounting.currency.CurrencyContants.FROMCURRENCYID;
import static com.krawler.spring.accounting.currency.CurrencyContants.JSONVIEW;
import static com.krawler.spring.accounting.currency.CurrencyContants.MODEL;
import static com.krawler.spring.accounting.currency.CurrencyContants.MSG;
import static com.krawler.spring.accounting.currency.CurrencyContants.SUCCESS;
import static com.krawler.spring.accounting.currency.CurrencyContants.TOCURRENCYID;
import static com.krawler.spring.accounting.currency.CurrencyContants.TODATE;
import static com.krawler.spring.accounting.currency.CurrencyContants.TRANSACTIONDATE;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * @author krawler
 */
public class accTaxCurrencyExchangeController extends MultiActionController implements CurrencyContants, MessageSourceAware {

    private MessageSource messageSource;
    private HibernateTransactionManager txnManager;
    private auditTrailDAO auditTrailObj;
    private AccTaxCurrencyExchangeService accTaxCurExchangeSerDAOObj;
    private AccTaxCurrencyExchangeDAO accTaxCurExchangeDAOObj;
    private accCurrencyDAO accCurrencyDAOObj;
    private String successView;
    private ImportHandler importHandler;
    private ImportDAO importDao;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }
    public void setauditTrailDAO(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }
    public void setAccTaxCurrencyExchangeService(AccTaxCurrencyExchangeService accTaxCurExchangeSerDAOObj) {
        this.accTaxCurExchangeSerDAOObj = accTaxCurExchangeSerDAOObj;
    }
    public void setAccTaxCurrencyExchangeDAO(AccTaxCurrencyExchangeDAO accTaxCurExchangeDAOObj) {
        this.accTaxCurExchangeDAOObj = accTaxCurExchangeDAOObj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOObj) {
        this.accCurrencyDAOObj = accCurrencyDAOObj;
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
    public void setImportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }
    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    
    public ModelAndView saveTaxCurrencyExchange(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String exchangerate = "";
        String currencycode = "";
        String applydate = "";
        String todate = "";
        boolean issuccess = false;
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Currency_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        JSONArray jArr = new JSONArray(request.getParameter(DATA));
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj1 = jArr.getJSONObject(i);
            exchangerate += jobj1.getString(EXCHANGERATE) + ",";
            currencycode += jobj1.getString(CURRENCYCODE) + ",";
            applydate += jobj1.getString(APPLYDATE) + ",";
            todate += jobj1.getString(TODATE) + ",";
        }
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("changerate", request.getParameter("changerate") == null ? false : Boolean.parseBoolean(request.getParameter("changerate")));
            requestParams.put(DATA, request.getParameter(DATA));
            requestParams.put("companyid", companyid);
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            requestParams.put(Constants.df, df);
            boolean dateexist = false;
            dateexist = accTaxCurExchangeSerDAOObj.saveTaxCurrencyExchange(requestParams);
            jobj.put(DATEEXIST, dateexist);
            if (dateexist == false) {
                auditTrailObj.insertAuditLog(AuditAction.CURRENCY_EXCHANGE_ADDED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated " + (java.util.Arrays.toString(currencycode.split(","))) + " " + " to rate " + (java.util.Arrays.toString(exchangerate.split(","))) + " on date " + (java.util.Arrays.toString(applydate.split(","))), request, companyid);
            }
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.curex.update", null, RequestContextUtils.getLocale(request));   //"Currency Exchange Rate has been updated successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accTaxCurrencyExchangeController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accTaxCurrencyExchangeController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxCurrencyExchangeController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }

    public ModelAndView saveTaxCurrencyExchangeDetail(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Currency_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(APPLYDATE, request.getParameter(APPLYDATE));
            requestParams.put(TODATE, request.getParameter(TODATE));
            requestParams.put("companyid", companyid);
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            requestParams.put(Constants.df, df);
            accTaxCurExchangeSerDAOObj.saveTaxCurrencyExchangeDetail(requestParams);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.curex.update", null, RequestContextUtils.getLocale(request));   //"Currency Exchange Rate has been updated successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accTaxCurrencyExchangeController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accTaxCurrencyExchangeController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxCurrencyExchangeController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }

    public ModelAndView getTaxCurrencyExchange(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put(TRANSACTIONDATE, request.getParameter(TRANSACTIONDATE));
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(FROMCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
            String toCurrencyid = request.getParameter(TOCURRENCYID);
            if (!StringUtil.isNullOrEmpty(toCurrencyid)) {
                requestParams.put(TOCURRENCYID, request.getParameter(TOCURRENCYID));
            }
            requestParams.put("iscurrencyexchangewindow", request.getParameter("iscurrencyexchangewindow"));
            requestParams.put("isAll", request.getParameter("isAll"));

            ExtraCompanyPreferences extraCompanyPreferencesObj = null;
            Map<String, Object> requestParamsExtra = new HashMap<String, Object>();
            requestParamsExtra.put("id", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject resultExtra = accCurrencyDAOObj.getExtraCompanyPreferencestoCheckBaseCurrency(requestParamsExtra);
            if (!resultExtra.getEntityList().isEmpty()) {
                extraCompanyPreferencesObj = (ExtraCompanyPreferences) resultExtra.getEntityList().get(0);
            }
            boolean isOnlyBaceCurrencyflag = false;
            if (extraCompanyPreferencesObj != null) {
                if (extraCompanyPreferencesObj.isOnlyBaseCurrency()) {
                    isOnlyBaceCurrencyflag = true;
                }
            }

            KwlReturnObject result = accTaxCurExchangeDAOObj.getTaxCurrencyExchange(requestParams);
            List list = result.getEntityList();

            JSONArray jArr = accTaxCurExchangeSerDAOObj.getTaxCurrencyExchangeJson(requestParams, list, isOnlyBaceCurrencyflag);
            jobj.put(DATA, jArr);
            jobj.put(COUNT, jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxCurrencyExchangeController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accTaxCurrencyExchangeController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxCurrencyExchangeController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }

    public ModelAndView getTaxCurrencyExchangeList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {

            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(ERID, request.getParameter("currencyid"));

            KwlReturnObject result = accTaxCurExchangeDAOObj.getTaxExchangeRateDetails(requestParams, true);
            List list = result.getEntityList();

            JSONArray jArr = accTaxCurExchangeSerDAOObj.getTaxCurrencyExchangeListJson(requestParams, list);
            jobj.put(DATA, jArr);
            jobj.put(COUNT, jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxCurrencyExchangeController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accTaxCurrencyExchangeController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxCurrencyExchangeController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }
}
