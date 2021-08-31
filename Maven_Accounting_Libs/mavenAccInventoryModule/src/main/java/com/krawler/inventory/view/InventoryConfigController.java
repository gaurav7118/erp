/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.view;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.BatchType;
import com.krawler.common.util.InventoryCheck;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.model.configuration.InventoryConfig;
import com.krawler.inventory.model.configuration.InventoryConfigService;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author Vipin Gupta
 */
public class InventoryConfigController extends MultiActionController {

    private static final Logger lgr = Logger.getLogger(InventoryConfigController.class.getName());
    private HibernateTransactionManager txnManager;
    private String successView;
    private InventoryConfigService configService;
    private AccountingHandlerDAO accountingHandlerDAO;
    private auditTrailDAO auditTrailObj;
    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setConfigService(InventoryConfigService configService) {
        this.configService = configService;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }
    
    public ModelAndView addOrUpdateConfig(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("AOUC_Tx_Save");
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

            String inventoryCheck = request.getParameter("checkInv");
            String stockBatchType = request.getParameter("iluTypeCode");
            String enableStockRequestApprovalFlow = request.getParameter("svenabledcheckbox");
            String enableStockAdjustmentApprovalFlow = request.getParameter("stockoutApproval");
            String enableStockOutApprovalFlow = request.getParameter("stockoutApproval");
            String enableInterStoreApprovalFlow = request.getParameter("interstoreApproval");
            String enableStockRequestReturnApprovalFlow = request.getParameter("stockrequestApproval");
           
            
            InventoryConfig config = configService.getConfigByCompany(company);
            
            boolean oldEnableStockOutApprovalFlow = config.isEnableStockoutApprovalFlow();
            boolean oldenableInterStoreApprovalFlow = config.isEnableISTReturnApprovalFlow();
            boolean oldEnableStockRequestReturnApprovalFlow = config.isEnableSRReturnApprovalFlow();

            if (!StringUtil.isNullOrEmpty(inventoryCheck)) {
                InventoryCheck invCheck = InventoryCheck.valueOf(inventoryCheck);
                config.setNegativeInventoryCheckType(invCheck);
            }
            if (!StringUtil.isNullOrEmpty(stockBatchType)) {
                BatchType batchType = BatchType.valueOf(stockBatchType);
                config.setStockBatchType(batchType);
            }
            boolean stockReqApprovalFlow = false;
            if ("on".equalsIgnoreCase(enableStockRequestApprovalFlow)) {
                stockReqApprovalFlow = true;
            }
            config.setEnableStockRequestApprovalFlow(stockReqApprovalFlow);
            boolean stockAdjApprovalFlow = false;
            if ("on".equalsIgnoreCase(enableStockAdjustmentApprovalFlow)) {
                stockAdjApprovalFlow = true;
            }
            config.setEnableStockAdjustmentApprovalFlow(stockAdjApprovalFlow);

            boolean stockOutApprovalFlow = false;
            if ("on".equalsIgnoreCase(enableStockOutApprovalFlow)) {
                stockOutApprovalFlow = true;
            }
            config.setEnableStockoutApprovalFlow(stockOutApprovalFlow);
            if(config.isEnableStockoutApprovalFlow()!=oldEnableStockOutApprovalFlow){
              auditMessage = "User " + user.getFullName() +" "+(config.isEnableStockoutApprovalFlow()?"enabled":"disabled")+ " the QA Inspection Flow for Stock Adjustment module.";
              auditTrailObj.insertAuditLog(AuditAction.INVENTORY_CONFIG, auditMessage, request, "0"); 
            }

            boolean interStoreApprovalFlow = false;
            if ("on".equalsIgnoreCase(enableInterStoreApprovalFlow)) {
                interStoreApprovalFlow = true;
            }
            config.setEnableISTReturnApprovalFlow(interStoreApprovalFlow);
            if(config.isEnableISTReturnApprovalFlow()!=oldenableInterStoreApprovalFlow){
              auditMessage = "User " + user.getFullName() +" "+(config.isEnableISTReturnApprovalFlow()?"enabled":"disabled")+ " the QA Inspection Flow for Inter Store Stock Transfer module.";
              auditTrailObj.insertAuditLog(AuditAction.INVENTORY_CONFIG, auditMessage, request, "0"); 
            }
            boolean stockRequestReturnApprovalFlow = false;
            if ("on".equalsIgnoreCase(enableStockRequestReturnApprovalFlow)) {
                stockRequestReturnApprovalFlow = true;
            }
            config.setEnableSRReturnApprovalFlow(stockRequestReturnApprovalFlow);
            if(config.isEnableSRReturnApprovalFlow()!=oldEnableStockRequestReturnApprovalFlow){
              auditMessage = "User " + user.getFullName() +" "+(config.isEnableSRReturnApprovalFlow()?"enabled":"disabled")+ " the QA Inspection Flow for Stock Request module.";
              auditTrailObj.insertAuditLog(AuditAction.INVENTORY_CONFIG, auditMessage, request, "0"); 
            }
            configService.addConfig(user, config);

            issuccess = true;
            msg = "Configuration has been saved successfully";
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

    public ModelAndView getConfig(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONObject jObject = new JSONObject();
             try {
            String companyId = sessionHandlerImpl.getCompanyid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            InventoryConfig config = configService.getConfigByCompany(company);

            jObject.put("id", config.getId());
            jObject.put("inventoryCheck", config.getNegativeInventoryCheckType().ordinal());
            jObject.put("stockBatchType", config.getStockBatchType().ordinal());
            jObject.put("enableStockRequestApprovalFlow", config.isEnableStockRequestApprovalFlow());
            jObject.put("enableStockAdjustmentApprovalFlow", config.isEnableStockAdjustmentApprovalFlow());
            jObject.put("enableStockOutApprovalFlow", config.isEnableStockoutApprovalFlow());
            jObject.put("enableInterStoreApprovalFlow", config.isEnableISTReturnApprovalFlow());
            jObject.put("enableStockRequestReturnApprovalFlow", config.isEnableSRReturnApprovalFlow());

            issuccess = true;
            msg = "Configuration has been fetched successfully";
     
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
                jobj.put("data", jObject);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getBatchTypeList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        try {

            for (BatchType bt : BatchType.values()) {
                JSONObject jObject = new JSONObject();
                jObject.put("id", bt.ordinal());
                jObject.put("name", bt.toString());
                jArray.put(jObject);
            }
            issuccess = true;
            msg = "Batch Types has been fetched successfully";
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                jobj.put("count", jArray.length());
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getInventoryCheckTypeList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArray = new JSONArray();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("AOUC_Tx_Save");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {

            for (InventoryCheck ic : InventoryCheck.values()) {
                JSONObject jObject = new JSONObject();
                jObject.put("id", ic.ordinal());
                jObject.put("name", ic.toString());
                jArray.put(jObject);
            }
            issuccess = true;
            msg = "Inventory Check Type has been fetched successfully";
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
                jobj.put("count", jArray.length());
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }
}
