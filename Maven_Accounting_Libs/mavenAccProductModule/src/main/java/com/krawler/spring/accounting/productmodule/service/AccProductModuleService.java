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
package com.krawler.spring.accounting.productmodule.service;

import com.krawler.common.admin.InventoryLocation;
import com.krawler.common.admin.InventoryWarehouse;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.frequency.Frequency;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.store.Store;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public interface AccProductModuleService {

    public JSONObject getIndividualProductPrice(HttpServletRequest request, HttpServletResponse response);
    
    public JSONObject getAssemblyItemsJson(JSONObject paramJobj, List list,int levelCount) throws ServiceException, SessionExpiredException;
    
    public AssetMaintenanceSchedulerObject saveAssetMaintenanceSchedule(HttpServletRequest request, String contractId) throws SessionExpiredException, ServiceException, AccountingException;
    
    public  List isProductUsedintransction(String productid,String companyId,HttpServletRequest request,boolean isProductAndServices) throws SessionExpiredException, AccountingException, ServiceException ;
    
    public  boolean isProductUsedinBatchSerialtransction(String productid,String companyId) throws SessionExpiredException, AccountingException, ServiceException ;
    
    public  List isProductUsedintransction(String productid,String companyId,HashMap<String,Object> hashMap) throws SessionExpiredException, AccountingException, ServiceException ;
    
    public String createCSVrecord(Object[] listArray);
    
    public HashMap getCurrencyMap(boolean isCurrencyCode) throws ServiceException ;
    
    public Map<Integer, Frequency> getCCFrequencyMap() throws ServiceException;
    
    public Producttype getProductTypeByName(String productTypeName) throws AccountingException ;
    
    public JSONObject getAssembyProductDetails(JSONObject paramJobj) throws ServiceException ;
    
    public Map getBatchSerialDetailsOfAssembyProduct(JSONObject paramJobj) throws ServiceException;
      
    public UnitOfMeasure getUOMByName(String productUOMName, String companyID) throws AccountingException ;
    
    public Product getProductByProductID(String productID, String companyID) throws AccountingException ;
    
    public Account getAccountByName(String accountName, String companyID) throws AccountingException ;
    
    public Vendor getVendorByName(String vendorName, String companyID) throws AccountingException ;

    public InventoryLocation getInventoryLocationByName(String inventoryLocation, String companyID) throws AccountingException ;
    
    /**
     * Description:This method is used to get the Location Object by passing location id.
     * @param String locationid, String companyid
     * @return Location class object
     * @throws ServiceException
     */
    public Location getLocationByID(String locID, String companyID) throws AccountingException ;
    
    public InventoryWarehouse getInventoryWarehouseByName(String inventoryWarehouse, String companyID) throws AccountingException;
    
    public String getCurrencyId(String currencyName, HashMap currencyMap);
    
    public void maintainCustomFieldHistoryForProduct(HttpServletRequest request, HashMap<String, Object> customrequestParams) ;
    
    public void createFailureFiles(String filename, StringBuilder failedRecords, String ext);
    
    public String getCustomerIDByCode(String customerCode, String companyID) throws AccountingException;
    
    public String getVendorIDByCode(String vendorCode, String companyID) throws AccountingException ;
        
    public Product getProductByProductName(String companyid, String productTypeID) throws AccountingException ;
    
    public String getProductCategoryIDByName(String productCategoryName, String companyID) throws AccountingException ;
    
    public JSONObject saveProductCategoryMapping(JSONObject paramJobj) throws ServiceException,SessionExpiredException;
    
    public Producttype getProductTypeByProductID(String productTypeID) throws AccountingException ;
    
    public Map<String, Object> exportStockLedger(JSONArray finalJArr, DateFormat df, String companyid) throws JSONException;

//    public void createFailureXlsFiles(String filename, List failureArr, String ext, List failureColumnArr);
    
    public JSONObject getDefaultColumns(HashMap<String, Object> params);
    
    public KwlReturnObject saveProductTerms(HashMap<String, Object> productTermMap) throws ServiceException;
    
     public JSONObject getBOMDetailJSON(HttpServletRequest request, List<BOMDetail> list) throws ServiceException, SessionExpiredException;
     
     public JSONObject getQualityControlJSON(List<QualityControl> list) throws ServiceException, SessionExpiredException;
     
     public JSONObject getIndividualProductPrice(JSONObject paramsjobj);
     
     public JSONObject getAssemblyProductSalesPrice(JSONObject paramsjobj, String productid) throws JSONException, ServiceException;
     
     public List getPriceListBandPriceInDocumentCurrencyConvertedFromBaseCurrency(JSONObject paramsjobj, HashMap<String, Object> pricingBandRequestParams, HashMap<String, Object> requestParams, String productid, boolean carryin, String globalCurrencyKey, JSONObject obj, boolean isPriceFromBand, boolean isPriceFromUseDiscount, boolean isPriceBandMappedWithVolDisc, boolean isIncludingGst, Date transactionDate, String discountType, double discountValue, String pricingBandMasterID, String companyid, JSONObject jobj) throws ServiceException, JSONException;
       
     public void getProductBrandDiscountForBand(String productID, String customerID, String companyID, String currencyID, Date transactionDate, JSONObject obj, String pricingBandMasterID) throws ServiceException, JSONException;
     
     public List isProductUsedintransction(String productid, String companyid, JSONObject paramJobj, boolean isProductAndServices) throws SessionExpiredException, AccountingException, ServiceException ;
     
     public JSONArray callProductTermMapJSONArray(HashMap<String, Object> data);
     
     public JSONObject getProducts(JSONObject paramJobj);
     
     /**
      * Description : Get disposal invoice related data from DAO layer
      * @param request
      * @return Map
      * @throws ServiceException 
      */
     public Map<String, Object>  getDisposalInvoiceDetailsFromAssetDetailID(Map<String, Object> request)throws ServiceException;
     
     public JSONArray getProductsJson(JSONObject paramJobj, List list) throws JSONException, ServiceException, SessionExpiredException, AccountingException, ParseException ;
     public JSONObject importPriceListBandPriceRecords(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException;
     
     public JSONObject manipulateBatchDetailsforMobileApps(String batchJSON, String productId, JSONObject paramJobj) throws JSONException, SessionExpiredException, ServiceException;
     
     public Map<String, Object> validateBatchSerialDetail(Map<String, Object> requestMap);
     /**
      * Desc: This function is used to validate and get Batch detail JSON if product having warehouse and location activated
      * @param requestMap
      * @param innerLineLevelDetailSet
      * @return Map
      */
     public Map<String, Object> validateAndGetBatchSerialDetail(Map<String, Object> requestMap,Set<String> innerLineLevelDetailSet);
     
     public JSONObject getProductsByCategory(JSONObject paramJobj);
     
     public JSONArray getProductsByCategoryJson(JSONObject paramJobj, List list) throws JSONException, ServiceException, UnsupportedEncodingException, SessionExpiredException, ParseException;
     
     public JSONArray getProductsByCategoryJsonForExport(JSONObject paramJobj, List list) throws JSONException, ServiceException, UnsupportedEncodingException, SessionExpiredException, ParseException;
     
    /**
    * Method to create or update Item on AvaTax whenever a Product is created or updated in our system
    * Used in Avalara Integration
    * @param product
    * @param companyid
    * @param isEdit
    * @throws JSONException
    * @throws ServiceException
    * @throws AccountingException 
    */
    public void createOrUpdateItemOnAvalaraForProduct(Product product, String companyid, boolean isEdit) throws JSONException, ServiceException, AccountingException;
    
    /**
     * Method to delete Item on AvaTax whenever a Product is delete from our system
     * Used in Avalara Integration
     * @param productid
     * @param companyid
     * @throws JSONException
     * @throws ServiceException
     * @throws AccountingException 
     */
    public void deleteItemOnAvalaraForProduct(String productid, String companyid) throws JSONException, ServiceException, AccountingException;
    
    public JSONObject createProductJsonObject(Product product) throws JSONException, ServiceException;
    
     public JSONObject getIndividualProductDiscount(JSONObject paramsjobj) throws ServiceException;
     
     public JSONObject getProductGSTHistory(Map<String, Object> reqMap) throws ServiceException, JSONException;
     public JSONObject getProductUsedHistory(Map<String, Object> reqMap) throws ServiceException, JSONException;
     public JSONObject saveProductGSTHistoryAuditTrail(JSONObject paramJObj) throws ServiceException, JSONException, SessionExpiredException, ParseException;
     
     public JSONObject getAssembyProductBOMDetails(JSONObject paramJobj) throws ServiceException ;
  
     public JSONObject getMappedVolumeDiscountwithBand(KwlReturnObject result, String volumeDiscountID, String pricingBandMasterID, double qty) throws ServiceException, JSONException;
}
