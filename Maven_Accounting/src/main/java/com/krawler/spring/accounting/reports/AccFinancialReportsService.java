/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.LayoutGroup;
import com.krawler.hql.accounting.AccountBudget;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface AccFinancialReportsService {

    public JSONObject getMonthlyTradingProfitLossJasperExport(HttpServletRequest request, boolean monthYearFormat) throws ServiceException, SessionExpiredException;
    
    public JSONObject getYearlyTradingProfitLossJasperExport(HttpServletRequest request, boolean monthYearFormat) throws ServiceException, SessionExpiredException;

    public JSONObject getTradingAndProfitLoss(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException;
    
    public JSONObject getNewMonthlyMYOBtradingreport(JSONObject paramJobj, JSONObject tradingjobj, boolean isPrint) throws ServiceException, SessionExpiredException;
    
    public JSONObject getMonthlyYearlyTradingProfitAndLossChartJSON(JSONObject paramJobj, JSONObject jobj) throws ServiceException, SessionExpiredException;
    
    public JSONArray getConsolidationReport(JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    
    public JSONObject getConsolidationProfitAndLossReport(JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    
    public JSONObject getConsolidationBalanceSheetReport(HttpServletRequest request,JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    
    public JSONObject getCustomConsolidationBalanceSheetReport(JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    
    public JSONObject getCustomConsolidationPNLReport(JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    
    public JSONObject getExportBalanceSheetJSON(HttpServletRequest request, JSONObject jobj, int flag, int toggle, boolean periodView) throws ServiceException;
    
    public JSONObject getMonthlyBalanceSheetforExport(HttpServletRequest request, boolean monthYearDate) throws ServiceException, SessionExpiredException;
    
    public JSONObject getTradingAndProfitLossWithBudget(HttpServletRequest request) throws ServiceException, SessionExpiredException;
    
    public JSONObject getDimesionBasedProfitLoss(JSONObject paramJobj, boolean monthYearFormat) throws ServiceException, SessionExpiredException ;
    
    public JSONObject getBSorPL_CustomLayout(JSONObject paramJobj, ExtraCompanyPreferences extrapref, String companyid) throws ServiceException, SessionExpiredException,JSONException;
    
    public double[] getBSorPL_CustomLayout(JSONObject paramJobj, JSONArray jArr, String companyid, ExtraCompanyPreferences extrapref) throws ServiceException, SessionExpiredException,JSONException;
    
    public double[] formatLayoutGroupDetails(JSONObject paramJobj, String companyid, LayoutGroup group, Date startDate, Date endDate, int level, 
            boolean isBalanceSheet, JSONArray jArr,Date startPreDate,Date endPreDate, Map<String, double[]> groupTotalMap, Map<String, Object> advSearchAttributes, Map<String, Map> stockDateMap) throws ServiceException, SessionExpiredException, ParseException;
    
    public double[] getLayoutAccountBalance(JSONObject paramJobj, HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate,String companyid, Map<String, Object> advSearchAttributes) throws ServiceException,JSONException ;
    
    public double[] getLayoutAccountBalance(JSONObject paramJobj, String accountid, Date startDate, Date endDate, String companyid, Map<String, Object> advSearchAttributes) throws ServiceException, SessionExpiredException,JSONException;
    
    public double getLayoutAccountBalanceTrans(HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate, String companyid, Map<String, Object> advSearchAttributes) throws ServiceException ;
    
    public double[] formatLayoutAccountDetails(JSONObject paramJobj, LayoutGroup group, Account account, Date startDate, Date endDate, int level,
            boolean isDebit, boolean isBalanceSheet, JSONArray jArr, DateFormat sdf, Date startPreDate, Date endPreDate, 
            String companyid, Map<String, Object> advSearchAttributes) throws ServiceException, SessionExpiredException, ParseException, JSONException;

    public JSONObject getAccountJson(JSONObject paramJobj, List list, accCurrencyDAO accCurrencyDAOobj, Map<String, Object> paramMap) throws SessionExpiredException, ServiceException ;
     
    public double getAccountBalanceInOriginalCurrency(JSONObject paramJobj, String accountid, Date startDate, Date endDate) throws ServiceException, SessionExpiredException,JSONException;//    public double getTotalOpeningBalance(Account account, double totalOpeningBalance, String defaultCurrencyid, accCurrencyDAO accCurrencyDAOobj, HttpServletRequest request) throws ServiceException;
    
    public double getTotalOpeningBalance(Account account, double totalOpeningBalance, String defaultCurrencyid, accCurrencyDAO accCurrencyDAOobj, JSONObject paramJobj, String companyid) throws ServiceException ;

    public JSONObject getMonthwiseGeneralLedgerReport(JSONObject paramJobj, boolean isExport) throws ServiceException;
    
    public JSONObject getAmountsForCashFlowStatementAsPerCOA(HttpServletRequest request) throws ServiceException, SessionExpiredException;
    
    public double getTotalAccountBalanceInSelectedCurrency(Account account, double totalAccountBalance, JSONObject paramJobj ) throws ServiceException,JSONException,SessionExpiredException;
    
    public HashMap<String, Object> getAdvanceSearchModuleFieldParams(HashMap<String, Object> requestParams ) throws ServiceException, SessionExpiredException;
    
    public double getTotalPricipleAmountForEClaimJE(Map<String,Object> jeDetailsMap) throws ServiceException;
    
    public Map<String, String> getColumnHeaderAndTitlesFromMonthList(JSONArray monthArr,boolean isShowAccountCode) throws ServiceException, SessionExpiredException;
    
    public JSONObject getJournalEntryJsonForExportMerged(HashMap<String, Object> requestParams, List list, JSONArray jArr, int templateflag) throws ServiceException;
    
    public JSONObject getDimesionBasedProfitLossAllAccounts(JSONObject paramJobj, boolean monthYearFormat) throws ServiceException, SessionExpiredException;
    
    public JSONObject getDimensionBasedMonthlyPeriodAmount(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    
    public JSONObject getMonthlyCustomLayout (JSONObject paramJobj, ExtraCompanyPreferences extrapref, String companyid) throws ServiceException, SessionExpiredException,JSONException, ParseException;
    
    public double getBudgetAmountForMonthByMonthNumber (AccountBudget accountBudget, int monthNumber);
    
    public JSONArray compareAgedAndbalanceSheetReport (JSONObject jsonObject,HashMap<String, Object> requestParams)throws JSONException,ParseException,ServiceException,SessionExpiredException;
    
    public JSONObject getBudgetVsCostReport(JSONObject jsonObject)throws ServiceException;
    
    public JSONObject getActualVsBudgetReport(JSONObject jsonObject)throws ServiceException;
    
    public JSONObject getForecastingReport(JSONObject jsonObject)throws ServiceException;
    
    public JSONObject getForecastingReportExportObject(JSONObject jsonObject)throws ServiceException;
    
    public JSONObject getCommonParametersForCustomLayout(JSONObject paramJobj, HttpServletRequest request);
}