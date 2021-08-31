/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.entitygst;

import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.List;

/**
 *
 * @author swapnil.khandre
 */
public interface AccGSTReportServiceDAO {

    public void getColumnModelForGSTR3BDetails(JSONArray jarrRecords, JSONArray jarrColumns, JSONObject params) throws JSONException;

    public JSONObject getSalesInvoiceJSONArrayForGSTR3B(List invoiceData, JSONObject reqParams, String companyId) throws JSONException;

    public JSONObject getPurchaseInvoiceJSONArrayForGSTR3B(List invoiceData, JSONObject reqParams, String companyId) throws JSONException;

    public JSONObject getCreditNoteJSONArrayForGSTR3B(List cnData, JSONObject reqParams, String companyId) throws JSONException;
    
    public JSONObject getDebitNoteJSONArrayForGSTR3B(List cnData, JSONObject reqParams, String companyId) throws JSONException;
}
