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
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.stockrequest.StockRequest;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;

/**
 *
 * @author Vipin Gupta
 */
public interface StockDAO {

    public void delete(Object object) throws ServiceException;

    public void saveOrUpdate(Object object) throws ServiceException;

    public double getProductTotalQuantityInStore(Product product, Store store) throws ServiceException;

    public double getProductTotalQuantity(Product product) throws ServiceException;

    public double getProductQuantityUnderParticularStore(Product product, String storeId, boolean isRepair) throws ServiceException;

    public double getProductQuantityInStoreLocation(Product product, Store store, Location location) throws ServiceException;

    public Stock getStock(Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName) throws ServiceException;

    public Stock getStock(String productId, String storeId, String locationId, String rowId, String rackId, String binId, String batchName) throws ServiceException;

    public Stock getStockById(String stockId) throws ServiceException;

    public List<Stock> getBatchWiseStockList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging) throws ServiceException;

    public List<Stock> getBatchWiseStockList(Company company, Store store, Location location, String searchString, Paging paging) throws ServiceException;

    public List<Stock> getStoreWiseStockList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging, String inventoryCatType) throws ServiceException;

    public List<Object[]> getStoreWiseProductStockList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging, String inventoryCatType) throws ServiceException;

    public List<Object[]> getStoreWiseDetailedStockList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging,String productId) throws ServiceException;

    public List<Object[]> getStoreWiseProductStockSummaryList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging, String inventoryCatType) throws ServiceException;

    public List<NewBatchSerial> getProductSerialList(String productid, String companyid, Set<Store> storeSet, String locationId, String storeId, String searchString, Paging paging) throws ServiceException;

    public void updateObject(Object object) throws ServiceException;

    public List<NewProductBatch> getERPActiveBatchList(Product product, Store store, Location location) throws ServiceException;
    
    public List<Object[]> getERPBatchListWithExpiryDate(Product product, Store store) throws ServiceException;

    public List<NewBatchSerial> getERPActiveSerialList(Product product, NewProductBatch productBatch, boolean checkQAReject) throws ServiceException;

    @Deprecated
    public List<String> getProductBatchWiseSerialList(Company company, Product product, String batchName) throws ServiceException;

    public Map<String, Double> getAvailableQuantity(Company company, boolean includeLocation) throws ServiceException;

    public List<Stock> getStockByStoreProduct(Company company, Product product, Store store) throws ServiceException;

    public NewProductBatch getERPProductBatch(String productId, String storeId, String locationId, String rowId, String rackId, String binId, String batchName) throws ServiceException;

    public NewProductBatch getERPProductBatch(Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName) throws ServiceException;

    public NewBatchSerial getERPBatchSerial(Product product, NewProductBatch productBatch, String serialName) throws ServiceException;
    
    public boolean isSerialExists(Product product, String batchName, String serialName) throws ServiceException;

    public boolean isSerialExists(Product product, String batchName, String serialName, String module,String documentID) throws ServiceException;

    public boolean isSKUExists(Product product, String batchName, String skuName) throws ServiceException;

    public boolean isAnyStockLiesInStore(Company company, Boolean isQAOrRepair) throws ServiceException;

    public NewProductBatch getBatchDataByProductBatchName(Product product, String batchName) throws ServiceException;

    public NewBatchSerial getSerialDataBySerialName(Product product, NewProductBatch productBatch, String serialName) throws ServiceException;

    public NewBatchSerial getSerialDataBySerialName(Product product, String serialName) throws ServiceException;

    public NewBatchSerial getSkuBySerialName(String product, String serialName, String company) throws ServiceException;

    public List<Stock> getBatchSerialListByStoreProductLocation(Product product, Set<Store> store, Location location, Paging paging) throws ServiceException;

    public Map<String, NewProductBatch> getBatchListByStoreProductLocation(Product product, Set<Store> store, Location location, Paging paging,boolean  isEdit) throws ServiceException;

    public List<Stock> getBatchSerialListByStoreProductLocation(String productId, Set<Store> store, Location location, Paging paging,String batchName,boolean  isEdit) throws ServiceException;

    public Map<String, String> getSerialListByStoreProductLocation(Product product, Set<Store> store, Location location, Paging paging) throws ServiceException;

    public List<Stock> getStockByProductBatch(Product product, String batchName, String searchString, Paging paging) throws ServiceException;

    public Map<Product, Double> getDateWiseStockList(Company company, Set<Store> storeSet, Location location, Date date, String searchString, Paging paging) throws ServiceException;

    public List getDateWiseStockInventory(Company company, Set<Store> storeSet, Location location, Date date, String searchString, Paging paging) throws ServiceException;

    public Map<String, Object[]> getDateWiseStockDetailList(Company company, Set<Store> storeSet, Location location, Date date, boolean outData, String searchString, Paging paging) throws ServiceException;

    public Map<String, Product> getProductDetailsForDateWiseStock(Company company, Set<Store> storeSet, Location location, Date date, String searchString, Paging paging) throws ServiceException;

    public Map<String, Object[]> getDateWiseStockDetailListForProduct(Product product, Set<Store> storeSet, Location location, Date date, boolean outData, String searchString, Paging paging) throws ServiceException;

    public Map<String, Object[]> getDateWiseStockDetailListForProduct(Product product, Set<Store> storeSet, Location location, Date fromDate, Date toDate, boolean outData, String searchString, Paging paging) throws ServiceException;

    public Map<String, Stock> getPendingApprovalStock(Product product, Store store) throws ServiceException;

    public List<Stock> getReorderReportList(Company company, Set<Store> storeSet, String searchString, boolean isBelowReorderFilter, Paging paging) throws ServiceException;

    public Map<String, Double> getProductBlockedQuantity(Company company, Store store, Location location, String searchString) throws ServiceException;

    public Map<String, Double> getProductBlockedQuantityWithInStore(Company company, Store store, String searchString) throws ServiceException;

    public List<Stock> getBatchSerialListByStore(Store store) throws ServiceException;

    public List<TransactionModule> getStockForPendingApprovalSerial(String productId, String batchName, String serialName) throws ServiceException;

    public List<TransactionModule> getStockForPendingRepairSerial(String productId, String batchName, String serialName) throws ServiceException;

    public List<Stock> getStockByProductBatchStore(Product product, Store store, String batchName, String serialName) throws ServiceException;

    public Map<String, String> getSerialSkuMap(String productId, String batchName, String[] serialArray) throws ServiceException;

    public NewProductBatch getERPProductBatch(String product, String store, String location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName, String companyId) throws ServiceException;

    public NewBatchSerial getERPBatchSerial(String product, NewProductBatch productBatch, String serialName, String companyId) throws ServiceException;

    public Map<String, Double> getAvailableQuantity(Company company, boolean includeLocation, StringBuilder productIds, boolean isExport) throws ServiceException;

    public List getAssetDetailList(HashMap<String, Object> jsonRequestParam, Paging paging);

    public List getAssetStockDetailList(HashMap<String, Object> jsonRequestParam, Paging paging);

    public HashMap<String, String> getCustomerAddress(String companyId);

    public KwlReturnObject getGRODetailISTMapping(JSONObject json) throws ServiceException;
    
    public KwlReturnObject getWOCDetailISTMapping(JSONObject json) throws ServiceException;

    public String getGoodsReceiptOrderNumberUsingMapping(JSONObject json) throws ServiceException;
    
    public String getWorkOrderNumberUsingMapping(JSONObject json) throws ServiceException;

    public KwlReturnObject getDODetailISTMapping(JSONObject json) throws ServiceException;

    public String getDeliveryOrderNumberUsingMapping(JSONObject json) throws ServiceException;

    public void updateDeliveryOrderStatus(JSONObject params) throws ServiceException;

    public KwlReturnObject getDODQCISTMapping(JSONObject json) throws ServiceException;

    public KwlReturnObject getRejectedDODQCISTMapping(JSONObject json) throws ServiceException;

//    public KwlReturnObject saveOrUpdateInspectionForm(JSONObject params) throws ServiceException;

//    public KwlReturnObject deleteInspectionFormDetails(String InspectionFormId) throws ServiceException;

//    public KwlReturnObject saveInspectionFormDetails(HashMap<String, Object> inspectionFormDetailsMap) throws ServiceException;

    public boolean isSerialExistsinDO(Product product, String batchName, String serialName) throws ServiceException;

    public List getAllAssetStockDetailList(HashMap<String, Object> jsonRequestParam, Paging paging);
    public List getERPSerialFromBatch(String companyid, String productid, String warehouse, String location, String row, String rack, String bin, String batchName, String serialName) throws ServiceException;

    public List getbatchserialdetails(HashMap<String, String> batchserialMap,TransactionModule transModule,Product product) throws ServiceException;
    
    public List<String> getbatchidFromDocumentId(String documentId) throws ServiceException;
    
    public List<Object[]> getWarehouseWiseProductStockList(Company company, Set<InventoryWarehouse> storeSet, InventoryLocation location, String searchString, Paging paging, String inventoryCatType) throws ServiceException;
}
