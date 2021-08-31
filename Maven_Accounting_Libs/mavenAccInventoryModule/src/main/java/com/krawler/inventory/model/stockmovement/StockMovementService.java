/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockmovement;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.NewBatchSerial;
import com.krawler.common.admin.StoreMaster;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.store.Store;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
public interface StockMovementService {

    public void addOrUpdateBulkStockMovement(Company company, String moduleRefId, List<StockMovement> stockMovementList) throws ServiceException;
    
    public void addOrUpdateBulkStockMovement(Company company, String moduleRefId, List<StockMovement> stockMovementList,boolean isRemoveModuleRefId) throws ServiceException;

    public void addStockMovement(StockMovement stockMovement) throws ServiceException;

    public void removeStockMovement(StockMovement stockMovement) throws ServiceException;

    public void removeStockMovementByReferenceId(Company company, String referenceId) throws ServiceException;

    public List<StockMovement> getStockMovementListByReferenceId(Company company, String referenceId) throws ServiceException;
    
    public List<StockMovement> getStockMovementListOfISTReturnRequest(Company company, String referenceId) throws ServiceException;
    
    public List<StockMovement> getStockMovementListById(Company company, String referenceId) throws ServiceException;
    
    public List<StockMovement> getStockMovementList(Company company, Store store, Date fromDate, Date todate, TransactionType transactionType, String searchString, Paging paging) throws ServiceException;
    
    public List<StockMovementDetail> getDetailedStockMovementList(Company company, Store store,Location location,Date fromDate, Date todate, TransactionType transactionType, String searchString, Paging paging,String productId) throws ServiceException;

    @Deprecated
    public void stockMovementInERP(boolean addOperation, Product product, Store fromStore, Location fromLocation, String batchName, String serialNames, double quantity) throws ServiceException;
    /**
     * this method is used only for return transaction
     * @param addOperation
     * @param product
     * @param fromStore
     * @param fromLocation
     * @param batchName
     * @param serialNames
     * @param quantity
     * @param isReturnTransaction  (if isReturntTransaction is false ,then both quantity and quantitydue of newproductbatch will get updated.other wise  only quantiydue will be updated)
     * @throws ServiceException 
     */
    @Deprecated
    public void stockMovementInERP(boolean addOperation, Product product, Store fromStore, Location fromLocation, String batchName, String serialNames, double quantity,boolean isReturnTransaction) throws ServiceException;
    
    public void stockMovementInERP(boolean addOperation, Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName, String serialNames, double quantity,boolean isReturnTransaction) throws ServiceException;
    
    public void updateSkuFields(Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName, String serialNames, String assetIds) throws ServiceException;

    public void updateProductIssueCount(Product product, double newCount) throws ServiceException;

    public Map<String, Integer> getTransactionWiseSerialUsedCount(String moduleRefId, Set<NewBatchSerial> batchSerialSet) throws ServiceException;
    
    public Store getOriginalStoreFromQAStore(String transactionNo, Product product) throws ServiceException;

    public List<Product> getAllProductsForStockMovement(Company company, Store store, Date fromDate, Date toDate, TransactionType transactionType, String searchString, Paging paging) throws ServiceException;
    
    public Map<Product, List<StockMovement>> getStockMovementByProductList(Company company, List<Product> productList, Store store, Date fromDate, Date toDate, TransactionType transactionType, String searchString) throws ServiceException;

    public void updateDOMemo(String doId,String memo)throws ServiceException;
    
    public String getDOdetailsremarks(String id,String transactionmodule) throws ServiceException;
    
    public void addOrUpdateBulkStockMovementForWorkOrder(Company company, String modulerefdetailid, List<StockMovement> stockMovementList) throws ServiceException;
    
    public void addOrUpdateBulkStockMovementForWorkOrder(Company company, String modulerefdetailid, List<StockMovement> stockMovementList,boolean isRemovemodulerefdetailid) throws ServiceException;
    
    public List<StockMovement> getStockMovementListByReferenceIdForWorkOrder(Company company, String modulerefdetailid) throws ServiceException;
   
    public void removeStockMovementByReferenceIdForWorkOrder(Company company, String modulerefdetailid) throws ServiceException;
    
    public List<StockMovement> getStockMovementListOfSRReturnRequest(Company company, String referenceId) throws ServiceException;
}
