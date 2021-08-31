/*
 * To change this templa
 * te, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.view;

import com.itextpdf.text.DocumentException;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.ProductBuild;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.hql.accounting.SequenceFormat;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.exception.NegativeInventoryException;
import com.krawler.inventory.exception.SeqFormatException;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.approval.InspectionCriteriaDetail;
import com.krawler.inventory.model.approval.InspectionDetail;
import com.krawler.inventory.model.approval.InspectionStatus;
import com.krawler.inventory.model.approval.consignment.Consignment;
import com.krawler.inventory.model.approval.consignment.ConsignmentApprovalDetails;
import com.krawler.inventory.model.approval.consignmentservice.ConsignmentService;
import com.krawler.inventory.model.approval.sa.SAApproval;
import com.krawler.inventory.model.approval.sa.SAApprovalService;
import com.krawler.inventory.model.approval.sa.SADetailApproval;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferApproval;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferApprovalService;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferDetailApproval;
import com.krawler.inventory.model.inspection.InspectionForm;
import com.krawler.inventory.model.inspection.InspectionFormDetails;
import com.krawler.inventory.model.inspection.InspectionTemplate;
import com.krawler.inventory.model.ist.*;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.sequence.ModuleConst;
import com.krawler.inventory.model.sequence.SeqFormat;
import com.krawler.inventory.model.sequence.SeqService;
import com.krawler.inventory.model.stock.StockDAO;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.StockMovement;
import com.krawler.inventory.model.stockmovement.StockMovementDetail;
import com.krawler.inventory.model.stockmovement.StockMovementService;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.stockmovement.TransactionType;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.inventory.model.stockout.StockAdjustmentDetail;
import com.krawler.inventory.model.stockout.StockAdjustmentService;
import com.krawler.inventory.model.stockrequest.StockRequest;
import com.krawler.inventory.model.stockrequest.StockRequestDetail;
import com.krawler.inventory.model.stockrequest.StockRequestService;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.spring.accounting.assemblyQA.AssemblyProductApprovalDetails;
import com.krawler.spring.accounting.assemblyQA.AssemblyQAStatus;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignLineItemProp;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import io.jsonwebtoken.lang.Strings;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author Vipin Gupta
 */
public class ApprovalController extends MultiActionController  implements MessageSourceAware{

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private static  DateFormat dfrmt = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Logger lgr = Logger.getLogger(ApprovalController.class.getName());
    private HibernateTransactionManager txnManager;
    private String successView;
    private StockAdjustmentService stockAdjustmentService;
    private InterStoreTransferService interStoreTransferService;
    private SAApprovalService saApprovalService;
    private StockTransferApprovalService stockTransferApprovalService;
    private StockRequestService stockRequestService;
    private AccountingHandlerDAO accountingHandlerDAO;
    private ConsignmentService consignmentService;
    private VelocityEngine velocityEngine;
    private StoreService storeService;
    private LocationService locationService;
    private auditTrailDAO auditTrailObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private StockService stockService;

    private StockMovementService stockMovementService;
    private exportMPXDAOImpl exportDAO;
    private authHandlerDAO authHandlerDAOObj;
    private fieldDataManager fieldDataManagercntrl;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accProductDAO accProductObj;
     private MessageSource messageSource;
    private SeqService seqService;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accJournalEntryDAO journalEntryDAO;
    private accMasterItemsDAO accMasterItemsDAO;
    private StockDAO stockDAOObj;
    
    
    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setFieldDataManagercntrl(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setAccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAO) {
        this.accMasterItemsDAO = accMasterItemsDAO;
    }
    
    public void setKwlCommonTablesDAOObj(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }

    public void setAccProductObj(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setStockAdjustmentService(StockAdjustmentService stockAdjustmentService) {
        this.stockAdjustmentService = stockAdjustmentService;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setInterStoreTransferService(InterStoreTransferService interStoreTransferService) {
        this.interStoreTransferService = interStoreTransferService;
    }

    public void setSaApprovalService(SAApprovalService saApprovalService) {
        this.saApprovalService = saApprovalService;
    }

    public void setstockTransferApprovalService(StockTransferApprovalService stockTransferApprovalService) {
        this.stockTransferApprovalService = stockTransferApprovalService;
    }

    public void setStockRequestService(StockRequestService stockRequestService) {
        this.stockRequestService = stockRequestService;
    }

    public void setConsignmentService(ConsignmentService consignmentService) {
        this.consignmentService = consignmentService;
    }

    public void setvelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }
    
    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }

    public AccountingHandlerDAO getAccountingHandlerDAOobj() {
        return accountingHandlerDAOobj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
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

    public void setExportDAO(exportMPXDAOImpl exportDAO) {
        this.exportDAO = exportDAO;
    }

    public void setSeqService(SeqService seqService) {
        this.seqService = seqService;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setJournalEntryDAO(accJournalEntryDAO journalEntryDAO) {
        this.journalEntryDAO = journalEntryDAO;
    }

    public void setStockDAOObj(StockDAO stockDAOObj) {
        this.stockDAOObj = stockDAOObj;
    }
    
    @Deprecated
    public ModelAndView getStockAdjutmentApprovalList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {


            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);

            String searchString = request.getParameter("ss");

            List<SAApproval> saa = saApprovalService.getStockAdjutmentApprovalList(searchString, paging);
            for (SAApproval sa : saa) {
                JSONObject jObj = new JSONObject();
                jObj.put("id", sa.getId());
                jObj.put("quantity", sa.getQuantity());
                jObj.put("status", sa.getApprovalStatus());
                jObj.put("transactionno", sa.getStockAdjustment() == null ? "" : sa.getStockAdjustment().getTransactionNo());
                jObj.put("productcode", sa.getStockAdjustment() == null ? "" : sa.getStockAdjustment().getProduct().getProductid());
                jObj.put("productid", sa.getStockAdjustment() == null ? "" : sa.getStockAdjustment().getProduct().getID());
                jObj.put("productname", sa.getStockAdjustment() == null ? "" : sa.getStockAdjustment().getProduct().getName());
                jObj.put("storename", sa.getStockAdjustment() == null ? "" : sa.getStockAdjustment().getStore().getFullName());
                jObj.put("storeid", sa.getStockAdjustment() == null ? "" : sa.getStockAdjustment().getStore().getId());
                jObj.put("uomname", sa.getStockAdjustment() == null ? "" : sa.getStockAdjustment().getUom() == null ? "" : sa.getStockAdjustment().getUom().getNameEmptyforNA());
                jArray.put(jObj);
            }
            issuccess = true;
            msg = "Stock Adjustment Approval List  has been fetched successfully";

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

    @Deprecated
    public ModelAndView getStockAdjutmentDetailApprovalList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);

            String searchString = request.getParameter("ss");
            String saId = request.getParameter("saId");
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(SAApproval.class.getName(), saId);
            SAApproval saApproval = (SAApproval) jeresult.getEntityList().get(0);

            List<SADetailApproval> saa = saApprovalService.getStockAdjutmentDetailApprovalList(saApproval, paging);
            for (SADetailApproval sa : saa) {
                JSONObject jObj = new JSONObject();
                jObj.put("id", sa.getId());
                jObj.put("status", sa.getApprovalStatus());
                jObj.put("serialname", sa.getSerialName());
//                jObj.put("sadetailid", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getId());
                jObj.put("batchname", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getBatchName());
//                jObj.put("quantity", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getFinalQuantity());
                jObj.put("quantity", sa.getQuantity());
                jObj.put("productcode", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().getProductid());
//                jObj.put("finalserialnames", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getFinalSerialNames());
                jObj.put("locationname", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getLocation() == null ? "" : sa.getStockAdjustmentDetail().getLocation().getName());
                jObj.put("locationid", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getLocation() == null ? "" : sa.getStockAdjustmentDetail().getLocation().getId());
//                jObj.put("returnedqty", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getReturnQuantity());
                jObj.put("productname", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().getName());
                jObj.put("transactionno", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getTransactionNo());
                jObj.put("storename", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getStore().getFullName());
                jArray.put(jObj);
            }
            issuccess = true;
            msg = "Stock Adjustment Detail Approval List  has been fetched successfully";

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

    public ModelAndView getISTApprovalList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);
            String searchString = request.getParameter("ss");
            String transationModule = request.getParameter("transactionModule");

            List<StockTransferApproval> saa = stockTransferApprovalService.getStockTransferApprovalList(transationModule.equals("0") ? TransactionModule.INTER_STORE_TRANSFER : TransactionModule.STOCK_REQUEST, searchString, paging);
            for (StockTransferApproval sa : saa) {
                JSONObject jObj = new JSONObject();
                jObj.put("id", sa.getId());
                jObj.put("status", sa.getApprovalStatus());
                jObj.put("transactionmodule", sa.getTransactionModule());
                TransactionModule module = sa.getTransactionModule();
                if (module == TransactionModule.STOCK_REQUEST) {
                    String stockTransferId = sa.getStockTransferId();
                    StockRequest SR = stockRequestService.getStockRequestById(stockTransferId);
                    if (SR != null) {
                        jObj.put("transactionno", SR.getTransactionNo());
                        jObj.put("quantity", SR.getOrderedQty() - SR.getDeliveredQty());
                        jObj.put("productcode", SR.getProduct().getProductid());
                        jObj.put("productid", SR.getProduct().getID());
                        jObj.put("productname", SR.getProduct().getName());
                        jObj.put("fromstorename", SR.getFromStore().getFullName());
                        jObj.put("fromstoreid", SR.getFromStore().getId());
                        jObj.put("tostorename", SR.getToStore().getFullName());
                        jObj.put("tostoreid", SR.getToStore().getId());
                        jObj.put("uomname", SR.getUom() == null ? "" : SR.getUom().getNameEmptyforNA());
                        jArray.put(jObj);
                    }
                } else if (module == TransactionModule.INTER_STORE_TRANSFER) {
                    String stockTransferId = sa.getStockTransferId();
                    InterStoreTransferRequest ISR = interStoreTransferService.getInterStoreTransferById(stockTransferId);
                    if (ISR != null) {
                        jObj.put("transactionno", ISR.getTransactionNo());
                        jObj.put("quantity", ISR.getOrderedQty() - ISR.getAcceptedQty());
                        jObj.put("productcode", ISR.getProduct().getProductid());
                        jObj.put("productid", ISR.getProduct().getID());
                        jObj.put("productname", ISR.getProduct().getName());
                        jObj.put("fromstorename", ISR.getFromStore().getFullName());
                        jObj.put("fromstoreid", ISR.getFromStore().getId());
                        jObj.put("tostorename", ISR.getToStore().getFullName());
                        jObj.put("tostoreid", ISR.getToStore().getId());
                        jObj.put("uomname", ISR.getUom() == null ? "" : ISR.getUom().getNameEmptyforNA());
                        jArray.put(jObj);
                    }
                }

            }
            issuccess = true;
            msg = "IST Return Approval List  has been fetched successfully";

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

    public ModelAndView getISTDetailApprovalList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);

            String searchString = request.getParameter("ss");
            String ISTApprovalId = request.getParameter("isapprovalId");
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(StockTransferApproval.class.getName(), ISTApprovalId);
            StockTransferApproval stApproval = (StockTransferApproval) jeresult.getEntityList().get(0);

            List<StockTransferDetailApproval> stda = stockTransferApprovalService.getStockTransferDetailApprovalList(stApproval, paging);
            for (StockTransferDetailApproval sa : stda) {
                JSONObject jObj = new JSONObject();
                jObj.put("id", sa.getId());
                jObj.put("status", sa.getApprovalStatus());
                jObj.put("serialname", sa.getSerialName());
                String stockTransferId = sa.getStockTransferDetailId();
                TransactionModule module = stApproval.getTransactionModule();
                if (module == TransactionModule.INTER_STORE_TRANSFER) {
                    ISTDetail ISD = interStoreTransferService.getISTDetailById(stockTransferId);
                    if (ISD != null) {
                        jObj.put("fromlocationname", ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getName());
                        jObj.put("fromlocationid", ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getId());
                        jObj.put("tolocationname", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getName());
                        jObj.put("tolocationid", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getId());
                        jObj.put("batchname", ISD.getBatchName());
                        jObj.put("quantity", ISD.getReturnQuantity());
                        jObj.put("issuedserialnames", ISD.getIssuedSerialNames());
                        jObj.put("serialname", ISD.getIssuedSerialNames());
                        jObj.put("returnserialnames", ISD.getReturnSerialNames());
                        jObj.put("productname", ISD.getIstRequest().getProduct().getName());
                        jObj.put("transactionno", ISD.getIstRequest().getTransactionNo());
                        jArray.put(jObj);
                    }
                } else if (module == TransactionModule.STOCK_REQUEST) {
                    StockRequestDetail SRD = stockRequestService.getStockRequestDetail(stockTransferId);
                    if (SRD != null) {
                        jObj.put("fromlocationname", SRD.getIssuedLocation() == null ? "" : SRD.getIssuedLocation().getName());
                        jObj.put("fromlocationid", SRD.getIssuedLocation() == null ? "" : SRD.getIssuedLocation().getId());
                        jObj.put("tolocationname", SRD.getDeliveredLocation() == null ? "" : SRD.getDeliveredLocation().getName());
                        jObj.put("tolocationid", SRD.getDeliveredLocation() == null ? "" : SRD.getDeliveredLocation().getId());
                        jObj.put("batchname", SRD.getBatchName());
                        jObj.put("quantity", SRD.getReturnQuantity());
                        jObj.put("issuedserialnames", SRD.getIssuedSerialNames());
                        jObj.put("returnserialnames", SRD.getReturnSerialNames());
                        jObj.put("productname", SRD.getStockRequest().getProduct().getName());
                        jObj.put("transactionno", SRD.getStockRequest().getTransactionNo());
                        jArray.put(jObj);
                    }

                }
            }
            issuccess = true;
            msg = "IST Return Detail Approval List  has been fetched successfully";

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

    @Deprecated
    public ModelAndView getInspectionData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject Jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        TransactionStatus status = null;
        try {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("STR_Tx");
            TransactionModule module1 = null;
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            status = txnManager.getTransaction(def);
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            String ISTApprovalId = request.getParameter("isapprovalId");
            String status1 = request.getParameter("status");
            if ("PENDING".equals(status1)) {
//                String inspe[] = {"DISTAL END", "A-RUBBER", "INSERTION TUBE", "FORCEPS CHANNEL", "CONTROL BODY",
//                    "CONTROL KNOB", "SUCTION CYLINDER", "UNIVERSAL CORD", "EL CONNECTOR", "LG ROD LENS"};
//                for (int i = 0; i < inspe.length; i++) {
//                    JSONObject jObj = new JSONObject();
//                    jObj.put("inspection", inspe[i]);
//
//                    jArray.put(jObj);
//                }
            } else {
                String saDetailApprovalId = request.getParameter("saDetailApprovalId");
                String module = request.getParameter("module");
                if ("stockout".equals(module)) {
                    SADetailApproval sada = saApprovalService.getSADetailApproval(saDetailApprovalId);
                    InspectionDetail inspectionDtl = sada.getInspectionDetail();
                    String referenceNo = inspectionDtl.getReferenceNo();
                    String customerPON = inspectionDtl.getCustomerPONo();
                    String hospital = inspectionDtl.getHospital();
                    String department = inspectionDtl.getDepartment();
                    String modelname = inspectionDtl.getModelname();
                    Set<InspectionCriteriaDetail> icdList = sada.getInspectionDetail().getInspectionCriteriaDetailSet();
                    for (InspectionCriteriaDetail idc : icdList) {
                        JSONObject jObj = new JSONObject();
                        jObj.put("inspection", idc.getInspectionArea());
                        String sts = idc.getAcceptable() == null ? "" : idc.getAcceptable() == 1 ? "OK" : "NG";
                        jObj.put("status", sts);
                        jObj.put("faults", idc.getFaults());
                        jObj.put("modelname", sada.getStockAdjustmentDetail().getStockAdjustment().getProduct().getProductid());
                        jObj.put("refno", referenceNo);
                        jObj.put("pono", customerPON);
                        jObj.put("description", sada.getStockAdjustmentDetail().getStockAdjustment().getProduct().getName());
                        jObj.put("hospital", hospital);
                        jObj.put("inspector", user.getFirstName());
                        jObj.put("serialno", sada.getStockAdjustmentDetail().getStockAdjustment().getTransactionNo());
                        jObj.put("department", department);
                        jArray.put(jObj);
                    }
                } else if ("interstore".equals(module)) {
                    KwlReturnObject jeresult1 = accountingHandlerDAO.getObject(StockTransferApproval.class.getName(), ISTApprovalId);
                    StockTransferApproval stApproval = (StockTransferApproval) jeresult1.getEntityList().get(0);
                    module1 = stApproval.getTransactionModule();
                    if (module1 == TransactionModule.INTER_STORE_TRANSFER || module1 == TransactionModule.STOCK_REQUEST) {
                        StockTransferDetailApproval stockTransferDetail = stockTransferApprovalService.getStockTransferDetailApproval(saDetailApprovalId);
                        InspectionDetail inspectionDtl = stockTransferDetail.getInspectionDetail();
                        String referenceNo = inspectionDtl.getReferenceNo();
                        String customerPON = inspectionDtl.getCustomerPONo();
                        String hospital = inspectionDtl.getHospital();
                        String department = inspectionDtl.getDepartment();
                        String modelname = inspectionDtl.getModelname();
                        Set<InspectionCriteriaDetail> icdList = stockTransferDetail.getInspectionDetail().getInspectionCriteriaDetailSet();
                        for (InspectionCriteriaDetail idc : icdList) {
                            JSONObject jObj = new JSONObject();
                            jObj.put("inspection", idc.getInspectionArea());
                            String sts = idc.getAcceptable() == null ? "" : idc.getAcceptable() == 1 ? "OK" : "NG";
                            jObj.put("status", sts);
                            jObj.put("faults", idc.getFaults());
                            jObj.put("modelname", modelname);
                            jObj.put("refno", referenceNo);
                            jObj.put("pono", customerPON);
//                        jObj.put("description", stockTransferDetail.getStockTransferApproval().getTransactionModu);
                            jObj.put("hospital", hospital);
                            jObj.put("inspector", user.getFirstName());
//                        jObj.put("serialno", sada.getStockAdjustmentDetail().getStockAdjustment().getTransactionNo());
                            jObj.put("department", department);
                            jArray.put(jObj);
                        }
                    }
                }
            }
            txnManager.commit(status);
            issuccess = true;

        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                Jobj.put("success", issuccess);
                Jobj.put("msg", msg);
                Jobj.put("data", jArray);
                if (paging != null) {
                    Jobj.put("count", paging.getTotalRecord());
                } else {
                    Jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", Jobj.toString());
    }

    @Deprecated
    public ModelAndView saveInspectionData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SA_Tx_Save_Inspection");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);
            String ISTApprovalId = request.getParameter("isapprovalId");

            String records = request.getParameter("jsondata");
            String extraDta = request.getParameter("extraFields");
            String module = request.getParameter("module");
            String department = "";
            String hospital = "";
            String inspectionArea = "";
            String faults = "";
            String saDetailApprovalId = request.getParameter("saDetailApprovalId");
            String operation = request.getParameter("operation");
            String pono = request.getParameter("pono");
            String date = request.getParameter("date");
            String modelname = request.getParameter("modelname");
            String description = request.getParameter("description");
            String refno = request.getParameter("refno");
            String serialno = request.getParameter("serialno");
            if (extraDta != null) {
                JSONArray jArr = new JSONArray(extraDta);
                for (int j = 0; j < jArr.length(); j++) {
                    JSONObject jObj = jArr.optJSONObject(j);
                    department = jObj.optString("department");
                    hospital = jObj.optString("hospitalname");
                }
            }
            InspectionDetail inspDtl = new InspectionDetail();
            inspDtl.setCustomerPONo(pono);
            inspDtl.setDepartment(department);
            inspDtl.setHospital(hospital);
            inspDtl.setReferenceNo(refno);
            inspDtl.setModelname(modelname);
            inspDtl.setCompany(company);

            Set<InspectionCriteriaDetail> icdSet = new HashSet<InspectionCriteriaDetail>();
            if (records != null) {
                JSONArray jArr = new JSONArray(records);
                for (int j = 0; j < jArr.length(); j++) {
                    InspectionCriteriaDetail criteriaDetl = new InspectionCriteriaDetail();
                    JSONObject jObj = jArr.optJSONObject(j);
                    inspectionArea = jObj.optString("inspection");
                    if ("OK".equals(jObj.optString("status"))) {
                        criteriaDetl.setAcceptable(InspectionStatus.ACCEPTABLE.getStatusCode());
                    } else if ("NG".equals(jObj.optString("status"))) {
                        criteriaDetl.setAcceptable(InspectionStatus.NOT_ACCEPTABLE.getStatusCode());
                    } else {
                        criteriaDetl.setAcceptable(null);
                    }
                    faults = jObj.optString("faults");
                    criteriaDetl.setFaults(faults);
                    criteriaDetl.setInspectionArea(inspectionArea);
                    criteriaDetl.setInspectionDetail(inspDtl);
                    icdSet.add(criteriaDetl);
                }
            }

            inspDtl.setInspectionCriteriaDetailSet(icdSet);

            if ("stockout".equals(module)) {
                SADetailApproval sada = saApprovalService.getSADetailApproval(saDetailApprovalId);
                if (operation.equals("Approve") && "stockout".equals(module)) {
                    saApprovalService.approveStockAdjustmentDetail(user, sada, inspDtl, 0, false);
                    operation = "Approved";
                } else if (operation.equals("Reject") && "stockout".equals(module)) {
                    saApprovalService.rejectStockAdjustmentDetail(user, sada, inspDtl, 0, false);
                    operation = "Rejected";
                }
                msg = "QA has " + operation + " Stock Adjustment data successfully";
            } else if ("interstore".equals(module)) {
                KwlReturnObject jeresult1 = accountingHandlerDAO.getObject(StockTransferApproval.class.getName(), ISTApprovalId);
                StockTransferApproval stApproval = (StockTransferApproval) jeresult1.getEntityList().get(0);
                TransactionModule module1 = stApproval.getTransactionModule();
                if (module1 == TransactionModule.INTER_STORE_TRANSFER) {
                    operation = "Approved";
                    StockTransferDetailApproval stockTransferDTL = stockTransferApprovalService.getStockTransferDetailApproval(saDetailApprovalId);
                    if (operation.equals("Approve")) {
                        stockTransferApprovalService.approveStockTransferDetail(user, stockTransferDTL, inspDtl, false, 0);
                    } else if (operation.equals("Reject")) {
                        operation = "Rejected";
                        stockTransferApprovalService.rejectStockTransferDetail(user, stockTransferDTL, inspDtl, false, 0);
                    }
                    msg = "QA has " + operation + " InterStore Transfer data successfully";
                } else if (module1 == TransactionModule.STOCK_REQUEST) {
                    StockTransferDetailApproval stockTransferDTL = stockTransferApprovalService.getStockTransferDetailApproval(saDetailApprovalId);
                    if (operation.equals("Approve")) {
                        stockTransferApprovalService.approveStockTransferDetail(user, stockTransferDTL, inspDtl, false, 0);
                    } else if (operation.equals("Reject")) {
                        operation = "Rejected";
                        stockTransferApprovalService.rejectStockTransferDetail(user, stockTransferDTL, inspDtl, false, 0);
                    }
                    msg = "QA has " + operation + " Stock Request data successfully";
                }
            }
            issuccess = true;

            txnManager.commit(status);
        } catch (Exception ex) {
            issuccess = false;
            msg = ex.getMessage();
            txnManager.rollback(status);
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

    @Deprecated
    public ModelAndView getConsignmentApprovalList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);

            String searchString = request.getParameter("ss");

            List<Consignment> consign = consignmentService.getConsingmentList(searchString, paging);

            for (Consignment conmnt : consign) {
                JSONObject jObj = new JSONObject();
                jObj.put("id", conmnt.getId());
                jObj.put("quantity", conmnt.getReturnQuantity());
                jObj.put("status", conmnt.getApprovalStatus());
                jObj.put("transactionno", conmnt.getTransactionNo());
                jObj.put("productcode", conmnt.getProduct().getProductid());
                jObj.put("productid", conmnt.getProduct().getID());
                jObj.put("productname", conmnt.getProduct().getName());
                jObj.put("storename", conmnt.getStore().getFullName());
                jObj.put("customer", conmnt.getCustomer().getName());
                jObj.put("storeid", conmnt.getStore().getId());
                jObj.put("uomname", conmnt.getUom() == null ? "" : conmnt.getUom().getNameEmptyforNA());
                jArray.put(jObj);
            }
            issuccess = true;
            msg = "Stock Adjustment Approval List  has been fetched successfully";

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

    @Deprecated
    public ModelAndView getConsignmentApprovalDetailList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);

            String searchString = request.getParameter("ss");
            String saId = request.getParameter("saId");

            Consignment consign = consignmentService.getConsingmentById(saId);
            Set<ConsignmentApprovalDetails> consList = consign.getConsignmentApprovalDetails();

            for (ConsignmentApprovalDetails conmnt : consList) {
                JSONObject jObj = new JSONObject();
                jObj.put("id", conmnt.getId());
                jObj.put("transactionno", conmnt.getConsignment().getTransactionNo());
                jObj.put("customer", conmnt.getConsignment().getCustomer() == null ? "" : conmnt.getConsignment().getCustomer().getName());
                jObj.put("quantity", conmnt.getQuantity());
                jObj.put("status", conmnt.getApprovalStatus());
                jObj.put("productcode", conmnt.getConsignment().getProduct().getProductid());
                jObj.put("productid", conmnt.getConsignment().getProduct().getID());
                jObj.put("productname", conmnt.getConsignment().getProduct().getName());
                jObj.put("storename", conmnt.getConsignment().getStore().getFullName());
                jObj.put("batchname", conmnt.getBatchName());
                jObj.put("customer", conmnt.getConsignment().getCustomer().getName());
                jObj.put("serialname", conmnt.getSerialName());
                jObj.put("storeid", conmnt.getConsignment().getStore().getId());
                jObj.put("uomname", conmnt.getConsignment().getUom() == null ? "" : conmnt.getConsignment().getUom().getNameEmptyforNA());
                jObj.put("locationname", conmnt.getLocation() == null ? "" : conmnt.getLocation().getName());
                jObj.put("leadtime", conmnt.getConsignment().getProduct().getLeadTimeInDays());

                int attachmentCount = saApprovalService.getAttachmentCount(conmnt.getConsignment().getCompany(), conmnt.getId());
                jObj.put("attachment", attachmentCount);
                jArray.put(jObj);
            }
            issuccess = true;
            msg = "Stock Adjustment Approval List  has been fetched successfully";

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
    
    public ModelAndView saveInspectionData1(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SA_Tx_Save_Inspection");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        StringBuilder productIds = new StringBuilder();
        String newistid = "";
        String auditMessage = "";
        try {
            /*
            * Added synchronized block to avoid multiple approval for same transaction. 
            */
            synchronized (this) {
            JSONArray jArr = new JSONArray(request.getParameter("jsondata"));
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            jeresult = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) jeresult.getEntityList().get(0);
            jeresult = accountingHandlerDAO.getObject(CompanyAccountPreferences.class.getName(), companyId);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) jeresult.getEntityList().get(0);
            jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);
            String ISTApprovalId = request.getParameter("saDetailApprovalId");
            String[] detailIds = ISTApprovalId != null ? ISTApprovalId.split(",") : (new String[0]);
            String saDetailApprovalId = request.getParameter("moduleApprovalId");
            String remark = request.getParameter("remark");
            String operation = request.getParameter("operation");
            String transactionrepairstore = request.getParameter("repairStoreId"); //ERM-691
            String transactionrepairlocation = request.getParameter("repairLocationId"); //ERM-691
            String operationMsg = "";
            String modulType = request.getParameter("moduleid");
            String transactionNo = request.getParameter("transactionno");
            String reusableCountStr = request.getParameter("reusableCount");
            String extraEmailIds = request.getParameter("extraEmail");
            Store qaStore = stockService.getQaStore(company);
            Store repairStore = null;
            Location repairLocation = null;
            /**
             * ERM 691 Allowing dynamic changing of repair store when GRN is rejected from QA store.
             */
            if (!StringUtil.isNullOrEmpty(transactionrepairstore) && "goodsreceipt".equalsIgnoreCase(modulType)) {
                repairStore = storeService.getStoreById(transactionrepairstore);
                if (!StringUtil.isNullOrEmpty(transactionrepairlocation)) {
                    repairLocation = locationService.getLocation(transactionrepairlocation);
                }
                
            } else {
                repairStore = stockService.getRepairStore(company);
            }
            Store packingStore = stockService.getPackingstore(company);
            boolean assignStockToPendingConsignmentRequest = false;

            List rejectionDetailMapList = new ArrayList();
            Map rejectionDetailMap = null;
            List approvedDetailMapList = new ArrayList();
            Map approvedDetailMap = null;

            double reusableCount = 0;
            if (!StringUtil.isNullOrEmpty(reusableCountStr)) {
                reusableCount = Double.parseDouble(reusableCountStr);
            }
            double operationQty = 0;
            String operationSerials = "";
              if ("consignment".equals(modulType)) {
                List<ConsignmentApprovalDetails> approvedRejectedConRecords = new ArrayList();
                Set salesPersonEmailIdSet=new HashSet();
//                for (String dtlId : detailIds)
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject job = jArr.getJSONObject(i);
                    String dtlId = job.getString("recordid");
                    double qty = job.has("quantity") ? job.getDouble("quantity") : 0;
                    ConsignmentApprovalDetails consignmentApDetail = consignmentService.getConsingmentDetailsById(dtlId);
                    approvedRejectedConRecords.add(consignmentApDetail);
                    Consignment consignment = consignmentApDetail.getConsignment();
                    consignmentApDetail.setRemark(remark);
                    if (consignmentApDetail.getApprovalStatus() == ApprovalStatus.APPROVED || consignmentApDetail.getApprovalStatus() == ApprovalStatus.REJECTED) {
                        throw new InventoryException("This item is already processed");
                    }
                    if (reusableCount > 0) {
                        consignment.getProduct().setTotalIssueCount(consignment.getProduct().getTotalIssueCount() + reusableCount);
                    }
                    if (operation.equals("Approve")) {
                        consignmentService.approveConsignmentDetail(user, consignmentApDetail, null, company, qty, false, qaStore, repairStore);
                        approvedDetailMap = consignmentService.rejectedApprovedItemsDetail(consignmentApDetail);
                        approvedDetailMapList.add(approvedDetailMap);
                        operationMsg = "approved";
                        assignStockToPendingConsignmentRequest = true;
                    } else if (operation.equals("Reject")) {
                        consignmentApDetail.setRepairStatus(ApprovalStatus.REPAIRPENDING);
                        consignmentService.rejectConsignmentDetail(user, consignmentApDetail, null, company, qty, false, qaStore, repairStore);
                        rejectionDetailMap = consignmentService.rejectedApprovedItemsDetail(consignmentApDetail);
                        rejectionDetailMapList.add(rejectionDetailMap);
                        operationMsg = "rejected";
                    }
                    operationQty += consignmentApDetail.getQuantity();
                    if (consignment.getProduct().isIsSerialForProduct()) {
                        if (!StringUtil.isNullOrEmpty(operationSerials)) {
                            operationSerials += ", ";
                        }
                        operationSerials += consignmentApDetail.getSerialName();
                    }

                    if (StringUtil.isNullOrEmpty(auditMessage)) {// only one time
                        auditMessage += "Consigment Request: " + consignment.getTransactionNo() + ", Product: " + consignment.getProduct().getProductid() + ", Store: " + consignment.getStore().getAbbreviation();
                    }
                    
                    String salesPersonEmail=consignmentService.getSalesPersonEmailIdBySRDetailId(consignment.getDocumentid(),companyId);
                    if(!StringUtil.isNullOrEmpty(salesPersonEmail)){
                        salesPersonEmailIdSet.add(salesPersonEmail);
                    }
                }
                String salesPersonEmailIds= StringUtils.join(salesPersonEmailIdSet, ",");
                
                consignmentService.creatStockmovementForConsignmentQAApproval(company, approvedRejectedConRecords, qaStore, repairStore);
                if (operation.equals("Approve")) {
                    sendUserProductQAApprovalRejectionMail(company,approvedDetailMapList,extraEmailIds,salesPersonEmailIds,user,transactionNo,true,"Consignment Return");
                } else if (operation.equals("Reject")) {
                    sendUserProductQAApprovalRejectionMail(company, rejectionDetailMapList, extraEmailIds,salesPersonEmailIds, user, transactionNo,false,"Consignment Return"); //user is logged in user
                }
                auditMessage += ", Quantity: " + operationQty + (!StringUtil.isNullOrEmpty(operationSerials) ? ", Serials: (" + operationSerials + ")" : "");

            }else if (Constants.BUILD_ASSEMBLY_QA_APPROVAL.equals(modulType)) {
                /*
                This function is called to approve/reject job work assembly product quantity from qa approval tab
                */
                    approveAssemblyProductPendingForQa(request);
                  if (operation.equals("Approve")) {
                      operationMsg = "approved";
                  } else {
                      operationMsg = "rejected";
                  }

            } else if ("stockrequest".equals(modulType) || "stocktransfer".equals(modulType)) {

                KwlReturnObject jeresult1 = accountingHandlerDAO.getObject(StockTransferApproval.class.getName(), saDetailApprovalId);
                StockTransferApproval stApproval = (StockTransferApproval) jeresult1.getEntityList().get(0);
                TransactionModule module1 = stApproval.getTransactionModule();
                if (module1 == TransactionModule.INTER_STORE_TRANSFER) {
                    List<StockTransferDetailApproval> approvedRejectedInstRecords = new ArrayList();
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject job = jArr.getJSONObject(i);
                        String dtlId = job.getString("recordid");
                        double qty = job.has("quantity") ? job.getDouble("quantity") : 0;
                        StockTransferDetailApproval stockTransferDTL = stockTransferApprovalService.getStockTransferDetailApproval(dtlId);
                        stockTransferDTL.setRemark(remark);
                        approvedRejectedInstRecords.add(stockTransferDTL);
                        ISTDetail ISD = interStoreTransferService.getISTDetailById(stockTransferDTL.getStockTransferDetailId());
                        InterStoreTransferRequest istr = ISD.getIstRequest();
                        if (stockTransferDTL.getApprovalStatus() == ApprovalStatus.APPROVED || stockTransferDTL.getApprovalStatus() == ApprovalStatus.REJECTED) {
                            throw new InventoryException("This item is already processed");
                        }
                        if (reusableCount > 0) {
                            istr.getProduct().setTotalIssueCount(istr.getProduct().getTotalIssueCount() + reusableCount);
                        }
                        if (operation.equals("Approve")) {
                            operationMsg = "approved";
                            stockTransferApprovalService.approveStockTransferDetail(user, stockTransferDTL, null, false, qty);
                            approvedDetailMap = stockTransferApprovalService.rejectedApprovedItemsDetail(stockTransferDTL);
                            approvedDetailMapList.add(approvedDetailMap);
                            assignStockToPendingConsignmentRequest = true;
                            //decrease Qastore inventory
//                            stockService.addStockMovementForQa(ISD, qaStore, TransactionType.OUT, ISD.getBatchName(), stockTransferDTL.getSerialName(),
//                                    qty, TransactionModule.INTER_STORE_TRANSFER, company, qaStore.getDefaultLocation());
//                            stockService.decreaseInventory(ISD.getIstRequest().getProduct(), qaStore, qaStore.getDefaultLocation(), ISD.getBatchName(),
//                                    stockTransferDTL.getSerialName(), qty);

                        } else if (operation.equals("Reject")) {
                            operationMsg = "rejected";
                            stockTransferDTL.setRepairStatus(ApprovalStatus.REPAIRPENDING);
                            stockTransferApprovalService.rejectStockTransferDetail(user, stockTransferDTL, null, false, qty);
                            rejectionDetailMap = stockTransferApprovalService.rejectedApprovedItemsDetail(stockTransferDTL);
                            rejectionDetailMapList.add(rejectionDetailMap);

                            //decrease Qastore and Increase Repair Store
//                            stockService.addStockMovementForQa(ISD, qaStore, TransactionType.OUT, ISD.getBatchName(), stockTransferDTL.getSerialName(),
//                                    qty, TransactionModule.INTER_STORE_TRANSFER, company, qaStore.getDefaultLocation());
//                            stockService.decreaseInventory(ISD.getIstRequest().getProduct(), qaStore, qaStore.getDefaultLocation(), ISD.getBatchName(),
//                                    stockTransferDTL.getSerialName(), qty);
//                            
//                            stockService.addStockMovementForQa(ISD, repairStore, TransactionType.IN, ISD.getBatchName(), stockTransferDTL.getSerialName(),
//                                    qty, TransactionModule.INTER_STORE_TRANSFER, company, repairStore.getDefaultLocation());
//                            stockService.increaseInventory(ISD.getIstRequest().getProduct(), repairStore, repairStore.getDefaultLocation(), ISD.getBatchName(),
//                                    stockTransferDTL.getSerialName(), qty);

                        }
//                        Store originalStore = ISD.getIstRequest().getToStore();
//                        if (originalStore != null && operation.equals("Approve")) {
//                            stockService.addStockMovementForQa(ISD, originalStore, TransactionType.IN, ISD.getBatchName(), stockTransferDTL.getSerialName(), qty, TransactionModule.INTER_STORE_TRANSFER, company, ISD.getIssuedLocation());
//                            stockService.increaseInventory(ISD.getIstRequest().getProduct(), originalStore, ISD.getIssuedLocation(), ISD.getBatchName(), stockTransferDTL.getSerialName(), qty);
//                            stockMovementService.stockMovementInERP(true, ISD.getIstRequest().getProduct(), originalStore, ISD.getIssuedLocation(), ISD.getBatchName(), stockTransferDTL.getSerialName(), qty);
//                        }

                        operationQty += stockTransferDTL.getQuantity();
                        if (istr.getProduct().isIsSerialForProduct()) {
                            if (!StringUtil.isNullOrEmpty(operationSerials)) {
                                operationSerials += ", ";
                            }
                            operationSerials += stockTransferDTL.getSerialName();
                        }

                        if (StringUtil.isNullOrEmpty(auditMessage)) {// only one time
                            auditMessage += "Inter Store transfer Request: " + istr.getTransactionNo() + ", Product: " + istr.getProduct().getProductid() + ", Store: " + istr.getToStore().getAbbreviation();
                        }

                    }
                    stockTransferApprovalService.createStockMovementForQAApprovalIntr(company, approvedRejectedInstRecords, qaStore, repairStore);
                    if (operation.equals("Approve")) {
                        sendUserProductQAApprovalRejectionMail(company, approvedDetailMapList, extraEmailIds, "", user, transactionNo,true,"Inter Store Transafer Return");
                    } else if (operation.equals("Reject")) {
                        sendUserProductQAApprovalRejectionMail(company, rejectionDetailMapList, extraEmailIds, "", user, transactionNo,false,"Inter Store Transafer Return"); //user is logged in user
                    }
                    msg = "QA has " + operationMsg + " InterStore Transfer data successfully";

                    auditMessage += ", Quantity: " + operationQty + (!StringUtil.isNullOrEmpty(operationSerials) ? ", Serials: (" + operationSerials + ")" : "");

                } else if (module1 == TransactionModule.STOCK_REQUEST) {
//                    for (String dtlId : detailIds) {
                    List<StockTransferDetailApproval> approvedRejectedSTRRecords = new ArrayList();
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject job = jArr.getJSONObject(i);
                        String dtlId = job.getString("recordid");
                        double qty = job.has("quantity") ? job.getDouble("quantity") : 0;
                        StockTransferDetailApproval stockTransferDTL = stockTransferApprovalService.getStockTransferDetailApproval(dtlId);
                        stockTransferDTL.setRemark(remark);
                        approvedRejectedSTRRecords.add(stockTransferDTL);
                        StockRequestDetail SRD = stockRequestService.getStockRequestDetail(stockTransferDTL.getStockTransferDetailId());
                        StockRequest sr = SRD.getStockRequest();
                        if (stockTransferDTL.getApprovalStatus() == ApprovalStatus.APPROVED || stockTransferDTL.getApprovalStatus() == ApprovalStatus.REJECTED) {
                            throw new InventoryException("This item is already processed");
                        }
                        sr.getProduct().setTotalIssueCount(sr.getProduct().getTotalIssueCount() + reusableCount);
                        if (operation.equals("Approve")) {
                            operationMsg = "approved";
                            stockTransferApprovalService.approveStockTransferDetail(user, stockTransferDTL, null, false, qty);
                            approvedDetailMap = stockTransferApprovalService.rejectedApprovedItemsDetail(stockTransferDTL);
                            approvedDetailMapList.add(approvedDetailMap);
                             assignStockToPendingConsignmentRequest = true;
                        } else if (operation.equals("Reject")) {
                            operationMsg = "rejected";
                            stockTransferApprovalService.rejectStockTransferDetail(user, stockTransferDTL, null, false, qty);
                            stockTransferDTL.setRepairStatus(ApprovalStatus.REPAIRPENDING);
//                            stockService.addStockMovementForSRDQa(SRD, repairStore, TransactionType.IN, SRD.getBatchName(), stockTransferDTL.getSerialName(), qty,
//                                    TransactionModule.INTER_STORE_TRANSFER, company, repairStore.getDefaultLocation());
//                            stockService.increaseInventory(SRD.getStockRequest().getProduct(), repairStore, repairStore.getDefaultLocation(), SRD.getBatchName(), stockTransferDTL.getSerialName(),
//                                    qty);

                            rejectionDetailMap = stockTransferApprovalService.rejectedApprovedItemsDetail(stockTransferDTL);
                            rejectionDetailMapList.add(rejectionDetailMap);
                            
                            /**ERP-37353
                             * Added productIds to update Stock Req price based on the valuation done in AOP advisor.
                             */
                            if (productIds.indexOf(sr.getProduct().getID()) == -1) {
                                productIds.append(sr.getProduct().getID()).append(",");
                            }
                        }
//                        stockService.addStockMovementForSRDQa(SRD, qaStore, TransactionType.OUT, SRD.getBatchName(), stockTransferDTL.getSerialName(), qty,
//                                TransactionModule.INTER_STORE_TRANSFER, company, qaStore.getDefaultLocation());
//                        stockService.decreaseInventory(SRD.getStockRequest().getProduct(), qaStore, qaStore.getDefaultLocation(), SRD.getBatchName(), stockTransferDTL.getSerialName(),
//                                qty);
//                        Store originalStore = SRD.getStockRequest().getFromStore();
//                        if (originalStore != null && operation.equals("Approve")) {
//                            stockService.addStockMovementForSRDQa(SRD, originalStore, TransactionType.IN, SRD.getBatchName(), stockTransferDTL.getSerialName(), qty,
//                                    TransactionModule.INTER_STORE_TRANSFER, company, SRD.getDeliveredLocation());
//                            stockService.increaseInventory(SRD.getStockRequest().getProduct(), originalStore, SRD.getDeliveredLocation(), SRD.getBatchName(), stockTransferDTL.getSerialName(),
//                                    qty);
//                            stockMovementService.stockMovementInERP(true, SRD.getStockRequest().getProduct(), originalStore, SRD.getDeliveredLocation(), SRD.getBatchName(), stockTransferDTL.getSerialName(), qty);
//                        }
                        operationQty += stockTransferDTL.getQuantity();
                        if (sr.getProduct().isIsSerialForProduct()) {
                            if (!StringUtil.isNullOrEmpty(operationSerials)) {
                                operationSerials += ", ";
                            }
                            operationSerials += stockTransferDTL.getSerialName();
                        }

                        if (StringUtil.isNullOrEmpty(auditMessage)) {// only one time
                            auditMessage += "Stock Request: " + sr.getTransactionNo() + ", Product: " + sr.getProduct().getProductid() + ", Store: " + sr.getFromStore().getAbbreviation();
                        }
//                        stockTransferApprovalService.createStockMovementForQAApproval(company, approvedRejectedSTRRecords, qaStore, repairStore);
                    }
                    stockTransferApprovalService.createStockMovementForQAApproval(company, approvedRejectedSTRRecords, qaStore, repairStore);
                    if (operation.equals("Approve")) {
                        sendUserProductQAApprovalRejectionMail(company, approvedDetailMapList, extraEmailIds, "", user, transactionNo,true,"Request Return");
                    } else if (operation.equals("Reject")) {
                        sendUserProductQAApprovalRejectionMail(company, rejectionDetailMapList, extraEmailIds, "", user, transactionNo,false,"Request Return"); //user is logged in user
                    }
                    msg = "QA has " + operationMsg + " Stock Request data successfully";

                    auditMessage += ", Quantity: " + operationQty + (!StringUtil.isNullOrEmpty(operationSerials) ? ", Serials: (" + operationSerials + ")" : "");

                }

            } else if ("stockout".equals(modulType)) {
//                for (String dtlId : detailIds) {
                List<SADetailApproval> approvedRejectedRecords = new ArrayList();
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject job = jArr.getJSONObject(i);
                    String dtlId = job.getString("recordid");
                    double qty = job.has("quantity") ? job.getDouble("quantity") : 0;
                    SADetailApproval sada = saApprovalService.getSADetailApproval(dtlId);
                    approvedRejectedRecords.add(sada);
                    StockAdjustment sa = sada.getSaApproval().getStockAdjustment();
                    sa.getProduct().setTotalIssueCount(sa.getProduct().getTotalIssueCount() + reusableCount);
                    sada.setRemark(remark);
                    if (sada.getApprovalStatus() == ApprovalStatus.APPROVED || sada.getApprovalStatus() == ApprovalStatus.REJECTED) {
                        throw new InventoryException("This item is already processed");
                    }
                    if (operation.equals("Approve")) {

                        saApprovalService.approveStockAdjustmentDetail(user, sada, null, qty, false);
                        approvedDetailMap = saApprovalService.rejectedApprovedSAItemsDetail(sada);
                        approvedDetailMapList.add(approvedDetailMap);
                        operationMsg = "approved";
                        assignStockToPendingConsignmentRequest = true;
                    } else if (operation.equals("Reject")) {
                        sada.setRepairStatus(ApprovalStatus.REPAIRPENDING);
                        saApprovalService.rejectStockAdjustmentDetail(user, sada, null, qty, false);
                        rejectionDetailMap = saApprovalService.rejectedApprovedSAItemsDetail(sada);
                        rejectionDetailMapList.add(rejectionDetailMap);
                        operationMsg = "rejected";
                    }

                    operationQty += sada.getQuantity();
                    if (sa.getProduct().isIsSerialForProduct()) {
                        if (!StringUtil.isNullOrEmpty(operationSerials)) {
                            operationSerials += ", ";
                        }
                        operationSerials += sada.getSerialName();
                    }

                    if (StringUtil.isNullOrEmpty(auditMessage)) {// only one time
                        auditMessage += "Stock Adjustment: " + sa.getTransactionNo() + ", Product: " + sa.getProduct().getProductid() + ", Store: " + sa.getStore().getAbbreviation();
                    }
                }
                saApprovalService.createStockMovementForQAApproval(company, approvedRejectedRecords, qaStore, repairStore);
                if (operation.equals("Approve")) {
                    sendUserProductQAApprovalRejectionMail(company,approvedDetailMapList,extraEmailIds,"",user,transactionNo,true,"Stock Adjustment");
                } else if (operation.equals("Reject")) {
                    sendUserProductQAApprovalRejectionMail(company, rejectionDetailMapList, extraEmailIds,"", user, transactionNo,false,"Stock Adjsutment"); //user is logged in user
                }
                msg = "QA has " + operationMsg + " Stock Adjustment data successfully";

                auditMessage += ", Quantity: " + operationQty + (!StringUtil.isNullOrEmpty(operationSerials) ? ", Serials: (" + operationSerials + ")" : "");

            } else if ("goodsreceipt".equals(modulType)) {
                /**
                 * Create an IST request based on the operation performed in QA
                 * approval screen. If user approves GRN then IST will be
                 * created to transfer stock from QA to GRN Store else if
                 * rejected then an IST will be created to repair store from QA
                 * store.
                 */

                String interstore_loc_No = "";
                SeqFormat seqFormat = null;
                try {
                    seqFormat = seqService.getDefaultSeqFormat(company, ModuleConst.INTER_STORE_TRANSFER);
                    if (seqFormat != null) {
                        interstore_loc_No = seqService.getNextFormatedSeqNumber(seqFormat);
                    } else {
                        throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforistnotset", null, RequestContextUtils.getLocale(request)));
                    }
                } catch (SeqFormatException ex) {
                    throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforistnotset", null, RequestContextUtils.getLocale(request)));
                }
                if (operation.equals("Reject")) {
                    if (repairStore == null) {
                        throw new AccountingException(messageSource.getMessage("acc.companypreferences.repairstore.notset", null, RequestContextUtils.getLocale(request)));
                    }
                    if (repairStore.getDefaultLocation() == null) {
                        throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultLocationNotSetforRepairStore", null, RequestContextUtils.getLocale(request)));
                    }
                }
                if (operation.equals("Approve")) {
                    operationMsg = "approved";
                } else {
                    operationMsg = "rejected";
                }
                Map<String, Object> requestParams = new HashMap<>();
                requestParams.put("jsondata", jArr.toString());
                requestParams.put("user", user);
                requestParams.put("company", companyId);
                requestParams.put("operation", operation);
                requestParams.put("remark", remark);
                requestParams.put("interstore_loc_No", interstore_loc_No);
                requestParams.put("seqFormat", seqFormat);
                auditMessage = stockTransferApprovalService.approveRejectGoodsReceipt(requestParams, repairStore, repairLocation);
                
                /*
                * Move this part to Service Layer
                */
//                InterStoreTransferRequest outgoingISTRequest = null;
//                InterStoreTransferRequest incomingISTRequest = null;
//                GRODetailISTMapping detailISTMapping = null;
//                double totalQuantityDue = 0, totalApprovedQty = 0, totalRejectedQty = 0, totalRejectedQtyDue = 0, totalQty = 0;
//                StringBuilder approvedSerials = new StringBuilder(), rejectedSerials = new StringBuilder();
//                String groNumber = "";
//                for (int i = 0; i < jArr.length(); i++) {
//                    JSONObject json = jArr.getJSONObject(i);
//                    String detailid = json.optString("recordid");
//                    String serialname = json.optString("serialname");
//                    double quantity = json.optDouble("quantity");
//                    if (!StringUtil.isNullOrEmpty(detailid)) {
//                        ISTDetail istDetail = interStoreTransferService.getISTDetailById(detailid);
//                        if (istDetail != null && quantity > 0) {
//                            if (operation.equals("Approve") || operation.equals("Reject")) {
//                                if(istDetail.getIssuedQuantity()-(istDetail.getQaApproved()+istDetail.getQaRejected()) < quantity){
//                                    throw  new AccountingException("This item is already processed");
//                                }
//                                if (operation.equals("Approve")) {
//                                    operationMsg = "approved";
//                                    istDetail.setQaApproved(istDetail.getQaApproved() + quantity);
//                                } else {
//                                    operationMsg = "rejected";
//                                    istDetail.setQaRejected(istDetail.getQaRejected() + quantity);
//                                }
//                                incomingISTRequest = istDetail.getIstRequest();
//                                JSONObject params = new JSONObject();
//                                params.put("istRequest", incomingISTRequest.getId());
//                                KwlReturnObject result = stockService.getGRODetailISTMapping(params);
//                                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
//                                    detailISTMapping = (GRODetailISTMapping) result.getEntityList().get(0);
//                                }
//                                if (outgoingISTRequest == null && incomingISTRequest != null) {
//                                    if (operation.equals("Reject")) {
//                                        outgoingISTRequest = new InterStoreTransferRequest(incomingISTRequest.getProduct(), incomingISTRequest.getToStore(), repairStore, incomingISTRequest.getUom());
//                                    } else {
//                                        outgoingISTRequest = new InterStoreTransferRequest(incomingISTRequest.getProduct(), incomingISTRequest.getToStore(), incomingISTRequest.getFromStore(), incomingISTRequest.getUom());
//                                    }
//                                    outgoingISTRequest.setRemark(remark);
//                                    outgoingISTRequest.setAcceptedQty(quantity);
//                                    outgoingISTRequest.setOrderedQty(quantity);
//                                    outgoingISTRequest.setBusinessDate(new Date());
//                                    outgoingISTRequest.setPackaging(incomingISTRequest.getProduct().getPackaging());
//                                    totalApprovedQty = detailISTMapping.getApprovedQty();
//                                    totalQuantityDue = detailISTMapping.getQuantityDue();
//                                    totalRejectedQty = detailISTMapping.getRejectedQty();
//                                } else {
//                                    outgoingISTRequest.setOrderedQty(outgoingISTRequest.getOrderedQty() + quantity);
//                                    outgoingISTRequest.setAcceptedQty(outgoingISTRequest.getAcceptedQty() + quantity);
//                                }
//                                /*
//                                * Check quantitydue before approving transaction to avoid negative stock
//                                * Quantitydue contains pending stock in QA
//                                */
//                                if (totalQuantityDue<=0 || totalQuantityDue<quantity){
//                                    throw new AccountingException("This item is already processed");
//                                }
//                                ISTDetail outISTDetail = new ISTDetail();
//                                outISTDetail.setBatchName(istDetail.getBatchName());
//                                outISTDetail.setDeliveredBin(istDetail.getIssuedBin());
//                                outISTDetail.setDeliveredQuantity(quantity);
//                                outISTDetail.setDeliveredRack(istDetail.getIssuedRack());
//                                outISTDetail.setDeliveredRow(istDetail.getIssuedRow());
//                                outISTDetail.setDeliveredSerialNames(serialname);
//                                if (operation.equals("Reject")) {
//                                    if (repairLocation != null) { //ERM-691 dynamic changing of repair store
//                                        outISTDetail.setDeliveredLocation(repairLocation);
//                                    } else {
//                                        outISTDetail.setDeliveredLocation(repairStore.getDefaultLocation());
//                                    }
//                                } else {
//                                    outISTDetail.setDeliveredLocation(istDetail.getIssuedLocation());
//                                }
//                                outISTDetail.setIssuedBin(istDetail.getDeliveredBin());
//                                outISTDetail.setIssuedLocation(istDetail.getDeliveredLocation());
//                                outISTDetail.setIssuedQuantity(quantity);
//                                outISTDetail.setIssuedRack(istDetail.getDeliveredRack());
//                                outISTDetail.setIssuedRow(istDetail.getDeliveredRow());
//                                outISTDetail.setIssuedSerialNames(serialname);
//                                outISTDetail.setIstRequest(outgoingISTRequest);
//                                if (outgoingISTRequest != null) {
//                                    outgoingISTRequest.getIstDetails().add(outISTDetail);
//                                }
//                                outgoingISTRequest.setTransactionNo(interstore_loc_No);
//                                if (detailISTMapping != null) {
//                                    JSONObject jsonParams = new JSONObject();
//                                    jsonParams.put(Constants.companyKey, companyId);
//                                    jsonParams.put("mappingid", detailISTMapping.getID());
//                                    groNumber = stockService.getGoodsReceiptOrderNumberUsingMapping(jsonParams);
//                                    StringBuilder processedSerials = new StringBuilder(detailISTMapping.getApprovedSerials()+","+detailISTMapping.getRejectedSerials());
//                                    if(!StringUtil.isNullOrEmpty(processedSerials.toString())){
//                                        String[] processedSerial = processedSerials.toString().split(",");
//                                        for(String serial : processedSerial)
//                                            if(serial.equals(serialname)){
//                                                throw new AccountingException("This item is already processed");
//                                            }
//                                    }
//                                    
//                                    if (!StringUtil.isNullOrEmpty(groNumber)) {
//                                        if (operation.equals("Approve")) {
//                                            totalApprovedQty += quantity;
//                                            outgoingISTRequest.setMemo("Approved Goods Receipt Note : " + groNumber);
//                                            outgoingISTRequest.setDetailISTMapping(detailISTMapping);
//                                            if (!StringUtil.isNullOrEmpty(serialname)) {
//                                                if (StringUtil.isNullOrEmpty(approvedSerials.toString())) {
//                                                    approvedSerials.append(StringUtil.isNullOrEmpty(detailISTMapping.getApprovedSerials()) ? "" : detailISTMapping.getApprovedSerials());
//                                                }
//                                                if (approvedSerials.length() > 0) {
//                                                    approvedSerials.append(",").append(serialname);
//                                                } else {
//                                                    approvedSerials.append(serialname);
//                                                }
//                                            }
//                                        } else {
//                                            totalRejectedQty += quantity;
//                                            totalRejectedQtyDue += quantity;
//                                            outgoingISTRequest.setMemo("Rejected Goods Receipt Note : " + groNumber);
//                                            if (!StringUtil.isNullOrEmpty(serialname)) {
//                                                if (StringUtil.isNullOrEmpty(rejectedSerials.toString())) {
//                                                    rejectedSerials.append(StringUtil.isNullOrEmpty(detailISTMapping.getRejectedSerials()) ? "" : detailISTMapping.getRejectedSerials());
//                                                }
//                                                if (rejectedSerials.length() > 0) {
//                                                    rejectedSerials.append(",").append(serialname);
//                                                } else {
//                                                    rejectedSerials.append(serialname);
//                                                }
//                                            }
//                                        }
//                                        totalQuantityDue -= quantity;
//                                    }
//                                }
//                                totalQty += quantity;
//                            }
//                        }
//                    }
//                }
//                if (outgoingISTRequest != null && incomingISTRequest != null) {
//                  //  Map<String, Object> requestParams = new HashMap<>();
//                    requestParams.put(Constants.companyid, companyId);
//                   requestParams.put("mappingid", detailISTMapping.getID());
//                    requestParams.put("operation", operation);
//                    requestParams.put(Constants.moduleid, Constants.Acc_InterStore_ModuleId);
//                    requestParams.put("quantitydue", totalQuantityDue);
//                    if (operation.equals("Approve")) {
//                        auditMessage = "Approved Goods Receipt Note: " + groNumber + " [Product: " + incomingISTRequest.getProduct().getProductid() + ", Quantity: " + totalQty + ", Store: " + incomingISTRequest.getFromStore().getAbbreviation() + (!StringUtil.isNullOrEmpty(approvedSerials.toString()) ? ", Serials: (" + approvedSerials.toString() + ")" : "");
//                        requestParams.put("approvedQty", totalApprovedQty);
//                        if (!StringUtil.isNullOrEmpty(approvedSerials.toString())) {
//                            requestParams.put("approvedSerials", approvedSerials.toString());
//                        }
//                    } else {
//                        auditMessage = "Rejected Goods Receipt Note: " + groNumber + " [Product: " + incomingISTRequest.getProduct().getProductid() + ", Quantity: " + totalQty + ", Store: " + incomingISTRequest.getFromStore().getAbbreviation() + (!StringUtil.isNullOrEmpty(rejectedSerials.toString()) ? ", Serials: (" + rejectedSerials.toString() + ")" : "");
//                        requestParams.put("rejectedQty", totalRejectedQty);
//                        requestParams.put("rejectedQtyDue", totalRejectedQtyDue);
//                        if (!StringUtil.isNullOrEmpty(rejectedSerials.toString())) {
//                            requestParams.put("rejectedSerials", rejectedSerials.toString());
//                        }
//                    }
//                    interStoreTransferService.addInterStoreTransferRequest(user, outgoingISTRequest, false, requestParams);
//                    interStoreTransferService.acceptInterStoreTransferRequest(user, outgoingISTRequest);
//                    seqService.updateSeqNumber(seqFormat);
//                    msg = "QA has " + operationMsg + " Goods Receipt Note successfully";
//                    
//                    //newistid contains id of newly rejected or approved row in qa in case of goods receipt
//                    boolean isfirst = true;
//                    ISTDetail istd;
//                    Iterator iterator = outgoingISTRequest.getIstDetails().iterator();
//                    while (iterator.hasNext()) {
//                        istd = (ISTDetail) iterator.next();
//                        if (isfirst) {
//                            newistid = istd.getId();
//                            isfirst = false;
//                        } else {
//                            newistid = newistid + "," + istd.getId();
//                        }
//                    }
//                }
            } else if (Constants.MRP_WORK_ORDER.equals(modulType)) {
                /**
                 * Create an IST request based on the operation performed in QA
                 * approval screen. If user approves WORK ORDER then IST will be
                 * created to transfer stock from QA to WORK ORDER Store else if
                 * rejected then an IST will be created to repair store from QA
                 * store.
                 */

                String interstore_loc_No = "";
                SeqFormat seqFormat = null;
                try {
                    seqFormat = seqService.getDefaultSeqFormat(company, ModuleConst.INTER_STORE_TRANSFER);
                    if (seqFormat != null) {
                        interstore_loc_No = seqService.getNextFormatedSeqNumber(seqFormat);
                    } else {
                        throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforistnotset", null, RequestContextUtils.getLocale(request)));
                    }
                } catch (SeqFormatException ex) {
                    throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforistnotset", null, RequestContextUtils.getLocale(request)));
                }
                if (operation.equals("Reject")) {
                    if (repairStore == null) {
                        throw new AccountingException(messageSource.getMessage("acc.companypreferences.repairstore.notset", null, RequestContextUtils.getLocale(request)));
                    }
                    if (repairStore.getDefaultLocation() == null) {
                        throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultLocationNotSetforRepairStore", null, RequestContextUtils.getLocale(request)));
                    }
                }
                InterStoreTransferRequest outgoingISTRequest = null;
                InterStoreTransferRequest incomingISTRequest = null;
                WOCDetailISTMapping detailISTMapping = null;
                double totalQuantityDue = 0, totalApprovedQty = 0, totalRejectedQty = 0, totalRejectedQtyDue = 0, totalQty = 0;
                StringBuilder approvedSerials = new StringBuilder(), rejectedSerials = new StringBuilder();
                String woNumber = "";
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject json = jArr.getJSONObject(i);
                    String detailid = json.optString("recordid");
                    String serialname = json.optString("serialname");
                    double quantity = json.optDouble("quantity");
                    if (!StringUtil.isNullOrEmpty(detailid)) {
                        ISTDetail istDetail = interStoreTransferService.getISTDetailById(detailid);
                        if (istDetail != null && quantity > 0) {
                            if (operation.equals("Approve") || operation.equals("Reject")) {
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
                                KwlReturnObject result = stockService.getWOCDetailISTMapping(params);
                                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                    detailISTMapping = (WOCDetailISTMapping) result.getEntityList().get(0);
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
                                ISTDetail outISTDetail = new ISTDetail();
                                outISTDetail.setBatchName(istDetail.getBatchName());
                                outISTDetail.setDeliveredBin(istDetail.getIssuedBin());
                                outISTDetail.setDeliveredQuantity(quantity);
                                outISTDetail.setDeliveredRack(istDetail.getIssuedRack());
                                outISTDetail.setDeliveredRow(istDetail.getIssuedRow());
                                outISTDetail.setDeliveredSerialNames(serialname);
                                
                                InspectionForm inspectionFormPending = istDetail.getInspectionForm();
                                if(inspectionFormPending!=null){
                                    JSONObject JOInspectionInfo= new JSONObject();
                                    JOInspectionInfo.put("inspectionDate",inspectionFormPending.getInspectionDate().toString());
                                    JOInspectionInfo.put("modelName",inspectionFormPending.getModelName());
                                    JOInspectionInfo.put("consignmentReturnNo",interstore_loc_No);
                                    JOInspectionInfo.put("department",inspectionFormPending.getDepartment());
                                    JOInspectionInfo.put("customerName",inspectionFormPending.getCustomerName());
                                    KwlReturnObject kwlReturnObject = accCommonTablesDAO.saveOrUpdateInspectionForm(JOInspectionInfo);
                                    List inspectionFormList= kwlReturnObject.getEntityList();
                                    InspectionForm inspectionForm =(InspectionForm) inspectionFormList.get(0);
                                    
                                    outISTDetail.setInspectionForm(inspectionForm);
                                    
                                    Set<InspectionFormDetails> inspectionFormDetails = inspectionFormPending.getRows();
                                    HashMap<String,Object> inspectionFormDetailmap = new HashMap<String,Object>();
                                    
                                    for(InspectionFormDetails inspectionFormDetail:inspectionFormDetails){
                                        inspectionFormDetailmap.put("inspectionFormId", inspectionForm.getId());
                                        inspectionFormDetailmap.put("areaId", inspectionFormDetail.getInspectionArea()!=null?inspectionFormDetail.getInspectionArea().getId():"");
                                        inspectionFormDetailmap.put("areaName", inspectionFormDetail.getInspectionAreaValue());
                                        inspectionFormDetailmap.put("status", inspectionFormDetail.getInspectionStatus());
                                        inspectionFormDetailmap.put("faults", inspectionFormDetail.getFaults());
                                        inspectionFormDetailmap.put("passingValue", inspectionFormDetail.getPassingValue());
                                        inspectionFormDetailmap.put("actualValue", inspectionFormDetail.getActualValue());
                                        accCommonTablesDAO.saveInspectionFormDetails(inspectionFormDetailmap);
                                    }
                                }
                                
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
                                    jsonParams.put("wocdistmapping", detailISTMapping.getID());
                                    woNumber = stockService.getWorkOrderNumberUsingMapping(jsonParams);
                                    // TO-DO
                                    if (!StringUtil.isNullOrEmpty(woNumber)) {
                                        if (operation.equals("Approve")) {
                                            totalApprovedQty += quantity;
                                            outgoingISTRequest.setMemo("Approved Work Order : " + woNumber);
                                            outgoingISTRequest.setWocdISTMapping(detailISTMapping);
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
                                            outgoingISTRequest.setMemo("Rejected Work Order : " + woNumber);
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
                    Map<String, Object> requestParams = new HashMap<>();
                    requestParams.put(Constants.companyid, companyId);
                    requestParams.put("wocdistmapping", detailISTMapping.getID());
                    requestParams.put("operation", operation);
                    requestParams.put(Constants.moduleid, Constants.Acc_InterStore_ModuleId);
                    requestParams.put(Constants.CREATE_IST_FOR_QC_WORKORDER, true);
                    requestParams.put("quantitydue", totalQuantityDue);
                    if (operation.equals("Approve")) {
                        auditMessage = "Approved Work Order: " + woNumber + " [Product: " + incomingISTRequest.getProduct().getProductid() + ", Quantity: " + totalQty + ", Store: " + incomingISTRequest.getFromStore().getAbbreviation() + (!StringUtil.isNullOrEmpty(approvedSerials.toString()) ? ", Serials: (" + approvedSerials.toString() + ")" : "");
                        requestParams.put("approvedQty", totalApprovedQty);
                        if (!StringUtil.isNullOrEmpty(approvedSerials.toString())) {
                            requestParams.put("approvedSerials", approvedSerials.toString());
                        }
                    } else {
                        auditMessage = "Rejected Work Order: " + woNumber + " [Product: " + incomingISTRequest.getProduct().getProductid() + ", Quantity: " + totalQty + ", Store: " + incomingISTRequest.getFromStore().getAbbreviation() + (!StringUtil.isNullOrEmpty(rejectedSerials.toString()) ? ", Serials: (" + rejectedSerials.toString() + ")" : "");
                        requestParams.put("rejectedQty", totalRejectedQty);
                        requestParams.put("rejectedQtyDue", totalRejectedQtyDue);
                        if (!StringUtil.isNullOrEmpty(rejectedSerials.toString())) {
                            requestParams.put("rejectedSerials", rejectedSerials.toString());
                        }
                    }
                    interStoreTransferService.addInterStoreTransferRequest(user, outgoingISTRequest, false, requestParams);
                    interStoreTransferService.acceptInterStoreTransferRequest(user, outgoingISTRequest);
                    seqService.updateSeqNumber(seqFormat);
                    msg = "QA has " + operationMsg + " Work Order successfully";
                    
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
                    
                     /**
                      * Added productIds to update IST price based on the
                      * valuation done in AOP advisor.
                      */     
                   
                    if (productIds.indexOf(outgoingISTRequest.getProduct().getID()) == -1) {
                        productIds.append(outgoingISTRequest.getProduct().getID()).append(",");
                    }                                      
                }
            } else if ("deliveryorder".equals(modulType)) {
                /**
                 * Create Stock Adjustment OUT if users reject document from
                 * repair store.
                 */
                String stockAdjustmentTransactionNo = "";
                SeqFormat seqFormat = null;
                if ("Reject".equalsIgnoreCase(operation) || ("Approve".equalsIgnoreCase(operation) && extraCompanyPreferences.isPickpackship())) {
                    try {
                        seqFormat = seqService.getDefaultSeqFormat(company, ModuleConst.INTER_STORE_TRANSFER);
                        if (seqFormat != null) {
                            stockAdjustmentTransactionNo = seqService.getNextFormatedSeqNumber(seqFormat);
                        } else {
                            throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforistnotset", null, RequestContextUtils.getLocale(request)));
                        }
                    } catch (SeqFormatException ex) {
                        throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforistnotset", null, RequestContextUtils.getLocale(request)));
                    }
                } else if ("Approve".equalsIgnoreCase(operation) && !extraCompanyPreferences.isPickpackship()) {
                    try {
                        seqFormat = seqService.getDefaultSeqFormat(company, ModuleConst.STOCK_ADJUSTMENT);
                        if (seqFormat != null) {
                            stockAdjustmentTransactionNo = seqService.getNextFormatedSeqNumber(seqFormat);
                        } else {
                            throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforStockAdjustmentNotSet", null, RequestContextUtils.getLocale(request)));
                        }
                    } catch (SeqFormatException ex) {
                        throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforStockAdjustmentNotSet", null, RequestContextUtils.getLocale(request)));
                    }
                }
                if ("Reject".equalsIgnoreCase(operation)) {
                    if (repairStore == null) {
                        throw new AccountingException(messageSource.getMessage("acc.companypreferences.repairstore.notset", null, RequestContextUtils.getLocale(request)));
                    }
                    if (repairStore.getDefaultLocation() == null) {
                        throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultLocationNotSetforRepairStore", null, RequestContextUtils.getLocale(request)));
                    }
                } else if ("Approve".equalsIgnoreCase(operation) && extraCompanyPreferences.isPickpackship()) {
                    if (packingStore == null) {
                        throw new AccountingException("Packing Store is not set in Company Preferences.");
                    }
                    if (packingStore.getDefaultLocation() == null) {
                        throw new AccountingException("Default location is not set for packaging store.");
                    }
                }
                List<Object> ids = approvedOrRejectDeliveryOrderFromQA(packingStore, repairStore, company, extraCompanyPreferences, companyAccountPreferences, remark, jArr, operation, operationMsg, auditMessage, seqFormat, stockAdjustmentTransactionNo, request, user);
                if (ids != null && ids.size() > 0) {
                    productIds = (StringBuilder) ids.get(0);
                    newistid = (String) ids.get(1);
                }
                if (operation.equals("Approve")) {
                    operationMsg = "approved";
                } else {
                    operationMsg = "rejected";
                }
            }
            msg = "Selected item is " + operationMsg.toLowerCase() + " successfully.";
            issuccess = true;
            if(!StringUtil.isNullOrEmpty(auditMessage)){
                auditMessage = "User " + user.getFullName() + " has " + operationMsg.toLowerCase() + " QA Approval - " + auditMessage;
                auditTrailObj.insertAuditLog(AuditAction.QA_INSPECTION, auditMessage, request, "0");
            }
            txnManager.commit(status);
            
            if (assignStockToPendingConsignmentRequest) {
                TransactionStatus statusforBlockSOQty = txnManager.getTransaction(def);
                try {
                    KwlReturnObject retObj = consignmentService.assignStockToPendingConsignmentRequests(request);
                    txnManager.commit(statusforBlockSOQty);
                } catch (Exception ex) {
                    txnManager.rollback(statusforBlockSOQty);
                    Logger.getLogger(StockAdjustmentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            }


        } catch (Exception ex) {
            issuccess = false;
            msg = ex.getMessage();
            txnManager.rollback(status);
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                /**
                 * Added productIds to update journal entry price based on the
                 * valuation done in AOP advisor.
                 */
                jobj.put("productIds", productIds);
                jobj.put("istid", newistid); //newistid contain id of rejected or approved row in case of delivery order or goodsreceipt
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView saveStockRepair(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SA_Tx_Save_Inspection");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        StringBuilder productIds = new StringBuilder();
        try {
            synchronized(this){
            JSONArray jArr = new JSONArray(request.getParameter("jsondata"));
            String operation = request.getParameter("operation");
            String type = request.getParameter("type");
            String scrapstoreid = request.getParameter("scrapstore")!=null?request.getParameter("scrapstore"):""; //ERM-691 scrap store flow for rejected QA repair stock
            String reason = request.getParameter("reason");
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);
            KwlReturnObject extracompanyprefObjresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), company.getCompanyID());
            ExtraCompanyPreferences extracompanyobj = (ExtraCompanyPreferences) extracompanyprefObjresult.getEntityList().get(0);
            KwlReturnObject capresult = accountingHandlerDAO.getObject(CompanyAccountPreferences.class.getName(), companyId);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            Store qaStore = storeService.getStoreById(extracompanyobj.getInspectionStore());
            Store repaieStore = storeService.getStoreById(extracompanyobj.getRepairStore());
            List<SADetailApproval> sadApprovalList = new ArrayList();
            List<ConsignmentApprovalDetails> consignmentList = new ArrayList();
            List<StockTransferDetailApproval> stockRequestDetailApprovalList = new ArrayList<StockTransferDetailApproval>();
            List<StockTransferDetailApproval> stockTransferDetailApprovalList = new ArrayList<StockTransferDetailApproval>();
            Map<RepairGRODetailISTMapping, List<ISTDetail>> mapping = new HashMap<>();
            Store packingStore = stockService.getPackingstore(company);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jsonObj = jArr.optJSONObject(i);
                String moduleId = jsonObj.getString("moduleId");
                String recordId = jsonObj.getString("recordid");
                String moduleType = jsonObj.getString("moduletype");
                double qty = jsonObj.has("quantity") ? jsonObj.getDouble("quantity") : 0;
                String operationMsg = "";
                if ("consignment".equals(moduleType)) {
                    ConsignmentApprovalDetails consignmentApDetail = consignmentService.getConsingmentDetailsById(recordId);
                    
                    consignmentApDetail.setReason(reason);
                    Consignment consignment = consignmentApDetail.getConsignment();
//                    consignmentList.add(consignmentApDetail);
                    if (operation.equals("Approve")) {
                        consignmentApDetail.setRepairStatus(ApprovalStatus.REPAIRDONE);
                        consignmentService.approveConsignmentDetail(user, consignmentApDetail, null, company, qty, true, qaStore, repaieStore);

                        operationMsg = "approved";
                        msg = "Selected item repaired successfully..";
                    } else if (operation.equals("Reject")) {
                        consignmentApDetail.setRepairStatus(ApprovalStatus.REPAIRREJECT);

                        consignmentService.rejectConsignmentDetail(user, consignmentApDetail, null, company, qty, true, qaStore, repaieStore);
                        msg = "Selected item cannot be repaired ";
                        operationMsg = "rejected";
                    }
                    consignmentList.add(consignmentApDetail);
                } else if (Constants.BUILD_ASSEMBLY_QA_APPROVAL.equals(moduleType)) {
                    /*
                This function is called to approve/reject job work assembly product quantity from qa approval tab
                */
                    request.setAttribute("isFromRepair", true);
                    request.setAttribute("qadetailid", recordId);
                    if (operation.equals("Approve")) {
                        approveAssemblyProductPendingForQa(request);
                        operationMsg = messageSource.getMessage("acc.common.Approved", null, RequestContextUtils.getLocale(request));
                        
                        msg =messageSource.getMessage("acc.qaapporval.succesmsg", null, RequestContextUtils.getLocale(request));
                    } else if (operation.equals("Reject")) {
//                       request.setAttribute("isFromRepair", true);
                        approveAssemblyProductPendingForQa(request);
                        msg = messageSource.getMessage("acc.qaapporval.failmsg", null, RequestContextUtils.getLocale(request));;
                        
                        operationMsg = messageSource.getMessage("acc.common.Rejected", null, RequestContextUtils.getLocale(request)) ;
                    }

                }else if ("stockrequest".equals(moduleType) || "stocktransfer".equals(moduleType)) {
                    KwlReturnObject jeresult1 = accountingHandlerDAO.getObject(StockTransferApproval.class.getName(), moduleId);
                    StockTransferApproval stApproval = (StockTransferApproval) jeresult1.getEntityList().get(0);
                    TransactionModule module1 = stApproval.getTransactionModule();
                    if (module1 == TransactionModule.INTER_STORE_TRANSFER) {
//                        operation = "Approved";
                        StockTransferDetailApproval stockTransferDTL = stockTransferApprovalService.getStockTransferDetailApproval(recordId);
                        stockTransferDTL.setReason(reason);
//                        stockTransferDetailApprovalList.add(stockTransferDTL);
                        if (operation.equals("Approve")) {
                            operationMsg = "approved";
                            stockTransferDTL.setRepairStatus(ApprovalStatus.REPAIRDONE);
                            stockTransferApprovalService.approveStockTransferDetail(user, stockTransferDTL, null, true, qty);
//                            ISTDetail ISD = interStoreTransferService.getISTDetailById(stockTransferDTL.getStockTransferDetailId());
//                            stockService.addStockMovementForQa(ISD, repaieStore, TransactionType.OUT, ISD.getBatchName(), stockTransferDTL.getSerialName(), qty, TransactionModule.INTER_STORE_TRANSFER, company);
                            operationMsg = "approved";
                            msg = "Selected item repaired successfully..";
                        } else if (operation.equals("Reject")) {
                            operationMsg = "rejected";
                            stockTransferDTL.setRepairStatus(ApprovalStatus.REPAIRREJECT);
                            stockTransferApprovalService.rejectStockTransferDetail(user, stockTransferDTL, null, true, qty);
                            msg = "Selected item repaired successfully..";
                            operationMsg = "rejected";

                        }
                        stockTransferDetailApprovalList.add(stockTransferDTL);
                        ISTDetail ISD = interStoreTransferService.getISTDetailById(stockTransferDTL.getStockTransferDetailId());
//                        stockService.addStockMovementForQa(ISD, repaieStore, TransactionType.OUT, ISD.getBatchName(), stockTransferDTL.getSerialName(), qty,
//                                TransactionModule.INTER_STORE_TRANSFER, company, repaieStore.getDefaultLocation());
//                        stockService.decreaseInventory(ISD.getIstRequest().getProduct(), repaieStore, repaieStore.getDefaultLocation(), ISD.getBatchName(), stockTransferDTL.getSerialName(),
//                                qty);
//                        
//                        Store originalStore = ISD.getIstRequest().getToStore();
//                        if (originalStore != null && operation.equals("Approve")) {
//                            stockService.addStockMovementForQa(ISD, originalStore, TransactionType.IN, ISD.getBatchName(), stockTransferDTL.getSerialName(), qty,
//                                    TransactionModule.INTER_STORE_TRANSFER, company, ISD.getIssuedLocation());
//                            stockService.increaseInventory(ISD.getIstRequest().getProduct(), originalStore, ISD.getIssuedLocation(), ISD.getBatchName(), stockTransferDTL.getSerialName(),
//                                    qty);
//                            stockMovementService.stockMovementInERP(true, ISD.getIstRequest().getProduct(), originalStore, ISD.getIssuedLocation(), ISD.getBatchName(), stockTransferDTL.getSerialName(), qty);
//                        }

                         /**
                         * ERP-37353 posting a JE when IST transactions are rejected from the Repair Store for perpetual type
                         * currently commenting this code for a issue related to product master corruption will be handled separately.
                         */
//                        if (preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD && operation.equals("Reject")) {
//                            //params for stock out JE posted for QA repair reject in IST
//                            JSONObject stockoutparams = new JSONObject();                            
//                            stockoutparams.put(Constants.moduleid, moduleId);                            
//                            stockoutparams.put(Constants.userid, sessionHandlerImpl.getUserid(request));                            
//                            stockoutparams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
//                            stockoutparams.put(Constants.userdateformat, sessionHandlerImpl.getUserDateFormat(request));
//                            stockoutparams.put(Constants.locale,RequestContextUtils.getLocale(request));                            
//                            stockoutparams.put(Constants.quantity, qty);
//                            stockoutparams.put(Constants.companyid, companyId);
//                            stockoutparams.put(Constants.transactionmodule,module1);
//                            stockoutparams.put(Constants.remoteIPAddress,request.getRemoteAddr());
//                            stockoutparams.put("recordid", stockTransferDTL.getStockTransferDetailId());
//                            stockoutparams.put("stocktransferDTL", stockTransferDTL);
//                            stockoutparams.put("moduletype", moduleType);                            
//                            stockoutparams.put("operation", operation);                            
//                            stockoutparams.put("reason", reason);
//                            stockoutparams.put("repairstore", extracompanyobj.getRepairStore());
//                            stockoutparams.put("repairlocation", extracompanyobj.getRepairStore());
//                            stockTransferApprovalService.createStockOutInventoryJEforQAtransaction(stockoutparams);
//                            
//                            //Adding product ids here for AOP advisor and IVP valuation JEs to be updated later
//                            if (productIds.indexOf(ISD.getIstRequest().getProduct().getID()) == -1) {
//                                productIds.append(ISD.getIstRequest().getProduct().getID()).append(",");
//                            }
//                        }
                    
                    
                    } else if (module1 == TransactionModule.STOCK_REQUEST) {
                        StockTransferDetailApproval stockTransferDTL = stockTransferApprovalService.getStockTransferDetailApproval(recordId);
                        stockTransferDTL.setReason(reason);
//                        stockRequestDetailApprovalList.add(stockTransferDTL);
                        StockRequestDetail SRD = stockRequestService.getStockRequestDetail(stockTransferDTL.getStockTransferDetailId());
                        StockRequest sr = SRD.getStockRequest();
                        
                        if (operation.equals("Approve")) {
                            operationMsg = "approved";
                            stockTransferApprovalService.approveStockTransferDetail(user, stockTransferDTL, null, true, qty);
                            operationMsg = "approved";
                            msg = "Selected item  repaired Successfully..";
                        } else if (operation.equals("Reject")) {
                            operationMsg = "rejected";
                            stockTransferApprovalService.rejectStockTransferDetail(user, stockTransferDTL, null, true, qty);
                            msg = "Selected item cannot be repaired ";
                            operationMsg = "rejected";
                            /**
                             * ERP-37353 posting a JE when Stock request
                             * transactions are rejected from the Repair Store for perpetual type.
                             */
                           if (preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) {
                               
                               //params for stock out JE posted for QA repair reject in IST
                               JSONObject stockoutparams = new JSONObject();
                               stockoutparams.put(Constants.moduleid, moduleId);
                               stockoutparams.put(Constants.userid, sessionHandlerImpl.getUserid(request));
                               stockoutparams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
                               stockoutparams.put(Constants.userdateformat, sessionHandlerImpl.getUserDateFormat(request));
                               stockoutparams.put(Constants.locale, RequestContextUtils.getLocale(request));
                               stockoutparams.put(Constants.quantity, qty);
                               stockoutparams.put(Constants.companyid, companyId);
                               stockoutparams.put(Constants.transactionmodule,module1);
                               stockoutparams.put(Constants.remoteIPAddress,request.getRemoteAddr());
                               stockoutparams.put("recordid", recordId);
                               stockoutparams.put("moduletype", moduleType);
                               stockoutparams.put("stockrequestdetail", SRD);
                               stockoutparams.put("operation", operation);
                               stockoutparams.put("reason", reason);
                               stockoutparams.put("repairstore", extracompanyobj.getRepairStore());
                               stockoutparams.put("repairlocation", extracompanyobj.getRepairStore());
                               stockTransferApprovalService.createStockOutInventoryJEforQAtransaction(stockoutparams);
                               
                               //Adding product ids here for AOP advisor and IVP valuation JEs to be updated later
                               if (productIds.indexOf(sr.getProduct().getID()) == -1) {
                                   productIds.append(sr.getProduct().getID()).append(",");
                               }
                           }
                        }
                        stockRequestDetailApprovalList.add(stockTransferDTL);
//                        stockService.addStockMovementForSRDQa(SRD, repaieStore, TransactionType.OUT, SRD.getBatchName(), stockTransferDTL.getSerialName(), qty,
//                                TransactionModule.INTER_STORE_TRANSFER, company, repaieStore.getDefaultLocation());
//                        stockService.decreaseInventory(SRD.getStockRequest().getProduct(), repaieStore, repaieStore.getDefaultLocation(), SRD.getBatchName(), stockTransferDTL.getSerialName(),
//                                qty);
//                        
//                        Store originalStore = SRD.getStockRequest().getFromStore();
//                        if (originalStore != null && operation.equals("Approve")) {
//                            stockService.addStockMovementForSRDQa(SRD, originalStore, TransactionType.IN, SRD.getBatchName(), stockTransferDTL.getSerialName(), qty,
//                                    TransactionModule.INTER_STORE_TRANSFER, company, SRD.getDeliveredLocation());
//                            stockService.increaseInventory(SRD.getStockRequest().getProduct(), originalStore, SRD.getDeliveredLocation(), SRD.getBatchName(), stockTransferDTL.getSerialName(),
//                                    qty);
//                            stockMovementService.stockMovementInERP(true, SRD.getStockRequest().getProduct(), originalStore, SRD.getDeliveredLocation(), SRD.getBatchName(), stockTransferDTL.getSerialName(), qty);
//                        }
                    }

                } else if ("stockout".equals(moduleType)) {
                    SADetailApproval sada = saApprovalService.getSADetailApproval(recordId);
                    sada.setReason(reason);
//                    sadApprovalList.add(sada);
                    if (operation.equals("Approve")) {
                        sada.setRepairStatus(ApprovalStatus.REPAIRDONE);
                        saApprovalService.approveStockAdjustmentDetail(user, sada, null, qty, true);
                        operationMsg = "approved";
                        msg = "Selected item  repaired Successfully..";
                    } else if (operation.equals("Reject")) {
                        sada.setRepairStatus(ApprovalStatus.REPAIRREJECT);
                        saApprovalService.rejectStockAdjustmentDetail(user, sada, null, qty, true);
                        msg = "Selected item cannot be repaired ";
                        operationMsg = "rejected";
                        /**
                         * ERP-37353 posting a JE when Stock adjustment IN type transactions are rejected from the Repair 
                         * Store for perpetual type currently commented for handling additional cases like stock ledger report issue.
                         */
//                        if (preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD && sada.getSaApproval().getStockAdjustment().getAdjustmentType().equalsIgnoreCase("Stock IN")) {
//                            //params for stock out JE posted for QA repair reject in Stock adjustment transactions
//                            JSONObject stockoutparams = new JSONObject();
//                            stockoutparams.put(Constants.moduleid, moduleId);
//                            stockoutparams.put(Constants.userid, sessionHandlerImpl.getUserid(request));
//                            stockoutparams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
//                            stockoutparams.put(Constants.userdateformat, sessionHandlerImpl.getUserDateFormat(request));
//                            stockoutparams.put(Constants.locale, RequestContextUtils.getLocale(request));
//                            stockoutparams.put(Constants.quantity, qty);
//                            stockoutparams.put(Constants.companyid, companyId);                            
//                            stockoutparams.put(Constants.transactionmodule,sada.getSaApproval().getStockAdjustment().getTransactionModule());
//                            stockoutparams.put(Constants.remoteIPAddress,request.getRemoteAddr());
//                            stockoutparams.put("approvaldetail", sada);
//                            stockoutparams.put("recordid", sada.getId());
//                            stockoutparams.put("initialSA", sada.getSaApproval().getStockAdjustment());
//                            stockoutparams.put("moduletype", moduleType);                      
//                            stockoutparams.put("operation", operation);                          
//                            stockoutparams.put("reason", reason);
//                            stockoutparams.put("repairstore", extracompanyobj.getRepairStore());
//                            stockoutparams.put("repairlocation", extracompanyobj.getRepairStore());
//                            stockTransferApprovalService.createStockOutInventoryJEforQAtransaction(stockoutparams);
//                            //Adding product ids here for AOP advisor and IVP valuation JEs to be updated later
//                            if (productIds.indexOf(sada.getStockAdjustmentDetail().getStockAdjustment().getProduct().getID()) == -1) {
//                                productIds.append(sada.getStockAdjustmentDetail().getStockAdjustment().getProduct().getID()).append(",");
//                            }
//                        }
                    }
                    sadApprovalList.add(sada);
                } else if ("goodsreceipt".equals(moduleType)) {
                    /**
                     * Create transaction based on the operation performed in
                     * Stock Repair screen. If user approves GRN then IST will
                     * be created to transfer stock from Repair Store to GRN
                     * Store else if rejected then Stock Out will be created
                     * from repair store.
                     */
                    String transactionNo = "";
                    SeqFormat seqFormat = null;
                    if ("Approve".equalsIgnoreCase(operation) || (Constants.SCRAP_OPERATION.equalsIgnoreCase(operation) && !StringUtil.isNullOrEmpty(scrapstoreid))) {
                        try {
                            seqFormat = seqService.getDefaultSeqFormat(company, ModuleConst.INTER_STORE_TRANSFER);
                            if (seqFormat != null) {
                                transactionNo = seqService.getNextFormatedSeqNumber(seqFormat);
                            } else {
                                throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforistnotset", null, RequestContextUtils.getLocale(request)));
                            }
                        } catch (SeqFormatException ex) {
                            throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforistnotset", null, RequestContextUtils.getLocale(request)));
                        }
                    } else if ("Reject".equalsIgnoreCase(operation)) {
                        try{
                            seqFormat = seqService.getDefaultSeqFormat(company, ModuleConst.STOCK_ADJUSTMENT);
                            if (seqFormat != null) {
                                transactionNo = seqService.getNextFormatedSeqNumber(seqFormat);
                            } else {
                                throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforStockAdjustmentNotSet", null, RequestContextUtils.getLocale(request)));
                            }
                        } catch (SeqFormatException ex) {
                            throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforStockAdjustmentNotSet", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                    Date businessDate = new Date();
                    KwlReturnObject jeresult1 = accountingHandlerDAO.getObject(RepairGRODetailISTMapping.class.getName(), moduleId);
                    RepairGRODetailISTMapping repairGRODetailISTMapping = (RepairGRODetailISTMapping) jeresult1.getEntityList().get(0);
                    if(repairGRODetailISTMapping.getRejectedQuantityDue() <= 0 || repairGRODetailISTMapping.getRejectedQuantityDue() < qty){
                        throw new AccountingException("This item is already processed");
                    }
                    jeresult1 = accountingHandlerDAO.getObject(ISTDetail.class.getName(), recordId);
                    ISTDetail istd = (ISTDetail) jeresult1.getEntityList().get(0);
                    if (repairGRODetailISTMapping != null && istd != null && repairGRODetailISTMapping.getGrodistmapping() != null) {
                        GRODetailISTMapping grodistm = repairGRODetailISTMapping.getGrodistmapping();
                        if (grodistm.getInterStoreTransferRequest() != null) {
                            if(istd.getIssuedQuantity()-(istd.getApprovedQtyFromRepairStore()+istd.getRejectedQtyFromRepairStore()) < qty){
                                throw new InventoryException("This item is already processed");
                            }
                            InterStoreTransferRequest interStoreTransferRequest = grodistm.getInterStoreTransferRequest();
                            Set<ISTDetail> grodistdetails = interStoreTransferRequest.getIstDetails();
                            ISTDetail grodistdetail = null;
                            for (ISTDetail grodistd : grodistdetails) {
                                grodistdetail = grodistd;
                            }
                            JSONObject jsonParams = new JSONObject();
                            jsonParams.put(Constants.companyKey, companyId);
                            jsonParams.put("mappingid", grodistm.getID());
                            String groNumber = stockService.getGoodsReceiptOrderNumberUsingMapping(jsonParams);
                            if (grodistdetail != null && !StringUtil.isNullOrEmpty(groNumber)) {
                                if ("Approve".equalsIgnoreCase(operation) || Constants.SCRAP_OPERATION.equalsIgnoreCase(operation)) {
                                /**
                                 * Create Inter Store Transfer if users approves document from repair store or chooses the reject to scrap store operation.
                                 */
                                    InterStoreTransferRequest createInterStoreTransferRequest = new InterStoreTransferRequest(interStoreTransferRequest.getProduct(), istd.getIstRequest().getToStore(), interStoreTransferRequest.getFromStore(), interStoreTransferRequest.getUom());
                                    createInterStoreTransferRequest.setRemark(reason);
                                    createInterStoreTransferRequest.setAcceptedQty(qty);
                                    createInterStoreTransferRequest.setOrderedQty(qty);
                                    createInterStoreTransferRequest.setBusinessDate(businessDate);
                                    createInterStoreTransferRequest.setPackaging(interStoreTransferRequest.getProduct().getPackaging());
                                    ISTDetail createISTDetail = new ISTDetail();
                                    createISTDetail.setBatchName(istd.getBatchName());
                                    createISTDetail.setDeliveredBin(istd.getIssuedBin());
                                    createISTDetail.setDeliveredQuantity(qty);
                                    createISTDetail.setDeliveredRack(istd.getIssuedRack());
                                    createISTDetail.setDeliveredRow(istd.getIssuedRow());
                                    createISTDetail.setDeliveredSerialNames(istd.getIssuedSerialNames());
                                    createISTDetail.setDeliveredLocation(grodistdetail.getIssuedLocation());
                                    /**
                                     * ERM-691 allow IST to scrap store after GRN stock has been rejected from repair store.
                                     */
                                    if (Constants.SCRAP_OPERATION.equalsIgnoreCase(operation) && !StringUtil.isNullOrEmpty(scrapstoreid)) {
                                        Store scrapstore = storeService.getStoreById(scrapstoreid);
                                        if (scrapstore != null) {
                                            createInterStoreTransferRequest.setToStore(scrapstore);
                                            createISTDetail.setDeliveredLocation(scrapstore.getDefaultLocation());
                                            createInterStoreTransferRequest.setMemo("Stock Sent to Scrap for GRN: " + groNumber);
                                            createInterStoreTransferRequest.setRepairGRODetailISTMapping(repairGRODetailISTMapping);
                                        }
                                    }
                                    createISTDetail.setIssuedBin(istd.getDeliveredBin());
                                    createISTDetail.setIssuedLocation(istd.getDeliveredLocation());
                                    createISTDetail.setIssuedQuantity(qty);
                                    createISTDetail.setIssuedRack(istd.getDeliveredRack());
                                    createISTDetail.setIssuedRow(istd.getDeliveredRow());
                                    createISTDetail.setIssuedSerialNames(istd.getDeliveredSerialNames());
                                    createISTDetail.setIstRequest(createInterStoreTransferRequest);
                                    createInterStoreTransferRequest.getIstDetails().add(createISTDetail);
                                    createInterStoreTransferRequest.setTransactionNo(transactionNo);
                                    istd.setApprovedQtyFromRepairStore(istd.getApprovedQtyFromRepairStore()+ qty);
                                    if (!StringUtil.isNullOrEmpty(groNumber) && !Constants.SCRAP_OPERATION.equalsIgnoreCase(operation)) {
                                        createInterStoreTransferRequest.setMemo("Repair Stock for GRN: " + groNumber);
                                        createInterStoreTransferRequest.setRepairGRODetailISTMapping(repairGRODetailISTMapping);
                                        Map<String, Object> requestParams = new HashMap<>();
                                        requestParams.put("repairid", repairGRODetailISTMapping.getID());
                                        requestParams.put("rejectedQtyDue", repairGRODetailISTMapping.getRejectedQuantityDue() - qty);
                                        interStoreTransferService.addInterStoreTransferRequest(user, createInterStoreTransferRequest, false, requestParams);
                                        interStoreTransferService.acceptInterStoreTransferRequest(user, createInterStoreTransferRequest);
                                        seqService.updateSeqNumber(seqFormat);
                                        auditMessage = "repaired Goods Receipt Note: " + groNumber;
                                        auditMessage = "User " + user.getFullName() + " has " + auditMessage;
                                        msg = "Selected item repaired Successfully.";
                                        auditTrailObj.insertAuditLog(AuditAction.QA_INSPECTION, auditMessage, request, "0");
                                    } 
                                    /**
                                     * ERM-691 Scrap Store flow for GRN Repair Stock preparing audit trail message.
                                     */
                                    else if (!StringUtil.isNullOrEmpty(groNumber) && Constants.SCRAP_OPERATION.equalsIgnoreCase(operation) && extracompanyobj.isActivateQAApprovalFlow()) {
                                        Product product = interStoreTransferRequest.getProduct();
                                        String StUom = interStoreTransferRequest.getUom() != null ? interStoreTransferRequest.getUom().getNameEmptyforNA() : "";

                                        Map<String, Object> requestParams = new HashMap<>();
                                        requestParams.put("repairid", repairGRODetailISTMapping.getID());
                                        requestParams.put("rejectedQtyDue", repairGRODetailISTMapping.getRejectedQuantityDue() - qty);
                                        interStoreTransferService.addInterStoreTransferRequest(user, createInterStoreTransferRequest, false, requestParams);
                                        interStoreTransferService.acceptInterStoreTransferRequest(user, createInterStoreTransferRequest);
                                        seqService.updateSeqNumber(seqFormat);
                                        auditMessage = "(Product :" + product.getProductid();
                                        if (!StringUtil.isNullOrEmpty(istd.getBatchName())) {
                                            auditMessage += " Batch :" + istd.getBatchName();
                                        }
                                        if (!StringUtil.isNullOrEmpty(istd.getDeliveredSerialNames())) {
                                            auditMessage += " Serials :" + istd.getDeliveredSerialNames();
                                        }
                                        auditMessage = auditMessage +", Quantity :" + (qty) + " " + StUom + ")";
                                        auditMessage = "User " + user.getFullName() + " has sent the Stock to Store: " + createISTDetail.getIstRequest().getToStore().getAbbreviation() + " for Goods Receipt Note:" + groNumber + auditMessage;
                                        msg = messageSource.getMessage("acc.qaapproval.scrapstoresuccess", null, RequestContextUtils.getLocale(request));
                                        requestParams.put(Constants.useridKey,user.getUserID());
                                        requestParams.put(Constants.companyKey,companyId);
                                        requestParams.put(Constants.remoteIPAddress,request.getRemoteAddr());
                                        auditTrailObj.insertAuditLog(AuditAction.QA_INSPECTION, auditMessage, requestParams, "0");
                                    }
                                } else if ("Reject".equalsIgnoreCase(operation)) {
                                    /**
                                     * Create Stock Adjustment OUT if users
                                     * reject document from repair store.
                                     */
                                    istd.setRejectedQtyFromRepairStore(istd.getRejectedQtyFromRepairStore() + qty);
                                    Product product = interStoreTransferRequest.getProduct();
                                    double productPrice = stockService.getProductPurchasePrice(product, businessDate);
                                    StockAdjustment createStockAdjustment = new StockAdjustment(product, istd.getIstRequest().getToStore(), istd.getIstRequest().getUom(), -qty, productPrice, businessDate);
                                    createStockAdjustment.setAdjustmentType("Stock Out");
                                    createStockAdjustment.setTransactionNo(transactionNo);
                                    createStockAdjustment.setMemo("Rejected Stock for GRN: " + groNumber);
                                    createStockAdjustment.setRemark("Rejected Stock for GRN: " + groNumber);
                                    createStockAdjustment.setReason(reason);
                                    createStockAdjustment.setCompany(company);
                                    createStockAdjustment.setCreatedOn(businessDate);
                                    createStockAdjustment.setCreationdate(businessDate.getTime());
                                    createStockAdjustment.setTransactionNo(transactionNo);
                                    createStockAdjustment.setTransactionModule(TransactionModule.STOCK_ADJUSTMENT);
                                    createStockAdjustment.setRejectedRepairGRODetailISTMapping(repairGRODetailISTMapping);
                                    Set<StockAdjustmentDetail> adjustmentDetailSet = new HashSet<StockAdjustmentDetail>();
                                    StockAdjustmentDetail stockAdjustmentDetail = new StockAdjustmentDetail();
                                    stockAdjustmentDetail.setBatchName(istd.getBatchName());
                                    stockAdjustmentDetail.setBin(istd.getDeliveredBin());
                                    stockAdjustmentDetail.setRack(istd.getDeliveredRack());
                                    stockAdjustmentDetail.setRow(istd.getDeliveredRow());
                                    stockAdjustmentDetail.setLocation(istd.getDeliveredLocation());
                                    stockAdjustmentDetail.setFinalQuantity(qty);
                                    stockAdjustmentDetail.setQuantity(qty);
                                    stockAdjustmentDetail.setFinalSerialNames(istd.getDeliveredSerialNames());
                                    stockAdjustmentDetail.setSerialNames(istd.getDeliveredSerialNames());
                                    stockAdjustmentDetail.setStockAdjustment(createStockAdjustment);
                                    adjustmentDetailSet.add(stockAdjustmentDetail);
                                    createStockAdjustment.setStockAdjustmentDetail(adjustmentDetailSet);
                                    JournalEntry inventoryJE = null;
                                    if (extracompanyobj.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) {
                                        if (product.getInventoryAccount() != null) {
                                            Map<String, Object> JEFormatParams = new HashMap<>();
                                            JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                            JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                                            JEFormatParams.put("companyid", companyId);
                                            JEFormatParams.put("isdefaultFormat", true);
                                            KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                            if (kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty()) {
                                                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                                if (format != null) {
                                                    Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                                                    Map<String, Object> seqNumberMap = new HashMap<>();
                                                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, businessDate);
                                                    jeDataMap.put("entrynumber", (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER));
                                                    jeDataMap.put("autogenerated", true);
                                                    jeDataMap.put(Constants.SEQFORMAT, format.getID());
                                                    jeDataMap.put(Constants.SEQNUMBER, (String) seqNumberMap.get(Constants.SEQNUMBER));
                                                    jeDataMap.put(Constants.DATEPREFIX, (String) seqNumberMap.get(Constants.DATEPREFIX));
                                                    jeDataMap.put(Constants.DATEAFTERPREFIX, (String) seqNumberMap.get(Constants.DATEAFTERPREFIX));
                                                    jeDataMap.put(Constants.DATESUFFIX, (String) seqNumberMap.get(Constants.DATESUFFIX));
                                                    jeDataMap.put("entrydate", businessDate);
                                                    jeDataMap.put("companyid", companyId);
                                                    jeDataMap.put("memo", "Stock Adjustment JE for " + product.getName());
                                                    jeDataMap.put("createdby", sessionHandlerImpl.getUserid(request));
                                                    jeDataMap.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
                                                    jeDataMap.put("transactionModuleid", Constants.Inventory_Stock_Adjustment_ModuleId);
                                                    jeresult = journalEntryDAO.saveJournalEntry(jeDataMap); // Create Journal entry without JEdetails
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
                                                    KwlReturnObject jedresult = journalEntryDAO.addJournalEntryDetails(jedjson);
                                                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                                    jeDetails.add(jed);
                                                    jedjson = new JSONObject();
                                                    jedjson.put("srno", jeDetails.size() + 1);
                                                    jedjson.put("companyid", companyId);
                                                    jedjson.put("amount", authHandler.round(((qty * productPrice) * (-1)), companyId));
                                                    jedjson.put("accountid", product.getStockAdjustmentAccount().getID());
                                                    jedjson.put("debit", true);
                                                    jedjson.put("jeid", inventoryJE.getID());
                                                    jedresult = journalEntryDAO.addJournalEntryDetails(jedjson);
                                                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                                    jeDetails.add(jed);
                                                    inventoryJE.setDetails(jeDetails);
                                                    journalEntryDAO.saveJournalEntryDetailsSet(jeDetails);
                                                } else {
                                                    throw new AccountingException("No default Sequence Format found. Please add a default format for Journal Entry.");
                                                }
                                            } else {
                                                   throw new AccountingException("No default Sequence Format found. Please add a default format for Journal Entry.");
                                            }
                                        } else {
                                            throw new AccountingException("Please set inventory account for product: " + product.getProductid());
                                        }
                                    }
                                    HashMap<String, Object> requestparams = new HashMap<>();
                                    requestparams.put("locale", RequestContextUtils.getLocale(request));
                                    stockAdjustmentService.requestStockAdjustment(user, createStockAdjustment, false, false, null, requestparams);
                                    if (inventoryJE != null) {
                                        inventoryJE.setTransactionId(createStockAdjustment.getId());
                                    }
                                    seqService.updateSeqNumber(seqFormat);
                                    JSONObject repairParams = new JSONObject();
                                    repairParams.put("repairid", repairGRODetailISTMapping.getID());
                                    repairParams.put("rejectedStockOutRequest", createStockAdjustment);
                                    repairParams.put("rejectedQtyDue", repairGRODetailISTMapping.getRejectedQuantityDue() - qty);
                                    interStoreTransferService.saveRepairGRODetailISTMapping(repairParams, null);
                                    msg = "Selected item rejected successfully.";
                                    String StUom = createStockAdjustment.getUom() != null ? createStockAdjustment.getUom().getNameEmptyforNA() : "";
                                    auditMessage = "(Product :" + product.getProductid() + ", Quantity :" + (-qty) + " " + StUom + ", AdjustmentType : " + "Stock Out " + ")";
                                    auditMessage = "User " + user.getFullName() + " has created Stock Adjustment: " + transactionNo + " for Store: " + istd.getIstRequest().getToStore().getAbbreviation() + " " + auditMessage;
                                    auditTrailObj.insertAuditLog(AuditAction.STOCK_ADJUSTMENT_ADDED, auditMessage, request, "0");
                                    if (productIds.indexOf(product.getID()) == -1) {
                                        productIds.append(product.getID()).append(",");
                                    }
                                }
                            }
                        }
                    }
                } else if (Constants.MRP_WORK_ORDER.equals(moduleType)) {
                    /**
                     * Create transaction based on the operation performed in
                     * Stock Repair screen. If user approves WO then IST will
                     * be created to transfer stock from Repair Store to WO
                     * Store else if rejected then Stock Out will be created
                     * from repair store.
                     */
                    String transactionNo = "";
                    SeqFormat seqFormat = null;
                    if ("Approve".equalsIgnoreCase(operation) || (Constants.SCRAP_OPERATION.equalsIgnoreCase(operation) && !StringUtil.isNullOrEmpty(scrapstoreid))) {
                        try {
                            seqFormat = seqService.getDefaultSeqFormat(company, ModuleConst.INTER_STORE_TRANSFER);
                            if (seqFormat != null) {
                                transactionNo = seqService.getNextFormatedSeqNumber(seqFormat);
                            } else {
                                throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforistnotset", null, RequestContextUtils.getLocale(request)));
                            }
                        } catch (SeqFormatException ex) {
                            throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforistnotset", null, RequestContextUtils.getLocale(request)));
                        }
                    } else if ("Reject".equalsIgnoreCase(operation)) {
                        try{
                            seqFormat = seqService.getDefaultSeqFormat(company, ModuleConst.STOCK_ADJUSTMENT);
                            if (seqFormat != null) {
                                transactionNo = seqService.getNextFormatedSeqNumber(seqFormat);
                            } else {
                                throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforStockAdjustmentNotSet", null, RequestContextUtils.getLocale(request)));
                            }
                        } catch (SeqFormatException ex) {
                            throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforStockAdjustmentNotSet", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                    Date businessDate = new Date();
                    KwlReturnObject jeresult1 = accountingHandlerDAO.getObject(RepairWOCDISTMapping.class.getName(), moduleId);
                    RepairWOCDISTMapping repairWOCDetailISTMapping = (RepairWOCDISTMapping) jeresult1.getEntityList().get(0);
                    jeresult1 = accountingHandlerDAO.getObject(ISTDetail.class.getName(), recordId);
                    ISTDetail istd = (ISTDetail) jeresult1.getEntityList().get(0);
                    if (repairWOCDetailISTMapping != null && istd != null && repairWOCDetailISTMapping.getWocdistmapping() != null) {
                        WOCDetailISTMapping wocdistm = repairWOCDetailISTMapping.getWocdistmapping();
                        if (wocdistm.getInterStoreTransferRequest() != null) {
                            InterStoreTransferRequest interStoreTransferRequest = wocdistm.getInterStoreTransferRequest();
                            Set<ISTDetail> wocdistdetails = interStoreTransferRequest.getIstDetails();
                            ISTDetail wocdistdetail = null;
                            for (ISTDetail wocdistd : wocdistdetails) {
                                wocdistdetail = wocdistd;
                            }
                            JSONObject jsonParams = new JSONObject();
                            jsonParams.put(Constants.companyKey, companyId);
                            jsonParams.put("wocdistmapping", wocdistm.getID());
                            String woNumber = stockService.getWorkOrderNumberUsingMapping(jsonParams);
                            if (wocdistdetail != null && !StringUtil.isNullOrEmpty(woNumber)) {
                                if ("Approve".equalsIgnoreCase(operation) || Constants.SCRAP_OPERATION.equalsIgnoreCase(operation)) {
                                /**
                                 * Create Inter Store Transfer if users approves document from repair store or chooses the reject to scrap store operation.
                                 */
                                    InterStoreTransferRequest createInterStoreTransferRequest = new InterStoreTransferRequest(interStoreTransferRequest.getProduct(), istd.getIstRequest().getToStore(), interStoreTransferRequest.getFromStore(), interStoreTransferRequest.getUom());
                                    createInterStoreTransferRequest.setRemark(reason);
                                    createInterStoreTransferRequest.setAcceptedQty(qty);
                                    createInterStoreTransferRequest.setOrderedQty(qty);
                                    createInterStoreTransferRequest.setBusinessDate(businessDate);
                                    createInterStoreTransferRequest.setPackaging(interStoreTransferRequest.getProduct().getPackaging());
                                    ISTDetail createISTDetail = new ISTDetail();
                                    createISTDetail.setBatchName(istd.getBatchName());
                                    createISTDetail.setDeliveredBin(istd.getIssuedBin());
                                    createISTDetail.setDeliveredQuantity(qty);
                                    createISTDetail.setDeliveredRack(istd.getIssuedRack());
                                    createISTDetail.setDeliveredRow(istd.getIssuedRow());
                                    createISTDetail.setDeliveredSerialNames(istd.getIssuedSerialNames());
                                    createISTDetail.setDeliveredLocation(wocdistdetail.getIssuedLocation());
                                    /**
                                     * ERM-691 allow IST to scrap store after WO stock has been rejected from repair store.
                                     */
                                    if (Constants.SCRAP_OPERATION.equalsIgnoreCase(operation) && !StringUtil.isNullOrEmpty(scrapstoreid)) {
                                        Store scrapstore = storeService.getStoreById(scrapstoreid);
                                        if (scrapstore != null) {
                                            createInterStoreTransferRequest.setToStore(scrapstore);
                                            createISTDetail.setDeliveredLocation(scrapstore.getDefaultLocation());
                                            createInterStoreTransferRequest.setMemo("Stock Sent to Scrap for Work Order: " + woNumber);
                                            createInterStoreTransferRequest.setRepairWOCDISTMapping(repairWOCDetailISTMapping);
                                        }
                                    }
                                    createISTDetail.setIssuedBin(istd.getDeliveredBin());
                                    createISTDetail.setIssuedLocation(istd.getDeliveredLocation());
                                    createISTDetail.setIssuedQuantity(qty);
                                    createISTDetail.setIssuedRack(istd.getDeliveredRack());
                                    createISTDetail.setIssuedRow(istd.getDeliveredRow());
                                    createISTDetail.setIssuedSerialNames(istd.getDeliveredSerialNames());
                                    createISTDetail.setIstRequest(createInterStoreTransferRequest);
                                    createInterStoreTransferRequest.getIstDetails().add(createISTDetail);
                                    createInterStoreTransferRequest.setTransactionNo(transactionNo);
                                    istd.setApprovedQtyFromRepairStore(istd.getApprovedQtyFromRepairStore()+ qty);
                                    if (!StringUtil.isNullOrEmpty(woNumber) && !Constants.SCRAP_OPERATION.equalsIgnoreCase(operation)) {
                                        createInterStoreTransferRequest.setMemo("Repair Stock for Work Order: " + woNumber);
                                        createInterStoreTransferRequest.setRepairWOCDISTMapping(repairWOCDetailISTMapping);
                                        Map<String, Object> requestParams = new HashMap<>();
                                        requestParams.put("repairid", repairWOCDetailISTMapping.getID());
                                        requestParams.put("rejectedQtyDue", repairWOCDetailISTMapping.getRejectedQuantityDue() - qty);
                                        requestParams.put(Constants.CREATE_IST_FOR_QC_WORKORDER, true);
                                        interStoreTransferService.addInterStoreTransferRequest(user, createInterStoreTransferRequest, false, requestParams);
                                        interStoreTransferService.acceptInterStoreTransferRequest(user, createInterStoreTransferRequest);
                                        seqService.updateSeqNumber(seqFormat);
                                        auditMessage = "repaired Work Order: " + woNumber;
                                        auditMessage = "User " + user.getFullName() + " has " + auditMessage;
                                        msg = "Selected item repaired Successfully.";
                                        auditTrailObj.insertAuditLog(AuditAction.QA_INSPECTION, auditMessage, request, "0");
                                        /**
                                        * Added productIds to update IST price based on the
                                        * valuation done in AOP advisor.
                                        */ 
                                        if (productIds.indexOf(interStoreTransferRequest.getProduct().getID()) == -1) {
                                            productIds.append(interStoreTransferRequest.getProduct().getID()).append(",");
                                        }
                                    } 
                                    /**
                                     * ERM-691 Scrap Store flow for WO Repair Stock preparing audit trail message.
                                     */
                                    else if (!StringUtil.isNullOrEmpty(woNumber) && Constants.SCRAP_OPERATION.equalsIgnoreCase(operation) && extracompanyobj.isActivateQAApprovalFlow()) {
                                        Product product = interStoreTransferRequest.getProduct();
                                        String StUom = interStoreTransferRequest.getUom() != null ? interStoreTransferRequest.getUom().getNameEmptyforNA() : "";

                                        Map<String, Object> requestParams = new HashMap<>();
                                        requestParams.put("repairid", repairWOCDetailISTMapping.getID());
                                        requestParams.put("rejectedQtyDue", repairWOCDetailISTMapping.getRejectedQuantityDue() - qty);
                                        requestParams.put(Constants.CREATE_IST_FOR_QC_WORKORDER, true);
                                        interStoreTransferService.addInterStoreTransferRequest(user, createInterStoreTransferRequest, false, requestParams);
                                        interStoreTransferService.acceptInterStoreTransferRequest(user, createInterStoreTransferRequest);
                                        seqService.updateSeqNumber(seqFormat);
                                        auditMessage = "(Product :" + product.getProductid();
                                        if (!StringUtil.isNullOrEmpty(istd.getBatchName())) {
                                            auditMessage += " Batch :" + istd.getBatchName();
                                        }
                                        if (!StringUtil.isNullOrEmpty(istd.getDeliveredSerialNames())) {
                                            auditMessage += " Serials :" + istd.getDeliveredSerialNames();
                                        }
                                        auditMessage = auditMessage +", Quantity :" + (qty) + " " + StUom + ")";
                                        auditMessage = "User " + user.getFullName() + " has sent the Stock to Store: " + createISTDetail.getIstRequest().getToStore().getAbbreviation() + " for Work Order:" + woNumber + auditMessage;
                                        msg = messageSource.getMessage("acc.qaapproval.scrapstoresuccess", null, RequestContextUtils.getLocale(request));
                                        requestParams.put(Constants.useridKey,user.getUserID());
                                        requestParams.put(Constants.companyKey,companyId);
                                        requestParams.put(Constants.remoteIPAddress,request.getRemoteAddr());
                                        auditTrailObj.insertAuditLog(AuditAction.QA_INSPECTION, auditMessage, requestParams, "0");
                                    }
                                } else if ("Reject".equalsIgnoreCase(operation)) {
                                    /**
                                     * Create Stock Adjustment OUT if users
                                     * reject document from repair store.
                                     */
                                    istd.setRejectedQtyFromRepairStore(istd.getRejectedQtyFromRepairStore() + qty);
                                    Product product = interStoreTransferRequest.getProduct();
                                    double productPrice = stockService.getProductPurchasePrice(product, businessDate);
                                    StockAdjustment createStockAdjustment = new StockAdjustment(product, istd.getIstRequest().getToStore(), istd.getIstRequest().getUom(), -qty, productPrice, businessDate);
                                    createStockAdjustment.setAdjustmentType("Stock Out");
                                    createStockAdjustment.setTransactionNo(transactionNo);
                                    createStockAdjustment.setMemo("Rejected Stock for Work Order: " + woNumber);
                                    createStockAdjustment.setRemark(reason);
                                    createStockAdjustment.setReason(reason);
                                    createStockAdjustment.setCompany(company);
                                    createStockAdjustment.setCreatedOn(businessDate);
                                    createStockAdjustment.setCreationdate(businessDate.getTime());
                                    createStockAdjustment.setTransactionNo(transactionNo);
                                    createStockAdjustment.setTransactionModule(TransactionModule.STOCK_ADJUSTMENT);
                                    createStockAdjustment.setRejectedRepairWOCDetailISTMapping(repairWOCDetailISTMapping);
                                    Set<StockAdjustmentDetail> adjustmentDetailSet = new HashSet<StockAdjustmentDetail>();
                                    StockAdjustmentDetail stockAdjustmentDetail = new StockAdjustmentDetail();
                                    stockAdjustmentDetail.setBatchName(istd.getBatchName());
                                    stockAdjustmentDetail.setBin(istd.getDeliveredBin());
                                    stockAdjustmentDetail.setRack(istd.getDeliveredRack());
                                    stockAdjustmentDetail.setRow(istd.getDeliveredRow());
                                    stockAdjustmentDetail.setLocation(istd.getDeliveredLocation());
                                    stockAdjustmentDetail.setFinalQuantity(qty);
                                    stockAdjustmentDetail.setQuantity(qty);
                                    stockAdjustmentDetail.setFinalSerialNames(istd.getDeliveredSerialNames());
                                    stockAdjustmentDetail.setSerialNames(istd.getDeliveredSerialNames());
                                    stockAdjustmentDetail.setStockAdjustment(createStockAdjustment);
                                    adjustmentDetailSet.add(stockAdjustmentDetail);
                                    createStockAdjustment.setStockAdjustmentDetail(adjustmentDetailSet);
                                    JournalEntry inventoryJE = null;
                                    if (extracompanyobj.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) {
                                        if (product.getInventoryAccount() != null) {
                                            Map<String, Object> JEFormatParams = new HashMap<>();
                                            JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                            JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                                            JEFormatParams.put("companyid", companyId);
                                            JEFormatParams.put("isdefaultFormat", true);
                                            KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                            if (kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty()) {
                                                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                                if (format != null) {
                                                    Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                                                    Map<String, Object> seqNumberMap = new HashMap<>();
                                                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, businessDate);
                                                    jeDataMap.put("entrynumber", (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER));
                                                    jeDataMap.put("autogenerated", true);
                                                    jeDataMap.put(Constants.SEQFORMAT, format.getID());
                                                    jeDataMap.put(Constants.SEQNUMBER, (String) seqNumberMap.get(Constants.SEQNUMBER));
                                                    jeDataMap.put(Constants.DATEPREFIX, (String) seqNumberMap.get(Constants.DATEPREFIX));
                                                    jeDataMap.put(Constants.DATEAFTERPREFIX, (String) seqNumberMap.get(Constants.DATEAFTERPREFIX));
                                                    jeDataMap.put(Constants.DATESUFFIX, (String) seqNumberMap.get(Constants.DATESUFFIX));
                                                    jeDataMap.put("entrydate", businessDate);
                                                    jeDataMap.put("companyid", companyId);
                                                    jeDataMap.put("memo", "Stock Adjustment JE for " + product.getName());
                                                    jeDataMap.put("createdby", sessionHandlerImpl.getUserid(request));
                                                    jeDataMap.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
                                                    jeDataMap.put("transactionModuleid", Constants.Inventory_Stock_Adjustment_ModuleId);
                                                    jeresult = journalEntryDAO.saveJournalEntry(jeDataMap); // Create Journal entry without JEdetails
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
                                                    KwlReturnObject jedresult = journalEntryDAO.addJournalEntryDetails(jedjson);
                                                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                                    jeDetails.add(jed);
                                                    jedjson = new JSONObject();
                                                    jedjson.put("srno", jeDetails.size() + 1);
                                                    jedjson.put("companyid", companyId);
                                                    jedjson.put("amount", authHandler.round(((qty * productPrice) * (-1)), companyId));
                                                    jedjson.put("accountid", product.getStockAdjustmentAccount().getID());
                                                    jedjson.put("debit", true);
                                                    jedjson.put("jeid", inventoryJE.getID());
                                                    jedresult = journalEntryDAO.addJournalEntryDetails(jedjson);
                                                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                                    jeDetails.add(jed);
                                                    inventoryJE.setDetails(jeDetails);
                                                    journalEntryDAO.saveJournalEntryDetailsSet(jeDetails);
                                                } else {
                                                    throw new AccountingException("No default Sequence Format found. Please add a default format for Journal Entry.");
                                                }
                                            } else {
                                                   throw new AccountingException("No default Sequence Format found. Please add a default format for Journal Entry.");
                                            }
                                        } else {
                                            throw new AccountingException("Please set inventory account for product: " + product.getProductid());
                                        }
                                    }
                                    HashMap<String, Object> requestparams = new HashMap<>();
                                    requestparams.put("locale", RequestContextUtils.getLocale(request));
                                    stockAdjustmentService.requestStockAdjustment(user, createStockAdjustment, false, false, null, requestparams);
                                    if (inventoryJE != null) {
                                        inventoryJE.setTransactionId(createStockAdjustment.getId());
                                    }
                                    seqService.updateSeqNumber(seqFormat);
                                    JSONObject repairParams = new JSONObject();
                                    repairParams.put("repairid", repairWOCDetailISTMapping.getID());
                                    repairParams.put("rejectedStockOutRequest", createStockAdjustment);
                                    repairParams.put("rejectedQtyDue", repairWOCDetailISTMapping.getRejectedQuantityDue() - qty);
                                    interStoreTransferService.saveRepairWOCDISTMapping(repairParams, null);
                                    msg = "Selected item rejected successfully.";
                                    String StUom = createStockAdjustment.getUom() != null ? createStockAdjustment.getUom().getNameEmptyforNA() : "";
                                    auditMessage = "(Product :" + product.getProductid() + ", Quantity :" + (-qty) + " " + StUom + ", AdjustmentType : " + "Stock Out " + ")";
                                    auditMessage = "User " + user.getFullName() + " has created Stock Adjustment: " + transactionNo + " for Store: " + istd.getIstRequest().getToStore().getAbbreviation() + " " + auditMessage;
                                    auditTrailObj.insertAuditLog(AuditAction.STOCK_ADJUSTMENT_ADDED, auditMessage, request, "0");
                                    if (productIds.indexOf(product.getID()) == -1) {
                                        productIds.append(product.getID()).append(",");
                                    }
                                }
                            }
                        }
                    }
                } else if ("deliveryorder".equals(moduleType)) {
                    String stockAdjustmentTransactionNo = "";
                    SeqFormat seqFormat = null;
                    if (extracompanyobj.isPickpackship() && "Approve".equalsIgnoreCase(operation)) {
                        try {
                            seqFormat = seqService.getDefaultSeqFormat(company, ModuleConst.INTER_STORE_TRANSFER);
                            if (seqFormat != null) {
                                stockAdjustmentTransactionNo = seqService.getNextFormatedSeqNumber(seqFormat);
                            } else {
                                throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforistnotset", null, RequestContextUtils.getLocale(request)));
                            }
                        } catch (SeqFormatException ex) {
                            throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforistnotset", null, RequestContextUtils.getLocale(request)));
                        }
                    } else {
                        try {
                            seqFormat = seqService.getDefaultSeqFormat(company, ModuleConst.STOCK_ADJUSTMENT);
                            if (seqFormat != null) {
                                stockAdjustmentTransactionNo = seqService.getNextFormatedSeqNumber(seqFormat);
                            } else {
                                throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforStockAdjustmentNotSet", null, RequestContextUtils.getLocale(request)));
                            }
                        } catch (SeqFormatException ex) {
                            throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforStockAdjustmentNotSet", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                    if ("Approve".equalsIgnoreCase(operation) && extracompanyobj.isPickpackship()) {
                        if (packingStore == null) {
                            throw new AccountingException("Packing Store is not set in Company Preferences.");
                        }
                        if (packingStore.getDefaultLocation() == null) {
                            throw new AccountingException("Default location is not set for packaging store.");
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(stockAdjustmentTransactionNo) && seqFormat != null) {
                        Date businessDate = new Date();
                        KwlReturnObject jeresult1 = accountingHandlerDAO.getObject(RejectedDODQCISTMapping.class.getName(), moduleId);
                        RejectedDODQCISTMapping rejectedDODQCISTMapping = (RejectedDODQCISTMapping) jeresult1.getEntityList().get(0);
                        jeresult1 = accountingHandlerDAO.getObject(ISTDetail.class.getName(), recordId);
                        ISTDetail istd = (ISTDetail) jeresult1.getEntityList().get(0);
                        JSONObject jsonParams = new JSONObject();
                        jsonParams.put(Constants.companyKey, company.getCompanyID());
                        jsonParams.put("mappingid", rejectedDODQCISTMapping.getDodqcistmapping().getID());
                        String doNumber = stockService.getDeliveryOrderNumberUsingMapping(jsonParams);
                        InterStoreTransferRequest interStoreTransferRequest = istd.getIstRequest();
                        boolean isQtyAlreadyApprovedorRejected = false ;
                        double totalQuantityDue = rejectedDODQCISTMapping.getQuantityDue();
                        if (totalQuantityDue <= 0 || totalQuantityDue < qty){
                            isQtyAlreadyApprovedorRejected= true;
                        }
                        if (extracompanyobj.isPickpackship() && "Approve".equalsIgnoreCase(operation)) {
                            /**
                             * If Pick, Pack and Ship flow is activated for the
                             * company then add an entry in
                             * DeliveryDetailInterStoreLocationMapping into
                             * packing warehouse.
                             */
                            /**
                             * Create Inter Store Transfer if users approves
                             * document from repair store.
                             */
                            istd.setApprovedQtyFromRepairStore(istd.getApprovedQtyFromRepairStore() + qty);
                            InterStoreTransferRequest createInterStoreTransferRequest = new InterStoreTransferRequest(interStoreTransferRequest.getProduct(), istd.getIstRequest().getToStore(), packingStore, interStoreTransferRequest.getUom());
                            createInterStoreTransferRequest.setRemark(reason);
                            createInterStoreTransferRequest.setAcceptedQty(qty);
                            createInterStoreTransferRequest.setOrderedQty(qty);
                            createInterStoreTransferRequest.setBusinessDate(businessDate);
                            createInterStoreTransferRequest.setPackaging(interStoreTransferRequest.getProduct().getPackaging());
                            createInterStoreTransferRequest.setMemo("Repaired Stock for DO: " + doNumber);
                            ISTDetail createISTDetail = new ISTDetail();
                            createISTDetail.setBatchName(istd.getBatchName());
                            createISTDetail.setDeliveredBin(istd.getIssuedBin());
                            createISTDetail.setDeliveredQuantity(qty);
                            createISTDetail.setDeliveredRack(istd.getIssuedRack());
                            createISTDetail.setDeliveredRow(istd.getIssuedRow());
                            createISTDetail.setDeliveredSerialNames(istd.getIssuedSerialNames());
                            createISTDetail.setDeliveredLocation(packingStore.getDefaultLocation());
                            createISTDetail.setIssuedBin(istd.getDeliveredBin());
                            createISTDetail.setIssuedLocation(istd.getDeliveredLocation());
                            createISTDetail.setIssuedQuantity(qty);
                            createISTDetail.setIssuedRack(istd.getDeliveredRack());
                            createISTDetail.setIssuedRow(istd.getDeliveredRow());
                            createISTDetail.setIssuedSerialNames(istd.getDeliveredSerialNames());
                            createISTDetail.setIstRequest(createInterStoreTransferRequest);
                            createInterStoreTransferRequest.getIstDetails().add(createISTDetail);
                            createInterStoreTransferRequest.setTransactionNo(stockAdjustmentTransactionNo);
                            if (!StringUtil.isNullOrEmpty(doNumber)) {
                                HashMap<String, Object> requestParams = new HashMap<>();
                                requestParams.put(Constants.CREATE_PICK_IST_FOR_APPROVE_DELIVERYORDER_FROM_REPAIR_STORE, true);
                                requestParams.put("dod", rejectedDODQCISTMapping.getDodqcistmapping().getDodetailID());
                                requestParams.put("pickedQty", rejectedDODQCISTMapping.getPickedQty() + qty);
                                requestParams.put("quantityDue", rejectedDODQCISTMapping.getQuantityDue() - qty);
                                requestParams.put("repairid", rejectedDODQCISTMapping.getID());
                                requestParams.put("pickRejectedDODQCISTMapping", rejectedDODQCISTMapping);
                                interStoreTransferService.addInterStoreTransferRequest(user, createInterStoreTransferRequest, false, requestParams);
                                interStoreTransferService.acceptInterStoreTransferRequest(user, createInterStoreTransferRequest);
                                seqService.updateSeqNumber(seqFormat);
                                auditMessage = "repaired Delivery Order: " + doNumber;
                                auditMessage = "User " + user.getFullName() + " has " + auditMessage;
                                msg = "Selected item repaired Successfully.";
                                auditTrailObj.insertAuditLog(AuditAction.QA_INSPECTION, auditMessage, request, "0");
                            }
                            /*
                             HashMap<String, Object> filterRequestParams = new HashMap<>();
                             ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                             filter_names.add("company.companyID");
                             filter_params.add(company.getCompanyID());
                             filter_names.add("masterGroup.ID");
                             filter_params.add("10"); // For getting DO status ID
                             filter_names.add("value");
                             filter_params.add("Picked");
                             filterRequestParams.put("filter_names", filter_names);
                             filterRequestParams.put("filter_params", filter_params);
                             KwlReturnObject retObj = accMasterItemsDAO.getMasterItems(filterRequestParams);
                             if (retObj != null && !retObj.getEntityList().isEmpty()) {
                             MasterItem doStatus = (MasterItem) retObj.getEntityList().get(0);
                             String statusID = doStatus.getID();
                             if (!StringUtil.isNullOrEmpty(statusID)) {
                             JSONObject doParams = new JSONObject();
                             doParams.put(Constants.companyKey, company.getCompanyID());
                             doParams.put("dodetailid", rejectedDODQCISTMapping.getDodqcistmapping().getDodetailID());
                             doParams.put("statusID", statusID);
                             stockService.updateDeliveryOrderStatus(doParams);
                             }
                             }*/
                        } else {
                            if(isQtyAlreadyApprovedorRejected){
                                throw new AccountingException("This item is already processed");
                            }else if(istd.getIssuedQuantity()-(istd.getApprovedQtyFromRepairStore()+istd.getRejectedQtyFromRepairStore()) < qty){
                                throw new InventoryException("This item is already processed");
                            }
                            /**
                             * Create Stock Adjustment OUT if users reject
                             * document from repair store.
                             */
                            Product product = interStoreTransferRequest.getProduct();
                            double productPrice = stockService.getProductPurchasePrice(product, businessDate);
                            StockAdjustment createStockAdjustment = new StockAdjustment(product, istd.getIstRequest().getToStore(), istd.getIstRequest().getUom(), -qty, productPrice, businessDate);
                            createStockAdjustment.setAdjustmentType("Stock Out");
                            createStockAdjustment.setTransactionNo(stockAdjustmentTransactionNo);
                            createStockAdjustment.setReason(reason);
                            createStockAdjustment.setCompany(company);
                            createStockAdjustment.setCreatedOn(businessDate);
                            createStockAdjustment.setCreationdate(businessDate.getTime());
                            createStockAdjustment.setTransactionNo(stockAdjustmentTransactionNo);
                            createStockAdjustment.setTransactionModule(TransactionModule.STOCK_ADJUSTMENT);
                            if ("Approve".equalsIgnoreCase(operation)) {
                                createStockAdjustment.setRejectedApprovedDODQCISTMapping(rejectedDODQCISTMapping);
                                createStockAdjustment.setMemo("Repaired Stock for DO: " + doNumber);
                                createStockAdjustment.setRemark("Repaired Stock for DO: " + doNumber);
                            } else {
                                createStockAdjustment.setRejectedDODQCISTMapping(rejectedDODQCISTMapping);
                                createStockAdjustment.setMemo("Rejected Stock for DO: " + doNumber);
                                createStockAdjustment.setRemark("Rejected Stock for DO: " + doNumber);
                            }
                            Set<StockAdjustmentDetail> adjustmentDetailSet = new HashSet<StockAdjustmentDetail>();
                            StockAdjustmentDetail stockAdjustmentDetail = new StockAdjustmentDetail();
                            stockAdjustmentDetail.setBatchName(istd.getBatchName());
                            stockAdjustmentDetail.setBin(istd.getDeliveredBin());
                            stockAdjustmentDetail.setRack(istd.getDeliveredRack());
                            stockAdjustmentDetail.setRow(istd.getDeliveredRow());
                            stockAdjustmentDetail.setLocation(istd.getDeliveredLocation());
                            stockAdjustmentDetail.setFinalQuantity(qty);
                            stockAdjustmentDetail.setQuantity(qty);
                            stockAdjustmentDetail.setFinalSerialNames(istd.getDeliveredSerialNames());
                            stockAdjustmentDetail.setSerialNames(istd.getDeliveredSerialNames());
                            stockAdjustmentDetail.setStockAdjustment(createStockAdjustment);
                            adjustmentDetailSet.add(stockAdjustmentDetail);
                            createStockAdjustment.setStockAdjustmentDetail(adjustmentDetailSet);
                            JournalEntry inventoryJE = null;
                            if (operation.equals("Approve")) {
                                istd.setApprovedQtyFromRepairStore(istd.getApprovedQtyFromRepairStore() + qty);
                            } else {
                                istd.setRejectedQtyFromRepairStore(istd.getRejectedQtyFromRepairStore() + qty);
                            }
                            if (extracompanyobj.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) {
                                if (product.getInventoryAccount() != null) {
                                    Map<String, Object> JEFormatParams = new HashMap<>();
                                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                    JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                                    JEFormatParams.put("companyid", companyId);
                                    JEFormatParams.put("isdefaultFormat", true);
                                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                    if (kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty()) {
                                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                        if (format != null) {
                                            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                                            Map<String, Object> seqNumberMap = new HashMap<>();
                                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, businessDate);
                                            jeDataMap.put("entrynumber", (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER));
                                            jeDataMap.put("autogenerated", true);
                                            jeDataMap.put(Constants.SEQFORMAT, format.getID());
                                            jeDataMap.put(Constants.SEQNUMBER, (String) seqNumberMap.get(Constants.SEQNUMBER));
                                            jeDataMap.put(Constants.DATEPREFIX, (String) seqNumberMap.get(Constants.DATEPREFIX));
                                            jeDataMap.put(Constants.DATEAFTERPREFIX, (String) seqNumberMap.get(Constants.DATEAFTERPREFIX));
                                            jeDataMap.put(Constants.DATESUFFIX, (String) seqNumberMap.get(Constants.DATESUFFIX));
                                            jeDataMap.put("entrydate", businessDate);
                                            jeDataMap.put("companyid", companyId);
                                            jeDataMap.put("memo", "Stock Adjustment JE for " + product.getName());
                                            jeDataMap.put("createdby", sessionHandlerImpl.getUserid(request));
                                            jeDataMap.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
                                            jeDataMap.put("transactionModuleid", Constants.Inventory_Stock_Adjustment_ModuleId);
                                            jeresult = journalEntryDAO.saveJournalEntry(jeDataMap); // Create Journal entry without JEdetails
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
                                            KwlReturnObject jedresult = journalEntryDAO.addJournalEntryDetails(jedjson);
                                            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                            jeDetails.add(jed);
                                            jedjson = new JSONObject();
                                            jedjson.put("srno", jeDetails.size() + 1);
                                            jedjson.put("companyid", companyId);
                                            jedjson.put("amount", authHandler.round(((qty * productPrice) * (-1)), companyId));
                                            jedjson.put("accountid", product.getStockAdjustmentAccount().getID());
                                            jedjson.put("debit", true);
                                            jedjson.put("jeid", inventoryJE.getID());
                                            jedresult = journalEntryDAO.addJournalEntryDetails(jedjson);
                                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                            jeDetails.add(jed);
                                            inventoryJE.setDetails(jeDetails);
                                            journalEntryDAO.saveJournalEntryDetailsSet(jeDetails);
                                        } else {
                                            throw new AccountingException("No default Sequence Format found. Please add a default format for Journal Entry.");
                                        }
                                    } else {
                                        throw new AccountingException("No default Sequence Format found. Please add a default format for Journal Entry.");
                                    }
                                } else {
                                    throw new AccountingException("Please set inventory account for product: " + product.getProductid());
                                }
                            }
                            HashMap<String, Object> requestparams = new HashMap<>();
                            requestparams.put("locale", RequestContextUtils.getLocale(request));
                            stockAdjustmentService.requestStockAdjustment(user, createStockAdjustment, false, false, null, requestparams);
                            if (inventoryJE != null) {
                                inventoryJE.setTransactionId(createStockAdjustment.getId());
                            }
                            seqService.updateSeqNumber(seqFormat);
                            if ("Approve".equalsIgnoreCase(operation)) {
                                msg = "Selected item repaired successfully.";
                            } else {
                                msg = "Selected item rejected successfully.";
                            }
                            JSONObject repairParams = new JSONObject();
                            repairParams.put("repairid", rejectedDODQCISTMapping.getID());
                            if ("Approve".equalsIgnoreCase(operation)) {
                                repairParams.put("repairedQty", rejectedDODQCISTMapping.getRepairedQty() + qty);
                                repairParams.put("approvedStockOut", createStockAdjustment);
                            } else {
                                repairParams.put("rejectQuantity", rejectedDODQCISTMapping.getRejectedQty()+ qty);
                                repairParams.put("rejectedStockOut", createStockAdjustment);
                            }
                            repairParams.put("quantityDue", rejectedDODQCISTMapping.getQuantityDue() - qty);
                            interStoreTransferService.saveRejectedDODQCISTMapping(repairParams);
                            String StUom = createStockAdjustment.getUom() != null ? createStockAdjustment.getUom().getNameEmptyforNA() : "";
                            auditMessage = "(Product :" + product.getProductid() + ", Quantity :" + (-qty) + " " + StUom + ", AdjustmentType : " + "Stock Out " + ")";
                            auditMessage = "User " + user.getFullName() + " has created Stock Adjustment: " + stockAdjustmentTransactionNo + " for Store: " + istd.getIstRequest().getToStore().getAbbreviation() + " " + auditMessage;
                            auditTrailObj.insertAuditLog(AuditAction.STOCK_ADJUSTMENT_ADDED, auditMessage, request, "0");
                            if (productIds.indexOf(product.getID()) == -1) {
                                productIds.append(product.getID()).append(",");
                            }
                        }
                    }
                }
            }
            consignmentService.createStockMovementForRepairing(company, consignmentList, repaieStore);
            saApprovalService.createStockMovementForRepairing(company, sadApprovalList, repaieStore);
            stockTransferApprovalService.createStockMovementForRepairing(company, stockRequestDetailApprovalList, repaieStore);
            stockTransferApprovalService.createStockMovementForRepairingIntr(company, stockTransferDetailApprovalList, repaieStore);
            issuccess = true;
            txnManager.commit(status);
        }
        } catch (Exception ex) {
            issuccess = false;
            msg = ex.getMessage();
            txnManager.rollback(status);
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                /**
                 * Added productIds to update journal entry price based on the
                 * valuation done in AOP advisor.
                 */
                jobj.put("productIds", productIds); 

            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    /*
    approveAssemblyProductPendingForQa() function is written to approve pending QA reocrd
    */
    public JSONObject approveAssemblyProductPendingForQa(HttpServletRequest request) throws ServiceException{
        JSONObject jobj = new JSONObject();
        try {
            /*
            isFromRepair  & qadetailid are used  for reapir approve/reject case
            */
            boolean isFromRepair = request.getAttribute("isFromRepair")==  null ? false : Boolean.parseBoolean(request.getAttribute("isFromRepair").toString());
            String  qadetailid = request.getAttribute("qadetailid")==null ? "" : request.getAttribute("qadetailid").toString();
          
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String productBuildId = request.getParameter("moduleApprovalId"); //productbuild id
            JSONArray jArr = new JSONArray(request.getParameter("jsondata"));
            String operation = request.getParameter("operation"); //approve/reject
            boolean isUnbuildAssembly = false;
            
            AssemblyProductApprovalDetails asd=null;
            if (isFromRepair) {
                KwlReturnObject assemQaD = accountingHandlerDAOobj.getObject(AssemblyProductApprovalDetails.class.getName(), qadetailid);
                 asd = (AssemblyProductApprovalDetails) assemQaD.getEntityList().get(0);
                  productBuildId=asd.getPrBuild().getID();
            }
              KwlReturnObject extraCompanyPreferencesObj = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraCompanyPreferencesObj.getEntityList().get(0);
            
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(ProductBuild.class.getName(), productBuildId);
            ProductBuild productBuild = (ProductBuild) capresult.getEntityList().get(0);
            request.setAttribute("ProductBuildID", productBuild.getID());
//            request.setAttribute("isBuildAssApproveReq", true);

            KwlReturnObject productObj = accountingHandlerDAOobj.getObject(Product.class.getName(), productBuild.getProduct().getID());
            Product product = (Product) productObj.getEntityList().get(0);
            boolean isLocationForProduct = false;
            boolean isWarehouseForProduct = false;
            boolean isRowForProduct = false;
            boolean isRackForProduct = false;
            boolean isBinForProduct = false;

            boolean isBatchForProduct = false;
            boolean isSerialForProduct = false;
            boolean isSKUForProduct = false;
            if (!StringUtil.isNullOrEmpty(product.getID())) {
                isLocationForProduct = product.isIslocationforproduct();
                isWarehouseForProduct = product.isIswarehouseforproduct();
                isBatchForProduct = product.isIsBatchForProduct();
                isSerialForProduct = product.isIsSerialForProduct();
                isSKUForProduct = product.isIsSKUForProduct();
                isRowForProduct = product.isIsrowforproduct();
                isRackForProduct = product.isIsrackforproduct();
                isBinForProduct = product.isIsbinforproduct();
            }

            DateFormat df = authHandler.getDateOnlyFormat(request);
         
            
            double approvedorRejectedQty=0.0;
            JSONObject batchSerailObj = null;
            for (int i = 0; i < jArr.length(); i++) {
                JSONArray BatchSerailJarr = new JSONArray();
                JSONObject job = jArr.getJSONObject(i);
                String dtlId = job.getString("recordid");
                
                if (isFromRepair && !qadetailid.equals(dtlId)) {
                    continue;
                }
                
                double qty = job.has("quantity") ? job.getDouble("quantity") : 0; //quantity coming from js sideenterd by user in case of batch
                Map<String, String> requestMap = new HashMap<>();
                requestMap.put("qadetailid", dtlId);
                requestMap.put("productbuildid", productBuildId);
                KwlReturnObject QaDetails = consignmentService.getBuildAssemblyProductQaDetails(requestMap); //saId ids referreed to productbuild table id.

                List<AssemblyProductApprovalDetails> list = (List<AssemblyProductApprovalDetails>) QaDetails.getEntityList();
                batchSerailObj = new JSONObject();
                AssemblyProductApprovalDetails asPrApprDet = null;
                    approvedorRejectedQty += qty;
                    if (list.size() > 0) {
                        asPrApprDet = list.get(0);
                        if (qty <= asPrApprDet.getQuantity()) {// if quantiry entered by user is less than or equal then and ythen only process request

                        //******batch data*****************
                        if ((isLocationForProduct || isWarehouseForProduct || isBatchForProduct || isRowForProduct || isRackForProduct || isBinForProduct) && qty <= asPrApprDet.getQuantity()) {
                            batchSerailObj.put("companyid", product.getCompany().getCompanyID());
                            batchSerailObj.put("batch", asPrApprDet.getBatchname());

                            if (asPrApprDet.getMfgdate() != null) {

                                batchSerailObj.put("mfgdate", df.format(asPrApprDet.getMfgdate()));
                            }
                            if (asPrApprDet.getExpdate() != null) {
                                batchSerailObj.put("expdate", df.format(asPrApprDet.getExpdate()));
                            }

                            batchSerailObj.put("quantity", qty);
//                        batchSerailObj.put("balance", asPrApprDet);
                            batchSerailObj.put("location", asPrApprDet.getLocation() != null ? asPrApprDet.getLocation().getId() : "");
                            batchSerailObj.put("product", product.getID());
                            batchSerailObj.put("warehouse", asPrApprDet.getWarehouse() != null ? asPrApprDet.getWarehouse().getId() : "");
                            batchSerailObj.put("row", asPrApprDet.getRow() != null ? asPrApprDet.getRow().getId() : "");
                            batchSerailObj.put("rack", asPrApprDet.getRack() != null ? asPrApprDet.getRack().getId() : "");
                            batchSerailObj.put("bin", asPrApprDet.getBin() != null ? asPrApprDet.getBin().getId() : "");
                            batchSerailObj.put("isopening", false);
                            if (isUnbuildAssembly) {
                                batchSerailObj.put("ispurchase", false);
                                batchSerailObj.put("transactiontype", "27");//This is DO Type Tranction  
                            } else {
                                batchSerailObj.put("ispurchase", true);
                                batchSerailObj.put("transactiontype", "28");//This is GRN Type Tranction  
                            }
                        }

                        //****************Serial data***************
                        if (isSerialForProduct) {  //if serial no option is on then only save the serial no details 
//                     batchSerailObj.put("id", isEdit ? "" : jSONObject.getString("serialnoid"));
                            batchSerailObj.put("companyid", companyid);
                            batchSerailObj.put("product", product.getID());
                            batchSerailObj.put("serialnoid", "");
                            batchSerailObj.put("serialno", asPrApprDet.getSerialname());

                            if (asPrApprDet.getExpfromdate() != null) {
                                batchSerailObj.put("expstart", df.format(asPrApprDet.getExpfromdate()));
                            }
                            if (asPrApprDet.getExptodate() != null) {
                                batchSerailObj.put("expend", df.format(asPrApprDet.getExptodate()));
                            }

                            if (isUnbuildAssembly) {       //Build Assembly
                                batchSerailObj.put("ispurchase", false);
                                batchSerailObj.put("quantity", "0");//This is DO Type Tranction  
                                batchSerailObj.put("transactiontype", "27");//This is DO Type Tranction  
                                batchSerailObj.put("isUnbuildAssembly", true);
                            } else {                        //Unbuild Assembly
                                batchSerailObj.put("ispurchase", true);
                                batchSerailObj.put("quantity", "1");//This is GRN Type Tranction  
                                batchSerailObj.put("transactiontype", "28");//This is GRN Type Tranction  
                            }
                            batchSerailObj.put("isopening", true);
                        }
                        BatchSerailJarr.put(batchSerailObj);
                        qAApproveRejectQuantityOfbuildAssembly(isFromRepair, request, extraCompanyPreferences, BatchSerailJarr, batchSerailObj, qty, operation, asPrApprDet, productBuild, product, df, companyid);

                        }
                    }
            }
            
            
            /*Update total approved  quantity in productbuild table to track how much quantity of current build is approved and rejected
            
            */
             HashMap<String, Object> ApprovalStatusData = new HashMap<String, Object>();
             ApprovalStatusData.put("productBuildId", productBuildId);
            if (operation.equals("Approve")) {
                ApprovalStatusData.put("approvedQty", approvedorRejectedQty);//save approved quantity in productbuild table
            } else if (operation.equals("Reject") && !isFromRepair) { // isFromRepair when item is alredy rejected and if it is rejected from stock repair at that time no need to approve qty.
                ApprovalStatusData.put("rejectedQty", approvedorRejectedQty);//save rejected quantity in productbuild table
            }
            ApprovalStatusData.put("quantity", productBuild.getQuantity());
            ApprovalStatusData.put("ApprQuantity", productBuild.getApprovedQuantity());
            ApprovalStatusData.put("rejQuantity", productBuild.getRejectedQuantity());
            if (operation.equals("Approve") && isFromRepair) { // if we are approving qty from stcok repair at that time that qty were already added to rejected qty. so at repair approval time decreasing rejected qty
                ApprovalStatusData.put("rejectedQty", - approvedorRejectedQty);//save rejected quantity in productbuild table
            }
            
            KwlReturnObject QaObj = accProductObj.saveProductBuildsApprovedQty(ApprovalStatusData);
            ProductBuild prBuild = (ProductBuild) QaObj.getEntityList().get(0);
            
        } catch (Exception e) {
            Logger.getLogger(ApprovalController.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("ApprovalController.approveAssemblyProductPendingForQa", e);
        }
        return jobj;
    }
    
     public String[] getStringArrayFromJsonArray(JSONArray jarray) throws ServiceException {
        String arr[] = new String[]{};
        try {
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < jarray.length(); i++) {
                list.add("["+jarray.getString(i)+"]");
            }
            arr = list.toArray(new String[list.size()]);

        } catch (Exception e) {
            throw ServiceException.FAILURE("accProductControllerCMN.getStringArrayFromJsonArray", e);
        }
        return arr;
    }
    public JSONObject qAApproveRejectQuantityOfbuildAssembly(boolean isFromRepair,HttpServletRequest request, ExtraCompanyPreferences extraCompanyPreferences, JSONArray BatchSerailJarr, JSONObject batchSerailObj, double qty, String operation, AssemblyProductApprovalDetails asPrApprDet, ProductBuild productBuild, Product product, DateFormat df, String companyid) throws ServiceException{
        JSONObject jobj=new JSONObject();
        try {
            /*
             Save batch/serail 
             */
            JSONObject savedBtachSerailData = null;
             String reason="";
             String remark="";
             
                reason = !StringUtil.isNullOrEmpty(request.getParameter("reason")) ? request.getParameter("reason") : "";
                remark = !StringUtil.isNullOrEmpty(request.getParameter("remark")) ? request.getParameter("remark") : "";
                Calendar cal=Calendar.getInstance();
            
            if (operation.equals("Approve")) {
                savedBtachSerailData = saveProductBatch(BatchSerailJarr.toString(), product, request, productBuild.getJobworkorderid());//update newproductbatch and newbatchserial for main product
            }

                double quantity = asPrApprDet.getQuantity() - qty;
                if (quantity > 0) {
                    /*batch case
                     to handle partial update case when 
                     Entered quantity is approved. for remaining quantity new record is created with pending status.
                     */
                    BatchSerailJarr.remove(0); //removing first jobj becoz there is only one object in jarray  and we have to update quantity in that jobj so insted of updating qty in that jobj  i inserted fresh copy of jobj with  new qty
                    batchSerailObj.put("quantity", quantity);
                    BatchSerailJarr.put(batchSerailObj);
                    saveAssemblyProductQADetails(BatchSerailJarr.toString(), product, request);
                }

                /*
                 After saving batch serial  data approving status of QA record and assigning batchid and serialid to QA detail  record for future references.
                In reject case we are not creating batch serial entries
                 */
                HashMap<String, Object> QaDetailsMap = new HashMap<String, Object>();
                QaDetailsMap.put("qadetailid", asPrApprDet.getId());
                QaDetailsMap.put("batchmapid", savedBtachSerailData !=null && savedBtachSerailData.has("batchmapid") ? savedBtachSerailData.getString("batchmapid") : "");
                QaDetailsMap.put("serialmapid",savedBtachSerailData !=null && savedBtachSerailData.has("serialmapid") ? savedBtachSerailData.getString("serialmapid") : "");
                
                if (operation.equals("Approve") && isFromRepair) {// approved from stock repair report
                          QaDetailsMap.put("approvalstatus", AssemblyQAStatus.REPAIRDONE);
                    }else if(operation.equals("Approve") && !isFromRepair){// approved from QA report
                       QaDetailsMap.put("approvalstatus", AssemblyQAStatus.APPROVED);
                   } else if (operation.equals("Reject")  && isFromRepair) {// rejeteced from stock reapir report
                        QaDetailsMap.put("approvalstatus", AssemblyQAStatus.REPAIRREJECT);
                   }else if(operation.equals("Reject")  && !isFromRepair){// rejected from QA report
                       QaDetailsMap.put("approvalstatus", AssemblyQAStatus.REJECTED);
                   }
                
                if(isFromRepair){
                    QaDetailsMap.put("reason", reason);  
                    QaDetailsMap.put("repareDate", cal.getTime());
                }else{
                    QaDetailsMap.put("remark", remark);  
                    QaDetailsMap.put("inspectedDate", cal.getTime());
                }
                QaDetailsMap.put("quantity", String.valueOf(qty));
                KwlReturnObject kwl = accProductObj.saveQAApprovalDetails(QaDetailsMap);
                

               if (operation.equals("Approve")) {
                   
                   /*
                   Update inventory table entry by quantity
                   */
                   JSONObject invntJson = new JSONObject();
                   invntJson.put("inventoryid", productBuild.getID()); // productbuilds id is referred as inventoryid
                   invntJson.put("quantity", qty);
                   invntJson.put("baseuomquantity", qty);
                   invntJson.put("carryin", true);
                   KwlReturnObject invresult = accProductObj.updateAssemblyProductsInventoryEntry(invntJson);
                  
                   /*
                 Making stock Movement entry for approved quantity.
                 */
                HashMap<String, Object> assemblyParams = new HashMap<String, Object>();
                assemblyParams.put(Constants.df, df);
                BatchSerailJarr.remove(0); //We want to create entry of qty entered
                batchSerailObj.put("quantity", qty);
                BatchSerailJarr.put(batchSerailObj);
                assemblyParams.put("applydate", productBuild.getEntryDate() != null ? df.format(productBuild.getEntryDate()) : "");
                addStockMovementForBuildProduct(getStringArrayFromJsonArray(BatchSerailJarr), product, companyid, productBuild.getProductcost()/productBuild.getQuantity(), assemblyParams, productBuild, productBuild.getRefno()); //stock movement for main product
            }

           
        } catch (Exception e) {
            throw ServiceException.FAILURE("accProductControllerCMN.qAApproveRejectQuantityOfbuildAssembly", e);
        }
        return jobj;
    }
/*
    saveAssemblyProductQADetails() is written to save batch/serial data in QA table 
    */
    public void saveAssemblyProductQADetails(String batchJSON, Product product, HttpServletRequest request) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException {
        JSONArray jArr = new JSONArray(batchJSON);
        String purchasebatchid = "";
        String isUpdate = !StringUtil.isNullOrEmpty((String) request.getAttribute("isUpdate")) ? (String) request.getAttribute("isUpdate") : "false";
        String ProductBuildID = !StringUtil.isNullOrEmpty((String) request.getAttribute("ProductBuildID")) ? (String) request.getAttribute("ProductBuildID") : product.getID();
        String radomID = !StringUtil.isNullOrEmpty((String) request.getAttribute("radomID")) ? (String) request.getAttribute("radomID") : "";
        boolean isUnbuildAssembly = !StringUtil.isNullOrEmpty(request.getParameter("isUnbuildAssembly")) ? Boolean.parseBoolean(request.getParameter("isUnbuildAssembly")) : false;
        //Please do same changes to ProductControllerCMN for Same Method
        boolean isFromRepair = request.getAttribute("isFromRepair")==  null ? false : Boolean.parseBoolean(request.getAttribute("isFromRepair").toString());
        
        KwlReturnObject kmsg = null;
        double ActbatchQty = 1;
        double batchQty = 0;
        boolean isBatch = false;
        boolean isserial = false;
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;

        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isSKUForProduct = false;
        boolean isEdit = false;
        isEdit = !StringUtil.isNullObject(request.getAttribute("EditFlag")) ? (Boolean) request.getAttribute("EditFlag") : false;
//        DateFormat df = authHandler.getDateFormatter(request);    //refer ticket ERP-15117
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
        isBatch = preferences.isIsBatchCompulsory();
        isserial = preferences.isIsSerialCompulsory();

        if (!StringUtil.isNullOrEmpty(product.getID())) {
            isLocationForProduct = product.isIslocationforproduct();
            isWarehouseForProduct = product.isIswarehouseforproduct();
            isBatchForProduct = product.isIsBatchForProduct();
            isSerialForProduct = product.isIsSerialForProduct();
            isSKUForProduct = product.isIsSKUForProduct();
            isRowForProduct = product.isIsrowforproduct();
            isRackForProduct = product.isIsrackforproduct();
            isBinForProduct = product.isIsbinforproduct();
        }
        NewProductBatch productBatch = null;
        String productBatchId = "";
        HashMap<String, Object> batchDataForSerialMap = new HashMap<String, Object>();// this map is used only in batch/serail case. when there is one batch and three serials at that except first record we doesent get batchdata so i have batch data in this map
        //Save Batch detail for both  option for serial no and batch also as if batch option is off then also we are generating batch in backend
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
            if (jSONObject.has("quantity") && !jSONObject.getString("quantity").equals("undefined") && !jSONObject.getString("quantity").isEmpty()) {
                ActbatchQty = jSONObject.getDouble("quantity");
            }
            if (batchQty == 0) {
                batchQty = jSONObject.getDouble("quantity");
            }
            
            HashMap<String, Object> QaDetailsMap = new HashMap<String, Object>();
            if ((isLocationForProduct || isWarehouseForProduct || isBatchForProduct || isRowForProduct || isRackForProduct || isBinForProduct) && (batchQty == ActbatchQty)) {

                String batchname = StringUtil.DecodeText(jSONObject.optString("batch"));

                QaDetailsMap.put("companyid", product.getCompany().getCompanyID());
                QaDetailsMap.put("batchname", batchname);
                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                    QaDetailsMap.put("mfgdate", df.parse(jSONObject.getString("mfgdate")));
                }
                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                    QaDetailsMap.put("expdate", df.parse(jSONObject.getString("expdate")));
                }
                QaDetailsMap.put("quantity", jSONObject.getString("quantity"));
                if (jSONObject.has("balance") && !StringUtil.isNullOrEmpty(jSONObject.getString("balance"))) {
                    QaDetailsMap.put("balance", jSONObject.getString("balance"));
                }
                QaDetailsMap.put("location", jSONObject.optString("location", ""));
                QaDetailsMap.put("product", product.getID());
                QaDetailsMap.put("warehouse", jSONObject.optString("warehouse", ""));
                QaDetailsMap.put("row", jSONObject.optString("row", null));
                QaDetailsMap.put("rack", jSONObject.optString("rack", null));
                QaDetailsMap.put("bin", jSONObject.optString("bin", null));
                QaDetailsMap.put("isopening", false);
                if (isUnbuildAssembly) {
                    QaDetailsMap.put("ispurchase", false);
                    QaDetailsMap.put("transactiontype", "27");//This is DO Type Tranction  
                } else {
                    QaDetailsMap.put("ispurchase", true);
                    QaDetailsMap.put("transactiontype", "28");//This is GRN Type Tranction  
                }
                for (Map.Entry<String, Object> entry : QaDetailsMap.entrySet()) {
                    batchDataForSerialMap.put(entry.getKey(), entry.getValue());
                }
//            
            }
            batchQty--;

            if (isSerialForProduct) {  //if serial no option is on then only save the serial no details 
                
                for (Map.Entry<String, Object> entry : batchDataForSerialMap.entrySet()) {
                    QaDetailsMap.put(entry.getKey(), entry.getValue());
                }
                QaDetailsMap.put("id", isEdit ? "" : jSONObject.getString("serialnoid"));
                QaDetailsMap.put("companyid", companyid);
                QaDetailsMap.put("product", product.getID());
                QaDetailsMap.put("serialname", StringUtil.DecodeText(jSONObject.optString("serialno")));
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    QaDetailsMap.put("expfromdate", df.parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    QaDetailsMap.put("exptodate", df.parse(jSONObject.getString("expend")));
                }
                QaDetailsMap.put("batch", productBatchId);
                if (isUnbuildAssembly) {       //Build Assembly
                    QaDetailsMap.put("ispurchase", false);
                    QaDetailsMap.put("quantity", "0");//This is DO Type Tranction  
                    QaDetailsMap.put("transactiontype", "27");//This is DO Type Tranction  
                    QaDetailsMap.put("isUnbuildAssembly", true);
                } else {                        //Unbuild Assembly
                    QaDetailsMap.put("ispurchase", true);
                    QaDetailsMap.put("quantity", "1");//This is GRN Type Tranction  
                    QaDetailsMap.put("transactiontype", "28");//This is GRN Type Tranction  
                }
                QaDetailsMap.put("isopening", true);
                QaDetailsMap.put("skuvalue", jSONObject.optString("skufield", ""));

            } else {
                batchQty = 0;
            }

            
            QaDetailsMap.put("documentid", ProductBuildID);
            if(isFromRepair){
            QaDetailsMap.put("approvalstatus", AssemblyQAStatus.REJECTED);
            }else{
            QaDetailsMap.put("approvalstatus", AssemblyQAStatus.PENDING);
            }
            KwlReturnObject QaObj = accProductObj.saveQAApprovalDetails(QaDetailsMap);
            AssemblyProductApprovalDetails apdq = (AssemblyProductApprovalDetails) QaObj.getEntityList().get(0);

        }

    }
    /*
    This function is used to generate batch serial data when use rapporves qunatity from qa approval of job work build assembly product
    */
    
    public JSONObject saveProductBatch(String batchJSON, Product product, HttpServletRequest request, String jobworkorderid) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException {
        JSONArray jArr = new JSONArray(batchJSON);
        String purchasebatchid = "";
        String isUpdate =!StringUtil.isNullOrEmpty((String) request.getAttribute("isUpdate"))? (String) request.getAttribute("isUpdate"):"false";
        String ProductBuildID =!StringUtil.isNullOrEmpty(jobworkorderid)? jobworkorderid:"";
        String radomID =!StringUtil.isNullOrEmpty((String) request.getAttribute("radomID"))?(String) request.getAttribute("radomID"):"";
        boolean isUnbuildAssembly = !StringUtil.isNullOrEmpty(request.getParameter("isUnbuildAssembly")) ? Boolean.parseBoolean(request.getParameter("isUnbuildAssembly")) : false;
        //Please do same changes to ProductControllerCMN for Same Method
        KwlReturnObject kmsg = null;
        double ActbatchQty = 1;
        double batchQty = 0;
        boolean isBatch = false;
        boolean isserial = false;
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;

        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isSKUForProduct = false;
        boolean isEdit = false;
        JSONObject returnObj=new JSONObject();
        isEdit = !StringUtil.isNullObject(request.getAttribute("EditFlag")) ? (Boolean) request.getAttribute("EditFlag") : false;
//        DateFormat df = authHandler.getDateFormatter(request);    //refer ticket ERP-15117
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
        isBatch = preferences.isIsBatchCompulsory();
        isserial = preferences.isIsSerialCompulsory();

        if (!StringUtil.isNullOrEmpty(product.getID())) {
            isLocationForProduct = product.isIslocationforproduct();
            isWarehouseForProduct = product.isIswarehouseforproduct();
            isBatchForProduct = product.isIsBatchForProduct();
            isSerialForProduct = product.isIsSerialForProduct();
            isSKUForProduct = product.isIsSKUForProduct();
            isRowForProduct = product.isIsrowforproduct();
            isRackForProduct = product.isIsrackforproduct();
            isBinForProduct = product.isIsbinforproduct();
        }
        NewProductBatch productBatch = null;
        String productBatchId = "";
        //Save Batch detail for both  option for serial no and batch also as if batch option is off then also we are generating batch in backend
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
            if (jSONObject.has("quantity") && !jSONObject.getString("quantity").equals("undefined") && !jSONObject.getString("quantity").isEmpty()) {
                ActbatchQty = jSONObject.getDouble("quantity");
            }
            if (batchQty == 0) {
                batchQty = jSONObject.getDouble("quantity");
            }
            if ((isLocationForProduct || isWarehouseForProduct || isBatchForProduct || isRowForProduct || isRackForProduct || isBinForProduct) && (batchQty == ActbatchQty)) {

                String batchname=StringUtil.DecodeText(jSONObject.optString("batch"));
                productBatchId = accCommonTablesDAO.getpurchaseBatchIdForLocationWarehouseRowRackBin(product.getID(), jSONObject.getString("location"), jSONObject.getString("warehouse"), jSONObject.optString("row", null), jSONObject.optString("rack", null), jSONObject.optString("bin", null), batchname);

                if (StringUtil.isNullOrEmpty(productBatchId)) {

                    HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
                    pdfTemplateMap.put("companyid", product.getCompany().getCompanyID());
                    pdfTemplateMap.put("name", batchname);
                    if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                        pdfTemplateMap.put("mfgdate", df.parse(jSONObject.getString("mfgdate")));
                    }
                    if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                        pdfTemplateMap.put("expdate", df.parse(jSONObject.getString("expdate")));
                    }
                    pdfTemplateMap.put("quantity", jSONObject.getString("quantity"));
                    if (jSONObject.has("balance") && !StringUtil.isNullOrEmpty(jSONObject.getString("balance"))) {
                        pdfTemplateMap.put("balance", jSONObject.getString("balance"));
                    }
                    pdfTemplateMap.put("location", jSONObject.optString("location", ""));
                    pdfTemplateMap.put("product", product.getID());
                    pdfTemplateMap.put("warehouse", jSONObject.optString("warehouse", ""));
                    pdfTemplateMap.put("row", jSONObject.optString("row", null));
                    pdfTemplateMap.put("rack", jSONObject.optString("rack", null));
                    pdfTemplateMap.put("bin", jSONObject.optString("bin", null));
                    pdfTemplateMap.put("isopening", false);
                    if(isUnbuildAssembly){
                        pdfTemplateMap.put("ispurchase", false);
                        pdfTemplateMap.put("transactiontype", "27");//This is DO Type Tranction  
                    } else {
                        pdfTemplateMap.put("ispurchase", true);
                        pdfTemplateMap.put("transactiontype", "28");//This is GRN Type Tranction  
                    }
                    kmsg = accCommonTablesDAO.saveNewBatchForProduct(pdfTemplateMap);
                   
                    if (kmsg != null && kmsg.getEntityList().size() != 0) {
                        productBatch = (NewProductBatch) kmsg.getEntityList().get(0);
                        productBatchId = productBatch.getId();
                    }
                } else {
                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                    String qtnyVal = String.valueOf(Double.parseDouble(jSONObject.getString("quantity")));
                    if(isUnbuildAssembly){
                        double qtyValue = - (Double.parseDouble(jSONObject.getString("quantity")));
                        qtnyVal = String.valueOf(qtyValue);
                    }
                    batchUpdateQtyMap.put("qty", qtnyVal);
                    batchUpdateQtyMap.put("quantity", String.valueOf(Double.parseDouble(jSONObject.getString("quantity"))));
                    batchUpdateQtyMap.put("id", productBatchId);
                    accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
                    KwlReturnObject batchRes = accProductObj.getObject(NewProductBatch.class.getName(), productBatchId);
                    productBatch = (NewProductBatch) batchRes.getEntityList().get(0);
                }

                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity", jSONObject.getString("quantity"));
                documentMap.put("batchmapid", productBatchId);
                documentMap.put("documentid", ProductBuildID);
                if(isUnbuildAssembly){
                    documentMap.put("transactiontype", "27");//This is DO Type Tranction  
                }else {
                    documentMap.put("transactiontype", "28");//This is GRN Type Tranction
            }
                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                    documentMap.put("mfgdate", df.parse(jSONObject.getString("mfgdate")));
                }
                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                    documentMap.put("expdate", df.parse(jSONObject.getString("expdate")));
                }
                returnObj.put("batchmapid", productBatchId);



                accCommonTablesDAO.saveBatchDocumentMapping(documentMap);
            }
            batchQty--;


            if (isSerialForProduct) {  //if serial no option is on then only save the serial no details 

                HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
                pdfTemplateMap.put("id", isEdit ? "" : jSONObject.getString("serialnoid"));
                pdfTemplateMap.put("companyid", companyid);
                pdfTemplateMap.put("product", product.getID());
                pdfTemplateMap.put("name", StringUtil.DecodeText(jSONObject.optString("serialno")));
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    pdfTemplateMap.put("expfromdate", df.parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    pdfTemplateMap.put("exptodate", df.parse(jSONObject.getString("expend")));
                }
                pdfTemplateMap.put("batch", productBatchId);
                if (isUnbuildAssembly) {       //Build Assembly
                    pdfTemplateMap.put("ispurchase", false);
                    pdfTemplateMap.put("quantity", "0");//This is DO Type Tranction  
                    pdfTemplateMap.put("transactiontype", "27");//This is DO Type Tranction  
                    pdfTemplateMap.put("isUnbuildAssembly", true);
                } else {                        //Unbuild Assembly
                    pdfTemplateMap.put("ispurchase", true);
                    pdfTemplateMap.put("quantity", "1");//This is GRN Type Tranction  
                    pdfTemplateMap.put("transactiontype", "28");//This is GRN Type Tranction  
                }
                pdfTemplateMap.put("isopening", true);
                pdfTemplateMap.put("skuvalue", jSONObject.optString("skufield", ""));
                kmsg = accCommonTablesDAO.saveNewSerialForBatch(pdfTemplateMap);
                String serialDetailsId = "";
                if (kmsg != null && kmsg.getEntityList().size() != 0) {
                    NewBatchSerial serialDetails = (NewBatchSerial) kmsg.getEntityList().get(0);
                    serialDetailsId = serialDetails.getId();
            }

                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("serialmapid", serialDetailsId);
                documentMap.put("documentid", ProductBuildID);
                    if(isUnbuildAssembly){
                    documentMap.put("quantity", 0);
                    documentMap.put("transactiontype", "27");//This is DO Type Tranction  
                }else {
                    documentMap.put("quantity", 1);
                    documentMap.put("transactiontype", "28");//This is GRN Type Tranction
                    }
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    documentMap.put("expfromdate", df.parse(jSONObject.getString("expstart")));
                    }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    documentMap.put("exptodate", df.parse(jSONObject.getString("expend")));
                }
                
                returnObj.put("serialmapid", serialDetailsId);
                // accCommonTablesDAO.saveSerialDocumentMapping(documentMap);
                KwlReturnObject krObj = accCommonTablesDAO.saveSerialDocumentMapping(documentMap);

                SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) krObj.getEntityList().get(0);
                if (jSONObject.has("customfield")) {
                    String customfield = jSONObject.getString("customfield");
                    if (!StringUtil.isNullOrEmpty(customfield)) {
                        HashMap<String, Object> DOMap = new HashMap<String, Object>();
                        JSONArray jcustomarray = new JSONArray(customfield);

                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "SerialDocumentMapping");
                        customrequestParams.put("moduleprimarykey", "SerialDocumentMappingId");
                        customrequestParams.put("modulerecid", serialDocumentMapping.getId());
                        customrequestParams.put("moduleid", Constants.SerialWindow_ModuleId);
                        customrequestParams.put("companyid", companyid);
                        DOMap.put("id", serialDocumentMapping.getId());
                        customrequestParams.put("customdataclasspath", Constants.Acc_Serial_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            DOMap.put("serialcustomdataref", serialDocumentMapping.getId());
                            accCommonTablesDAO.updateserialcustomdata(DOMap);
                }
                }
                }
                
            } else {
                batchQty = 0;
            }
        }
        return returnObj;// return obj is used only in approving build assembly quantity through QA approval to update batchid nad serialids in QA detail record
    }
    /*
    This function is used to make stock movement entry
    */
     private void addStockMovementForBuildProduct(String[] jsonData, Product product,String companyid,double  price,HashMap<String, Object> details,ProductBuild productBuild,String refNo) throws AccountingException {

        try {
            Company company=null;
            DateFormat df = (details!=null) ? (DateFormat) details.get("df") : null;
            String applydate = (details!=null) ? (String) details.get("applydate") : null;
            Date appDate = (df!=null && applydate!=null) ? df.parse(applydate) : (authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            List<StockMovement> stockMovementsList=new ArrayList<StockMovement>();
            boolean isUnbuildAssembly = false;
            if (details != null && details.containsKey("isUnbuildAssembly")) {
                isUnbuildAssembly = (Boolean) details.get("isUnbuildAssembly");
            }
            for (int i = 0; i < jsonData.length; i++) {
                String jsonStr = jsonData[i];
                JSONArray jArr = new JSONArray(jsonStr);

                Map prodSerialMap = new HashMap();
                for (int j = 0; j < jArr.length(); j++) {
                    JSONObject jObj = new JSONObject(jArr.get(j).toString());
                    String prod = jObj.optString("productid");
                    String serialno = jObj.optString("serialno","");
                    if (!StringUtil.isNullOrEmpty(serialno)) {
                        if (prodSerialMap.containsKey(prod)) {
                            String srl=(String)prodSerialMap.get(prod);
                            srl += "," + serialno;
                            prodSerialMap.put(prod, srl);
                        }else {
                            prodSerialMap.put(prod, serialno);
                        }
                    }
                }
                
                for (int j = 0; j < jArr.length(); j++) {
                    /*StockMovementDetail obj & StockMovementDetail Set used only when you have location & warehouse
                     * So create its instance only in the case of location & warehouse.
                     */
                    Set<StockMovementDetail> stockMovementDetails = null;
                    StockMovement stockmnt=new StockMovement();
                    Store Invwarehouse =null;
                    Location locationObj=null;
                    
                    JSONObject jSONObject = new JSONObject(jArr.get(j).toString());
                    String qty = jSONObject.getString("quantity");
                    String location = jSONObject.getString("location");
                    String warehouse = jSONObject.getString("warehouse");
//                    String serialno = jSONObject.getString("serialno");
                    String batch = jSONObject.getString("batch");
                    double quantity=0;

                    if(!StringUtil.isNullOrEmpty(companyid)){
                        company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), companyid);
                    }
                    if(!StringUtil.isNullOrEmpty(location)){
                        locationObj = (Location) kwlCommonTablesDAOObj.getClassObject(Location.class.getName(), location);
                    }
                    if(!StringUtil.isNullOrEmpty(warehouse)){
                         Invwarehouse = (Store) kwlCommonTablesDAOObj.getClassObject(Store.class.getName(), warehouse);
                    }

                    if(!StringUtil.isNullOrEmpty(qty)){
                        quantity=Double.parseDouble(qty);
                    }
                    if (locationObj != null && Invwarehouse != null) {
                        stockMovementDetails = new HashSet<StockMovementDetail>();
                        StockMovementDetail stockmovementDTL = new StockMovementDetail();
                        stockmnt.setProduct(product);
                        stockmnt.setPricePerUnit(price);
                        stockmnt.setModuleRefId(productBuild != null ? productBuild.getID() : product.getID());
                        stockmnt.setModuleRefDetailId(productBuild != null ? productBuild.getID() : product.getID());
                        stockmnt.setAssembledProduct(product);
                        stockmnt.setCompany(company);
                        stockmnt.setQuantity(quantity);
                        stockmnt.setTransactionNo(refNo);
                        stockmnt.setStore(Invwarehouse);
                        stockmnt.setStockUoM(product.getUnitOfMeasure());
                        if (isUnbuildAssembly) {    //Parent Product Stock Deduction
                            stockmnt.setRemark("New Stock deducted from Unbuild Assembly.");
                            stockmnt.setTransactionModule(TransactionModule.PRODUCT_UNBUILD_ASSEMBLY);
                            stockmnt.setTransactionType(TransactionType.OUT);
                        } else {
                            stockmnt.setRemark("New Stock Added from Build Assembly.");
                            stockmnt.setTransactionModule(TransactionModule.PRODUCT_BUILD_ASSEMBLY);
                            stockmnt.setTransactionType(TransactionType.IN);
                        }
                        stockmnt.setTransactionDate(appDate);
                        stockmovementDTL.setBatchName(batch);
                        stockmovementDTL.setLocation(locationObj);
                        if (product.isIsrowforproduct()) {
                            KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), jSONObject.optString("row"));
                            StoreMaster row = (StoreMaster) krObject.getEntityList().get(0);
                            stockmovementDTL.setRow(row);
                        }
                        if (product.isIsrackforproduct()) {
                            KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), jSONObject.optString("rack"));
                            StoreMaster rack = (StoreMaster) krObject.getEntityList().get(0);
                            stockmovementDTL.setRack(rack);
                        }
                        if (product.isIsbinforproduct()) {
                            KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), jSONObject.optString("bin"));
                            StoreMaster bin = (StoreMaster) krObject.getEntityList().get(0);
                            stockmovementDTL.setBin(bin);
                        }
                        stockmovementDTL.setQuantity(quantity);
                        stockmovementDTL.setSerialNames(prodSerialMap.containsKey(product.getID())? (String)prodSerialMap.get(product.getID()): null);
                        stockmovementDTL.setStockMovement(stockmnt);
                        stockMovementDetails.add(stockmovementDTL);
                        stockmnt.setStockMovementDetails(stockMovementDetails);
                        stockMovementsList.add(stockmnt);
                    }
                }

            }
            if (stockMovementsList.size() > 0) {
                stockMovementService.addOrUpdateBulkStockMovement(product.getCompany(), product.getID(), stockMovementsList,false);
            }

        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while Build Product");
        }
    }
    public ModelAndView getAllStockRepairList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            String companyId = sessionHandlerImpl.getCompanyid(request);
             KwlReturnObject extraCompanyPreferencesObj = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraCompanyPreferencesObj.getEntityList().get(0);
            boolean isQAapprovalForBuildAssembly = extraCompanyPreferences != null ? extraCompanyPreferences.isBuildAssemblyApprovalFlow() : false;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);
            String searchString = request.getParameter("ss");
            String type = request.getParameter("type");
            String repairStatusStr = request.getParameter("repairStatus");
            String storeId = request.getParameter("storeid");;
            String fd = request.getParameter("frmDate");
            String td = request.getParameter("toDate");
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
            String statusType = null;
            if (!StringUtil.isNullOrEmpty(repairStatusStr)) {
                if ("REPAIRDONE".equals(repairStatusStr)) {
                    statusType = "5";
                } else if ("REPAIRREJECT".equals(repairStatusStr)) {
                    statusType = "6";
                }
            }
            Store store = storeService.getStoreById(storeId);

            KwlReturnObject retObj = null;
            //sending My Account time zone diff to compare with created on Date.
            String tzdiff = sessionHandlerImpl.getTimeZoneDifference(request);
            retObj = consignmentService.getAllRepairList(companyId, statusType, store, fromDate, toDate, searchString, paging,tzdiff,isQAapprovalForBuildAssembly);
            List resultList = null;
            if (retObj != null) {
                if (paging != null) {
                    paging.setTotalRecord(retObj.getRecordTotalCount());
                }
                resultList = retObj.getEntityList();
            }
            if (resultList != null) {
                Iterator itr = resultList.iterator();
                JSONArray UserReoprtJson = new JSONArray();
                while (itr.hasNext()) {
                    Object[] roww = (Object[]) itr.next();
                    String Appstatus = "";
                    if (roww[5] != null) {
                        if ("5".equals(roww[5].toString())) {
                            Appstatus = "Done";
                        } else if ("6".equals(roww[5].toString())) {
                            Appstatus = "Stockout";
                        }
                    }
                    JSONObject jObj = new JSONObject();
                    jObj.put("productcode", roww[0] != null ? roww[0].toString() : "");
                    jObj.put("id", roww[1] != null ? roww[1].toString() : "");
                    jObj.put("batchname", (roww[2] != null && !roww[2].equals("")) ? roww[2].toString() : "-");
                    jObj.put("serialname", roww[3] != null ? roww[3].toString() : "-");
                    jObj.put("quantity", roww[4] != null ? roww[4].toString() : "");
                    jObj.put("status", Appstatus);
                    jObj.put("transactionno", roww[6] != null ? roww[6].toString() : "");
                    jObj.put("productname", roww[7] != null ? roww[7].toString() : "");
                    jObj.put("productdescription", roww[16] != null ? roww[16].toString() : "");
                    jObj.put("storename", roww[8] != null ? roww[8].toString() : "");
                    //To change UTC date into user date format as we are removing it from JS side.
                    jObj.put("transactiondate", roww[9] != null ? (roww[15].equals("goodsreceipt") || roww[15].equals("deliveryorder")) ? roww[9] : authHandler.getUTCToUserLocalDateFormatter_NEW(request, (Date) roww[9]) : "");
                    jObj.put("locationname", roww[10] != null ? roww[10].toString() : "");
                    jObj.put("reason", roww[11] != null ? roww[11].toString() : "");
                    jObj.put("rdate", roww[12] != null ? authHandler.getUTCToUserLocalDateFormatter_NEW(request,(Date) roww[12]) : "");
                    jObj.put("billid", roww[14] != null ? roww[14].toString() : "");
                    jObj.put("transactionmodule", roww[15] != null ? roww[15].toString() : "");

                    jArray.put(jObj);
                }
            }
            if (isExport) {
                jobj.put("data", jArray);
                exportDAO.processRequest(request, response, jobj);
            }
            issuccess = true;
            msg = type + " Approval List  has been fetched successfully";

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

    public ModelAndView getAllQAApprovalList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            String companyId = sessionHandlerImpl.getCompanyid(request);
            
             KwlReturnObject extraCompanyPreferencesObj = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraCompanyPreferencesObj.getEntityList().get(0);
            boolean isQAapprovalForBuildAssembly=extraCompanyPreferences != null ? extraCompanyPreferences.isBuildAssemblyApprovalFlow() : false;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
                paging = null;
            }
            String searchString = request.getParameter("ss");
              boolean  isJobWorkOrder = !StringUtil.isNullOrEmpty(request.getParameter("isJobWorkOrder")) ? Boolean.parseBoolean(request.getParameter("isJobWorkOrder")) : false ;
              boolean  isisJobWorkOrderInQA = !StringUtil.isNullOrEmpty(request.getParameter("isisJobWorkOrderInQA")) ? Boolean.parseBoolean(request.getParameter("isisJobWorkOrderInQA")) : false ;
               
            String type = request.getParameter("type");
            String storeId = request.getParameter("storeid");
            String statusType = request.getParameter("status");
            String fd = request.getParameter("frmDate");
            String td = request.getParameter("toDate");
            Store store = null;
            Set<Store> storeSet = new HashSet();
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
                storeSet.add(store);
            } else {
                List storeListByManager = storeService.getStoresByStoreManagers(user, true, null, null, null);
                List storeListByExecutive = storeService.getStoresByStoreExecutives(user, true, null, null, null);
                List storeListByQA = storeService.getStoresByQAPerson(user);
                Iterator itr = storeListByQA.iterator();
                while (itr.hasNext()) {
                    InventoryWarehouse inventoryWarehouse = (InventoryWarehouse) itr.next();
                    jeresult = accountingHandlerDAO.getObject(Store.class.getName(), inventoryWarehouse.getId());
                    Store store1 = (Store) jeresult.getEntityList().get(0);
                    if (store1 != null && store1.isActive()) {
                        storeSet.add(store1);
                    }
                }
                storeSet.addAll(storeListByManager);
                storeSet.addAll(storeListByExecutive);
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
            if (!StringUtil.isNullOrEmpty(statusType)) {
                if ("DONE".equals(statusType)) {
                    statusType = "3";
                } else {
                    statusType = "0";
                }
            }
            KwlReturnObject retObj = null;
            //sending My Account time zone diff to compare with created on Date.
            String TZdiff = sessionHandlerImpl.getTimeZoneDifference(request);
            List resultList = null;
            if (!storeSet.isEmpty() && storeSet != null) {
                retObj = consignmentService.getAllQAList(companyId, fromDate, toDate, type, statusType, storeSet, searchString, paging,TZdiff,isQAapprovalForBuildAssembly,isisJobWorkOrderInQA);
            }
            if (retObj != null) {
                if (paging != null) {
                paging.setTotalRecord(retObj.getRecordTotalCount());
                }
                resultList = retObj.getEntityList();
            }
            if (resultList != null) {
                Iterator itr = resultList.iterator();
                JSONArray UserReoprtJson = new JSONArray();
                while (itr.hasNext()) {

                    Object[] roww = (Object[]) itr.next();
                    String Appstatus = "";
                    if (roww[6] != null) {
                        if ("0".equals(roww[6].toString())) {
                            Appstatus = "PENDING";
                        } else if ("3".equals(roww[6].toString())) {
                            Appstatus = "DONE";
                        }
                    }
                    JSONObject jObj = new JSONObject();
                    jObj.put("id", roww[0] != null ? roww[0].toString() : "");
                    jObj.put("quantity", roww[4] != null ? Double.parseDouble(roww[4].toString()) : "");
                    jObj.put("status", Appstatus);
                    jObj.put("transactionno", roww[3] != null ? roww[3].toString() : "");
                    jObj.put("productcode", roww[1] != null ? roww[1].toString() : "");
                    jObj.put("productid", roww[5] != null ? roww[5].toString() : "");
                    jObj.put("productname", roww[2] != null ? roww[2].toString() : "");
                    jObj.put("productdescription", roww[14] != null ? roww[14].toString() : "");
                    jObj.put("storename", roww[9] != null ? roww[9].toString() : "");
                    if (isQAapprovalForBuildAssembly && "BuildAssemblyQA".equals(roww[9].toString())) {
                        getWarehouseNames(roww[0].toString(), jObj);
                    }
                    jObj.put("storeid", "");
                    jObj.put("uomname", roww[7] != null ? roww[7].toString() : "");
                    String moduleType = roww[8].toString();
                    jObj.put("moduletype", roww[8] != null ? moduleType : "");
                    jObj.put("packaging", roww[10] != null ? roww[10].toString() : "");
                    
                    Date createdOn = null;
                    if (roww[11] != null) {
                        if (StringUtil.equal(type, "ALL")) {
                            if (StringUtil.equal(moduleType, Constants.BUILD_ASSEMBLY_QA_APPROVAL)) {
                                createdOn = new Date(Long.parseLong(roww[11].toString()));
                            } else {
                                createdOn = df.parse(roww[11].toString());
                            }
                        } else if (StringUtil.equal(moduleType, Constants.BUILD_ASSEMBLY_QA_APPROVAL)) {
                            BigInteger bigInt = (BigInteger) roww[11];
                            createdOn = new Date(bigInt.longValue());
                        } else {
                            createdOn = (Date) roww[11];
                        }
                    }
                    jObj.put("createdon", roww[11] != null ? authHandler.getUTCToUserLocalDateFormatter_NEW(request, createdOn) : "");
                    jObj.put("customerid", roww[12] != null ? roww[12].toString() : "");
                    jObj.put("transactionid", roww[13] != null ? roww[13].toString() : "");
                    if (roww[10] != null && !StringUtil.isNullOrEmpty(roww[10].toString())) {
                        jeresult = accountingHandlerDAO.getObject(Packaging.class.getName(), roww[10].toString());
                        Packaging packagingObj = (Packaging) jeresult.getEntityList().get(0);
                        jObj.put("uomname", packagingObj != null ? packagingObj.getStockUoM().getNameEmptyforNA() : "");
                    }
                    if (roww[12] != null) {
                        jeresult = accountingHandlerDAO.getObject(Customer.class.getName(), roww[12].toString());
                        Customer customer = (Customer) jeresult.getEntityList().get(0);
                        jObj.put("customer", customer != null ? customer.getName() : "");
                    }
                    jArray.put(jObj);
                }
            }
            if (isExport) {
                jobj.put("data", jArray);
                exportDAO.processRequest(request, response, jobj);
            }
            issuccess = true;
            msg = type + " Approval List  has been fetched successfully";

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
  //  JOptionPane.showMessageDialog(null, "ALERT MESSAGE", "TITLE", JOptionPane.WARNING_MESSAGE);

        return new ModelAndView(successView, "model", jobj.toString());
    }
    
    public void getWarehouseNames(String id,JSONObject jObj) throws ServiceException{
        try {
            // this code is added to get store names by their ids
            KwlReturnObject QaObj = accProductObj.GetWareHouseNamesOfProductBuild(id);
            List list = QaObj.getEntityList();
            String storename = "";
            for (Object obj : list) {
                if (obj != null) {
                    KwlReturnObject warehouseObj = accountingHandlerDAOobj.getObject(Store.class.getName(), obj.toString());
                    Store warehouse = null;
                    if (warehouseObj.getEntityList().size() > 0) {
                        warehouse = (Store) warehouseObj.getEntityList().get(0);
                        storename += warehouse.getFullName() + ",";
                    }
                }
            }
            if (storename.length() > 0) {
                storename = storename.substring(0, storename.length() - 1);
            }
            jObj.put("storename", storename);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getWarehouseNames", ex);
        }
    }

    public ModelAndView getAllQAApprovalDetailList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);

            String searchString = request.getParameter("ss");
            String saId = request.getParameter("saId");
            String modulType = request.getParameter("moduleid");
            String userId = sessionHandlerImpl.getUserid(request);
            List approvalStatusIgnoreList=new ArrayList<ApprovalStatus>();
            approvalStatusIgnoreList.add(ApprovalStatus.PENDING);
            String company=sessionHandlerImpl.getCompanyid(request);
            if (modulType.equals(Constants.BUILD_ASSEMBLY_QA_APPROVAL)) {
                  /*
                        This block is written for fetching job work build assembly products buils and their batch/serial wise records
                        */
                 Map<String,String> requestMap=new HashMap<>();
                requestMap.put("productbuildid" ,saId);
                KwlReturnObject QaDetails = consignmentService.getBuildAssemblyProductQaDetails(requestMap); //saId ids referreed to productbuild table id.
                        
                List<AssemblyProductApprovalDetails> list=(List<AssemblyProductApprovalDetails> )QaDetails.getEntityList();

                for (AssemblyProductApprovalDetails appDet : list) {
                        JSONObject jObj = new JSONObject();
                        jObj.put("id", appDet.getId());
                        jObj.put("quantity", appDet.getQuantity());
                        jObj.put("isbuildassemblyApprovalrec", true);
                        jObj.put("status", appDet.getApprovalStatus());
                        if (!(appDet.getApprovalStatus() == AssemblyQAStatus.APPROVED || appDet.getApprovalStatus() == AssemblyQAStatus.PENDING || appDet.getApprovalStatus() == AssemblyQAStatus.REJECTED)) {
                            jObj.put("status", AssemblyQAStatus.REJECTED);
                        }
                         jObj.put("moduletype", modulType);
                         
                        if (!StringUtil.isNullObject(appDet.getPrBuild()) ) {
                        jObj.put("transactionno", appDet.getPrBuild().getRefno());
                        jObj.put("productcode", appDet.getPrBuild().getProduct().getProductid());
                        jObj.put("productid", appDet.getPrBuild().getProduct().getID());
                        jObj.put("productname", appDet.getPrBuild().getProduct().getName());
                        jObj.put("productdescription", appDet.getPrBuild().getProduct().getDescription());
                        jObj.put("isSerialForProduct", appDet.getPrBuild().getProduct().isIsSerialForProduct());
                        jObj.put("isBatchForProduct", appDet.getPrBuild().getProduct().isIsBatchForProduct());
                        jObj.put("isreusable", appDet.getPrBuild().getProduct().getItemReusability());
                        if (appDet.getPrBuild().getJobworkorderid() != null) {
                            SalesOrder so = (SalesOrder) kwlCommonTablesDAOObj.getClassObject(SalesOrder.class.getName(), appDet.getPrBuild().getJobworkorderid());
                            jObj.put("customer", so != null ? (so.getCustomer() != null ? so.getCustomer().getName() : "") : "");
                        }
                        
                    }
                        jObj.put("remark", appDet.getRemark());
                        jObj.put("modifiedon", appDet.getInspectionDate() != null ?  authHandler.getUTCToUserLocalDateFormatter_NEW(request,appDet.getInspectionDate()) : "");
                        String storeFullName="";
                        if (!StringUtil.isNullObject(appDet.getWarehouse())) {
                        KwlReturnObject kwlSt = accountingHandlerDAO.getObject(Store.class.getName(), appDet.getWarehouse().getId());
                        Store store = (Store) kwlSt.getEntityList().get(0);
                        storeFullName=store.getFullName();
                    }
                        jObj.put("storename", storeFullName);
                        jObj.put("batchname", appDet.getProductBatch() !=null ? StringUtil.DecodeText(appDet.getProductBatch().getBatchname()) : StringUtil.DecodeText(appDet.getBatchname()) );
                        if (StringUtil.isNullOrEmpty( appDet.getBatchname())) {
                            jObj.put("batchname", "-");
                        }
                        jObj.put("serialname", appDet.getSerial() !=null ? appDet.getSerial().getSerialname() : appDet.getSerialname() );
                        if (StringUtil.isNullOrEmpty(appDet.getSerialname())) {
                            jObj.put("serialname", "-");
                        }
                        jObj.put("storeid", appDet.getWarehouse()== null ? "" : appDet.getWarehouse().getId());
                        jObj.put("locationname", appDet.getLocation() == null ? "" : appDet.getLocation().getName());
                        int approvePermissionCount = consignmentService.isQAApprovePermissionForUser(appDet.getPrBuild().getCompany(), appDet.getWarehouse() == null ? "" : appDet.getWarehouse().getId(), appDet.getLocation() == null ? "" : appDet.getLocation().getId(), userId);
                        jObj.put("approvepermissioncount", 1);
                        
                        int attachmentCount = saApprovalService.getAttachmentCount(appDet.getPrBuild().getCompany(), appDet.getId());
                        jObj.put("attachment", attachmentCount);
                        jArray.put(jObj);
                }
                
            }
            else if ("consignment".equals(modulType)) {

                Consignment consign = consignmentService.getConsingmentById(saId);
                Set<ConsignmentApprovalDetails> consList = consign.getConsignmentApprovalDetails();

                for (ConsignmentApprovalDetails conmnt : consList) {
                    if (conmnt.getRepairStatus() != ApprovalStatus.RETURNTOREPAIR) {
                        JSONObject jObj = new JSONObject();
                        jObj.put("id", conmnt.getId());
                        jObj.put("transactionno", conmnt.getConsignment().getTransactionNo());
                        jObj.put("customer", conmnt.getConsignment().getCustomer() == null ? "" : conmnt.getConsignment().getCustomer().getName());
                        jObj.put("quantity", conmnt.getQuantity());
                        jObj.put("actualquantity", conmnt.getQuantity());
                        jObj.put("status", conmnt.getApprovalStatus());
                        jObj.put("supervisor", conmnt.getInspector() != null ? conmnt.getInspector().getFullName() : "");
                        jObj.put("productcode", conmnt.getConsignment().getProduct().getProductid());
                        jObj.put("productid", conmnt.getConsignment().getProduct().getID());
                        jObj.put("productname", conmnt.getConsignment().getProduct().getName());
                        jObj.put("productdescription", conmnt.getConsignment().getProduct().getDescription());
                        jObj.put("isSerialForProduct", conmnt.getConsignment().getProduct().isIsSerialForProduct());
                        jObj.put("isBatchForProduct", conmnt.getConsignment().getProduct().isIsBatchForProduct());
                        jObj.put("isreusable", conmnt.getConsignment().getProduct().getItemReusability());
                        jObj.put("reusablecount", (conmnt.getApprovalStatus() == conmnt.getApprovalStatus().APPROVED || conmnt.getApprovalStatus() == conmnt.getApprovalStatus().REJECTED) ? conmnt.getConsignment().getProduct().getTotalIssueCount() : "");
                        jObj.put("storename", conmnt.getConsignment().getStore() == null ? "" : conmnt.getConsignment().getStore().getFullName());
                        jObj.put("batchname", conmnt.getBatchName());
                        jObj.put("modifiedon", (conmnt.getModifiedOn() != null && !approvalStatusIgnoreList.contains(conmnt.getApprovalStatus())) ?  authHandler.getUTCToUserLocalDateFormatter_NEW(request,conmnt.getModifiedOn()) : "");
                        if (StringUtil.isNullOrEmpty(conmnt.getBatchName())) {
                            jObj.put("batchname", "-");
                        }
                        jObj.put("customer", conmnt.getConsignment().getCustomer().getName());
                        jObj.put("serialname", conmnt.getSerialName());
                        if (StringUtil.isNullOrEmpty(conmnt.getSerialName())) {
                            jObj.put("serialname", "-");
                        }
                        jObj.put("storeid", conmnt.getConsignment().getStore() == null ? "" : conmnt.getConsignment().getStore().getId());
                        jObj.put("uomname", conmnt.getConsignment().getUom() == null ? "" : conmnt.getConsignment().getUom().getNameEmptyforNA());
                        jObj.put("locationname", conmnt.getLocation() == null ? "" : conmnt.getLocation().getName());
                        jObj.put("leadtime", conmnt.getConsignment().getProduct().getQALeadTimeInDays());
                        jObj.put("moduletype", modulType);
                        jObj.put("remark", conmnt.getRemark());
                        InspectionTemplate it = conmnt.getConsignment().getProduct().getInspectionTemplate();
                        jObj.put("inspectionTemplate", it != null ? it.getId() : null);

                        int approvePermissionCount = consignmentService.isQAApprovePermissionForUser(conmnt.getConsignment().getCompany(), conmnt.getConsignment().getStore() == null ? "" : conmnt.getConsignment().getStore().getId(), conmnt.getLocation() == null ? "" : conmnt.getLocation().getId(), userId);
                        jObj.put("approvepermissioncount", approvePermissionCount);
                        int attachmentCount = saApprovalService.getAttachmentCount(conmnt.getConsignment().getCompany(), conmnt.getId());
                        jObj.put("attachment", attachmentCount);
                        jArray.put(jObj);
                    }
                }
                issuccess = true;
                msg = "Stock Adjustment Approval List  has been fetched successfully";
            } else if ("stockrequest".equals(modulType) || "stocktransfer".equals(modulType)) {
                KwlReturnObject jeresult = accountingHandlerDAO.getObject(StockTransferApproval.class.getName(), saId);
                StockTransferApproval stApproval = (StockTransferApproval) jeresult.getEntityList().get(0);

                List<StockTransferDetailApproval> stda = stockTransferApprovalService.getStockTransferDetailApprovalList(stApproval, paging);
                for (StockTransferDetailApproval sa : stda) {
                    if (sa.getRepairStatus() != ApprovalStatus.RETURNTOREPAIR) {
                        JSONObject jObj = new JSONObject();
                        jObj.put("id", sa.getId());
                        jObj.put("status", sa.getApprovalStatus());
                        jObj.put("supervisor", sa.getInspector() != null ? sa.getInspector().getFullName() : "");
                        jObj.put("serialname", sa.getSerialName());
                        if (StringUtil.isNullOrEmpty(sa.getSerialName())) {
                            jObj.put("serialname", "-");
                        }
                        String stockTransferId = sa.getStockTransferDetailId();
                        TransactionModule module = stApproval.getTransactionModule();
                        if (module == TransactionModule.INTER_STORE_TRANSFER) {
                            ISTDetail ISD = interStoreTransferService.getISTDetailById(stockTransferId);
                            if (ISD != null) {
                                jObj.put("fromlocationname", ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getName());
                                jObj.put("fromlocationid", ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getId());
                                jObj.put("locationname", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getName());
                                jObj.put("locationid", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getId());
                                jObj.put("batchname", ISD.getBatchName());
                                if (StringUtil.isNullOrEmpty(ISD.getBatchName())) {
                                    jObj.put("batchname", "-");
                                }
                                jObj.put("quantity", sa.getQuantity());
                                jObj.put("actualquantity", sa.getQuantity());
                                jObj.put("issuedserialnames", ISD.getIssuedSerialNames());
                                jObj.put("serialname", sa.getSerialName());
                                jObj.put("returnserialnames", ISD.getReturnSerialNames());
                                jObj.put("isreusable", ISD.getIstRequest().getProduct().getItemReusability());
                                jObj.put("reusablecount", (sa.getApprovalStatus() == sa.getApprovalStatus().APPROVED || sa.getApprovalStatus() == sa.getApprovalStatus().REJECTED) ? ISD.getIstRequest().getProduct().getTotalIssueCount() : "");
                                jObj.put("leadtime", ISD.getIstRequest().getProduct().getQALeadTimeInDays());
                                jObj.put("productname", ISD.getIstRequest().getProduct().getName());
                                jObj.put("productdescription", ISD.getIstRequest().getProduct().getDescription());
                                jObj.put("transactionno", ISD.getIstRequest().getTransactionNo());
                                jObj.put("storename", ISD.getIstRequest().getToStore().getFullName());
                                jObj.put("productcode", ISD.getIstRequest().getProduct().getProductid());
                                jObj.put("isSerialForProduct", ISD.getIstRequest().getProduct().isIsSerialForProduct());
                                jObj.put("isBatchForProduct", ISD.getIstRequest().getProduct().isIsBatchForProduct());
                                jObj.put("moduletype", modulType);
                                jObj.put("remark", sa.getRemark()); 
                                jObj.put("modifiedon",(sa.getModifiedOn() != null && !approvalStatusIgnoreList.contains(sa.getApprovalStatus())) ?  df.format(sa.getModifiedOn()) : "");
                                if (ISD.getIstRequest() != null && ISD.getDeliveredLocation() != null) {
                                    int approvePermissionCount = consignmentService.isQAApprovePermissionForUser(ISD.getIssuedLocation().getCompany(), ISD.getIstRequest().getToStore() == null ? "" : ISD.getIstRequest().getToStore().getId(), ISD.getDeliveredLocation().getId(), userId);
                                    jObj.put("approvepermissioncount", approvePermissionCount);
                                }
                                InspectionTemplate it = ISD.getIstRequest().getProduct().getInspectionTemplate();
                                jObj.put("inspectionTemplate", it != null ? it.getId() : null);
                                int attachmentCount = saApprovalService.getAttachmentCount(ISD.getIstRequest().getCompany(), ISD.getId());
                                jObj.put("attachment", attachmentCount);

                                jArray.put(jObj);
                            }
                        } else if (module == TransactionModule.STOCK_REQUEST) {
                            StockRequestDetail SRD = stockRequestService.getStockRequestDetail(stockTransferId);
                            if (SRD != null) {
                                jObj.put("fromlocationname", SRD.getIssuedLocation() == null ? "" : SRD.getIssuedLocation().getName());
                                jObj.put("fromlocationid", SRD.getIssuedLocation() == null ? "" : SRD.getIssuedLocation().getId());
                                jObj.put("locationname", SRD.getDeliveredLocation() == null ? "" : SRD.getDeliveredLocation().getName());
                                jObj.put("locationid", SRD.getDeliveredLocation() == null ? "" : SRD.getDeliveredLocation().getId());
                                jObj.put("batchname", SRD.getBatchName());
                                if (StringUtil.isNullOrEmpty(SRD.getBatchName())) {
                                    jObj.put("batchname", "-");
                                }
                                jObj.put("quantity", sa.getQuantity());
                                jObj.put("actualquantity", sa.getQuantity());
                                jObj.put("issuedserialnames", SRD.getIssuedSerialNames());
                                jObj.put("isreusable", SRD.getStockRequest().getProduct().getItemReusability());
                                jObj.put("leadtime", SRD.getStockRequest().getProduct().getQALeadTimeInDays());
                                jObj.put("reusablecount", (sa.getApprovalStatus() == sa.getApprovalStatus().APPROVED || sa.getApprovalStatus() == sa.getApprovalStatus().REJECTED) ? SRD.getStockRequest().getProduct().getTotalIssueCount() : "");
                                jObj.put("returnserialnames", SRD.getReturnSerialNames());
                                jObj.put("productname", SRD.getStockRequest().getProduct().getName());
                                jObj.put("productdescription", SRD.getStockRequest().getProduct().getDescription());
                                jObj.put("isSerialForProduct", SRD.getStockRequest().getProduct().isIsSerialForProduct());
                                jObj.put("isBatchForProduct", SRD.getStockRequest().getProduct().isIsBatchForProduct());
                                jObj.put("storename", SRD.getStockRequest().getFromStore().getFullName());
                                jObj.put("transactionno", SRD.getStockRequest().getTransactionNo());
                                jObj.put("productcode", SRD.getStockRequest().getProduct().getProductid());
                                jObj.put("moduletype", modulType);
                                jObj.put("modifiedon", (sa.getModifiedOn() != null && !approvalStatusIgnoreList.contains(sa.getApprovalStatus())) ?  df.format(sa.getModifiedOn()) : "");
                                if (SRD.getStockRequest() != null && SRD.getDeliveredLocation() != null) {
                                    int approvePermissionCount = consignmentService.isQAApprovePermissionForUser(SRD.getIssuedLocation().getCompany(), SRD.getStockRequest().getFromStore() == null ? "" : SRD.getStockRequest().getFromStore().getId(), SRD.getDeliveredLocation() == null ? "" : SRD.getDeliveredLocation().getId(), userId);
                                    jObj.put("approvepermissioncount", approvePermissionCount);
                                }
                                int attachmentCount = saApprovalService.getAttachmentCount(SRD.getStockRequest().getCompany(), SRD.getId());
                                jObj.put("attachment", attachmentCount);
                                InspectionTemplate it = SRD.getStockRequest().getProduct().getInspectionTemplate();
                                jObj.put("inspectionTemplate", it != null ? it.getId() : null);
                                jObj.put("remark", sa.getRemark());
                                jArray.put(jObj);
                            }
                        }
                    }
                    issuccess = true;
                    msg = "IST Return Detail Approval List  has been fetched successfully";
                }
            } else if ("stockout".equals(modulType)) {
                KwlReturnObject jeresult = accountingHandlerDAO.getObject(SAApproval.class.getName(), saId);
                SAApproval saApproval = (SAApproval) jeresult.getEntityList().get(0);

                List<SADetailApproval> saa = saApprovalService.getStockAdjutmentDetailApprovalList(saApproval, paging);
                for (SADetailApproval sa : saa) {
                    if (sa.getRepairStatus() != ApprovalStatus.RETURNTOREPAIR) {
                        JSONObject jObj = new JSONObject();
                        jObj.put("id", sa.getId());
                        jObj.put("status", sa.getApprovalStatus());
                        jObj.put("supervisor", sa.getInspector() != null ? sa.getInspector().getFullName() : "");
                        jObj.put("serialname", sa.getSerialName());
                        if (StringUtil.isNullOrEmpty(sa.getSerialName())) {
                            jObj.put("serialname", "-");
                        }
                        jObj.put("batchname", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getBatchName());
                        if (StringUtil.isNullOrEmpty(sa.getStockAdjustmentDetail().getBatchName())) {
                            jObj.put("batchname", "-");
                        }
                        jObj.put("quantity", sa.getQuantity());
                        jObj.put("actualquantity", sa.getQuantity());
                        jObj.put("productcode", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().getProductid());
                        jObj.put("locationname", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getLocation() == null ? "" : sa.getStockAdjustmentDetail().getLocation().getName());
                        jObj.put("locationid", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getLocation() == null ? "" : sa.getStockAdjustmentDetail().getLocation().getId());
                        jObj.put("productname", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().getName());
                        jObj.put("productdescription", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().getDescription());
                        jObj.put("isSerialForProduct", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().isIsSerialForProduct());
                        jObj.put("isBatchForProduct", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().isIsBatchForProduct());
                        jObj.put("transactionno", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getTransactionNo());
                        jObj.put("storename", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getStore().getFullName());
                        jObj.put("isreusable", sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().getItemReusability());
                        jObj.put("reusablecount", (sa.getApprovalStatus() == sa.getApprovalStatus().APPROVED || sa.getApprovalStatus() == sa.getApprovalStatus().REJECTED) ? sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().getTotalIssueCount() : "");
                        jObj.put("leadtime", sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().getQALeadTimeInDays());
                        jObj.put("moduletype", modulType);
                        jObj.put("remark", sa.getRemark());
                        jObj.put("modifiedon", (sa.getModifiedOn() != null && !approvalStatusIgnoreList.contains(sa.getApprovalStatus())) ?  df.format(sa.getModifiedOn()) : "");
                        if (sa.getStockAdjustmentDetail() != null && sa.getStockAdjustmentDetail().getLocation() != null) {
                            int approvePermissionCount = consignmentService.isQAApprovePermissionForUser(sa.getStockAdjustmentDetail().getLocation().getCompany(), sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getStore() == null ? "" : sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getStore().getId(), sa.getStockAdjustmentDetail().getLocation().getId(), userId);
                            jObj.put("approvepermissioncount", approvePermissionCount);
                        }
                        int attachmentCount = saApprovalService.getAttachmentCount(sa.getStockAdjustmentDetail().getStockAdjustment().getCompany(), sa.getId());
                        jObj.put("attachment", attachmentCount);

                        InspectionTemplate it = sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().getInspectionTemplate();
                        jObj.put("inspectionTemplate", it != null ? it.getId() : null);
                        jArray.put(jObj);
                    }
                }
                issuccess = true;
                msg = "Stock Adjustment Detail Approval List  has been fetched successfully";
            } else if ("goodsreceipt".equals(modulType)) {
                JSONObject json = new JSONObject();
                json.put("istRequest", saId);
                KwlReturnObject jeresult = accountingHandlerDAO.getObject(InterStoreTransferRequest.class.getName(), saId);
                InterStoreTransferRequest istRequest = (InterStoreTransferRequest) jeresult.getEntityList().get(0);
                if (istRequest != null) {
                    jArray = getGRNDetailApprovalTransactionJSON(jArray, istRequest, json, modulType, userId);
                }
                issuccess = true;
                msg = "Goods Receipt Note Approval List has been fetched successfully";
            } else if (Constants.MRP_WORK_ORDER.equals(modulType)) {
                JSONObject json = new JSONObject();
                json.put("istRequest", saId);
                KwlReturnObject jeresult = accountingHandlerDAO.getObject(InterStoreTransferRequest.class.getName(), saId);
                InterStoreTransferRequest istRequest = (InterStoreTransferRequest) jeresult.getEntityList().get(0);
                if (istRequest != null) {
                    jArray = getWOCDetailApprovalTransactionJSON(jArray, istRequest, json, modulType, userId);
                }
                issuccess = true;
                msg = "WORK ORDER Approval List has been fetched successfully";
            } else if ("deliveryorder".equals(modulType)) {
                /**
                 * QC Approval Detailed Report.
                 */
                JSONObject json = new JSONObject();
                json.put("istrequest", saId);
                KwlReturnObject jeresult = accountingHandlerDAO.getObject(InterStoreTransferRequest.class.getName(), saId);
                InterStoreTransferRequest istRequest = (InterStoreTransferRequest) jeresult.getEntityList().get(0);
                if (istRequest != null) {
                    jArray = getDODetailApprovalTransactionJSON(jArray, istRequest, json, modulType, userId);
                }
                issuccess = true;
            }
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

    private JSONArray getGRNDetailApprovalTransactionJSON(JSONArray jArray, InterStoreTransferRequest istRequest, JSONObject json, String modulType, String userId) throws JSONException, ServiceException {
        KwlReturnObject resultList = stockService.getGRODetailISTMapping(json);
        //ERM-691 passing repair store set in companpreferences and its default location to auto populate QA flow window
        String companyid = istRequest!=null?istRequest.getCompany().getCompanyID():"";
        KwlReturnObject companyprefkwl = kwlCommonTablesDAOObj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
        ExtraCompanyPreferences extracompanypreferences = (ExtraCompanyPreferences) (companyprefkwl.getEntityList().isEmpty()?null:companyprefkwl.getEntityList().get(0));
        String defaultrepairstore = extracompanypreferences!=null?extracompanypreferences.getRepairStore():"";
        Store repairstore = storeService.getStoreById(defaultrepairstore);
        String defaultrepairstorelocation = repairstore!=null?repairstore.getDefaultLocation().getId():"";
        
        if (resultList != null && resultList.getEntityList() != null && !resultList.getEntityList().isEmpty()) {
            List<GRODetailISTMapping> groDetailIstMappings = resultList.getEntityList();
            if (groDetailIstMappings != null && !groDetailIstMappings.isEmpty()) {
                String grnStorename = "", grnLocationName = "", grnLocationId = "";
                GRODetailISTMapping groDetailIstMapping = groDetailIstMappings.get(0);
                if (groDetailIstMapping != null) {
                    Set<InterStoreTransferRequest> approvedRequests = groDetailIstMapping.getApprovedInterStoreTransferRequests();
                    Set<ISTDetail> istdetails = istRequest.getIstDetails();
                    grnStorename = istRequest.getFromStore().getFullName();
                    for (ISTDetail ISD : istdetails) {
                        grnLocationName = ISD.getIssuedLocation() != null ? ISD.getIssuedLocation().getName() : "";
                        grnLocationId = ISD.getIssuedLocation() != null ? ISD.getIssuedLocation().getId() : "";

                        List<String> serials = new ArrayList();
                        if (!StringUtil.isNullOrEmpty(groDetailIstMapping.getApprovedSerials())) {
                            /**
                             * Add all approved serials.
                             */
                            serials.addAll(Arrays.asList(groDetailIstMapping.getApprovedSerials().split(",")));
                        }
                        if (!StringUtil.isNullOrEmpty(groDetailIstMapping.getRejectedSerials())) {
                            /**
                             * Add all rejected serials.
                             */
                            serials.addAll(Arrays.asList(groDetailIstMapping.getRejectedSerials().split(",")));
                        }
                        /**
                         * If quantitydue is greater than 0, then only add
                         * pending IST request.
                         */
                        if (groDetailIstMapping.getQuantityDue() > 0.0) {
                            if (istRequest.getProduct().isIsSerialForProduct()) {
                                /**
                                 * If serial is activated for the product, then
                                 * split serial names using ',' as a delimeter.
                                 * We need to show separate row in QA approval
                                 * report for each serial.
                                 */
                                String[] serialNames = ISD.getIssuedSerialNames().split(",");
                                for (String serialName : serialNames) {
                                    if (!serials.contains(serialName)) {
                                        /**
                                         * Show serial which are not approved or
                                         * rejected.
                                         */
                                        JSONObject jObj = new JSONObject();
                                        jObj.put("id", ISD.getId() == null ? "" : ISD.getId());
                                        String detailStatus = groDetailIstMapping.getQuantityDue() > 0 ? "PENDING" : "APPROVED";
                                        jObj.put("status", detailStatus);
                                        if (detailStatus.equals("APPROVED")) {
                                            jObj.put("supervisor", istRequest.getCreatedBy() != null ? istRequest.getCreatedBy().getFullName() : "");
                                        }
                                        jObj.put("locationname", ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getName());
                                        jObj.put("locationid", ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getId());
                                        jObj.put("fromlocationname", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getName());
                                        jObj.put("fromlocationid", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getId());
                                        jObj.put("batchname", ISD.getBatchName());
                                        if (StringUtil.isNullOrEmpty(ISD.getBatchName())) {
                                            jObj.put("batchname", "-");
                                        }
                                        jObj.put("quantity", 1);
                                        jObj.put("actualquantity", ISD.getIssuedQuantity());
                                        jObj.put("issuedserialnames", serialName);
                                        jObj.put("serialname", serialName);
                                        jObj.put("returnserialnames", ISD.getReturnSerialNames());
                                        jObj.put("isreusable", ISD.getIstRequest().getProduct().getItemReusability());
                                        jObj.put("leadtime", ISD.getIstRequest().getProduct().getQALeadTimeInDays());
                                        jObj.put("productname", ISD.getIstRequest().getProduct().getName());
                                        jObj.put("productdescription", ISD.getIstRequest().getProduct().getDescription());
                                        jObj.put("transactionno", ISD.getIstRequest().getTransactionNo());
                                        jObj.put("storename", grnStorename); //ERM-691 default store should be displayed in QA approval tab
                                        jObj.put("productcode", ISD.getIstRequest().getProduct().getProductid());
                                        jObj.put("isSerialForProduct", ISD.getIstRequest().getProduct().isIsSerialForProduct());
                                        jObj.put("isBatchForProduct", ISD.getIstRequest().getProduct().isIsBatchForProduct());
                                        jObj.put("moduletype", modulType);
                                        jObj.put("remark", istRequest.getRemark());
                                        jObj.put("repairstore",defaultrepairstore); //ERM-691 passing repair store and its default location to autopopulate selection window
                                        jObj.put("repairstorelocation", defaultrepairstorelocation);
                                        if (detailStatus.equals("APPROVED")) {
                                            jObj.put("modifiedon", istRequest.getModifiedOn() != null ? df.format(istRequest.getModifiedOn()) : "");
                                        }
                                        if (ISD.getIstRequest() != null && ISD.getDeliveredLocation() != null) {
                                            int approvePermissionCount = consignmentService.isQAApprovePermissionForUser(ISD.getIssuedLocation().getCompany(), ISD.getIstRequest().getToStore() == null ? "" : ISD.getIstRequest().getToStore().getId(), ISD.getDeliveredLocation().getId(), userId);
                                            jObj.put("approvepermissioncount", approvePermissionCount);
                                        }
                                        InspectionTemplate it = ISD.getIstRequest().getProduct().getInspectionTemplate();
                                        jObj.put("inspectionTemplate", it != null ? it.getId() : null);
                                        int attachmentCount = saApprovalService.getAttachmentCount(ISD.getIstRequest().getCompany(), ISD.getId());
                                        jObj.put("attachment", attachmentCount);
                                        jArray.put(jObj);
                                    }
                                }
                            } else {
                                JSONObject jObj = new JSONObject();
                                jObj.put("id", ISD.getId() == null ? "" : ISD.getId());
                                String detailStatus = groDetailIstMapping.getQuantityDue() > 0 ? "PENDING" : "APPROVED";
                                jObj.put("status", detailStatus);
                                if (detailStatus.equals("APPROVED")) {
                                    jObj.put("supervisor", istRequest.getCreatedBy() != null ? istRequest.getCreatedBy().getFullName() : "");
                                }
                                jObj.put("locationname",grnLocationName);
                                jObj.put("locationid", ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getId());
                                jObj.put("fromlocationname", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getName());
                                jObj.put("fromlocationid", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getId());
                                jObj.put("batchname", ISD.getBatchName());
                                if (StringUtil.isNullOrEmpty(ISD.getBatchName())) {
                                    jObj.put("batchname", "-");
                                }
                                double qty = ISD.getIssuedQuantity() - ISD.getQaApproved() - ISD.getQaRejected();
                                
                                jObj.put("quantity",qty);
                                jObj.put("actualquantity", ISD.getIssuedQuantity());
                                jObj.put("issuedserialnames", ISD.getIssuedSerialNames());
                                jObj.put("serialname", ISD.getIssuedSerialNames());
                                jObj.put("returnserialnames", ISD.getReturnSerialNames());
                                jObj.put("isreusable", ISD.getIstRequest().getProduct().getItemReusability());
                                jObj.put("leadtime", ISD.getIstRequest().getProduct().getQALeadTimeInDays());
                                jObj.put("productname", ISD.getIstRequest().getProduct().getName());
                                jObj.put("productdescription", ISD.getIstRequest().getProduct().getDescription());
                                jObj.put("transactionno", ISD.getIstRequest().getTransactionNo());
                                jObj.put("storename", grnStorename);
                                jObj.put("productcode", ISD.getIstRequest().getProduct().getProductid());
                                jObj.put("isSerialForProduct", ISD.getIstRequest().getProduct().isIsSerialForProduct());
                                jObj.put("isBatchForProduct", ISD.getIstRequest().getProduct().isIsBatchForProduct());
                                jObj.put("moduletype", modulType);
                                jObj.put("remark", istRequest.getRemark());
                                jObj.put("repairstore",defaultrepairstore); //ERM-691 passing repair store and its default location to autopopulate selection window
                                jObj.put("repairstorelocation", defaultrepairstorelocation);
                                if (detailStatus.equals("APPROVED")) {
                                    jObj.put("modifiedon", istRequest.getModifiedOn() != null ? df.format(istRequest.getModifiedOn()) : "");
                                }
                                if (ISD.getIstRequest() != null && ISD.getDeliveredLocation() != null) {
                                    int approvePermissionCount = consignmentService.isQAApprovePermissionForUser(ISD.getIssuedLocation().getCompany(), ISD.getIstRequest().getToStore() == null ? "" : ISD.getIstRequest().getToStore().getId(), ISD.getDeliveredLocation().getId(), userId);
                                    jObj.put("approvepermissioncount", approvePermissionCount);
                                }
                                InspectionTemplate it = ISD.getIstRequest().getProduct().getInspectionTemplate();
                                jObj.put("inspectionTemplate", it != null ? it.getId() : null);
                                int attachmentCount = saApprovalService.getAttachmentCount(ISD.getIstRequest().getCompany(), ISD.getId());
                                jObj.put("attachment", attachmentCount);
                                if (qty > 0.0) {
                                    jArray.put(jObj);
                                }
                            }
                        }
                    }
                    /**
                     * Get all approved inter store transfer request and create
                     * JSON with respect to inter store transfer details created
                     * for approved IST.
                     */
                    if (approvedRequests != null && !approvedRequests.isEmpty()) {
                        for (InterStoreTransferRequest approvedRequest : approvedRequests) {
                            for (ISTDetail outgoingISTDetail : approvedRequest.getIstDetails()) {
                                JSONObject jObj = new JSONObject();
                                jObj.put("id", outgoingISTDetail.getId() == null ? "" : outgoingISTDetail.getId());
                                String detailStatus = "APPROVED";
                                jObj.put("status", detailStatus);
                                if (detailStatus.equals("APPROVED")) {
                                    jObj.put("supervisor", approvedRequest.getCreatedBy() != null ? approvedRequest.getCreatedBy().getFullName() : "");
                                }
                                jObj.put("locationname",grnLocationName);
                                jObj.put("locationid", outgoingISTDetail.getDeliveredLocation().getId());
                                jObj.put("fromlocationname", outgoingISTDetail.getIssuedLocation() == null ? "" : outgoingISTDetail.getIssuedLocation().getName());
                                jObj.put("fromlocationid", outgoingISTDetail.getIssuedLocation() == null ? "" : outgoingISTDetail.getIssuedLocation().getId());
                                jObj.put("batchname", outgoingISTDetail.getBatchName());
                                if (StringUtil.isNullOrEmpty(outgoingISTDetail.getBatchName())) {
                                    jObj.put("batchname", "-");
                                }
                                jObj.put("quantity", outgoingISTDetail.getDeliveredQuantity());
                                jObj.put("actualquantity", outgoingISTDetail.getIssuedQuantity());
                                jObj.put("issuedserialnames", outgoingISTDetail.getIssuedSerialNames());
                                jObj.put("serialname", outgoingISTDetail.getIssuedSerialNames());
                                jObj.put("returnserialnames", outgoingISTDetail.getReturnSerialNames());
                                jObj.put("isreusable", outgoingISTDetail.getIstRequest().getProduct().getItemReusability());
                                jObj.put("leadtime", outgoingISTDetail.getIstRequest().getProduct().getQALeadTimeInDays());
                                jObj.put("productname", outgoingISTDetail.getIstRequest().getProduct().getName());
                                jObj.put("productdescription", outgoingISTDetail.getIstRequest().getProduct().getDescription());
                                jObj.put("transactionno", outgoingISTDetail.getIstRequest().getTransactionNo());
                                jObj.put("storename", grnStorename);
                                jObj.put("productcode", outgoingISTDetail.getIstRequest().getProduct().getProductid());
                                jObj.put("isSerialForProduct", outgoingISTDetail.getIstRequest().getProduct().isIsSerialForProduct());
                                jObj.put("isBatchForProduct", outgoingISTDetail.getIstRequest().getProduct().isIsBatchForProduct());
                                jObj.put("moduletype", modulType);
                                jObj.put("remark", outgoingISTDetail.getIstRequest().getRemark());
                                if (detailStatus.equals("APPROVED")) {
                                    jObj.put("modifiedon", approvedRequest.getModifiedOn() != null ? df.format(approvedRequest.getModifiedOn()) : "");
                                }
                                if (outgoingISTDetail.getIstRequest() != null && outgoingISTDetail.getDeliveredLocation() != null) {
                                    int approvePermissionCount = consignmentService.isQAApprovePermissionForUser(outgoingISTDetail.getIssuedLocation().getCompany(), outgoingISTDetail.getIstRequest().getToStore() == null ? "" : outgoingISTDetail.getIstRequest().getToStore().getId(), outgoingISTDetail.getDeliveredLocation().getId(), userId);
                                    jObj.put("approvepermissioncount", approvePermissionCount);
                                }
                                InspectionTemplate it = outgoingISTDetail.getIstRequest().getProduct().getInspectionTemplate();
                                jObj.put("inspectionTemplate", it != null ? it.getId() : null);
                                int attachmentCount = saApprovalService.getAttachmentCount(outgoingISTDetail.getIstRequest().getCompany(), outgoingISTDetail.getId());
                                jObj.put("attachment", attachmentCount);
                                jArray.put(jObj);
                            }
                        }
                    }

                    /**
                     * Get all rejected inter store transfer request and create
                     * JSON with respect to inter store transfer details created
                     * for rejected IST.
                     */
                    Set<RepairGRODetailISTMapping> rejectedInterStoreTransferRequests = groDetailIstMapping.getRejectedInterStoreTransferRequests();
                    if (rejectedInterStoreTransferRequests != null && !rejectedInterStoreTransferRequests.isEmpty()) {
                        for (RepairGRODetailISTMapping rejectedInterStoreTransferRequest : rejectedInterStoreTransferRequests) {
                            InterStoreTransferRequest rejectedRequest = rejectedInterStoreTransferRequest.getInterStoreTransferRequest();
                            for (ISTDetail outgoingISTDetail : rejectedRequest.getIstDetails()) {
                                JSONObject jObj = new JSONObject();
                                jObj.put("id", outgoingISTDetail.getId() == null ? "" : outgoingISTDetail.getId());
                                String detailStatus = "REJECTED";
                                jObj.put("status", detailStatus);
                                jObj.put("supervisor", rejectedRequest.getCreatedBy() != null ? rejectedRequest.getCreatedBy().getFullName() : "");
                                jObj.put("locationname", grnLocationName);
                                jObj.put("locationid", outgoingISTDetail.getDeliveredLocation().getId());
                                jObj.put("fromlocationname", outgoingISTDetail.getIssuedLocation() == null ? "" : outgoingISTDetail.getIssuedLocation().getName());
                                jObj.put("fromlocationid", outgoingISTDetail.getIssuedLocation() == null ? "" : outgoingISTDetail.getIssuedLocation().getId());
                                jObj.put("batchname", outgoingISTDetail.getBatchName());
                                if (StringUtil.isNullOrEmpty(outgoingISTDetail.getBatchName())) {
                                    jObj.put("batchname", "-");
                                }
                                jObj.put("quantity", outgoingISTDetail.getDeliveredQuantity());
                                jObj.put("actualquantity", outgoingISTDetail.getIssuedQuantity());
                                jObj.put("issuedserialnames", outgoingISTDetail.getIssuedSerialNames());
                                jObj.put("serialname", outgoingISTDetail.getIssuedSerialNames());
                                jObj.put("returnserialnames", outgoingISTDetail.getReturnSerialNames());
                                jObj.put("isreusable", outgoingISTDetail.getIstRequest().getProduct().getItemReusability());
                                jObj.put("leadtime", outgoingISTDetail.getIstRequest().getProduct().getQALeadTimeInDays());
                                jObj.put("productname", outgoingISTDetail.getIstRequest().getProduct().getName());
                                jObj.put("productdescription", outgoingISTDetail.getIstRequest().getProduct().getDescription());
                                jObj.put("transactionno", outgoingISTDetail.getIstRequest().getTransactionNo());
                                jObj.put("storename", grnStorename);
                                jObj.put("productcode", outgoingISTDetail.getIstRequest().getProduct().getProductid());
                                jObj.put("isSerialForProduct", outgoingISTDetail.getIstRequest().getProduct().isIsSerialForProduct());
                                jObj.put("isBatchForProduct", outgoingISTDetail.getIstRequest().getProduct().isIsBatchForProduct());
                                jObj.put("moduletype", modulType);
                                jObj.put("remark", rejectedRequest.getRemark());
                                jObj.put("modifiedon", rejectedRequest.getModifiedOn() != null ? df.format(rejectedRequest.getModifiedOn()) : "");
                                if (outgoingISTDetail.getIstRequest() != null && outgoingISTDetail.getDeliveredLocation() != null) {
                                    int approvePermissionCount = consignmentService.isQAApprovePermissionForUser(outgoingISTDetail.getIssuedLocation().getCompany(), outgoingISTDetail.getIstRequest().getToStore() == null ? "" : outgoingISTDetail.getIstRequest().getToStore().getId(), outgoingISTDetail.getDeliveredLocation().getId(), userId);
                                    jObj.put("approvepermissioncount", approvePermissionCount);
                                }
                                InspectionTemplate it = outgoingISTDetail.getIstRequest().getProduct().getInspectionTemplate();
                                jObj.put("inspectionTemplate", it != null ? it.getId() : null);
                                int attachmentCount = saApprovalService.getAttachmentCount(outgoingISTDetail.getIstRequest().getCompany(), outgoingISTDetail.getId());
                                jObj.put("attachment", attachmentCount);
                                jArray.put(jObj);
                            }
                        }
                    }
                }
            }
        }
        return jArray;
    }
    
    
    private JSONArray getWOCDetailApprovalTransactionJSON(JSONArray jArray, InterStoreTransferRequest istRequest, JSONObject json, String modulType, String userId) throws JSONException, ServiceException {
        KwlReturnObject resultList = stockService.getWOCDetailISTMapping(json);
        
        String companyid = istRequest!=null?istRequest.getCompany().getCompanyID():"";
        KwlReturnObject companyprefkwl = kwlCommonTablesDAOObj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
        ExtraCompanyPreferences extracompanypreferences = (ExtraCompanyPreferences) (companyprefkwl.getEntityList().isEmpty()?null:companyprefkwl.getEntityList().get(0));
        String defaultrepairstore = extracompanypreferences!=null?extracompanypreferences.getRepairStore():"";
        Store repairstore = storeService.getStoreById(defaultrepairstore);
        String defaultrepairstorelocation = repairstore!=null?repairstore.getDefaultLocation().getId():"";
        
        if (resultList != null && resultList.getEntityList() != null && !resultList.getEntityList().isEmpty()) {
            List<WOCDetailISTMapping> wocDetailIstMappings = resultList.getEntityList();
            if (wocDetailIstMappings != null && !wocDetailIstMappings.isEmpty()) {
                String woStorename = "", woLocationName = "", woLocationId = "";
                WOCDetailISTMapping wocDetailIstMapping = wocDetailIstMappings.get(0);
                if (wocDetailIstMapping != null) {
                    Set<InterStoreTransferRequest> approvedRequests = wocDetailIstMapping.getApprovedInterStoreTransferRequests();
                    Set<ISTDetail> istdetails = istRequest.getIstDetails();
                    woStorename = istRequest.getFromStore().getFullName();
                    for (ISTDetail ISD : istdetails) {
                        woLocationName = ISD.getIssuedLocation() != null ? ISD.getIssuedLocation().getName() : "";
                        woLocationId = ISD.getIssuedLocation() != null ? ISD.getIssuedLocation().getId() : "";

                        List<String> serials = new ArrayList();
                        if (!StringUtil.isNullOrEmpty(wocDetailIstMapping.getApprovedSerials())) {
                            /**
                             * Add all approved serials.
                             */
                            serials.addAll(Arrays.asList(wocDetailIstMapping.getApprovedSerials().split(",")));
                        }
                        if (!StringUtil.isNullOrEmpty(wocDetailIstMapping.getRejectedSerials())) {
                            /**
                             * Add all rejected serials.
                             */
                            serials.addAll(Arrays.asList(wocDetailIstMapping.getRejectedSerials().split(",")));
                        }
                        /**
                         * If quantitydue is greater than 0, then only add
                         * pending IST request.
                         */
                        if (wocDetailIstMapping.getQuantityDue() > 0.0) {
                            if (istRequest.getProduct().isIsSerialForProduct()) {
                                /**
                                 * If serial is activated for the product, then
                                 * split serial names using ',' as a delimeter.
                                 * We need to show separate row in QA approval
                                 * report for each serial.
                                 */
                                String[] serialNames = ISD.getIssuedSerialNames().split(",");
                                for (String serialName : serialNames) {
                                    if (!serials.contains(serialName)) {
                                        /**
                                         * Show serial which are not approved or
                                         * rejected.
                                         */
                                        JSONObject jObj = new JSONObject();
                                        jObj.put("id", ISD.getId() == null ? "" : ISD.getId());
                                        String detailStatus = wocDetailIstMapping.getQuantityDue() > 0 ? "PENDING" : "APPROVED";
                                        jObj.put("status", detailStatus);
                                        if (detailStatus.equals("APPROVED")) {
                                            jObj.put("supervisor", istRequest.getCreatedBy() != null ? istRequest.getCreatedBy().getFullName() : "");
                                        }
                                        jObj.put("locationname", ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getName());
                                        jObj.put("locationid", ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getId());
                                        jObj.put("fromlocationname", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getName());
                                        jObj.put("fromlocationid", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getId());
                                        jObj.put("batchname", ISD.getBatchName());
                                        if (StringUtil.isNullOrEmpty(ISD.getBatchName())) {
                                            jObj.put("batchname", "-");
                                        }
                                        jObj.put("quantity", 1);
                                        jObj.put("actualquantity", ISD.getIssuedQuantity());
                                        jObj.put("issuedserialnames", serialName);
                                        jObj.put("serialname", serialName);
                                        jObj.put("returnserialnames", ISD.getReturnSerialNames());
                                        jObj.put("isreusable", ISD.getIstRequest().getProduct().getItemReusability());
                                        jObj.put("leadtime", ISD.getIstRequest().getProduct().getQALeadTimeInDays());
                                        jObj.put("productname", ISD.getIstRequest().getProduct().getName());
                                        jObj.put("productdescription", ISD.getIstRequest().getProduct().getDescription());
                                        jObj.put("transactionno", ISD.getIstRequest().getTransactionNo());
                                        jObj.put("storename", woStorename); 
                                        jObj.put("productcode", ISD.getIstRequest().getProduct().getProductid());
                                        jObj.put("isSerialForProduct", ISD.getIstRequest().getProduct().isIsSerialForProduct());
                                        jObj.put("isBatchForProduct", ISD.getIstRequest().getProduct().isIsBatchForProduct());
                                        jObj.put("moduletype", modulType);
                                        jObj.put("remark", istRequest.getRemark());
                                        jObj.put("repairstore",defaultrepairstore); 
                                        jObj.put("repairstorelocation", defaultrepairstorelocation);
                                        if (detailStatus.equals("APPROVED")) {
                                            jObj.put("modifiedon", istRequest.getModifiedOn() != null ? df.format(istRequest.getModifiedOn()) : "");
                                        }
                                        if (ISD.getIstRequest() != null && ISD.getDeliveredLocation() != null) {
                                            int approvePermissionCount = consignmentService.isQAApprovePermissionForUser(ISD.getIssuedLocation().getCompany(), ISD.getIstRequest().getToStore() == null ? "" : ISD.getIstRequest().getToStore().getId(), ISD.getDeliveredLocation().getId(), userId);
                                            jObj.put("approvepermissioncount", approvePermissionCount);
                                        }
                                        InspectionTemplate it = ISD.getIstRequest().getProduct().getInspectionTemplate();
                                        jObj.put("inspectionTemplate", it != null ? it.getId() : null);
                                        int attachmentCount = saApprovalService.getAttachmentCount(ISD.getIstRequest().getCompany(), ISD.getId());
                                        jObj.put("attachment", attachmentCount);
                                        jArray.put(jObj);
                                    }
                                }
                            } else {
                                JSONObject jObj = new JSONObject();
                                jObj.put("id", ISD.getId() == null ? "" : ISD.getId());
                                String detailStatus = wocDetailIstMapping.getQuantityDue() > 0 ? "PENDING" : "APPROVED";
                                jObj.put("status", detailStatus);
                                if (detailStatus.equals("APPROVED")) {
                                    jObj.put("supervisor", istRequest.getCreatedBy() != null ? istRequest.getCreatedBy().getFullName() : "");
                                }
                                jObj.put("locationname",woLocationName);
                                jObj.put("locationid", ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getId());
                                jObj.put("fromlocationname", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getName());
                                jObj.put("fromlocationid", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getId());
                                jObj.put("batchname", ISD.getBatchName());
                                if (StringUtil.isNullOrEmpty(ISD.getBatchName())) {
                                    jObj.put("batchname", "-");
                                }
                                double qty = ISD.getIssuedQuantity() - ISD.getQaApproved() - ISD.getQaRejected();
                                
                                jObj.put("quantity",qty);
                                jObj.put("actualquantity", ISD.getIssuedQuantity());
                                jObj.put("issuedserialnames", ISD.getIssuedSerialNames());
                                jObj.put("serialname", ISD.getIssuedSerialNames());
                                jObj.put("returnserialnames", ISD.getReturnSerialNames());
                                jObj.put("isreusable", ISD.getIstRequest().getProduct().getItemReusability());
                                jObj.put("leadtime", ISD.getIstRequest().getProduct().getQALeadTimeInDays());
                                jObj.put("productname", ISD.getIstRequest().getProduct().getName());
                                jObj.put("productdescription", ISD.getIstRequest().getProduct().getDescription());
                                jObj.put("transactionno", ISD.getIstRequest().getTransactionNo());
                                jObj.put("storename", woStorename);
                                jObj.put("productcode", ISD.getIstRequest().getProduct().getProductid());
                                jObj.put("isSerialForProduct", ISD.getIstRequest().getProduct().isIsSerialForProduct());
                                jObj.put("isBatchForProduct", ISD.getIstRequest().getProduct().isIsBatchForProduct());
                                jObj.put("moduletype", modulType);
                                jObj.put("remark", istRequest.getRemark());
                                jObj.put("repairstore",defaultrepairstore); 
                                jObj.put("repairstorelocation", defaultrepairstorelocation);
                                if (detailStatus.equals("APPROVED")) {
                                    jObj.put("modifiedon", istRequest.getModifiedOn() != null ? df.format(istRequest.getModifiedOn()) : "");
                                }
                                if (ISD.getIstRequest() != null && ISD.getDeliveredLocation() != null) {
                                    int approvePermissionCount = consignmentService.isQAApprovePermissionForUser(ISD.getIssuedLocation().getCompany(), ISD.getIstRequest().getToStore() == null ? "" : ISD.getIstRequest().getToStore().getId(), ISD.getDeliveredLocation().getId(), userId);
                                    jObj.put("approvepermissioncount", approvePermissionCount);
                                }
                                InspectionTemplate it = ISD.getIstRequest().getProduct().getInspectionTemplate();
                                jObj.put("inspectionTemplate", it != null ? it.getId() : null);
                                int attachmentCount = saApprovalService.getAttachmentCount(ISD.getIstRequest().getCompany(), ISD.getId());
                                jObj.put("attachment", attachmentCount);
                                if (qty > 0.0) {
                                    jArray.put(jObj);
                                }
                            }
                        }
                    }
                    /**
                     * Get all approved inter store transfer request and create
                     * JSON with respect to inter store transfer details created
                     * for approved IST.
                     */
                    if (approvedRequests != null && !approvedRequests.isEmpty()) {
                        for (InterStoreTransferRequest approvedRequest : approvedRequests) {
                            for (ISTDetail outgoingISTDetail : approvedRequest.getIstDetails()) {
                                JSONObject jObj = new JSONObject();
                                jObj.put("id", outgoingISTDetail.getId() == null ? "" : outgoingISTDetail.getId());
                                String detailStatus = "APPROVED";
                                jObj.put("status", detailStatus);
                                if (detailStatus.equals("APPROVED")) {
                                    jObj.put("supervisor", approvedRequest.getCreatedBy() != null ? approvedRequest.getCreatedBy().getFullName() : "");
                                }
                                jObj.put("locationname",woLocationName);
                                jObj.put("locationid", outgoingISTDetail.getDeliveredLocation().getId());
                                jObj.put("fromlocationname", outgoingISTDetail.getIssuedLocation() == null ? "" : outgoingISTDetail.getIssuedLocation().getName());
                                jObj.put("fromlocationid", outgoingISTDetail.getIssuedLocation() == null ? "" : outgoingISTDetail.getIssuedLocation().getId());
                                jObj.put("batchname", outgoingISTDetail.getBatchName());
                                if (StringUtil.isNullOrEmpty(outgoingISTDetail.getBatchName())) {
                                    jObj.put("batchname", "-");
                                }
                                jObj.put("quantity", outgoingISTDetail.getDeliveredQuantity());
                                jObj.put("actualquantity", outgoingISTDetail.getIssuedQuantity());
                                jObj.put("issuedserialnames", outgoingISTDetail.getIssuedSerialNames());
                                jObj.put("serialname", outgoingISTDetail.getIssuedSerialNames());
                                jObj.put("returnserialnames", outgoingISTDetail.getReturnSerialNames());
                                jObj.put("isreusable", outgoingISTDetail.getIstRequest().getProduct().getItemReusability());
                                jObj.put("leadtime", outgoingISTDetail.getIstRequest().getProduct().getQALeadTimeInDays());
                                jObj.put("productname", outgoingISTDetail.getIstRequest().getProduct().getName());
                                jObj.put("productdescription", outgoingISTDetail.getIstRequest().getProduct().getDescription());
                                jObj.put("transactionno", outgoingISTDetail.getIstRequest().getTransactionNo());
                                jObj.put("storename", woStorename);
                                jObj.put("productcode", outgoingISTDetail.getIstRequest().getProduct().getProductid());
                                jObj.put("isSerialForProduct", outgoingISTDetail.getIstRequest().getProduct().isIsSerialForProduct());
                                jObj.put("isBatchForProduct", outgoingISTDetail.getIstRequest().getProduct().isIsBatchForProduct());
                                jObj.put("moduletype", modulType);
                                jObj.put("remark", outgoingISTDetail.getIstRequest().getRemark());
                                if (detailStatus.equals("APPROVED")) {
                                    jObj.put("modifiedon", approvedRequest.getModifiedOn() != null ? df.format(approvedRequest.getModifiedOn()) : "");
                                }
                                if (outgoingISTDetail.getIstRequest() != null && outgoingISTDetail.getDeliveredLocation() != null) {
                                    int approvePermissionCount = consignmentService.isQAApprovePermissionForUser(outgoingISTDetail.getIssuedLocation().getCompany(), outgoingISTDetail.getIstRequest().getToStore() == null ? "" : outgoingISTDetail.getIstRequest().getToStore().getId(), outgoingISTDetail.getDeliveredLocation().getId(), userId);
                                    jObj.put("approvepermissioncount", approvePermissionCount);
                                }
                                InspectionTemplate it = outgoingISTDetail.getIstRequest().getProduct().getInspectionTemplate();
                                jObj.put("inspectionTemplate", it != null ? it.getId() : null);
                                int attachmentCount = saApprovalService.getAttachmentCount(outgoingISTDetail.getIstRequest().getCompany(), outgoingISTDetail.getId());
                                jObj.put("attachment", attachmentCount);
                                jArray.put(jObj);
                            }
                        }
                    }

                    /**
                     * Get all rejected inter store transfer request and create
                     * JSON with respect to inter store transfer details created
                     * for rejected IST.
                     */
                    Set<RepairWOCDISTMapping> rejectedInterStoreTransferRequests = wocDetailIstMapping.getRejectedInterStoreTransferRequests();
                    if (rejectedInterStoreTransferRequests != null && !rejectedInterStoreTransferRequests.isEmpty()) {
                        for (RepairWOCDISTMapping rejectedInterStoreTransferRequest : rejectedInterStoreTransferRequests) {
                            InterStoreTransferRequest rejectedRequest = rejectedInterStoreTransferRequest.getInterStoreTransferRequest();
                            for (ISTDetail outgoingISTDetail : rejectedRequest.getIstDetails()) {
                                JSONObject jObj = new JSONObject();
                                jObj.put("id", outgoingISTDetail.getId() == null ? "" : outgoingISTDetail.getId());
                                String detailStatus = "REJECTED";
                                jObj.put("status", detailStatus);
                                jObj.put("supervisor", rejectedRequest.getCreatedBy() != null ? rejectedRequest.getCreatedBy().getFullName() : "");
                                jObj.put("locationname", woLocationName);
                                jObj.put("locationid", outgoingISTDetail.getDeliveredLocation().getId());
                                jObj.put("fromlocationname", outgoingISTDetail.getIssuedLocation() == null ? "" : outgoingISTDetail.getIssuedLocation().getName());
                                jObj.put("fromlocationid", outgoingISTDetail.getIssuedLocation() == null ? "" : outgoingISTDetail.getIssuedLocation().getId());
                                jObj.put("batchname", outgoingISTDetail.getBatchName());
                                if (StringUtil.isNullOrEmpty(outgoingISTDetail.getBatchName())) {
                                    jObj.put("batchname", "-");
                                }
                                jObj.put("quantity", outgoingISTDetail.getDeliveredQuantity());
                                jObj.put("actualquantity", outgoingISTDetail.getIssuedQuantity());
                                jObj.put("issuedserialnames", outgoingISTDetail.getIssuedSerialNames());
                                jObj.put("serialname", outgoingISTDetail.getIssuedSerialNames());
                                jObj.put("returnserialnames", outgoingISTDetail.getReturnSerialNames());
                                jObj.put("isreusable", outgoingISTDetail.getIstRequest().getProduct().getItemReusability());
                                jObj.put("leadtime", outgoingISTDetail.getIstRequest().getProduct().getQALeadTimeInDays());
                                jObj.put("productname", outgoingISTDetail.getIstRequest().getProduct().getName());
                                jObj.put("productdescription", outgoingISTDetail.getIstRequest().getProduct().getDescription());
                                jObj.put("transactionno", outgoingISTDetail.getIstRequest().getTransactionNo());
                                jObj.put("storename", woStorename);
                                jObj.put("productcode", outgoingISTDetail.getIstRequest().getProduct().getProductid());
                                jObj.put("isSerialForProduct", outgoingISTDetail.getIstRequest().getProduct().isIsSerialForProduct());
                                jObj.put("isBatchForProduct", outgoingISTDetail.getIstRequest().getProduct().isIsBatchForProduct());
                                jObj.put("moduletype", modulType);
                                jObj.put("remark", rejectedRequest.getRemark());
                                jObj.put("modifiedon", rejectedRequest.getModifiedOn() != null ? df.format(rejectedRequest.getModifiedOn()) : "");
                                if (outgoingISTDetail.getIstRequest() != null && outgoingISTDetail.getDeliveredLocation() != null) {
                                    int approvePermissionCount = consignmentService.isQAApprovePermissionForUser(outgoingISTDetail.getIssuedLocation().getCompany(), outgoingISTDetail.getIstRequest().getToStore() == null ? "" : outgoingISTDetail.getIstRequest().getToStore().getId(), outgoingISTDetail.getDeliveredLocation().getId(), userId);
                                    jObj.put("approvepermissioncount", approvePermissionCount);
                                }
                                InspectionTemplate it = outgoingISTDetail.getIstRequest().getProduct().getInspectionTemplate();
                                jObj.put("inspectionTemplate", it != null ? it.getId() : null);
                                int attachmentCount = saApprovalService.getAttachmentCount(outgoingISTDetail.getIstRequest().getCompany(), outgoingISTDetail.getId());
                                jObj.put("attachment", attachmentCount);
                                jArray.put(jObj);
                            }
                        }
                    }
                }
            }
        }
        return jArray;
    }
    
    public ModelAndView getAllQAApprovalReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "", TempDate="";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject extraCompanyPreferencesObj = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraCompanyPreferencesObj.getEntityList().get(0);
            boolean isQAapprovalForBuildAssembly=extraCompanyPreferences != null ? extraCompanyPreferences.isBuildAssemblyApprovalFlow() : false;
            
            String saId = "";
            String modulType = "", transactionNo = "";
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);
            String storeId = request.getParameter("storeid");
            String searchString = request.getParameter("ss");
            String type = request.getParameter("type");
            String statusType = request.getParameter("status");
            String fd = request.getParameter("frmDate");
            String td = request.getParameter("toDate");
            Store store = null;
            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
                paging = null;
            }
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

            Date fromDate = null;
            Date toDate = null;
            try {
                if (!StringUtil.isNullOrEmpty(fd) && !StringUtil.isNullOrEmpty(td)) {
                    fromDate = df.parse(fd);
                    toDate = df.parse(td);
                }
            } catch (ParseException ex) {
            }
            if (!StringUtil.isNullOrEmpty(statusType)) {
                if ("DONE".equals(statusType)) {
                    statusType = "3";
                } else {
                    statusType = "0";
                }
            }
            KwlReturnObject retObj = null;
            //sending My Account time zone diff to compare with created on Date.
            String tzdiff =sessionHandlerImpl.getTimeZoneDifference(request);
            List resultList = null;
            if (!storeSet.isEmpty() && storeSet != null) {
                retObj = consignmentService.getAllQARepairPendingList(companyId, null, null, type, statusType, storeSet, searchString, paging,tzdiff,isQAapprovalForBuildAssembly);
            }
            if (retObj != null && paging != null) {
                paging.setTotalRecord(retObj.getRecordTotalCount());
            }
            if (retObj != null) {
                resultList = retObj.getEntityList();
            }
            if (resultList != null) {
                Iterator itr = resultList.iterator();

                while (itr.hasNext()) {

                    Object[] roww = (Object[]) itr.next();

                    saId = roww[0] != null ? roww[0].toString() : "";
                    transactionNo = roww[1] != null ? roww[1].toString() : "";
                    modulType = roww[4] != null ? roww[4].toString() : "";

                    String movementStatus = request.getParameter("movementstatus");
                    Boolean isMovementStatus = false;
                    if (!StringUtil.isNullOrEmpty(movementStatus)) {
                        if (movementStatus.equals("1")) {
                            isMovementStatus = true;
                        }
                    }
                    if ("consignment".equals(modulType)) {

                        Consignment consign = consignmentService.getConsingmentById(saId);
                        Set<ConsignmentApprovalDetails> consList = consign.getConsignmentApprovalDetails();

                        for (ConsignmentApprovalDetails conmnt : consList) {
                            //conmnt.getModifiedOn() date is in UTC, so applying time zone diff on it & then compare.
                            if(conmnt.getModifiedOn() != null){
                                TempDate = authHandler.getUTCToUserLocalDateFormatter_NEW(request, conmnt.getModifiedOn());
                                dfrmt = authHandler.getUserDateFormatterWithoutTimeZone(request);//User Date Formatter
                            }
                            if (conmnt.getApprovalStatus() == ApprovalStatus.REJECTED && (conmnt.getRepairStatus() == ApprovalStatus.REPAIRPENDING || conmnt.getRepairStatus() == ApprovalStatus.RETURNTOREPAIR)
                                    && conmnt.isMovementStatus() == isMovementStatus && (fromDate != null && toDate != null && TempDate != null
                                    ? ((dfrmt.parse(TempDate).after(fromDate) || dfrmt.parse(TempDate).equals(fromDate))
                                    && (dfrmt.parse(TempDate).before(toDate) || dfrmt.parse(TempDate).equals(toDate))) : false)) {
                                JSONObject jObj = new JSONObject();
                                jObj.put("qaapprovalid", saId);
                                jObj.put("id", conmnt.getId());
                                jObj.put("transactionno", conmnt.getConsignment().getTransactionNo());
                                jObj.put("quantity", conmnt.getQuantity());
                                jObj.put("actualquantity", conmnt.getQuantity());
                                jObj.put("status", conmnt.getApprovalStatus());
                                jObj.put("productcode", conmnt.getConsignment().getProduct().getProductid());
                                jObj.put("productid", conmnt.getConsignment().getProduct().getID());
                                jObj.put("productname", conmnt.getConsignment().getProduct().getName());
                                jObj.put("productdescription", conmnt.getConsignment().getProduct().getDescription());
                                jObj.put("isSerialForProduct", conmnt.getConsignment().getProduct().isIsSerialForProduct());
                                jObj.put("isBatchForProduct", conmnt.getConsignment().getProduct().isIsBatchForProduct());
                                jObj.put("packaging", conmnt.getConsignment().getProduct().getPackaging());
                                jObj.put("transferinguom", conmnt.getConsignment().getProduct().getTransferUOM());
                                jObj.put("storename", conmnt.getConsignment().getStore().getFullName());
                                jObj.put("batchname", conmnt.getBatchName());
                                if (StringUtil.isNullOrEmpty(conmnt.getBatchName())) {
                                    jObj.put("batchname", "-");
                                }
                                jObj.put("movementstatus", conmnt.isMovementStatus());
                                jObj.put("serialname", conmnt.getSerialName());
                                if (StringUtil.isNullOrEmpty(conmnt.getSerialName())) {
                                    jObj.put("serialname", "-");
                                }
                                jObj.put("storeid", conmnt.getConsignment().getStore().getId());
                                jObj.put("uomname", conmnt.getConsignment().getUom() == null ? "" : conmnt.getConsignment().getUom().getNameEmptyforNA());
                                jObj.put("locationname", conmnt.getLocation() == null ? "" : conmnt.getLocation().getName());
                                jObj.put("locationid", conmnt.getLocation() == null ? "" : conmnt.getLocation().getId());
                                jObj.put("moduletype", modulType);
                                //To change UTC date into user date format as we are removing it from JS side.
                                jObj.put("transactiondate", conmnt.getModifiedOn() != null ? authHandler.getUTCToUserLocalDateFormatter_NEW(request,conmnt.getModifiedOn() ): "");
                                jObj.put("repairstatus", conmnt.getRepairStatus() != null ? conmnt.getRepairStatus() : "");
                                jObj.put("reason", conmnt.getReason() != null ? conmnt.getReason() : "");
                                jObj.put("remark", conmnt.getRemark() != null ? conmnt.getRemark() : "");
                                jArray.put(jObj);
                            }
                        }
                    }else  if (modulType.equals(Constants.BUILD_ASSEMBLY_QA_APPROVAL) && isQAapprovalForBuildAssembly) {
                        /*
                        This block is written for fetching job work build assembly products builds to show in qa approval
                        */
                        Map<String, String> requestMap = new HashMap<>();
                        requestMap.put("productbuildid", saId);
                        KwlReturnObject QaDetails = consignmentService.getBuildAssemblyProductQaDetails(requestMap); //saId ids referreed to productbuild table id.

                        List<AssemblyProductApprovalDetails> list = (List<AssemblyProductApprovalDetails>) QaDetails.getEntityList();

                        for (AssemblyProductApprovalDetails appDet : list) {
                            //stock repair report
                            if(appDet.getApprovalStatus() != AssemblyQAStatus.REJECTED){
                                continue;// continue is used for :- only rejected items in qa approval should be displayed here
                            }
                            JSONObject jObj = new JSONObject();
                            jObj.put("id", appDet.getId());
                            jObj.put("quantity", appDet.getQuantity());
                            jObj.put("actualquantity", appDet.getQuantity());
                            jObj.put("isbuildassemblyApprovalrec", true);
                            jObj.put("status", appDet.getApprovalStatus());

                            if (!StringUtil.isNullObject(appDet.getPrBuild())) {
                                jObj.put("transactionno", appDet.getPrBuild().getRefno());
                                jObj.put("productcode", appDet.getPrBuild().getProduct().getProductid());
                                jObj.put("productid", appDet.getPrBuild().getProduct().getID());
                                jObj.put("productname", appDet.getPrBuild().getProduct().getName());
                                jObj.put("productdescription", appDet.getPrBuild().getProduct().getDescription());
                                jObj.put("moduletype", modulType);
                                jObj.put("isSerialForProduct", appDet.getPrBuild().getProduct().isIsSerialForProduct());
                                jObj.put("isBatchForProduct", appDet.getPrBuild().getProduct().isIsBatchForProduct());
                                jObj.put("isreusable", appDet.getPrBuild().getProduct().getItemReusability());
                            }
                             jObj.put("transactiondate", appDet.getInspectionDate()!= null ? authHandler.getUTCToUserLocalDateFormatter_NEW(request,appDet.getInspectionDate() ): "");
                            jObj.put("reason", appDet.getReason());
                            String storeFullName = "";
                            if (!StringUtil.isNullObject(appDet.getWarehouse())) {
                                KwlReturnObject kwlSt = accountingHandlerDAO.getObject(Store.class.getName(), appDet.getWarehouse().getId());
                                store = (Store) kwlSt.getEntityList().get(0);
                                storeFullName = store.getFullName();
                            }
                            jObj.put("storename", storeFullName);
                            jObj.put("batchname", appDet.getProductBatch() != null ? appDet.getProductBatch().getBatchname() : appDet.getBatchname());
                            if (StringUtil.isNullOrEmpty(appDet.getBatchname())) {
                                jObj.put("batchname", "-");
                            }
                            jObj.put("serialname", appDet.getSerial() != null ? appDet.getSerial().getSerialname() : appDet.getSerialname());
                            if (StringUtil.isNullOrEmpty(appDet.getSerialname())) {
                                jObj.put("serialname", "-");
                            }
                            jObj.put("storeid", appDet.getWarehouse() == null ? "" : appDet.getWarehouse().getId());
                            jObj.put("locationname", appDet.getLocation() == null ? "" : appDet.getLocation().getName());
                            int approvePermissionCount = consignmentService.isQAApprovePermissionForUser(appDet.getPrBuild().getCompany(), appDet.getWarehouse() == null ? "" : appDet.getWarehouse().getId(), appDet.getLocation() == null ? "" : appDet.getLocation().getId(), userId);
                            jObj.put("approvepermissioncount", 1);
                            jArray.put(jObj);

                        }
                    } else if ("stockrequest".equals(modulType) || "stocktransfer".equals(modulType)) {
                        jeresult = accountingHandlerDAO.getObject(StockTransferApproval.class.getName(), saId);
                        StockTransferApproval stApproval = (StockTransferApproval) jeresult.getEntityList().get(0);

                        List<StockTransferDetailApproval> stda = stockTransferApprovalService.getStockTransferDetailApprovalList(stApproval, paging);
                        for (StockTransferDetailApproval sa : stda) {
                            if(sa.getModifiedOn() != null){
                                TempDate = authHandler.getUTCToUserLocalDateFormatter_NEW(request, sa.getModifiedOn());
                                 dfrmt = authHandler.getUserDateFormatterWithoutTimeZone(request);//User Date Formatter
                            }
                            if (sa.getApprovalStatus() == ApprovalStatus.REJECTED && (sa.getRepairStatus() == ApprovalStatus.REPAIRPENDING || sa.getRepairStatus() == ApprovalStatus.RETURNTOREPAIR) 
                                    && sa.isMovementStatus() == isMovementStatus && (fromDate != null && toDate != null && TempDate != null 
                                    ? ((dfrmt.parse(TempDate).after(fromDate) || dfrmt.parse(TempDate).equals(fromDate)) 
                                    && (dfrmt.parse(TempDate).before(toDate) || dfrmt.parse(TempDate).equals(toDate))) : false)) {
                                JSONObject jObj = new JSONObject();
                                jObj.put("qaapprovalid", saId);
                                jObj.put("id", sa.getId());
                                jObj.put("status", sa.getApprovalStatus());
                                jObj.put("serialname", sa.getSerialName());
                                if (StringUtil.isNullOrEmpty(sa.getSerialName())) {
                                    jObj.put("serialname", "-");
                                }
                                //To change UTC date into user date format as we are removing it from JS side.
                                jObj.put("transactiondate", sa.getModifiedOn() != null ? authHandler.getUTCToUserLocalDateFormatter_NEW(request, sa.getModifiedOn()) : "");
                                String stockTransferId = sa.getStockTransferDetailId();
                                TransactionModule module = stApproval.getTransactionModule();
                                if (module == TransactionModule.INTER_STORE_TRANSFER) {
                                    ISTDetail ISD = interStoreTransferService.getISTDetailById(stockTransferId);
                                    if (ISD != null) {
                                        jObj.put("fromlocationname", ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getName());
                                        jObj.put("locationname", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getName());
                                        jObj.put("fromlocationid", ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getId());
                                        jObj.put("locationid", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getId());
                                        jObj.put("tolocationname", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getName());
                                        jObj.put("tolocationid", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getId());
                                        jObj.put("batchname", ISD.getBatchName());
                                        if (StringUtil.isNullOrEmpty(ISD.getBatchName())) {
                                            jObj.put("batchname", "-");
                                        }
                                        jObj.put("quantity", sa.getQuantity());
                                        jObj.put("actualquantity", sa.getQuantity());
                                        jObj.put("movementstatus", sa.isMovementStatus());
                                        jObj.put("issuedserialnames", ISD.getIssuedSerialNames());
                                        jObj.put("serialname", sa.getSerialName());
                                        if (StringUtil.isNullOrEmpty(sa.getSerialName())) {
                                            jObj.put("serialname", "-");
                                        }
                                        jObj.put("returnserialnames", ISD.getReturnSerialNames());
                                        jObj.put("productname", ISD.getIstRequest().getProduct().getName());
                                        jObj.put("productdescription", ISD.getIstRequest().getProduct().getDescription());
                                        jObj.put("isSerialForProduct", ISD.getIstRequest().getProduct().isIsSerialForProduct());
                                        jObj.put("isBatchForProduct", ISD.getIstRequest().getProduct().isIsBatchForProduct());
                                        jObj.put("transactionno", ISD.getIstRequest().getTransactionNo());
                                        jObj.put("storename", ISD.getIstRequest().getToStore().getFullName());
                                        jObj.put("storeid", ISD.getIstRequest().getToStore().getId());
                                        jObj.put("productcode", ISD.getIstRequest().getProduct().getProductid());
                                        jObj.put("productid", ISD.getIstRequest().getProduct().getID());
                                        jObj.put("moduletype", modulType);
                                        jObj.put("remark", sa.getRemark());
                                        jObj.put("reason", sa.getReason());
                                        jObj.put("moduleid", sa.getRemark());
                                        jArray.put(jObj);

                                    }
                                } else if (module == TransactionModule.STOCK_REQUEST) {
                                    StockRequestDetail SRD = stockRequestService.getStockRequestDetail(stockTransferId);
                                    if (SRD != null) {
                                        jObj.put("fromlocationname", SRD.getIssuedLocation() == null ? "" : SRD.getIssuedLocation().getName());
                                        jObj.put("fromlocationid", SRD.getIssuedLocation() == null ? "" : SRD.getIssuedLocation().getId());
                                        jObj.put("locationid", SRD.getIssuedLocation() == null ? "" : SRD.getIssuedLocation().getId());
                                        jObj.put("locationname", SRD.getDeliveredLocation() == null ? "" : SRD.getDeliveredLocation().getName());
                                        jObj.put("tolocationid", SRD.getDeliveredLocation() == null ? "" : SRD.getDeliveredLocation().getId());
                                        jObj.put("batchname", SRD.getBatchName());
                                        if (StringUtil.isNullOrEmpty(SRD.getBatchName())) {
                                            jObj.put("batchname", "-");
                                        }
                                        jObj.put("quantity", sa.getQuantity());
                                        jObj.put("actualquantity", sa.getQuantity());
                                        jObj.put("movementstatus", sa.isMovementStatus());
                                        jObj.put("issuedserialnames", SRD.getIssuedSerialNames());
                                        jObj.put("isreusable", SRD.getStockRequest().getProduct().getItemReusability());
                                        jObj.put("leadtime", SRD.getStockRequest().getProduct().getQALeadTimeInDays());
                                        jObj.put("reusablecount", (sa.getApprovalStatus() == sa.getApprovalStatus().APPROVED || sa.getApprovalStatus() == sa.getApprovalStatus().REJECTED) ? SRD.getStockRequest().getProduct().getTotalIssueCount() : "");
                                        jObj.put("returnserialnames", SRD.getReturnSerialNames());
                                        jObj.put("productname", SRD.getStockRequest().getProduct().getName());
                                        jObj.put("productdescription", SRD.getStockRequest().getProduct().getDescription());
                                        jObj.put("isSerialForProduct", SRD.getStockRequest().getProduct().isIsSerialForProduct());
                                        jObj.put("isBatchForProduct", SRD.getStockRequest().getProduct().isIsBatchForProduct());
                                        jObj.put("storename", SRD.getStockRequest().getToStore().getFullName());
                                        jObj.put("storeid", SRD.getStockRequest().getToStore().getId());
                                        jObj.put("transactionno", SRD.getStockRequest().getTransactionNo());
                                        jObj.put("productcode", SRD.getStockRequest().getProduct().getProductid());
                                        jObj.put("productid", SRD.getStockRequest().getProduct().getID());
                                        jObj.put("moduletype", modulType);
                                        InspectionTemplate it = SRD.getStockRequest().getProduct().getInspectionTemplate();
                                        jObj.put("inspectionTemplate", it != null ? it.getId() : null);
                                        jObj.put("remark", sa.getRemark());
                                        jObj.put("reason", sa.getReason());
                                        jArray.put(jObj);

                                    }
                                }
                            }
                        }

                    } else if ("stockout".equals(modulType)) {
                        jeresult = accountingHandlerDAO.getObject(SAApproval.class.getName(), saId);
                        SAApproval saApproval = (SAApproval) jeresult.getEntityList().get(0);

                        List<SADetailApproval> saa = saApprovalService.getStockAdjutmentDetailApprovalList(saApproval, paging);
                        for (SADetailApproval sa : saa) {
                            if(sa.getModifiedOn() != null){
                                TempDate = authHandler.getUTCToUserLocalDateFormatter_NEW(request, sa.getModifiedOn());
                                dfrmt = authHandler.getUserDateFormatterWithoutTimeZone(request);//User Date Formatter
                            }
                            if (sa.getApprovalStatus() == ApprovalStatus.REJECTED && (sa.getRepairStatus() == ApprovalStatus.REPAIRPENDING || sa.getRepairStatus() == ApprovalStatus.RETURNTOREPAIR)
                                    && sa.isMovementStatus() == isMovementStatus && (fromDate != null && toDate != null && TempDate != null 
                                    ? ((dfrmt.parse(TempDate).after(fromDate) || dfrmt.parse(TempDate).equals(fromDate)) 
                                    && (dfrmt.parse(TempDate).before(toDate) || dfrmt.parse(TempDate).equals(toDate))) : false)) {
                                JSONObject jObj = new JSONObject();
                                jObj.put("qaapprovalid", saId);
                                jObj.put("id", sa.getId());
                                jObj.put("status", sa.getApprovalStatus());
                                jObj.put("serialname", sa.getSerialName());
                                if (StringUtil.isNullOrEmpty(sa.getSerialName())) {
                                    jObj.put("serialname", "-");
                                }
                                jObj.put("batchname", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getBatchName());
                                if (sa.getStockAdjustmentDetail() != null && StringUtil.isNullOrEmpty(sa.getStockAdjustmentDetail().getBatchName())) {
                                    jObj.put("batchname", "-");
                                }
                                jObj.put("quantity", sa.getQuantity());
                                jObj.put("actualquantity", sa.getQuantity());
                                jObj.put("movementstatus", sa.isMovementStatus());
                                jObj.put("productcode", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().getProductid());
                                jObj.put("productid", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().getID());
                                jObj.put("locationname", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getLocation() == null ? "" : sa.getStockAdjustmentDetail().getLocation().getName());
                                jObj.put("locationid", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getLocation() == null ? "" : sa.getStockAdjustmentDetail().getLocation().getId());
                                jObj.put("productname", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().getName());
                                jObj.put("productdescription", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().getDescription());
                                jObj.put("isSerialForProduct", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().isIsSerialForProduct());
                                jObj.put("isBatchForProduct", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().isIsBatchForProduct());
                                jObj.put("transactionno", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getTransactionNo());
                                jObj.put("storename", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getStore().getFullName());
                                jObj.put("storeid", sa.getStockAdjustmentDetail() == null ? "" : sa.getStockAdjustmentDetail().getStockAdjustment().getStore().getId());
                                jObj.put("isreusable", sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().getItemReusability());
                                jObj.put("reusablecount", (sa.getApprovalStatus() == sa.getApprovalStatus().APPROVED || sa.getApprovalStatus() == sa.getApprovalStatus().REJECTED) ? sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().getTotalIssueCount() : "");
                                jObj.put("leadtime", sa.getStockAdjustmentDetail().getStockAdjustment().getProduct().getQALeadTimeInDays());
                                jObj.put("moduletype", modulType);
                                jObj.put("remark", sa.getRemark());
                                jObj.put("reason", sa.getReason());
                                //To change UTC date into user date format as we are removing it from JS side.
                                jObj.put("transactiondate", sa.getModifiedOn() != null ? authHandler.getUTCToUserLocalDateFormatter_NEW(request, sa.getModifiedOn()) : "");
                                jArray.put(jObj);
                            }
                        }
                    } else if ("goodsreceipt".equals(modulType)) {
                        jArray = getGRNDataForStockRepair(jArray, saId, modulType, transactionNo);
                    } else if (Constants.MRP_WORK_ORDER.equals(modulType)) {
                        jArray = getWODataForStockRepair(jArray, saId, modulType, transactionNo);
                    } else if ("deliveryorder".equals(modulType)) {
                        jArray = getDeliveryOrderDataForStockRepair(jArray, saId, modulType, transactionNo);
                    }
                }
                if (isExport) {
                    jobj.put("data", jArray);
                    exportDAO.processRequest(request, response, jobj);
                }
                issuccess = true;
                msg = "QA Approval Report  has been fetched successfully";
            }

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
                    jobj.put("count", jArray.length());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    private JSONArray getGRNDataForStockRepair(JSONArray jArray, String saId, String modulType, String transactionNo) throws JSONException, ServiceException {
        KwlReturnObject jeresult = accountingHandlerDAO.getObject(RepairGRODetailISTMapping.class.getName(), saId);
        RepairGRODetailISTMapping repairGRODetailISTMapping = (RepairGRODetailISTMapping) jeresult.getEntityList().get(0);
        if (repairGRODetailISTMapping != null && repairGRODetailISTMapping.getGrodistmapping() != null) {
            GRODetailISTMapping grodistm = repairGRODetailISTMapping.getGrodistmapping();
            InterStoreTransferRequest grodist = grodistm.getInterStoreTransferRequest();
            if (!grodist.getIstDetails().isEmpty()) {
                Set<ISTDetail> grodistdetails = grodist.getIstDetails();
                ISTDetail grodistdetail = null;
                for (ISTDetail istdetail : grodistdetails) {
                    grodistdetail = istdetail;
                }
                List serialList = new ArrayList();
                if (grodist.getProduct() != null && grodist.getProduct().isIsSerialForProduct()) {
                    if (repairGRODetailISTMapping.getRepairRejectedISTRequest() != null && !repairGRODetailISTMapping.getRepairRejectedISTRequest().isEmpty()) {
                        for (InterStoreTransferRequest ist : repairGRODetailISTMapping.getRepairRejectedISTRequest()) {
                            for (ISTDetail istdetail : ist.getIstDetails()) {
                                if (!StringUtil.isNullOrEmpty(istdetail.getDeliveredSerialNames())) {
                                    serialList.addAll(Arrays.asList(istdetail.getDeliveredSerialNames().split(",")));
                                }
                            }
                        }
                    }
                    if (repairGRODetailISTMapping.getRejectedStockOuts() != null && !repairGRODetailISTMapping.getRejectedStockOuts().isEmpty()) {
                        for (StockAdjustment adjustment : repairGRODetailISTMapping.getRejectedStockOuts()) {
                            for (StockAdjustmentDetail adjustmentDetail : adjustment.getStockAdjustmentDetail()) {
                                serialList.addAll(Arrays.asList(adjustmentDetail.getFinalSerialNames().split(",")));
                            }
                        }
                    }
                }
                InterStoreTransferRequest istRequest = repairGRODetailISTMapping.getInterStoreTransferRequest();
                Set<ISTDetail> istdetails = istRequest.getIstDetails();
                for (ISTDetail ISD : istdetails) {
                    if (repairGRODetailISTMapping.getRejectedQuantityDue() > 0.0) {
                        if (grodist.getProduct() != null && grodist.getProduct().isIsSerialForProduct() && serialList.contains(ISD.getIssuedSerialNames())) {
                            continue;
                        }
                        JSONObject jObj = new JSONObject();
                        jObj.put("qaapprovalid", saId);
                        jObj.put("id", ISD.getId() == null ? "" : ISD.getId());
                        jObj.put("moduletype", modulType);
                        String detailStatus = repairGRODetailISTMapping.getRejectedQuantityDue() > 0.0 ? "REJECTED" : "APPROVED";
                        jObj.put("status", detailStatus);
                        jObj.put("locationname", (grodistdetail != null && grodistdetail.getIssuedLocation() != null) ? grodistdetail.getIssuedLocation().getName() : (ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getName()));
                        jObj.put("locationid", (grodistdetail != null && grodistdetail.getIssuedLocation() != null) ? grodistdetail.getIssuedLocation().getId() : (ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getId()));
                        jObj.put("batchname", ISD.getBatchName());
                        if (StringUtil.isNullOrEmpty(ISD.getBatchName())) {
                            jObj.put("batchname", "-");
                        }
                        double qty = ISD.getDeliveredQuantity() - ISD.getApprovedQtyFromRepairStore() - ISD.getRejectedQtyFromRepairStore();
                        jObj.put("quantity", qty);
                        jObj.put("actualquantity", qty);        
                        jObj.put("serialname", ISD.getIssuedSerialNames());
                        jObj.put("leadtime", ISD.getIstRequest().getProduct().getQALeadTimeInDays());
                        jObj.put("productname", ISD.getIstRequest().getProduct().getName());
                        jObj.put("productdescription", ISD.getIstRequest().getProduct().getDescription());
                        jObj.put("transactionno", transactionNo);
                        jObj.put("storename", grodist.getFromStore().getFullName());
                        jObj.put("productcode", ISD.getIstRequest().getProduct().getProductid());
                        jObj.put("isSerialForProduct", ISD.getIstRequest().getProduct().isIsSerialForProduct());
                        jObj.put("isBatchForProduct", ISD.getIstRequest().getProduct().isIsBatchForProduct());
                        jObj.put("moduletype", modulType);
                        jObj.put("remark", istRequest.getRemark());
                        jObj.put("transactiondate", istRequest.getModifiedOn() != null ? df.format(istRequest.getModifiedOn()) : "");
                        jArray.put(jObj);
                    }
                }
            }
        }
        return jArray;
    }
    
    
    private JSONArray getWODataForStockRepair(JSONArray jArray, String saId, String modulType, String transactionNo) throws JSONException, ServiceException {
        KwlReturnObject jeresult = accountingHandlerDAO.getObject(RepairWOCDISTMapping.class.getName(), saId);
        RepairWOCDISTMapping repairWOCDetailISTMapping = (RepairWOCDISTMapping) jeresult.getEntityList().get(0);
        if (repairWOCDetailISTMapping != null && repairWOCDetailISTMapping.getWocdistmapping() != null) {
            WOCDetailISTMapping wocdistm = repairWOCDetailISTMapping.getWocdistmapping();
            InterStoreTransferRequest wocdist = wocdistm.getInterStoreTransferRequest();
            if (!wocdist.getIstDetails().isEmpty()) {
                Set<ISTDetail> wocdistdetails = wocdist.getIstDetails();
                ISTDetail wocdistdetail = null;
                for (ISTDetail istdetail : wocdistdetails) {
                    wocdistdetail = istdetail;
                }
                List serialList = new ArrayList();
                if (wocdist.getProduct() != null && wocdist.getProduct().isIsSerialForProduct()) {
                    if (repairWOCDetailISTMapping.getRepairRejectedISTRequest() != null && !repairWOCDetailISTMapping.getRepairRejectedISTRequest().isEmpty()) {
                        for (InterStoreTransferRequest ist : repairWOCDetailISTMapping.getRepairRejectedISTRequest()) {
                            for (ISTDetail istdetail : ist.getIstDetails()) {
                                if (!StringUtil.isNullOrEmpty(istdetail.getDeliveredSerialNames())) {
                                    serialList.addAll(Arrays.asList(istdetail.getDeliveredSerialNames().split(",")));
                                }
                            }
                        }
                    }
                    if (repairWOCDetailISTMapping.getRejectedStockOuts() != null && !repairWOCDetailISTMapping.getRejectedStockOuts().isEmpty()) {
                        for (StockAdjustment adjustment : repairWOCDetailISTMapping.getRejectedStockOuts()) {
                            for (StockAdjustmentDetail adjustmentDetail : adjustment.getStockAdjustmentDetail()) {
                                serialList.addAll(Arrays.asList(adjustmentDetail.getFinalSerialNames().split(",")));
                            }
                        }
                    }
                }
                InterStoreTransferRequest istRequest = repairWOCDetailISTMapping.getInterStoreTransferRequest();
                Set<ISTDetail> istdetails = istRequest.getIstDetails();
                for (ISTDetail ISD : istdetails) {
                    if (repairWOCDetailISTMapping.getRejectedQuantityDue() > 0.0) {
                        if (wocdist.getProduct() != null && wocdist.getProduct().isIsSerialForProduct() && serialList.contains(ISD.getIssuedSerialNames())) {
                            continue;
                        }
                        JSONObject jObj = new JSONObject();
                        jObj.put("qaapprovalid", saId);
                        jObj.put("id", ISD.getId() == null ? "" : ISD.getId());
                        jObj.put("moduletype", modulType);
                        String detailStatus = repairWOCDetailISTMapping.getRejectedQuantityDue() > 0.0 ? "REJECTED" : "APPROVED";
                        jObj.put("status", detailStatus);
                        jObj.put("locationname", (wocdistdetail != null && wocdistdetail.getIssuedLocation() != null) ? wocdistdetail.getIssuedLocation().getName() : (ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getName()));
                        jObj.put("locationid", (wocdistdetail != null && wocdistdetail.getIssuedLocation() != null) ? wocdistdetail.getIssuedLocation().getId() : (ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getId()));
                        jObj.put("batchname", ISD.getBatchName());
                        if (StringUtil.isNullOrEmpty(ISD.getBatchName())) {
                            jObj.put("batchname", "-");
                        }
                        double qty = ISD.getDeliveredQuantity() - ISD.getApprovedQtyFromRepairStore() - ISD.getRejectedQtyFromRepairStore();
                        jObj.put("quantity", qty);
                        jObj.put("actualquantity", qty);
                        jObj.put("serialname", ISD.getIssuedSerialNames());
                        jObj.put("leadtime", ISD.getIstRequest().getProduct().getQALeadTimeInDays());
                        jObj.put("productname", ISD.getIstRequest().getProduct().getName());
                        jObj.put("productdescription", ISD.getIstRequest().getProduct().getDescription());
                        jObj.put("transactionno", transactionNo);
                        jObj.put("storename", wocdist.getFromStore().getFullName());
                        jObj.put("productcode", ISD.getIstRequest().getProduct().getProductid());
                        jObj.put("isSerialForProduct", ISD.getIstRequest().getProduct().isIsSerialForProduct());
                        jObj.put("isBatchForProduct", ISD.getIstRequest().getProduct().isIsBatchForProduct());
                        jObj.put("moduletype", modulType);
                        jObj.put("remark", istRequest.getRemark());
                        jObj.put("transactiondate", istRequest.getModifiedOn() != null ? df.format(istRequest.getModifiedOn()) : "");
                        jArray.put(jObj);
                    }
                }
            }
        }
        return jArray;
    }
    
    public void sendUserProductQAApprovalRejectionMail(Company company, List approvedrejectedDetailMapList, String extraEmailIds,String salesPersonEmailIds, User loggedInUser, String transactionNo,boolean isApprove,String moduleName) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kwlCompany = null;
        KwlReturnObject result = null;
        List ll;
        String msg = null, companyId, companyName;
        try {

            String qaApprovalRejectionDateID = "";
            kwlCompany = accountingHandlerDAO.getCompanyList();
            ll = kwlCompany.getEntityList();


            companyId = company.getCompanyID();
            companyName = company.getCompanyName();

            /**
             * 0=Before Due Date 1=On Due Date 2=After Due Date
             */
            int moduleId, days, beforeAfter;

            String fieldid = "", mailContent = "", mailSubject = "";
            result = accountingHandlerDAO.getNotifications(companyId);
            List<NotificationRules> list = result.getEntityList();
            for (NotificationRules nr : list) {
                Calendar cld = Calendar.getInstance();
                moduleId = nr.getModuleId();
                fieldid = nr.getFieldid();
                days = nr.getDays();
                mailContent = nr.getMailcontent();
                mailSubject = nr.getMailsubject();
                String otherEmails = nr.getEmailids();
                beforeAfter = nr.getBeforeafter();
                if (beforeAfter == 2) {
                    days = -days;
                }
                cld.add(Calendar.DATE, days);
                String userLname = "", userFname = "";
                List<String> userEmailList = new ArrayList<String>();

                if (!StringUtil.isNullOrEmpty(nr.getUsers()) || !StringUtil.isNullOrEmpty(otherEmails) || !StringUtil.isNullOrEmpty(extraEmailIds) || !StringUtil.isNullOrEmpty(salesPersonEmailIds)) {
                    String[] userids = nr.getUsers().split(",");

                    
                    switch (moduleId) {
                        case Constants.Acc_Product_Master_ModuleId:
                            
                            if(isApprove){
                                qaApprovalRejectionDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.Product_QA_Inspection_Approval)).optString("fieldid", "");
                            }else{
                                qaApprovalRejectionDateID = new JSONObject(Constants.staticGlobalDateFields.get(Constants.Product_QA_Inspection_Rejection)).optString("fieldid", "");
                            }
                            break;
                    }
                    if (moduleId == Constants.Acc_Product_Master_ModuleId && fieldid.equals(qaApprovalRejectionDateID)) {
                    if (userids.length > 0) {
                        KwlReturnObject userDetailobj = accountingHandlerDAO.getUserDetailObj(userids);
                        List<User> user = userDetailobj.getEntityList();
                        for (User ur : user) {
                            userLname = StringUtil.isNullOrEmpty(ur.getLastName()) ? "" : ur.getLastName();
                            userFname = StringUtil.isNullOrEmpty(ur.getFirstName()) ? "" : ur.getFirstName();
                            String Mailid = StringUtil.isNullOrEmpty(ur.getEmailID()) ? "" : ur.getEmailID();
                            if (!StringUtil.isNullOrEmpty(Mailid)) {
                                userEmailList.add(Mailid);
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(otherEmails)) {
                        String[] otherMails = otherEmails.split(",");
                        for (String Mailid : otherMails) {
                            userEmailList.add(Mailid);
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(extraEmailIds)) {
                        String[] extraMail = extraEmailIds.split(",");
                        for (String Mailid : extraMail) {
                            userEmailList.add(Mailid);
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(salesPersonEmailIds) && nr.isMailToSalesPerson()) {
                        String[] extraMail = salesPersonEmailIds.split(",");
                        for (String Mailid : extraMail) {
                            userEmailList.add(Mailid);
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(nr.getUsers())) {
                        String userIds[] = nr.getUsers().split(",");
                        KwlReturnObject userRes=null;
                        for (String userId : userIds) {
                            User user = null;
                            userRes = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
                            user = (User) userRes.getEntityList().get(0);
                            if(user != null && !StringUtil.isNullOrEmpty(user.getEmailID())){
                                userEmailList.add(user.getEmailID());
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(nr.getEmailids())) { //copy to emails address
                        String copyToEmailIdArr[] = nr.getEmailids().split(",");
                        for(String usermail : copyToEmailIdArr){
                            userEmailList.add(usermail);
                        }
                    }
                        if (nr.isMailToStoreManager()) {
                            for (int i = 0; i < approvedrejectedDetailMapList.size(); i++) {
                                Map approvedRejectedItemsDetail = (Map) approvedrejectedDetailMapList.get(i);
                                if (approvedRejectedItemsDetail != null && approvedRejectedItemsDetail.get("storeManagerEmailIds") != null) {
                                    String email = (String) approvedRejectedItemsDetail.get("storeManagerEmailIds");
                                    String[] extraMail = email.split(",");
                                    for (String Mailid : extraMail) {
                                        userEmailList.add(Mailid);
                                    }
                                }
                                if (approvedRejectedItemsDetail != null && approvedRejectedItemsDetail.get("repairStoreManagerEmailIds") != null) {
                                    String email = (String) approvedRejectedItemsDetail.get("repairStoreManagerEmailIds");
                                    String[] extraMail = email.split(",");
                                    for (String Mailid : extraMail) {
                                        userEmailList.add(Mailid);
                                    }
                                }
                            }
                        }
                        String[] userMailid = userEmailList.toArray(new String[userEmailList.size()]);
                        sendProductQAApprovalRejectionNotificationMail(companyId, companyName, loggedInUser, userFname, userLname, userMailid, mailContent, mailSubject, beforeAfter, days, approvedrejectedDetailMapList, transactionNo,moduleName);
                    }
                }
            }

        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            jobj.put("msg", msg);
        }

    }

    private void sendProductQAApprovalRejectionNotificationMail(String companyId, String companyName, User loggedInUser, String userFname, String userLname, String[] userMailid, String mailContent, String mailSubject, int beforeAfter, int days, List approvedrejectedDetailMapList, String transactionNo,String moduleName) { // 0-before , 1 after 
        KwlReturnObject kwlReturnObj = null;
        try {
            Date todayDate = new Date();
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            DateFormat dbformat = new SimpleDateFormat("yyyy-MM-dd");
            String today = formatter.format(todayDate);
            int duration = days;
            ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
            boolean removeBatchField = false;
            boolean removeSerialField = false;

            todayDate.setHours(0);
            todayDate.setMinutes(0);
            todayDate.setSeconds(0);

            Calendar currentSystemDate = Calendar.getInstance();
            currentSystemDate.setTime(todayDate);

            List finalData = new ArrayList();

            List headerItems = new ArrayList();
            headerItems.add("No.");
            headerItems.add("Transaction No.");
            headerItems.add("Product Code");
            headerItems.add("Product Name");
            headerItems.add("Quantity");
            headerItems.add("Batch");
            headerItems.add("Serial");



            String htmlText = "";
            String subject = mailSubject;
            mailSubject = mailSubject.replaceAll("#Document_Number#", transactionNo);
            mailContent = mailContent.replaceAll("#Document_Number#", transactionNo);
            mailContent = mailContent.replaceAll("#User_Name#", loggedInUser.getFullName());
            String plainMsg = "";
            //String from = Constants.ADMIN_EMAILID;
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) returnObject.getEntityList().get(0);
            String from = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
//            if (loggedInUser != null) {
//                if (!StringUtil.isNullOrEmpty(loggedInUser.getEmailID())) {
//                    from = loggedInUser.getEmailID();
//                }
//            }

            htmlText = mailContent.replace(Constants.DateLabel_MailContent_Placeholder, today);
            htmlText = htmlText.replace(Constants.TodayDateLabel_MailContent_Placeholder, today);
            htmlText = htmlText.concat("<br/>");
            htmlText = htmlText.concat("<b>Module Name : </b>"+moduleName+"<br/>");
            htmlText = htmlText.concat("<br/>");

            if (userMailid.length > 0) {

                int sno = 1;

                if (approvedrejectedDetailMapList != null && !approvedrejectedDetailMapList.isEmpty()) {
                    for (int i = 0; i < approvedrejectedDetailMapList.size(); i++) {
                        Map datamap = (Map) approvedrejectedDetailMapList.get(i);
                        List data = new ArrayList();
                        data.add(sno);
                        data.add(transactionNo); //Transaction No.
                        data.add(datamap.get("productId")); //product code
                        data.add(datamap.get("productName")); //product name
                        data.add(datamap.get("quantity")); //quantity

                        if (datamap.get("batchName") != null) {
                            String batch = datamap.get("batchName").toString();
                            if (!StringUtil.isNullOrEmpty(batch)) {
                                data.add(batch); // batch
                            } else {
                                removeBatchField = true;
                            }
                        } else {
                            removeBatchField = true;
                        }

                        if (datamap.get("serialNames") != null) {
                            String serial = datamap.get("serialNames").toString();
                            if (!StringUtil.isNullOrEmpty(serial)) {
                                data.add(serial); // serial
                            } else {
                                removeSerialField = true;
                            }
                        } else {
                            removeSerialField = true;
                        }



                        finalData.add(data);
                        sno++;
                    }

                }

                if (removeBatchField) {
                    headerItems.remove("Batch");
                }
                if (removeSerialField) {
                    headerItems.remove("Serial");
                }

                if (sno > 1) {
                    for (Object header : headerItems) {
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        String a = header.toString();
                        headerprop.setAlign("left");
                        headerprop.setData(a);
                        if("Product Name".equalsIgnoreCase(a)){
                            headerprop.setWidth("200px");
                        }else{
                        headerprop.setWidth("50px");
                        }
                        headerlist.add(headerprop);
                    }
                    List finalProductList = new ArrayList();
                    for (Object headerdata : finalData) {
                        ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                        List datalist = (List) headerdata;
                        for (Object hdata : datalist) {
                            CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                            prop.setAlign("left");
                            prop.setData(hdata.toString());
                            prodlist.add(prop);
                        }
                        finalProductList.add(prodlist);
                    }
                    String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                    StringWriter writer = new StringWriter();
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalProductList);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);
                    velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    htmlText = htmlText.concat(tablehtml);
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    Set userEmailSet=new HashSet<String>();
                    for(String mailId: userMailid){
                        userEmailSet.add(mailId);
                    }
                    userMailid=Strings.toStringArray(userEmailSet);
                    SendMailHandler.postMail(userMailid, subject, htmlText, plainMsg, from, smtpConfigMap);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void sendUserProductQAApprovalMail(Company company, List rejectionDetailMapList, String extraEmailIds,String salesPersonEmailIds, User loggedInUser, String transactionNo) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {

//        public void sendTransactionEmails(String[] toEmailIds,String ccmailids,String subject,String htmlMsg,String plainMsg,String companyid) throws ServiceException 
        String mailContent = "", mailSubject = "";
        KwlReturnObject result = accountingHandlerDAO.getNotifications(company.getCompanyID());
        List<NotificationRules> list = result.getEntityList();
        for (NotificationRules nr : list) {
            if (nr.getModuleId() == Constants.Acc_Product_Master_ModuleId && 19 == Integer.parseInt(nr.getFieldid())) {
                mailContent = nr.getMailcontent();
                mailSubject = nr.getMailsubject();
                String otherEmails = nr.getEmailids();
                String userLname = "", userFname = "";
                List<String> userEmailList = new ArrayList<String>();
                if (!StringUtil.isNullOrEmpty(nr.getUsers()) || !StringUtil.isNullOrEmpty(otherEmails) || !StringUtil.isNullOrEmpty(extraEmailIds)) {
                    String[] userids = nr.getUsers().split(",");
                    if (userids.length > 0) {
                        KwlReturnObject userDetailobj = accountingHandlerDAO.getUserDetailObj(userids);
                        List<User> user = userDetailobj.getEntityList();
                        for (User ur : user) {
                            userLname = StringUtil.isNullOrEmpty(ur.getLastName()) ? "" : ur.getLastName();
                            userFname = StringUtil.isNullOrEmpty(ur.getFirstName()) ? "" : ur.getFirstName();
                            String Mailid = StringUtil.isNullOrEmpty(ur.getEmailID()) ? "" : ur.getEmailID();
                            if (!StringUtil.isNullOrEmpty(Mailid)) {
                                userEmailList.add(Mailid);
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(otherEmails)) {
                        String[] otherMails = otherEmails.split(",");
                        for (String Mailid : otherMails) {
                            userEmailList.add(Mailid);
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(extraEmailIds)) {
                        String[] extraMail = extraEmailIds.split(",");
                        for (String Mailid : extraMail) {
                            userEmailList.add(Mailid);
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(salesPersonEmailIds)) {
                        String[] extraMail = salesPersonEmailIds.split(",");
                        for (String Mailid : extraMail) {
                            userEmailList.add(Mailid);
                        }
                    }//
                    String[] userMailid = userEmailList.toArray(new String[userEmailList.size()]);
                        mailSubject = mailSubject.replaceAll("#Document_Number#", transactionNo);
                        mailContent = mailContent.replaceAll("#Document_Number#", transactionNo);
                        mailContent = mailContent.replaceAll("#User_Name#", loggedInUser.getFullName());
                        String[] toEmailIds = new String[0];
                        if(userMailid.length > 0){
                            toEmailIds = userMailid;
                        }
                        accountingHandlerDAOobj.sendTransactionEmails(toEmailIds, extraEmailIds, mailSubject, mailContent, mailContent, company.getCompanyID());
                }
                break;
            }
        }

    }
    
    /**
     * Method used to approve or reject delivery order from QA tab.
     */
    private List<Object> approvedOrRejectDeliveryOrderFromQA(Store packingStore, Store repairStore, Company company, ExtraCompanyPreferences extraCompanyPreferences, CompanyAccountPreferences companyAccountPreferences, String remark, JSONArray jArr, String operation, String operationMsg, String auditMessage, SeqFormat seqFormat, String stockAdjustmentTransactionNo, HttpServletRequest request, User user) throws AccountingException, InventoryException {
        StringBuilder productIds = new StringBuilder();
        String newid=null;
        try {
            InterStoreTransferRequest outgoingISTRequest = null, ist = null;
            double totalQuantityDue = 0, totalRejectedQty = 0, rejectedQty = 0, totalPickedQty = 0;
            StringBuilder rejectedSerials = new StringBuilder();
            StringBuilder approvedSerials = new StringBuilder();
            DODQCISTMapping dodqcistm = null;
            String doNumber = null;
            boolean isfirst = true;
            boolean isAlreadyApprovedorRejected = false;
            boolean isserialApprovedorRejected = false;
            for (int i = 0; i < jArr.length(); i++) {
                /**
                 * Approve/Reject from QA store.
                 */
                JSONObject json = jArr.getJSONObject(i);
                String detailid = json.optString("recordid");
                String serialname = json.optString("serialname");
                double quantity = json.optDouble("quantity");
                KwlReturnObject jeresult1 = accountingHandlerDAO.getObject(ISTDetail.class.getName(), detailid);
                ISTDetail istd = (ISTDetail) jeresult1.getEntityList().get(0);
                ist = istd.getIstRequest();
                Product product = ist.getProduct();
                JSONObject params = new JSONObject();
                params.put("istrequest", ist.getId());
                KwlReturnObject result = stockService.getDODetailISTMapping(params);
                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    dodqcistm = (DODQCISTMapping) result.getEntityList().get(0);
                }
                if (dodqcistm != null) {
                    JSONObject jsonParams = new JSONObject();
                    jsonParams.put(Constants.companyKey, company.getCompanyID());
                    jsonParams.put("mappingid", dodqcistm.getID());
                    doNumber = stockService.getDeliveryOrderNumberUsingMapping(jsonParams);
                    totalQuantityDue = dodqcistm.getQuantityDue();
                    /*
                    * Check quantitydue before approving transaction to avoid negative stock
                    * Quantitydue contains pending stock in QA
                    */
                    if (totalQuantityDue<=0 || totalQuantityDue<quantity){
                        isAlreadyApprovedorRejected = true;
                    }
                    if (!StringUtil.isNullOrEmpty(serialname)) {
                    StringBuilder processedSerials = new StringBuilder(dodqcistm.getApprovedSerials()+","+dodqcistm.getRejectedSerials());
                    if(!StringUtil.isNullOrEmpty(processedSerials.toString())){
                        String[] processedSerial = processedSerials.toString().split(",");
                        for(String serial : processedSerial){
                            if(serial.equals(serialname)){
                                isserialApprovedorRejected = true;
                            }
                        }
                    }
                    }
                    if (!StringUtil.isNullOrEmpty(doNumber)) {
                        if ("Approve".equalsIgnoreCase(operation)) {
                            if (extraCompanyPreferences.isPickpackship()) {
                                /**
                                 * If Pick, Pack and Ship flow is activated for
                                 * the company then add an entry in
                                 * DeliveryDetailInterStoreLocationMapping into
                                 * packing warehouse.
                                 */
                                if (outgoingISTRequest == null && ist != null) {
                                    outgoingISTRequest = new InterStoreTransferRequest(ist.getProduct(), ist.getToStore(), packingStore, ist.getUom());
                                    outgoingISTRequest.setRemark(remark);
                                    outgoingISTRequest.setAcceptedQty(quantity);
                                    outgoingISTRequest.setOrderedQty(quantity);
                                    outgoingISTRequest.setBusinessDate(new Date());
                                    outgoingISTRequest.setPackaging(ist.getProduct().getPackaging());
                                    totalQuantityDue = dodqcistm.getQuantityDue();
                                    totalRejectedQty = dodqcistm.getRejectedQty();
                                    totalPickedQty = dodqcistm.getPickedQty();
                                } else {
                                    outgoingISTRequest.setOrderedQty(outgoingISTRequest.getOrderedQty() + quantity);
                                    outgoingISTRequest.setAcceptedQty(outgoingISTRequest.getAcceptedQty() + quantity);
                                }
                                ISTDetail outISTDetail = new ISTDetail();
                                outISTDetail.setBatchName(istd.getBatchName());
                                outISTDetail.setDeliveredBin(istd.getIssuedBin());
                                outISTDetail.setDeliveredQuantity(quantity);
                                outISTDetail.setDeliveredRack(istd.getIssuedRack());
                                outISTDetail.setDeliveredRow(istd.getIssuedRow());
                                outISTDetail.setDeliveredSerialNames(serialname);
                                outISTDetail.setDeliveredLocation(packingStore.getDefaultLocation());
                                outISTDetail.setIssuedBin(istd.getDeliveredBin());
                                outISTDetail.setIssuedLocation(istd.getDeliveredLocation());
                                outISTDetail.setIssuedQuantity(quantity);
                                outISTDetail.setIssuedRack(istd.getDeliveredRack());
                                outISTDetail.setIssuedRow(istd.getDeliveredRow());
                                outISTDetail.setIssuedSerialNames(serialname);
                                outISTDetail.setIstRequest(outgoingISTRequest);
                                if (outgoingISTRequest != null) {
                                    outgoingISTRequest.getIstDetails().add(outISTDetail);
                                }
                                istd.setQaApproved(istd.getQaApproved()+ quantity);
                                outgoingISTRequest.setTransactionNo(stockAdjustmentTransactionNo);
                                if (!StringUtil.isNullOrEmpty(doNumber)) {
                                    totalRejectedQty += quantity;
                                    totalPickedQty += quantity;
                                    rejectedQty += quantity;
                                    outgoingISTRequest.setMemo("Approved Delivery Order : " + doNumber);
                                    if (!StringUtil.isNullOrEmpty(serialname)) {
                                        if (StringUtil.isNullOrEmpty(approvedSerials.toString())) {
                                            approvedSerials.append(StringUtil.isNullOrEmpty(dodqcistm.getApprovedSerials()) ? "" : dodqcistm.getApprovedSerials());
                                        }
                                        if (approvedSerials.length() > 0) {
                                            approvedSerials.append(",").append(serialname);
                                        } else {
                                            approvedSerials.append(serialname);
                                        }
                                    }
                                    totalQuantityDue -= quantity;
                                }
                                
                                auditMessage = "User " + user.getFullName() + " has Approved Delivery Order: " + doNumber + " [Product: " + outgoingISTRequest.getProduct().getProductid() + ", Quantity: " + rejectedQty + ", Store: " + ist.getFromStore().getAbbreviation() + (!StringUtil.isNullOrEmpty(approvedSerials.toString()) ? ", Serials: (" + approvedSerials.toString() + ")" : "");
                                auditTrailObj.insertAuditLog(AuditAction.QA_INSPECTION, auditMessage, request, "0");
                                /*
                                HashMap<String, Object> filterRequestParams = new HashMap<>();
                                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                                filter_names.add("company.companyID");
                                filter_params.add(company.getCompanyID());
                                filter_names.add("masterGroup.ID");
                                filter_params.add("10"); // For getting DO status ID
                                filter_names.add("value");
                                filter_params.add("Picked");
                                filterRequestParams.put("filter_names", filter_names);
                                filterRequestParams.put("filter_params", filter_params);
                                KwlReturnObject retObj = accMasterItemsDAO.getMasterItems(filterRequestParams);
                                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                    MasterItem status = (MasterItem) retObj.getEntityList().get(0);
                                    String statusID = status.getID();
                                    if (!StringUtil.isNullOrEmpty(statusID)) {
                                        JSONObject doParams = new JSONObject();
                                        doParams.put(Constants.companyKey, company.getCompanyID());
                                        doParams.put("dodetailid", dodqcistm.getDodetailID());
                                        doParams.put("statusID", statusID);
                                        stockService.updateDeliveryOrderStatus(doParams);
                                    }
                                }*/
                            } else {
                                /*
                                * Check quantitydue before approving transaction to avoid negative stock
                                * Quantitydue contains pending stock in QA
                                */
                                if (isAlreadyApprovedorRejected){
                                    throw new AccountingException("This item is already processed");
                                }else if(istd.getIssuedQuantity()-(istd.getApprovedQtyFromRepairStore()+istd.getRejectedQtyFromRepairStore()) < quantity){
                                    throw new AccountingException("This item is already processed");
                                }
                                Date businessDate = new Date();
                                double productPrice = stockService.getProductPurchasePrice(product, businessDate);
                                StockAdjustment createStockAdjustment = new StockAdjustment(product, istd.getIstRequest().getToStore(), istd.getIstRequest().getUom(), -quantity, productPrice, businessDate);
                                createStockAdjustment.setAdjustmentType("Stock Out");
                                createStockAdjustment.setTransactionNo(stockAdjustmentTransactionNo);
                                createStockAdjustment.setMemo("Approved Stock for DO: " + doNumber);
                                createStockAdjustment.setRemark(remark);
                                createStockAdjustment.setCompany(company);
                                createStockAdjustment.setCreatedOn(businessDate);
                                createStockAdjustment.setCreationdate(businessDate.getTime());
                                createStockAdjustment.setTransactionNo(stockAdjustmentTransactionNo);
                                createStockAdjustment.setTransactionModule(TransactionModule.STOCK_ADJUSTMENT);
                                createStockAdjustment.setApprovedDODQCISTMapping(dodqcistm);
                                Set<StockAdjustmentDetail> adjustmentDetailSet = new HashSet<>();
                                StockAdjustmentDetail stockAdjustmentDetail = new StockAdjustmentDetail();
                                stockAdjustmentDetail.setBatchName(istd.getBatchName());
                                stockAdjustmentDetail.setBin(istd.getDeliveredBin());
                                stockAdjustmentDetail.setRack(istd.getDeliveredRack());
                                stockAdjustmentDetail.setRow(istd.getDeliveredRow());
                                stockAdjustmentDetail.setLocation(istd.getDeliveredLocation());
                                stockAdjustmentDetail.setFinalQuantity(quantity);
                                stockAdjustmentDetail.setQuantity(quantity);
                                stockAdjustmentDetail.setFinalSerialNames(serialname);
                                stockAdjustmentDetail.setSerialNames(serialname);
                                stockAdjustmentDetail.setStockAdjustment(createStockAdjustment);
                                adjustmentDetailSet.add(stockAdjustmentDetail);
                                createStockAdjustment.setStockAdjustmentDetail(adjustmentDetailSet);
                                JournalEntry inventoryJE = null;
                                if (extraCompanyPreferences.isActivateMRPModule() || companyAccountPreferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) {
                                    if (product.getInventoryAccount() != null) {
                                        Map<String, Object> JEFormatParams = new HashMap<>();
                                        JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                        JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                                        JEFormatParams.put("companyid", company.getCompanyID());
                                        JEFormatParams.put("isdefaultFormat", true);
                                        KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                        if (kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty()) {
                                            SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                            if (format != null) {
                                                Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                                                Map<String, Object> seqNumberMap = new HashMap<>();
                                                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(company.getCompanyID(), StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, businessDate);
                                                jeDataMap.put("entrynumber", (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER));
                                                jeDataMap.put("autogenerated", true);
                                                jeDataMap.put(Constants.SEQFORMAT, format.getID());
                                                jeDataMap.put(Constants.SEQNUMBER, (String) seqNumberMap.get(Constants.SEQNUMBER));
                                                jeDataMap.put(Constants.DATEPREFIX, (String) seqNumberMap.get(Constants.DATEPREFIX));
                                                jeDataMap.put(Constants.DATEAFTERPREFIX, (String) seqNumberMap.get(Constants.DATEAFTERPREFIX));
                                                jeDataMap.put(Constants.DATESUFFIX, (String) seqNumberMap.get(Constants.DATESUFFIX));
                                                jeDataMap.put("entrydate", businessDate);
                                                jeDataMap.put("companyid", company.getCompanyID());
                                                jeDataMap.put("memo", "Stock Adjustment JE for " + product.getName());
                                                jeDataMap.put("createdby", sessionHandlerImpl.getUserid(request));
                                                jeDataMap.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
                                                jeDataMap.put("transactionModuleid", Constants.Inventory_Stock_Adjustment_ModuleId);
                                                KwlReturnObject jeresult = journalEntryDAO.saveJournalEntry(jeDataMap); // Create Journal entry without JEdetails
                                                inventoryJE = (JournalEntry) jeresult.getEntityList().get(0);
                                                createStockAdjustment.setInventoryJE(inventoryJE);
                                                HashSet jeDetails = new HashSet();
                                                JSONObject jedjson = new JSONObject();
                                                jedjson.put("srno", jeDetails.size() + 1);
                                                jedjson.put("companyid", company.getCompanyID());
                                                jedjson.put("amount", authHandler.round(((quantity * productPrice) * (-1)), company.getCompanyID()));
                                                jedjson.put("debit", false);
                                                jedjson.put("accountid", product.getInventoryAccount().getID());
                                                jedjson.put("jeid", inventoryJE.getID());
                                                KwlReturnObject jedresult = journalEntryDAO.addJournalEntryDetails(jedjson);
                                                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                                jeDetails.add(jed);
                                                jedjson = new JSONObject();
                                                jedjson.put("srno", jeDetails.size() + 1);
                                                jedjson.put("companyid", company.getCompanyID());
                                                jedjson.put("amount", authHandler.round(((quantity * productPrice) * (-1)), company.getCompanyID()));
                                                jedjson.put("accountid", product.getStockAdjustmentAccount().getID());
                                                jedjson.put("debit", true);
                                                jedjson.put("jeid", inventoryJE.getID());
                                                jedresult = journalEntryDAO.addJournalEntryDetails(jedjson);
                                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                                jeDetails.add(jed);
                                                inventoryJE.setDetails(jeDetails);
                                                journalEntryDAO.saveJournalEntryDetailsSet(jeDetails);
                                            } else {
                                                throw new AccountingException("No default Sequence Format found. Please add a default format for Journal Entry.");
                                            }
                                        } else {
                                            throw new AccountingException("No default Sequence Format found. Please add a default format for Journal Entry.");
                                        }
                                    } else {
                                        throw new AccountingException("Please set inventory account for product: " + product.getProductid());
                                    }
                                }
                                HashMap<String, Object> requestparams = new HashMap<>();
                                requestparams.put("locale", RequestContextUtils.getLocale(request));
                                stockAdjustmentService.requestStockAdjustment(user, createStockAdjustment, false, false, null, requestparams);
                                if (inventoryJE != null) {
                                    inventoryJE.setTransactionId(createStockAdjustment.getId());
                                }
                                JSONObject repairParams = new JSONObject();
                                repairParams.put("id", dodqcistm.getID());
                                repairParams.put("approvedStockOut", createStockAdjustment);
                                repairParams.put("approvedQty", dodqcistm.getApprovedQty() + quantity);
                                repairParams.put("quantityDue", dodqcistm.getQuantityDue() - quantity);
                                if (!StringUtil.isNullOrEmpty(serialname)) {
                                    if(isserialApprovedorRejected){
                                        throw new AccountingException("This item is already processed");
                                    }
                                    if (StringUtil.isNullOrEmpty(approvedSerials.toString())) {
                                        approvedSerials.append(StringUtil.isNullOrEmpty(dodqcistm.getApprovedSerials()) ? "" : dodqcistm.getApprovedSerials());
                                    }
                                    if (approvedSerials.length() > 0) {
                                        approvedSerials.append(",").append(serialname);
                                    } else {
                                        approvedSerials.append(serialname);
                                    }
                                    repairParams.put("approvedSerials", approvedSerials.toString());
                                }
                                interStoreTransferService.saveDODQCISTMapping(repairParams);
                                String StUom = createStockAdjustment.getUom() != null ? createStockAdjustment.getUom().getNameEmptyforNA() : "";
                                auditMessage = "(Product :" + product.getProductid() + ", Quantity :" + (-quantity) + " " + StUom + ", AdjustmentType : " + "Stock Out " + ")";
                                auditMessage = "User " + user.getFullName() + " has created Stock Adjustment: " + stockAdjustmentTransactionNo + " for Store: " + istd.getIstRequest().getToStore().getAbbreviation() + " " + auditMessage;
                                auditTrailObj.insertAuditLog(AuditAction.STOCK_ADJUSTMENT_ADDED, auditMessage, request, "0");
                                //Insert audit trial entry of Approved DO
                                auditMessage = "Approved Delivery Order: " + doNumber + " [Product: " + product.getProductid() + ", Quantity: " + (-quantity) + ", Store: " + istd.getIstRequest().getFromStore().getAbbreviation() + (!StringUtil.isNullOrEmpty(serialname) ? ", Serials: (" + serialname + ")]" : "]");
                                auditMessage = "User " + user.getFullName() + " has " + auditMessage;
                                auditTrailObj.insertAuditLog(AuditAction.QA_INSPECTION, auditMessage, request, "0");
                                
                                if (productIds.indexOf(product.getID()) == -1) {
                                    productIds.append(product.getID()).append(",");
                                }                            
                                istd.setQaApproved(istd.getQaApproved()+ quantity);
                                StockAdjustmentDetail sad;
                                Iterator iterator = createStockAdjustment.getStockAdjustmentDetail().iterator();
                                while (iterator.hasNext()) {
                                    sad = (StockAdjustmentDetail) iterator.next();
                                    if (isfirst) {
                                        newid = sad.getId();
                                        isfirst = false;
                                    } else {
                                        newid = newid + "," + sad.getId();
                                    }
                                }
                            }
                        } else if ("Reject".equalsIgnoreCase(operation)) {
                            if (outgoingISTRequest == null && ist != null) {
                                outgoingISTRequest = new InterStoreTransferRequest(ist.getProduct(), ist.getToStore(), repairStore, ist.getUom());
                                outgoingISTRequest.setRemark(remark);
                                outgoingISTRequest.setAcceptedQty(quantity);
                                outgoingISTRequest.setOrderedQty(quantity);
                                outgoingISTRequest.setBusinessDate(new Date());
                                outgoingISTRequest.setPackaging(ist.getProduct().getPackaging());
                                totalQuantityDue = dodqcistm.getQuantityDue();
                                totalRejectedQty = dodqcistm.getRejectedQty();
                            } else {
                                outgoingISTRequest.setOrderedQty(outgoingISTRequest.getOrderedQty() + quantity);
                                outgoingISTRequest.setAcceptedQty(outgoingISTRequest.getAcceptedQty() + quantity);
                            }
                            /*
                                * Check quantitydue before approving transaction to avoid negative stock
                                * Quantitydue contains pending stock in QA
                                */
                            if (isAlreadyApprovedorRejected){
                                throw new AccountingException("This item is already processed");
                            } else if(istd.getIssuedQuantity()-(istd.getApprovedQtyFromRepairStore()+istd.getRejectedQtyFromRepairStore()) < quantity){
                                throw new AccountingException("This item is already processed");
                            }
                            ISTDetail outISTDetail = new ISTDetail();
                            outISTDetail.setBatchName(istd.getBatchName());
                            outISTDetail.setDeliveredBin(istd.getIssuedBin());
                            outISTDetail.setDeliveredQuantity(quantity);
                            outISTDetail.setDeliveredRack(istd.getIssuedRack());
                            outISTDetail.setDeliveredRow(istd.getIssuedRow());
                            outISTDetail.setDeliveredSerialNames(serialname);
                            if (operation.equals("Reject")) {
                                outISTDetail.setDeliveredLocation(repairStore.getDefaultLocation());
                            } else {
                                outISTDetail.setDeliveredLocation(istd.getIssuedLocation());
                            }
                            outISTDetail.setIssuedBin(istd.getDeliveredBin());
                            outISTDetail.setIssuedLocation(istd.getDeliveredLocation());
                            outISTDetail.setIssuedQuantity(quantity);
                            outISTDetail.setIssuedRack(istd.getDeliveredRack());
                            outISTDetail.setIssuedRow(istd.getDeliveredRow());
                            outISTDetail.setIssuedSerialNames(serialname);
                            outISTDetail.setIstRequest(outgoingISTRequest);
                            if (outgoingISTRequest != null) {
                                outgoingISTRequest.getIstDetails().add(outISTDetail);
                            }
                            outgoingISTRequest.setTransactionNo(stockAdjustmentTransactionNo);
                            if (!StringUtil.isNullOrEmpty(doNumber)) {
                                totalRejectedQty += quantity;
                                rejectedQty += quantity;
                                outgoingISTRequest.setMemo("Rejected Delivery Order : " + doNumber);
                                if (!StringUtil.isNullOrEmpty(serialname)) {
                                    if(isserialApprovedorRejected){
                                        throw new AccountingException("This item is already processed");
                                    }
                                    if (StringUtil.isNullOrEmpty(rejectedSerials.toString())) {
                                        rejectedSerials.append(StringUtil.isNullOrEmpty(dodqcistm.getRejectedSerials()) ? "" : dodqcistm.getRejectedSerials());
                                    }
                                    if (rejectedSerials.length() > 0) {
                                        rejectedSerials.append(",").append(serialname);
                                    } else {
                                        rejectedSerials.append(serialname);
                                    }
                                }
                                totalQuantityDue -= quantity;
                            }
                            istd.setQaRejected(istd.getQaRejected() + quantity);
                            //Insert audit trial entry of Rejected DO
                            auditMessage = "Rejected Delivery Order: " + doNumber + " [Product: " + product.getProductid() + ", Quantity: " + quantity + ", Store: " + ist.getFromStore().getAbbreviation() + (!StringUtil.isNullOrEmpty(serialname) ? ", Serials: (" + serialname + ")]" : "]");
                            auditMessage = "User " + user.getFullName() + " has " + auditMessage + " ";
                            auditTrailObj.insertAuditLog(AuditAction.QA_INSPECTION, auditMessage, request, "0");
                        }
                    }
                }
            }
            if (outgoingISTRequest != null && ist != null && ("Reject".equalsIgnoreCase(operation) || "Approve".equalsIgnoreCase(operation) && extraCompanyPreferences.isPickpackship())) {
                Map<String, Object> requestParams = new HashMap<>();
                if ("Approve".equalsIgnoreCase(operation) && extraCompanyPreferences.isPickpackship()) {
                    requestParams.put("dod", dodqcistm.getDodetailID());
                    requestParams.put("dodqcistmapping", dodqcistm);
                    requestParams.put("dodQCIstMappingID", dodqcistm.getID());
                    requestParams.put("quantityDue", totalQuantityDue);
                    requestParams.put("pickedQty", totalPickedQty);
                    if (!StringUtil.isNullOrEmpty(approvedSerials.toString())) {
                        requestParams.put("approvedSerials", approvedSerials.toString());
                    }
                    auditMessage = "Approved Delivery Order: " + doNumber + " [Product: " + outgoingISTRequest.getProduct().getProductid() + ", Quantity: " + rejectedQty + ", Store: " + ist.getFromStore().getAbbreviation() + (!StringUtil.isNullOrEmpty(approvedSerials.toString()) ? ", Serials: (" + approvedSerials.toString() + ")" : "");
                } else {
                    requestParams.put(Constants.CREATE_IST_FOR_REJECT_DELIVERYORDER_FROM_QC, true);
                    requestParams.put(Constants.companyid, company.getCompanyID());
                    requestParams.put("dodQCIstMappingID", dodqcistm.getID());
                    requestParams.put("operation", operation);
                    requestParams.put(Constants.moduleid, Constants.Acc_InterStore_ModuleId);
                    requestParams.put("quantityDue", totalQuantityDue);
                    requestParams.put("rejectedQty", totalRejectedQty);
                    requestParams.put("rejectQuantity", rejectedQty);
                    requestParams.put("rejectQuantityDue", rejectedQty);
                    auditMessage = "Rejected Delivery Order: " + doNumber + " [Product: " + outgoingISTRequest.getProduct().getProductid() + ", Quantity: " + rejectedQty + ", Store: " + ist.getFromStore().getAbbreviation() + (!StringUtil.isNullOrEmpty(rejectedSerials.toString()) ? ", Serials: (" + rejectedSerials.toString() + ")" : "");
                    if (!StringUtil.isNullOrEmpty(rejectedSerials.toString())) {
                        requestParams.put("rejectedSerials", rejectedSerials.toString());
                    }
                }
                interStoreTransferService.addInterStoreTransferRequest(user, outgoingISTRequest, false, requestParams);
                interStoreTransferService.acceptInterStoreTransferRequest(user, outgoingISTRequest);
                
                     
               // ISTDetail istd=outgoingISTRequest.getIstDetails().iterator().next();  //istd contains id of newly rejected or approved row in qa in case of do
                //newid=istd.getId();
                isfirst = true;
                ISTDetail istd;
                Iterator iterator = outgoingISTRequest.getIstDetails().iterator();
                while (iterator.hasNext()) {
                    istd = (ISTDetail) iterator.next();
                    if (isfirst) {
                        newid = istd.getId();
                        isfirst = false;
                    } else {
                        newid = newid + "," + istd.getId();
                    }
                }
               // productIds.append(newid);
                
            }
            seqService.updateSeqNumber(seqFormat);
        } catch (AccountingException | InventoryException ex) {
            throw new AccountingException(ex.getMessage());
        } catch (ServiceException | SessionExpiredException | NegativeInventoryException | SeqFormatException | JSONException ex) {
            Logger.getLogger(ApprovalController.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        List<Object> ids=new ArrayList<>();
        ids.add(productIds);
        ids.add(newid);
        return ids;
    }
    
    /**
     * Method to get transaction detail JSON in QC tab.
     */
    private JSONArray getDODetailApprovalTransactionJSON(JSONArray jArray, InterStoreTransferRequest istRequest, JSONObject json, String modulType, String userId) throws JSONException, ServiceException {
        KwlReturnObject resultList = stockService.getDODetailISTMapping(json);
        if (resultList != null && resultList.getEntityList() != null && !resultList.getEntityList().isEmpty()) {
            List<DODQCISTMapping> dodQCISTMappingList = resultList.getEntityList();
            if (dodQCISTMappingList != null && !dodQCISTMappingList.isEmpty()) {
                String grnStorename = "", grnLocationName = "", grnLocationId = "";
                DODQCISTMapping dodQCISTMapping = dodQCISTMappingList.get(0);
                if (dodQCISTMapping != null) {
                    Set<ISTDetail> istdetails = istRequest.getIstDetails();
                    grnStorename = istRequest.getFromStore().getFullName();
                    for (ISTDetail ISD : istdetails) {
                        grnLocationName = ISD.getIssuedLocation() != null ? ISD.getIssuedLocation().getName() : "";
                        grnLocationId = ISD.getIssuedLocation() != null ? ISD.getIssuedLocation().getId() : "";

                        List<String> serials = new ArrayList();
                        if (!StringUtil.isNullOrEmpty(dodQCISTMapping.getApprovedSerials())) {
                            /**
                             * Add all approved serials.
                             */
                            serials.addAll(Arrays.asList(dodQCISTMapping.getApprovedSerials().split(",")));
                        }
                        if (!StringUtil.isNullOrEmpty(dodQCISTMapping.getRejectedSerials())) {
                            /**
                             * Add all rejected serials.
                             */
                            serials.addAll(Arrays.asList(dodQCISTMapping.getRejectedSerials().split(",")));
                        }
                        /**
                         * If quantitydue is greater than 0, then only add
                         * pending IST request.
                         */
                        if (dodQCISTMapping.getQuantityDue() > 0.0) {
                            if (istRequest.getProduct().isIsSerialForProduct()) {
                                /**
                                 * If serial is activated for the product, then
                                 * split serial names using ',' as a delimeter.
                                 * We need to show separate row in QA approval
                                 * report for each serial.
                                 */
                                String[] serialNames = ISD.getIssuedSerialNames().split(",");
                                for (String serialName : serialNames) {
                                    if (!serials.contains(serialName)) {
                                        /**
                                         * Show serial which are not approved or
                                         * rejected.
                                         */
                                        JSONObject jObj = new JSONObject();
                                        jObj.put("id", ISD.getId() == null ? "" : ISD.getId());
                                        String detailStatus = dodQCISTMapping.getQuantityDue() > 0 ? "PENDING" : "APPROVED";
                                        jObj.put("status", detailStatus);
                                        if (detailStatus.equals("APPROVED")) {
                                            jObj.put("supervisor", istRequest.getCreatedBy() != null ? istRequest.getCreatedBy().getFullName() : "");
                                        }
                                        jObj.put("locationname", ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getName());
                                        jObj.put("locationid", ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getId());
                                        jObj.put("fromlocationname", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getName());
                                        jObj.put("fromlocationid", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getId());
                                        jObj.put("batchname", ISD.getBatchName());
                                        if (StringUtil.isNullOrEmpty(ISD.getBatchName())) {
                                            jObj.put("batchname", "-");
                                        }
                                        jObj.put("quantity", 1);
                                        jObj.put("actualquantity", ISD.getIssuedQuantity());
                                        jObj.put("issuedserialnames", serialName);
                                        jObj.put("serialname", serialName);
                                        jObj.put("returnserialnames", ISD.getReturnSerialNames());
                                        jObj.put("isreusable", ISD.getIstRequest().getProduct().getItemReusability());
                                        jObj.put("leadtime", ISD.getIstRequest().getProduct().getQALeadTimeInDays());
                                        jObj.put("productname", ISD.getIstRequest().getProduct().getName());
                                        jObj.put("productdescription", ISD.getIstRequest().getProduct().getDescription());
                                        jObj.put("transactionno", ISD.getIstRequest().getTransactionNo());
                                        jObj.put("storename", ISD.getIstRequest().getFromStore().getFullName());
                                        jObj.put("productcode", ISD.getIstRequest().getProduct().getProductid());
                                        jObj.put("isSerialForProduct", ISD.getIstRequest().getProduct().isIsSerialForProduct());
                                        jObj.put("isBatchForProduct", ISD.getIstRequest().getProduct().isIsBatchForProduct());
                                        jObj.put("moduletype", modulType);
                                        jObj.put("transactionmodule", Constants.Acc_InterStoreTransfer_modulename);
                                        jObj.put("billid", istRequest.getId());
                                        jObj.put("remark", istRequest.getRemark());
                                        if (detailStatus.equals("APPROVED")) {
                                            jObj.put("modifiedon", istRequest.getModifiedOn() != null ? df.format(istRequest.getModifiedOn()) : "");
                                        }
                                        if (ISD.getIstRequest() != null && ISD.getDeliveredLocation() != null) {
                                            int approvePermissionCount = consignmentService.isQAApprovePermissionForUser(ISD.getIssuedLocation().getCompany(), ISD.getIstRequest().getToStore() == null ? "" : ISD.getIstRequest().getToStore().getId(), ISD.getDeliveredLocation().getId(), userId);
                                            jObj.put("approvepermissioncount", approvePermissionCount);
                                        }
                                        InspectionTemplate it = ISD.getIstRequest().getProduct().getInspectionTemplate();
                                        jObj.put("inspectionTemplate", it != null ? it.getId() : null);
                                        int attachmentCount = saApprovalService.getAttachmentCount(ISD.getIstRequest().getCompany(), ISD.getId());
                                        jObj.put("attachment", attachmentCount);
                                        jArray.put(jObj);
                                    }
                                }
                            } else {
                                JSONObject jObj = new JSONObject();
                                jObj.put("id", ISD.getId() == null ? "" : ISD.getId());
                                String detailStatus = dodQCISTMapping.getQuantityDue() > 0 ? "PENDING" : "APPROVED";
                                jObj.put("status", detailStatus);
                                if (detailStatus.equals("APPROVED")) {
                                    jObj.put("supervisor", istRequest.getCreatedBy() != null ? istRequest.getCreatedBy().getFullName() : "");
                                }
                                jObj.put("locationname", ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getName());
                                jObj.put("locationid", ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getId());
                                jObj.put("fromlocationname", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getName());
                                jObj.put("fromlocationid", ISD.getDeliveredLocation() == null ? "" : ISD.getDeliveredLocation().getId());
                                jObj.put("batchname", ISD.getBatchName());
                                if (StringUtil.isNullOrEmpty(ISD.getBatchName())) {
                                    jObj.put("batchname", "-");
                                }
                                double qty = ISD.getIssuedQuantity() - ISD.getQaApproved() - ISD.getQaRejected();
                                if (qty == 0.0) {
                                    /**
                                     * Show record in QA Pending Report only if
                                     * Approved Quantity + Rejected Quantity is
                                     * less than issued quantity to QC store.
                                     */
                                    continue;
                                }
                                jObj.put("quantity", qty);
                                jObj.put("actualquantity", ISD.getIssuedQuantity());
                                jObj.put("issuedserialnames", ISD.getIssuedSerialNames());
                                jObj.put("serialname", ISD.getIssuedSerialNames());
                                jObj.put("returnserialnames", ISD.getReturnSerialNames());
                                jObj.put("isreusable", ISD.getIstRequest().getProduct().getItemReusability());
                                jObj.put("leadtime", ISD.getIstRequest().getProduct().getQALeadTimeInDays());
                                jObj.put("productname", ISD.getIstRequest().getProduct().getName());
                                jObj.put("productdescription", ISD.getIstRequest().getProduct().getDescription());
                                jObj.put("transactionno", ISD.getIstRequest().getTransactionNo());
                                jObj.put("storename", ISD.getIstRequest().getFromStore().getFullName());
                                jObj.put("productcode", ISD.getIstRequest().getProduct().getProductid());
                                jObj.put("isSerialForProduct", ISD.getIstRequest().getProduct().isIsSerialForProduct());
                                jObj.put("isBatchForProduct", ISD.getIstRequest().getProduct().isIsBatchForProduct());
                                jObj.put("moduletype", modulType);
                                jObj.put("transactionmodule", Constants.Acc_InterStoreTransfer_modulename);
                                jObj.put("billid", istRequest.getId());
                                jObj.put("remark", istRequest.getRemark());
                                if (detailStatus.equals("APPROVED")) {
                                    jObj.put("modifiedon", istRequest.getModifiedOn() != null ? df.format(istRequest.getModifiedOn()) : "");
                                }
                                if (ISD.getIstRequest() != null && ISD.getDeliveredLocation() != null) {
                                    int approvePermissionCount = consignmentService.isQAApprovePermissionForUser(ISD.getIssuedLocation().getCompany(), ISD.getIstRequest().getToStore() == null ? "" : ISD.getIstRequest().getToStore().getId(), ISD.getDeliveredLocation().getId(), userId);
                                    jObj.put("approvepermissioncount", approvePermissionCount);
                                }
                                InspectionTemplate it = ISD.getIstRequest().getProduct().getInspectionTemplate();
                                jObj.put("inspectionTemplate", it != null ? it.getId() : null);
                                int attachmentCount = saApprovalService.getAttachmentCount(ISD.getIstRequest().getCompany(), ISD.getId());
                                jObj.put("attachment", attachmentCount);
                                jArray.put(jObj);
                            }
                        }
                    }
                    /**
                     * Get all approved inter store transfer request and create
                     * JSON with respect to inter store transfer details created
                     * for approved IST.
                     */
                    Set<StockAdjustment> approvedRequests = dodQCISTMapping.getApprovedStockOuts();
                    for (StockAdjustment stockAdjustment : approvedRequests) {
                        JSONObject jsonParams = new JSONObject();
                        jsonParams.put(Constants.companyKey, stockAdjustment.getCompany().getCompanyID());
                        jsonParams.put("mappingid", stockAdjustment.getApprovedDODQCISTMapping() != null ? stockAdjustment.getApprovedDODQCISTMapping().getID() : "");
                        String doNumber = stockService.getDeliveryOrderNumberUsingMapping(jsonParams);
                        for (StockAdjustmentDetail adjustmentDetail : stockAdjustment.getStockAdjustmentDetail()) {
                            JSONObject jObj = new JSONObject();
                            jObj.put("id", adjustmentDetail.getId() == null ? "" : adjustmentDetail.getId());
                            String detailStatus = "APPROVED";
                            jObj.put("status", detailStatus);
                            jObj.put("supervisor", stockAdjustment.getCreator() != null ? stockAdjustment.getCreator().getFullName() : "");
                            jObj.put("locationname", grnLocationName);
                            jObj.put("locationid", grnLocationId);
                            jObj.put("batchname", adjustmentDetail.getBatchName());
                            if (StringUtil.isNullOrEmpty(adjustmentDetail.getBatchName())) {
                                jObj.put("batchname", "-");
                            }
                            jObj.put("quantity", adjustmentDetail.getQuantity());
                            jObj.put("actualquantity", adjustmentDetail.getQuantity());
                            jObj.put("serialname", adjustmentDetail.getFinalSerialNames());
                            jObj.put("isreusable", stockAdjustment.getProduct().getItemReusability());
                            jObj.put("leadtime", stockAdjustment.getProduct().getQALeadTimeInDays());
                            jObj.put("productname", stockAdjustment.getProduct().getName());
                            jObj.put("productdescription", stockAdjustment.getProduct().getDescription());
                            jObj.put("transactionno", stockAdjustment.getTransactionNo());
                            jObj.put("storename", grnStorename);
                            jObj.put("productcode", stockAdjustment.getProduct().getProductid());
                            jObj.put("isSerialForProduct", stockAdjustment.getProduct().isIsSerialForProduct());
                            jObj.put("isBatchForProduct", stockAdjustment.getProduct().isIsBatchForProduct());
                            jObj.put("moduletype", modulType);
                            jObj.put("remark", stockAdjustment.getRemark());
                            jObj.put("transactionmodule", Constants.Acc_StockAdjustment_modulename);
                            jObj.put("billid", stockAdjustment.getId());
                            jObj.put("modifiedon", stockAdjustment.getModifiedOn() != null ? df.format(stockAdjustment.getModifiedOn()) : "");
                            InspectionTemplate it = stockAdjustment.getProduct().getInspectionTemplate();
                            jObj.put("inspectionTemplate", it != null ? it.getId() : null);
                            int attachmentCount = saApprovalService.getAttachmentCount(stockAdjustment.getCompany(), adjustmentDetail.getId());
                            jObj.put("attachment", attachmentCount);
                            jArray.put(jObj);
                        }
                    }
                    /**
                     * Get all rejected inter store transfer request and create
                     * JSON with respect to inter store transfer details created
                     * for rejected IST.
                     */
                    Set<RejectedDODQCISTMapping> rejectedInterStoreTransferRequests = dodQCISTMapping.getRejectedDODQCISTMappings();
                    if (rejectedInterStoreTransferRequests != null && !rejectedInterStoreTransferRequests.isEmpty()) {
                        for (RejectedDODQCISTMapping rejectedInterStoreTransferRequest : rejectedInterStoreTransferRequests) {
                            InterStoreTransferRequest rejectedRequest = rejectedInterStoreTransferRequest.getRepairInterStoreTransferRequest();
                            for (ISTDetail outgoingISTDetail : rejectedRequest.getIstDetails()) {
                                JSONObject jObj = new JSONObject();
                                jObj.put("id", outgoingISTDetail.getId() == null ? "" : outgoingISTDetail.getId());
                                String detailStatus = "REJECTED";
                                jObj.put("status", detailStatus);
                                jObj.put("supervisor", rejectedRequest.getCreatedBy() != null ? rejectedRequest.getCreatedBy().getFullName() : "");
                                jObj.put("locationname", grnLocationName);
                                jObj.put("locationid", grnLocationId);
                                jObj.put("fromlocationname", outgoingISTDetail.getIssuedLocation() == null ? "" : outgoingISTDetail.getIssuedLocation().getName());
                                jObj.put("fromlocationid", outgoingISTDetail.getIssuedLocation() == null ? "" : outgoingISTDetail.getIssuedLocation().getId());
                                jObj.put("batchname", outgoingISTDetail.getBatchName());
                                if (StringUtil.isNullOrEmpty(outgoingISTDetail.getBatchName())) {
                                    jObj.put("batchname", "-");
                                }
                                jObj.put("quantity", outgoingISTDetail.getDeliveredQuantity());
                                jObj.put("actualquantity", outgoingISTDetail.getIssuedQuantity());
                                jObj.put("issuedserialnames", outgoingISTDetail.getIssuedSerialNames());
                                jObj.put("serialname", outgoingISTDetail.getIssuedSerialNames());
                                jObj.put("returnserialnames", outgoingISTDetail.getReturnSerialNames());
                                jObj.put("isreusable", outgoingISTDetail.getIstRequest().getProduct().getItemReusability());
                                jObj.put("leadtime", outgoingISTDetail.getIstRequest().getProduct().getQALeadTimeInDays());
                                jObj.put("productname", outgoingISTDetail.getIstRequest().getProduct().getName());
                                jObj.put("productdescription", outgoingISTDetail.getIstRequest().getProduct().getDescription());
                                jObj.put("transactionno", outgoingISTDetail.getIstRequest().getTransactionNo());
                                jObj.put("storename", grnStorename);
                                jObj.put("productcode", outgoingISTDetail.getIstRequest().getProduct().getProductid());
                                jObj.put("isSerialForProduct", outgoingISTDetail.getIstRequest().getProduct().isIsSerialForProduct());
                                jObj.put("isBatchForProduct", outgoingISTDetail.getIstRequest().getProduct().isIsBatchForProduct());
                                jObj.put("moduletype", modulType);
                                jObj.put("remark", rejectedRequest.getRemark());
                                jObj.put("transactionmodule", Constants.ACC_REPAIR_IST_MODULENAME);
                                jObj.put("billid", rejectedRequest.getId());
                                jObj.put("modifiedon", rejectedRequest.getModifiedOn() != null ? df.format(rejectedRequest.getModifiedOn()) : "");
                                InspectionTemplate it = outgoingISTDetail.getIstRequest().getProduct().getInspectionTemplate();
                                jObj.put("inspectionTemplate", it != null ? it.getId() : null);
                                int attachmentCount = saApprovalService.getAttachmentCount(outgoingISTDetail.getIstRequest().getCompany(), outgoingISTDetail.getId());
                                jObj.put("attachment", attachmentCount);
                                jArray.put(jObj);
                            }
                        }
                    }
                    /**
                     * Get all approved inter store transfer request which are
                     * sent to packing store and create JSON with respect to
                     * inter store transfer details created for approved IST.
                     */
                    Set<DeliveryDetailInterStoreLocationMapping> pickedMapping = dodQCISTMapping.getPickedMapping();
                    for (DeliveryDetailInterStoreLocationMapping deliveryDetailInterStoreLocationMapping : pickedMapping) {
                        InterStoreTransferRequest pickedISTRequest = deliveryDetailInterStoreLocationMapping.getInterStoreTransferRequest();
                        for (ISTDetail outgoingISTDetail : pickedISTRequest.getIstDetails()) {
                            JSONObject jObj = new JSONObject();
                            jObj.put("id", outgoingISTDetail.getId() == null ? "" : outgoingISTDetail.getId());
                            String detailStatus = "APPROVED";
                            jObj.put("status", detailStatus);
                            jObj.put("supervisor", pickedISTRequest.getCreatedBy() != null ? pickedISTRequest.getCreatedBy().getFullName() : "");
                            jObj.put("locationname", grnLocationName);
                            jObj.put("locationid", grnLocationId);
                            jObj.put("fromlocationname", outgoingISTDetail.getIssuedLocation() == null ? "" : outgoingISTDetail.getIssuedLocation().getName());
                            jObj.put("fromlocationid", outgoingISTDetail.getIssuedLocation() == null ? "" : outgoingISTDetail.getIssuedLocation().getId());
                            jObj.put("batchname", outgoingISTDetail.getBatchName());
                            if (StringUtil.isNullOrEmpty(outgoingISTDetail.getBatchName())) {
                                jObj.put("batchname", "-");
                            }
                            jObj.put("quantity", outgoingISTDetail.getDeliveredQuantity());
                            jObj.put("actualquantity", outgoingISTDetail.getIssuedQuantity());
                            jObj.put("issuedserialnames", outgoingISTDetail.getIssuedSerialNames());
                            jObj.put("serialname", outgoingISTDetail.getIssuedSerialNames());
                            jObj.put("returnserialnames", outgoingISTDetail.getReturnSerialNames());
                            jObj.put("isreusable", outgoingISTDetail.getIstRequest().getProduct().getItemReusability());
                            jObj.put("leadtime", outgoingISTDetail.getIstRequest().getProduct().getQALeadTimeInDays());
                            jObj.put("productname", outgoingISTDetail.getIstRequest().getProduct().getName());
                            jObj.put("productdescription", outgoingISTDetail.getIstRequest().getProduct().getDescription());
                            jObj.put("transactionno", outgoingISTDetail.getIstRequest().getTransactionNo());
                            jObj.put("storename", grnStorename);
                            jObj.put("productcode", outgoingISTDetail.getIstRequest().getProduct().getProductid());
                            jObj.put("isSerialForProduct", outgoingISTDetail.getIstRequest().getProduct().isIsSerialForProduct());
                            jObj.put("isBatchForProduct", outgoingISTDetail.getIstRequest().getProduct().isIsBatchForProduct());
                            jObj.put("moduletype", modulType);
                            jObj.put("remark", pickedISTRequest.getRemark());
                            jObj.put("transactionmodule", Constants.ACC_PACKED_IST_MODULENAME);
                            jObj.put("billid", pickedISTRequest.getId());
                            jObj.put("modifiedon", pickedISTRequest.getModifiedOn() != null ? df.format(pickedISTRequest.getModifiedOn()) : "");
                            InspectionTemplate it = outgoingISTDetail.getIstRequest().getProduct().getInspectionTemplate();
                            jObj.put("inspectionTemplate", it != null ? it.getId() : null);
                            int attachmentCount = saApprovalService.getAttachmentCount(outgoingISTDetail.getIstRequest().getCompany(), outgoingISTDetail.getId());
                            jObj.put("attachment", attachmentCount);
                            jArray.put(jObj);
                        }

                    }
                }
            }
        }
        return jArray;
    }
    private JSONArray getDeliveryOrderDataForStockRepair(JSONArray jArray, String saId, String modulType, String transactionNo) throws JSONException, ServiceException {
        KwlReturnObject jeresult = accountingHandlerDAO.getObject(RejectedDODQCISTMapping.class.getName(), saId);
        RejectedDODQCISTMapping rejectedDODQCISTMapping = (RejectedDODQCISTMapping) jeresult.getEntityList().get(0);
        if (rejectedDODQCISTMapping != null && rejectedDODQCISTMapping.getDodqcistmapping()!= null) {
            DODQCISTMapping dodqcistmapping = rejectedDODQCISTMapping.getDodqcistmapping();
            InterStoreTransferRequest grodist = dodqcistmapping.getQcInterStoreTransferRequest();
            if (!grodist.getIstDetails().isEmpty()) {
                Set<ISTDetail> grodistdetails = grodist.getIstDetails();
                ISTDetail grodistdetail = null;
                for (ISTDetail istdetail : grodistdetails) {
                    grodistdetail = istdetail;
                }
                List serialList = new ArrayList();
                if (grodist.getProduct() != null && grodist.getProduct().isIsSerialForProduct()) {
                    if (rejectedDODQCISTMapping.getPickedMappings() != null && !rejectedDODQCISTMapping.getPickedMappings().isEmpty()) {
                        Set<DeliveryDetailInterStoreLocationMapping> ddislm = rejectedDODQCISTMapping.getPickedMappings();
                        for (DeliveryDetailInterStoreLocationMapping deliveryDetailInterStoreLocationMapping : ddislm) {
                            InterStoreTransferRequest ist = deliveryDetailInterStoreLocationMapping.getInterStoreTransferRequest();
                            for (ISTDetail istdetail : ist.getIstDetails()) {
                                if (!StringUtil.isNullOrEmpty(istdetail.getDeliveredSerialNames())) {
                                    serialList.addAll(Arrays.asList(istdetail.getDeliveredSerialNames().split(",")));
                                }
                            }
                        }
                    }
                    if (rejectedDODQCISTMapping.getRejectedStockOuts() != null && !rejectedDODQCISTMapping.getRejectedStockOuts().isEmpty()) {
                        for (StockAdjustment adjustment : rejectedDODQCISTMapping.getRejectedStockOuts()) {
                            for (StockAdjustmentDetail adjustmentDetail : adjustment.getStockAdjustmentDetail()) {
                                serialList.addAll(Arrays.asList(adjustmentDetail.getFinalSerialNames().split(",")));
                            }
                        }
                    }
                    if (rejectedDODQCISTMapping.getApprovedStockOuts()!= null && !rejectedDODQCISTMapping.getApprovedStockOuts().isEmpty()) {
                        for (StockAdjustment adjustment : rejectedDODQCISTMapping.getApprovedStockOuts()) {
                            for (StockAdjustmentDetail adjustmentDetail : adjustment.getStockAdjustmentDetail()) {
                                serialList.addAll(Arrays.asList(adjustmentDetail.getFinalSerialNames().split(",")));
                            }
                        }
                    }
                }
                InterStoreTransferRequest istRequest = rejectedDODQCISTMapping.getRepairInterStoreTransferRequest();
                Set<ISTDetail> istdetails = istRequest.getIstDetails();
                for (ISTDetail ISD : istdetails) {
                    if (rejectedDODQCISTMapping.getQuantityDue() > 0.0) {
                        if (grodist.getProduct() != null && grodist.getProduct().isIsSerialForProduct() && serialList.contains(ISD.getIssuedSerialNames())) {
                            continue;
                        }
                        JSONObject jObj = new JSONObject();
                        jObj.put("qaapprovalid", saId);
                        jObj.put("id", ISD.getId() == null ? "" : ISD.getId());
                        jObj.put("moduletype", modulType);
                        String detailStatus = rejectedDODQCISTMapping.getQuantityDue() > 0.0 ? "REJECTED" : "APPROVED";
                        jObj.put("status", detailStatus);
                        jObj.put("locationname", (grodistdetail != null && grodistdetail.getIssuedLocation() != null) ? grodistdetail.getIssuedLocation().getName() : (ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getName()));
                        jObj.put("locationid", (grodistdetail != null && grodistdetail.getIssuedLocation() != null) ? grodistdetail.getIssuedLocation().getId() : (ISD.getIssuedLocation() == null ? "" : ISD.getIssuedLocation().getId()));
                        jObj.put("batchname", ISD.getBatchName());
                        if (StringUtil.isNullOrEmpty(ISD.getBatchName())) {
                            jObj.put("batchname", "-");
                        }
                        double qty = ISD.getDeliveredQuantity()- ISD.getApprovedQtyFromRepairStore()- ISD.getRejectedQtyFromRepairStore();
                        if (qty == 0.0) {
                            continue;
                        }
                        jObj.put("quantity", qty);
                        jObj.put("actualquantity", qty);
                        jObj.put("serialname", ISD.getIssuedSerialNames());
                        jObj.put("leadtime", ISD.getIstRequest().getProduct().getQALeadTimeInDays());
                        jObj.put("productname", ISD.getIstRequest().getProduct().getName());
                        jObj.put("productdescription", ISD.getIstRequest().getProduct().getDescription());
                        jObj.put("transactionno", transactionNo);
                        jObj.put("storename", grodist.getFromStore().getFullName());
                        jObj.put("productcode", ISD.getIstRequest().getProduct().getProductid());
                        jObj.put("isSerialForProduct", ISD.getIstRequest().getProduct().isIsSerialForProduct());
                        jObj.put("isBatchForProduct", ISD.getIstRequest().getProduct().isIsBatchForProduct());
                        jObj.put("moduletype", modulType);
                        jObj.put("remark", istRequest.getRemark());
                        jObj.put("transactiondate", istRequest.getModifiedOn() != null ? df.format(istRequest.getModifiedOn()) : "");
                        jArray.put(jObj);
                    }
                }
            }
        }
        return jArray;
    }
    /**
     * Save or Update Inspection Form
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView saveInspectionForm(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SA_Tx_Save_Inspection");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try{
            String recordId = StringUtil.isNullOrEmpty(request.getParameter("recordId")) ? "" : request.getParameter("recordId");
            String module = StringUtil.isNullOrEmpty(request.getParameter("module")) ? "" : request.getParameter("module");
            String inspectionDate = StringUtil.isNullOrEmpty(request.getParameter("inspectionDate")) ? "" : request.getParameter("inspectionDate");
            String modelName = StringUtil.isNullOrEmpty(request.getParameter("modelName")) ? "" : request.getParameter("modelName");
            String consignmentReturnNo = StringUtil.isNullOrEmpty(request.getParameter("consignmentReturnNo")) ? "" : request.getParameter("consignmentReturnNo");
            String department = StringUtil.isNullOrEmpty(request.getParameter("department")) ? "" : request.getParameter("department");
            String customerName = StringUtil.isNullOrEmpty(request.getParameter("customerName")) ?  "" : request.getParameter("customerName");
            String inspectionAreaDetails = StringUtil.isNullOrEmpty(request.getParameter("inspectionAreaDetails")) ? "[]" : request.getParameter("inspectionAreaDetails");
            
            JSONObject params = new JSONObject();
            params.put("inspectionDate", inspectionDate);
            params.put("modelName", modelName);
            params.put("consignmentReturnNo", consignmentReturnNo);
            params.put("department", department);
            params.put("customerName", customerName);
            
            SADetailApproval saDetail = null;
            StockAdjustmentDetail sa_dtlObj = null;
            ISTDetail istDetail = null;
            StockTransferDetailApproval stDetail = null;
            ConsignmentApprovalDetails consDetail =null;
            String inspectionFormId = "";
            
            KwlReturnObject sadResult = accountingHandlerDAO.getObject(SADetailApproval.class.getName(), recordId);    //Stock Adjustment
//            SADetailApproval sADApproval = (SADetailApproval) sadResult.getEntityList().get(0);
//            saDetail = sADApproval!=null?sADApproval.getStockAdjustmentDetail():null;
            saDetail = (SADetailApproval) sadResult.getEntityList().get(0);
            
            KwlReturnObject istResult = accountingHandlerDAO.getObject(ISTDetail.class.getName(), recordId);       //Inter Store transfer
            istDetail = (ISTDetail) istResult.getEntityList().get(0);
            
            KwlReturnObject stResult = accountingHandlerDAO.getObject(StockTransferDetailApproval.class.getName(), recordId);     //Stock Request
            stDetail = (StockTransferDetailApproval) stResult.getEntityList().get(0);
            
            KwlReturnObject consResult = accountingHandlerDAO.getObject(ConsignmentApprovalDetails.class.getName(), recordId);     //Consignment
            consDetail = (ConsignmentApprovalDetails) consResult.getEntityList().get(0);
        
            KwlReturnObject sa_dtlResult = accountingHandlerDAO.getObject(StockAdjustmentDetail.class.getName(), recordId);
            sa_dtlObj = (StockAdjustmentDetail) sa_dtlResult.getEntityList().get(0);
            
            if (saDetail != null) {
                if (saDetail.getInspectionForm() != null) {
                    inspectionFormId = saDetail.getInspectionForm().getId();
                }
            } else if (istDetail != null) {
                if (istDetail.getInspectionForm() != null) {
                    inspectionFormId = istDetail.getInspectionForm().getId();
                }
            } else if (stDetail != null) {
                if (stDetail.getInspectionForm() != null) {
                    inspectionFormId = stDetail.getInspectionForm().getId();
                }
            } else if (consDetail !=null) {
                if (consDetail.getInspectionForm() != null) {
                    inspectionFormId = consDetail.getInspectionForm().getId();
                }
            } else if (sa_dtlObj !=null) {
                if (sa_dtlObj.getInspectionForm() != null) {
                    inspectionFormId = sa_dtlObj.getInspectionForm().getId();
                }
            }
            
            params.put("inspectionFormId", inspectionFormId);
            KwlReturnObject inspectionFormResult = accCommonTablesDAO.saveOrUpdateInspectionForm(params);
            List list = inspectionFormResult.getEntityList();
            InspectionForm insForm = (InspectionForm) list.get(0);
            if(StringUtil.isNullOrEmpty(inspectionFormId)){
                if(saDetail != null){
                    saDetail.setInspectionForm(insForm);
                    inspectionFormId = insForm.getId();
                } else if(istDetail != null){
                    istDetail.setInspectionForm(insForm);
                    inspectionFormId = insForm.getId();
                }else if(stDetail != null){
                    stDetail.setInspectionForm(insForm);
                    inspectionFormId = insForm.getId();
                }else if(consDetail != null){
                    consDetail.setInspectionForm(insForm);
                    inspectionFormId = insForm.getId();
                }else if (sa_dtlObj !=null) {
                    sa_dtlObj.setInspectionForm(insForm); 
                    inspectionFormId = insForm.getId();
                }
            
                params.put("inspectionFormId", inspectionFormId);
            }
            
            params.put(Constants.detail, inspectionAreaDetails);
            
            accCommonTablesDAO.deleteInspectionFormDetails(inspectionFormId);
            JSONArray inspectionAreaJarr = new JSONArray(inspectionAreaDetails);
            
            for(int ind = 0; ind < inspectionAreaJarr.length(); ind++){
                JSONObject inspectionAreaObj = inspectionAreaJarr.optJSONObject(ind);
                
                HashMap<String, Object> inspectionFormDetailsMap = new HashMap<String, Object>();
                inspectionFormDetailsMap.put("inspectionFormId", inspectionFormId);
                inspectionFormDetailsMap.put("areaId", inspectionAreaObj.optString("areaId", ""));
                inspectionFormDetailsMap.put("areaName", inspectionAreaObj.optString("areaName", ""));
                inspectionFormDetailsMap.put("status", inspectionAreaObj.optString("status", ""));
                inspectionFormDetailsMap.put("faults", inspectionAreaObj.optString("faults", ""));
                inspectionFormDetailsMap.put("passingValue", inspectionAreaObj.optString("passingValue", ""));
                inspectionFormDetailsMap.put("actualValue", inspectionAreaObj.optString("actualValue", ""));
                                
                accCommonTablesDAO.saveInspectionFormDetails(inspectionFormDetailsMap);
            }
            issuccess = true;
            msg = "Inspection Form saved successfully.";
            txnManager.commit(status);
        } catch (Exception ex) {
            issuccess = false;
            msg = ex.getMessage();
            txnManager.rollback(status);
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
    /**
     * Get Inspection Form all details
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView getInspectionForm(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SA_Tx_Save_Inspection");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try{
            String recordId = StringUtil.isNullOrEmpty(request.getParameter("recordId")) ? "" : request.getParameter("recordId");
            String module = StringUtil.isNullOrEmpty(request.getParameter("module")) ? "" : request.getParameter("module");
            
            JSONObject params = new JSONObject();
                        
            SADetailApproval saDetail = null;
            StockAdjustmentDetail sa_dtlObj = null;
            ISTDetail istDetail = null;
            ISTDetail iistDetail = null;
            StockTransferDetailApproval stDetail = null;
            ConsignmentApprovalDetails consDetail =null;
            InspectionForm inspectionForm = null;
            String templateId = "";
            
            KwlReturnObject sadResult = accountingHandlerDAO.getObject(SADetailApproval.class.getName(), recordId);
//            SADetailApproval sADApproval = (SADetailApproval) sadResult.getEntityList().get(0);
//            saDetail = sADApproval!=null?sADApproval.getStockAdjustmentDetail():null;
            saDetail = (SADetailApproval) sadResult.getEntityList().get(0);
            
            if ("stockout".equals(module) && saDetail == null) {
                KwlReturnObject sa_dtlResult = accountingHandlerDAO.getObject(StockAdjustmentDetail.class.getName(), recordId);
                sa_dtlObj = (StockAdjustmentDetail) sa_dtlResult.getEntityList().get(0);
            }
            
            KwlReturnObject istResult = accountingHandlerDAO.getObject(ISTDetail.class.getName(), recordId);
            istDetail = (ISTDetail) istResult.getEntityList().get(0);
            
            KwlReturnObject stResult = accountingHandlerDAO.getObject(StockTransferDetailApproval.class.getName(), recordId);
            stDetail = (StockTransferDetailApproval) stResult.getEntityList().get(0);

            KwlReturnObject consResult = accountingHandlerDAO.getObject(ConsignmentApprovalDetails.class.getName(), recordId);
            consDetail = (ConsignmentApprovalDetails) consResult.getEntityList().get(0);

            if (saDetail != null) {
                if (saDetail.getInspectionForm() != null) {
                    inspectionForm = saDetail.getInspectionForm();
                    templateId = saDetail.getStockAdjustmentDetail().getStockAdjustment().getProduct().getInspectionTemplate() == null ? "" : saDetail.getStockAdjustmentDetail().getStockAdjustment().getProduct().getInspectionTemplate().getId();
                }
            } else if (istDetail != null) {
                if (istDetail.getInspectionForm() != null) {
                    inspectionForm = istDetail.getInspectionForm();
                    templateId = istDetail.getIstRequest().getProduct().getInspectionTemplate() == null ? "" : istDetail.getIstRequest().getProduct().getInspectionTemplate().getId();
                }
            } else if (stDetail !=null) {
                if (stDetail.getInspectionForm() != null) {
//                    String istDetailid = stDetail.getStockTransferDetailId();
//                    KwlReturnObject istResult1 = accountingHandlerDAO.getObject(ISTDetail.class.getName(), istDetailid);
//                    iistDetail = (ISTDetail) istResult1.getEntityList().get(0);
                    inspectionForm = stDetail.getInspectionForm();
//                    templateId = iistDetail.getIstRequest().getProduct().getInspectionTemplate() == null ? "" : istDetail.getIstRequest().getProduct().getInspectionTemplate().getId();
                }
            } else if (consDetail !=null) {
                if (consDetail.getInspectionForm() != null) {
                    inspectionForm = consDetail.getInspectionForm();
                    templateId = consDetail.getConsignment().getProduct().getInspectionTemplate() == null ? "" : consDetail.getConsignment().getProduct().getInspectionTemplate().getId() ;
                }
            } else if (sa_dtlObj !=null) {
                if (sa_dtlObj.getInspectionForm() != null) {
                    inspectionForm = sa_dtlObj.getInspectionForm();
                    templateId = sa_dtlObj.getStockAdjustment().getProduct().getInspectionTemplate() == null ? "" : sa_dtlObj.getStockAdjustment().getProduct().getInspectionTemplate().getId() ;
                }
            }
            
            JSONArray jArr = new JSONArray();
            if(inspectionForm != null){
                JSONObject formDataObj = new JSONObject();
                formDataObj.put("inspectionDate", inspectionForm.getInspectionDate().toString());
                formDataObj.put("modelName", inspectionForm.getModelName());
                formDataObj.put("customerName", inspectionForm.getCustomerName());
                formDataObj.put("department", inspectionForm.getDepartment());
                formDataObj.put("consignmentReturnNo", inspectionForm.getConsignmentReturnNo());

                JSONArray detailsJArr = new JSONArray();

                Set<InspectionFormDetails> insFormDetailSet = inspectionForm.getRows();

                for(InspectionFormDetails insFormDetail : insFormDetailSet){
                    JSONObject insFormDetailJobj = new JSONObject();
                    insFormDetailJobj.put("templateId", templateId);
                    insFormDetailJobj.put("areaId", insFormDetail.getInspectionArea() == null ? "" : insFormDetail.getInspectionArea().getId());
                    insFormDetailJobj.put("areaName", insFormDetail.getInspectionAreaValue());
                    insFormDetailJobj.put("status", insFormDetail.getInspectionStatus());
                    insFormDetailJobj.put("faults", insFormDetail.getFaults());
                    insFormDetailJobj.put("passingValue", insFormDetail.getPassingValue() == null ? "":insFormDetail.getPassingValue());
                    insFormDetailJobj.put("actualValue", insFormDetail.getActualValue() == null ? "":insFormDetail.getActualValue());

                    detailsJArr.put(insFormDetailJobj);
                }

                formDataObj.put(Constants.detail, detailsJArr);
                jArr.put(formDataObj);
            }
            jobj.put("data", jArr);
            
            issuccess = true;
            msg = "Success";
            txnManager.commit(status);
        } catch (Exception ex) {
            issuccess = false;
            msg = ex.getMessage();
            txnManager.rollback(status);
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
}
