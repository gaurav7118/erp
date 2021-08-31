/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stock.impl;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.ValuationMethod;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.model.configuration.InventoryConfigService;
import com.krawler.inventory.model.ist.ISTDetail;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.stock.Stock;
import com.krawler.inventory.model.stock.StockDAO;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.StockMovement;
import com.krawler.inventory.model.stockmovement.StockMovementDetail;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.stockmovement.TransactionType;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.inventory.model.stockout.StockAdjustmentDetail;
import com.krawler.inventory.model.stockrequest.StockRequestDetail;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.product.TransactionBatch;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Map.Entry;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.common.util.Constants;
import com.krawler.inventory.model.location.LocationDAO;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.stockmovement.*;
import com.krawler.inventory.model.stockrequest.StockRequest;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Vipin Gupta
 */
public class StockServiceImpl implements StockService {

      private static final Logger lgr = Logger.getLogger(StockServiceImpl.class.getName());
    private StockDAO stockDAO;
    private InventoryConfigService invConfigService;
    private accProductDAO accProductDAO;
    private StoreService storeService;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private static final DateFormat yyyyMMdd_HIPHON = new SimpleDateFormat("yyyy-MM-dd");
    private AccountingHandlerDAO accountingHandlerDAO;
    private accAccountDAO accAccountDAOobj;
    private LocationService locationService;
    private StockMovementDAO stockMovementDAO;
    
    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }
    
    public void setStockDAO(StockDAO stockDAO) {
        this.stockDAO = stockDAO;
    }

    public void setInvConfigService(InventoryConfigService invConfigService) {
        this.invConfigService = invConfigService;
    }

    public void setAccProductDAO(accProductDAO accProductDAO) {
        this.accProductDAO = accProductDAO;
    }

    public StoreService getStoreService() {
        return storeService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public AccountingHandlerDAO getAccountingHandlerDAOobj() {
        return accountingHandlerDAOobj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    public void setAccCurrencyDAOobj(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setAccAccountDAOobj(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public StockMovementDAO getStockMovementDAO() {
        return stockMovementDAO;
    }

    public void setStockMovementDAO(StockMovementDAO stockMovementDAO) {
        this.stockMovementDAO = stockMovementDAO;
    }

    @Override
    public Inventory updateERPInventory(boolean increase, Date transactionDate, Product product, Packaging packaging, UnitOfMeasure uom, double uomQuantity, String description) throws ServiceException {
        if (product == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Product is null");
        }
        uomQuantity = Math.abs(uomQuantity);
        Inventory inventory = null;
        try {
            boolean updateInventoryFlag = true;
            JSONObject inventoryjson = new JSONObject();
            inventoryjson.put("productid", product.getID());
            inventoryjson.put("quantity", uomQuantity);
            if (uom != null) {
                inventoryjson.put("uomid", uom.getID());
            }
            double baseUomRate = 1;
            if (packaging != null) {
                baseUomRate = packaging.getStockUomQtyFactor(uom);
            }
            inventoryjson.put("baseuomquantity", updateInventoryFlag ? baseUomRate * uomQuantity : 0);
            inventoryjson.put("actquantity", updateInventoryFlag ? 0 : baseUomRate * uomQuantity);
            inventoryjson.put("baseuomrate", baseUomRate);

            inventoryjson.put("invrecord", updateInventoryFlag ? true : false);

            inventoryjson.put("description", description);

            inventoryjson.put("carryin", increase);
            inventoryjson.put("defective", false);
            inventoryjson.put("newinventory", false);
            inventoryjson.put("companyid", product.getCompany().getCompanyID());
            inventoryjson.put("updatedate", transactionDate);
            KwlReturnObject invresult = accProductDAO.addInventory(inventoryjson);
            inventory = (Inventory) invresult.getEntityList().get(0);

        } catch (JSONException ex) {
            Logger.getLogger(StockServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            return inventory;
        }
    }

    public Inventory updateERPInventoryByid(boolean increase, Date transactionDate, Product product, Packaging packaging, UnitOfMeasure uom, double uomQuantity, String description, String inventoryid) throws ServiceException {
        if (product == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Product is null");
        }
        uomQuantity = Math.abs(uomQuantity);
        Inventory inventory = null;
        try {
            boolean updateInventoryFlag = true;
            JSONObject inventoryjson = new JSONObject();
            inventoryjson.put("inventoryid", inventoryid);
            inventoryjson.put("productid", product.getID());
            inventoryjson.put("quantity", uomQuantity);
            if (uom != null) {
                inventoryjson.put("uomid", uom.getID());
            }
            double baseUomRate = 1;
            if (packaging != null) {
                baseUomRate = packaging.getStockUomQtyFactor(uom);
            }
            inventoryjson.put("baseuomquantity", updateInventoryFlag ? baseUomRate * uomQuantity : 0);
            inventoryjson.put("actquantity", updateInventoryFlag ? 0 : baseUomRate * uomQuantity);
            inventoryjson.put("baseuomrate", baseUomRate);

            inventoryjson.put("invrecord", updateInventoryFlag ? true : false);


            inventoryjson.put("description", description);

            inventoryjson.put("carryin", increase);
            inventoryjson.put("defective", false);
            inventoryjson.put("newinventory", false);
            inventoryjson.put("companyid", product.getCompany().getCompanyID());
            inventoryjson.put("updatedate", transactionDate);
            KwlReturnObject invresult = accProductDAO.updateInventory(inventoryjson);
            inventory = (Inventory) invresult.getEntityList().get(0);

        } catch (JSONException ex) {
            Logger.getLogger(StockServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            return inventory;
        }
    }

    private void updateInventory(Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName, String serialNames, double qunatity) throws ServiceException {
        try {
            if (qunatity != 0) {
                Stock stock = getStock(product, store, location, row, rack, bin, batchName);
                if (stock != null) {
                    stock.setQuantity(authHandler.roundQuantity(stock.getQuantity() + qunatity,product.getCompany().getCompanyID()));
                    if (!StringUtil.isNullOrEmpty(serialNames)) {
                        serialNames = serialNames.replaceAll(", ","");
                        String serials = stock.getSerialNames();
                        if (qunatity > 0) {
                            serials = addSerials(serials, serialNames);
                        } else {
                            serials = removeSerials(serials, serialNames);
                        }
                        stock.setSerialNames(serials);
                    }
                } else {
                    stock = new Stock(product, store, location, row, rack, bin, batchName, qunatity);
                    if (qunatity > 0) {
                        stock.setSerialNames(serialNames);
                    }
                    stock.setCreatedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                }
                stock.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                stockDAO.saveOrUpdate(stock);
            }
        } catch (ParseException ex) {
            Logger.getLogger(StockMovement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockMovement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void updateStockForCycleCount(Product product, Store store, Location location, String batchName, String serialNames, double expectedQuantity) throws ServiceException {
//        Stock stock = getStock(product, store, location, batchName);
//        if (stock != null) {
//            stockDAO.delete(stock);
//        }
//        updateInventory(product, store, location, batchName, serialNames, expectedQuantity);
    }

    @Override
    public void increaseInventory(Product product, Store store, Location location, String batchName, String serialNames, double quantity) throws ServiceException {
        increaseInventory(product, store, location, null, null, null, batchName, serialNames, quantity);
    }

    @Override
    public void decreaseInventory(Product product, Store store, Location location, String batchName, String serialNames, double quantity) throws ServiceException {
        decreaseInventory(product, store, location, null, null, null, batchName, serialNames, quantity);
    }

    @Override
    public void increaseInventory(Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName, String serialNames, double quantity) throws ServiceException {
        quantity = Math.abs(quantity);
        updateInventory(product, store, location, row, rack, bin, batchName, serialNames, quantity);
    }

    @Override
    public void decreaseInventory(Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName, String serialNames, double quantity) throws ServiceException {
        quantity = Math.abs(quantity);
        updateInventory(product, store, location, row, rack, bin, batchName, serialNames, -quantity);
    }

    @Override
    public double getProductPurchasePrice(Product product, Date date) throws ServiceException {
        double price = 0;
        KwlReturnObject result = accProductDAO.getProductPrice(product.getID(), true, date, null, null);
        List list = result.getEntityList();
        Iterator itr = list.iterator();
        if (itr.hasNext()) {
            Object row = itr.next();
            if (row != null) {
                price = (Double) row;
            }
        }
        return price;
    }

    @Override
    public double getProductQuantityInStoreLocation(Product product, Store store, Location location) throws ServiceException {
        return stockDAO.getProductQuantityInStoreLocation(product, store, location);
    }

    @Override
    public double getProductQuantityInStore(Product product, Store store) throws ServiceException {
        return stockDAO.getProductTotalQuantityInStore(product, store);
    }

    @Override
    public double getProductTotalAvailableQuantity(Product product, Store store, Location location) throws ServiceException {
        double availableQuantity = 0;

        if (product.isblockLooseSell()) {
            KwlReturnObject qtyResult = accProductDAO.getAvailableQuantityInSelectedUOM(product.getID(), product.getUnitOfMeasure().getID());
            availableQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
        } else {
            availableQuantity = stockDAO.getProductQuantityInStoreLocation(product, store, location);
        }


//            qtyResult = accProductDAO.getLockQuantityInSelectedUOM(product.getID(), product.getUnitOfMeasure().getID());
//            double lockQuantityInSelectedUOM = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);

//            return availableQuantity-lockQuantityInSelectedUOM;
        return availableQuantity;

    }

    @Override
    public double getProductTotalQuantity(Product product) throws ServiceException {
        return stockDAO.getProductTotalQuantity(product);
    }

    @Override
    public double getProductQuantityUnderQA(Product product, String qaStoreId, boolean isRepair) throws ServiceException {
        return stockDAO.getProductQuantityUnderParticularStore(product, qaStoreId, isRepair);
    }

    @Override
    public double getProductQuantityUnderRepair(Product product, String repairStoreId) throws ServiceException {
        return stockDAO.getProductQuantityUnderParticularStore(product, repairStoreId, true);
    }

    @Override
    public Stock getStockById(String stockId) throws ServiceException {
        return stockDAO.getStockById(stockId);
    }

    @Override
    public Stock getStock(Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName) throws ServiceException {
        return stockDAO.getStock(product, store, location, row, rack, bin, batchName);
    }
    @Override
    public Stock getStock(String productId, String storeId, String locationId, String rowId, String rackId, String binId, String batchName) throws ServiceException {
        return stockDAO.getStock(productId, storeId, locationId, rowId, rackId, binId, batchName);
    }

    @Override
    public List<Stock> getBatchWiseStockList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging) throws ServiceException {
        return stockDAO.getBatchWiseStockList(company, storeSet, location, searchString, paging);
    }

    @Override
    public List<Stock> getStoreWiseStockList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging, String inventoryCatType) throws ServiceException {
        return stockDAO.getStoreWiseStockList(company, storeSet, location, searchString, paging, inventoryCatType);
    }

    @Override
    public List<Object[]> getStoreWiseProductStockList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging, String inventoryCatType) throws ServiceException {
        return stockDAO.getStoreWiseProductStockList(company, storeSet, location, searchString, paging, inventoryCatType);
    }
    @Override
    public List<Object[]> getWarehouseWiseProductStockList(Company company, Set<InventoryWarehouse> storeSet, InventoryLocation location, String searchString, Paging paging, String inventoryCatType) throws ServiceException {
        return stockDAO.getWarehouseWiseProductStockList(company, storeSet, location, searchString, paging, inventoryCatType);
    }

    @Override
    public List<Object[]> getStoreWiseDetailedStockList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging,String productId) throws ServiceException {
        return stockDAO.getStoreWiseDetailedStockList(company, storeSet, location, searchString, paging,productId);
    }
    
    @Override
    public List<Object[]> getStoreWiseProductStockSummaryList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging,String inventoryCatType) throws ServiceException {
        return stockDAO.getStoreWiseProductStockSummaryList(company, storeSet, location, searchString, paging, inventoryCatType);
    }

    @Override
    public List<NewBatchSerial> getProductSerialList(String productid, String companyid, Set<Store> storeSet, String locationId, String storeId, String searchString, Paging paging) throws ServiceException {
        return stockDAO.getProductSerialList(productid, companyid, storeSet, locationId, storeId, searchString, paging);
    }

    @Override
    public void updateObject(Object object) throws ServiceException {
        stockDAO.updateObject(object);
    }

    @Override
    public Map<String, Double> getAvailableQuantityForAllProductByStore(Company company) throws ServiceException {
        return stockDAO.getAvailableQuantity(company, false);
    }

    @Override
    public Map<String, Double> getAvailableQuantityForAllProductByStoreLocation(Company company) throws ServiceException {
        return stockDAO.getAvailableQuantity(company, true);
    }

    @Override
    public List<NewProductBatch> getERPActiveBatchList(Product product, Store store) throws ServiceException {
        return stockDAO.getERPActiveBatchList(product, store, null);
    }

    @Override
    public List<NewProductBatch> getERPActiveBatchList(Product product, Store store, Location location) throws ServiceException {
        return stockDAO.getERPActiveBatchList(product, store, location);
    }

    @Override
    public Map<String,String> getERPBatchListWithExpiryDate(Product product, Store store) throws ServiceException {
        Map<String,String> map=new HashMap<>();
        List<Object[]> resObj=stockDAO.getERPBatchListWithExpiryDate(product, store);
        if(resObj != null && resObj.size() > 0){
            for(Object[] ob : resObj){
                if(ob!=null){
                    String mapKey="";
                    mapKey+=(ob[1] != null ? ob[1].toString():"");
                    mapKey+=(ob[2].toString());
                    mapKey+=(ob[3].toString());
                    mapKey+=(ob[4].toString());
                    mapKey+=(ob[5] != null ? ob[5].toString():"");
                    map.put(mapKey,ob[6].toString());
                }
            }
        }
        return map;
    }
    
    @Override
    public List<NewBatchSerial> getERPActiveSerialList(Product product, NewProductBatch productBatch, boolean checkQAReject) throws ServiceException {
        return stockDAO.getERPActiveSerialList(product, productBatch, checkQAReject);
    }

    @Override
    public List<NewBatchSerial> getERPActiveSerialList(Product product, Store store, Location location, String batchName, boolean checkQAReject) throws ServiceException {
        List<NewBatchSerial> batchSerials = new ArrayList<NewBatchSerial>();
        NewProductBatch productBatch = getERPProductBatch(product, store, location, batchName);
        if (productBatch != null) {
            batchSerials = stockDAO.getERPActiveSerialList(product, productBatch, checkQAReject);
        }
        return batchSerials;
    }

    @Override
    public List<String> getProductBatchWiseSerialList(Product product, String batchName) throws ServiceException {
        return stockDAO.getProductBatchWiseSerialList(product.getCompany(), product, batchName);
    }

    private String addSerials(String target, String serialNames) {
        if (!StringUtil.isNullOrEmpty(serialNames)) {
            if (StringUtil.isNullOrEmpty(target)) {
                target = serialNames;
            } else {
                Set<String> targetSet = new HashSet<String>(Arrays.asList(target.split(",")));
                Set<String> givenSerialSet = new HashSet<String>(Arrays.asList(serialNames.split(",")));
                targetSet.addAll(givenSerialSet);
                target = "";
                for (String ts : targetSet) {
                    if (StringUtil.isNullOrEmpty(target)) {
                        target = ts;
                    } else {
                        target += "," + ts;
                    }
                }
            }
        }
        return target;
    }

    private String removeSerials(String target, String givenSerialNames) {
        if (!StringUtil.isNullOrEmpty(target) && !StringUtil.isNullOrEmpty(givenSerialNames)) {
            Set<String> tSet = new HashSet<String>(Arrays.asList(target.split(",")));
            Set<String> gSet = new HashSet<String>(Arrays.asList(givenSerialNames.split(",")));
            tSet.removeAll(gSet);
            target = "";
            for (String ts : tSet) {
                if (StringUtil.isNullOrEmpty(target)) {
                    target = ts;
                } else {
                    target += "," + ts;
                }
            }
        }
        return target;
    }

    @Override
    public List<Stock> getStockByStoreProduct(Product product, Store store) throws ServiceException {
        return stockDAO.getStockByStoreProduct(product.getCompany(), product, store);
    }

    @Override
    public NewProductBatch getERPProductBatch(Product product, Store store, Location location, String batchName) throws ServiceException {
        return getERPProductBatch(product, store, location, null, null, null, batchName);
    }

    public NewProductBatch getERPProductBatch(Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName) throws ServiceException {
        return stockDAO.getERPProductBatch(product, store, location, row, rack, bin, batchName);
    }
    
    public NewProductBatch getERPProductBatch(String product, String store, String location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName, String companyId) throws ServiceException {
        return stockDAO.getERPProductBatch(product, store, location, row, rack, bin, batchName, companyId);
    }

    @Override
    public NewBatchSerial getERPBatchSerial(String product, NewProductBatch productBatch, String serialName, String companyId) throws ServiceException {
        return stockDAO.getERPBatchSerial(product, productBatch, serialName, companyId);
    }


    @Override
    public NewBatchSerial getERPBatchSerial(Product product, NewProductBatch productBatch, String serialName) throws ServiceException {
        return stockDAO.getERPBatchSerial(product, productBatch, serialName);
    }

    @Override
    public boolean isSerialExists(Product product, Store store, Location location, String batchName, String serialName) throws ServiceException {
        return stockDAO.isSerialExists(product, batchName, serialName);
    }
    
    @Override
    public boolean isSKUExists(Product product, String batchName, String skuName) throws ServiceException {
        return stockDAO.isSKUExists(product, batchName, skuName);
    }

    @Override
    public boolean isAnyStockLiesInStore(Company company, Boolean isQAOrRepair) throws ServiceException {
        return stockDAO.isAnyStockLiesInStore(company, isQAOrRepair);
    }

    @Override
    public NewProductBatch getBatchDataByProductBatchName(Product product, String batchName) throws ServiceException {
        return stockDAO.getBatchDataByProductBatchName(product, batchName);
    }

    @Override
    public NewBatchSerial getSerialDataBySerialName(Product product, NewProductBatch productBatch, String serialName) throws ServiceException {
        return stockDAO.getSerialDataBySerialName(product, productBatch, serialName);
    }

    @Override
    public NewBatchSerial getSerialDataBySerialName(Product product, String serialName) throws ServiceException {
        return stockDAO.getSerialDataBySerialName(product, serialName);
    }

    @Override
    public String getSkuDataBySerialName(Product product, String serialName) throws ServiceException {
        String skuValues = "";
        String[] serArr = serialName.split(",");
        if (serArr.length > 0) {
            for (int i = 0; i < serArr.length; i++) {
                NewBatchSerial newBatchser = stockDAO.getSerialDataBySerialName(product, serArr[i]);
                if (newBatchser != null) {
                    if (StringUtil.isNullOrEmpty(skuValues)) {
                        skuValues = newBatchser.getSkufield();
                    } else {
                        skuValues += "," + newBatchser.getSkufield();
                    }
                }
            }
        }

        return skuValues;
    }

    @Override
    public List<Stock> getBatchSerialListByStoreProductLocation(Product product, Set<Store> storeSet, Location location) throws ServiceException {
        return stockDAO.getBatchSerialListByStoreProductLocation(product, storeSet, location, null);
    }
    @Override
    public Map<String, NewProductBatch> getBatchListByStoreProductLocation(Product product, Set<Store> storeSet, Location location,boolean isEdit) throws ServiceException {
        return stockDAO.getBatchListByStoreProductLocation(product, storeSet, location, null, isEdit);
    }

    public List<Stock> getBatchSerialListByStoreProductLocation(String productId, Set<Store> storeSet, Location location,String batchName,boolean isEdit) throws ServiceException {
        return stockDAO.getBatchSerialListByStoreProductLocation(productId, storeSet, location, null,batchName,isEdit);
    }

    @Override
    public Map<String, String> getSerialListByStoreProductLocation(Product product, Set<Store> storeSet, Location location) throws ServiceException {
        return stockDAO.getSerialListByStoreProductLocation(product, storeSet, location, null);
    }

    @Override
    public void renameSerialInStock(Product product, String batchName, String oldSerialName, String newSerialName) throws ServiceException {
        if (product == null) {
            throw new InventoryException("Invalid product");
        }
        if (StringUtil.isNullOrEmpty(batchName)) {
            batchName = "";
        }
        List<Stock> stockList = stockDAO.getStockByProductBatch(product, batchName, oldSerialName, null);
        for (Stock stock : stockList) {
            String serialNames = stock.getSerialNames();
            if (!StringUtil.isNullOrEmpty(serialNames)) {
                Set<String> serialSet = new HashSet<String>(Arrays.asList(serialNames.split(",")));
                if (serialSet.contains(oldSerialName)) {
                    stock.removeSerialName(oldSerialName);
                    stock.addSerialName(newSerialName);
                    stockDAO.saveOrUpdate(stock);
                }
            }
        }
    }

    public Store getQaStore(Company company) throws ServiceException {
        KwlReturnObject extracompanyprefObjresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), company.getCompanyID());
        ExtraCompanyPreferences extracompanyobj = (ExtraCompanyPreferences) extracompanyprefObjresult.getEntityList().get(0);
        Store qaStore = storeService.getStoreById(extracompanyobj.getInspectionStore());
        return qaStore;
    }

    public Store getRepairStore(Company company) throws ServiceException {
        KwlReturnObject extracompanyprefObjresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), company.getCompanyID());
        ExtraCompanyPreferences extracompanyobj = (ExtraCompanyPreferences) extracompanyprefObjresult.getEntityList().get(0);
        Store repairStore = storeService.getStoreById(extracompanyobj.getRepairStore());
        return repairStore;
    }
    @Override
    public Store getPackingstore(Company company) throws ServiceException {
        KwlReturnObject extracompanyprefObjresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), company.getCompanyID());
        ExtraCompanyPreferences extracompanyobj = (ExtraCompanyPreferences) extracompanyprefObjresult.getEntityList().get(0);
        Store packingStore = storeService.getStoreById(extracompanyobj.getPackingstore());
        return packingStore;
    }

    public void addStockMovementForQa(StockAdjustmentDetail sad, Store store, TransactionType type, String batch, String Serno, double qty, TransactionModule moduleType, String remark, Location location) throws ServiceException {
        if (sad != null) {
            StockMovement stkmov = new StockMovement();
            Set<StockMovementDetail> dtlList = new HashSet<StockMovementDetail>();
            stkmov.setCompany(sad.getStockAdjustment().getCompany());
            stkmov.setCostCenter(sad.getStockAdjustment().getCostCenter());
            stkmov.setModuleRefId(sad.getStockAdjustment().getId());
            stkmov.setModuleRefDetailId(sad.getStockAdjustment().getId());
            stkmov.setPricePerUnit(sad.getStockAdjustment().getPricePerUnit());
            stkmov.setProduct(sad.getStockAdjustment().getProduct());
            stkmov.setQuantity(qty);
//            stkmov.setCustomer(sad.getStockAdjustment().);
            stkmov.setStockUoM(sad.getStockAdjustment().getUom());
            stkmov.setRemark(remark);
            stkmov.setStore(store);
            stkmov.setTransactionModule(TransactionModule.STOCK_ADJUSTMENT);

            StockMovementDetail stockMovementDtl = new StockMovementDetail();
            stockMovementDtl.setBatchName(batch);
            if (location == null) {
                stockMovementDtl.setLocation(store.getDefaultLocation());
            } else {
                stockMovementDtl.setLocation(location);
            }
            stockMovementDtl.setQuantity(qty);
            stockMovementDtl.setSerialNames(Serno);
            stockMovementDtl.setStockMovement(stkmov);

            dtlList.add(stockMovementDtl);
            stkmov.setStockMovementDetails(dtlList);
            //Created on Date already converted into UTC date.
            stkmov.setTransactionDate(sad.getStockAdjustment().getCreatedOn());
            stkmov.setTransactionType(type);
            stkmov.setTransactionNo(sad.getStockAdjustment().getTransactionNo());

            stockDAO.saveOrUpdate(stkmov);

        }
    }

    public void addBulkStockMovementForQa(StockAdjustment sa, TransactionType transactionType, String remark, Store store) throws ServiceException {
        Inventory inventory = null;
        StockMovement sm = new StockMovement(sa.getProduct(), store, sa.getQuantity(), sa.getPricePerUnit(), sa.getTransactionNo(), sa.getBusinessDate(), transactionType, TransactionModule.STOCK_ADJUSTMENT, sa.getId(), sa.getId());
        sm.setStockUoM(sa.getProduct().getUnitOfMeasure());
        sm.setCostCenter(sa.getCostCenter());
        sm.setRemark(remark);

        Set<StockMovementDetail> smdSet = new HashSet<StockMovementDetail>();
        for (StockAdjustmentDetail sad : sa.getStockAdjustmentDetail()) {

            StockMovementDetail smd = new StockMovementDetail();
            smd.setStockMovement(sm);
            smd.setLocation(store.getDefaultLocation());
            smd.setBatchName(sad.getBatchName());
            smd.setSerialNames(sad.getSerialNames());
            smd.setQuantity(sad.getQuantity());
            if (smd.getQuantity() != 0) {
                smdSet.add(smd);
            }
        }

        if (!smdSet.isEmpty() && smdSet.size() > 0 && sm != null) {
            sm.setStockMovementDetails(smdSet);
            stockDAO.saveOrUpdate(sm);
        }
    }

    public void addStockMovementForQa(ISTDetail stktranfer, Store store, TransactionType type, String batch, String Serno, double qty, TransactionModule moduleType, Company company, Location location) throws ServiceException {
        if (stktranfer != null) {
            String remark = "";
            if (type == TransactionType.IN) {
                remark = "Stock added after QA Inspection";
            } else {
                remark = "Stock sent after QA Inspection";
            }
            double purchasePrice = getProductPurchasePrice(stktranfer.getIstRequest().getProduct(), stktranfer.getIstRequest().getBusinessDate());
            StockMovement stkmov = new StockMovement();
            Set<StockMovementDetail> dtlList = new HashSet<StockMovementDetail>();

            stkmov.setCompany(company);
            stkmov.setModuleRefId(stktranfer.getId());
            stkmov.setModuleRefDetailId(stktranfer.getId());
            stkmov.setProduct(stktranfer.getIstRequest().getProduct());
            stkmov.setPricePerUnit(purchasePrice);
            stkmov.setQuantity(qty);
            stkmov.setRemark(remark);
            stkmov.setStockUoM(stktranfer.getIstRequest().getUom());
            stkmov.setStore(store);
            stkmov.setTransactionDate(stktranfer.getIstRequest().getBusinessDate());
            stkmov.setTransactionModule(moduleType);
            stkmov.setTransactionNo(stktranfer.getIstRequest().getTransactionNo());
            stkmov.setTransactionType(type);

            StockMovementDetail stockDtl = new StockMovementDetail();
            stockDtl.setBatchName(batch);
            if (location == null) {
                stockDtl.setLocation(store.getDefaultLocation());
            } else {
                stockDtl.setLocation(location);
            }
            stockDtl.setQuantity(qty);
            stockDtl.setSerialNames(Serno);
            stockDtl.setStockMovement(stkmov);
            dtlList.add(stockDtl);

            stkmov.setStockMovementDetails(dtlList);

            stockDAO.saveOrUpdate(stkmov);


        }
    }

    @Override
    public void addStockMovementForSRDQa(StockRequestDetail stktr, Store store, TransactionType type, String batch, String Serno, double qty, TransactionModule moduleType, Company company, Location location) throws ServiceException {
        if (stktr != null) {
            String remark = "";
            if (type == TransactionType.IN) {
                remark = "Stock added after QA Inspection";
            } else {
                remark = "Stock sent after QA Inspection";
            }
            double purchasePrice = getProductPurchasePrice(stktr.getStockRequest().getProduct(), stktr.getStockRequest().getBusinessDate());
            StockMovement stkmov = new StockMovement();
            Set<StockMovementDetail> dtlList = new HashSet<StockMovementDetail>();

            stkmov.setCompany(company);
            stkmov.setModuleRefId(stktr.getId());
            stkmov.setModuleRefDetailId(stktr.getId());
            stkmov.setProduct(stktr.getStockRequest().getProduct());
            stkmov.setPricePerUnit(purchasePrice);
            stkmov.setQuantity(qty);
            stkmov.setRemark(remark);
            stkmov.setStockUoM(stktr.getStockRequest().getUom());
            stkmov.setStore(store);
            stkmov.setTransactionDate(stktr.getStockRequest().getBusinessDate());
            stkmov.setTransactionModule(moduleType);
            stkmov.setTransactionNo(stktr.getStockRequest().getTransactionNo());
            stkmov.setTransactionType(type);

            StockMovementDetail stockDtl = new StockMovementDetail();
            stockDtl.setBatchName(batch);
            stockDtl.setLocation(location);
            stockDtl.setQuantity(qty);
            stockDtl.setSerialNames(Serno);
            stockDtl.setStockMovement(stkmov);
            dtlList.add(stockDtl);
            stkmov.setStockMovementDetails(dtlList);

            stockDAO.saveOrUpdate(stkmov);


        }
    }

    @Override
    public Map<Product, Double> getDateWiseStockList(Company company, Set<Store> storeSet, Location location, Date date, String searchString, Paging paging) throws ServiceException {
        return stockDAO.getDateWiseStockList(company, storeSet, location, date, searchString, paging);
    }

    @Override
    public List<Object[]> getDateWiseStockInventory(Company company, Set<Store> storeSet, Location location, Date date, String searchString, Paging paging) throws ServiceException {
        return stockDAO.getDateWiseStockInventory(company, storeSet, location, date, searchString, paging);
    }

    @Override
    public Map<Product, List<Object[]>> getDateWiseStockDetailList(Company company, Set<Store> storeSet, Location location, Date date, String searchString, Paging paging) throws ServiceException {
        Map<String, Product> productMap = stockDAO.getProductDetailsForDateWiseStock(company, storeSet, location, date, searchString, paging);
        Map<String, Object[]> inStockMap = stockDAO.getDateWiseStockDetailList(company, storeSet, location, date, false, searchString, paging);
        Map<String, Object[]> outStockMap = stockDAO.getDateWiseStockDetailList(company, storeSet, location, date, true, searchString, paging);
        Map<Product, List<Object[]>> productStockDetail = new HashMap();
        for (Entry<String, Object[]> inStockEntry : inStockMap.entrySet()) {

            String key = inStockEntry.getKey();
            Object[] inStock = inStockEntry.getValue();
            String inSerials = inStock[7] != null ? (String) inStock[7] : null;
            double inQty = inStock[8] != null ? (Double) inStock[8] : 0;
            Product product = productMap.get(inStock[0]);
            if (outStockMap.containsKey(key)) {
                Object[] outStock = outStockMap.get(key);
                double outQty = outStock[8] != null ? (Double) outStock[8] : 0;
                if (product.isIsSerialForProduct() && !StringUtil.isNullOrEmpty(inSerials)) {
                    String[] serials = inSerials.split(",");
                    outQty = 0;
                    String outSerials = (String) outStock[7];
                    String serialNames = "";
                    
                    List<String> inSerialList= new ArrayList(Arrays.asList(serials));
                    List<String> outSerialList=new ArrayList(Arrays.asList(!StringUtil.isNullOrEmpty(outSerials) ? outSerials.split(",") : new String[0]));
                    
                     Iterator itr=outSerialList.iterator();
                     while (itr.hasNext()) {
                        String s = (String) itr.next();
                        int indx = inSerialList.indexOf(s);
                        if (inSerialList.size() > indx && indx >= 0) {
                            inSerialList.remove(indx);
                        }
                    }
//                    inSerialList.removeAll(outSerialList);
                    serialNames= (inSerialList != null && !inSerialList.isEmpty())  ? StringUtils.join(inSerialList, ",") : "";
                    outQty=inQty-inSerialList.size();
                    
                    inStock[7] = serialNames;
                } else {
                    inStock[7] = null;
                }
                inStock[8] = (inQty - outQty);

            }
            inQty = inStock[8] != null ? (Double) inStock[8] : 0;
            if (inQty != 0) {
                if (productStockDetail.containsKey(product)) {
                    productStockDetail.get(product).add(inStock);
                } else {
                    List list = new ArrayList<>();
                    list.add(inStock);
                    productStockDetail.put(product, list);
                }
            }
        }
        
        if (outStockMap != null || outStockMap.size() != 0) {
            for (Entry<String, Object[]> inStockEntry : outStockMap.entrySet()) {
                String key = inStockEntry.getKey();
                Object[] inStock = inStockEntry.getValue();
                String inSerials = inStock[7] != null ? (String) inStock[7] : null;
                double inQty = 0;
                double outQty = 0;
                Product product = productMap.get(inStock[0]);
                if (!inStockMap.containsKey(key)) {
                    Object[] outStock = outStockMap.get(key);
                    outQty = outStock[8] != null ? (Double) outStock[8] : 0;
                    if (product.isIsSerialForProduct() && !StringUtil.isNullOrEmpty(inSerials)) {
                        String[] serials = inSerials.split(",");
                        outQty = 0;
                        String outSerials = (String) outStock[7];
                        String serialNames = "";

                        List<String> inSerialList = new ArrayList(Arrays.asList(serials));
                        List<String> outSerialList = new ArrayList(Arrays.asList(!StringUtil.isNullOrEmpty(outSerials) ? outSerials.split(",") : new String[0]));
                        inSerialList.removeAll(outSerialList);
                        serialNames = (inSerialList != null && !inSerialList.isEmpty()) ? StringUtils.join(inSerialList, ",") : "";
                        outQty = inQty - inSerialList.size();

                        inStock[7] = serialNames;
                    } else {
                        inStock[7] = null;
                    }
                    inStock[8] = (inQty - outQty);
                    outQty = inStock[8] != null ? (Double) inStock[8] : 0;
                    if (outQty != 0) {
                        if (productStockDetail.containsKey(product)) {
                            productStockDetail.get(product).add(inStock);
                        } else {
                            List list = new ArrayList<>();
                            list.add(inStock);
                            productStockDetail.put(product, list);
                        }
                    }
                }

            }
        }
        return productStockDetail;

    }

    @Override
    public List<Object[]> getDateWiseStockDetailListForProduct(Product product, Set<Store> storeSet, Location location, Date date, String searchString, Paging paging) throws ServiceException {
        return getDateWiseStockDetailListForProduct(product, storeSet, location, null, date, searchString, paging);
    }

    @Override
    public List<Object[]> getDateWiseStockDetailListForProduct(Product product, Set<Store> storeSet, Location location, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException {
        List<Object[]> productStockDetail = new ArrayList<>();
        if (fromDate != null && fromDate.after(toDate)) {
            return productStockDetail;
        }
        Map<String, Object[]> inStockMap = stockDAO.getDateWiseStockDetailListForProduct(product, storeSet, location, fromDate, toDate, false, searchString, paging);
        Map<String, Object[]> outStockMap = stockDAO.getDateWiseStockDetailListForProduct(product, storeSet, location, fromDate, toDate, true, searchString, paging);

        for (Entry<String, Object[]> inStockEntry : inStockMap.entrySet()) {
            String key = inStockEntry.getKey();
            Object[] inStock = inStockEntry.getValue();
            String inSerials = inStock[7] != null ? (String) inStock[7] : null;
            double inQty = inStock[8] != null ? (Double) inStock[8] : 0;
            if (outStockMap.containsKey(key)) {
                Object[] outStock = outStockMap.get(key);
                double outQty = outStock[8] != null ? (Double) outStock[8] : 0;
                if (product.isIsSerialForProduct() && !StringUtil.isNullOrEmpty(inSerials)) {
                    String[] serials = inSerials.split(",");
                    outQty = 0;
                    String outSerials = (String) outStock[7];
                    String serialNames = "";
                    
                    List<String> inSerialList= new ArrayList(Arrays.asList(serials));
                    List<String> outSerialList=new ArrayList(Arrays.asList(!StringUtil.isNullOrEmpty(outSerials) ? outSerials.split(",") : new String[0]));
                    
                    Iterator itr=outSerialList.iterator();
                    while (itr.hasNext()) {
                        String s = (String) itr.next();
                        int indx = inSerialList.indexOf(s);
                        if (inSerialList.size() > indx && indx >= 0) {
                        inSerialList.remove(indx);
                    }
                    }
//                    inSerialList.removeAll(outSerialList);
                    serialNames= (inSerialList != null && !inSerialList.isEmpty())  ? StringUtils.join(inSerialList, ",") : "";
                    outQty=inQty-inSerialList.size();
                    
                    inStock[7] = serialNames;
                } else {
                    inStock[7] = null;
                }
                inStock[8] = (inQty - outQty);

            }
            if ((Double) inStock[8] != 0) {
                productStockDetail.add(inStock);
            }
        }
               
        if (outStockMap != null || outStockMap.size() != 0) {
            for (Entry<String, Object[]> outStockEntry : outStockMap.entrySet()) {
                String key = outStockEntry.getKey();
//                Object[] inStock = outStockEntry.getValue();
//                String inSerials = inStock[7] != null ? (String) inStock[7] : null;
                double inQty = 0;
                double outQty = 0;

                if (!inStockMap.containsKey(key)) {
                    Object[] outStock = outStockMap.get(key);
                    outQty = outStock[8] != null ? (Double) outStock[8] : 0;
                    if (product.isIsSerialForProduct()) {
//                        String[] serials = inSerials.split(",");
                        outQty = 0;
                        String outSerials = (String) outStock[7];
                        String serialNames = "";

//                        List<String> inSerialList = new ArrayList(Arrays.asList(serials));
                        List<String> outSerialList = new ArrayList(Arrays.asList(!StringUtil.isNullOrEmpty(outSerials) ? outSerials.split(",") : new String[0]));
//                        inSerialList.removeAll(outSerialList);
                        serialNames = (outSerialList != null && !outSerialList.isEmpty()) ? StringUtils.join(outSerialList, ",") : "";
                        outQty = inQty - outSerialList.size();

                        outStock[7] = serialNames;
                    } else {
                        outStock[7] = null;
                    }
                    outStock[8] = (inQty - outQty);
                    outQty = outStock[8] != null ? (Double) outStock[8] : 0;
                    if (outQty != 0) {
                        productStockDetail.add(outStock);
                    }
                }

            }
        }
        return productStockDetail;

    }

    @Override
    public Map<String, Object[]> getDateWiseStockDetailListForProduct(Product product, Set<Store> storeSet, Location location, Date fromDate, Date toDate, boolean outData, String searchString, Paging paging) throws ServiceException {
        return stockDAO.getDateWiseStockDetailListForProduct(product, storeSet, location, fromDate, toDate, outData, searchString, paging);
    }

    @Override
    public Map<String, Stock> getPendingApprovalStock(Product product, Store store) throws ServiceException {
        return stockDAO.getPendingApprovalStock(product, store);
    }

    @Override
    public Map<String, Double> getProductQuantityUnderQA(Company company, Store store) throws ServiceException {
        return accProductDAO.getProductQuantityUnderQA(company, store);
    }

    @Override
    public Map<String, Double> getProductQuantityUnderRepair(Company company, Store store) throws ServiceException {
        return accProductDAO.getProductQuantityUnderRepair(company, store);
    }

    @Override
    public List<Stock> getReorderReportList(Company company, Set<Store> storeSet, String searchString, boolean isBelowReorderFilter, Paging paging) throws ServiceException {
        return stockDAO.getReorderReportList(company, storeSet, searchString, isBelowReorderFilter, paging);
    }

    @Override
    public List<Stock> getStockDetailBySerialInOtherStore(Product product, Store store, String batchName, String serialName) throws ServiceException {
        List<Stock> otherStoreStock = new ArrayList();
        if (product.isIsSerialForProduct()) {
            List<Stock> stockList = stockDAO.getStockByProductBatch(product, batchName, serialName, null);
            for (Stock stock : stockList) {
                if (stock.getStore() != store) {
                    String serialNames = stock.getSerialNames();
                    if (!StringUtil.isNullOrEmpty(serialNames)) {
                        List<String> serialNameList = Arrays.asList(serialNames.split(","));
                        if (serialNameList.contains(serialName)) {
                            otherStoreStock.add(stock);
                        }
                    }
                }
            }
        }
        return otherStoreStock;
    }

    @Override
    public List getStockForPendingApprovalSerial(String productId, String batchName, String serialName) throws ServiceException {
        if (batchName == null) {
            batchName = "";
        }
        return stockDAO.getStockForPendingApprovalSerial(productId, batchName, serialName);
    }

    @Override
    public List getStockForPendingRepairSerial(String productId, String batchName, String serialName) throws ServiceException {
        if (batchName == null) {
            batchName = "";
        }
        return stockDAO.getStockForPendingRepairSerial(productId, batchName, serialName);
    }

    @Override
    public List<Stock> getStockDetailBySerial(Product product, String batchName, String serialName) throws ServiceException {
        List<Stock> sameStoreStock = new ArrayList();
        if (product.isIsSerialForProduct()) {
            List<Stock> stockList = stockDAO.getStockByProductBatch(product, batchName, serialName, null);
            for (Stock stock : stockList) {
                String serialNames = stock.getSerialNames();
                if (!StringUtil.isNullOrEmpty(serialNames)) {
                    List<String> serialNameList = Arrays.asList(serialNames.split(","));
                    if (serialNameList.contains(serialName)) {
                        sameStoreStock.add(stock);
                    }
                }
            }
        }
        return sameStoreStock;
    }

    @Override
    public Map<String, Double> getProductBlockedQuantity(Company company, Store store, Location location, String searchString) throws ServiceException {
        return stockDAO.getProductBlockedQuantity(company, store, location, searchString);
    }
    
    @Override
    public Map<String, Double> getProductBlockedQuantityWithInStore(Company company, Store store,String searchString) throws ServiceException {
        return stockDAO.getProductBlockedQuantityWithInStore(company, store, searchString);
    }
    @Override
    public JSONArray getNewStockLedgerJson(HashMap<String, Object> jsonRequestParam, List list, Map<String, Object[]> companyMaxDateProductPriceList, JSONArray valuationArray) throws JSONException, ServiceException, ParseException, SessionExpiredException {
        Iterator itr = list.iterator();
        JSONArray jArr = new JSONArray();
        String pid = "";
        String productidLast = "";
        String productDescLast = "";
        double balance = 0.0, onhand = 0.0, lifo = 0, valuation = 0,totalevaluationcost = 0, totalStockOutQty = 0, totalStockInQty = 0, totalOpeningQty = 0, totalLedgerOnHandQuantity = 0;
        int inIncludeOpening = 0;
        int recCount = 0, lsize = 0;
        List<TransactionBatch> pvbList = new ArrayList();
        String companyid=jsonRequestParam.get("companyid").toString();
        String gcurrencyid=jsonRequestParam.get("gcurrencyid").toString();
        Date StartDate = null, EndDate = null;
        
//        if (request.getAttribute("jasperreport") != null && request.getAttribute("jasperreport").equals("JasperReport")) {//MonthlyTradingAndProfitLoss
//            StartDate = (Date) request.getAttribute("jaspersdate");
//            EndDate = (Date) request.getAttribute("jasperenddate");
//        } else {
//
//            KwlReturnObject result = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
//            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) result.getEntityList().get(0);
//
//            StartDate = null;
//            if (request.getParameter("startdate") == null) {
//                if (request.getParameter("stdate") == null) {
//                    StartDate = companyAccountPreferences.getBookBeginningFrom();
//                } else {
//                    StartDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("stdate"));
//                }
//            } else {
//                StartDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("startdate"));
//            }
//
//            EndDate = null;
//            if (request.getParameter("enddate") == null) {
//                EndDate = new Date();
//            } else {
//                EndDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate"));
//            }
//
//        }
        
        
        if (jsonRequestParam.containsKey("jasperreport") && jsonRequestParam.get("jasperreport").equals("JasperReport")) {
            StartDate = (Date) jsonRequestParam.get("jaspersdate");
            EndDate = (Date) jsonRequestParam.get("jasperenddate");
        } else if (jsonRequestParam.containsKey("PandLClosingStockOnCompare") && Boolean.parseBoolean(jsonRequestParam.get("PandLClosingStockOnCompare").toString())) {// Custom Layout Profit&Loss Closing Stock Valuation for compare predate
            StartDate = ((DateFormat) (jsonRequestParam.get("df"))).parse(jsonRequestParam.get("stpredate").toString());
            EndDate = ((DateFormat) (jsonRequestParam.get("df"))).parse(jsonRequestParam.get("endpredate").toString());
        } else {
            KwlReturnObject result = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) result.getEntityList().get(0);

            StartDate = null;
            if (!jsonRequestParam.containsKey("startdate") || jsonRequestParam.get("startdate") == null) {
                if (!jsonRequestParam.containsKey("stdate") || jsonRequestParam.get("stdate") == null) {
                    StartDate = companyAccountPreferences.getBookBeginningFrom();
                } else {
                    StartDate = ((DateFormat) (jsonRequestParam.get("df"))).parse(jsonRequestParam.get("stdate").toString());
                }
            } else {
                StartDate = ((DateFormat) (jsonRequestParam.get("df"))).parse(jsonRequestParam.get("startdate").toString());
            }

            EndDate = null;
            if (!jsonRequestParam.containsKey("enddate") || jsonRequestParam.get("enddate") == null) {
                EndDate = new Date();
            } else {
                EndDate = ((DateFormat)(jsonRequestParam.get("df"))).parse(jsonRequestParam.get("enddate").toString());
            }
        }
        boolean isActivateLandedInvAmt=false;
        if (jsonRequestParam.containsKey("isactivatelandedinvamt") && jsonRequestParam.get("isactivatelandedinvamt") != null){
            isActivateLandedInvAmt=Boolean.parseBoolean(jsonRequestParam.get("isactivatelandedinvamt").toString());
        }
//        boolean isExport = request.getAttribute("isExportPDF") == null ? false : Boolean.parseBoolean(request.getAttribute("isExportPDF").toString());
        boolean isExport=false;
        if (jsonRequestParam.containsKey("isExportPDF") && jsonRequestParam.get("isExportPDF") != null){
            isExport=Boolean.parseBoolean(jsonRequestParam.get("isExportPDF").toString());
        }
        boolean isInventoryValuation = false;                //Inventory Valuation Report in  Product Master
        boolean isFromStockReport = false;                   //Stock Report in Inventory Tab
//        isInventoryValuation = Boolean.parseBoolean(request.getParameter("isInventoryValuation"));
        if (jsonRequestParam.containsKey("isInventoryValuation") && jsonRequestParam.get("isInventoryValuation") != null) {
            isInventoryValuation = Boolean.parseBoolean(jsonRequestParam.get("isInventoryValuation").toString());
        }
//        isFromStockReport = Boolean.parseBoolean(request.getParameter("isFromStockReport"));
        if (jsonRequestParam.containsKey("isFromStockReport") && jsonRequestParam.get("isFromStockReport") != null) {
            isFromStockReport = Boolean.parseBoolean(jsonRequestParam.get("isFromStockReport").toString());
        }
//        boolean exportInventoryValuation = request.getParameter("exportInventoryValuation") == null ? false : Boolean.parseBoolean(request.getParameter("exportInventoryValuation"));
        boolean exportInventoryValuation =false;
        if (jsonRequestParam.containsKey("exportInventoryValuation") && jsonRequestParam.get("exportInventoryValuation") != null) {
            exportInventoryValuation = Boolean.parseBoolean(jsonRequestParam.get("exportInventoryValuation").toString());
        }
        boolean isStockLedgerReport = true;
        double openingAmount = 0.0;
        double periodAmount = 0.0;
        double periodAssemblyValuation = 0.0;
        double OpeningAssemblyValuation = 0.0;
        double ledgerFinalValuation = 0;
        double ledgerOnHandQuantity = 0;
        double periodQty = 0;
        double openingQty = 0;
        double closingstack = 0;
        double ledgeropeningAmt = 0;
        double ledgerPeriodAmt = 0;
        double productopeningQty = 0;
        double productPeriodQty = 0;
        double productOpeningPrice = 0;
        double productPeriodPrice = 0;
        double stockInQty = 0;                    //Quantity In Selected date period
        double stockOutQty = 0;                   //Quantity Out Selected date period
        JSONObject FinalValuationObject = new JSONObject();
        double stockRate = 0;
        int openingRecordIndex = 0;
        int gridCounter = 0;
        Date transactionDate = null;

        while (itr.hasNext()) {
            try {
//                String companyid = sessionHandlerImpl.getCompanyid(request);
                DateFormat df=null;
                DateFormat userdf=null;
                if (jsonRequestParam.containsKey("df") && jsonRequestParam.get("df") != null) {
                    df = (DateFormat)(jsonRequestParam.get("df"));
                }
//                DateFormat df = authHandler.getDateOnlyFormat(request);
//                DateFormat userdf = authHandler.getUserDateFormatter(request);
                if (jsonRequestParam.containsKey("userdf") && jsonRequestParam.get("userdf") != null) {
                    userdf = (DateFormat) (jsonRequestParam.get("userdf"));
                }
                KwlReturnObject crresult;
                JSONObject ob =new JSONObject();
                ob.put("companyid", companyid);
                ob.put("gcurrencyid", gcurrencyid);
                HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(ob);
//                String gcurrencyid = sessionHandlerImpl.getCurrencyID(request);
                requestParams.put("companyid", companyid);
//                requestParams.put("gcurrencyid", gcurrencyid);
                requestParams.put("isStockLedgerReport", isStockLedgerReport);

                Object[] row = (Object[]) itr.next();
                String productid = (String) row[1];
                

                if (recCount > 0 && !pid.equals((String) row[2])) {
                    KwlReturnObject lastproductresult = accountingHandlerDAOobj.getObject(Product.class.getName(), productidLast);
                    Product lastproduct = (Product) lastproductresult.getEntityList().get(0);
                    ValuationMethod valuationMethod = lastproduct.getValuationMethod();
                    requestParams.put("productid", productidLast);
                    requestParams.put("isprovalReport", false);
                    String productCategoryid = "";
//                    if (request.getParameter(Constants.productCategoryid) != null) {
//                        productCategoryid = (String) request.getParameter(Constants.productCategoryid);
//                    }
                    if (jsonRequestParam.containsKey(Constants.productCategoryid) && jsonRequestParam.get(Constants.productCategoryid) != null) {
                        productCategoryid = (jsonRequestParam.get(Constants.productCategoryid).toString());
                    }
                    requestParams.put("endDate", EndDate);
                    requestParams.put("companyid", companyid);
                    requestParams.put("productCategoryid", productCategoryid);
                    //For Calcuating Quantity & Valuation for each product 
                    if (valuationMethod == ValuationMethod.FIFO || valuationMethod == ValuationMethod.STANDARD || lastproduct.isIsSerialForProduct() || lastproduct.isIsBatchForProduct()) {
                        for (TransactionBatch pvb : pvbList) {
                            double qty = pvb.getQuantity();
                            if (qty > 0) {
                                if (pvb.isOpening()) {
                                    if (pvb.isOutEntry()) {
//                                        openingAmount -= pvb.getPrice() * qty;
                                        ledgeropeningAmt -= pvb.getPrice() * qty;
                                        ledgerOnHandQuantity -= qty;
                                        ledgerFinalValuation -= pvb.getPrice() * qty;
                                        openingQty -= qty;
                                        if (lastproduct.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                                            OpeningAssemblyValuation -= pvb.getPrice() * qty;
                                        }
                                    } else {
//                                        openingAmount += pvb.getPrice() * qty;
                                        ledgeropeningAmt += pvb.getPrice() * qty;
                                        ledgerOnHandQuantity += qty;
                                        ledgerFinalValuation += pvb.getPrice() * qty;
                                        openingQty += qty;
                                        if (lastproduct.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                                            OpeningAssemblyValuation += pvb.getPrice() * qty;
                                        }
                                    }
                                } else {
                                    if (pvb.isOutEntry()) {
//                                        periodAmount -= pvb.getPrice() * qty;
                                        ledgerPeriodAmt -= pvb.getPrice() * qty;
                                        ledgerOnHandQuantity -= qty;
                                        ledgerFinalValuation -= pvb.getPrice() * qty;
                                        periodQty -= qty;
                                        stockOutQty -= qty;
                                        if (lastproduct.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                                            OpeningAssemblyValuation -= pvb.getPrice() * qty;
                                        }
                                    } else {
//                                        periodAmount += pvb.getPrice() * qty;
                                        ledgerPeriodAmt += pvb.getPrice() * qty;
                                        ledgerOnHandQuantity += qty;
                                        ledgerFinalValuation += pvb.getPrice() * qty;
                                        periodQty += qty;
                                        stockInQty += qty;
                                        if (lastproduct.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                                            OpeningAssemblyValuation += pvb.getPrice() * qty;
                                        }
                                    }
                                }
                            }
                        }
                        openingAmount += Double.parseDouble(authHandler.formattedAmount(ledgeropeningAmt, companyid));
                        periodAmount += Double.parseDouble(authHandler.formattedQuantity(ledgerPeriodAmt, companyid));
                    } else {
                        for (TransactionBatch pvb : pvbList) {
                            double qty = pvb.getQuantity();
                            if (qty > 0) {
                                if (pvb.isOpening()) {
                                    if (pvb.isOutEntry()) {
                                        openingQty -= qty;
                                        ledgerOnHandQuantity -= qty;
                                        productopeningQty -= qty;
                                        productOpeningPrice -= pvb.getPrice() * qty;
                                    } else {
                                        openingQty += qty;
                                        productopeningQty += qty;
                                        ledgerOnHandQuantity += qty;
                                        productOpeningPrice += pvb.getPrice() * qty;
                                    }
                                } else {
                                    if (pvb.isOutEntry()) {
                                        periodQty -= qty;
                                        productPeriodQty -= qty;
                                        ledgerOnHandQuantity -= qty;
                                        stockOutQty -= qty;
                                        productPeriodPrice -= pvb.getPrice() * qty;
                                    } else {
                                        periodQty += qty;
                                        productPeriodQty += qty;
                                        ledgerOnHandQuantity += qty;
                                        stockInQty += qty;
                                        productPeriodPrice += pvb.getPrice() * qty;
                                    }
                                }
                            }
                        }
//                        productavarageQty=productPeriodQty+productopeningQty;
//                        productavarageprice=productOpeningPrice+productPeriodPrice;
//                        avgcost=productavarageprice/productavarageQty;
                        periodAmount += periodQty * stockRate;
                        openingAmount += openingQty * stockRate;
                        ledgerFinalValuation = (periodQty * stockRate) + (openingQty * stockRate);
                        ledgeropeningAmt = openingQty * stockRate;
                    }

                    // For adding Quantity & Valuation rows to each product.
                    JSONObject FinalRowObj = new JSONObject();
                    if (!isInventoryValuation && !isFromStockReport) {
                        // For adding Quantity & Valuation rows to each product in Stock Ledger.
                        FinalRowObj.put("pid", pid);
                        FinalRowObj.put("productDesc", "");
                        FinalRowObj.put("transactionNumber", "");
                        FinalRowObj.put("personCode", "");
                        FinalRowObj.put("personName", "");
                        FinalRowObj.put("received", "");
                        FinalRowObj.put("stockRate", "Quantity on Hand: " + authHandler.formattedQuantity(ledgerOnHandQuantity, companyid));
                        FinalRowObj.put("value", "Valuation: " + authHandler.formattedAmount(ledgerFinalValuation, companyid));
                        FinalRowObj.put("ledgerFinalValuation",  authHandler.formattedAmount(ledgerFinalValuation, companyid));
                        FinalRowObj.put("balance", "-");
                        if (isExport) {
                            FinalRowObj.put("productid", productidLast);//
                            FinalRowObj.put("QtyOnHandJasper", authHandler.formattedQuantity(ledgerOnHandQuantity, companyid));
                            FinalRowObj.put("ValuationJasper", authHandler.formattedAmount(ledgerFinalValuation, companyid));
                            FinalRowObj.put("stockRate", 0);//
                            FinalRowObj.put("value", 0);//
                            FinalRowObj.put("isQtyAndValuationJSON", 1);//
                        }
                        jArr.put(gridCounter + 1, FinalRowObj);
                        gridCounter++;
                        gridCounter++;


                        JSONObject OpeningRowObj = new JSONObject();// For adding  Quantity & Valuation row to Last Product
                        OpeningRowObj.put("pid", pid);
                        OpeningRowObj.put("productDesc", "");
                        OpeningRowObj.put("transactionNumber", "Opening");
                        OpeningRowObj.put("personCode", "");
                        OpeningRowObj.put("personName", "");
                        OpeningRowObj.put("received", openingQty);
                        OpeningRowObj.put("stockRate", "-");
                        OpeningRowObj.put("value", authHandler.formattedAmount(ledgeropeningAmt, companyid));
                        OpeningRowObj.put("balance", "-");
                        if (isExport) {
                            OpeningRowObj.put("productDesc", productDescLast);
                            OpeningRowObj.put("productid", productidLast);//
                            OpeningRowObj.put("isQtyAndValuationJSON", 0);//
                        }
                        jArr.put(openingRecordIndex, OpeningRowObj);
                    } else {
                        FinalRowObj.put("productid", lastproduct.getID());
                        FinalRowObj.put("pid", lastproduct.getProductid());
                        FinalRowObj.put("productname", lastproduct.getName());
                        FinalRowObj.put("productdesc", lastproduct.getDescription());
                        FinalRowObj.put("productType", lastproduct.getProducttype().getName());
                        FinalRowObj.put("productTypeID", lastproduct.getProducttype().getID());
                        if (valuationMethod == ValuationMethod.FIFO) {
                            FinalRowObj.put("fifo", ledgerFinalValuation);
                        } else {
                            FinalRowObj.put("fifo", "NA");
                        }
                        if (valuationMethod == ValuationMethod.STANDARD) {
                            FinalRowObj.put("lifo", ledgerFinalValuation);
                        } else {
                            FinalRowObj.put("lifo", "NA");
                        }

                        KwlReturnObject priceResult = accProductDAO.getProductPrice(lastproduct.getID(), true, null, "-1", lastproduct.getCurrency().getCurrencyID());
                        List<Object> priceList = priceResult.getEntityList();
                        double proPrice = 0;
                        if (priceList != null) {
                            for (Object cogsval : priceList) {
                                proPrice = (cogsval == null ? 0.0 : (Double) cogsval);
                            }
                            crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, proPrice,  lastproduct.getCurrency().getCurrencyID(), null, 0);
                            proPrice = (Double) crresult.getEntityList().get(0);
                            
                        }
                        FinalRowObj.put("purchasecost", proPrice);
                        FinalRowObj.put("quantity", ledgerOnHandQuantity);
                        FinalRowObj.put("openingstockQty", openingQty);
                        FinalRowObj.put("stockInQty", stockInQty);
                        FinalRowObj.put("stockOutQty", stockOutQty);
                        FinalRowObj.put("evaluationcost", ledgerFinalValuation);
                        totalLedgerOnHandQuantity += ledgerOnHandQuantity;
                        totalOpeningQty += openingQty;
                        totalStockInQty += stockInQty;
                        totalStockOutQty += stockOutQty;
                        totalevaluationcost += ledgerFinalValuation;
                        if (valuationMethod == ValuationMethod.AVERAGE && !lastproduct.isIsSerialForProduct() && !lastproduct.isIsBatchForProduct()) {
                            FinalRowObj.put("avgcost", (stockRate != 0 && ledgerOnHandQuantity != 0) ? stockRate : "N.A");
                            FinalRowObj.put("valuation", ledgerFinalValuation);
                        } else if (valuationMethod == ValuationMethod.AVERAGE && (lastproduct.isIsSerialForProduct() || lastproduct.isIsBatchForProduct())) {//By Dipak P.
                            FinalRowObj.put("valuation", ledgerFinalValuation);
                        } else {
                            FinalRowObj.put("valuation", "NA");
                        }
                        jArr.put(FinalRowObj);
                    }

                    if (!pid.equals((String) row[2])) {                     //Re-Initializing the Variables for Product Level Data in Stockledger/StockReport/Inventory Valuation Report
                        balance = 0.0;
                        inIncludeOpening = 0;
                        pvbList.clear();
                        ledgerOnHandQuantity = 0;
                        ledgerFinalValuation = 0;
                        productopeningQty = 0;
                        productPeriodQty = 0;
                        productOpeningPrice = 0;
                        productPeriodPrice = 0;
                        periodQty = 0;
                        openingQty = 0;
                        ledgeropeningAmt = 0;
                        ledgerPeriodAmt = 0;
                        stockInQty = 0;
                        stockOutQty = 0;
                    }
                    openingRecordIndex = gridCounter;

                }

                BigInteger transType = (BigInteger) row[0];
//                String productid = (String) row[1];
                productidLast = (String) row[1];
                pid = (String) row[2];
                String productDesc = (String) row[3];
                productDescLast = (String) row[3];
                transactionDate = (Date) row[4];
                String transactionNumber = (String) row[5];
                String personCode = "";
                String personName = "";
                if (!StringUtil.isNullOrEmpty((String) row[6])) {
                    personCode = (String) row[6];
                }
                if (!StringUtil.isNullOrEmpty((String) row[7])) {
                    personName = (String) row[7];
                }

                String personid = (String) row[8];
                double quantity = (Double) row[9];
                stockRate = row[10] != null ? (Double) row[10] : 0.0;
                double baseUOMRate = (Double) row[11]; // Conversion Factor
//                String ConsignmentInvoiceID = (String) row[12];
                String currencyid = "";
                if (!StringUtil.isNullOrEmpty((String) row[13])) {
                    currencyid = (String) row[13];
                }

                String invoiceID = (String) row[14];
                double grSpotRate = 0.0;
                String detailid = "";
                try {
                    if (!StringUtil.isNullOrEmpty((String) row[16])) {
                        grSpotRate = StringUtil.getDouble((String) row[16]);
                    }
                } catch (java.lang.ClassCastException ex) {
                    if (row[16] != null) {
                        grSpotRate = (double) row[16];
                    }
                }
                if (!StringUtil.isNullOrEmpty((String) row[17])) {
                    detailid = (String) row[17];
                }
                boolean isOpeningtransaction = isOpeingOrPeriodTransaction(transactionDate, StartDate, transType.intValue());

                KwlReturnObject productresult = accountingHandlerDAOobj.getObject(Product.class.getName(), productid);
                Product product = (Product) productresult.getEntityList().get(0);
                ValuationMethod valuationMethod = product.getValuationMethod();
                Double baseUOMQuantitycount = authHandler.calculateBaseUOMQuatity(quantity, baseUOMRate, companyid);
                double baseUOMQuantity = baseUOMQuantitycount;
                double amount = 0.0;

                inIncludeOpening++;
                recCount++;
                String consignmentInvoice = accProductDAO.consignmentInvoice(invoiceID, companyid);
                if (!StringUtil.isNullOrEmpty(consignmentInvoice) && isActivateLandedInvAmt) {
                            double vendorInvoiceTotalAmountInBase = 1.0;
                            double consignmentInvoiceTotalAmountInBase = 1.0;

                            List invioceTotalAmountDetail = accProductDAO.getVendorInvoiceTotalAmountDetail(consignmentInvoice, personName, companyid);
                            Iterator it = invioceTotalAmountDetail.iterator();
                            while (it.hasNext()) {
                                Object[] itrow = (Object[]) it.next();
                                double vendorInvoiceTotalAmount = (Double) itrow[0];
                                String fromcurrencyid = (String) itrow[1];
                                Date entryDate = (Date) itrow[2];
                                double externalcurrencyrate = (Double) itrow[3];

                                crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, vendorInvoiceTotalAmount, fromcurrencyid, entryDate, externalcurrencyrate);
                                vendorInvoiceTotalAmountInBase = (Double) crresult.getEntityList().get(0);
                            }

                            List consignmentInvoiceTotalAmountDetail = accProductDAO.getVendorInvoiceTotalAmountDetail(invoiceID, personName, companyid);
                            it = consignmentInvoiceTotalAmountDetail.iterator();
                            while (it.hasNext()) {
                                Object[] itrow = (Object[]) it.next();
                                double consignmentInvoiceTotalAmount = (Double) itrow[0];
                                String fromcurrencyid = (String) itrow[1];
                                Date entryDate = (Date) itrow[2];
                                double externalcurrencyrate = (Double) itrow[3];

                                crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, consignmentInvoiceTotalAmount, fromcurrencyid, entryDate, externalcurrencyrate);
                                consignmentInvoiceTotalAmountInBase = (Double) crresult.getEntityList().get(0);
                            }
                            crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, stockRate, currencyid, transactionDate, 0);
                            stockRate = (Double) crresult.getEntityList().get(0);

                            amount = stockRate * quantity;
                            stockRate = amount + ((amount / vendorInvoiceTotalAmountInBase) * consignmentInvoiceTotalAmountInBase);

                            amount = stockRate * quantity;
                } else if (product.isIsSerialForProduct()) {
                    crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, stockRate, currencyid, transactionDate, grSpotRate);
                    stockRate = (Double) crresult.getEntityList().get(0);
                    int transactiontype = 28;
                    String serial = "";
                    if (transType.intValue() == 0 || transType.intValue() == 1 || transType.intValue() == 2 || transType.intValue() == 3 || transType.intValue() == 5 || transType.intValue() == 4) {
                        if (transType.intValue() == 0 || transType.intValue() == 1) {
                            transactiontype = 28;
                        } else if (transType.intValue() == 2 || transType.intValue() == 3 || transType.intValue() == 5 || transType.intValue() == 4) {
                            if (transType.intValue() == 2) {
                                transactiontype = 31;
                            } else if (transType.intValue() == 3 || transType.intValue() == 5) {
                                transactiontype = 27;
                            } else if (transType.intValue() == 4) {
                                transactiontype = 29;
                            }

                        }
                        KwlReturnObject srno = accProductDAO.getSerialNoByDocumentid(detailid, transactiontype);
                        List list2 = srno.getEntityList();

                        for (Object obj : list2) {
                            serial += obj.toString() + ",";
                        }
                        String[] serialId = serial.split(",");
                        stockRate = accProductDAO.getValuationPriceForSerialBatches(productid, transType.intValue(), valuationMethod, baseUOMQuantity, stockRate, baseUOMRate, isOpeningtransaction, pvbList, true, serialId);
                        amount += stockRate * baseUOMQuantity;
                    } else if (transType.intValue() == 7 || transType.intValue() == 8) {
                        KwlReturnObject saresult = accountingHandlerDAOobj.getObject(StockAdjustment.class.getName(), detailid);
                        StockAdjustment sa = (StockAdjustment) saresult.getEntityList().get(0);
                        KwlReturnObject saDetails = accProductDAO.getSADetailByStockAdjustment(detailid);
                        List saDetailsList = saDetails.getEntityList();
                        Iterator it = saDetailsList.iterator();
                        while (it.hasNext()) {
                            Object[] Objrow = (Object[]) it.next();
                            String store = sa.getStore().getId();
                            String location = (String) Objrow[2];
                            String batchnames = (String) Objrow[3];
                            double finalQuantity = (Double) Objrow[4];
                            String serialNames = (String) Objrow[5];
                            if (!StringUtil.isNullOrEmpty(serialNames)) {
                                String srl[] = serialNames.split(",");
                                for (String s : srl) {
                                    KwlReturnObject storeresult = accountingHandlerDAOobj.getObject(Store.class.getName(), store);
                                    Store str = (Store) storeresult.getEntityList().get(0);
                                    KwlReturnObject locationresult = accountingHandlerDAOobj.getObject(Location.class.getName(), location);
                                    Location loc = (Location) locationresult.getEntityList().get(0);

                                    NewProductBatch batchObj = stockDAO.getERPProductBatch(sa.getProduct(), str, loc, null, null, null, batchnames);
                                    NewBatchSerial serialObj = stockDAO.getERPBatchSerial(sa.getProduct(), batchObj, s);
                                    if (serialObj != null) {
                                        serial += serialObj.getId() + ",";
                                    }
                                }
                            }
                        }
                        String[] serialId = serial.split(",");
                        stockRate = accProductDAO.getValuationPriceForSerialBatches(productid, transType.intValue(), valuationMethod, baseUOMQuantity, stockRate, baseUOMRate, isOpeningtransaction, pvbList, true, serialId);
                        amount += stockRate * baseUOMQuantity;
                    }
                } else if (product.isIsBatchForProduct() && !product.isIsSerialForProduct()) {
                    crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, stockRate, currencyid, transactionDate, grSpotRate);
                    stockRate = (Double) crresult.getEntityList().get(0);
                    int transactiontype = 28;
                    String batch = "";
                    if (transType.intValue() == 0 || transType.intValue() == 1 || transType.intValue() == 2 || transType.intValue() == 3 || transType.intValue() == 5 || transType.intValue() == 4) {
                        if (transType.intValue() == 0 || transType.intValue() == 1) {
                            transactiontype = 28;
                        } else if (transType.intValue() == 2 || transType.intValue() == 3 || transType.intValue() == 5 || transType.intValue() == 4) {
                            if (transType.intValue() == 2) {
                                transactiontype = 31;
                            } else if (transType.intValue() == 3 || transType.intValue() == 5) {
                                transactiontype = 27;
                            } else if (transType.intValue() == 4) {
                                transactiontype = 29;
                            }
                        }
                        KwlReturnObject srno = accProductDAO.getBatchesByDocumentid(detailid, transactiontype);
                        List list2 = srno.getEntityList();
                        Iterator itr1 = list2.iterator();
                        while (itr1.hasNext()) {
                            Object[] batchRow = (Object[]) itr1.next();
                            batch = (String) batchRow[0];
                            double batchQty = (Double) batchRow[1];
                            String[] batchId = batch.split(",");
                            stockRate = accProductDAO.getValuationPriceForSerialBatches(productid, transType.intValue(), valuationMethod, batchQty, stockRate, baseUOMRate, isOpeningtransaction, pvbList, false, batchId);
                            amount += stockRate * batchQty;
                        }
                        stockRate = amount / baseUOMQuantity;
                    } else if (transType.intValue() == 7 || transType.intValue() == 8) {
                        KwlReturnObject saresult = accountingHandlerDAOobj.getObject(StockAdjustment.class.getName(), detailid);
                        StockAdjustment sa = (StockAdjustment) saresult.getEntityList().get(0);
                        KwlReturnObject saDetails = accProductDAO.getSADetailByStockAdjustment(detailid);
                        List saDetailsList = saDetails.getEntityList();
                        Iterator it = saDetailsList.iterator();
                        while (it.hasNext()) {
                            Object[] Objrow = (Object[]) it.next();
                            String store = sa.getStore().getId();
                            String location = (String) Objrow[2];
                            String batchnames = (String) Objrow[3];
                            double finalQuantity = (Double) Objrow[4];
                            String pro = sa.getProduct().getID();
                            requestParams.put("store", store);
                            requestParams.put("location", location);
                            requestParams.put("product", pro);
                            requestParams.put("batchnames", batchnames);
                            requestParams.put("finalQuantity", finalQuantity);

                            KwlReturnObject storeresult = accountingHandlerDAOobj.getObject(Store.class.getName(), store);
                            Store str = (Store) storeresult.getEntityList().get(0);
                            KwlReturnObject locationresult = accountingHandlerDAOobj.getObject(Location.class.getName(), location);
                            Location loc = (Location) locationresult.getEntityList().get(0);

                            NewProductBatch batchObj = stockDAO.getERPProductBatch(sa.getProduct(), str, loc, null, null, null, batchnames);
                            batch = batchObj.getId();
                            String[] batchId = batch.split(",");
                            stockRate = accProductDAO.getValuationPriceForSerialBatches(productid, transType.intValue(), valuationMethod, finalQuantity, stockRate, baseUOMRate, isOpeningtransaction, pvbList, false, batchId);
                            amount += stockRate * finalQuantity;
                        }
                        stockRate = amount / baseUOMQuantity;
                    }

                } else if (valuationMethod == ValuationMethod.FIFO || valuationMethod == ValuationMethod.STANDARD || valuationMethod == ValuationMethod.AVERAGE) {
                    crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, stockRate, currencyid, transactionDate, grSpotRate);
                    stockRate = (Double) crresult.getEntityList().get(0);
                    stockRate = accProductDAO.getValuationPrice(productid, transType.intValue(), valuationMethod, baseUOMQuantity, stockRate, baseUOMRate, isOpeningtransaction, pvbList);
                    amount += stockRate * baseUOMQuantity;
                }
                if (transType.intValue() == 0 || transType.intValue() == 1 || transType.intValue() == 4 || transType.intValue() == 6 || transType.intValue() == -1 || transType.intValue() == 7) {
                    balance += amount;
                } else {
                    balance -= amount;
                }

                JSONObject obj = new JSONObject();
                String duatyPaid = "";
                
                if (!isOpeningtransaction && !isInventoryValuation && !isFromStockReport) {
                    obj.put("pid", pid);
                    obj.put("productDesc", productDesc);
                    if (transType.intValue() != 6 && transType.intValue() != -1) {
                        obj.put("transactionDate", df.format(transactionDate));
                    }
                    obj.put("transactionNumber", transactionNumber);
                    obj.put("personCode", personCode);
                    obj.put("personName", personName);
                    if (transType.intValue() == 0 || transType.intValue() == 1 || transType.intValue() == 4 || transType.intValue() == 6 || transType.intValue() == -1 || transType.intValue() == 7) {
                        obj.put("received", baseUOMQuantity);
                    } else {
                        obj.put("delivered", baseUOMQuantity);
                    }
                    obj.put("transactiontype", transType.intValue());
                    obj.put("billid", (String) row[15]);
                    obj.put("stockRate", authHandler.roundUnitPrice(stockRate, companyid));
                    if (transType.intValue() == -1) {
                        obj.put("stockRate", "-");
                    }
                    obj.put("value", authHandler.round(amount, companyid));
                    if (isExport) {
                        obj.put("productid", productid);//
                        obj.put("isQtyAndValuationJSON", 0);//
                        obj.put("transType", transType.intValue());
                    }
                    obj.put("balance", authHandler.round(balance, companyid));
                        jArr.put(gridCounter + 1, obj);
                    gridCounter++;
                }
                if (lsize == list.size() - 1 ) { 
                    if (valuationMethod == ValuationMethod.FIFO || valuationMethod == ValuationMethod.STANDARD || product.isIsSerialForProduct() || product.isIsBatchForProduct()) {
                        for (TransactionBatch pvb : pvbList) {
                            double qty = pvb.getQuantity();
                            if (qty > 0) {
                                if (pvb.isOpening()) {
                                    if (pvb.isOutEntry()) {
//                                        openingAmount -= pvb.getPrice() * qty;
                                        ledgeropeningAmt -= pvb.getPrice() * qty;
                                        ledgerOnHandQuantity -= qty;
                                        ledgerFinalValuation -= pvb.getPrice() * qty;
                                        openingQty -= qty;
                                        if (product.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                                            OpeningAssemblyValuation -= pvb.getPrice() * qty;
                                        }
                                    } else {
//                                        openingAmount += pvb.getPrice() * qty;
                                        ledgeropeningAmt += pvb.getPrice() * qty;
                                        ledgerOnHandQuantity += qty;
                                        ledgerFinalValuation += pvb.getPrice() * qty;
                                        openingQty += qty;
                                        if (product.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                                            OpeningAssemblyValuation += pvb.getPrice() * qty;
                                        }
                                    }
                                } else {
                                    if (pvb.isOutEntry()) {
//                                        periodAmount -= pvb.getPrice() * qty;
                                        ledgerPeriodAmt -= pvb.getPrice() * qty;
                                        ledgerOnHandQuantity -= qty;
                                        periodQty -= qty;
                                        stockOutQty -= qty;
                                        ledgerFinalValuation -= pvb.getPrice() * qty;
                                        if (product.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                                            OpeningAssemblyValuation -= pvb.getPrice() * qty;
                                        }
                                    } else {
//                                        periodAmount += pvb.getPrice() * qty;
                                        ledgerPeriodAmt += pvb.getPrice() * qty;
                                        ledgerOnHandQuantity += qty;
                                        ledgerFinalValuation += pvb.getPrice() * qty;
                                        periodQty += qty;
                                        stockInQty += qty;
                                        if (product.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                                            OpeningAssemblyValuation += pvb.getPrice() * qty;
                                        }
                                    }
                                }
                            }
                        }
                        openingAmount += Double.parseDouble(authHandler.formattedAmount(ledgeropeningAmt, companyid));
                        periodAmount += Double.parseDouble(authHandler.formattedQuantity(ledgerPeriodAmt, companyid));
                    } else {
                        for (TransactionBatch pvb : pvbList) {
                            double qty = pvb.getQuantity();
                            if (qty > 0) {
                                if (pvb.isOpening()) {
                                    if (pvb.isOutEntry()) {
                                        openingQty -= qty;
                                        ledgerOnHandQuantity -= qty;
//                                        productopeningQty-=qty;
//                                        productOpeningPrice -=pvb.getPrice() * qty;
                                        stockOutQty -= qty;
                                        if (product.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                                            OpeningAssemblyValuation += pvb.getPrice() * qty;
                                        }
                                    } else {
                                        openingQty += qty;
                                        ledgerOnHandQuantity += qty;
//                                        productopeningQty+=qty;
//                                        productOpeningPrice+=pvb.getPrice() * qty;
                                        if (product.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                                            OpeningAssemblyValuation += pvb.getPrice() * qty;
                                        }
                                    }
                                } else {
                                    if (pvb.isOutEntry()) {
                                        periodQty -= qty;
                                        ledgerOnHandQuantity -= qty;
//                                        productPeriodQty-=qty;
//                                        productPeriodPrice-=pvb.getPrice() * qty;
                                        if (product.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                                            OpeningAssemblyValuation += pvb.getPrice() * qty;
                                        }
                                    } else {
                                        periodQty += qty;
                                        ledgerOnHandQuantity += qty;
//                                        productPeriodQty+=qty;
//                                        productPeriodPrice+=pvb.getPrice() * qty;
                                        stockInQty += qty;
                                        if (product.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                                            OpeningAssemblyValuation += pvb.getPrice() * qty;
                                        }
                                    }
                                }
                            }
                        }
//                        productavarageQty=productPeriodQty+productopeningQty;
//                        productavarageprice=productOpeningPrice+productPeriodPrice;
//                        avgcost=productavarageprice/productavarageQty;
                        periodAmount += periodQty * stockRate;
                        openingAmount += openingQty * stockRate;
                        ledgerFinalValuation = (periodQty * stockRate) + (openingQty * stockRate);
                        ledgeropeningAmt = openingQty * stockRate;
                    }

                    JSONObject FinalRowObj = new JSONObject();// For adding  Quantity & Valuation row to Last Product
                    if (!isInventoryValuation && !isFromStockReport) {                   //prepare this JSON in case of only Stock ledger
                        FinalRowObj.put("pid", pid);
                        FinalRowObj.put("productDesc", "");
                        FinalRowObj.put("transactionNumber", "");
                        FinalRowObj.put("personCode", "");
                        FinalRowObj.put("personName", "");
                        FinalRowObj.put("received", "");
                        FinalRowObj.put("stockRate", "Quantity on Hand: " + authHandler.formattedQuantity(ledgerOnHandQuantity, companyid));
                        FinalRowObj.put("value", "Valuation: " + authHandler.formattedAmount(ledgerFinalValuation, companyid));
                        FinalRowObj.put("ledgerFinalValuation",  authHandler.formattedAmount(ledgerFinalValuation, companyid));
                        FinalRowObj.put("balance", "-");
                        if (isExport) {
                            FinalRowObj.put("productid", productid);//
                            FinalRowObj.put("QtyOnHandJasper", authHandler.formattedQuantity(ledgerOnHandQuantity, companyid));
                            FinalRowObj.put("ValuationJasper", authHandler.formattedAmount(ledgerFinalValuation, companyid));
                            FinalRowObj.put("stockRate", 0);//
                            FinalRowObj.put("value", 0);//
                            FinalRowObj.put("isQtyAndValuationJSON", 1);//
                        }
                        jArr.put(gridCounter + 1, FinalRowObj);
                        gridCounter++;

                        JSONObject OpeningRowObj = new JSONObject();// For adding  Opening valuation row to Every Product.
                        OpeningRowObj.put("pid", pid);
                        OpeningRowObj.put("productDesc", "");
                        OpeningRowObj.put("transactionNumber", "Opening");
                        OpeningRowObj.put("personCode", "");
                        OpeningRowObj.put("personName", "");
                        OpeningRowObj.put("received", openingQty);
                        OpeningRowObj.put("stockRate", "-");
                        OpeningRowObj.put("value", authHandler.formattedAmount(ledgeropeningAmt, companyid));
                        OpeningRowObj.put("balance", "-");
                        if (isExport) {
                            OpeningRowObj.put("productDesc", productDesc);
                            OpeningRowObj.put("productid", productid);//
                            OpeningRowObj.put("isQtyAndValuationJSON", 0);//
                        }
                        jArr.put(openingRecordIndex, OpeningRowObj);
                    } else {
                        FinalRowObj.put("productid", product.getID());
                        FinalRowObj.put("pid", product.getProductid());
                        FinalRowObj.put("productname", product.getName());
                        FinalRowObj.put("productdesc", product.getDescription());
                        FinalRowObj.put("productType", product.getProducttype().getName());
                        FinalRowObj.put("productTypeID", product.getProducttype().getID());
                        if (valuationMethod == ValuationMethod.FIFO) {
                            FinalRowObj.put("fifo", ledgerFinalValuation);
                        } else {
                            FinalRowObj.put("fifo", "NA");
                        }
                        if (valuationMethod == ValuationMethod.STANDARD) {
                            FinalRowObj.put("lifo", ledgerFinalValuation);
                        } else {
                            FinalRowObj.put("lifo", "NA");
                        }
                        KwlReturnObject priceResult = accProductDAO.getProductPrice(product.getID(), true, null, "-1", product.getCurrency().getCurrencyID());
                        List<Object> priceList = priceResult.getEntityList();
                        double proPrice = 0;
                        if (priceList != null) {
                            for (Object cogsval : priceList) {
                                proPrice = (cogsval == null ? 0.0 : (Double) cogsval);
                            }
                            crresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, proPrice, product.getCurrency().getCurrencyID(), null, 0);
                            proPrice = (Double) crresult.getEntityList().get(0);

                        }
                        FinalRowObj.put("purchasecost", proPrice);
                        FinalRowObj.put("quantity", ledgerOnHandQuantity);
                        FinalRowObj.put("openingstockQty", openingQty);
                        FinalRowObj.put("stockInQty", stockInQty);
                        FinalRowObj.put("stockOutQty", stockOutQty);
                        FinalRowObj.put("evaluationcost", ledgerFinalValuation);
                        totalLedgerOnHandQuantity += ledgerOnHandQuantity;
                        totalOpeningQty += openingQty;
                        totalStockInQty += stockInQty;
                        totalStockOutQty += stockOutQty;
                        totalevaluationcost += ledgerFinalValuation;
                        if (valuationMethod == ValuationMethod.AVERAGE && !product.isIsSerialForProduct()&& !product.isIsBatchForProduct()) {
                            FinalRowObj.put("avgcost", (stockRate != 0 && ledgerOnHandQuantity != 0) ? stockRate : "N.A");
                            FinalRowObj.put("valuation", ledgerFinalValuation);
                        } else if (valuationMethod == ValuationMethod.AVERAGE && (product.isIsSerialForProduct() || product.isIsBatchForProduct())) {//By Dipak P.
                            FinalRowObj.put("valuation", ledgerFinalValuation);
                        } else {
                            FinalRowObj.put("valuation", "NA");
                        }
                        jArr.put(FinalRowObj);
                    }
                }
                lsize++;
                valuation = 0;
                onhand = 0;

            } catch (SessionExpiredException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //this condition is for only Product Build Assembly Case
//        if (request.getAttribute("totalQtyForProductBuild") != null && pvbList != null) {
        if (jsonRequestParam.containsKey("totalQtyForProductBuild") && jsonRequestParam.get("totalQtyForProductBuild") != null && pvbList != null) {
            //calculate unit price for totalQty of BOM 
            double totalQtyUsedInBuild = (Double) jsonRequestParam.get("totalQtyForProductBuild");
//            String productid = (String) request.getAttribute("BOMProductId");
            String productid = (String) jsonRequestParam.get("BOMProductId");
            KwlReturnObject productresult = accountingHandlerDAOobj.getObject(Product.class.getName(), productid);
            Product product = (Product) productresult.getEntityList().get(0);
            ValuationMethod valuationMethod = product.getValuationMethod();
            double unitCostForProduct = accProductDAO.getValuationPrice(productid, 3, valuationMethod, totalQtyUsedInBuild, stockRate, 1, false, pvbList);
            FinalValuationObject.put("productBuildUnitCost", unitCostForProduct);
        }

        FinalValuationObject.put("openingValuation", openingAmount);
        FinalValuationObject.put("periodValution", periodAmount);
        FinalValuationObject.put("totalValution", periodAmount + openingAmount);
        FinalValuationObject.put("OpeningAssemblyValuation", OpeningAssemblyValuation);
        FinalValuationObject.put("periodAssemblyValuation", periodAssemblyValuation);
        FinalValuationObject.put("totalAssemblyValuation", OpeningAssemblyValuation + periodAssemblyValuation);

        valuationArray.put(FinalValuationObject);
        if (exportInventoryValuation) {
            JSONObject obj = new JSONObject();
            obj.put("productdesc", "Total");
            obj.put("stockInQty", totalStockInQty);
            obj.put("stockOutQty", totalStockOutQty);
            obj.put("openingstockQty", totalOpeningQty);
            obj.put("quantity", totalLedgerOnHandQuantity);
            obj.put("evaluationcost", totalevaluationcost);
            jArr.put(obj);
        }
        return jArr;
    }

    public boolean isOpeingOrPeriodTransaction(Date transactionDate, Date StartDate, int transType) throws SessionExpiredException, ParseException {
        boolean isOpeningtransaction = false;
        try {
            if(StartDate == null || transactionDate == null){
                return false;
            }
            String transDate = authHandler.getDateOnlyFormat().format(transactionDate);
            transactionDate = authHandler.getDateOnlyFormat().parse(transDate);
            if (transactionDate.before(StartDate) || (transactionDate.equals(StartDate) && transType == 0)) {
                isOpeningtransaction = true;
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockMovement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(StockMovement.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isOpeningtransaction;
    }
    
    @Override
    public JSONArray getPriceCalculationForAsseblyProduct(HashMap<String, Object> jsonRequestParam) throws ServiceException, SessionExpiredException, ParseException, JSONException {
        JSONArray jArr = new JSONArray();
        String Productid = "";
        if (jsonRequestParam.containsKey("productid") && jsonRequestParam.get("productid") != null) {
            Productid = jsonRequestParam.get("productid").toString();
        }
        HashMap<String, Object> companyPriceListParams = new HashMap<String, Object>();
        companyPriceListParams.put("isPurchase", true);
        companyPriceListParams.put("productid", Productid);
        KwlReturnObject kwlCompanyMaxDateProductPriceList = accProductDAO.getAllProductsMaxAppliedDatePriceDetails(jsonRequestParam.get("companyid").toString(), companyPriceListParams);
        Map<String, Object[]> companyMaxDateProductPriceList = getcompanyMaxDateProductPriceListMap(kwlCompanyMaxDateProductPriceList.getEntityList());

        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("productid", Productid);
        requestParams.put("currencyid", jsonRequestParam.get("gcurrencyid").toString());
        requestParams.put("bomdetailid", jsonRequestParam.get("bomdetailid") != null ? jsonRequestParam.get("bomdetailid").toString() : "");
        KwlReturnObject result = accProductDAO.getAssemblyItems(requestParams);
        Iterator itr = result.getEntityList().iterator();
        double valuation = 0;
        double buildquantity = 0;
        if (jsonRequestParam.containsKey("buildquantity") && jsonRequestParam.get("buildquantity") != null) {
            buildquantity = Double.parseDouble(jsonRequestParam.get("buildquantity").toString());
        }
//        double buildquantity = Double.parseDouble(request.getParameter("buildquantity"));
        Map bomProductInvQtyMap = new HashMap();

//        String bomJson=request.getParameter("bomjson");
        String bomJson = "";
        if (jsonRequestParam.containsKey("bomjson") && jsonRequestParam.get("bomjson") != null) {
            bomJson = jsonRequestParam.get("bomjson").toString();
        }
        JSONArray bomArr = null;
        if (!StringUtil.isNullOrEmpty(bomJson)) {
            bomArr = new JSONArray("[" + bomJson + "]");
            for (int i = 0; i < bomArr.length(); i++) {
                JSONObject bomObj = bomArr.getJSONObject(i);
                String bomProductId = bomObj.getString("product");
                if(!StringUtil.isNullOrEmpty(bomProductId)){
                    double invQty = bomObj.getDouble("actualquantity");
                    bomProductInvQtyMap.put(bomProductId, invQty);
                }
            }
        }
        while (itr.hasNext()) {
            JSONObject obj = new JSONObject();
            double productVal = 0;
            Object[] row = (Object[]) itr.next();
            ProductAssembly passembly = (ProductAssembly) row[0];
            String bomProductId = passembly.getSubproducts() != null ? passembly.getSubproducts().getID() : "";
            String prodTypeId = (passembly.getSubproducts()!= null && passembly.getSubproducts().getProducttype() != null) ? passembly.getSubproducts().getProducttype().getID() : "";
            if (!prodTypeId.equals(Producttype.CUSTOMER_INVENTORY)) {
                double actQty = (bomProductInvQtyMap.containsKey(bomProductId)) ? (Double) bomProductInvQtyMap.get(bomProductId) : passembly.getInventoryQuantity();
    //            productVal = getProductCalculation(request, passembly.getSubproducts(),actQty, companyMaxDateProductPriceList);

                KwlReturnObject rs = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), jsonRequestParam.get("companyid").toString());
                CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) rs.getEntityList().get(0);

                HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
                DateFormat df = (DateFormat) (jsonRequestParam.get("df"));
                requestParams1.put(Constants.df, df);
                requestParams1.put("companyid", jsonRequestParam.get("companyid").toString());
                requestParams1.put("productId", bomProductId);
                requestParams1.put("startdate", df.format(companyAccountPreferences.getBookBeginningFrom()));
                requestParams1.put("enddate", df.format(new Date()));
                KwlReturnObject result1 = accProductDAO.getStockLedger(requestParams1);

                jsonRequestParam.put("totalQtyForProductBuild", actQty * buildquantity);
                jsonRequestParam.put("BOMProductId", bomProductId);
                List list = result1.getEntityList();
                JSONArray resultJson = new JSONArray();
    //            JSONArray DataJArr = productControllerCMN.getNewStockLedgerJson(request, list, companyMaxDateProductPriceList,resultJson);
                JSONArray DataJArr = getNewStockLedgerJson(jsonRequestParam, list, companyMaxDateProductPriceList, resultJson);

                if (resultJson.length() > 0) {
                    JSONObject jObjt = resultJson.getJSONObject(0);
                    productVal = jObjt.optDouble("productBuildUnitCost");
                }
                valuation += productVal;
                obj.put("productid", passembly.getSubproducts().getID());

                double unitBuildCost = 0;
                unitBuildCost = (productVal * actQty);
                obj.put("buildcost", unitBuildCost);
                jArr.put(obj);
            }
        }
        return jArr;
    }
        
    public static Map<String, Object[]> getcompanyMaxDateProductPriceListMap(List<Object[]> companyMaxDateProductPriceList) {
        Map<String, Object[]> map = new HashMap<String, Object[]>();
        try {
            for (Object[] obj : companyMaxDateProductPriceList) {
                if (!map.containsKey(obj[3].toString())) {
                    map.put(obj[3].toString(), obj);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(StockMovement.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return map;
        }

    }
    @Override
    public Map<String, String> getSerialSkuMap(String productId, String batchName, String[] serialArray) throws ServiceException {
        if (batchName == null) {
            batchName = "";
        }
        return stockDAO.getSerialSkuMap(productId, batchName, serialArray);
    }
    
  
    @Override
     public JSONObject getStoreProductWiseAllAvailableStockDetailList(JSONObject paramJobj) { // for Cycle Count
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            String companyId = paramJobj.getString(Constants.companyKey);
            String productId = paramJobj.optString("productId");

            KwlReturnObject jeresult = null;
            Product product = null;
            if (!StringUtil.isNullOrEmpty(productId)) {
                jeresult = accountingHandlerDAO.getObject(Product.class.getName(), productId);
                product = (Product) jeresult.getEntityList().get(0);
            }

            String storeId =  paramJobj.optString("storeId");
            Set<Store> storeSet = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                Store store = storeService.getStoreById(storeId);
                if (store != null) {
                    storeSet = new HashSet();
                    storeSet.add(store);
                }
            }
            String ccDate =  paramJobj.getString("businessDate");
            Date businessDate;
            try {
                businessDate = yyyyMMdd_HIPHON.parse(ccDate);
            } catch (ParseException ex) {
                businessDate = null;
            }
            List<Object[]> productBatchList = getDateWiseStockDetailListForProduct(product, storeSet, null, businessDate, null, paging);
            for (Object[] objs : productBatchList) {
                String locationName = objs[2] != null ? (String) objs[2] : null;
                String rowName = objs[3] != null ? (String) objs[3] : null;
                String rackName = objs[4] != null ? (String) objs[4] : null;
                String binName = objs[5] != null ? (String) objs[5] : null;
                String batchName = objs[6] != null ? (String) objs[6] : null;
                String serialnames = objs[7] != null ? (String) objs[7] : null;
                double quantity = objs[8] != null ? (Double) objs[8] : 0;
                String strId = objs[11] != null ? (String) objs[11] : null;
                String locationId = objs[12] != null ? (String) objs[12] : null;
                String rowId = objs[13] != null ? (String) objs[13] : null;
                String rackId = objs[14] != null ? (String) objs[14] : null;
                String binId = objs[15] != null ? (String) objs[15] : null;
                JSONObject jObj = new JSONObject();
                jObj.put("fromStoreId", strId);
                jObj.put("batchName", batchName);
                jObj.put("fromLocationId", locationId);
                jObj.put("fromLocationName", locationName);
                if (product.isIsrowforproduct()) {
                    jObj.put("fromRowId", rowId);
                    jObj.put("fromRowName", rowName);
                }
                if (product.isIsrackforproduct()) {
                    jObj.put("fromRackId", rackId);
                    jObj.put("fromRackName", rackName);
                }
                if (product.isIsbinforproduct()) {
                    jObj.put("fromBinId", binId);
                    jObj.put("fromBinName", binName);
                }

                boolean addRecord = true;

                if (product.isIsSerialForProduct()) {
                    String serials = serialnames;
                    String[] serialArray = null;
                    if (!StringUtil.isNullOrEmpty(serials)) {
                        serialArray = serials.split(",");
                    }
                    int serialCount = serialArray.length;

                    jObj.put("availableSerials", serials);
                    jObj.put("availableSerialsSku", "");
                    if (product.isIsSKUForProduct()) {
                        String sku = "";
                        Map<String, String> serialSkuMap =getSerialSkuMap(product.getID(), batchName, serialArray);
                        for (int i = 0; i < serialCount; i++) {

                            if (serialSkuMap.containsKey(serialArray[i]) && serialSkuMap.get(serialArray[i]) != null) {
                                sku += serialSkuMap.get(serialArray[i]) + ",";
                            } else {
                                sku += ",";
                            }
                        }
                        if (sku.contains(",")) {
                            sku = sku.substring(0, sku.lastIndexOf(","));
                        }
                        jObj.put("availableSerialsSku", sku);

                    }
                    jObj.put("availableQty", serialCount);

                    if (serialCount == 0) {
                        addRecord = false;
                    }
                } else {
                    jObj.put("availableSerials", "");
                    jObj.put("availableSerialsSku", "");
                    jObj.put("availableQty", quantity);
                }
                if (addRecord) {
                    jArray.put(jObj);
                }

            }

            issuccess = true;
            msg = "store product wise location batch list has been fetched successfully";

        } catch (InventoryException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return jobj;
    }
    
    @Override
    public Map<String, Double> getAvailableQuantityForAllProductByStore(Company company, List<StockRequest> stockRequestList, boolean isExport) throws ServiceException {
        StringBuilder producIn = new StringBuilder();
        if (stockRequestList != null & stockRequestList.size() > 0 && !isExport) {
            boolean first = true;

            for (StockRequest sr : stockRequestList) {
                if (first) {
                    producIn.append(" AND s.product.id IN ( ");
                    producIn.append("'").append(sr.getProduct().getID()).append("'");
                    first = false;
                } else {
                    if (producIn.indexOf(sr.getProduct().getID()) == -1) {
                        producIn.append(",").append("'").append(sr.getProduct().getID()).append("'");
                    }
                }
            }
            producIn.append(")");
        }
        return stockDAO.getAvailableQuantity(company, false, producIn, isExport);
    }
    
    @Override
    public boolean isSerialExistsinDO(Product product,String batchName, String serialName) throws ServiceException {
        return stockDAO.isSerialExistsinDO(product, batchName, serialName);
    }
    
         @Override
    public JSONObject getAssetDetailList(HashMap<String, Object> jsonRequestParam, Paging paging,Company company) {

        JSONObject jsonObjData = new JSONObject();
        JSONArray jArray = new JSONArray();
        String msg = "";
        String companyId = jsonRequestParam.containsKey("company") ? jsonRequestParam.get("company").toString() : "";
        String type = jsonRequestParam.containsKey("type") ? jsonRequestParam.get("type").toString() : "";
        DateFormat df = (jsonRequestParam.containsKey("df") ? ((DateFormat) jsonRequestParam.get("df")) : null);
         HttpServletRequest request= jsonRequestParam.containsKey("request") ? (HttpServletRequest)jsonRequestParam.get("request") : null;
        

        boolean issuccess = false;
        HashMap<String, String> addamp = new HashMap<>();
        try {
            List list = new ArrayList(); 
            List<Store> storList=new ArrayList<Store>();
            Map<String,String> storeId=new HashMap<String,String>();
            List<Location> locationList=new ArrayList<Location>();
            Map<String,String> locationId=new HashMap<String,String>();
            
//            if ("1".equals(type)) {
//                list = stockDAO.getAssetDetailList(jsonRequestParam, paging);
//            } else if ("2".equals(type)) {
//                list = stockDAO.getAssetStockDetailList(jsonRequestParam, paging);
//            }
            list = stockDAO.getAllAssetStockDetailList(jsonRequestParam, paging);
            if (list != null && list.size() > 0) {
                addamp = stockDAO.getCustomerAddress(companyId);
                storList = storeService.getStores(company, "", null);
                locationList = locationService.getLocations(company, "", null);
                if (storList != null && storList.size() > 0) {
                    for (Store str : storList) {
                        storeId.put(str.getId(), str.getAbbreviation());
                    }
                }
                if (locationList != null && locationList.size() > 0) {
                    for (Location lcn : locationList) {
                        locationId.put(lcn.getId(), lcn.getName());
                    }
                }
            }
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                JSONObject jsonObj = new JSONObject();
                Object[] objs = (Object[]) itr.next();
                jsonObj.put("consignmentdn", objs[0] != null ? objs[0] : "");
                jsonObj.put("itemcode", objs[1] != null ? objs[1] : "");
                String batchName = (objs[3] != null && objs[3] != "") ? objs[3].toString() : "";
//                if(!StringUtil.isNullOrEmpty(b)&&"7242817".equals(b)){
//                    System.out.println("");
//                }
                String quantity=authHandler.formattedQuantity((double)(objs[9] != null ? objs[9] : 0.0),companyId);
                jsonObj.put("itemdescription", objs[2] != null ? objs[2] : "");
                jsonObj.put("batchname", batchName);
                jsonObj.put("serialname", objs[4] != null ? objs[4] : "");
                jsonObj.put("skufield", objs[5] != null ? objs[5] : "");
                jsonObj.put("storedescription", objs[6] != null ? objs[6] : "");
                jsonObj.put("location", objs[7] != null ? objs[7] : "-");
                jsonObj.put("ConsigneeName", (objs[8] != null && objs[8] != "NA") ? objs[8] : "");
                jsonObj.put("quantity", quantity);
                jsonObj.put("itemname", objs[10] != null ? objs[10] : "");
                jsonObj.put("consignmendate", (objs[11] != null && objs[11] != "NA") ? objs[11] : "");
                jsonObj.put("loanfromdate", (objs[12] != null && objs[12] != "NA") ? objs[12] : "");
                jsonObj.put("loantodate", (objs[13] != null && objs[13] != "NA") ? objs[13] : "");
                String batchExpdate = objs[20] != null ? objs[20].toString() : ""; //getting batchExpdate for asset details report -> asset details tab
                jsonObj.put("serialname", objs[4] != null ? objs[4].toString() : "");
                jsonObj.put("serialid", "");
                jsonObj.put("skufieldValue", "");
                jsonObj.put("serialexptodate", "");
                if (objs[4] != null && objs[19] != null) {
                    KwlReturnObject batchSerial = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), objs[19].toString());
                    if (!batchSerial.getEntityList().isEmpty() && batchSerial.getEntityList().get(0) != null) {
                        NewBatchSerial bserial = (NewBatchSerial) batchSerial.getEntityList().get(0);
                        jsonObj.put("serialid", bserial.getId());
                        jsonObj.put("skufieldValue", bserial.getSkufield());
                        jsonObj.put("serialexptodate", bserial.getExptodate() != null ? authHandler.getUTCToUserLocalDateFormatter_NEW(request, bserial.getExptodate()) : "");
                    }

                }else{
                    jsonObj.put("serialexptodate", batchExpdate);
                }
                String customerId = objs[14] != null ? objs[14].toString() : "";
                String soId = objs[15] != null ? objs[15].toString() : "";
                String productID = objs[16] != null ? objs[16].toString() : "";
                String wid = objs[17] != null ? objs[17].toString() : "";
                String lid = objs[18] != null ? objs[18].toString() : "";
                String isBatchForProduct = objs[21] != null ? objs[21].toString() : "";
                String isSerialForProduct = objs[22] != null ? objs[22].toString() : "";
               
//                if ("F".equals(isSerialForProduct)) {
//                    jsonObj.put("serialexptodate", batchExpdate);
//                }
                jsonObj.put("batchExpdate", batchExpdate);
                jsonObj.put("isBatchForProduct", isBatchForProduct);
                jsonObj.put("isSerialForProduct", isSerialForProduct);
                
                if (storeId.containsKey(wid) && (StringUtil.isNullOrEmpty(objs[6].toString().trim()))) {
                    jsonObj.put("storedescription", storeId.get(wid));
                }
                if (locationId.containsKey(lid) && (StringUtil.isNullOrEmpty(objs[7].toString().trim()))) {
                    jsonObj.put("location", locationId.get(lid));
                }
                
                if (addamp.containsKey(customerId)&&customerId!="NA") {
                    jsonObj.put("country", addamp.get(customerId));
                }
                if (objs[5] == null || objs[5].toString().equals("")) {
                    String skufiled = "";
                    NewBatchSerial serialForSku = stockDAO.getSkuBySerialName(productID, objs[4].toString(), companyId);
                    if (serialForSku != null) {
                        jsonObj.put("skufield", serialForSku.getSkufield());
                    }
                }
              
                // custom column data //
                SalesOrder consignmentRequest = null;
                String purposeOfLoan = "";
                if (!StringUtil.isNullOrEmpty(soId)) {
                    KwlReturnObject consignmentReqObj = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soId);
                    if (!consignmentReqObj.getEntityList().isEmpty() && consignmentReqObj.getEntityList().get(0) != null) {
                        consignmentRequest = (SalesOrder) consignmentReqObj.getEntityList().get(0);
                    }
//                }
                   if (consignmentRequest!=null){
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_ConsignmentRequest_ModuleId));
                    HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    SalesOrderCustomData soCustomData = (SalesOrderCustomData) consignmentRequest.getSoCustomData();
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    AccountingManager.setCustomColumnValues(soCustomData, fieldMap, replaceFieldMap, variableMap);
                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                        String coldata = varEntry.getValue().toString();
                        if (customFieldMap.containsKey(varEntry.getKey())) {
                            if (varEntry.getKey().equalsIgnoreCase("Custom_Purpose of Request")) {
                                String Ids[] = coldata.split(",");
                                for (int i = 0; i < Ids.length; i++) {
                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
                                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                    if (fieldComboData != null) {
                                        purposeOfLoan += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                    }
                                }
                                if (!StringUtil.isNullOrEmpty(purposeOfLoan)) {
                                    purposeOfLoan = purposeOfLoan.substring(0, purposeOfLoan.length() - 1);
                                }
                            }
                        }
                    }
                    jsonObj.put("Purposeofloan", purposeOfLoan);
                }
                }
                jArray.put(jsonObj);
            }
            issuccess = true;
        } catch (InventoryException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jsonObjData.put("success", issuccess);
                jsonObjData.put("msg", msg);
                jsonObjData.put("data", jArray);
                if (paging != null) {
                    jsonObjData.put("count", paging.getTotalRecord());
                } else {
                    jsonObjData.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return jsonObjData;
    }
    
    @Override
    public NewBatchSerial getSkuBySerialName(String productID, String serialName, String companyID) throws ServiceException {
        /*
        Get serial details or Asset(SKU fields) details from below method
        */
        return stockDAO.getSkuBySerialName(productID, serialName, companyID);
    }
    
    @Override
    public KwlReturnObject getGRODetailISTMapping(JSONObject json) throws ServiceException {
        return stockDAO.getGRODetailISTMapping(json);
    }
    
    @Override
    public KwlReturnObject getWOCDetailISTMapping(JSONObject json) throws ServiceException {
        return stockDAO.getWOCDetailISTMapping(json);
    }
    
    public String getGoodsReceiptOrderNumberUsingMapping(JSONObject json) throws ServiceException {
        return stockDAO.getGoodsReceiptOrderNumberUsingMapping(json);
    }
    
    public String getWorkOrderNumberUsingMapping(JSONObject json) throws ServiceException {
        return stockDAO.getWorkOrderNumberUsingMapping(json);
    }
    
    @Override
    public String getDeliveryOrderNumberUsingMapping(JSONObject json) throws ServiceException {
        return stockDAO.getDeliveryOrderNumberUsingMapping(json);
    }
    
    @Override
    public KwlReturnObject getDODetailISTMapping(JSONObject json) throws ServiceException {
        return stockDAO.getDODetailISTMapping(json);
    }
    
    @Override
    public KwlReturnObject getRejectedDODQCISTMapping(JSONObject json) throws ServiceException {
        return stockDAO.getRejectedDODQCISTMapping(json);
    }
    
    @Override
    public KwlReturnObject getDODQCISTMapping(JSONObject json) throws ServiceException {
        return stockDAO.getDODQCISTMapping(json);
    }
    
    @Override
    public void updateDeliveryOrderStatus(JSONObject params) throws ServiceException {
        stockDAO.updateDeliveryOrderStatus(params);
    }
     @Override
    public boolean isSerialExists(Product product, Store store, Location location, String batchName, String serialName, String module,String documentID) throws ServiceException {
        return stockDAO.isSerialExists(product, batchName, serialName, module,documentID);
    }
    
    @Override
    public List getERPSerialFromBatch(Product product, String warehouse, String location, String row, String rack, String bin, String batchName, String serialName) throws ServiceException{
        return stockDAO.getERPSerialFromBatch(product.getCompany().getCompanyID(), product.getID(), warehouse, location, row, rack, bin, batchName, serialName);
    }
    
    @Override
    public List getbatchserialdetails(HashMap<String, String> batchserialMap,TransactionModule transModule,Product product) throws ServiceException {
        return stockDAO.getbatchserialdetails(batchserialMap,transModule,product);

    }
    @Override
    public boolean getISSerialisValidforOut(Product product,String storeId,String locationId,Date bussinessDate,String ss){
        boolean isvalid=true;
        try {
            Store store = storeService.getStoreById(storeId);
            Location location = locationService.getLocation(locationId);
            Set<Store> storeSet=new HashSet<>();
            storeSet.add(store);
            List<Object[]> stockDetailList = getDateWiseStockDetailListForProduct(product, storeSet, location, bussinessDate, ss, null);
            List<String> serList=new ArrayList<>();
            for (Object[] stock : stockDetailList) {
                if (stock != null) {
                    String[] serials = stock[7].toString().trim().split(",");
                    serList = Arrays.asList(serials);
                }
            }
            if (serList.contains(ss)) {
                List<StockMovement> smList = stockMovementDAO.getStockMovementByProduct(product.getCompany(), product, store, bussinessDate, ss, null);
                List<String> srList = Arrays.asList(ss);
                if (smList.size() > 0) {
                    for (StockMovement sm : smList) {
                        for (StockMovementDetail dtl : sm.getStockMovementDetails()) {
                            String[] smserArr = dtl.getSerialNames().split(",");
                            List<String> l3 = Arrays.asList(smserArr);
                            if (!Collections.disjoint(l3, srList)) {
                               // msg=" Transactions are found after Stock adjustment.";
                                isvalid=false;
                            }
                        }
                    }
                }
            }else{
//                msg = "Stock was not available for businessdate " + bussinessDate;
                isvalid=false;
            }
            
        } catch (ServiceException ex) {
            Logger.getLogger(StockServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return isvalid;

    }
    public List<String> getbatchidFromDocumentId(String documentId) throws ServiceException {
        return  stockDAO.getbatchidFromDocumentId(documentId);
    }
    }