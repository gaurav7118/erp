/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stock;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.InventoryLocation;
import com.krawler.common.admin.InventoryWarehouse;
import com.krawler.common.admin.NewBatchSerial;
import com.krawler.common.admin.NewProductBatch;
import com.krawler.common.admin.StoreMaster;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferDetailApproval;
import com.krawler.inventory.model.ist.ISTDetail;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.stockmovement.TransactionType;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.inventory.model.stockout.StockAdjustmentDetail;
import com.krawler.inventory.model.stockrequest.StockRequest;
import com.krawler.inventory.model.stockrequest.StockRequestDetail;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.*;

/**
 *
 * @author Vipin Gupta
 */
public interface StockService {

    public Inventory updateERPInventory(boolean increase, Date transactionDate, Product product, Packaging packaging, UnitOfMeasure uom, double uomQuantity, String description) throws ServiceException;

    public Inventory updateERPInventoryByid(boolean increase, Date transactionDate, Product product, Packaging packaging, UnitOfMeasure uom, double uomQuantity, String description, String inventoryid) throws ServiceException;

    @Deprecated
    public void updateStockForCycleCount(Product product, Store store, Location location, String batchName, String serialNames, double expectedQuantity) throws ServiceException;

    @Deprecated
    public void increaseInventory(Product product, Store store, Location location, String batchName, String serialNames, double quantity) throws ServiceException;

    @Deprecated
    public void decreaseInventory(Product product, Store store, Location location, String batchName, String serialNames, double quantity) throws ServiceException;

    public void increaseInventory(Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName, String serialNames, double quantity) throws ServiceException;

    public void decreaseInventory(Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName, String serialNames, double quantity) throws ServiceException;

    public double getProductPurchasePrice(Product product, Date date) throws ServiceException;

    public double getProductTotalAvailableQuantity(Product product,Store store,Location location) throws ServiceException;

    public double getProductQuantityInStore(Product product, Store store) throws ServiceException;

    public double getProductTotalQuantity(Product product) throws ServiceException;

    public double getProductQuantityUnderQA(Product product, String qaStoreId, boolean isRepair) throws ServiceException;

    public double getProductQuantityUnderRepair(Product product, String repairStoreId) throws ServiceException;

    public double getProductQuantityInStoreLocation(Product product, Store store, Location location) throws ServiceException;

    public Stock getStockById(String stockId) throws ServiceException;

    public Stock getStock(Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName) throws ServiceException;
    
    public Stock getStock(String productId, String storeId, String locationId, String rowId, String rackId, String binId, String batchName) throws ServiceException;

    public List<Stock> getBatchWiseStockList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging) throws ServiceException;
//    public List<Stock> getBatchWiseStockList(Company company, Store store, Location location, String searchString, Paging paging) throws ServiceException;

    public List<Stock> getStoreWiseStockList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging,String inventoryCatType) throws ServiceException;

    public List<Object[]> getStoreWiseProductStockList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging, String inventoryCatType) throws ServiceException;

    public List<Object[]> getWarehouseWiseProductStockList(Company company, Set<InventoryWarehouse> storeSet, InventoryLocation location, String searchString, Paging paging, String inventoryCatType) throws ServiceException;

    public List<Object[]> getStoreWiseDetailedStockList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging,String productId) throws ServiceException;
    
    public List<Object[]> getStoreWiseProductStockSummaryList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging,String inventoryCatType) throws ServiceException ;

    public List<NewBatchSerial> getProductSerialList(String productid, String companyid, Set<Store> storeSet, String locationId, String storeId, String searchString, Paging paging) throws ServiceException;

    public void updateObject(Object object) throws ServiceException;

    public Map<String, Double> getAvailableQuantityForAllProductByStore(Company company) throws ServiceException;

    public Map<String, Double> getAvailableQuantityForAllProductByStore(Company company,List<StockRequest> stockRequestList,boolean isExport) throws ServiceException;

    public Map<String, Double> getAvailableQuantityForAllProductByStoreLocation(Company company) throws ServiceException;

    public List<NewProductBatch> getERPActiveBatchList(Product product, Store store) throws ServiceException;
    
    public Map<String,String> getERPBatchListWithExpiryDate(Product product, Store store) throws ServiceException;

    public List<NewProductBatch> getERPActiveBatchList(Product product, Store store, Location location) throws ServiceException;

    public List<NewBatchSerial> getERPActiveSerialList(Product product, NewProductBatch productBatch, boolean checkQAReject) throws ServiceException;

    public List<NewBatchSerial> getERPActiveSerialList(Product product, Store store, Location location, String batchName, boolean checkQAReject) throws ServiceException;

    @Deprecated
    public List<String> getProductBatchWiseSerialList(Product product, String batchName) throws ServiceException;

    public List<Stock> getStockByStoreProduct(Product product, Store store) throws ServiceException;

    @Deprecated
    public NewProductBatch getERPProductBatch(Product product, Store store, Location location, String batchName) throws ServiceException;

    public NewProductBatch getERPProductBatch(Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName) throws ServiceException;

    public NewBatchSerial getERPBatchSerial(Product product, NewProductBatch productBatch, String serialName) throws ServiceException;

    public boolean isSerialExists(Product product, Store store, Location location, String batchName, String serialName) throws ServiceException;
    
    public boolean isSerialExists(Product product, Store store, Location location, String batchName, String serialName,String module,String documentID) throws ServiceException;
    
    public boolean isSerialExistsinDO(Product product,String batchName, String serialName) throws ServiceException;
    
    public boolean isSKUExists(Product product, String batchName, String skuName) throws ServiceException;

    public boolean isAnyStockLiesInStore(Company company, Boolean isQAOrRepair) throws ServiceException;

    public NewProductBatch getBatchDataByProductBatchName(Product product, String batchName) throws ServiceException;

    public List<Stock> getBatchSerialListByStoreProductLocation(Product product, Set<Store> storeSet, Location location) throws ServiceException;

    public Map<String, NewProductBatch> getBatchListByStoreProductLocation(Product product, Set<Store> storeSet, Location location,boolean isEdit) throws ServiceException;

    public List<Stock> getBatchSerialListByStoreProductLocation(String productId, Set<Store> storeSet, Location location,String batchName,boolean isEdit) throws ServiceException;

    public Map<String, String> getSerialListByStoreProductLocation(Product product, Set<Store> storeSet, Location location) throws ServiceException;

    public NewBatchSerial getSerialDataBySerialName(Product product, NewProductBatch productBatch, String serialName) throws ServiceException;

    public NewBatchSerial getSerialDataBySerialName(Product product, String serialName) throws ServiceException;

    public String getSkuDataBySerialName(Product product, String serialName) throws ServiceException;

    public void renameSerialInStock(Product product, String batchName, String oldSerialName, String newSerialName) throws ServiceException;

    public Store getQaStore(Company company) throws ServiceException;

    public Store getRepairStore(Company company) throws ServiceException;

    public void addStockMovementForQa(StockAdjustmentDetail sad, Store store, TransactionType type, String batch, String Serno, double qty, TransactionModule moduleType, String remark, Location location) throws ServiceException;

    public void addStockMovementForQa(ISTDetail stktr, Store store, TransactionType type, String batch, String Serno, double qty, TransactionModule moduleType, Company company, Location location) throws ServiceException;

    public void addStockMovementForSRDQa(StockRequestDetail stktr, Store store, TransactionType type, String batch, String Serno, double qty, TransactionModule moduleType, Company company, Location location) throws ServiceException;

    public void addBulkStockMovementForQa(StockAdjustment sa, TransactionType transactionType, String remark, Store store) throws ServiceException;

    public Map<Product, Double> getDateWiseStockList(Company company, Set<Store> storeSet, Location location, Date date, String searchString, Paging paging) throws ServiceException;

    public List<Object[]> getDateWiseStockInventory(Company company, Set<Store> storeSet, Location location, Date date, String searchString, Paging paging) throws ServiceException;

    public Map<Product, List<Object[]>> getDateWiseStockDetailList(Company company, Set<Store> storeSet, Location location, Date date, String searchString, Paging paging) throws ServiceException;

    /**
     * This method returns stock movement transactions (IN or OUT) as you want,
     * If no date range is selected then it will give all the transaction, if
     * only toDate is given then it will return all transaction till toDate
     * including toDate, if only fromDate is given then it will return all
     * transaction from fromDate including fromDate, if both fromDate and toDate
     * is given then it will return all transaction between fromDate and toDate
     * including bothDate.
     *
     * @param product
     * @param storeSet optional, if null or empty then consider all store
     * @param location optional, if null then consider all location
     * @param fromDate optional, if null then do not restrict fromDate
     * @param toDate optional, if null then do not restrict toDate
     * @param outData true for out transaction and false for in or opening
     * transactions
     * @param searchString optional
     * @param paging optional
     * @return return the Map with key as concatination of ProductId,
     * StoreAbbrevation, LocationName, RowName, RackName, BinName, BatchName.
     * and value as a array of Object which contains values with indexes as 0 -
     * ProductId, 1 - StoreAbbrevation, 2 - LocationName, 3 - RowName, 4 -
     * RackName, 5 - BinName, 6 - BatchName, 7 - quantity(Double), 8 -
     * SerialNames(comma separated) 9 - StoreDescription, 10 - UOMname, 11 -
     * StoreId, 12 - LocationId, 13 - RowId, 14 - RackId, 15 - BinId
     * @throws ServiceException
     */
    public Map<String, Object[]> getDateWiseStockDetailListForProduct(Product product, Set<Store> storeSet, Location location, Date fromDate, Date toDate, boolean outData, String searchString, Paging paging) throws ServiceException;

    public List<Object[]> getDateWiseStockDetailListForProduct(Product product, Set<Store> storeSet, Location location, Date date, String searchString, Paging paging) throws ServiceException;

    public List<Object[]> getDateWiseStockDetailListForProduct(Product product, Set<Store> storeSet, Location location, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException;

    public Map<String, Stock> getPendingApprovalStock(Product product, Store store) throws ServiceException;

    public Map<String, Double> getProductQuantityUnderQA(Company company, Store store) throws ServiceException;

    public Map<String, Double> getProductQuantityUnderRepair(Company company, Store store) throws ServiceException;

    public List<Stock> getReorderReportList(Company company, Set<Store> storeSet, String searchString, boolean isBelowReorderFilter, Paging paging) throws ServiceException;

    public List<Stock> getStockDetailBySerialInOtherStore(Product product, Store store, String batchName, String serialName) throws ServiceException;

    public List getStockForPendingApprovalSerial(String productId, String batchName, String serialName) throws ServiceException;

    public List getStockForPendingRepairSerial(String productId, String batchName, String serialName) throws ServiceException;

    public List<Stock> getStockDetailBySerial(Product product, String batchName, String serialName) throws ServiceException;

    public Map<String, Double> getProductBlockedQuantity(Company company, Store store, Location location, String searchString) throws ServiceException;
    
    public Map<String, Double> getProductBlockedQuantityWithInStore(Company company, Store store,String searchString) throws ServiceException ;
    
    public Map<String, String> getSerialSkuMap(String productId, String batchName, String[] serialArray) throws ServiceException;
    
    public JSONArray getNewStockLedgerJson(HashMap<String, Object> jsonRequestParam, List list, Map<String, Object[]> companyMaxDateProductPriceList, JSONArray valuationArray) throws JSONException, ServiceException, ParseException, SessionExpiredException ;
    
    public JSONArray getPriceCalculationForAsseblyProduct(HashMap<String, Object> jsonRequestParam) throws ServiceException, SessionExpiredException, ParseException, JSONException;
    
    public boolean isOpeingOrPeriodTransaction(Date transactionDate, Date StartDate, int transType) throws SessionExpiredException, ParseException;

    public NewProductBatch getERPProductBatch(String product, String store, String location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName, String companyId) throws ServiceException;

    public NewBatchSerial getERPBatchSerial(String product, NewProductBatch productBatch, String serialName, String companyId) throws ServiceException;
    
    public List getERPSerialFromBatch(Product product, String warehouse, String location, String row, String rack, String bin, String batchName, String serialName) throws ServiceException;
    
    public JSONObject getStoreProductWiseAllAvailableStockDetailList(JSONObject paramJobj);
    
    public JSONObject getAssetDetailList(HashMap<String, Object> jsonRequestParam,Paging paging,Company company);
    
    /**
     * Description :This method is used to get Asset (SKU field)  details by below parameters 
     * @param productID
     * @param serialName
     * @param companyID
     * @return NewBatchSerial Object
     * @throws ServiceException 
     */   
    public NewBatchSerial getSkuBySerialName(String productID, String serialName, String companyID) throws ServiceException;
    
    public KwlReturnObject getGRODetailISTMapping(JSONObject json) throws ServiceException;
    
    public KwlReturnObject getWOCDetailISTMapping(JSONObject json) throws ServiceException;
    
    public String getGoodsReceiptOrderNumberUsingMapping(JSONObject json) throws ServiceException;
    
    public String getWorkOrderNumberUsingMapping(JSONObject json) throws ServiceException;
    
    public KwlReturnObject getDODetailISTMapping(JSONObject json) throws ServiceException;
    
    public KwlReturnObject getDODQCISTMapping(JSONObject json) throws ServiceException;
    
    public KwlReturnObject getRejectedDODQCISTMapping(JSONObject json) throws ServiceException;

    public String getDeliveryOrderNumberUsingMapping(JSONObject json) throws ServiceException;
    
    public Store getPackingstore(Company company) throws ServiceException;
    
    public void updateDeliveryOrderStatus(JSONObject params) throws ServiceException;
    
    public List getbatchserialdetails(HashMap<String, String> batchserialMap,TransactionModule transModule,Product product) throws ServiceException;    
    
    public boolean getISSerialisValidforOut(Product product, String storeId, String locationId, Date bussinessDate, String ss);

    public List<String> getbatchidFromDocumentId(String documentId) throws ServiceException;
}
