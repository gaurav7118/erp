/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval.stocktransfer.impl;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.SequenceFormat;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.approval.ApprovalType;
import com.krawler.inventory.model.approval.InspectionDetail;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferApproval;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferApprovalDAO;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferApprovalService;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferDetailApproval;
import com.krawler.inventory.model.ist.GRODetailISTMapping;
import com.krawler.inventory.model.ist.ISTDetail;
import com.krawler.inventory.model.ist.InterStoreTransferRequest;
import com.krawler.inventory.model.ist.InterStoreTransferService;
import com.krawler.inventory.model.ist.InterStoreTransferStatus;
import com.krawler.inventory.model.ist.RepairGRODetailISTMapping;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.sequence.SeqFormat;
import com.krawler.inventory.model.sequence.SeqService;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.*;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.inventory.model.stockout.StockAdjustmentDetail;
import com.krawler.inventory.model.stockrequest.RequestStatus;
import com.krawler.inventory.model.stockrequest.StockRequest;
import com.krawler.inventory.model.stockrequest.StockRequestDetail;
import com.krawler.inventory.model.stockrequest.StockRequestService;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.krawler.inventory.model.stockout.StockAdjustmentService;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.inventory.model.sequence.ModuleConst;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.inventory.model.approval.sa.SADetailApproval;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import org.springframework.context.MessageSource;
/**
 *
 * @author Vipin Gupta
 */
public class StockTransferApprovalServiceImpl implements StockTransferApprovalService {

    private StockTransferApprovalDAO stockTransferApprovalDAO;
    private InterStoreTransferService istService;
//    private StockRequestService srService;
    private StockService stockService;
    private StockMovementService stockMovementService;
    private StockRequestService stockRequestService;
    private SeqService seqService;
    private AccountingHandlerDAO accountingHandlerDAO;
    private StoreService storeService;
    private MessageSource messageSource;
    private accCompanyPreferencesDAO accCompanyPreferencesDAO;
    private accJournalEntryDAO accJournalEntryDAO;    
    private StockAdjustmentService stockAdjustmentService;
    private auditTrailDAO auditTrailObj;

    public void setStockTransferApprovalDAO(StockTransferApprovalDAO stockTransferApprovalDAO) {
        this.stockTransferApprovalDAO = stockTransferApprovalDAO;
    }

    public void setIstService(InterStoreTransferService istService) {
        this.istService = istService;
    }

//    public void setSrService(StockRequestService srService) {
//        this.srService = srService;
//    }

    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    @Override
    public List<StockTransferApproval> getStockTransferApprovalList(TransactionModule transactionModule, String searchString, Paging paging) throws ServiceException {
        return stockTransferApprovalDAO.getStockTransferApprovalList(transactionModule, searchString, paging);
    }

    @Override
    public List<StockTransferDetailApproval> getStockTransferDetailApprovalList(StockTransferApproval stockTransferApproval, Paging paging) throws ServiceException {
        return stockTransferApprovalDAO.getStockTransferDetailApprovalList(stockTransferApproval, paging);
    }

    public StockRequestService getStockRequestService() {
        return stockRequestService;
    }

    public void setStockRequestService(StockRequestService stockRequestService) {
        this.stockRequestService = stockRequestService;
    }
      
    public void setSeqService(SeqService seqService) {
        this.seqService = seqService;
    }
    
    public void setAccJournalEntryDAO(accJournalEntryDAO accJournalEntryDAO) {
        this.accJournalEntryDAO = accJournalEntryDAO;
    }

    public void setAccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesDAO) {
        this.accCompanyPreferencesDAO = accCompanyPreferencesDAO;
    }
        
    public void setStockAdjustmentService(StockAdjustmentService stockAdjustmentService) {
        this.stockAdjustmentService = stockAdjustmentService;
    }   
    
    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }
    
    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }    
    
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }

    @Override
    public void addStockReturnApproval(InterStoreTransferRequest istRequest, String[] approvalSerialNames) throws ServiceException {
        StockTransferApproval sta = new StockTransferApproval();
        sta.setApprovalStatus(ApprovalStatus.PENDING);
        sta.setApprovalType(ApprovalType.STOCK_RETURN_APPROVAL);
        sta.setStockTransferId(istRequest.getId());
        sta.setTransactionModule(TransactionModule.INTER_STORE_TRANSFER);

        Set<String> approvalSerialNameSet = new HashSet<>();
        if (approvalSerialNames != null) {
            approvalSerialNameSet = new HashSet<>(Arrays.asList(approvalSerialNames));
        }
        Store qaStore = stockService.getQaStore(istRequest.getCompany());
        Location qaLocation = null;
        if(qaStore != null){
            qaLocation = qaStore.getDefaultLocation();
        }
        if(qaStore == null || qaLocation == null){
            throw new InventoryException("QA Store or Default location for QA store is not set.");
        }
        Product product = istRequest.getProduct();
        double purchasePrice = stockService.getProductPurchasePrice(istRequest.getProduct(), istRequest.getBusinessDate());
        
        StockMovement sm = new StockMovement(product, istRequest.getToStore(), 0, purchasePrice, istRequest.getTransactionNo(), istRequest.getBusinessDate(), TransactionType.IN, TransactionModule.INTER_STORE_TRANSFER, istRequest.getId(), istRequest.getId());
        sm.setRemark("Returned Stock not sent for QA Approval");
        sm.setStockUoM(product.getUnitOfMeasure());
        
        StockMovement smQA = new StockMovement(product, qaStore, 0, purchasePrice, istRequest.getTransactionNo(), istRequest.getBusinessDate(), TransactionType.IN, TransactionModule.INTER_STORE_TRANSFER, istRequest.getId(), istRequest.getId());
        smQA.setRemark("Returned Stock sent for QA Approval");
        smQA.setStockUoM(product.getUnitOfMeasure());
        
        Map<String, StockMovementDetail> smdMap = new HashMap<>();
        Map<String, StockMovementDetail> smdQAMap = new HashMap<>();
        
        for (ISTDetail istd : istRequest.getIstDetails()) {
            Location location = istd.getDeliveredLocation();
            StoreMaster row = istd.getDeliveredRow();
            StoreMaster rack = istd.getDeliveredRack();
            StoreMaster bin = istd.getDeliveredBin();
            String batchName = istd.getBatchName();
            double qty = istd.getDeliveredQuantity();
            String serialNames = istd.getDeliveredSerialNames();
            if (!StringUtil.isNullOrEmpty(serialNames)) {
                String[] serialNameArr = serialNames.split(",");
                String key = location.getId()
                        + (product.isIsrowforproduct() ? row.getId() : "")
                        + (product.isIsrackforproduct() ? rack.getId() : "")
                        + (product.isIsbinforproduct() ? bin.getId() : "")
                        + batchName;
                for (int i = 0; i < qty; i++) {
                    if (approvalSerialNameSet.contains(serialNameArr[i])) { // selected serials for approval
                        StockTransferDetailApproval stda = new StockTransferDetailApproval();
                        stda.setStockTransferDetailId(istd.getId());
                        stda.setApprovalStatus(ApprovalStatus.PENDING);
                        stda.setQuantity(1);
                        stda.setStockTransferApproval(sta);
                        stda.setSerialName(serialNameArr[i]);

                        sta.getStockTransferDetailApprovals().add(stda);
                        sta.setQuantity(sta.getQuantity() + 1);
                        
                        if (smdQAMap.containsKey(key)) {
                            StockMovementDetail smd = smdQAMap.get(key);
                            smd.setQuantity(smd.getQuantity() + 1);
                            smd.addSerialName(serialNameArr[i]);
                        } else {
                            StockMovementDetail smd = new StockMovementDetail(smQA, qaLocation, row, rack, bin, batchName, serialNameArr[i], 1);
                            smdQAMap.put(key, smd);
                            smQA.getStockMovementDetails().add(smd);
                        }
                        smQA.setQuantity(smQA.getQuantity() + 1);

                    } else {
                        if (smdMap.containsKey(key)) {
                            StockMovementDetail smd = smdMap.get(key);
                            smd.setQuantity(smd.getQuantity() + 1);
                            smd.addSerialName(serialNameArr[i]);
                        } else {
                            StockMovementDetail smd = new StockMovementDetail(sm, location, row, rack, bin, batchName, serialNameArr[i], 1);
                            smdMap.put(key, smd);
                            sm.getStockMovementDetails().add(smd);
                        }
                        sm.setQuantity(sm.getQuantity() + 1);
                    }
                }
            } else {
                StockTransferDetailApproval stda = new StockTransferDetailApproval();
                stda.setStockTransferDetailId(istd.getId());
                stda.setApprovalStatus(ApprovalStatus.PENDING);
                stda.setQuantity(qty);
                stda.setStockTransferApproval(sta);
                sta.getStockTransferDetailApprovals().add(stda);
                sta.setQuantity(sta.getQuantity() + qty);
                
                StockMovementDetail smd = new StockMovementDetail(smQA, qaLocation, row, rack, bin, batchName, null, qty);
                smQA.getStockMovementDetails().add(smd);
                smQA.setQuantity(smQA.getQuantity() + qty);
            }
        }
        stockTransferApprovalDAO.saveOrUpdate(sta);

        if (!sm.getStockMovementDetails().isEmpty()) {
            for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                stockService.increaseInventory(istRequest.getProduct(), istRequest.getToStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                stockService.updateERPInventory(true, istRequest.getBusinessDate(), istRequest.getProduct(), null, istRequest.getUom(), smd.getQuantity(), "QA not applicable");
                stockMovementService.stockMovementInERP(true, istRequest.getProduct(), istRequest.getToStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), true);
            }
            stockMovementService.addStockMovement(sm);
        }
        if (!smQA.getStockMovementDetails().isEmpty()) {
            for (StockMovementDetail smd : smQA.getStockMovementDetails()) {
                stockService.increaseInventory(smQA.getProduct(), smQA.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                stockMovementService.stockMovementInERP(true, smQA.getProduct(), smQA.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), true);
            }
            stockMovementService.addStockMovement(smQA);
        }                

    }

    @Override
    public void addStockReturnApproval(StockRequest sr, String[] approvalSerialNames) throws ServiceException {
        StockTransferApproval sta = new StockTransferApproval();
        sta.setApprovalStatus(ApprovalStatus.PENDING);
        sta.setApprovalType(ApprovalType.STOCK_RETURN_APPROVAL);
        sta.setStockTransferId(sr.getId());
        sta.setTransactionModule(TransactionModule.STOCK_REQUEST);
        Set<String> approvalSerialNameSet = new HashSet<String>();
        if (approvalSerialNames != null) {
            approvalSerialNameSet = new HashSet<String>(Arrays.asList(approvalSerialNames));
        }
        Store qaStore = stockService.getQaStore(sr.getCompany());
        Location qaLocation = null;
        if(qaStore != null){
            qaLocation = qaStore.getDefaultLocation();
        }
        if(qaStore == null || qaLocation == null){
            throw new InventoryException("QA Store or Default location for QA store is not set.");
        }
        Product product = sr.getProduct();
        double purchasePrice = stockService.getProductPurchasePrice(sr.getProduct(), sr.getBusinessDate());
        // here we are using from store because in stock request stock collected in fromstore and issued from tostore so returned in tostore and in return request we interchage store.
        StockMovement sm = new StockMovement(product, sr.getFromStore(), 0, purchasePrice, sr.getTransactionNo(), sr.getBusinessDate(), TransactionType.IN, TransactionModule.STOCK_REQUEST, sr.getId(), sr.getId());
        sm.setRemark("Returned Stock not sent for QA Approval");
        sm.setStockUoM(sr.getProduct().getUnitOfMeasure());
        
        StockMovement smQA = new StockMovement(product, qaStore, 0, purchasePrice, sr.getTransactionNo(), sr.getBusinessDate(), TransactionType.IN, TransactionModule.STOCK_REQUEST, sr.getId(), sr.getId());
        smQA.setRemark("Returned Stock sent for QA Approval");
        smQA.setStockUoM(product.getUnitOfMeasure());

        Map<String, StockMovementDetail> smdMap = new HashMap<>();
        Map<String, StockMovementDetail> smdQAMap = new HashMap<>();

        for (StockRequestDetail srd : sr.getStockRequestDetails()) {
            Location location = srd.getDeliveredLocation();
            StoreMaster row = srd.getDeliveredRow();
            StoreMaster rack = srd.getDeliveredRack();
            StoreMaster bin = srd.getDeliveredBin();
            String batchName = srd.getBatchName();
            double qty = srd.getDeliveredQuantity();
            String serialNames = srd.getDeliveredSerialNames();
            if (product.isIsSerialForProduct()) {
                String[] serialNameArr = serialNames.split(",");

                String key = location.getId()
                        + (product.isIsrowforproduct() ? row.getId() : "")
                        + (product.isIsrackforproduct() ? rack.getId() : "")
                        + (product.isIsbinforproduct() ? bin.getId() : "")
                        + batchName;
                for (int i = 0; i < qty; i++) {
                    if (approvalSerialNameSet.contains(serialNameArr[i])) { // selected serials for approval
                        StockTransferDetailApproval stda = new StockTransferDetailApproval();
                        stda.setStockTransferDetailId(srd.getId());
                        stda.setApprovalStatus(ApprovalStatus.PENDING);
                        stda.setQuantity(1);
                        stda.setStockTransferApproval(sta);
                        stda.setSerialName(serialNameArr[i]);
                        sta.getStockTransferDetailApprovals().add(stda);
                        sta.setQuantity(sta.getQuantity() + 1);

                        if (smdQAMap.containsKey(key)) {
                            StockMovementDetail smd = smdQAMap.get(key);
                            smd.setQuantity(smd.getQuantity() + 1);
                            smd.addSerialName(serialNameArr[i]);
                        } else {
                            StockMovementDetail smd = new StockMovementDetail(smQA, qaLocation, row, rack, bin, batchName, serialNameArr[i], 1);
                            smdQAMap.put(key, smd);
                            smQA.getStockMovementDetails().add(smd);
                        }
                        smQA.setQuantity(smQA.getQuantity() + 1);
                    } else {

                        if (smdMap.containsKey(key)) {
                            StockMovementDetail smd = smdMap.get(key);
                            smd.setQuantity(smd.getQuantity() + 1);
                            smd.addSerialName(serialNameArr[i]);
                        } else {
                            StockMovementDetail smd = new StockMovementDetail(sm, location, row, rack, bin, batchName, serialNameArr[i], 1);
                            smdMap.put(key, smd);
                            sm.getStockMovementDetails().add(smd);
                        }
                        sm.setQuantity(sm.getQuantity() + 1);
                    }
                }
            } else {
                StockTransferDetailApproval stda = new StockTransferDetailApproval();
                stda.setStockTransferDetailId(srd.getId());
                stda.setApprovalStatus(ApprovalStatus.PENDING);
                stda.setQuantity(qty);
                stda.setStockTransferApproval(sta);
                sta.getStockTransferDetailApprovals().add(stda);
                sta.setQuantity(sta.getQuantity() + qty);

                StockMovementDetail smd = new StockMovementDetail(smQA, qaLocation, row, rack, bin, batchName, null, qty);
                smQA.getStockMovementDetails().add(smd);
                smQA.setQuantity(smQA.getQuantity() + qty);
            }
        }
        stockTransferApprovalDAO.saveOrUpdate(sta);

        if (!sm.getStockMovementDetails().isEmpty()) {
            for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                stockService.increaseInventory(sr.getProduct(), sr.getFromStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                stockMovementService.stockMovementInERP(true, sr.getProduct(), sr.getFromStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), true);
            }
            stockService.updateERPInventory(true, sr.getBusinessDate(), sr.getProduct(), null, sr.getUom(), sm.getQuantity(), "QA not applicable");
            stockMovementService.addStockMovement(sm);
        }
        if (!smQA.getStockMovementDetails().isEmpty()) {
            for (StockMovementDetail smd : smQA.getStockMovementDetails()) {
                stockService.increaseInventory(smQA.getProduct(), smQA.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                stockMovementService.stockMovementInERP(true, smQA.getProduct(), smQA.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), true);
            }
            stockService.updateERPInventory(true, smQA.getTransactionDate(), smQA.getProduct(), null, smQA.getStockUoM(), smQA.getQuantity(), "Send for QA");
            stockMovementService.addStockMovement(smQA);
        }
    }

    @Override
    public void approveStockTransferDetail(User inspector, StockTransferDetailApproval stdApproval, InspectionDetail inspDTL, boolean fromRepair, double quantity) throws ServiceException {

        double retuQty = stdApproval.getQuantity() - quantity;
        if (retuQty > 0) {
            //create return request
            createReturnrequestForInterstore(stdApproval, retuQty, fromRepair);
        }
        if (quantity > 0) {
            try {
                stdApproval.setInspector(inspector);
                stdApproval.setInspectionDetail(inspDTL);
               
                if (!fromRepair) {
                    stdApproval.setQuantity(quantity);
                    stdApproval.setApprovalStatus(ApprovalStatus.APPROVED);
                    stdApproval.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                } else {
                    stdApproval.setRetQty(quantity);
                    stdApproval.setRepairStatus(ApprovalStatus.REPAIRDONE);
                    stdApproval.setRepairedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                }
                stockTransferApprovalDAO.saveOrUpdate(stdApproval);
                
                String srId=stdApproval.getStockTransferApproval().getStockTransferId();
                String srDetailsId=stdApproval.getStockTransferDetailId();
                
                StockRequest sr=stockRequestService.getStockRequestById(srId);
                StockRequestDetail srdtl=stockRequestService.getStockRequestDetail(srDetailsId);
                
                if(sr!=null){
                    sr.setDeliveredQty(sr.getDeliveredQty()+quantity);
                }
                if(srdtl!=null){
                    srdtl.setDeliveredQuantity(srdtl.getDeliveredQuantity()+quantity);
                    String serName=StringUtil.isNullOrEmpty(srdtl.getDeliveredSerialNames())?stdApproval.getSerialName():","+stdApproval.getSerialName();
                    srdtl.setDeliveredSerialNames(serName);
                }
                
            if (retuQty == 0) {
                reflectStockAfterFullApproval(inspector, stdApproval, fromRepair);
                }
            } catch (ParseException ex) {
                Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SessionExpiredException ex) {
                Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void rejectStockTransferDetail(User inspector, StockTransferDetailApproval stdApproval, InspectionDetail inspDTL, boolean fromRepair, double quantity) throws ServiceException {
        try {
        double retuQty = stdApproval.getQuantity() - quantity;
        if (retuQty > 0) {
            //create return request
            createReturnrequestForInterstore(stdApproval, retuQty, fromRepair);
        }
        if (!fromRepair) {
            stdApproval.setApprovalStatus(ApprovalStatus.REJECTED);
            stdApproval.setQuantity(quantity);
            stdApproval.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));

        } else {
            stdApproval.setRepairStatus(ApprovalStatus.REPAIRREJECT);
            stdApproval.setRetQty(quantity);
            stdApproval.setRepairedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
        }
        stdApproval.setInspectionDetail(inspDTL);
        stdApproval.setInspector(inspector);
            stdApproval.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            stockTransferApprovalDAO.saveOrUpdate(stdApproval);
            if (retuQty == 0) {
                reflectStockAfterFullApproval(inspector, stdApproval, fromRepair);

            }
        } catch (ParseException ex) {
            Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
//    @Override
//    public void rejectStockTransferDetail(User inspector, StockTransferDetailApproval stdApproval, InspectionDetail inspDTL, boolean fromRepair, double quantity) throws ServiceException {
//        double retuQty = stdApproval.getQuantity() - quantity;
//        if (retuQty > 0) {
//            //create return request
//            createReturnrequestForInterstore(stdApproval, retuQty, fromRepair);
//        }
//        if (!fromRepair) {
//            stdApproval.setApprovalStatus(ApprovalStatus.REJECTED);
//            stdApproval.setQuantity(quantity);
//
//        } else {
//            stdApproval.setRepairStatus(ApprovalStatus.REPAIRREJECT);
//            stdApproval.setRetQty(quantity);
//        }
//        stdApproval.setInspectionDetail(inspDTL);
//        stdApproval.setInspector(inspector);
//        stdApproval.setModifiedOn(new Date());
//        stockTransferApprovalDAO.saveOrUpdate(stdApproval);
//        if (retuQty == 0) {
//            reflectStockAfterFullApproval(inspector, stdApproval, fromRepair);
//        }
//    }

    public void createReturnrequestForInterstore(StockTransferDetailApproval stdApproval, double retQty, boolean fromRepair) throws ServiceException {
        if (stdApproval != null) {
        try {
            StockTransferDetailApproval stkTransDtl = new StockTransferDetailApproval();
            stkTransDtl.setInspector(stdApproval.getInspector());
            stkTransDtl.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            stkTransDtl.setQuantity(retQty);
            stkTransDtl.setStockTransferApproval(stdApproval.getStockTransferApproval());
            stkTransDtl.setStockTransferDetailId(stdApproval.getStockTransferDetailId());
            stkTransDtl.setRemark("QA return");
            if (fromRepair) {
                stkTransDtl.setRetQty(retQty);
                stkTransDtl.setApprovalStatus(ApprovalStatus.REJECTED);
                stkTransDtl.setRepairStatus(ApprovalStatus.RETURNTOREPAIR);
//                stkTransDtl.setRetQty(retQty);

            } else {
                stkTransDtl.setApprovalStatus(ApprovalStatus.PENDING);
            }
            stockTransferApprovalDAO.saveOrUpdate(stkTransDtl);
        } catch (ParseException ex) {
                Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SessionExpiredException ex) {
                Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public Map rejectedApprovedItemsDetail(StockTransferDetailApproval stda) throws ServiceException {
        Map map = null;
        if (stda != null) {
            map = new HashMap();
            TransactionModule module = stda.getStockTransferApproval().getTransactionModule();
            Store store = null;
            String batchName = "";
            Product product = null;
            if (module == TransactionModule.STOCK_REQUEST) {
                StockRequestDetail srd = stockRequestService.getStockRequestDetail(stda.getStockTransferDetailId());
                batchName = srd.getBatchName();
                store = srd.getStockRequest().getFromStore();
                product = srd.getStockRequest().getProduct();
            } else if (module == TransactionModule.INTER_STORE_TRANSFER) {
                ISTDetail istd = istService.getISTDetailById(stda.getStockTransferDetailId());
                batchName = istd.getBatchName();
                store = istd.getIstRequest().getToStore();
                product = istd.getIstRequest().getProduct();
            }

            String emailIds = "";
            boolean isfirst = true;
            Set<User> managerSet = new HashSet();
            if (store != null) {
                managerSet = store.getStoreManagerSet();
                Iterator itr = managerSet.iterator();
                while (itr.hasNext()) {
                    User user = (User) itr.next();
                    if (isfirst) {
                        emailIds += user.getEmailID();
                        isfirst = false;
                    } else {
                        emailIds += "," + user.getEmailID();
                    }
                }
            }
            map.put("batchName", batchName);
            map.put("serialNames", stda.getSerialName());
            map.put("quantity", stda.getQuantity());
            map.put("productId", (product != null ? product.getProductid() : ""));
            map.put("productName", (product != null ? product.getName() : ""));
            map.put("storeName", (store != null ? store.getFullName() : ""));
            map.put("storeManagerEmailIds", emailIds);
        }
        return map;
    }

    private void reflectStockAfterFullApproval(User inspector, StockTransferDetailApproval stdApproval, boolean fromRepair) throws ServiceException {
        StockTransferApproval sta = stdApproval.getStockTransferApproval();
        List<StockTransferDetailApproval> sadApprovalList = getStockTransferDetailApprovalList(sta, null);
        boolean approvalDone = true;
        boolean isCompleted = true;
        for (StockTransferDetailApproval sadApproval : sadApprovalList) {
            if (sadApproval.getApprovalStatus() == ApprovalStatus.PENDING) {
                approvalDone = false;
                break;
            }
        }
        if (approvalDone && !fromRepair) {
            sta.setApprovalStatus(ApprovalStatus.DONE);
            sta.setInspector(inspector);
            stockTransferApprovalDAO.saveOrUpdate(sta);
//            reflectStock(sta);
        }
        for (StockTransferDetailApproval sadApproval : sadApprovalList) {
            if (sadApproval.getApprovalStatus() == ApprovalStatus.PENDING || sadApproval.getRepairStatus() == ApprovalStatus.REPAIRPENDING || sadApproval.getRepairStatus() == ApprovalStatus.RETURNTOREPAIR) {
                isCompleted = false;
                break;
            }
        }
        if (isCompleted && fromRepair) {
            reflectStock(sta);
        }

    }

//    private void reflectStock(StockTransferApproval sta) throws ServiceException {
//
//        switch (sta.getTransactionModule()) {
//            case INTER_STORE_TRANSFER:
//                reflectISTStock(sta);
//                break;
//            case STOCK_REQUEST:
//                reflectSRStock(sta);
//                break;
//        }
//
//    }
    private void reflectStock(StockTransferApproval sta) throws ServiceException {

        switch (sta.getTransactionModule()) {
            case INTER_STORE_TRANSFER:
                InterStoreTransferRequest istr = istService.getInterStoreTransferById(sta.getStockTransferId());
                istr.setModifiedBy(sta.getInspector());
                istr.setModifiedOn(new Date());
                long modifiedDate = System.currentTimeMillis();
                istr.setModifieddate(modifiedDate);
                istr.setStatus(InterStoreTransferStatus.RETURN_ACCEPTED);
                stockTransferApprovalDAO.saveOrUpdate(istr);
                break;
            case STOCK_REQUEST:
                StockRequest sr = stockRequestService.getStockRequestById(sta.getStockTransferId());
                sr.setCollectedBy(sta.getInspector());
                sr.setStatus(RequestStatus.RETURNED);
                sr.setCollectedOn(new Date());
                long collectedDate = System.currentTimeMillis();
                sr.setCollecteddate(collectedDate);
                sr.setModifiedOn(new Date());
                modifiedDate = System.currentTimeMillis();
                sr.setModifieddate(modifiedDate);
                stockTransferApprovalDAO.saveOrUpdate(sr);
                break;
        }

    }

    // creating IN Entry for approval detail and IN entry for rejected details.
    private void reflectISTStock(StockTransferApproval sta) throws ServiceException {
        try {
            InterStoreTransferRequest istr = istService.getInterStoreTransferById(sta.getStockTransferId());

        Product product = istr.getProduct();
        double totalApprovedQty = 0;
        double totalRejectedQty = 0;
        double purchasePrice = stockService.getProductPurchasePrice(istr.getProduct(), istr.getBusinessDate());

        StockMovement smApproved = new StockMovement(product, istr.getToStore(), 0, purchasePrice, istr.getTransactionNo(), istr.getBusinessDate(), TransactionType.IN, TransactionModule.INTER_STORE_TRANSFER, istr.getId(), istr.getId());

        StockMovement smRejected = new StockMovement(product, istr.getToStore(), 0, purchasePrice, istr.getTransactionNo(), istr.getBusinessDate(), TransactionType.IN, TransactionModule.INTER_STORE_TRANSFER, istr.getId(), istr.getId());

            Map<String, StockMovementDetail> smdApprovedMap = new HashMap<String, StockMovementDetail>();

            Map<String, StockMovementDetail> smdRejectedMap = new HashMap<String, StockMovementDetail>();

        for (StockTransferDetailApproval stda : sta.getStockTransferDetailApprovals()) {
            ISTDetail istd = istService.getISTDetailById(stda.getStockTransferDetailId());
            String serialName = stda.getSerialName();
            double qty = stda.getQuantity();
            String key = istd.getDeliveredLocation().getId()
                    + (product.isIsrowforproduct() ? istd.getDeliveredRow().getId() : "")
                    + (product.isIsrackforproduct() ? istd.getDeliveredRack().getId() : "")
                    + (product.isIsbinforproduct() ? istd.getDeliveredBin().getId() : "")
                    + istd.getBatchName();
            if (stda.getApprovalStatus() == ApprovalStatus.APPROVED) {
                totalApprovedQty += qty;

                if (smdApprovedMap.containsKey(key)) {
                    StockMovementDetail smd = smdApprovedMap.get(key);
                    smd.setQuantity(smd.getQuantity() + qty);
                    smd.addSerialName(serialName);
                } else {
                    StockMovementDetail smd = new StockMovementDetail(smApproved, istd.getDeliveredLocation(), istd.getDeliveredRow(), istd.getDeliveredRack(), istd.getDeliveredBin(), istd.getBatchName(), serialName, qty);
                    smdApprovedMap.put(key, smd);
                    smApproved.getStockMovementDetails().add(smd);
                }
                smApproved.setQuantity(smApproved.getQuantity() + qty);

            } else if (stda.getApprovalStatus() == ApprovalStatus.REJECTED) {
                totalRejectedQty += qty;
//                istd.setDeliveredQuantity(istd.getDeliveredQuantity() - qty);
//                istd.removeFromDeliveredSerialName(serialName);
//                stockTransferApprovalDAO.saveOrUpdate(istd);
                if (smdRejectedMap.containsKey(key)) {
                    StockMovementDetail smd = smdRejectedMap.get(key);
                    smd.setQuantity(smd.getQuantity() + qty);
                    smd.addSerialName(serialName);
                } else {
                    StockMovementDetail smd = new StockMovementDetail(smRejected, istd.getDeliveredLocation(), istd.getDeliveredRow(), istd.getDeliveredRack(), istd.getDeliveredBin(), istd.getBatchName(), serialName, qty);
                    smdRejectedMap.put(key, smd);
                    smRejected.getStockMovementDetails().add(smd);
                }
                smRejected.setQuantity(smRejected.getQuantity() + qty);
            }

            }

    //        Packaging packaging = istr.getPackaging();
    //        double stockUomFactor = 1;
    //        if (packaging != null) {
    //            stockUomFactor = packaging.getStockUomQtyFactor(istr.getUom());
    //        }
    //        double acceptedQtyINStockUOM = istr.getOrderedQty() * stockUomFactor - totalRejectedQty;
    //        double acceptedQty = acceptedQtyINStockUOM / stockUomFactor;
    //        istr.setAcceptedQty(acceptedQty);
    //        stockTransferApprovalDAO.saveOrUpdate(istr);

        if (!smApproved.getStockMovementDetails().isEmpty()) {
            for (StockMovementDetail smd : smApproved.getStockMovementDetails()) {
                stockService.increaseInventory(istr.getProduct(), istr.getToStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                stockService.updateERPInventory(true, istr.getBusinessDate(), istr.getProduct(), istr.getPackaging(), istr.getUom(), smd.getQuantity(), "Approved");
                stockMovementService.stockMovementInERP(true, istr.getProduct(), istr.getToStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), false);
            }
            smApproved.setRemark("Returned Stock approved by QA");
            smApproved.setStockUoM(istr.getProduct().getUnitOfMeasure());
            stockMovementService.addStockMovement(smApproved);
        }
        if (!smRejected.getStockMovementDetails().isEmpty()) {
            for (StockMovementDetail smd : smRejected.getStockMovementDetails()) {
                stockService.increaseInventory(istr.getProduct(), istr.getToStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                stockService.updateERPInventory(true, istr.getBusinessDate(), istr.getProduct(), istr.getPackaging(), istr.getUom(), smd.getQuantity(), "Rejected");
                stockMovementService.stockMovementInERP(true, istr.getProduct(), istr.getToStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), false);

                //update serial status when a serial rejected.
                if (istr.getProduct().isIsSerialForProduct()) {
                    NewProductBatch productBatch = stockService.getERPProductBatch(istr.getProduct(), istr.getToStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName());
                    String[] serialArr = smd.getSerialNames().split(",");
                    for (String serial : serialArr) {
                        NewBatchSerial batchSerial = stockService.getERPBatchSerial(istr.getProduct(), productBatch, serial);
                        batchSerial.setQaApprovalstatus(QaApprovalStatus.REJECTED);
                        stockTransferApprovalDAO.saveOrUpdate(batchSerial);
                    }
                }
            }
                smRejected.setRemark("Returned Stock rejected by QA");
                smRejected.setStockUoM(istr.getProduct().getUnitOfMeasure());
                stockMovementService.addStockMovement(smRejected);
            }

            istr.setModifiedBy(sta.getInspector());
            istr.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long modifiedDate = System.currentTimeMillis();
            istr.setModifieddate(modifiedDate);
            istr.setStatus(InterStoreTransferStatus.RETURN_ACCEPTED);
            stockTransferApprovalDAO.saveOrUpdate(istr);
        } catch (ParseException ex) {
            Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void reflectSRStock(StockTransferApproval sta) throws ServiceException {
        try {
            double totalApprovedQuantity = 0;
            StockRequest sr = stockRequestService.getStockRequestById(sta.getStockTransferId());

        Product product = sr.getProduct();
        double purchasePrice = stockService.getProductPurchasePrice(sr.getProduct(), sr.getBusinessDate());
        StockMovement smApproved = new StockMovement(product, sr.getFromStore(), 0, purchasePrice, sr.getTransactionNo(), sr.getBusinessDate(), TransactionType.IN, TransactionModule.STOCK_REQUEST, sr.getId(), sr.getId());

        StockMovement smRejected = new StockMovement(product, sr.getFromStore(), 0, purchasePrice, sr.getTransactionNo(), sr.getBusinessDate(), TransactionType.IN, TransactionModule.STOCK_REQUEST, sr.getId(), sr.getId());

            Map<String, StockMovementDetail> smdApprovedMap = new HashMap<String, StockMovementDetail>();

            Map<String, StockMovementDetail> smdRejectedMap = new HashMap<String, StockMovementDetail>();

        for (StockTransferDetailApproval stda : sta.getStockTransferDetailApprovals()) {
            StockRequestDetail srd = stockRequestService.getStockRequestDetail(stda.getStockTransferDetailId());
            double qty = stda.getQuantity();
            String key = srd.getDeliveredLocation().getId()
                    + (product.isIsrowforproduct() ? srd.getDeliveredRow().getId() : "")
                    + (product.isIsrackforproduct() ? srd.getDeliveredRack().getId() : "")
                    + (product.isIsbinforproduct() ? srd.getDeliveredBin().getId() : "")
                    + srd.getBatchName();
            if (stda.getApprovalStatus() == ApprovalStatus.APPROVED) {
                totalApprovedQuantity += qty;
                if (smdApprovedMap.containsKey(key)) {
                    StockMovementDetail smd = smdApprovedMap.get(key);
                    smd.setQuantity(smd.getQuantity() + qty);
                    smd.addSerialName(stda.getSerialName());
                } else {
                    StockMovementDetail smd = new StockMovementDetail(smApproved, srd.getDeliveredLocation(), srd.getDeliveredRow(), srd.getDeliveredRack(), srd.getDeliveredBin(), srd.getBatchName(), stda.getSerialName(), qty);
                    smdApprovedMap.put(key, smd);
                    smApproved.getStockMovementDetails().add(smd);
                }
                smApproved.setQuantity(smApproved.getQuantity() + qty);
            } else if (stda.getApprovalStatus() == ApprovalStatus.REJECTED) {
//                srd.setDeliveredQuantity(srd.getDeliveredQuantity() - qty);
//                srd.removeFromDeliveredSerialName(stda.getSerialName());
//                stockTransferApprovalDAO.saveOrUpdate(srd);

                if (smdRejectedMap.containsKey(key)) {
                    StockMovementDetail smd = smdRejectedMap.get(key);
                    smd.setQuantity(smd.getQuantity() + qty);
                    smd.addSerialName(stda.getSerialName());
                } else {
                    StockMovementDetail smd = new StockMovementDetail(smRejected, srd.getDeliveredLocation(), srd.getDeliveredRow(), srd.getDeliveredRack(), srd.getDeliveredBin(), srd.getBatchName(), stda.getSerialName(), qty);
                    smdRejectedMap.put(key, smd);
                    smRejected.getStockMovementDetails().add(smd);
                }
                smRejected.setQuantity(smRejected.getQuantity() + qty);
            }
        }
    //        double deliveredQty = totalQuantity;
    //        Packaging packaging = sr.getPackaging();
    //        if (packaging != null) {
//            double stockUomFactor = packaging.getStockUomQtyFactor(sr.getUom());
    //            deliveredQty = deliveredQty / stockUomFactor;
    //        }
    //        sr.setDeliveredQty(deliveredQty);
    //        stockTransferApprovalDAO.saveOrUpdate(sr);

        if (!smApproved.getStockMovementDetails().isEmpty()) {
            for (StockMovementDetail smd : smApproved.getStockMovementDetails()) {
                stockService.increaseInventory(sr.getProduct(), sr.getFromStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                stockService.updateERPInventory(true, sr.getBusinessDate(), sr.getProduct(), sr.getPackaging(), sr.getUom(), smd.getQuantity(), "Approved");
                stockMovementService.stockMovementInERP(true, sr.getProduct(), sr.getFromStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), false);
            }
            smApproved.setRemark("Returned Stock approved by QA");
            smApproved.setStockUoM(sr.getProduct().getUnitOfMeasure());
            stockMovementService.addStockMovement(smApproved);
        }
        if (!smRejected.getStockMovementDetails().isEmpty()) {
            for (StockMovementDetail smd : smRejected.getStockMovementDetails()) {
                stockService.increaseInventory(sr.getProduct(), sr.getFromStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                stockService.updateERPInventory(true, sr.getBusinessDate(), sr.getProduct(), sr.getPackaging(), sr.getUom(), smd.getQuantity(), "Rejected");
                stockMovementService.stockMovementInERP(true, sr.getProduct(), sr.getFromStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), false);

                //update serial status when a serial rejected.
                if (sr.getProduct().isIsSerialForProduct()) {
                    NewProductBatch productBatch = stockService.getERPProductBatch(sr.getProduct(), sr.getFromStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName());
                    String[] serialArr = smd.getSerialNames().split(",");
                    for (String serial : serialArr) {
                        NewBatchSerial batchSerial = stockService.getERPBatchSerial(sr.getProduct(), productBatch, serial);
                        batchSerial.setQaApprovalstatus(QaApprovalStatus.REJECTED);
                        stockTransferApprovalDAO.saveOrUpdate(batchSerial);
                    }
                }
            }
                smRejected.setRemark("Returned Stock rejected by QA");
                smRejected.setStockUoM(sr.getProduct().getUnitOfMeasure());
                stockMovementService.addStockMovement(smRejected);
            }

            sr.setCollectedBy(sta.getInspector());
            sr.setStatus(RequestStatus.RETURNED);
            sr.setCollectedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long collectedDate = System.currentTimeMillis();
            sr.setCollecteddate(collectedDate);
            sr.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long modifiedDate = System.currentTimeMillis();
            sr.setModifieddate(modifiedDate);
            stockTransferApprovalDAO.saveOrUpdate(sr);
        } catch (ParseException ex) {
            Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public StockTransferDetailApproval getStockTransferDetailApproval(String id) {
        StockTransferDetailApproval stockTransferDetailapproval = null;
        if (!StringUtil.isNullOrEmpty(id)) {
            stockTransferDetailapproval = stockTransferApprovalDAO.getStockTransferDetailApproval(id);
        }
        return stockTransferDetailapproval;
    }

    @Override
    public void createStockMovementForQAApproval(Company company, List<StockTransferDetailApproval> approvedRejectedRecords, Store qaStore, Store repairStore) throws ServiceException {
        try{
        Date curDate = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
        if (qaStore == null && repairStore == null) {
            qaStore = stockService.getQaStore(company);
            repairStore = stockService.getRepairStore(company);
        }
        if (qaStore == null || repairStore == null) {
            throw new InventoryException("QA Store OR Repairing store are not set");
        }
        if (approvedRejectedRecords != null && !approvedRejectedRecords.isEmpty()) {
            Map<String, StockMovement> stockMovementQAMap = new HashMap(); // for qa store
            Map<String, StockMovement> stockMovementSRTMap = new HashMap(); // for Stock Request Store
            Map<String, StockMovement> stockMovementRepMap = new HashMap(); // for Repairing Store
            for (StockTransferDetailApproval srt : approvedRejectedRecords) {
                double quantity = srt.getRetQty() == 0 ? srt.getQuantity() : srt.getRetQty();
                ApprovalStatus approvalStatus = srt.getApprovalStatus();
                StockRequest sr = stockRequestService.getStockRequestById(srt.getStockTransferApproval().getStockTransferId());
                StockRequestDetail srd = stockRequestService.getStockRequestDetail(srt.getStockTransferDetailId());
                double purchasePrice = stockService.getProductPurchasePrice(sr.getProduct(), sr.getBusinessDate());
                StockMovement qasm;
                if (stockMovementQAMap.containsKey(sr.getId())) {
                    qasm = stockMovementQAMap.get(sr.getId());
                } else {
                    qasm = new StockMovement(sr.getProduct(), qaStore, 0, purchasePrice, sr.getTransactionNo(), curDate, TransactionType.OUT, TransactionModule.STOCK_REQUEST, sr.getId(), sr.getId());
                    qasm.setRemark(approvalStatus == ApprovalStatus.APPROVED ? "Stock Approved by QA" : "Stock Rejected by QA");
                    qasm.setStockUoM(sr.getUom());
                    qasm.setCostCenter(sr.getCostCenter());
                    stockMovementQAMap.put(sr.getId(), qasm);

                }
                StockMovementDetail qasmd = new StockMovementDetail(qasm, qaStore.getDefaultLocation(), srd.getDeliveredRow(), srd.getDeliveredRack(), srd.getDeliveredBin(), srd.getBatchName(), srt.getSerialName(), quantity);
                qasm.getStockMovementDetails().add(qasmd);
                qasm.setQuantity(qasm.getQuantity() + quantity);

                Location location = null;
                StockMovement insm = null;
                if (approvalStatus == ApprovalStatus.APPROVED) {
                    Store store = sr.getFromStore();
                    location = srd.getDeliveredLocation();
                    if (stockMovementSRTMap.containsKey(sr.getId())) {
                        insm = stockMovementSRTMap.get(sr.getId());
                    } else {
                        insm = new StockMovement(sr.getProduct(), store, 0, purchasePrice, sr.getTransactionNo(), curDate, TransactionType.IN, TransactionModule.STOCK_REQUEST, sr.getId(), sr.getId());
                        insm.setRemark(approvalStatus == ApprovalStatus.APPROVED ? "Stock Approved by QA" : "Stock Rejected by QA");
                        insm.setStockUoM(sr.getUom());
                        insm.setCostCenter(sr.getCostCenter());
                        stockMovementSRTMap.put(sr.getId(), insm);
                    }
                } else if (approvalStatus == ApprovalStatus.REJECTED) {
                    Store store = repairStore;
                    location = repairStore.getDefaultLocation();
                    if (stockMovementRepMap.containsKey(sr.getId())) {
                        insm = stockMovementRepMap.get(sr.getId());
                    } else {
                        insm = new StockMovement(sr.getProduct(), store, 0, purchasePrice, sr.getTransactionNo(), curDate, TransactionType.IN, TransactionModule.STOCK_REQUEST, sr.getId(), sr.getId());
                        insm.setRemark(approvalStatus == ApprovalStatus.APPROVED ? "Stock Approved by QA" : "Stock Rejected by QA");
                        insm.setStockUoM(sr.getUom());
                        insm.setCostCenter(sr.getCostCenter());
                        stockMovementRepMap.put(sr.getId(), insm);
                    }
                }
                if (insm != null && location != null) {
                    StockMovementDetail insmd = new StockMovementDetail(insm, location, srd.getDeliveredRow(), srd.getDeliveredRack(), srd.getDeliveredBin(), srd.getBatchName(), srt.getSerialName(), quantity);
                    insm.getStockMovementDetails().add(insmd);
                    insm.setQuantity(insm.getQuantity() + quantity);
                }

            }
            List<StockMovement> smList = new ArrayList<StockMovement>(); // Stock movements without SRT because SRT needs stock movement in ERP
            smList.addAll(stockMovementQAMap.values());
            smList.addAll(stockMovementRepMap.values());
            for (StockMovement sm : smList) {
                if (!sm.getStockMovementDetails().isEmpty()) {
                    for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                        if (sm.getTransactionType() == TransactionType.IN) {
                            stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
//                            stockService.updateERPInventory(true, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(true, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(),true);
                        } else {
                            stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
//                            stockService.updateERPInventory(false, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(false, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(),true);
                        }
                    } 
                    stockMovementService.addStockMovement(sm);
                }
            }
            for (StockMovement sm : stockMovementSRTMap.values()) {
                if (!sm.getStockMovementDetails().isEmpty()) {
                    for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                        if (sm.getTransactionType() == TransactionType.IN) {
                            stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
//                            stockService.updateERPInventory(true, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(true, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), true);
                        } else {
                            stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
//                            stockService.updateERPInventory(false, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(false, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), true);
                        }
                    }
                    stockMovementService.addStockMovement(sm);
                }
            }
        }
        } catch (ParseException ex) {
            Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void createStockMovementForRepairing(Company company, List<StockTransferDetailApproval> stockTransferDetailApprovalList, Store repairStore) throws ServiceException {
        try {
            Date curDate = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
        if (repairStore == null) {
            repairStore = stockService.getRepairStore(company);
        }
        if (repairStore == null) {
            throw new InventoryException("Repairing store is not set");
        }
            KwlReturnObject comprefkwl = accountingHandlerDAO.getObject(CompanyAccountPreferences.class.getName(), company.getCompanyID());
            CompanyAccountPreferences comppref = (CompanyAccountPreferences) comprefkwl.getEntityList().get(0);
            boolean isperpetualinv = comppref.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD;
            
        if (stockTransferDetailApprovalList != null && !stockTransferDetailApprovalList.isEmpty()) {
            Map<String, StockMovement> stockMovementSRTMap = new HashMap(); // for Stock Adjustment Store
            Map<String, StockMovement> stockMovementRepMap = new HashMap(); // for Repairing Store
            for (StockTransferDetailApproval srt : stockTransferDetailApprovalList) {
                double quantity = srt.getRetQty() == 0 ? srt.getQuantity() : srt.getRetQty();
                ApprovalStatus repairingStatus = srt.getRepairStatus();
                ApprovalStatus approvalStatus = srt.getApprovalStatus();
                StockRequest sr = stockRequestService.getStockRequestById(srt.getStockTransferApproval().getStockTransferId());
                StockRequestDetail srd = stockRequestService.getStockRequestDetail(srt.getStockTransferDetailId());
                double purchasePrice = stockService.getProductPurchasePrice(sr.getProduct(), sr.getBusinessDate());
                Store store=sr.getStatus()==RequestStatus.RETURN_APPROVAL?sr.getFromStore(): sr.getToStore();                
                /**ERP-37353 
                 * Avoiding this block for perpetual case as it creates a separate Stock Request entry when repair stock is rejected 
                 * and we are creating Stock adjustment already for reducing the stock hence to avoid double quantity reduction this part wont execute in perpetual case.
                 */
                if(!((srt.getStockTransferApproval().getTransactionModule() == TransactionModule.STOCK_REQUEST) && isperpetualinv)){

                    
                    StockMovement repsm;
                    if (stockMovementRepMap.containsKey(sr.getId())) {
                        repsm = stockMovementRepMap.get(sr.getId());
                    } else {
                        repsm = new StockMovement(sr.getProduct(), repairStore, 0, purchasePrice, sr.getTransactionNo(), curDate, TransactionType.OUT, TransactionModule.STOCK_REQUEST, sr.getId(), sr.getId());
                        repsm.setRemark(repairingStatus == ApprovalStatus.REPAIRDONE ? "Stock Repaired" : "Stock cannot be Repaired");
                        repsm.setStockUoM(sr.getUom());
                        repsm.setCostCenter(sr.getCostCenter());
                        stockMovementRepMap.put(sr.getId(), repsm);

                    }
                    StockMovementDetail qasmd = new StockMovementDetail(repsm, repairStore.getDefaultLocation(), srd.getDeliveredRow(), srd.getDeliveredRack(), srd.getDeliveredBin(), srd.getBatchName(), srt.getSerialName(), quantity);
                    repsm.getStockMovementDetails().add(qasmd);
                    repsm.setQuantity(repsm.getQuantity() + quantity);
                }
                Location location = sr.getStatus()==RequestStatus.RETURN_APPROVAL?srd.getIssuedLocation(): srd.getDeliveredLocation();
                StockMovement insm = null;
                if (repairingStatus == ApprovalStatus.REPAIRDONE) {
//                    location = srd.getDeliveredLocation();
                    if (stockMovementSRTMap.containsKey(sr.getId())) {
                        insm = stockMovementSRTMap.get(sr.getId());
                    } else {
                        insm = new StockMovement(sr.getProduct(), store, 0, purchasePrice, sr.getTransactionNo(), curDate, TransactionType.IN, TransactionModule.STOCK_REQUEST, sr.getId(), sr.getId());
                        insm.setRemark(repairingStatus == ApprovalStatus.REPAIRDONE ? "Stock Repaired" : "Stock cannot be Repaired");
                        insm.setStockUoM(sr.getUom());
                        insm.setCostCenter(sr.getCostCenter());
                        stockMovementSRTMap.put(sr.getId(), insm);
                    }
                }
                if (insm != null && location != null) {
                    StockMovementDetail insmd = new StockMovementDetail(insm, location, srd.getDeliveredRow(), srd.getDeliveredRack(), srd.getDeliveredBin(), srd.getBatchName(), srt.getSerialName(), quantity);
                    insm.getStockMovementDetails().add(insmd);
                    insm.setQuantity(insm.getQuantity() + quantity);
                }
            }
            for (StockMovement sm : stockMovementRepMap.values()) {
                if (!sm.getStockMovementDetails().isEmpty()) {
                    for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                        if (sm.getTransactionType() == TransactionType.IN) {
                            stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                            stockService.updateERPInventory(true, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                        } else { 
                        //ERP-37353 - For perpetual companies now repair rejected stock will have a Stock Adjustment posted hence do not decrease inventory quantity here
                            if(!((sm.getTransactionModule() == TransactionModule.STOCK_REQUEST) && isperpetualinv)){
                                stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                                stockService.updateERPInventory(false, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            }
                        }
                    }
                    stockMovementService.addStockMovement(sm);
                }
            }
                    for (StockMovement sm : stockMovementSRTMap.values()) {
                        if (!sm.getStockMovementDetails().isEmpty()) {
                            for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                                if (sm.getTransactionType() == TransactionType.IN) {
                            stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                            stockService.updateERPInventory(true, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(true, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), true);
                        } else {
                            stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                            stockService.updateERPInventory(false, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(false, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), true);
                        }
                    }
                    stockMovementService.addStockMovement(sm);
                }
            }
        }
        } catch (ParseException ex) {
            Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void createStockMovementForQAApprovalIntr(Company company, List<StockTransferDetailApproval> approvedRejectedRecords, Store qaStore, Store repairStore) throws ServiceException {
        try{
            
        Date curDate =authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
        if (qaStore == null && repairStore == null) {
            qaStore = stockService.getQaStore(company);
            repairStore = stockService.getRepairStore(company);
        }
        if (qaStore == null || repairStore == null) {
            throw new InventoryException("QA Store OR Repairing store are not set");
        }
        if (approvedRejectedRecords != null && !approvedRejectedRecords.isEmpty()) {
            Map<String, StockMovement> stockMovementQAMap = new HashMap(); // for qa store
            Map<String, StockMovement> stockMovementSTRMap = new HashMap(); // for Stock Request Store
            Map<String, StockMovement> stockMovementRepMap = new HashMap(); // for Repairing Store
            for (StockTransferDetailApproval srt : approvedRejectedRecords) {
                double quantity = srt.getRetQty() == 0 ? srt.getQuantity() : srt.getRetQty();
                ApprovalStatus approvalStatus = srt.getApprovalStatus();
                InterStoreTransferRequest sr = istService.getInterStoreTransferById(srt.getStockTransferApproval().getStockTransferId());
//                StockRequestDetail SRD = stockRequestService.getStockRequestDetail(srt.getStockTransferDetailId());
                ISTDetail istd = istService.getISTDetailById(srt.getStockTransferDetailId());
                double purchasePrice = stockService.getProductPurchasePrice(sr.getProduct(), sr.getBusinessDate());
                StockMovement qasm;
                if (stockMovementQAMap.containsKey(sr.getId())) {
                    qasm = stockMovementQAMap.get(sr.getId());
                } else {
                    qasm = new StockMovement(sr.getProduct(), qaStore, 0, purchasePrice, sr.getTransactionNo(), curDate, TransactionType.OUT, TransactionModule.INTER_STORE_TRANSFER, sr.getId(), sr.getId());
                    qasm.setRemark(approvalStatus == ApprovalStatus.APPROVED ? "Stock Approved by QA" : "Stock Rejected by QA");
                    qasm.setStockUoM(sr.getUom());
                    qasm.setCostCenter(sr.getCostCenter());
                    stockMovementQAMap.put(sr.getId(), qasm);

                }
                StockMovementDetail qasmd = new StockMovementDetail(qasm, qaStore.getDefaultLocation(), istd.getDeliveredRow(), istd.getDeliveredRack(), istd.getDeliveredBin(), istd.getBatchName(), srt.getSerialName(), quantity);
                qasm.getStockMovementDetails().add(qasmd);
                qasm.setQuantity(qasm.getQuantity() + quantity);

                Location location = null;
                StockMovement insm = null;
                if (approvalStatus == ApprovalStatus.APPROVED) {
                    Store store = sr.getToStore();
                    location = istd.getDeliveredLocation();
                    if (stockMovementSTRMap.containsKey(sr.getId())) {
                        insm = stockMovementSTRMap.get(sr.getId());
                    } else {
                        insm = new StockMovement(sr.getProduct(), store, 0, purchasePrice, sr.getTransactionNo(), curDate, TransactionType.IN, TransactionModule.INTER_STORE_TRANSFER, sr.getId(), sr.getId());
                        insm.setRemark(approvalStatus == ApprovalStatus.APPROVED ? "Stock Approved by QA" : "Stock Rejected by QA");
                        insm.setStockUoM(sr.getUom());
                        insm.setCostCenter(sr.getCostCenter());
                        stockMovementSTRMap.put(sr.getId(), insm);
                    }
                } else if (approvalStatus == ApprovalStatus.REJECTED) {
                    Store store = repairStore;
                    location = repairStore.getDefaultLocation();
                    if (stockMovementRepMap.containsKey(sr.getId())) {
                        insm = stockMovementRepMap.get(sr.getId());
                    } else {
                        insm = new StockMovement(sr.getProduct(), store, 0, purchasePrice, sr.getTransactionNo(), curDate, TransactionType.IN, TransactionModule.INTER_STORE_TRANSFER, sr.getId(), sr.getId());
                        insm.setRemark(approvalStatus == ApprovalStatus.APPROVED ? "Stock Approved by QA" : "Stock Rejected by QA");
                        insm.setStockUoM(sr.getUom());
                        insm.setCostCenter(sr.getCostCenter());
                        stockMovementRepMap.put(sr.getId(), insm);
                    }
                }
                if (insm != null && location != null) {
                    StockMovementDetail insmd = new StockMovementDetail(insm, location, istd.getDeliveredRow(), istd.getDeliveredRack(), istd.getDeliveredBin(), istd.getBatchName(), srt.getSerialName(), quantity);
                    insm.getStockMovementDetails().add(insmd);
                    insm.setQuantity(insm.getQuantity() + quantity);
                }

            }
            List<StockMovement> smList = new ArrayList<StockMovement>(); // Stock movements without SRT because SRT needs stock movement in ERP
            smList.addAll(stockMovementQAMap.values());
            smList.addAll(stockMovementRepMap.values());
            for (StockMovement sm : smList) {
                if (!sm.getStockMovementDetails().isEmpty()) {
                    for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                        if (sm.getTransactionType() == TransactionType.IN) {
                            stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
//                            stockService.updateERPInventory(true, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(true, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(),true);
                        } else {
                            stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
//                            stockService.updateERPInventory(false, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(false, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(),true);
                        }
                    }
                    stockMovementService.addStockMovement(sm);
                }
            }
            for (StockMovement sm : stockMovementSTRMap.values()) {
                if (!sm.getStockMovementDetails().isEmpty()) {
                    for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                        if (sm.getTransactionType() == TransactionType.IN) {
                            stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                            stockService.updateERPInventory(true, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(true, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), true);
                        } else {
                            stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                            stockService.updateERPInventory(false, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(false, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), true);
                        }
                    }
                    stockMovementService.addStockMovement(sm);
                }
            }
        }
        } catch (ParseException ex) {
            Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void createStockMovementForRepairingIntr(Company company, List<StockTransferDetailApproval> stockTransferDetailApprovalList, Store repaieStore) throws ServiceException {
        try{
            
            Date curDate = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            KwlReturnObject comprefkwl = accountingHandlerDAO.getObject(CompanyAccountPreferences.class.getName(), company.getCompanyID());
            CompanyAccountPreferences comppref = (CompanyAccountPreferences) comprefkwl.getEntityList().get(0);
            boolean isperpetualinv = comppref.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD;
        if (repaieStore == null) {
            repaieStore = stockService.getRepairStore(company);
        }
        if (repaieStore == null) {
            throw new InventoryException("Repairing store is not set");
        }
        if (stockTransferDetailApprovalList != null && !stockTransferDetailApprovalList.isEmpty()) {
            Map<String, StockMovement> stockMovementSTRMap = new HashMap(); // for Stock Adjustment Store
            Map<String, StockMovement> stockMovementRepMap = new HashMap(); // for Repairing Store
            for (StockTransferDetailApproval srt : stockTransferDetailApprovalList) {
                double quantity = srt.getRetQty() == 0 ? srt.getQuantity() : srt.getRetQty();
                ApprovalStatus repairingStatus = srt.getRepairStatus();
                ApprovalStatus approvalStatus = srt.getApprovalStatus();
                InterStoreTransferRequest sr = istService.getInterStoreTransferById(srt.getStockTransferApproval().getStockTransferId());
//                StockRequestDetail SRD = stockRequestService.getStockRequestDetail(srt.getStockTransferDetailId());
                ISTDetail istd = istService.getISTDetailById(srt.getStockTransferDetailId());
                double purchasePrice = stockService.getProductPurchasePrice(sr.getProduct(), sr.getBusinessDate());
                /**ERP-37353 
                 * Avoiding this block for perpetual case as it creates a separate Stock Request/Return IST entry when repair stock is rejected 
                 * and we are creating Stock adjustment already for reducing the stock hence to avoid double quantity reduction this part wont execute in perpetual case.
                 */
                if(!((srt.getStockTransferApproval().getTransactionModule() == TransactionModule.STOCK_REQUEST) && isperpetualinv)){

                    
                    StockMovement repsm;
                    if (stockMovementRepMap.containsKey(sr.getId())) {
                        repsm = stockMovementRepMap.get(sr.getId());
                    } else {
                        repsm = new StockMovement(sr.getProduct(), repaieStore, 0, purchasePrice, sr.getTransactionNo(), curDate, TransactionType.OUT, TransactionModule.INTER_STORE_TRANSFER, sr.getId(), sr.getId());
                        repsm.setRemark(repairingStatus == ApprovalStatus.REPAIRDONE ? "Stock Repaired" : "Stock cannot be Repaired");
                        repsm.setStockUoM(sr.getUom());
                        repsm.setCostCenter(sr.getCostCenter());
                        stockMovementRepMap.put(sr.getId(), repsm);

                    }
                    StockMovementDetail qasmd = new StockMovementDetail(repsm, repaieStore.getDefaultLocation(), istd.getDeliveredRow(), istd.getDeliveredRack(), istd.getDeliveredBin(), istd.getBatchName(), srt.getSerialName(), quantity);
                    repsm.getStockMovementDetails().add(qasmd);
                    repsm.setQuantity(repsm.getQuantity() + quantity);
                }
                Location location = null;
                StockMovement insm = null;
                if (repairingStatus == ApprovalStatus.REPAIRDONE) {
                    location = istd.getDeliveredLocation();
                    if (stockMovementSTRMap.containsKey(sr.getId())) {
                        insm = stockMovementSTRMap.get(sr.getId());
                    } else {
                        insm = new StockMovement(sr.getProduct(), sr.getToStore(), 0, purchasePrice, sr.getTransactionNo(), curDate, TransactionType.IN, TransactionModule.INTER_STORE_TRANSFER, sr.getId(), sr.getId());
                        insm.setRemark(repairingStatus == ApprovalStatus.REPAIRDONE ? "Stock Repaired" : "Stock cannot be Repaired");
                        insm.setStockUoM(sr.getUom());
                        insm.setCostCenter(sr.getCostCenter());
                        stockMovementSTRMap.put(sr.getId(), insm);
                    }
                }
                if (insm != null && location != null) {
                    StockMovementDetail insmd = new StockMovementDetail(insm, location, istd.getDeliveredRow(), istd.getDeliveredRack(), istd.getDeliveredBin(), istd.getBatchName(), srt.getSerialName(), quantity);
                    insm.getStockMovementDetails().add(insmd);
                    insm.setQuantity(insm.getQuantity() + quantity);
                }
            }
            for (StockMovement sm : stockMovementRepMap.values()) {
                if (!sm.getStockMovementDetails().isEmpty()) {
                    for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                        if (sm.getTransactionType() == TransactionType.IN) {
                            stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
//                            stockService.updateERPInventory(true, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                        } else {
                            if(!((sm.getTransactionModule() == TransactionModule.STOCK_REQUEST) && isperpetualinv)){
                                stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
//                            stockService.updateERPInventory(false, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            }
                        }
                    }
                    stockMovementService.addStockMovement(sm);
                }
            }
            for (StockMovement sm : stockMovementSTRMap.values()) {
                if (!sm.getStockMovementDetails().isEmpty()) {
                    for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                        if (sm.getTransactionType() == TransactionType.IN) {
                            stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                            stockService.updateERPInventory(true, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(true, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), true);
                        } else {
                            stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                            stockService.updateERPInventory(false, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(false, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), true);
                            }
                    }
                    stockMovementService.addStockMovement(sm);
                }
            }
        }
        } catch (ParseException ex) {
            Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String approveRejectGoodsReceipt(Map<String,Object> requestParams, Store repairStore, Location repairLocation) throws ServiceException {
        String auditMessage = "";
        try {
            JSONArray jArr = new JSONArray((String) requestParams.get("jsondata"));
            String operation= (String) requestParams.get("operation");
            String interstore_loc_No="";
            String remark="";
            SeqFormat seqFormat = (SeqFormat) requestParams.get("seqFormat");
            String operationMsg = "";
            String newistid = "";
            
            String msg = "";
            User user = (User) requestParams.get("user");
            String companyId = (String) requestParams.get("company");
            InterStoreTransferRequest outgoingISTRequest = null;
            InterStoreTransferRequest incomingISTRequest = null;
            GRODetailISTMapping detailISTMapping = null;
            double totalQuantityDue = 0, totalApprovedQty = 0, totalRejectedQty = 0, totalRejectedQtyDue = 0, totalQty = 0;
            StringBuilder approvedSerials = new StringBuilder(), rejectedSerials = new StringBuilder();
            String groNumber = "";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject json = jArr.getJSONObject(i);
                String detailid = json.optString("recordid");
                String serialname = json.optString("serialname");
                double quantity = json.optDouble("quantity");
                if (!StringUtil.isNullOrEmpty(detailid)) {
                    ISTDetail istDetail = istService.getISTDetailById(detailid);
                    if (istDetail != null && quantity > 0) {
                        if (operation.equals("Approve") || operation.equals("Reject")) {
                            if (istDetail.getIssuedQuantity() - (istDetail.getQaApproved() + istDetail.getQaRejected()) < quantity) {
                                throw new AccountingException("This item is already processed");
                            }
                            if (operation.equals("Approve")) {
                                operationMsg = "approved";
                                istDetail.setQaApproved(istDetail.getQaApproved() + quantity);
                            } else {
                                operationMsg = "rejected";
                                istDetail.setQaRejected(istDetail.getQaRejected() + quantity);
                            }
                            incomingISTRequest = istDetail.getIstRequest();
                            JSONObject params = new JSONObject();
                            params.put("istRequest", incomingISTRequest.getId());
                            KwlReturnObject result = stockService.getGRODetailISTMapping(params);
                            if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                detailISTMapping = (GRODetailISTMapping) result.getEntityList().get(0);
                            }
                            if (outgoingISTRequest == null && incomingISTRequest != null) {
                                if (operation.equals("Reject")) {
                                    outgoingISTRequest = new InterStoreTransferRequest(incomingISTRequest.getProduct(), incomingISTRequest.getToStore(), repairStore, incomingISTRequest.getUom());
                                } else {
                                    outgoingISTRequest = new InterStoreTransferRequest(incomingISTRequest.getProduct(), incomingISTRequest.getToStore(), incomingISTRequest.getFromStore(), incomingISTRequest.getUom());
                                }
                                outgoingISTRequest.setRemark(remark);
                                outgoingISTRequest.setAcceptedQty(quantity);
                                outgoingISTRequest.setOrderedQty(quantity);
                                outgoingISTRequest.setBusinessDate(new Date());
                                outgoingISTRequest.setPackaging(incomingISTRequest.getProduct().getPackaging());
                                totalApprovedQty = detailISTMapping.getApprovedQty();
                                totalQuantityDue = detailISTMapping.getQuantityDue();
                                totalRejectedQty = detailISTMapping.getRejectedQty();
                            } else {
                                outgoingISTRequest.setOrderedQty(outgoingISTRequest.getOrderedQty() + quantity);
                                outgoingISTRequest.setAcceptedQty(outgoingISTRequest.getAcceptedQty() + quantity);
                            }
                            /*
                             * Check quantitydue before approving transaction to avoid negative stock
                             * Quantitydue contains pending stock in QA
                             */
                            if (totalQuantityDue <= 0 || totalQuantityDue < quantity) {
                                throw new AccountingException("This item is already processed");
                            }
                            ISTDetail outISTDetail = new ISTDetail();
                            outISTDetail.setBatchName(istDetail.getBatchName());
                            outISTDetail.setDeliveredBin(istDetail.getIssuedBin());
                            outISTDetail.setDeliveredQuantity(quantity);
                            outISTDetail.setDeliveredRack(istDetail.getIssuedRack());
                            outISTDetail.setDeliveredRow(istDetail.getIssuedRow());
                            outISTDetail.setDeliveredSerialNames(serialname);
                            if (operation.equals("Reject")) {
                                if (repairLocation != null) { //ERM-691 dynamic changing of repair store
                                    outISTDetail.setDeliveredLocation(repairLocation);
                                } else {
                                    outISTDetail.setDeliveredLocation(repairStore.getDefaultLocation());
                                }
                            } else {
                                outISTDetail.setDeliveredLocation(istDetail.getIssuedLocation());
                            }
                            outISTDetail.setIssuedBin(istDetail.getDeliveredBin());
                            outISTDetail.setIssuedLocation(istDetail.getDeliveredLocation());
                            outISTDetail.setIssuedQuantity(quantity);
                            outISTDetail.setIssuedRack(istDetail.getDeliveredRack());
                            outISTDetail.setIssuedRow(istDetail.getDeliveredRow());
                            outISTDetail.setIssuedSerialNames(serialname);
                            outISTDetail.setIstRequest(outgoingISTRequest);
                            if (outgoingISTRequest != null) {
                                outgoingISTRequest.getIstDetails().add(outISTDetail);
                            }
                            outgoingISTRequest.setTransactionNo(interstore_loc_No);
                            if (detailISTMapping != null) {
                                JSONObject jsonParams = new JSONObject();
                                jsonParams.put(Constants.companyKey, companyId);
                                jsonParams.put("mappingid", detailISTMapping.getID());
                                groNumber = stockService.getGoodsReceiptOrderNumberUsingMapping(jsonParams);
                                StringBuilder processedSerials = new StringBuilder(detailISTMapping.getApprovedSerials() + "," + detailISTMapping.getRejectedSerials());
                                if (!StringUtil.isNullOrEmpty(processedSerials.toString())) {
                                    String[] processedSerial = processedSerials.toString().split(",");
                                    for (String serial : processedSerial) {
                                        if (serial.equals(serialname)) {
                                            throw new AccountingException("This item is already processed");
                                        }
                                    }
                                }

                                if (!StringUtil.isNullOrEmpty(groNumber)) {
                                    if (operation.equals("Approve")) {
                                        totalApprovedQty += quantity;
                                        outgoingISTRequest.setMemo("Approved Goods Receipt Note : " + groNumber);
                                        outgoingISTRequest.setDetailISTMapping(detailISTMapping);
                                        if (!StringUtil.isNullOrEmpty(serialname)) {
                                            if (StringUtil.isNullOrEmpty(approvedSerials.toString())) {
                                                approvedSerials.append(StringUtil.isNullOrEmpty(detailISTMapping.getApprovedSerials()) ? "" : detailISTMapping.getApprovedSerials());
                                            }
                                            if (approvedSerials.length() > 0) {
                                                approvedSerials.append(",").append(serialname);
                                            } else {
                                                approvedSerials.append(serialname);
                                            }
                                        }
                                    } else {
                                        totalRejectedQty += quantity;
                                        totalRejectedQtyDue += quantity;
                                        outgoingISTRequest.setMemo("Rejected Goods Receipt Note : " + groNumber);
                                        if (!StringUtil.isNullOrEmpty(serialname)) {
                                            if (StringUtil.isNullOrEmpty(rejectedSerials.toString())) {
                                                rejectedSerials.append(StringUtil.isNullOrEmpty(detailISTMapping.getRejectedSerials()) ? "" : detailISTMapping.getRejectedSerials());
                                            }
                                            if (rejectedSerials.length() > 0) {
                                                rejectedSerials.append(",").append(serialname);
                                            } else {
                                                rejectedSerials.append(serialname);
                                            }
                                        }
                                    }
                                    totalQuantityDue -= quantity;
                                }
                            }
                            totalQty += quantity;
                        }
                    }
                }
            }
            if (outgoingISTRequest != null && incomingISTRequest != null) {
                Map<String, Object> requestParams1 = new HashMap<>();
                requestParams1.put(Constants.companyid, companyId);
                requestParams1.put("mappingid", detailISTMapping.getID());
                requestParams1.put("operation", operation);
                requestParams1.put(Constants.moduleid, Constants.Acc_InterStore_ModuleId);
                requestParams1.put("quantitydue", totalQuantityDue);
                if (operation.equals("Approve")) {
                    auditMessage = "Approved Goods Receipt Note: " + groNumber + " [Product: " + incomingISTRequest.getProduct().getProductid() + ", Quantity: " + totalQty + ", Store: " + incomingISTRequest.getFromStore().getAbbreviation() + (!StringUtil.isNullOrEmpty(approvedSerials.toString()) ? ", Serials: (" + approvedSerials.toString() + ")" : "");
                    requestParams1.put("approvedQty", totalApprovedQty);
                    if (!StringUtil.isNullOrEmpty(approvedSerials.toString())) {
                        requestParams1.put("approvedSerials", approvedSerials.toString());
                    }
                } else {
                    auditMessage = "Rejected Goods Receipt Note: " + groNumber + " [Product: " + incomingISTRequest.getProduct().getProductid() + ", Quantity: " + totalQty + ", Store: " + incomingISTRequest.getFromStore().getAbbreviation() + (!StringUtil.isNullOrEmpty(rejectedSerials.toString()) ? ", Serials: (" + rejectedSerials.toString() + ")" : "");
                    requestParams1.put("rejectedQty", totalRejectedQty);
                    requestParams1.put("rejectedQtyDue", totalRejectedQtyDue);
                    if (!StringUtil.isNullOrEmpty(rejectedSerials.toString())) {
                        requestParams1.put("rejectedSerials", rejectedSerials.toString());
                    }
                }
                istService.addInterStoreTransferRequest(user, outgoingISTRequest, false, requestParams1);
                istService.acceptInterStoreTransferRequest(user, outgoingISTRequest);
                seqService.updateSeqNumber(seqFormat);
                msg = "QA has " + operationMsg + " Goods Receipt Note successfully";

                //newistid contains id of newly rejected or approved row in qa in case of goods receipt
                boolean isfirst = true;
                ISTDetail istd;
                Iterator iterator = outgoingISTRequest.getIstDetails().iterator();
                while (iterator.hasNext()) {
                    istd = (ISTDetail) iterator.next();
                    if (isfirst) {
                        newistid = istd.getId();
                        isfirst = false;
                    } else {
                        newistid = newistid + "," + istd.getId();
                    }
                }
            }

        } catch (Exception ex) {
             Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);

        }
        return auditMessage;
    }
    
 
  /**
    * Create a Stock Adjustment Out entry for QA rejected transactions post a JE in case of perpetual inventory only 
    * Handled transactions include  1.IST  2.Stock Request  3.Stock Adjustment
    * @param params    
    * @throws ServiceException 
    */    
    @Override
    public void createStockOutInventoryJEforQAtransaction(JSONObject params) throws ServiceException  {
        try {
            KwlReturnObject jeresult = null;
            String companyId = params.optString(Constants.companyid, "");
            String remoteIP = params.optString(Constants.remoteIPAddress, "");
            String userId = params.optString(Constants.userid, "");
            String auditmessage = "";
            Map<String,Object> auditparams = new HashMap<>();
            
            KwlReturnObject useresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) (useresult.getEntityList().isEmpty() ? null : useresult.getEntityList().get(0));
            
            KwlReturnObject companyprefkwl = accountingHandlerDAO.getObject(CompanyAccountPreferences.class.getName(), companyId);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) companyprefkwl.getEntityList().get(0);
            Company company = preferences.getCompany();
            
            Locale locale =(Locale) params.opt(Constants.locale);            
            TransactionModule transmodule =(TransactionModule) params.opt(Constants.transactionmodule);
            String recordId = params.optString("recordid", "");
            String reason = params.optString("reason", "");
            double qty = params.optDouble(Constants.quantity, 0);
            String repairstore = params.optString("repairstore", "");
            Store repairstoreobj = storeService.getStoreById(repairstore);
            String stockadjustmentno = "";
            StockTransferDetailApproval stockTransferDTL = stockTransferApprovalDAO.getStockTransferDetailApproval(recordId);
            
            /**
             *These fields will change depending on the transaction(Stock Request/Adjustment/IST) hence the prefix 'trans' is added.
             */
            Location translocation = null; 
            StoreMaster transrow = null;
            StoreMaster transrack = null;
            StoreMaster transbin = null;
            String transbatchname = "";
            String transserialnames = "";
            String transfinalserialnames = "";
            
            SeqFormat seqFormat = seqService.getDefaultSeqFormat(company, ModuleConst.STOCK_ADJUSTMENT);
            if (seqFormat != null) {
                stockadjustmentno = seqService.getNextFormatedSeqNumber(seqFormat);
            } else {
                throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforStockAdjustmentNotSet", null,locale));
            }
            Date businessDate = new Date();
            Product product = new Product();
            double productPrice = 0;
            StockAdjustment createStockAdjustment = null;
            
            switch (transmodule) { //based on the type of transaction prepare the Stock Adjustment OUT details 
                case STOCK_REQUEST: //Stock Request return stock rejected from QC Repair
                    StockRequestDetail srd = (StockRequestDetail) params.opt("stockrequestdetail");
                    product = srd.getStockRequest().getProduct();
                    productPrice = stockService.getProductPurchasePrice(product, businessDate);
                    transrow = srd.getDeliveredRow(); 
                    transrack = srd.getDeliveredRack();
                    transbin = srd.getDeliveredBin();
                    transbatchname = srd.getBatchName();
                    //get only the rejected serial names here not all which is separated serial wise in StockTransferApprovalDetail
                    transserialnames = stockTransferDTL.getSerialName();
                    transfinalserialnames = stockTransferDTL.getSerialName();
                    translocation = srd.getDeliveredLocation();
                    createStockAdjustment = new StockAdjustment(product, repairstoreobj, srd.getStockRequest().getUom(), -qty, productPrice, businessDate);
                    createStockAdjustment.setMemo("QA Rejected Stock for Stock Request:"+srd.getStockRequest().getTransactionNo());
                    createStockAdjustment.setRemark("QA Rejected Stock for Stock Request:"+srd.getStockRequest().getTransactionNo());
                    auditmessage = "(Product :" + product.getProductid() + ", Quantity :" + (-qty) + " " + srd.getStockRequest().getUom().getNameEmptyforNA() + ", AdjustmentType : " + "Stock Out " + ")";
                    auditmessage = "User " + user.getFullName() + " has rejected Stock Request: " + srd.getStockRequest().getTransactionNo() + " for Store: " + repairstoreobj.getAbbreviation() + " " + auditmessage;
                    break;

                case INTER_STORE_TRANSFER: //Repair IST rejected from QC Repair store 
                    stockTransferDTL =(StockTransferDetailApproval) params.opt("stocktransferDTL");
                    KwlReturnObject istkwl = accountingHandlerDAO.getObject(ISTDetail.class.getName(), recordId);
                    ISTDetail istd = (ISTDetail) istkwl.getEntityList().get(0);
                    InterStoreTransferRequest interStoreTransferRequest = istd.getIstRequest();
                    product = interStoreTransferRequest.getProduct();
                    createStockAdjustment = new StockAdjustment(product,repairstoreobj, istd.getIstRequest().getUom(), -qty, productPrice, businessDate);
                    productPrice = stockService.getProductPurchasePrice(product, businessDate);
                    transrow = istd.getDeliveredRow();                    
                    transrack = istd.getDeliveredRack();
                    transbin = istd.getDeliveredBin();
                    transbatchname = istd.getBatchName();
                    //get only the rejected serial names here not all which is separated serial wise in StockTransferApprovalDetail
                    transserialnames = stockTransferDTL.getSerialName();
                    transfinalserialnames = stockTransferDTL.getSerialName();
                    translocation = istd.getDeliveredLocation();
                    createStockAdjustment.setMemo("QA Rejected Stock for IST:"+istd.getIstRequest().getTransactionNo());
                    createStockAdjustment.setRemark("QA Rejected Stock for IST:"+istd.getIstRequest().getTransactionNo());
                    auditmessage = "(Product :" + product.getProductid() + ", Quantity :" + (-qty) + " " + interStoreTransferRequest.getUom().getNameEmptyforNA() + ", AdjustmentType : " + "Stock Out " + ")";
                    auditmessage = "User " + user.getFullName() + " has rejected the IST: " + interStoreTransferRequest.getTransactionNo() + " for Store: " + repairstoreobj.getAbbreviation() + " " + auditmessage;
                    break;

                case STOCK_ADJUSTMENT: //SA rejected from QC Repair 
                    StockAdjustment initialsa = (StockAdjustment) params.opt("initialSA");
                    SADetailApproval sada = (SADetailApproval) params.opt("approvaldetail");
                    product = initialsa.getProduct();
                    createStockAdjustment = new StockAdjustment(product,repairstoreobj, initialsa.getUom(), -qty, productPrice, businessDate);
                    productPrice = stockService.getProductPurchasePrice(product, businessDate);
                    transrow = sada.getStockAdjustmentDetail().getRow();
                    transrack = sada.getStockAdjustmentDetail().getRack();
                    translocation = repairstoreobj.getDefaultLocation();
                    transbin = sada.getStockAdjustmentDetail().getBin();
                    transbatchname = sada.getStockAdjustmentDetail().getBatchName();
                    //get only the rejected serial names here not all which is separated serial wise in StockTransferApprovalDetail
                    transserialnames = sada.getSerialName(); 
                    transfinalserialnames = sada.getSerialName(); 
                    createStockAdjustment.setMemo("QA Rejected Stock for SA:"+initialsa.getTransactionNo());
                    createStockAdjustment.setRemark("QA Rejected Stock for SA:"+initialsa.getTransactionNo());
                    auditmessage = "(Product :" + product.getProductid() + ", Quantity :" + (-qty) + " " + initialsa.getUom().getNameEmptyforNA() + ", AdjustmentType : " + "Stock Out " + ")";
                    auditmessage = "User " + user.getFullName() + " has rejected the Stock Adjustment: " + initialsa.getTransactionNo()+ " for Store: " + repairstoreobj.getAbbreviation() + " " + auditmessage;
                    break;
            }
            if(createStockAdjustment!=null){
                createStockAdjustment.setAdjustmentType("Stock Out");
                createStockAdjustment.setTransactionNo(stockadjustmentno);
                createStockAdjustment.setReason(reason);
                createStockAdjustment.setCompany(company);
                createStockAdjustment.setCreatedOn(businessDate);
                createStockAdjustment.setCreationdate(businessDate.getTime());
                createStockAdjustment.setTransactionModule(TransactionModule.STOCK_ADJUSTMENT);
                

                Set<StockAdjustmentDetail> adjustmentDetailSet = new HashSet<>();
                StockAdjustmentDetail stockAdjustmentDetail = new StockAdjustmentDetail();
                stockAdjustmentDetail.setBatchName(transbatchname);
                stockAdjustmentDetail.setBin(transbin);
                stockAdjustmentDetail.setRack(transrack);
                stockAdjustmentDetail.setRow(transrow);
                stockAdjustmentDetail.setLocation(translocation);
                stockAdjustmentDetail.setFinalQuantity(qty);
                stockAdjustmentDetail.setQuantity(qty);
                stockAdjustmentDetail.setFinalSerialNames(transfinalserialnames);
                stockAdjustmentDetail.setSerialNames(transserialnames);
                stockAdjustmentDetail.setStockAdjustment(createStockAdjustment);
                adjustmentDetailSet.add(stockAdjustmentDetail);
                createStockAdjustment.setStockAdjustmentDetail(adjustmentDetailSet);
                JournalEntry inventoryJE = null;
                if (product.getInventoryAccount() != null) {                    
                    Map<String, Object> JEFormatParams = new HashMap<>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                    JEFormatParams.put("companyid", companyId);
                    JEFormatParams.put("isdefaultFormat", true);
                    KwlReturnObject kwlObj = accCompanyPreferencesDAO.getSequenceFormat(JEFormatParams);
                    if (kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty()) {
                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                        if (format != null) {
                            JSONObject globalparamjson = new JSONObject();
                            globalparamjson.put(Constants.companyKey, companyId);
                            globalparamjson.put(Constants.globalCurrencyKey, params.optString(Constants.globalCurrencyKey, ""));
                            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(globalparamjson);
                            Map<String, Object> seqNumberMap = new HashMap<>();
                            seqNumberMap = accCompanyPreferencesDAO.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, businessDate);
                            jeDataMap.put("entrynumber", (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER));
                            jeDataMap.put("autogenerated", true);
                            jeDataMap.put(Constants.SEQFORMAT, format.getID());
                            jeDataMap.put(Constants.SEQNUMBER, (String) seqNumberMap.get(Constants.SEQNUMBER));
                            jeDataMap.put(Constants.DATEPREFIX, (String) seqNumberMap.get(Constants.DATEPREFIX));
                            jeDataMap.put(Constants.DATEAFTERPREFIX, (String) seqNumberMap.get(Constants.DATEAFTERPREFIX));
                            jeDataMap.put(Constants.DATESUFFIX, (String) seqNumberMap.get(Constants.DATESUFFIX));
                            jeDataMap.put("userdf", params.opt(Constants.userdateformat));
                            jeDataMap.put("userdf", authHandler.getUserDateFormatterWithoutTimeZone(params));
                            jeDataMap.put("entrydate", businessDate);
                            jeDataMap.put("companyid", companyId);
                            jeDataMap.put("memo", "Stock Adjustment JE for QC rejected " + product.getName());
                            jeDataMap.put("createdby", params.optString("userid", ""));
                            jeDataMap.put("currencyid", params.optString("gcurrencyid", ""));
                            jeDataMap.put("transactionModuleid", Constants.Inventory_Stock_Adjustment_ModuleId);
                            jeresult = accJournalEntryDAO.saveJournalEntry(jeDataMap); // Create Journal entry without JEdetails
                            inventoryJE = (JournalEntry) jeresult.getEntityList().get(0);
                            createStockAdjustment.setInventoryJE(inventoryJE);
                            HashSet jeDetails = new HashSet();
                            JSONObject jedjson = new JSONObject();
                            jedjson.put("srno", jeDetails.size() + 1);
                            jedjson.put("companyid", companyId);
                            jedjson.put("amount", authHandler.round(((qty * productPrice) * (-1)), companyId));
                            jedjson.put("debit", false);
                            jedjson.put("accountid", product.getInventoryAccount().getID());
                            jedjson.put("jeid", inventoryJE.getID());
                            KwlReturnObject jedresult = accJournalEntryDAO.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jeDetails.add(jed);
                            jedjson = new JSONObject();
                            jedjson.put("srno", jeDetails.size() + 1);
                            jedjson.put("companyid", companyId);
                            jedjson.put("amount", authHandler.round(((qty * productPrice) * (-1)), companyId));
                            jedjson.put("accountid", product.getStockAdjustmentAccount().getID());
                            jedjson.put("debit", true);
                            jedjson.put("jeid", inventoryJE.getID());
                            jedresult = accJournalEntryDAO.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jeDetails.add(jed);
                            inventoryJE.setDetails(jeDetails);
                            accJournalEntryDAO.saveJournalEntryDetailsSet(jeDetails);
                        } else {
                            throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforJEnotset", null,locale));
                        }
                    } else {
                        throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforJEnotset", null,locale));
                    }                
                } else {
                    throw new AccountingException(messageSource.getMessage("acc.inventorysetup.productinvaccount", null,locale) + product.getProductid());
                }
                auditparams.put(Constants.useridKey,userId);
                auditparams.put(Constants.companyKey,companyId);
                auditparams.put(Constants.remoteIPAddress,remoteIP);
                auditTrailObj.insertAuditLog(AuditAction.STOCK_ADJUSTMENT_ADDED, auditmessage, auditparams, "0");
                HashMap<String, Object> requestparams = new HashMap<>();
                requestparams.put(Constants.locale, locale);
                stockAdjustmentService.requestStockAdjustment(user, createStockAdjustment, false, false, null, requestparams);
                if (inventoryJE != null) {
                    inventoryJE.setTransactionId(createStockAdjustment.getId());
                }
                seqService.updateSeqNumber(seqFormat);
            }
        } catch (Exception e) {
            Logger.getLogger(StockTransferApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, e);
            throw new InventoryException("Unable to post Stock Out JE while processing: " + e.getMessage());
        }
    }    
}
