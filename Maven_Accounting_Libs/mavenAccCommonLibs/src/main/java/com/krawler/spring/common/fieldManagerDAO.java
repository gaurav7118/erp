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
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface fieldManagerDAO {

    public KwlReturnObject getFieldParams(HashMap<String, Object> requestParams)throws ServiceException;
    public List getFieldComboValue(int colnum, String productid) throws ServiceException, JSONException;
    public List getTaxPercentageUsingDefaultTerm(int colnum, String defaulttermid, String companyid, String productid, String entityID, Date applyDate) throws ServiceException, JSONException;
    public String getParamsValue(String fieldid,String fieldValue)throws ServiceException;
    public String getIdsUsingParamsValue(String fieldid,String fieldValue)throws ServiceException;
    public String getIdsUsingParamsValueWithoutInsert(String fieldid,String fieldValue)throws ServiceException;
        
    public int getColumnFromFieldParams(String fieldLabel, String companyId, int module,int customColumn) throws ServiceException;
    
    public List getFieldwiseColumnFieldParams(JSONObject params) throws ServiceException;
    
    public String getFieldComboDtaValueUsingId(String fieldid,String companyid)throws ServiceException;
     
    public KwlReturnObject getDefaultHeaders(HashMap<String, Object> requestParams);
    
    public String getFieldParamsId(HashMap<String, Object> requestParams)throws ServiceException;
    
    public KwlReturnObject getUsersGroupMappedToFCD(Map<String, Object> map) throws ServiceException;

    public String appendUsersCondition(Map<String, Object> reqparams) throws ServiceException;
    
    public boolean isKnockOffAdvancedSearch(String Searchjson, String companyId) throws ServiceException;
    
    public KwlReturnObject getAddressDimensionMapping(JSONObject nObject) throws ServiceException;
    
    public String getStateForEntity(JSONObject reqParams) throws ServiceException;

    public KwlReturnObject getFieldComboDescAndValue(JSONObject reqParams);
    
    public KwlReturnObject saveGstDocumentHistory(Map<String, Object> reqMap) throws ServiceException;
    
    public KwlReturnObject saveGstTaxClassHistory(Map<String, Object> reqMap) throws ServiceException;
    
    public List getGSTDocumentHistory(JSONObject reqParams) throws ServiceException, JSONException;
    
    public List getGSTTaxClassHistory(JSONObject reqParams) throws ServiceException, JSONException;
    
    public void deleteGstTaxClassDetails(String docrefid) throws ServiceException;
    
    public void deleteGstDocHistoryDetails(String docrefid) throws ServiceException;
    
    public JSONObject GetUserAmendingPrice(HashMap<String, Object> requestParams) throws ServiceException, JSONException;
    
    public boolean isTaxActivated(String companyId, String taxId) throws ServiceException;
    
    public List getEntityDataForRequestedModule(JSONObject reqParams) throws ServiceException;
    
    public List getColnumForDimensionCollectively(Map<String,Integer> fieldData,JSONObject reqParams) throws ServiceException;
    
}
