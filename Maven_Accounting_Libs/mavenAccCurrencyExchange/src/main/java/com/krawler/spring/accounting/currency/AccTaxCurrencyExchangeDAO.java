/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.currency;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccTaxCurrencyExchangeDAO {

    public KwlReturnObject getTaxExchangeRateDetails(Map<String, Object> filterParams, boolean doSort) throws ServiceException;

    public KwlReturnObject addTaxExchangeRateDetails(Map<String, Object> erdMap) throws ServiceException;

    public KwlReturnObject updateTaxExchangeRateDetails(Map<String, Object> erdMap) throws ServiceException;

    public KwlReturnObject getTaxCurrencyExchange(Map<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject getTaxExcDetailID(Map request, String currencyid, Date transactiondate, String erid) throws ServiceException;

}
