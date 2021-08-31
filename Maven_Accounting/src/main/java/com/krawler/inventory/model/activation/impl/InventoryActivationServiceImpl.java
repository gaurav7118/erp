/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.activation.impl;

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.exception.SeqFormatException;
import com.krawler.inventory.model.activation.InventoryActivationService;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.approval.sa.SAApprovalService;
import com.krawler.inventory.model.approval.sa.SADetailApproval;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferApprovalService;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferDetailApproval;
import com.krawler.inventory.model.ist.InterStoreTransferRequest;
import com.krawler.inventory.model.ist.InterStoreTransferService;
import com.krawler.inventory.model.ist.InterStoreTransferStatus;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.sequence.ModuleConst;
import com.krawler.inventory.model.sequence.SeqFormat;
import com.krawler.inventory.model.sequence.SeqService;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.*;
import com.krawler.inventory.model.stockrequest.RequestStatus;
import com.krawler.inventory.model.stockrequest.StockRequest;
import com.krawler.inventory.model.stockrequest.StockRequestService;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.inventory.model.store.StoreType;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author Vipin Gupta
 */
public class InventoryActivationServiceImpl extends BaseDAO implements InventoryActivationService {

    private static final int TT_OPENING = 28;
    private static final int TT_GRN = 28;
    private static final int TT_DO = 27;
    private static final int TT_PR = 31;
    private static final int TT_SR = 29;
    private static final int TT_PB = 28;
    private static final int TT_PBD = 27;
    private static final int TRANSACTION_LIMIT = 500;
    private StoreService storeService;
    private LocationService locationService;
    private StockService stockService;
    private accMasterItemsDAO accMasterItemsDAO;
    private HibernateTransactionManager txnManager;
    private auditTrailDAO auditTrailObj;
    private StockRequestService srService;
    private InterStoreTransferService istService;
    private StockTransferApprovalService stApprovalService;
    private SAApprovalService saApprovalService;
    private SeqService seqService;

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void setAccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAO) {
        this.accMasterItemsDAO = accMasterItemsDAO;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }

    public void setIstService(InterStoreTransferService istService) {
        this.istService = istService;
    }

    public void setSrService(StockRequestService srService) {
        this.srService = srService;
    }

    public void setSaApprovalService(SAApprovalService saApprovalService) {
        this.saApprovalService = saApprovalService;
    }

    public void setStApprovalService(StockTransferApprovalService stApprovalService) {
        this.stApprovalService = stApprovalService;
    }
    
    public void setSeqService(SeqService seqService) {
        this.seqService = seqService;
    }

    @Override
    public List<Product> getInvactivatedInventoryProducts(Company company, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM Product WHERE company = ? AND (iswarehouseforproduct = ? OR islocationforproduct = ?) AND deleted = ? AND producttype.ID IN (?,?,?) ");
        List params = new ArrayList();
        params.add(company);
        params.add(false);
        params.add(false);
        params.add(false);
        params.add(Producttype.ASSEMBLY);
        params.add(Producttype.INVENTORY_PART);
        params.add(Producttype.Inventory_Non_Sales);

        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (productid LIKE ? OR name LIKE ? OR description LIKE ?) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        hql.append(" ORDER BY productid, name, description ");

        List list;
        if (paging != null) {
            if (paging.isValid()) {
                String countQuery = "SELECT COUNT(*) AS cnt " + hql.toString().substring(hql.indexOf("FROM"));
                list = executeQuery( countQuery, params.toArray());
                int totalCount = 0;
                if (!list.isEmpty()) {
                    Long c = list.get(0) != null ? (Long) list.get(0) : 0;
                    totalCount = c.intValue();
                }
                paging.setTotalRecord(totalCount);

                list = executeQueryPaging( hql.toString(), params.toArray(), paging);
            } else {
                list = executeQuery( hql.toString(), params.toArray());
                paging.setTotalRecord(list.size());
            }

        } else {
            list = executeQuery( hql.toString(), params.toArray());
        }

        return list;
    }

    @Override
    public List<Object[]> getInvactivatedInventoryProductsForUI(Company company, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("SELECT p.ID, p.productid, p.name, p.description FROM Product p WHERE p.company = ? AND (p.iswarehouseforproduct = ? OR p.islocationforproduct = ?) AND p.deleted = ? AND p.producttype.ID IN (?,?,?) ");
        List params = new ArrayList();
        params.add(company);
        params.add(false);
        params.add(false);
        params.add(false);
        params.add(Producttype.ASSEMBLY);
        params.add(Producttype.INVENTORY_PART);
        params.add(Producttype.Inventory_Non_Sales);

        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (p.productid LIKE ? OR p.name LIKE ? OR p.description LIKE ?) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        hql.append(" ORDER BY p.productid, p.name, p.description ");

        List list;
        if (paging != null) {
            if (paging.isValid()) {
                String countQuery = "SELECT COUNT(p.*) AS cnt " + hql.toString().substring(hql.indexOf("FROM"));
                list = executeQuery( countQuery, params.toArray());
                int totalCount = 0;
                if (!list.isEmpty()) {
                    Long c = list.get(0) != null ? (Long) list.get(0) : 0;
                    totalCount = c.intValue();
                }
                paging.setTotalRecord(totalCount);

                list = executeQueryPaging( hql.toString(), params.toArray(), paging);
            } else {
                list = executeQuery( hql.toString(), params.toArray());
                paging.setTotalRecord(list.size());
            }

        } else {
            list = executeQuery( hql.toString(), params.toArray());
        }

        return list;
    }

    @Override
    public List<Object[]> getAllInTransitTransactionRequests(Company company, Paging paging) throws ServiceException {

        // Stock Request In-transit
        String query = "(SELECT t.transactionno, p.productid, 'Stock Request' "
                + " FROM in_goodsrequest t "
                + " INNER JOIN product p ON p.id = t.product "
                + " WHERE t.company = ? AND t.`status` IN (?,?,?))";
        List params = new ArrayList();
        params.add(company.getCompanyID());
        params.add(RequestStatus.ORDERED.ordinal());
        params.add(RequestStatus.ISSUED.ordinal());
        params.add(RequestStatus.RETURN_REQUEST.ordinal());

        //Stock Request Approval
        query += "UNION (SELECT gr.transactionno,  p.productid, 'Stock Request' FROM in_stocktransfer_approval sta "
                + " INNER JOIN in_stocktransfer_detail_approval stad ON stad.stocktransfer_approval = sta.id "
                + " INNER JOIN in_goodsrequest gr ON sta.stocktransferid = gr.id AND gr.company = ? "
                + " INNER JOIN product p ON p.id = gr.product "
                + " WHERE (stad.approval_status = ? OR stad.repair_status IN (?,?)))";
        params.add(company.getCompanyID());
        params.add(ApprovalStatus.PENDING.ordinal());
        params.add(ApprovalStatus.REPAIRPENDING.ordinal());
        params.add(ApprovalStatus.RETURNTOREPAIR.ordinal());

        //IST Request In-Transit
        query += "UNION (SELECT t.transactionno, p.productid, 'Inter Store Transfer' "
                + " FROM in_interstoretransfer t "
                + " INNER JOIN product p ON p.id = t.product "
                + " WHERE t.company = ? AND t.`status` IN (?, ?))";
        params.add(company.getCompanyID());
        params.add(InterStoreTransferStatus.INTRANSIT.ordinal());
        params.add(InterStoreTransferStatus.RETURNED.ordinal());

        //IST Request Approval
        query += "UNION (SELECT  gr.transactionno,  p.productid, 'Inter Store Transfer'  FROM in_stocktransfer_approval sta "
                + " INNER JOIN in_stocktransfer_detail_approval stad ON stad.stocktransfer_approval = sta.id "
                + " INNER JOIN in_interstoretransfer gr ON sta.stocktransferid = gr.id AND gr.company = ? "
                + " INNER JOIN product p ON p.id = gr.product "
                + " WHERE (stad.approval_status = ? OR stad.repair_status IN (?,?)))";
        params.add(company.getCompanyID());
        params.add(ApprovalStatus.PENDING.ordinal());
        params.add(ApprovalStatus.REPAIRPENDING.ordinal());
        params.add(ApprovalStatus.RETURNTOREPAIR.ordinal());

        //Stock Adjustment Approval
        query += "UNION (SELECT gr.seqno,  p.productid, 'Stock Adjustment'  FROM in_sa_approval sta "
                + " INNER JOIN in_sa_detail_approval stad ON stad.sa_approval = sta.id "
                + " INNER JOIN in_stockadjustment gr ON sta.stock_adjustment = gr.id AND gr.company = ? "
                + " INNER JOIN product p ON p.id = gr.product "
                + " WHERE (stad.approval_status = ? OR stad.repair_status IN (?,?)))";
        params.add(company.getCompanyID());
        params.add(ApprovalStatus.PENDING.ordinal());
        params.add(ApprovalStatus.REPAIRPENDING.ordinal());
        params.add(ApprovalStatus.RETURNTOREPAIR.ordinal());
        
        //Goods Receipt Approval
        query += "UNION (SELECT gr.gronumber,  p.productid, 'Goods Receipt'  FROM grodetailistmapping sta "
                + " INNER JOIN grodetails stad ON stad.id = sta.grodetail "
                + " INNER JOIN grorder gr ON stad.grorder = gr.id AND gr.company = ? "
                + " INNER JOIN product p ON p.id = stad.product "
                + " WHERE sta.quantitydue > 0)";
        params.add(company.getCompanyID());


        List list = executeSQLQuery( query, params.toArray());
        
        if (paging != null) {
            int totalCount = list.size();
            paging.setTotalRecord(totalCount);
            if (paging.isValid() && totalCount > paging.getLimit()) {
                list = executeSQLQueryPaging( query, params.toArray(), paging);
            }
        }
        return list;
    }

    @Override
    public void updateInventoryTab(Company company, boolean activate) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String query = "UPDATE ExtraCompanyPreferences SET activateInventoryTab = ? WHERE id = ? ";
            executeUpdate( query, new Object[]{activate, company.getCompanyID()});

            txnManager.commit(status);

        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "ActivateInventoryServiceImpl.activateInventoryTab", ex);
        }
    }

    @Override
    public void maintainWarehouseLocationHirarchy(Company company) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = accMasterItemsDAO.getLocationLevelMapping(company.getCompanyID());
            List<Object> resultList = result.getEntityList();

            for (Object resultObj : resultList) {
                HashMap<String, Object> requestParam = new HashMap<String, Object>();
                LocationLevelMapping levelMapping = (LocationLevelMapping) resultObj;
                requestParam.put("id", levelMapping.getID());
                String parent = "0";
                if (levelMapping.getLlevelid().getId() == 2) {
                    parent = "1";
                }
                requestParam.put("parent", parent);
                accMasterItemsDAO.updateMasterSetting(requestParam);
            }
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "ActivateInventoryServiceImpl.maintainWarehouseLocationHirarchy", ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "ActivateInventoryServiceImpl.maintainWarehouseLocationHirarchy", ex);
        }
    }

    @Override
    public void activateLocationWarehouseInCompany(User user, boolean activateBatch, boolean activateSerial) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            Company company = user.getCompany();
            CompanyAccountPreferences companyPreferences = (CompanyAccountPreferences) get(CompanyAccountPreferences.class, company.getCompanyID());
            boolean changesInCompPref = false;
            if (!companyPreferences.isIswarehousecompulsory()) {
                companyPreferences.setIswarehousecompulsory(true);
                changesInCompPref = true;
            }
            if (!companyPreferences.isIslocationcompulsory()) {
                companyPreferences.setIslocationcompulsory(true);
                changesInCompPref = true;
            }

            Store defaultStore = storeService.getDefaultStore(company);
            if (defaultStore == null) {
                List<Store> storeList = storeService.getStores(company, null, new Paging(0, 1));
                if (storeList != null && !storeList.isEmpty()) {
                    defaultStore = storeList.get(0);
                    defaultStore.setDefaultStore(true);
                    defaultStore.setActive(true);
                    storeService.updateStore(user, defaultStore);
                } else {
                    defaultStore = new Store("DS", "Default Store", "", StoreType.WAREHOUSE, company);
                    storeService.addStore(user, defaultStore);
                }
            }
            if (defaultStore.getDefaultLocation() == null) {
                Location defaultLocation = locationService.getDefaultLocation(company);
                if (defaultLocation == null) {
                    List<Location> locationList = locationService.getLocations(company, null, new Paging(0, 1));
                    if (locationList != null && !locationList.isEmpty()) {
                        defaultLocation = locationList.get(0);
                        defaultLocation.setDefaultLocation(true);
                        defaultLocation.setActive(true);
                        locationService.updateLocation(user, defaultLocation);
                    } else {
                        defaultLocation = new Location(company, "Default Location");
                        locationService.addLocation(user, defaultLocation);
                    }
                }
                defaultStore.setDefaultLocation(defaultLocation);
                storeService.updateStore(user, defaultStore);
            }
            if (activateBatch && !companyPreferences.isIsBatchCompulsory()) {
                companyPreferences.setIsBatchCompulsory(true);
                changesInCompPref = true;
            }
            if (activateSerial && !companyPreferences.isIsSerialCompulsory()) {
                companyPreferences.setIsSerialCompulsory(true);
                changesInCompPref = true;
            }
            if (changesInCompPref) {
                saveOrUpdate(companyPreferences);
            }
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "ActivateInventoryServiceImpl.activateLocationWarehouseInCompany", ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "ActivateInventoryServiceImpl.activateLocationWarehouseInCompany", ex);
        }
    }

    @Override
    public void activateLocationWarehouseInProduct(Product product, InventoryWarehouse warehouse, InventoryLocation location) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List collectedObjects = new ArrayList();
            if (product.isIswarehouseforproduct() || product.isIslocationforproduct() || product.isIsBatchForProduct()) {
                updateWarehouseLocationForNewProductBatch(product, warehouse, location);
            } else {//Sequence is mandatory
                NewProductBatch productBatch = null;

                productBatch = tagWLforOpeningTransactions(product, warehouse, location, productBatch, collectedObjects);//1
                productBatch = tagWLForGRTransactions(product, warehouse, location, productBatch, collectedObjects);//2
                productBatch = tagWLForDOTransactions(product, warehouse, location, productBatch, collectedObjects);//3
                productBatch = tagWLForSRTransactions(product, warehouse, location, productBatch, collectedObjects);//4
                productBatch = tagWLForPRTransactions(product, warehouse, location, productBatch, collectedObjects);//5
                productBatch = tagWLForProductBuildTransactions(product, warehouse, location, productBatch, collectedObjects);//6
                productBatch = tagWLForProductBuildDetailTransactions(product, warehouse, location, productBatch, collectedObjects);//7

                if (productBatch != null) {
                    saveOrUpdate(productBatch);
                    linkSerialsWithCreatedBatch(product, productBatch);
                }
            }
            saveAll(collectedObjects);

            if (!product.isIswarehouseforproduct() && product.isIslocationforproduct()) {
                String query = "UPDATE Product SET iswarehouseforproduct = ?, warehouse = ? WHERE ID = ? ";
                executeUpdate( query, new Object[]{true, warehouse, product.getID()});
            } else if (product.isIswarehouseforproduct() && !product.isIslocationforproduct()) {
                String query = "UPDATE Product SET islocationforproduct = ?, location = ? WHERE ID = ? ";
                executeUpdate( query, new Object[]{true, location, product.getID()});
            } else if (!product.isIswarehouseforproduct() && !product.isIslocationforproduct()) {
                String query = "UPDATE Product SET iswarehouseforproduct = ?, warehouse = ?, islocationforproduct = ?, location = ? WHERE ID = ? ";
                executeUpdate( query, new Object[]{true, warehouse, true, location, product.getID()});
            }

            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "activate Location Warehouse for Product [" + product.getProductid() + "]", ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "activate Location Warehouse for Product [" + product.getProductid() + "]", ex);
        } finally {
            System.gc();
        }
    }

    @Override
    public void removeStockDataFromInventoryTables(Company company) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {

            String deleteQuery = "DELETE FROM StockMovement WHERE company = ? AND transactionModule NOT IN (?,?,?,?,?,?,?)";
            List params = new ArrayList();
            params.add(company);
            params.add(TransactionModule.STOCK_ADJUSTMENT);
            params.add(TransactionModule.STOCK_REQUEST);
            params.add(TransactionModule.ISSUE_NOTE);
            params.add(TransactionModule.INTER_STORE_TRANSFER);
            params.add(TransactionModule.INTER_LOCATION_TRANSFER);
            params.add(TransactionModule.CYCLE_COUNT);
            params.add(TransactionModule.IMPORT); // for olympus import Stock movement
            executeUpdate( deleteQuery, params.toArray());

            String query = "DELETE FROM Stock WHERE company = ? ";
            executeUpdate( query, company);

            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "ActivateInventoryServiceImpl.removeStockDataFromInventoryTables", ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "ActivateInventoryServiceImpl.removeStockDataFromInventoryTables", ex);
        }
    }

    @Override
    public void insertStockFromSM(Company company, Map<String, Store> storeMap, Map<String, Location> locationMap) {
        Paging paging = new Paging(0, TRANSACTION_LIMIT);
        boolean hasMore;
        do {
            hasMore = false;
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("STR_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            try {
                String query = "SELECT smd.stockMovement.product, smd.stockMovement.store.id, smd.location.id, row, rack, bin, smd.batchName, smd.serialNames, smd.quantity, smd.stockMovement.transactionType "
                        + " FROM StockMovementDetail smd "
                        + " LEFT JOIN smd.row row "
                        + " LEFT JOIN smd.rack rack "
                        + " LEFT JOIN smd.bin bin "
                        + " WHERE smd.stockMovement IS NOT NULL AND smd.stockMovement.company = ? ";
                List<Object[]> smdList = executeQueryPaging( query, new Object[]{company}, paging);
                for (Object[] smd : smdList) {
                    Product product = smd[0] != null ? (Product) smd[0] : null;
                    String storeId = smd[1] != null ? (String) smd[1] : null;
                    String locationId = smd[2] != null ? (String) smd[2] : null;
                    StoreMaster row = smd[3] != null ? (StoreMaster) smd[3] : null;
                    StoreMaster rack = smd[4] != null ? (StoreMaster) smd[4] : null;
                    StoreMaster bin = smd[5] != null ? (StoreMaster) smd[5] : null;
                    String batchName = smd[6] != null ? (String) smd[6] : null;
                    String serialNames = smd[7] != null ? (String) smd[7] : null;
                    double qty = smd[8] != null ? (Double) smd[8] : 0;
                    TransactionType transactionType = smd[9] != null ? (TransactionType) smd[9] : null;
                    Store store = storeMap.get(storeId);
                    if (store == null) {
                        store = storeService.getStoreById(storeId);
                    }
                    Location location = locationMap.get(locationId);
                    if (transactionType == TransactionType.OUT) {
                        stockService.decreaseInventory(product, store, location, row, rack, bin, batchName, serialNames, qty);
                    } else {
                        stockService.increaseInventory(product, store, location, row, rack, bin, batchName, serialNames, qty);
                    }
                }
                if (smdList.size() == paging.getLimit()) {
                    paging.setOffset(paging.getOffset() + paging.getLimit());
                    hasMore = true;
                }
                txnManager.commit(status);
            } catch (ServiceException ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "insertStockFromSM for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } catch (Exception ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "insertStockFromSM for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } finally {
                System.gc();
            }
        } while (hasMore);

    }

    private NewProductBatch tagWLforOpeningTransactions(Product product, InventoryWarehouse warehouse, InventoryLocation location, NewProductBatch productBatch, List collectedObjects) throws ServiceException {
        String query = "SELECT inv.baseuomquantity FROM Inventory inv WHERE inv.product = ? AND inv.newInv = ? ";
        List params = new ArrayList();
        params.add(product);
        params.add(true);

        List inventoryList = executeQuery( query, params.toArray());
        for (Object inv : inventoryList) {
            int transactionType = TT_OPENING;
            double qty = inv != null ? (Double) inv : 0;
            if (productBatch != null) {
//                productBatch.setQuantity(productBatch.getQuantity() + qty);
                productBatch.setQuantity(authHandler.roundQuantity((productBatch.getQuantity() + qty),product.getCompany().getCompanyID()));
                productBatch.setQuantitydue(authHandler.roundQuantity((productBatch.getQuantitydue() + qty),product.getCompany().getCompanyID()));
            } else {
                productBatch = createNewBatchEntry(product, warehouse, location, qty, qty, 28, false, true);
            }
            LocationBatchDocumentMapping documentMapping = createBatchDocumentEntry(productBatch, product.getID(), qty, transactionType);
            collectedObjects.add(documentMapping);
        }
        return productBatch;
    }

    private NewProductBatch tagWLForGRTransactions(Product product, InventoryWarehouse warehouse, InventoryLocation location, NewProductBatch productBatch, List collectedObjects) throws ServiceException {

        String query = "SELECT grod.ID, grod.baseuomdeliveredquantity FROM GoodsReceiptOrderDetails grod WHERE grod.product = ? AND grod.grOrder.deleted = ?";
        List params = new ArrayList();
        params.add(product);
        params.add(false);
        List<Object[]> grodList = executeQuery( query, params.toArray());
        for (Object[] grod : grodList) {
            int trasactiontype = TT_GRN;
            String grodId = grod[0] != null ? (String) grod[0] : null;
            double qty = grod[1] != null ? (Double) grod[1] : 0;
            if (productBatch != null) {
                //productBatch.setQuantity(productBatch.getQuantity() + qty);
                productBatch.setQuantity(authHandler.roundQuantity((productBatch.getQuantity() + qty),product.getCompany().getCompanyID()));
                productBatch.setQuantitydue(authHandler.roundQuantity((productBatch.getQuantitydue() + qty),product.getCompany().getCompanyID()));
            } else {
                productBatch = createNewBatchEntry(product, warehouse, location, qty, qty, 28, false, true);
            }
            LocationBatchDocumentMapping documentMapping = createBatchDocumentEntry(productBatch, grodId, qty, trasactiontype);
            collectedObjects.add(documentMapping);
        }
        return productBatch;
    }

    private NewProductBatch tagWLForDOTransactions(Product product, InventoryWarehouse warehouse, InventoryLocation location, NewProductBatch productBatch, List collectedObjects) throws ServiceException {

        String query = "SELECT dod.ID, dod.baseuomdeliveredquantity FROM DeliveryOrderDetail dod WHERE dod.product = ? AND dod.deliveryOrder.deleted = ?";
        List params = new ArrayList();
        params.add(product);
        params.add(false);

        List<Object[]> dodList = executeQuery( query, params.toArray());
        for (Object[] dod : dodList) {
            int trasactiontype = TT_DO;
            String dodId = dod[0] != null ? (String) dod[0] : null;
            double qty = dod[1] != null ? (Double) dod[1] : 0;
            if (productBatch != null) {
                productBatch.setQuantitydue(authHandler.roundQuantity((productBatch.getQuantitydue() - qty),product.getCompany().getCompanyID()));
            } else {
                productBatch = createNewBatchEntry(product, warehouse, location, qty, qty - qty, 28, false, true); // need to confirm with mayur - must be quantity = 0 AND quantitydue = 0-qty
            }
            LocationBatchDocumentMapping documentMapping = createBatchDocumentEntry(productBatch, dodId, qty, trasactiontype);
            collectedObjects.add(documentMapping);
        }
        return productBatch;
    }

    private NewProductBatch tagWLForSRTransactions(Product product, InventoryWarehouse warehouse, InventoryLocation location, NewProductBatch productBatch, List collectedObjects) throws ServiceException {
        String query = "SELECT srd.ID, srd.baseuomquantity FROM SalesReturnDetail srd WHERE srd.product = ? AND srd.salesReturn.deleted = ?";
        List params = new ArrayList();
        params.add(product);
        params.add(false);

        List<Object[]> srdList = executeQuery( query, params.toArray());
        for (Object[] srd : srdList) {
            int trasactiontype = TT_SR;
            String srdId = srd[0] != null ? (String) srd[0] : null;
            double qty = srd[1] != null ? (Double) srd[1] : 0;
            if (productBatch != null) {
                productBatch.setQuantitydue(authHandler.roundQuantity((productBatch.getQuantitydue() + qty),product.getCompany().getCompanyID()));
            } else {
                productBatch = createNewBatchEntry(product, warehouse, location, qty, qty, 28, false, true);
            }
            LocationBatchDocumentMapping documentMapping = createBatchDocumentEntry(productBatch, srdId, qty, trasactiontype);
            collectedObjects.add(documentMapping);
        }
        return productBatch;
    }

    private NewProductBatch tagWLForPRTransactions(Product product, InventoryWarehouse warehouse, InventoryLocation location, NewProductBatch productBatch, List collectedObjects) throws ServiceException {
        String query = "SELECT prd.ID, prd.baseuomquantity FROM PurchaseReturnDetail prd WHERE prd.product = ? AND prd.purchaseReturn.deleted = ?";
        List params = new ArrayList();
        params.add(product);
        params.add(false);

        List<Object[]> prdList = executeQuery( query, params.toArray());
        for (Object[] prd : prdList) {
            int trasactiontype = TT_PR;
            String prdId = prd[0] != null ? (String) prd[0] : null;
            double qty = prd[1] != null ? (Double) prd[1] : 0;
            if (productBatch != null) {
                productBatch.setQuantitydue(authHandler.roundQuantity((productBatch.getQuantitydue() - qty),product.getCompany().getCompanyID()));
            } else {
                productBatch = createNewBatchEntry(product, warehouse, location, -qty, -qty, 28, false, true); // need to confirm with mayur - must be quantity = 0 AND quantitydue = 0-qty
            }
            LocationBatchDocumentMapping documentMapping = createBatchDocumentEntry(productBatch, prdId, qty, trasactiontype);
            collectedObjects.add(documentMapping);
        }
        return productBatch;
    }

    private NewProductBatch tagWLForProductBuildTransactions(Product product, InventoryWarehouse warehouse, InventoryLocation location, NewProductBatch productBatch, List collectedObjects) throws ServiceException {
        String query = "SELECT pb.ID, pb.quantity FROM ProductBuild pb WHERE pb.product = ? ";
        List params = new ArrayList();
        params.add(product);
        List<Object[]> pbList = executeQuery( query, params.toArray());
        for (Object[] pb : pbList) {
            int trasactiontype = TT_PB;
            String pbId = pb[0] != null ? (String) pb[0] : null;
            double qty = pb[1] != null ? (Double) pb[1] : 0;
            if (productBatch != null) {
                productBatch.setQuantity(productBatch.getQuantitydue() + qty); // need to disscuss with mayur
                productBatch.setQuantitydue(authHandler.roundQuantity((productBatch.getQuantitydue() + qty),product.getCompany().getCompanyID()));
            } else {
                productBatch = createNewBatchEntry(product, warehouse, location, qty, qty, 28, false, true);
            }
            LocationBatchDocumentMapping documentMapping = createBatchDocumentEntry(productBatch, pbId, qty, trasactiontype);
            collectedObjects.add(documentMapping);
        }
        return productBatch;
    }

    private NewProductBatch tagWLForProductBuildDetailTransactions(Product product, InventoryWarehouse warehouse, InventoryLocation location, NewProductBatch productBatch, List collectedObjects) throws ServiceException {
        String query = "SELECT pbd.ID, pbd.inventoryQuantity, pbd.build.quantity FROM ProductBuildDetails pbd WHERE pbd.aproduct = ?";
        List params = new ArrayList();
        params.add(product);
        List<Object[]> pbdList = executeQuery( query, params.toArray());
        for (Object[] pbd : pbdList) {
            int trasactiontype = TT_PBD;
            String pbdId = pbd[0] != null ? (String) pbd[0] : null;
            double iqty = pbd[1] != null ? (Double) pbd[1] : 0;
            double bqty = pbd[2] != null ? (Double) pbd[2] : 0;
            double qty = iqty * bqty;
            if (productBatch != null) {
                productBatch.setQuantity(productBatch.getQuantitydue() - qty); // need to disscuss with mayur
                productBatch.setQuantitydue(authHandler.roundQuantity((productBatch.getQuantitydue() - qty),productBatch.getCompany().getCompanyID()));
            } else {
                productBatch = createNewBatchEntry(product, warehouse, location, qty, qty, 28, false, true);
            }
            LocationBatchDocumentMapping documentMapping = createBatchDocumentEntry(productBatch, pbdId, qty, trasactiontype);
            collectedObjects.add(documentMapping);
        }
        return productBatch;
    }

    @Override
    public void insertSMForOpeningTransactions(Company company, Map<String, Store> storeMap, Map<String, Location> locationMap) {
        boolean hasMore;
        Paging paging = new Paging(0, TRANSACTION_LIMIT);
        do {
            hasMore = false;
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("STR_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            try {

                String query = "SELECT inv.ID, inv.product, inv.baseuomrate, inv.updateDate FROM Inventory inv WHERE inv.company = ? AND inv.product.producttype.ID IN(?,?,?) "
                        + " AND inv.product.iswarehouseforproduct = ? AND inv.product.islocationforproduct = ? "
                        + " AND inv.newInv = ?";

                List params = new ArrayList();
                params.add(company);
                params.add(Producttype.INVENTORY_PART);
                params.add(Producttype.ASSEMBLY);
                params.add(Producttype.Inventory_Non_Sales);
                params.add(true);
                params.add(true);
                params.add(true);

                List<Object[]> openingList = executeQueryPaging( query, params.toArray(), paging);

                for (Object[] opening : openingList) {

                    String openingId = opening[0] != null ? (String) opening[0] : null;
                    Product product = opening[1] != null ? (Product) opening[1] : null;
                    double baseuomrate = opening[2] != null ? (Double) opening[2] : 0;
                    Date updateDate = opening[3] != null ? (Date) opening[3] : null;


                    Map<String, StockMovement> smMap = new HashMap<>();

                    Map<String, String> serialMapping = null;
                    if (product.isIsSerialForProduct()) {
                        serialMapping = getMappedSerialNames(product.getID());
                    }

                    query = "SELECT pb.id, pb.batchname, pb.warehouse, pb.location, pb.`row`, pb.rack, pb.bin, SUM(dm.quantity) "
                        +" FROM locationbatchdocumentmapping dm "
                        +" INNER JOIN newproductbatch pb ON pb.id = dm.batchmapid"
                        +" WHERE dm.documentid = ? AND dm.batchmapid IS NOT NULL AND pb.warehouse IS NOT NULL AND pb.location IS NOT NULL "
                        +" GROUP BY pb.id ";
                    List<Object[]> batchMappinglist = executeSQLQuery(query, new Object[]{product.getID()});
                    for (Object[] batchMapping : batchMappinglist) {
                        String batchId = batchMapping[0] != null ? (String) batchMapping[0] : null;
                        String batchName = batchMapping[1] != null ? (String) batchMapping[1] : "";
                        String warehouseId = batchMapping[2] != null ? (String) batchMapping[2] : null;
                        String locationId = batchMapping[3] != null ? (String) batchMapping[3] : null;
                        StoreMaster row =  null;
                        if(product.isIsrowforproduct()){
                            String rowId = batchMapping[4] != null ? (String) batchMapping[4] : null;
                            row = storeService.getStoreMaster(rowId);
                        }
                        StoreMaster rack = null;
                        if(product.isIsrackforproduct()){
                            String rackId = batchMapping[5] != null ? (String) batchMapping[5] : null;
                            rack = storeService.getStoreMaster(rackId);
                        }
                        StoreMaster bin = null;
                        if(product.isIsbinforproduct()){
                            String rbinId = batchMapping[6] != null ? (String) batchMapping[6] : null;
                            bin = storeService.getStoreMaster(rbinId);
                        }
                        double qty = batchMapping[7] != null ? (Double) batchMapping[7] : 0;

                        Store store = storeMap.get(warehouseId);

                        Location location = locationMap.get(locationId);

                        String serialNames = serialMapping != null ? serialMapping.get(batchId) : null;
                        if (qty != 0) {
                            String key = product.getID() + store.getId();
                            if (smMap.containsKey(key)) {
                                StockMovement sm = smMap.get(key);
                                StockMovementDetail smd = new StockMovementDetail(sm, location, row, rack, bin, batchName, serialNames, qty);
                                sm.setQuantity(sm.getQuantity() + smd.getQuantity());
                                sm.getStockMovementDetails().add(smd);
                            } else {
                                StockMovement sm = new StockMovement(product, store, qty, baseuomrate, product.getProductid(), updateDate, TransactionType.OPENING, TransactionModule.ERP_PRODUCT, product.getID(),product.getID());
                                sm.setStockUoM(product.getUnitOfMeasure());
                                sm.setCreatedOn(updateDate);
                                sm.setRemark("Stock added throw OPENING");
                                StockMovementDetail smd = new StockMovementDetail(sm, location, row, rack, bin, batchName, serialNames, qty);
                                sm.getStockMovementDetails().add(smd);

                                smMap.put(key, sm);
                            }
                        }
                    }
                    saveAll(smMap.values());
                }
                if (openingList.size() == paging.getLimit()) {
                    hasMore = true;
                    paging.setOffset(paging.getOffset() + paging.getLimit());
                }
                txnManager.commit(status);
            } catch (ServiceException ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "insertSMForOpeningTransactions for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } catch (Exception ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "insertSMForOpeningTransactions for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } finally {
                System.gc();
            }
        } while (hasMore);
    }

    @Override
    public void insertSMForGRNTransactions(Company company, Map<String, Store> storeMap, Map<String, Location> locationMap) {
        boolean hasMore;
        Paging paging = new Paging(0, TRANSACTION_LIMIT);
        do {
            hasMore = false;
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("STR_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            try {

                String query = "SELECT grod.ID, grod.product, grod.baseuomrate, grod.grOrder.goodsReceiptOrderNumber, grod.grOrder.orderDate, cc, "
                        + " vendor,grod.grOrder.createdon,grod.grOrder.ID,grod.rate "
                        + " FROM GoodsReceiptOrderDetails grod "
                        + " LEFT JOIN grod.grOrder.costcenter cc "
                        + " LEFT JOIN grod.grOrder.vendor vendor "
                        + " WHERE grod.company = ? AND grod.product.producttype.ID IN(?,?,?) AND grod.product.iswarehouseforproduct = ? AND grod.product.islocationforproduct = ? "
                        + " AND grod.grOrder.fixedAssetGRO = ? AND grod.grOrder IS NOT NULL and grod.grOrder.deleted=? ";
                List params = new ArrayList();
                params.add(company);
                params.add(Producttype.INVENTORY_PART);
                params.add(Producttype.ASSEMBLY);
                params.add(Producttype.Inventory_Non_Sales);
                params.add(true);
                params.add(true);
                params.add(false);
                params.add(false);

                List<Object[]> grodList = executeQuery( query, params.toArray());
                if (grodList.size() == paging.getLimit()) {
                    hasMore = true;
                    paging.setOffset(paging.getOffset() + paging.getLimit());
                }
                for (Object[] grod : grodList) {

                    String grodId = grod[0] != null ? (String) grod[0] : null;
                    Product product = grod[1] != null ? (Product) grod[1] : null;
                    double baseUomRate = grod[2] != null ? (Double) grod[2] : null;
                    String transactionNo = grod[3] != null ? (String) grod[3] : null;
                    Date orderDate = grod[4] != null ? (Date) grod[4] : null;
                    CostCenter costCenter = grod[5] != null ? (CostCenter) grod[5] : null;
                    Vendor vendor = grod[6] != null ? (Vendor) grod[6] : null;
                    Long createdDate = grod[7] != null ? (Long) grod[7] : 0;
                    String grId = grod[8] != null ? (String) grod[8] : null;
                    double rate = grod[9] != null ? (Double) grod[9] : null;

                    Map<String, StockMovement> smMap = new HashMap<>();
                    Map<String, String> serialMapping = null;
                    if (product.isIsSerialForProduct()) {
                        serialMapping = getMappedSerialNames(grodId);
                    }

                    query = "SELECT pb.id, pb.batchname, pb.warehouse, pb.location, pb.`row`, pb.rack, pb.bin, SUM(dm.quantity) "
                        +" FROM locationbatchdocumentmapping dm "
                        +" INNER JOIN newproductbatch pb ON pb.id = dm.batchmapid"
                        +" WHERE dm.documentid = ? AND dm.batchmapid IS NOT NULL AND pb.warehouse IS NOT NULL AND pb.location IS NOT NULL "
                        +" GROUP BY pb.id ";
                    List<Object[]> batchMappinglist = executeSQLQuery(query, new Object[]{grodId});
                    for (Object[] batchMapping : batchMappinglist) {
                        String batchId = batchMapping[0] != null ? (String) batchMapping[0] : null;
                        String batchName = batchMapping[1] != null ? (String) batchMapping[1] : "";
                        String warehouseId = batchMapping[2] != null ? (String) batchMapping[2] : null;
                        String locationId = batchMapping[3] != null ? (String) batchMapping[3] : null;
                        StoreMaster row =  null;
                        if(product.isIsrowforproduct()){
                            String rowId = batchMapping[4] != null ? (String) batchMapping[4] : null;
                            row = storeService.getStoreMaster(rowId);
                        }
                        StoreMaster rack = null;
                        if(product.isIsrackforproduct()){
                            String rackId = batchMapping[5] != null ? (String) batchMapping[5] : null;
                            rack = storeService.getStoreMaster(rackId);
                        }
                        StoreMaster bin = null;
                        if(product.isIsbinforproduct()){
                            String rbinId = batchMapping[6] != null ? (String) batchMapping[6] : null;
                            bin = storeService.getStoreMaster(rbinId);
                        }
                        double qty = batchMapping[7] != null ? (Double) batchMapping[7] : null;

                        Store store = storeMap.get(warehouseId);

                        Location location = locationMap.get(locationId);

                        String serialNames = serialMapping != null ? serialMapping.get(batchId) : null;
                        if (qty != 0) {
                            String key = product.getID() + store.getId();
                            if (smMap.containsKey(key)) {
                                StockMovement sm = smMap.get(key);
                                StockMovementDetail smd = new StockMovementDetail(sm, location, row, rack, bin, batchName, serialNames, qty);
                                sm.setQuantity(sm.getQuantity() + smd.getQuantity());
                                sm.getStockMovementDetails().add(smd);
                            } else {
                                 double price = 0;
                                if (baseUomRate < 1) {
                                    price = (1/baseUomRate) * rate;
                                } else if (baseUomRate > 0) {
                                    price = rate / baseUomRate;
                                }
                                StockMovement sm = new StockMovement(product, store, qty, price, transactionNo, orderDate, TransactionType.IN, TransactionModule.ERP_GRN, grId,grodId);
                                sm.setStockUoM(product.getUnitOfMeasure());
                                sm.setCostCenter(costCenter);
                                sm.setVendor(vendor);
                                sm.setCreatedOn(new Date(createdDate));
                                sm.setRemark("Stock added through GRN");
                                StockMovementDetail smd = new StockMovementDetail(sm, location, row, rack, bin, batchName, serialNames, qty);
                                sm.getStockMovementDetails().add(smd);

                                smMap.put(key, sm);

                            }
                        }
                    }
                    saveAll(smMap.values());
                }
                txnManager.commit(status);
            } catch (ServiceException ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "insertSMForGRNTransactions for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } catch (Exception ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "insertSMForGRNTransactions for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } finally {
                System.gc();
            }
        } while (hasMore);
    }

    @Override
    public void insertSMForDOTransactions(Company company, Map<String, Store> storeMap, Map<String, Location> locationMap) {
        boolean hasMore;
        Paging paging = new Paging(0, TRANSACTION_LIMIT);
        do {
            hasMore = false;
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("STR_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            try {

                String query = "SELECT dod.ID, dod.product, dod.baseuomrate, dod.deliveryOrder.deliveryOrderNumber, "
                        + " dod.deliveryOrder.orderDate, cc, customer, dod.deliveryOrder.createdon,dod.deliveryOrder.ID,dod.rate "
                        + " FROM DeliveryOrderDetail dod "
                        + " LEFT JOIN dod.deliveryOrder.costcenter cc "
                        + " LEFT JOIN dod.deliveryOrder.customer customer "
                        + " WHERE dod.company = ? AND dod.product.producttype.ID IN(?,?) AND dod.product.iswarehouseforproduct = ? AND dod.product.islocationforproduct = ? "
                        + " AND dod.deliveryOrder.fixedAssetDO = ? AND dod.deliveryOrder IS NOT NULL and dod.deliveryOrder.deleted=? and dod.deliveryOrder.approvestatuslevel=11";
                List params = new ArrayList();
                params.add(company);
                params.add(Producttype.INVENTORY_PART);
                params.add(Producttype.ASSEMBLY);
                params.add(true);
                params.add(true);
                params.add(false);
                params.add(false);
                List<Object[]> dodList = executeQuery( query, params.toArray());
                if (dodList.size() == paging.getLimit()) {
                    hasMore = true;
                    paging.setOffset(paging.getOffset() + paging.getLimit());
                }
                for (Object[] dod : dodList) {
                    String dodId = dod[0] != null ? (String) dod[0] : null;
                    Product product = dod[1] != null ? (Product) dod[1] : null;
                    double baseUomRate = dod[2] != null ? (Double) dod[2] : null;
                    double rate = dod[9] != null ? (Double) dod[9] : null;
                    String transactionNo = dod[3] != null ? (String) dod[3] : null;
                    Date orderDate = dod[4] != null ? (Date) dod[4] : null;
                    CostCenter costCenter = dod[5] != null ? (CostCenter) dod[5] : null;
                    Customer customer = dod[6] != null ? (Customer) dod[6] : null;
                    Long createdDate = dod[7] != null ? (Long) dod[7] : 0;
                    String doId = dod[8] != null ? (String) dod[8] : null;

                    Map<String, StockMovement> smMap = new HashMap<>();
                    Map<String, String> serialMapping = null;
                    if (product.isIsSerialForProduct()) {
                        serialMapping = getMappedSerialNames(dodId);
                    }

                    query = "SELECT pb.id, pb.batchname, pb.warehouse, pb.location, pb.`row`, pb.rack, pb.bin, SUM(dm.quantity) "
                        +" FROM locationbatchdocumentmapping dm "
                        +" INNER JOIN newproductbatch pb ON pb.id = dm.batchmapid"
                        +" WHERE dm.documentid = ? AND dm.batchmapid IS NOT NULL AND pb.warehouse IS NOT NULL AND pb.location IS NOT NULL "
                        +" GROUP BY pb.id ";
                    List<Object[]> batchMappinglist = executeSQLQuery(query, new Object[]{dodId});
                    for (Object[] batchMapping : batchMappinglist) {
                        String batchId = batchMapping[0] != null ? (String) batchMapping[0] : null;
                        String batchName = batchMapping[1] != null ? (String) batchMapping[1] : "";
                        String warehouseId = batchMapping[2] != null ? (String) batchMapping[2] : null;
                        String locationId = batchMapping[3] != null ? (String) batchMapping[3] : null;
                        StoreMaster row =  null;
                        if(product.isIsrowforproduct()){
                            String rowId = batchMapping[4] != null ? (String) batchMapping[4] : null;
                            row = storeService.getStoreMaster(rowId);
                        }
                        StoreMaster rack = null;
                        if(product.isIsrackforproduct()){
                            String rackId = batchMapping[5] != null ? (String) batchMapping[5] : null;
                            rack = storeService.getStoreMaster(rackId);
                        }
                        StoreMaster bin = null;
                        if(product.isIsbinforproduct()){
                            String rbinId = batchMapping[6] != null ? (String) batchMapping[6] : null;
                            bin = storeService.getStoreMaster(rbinId);
                        }
                        
                        double qty = batchMapping[7] != null ? (Double) batchMapping[7] : null;

                        Store store = storeMap.get(warehouseId);

                        Location location = locationMap.get(locationId);

                        String serialNames = serialMapping != null ? serialMapping.get(batchId) : null;
                        if (qty != 0) {
                            String key = product.getID() + store.getId();
                            if (smMap.containsKey(key)) {
                                StockMovement sm = smMap.get(key);
                                StockMovementDetail smd = new StockMovementDetail(sm, location, row, rack, bin, batchName, serialNames, qty);
                                sm.setQuantity(sm.getQuantity() + smd.getQuantity());
                                sm.getStockMovementDetails().add(smd);
                            } else {
                              
                                double price = 0;
                                if (baseUomRate < 1) {
                                    price = (1/baseUomRate) * rate;
                                } else if (baseUomRate > 0) {
                                    price = rate / baseUomRate;
                                }
                                StockMovement sm = new StockMovement(product, store, qty, price, transactionNo, orderDate, TransactionType.OUT, TransactionModule.ERP_DO, doId,dodId);
                                sm.setStockUoM(product.getUnitOfMeasure());
                                sm.setCostCenter(costCenter);
                                sm.setCustomer(customer);
                                sm.setCreatedOn(new Date(createdDate));
                                sm.setRemark("Stock out through DO");
                                StockMovementDetail smd = new StockMovementDetail(sm, location, row, rack, bin, batchName, serialNames, qty);
                                sm.getStockMovementDetails().add(smd);

                                smMap.put(key, sm);

                            }
                        }

                    }
                    saveAll(smMap.values());
                }
                txnManager.commit(status);
            } catch (ServiceException ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "insertSMForDOTransactions for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } catch (Exception ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "insertSMForDOTransactions for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            }
        } while (hasMore);
    }

    @Override
    public void insertSMForPRTransactions(Company company, Map<String, Store> storeMap, Map<String, Location> locationMap) {
        boolean hasMore;
        Paging paging = new Paging(0, TRANSACTION_LIMIT);
        do {
            hasMore = false;
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("STR_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            try {

                String query = "SELECT prd.ID, prd.product, prd.baseuomrate, prd.purchaseReturn.purchaseReturnNumber, prd.purchaseReturn.orderDate, cc, vendor,prd.purchaseReturn.createdon,prd.purchaseReturn.ID "
                        + " FROM PurchaseReturnDetail prd "
                        + " LEFT JOIN prd.purchaseReturn.costcenter cc "
                        + " LEFT JOIN prd.purchaseReturn.vendor vendor "
                        + " WHERE prd.company = ? AND prd.product.producttype.ID IN(?,?,?) AND prd.product.iswarehouseforproduct = ? AND prd.product.islocationforproduct = ? "
                        + " AND prd.purchaseReturn.fixedAsset = ? AND prd.purchaseReturn IS NOT NULL";
                List params = new ArrayList();
                params.add(company);
                params.add(Producttype.INVENTORY_PART);
                params.add(Producttype.ASSEMBLY);
                params.add(Producttype.Inventory_Non_Sales);
                params.add(true);
                params.add(true);
                params.add(false);
                List<Object[]> prdList = executeQuery( query, params.toArray());
                if (prdList.size() == paging.getLimit()) {
                    hasMore = true;
                    paging.setOffset(paging.getOffset() + paging.getLimit());
                }
                for (Object[] prd : prdList) {

                    String prdId = prd[0] != null ? (String) prd[0] : null;
                    Product product = prd[1] != null ? (Product) prd[1] : null;
                    double baseUomRate = prd[2] != null ? (Double) prd[2] : null;
                    String transactionNo = prd[3] != null ? (String) prd[3] : null;
                    Date orderDate = prd[4] != null ? (Date) prd[4] : null;
                    CostCenter costCenter = prd[5] != null ? (CostCenter) prd[5] : null;
                    Vendor vendor = prd[6] != null ? (Vendor) prd[6] : null;
                    Long createdDate = prd[7] != null ? (Long) prd[7] : 0;
                    String prId = prd[8] != null ? (String) prd[8] : null;

                    Map<String, StockMovement> smMap = new HashMap<String, StockMovement>();
                    Map<String, String> serialMapping = null;
                    if (product.isIsSerialForProduct()) {
                        serialMapping = getMappedSerialNames(prdId);
                    }

                    query = "SELECT pb.id, pb.batchname, pb.warehouse, pb.location, pb.`row`, pb.rack, pb.bin, SUM(dm.quantity) "
                        +" FROM locationbatchdocumentmapping dm "
                        +" INNER JOIN newproductbatch pb ON pb.id = dm.batchmapid"
                        +" WHERE dm.documentid = ? AND dm.batchmapid IS NOT NULL AND pb.warehouse IS NOT NULL AND pb.location IS NOT NULL "
                        +" GROUP BY pb.id ";
                    List<Object[]> batchMappinglist = executeSQLQuery(query, new Object[]{prdId});

                    for (Object[] batchMapping : batchMappinglist) {
                        String batchId = batchMapping[0] != null ? (String) batchMapping[0] : null;
                        String batchName = batchMapping[1] != null ? (String) batchMapping[1] : "";
                        String warehouseId = batchMapping[2] != null ? (String) batchMapping[2] : null;
                        String locationId = batchMapping[3] != null ? (String) batchMapping[3] : null;
                        StoreMaster row =  null;
                        if(product.isIsrowforproduct()){
                            String rowId = batchMapping[4] != null ? (String) batchMapping[4] : null;
                            row = storeService.getStoreMaster(rowId);
                        }
                        StoreMaster rack = null;
                        if(product.isIsrackforproduct()){
                            String rackId = batchMapping[5] != null ? (String) batchMapping[5] : null;
                            rack = storeService.getStoreMaster(rackId);
                        }
                        StoreMaster bin = null;
                        if(product.isIsbinforproduct()){
                            String rbinId = batchMapping[6] != null ? (String) batchMapping[6] : null;
                            bin = storeService.getStoreMaster(rbinId);
                        }
                        double qty = batchMapping[7] != null ? (Double) batchMapping[7] : null;

                        Store store = storeMap.get(warehouseId);

                        Location location = locationMap.get(locationId);

                        String serialNames = serialMapping != null ? serialMapping.get(batchId) : null;
                        if (qty != 0) {
                            String key = product.getID() + store.getId();
                            if (smMap.containsKey(key)) {
                                StockMovement sm = smMap.get(key);
                                StockMovementDetail smd = new StockMovementDetail(sm, location, row, rack, bin, batchName, serialNames, qty);
                                sm.setQuantity(sm.getQuantity() + smd.getQuantity());
                                sm.getStockMovementDetails().add(smd);
                            } else {
                                StockMovement sm = new StockMovement(product, store, qty, baseUomRate, transactionNo, orderDate, TransactionType.OUT, TransactionModule.ERP_PURCHASE_RETURN, prId,prdId);
                                sm.setStockUoM(product.getUnitOfMeasure());
                                sm.setCostCenter(costCenter);
                                sm.setVendor(vendor);
                                sm.setCreatedOn(new Date(createdDate));
                                sm.setRemark("Stock out through Purchase Return");
                                StockMovementDetail smd = new StockMovementDetail(sm, location, row, rack, bin, batchName, serialNames, qty);
                                sm.getStockMovementDetails().add(smd);

                                smMap.put(key, sm);
                            }
                        }
                    }
                    saveAll(smMap.values());
                }
                txnManager.commit(status);
            } catch (ServiceException ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "insertSMForPRTransactions for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } catch (Exception ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "insertSMForPRTransactions for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            }
        } while (hasMore);
    }

    @Override
    public void insertSMForSRTransactions(Company company, Map<String, Store> storeMap, Map<String, Location> locationMap) {
        boolean hasMore;
        Paging paging = new Paging(0, TRANSACTION_LIMIT);
        do {
            hasMore = false;
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("STR_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            try {

                String query = "SELECT srd.ID, srd.product, srd.baseuomrate, srd.salesReturn.salesReturnNumber, srd.salesReturn.orderDate, cc, "
                        + " customer, srd.salesReturn.createdon,srd.salesReturn.ID "
                        + " FROM SalesReturnDetail srd "
                        + " LEFT JOIN srd.salesReturn.costcenter cc "
                        + " LEFT JOIN srd.salesReturn.customer customer "
                        + " WHERE srd.company = ? AND srd.product.producttype.ID IN(?,?) AND srd.product.iswarehouseforproduct = ? "
                        + " AND srd.product.islocationforproduct = ? "
                        + " AND srd.salesReturn.fixedAsset = ? AND srd.salesReturn IS NOT NULL and srd.salesReturn.deleted=? ";
                List params = new ArrayList();
                params.add(company);
                params.add(Producttype.INVENTORY_PART);
                params.add(Producttype.ASSEMBLY);
                params.add(true);
                params.add(true);
                params.add(false);
                params.add(false);
                List<Object[]> srdList = executeQuery( query, params.toArray());
                if (srdList.size() == paging.getLimit()) {
                    hasMore = true;
                    paging.setOffset(paging.getOffset() + paging.getLimit());
                }
                for (Object[] srd : srdList) {

                    String srdId = srd[0] != null ? (String) srd[0] : null;
                    Product product = srd[1] != null ? (Product) srd[1] : null;
                    double baseUomRate = srd[2] != null ? (Double) srd[2] : null;
                    String transactionNo = srd[3] != null ? (String) srd[3] : null;
                    Date orderDate = srd[4] != null ? (Date) srd[4] : null;
                    CostCenter costCenter = srd[5] != null ? (CostCenter) srd[5] : null;
                    Customer customer = srd[6] != null ? (Customer) srd[6] : null;
                    Long createdDate = srd[7] != null ? (Long) srd[7] : null;
                    String srId = srd[8] != null ? (String) srd[8] : null;

                    Map<String, StockMovement> smMap = new HashMap<String, StockMovement>();
                    Map<String, String> serialMapping = null;
                    if (product.isIsSerialForProduct()) {
                        serialMapping = getMappedSerialNames(srdId);
                    }

                    query = "SELECT pb.id, pb.batchname, pb.warehouse, pb.location, pb.`row`, pb.rack, pb.bin, SUM(dm.quantity) "
                        +" FROM locationbatchdocumentmapping dm "
                        +" INNER JOIN newproductbatch pb ON pb.id = dm.batchmapid"
                        +" WHERE dm.documentid = ? AND dm.batchmapid IS NOT NULL AND pb.warehouse IS NOT NULL AND pb.location IS NOT NULL "
                        +" GROUP BY pb.id ";
                    List<Object[]> batchMappinglist = executeSQLQuery(query, new Object[]{srdId});
                    for (Object[] batchMapping : batchMappinglist) {
                        String batchId = batchMapping[0] != null ? (String) batchMapping[0] : null;
                        String batchName = batchMapping[1] != null ? (String) batchMapping[1] : "";
                        String warehouseId = batchMapping[2] != null ? (String) batchMapping[2] : null;
                        String locationId = batchMapping[3] != null ? (String) batchMapping[3] : null;
                        StoreMaster row =  null;
                        if(product.isIsrowforproduct()){
                            String rowId = batchMapping[4] != null ? (String) batchMapping[4] : null;
                            row = storeService.getStoreMaster(rowId);
                        }
                        StoreMaster rack = null;
                        if(product.isIsrackforproduct()){
                            String rackId = batchMapping[5] != null ? (String) batchMapping[5] : null;
                            rack = storeService.getStoreMaster(rackId);
                        }
                        StoreMaster bin = null;
                        if(product.isIsbinforproduct()){
                            String rbinId = batchMapping[6] != null ? (String) batchMapping[6] : null;
                            bin = storeService.getStoreMaster(rbinId);
                        }
                        double qty = batchMapping[7] != null ? (Double) batchMapping[7] : 0;

                        Store store = storeMap.get(warehouseId);

                        Location location = locationMap.get(locationId);

                        String serialNames = serialMapping != null ? serialMapping.get(batchId) : null;
                        if (qty != 0) {
                            String key = product.getID() + store.getId();
                            if (smMap.containsKey(key)) {
                                StockMovement sm = smMap.get(key);
                                StockMovementDetail smd = new StockMovementDetail(sm, location, row, rack, bin, batchName, serialNames, qty);
                                sm.setQuantity(sm.getQuantity() + smd.getQuantity());
                                sm.getStockMovementDetails().add(smd);
                            } else {
                                StockMovement sm = new StockMovement(product, store, qty, baseUomRate, transactionNo, orderDate, TransactionType.IN, TransactionModule.ERP_SALES_RETURN,srId, srdId);
                                sm.setStockUoM(product.getUnitOfMeasure());
                                sm.setCostCenter(costCenter);
                                sm.setCustomer(customer);
                                sm.setCreatedOn(new Date(createdDate));
                                sm.setRemark("Stock added through Sales Return");
                                StockMovementDetail smd = new StockMovementDetail(sm, location, row, rack, bin, batchName, serialNames, qty);
                                sm.getStockMovementDetails().add(smd);

                                smMap.put(key, sm);
                            }
                        }
                    }
                    saveAll(smMap.values());
                }
                txnManager.commit(status);
            } catch (ServiceException ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "insertSMForSRTransactions for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } catch (Exception ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "insertSMForSRTransactions for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            }
        } while (hasMore);
    }

    @Override
    public void insertSMForPBTransactions(Company company, Map<String, Store> storeMap, Map<String, Location> locationMap) {
        boolean hasMore;
        Paging paging = new Paging(0, TRANSACTION_LIMIT);
        do {
            hasMore = false;
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("STR_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            try {

                String query = "SELECT pb.ID, pb.product, pb.productcost, pb.refno, pb.entryDate, pb.createdon "
                        + " FROM ProductBuild pb "
                        + " WHERE pb.company = ? AND pb.product.producttype.ID IN(?,?) AND pb.product.iswarehouseforproduct = ? AND pb.product.islocationforproduct = ?";
                List params = new ArrayList();
                params.add(company);
                params.add(Producttype.INVENTORY_PART);
                params.add(Producttype.ASSEMBLY);
                params.add(true);
                params.add(true);
                List<Object[]> pbList = executeQuery( query, params.toArray());
                if (pbList.size() == paging.getLimit()) {
                    hasMore = true;
                    paging.setOffset(paging.getOffset() + paging.getLimit());
                }
                for (Object[] pb : pbList) {

                    String pbId = pb[0] != null ? (String) pb[0] : null;
                    Product product = pb[1] != null ? (Product) pb[1] : null;
                    double baseUomRate = pb[2] != null ? (Double) pb[2] : null;
                    String transactionNo = pb[3] != null ? (String) pb[3] : null;
                    Date orderDate = pb[4] != null ? (Date) pb[4] : null;
                    Date createdDate =  null;
                    try{
                        createdDate = pb[5] != null ? (Date) pb[5] : null;
                    }catch(java.lang.ClassCastException cce){
                        if(pb[5] instanceof java.lang.Long){
                            createdDate=new Date((java.lang.Long)pb[5]);
                        }
                    }


                    Map<String, StockMovement> smMap = new HashMap<>();
                    Map<String, String> serialMapping = null;
                    if (product.isIsSerialForProduct()) {
                        serialMapping = getMappedSerialNames(pbId);
                    }

                    query = "SELECT pb.id, pb.batchname, pb.warehouse, pb.location, pb.`row`, pb.rack, pb.bin, SUM(dm.quantity) "
                        +" FROM locationbatchdocumentmapping dm "
                        +" INNER JOIN newproductbatch pb ON pb.id = dm.batchmapid"
                        +" WHERE dm.documentid = ? AND dm.batchmapid IS NOT NULL AND pb.warehouse IS NOT NULL AND pb.location IS NOT NULL "
                        +" GROUP BY pb.id ";
                    List<Object[]> batchMappinglist = executeSQLQuery(query, new Object[]{pbId});
                    for (Object[] batchMapping : batchMappinglist) {
                        String batchId = batchMapping[0] != null ? (String) batchMapping[0] : null;
                        String batchName = batchMapping[1] != null ? (String) batchMapping[1] : "";
                        String warehouseId = batchMapping[2] != null ? (String) batchMapping[2] : null;
                        String locationId = batchMapping[3] != null ? (String) batchMapping[3] : null;
                        StoreMaster row =  null;
                        if(product.isIsrowforproduct()){
                            String rowId = batchMapping[4] != null ? (String) batchMapping[4] : null;
                            row = storeService.getStoreMaster(rowId);
                        }
                        StoreMaster rack = null;
                        if(product.isIsrackforproduct()){
                            String rackId = batchMapping[5] != null ? (String) batchMapping[5] : null;
                            rack = storeService.getStoreMaster(rackId);
                        }
                        StoreMaster bin = null;
                        if(product.isIsbinforproduct()){
                            String rbinId = batchMapping[6] != null ? (String) batchMapping[6] : null;
                            bin = storeService.getStoreMaster(rbinId);
                        }
                        double qty = batchMapping[7] != null ? (Double) batchMapping[7] : 0;

                        Store store = storeMap.get(warehouseId);

                        Location location = locationMap.get(locationId);

                        String serialNames = serialMapping != null ? serialMapping.get(batchId) : null;
                        if (qty != 0) {
                            String key = product.getID() + store.getId();
                            if (smMap.containsKey(key)) {
                                StockMovement sm = smMap.get(key);
                                StockMovementDetail smd = new StockMovementDetail(sm, location, row, rack, bin, batchName, serialNames, qty);
                                sm.setQuantity(sm.getQuantity() + smd.getQuantity());
                                sm.getStockMovementDetails().add(smd);
                            } else {
                                StockMovement sm = new StockMovement(product, store, qty, baseUomRate, transactionNo, orderDate, TransactionType.IN, TransactionModule.PRODUCT_BUILD_ASSEMBLY, pbId,pbId);
                                sm.setStockUoM(product.getUnitOfMeasure());
                                sm.setCreatedOn(createdDate);
                                sm.setRemark("Stock added  through Product Build.");
                                StockMovementDetail smd = new StockMovementDetail(sm, location, row, rack, bin, batchName, serialNames, qty);
                                sm.getStockMovementDetails().add(smd);

                                smMap.put(key, sm);
                            }
                        }
                    }
                    saveAll(smMap.values());
                }
                txnManager.commit(status);
            } catch (ServiceException ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "insertSMForPBTransactions for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } catch (Exception ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "insertSMForPBTransactions for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            }
        } while (hasMore);
    }

    @Override
    public void insertSMForPBDTransactions(Company company, Map<String, Store> storeMap, Map<String, Location> locationMap) {
        boolean hasMore;
        Paging paging = new Paging(0, TRANSACTION_LIMIT);
        do {
            hasMore = false;
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("STR_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            try {

                String query = "SELECT pbd.ID, pbd.aproduct, pbd.rate, pbd.build.refno, pbd.build.entryDate, pbd.build.createdon, pbd.build.product , pbd.build.ID "
                        + " FROM ProductBuildDetails pbd "
                        + " WHERE pbd.build.company = ? AND pbd.aproduct.producttype.ID IN(?,?) AND pbd.aproduct.iswarehouseforproduct = ? AND pbd.aproduct.islocationforproduct = ? "
                        + " AND pbd.build IS NOT NULL";
                List params = new ArrayList();
                params.add(company);
                params.add(Producttype.INVENTORY_PART);
                params.add(Producttype.ASSEMBLY);
                params.add(true);
                params.add(true);
                List<Object[]> pbdList = executeQuery( query, params.toArray());
                if (pbdList.size() == paging.getLimit()) {
                    hasMore = true;
                    paging.setOffset(paging.getOffset() + paging.getLimit());
                }
                for (Object[] pbd : pbdList) {

                    String pbdId = pbd[0] != null ? (String) pbd[0] : null;
                    Product aproduct = pbd[1] != null ? (Product) pbd[1] : null;
                    double baseUomRate = pbd[2] != null ? (Double) pbd[2] : null;
                    String transactionNo = pbd[3] != null ? (String) pbd[3] : null;
                    Date orderDate = pbd[4] != null ? (Date) pbd[4] : null;
                    Date createdDate = pbd[5] != null ? (Date) pbd[5] : null;
                    Product product = pbd[6] != null ? (Product) pbd[6] : null;
                    String pbId = pbd[7] != null ? (String) pbd[7] : null;

                    Map<String, StockMovement> smMap = new HashMap<String, StockMovement>();
                    Map<String, String> serialMapping = null;
                    if (product.isIsSerialForProduct()) {
                        serialMapping = getMappedSerialNames(pbdId);
                    }

                    query = "SELECT pb.id, pb.batchname, pb.warehouse, pb.location, pb.`row`, pb.rack, pb.bin, SUM(dm.quantity) "
                        +" FROM locationbatchdocumentmapping dm "
                        +" INNER JOIN newproductbatch pb ON pb.id = dm.batchmapid"
                        +" WHERE dm.documentid = ? AND dm.batchmapid IS NOT NULL AND pb.warehouse IS NOT NULL AND pb.location IS NOT NULL "
                        +" GROUP BY pb.id ";
                    List<Object[]> batchMappinglist = executeSQLQuery(query, new Object[]{pbdId});
                    for (Object[] batchMapping : batchMappinglist) {
                        String batchId = batchMapping[0] != null ? (String) batchMapping[0] : null;
                        String batchName = batchMapping[1] != null ? (String) batchMapping[1] : "";
                        String warehouseId = batchMapping[2] != null ? (String) batchMapping[2] : null;
                        String locationId = batchMapping[3] != null ? (String) batchMapping[3] : null;
                        StoreMaster row =  null;
                        if(product.isIsrowforproduct()){
                            String rowId = batchMapping[4] != null ? (String) batchMapping[4] : null;
                            row = storeService.getStoreMaster(rowId);
                        }
                        StoreMaster rack = null;
                        if(product.isIsrackforproduct()){
                            String rackId = batchMapping[5] != null ? (String) batchMapping[5] : null;
                            rack = storeService.getStoreMaster(rackId);
                        }
                        StoreMaster bin = null;
                        if(product.isIsbinforproduct()){
                            String rbinId = batchMapping[6] != null ? (String) batchMapping[6] : null;
                            bin = storeService.getStoreMaster(rbinId);
                        }
                        double qty = batchMapping[7] != null ? (Double) batchMapping[7] : 0;

                        Store store = storeMap.get(warehouseId);

                        Location location = locationMap.get(locationId);

                        String serialNames = serialMapping != null ? serialMapping.get(batchId) : null;
                        if (qty != 0) {
                            String key = product.getID() + store.getId();
                            if (smMap.containsKey(key)) {
                                StockMovement sm = smMap.get(key);
                                StockMovementDetail smd = new StockMovementDetail(sm, location, row, rack, bin, batchName, serialNames, qty);
                                sm.setQuantity(sm.getQuantity() + smd.getQuantity());
                                sm.getStockMovementDetails().add(smd);
                            } else {
                                StockMovement sm = new StockMovement(aproduct, store, qty, baseUomRate, transactionNo, orderDate, TransactionType.OUT, TransactionModule.PRODUCT_BUILD_ASSEMBLY, pbId,pbdId);
                                sm.setStockUoM(aproduct.getUnitOfMeasure());
                                sm.setCreatedOn(createdDate);
                                sm.setAssembledProduct(product);
                                sm.setRemark("Stock out through  Product Build.");
                                StockMovementDetail smd = new StockMovementDetail(sm, location, row, rack, bin, batchName, serialNames, qty);
                                sm.getStockMovementDetails().add(smd);

                                smMap.put(key, sm);
                            }
                        }
                    }
                    saveAll(smMap.values());
                }
                txnManager.commit(status);
            } catch (ServiceException ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "insertSMForPBDTransactions for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } catch (Exception ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "insertSMForPBDTransactions for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } finally {
                System.gc();
            }
        } while (hasMore);
    }

    private void updateWarehouseLocationForNewProductBatch(Product product, InventoryWarehouse warehouse, InventoryLocation location) throws ServiceException, DataAccessException {
        List<NewProductBatch> productBatchList = getProductBatchList(product);
        for (NewProductBatch productBatch : productBatchList) {
            if (!product.isIswarehouseforproduct()) {
                productBatch.setWarehouse(warehouse);
            }
            if (!product.isIslocationforproduct()) {
                productBatch.setLocation(location);
            }
        }
        saveAll(productBatchList);
    }

    private List<NewProductBatch> getProductBatchList(Product product) throws ServiceException {
        String hql = "FROM NewProductBatch WHERE product = ?";
        return executeQuery( hql, product.getID());
    }

    private Map<String, String> getMappedSerialNames(String documentId) throws ServiceException {
        String query = "SELECT sdm.serialid.batch.id, sdm.serialid.serialname FROM SerialDocumentMapping sdm WHERE sdm.documentid = ? AND sdm.serialid IS NOT NULL";
        List params = new ArrayList();
        params.add(documentId);
        List<Object[]> list = executeQuery( query, params.toArray());
        Map<String, String> serialMap = new HashMap();
        for (Object[] batchSerial : list) {
            String batchId = batchSerial[0] != null ? (String) batchSerial[0] : null;
            String serialName = batchSerial[1] != null ? (String) batchSerial[1] : null;
            if (serialMap.containsKey(batchId)) {
                String serials = serialMap.get(batchId);
                serialMap.put(batchId, serials + "," + serialName);
            } else {
                serialMap.put(batchId, serialName);
            }
        }
        return serialMap;
    }

    private NewProductBatch createNewBatchEntry(Product product, InventoryWarehouse warehouse, InventoryLocation location, double quantity, double quntityDue, int transactionType, boolean isOpening, boolean isPurchase) {

        String id = UUID.randomUUID().toString();
        NewProductBatch productBatch = new NewProductBatch();
        productBatch.setId(id);
        productBatch.setBatchname("");
        productBatch.setProduct(product.getID());
        productBatch.setCompany(product.getCompany());
        productBatch.setWarehouse(warehouse);
        productBatch.setLocation(location);
        productBatch.setTransactiontype(transactionType);
        productBatch.setIsopening(isOpening);
        productBatch.setIspurchase(isPurchase);
        productBatch.setQuantity(quantity);
        productBatch.setQuantitydue(quntityDue);

        return productBatch;
    }

    private LocationBatchDocumentMapping createBatchDocumentEntry(NewProductBatch productBatch, String documentId, double quantity, int transactionType) {

        String id = UUID.randomUUID().toString();
        LocationBatchDocumentMapping documentMapping = new LocationBatchDocumentMapping();
        documentMapping.setId(id);
        documentMapping.setBatchmapid(productBatch);
        documentMapping.setDocumentid(documentId);
        documentMapping.setTransactiontype(transactionType);
        documentMapping.setQuantity(quantity);

        return documentMapping;

    }

    private void linkSerialsWithCreatedBatch(Product product, NewProductBatch productBatch) throws ServiceException {
        if (product.isIsSerialForProduct() && productBatch != null) {
            String hql = "UPDATE NewBatchSerial SET batch = ? WHERE product = ? AND batch IS NULL";
            executeUpdate( hql, new Object[]{productBatch, product.getID()});
        }
    }

    @Override
    public InventoryWarehouse getERPWarehouse(Store store) throws ServiceException {
        if (store == null) {
            return null;
        }
        return storeService.getERPWarehouse(store.getId());
    }

    @Override
    public Store getDefaultStore(Company company) throws ServiceException {
        if (company == null) {
            return null;
        }
        return storeService.getDefaultStore(company);
    }

    @Override
    public InventoryLocation getERPLocation(Location location) throws ServiceException {
        if (location == null) {
            return null;
        }
        return locationService.getERPLocation(location.getId());
    }

    @Override
    public void sendActivationMail(User user) throws ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);

        try {
            String query = "SELECT emailID FROM User WHERE company = ? AND deleteflag = 0 AND emailID IS NOT NULL";
            List list = executeQuery( query, user.getCompany());
            String[] recipients = (String[]) list.toArray(new String[list.size()]);
            Company company = (Company) get(Company.class, user.getCompany().getCompanyID());

            query = "SELECT subDomain FROM Company WHERE companyID = ?";

            String subdomain = "";
            List listComp = executeQuery( query, new Object[]{user.getCompany().getCompanyID()});
            if (!list.isEmpty()) {
                subdomain = (String) listComp.get(0);
            }
            String url = URLUtil.getDomainURL(subdomain, true);

            String subject = "Inventory Activation Notification";
            String htmlMessage = "<p> Hi,<p>"
                    + "<p>Inventory module is activated successfully. Please refresh your dashboard.</p>"
                    + "<p>Please do the necessary settings before using the system."
                    + "<li>Set Sequence Format for all Inventory Operations <b>(Inventory -> Configuration -> Sequence Management)</b></li>"
                    + "<li>Add new Stores and Locations or use previous <b>(Masters -> Inventory Masters -> Store master / Location Master)</b></li>"
                    + "<li>Assign Store Manager and Store Executives to the stores (by store update operation)</li></p>"
                    + "<br><br><p>This is an auto-generated email from " + url + ". Please do not reply.</p>";
            String plainMessage = "Hi,"
                    + "Inventory module is activated successfully. Please refresh your dashboard.\n"
                    + "Please do the necessary settings before using the system.\n"
                    + "Set Sequence Format for all Inventory Operations (Inventory -> Configuration -> Sequence Management)\n"
                    + "Add new Stores and Locations or use previous (Masters -> Inventory Masters -> Store master / Location Master)\n"
                    + "Assign Store Manager and Store Executives to the stores (by store update operation)\n"
                    + "This is an auto-generated email from " + url + ". Please do not reply.\n";
            Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
             txnManager.commit(status);
            SendMailHandler.postMail(recipients, subject, htmlMessage, plainMessage, user.getEmailID(), smtpConfigMap);

           
        } catch (MessagingException ex) {
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.gc();
        }
    }

    @Override
    public Map<String, Store> getAllStores(Company company) throws ServiceException {
        List<Store> storeList = storeService.getStores(company, null, null);
        Map data = new HashMap();
        for (Store store : storeList) {
            data.put(store.getId(), store);
        }
        return data;
    }

    @Override
    public Map<String, Location> getAllLocations(Company company) throws ServiceException {
        List<Location> locationList = locationService.getLocations(company, null, null);
        Map data = new HashMap();
        for (Location location : locationList) {
            data.put(location.getId(), location);
        }
        return data;
    }
    @Override
    public Map<String, StoreMaster> getAllStoreMaster(Company company) throws ServiceException{
        String query = "FROM StoreMaster WHERE company = ?" ;
        List<StoreMaster> list = executeQuery( query, company);
        Map data = new HashMap();
        for (StoreMaster sm : list) {
            data.put(sm.getId(), sm);
        }
        return data;
    }

    @Override
    public List<Product> getSelectedProducts(List<String> productids) throws ServiceException {
        String query = "FROM Product WHERE id IN ( :ids ) ";
        List list = findByNamedParam(query, "ids", productids);
        return list;
    }

    @Override
    public void addAuditlog(Map<String, Object> auditParams, String msg) throws ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            auditTrailObj.insertAuditLog(AuditAction.INVENTORY_CONFIG, msg, auditParams, "");
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.gc();
        }
    }

    @Override
    public void removeRunningStatus(Company company) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String query = "DELETE FROM inventory_thread_running_status WHERE company = ?";
            executeSQLUpdate( query, new Object[]{company.getCompanyID()});
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addRunningStatus(Company company) throws ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String query = "INSERT INTO inventory_thread_running_status VALUES (?)";
            executeSQLUpdate( query, new Object[]{company.getCompanyID()});
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean isActivateDeactivateProcessRunning(Company company) throws ServiceException {
        boolean running = false;
        String query = "SELECT 1 FROM inventory_thread_running_status WHERE company = ?";
        List list = executeSQLQuery( query, new Object[]{company.getCompanyID()});
        if (!list.isEmpty()) {
            running = true;
        }
        if (!running) {
            addRunningStatus(company);
        }
        return running;
    }

    @Override
    public void completeInTransitSRRequests(User user, Store qaStore, Store repairStore) throws ServiceException {
        completeSRRequests(user);
        approveAllPendingSRRequests(user, qaStore, repairStore);
    }

    @Override
    public void completeInTransitISTRequests(User user, Store qaStore, Store repairStore) throws ServiceException {
        completeISTRequests(user);
        approveAllISTPendingRequests(user, qaStore, repairStore);
    }

    @Override
    public void completeINTransitSARequests(User user, Store qaStore, Store repairStore) throws ServiceException {
        approveAllSAPendingRequests(user, qaStore, repairStore);
    }

    @Override
    public void sendDeactivationMail(User user) throws ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);

        try {
            String query = "SELECT emailID FROM User WHERE company = ? AND deleteflag = 0 AND emailID IS NOT NULL";
            List list = executeQuery( query, user.getCompany());
            String[] recipients = (String[]) list.toArray(new String[list.size()]);
            Company company = (Company) get(Company.class, user.getCompany().getCompanyID());

            query = "SELECT subDomain FROM Company WHERE companyID = ?";

            String subdomain = "";
            List listComp = executeQuery( query, new Object[]{user.getCompany().getCompanyID()});
            if (!list.isEmpty()) {
                subdomain = (String) listComp.get(0);
            }
            String url = URLUtil.getDomainURL(subdomain, true);

            String subject = "Inventory Deactivation Notification";
            String htmlMessage = "<p> Hi,<p>"
                    + "<p>Inventory module is deactivated successfully.Please refresh your dashboard.</p>"
                    + "<br><br><p>This is an auto-generated email from " + url + ". Please do not reply.</p>";
            String plainMessage = "Hi,"
                    + "Inventory module is deactivated successfully.Please refresh your dashboard.\n"
                    + "This is an auto-generated email from " + url + ". Please do not reply.\n";
            Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
            txnManager.commit(status);
            SendMailHandler.postMail(recipients, subject, htmlMessage, plainMessage, user.getEmailID(), smtpConfigMap);

//            txnManager.commit(status);
        } catch (MessagingException ex) {
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.gc();
        }
    }

    private void completeSRRequests(User user) {
        Paging paging = new Paging(0, TRANSACTION_LIMIT);
        boolean hasMore;
        do {
            hasMore = false;
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("STR_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            try {
                String query = "FROM StockRequest WHERE company = ? AND status IN (?, ?, ?)";
                List params = new ArrayList();
                params.add(user.getCompany());
                params.add(RequestStatus.ORDERED);
                params.add(RequestStatus.ISSUED);
                params.add(RequestStatus.RETURN_REQUEST);
                List<StockRequest> list = executeQueryPaging( query, params.toArray(), paging);
                if (list.size() == paging.getLimit()) {
                    hasMore = true;
                    paging.setOffset(paging.getOffset() + paging.getLimit());
                }
                for (StockRequest sr : list) {
                    srService.cancelStockRequest(user, sr);
                }
                txnManager.commit(status);
            } catch (ServiceException ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "completeSRRequests for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } catch (Exception ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "completeSRRequests for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } finally {
                System.gc();
            }
        } while (hasMore);
    }

    private void completeISTRequests(User user) {
        Paging paging = new Paging(0, TRANSACTION_LIMIT);
        boolean hasMore;
        do {
            hasMore = false;
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("STR_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            try {
                String query = "FROM InterStoreTransferRequest WHERE company = ? AND status IN (?, ?)";
                List params = new ArrayList();
                params.add(user.getCompany());
                params.add(InterStoreTransferStatus.INTRANSIT);
                params.add(InterStoreTransferStatus.RETURNED);
                List<InterStoreTransferRequest> list = executeQueryPaging( query, params.toArray(), paging);
                if (list.size() == paging.getLimit()) {
                    hasMore = true;
                    paging.setOffset(paging.getOffset() + paging.getLimit());
                }
                for (InterStoreTransferRequest ist : list) {
                    istService.cancelISTRequest(user, ist);
                }
                txnManager.commit(status);
            } catch (ServiceException ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "completeISTRequests for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } catch (Exception ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "completeISTRequests for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            }
        } while (hasMore);
    }

    private void approveAllPendingSRRequests(User user, Store qaStore, Store repairStore) throws ServiceException {
        Paging paging = new Paging(0, TRANSACTION_LIMIT);
        boolean hasMore;
        do {
            hasMore = false;
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("STR_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            try {
                String query = "SELECT sta.id FROM in_stocktransfer_approval sta "
                        + " INNER JOIN in_stocktransfer_detail_approval stad ON stad.stocktransfer_approval = sta.id "
                        + " INNER JOIN in_goodsrequest gr ON sta.stocktransferid = gr.id AND gr.company = ? "
                        + " WHERE (stad.approval_status = ? OR stad.repair_status IN (?,?))";
                List params = new ArrayList();
                params.add(user.getCompany().getCompanyID());
                params.add(ApprovalStatus.PENDING.ordinal());
                params.add(ApprovalStatus.REPAIRPENDING.ordinal());
                params.add(ApprovalStatus.RETURNTOREPAIR.ordinal());

                List list = executeSQLQueryPaging( query, params.toArray(), paging);
                if (list.size() == paging.getLimit()) {
                    hasMore = true;
                    paging.setOffset(paging.getOffset() + paging.getLimit());
                }
                for (Object obj : list) {
                    String approvalId = (String) obj;
                    params.clear();
                    query = "FROM StockTransferDetailApproval WHERE stockTransferApproval.id = ? AND (approvalStatus IN(?) OR repairStatus IN (?,?))  ";
                    params.add(approvalId);
                    params.add(ApprovalStatus.PENDING);
                    params.add(ApprovalStatus.REPAIRPENDING);
                    params.add(ApprovalStatus.RETURNTOREPAIR);
                    List<StockTransferDetailApproval> stdaList = executeQuery( query, params.toArray());
                    List<StockTransferDetailApproval> approvedRecordsQA = new ArrayList();
                    List<StockTransferDetailApproval> approvedRecordsRp = new ArrayList();
                    for (StockTransferDetailApproval stda : stdaList) {
                        stda.setRemark("Deactivate Inventory Module");
                        if (stda.getApprovalStatus() == ApprovalStatus.PENDING) {
                            stApprovalService.approveStockTransferDetail(user, stda, null, false, stda.getQuantity());
                            approvedRecordsQA.add(stda);
                        } else {
                            stApprovalService.approveStockTransferDetail(user, stda, null, true, stda.getQuantity());
                            approvedRecordsRp.add(stda);
                        }

                    }
                    stApprovalService.createStockMovementForQAApproval(user.getCompany(), approvedRecordsQA, qaStore, repairStore);
                    stApprovalService.createStockMovementForRepairing(user.getCompany(), approvedRecordsRp, repairStore);
                }
                txnManager.commit(status);
            } catch (ServiceException ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "approveAllPendingSRRequests for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } catch (Exception ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "approveAllPendingSRRequests for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            }
        } while (hasMore);
    }

    private void approveAllISTPendingRequests(User user, Store qaStore, Store repairStore) throws ServiceException {
        Paging paging = new Paging(0, TRANSACTION_LIMIT);
        boolean hasMore;
        do {
            hasMore = false;
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("STR_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            try {
                String query = "SELECT sta.id FROM in_stocktransfer_approval sta "
                        + " INNER JOIN in_stocktransfer_detail_approval stad ON stad.stocktransfer_approval = sta.id "
                        + " INNER JOIN in_interstoretransfer gr ON sta.stocktransferid = gr.id AND gr.company = ? "
                        + " WHERE (stad.approval_status = ? OR stad.repair_status IN (?,?))";
                List params = new ArrayList();
                params.add(user.getCompany().getCompanyID());
                params.add(ApprovalStatus.PENDING.ordinal());
                params.add(ApprovalStatus.REPAIRPENDING.ordinal());
                params.add(ApprovalStatus.RETURNTOREPAIR.ordinal());

                List list = executeSQLQueryPaging( query, params.toArray(), paging);
                if (list.size() == paging.getLimit()) {
                    hasMore = true;
                    paging.setOffset(paging.getOffset() + paging.getLimit());
                }
                for (Object obj : list) {
                    String approvalId = (String) obj;
                    params.clear();
                    query = "FROM StockTransferDetailApproval WHERE stockTransferApproval.id = ? AND (approvalStatus IN(?) OR repairStatus IN (?,?))  ";
                    params.add(approvalId);
                    params.add(ApprovalStatus.PENDING);
                    params.add(ApprovalStatus.REPAIRPENDING);
                    params.add(ApprovalStatus.RETURNTOREPAIR);
                    List<StockTransferDetailApproval> stdaList = executeQuery( query, params.toArray());
                    List<StockTransferDetailApproval> approvedRecordsQA = new ArrayList();
                    List<StockTransferDetailApproval> approvedRecordsRp = new ArrayList();
                    for (StockTransferDetailApproval stda : stdaList) {
                        stda.setRemark("Deactivate Inventory Module");
                        if (stda.getApprovalStatus() == ApprovalStatus.PENDING) {
                            stApprovalService.approveStockTransferDetail(user, stda, null, false, stda.getQuantity());
                            approvedRecordsQA.add(stda);
                        } else {
                            stApprovalService.approveStockTransferDetail(user, stda, null, true, stda.getQuantity());
                            approvedRecordsRp.add(stda);
                        }

                    }
                    stApprovalService.createStockMovementForQAApprovalIntr(user.getCompany(), approvedRecordsQA, qaStore, repairStore);
                    stApprovalService.createStockMovementForRepairingIntr(user.getCompany(), approvedRecordsRp, repairStore);
                }
                Company company = user.getCompany();
                Location repairLocation = null;
                String grQuery = " SELECT istd.id as recordid, grodistmapping.quantitydue as quantity, '' as serialname from grodetails grod "
                        + " INNER JOIN grorder gro ON gro.id=grod.grorder "
                        + " INNER JOIN grodetailistmapping grodistmapping ON grodistmapping.grodetail=grod.id "
                        + " INNER JOIN product p ON grod.product = p.id "
                        + " INNER JOIN in_interstoretransfer inst ON grodistmapping.istrequest = inst.id "
                        + " INNER JOIN in_ist_detail istd ON inst.id=istd.istrequest "
                        + " WHERE grodistmapping.quantitydue > 0 and p.company=? ";
                List grParams = new ArrayList();
                grParams.add(user.getCompany().getCompanyID());
                List grList = executeSQLQueryPaging( grQuery, grParams.toArray(), paging);
                
                
                String operation = "Approve";
                String interstore_loc_No = "";
                SeqFormat seqFormat = null;
                try {
                    seqFormat = seqService.getDefaultSeqFormat(company, ModuleConst.INTER_STORE_TRANSFER);
                    if (seqFormat != null) {
                        interstore_loc_No = seqService.getNextFormatedSeqNumber(seqFormat);
                    } else {
                      //  throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforistnotset", null, RequestContextUtils.getLocale(request)));
                    }
                } catch (SeqFormatException ex) {
                   // throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforistnotset", null, RequestContextUtils.getLocale(request)));
                }
                
                for (Object grObj : grList) {
                    JSONArray jArr = new JSONArray();
                    String recordid = "", serialname = "";
                    double quantity = 0;
                    Object[] a =  (Object[]) grObj;
                    recordid = (String) a[0];
                    serialname=(String) a[2];
                    quantity = (double) a[1];
                    JSONObject obj = new JSONObject();
                    obj.put("recordid", recordid);
                    obj.put("serialname", serialname);
                    obj.put("quantity", quantity);
                    jArr.put(obj);
                    String remark = "Approved";
                    Map<String, Object> requestParams = new HashMap<>();
                    requestParams.put("jsondata", jArr.toString());
                    requestParams.put("user", user);
                    requestParams.put("company", company.getCompanyID());
                    requestParams.put("operation", operation);
                    requestParams.put("remark", remark);
                    requestParams.put("interstore_loc_No", interstore_loc_No);
                    requestParams.put("seqFormat", seqFormat);    
                    stApprovalService.approveRejectGoodsReceipt(requestParams, repairStore, repairLocation);
                }
                              
                txnManager.commit(status);
            } catch (ServiceException ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "approveAllISTPendingRequests for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } catch (Exception ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "approveAllISTPendingRequests for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            }
        } while (hasMore);

    }

    private void approveAllSAPendingRequests(User user, Store qaStore, Store repairStore) throws ServiceException {
        Paging paging = new Paging(0, TRANSACTION_LIMIT);
        boolean hasMore;
        do {
            hasMore = false;
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("STR_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus status = txnManager.getTransaction(def);
            try {
                String query = "SELECT sta.id FROM in_sa_approval sta "
                        + " INNER JOIN in_sa_detail_approval stad ON stad.sa_approval = sta.id "
                        + " INNER JOIN in_stockadjustment gr ON sta.stock_adjustment = gr.id AND gr.company = ? "
                        + " WHERE (stad.approval_status = ? OR stad.repair_status IN (?,?))";
                List params = new ArrayList();
                params.add(user.getCompany().getCompanyID());
                params.add(ApprovalStatus.PENDING.ordinal());
                params.add(ApprovalStatus.REPAIRPENDING.ordinal());
                params.add(ApprovalStatus.RETURNTOREPAIR.ordinal());

                List list = executeSQLQueryPaging( query, params.toArray(), paging);
                if (list.size() == paging.getLimit()) {
                    hasMore = true;
                    paging.setOffset(paging.getOffset() + paging.getLimit());
                }
                for (Object obj : list) {
                    String approvalId = (String) obj;
                    params.clear();
                    query = "FROM SADetailApproval WHERE saApproval.id = ? AND (approvalStatus IN(?) OR repairStatus IN (?,?))  ";
                    params.add(approvalId);
                    params.add(ApprovalStatus.PENDING);
                    params.add(ApprovalStatus.REPAIRPENDING);
                    params.add(ApprovalStatus.RETURNTOREPAIR);
                    List<SADetailApproval> stdaList = executeQuery( query, params.toArray());
                    List<SADetailApproval> approvedRecordsQA = new ArrayList();
                    List<SADetailApproval> approvedRecordsRp = new ArrayList();
                    for (SADetailApproval sada : stdaList) {
                        sada.setRemark("Deactivate Inventory Module");
                        if (sada.getApprovalStatus() == ApprovalStatus.PENDING) {//QA
                            saApprovalService.approveStockAdjustmentDetail(user, sada, null, sada.getQuantity(), false);
                            approvedRecordsQA.add(sada);
                        } else {//repaire
                            saApprovalService.approveStockAdjustmentDetail(user, sada, null, sada.getQuantity(), true);
                            approvedRecordsRp.add(sada);
                        }

                    }
                    saApprovalService.createStockMovementForQAApproval(user.getCompany(), approvedRecordsQA, qaStore, repairStore);
                    saApprovalService.createStockMovementForRepairing(user.getCompany(), approvedRecordsRp, repairStore);
                }
                txnManager.commit(status);
            } catch (ServiceException ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "approveAllSAPendingRequests for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            } catch (Exception ex) {
                txnManager.rollback(status);
                Logger.getLogger(InventoryActivationServiceImpl.class.getName()).log(Level.SEVERE, "approveAllSAPendingRequests for paging- offset " + paging.getOffset() + ", limit " + paging.getLimit(), ex);
            }
        } while (hasMore);
    }

    @Override
    public Store getQAStore(Company company) throws ServiceException {
        String query = "SELECT inspectionStore FROM ExtraCompanyPreferences WHERE id = ?";
        List list = executeQuery( query, company.getCompanyID());
        Store store = null;
        if (!list.isEmpty()) {
            String storeId = (String) list.get(0);
            store = storeService.getStoreById(storeId);
        }
        return store;
    }

    @Override
    public Store getRepairStore(Company company) throws ServiceException {
        String query = "SELECT repairStore FROM ExtraCompanyPreferences WHERE id = ?";
        List list = executeQuery( query, company.getCompanyID());
        Store store = null;
        if (!list.isEmpty()) {
            String storeId = (String) list.get(0);
            store = storeService.getStoreById(storeId);
        }
        return store;
    }
}
