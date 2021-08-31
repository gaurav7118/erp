/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.goodsreceipt;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.CreditNoteDetail;
import com.krawler.hql.accounting.DebitNoteDetail;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.LinkDetailPayment;
import com.krawler.hql.accounting.LinkDetailReceipt;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.ModelAndView;
import com.lowagie.text.DocumentException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
/**
 *
 * @author krawler
 */
public interface AccGoodsReceiptServiceDAO {

    public JSONArray getVendorAgedPayableMerged(HttpServletRequest request, HashMap requestParams) throws SessionExpiredException, ServiceException;
    public JSONArray getVendorAgedPayableMerged(JSONObject request, HashMap requestParams) throws SessionExpiredException, ServiceException;
    
    public JSONArray getParentChildVendorAgedPayableMerged(HttpServletRequest request, HashMap requestParams) throws SessionExpiredException, ServiceException;

    public void setMessageSource(MessageSource msg);

    public JSONArray getBadDebtClaimedInvoicesJson(HttpServletRequest request) throws SessionExpiredException, ServiceException;
    
    public JSONArray getBadDebtInvoices(HashMap<String, Object> badMaps) throws SessionExpiredException, ServiceException, JSONException;
    
    public JSONArray getRecoveredBadDebtInvoices(HttpServletRequest request) throws SessionExpiredException, ServiceException;
     
    public JSONArray getMonthlyVendorAgedPayableMerged(HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    
    public JSONArray getMonthlyVendorAgedPayableMerged(JSONObject request) throws ServiceException, JSONException, SessionExpiredException, ParseException;

    public void exportMonthlyAgedPayableSummarized(HttpServletRequest request, HttpServletResponse response, JSONObject jobj) throws DocumentException, ServiceException, IOException;

    public void exportMonthlyAgedPayableDetails(HttpServletRequest request, HttpServletResponse response, JSONObject jobj) throws DocumentException, ServiceException, IOException;
    
    public JSONArray getVendorPartyLedgerSummary(HttpServletRequest request, HashMap requestParams) throws SessionExpiredException, ServiceException;
    
    public JSONObject getVendorAgedPayablebasedonDimensions(HttpServletRequest request, HashMap<String, Object> requestParams) throws ServiceException;

    public JSONObject getVendorAgedPayableDetailedbasedonDimensions(HttpServletRequest request, HashMap<String, Object> requestParams) throws ServiceException;
    
    public List unlinkDeditNoteFromPurchaseInvoice(HttpServletRequest request, List<DebitNoteDetail> details,String cnid) throws ServiceException, SessionExpiredException;
       
    public List unlinkPaymentFromInvoice(HttpServletRequest request, List<LinkDetailPayment> details, String paymentid) throws ServiceException, SessionExpiredException ;
    
    public JSONObject getJobWorkOutIngradientDetails(JSONObject reqParams) throws JSONException,ServiceException;
    
    public JSONArray getAgedPayableReceivableBarChartJson(HttpServletRequest request, String companyid, double[] totalAmountDueInBase) throws JSONException;
    
    public JSONArray getAgedPayableReceivablePieChartJson(String companyid, JSONArray jArr) throws JSONException;
    
    public void getGoodsReceiptCustomDataForPayment(HashMap<String, Object> request, JSONObject obj, GoodsReceipt goodsReceipt, JournalEntry je) throws ServiceException;
    
    public JSONObject  deletePurchaseReturnPermanentJSON(JSONObject paramJobj) throws SessionExpiredException ;
    
    public JSONObject deleteGoodsReceiptPermanentJSON(JSONObject paramJobj) ;
    public JSONArray getGoodsReceiptListForLinking(HashMap<String, Object> requestParams) throws JSONException, ServiceException;
    
    public HashMap getVendorAgedPayableMap(HttpServletRequest request, HashMap<String, Object> requestParams) throws SessionExpiredException,  UnsupportedEncodingException,   ServiceException;
    
    public void getCustmDataForPurchaseInvoice(HashMap<String, Object> request, JSONArray jArr,String companyid,HashMap replaceFieldMap, HashMap customFieldMap, HashMap customDateFieldMap, HashMap FieldMap, HashMap replaceFieldMapRows, HashMap customFieldMapRows, HashMap customDateFieldMapRows, HashMap fieldMapRows) throws JSONException, ServiceException, SessionExpiredException;
   
    public JSONArray getPurchaseReturnJson(Map request, List list) throws ServiceException;
    
    public JSONObject isAllITCReversal(JSONObject reqParams) throws ServiceException,JSONException;
}
