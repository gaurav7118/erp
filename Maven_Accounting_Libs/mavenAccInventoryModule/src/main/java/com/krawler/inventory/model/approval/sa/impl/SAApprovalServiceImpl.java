/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval.sa.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.StoreMaster;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.approval.ApprovalType;
import com.krawler.inventory.model.approval.InspectionDetail;
import com.krawler.inventory.model.approval.sa.SAApproval;
import com.krawler.inventory.model.approval.sa.SAApprovalDAO;
import com.krawler.inventory.model.approval.sa.SAApprovalService;
import com.krawler.inventory.model.approval.sa.SADetailApproval;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.*;
import com.krawler.inventory.model.stockout.AdjustmentStatus;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.inventory.model.stockout.StockAdjustmentDetail;
import com.krawler.inventory.model.stockout.StockAdjustmentService;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.kwlCommonTablesDAO;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;

/**
 *
 * @author Vipin Gupta
 */
public class SAApprovalServiceImpl implements SAApprovalService {

    private SAApprovalDAO saApprovalDAO;
    private StockAdjustmentService stockAdjustmentService;
    private StockService stockService;
    private StockMovementService stockMovementService;
    private MessageSource messageSource;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    public void setKwlCommonTablesDAOObj(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }

    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setSaApprovalDAO(SAApprovalDAO saApprovalDAO) {
        this.saApprovalDAO = saApprovalDAO;
    }

    public void setStockAdjustmentService(StockAdjustmentService stockAdjustmentService) {
        this.stockAdjustmentService = stockAdjustmentService;
    }

    @Override
    public SAApproval getSAApproval(String saApprovalId) throws ServiceException {
        SAApproval saa = null;
        if (!StringUtil.isNullOrEmpty(saApprovalId)) {
            saa = saApprovalDAO.getSAApproval(saApprovalId);
        }
        return saa;
    }

    @Override
    public SADetailApproval getSADetailApproval(String saDetailApprovalId) throws ServiceException {
        SADetailApproval sada = null;
        if (!StringUtil.isNullOrEmpty(saDetailApprovalId)) {
            sada = saApprovalDAO.getSADetailApproval(saDetailApprovalId);
        }
        return sada;
    }

    @Override
    public List<SAApproval> getStockAdjutmentApprovalList(String searchString, Paging paging) throws ServiceException {
        return saApprovalDAO.getStockAdjutmentApprovalList(searchString, paging);
    }

    @Override
    public List<SADetailApproval> getStockAdjutmentDetailApprovalList(SAApproval saApproval, Paging paging) throws ServiceException {
        return saApprovalDAO.getStockAdjutmentDetailApprovalList(saApproval, paging);
    }

    public StockService getStockService() {
        return stockService;
    }

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public StockMovementService getStockMovementService() {
        return stockMovementService;
    }

    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @Override
    public void addStockoutApproval(StockAdjustment stockAdjustment, HashMap<String, Object> requestParams) throws ServiceException {
        SAApproval saApproval = new SAApproval();
        saApproval.setStockAdjustment(stockAdjustment);
        saApproval.setApprovalStatus(ApprovalStatus.PENDING);
        saApproval.setApprovalType(ApprovalType.STOCK_OUT_APPROVAL);
        String customerid = (requestParams.containsKey("customer") && requestParams.get("customer") != null) ? requestParams.get("customer").toString() : "";
        /*
         * If Customer id is present therefore saving Customer object in SAApproval table.
         */
        if (!StringUtil.isNullOrEmpty(customerid)) {
            Customer custObj = (Customer) kwlCommonTablesDAOObj.getClassObject(Customer.class.getName(), customerid);
            saApproval.setCustomer(custObj);
        }
        saApproval.setQuantity(Math.abs(stockAdjustment.getQuantity() - stockAdjustment.getFinalQuantity()));
//        Set<SADetailApproval> sadaSet = new HashSet<SADetailApproval>();
        Store qaStore = stockService.getQaStore(stockAdjustment.getCompany());
        Locale locale = null;
        if (requestParams.containsKey("locale")) {
            locale = (Locale) requestParams.get("locale");
        }
        if (qaStore.getDefaultLocation() == null) {
            throw new InventoryException(InventoryException.Type.NULL, messageSource.getMessage("acc.JE.NodefaultlocationisfoundforQAstore", null, locale));
        }

        StockMovement smApproval = new StockMovement(stockAdjustment.getProduct(), qaStore, 0, stockAdjustment.getPricePerUnit(), stockAdjustment.getTransactionNo(), stockAdjustment.getBusinessDate(), TransactionType.IN, TransactionModule.STOCK_ADJUSTMENT, stockAdjustment.getId(), stockAdjustment.getId());
        smApproval.setRemark("Stock IN send for QA Approval");
        smApproval.setStockUoM(stockAdjustment.getUom());

        StockMovement smFinal = new StockMovement(stockAdjustment.getProduct(), stockAdjustment.getStore(), 0, stockAdjustment.getPricePerUnit(), stockAdjustment.getTransactionNo(), stockAdjustment.getBusinessDate(), TransactionType.IN, TransactionModule.STOCK_ADJUSTMENT, stockAdjustment.getId(), stockAdjustment.getId());
        smFinal.setRemark("Stock IN without QA Approval");
        smFinal.setStockUoM(stockAdjustment.getUom());

        for (StockAdjustmentDetail sad : stockAdjustment.getStockAdjustmentDetail()) {
            double approvalQty = sad.getReturnQuantity();
            String approvalSerialNames = sad.getReturnSerialNames();
            if (approvalQty > 0) {
                if (stockAdjustment.getProduct().isIsSerialForProduct() && !StringUtil.isNullOrEmpty(approvalSerialNames)) {
                    String[] serialNameArr = approvalSerialNames.split(",");
                    for (int i = 0; i < serialNameArr.length; i++) {
                        SADetailApproval sada = new SADetailApproval();
                        sada.setStockAdjustmentDetail(sad);
                        sada.setApprovalStatus(ApprovalStatus.PENDING);
                        sada.setQuantity(1);
                        sada.setSaApproval(saApproval);
                        sada.setSerialName(serialNameArr[i]);
                        saApproval.getSADetailApprovalSet().add(sada);

//                        if (qaStore != null) {
//                            stockService.increaseInventory(sad.getStockAdjustment().getProduct(), qaStore, qaStore.getDefaultLocation(), sad.getBatchName(), serialNameArr[i], 1);
//                            //   stockService.updateERPInventory(true, stockAdjustment.getBusinessDate(), stockAdjustment.getProduct(), stockAdjustment.getPackaging(), stockAdjustment.getUom(), 1, stockAdjustment.getRemark());
//                            // stockService.addStockMovementForQa(sad, qaStore, TransactionType.IN, sad.getBatchName(), serialNameArr[i], 1, TransactionModule.STOCK_ADJUSTMENT, "Stock In");
//                        }
                    }
                } else {
                    SADetailApproval sada = new SADetailApproval();
                    sada.setStockAdjustmentDetail(sad);
                    sada.setApprovalStatus(ApprovalStatus.PENDING);
                    sada.setQuantity(approvalQty);
                    sada.setSaApproval(saApproval);

                    saApproval.getSADetailApprovalSet().add(sada);
//                    if (qaStore != null) {
//                        stockService.increaseInventory(sad.getStockAdjustment().getProduct(), qaStore, qaStore.getDefaultLocation(), sad.getBatchName(), null, sad.getQuantity());
//                        // stockService.updateERPInventory(true, stockAdjustment.getBusinessDate(), stockAdjustment.getProduct(), stockAdjustment.getPackaging(), stockAdjustment.getUom(), sad.getQuantity(), stockAdjustment.getRemark());
//                        // stockService.addStockMovementForQa(sad, qaStore, TransactionType.IN, sad.getBatchName(), null, sad.getQuantity(), TransactionModule.STOCK_ADJUSTMENT, "Stock In");
//                    }

                }
                StockMovementDetail smdApproval = new StockMovementDetail(smApproval, qaStore.getDefaultLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), approvalSerialNames, approvalQty);
                smdApproval.setStockMovement(smApproval);
                smApproval.getStockMovementDetails().add(smdApproval);
                smApproval.setQuantity(smApproval.getQuantity() + approvalQty);
            }
            if (sad.getFinalQuantity() > 0) {
                StockMovementDetail smdFinal = new StockMovementDetail(smFinal, sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), sad.getFinalSerialNames(), sad.getFinalQuantity());
                smdFinal.setStockMovement(smFinal);
                smFinal.getStockMovementDetails().add(smdFinal);
                smFinal.setQuantity(smFinal.getQuantity() + sad.getFinalQuantity());

            }
        }
        if (saApproval.getQuantity() > 0) {
            saApprovalDAO.saveOrUpdate(saApproval);
        }

        if (!smFinal.getStockMovementDetails().isEmpty()) {
            for (StockMovementDetail smd : smFinal.getStockMovementDetails()) {
                stockService.increaseInventory(smFinal.getProduct(), smFinal.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                stockService.updateERPInventory(true, smFinal.getTransactionDate(), smFinal.getProduct(), stockAdjustment.getPackaging(), stockAdjustment.getUom(), smd.getQuantity(), "Stock In By Stock Adjustment");
                stockMovementService.stockMovementInERP(true, smFinal.getProduct(), smFinal.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), false);
            }
            stockMovementService.addStockMovement(smFinal);
        }
        if (!smApproval.getStockMovementDetails().isEmpty()) {
            for (StockMovementDetail smd : smApproval.getStockMovementDetails()) {
                stockService.increaseInventory(smApproval.getProduct(), smApproval.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                stockMovementService.stockMovementInERP(true, smApproval.getProduct(), smApproval.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), 0, false);
            }
            stockMovementService.addStockMovement(smApproval);
        }
//        stockService.addBulkStockMovementForQa(stockAdjustment, TransactionType.IN, "Stock In", qaStore);
    }

    @Override
    public void approveStockAdjustmentDetail(User inspector, SADetailApproval saDetailApproval, InspectionDetail inspectionDetail, double quantity, boolean fromRepair) throws ServiceException {
        StockAdjustmentDetail sad = saDetailApproval.getStockAdjustmentDetail();

        double returnQty = saDetailApproval.getQuantity() - quantity;
        double adjustQty = 0;
        if (returnQty == 0) {
            adjustQty = saDetailApproval.getQuantity();
        } else if (returnQty > 0) {
            adjustQty = quantity;
            createQAReturnRequestForSA(saDetailApproval, returnQty, fromRepair);
        }
        if (returnQty >= 0) {

            saDetailApproval.setInspector(inspector);
            saDetailApproval.setInspectionDetail(inspectionDetail);

            String remark = "";
            if (fromRepair) {
                saDetailApproval.setRetQty(adjustQty);
                saDetailApproval.setRepairStatus(ApprovalStatus.REPAIRDONE);
                remark = "Stock received after repairing";
                try {

                    saDetailApproval.setRepairedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));

                } catch (ParseException ex) {
                    Logger.getLogger(SAApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SessionExpiredException ex) {
                    Logger.getLogger(SAApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                saDetailApproval.setQuantity(adjustQty);
                saDetailApproval.setApprovalStatus(ApprovalStatus.APPROVED);
                remark = "Stock added after QA Inspection";
                try {

                    saDetailApproval.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));

                } catch (ParseException ex) {
                    Logger.getLogger(SAApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SessionExpiredException ex) {
                    Logger.getLogger(SAApprovalServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Store originalStore = saDetailApproval.getStockAdjustmentDetail().getStockAdjustment().getStore();
            if (originalStore != null) {
                sad.getStockAdjustment().setStatus(AdjustmentStatus.COMPLETED);
                sad.setFinalQuantity(sad.getFinalQuantity() + adjustQty);
                sad.addFinalSerialName(saDetailApproval.getSerialName());
                sad.getStockAdjustment().setFinalQuantity(sad.getStockAdjustment().getFinalQuantity() + adjustQty);
                saApprovalDAO.saveOrUpdate(sad);
            }
            saApprovalDAO.saveOrUpdate(saDetailApproval);
            if (returnQty == 0) {
                reflectStockAfterFullApproval(inspector, saDetailApproval, fromRepair);
            }
        }
    }

    public void createQAReturnRequestForSA(SADetailApproval saDetailApproval, double quntity, boolean fromRepair) throws ServiceException {
        try {
            if (saDetailApproval != null && quntity != 0) {

                SADetailApproval SADtlAppr = new SADetailApproval();
                if (!fromRepair) {
                    SADtlAppr.setApprovalStatus(ApprovalStatus.PENDING);
                } else {
                    SADtlAppr.setApprovalStatus(ApprovalStatus.REJECTED);
                    SADtlAppr.setRepairStatus(ApprovalStatus.RETURNTOREPAIR);
                    SADtlAppr.setRetQty(quntity);
                }
                SADtlAppr.setInspectionDetail(saDetailApproval.getInspectionDetail());
                SADtlAppr.setInspector(saDetailApproval.getInspector());
                SADtlAppr.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                SADtlAppr.setMovementStatus(saDetailApproval.isMovementStatus());
                SADtlAppr.setQuantity(quntity);
//            SADtlAppr.setRemark("QA Return");
                SADtlAppr.setSaApproval(saDetailApproval.getSaApproval());
                SADtlAppr.setSerialName(saDetailApproval.getSerialName());
                SADtlAppr.setStockAdjustmentDetail(saDetailApproval.getStockAdjustmentDetail());
                saApprovalDAO.saveOrUpdate(SADtlAppr);

            }
        } catch (ParseException ex) {
            Logger.getLogger(StockMovement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockMovement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void rejectStockAdjustmentDetail(User inspector, SADetailApproval saDetailApproval, InspectionDetail inspectionDetail, double quantity, boolean fromRepair) throws ServiceException {
        try {
            double returnQty = saDetailApproval.getQuantity() - quantity;
            double adjustQty = 0;
            if (returnQty == 0) {
                adjustQty = saDetailApproval.getQuantity();
            } else if (returnQty > 0) {
                adjustQty = quantity;
                createQAReturnRequestForSA(saDetailApproval, returnQty, fromRepair);
            }
            if (returnQty >= 0) {

                saDetailApproval.setInspector(inspector);
                saDetailApproval.setInspectionDetail(inspectionDetail);

                if (fromRepair) {
                    saDetailApproval.setRetQty(adjustQty);
                    saDetailApproval.setRepairStatus(ApprovalStatus.REPAIRREJECT);
                    saDetailApproval.setRepairedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                } else {
                    saDetailApproval.setQuantity(adjustQty);
                    saDetailApproval.setApprovalStatus(ApprovalStatus.REJECTED);
                    saDetailApproval.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                }

                saApprovalDAO.saveOrUpdate(saDetailApproval);

                if (returnQty == 0) {
                    reflectStockAfterFullApproval(inspector, saDetailApproval, fromRepair);
                }
            }
        } catch (ParseException ex) {
            Logger.getLogger(StockMovement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockMovement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Map rejectedApprovedSAItemsDetail(SADetailApproval sada) throws ServiceException {
        Map map = null;
        if (sada != null) {
            map = new HashMap();

            String emailIds = "";
            String mailSeparator = ",";
            boolean isfirst = true;
            Store store = sada.getStockAdjustmentDetail().getStockAdjustment().getStore();
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
            Product product = sada.getStockAdjustmentDetail().getStockAdjustment().getProduct();
            map.put("batchName", sada.getStockAdjustmentDetail().getBatchName());
            map.put("serialNames", sada.getSerialName());
            map.put("quantity", sada.getQuantity());
            map.put("productId", (product != null ? product.getProductid() : ""));
            map.put("productName", (product != null ? product.getName() : ""));
            map.put("storeName", (store != null ? store.getFullName() : ""));
            map.put("storeManagerEmailIds", emailIds);
        }
        return map;
    }

    private void reflectStockAfterFullApproval(User inspector, SADetailApproval saDetailApproval, boolean fromRepair) throws ServiceException {
        try {
            SAApproval saApproval = saDetailApproval.getSaApproval();
            List<SADetailApproval> saDetailApprovalList = getStockAdjutmentDetailApprovalList(saApproval, null);
            boolean approvalDone = true;
            boolean isCompleted = true;
            for (SADetailApproval sadApproval : saDetailApprovalList) {
                if (sadApproval.getApprovalStatus() == ApprovalStatus.PENDING) {
                    approvalDone = false;
                    break;
                }
            }
            if (approvalDone && !fromRepair) {
                saApproval.setApprovalStatus(ApprovalStatus.DONE);
                saApproval.setInspector(inspector);
                saApprovalDAO.saveOrUpdate(saApproval);
//            reflectStock(saApproval);     
            }
            for (SADetailApproval sadApproval : saDetailApprovalList) {
                if (sadApproval.getApprovalStatus() == ApprovalStatus.PENDING || sadApproval.getRepairStatus() == ApprovalStatus.REPAIRPENDING || sadApproval.getRepairStatus() == ApprovalStatus.RETURNTOREPAIR) {
                    isCompleted = false;
                    break;
                }
            }
            if (isCompleted) {
                StockAdjustment sa = saApproval.getStockAdjustment();
                if (sa == null) {
                    throw new InventoryException(InventoryException.Type.NULL, "Stock Adjustment is null");
                }
                if (sa.getStockAdjustmentDetail().isEmpty()) {
                    throw new InventoryException(InventoryException.Type.NULL, "Stock Adjustment detail is empty");
                }
                if (sa.getCreator() == null) {
                    sa.setCreator(saApproval.getInspector());
                    long creationDate = System.currentTimeMillis();
                    sa.setCreationdate(creationDate);
                    sa.setCreatedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                }
                sa.setUom(sa.getProduct().getUnitOfMeasure());
                sa.setModifier(saApproval.getInspector());
                sa.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
                sa.setStatus(AdjustmentStatus.COMPLETED);
                saApprovalDAO.saveOrUpdate(sa);
            }
        } catch (ParseException ex) {
            Logger.getLogger(StockMovement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(StockMovement.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void reflectStock(SAApproval saApproval) throws ServiceException {
        double totalQuantity = 0;
        for (SADetailApproval sada : saApproval.getSADetailApprovalSet()) {
            StockAdjustmentDetail sad = sada.getStockAdjustmentDetail();
            double qty = sada.getQuantity();
            if (sada.getApprovalStatus() == ApprovalStatus.APPROVED) {
//                sad.setFinalQuantity(sad.getFinalQuantity() + qty);
//                sad.addFinalSerialName(sada.getSerialName());
                totalQuantity += qty;
            } else {
                sad.setFinalQuantity(sad.getFinalQuantity() - qty);
                sad.removeSerialNameFromFinal(sada.getSerialName());
            }
            saApprovalDAO.saveOrUpdate(sad);
        }
        StockAdjustment sa = saApproval.getStockAdjustment();
        if (sa.getQuantity() < 0) {
            totalQuantity = -totalQuantity;
        }
        sa.setFinalQuantity(totalQuantity);
        saApprovalDAO.saveOrUpdate(sa);

        stockAdjustmentService.approveStockAdjustment(saApproval.getInspector(), sa);
    }

    @Override
    public int getAttachmentCount(Company company, String moduleWiseMainId) throws ServiceException {
        return saApprovalDAO.getAttachmentCount(company, moduleWiseMainId);
    }

    @Override
    public void createStockMovementForQAApproval(Company company, List<SADetailApproval> approvedRejectedRecords, Store qaStore, Store repairStore) throws ServiceException {
        if (qaStore == null && repairStore == null) {
            qaStore = stockService.getQaStore(company);
            repairStore = stockService.getRepairStore(company);
        }
        if (qaStore == null || repairStore == null) {
            throw new InventoryException("QA Store OR Repairing store are not set");
        }
        if (approvedRejectedRecords != null && !approvedRejectedRecords.isEmpty()) {
            Map<String, StockMovement> stockMovementQAMap = new HashMap(); // for qa store
            Map<String, StockMovement> stockMovementSAMap = new HashMap(); // for Stock Adjustment Store
            Map<String, StockMovement> stockMovementRepMap = new HashMap(); // for Repairing Store

            Map<String, StockMovementDetail> qasmdMap = new HashMap();
            Map<String, StockMovementDetail> insmdMap = new HashMap();
            for (SADetailApproval sada : approvedRejectedRecords) {
                ApprovalStatus approvalStatus = sada.getApprovalStatus();
                StockAdjustment sa = sada.getStockAdjustmentDetail().getStockAdjustment();
                StockAdjustmentDetail sad = sada.getStockAdjustmentDetail();
                Product product = sa.getProduct();
                StoreMaster row = sad.getRow();
                StoreMaster rack = sad.getRack();
                StoreMaster bin = sad.getBin();
                String batchName = sad.getBatchName();
                String serialName = sada.getSerialName();
                String memo = sa.getMemo();
                double qty = sada.getQuantity();
                StockMovement qasm;
                if (stockMovementQAMap.containsKey(sa.getId())) {
                    qasm = stockMovementQAMap.get(sa.getId());
                } else {
                    qasm = new StockMovement(product, qaStore, 0, sa.getPricePerUnit(), sa.getTransactionNo(), new Date(), TransactionType.OUT, TransactionModule.STOCK_ADJUSTMENT, sa.getId(), sa.getId());
                    qasm.setRemark(approvalStatus == ApprovalStatus.APPROVED ? "Stock Approved by QA" : "Stock Rejected by QA");
                    qasm.setStockUoM(sa.getUom());
                    qasm.setCostCenter(sa.getCostCenter());
                    stockMovementQAMap.put(sa.getId(), qasm);

                }
                String key = sa.getId() + qaStore.getDefaultLocation().getId()
                        + (row != null ? row.getId() : "")
                        + (rack != null ? rack.getId() : "")
                        + (bin != null ? bin.getId() : "")
                        + (batchName != null ? batchName : "");
                if (qasmdMap.containsKey(key)) {
                    StockMovementDetail qasmd = qasmdMap.get(key);
                    qasmd.setQuantity(qasmd.getQuantity() + qty);
                    qasmd.addSerialName(serialName);
                } else {
                    StockMovementDetail qasmd = new StockMovementDetail(qasm, qaStore.getDefaultLocation(), row, rack, bin, batchName, serialName, qty);
                    qasm.getStockMovementDetails().add(qasmd);
                    qasmdMap.put(key, qasmd);
                }
                qasm.setQuantity(qasm.getQuantity() + qty);
                qasm.setMemo(memo);

                Location location = null;
                StockMovement insm = null;
                if (approvalStatus == ApprovalStatus.APPROVED) {
                    Store store = sa.getStore();
                    location = sad.getLocation();
                    if (stockMovementSAMap.containsKey(sa.getId())) {
                        insm = stockMovementSAMap.get(sa.getId());
                    } else {
                        insm = new StockMovement(sa.getProduct(), store, 0, sa.getPricePerUnit(), sa.getTransactionNo(), new Date(), TransactionType.IN, TransactionModule.STOCK_ADJUSTMENT, sa.getId(), sa.getId());
                        insm.setRemark(approvalStatus == ApprovalStatus.APPROVED ? "Stock Approved by QA" : "Stock Rejected by QA");
                        insm.setStockUoM(sa.getUom());
                        insm.setCostCenter(sa.getCostCenter());
                        stockMovementSAMap.put(sa.getId(), insm);
                    }
                } else if (approvalStatus == ApprovalStatus.REJECTED) {
                    Store store = repairStore;
                    location = repairStore.getDefaultLocation();
                    if (stockMovementRepMap.containsKey(sa.getId())) {
                        insm = stockMovementRepMap.get(sa.getId());
                    } else {
                        insm = new StockMovement(sa.getProduct(), store, 0, sa.getPricePerUnit(), sa.getTransactionNo(), new Date(), TransactionType.IN, TransactionModule.STOCK_ADJUSTMENT, sa.getId(), sa.getId());
                        insm.setRemark(approvalStatus == ApprovalStatus.APPROVED ? "Stock Approved by QA" : "Stock Rejected by QA");
                        insm.setStockUoM(sa.getUom());
                        insm.setCostCenter(sa.getCostCenter());
                        stockMovementRepMap.put(sa.getId(), insm);
                    }
                }
                if (insm != null && location != null) {
                    key = insm.getId() + location.getId()
                            + (row != null ? row.getId() : "")
                            + (rack != null ? rack.getId() : "")
                            + (bin != null ? bin.getId() : "")
                            + (batchName != null ? batchName : "");
                    if (insmdMap.containsKey(key)) {
                        StockMovementDetail insmd = insmdMap.get(key);
                        insmd.setQuantity(insmd.getQuantity() + qty);
                        insmd.addSerialName(serialName);
                    } else {
                        StockMovementDetail insmd = new StockMovementDetail(insm, location, row, rack, bin, batchName, serialName, qty);
                        insm.getStockMovementDetails().add(insmd);
                        insmdMap.put(key, insmd);
                    }
                    insm.setQuantity(insm.getQuantity() + qty);
                    insm.setMemo(memo);
                }
            }

            List<StockMovement> smList = new ArrayList<>(); // Stock movements without SA because SA needs stock movement in ERP
            smList.addAll(stockMovementQAMap.values());
            smList.addAll(stockMovementRepMap.values());

            for (StockMovement sm : smList) {
                if (!sm.getStockMovementDetails().isEmpty()) {
                    for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                        if (sm.getTransactionType() == TransactionType.IN) {
                            stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
//                            stockService.updateERPInventory(true, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(true, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), 0, false);
                        } else {
                            stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
//                            stockService.updateERPInventory(false, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(true, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), 0, false);
                        }
                    }
                    stockMovementService.addStockMovement(sm);
                }
            }
            for (StockMovement sm : stockMovementSAMap.values()) {
                if (!sm.getStockMovementDetails().isEmpty()) {
                    for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                        if (sm.getTransactionType() == TransactionType.IN) {
                            stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                            stockService.updateERPInventory(true, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(true, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), false);
                        } else {
                            stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                            stockService.updateERPInventory(false, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(false, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), false);
                        }
                    }
                    stockMovementService.addStockMovement(sm);
                }
            }
        }
    }

    @Override
    public void createStockMovementForRepairing(Company company, List<SADetailApproval> approvedRejectedRecords, Store repairStore) throws ServiceException {
        if (repairStore == null) {
            repairStore = stockService.getRepairStore(company);
        }
        if (repairStore == null) {
            throw new InventoryException("Repairing store is not set");
        }
        if (approvedRejectedRecords != null && !approvedRejectedRecords.isEmpty()) {
            Map<String, StockMovement> stockMovementSAMap = new HashMap(); // for Stock Adjustment Store
            Map<String, StockMovement> stockMovementRepMap = new HashMap(); // for Repairing Store
            for (SADetailApproval sada : approvedRejectedRecords) {
                ApprovalStatus repairingStatus = sada.getRepairStatus();
                StockAdjustment sa = sada.getStockAdjustmentDetail().getStockAdjustment();
                StockAdjustmentDetail sad = sada.getStockAdjustmentDetail();
                StockMovement repsm;
                if (stockMovementRepMap.containsKey(sa.getId())) {
                    repsm = stockMovementRepMap.get(sa.getId());
                } else {
                    repsm = new StockMovement(sa.getProduct(), repairStore, 0, sa.getPricePerUnit(), sa.getTransactionNo(), new Date(), TransactionType.OUT, TransactionModule.STOCK_ADJUSTMENT, sa.getId(), sa.getId());
                    repsm.setRemark(repairingStatus == ApprovalStatus.REPAIRDONE ? "Stock Repaired" : "Stock cannot be Repaired");
                    repsm.setStockUoM(sa.getUom());
                    repsm.setCostCenter(sa.getCostCenter());
                    stockMovementRepMap.put(sa.getId(), repsm);

                }
                StockMovementDetail qasmd = new StockMovementDetail(repsm, repairStore.getDefaultLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), sada.getSerialName(), sada.getRetQty() == 0 ? sada.getQuantity() : sada.getRetQty());
                repsm.getStockMovementDetails().add(qasmd);
                repsm.setQuantity(repsm.getQuantity() + (sada.getRetQty() == 0 ? sada.getQuantity() : sada.getRetQty()));

                Location location = null;
                StockMovement insm = null;
                if (repairingStatus == ApprovalStatus.REPAIRDONE) {
                    location = sad.getLocation();
                    if (stockMovementSAMap.containsKey(sa.getId())) {
                        insm = stockMovementSAMap.get(sa.getId());
                    } else {
                        insm = new StockMovement(sa.getProduct(), sa.getStore(), 0, sa.getPricePerUnit(), sa.getTransactionNo(), new Date(), TransactionType.IN, TransactionModule.STOCK_ADJUSTMENT, sa.getId(), sa.getId());
                        insm.setRemark(repairingStatus == ApprovalStatus.REPAIRDONE ? "Stock Repaired" : "Stock cannot be Repaired");
                        insm.setStockUoM(sa.getUom());
                        insm.setCostCenter(sa.getCostCenter());
                        stockMovementSAMap.put(sa.getId(), insm);
                    }
                }
                if (insm != null && location != null) {
                    StockMovementDetail insmd = new StockMovementDetail(insm, location, sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName(), sada.getSerialName(), sada.getRetQty() == 0 ? sada.getQuantity() : sada.getRetQty());
                    insm.getStockMovementDetails().add(insmd);
                    insm.setQuantity(insm.getQuantity() + (sada.getRetQty() == 0 ? sada.getQuantity() : sada.getRetQty()));
                }
            }

            for (StockMovement sm : stockMovementRepMap.values()) {
                if (!sm.getStockMovementDetails().isEmpty()) {
                    for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                        if (sm.getTransactionType() == TransactionType.IN) {
                            stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
//                            stockService.updateERPInventory(true, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                        } else {
                            stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
//                            stockService.updateERPInventory(false, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                        }
                    }
                    stockMovementService.addStockMovement(sm);
                }
            }
            for (StockMovement sm : stockMovementSAMap.values()) {
                if (!sm.getStockMovementDetails().isEmpty()) {
                    for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                        if (sm.getTransactionType() == TransactionType.IN) {
                            stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                            stockService.updateERPInventory(true, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(true, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), false);
                        } else {
                            stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                            stockService.updateERPInventory(false, sm.getTransactionDate(), sm.getProduct(), null, sm.getStockUoM(), smd.getQuantity(), "");
                            stockMovementService.stockMovementInERP(false, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), false);
                        }
                    }
                    stockMovementService.addStockMovement(sm);
                }
            }
        }
    }
}
