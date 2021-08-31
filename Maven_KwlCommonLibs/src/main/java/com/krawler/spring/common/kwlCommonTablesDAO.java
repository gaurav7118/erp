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
package com.krawler.spring.common;

import com.krawler.common.service.ServiceException;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Karthik
 */
public interface kwlCommonTablesDAO {

    public KwlReturnObject getObject(String classpath, String id) throws ServiceException;

    public Object getClassObject(String classpath, String id) throws ServiceException;
    
    public Object getRequestedObjectFields(Class classname, String[] columnNames, Map<String, Object> paramMap) throws ServiceException;
    
    public KwlReturnObject populateMasterInformation(Map<String, String> requestParams) throws ServiceException;

    public KwlReturnObject getAllTimeZones() throws ServiceException;

    public KwlReturnObject getAllCurrencies(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getAllDateFormats(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getAllCountries() throws ServiceException;
    
    public KwlReturnObject getAllStates(String countryid) throws ServiceException;

    public KwlReturnObject getSubdomainListFromCountry(String countryid) throws ServiceException;

    public DateFormat getUserDateFormatter(String dateFormatId, String userTimeFormatId, String timeZoneDiff) throws ServiceException;

    public KwlReturnObject getEditHelpComponent(HashMap requestMap) throws ServiceException;
    
    public String getCompanyId(String subdomain) throws ServiceException;
    
    public String getCountryTimezoneID(String countryid) throws ServiceException;
    
    public void updateCompanyUsersTimezone(String companyid) throws ServiceException;
    
    public void saveObj(Object obj)throws ServiceException;
    public void saveorUpdateObj(Object obj)throws ServiceException;
    
    public void evictObject(Object obj) throws ServiceException;
    
    public KwlReturnObject getSampleFileDataList(HashMap<String,Object> map) throws ServiceException;
   
    public KwlReturnObject getIsCurrenyCodeAndIsActivatedTodate(String conmpanyId) throws ServiceException;
    
    public KwlReturnObject getLandingCostCategoryStore(String companyid) throws ServiceException;
        
    public List getCommonQueryForTermsInLinking(String tableName,String ids) throws ServiceException;
    
    public List getPercentAndTaxNameFromTaxid(String taxid,String companyid) throws ServiceException;
    
    public List getRequestedObjectFieldsInCollection(Class c, String[] columnNames, Map<String, Object> paramMap) throws ServiceException;
    
    public List getSummationOfTermAmtAndTermTaxAmt(String tableName,String transactionId) throws ServiceException;
    public Map<String, Object[]> getSummationOfTermAmtAndTermTaxAmtList(String tableName,String transactionIdList) throws ServiceException;
}
