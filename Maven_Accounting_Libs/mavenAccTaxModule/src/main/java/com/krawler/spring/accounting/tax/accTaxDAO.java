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
package com.krawler.spring.accounting.tax;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author krawler
 */
public interface accTaxDAO {

    public KwlReturnObject belongsTo1099(String companyid, ArrayList acclist) throws ServiceException;

    public KwlReturnObject getTax1099AccCategory(Map<String, Object> request) throws ServiceException;

    public KwlReturnObject deleteTax1099AccountList(String taxcategoryid, String companyid) throws ServiceException;

    public KwlReturnObject getTax(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getTax1099Category(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getTaxCategoryAccount(String companyid, String id) throws ServiceException;

    public KwlReturnObject getTaxList(Map<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject addTax(Map<String, Object> taxMap) throws ServiceException;

    void copyTax1099Category(String companyid) throws ServiceException;

    public KwlReturnObject updateTax(Map<String, Object> taxMap) throws ServiceException;

    public KwlReturnObject deleteTax(String taxid, String companyid) throws ServiceException;

    public KwlReturnObject addTaxList(Map<String, Object> taxListMap) throws ServiceException;

    public KwlReturnObject updateTax1099Account(Map<String, Object> taxListMap) throws ServiceException;

    public KwlReturnObject updateTax1099Category(Map<String, Object> taxCategoryMap) throws ServiceException;

    public KwlReturnObject updateTaxList(Map<String, Object> taxListMap) throws ServiceException;

    public KwlReturnObject deleteTaxList(String taxid, String companyid) throws ServiceException;

    KwlReturnObject getCalculatedTax(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getTaxFromAccount(String accountid, String companyid) throws ServiceException;

    public KwlReturnObject getTaxPercent(String companyid, Date transactiondate, String taxid) throws ServiceException;

    public KwlReturnObject getDefaultGSTList(HashMap<String, Object> dataMap) throws ServiceException;
    
    public boolean isTermMappedwithTax (HashMap<String, Object> requestParams)  throws ServiceException;
    
    public List getTerms(String tax) throws ServiceException;
    
    public JSONObject getTerms(String tax, JSONObject obj) throws ServiceException;
    
    public KwlReturnObject getAllTaxOfCompany(String companyID) throws ServiceException;
    
    public boolean belongsTo1099Count(String companyid, ArrayList accIDArr) throws ServiceException;
    
    public KwlReturnObject updateApplyDateForTaxes(HashMap<String, Object> requestParams) throws ServiceException; //Update the applydate of all tax

    public Set<Object> getTaxIdsWithLandedCost(String companyid)throws ServiceException;
}
