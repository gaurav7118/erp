/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.goodsreceipt.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.exception.SeqFormatException;
import com.krawler.inventory.model.stockmovement.StockMovement;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.script.ScriptException;
import org.apache.velocity.exception.ParseErrorException;

/**
 *
 * @author krawler
 */
public interface accGoodsReceiptModuleService {

    public List saveGoodsReceiptOrder(JSONObject paramJobj, String invoiceid, Map<String, String> map) throws SessionExpiredException, ServiceException, AccountingException, UnsupportedEncodingException,SeqFormatException;
    
    public JSONObject saveGoodsReceiptOrder(JSONObject paramJobj) throws AccountingException;
    
    public JSONObject importGoodsReceiptOrdersJSON(JSONObject paramJobj);
    
    public void updatePOisOpenAndLinkingWithGR(String linking, String grorderId) throws ServiceException;

    public void updatePIisOpenAndLinkingWithGR(String linking) throws ServiceException;

    public List<String> approveGRO(GoodsReceiptOrder groObj, HashMap<String, Object> grApproveMap, boolean isMailApplicable) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException, JSONException;

    public HashMap getCurrencyMap(boolean isCurrencyCode) throws ServiceException;

    public String getCurrencyId(String currencyName, HashMap currencyMap);

    public String createCSVrecord(Object[] listArray);

    public Vendor getVendorByCode(String vendorCode, String companyID) throws AccountingException;

    public Vendor getVendorByName(String vendorName, String companyID) throws AccountingException;
    
//    public JSONObject importPurchaseInvoiceJSON(JSONObject paramJobj);
    
    public JSONObject importExpenseInvoiceJSON(JSONObject paramJobj);

//    public JSONObject importPurchaseInvoiceRecordsForCSV(JSONObject requestJobj) throws AccountingException, IOException, JSONException;
    
    public Map<String,Object> manipulateRowDetails(Map<String, Object> rowDetailMap, Map<String, List<Object>> batchSerialMap, Map<String, List<JSONObject>> batchMap, JSONArray batchDetailArr, StringBuilder failedRecords, StringBuilder singleInvociceFailedRecords, double totalBatchQty, boolean isRecordFailed, JSONArray rows);

    public void deleteEntryInTemp(Map deleteparam);

    public void deleteEditedGoodsReceiptJE(String oldjeid, String companyid) throws ServiceException, AccountingException, SessionExpiredException;

    public void deleteEditedGoodsReceiptDiscount(ArrayList discArr, String companyid) throws ServiceException, AccountingException, SessionExpiredException;

    public void savemachineAsset(Map<String, String> map, String company) throws AccountingException;
    
    public JSONObject getLandingCostItemReport(HashMap<String, Object> map, String company) throws ServiceException;
    
    public JSONObject createLandingCostItemConfig(HashMap<String, Object> map, String company) throws ServiceException;
    
    public Map DeleteStockOutFromGRN(Map<String,Object> reqMap) throws ServiceException;
    
    public JournalEntry createRoundingOffJE(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, AccountingException;
    
    public double getGrAmountUtilizedInMPandDN(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, AccountingException;
    
    public List mapInvoiceTerms(String InvoiceTerms, String ID, String userid, boolean isGR) throws ServiceException;
    
    public JSONObject deleteGoodsReceiptOrdersPermanentJSON(JSONObject paramJObj);
    
    public void getGoodsReceiptCustomDataForPayment(HashMap<String, Object> request, JSONObject obj, GoodsReceipt goodsReceipt, JournalEntry je) throws ServiceException;
    
    public void saveGoodsReceiptTdsJEMapping(HashMap<String,Object> paramJObj);
    
    public JSONObject getTotalInvoiceAmountURDVendorPurchaseInvoice(JSONObject pramsObj) throws ServiceException, JSONException;
    
    public JSONObject modifyURDVendorRCMPurchaseInvoiceJEDetails(JSONObject pramsObj) throws ServiceException, JSONException;
    
    public void sendApprovalMailIfAllowedFromSystemPreferences(HashMap emailMap) throws ServiceException;
    
    public void sendApprovalMailForGRIfAllowedFromSystemPreferences(HashMap emailMap) throws ServiceException;
    
    public List saveApprovalHistory(HashMap emailMap) throws ServiceException;
    
    public List approveRelevantDocumentAttachedToVendorInvoice(HashMap approveJeMap) throws ServiceException , JSONException , AccountingException ,SessionExpiredException , ParseException;
    
    public void newStockMovementGROrder(GoodsReceiptOrderDetails goodsReceiptOrderDetails,List<StockMovement> stockMovementsList) throws ServiceException;
    
    public void updateInvTablesAfterPendingApproval(GoodsReceiptOrder grorder , boolean isbeforePendingApproval, boolean restrictDuplicateBatch) throws ServiceException , AccountingException;
    
    public JSONObject saveGoodsReceipt(JSONObject paramJobj) ;
    
    public List saveGoodsReceipt(JSONObject paramJobj, Map<String,String> map) throws SessionExpiredException, ServiceException, AccountingException, UnsupportedEncodingException;
    
    public void deleteJEDetailsCustomData(String jeid) throws ServiceException;

    public Tax getGSTByCode(String taxCode, String companyID);
    
    public JSONObject  isLandedCostWithTermTransactionsPresent(JSONObject params) throws ServiceException;
    
    public JSONObject getITCInformationForProducts(JSONObject paramJobj) throws ServiceException, JSONException;

}
