/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.salesreturn;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface AccSalesReturnService {

    public List savePurchaseReturn(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException;
    
    public KwlReturnObject getPendingConsignmentRequests(String companyid,String productid) throws ServiceException;
        
    KwlReturnObject getConsignmentRequestApproverList(String ruleid) throws ServiceException;
    
    public void updateOpenStatusFlagInDOForSR(String id,String salesreturnId,boolean isconsignment,boolean soReopen) throws ServiceException;
    public void updateOpenStatusFlagInDOForSI(String id) throws ServiceException;
    public void updateOpenStatusFlagInGRForPR(String linkNumbers, String companyid,String purchasereturnId) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException;
    public void updateOpenStatusFlagInVIForPR(String linkNumbers, String companyid) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException;
    
    public KwlReturnObject getConsignmentSalesQAReport(Company company, Date fromDate, Date toDate, String statusType,String customerId, String searchString, Paging paging) throws ServiceException;
    
    public KwlReturnObject getStockRequestOnLoanReport(Company company, Date fromDate, Date toDate, String documenttype,String customerId, String searchString, Paging paging) throws ServiceException;
    
    public boolean updatePurchasereturnAmount(PurchaseReturn purchasereturn, JSONObject json) throws ServiceException;
    
    public boolean updateSalesreturnAmount(SalesReturn salesreturn, JSONObject json) throws ServiceException;
    
    public JSONArray getPurchaseInvoiceJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid,int type);
    
    public JSONArray getGoodsReceiptJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid,int type);
    
    public JSONArray getDebitNoteJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid,int type);
    
    public void addLinkingInformationCreatingPRwithDN(PurchaseReturn purchasereturn, List debitnotelist) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException;
    
    public void addLinkingInformationCreatingSRwithCN(SalesReturn purchasereturn, List debitnotelist) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException;
    
    public JSONArray getSalesInvoiceJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid, int type);
    
    public JSONArray getDeliveryOrderJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid, int type);
    
    public JSONArray getCreditNoteJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid, int type);
    
    public JSONArray getPaymentJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid, int type);
    
    public JSONObject saveSalesReturn(JSONObject paramJobj) throws JSONException, SessionExpiredException;
    
    public JSONObject updateSalesReturn(JSONObject paramJobj) throws JSONException, SessionExpiredException,ServiceException;
    
    public void deleteEntryInTemp(Map deleteparam) ;
    
    public List saveSalesReturnJson(JSONObject paramJobj) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException ;
    
    public List saveSalesReturnRows(JSONObject paramJobj, SalesReturn salesReturn, String companyid, JournalEntry je, Set<JournalEntryDetail> inventoryJEDetails, String inventoryJEid) throws ServiceException, AccountingException, SessionExpiredException, ParseException, UnsupportedEncodingException;
    
    public JSONObject validateToedit(String formRecord, String billid, boolean isConsignment,Company company);
    
    public JSONObject getSalesReturnSummaryReport(JSONObject paramJobj) throws ServiceException;
    
    public List mapSalesPurcahseReturnTerms(String InvoiceTerms, String ID, String userid, boolean isSR) throws ServiceException;

}
