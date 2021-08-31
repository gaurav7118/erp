/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting.currency.service;

import com.krawler.spring.accounting.currency.AccTaxCurrencyExchangeDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.TaxExchangeRate;
import com.krawler.hql.accounting.TaxExchangeRateDetails;
import com.krawler.spring.accounting.currency.CurrencyContants;
import static com.krawler.spring.accounting.currency.CurrencyContants.APPLYDATE;
import static com.krawler.spring.accounting.currency.CurrencyContants.COMPANYID;
import static com.krawler.spring.accounting.currency.CurrencyContants.CURRENCYCODE;
import static com.krawler.spring.accounting.currency.CurrencyContants.DATA;
import static com.krawler.spring.accounting.currency.CurrencyContants.ERDID;
import static com.krawler.spring.accounting.currency.CurrencyContants.ERID;
import static com.krawler.spring.accounting.currency.CurrencyContants.EXCHANGERATE;
import static com.krawler.spring.accounting.currency.CurrencyContants.FOREIGNTOBASEEXCHANGERATE;
import static com.krawler.spring.accounting.currency.CurrencyContants.FROMCURRENCY;
import static com.krawler.spring.accounting.currency.CurrencyContants.FROMCURRENCYID;
import static com.krawler.spring.accounting.currency.CurrencyContants.HTMLCODE;
import static com.krawler.spring.accounting.currency.CurrencyContants.ID;
import static com.krawler.spring.accounting.currency.CurrencyContants.ISMAXNEARESTEXCHANGERATE;
import static com.krawler.spring.accounting.currency.CurrencyContants.NEWEXCHANGERATE;
import static com.krawler.spring.accounting.currency.CurrencyContants.SYMBOL;
import static com.krawler.spring.accounting.currency.CurrencyContants.TOCURRENCY;
import static com.krawler.spring.accounting.currency.CurrencyContants.TOCURRENCYID;
import static com.krawler.spring.accounting.currency.CurrencyContants.TODATE;
import static com.krawler.spring.accounting.currency.CurrencyContants.TRANSACTIONDATE;
import com.krawler.spring.accounting.currency.accCurrencyController;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;

/**
 *
 * @author krawler
 */
public class AccTaxCurrencyExchangeSerivceImpl implements AccTaxCurrencyExchangeService, CurrencyContants {

    private MessageSource messageSource;
    private AccTaxCurrencyExchangeDAO accTaxCurExngeDAOObj;
    
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    public void setAccTaxCurrencyExchangeDAO(AccTaxCurrencyExchangeDAO accTaxCurExngeDAOObj) {
        this.accTaxCurExngeDAOObj = accTaxCurExngeDAOObj;
    }
    
    @Override
    public boolean saveTaxCurrencyExchange(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException, AccountingException {
        try {
            Locale requestcontextutilsobj = null;
            if (requestParams.containsKey("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
            }
            boolean updateRate = (boolean) requestParams.get("changerate");
            JSONArray jArr = new JSONArray(requestParams.get(DATA).toString());
            String companyid = requestParams.get("companyid").toString();
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                Date appDate = null;
                if (StringUtil.isNullOrEmpty(jobj.getString(APPLYDATE))) {
                    throw new AccountingException(messageSource.getMessage("acc.curex.excp1", null, requestcontextutilsobj));
                } else {
                    appDate = df.parse(StringUtil.DecodeText(jobj.optString(APPLYDATE)));
                }
                Date toDateVal = null;
                if (StringUtil.isNullOrEmpty(jobj.getString(TODATE))) {
                    throw new AccountingException(messageSource.getMessage("acc.curex.exception2", null, requestcontextutilsobj));
                } else {
                    toDateVal = df.parse(StringUtil.DecodeText(jobj.optString(TODATE)));
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
                KwlReturnObject result = accTaxCurExngeDAOObj.getTaxExchangeRateDetails(filterParams, false);
                List list = result.getEntityList();
                Map<String, Object> erdMap = new HashMap<String, Object>();
                if (StringUtil.isNullOrEmpty(jobj.getString(EXCHANGERATE))) {
                    throw new AccountingException(messageSource.getMessage("acc.curex.excp2", null, requestcontextutilsobj));
                } else {
                    erdMap.put(EXCHANGERATE, Double.parseDouble(StringUtil.DecodeText(jobj.optString(EXCHANGERATE))));
                }
                if (jobj.has(FOREIGNTOBASEEXCHANGERATE) && !StringUtil.isNullOrEmpty(jobj.getString(FOREIGNTOBASEEXCHANGERATE))) {
                    erdMap.put(FOREIGNTOBASEEXCHANGERATE, Double.parseDouble(StringUtil.DecodeText(jobj.optString(FOREIGNTOBASEEXCHANGERATE))));
                }
                TaxExchangeRateDetails erd;
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
                        erdresult = accTaxCurExngeDAOObj.addTaxExchangeRateDetails(erdMap);
                    } else {
                        erd = (TaxExchangeRateDetails) list.get(0);
                        erdMap.put(ERDID, erd.getID());
                        erdresult = accTaxCurExngeDAOObj.updateTaxExchangeRateDetails(erdMap);
                    }
                    erd = (TaxExchangeRateDetails) erdresult.getEntityList().get(0);
                }
            }
        } /*catch (UnsupportedEncodingException ex) {
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

    public void saveTaxCurrencyExchangeDetail(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException, AccountingException {
        try {

            String companyid = requestParams.get("companyid").toString();
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            Date appDate = null;
            Date toDate = null;
            String erid = "", exchangeRate = "";
            Locale requestcontextutilsobj = null;
            if (requestParams.containsKey("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
            }
            if (requestParams.containsKey(APPLYDATE) && requestParams.get(APPLYDATE) != null && (!StringUtil.isNullOrEmpty(requestParams.get(APPLYDATE).toString()))) {
                appDate = df.parse(StringUtil.DecodeText(requestParams.get(APPLYDATE).toString()));
            } else {
                throw new AccountingException(messageSource.getMessage("acc.curex.excp1", null, requestcontextutilsobj));
            }
            if (requestParams.containsKey(TODATE) && requestParams.get(TODATE) != null && (!StringUtil.isNullOrEmpty(requestParams.get(TODATE).toString()))) {
                toDate = df.parse(StringUtil.DecodeText(requestParams.get(TODATE).toString()));
            } else {
                throw new AccountingException(messageSource.getMessage("acc.curex.excp1", null, requestcontextutilsobj));
            }
            if (requestParams.containsKey(ID) && requestParams.get(ID) != null && (!StringUtil.isNullOrEmpty(requestParams.get(ID).toString()))) {
                erid = requestParams.get(ID).toString();
            } else {
                throw new AccountingException(messageSource.getMessage("acc.curex.excp1", null, requestcontextutilsobj));
            }
            if (requestParams.containsKey(EXCHANGERATE) && requestParams.get(EXCHANGERATE) != null && (!StringUtil.isNullOrEmpty(requestParams.get(EXCHANGERATE).toString()))) {
                exchangeRate = requestParams.get(EXCHANGERATE).toString();
            } else {
                throw new AccountingException(messageSource.getMessage("acc.curex.excp1", null, requestcontextutilsobj));
            }

            Map<String, Object> erdMap = new HashMap<String, Object>();
            erdMap.put(APPLYDATE, appDate);
            erdMap.put(TODATE, toDate);
            erdMap.put(ERID, erid);
            erdMap.put(COMPANYID, companyid);
            erdMap.put(EXCHANGERATE, Double.parseDouble(StringUtil.DecodeText(exchangeRate)));
            accTaxCurExngeDAOObj.addTaxExchangeRateDetails(erdMap);

        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE("Can't extract the records. <br>Encoding not supported", ex);
        } */catch (ParseException ex) {
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveTaxCurrencyExchange : " + ex.getMessage(), ex);
        }

    }

    public JSONArray getTaxCurrencyExchangeJson(Map<String, Object> requestParams, List<TaxExchangeRate> list, boolean isOnlyBaceCurrencyflag) throws SessionExpiredException, ParseException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            boolean isCurrencyExchangeWindow = false;
            Locale requestcontextutilsobj = null;
            if (requestParams.containsKey("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
            }
            Date transactiondate = null;
            if (requestParams.containsKey(TRANSACTIONDATE) && requestParams.get(TRANSACTIONDATE) != null && (!StringUtil.isNullOrEmpty(requestParams.get(TRANSACTIONDATE).toString()))) {
                transactiondate = df.parse(StringUtil.DecodeText(requestParams.get(TRANSACTIONDATE).toString()));
            } else {
                throw new AccountingException(messageSource.getMessage("acc.curex.excp1", null, requestcontextutilsobj));
            }
            if (requestParams.containsKey("iscurrencyexchangewindow") && requestParams.get("iscurrencyexchangewindow") != null) {
                isCurrencyExchangeWindow = Boolean.parseBoolean((String) requestParams.get("iscurrencyexchangewindow"));
            }
            
            requestParams.put("isCurrencyExchangeWindow", isCurrencyExchangeWindow);
//            String isAddAll = requestParams.get("isAll").toString();
            JSONObject obj = new JSONObject();
            if (list != null && !list.isEmpty()) {
                for (TaxExchangeRate ER : list) {
                    String erID = ER.getID();
                    KwlReturnObject erdresult = accTaxCurExngeDAOObj.getTaxExcDetailID(requestParams, null, transactiondate, erID);
                    TaxExchangeRateDetails erd = (TaxExchangeRateDetails) erdresult.getEntityList().get(0);
                    boolean isMaxNearestExchangeRate = Boolean.parseBoolean(erdresult.getEntityList().get(1).toString());
                    obj = new JSONObject();
                    if (erd != null) {
                        if (isOnlyBaceCurrencyflag && !erd.getExchangeratelink().getFromCurrency().getCurrencyID().equals(erd.getExchangeratelink().getToCurrency().getCurrencyID())) {
                            continue;
                        }
                        obj.put(ID, erd.getExchangeratelink().getID());
                        obj.put(APPLYDATE, df.format(erd.getApplyDate()));
                        obj.put(TODATE, erd.getToDate() != null ? df.format(erd.getToDate()) : null);
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
            throw ServiceException.FAILURE("getTaxCurrencyExchangeJson : " + ex.getMessage(), ex);
        } /*catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AccTaxCurrencyExchangeSerivceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } */catch (AccountingException ex) {
            Logger.getLogger(AccTaxCurrencyExchangeSerivceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }

    public JSONArray getTaxCurrencyExchangeListJson(Map<String, Object> requestParams, List<TaxExchangeRateDetails> list) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        DateFormat df = (DateFormat) requestParams.get(Constants.df);
        try {

            if (list != null && !list.isEmpty()) {
                for (TaxExchangeRateDetails erd : list) {
                    JSONObject obj = new JSONObject();
                    obj.put(ID, erd.getExchangeratelink().getID());
                    obj.put(APPLYDATE, df.format(erd.getApplyDate()));
                    if (erd.getToDate() != null) {
                        obj.put(TODATE, df.format(erd.getToDate()));
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
            throw ServiceException.FAILURE("getTaxCurrencyExchangeListJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }


}
