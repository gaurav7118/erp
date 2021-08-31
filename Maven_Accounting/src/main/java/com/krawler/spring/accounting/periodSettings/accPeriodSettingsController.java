/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.periodSettings;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.AccountingPeriod;
import com.krawler.common.admin.TaxPeriod;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author krawler
 */
public class accPeriodSettingsController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private String successView;
    private accCurrencyDAO accCurrencyDAOobj;
    private accPeriodSettingsDao accPeriodSettingsDao;
    private MessageSource messageSource;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private auditTrailDAO auditTrailObj;
    
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public accCurrencyDAO getAccCurrencyDAOobj() {
        return accCurrencyDAOobj;
    }

    public void setAccCurrencyDAOobj(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setaccPeriodSettingsDao(accPeriodSettingsDao accPeriodSettingsDao) {
        this.accPeriodSettingsDao = accPeriodSettingsDao;
    }

//saving tax period
    public ModelAndView saveTaxPeriodSettings(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        KwlReturnObject result = null;
        JSONObject jobjvalidate = new JSONObject();
        String msg = "";
        String auditID = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BRecnl_Tx");
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            auditID = AuditAction.SAVE_TAXPERIOD; 
            int periodType = !StringUtil.isNullOrEmpty(request.getParameter("periodtype")) ? Integer.parseInt(request.getParameter("periodtype")) : null;
            if (periodType == TaxPeriod.PERIODTYPE_FULLYEAR) {
                jobjvalidate = saveFullYearSetupforTaxPeriod(request);
            } else {
                jobjvalidate = saveTaxPeriod(request);
            }

            if (jobjvalidate.has("isSuccess") && jobjvalidate.getBoolean("isSuccess") == false) {
                txnManager.rollback(status);
                isSuccess = false;
                msg = jobjvalidate.optString("msg", "");
            } else {
                isSuccess = true;
                msg = messageSource.getMessage("acc.accountingperiodtab.taxperiodsave", null, RequestContextUtils.getLocale(request));
                txnManager.commit(status);
                status=null;
                auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has created Tax Period "+jobjvalidate.optString("periodname", ""), request,jobjvalidate.optString("periodid", ""));
            }

        } catch (Exception ex) {
            txnManager.rollback(status);
            jobj.put("msg", ex.getMessage());
            Logger.getLogger(accPeriodSettingsController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveAccountingorTaxPeriod : " + ex.getMessage(), ex);
        } finally {
            jobj.put("success", isSuccess);
            jobj.put("msg", msg);

        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
   
    
    /*
     * Checking Start Date, End Date exist or not for particluar subperiod
     */
    public JSONObject validateFinancialYear(HttpServletRequest request, HashMap<String, Object> dataMap, boolean taxaccountingflag) throws JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        Date startdate = null, endDate = null;
        Map<String, Object> requestParams = new HashMap();
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            requestParams.put("company", sessionHandlerImpl.getCompanyid(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter("periodtype"))) {
                requestParams.put("periodtype", 1);
            }

            if (request.getParameter("startmonth") != null) {
                requestParams.put("startmonth", request.getParameter("startmonth"));
            }

            if (request.getParameter("endyear") != null) {
                requestParams.put("endyear", request.getParameter("endyear"));
            }

            if (dataMap.containsKey("startdate") && dataMap.get("startdate") != null) {
                startdate = (Date) dataMap.get("startdate");
                requestParams.put("startdate", startdate);
            }

            if (dataMap.containsKey("enddate") && dataMap.get("enddate") != null) {
                endDate = (Date) dataMap.get("enddate");
                requestParams.put("endDate", endDate);
            }
            requestParams.put("locale", RequestContextUtils.getLocale(request));
            if (taxaccountingflag) {
                msg = accPeriodSettingsDao.checkFinancialYearDatesforAccounting(requestParams);
            } else {
                msg = accPeriodSettingsDao.checkFinancialYearDatesforTaxPeriod(requestParams);
            }

        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jobj.put("msg", msg);
        }
        return jobj;
    }
    
    //For Normal Save
    public JSONObject saveTaxPeriod(HttpServletRequest request) throws SessionExpiredException, ServiceException, JSONException {
        KwlReturnObject result = null;
        JSONObject jobjvalidate = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        try {
            String id = StringUtil.generateUUID();
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String periodname = (String) request.getParameter("periodname");
            int periodtype = Integer.parseInt((String) request.getParameter("periodtype"));
            Date startdate = (Date) df.parse(request.getParameter("startdate"));
            Date enddate = (Date) df.parse(request.getParameter("enddate"));
            String subPeriodOf = request.getParameter("subperiodof");

            HashMap<String, Object> accountingandtaxperiodRulesMap = new HashMap<String, Object>();
            accountingandtaxperiodRulesMap.put("id", id);
            accountingandtaxperiodRulesMap.put("companyid", companyid);
            accountingandtaxperiodRulesMap.put("periodname", periodname);
            accountingandtaxperiodRulesMap.put("periodtype", periodtype);
            accountingandtaxperiodRulesMap.put("startdate", startdate);
            accountingandtaxperiodRulesMap.put("enddate", enddate);
            if (!StringUtil.isNullOrEmpty(subPeriodOf)) {
                accountingandtaxperiodRulesMap.put("subperiodof", subPeriodOf);
            }

            if (periodtype == TaxPeriod.PERIODTYPE_YEAR) {//for year only validate the year
                jobjvalidate = validateFinancialYear(request, accountingandtaxperiodRulesMap,false);
                msg = jobjvalidate.has("msg") ? jobjvalidate.optString("msg", "") : "";
            }

            if (StringUtil.isNullOrEmpty(msg)) {//if no exception occured
                isSuccess = true;
                result = accPeriodSettingsDao.saveTaxPeriod(accountingandtaxperiodRulesMap);
                TaxPeriod acctaxperiod = (TaxPeriod) result.getEntityList().get(0); 
                jobjvalidate.put("periodname", acctaxperiod.getPeriodName()); 
                jobjvalidate.put("periodid", acctaxperiod.getId()); 
            }
        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            jobjvalidate.put("isSuccess", isSuccess);
            jobjvalidate.put("msg", msg);
        }
        return jobjvalidate;
    }

    /*
     * Full Year Setup for Tax Period
     */
    public JSONObject saveFullYearSetupforTaxPeriod(HttpServletRequest request) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        JSONObject jobjvalidate = new JSONObject();
        String msg = "",periodname="",periodid="";
        boolean isSuccess = false;
        try {

            DateFormat df = authHandler.getDateOnlyFormat(request);
            int periodformat = !StringUtil.isNullOrEmpty(request.getParameter("periodformat")) ? Integer.parseInt(request.getParameter("periodformat")) : null;
            int yearinperiodname = !StringUtil.isNullOrEmpty(request.getParameter("yearinperiodname")) ? Integer.parseInt(request.getParameter("yearinperiodname")) : null;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userid = sessionHandlerImpl.getUserid(request);

            int startmonth = Integer.parseInt(request.getParameter("startmonth"));
            int endyear = Integer.parseInt(request.getParameter("endyear"));
//           
            Calendar startdatecalcal = Calendar.getInstance();
            startdatecalcal.set(Calendar.YEAR, startmonth == 0 ? endyear : endyear - 1);
            startdatecalcal.set(Calendar.MONTH, startmonth);
            startdatecalcal.set(Calendar.DAY_OF_MONTH, 1);
            Date startDate = startdatecalcal.getTime();

            Calendar endDatecal = Calendar.getInstance();
            endDatecal.setTime(startDate);
            endDatecal.add(Calendar.MONTH, 12);
            endDatecal.add(Calendar.DAY_OF_MONTH, -1);
            Date endDate = endDatecal.getTime();

            
            HashMap<String, Object> dataMap = new HashMap();
            dataMap.put("companyid", companyid);
            dataMap.put("periodtype", TaxPeriod.PERIODTYPE_YEAR);
            dataMap.put("startdate", startDate);
            dataMap.put("enddate", endDate);
            dataMap.put("periodformat", periodformat);
            dataMap.put("yearinperiodname", yearinperiodname);
            dataMap.put("userid", userid);
            dataMap.put("periodname", "FY " + endDatecal.get(Calendar.YEAR));

            jobjvalidate = validateFinancialYear(request, dataMap,false);
            msg = jobjvalidate.has("msg") ? jobjvalidate.optString("msg", "") : "";

            if (StringUtil.isNullOrEmpty(msg)) {//if no exception occured while validating dates
                isSuccess = true;
                KwlReturnObject kwl = accPeriodSettingsDao.saveTaxPeriod(dataMap);
                TaxPeriod accperiod = (TaxPeriod) kwl.getEntityList().get(0);
                periodname = accperiod.getPeriodName();
                periodid = accperiod.getId();
                
                Date quarterstartDate = startDate;
                Date quarterendDate = null;
                for (int quarter = 1; quarter <= 4; quarter++) {

                    Calendar quarterCal = Calendar.getInstance();
                    quarterCal.setTime(quarterstartDate);
                    quarterCal.add(Calendar.MONTH, 3);
                    quarterCal.add(Calendar.DATE, -1);
                    quarterendDate = quarterCal.getTime();

                    dataMap = new HashMap();
                    dataMap.put("companyid", companyid);
                    dataMap.put("periodtype", 2);
                    dataMap.put("startdate", quarterstartDate);
                    dataMap.put("enddate", quarterendDate);
                    dataMap.put("userid", userid);
                    dataMap.put("periodname", "Q" + quarter + " " + quarterCal.get(Calendar.YEAR));
                    dataMap.put("subperiodof", accperiod.getId());

                    KwlReturnObject kwlresult = accPeriodSettingsDao.saveTaxPeriod(dataMap);
                    TaxPeriod accperiodFormonth = (TaxPeriod) kwlresult.getEntityList().get(0);

                    Date monthstartDate = quarterstartDate;
                    Date monthendDate = null;
                    for (int month = 1; month <= 3; month++) {

                        Calendar monthCal = Calendar.getInstance();
                        monthCal.setTime(monthstartDate);
                        monthCal.set(Calendar.DAY_OF_MONTH, monthCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                        monthendDate = monthCal.getTime();

                        dataMap = new HashMap();
                        dataMap.put("companyid", companyid);
                        dataMap.put("periodtype", 3);
                        dataMap.put("startdate", monthstartDate);
                        dataMap.put("enddate", monthendDate);
                        dataMap.put("userid", userid);
                        dataMap.put("periodname", monthCal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + monthCal.get(Calendar.YEAR));
                        dataMap.put("subperiodof", accperiodFormonth.getId());

                        accPeriodSettingsDao.saveTaxPeriod(dataMap);

                        monthCal.setTime(monthendDate);
                        monthCal.add(Calendar.DATE, 1);
                        monthstartDate = monthCal.getTime();
                    }

                    quarterCal.setTime(quarterendDate);
                    quarterCal.add(Calendar.DATE, 1);
                    quarterstartDate = quarterCal.getTime();
                }
            }//end og msg
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPeriodSettingsController.saveFullYearSetupforTaxPeriod", ex);
        } finally {     
            jobj.put("periodname", periodname);
            jobj.put("periodid", periodid);
            jobj.put("isSuccess", isSuccess);
            jobj.put("msg", msg);
        }
        return jobj;
    }
    
    /*
     * Check Existing dates are present in db or not
     */
    public ModelAndView checkExistingDatesforTaxPeriod(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean isSuccess = true;
        Map<String, Object> requestParams = new HashMap();
        KwlReturnObject result = null;
        List subPeriodList = null;
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            requestParams.put("company", sessionHandlerImpl.getCompanyid(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter("periodtype"))) {
                int periodType = Integer.parseInt(request.getParameter("periodtype"));
                requestParams.put("periodtype", periodType);
            }
            if (request.getParameter("subperiodOf") != null) {
                String subperiodOf = !StringUtil.isNullOrEmpty(request.getParameter("subperiodOf")) ? request.getParameter("subperiodOf") : null;
                requestParams.put("subperiodOf", subperiodOf);
            }

            if (request.getParameter("startdate") != null) {
                Date enteredstartdate = df.parse(request.getParameter("startdate"));
                requestParams.put("startdate", enteredstartdate);
            }

            if (request.getParameter("endDate") != null) {
                Date enteredenddate = df.parse(request.getParameter("endDate"));
                requestParams.put("endDate", enteredenddate);
            }
            result = accPeriodSettingsDao.checkExistingDatesforTaxPeriod(requestParams);//Tax Period
            subPeriodList = result.getEntityList();
            if (subPeriodList != null && !subPeriodList.isEmpty()) {
                for (int i = 0; i < subPeriodList.size(); i++) {
                    TaxPeriod tperiod = (TaxPeriod) subPeriodList.get(i);
                    msg = messageSource.getMessage("acc.accountingperiodtab.between", null, RequestContextUtils.getLocale(request)) + tperiod.getPeriodName()+".";
                    isSuccess = false;
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jobj.put("success", isSuccess);
            jobj.put("msg", msg);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
  
    
    /*
     * getting Accounting SUB Periods
     */
    public ModelAndView getParentTaxPeriods(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray jArr = new JSONArray();
            Map<String, Object> requestParams = new HashMap();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            int periodType = !StringUtil.isNullOrEmpty(request.getParameter("periodtype")) ? Integer.parseInt(request.getParameter("periodtype")) : -1;

            if (periodType != -1) {
                requestParams.put("periodtype", periodType);
                requestParams.put("company", companyid);
                KwlReturnObject result = accPeriodSettingsDao.getChildTaxPeriods(requestParams);
                List subPeriodList = result.getEntityList();
                
                if (subPeriodList != null && !subPeriodList.isEmpty()) {
                    for (int i = 0; i < subPeriodList.size(); i++) {
                        TaxPeriod accTaxPeriod = (TaxPeriod) subPeriodList.get(i);
                        JSONObject tempObj = new JSONObject();
                        tempObj.put("id", accTaxPeriod.getId());
                        tempObj.put("name", accTaxPeriod.getPeriodName());
                        tempObj.put("entrydate", accTaxPeriod.getStartDate());
                        tempObj.put("enddate", accTaxPeriod.getEndDate());
                        jArr.put(tempObj);
                    }
                }
                jobj.put("data", jArr);
                jobj.put("success", true);
            }

        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /*To show data in Grid and to get Child Elements of Financial Year of GST Report and Tax Report*/
    public ModelAndView getTaxPeriods(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray jArr = new JSONArray();

            Map<String, Object> requestParams = new HashMap();
            requestParams.put("company", sessionHandlerImpl.getCompanyid(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter("periodtype"))) {
                int periodType = Integer.parseInt(request.getParameter("periodtype"));
                requestParams.put("periodtype", periodType);
            }
            
            if (request.getParameter("subperiodOf") != null) {
                String subperiodOf = !StringUtil.isNullOrEmpty(request.getParameter("subperiodOf")) ? request.getParameter("subperiodOf") : null;
                requestParams.put("subperiodOf",  subperiodOf);
            }
            getTaxPeriodsJson(requestParams, jArr);
            jobj.put("data", jArr);
            jobj.put("success", true);

        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /*Returning Json of Tax Periods*/
    public void getTaxPeriodsJson(Map<String, Object> requestParams, JSONArray jArr) throws ServiceException {
        try {
            KwlReturnObject result = accPeriodSettingsDao.getChildTaxPeriods(requestParams);
            List subPeriodList = result.getEntityList();
                
            if (subPeriodList != null && !subPeriodList.isEmpty()) {
                for (int i = 0; i < subPeriodList.size(); i++) {
                    TaxPeriod accTaxPeriod = (TaxPeriod) subPeriodList.get(i);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("id", accTaxPeriod.getId());
                    tempObj.put("periodname", accTaxPeriod.getPeriodName());
                    tempObj.put("subperiodof", !StringUtil.isNullObject(accTaxPeriod.getSubPeriodOf())?accTaxPeriod.getSubPeriodOf().getId():null);
                    tempObj.put("startdate", accTaxPeriod.getStartDate());
                    tempObj.put("enddate", accTaxPeriod.getEndDate());
                    tempObj.put("level", accTaxPeriod.getPeriodType() - 1);//accPeriod.getPeriodType() -1 = just trick because at js side level start from 0 and I have saved period type from 1
                    tempObj.put("leaf", (accTaxPeriod.getPeriodType())== TaxPeriod.PERIODTYPE_MONTHLY ? true : false );
                    tempObj.put("periodtype", accTaxPeriod.getPeriodType() );//accPeriod.getPeriodType() -1 = just trick because at js side level start from 0 and I have saved period type from 1
                    
                    jArr.put(tempObj);
                    requestParams.put("periodtype", accTaxPeriod.getPeriodType() + 1);
                    requestParams.put("subperiodOf", accTaxPeriod.getId());
                    getTaxPeriodsJson(requestParams, jArr);//Recursive call 
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPeriodSettingsController.getTaxPeriodsJson", ex);
        }
    }

    /*
     * Deleting Tax Periods
     */
    public ModelAndView deleteSelectedTaxPeriod(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String auditID = "";
        try {
            auditID = AuditAction.DELETE_TAXPERIOD; 
            int rows = deleteTaxPeriod(request);
            if (rows != 0) {
                jobj.put("success", true);
                jobj.put("msg",messageSource.getMessage("acc.accountingperiodtab.taxperioddelete", null, RequestContextUtils.getLocale(request)));
                auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Tax Period "+request.getParameter("periodname"), request,request.getParameter("transactionid"));
            } else {
                jobj.put("success", false);
                jobj.put("msg", messageSource.getMessage("acc.accountingperiodtab.taxperiodnotdelete", null, RequestContextUtils.getLocale(request)));
            }
        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public int deleteTaxPeriod(HttpServletRequest request) {
        int rows = 0;
        String transactionid = "";
        Map<String, Object> requestParams = new HashMap();
        try {
            requestParams.put("company", sessionHandlerImpl.getCompanyid(request));
            if (request.getParameter("transactionid") != null) {
                transactionid = !StringUtil.isNullOrEmpty(request.getParameter("transactionid")) ? request.getParameter("transactionid") : "";
                requestParams.put("subperiodOf", transactionid);
            }
            KwlReturnObject result = accPeriodSettingsDao.getChildTaxPeriods(requestParams);
            List subPeriodList = result.getEntityList();

            if (subPeriodList != null && !subPeriodList.isEmpty()) {
                for (int i = 0; i < subPeriodList.size(); i++) {
                    TaxPeriod accPeriod = (TaxPeriod) subPeriodList.get(i);
                    String id = accPeriod.getId();
                    requestParams.put("transactionid", id);
                    rows = accPeriodSettingsDao.deleteTaxPeriods(requestParams);
                }
            }
            //At last deleting the last selected element
            requestParams.put("transactionid", transactionid);
            rows = accPeriodSettingsDao.deleteTaxPeriods(requestParams);

        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rows;
    }
    
  /*---------------------------------------Accounting Period Function Section-------------------------------*/  
    public ModelAndView saveAccountingPeriod(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        KwlReturnObject result = null;
        JSONObject jobjvalidate = new JSONObject();
        String msg = "",auditID="";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BRecnl_Tx");
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            auditID = AuditAction.SAVE_ACCOUNTINGPERIOD; 
            int periodType = !StringUtil.isNullOrEmpty(request.getParameter("periodtype")) ? Integer.parseInt(request.getParameter("periodtype")) : null;
            if (periodType ==AccountingPeriod.AccountingPeriod_FULL_YEAR_SETUP) {
                jobjvalidate = saveFullYearSetup(request);
            } else {
                jobjvalidate =saveIndividualAccountingPeriod(request, periodType);
            }

            if (jobjvalidate.has("isSuccess") && jobjvalidate.getBoolean("isSuccess") == false) {
                txnManager.rollback(status);
                isSuccess = false;
                msg = jobjvalidate.optString("msg", "");
            } else {
                isSuccess = true;
                msg = messageSource.getMessage("acc.accountingperiodtab.accountingperiodsave", null, RequestContextUtils.getLocale(request));
                txnManager.commit(status);
                status=null;
                auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has created Accounting Period "+jobjvalidate.optString("periodname", ""), request,jobjvalidate.optString("periodid", ""));
            }

        } catch (Exception ex) {
            txnManager.rollback(status);
            jobj.put("msg", ex.getMessage());
            Logger.getLogger(accPeriodSettingsController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveAccountingorTaxPeriod : " + ex.getMessage(), ex);
        } finally {
            jobj.put("success", isSuccess);
            jobj.put("msg", msg);

        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }    
    
    public JSONObject saveIndividualAccountingPeriod(HttpServletRequest request, int periodType) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        KwlReturnObject result = null;
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);

            String id = !StringUtil.isNullOrEmpty(request.getParameter("id")) ? request.getParameter("id") : null;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userid = sessionHandlerImpl.getUserid(request);
            String subPeriodOf = request.getParameter("subperiodof");

            HashMap<String, Object> dataMap = new HashMap();
            dataMap.put("id", id);
            dataMap.put("company", companyid);
            dataMap.put("periodtype", periodType);
            dataMap.put("startdate", df.parse(request.getParameter("startdate")));
            dataMap.put("enddate", df.parse(request.getParameter("enddate")));
            dataMap.put("periodname", request.getParameter("periodname"));
            dataMap.put("userid", userid);
            if (!StringUtil.isNullOrEmpty(subPeriodOf)) {
                dataMap.put("subperiodof", subPeriodOf);
            }
            if (periodType == AccountingPeriod.AccountingPeriod_YEAR) {//for year only validate the year
                jobj = validateFinancialYear(request, dataMap, true);
                msg = jobj.has("msg") ? jobj.optString("msg", "") : "";
            }
            if (StringUtil.isNullOrEmpty(msg)) {//if no exception occured
                isSuccess = true;
                result=accPeriodSettingsDao.saveAccountingPeriodSettings(dataMap);
                AccountingPeriod accperiod = (AccountingPeriod) result.getEntityList().get(0);
                jobj.put("periodname", accperiod.getPeriodName());
                jobj.put("periodid", accperiod.getId());
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPeriodSettingsController.saveIndividualAccountingPeriod", ex);
        }finally {
            jobj.put("isSuccess", isSuccess);
            jobj.put("msg", msg);
        }
        return jobj;
    }
        
    /*
     * Showing in combo Field of New MOnth,New Quarter
     */
    public ModelAndView getParentAccountingPeriods(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray jArr = new JSONArray();

            Map<String, Object> requestParams = new HashMap();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            int periodType = !StringUtil.isNullOrEmpty(request.getParameter("periodtype")) ? Integer.parseInt(request.getParameter("periodtype")) : -1;

            if (periodType != -1) {
                requestParams.put("periodtype", periodType);
                requestParams.put("company", companyid);
                KwlReturnObject result = accPeriodSettingsDao.getParentAccountingPeriods(requestParams);
                List subPeriodList = result.getEntityList();

                if (subPeriodList != null && !subPeriodList.isEmpty()) {
                    for (int i = 0; i < subPeriodList.size(); i++) {
                        AccountingPeriod accPeriod = (AccountingPeriod) subPeriodList.get(i);
                        JSONObject tempObj = new JSONObject();
                        tempObj.put("id", accPeriod.getId());
                        tempObj.put("name", accPeriod.getPeriodName());
                        tempObj.put("entrydate", accPeriod.getStartDate());
                        tempObj.put("enddate", accPeriod.getEndDate());
                        jArr.put(tempObj);
                    }
                }
                jobj.put("data", jArr);
            }
        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getAccountingPeriods(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray jArr = new JSONArray();
            DateFormat dFormat=authHandler.getUserDateFormatterWithoutTimeZone(request);
            Map<String, Object> requestParams = new HashMap();
            requestParams.put("company", sessionHandlerImpl.getCompanyid(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter("periodtype"))) {
                int periodType = Integer.parseInt(request.getParameter("periodtype"));
                requestParams.put("periodtype", periodType);
            }
            if (request.getParameter("subperiodOf") != null) {
                String subperiodOf = !StringUtil.isNullOrEmpty(request.getParameter("subperiodOf")) ? request.getParameter("subperiodOf") : null;
                requestParams.put("subperiodOf", subperiodOf);
            }

            getAccountingPeriodsJson(requestParams, jArr,dFormat);
            jobj.put("data", jArr);

        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void getAccountingPeriodsJson(Map<String, Object> requestParams, JSONArray jArr, DateFormat dFormat) throws ServiceException {
        try {
            
            KwlReturnObject result = accPeriodSettingsDao.getParentAccountingPeriods(requestParams);
            List subPeriodList = result.getEntityList();
            
            if (subPeriodList != null && !subPeriodList.isEmpty()) {
                for (int i = 0; i < subPeriodList.size(); i++) {
                    AccountingPeriod accPeriod = (AccountingPeriod) subPeriodList.get(i);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("id", accPeriod.getId());
                    tempObj.put("periodname", accPeriod.getPeriodName());
                    tempObj.put("periodclosed", accPeriod.isPeridClosed());
                    tempObj.put("aptransactions", accPeriod.isApTransactionClosed());
                    tempObj.put("artransactions", accPeriod.isArTransactionClosed());
                    tempObj.put("allgltransactions", accPeriod.isAllGLTransactionClosed());
                    tempObj.put("subperiodof", !StringUtil.isNullObject(accPeriod.getSubPeriodOf())?accPeriod.getSubPeriodOf().getId():null);
                    tempObj.put("startdate", accPeriod.getStartDate()!=null ? dFormat.format(accPeriod.getStartDate()) : "");
                    tempObj.put("enddate", accPeriod.getEndDate()!=null ? dFormat.format(accPeriod.getEndDate()) : "");
                    tempObj.put("level", accPeriod.getPeriodType() - 1);
                    tempObj.put("leaf", (accPeriod.getPeriodType())== 3 ? true : false );
                    tempObj.put("periodtype", accPeriod.getPeriodType() );//accPeriod.getPeriodType() -1 = just trick because at js side level start from 0 and I have saved period type from 1

                    jArr.put(tempObj);
                    requestParams.put("periodtype", accPeriod.getPeriodType() + 1);
                    requestParams.put("subperiodOf", accPeriod.getId());
                    getAccountingPeriodsJson(requestParams, jArr,dFormat);//Recursive call 
                }
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPeriodSettingsController.getAccountingPeriodsJson", ex);
        }
    }
    
    /*
     * Checking Start Date, End Date exist or not for particluar subperiod
     */
    public ModelAndView checkExistingDatesforAccounting(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean isSuccess = true;
        Map<String, Object> requestParams = new HashMap();
        KwlReturnObject result = null;
        List subPeriodList = null;
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            requestParams.put("company", sessionHandlerImpl.getCompanyid(request));

            if (!StringUtil.isNullOrEmpty(request.getParameter("periodtype"))) {
                int periodType = Integer.parseInt(request.getParameter("periodtype"));
                requestParams.put("periodtype", periodType);
            }
            if (request.getParameter("subperiodOf") != null) {
                String subperiodOf = !StringUtil.isNullOrEmpty(request.getParameter("subperiodOf")) ? request.getParameter("subperiodOf") : null;
                requestParams.put("subperiodOf", subperiodOf);
            }

            if (request.getParameter("startdate") != null) {
                Date enteredstartdate = df.parse(request.getParameter("startdate"));
                requestParams.put("startdate", enteredstartdate);
            }

            if (request.getParameter("endDate") != null) {
                Date enteredenddate = df.parse(request.getParameter("endDate"));
                requestParams.put("endDate", enteredenddate);
            }
            result = accPeriodSettingsDao.checkExistingDatesforAccounting(requestParams);
            subPeriodList = result.getEntityList();
            if (subPeriodList != null && !subPeriodList.isEmpty()) {//Accounting Period
                for (int i = 0; i < subPeriodList.size(); i++) {
                    AccountingPeriod accPeriod = (AccountingPeriod) subPeriodList.get(i);
                    msg = messageSource.getMessage("acc.accountingperiodtab.between", null, RequestContextUtils.getLocale(request)) + accPeriod.getPeriodName()+".";
                    isSuccess = false;
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jobj.put("success", isSuccess);
            jobj.put("msg", msg);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
  public JSONObject saveFullYearSetup(HttpServletRequest request) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        JSONObject jobjvalidate = new JSONObject();
        String msg = "",periodname="",periodid="";
        boolean isSuccess = false;
        try {

            DateFormat df = authHandler.getDateOnlyFormat(request);
            int periodformat = !StringUtil.isNullOrEmpty(request.getParameter("periodformat")) ? Integer.parseInt(request.getParameter("periodformat")) : null;
            int yearinperiodname = !StringUtil.isNullOrEmpty(request.getParameter("yearinperiodname")) ? Integer.parseInt(request.getParameter("yearinperiodname")) : null;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userid=sessionHandlerImpl.getUserid(request);
            
            int startmonth =Integer.parseInt(request.getParameter("startmonth")) ;
            int endyear =Integer.parseInt( request.getParameter("endyear"));
//           
            Calendar startdatecalcal = Calendar.getInstance();
            startdatecalcal.set(Calendar.YEAR, startmonth==0 ? endyear : endyear-1);
            startdatecalcal.set(Calendar.MONTH, startmonth);
            startdatecalcal.set(Calendar.DAY_OF_MONTH, 1);
             Date  startDate = startdatecalcal.getTime();
          
            Calendar endDatecal = Calendar.getInstance();
            endDatecal.setTime(startDate);
            endDatecal.add(Calendar.MONTH,  12);
            endDatecal.add(Calendar.DAY_OF_MONTH, -1);
            Date endDate = endDatecal.getTime();
            
            
            HashMap<String, Object> dataMap = new HashMap();
            dataMap.put("company", companyid);
            dataMap.put("periodtype", AccountingPeriod.AccountingPeriod_YEAR);
            dataMap.put("startdate", startDate);
            dataMap.put("enddate", endDate);
            dataMap.put("periodformat", periodformat);
            dataMap.put("yearinperiodname", yearinperiodname);
            dataMap.put("userid", userid);
            dataMap.put("periodname", "FY " + endDatecal.get(Calendar.YEAR));

            jobjvalidate = validateFinancialYear(request, dataMap, true);
            msg = jobjvalidate.has("msg") ? jobjvalidate.optString("msg", "") : "";

            if (StringUtil.isNullOrEmpty(msg)) {
            isSuccess = true;
            KwlReturnObject kwl = accPeriodSettingsDao.saveAccountingPeriodSettings(dataMap);
            AccountingPeriod accperiod = (AccountingPeriod) kwl.getEntityList().get(0);
            periodname = accperiod.getPeriodName();
            periodid = accperiod.getId();
            
            Date quarterstartDate = startDate;
            Date quarterendDate = null;
            for (int quarter = 1; quarter <= 4; quarter++) {

                Calendar quarterCal = Calendar.getInstance();
                quarterCal.setTime(quarterstartDate);
                quarterCal.add(Calendar.MONTH, 3);
                quarterCal.add(Calendar.DATE, -1);
                quarterendDate = quarterCal.getTime();

                dataMap = new HashMap();
                dataMap.put("company", companyid);
                dataMap.put("periodtype", AccountingPeriod.AccountingPeriod_QUARTER);
                dataMap.put("startdate", quarterstartDate);
                dataMap.put("enddate", quarterendDate);
                 dataMap.put("userid", userid);
                dataMap.put("periodname", "Q" + quarter + " " + quarterCal.get(Calendar.YEAR));
                dataMap.put("subperiodof", accperiod.getId());

                KwlReturnObject kwlresult = accPeriodSettingsDao.saveAccountingPeriodSettings(dataMap);
                AccountingPeriod accperiodFormonth = (AccountingPeriod) kwlresult.getEntityList().get(0);

                Date monthstartDate = quarterstartDate;
                Date monthendDate = null;
                for (int month = 1; month <= 3; month++) {

                    Calendar monthCal = Calendar.getInstance();
                    monthCal.setTime(monthstartDate);
                    monthCal.set(Calendar.DAY_OF_MONTH, monthCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                    monthendDate = monthCal.getTime();

                    dataMap = new HashMap();
                    dataMap.put("company", companyid);
                    dataMap.put("periodtype", AccountingPeriod.AccountingPeriod_MONTH);
                    dataMap.put("startdate", monthstartDate);
                    dataMap.put("enddate", monthendDate);
                    dataMap.put("userid", userid);
                    dataMap.put("periodname", monthCal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + monthCal.get(Calendar.YEAR));
                    dataMap.put("subperiodof", accperiodFormonth.getId());

                     accPeriodSettingsDao.saveAccountingPeriodSettings(dataMap);

                    monthCal.setTime(monthendDate);
                    monthCal.add(Calendar.DATE, 1);
                    monthstartDate = monthCal.getTime();
                }

                quarterCal.setTime(quarterendDate);
                quarterCal.add(Calendar.DATE, 1);
                quarterstartDate = quarterCal.getTime();
                }
            }//END OF MSG

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPeriodSettingsController.saveFullYearSetup", ex);
        }finally {
            jobj.put("periodname", periodname);
            jobj.put("periodid", periodid);
            jobj.put("isSuccess", isSuccess);
            jobj.put("msg", msg);
        }
        return jobj;
    }
  
   public ModelAndView saveLockUnlockInformationofAccountingPeriod(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject tempObj=null;
         KwlReturnObject result = null;
         Map<String, Object> dataMap =null;
         String auditID="";
        try {
            auditID = AuditAction.SAVE_ACCOUNTINGPERIOD; 
            JSONArray lockinfoJarr = new JSONObject(request.getParameter("gridDataJson")).getJSONArray(Constants.root);
            for (int i = 0; i < lockinfoJarr.length(); i++) {
                 tempObj = lockinfoJarr.getJSONObject(i);

                dataMap = new HashMap();
                dataMap.put("id", !StringUtil.isNullOrEmpty(tempObj.getString("id")) ? tempObj.getString("id") : null);
                dataMap.put("periodclosed", !StringUtil.isNullOrEmpty(tempObj.getString("periodclosed")) ? Boolean.parseBoolean(tempObj.getString("periodclosed")) : null);
                dataMap.put("aptransactions", !StringUtil.isNullOrEmpty(tempObj.getString("aptransactions")) ? Boolean.parseBoolean(tempObj.getString("aptransactions")) : null);
                dataMap.put("artransactions", !StringUtil.isNullOrEmpty(tempObj.getString("artransactions")) ? Boolean.parseBoolean(tempObj.getString("artransactions")) : null);
                dataMap.put("allgltransactions", !StringUtil.isNullOrEmpty(tempObj.getString("allgltransactions")) ? Boolean.parseBoolean(tempObj.getString("allgltransactions")) : null);
                
                accPeriodSettingsDao.saveAccountingPeriodSettings(dataMap);
            }
            jobj.put("success", true);
            jobj.put("msg", messageSource.getMessage("acc.accountingperiodtab.save", null, RequestContextUtils.getLocale(request)));
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated Settings for Accounting Period ", request,"");
        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                jobj.put("success", false);
                jobj.put("msg", messageSource.getMessage("acc.accountingperiodtab.notsaved", null, RequestContextUtils.getLocale(request)));
            } catch (JSONException ex1) {
                Logger.getLogger(accPeriodSettingsController.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
   
   /*Deleting Accounting Periods*/
    public ModelAndView deleteSelectedAccountingPeriod(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String auditID="";
        try {
            auditID = AuditAction.DELETE_ACCOUNTINGPERIOD; 
            int rows = deleteAccountingPeriod(request);
            if (rows != 0) {
                jobj.put("success", true);
                jobj.put("msg", messageSource.getMessage("acc.accountingperiodtab.delete", null, RequestContextUtils.getLocale(request)));
                auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Accounting Period "+request.getParameter("periodname"), request,request.getParameter("transactionid"));
            } else {
                jobj.put("success", false);
                jobj.put("msg", messageSource.getMessage("acc.accountingperiodtab.notdelete", null, RequestContextUtils.getLocale(request)));
            }
        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public int deleteAccountingPeriod(HttpServletRequest request) {
        int rows = 0;
        String transactionid = "";
        Map<String, Object> requestParams = new HashMap();
        try {
            requestParams.put("company", sessionHandlerImpl.getCompanyid(request));
            if (request.getParameter("transactionid") != null) {
                transactionid = !StringUtil.isNullOrEmpty(request.getParameter("transactionid")) ? request.getParameter("transactionid") : "";
                requestParams.put("subperiodOf", transactionid);
            }
            KwlReturnObject result = accPeriodSettingsDao.getParentAccountingPeriods(requestParams);
            List subPeriodList = result.getEntityList();

            if (subPeriodList != null && !subPeriodList.isEmpty()) {
                for (int i = 0; i < subPeriodList.size(); i++) {
                    AccountingPeriod accPeriod = (AccountingPeriod) subPeriodList.get(i);
                    String id = accPeriod.getId();
                    requestParams.put("transactionid", id);
                    rows = accPeriodSettingsDao.deleteAccountingPeriods(requestParams);
                }
            }
            //At last deleting the last element
            requestParams.put("transactionid", transactionid);
            rows = accPeriodSettingsDao.deleteAccountingPeriods(requestParams);

        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rows;
    }

 }
