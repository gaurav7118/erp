/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.gst.services;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.List;

/**
 *
 * @author Suhas.Chaware
 */
public interface GSTR1ServiceDao {

    public JSONObject getB2BInvoices(JSONObject reqParams) throws JSONException, ServiceException;
    
    public JSONObject getDocumentDetails(JSONObject reqParams) throws JSONException, ServiceException;

    public JSONObject getB2CLInvoices(JSONObject reqParams) throws JSONException, ServiceException;

    public JSONObject getB2CSInvoices(JSONObject reqParams) throws JSONException, ServiceException;

    public JSONObject getCDNRInvoices(JSONObject reqParams) throws JSONException, ServiceException;

    public JSONObject getAT(JSONObject reqParams) throws JSONException, ServiceException;

    public JSONObject getCDNURInvoices(JSONObject reqParams) throws JSONException, ServiceException;

    public JSONObject getAdvanceReceiptAdjustmentMerged(JSONObject reqParams) throws JSONException, ServiceException;

    public JSONObject getHSNSummarydetails(JSONObject reqParams) throws JSONException, ServiceException;
    
    public JSONObject getExportTypeInvoice(JSONObject reqParams) throws JSONException, ServiceException;
    
    public JSONArray createJsonForAdvanceDataFetchedFromDB(List<Object> invoiceList, JSONObject params) throws JSONException;
    
    public JSONObject getGSTMissingInvoice(JSONObject reqParams) throws JSONException, ServiceException;
    
    public void setB2BInvoiceList(JSONArray array, List<Object> invoiceList, JSONObject params) throws JSONException;
    
    public void setB2BCNDetailsList(JSONArray array, List<Object> invoiceList, JSONObject reqParams) throws JSONException;
}
