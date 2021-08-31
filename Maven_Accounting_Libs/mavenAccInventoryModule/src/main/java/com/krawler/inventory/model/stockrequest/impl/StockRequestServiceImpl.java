/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockrequest.impl;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.exception.NegativeInventoryException;
import com.krawler.inventory.model.approval.InspectionDetail;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferApprovalService;
import com.krawler.inventory.model.configuration.InventoryConfig;
import com.krawler.inventory.model.configuration.InventoryConfigService;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.packaging.PackagingService;
import com.krawler.inventory.model.stock.StockCustomData;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.*;
import com.krawler.inventory.model.stockrequest.*;
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

/**
 *
 * @author Vipin Gupta
 */
public class StockRequestServiceImpl implements StockRequestService {

    private StockRequestDAO stockRequestDAO;
    private InventoryConfigService invConfigService;
    private StockService stockService;
    private LocationService locationService;
    private PackagingService packagingService;
    private StoreService storeService;
    private StockMovementService stockMovementService;
    private StockTransferApprovalService approvalService;
    private fieldDataManager fieldDataManagercntrl;
    private StockMovementDAO stockMovementDAO;
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void setStockRequestDAO(StockRequestDAO stockRequestDAO) {
        this.stockRequestDAO = stockRequestDAO;
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

    public void setPackagingService(PackagingService packagingService) {
        this.packagingService = packagingService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    public void setApprovalService(StockTransferApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public StockMovementDAO getStockMovementDAO() {
        return stockMovementDAO;
    }

    public void setStockMovementDAO(StockMovementDAO stockMovementDAO) {
        this.stockMovementDAO = stockMovementDAO;
    }

    @Override
    public void addStockOrderRequest(User user, StockRequest sr, Map<String,Object> requestParams) throws ServiceException {
        try {
            if (sr == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Stock Request is null");
            }
            if (sr.getUom() == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Please select uom for Product : " + sr.getProduct().getProductid());
            }
            sr.setStatus(RequestStatus.ORDERED);

            if (sr.getPackaging() != null) {
                Packaging packaging = packagingService.createClonePackaging(sr.getPackaging());
                sr.setPackaging(packaging);
            } else {
                Packaging packaging = packagingService.createPackagingByStockUom(sr.getProduct().getUnitOfMeasure());
                sr.setPackaging(packaging);
            }
            if (sr.getUom() != null) {
                sr.setUom(sr.getUom());
            }
            sr.setRequestedBy(user);
            sr.setRequestedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            sr.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long modifiedDate = System.currentTimeMillis();
            sr.setModifieddate(modifiedDate);
            stockRequestDAO.saveOrUpdate(sr);
            
            //Save Stock Request Custom Data
            String globalLevelCustomData = (String) (requestParams.containsKey(Constants.customfield)?requestParams.get(Constants.customfield):"");
            String lineLevelCustomData = (String) (requestParams.containsKey(Constants.LineLevelCustomData)?requestParams.get(Constants.LineLevelCustomData):"");
            
            if (!StringUtil.isNullOrEmpty(globalLevelCustomData) || !StringUtil.isNullOrEmpty(lineLevelCustomData)) {
                HashMap<String, Object> customRequestParams = new HashMap<>();
                customRequestParams.put(Constants.modulename, Constants.Acc_Stock_modulename);
                customRequestParams.put(Constants.moduleprimarykey, Constants.Acc_StockId);
                customRequestParams.put(Constants.moduleid, Constants.Acc_Stock_Request_ModuleId);
                customRequestParams.put(Constants.companyid, user.getCompany().getCompanyID());
                customRequestParams.put(Constants.modulerecid, sr.getId());
                customRequestParams.put(Constants.customdataclasspath, Constants.Stock_custom_data_classpath);
                KwlReturnObject customDataResult = null;
                
                /*
                 * Save Global level custom data
                 */
                if (!StringUtil.isNullOrEmpty(globalLevelCustomData)) {
                    customRequestParams.put(Constants.customarray, new JSONArray(globalLevelCustomData));
                    customDataResult = fieldDataManagercntrl.setCustomData(customRequestParams);
                    if (customDataResult != null && customDataResult.getEntityList().size() > 0) {
                        StockCustomData stockGlobalCustomData = (StockCustomData) customDataResult.getEntityList().get(0);
                        sr.setStockCustomData(stockGlobalCustomData);
                    }
                }
                /*
                 *Save Line level custom data
                 */
                customDataResult = null;
                if (!StringUtil.isNullOrEmpty(lineLevelCustomData)) {
                    customRequestParams.put(Constants.customarray, new JSONArray(lineLevelCustomData));
                    customDataResult = fieldDataManagercntrl.setCustomData(customRequestParams);
                    if (customDataResult != null && customDataResult.getEntityList().size() > 0) {
                        StockCustomData stockLineCustomData = (StockCustomData) customDataResult.getEntityList().get(0);
                        sr.setStockLineLevelCustomData(stockLineCustomData);
                    }
                }
                stockRequestDAO.saveOrUpdate(sr);
            }
        } catch (JSONException | ParseException | SessionExpiredException ex) {
            Logger.getLogger(StockRequestServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void approveStockOrderRequest(User user, StockRequest stockRequest) throws ServiceException {
        try {
            if (stockRequest == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Stock Request is null");
            }
            stockRequest.setApprovedBy(user);
            stockRequest.setStatus(RequestStatus.ORDERED);
            stockRequest.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long modifiedDate = System.currentTimeMillis();
            stockRequest.setModifieddate(modifiedDate);
            stockRequestDAO.saveOrUpdate(stockRequest);
        } catch (ParseException ex) {
            Logger.getLogger(StockRequestServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockRequestServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void rejectStockOrderRequest(User user, StockRequest stockRequest) throws ServiceException {
        try {
            if (stockRequest == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Stock Request is null");
            }
            stockRequest.setApprovedBy(user);
            stockRequest.setStatus(RequestStatus.REJECTED);
            stockRequest.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long modifiedDate = System.currentTimeMillis();
            stockRequest.setModifieddate(modifiedDate);
            stockRequestDAO.saveOrUpdate(stockRequest);
        } catch (ParseException ex) {
            Logger.getLogger(StockRequestServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockRequestServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void issueStockOrderRequest(User user, StockRequest sr) throws ServiceException, NegativeInventoryException {
        issueStockOrderRequest(user, sr, false);
    }

    @Override
    public void issueStockOrderRequest(User user, StockRequest sr, boolean allowNegativeInventory) throws ServiceException, NegativeInventoryException {
        try {
            if (sr == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Stock Request is null");
            }
            if (sr.getStockRequestDetails().isEmpty()) {
                throw new InventoryException(InventoryException.Type.NULL, "Stock Request detail is empty");
            }

            sr.setIssuedBy(user);
            sr.setStatus(RequestStatus.ISSUED);
            sr.setIssuedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long issuedDate = System.currentTimeMillis();
            sr.setIssueddate(issuedDate);
            sr.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long modifiedDate = System.currentTimeMillis();
            sr.setModifieddate(modifiedDate);

            stockRequestDAO.saveOrUpdate(sr);

            addToSRStockBuffer(sr);
        } catch (ParseException ex) {
            Logger.getLogger(StockRequestServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockRequestServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void collectStockOrderRequest(User user, StockRequest sr) throws ServiceException {
        collectStockOrderRequest(user, sr, null);
    }
    @Override
    public void collectStockOrderRequest(User user, StockRequest sr, String stockMovementRemark) throws ServiceException {
        try {
            if (sr == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Stock Request is null");
            }
            sr.setCollectedBy(user);
            sr.setStatus(RequestStatus.COLLECTED);
            sr.setCollectedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long collectedDate = System.currentTimeMillis();
            sr.setCollecteddate(collectedDate);
            sr.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long modifiedDate = System.currentTimeMillis();
            sr.setModifieddate(modifiedDate);
            
            stockRequestDAO.saveOrUpdate(sr);

            double returnQty = sr.getIssuedQty() - sr.getDeliveredQty();

            if (returnQty > 0) {
                createReturnRequest(sr);
                //            returnFromSRStockBuffer(sr);
            }
            collectSRStockBuffer(sr, stockMovementRemark);
        } catch (ParseException ex) {
            Logger.getLogger(StockRequestServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockRequestServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void stockIssueByIssueNote(User user, StockRequest sr) throws ServiceException, NegativeInventoryException, JSONException {
        stockIssueByIssueNote(user, sr, false, null);
    }

    @Override
    public void stockIssueByIssueNote(User user, StockRequest sr, boolean allowNegativeInventory, Map<String, Object> requestParams) throws ServiceException, NegativeInventoryException, JSONException {
        try {
            if (sr == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Stock request is null");
            }
            if (sr.getStockRequestDetails().isEmpty()) {
                throw new InventoryException(InventoryException.Type.NULL, "Stock Isuue detail is empty");
            }
            if (sr.getUom() == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Please select uom for Product : " + sr.getProduct().getProductid());
            }
            if (sr.getPackaging() == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Please select packaging for Product : " + sr.getProduct().getProductid());
            }
            sr.setRequestedBy(user);
            sr.setIssuedBy(user);
            sr.setCollectedBy(user);
            sr.setRequestedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            sr.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long modifiedDate = System.currentTimeMillis();
            sr.setModifieddate(modifiedDate);
            sr.setIssuedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long issuedDate = System.currentTimeMillis();
            sr.setIssueddate(issuedDate);
//            sr.setCollectedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long collectedDate = System.currentTimeMillis();
            sr.setCollecteddate(collectedDate);

            if (sr.getPackaging() != null) {
                Packaging packaging = packagingService.createClonePackaging(sr.getPackaging());
                sr.setPackaging(packaging);
            } else {
                Packaging packaging = packagingService.createPackagingByStockUom(sr.getProduct().getUnitOfMeasure());
                sr.setPackaging(packaging);
            }
            //        if (sr.getProduct().getOrderingUOM() != null) {
            //            sr.setUom(sr.getProduct().getOrderingUOM());
            //        }
            sr.setStatus(RequestStatus.COLLECTED);

            stockRequestDAO.saveOrUpdate(sr);

            //Custom Data
            String globalLevelCustomData = (String) (requestParams.containsKey(Constants.customfield) ? requestParams.get(Constants.customfield) : "");
            String lineLevelCustomData = (String) (requestParams.containsKey(Constants.LineLevelCustomData) ? requestParams.get(Constants.LineLevelCustomData) : "");

            if (!StringUtil.isNullOrEmpty(globalLevelCustomData) || !StringUtil.isNullOrEmpty(lineLevelCustomData)) {
                HashMap<String, Object> customRequestParams = new HashMap<>();
                customRequestParams.put(Constants.modulename, Constants.Acc_Stock_modulename);
                customRequestParams.put(Constants.moduleprimarykey, Constants.Acc_StockId);
                customRequestParams.put(Constants.moduleid, Constants.Inventory_ModuleId);
                customRequestParams.put(Constants.companyid, user.getCompany().getCompanyID());
                customRequestParams.put(Constants.modulerecid, sr.getId());
                customRequestParams.put(Constants.customdataclasspath, Constants.Stock_custom_data_classpath);
                KwlReturnObject customDataResult = null;

                /*
                 * Save Global level custom data
                 */
                if (!StringUtil.isNullOrEmpty(globalLevelCustomData)) {
                    customRequestParams.put(Constants.customarray, new JSONArray(globalLevelCustomData));
                    customDataResult = fieldDataManagercntrl.setCustomData(customRequestParams);
                    if (customDataResult != null && customDataResult.getEntityList().size() > 0) {
                        StockCustomData stockGlobalCustomData = (StockCustomData) customDataResult.getEntityList().get(0);
                        sr.setStockCustomData(stockGlobalCustomData);
                    }
                }
                /*
                 *Save Line level custom data
                 */
                customDataResult = null;
                if (!StringUtil.isNullOrEmpty(lineLevelCustomData)) {
                    customRequestParams.put(Constants.customarray, new JSONArray(lineLevelCustomData));
                    customDataResult = fieldDataManagercntrl.setCustomData(customRequestParams);
                    if (customDataResult != null && customDataResult.getEntityList().size() > 0) {
                        StockCustomData stockLineCustomData = (StockCustomData) customDataResult.getEntityList().get(0);
                        sr.setStockLineLevelCustomData(stockLineCustomData);
                    }
                }
                stockRequestDAO.saveOrUpdate(sr);
            }
            
            transferIssueNoteStock(sr);
        } catch (ParseException | SessionExpiredException ex) {
            Logger.getLogger(StockRequestServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void transferIssueNoteStock(StockRequest sr) throws ServiceException {

        double productPrice = stockService.getProductPurchasePrice(sr.getProduct(), sr.getBusinessDate());

        StockMovement smIssue = new StockMovement(sr.getProduct(), sr.getFromStore(), 0, 0, sr.getTransactionNo(), sr.getBusinessDate(), TransactionType.OUT, TransactionModule.ISSUE_NOTE, sr.getId(), sr.getId());
        smIssue.setStockUoM(sr.getProduct().getUnitOfMeasure());
        smIssue.setCostCenter(sr.getCostCenter());
        smIssue.setRemark("Issue Note Sent");

        StockMovement smCollect = new StockMovement(sr.getProduct(), sr.getToStore(), 0, 0, sr.getTransactionNo(), sr.getBusinessDate(), TransactionType.IN, TransactionModule.ISSUE_NOTE, sr.getId(), sr.getId());
        smCollect.setStockUoM(sr.getProduct().getUnitOfMeasure());
        smCollect.setCostCenter(sr.getCostCenter());
        smCollect.setRemark("Issue Note Collected");

        Set<StockMovementDetail> smdIssueSet = new HashSet();
        Set<StockMovementDetail> smdCollectSet = new HashSet();

        double totalQuantity = 0;
        for (StockRequestDetail srd : sr.getStockRequestDetails()) {
            totalQuantity += srd.getDeliveredQuantity();
            stockService.increaseInventory(sr.getProduct(), sr.getToStore(), srd.getDeliveredLocation(), srd.getDeliveredRow(), srd.getDeliveredRack(), srd.getDeliveredBin(), srd.getBatchName(), srd.getIssuedSerialNames(), srd.getIssuedQuantity());
            stockService.decreaseInventory(sr.getProduct(), sr.getFromStore(), srd.getIssuedLocation(), srd.getIssuedRow(), srd.getIssuedRack(), srd.getIssuedBin(), srd.getBatchName(), srd.getIssuedSerialNames(), srd.getIssuedQuantity());

            stockMovementService.stockMovementInERP(true, sr.getProduct(), sr.getToStore(), srd.getDeliveredLocation(), srd.getDeliveredRow(), srd.getDeliveredRack(), srd.getDeliveredBin(), srd.getBatchName(), srd.getIssuedSerialNames(), srd.getIssuedQuantity(), false);
            stockMovementService.stockMovementInERP(false, sr.getProduct(), sr.getFromStore(), srd.getIssuedLocation(), srd.getIssuedRow(), srd.getIssuedRack(), srd.getIssuedBin(), srd.getBatchName(), srd.getIssuedSerialNames(), srd.getIssuedQuantity(), false);

            StockMovementDetail smdIssue = new StockMovementDetail(smIssue, srd.getIssuedLocation(), srd.getIssuedRow(), srd.getIssuedRack(), srd.getIssuedBin(), srd.getBatchName(), srd.getIssuedSerialNames(), srd.getIssuedQuantity());
            smdIssueSet.add(smdIssue);
            StockMovementDetail smdCollect = new StockMovementDetail(smCollect, srd.getDeliveredLocation(), srd.getDeliveredRow(), srd.getDeliveredRack(), srd.getDeliveredBin(), srd.getBatchName(), srd.getDeliveredSerialNames(), srd.getDeliveredQuantity());
            smdCollectSet.add(smdCollect);
        }

        smIssue.setQuantity(totalQuantity);
        smIssue.setPricePerUnit(productPrice);
        smIssue.setStockMovementDetails(smdIssueSet);
        stockMovementService.addStockMovement(smIssue);

        smCollect.setQuantity(totalQuantity);
        smCollect.setPricePerUnit(productPrice);
        smCollect.setStockMovementDetails(smdCollectSet);
        stockMovementService.addStockMovement(smCollect);

        stockMovementService.updateProductIssueCount(sr.getProduct(), totalQuantity);

    }

    @Override
    public StockRequest getStockRequestById(String stockRequestId) throws ServiceException {
        if (StringUtil.isNullOrEmpty(stockRequestId)) {
            throw new InventoryException(InventoryException.Type.NULL, "StockRequest Id is null or empty.");
        }
        return stockRequestDAO.getStockRequestById(stockRequestId);
    }

    @Override
    public List<StockRequest> getPendingStockRequestList(Map<String, Object> request, Company company, User requestedBy, Store store, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException {
        RequestStatus[] statusList = new RequestStatus[]{RequestStatus.ORDERED, RequestStatus.ISSUED, RequestStatus.PENDING_APPROVAL};
        return stockRequestDAO.getStockRequestList(request, company, requestedBy, statusList, store, fromDate, toDate, searchString, paging);
    }

    @Override
    public List<StockRequest> getCompletedStockRequestList(Map<String, Object> request, Company company, User requestedBy, Store store, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException {
        RequestStatus[] statusList = new RequestStatus[]{RequestStatus.REJECTED, RequestStatus.COLLECTED, RequestStatus.DELETED, RequestStatus.RETURNED};
        return stockRequestDAO.getStockRequestList(request, company, requestedBy, statusList, store, fromDate, toDate, searchString, paging);
    }

    @Override
    public List<StockRequest> getStockRequestList(Map<String, Object> request, Company company, User requestedBy, Store store, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException {
        return stockRequestDAO.getStockRequestList(request, company, requestedBy, null, store, fromDate, toDate, searchString, paging);
    }

    @Override
    public List<StockRequest> getStockRequestDetailBySequenceNo(Company company, String transactionNo, TransactionModule module) throws ServiceException {
        if (module == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Module Name is null");
        }
        return stockRequestDAO.getStockRequestDetailBySequenceNo(company, transactionNo, module);
    }

    private void addToSRStockBuffer(StockRequest sr) throws ServiceException {
        addToSRStockBuffer(sr, null);
    }
    private void addToSRStockBuffer(StockRequest sr, String remark) throws ServiceException {
        double productPrice = stockService.getProductPurchasePrice(sr.getProduct(), sr.getBusinessDate());

        StockMovement sm = new StockMovement(sr.getProduct(), sr.getToStore(), 0, productPrice, sr.getTransactionNo(), sr.getBusinessDate(), TransactionType.OUT, TransactionModule.STOCK_REQUEST, sr.getId(), sr.getId());
        sm.setStockUoM(sr.getProduct().getUnitOfMeasure());
        sm.setCostCenter(sr.getCostCenter());
        if (!StringUtil.isNullOrEmpty(remark)) {
            sm.setRemark(remark);
        } else {
            sm.setRemark("Stock Issued from store: " + sr.getToStore().getAbbreviation() + ", for Store: " + sr.getFromStore().getAbbreviation());
        }
        double totalQuantity = 0;
        Set<StockMovementDetail> smdSet = new HashSet<StockMovementDetail>();

        for (StockRequestDetail srd : sr.getStockRequestDetails()) {
            totalQuantity += srd.getIssuedQuantity();
            stockService.decreaseInventory(sr.getProduct(), sr.getToStore(), srd.getIssuedLocation(), srd.getIssuedRow(), srd.getIssuedRack(), srd.getIssuedBin(), srd.getBatchName(), srd.getIssuedSerialNames(), srd.getIssuedQuantity());
            stockMovementService.stockMovementInERP(false, sr.getProduct(), sr.getToStore(), srd.getIssuedLocation(), srd.getIssuedRow(), srd.getIssuedRack(), srd.getIssuedBin(), srd.getBatchName(), srd.getIssuedSerialNames(), srd.getIssuedQuantity(), false);
            StockMovementDetail smd = new StockMovementDetail(sm, srd.getIssuedLocation(), srd.getIssuedRow(), srd.getIssuedRack(), srd.getIssuedBin(), srd.getBatchName(), srd.getIssuedSerialNames(), srd.getIssuedQuantity());
            smdSet.add(smd);
        }
        sm.setQuantity(totalQuantity);
        sm.setStockMovementDetails(smdSet);
        stockMovementService.updateProductIssueCount(sr.getProduct(), totalQuantity);
        stockMovementService.addStockMovement(sm);

        stockService.updateERPInventory(false, sr.getBusinessDate(), sr.getProduct(), sr.getPackaging(), sr.getProduct().getUnitOfMeasure(), totalQuantity, "stock issued");
    }

    private void returnFromSRStockBuffer(StockRequest sr) throws ServiceException {
        returnFromSRStockBuffer(sr, null);
    }

    private void returnFromSRStockBuffer(StockRequest sr, String remark) throws ServiceException {
        double productPrice = stockService.getProductPurchasePrice(sr.getProduct(), sr.getBusinessDate());

        StockMovement sm = new StockMovement(sr.getProduct(), sr.getToStore(), 0, productPrice, sr.getTransactionNo(), sr.getBusinessDate(), TransactionType.IN, TransactionModule.STOCK_REQUEST, sr.getId(), sr.getId());
        sm.setStockUoM(sr.getProduct().getUnitOfMeasure());
        sm.setCostCenter(sr.getCostCenter());
        if (!StringUtil.isNullOrEmpty(remark)) {
            sm.setRemark(remark);
        } else {
            sm.setRemark("Stock Returned from store: " + sr.getFromStore().getAbbreviation() + ", to store: " + sr.getToStore().getAbbreviation());
        }
        double totalQuantity = 0;
        Set<StockMovementDetail> smdSet = new HashSet<StockMovementDetail>();

        for (StockRequestDetail srd : sr.getStockRequestDetails()) {
            StockRequestDetail rsrd = srd.getReturnStockDetail();
            if (rsrd != null) {
                double quantity = rsrd.getDeliveredQuantity();
                totalQuantity += quantity;
                stockService.increaseInventory(sr.getProduct(), sr.getToStore(), rsrd.getDeliveredLocation(), rsrd.getDeliveredRow(), rsrd.getDeliveredRack(), rsrd.getDeliveredBin(), rsrd.getBatchName(), rsrd.getDeliveredSerialNames(), quantity);
                stockMovementService.stockMovementInERP(true, sr.getProduct(), sr.getToStore(), rsrd.getDeliveredLocation(), rsrd.getDeliveredRow(), rsrd.getDeliveredRack(), rsrd.getDeliveredBin(), rsrd.getBatchName(), rsrd.getDeliveredSerialNames(), quantity, false);
                StockMovementDetail smd = new StockMovementDetail(sm, rsrd.getDeliveredLocation(), rsrd.getDeliveredRow(), rsrd.getDeliveredRack(), rsrd.getDeliveredBin(), rsrd.getBatchName(), rsrd.getDeliveredSerialNames(), quantity);
                smdSet.add(smd);
            }

        }
        sm.setQuantity(totalQuantity);
        sm.setStockMovementDetails(smdSet);
        stockMovementService.addStockMovement(sm);

        stockService.updateERPInventory(true, sr.getBusinessDate(), sr.getProduct(), sr.getPackaging(), sr.getProduct().getUnitOfMeasure(), totalQuantity, "stock returned");
    }

    private void collectSRStockBuffer(StockRequest sr) throws ServiceException {
        collectSRStockBuffer(sr, null);
    }

    private void collectSRStockBuffer(StockRequest sr, String remark) throws ServiceException {
        double productPrice = stockService.getProductPurchasePrice(sr.getProduct(), sr.getBusinessDate());

        StockMovement sm = new StockMovement(sr.getProduct(), sr.getFromStore(), 0, productPrice, sr.getTransactionNo(), sr.getBusinessDate(), TransactionType.IN, TransactionModule.STOCK_REQUEST, sr.getId(), sr.getId());
        sm.setStockUoM(sr.getProduct().getUnitOfMeasure());
        sm.setCostCenter(sr.getCostCenter());
        if (!StringUtil.isNullOrEmpty(remark)) {
            sm.setRemark(remark);
        } else {
            sm.setRemark("Stock Collected in store: " + sr.getFromStore().getAbbreviation() + ", issued from store: " + sr.getToStore().getAbbreviation());
        }

        double totalQuantity = 0;
        Set<StockMovementDetail> smdSet = new HashSet<StockMovementDetail>();

        for (StockRequestDetail srd : sr.getStockRequestDetails()) {
            if (srd.getDeliveredQuantity() > 0) {
                stockService.increaseInventory(sr.getProduct(), sr.getFromStore(), srd.getDeliveredLocation(), srd.getDeliveredRow(), srd.getDeliveredRack(), srd.getDeliveredBin(), srd.getBatchName(), srd.getDeliveredSerialNames(), srd.getDeliveredQuantity());
                stockMovementService.stockMovementInERP(true, sr.getProduct(), sr.getFromStore(), srd.getDeliveredLocation(), srd.getDeliveredRow(), srd.getDeliveredRack(), srd.getDeliveredBin(), srd.getBatchName(), srd.getDeliveredSerialNames(), srd.getDeliveredQuantity(), false);
                totalQuantity += srd.getDeliveredQuantity();
                StockMovementDetail smd = new StockMovementDetail(sm, srd.getDeliveredLocation(), srd.getDeliveredRow(), srd.getDeliveredRack(), srd.getDeliveredBin(), srd.getBatchName(), srd.getDeliveredSerialNames(), srd.getDeliveredQuantity());
                smdSet.add(smd);
            }
        }
        sm.setQuantity(totalQuantity);
        sm.setStockMovementDetails(smdSet);
        stockMovementService.addStockMovement(sm);

        stockService.updateERPInventory(true, sr.getBusinessDate(), sr.getProduct(), sr.getPackaging(), sr.getProduct().getUnitOfMeasure(), totalQuantity, "stock collected");
    }

    @Override
    public StockRequestDetail getStockRequestDetail(String detailId) throws ServiceException {
        return stockRequestDAO.getStockRequestDetail(detailId);
    }

    @Override
    public Map<String, Double> getTotalOrderedQuantityForProductStore(Company company) throws ServiceException {
        return stockRequestDAO.getTotalOrderedQuantityForProductStore(company);
    }

    @Override
    public List<StockRequest> getStorewisePendingStockRequestList(Map <String,Object> requestParams,Company company, Set<Store> storeSet, Date fromDate, Date toDate, String searchString, Paging paging, boolean isStorewiseStoreOrderList) throws ServiceException {
        RequestStatus[] statusList = new RequestStatus[]{RequestStatus.ORDERED, RequestStatus.ISSUED, RequestStatus.PENDING_APPROVAL, RequestStatus.RETURN_REQUEST};
        return stockRequestDAO.getStorewisePendingStockRequestList(requestParams,company, storeSet, statusList, fromDate, toDate, searchString, paging, isStorewiseStoreOrderList);
    }

    @Override
    public void approveSRReturnRequest(User approver, StockRequest sr) throws ServiceException {
        try {
            if (sr == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Stock Return Request is null");
            }
            sr.setCollectedBy(approver);
            sr.setStatus(RequestStatus.RETURNED);
            sr.setCollectedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long collectedDate = System.currentTimeMillis();
            sr.setCollecteddate(collectedDate);
            sr.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long modifiedDate = System.currentTimeMillis();
            sr.setModifieddate(modifiedDate);
            stockRequestDAO.saveOrUpdate(sr);
        } catch (ParseException ex) {
            Logger.getLogger(StockRequestServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockRequestServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createReturnRequest(StockRequest sr) throws ServiceException {
        if (sr == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Stock return Request is null");
        }
        if (sr.getStockRequestDetails().isEmpty()) {
            throw new InventoryException(InventoryException.Type.NULL, "Stock return request detail is empty");
        }
        StockRequest returnSR = new StockRequest(sr.getProduct(), sr.getToStore(), sr.getFromStore(), sr.getUom(), 0);
        returnSR.setBusinessDate(sr.getBusinessDate());
        returnSR.setCompany(sr.getCompany());
        returnSR.setCostCenter(sr.getCostCenter());
        returnSR.setIssuedBy(sr.getCollectedBy());
        returnSR.setIssuedOn(sr.getModifiedOn());
        returnSR.setIssueddate(sr.getModifieddate());
        returnSR.setModifiedOn(sr.getModifiedOn());
        returnSR.setModifieddate(sr.getModifieddate());
        returnSR.setRequestedBy(sr.getCollectedBy());
        returnSR.setRequestedOn(sr.getModifiedOn());
        returnSR.setPackaging(sr.getPackaging());
        returnSR.setRemark("Stock Returned");
        returnSR.setStatus(RequestStatus.RETURN_REQUEST);
        returnSR.setReturnRequest(true);
        returnSR.setModule(TransactionModule.STOCK_REQUEST);
        returnSR.setParentID(sr.getId()); //ERP-39060 creating parent ID for handling valuation case on SR/ ReturnSR
        returnSR.setTransactionNo("R" + sr.getTransactionNo());
        Set<StockRequestDetail> returnDetailSet = new HashSet<StockRequestDetail>();
        for (StockRequestDetail srd : sr.getStockRequestDetails()) {
            if (srd.getReturnQuantity() > 0) {
                StockRequestDetail returnDetail = new StockRequestDetail();
                returnDetail.setBatchName(srd.getBatchName());
                returnDetail.setDeliveredLocation(srd.getIssuedLocation());
                returnDetail.setIssuedLocation(locationService.getDefaultLocation(sr.getCompany()));
                returnDetail.setIssuedRow(srd.getIssuedRow());
                returnDetail.setDeliveredRow(srd.getIssuedRow());
                returnDetail.setIssuedRack(srd.getIssuedRack());
                returnDetail.setDeliveredRack(srd.getIssuedRack());
                returnDetail.setIssuedBin(srd.getIssuedBin());
                returnDetail.setDeliveredBin(srd.getIssuedBin());
                returnDetail.setIssuedSerialNames(srd.getReturnSerialNames());  // check r
                returnDetail.setDeliveredSerialNames(srd.getReturnSerialNames());  // check r,serials are not displayed in material in/out report
                returnDetail.setStockRequest(returnSR);
                returnDetail.setIssuedQuantity(srd.getReturnQuantity());
                returnDetail.setDeliveredQuantity(srd.getReturnQuantity());
                returnDetailSet.add(returnDetail);
            }
        }
        returnSR.setStockRequestDetails(returnDetailSet);
        returnSR.setOrderedQty(sr.getOrderedQty());
        returnSR.setIssuedQty(sr.getIssuedQty() - sr.getDeliveredQty());
        returnSR.setDeliveredQty(sr.getIssuedQty() - sr.getDeliveredQty());

        stockRequestDAO.saveOrUpdate(returnSR);

    }

    @Override
    public void acceptReturnStockRequest(User approver, StockRequest returnSR) throws ServiceException {
        acceptReturnStockRequest(approver, returnSR, null);
    }
    @Override
    public void acceptReturnStockRequest(User approver, StockRequest returnSR, String stockMovementRemark) throws ServiceException {
        try {
            returnSR.setCollectedBy(approver);
            returnSR.setCollectedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long collectedDate = System.currentTimeMillis();
            returnSR.setCollecteddate(collectedDate);
            returnSR.setStatus(RequestStatus.RETURNED);
            stockRequestDAO.saveOrUpdate(returnSR);
            collectSRStockBuffer(returnSR, stockMovementRemark);
        } catch (ParseException ex) {
            Logger.getLogger(StockRequestServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockRequestServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void sendReturnStockRequestForQA(User approver, StockRequest returnSR, String[] serialNames) throws ServiceException {
        if (returnSR == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Stock return request is null");
        }

        
        approvalService.addStockReturnApproval(returnSR, serialNames);
        for(StockRequestDetail srd:returnSR.getStockRequestDetails()){
            srd.setDeliveredQuantity(0);
            srd.setDeliveredSerialNames("");
        }
        returnSR.setDeliveredQty(0);
        returnSR.setStatus(RequestStatus.RETURN_APPROVAL);
        stockRequestDAO.saveOrUpdate(returnSR);
    }

    @Override
    public void deleteStockRequest(StockRequest stockRequest) throws ServiceException {
        if (stockRequest == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Stock return request is null");
        }
        if (stockRequest.getStatus() != RequestStatus.ORDERED) {
            throw new InventoryException(InventoryException.Type.INVALID, "Stock Request status must be Ordered");
        }
        stockRequest.setStatus(RequestStatus.DELETED);
        stockRequestDAO.saveOrUpdate(stockRequest);
    }

    @Override
    public void cancelStockRequest(User user, StockRequest sr) throws ServiceException {
        if (sr == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Stock Request is null");
        }
        Set<RequestStatus> processedStatus = new HashSet<RequestStatus>();
        processedStatus.add(RequestStatus.COLLECTED);
        processedStatus.add(RequestStatus.DELETED);
        processedStatus.add(RequestStatus.REJECTED);
        if (processedStatus.contains(sr.getStatus())) {
            throw new InventoryException(InventoryException.Type.INVALID, "Stock Request is already processed");
        }
        if (sr.getStatus() == RequestStatus.ISSUED) {
            sr.setApprovedBy(user);
            sr.setModifiedOn(new Date());
            long modifiedDate = System.currentTimeMillis();
            sr.setModifieddate(modifiedDate);
            returnFromSRStockBuffer(sr);
            sr.setStatus(RequestStatus.DELETED);
        } else if (sr.getStatus() == RequestStatus.RETURN_REQUEST) {
            acceptReturnStockRequest(user, sr);
        } else if (sr.getStatus() == RequestStatus.ORDERED) {
            sr.setStatus(RequestStatus.DELETED);
        }
        stockRequestDAO.saveOrUpdate(sr);

    }

    @Override
    public StockRequestDetail getIssuedSrDetailsforSerial(Product product, String batchName, String serialName) throws ServiceException {
        StockRequestDetail sr = null;
        if (product.isIsSerialForProduct() && !StringUtil.isNullOrEmpty(serialName)) {
            List<StockRequestDetail> list = stockRequestDAO.getSrDetailsForSerialByStatus(RequestStatus.ISSUED, product, batchName, serialName);
            for (StockRequestDetail srd : list) {
                List<String> serials = Arrays.asList(srd.getIssuedSerialNames().split(","));
                if (serials.contains(serialName)) {
                    sr = srd;
                    break;
                }
            }
        }
        return sr;
    }

    @Override
    public StockRequest getReturnSrforSerial(Product product, String batchName, String serialName) throws ServiceException {
        StockRequest sr = null;
        if (product.isIsSerialForProduct() && !StringUtil.isNullOrEmpty(serialName)) {
            List<StockRequestDetail> list = stockRequestDAO.getSrDetailsForSerialByStatus(RequestStatus.RETURN_REQUEST, product, batchName, serialName);
            for (StockRequestDetail srd : list) {
                List<String> serials = Arrays.asList(srd.getIssuedSerialNames().split(","));
                if (serials.contains(serialName)) {
                    sr = srd.getStockRequest();
                    break;
                }
            }
        }
        return sr;
    }
    /**
     *
     * @param params
     * @return = Return all data field for Stock Request record
     * @throws ServiceException
     */
    public JSONObject getSingleStockRequestToLoad(JSONObject params) throws ServiceException {
        JSONObject jSONObject = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        List list = stockRequestDAO.getStockRequestTemplate(params);
        try {
            for (Iterator it = list.iterator(); it.hasNext();) {
                String stockRequestId = it.next().toString();
                StockRequest sr = stockRequestDAO.getStockRequestById(stockRequestId);
                if (sr != null) {
                    JSONObject jObj = new JSONObject();
                    JSONArray stockDetails = new JSONArray();
                    jObj.put("id", sr.getId());
                    jObj.put("productid", sr.getProduct().getID());
                    jObj.put("pid", sr.getProduct().getName());
                    jObj.put("productname", sr.getProduct().getName());
                    jObj.put("uomname", sr.getUom().getNameEmptyforNA());
                    jObj.put("name", sr.getUom().getNameEmptyforNA());
                    jObj.put("transfernoteno", sr.getTransactionNo());
                    jObj.put("itemId", sr.getProduct().getID());
                    jObj.put("itemcode", sr.getProduct().getProductid());
                    jObj.put("itemname", sr.getProduct().getName());
                    jObj.put("itemdescription", sr.getProduct().getDescription());
                    jObj.put("fromstore", sr.getFromStore() != null ? sr.getFromStore().getId() : "");
                    jObj.put("fromStoreCode", sr.getFromStore() != null ? sr.getFromStore().getAbbreviation() : "");
                    jObj.put("fromstorename", sr.getFromStore() != null ? sr.getFromStore().getDescription() : "");
                    jObj.put("fromstoreadd", sr.getFromStore() != null ? sr.getFromStore().getAddress() : "");
                    jObj.put("fromstorefax", sr.getFromStore() != null ? sr.getFromStore().getFaxNo() : "");
                    jObj.put("fromstorephno", sr.getFromStore() != null ? sr.getFromStore().getContactNo() : "");
                    jObj.put("tostore", sr.getToStore() != null ? sr.getToStore().getId() : "");
                    jObj.put("toStoreCode", sr.getToStore() != null ? sr.getToStore().getAbbreviation() : "");
                    jObj.put("tostorename", sr.getToStore() != null ? sr.getToStore().getDescription() : "");
                    jObj.put("tostoreadd", sr.getToStore() != null ? sr.getToStore().getAddress() : "");
                    jObj.put("tostorefax", sr.getToStore() != null ? sr.getToStore().getFaxNo() : "");
                    jObj.put("tostorephno", sr.getToStore() != null ? sr.getToStore().getContactNo() : "");
                    jObj.put("costcenter", (sr.getCostCenter() == null) ? "" : sr.getCostCenter().getCcid());
                    jObj.put("packaging", (sr.getPackaging() == null) ? "" : sr.getPackaging().toString());
                    jObj.put("uomid", sr.getUom() != null ? sr.getUom().getID() : "");
                    jObj.put("name", sr.getUom() != null ? sr.getUom().getNameEmptyforNA() : "");
                    jObj.put("orderinguomname", sr.getProduct().getOrderingUOM() == null ? "" : sr.getProduct().getOrderingUOM().getNameEmptyforNA());
                    jObj.put("transferinguomname", sr.getProduct().getTransferUOM() == null ? "" : sr.getProduct().getTransferUOM().getNameEmptyforNA());
                    jObj.put("stockuomname", sr.getProduct().getUnitOfMeasure() == null ? "" : sr.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                    jObj.put("statusId", sr.getStatus().ordinal());
                    jObj.put("status", sr.getStatus().toString());
                    jObj.put("remark", sr.getRemark());
                    jObj.put("returnReason", sr.getReturnReason());
                    jObj.put("projectnumber", sr.getProjectNumber());
                    jObj.put("quantity", (sr.getIssuedOn() != null && sr.getCollectedOn() != null) ? (sr.getIssuedOn().equals(sr.getCollectedOn()) ? "N.A." : sr.getOrderedQty()) : sr.getOrderedQty());
//                    jObj.put("nwquantity", "1".equals(type) && sr.getStatus() != RequestStatus.ISSUED ? sr.getOrderedQty() : sr.getIssuedQty());
                    //jObj.put("delquantity", "3".equals(type) && sr.getStatus() != RequestStatus.COLLECTED ? sr.getIssuedQty() : sr.getDeliveredQty());
                    jObj.put("isBatchForProduct", sr.getProduct() != null ? sr.getProduct().isIsBatchForProduct() : "");
                    jObj.put("isSerialForProduct", sr.getProduct() != null ? sr.getProduct().isIsSerialForProduct() : "");
                    jObj.put("hscode", sr.getProduct() != null ? sr.getProduct().getHSCode() : "");
                    jObj.put("itemdefaultwarehouse", sr.getProduct().getWarehouse() != null ? sr.getProduct().getWarehouse().getId() : "");

                    //At the time of generating PDF, business date is to be shown.
                    if (sr.getBusinessDate() != null) {
                        jObj.put("date", df.format(sr.getBusinessDate()));
                    } else if (sr.getRequestedOn() != null) {
                        jObj.put("date", df.format(sr.getRequestedOn()));
                    }
                    if (sr.getIssuedOn() != null) {
                        jObj.put("issuedOn", df1.format(sr.getIssuedOn()));
                    }
                    if (sr.getCollectedOn() != null) {
                        jObj.put("collectedOn", df1.format(sr.getCollectedOn()));
                    }
                    if (sr.getModifiedOn() != null) {
                        jObj.put("modifiedOn", df.format(sr.getModifiedOn()));
                    }

                    if (sr.getRequestedBy() != null) {
                        jObj.put("createdby", sr.getRequestedBy().getFullName());
                    }
                    if (sr.getIssuedBy() != null) {
                        jObj.put("issuedBy", sr.getIssuedBy().getFullName());
                    }
                    if (sr.getCollectedBy() != null) {
                        jObj.put("collectedBy", sr.getCollectedBy().getFullName());
                    }
                    if (sr.getApprovedBy() != null) {
                        jObj.put("approvedBy", sr.getApprovedBy().getFullName());
                    }

                    if (sr != null) {
                        for (StockRequestDetail srd : sr.getStockRequestDetails()) {
                            if (srd != null) {
                                JSONObject srObject = new JSONObject();
                                srObject.put("id", srd.getId());
                                srObject.put("issuedLocationName", (srd.getIssuedLocation() != null) ? srd.getIssuedLocation().getName() : "");
                                srObject.put("issuedRowName", (srd.getIssuedRow() != null) ? srd.getIssuedRow().getName() : "");
                                srObject.put("issuedRackName", (srd.getIssuedRack() != null) ? srd.getIssuedRack().getName() : "");
                                srObject.put("issuedBinName", (srd.getIssuedBin() != null) ? srd.getIssuedBin().getName() : "");
                                srObject.put("issuedQuantity", srd.getIssuedQuantity());
                                srObject.put("issuedSerials", (srd.getIssuedSerialNames() != null) ? srd.getIssuedSerialNames().replace(",", ", ") : "");
                                srObject.put("batchName", (srd.getBatchName() != null) ? srd.getBatchName() : "");
                                srObject.put("collectedLocationName", (srd.getDeliveredLocation() != null) ? srd.getDeliveredLocation().getName() : "");
                                srObject.put("collectedRowName", (srd.getDeliveredRow() != null) ? srd.getDeliveredRow().getName() : "");
                                srObject.put("collectedRackName", (srd.getDeliveredRack() != null) ? srd.getDeliveredRack().getName() : "");
                                srObject.put("collectedBinName", (srd.getDeliveredBin() != null) ? srd.getDeliveredBin().getName() : "");
                                srObject.put("collectedQuantity", srd.getDeliveredQuantity());
                                srObject.put("collectedSerials", (srd.getDeliveredSerialNames() != null) ? srd.getDeliveredSerialNames().replace(",", ", ") : "");
                                stockDetails.put(srObject);
                            }

                        }
                    }
                    jObj.put("stockDetails", stockDetails);
                    /*
                     calculate available qty
                     */
                    double availableQty = stockService.getProductQuantityInStore(sr.getProduct(), sr.getToStore());
                    jObj.put("avaquantity", availableQty);
                    jSONArray.put(jObj);

                }
            }
            jSONObject.put("loaddata", jSONArray);
        } catch (JSONException ex) {
            Logger.getLogger(StockRequestServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jSONObject;
    }
    public JSONObject deleteStockIssueDetail(String id) throws ServiceException {
        JSONObject json = new JSONObject();
        try {

            StockRequest sr = stockRequestDAO.getStockRequestById(id);

            boolean isValidForDelete = checkValidation(sr);
            if (isValidForDelete) {
                for (StockRequestDetail dtl : sr.getStockRequestDetails()) {
                    stockMovementService.stockMovementInERP(true, sr.getProduct(), sr.getFromStore(), dtl.getIssuedLocation(), dtl.getIssuedRow(),
                            dtl.getIssuedRack(), dtl.getIssuedBin(), dtl.getBatchName(), dtl.getIssuedSerialNames(), dtl.getIssuedQuantity(), false);

                    stockService.increaseInventory(sr.getProduct(), sr.getFromStore(), dtl.getIssuedLocation(), dtl.getIssuedRow(), dtl.getIssuedRack(),
                            dtl.getIssuedBin(), dtl.getBatchName(), dtl.getIssuedSerialNames(), dtl.getIssuedQuantity());

                    stockMovementService.stockMovementInERP(false, sr.getProduct(), sr.getToStore(), dtl.getDeliveredLocation(), dtl.getDeliveredRow(),
                            dtl.getDeliveredRack(), dtl.getDeliveredBin(), dtl.getBatchName(), dtl.getDeliveredSerialNames(), dtl.getDeliveredQuantity(), false);

                    stockService.decreaseInventory(sr.getProduct(), sr.getToStore(), dtl.getDeliveredLocation(), dtl.getDeliveredRow(),
                            dtl.getDeliveredRack(), dtl.getDeliveredBin(), dtl.getBatchName(), dtl.getDeliveredSerialNames(), dtl.getDeliveredQuantity());
                }

                List<StockMovement> smList = stockMovementService.getStockMovementListByReferenceId(sr.getCompany(), sr.getId());
                for (StockMovement sm : smList) {
                    stockRequestDAO.delete(sm);
                }
                stockRequestDAO.delete(sr);

                json.put("msg", "Stock Issue record is deleted successfully.");
                json.put("success", true);
            } else {
                String msg = "Quantity is not available for Transaction : " + sr.getTransactionNo();
                json.put("msg", msg);
                json.put("success", false);
            }
        } catch (JSONException ex) {
            try {
                json.put("msg", ex.getMessage());
                json.put("success", false);
            } catch (JSONException ex1) {
                Logger.getLogger(StockRequestServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return json;
    }

    private boolean checkValidation(StockRequest sa) {

        boolean isValid = false;

        try {
            if (sa != null) {
                Product product = sa.getProduct();
                Store store = sa.getToStore();
                if (sa.getStockRequestDetails() != null) {
                    for (StockRequestDetail sadtl : sa.getStockRequestDetails()) {
                        Location location = sadtl.getDeliveredLocation();
                        if (product.isIsBatchForProduct() || product.isIsSerialForProduct() || product.isIsBatchForProduct() || product.isIslocationforproduct()) {
                            NewProductBatch productBatch = stockService.getERPProductBatch(product, store, location, sadtl.getDeliveredRow(), sadtl.getDeliveredRack(), sadtl.getDeliveredBin(), sadtl.getBatchName());
                            if (productBatch != null) {
                                if (productBatch.getQuantitydue() >= sadtl.getDeliveredQuantity() && !product.isIsSerialForProduct()) {
                                    isValid = true;
                                } else if (product.isIsSerialForProduct() && productBatch.getQuantitydue() >= sadtl.getDeliveredQuantity()) {

                                    String[] serArr = sadtl.getDeliveredSerialNames().split(",");
                                    if (serArr.length > 0) {
                                        List<String> srList = Arrays.asList(serArr);
                                        List<StockMovement> smList = stockMovementDAO.getStockMovementByProduct(sa.getCompany(), product, store, sa.getBusinessDate(), sadtl.getDeliveredSerialNames(), sa.getId());
                                        if (smList.size() > 0) {
                                            for (StockMovement sm : smList) {
                                                for (StockMovementDetail dtl : sm.getStockMovementDetails()) {
                                                    String[] smserArr = dtl.getSerialNames().split(",");
                                                    List<String> l3 = Arrays.asList(smserArr);
                                                    if (!Collections.disjoint(l3, srList)) {
                                                        throw new InventoryException(InventoryException.Type.NULL, "Transactions are found after Stock Issue.");
                                                    }
                                                }
                                            }
                                        }

                                        List<NewBatchSerial> listSeril = stockService.getERPActiveSerialList(product, productBatch, false);
                                        if (listSeril.size() > 0) {
                                            for (NewBatchSerial sr : listSeril) {
                                                if (!srList.contains(sr.getSerialname())) {
                                                    throw new InventoryException(InventoryException.Type.NULL, "Quantity is not available." + sa.getTransactionNo());
                                                } else {
                                                    /**
                                                     * If serial number is not
                                                     * used then set avlQty to
                                                     * true(ERP-31476).
                                                     */
                                                    isValid = true;
                                                }
                                            }
                                        }

                                    }
                                }
                            } else {
                                throw new InventoryException(InventoryException.Type.NULL, "Quantity is not available." + sa.getTransactionNo());
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new InventoryException(InventoryException.Type.NULL, ex.getMessage());
        }
        return isValid;

    }
}
