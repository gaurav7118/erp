/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockmovement;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.NewBatchSerial;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.store.Store;
import java.util.*;

/**
 *
 * @author Vipin Gupta
 */
public interface StockMovementDAO {

    public StockMovement getStockMovement(String stockMovementId) throws ServiceException;

    public void saveOrUpdate(Object object) throws ServiceException;

    public void delete(Object object) throws ServiceException;

    public List<StockMovement> getStockMovementList(Company company, Store store, Date fromDate, Date todate, TransactionType transactionType, String searchString, Paging paging) throws ServiceException;

    public List<StockMovementDetail> getDetailedStockMovementList(Company company, Store store,Location location,Date fromDate, Date todate, TransactionType transactionType, String searchString, Paging paging,String productId) throws ServiceException;

    public List<StockMovement> getStockMovementListByReferenceId(Company company, String moduleRefId) throws ServiceException;
    
    public List<StockMovement> getStockMovementListOfISTReturnRequest(Company company, String moduleRefId) throws ServiceException;
    
    public List<StockMovement> getStockMovementListById(Company company, String moduleRefId) throws ServiceException;
    
    public List<StockMovement> getStockMovementListByReferenceIdForWorkOrder(Company company, String modulerefdetailid) throws ServiceException;

    public Map<String, Integer> getTransactionWiseSerialUsedCount(String moduleRefId, Set<NewBatchSerial> batchSerialSet) throws ServiceException;

    public Store getOriginalStoreFromQAStore(String transactionNo, Product product) throws ServiceException;

    public List<Product> getAllProductsForStockMovement(Company company, Store store, Date fromDate, Date toDate, TransactionType transactionType, String searchString, Paging paging) throws ServiceException;

    public List<StockMovement> getStockMovementByProductList(Company company, List<Product> productList, Store store, Date fromDate, Date toDate, TransactionType transactionType, String searchString) throws ServiceException;

    public List<StockMovement> getStockMovementByProduct(Company company, Product product, Store store, Date fromDate, String searchString, String saID) throws ServiceException;
    
     public void updateDOMemo(String doId,String memo)throws ServiceException;

     public String getDOdetailsremarks(String id,String transactionmodule);
     
     public List<StockMovement> getStockMovementByProduct(HashMap<String,Object> mpList) throws ServiceException ;

     public List<StockMovement> getStockMovementListOfSRReturnRequest(Company company, String moduleRefId) throws ServiceException;
}
