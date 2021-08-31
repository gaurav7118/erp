/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.salesorder;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.*;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.MessagingException;
import javax.script.ScriptException;

/**
 *
 * @author krawler
 */
public interface accSalesOrderService {
    
    /**
     * Create JSON from SalesOrder Object for Avalara integration
     * Used only in Avalara integration
     * @param requestJobj
     * @param salesOrder
     * @param salesOrderDate
     * @param companyid
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws AccountingException
     * @throws SessionExpiredException 
     */
    public JSONObject createJsonFromSalesOrderObject(JSONObject requestJobj, SalesOrder salesOrder, Date salesOrderDate, String companyid) throws ServiceException, JSONException, AccountingException, SessionExpiredException;
    
    public JSONObject saveSalesOrderJSON(JSONObject paramJobj) throws SessionExpiredException, ServiceException, JSONException;

    public JSONObject saveSalesOrderLinkingJSON(JSONObject paramJobj) throws SessionExpiredException, ServiceException, JSONException;

    public KwlReturnObject assignStockToPendingConsignmentRequests(JSONObject paramJobj, Company company, User user) throws ServiceException;

    public Set<AssetDetails> saveAssetDetails(JSONObject paramJobj, String productId, String assetDetails) throws SessionExpiredException, AccountingException, UnsupportedEncodingException;

    /**
     * @param mailParameters(String companyid, String ruleId, String prNumber, String fromName, boolean hasApprover, int moduleid, String createdby, String PAGE_URL)
     * @throws ServiceException 
     */
    public void sendMailToApprover(Map<String, Object> mailParameters) throws ServiceException;

    public void updateOpenStatusFlagForSO(String linkNumbers) throws ServiceException;

    public List<String> approveSalesOrder(SalesOrder soObj, HashMap<String, Object> soApproveMap, boolean isMailApplicable) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException, JSONException;

    public List mapInvoiceTerms(String InvoiceTerms, String id, String userid, boolean isQuotation) throws ServiceException;

    public void sendConsignmentApprovalEmails(JSONObject paramJobj, User sender, SalesOrder so, String billno, boolean isApproved, boolean isEdit) throws ServiceException, JSONException;
    
    public JSONObject deleteSalesOrdersPermanentJson(JSONObject paramJobj) throws JSONException;
    
    //delete single sales order permanently
    public String deleteSalesOrderPermanent (String linkedTransactions, JSONObject jobj, String companyid, boolean isLeaseFixedAsset, JSONObject paramJobj, String modulename) throws JSONException, ServiceException, AccountingException;
    
    public JSONObject deleteSalesOrdersJSON(JSONObject paramJobj) throws JSONException;
    
    //delete single sales order
    public String deleteSalesOrder(JSONObject jobj, String linkedTransactions, String companyid, JSONObject paramJobj, String modulename, boolean isLeaseFixedAsset) throws ServiceException, JSONException, AccountingException;
    
    public JSONObject saveQuotationJSON(JSONObject paramJobj) throws SessionExpiredException, ServiceException, JSONException;
    
    public List<String> approveCustomerQuotation(Quotation doObj, HashMap<String, Object> qApproveMap, boolean isMailApplicable) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException, JSONException;
    
    public JSONObject importCustomerQuotationRecordsForCSV(HashMap<String, Object> request, JSONObject jobj, HashMap<String, Object> globalParams) throws AccountingException, IOException, SessionExpiredException, JSONException;
    
    public void saveSONewBatch(String batchJSON, String productId, JSONObject paramJobj, String documentId) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException;
    
    public HashMap getCurrencyMap(boolean isCurrencyCode) throws ServiceException;
    
    public String createCSVrecord(Object[] listArray);
    
    public String getTermIDByName(String termName, String companyID) throws AccountingException;
    
    public String getCurrencyId(String currencyName, HashMap currencyMap);
    
    public Product getProductByProductID(String productID, String companyID) throws AccountingException;
    
    public UnitOfMeasure getUOMByName(String productUOMName, String companyID) throws AccountingException;
    
    public void setValuesForAuditTrialForRecurringSO(RepeatedSalesOrder rSalesOrder,HashMap<String, Object> oldsoMap,HashMap<String, Object> newAuditKey) throws ServiceException;
    
    public String getCostCenterIDByName(String costCenterName, String companyID) throws AccountingException;
    
    public String getSalesPersonIDByName(String salesPersonName, String companyID) throws AccountingException ;
    
    public Customer getCustomerByCode(String customerCode, String companyID) throws AccountingException;
            
    public double getExchangeRateForTransaction(JSONObject requestJobj, Date billDate, String currencyID) throws JSONException, ServiceException;
    /*
    Sales side Global and Line Level Custom field method
    */
    public JSONArray createLineLevelCustomFieldArrayForImport(JSONObject requestJobj, JSONArray jSONArray, String[] recarr, DateFormat df, int moduleID) throws JSONException, ParseException,AccountingException;
    
    public JSONArray createGlobalCustomFieldArrayForImport(JSONObject requestJobj, JSONArray jSONArray, String[] recarr, DateFormat df, int moduleID) throws JSONException, ParseException,AccountingException;
    
    public String isValidCustomFieldData(JSONArray jSONArray);
    
    public void saveImportLog(JSONObject requestJobj, String msg, int total, int failed, int moduleID);
    
    public Date getDueDateFromTermAndBillDate(String termID, Date billDate) throws ServiceException;
    
    public JSONObject importSalesOrderJSON(JSONObject paramJobj);
    
    public String deleteQuotation (JSONObject jobj, JSONObject requestJobj, String companyid, String linkedQuotaions) throws JSONException, ServiceException, AccountingException;
    
    public String deleteQuotationPermanent(JSONObject jobj, String companyid, String linkedTransaction, JSONObject requestJobj) throws ServiceException, JSONException, AccountingException;
    
    public KwlReturnObject saveDocuments(JSONObject jsonObj) throws ServiceException;
    
    public JSONObject deleteSalesOrdersPermanent(JSONObject paramJobj) throws JSONException;
    
    public JSONObject deleteSalesOrdersTemporary(JSONObject paramJobj) throws JSONException ;
    
    public void sendApprovalMailForCQIfAllowedFromSystemPreferences(HashMap emailMap) throws ServiceException;

    /**
     * Below method is executed only when Avalara Integration is enabled
     * and saves the tax details in database table TransactionDetailAvalaraTaxMapping
     * @param salesOrderJobj
     * @param repeatedSO
     * @param salesOrderNumber
     * @param companyid
     * @param msg
     * @return
     * @throws JSONException 
     */
    public JSONObject saveTaxToAvalara(JSONObject salesOrderJobj, SalesOrder repeatedSO, String salesOrderNumber, String companyid, String msg)throws JSONException;
   
    public Tax getGSTByCode(String taxCode, String companyID);
}
