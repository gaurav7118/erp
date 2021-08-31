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
package com.krawler.spring.mainaccounting.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface AccCustomerMainAccountingService {
 
    public JSONObject getCustomerExceedingCreditLimit(HttpServletRequest request) throws ServiceException, SessionExpiredException, JSONException;

    public JSONArray getCustomerAmountDue(JSONArray jArr, HttpServletRequest request);

    public JSONObject getCustomerAmountDueByObject(JSONObject jSONObject, HttpServletRequest request, HashMap<String, Object> requestParams) throws JSONException, ServiceException, SessionExpiredException;

    public HashMap<String, Object> getCustomerRequestMapJSON(JSONObject paramJobj) throws SessionExpiredException, JSONException, ServiceException;

    public JSONObject getCustomers(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException;

    public JSONObject getCustomerAmountDueByObject(JSONObject jSONObject, JSONObject paramJobj, HashMap<String, Object> requestParams) throws JSONException, ServiceException, SessionExpiredException;

    public JSONArray getCustomerAmountDue(JSONArray jArr, JSONObject paramJobj);
    
    public JSONObject getCustomerExceedingCreditLimit(JSONObject paramJObj);
    
    public JSONArray getCustomerJson(JSONObject paramJobj, List<Object[]> list) throws SessionExpiredException, ServiceException;

    public String[] getCustomerCategoryIDs(String customerid);
    
    public String[] getMultiSalesPersonIDs(String customerid);
    
    public JSONObject saveCustomerCheckInOut(JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    
    public JSONObject getCustomerCheckIn(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException;
    
    public JSONObject getCustomerCheckInCheckOutRegistryGridInfo(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException;
    
    public JSONObject getIncidentCasesRegistryGridInfo(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException;
    
    public JSONObject getCustomerCheckInandCheckOutDetails(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException ;
    
    public JSONObject getIncidentCasesDetails(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException ;
    
    public JSONArray createCheckInCheckOutJSON(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException ;
    
    public JSONArray createIncidentCasesDetailJSON(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException ;
}
