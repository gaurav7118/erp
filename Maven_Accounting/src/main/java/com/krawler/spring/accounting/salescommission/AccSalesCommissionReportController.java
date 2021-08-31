/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.salescommission;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.masteritems.accMasterItemsController;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

public class AccSalesCommissionReportController extends MultiActionController implements MessageSourceAware {

    private MessageSource messageSource;
    private HibernateTransactionManager txnManager;
    private AccSalesCommissionService accSalesCommissionService;
    private auditTrailDAO auditTrailObj;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setAccSalesCommissionService(AccSalesCommissionService accSalesCommissionService) {
        this.accSalesCommissionService = accSalesCommissionService;
    }
    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }
    /**
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView saveSalesCommissionSchemaMaster(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        boolean isSuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String auditAction = "", auditActionID = "", schemaMaster = "";
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(SalesCommissionSchemaMaster.SCHEMAMASTERNAME, request.getParameter("schemaMaster"));
            requestParams.put(SalesCommissionSchemaMaster.SCHEMAMASTERID, request.getParameter(SalesCommissionSchemaMaster.SCHEMAMASTERID));
            requestParams.put(SalesCommissionSchemaMaster.RULESDETAIL, request.getParameter("rulesDetail"));
            jObj = accSalesCommissionService.saveSalesCommissionSchemaMaster(requestParams);
            txnManager.commit(status);
            msg = messageSource.getMessage("acc.salesCommissionSchema.save.success", null, RequestContextUtils.getLocale(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter(SalesCommissionSchemaMaster.SCHEMAMASTERID))) {
                auditAction = "edited";
                auditActionID = AuditAction.SALESCOMMISIONSCHEMA_EDITED;
            } else {
                auditAction = "added";
                auditActionID = AuditAction.SALESCOMMISIONSCHEMA_ADDED;
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("schemaMaster"))) {
                schemaMaster = (String) request.getParameter("schemaMaster");
            }
            auditTrailObj.insertAuditLog(auditActionID, "User " + sessionHandlerImpl.getUserFullName(request) + " has been " + auditAction + " Sales commission schema :<b>" + schemaMaster + "</b>", request, "0");
            jObj.put("success", true);
            jObj.put("msg", msg);
        } catch (SessionExpiredException | ServiceException | JSONException ex) {
            try {
                jObj.put("success", isSuccess);
                jObj.put("msg", messageSource.getMessage("acc.salesCommissionSchema.save.failed", null, RequestContextUtils.getLocale(request)));
            } catch (JSONException ex1) {
                Logger.getLogger(AccSalesCommissionReportController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            txnManager.rollback(status);
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }

    /**
     *
     * @param request
     * @param response
     * @return SalesCommissionSchemaMasters
     */
    public ModelAndView getSalesCommissionSchemaMasters(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        boolean isSuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(SalesCommissionSchemaMaster.SCHEMAMASTERID, request.getParameter(SalesCommissionSchemaMaster.SCHEMAMASTERID));
            jObj = accSalesCommissionService.getSalesCommissionSchemaMasters(requestParams);
            msg = messageSource.getMessage("acc.salesCommissionSchema.fetch.success", null, RequestContextUtils.getLocale(request));
            jObj.put("success", true);
            jObj.put("msg", msg);
        } catch (Exception ex) {
            try {
                jObj.put("success", isSuccess);
                jObj.put("msg", messageSource.getMessage("acc.salesCommissionSchema.fetch.error", null, RequestContextUtils.getLocale(request)));
            } catch (JSONException ex1) {
                Logger.getLogger(AccSalesCommissionReportController.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }

    /**
     *
     * @param request
     * @param response
     * @return SalesCommissionRules
     */
    public ModelAndView getSalesCommissionRules(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        boolean isSuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(SalesCommissionSchemaMaster.SCHEMAMASTERID, request.getParameter(SalesCommissionSchemaMaster.SCHEMAMASTERID));
            jObj = accSalesCommissionService.getSalesCommissionRules(requestParams);

            msg = messageSource.getMessage("acc.salesCommissionSchema.fetch.success", null, RequestContextUtils.getLocale(request));
            jObj.put("success", true);
            jObj.put("msg", msg);
        } catch (Exception ex) {
            try {
                jObj.put("success", isSuccess);
                jObj.put("msg", messageSource.getMessage("acc.salesCommissionSchema.fetch.error", null, RequestContextUtils.getLocale(request)));
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex1) {
                Logger.getLogger(AccSalesCommissionReportController.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }

    /**
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView deleteSalesCommissionRules(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        boolean isSuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(SalesCommissionRules.RULEID, request.getParameter(SalesCommissionRules.RULEID));
            jObj = accSalesCommissionService.deleteSalesCommissionRules(requestParams);
            msg = messageSource.getMessage("acc.salesCommissionRules.delete.success", null, RequestContextUtils.getLocale(request));
            jObj.put("msg", msg);
        } catch (Exception ex) {
            try {
                jObj.put("success", isSuccess);
                jObj.put("msg", messageSource.getMessage("acc.salesCommissionRules.delete.error", null, RequestContextUtils.getLocale(request)));
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex1) {
                Logger.getLogger(AccSalesCommissionReportController.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }
    
    /**
     *
     * @param request
     * @param response
     * @return
     */
    public ModelAndView saveSalesCommissionMapping(HttpServletRequest request,HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        boolean isSuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            requestParams.put("masterItem", request.getParameter("masterItem"));
            requestParams.put(SalesCommissionSchemaMaster.SCHEMAMASTERID, request.getParameter("salesCommisionSchema"));
            jObj = accSalesCommissionService.saveSalesCommissionMapping(requestParams);
            jObj.put("msg", messageSource.getMessage("acc.salesCommission.save.schemaMapping.success", null, RequestContextUtils.getLocale(request)));
        } catch (Exception ex) {
            try {
                jObj.put("success", isSuccess);
                jObj.put("msg", messageSource.getMessage("acc.salesCommission.save.schemaMapping.failed", null, RequestContextUtils.getLocale(request)));
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex1) {
                Logger.getLogger(AccSalesCommissionReportController.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }
    
    public ModelAndView deleteSalesCommissionSchema(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        boolean isSuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(SalesCommissionSchemaMaster.SCHEMAMASTERID, request.getParameter(SalesCommissionSchemaMaster.SCHEMAMASTERID));
            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            String schemaMaster  = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter(SalesCommissionSchemaMaster.SCHEMAMASTERNAME))) {
                schemaMaster = (String) request.getParameter(SalesCommissionSchemaMaster.SCHEMAMASTERNAME);
            }
            jObj = accSalesCommissionService.deleteSalesCommissionSchema(requestParams);
            
            jObj.put("msg", messageSource.getMessage("acc.salesCommissionRules.schema.delete.success", null, RequestContextUtils.getLocale(request)));
            auditTrailObj.insertAuditLog(AuditAction.SALESCOMMISIONSCHEMA_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has been deleted Sales commission schema <b>"+schemaMaster+"</b>", request, "0");
            txnManager.commit(status);
            
        } catch (Exception ex) {
            try {
                jObj.put("success", isSuccess);
                jObj.put("msg", messageSource.getMessage("acc.salesCommissionRules.schema.usedIntransactionAlert", null, RequestContextUtils.getLocale(request)));
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
                txnManager.rollback(status);
            } catch (JSONException ex1) {
                Logger.getLogger(AccSalesCommissionReportController.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }
}
