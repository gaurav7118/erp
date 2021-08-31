/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockout.impl;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.exception.NegativeInventoryException;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.approval.sa.SAApprovalService;
import com.krawler.inventory.model.approval.sa.SADetailApproval;
import com.krawler.inventory.model.configuration.InventoryConfig;
import com.krawler.inventory.model.configuration.InventoryConfigService;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.packaging.PackagingService;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.*;
import com.krawler.inventory.model.stockout.*;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.spring.accounting.product.accProductDAO;

/**
 *
 * @author Vipin Gupta
 */
public class StockAdjustmentServiceImpl implements StockAdjustmentService {

    private StockAdjustmentDAO stockAdjustmentDAO;
    private InventoryConfigService invConfigService;
    private StockService stockService;
    private StoreService storeService;
    private LocationService locationService;
    private PackagingService packagingService;
    private StockMovementService stockMovementService;
    private SAApprovalService approvalService;
    private StockMovementDAO stockMovementDAO;
    private com.krawler.spring.common.fieldDataManager fieldDataManagercntrl;
    private accProductDAO accProductDaoObj;

    public void setStockAdjustmentDAO(StockAdjustmentDAO stockAdjustmentDAO) {
        this.stockAdjustmentDAO = stockAdjustmentDAO;
    }

    public void setInvConfigService(InventoryConfigService invConfigService) {
        this.invConfigService = invConfigService;
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

    public void setPackagingService(PackagingService packagingService) {
        this.packagingService = packagingService;
    }

    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    public void setApprovalService(SAApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    public void setStockMovementDAO(StockMovementDAO stockMovementDAO) {
        this.stockMovementDAO = stockMovementDAO;
    }
    
    public void setAccProductDaoObj(accProductDAO accProductDaoObj) {
        this.accProductDaoObj = accProductDaoObj;
    }

    @Override
    public void requestStockAdjustment(User user, StockAdjustment sa) throws ServiceException, NegativeInventoryException {
        HashMap<String,Object> requestParams = new HashMap<>();
        requestStockAdjustment(user, sa, false, false, null,requestParams);
    }

    public void setFieldDataManager(com.krawler.spring.common.fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    @Override
    public void requestStockAdjustment(User user, StockAdjustment sa, boolean allowNegativeInventory, boolean sendForApproval, String customfield,HashMap<String, Object> requestParams) throws ServiceException, NegativeInventoryException {
        if (sa == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Stock Adjustment is null");
        }
        sa.setUom(sa.getProduct().getUnitOfMeasure());
        if (sa.getStore() == null) {
            sa.setStore(storeService.getDefaultStore(sa.getCompany()));
        }

        if (sa.getProduct().getPackaging() != null) {
            Packaging packaging = packagingService.createClonePackaging(sa.getProduct().getPackaging());
            sa.setPackaging(packaging);
        } else {
            Packaging packaging = packagingService.createPackagingByStockUom(sa.getProduct().getUnitOfMeasure());
            sa.setPackaging(packaging);
        }

        InventoryConfig config = invConfigService.getConfigByCompany(sa.getCompany());

        if (sendForApproval && config.isEnableStockoutApprovalFlow() && (sa.getProduct()!=null && sa.getProduct().isQaenable())) {
            if (sa.getAdjustmentType().equalsIgnoreCase("Stock IN")) {
                try {
                    sa.setCreator(user);
                    //Setting UTC new Date getSimpleDateAndTimeFormat
                    sa.setCreatedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                    long creationDate = System.currentTimeMillis();
                    sa.setCreationdate(creationDate);
                    sa.setModifier(user);
                    sa.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                    sa.setStatus(AdjustmentStatus.COMPLETED);
                    stockAdjustmentDAO.saveOrUpdateAdjustment(sa);

                    approvalService.addStockoutApproval(sa,requestParams);

//                setSAFinalQuantityASZero(sa);  // not use if serial selection for approval is implemented
                } catch (ParseException ex) {
                    Logger.getLogger(StockAdjustmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SessionExpiredException ex) {
                    Logger.getLogger(StockAdjustmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                sa.setFinalQuantity(sa.getQuantity());
                for (StockAdjustmentDetail sad : sa.getStockAdjustmentDetail()) {
                    sad.setFinalQuantity(sad.getQuantity());
                    sad.setFinalSerialNames(sad.getSerialNames());
                }
                addStockAdjustmentWithStockMovement(user, sa, allowNegativeInventory, "New Stock Added", "Stockout Done");
            }

        } else {
            sa.setFinalQuantity(sa.getQuantity());
            for (StockAdjustmentDetail sad : sa.getStockAdjustmentDetail()) {
                sad.setFinalQuantity(sad.getQuantity());
                sad.setFinalSerialNames(sad.getSerialNames());
            }
            addStockAdjustmentWithStockMovement(user, sa, allowNegativeInventory, "New Stock Added", "Stockout Done");
        }
        String linelevelcustomdata = "";
        if(requestParams.containsKey(Constants.LineLevelCustomData) && requestParams.get(Constants.LineLevelCustomData) !=null){
            linelevelcustomdata = (String) requestParams.get(Constants.LineLevelCustomData);
        }
        if (!StringUtil.isNullOrEmpty(customfield) || !StringUtil.isNullOrEmpty(linelevelcustomdata)) {
            try {
                KwlReturnObject customDataresult =null;
                JSONArray jcustomarray = null;
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put(Constants.modulename, Constants.Acc_StockAdjustment_modulename);
                customrequestParams.put(Constants.moduleprimarykey, Constants.Acc_StockAdjustmentId);
                customrequestParams.put(Constants.modulerecid, sa.getId());
                customrequestParams.put(Constants.moduleid, Constants.Inventory_Stock_Adjustment_ModuleId);
                customrequestParams.put(Constants.companyid, user.getCompany().getCompanyID());
                customrequestParams.put(Constants.customdataclasspath, Constants.Acc_StockAdjustment_custom_data_classpath);
                /*
                 Save Global level Custom Field Data
                 */
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    jcustomarray = new JSONArray(customfield);
                    customrequestParams.put(Constants.customarray, jcustomarray);
                    customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        StockAdjustmentCustomData cmp = (StockAdjustmentCustomData) customDataresult.getEntityList().get(0);
                        sa.setStockAdjustmentCustomData(cmp);
                    }
                }        

                /*
                 Save line level Custom Field Data
                 */
                customDataresult = null;
                if (!StringUtil.isNullOrEmpty(linelevelcustomdata)) {
                    jcustomarray = new JSONArray(linelevelcustomdata);
                    customrequestParams.put(Constants.customarray, jcustomarray);
                    customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        StockAdjustmentCustomData cmp = (StockAdjustmentCustomData) customDataresult.getEntityList().get(0);
                        sa.setStockAdjustmentLineLevelCustomData(cmp);
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(StockAdjustmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            stockAdjustmentDAO.saveOrUpdateAdjustment(sa);
        }
        stockAdjustmentDAO.setBatchSerialDateDetail(sa.getCompany());
    }

    @Override
    public void addStockAdjustmentWithStockMovement(User user, StockAdjustment sa, boolean allowNegativeInventory, String smInRemark, String smOutRemark) throws ServiceException {
        addStockAdjustment(user, sa, allowNegativeInventory);
        if (sa.getQuantity() < 0) {
            updateSAMovement(sa, TransactionType.OUT, smOutRemark);
        } else if (sa.getQuantity() > 0) {
            updateSAMovement(sa, TransactionType.IN, smInRemark);
        }
    }

    private void addStockAdjustment(User user, StockAdjustment sa, boolean allowNegativeInventory) throws ServiceException, NegativeInventoryException {
        try {
            if (sa == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Stock Adjustment is null");
            }
            if (sa.getStockAdjustmentDetail().isEmpty()) {
                throw new InventoryException(InventoryException.Type.NULL, "Stock Adjustment detail is empty");
            }
            if (sa.getCreator() == null) {
                sa.setCreator(user);
                //Setting UTC new Date
                sa.setCreatedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                long creationDate = System.currentTimeMillis();
                sa.setCreationdate(creationDate);
            }
            sa.setUom(sa.getProduct().getUnitOfMeasure());
            sa.setModifier(user);
            sa.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            sa.setStatus(AdjustmentStatus.COMPLETED);
            stockAdjustmentDAO.saveOrUpdateAdjustment(sa);
        } catch (ParseException ex) {
            Logger.getLogger(StockAdjustmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockAdjustmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void approveStockAdjustment(User user, StockAdjustment sa) throws ServiceException, NegativeInventoryException {
        approveStockAdjustment(user, sa, false);
    }

    @Override
    public void approveStockAdjustment(User user, StockAdjustment sa, boolean allowNegativeInventory) throws ServiceException, NegativeInventoryException {
        addStockAdjustment(user, sa, allowNegativeInventory);
        if (sa.getQuantity() > 0) {
            updateSAMovement(sa, TransactionType.IN, "Approved Stock Added");
        } else if ((sa.getQuantity() < 0)) {
//            updateSAMovement(sa, TransactionType.OUT, "Approved Stockout Request");

            updateSAMovementOnRejection(sa, TransactionType.IN, "Rejected Stockout Request");
        }
    }

//    @Override
//    public void rejectStockAdjustment(User user, StockAdjustment sa) throws ServiceException {
//        if (sa == null) {
//            throw new InventoryException(InventoryException.Type.NULL, "Stock Adjustment is null");
//        }
//        sa.setModifier(user);
//        sa.setModifiedOn(new Date());
//        sa.setStatus(AdjustmentStatus.REJECTED);
//        stockAdjustmentDAO.saveOrUpdateAdjustment(sa);
//        if (sa.getQuantity() < 0) {
//            updateSAMovement(sa, TransactionType.IN, "Rejected Stockout Request");
//        }
//
//    }
    @Override
    public StockAdjustment getStockAdjustmentById(String id) throws ServiceException {
        return stockAdjustmentDAO.getStockAdjustmentById(id);
    }

    @Override
    public List<StockAdjustment> getStockAdjustmentBySequenceNo(Company company, String sequenceNo) throws ServiceException {
        return stockAdjustmentDAO.getStockAdjustmentBySequenceNo(company, sequenceNo);
    }
    
    @Override
    public double getTotalAmountOFSABySequenceNo(Company company,String sequenceNo) throws ServiceException{
        return stockAdjustmentDAO.getTotalAmountOFSABySequenceNo(company, sequenceNo);
    }

    @Override
    public List<StockAdjustment> getStockAdjustmentList(Company company, Set<Store> storeSet, Product product, Set<AdjustmentStatus> status, String adjustmentType, Date fromDate, Date toDate, String searchString, Paging paging, HashMap<String, Object> requestParams) throws ServiceException {
        return stockAdjustmentDAO.getStockAdjustmentList(company, storeSet, product, status, adjustmentType, fromDate, toDate, searchString, paging, requestParams);
    }

    @Override
    public List<StockAdjustment> getStockAdjustmentSummary(Company company, Set<Store> storeSet, Product product, AdjustmentStatus status, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException {
        return stockAdjustmentDAO.getStockAdjustmentSummary(company, storeSet, product, status, fromDate, toDate, searchString, paging);
    }

    @Override
    public void createStockAdjustmentDraft(User user, StockAdjustmentDraft stockAdjustmentDraft) throws ServiceException {
        if (stockAdjustmentDraft == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Stock Adjustment Draft is null");
        }
        StockAdjustmentDraft sad = stockAdjustmentDAO.getDraftByStoreAndBussinessDate(stockAdjustmentDraft.getCompany(), stockAdjustmentDraft.getStore(), stockAdjustmentDraft.getBusinessDate());
        if (sad != null) {
            stockAdjustmentDAO.removeDraft(sad);
        }
        stockAdjustmentDAO.saveOrUpdateDraft(stockAdjustmentDraft);
    }

    @Override
    public void removeDraft(User user, StockAdjustmentDraft stockAdjustmentDraft) throws ServiceException {
        if (stockAdjustmentDraft == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Stock Adjustment Draft is null");
        }
        stockAdjustmentDAO.removeDraft(stockAdjustmentDraft);
    }

    @Override
    public StockAdjustmentDraft getStockAdjustmentDraftById(String id) throws ServiceException {
        return stockAdjustmentDAO.getStockAdjustmentDraftById(id);
    }

    @Override
    public List<StockAdjustmentDraft> getStockAdjustmentDraftList(Company company, Paging paging) throws ServiceException {
        return stockAdjustmentDAO.getStockAdjustmentDraftList(company, paging);
    }

    private void updateSAMovement(StockAdjustment sa, TransactionType transactionType, String remark) throws ServiceException {
        Inventory inventory = null;
        StockMovement sm = new StockMovement(sa.getProduct(), sa.getStore(), sa.getFinalQuantity(), sa.getPricePerUnit(), sa.getTransactionNo(), sa.getBusinessDate(), transactionType, TransactionModule.STOCK_ADJUSTMENT, sa.getId(), sa.getId());
        sm.setStockUoM(sa.getProduct().getUnitOfMeasure());
        sm.setCostCenter(sa.getCostCenter());
         sm.setMemo(sa.getMemo());
        if (!StringUtil.isNullOrEmpty(sa.getRemark())) {
            sm.setRemark(sa.getRemark());
        } else {
            sm.setRemark(remark);
        }
//
//        StockMovement smReject = new StockMovement(sa.getProduct(), sa.getStore(), (sa.getQuantity() - sa.getFinalQuantity()), sa.getPricePerUnit(), sa.getTransactionNo(), sa.getBusinessDate(), transactionType, TransactionModule.STOCK_ADJUSTMENT, sa.getId());
//        smReject.setStockUoM(sa.getProduct().getUnitOfMeasure());
//        smReject.setCostCenter(sa.getCostCenter());
//        smReject.setRemark("Rejected Stock Added");
//        Set<StockMovementDetail> smdRejectedSet = new HashSet<StockMovementDetail>();

        Set<StockMovementDetail> smdSet = new HashSet<StockMovementDetail>();
        double sa_quantity=0.0;
        for (StockAdjustmentDetail sad : sa.getStockAdjustmentDetail()) {

            if (transactionType == TransactionType.OUT) {
                stockService.decreaseInventory(sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), sad.getFinalSerialNames(), sad.getFinalQuantity());
                if(!(sa.getMemo().contains("Stock Adjustment created for Pick Pack"))){
                   inventory = stockService.updateERPInventory(false, sa.getBusinessDate(), sa.getProduct(), sa.getPackaging(), sa.getUom(), sad.getFinalQuantity(), sa.getRemark());
                }else{
                   sa_quantity += sad.getFinalQuantity();
                }                
                stockMovementService.stockMovementInERP(false, sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), sad.getFinalSerialNames(), sad.getFinalQuantity(), false);

                if (sad.getReturnQuantity() != 0) {
                    stockService.increaseInventory(sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), sad.getReturnSerialNames(), sad.getReturnQuantity());
                    inventory = stockService.updateERPInventory(true, sa.getBusinessDate(), sa.getProduct(), sa.getPackaging(), sa.getUom(), sad.getReturnQuantity(), sa.getRemark());
                    stockMovementService.stockMovementInERP(true, sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), sad.getReturnSerialNames(), sad.getReturnQuantity(), false);
                }


            } else if (transactionType == TransactionType.IN) {
                if (sad.getFinalQuantity() > 0) {
                    stockService.increaseInventory(sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), sad.getFinalSerialNames(), sad.getFinalQuantity());
                    inventory = stockService.updateERPInventory(true, sa.getBusinessDate(), sa.getProduct(), sa.getPackaging(), sa.getUom(), sad.getFinalQuantity(), sa.getRemark());
                    stockMovementService.stockMovementInERP(true, sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), sad.getFinalSerialNames(), sad.getFinalQuantity(), false);
                }
//                stockService.increaseInventory(sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getBatchName(), sad.getSerialNames(), sad.getQuantity());
//                inventory = stockService.updateERPInventory(true, sa.getBusinessDate(), sa.getProduct(), sa.getPackaging(), sa.getUom(), sad.getQuantity(), sa.getRemark());
//                stockMovementService.stockMovementInERP(true, sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getBatchName(), sad.getSerialNames(), sad.getQuantity());

//                if (sad.getReturnQuantity() != 0) {
//                    StockMovementDetail smdrej = new StockMovementDetail();
//                    smdrej.setStockMovement(smReject);
//                    smdrej.setLocation(sad.getLocation());
//                    smdrej.setBatchName(sad.getBatchName());
//                    smdrej.setSerialNames(sad.getReturnSerialNames());
//                    smdrej.setQuantity(sad.getReturnQuantity());
//
//                    if (smdrej.getQuantity() != 0) {
//                        smdRejectedSet.add(smdrej);
//                    }
//                }
            }

            StockMovementDetail smd = new StockMovementDetail();
            smd.setStockMovement(sm);
            smd.setLocation(sad.getLocation());
            smd.setRow(sad.getRow());
            smd.setRack(sad.getRack());
            smd.setBin(sad.getBin());
            smd.setBatchName(sad.getBatchName());
            smd.setSerialNames(sad.getFinalSerialNames());
            smd.setQuantity(sad.getFinalQuantity());
            if (smd.getQuantity() != 0) {
                smdSet.add(smd);
            }
        }
        if(transactionType == TransactionType.OUT && (sa.getMemo().contains("Stock Adjustment created for Pick Pack"))){
            inventory = stockService.updateERPInventory(false, sa.getBusinessDate(), sa.getProduct(), sa.getPackaging(), sa.getUom(), sa_quantity, sa.getRemark());
        }

        if (!smdSet.isEmpty() && smdSet.size() > 0 && sm != null) {
            sm.setStockMovementDetails(smdSet);
            stockMovementService.addStockMovement(sm);
        }

//        if (!smdRejectedSet.isEmpty() && smdRejectedSet.size() > 0 && smReject != null) {
//            smReject.setStockMovementDetails(smdRejectedSet);
//            stockMovementService.addStockMovement(smReject);
//        }


         if (!"SA delete".equals(remark)) {
            sa.setInventoryRef(inventory);
            stockAdjustmentDAO.saveOrUpdateAdjustment(sa);
        }

        if (transactionType == TransactionType.IN) {

            for (StockAdjustmentDetail sad : sa.getStockAdjustmentDetail()) {
                if (!StringUtil.isNullOrEmpty(sad.getReturnSerialNames())) {
                    NewProductBatch productBatch = stockService.getERPProductBatch(sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName());
                    if (productBatch != null) {
                        String[] serialArray = sad.getReturnSerialNames().split(",");
                        for (String serialName : serialArray) {
                            NewBatchSerial batchSerial = stockService.getERPBatchSerial(sa.getProduct(), productBatch, serialName);
                            if (batchSerial != null) {
                                batchSerial.setQaApprovalstatus(QaApprovalStatus.REJECTED);
                                stockMovementDAO.saveOrUpdate(batchSerial);
                            }
                        }
                    }
                }
            }
        }
        stockAdjustmentDAO.setBatchSerialDateDetail(sa.getCompany());
    }

    private void updateSAMovementOnRejection(StockAdjustment sa, TransactionType transactionType, String remark) throws ServiceException {
        StockMovement sm = new StockMovement(sa.getProduct(), sa.getStore(), sa.getQuantity(), sa.getPricePerUnit(), sa.getTransactionNo(), sa.getBusinessDate(), transactionType, TransactionModule.STOCK_ADJUSTMENT, sa.getId(), sa.getId());
        sm.setStockUoM(sa.getProduct().getUnitOfMeasure());
        sm.setCostCenter(sa.getCostCenter());
        if (!StringUtil.isNullOrEmpty(sa.getRemark())) {
            sm.setRemark(sa.getRemark());
        } else {
            sm.setRemark(remark);
        }
        Set<StockMovementDetail> smdSet = new HashSet<StockMovementDetail>();

        for (StockAdjustmentDetail sad : sa.getStockAdjustmentDetail()) {
            if (transactionType == TransactionType.IN) {
                if (sad.getReturnQuantity() != 0) {
                    stockService.increaseInventory(sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), sad.getReturnSerialNames(), sad.getReturnQuantity());
                    stockService.updateERPInventory(true, sa.getBusinessDate(), sa.getProduct(), sa.getPackaging(), sa.getUom(), sad.getReturnQuantity(), sa.getRemark());
                    stockMovementService.stockMovementInERP(true, sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), sad.getReturnSerialNames(), sad.getReturnQuantity(), false);
                }
            }
            StockMovementDetail smd = new StockMovementDetail();
            smd.setStockMovement(sm);
            smd.setLocation(sad.getLocation());
            smd.setBatchName(sad.getBatchName());
            smd.setSerialNames(sad.getSerialNames());
            smd.setQuantity(sad.getQuantity());
            smdSet.add(smd);
        }
        if (!smdSet.isEmpty()) {
            sm.setStockMovementDetails(smdSet);
            stockMovementService.addStockMovement(sm);
        }
        stockAdjustmentDAO.setBatchSerialDateDetail(sa.getCompany());
    }
    
     @Override
    public void saveSADetailInTemporaryTable(Product product, Store store, Location location, String batchName, Map<String, Object> tempTablMap) throws ServiceException, ParseException {
        saveSADetailInTemporaryTable( product,  store,  location,  batchName,  tempTablMap,null);
    }

    @Override
    public void saveSADetailInTemporaryTable(Product product, Store store, Location location, String batchName, Map<String, Object> tempTablMap,SimpleDateFormat dfrm) throws ServiceException, ParseException {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        if(dfrm!=null){
            df=dfrm;
        }
        String[] serial = null;
        String[] mfgDate = null;
        String[] skuValue = null;
        String serialNames = tempTablMap.containsKey("serials") ? tempTablMap.get("serials").toString() : "";
        String mfgdates = tempTablMap.containsKey("mfgdate") ? tempTablMap.get("mfgdate").toString() : "";
        String expdates = tempTablMap.containsKey("expdate") ? tempTablMap.get("expdate").toString() : "";
        String warrantyexpfromdates = tempTablMap.containsKey("warrantyexpfromdate") ? tempTablMap.get("warrantyexpfromdate").toString() : "";
        String warrantyexptodates = tempTablMap.containsKey("warrantyexptodate") ? tempTablMap.get("warrantyexptodate").toString() : "";
        String skuFields = tempTablMap.containsKey("skufields") ? tempTablMap.get("skufields").toString() : "";

        if (!StringUtil.isNullOrEmpty(serialNames)) {
            serial = serialNames.split(",");
        }
        if (!StringUtil.isNullOrEmpty(mfgdates)) {
            mfgDate = mfgdates.split(",");
        }
        String[] expDate = expdates.split(",");
        String[] warrantyExpFromDate = warrantyexpfromdates.split(",");
        String[] warrantyExpToDate = warrantyexptodates.split(",");
        skuValue = skuFields.split(",");
        if (serial != null) {
            for (int i = 0; i < serial.length; i++) {
                Date d1 = null, d2 = null, d3 = null, d4 = null;
                String sku = "";
                if (mfgDate != null && mfgDate.length > i) {
                    d1 = StringUtil.isNullOrEmpty(mfgDate[i]) ? null : df.parse(mfgDate[i]);
                }
                if (expDate.length > i) {
                    d2 = StringUtil.isNullOrEmpty(expDate[i]) ? null : df.parse(expDate[i]);
                }

                if (warrantyExpFromDate.length > i) {
                    d3 = StringUtil.isNullOrEmpty(warrantyExpFromDate[i]) ? null : df.parse(warrantyExpFromDate[i]);
                }

                if (warrantyExpToDate.length > i) {
                    d4 = StringUtil.isNullOrEmpty(warrantyExpToDate[i]) ? null : df.parse(warrantyExpToDate[i]);
                }
                if (skuValue.length > i) {
                    sku = StringUtil.isNullOrEmpty(skuValue[i]) ? "" : skuValue[i];
                }

                stockAdjustmentDAO.saveSADetailInTemporaryTable(product.getID(), store.getId(), location.getId(), batchName, serial[i], d1, d2, d3, d4, sku);
            }
        } else {
            stockAdjustmentDAO.saveSADetailInTemporaryTable(product.getID(), store.getId(), location.getId(), batchName, "", null, expDate.length > 0 && StringUtil.isNullOrEmpty(expDate[0]) ? null : df.parse(expDate[0]), warrantyExpFromDate.length > 0 && StringUtil.isNullOrEmpty(warrantyExpFromDate[0]) ? null : df.parse(warrantyExpFromDate[0]), warrantyExpToDate.length > 0 && StringUtil.isNullOrEmpty(warrantyExpToDate[0]) ? null : df.parse(warrantyExpToDate[0]), "");
        }
    }

    private void setSAFinalQuantityASZero(StockAdjustment sa) throws ServiceException {
        for (StockAdjustmentDetail saDetail : sa.getStockAdjustmentDetail()) {
            saDetail.setFinalQuantity(0);
            saDetail.setFinalSerialNames(null);
        }
        sa.setFinalQuantity(0);
        stockAdjustmentDAO.saveOrUpdateAdjustment(sa);
    }

    @Override
    public List<StockAdjustment> getStockAdjustmentRows(HashMap<String, Object> requestParams) throws ServiceException {
        return stockAdjustmentDAO.getStockAdjustmentRows(requestParams);
    }
      @Override
    public JSONObject deleteSA(String saId, Company company, User user, boolean isPermanent) throws ServiceException, ParseException {
        StringBuffer productIds = new StringBuffer();
        JSONObject jobj = new JSONObject();
        boolean valid = false;
        String msg = "";
        try {
            StockAdjustment sa = stockAdjustmentDAO.getStockAdjustmentById(saId);
            
            if (sa != null) {
                if (sa.isIsJobWorkIn()) {
                    // Fetch available quantity from product batch 
                    boolean avlQuatity = checkForAvlQuantity(sa);
                    if (avlQuatity) {
                        for (StockAdjustmentDetail sad : sa.getStockAdjustmentDetail()) {
                            //Remove quantity of Stock Adjustment IN from in_stock and newproductbatch table and update inventory table entry
                            stockService.decreaseInventory(sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), sad.getFinalSerialNames(), sad.getFinalQuantity());
                            stockService.updateERPInventory(false, sa.getBusinessDate(), sa.getProduct(), sa.getPackaging(), sa.getUom(), sad.getFinalQuantity(), sa.getRemark());
                            stockMovementService.stockMovementInERP(false, sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), sad.getFinalSerialNames(), sad.getFinalQuantity(), false);
                        }
                    deleteSAPermanently(sa);
                    valid = true;
                    msg = "Stock Adjustment record is deleted successfully.";
                } else {
                        // System will not allow to delete Job Work Stock In if quantity of inventory product is already consume in build assembly
                        throw new InventoryException(InventoryException.Type.NULL, "Quantity is not available for product : "+sa.getProduct().getProductid()+", so you can not delete Transaction : " + sa.getTransactionNo()+"." );
                    }
                } else {
                    if(isPermanent) {
                        boolean breakLoopIfQAProcess = false;
                        if ("Stock In".equalsIgnoreCase(sa.getAdjustmentType()) && StringUtil.isNullOrEmpty(msg)) {
                            boolean avlQuatity = checkForAvlQuantity(sa);
                            if(sa.isIsdeleted()){
                                valid = true;
                            }else if (avlQuatity) {
                                valid = true;
                            } else {
                                msg = "Quantity is not available for product : "+sa.getProduct().getProductid()+" , so you can not delete Transaction : "+sa.getTransactionNo()+".";
                            }
                        } else if ("Stock Out".equalsIgnoreCase(sa.getAdjustmentType()) || "Stock Sales".equalsIgnoreCase(sa.getAdjustmentType())) {
                                valid = true;
                                msg = "Stock Adjustment record is deleted successfully.";
                           
                        }
                        if(valid)
                        {
                            if(sa.isIsdeleted()){
                                String sequenceNo = "R"+sa.getTransactionNo();
                                List<StockAdjustment> saList = null;
                                saList = getStockAdjustmentBySequenceNo(company, sequenceNo);
                                if (saList != null && !saList.isEmpty()) {
                                    for (StockAdjustment revsa : saList) {
                                        if(sa.getProduct().getProductid().equals(revsa.getProduct().getProductid())){
                                            deleteSAPermanently(revsa);
                                        }
                                    }
                                }
                            }else if ("Stock Out".equalsIgnoreCase(sa.getAdjustmentType()) || "Stock Sales".equalsIgnoreCase(sa.getAdjustmentType())) {
                                //updateSAMovement(sa, TransactionType.IN, "SA delete");
                                for (StockAdjustmentDetail sad : sa.getStockAdjustmentDetail()){
                                    List<SADetailApproval> approvalList = stockAdjustmentDAO.getStockAdjustmentApprovalDetail(sad.getId());   // if transaction is some how related to QA then not allow user to delete that transaction
                                    if (approvalList != null && !approvalList.isEmpty()) {                                       
                                                breakLoopIfQAProcess = true;
                                                valid = false;
                                                msg = "Cannot delete stock adjustment with QA process for product "+sa.getProduct().getProductid()+" Transaction : "+sa.getTransactionNo()+".";  
                                                break;                                       
                                    }
                                    if(!breakLoopIfQAProcess){
                                        stockService.increaseInventory(sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), sad.getFinalSerialNames(), sad.getFinalQuantity());
                                        stockMovementService.stockMovementInERP(true, sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), sad.getFinalSerialNames(), sad.getFinalQuantity(), false);
                                    }                                    
                                }
                                if(!StringUtil.isNullObject(sa.getInventoryRef()) && !breakLoopIfQAProcess){
                                accProductDaoObj.deleteInventory(sa.getInventoryRef().getID(), sa.getCompany().getCompanyID());
                                }
                                } else {                                
                                for (StockAdjustmentDetail sad : sa.getStockAdjustmentDetail()){
                                    List<SADetailApproval> approvalList = stockAdjustmentDAO.getStockAdjustmentApprovalDetail(sad.getId()); // if transaction is some how related to QA then not allow user to delete that transaction
                                    if (approvalList != null && !approvalList.isEmpty()) {                                        
                                                breakLoopIfQAProcess = true;
                                                valid = false;
                                                msg = "Cannot delete stock adjustment with QA process for product "+sa.getProduct().getProductid()+" Transaction : "+sa.getTransactionNo()+".";
                                                break;                                        
                                    }
                                    if(!breakLoopIfQAProcess){
                                        stockService.decreaseInventory(sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), sad.getFinalSerialNames(), sad.getFinalQuantity());
                                        stockMovementService.stockMovementInERP(false, sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), sad.getFinalSerialNames(), sad.getFinalQuantity(), false);
                                    }                                    
                                }
                                if(!StringUtil.isNullObject(sa.getInventoryRef()) && !breakLoopIfQAProcess){
                                    accProductDaoObj.deleteInventory(sa.getInventoryRef().getID(), sa.getCompany().getCompanyID());
                                }                                
                            } 
                            if(!breakLoopIfQAProcess){
                                deleteSAPermanently(sa);
                                valid = true;
                                msg = "Stock Adjustment record is deleted permanently.";
                            }                             
                        }
                    } else {
                        StockAdjustment revSA = new StockAdjustment();
                        Set<StockAdjustmentDetail> stockAdjustmentDetail = new HashSet<StockAdjustmentDetail>();
                        revSA.setAdjustmentType(("Stock Out".equalsIgnoreCase(sa.getAdjustmentType()) ? "Stock In" : "Stock Out"));
                        revSA.setBusinessDate(sa.getBusinessDate());
                        revSA.setCompany(company);
                        revSA.setCostCenter(sa.getCostCenter());
                        revSA.setCreatedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                        revSA.setCreator(user);
                        revSA.setFinalQuantity(sa.getFinalQuantity());
                        revSA.setMemo(sa.getMemo());
                        revSA.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                        revSA.setModifier(user);
                        revSA.setPackaging(sa.getPackaging());
                        revSA.setProduct(sa.getProduct());
                        revSA.setPricePerUnit(sa.getPricePerUnit());
                        revSA.setQuantity(sa.getQuantity());
                        revSA.setReason(sa.getReason());
                        revSA.setRemark(sa.getRemark());
                        revSA.setStatus(sa.getStatus());
                        revSA.setStockAdjDraft(sa.getStockAdjDraft());
                        revSA.setUom(sa.getUom());
                        revSA.setTransactionNo("R" + sa.getTransactionNo());
                        revSA.setTransactionModule(TransactionModule.STOCK_ADJUSTMENT);
                        revSA.setStockAdjustmentReason(sa.getStockAdjustmentReason());
                        revSA.setStore(sa.getStore());
                        revSA.setStockAdjustmentCustomData(sa.getStockAdjustmentCustomData());
                        boolean breakLoopIfQAProcess = false;
                        if (sa.getStockAdjustmentDetail() != null) {
                            for (StockAdjustmentDetail saDtl : sa.getStockAdjustmentDetail()) {
                                if (breakLoopIfQAProcess) {
                                    break;
                                }
                                StockAdjustmentDetail stockAdDtl = new StockAdjustmentDetail();
                                stockAdDtl.setBatchName(saDtl.getBatchName());
                                stockAdDtl.setBin(saDtl.getBin());
                                stockAdDtl.setFinalQuantity(saDtl.getFinalQuantity());
                                stockAdDtl.setFinalSerialNames(saDtl.getFinalSerialNames());
                                stockAdDtl.setLocation(saDtl.getLocation());
                                stockAdDtl.setQuantity(saDtl.getQuantity());
                                stockAdDtl.setRack(saDtl.getRack());
                                stockAdDtl.setRow(saDtl.getRow());
                                stockAdDtl.setSerialNames(saDtl.getSerialNames());
                                stockAdDtl.setStockAdjustment(revSA);
                                stockAdjustmentDetail.add(stockAdDtl);
                                List<SADetailApproval> approvalList = stockAdjustmentDAO.getStockAdjustmentApprovalDetail(saDtl.getId());
                                if (approvalList != null && !approvalList.isEmpty()) {  // if transaction is some how related to QA then not allow user to delete that transaction
                                            breakLoopIfQAProcess = true;
                                            msg = "Cannot delete stock adjustment with QA process for product "+sa.getProduct().getProductid()+" Transaction : "+sa.getTransactionNo()+".";
                                            break;                                                            
                                }
                            }
                        }
                        revSA.setStockAdjustmentDetail(stockAdjustmentDetail);
                        if ("Stock Out".equalsIgnoreCase(sa.getAdjustmentType()) || "Stock Sales".equalsIgnoreCase(sa.getAdjustmentType()) && StringUtil.isNullOrEmpty(msg)) {
                            valid = true;
                            msg = "Stock Adjustment record is deleted temporary successfully.";
                        } else if (StringUtil.isNullOrEmpty(msg)) {
                            boolean avlQuatity = checkForAvlQuantity(sa);
                            if (avlQuatity) {

                                valid = true;
                                msg = "Stock Adjustment record is deleted temporary successfully.";
                            } else {
                                msg = "Quantity is not available for product : "+sa.getProduct().getProductid()+" , so you can not delete Transaction : "+sa.getTransactionNo()+".";
                            }
                        }
                        if (valid) {
                            sa.setIsdeleted(true);
                            if (sa.getInventoryJE() != null) {
                                sa.getInventoryJE().setDeleted(true);
                            }
                            stockAdjustmentDAO.saveOrUpdateAdjustment(sa);
                            //ERP-30726
                            revSA.setIsdeleted(true);
                            stockAdjustmentDAO.saveOrUpdateAdjustment(revSA);
                            if ("Stock Out".equalsIgnoreCase(sa.getAdjustmentType()) || "Stock Sales".equalsIgnoreCase(sa.getAdjustmentType())) {
                                updateSAMovement(revSA, TransactionType.IN, "SA delete");
                            } else {
                                updateSAMovement(revSA, TransactionType.OUT, "SA delete");
                            }
                        }
                        if (sa.getProduct() != null && productIds.indexOf(sa.getProduct().getID()) == -1) {
                            productIds.append(sa.getProduct().getID()).append(",");
                        }
                    }
                }
            }
            jobj.put("success", valid);
            jobj.put("msg", msg);
            jobj.put("productIds", productIds);
        } catch (Exception ex) {
             throw new InventoryException(InventoryException.Type.NULL, ex.getMessage());
        }
        return jobj;
    }
    private void deleteSAPermanently(StockAdjustment sa) throws ServiceException {
        try {
            stockAdjustmentDAO.deleteSAPermanently(sa);
        } catch (Exception ex) {
            throw new InventoryException(InventoryException.Type.NULL, ex.getMessage());
        }
    }
    /**
     * Delete stock adjustment created with build assembly.
     */
    public void deleteStockadjustmentForBuildassemby(Company company, String productbuild, User user) throws ServiceException {
        if (!StringUtil.isNullObject(productbuild)) {
            List SA_List = stockAdjustmentDAO.getStockAdjustmentByProductBuild(company, productbuild);
            if (SA_List != null && !SA_List.isEmpty()) {
                for (Object SA : SA_List) {
                    try {
                        Object[] SA_Obj = (Object[]) SA;
                        String SA_ID = SA_Obj[0].toString();
                        deleteSA(SA_ID, company, user, true);
                    } catch (ParseException ex) {
                        Logger.getLogger(StockAdjustmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    private boolean checkForAvlQuantity(StockAdjustment sa) throws ServiceException {
        boolean avlQty = false;
        try {
            if (sa != null) {
                Product product = sa.getProduct();
                Store store = sa.getStore();
                if (sa.getStockAdjustmentDetail() != null) {
                    for (StockAdjustmentDetail sadtl : sa.getStockAdjustmentDetail()) {
                        Location location = sadtl.getLocation();
                        if (product.isIsBatchForProduct() || product.isIsSerialForProduct() || product.isIsBatchForProduct() || product.isIslocationforproduct()) {
                            NewProductBatch productBatch = stockService.getERPProductBatch(product, store, location,  sadtl.getRow(), sadtl.getRack(), sadtl.getBin(), sadtl.getBatchName());
                            if (productBatch != null) {
                                if (productBatch.getQuantitydue() >= sadtl.getFinalQuantity() && !product.isIsSerialForProduct()) {
                                    avlQty = true;
                                } else if (product.isIsSerialForProduct() && productBatch.getQuantitydue() >= sadtl.getFinalQuantity()) {

                                    String[] serArr = sadtl.getFinalSerialNames().split(",");
                                    if (serArr.length > 0) {
                                        List<String> srList = Arrays.asList(serArr);
                                        List<StockMovement> smList = stockMovementDAO.getStockMovementByProduct(sa.getCompany(), product, store, sa.getCreatedOn(), sadtl.getFinalSerialNames(), sa.getId());
                                        if (smList.size() > 0) {
                                            for (StockMovement sm : smList) {
                                                for (StockMovementDetail dtl : sm.getStockMovementDetails()) {
                                                    String[] smserArr = dtl.getSerialNames().split(",");
                                                    List<String> l3 = Arrays.asList(smserArr);
                                                    if (!Collections.disjoint(l3, srList)) {
                                                        throw new InventoryException(InventoryException.Type.NULL, "Transactions are found after Stock adjustment.");
                                                    }
                                                }
                                            }
                                        }

                                        List<NewBatchSerial> listSeril = stockService.getERPActiveSerialList(product, productBatch, false);
                                        if (listSeril.size() > 0) {
                                            for (NewBatchSerial sr : listSeril) {
                                                if (!srList.contains(sr.getSerialname())) {
                                                    throw new InventoryException(InventoryException.Type.NULL, "Quantity is not available for product "+sa.getProduct().getProductid()+", so you can not delete Transaction : "+sa.getTransactionNo()+".");
                                                } else {
                                                    /**
                                                     * If serial number is not
                                                     * used then set avlQty to
                                                     * true(ERP-31476). 
                                                     */
                                                    avlQty = true;
                                                }
                                            }
                                        }

                                    }
                                }
                            } else{
                                 throw new InventoryException(InventoryException.Type.NULL, "Quantity is not available for product "+sa.getProduct().getProductid()+", so you can not delete Transaction : "+sa.getTransactionNo()+".");
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new InventoryException(InventoryException.Type.NULL, ex.getMessage());
        }
        return avlQty;
    }
    @Override
    public int stockOutCreatedFromOtherTransaction(JSONObject jSONObject) throws ServiceException {
        int retValue=0;
        KwlReturnObject kwlReturnObject = stockAdjustmentDAO.getSAfromGRN(jSONObject);
        int count = kwlReturnObject.getRecordTotalCount();
        if (count > 0) {
            /**
             * Record created from Back end
             */
            retValue=1;
            return retValue;
        }
        if (jSONObject.optBoolean("checkQCTransaction", false)) {
            kwlReturnObject = stockAdjustmentDAO.checkStockAdjustmentForQC(jSONObject);
            count = kwlReturnObject.getRecordTotalCount();
            if (count > 0) {
                retValue=2;
                return retValue;
            }
        }
        
        kwlReturnObject = stockAdjustmentDAO.getSAfromDO(jSONObject);
        int cnt = kwlReturnObject.getRecordTotalCount();
        if (cnt > 0) {
            /**
             * Record created from Back end
             */
            retValue=3;
            return 3;
        }
        return retValue;
    }
}
