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
package com.krawler.spring.accounting.product;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.ValuationMethod;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.frequency.Frequency;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface accProductDAO {

    public KwlReturnObject getReportSchema(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getObject(String classpath, String id) throws ServiceException;
    
    public KwlReturnObject getObject(String classpath, int id) throws ServiceException;
    //Product

    public KwlReturnObject addProduct(HashMap<String, Object> productMap) throws ServiceException;
    
    /**
     * Method to save or update mapping of Deskera product and corresponding item on AvaTax
     * @param paramsJobj
     * @return
     * @throws ServiceException 
     */
    public KwlReturnObject saveOrUpdateProductAvalaraIdMapping(JSONObject paramsJobj) throws ServiceException;
    
    /**
     * Method to delete mapping of Deskera product and corresponding item on AvaTax
     * @param paramsJobj
     * @return
     * @throws ServiceException 
     */
    public KwlReturnObject deleteProductAvalaraIdMapping(JSONObject paramsJobj) throws ServiceException;
    
    public KwlReturnObject getProductCategory(String productid) throws ServiceException;

    public KwlReturnObject deleteCyclecountPermanently(String productid, String companyid) throws ServiceException;

    public KwlReturnObject deleteInventoryByProduct(String productid, String companyid) throws ServiceException;

    public KwlReturnObject deleteProPricePermanently(String productid, String companyid) throws ServiceException;

    public KwlReturnObject deleteProductCycleCountPermanently(String productid, String companyid) throws ServiceException;

    public KwlReturnObject deleteProductPermanently(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getProductByID(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getProductByProdID(String productid, String companyid,boolean isFixedAsset) throws ServiceException;

    KwlReturnObject getProductList(Map<String, Object> requestParams) throws ServiceException;
    
    public Integer getSyncableProductsCount(Map<String, Object> requestParams) throws ServiceException;
    
    public List getSyncableProductList(Map<String, Object> requestParams) throws ServiceException;

    KwlReturnObject getNewProductList(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject updateProduct(HashMap<String, Object> productMap) throws ServiceException;

    public KwlReturnObject deleteProduct(String productid) throws ServiceException;

    public KwlReturnObject deleteProductCustomData(String productid) throws ServiceException;

    public KwlReturnObject getProductsIds(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getProducts(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getAssemblyProducts(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getBuildAssemblyDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getBuildAssemblyProducts(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getProductByName(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getProductsForCombo(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getProductsForSelectionGrid(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getUserMappedProducts(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getProductfromAccount(String accountid, String companyid) throws ServiceException;
    
    public KwlReturnObject getAssetGroupfromAccount(String accountid, String companyid) throws ServiceException;
    
    public KwlReturnObject getInvoiceTermfromAccount(String accountid, String companyid) throws ServiceException;

    public KwlReturnObject getProValuation(HashMap requestParam) throws ServiceException;
    
    public KwlReturnObject getStockInOutOpeningCalculation(HashMap requestParam) throws ServiceException;
    
    public KwlReturnObject getAvailableQuantityForSerial(HashMap requestParam) throws ServiceException;
    
    public KwlReturnObject getAvailableStockForSerial(HashMap requestParam) throws ServiceException;
    
    public KwlReturnObject getAvailablequantityBatches(HashMap requestParam) throws ServiceException;
    
    public KwlReturnObject getquantityAndRateOfProductForInventory(HashMap requestParam) throws ServiceException;
    
    public KwlReturnObject getERPProductBatch(String companyId,String product, String store, String location, String row, String rack, String bin, String batchName,double perUnitPrice) throws ServiceException;

    public double getSerialNoStockValuation(String product,String companyId,String serialNames,String batchnames,String store) throws ServiceException;
    
    public KwlReturnObject getStockStatus(HashMap requestParam) throws ServiceException;

    public KwlReturnObject saveProductPackging(Packaging packaging) throws ServiceException;

    public KwlReturnObject getSuggestedReorderProducts(HashMap<String, Object> requestParams) throws ServiceException;

    //Product Types
    public KwlReturnObject saveProductTypes(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject saveIncidentCase(JSONObject dataObj) throws ServiceException,SessionExpiredException,ParseException;
    
    public KwlReturnObject getIncidentCase(JSONObject dataObj) throws ServiceException,SessionExpiredException,ParseException;
    
    public KwlReturnObject getIncidentChart(JSONObject dataObj) throws ServiceException, SessionExpiredException, ParseException;
    
    public JSONObject getamendingPurchaseprice(String Productid,String userid,Date transactionDate,String currency,String uomid,JSONObject obj) throws ServiceException, JSONException, ParseException;
    
    public KwlReturnObject deleteIncidentPermanent(String incidentid) throws ServiceException;
    
    public KwlReturnObject deleteIncidentTemporary(JSONObject jobj) throws ServiceException,JSONException;

    public KwlReturnObject getProductTypes(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteProductType(String typeid) throws ServiceException;

    public KwlReturnObject getProductsByType(HashMap<String, Object> requestParams) throws ServiceException;

    //Product Assembly
    public KwlReturnObject saveProductAssembly(HashMap<String, Object> assemblyMap) throws ServiceException;

    public KwlReturnObject deleteProductAssembly(String parentProductId) throws ServiceException;
    
    //Product Composition
    public KwlReturnObject saveProductComposition(HashMap<String, Object> productCompositionMap) throws ServiceException;

    public KwlReturnObject getProductCompositionDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteProductComposition(String productid) throws ServiceException;

    public KwlReturnObject deleteProductBuildDetails(String parentProductId, String companyid) throws ServiceException;

    public KwlReturnObject deleteProductbBuild(String parentProductId, String companyid) throws ServiceException;
   
    public KwlReturnObject deleteNewProductBatch(String parentProductId, String companyid) throws ServiceException;

    public KwlReturnObject deleteLocationDocumentMap(String parentProductId, String companyid) throws ServiceException;

    public KwlReturnObject deleteProductBuildDetailsByID(String parentProductId, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteProductBuildBatchDetailsByID(String documentId,boolean isAssemblyProduct, String companyid) throws ServiceException;

    public KwlReturnObject deleteProductbBuildByID(String parentProductId, String companyid) throws ServiceException;

    public List getAssembyProducts(String buildProductid, String compnyid) throws ServiceException;

    public KwlReturnObject deleteSubProductFromAssembly(String subProductId) throws ServiceException;

    public KwlReturnObject updateRecycleQuantityofSubProductFromAssembly(String subProductId, double recycleQuantity) throws ServiceException,AccountingException;
    
    public KwlReturnObject updateProductBuild(ProductBuild productBuild) throws ServiceException;

    public KwlReturnObject updateProductBuildDetails(ProductBuildDetails buildDetails) throws ServiceException;

    public Product updateAssemblyInventoryForOther(HashMap<String, Object> requestParams) throws ServiceException;
    public ProductBuild updateAssemblyInventory(HashMap<String, Object> requestParams) throws ServiceException;
    public ProductBuildDetails updateAssemblyBuildDetails(HashMap<String, Object> requestParams, String jeid, HashSet jeDetails, List returnList) throws ServiceException, AccountingException;

    public KwlReturnObject searchRefNo(String refNO, String companyId, boolean isBuild) throws ServiceException;
    
    public KwlReturnObject getAssemblyItems(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getDocumentBySerialId(HashMap<String, Object> requestParams) throws ServiceException;

    // maintan product custom fields history
    public KwlReturnObject maintainCustomFieldHistoryForProduct(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteCustomFieldHistoryForProduct(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getProductCustomFieldValue(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCustomFieldHistoryForProduct(HashMap<String, Object> requestParams) throws ServiceException;

    //PriceList
    public KwlReturnObject addPriceList(HashMap<String, Object> priceMap) throws ServiceException;

    public KwlReturnObject saveProducsPriceRule(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getProducsPriceRule(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteProductPriceRule(String itemid) throws ServiceException;

    public KwlReturnObject getProductByPrice(double lowerlimit, double upperlimit, String companyid, String catagoryid, int priceType, int ruleType, String catagoryIds, String currencyid) throws ServiceException;

    public KwlReturnObject updatePriceList(HashMap<String, Object> priceMap) throws ServiceException;

    public KwlReturnObject getAllProductsMaxAppliedDatePriceDetails(String companyid, HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getProductPrice(String productid, boolean isPurchase, Date transactiondate, String affecteduser, String forCurrency) throws ServiceException;
    
    public KwlReturnObject getProductPrice(String productid, boolean isPurchase, Date transactiondate, String affecteduser, String forCurrency, String uomid, boolean excludeInitialPrice) throws ServiceException;
    
    public boolean checkIfPriceIsMappedToUOMInPriceList(String productid, boolean isPurchase, Date transactiondate, String affecteduser, String forCurrency, String uomid) throws ServiceException;
    
    public KwlReturnObject getSerialNoByDocumentid(String documentid,int transactiontype) throws ServiceException;
    public KwlReturnObject getSerialNoByDocumentidAndSerialNames(String documentid,int transactiontype,String serialName) throws ServiceException;//serialnames comma seprated
    public KwlReturnObject getBatchesByDocumentid(String documentid,int transactiontype) throws ServiceException;
    public KwlReturnObject getBatchesByDocumentidAndBatchNames(String documentid,int transactiontype,String batchName) throws ServiceException;

    public KwlReturnObject getInitialPrice(String productid, boolean isPurchase) throws ServiceException;

    public KwlReturnObject getPrice(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getProductBaseUOMRate(HashMap<String, Object> request) throws ServiceException;
    
    public JSONObject getProductDisplayUOM(Product product,double  quantity , double baseuomrate,boolean carryin,JSONObject obj) throws ServiceException;

    public KwlReturnObject getPriceListEntry(HashMap<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject getAvgcostAssemblyProduct(String productid, Date stDate, Date endDate) throws ServiceException;

    //Cycle Count
//    public KwlReturnObject getCycleCountForApproval(HashMap<String, Object> requestParams) throws ServiceException;
//
//    public KwlReturnObject getCycleCountWorkSheet(HashMap<String, Object> requestParams, boolean isExpotrt) throws ServiceException;
//
//    public KwlReturnObject saveCycleCount(HashMap<String, Object> dataMap) throws ServiceException;
//
//    public KwlReturnObject getCycleCountProduct(HashMap<String, Object> filterParams) throws ServiceException;
//
//    public KwlReturnObject cycleCountReport(HashMap<String, Object> filterParams, boolean isExport) throws ServiceException;
//
//    public KwlReturnObject getCycleCountEntry(HashMap<String, Object> filterParams) throws ServiceException;
//
//    public KwlReturnObject saveProductCycleCount(HashMap<String, Object> dataMap) throws ServiceException;
    //Inventory
    
    public KwlReturnObject addInventory(JSONObject json) throws ServiceException;
    
    public KwlReturnObject updateJobWorkDetailsAsClose(String rowId,String companyId) throws ServiceException;
    
    public KwlReturnObject updateInventory(JSONObject json) throws ServiceException;

    public KwlReturnObject deleteInventory(String inventoryid, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteLBDMOnDocumentID(Map<String, Object> params) throws ServiceException;

    public KwlReturnObject deleteInventoryEntry(String inventoryid, String companyid) throws ServiceException;

    public KwlReturnObject getInventoryEntry(HashMap<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject getQuantity(String productid) throws ServiceException;

    public KwlReturnObject getAvailableQuantityInSelectedUOM(String productid, String uomId) throws ServiceException;
    
    public KwlReturnObject getAvailableBaseQuantityExceptInSelectedUOM(String productid, String uomId) throws ServiceException;
    
    public double getReserveQuantityTaggedInQuotation(String productid, String companyid) throws ServiceException;
    
    public Map<String, Double> getReserveQuantityTaggedInQuotation(String companyid) throws ServiceException ;

    public KwlReturnObject getConsignedQuantity(String productid) throws ServiceException;

    public KwlReturnObject getVendorConsignedQuantity(String productid) throws ServiceException;

    public KwlReturnObject getLockQuantity(String productid) throws ServiceException;   //for selecting a locked quantity of inventory product used in salesorder      
    
    public KwlReturnObject getInvoiceQuantity(String productid,String comapnyId) throws ServiceException;   
    
    public KwlReturnObject getDOtoReturnQtyByProductBatch(String documentId,String productid,String comapnyId) throws ServiceException;   

    public KwlReturnObject getLockQuantityInSelectedUOM(String productid, String uomId) throws ServiceException;   //for selecting a locked quantity of inventory product used in salesorder      

    public KwlReturnObject getAssemblyLockQuantity(String productid) throws ServiceException;         //get the lock quantity locked in Assembly type of product in  all SO

    public KwlReturnObject getAssemblyLockQuantityForBuild(String mainProductid, String subProductid) throws ServiceException;         //get the lock quantity locked in inventory type of product in  all SO

    public KwlReturnObject getInitialQuantity(String productid) throws ServiceException;

    KwlReturnObject getQuantityPurchaseOrSalesDetails(String productid, boolean isPurchase,boolean isSalesByItem) throws ServiceException;
    KwlReturnObject getQuantityPurchaseOrSalesDetailsByItem(String productid,Date stDate, Date endDate,boolean isPurchase,boolean isSalesByItem) throws ServiceException;
    KwlReturnObject getQuantityPurchaseDetails(String productid, boolean isPurchase,Date stDate, Date endDate,boolean isAssemblySubProduct) throws ServiceException;

    public KwlReturnObject getQuantityPurchaseOrSales(String productid, boolean isPurchase) throws ServiceException;

    public KwlReturnObject getInventoryWOdetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getInventoryWithDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getInventoryOpeningBalanceDate(String companyid) throws ServiceException;

    public KwlReturnObject getInitialCost(String productid) throws ServiceException;

    public boolean isChild(String ParentID, String childID) throws ServiceException;

    public List isChildorGrandChild(String childID) throws ServiceException;

    public KwlReturnObject selectCyclecountPermanently(String productid, String companyid) throws ServiceException;

    public KwlReturnObject selectInventoryByProduct(String productid, String companyid) throws ServiceException;

    public KwlReturnObject selectProPricePermanently(String productid, String companyid) throws ServiceException;

    public KwlReturnObject selectProductCycleCountPermanently(String productid, String companyid) throws ServiceException;

    public KwlReturnObject selectSubProductFromAssembly(String subProductId) throws ServiceException;
    
    public KwlReturnObject selectSubProductFromAssemblyMRP(String subProductId, String bomDetailId) throws ServiceException;
    
    public KwlReturnObject selectSubProductFromBuildAssembly(String subProductId) throws ServiceException;

    public List searchInventoryId(String productid, boolean flag) throws ServiceException;

    public KwlReturnObject updateInitialInventory(JSONObject json) throws ServiceException;
    
    public KwlReturnObject updateQuantityDueOfSerailnumbers(JSONObject json) throws ServiceException;

    public KwlReturnObject getQtyandUnitCost(String productid, Date endDate) throws ServiceException;

    public KwlReturnObject getAssemblyProductBillofMaterials(String productid) throws ServiceException;

    public KwlReturnObject getAssemblyProductDetails(String productid) throws ServiceException;

    public KwlReturnObject getAssemblyBuidDetails(String buidid) throws ServiceException;
    
    public KwlReturnObject getAssembyProductDetails(JSONObject paramJobj) throws ServiceException;

    public String getNextAutoProductIdNumber(String companyid) throws ServiceException;

//    public KwlReturnObject getSoldorPurchaseQty(String productid, boolean sold) throws ServiceException;
    public KwlReturnObject getRateandQtyfromInvoice(String productid) throws ServiceException;

    public KwlReturnObject checkSubProductforAssembly(String productid) throws ServiceException;

    public KwlReturnObject checkIfParentProduct(String productid) throws ServiceException;

    public KwlReturnObject getPriceCustVen(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getProductPriceReportCustVen(HashMap<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getPriceBandListReportCustVen(HashMap<String, Object> request) throws ServiceException;

    public DefaultsForProduct getDefaultAccountsForProduct(HashMap<String, Object> request) throws ServiceException;

    public KwlReturnObject getProduct(String productCode, String companyid) throws ServiceException;

    public int deleteShelfLacation(String companyid) throws ServiceException;

    public KwlReturnObject saveShelfLocationData(HashMap<String, Object> dataMap) throws ServiceException;

//    public KwlReturnObject getShelfLocations(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject saveProductCategoryMapping(String productid, String productCategory) throws ServiceException;
    
    public KwlReturnObject updateProductCategoryIndustryCode(String productid, String indusrtyCode) throws ServiceException;

    public KwlReturnObject deleteProductCategoryMappingDtails(String productid) throws ServiceException;

    public KwlReturnObject deleteProductInitialInventoryDtails(String productid, String companyid) throws ServiceException;

    public KwlReturnObject deleteAssemblyProductInventory(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getProductIDCount(String productID, String companyId, boolean isFixedAsset) throws ServiceException;

    public MasterItem getProductsMasterItem(String companyid, String productid) throws ServiceException;

    public KwlReturnObject getProductTypeByName(String productTypeName) throws ServiceException;

    public KwlReturnObject getUOMByName(String productUOMName, String companyId) throws ServiceException;

    public KwlReturnObject getProductByProductName(String company, String productName) throws ServiceException;

    public KwlReturnObject getLocationByName(String company, String locationName,Store storeId) throws ServiceException;
    
    /**
     * Description:This method is used to get the Location Object by passing location id.
     * @param String locationid, String companyid
     * @return KwlReturnObject
     * @throws ServiceException
     */
    public KwlReturnObject getLocationByID(String locid, String company) throws ServiceException;

    public KwlReturnObject getStoreByName(String company, String storeName) throws ServiceException;

    public KwlReturnObject getProductByProductID(String productID, String companyId) throws ServiceException;

    public KwlReturnObject getAccountByName(String accountName, String companyId) throws ServiceException;

    public KwlReturnObject getVendorByName(String vendorName, String companyId) throws ServiceException;

    public KwlReturnObject getInventoryLocationByName(String inventoryLocation, String companyId) throws ServiceException;

    public KwlReturnObject getInventoryWarehouseByName(String inventoryWarehouse, String companyId) throws ServiceException;

    public KwlReturnObject getCustomerIDByCode(String customerCode, String companyId) throws ServiceException;

    public KwlReturnObject getCurrencies() throws ServiceException;
    
    public List<Frequency> getFrequencies() throws ServiceException;

    public KwlReturnObject getVendorIDByCode(String vendorCode, String companyId) throws ServiceException;

    // Fixed Asset
    public KwlReturnObject addAssetOpening(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getAssetOpenings(HashMap<String, Object> requestMap) throws ServiceException;

    public KwlReturnObject getPurchaseInvoiceIdOfAssetCreatedFromGRN(HashMap<String, Object> requestMap) throws ServiceException;

    public KwlReturnObject getPurchaseInvoiceIdOfAssetCreatedFromPurchaseInvoice(HashMap<String, Object> requestMap) throws ServiceException;

    public KwlReturnObject saveAssetDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject updateAssetDetails(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject increaseOrDecreaseAvailableQuantity(HashMap<String, Object> reqParams) throws ServiceException;
    
    public KwlReturnObject saveAssetOpeningMapping(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getAssetDetails(HashMap<String, Object> requestMap) throws ServiceException;
    public KwlReturnObject getAssetDetailsById(HashMap<String, Object> requestMap) throws ServiceException;

    public KwlReturnObject isOpeningDepreciationRemaining(Map<String, Object> requestMap) throws ServiceException;
    
    /**
     * Description: This method is used to get Asset id with Maximum period of selected asset id's
     * @param requestMap
     * @return KwlReturnObject
     * @throws ServiceException 
     */
    public KwlReturnObject getMaxPeriodOfSelectedAssets(Map<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject getPeriodicDepreciationOFAssetIds(Map<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject getAssetDetailsForCombo(HashMap<String, Object> requestMap) throws ServiceException;

    public KwlReturnObject getAssetDetailsMappedWithOPeningDocument(HashMap<String, Object> requestMap) throws ServiceException;

    public KwlReturnObject deleteAssetDetails(HashMap<String, Object> requestMap) throws ServiceException;

    public KwlReturnObject deleteAssetOpeningDocument(HashMap<String, Object> requestMap) throws ServiceException;

    public KwlReturnObject addDepreciationDetail(HashMap<String, Object> requestMap) throws ServiceException;

    public KwlReturnObject getAssetDepreciationDetail(HashMap<String, Object> filterParams) throws ServiceException;
  
    public KwlReturnObject getPostDepreciationJE(String jeid, String companyid) throws ServiceException;

    public List isAssetExpire(AssetDetails assetDetails,Date disposalDate) throws ServiceException;

    public boolean isDepreciationPosted(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getAssetInvoiceDetailMapping(HashMap<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject saveAssetInvoiceDetailMapping(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject deleteAssetInvoiceDetailMapping(HashMap<String, Object> dataMap) throws ServiceException;
//    public boolean isDocumenthasDepreciatedAsset(String documentId, String companyId)throws ServiceException;

    public KwlReturnObject deleteAssetDetailsLinkedWithOpeningDocuments(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getDepreciatedAssetsOfOpeningDocuments(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getSoldAssetsOfOpeningDocuments(HashMap<String, Object> requestParams) throws ServiceException;
    
    /**
     * Description : This method is used to get Disposal Invoice related data from DB
     * @param requestParams
     * @return List Object Array
     * @throws ServiceException 
     */
    public KwlReturnObject getDisposalInvoiceDetailsFromAssetDetailID(Map<String, Object> requestParams) throws ServiceException;
    
    // For Stock Ledger Report
    public KwlReturnObject getStockLedger(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getDailyStockRegister(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getDetailedStockMovementList(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getStockQuantityOnDate(HashMap<String, Object> requestParams) throws ServiceException,SessionExpiredException;
    
    //get Total product ids count and insert paged product into tempproductcompanyid table.
    public int getTempProductIdsForReport(Date endDate, int start, int limit, String companyId, String requestUUID, String searchString, boolean isExport) throws ServiceException;

//    public KwlReturnObject consignmentInvoice(String invoiceID, String companyid) throws ServiceException;
    public String consignmentInvoice(String invoiceID, String companyid) throws ServiceException;
    
     public String procrutmentInvoice(String invoiceID, String companyid) throws ServiceException ;

    public List getVendorInvoiceTotalAmountDetail(String invoiceID, String personName, String companyid) throws ServiceException;
//    public List getCustomerInvoiceTotalAmountDetail(String invoiceID, String personName, String companyid) throws ServiceException;

    // For Stock Ageing Report
    public KwlReturnObject getOnhandQuantityOfProduct(HashMap requestParam) throws ServiceException;

    public KwlReturnObject getRateandQtyOfOpeningGRSR(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getLocationSummary(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getQuantityAndRateOfProductForLocation(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getQuantityAndRateOfProductForLocationDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getSerialNoWhoseDONotCreated(HashMap<String, Object> requestParams) throws ServiceException;

    public List getQuantityWhoseDOCreated(HashMap<String, Object> requestParams) throws ServiceException;
    
    public boolean IsBatchUsedInOutTransaction(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getAssetMaintenanceWorkOrders(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteAssetMaintenanceSchedule(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getScheduleNumberCount(String scheduleNumber, String companyid) throws ServiceException;

    public KwlReturnObject saveMaintenanceSchedulerObject(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveMaintenanceSchedule(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject updateMaintenanceSchedule(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getAutoBuildAssemblyEntriesForDeliveryOrder(HashMap<String, Object> requestParams) throws ServiceException;

    //functions to check product used in transaction or not
    public KwlReturnObject getPO_Product(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getSO_Product(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getCQ_Product(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getGoodsReceipt_Product(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getInvoice_Product(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getGR_Product(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getDO_Product(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getSalesReturn_Product(String productid, String companyid) throws ServiceException;
    
    public KwlReturnObject getInventoryTransaction_Product(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getPR_Product(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getVQ_Product(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getPurchaseReturn_Product(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getSerialNos(String productid, String companyid) throws ServiceException;

    public KwlReturnObject getProductPriceFromPricingBand(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteProductBatchSerialDetails(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteProductCompostion(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject activateDeactivateProducts(HashMap request) throws ServiceException;

    public KwlReturnObject getPriceListBandIDByName(String priceListBandName, String companyId) throws ServiceException;

    public KwlReturnObject getProductPriceFromPriceListVolumeDiscount(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteAssetDepreciationJE(String je, String companyID) throws ServiceException;

    public KwlReturnObject deleteAssetDepreciationDetails(String je, int period) throws ServiceException;
   
    /**
     * Description: This method is used delete Asset Depreciation details Entries
     * @param dataMap
     * @return
     * @throws ServiceException 
     */
    
    public KwlReturnObject deleteAssetDepreciationDetails(Map<String, Object> dataMap) throws ServiceException;
    
      /**
     * Description: This method is used delete Journal Entry details against Asset Depreciation
     * @param dataMap
     * @return
     * @throws ServiceException 
     */
    public KwlReturnObject deleteAssetDepreciationJE(Map<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getProductExportDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject saveProductExportDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getAssetDetail(HashMap<String, Object> requestMap) throws ServiceException;   //Edit  option for  Asset opeening Window 
    
    public KwlReturnObject getAssetDetailFromAssetID(HashMap<String, Object> requestMap) throws ServiceException;

    public KwlReturnObject getAssetPurchaseRequisitionDetailMapping(HashMap<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject savePurchaseRequisitionAssetDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveAssetPurchaseRequisitionDetailMapping(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getPurchaseRequisitionAssetDetails(HashMap<String, Object> requestMap) throws ServiceException;

    public KwlReturnObject updateTotalIssueCount(double totalIssueCount, String prductID, String companyid,boolean updateflag, boolean isEdit) throws ServiceException;
    
    public KwlReturnObject getProductByInventoryID(String inventoryid, String companyid) throws ServiceException ;
    
    public KwlReturnObject getPriceBandProductsPrice(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getProductTypeByProductID(String productTypeID) throws ServiceException;
    
    public KwlReturnObject getProductBuildDetailInventory(String productBuildID) throws ServiceException;
    
    public KwlReturnObject getProductCategoryIDByName(String productCategoryName, String companyId) throws ServiceException;
    
    public KwlReturnObject getProductCategoryMapping(String productID, String productCategoryID) throws ServiceException;
    
    public KwlReturnObject deleteProductCategoryMappingForNoneCategory(String productID) throws ServiceException;
        
    public double getProductQuantityUnderParticularStore(Product product,String storeId) throws ServiceException;
    
    public double getProductQuantityUnderParticularStoreLocation(String product,String warehouseId,String locationid,String companyId) throws ServiceException;
    
    public String updateProductEntryNumber(Map<String, Object> seqNumberMap);
    
    public double getPriceByDocumentId(String documentId) throws ServiceException;
    
    public String updateNAProductEntryNumber(String pid,String entrynumber);
    
    public List getPurchaseInvoiceNoAndVendorOfAssetPurchaseInvoice(HashMap<String, Object> requestMap) throws ServiceException;
    
    public List getPIDetails_AssetGRLinkedInAssetPI(HashMap<String, Object> requestMap) throws ServiceException;
    
    public List getVendorForAssetDetail(HashMap<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject getVendors_Product(String productid, String companyid) throws ServiceException;
    
    public KwlReturnObject getCustomers_Product(String productid, String companyid) throws ServiceException;
    
    public KwlReturnObject isQuantityInStockMovementfor_Product(String productid, String companyid) throws ServiceException;
    
    public KwlReturnObject getBatchesfor_Product(String productid, String companyid) throws ServiceException;
    
    public KwlReturnObject getSerialsfor_Product(String productid, String companyid) throws ServiceException;
    
    //public KwlReturnObject getinvoiceDocuments(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getProductCategoryForDetailsReport(String productId) throws ServiceException;
    
    public KwlReturnObject getOpeningDepreciationForAccount(HashMap<String, Object> reqParams) throws ServiceException;
        
    public int getOpeningDepreciationCountForAccount(String accountid, String companyid) throws ServiceException;
    
    public KwlReturnObject getAssetPurchaseOpeningForAccount(HashMap<String, Object> reqParams) throws ServiceException;
    
    public int getAssetPurchaseOpeningBalanceCountForAccount(HashMap<String, Object> requestMap) throws ServiceException;
    
//    public Date getAssetPurchaseAccountBalanceBasedOnDate(ExtraCompanyPreferences extra, CompanyAccountPreferences preferences)throws ServiceException;

    public KwlReturnObject getAssetOpeningsEditCount(String documentNumber, String companyId, String assetOpeningId) throws ServiceException;

    public KwlReturnObject getDepartment(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getUser(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getRowRackBin(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteOpeningAssetDepreciationDetails(Map<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject resetOpeningDepreciationOfAssets(Map<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject updateProductParent(String productid, String companyid) throws ServiceException;

    public String getfieldComboIdFromAssetDetail(String assetId, String column) throws ServiceException;

    public double getBalanceFromAssetOpening(String companyId, String docId) throws ServiceException;
    
    public boolean isOpeningPresentForAsset(String productId, String companyId) throws ServiceException;
    
    public String getDefaultBomId(String productId) throws ServiceException;
     
    public KwlReturnObject getChildproducts(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject checkDuplicateProductForEdit(String entryNumber, String companyid, String productid,boolean isFixedAsset) throws ServiceException;
    
    public List getRetailStorelistForPOS(String companyID) throws ServiceException;
    
    public List getProductIDsOfCompanyForPOS(String companyID) throws ServiceException;
    
    public List getProductQuantityForPOS(String pid) throws ServiceException;
    
    public KwlReturnObject getProductsFoRemoteAPI(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getChildProductsFoRemoteAPI(HashMap<String, Object> requestParams) throws ServiceException;
    
    public List<Product> getSyncableProduct(String companyid) throws ServiceException;
    
    public Set getProductIdUsedInTransaction(String productIds[], boolean unbuild) throws ServiceException;
    
    public List getProductListByIds(Set<String> productIds) throws ServiceException;
    
    public List getProductListByIdsSQL(Set<String> productIds) throws ServiceException;
    
    public List getProductCodesListByIds(Set<String> productIds) throws ServiceException;
    
    public KwlReturnObject getProductCategoryDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getSpecialRateofProduct(String productid, boolean isPurchase, Date transactionDate, String affectedUser, String forCurrency) throws ServiceException;
    
    public KwlReturnObject getStockAdjustment_Product(Map<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject deletePriceList(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getLineLevelTerm(HashMap<String,Object> pamsHashMap) throws ServiceException;
    
    /**
     * 
     * @param productId
     * @param transactionType  input values as 'Opening'= -1, 'Initial Quantity' = 0, 'Assembly Main Product' = 0, 'GRN'= 1, 'Purchase Return'= 2, 'DO'= 3, 'Sales Return' = 4, 'Assembly Sub Product' = 5, 'Inventory Stock In'=7, 'Inventory Stock Out'= 8
     * @param valuationMethod
     * @param baseUomQuantity
     * @param stockPriceInTransactionUom
     * @param baseUomRate
     * @param isOpening // for opening transactions true otherwise false
     * @param pvbList can not null
     * @return perunitprice 
     * @throws ServiceException 
     */
    public double getValuationPrice(String productId, int transactionType, ValuationMethod valuationMethod, double baseUomQuantity, double stockPriceInTransactionUom, double baseUomRate, boolean isOpening, List<TransactionBatch> pvbList) throws ServiceException;
    
    public double getValuationPriceForSerialBatches(String productId, int transactionType, ValuationMethod valuationMethod, double baseUomQuantity, double stockPriceInTransactionUom, double baseUomRate, boolean isOpening, List<TransactionBatch> pvbList,boolean isForSerials,String[] serialId) throws ServiceException;
    
    public KwlReturnObject getSADetailByStockAdjustment(String stockadjustmentid) throws ServiceException;
    
//    public KwlReturnObject getbatchId(HashMap requestParam) throws ServiceException;
    
    /**
     * Description : For getting Product object whose 'productid' is substitute product
     * @param <productid> for getting product id
     * @param <companyid> for getting company id
     * @return KwlReturnObject
     * @throws ServiceException 
     */
    public KwlReturnObject getSubstituteProduct(String productid, String companyid) throws ServiceException;
    
    public KwlReturnObject getProductTermDetails(Map<String,Object> mapData) throws ServiceException;
    
    public KwlReturnObject saveProductTermsMap(HashMap<String, Object> productTermsMap)throws ServiceException;
    
    public KwlReturnObject saveQualityControlData(Map<String, Object> map) throws ServiceException;
    
    public KwlReturnObject deleteQualityControlData(String parentProductId) throws ServiceException;
    
    public KwlReturnObject getQualityControlData(Map<String, Object> requestParams) throws ServiceException ;
    
    public boolean isDefaultSeuenceFormatSetForBuildAssembly( JSONObject requestParams) throws AccountingException ;
    
    public KwlReturnObject saveBOMDetail(Map<String, Object> assemblyMap) throws ServiceException;
    
    public KwlReturnObject deleteBOMDetail(String parentProductId) throws ServiceException;    
    public KwlReturnObject deleteBOMDetailById(String bomid) throws ServiceException;    
    public KwlReturnObject deleteProductAssemblyByBOMDetailId(String bomid) throws ServiceException;    
    public KwlReturnObject getBOMids(String parentProductId) throws ServiceException;    
    
    public KwlReturnObject getBOMDetail(Map<String, Object> requestParams) throws ServiceException ;

    public KwlReturnObject getAssetDetailsbyAssetIds(String companyId, JSONArray array) throws ServiceException ;


    public KwlReturnObject saveAssetMachineMapping(Map<String, String> dataMap) throws ServiceException;
    public KwlReturnObject getMachineId(Map<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getBOMCombo(Map<String, Object> requestParams) throws ServiceException;
    public boolean checkProductBuild(String productid);
   
    public boolean checkProductBomDetail(String productid);
    
    public boolean checkProductUsedinJWO(String productid);
    
    public KwlReturnObject getProductBrandDiscountDetails(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getProductBrandDiscountDetailsList(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject saveOrUpdateProductBrandDiscountDetails(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteProductBrandDiscountDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getSumOfOpeningBalanceForProduct(Map<String, Object> requestMap) throws ServiceException;
    public KwlReturnObject getWOLockQuantity(String productid,boolean isManageQuantity) throws ServiceException;
    public KwlReturnObject getWOLockQuantity(String productid) throws ServiceException;
    public KwlReturnObject getVATCommodityCodeByName(Map<String,String> mapData) throws ServiceException; 
    
    public KwlReturnObject getNatureofStockItemCodeByName(Map<String,String> mapData) throws ServiceException; 
    
    public KwlReturnObject getRG23PartIData(HashMap<String, Object> requestParams) throws ServiceException;
    public Map<String, Double> getProductBlockedQuantity(Company company, Store store, Location location, String searchString) throws ServiceException ;
    
    public JSONObject getProductBlockedQuantitybyWarehouseLocation(JSONObject obj, Store store, Location location,Date startDate,Date endDate) throws ServiceException, JSONException ;
    
    /**
     * This method is used to check asset id is used in maintenance or not
     * @param dataMap
     * @return boolean
     * @throws ServiceException 
     */
    public boolean isAssetUsedInMaintenanceSchedule(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getJEFromDisposedOrRevertedAssetDetails(String jeId, String CompanyId, Boolean isReverseJe) throws ServiceException;
    
    public int deleteDisposedAssets(HashMap<String,Object> reqMap) throws ServiceException;
    
    public KwlReturnObject saveOrUpdateDisposeRevertAssets(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getProductIdsForCompany(Map<String, Object> reqMap);

    public int getProductValuationMethod(String productid);

    public KwlReturnObject getSerialIdByStorageDetails(Map<String, String> storageDetails) throws ServiceException;
    
    public double getCycleCountQuantity(Map<String, Object> requestParams) throws ServiceException;
    
    /**
     * This method used for Get Product line level terms for CRM Quotation integrations
     * @param customParams
     * @return 
     */
    public JSONArray getProductTermsJsonArray(Map<String, Object> customParams);
    
    /**
     * This method used for Get Company line level terms for CRM Quotation integrations
     * @param customParams
     * @return 
     */
    public JSONArray getCompanyTermsJsonArray(HashMap<String, String> customParams);
    
    public boolean mapCompanyTaxToProductTax(HashMap<String, String> dataMap,Product product,Map<String,List<String>> mapDefaultTerm) throws ServiceException;
    
    /**
     * This method is used to check that user entered BOM Code is already exist in system or not.
     * @param bomCode, Product ID, Company ID
     * @return boolean
     * @throws ServiceException 
     */
    public boolean isBOMCodeExist(String bomCode, String productid, String companyId) throws ServiceException;
    
    public KwlReturnObject getSADetailsForSO(HashMap<String, Object> requestParams) throws ServiceException ;
    
    public KwlReturnObject getISTDetailsForJobWorkOrder(HashMap<String, Object> requestParams) throws ServiceException ;
     
    public KwlReturnObject saveQAApprovalDetails(HashMap<String, Object> documentMap) throws ServiceException;

    public KwlReturnObject saveProductBuildsApprovedQty(HashMap<String, Object> ApprovalStatusData)  throws ServiceException;

    public KwlReturnObject updateAssemblyProductsInventoryEntry(JSONObject invntJson) throws ServiceException;

    public KwlReturnObject GetWareHouseNamesOfProductBuild(String productBuild) throws ServiceException;
    
    public boolean UpdateTaxToProductTax(HashMap<String, String> requestParams,Product product,Map<String,List<String>> mapDefaultTerm) throws ServiceException;
    
    public KwlReturnObject  getVendorList(HashMap<String,Object> requesrParms)throws ServiceException;
     /**
     * description : return number of record are already present for given asset id for current company
     * @param requestParams
     * @return
     * @throws ServiceException 
     */
    public KwlReturnObject checkIsAssetIDAlreadyUsed(Map<String, Object> requestParams)throws ServiceException;
    
    /**
     * description : return attached documents with Synced Product.
     * @param Map
     * @return JSONArray
     * @throws ServiceException 
     */
    public JSONArray getProductDocumentsArray(Map<String, Object> customParams);
    
    public KwlReturnObject getSameNameProductCategoryIds(Map<String, Object> customParams) throws ServiceException;
    
    public KwlReturnObject deleteProductLandingCostCategory(String subProductId) throws ServiceException;
    
    public KwlReturnObject getProductCustomData(Map<String, Object> requestParams) throws ServiceException;
    
    /**
     * description : This method used to check whether the Product having BOM Products or not
     * @param String Product ID
     * @return List with Boolean value
     * @throws ServiceException 
     */
    public List isAssemblyProductWithBOM(String productid) throws ServiceException;
    
    public List getConsignmentInvoice(String invoiceID, String companyid) throws ServiceException;
    
    public IncidentCases getIncidentById(Map<String, Object> requestParams) throws ServiceException;    
    
    public KwlReturnObject getAssemblyProductDefaultBuildOfMaterials(String productid) throws ServiceException;
    
    public List getProductsCategoryByProductID(String companyid, String productid) throws ServiceException;
    
    public KwlReturnObject getSubAssemblyProduct(JSONObject reqParams) throws ServiceException ;
    
    public double getSumOfPurchasePriceForSubassebmblyItems(String productid) throws ServiceException;
    
    public Map<String, Double> getProductQuantityUnderQA(Company company, Store store) throws ServiceException;
    
    public Map<String, Double> getProductQuantityUnderRepair(Company company, Store store) throws ServiceException;
    
    public Map<String, Double> getProductQuantityUnderQAForGRNAndDO(Company company, Store store, boolean isGRNOnly) throws ServiceException;
    
    public Map<String, Double> getProductQuantityUnderQAForWO(Company company, Store store, boolean isWOOnly) throws ServiceException;

    public Map<String, Double> getProductQuantityUnderRepairForGRNAndDO(Company company, Store store, boolean isGRNOnly) throws ServiceException;
    
    public Map<String, Double> getProductQuantityUnderRepairForWO(Company company, Store store, boolean isGRNOnly) throws ServiceException;
    
    public void unmapAccount(String Productid) throws ServiceException;
    
    public KwlReturnObject getBuildAssemblyProdcutDetails(String transactionId) throws ServiceException;
    
    public KwlReturnObject checkIsAssetIDAlreadyUsed(Map<String, Object> requestParams,NewBatchSerial batchserialOBj)throws ServiceException;
    
    public KwlReturnObject getProductDiscountFromPriceListBand(HashMap<String, Object> requestParams) throws ServiceException;
    
    /**
     * Description - This method has written to get Batch Details for selected product & used for Barcode Purpose.
     * @param String, String
     * @return List of NewProductBatch Object
     * @throws ServiceException 
     */
    public KwlReturnObject getBatchDetailsByProductID(String productid, String companyid) throws ServiceException;
    
    public Map<String, Double> getProductApprovedOrRejectedOrPickedQty(Company company, Store store) throws ServiceException;
    
    public boolean saveCompanyProductPriceList(JSONObject json) throws ServiceException;

    public KwlReturnObject getProductByProductIdAndSubdomain(String productID, String subdomain) throws ServiceException;
    
    /*
     * Description - This method has written to delete stock movement entries for Assembly Product
     * @param String, String
     * @return List of KwlReturnObject
     * @throws ServiceException 
    */
    public KwlReturnObject deleteStockMovement(String assemblyProduct, String companyid) throws ServiceException;
    
    public KwlReturnObject getProductBuildDetails(String pbDetailsID) throws ServiceException;

   public KwlReturnObject getProductsForComboSQLQuery(HashMap<String, Object> requestParams) throws ServiceException;
   
   public boolean isProductQuantityFromColumn(String companyid) throws ServiceException;
   
   public KwlReturnObject getLandedCostCategoriesforProduct(String productid,String companyid) throws ServiceException;
   
   public List getGstProductHistory(Map<String, Object> reqMap) throws ServiceException;
   
   public List getGstProductUsedHistory(Map<String, Object> reqMap) throws ServiceException;
   
   public KwlReturnObject deleteProductTaxClassHistoryPermanently(String productid) throws ServiceException;
   
   /**
     * Description : This method is used to get the blocked qunatity of BOM product used in Assembly Product.
     * @param productid : Assembly Product ID, subproduct : BOM Product ID
     * @return KwlReturnObject (with Locked Quantity)
     * @throws ServiceException 
     */    
    public KwlReturnObject getBOMProductLockQuantity(String productid, String subproduct) throws ServiceException;
    
    public JSONObject getVendorsMappedProduct(String productid) throws ServiceException ;
    
     public KwlReturnObject getInventoryTransactions(String productId,String companyid) throws ServiceException;

    public KwlReturnObject getRateAndExchangeRateFromGoodsReceiptOrderDetail(String istid, String companyid) throws ServiceException;
    
    public KwlReturnObject getRateFromWorkOrderComponentDetail(String istid, String companyid) throws ServiceException;
    
    public boolean isWorkOrderComponentDetailSentToQA(String wocdid, String companyid)  throws ServiceException;

    public boolean isGoodsReceiptOrderDetailSentToQA(String grodid, String companyid) throws ServiceException;
    
    public KwlReturnObject insertInitialPurchasePrice(String companyid) throws ServiceException;
    
    public List getGoodsReceiptOrderDetailSentToQA(String companyid)  throws ServiceException ;
    
    /**
     * Description : This method is used to get the available qunatity of BOM product used in Assembly Product by location and warehouse
     * @param productid, location, warehouse, company
     * @return KwlReturnObject (with available Quantity)
     * @throws ServiceException 
     */ 
    public KwlReturnObject getLocationWarehouseWiseAvailableQuantity(HashMap<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject getAssembyProductBOMDetails(JSONObject paramJobj) throws ServiceException;
    
    public KwlReturnObject getProductCountfromInventoryAccount(String accountid, String companyid) throws ServiceException;
    
    public KwlReturnObject getLandedInvoiceListForProduct(String productid, String companyid) throws ServiceException;
    
    public KwlReturnObject getWorkOrderProductsCombo(Map<String, Object> requestParams) throws ServiceException;
    
}
