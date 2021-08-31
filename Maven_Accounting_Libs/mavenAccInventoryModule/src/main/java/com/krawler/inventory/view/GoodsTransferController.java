/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.view;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.ItemReusability;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.ServerEventManager;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.exception.NegativeInventoryException;
import com.krawler.inventory.model.approval.consignment.ConsignmentApprovalDetails;
import com.krawler.inventory.model.approval.consignmentservice.ConsignmentService;
import com.krawler.inventory.model.approval.sa.SADetailApproval;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferDetailApproval;
import com.krawler.inventory.model.ist.*;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.packaging.PackagingService;
import com.krawler.inventory.model.sequence.ModuleConst;
import com.krawler.inventory.model.sequence.SeqFormat;
import com.krawler.inventory.model.sequence.SeqService;
import com.krawler.inventory.model.stock.Stock;
import com.krawler.inventory.model.stock.StockCustomData;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.*;
import com.krawler.inventory.model.stockout.StockAdjustmentCustomData;
import com.krawler.inventory.model.stockout.StockAdjustmentService;
import com.krawler.inventory.model.stockrequest.RequestStatus;
import com.krawler.inventory.model.stockrequest.StockRequest;
import com.krawler.inventory.model.stockrequest.StockRequestDetail;
import com.krawler.inventory.model.stockrequest.StockRequestService;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.inventory.model.store.StoreType;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.ObjectNotFoundException;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import java.io.File;
import java.io.FileInputStream;
import javax.servlet.ServletContext;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.support.RequestContextUtils;


/**
 *
 * @author Vipin Gupta
 */
public class GoodsTransferController extends MultiActionController {

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Logger lgr = Logger.getLogger(GoodsTransferController.class.getName());
    private HibernateTransactionManager txnManager;
    private String successView;
    private StockRequestService stockRequestService;
    private InterStoreTransferService istService;
    private AccountingHandlerDAO accountingHandlerDAO;
    private StoreService storeService;
    private SeqService seqService;
    private LocationService locationService;
    private StockService stockService;
    private StockMovementService stockMovementService;
    private exportMPXDAOImpl exportDAO;
    private accAccountDAO accAccountDAOobj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private auditTrailDAO auditTrailObj;
    private PackagingService packagingService;
    private ConsignmentService consignmentService;
    private fieldDataManager fieldDataManagercntrl;
    private accProductDAO accProductObj;
    private accJournalEntryDAO accJournalEntryobj;
    private MessageSource messageSource;
    public  ImportHandler importHandler;
    private ImportInvData importInvData;
    private StockAdjustmentService stockAdjustmentService;
    public ImportHandler getImportHandler() {
        return importHandler;
    }
   

    public ImportInvData getImportInvData() {
        return importInvData;
    }

    public void setImportInvData(ImportInvData importInvData) {
        this.importInvData = importInvData;
    }

    public void setImportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setStockRequestService(StockRequestService stockRequestService) {
        this.stockRequestService = stockRequestService;
    }

    public void setIstService(InterStoreTransferService istService) {
        this.istService = istService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setSeqService(SeqService seqService) {
        this.seqService = seqService;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    public void setExportDAO(exportMPXDAOImpl exportDAO) {
        this.exportDAO = exportDAO;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }

    public PackagingService getPackagingService() {
        return packagingService;
    }

    public void setPackagingService(PackagingService packagingService) {
        this.packagingService = packagingService;
    }

    public void setConsignmentService(ConsignmentService consignmentService) {
        this.consignmentService = consignmentService;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    
    public void setAccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    
     public void setStockAdjustmentService(StockAdjustmentService stockAdjustmentService) {
        this.stockAdjustmentService = stockAdjustmentService;
    }
     
    public ModelAndView getStockRequestList(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("GTR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {


            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);

            String searchString = request.getParameter("ss");

            String fd = request.getParameter("frmDate");
            String td = request.getParameter("toDate");
            String filterStatusValue = request.getParameter("status");

            String storeId = request.getParameter("storeid");
            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
                paging = null;
            }
            Date fromDate = null;
            Date toDate = null;
            try {
                    fromDate = df.parse(fd);
                    toDate = df.parse(td);
            } catch (ParseException ex) {
            }
//            Map<String, Double> map = stockService.getAvailableQuantityForAllProductByStore(user.getCompany());
//            Map<String, Double> orderedQtyMap = stockRequestService.getTotalOrderedQuantityForProductStore(user.getCompany());
            
            Store store = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
            }
            List<Store> stores = new ArrayList();

            if (store != null) {
                stores.add(store);
            } else {
                stores = storeService.getStores(user.getCompany(), null, null);
            }
            Set<Store> storeSet = new HashSet();
            Set<Store> storeSet1 = new HashSet();
            for (Store s : stores) {
                Set<User> storeManagers = s.getStoreManagerSet();
                if (storeManagers.contains(user)) {
                    storeSet.add(s);

                    if (s.getStoreType() == StoreType.HEADQUARTER || s.getStoreType() == StoreType.WAREHOUSE) {
                        storeSet1.add(s);
                    }
                }
            }
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
            requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
            requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
            requestParams.put("filterStatusValue", filterStatusValue);
            requestParams.put("reportId", request.getParameter("type"));
            String type = request.getParameter("type");
            List<StockRequest> stockRequestList = new ArrayList();
            if ("1".equals(type)) {
                if (!storeSet1.isEmpty()) {
                    stockRequestList = stockRequestService.getPendingStockRequestList(requestParams, user.getCompany(), user, store, fromDate, toDate, searchString, paging);
                } else {
                    stockRequestList = stockRequestService.getStorewisePendingStockRequestList(requestParams,user.getCompany(), storeSet, fromDate, toDate, searchString, paging, true);
                }
            } else if ("3".equals(type)) {
                stockRequestList = stockRequestService.getStorewisePendingStockRequestList(requestParams,user.getCompany(), storeSet, fromDate, toDate, searchString, paging, false);
            } else if ("2".equals(type)) {
                stockRequestList = stockRequestService.getCompletedStockRequestList(requestParams, user.getCompany(), user, store, fromDate, toDate, searchString, paging);
            }
            /**
             * getAvailableQuantityForAllProductByStore: Get product available
             * quantity. 
             * Working: Inner Query is added for getting available quantity of selected products.
             * 
             * If (isExport) then product IN query is not added 
             * else if(!isExport && paging.getLimit() - paging.getOffset() > Constants.BATCH_LIMIT ) then product IN query is not added 
             * else product IN query is added
             */
            Map<String, Double> map = stockService.getAvailableQuantityForAllProductByStore(user.getCompany(), stockRequestList, isExport ? isExport : (paging != null && paging.getLimit() - paging.getOffset() > Constants.BATCH_LIMIT ? true : isExport));
            Map<String, Double> orderedQtyMap = stockRequestService.getTotalOrderedQuantityForProductStore(user.getCompany());

            /* BEGIN: GLOBAL CUSTOM FIELD STOCK REQUEST MODULE */
            HashMap<String, String> customFieldStockRequestMap = new HashMap<>();
            HashMap<String, String> customDateFieldStockRequestMap = new HashMap<>();
            HashMap<String, String> replaceFieldStockRequestMap = new HashMap<>();
            HashMap<String, Object> fieldStockRequestParams = new HashMap();
            fieldStockRequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldStockRequestParams.put(Constants.filter_values, Arrays.asList(user.getCompany().getCompanyID(), Constants.Acc_Stock_Request_ModuleId));
            //HashMap<String, Integer> FieldMapStockRequest = accAccountDAOobj.getFieldParamsCustomMap(fieldStockRequestParams, replaceFieldStockRequestMap, customDateFieldStockRequestMap, customFieldStockRequestMap);
            HashMap<String, Integer> FieldMapStockRequest = accAccountDAOobj.getFieldParamsCustomMap(fieldStockRequestParams, replaceFieldStockRequestMap, customFieldStockRequestMap, customDateFieldStockRequestMap);
            /* END: STOCK REQUEST MODULE ID */
            
            /* BEGIN: GLOBAL CUSTOM FIELD INVENTORY MODULE */
            HashMap<String, String> customFieldInventoryModuleMap = new HashMap<>();
            HashMap<String, String> customDateFieldInventoryModuleMap = new HashMap<>();
            HashMap<String, String> replaceFieldInventoryModuleMap = new HashMap<>();
            HashMap<String, Object> fieldrequestInventoryModuleParams = new HashMap();
            fieldrequestInventoryModuleParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestInventoryModuleParams.put(Constants.filter_values, Arrays.asList(user.getCompany().getCompanyID(), Constants.Inventory_ModuleId));
            //HashMap<String, Integer> FieldMapInventoryModule = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestInventoryModuleParams, replaceFieldInventoryModuleMap, customDateFieldInventoryModuleMap, customFieldInventoryModuleMap);
            HashMap<String, Integer> FieldMapInventoryModule = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestInventoryModuleParams, replaceFieldInventoryModuleMap, customFieldInventoryModuleMap, customDateFieldInventoryModuleMap);
            /* END: INVENTORY MODULE ID */
            for (StockRequest sr : stockRequestList) {
                JSONObject jObj = new JSONObject();
                JSONArray stockDetails = new JSONArray();
                Product product = sr.getProduct();
                Store fromStore = sr.getFromStore();
                Store toStore = sr.getToStore();
                double availableQty = 0;
                if (product != null && toStore != null) {
                    String key = ((Store) toStore).getId().concat(((Product) product).getID().toString());
                    availableQty = map.get(key) != null ? map.get(key) : 0;
                }

                double totalOrderedQty = 0;
                if (product != null && fromStore != null) {
                    String key = ((Store) fromStore).getId().concat(((Product) product).getID().toString());
                    totalOrderedQty = orderedQtyMap.get(key) != null ? orderedQtyMap.get(key) : 0;
                }
                String defaultStoreName = null;
                String defaultStoreId = null;
                String defaultLocName = null;
                String defaultLocId = null;
                String defaultCollectionLocId = null;
                String defaultCollectionLocName = null;
                double defaultAvailQty = 0;
                if(!(product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct() || product.isIsBatchForProduct() || product.isIsSerialForProduct())){
                    if(product.getWarehouse() != null && product.getLocation() != null && sr.getStatus() == RequestStatus.ORDERED){
                        defaultStoreId = product.getWarehouse().getId();
                        defaultLocId = product.getLocation().getId();
                        defaultStoreName = product.getWarehouse().getName();
                        defaultLocName = product.getLocation().getName();
                        Stock stock = stockService.getStock(product.getID(), defaultStoreId, defaultLocId, null, null, null, null);
                        if(stock != null){
                            defaultAvailQty = stock.getQuantity();
                        }
                    }
                }
                if (sr.getStatus() == RequestStatus.ISSUED && fromStore != null && fromStore.getDefaultLocation() != null) {
                    defaultCollectionLocId = fromStore.getDefaultLocation().getId();
                    defaultCollectionLocName = fromStore.getDefaultLocation().getName();
                }
                

                double orderToStockUOMFactor = 1;
                double transferToStockUOMFactor = 1;
                Packaging pckg = sr.getPackaging();
                if (pckg != null) {
                    orderToStockUOMFactor = pckg.getStockUomQtyFactor(sr.getUom());
                    transferToStockUOMFactor = pckg.getStockUomQtyFactor(product.getTransferUOM());
                }

                jObj.put("id", sr.getId());
                jObj.put("transactiotype", sr.getModule().ordinal());
                jObj.put("transfernoteno", sr.getTransactionNo());
                jObj.put("itemId", product.getID());
                jObj.put("itemcode", product.getProductid());
                jObj.put("itemname", product.getName());
                jObj.put("itemdescription", product.getDescription());
                jObj.put("fromstore", fromStore != null ? fromStore.getId() : "");
                jObj.put("fromStoreCode", fromStore != null ? fromStore.getAbbreviation() : "");
                jObj.put("fromstorename", fromStore != null ? fromStore.getDescription() : "");
                jObj.put("fromstoreadd", fromStore != null ? fromStore.getAddress() : "");
                jObj.put("fromstorefax", fromStore != null ? fromStore.getFaxNo() : "");
                jObj.put("fromstorephno", fromStore != null ? fromStore.getContactNo() : "");
                jObj.put("tostore", toStore != null ? toStore.getId() : "");
                jObj.put("tostoredefaultlocationid", toStore != null ? (toStore.getDefaultLocation() != null ? toStore.getDefaultLocation().getId() : "") : "");
                jObj.put("tostoredefaultlocationname", toStore != null ? (toStore.getDefaultLocation() != null ? toStore.getDefaultLocation().getName() : "") : "");
                jObj.put("toStoreCode", toStore != null ? toStore.getAbbreviation() : "");
                jObj.put("tostorename", toStore != null ? toStore.getDescription() : "");
                jObj.put("tostoreadd", toStore != null ? toStore.getAddress() : "");
                jObj.put("tostorefax", toStore != null ? toStore.getFaxNo() : "");
                jObj.put("tostorephno", toStore != null ? toStore.getContactNo() : "");
                jObj.put("costcenter", (sr.getCostCenter() == null) ? "" : sr.getCostCenter().getCcid());
                jObj.put("costcenterid", (sr.getCostCenter() == null) ? "" : sr.getCostCenter().getID());//ERP-15048
                jObj.put("packaging", (sr.getPackaging() == null) ? "" : sr.getPackaging().toString());
                jObj.put("defaultStoreName", defaultStoreName);
                jObj.put("defaultStoreId", defaultStoreId);
                jObj.put("defaultLocName", defaultLocName);
                jObj.put("defaultLocId", defaultLocId);
                jObj.put("defaultCollectionLocId", defaultCollectionLocId);
                jObj.put("defaultCollectionLocName", defaultCollectionLocName);
                jObj.put("defaultAvailQty", defaultAvailQty);
                jObj.put("uomId", sr.getUom() != null ? sr.getUom().getID() : "");
                jObj.put("name", sr.getUom() != null ? sr.getUom().getNameEmptyforNA() : "");
//                jObj.put("orderinguomname", sr.getProduct().getOrderingUOM() == null ? "" : sr.getProduct().getOrderingUOM().getName());
                jObj.put("orderinguomname", sr.getUom() != null ? sr.getUom().getNameEmptyforNA() : product.getOrderingUOM().getNameEmptyforNA());
                jObj.put("transferinguomname", product.getTransferUOM() == null ? "" : product.getTransferUOM().getNameEmptyforNA());
                jObj.put("stockuomname", product.getUnitOfMeasure() == null ? "" : product.getUnitOfMeasure().getNameEmptyforNA());
                jObj.put("statusId", sr.getStatus().ordinal());
                jObj.put("status", sr.getStatus().toString());
                jObj.put("isReturnRequest", sr.isReturnRequest());
                jObj.put("remark", sr.getRemark());
                jObj.put("returnReason", sr.getReturnReason());
                jObj.put("projectnumber", sr.getProjectNumber());
                jObj.put("quantity", (sr.getIssuedOn() != null && sr.getCollectedOn() != null) ? (sr.getIssuedOn().equals(sr.getCollectedOn()) ? "N.A." : sr.getOrderedQty()) : sr.getOrderedQty());
                jObj.put("nwquantity", "1".equals(type) && sr.getStatus() != RequestStatus.ISSUED ? sr.getOrderedQty() : sr.getIssuedQty());
                jObj.put("delquantity", "3".equals(type) && sr.getStatus() != RequestStatus.COLLECTED ? sr.getIssuedQty() : sr.getDeliveredQty());
                jObj.put("availabelQuantity", availableQty);
                jObj.put("totalOrderedQty", totalOrderedQty);
                jObj.put("isBatchForProduct", product != null ? product.isIsBatchForProduct() : "");
                jObj.put("isSerialForProduct", product != null ? product.isIsSerialForProduct() : "");
                jObj.put("isRowForProduct", product != null ? product.isIsrowforproduct() : false);
                jObj.put("isRackForProduct", product != null ? product.isIsrackforproduct() : false);
                jObj.put("isBinForProduct", product != null ? product.isIsbinforproduct() : false);
                jObj.put("hscode", product != null ? product.getHSCode() : "");
                jObj.put("orderToStockUOMFactor", orderToStockUOMFactor);
                jObj.put("transferToStockUOMFactor", transferToStockUOMFactor);
                jObj.put("itemdefaultwarehouse", product.getWarehouse() != null ? product.getWarehouse().getId() : "");
                jObj.put("isQAEnable", product != null ? product.isQaenable() : false);

                if (sr.getRequestedOn() != null) {
//                    jObj.put("date", df.format(sr.getRequestedOn()));
                    //To change date into user date format as we are removing it from JS side.
                    //As sr.getRequestedOn() date is in UTC, so applying timezone diff. on it.
                    jObj.put("date", authHandler.getUTCToUserLocalDateFormatter_NEW(request, sr.getRequestedOn()));
                }
                if (sr.getBusinessDate() != null) {
//                    jObj.put("date", df.format(sr.getRequestedOn()));
                    //To change date into user date format as we are removing it from JS side.
                    //As sr.getRequestedOn() date is in UTC, so applying timezone diff. on it.
                    jObj.put("bussinessdate", authHandler.getUTCToUserLocalDateFormatter_NEW(request, sr.getBusinessDate()));
                }
                if (sr.getIssuedOn() != null) {
                    jObj.put("issuedOn", authHandler.getUTCToUserLocalDateFormatter_NEW(request, sr.getIssuedOn()));
                    jObj.put("issuedOnFull", df1.format(sr.getIssuedOn()));
                }
                if (sr.getCollectedOn() != null) {
                    jObj.put("collectedOn", authHandler.getUTCToUserLocalDateFormatter_NEW(request, sr.getCollectedOn()));
                    jObj.put("collectedOnFull", df1.format(sr.getCollectedOn()));
                }
                if (sr.getModifiedOn() != null) {
                    jObj.put("modifiedOn", authHandler.getUTCToUserLocalDateFormatter_NEW(request, sr.getModifiedOn()));
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

                jObj.put("availabelQuantity", availableQty);


                if (sr != null) {
                    for (StockRequestDetail srd : sr.getStockRequestDetails()) {
                        if (srd != null) {
                            JSONObject srObject = new JSONObject();
                            srObject.put("id", srd.getId());
                            srObject.put("issuedLocationId", (srd.getIssuedLocation() != null) ? srd.getIssuedLocation().getId() : "");
                            srObject.put("issuedLocationName", (srd.getIssuedLocation() != null) ? srd.getIssuedLocation().getName() : "");
                            srObject.put("issuedRowName", (srd.getIssuedRow() != null) ? srd.getIssuedRow().getName() : "");
                            srObject.put("issuedRackName", (srd.getIssuedRack() != null) ? srd.getIssuedRack().getName() : "");
                            srObject.put("issuedBinName", (srd.getIssuedBin() != null) ? srd.getIssuedBin().getName() : "");
                            srObject.put("issuedQuantity", srd.getIssuedQuantity());
                            srObject.put("issuedSerials", (srd.getIssuedSerialNames() != null) ? srd.getIssuedSerialNames().replace(",", ", ") : "");
                            srObject.put("batchName", (srd.getBatchName() != null) ? srd.getBatchName() : "");
                            NewProductBatch productBatch = stockService.getERPProductBatch(product, fromStore,srd.getIssuedLocation(), srd.getIssuedRow(), srd.getIssuedRack(), srd.getIssuedBin(), ((srd.getBatchName() != null) ? srd.getBatchName() : ""));
                            srObject.put("purchasebatchid",(productBatch != null) ? productBatch.getId():"" );
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

                int moduleId = Constants.Acc_Stock_Request_ModuleId;
                if ("2".equals(type) && sr.getModule().equals(TransactionModule.ISSUE_NOTE)) {
                    moduleId = Constants.Inventory_ModuleId;
                }
                if (moduleId == Constants.Inventory_ModuleId) {
                    Map<String, Object> variableMap = new HashMap<>();
                    KwlReturnObject custumObjResult = accountingHandlerDAO.getObject(StockCustomData.class.getName(), sr.getId());
                    HashMap<String, String> replaceFieldMap = new HashMap<>();
                    if (custumObjResult != null && custumObjResult.getEntityList().size() > 0) {
                        StockCustomData stockDetailCustom = (StockCustomData) custumObjResult.getEntityList().get(0);
                        if (stockDetailCustom != null) {
                            AccountingManager.setCustomColumnValues(stockDetailCustom, FieldMapInventoryModule, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.isExport, isExport);
                            fieldDataManagercntrl.addCustomData(variableMap, customFieldInventoryModuleMap, customDateFieldInventoryModuleMap, jObj, params);
                        }
                    }
                } else {
                    Map<String, Object> variableMap = new HashMap<>();
                    KwlReturnObject custumObjResult = accountingHandlerDAO.getObject(StockCustomData.class.getName(), sr.getId());
                    HashMap<String, String> replaceFieldMap = new HashMap<>();
                    if (custumObjResult != null && custumObjResult.getEntityList().size() > 0) {
                        StockCustomData stockDetailCustom = (StockCustomData) custumObjResult.getEntityList().get(0);
                        if (stockDetailCustom != null) {
                            AccountingManager.setCustomColumnValues(stockDetailCustom, FieldMapStockRequest, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.isExport, isExport);
                            fieldDataManagercntrl.addCustomData(variableMap, customFieldStockRequestMap, customDateFieldStockRequestMap, jObj, params);
                        }
                    }
                }
                jArray.put(jObj);
            }
            if (isExport) {
                jobj.put("data", jArray);
                exportDAO.processRequest(request, response, jobj);
            }
            issuccess = true;
            msg = "Goods Pending Requests have been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView(successView, "model", jobj.toString());

    }
 
    public ModelAndView importInterStoreTransferRequest(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            KwlReturnObject custumObjresult = accountingHandlerDAO.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) custumObjresult.getEntityList().get(0);
            Date bookBeginningDate = preferences.getBookBeginningFrom();
            String doAction = request.getParameter("do");// validate datas
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());
            requestParams.put("bookbeginning", bookBeginningDate);
            requestParams.put("locale", RequestContextUtils.getLocale(request));

            if (doAction.compareToIgnoreCase("import") == 0) {
                System.out.println("A(( Import start : " + new Date());
                JSONObject datajobj = new JSONObject();
                JSONObject resjson = new JSONObject(request.getParameter("resjson").toString());
                JSONArray resjsonJArray = resjson.getJSONArray("root");
                String filename = request.getParameter("filename");
                datajobj.put("filename", filename);
                boolean typeXLSFile = (request.getParameter("typeXLSFile") != null) ? Boolean.parseBoolean(request.getParameter("typeXLSFile")) : false;
                String destinationDirectory = "";
                if (typeXLSFile) {
                    destinationDirectory = storageHandlerImpl.GetDocStorePath() + "xlsfiles";
                } else {
                    destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
                }

                File filepath = new File(destinationDirectory + "/" + filename);
                datajobj.put("FilePath", filepath);
                String currencyid = sessionHandlerImpl.getCurrencyID(request);
                String dateFormatId = request.getParameter("dateFormat");
                String isQAActivated = request.getParameter("isQAActivated");
                datajobj.put("resjson", resjsonJArray);
                requestParams.put("importflag", Constants.importproductopeningqty);
                requestParams.put("currencyId", currencyid);
                datajobj.put("dateformateproduct", dateFormatId);
                datajobj.put("isQAActivated", isQAActivated);
                requestParams.put("updateExistingRecordFlag", request.getParameter("updateExistingRecordFlag"));
                requestParams.put("jobj", datajobj);
                String exceededLimit = request.getParameter("exceededLimit");
                Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                Map<String, Object> jeDataMap1 = AccountingManager.getGlobalParams(request);
                Locale locale = RequestContextUtils.getLocale(request);
                requestParams.put("jeDataMap", jeDataMap);
                requestParams.put("jeDataMap1", jeDataMap1);
                requestParams.put("locale", locale);
                requestParams.put("typeXLSFile", typeXLSFile);
                int totalRecsInFile = 0;

                jobj = importInvData.importInterStoreTransferRecord(requestParams, jeDataMap, jeDataMap1, locale);

            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                System.out.println("A(( Validation start : " + new Date());
                jobj = importHandler.validateFileData(requestParams);
                System.out.println("A(( Validation end : " + new Date());
            }

            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(GoodsTransferController.class.getName()).log(Level.SEVERE, null, ex);
            }

            Logger.getLogger(GoodsTransferController.class.getName()).log(Level.SEVERE, null, ex);

        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getInterStockTransferList(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        String view = "jsonView_ex";
        String fileType = request.getParameter("filetype");                    
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IST_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        String start = request.getParameter("start");
        String limit = request.getParameter("limit");
        boolean isJobWorkOutRemain = !StringUtil.isNullOrEmpty(request.getParameter("isJobWorkOutRemain")) ? Boolean.parseBoolean(request.getParameter("isJobWorkOutRemain")) : false;
        try {

            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            
            paging = new Paging(start, limit);

            String searchString = request.getParameter("ss");

            String fd = request.getParameter("frmDate");
            String td = request.getParameter("toDate");
            String searchJson="";
            String filterConjuctionCriteria="";
            
            String asofdate = request.getParameter("asofdate");
            boolean isJobWorkStockOut = !StringUtil.isNullOrEmpty(request.getParameter("jobWorkStockOut")) ? Boolean.parseBoolean(request.getParameter("jobWorkStockOut")) : false;
            if (request.getParameter("searchJson") != null) {
                searchJson = request.getParameter("searchJson").toString();
            }
            if (request.getParameter("filterConjuctionCriteria") != null) {
                filterConjuctionCriteria = request.getParameter("filterConjuctionCriteria").toString();
            }
            Map<String, Object> reqMap = new HashMap();
            if (isJobWorkStockOut) {
                reqMap.put("isJobWorkStockOut", isJobWorkStockOut);
            }
            reqMap.put("searchJson", searchJson);
            reqMap.put("filterConjuctionCriteria", filterConjuctionCriteria);
            String storeId = request.getParameter("storeid");
            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
                paging = null;
            }
            Date fromDate = null;
            Date toDate = null;
            Date asOfDate = null;
            try {
                if (!StringUtil.isNullOrEmpty(fd) && !StringUtil.isNullOrEmpty(td)) {
                    fromDate = df.parse(fd);
                    toDate = df.parse(td);
                }
                if (!StringUtil.isNullOrEmpty(asofdate)) {
                    asOfDate = df.parse(asofdate);
                }
                
            } catch (ParseException ex) {
            }

            Store store = null;
            Set<Store> storeSet = new HashSet();
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
                storeSet.add(store);
            } else {
                List storeListByManager = storeService.getStoresByStoreManagers(user, true, null, null, null);
                List storeListByExecutive = storeService.getStoresByStoreExecutives(user, true, null, null, null);
                storeSet.addAll(storeListByManager);
                storeSet.addAll(storeListByExecutive);
            }

            String type = request.getParameter("type");
            List<InterStoreTransferRequest> interStoreTransferList = new ArrayList();
            //sending My Account time zone diff to compare with created on Date.
            String TZdiff = sessionHandlerImpl.getTimeZoneDifference(request);
            if ("1".equals(type)) {
                interStoreTransferList = istService.getIncommingInterStoreTransferList(user, store, fromDate, toDate, searchString, paging, TZdiff,reqMap);
            } else if ("3".equals(type)) {
                interStoreTransferList = istService.getOutgoingInterStoreTransferList(user, store, fromDate, toDate, searchString, paging, TZdiff,reqMap);
            } else if ("2".equals(type)) {
                interStoreTransferList = istService.getCompletedInterStoreTransferList(user.getCompany(), storeSet, fromDate, toDate, searchString, paging, TZdiff,reqMap);
            }

            for (InterStoreTransferRequest ist : interStoreTransferList) {
                JSONObject jObj = new JSONObject();
                JSONArray stockDetails = new JSONArray();

                double orderToStockUOMFactor = 1;
                double transferToStockUOMFactor = 1;
                Packaging pckg = ist.getPackaging();
                if (pckg != null) {
                    orderToStockUOMFactor = 1;//pckg.getStockUomQtyFactor(ist.getProduct().getOrderingUOM());
                    transferToStockUOMFactor = pckg.getStockUomQtyFactor(ist.getUom());
                }
                
                String defaultStoreName = null;
                String defaultStoreId = null;
                String defaultLocName = null;
                String defaultLocId = null;
                String defaultCollectionLocId = null;
                String defaultCollectionLocName = null;
                double defaultAvailQty = 0;
                String podetails=ist.getPurchaseOrderDetail() != null ? ist.getPurchaseOrderDetail() : "";
                String companyId=user.getCompany().getCompanyID() != null ? user.getCompany().getCompanyID() : "";
                Product product=ist.getProduct();
                if(product != null && !(product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct() || product.isIsBatchForProduct() || product.isIsSerialForProduct())){
                    if(product.getWarehouse() != null && product.getLocation() != null){
                        defaultStoreId = product.getWarehouse().getId();
                        defaultLocId = product.getLocation().getId();
                        defaultStoreName = product.getWarehouse().getName();
                        defaultLocName = product.getLocation().getName();
                        Stock stock = stockService.getStock(product.getID(), defaultStoreId, defaultLocId, null, null, null, null);
                        if(stock != null){
                            defaultAvailQty = stock.getQuantity();
                        }
                    }
                }
                if (ist.getToStore() != null && ist.getToStore().getDefaultLocation() != null) {
                    defaultCollectionLocId = ist.getToStore().getDefaultLocation().getId();
                    defaultCollectionLocName = ist.getToStore().getDefaultLocation().getName();
                }
                if ("2".equals(type) && isJobWorkStockOut) {
                    KwlReturnObject venNameAndPOnumberObj = istService.getVendorNameAndJWONo(podetails, companyId);
                    List<Object[]> jwoObj = venNameAndPOnumberObj.getEntityList();
                    for (Object[] jwonoobj : jwoObj) {
                        if (jwonoobj != null) {
                            jObj.put("vendorname", jwonoobj[0]);
                            jObj.put("jobWorkOrderNo", jwonoobj[1]);
                            jObj.put("personid", jwonoobj[2]);
                            jObj.put("personname", jwonoobj[0]);
                            jObj.put("billid", jwonoobj[3]);
                            jObj.put("currency", jwonoobj[4]);
                        }
                    }
                }
                /*
                 * isJobWorkOutRemain is true if sales invoice is creating from Aged order work report.
                 */
                if (isJobWorkOutRemain) {
                    if (!ist.isIsjobWorkClose()) {
                        jObj.put("statusJob", "Open");
                    } else {
                        continue;
                    }
                    double usedqty = accAccountDAOobj.getSumofChallanUsedQuantity(ist.getId());
                    double balQty=ist.getOrderedQty() - usedqty;
                    if(balQty==0.0){
                        continue;
                    }
                    jObj.put("balquantity", ist.getOrderedQty() - usedqty);
                    jObj.put("recQuantity", usedqty);
                    long differenceDays = (asOfDate.getTime() - ist.getBusinessDate().getTime()) / 86400000;
                    jObj.put("ageingdays", differenceDays + " days");
                }
                jObj.put("id", ist.getId());
                jObj.put("transfernoteno", ist.getTransactionNo());
                jObj.put("itemId", ist.getProduct().getID());
                jObj.put("itemcode", ist.getProduct().getProductid());
                jObj.put("itemname", ist.getProduct().getName());
                jObj.put("partnumber", ist.getProduct().getCoilcraft());
                jObj.put("itemdescription", ist.getProduct().getDescription());
                jObj.put("fromStoreId", ist.getFromStore().getId());
                jObj.put("fromstorename", ist.getFromStore().getAbbreviation());
                jObj.put("fromStoreName", ist.getFromStore().getAbbreviation());
                jObj.put("fromstoreadd", ist.getFromStore() != null ? ist.getFromStore().getAddress() : "");
                jObj.put("fromstorefax", ist.getFromStore() != null ? ist.getFromStore().getFaxNo() : "");
                jObj.put("fromstorephno", ist.getFromStore() != null ? ist.getFromStore().getContactNo() : "");
                jObj.put("toStoreId", ist.getToStore().getId());
                jObj.put("tostorename", ist.getToStore().getAbbreviation());
                jObj.put("toStoreName", ist.getToStore().getAbbreviation());
                jObj.put("tostoreadd", ist.getToStore() != null ? ist.getToStore().getAddress() : "");
                jObj.put("tostorefax", ist.getToStore() != null ? ist.getToStore().getFaxNo() : "");
                jObj.put("tostorephno", ist.getToStore() != null ? ist.getToStore().getContactNo() : "");
                jObj.put("packaging", ist.getPackaging() != null ? ist.getPackaging().toString() : "");
                jObj.put("uomid", ist.getUom()!=null?ist.getUom().getID():"");
                jObj.put("name", ist.getUom()!=null?ist.getUom().getNameEmptyforNA():"");
                jObj.put("orderinguomname", ist.getProduct().getOrderingUOM() == null ? "" : ist.getProduct().getOrderingUOM().getNameEmptyforNA());
                jObj.put("transferinguomname", ist.getUom() != null ? ist.getUom().getNameEmptyforNA() : "");
//                jObj.put("transferinguomname", ist.getProduct().getTransferUOM() == null ? "" : ist.getProduct().getTransferUOM().getName());
                jObj.put("stockuomname", ist.getProduct().getUnitOfMeasure() == null ? "" : ist.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                jObj.put("statusId", ist.getStatus().ordinal());
                jObj.put("status", ist.getStatus().toString());
                jObj.put("remark", ist.getRemark());
                jObj.put("quantity", ist.getOrderedQty());
                jObj.put("acceptedqty", ist.getAcceptedQty());
                jObj.put("isBatchForProduct", ist.getProduct() != null ? ist.getProduct().isIsBatchForProduct() : "");
                jObj.put("isRowForProduct", ist.getProduct() != null ? ist.getProduct().isIsrowforproduct() : false);
                jObj.put("isRackForProduct", ist.getProduct() != null ? ist.getProduct().isIsrackforproduct() : false);
                jObj.put("isBinForProduct", ist.getProduct() != null ? ist.getProduct().isIsbinforproduct() : false);
                jObj.put("hscode", ist.getProduct() != null ? ist.getProduct().getHSCode() : "");
                jObj.put("isSerialForProduct", ist.getProduct() != null ? ist.getProduct().isIsSerialForProduct() : "");
                jObj.put("orderToStockUOMFactor", orderToStockUOMFactor);
                jObj.put("transferToStockUOMFactor", transferToStockUOMFactor);
                jObj.put("costcenter", ist.getCostCenter() == null ? "" : ist.getCostCenter().getCcid());
                jObj.put("defaultStoreName", defaultStoreName);//product's default warehouse id
                jObj.put("defaultStoreId", defaultStoreId);//product's default warehouse name
                jObj.put("defaultLocName", defaultLocName);//product's default location id
                jObj.put("defaultLocId", defaultLocId);//product's default location name
                jObj.put("defaultCollectionLocId", defaultCollectionLocId);//To Store's default location id
                jObj.put("defaultCollectionLocName", defaultCollectionLocName);//To Store's default location name
                jObj.put("defaultAvailQty", defaultAvailQty);
                jObj.put("memo", ist.getMemo());
                jObj.put("challanno", ist.getChallanNumber()!=null?ist.getChallanNumber().getChallanNumber():"");
                jObj.put("isQAEnable", product != null ? product.isQaenable() : false);

                if (ist != null) {
                    for (ISTDetail istd : ist.getIstDetails()) {
                        if (istd != null) {
                            JSONObject srObject = new JSONObject();
                            srObject.put("id", istd.getId());
                            
                            srObject.put("issuedLocationId", (istd.getIssuedLocation() != null) ? istd.getIssuedLocation().getId() : "");
                            srObject.put("issuedRowId", (istd.getIssuedRow() != null) ? istd.getIssuedRow().getId() : "");
                            srObject.put("issuedRackId", (istd.getIssuedRack() != null) ? istd.getIssuedRack().getId() : "");
                            srObject.put("issuedBinId", (istd.getIssuedBin() != null) ? istd.getIssuedBin().getId() : "");
                            srObject.put("issuedLocationName", (istd.getIssuedLocation() != null) ? istd.getIssuedLocation().getName() : "");
                            srObject.put("issuedRowName", (istd.getIssuedRow() != null) ? istd.getIssuedRow().getName() : "");
                            srObject.put("issuedRackName", (istd.getIssuedRack() != null) ? istd.getIssuedRack().getName() : "");
                            srObject.put("issuedBinName", (istd.getIssuedBin() != null) ? istd.getIssuedBin().getName() : "");
                            srObject.put("issuedQuantity", istd.getIssuedQuantity());
                            srObject.put("issuedSerials", (istd.getIssuedSerialNames() != null) ? istd.getIssuedSerialNames().replace(",", ", ") : "");
                            srObject.put("batchName", (istd.getBatchName() != null) ? istd.getBatchName() : "");
                            srObject.put("collectedLocationName", (istd.getDeliveredLocation() != null) ? istd.getDeliveredLocation().getName() : "");
                            srObject.put("collectedRowName", (istd.getDeliveredRow() != null) ? istd.getDeliveredRow().getName() : "");
                            srObject.put("collectedRackName", (istd.getDeliveredRack() != null) ? istd.getDeliveredRack().getName() : "");
                            srObject.put("collectedBinName", (istd.getDeliveredBin() != null) ? istd.getDeliveredBin().getName() : "");
                            srObject.put("collectedQuantity", istd.getDeliveredQuantity());
                            srObject.put("collectedSerials", (istd.getDeliveredSerialNames() != null) ? istd.getDeliveredSerialNames().replace(",", ", ") : "");
                            stockDetails.put(srObject);
                        }
                    }
                }
                jObj.put("stockDetails", stockDetails);

                //To change UTC date into user date format as we are removing it from JS side.
                if (ist.getCreatedOn() != null) {
                    jObj.put("date", authHandler.getUTCToUserLocalDateFormatter_NEW(request, ist.getBusinessDate()));
                }
                if (ist.getModifiedOn() != null) {
                    jObj.put("modifiedOn", authHandler.getUTCToUserLocalDateFormatter_NEW(request, ist.getModifiedOn()));
                }

                if (ist.getCreatedBy() != null) {
                    jObj.put("createdby", ist.getCreatedBy().getFullName());
                }

                if (ist.getModifiedBy() != null) {
                    jObj.put("modifiedBy", ist.getModifiedBy().getFullName());
                }

                if (ist.getApprovedBy() != null) {
                    if (ist.getStatus() == InterStoreTransferStatus.REJECTED) {
                        jObj.put("rejectedBy", ist.getApprovedBy().getFullName());
                    } else {
                        jObj.put("approvedBy", ist.getApprovedBy().getFullName());
                    }
                }

                /*
                 Global & Line level Custom data
                 */
                HashMap<String, String> customFieldMap = new HashMap<>();
                HashMap<String, String> customDateFieldMap = new HashMap<>();
                HashMap<String, Object> fieldrequestParams = new HashMap();
                HashMap<String, String> replaceFieldMap = new HashMap<>();
                Map<String, Object> variableMap = new HashMap<>();
                KwlReturnObject custumObjresult = null;
                
                 if (!isJobWorkOutRemain) {
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(user.getCompany().getCompanyID(), Constants.Acc_InterStore_ModuleId));
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                custumObjresult = accountingHandlerDAO.getObject(InterStoreTransferCustomData.class.getName(), ist.getId());
                replaceFieldMap = new HashMap<>();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        InterStoreTransferCustomData stockDetailCustom = (InterStoreTransferCustomData) custumObjresult.getEntityList().get(0);
                        if (stockDetailCustom != null) {
                            AccountingManager.setCustomColumnValues(stockDetailCustom, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.isExport, isExport);
                            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jObj, params);
                        }
                    }
                }
                /*
                 * Get Custome column of product master in aged job work report
                 */
                if (isJobWorkOutRemain) {
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(user.getCompany().getCompanyID(), Constants.Acc_Product_Master_ModuleId));
                    HashMap<String, Integer> FieldMapProd = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    custumObjresult = accountingHandlerDAO.getObject(AccProductCustomData.class.getName(), ist.getProduct().getID());
                    replaceFieldMap = new HashMap<>();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        AccProductCustomData stockDetailCustom = (AccProductCustomData) custumObjresult.getEntityList().get(0);
                        if (stockDetailCustom != null) {
                            AccountingManager.setCustomColumnValues(stockDetailCustom, FieldMapProd, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.isExport, isExport);
                            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jObj, params);
                        }
                    }
                }
                jArray.put(jObj);
            }
            view = successView;
            if (isExport) {
                if (StringUtil.equal(fileType, "print") && isJobWorkOutRemain) {
                    String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                    jobj.put("GenerateDate", GenerateDate);
                    view = "jsonView-empty";
                }
                jobj.put("data", jArray);
                exportDAO.processRequest(request, response, jobj);
            }
            issuccess = true;
            msg = "Inter Store Stock Transfer have been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit) && isJobWorkOutRemain) {
                    JSONArray pagedJson = jArray;
                    pagedJson = StringUtil.getPagedJSON(jArray, Integer.parseInt(start), Integer.parseInt(limit));
                    jobj.put("data", pagedJson);
                } else {
                    jobj.put("data", jArray);
                }
                if (paging != null&&!isJobWorkOutRemain) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(view, "model", jobj.toString());

    }

    public ModelAndView getInterLocationTransferList(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IST_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);
            HashMap<String, String> customFieldMap = new HashMap<>();
            HashMap<String, String> customDateFieldMap = new HashMap<>();
            HashMap<String, Object> fieldRequestParams = new HashMap();
            HashMap<String, String> replaceFieldMap = new HashMap<>();
            fieldRequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldRequestParams.put(Constants.filter_values, Arrays.asList(user.getCompany().getCompanyID(), Constants.Acc_InterLocation_ModuleId));
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldRequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
            String searchString = request.getParameter("ss");

            String fd = request.getParameter("frmDate");
            String td = request.getParameter("toDate");

            String storeId = request.getParameter("storeid");
            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
                paging = null;
            }
            Date fromDate = null;
            Date toDate = null;
            try {
                if (!StringUtil.isNullOrEmpty(fd) && !StringUtil.isNullOrEmpty(td)) {
                    fromDate = df.parse(fd);
                    toDate = df.parse(td);
                }
            } catch (ParseException ex) {
            }

            Store store = null;
            Set<Store> storeSet = new HashSet();
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
                storeSet.add(store);
            } else {
                List storeListByManager = storeService.getStoresByStoreManagers(user, true, null, null, null);
                List storeListByExecutive = storeService.getStoresByStoreExecutives(user, true, null, null, null);
                storeSet.addAll(storeListByManager);
                storeSet.addAll(storeListByExecutive);
            }
            //sending My Account time zone diff to compare with created on Date.
            String TZdiff = sessionHandlerImpl.getTimeZoneDifference(request);
            List<ISTDetail> interStoreTransferList = new ArrayList();
            interStoreTransferList = istService.getInterLocationTransferListByDetailwise(user.getCompany(), storeSet, fromDate, toDate, searchString, paging, TZdiff);


            for (ISTDetail istd : interStoreTransferList) {
                JSONObject jObj = new JSONObject();
                JSONArray stockDetails = new JSONArray();
                InterStoreTransferRequest ist = istd.getIstRequest();
                double orderToStockUOMFactor = 1;
                double transferToStockUOMFactor = 1;
                Packaging pckg = ist.getProduct().getPackaging();
                if (pckg != null) {
                    orderToStockUOMFactor = pckg.getStockUomQtyFactor(ist.getProduct().getOrderingUOM());
                    transferToStockUOMFactor = pckg.getStockUomQtyFactor(ist.getProduct().getTransferUOM());
                }

                jObj.put("id", ist.getId());
                jObj.put("transfernoteno", ist.getTransactionNo());
                jObj.put("itemId", ist.getProduct().getID());
                jObj.put("itemcode", ist.getProduct().getProductid());
                jObj.put("itemname", ist.getProduct().getName());
                jObj.put("itemdescription", ist.getProduct().getDescription());
                jObj.put("fromStoreId", ist.getFromStore().getId());
                jObj.put("fromstorename", ist.getFromStore().getDescription());
                jObj.put("fromStoreName", ist.getFromStore().getDescription());
                jObj.put("packaging", ist.getPackaging() != null ? ist.getPackaging().toString() : "");
                jObj.put("uomid", ist.getUom().getID());
                jObj.put("name", ist.getUom().getNameEmptyforNA());
                jObj.put("memo", ist.getMemo());
                jObj.put("orderinguomname", ist.getProduct().getOrderingUOM() == null ? "" : ist.getProduct().getOrderingUOM().getNameEmptyforNA());
                jObj.put("transferinguomname", ist.getProduct().getTransferUOM() == null ? "" : ist.getProduct().getTransferUOM().getNameEmptyforNA());
                jObj.put("stockuomname", ist.getProduct().getUnitOfMeasure() == null ? "" : ist.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                jObj.put("remark", ist.getRemark());
                jObj.put("quantity", authHandler.formattedQuantity(istd.getIssuedQuantity(),user.getCompany().getCompanyID()));
                jObj.put("isBatchForProduct", ist.getProduct() != null ? ist.getProduct().isIsBatchForProduct() : "");
                jObj.put("isSerialForProduct", ist.getProduct() != null ? ist.getProduct().isIsSerialForProduct() : "");
                jObj.put("isRowForProduct", ist.getProduct() != null ? ist.getProduct().isIsrowforproduct() : "");
                jObj.put("isRackForProduct", ist.getProduct() != null ? ist.getProduct().isIsrackforproduct() : "");
                jObj.put("isBinForProduct", ist.getProduct() != null ? ist.getProduct().isIsbinforproduct() : "");
                jObj.put("orderToStockUOMFactor", orderToStockUOMFactor);
                jObj.put("transferToStockUOMFactor", transferToStockUOMFactor);
                jObj.put("costcenter", ist.getCostCenter() == null ? "" : ist.getCostCenter().getCcid());
                jObj.put("fromlocation", (istd.getIssuedLocation() != null) ? istd.getIssuedLocation().getName() : "");
                jObj.put("tolocation", (istd.getDeliveredLocation() != null) ? istd.getDeliveredLocation().getName() : "");
                JSONObject srObject = new JSONObject();
                srObject.put("id", istd.getId());
                srObject.put("issuedLocationName", (istd.getIssuedLocation() != null) ? istd.getIssuedLocation().getName() : "");
                srObject.put("issuedRowName", (istd.getIssuedRow() != null) ? istd.getIssuedRow().getName() : "");
                srObject.put("issuedRackName", (istd.getIssuedRack() != null) ? istd.getIssuedRack().getName() : "");
                srObject.put("issuedBinName", (istd.getIssuedBin() != null) ? istd.getIssuedBin().getName() : "");
                srObject.put("issuedQuantity", authHandler.formattedQuantity(istd.getIssuedQuantity(),user.getCompany().getCompanyID()));
                srObject.put("issuedSerials", (istd.getIssuedSerialNames() != null) ? istd.getIssuedSerialNames().replace(",", ", ") : "");
                srObject.put("batchName", (istd.getBatchName() != null) ? istd.getBatchName() : "");
                srObject.put("collectedLocationName", (istd.getDeliveredLocation() != null) ? istd.getDeliveredLocation().getName() : "");
                srObject.put("collectedRowName", (istd.getDeliveredRow() != null) ? istd.getDeliveredRow().getName() : "");
                srObject.put("collectedRackName", (istd.getDeliveredRack() != null) ? istd.getDeliveredRack().getName() : "");
                srObject.put("collectedBinName", (istd.getDeliveredBin() != null) ? istd.getDeliveredBin().getName() : "");
                srObject.put("collectedQuantity", authHandler.formattedQuantity(istd.getDeliveredQuantity(),user.getCompany().getCompanyID()));
                srObject.put("collectedSerials", (istd.getDeliveredSerialNames() != null) ? istd.getDeliveredSerialNames().replace(",", ", ") : "");
                stockDetails.put(srObject);

                jObj.put("stockDetails", stockDetails);


                if (ist.getCreatedOn() != null) {
                    jObj.put("date", authHandler.getUTCToUserLocalDateFormatter_NEW(request, ist.getBusinessDate()));
                }
                if (ist.getModifiedOn() != null) {
                    jObj.put("modifiedOn", authHandler.getUTCToUserLocalDateFormatter_NEW(request, ist.getModifiedOn()));
                }

                if (ist.getCreatedBy() != null) {
                    jObj.put("createdby", ist.getCreatedBy().getFullName());
                }

                if (ist.getModifiedBy() != null) {
                    jObj.put("modifiedBy", ist.getModifiedBy().getFullName());
                }

                if (ist.getApprovedBy() != null) {
                    if (ist.getStatus() == InterStoreTransferStatus.REJECTED) {
                        jObj.put("rejectedBy", ist.getApprovedBy().getFullName());
                    } else {
                        jObj.put("approvedBy", ist.getApprovedBy().getFullName());
                    }
                }
                /*
                 *Get Global and Line Level Custom data
                 */
                KwlReturnObject customObjResult = null;
                Map<String, Object> variableMap = new HashMap<>();
                customObjResult = accountingHandlerDAO.getObject(InterStoreTransferCustomData.class.getName(), ist.getId());
                replaceFieldMap = new HashMap<>();
                if (customObjResult != null && customObjResult.getEntityList().size() > 0) {
                    InterStoreTransferCustomData stockDetailCustomData = (InterStoreTransferCustomData) customObjResult.getEntityList().get(0);
                    if (stockDetailCustomData != null) {
                        AccountingManager.setCustomColumnValues(stockDetailCustomData, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, isExport);
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jObj, params);
                    }
                }
                jArray.put(jObj);
            }
            if (isExport) {
                jobj.put("data", jArray);
                exportDAO.processRequest(request, response, jobj);
            }
            issuccess = true;
            msg = "Inter Store Stock Transfer have been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView addStockOrderRequest(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SOR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String fromStoreId = request.getParameter("fromstore");
            Store fromStore = storeService.getStoreById(fromStoreId);
            
            Map<String, Object> requestParams = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.customfield))) {
                requestParams.put(Constants.customfield, request.getParameter(Constants.customfield));
            }
            
            boolean UomSchemaType = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("UomSchemaType"))) {
                UomSchemaType = Boolean.parseBoolean(request.getParameter("UomSchemaType"));
            }
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            int istemplate = paramJobj.optString("istemplate", null) != null ? Integer.parseInt(paramJobj.getString("istemplate")) : 0;
            String transactionNo = request.getParameter("seqno")!=null?request.getParameter("seqno"):"";
            String seqFormatId = request.getParameter("seqFormatId")!=null?request.getParameter("seqFormatId"):"";
            String documentNumber = request.getParameter("documentNumber")!=null?request.getParameter("documentNumber"):"";
            String templatename = request.getParameter("templatename")!=null?request.getParameter("templatename"):"";
            Date businessDate = df.parse(request.getParameter("businessdate"));
            String records = request.getParameter("jsondata");
            JSONArray jArr = new JSONArray(records);

            /**
             * No Need to check sequence no for template 
             */
            SeqFormat seqFormat = null;
            if (!"NA".equals(seqFormatId) && istemplate==0) {
                if (!StringUtil.isNullOrEmpty(seqFormatId)) {
                    seqFormat = seqService.getSeqFormat(seqFormatId);
                } else {
                    seqFormat = seqService.getDefaultSeqFormat(company, ModuleConst.STOCK_REQUEST);
                }
            }
            synchronized (this) {
                boolean seqExist = false;
                if ("NA".equals(seqFormatId) && istemplate==0) {
                    transactionNo = documentNumber;
                    seqExist = seqService.isExistingSeqNumber(transactionNo, company, ModuleConst.STOCK_REQUEST);
                    if (seqExist) {
                        throw new InventoryException("Sequence number already exist, please enter other one.");
                    }
                } else if (istemplate==0){
                    seqExist = false;
                    do {
                        transactionNo = seqService.getNextFormatedSeqNumber(seqFormat);
                        seqExist = seqService.isExistingSeqNumber(transactionNo, company, ModuleConst.STOCK_REQUEST);
                        if (seqExist) {
                            seqService.updateSeqNumber(seqFormat);
                        }
                    } while (seqExist);
                }
                int productDetails=0;
                transactionNo = seqFormat != null ? seqService.getNextFormatedSeqNumber(seqFormat) : documentNumber;
                if (!seqExist) {
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject jObj = jArr.optJSONObject(i);
                        String itemId = jObj.optString("productid");
                        String uomId = jObj.optString("uomid");

                        String uomname = jObj.optString("uomname");

                        String packagingId = jObj.optString("packagingid");
                        String remark = jObj.optString("remark");
                        String costCenterId = jObj.optString("costcenter");
                        double quantity = jObj.optDouble("quantity", 0);
                        String projectNumber = jObj.optString("projectnumber", "");
                        double confactor = jObj.has("confactor") ? jObj.optDouble("confactor", 1) : 1;
                        
                        if (jObj.has(Constants.LineLevelCustomData) &&!StringUtil.isNullOrEmpty(jObj.optString(Constants.LineLevelCustomData))) {
                            requestParams.put(Constants.LineLevelCustomData, jObj.optString(Constants.LineLevelCustomData));
                        }

                        jeresult = accountingHandlerDAO.getObject(Product.class.getName(), itemId);
                        Product product = (Product) jeresult.getEntityList().get(0);

                        jeresult = accountingHandlerDAO.getObject(UnitOfMeasure.class.getName(), uomname);
                        UnitOfMeasure uom = (UnitOfMeasure) jeresult.getEntityList().get(0);
                        if (uom == null) {
                            jeresult = accountingHandlerDAO.getObject(UnitOfMeasure.class.getName(), uomId);
                            uom = (UnitOfMeasure) jeresult.getEntityList().get(0);
                        }

                        Packaging packaging = new Packaging();
                        if (uom==product.getUnitOfMeasure()){
                            packaging = packagingService.getPackaging(packagingId);
                        } else {
                        if (UomSchemaType) {
//                        quantity = quantity * confactor;
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

                        jeresult = accountingHandlerDAO.getObject(CostCenter.class.getName(), costCenterId);
                        CostCenter costCenter = (CostCenter) jeresult.getEntityList().get(0);

                        InventoryWarehouse warehouse = product.getWarehouse();

                        if (warehouse == null) {
                            issuccess = false;
                            msg = "Warehouse has not been set for Product : " + product.getProductid();
                            break;
                        }

                        Store toStore = storeService.getStoreById(warehouse.getId());
                        StockRequest stockRequest = new StockRequest(product, fromStore, toStore, uom, quantity);
                        stockRequest.setCostCenter(costCenter);
                        stockRequest.setRemark(remark);
                        stockRequest.setUom(uom);
                        stockRequest.setPackaging(packaging);
                        if (!StringUtil.isNullOrEmpty(paramJobj.optString("templatename", null)) && (istemplate == 1 || istemplate == 2)) {
                            stockRequest.setTransactionNo(templatename);
                        } else {
                            stockRequest.setTransactionNo(transactionNo);
                        }
                        stockRequest.setProjectNumber(projectNumber);
                        stockRequest.setBusinessDate(businessDate);
                        stockRequest.setCostCenter(costCenter);
                        stockRequest.setIstemplate(istemplate);
                        stockRequest.setModule(TransactionModule.STOCK_REQUEST);
                        stockRequestService.addStockOrderRequest(user, stockRequest,requestParams);
                        issuccess = true;
                        jobj.put("billid", stockRequest.getId());
                        if (!StringUtil.isNullOrEmpty(auditMessage)) {
                            auditMessage += ", ";
                        }
                        auditMessage += "(Product :" + product.getProductid() + ", Quantity :" + quantity + " " + uom.getNameEmptyforNA() + ")";
                    }
                    /**
                     * Save Template record
                     */

                    if (!StringUtil.isNullOrEmpty(paramJobj.optString("templatename", null)) && (istemplate == 1 || istemplate == 2)) {
                        HashMap<String, Object> hashMap = new HashMap<String, Object>();
                        String moduletemplateid = paramJobj.optString("moduletemplateid");
                        hashMap.put("templatename", paramJobj.optString("templatename", null));
                        if (!StringUtil.isNullOrEmpty(moduletemplateid)) {
                            hashMap.put("moduletemplateid", moduletemplateid);
                        }
                        hashMap.put("companyunitid", paramJobj.optString("companyunitid", null));
                        hashMap.put(Constants.moduleid, Constants.Acc_Stock_Request_ModuleId);
                        hashMap.put("modulerecordid", templatename);
                        hashMap.put(Constants.companyKey, companyId);
                        if (!StringUtil.isNullOrEmpty(paramJobj.optString("companyunitid", null))) {
                            hashMap.put("companyunitid", paramJobj.getString("companyunitid")); // Added Unit ID if it is present in request
                        }
                        /**
                         * checks the template name is already exist in create
                         * and edit template case
                         */
                        KwlReturnObject result = accountingHandlerDAO.getModuleTemplateForTemplatename(hashMap);
                        int nocount = result.getRecordTotalCount();
                        if (nocount > 0 && productDetails == 0) {
                            issuccess = false;
                            throw new AccountingException(messageSource.getMessage("acc.tmp.templateNameAlreadyExists", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                        }
                        accountingHandlerDAO.saveModuleTemplate(hashMap);
                        productDetails++;
                    }
                }
                if (!"NA".equals(seqFormatId) && istemplate==0) {
                    seqService.updateSeqNumber(seqFormat);
                }
            }
            auditMessage = "User " + user.getFullName() + " has added Stock Request: " + transactionNo + " for Store: " + fromStore.getAbbreviation() + ", " + auditMessage;
            auditTrailObj.insertAuditLog(AuditAction.STOCK_REQUEST_ADDED, auditMessage, request, "0");
            if (!issuccess) {
                txnManager.rollback(status);
            } else {
                /**
                 * saving message i.e(Stock Request Template has been saved successfully.) when we save template for Stock request.
                 */
                if (istemplate == 1 || istemplate == 2) {
                    msg = messageSource.getMessage("acc.up.42", null, RequestContextUtils.getLocale(request)) + " "+ messageSource.getMessage("acc.nee.22", null, RequestContextUtils.getLocale(request));
                } else {
                    /**
                     * saving message i.e(Stock Request has been added successfully) when we save normal Stock request record.
                     */
                    msg = messageSource.getMessage("acc.up.42", null, RequestContextUtils.getLocale(request)) + ": " + transactionNo + " " + messageSource.getMessage("acc.stockrequest.hasbeenaddedsuccessfully", null, RequestContextUtils.getLocale(request));
                }
                txnManager.commit(status);
                jobj.put("OrderNoteNo", transactionNo);
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView approveRejectStockOrderRequest(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            String requestIds = request.getParameter("jsondata");
            String type = request.getParameter("type");
            String auditType = "";
            if (!StringUtil.isNullOrEmpty(requestIds)) {
                String[] reqIdArr = requestIds.split(",");
                for (String reqId : reqIdArr) {
                    StockRequest stockRequest = stockRequestService.getStockRequestById(reqId);
                    Product product = stockRequest.getProduct();
                    if ("APPROVE".equalsIgnoreCase(type)) {
                        stockRequestService.approveStockOrderRequest(user, stockRequest);
                        msg = "Request has been approved successfully";
                        auditType = "Approved";
                    } else if ("REJECT".equalsIgnoreCase(type)) {
                        stockRequestService.rejectStockOrderRequest(user, stockRequest);
                        msg = "Request has been rejected successfully";
                        auditType = "Rejected";
                    }
                    if (!StringUtil.isNullOrEmpty(auditMessage)) {
                        auditMessage += ", ";
                    }
                    auditMessage += "(Stock Request: " + stockRequest.getTransactionNo() + " for Store: " + stockRequest.getToStore().getAbbreviation() + ", Product :" + product.getProductid() + ", Quantity :" + stockRequest.getOrderedQty() + " " + stockRequest.getUom().getNameEmptyforNA() + ")";
                }
            } else {
                msg = "No any record selected for approval";
            }

            issuccess = true;

            auditMessage = "User " + user.getFullName() + " has " + auditType + " Stock Requests - " + auditMessage;
            auditTrailObj.insertAuditLog(AuditAction.STOCK_REQUEST_ISSUED, auditMessage, request, "0");

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView issueStockOrderRequest(HttpServletRequest request, HttpServletResponse response) throws JSONException, AccountingException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        NewProductBatch productBatch=null;
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            boolean allowNegativeInventory = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("allowNegativeInventory"))) {
                allowNegativeInventory = Boolean.parseBoolean(request.getParameter("allowNegativeInventory"));
            }

            String data = request.getParameter("jsondata");
            JSONArray srObjectArray = new JSONArray(data);
            for (int cnt = 0; cnt < srObjectArray.length(); cnt++) {
                JSONObject srObject = srObjectArray.optJSONObject(cnt);
                String reqId = srObject.getString("id");
                double issueQty = srObject.optDouble("issueQty", 0);
                String issuedStore = srObject.optString("tostoreid");
                Store toStore = null;
                if (!StringUtil.isNullOrEmpty(issuedStore)) {
                    toStore = storeService.getStoreById(issuedStore);
                }
                StockRequest stockRequest = stockRequestService.getStockRequestById(reqId);
                stockRequest.setIssuedQty(issueQty);
                if (toStore != null) {
                    stockRequest.setToStore(toStore);
                }
                Product product = stockRequest.getProduct();
                Set<StockRequestDetail> issuedStockDetail = new HashSet<StockRequestDetail>();
                JSONArray jArr = srObject.optJSONArray("stockDetails");
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject detailObj = jArr.optJSONObject(i);
                    String issuedLocationId = detailObj.optString("locationId");
                    String issuedRowId = detailObj.optString("rowId");
                    String issuedRackId = detailObj.optString("rackId");
                    String issuedBinId = detailObj.optString("binId");
                    String batchName = detailObj.optString("batchName");
                    String issuedSerialNames = detailObj.optString("serialNames");
                    String issuedSerialIds=detailObj.optString("serialsId");
                    double quantity = detailObj.optDouble("quantity");
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
                    productBatch = stockService.getERPProductBatch(product, toStore, issuedLocation, issuedRow, issuedRack, issuedBin, batchName);
                    if(productBatch == null || productBatch.getQuantitydue() <= quantity ){
                        issuccess=false;
                        throw new AccountingException(messageSource.getMessage("acc.stockrequest.Quantitycannotbemorethanavailablequantity", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                    }
                    if (product.isIsSerialForProduct() && !StringUtil.isNullOrEmpty(issuedSerialIds)) {
                        String[] serialIds = issuedSerialIds.split(",");
                        for (String serialId : serialIds) {
                            jeresult = accountingHandlerDAO.getObject(NewBatchSerial.class.getName(), serialId);
                            NewBatchSerial newBatchSerial = (NewBatchSerial) jeresult.getEntityList().get(0);
                            if (newBatchSerial.getQuantitydue() <= 0) {
                                issuccess=false;
                                throw new AccountingException(messageSource.getMessage("acc.batchserial.serialdetail", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                            }
                        }
                    }
                    StockRequestDetail srd = new StockRequestDetail();
                    srd.setStockRequest(stockRequest);
                    srd.setIssuedLocation(issuedLocation);
                    srd.setIssuedRow(issuedRow);
                    srd.setIssuedRack(issuedRack);
                    srd.setIssuedBin(issuedBin);
                    srd.setBatchName(batchName);
                    srd.setIssuedSerialNames(issuedSerialNames);
                    srd.setIssuedQuantity(quantity);
                    issuedStockDetail.add(srd);

//                if (!StringUtil.isNullOrEmpty(auditMessage)) {
//                    auditMessage += ", ";
//                }
//                auditMessage += "Location: " + issuedLocation.getName() + " ";
//                if (product.isIsBatchForProduct()) {
//                    auditMessage += ", Batch: " + batchName;
//                }
//                if (product.isIsSerialForProduct()) {
//                    auditMessage += ", Serial: " + issuedSerialNames;
//                }
//                auditMessage += ", Quantity: " + quantity + " " + product.getUnitOfMeasure();
                }
                stockRequest.setStockRequestDetails(issuedStockDetail);

                stockRequestService.issueStockOrderRequest(user, stockRequest, allowNegativeInventory);
                
                // code for send mail notification when item qty goes below than reorder level.
//            ExtraCompanyPreferences extraCompanyPreferences = null;
//            KwlReturnObject extraprefresult = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
//            extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                DocumentEmailSettings documentEmailSettings = null;
                KwlReturnObject documentEmailresult = accountingHandlerDAO.getObject(DocumentEmailSettings.class.getName(), sessionHandlerImpl.getCompanyid(request));
                documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;

                if (documentEmailSettings.isQtyBelowReorderLevelMail()) {
                    for (StockRequestDetail srdetail : stockRequest.getStockRequestDetails()) {
                        double availableQtyinStore = stockService.getProductQuantityInStore(stockRequest.getProduct(), stockRequest.getToStore());
                        if (availableQtyinStore < stockRequest.getProduct().getReorderLevel()) {
                            HashMap<String, String> datamap = new HashMap<String, String>();
                            datamap.put("productName", stockRequest.getProduct().getProductName());
                            datamap.put("storeId", stockRequest.getToStore().getId());
                            datamap.put("availableQty", Double.toString(availableQtyinStore));
                            accountingHandlerDAO.sendReorderLevelEmails(user.getUserID(), null, TransactionModule.STOCK_REQUEST.toString(), datamap);
                        }
                    }
                }
                auditMessage = "User " + user.getFullName() + " has issued stock for Stock Request: " + stockRequest.getTransactionNo() + ", Product :" + product.getProductid() + ", Quantity :" + issueQty + " " + stockRequest.getUom().getNameEmptyforNA() + " from Store: " + toStore.getAbbreviation() + " " + auditMessage;
                auditTrailObj.insertAuditLog(AuditAction.STOCK_REQUEST_ISSUED, auditMessage, request, stockRequest.getId());
            }
            txnManager.commit(status);
            msg = "Goods Order Request issued successfully.";
            issuccess = true;

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch(AccountingException ex){
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (NegativeInventoryException ex) {
            txnManager.rollback(status);
            if (ex.getType() == NegativeInventoryException.Type.BLOCK) {
                jobj.put("currentInventoryLevel", "block");
            } else if (ex.getType() == NegativeInventoryException.Type.WARN) {
                jobj.put("currentInventoryLevel", "warn");
            }
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView collectStockOrderRequest(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            StringBuilder productIds = new StringBuilder();
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String data = request.getParameter("jsondata");
            JSONArray dataArr = new JSONArray(data);
            for (int idx = 0; idx < dataArr.length(); idx++) {
                JSONObject srObject = dataArr.optJSONObject(idx);

                String reqId = srObject.getString("id");
                double collectQty = srObject.optDouble("delquantity", 0);
                String returnReason = srObject.getString("reason");

                StockRequest stockRequest = stockRequestService.getStockRequestById(reqId);
                stockRequest.setDeliveredQty(collectQty);
                stockRequest.setReturnReason(returnReason);

                Product product = stockRequest.getProduct();
                JSONArray jArr = srObject.optJSONArray("stockDetails");
                int arrLength = (jArr != null) ? jArr.length() : 0;
                for (int i = 0; i < arrLength; i++) {
                    JSONObject detailObj = jArr.optJSONObject(i);
                    String detailId = detailObj.optString("detailId");
                    String collectedLocationId = detailObj.optString("locationId");
                    String collectedRowId = detailObj.optString("rowId");
                    String collectedRackId = detailObj.optString("rackId");
                    String collectedBinId = detailObj.optString("binId");
                    String batchName = detailObj.optString("batchName");
                    String collectedSerialNames = detailObj.optString("serialNames");
                    double quantity = detailObj.optDouble("quantity");
                    Location collectedLocation = locationService.getLocation(collectedLocationId);
                    StockRequestDetail srd = stockRequestService.getStockRequestDetail(detailId);
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
                    if (srd != null) {
                        srd.setStockRequest(stockRequest);
                        srd.setDeliveredLocation(collectedLocation);
                        srd.setDeliveredRow(collectedRow);
                        srd.setDeliveredRack(collectedRack);
                        srd.setDeliveredBin(collectedBin);
                        srd.setBatchName(batchName);
                        srd.setDeliveredSerialNames(collectedSerialNames);
                        srd.setDeliveredQuantity(quantity);
                        stockRequest.getStockRequestDetails().add(srd);

//                    if (!StringUtil.isNullOrEmpty(auditMessage)) {
//                        auditMessage += ", ";
//                    }
//                    auditMessage += "Location: " + collectedLocation.getName() + " ";
//                    if (product.isIsBatchForProduct()) {
//                        auditMessage += ", Batch: " + batchName;
//                    }
//                    if (product.isIsSerialForProduct()) {
//                        auditMessage += ", Serial: " + collectedSerialNames;
//                    }
//                    auditMessage += ", Quantity: " + quantity + " " + product.getUnitOfMeasure();
                    }

                }
                if (productIds.indexOf(product.getID()) == -1) {
                    productIds.append(product.getID()).append(",");
                }
                stockRequestService.collectStockOrderRequest(user, stockRequest);
                auditMessage = "User " + user.getFullName() + " has collected stock for Stock Request: " + stockRequest.getTransactionNo() + ", Product :" + product.getProductid() + ", Quantity :" + collectQty + " " + stockRequest.getUom().getNameEmptyforNA() + " in Store: " + stockRequest.getFromStore().getAbbreviation() + " " + auditMessage;
                auditTrailObj.insertAuditLog(AuditAction.STOCK_REQUEST_COLLECTED, auditMessage, request, stockRequest.getId());
                auditMessage = "";
                if (stockRequest.getIssuedQty() > stockRequest.getDeliveredQty()) {
                    auditMessage = "User " + user.getFullName() + " has created Return Stock Request R" + stockRequest.getTransactionNo() + " for Stock Request: " + stockRequest.getTransactionNo() + ", Product :" + product.getProductid() + ", Quantity :" + (stockRequest.getIssuedQty() - stockRequest.getDeliveredQty()) + " " + stockRequest.getUom().getNameEmptyforNA() + ", Store: from(" + stockRequest.getToStore().getAbbreviation() + " to " + stockRequest.getFromStore().getAbbreviation() + ") " + auditMessage;
                    auditTrailObj.insertAuditLog(AuditAction.STOCK_REQUEST_ADDED, auditMessage, request, stockRequest.getId());
                }
            }
            jobj.put("productIds", productIds);
            msg = messageSource.getMessage("acc.stockrequest.GoodsOrderRequestacceptedsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            if(ex==null){
                msg="Error occurred while processing";
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
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView addInterStoreTransferRequest(HttpServletRequest request, HttpServletResponse response) {
         JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try{
            /*
            Convert request object parameters to JSON object
            */
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj=istService.saveInterStoreTransferRequest(paramJobj);
        }catch (Exception ex) {
            Logger.getLogger(GoodsTransferController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }
   

    public ModelAndView addInterLocationTransfer(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "", billid = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            Company company = user.getCompany();
            String records = request.getParameter("str");
            String transactionNo = request.getParameter("transactionno");
            String seqFormatId = request.getParameter("seqFormatId");
            String documentNumber = request.getParameter("documentNumber");
            String fromStoreId = request.getParameter("fromstore");
            boolean UomSchemaType = false;
            String memo = (request.getParameter("memo")) != null ? request.getParameter("memo") : "";
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.companyid, company.getCompanyID());
            requestParams.put(Constants.moduleid, Constants.Acc_InterLocation_ModuleId);
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.customfield))) {
                requestParams.put(Constants.customfield, request.getParameter(Constants.customfield));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("UomSchemaType"))) {
                UomSchemaType = Boolean.parseBoolean(request.getParameter("UomSchemaType"));
            }
            JSONArray jArr = new JSONArray(records);

            Store fromStore = storeService.getStoreById(fromStoreId);
            SeqFormat seqFormat = null;
            if (!("NA".equals(seqFormatId))) {
                if (!StringUtil.isNullOrEmpty(seqFormatId)) {
                    seqFormat = seqService.getSeqFormat(seqFormatId);
                } else {
                    seqFormat = seqService.getDefaultSeqFormat(user.getCompany(), ModuleConst.INTER_LOCATION_TRANSFER);
                }
            }
            boolean allowNegativeInventory = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("allowNegativeInventory"))) {
                allowNegativeInventory = Boolean.parseBoolean(request.getParameter("allowNegativeInventory"));
            }


            synchronized (this) {
                boolean seqExist = false;
                if ("NA".equals(seqFormatId)) {
                    transactionNo = documentNumber;
                    seqExist = seqService.isExistingSeqNumber(transactionNo, company, ModuleConst.INTER_LOCATION_TRANSFER);
                    if (seqExist) {
                        throw new InventoryException("Sequence number already exist, please enter other one.");
                    }
                } else {
                    seqExist = false;
                    do {
                        transactionNo = seqService.getNextFormatedSeqNumber(seqFormat);
                        seqExist = seqService.isExistingSeqNumber(transactionNo, company, ModuleConst.INTER_LOCATION_TRANSFER);
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

                        UnitOfMeasure uom = null;
                        String packagingId = jObj.optString("packaging");
                        String uomId = jObj.optString("uom");
                        String uomName = jObj.optString("uomname");
                        jeresult = accountingHandlerDAO.getObject(Product.class.getName(), itemId);
                        Product product = (Product) jeresult.getEntityList().get(0);
                        jeresult = accountingHandlerDAO.getObject(UnitOfMeasure.class.getName(), uomName);
                        uom = (UnitOfMeasure) jeresult.getEntityList().get(0);
                        if (uom == null) {
                            jeresult = accountingHandlerDAO.getObject(UnitOfMeasure.class.getName(), product.getTransferUOM().getID());
                            uom = (UnitOfMeasure) jeresult.getEntityList().get(0);
                        }
                        Packaging packaging = null;
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
                        double qty = authHandler.roundQuantity((jObj.optDouble("quantity", 0)), company.getCompanyID());
                        String remark = jObj.getString("remark");
                        InterStoreTransferRequest interStoreTransfer = new InterStoreTransferRequest(product, fromStore, fromStore, uom);
                        interStoreTransfer.setOrderedQty(qty);
                        interStoreTransfer.setAcceptedQty(qty);
                        interStoreTransfer.setRemark(remark);
                        interStoreTransfer.setTransactionNo(transactionNo);
                        interStoreTransfer.setBusinessDate(businessDate);
                        interStoreTransfer.setCostCenter(costCenter);
                        interStoreTransfer.setPackaging(packaging);
                        interStoreTransfer.setUom(uom);
                        interStoreTransfer.setMemo(memo);
                        Set<ISTDetail> issuedStockDetails = new HashSet<ISTDetail>();
                        JSONArray stockDetails = jObj.optJSONArray("stockDetails");
                        for (int x = 0; x < stockDetails.length(); x++) {
                            JSONObject detailObj = stockDetails.optJSONObject(x);
                            String issuedLocationId = detailObj.optString("fromLocationId");
                            String collectLocationId = detailObj.optString("toLocationId");
                            String issuedRowId = detailObj.optString("fromRowId");
                            String collectedRowId = detailObj.optString("toRowId");
                            String issuedRackId = detailObj.optString("fromRackId");
                            String collectedRackId = detailObj.optString("toRackId");
                            String issuedBinId = detailObj.optString("fromBinId");
                            String collectedBinId = detailObj.optString("toBinId");
                            String batchName = detailObj.optString("batchName");
                            String serialNames = detailObj.optString("serials");
                            double quantity = authHandler.roundQuantity((detailObj.optDouble("quantity")), company.getCompanyID());
                            Location issuedLocation = locationService.getLocation(issuedLocationId);
                            Location collectLocation = locationService.getLocation(collectLocationId);
                            StoreMaster issuedRow = null;
                            StoreMaster collectedRow = null;
                            if (product.isIsrowforproduct()) {
                                issuedRow = storeService.getStoreMaster(issuedRowId);
                                collectedRow = storeService.getStoreMaster(collectedRowId);
                            }
                            StoreMaster issuedRack = null;
                            StoreMaster collectedRack = null;
                            if (product.isIsrackforproduct()) {
                                issuedRack = storeService.getStoreMaster(issuedRackId);
                                collectedRack = storeService.getStoreMaster(collectedRackId);
                            }
                            StoreMaster issuedBin = null;
                            StoreMaster collectedBin = null;
                            if (product.isIsbinforproduct()) {
                                issuedBin = storeService.getStoreMaster(issuedBinId);
                                collectedBin = storeService.getStoreMaster(collectedBinId);
                            }

                            ISTDetail srd = new ISTDetail();
                            srd.setIstRequest(interStoreTransfer);
                            srd.setIssuedLocation(issuedLocation);
                            srd.setDeliveredLocation(collectLocation);
                            srd.setIssuedRow(issuedRow);
                            srd.setDeliveredRow(collectedRow);
                            srd.setIssuedRack(issuedRack);
                            srd.setDeliveredRack(collectedRack);
                            srd.setIssuedBin(issuedBin);
                            srd.setDeliveredBin(collectedBin);
                            srd.setBatchName(batchName);
                            srd.setIssuedSerialNames(serialNames);
                            srd.setDeliveredSerialNames(serialNames);
                            srd.setIssuedQuantity(quantity);
                            srd.setDeliveredQuantity(quantity);
                            issuedStockDetails.add(srd);
                        }
                        interStoreTransfer.setIstDetails(issuedStockDetails);
                        
                        requestParams.put(Constants.LineLevelCustomData, jObj.optString(Constants.LineLevelCustomData));//Line Level Custom data
                        istService.addInterLocationTransfer(user, interStoreTransfer,requestParams);
                        billid = interStoreTransfer.getId();
                        if (!StringUtil.isNullOrEmpty(auditMessage)) {
                            auditMessage += ", ";
                        }
                        auditMessage += "(Product :" + product.getProductid() + ", Quantity :" + qty + " " + interStoreTransfer.getUom().getNameEmptyforNA() + ")";
                    }
                }
                if (!("NA".equals(seqFormatId))) {
                    seqService.updateSeqNumber(seqFormat);
                }
            }
            jobj.put("Interlocationno", transactionNo);
            jobj.put("billid", billid);
            issuccess = true;
            msg = "Inter Location Stock Transfer : <b>" + transactionNo + "</b> has been done successfully";

            auditMessage = "User " + user.getFullName() + " has created Inter Location Transfer: " + transactionNo + " for Store: " + fromStore.getAbbreviation() + ", " + auditMessage;
            auditTrailObj.insertAuditLog(AuditAction.INTER_STORE_REQUEST_ADDED, auditMessage, request, "0");

            txnManager.commit(status);

            TransactionStatus statusforBlockSOQty = txnManager.getTransaction(def);
            try {
                KwlReturnObject retObj = consignmentService.assignStockToPendingConsignmentRequests(request);
                txnManager.commit(statusforBlockSOQty);
            } catch (Exception ex) {
                txnManager.rollback(statusforBlockSOQty);
                Logger.getLogger(StockAdjustmentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }
    
    public ModelAndView acceptInterStoreTransferRequest(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            /*
             Convert request object parameters to JSON object
             */
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = istService.acceptInterStoreTransferRequest(paramJobj);
        } catch (Exception ex) {
            Logger.getLogger(GoodsTransferController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView acceptISTReturnRequest(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String reqId = request.getParameter("requestId");
            String serials = request.getParameter("serialNames");
            String qaApproval = request.getParameter("qaApprove");

            InterStoreTransferRequest ist = istService.getInterStoreTransferById(reqId);
            Product product = ist.getProduct();
            //inserting in json for call to AopAdvisor for performing valuation of only this product
            jobj.put("productIds", product.getID()); 

            boolean sendForQAApproval = false;
            if ("true".equals(qaApproval)) {
                sendForQAApproval = true;
            }
            if (sendForQAApproval) {
                String[] serialNames = null;
                if (ist != null && ist.getProduct().isIsSerialForProduct()) {
                    if (!StringUtil.isNullOrEmpty(serials)) {
                        serialNames = serials.split(",");
                    }
                }
                istService.sendISTReturnRequestForQA(user, ist, serialNames);

                if (serialNames != null) {
                    auditMessage += " and Serial: (" + serials + ") send for QA Approval";
                } else {
                    auditMessage += " and send for QA Approval";
                }

            } else {
                istService.acceptISTReturnRequest(user, ist,serials);
            }
            msg = "Request has been accepted successfully";
            issuccess = true;

            auditMessage = "User " + user.getFullName() + " has accepted returned stock for IST Request: " + ist.getTransactionNo() + ", Product :" + product.getProductid() + ", Quantity :" + ist.getAcceptedQty() + " " + ist.getUom().getNameEmptyforNA() + "(" + ist.getPackaging().getQuantityInStockUoM(ist.getUom(), ist.getAcceptedQty()) + " " + ist.getPackaging().getStockUoM().getNameEmptyforNA() + ")" + " in Store: " + ist.getToStore().getAbbreviation() + ", " + auditMessage;
            auditTrailObj.insertAuditLog(AuditAction.INTER_STORE_REQUEST_ACCEPTED, auditMessage, request, ist.getId());

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView rejectInterStoreTransferRequest(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        String returnReqAuditMessage = "";
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            String remark = request.getParameter("remark");
            String requestIds = request.getParameter("jsondata");
            if (!StringUtil.isNullOrEmpty(requestIds)) {
                String[] reqIdArr = requestIds.split(",");
                for (String reqId : reqIdArr) {
                    InterStoreTransferRequest ist = istService.getInterStoreTransferById(reqId);
                    ist.setRemark(remark);
                    istService.rejectInterStoreTransferRequest(user, ist);
                    msg = "Request has been rejected successfully";

                    if (!StringUtil.isNullOrEmpty(auditMessage)) {
                        auditMessage += ", ";
                        returnReqAuditMessage += ",";
                    }
                    auditMessage += "(Request No: " + ist.getTransactionNo() + " Product :" + ist.getProduct().getProductid() + " Quantity :" + (ist.getOrderedQty() - ist.getAcceptedQty()) + " " + ist.getUom().getNameEmptyforNA() + ")";
                    returnReqAuditMessage += "(Return Request No: R" + ist.getTransactionNo() + " Product :" + ist.getProduct().getProductid() + " Quantity :" + (ist.getOrderedQty() - ist.getAcceptedQty()) + " " + ist.getUom().getNameEmptyforNA() + " Store: from(" + ist.getToStore().getAbbreviation() + " to " + ist.getFromStore().getAbbreviation() + "))";
                }
            }
            issuccess = true;

            auditMessage = "User " + user.getFullName() + " has rejected IST Requests -  " + auditMessage;
            auditTrailObj.insertAuditLog(AuditAction.INTER_STORE_REQUEST_REJECTED, auditMessage, request, "0");

            returnReqAuditMessage = "User " + user.getFullName() + " has created return IST Requests - " + returnReqAuditMessage;
            auditTrailObj.insertAuditLog(AuditAction.INTER_STORE_REQUEST_ADDED, returnReqAuditMessage, request, "0");

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView addIssueNoteRequest(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SOR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "", billid = "";
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String fromStoreId = request.getParameter("fromstore");
            Store fromStore = storeService.getStoreById(fromStoreId);

            String toStoreId = request.getParameter("tostore");
            Store toStore = storeService.getStoreById(toStoreId);
            boolean UomSchemaType = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("UomSchemaType"))) {
                UomSchemaType = Boolean.parseBoolean(request.getParameter("UomSchemaType"));
            }

            String transactionNo = "";
            String seqFormatId = request.getParameter("seqFormatId");
            String documentNumber = request.getParameter("documentNumber");
            String isqarejected = request.getParameter("isqarejected");
            boolean isQARejected = false;
            if ("true".equalsIgnoreCase(isqarejected)) {
                isQARejected = true;
            }
            Date businessDate = df.parse(request.getParameter("businessdate"));
            String records = request.getParameter("jsondata");
            JSONArray jArr = new JSONArray(records);
            
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.companyid, companyId);
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.customfield))) {
                requestParams.put(Constants.customfield, request.getParameter(Constants.customfield));
            }

            SeqFormat seqFormat = null;
            if (!("NA".equals(seqFormatId))) {
                if (!StringUtil.isNullOrEmpty(seqFormatId)) {
                    seqFormat = seqService.getSeqFormat(seqFormatId);
                } else {
                    seqFormat = seqService.getDefaultSeqFormat(company, ModuleConst.ISSUE_NOTE);
                }
            }
            boolean allowNegativeInventory = false;

            if (!StringUtil.isNullOrEmpty(request.getParameter("allowNegativeInventory"))) {
                allowNegativeInventory = Boolean.parseBoolean(request.getParameter("allowNegativeInventory"));
            }

            synchronized (this) {
                boolean seqExist = false;
                if ("NA".equals(seqFormatId)) {
                    transactionNo = documentNumber;
                    seqExist = seqService.isExistingSeqNumber(transactionNo, company, ModuleConst.ISSUE_NOTE);
                    if (seqExist) {
                        throw new InventoryException("Sequence number already exist, please enter other one.");
                    }
                } else {
                    seqExist = false;
                    do {
                        transactionNo = seqService.getNextFormatedSeqNumber(seqFormat);
                        seqExist = seqService.isExistingSeqNumber(transactionNo, company, ModuleConst.ISSUE_NOTE);
                        if (seqExist) {
                            seqService.updateSeqNumber(seqFormat);
                        }
                    } while (seqExist);
                }

                transactionNo = seqFormat != null ? seqService.getNextFormatedSeqNumber(seqFormat) : documentNumber;
                if (!seqExist) {

                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject jObj = jArr.optJSONObject(i);
                        String itemId = jObj.optString("productid");
                        String remark = jObj.optString("remark");
                        double transferQuantity = authHandler.roundQuantity((jObj.optDouble("transferQuantity", 0)), companyId);
                        String costCenterId = jObj.optString("costcenter");
                        String uomId = jObj.optString("uom");
                        String packagingId = jObj.optString("packaging");
                        double confactor = jObj.has("confactor") ? jObj.optDouble("confactor", 1) : 1;
                        CostCenter costCenter = null;

                        if (!StringUtil.isNullOrEmpty(costCenterId)) {
                            jeresult = accountingHandlerDAO.getObject(CostCenter.class.getName(), costCenterId);
                            costCenter = (CostCenter) jeresult.getEntityList().get(0);
                        }

                        jeresult = accountingHandlerDAO.getObject(Product.class.getName(), itemId);
                        Product product = (Product) jeresult.getEntityList().get(0);
                        UnitOfMeasure unitOfMeasure = null;
                        jeresult = accountingHandlerDAO.getObject(UnitOfMeasure.class.getName(), uomId);
                        unitOfMeasure = (UnitOfMeasure) jeresult.getEntityList().get(0);
                        if (unitOfMeasure == null) {
                            jeresult = accountingHandlerDAO.getObject(UnitOfMeasure.class.getName(), product.getOrderingUOM().getID());
                            unitOfMeasure = (UnitOfMeasure) jeresult.getEntityList().get(0);
                        }

                        Packaging packaging = new Packaging();
                        if (unitOfMeasure==product.getUnitOfMeasure()){
                            packaging = packagingService.getPackaging(packagingId);
                        } else {
                        if (UomSchemaType) {
                            packaging.setCompany(company);
                            packaging.setInnerUoM(unitOfMeasure);
                            packaging.setInnerUomValue(1);
                            packaging.setStockUoM(product.getUnitOfMeasure());
                            packaging.setStockUomValue(confactor);
                            packagingService.addOrUpdatePackaging(packaging);
                        } else {
                            packaging = packagingService.getPackaging(packagingId);
                        }
                        }

//                    StockRequest stockRequest = new StockRequest(product, fromStore, toStore, product.getOrderingUOM(), transferQuantity);
                        StockRequest stockRequest = new StockRequest(product, fromStore, toStore, unitOfMeasure, transferQuantity);
                        stockRequest.setIssuedQty(transferQuantity);
                        stockRequest.setDeliveredQty(transferQuantity);
                        stockRequest.setRemark(remark);
                        stockRequest.setPackaging(packaging);
                        stockRequest.setTransactionNo(transactionNo);
                        stockRequest.setUom(unitOfMeasure);
                        stockRequest.setModule(TransactionModule.ISSUE_NOTE);
                        if (costCenter != null) {
                            stockRequest.setCostCenter(costCenter);
                        }
                        stockRequest.setBusinessDate(businessDate);
                        stockRequest.setCollectedOn(businessDate);

                        Set<StockRequestDetail> issuedStockDetail = new HashSet<StockRequestDetail>();
                        JSONArray stockDetailsArr = jObj.optJSONArray("stockDetails") != null ? jObj.optJSONArray("stockDetails") : new JSONArray();
                        for (int x = 0; x < stockDetailsArr.length(); x++) {
                            JSONObject detailObj = stockDetailsArr.optJSONObject(x);

                            String issuedLocationId = detailObj.optString("fromLocationId");
                            String collectLocationId = detailObj.optString("toLocationId");
                            String issuedRowId = detailObj.optString("fromRowId");
                            String collectedRowId = detailObj.optString("toRowId");
                            String issuedRackId = detailObj.optString("fromRackId");
                            String collectedRackId = detailObj.optString("toRackId");
                            String issuedBinId = detailObj.optString("fromBinId");
                            String collectedBinId = detailObj.optString("toBinId");
                            String batchName = detailObj.optString("batchName");
                            String serialNames = detailObj.optString("serialNames");
                            double quantity = authHandler.roundQuantity((detailObj.optDouble("quantity")), companyId);

                            Location issuedLocation = locationService.getLocation(issuedLocationId);
                            Location collectedLocation = null;
                            if (isQARejected) {
                                collectedLocation = toStore.getDefaultLocation();
                                if (collectedLocation == null) {
                                    issuccess = false;
                                    msg = " Default location is not set for  this store: " + toStore.getFullName();
                                    throw new Exception("Please set default location for store " + toStore.getFullName());

                                }
                            } else {
                                collectedLocation = locationService.getLocation(collectLocationId);
                            }

                            StoreMaster issuedRow = null;
                            StoreMaster collectedRow = null;
                            if (product.isIsrowforproduct()) {
                                issuedRow = storeService.getStoreMaster(issuedRowId);
                                collectedRow = storeService.getStoreMaster(collectedRowId);
                            }
                            StoreMaster issuedRack = null;
                            StoreMaster collectedRack = null;
                            if (product.isIsrackforproduct()) {
                                issuedRack = storeService.getStoreMaster(issuedRackId);
                                collectedRack = storeService.getStoreMaster(collectedRackId);
                            }
                            StoreMaster issuedBin = null;
                            StoreMaster collectedBin = null;
                            if (product.isIsbinforproduct()) {
                                issuedBin = storeService.getStoreMaster(issuedBinId);
                                collectedBin = storeService.getStoreMaster(collectedBinId);
                            }

                            StockRequestDetail srd = new StockRequestDetail();
                            srd.setStockRequest(stockRequest);
                            srd.setIssuedLocation(issuedLocation);
                            srd.setDeliveredLocation(collectedLocation);
                            srd.setIssuedRow(issuedRow);
                            srd.setDeliveredRow(collectedRow);
                            srd.setIssuedRack(issuedRack);
                            srd.setDeliveredRack(collectedRack);
                            srd.setIssuedBin(issuedBin);
                            srd.setDeliveredBin(collectedBin);
                            srd.setBatchName(batchName);
                            srd.setIssuedSerialNames(serialNames);
                            srd.setDeliveredSerialNames(serialNames);
                            srd.setIssuedQuantity(quantity);
                            srd.setDeliveredQuantity(quantity);
                            issuedStockDetail.add(srd);

                            istService.checkItemAlreadyProcessed(isQARejected, detailObj.optString("moduletype"), detailObj.optString("qaapprovaldetailid"));
                        }
                        stockRequest.setStockRequestDetails(issuedStockDetail);
                        /*
                         *line level custom data
                         */
                        if (jObj.has(Constants.LineLevelCustomData) && !StringUtil.isNullOrEmpty(jObj.optString(Constants.LineLevelCustomData))) {
                            requestParams.put(Constants.LineLevelCustomData, jObj.optString(Constants.LineLevelCustomData));
                        }

                        stockRequestService.stockIssueByIssueNote(user, stockRequest, allowNegativeInventory,requestParams);
                        billid = stockRequest.getId();
                        issuccess = true;

                        // code for send mail notification when item qty goes below than reorder level.
//                    ExtraCompanyPreferences extraCompanyPreferences = null;
//                    KwlReturnObject extraprefresult = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
//                    extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                        DocumentEmailSettings documentEmailSettings = null;
                        KwlReturnObject documentEmailresult = accountingHandlerDAO.getObject(DocumentEmailSettings.class.getName(), sessionHandlerImpl.getCompanyid(request));
                        documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
                        if (documentEmailSettings.isQtyBelowReorderLevelMail()) {
                            for (StockRequestDetail srdetail : stockRequest.getStockRequestDetails()) {
                                double availableQtyinStore = stockService.getProductQuantityInStore(stockRequest.getProduct(), stockRequest.getFromStore());
                                if (availableQtyinStore < stockRequest.getProduct().getReorderLevel()) {
                                    HashMap<String, String> data = new HashMap<String, String>();
                                    data.put("productName", stockRequest.getProduct().getProductName());
                                    data.put("storeId", stockRequest.getFromStore().getId());
                                    data.put("availableQty", Double.toString(availableQtyinStore));
                                    accountingHandlerDAO.sendReorderLevelEmails(user.getUserID(), null, TransactionModule.ISSUE_NOTE.toString(), data);
                                }
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(auditMessage)) {
                            auditMessage += ", ";
                        }
                        auditMessage += "(Product :" + product.getProductid() + ", Quantity :" + transferQuantity + " " + stockRequest.getUom().getNameEmptyforNA() + ")";

                    }
                }
                if (!("NA".equals(seqFormatId))) {
                    seqService.updateSeqNumber(seqFormat);
                }
            }

            if (!issuccess) {
                txnManager.rollback(status);
            } else {
                msg = "Issue Note : " + transactionNo + " has been added successfully";
                txnManager.commit(status);
                jobj.put("IssueNoteNo", transactionNo);
                jobj.put("billid", billid);
            }
            TransactionStatus statusforBlockSOQty = txnManager.getTransaction(def);
            try {
                KwlReturnObject retObj = consignmentService.assignStockToPendingConsignmentRequests(request);
                txnManager.commit(statusforBlockSOQty);
            } catch (Exception ex) {
                txnManager.rollback(statusforBlockSOQty);
                Logger.getLogger(StockAdjustmentController.class.getName()).log(Level.SEVERE, null, ex);
            }
            auditMessage = "User " + user.getFullName() + " has transfered " + (isQARejected ? "rejected" : "") + " stock with  Issue Note: " + transactionNo + ", Store: from(" + fromStore.getAbbreviation() + " to " + toStore.getAbbreviation() + "), " + auditMessage;
            auditTrailObj.insertAuditLog(AuditAction.ISSUE_NOTE_ADDED, auditMessage, request, "0");

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (NegativeInventoryException ex) {
            txnManager.rollback(status);
            if (ex.getType() == NegativeInventoryException.Type.BLOCK) {
                jobj.put("currentInventoryLevel", "block");
            } else if (ex.getType() == NegativeInventoryException.Type.WARN) {
                jobj.put("currentInventoryLevel", "warn");
            }
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getStockMovementList(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SMT_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        String companyid = "";
        try {
            companyid = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);

            String searchString = request.getParameter("ss");

            String fd = request.getParameter("fromDate");
            String td = request.getParameter("toDate");

            String storeId = request.getParameter("storeid");
            String tType = request.getParameter("transactionType");
            String fieldToSearch = request.getParameter("fieldToSearch");
            String smFieldSearch = "";

            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
                paging = null;
            }
            Date fromDate = null;
            Date toDate = null;
            if (!StringUtil.isNullOrEmpty(fd) && !StringUtil.isNullOrEmpty(td)) {
                try {
                    fromDate = df.parse(fd);
                    toDate = df.parse(td);
                } catch (ParseException ex) {
                }
                //try {
//                    long lfd = Long.parseLong(fd);
//                    long ltd = Long.parseLong(td);
//                    fromDate = new Date();
//                    fromDate.setTime(lfd);
//
//                    toDate = new Date();
//                    toDate.setTime(ltd);
//
//                } catch (NumberFormatException ex) {
//                }
            }

            Store store = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                jeresult = accountingHandlerDAO.getObject(Store.class.getName(), storeId);
                store = (Store) jeresult.getEntityList().get(0);
            }
            TransactionType transactionType = null;
            if (!StringUtil.isNullOrEmpty(tType) && !tType.equals("ALL")) {
                transactionType = TransactionType.valueOf(tType);
            }
            if (!StringUtil.isNullOrEmpty(fieldToSearch) && !fieldToSearch.equals("ALL")) {
                smFieldSearch = searchString;
            }
            List<Product> productsForStockMovement = stockMovementService.getAllProductsForStockMovement(user.getCompany(), store, fromDate, toDate, transactionType, searchString, paging);
            Map<Product, List<StockMovement>> smList = stockMovementService.getStockMovementByProductList(user.getCompany(), productsForStockMovement, store, fromDate, toDate, transactionType,smFieldSearch);
            Date backDate = new Date(fromDate.getTime() - 24 * 60 * 60 * 1000);
            Set<Store> storeSet = new HashSet<Store>();
            storeSet.add(store);
            Map<Product, List<Object[]>> backForwardQuantityMap = stockService.getDateWiseStockDetailList(user.getCompany(), storeSet, null, backDate, searchString, null);
            Set<Product> doneforProduct = new HashSet();
            for (Product product : productsForStockMovement) {
                JSONObject jObj = null;
                JSONArray smDetails = null;

//                Product product = product.getProduct();
                if (!doneforProduct.contains(product)) {
                    jObj = new JSONObject();
                    smDetails = new JSONArray();
                    double orderedQuantity = 0;
                    double orderedAmount = 0;
                    if (backForwardQuantityMap != null && backForwardQuantityMap.containsKey(product)) {
                        for (Object[] stock : backForwardQuantityMap.get(product)) {
                            if (stock != null) {
                                JSONObject srObject = new JSONObject();
//                                srObject.put("id", smd.getId());
                                srObject.put("locationName", stock[2]);//(stock.getLocation() != null) ? stock.getLocation().getName() : "");
                                srObject.put("rowName", stock[3]);//(stock.getRow() != null) ? stock.getRow().getName() : "");
                                srObject.put("rackName", stock[4]);//(stock.getRack() != null) ? stock.getRack().getName() : "");
                                srObject.put("binName", stock[5]);//(stock.getBin() != null) ? stock.getBin().getName() : "");
                                srObject.put("quantity", stock[8]);//stock.getQuantity());
                                srObject.put("serialNames",stock[7]!= null? stock[7].toString().replace(",", ", "):"-");//(stock.getSerialNames() != null && !stock.getSerialNames().equals("")) ? stock.getSerialNames().replace(",", ", ") : "-");
                                srObject.put("batchName", stock[6]);//(stock.getBatchName() != null && !stock.getBatchName().equals("")) ? stock.getBatchName() : "-");
                                smDetails.put(srObject);

                                orderedQuantity += (Double) stock[8];//stock.getQuantity();
                                orderedAmount += (Double) stock[8] * 1;
                            }
                        }
                    }
                    jObj.put("stockDetails", smDetails);

                    jObj.put("itemcode", product.getProductid());
                    jObj.put("itemdescription", product.getDescription());
                    jObj.put("itemname", product.getName());
                    jObj.put("store", (store == null) ? "" : store.getDescription());
                    jObj.put("assemble", "-");
                    jObj.put("orderuom", (product.getUnitOfMeasure() == null) ? "" : product.getUnitOfMeasure().getNameEmptyforNA());
                    jObj.put("costcenter", "-");
                    jObj.put("vendor", "-");
                    jObj.put("type", "BACK FORWARD");
                    jObj.put("date", authHandler.getUserDateFormatterWithoutTimeZone(request).format(backDate));
                    jObj.put("orderquantity", orderedQuantity);
                    jObj.put("amount", authHandler.round(orderedAmount, companyid));
                    double price = orderedQuantity != 0 ? (orderedAmount / orderedQuantity) : 0;
                    jObj.put("avgCost", authHandler.round(price, companyid));
                    jObj.put("orderno", "-");
                    jObj.put("remark", "Back forward transaction balance");
                    jObj.put("isBatchForProduct", product != null ? product.isIsBatchForProduct() : "");
                    jObj.put("isSerialForProduct", product != null ? product.isIsSerialForProduct() : "");
                    jObj.put("isRowForProduct", product != null ? product.isIsrowforproduct() : "");
                    jObj.put("isRackForProduct", product != null ? product.isIsrackforproduct() : "");
                    jObj.put("isBinForProduct", product != null ? product.isIsbinforproduct() : "");
                    jObj.put("moduleName", "-");

                    jArray.put(jObj);

                    doneforProduct.add(product);
                }
                for (StockMovement sm : smList.get(product)) {
                    jObj = new JSONObject();
                    smDetails = new JSONArray();
                    jObj.put("itemcode", product.getProductid());
                    jObj.put("itemdescription", product.getDescription());
                    jObj.put("itemname", product.getName());
                    jObj.put("store", (sm.getStore() == null) ? "" : sm.getStore().getDescription());
                    jObj.put("assemble", (sm.getAssembledProduct() == null) ? "" : sm.getAssembledProduct().getName());
//                jObj.put("location", (sm.getLocation() == null) ? "" : sm.getLocation().getName());
                    jObj.put("orderuom", (sm.getStockUoM() == null) ? "" : sm.getStockUoM().getNameEmptyforNA());
                    jObj.put("costcenter", (sm.getCostCenter() == null) ? "" : sm.getCostCenter().getCcid());
                    String vcName = (sm.getVendor() == null) ? ((sm.getCustomer() == null) ? null : sm.getCustomer().getName()) : sm.getVendor().getName();
                    jObj.put("vendor", vcName);
                    jObj.put("type", sm.getTransactionType().toString());
                    jObj.put("date", authHandler.getUserDateFormatterWithoutTimeZone(request).format(sm.getTransactionDate()));
                    double quantity = sm.getQuantity();
                    double price = sm.getPricePerUnit();
                    if (sm.getTransactionType() == TransactionType.OUT) {
                        quantity = -quantity;
                    }
                    double amount = quantity * price;
                    jObj.put("orderquantity", quantity);
                    jObj.put("amount", authHandler.round(amount, companyid));
                    jObj.put("avgCost", authHandler.round(price, companyid));
                    jObj.put("orderno", sm.getTransactionNo());
                    jObj.put("remark", sm.getRemark());
                    jObj.put("isBatchForProduct", product != null ? product.isIsBatchForProduct() : "");
                    jObj.put("isSerialForProduct", product != null ? product.isIsSerialForProduct() : "");
                    jObj.put("isRowForProduct", product != null ? product.isIsrowforproduct() : "");
                    jObj.put("isRackForProduct", product != null ? product.isIsrackforproduct() : "");
                    jObj.put("isBinForProduct", product != null ? product.isIsbinforproduct() : "");
                    jObj.put("module", sm.getTransactionModule() != null ? sm.getTransactionModule().ordinal() : "");
                    String moduleName = sm.getTransactionModule() != null ? sm.getTransactionModule().getString() : "";
                    jObj.put("moduleName", moduleName);

                    if (product != null) {
                        for (StockMovementDetail smd : sm.getStockMovementDetails()) {
                            if (smd != null) {
                                JSONObject srObject = new JSONObject();
                                srObject.put("id", smd.getId());
                                srObject.put("locationName", (smd.getLocation() != null) ? smd.getLocation().getName() : "");
                                jObj.put("locationName", (smd.getLocation() != null) ? smd.getLocation().getName() : "");
                                srObject.put("rowName", (smd.getRow() != null) ? smd.getRow().getName() : "");
                                srObject.put("rackName", (smd.getRack() != null) ? smd.getRack().getName() : "");
                                srObject.put("binName", (smd.getBin() != null) ? smd.getBin().getName() : "");
                                srObject.put("quantity", smd.getQuantity());
                                srObject.put("serialNames", (smd.getSerialNames() != null && !smd.getSerialNames().equals("")) ? smd.getSerialNames().replace(",", ", ") : "-");
                                srObject.put("batchName", (smd.getBatchName() != null && !smd.getBatchName().equals("")) ? smd.getBatchName() : "-");
                                smDetails.put(srObject);
                            }

                        }
                    }
                    jObj.put("stockDetails", smDetails);

                    jArray.put(jObj);
                }
            }
            if (isExport) {
                jobj.put("data", jArray);
                exportDAO.processRequest(request, response, jobj);
            }
            issuccess = true;
            msg = "Inter Store Stock Transfer have been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView getDetailedStockMovementList(HttpServletRequest request, HttpServletResponse response) {
//        Calendar c1 = Calendar.getInstance();
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        JSONArray productCustomFieldInfo = new JSONArray();
        try {
//            System.out.println("Start : "+ startTime);
            String userId = sessionHandlerImpl.getUserid(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            jeresult = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), user.getCompany().getCompanyID());
            ExtraCompanyPreferences ecf = (ExtraCompanyPreferences) jeresult.getEntityList().get(0);
            DateFormat userDateFormat=new SimpleDateFormat(sessionHandlerImpl.getUserDateFormat(request));
            boolean isSku = ecf.isSKUFieldParm();
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);

            String searchString = request.getParameter("ss");

            String fd = request.getParameter("fromDate");
            String td = request.getParameter("toDate");

            String storeId = request.getParameter("storeid");
            String locId = request.getParameter("locationName");
            String tType = request.getParameter("transactionType");
            String productId = request.getParameter("productId") != null ? request.getParameter("productId") : "";

            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
                paging = null;
            }
            Date fromDate = null;
            Date toDate = null;
            if (!StringUtil.isNullOrEmpty(fd) && !StringUtil.isNullOrEmpty(td)) {
                try {
                    fromDate = df.parse(fd);
                    toDate = df.parse(td);
                } catch (ParseException ex) {
                }
            }

            Store store = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                jeresult = accountingHandlerDAO.getObject(Store.class.getName(), storeId);
                store = (Store) jeresult.getEntityList().get(0);
            }
            Location location = null;
            if (!StringUtil.isNullOrEmpty(locId)) {
                jeresult = accountingHandlerDAO.getObject(Location.class.getName(), locId);
                location = (Location) jeresult.getEntityList().get(0);

            }
            
            TransactionType transactionType = null;
            if (!StringUtil.isNullOrEmpty(tType)) {
                transactionType = TransactionType.valueOf(tType);
            }

            // Find out any product custom fields need to show in this report
            String customFieldQuery = "select customcolumninfo from showcustomcolumninreport where moduleid = ? and companyid = ?";
            List<String> customFieldinfoList = null;
            customFieldinfoList = accCommonTablesDAO.executeSQLQuery(customFieldQuery, new Object[]{Constants.Acc_Product_Master_ModuleId, companyid});

            HashMap<String, HashMap> productCustomData = new HashMap<String, HashMap>();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;

            if (customFieldinfoList.size() > 0) {
                String jsonString = customFieldinfoList.get(0);
                JSONArray productCustomFields = new JSONArray(jsonString);
                String fieldIds = "";
                for (int jCnt = 0; jCnt < productCustomFields.length(); jCnt++) {
                    fieldIds = fieldIds.concat("'").concat(productCustomFields.getJSONObject(jCnt).getString("fieldid")).concat("',");
                }
                if (!StringUtil.isNullOrEmpty(fieldIds)) {
                    fieldIds = fieldIds.substring(0, fieldIds.length() - 1);
                }
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, "INid"));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, fieldIds));
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }

            if (replaceFieldMap.size() > 0) {
                for (Map.Entry<String, String> varEntry : replaceFieldMap.entrySet()) {
                    JSONObject fieldInfo = new JSONObject();
                    fieldInfo.put("dataindex", varEntry.getKey());
                    fieldInfo.put("columnname", varEntry.getKey().replaceAll("Custom_", ""));
                    productCustomFieldInfo.put(fieldInfo);
                }
            }
            HashMap<String, Object> addressParams = new HashMap<>();
            HashMap<String, String> vendorCountryMap = new HashMap<>();
            HashMap<String, String> customerCountryMap = new HashMap<>();
            HashMap<String, String> batchserialMap = new HashMap<>();
            List<StockMovementDetail> smdList = stockMovementService.getDetailedStockMovementList(user.getCompany(), store,location,fromDate, toDate, transactionType, searchString, paging,productId);
            Map<String,String> prodBatchExpDateMap = new HashMap<>();
            for (StockMovementDetail smd : smdList) {
                StockMovement sm = smd.getStockMovement();
                JSONObject jObj = new JSONObject();
                TransactionModule transactionModule = sm.getTransactionModule();
                Product product=sm.getProduct();
                String moduleName = sm.getTransactionModule().getString();
                jObj.put("itemcode", product.getProductid());
                jObj.put("itemdescription", product.getDescription());
                jObj.put("itemname", product.getName());
                jObj.put("storedescription", (sm.getStore() == null) ? "" : sm.getStore().getDescription());
                jObj.put("orderuom", (sm.getStockUoM() == null) ? "" : sm.getStockUoM().getNameEmptyforNA());
                jObj.put("costcenter", (sm.getCostCenter() == null) ? "" : sm.getCostCenter().getCcid());
                String vcName = (sm.getVendor() == null) ? ((sm.getCustomer() == null) ? null : sm.getCustomer().getName()) : sm.getVendor().getName();
                jObj.put("vendor", vcName);
                jObj.put("type", sm.getTransactionType().toString());
                //To display date in user seleceted date format.
                jObj.put("date", authHandler.getUserDateFormatterWithoutTimeZone(request).format(sm.getTransactionDate()));
                jObj.put("orderno", sm.getTransactionNo());
                jObj.put("memo", sm.getMemo());
                jObj.put("remark", sm.getRemark());
                
                if (!StringUtil.isNullOrEmpty(sm.getModuleRefId())) {
                    if(transactionModule ==TransactionModule.INTER_LOCATION_TRANSFER || transactionModule ==TransactionModule.INTER_STORE_TRANSFER){
                        InterStoreTransferRequest istRequest = istService.getInterStoreTransferById(sm.getModuleRefId());
                        jObj.put("remark", istRequest != null ? istRequest.getRemark() : "");
                        jObj.put("memo", istRequest != null ? istRequest.getMemo() : "");
                    }
                    if (transactionModule == TransactionModule.ERP_Consignment_DO) {
                        String transaction_module = "DO";
                        String modulerefdetailid = sm.getModuleRefDetailId();
                        String remark = stockMovementService.getDOdetailsremarks(modulerefdetailid, transaction_module);
                        jObj.put("remark", remark);
                    }
                    if (transactionModule == TransactionModule.ERP_SALES_RETURN) {
                        String transaction_module = "SR";
                        String modulerefdetailid = sm.getModuleRefDetailId();
                        String remark = stockMovementService.getDOdetailsremarks(modulerefdetailid, transaction_module);
                        jObj.put("remark", remark);
                    }
                }
                jObj.put("isBatchForProduct", product != null ? product.isIsBatchForProduct() : "");
                jObj.put("isSerialForProduct", product != null ? product.isIsSerialForProduct() : "");
                jObj.put("isRowForProduct", product != null ? product.isIsrackforproduct() : "");
                jObj.put("isRackForProduct", product != null ? product.isIsrackforproduct() : "");
                jObj.put("isBinForProduct", product!= null ? product.isIsbinforproduct() : "");
                jObj.put("module", transactionModule.ordinal());
                jObj.put("moduleName", moduleName);
                jObj.put("stockTypeId", sm.getProduct().getItemReusability());
                jObj.put("id", smd.getId());
                jObj.put("locationName", (smd.getLocation() != null) ? smd.getLocation().getName() : "");
                jObj.put("rowName", (smd.getRow() != null) ? smd.getRow().getName() : "");
                jObj.put("rackName", (smd.getRack() != null) ? smd.getRack().getName() : "");
                jObj.put("binName", (smd.getBin() != null) ? smd.getBin().getName() : "");
                jObj.put("quantity", authHandler.formattedQuantity(smd.getQuantity(), companyid));
                jObj.put("serialNames", (smd.getSerialNames() != null) ? smd.getSerialNames().replace(",", ", ") : "");
                jObj.put("batchName", (smd.getBatchName() != null) ? smd.getBatchName() : "");
                
                String country = "";
                
                if (sm.getVendor()!=null && !StringUtil.isNullOrEmpty(sm.getVendor().getID())) {
                    if(vendorCountryMap.containsKey(sm.getVendor().getID())){
                        country = vendorCountryMap.get(sm.getVendor().getID());
                    }else {
                        addressParams.clear();
                        addressParams.put(Constants.companyKey, companyid);
                        addressParams.put(Constants.vendorid, sm.getVendor().getID());
                        KwlReturnObject returnObject = accountingHandlerDAO.getVendorAddressDetails(addressParams);
                        if (returnObject != null && !returnObject.getEntityList().isEmpty()) {
                            VendorAddressDetails details = (VendorAddressDetails) returnObject.getEntityList().get(0);
                            if (details != null) {
                                country = details.getCountry();
                                vendorCountryMap.put(sm.getVendor().getID(), country);
                            }
                        }
                    }
                } else if (sm.getCustomer() != null && !StringUtil.isNullOrEmpty(sm.getCustomer().getID())) {
                    if (customerCountryMap.containsKey(sm.getCustomer().getID())) {
                        country = customerCountryMap.get(sm.getCustomer().getID());
                    } else {
                        addressParams.clear();
                        addressParams.put(Constants.companyKey, companyid);
                        addressParams.put(Constants.customerid, sm.getCustomer().getID());
                        KwlReturnObject returnObject = accountingHandlerDAO.getCustomerAddressDetails(addressParams);
                        if (returnObject != null && !returnObject.getEntityList().isEmpty()) {
                            CustomerAddressDetails details = (CustomerAddressDetails) returnObject.getEntityList().get(0);
                            if (details != null) {
                                country = details.getCountry();
                                customerCountryMap.put(sm.getCustomer().getID(), country);
                            }
                        }
                    }
                }
                jObj.put("country", country);
                double quantity = smd.getQuantity();
                double price = sm.getPricePerUnit();
                if (sm.getTransactionType() == TransactionType.OUT) {
                    jObj.put("qtyOut", isExport?authHandler.formattedQuantity(quantity, companyid):quantity);                              
                    quantity = -quantity;
                } else {
                    jObj.put("qtyIn", isExport?authHandler.formattedQuantity(quantity, companyid):quantity);                
                }
                double amount = quantity * price;
                jObj.put("amount", authHandler.round(amount, companyid));
                jObj.put("avgCost", authHandler.round(price, companyid));
                jObj.put("stockType", product.getItemReusability() == ItemReusability.REUSABLE ? "R" : "C");
                Set<NewBatchSerial> batchSerialSet = new HashSet<NewBatchSerial>();
                String serials = "";
                String expDate = "";
                String asset = "";
                String serialids = "";
                String serialnames="";
                String storeStr = sm.getStore() != null ? sm.getStore().getId() : "";
                String locationStr = smd.getLocation() != null ? smd.getLocation().getId() : "";
                String rowStr = smd.getRow() != null ? smd.getRow().getId() : "";
                String rackStr = smd.getRack() != null ? smd.getRack().getId() : "";
                String binStr = smd.getBin() != null ? smd.getBin().getId() : "";

                if (product.isIsSerialForProduct() && !StringUtil.isNullOrEmpty(smd.getSerialNames())) {

                    List<String> serialList = stockService.getERPSerialFromBatch(product, storeStr, locationStr, rowStr, rackStr, binStr, smd.getBatchName(), smd.getSerialNames());
                    boolean isfirst = true;
                    for (String serialId : serialList) {
                        if (isfirst) {
                            serialids += serialId;
                            isfirst = false;
                        } else {
                            serialids += "','" + serialId;
                        }
                    }
                  if(serialList.isEmpty()){
                      StringBuilder sb = new StringBuilder();
                      String srl[] = smd.getSerialNames().split(",");
                      for (String serial : srl) {
                       sb.append(serial).append("','");
                }
                      serialnames =srl.length==1?smd.getSerialNames(): sb.substring(0,sb.lastIndexOf("','"));
                  }
                }
                batchserialMap.put("moduleid", sm.getModuleRefDetailId());
                batchserialMap.put("location", locationStr);
                batchserialMap.put("warehouse", storeStr);
                batchserialMap.put("row", rowStr);
                batchserialMap.put("rack", rackStr);
                batchserialMap.put("bin", binStr);
                batchserialMap.put("seriallist", serialids != null ? serialids : "");
                batchserialMap.put("batchname", smd.getBatchName() != null ? smd.getBatchName() : "");
                batchserialMap.put("serialname",serialnames);
                batchserialMap.put("fromrepair","0");
                if(ecf.getRepairStore()!=null && ecf.getRepairStore().equalsIgnoreCase(sm.getStore().getId())){     //ERP-36490
                     batchserialMap.put("fromrepair","1");
                }
                
                List batchserialdetails = stockService.getbatchserialdetails(batchserialMap, sm.getTransactionModule(), product);
                
                if (batchserialdetails != null) {
                    serials = batchserialdetails.get(0) != null ? (String) batchserialdetails.get(0) : "";
                    serials=(!StringUtil.isNullOrEmpty(serials) && serials.endsWith(",")) ? (serials.substring(0, serials.length() - 1)) : "";
                    expDate = batchserialdetails.get(1) != null ? (String) batchserialdetails.get(1) : "";
                    expDate=(!StringUtil.isNullOrEmpty(expDate) && expDate.endsWith(",")) ? (expDate.substring(0, expDate.length() - 1)) : "";
                    asset = batchserialdetails.get(2) != null ? (String) batchserialdetails.get(2) : "";
                    asset=(!StringUtil.isNullOrEmpty(asset) && asset.endsWith(",")) ? (asset.substring(0, asset.length() - 1)) : "";
                }
                jObj.put("serialNames", serials);
                jObj.put("serialexpdate", expDate);
                jObj.put("itemasset", asset);

                if (!StringUtil.isNullOrEmpty(sm.getModuleRefId())) {
                    if (transactionModule == TransactionModule.ERP_SALES_RETURN && !batchSerialSet.isEmpty()) {
                        String moduleRefId = sm.getModuleRefId();
                        Map<String, Integer> serialCount = stockMovementService.getTransactionWiseSerialUsedCount(moduleRefId, batchSerialSet);
                        String reusabilityCount = "";
                        for (Entry<String, Integer> entry : serialCount.entrySet()) {
                            jeresult = accountingHandlerDAO.getObject(NewBatchSerial.class.getName(), entry.getKey());
                            NewBatchSerial serial = (NewBatchSerial) jeresult.getEntityList().get(0);
                            if (StringUtil.isNullOrEmpty(reusabilityCount)) {
                                reusabilityCount = serial.getSerialname() + "(" + entry.getValue() + ")";
                            } else {
                                reusabilityCount += "," + serial.getSerialname() + "(" + entry.getValue() + ")";
                            }
                        }
                        jObj.put("reusabilityCount", reusabilityCount);
                    }
                }



                // Add Product Level Custom Fiels 
                if (FieldMap != null) {
                    if (productCustomData.containsKey(product.getID())) {
                        HashMap<String, String> prodDataArray = productCustomData.get(sm.getProduct().getID());
                        for (Map.Entry<String, String> varEntry : prodDataArray.entrySet()) {
                            jObj.put(varEntry.getKey(), varEntry.getValue());
                        }
                    } else {
                        AccProductCustomData obj1 = (AccProductCustomData) kwlCommonTablesDAOObj.getClassObject(AccProductCustomData.class.getName(), product.getID());
                        if (obj1 != null) {
                            HashMap<String, String> prodDataArray = new HashMap<String, String>();
                            HashMap<String, Object> variableMap = new HashMap<String, Object>();
                            setCustomColumnValuesForProduct(obj1, FieldMap, replaceFieldMap, variableMap);
                            DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                                    Date dateFromDB=null;
                            for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                String coldata = varEntry.getValue().toString();
                                if (customFieldMap.containsKey(varEntry.getKey())) {
                                    boolean isCustomExport = true;
                                    String value = "";
                                    String Ids[] = coldata.split(",");
                                    for (int i = 0; i < Ids.length; i++) {
                                        FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), Ids[i]);
                                        if (fieldComboData != null) {
                                            if (fieldComboData.getField().getFieldtype() == 12 && !isCustomExport) {
                                                value += Ids[i] != null ? Ids[i] + "," : ",";
                                            } else {
                                                value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                            }
                                        }
                                    }
                                    if (!StringUtil.isNullOrEmpty(value)) {
                                        value = value.substring(0, value.length() - 1);
                                    }
                                    prodDataArray.put(varEntry.getKey(), value);
                                    jObj.put(varEntry.getKey(), value);
                                } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                                       try {
                                        dateFromDB = defaultDateFormat.parse(coldata);
                                        coldata = userDateFormat != null ? userDateFormat.format(dateFromDB) : df.format(dateFromDB);
                                    } catch (Exception e) {
                                    }
                                    jObj.put(varEntry.getKey(), coldata);
                                    prodDataArray.put(varEntry.getKey(),coldata);
                                } else {
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jObj.put(varEntry.getKey(), coldata);
                                        prodDataArray.put(varEntry.getKey(), coldata);
                                    }
                                }
                            }
                            productCustomData.put(product.getID(), prodDataArray);
                        }
                    }
                }
                jArray.put(jObj);

            }
            if (isExport) {
                jobj.put("data", jArray);
                jobj.put("productcustomfield", productCustomFieldInfo);
                exportDAO.processRequest(request, response, jobj);
            }
            issuccess = true;
            msg = "Inter Store Stock Transfer have been fetched successfully";
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
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
                jobj.put("productcustomfield", productCustomFieldInfo);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
          
//        Calendar c2 = Calendar.getInstance();
//        System.out.println("Total time taken ->  "+((c2.getTimeInMillis()-c1.getTimeInMillis())/1000)/60 +" minutes" + ((c2.getTimeInMillis()-c1.getTimeInMillis())/ (1000))%60+" seconds ");
        return new ModelAndView(successView, "model", jobj.toString());

    }

    private void setCustomColumnValuesForProduct(AccProductCustomData customData, HashMap<String, Integer> fieldMap, Map<String, String> replaceFieldMap,
            Map<String, Object> variableMap) {
        for (Map.Entry<String, Integer> field : fieldMap.entrySet()) {
            Integer colnumber = field.getValue();
            if (colnumber > 0) { // colnumber will be 0 if key is part of reference map
                Integer isref = fieldMap.get(field.getKey() + "#" + colnumber);// added '#' while creating map collection for custom fields.
                String coldata = null;
                if (isref != null) {
                    try {
                        coldata = customData.getCol(colnumber);
                        String coldataVal = null;
                        if (!StringUtil.isNullOrEmpty(coldata)) {
                            variableMap.put(field.getKey(), coldata);
                        }
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                    } catch (ObjectNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public ModelAndView getIssuedStockDetail(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String userId = sessionHandlerImpl.getUserid(request);

            String orderId = request.getParameter("orderId");

            StockRequest stockRequest = null;
            if (!StringUtil.isNullOrEmpty(orderId)) {
                stockRequest = stockRequestService.getStockRequestById(orderId);

                if (stockRequest != null) {
                    for (StockRequestDetail srd : stockRequest.getStockRequestDetails()) {
                        JSONObject srObject = new JSONObject();
                        srObject.put("id", srd.getId());
                        srObject.put("issuedLocationId", (srd.getIssuedLocation() != null) ? srd.getIssuedLocation().getId() : "");
                        srObject.put("issuedLocationName", (srd.getIssuedLocation() != null) ? srd.getIssuedLocation().getName() : "");
                        srObject.put("issuedRowId", (srd.getIssuedRow() != null) ? srd.getIssuedRow().getId() : "");
                        srObject.put("issuedRowName", (srd.getIssuedRow() != null) ? srd.getIssuedRow().getName() : "");
                        srObject.put("issuedRackId", (srd.getIssuedRack() != null) ? srd.getIssuedRack().getId() : "");
                        srObject.put("issuedRackName", (srd.getIssuedRack() != null) ? srd.getIssuedRack().getName() : "");
                        srObject.put("issuedBinId", (srd.getIssuedBin() != null) ? srd.getIssuedBin().getId() : "");
                        srObject.put("issuedBinName", (srd.getIssuedBin() != null) ? srd.getIssuedBin().getName() : "");
                        srObject.put("issuedQuantity", srd.getIssuedQuantity());
                        srObject.put("issuedBatch", (srd.getBatchName() != null) ? srd.getBatchName() : "");
                        NewProductBatch productBatch = stockService.getERPProductBatch(stockRequest.getProduct(), stockRequest.getFromStore(),srd.getIssuedLocation(), srd.getIssuedRow(), srd.getIssuedRack(), srd.getIssuedBin(), ((srd.getBatchName() != null) ? srd.getBatchName() : ""));
                        srObject.put("purchasebatchid",(productBatch != null) ? productBatch.getId():"" );
                        srObject.put("issuedSerials", (srd.getIssuedSerialNames() != null) ? srd.getIssuedSerialNames() : "");
                        jArray.put(srObject);
                    }
                }
            }


            msg = "Issued batch serial has been fetched successfully.";
            issuccess = true;
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView(successView, "model", jobj.toString());
    }
    
    /**
     * Method : Used to return serials which are used in stock request
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView getIssuedStockSerialDetail(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            String orderId = request.getParameter("billid");
            StockRequest stockRequest = null;
            String reqBatchName = request.getParameter("batchName") == null ? "" : request.getParameter("batchName");
            if (!StringUtil.isNullOrEmpty(orderId)) {
                stockRequest = stockRequestService.getStockRequestById(orderId);
                if (stockRequest != null) {
                    for (StockRequestDetail srd : stockRequest.getStockRequestDetails()) {
                        String srdBatchName = ((srd.getBatchName() != null) ? srd.getBatchName() : "");
                        if (reqBatchName.equals(srdBatchName)) {
                            NewProductBatch productBatch = stockService.getERPProductBatch(stockRequest.getProduct(), stockRequest.getToStore(), srd.getIssuedLocation(), srd.getIssuedRow(), srd.getIssuedRack(), srd.getIssuedBin(), srdBatchName);
                        if (!StringUtil.isNullOrEmpty(srd.getIssuedSerialNames())) {
                            String[] serialArray = srd.getIssuedSerialNames().split(",");
                            for (String serialName : serialArray) {
                                NewBatchSerial batchSerial = stockService.getERPBatchSerial(stockRequest.getProduct(), productBatch, serialName);
                                JSONObject obj = new JSONObject();
                                if (batchSerial != null) {
                                    obj.put("id", batchSerial.getId());
                                    obj.put("serialno", batchSerial.getSerialname());
                                    obj.put("serialnoid", batchSerial.getId());
                                    obj.put("expstart", batchSerial.getExpfromdate() != null ? df.format(batchSerial.getExpfromdate()) : "");
                                    obj.put("expend", batchSerial.getExptodate() != null ? df.format(batchSerial.getExptodate()) : "");
                                    obj.put("purchaseserialid", batchSerial.getId());
                                    obj.put("purchasebatchid", (batchSerial.getBatch() != null) ? batchSerial.getBatch().getId() : "");
                                    obj.put("skufield", batchSerial.getSkufield());
                                    
                                } else {
                                    batchSerial = stockService.getSerialDataBySerialName(stockRequest.getProduct(), serialName);
                                        if (batchSerial != null) {
                                        obj.put("id", batchSerial.getId());
                                        obj.put("serialno", batchSerial.getSerialname());
                                        obj.put("serialnoid", batchSerial.getId());
                                        obj.put("expstart", batchSerial.getExpfromdate() != null ? df.format(batchSerial.getExpfromdate()) : "");
                                        obj.put("expend", batchSerial.getExptodate() != null ? df.format(batchSerial.getExptodate()) : "");
                                        obj.put("purchaseserialid", batchSerial.getId());
                                        obj.put("purchasebatchid", (batchSerial.getBatch() != null) ? batchSerial.getBatch().getId() : "");
                                        obj.put("skufield", batchSerial.getSkufield());
                                    }
                                }
                                 jArray.put(obj);
                            }
                        }
                    }
                }
            }
            }
            msg = "Issued batch serial has been fetched successfully.";
            issuccess = true;
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getISTStockDetail(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String userId = sessionHandlerImpl.getUserid(request);

            String orderId = request.getParameter("orderId");

            InterStoreTransferRequest istRequest = null;
            if (!StringUtil.isNullOrEmpty(orderId)) {
                istRequest = istService.getInterStoreTransferById(orderId);

                if (istRequest != null) {
                    for (ISTDetail isd : istRequest.getIstDetails()) {
                        JSONObject srObject = new JSONObject();
                        srObject.put("id", isd.getId());
                        srObject.put("issuedLocationId", (isd.getIssuedLocation() != null) ? isd.getIssuedLocation().getId() : "");
                        srObject.put("issuedLocationName", (isd.getIssuedLocation() != null) ? isd.getIssuedLocation().getName() : "");
                        srObject.put("issuedRowId", (isd.getIssuedRow() != null) ? isd.getIssuedRow().getId() : "");
                        srObject.put("issuedRowName", (isd.getIssuedRow() != null) ? isd.getIssuedRow().getName() : "");
                        srObject.put("issuedRackId", (isd.getIssuedRack() != null) ? isd.getIssuedRack().getId() : "");
                        srObject.put("issuedRackName", (isd.getIssuedRack() != null) ? isd.getIssuedRack().getName() : "");
                        srObject.put("issuedBinId", (isd.getIssuedBin() != null) ? isd.getIssuedBin().getId() : "");
                        srObject.put("issuedBinName", (isd.getIssuedBin() != null) ? isd.getIssuedBin().getName() : "");
                        srObject.put("issuedQuantity", isd.getIssuedQuantity());
                        srObject.put("issuedBatch", (isd.getBatchName() != null) ? isd.getBatchName() : "");
                        srObject.put("issuedSerials", (isd.getIssuedSerialNames() != null) ? isd.getIssuedSerialNames() : "");
                        jArray.put(srObject);
                    }
                }
            }


            msg = "IST issued stock detail has been fetched successfully.";
            issuccess = true;
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView acceptReturnStockRequest(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            List<String> produtctids = new ArrayList<>();
            boolean sendForQAApproval = false;
            String serials = request.getParameter("serialNames");
            String reqId = request.getParameter("id");
            if ("true".equals(request.getParameter("sendForQAApproval"))) {
                sendForQAApproval = true;
            }

            StockRequest stockRequest = stockRequestService.getStockRequestById(reqId);
            Product product = stockRequest.getProduct();
            produtctids.add(product.getID());
            if (sendForQAApproval) {
                String[] serialNames = null;
                if (stockRequest != null && stockRequest.getProduct().isIsSerialForProduct()) {
                    if (!StringUtil.isNullOrEmpty(serials)) {
                        serialNames = serials.split(",");
                    }
                }
                stockRequestService.sendReturnStockRequestForQA(user, stockRequest, serialNames);

                if (serialNames != null) {
                    auditMessage += " and Serial: (" + serials + ") send for QA Approval";
                } else {
                    auditMessage += " and send for QA Approval";
                }

            } else {
                stockRequestService.acceptReturnStockRequest(user, stockRequest);
            }

            msg = "Return Stock Request accepted successfully.";
            issuccess = true;

            auditMessage = "User " + user.getFullName() + " has accepted returned stock for Stock Request: " + stockRequest.getTransactionNo() + " Product :" + product.getProductid() + " Quantity :" + stockRequest.getDeliveredQty() + " " + stockRequest.getUom().getNameEmptyforNA() + "(" + stockRequest.getPackaging().getQuantityInStockUoM(stockRequest.getUom(), stockRequest.getDeliveredQty()) + " " + stockRequest.getPackaging().getStockUoM().getNameEmptyforNA() + ")" + " in Store: " + stockRequest.getFromStore().getAbbreviation() + ", " + auditMessage;
            auditTrailObj.insertAuditLog(AuditAction.STOCK_REQUEST_COLLECTED, auditMessage, request, stockRequest.getId());
            String valuation_productids = StringUtil.join(",", produtctids);
            jobj.put("productIds",valuation_productids);
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getStockRequestDetailBySequenceNo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SA_Tx_Get");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String sequenceNo = request.getParameter("sequenceNo");
            String moduleName = request.getParameter("moduleName");
            TransactionModule module;
            try{
                module = TransactionModule.valueOf(moduleName);
            }catch(Exception ex){
                module=null;
            }
            String type = "1"; // for order

            List<StockRequest> srList = null;
            if (!StringUtil.isNullOrEmpty(sequenceNo)) {
                srList = stockRequestService.getStockRequestDetailBySequenceNo(company, sequenceNo,module);
            }


            if (srList != null && !srList.isEmpty()) {

                for (StockRequest sr : srList) {

                    JSONObject jObj = new JSONObject();
                    JSONArray stockDetails = new JSONArray();


                    jObj.put("id", sr.getId());
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
                    jObj.put("uomId", sr.getUom() != null ? sr.getUom().getID() : "");
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
                    jObj.put("nwquantity", "1".equals(type) && sr.getStatus() != RequestStatus.ISSUED ? sr.getOrderedQty() : sr.getIssuedQty());
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
                     Line level Custom data
                     */
                    HashMap<String, String> customFieldMap = new HashMap<>();
                    HashMap<String, String> customDateFieldMap = new HashMap<>();
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    HashMap<String, String> replaceFieldMap = new HashMap<>();
                    Map<String, Object> variableMap = new HashMap<>();

                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(user.getCompany().getCompanyID(), Constants.Acc_Stock_Request_ModuleId, 1));
                    HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    StockCustomData stockLineLevelCustomData = sr.getStockLineLevelCustomData();
                    AccountingManager.setCustomColumnValues(stockLineLevelCustomData, FieldMap, replaceFieldMap, variableMap);
                    if (stockLineLevelCustomData != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, true);
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jObj, params);
                    }

                    jArray.put(jObj);

                }
            }


            issuccess = true;
            msg = "Stock Request Detail has been fetched successfully";
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getInterStockTransferBySequenceNo(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SA_Tx_Get");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String sequenceNo = request.getParameter("sequenceNo");

            List<InterStoreTransferRequest> istrList = null;
            if (!StringUtil.isNullOrEmpty(sequenceNo)) {
                istrList = istService.getInterStoreTransferBySequenceNo(company, sequenceNo);
            }


            for (InterStoreTransferRequest ist : istrList) {
                JSONObject jObj = new JSONObject();
                JSONArray stockDetails = new JSONArray();



                jObj.put("id", ist.getId());
                jObj.put("transfernoteno", ist.getTransactionNo());
                jObj.put("itemId", ist.getProduct().getID());
                jObj.put("itemcode", ist.getProduct().getProductid());
                jObj.put("itemname", ist.getProduct().getName());
                jObj.put("partnumber", ist.getProduct().getCoilcraft());
                jObj.put("itemdescription", ist.getProduct().getDescription());
                jObj.put("fromStoreId", ist.getFromStore().getId());
                jObj.put("fromstorename", ist.getFromStore().getFullName());
                jObj.put("fromStoreName", ist.getFromStore().getDescription());
                jObj.put("fromstoreadd", ist.getFromStore() != null ? ist.getFromStore().getAddress() : "");
                jObj.put("fromstorefax", ist.getFromStore() != null ? ist.getFromStore().getFaxNo() : "");
                jObj.put("fromstorephno", ist.getFromStore() != null ? ist.getFromStore().getContactNo() : "");
                jObj.put("toStoreId", ist.getToStore().getId());
                jObj.put("tostorename", ist.getToStore().getFullName());
                jObj.put("toStoreName", ist.getToStore().getDescription());
                jObj.put("tostoreadd", ist.getToStore() != null ? ist.getToStore().getAddress() : "");
                jObj.put("tostorefax", ist.getToStore() != null ? ist.getToStore().getFaxNo() : "");
                jObj.put("tostorephno", ist.getToStore() != null ? ist.getToStore().getContactNo() : "");
                jObj.put("packaging", ist.getPackaging() != null ? ist.getPackaging().toString() : "");
                jObj.put("uomid", ist.getUom().getID());
                jObj.put("name", ist.getUom().getNameEmptyforNA());
                jObj.put("orderinguomname", ist.getProduct().getOrderingUOM() == null ? "" : ist.getProduct().getOrderingUOM().getNameEmptyforNA());
                jObj.put("transferinguomname", ist.getProduct().getTransferUOM() == null ? "" : ist.getProduct().getTransferUOM().getNameEmptyforNA());
                jObj.put("stockuomname", ist.getProduct().getUnitOfMeasure() == null ? "" : ist.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                jObj.put("statusId", ist.getStatus().ordinal());
                jObj.put("status", ist.getStatus().toString());
                jObj.put("remark", ist.getRemark());
                jObj.put("quantity", ist.getOrderedQty());
                jObj.put("acceptedqty", ist.getAcceptedQty());
                jObj.put("isBatchForProduct", ist.getProduct() != null ? ist.getProduct().isIsBatchForProduct() : "");
                jObj.put("hscode", ist.getProduct() != null ? ist.getProduct().getHSCode() : "");
                jObj.put("isSerialForProduct", ist.getProduct() != null ? ist.getProduct().isIsSerialForProduct() : "");
                jObj.put("costcenter", ist.getCostCenter() == null ? "" : ist.getCostCenter().getCcid());


                if (ist != null) {
                    for (ISTDetail istd : ist.getIstDetails()) {
                        if (istd != null) {
                            JSONObject srObject = new JSONObject();
                            srObject.put("id", istd.getId());
                            srObject.put("issuedLocationName", (istd.getIssuedLocation() != null) ? istd.getIssuedLocation().getName() : "");
                            srObject.put("issuedRowName", (istd.getIssuedRow() != null) ? istd.getIssuedRow().getName() : "");
                            srObject.put("issuedRackName", (istd.getIssuedRack() != null) ? istd.getIssuedRack().getName() : "");
                            srObject.put("issuedBinName", (istd.getIssuedBin() != null) ? istd.getIssuedBin().getName() : "");
                            srObject.put("issuedQuantity", istd.getIssuedQuantity());
                            srObject.put("issuedSerials", (istd.getIssuedSerialNames() != null) ? istd.getIssuedSerialNames().replace(",", ", ") : "");
                            srObject.put("batchName", (istd.getBatchName() != null) ? istd.getBatchName() : "");
                            srObject.put("collectedLocationName", (istd.getDeliveredLocation() != null) ? ((Location) istd.getDeliveredLocation()).getName() : "");
                            srObject.put("collectedRowName", (istd.getDeliveredRow() != null) ? istd.getDeliveredRow().getName() : "");
                            srObject.put("collectedRackName", (istd.getDeliveredRack() != null) ? istd.getDeliveredRack().getName() : "");
                            srObject.put("collectedBinName", (istd.getDeliveredBin() != null) ? istd.getDeliveredBin().getName() : "");
                            srObject.put("collectedQuantity", istd.getDeliveredQuantity());
                            srObject.put("collectedSerials", (istd.getDeliveredSerialNames() != null) ? istd.getDeliveredSerialNames().replace(",", ", ") : "");
                            stockDetails.put(srObject);
                        }
                    }
                }
                jObj.put("stockDetails", stockDetails);
                
                                    
                /*
                 Line level Custom data
                 */
                HashMap<String, String> customFieldMap = new HashMap<>();
                HashMap<String, String> customDateFieldMap = new HashMap<>();
                HashMap<String, Object> fieldrequestParams = new HashMap();
                HashMap<String, String> replaceFieldMap = new HashMap<>();
                Map<String, Object> variableMap = new HashMap<>();

                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(user.getCompany().getCompanyID(), Constants.Acc_InterStore_ModuleId, 1));
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                InterStoreTransferCustomData interStoreTransferLineLevelCustomData = ist.getISTLineLevelCustomData();
                AccountingManager.setCustomColumnValues(interStoreTransferLineLevelCustomData, FieldMap, replaceFieldMap, variableMap);
                if (interStoreTransferLineLevelCustomData != null) {
                    JSONObject params = new JSONObject();
                    params.put(Constants.isExport, true);
                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jObj, params);
                }

                if (ist.getBusinessDate() != null) {
                    jObj.put("date", df.format(ist.getBusinessDate()));
                }
                if (ist.getModifiedOn() != null) {
                    jObj.put("modifiedOn", df.format(ist.getModifiedOn()));
                }

                if (ist.getCreatedBy() != null) {
                    jObj.put("createdby", ist.getCreatedBy().getFullName());
                }

                if (ist.getModifiedBy() != null) {
                    jObj.put("modifiedBy", ist.getModifiedBy().getFullName());
                }

                if (ist.getApprovedBy() != null) {
                    jObj.put("approvedBy", ist.getApprovedBy().getFullName());
                }

                jArray.put(jObj);
            }
            issuccess = true;
            msg = "Inter Store Stock Transfer have been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView getISTIssuedDetailList(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cap1 = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences ecp = (ExtraCompanyPreferences) cap1.getEntityList().get(0);
            String istRequestId = request.getParameter("requestId");

            InterStoreTransferRequest ist = istService.getInterStoreTransferById(istRequestId);
            for (ISTDetail istd : ist.getIstDetails()) {
                JSONObject jObj = new JSONObject();
                jObj.put("detailId", istd.getId());
                jObj.put("fromStoreId", ist.getFromStore().getId());
                jObj.put("batchName", istd.getBatchName());
                jObj.put("fromLocationId", istd.getIssuedLocation().getId());
                jObj.put("fromLocationName", istd.getIssuedLocation().getName());
                if (istd.getIssuedRow() != null) {
                    jObj.put("fromRowId", istd.getIssuedRow().getId());
                    jObj.put("fromRowName", istd.getIssuedRow().getName());
                }
                if (istd.getIssuedRack() != null) {
                    jObj.put("fromRackId", istd.getIssuedRack().getId());
                    jObj.put("fromRackName", istd.getIssuedRack().getName());
                }
                if (istd.getIssuedBin() != null) {
                    jObj.put("fromBinId", istd.getIssuedBin().getId());
                    jObj.put("fromBinName", istd.getIssuedBin().getName());
                }
                jObj.put("availableSerials", istd.getIssuedSerialNames());
                if (ecp.isSKUFieldParm()) {
                    String skuValues=stockService.getSkuDataBySerialName(ist.getProduct(), istd.getIssuedSerialNames());
                    jObj.put("availableSKUs", skuValues);
                }
                jObj.put("availableQty", istd.getIssuedQuantity());
                
                
                jArray.put(jObj);

            }
            issuccess = true;
            msg = "operation successfully";

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
        return new ModelAndView(successView, "model", jobj.toString());

    }
/**
     * Method : Used to return serials which are used in stock request
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView getISTIssuedSerialDetailList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String istRequestId = request.getParameter("billid");
            if (!StringUtil.isNullOrEmpty(istRequestId)) {
              KwlReturnObject result = accountingHandlerDAO.getObject(ISTDetail.class.getName(), istRequestId);
            ISTDetail istd = (ISTDetail) result.getEntityList().get(0);  
                if (istd != null) {
                        NewProductBatch productBatch = stockService.getERPProductBatch(istd.getIstRequest().getProduct(), istd.getIstRequest().getFromStore(),istd.getIssuedLocation(), istd.getIssuedRow(), istd.getIssuedRack(), istd.getIssuedBin(), ((istd.getBatchName() != null) ? istd.getBatchName() : ""));
                        if (!StringUtil.isNullOrEmpty(istd.getIssuedSerialNames())) {
                            String[] serialArray = istd.getIssuedSerialNames().split(",");
                            for (String serialName : serialArray) {
                                NewBatchSerial batchSerial = stockService.getERPBatchSerial(istd.getIstRequest().getProduct(), productBatch, serialName);
                                JSONObject obj = new JSONObject();
                                if (batchSerial != null) {
                                    obj.put("id", batchSerial.getId());
                                    obj.put("serialno", batchSerial.getSerialname());
                                    obj.put("serialnoid", batchSerial.getId());
                                    obj.put("expstart", batchSerial.getExpfromdate() != null ? df.format(batchSerial.getExpfromdate()) : "");
                                    obj.put("expend", batchSerial.getExptodate() != null ? df.format(batchSerial.getExptodate()) : "");
                                    obj.put("purchaseserialid", batchSerial.getId());
                                    obj.put("purchasebatchid", (batchSerial.getBatch() != null) ? batchSerial.getBatch().getId() : "");
                                    obj.put("skufield", batchSerial.getSkufield());

                                } else {
                                    batchSerial = stockService.getSerialDataBySerialName(istd.getIstRequest().getProduct(), serialName);
                                    if(batchSerial != null){
                                        obj.put("id", batchSerial.getId());
                                        obj.put("serialno", batchSerial.getSerialname());
                                        obj.put("serialnoid", batchSerial.getId());
                                        obj.put("expstart", batchSerial.getExpfromdate() != null ? df.format(batchSerial.getExpfromdate()) : "");
                                        obj.put("expend", batchSerial.getExptodate() != null ? df.format(batchSerial.getExptodate()) : "");
                                        obj.put("purchaseserialid", batchSerial.getId());
                                        obj.put("purchasebatchid", (batchSerial.getBatch() != null) ? batchSerial.getBatch().getId() : "");
                                        obj.put("skufield", batchSerial.getSkufield());
                                    }
                                }
                                 jArray.put(obj);
                            }
                        }
                }
            }
            msg = "Issued batch serial has been fetched successfully.";
            issuccess = true;
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView(successView, "model", jobj.toString());
    }
    public ModelAndView getSRIssuedDetailList(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String istRequestId = request.getParameter("requestId");
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cap1 = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences ecp = (ExtraCompanyPreferences) cap1.getEntityList().get(0);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            StockRequest sr = stockRequestService.getStockRequestById(istRequestId);
            for (StockRequestDetail srd : sr.getStockRequestDetails()) {
                JSONObject jObj = new JSONObject();
                jObj.put("orderId", sr.getId());
                jObj.put("detailId", srd.getId());
                jObj.put("fromStoreId", sr.getFromStore().getId());
                jObj.put("batchName", srd.getBatchName());
                jObj.put("fromLocationId", srd.getIssuedLocation().getId());
                jObj.put("fromLocationName", srd.getIssuedLocation().getName());
                if (srd.getIssuedRow() != null) {
                    jObj.put("fromRowId", srd.getIssuedRow().getId());
                    jObj.put("fromRowName", srd.getIssuedRow().getName());
                }
                if (srd.getIssuedRack() != null) {
                    jObj.put("fromRackId", srd.getIssuedRack().getId());
                    jObj.put("fromRackName", srd.getIssuedRack().getName());
                }
                if (srd.getIssuedBin() != null) {
                    jObj.put("fromBinId", srd.getIssuedBin().getId());
                    jObj.put("fromBinName", srd.getIssuedBin().getName());
                }
                jObj.put("availableSerials", srd.getIssuedSerialNames());
                jObj.put("availableQty", srd.getIssuedQuantity());

                NewProductBatch npb = stockService.getERPProductBatch(sr.getProduct(), sr.getFromStore(), srd.getIssuedLocation(), srd.getIssuedRow(), srd.getIssuedRack(), srd.getIssuedBin(), srd.getBatchName());
                jObj.put("purchasebatchid", npb != null ? npb.getId() : "");
                
                JSONArray serialArray = new JSONArray();
                StringBuilder sku = new StringBuilder();
                if (ecp.isSKUFieldParm() && sr.getProduct().isIsSerialForProduct()) {                    
                    String[] serialArr = srd.getIssuedSerialNames().split(",");
                    NewBatchSerial serial = null;
                     for (String srl : serialArr) {
                        serial = stockService.getERPBatchSerial(sr.getProduct(), npb, srl);
                        if(serial !=null){
                        if (sku.length() == 0) {
                            sku.append(serial.getSkufield() == null ? "" : serial.getSkufield());
                        } else {
                            sku.append(",").append(serial.getSkufield() == null ? "" : serial.getSkufield());
                        }
                        }
                    }

                }
                jObj.put("availableSKUs", sku.toString());
                jArray.put(jObj);

            }
            issuccess = true;
            msg = "operation successfully";

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
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView deleteInterStoreTransferRequest(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);


            String reqIds = request.getParameter("requestIds"); // comma separated

            String[] istIdArray = null;
            if (!StringUtil.isNullOrEmpty(reqIds)) {
                istIdArray = reqIds.split(",");
                for (String istId : istIdArray) {
                    InterStoreTransferRequest istRequest = istService.getInterStoreTransferById(istId);
                    if (!StringUtil.isNullOrEmpty(auditMessage)) {
                        auditMessage += ", ";
                    }
                    auditMessage += "(Transaction No : " + istRequest.getTransactionNo();
                    auditMessage += " Product : " + istRequest.getProduct().getProductid();
                    auditMessage += " From Store : " + istRequest.getFromStore().getAbbreviation();
                    auditMessage += " To Store : " + istRequest.getToStore().getAbbreviation();
                    auditMessage += " Quantity : " + istRequest.getOrderedQty() + " " + istRequest.getUom().getNameEmptyforNA() + ")";
                    if(istRequest.getStatus().equals(InterStoreTransferStatus.INTRANSIT)){
                    istService.deleteISTRequest(istRequest);
                    }else{
                        istService.deleteAcceptedISTRequests(istRequest);
                    }
                }
                msg = "Request(s) has been deleted successfully";
                issuccess = true;

                auditMessage = "User " + user.getFullName() + " has deleted IST Request(s): " + auditMessage;
                auditTrailObj.insertAuditLog(AuditAction.INTER_STORE_REQUEST_REJECTED, auditMessage, request, "");
            } else {
                msg = "Request is not selected";
                issuccess = false;
            }

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView deleteInterLocationTransferRequest(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);


            String reqIds = request.getParameter("requestIds"); // comma separated

            boolean avlbl = checkForAvailableQty(reqIds);
            if (avlbl) {
                String[] istIdArray = null;
                if (!StringUtil.isNullOrEmpty(reqIds)) {
                    istIdArray = reqIds.split(",");
                    for (String istId : istIdArray) {
                        InterStoreTransferRequest istRequest = istService.getInterStoreTransferById(istId);
                        if (!StringUtil.isNullOrEmpty(auditMessage)) {
                            auditMessage += ", ";
                        }
                        auditMessage += "(Transaction No : " + istRequest.getTransactionNo();
                        auditMessage += " Product : " + istRequest.getProduct().getProductid();
                        auditMessage += " From Store : " + istRequest.getFromStore().getAbbreviation();
                        auditMessage += " To Store : " + istRequest.getToStore().getAbbreviation();
                        auditMessage += " Quantity : " + istRequest.getOrderedQty() + " " + istRequest.getUom().getNameEmptyforNA() + ")";
                        if (istRequest.getStatus() != InterStoreTransferStatus.DELETED) {
                            istService.deleteILocationTRequest(istRequest);
                        } else {
                            msg = "Request('" + istRequest.getTransactionNo() + "' already deleted)";
                            issuccess = false;
                        }
                    }
                    msg = "Request(s) has been deleted successfully";
                    issuccess = true;

                    auditMessage = "User " + user.getFullName() + " has deleted IST Request(s): " + auditMessage;
                    auditTrailObj.insertAuditLog(AuditAction.INTER_STORE_REQUEST_REJECTED, auditMessage, request, "");
                } else {
                    msg = "Request is not selected";
                    issuccess = false;
                }
            } else {
                issuccess = false;
                msg = "quantity is not available for some products";
            }
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView deleteStockRequest(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ARSR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);


            String reqIds = request.getParameter("requestIds"); // comma separated

            String[] reqIdArray = null;
            if (!StringUtil.isNullOrEmpty(reqIds)) {
                reqIdArray = reqIds.split(",");
                for (String reqId : reqIdArray) {
                    StockRequest sr = stockRequestService.getStockRequestById(reqId);
                    if (!StringUtil.isNullOrEmpty(auditMessage)) {
                        auditMessage += ", ";
                    }
                    auditMessage += "(Transaction No : " + sr.getTransactionNo();
                    auditMessage += " Product : " + sr.getProduct().getProductid();
                    auditMessage += " For Store : " + sr.getFromStore().getAbbreviation();
                    auditMessage += " Quantity : " + sr.getOrderedQty() + " " + sr.getUom().getNameEmptyforNA() + ")";
                    stockRequestService.deleteStockRequest(sr);
                }
                msg = "Request(s) has been deleted successfully";
                issuccess = true;

                auditMessage = "User " + user.getFullName() + " has deleted Stock Request(s): " + auditMessage;
                auditTrailObj.insertAuditLog(AuditAction.STOCK_REQUEST_ADDED, auditMessage, request, "");
            } else {
                msg = "Request is not selected";
                issuccess = false;
            }

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    private boolean checkForAvailableQty(String reqIds) throws ServiceException {
        boolean availableQty = true;
        String[] istIdArray = null;
        if (!StringUtil.isNullOrEmpty(reqIds)) {
            istIdArray = reqIds.split(",");
            for (String istId : istIdArray) {
                InterStoreTransferRequest istRequest = istService.getInterStoreTransferById(istId);
                Product p = istRequest.getProduct();
                for (ISTDetail istd : istRequest.getIstDetails()) {
                    String searchStr = p.isIsSerialForProduct() ? istd.getDeliveredSerialNames() : p.isIsBatchForProduct() ? istd.getBatchName() : "";

                    if (p.isIsSerialForProduct()) {
                        String[] delSer = StringUtil.isNullOrEmpty(istd.getDeliveredSerialNames()) ? null : istd.getDeliveredSerialNames().split(",");
                        List<NewBatchSerial> serialList = stockService.getERPActiveSerialList(p, istRequest.getToStore(), istd.getDeliveredLocation(), istd.getBatchName(), true);
                        Set<String> st = new HashSet<>();
                        for (NewBatchSerial serial : serialList) {
                            st.add(serial.getSerialname());
                        }
                        Set<String> derSerials = new HashSet<>(Arrays.asList(delSer));
                        if (!st.containsAll(derSerials)) {
                            availableQty = false;
                            break;
                        }
//                        for (int i = 0; i < delSer.length; i++) {
//                            String serialName = delSer[i].toString();
//                            if (!st.contains(serialName)) {
//                                availableQty = false;
//                                return availableQty;
//                            }
//                        }
                    } else if (p.isIsBatchForProduct()) {
                        NewProductBatch newProductBatch = stockService.getERPProductBatch(p, istRequest.getToStore(), istd.getDeliveredLocation(), istd.getDeliveredRow(), istd.getDeliveredRack(), istd.getDeliveredBin(), istd.getBatchName());
                        if (!newProductBatch.getBatchname().equals(istd.getBatchName()) || newProductBatch.getQuantitydue() < istd.getDeliveredQuantity()) {
                            availableQty = false;
                            break;
                        }
                    } else if (!p.isIsSerialForProduct() && !p.isIsBatchForProduct()) {
                        double qty = stockService.getProductTotalAvailableQuantity(p, istRequest.getToStore(), istd.getDeliveredLocation());
                        if (qty < istd.getDeliveredQuantity()) {
                            availableQty = false;
                            break;
                        }
                    }
                }
                if (!availableQty) {
                    break;
                }
            }
        }
        return availableQty;
    }
    
    
    public ModelAndView deleteProductBuildAssembly(HttpServletRequest request, HttpServletResponse response) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BuildAssembly_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            deleteExistingBuildAssemblyEntries(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.buildassembly.deletebuildaseembly", null, RequestContextUtils.getLocale(request));
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(GoodsTransferController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(GoodsTransferController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(GoodsTransferController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
     public boolean deleteExistingBuildAssemblyEntries(HttpServletRequest request) throws ServiceException,SessionExpiredException,AccountingException {
        try{
             String productids[] = request.getParameterValues("productids");
            String productrefno[] = request.getParameterValues("productrefno");
            String mainproductids[] = request.getParameterValues("product");
            String assmbledProdQty[] = request.getParameterValues("assmbledProdQty");
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            for (int i = 0; i < productids.length; i++) {
//                addAsblyProdsNegativeEntry(productids[i], companyid);
                KwlReturnObject prodresult1 = accProductObj.getObject(ProductBuild.class.getName(), productids[i]);
                ProductBuild productbuild = (ProductBuild) prodresult1.getEntityList().get(0);
                
                JSONObject bomjson = new JSONObject();
                double quantitydue = 0.0;
                String  bomcode = productbuild.getBomdetail().getBomCode();
                bomjson.put("bomid", productbuild.getBomdetail().getID());
                bomjson.put("productid", mainproductids[i]);
                bomjson.put(Constants.companyKey, companyid);
                KwlReturnObject retObj = accProductObj.getAssembyProductBOMDetails(bomjson);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    List<Object[]> list = retObj.getEntityList();
                    if (list.size() > 0) {
                        if (!StringUtil.isNullObject(list.get(0))) {
                            for(Object[] row : list){
                                quantitydue = (double) row[4] - (double) row[5];
                            }
                        }
                    }
                }
                if(quantitydue < productbuild.getQuantity()){
                    throw new AccountingException("This Build Assembly record is created by using '"+ bomcode +"' Bom Code. It can not be deleted as stock with this BOM CODE '"+ bomcode +"' is not available.");
                }
                
                //ERP-11730
                JSONObject assmblejson = new JSONObject();
                assmblejson.put("productid",mainproductids[i]);
                assmblejson.put("productBuildID",productbuild.getID());
                assmblejson.put("productBuildQuantity",productbuild.getQuantity());
                assmblejson.put("companyid", companyid);
                accProductObj.updateQuantityDueOfSerailnumbers(assmblejson);
                
                // delete ProductBuild inventory
                if (productbuild.getInventory() != null) {
                    try {
                        accProductObj.deleteInventory(productbuild.getInventory().getID(), companyid);
                    } catch (Exception ex) {
                        Logger.getLogger(GoodsTransferController.class.getName()).info(ex.getMessage());
                    }
                }
                
                
                // Delete Stock Movement for Product Build
                 stockMovementService.removeStockMovementByReferenceId(productbuild.getCompany(), productbuild.getID());
                // delete ProductBuildDetail inventory
                KwlReturnObject result = accProductObj.getProductBuildDetailInventory(productbuild.getID());
                List<ProductBuildDetails> list = result.getEntityList();
                for (ProductBuildDetails buildDetails : list) {
                    if(buildDetails.getAproduct()!=null){
                        if(buildDetails.getRecycleQuantity()==0 && buildDetails.getRemainingQuantity() > 0 ){
                            buildDetails.getAproduct().setRecycleQuantity(buildDetails.getAproduct().getRecycleQuantity()-buildDetails.getRemainingQuantity());
                        }
                        buildDetails.getAproduct().setRecycleQuantity(buildDetails.getAproduct().getRecycleQuantity()+buildDetails.getRecycleQuantity());
                    }
                    accProductObj.deleteInventory(buildDetails.getInventory().getID(), companyid);
                }
                
                stockAdjustmentService.deleteStockadjustmentForBuildassemby(productbuild.getCompany(), productbuild.getID(),user);
                accProductObj.deleteProductBuildDetailsByID(productids[i], companyid);
                accProductObj.deleteProductbBuildByID(productids[i], companyid);
                accJournalEntryobj.deleteJournalEntryPermanent(productbuild.getJournalentry().getID(), sessionHandlerImpl.getCompanyid(request));

                if (!isEdit) {
                    auditTrailObj.insertAuditLog(AuditAction.PRODUCT_BUILD_ASSEMBLY_DELETION, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted product build Assembly " + productrefno[i], request, productids[i]);
                }
            }                
        }catch(AccountingException ex){
            throw new AccountingException(ex.getMessage(), ex);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }
     
    public ModelAndView getDefaultLocationDetailForIssueNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String productIdStr=request.getParameter("productIds");
            String [] productIdArr = !StringUtil.isNullOrEmpty(productIdStr) ? productIdStr.split(",")  : new String[0];
            String fromStoreId = request.getParameter("fromStoreId");
            String toStoreId = request.getParameter("toStoreId");
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);
            Store fromStore = null;
            if (!StringUtil.isNullOrEmpty(fromStoreId)) {
                fromStore = storeService.getStoreById(fromStoreId);
            }
            
            Store toStore = null;
            if (!StringUtil.isNullOrEmpty(toStoreId)) {
                toStore = storeService.getStoreById(toStoreId);
            }
            String fromStoresDefaultLocationId="";
            String toStoresDefaultLocationId="";
            String fromStoresDefaultLocationName="";
            String toStoresDefaultLocationName="";
            
            for (String productId : productIdArr) {
                double availableQty = 0;
                Product product = null;
                if (!StringUtil.isNullOrEmpty(productId)) {
                    jeresult = accountingHandlerDAO.getObject(Product.class.getName(), productId);
                    product = (Product) jeresult.getEntityList().get(0);
                }

                if (fromStore != null && toStore != null && product != null) {
                    availableQty = stockService.getProductQuantityInStoreLocation(product, fromStore, fromStore.getDefaultLocation());
                    fromStoresDefaultLocationId = fromStore.getDefaultLocation() != null ? fromStore.getDefaultLocation().getId() : "";
                    toStoresDefaultLocationId = toStore.getDefaultLocation() != null ? toStore.getDefaultLocation().getId() : "";
                    fromStoresDefaultLocationName = toStore.getDefaultLocation() != null ? toStore.getDefaultLocation().getName() : "";
                    toStoresDefaultLocationName = toStore.getDefaultLocation() != null ? toStore.getDefaultLocation().getName() : "";
                    
                    JSONObject jDataObj = new JSONObject();
                    jDataObj.put("productId", productId);
                    jDataObj.put("defaultFromLocQty", availableQty);
                    jDataObj.put("defultFromLocationID", fromStoresDefaultLocationId);
                    jDataObj.put("defultToLocationID", toStoresDefaultLocationId);
                    jDataObj.put("defultFromLocationName", fromStoresDefaultLocationName);
                    jDataObj.put("defultToLocationName", toStoresDefaultLocationName);
                    jArray.put(jDataObj);
                }
            }

            issuccess = true;
            msg = "product's available quantity in default location has been fetched successfully";

        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray.toString());
                jobj.put("count", jArray.length());
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }
    /**
     *
     * @param request
     * @param response
     * @return
     * @Desc : Return single record request to load data in UI
     */
    public ModelAndView getSingleStockRequestToLoad(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            /*
             Get record for single template
             */
            JSONObject jSONObject = stockRequestService.getSingleStockRequestToLoad(paramJobj);
            jobj.put("data", jSONObject.optJSONArray("loaddata"));
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(GoodsTransferController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(GoodsTransferController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(GoodsTransferController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getPurchasePriceForSA(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jsonObj = new JSONObject();
        double price=0.0;
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        String auditMessage = "";
        HashMap<String, Object> requestParams = new HashMap<>();
        StringBuffer productIds = new StringBuffer();
        try {
            String productId = request.getParameter("productId");
            String bssDate = request.getParameter("bssDate");
            Date fromDate = null;
            Date toDate = null;
            try {
                fromDate = df.parse(bssDate);
            } catch (ParseException ex) {
            }
            if (!StringUtil.isNullOrEmpty(productId)) {

                KwlReturnObject purchase = accProductObj.getProductPrice(productId, true, fromDate, "", "");
                if (purchase.getEntityList() != null && purchase.getEntityList().size() > 0&&purchase.getEntityList().get(0)!=null) {
                    List ls = purchase.getEntityList();
                    Iterator itr = ls.iterator();
                    while (itr.hasNext()) {
                        JSONObject obj = new JSONObject();
                         price= (Double) itr.next();
                        obj.put("purchaseprice", price);
                        jArray.put(obj);
                    }

                }

            }
            issuccess=true;
            msg="Data Fetched Successfully ..";
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                if (!StringUtil.isNullOrEmpty(msg)) {
                    jsonObj.put("success", issuccess);
                    jsonObj.put("msg", msg);
                    jsonObj.put("purchaseprice", price);
                    jsonObj.put("productIds", productIds);
                }
            } catch (JSONException ex1) {
                lgr.log(Level.SEVERE, msg, ex1);
            }
        }
        return new ModelAndView(successView, "model", jsonObj.toString());
    }
    
    public ModelAndView deleteStockIssueDetail(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jsonObj = new JSONObject();
       
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SAD_Tx_Get");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        String auditMessage = "";
        HashMap<String, Object> requestParams = new HashMap<>();
        StringBuffer productIds = new StringBuffer();
        try {

            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject objResult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) objResult.getEntityList().get(0);
            String orderIds = request.getParameter("orderId");
            JSONArray jarr = new JSONArray(orderIds);
            for (int j = 0; j < jarr.length(); j++) {
                JSONObject obj = (JSONObject) jarr.get(j);
                String requestID = obj.getString("requestid");
                
                StockRequest sr = stockRequestService.getStockRequestById(requestID);
                jsonObj = stockRequestService.deleteStockIssueDetail(requestID);
                
                auditMessage += sr.getTransactionNo() + " ( Product :" + sr.getProduct().getProductid() + ", Quantity :" + sr.getDeliveredQty() + ")";

                auditMessage = "User " + user.getFullName() + " has deleted Stock Issue (s): " + auditMessage;
                auditTrailObj.insertAuditLog(AuditAction.ISSUE_NOTE_ADDED, auditMessage, request, "");
            }

            txnManager.commit(status);

        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            jsonObj.put("msg", msg);
            jsonObj.put("success", false);
            lgr.log(Level.SEVERE, msg, ex);
        }  
        return new ModelAndView(successView, "model", jsonObj.toString());
    }
}
