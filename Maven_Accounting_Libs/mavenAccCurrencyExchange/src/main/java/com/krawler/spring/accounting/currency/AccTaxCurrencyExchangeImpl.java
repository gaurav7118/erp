/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.currency;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.TaxExchangeRate;
import com.krawler.hql.accounting.TaxExchangeRateDetails;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author krawler
 */
public class AccTaxCurrencyExchangeImpl extends BaseDAO implements AccTaxCurrencyExchangeDAO {

    private accCurrencyDAO accCurrencyDAOObj;
    
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOObj) {
        this.accCurrencyDAOObj = accCurrencyDAOObj;
    }
    @Override
    public KwlReturnObject getTaxExchangeRateDetails(Map<String, Object> filterParams, boolean doSort) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from TaxExchangeRateDetails ";

        String companyid = filterParams.get("companyid") != null ? filterParams.get("companyid").toString() : "";
        ExtraCompanyPreferences extraCompanyPreferencesObj = null;
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("id", companyid);
        KwlReturnObject resultExtra = accCurrencyDAOObj.getExtraCompanyPreferencestoCheckBaseCurrency(requestParams);
        if (!resultExtra.getEntityList().isEmpty()) {
            extraCompanyPreferencesObj = (ExtraCompanyPreferences) resultExtra.getEntityList().get(0);
        }
        boolean isActivateToDateforExchangeRates = extraCompanyPreferencesObj != null ? extraCompanyPreferencesObj.isActivateToDateforExchangeRates() : false;

        if (isActivateToDateforExchangeRates) {
            // Checking all ovelapping conditions for From Date and To Date
            if (filterParams.containsKey("applydate") && filterParams.get("applydate") != null && filterParams.containsKey("todate") && filterParams.get("todate") != null) {
                condition += condition.length() == 0 ? " where " : " and ";
                condition += " ((DATE(applyDate)>=? and DATE(applyDate)<=?) or ((DATE(applyDate)>=? or DATE(toDate)>=?) and DATE(toDate)<=?) or (DATE(applyDate)<=? and DATE(toDate)>=?)) ";    //to compare with date part only - refer ticket ERP-15008
                params.add(filterParams.get("applydate"));
                params.add(filterParams.get("todate"));
                params.add(filterParams.get("applydate"));
                params.add(filterParams.get("applydate"));
                params.add(filterParams.get("todate"));
                params.add(filterParams.get("applydate"));
                params.add(filterParams.get("todate"));
            }
        } else {
            if (filterParams.containsKey("applydate") && filterParams.get("applydate") != null) {
                condition += (condition.length() == 0 ? " where " : " and ") + "applyDate=?";
                params.add(filterParams.get("applydate"));
            }
        }
        if (filterParams.containsKey("erid") && filterParams.get("erid") != null) {
            condition += (condition.length() == 0 ? " where " : " and ") + "exchangeratelink.ID=?";
            params.add(filterParams.get("erid"));
        }
        if (filterParams.containsKey("companyid") && filterParams.get("companyid") != null) {
            condition += (condition.length() == 0 ? " where " : " and ") + "company.companyID=?";
            params.add(filterParams.get("companyid"));
        }
        query += condition;
        if (doSort) {
            query += " order by exchangeorder asc"; //We have removed time part from the date. So for the same date ordering will be based on their exchange order no.
        }
//        query="from ExchangeRateDetails where applyDate=? and exchangeratelink.ID=? and company.companyID=?";
//        query="from ExchangeRateDetails where exchangeratelink.ID=? and company.companyID=? order by applyDate asc";
        returnList = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject addTaxExchangeRateDetails(Map<String, Object> erdMap) throws ServiceException {
        List list = new ArrayList();
        try {
            TaxExchangeRateDetails erd = new TaxExchangeRateDetails();
            erd = buildTaxExchangeRateDetails(erd, erdMap);
            save(erd);
            list.add(erd);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addTaxExchangeRateDetails : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Tax Exchange Rate Details has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updateTaxExchangeRateDetails(Map<String, Object> erdMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String erdid = (String) erdMap.get("erdid");
            TaxExchangeRateDetails erd = (TaxExchangeRateDetails) get(TaxExchangeRateDetails.class, erdid);
            if (erd != null) {
                erd = buildTaxExchangeRateDetails(erd, erdMap);
            }
            save(erd);
            list.add(erd);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateExchangeRateDetails : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Tax Exchange Rate Details has been updated successfully", null, list, list.size());
    }

    public TaxExchangeRateDetails buildTaxExchangeRateDetails(TaxExchangeRateDetails erd, Map<String, Object> erdMap) {
        if (erdMap.containsKey("exchangerate")) {
            erd.setExchangeRate((Double) erdMap.get("exchangerate"));
        }
        if (erdMap.containsKey("applydate")) {
            erd.setApplyDate((Date) erdMap.get("applydate"));
        }
        if (erdMap.containsKey("todate") && erdMap.get("todate") != null) {
            erd.setToDate((Date) erdMap.get("todate"));
        }
        if (erdMap.containsKey("erid")) {
            TaxExchangeRate er = erdMap.get("erid") == null ? null : (TaxExchangeRate) get(TaxExchangeRate.class, (String) erdMap.get("erid"));
            erd.setExchangeratelink(er);
        }
        if (erdMap.containsKey("companyid")) {
            Company company = erdMap.get("companyid") == null ? null : (Company) get(Company.class, (String) erdMap.get("companyid"));
            erd.setCompany(company);
        }
        if (erdMap.containsKey("foreigntobaseexchangerate") && erdMap.get("foreigntobaseexchangerate") != null) {
            erd.setForeignToBaseExchangeRate((Double) erdMap.get("foreigntobaseexchangerate"));
        }
        return erd;
    }

    @Override
    public KwlReturnObject getTaxCurrencyExchange(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from TaxExchangeRate";

        if (filterParams.containsKey("fromcurrencyid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "fromCurrency.currencyID=?";
            params.add(filterParams.get("fromcurrencyid"));
        }
        if (filterParams.containsKey("tocurrencyid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "toCurrency.currencyID=?";
            params.add(filterParams.get("tocurrencyid"));
        }
        query += condition;
//        query="select ID from ExchangeRate where fromCurrency.currencyID=?";
        returnList = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getTaxExcDetailID(Map request, String currencyid, Date transactiondate, String erid) throws ServiceException {
        List list = new ArrayList();
        TaxExchangeRateDetails erd = null;
        boolean isMaxNearestExchangeRate = false;
        try {
            String condition = "";
            String conditionForToDate = "";
            String appDate = "";
            ArrayList inparams = new ArrayList();
            ArrayList params = new ArrayList();
            String companyid = request.get("companyid") != null ? request.get("companyid").toString() : "";
            boolean isCurrencyExchangeWindow = request.get("isCurrencyExchangeWindow") == null ? false : (Boolean) request.get("isCurrencyExchangeWindow");
            ExtraCompanyPreferences extraCompanyPreferencesObj = null;
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", companyid);
            KwlReturnObject resultExtra = accCurrencyDAOObj.getExtraCompanyPreferencestoCheckBaseCurrency(requestParams);
            if (!resultExtra.getEntityList().isEmpty()) {
                extraCompanyPreferencesObj = (ExtraCompanyPreferences) resultExtra.getEntityList().get(0);
            }
            boolean isActivateToDateforExchangeRates = extraCompanyPreferencesObj != null ? extraCompanyPreferencesObj.isActivateToDateforExchangeRates() : false;
//            params.add(AuthHandler.getCurrencyID(request));
            params.add(request.get("gcurrencyid"));
            params.add(currencyid);
            if (erid == null) {
                String erIDQuery = "select ID from TaxExchangeRate where fromCurrency.currencyID=? and toCurrency.currencyID=? ";

                List erIDList = executeQuery(erIDQuery, params.toArray());
                if (erIDList.size() > 0) {
                    Iterator erIDitr = erIDList.iterator();
                    erid = (String) erIDitr.next();
                }
            }
            params = new ArrayList();
//            params.add(AuthHandler.getCompanyid(request));
            params.add(request.get("companyid"));
            params.add(erid);
            if (transactiondate != null && (isCurrencyExchangeWindow || !isActivateToDateforExchangeRates)) {
                params.add(transactiondate);
                if (request.containsKey("downloadexchangerateflag") && Boolean.parseBoolean(request.get("downloadexchangerateflag").toString())) {
                    condition += " and applyDate = ?  ";
                } else {
                    condition += " and applyDate <= ?  ";
                }
            }
            String applyDateQuery = "";
            List applyDateList = new ArrayList();
            if (isCurrencyExchangeWindow || !isActivateToDateforExchangeRates) {
                applyDateQuery = "select erd.applyDate from TaxExchangeRateDetails erd where erd.company.companyID=? and  erd.exchangeratelink.ID = ? " + condition + " ORDER BY erd.applyDate DESC, erd.exchangeorder DESC ";
                applyDateList = executeQuery(applyDateQuery, params.toArray());
                if (applyDateList != null && !applyDateList.isEmpty()) {
                    appDate = applyDateList.get(0).toString();
                }
            }
            /* System.out.println("getExcDetailID 2 - "+ applyDateQuery);
             System.out.println("getExcDetailID 2 - "+ params.toString());   */

            /*            Iterator itr = applyDateList.iterator();
             Date maxDate = (Date) itr.next();*/
            params = new ArrayList();
            params.add(request.get("companyid"));
            inparams.add(request.get("companyid"));
            inparams.add(erid);
            params.add(erid);
            if (transactiondate != null) {
                if (isActivateToDateforExchangeRates && !isCurrencyExchangeWindow) {
                    params.add(transactiondate);
                    inparams.add(transactiondate);
                    inparams.add(transactiondate);
                    params.add(transactiondate);
                    conditionForToDate = " and erd.toDate >= ? and erd.applyDate <= ? ";
                } else {
                    params.add(transactiondate);
//                condition += " and applyDate <= ?  ";
                }
            }
            if (isCurrencyExchangeWindow || !isActivateToDateforExchangeRates) {
//            params.add(maxDate);
                params.add(erid);
//            params.add(AuthHandler.getCompanyid(request));
                params.add(request.get("companyid"));
            }

            String erdIDQuery = "";
            if (isActivateToDateforExchangeRates && !isCurrencyExchangeWindow) {
                erdIDQuery = "from TaxExchangeRateDetails erd where erd.company.companyID=? and erd.exchangeratelink.ID=?" + conditionForToDate;
            } else {
                erdIDQuery = "from TaxExchangeRateDetails erd where erd.applyDate='" + appDate + "' and erd.company.companyID=? and erd.exchangeratelink.ID=? ORDER BY erd.exchangeorder DESC";
            }
            List erdIDList = executeQuery(erdIDQuery, inparams.toArray());
            if (erdIDList.size() > 0) {
                Iterator erdIDItr = erdIDList.iterator();
                if (erdIDItr.hasNext()) {
                    erd = (TaxExchangeRateDetails) erdIDItr.next();
                }
                /* If exchange rate is not available then take max nearest exchange rate. erd is null when no exchange rate is available.
                 * If isActivateToDateforExchangeRates option true and having following exchange rate for currency US Dollars.
                 * |Currency  | Exchange rate | From Date | To Date  |
                 * |US Dollars|1.5            |01-01-2016 |31-01-2016|
                 * |US Dollars|1.6            |15-02-2016 |29-02-2016|
                 * |US Dollars|1.7            |15-03-2016 |31-03-2016|
                 * If transaction date is 15-04-2016 then no exchange rate exist then it consider max nearest exchange rate which is From Date 15-03-2016 To Date 31-03-2016 - 1.7
                 * If transaction date is 12-03-2016 then max nearest exchange rate will be From Date 15-02-2016 To Date 29-02-2016 - 1.6
                 */
                if (erd == null && isActivateToDateforExchangeRates) {
                    isMaxNearestExchangeRate = true;
                    KwlReturnObject retObj = getMaxNearestTaxExchangeRate(request, currencyid, transactiondate, erid);
                    erd = (TaxExchangeRateDetails) retObj.getEntityList().get(0);
                }
            } else if (isActivateToDateforExchangeRates) {
                /* If exchange rate is not available then take max nearest exchange rate. erdIDList is of size zero when no exchange rate is available.
                 */
                isMaxNearestExchangeRate = true;
                KwlReturnObject retObj = getMaxNearestTaxExchangeRate(request, currencyid, transactiondate, erid);
                erd = (TaxExchangeRateDetails) retObj.getEntityList().get(0);
            }
        } catch (ServiceException ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("accCurrencyImpl.getExcDetailID : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("accCurrencyImpl.getExcDetailID : " + ex.getMessage(), ex);
        } finally {
            list.add(erd);
            list.add(isMaxNearestExchangeRate);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }

    // Get max nearest Weekly exchange rate for currency
    public KwlReturnObject getMaxNearestTaxExchangeRate(Map request, String currencyid, Date transactiondate, String erid) throws ServiceException {
        List list = new ArrayList();
        TaxExchangeRateDetails erd = null;
        try {
            if (transactiondate != null) {
                ArrayList params = new ArrayList();
                String companyid = request.get("companyid") != null ? request.get("companyid").toString() : "";
                Map<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("id", companyid);
                params.add(request.get("gcurrencyid"));
                params.add(currencyid);
                if (erid == null) {
                    String erIDQuery = "select ID from TaxExchangeRate where fromCurrency.currencyID=? and toCurrency.currencyID=? ";
                    List erIDList = executeQuery(erIDQuery, params.toArray());
                    if (erIDList.size() > 0) {
                        Iterator erIDitr = erIDList.iterator();
                        erid = (String) erIDitr.next();
                    }
                }
                String applyDateQuery = "select max(erd.applyDate) from TaxExchangeRateDetails erd where erd.company.companyID=? and  erd.exchangeratelink.ID = ? and applyDate < ? ";
                params = new ArrayList();
                params.add(request.get("companyid"));
                params.add(erid);
                params.add(transactiondate);
                params.add(erid);
                params.add(request.get("companyid"));
                String erdIDQuery = "from TaxExchangeRateDetails erd where erd.applyDate=(" + applyDateQuery + ") and erd.exchangeratelink.ID=? and erd.company.companyID=?";
                List erdIDList = executeQuery(erdIDQuery, params.toArray());
                if (erdIDList.size() > 0) {
                    Iterator erdIDItr = erdIDList.iterator();
                    if (erdIDItr.hasNext()) {
                        erd = (TaxExchangeRateDetails) erdIDItr.next();
                    }
                }
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getMaxNearestTaxExchangeRate : " + ex.getMessage(), ex);
        } finally {
            list.add(erd);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }
}
