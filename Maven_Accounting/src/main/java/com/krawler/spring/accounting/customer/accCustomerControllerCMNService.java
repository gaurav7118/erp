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
package com.krawler.spring.accounting.customer;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.*;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
/**
 *
 * @author krawler
 */
public interface accCustomerControllerCMNService {

    public JSONObject getCustomerFromCRMAccounts(HashMap<String, Object> reqParams, JSONObject resObj) throws ServiceException;

    public List getCustomerAddressInfoByAliasName(String AliasName, String customerid, String companyid, boolean isbillingAddress) throws ServiceException;

    public JSONObject deleteCustomer(HashMap<String, Object> reqParams, JSONObject resObj) throws ServiceException;

    public HashMap deleteCustomerArray(JSONObject resObj, String companyid, boolean propagateTOChildCompaniesFalg, JSONArray propagatedCustomerjarr) throws ServiceException, AccountingException, SessionExpiredException;

    public UOBReceivingDetails saveUOBReceivingBankDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException;

    public HashMap<String, Object> getUOBReceivingBankDetailsRequestParamsMap(HttpServletRequest request) throws SessionExpiredException;

    public JSONArray getUOBReceivingBankDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException;

    public JSONArray getUOBReceivingBankDetails(List UOBReceivingBankDetailsList, boolean isForForm) throws JSONException, ServiceException;

    public void deleteUOBReceivingBankDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException;

    public JSONArray getGiroFileGenerationHistory(HashMap map) throws SessionExpiredException, ServiceException;

    public JSONArray getGiroFileGenerationHistoryJson(List<GiroFileGenerationHistory> historyList, DateFormat df) throws JSONException, ServiceException;

    public JSONObject saveCustomer(JSONObject paramJObj);
    
    public void saveCustomerAddressesJSON(JSONObject paramJObj) throws ServiceException, SessionExpiredException, AccountingException;

    public JSONObject saveCustomerAddresses(JSONObject paramJObj);

    public JSONObject activateDeactivateCustomers(JSONObject paramJObj);
    
    public JSONObject getCustomerGSTHistory(JSONObject reqParams) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    
    public JSONObject getCustomerGSTUsedHistory(JSONObject reqParams) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    
    public JSONObject saveCustomerGSTHistoryAuditTrail(JSONObject reqParams) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    
    public JSONObject deleteCustomer(JSONObject paramJobj) throws ServiceException, AccountingException;
}
