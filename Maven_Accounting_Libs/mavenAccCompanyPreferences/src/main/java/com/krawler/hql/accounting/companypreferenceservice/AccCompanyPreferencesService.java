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
package com.krawler.hql.accounting.companypreferenceservice;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public interface AccCompanyPreferencesService {

    public JSONObject getSequenceFormatStore(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;

    public JSONObject getNextAutoNumber(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;

    public JSONObject getCompanyAccountPreferences(HttpServletRequest request, HttpServletResponse response);

    public JSONObject getCompanyAddressDetails(HashMap<String, Object> requestParams) throws ServiceException, SessionExpiredException;

    public JSONObject saveCompanyAddressDetails(HashMap<String, Object> requestParams) throws ServiceException, SessionExpiredException;
    
    public JSONObject saveSMTPAuthenticationDetails(HashMap<String, Object> requestParams) throws ServiceException, SessionExpiredException;
    
    public JSONObject getSMTPAuthenticationDetails(HashMap<String, Object> requestParams) throws ServiceException, SessionExpiredException;
    
    public JSONObject getCompanyAccountPreferences(HttpServletRequest request, CompanyAccountPreferences pref, List ll, List chequeFormatList) throws ServiceException, SessionExpiredException;
    
    public JSONObject getCompanyInventoryAccountPreferences(JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    
    public JSONObject saveIndianGSTSettings(JSONObject paramJobj) throws ServiceException, JSONException;
    
    public JSONObject getNextAutoNumber(JSONObject paramJobj);
    
    public JSONObject getAgedDateFilter(String userId);
    
    public JSONObject deleteSequenceFormat(JSONObject paramJobj) throws ServiceException;
}
