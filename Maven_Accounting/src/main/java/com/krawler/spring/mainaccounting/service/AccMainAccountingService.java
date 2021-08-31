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

import com.itextpdf.text.DocumentException;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public interface AccMainAccountingService {
      public JSONObject getAccountsForCombo(HttpServletRequest request, HttpServletResponse response)throws ServiceException, SessionExpiredException;
      public JSONObject getCustomCombodata(HttpServletRequest request, HttpServletResponse response)throws ServiceException, SessionExpiredException;
      public String getModuleName(int moduleid);
      public JSONObject getFieldParams(HttpServletRequest request, HttpServletResponse response);
      public JSONObject getInvoiceCreationJson(HttpServletRequest request, HttpServletResponse response ) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException;
      public JSONObject getDesignTemplateList(HttpServletRequest request, HttpServletResponse response);
      public Map<String, Object> getCustomFieldsForExport(HashMap<String, String> customFieldMap, Map<String, Object> variableMap,HashMap<String, String> customDateFieldMap) throws ServiceException;
      public JSONObject getAccountsForComboJson(JSONObject paramJobj) throws SessionExpiredException, JSONException, ServiceException;
      public JSONObject getAccountsIdNameJson(JSONObject paramJobj) throws SessionExpiredException, JSONException, ServiceException;
}
