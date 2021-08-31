/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.hql.accounting.invoice.service;

import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.exception.SeqFormatException;
import com.krawler.inventory.model.approval.consignment.Consignment;
import com.krawler.inventory.model.ist.InterStoreTransferRequest;
import com.krawler.inventory.model.stockmovement.StockMovement;
import com.krawler.spring.accounting.product.TransactionBatch;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.mail.MessagingException;
import javax.script.ScriptException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public interface AccInvoiceModuleService {

    public JSONObject saveInvoice(HttpServletRequest request, HttpServletResponse response);
    
    public Map<String,Object> manipulateRowDetails(Map<String, Object> rowDetailMap, Map<String, List<Object>> batchSerialMap, Map<String, List<JSONObject>> batchMap, JSONArray batchDetailArr, StringBuilder failedRecords, StringBuilder singleInvociceFailedRecords, double totalBatchQty, boolean isRecordFailed, JSONArray rows,boolean isGenerateDeliveryOrder);
     
    
    public JSONObject deleteInvoice(HttpServletRequest request, HttpServletResponse response);
    
    public double getProductPrice(HttpServletRequest request, String rowid) throws SessionExpiredException, ServiceException, JSONException;
    
    public String getDeliveryReturnStatus(DeliveryOrder so,String salesreturnId) throws ServiceException;
    
    public void saveNewSRBatch(String batchJSON, Inventory inventory, HttpServletRequest request, SalesReturnDetail salesReturnDetail, List<StockMovement> stockMovementsList) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException;

     public void saveSRBatch(String batchJSON, Inventory inventory, JSONObject paramJobj, SalesReturnDetail salesReturnDetail, List<StockMovement> stockMovementsList,List<Consignment> ConsignmentList,String rowId) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException;
     
    public void saveConsignSRBatch(String batchJSON, Inventory inventory, JSONObject paramJobj, SalesReturnDetail salesReturnDetail) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException;
    
    public void saveNewConsignSRBatch(String batchJSON, Inventory inventory, HttpServletRequest request, SalesReturnDetail salesReturnDetail,List<Consignment> ConsignmentList, List<StockMovement> stockMovementsListForConsignment) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException;
    
    public Set<AssetDetails> saveAssetDetails(JSONObject paramJobj, String productId, String assetDetails, int assetSoldFlag, boolean isUsedFlag, boolean isFromInvoice, boolean isLeaseFixedAsset, boolean isFromSalesReturn, boolean isFixedAsset, boolean isFromPurchaseReturn, double profitLossAmt) throws SessionExpiredException, AccountingException, JSONException,UnsupportedEncodingException;
    
    public Set<AssetInvoiceDetailMapping> saveAssetInvoiceDetailMapping(String invoiceDetailId, Set<AssetDetails> assetDetailsSet, String companyId, int moduleId) throws AccountingException;
    
    public JSONObject saveInvoiceFromLMS(HttpServletRequest request, HttpServletResponse response);
    
    public Invoice saveInvoiceFromLMS(HttpServletRequest request, JSONObject dataMap1, int counter) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException;
    
    public void getInvoiceFromLMS(HashMap<String, Object> requestParams) throws SessionExpiredException,ServiceException;
    
    public void saveDONewBatch(String batchJSON, Inventory inventory, HttpServletRequest request, DeliveryOrderDetail deliveryOrderDetail, List<StockMovement> stockMovementsList,boolean isLockedinSo,boolean isbatchlockedinSO,boolean isSeriallockedinSO,String replacebatchdetails,List<InterStoreTransferRequest> interStoreTransferList)throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException , AccountingException;
    
    public void saveConsignmentNewBatch(String batchJSON, Inventory inventory, HttpServletRequest request, String documentId) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException , AccountingException;
    
    public Set<WastageDetails> saveWastageDetails(JSONObject paramJobj, String deliveryOrderDetailID, String wastageDetails) throws ServiceException,JSONException;
    
    public DeliveryOrder saveDeliveryOrder(HttpServletRequest request, String invoiceid) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException;
    
    public double getValuationForDOAndSR(HashMap<String, Object> requestMap, Map<String, List<TransactionBatch>> priceValuationMap, Map<String, Double> batchQuantityMap);
    
    /**
     * Description: This method is used to save Delivery Order
     * @param paramJobj
     * @return 
     */
    public List saveDeliveryOrder(JSONObject paramJobj,String invoiceid) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException ;
    
    public JSONObject saveDeliveryOrderJSON(JSONObject paramJobj);
    
    public JSONObject importDeliveryOrdersJSON(JSONObject paramJobj);
    
    public String checkSOUsedFlag(JSONObject paramJobj)throws ServiceException;
    
    public List mapInvoiceTerms(Map<String, Object> requestParams) throws ServiceException;
    
    public List mapInvoiceTerms(String InvoiceTerms, String ID, String userid, boolean isDO) throws ServiceException;
    
    public void updateDeliveryPlannerEntry(Map<String, Object> requestParams) throws ServiceException;
    
    public void createCRMJsonArrayForUpdatingCloseStatus(HashSet<String> productReplacementIds, JSONArray crmJsonArray, String companyid) throws JSONException,ServiceException;
    
    public String getBatchSerialIDs(String batchJSON, Product product, Map<String, Double> batchQuantityMap);
    public String getBatchSerialIDsForAutofillBatchSerial(String batchJSON, Product product, Map<String, Double> batchQuantityMap);
    
    public double getProductPriceJson(JSONObject jobj, String rowid) throws ServiceException ;
    
    public void saveDONewBatchJson(String batchJSON, Inventory inventory, JSONObject paramJobj, DeliveryOrderDetail deliveryOrderDetail, List<StockMovement> stockMovementsList,boolean isLockedinSo,boolean isbatchlockedinSO,boolean isSeriallockedinSO,String replacebatchdetails) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException , AccountingException;
    
    public JSONObject saveCustomerInvoiceJson(JSONObject paramJobj) throws ServiceException, JSONException ;
    
    public Set<DOContractMapping> getDOContractMappings(Map<String, Object> requestParams, DeliveryOrder deliveryOrder) throws ServiceException, AccountingException;
    
    public Set<DOContractMapping> getDOContractMappingsFromInvoice(Map<String, Object> requestParams, DeliveryOrder deliveryOrder) throws ServiceException; 
    
    public boolean deleteExistingAutoBuildAssemblyEntriesOfDO(JSONObject paramJobj,String doid) throws ServiceException, SessionExpiredException;
    
    public String getSalesOrderStatusForDO(SalesOrder so, boolean isConsignment, ExtraCompanyPreferences extraCompanyPreferences, String doid) throws ServiceException;
    
    public String getInvoiceStatusForDO(Invoice iv) throws ServiceException;
    
    /**
     * ERP-34156
     * @desc checks if pickpackDO created
     * @param requestParams
     * @return true (for Present) and false (for Not Present)
     * @throws ServiceException
     * @throws JSONException 
     */
    public JSONObject isPickPackShipDOPresent(JSONObject requestParams) throws ServiceException, JSONException;
    
    public void makeProductReplacementIdsSetAndupdateProductReplacementDetails(ProductReplacementDetail productReplacementDetail, HashSet<String> productReplacementIds, String companyid, double dquantity) throws ServiceException ;
    
    public void sendRequestToCRMForUpdatingProductReplacementStatus(JSONObject paramJobj, JSONArray crmArray) throws JSONException, SessionExpiredException, ServiceException ;
        
    public JSONObject deleteInvoiceJson(JSONObject paramJObj) throws ServiceException, JSONException;
    
    public List<String> approveInvoice(Invoice invoice, HashMap<String, Object> invApproveMap, boolean isMailApplicable) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException, JSONException;
    
    /**
     * @param mailParameters(String companyid, String ruleId, String prNumber, String fromName, boolean hasApprover, int moduleid, boolean iscash, String createdby)
     * @throws ServiceException 
     */
    public void sendMailToApprover(Map<String, Object> mailParameters) throws ServiceException;
    
    public void saveBankReconsilation(Map<String, Object> requestParams, Map<String, Object> globalParams) throws ServiceException, JSONException, UnsupportedEncodingException;
    
    public void deleteBankReconcilation(Map<String, Object> requestParams) throws ServiceException;
    
    public void updateOpenStatusFlagForDOInSI(String linkNumbers) throws ServiceException;
     
    public String getSalesOrderStatus(SalesOrder so) throws ServiceException;
    
    public void updateOpenStatusFlagForSI(String linkNumbers) throws ServiceException;
    
    public HashMap mapExciseDetails(String invid, JSONObject temp, JSONObject paramJobj) throws ServiceException ;
    
    public JSONObject saveInvoice(JSONObject paramJobj) ;
    
    /**
     * Below method is executed only when Avalara Integration is enabled
     * This code voids previously committed taxes on Avalara in case of edit, then it commits new taxes to Avalara, 
     * and saves the tax details in database table TransactionDetailAvalaraTaxMapping
     * Finally the code updates the taxCommittedOnAvalara flag in Invoice table, which indicates whether or not taxes were successfully committed
     * @param paramJobj
     * @param invoice
     * @param invoiceId
     * @param invoiceNumber
     * @param companyid
     * @param isEdit
     * @param msg
     * @param isCommit - flag to indicate whether taxes are to be committed or not
     * @return 
     * @throws com.krawler.utils.json.base.JSONException 
     */
     public JSONObject commitTaxToAvalaraAndSave(JSONObject paramJobj, Invoice invoice, String invoiceId, String invoiceNumber, String companyid, boolean isEdit, String msg, boolean isCommit) throws JSONException;
     
     /**
     * Create JSON from Invoice Object for Avalara integration
     * Used only in Avalara integration
     * @param requestJobj
     * @param invoice
     * @param invoiceId
     * @param invoiceNumber
     * @param invoiceDate
     * @param companyid
     * @param isCommit
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws AccountingException
     * @throws SessionExpiredException 
     */
     public JSONObject createJsonFromInvoiceObject(JSONObject requestJobj, Invoice invoice, String invoiceId, String invoiceNumber, Date invoiceDate, String companyid, boolean isCommit) throws ServiceException, JSONException, AccountingException, SessionExpiredException;
    
//    public JSONObject importInvoicesJSON(JSONObject paramJobj);
    
    public JSONObject getBatchRemainingQuantity(JSONObject paramJobj);
    
    public JSONObject getBatchRemainingQuantityforAssembly(JSONObject paramJobj);
    
    public JSONObject getBatchRemainingQuantityForImport(JSONObject paramJobj);
   
    public double getNewBatchRemainingQuantity(String locationid, String warehouseid, String companyId, String productid, String purchasebatchid, String transType, boolean isEdit, String documentid, boolean linkflag) throws ServiceException ;
    
    public void postJEForFreeGiftDo(JSONObject parameters, DeliveryOrder DO) throws AccountingException;
    
    public void revertGIROFileGenerationStatus(HashMap<String,Object> map);
    
    public JSONObject updateLockQuantityCalculation(HashMap<String, Object> requestParams)  throws ServiceException;
    
    public void saveCustomFieldsForInvoice(HashMap<String, Object> requestParams) throws ServiceException , AccountingException;
    
    public JSONArray getTermDetailsForDeliveryOrder(String id) throws ServiceException;

    
    public JSONObject saveStockOutfromDO(Map requestParams) throws ServiceException, JSONException, ParseException, SeqFormatException, AccountingException;
    
    public void setDOStatus(Map<String, Object> map) throws ServiceException;   
 
    public List deleteDeliveryOrder(JSONObject jobj, String linkedTransaction, CompanyAccountPreferences preferences, ExtraCompanyPreferences extraCompanyPreferences, StringBuffer productIds, JSONObject paramJobj, HashSet<String> productReplacementIds) throws ServiceException, AccountingException, JSONException, SessionExpiredException;
    
    public List deleteDeliveryOrderPermanent(JSONObject jobj, String linkedTransaction, CompanyAccountPreferences preferences, ExtraCompanyPreferences extraCompanyPreferences, String status, StringBuffer productIds, JSONObject paramJobj) throws ServiceException, AccountingException, JSONException, SessionExpiredException;
    
    public void makeProductReplacementIdsSetForUpdatingStatusOFProductReplacementRequest(HashSet<String> productReplacementIds, String doId, String companyid) throws ServiceException, JSONException;
    
    public JSONObject deleteDeliveryOrdersJSON(JSONObject paramJobj) ;
    
    public JSONObject  deleteTemporaryDeliveryOrders(JSONObject paramJobj) ;
    
    public List deleteDeliveryOrdersPermanent(JSONObject paramJobj) throws SessionExpiredException, AccountingException, ServiceException, JSONException ;
    
    public JournalEntry createRoundingOffJE(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, AccountingException;
    
    public double getInvoiceAmountUtilizedInRPandCN(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    
    public void newStockMovementDeliveryOrder(DeliveryOrderDetail doDetail,List<StockMovement> stockMovementsList) throws ServiceException;
    
    public JSONArray getSalesReportMasterDataJson(JSONObject requestParams, List<Object[]> list, JSONArray jArr, HashMap<String, Object> params) throws ServiceException, JSONException , SessionExpiredException,ParseException;
    
    public JSONArray getSalesCommissionProductDetailReportJson(JSONObject requestParams) throws ServiceException, JSONException , SessionExpiredException,ParseException;

    public JSONObject isPackingStoreUsedBefore(JSONObject requestParams) throws ServiceException, JSONException;
    /**
     * Desc: This method is used to check if select store is used in transaction or not
     * @param requestParams
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public boolean isStoreUsedInTransaction(JSONObject requestParams) throws ServiceException, JSONException;
    /**
     * Get Additional memo details in JSON 
     * @param requestParams
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public JSONObject getAdditionalMemo(JSONObject requestParams) throws ServiceException, JSONException;

    public void updateLineLevelTaxUnitPriceOnIncludeGST(JSONArray rows, JSONArray invoiceTermArrayDetails, Map<String, Object> invoiceMap);

    public double getGlobalTaxAmount(JSONObject paramJobj, String companyID, double amountAfterRemovingDiscount);

    public JSONArray getInvoiceTermDetailsArray(Map<String, Object> invoiceTermDetailMap, LinkedHashMap<String, InvoiceTermsSales> salesTermMap);

    public boolean isAllLineLevelInformationIsEmpty(HashMap<String, Integer> columnConfig, String[] recarr);

    public Customer getCustomerByCode(String customerCode, String companyID);
    
    public KwlReturnObject saveDocuments(JSONObject jsonObj) throws ServiceException;
}
