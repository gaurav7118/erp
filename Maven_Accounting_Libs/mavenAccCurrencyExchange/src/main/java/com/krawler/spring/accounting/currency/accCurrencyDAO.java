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

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface accCurrencyDAO {

    public KwlReturnObject addExchangeRateDetails(Map<String, Object> erdMap) throws ServiceException;

    public KwlReturnObject updateExchangeRateDetails(Map<String, Object> erdMap) throws ServiceException;

    public KwlReturnObject getCurrencyExchange(Map<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject getExchangeRateDetails(Map<String, Object> filterParams, boolean doSort) throws ServiceException;

    public KwlReturnObject getDefaultCurrencyExchange(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getExcDetailID(Map request, String currencyid, Date transactiondate, String erid) throws ServiceException;

    public KwlReturnObject getCurrencyToBaseAmount(Map request, Double Amount, String currencyid, Date transactiondate, double rate) throws ServiceException;
    
    public String getCurrencyFromPriceList(String productid, String companyid,String carryin,String uomid) throws ServiceException;
    
    public Map<String, String> getCurrencyFromPriceList(String companyid, String carryin) throws ServiceException ;
    
    public KwlReturnObject getforeignToBaseAmountAndBaseToSGD(Map request, Double Amount, String currencyid, Date transactiondate, double rate,double gstCurrencyRate) throws ServiceException;

    public double getCurrencyToBaseRate(Map request, String currencyid, Date transactiondate) throws ServiceException;

    public KwlReturnObject getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(Map request, Double Amount, String currencyid, Date transactiondate, double rate) throws ServiceException;

    public KwlReturnObject getCurrencyToBaseAmountRoundOff(Map request, Double Amount, String currencyid, Date transactiondate, double rate) throws ServiceException;

    public KwlReturnObject getBaseToCurrencyAmount(Map request, Double Amount, String currencyid, Date transactiondate, double rate) throws ServiceException;
    public KwlReturnObject getIfBaseToCurrencyRatePresence(Map request, Double Amount, String currencyid, Date transactiondate, double rate) throws ServiceException;

    public KwlReturnObject getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(Map request, Double Amount, String currencyid, Date transactiondate, double rate) throws ServiceException;

    public KwlReturnObject getBaseToCurrencyAmountRoundOff(Map request, Double Amount, String currencyid, Date transactiondate, double rate) throws ServiceException;

    public KwlReturnObject getOneCurrencyToOther(Map request, Double Amount, String oldcurrencyid, String newcurrencyid, Date transactiondate, double rate) throws ServiceException;
    public KwlReturnObject getOneCurrencyToOtherWithDiffRates(Map request, Double Amount, String oldcurrencyid, String newcurrencyid, Date transactiondate, double transactionRate,double paymentRate) throws ServiceException;

    public KwlReturnObject getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(Map request, Double Amount, String oldcurrencyid, String newcurrencyid, Date transactiondate, double rate) throws ServiceException;

    public KwlReturnObject getOneCurrencyToOtherModified(Map request, Double Amount, String oldcurrencyid, String newcurrencyid, Date transactiondate, double rate) throws ServiceException;
    
    public KwlReturnObject getOneCurrencyToOtherModifiedGstCurrencyRate(Map request, Double Amount, String oldcurrencyid, String newcurrencyid, Date transactiondate, double rate) throws ServiceException;

    public KwlReturnObject getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(Map request, Double Amount, String oldcurrencyid, String newcurrencyid, Date transactiondate, double rate) throws ServiceException;

    public KwlReturnObject getOneCurrencyToOtherRoundOff(Map request, Double Amount, String oldcurrencyid, String newcurrencyid, Date transactiondate, double rate) throws ServiceException;

    public KwlReturnObject getExchangeRateLinkids(String companyid) throws ServiceException;

    public KwlReturnObject getExchangeRateonMinApplyDate(Map<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject getCurrencies(Map request) throws ServiceException;

    public KwlReturnObject getExtraCompanyPreferencestoCheckBaseCurrency(Map<String, Object> filterParams) throws ServiceException;
    public KwlReturnObject getCurrencyFromCode(String currencyCode) throws ServiceException;
    
    public KwlReturnObject getMaxNearestExchangeRate(Map request, String currencyid, Date transactiondate, String erid) throws ServiceException;
    
    public KwlReturnObject saveConsolidation(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject saveConsolidationExchangeRateDetails(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getConsolidation(Map requestMap) throws ServiceException;

    public KwlReturnObject getConsolidationExchangeRate(Map filterMap) throws ServiceException;

    public KwlReturnObject getCustomCurrencies(String companyid) throws ServiceException;

    public KwlReturnObject getCustomCurrencies(String companyid, String currencyid) throws ServiceException;

    public KwlReturnObject addCustomCurrency(Map<String, Object> customCurrencyMap) throws ServiceException;

    public KwlReturnObject updateCustomCurrency(Map<String, Object> customCurrencyMap) throws ServiceException;
    
    public KwlReturnObject deleteCurrencySymbol(String currencyid, String companyid) throws ServiceException;
    
    public KwlReturnObject getSQLConsolidation(Map requestMap) throws ServiceException;
    
}
