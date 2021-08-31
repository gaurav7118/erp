
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.store.impl;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.model.inspection.TemplateException;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.store.*;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vipin Gupta
 */
public class StoreServiceImpl implements StoreService {

    StoreDAO storeDAO;
    private LocationService locationService;

    public void setStoreDAO(StoreDAO storeDAO) {
        this.storeDAO = storeDAO;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public StoreType getStoreType(int ordinal) {
        StoreType storeType = null;
        for (StoreType st : StoreType.values()) {
            if (st.ordinal() == ordinal) {
                storeType = st;
                break;
            }
        }
        return storeType;
    }

    @Override
    public InventoryWarehouse getERPWarehouse(String storeId) throws ServiceException {
        InventoryWarehouse iw = null;
        if (!StringUtil.isNullOrEmpty(storeId)) {
            iw = storeDAO.getERPWarehouse(storeId);
        }
        return iw;
    }

    @Override
    public Store getStoreById(String storeId) throws ServiceException {
        Store store = null;
        if (!StringUtil.isNullOrEmpty(storeId)) {
            store = storeDAO.getStoreById(storeId);
        }
        return store;
    }

    @Override
    public Store getDefaultStore(Company company) throws ServiceException {
        return storeDAO.getDefaultStore(company);
    }

    @Override
    public Store getStoreByAbbreviation(Company company, String abbreviation) throws ServiceException {
        if (company == null) {
            throw new StorageException(StorageException.Type.NULL, "Company Object is null.");
        }
        if (StringUtil.isNullOrEmpty(abbreviation)) {
            throw new StorageException(StorageException.Type.NULL, "Store abbreviation is null or empty.");
        }
        return storeDAO.getStoreByAbbreviation(company, abbreviation);
    }

    @Override
    public void addStore(User user, Store store) throws ServiceException, SessionExpiredException {
        if (store == null) {
            throw new StorageException(StorageException.Type.NULL, "Store Object is null.");
        }
        if (!isValidStore(store)) {
            throw new StorageException(StorageException.Type.INVALID, "Store Object is invalid.");
        }
        if (isStoreExists(store)) {
            throw new StorageException(StorageException.Type.ALREADY_EXISTS, "Store [" + store.getAbbreviation() + "] is already exists.");
        }
        Company company = store.getCompany();
        Set<User> storeMasters = new HashSet<User>();
        List<Store> stores = getStores(company, true, null, null);
        if (stores.isEmpty()) {
            store.setDefaultStore(true);
        } else {
            store.setDefaultStore(false);
        }
        store.setActive(true);
        store.setCreatedBy(user);
        store.setModifiedBy(user);
        storeMasters.add(user);
        store.setStoreManagerSet(storeMasters);
        store.setStoreExecutiveSet(storeMasters);
        try {
            store.setCreatedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));//Save date in UTC
            store.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));//Save date in UTC
        } catch (ParseException pe) {
            store.setCreatedOn(new Date());
            store.setModifiedOn(new Date());
        }
        Location defaultLocation = locationService.getDefaultLocation(company);
        if (defaultLocation != null) {
            store.getLocationSet().add(defaultLocation);
            store.setDefaultLocation(defaultLocation);
        }

        storeDAO.saveOrUpdate(store);

        InventoryWarehouse iw = new InventoryWarehouse();
        iw.setId(store.getId());
        iw.setName(store.getAbbreviation());
        iw.setCompany(store.getCompany());
        iw.setIsdefault(store.isDefaultStore());
        iw.setParentId(store.getParentId());
        iw.setActive(true);//Default set to true at time creation 
        storeDAO.saveOrUpdate(iw);
    }

    @Override
    public void updateStore(User user, Store store) throws ServiceException {
        try {
            if (store == null) {
                throw new StorageException(StorageException.Type.NULL, "Store Object is null.");
            }
            if (!isValidStore(store)) {
                throw new StorageException(StorageException.Type.INVALID, "Store Object is invalid.");
            }
            Store existingStore = getOtherStoreByAbbreviation(store);
            if (existingStore != null && !(existingStore.getId().equalsIgnoreCase(store.getId()))) {
                throw new StorageException(StorageException.Type.ALREADY_EXISTS, "Store [" + store.getAbbreviation() + "] is already exists.");
            }
            store.setModifiedBy(user);
            store.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            storeDAO.saveOrUpdate(store);

            InventoryWarehouse iw = storeDAO.getERPWarehouse(store.getId());
            if (iw != null) {
                iw.setName(store.getAbbreviation());
                iw.setParentId(store.getParentId());
                iw.setIsdefault(store.isDefaultStore());
                storeDAO.saveOrUpdate(iw);
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StoreServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(StoreServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void removeStore(User user, Store store) throws ServiceException {
        if (store == null) {
            throw new StorageException(StorageException.Type.NULL, "Store Object is null.");
        }
        if (!isValidStore(store)) {
            throw new StorageException(StorageException.Type.INVALID, "Store Object is invalid.");
        }
        storeDAO.delete(store);
    }

    @Override
    public void activateStore(User user, Store store) throws ServiceException {
        try {
            if (store == null) {
                throw new StorageException(StorageException.Type.NULL, "Store Object is null.");
            }
            if (!isValidStore(store)) {
                throw new StorageException(StorageException.Type.INVALID, "Store Object is invalid.");
            }
            store.setActive(true);
            store.setModifiedBy(user);
            store.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            storeDAO.saveOrUpdate(store);

            InventoryWarehouse iw = storeDAO.getERPWarehouse(store.getId());
            if (iw != null) {
                iw.setActive(true);
                storeDAO.saveOrUpdate(iw);
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StoreServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(StoreServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void deactivateStore(User user, Store store) throws ServiceException {
        try {
            if (store == null) {
                throw new StorageException(StorageException.Type.NULL, "Store Object is null.");
            }
            if (!isValidStore(store)) {
                throw new StorageException(StorageException.Type.INVALID, "Store Object is invalid.");
            }
            store.setActive(false);
            store.setModifiedBy(user);
            store.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            storeDAO.saveOrUpdate(store);

            InventoryWarehouse iw = storeDAO.getERPWarehouse(store.getId());
            if (iw != null) {
                iw.setActive(false);
                storeDAO.saveOrUpdate(iw);
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StoreServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(StoreServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public List<Store> getStores(Company company, String searchString, Paging paging) throws ServiceException {
        return storeDAO.getStores(company, searchString, paging,false);
    }

    @Override
    public List<Store> getStores(Company company, Boolean isActive, String searchString, Paging paging) throws ServiceException {
        return storeDAO.getStores(company, isActive, null, searchString, paging,false, false,false);
    }

    private boolean isStoreExists(Store store) throws ServiceException {
        Store prevStore = getStoreByAbbreviation(store.getCompany(), store.getAbbreviation());
        if (prevStore != null && !prevStore.equals(store)) {
            return true;
        }
        return false;
    }

    private Store getOtherStoreByAbbreviation(Store store) throws ServiceException {
        if (store == null) {
            throw new StorageException("store cannot be empty");
        }
        if (store.getCompany() == null) {
            throw new StorageException(StorageException.Type.NULL, "Company Object is null.");
        }
        if (StringUtil.isNullOrEmpty(store.getAbbreviation())) {
            throw new StorageException(StorageException.Type.NULL, "Store abbreviation is null or empty.");
        }
        return storeDAO.getOtherStoreByAbbreviation(store);
    }

    private boolean isValidStore(Store store) throws ServiceException {
        boolean valid = true;
        if (store.getAbbreviation() == null) {
            valid = false;
        } else if (store.getCompany() == null) {
            valid = false;
        } else if (store.getStoreType() == null) {
            valid = false;
        }
        return valid;
    }

    @Override
    public List<Store> getStoresByTypes(Company company, Boolean isActive, StoreType[] storeTypes, String searchString, Paging paging, boolean isForAvailableWarehouse, boolean includeQAAndRepairStore,boolean includePickandPackStore) throws ServiceException {
        return storeDAO.getStores(company, isActive, storeTypes, searchString, paging,isForAvailableWarehouse, includeQAAndRepairStore,includePickandPackStore);
    }

    @Override
    public List<Store> getStoresByStoreManagers(User user, Boolean isActive, StoreType[] storeTypes, String searchString, Paging paging) throws ServiceException {
        return storeDAO.getStoresByStoreManagers(user, isActive, storeTypes, searchString, paging);
    }

    @Override
    public List<Store> getStoresByStoreExecutives(User user, Boolean isActive, StoreType[] storeTypes, String searchString, Paging paging) throws ServiceException {
        return storeDAO.getStoresByStoreExecutives(user, isActive, storeTypes, searchString, paging);
    }

    @Override
    public List<InventoryWarehouse> getStoresByQAPerson(User user) throws ServiceException {
        return storeDAO.getStoresByQAPerson(user);
    }

    @Override
    public List<Store> getStoresByStoreExecutivesAndManagers(User user, Boolean isActive, StoreType[] storeTypes, String searchString, Paging paging,boolean includeQAAndRepairStore,boolean includePickandPackStore) throws ServiceException {
        return storeDAO.getStoresByStoreExecutivesAndManagers(user, isActive, storeTypes, searchString, paging,includeQAAndRepairStore,includePickandPackStore);
    }
    @Override
    public List<InventoryWarehouse> getWarehouseByStoreExecutivesAndManagers(User user, Boolean isActive, Set<Store>  storeTypes) throws ServiceException {
        return storeDAO.getWarehouseByStoreExecutivesAndManagers( user,  isActive,  storeTypes);
    }

    @Override
    public String getMovementTypeName(String movementTypeId) throws ServiceException {
        return storeDAO.getMovementTypeName(movementTypeId);
    }
    
    @Override
    public String getUnitNameFromWarehouseid(String warehouseid) throws ServiceException {
        return storeDAO.getUnitNameFromWarehouseid(warehouseid);
    }

    @Override
    public KwlReturnObject getInventoryProductDetails(Map<String, Object> requestParams) throws ServiceException {
        return storeDAO.getInventoryProductDetails(requestParams);
    }

    @Override
    public KwlReturnObject getInventoryBatchDetailsid(Map<String, Object> requestParams) throws ServiceException {
        return storeDAO.getInventoryBatchDetailsid(requestParams);
    }

    @Override
    public StoreMaster getStoreMaster(String storeMasterId) throws ServiceException {
        StoreMaster storeMaster = null;
        if (!StringUtil.isNullOrEmpty(storeMasterId)) {
            storeMaster = storeDAO.getStoreMaster(storeMasterId);
        }
        return storeMaster;
    }

    @Override
    public StoreMaster getStoreMasterByName(String name, String company, int type) throws ServiceException {
        StoreMaster storeMaster = null;
        if (!StringUtil.isNullOrEmpty(name)) {
            storeMaster = storeDAO.getStoreMasterByName(name, company, type);
        }
        return storeMaster;
    }

//    @Override
    public Set<User> saveStoreManagerMapping(String companyId, String[] agents) throws ServiceException {
        String userIds = "";
        Set<User> userSet = new HashSet<User>();
        List<UserLogin> userlogin = null;
        userlogin = storeDAO.saveStoreManagerMapping(companyId, agents);

        if (userlogin.size() > 0) {
            for (UserLogin usrLoging : userlogin) {
                User user = usrLoging.getUser();
                userSet.add(user);
            }
        }

        return userSet;
    }

    @Override
    public Set<Store> getStoreMapping(String companyId, String[] storeIds) throws ServiceException {
        String userIds = "";
        Set<Store> storeSet = new HashSet<Store>();
        List<Store> stores = null;
        stores = storeDAO.getStoreMapping(companyId, storeIds);

        if (stores.size() > 0) {
            for (Store storeobj : stores) {
                Store store = storeobj;
                storeSet.add(store);
            }
        }
        return storeSet;
    }
    
    @Override
    public List<Store> getStoresByUser(String userId, Boolean isActive, StoreType[] storeTypes, boolean excludeQARepair, String searchString, Paging paging,boolean includePickandPackStore) throws ServiceException{
        return storeDAO.getStoresByUser(userId, isActive, storeTypes, excludeQARepair, searchString, paging,includePickandPackStore);
    }

  
  @Override  
    public JSONObject getStoreJson(Store store) throws JSONException, ServiceException {
        String unitName = getUnitNameFromWarehouseid(store.getId());
        JSONObject jObj = new JSONObject();
        jObj.put("store_id", store.getId());
        jObj.put(Constants.storeid, store.getId());
        jObj.put("abbr", store.getAbbreviation());
        jObj.put("description", store.getDescription());
        jObj.put("fullname", store.getFullName());
        jObj.put("storeName", store.getFullName());
        jObj.put("unitname", unitName);
        jObj.put("name", store.getDescription());
        jObj.put("storetypeid", store.getStoreType().ordinal());
        jObj.put("storetypename", store.getStoreType().toString());
        jObj.put("address", store.getAddress());
        jObj.put("contact", store.getContactNo());
        jObj.put("fax", store.getFaxNo());
        jObj.put("createdBy", store.getCreatedBy().getFirstName());
        jObj.put("createdOn", store.getCreatedOn());
        jObj.put("updatedOn", store.getModifiedOn());
        jObj.put("actstatus", store.isActive());
        jObj.put("ccdateallow", store.isCcDateAllow());
        jObj.put("smccallow", store.isSmccAllow());
        jObj.put("defaultlocation", store.getDefaultLocation() != null ? store.getDefaultLocation().getName() : "");
        jObj.put("defaultlocationid", store.getDefaultLocation() != null ? store.getDefaultLocation().getId() : "");
        jObj.put("parentid", store.getParentId());
        jObj.put("vattinnumber", store.getVATTINnumber());
        jObj.put("csttinnumber", store.getCSTTINnumber());
        Set<Location> locationSet = store.getLocationSet();
        Iterator<Location> itr = locationSet.iterator();
        String locationNames = "", locationIds = "";
        int count = 0;
        while (itr.hasNext()) {
            Location loc = itr.next();
            if (count == 0) {
                locationIds = loc.getId();
                locationNames = loc.getName();
            } else {
                locationIds += "," + loc.getId();
                locationNames += "," + loc.getName();
            }
        }
        jObj.put("locationids", locationIds);
        jObj.put("locations", locationNames);
        Set<String> movementTypeSet = store.getMovementTypeSet();
        String movementTypeId = "";
        for (String mt : movementTypeSet) {
            movementTypeId += mt + ",";
        }
        if (movementTypeId.endsWith(",")) {
            movementTypeId = movementTypeId.substring(0, movementTypeId.length() - 1);
        }
        jObj.put("movementtype", movementTypeId);
        Set<User> user = store.getStoreManagerSet();
        String userNames = "", userIds = "";
        for (User us : user) {
            userNames += us.getFullName() + " ,";
            userIds += us.getUserID() + ",";
        }
        if (userNames.endsWith(",")) {
            userNames = userNames.substring(0, userNames.length() - 1);
        }

        if (userIds.endsWith(",")) {
            userIds = userIds.substring(0, userIds.length() - 1);
        }
        if (userNames.length() > 0) {
           String[] arrOfUserNames = userNames.split(",");
           if(arrOfUserNames.length > 0){
                Arrays.sort(arrOfUserNames);
                userNames = Arrays.toString(arrOfUserNames).substring(1, Arrays.toString(arrOfUserNames).length() - 2);//After using toString() it saperates each element by [', '], so to exclude "[" and "]," substring is used
            }
        }
        
        jObj.put("users", userNames);
        jObj.put("userids", userIds);
        Set<User> executives = store.getStoreExecutiveSet();
        String executiveNames = "", executivesIds = "";
        for (User us : executives) {
            executiveNames += us.getFullName() + " ,";
            executivesIds += us.getUserID() + ",";
        }
        if (executiveNames.endsWith(",")) {
            executiveNames = executiveNames.substring(0, executiveNames.length() - 1);
        }

        if (executivesIds.endsWith(",")) {
            executivesIds = executivesIds.substring(0, executivesIds.length() - 1);
        }
        if (executiveNames.length() > 0) {
           String[] arrOfUserNames = executiveNames.split(",");
           if(arrOfUserNames.length > 0){
                Arrays.sort(arrOfUserNames);
                executiveNames = Arrays.toString(arrOfUserNames).substring(1, Arrays.toString(arrOfUserNames).length() - 2);//After using toString() it saperates each element by [', '], so to exclude "[" and "]," substring is used
            }
        }
        jObj.put("executives", executiveNames);
        jObj.put("executiveids", executivesIds);
        return jObj;
    }
    public double getProductQuantityUnderParticularStore(String storeID,String company) throws ServiceException{
        return storeDAO.getProductQuantityUnderParticularStore(storeID,company);
}
    public double getQuantityPendingUnderParticularStore(String storeID,String company) throws ServiceException{
        return storeDAO.getQuantityPendingUnderParticularStore(storeID,company);
    }
    @Override
    public List<Object> getTransactionCountForStoreId(String storeid, String companyid,boolean isForStockRequest) throws ServiceException {
       return storeDAO.getTransactionCountForStoreId(storeid, companyid,isForStockRequest);
}
    
    @Override
    public List<Object> getProductCountForStoreId(String storeid, String companyid) throws ServiceException {
        return storeDAO.getProductCountForStoreId(storeid, companyid);
}
}
