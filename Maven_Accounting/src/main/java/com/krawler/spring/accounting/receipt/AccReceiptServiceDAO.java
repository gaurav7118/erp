/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.receipt;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.DebitNote;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.Payment;
import com.krawler.hql.accounting.Receipt;
import com.krawler.hql.accounting.ReceiptAdvanceDetail;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public interface AccReceiptServiceDAO {
    public JSONObject saveReceipt(HttpServletRequest request, HttpServletResponse response);
    
    public List<JSONObject> getReceiptJson(JSONObject paramJObj, List list, List<JSONObject> jsonObjectlist) throws SessionExpiredException, ServiceException;
  
    public List<JSONObject> getOpeningBalanceReceiptJsonForReport(JSONObject paramJObj, List list, List<JSONObject> jsonObjectlist);

    public JSONObject getReceiptRowsJSONNew(JSONObject paramJObj) throws SessionExpiredException, ServiceException;
    
    public HashMap<String, Object> getReceiptRequestMapJSON(JSONObject paramJobj) throws SessionExpiredException,JSONException;

    public JSONObject getReceiptFromLMS(HashMap<String, Object> requestParams) throws SessionExpiredException, ServiceException;

    public double getReceiptAmountDue(Receipt receipt);
    public JSONObject getInvoiceJSON(Invoice invoice, Map invoiceMap, Map<String, Object> requestParams);
    public JSONObject getDebitNoteJSON(DebitNote debitNote, Map<String, Object> requestParams);
    public JSONObject getPaymentJSON(Payment payment, Map<String, Object> requestParams)  throws JSONException,ServiceException,SessionExpiredException;
    public JSONArray getPurchaseReturnJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid);
    public JSONArray getRPDetailsItemJSONNew(JSONObject paramJobj, String SOID, HashMap<String, Object> paramMap) throws SessionExpiredException, ServiceException, JSONException;
    
    public JSONObject saveReceiptJson(JSONObject paramJobj) throws JSONException, ServiceException;
    public KwlReturnObject getReceiptList(JSONObject paramJobj);
    
    public void getAdvanceReceiptCustomData(HashMap<String, Object> requestParams, ReceiptAdvanceDetail advanceDetail, JSONObject obj) throws ServiceException;
    
    public JSONObject getAdvanceCustomerPaymentForRefunds(JSONObject paramJobj) throws ServiceException;
    public JSONArray getAllSalesReceiptKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException;
    public JSONArray getOpeningSalesReceiptKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException;
    public JSONArray getSalesReceiptKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException;
}
