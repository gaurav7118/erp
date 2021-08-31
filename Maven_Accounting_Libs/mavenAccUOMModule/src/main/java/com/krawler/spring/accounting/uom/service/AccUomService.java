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
package com.krawler.spring.accounting.uom.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public interface AccUomService {

    public JSONObject getUnitOfMeasure(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException, JSONException;

    public JSONArray getUoMJson(HttpServletRequest request, List list) throws ServiceException;
    
    public JSONArray getUoMJson(JSONObject paramjobj, List<UnitOfMeasure> list) throws ServiceException;
    
    public JSONObject getISUOMSchemaConfiguredandUsed(Map<String,Object> requestData) throws ServiceException;
    
    public JSONArray getPurchaseUOMSchemaJson(HashMap requestParams, List list) throws ServiceException;
    
    public JSONArray getSalesUOMSchemaJson(HashMap requestParams, List list) throws ServiceException;
    
    public JSONArray getOrderUOMSchemaJson(HashMap requestParams, List list) throws ServiceException;
    
    public JSONArray getTransferUOMSchemaJson(HashMap requestParams, List list) throws ServiceException;
    
    public JSONArray getUnitOfMeasureOfProductUOMSchemaJSON(HttpServletRequest request, List list) throws ServiceException;
}
