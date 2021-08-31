/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.store;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.InventoryWarehouse;
import com.krawler.common.admin.StoreMaster;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Paging;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.SessionException;

/**
 *
 * @author Vipin Gupta
 */
public interface StoreService {

    public StoreType getStoreType(int ordinal);

    public InventoryWarehouse getERPWarehouse(String storeId) throws ServiceException;

    public Store getStoreById(String storeId) throws ServiceException;

    public Store getDefaultStore(Company company) throws ServiceException;

    public Store getStoreByAbbreviation(Company company, String abbreviation) throws ServiceException;

    public void addStore(User user, Store store) throws ServiceException, SessionExpiredException;

    public void updateStore(User user, Store store) throws ServiceException;

    public void removeStore(User user, Store store) throws ServiceException;

    public void activateStore(User user, Store store) throws ServiceException;

    public void deactivateStore(User user, Store store) throws ServiceException;

    public List<Store> getStores(Company company, String searchString, Paging paging) throws ServiceException;

    public List<Store> getStores(Company company, Boolean isActive, String searchString, Paging paging) throws ServiceException;

    public List<Store> getStoresByTypes(Company company, Boolean isActive, StoreType[] storeTypes, String searchString, Paging paging, boolean isForAvailableWarehouse, boolean includeQAAndRepairStore,boolean includePickandPackStore) throws ServiceException;

    public List<Store> getStoresByStoreManagers(User user, Boolean isActive, StoreType[] storeTypes, String searchString, Paging paging) throws ServiceException;

    public List<Store> getStoresByStoreExecutives(User user, Boolean isActive, StoreType[] storeTypes, String searchString, Paging paging) throws ServiceException;

    public List<Store> getStoresByStoreExecutivesAndManagers(User user, Boolean isActive, StoreType[] storeTypes, String searchString, Paging paging, boolean includeQAAndRepairStore,boolean includePickandPackStore) throws ServiceException;
    
    public List<Store> getStoresByUser(String userId, Boolean isActive, StoreType[] storeTypes, boolean excludeQARepair, String searchString, Paging paging ,boolean includePickandPackStore) throws ServiceException;

    public List<InventoryWarehouse> getStoresByQAPerson(User user) throws ServiceException;

    public String getMovementTypeName(String movementTypeId) throws ServiceException;
    
    public String getUnitNameFromWarehouseid(String warehouseid) throws ServiceException;

    public KwlReturnObject getInventoryProductDetails(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getInventoryBatchDetailsid(Map<String, Object> requestParams) throws ServiceException;

    public StoreMaster getStoreMaster(String storeMasterId) throws ServiceException;

    public StoreMaster getStoreMasterByName(String name, String company, int type) throws ServiceException;

    public Set<User> saveStoreManagerMapping(String companyId, String[] agents) throws ServiceException;

    public Set<Store> getStoreMapping(String companyId, String[] storeIds) throws ServiceException;
    
    public JSONObject getStoreJson(Store store) throws JSONException, ServiceException;

    public double getProductQuantityUnderParticularStore(String storeID,String company) throws ServiceException;
    
    public double getQuantityPendingUnderParticularStore(String storeID,String company) throws ServiceException;
    
    public List<Object> getTransactionCountForStoreId(String storeid,String companyid,boolean isForStockRequest) throws ServiceException;
    
    public List<Object> getProductCountForStoreId(String storeid, String companyid) throws ServiceException;

    public List<InventoryWarehouse> getWarehouseByStoreExecutivesAndManagers(User user, Boolean isActive, Set<Store> storeTypes) throws ServiceException;
}
