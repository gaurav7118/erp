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
package com.krawler.spring.accounting.ws.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

/**
 *
 * @author krawler
 */
public interface GroupCompanyService {

    public JSONObject convertPOtoSO(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    
    public JSONObject convertMakePaymenttoReceivePayment(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    
    public JSONObject convertDebitNotetoCreditNote(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;

    public JSONObject convertPIwithGRNtoSIwithDO(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;

    public JSONObject deletePurchaseOrderPermanent(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;

    public JSONObject deleteSalesOrdersPermanent(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    
    public JSONObject deleteSalesOrders(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    
    public JSONObject deleteInvoiceandDO(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException,AccountingException;
    
    public JSONObject convertPurchaseReturnToSalesReturn(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException ;
    
    public JSONObject deleteSalesReturn(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    
    public JSONObject deleteMakePaymentPermanent(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    
    public JSONObject deletePurchaseReturnPermanent(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    
    public JSONObject deleteVendorInvoiceandGRN(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, AccountingException;

    public JSONObject deleteReceivePayment(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    
    public JSONObject convertGRNtoDO(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;

    public JSONObject deleteDeliveryOrder(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
}
