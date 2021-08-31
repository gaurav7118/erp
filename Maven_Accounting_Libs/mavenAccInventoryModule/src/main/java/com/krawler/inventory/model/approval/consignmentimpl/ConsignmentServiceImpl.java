/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval.consignmentimpl;

import com.krawler.common.admin.*;
import com.krawler.inventory.model.approval.consignmentservice.ConsignmentService;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.hql.accounting.SalesOrderDetail;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.approval.InspectionDetail;
import com.krawler.inventory.model.approval.consignment.Consignment;
import com.krawler.inventory.model.approval.consignment.ConsignmentApprovalDetails;
import com.krawler.inventory.model.approval.consignmentservice.ConsignmentDAO;
import com.krawler.inventory.model.approval.sa.SADetailApproval;
import com.krawler.inventory.model.approval.stocktransfer.impl.StockTransferApprovalServiceImpl;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.*;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.inventory.model.stockout.StockAdjustmentDetail;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.springframework.orm.hibernate3.HibernateTransactionManager;

/**
 *
 * @author krawler
 */
public class ConsignmentServiceImpl implements ConsignmentService {

    private ConsignmentDAO consignmentDAO;
    private StockService stockService;
    private StockMovementService stockMovementService;
    private LocationService locationService;
    private AccountingHandlerDAO accountingHandlerDAO;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private HibernateTransactionManager txnManager;
    private auditTrailDAO auditTrailObj;
    private StoreService storeService;

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }

    public void setConsignmentDAO(ConsignmentDAO consignmentDAO) {
        this.consignmentDAO = consignmentDAO;
    }

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    
    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    @Override
    public Consignment getConsingmentById(String consignmentId) {
        Consignment consignment = null;
        if (consignmentId != null) {
            consignment = consignmentDAO.getConsingmentById(consignmentId);
        }
        return consignment;
    }

    @Override
    public List<Consignment> getConsingmentList(String searchString, Paging paging) throws ServiceException {
        return consignmentDAO.getConsingmentList(searchString, paging);
    }
    
    @Override
    public void approveConsignmentDetail(User inspector, ConsignmentApprovalDetails consignment, InspectionDetail inspDTL, Company company,
            double returnqty, boolean fromRepair, Store qaStore, Store repairStore) throws ServiceException {
//        consignmentDAO.saveOrUpdateConsignment(inspDTL);
//        consignment.setInspectionDTL(inspDTL);
        try {
        String remark = "";
        double quantity = consignment.getQuantity() - returnqty;
        boolean done = false;
        double consignQty = 0;
        //To Change Server's new Date() to user's new Date().
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date newUserDate = new Date();//Server's new Date();
        if (inspector != null && !StringUtil.isNullObject(inspector.getTimeZone())) {
            //Fetching user's timezone difference.
            String difference = inspector.getTimeZone().getDifference();
            newUserDate = authHandler.getUserNewDate(difference, null);
        }
        if (quantity == 0) {
            done = true;
            consignQty = consignment.getQuantity();
        } else if (quantity > 0) {
            done = false;
            consignQty = returnqty;
            createQAReturnRequest(consignment, quantity, fromRepair);
        }
        if (quantity >= 0) {
            Set<StockMovementDetail> stockmovementdtl = new HashSet<StockMovementDetail>();
            Location dfltLocation = locationService.getDefaultLocation(company);
            StockMovement sm = new StockMovement();
            sm.setCompany(consignment.getConsignment().getCompany());
//            stockmovment.setCreatedOn(consignment.getConsignment().getFromDate());
            sm.setCustomer(consignment.getConsignment().getCustomer());
            sm.setModuleRefId(consignment.getConsignment().getModuleRefId());
            sm.setModuleRefDetailId(consignment.getConsignment().getModuleRefId());
            sm.setProduct(consignment.getConsignment().getProduct());
            sm.setQuantity(consignQty);
            sm.setRemark("Consignment Approved");
            sm.setStockUoM(consignment.getConsignment().getUom());
            sm.setStore(consignment.getConsignment().getStore());
            sm.setTransactionDate(newUserDate);
            sm.setTransactionModule(TransactionModule.ERP_SALES_RETURN);
            sm.setTransactionNo(consignment.getConsignment().getTransactionNo());
            sm.setTransactionType(TransactionType.IN);
            sm.setPricePerUnit(consignment.getConsignment().getUnitPrice());

            StockMovementDetail smd = new StockMovementDetail();
            smd.setBatchName(consignment.getBatchName());
            smd.setLocation(consignment.getLocation());
            NewProductBatch productBatch = consignment.getBatch();
            if(productBatch != null){
                smd.setRow(productBatch.getRow());
                smd.setRack(productBatch.getRack());
                smd.setBin(productBatch.getBin());
            }
            smd.setQuantity(consignQty);
            smd.setSerialNames(consignment.getSerialName());
            smd.setStockMovement(sm);
            smd.setLocation(consignment.getLocation() != null ? consignment.getLocation() : dfltLocation);
            stockmovementdtl.add(smd);

            sm.setStockMovementDetails(stockmovementdtl);


                if (consignment.getPurchaseSerialId() != null) {
                    NewBatchSerial newBSerial = consignment.getPurchaseSerialId();
                    newBSerial.setQaApprovalstatus(QaApprovalStatus.APPROVED);
                    newBSerial.setQuantitydue(consignment.getQuantity());
                    consignment.setPurchaseSerialId(newBSerial);
                }
                if (!fromRepair) {
                    consignment.setApprovalStatus(ApprovalStatus.APPROVED);
                    consignment.setQuantity(returnqty);
                    consignment.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
    //                stockService.decreaseInventory(consignment.getConsignment().getProduct(), qaStore, qaStore.getDefaultLocation(), consignment.getBatchName(), consignment.getSerialName(), returnqty);
    //                addToMovementForQa(stockmovment, qaStore, TransactionType.OUT, returnqty, "Stock sent after QA Inspection");
    //                remark = "Stock added after  QA Inspection";
                } else {
                    consignment.setRepairStatus(ApprovalStatus.REPAIRDONE);
                    consignment.setRetQty(returnqty);
                    consignment.setRepairedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                    //decrease QA quantity
    //                stockService.decreaseInventory(consignment.getConsignment().getProduct(), repairStore, repairStore.getDefaultLocation(), consignment.getBatchName(), consignment.getSerialName(), returnqty);
    //                addToMovementForQa(stockmovment, repairStore, TransactionType.OUT, returnqty, "Stock sent after Repairing");
    //                remark = "Stock received after  Repairing";
                }

                consignment.setInspector(inspector);
           
                consignmentDAO.saveOrUpdateConsignment(consignment);
                if (done && fromRepair == false) {
                    reflectStockAfterFullApproval(inspector, consignment);
                }

    //        stockService.increaseInventory(consignment.getConsignment().getProduct(), consignment.getConsignment().getStore(), consignment.getLocation(), consignment.getBatchName(), consignment.getSerialName(), consignment.getQuantity());

                //            stockService.updateERPInventoryByid(true, new Date(), consignment.getConsignment().getProduct(), consignment.getConsignment().getProduct().getPackaging(), consignment.getConsignment().getUom(), consignment.getQuantity(), remark, consignment.getConsignment().getDocumentid());
                //            stockMovementService.addStockMovement(stockmovment);

            }
        } catch (ParseException ex) {
            Logger.getLogger(ConsignmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(ConsignmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void createQAReturnRequest(ConsignmentApprovalDetails consignment, double returnqty, boolean fromRepair) throws ServiceException {
        if (consignment != null && returnqty > 0) {
            ConsignmentApprovalDetails cad = new ConsignmentApprovalDetails();
            if (!fromRepair) {
                cad.setApprovalStatus(ApprovalStatus.PENDING);
            } else {
                cad.setRepairStatus(ApprovalStatus.RETURNTOREPAIR);
                cad.setApprovalStatus(ApprovalStatus.REJECTED);
                cad.setRetQty(returnqty);
            }
            cad.setConsignment(consignment.getConsignment());
            cad.setInspector(consignment.getInspector());
            cad.setLocation(consignment.getLocation());
            cad.setBatch(consignment.getBatch());
            cad.setModifiedOn(consignment.getModifiedOn());
            cad.setQuantity(returnqty);
//            consigndtl.setRemark("return request");
            cad.setSerialName(consignment.getSerialName());
            cad.setBatchName(consignment.getBatchName());
            cad.setMovementStatus(consignment.isMovementStatus());
            consignmentDAO.saveOrUpdateConsignment(cad);
        }
    }

    @Override
    public void rejectConsignmentDetail(User inspector, ConsignmentApprovalDetails cad, InspectionDetail inspDTL, Company company, double returnqty,
            boolean fromRepair, Store qaStore, Store repairStore) throws ServiceException {
//        consignmentDAO.saveOrUpdateConsignment(inspDTL);
//        consignment.setInspectionDTL(inspDTL);
        try {
        double quantity = cad.getQuantity() - returnqty;
        double consignQty = 0;
        //To Change Server's new Date() to user's new Date().
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date newUserDate = new Date();//Server's new Date();
        if (inspector != null) {
            //Fetching user's timezone difference.
            String difference = inspector.getTimeZone().getDifference();
            newUserDate = authHandler.getUserNewDate(difference, null);
        }
        if (quantity == 0) {
            consignQty = cad.getQuantity();
        } else if (quantity > 0) {
            consignQty = returnqty;
            createQAReturnRequest(cad, quantity, fromRepair);
        }
        if (quantity >= 0) {
            Set<StockMovementDetail> smdSet = new HashSet<StockMovementDetail>();
            Location dfltLocation = locationService.getDefaultLocation(company);
            StockMovement sm = new StockMovement();
            sm.setCompany(cad.getConsignment().getCompany());
            sm.setCustomer(cad.getConsignment().getCustomer());
            sm.setModuleRefId(cad.getConsignment().getModuleRefId());
            sm.setModuleRefDetailId(cad.getConsignment().getModuleRefId());
            sm.setProduct(cad.getConsignment().getProduct());
            sm.setQuantity(consignQty);
            sm.setRemark("Consignment Rejected");
            sm.setStockUoM(cad.getConsignment().getUom());
//            stockmovment.setStore(consignmentAD.getConsignment().getStore());
            sm.setStore(cad.getConsignment().getStore());
            sm.setTransactionDate(newUserDate);
            sm.setTransactionModule(TransactionModule.ERP_SALES_RETURN);
            sm.setTransactionNo(cad.getConsignment().getTransactionNo());
            sm.setTransactionType(TransactionType.OUT);
            sm.setPricePerUnit(cad.getConsignment().getUnitPrice());

            StockMovementDetail smd = new StockMovementDetail();
            smd.setBatchName(cad.getBatchName());
            smd.setLocation(cad.getLocation());
            smd.setQuantity(consignQty);
            smd.setSerialNames(cad.getSerialName());
            smd.setStockMovement(sm);
            smd.setLocation(cad.getLocation() != null ? cad.getLocation() : dfltLocation);
            NewProductBatch productBatch = cad.getBatch();
            if(productBatch !=null){
                smd.setRow(productBatch.getRow());
                smd.setRack(productBatch.getRack());
                smd.setBin(productBatch.getBin());
            }
            smdSet.add(smd);

            sm.setStockMovementDetails(smdSet);

//            if (consignmentAD.getPurchaseSerialId() != null) {
//                NewBatchSerial newBSerial = consignmentAD.getPurchaseSerialId();
//                newBSerial.setQaApprovalstatus(QaApprovalStatus.REJECTED);
//                newBSerial.setQuantitydue(consignmentAD.getQuantity());
//
//                NewProductBatch btch = newBSerial.getBatch();
//                btch.setQuantitydue(btch.getQuantitydue() - 1);
//                newBSerial.setBatch(btch);
//
//                consignmentAD.setPurchaseSerialId(newBSerial);
//            }
            if (!fromRepair) {
                cad.setApprovalStatus(ApprovalStatus.REJECTED);
                cad.setQuantity(returnqty);
                cad.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                // decrease QA qty and Increase Repaire store
                if (qaStore != null) {
//                    stockService.decreaseInventory(consignmentAD.getConsignment().getProduct(), qaStore, qaStore.getDefaultLocation(), consignmentAD.getBatchName(), consignmentAD.getSerialName(), returnqty);
//                    addToMovementForQa(stockmovment, qaStore, TransactionType.OUT, returnqty, "Stock sent for repairing");
//                    
//                    stockService.increaseInventory(consignmentAD.getConsignment().getProduct(), repairStore, repairStore.getDefaultLocation(), consignmentAD.getBatchName(), consignmentAD.getSerialName(), returnqty);
//                    addToMovementForQa(stockmovment, repairStore, TransactionType.IN, returnqty, "Stock added after QA rejection");
                }
                } else {
                cad.setRepairStatus(ApprovalStatus.REPAIRREJECT);
                cad.setRetQty(returnqty);
                cad.setRepairedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                //decrease Repaire qty and do Stockout
//                stockService.decreaseInventory(consignmentAD.getConsignment().getProduct(), repairStore, repairStore.getDefaultLocation(), consignmentAD.getBatchName(), consignmentAD.getSerialName(), returnqty);
//                addToMovementForQa(stockmovment, repairStore, TransactionType.OUT, returnqty, "Stock could not be repaired");
            }

            cad.setInspector(inspector);
         
            consignmentDAO.saveOrUpdateConsignment(cad);
            if (quantity == 0 && fromRepair == false) {
                reflectStockAfterFullApproval(inspector, cad);
            }
//        stockService.increaseInventory(consignmentAD.getConsignment().getProduct(), consignmentAD.getConsignment().getStore(), consignmentAD.getLocation(), consignmentAD.getBatchName(), consignmentAD.getSerialName(), consignmentAD.getQuantity());
            if (fromRepair) {
//                stockService.updateERPInventory(true, new Date(), consignmentAD.getConsignment().getProduct(), null, consignmentAD.getConsignment().getUom(), consignmentAD.getQuantity(), "consignment return");
//                stockMovementService.addStockMovement(stockmovment);
//            stockService.updateERPInventoryByid(false, new Date(), consignmentAD.getConsignment().getProduct(), consignmentAD.getConsignment().getProduct().getPackaging(), consignmentAD.getConsignment().getUom(), consignmentAD.getQuantity(), "Qa Rejected", consignmentAD.getConsignment().getDocumentid());
            }

            }
        } catch (ParseException ex) {
            Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(ConsignmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addToMovementForQa(StockMovement stockmovement, Store store, TransactionType type, double qty, String remark) throws ServiceException {
        if (stockmovement != null) {
            StockMovement stkmvt = new StockMovement();
            stkmvt.setCompany(stockmovement.getCompany());
            stkmvt.setCostCenter(stockmovement.getCostCenter());
            stkmvt.setModuleRefId(stockmovement.getModuleRefId());
            stkmvt.setModuleRefDetailId(stockmovement.getModuleRefDetailId());
            stkmvt.setPricePerUnit(stockmovement.getPricePerUnit());
            stkmvt.setProduct(stockmovement.getProduct());
            stkmvt.setQuantity(qty);
            stkmvt.setTransactionType(type);
            stkmvt.setRemark(remark);


            Set<StockMovementDetail> movDtlSet = new HashSet<StockMovementDetail>();

            for (StockMovementDetail smdGiven : stockmovement.getStockMovementDetails()) {
                StockMovementDetail smd = new StockMovementDetail();
                smd.setBatchName(smdGiven.getBatchName());
                smd.setLocation(store.getDefaultLocation());
                smd.setRow(smdGiven.getRow());
                smd.setRack(smdGiven.getRack());
                smd.setBin(smdGiven.getBin());
                smd.setQuantity(qty);
                smd.setSerialNames(smdGiven.getSerialNames());
                smd.setStockMovement(stkmvt);
                movDtlSet.add(smd);
            }
            stkmvt.setStockMovementDetails(movDtlSet);
            stkmvt.setStockUoM(stockmovement.getStockUoM());
            stkmvt.setStore(store);
            stkmvt.setTransactionDate(stockmovement.getTransactionDate());
            stkmvt.setTransactionModule(stockmovement.getTransactionModule());
            stkmvt.setTransactionNo(stockmovement.getTransactionNo());
            consignmentDAO.saveOrUpdateConsignment(stkmvt);

        }
    }

    @Override
    public Map rejectedApprovedItemsDetail(ConsignmentApprovalDetails consignment) throws ServiceException {
        Map map = null;
        if (consignment != null) {
            map = new HashMap();

            String emailIds = "";
            String mailSeparator = ",";
            boolean isfirst = true;
            Store store = consignment.getConsignment().getStore();
            if (store != null) {
                for (User user : store.getStoreManagerSet()) {
                    if (isfirst) {
                        emailIds += user.getEmailID();
                        isfirst = false;
                    } else {
                        emailIds += mailSeparator + user.getEmailID();
                    }

                }
            }
            Product product = consignment.getConsignment().getProduct();
            
            KwlReturnObject extracompanyprefObjresult = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), product.getCompany().getCompanyID());
            ExtraCompanyPreferences extracompanyobj = (ExtraCompanyPreferences) extracompanyprefObjresult.getEntityList().get(0);
            Store repairStore = storeService.getStoreById(extracompanyobj.getRepairStore());
            String repairStoreManagerListStr="";
            if(repairStore != null){
                Set<User> mngr=repairStore.getStoreManagerSet();
                Set<User> exctv=repairStore.getStoreExecutiveSet();
                List<User> allUsr=(List<User>) CollectionUtils.intersection(mngr, exctv);
                Iterator itr1=mngr.iterator();
                while(itr1.hasNext()){
                    User usr=(User) itr1.next();
                    if(usr != null && !StringUtil.isNullOrEmpty(usr.getEmailID())){
                        if(StringUtil.isNullOrEmpty(repairStoreManagerListStr)){
                            repairStoreManagerListStr = usr.getEmailID();
                        }else{
                            repairStoreManagerListStr +=  "," + usr.getEmailID();
                        }
                    }
                }
                
            }
            
            map.put("batchName", consignment.getBatchName());
            map.put("serialNames", consignment.getSerialName());
            map.put("quantity", consignment.getQuantity());
            map.put("productId", (product != null ? product.getProductid() : ""));
            map.put("productName", (product != null ? product.getName() : ""));
            map.put("storeName", (store != null ? store.getFullName() : ""));
            map.put("storeManagerEmailIds", emailIds);
            map.put("repairStoreManagerEmailIds", repairStoreManagerListStr);
            
        }
        return map;
    }

    private void reflectStockAfterFullApproval(User inspector, ConsignmentApprovalDetails consignment) throws ServiceException {
        if (consignment != null) {
            boolean done = true;
            Consignment consign = consignmentDAO.getConsingmentById(consignment.getConsignment().getId());
            Set<ConsignmentApprovalDetails> lst = consign.getConsignmentApprovalDetails();
            for (ConsignmentApprovalDetails consindtl : lst) {
                if (consindtl.getApprovalStatus() == ApprovalStatus.PENDING) {
                    done = false;
                    break;
                }
            }
            if (done) {
                consign.setApprovalStatus(ApprovalStatus.DONE);;
                consignmentDAO.saveOrUpdateConsignment(consign);
            }
                }

    }

    @Override
    public void addOrUpdateConsignment(Company company, String moduleRefId, List<Consignment> consignment) throws ServiceException {
        if (consignment == null || consignment.isEmpty()) {
            throw new InventoryException(InventoryException.Type.NULL, "Stock Movement entry list is null or empty");
        }
//        removeStockMovementByReferenceId(company, moduleRefId);
        for (Consignment consignmentObj : consignment) {
            try {
                consignmentObj.setFromDate(authHandler.getConstantDateFormatter().parse(authHandler.getConstantDateFormatter().format(new Date())));
                consignmentObj.setToDate(authHandler.getConstantDateFormatter().parse(authHandler.getConstantDateFormatter().format(new Date())));
                consignmentObj.setApprovalStatus(ApprovalStatus.PENDING);
                consignmentObj.setModuleRefId(moduleRefId);
                consignmentDAO.saveOrUpdateConsignment(consignmentObj);
            } catch (SessionExpiredException ex) {
                Logger.getLogger(ConsignmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(ConsignmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @Override
    public void deletePreviousConsignmentQAForSR(Company company, String salesReturnId) throws ServiceException{
        consignmentDAO.deletePreviousConsignmentQAForSR(company,salesReturnId);
    }
    
    @Override
    public ConsignmentApprovalDetails getConsingmentDetailsById(String consignmentId) {
        return consignmentDAO.getConsingmentDetailsById(consignmentId);
    }

    @Override
    public int isQAApprovePermissionForUser(Company company, String storeid, String locationid, String userId) throws ServiceException {
        return consignmentDAO.isQAApprovePermissionForUser(company, storeid, locationid, userId);
    }

    public KwlReturnObject getAllQAList(String companyId, Date fromDate, Date toDate, String moduleType, String statusType, Set<Store> storeSet, String searchString, Paging paging,String tzDiff,boolean isQAapprovalForBuildAssembly,boolean  isisJobWorkOrderInQA) throws ServiceException {
        return consignmentDAO.getAllQAList(companyId, fromDate, toDate, moduleType, statusType, storeSet, searchString, paging,tzDiff,isQAapprovalForBuildAssembly,isisJobWorkOrderInQA);
    }

    public KwlReturnObject getAllQARepairPendingList(String companyId, Date fromDate, Date toDate, String moduleType, String statusType, Set<Store> storeSet, String searchString, Paging paging,String tzDiff,boolean isQAapprovalForBuildAssembly) throws ServiceException {
        return consignmentDAO.getAllQARepairPendingList(companyId, fromDate, toDate, moduleType, statusType, storeSet, searchString, paging,tzDiff,isQAapprovalForBuildAssembly);
    }

    @Override
    public KwlReturnObject getAllRepairList(String companyId, String statusType, Store store, Date fromDate, Date toDate, String searchString, Paging paging,String tzDiff,boolean  isQAapprovalForBuildAssembly) throws ServiceException {
        return consignmentDAO.getAllRepairList(companyId, statusType, store, fromDate, toDate, searchString, paging,tzDiff,isQAapprovalForBuildAssembly);
    }

    @Override
    public KwlReturnObject assignStockToPendingConsignmentRequests(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject requestJobj = new JSONObject();
        try{
            requestJobj = StringUtil.convertRequestToJsonObject(request);
        }catch(JSONException e){
            throw ServiceException.FAILURE("ConsignmentServiceImpl.assignStockToPendingConsignmentRequests", e);
        }
        return assignStockToPendingConsignmentRequests(requestJobj);
    }
    
    @Override
    public KwlReturnObject assignStockToPendingConsignmentRequests(JSONObject request) throws ServiceException, SessionExpiredException {
        String companyId = request.optString(Constants.companyid);
        KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
        Company company = (Company) jeresult.getEntityList().get(0);
        String userid = request.optString(Constants.userid);
        User user = null;
        if (!StringUtil.isNullOrEmpty(userid)) {
            jeresult = accountingHandlerDAO.getObject(User.class.getName(), userid);
            user = (User) jeresult.getEntityList().get(0);
        }
        return assignStockToPendingConsignmentRequests(request, company, user);
    }
    
    @Override
    public KwlReturnObject assignStockToPendingConsignmentRequests(HttpServletRequest request, Company company, User user) throws ServiceException, SessionExpiredException {
        JSONObject requestJobj = new JSONObject();
        try {
            requestJobj = StringUtil.convertRequestToJsonObject(request);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("ConsignmentServiceImpl.assignStockToPendingConsignmentRequests", e);
        }
        return assignStockToPendingConsignmentRequests(requestJobj,company,user);
    }
    
    public KwlReturnObject assignStockToPendingConsignmentRequests(JSONObject request, Company company, User user) throws ServiceException {

        if (company == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Company cannot be null or empty.");
        }

        KwlReturnObject retObj = new KwlReturnObject(false, null, null, null, 0);

        boolean activateCRblockingWithoutStock = false;
        KwlReturnObject extracap = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), company.getCompanyID());
        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
        activateCRblockingWithoutStock = extraCompanyPreferences.isActivateCRblockingWithoutStock();

        // if CRBlockingWithoutStock feature is activated then procceed further
        if (activateCRblockingWithoutStock) {

            // get Pending consignment requests 
            KwlReturnObject pendingReqList = accCommonTablesDAO.getPendingConsignmentRequests(company);

            if (pendingReqList != null && pendingReqList.isSuccessFlag() && pendingReqList.getRecordTotalCount() > 0) {

                List<SalesOrder> consReqList = new ArrayList<SalesOrder>();
                List listSerial = pendingReqList.getEntityList();
                Iterator itrSerial = listSerial.iterator();
                while (itrSerial.hasNext()) {
//                        Object[] obj = (Object[]) itrSerial.next();
                    Object obj = (Object) itrSerial.next();
                    if (obj != null) {
                        String salesOrderId = (String) obj.toString();
                        if (!StringUtil.isNullOrEmpty(salesOrderId)) {
                            KwlReturnObject solist = accountingHandlerDAO.getObject(SalesOrder.class.getName(), salesOrderId);
                            SalesOrder salesOrder = (SalesOrder) solist.getEntityList().get(0);
                            consReqList.add(salesOrder);
                        }
                    }
                }

                /*
                 * this set is used to check whether serial is locked already or
                 * not.this has to be used bcoz somewhere in code sql query is
                 * used and somewhere hql is used (so hibernatetemplates session
                 * will be different) so changes made in Objects will not get
                 * reflected due to different hibernate session.So for this ,
                 * map is used to save locked serial until commit operation is
                 * performed.
                 */
                Set usedProductBatchSerialSet = new HashSet();
                Set usedProductBatchSet = new HashSet();

                // Sales Order for loop
                for (int i = 0; i < consReqList.size(); i++) {

                    SalesOrder so = consReqList.get(i);
                    Set<SalesOrderDetail> rows = so.getRows();
                    boolean autoapproveflag = false;
                    autoapproveflag = so.isAutoapproveflag();
                    MasterItem requestType = so.getMovementType();
                    String requestTypeId = null, requestWarehouse = "", requestLocation = "";;
                    if (requestType != null) {
                        requestTypeId = requestType.getID();
                    }
                    if (autoapproveflag) {
                        if (so.getRequestWarehouse() != null) {
                            requestWarehouse = so.getRequestWarehouse().getId();
                        }
                        if (so.getRequestLocation() != null) {
                            requestLocation = so.getRequestLocation().getId();
                        }
                    }
                    //Sales Order Detail for loop
                    for (SalesOrderDetail soDetail : rows) {

                        try {
                            Product product = soDetail.getProduct();
                            HashMap<Integer, Object[]> BatchdetalisMap = new HashMap<Integer, Object[]>();
                            KwlReturnObject kmsg = null;
                            String companyid = company.getCompanyID();

                            int batchcnt = 0;
                            int cnt = 0;
                            boolean isquantityNotavl = false;  //this flag is used to check whether serial batch quantity is avilabale 

                            //get products batch serial list that is available (ie. non-locked)

                            //location and warehouse should be enable for product while assigning the stock
                            if (product.isIslocationforproduct() && product.isIswarehouseforproduct()) {
                                kmsg = accCommonTablesDAO.getBatchSerialDetailsforProduct(company, product.getID(), product.isIsSerialForProduct(), requestTypeId, false, requestWarehouse, requestLocation);
                                List batchList = kmsg.getEntityList();
                                Iterator bitr = batchList.iterator();
                                while (bitr.hasNext()) {
                                    Object[] ObjBatchrow = (Object[]) bitr.next();
                                    BatchdetalisMap.put(cnt++, ObjBatchrow);
                                }

                                String sodetailsid = soDetail.getID();

                                double lockquantitydue = 0.0, ActbatchQty = 0.0, approvedquantity = 0.0, batchQty = 0.0;
                                if (autoapproveflag) {
                                    approvedquantity = soDetail.getApprovedQuantity();
                                    if (!StringUtil.isNullOrEmpty(sodetailsid) && product.isIsSerialForProduct()) {
                                        ActbatchQty = accCommonTablesDAO.getserialAssignedQty(sodetailsid);
                                    } else {
                                        ActbatchQty = accCommonTablesDAO.getbatchAssignedQty(sodetailsid);
                                    }
                                    lockquantitydue = approvedquantity - ActbatchQty;
                                } else {
                                    lockquantitydue = soDetail.getLockquantitydue();
                                }
                                int cntp = (int) lockquantitydue;
                                batchQty = cntp;
                                if (product.isIsSerialForProduct()) { // for serial no case we will save the serial details and as location and warehouse are madnatory so it will genrate batchses also

                                    for (int j = 0; j < cntp; j++) {

                                        for (int serialCnt = 0; serialCnt < cnt; serialCnt++) {
                                            Object[] objArr = BatchdetalisMap.get(serialCnt);

                                            if (objArr != null) {

                                                String serialId = objArr[0] != null ? (String) objArr[0] : "";
                                                String batchId = objArr[1] != null ? (String) objArr[1] : "";
                                                String warehouse = objArr[10] != null ? (String) objArr[10] : "";
                                                String location = objArr[11] != null ? (String) objArr[11] : "";
                                                String reusableCount = objArr[13] != null ? String.valueOf(objArr[13]) : "0";

                                                Date mfgDateObj = null;
                                                Date expDateObj = null;

                                                String checkInSet = product.getID() + batchId + serialId;

                                                if (!usedProductBatchSerialSet.contains(checkInSet)) {

                                                    if (objArr[3] != null) { //ie mfgdate is not null
                                                        mfgDateObj = (java.util.Date) objArr[3];
                                                    }
                                                    if (objArr[4] != null) { //ie expdate is not null
                                                        expDateObj = (java.util.Date) objArr[3];
                                                    }
                                                    if (!StringUtil.isNullOrEmpty(sodetailsid) && !StringUtil.isNullOrEmpty(batchId) && !StringUtil.isNullOrEmpty(serialId)) {
                                                        HashMap<String, Object> documentMap = new HashMap<String, Object>();
                                                        documentMap.put("quantity", "1");
                                                        documentMap.put("documentid", sodetailsid);
                                                        documentMap.put("transactiontype", "20");//This is SO Type Tranction   sales order moduleid
                                                        if (mfgDateObj != null) {
                                                            documentMap.put("mfgdate", mfgDateObj);
                                                        }
                                                        if (expDateObj != null) {
                                                            documentMap.put("expdate", expDateObj);
                                                        }
                                                        documentMap.put("batchmapid", batchId);
                                                        accCommonTablesDAO.saveBatchDocumentMapping(documentMap);

                                                        HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                                                        batchUpdateQtyMap.put("id", batchId);
                                                        batchUpdateQtyMap.put("lockquantity", "1");
                                                        accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);

                                                        HashMap<String, Object> serialdocumentMap = new HashMap<String, Object>();
                                                        serialdocumentMap.put("quantity", "1");
                                                        serialdocumentMap.put("documentid", sodetailsid);
                                                        if (mfgDateObj != null) {
                                                            serialdocumentMap.put("mfgdate", mfgDateObj);
                                                        }
                                                        if (expDateObj != null) {
                                                            serialdocumentMap.put("expdate", expDateObj);
                                                        }
                                                        serialdocumentMap.put("serialmapid", serialId);
                                                        serialdocumentMap.put("transactiontype", "20");//This is so Type Tranction  

                                                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                                        requestParams.put("companyid", companyid);
                                                        if (!StringUtil.isNullOrEmpty(user.getUserID())) {
                                                            requestParams.put("requestorid", user.getUserID());
                                                        }
                                                        if (!StringUtil.isNullOrEmpty(warehouse)) {
                                                            requestParams.put("warehouse", warehouse);
                                                        }
                                                        if (!StringUtil.isNullOrEmpty(location)) {
                                                            requestParams.put("location", location);
                                                        }
                                                        //code to Apply Pending Approval Rule
                                                        if (!autoapproveflag) {
                                                            KwlReturnObject ruleResult = accMasterItemsDAOobj.CheckRuleForPendingApproval(requestParams);
                                                            Iterator itr = ruleResult.getEntityList().iterator();
                                                            Set<User> approverSet = null;
                                                            boolean isRequestPending = false;
                                                            while (itr.hasNext()) {
                                                                ConsignmentRequestApprovalRule approvalRule = (ConsignmentRequestApprovalRule) itr.next();
                                                                if (approvalRule != null) {
                                                                    KwlReturnObject res = accSalesOrderDAOobj.getConsignmentRequestApproverList(approvalRule.getID());
                                                                    List<User> userlist = res.getEntityList();
                                                                    Set<User> users = new HashSet<User>();;
                                                                    for (User us : userlist) {
                                                                        users.add(us);
                                                                    }
                                                                    approverSet = users;
                                                                    isRequestPending = true;
                                                                    break;
                                                                }
                                                            }
                                                            if (isRequestPending) {
                                                                serialdocumentMap.put("requestpendingapproval", RequestApprovalStatus.PENDING);
                                                                serialdocumentMap.put("approver", approverSet);
                                                            }
                                                        } else {
                                                            Set<User> approverSet = null;
                                                            Set<User> users = new HashSet<User>();
                                                            if (soDetail != null) {
                                                                approverSet = soDetail.getApproverSet();
                                                            }
                                                            Iterator iterator = approverSet.iterator();
                                                            while (iterator.hasNext()) {
                                                                users.add((User) iterator.next());
                                                            }
                                                            serialdocumentMap.put("requestpendingapproval", RequestApprovalStatus.APPROVED);
                                                            serialdocumentMap.put("approver", users);
                                                            serialdocumentMap.put("reusablecount", reusableCount);
                                                        }

                                                        accCommonTablesDAO.saveSerialDocumentMapping(serialdocumentMap);

                                                        HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
                                                        serialUpdateQtyMap.put("lockquantity", "1");
                                                        serialUpdateQtyMap.put("id", serialId);
                                                        accCommonTablesDAO.saveSerialAmountDue(serialUpdateQtyMap);

                                                        String setName = product.getID() + batchId + serialId;
                                                        usedProductBatchSerialSet.add(setName);

                                                        batchcnt += 1;
                                                        break;
                                                    }
                                                }

                                            } else {
                                                isquantityNotavl = true;  //if quantity is not available then break and come out of for loop
                                                break;
                                            }
                                        }
                                    }
                                } else {// for without serial case assign data to those
                                    while (batchQty != 0) {

                                        for (int batchCount = 0; batchCount < cnt; batchCount++) {
                                            Object[] objArr = BatchdetalisMap.get(batchCount);

                                            if (objArr != null && batchQty != 0) {
//                                                    String serialId = objArr[0] != null ? (String) objArr[0] : "";
                                                String batchId = objArr[1] != null ? (String) objArr[1] : "";
                                                String warehouse = objArr[10] != null ? (String) objArr[10] : "";
                                                String location = objArr[11] != null ? (String) objArr[11] : "";

                                                Date mfgDateObj = null;
                                                Date expDateObj = null;
                                                double batchavlqty = 0;
                                                if (!StringUtil.isNullOrEmpty(batchId)) {
                                                    KwlReturnObject pbdresult = accountingHandlerDAO.getObject(NewProductBatch.class.getName(), batchId);
                                                    NewProductBatch pbdetail = (NewProductBatch) pbdresult.getEntityList().get(0);
                                                    batchavlqty = pbdetail.getQuantitydue() - pbdetail.getLockquantity();
                                                }
                                                String checkInSet = product.getID() + batchId;

                                                if (!usedProductBatchSet.contains(checkInSet)) {

                                                    if (objArr[3] != null) { //ie mfgdate is not null
                                                        mfgDateObj = (java.util.Date) objArr[3];
                                                    }
                                                    if (objArr[4] != null) { //ie expdate is not null
                                                        expDateObj = (java.util.Date) objArr[3];
                                                    }
                                                    HashMap<String, Object> documentMap = new HashMap<String, Object>();
                                                    if (!StringUtil.isNullOrEmpty(sodetailsid) && !StringUtil.isNullOrEmpty(batchId)) {
                                                        HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                                                        if (batchavlqty > 0) {
                                                            if (batchQty > batchavlqty) {
                                                                batchUpdateQtyMap.put("lockquantity", String.valueOf(batchavlqty));
                                                                documentMap.put("quantity", String.valueOf(batchavlqty));
                                                                documentMap.put("approvedqty", String.valueOf(batchavlqty));

                                                                batchQty = batchQty - batchavlqty;
                                                            } else {
                                                                batchUpdateQtyMap.put("lockquantity", String.valueOf((batchQty)));
                                                                documentMap.put("quantity", String.valueOf(batchQty));
                                                                documentMap.put("approvedqty", String.valueOf(batchQty));

                                                                batchQty = batchQty - batchQty;
                                                            }
                                                            batchUpdateQtyMap.put("id", batchId);
                                                            accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
                                                        }


                                                        documentMap.put("documentid", sodetailsid);
                                                        documentMap.put("transactiontype", "20");//This is SO Type Tranction   sales order moduleid
                                                        if (mfgDateObj != null) {
                                                            documentMap.put("mfgdate", mfgDateObj);
                                                        }
                                                        if (expDateObj != null) {
                                                            documentMap.put("expdate", expDateObj);
                                                        }
                                                        documentMap.put("batchmapid", batchId);

                                                        accCommonTablesDAO.saveBatchDocumentMapping(documentMap);

                                                    }

                                                    String setName = product.getID() + batchId;
                                                    usedProductBatchSerialSet.add(setName);
                                                }

                                            } else {
                                                isquantityNotavl = true;  //if quantity is not available then break and come out of for loop
                                                break;
                                            }

                                        }
                                        if (isquantityNotavl) {
                                            break;
                                        }
                                        batchQty = 0;
                                    }
                                }
                                if (!autoapproveflag) {
                                    accCommonTablesDAO.updateSOLockQuantitydue(sodetailsid, batchcnt, companyid);
                                }
                                if (isquantityNotavl) {
                                    break;
                                }
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(ConsignmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }

                }

            }

        }
        return retObj;
    }

    @Override
    public void creatStockmovementForConsignmentQAApproval(Company company, List<ConsignmentApprovalDetails> approrejectList, Store qaStore, Store repairStore) throws ServiceException {
	try {
            Date currDate = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));        
		if (qaStore == null && repairStore == null) {
            qaStore = stockService.getQaStore(company);
            repairStore = stockService.getRepairStore(company);
        }
        if (qaStore == null || repairStore == null) {
            throw new InventoryException("QA Store OR Repairing store are not set");
        }
        if (approrejectList != null && !approrejectList.isEmpty()) {
            Map<String, StockMovement> stockMovementQAMap = new HashMap(); // for qa store
            Map<String, StockMovement> stockMovementConsignMap = new HashMap(); // for Consignment Store
            Map<String, StockMovement> stockMovementRepMap = new HashMap(); // for Repairing Store
            for (ConsignmentApprovalDetails approvalDtl : approrejectList) {
                if (approvalDtl.getInspector() != null && !StringUtil.isNullObject(approvalDtl.getInspector().getTimeZone())) {
                    //Fetching user's timezone difference.
                    String difference = approvalDtl.getInspector().getTimeZone().getDifference();
                    currDate = authHandler.getUserNewDate(difference, null);
                }
                NewProductBatch productBatch = approvalDtl.getBatch();
                StoreMaster row = productBatch != null ? productBatch.getRow() : null;
                StoreMaster rack = productBatch != null ? productBatch.getRack() : null;
                StoreMaster bin = productBatch != null ? productBatch.getBin() : null;
                double quantity = approvalDtl.getRetQty() == 0 ? approvalDtl.getQuantity() : approvalDtl.getRetQty();
                ApprovalStatus approvalStatus = approvalDtl.getApprovalStatus();
                Consignment consignment = approvalDtl.getConsignment();
                String memo=consignmentDAO.getSalesReturnMemo(company, consignment.getModuleRefId());
                StockMovement qasm;
                if (stockMovementQAMap.containsKey(consignment.getId())) {
                    qasm = stockMovementQAMap.get(consignment.getId());
                } else {
                    qasm = new StockMovement(consignment.getProduct(), qaStore, 0, consignment.getUnitPrice(), consignment.getTransactionNo(), currDate, TransactionType.OUT, TransactionModule.ERP_SALES_RETURN, consignment.getModuleRefId(), consignment.getModuleRefId());
                    qasm.setRemark(approvalStatus == ApprovalStatus.APPROVED ? "Stock Approved by QA" : "Stock Rejected by QA");
                    qasm.setStockUoM(consignment.getUom());
                    qasm.setCustomer(consignment.getCustomer());
                    qasm.setCostCenter(consignment.getCostcenter());

                    stockMovementQAMap.put(consignment.getId(), qasm);

                }
                StockMovementDetail qasmd = new StockMovementDetail(qasm, qaStore.getDefaultLocation(), row, rack, bin, approvalDtl.getBatchName(), approvalDtl.getSerialName(), quantity);
                qasm.getStockMovementDetails().add(qasmd);
                qasm.setQuantity(qasm.getQuantity() + quantity);
                qasm.setMemo(memo);
                Location location = null;
                StockMovement insm = null;
                if (approvalStatus == ApprovalStatus.APPROVED) {
                    Store store = consignment.getStore();
                    location = approvalDtl.getLocation();
                    if (stockMovementConsignMap.containsKey(consignment.getId())) {
                        insm = stockMovementConsignMap.get(consignment.getId());
                    } else {
                        insm = new StockMovement(consignment.getProduct(), store, 0, consignment.getUnitPrice(), consignment.getTransactionNo(), currDate, TransactionType.IN, TransactionModule.ERP_SALES_RETURN, consignment.getModuleRefId(), consignment.getModuleRefId());
                        insm.setRemark(approvalStatus == ApprovalStatus.APPROVED ? "Stock Approved by QA" : "Stock Rejected by QA");
                        insm.setStockUoM(consignment.getUom());
                        insm.setCustomer(consignment.getCustomer());
                        insm.setCostCenter(consignment.getCostcenter());

                        stockMovementConsignMap.put(consignment.getId(), insm);
                    }
                } else if (approvalStatus == ApprovalStatus.REJECTED) {
                    Store store = repairStore;
                    location = repairStore.getDefaultLocation();
                    if (stockMovementRepMap.containsKey(consignment.getId())) {
                        insm = stockMovementRepMap.get(consignment.getId());
                    } else {
                        insm = new StockMovement(consignment.getProduct(), store, 0, consignment.getUnitPrice(), consignment.getTransactionNo(), currDate, TransactionType.IN, TransactionModule.ERP_SALES_RETURN, consignment.getModuleRefId(), consignment.getModuleRefId());
                        insm.setRemark(approvalStatus == ApprovalStatus.APPROVED ? "Stock Approved by QA" : "Stock Rejected by QA");
                        insm.setStockUoM(consignment.getUom());
                        insm.setCustomer(consignment.getCustomer());
                        insm.setCostCenter(consignment.getCostcenter());
                        stockMovementRepMap.put(consignment.getId(), insm);
                    }
                }
                if (insm != null && location != null) {
                    StockMovementDetail insmd = new StockMovementDetail(insm, location, row, rack, bin, approvalDtl.getBatchName(), approvalDtl.getSerialName(), quantity);
                    insm.getStockMovementDetails().add(insmd);
                    insm.setQuantity(insm.getQuantity() + quantity);
                    insm.setMemo(memo);
                }

            }
            List<StockMovement> smList = new ArrayList<StockMovement>(); // Stock movements without consignment because consignment needs stock movement in ERP
            smList.addAll(stockMovementQAMap.values());
            smList.addAll(stockMovementRepMap.values());
            for (StockMovement sm : smList) {
                if (!sm.getStockMovementDetails().isEmpty()) {
                    for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                        if (sm.getTransactionType() == TransactionType.IN) {
//                            stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
//                            stockService.updateERPInventory(true, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                             stockMovementService.stockMovementInERP(true, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(),true);
                        } else {
//                            stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
//                            stockService.updateERPInventory(false, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                             stockMovementService.stockMovementInERP(false, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(),true);
                        }
                    }
                    stockMovementService.addStockMovement(sm);
                }
            }
            for (StockMovement sm : stockMovementConsignMap.values()) {
                if (!sm.getStockMovementDetails().isEmpty()) {
                    for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                        if (sm.getTransactionType() == TransactionType.IN) {
//                            stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                            stockService.updateERPInventory(true, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(true, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(),true);

                        } else {
//                            stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                            stockService.updateERPInventory(false, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(false, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(),true);
                        }
                    }
                    stockMovementService.addStockMovement(sm);
                }
            }
        }
        } catch (ParseException ex) {
            Logger.getLogger(ConsignmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(ConsignmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void createStockMovementForRepairing(Company company, List<ConsignmentApprovalDetails> approrejectList, Store repairStore) throws ServiceException {
        try{
        Date currDate = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));    
        if (repairStore == null) {
            repairStore = stockService.getRepairStore(company);
        }
        if (repairStore == null) {
            throw new InventoryException("Repairing store is not set");
        }
        if (approrejectList != null && !approrejectList.isEmpty()) {
            Map<String, StockMovement> stockMovementConsignmentMap = new HashMap(); // for Consignment Store
            Map<String, StockMovement> stockMovementRepMap = new HashMap(); // for Repairing Store
            for (ConsignmentApprovalDetails conDtl : approrejectList) {
                if (conDtl.getInspector() != null) {
                    //Fetching user's timezone difference.
                    String difference = conDtl.getInspector().getTimeZone().getDifference();
                    currDate = authHandler.getUserNewDate(difference, null);
                }
                NewProductBatch productBatch = conDtl.getBatch();
                StoreMaster row = productBatch != null ? productBatch.getRow() : null;
                StoreMaster rack = productBatch != null ? productBatch.getRack() : null;
                StoreMaster bin = productBatch != null ? productBatch.getBin() : null;
                double quantity = conDtl.getRetQty() == 0 ? conDtl.getQuantity() : conDtl.getRetQty();
                ApprovalStatus repairingStatus = conDtl.getRepairStatus();
                Consignment consignment = conDtl.getConsignment();
                String memo = consignmentDAO.getSalesReturnMemo(company, consignment.getModuleRefId());
                StockMovement repsm;
                if (stockMovementRepMap.containsKey(consignment.getId())) {
                    repsm = stockMovementRepMap.get(consignment.getId());
                } else {
                    repsm = new StockMovement(consignment.getProduct(), repairStore, 0, consignment.getUnitPrice(), consignment.getTransactionNo(), currDate, TransactionType.OUT, TransactionModule.ERP_SALES_RETURN, consignment.getModuleRefId(), consignment.getModuleRefId());
                    repsm.setRemark(repairingStatus == ApprovalStatus.REPAIRDONE ? "Stock Repaired" : "Stock cannot be Repaired");
                    repsm.setStockUoM(consignment.getUom());
                    repsm.setCostCenter(consignment.getCostcenter());
                    repsm.setCustomer(consignment.getCustomer());
                    stockMovementRepMap.put(consignment.getId(), repsm);

                }
                StockMovementDetail qasmd = new StockMovementDetail(repsm, repairStore.getDefaultLocation(), row, rack, bin, conDtl.getBatchName(), conDtl.getSerialName(), quantity);
                repsm.getStockMovementDetails().add(qasmd);
                repsm.setQuantity(repsm.getQuantity() + quantity);
                repsm.setMemo(memo);
                Location location = null;
                StockMovement insm = null;
                if (repairingStatus == ApprovalStatus.REPAIRDONE) {
                    location = conDtl.getLocation();
                    if (stockMovementConsignmentMap.containsKey(consignment.getId())) {
                        insm = stockMovementConsignmentMap.get(consignment.getId());
                    } else {
                        insm = new StockMovement(consignment.getProduct(), consignment.getStore(), 0, consignment.getUnitPrice(), consignment.getTransactionNo(), currDate, TransactionType.IN, TransactionModule.ERP_SALES_RETURN, consignment.getModuleRefId(), consignment.getModuleRefId());
                        insm.setRemark(repairingStatus == ApprovalStatus.REPAIRDONE ? "Stock Repaired" : "Stock cannot be Repaired");
                        insm.setStockUoM(consignment.getUom());
                        insm.setCostCenter(consignment.getCostcenter());
                        insm.setCustomer(consignment.getCustomer());
                        stockMovementConsignmentMap.put(consignment.getId(), insm);
                    }
                }
                if (insm != null && location != null) {
                    StockMovementDetail insmd = new StockMovementDetail(insm, location, row, rack, bin, conDtl.getBatchName(), conDtl.getSerialName(), quantity);
                    insm.getStockMovementDetails().add(insmd);
                    insm.setQuantity(insm.getQuantity() + quantity);
                    insm.setMemo(memo);
                }
            }

            for (StockMovement sm : stockMovementRepMap.values()) {
                if (!sm.getStockMovementDetails().isEmpty()) {
                    for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                        if (sm.getTransactionType() == TransactionType.IN) {
//                            stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
//                            stockService.updateERPInventory(true, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                        } else {
//                            stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
//                            stockService.updateERPInventory(false, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(false, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(),true);
                        }
                    }
                    stockMovementService.addStockMovement(sm);
                }
            }
            for (StockMovement sm : stockMovementConsignmentMap.values()) {
                if (!sm.getStockMovementDetails().isEmpty()) {
                    for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                        if (sm.getTransactionType() == TransactionType.IN) {
//                            stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                            stockService.updateERPInventory(true, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(true, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(),true);
                        } else {
//                            stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                            stockService.updateERPInventory(false, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(false, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(),true);
                        }
                    }
                    stockMovementService.addStockMovement(sm);
                }
            }
        }
        }catch (ParseException ex) {
            Logger.getLogger(ConsignmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(ConsignmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
    }
    }

    @Override
    public boolean isPendingForApproval(String consignmentReturnId) throws ServiceException {
        if(StringUtil.isNullOrEmpty(consignmentReturnId)){
            throw new IllegalArgumentException("Consignment Return Id is required");
        }
        return consignmentDAO.isPendingForApproval(consignmentReturnId);
    }
    
    @Override
    public String getSalesPersonEmailIdBySRDetailId(String srDetailid,String companyId) throws ServiceException{
        return consignmentDAO.getSalesPersonEmailIdBySRDetailId(srDetailid, companyId);
    }

    @Override
    public KwlReturnObject getBuildAssemblyProductQaDetails(Map<String,String>requestMap) throws ServiceException {
       List list=new ArrayList();
       KwlReturnObject kwl=null;
        try {
            
             kwl= consignmentDAO.getBuildProductsAssemblyQaDetails(requestMap);
           
            
        } catch (Exception e) {
            throw ServiceException.FAILURE("ConsignmentServiceImpl.getBuildAssemblyProductQaDetails", e);
        }
        return  kwl;
    }
     @Override
    public String getSalesReturnMemo(Company company, String salesReturnId) throws ServiceException {
        return  consignmentDAO.getSalesReturnMemo(company, salesReturnId);
    }
}
