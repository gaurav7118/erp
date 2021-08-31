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

import com.krawler.common.admin.ProductBatch;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.hql.accounting.SalesOrderDetail;
import com.krawler.utils.json.base.JSONArray;
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
public interface AccSalesOrderAccountingService {
    public JSONObject getSalesOrders(HttpServletRequest request, HttpServletResponse response);
    public JSONObject getSalesOrderRows(HttpServletRequest request) throws SessionExpiredException, ServiceException;
    public HashMap<String, Object> getSalesOrdersMap(HttpServletRequest request) throws SessionExpiredException;
    public double getSalesOrderDetailStatusForDO(SalesOrderDetail sod) throws ServiceException;
    public double getSalesOrderDetailStatus(SalesOrderDetail sod) throws ServiceException;
    public String getSalesOrderStatus(SalesOrder so) throws ServiceException; 
    public JSONArray getTermDetails(String id,boolean isOrder) throws ServiceException;
    public JSONObject getQuotations(HttpServletRequest request, HttpServletResponse response);
    public JSONArray getQuotationsJson(HttpServletRequest request, List list, JSONArray jArr) throws ServiceException;
    public JSONObject getQuotationRows(HttpServletRequest request, HttpServletResponse response);
    public void getAssetDetailJsonObject(HttpServletRequest request, SalesOrderDetail row, JSONObject obj) throws ServiceException, JSONException, SessionExpiredException;
   public String getBatchJson(ProductBatch productBatch, boolean isFixedAssetDO,boolean isbatch,boolean isBatchForProduct,boolean isserial,boolean isSerialForProduct,HttpServletRequest request) throws ServiceException, SessionExpiredException, JSONException ;
   public JSONObject saveQuotation(HttpServletRequest request, HttpServletResponse response);
}
