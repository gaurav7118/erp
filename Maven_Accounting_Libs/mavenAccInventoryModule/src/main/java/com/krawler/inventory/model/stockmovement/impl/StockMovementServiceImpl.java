/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockmovement.impl;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.*;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.spring.authHandler.authHandler;
import java.util.*;

/**
 *
 * @author Vipin gupta
 */
public class StockMovementServiceImpl implements StockMovementService {

    private StockMovementDAO stockMovementDAO;
    private StockService stockService;
    private StoreService storeService;
    private LocationService locationService;

    public void setStockMovementDAO(StockMovementDAO stockMovementDAO) {
        this.stockMovementDAO = stockMovementDAO;
    }

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    @Override
    public void addOrUpdateBulkStockMovement(Company company, String moduleRefId, List<StockMovement> stockMovementList) throws ServiceException {
        addOrUpdateBulkStockMovement(company, moduleRefId, stockMovementList, true);
    }
    
    @Override
    public void addOrUpdateBulkStockMovement(Company company, String moduleRefId, List<StockMovement> stockMovementList,boolean isRemoveModuleRefId) throws ServiceException {
        if (stockMovementList == null || stockMovementList.isEmpty()) {
            throw new InventoryException(InventoryException.Type.NULL, "Stock Movement entry list is null or empty");
        }
        if(isRemoveModuleRefId){
            removeStockMovementByReferenceId(company, moduleRefId);
        }
        for (StockMovement sm : stockMovementList) {
            addStockMovement(sm);
        }
    }
    
    @Override
    public void addOrUpdateBulkStockMovementForWorkOrder(Company company, String modulerefdetailid, List<StockMovement> stockMovementList) throws ServiceException {
        addOrUpdateBulkStockMovementForWorkOrder(company, modulerefdetailid, stockMovementList, true);
    }
    
    @Override
    public void addOrUpdateBulkStockMovementForWorkOrder(Company company, String modulerefdetailid, List<StockMovement> stockMovementList,boolean isRemovemodulerefdetailid) {
        try{
        if (stockMovementList == null || stockMovementList.isEmpty()) {
            throw new InventoryException(InventoryException.Type.NULL, "Stock Movement entry list is null or empty");
        }
        if(isRemovemodulerefdetailid){
            //removeStockMovement by using modulerefdetailid
            removeStockMovementByReferenceIdForWorkOrder(company, modulerefdetailid);
        }
        for (StockMovement sm : stockMovementList) {
            addStockMovement(sm);
        }
        }
        catch(Exception e){
        throw new InventoryException(InventoryException.Type.NULL, "Stock Movement entry list is null or empty");
        }
    }

    @Override
    public void addStockMovement(StockMovement stockMovement) throws ServiceException {
        if (stockMovement == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Stock Movement entry is null or empty");
        }
        if (stockMovement.getStore() == null) {
            stockMovement.setStore(storeService.getDefaultStore(stockMovement.getCompany()));
        }
        if (stockMovement.getProduct().getUnitOfMeasure() != null) {
            stockMovement.setStockUoM(stockMovement.getProduct().getUnitOfMeasure());
        }
        stockMovementDAO.saveOrUpdate(stockMovement);
        updateStockInventoryFromERP(true, stockMovement);
    }

       @Override
    public void removeStockMovementByReferenceId(Company company, String moduleRefId) throws ServiceException {
        List<StockMovement> smList = getStockMovementListByReferenceId(company, moduleRefId);
        for (StockMovement sm : smList) {
            removeStockMovement(sm);
        }
    }
       
    @Override
    public void removeStockMovementByReferenceIdForWorkOrder(Company company, String modulerefdetailid) throws ServiceException {
        List<StockMovement> smList = getStockMovementListByReferenceIdForWorkOrder(company, modulerefdetailid);
        if (!smList.isEmpty()) {
            for (StockMovement sm : smList) {
                removeStockMovement(sm);
            }
        }
    }

    @Override
    public void removeStockMovement(StockMovement stockMovement) throws ServiceException {
        if (stockMovement == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Stock Movement entry is null or empty");
        }
        stockMovementDAO.delete(stockMovement);
        updateStockInventoryFromERP(false, stockMovement);
    }

    @Override
    public List<StockMovement> getStockMovementListByReferenceId(Company company, String referenceId) throws ServiceException {
        return stockMovementDAO.getStockMovementListByReferenceId(company, referenceId);
    }
    
    public List<StockMovement> getStockMovementListOfISTReturnRequest(Company company, String referenceId) throws ServiceException {
        return stockMovementDAO.getStockMovementListOfISTReturnRequest(company, referenceId);
    }
    @Override
    public List<StockMovement> getStockMovementListOfSRReturnRequest(Company company, String referenceId) throws ServiceException {
        return stockMovementDAO.getStockMovementListOfSRReturnRequest(company, referenceId);
    }
    @Override
    public List<StockMovement> getStockMovementListById(Company company, String referenceId) throws ServiceException {
        return stockMovementDAO.getStockMovementListById(company, referenceId);
    }
    
     @Override
    public List<StockMovement> getStockMovementListByReferenceIdForWorkOrder(Company company, String modulerefdetailid) throws ServiceException {
        return stockMovementDAO.getStockMovementListByReferenceIdForWorkOrder(company, modulerefdetailid);
    }

    @Override
    public List<StockMovement> getStockMovementList(Company company, Store store, Date fromDate, Date todate, TransactionType transactionType, String searchString, Paging paging) throws ServiceException {
        return stockMovementDAO.getStockMovementList(company, store, fromDate, todate, transactionType, searchString, paging);
    }

    @Override
    public List<StockMovementDetail> getDetailedStockMovementList(Company company, Store store,Location location, Date fromDate, Date todate, TransactionType transactionType, String searchString, Paging paging,String productId) throws ServiceException {
        return stockMovementDAO.getDetailedStockMovementList(company, store, location,fromDate, todate, transactionType, searchString, paging,productId);
    }

    private void updateStockInventoryFromERP(boolean addAction, StockMovement sm) throws ServiceException {
        TransactionModule tm = sm.getTransactionModule();
        if (tm == TransactionModule.ERP_GRN || tm == TransactionModule.ERP_DO || tm == TransactionModule.ERP_Consignment_DO || tm == TransactionModule.ERP_PURCHASE_RETURN || tm == TransactionModule.ERP_SALES_RETURN || tm == TransactionModule.ERP_PRODUCT || tm == TransactionModule.PRODUCT_BUILD_ASSEMBLY || tm == TransactionModule.PRODUCT_UNBUILD_ASSEMBLY ||tm == TransactionModule.Work_Order) { /* for Work Order increase or decrease quantity of Assembly product/Inventory part product  */
            TransactionType tt = sm.getTransactionType();
            for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                if (addAction) { // add stock movement
                    if (tt == TransactionType.OPENING || tt == TransactionType.IN) {
                        stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                    } else if (tt == TransactionType.OUT) {
                        stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                    }
                } else { // delete stock movement
                    if (tt == TransactionType.OPENING || tt == TransactionType.IN) {
                        stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                    } else if (tt == TransactionType.OUT) {
                        stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                    }
                }
            }
        }
    }

    @Override
    public void stockMovementInERP(boolean addOperation, Product product, Store store, Location location, String batchName, String serialNames, double quantity) throws ServiceException {
        stockMovementInERP(addOperation, product, store, location, batchName, serialNames, quantity, false);
    }

    @Override
    public void stockMovementInERP(boolean addOperation, Product product, Store store, Location location, String batchName, String serialNames, double quantity, boolean isReturnTransaction) throws ServiceException {
        stockMovementInERP(addOperation, product, store, location, null, null, null, batchName, serialNames, quantity, isReturnTransaction);
    }

    @Override
    public void stockMovementInERP(boolean addOperation, Product product, Store store, Location location, StoreMaster row, 
    StoreMaster rack, StoreMaster bin, String batchName, String serialNames, double quantity, boolean isReturnTransaction) throws ServiceException {
        quantity = Math.abs(quantity);
        if (StringUtil.isNullOrEmpty(batchName)) {
            batchName = "";
        }
        NewProductBatch productBatch = stockService.getERPProductBatch(product, store, location, row, rack, bin, batchName);
        if (addOperation) {
            if (productBatch == null) {
                productBatch = new NewProductBatch();
                String newitemID = UUID.randomUUID().toString();
                productBatch.setId(newitemID);
                productBatch.setCompany(product.getCompany());
                productBatch.setProduct(product.getID());
                if (store != null) {
                    InventoryWarehouse warehouse = storeService.getERPWarehouse(store.getId());
                    productBatch.setWarehouse(warehouse);
                }
                if (location != null) {
                    InventoryLocation il = locationService.getERPLocation(location.getId());
                    productBatch.setLocation(il);
                }
                productBatch.setRow(row);
                productBatch.setRack(rack);
                productBatch.setBin(bin);
                productBatch.setBatchname(batchName);

                NewProductBatch dbBatch = stockService.getBatchDataByProductBatchName(product, batchName);
                if (dbBatch != null) {
                    productBatch.setMfgdate(dbBatch.getMfgdate());
                    productBatch.setExpdate(dbBatch.getExpdate());
                }
                productBatch.setTransactiontype(28); // marked as opening to use in all modules
                productBatch.setIspurchase(true); // marked as purchase to use in all modules

            }
            //if stock is any return stock then do not update its quantity.else if it is new stock then add quantitu
            if (!isReturnTransaction) {
                productBatch.setQuantity(authHandler.roundQuantity(productBatch.getQuantity() + quantity, product.getCompany().getCompanyID()));
            }
            productBatch.setQuantitydue(authHandler.roundQuantity((productBatch.getQuantitydue() + quantity),product.getCompany().getCompanyID()));

            stockMovementDAO.saveOrUpdate(productBatch);

            if (!StringUtil.isNullOrEmpty(serialNames)) {
                String[] serialArray = serialNames.split(",");
                for (String serialName : serialArray) {
                    serialName=serialName.trim();
                    NewBatchSerial batchSerial = stockService.getERPBatchSerial(product, productBatch, serialName);
                    if (batchSerial == null) {
                        batchSerial = new NewBatchSerial();
                        String newitemID = UUID.randomUUID().toString();
                        batchSerial.setId(newitemID);
                        batchSerial.setBatch(productBatch);
                        batchSerial.setSerialname(serialName);
                        batchSerial.setCompany(product.getCompany());
                        batchSerial.setProduct(product.getID());
                        if(quantity>0){
                        batchSerial.setQuantity(1);
                        } 
                        NewBatchSerial dbSerial = stockService.getSerialDataBySerialName(product, productBatch, serialName);
                        if (dbSerial != null) {
                            batchSerial.setExpfromdate(dbSerial.getExpfromdate());
                            batchSerial.setExptodate(dbSerial.getExptodate());
                            batchSerial.setSkufield(dbSerial.getSkufield());
                        } else {
                            NewBatchSerial serialForSku = stockService.getSerialDataBySerialName(product, serialName);
                            if (serialForSku != null) {
                                batchSerial.setExpfromdate(serialForSku.getExpfromdate());
                                batchSerial.setExptodate(serialForSku.getExptodate());
                                batchSerial.setSkufield(serialForSku.getSkufield());
                        }
                        }
                        batchSerial.setTransactiontype(28); // marked as opening to use in all modules
                        batchSerial.setIspurchase(true); // marked as purchase to use in all modules
                    }
                    if (quantity > 0) {
                    batchSerial.setQuantitydue(1);
                    }
                    batchSerial.setLockquantity(0);
                    stockMovementDAO.saveOrUpdate(batchSerial);
                }
            }
        } else {
            if (productBatch != null) {
                productBatch.setQuantitydue(authHandler.roundQuantity((productBatch.getQuantitydue() - quantity),product.getCompany().getCompanyID()));
                stockMovementDAO.saveOrUpdate(productBatch);

                if (!StringUtil.isNullOrEmpty(serialNames)) {
                    String[] serialArray = serialNames.split(",");
                    for (String serialName : serialArray) {
                        NewBatchSerial batchSerial = stockService.getERPBatchSerial(product, productBatch, serialName);
                        if (batchSerial != null) {
                            batchSerial.setQuantitydue(0);
                            stockMovementDAO.saveOrUpdate(batchSerial);
                        }

                    }
                }
            }
        }
    }
    
    @Override
    public void updateSkuFields(Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName, String serialNames, String skuNames) throws ServiceException {
        if (product.isIsSerialForProduct() && product.isIsSKUForProduct()) {
            NewProductBatch productBatch = stockService.getERPProductBatch(product, store, location, row, rack, bin, batchName);
            if (!StringUtil.isNullOrEmpty(serialNames)) {
                String[] serialArray = serialNames.split(",");
                String[] skuNamesArray = skuNames.split(",");

                int i = 0;
                for (String serialName : serialArray) {
                    String skuName = skuNamesArray[i++];
                    NewBatchSerial batchSerial = stockService.getERPBatchSerial(product, productBatch, serialName);
                    batchSerial.setSkufield(skuName);
                    stockMovementDAO.saveOrUpdate(batchSerial);
                }
            }
        }
    }

    @Override
    public void updateProductIssueCount(Product product, double newCount) throws ServiceException {
        if (product != null) {
            double totalIssuedCount = (product.getTotalIssueCount()) + newCount;
            product.setTotalIssueCount(totalIssuedCount);
            stockMovementDAO.saveOrUpdate(product);
        }
    }

    @Override
    public Map<String, Integer> getTransactionWiseSerialUsedCount(String moduleRefId, Set<NewBatchSerial> batchSerialSet) throws ServiceException {
        return stockMovementDAO.getTransactionWiseSerialUsedCount(moduleRefId, batchSerialSet);
    }

    @Override
    public Store getOriginalStoreFromQAStore(String transactionNo, Product product) throws ServiceException {
        return stockMovementDAO.getOriginalStoreFromQAStore(transactionNo, product);
    }

    @Override
    public List<Product> getAllProductsForStockMovement(Company company, Store store, Date fromDate, Date toDate, TransactionType transactionType, String searchString, Paging paging) throws ServiceException {
        return stockMovementDAO.getAllProductsForStockMovement(company, store, fromDate, toDate, transactionType, searchString, paging);
    }

    @Override
    public Map<Product, List<StockMovement>> getStockMovementByProductList(Company company, List<Product> productList, Store store, Date fromDate, Date toDate, TransactionType transactionType,String searchString) throws ServiceException {
        List<StockMovement> list = stockMovementDAO.getStockMovementByProductList(company, productList, store, fromDate, toDate, transactionType,searchString);

        Map<Product, List<StockMovement>> dataMap = new HashMap();
        for (Product product : productList) {
            dataMap.put(product, new ArrayList<StockMovement>());
        }
        for (StockMovement sm : list) {
            dataMap.get(sm.getProduct()).add(sm);
        }
        return dataMap;
    }

    public void updateDOMemo(String doID,String memo) throws ServiceException{
         stockMovementDAO.updateDOMemo(doID, memo);
    }
     
    @Override
    public String  getDOdetailsremarks(String id,String transactionmodule) throws ServiceException {
            return stockMovementDAO.getDOdetailsremarks(id, transactionmodule);
    }
}
