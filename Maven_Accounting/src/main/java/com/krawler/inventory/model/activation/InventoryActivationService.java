/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.activation;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.store.Store;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vipin Gupta
 */
public interface InventoryActivationService {

    public List<Product> getInvactivatedInventoryProducts(Company company, String searchString, Paging paging) throws ServiceException;

    public List<Object[]> getInvactivatedInventoryProductsForUI(Company company, String searchString, Paging paging) throws ServiceException;

    public List<Object[]> getAllInTransitTransactionRequests(Company company, Paging paging) throws ServiceException;

    public void updateInventoryTab(Company company, boolean activate);

    public void maintainWarehouseLocationHirarchy(Company company);

    public void activateLocationWarehouseInCompany(User user, boolean activateBatch, boolean activateSerial);

    public void activateLocationWarehouseInProduct(Product product, InventoryWarehouse warehouse, InventoryLocation location);

    public void removeStockDataFromInventoryTables(Company company);

    public void insertSMForOpeningTransactions(Company company, Map<String, Store> storeMap, Map<String, Location> locationMap);

    public void insertSMForGRNTransactions(Company company, Map<String, Store> storeMap, Map<String, Location> locationMap);

    public void insertSMForDOTransactions(Company company, Map<String, Store> storeMap, Map<String, Location> locationMap);

    public void insertSMForPRTransactions(Company company, Map<String, Store> storeMap, Map<String, Location> locationMap);

    public void insertSMForSRTransactions(Company company, Map<String, Store> storeMap, Map<String, Location> locationMap);

    public void insertSMForPBTransactions(Company company, Map<String, Store> storeMap, Map<String, Location> locationMap);

    public void insertSMForPBDTransactions(Company company, Map<String, Store> storeMap, Map<String, Location> locationMap);

    public void insertStockFromSM(Company company, Map<String, Store> storeMap, Map<String, Location> locationMap);

    public InventoryWarehouse getERPWarehouse(Store store) throws ServiceException;

    public Store getDefaultStore(Company company) throws ServiceException;

    public InventoryLocation getERPLocation(Location defaultLocation) throws ServiceException;

    public void sendActivationMail(User user) throws ServiceException;

    public Map<String, Store> getAllStores(Company company) throws ServiceException;

    public Map<String, Location> getAllLocations(Company company) throws ServiceException;
    /**
     * Getting All Row / Rack /  Bin
     * @param company
     * @return
     * @throws ServiceException 
     */
    public Map<String, StoreMaster> getAllStoreMaster(Company company) throws ServiceException;

    public List<Product> getSelectedProducts(List<String> productids) throws ServiceException;

    public void addAuditlog(Map<String, Object> auditParams, String msg) throws ServiceException;

    public void removeRunningStatus(Company company);

    public boolean isActivateDeactivateProcessRunning(Company company) throws ServiceException;

    public void completeInTransitSRRequests(User user, Store qaStore, Store repairStore) throws ServiceException;

    public void completeInTransitISTRequests(User user, Store qaStore, Store repairStore) throws ServiceException;

    public void completeINTransitSARequests(User user, Store qaStore, Store repairStore) throws ServiceException;

    public void sendDeactivationMail(User user) throws ServiceException;

    public Store getQAStore(Company company) throws ServiceException;

    public Store getRepairStore(Company company) throws ServiceException;
}
