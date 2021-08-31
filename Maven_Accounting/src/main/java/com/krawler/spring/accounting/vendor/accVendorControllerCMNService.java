/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.vendor;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface accVendorControllerCMNService {
    public JSONArray getRecordsForStore(HashMap<String, Object> requestParams,String modules, JSONArray jarrRecords) throws SessionExpiredException, ServiceException;
    
    public JSONArray getColumnsForGrid(HashMap<String, Object> requestParams,String modules, JSONArray jarrColumns) throws SessionExpiredException, ServiceException;
    
    public JSONArray getPurchaseInvoiceInformation(HashMap<String, Object> requestParams, List<Object []> invoices, JSONArray DataJArr) throws SessionExpiredException, ServiceException;
    
    public JSONArray getPurchaseOrdersInformation(HashMap<String, Object> requestParams, List<Object []> orders, JSONArray DataJArr) throws SessionExpiredException, ServiceException;
    
    public JSONArray getPurchaseReturnInformation(HashMap<String, Object> requestParams, List<Object []> returns, JSONArray DataJArr) throws SessionExpiredException, ServiceException;
    
    public JSONArray getQuotationsInformation(HashMap<String, Object> requestParams, List<Object> vquotations, JSONArray DataJArr) throws SessionExpiredException, ServiceException, ParseException;
    
    public JSONArray getGoodsReceiptInformation(HashMap<String, Object> requestParams, List<Object []> greceipts, JSONArray DataJArr) throws SessionExpiredException, ServiceException;
    
    public JSONArray getDebitNoteInformation(HashMap<String, Object> requestParams, List<Object []> dnotes, JSONArray DataJArr) throws SessionExpiredException, ServiceException;
    
    public JSONArray getOpeningDebitNoteInformation(HashMap<String, Object> requestParams, List<Object []> dnotes, JSONArray DataJArr) throws SessionExpiredException, ServiceException;
    
    public JSONArray getCreditNoteInformation(HashMap<String, Object> requestParams, List<Object []> cnotes, JSONArray DataJArr) throws SessionExpiredException, ServiceException;
    
    public JSONArray getOpeningCreditNoteInformation(HashMap<String, Object> requestParams, List<Object []> cnotes, JSONArray DataJArr) throws SessionExpiredException, ServiceException;
    
    public JSONArray getMadePaymentsInformation(HashMap<String, Object> requestParams, List<Object []> payments, JSONArray DataJArr) throws SessionExpiredException, ServiceException;
    
    public JSONArray getOpeningMadePaymentsInformation(HashMap<String, Object> requestParams, List<Object []> payments, JSONArray DataJArr) throws SessionExpiredException, ServiceException;
    
    public JSONArray getReceivedPaymentsInformation(HashMap<String, Object> requestParams, List<Object []> receipts, JSONArray DataJArr) throws SessionExpiredException, ServiceException;
    
    public JSONObject saveVendor(JSONObject paramJobj);
    
    public void saveVendorAddresses(JSONObject paramJobj) throws ServiceException, SessionExpiredException, AccountingException ;
    
    public JSONObject activateDeactivateVendors(JSONObject paramJobj) ;
    
    public JSONObject isCustomerVendorUsedInTransacton(JSONObject paramJobj)throws JSONException,ServiceException;
    
    public JSONObject getVendorGSTHistory(JSONObject reqParams) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    
      public JSONObject getVendorGSTUsedHistory(JSONObject reqParams) throws ServiceException, JSONException, ParseException ;
    
    public JSONObject saveVendorGSTHistoryAuditTrail(JSONObject reqParams) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    
    public JSONObject deleteVendor(JSONObject paramJobj) throws ServiceException, AccountingException;
}
