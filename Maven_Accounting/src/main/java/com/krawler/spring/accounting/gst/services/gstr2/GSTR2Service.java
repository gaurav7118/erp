/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.gst.services.gstr2;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.List;

/**
 *
 * @author krawler
 */
public interface GSTR2Service {

    public JSONObject getB2BInvoices(JSONObject json) throws JSONException,ServiceException;

    public JSONObject getImportofGoodsInvoices(JSONObject json) throws JSONException;

    public JSONObject getImportofServicesBills(JSONObject json) throws JSONException;

    public JSONObject getCDNInvoices(JSONObject json) throws JSONException,ServiceException;

    public JSONObject getNilRatedSupplies(JSONObject json) throws JSONException;

    public JSONObject getTaxliabilityunderReverseChargeSummary(JSONObject json) throws JSONException;

    public JSONObject getTaxPaidUnderReverseCharge(JSONObject json) throws JSONException;

    public JSONObject getHSNSummaryofInwardsupplies(JSONObject json) throws JSONException;

    public JSONObject getB2BUnregisteredInvoice(JSONObject json) throws JSONException;

    public JSONObject getITCReversalDetails(JSONObject json) throws JSONException;

    public JSONObject getCDNUnregisteredData(JSONObject json) throws JSONException;

    public JSONObject getGSTR2Summary(JSONObject json) throws JSONException;
    
    public JSONObject getHSNSummarydetails(JSONObject reqParams) throws JSONException, ServiceException;
    
    public JSONObject getRCMOnAdvance(JSONObject reqParams) throws JSONException, ServiceException;
    
    public JSONObject getTaxPaidOnAdvance(JSONObject reqParams) throws JSONException, ServiceException;
    
    public void setB2BInvoiceDetailsList(JSONArray array, List<Object> invoiceList, JSONObject reqParams) throws JSONException;
    
    public void setDNCNDetailsList(JSONArray array, List<Object> invoiceList, JSONObject reqParams) throws JSONException;
    
    public JSONObject getITCJournalEntryDetails(JSONObject reqParams,JSONArray columnDataArr) throws ServiceException, JSONException;
}
