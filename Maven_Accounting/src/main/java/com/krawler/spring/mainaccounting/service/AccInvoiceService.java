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
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.DeliveryOrderDetail;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public interface AccInvoiceService {
     public JSONObject getInvoicesMerged(HttpServletRequest request, HttpServletResponse response);
     public JSONObject getDeliveryOrdersMerged(HttpServletRequest request, HttpServletResponse response);
     public JSONObject getDeliveryOrderRows(HttpServletRequest request) throws SessionExpiredException, ServiceException;
     public String getBatchJson(ProductBatch productBatch, boolean isFixedAssetDO, HttpServletRequest request,boolean isbatch,boolean isBatchForProduct,boolean isserial,boolean isSerialForProduct) throws ServiceException, SessionExpiredException, JSONException;
     public void getASsetDetailsJson(DeliveryOrderDetail row,  String companyid, JSONObject obj, DateFormat df,CompanyAccountPreferences preferences,HttpServletRequest request) throws JSONException, ServiceException, SessionExpiredException ;
     public JSONObject exportSingleInvoice(HttpServletRequest request, HttpServletResponse response);
}
