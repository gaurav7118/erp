/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting.currency.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.TaxExchangeRate;
import com.krawler.hql.accounting.TaxExchangeRateDetails;
import com.krawler.utils.json.base.JSONArray;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccTaxCurrencyExchangeService {

    public boolean saveTaxCurrencyExchange(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException, AccountingException;

    public void saveTaxCurrencyExchangeDetail(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException, AccountingException;

    public JSONArray getTaxCurrencyExchangeJson(Map<String, Object> requestParams, List<TaxExchangeRate> list, boolean isOnlyBaceCurrencyflag) throws SessionExpiredException, ParseException, ServiceException;

    public JSONArray getTaxCurrencyExchangeListJson(Map<String, Object> requestParams, List<TaxExchangeRateDetails> list) throws SessionExpiredException, ServiceException;
}
