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
package com.krawler.spring.accounting.salesorder;

import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.*;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.MessagingException;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface AccSalesOrderServiceDAO {

    public JSONArray getQuotationsJson(HashMap<String, Object> requestParams, List list, JSONArray jArr) throws ServiceException;

    public JSONArray getTermDetails(String id, boolean isOrder) throws ServiceException;

    public double getQuotationDetailStatusSO(QuotationDetail quod) throws ServiceException;

    public double getQuotationDetailStatusINV(QuotationDetail quod) throws ServiceException;

    public JSONObject getQuotationRows(HashMap<String, Object> requestParams) throws SessionExpiredException, ServiceException;

    public String getTimeIntervalForProduct(String inouttime) throws ParseException, java.text.ParseException;
    
    public HashMap<String, Object> getSalesOrdersMap (HttpServletRequest request) throws SessionExpiredException;
    
    public double getPOCount(HashMap<String, Object> orderParams) throws ServiceException, JSONException, SessionExpiredException;
    
    public double getSOCount(HashMap<String, Object> orderParams) throws ServiceException, JSONException, SessionExpiredException;
   
    public JSONArray getSalesOrdersJsonMerged(JSONObject jobj, List<Object[]> list, JSONArray jArr) throws ServiceException;
    
    public JSONArray getSalesOrdersJson(HashMap<String, Object> requestParams,JSONArray dataJArr,JSONObject paramJobj) throws ServiceException;

    public String getSalesOrderStatus(SalesOrder so) throws ServiceException;
    
    public String getSalesOrderStatusNew(SalesOrder so, Set<SalesOrderDetail> orderset, CompanyAccountPreferences pref, String companyid) throws ServiceException;

    public JSONObject getSalesOrderRows(JSONObject paramJobj) throws SessionExpiredException, ServiceException, ParseException;
    
    public JSONObject getLinkedAdvancePayments(JSONObject paramJobj) throws ServiceException;
    
    public double getSalesOrderBalanceQuantity(SalesOrderDetail salesOrderDetail);

    public String getNewBatchJson(Product product, JSONObject paramJobj, String documentid) throws ServiceException, SessionExpiredException, JSONException;

    public double getSalesOrderDetailStatusForDO(SalesOrderDetail sod) throws ServiceException;

    public double getSalesOrderDetailStatus(SalesOrderDetail sod) throws ServiceException;

    public JSONArray getSalesOrderJsonForLinking(JSONArray jsonArray, List salesorders, KWLCurrency currency, DateFormat df);
    
    public JSONArray getPurchaseOrderJsonForLinking(JSONArray jsonArray, List salesorders, KWLCurrency currency, DateFormat df);
    
    public JSONArray getVendorQuotationJsonForLinking(JSONArray jsonArray, List salesorders, KWLCurrency currency, DateFormat df, String companyid);
    
    public JSONArray getCustomerQuotationJsonForLinking(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, DateFormat df,int linkType);
    
    public JSONArray getSODetailsItemJSON(JSONObject requestObj, String SOID, HashMap<String, Object> paramMap);
    
    public HashMap<String, JSONArray> getSODetailsJobOrderFlowItemJSON(JSONObject requestObj, String SOID, HashMap<String, Object> paramMap);
    
    public String getSOStatus(SalesOrder so, CompanyAccountPreferences pref,ExtraCompanyPreferences extraCompanyPreferences) throws ServiceException;
    
    public HashMap<String, Object> getSalesOrdersMapJson (JSONObject paramJobj) throws SessionExpiredException, JSONException, ServiceException;
    
    public List<String> approveSalesOrder(SalesOrder soObj, HashMap<String, Object> soApproveMap, boolean isMailApplicable) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException, JSONException;
    
    public JSONObject getDailySalesReportByCustomer(HttpServletRequest request, Map<String, Object> requestParams) throws ServiceException;
    
    public JSONObject getMonthlySalesOrdesByCustomer(HttpServletRequest request, Map<String, Object> requestParams) throws ServiceException;
    
    public JSONObject getYearlySalesOrdersByCustomer(HttpServletRequest request, Map<String, Object> requestParams) throws ServiceException;
    
    public JSONObject calculateAndUpdateTotalShippingCost(JSONArray dataJArr, JSONObject paramJobj) throws ServiceException;
    
    public JSONObject getCQLinkedInTransaction(JSONObject paramJObj)throws SessionExpiredException, ServiceException;
    
    public JSONObject generatePOFromMultipleSO(JSONObject paramJObj) throws ServiceException, SessionExpiredException, JSONException;
    
    public JSONObject getProductQuanityJSONForJWO(List<String> saDetailIds);
    
}
