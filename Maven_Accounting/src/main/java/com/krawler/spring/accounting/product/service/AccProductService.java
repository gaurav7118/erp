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
package com.krawler.spring.accounting.product.service;

import com.krawler.common.admin.NewBatchSerial;
import com.krawler.common.admin.ProductBatch;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Product;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.product.PriceValuationStack;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface AccProductService {

    public JSONObject getProductsJsonForCombo(JSONObject paramsjobj) throws ServiceException, SessionExpiredException;

    public JSONArray getCustomColumnData(String productId, String companyId) throws JSONException, Exception;

    public ProductBatch getBatchDetailsForProduct(String productid) throws ServiceException;

    public void putGoodsReeiptCustomData(JSONObject jSONObject) throws ServiceException, JSONException;

    public void putDeliveryOrderCustomData(JSONObject jSONObject) throws ServiceException, JSONException;

    public void putPurchaseReturnCustomData(JSONObject jSONObject) throws ServiceException, JSONException;

    public void putSalesReturnCustomData(JSONObject jSONObject) throws ServiceException, JSONException;

    public void putProductCustomData(JSONObject jSONObject) throws ServiceException, JSONException;
    
    public void putStockAdjustmentCustomData(JSONObject jSONObject) throws ServiceException, JSONException;
    
    public void putInterStoreTransferCustomData(JSONObject jSONObject) throws ServiceException, JSONException;
    
    public  JSONObject getProductSummary(Map<String, Object> requestMap) throws ServiceException;
    
    public double getOutstandingPoSoProductsCount(List list, boolean isPo, String productid,
            AccountingHandlerDAO accountingHandlerDAOobj, accGoodsReceiptDAO accGoodsReceiptDAOobj, accInvoiceDAO accInvoiceDAOobj) throws JSONException, ServiceException, SessionExpiredException;
    
    public  JSONObject getProductRecipes(HashMap<String, Object> requestMap) throws ServiceException;
    
    public JSONArray getBOMCombo(Map<String, Object> map) throws ServiceException;
    
    public JSONArray getProductBrandDiscountDetailsJson(Map<String, Object> requestMap, List<Object> list) throws JSONException, ServiceException;
    
    public void createColumnModelForProcuctBrandDisocuntDetails(JSONArray jarrColumns, JSONArray jarrRecords, JSONObject paramJobj) throws JSONException, ServiceException;

    public JSONObject syncDataIntoPM(HashMap<String, Object> requestParams) throws ServiceException;
    
    public JSONArray getProductsForProject(Map<String, Object> requestParams) throws ServiceException ,JSONException,SessionExpiredException;
    
    public JSONObject getProductDetailJSON(Product product, Map<String,Object> requestParams) throws ServiceException, JSONException;
    
   public JSONArray getAssemblyProducts(Product product,Map<String,Object> requestParams , JSONArray JArr) throws ServiceException,JSONException,SessionExpiredException;
   
   public JSONObject getBatchRemainingQuantity(Map<String,Object> requestMap);
     
   public JSONObject getBatchRemainingQuantityForMultipleRecords(Map<String ,Object> requestMap);
   
    public JSONArray getInventoryValuationData(HashMap<String, Object> requestParams);
    
    public JSONObject getConsolidationStockReport(JSONObject paramJobj)throws ServiceException, SessionExpiredException;

    public double[] getInventoryValuationDataForFinancialReports(HashMap<String, Object> requestParams);    
    
    public Map<String, PriceValuationStack.Batch> getValuationMapForCostAndSellingPriceReport(JSONObject requestParams);
    
    public double getOutstandingSICount(HashMap<String, Object> requestParams, accInvoiceDAO accInvoiceDAOobj);

    public JSONObject getIndividualProductPrice(JSONObject paramsjobj);
    
    public JSONObject getBlockedQuantityOfProduct(HashMap<String, Object> requestParams) throws ServiceException;

    public double getOutstandingPoSoProductsCount(HashMap<String, Object> requestParams, boolean isPo, String productid,
            AccountingHandlerDAO accountingHandlerDAOobj, accGoodsReceiptDAO accGoodsReceiptDAOobj, accInvoiceDAO accInvoiceDAOobj,
            accSalesOrderDAO accSalesOrderDAOobj, accPurchaseOrderDAO accPurchaseOrderobj, String compareUOMId) throws JSONException, ServiceException, SessionExpiredException;

    public double getPurchaseOrderCountFromProduct(HashMap<String, Object> requestParams, accGoodsReceiptDAO accGoodsReceiptDAOobj, CompanyAccountPreferences pref, double count) throws ServiceException;

    public double getSalesOrderCountFromProduct(HashMap<String, Object> requestParams, accInvoiceDAO accInvoiceDAOobj, CompanyAccountPreferences pref, double count) throws ServiceException;
    
    public JSONObject saveIncidentCase(JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    
    public JSONObject deleteIncident(JSONObject paramJObj) throws JSONException, ServiceException;
    
    public JSONObject getIncidentCase(JSONObject paramJObj) throws JSONException, ServiceException,SessionExpiredException,ParseException;
    
    public JSONObject getIncidentChart(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, ParseException;

    public Map<String,Object> getDataForStockAgeingReport(HashMap<String, Object> requestParams);
    /**
     * description : this method is used to create product json to sync product array to PM
     * 
     * @param lst
     * @return 
     * @throws com.krawler.common.service.ServiceException 
    */
    public JSONArray getProductsJsonToSyncTOPM(List lst)  throws ServiceException;
/**
 * description : this method is used to sync products data to PM
     * @param DataJArr    
     * @param companyid    
     * @return     
     * @throws com.krawler.common.service.ServiceException    
    */
    public JSONObject SyncProductsIntoPM(JSONArray DataJArr, String companyid)  throws ServiceException;
    
     public JSONObject saveQAApprovalDetails(HashMap<String, Object> requestParams) throws ServiceException;
     /**
      * description : This methods check whether there is any SkufieldValue already available 
      * @param requestParams
      * @return jsonObject
      * @throws ServiceException 
      */
    public JSONObject checkAssetIdIsPresent(Map<String, Object > requestParams) throws ServiceException; 
    public boolean isNegativeStockPresent(HashMap<String, Object> requestParams);
    public JSONArray getBuildAssemblyDetailsItemJSON(JSONObject paramJobj, String companyid, String transactionId);
    
    public JSONObject checkAssetIdIsPresent(Map<String, Object > requestParams,NewBatchSerial batchserialOBj) throws ServiceException; 
    
    public JSONObject getIndividualProductDiscount(JSONObject paramsjobj) throws ServiceException;
    
    public boolean migrationPriceList(HashMap<String, Object> requestParams);
    
    public JSONObject getOutstandingPOSOCount(JSONObject paramJobj) throws ServiceException, SessionExpiredException, ParseException, JSONException ;
    
    public List getListOfInventoryQuantitiesOfProduct(HashMap balanceQuantityParams) throws  ServiceException;
    
    public JSONObject getProductsIdNameforCombo(JSONObject paramJObj);
    
    public Map<String, Map> getInventoryValuationDataForFinancialReports(HashMap<String, Object> requestParams, Map<String, Map> stockDateMap);
}
