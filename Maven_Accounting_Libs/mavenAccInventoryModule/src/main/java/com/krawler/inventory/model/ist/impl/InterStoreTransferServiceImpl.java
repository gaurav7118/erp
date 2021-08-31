/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.ist.impl;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.CostCenter;
import com.krawler.common.admin.DocumentEmailSettings;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.NewBatchSerial;
import com.krawler.common.admin.NewProductBatch;
import com.krawler.common.admin.StoreMaster;
import com.krawler.common.admin.User;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.exception.NegativeInventoryException;
import com.krawler.inventory.model.approval.consignment.ConsignmentApprovalDetails;
import com.krawler.inventory.model.approval.consignmentservice.ConsignmentService;
import com.krawler.inventory.model.approval.sa.SADetailApproval;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferApprovalService;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferDetailApproval;
import com.krawler.inventory.model.configuration.InventoryConfigService;
import com.krawler.inventory.model.ist.*;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.packaging.PackagingService;
import com.krawler.inventory.model.sequence.ModuleConst;
import com.krawler.inventory.model.sequence.SeqFormat;
import com.krawler.inventory.model.sequence.SeqService;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.*;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.inventory.model.stockrequest.StockRequest;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.inventory.view.GoodsTransferController;
import com.krawler.inventory.view.StockAdjustmentController;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author Vipin Gupta
 */
public class InterStoreTransferServiceImpl implements InterStoreTransferService {

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private HibernateTransactionManager txnManager;
    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;
     private static final Logger lgr = Logger.getLogger(GoodsTransferController.class.getName());
    private InterStoreTransferDAO istDAO;
    private InventoryConfigService invConfigService;
    private StockService stockService;
    private LocationService locationService;
    private PackagingService packagingService;
    private StoreService storeService;
    private SeqService seqService;
    private ConsignmentService consignmentService;
    private StockMovementService stockMovementService;
    private StockMovementDAO stockMovementDAO;
    private accProductDAO accProductObj;
    private StockTransferApprovalService approvalService;
    private AccountingHandlerDAO accountingHandlerDAO;
    private com.krawler.spring.common.fieldDataManager fieldDataManagercntrl;

    public void setIstDAO(InterStoreTransferDAO istDAO) {
        this.istDAO = istDAO;
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

    public void setSeqService(SeqService seqService) {
        this.seqService = seqService;
    }

    public void setConsignmentService(ConsignmentService consignmentService) {
        this.consignmentService = consignmentService;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
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

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setFieldDataManager(com.krawler.spring.common.fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    public void setStockMovementDAO(StockMovementDAO stockMovementDAO) {
        this.stockMovementDAO = stockMovementDAO;
    }

    public void setAccProductObj(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    
    @Override
    public JSONObject saveInterStoreTransferRequest(JSONObject paramJobj) {

        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "", billid = "";
        String detailId="";
        try {
            String records = paramJobj.optString("str");
            String transactionNo = paramJobj.optString("transactionno");
            String seqFormatId = paramJobj.optString("seqFormatId");
            String documentNumber = paramJobj.optString("documentNumber");
            String isqarejected = paramJobj.optString("isqarejected");
            String challanno = paramJobj.optString("challanno");
            boolean isJobWorkStockOut = !StringUtil.isNullOrEmpty(paramJobj.optString("isJobWorkStockOut")) ? Boolean.parseBoolean(paramJobj.optString("isJobWorkStockOut")) : false;
            String fromStoreId = paramJobj.optString("fromstore");
            String toStoreId = paramJobj.optString("tostore");
            String companyid = paramJobj.optString(Constants.companyid);
            String memo = !StringUtil.isNullOrEmpty(paramJobj.optString("memo")) ? paramJobj.optString("memo") : "";
            boolean UomSchemaType = false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("UomSchemaType"))) {
                UomSchemaType = Boolean.parseBoolean(paramJobj.optString("UomSchemaType"));
            }
            Map requestParams = new HashMap<>();
            requestParams.put(Constants.companyid, companyid);
            requestParams.put(Constants.moduleid, Constants.Acc_InterStore_ModuleId);
            if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.customfield))) {
                requestParams.put(Constants.customfield, paramJobj.optString(Constants.customfield));//Global level Custom data
            }
            Store fromStore = storeService.getStoreById(fromStoreId);
            Store toStore = storeService.getStoreById(toStoreId);

            JSONArray jArr = new JSONArray(records);
            /**
             * save Challan number data
             */
            ChallanNumber challanNumber = null;
            boolean seqCheckedWhileImport=false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("seqCheckedWhileImport"))) {
                seqCheckedWhileImport = Boolean.parseBoolean(paramJobj.optString("seqCheckedWhileImport"));
            }
            
            if (!StringUtil.isNullOrEmpty(challanno)) {
                requestParams.put("challanno", challanno);
                requestParams.put("company", fromStore.getCompany());
                KwlReturnObject kwlReturnObject = saveChallanNumber(requestParams);
                challanNumber = kwlReturnObject.getEntityList().size() > 0 ? (ChallanNumber) kwlReturnObject.getEntityList().get(0) : null;
            }

            boolean isQARejected = false;
            if ("true".equals(isqarejected)) {
                isQARejected = true;
            }
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyid);
            Company company = (Company) jeresult.getEntityList().get(0);
            SeqFormat seqFormat = null;
            if (!"NA".equals(seqFormatId)) {
                if (!StringUtil.isNullOrEmpty(seqFormatId)) {
                    seqFormat = seqService.getSeqFormat(seqFormatId);
                } else {
                    seqFormat = seqService.getDefaultSeqFormat(company, ModuleConst.INTER_STORE_TRANSFER);
                }
            }

            boolean allowNegativeInventory = false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("allowNegativeInventory"))) {
                allowNegativeInventory = Boolean.parseBoolean(paramJobj.optString("allowNegativeInventory"));
            }
            synchronized (this) {
                boolean seqExist = false;
                if ("NA".equals(seqFormatId) && !seqCheckedWhileImport) {
                    transactionNo = documentNumber;
                    seqExist = seqService.isExistingSeqNumber(transactionNo, company, ModuleConst.INTER_STORE_TRANSFER);
                    if (seqExist) {
                        throw new InventoryException("Sequence number already exist, please enter other one.");
                    }
                } else if(seqFormat!=null){
                    seqExist = false;
                    do {
                        transactionNo = seqService.getNextFormatedSeqNumber(seqFormat);
                        seqExist = seqService.isExistingSeqNumber(transactionNo, company, ModuleConst.INTER_STORE_TRANSFER);
                        if (seqExist) {
                            seqService.updateSeqNumber(seqFormat);
                        }
                    } while (seqExist);
                }
                transactionNo = seqFormat != null ? seqService.getNextFormatedSeqNumber(seqFormat) : documentNumber;
                if (!seqExist) {
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject jObj = jArr.optJSONObject(i);
                        String itemId = jObj.optString("itemid");

                        String businessStrDate = jObj.optString("businessdate");
                        String costCenterId = jObj.optString("costcenter");
                        String podid = jObj.optString("podid");
                        double confactor = jObj.has("confactor") ? jObj.optDouble("confactor", 1) : 1;
                        Date businessDate = null;
                        CostCenter costCenter = null;
                        if (!StringUtil.isNullOrEmpty(costCenterId)) {
                            jeresult = accountingHandlerDAO.getObject(CostCenter.class.getName(), costCenterId);
                            costCenter = (CostCenter) jeresult.getEntityList().get(0);
                        }
                        try {
                            businessDate = df.parse(businessStrDate);
                        } catch (ParseException ex) {
                        }
                        String uomId = jObj.optString("uom");
                        String uomName = jObj.optString("uomname");
                        String packagingId = jObj.optString("packaging");
                        jeresult = accountingHandlerDAO.getObject(Product.class.getName(), itemId);
                        Product product = (Product) jeresult.getEntityList().get(0);

                        UnitOfMeasure uom = null;
                        int moduleid = (int) requestParams.get("moduleid");
                        if (moduleid == Constants.Acc_InterStore_ModuleId) {
                            uomId = uomName;
                        }
                        jeresult = accountingHandlerDAO.getObject(UnitOfMeasure.class.getName(), uomId);
                        uom = (UnitOfMeasure) jeresult.getEntityList().get(0);
                        if (uom == null && product.getTransferUOM() != null) {
                            jeresult = accountingHandlerDAO.getObject(UnitOfMeasure.class.getName(), product.getTransferUOM().getID());
                            uom = (UnitOfMeasure) jeresult.getEntityList().get(0);
                        }
                        Packaging packaging = null;
                        if (uom == product.getUnitOfMeasure() && !StringUtil.isNullOrEmpty(packagingId)) {
                            packaging = packagingService.getPackaging(packagingId);
                        } else {
                            if (UomSchemaType) {
                                packaging = new Packaging();
                                packaging.setCompany(company);
                                packaging.setInnerUoM(uom);
                                packaging.setInnerUomValue(1);
                                packaging.setStockUoM(product.getUnitOfMeasure());
                                packaging.setStockUomValue(confactor);
                                packagingService.addOrUpdatePackaging(packaging);
                            } else {
                                packaging = packagingService.getPackaging(packagingId);
                            }
                        }
                        double qty = authHandler.roundQuantity((jObj.optDouble("quantity", 0)), company.getCompanyID());
                        String remark = jObj.getString("remark");
                        InterStoreTransferRequest interStoreTransfer = new InterStoreTransferRequest(product, fromStore, toStore, uom);
                        interStoreTransfer.setOrderedQty(qty);
                        interStoreTransfer.setRemark(remark);
                        interStoreTransfer.setTransactionNo(transactionNo);
                        interStoreTransfer.setBusinessDate(businessDate);
                        interStoreTransfer.setCostCenter(costCenter);
                        interStoreTransfer.setPackaging(packaging);
                        interStoreTransfer.setUom(uom);
                        interStoreTransfer.setIsJobWorkStockTransfer(isJobWorkStockOut);
                        if (!StringUtil.isNullOrEmpty(podid)) {
                            interStoreTransfer.setPurchaseOrderDetail(podid);
                        }
                        if (!StringUtil.isNullObject(challanNumber)) {
                            interStoreTransfer.setChallanNumber(challanNumber);
                        }
                        interStoreTransfer.setMemo(memo);
                        Set<ISTDetail> istdSet = new HashSet<ISTDetail>();
                        JSONArray stockDetails = jObj.optJSONArray("stockDetails");
                        for (int x = 0; x < stockDetails.length(); x++) {
                            JSONObject detailObj = stockDetails.optJSONObject(x);
                            String issuedLocationId = detailObj.optString("locationId");
                            String issuedRowId = detailObj.optString("rowId");
                            String issuedRackId = detailObj.optString("rackId");
                            String issuedBinId = detailObj.optString("binId");
                            String batchName = detailObj.optString("batchName");
                            String issuedSerialNames = detailObj.optString("serialNames");
                            double quantity = authHandler.roundQuantity((detailObj.optDouble("quantity")), company.getCompanyID());

                            Location issuedLocation = locationService.getLocation(issuedLocationId);

                            StoreMaster issuedRow = null;
                            if (product.isIsrowforproduct()) {
                                issuedRow = storeService.getStoreMaster(issuedRowId);
                            }
                            StoreMaster issuedRack = null;
                            if (product.isIsrackforproduct()) {
                                issuedRack = storeService.getStoreMaster(issuedRackId);
                            }
                            StoreMaster issuedBin = null;
                            if (product.isIsbinforproduct()) {
                                issuedBin = storeService.getStoreMaster(issuedBinId);
                            }
                            ISTDetail istd = new ISTDetail();
                            istd.setIstRequest(interStoreTransfer);
                            istd.setIssuedLocation(issuedLocation);
                            istd.setIssuedRow(issuedRow);
                            istd.setIssuedRack(issuedRack);
                            istd.setIssuedBin(issuedBin);
                            istd.setBatchName(batchName);
                            istd.setIssuedSerialNames(issuedSerialNames);
                            istd.setIssuedQuantity(quantity);
                            if (interStoreTransfer.isIsJobWorkStockTransfer()) {
                                /**
                                 * If Record is JOb Work Out
                                 */
                                istd.setDeliveredLocation(issuedLocation);
                                istd.setDeliveredRow(issuedRow);
                                istd.setDeliveredRack(issuedRack);
                                istd.setDeliveredBin(issuedBin);
                                istd.setBatchName(batchName);
                                istd.setDeliveredSerialNames(issuedSerialNames);
                                istd.setDeliveredQuantity(quantity);
                            }
                            istdSet.add(istd);

                            checkItemAlreadyProcessed(isQARejected, detailObj.optString("moduletype"), detailObj.optString("qaapprovaldetailid"));
                        }
                        interStoreTransfer.setIstDetails(istdSet);

                        requestParams.put(Constants.LineLevelCustomData, jObj.optString(Constants.LineLevelCustomData));//Line Level Custom data
                        jeresult = accountingHandlerDAO.getObject(User.class.getName(), paramJobj.optString(Constants.useridKey));
                        User user = (User) jeresult.getEntityList().get(0);
                        addInterStoreTransferRequest(user, interStoreTransfer, allowNegativeInventory, requestParams);
                        if (interStoreTransfer.isIsJobWorkStockTransfer()) {
                            /**
                             * If Record is JOb Work Out
                             */
                            interStoreTransfer.setAcceptedQty(qty);
                            acceptInterStoreTransferRequest(user, interStoreTransfer);
                        }
                        billid = interStoreTransfer.getId();
                        
                        Set<ISTDetail>  istd =interStoreTransfer.getIstDetails();
                        for (ISTDetail istDetailvalue : istd) {
                            detailId=istDetailvalue.getId();
                        }
                       
                        // code for send mail notification when item qty goes below than reorder level.
//                    ExtraCompanyPreferences extraCompanyPreferences = null;
//                    KwlReturnObject extraprefresult = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
//                    extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                        DocumentEmailSettings documentEmailSettings = null;
                        KwlReturnObject documentEmailresult = accountingHandlerDAO.getObject(DocumentEmailSettings.class.getName(), company.getCompanyID());
                        documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;

                        if (documentEmailSettings.isQtyBelowReorderLevelMail()) {
                            for (ISTDetail istdetail : interStoreTransfer.getIstDetails()) {
                                double availableQtyinStore = stockService.getProductQuantityInStore(interStoreTransfer.getProduct(), interStoreTransfer.getFromStore());
                                if (availableQtyinStore < interStoreTransfer.getProduct().getReorderLevel()) {
                                    HashMap<String, String> data = new HashMap<String, String>();
                                    data.put("productName", interStoreTransfer.getProduct().getProductName());
                                    data.put("storeId", interStoreTransfer.getFromStore().getId());
                                    data.put("availableQty", Double.toString(availableQtyinStore));
                                    accountingHandlerDAO.sendReorderLevelEmails(user.getUserID(), null, TransactionModule.INTER_STORE_TRANSFER.toString(), data);
                                }
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(auditMessage)) {
                            auditMessage += ", ";
                        }
                        auditMessage += "(Product :" + product.getProductid() + ", Quantity :" + qty + " " + interStoreTransfer.getUom().getNameEmptyforNA() + ")";

                    }
                }
                if (!"NA".equals(seqFormatId)) {
                    seqService.updateSeqNumber(seqFormat);
                }
            }

            issuccess = true;
            msg = messageSource.getMessage("acc.stockrequest.InterStoreStockTransferRequest", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " : <b>" + transactionNo + "</b> " + messageSource.getMessage("acc.stockrequest.hasbeenaddedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
            jobj.put("ISTNoteNo", transactionNo);
            jobj.put("billid", billid);//for document designer
            jobj.put("datailId",detailId);

            auditMessage = "User " + paramJobj.optString(Constants.userfullname) + " has created Inter Store Transfer " + (isQARejected ? "for rejected stock" : "") + ": " + transactionNo + ", Store: from(" + fromStore.getAbbreviation() + " to " + toStore.getAbbreviation() + "), " + auditMessage;
            Map<String, Object> auditRequestParams = new HashMap<String, Object>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
            auditTrailObj.insertAuditLog(AuditAction.INTER_STORE_REQUEST_ADDED, auditMessage, auditRequestParams, "0");

            txnManager.commit(status);

            TransactionStatus statusforBlockSOQty = txnManager.getTransaction(def);
            try {
                KwlReturnObject retObj = consignmentService.assignStockToPendingConsignmentRequests(paramJobj);
                txnManager.commit(statusforBlockSOQty);
            } catch (Exception ex) {
                txnManager.rollback(statusforBlockSOQty);
                Logger.getLogger(StockAdjustmentController.class.getName()).log(Level.SEVERE, null, ex);
            }
//        }catch (SessionExpiredException ex) {
//            txnManager.rollback(status);
//            msg = ex.getMessage();
//            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (NegativeInventoryException ex) {
            txnManager.rollback(status);
            try {
                if (ex.getType() == NegativeInventoryException.Type.BLOCK) {
                    jobj.put("currentInventoryLevel", "block");
                } else if (ex.getType() == NegativeInventoryException.Type.WARN) {
                    jobj.put("currentInventoryLevel", "warn");
                }
                msg = ex.getMessage();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            if (msg == null) {
                msg = "Error occurred while processing";
            }
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }

        return jobj;
    };
    
    public JSONObject acceptInterStoreTransferRequest(JSONObject paramJobj){
        JSONObject returnObj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try{
            String userId = paramJobj.optString(Constants.useridKey);
            StringBuilder productIds = new StringBuilder();
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            
             String data = paramJobj.optString("jsondata");
            String collToDfLocation = paramJobj.optString("colltoDfLocation");
//            JSONArray dataArr = new JSONArray(data);
            JSONArray dataArr = new JSONArray("["+data+"]");
            for (int idx = 0; idx < dataArr.length(); idx++) {
            
                JSONObject srObject = dataArr.optJSONObject(idx);
                String reqId = srObject.optString("id");
                double qty = srObject.optDouble("quantity", 0);
                String remark = paramJobj.optString("remark");

                InterStoreTransferRequest ist = getInterStoreTransferById(reqId);
                remark=(!StringUtil.isNullOrEmpty(remark)?remark:ist.getRemark());
                ist.setRemark(remark);
                ist.setAcceptedQty(qty);

            Product product = ist.getProduct();
            JSONArray stockDetails = srObject.optJSONArray("stockDetails");
            for (int x = 0; x < stockDetails.length(); x++) {
                JSONObject detailObj = stockDetails.optJSONObject(x);
                String detailId = detailObj.optString("detailId");
                String collectedLocationId = detailObj.optString("locationId");
                String collectedRowId = detailObj.optString("rowId");
                String collectedRackId = detailObj.optString("rackId");
                String collectedBinId = detailObj.optString("binId");
                String batchName = detailObj.optString("batchName");
                String issuedSerialNames = detailObj.optString("serialNames");
                if(!StringUtil.isNullOrEmpty(issuedSerialNames)){
                    issuedSerialNames=issuedSerialNames.replaceAll(", ", ",");
                }
                double quantity = detailObj.optDouble("quantity");

                Location deliveredLocation = locationService.getLocation(collectedLocationId);
                StoreMaster collectedRow = null;
                if (product.isIsrowforproduct()) {
                    collectedRow = storeService.getStoreMaster(collectedRowId);
                }
                StoreMaster collectedRack = null;
                if (product.isIsrackforproduct()) {
                    collectedRack = storeService.getStoreMaster(collectedRackId);
                }
                StoreMaster collectedBin = null;
                if (product.isIsbinforproduct()) {
                    collectedBin = storeService.getStoreMaster(collectedBinId);
                }

                ISTDetail istd = getISTDetailById(detailId);
                if (istd != null) {
                    istd.setIstRequest(ist);
                    istd.setDeliveredLocation(deliveredLocation);
                    istd.setDeliveredRow(collectedRow);
                    istd.setDeliveredRack(collectedRack);
                    istd.setDeliveredBin(collectedBin);
                    istd.setBatchName(batchName);
                    istd.setDeliveredSerialNames(issuedSerialNames);
                    istd.setDeliveredQuantity(quantity);
                    ist.getIstDetails().add(istd);
                }

            }
                acceptInterStoreTransferRequest(user, ist);
                if (productIds.indexOf(product.getID()) == -1) {
                    productIds.append(product.getID()).append(",");
                }
            auditMessage = "User " + user.getFullName() + " has accepted stock for IST Request: " + ist.getTransactionNo() + ", Product :" + product.getProductid() + ", Quantity :" + ist.getAcceptedQty() + " " + ist.getUom().getNameEmptyforNA() + " in Store: " + ist.getToStore().getAbbreviation() + " " + auditMessage;
             Map<String, Object> auditRequestParams = new HashMap<String, Object>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
            auditTrailObj.insertAuditLog(AuditAction.INTER_STORE_REQUEST_ACCEPTED, auditMessage, auditRequestParams, ist.getId());
            auditMessage = "";
            if (ist.getOrderedQty() > ist.getAcceptedQty()) {
                auditMessage = "User " + user.getFullName() + " has created Return IST Request R" + ist.getTransactionNo() + " for IST Request: " + ist.getTransactionNo() + ", Product :" + product.getProductid() + ", Quantity :" + (ist.getOrderedQty() - ist.getAcceptedQty()) + " " + ist.getUom().getNameEmptyforNA() + ", Store: from(" + ist.getToStore().getAbbreviation() + " to " + ist.getFromStore().getAbbreviation() + ") " + auditMessage;
                auditTrailObj.insertAuditLog(AuditAction.INTER_STORE_REQUEST_ADDED, auditMessage, auditRequestParams, ist.getId());
            }
            }
             msg = "Request has been accepted successfully";
            issuccess = true;
            returnObj.put("productIds", productIds);
            txnManager.commit(status);

            
//        }catch (SessionExpiredException ex) {
//            txnManager.rollback(status);
//            msg = ex.getMessage();
//            lgr.log(Level.SEVERE, msg, ex);
//        }
        }catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        }catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        }finally {
            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return returnObj;
    }
    
    
    public void checkItemAlreadyProcessed(boolean isQAReject, String moduleType, String qaApprovalDetailId) throws InventoryException, ServiceException {
        if (isQAReject) {
            KwlReturnObject jeresult = null;
            if ("consignment".equals(moduleType)) {
                jeresult = accountingHandlerDAO.getObject(ConsignmentApprovalDetails.class.getName(), qaApprovalDetailId);
                ConsignmentApprovalDetails consignDetails = (ConsignmentApprovalDetails) jeresult.getEntityList().get(0);

                if (consignDetails.isMovementStatus()) {
                    throw new InventoryException("This item is already processed");
                }
                consignDetails.setMovementStatus(true);

            } else if ("stockout".equals(moduleType)) {

                jeresult = accountingHandlerDAO.getObject(SADetailApproval.class.getName(), qaApprovalDetailId);
                SADetailApproval saApprovalDetail = (SADetailApproval) jeresult.getEntityList().get(0);
                if (saApprovalDetail.isMovementStatus()) {
                    throw new InventoryException("This item is already processed");
                }
                saApprovalDetail.setMovementStatus(true);
            } else if ("stockrequest".equals(moduleType) || "stocktransfer".equals(moduleType)) {

                jeresult = accountingHandlerDAO.getObject(StockTransferDetailApproval.class.getName(), qaApprovalDetailId);
                StockTransferDetailApproval stApprovalDetails = (StockTransferDetailApproval) jeresult.getEntityList().get(0);
                if (stApprovalDetails.isMovementStatus()) {
                    throw new InventoryException("This item is already processed");
                }
                stApprovalDetails.setMovementStatus(true);
            }

        }
    }
    @Override
    public void addInterStoreTransferRequest(User user, InterStoreTransferRequest ist) throws ServiceException, NegativeInventoryException {
        addInterStoreTransferRequest(user, ist, false, null);
    }

    @Override
    public void addInterStoreTransferRequest(User user, InterStoreTransferRequest ist, boolean allowNegativeInventory, Map<String, Object> requestParams) throws ServiceException, NegativeInventoryException {
        try {
            if (ist == null) {
                throw new InventoryException(InventoryException.Type.NULL, "InterStoreTransfer is null");
            }
            if (ist.getIstDetails().isEmpty()) {
                throw new InventoryException(InventoryException.Type.NULL, "Inter Store Transfer detail is empty");
            }
            if (ist.getUom() == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Please select uom for Product : " + ist.getProduct().getProductid());
            }
            if (ist.getPackaging() == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Please select packaging for Product : " + ist.getProduct().getProductid());
            }

            if (ist.getPackaging() != null) {
                Packaging packaging = packagingService.createClonePackaging(ist.getPackaging());
                ist.setPackaging(packaging);
            } else {
                Packaging packaging = packagingService.createPackagingByStockUom(ist.getProduct().getUnitOfMeasure());
                ist.setPackaging(packaging);
            }
            //        if (ist.getProduct().getTransferUOM() != null) {
            //            ist.setUom(ist.getProduct().getTransferUOM());
            //        }
            ist.setCreatedBy(user);
            ist.setCreatedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long creationDate = System.currentTimeMillis();
            ist.setCreationdate(creationDate);
            ist.setModifiedBy(user);
            ist.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long modifiedDate = System.currentTimeMillis();
            ist.setModifieddate(modifiedDate);
            ist.setStatus(InterStoreTransferStatus.INTRANSIT);

            istDAO.saveOrUpdate(ist);

            /**
             * Save DO details and Inter Store Mapping
             */
            if (requestParams.containsKey("dod") && requestParams.get("dod") != null && !requestParams.containsKey(Constants.CREATE_IST_FOR_QC_DELIVERYORDER)) {
                String dod = (String) requestParams.get("dod");
                Map<String, Object> mappingParams = new HashMap();
                mappingParams.put("dod", dod);
                mappingParams.put("ist", ist);
                if (requestParams.containsKey("dodqcistmapping") && requestParams.get("dodqcistmapping") != null) {
                    mappingParams.put("dodqcistmapping", requestParams.get("dodqcistmapping"));
                }
                if (requestParams.containsKey("pickRejectedDODQCISTMapping") && requestParams.get("pickRejectedDODQCISTMapping") != null) {
                    mappingParams.put("pickRejectedDODQCISTMapping", requestParams.get("pickRejectedDODQCISTMapping"));
                }
                DeliveryDetailInterStoreLocationMapping deliveryDetailInterStoreLocationMapping = saveDODISTMapping(mappingParams);
                if (requestParams.containsKey("dodQCIstMappingID") && requestParams.get("dodQCIstMappingID") != null && !requestParams.containsKey(Constants.CREATE_PICK_IST_FOR_APPROVE_DELIVERYORDER_FROM_REPAIR_STORE)) {
                    /**
                     * Approved from QA store and Pick Pack & Ship flow is
                     * activated then update mapping in DODQCISTMapping.
                     */
                    JSONObject json = getDODQCISTMappingJSONMap(requestParams);
                    json.put("pickedMapping", deliveryDetailInterStoreLocationMapping);
                    istDAO.saveDODQCISTMapping(json);
                } else if (requestParams.containsKey(Constants.CREATE_PICK_IST_FOR_APPROVE_DELIVERYORDER_FROM_REPAIR_STORE) && requestParams.get(Constants.CREATE_PICK_IST_FOR_APPROVE_DELIVERYORDER_FROM_REPAIR_STORE) != null) {
                    /**
                     * Approved from Repair store and Pick Pack & Ship flow is
                     * activated then update mapping in RejectedDODQCISTMapping.
                     */
                    JSONObject repairParams = new JSONObject();
                    repairParams.put("repairid", requestParams.get("repairid"));
                    if (requestParams.containsKey("quantityDue") && requestParams.get("quantityDue") != null) {
                        repairParams.put("quantityDue", (Double) requestParams.get("quantityDue"));
                    }
                    if (requestParams.containsKey("repairedQty") && requestParams.get("repairedQty") != null) {
                        repairParams.put("repairedQty", (Double) requestParams.get("repairedQty"));
                    }
                    if (requestParams.containsKey("pickedQty") && requestParams.get("pickedQty") != null) {
                        repairParams.put("pickedQty", (Double) requestParams.get("pickedQty"));
                    }
                    if (requestParams.containsKey("rejectQuantity") && requestParams.get("rejectQuantity") != null) {
                        repairParams.put("quantity", (Double) requestParams.get("rejectQuantity"));
                    }
                    if (requestParams.containsKey("approvedStockOut") && requestParams.get("approvedStockOut") != null) {
                        repairParams.put("approvedStockOut", requestParams.get("approvedStockOut"));
                    }
                    if (requestParams.containsKey("rejectedStockOut") && requestParams.get("rejectedStockOut") != null) {
                        repairParams.put("rejectedStockOut", requestParams.get("rejectedStockOut"));
                    }
                    repairParams.put("pickedMapping", deliveryDetailInterStoreLocationMapping);
                    istDAO.saveRejectedDODQCISTMapping(repairParams);
                }
            } else if (requestParams.containsKey(Constants.CREATE_IST_FOR_QC_DELIVERYORDER) && requestParams.get(Constants.CREATE_IST_FOR_QC_DELIVERYORDER) != null) {
                /**
                 * Update mapping in DODQCISTMapping when QA approval flow is
                 * activated in Delivery Order. Updating IST mapping in
                 * DODQCISTMapping.
                 */
                requestParams.put("istrequest", ist);
                JSONObject json = getDODQCISTMappingJSONMap(requestParams);
                istDAO.saveDODQCISTMapping(json);
            } else if (requestParams.containsKey(Constants.CREATE_IST_FOR_REJECT_DELIVERYORDER_FROM_QC) && requestParams.get(Constants.CREATE_IST_FOR_REJECT_DELIVERYORDER_FROM_QC) != null) {
                /**
                 * To be used for QA Reject for Delivery Order.
                 */
                JSONObject json = getDODQCISTMappingJSONMap(requestParams);
                if (requestParams.containsKey("operation") && requestParams.get("operation") != null) {
                    String operation = (String) requestParams.get("operation");
                    if (!StringUtil.isNullOrEmpty(operation)) {
                        JSONObject repairParams = new JSONObject();
                        if (requestParams.containsKey("rejectQuantity") && requestParams.get("rejectQuantity") != null) {
                            repairParams.put("quantity", (Double) requestParams.get("rejectQuantity"));
                        }
                        if (requestParams.containsKey("rejectQuantityDue") && requestParams.get("rejectQuantityDue") != null) {
                            repairParams.put("quantityDue", (Double) requestParams.get("rejectQuantityDue"));
                        }
                        repairParams.put("repairInterStoreTransferRequest", ist);
                        repairParams.put("dodqcistmappingid", json.optString("id"));
                        RejectedDODQCISTMapping rejectedDODQCISTMapping = istDAO.saveRejectedDODQCISTMapping(repairParams);
                        json.put("rejectedDODQCISTMapping", rejectedDODQCISTMapping);
                    }
                }
                istDAO.saveDODQCISTMapping(json);
            }
            if (requestParams.containsKey("grodid") && requestParams.get("grodid") != null && !requestParams.containsKey(Constants.CREATE_IST_FOR_REJECT_DELIVERYORDER_FROM_QC) && !requestParams.containsKey(Constants.CREATE_PICK_IST_FOR_APPROVE_DELIVERYORDER_FROM_REPAIR_STORE)) {
                /**
                 * to be used while creating GRN
                 */
                String grodid = (String) requestParams.get("grodid");
                double actualQty = 0, quantitydue = 0;
                JSONObject mappingParams = new JSONObject();
                mappingParams.put("grodid", grodid);
                mappingParams.put("istrequest", ist);
                if (requestParams.containsKey("actualquantity") && requestParams.get("actualquantity") != null) {
                    if (requestParams.get("actualquanttiy") instanceof Double) {
                        actualQty = (Double) requestParams.get("actualquantity");
                    } else {
                        actualQty = Double.parseDouble(requestParams.get("actualquantity").toString());
                    }
                    mappingParams.put("actualquantity", actualQty);
                }
                if (requestParams.containsKey("quantitydue") && requestParams.get("quantitydue") != null) {
                    if (requestParams.get("quantitydue") instanceof Double) {
                        quantitydue = (Double) requestParams.get("quantitydue");
                    } else {
                        quantitydue = Double.parseDouble(requestParams.get("quantitydue").toString());
                    }
                    mappingParams.put("quantitydue", quantitydue);
                }

                istDAO.saveGROISTDetailMapping(mappingParams);
            } else if (requestParams.containsKey("mappingid") && requestParams.get("mappingid") != null && !requestParams.containsKey(Constants.CREATE_IST_FOR_REJECT_DELIVERYORDER_FROM_QC) && !requestParams.containsKey(Constants.CREATE_PICK_IST_FOR_APPROVE_DELIVERYORDER_FROM_REPAIR_STORE) && !requestParams.containsKey(Constants.CREATE_IST_FOR_QC_WORKORDER)) {
                /**
                 * To be used for QA approval
                 */
                JSONObject mappingParams = new JSONObject();
                mappingParams.put("mappingid", requestParams.get("mappingid"));
                if (requestParams.containsKey("operation") && requestParams.get("operation") != null) {
                    String operation = (String) requestParams.get("operation");
                    if (!StringUtil.isNullOrEmpty(operation)) {
                        if (operation.equals("Approve")) {
                            mappingParams.put("approvedIstRequest", ist);
                        } else if (operation.equals("Reject")) {
                            JSONObject repairParams = new JSONObject();
                            if (requestParams.containsKey("rejectedQty") && requestParams.get("rejectedQty") != null) {
                                repairParams.put("rejectedQty", (Double) requestParams.get("rejectedQty"));
                            }
                            if (requestParams.containsKey("rejectedQtyDue") && requestParams.get("rejectedQtyDue") != null) {
                                repairParams.put("rejectedQtyDue", (Double) requestParams.get("rejectedQtyDue"));
                            }
                            repairParams.put("mappingid", requestParams.get("mappingid"));
                            RepairGRODetailISTMapping repairGRODetailISTMapping = istDAO.saveRepairGRODetailISTMapping(repairParams, ist);
                            mappingParams.put("repairGRODetailISTMapping", repairGRODetailISTMapping);
                        }
                    }
                }
                if (requestParams.containsKey("quantitydue") && requestParams.get("quantitydue") != null) {
                    double quantitydue;
                    if (requestParams.get("quantitydue") instanceof Double) {
                        quantitydue = (Double) requestParams.get("quantitydue");
                    } else {
                        quantitydue = Double.parseDouble(requestParams.get("quantitydue").toString());
                    }
                    mappingParams.put("quantitydue", quantitydue);
                }
                if (requestParams.containsKey("approvedQty") && requestParams.get("approvedQty") != null) {
                    double approvedQty;
                    if (requestParams.get("approvedQty") instanceof Double) {
                        approvedQty = (Double) requestParams.get("approvedQty");
                    } else {
                        approvedQty = Double.parseDouble(requestParams.get("approvedQty").toString());
                    }
                    mappingParams.put("approvedQty", approvedQty);
                }
                if (requestParams.containsKey("approvedSerials") && requestParams.get("approvedSerials") != null) {
                    mappingParams.put("approvedSerials", requestParams.get("approvedSerials"));
                }
                if (requestParams.containsKey("rejectedSerials") && requestParams.get("rejectedSerials") != null) {
                    mappingParams.put("rejectedSerials", requestParams.get("rejectedSerials"));
                }
                istDAO.saveGROISTDetailMapping(mappingParams);
            } else if (requestParams.containsKey("repairid") && requestParams.get("repairid") != null && !requestParams.containsKey(Constants.CREATE_IST_FOR_REJECT_DELIVERYORDER_FROM_QC) && !requestParams.containsKey(Constants.CREATE_PICK_IST_FOR_APPROVE_DELIVERYORDER_FROM_REPAIR_STORE) && !requestParams.containsKey(Constants.CREATE_IST_FOR_QC_WORKORDER)) {
                /**
                 * To be used in case of stock repair
                 */
                JSONObject repairParams = new JSONObject();
                repairParams.put("repairid", (String) requestParams.get("repairid"));
                repairParams.put("repairedIstRequest", ist);
                if (requestParams.containsKey("rejectedQtyDue") && requestParams.get("rejectedQtyDue") != null) {
                    repairParams.put("rejectedQtyDue", (Double) requestParams.get("rejectedQtyDue"));
                }
                istDAO.saveRepairGRODetailISTMapping(repairParams, null);
            } else if ( requestParams.containsKey("wocdetailid") && requestParams.get("wocdetailid")!=null && requestParams.containsKey(Constants.CREATE_IST_FOR_QC_WORKORDER)){
                if(requestParams.containsKey("wocdetailid") && requestParams.get("wocdetailid")!=null){
                 /**
                 * to be used while managing quantity for produced product 
                 */
                String wocdetailid = (String) requestParams.get("wocdetailid");
                double actualQty = 0, quantitydue = 0;
                JSONObject mappingParams = new JSONObject();
                mappingParams.put("wocdetailid", wocdetailid);
                mappingParams.put("istrequest", ist);
                if (requestParams.containsKey("actualquantity") && requestParams.get("actualquantity") != null) {
                    if (requestParams.get("actualquanttiy") instanceof Double) {
                        actualQty = (Double) requestParams.get("actualquantity");
                    } else {
                        actualQty = Double.parseDouble(requestParams.get("actualquantity").toString());
                    }
                    mappingParams.put("actualquantity", actualQty);
                }
                if (requestParams.containsKey("quantitydue") && requestParams.get("quantitydue") != null) {
                    if (requestParams.get("quantitydue") instanceof Double) {
                        quantitydue = (Double) requestParams.get("quantitydue");
                    } else {
                        quantitydue = Double.parseDouble(requestParams.get("quantitydue").toString());
                    }
                    mappingParams.put("quantitydue", quantitydue);
                }

                istDAO.saveWOCDetailISTMapping(mappingParams);
                }                   
            } else if (requestParams.containsKey("wocdistmapping") && requestParams.get("wocdistmapping") != null && requestParams.containsKey(Constants.CREATE_IST_FOR_QC_WORKORDER)) {

                if (requestParams.containsKey("wocdistmapping") && requestParams.get("wocdistmapping") != null) {
                    /**
                     * To be used for QA approval
                     */
                    JSONObject mappingParams = new JSONObject();
                    mappingParams.put("wocdistmapping", requestParams.get("wocdistmapping"));
                    if (requestParams.containsKey("operation") && requestParams.get("operation") != null) {
                        String operation = (String) requestParams.get("operation");
                        if (!StringUtil.isNullOrEmpty(operation)) {
                            if (operation.equals("Approve")) {
                                mappingParams.put("approvedIstRequest", ist);
                            } else if (operation.equals("Reject")) {
                                JSONObject repairParams = new JSONObject();
                                if (requestParams.containsKey("rejectedQty") && requestParams.get("rejectedQty") != null) {
                                    repairParams.put("rejectedQty", (Double) requestParams.get("rejectedQty"));
                                }
                                if (requestParams.containsKey("rejectedQtyDue") && requestParams.get("rejectedQtyDue") != null) {
                                    repairParams.put("rejectedQtyDue", (Double) requestParams.get("rejectedQtyDue"));
                                }
                                repairParams.put("wocdistmapping", requestParams.get("wocdistmapping"));
                                RepairWOCDISTMapping repairWOCDISTMapping = istDAO.saveRepairWOCDISTMapping(repairParams, ist);
                                mappingParams.put("repairWOCDISTMapping", repairWOCDISTMapping);
                            }
                        }
                    }
                    if (requestParams.containsKey("quantitydue") && requestParams.get("quantitydue") != null) {
                        double quantitydue;
                        if (requestParams.get("quantitydue") instanceof Double) {
                            quantitydue = (Double) requestParams.get("quantitydue");
                        } else {
                            quantitydue = Double.parseDouble(requestParams.get("quantitydue").toString());
                        }
                        mappingParams.put("quantitydue", quantitydue);
                    }
                    if (requestParams.containsKey("approvedQty") && requestParams.get("approvedQty") != null) {
                        double approvedQty;
                        if (requestParams.get("approvedQty") instanceof Double) {
                            approvedQty = (Double) requestParams.get("approvedQty");
                        } else {
                            approvedQty = Double.parseDouble(requestParams.get("approvedQty").toString());
                        }
                        mappingParams.put("approvedQty", approvedQty);
                    }
                    if (requestParams.containsKey("approvedSerials") && requestParams.get("approvedSerials") != null) {
                        mappingParams.put("approvedSerials", requestParams.get("approvedSerials"));
                    }
                    if (requestParams.containsKey("rejectedSerials") && requestParams.get("rejectedSerials") != null) {
                        mappingParams.put("rejectedSerials", requestParams.get("rejectedSerials"));
                    }
                    istDAO.saveWOCDetailISTMapping(mappingParams);
                }
            } else if (requestParams.containsKey("repairid") && requestParams.get("repairid") != null && requestParams.containsKey(Constants.CREATE_IST_FOR_QC_WORKORDER)) {
                /**
                 * To be used in case of stock repair
                 */
                if (requestParams.containsKey("repairid") && requestParams.get("repairid") != null) {

                    JSONObject repairParams = new JSONObject();
                    repairParams.put("repairid", (String) requestParams.get("repairid"));
                    repairParams.put("repairedIstRequest", ist);
                    if (requestParams.containsKey("rejectedQtyDue") && requestParams.get("rejectedQtyDue") != null) {
                        repairParams.put("rejectedQtyDue", (Double) requestParams.get("rejectedQtyDue"));
                    }
                    istDAO.saveRepairWOCDISTMapping(repairParams, null);

                }

            }
                                            
            /*
             *Save Custom data
             */
            saveGlobalAndLineLevelTransferCustomData(ist,requestParams);
            addToISTBuffer(ist, TransactionModule.INTER_STORE_TRANSFER);
        } catch (ParseException | SessionExpiredException | JSONException ex) {
            Logger.getLogger(InterStoreTransferServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void acceptInterStoreTransferRequest(User user, InterStoreTransferRequest ist) throws ServiceException {
        acceptISTReturnRequest(user, ist, null);
    }
    
    private JSONObject getDODQCISTMappingJSONMap(Map<String, Object> requestParams) throws JSONException {
        JSONObject json = new JSONObject();
        if (requestParams.containsKey("dodQCIstMappingID") && requestParams.get("dodQCIstMappingID") != null) {
            json.put("id", (String) requestParams.get("dodQCIstMappingID"));
        }
        if (requestParams.containsKey("approvedQty") && requestParams.get("approvedQty") != null) {
            json.put("approvedQty", (Double) requestParams.get("approvedQty"));
        }
        if (requestParams.containsKey("quantity") && requestParams.get("quantity") != null) {
            json.put("quantity", (Double) requestParams.get("quantity"));
        }
        if (requestParams.containsKey("quantityDue") && requestParams.get("quantityDue") != null) {
            json.put("quantityDue", (Double) requestParams.get("quantityDue"));
        }
        if (requestParams.containsKey("pickedQty") && requestParams.get("pickedQty") != null) {
            json.put("pickedQty", (Double) requestParams.get("pickedQty"));
        }
        if (requestParams.containsKey("rejectedQty") && requestParams.get("rejectedQty") != null) {
            json.put("rejectedQty", (Double) requestParams.get("rejectedQty"));
        }
        if (requestParams.containsKey("approvedSerials") && requestParams.get("approvedSerials") != null) {
            json.put("approvedSerials", (String) requestParams.get("approvedSerials"));
        }
        if (requestParams.containsKey("rejectedSerials") && requestParams.get("rejectedSerials") != null) {
            json.put("rejectedSerials", (String) requestParams.get("rejectedSerials"));
        }
        if (requestParams.containsKey("approvedStockOut") && requestParams.get("approvedStockOut") != null) {
            json.put("approvedStockOut", requestParams.get("approvedStockOut"));
        }
        if (requestParams.containsKey("istrequest") && requestParams.get("istrequest") != null) {
            json.put("istrequest", requestParams.get("istrequest"));
        }
        if (requestParams.containsKey("rejectedDODQCISTMapping") && requestParams.get("rejectedDODQCISTMapping") != null) {
            json.put("rejectedDODQCISTMapping", requestParams.get("rejectedDODQCISTMapping"));
        }
        if (requestParams.containsKey("pickedMapping") && requestParams.get("pickedMapping") != null) {
            json.put("pickedMapping", requestParams.get("pickedMapping"));
        }
        if (requestParams.containsKey("dodetailid") && requestParams.get("dodetailid") != null) {
            json.put("dodetailid", (String) requestParams.get("dodetailid"));
        }
        return json;
    }
    @Override
    public void acceptInterStoreTransferRequest(User user, InterStoreTransferRequest ist, String stockMovementRemark) throws ServiceException {
        try {
            if (ist == null) {
                throw new InventoryException(InventoryException.Type.NULL, "InterStoreTransfer is null");
            }
            ist.setModifiedBy(user);
            ist.setApprovedBy(user);
            ist.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long modifiedDate = System.currentTimeMillis();
            ist.setModifieddate(modifiedDate);
            ist.setStatus(InterStoreTransferStatus.ACCEPTED);
            istDAO.saveOrUpdate(ist);

            double returnQty = ist.getOrderedQty() - ist.getAcceptedQty();
            //        InventoryConfig invConfig = invConfigService.getConfigByCompany(ist.getCompany());
            if (returnQty > 0) {
                createReturnRequest(ist);
                //            returnFromISTBuffer(ist, TransactionModule.INTER_STORE_TRANSFER);
            }
            collectISTBuffer(ist, TransactionModule.INTER_STORE_TRANSFER, stockMovementRemark);
        } catch (ParseException ex) {
            Logger.getLogger(InterStoreTransferServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(InterStoreTransferServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void rejectInterStoreTransferRequest(User user, InterStoreTransferRequest ist) throws ServiceException {
        try {
            if (ist == null) {
                throw new InventoryException(InventoryException.Type.NULL, "InterStoreTransfer is null");
            }
            ist.setModifiedBy(user);
            ist.setApprovedBy(user);
            ist.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long modifiedDate = System.currentTimeMillis();
            ist.setModifieddate(modifiedDate);
            ist.setStatus(InterStoreTransferStatus.REJECTED);
            istDAO.saveOrUpdate(ist);

            createReturnRequest(ist);
//            returnFromISTBuffer(ist, TransactionModule.INTER_STORE_TRANSFER);
        } catch (ParseException ex) {
            Logger.getLogger(InterStoreTransferServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(InterStoreTransferServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        collectISTBuffer(ist, TransactionModule.INTER_STORE_TRANSFER);
    }

    @Override
    public List<InterStoreTransferRequest> getIncommingInterStoreTransferList(User user, Store store, Date fromDate, Date toDate, String searchString, Paging paging, String TZdiff,Map<String, Object> reqMap) throws ServiceException {
        List<InterStoreTransferRequest> interStoreTransferList = new ArrayList<InterStoreTransferRequest>();
        List<Store> stores = new ArrayList();
        if (store != null) {
            stores.add(store);
        } else {
            stores = storeService.getStores(user.getCompany(), null, null);
        }
        Set<Store> storeSet = new HashSet();
        for (Store s : stores) {
            Set<User> storeManagers = s.getStoreManagerSet();
            storeManagers.addAll(s.getStoreExecutiveSet());
            if (storeManagers.contains(user)) {
                storeSet.add(s);
            }
        }
        if (!storeSet.isEmpty()) {
            interStoreTransferList = istDAO.getISTIncommingRequestList(user.getCompany(), storeSet, fromDate, toDate, searchString, paging, TZdiff,reqMap);
        }
        return interStoreTransferList;
    }

    @Override
    public List<InterStoreTransferRequest> getOutgoingInterStoreTransferList(User user, Store store, Date fromDate, Date toDate, String searchString, Paging paging, String TZdiff,Map<String, Object> reqMap) throws ServiceException {
        List<InterStoreTransferRequest> interStoreTransferList = new ArrayList<InterStoreTransferRequest>();
        List<Store> stores = new ArrayList();
        if (store != null) {
            stores.add(store);
        } else {
            stores = storeService.getStores(user.getCompany(), null, null);
        }
        Set<Store> storeSet = new HashSet();
        for (Store s : stores) {
            Set<User> storeManagers = s.getStoreManagerSet();
            storeManagers.addAll(s.getStoreExecutiveSet());
            if (storeManagers.contains(user)) {
                storeSet.add(s);
            }
        }
        if (!storeSet.isEmpty()) {
            interStoreTransferList = istDAO.getISTOutgoingRequestList(user.getCompany(), storeSet, fromDate, toDate, searchString, paging, TZdiff,reqMap);
        }
        return interStoreTransferList;
    }

    @Override
    public List<InterStoreTransferRequest> getCompletedInterStoreTransferList(Company company, Set<Store> storeSet, Date fromDate, Date toDate, String searchString, Paging paging, String TZdiff,Map<String, Object> reqMap) throws ServiceException {
        List<InterStoreTransferRequest> list = null;
        if (!storeSet.isEmpty()) {
            InterStoreTransferStatus[] statusList = new InterStoreTransferStatus[]{InterStoreTransferStatus.ACCEPTED, InterStoreTransferStatus.REJECTED, InterStoreTransferStatus.DELETED, InterStoreTransferStatus.RETURN_ACCEPTED};
            list = istDAO.getInterStoreTransferList(company, storeSet, storeSet, statusList, fromDate, toDate, searchString, paging, TZdiff,reqMap);
        }
        return list;
    }

    @Override
    public List<InterStoreTransferRequest> getInterLocationTransferList(Company company, Set<Store> storeSet, Date fromDate, Date toDate, String searchString, Paging paging, String TZdiff) throws ServiceException {
        List<InterStoreTransferRequest> list = null;
        if (!storeSet.isEmpty()) {

            list = istDAO.getInterLocationTransferList(company, storeSet, fromDate, toDate, searchString, paging, TZdiff);
        }
        return list;
    }

    @Override
    public List<ISTDetail> getInterLocationTransferListByDetailwise(Company company, Set<Store> storeSet, Date fromDate, Date toDate, String searchString, Paging paging, String TZdiff) throws ServiceException {
        List<ISTDetail> list = null;
        if (!storeSet.isEmpty()) {

            list = istDAO.getInterLocationTransferListByDetailwise(company, storeSet, fromDate, toDate, searchString, paging, TZdiff);
        }
        return list;
    }
    
    @Override
    public ISTDetail getISTDetailById(String istDetailId) throws ServiceException {
        if (StringUtil.isNullOrEmpty(istDetailId)) {
            throw new InventoryException(InventoryException.Type.NULL, "ISTDetail Id is null or empty.");
        }
        return istDAO.getISTDetailById(istDetailId);
    }

    @Override
    public InterStoreTransferRequest getInterStoreTransferById(String interStoreTransferId) throws ServiceException {
        if (StringUtil.isNullOrEmpty(interStoreTransferId)) {
            throw new InventoryException(InventoryException.Type.NULL, "StockRequest Id is null or empty.");
        }
        return istDAO.getInterStoreTransferById(interStoreTransferId);
    }

    private void addToISTBuffer(InterStoreTransferRequest ist, TransactionModule transactionModule) throws ServiceException {
        double productPrice = stockService.getProductPurchasePrice(ist.getProduct(), ist.getBusinessDate());
        StockMovement sm = new StockMovement(ist.getProduct(), ist.getFromStore(), 0, 0, ist.getTransactionNo(), ist.getBusinessDate(), TransactionType.OUT, transactionModule, ist.getId(), ist.getId());
        sm.setStockUoM(ist.getProduct().getUnitOfMeasure());
        sm.setCostCenter(ist.getCostCenter());
        if (transactionModule != null && TransactionModule.INTER_LOCATION_TRANSFER.equals(transactionModule)) {
            if (ist != null) {
                for (ISTDetail istd : ist.getIstDetails()) {
                    if (istd != null && istd.getIssuedLocation() != null && istd.getDeliveredLocation() != null) {
                        sm.setRemark("Stock sent from " + istd.getIssuedLocation().getName() + " to " + istd.getDeliveredLocation().getName());
                    }
                }
                sm.setMemo(ist.getMemo());
            }
        } else {
            sm.setRemark("Stock sent from " + ist.getFromStore().getFullName() + " to " + ist.getToStore().getFullName());
        }
        double totalQuantity = 0;
        Set<StockMovementDetail> smdSet = new HashSet<StockMovementDetail>();

        for (ISTDetail istd : ist.getIstDetails()) {
            totalQuantity += istd.getIssuedQuantity();
            stockService.decreaseInventory(ist.getProduct(), ist.getFromStore(), istd.getIssuedLocation(), istd.getIssuedRow(), istd.getIssuedRack(), istd.getIssuedBin(), istd.getBatchName(), istd.getIssuedSerialNames(), istd.getIssuedQuantity());
            stockMovementService.stockMovementInERP(false, ist.getProduct(), ist.getFromStore(), istd.getIssuedLocation(), istd.getIssuedRow(), istd.getIssuedRack(), istd.getIssuedBin(), istd.getBatchName(), istd.getIssuedSerialNames(), istd.getIssuedQuantity(), false);
            StockMovementDetail smd = new StockMovementDetail(sm, istd.getIssuedLocation(), istd.getIssuedRow(), istd.getIssuedRack(), istd.getIssuedBin(), istd.getBatchName(), istd.getIssuedSerialNames(), istd.getIssuedQuantity());
            smdSet.add(smd);
        }
        sm.setQuantity(totalQuantity);
        sm.setPricePerUnit(productPrice);
        sm.setStockMovementDetails(smdSet);
        stockMovementService.addStockMovement(sm);

        stockService.updateERPInventory(false, ist.getBusinessDate(), ist.getProduct(), ist.getPackaging(), ist.getProduct().getUnitOfMeasure(), totalQuantity, "Stock send");
        }

    private void returnFromISTBuffer(InterStoreTransferRequest ist, TransactionModule transactionModule) throws ServiceException {
        double productPrice = stockService.getProductPurchasePrice(ist.getProduct(), ist.getBusinessDate());
        StockMovement sm = new StockMovement(ist.getProduct(), ist.getFromStore(), 0, productPrice, ist.getTransactionNo(), ist.getBusinessDate(), TransactionType.IN, transactionModule, ist.getId(), ist.getId());
        sm.setStockUoM(ist.getProduct().getUnitOfMeasure());
        sm.setCostCenter(ist.getCostCenter());
        if (ist.getStatus() == InterStoreTransferStatus.DELETED) {
            sm.setRemark("Inter Store Stock Request is deleted");
        } else {
            sm.setRemark("Stock Returned from " + ist.getToStore().getFullName() + ", and sent to " + ist.getFromStore().getFullName());
        }
        double totalQuantity = 0;
        Set<StockMovementDetail> smdSet = new HashSet<StockMovementDetail>();

        for (ISTDetail istd : ist.getIstDetails()) {
            ISTDetail ristd = istd.getReturnStockDetail();
            if (ristd != null) {
                double quantity = ristd.getDeliveredQuantity();
                totalQuantity += quantity;
                stockService.increaseInventory(ist.getProduct(), ist.getFromStore(), ristd.getDeliveredLocation(), ristd.getDeliveredRow(), ristd.getDeliveredRack(), ristd.getDeliveredBin(), ristd.getBatchName(), ristd.getDeliveredSerialNames(), quantity);
                stockMovementService.stockMovementInERP(true, ist.getProduct(), ist.getFromStore(), ristd.getDeliveredLocation(), ristd.getDeliveredRow(), ristd.getDeliveredRack(), ristd.getDeliveredBin(), ristd.getBatchName(), ristd.getDeliveredSerialNames(), quantity, false);
                StockMovementDetail smd = new StockMovementDetail(sm, ristd.getDeliveredLocation(), ristd.getDeliveredRow(), ristd.getDeliveredRack(), ristd.getDeliveredBin(), ristd.getBatchName(), ristd.getDeliveredSerialNames(), quantity);
                smdSet.add(smd);
            }
        }
        sm.setQuantity(totalQuantity);
        sm.setStockMovementDetails(smdSet);
        stockMovementService.addStockMovement(sm);

        stockService.updateERPInventory(true, ist.getBusinessDate(), ist.getProduct(), ist.getPackaging(), ist.getProduct().getUnitOfMeasure(), totalQuantity, "Stock returned");
    }
    
      @Override
    public void deleteAcceptedISTRequests(InterStoreTransferRequest istRequest) throws ServiceException {
        if (istRequest == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Inter Store Transfer is null");
        }
//        if (istRequest.getStatus() != InterStoreTransferStatus.ACCEPTED || istRequest.getStatus() != InterStoreTransferStatus.PENDING_APPROVAL) {
        if ( istRequest.getStatus() != InterStoreTransferStatus.PENDING_APPROVAL || istRequest.getStatus() != InterStoreTransferStatus.ACCEPTED) { 
            throw new InventoryException(InventoryException.Type.NULL, "Inter Store Transfer status must  be Pending for Approval");
        }
        deleteAccpetedOrPendingIST(istRequest, TransactionModule.INTER_STORE_TRANSFER);
    }
    private void deleteAccpetedOrPendingIST(InterStoreTransferRequest ist, TransactionModule transactionModule) throws ServiceException {
        boolean checkForAvlQuantityForIST = checkForAvlQuantityForIST(ist);
        if (checkForAvlQuantityForIST) {
            List<StockMovement> smlist = stockMovementService.getStockMovementListByReferenceIdForWorkOrder(ist.getCompany(), ist.getId());
            if (smlist != null && !smlist.isEmpty()) {
                for (StockMovement sm : smlist) {
                    for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                        if (sm.getTransactionType() == TransactionType.IN) {
                            stockService.decreaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                            stockMovementService.stockMovementInERP(false, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), false);
                        } else if (sm.getTransactionType() == TransactionType.OUT) {
                            stockService.increaseInventory(sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity());
                            stockMovementService.stockMovementInERP(true, sm.getProduct(), sm.getStore(), smd.getLocation(), smd.getRow(), smd.getRack(), smd.getBin(), smd.getBatchName(), smd.getSerialNames(), smd.getQuantity(), false);
                        }
                        istDAO.delete(smd);
                    }
                    
                      // Delete nventory records 
                   
                    KwlReturnObject inventoryObject = accountingHandlerDAO.getObject(Inventory.class.getName(), sm.getId());
                    Inventory inventory = (inventoryObject.getEntityList().isEmpty()||inventoryObject.getEntityList()==null) ? null:(Inventory) inventoryObject.getEntityList().get(0);
                    
                    if (inventory != null) {
                        istDAO.delete(inventory);
                    }
                    istDAO.delete(sm);
                    
                  
                }
            }
            
            istDAO.delete(ist);
        }else{
            throw new InventoryException(InventoryException.Type.NULL, "Stock is not available in "+ist.getToStore().getFullName());
        }
    }

    private boolean checkForAvlQuantityForIST(InterStoreTransferRequest IST) throws ServiceException {
        boolean avlQty = false;
        try {
            if (IST != null) {
                Product product = IST.getProduct();
                Store store = IST.getToStore();
                if (IST.getIstDetails() != null) {
                    for (ISTDetail istd : IST.getIstDetails()) {
                        Location location = istd.getIssuedLocation();
                        if (product.isIsBatchForProduct() || product.isIsSerialForProduct() || product.isIsBatchForProduct() || product.isIslocationforproduct()) {
                            NewProductBatch productBatch = stockService.getERPProductBatch(product, store, location, istd.getIssuedRow(), istd.getIssuedRack(), istd.getIssuedBin(), istd.getBatchName());
                            if (productBatch != null) {
                                if (productBatch.getQuantitydue() >= istd.getIssuedQuantity() && !product.isIsSerialForProduct()) {
                                    avlQty = true;
                                } else if (product.isIsSerialForProduct() && productBatch.getQuantitydue() >= istd.getIssuedQuantity()) {

                                    String[] serArr = istd.getIssuedSerialNames().split(",");
                                    if (serArr.length > 0) {
                                        List<String> srList = Arrays.asList(serArr);
                                        List<StockMovement> smList = stockMovementDAO.getStockMovementByProduct(IST.getCompany(), product, store, IST.getCreatedOn(), istd.getIssuedSerialNames(), IST.getId());
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
                                                    throw new InventoryException(InventoryException.Type.NULL, "Quantity is not available." + IST.getTransactionNo());
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
                            } else {
                                throw new InventoryException(InventoryException.Type.NULL, "Quantity is not available, so you can not delete " + IST.getTransactionNo());
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
    private void collectISTBuffer(InterStoreTransferRequest ist, TransactionModule transactionModule, String remark) throws ServiceException {
        double productPrice = stockService.getProductPurchasePrice(ist.getProduct(), ist.getBusinessDate());
        StockMovement sm = new StockMovement(ist.getProduct(), ist.getToStore(), 0, productPrice, ist.getTransactionNo(), ist.getBusinessDate(), TransactionType.IN, transactionModule, ist.getId(), ist.getId());
        sm.setStockUoM(ist.getProduct().getUnitOfMeasure());
        sm.setCostCenter(ist.getCostCenter());
        if (StringUtil.isNullOrEmpty(remark)) {
            if (transactionModule != null && TransactionModule.INTER_LOCATION_TRANSFER.equals(transactionModule)) {
                if (ist != null) {
                    for (ISTDetail istd : ist.getIstDetails()) {
                        if (istd != null && istd.getIssuedLocation() != null && istd.getDeliveredLocation() != null) {
                            sm.setRemark("Stock Accepted in " + istd.getDeliveredLocation().getName() + ", sent from " + istd.getIssuedLocation().getName());
                        }
                    }
                }
            } else {
                sm.setRemark("Stock Accepted in " + ist.getToStore().getFullName() + ", sent from " + ist.getFromStore().getFullName());
            }
        } else {
            sm.setRemark(remark);
        }
        double totalQuantity = 0;
        Set<StockMovementDetail> smdSet = new HashSet<StockMovementDetail>();
        for (ISTDetail istd : ist.getIstDetails()) {
            if (istd.getDeliveredQuantity() > 0) {
                stockService.increaseInventory(ist.getProduct(), ist.getToStore(), istd.getDeliveredLocation(), istd.getDeliveredRow(), istd.getDeliveredRack(), istd.getDeliveredBin(), istd.getBatchName(), istd.getDeliveredSerialNames(), istd.getDeliveredQuantity());
                stockMovementService.stockMovementInERP(true, ist.getProduct(), ist.getToStore(), istd.getDeliveredLocation(), istd.getDeliveredRow(), istd.getDeliveredRack(), istd.getDeliveredBin(), istd.getBatchName(), istd.getDeliveredSerialNames(), istd.getDeliveredQuantity(), false);
                totalQuantity += istd.getDeliveredQuantity();
                StockMovementDetail smd = new StockMovementDetail(sm, istd.getDeliveredLocation(), istd.getDeliveredRow(), istd.getDeliveredRack(), istd.getDeliveredBin(), istd.getBatchName(), istd.getDeliveredSerialNames(), istd.getDeliveredQuantity());
                smdSet.add(smd);
            }
        }
        sm.setQuantity(totalQuantity);
        sm.setStockMovementDetails(smdSet);
        stockMovementService.updateProductIssueCount(sm.getProduct(), totalQuantity);
        if (sm != null && sm.getQuantity() > 0) {
            stockMovementService.addStockMovement(sm);
        }
        stockService.updateERPInventory(true, ist.getBusinessDate(), ist.getProduct(), ist.getPackaging(), ist.getProduct().getUnitOfMeasure(), totalQuantity, "Stock accepted");
        }

    private void collectISTBuffer(InterStoreTransferRequest ist, TransactionModule transactionModule) throws ServiceException {
        collectISTBuffer(ist, transactionModule, null);
    }

    @Override
    public void addInterLocationTransfer(User user, InterStoreTransferRequest ist, Map<String,Object> requestParams) throws ServiceException {
        try {
//        if (ist == null) {
//            throw new InventoryException(InventoryException.Type.NULL, "Inter Location Transfer is empty");
//        }
//        if (ist.getIstDetails().isEmpty()) {
//            throw new InventoryException(InventoryException.Type.NULL, "Inter Location Transfer detail is empty");
//        }
//
//        if (ist.getProduct().getPackaging() != null) {
//            Packaging packaging = packagingService.createClonePackaging(ist.getProduct().getPackaging());
//            ist.setPackaging(packaging);
//        } else {
//            Packaging packaging = packagingService.createPackagingByStockUom(ist.getProduct().getUnitOfMeasure());
//            ist.setPackaging(packaging);
//        }
//        if (ist.getProduct().getTransferUOM() != null) {
//            ist.setUom(ist.getProduct().getTransferUOM());
//        }
            if (ist == null) {
                throw new InventoryException(InventoryException.Type.NULL, "InterStoreTransfer is null");
            }
            if (ist.getIstDetails().isEmpty()) {
                throw new InventoryException(InventoryException.Type.NULL, "Inter Store Transfer detail is empty");
            }
            if (ist.getUom() == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Please select uom for Product : " + ist.getProduct().getProductid());
            }
            if (ist.getPackaging() == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Please select packaging for Product : " + ist.getProduct().getProductid());
            }

            if (ist.getPackaging() != null) {
                Packaging packaging = packagingService.createClonePackaging(ist.getPackaging());
                ist.setPackaging(packaging);
            } else {
                Packaging packaging = packagingService.createPackagingByStockUom(ist.getProduct().getUnitOfMeasure());
                ist.setPackaging(packaging);
            }

            ist.setCreatedBy(user);
            ist.setCreatedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long creationDate = System.currentTimeMillis();
            ist.setCreationdate(creationDate);
            ist.setModifiedBy(user);
            ist.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long modifiedDate = System.currentTimeMillis();
            ist.setModifieddate(modifiedDate);
            ist.setStatus(InterStoreTransferStatus.ACCEPTED);
            ist.setTransactionModule(TransactionModule.INTER_LOCATION_TRANSFER);

            istDAO.saveOrUpdate(ist);

            /*
             *Save Custom data
             */
            saveGlobalAndLineLevelTransferCustomData(ist,requestParams);

            /**
             * Save DO details and Inter Store Mapping
             */
            
            if (requestParams.containsKey("dod") && requestParams.get("dod") != null) {
                String dod = (String) requestParams.get("dod");
                Map<String ,Object> mappingParams = new HashMap();
                mappingParams.put("dod", dod);
                mappingParams.put("ist", ist);
                saveDODISTMapping(mappingParams);
            }


            addToISTBuffer(ist, TransactionModule.INTER_LOCATION_TRANSFER);

            collectISTBuffer(ist, TransactionModule.INTER_LOCATION_TRANSFER);
        } catch (ParseException ex) {
            Logger.getLogger(InterStoreTransferServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(InterStoreTransferServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
/**
 * @Desc : Save DO - Inter Store - Stock Out Mapping
 * @param mappingParams
 * @return DeliveryDetailInterStoreLocationMapping
 * @throws ServiceException 
 */
    public DeliveryDetailInterStoreLocationMapping saveDODISTMapping(Map<String ,Object>  mappingParams) throws ServiceException {
        String dod = "";
        InterStoreTransferRequest interStoreTransferRequest = null;
        StockAdjustment stockAdjustment=null;
        DeliveryDetailInterStoreLocationMapping deliveryDetailInterStoreLocationMapping=null;
        if(mappingParams.containsKey("id") && mappingParams.get("id")!=null){
            String billid = (String) mappingParams.get("id");
//            deliveryDetailInterStoreLocationMapping = (DeliveryDetailInterStoreLocationMapping) get(DeliveryDetailInterStoreLocationMapping.class, billid);
//            deliveryDetailInterStoreLocationMapping = new DeliveryDetailInterStoreLocationMapping();
        } else {
            deliveryDetailInterStoreLocationMapping = new DeliveryDetailInterStoreLocationMapping();
        }

        if (mappingParams.containsKey("dod")) {
            dod = (String) mappingParams.get("dod");
            deliveryDetailInterStoreLocationMapping.setDeliveryOrderDetail(dod);
        }
        if (mappingParams.containsKey("ist")) {
            interStoreTransferRequest = (InterStoreTransferRequest) mappingParams.get("ist");
            deliveryDetailInterStoreLocationMapping.setInterStoreTransferRequest(interStoreTransferRequest);
            deliveryDetailInterStoreLocationMapping.setPickedQty(interStoreTransferRequest.getOrderedQty());
        }
        if (mappingParams.containsKey("stockadjustment")) {
            stockAdjustment = (StockAdjustment) mappingParams.get("stockadjustment");
            deliveryDetailInterStoreLocationMapping.setStockAdjustment(stockAdjustment);
            deliveryDetailInterStoreLocationMapping.setShippedQty(stockAdjustment.getQuantity());
        }
        if (mappingParams.containsKey("dodqcistmapping") && mappingParams.get("dodqcistmapping") != null) {
            DODQCISTMapping dODQCISTMapping = (DODQCISTMapping)mappingParams.get("dodqcistmapping");
            deliveryDetailInterStoreLocationMapping.setDodqcistmapping(dODQCISTMapping);
        }
        if (mappingParams.containsKey("pickRejectedDODQCISTMapping") && mappingParams.get("pickRejectedDODQCISTMapping") != null) {
            RejectedDODQCISTMapping pickRejectedDODQCISTMapping = (RejectedDODQCISTMapping) mappingParams.get("pickRejectedDODQCISTMapping");
            deliveryDetailInterStoreLocationMapping.setPickRejectedDODQCISTMapping(pickRejectedDODQCISTMapping);
        }
        istDAO.saveOrUpdate(deliveryDetailInterStoreLocationMapping);
        return deliveryDetailInterStoreLocationMapping;
    }
    /**
     * 
     * @param map
     * @return
     * @Desc : save Challan No rec
     * @throws ServiceException 
     */
    public KwlReturnObject saveChallanNumber(Map<String, Object> map) throws ServiceException {
        List list = new ArrayList();
        ChallanNumber challanNumber = new ChallanNumber();
        if (map.containsKey("challanno")) {
            challanNumber.setChallanNumber((String) map.get("challanno"));
        }
        if (map.containsKey("company")) {
            Company cmp = (Company) map.get("company");
            challanNumber.setCompany(cmp);
        }
        istDAO.saveOrUpdate(challanNumber);
        list.add(challanNumber);

        return new KwlReturnObject(true, "Challan Number has been saved successfully", null, list, list.size());
    }
    @Override
    public KwlReturnObject getVendorNameAndJWONo(String podid,String companyid)throws ServiceException{
        return istDAO.getVendorNameAndJWONo(podid,companyid);
    }

    private void createReturnRequest(InterStoreTransferRequest ist) throws ServiceException {
        if (ist == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Inter Store Transfer return is null");
        }
        if (ist.getIstDetails().isEmpty()) {
            throw new InventoryException(InventoryException.Type.NULL, "Inter Store Transfer return detail  is empty");
        }
        InterStoreTransferRequest returnIST = new InterStoreTransferRequest(ist.getProduct(), ist.getToStore(), ist.getFromStore(), ist.getUom());
        returnIST.setBusinessDate(ist.getBusinessDate());
        returnIST.setCompany(ist.getCompany());
        returnIST.setCostCenter(ist.getCostCenter());
        returnIST.setCreatedBy(ist.getModifiedBy());
        returnIST.setModifiedBy(ist.getModifiedBy());
        returnIST.setCreatedOn(ist.getModifiedOn());
        returnIST.setCreationdate(ist.getModifieddate());
        returnIST.setPackaging(ist.getPackaging());
        returnIST.setRemark("Stock Returned");
        returnIST.setParentID(ist.getId());
        returnIST.setStatus(InterStoreTransferStatus.RETURNED);
        returnIST.setTransactionNo("R" + ist.getTransactionNo());
        Set<ISTDetail> returnDetailSet = new HashSet<ISTDetail>();
        for (ISTDetail istd : ist.getIstDetails()) {
            if (istd.getReturnQuantity() > 0) {
                ISTDetail returnDetail = new ISTDetail();
                returnDetail.setIstRequest(returnIST);
                returnDetail.setBatchName(istd.getBatchName());
                returnDetail.setIssuedLocation(locationService.getDefaultLocation(ist.getCompany()));
                returnDetail.setDeliveredLocation(istd.getIssuedLocation());
                returnDetail.setIssuedRow(istd.getIssuedRow());
                returnDetail.setDeliveredRow(istd.getIssuedRow());
                returnDetail.setIssuedRack(istd.getIssuedRack());
                returnDetail.setDeliveredRack(istd.getIssuedRack());
                returnDetail.setIssuedBin(istd.getIssuedBin());
                returnDetail.setDeliveredBin(istd.getIssuedBin());
                returnDetail.setIssuedSerialNames(istd.getReturnSerialNames());
                returnDetail.setDeliveredSerialNames(istd.getReturnSerialNames());
                returnDetail.setIssuedQuantity(istd.getReturnQuantity());
                returnDetail.setDeliveredQuantity(istd.getReturnQuantity());
                returnDetailSet.add(returnDetail);
            }
        }
        returnIST.setIstDetails(returnDetailSet);
        returnIST.setOrderedQty(ist.getOrderedQty() - ist.getAcceptedQty());
        returnIST.setAcceptedQty(ist.getOrderedQty() - ist.getAcceptedQty());
       
        istDAO.saveOrUpdate(returnIST);
        
        if (!StringUtil.isNullObject(ist.getISTCustomData()) || !StringUtil.isNullObject(ist.getISTLineLevelCustomData())) {
            int NoOFRecords = istDAO.saveCustomDataForReturnTransactions(returnIST.getId(), ist.getId(), Constants.Acc_InterStore_ModuleId);
        }
    }

    private void createReturnRequestForInterLocation(InterStoreTransferRequest ist) throws ServiceException {
        if (ist == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Inter Store Transfer return is null");
        }
        if (ist.getIstDetails().isEmpty()) {
            throw new InventoryException(InventoryException.Type.NULL, "Inter Store Transfer return detail  is empty");
        }
        InterStoreTransferRequest returnIST = new InterStoreTransferRequest(ist.getProduct(), ist.getToStore(), ist.getFromStore(), ist.getUom());
        returnIST.setBusinessDate(new Date());
        returnIST.setCompany(ist.getCompany());
        returnIST.setCostCenter(ist.getCostCenter());
        returnIST.setCreatedBy(ist.getModifiedBy());
        returnIST.setModifiedBy(ist.getModifiedBy());
        returnIST.setCreatedOn(new Date());
        long creationDate = System.currentTimeMillis();
        returnIST.setCreationdate(creationDate);
        returnIST.setPackaging(ist.getPackaging());
        returnIST.setRemark("Stock Returned");
        returnIST.setParentID(ist.getId());
        returnIST.setStatus(InterStoreTransferStatus.RETURNED);
        returnIST.setTransactionNo("R" + ist.getTransactionNo());
        Set<ISTDetail> returnDetailSet = new HashSet<ISTDetail>();
        for (ISTDetail istd : ist.getIstDetails()) {
//            if (istd.getReturnQuantity() > 0) {
            ISTDetail returnDetail = new ISTDetail();
            returnDetail.setIstRequest(returnIST);
            returnDetail.setBatchName(istd.getBatchName());
            returnDetail.setIssuedLocation(istd.getDeliveredLocation());
//            returnDetail.setIssuedLocation(locationService.getDefaultLocation(ist.getCompany()));
            returnDetail.setDeliveredLocation(istd.getIssuedLocation());
            returnDetail.setIssuedRow(istd.getDeliveredRow());
            returnDetail.setDeliveredRow(istd.getIssuedRow());
            returnDetail.setIssuedRack(istd.getDeliveredRack());
            returnDetail.setDeliveredRack(istd.getIssuedRack());
            returnDetail.setIssuedBin(istd.getDeliveredBin());
            returnDetail.setDeliveredBin(istd.getIssuedBin());
            returnDetail.setIssuedSerialNames(istd.getDeliveredSerialNames());
            returnDetail.setDeliveredSerialNames(istd.getIssuedSerialNames());
            returnDetail.setIssuedQuantity(istd.getDeliveredQuantity());
            returnDetail.setDeliveredQuantity(istd.getIssuedQuantity());
            returnDetailSet.add(returnDetail);
//            }
        }
        returnIST.setIstDetails(returnDetailSet);

        addToISTBuffer(returnIST, TransactionModule.INTER_LOCATION_TRANSFER);
        collectISTBuffer(returnIST, TransactionModule.INTER_LOCATION_TRANSFER);

    }

        @Override
    public void acceptISTReturnRequest(User user, InterStoreTransferRequest istReturn) throws ServiceException {
        acceptISTReturnRequest(user, istReturn, null);
    }

    @Override
    public void acceptISTReturnRequest(User user, InterStoreTransferRequest istReturn, String stockMovementRemark) throws ServiceException {
        try {
            if (istReturn == null) {
                throw new InventoryException(InventoryException.Type.NULL, "InterStoreTransfer Return is null");
            }
            istReturn.setModifiedBy(user);
            istReturn.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long modifiedDate = System.currentTimeMillis();
            istReturn.setModifieddate(modifiedDate);        
            istReturn.setStatus(InterStoreTransferStatus.RETURN_ACCEPTED);
            istDAO.saveOrUpdate(istReturn);
            double returnQty = istReturn.getOrderedQty() - istReturn.getAcceptedQty();
            //        InventoryConfig invConfig = invConfigService.getConfigByCompany(ist.getCompany());
            if (returnQty > 0) {
                createReturnRequest(istReturn);
                //            returnFromISTBuffer(ist, TransactionModule.INTER_STORE_TRANSFER);
            }
            collectISTBuffer(istReturn, TransactionModule.INTER_STORE_TRANSFER, stockMovementRemark);
        } catch (ParseException ex) {
            Logger.getLogger(InterStoreTransferServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(InterStoreTransferServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void sendISTReturnRequestForQA(User user, InterStoreTransferRequest istReturn, String[] serialNames) throws ServiceException {
        try {
            if (istReturn == null) {
                throw new InventoryException(InventoryException.Type.NULL, "InterStoreTransfer Return is null");
            }
            Store QAStore = null;
            Location QALocation = null;

            KwlReturnObject extracap = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), istReturn.getCompany().getCompanyID());
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
            if (extraCompanyPreferences.isActivateQAApprovalFlow()) {
                String qaInspectionStore = extraCompanyPreferences.getInspectionStore();
                if (!StringUtil.isNullOrEmpty(qaInspectionStore)) {
                    KwlReturnObject storeObj = accountingHandlerDAO.getObject(Store.class.getName(), qaInspectionStore);
                    QAStore = (Store) storeObj.getEntityList().get(0);
                    if (QAStore != null) {
                        QALocation = QAStore.getDefaultLocation();
                    } else {
                        throw new InventoryException(InventoryException.Type.NULL, "QA Inspection store is null");
                    }
                } else {
                    throw new InventoryException(InventoryException.Type.NULL, "QA Inspection store is null");
                }

            }
            istReturn.setModifiedBy(user);
            istReturn.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            long modifiedDate = System.currentTimeMillis();
            istReturn.setModifieddate(modifiedDate);
            istReturn.setStatus(InterStoreTransferStatus.PENDING_APPROVAL);
            double qty = (serialNames != null) ? serialNames.length : istReturn.getOrderedQty();

            //To Change Server's new Date() to user's new Date().
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date newUserDate = new Date();//Server's new Date();
            if (user != null) {
                //Fetching user's timezone difference.
                String difference = user.getTimeZone().getDifference();
                newUserDate = authHandler.getUserNewDate(difference, null);
            }

            istDAO.saveOrUpdate(istReturn);
            approvalService.addStockReturnApproval(istReturn, serialNames);
        } catch (ParseException ex) {
            Logger.getLogger(InterStoreTransferServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(InterStoreTransferServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public List<InterStoreTransferRequest> getInterStoreTransferBySequenceNo(Company company, String sequenceNo) throws ServiceException {
        return istDAO.getInterStoreTransferBySequenceNo(company, sequenceNo);
    }

    @Override
    public void deleteISTRequest(InterStoreTransferRequest istRequest) throws ServiceException {
        if (istRequest == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Inter Store Transfer is null");
        }
        if (istRequest.getStatus() != InterStoreTransferStatus.INTRANSIT) {
            throw new InventoryException(InventoryException.Type.NULL, "Inter Store Transfer status must be In-Transit");
        }
        istRequest.setStatus(InterStoreTransferStatus.DELETED);
        istDAO.saveOrUpdate(istRequest);

        returnFromISTBuffer(istRequest, TransactionModule.INTER_STORE_TRANSFER);

    }
    
    @Override
    public void deleteILocationTRequest(InterStoreTransferRequest istRequest) throws ServiceException {
        if (istRequest == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Inter Store Transfer is null");
        }

        istRequest.setStatus(InterStoreTransferStatus.DELETED);
        istDAO.saveOrUpdate(istRequest);
        createReturnRequestForInterLocation(istRequest);

    }

    @Override
    public void cancelISTRequest(User user, InterStoreTransferRequest istRequest) throws ServiceException {
        if (istRequest == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Inter Store Transfer is null");
        }
        if (istRequest.getStatus() == InterStoreTransferStatus.INTRANSIT) {
            istRequest.setStatus(InterStoreTransferStatus.DELETED);
            istRequest.setModifiedBy(user);
            returnFromISTBuffer(istRequest, TransactionModule.INTER_STORE_TRANSFER);
        } else if (istRequest.getStatus() == InterStoreTransferStatus.RETURNED) {
            acceptISTReturnRequest(user, istRequest);
        }
        istDAO.saveOrUpdate(istRequest);

    }

    @Override
    public ISTDetail getIntransitIstDetailForSerial(Product product, String batchName, String serialName) throws ServiceException {
        ISTDetail istd = null;
        if (product.isIsSerialForProduct() && !StringUtil.isNullOrEmpty(serialName)) {
            List<ISTDetail> list = istDAO.getIstDetailsForSerialByStatus(InterStoreTransferStatus.INTRANSIT, product, batchName, serialName);
            for (ISTDetail istdi : list) {
                List<String> serials = Arrays.asList(istdi.getIssuedSerialNames().split(","));
                if (serials.contains(serialName)) {
                    istd = istdi;
                    break;
                }
            }
        }
        return istd;
    }

    @Override
    public InterStoreTransferRequest getReturnTransactionforSerial(Product product, String batchName, String serialName) throws ServiceException {
        InterStoreTransferRequest ist = null;
        if (product.isIsSerialForProduct() && !StringUtil.isNullOrEmpty(serialName)) {
            List<ISTDetail> list = istDAO.getIstDetailsForSerialByStatus(InterStoreTransferStatus.RETURNED, product, batchName, serialName);
            for (ISTDetail istd : list) {
                List<String> serials = Arrays.asList(istd.getIssuedSerialNames().split(","));
                if (serials.contains(serialName)) {
                    ist = istd.getIstRequest();
                    break;
                }
            }
        }
        return ist;
    }
    
    /**
     *
     * @param ist
     * @param requestParams
     * @throws ServiceException
     * Save InterLocationTransfer and InterStoreTransfer Custom data
     */
    public void saveGlobalAndLineLevelTransferCustomData(InterStoreTransferRequest ist, Map<String, Object> requestParams) throws ServiceException {
        /*
         Save InterLocationTransfer and InterStoreTransfer Custom data
         */
        String customfield = "";
        String lineLevelCustomData = null;
        String companyId = (String) requestParams.get(Constants.companyid);
        Integer moduleId = (Integer) requestParams.get(Constants.moduleid);
        
        if (requestParams.containsKey(Constants.customfield)) {
            customfield = (String) requestParams.get(Constants.customfield);
        }
        if (requestParams.containsKey(Constants.LineLevelCustomData)) {
            lineLevelCustomData = (String) requestParams.get(Constants.LineLevelCustomData);
        }

        if (!StringUtil.isNullOrEmpty(customfield) || !StringUtil.isNullOrEmpty(lineLevelCustomData)) {
            try {
                KwlReturnObject customDataResult = null;
                HashMap<String, Object> customRequestParams = new HashMap<>();
                customRequestParams.put(Constants.modulename, Constants.Acc_InterStoreTransfer_modulename);
                customRequestParams.put(Constants.moduleprimarykey, Constants.Acc_ISTId);
                customRequestParams.put(Constants.modulerecid, ist.getId());
                customRequestParams.put(Constants.moduleid, moduleId);
                customRequestParams.put(Constants.companyid, companyId);
                customRequestParams.put(Constants.customdataclasspath, Constants.Acc_InterStoreTransfer_custom_data_classpath);
                /*
                 Global Level Custom Fields Data
                 */
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    customRequestParams.put(Constants.customarray, new JSONArray(customfield));
                    customDataResult = fieldDataManagercntrl.setCustomData(customRequestParams);
                    if (customDataResult != null && customDataResult.getEntityList().size() > 0) {
                        InterStoreTransferCustomData cmp = (InterStoreTransferCustomData) customDataResult.getEntityList().get(0);
                        ist.setISTCustomData(cmp);
                    }
                }
                /*
                 Save Line Level Custom Fields Data
                 */
                customDataResult = null;
                if (!StringUtil.isNullOrEmpty(lineLevelCustomData)) {
                    customRequestParams.put(Constants.customarray, new JSONArray(lineLevelCustomData));
                    customDataResult = fieldDataManagercntrl.setCustomData(customRequestParams);
                    if (customDataResult != null && customDataResult.getEntityList().size() > 0) {
                        InterStoreTransferCustomData cmp = (InterStoreTransferCustomData) customDataResult.getEntityList().get(0);
                        ist.setISTLineLevelCustomData(cmp);
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(InterStoreTransferServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            istDAO.saveOrUpdate(ist);
        }

    }
    public RepairGRODetailISTMapping saveRepairGRODetailISTMapping(JSONObject json, InterStoreTransferRequest repairRequest) throws ServiceException, JSONException {
        return istDAO.saveRepairGRODetailISTMapping(json, repairRequest);
    }
    
    public RepairWOCDISTMapping saveRepairWOCDISTMapping(JSONObject json, InterStoreTransferRequest repairRequest) throws ServiceException, JSONException {
        return istDAO.saveRepairWOCDISTMapping(json, repairRequest);
    }
    
    @Override
    public DODQCISTMapping saveDODQCISTMapping(JSONObject json) throws ServiceException, JSONException {
        return istDAO.saveDODQCISTMapping(json);
    }
    
    @Override
    public RejectedDODQCISTMapping saveRejectedDODQCISTMapping(JSONObject json) throws ServiceException, JSONException {
        return istDAO.saveRejectedDODQCISTMapping(json);
    }
}

