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

import com.krawler.common.admin.Company;
import com.krawler.common.admin.CustomCurrency;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.ConsolidationData;
import com.krawler.hql.accounting.ConsolidationExchangeRateDetails;
import com.krawler.hql.accounting.ExchangeRate;
import com.krawler.hql.accounting.ExchangeRateDetails;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import java.text.SimpleDateFormat;
import java.util.*;
import com.krawler.spring.common.kwlCommonTablesDAO;

/**
 *
 * @author krawler
 */
public class accCurrencyImpl extends BaseDAO implements accCurrencyDAO {
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

     public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }


    public KwlReturnObject getExcDetailID(Map request, String currencyid, Date transactiondate, String erid) throws ServiceException {
        List list = new ArrayList();
        ExchangeRateDetails erd = null;
        boolean isMaxNearestExchangeRate=false;
        try {
            String condition = "";
            String conditionForToDate = "";
            String appDate = "";
            ArrayList inparams = new ArrayList();
            ArrayList params = new ArrayList();
            String companyid = request.get("companyid") !=null ? request.get("companyid").toString() : "";
            boolean isCurrencyExchangeWindow = request.get("isCurrencyExchangeWindow")==null ? false : (Boolean)request.get("isCurrencyExchangeWindow");
            ExtraCompanyPreferences extraCompanyPreferencesObj = null;
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", companyid);
            KwlReturnObject resultExtra = getExtraCompanyPreferencestoCheckBaseCurrency(requestParams);
            if (!resultExtra.getEntityList().isEmpty()) {
                extraCompanyPreferencesObj = (ExtraCompanyPreferences) resultExtra.getEntityList().get(0);
            }
            boolean isActivateToDateforExchangeRates = extraCompanyPreferencesObj!=null ? extraCompanyPreferencesObj.isActivateToDateforExchangeRates() : false;
//            params.add(AuthHandler.getCurrencyID(request));
            params.add(request.get("gcurrencyid"));
            params.add(currencyid);
            if (erid == null) {
                String erIDQuery = "select ID from ExchangeRate where fromCurrency.currencyID=? and toCurrency.currencyID=? ";
                
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
                    if(request.containsKey("downloadexchangerateflag")&&Boolean.parseBoolean(request.get("downloadexchangerateflag").toString())){
                         condition += " and applyDate = ?  ";
                    }else{
                    condition += " and applyDate <= ?  ";
                    }
                }
            String applyDateQuery = "";
            List applyDateList = new ArrayList();
            if (isCurrencyExchangeWindow || !isActivateToDateforExchangeRates) {
                applyDateQuery = "select erd.applyDate from ExchangeRateDetails erd where erd.company.companyID=? and  erd.exchangeratelink.ID = ? " + condition + " ORDER BY erd.applyDate DESC, erd.exchangeorder DESC ";
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
                if(isActivateToDateforExchangeRates && !isCurrencyExchangeWindow){
                    params.add(transactiondate);
                    inparams.add(transactiondate);
                    inparams.add(transactiondate);
                    params.add(transactiondate);
                    conditionForToDate = " and erd.toDate >= ? and erd.applyDate <= ? ";
                }else{
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
            if(isActivateToDateforExchangeRates && !isCurrencyExchangeWindow){
                erdIDQuery = "from ExchangeRateDetails erd where erd.company.companyID=? and erd.exchangeratelink.ID=?"+ conditionForToDate;
            }else{
                erdIDQuery = "from ExchangeRateDetails erd where erd.applyDate='"+appDate+"' and erd.company.companyID=? and erd.exchangeratelink.ID=? ORDER BY erd.exchangeorder DESC";
                }
            List erdIDList = executeQuery(erdIDQuery, inparams.toArray());
            if (erdIDList.size() > 0) {
                Iterator erdIDItr = erdIDList.iterator();
                if (erdIDItr.hasNext()) {
                    erd = (ExchangeRateDetails) erdIDItr.next();
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
                if(erd==null && isActivateToDateforExchangeRates){
                    isMaxNearestExchangeRate=true;
                    KwlReturnObject retObj=getMaxNearestExchangeRate(request,currencyid,transactiondate,erid);
                    erd = (ExchangeRateDetails) retObj.getEntityList().get(0);
                }
            }else if(isActivateToDateforExchangeRates){
                /* If exchange rate is not available then take max nearest exchange rate. erdIDList is of size zero when no exchange rate is available.
                 */
                isMaxNearestExchangeRate = true;
                KwlReturnObject retObj = getMaxNearestExchangeRate(request, currencyid, transactiondate, erid);
                erd = (ExchangeRateDetails) retObj.getEntityList().get(0);
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
    // Get max nearest exchange rate for currency
    public KwlReturnObject getMaxNearestExchangeRate(Map request, String currencyid, Date transactiondate, String erid) throws ServiceException {
        List list = new ArrayList();
        ExchangeRateDetails erd = null;
        try {
            if (transactiondate != null) {
                ArrayList params = new ArrayList();
                String companyid = request.get("companyid") != null ? request.get("companyid").toString() : "";
                Map<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("id", companyid);
                params.add(request.get("gcurrencyid"));
                params.add(currencyid);
                if (erid == null) {
                    String erIDQuery = "select ID from ExchangeRate where fromCurrency.currencyID=? and toCurrency.currencyID=? ";
                    List erIDList = executeQuery(erIDQuery, params.toArray());
                    if (erIDList.size() > 0) {
                        Iterator erIDitr = erIDList.iterator();
                        erid = (String) erIDitr.next();
                    }
                }
                String applyDateQuery = "select max(erd.applyDate) from ExchangeRateDetails erd where erd.company.companyID=? and  erd.exchangeratelink.ID = ? and applyDate < ? " ;
                params = new ArrayList();
                params.add(request.get("companyid"));
                params.add(erid);
                params.add(transactiondate);
                params.add(erid);
                params.add(request.get("companyid"));
                String erdIDQuery = "from ExchangeRateDetails erd where erd.applyDate=(" + applyDateQuery + ") and erd.exchangeratelink.ID=? and erd.company.companyID=?";
                List erdIDList = executeQuery(erdIDQuery, params.toArray());
                if (erdIDList.size() > 0) {
                    Iterator erdIDItr = erdIDList.iterator();
                    if (erdIDItr.hasNext()) {
                        erd = (ExchangeRateDetails) erdIDItr.next();
                    }
                }
            }
        } catch (ServiceException ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("accCurrencyImpl.getMaxNearestExchangeRate : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("accCurrencyImpl.getMaxNearestExchangeRate : " + ex.getMessage(), ex);
        } finally {
            list.add(erd);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }
    
    public KwlReturnObject getCurrencyToBaseAmount(Map request, Double Amount, String currencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        try {
            if (Amount != 0) {
                if (rate == 0) {
                    KwlReturnObject result = getExcDetailID(request, currencyid, transactiondate, null);
                    List li = result.getEntityList();
                    if (!li.isEmpty()) {
                        Iterator itr = li.iterator();
                        ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                        rate = erd.getExchangeRate();
                    }
                }
                Amount = Amount / rate;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getCurrencyToBaseAmount : " + ex.getMessage(), ex);
        } finally {
            list.add(Amount);
            list.add(rate);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }

    public String getCurrencyFromPriceList(String productid, String companyid, String carryin,String uomid) throws ServiceException {
        String currency = "";
        try {
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            String tdate = sdf.format(new Date());
            String query = "SELECT t.currency FROM (SELECT currency,applydate FROM  pricelist WHERE applydate<='"+tdate+"' AND  product='"+productid+"' and company='"+companyid+"' and carryin='T' and uomid = '"+uomid+"' ORDER BY applydate DESC LIMIT 1) as t";
            List list = executeSQLQuery(query);
            if (list.isEmpty() == false) {
                currency = (String) list.get(0);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getCurrencyFromPriceList : " + ex.getMessage(), ex);
        }
        return currency;
    }

    @Override
    public Map<String, String> getCurrencyFromPriceList(String companyid, String carryin) throws ServiceException {
        Map<String, String> currencyFromPriceListMap = new HashMap<String, String>();
        String query = "SELECT product, currency, max(applydate) FROM pricelist WHERE company=? and carryin='T' group by product";
//        String query = "SELECT pl1.product.ID, pl1.currency.currencyID, max(pl1.applydate) FROM PriceList pl1 WHERE pl1.company.companyID='04575a0c-b33c-11e3-986d-001e670e1889' and pl1.carryin='T' group by pl1.product.ID";
        List returnList = executeSQLQuery(query, companyid);
        for(int i=0; i<returnList.size(); i++){
            Object[] obj = (Object[]) returnList.get(i);
            currencyFromPriceListMap.put((String) obj[0], (String) obj[1]);
        }
        return currencyFromPriceListMap;
    }

public KwlReturnObject getforeignToBaseAmountAndBaseToSGD(Map request, Double Amount, String currencyid, Date transactiondate, double rate,double gstCurrencyRate) throws ServiceException {
        List list = new ArrayList();
        try {
            if (Amount != 0) {
                if (rate == 0) {
                    KwlReturnObject result = getExcDetailID(request, currencyid, transactiondate, null);
                    List li = result.getEntityList();
                    if (!li.isEmpty()) {
                        Iterator itr = li.iterator();
                        ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                        rate = erd.getExchangeRate();
                    }
                }
                Amount = Amount / rate * gstCurrencyRate;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getCurrencyToBaseAmount : " + ex.getMessage(), ex);
        } finally {
            list.add(Amount);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }

@Override
    public KwlReturnObject getOneCurrencyToOtherModifiedGstCurrencyRate(Map request, Double Amount, String oldcurrencyid, String newcurrencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        Double currencyAmount = 0.0;
        try {
            if (Amount != 0) {
                KwlReturnObject bAmt = getCurrencyToBaseAmount(request, Amount, oldcurrencyid, transactiondate, rate);
                Double baseAmount = (Double) bAmt.getEntityList().get(0);
                bAmt = getBaseToCurrencyAmount(request, baseAmount, newcurrencyid, transactiondate, 0);
                currencyAmount = (Double) bAmt.getEntityList().get(0);
            }
            list.add(currencyAmount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getOneCurrencyToOther : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public double getCurrencyToBaseRate(Map request, String currencyid, Date transactiondate) throws ServiceException {
        double rate = 0;
        try{
            KwlReturnObject result = getExcDetailID(request, currencyid, transactiondate, null);
            List li = result.getEntityList();
            if (!li.isEmpty()) {
                Iterator itr = li.iterator();
                ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                rate = erd.getExchangeRate();
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getCurrencyToBaseRate : "+ex.getMessage(), ex);
        } finally {
            return rate;
        }
    }
    @Override
    public KwlReturnObject getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(Map request, Double Amount, String currencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        try {
            if (Amount != 0) {
                Amount = Amount * rate;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate : " + ex.getMessage(), ex);
        } finally {
            list.add(Amount);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }

    @Override
    public KwlReturnObject getBaseToCurrencyAmount(Map request, Double Amount, String newcurrencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        try {
            if (Amount != 0) {
                if (rate == 0) {
                    KwlReturnObject result = getExcDetailID(request, newcurrencyid, transactiondate, null);
                    List li = result.getEntityList();
                    if (!li.isEmpty()) {
                        Iterator itr = li.iterator();
                        ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                        rate = erd.getExchangeRate();
                    }
                }
                Amount = Amount * rate;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getBaseToCurrencyAmount : " + ex.getMessage(), ex);
        } finally {
            list.add(Amount);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }
    @Override
    public KwlReturnObject getIfBaseToCurrencyRatePresence(Map request, Double Amount, String newcurrencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        try {
            if (Amount != 0) {
                if (rate == 0) {
                    KwlReturnObject result = getExcDetailID(request, newcurrencyid, transactiondate, null);
                    List li = result.getEntityList();
                    if (!li.isEmpty()) {
                        Iterator itr = li.iterator();
                        ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                        rate = erd.getExchangeRate();
                    }
                }
                Amount = Amount * rate;
            }
        } catch (Exception ex) {
            Amount = Amount * rate;
            throw ServiceException.FAILURE("accCurrencyImpl.getBaseToCurrencyAmount : " + ex.getMessage(), ex);
        } finally {
            list.add(Amount);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }

    @Override
    public KwlReturnObject getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(Map request, Double Amount, String newcurrencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        try {
            if (Amount != 0) {
                if (rate != 0) {
                    Amount = Amount / rate;
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate : " + ex.getMessage(), ex);
        } finally {
            list.add(Amount);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }

    @Override
    public KwlReturnObject getOneCurrencyToOther(Map request, Double Amount, String oldcurrencyid, String newcurrencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        Double currencyAmount = 0.0;
        try {
            if (Amount != 0) {
                KwlReturnObject bAmt = getCurrencyToBaseAmount(request, Amount, oldcurrencyid, transactiondate, rate);
                Double baseAmount = (Double) bAmt.getEntityList().get(0);
                if (request.containsKey("isRevalue")) {
                    currencyAmount = baseAmount;
                } else {
                    bAmt = getBaseToCurrencyAmount(request, baseAmount, newcurrencyid, transactiondate, rate);
                    currencyAmount = (Double) bAmt.getEntityList().get(0);
                }
            }
            list.add(currencyAmount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getOneCurrencyToOther : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    @Override
    public KwlReturnObject getOneCurrencyToOtherWithDiffRates(Map request, Double Amount, String oldcurrencyid, String newcurrencyid, Date transactiondate, double transactionRate,double paymentRate) throws ServiceException {
        List list = new ArrayList();
        Double currencyAmount = 0.0;
        try {
            if (Amount != 0) {
                KwlReturnObject bAmt = getCurrencyToBaseAmount(request, Amount, oldcurrencyid, transactiondate, transactionRate);
                Double baseAmount = (Double) bAmt.getEntityList().get(0);
                if (request.containsKey("isRevalue")) {
                    currencyAmount = baseAmount;
                } else {
                    bAmt = getBaseToCurrencyAmount(request, baseAmount, newcurrencyid, transactiondate, paymentRate);
                    currencyAmount = (Double) bAmt.getEntityList().get(0);
                }
            }
            list.add(currencyAmount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getOneCurrencyToOther : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(Map request, Double Amount, String oldcurrencyid, String newcurrencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        Double currencyAmount = 0.0;
        try {
            if (Amount != 0) {
                KwlReturnObject bAmt = getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(request, Amount, oldcurrencyid, transactiondate, rate);
                Double baseAmount = (Double) bAmt.getEntityList().get(0);
                if (request.containsKey("isRevalue")) {
                    currencyAmount = baseAmount;
                } else {
                    bAmt = getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(request, baseAmount, newcurrencyid, transactiondate, rate);
                    currencyAmount = (Double) bAmt.getEntityList().get(0);
                }
            }
            list.add(currencyAmount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject getOneCurrencyToOtherModified(Map request, Double Amount, String oldcurrencyid, String newcurrencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        Double currencyAmount = 0.0;
        try {
            if (Amount != 0) {
                KwlReturnObject bAmt = getCurrencyToBaseAmount(request, Amount, oldcurrencyid, transactiondate, rate);
                Double baseAmount = (Double) bAmt.getEntityList().get(0);
                bAmt = getBaseToCurrencyAmount(request, baseAmount, newcurrencyid, transactiondate, 0);
                currencyAmount = (Double) bAmt.getEntityList().get(0);
            }
            list.add(currencyAmount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getOneCurrencyToOther : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(Map request, Double Amount, String oldcurrencyid, String newcurrencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        Double currencyAmount = 0.0;
        try {
            if (Amount != 0) {
                KwlReturnObject bAmt = getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(request, Amount, oldcurrencyid, transactiondate, rate);
                Double baseAmount = (Double) bAmt.getEntityList().get(0);
                bAmt = getBaseToCurrencyAmount(request, baseAmount, newcurrencyid, transactiondate, 0);
                currencyAmount = (Double) bAmt.getEntityList().get(0);
            }
            list.add(currencyAmount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

  @Override
    public KwlReturnObject getCurrencyExchange(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from ExchangeRate ";

        if (filterParams.containsKey("fromcurrencyid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "fromCurrency.currencyID=?";
            params.add(filterParams.get("fromcurrencyid"));
        }
        if (filterParams.containsKey("tocurrencyid")) {
            String currency = AccountingManager.getFilterInNumber((String)filterParams.get("tocurrencyid"));
            condition += (condition.length() == 0 ? " where " : " and ") + "toCurrency.currencyID In "+ currency;
             
        }
        query += condition;
//        query="select ID from ExchangeRate where fromCurrency.currencyID=?";
        returnList = executeQuery(query, params.toArray());       
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

 @Override
    public KwlReturnObject getExchangeRateDetails(Map<String, Object> filterParams, boolean doSort) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from ExchangeRateDetails ";
        
        String companyid = filterParams.get("companyid") != null ? filterParams.get("companyid").toString() : "";
        ExtraCompanyPreferences extraCompanyPreferencesObj = null;
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("id", companyid);
        KwlReturnObject resultExtra = getExtraCompanyPreferencestoCheckBaseCurrency(requestParams);
        if (!resultExtra.getEntityList().isEmpty()) {
            extraCompanyPreferencesObj = (ExtraCompanyPreferences) resultExtra.getEntityList().get(0);
        }
        boolean isActivateToDateforExchangeRates = extraCompanyPreferencesObj != null ? extraCompanyPreferencesObj.isActivateToDateforExchangeRates() : false;
        
        if(isActivateToDateforExchangeRates){
            // Checking all ovelapping conditions for From Date and To Date
            if (filterParams.containsKey("applydate") && filterParams.get("applydate")!=null && filterParams.containsKey("todate") && filterParams.get("todate")!=null) {
                condition += condition.length() == 0 ? " where " : " and ";
                condition +=  " ((DATE(applyDate)>=? and DATE(applyDate)<=?) or ((DATE(applyDate)>=? or DATE(toDate)>=?) and DATE(toDate)<=?) or (DATE(applyDate)<=? and DATE(toDate)>=?)) ";    //to compare with date part only - refer ticket ERP-15008
                params.add(filterParams.get("applydate"));
                params.add(filterParams.get("todate"));
                params.add(filterParams.get("applydate"));
                params.add(filterParams.get("applydate"));
                params.add(filterParams.get("todate"));
                params.add(filterParams.get("applydate"));
                params.add(filterParams.get("todate"));
            }
        }else{
            if (filterParams.containsKey("applydate") && filterParams.get("applydate")!=null) {
                condition += (condition.length() == 0 ? " where " : " and ") + "applyDate=?";
                params.add(filterParams.get("applydate"));
            }
        }        
        if (filterParams.containsKey("erid") && filterParams.get("erid")!=null) {
            condition += (condition.length() == 0 ? " where " : " and ") + "exchangeratelink.ID=?";
            params.add(filterParams.get("erid"));
        }
        if (filterParams.containsKey("companyid") && filterParams.get("companyid")!=null) {
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

    public KwlReturnObject getDefaultCurrencyExchange(Map<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        String currencyid = (String) requestParams.get("gcurrencyid");
        Date transactiondate = (Date) requestParams.get("transactiondate");
        ArrayList params = new ArrayList();
        String condition = "";
        if (transactiondate != null) {
            params.add(transactiondate);
            condition += " and applyDate <= ?  ";
        }
        params.add(currencyid);
        String query = "select er,erd from DefaultExchangeRateDetails erd, DefaultExchangeRate er where erd.exchangeratelink.ID=er.ID and"
                + " applyDate in (select max(applyDate) from DefaultExchangeRateDetails where exchangeratelink.ID=erd.exchangeratelink.ID " + condition + " group by exchangeratelink )"
                + " and fromCurrency=? order by toCurrency desc";
        returnList = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject addExchangeRateDetails(Map<String, Object> erdMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ExchangeRateDetails erd = new ExchangeRateDetails();
            erd = buildExchangeRateDetails(erd, erdMap);
            save(erd);
            list.add(erd);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addExchangeRateDetails : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Exchange Rate Details has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updateExchangeRateDetails(Map<String, Object> erdMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String erdid = (String) erdMap.get("erdid");
            ExchangeRateDetails erd = (ExchangeRateDetails) get(ExchangeRateDetails.class, erdid);
            if (erd != null) {
                erd = buildExchangeRateDetails(erd, erdMap);
            }
            save(erd);
            list.add(erd);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateExchangeRateDetails : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Exchange Rate Details has been updated successfully", null, list, list.size());
    }

    public ExchangeRateDetails buildExchangeRateDetails(ExchangeRateDetails erd, Map<String, Object> erdMap) {
        if (erdMap.containsKey("exchangerate")) {
            erd.setExchangeRate((Double) erdMap.get("exchangerate"));
        }
        if (erdMap.containsKey("applydate")) {
            erd.setApplyDate((Date) erdMap.get("applydate"));
        }
        if (erdMap.containsKey("todate") && erdMap.get("todate")!=null) {
            erd.setToDate((Date) erdMap.get("todate"));
        }
        if (erdMap.containsKey("erid")) {
            ExchangeRate er = erdMap.get("erid") == null ? null : (ExchangeRate) get(ExchangeRate.class, (String) erdMap.get("erid"));
            erd.setExchangeratelink(er);
        }
        if (erdMap.containsKey("companyid")) {
            Company company = erdMap.get("companyid") == null ? null : (Company) get(Company.class, (String) erdMap.get("companyid"));
            erd.setCompany(company);
        }
        if (erdMap.containsKey("foreigntobaseexchangerate") && erdMap.get("foreigntobaseexchangerate")!=null) {
            erd.setForeignToBaseExchangeRate((Double) erdMap.get("foreigntobaseexchangerate"));
        }
        return erd;
    }

    @Override
    public KwlReturnObject getCurrencies(Map request) throws ServiceException {
        ArrayList params = new ArrayList();
        int totalCount = 0;
        String condition = "";
        String currencycode = "";
        if (request.containsKey("currencyCode")) {
            currencycode = request.get("currencyCode").toString();
            params.add(currencycode);
            condition = " where currencyCode= ?";
        }
        String query = "from KWLCurrency" + condition;
        List list = executeQuery(query, params.toArray());
        totalCount = list.size();
        if (request.containsKey("start") && request.get("start") != null && request.containsKey("limit") && request.get("limit") != null) {
            int start = Integer.parseInt(request.get("start").toString());
            int limit = Integer.parseInt(request.get("limit").toString());
            list = executeQueryPaging(query, params.toArray(), new Integer[]{start, limit});
        }
        return new KwlReturnObject(true, "", "", list, totalCount);
    }
    
     @Override
    public KwlReturnObject getExtraCompanyPreferencestoCheckBaseCurrency(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from ExtraCompanyPreferences ";

        if (filterParams.containsKey("id")) {
            condition += " where ID=?";
            params.add(filterParams.get("id"));
        }
        query += condition;
        returnList = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getCurrencyToBaseAmountRoundOff(Map request, Double Amount, String currencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        String companyid = "";
        try {
            if (request.containsKey("companyid")) {
                companyid = (String) request.get("companyid");
            }
            if (Amount != 0) {
                if (rate == 0) {
                    KwlReturnObject result = getExcDetailID(request, currencyid, transactiondate, null);
                    List li = result.getEntityList();
                    if (!li.isEmpty()) {
                        Iterator itr = li.iterator();
                        ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                        rate = erd.getExchangeRate();
                    }
                }
                Amount = authHandler.round(Amount / rate, companyid);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getCurrencyToBaseAmount : " + ex.getMessage(), ex);
        } finally {
            list.add(Amount);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }

    @Override
    public KwlReturnObject getBaseToCurrencyAmountRoundOff(Map request, Double Amount, String newcurrencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        String companyid = "";
        try {
            if (request.containsKey("companyid")) {
                companyid = (String) request.get("companyid");
            }
            if (Amount != 0) {
                if (rate == 0) {
                    KwlReturnObject result = getExcDetailID(request, newcurrencyid, transactiondate, null);
                    List li = result.getEntityList();
                    if (!li.isEmpty()) {
                        Iterator itr = li.iterator();
                        ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                        rate = erd.getExchangeRate();
                    }
                }
                Amount = authHandler.round(Amount * rate, companyid);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getBaseToCurrencyAmount : " + ex.getMessage(), ex);
        } finally {
            list.add(Amount);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }

    @Override
    public KwlReturnObject getOneCurrencyToOtherRoundOff(Map request, Double Amount, String oldcurrencyid, String newcurrencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        Double currencyAmount = 0.0;
        try {
            if (Amount != 0) {
                if (!oldcurrencyid.equalsIgnoreCase(newcurrencyid)) {
                    KwlReturnObject bAmt = getCurrencyToBaseAmountRoundOff(request, Amount, oldcurrencyid, transactiondate, rate);
                    Double baseAmount = (Double) bAmt.getEntityList().get(0);
                    if (request.containsKey("isRevalue")) {
                        currencyAmount = baseAmount;
                    } else {
                        bAmt = getBaseToCurrencyAmountRoundOff(request, baseAmount, newcurrencyid, transactiondate, 0);
                    }
                    currencyAmount = (Double) bAmt.getEntityList().get(0);
                } else {
                    currencyAmount = Amount;
                }
            } 
            list.add(currencyAmount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getOneCurrencyToOther : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    public KwlReturnObject getExchangeRateLinkids(String companyid) throws ServiceException {
        try {
            List list = new ArrayList();
            ArrayList params = new ArrayList();
            params.add(companyid);
            List returnList = new ArrayList();
            String query = "select distinct erd.exchangeratelink.ID from ExchangeRateDetails erd where erd.company.companyID =? order by erd.applyDate asc";
            returnList = executeQuery(query, params.toArray());
            return new KwlReturnObject(true, "", null, returnList, returnList.size());
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateExchangeRateDetails : " + e.getMessage(), e);
        }
    }

    public KwlReturnObject getExchangeRateonMinApplyDate(Map<String, Object> filterParams) throws ServiceException {
        List list = new ArrayList();
        List applyDateList = new ArrayList();
        ExchangeRateDetails erd = null;
        try {
            String condition = "";
            ArrayList params = new ArrayList();

            if (filterParams.containsKey("erid")) {
                condition += (condition.length() == 0 ? " where " : " and ") + " erd.exchangeratelink.ID = ? ";
                params.add(filterParams.get("erid"));
            }
            if (filterParams.containsKey("companyid")) {
                condition += (condition.length() == 0 ? " where " : " and ") + " erd.company.companyID=? ";
                params.add(filterParams.get("companyid"));
            }
            String applyDateQuery = "select distinct(min(erd.applyDate)) from ExchangeRateDetails erd" + condition;
            applyDateList = executeQuery(applyDateQuery, params.toArray());

            if (applyDateList.get(0) != null) {
                condition += (condition.length() == 0 ? " where " : " and ") + " applyDate=? ";
                params.add(applyDateList.get(0));
            }

            String exchangeratequery = "select erd.exchangeRate from ExchangeRateDetails erd " + condition;
            applyDateList = executeQuery(exchangeratequery, params.toArray());
            Iterator itr = applyDateList.iterator();
            erd = (ExchangeRateDetails) itr.next();
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getExcDetailID : " + ex.getMessage(), ex);
        } finally {
            for (int i = 0; i < applyDateList.size(); i++) {
                list.add(applyDateList.get(i));
            }
            return new KwlReturnObject(true, "Exchange Rate Details has been updated successfully", null, applyDateList, applyDateList.size());
        }
    }
    
    @Override
    public KwlReturnObject getCurrencyFromCode(String currencyCode) throws ServiceException{
        try {
            List list = new ArrayList();
            ArrayList params = new ArrayList();
            params.add(currencyCode);
            List returnList = new ArrayList();
            String query = " from KWLCurrency where currencyCode = ?";
            returnList = executeQuery(query, params.toArray());
            return new KwlReturnObject(true, "", null, returnList, returnList.size());
        } catch (Exception e) {
            throw ServiceException.FAILURE("getCurrencyFromCode : " + e.getMessage(), e);
        }
        
    }
 
    @Override
    public KwlReturnObject saveConsolidation(Map<String, Object> requestParams) throws ServiceException{
        List list= new ArrayList();
        try{
            ConsolidationData consolidationData=null;
            if(requestParams.containsKey("consolidationid") && requestParams.get("consolidationid")!=null && !StringUtil.isNullOrEmpty(requestParams.get("consolidationid").toString())){
                 consolidationData = requestParams.get("consolidationid") == null ? null : (ConsolidationData) get(ConsolidationData.class, (String) requestParams.get("consolidationid"));
            }
            if(consolidationData==null){
               consolidationData=new ConsolidationData(); 
            }
            if(requestParams.containsKey("stakeinpercentage") && requestParams.get("stakeinpercentage")!=null){
                consolidationData.setStakeInPercentage(Double.parseDouble(requestParams.get("stakeinpercentage").toString()));
            }
            if(requestParams.containsKey("childcompanyid") && requestParams.get("childcompanyid")!=null){
                Company childCompany = requestParams.get("childcompanyid") == null ? null : (Company) get(Company.class, (String) requestParams.get("childcompanyid"));
                consolidationData.setChildCompany(childCompany);
            }
            if(requestParams.containsKey("companyid") && requestParams.get("companyid")!=null){
                Company company = requestParams.get("companyid") == null ? null : (Company) get(Company.class, (String) requestParams.get("companyid"));
                consolidationData.setCompany(company);
            }
            saveOrUpdate(consolidationData);
            list.add(consolidationData);
        } catch(NumberFormatException | ServiceException ex){
            throw ServiceException.FAILURE("saveConsolidation : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject saveConsolidationExchangeRateDetails(Map<String, Object> requestParams) throws ServiceException{
        List list= new ArrayList();
        try{
            ConsolidationExchangeRateDetails cerDetails=new ConsolidationExchangeRateDetails();
            if(requestParams.containsKey("id") && requestParams.get("id")!=null && !StringUtil.isNullOrEmpty(requestParams.get("id").toString())){
                 cerDetails = (ConsolidationExchangeRateDetails) get(ConsolidationExchangeRateDetails.class, (String) requestParams.get("id"));
            }
            
            if(cerDetails==null){
               cerDetails=new ConsolidationExchangeRateDetails(); 
            }
            
            if(requestParams.containsKey("exchangerate") && requestParams.get("exchangerate")!=null){
                cerDetails.setExchangeRate(Double.parseDouble(requestParams.get("exchangerate").toString()));
            }
            if(requestParams.containsKey("applydate") && requestParams.get("applydate")!=null){
                cerDetails.setApplyDate((Date)requestParams.get("applydate"));
            }
            if(requestParams.containsKey("consolidationid") && requestParams.get("consolidationid")!=null){
                ConsolidationData consolidationData = (ConsolidationData) get(ConsolidationData.class, (String) requestParams.get("consolidationid"));
                cerDetails.setConsolidationData(consolidationData);
            }
            saveOrUpdate(cerDetails);
            list.add(cerDetails);
        } catch(NumberFormatException | ServiceException ex){
            throw ServiceException.FAILURE("saveConsolidation : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject getConsolidation(Map requestMap) throws ServiceException {
        List returnList = new ArrayList();
        try {
            String companyid=(String) requestMap.get("companyid");
            ArrayList params = new ArrayList();
            params.add(companyid);
            String query = " from ConsolidationData where company.companyID = ?";
            if(requestMap.containsKey("selectedSubdomains") && requestMap.get("selectedSubdomains")!=null && !StringUtil.isNullOrEmpty(requestMap.get("selectedSubdomains").toString())){
                String subdomainsIds = AccountingManager.getFilterInString(requestMap.get("selectedSubdomains").toString());
                query+=" and childCompany.companyID in"+subdomainsIds;
            }
            returnList = executeQuery(query, params.toArray());
        } catch (Exception e) {
            throw ServiceException.FAILURE("getCurrencyFromCode : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    @Override
    public KwlReturnObject getSQLConsolidation(Map requestMap) throws ServiceException {
        List returnList = new ArrayList();
        try {
            String companyid=(String) requestMap.get("companyid");
            ArrayList params = new ArrayList();
            params.add(companyid);
            String query = "select stakeinpercentage, childcompany, company, id from consolidationdata where company = ?";
            returnList = executeSQLQuery(query, params.toArray());
        } catch (Exception e) {
            throw ServiceException.FAILURE("getSQLConsolidation : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
    @Override
    public KwlReturnObject getConsolidationExchangeRate(Map filterMap) throws ServiceException {
        List returnList = new ArrayList();
        try {
            String consalidationid=filterMap.get("consolidationid").toString();
            ArrayList params = new ArrayList();
            params.add(consalidationid);
            String query = " from ConsolidationExchangeRateDetails where consolidationData.ID = ?";
            
            if(filterMap.containsKey("applydate") && filterMap.get("applydate")!=null){
                Date applyDate = (Date)filterMap.get("applydate");
                query+=" and applyDate=?";
                params.add(applyDate);
            } else if(filterMap.containsKey("recentapplydate") && filterMap.get("recentapplydate")!=null){
                Date applyDate = (Date)filterMap.get("recentapplydate");
                query+=" and applyDate <= ?";
                params.add(applyDate);
            }
            query += " order by applyDate DESC ";//Here order desc is matters so before changing please communicate
            returnList = executeQuery(query, params.toArray());
        } catch (Exception e) {
            throw ServiceException.FAILURE("getConsolidationExchangeRate : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
        @Override
    public KwlReturnObject getCustomCurrencies(String companyID) throws ServiceException {
        ArrayList params = new ArrayList();
        List list = null;
        params.add(companyID);
        String query = "from CustomCurrency where companyid=?";
        list = executeQuery(query, params.toArray());

        return new KwlReturnObject(true, "", "", list, list.size());
    }
 @Override
    public KwlReturnObject getCustomCurrencies(String companyID, String currencyid) throws ServiceException {
        ArrayList params = new ArrayList();
        List list = null;
        params.add(companyID);
        params.add(currencyid);
        String query = "from CustomCurrency where companyid=? and currencyid=?";
        list = executeQuery(query, params.toArray());

        return new KwlReturnObject(true, "", "", list, list.size());
    }

    public CustomCurrency buildCustomCurrency(CustomCurrency customCurrency, Map<String, Object> customCurrencyMap) throws ServiceException {
           if (customCurrencyMap.containsKey("currencyid")) {
            KWLCurrency curid = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.KWLCurrency", customCurrencyMap.get("currencyid").toString());
            customCurrency.setCurrencyID(curid);
        }
            if (customCurrencyMap.containsKey("name")) {
                customCurrency.setName(customCurrencyMap.get("name").toString());
            }
            if (customCurrencyMap.containsKey("systemcurrencysymbol")) {
                customCurrency.setSystemcurrencysymbol(customCurrencyMap.get("systemcurrencysymbol").toString());
            }
            if (customCurrencyMap.containsKey("systemcurrencycode")) {
                customCurrency.setSystemcurrencycode(customCurrencyMap.get("systemcurrencycode").toString());
            }
            if (customCurrencyMap.containsKey("customcurrencysymbol")) {
                customCurrency.setCustomcurrencysymbol(customCurrencyMap.get("customcurrencysymbol").toString());
            }
            if (customCurrencyMap.containsKey("customcurrencycode")) {
                customCurrency.setCustomcurrencycode(customCurrencyMap.get("customcurrencycode").toString());
            }
            if (customCurrencyMap.containsKey("companyid")) {
                customCurrency.setCompanyid(customCurrencyMap.get("companyid").toString());
            }
        return customCurrency;
    }
    public KwlReturnObject addCustomCurrency(Map<String, Object> customCurrencyMap) throws ServiceException {
        List list = new ArrayList();
        try {
            CustomCurrency cutomcurrency = new CustomCurrency();
            cutomcurrency = buildCustomCurrency(cutomcurrency, customCurrencyMap);
            save(cutomcurrency);
            list.add(cutomcurrency);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addCustomCurrency : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Custom Currency has been added successfully", null, list, list.size());
    }
    public KwlReturnObject updateCustomCurrency(Map<String, Object> customCurrencyMap) throws ServiceException {
     List list = new ArrayList();
        try {
            String id = (String) customCurrencyMap.get("id");
            CustomCurrency cutomcurrency = (CustomCurrency) get(CustomCurrency.class, id);
            if (cutomcurrency != null) {
                cutomcurrency = buildCustomCurrency(cutomcurrency, customCurrencyMap);
            }
            save(cutomcurrency);
            list.add(cutomcurrency);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addCustomCurrency : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Custom Currency has been updated successfully", null, list, list.size());
    }
    //Delete custom currency from customcurrency table.
    public KwlReturnObject deleteCurrencySymbol(String currencyid, String companyid) throws ServiceException {
        String delQuery = "delete from customcurrency where currencyid=? and companyid = ? ";
        int numRows = executeSQLUpdate(delQuery, new Object[]{currencyid, companyid});
        return new KwlReturnObject(true, "Cutom Currency has been deleted successfully.", null, null, numRows);
    }
}
