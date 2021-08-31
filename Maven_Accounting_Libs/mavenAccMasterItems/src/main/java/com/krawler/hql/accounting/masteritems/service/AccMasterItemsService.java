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
package com.krawler.hql.accounting.masteritems.service;

import com.krawler.common.admin.FieldParams;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface AccMasterItemsService {

    public JSONObject getMasterItems(HttpServletRequest request) throws ServiceException, SessionExpiredException;

    public JSONArray getMasterItems(JSONObject jobj) throws ServiceException;
    
    public JSONObject getMasterItemsForEclaim(HttpServletRequest request) throws ServiceException, SessionExpiredException;
    
    public JSONObject getMasterItemsForEclaim(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException;
        
    public JSONObject addEditMasterItemsForEclaim(HttpServletRequest request) throws ServiceException, SessionExpiredException;
    
    public JSONObject deleteMasterItemsForEclaim(HttpServletRequest request) throws ServiceException, SessionExpiredException;
    
    public JSONObject sendProcessSkill(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException;
    
    public JSONObject getWarehouseItems(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException;
    
    public JSONObject getLocationItemsFromStore(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException;
    
    public JSONObject getNewBatches(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException;
    
    public JSONObject getLevels(Map<String, Object> requestParams)throws ServiceException;
    
    public JSONObject getStoreMasters(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException;
    
    public JSONObject getNewSerials(Map<String, Object> requestMap) throws ServiceException, SessionExpiredException;
    
    public JSONObject sendMasterToPM(Map map);
    
    public JSONObject getNewBatches(JSONObject paramJObj) throws ServiceException, SessionExpiredException;
    
    public JSONObject getBatches(HttpServletRequest request) throws ServiceException, SessionExpiredException;
    
    public JSONObject getNewSerials(JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    
    public String getParentItemsForMap(FieldParams fieldParams, String parentValueid) throws ServiceException;

    public String getUserGroupParentForMap(HashMap<String, Object> reqParams) throws ServiceException;
            
    public JSONObject saveUserGroup(JSONObject reqParams) throws ServiceException;

    public JSONObject saveUserGroupFieldComboMapping(JSONObject reqParams) throws ServiceException;

    public JSONObject saveUserGroupFieldComboMappingForchild(JSONObject reqParams) throws ServiceException;

    public JSONObject getUsersGroup(JSONObject reqParams) throws ServiceException, JSONException;

    public JSONObject deleteUsersGroup(JSONObject reqParams) throws JSONException, ServiceException;
    
    public void checkUseInEclaim(HashMap<String, Object> params) throws AccountingException;
    
    public JSONObject deleteDimension (JSONObject paramJobj) throws ServiceException, JSONException, AccountingException;
    
    public JSONObject validateEntityCustomFieldUsage(JSONObject requestJobj) throws JSONException, ServiceException;
    
    public void deleteDimensionFromChildCompanies(JSONObject paramJobj, String fieldidsString) throws JSONException, ServiceException,AccountingException;
    
    public JSONObject getAddressMappingForDimension(JSONObject reqParams) throws JSONException, ServiceException;
    
    public void saveMultiEntityDimCustomDataJSON(JSONObject paramJobj) throws SessionExpiredException, ServiceException, com.krawler.utils.json.base.JSONException;
    
    public JSONObject deleteMasterItem(JSONObject reqParams);
    
    public JSONObject getMasterItemsForCustomFoHire(JSONObject paramJObj);
    
    public String getModuleName(int moduleid);
    
    public JSONObject getMasterGroups(JSONObject paramJObj);
    
    public JSONObject getInspectionTemplateList(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException;
    
    public JSONObject getInspectionAreaList(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException;
    
}
