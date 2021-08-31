/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.purchaseorder;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.*;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface AccPurchaseOrderServiceDAO {

    public HashMap<String, Object> getPurchaseOrderMap(HttpServletRequest request) throws SessionExpiredException;
    
    public HashMap<String, Object> getPurchaseOrderMap(JSONObject paramObj) throws SessionExpiredException;

    public JSONArray getPurchaseOrdersJsonMerged(HashMap<String, Object> requestParams, List list, JSONArray jArr) throws ServiceException;

    public JSONObject getPurchaseOrdersMerged(JSONObject requestParams) throws ServiceException,SessionExpiredException;

    public JSONArray getPurchaseOrdersVersionJson(HashMap<String, Object> requestParams, List list, JSONArray jArr) throws ServiceException;
    
    public JSONArray getPurchaseOrderVersionRows(HashMap<String, Object> requestParams) throws ServiceException;
    
    public JSONArray getPOVersiovTermDetails(String id, boolean isOrder) throws ServiceException;

    public JSONArray getSecurityGateEntryJsonMerged(HashMap<String, Object> requestParams, List list, JSONArray jArr) throws ServiceException;

    public String getBillingPurchaseOrderStatus(BillingPurchaseOrder po) throws ServiceException;
    
    public JSONArray getPurchaseRequisitionJsonForLinking(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, int linkType) throws ServiceException;
    

    public JSONArray getRFQJsonForLinking(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, int linkType) throws ServiceException;
    
    public String getPurchaseOrderStatus(PurchaseOrder po) throws ServiceException;
    
    public String getPurchaseRequisitionStatus(PurchaseRequisition pReq) throws ServiceException;

    public int getQAPOStatus(PurchaseOrder po) throws ServiceException;

    public JSONArray getTermDetails(String id, boolean isOrder) throws ServiceException;
    
    
    
    public JSONArray getPurchaseOrderRows(HashMap<String, Object> requestParams) throws ServiceException;
    
    public JSONArray getSecurityGateEbtryOrderRows(HashMap<String, Object> requestParams) throws ServiceException;
    
    public String getTimeIntervalForProduct(String inouttime) throws ParseException, java.text.ParseException;
    
    public double getPurchaseOrderDetailStatusForGRO(PurchaseOrderDetail pod) throws ServiceException;
    
    public double getPurchaseOrderDetailStatus(PurchaseOrderDetail pod) throws ServiceException;
    
    public JSONArray getQuotationsJson(JSONObject paramObj, List list, JSONArray jArr) throws ServiceException;
    
    public double getQuotationDetailStatusPO(VendorQuotationDetail quod) throws ServiceException;
    
    public double getQuotationDetailStatusGR(VendorQuotationDetail quod) throws ServiceException;
    
    public JSONObject getQuotationRows(HttpServletRequest request) throws SessionExpiredException, ServiceException, ParseException ;
    
    public int getQAPOStatusUsingPODetails(Set<PurchaseOrderDetail> orderDetail) throws ServiceException;
    
    public String getPurchaseOrderStatusUsingPODetails(CompanyAccountPreferences preferences,String companyid,Set<PurchaseOrderDetail> orderDetail) throws ServiceException;
    
    public JSONArray getVersionQuotationsJson(HttpServletRequest request, List list, JSONArray jArr) throws ServiceException;
    
    public JSONArray getPODetailsItemJSON(JSONObject paramJobj, String SOID, HashMap<String, Object> paramMap);
    
    public JSONObject fetchShippingAddress(JSONObject paramJObj) throws ServiceException, SessionExpiredException, JSONException ;
    
    public JSONObject fetchShippingAddressForGRN(JSONObject paramJObj) throws ServiceException, SessionExpiredException, JSONException;
    
    public JSONObject getRFQs(JSONObject paramJObj);
    
    public JSONArray getRFQJson(JSONObject paramJObj, List list, JSONArray jArr,HashMap<String, Object> requestParams) throws ServiceException;
    
    public JSONObject getRequisitions(JSONObject paramJObj);
    
    public JSONArray getRequisitionJson(JSONObject paramJObj, List list, JSONArray jArr,boolean ispending,HashMap<String, Object> filterRequestParams) throws ServiceException;
}
