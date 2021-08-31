/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting.currency.service;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccCurrencyExchangeRate {
    
    public JSONObject getUpdatedExchangeRates(Map request) throws ServiceException;
}
