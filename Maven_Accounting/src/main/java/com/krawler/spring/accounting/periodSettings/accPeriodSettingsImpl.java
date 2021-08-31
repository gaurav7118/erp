/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.periodSettings;

import com.krawler.common.admin.AccountingPeriod;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.TaxPeriod;
import com.krawler.common.admin.User;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;

/**
 *
 * @author krawler
 */
public class accPeriodSettingsImpl extends BaseDAO implements accPeriodSettingsDao,MessageSourceAware {

     private MessageSource messageSource;
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
     
    @Override
    //saving Tax Period
    public KwlReturnObject saveTaxPeriod(HashMap<String, Object> hm) {
        List list = new ArrayList();
        int periodtype = 0;
        try {
            TaxPeriod tp = new TaxPeriod();
            if (hm.containsKey("id")) {
                tp.setId((String) hm.get("id"));
            }
            if (hm.containsKey("companyid")) {
                Company company = hm.get("companyid") == null ? null : (Company) get(Company.class, (String) hm.get("companyid"));
                tp.setCompany(company);
            }

            if (hm.containsKey("periodname")) {
                tp.setPeriodName((String) hm.get("periodname"));
            }

            if (hm.containsKey("startdate")) {
                tp.setStartDate((Date) hm.get("startdate"));
            }

            if (hm.containsKey("enddate")) {
                tp.setEndDate((Date) hm.get("enddate"));
            }
            if (hm.containsKey("periodtype")) {
                periodtype = (Integer) hm.get("periodtype");
                tp.setPeriodType((Integer) hm.get("periodtype"));
            }

            if (hm.containsKey("subperiodof") && hm.get("subperiodof") != null) {
                tp.setSubPeriodOf((TaxPeriod) get(TaxPeriod.class, (String) hm.get("subperiodof")));
            }
            
            if (hm.containsKey("periodformat") && hm.get("periodformat") != null) {
                tp.setPeriodFormat((Integer) hm.get("periodformat"));
            }
            if (hm.containsKey("yearinperiodname") && hm.get("yearinperiodname") != null) {
                tp.setYearInPeriodName((Integer) hm.get("yearinperiodname"));
            }
            
            save(tp);
            list.add(tp);
        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "Tax Period Information has been saved successfully.", null, list, list.size());
    }
    
    @Override
    //getting Parent Accounting Periods
    public KwlReturnObject getChildTaxPeriods(Map<String, Object> requestParams)  {
        List resultList = Collections.emptyList();
        List params = new ArrayList();

        try {
            String conditionHql = "";
            if (requestParams.containsKey("company") && requestParams.get("company") != null) {
                conditionHql += " where acp.company.companyID= ? ";
                params.add((String) requestParams.get("company"));
            }
            if (requestParams.containsKey("periodtype") && requestParams.get("periodtype") != null) {
                conditionHql += "  and acp.periodType= ? ";
                params.add((Integer) requestParams.get("periodtype"));
            }
            if (requestParams.containsKey("subperiodOf") && requestParams.get("subperiodOf") != null) {
                conditionHql += "  and acp.subPeriodOf.id= ? ";
                params.add((String) requestParams.get("subperiodOf"));
            }

            String hql = "From TaxPeriod acp " + conditionHql;
            resultList = executeQuery(hql, params.toArray());
        } catch (Exception ex) {
             Logger.getLogger(accPeriodSettingsImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, resultList, resultList.size());
    }
    
    
    public KwlReturnObject checkExistingDatesforTaxPeriod(Map<String, Object> requestParams) throws ServiceException {
        List resultList = Collections.emptyList();
        List params = new ArrayList();
        Date enteredstartdate = null, enteredenddate = null;
        String conditionHql = "", betweenDatesconditionHql = "";
        try {
            if (requestParams.containsKey("periodtype") && requestParams.get("periodtype") != null) {
                betweenDatesconditionHql += "(tp.periodType= ? AND (";
                params.add((Integer) requestParams.get("periodtype"));
            }

            if (requestParams.containsKey("startdate") && requestParams.get("startdate") != null && requestParams.containsKey("endDate") && requestParams.get("endDate") != null) {
                enteredstartdate = (Date) requestParams.get("startdate");
                enteredenddate = (Date) requestParams.get("endDate");
                betweenDatesconditionHql += "((?) BETWEEN  tp.startDate AND tp.endDate)  OR ((?) BETWEEN  tp.startDate AND tp.endDate) OR ( (?) <  tp.startDate AND (?) > tp.endDate) ) )";
                params.add(enteredstartdate);
                params.add(enteredenddate);
                params.add(enteredstartdate);
                params.add(enteredenddate);
            }

            if (requestParams.containsKey("company") && requestParams.get("company") != null) {
                conditionHql += " AND  tp.company.companyID= ? ";
                params.add((String) requestParams.get("company"));
            }
            if (requestParams.containsKey("subperiodOf") && requestParams.get("subperiodOf") != null) {
                conditionHql += "AND tp.subPeriodOf.id= ? ";
                params.add((String) requestParams.get("subperiodOf"));
            }

            String hql = "From TaxPeriod tp where " + betweenDatesconditionHql + conditionHql;
            resultList = executeQuery(hql, params.toArray());

        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, resultList, resultList.size());
    }
    
    
  /*Check whether start date and end date of financial year is valid or not*/
    public String checkFinancialYearDatesforTaxPeriod(Map<String, Object> requestParams) throws ServiceException {
        List resultList = Collections.emptyList();
        List params = new ArrayList();
        Locale locale = null;
        if(requestParams.containsKey("locale")){
        locale = (Locale) requestParams.get("locale");
        }
        Date enteredstartdate = null, enteredendDate = null;
        String conditionHql = "", betweenDatesconditionHql = "", msg = "", hql = "";
        int periodtype = 0, size = 0;
        try {
            KwlReturnObject result = getChildTaxPeriods(requestParams);
            List subPeriodList = result.getEntityList();
            if (subPeriodList != null && !subPeriodList.isEmpty()) {//checking if any financial year is present or not
                if (requestParams.containsKey("periodtype") && requestParams.get("periodtype") != null) {
                    periodtype = (Integer) requestParams.get("periodtype");
                }

                result = checkExistingDatesforTaxPeriod(requestParams);//checking if the date is already is present in db with Financial year 2016
                size = result.getEntityList().size();
                if (size > 0) {
                    msg =messageSource.getMessage("acc.accountingperiodtab.finacialyearalreadypresent", null, locale);
                }

                //To check date and month
                if (StringUtil.isNullOrEmpty(msg) && requestParams.containsKey("startdate") && requestParams.get("startdate") != null && requestParams.containsKey("endDate") && requestParams.get("endDate") != null) {
                    List paramsList = new ArrayList();
                    if (requestParams.containsKey("periodtype") && requestParams.get("periodtype") != null) {
                        betweenDatesconditionHql += "tp.periodType= ? AND ";
                        paramsList.add((Integer) requestParams.get("periodtype"));
                    }
                    betweenDatesconditionHql += "(month(tp.startDate)=month(?) AND day(tp.startDate)=day(?)) ";
                    enteredstartdate = (Date) requestParams.get("startdate");
                    paramsList.add(enteredstartdate);
                    paramsList.add(enteredstartdate);

                    enteredendDate = (Date) requestParams.get("endDate");
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(enteredendDate);
                    cal.add(Calendar.DATE, 1);//incrementing date by one and it should be equal to day and month of Start Date.Checking whether end date is coming proper or not.
                    enteredendDate = cal.getTime();
                    betweenDatesconditionHql += "AND (month(tp.startDate)=month(?) AND day(tp.startDate)=day(?)) ";
                    paramsList.add(enteredendDate);
                    paramsList.add(enteredendDate);

                    if (requestParams.containsKey("company") && requestParams.get("company") != null) {
                        conditionHql = " AND  tp.company.companyID= ? ";
                        paramsList.add((String) requestParams.get("company"));
                    }

                    hql = "From TaxPeriod tp where " + betweenDatesconditionHql + conditionHql;
                    resultList = executeQuery(hql, paramsList.toArray());
                    size = resultList.size();
                    if (size == 0) {
                        if (periodtype == TaxPeriod.PERIODTYPE_FULLYEAR) {
                            msg = messageSource.getMessage("acc.accountingperiodtab.invalidfiscalyear", null, locale);
                        } else {
                            msg = messageSource.getMessage("acc.accountingperiodtab.invalisestartandenddate", null, locale);
                        }
                    }
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msg;
    }
   
    //Delete Tax Period
    public int deleteTaxPeriods(Map<String, Object> requestParams) throws ServiceException {
        String companyid = "", transactionid = "", delQuery = "";
        int numRows = 0;
        try {
            if (requestParams.containsKey("company") && requestParams.get("company") != null) {
                companyid = (String) requestParams.get("company");
            }

            if (requestParams.containsKey("transactionid") && requestParams.get("transactionid") != null) {
                transactionid = (String) requestParams.get("transactionid");
            }

            delQuery = "delete from TaxPeriod tp where tp.company.companyID= ? and tp.subPeriodOf.id= ?";
            numRows = executeUpdate(delQuery, new Object[]{companyid, transactionid});
            delQuery = "delete from TaxPeriod tp where tp.company.companyID= ? and tp.id= ?";
            numRows = executeUpdate(delQuery, new Object[]{companyid, transactionid});

        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return numRows;
    }
    
    /*-------------------------Accounting Section-----------------*/
    @Override
    public KwlReturnObject saveAccountingPeriodSettings(Map<String, Object> dataMap) throws ServiceException{
        List list =new ArrayList();
        AccountingPeriod accPeriod = new AccountingPeriod();
        try {
            if (dataMap.containsKey("id") && dataMap.get("id") != null) {
                accPeriod = (AccountingPeriod) get(AccountingPeriod.class, (String) dataMap.get("id"));
            }
            if (dataMap.containsKey("startdate") && dataMap.get("startdate") != null) {
                accPeriod.setStartDate((Date) dataMap.get("startdate"));
            }
            if (dataMap.containsKey("enddate") && dataMap.get("enddate") != null) {
                accPeriod.setEndDate((Date) dataMap.get("enddate"));
            }
            if (dataMap.containsKey("periodname") && dataMap.get("periodname") != null) {
                accPeriod.setPeriodName((String) dataMap.get("periodname"));
            }
            if (dataMap.containsKey("periodtype") && dataMap.get("periodtype") != null) {
                accPeriod.setPeriodType((Integer) dataMap.get("periodtype"));
            }
            if (dataMap.containsKey("subperiodof") && dataMap.get("subperiodof") != null) {
                accPeriod.setSubPeriodOf((AccountingPeriod)get(AccountingPeriod.class,(String)dataMap.get("subperiodof")));
            }
            if (dataMap.containsKey("company") && dataMap.get("company") != null) {
                accPeriod.setCompany((Company)get(Company.class,(String)dataMap.get("company")));
            }
            if (dataMap.containsKey("periodformat") && dataMap.get("periodformat") != null) {
                accPeriod.setPeriodFormat((Integer)dataMap.get("periodformat"));
            }
            if (dataMap.containsKey("yearinperiodname") && dataMap.get("yearinperiodname") != null) {
                accPeriod.setYearInPeriodName((Integer)dataMap.get("yearinperiodname"));
            }
            if (dataMap.containsKey("userid") && dataMap.get("userid") != null) {
                accPeriod.setUser((User)get(User.class,(String)dataMap.get("userid")));
            }
            if (dataMap.containsKey("periodclosed") && dataMap.get("periodclosed") != null) {
                accPeriod.setPeridClosed((Boolean)dataMap.get("periodclosed"));
            }
             if (dataMap.containsKey("artransactions") && dataMap.get("artransactions") != null) {
                accPeriod.setArTransactionClosed((Boolean)dataMap.get("artransactions"));
            }
              if (dataMap.containsKey("aptransactions") && dataMap.get("aptransactions") != null) {
                accPeriod.setApTransactionClosed((Boolean)dataMap.get("aptransactions"));
            }
              if (dataMap.containsKey("allgltransactions") && dataMap.get("allgltransactions") != null) {
                accPeriod.setAllGLTransactionClosed((Boolean)dataMap.get("allgltransactions"));
            }

            save(accPeriod);
            list.add(accPeriod);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPeriodSettingsImpl.saveAccountingPeriodSettings", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getParentAccountingPeriods(Map<String, Object> requestParams) throws ServiceException{
        List resultList = Collections.emptyList();
        List params = new ArrayList();

        try {
            String conditionHql = "";
            if (requestParams.containsKey("company") && requestParams.get("company") != null) {
                conditionHql += " where acp.company.companyID= ? ";
                params.add((String) requestParams.get("company"));
            }
            if (requestParams.containsKey("periodtype") && requestParams.get("periodtype") != null) {
                conditionHql += "  and acp.periodType= ? ";
                params.add((Integer) requestParams.get("periodtype"));
            }
            if (requestParams.containsKey("subperiodOf") && requestParams.get("subperiodOf") != null) {
                conditionHql += "  and acp.subPeriodOf.id= ? ";
                params.add((String) requestParams.get("subperiodOf"));
            }
             if (requestParams.containsKey(Constants.Checklocktransactiondate) && requestParams.get(Constants.Checklocktransactiondate) != null) {
                conditionHql += "  and (?) BETWEEN  acp.startDate AND acp.endDate ";
                params.add((Date) requestParams.get(Constants.Checklocktransactiondate));
            }
             
            if (requestParams.containsKey("transactionid") && requestParams.get("transactionid") != null) {
                conditionHql += "  and acp.id= ? ";
                params.add((String) requestParams.get("transactionid"));
            }

            String hql = "From AccountingPeriod acp " + conditionHql;
            resultList = executeQuery(hql, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPeriodSettingsImpl.getParentAccountingPeriods", ex);
        }
         return new KwlReturnObject(true, "", null, resultList, resultList.size());
    }
  
    @Override
    //Conditions to check whether Date lies in start date,end date lies in between subperiod start date and end date and the range should not overlap any other existing date range  
    public KwlReturnObject checkExistingDatesforAccounting(Map<String, Object> requestParams) throws ServiceException {
        List resultList = Collections.emptyList();
        List params = new ArrayList();
        Date enteredstartdate=null,enteredenddate=null;
        try {
            String conditionHql = "",betweenDatesconditionHql="";
    
            if (requestParams.containsKey("periodtype") && requestParams.get("periodtype") != null) {
                betweenDatesconditionHql += "(acp.periodType= ? AND (";
                params.add((Integer) requestParams.get("periodtype"));
            }
    
            if (requestParams.containsKey("startdate") && requestParams.get("startdate") != null && requestParams.containsKey("endDate") && requestParams.get("endDate") != null) {
                enteredstartdate = (Date) requestParams.get("startdate");
                enteredenddate = (Date) requestParams.get("endDate");
                betweenDatesconditionHql += "((?) BETWEEN  acp.startDate AND acp.endDate)  OR ((?) BETWEEN  acp.startDate AND acp.endDate) OR( (?) <  acp.startDate AND (?) > acp.endDate) ) )";
                params.add(enteredstartdate);
                params.add(enteredenddate);
                params.add(enteredstartdate);
                params.add(enteredenddate);
              }

            if (requestParams.containsKey("company") && requestParams.get("company") != null) {
                conditionHql += " AND  acp.company.companyID= ? ";
                params.add((String) requestParams.get("company"));
            }
            if (requestParams.containsKey("subperiodOf") && requestParams.get("subperiodOf") != null) {
                conditionHql += "AND acp.subPeriodOf.id= ? ";
                params.add((String) requestParams.get("subperiodOf"));
            }
            
            String hql = "From AccountingPeriod acp where "+betweenDatesconditionHql + conditionHql;
            resultList = executeQuery(hql, params.toArray());

        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, resultList, resultList.size());
    }

    /*Check Existing Financial Year Dates for Accounting*/
    public String checkFinancialYearDatesforAccounting(Map<String, Object> requestParams) throws ServiceException {
        List resultList = Collections.emptyList();
        Date enteredstartdate = null, enteredendDate = null;
        String conditionHql = "", betweenDatesconditionHql = "", msg = "", hql = "";
        int periodtype = 0, size = 0;
        Locale locale = null;
        if(requestParams.containsKey("locale")){
        locale = (Locale) requestParams.get("locale");
        }
        try {
            KwlReturnObject result = getParentAccountingPeriods(requestParams);
            List subPeriodList = result.getEntityList();
            if (subPeriodList != null && !subPeriodList.isEmpty()) {//checking if any financial year is present or not
                if (requestParams.containsKey("periodtype") && requestParams.get("periodtype") != null) {
                    periodtype = (Integer) requestParams.get("periodtype");
                }

                result = checkExistingDatesforAccounting(requestParams);//checking if the date is already is present in db with Financial year 2016
                size = result.getEntityList().size();
                if (size > 0) {
                    msg = messageSource.getMessage("acc.accountingperiodtab.finacialyearalreadypresent", null, locale);
                }

                //To check date and month
                if (StringUtil.isNullOrEmpty(msg) && requestParams.containsKey("startdate") && requestParams.get("startdate") != null && requestParams.containsKey("endDate") && requestParams.get("endDate") != null) {
                    List paramsList = new ArrayList();
                    if (requestParams.containsKey("periodtype") && requestParams.get("periodtype") != null) {
                        betweenDatesconditionHql += "acp.periodType= ? AND ";
                        paramsList.add((Integer) requestParams.get("periodtype"));
                    }
                    betweenDatesconditionHql += "(month(acp.startDate)=month(?) AND day(acp.startDate)=day(?)) ";
                    enteredstartdate = (Date) requestParams.get("startdate");
                    paramsList.add(enteredstartdate);
                    paramsList.add(enteredstartdate);

                    enteredendDate = (Date) requestParams.get("endDate");
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(enteredendDate);
                    cal.add(Calendar.DATE, 1);//incrementing date by one and it should be equal to day and month of Start Date.Checking whether end date is coming proper or not.
                    enteredendDate = cal.getTime();
                    betweenDatesconditionHql += "AND (month(acp.startDate)=month(?) AND day(acp.startDate)=day(?)) ";
                    paramsList.add(enteredendDate);
                    paramsList.add(enteredendDate);

                    if (requestParams.containsKey("company") && requestParams.get("company") != null) {
                        conditionHql = " AND  acp.company.companyID= ? ";
                        paramsList.add((String) requestParams.get("company"));
                    }

                    hql = "From AccountingPeriod acp where " + betweenDatesconditionHql + conditionHql;
                    resultList = executeQuery(hql, paramsList.toArray());
                    size = resultList.size();
                    if (size == 0) {
                        if (periodtype == AccountingPeriod.AccountingPeriod_FULL_YEAR_SETUP) {
                            msg = msg = messageSource.getMessage("acc.accountingperiodtab.invalidfiscalyear", null, locale);
                        } else {
                            msg = messageSource.getMessage("acc.accountingperiodtab.invalisestartandenddate", null, locale);
                        }
                    }
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msg;
    }

    //Deleting Accounting Periods
    public int deleteAccountingPeriods(Map<String, Object> requestParams) throws ServiceException {
        String companyid = "", transactionid = "", delQuery = "";
        int numRows = 0;
        try {
            if (requestParams.containsKey("company") && requestParams.get("company") != null) {
                companyid = (String) requestParams.get("company");
            }

            if (requestParams.containsKey("transactionid") && requestParams.get("transactionid") != null) {
                transactionid = (String) requestParams.get("transactionid");
            }

            delQuery = "delete from AccountingPeriod acp where acp.company.companyID= ? and acp.subPeriodOf.id= ?";
            numRows = executeUpdate(delQuery, new Object[]{companyid, transactionid});
            delQuery = "delete from AccountingPeriod acp where acp.company.companyID= ? and acp.id= ?";
            numRows = executeUpdate(delQuery, new Object[]{companyid, transactionid});

        } catch (Exception ex) {
            Logger.getLogger(accPeriodSettingsImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return numRows;
    }
    
}