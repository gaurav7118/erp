/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.periodSettings;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface accPeriodSettingsDao {

    public KwlReturnObject saveTaxPeriod(HashMap<String, Object> hm);

    public KwlReturnObject getChildTaxPeriods(Map<String, Object> requestParams);

    public KwlReturnObject saveAccountingPeriodSettings(Map<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getParentAccountingPeriods(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject checkExistingDatesforAccounting(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject checkExistingDatesforTaxPeriod(Map<String, Object> requestParams) throws ServiceException;

    public String checkFinancialYearDatesforTaxPeriod(Map<String, Object> requestParams) throws ServiceException;
    
    public String checkFinancialYearDatesforAccounting(Map<String, Object> requestParams) throws ServiceException;
    
    public int deleteAccountingPeriods(Map<String, Object> requestParams) throws ServiceException;
    
    public int deleteTaxPeriods(Map<String, Object> requestParams) throws ServiceException;
    
}
