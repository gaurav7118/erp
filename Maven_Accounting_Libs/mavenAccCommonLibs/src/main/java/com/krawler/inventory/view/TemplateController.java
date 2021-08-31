/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.view;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.model.inspection.InspectionArea;
import com.krawler.inventory.model.inspection.InspectionTemplate;
import com.krawler.inventory.model.inspection.TemplateException;
import com.krawler.inventory.model.inspection.TemplateService;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
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
public class TemplateController extends MultiActionController {

    private static final Logger lgr = Logger.getLogger(TemplateController.class.getName());
    private HibernateTransactionManager txnManager;
    private String successView;
    private TemplateService templateService;
    private AccountingHandlerDAO accountingHandlerDAO;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
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

    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    
    public ModelAndView addUpdateInspectionTemplate(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("AOUC_Tx_Save");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {

            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String templateId = request.getParameter("templateId");
            String templateName = request.getParameter("templateName");
            String templateDescription = request.getParameter("templateDescription");

            InspectionTemplate iTemplate;
            if (!StringUtil.isNullOrEmpty(templateId)) {
                iTemplate = templateService.getInspectionTemplate(templateId);

                String oldName = iTemplate.getName() != null ? iTemplate.getName() : "";
                String newName = templateName != null ? templateName : "";
                if (!newName.equals(oldName)) {
                    auditMessage += " Name : from (" + oldName + " to " + newName + ") ";
                }
                String oldDescription = iTemplate.getDescription() != null ? iTemplate.getDescription() : "";
                String newDescription = templateDescription != null ? templateDescription : "";
                if (!newDescription.equals(oldDescription)) {
                    if (!StringUtil.isNullOrEmpty(auditMessage)) {
                        auditMessage += ", ";
                    }
                    auditMessage += " Description : from (" + oldDescription + " to " + newDescription + ") ";
                }

                iTemplate.setName(templateName);
                iTemplate.setDescription(templateDescription);
                templateService.updateTemplate(iTemplate);

                if (!StringUtil.isNullOrEmpty(auditMessage)) {
                    auditMessage = "updated Inspection Template : " + oldName + " - " + auditMessage;
                }
            } else {
                iTemplate = new InspectionTemplate(templateName, user.getCompany());
                iTemplate.setDescription(templateDescription);
                templateService.addTemplate(iTemplate);

                auditMessage = "created Inspection Template : " + templateName;
            }

            issuccess = true;
            msg = messageSource.getMessage("acc.field.Templatesavedsuccessfully", null, RequestContextUtils.getLocale(request));
                    
            if (!StringUtil.isNullOrEmpty(auditMessage)) {
                auditMessage = "User " + user.getFullName() + " has " + auditMessage;
                auditTrailObj.insertAuditLog(AuditAction.INSPECTION_TEMPLATE, auditMessage, request, iTemplate.getId());
            }


            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (TemplateException ex) {
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

    public ModelAndView addUpdateInspectionArea(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("AOUC_Tx_Save");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {

            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String templateId = request.getParameter("templateId");
            String areaId = request.getParameter("areaId");
            String areaName = request.getParameter("areaName");
            String faults = request.getParameter("faults");
            String passingValue = request.getParameter("passingValue");

            InspectionTemplate iTemplate = templateService.getInspectionTemplate(templateId);

            InspectionArea iArea;
            if (!StringUtil.isNullOrEmpty(areaId)) {
                iArea = templateService.getInspectionArea(areaId);

                String oldName = iArea.getName() != null ? iArea.getName() : "";
                String newName = areaName != null ? areaName : "";
                if (!newName.equals(oldName)) {
                    auditMessage += " Area Name : from (" + oldName + " to " + newName + ") ";
                }
                String oldFaults = iArea.getFaults() != null ? iArea.getFaults() : "";
                String newFaults = faults != null ? faults : "";
                if (!newFaults.equals(oldFaults)) {
                    if (!StringUtil.isNullOrEmpty(auditMessage)) {
                        auditMessage += ", ";
                    }
                    auditMessage += " Faults : from (" + oldFaults + " to " + newFaults + ") ";
                }
                /**
                 * audit entries for passing value changes
                 */
                String oldPassingValue = iArea.getPassingValue()!= null ? iArea.getPassingValue(): "";
                String newPassingValue = passingValue != null ? passingValue : "";
                if (!newPassingValue.equals(oldPassingValue)) {
                    if (!StringUtil.isNullOrEmpty(auditMessage)) {
                        auditMessage += ", ";
                    }
                    auditMessage += " Passing Value : from (" + oldPassingValue + " to " + newPassingValue + ") ";
                }

                iArea.setName(areaName);
                iArea.setFaults(faults);
                iArea.setPassingValue(passingValue);
                templateService.updateInspectionArea(iArea);

                if (!StringUtil.isNullOrEmpty(auditMessage)) {
                    auditMessage = "updated Inspection Area : " + oldName + " - " + auditMessage + " for Inspection Template : " + iTemplate.getName();
                }

            } else {
                iArea = new InspectionArea(iTemplate, areaName, faults, passingValue);
                templateService.addInspectionArea(iArea);

                auditMessage = "created Inspection Area : " + areaName + " with faults: ("+faults+") and passing value: ("+passingValue+") for Inspection Template : " + iTemplate.getName();
            }

            issuccess = true;
            msg = messageSource.getMessage("acc.inspection.area.saved.successfully", null, RequestContextUtils.getLocale(request));
            
            if (!StringUtil.isNullOrEmpty(auditMessage)) {
                auditMessage = "User " + user.getFullName() + " has " + auditMessage;
                auditTrailObj.insertAuditLog(AuditAction.INSPECTION_TEMPLATE, auditMessage, request, iArea.getId());
            }

            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (TemplateException ex) {
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

    public ModelAndView deleteInspectionTemplate(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("AOUC_Tx_Save");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {

            String userId = sessionHandlerImpl.getUserid(request);
            
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String templateId = request.getParameter("templateId");

            InspectionTemplate iTemplate = templateService.getInspectionTemplate(templateId);
            String templateName = iTemplate.getName();

            templateService.deleteInspectionTemplate(iTemplate);

            issuccess = true;
            msg = messageSource.getMessage("acc.field.Templatedeletedsuccessfully", null, RequestContextUtils.getLocale(request));

            auditMessage = "User " + user.getFullName() + " has deleted Inspection Template : " + templateName;
            auditTrailObj.insertAuditLog(AuditAction.INSPECTION_TEMPLATE, auditMessage, request, templateId);

            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (TemplateException ex) {
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

    public ModelAndView deleteInspectionArea(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("AOUC_Tx_Save");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {

            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String areaId = request.getParameter("areaId");

            InspectionArea iArea = templateService.getInspectionArea(areaId);
            String areaName = iArea.getName();
            String templateName = iArea.getInspectionTemplate().getName();
            
            templateService.deleteInspectionArea(iArea);

            issuccess = true;
            msg = messageSource.getMessage("acc.inspection.area.deleted.successfully", null, RequestContextUtils.getLocale(request));
            
            auditMessage = "User " + user.getFullName() + " has deleted Inspection Area: " + areaName+ " for Inspection Template: "+templateName;
            auditTrailObj.insertAuditLog(AuditAction.INSPECTION_TEMPLATE, auditMessage, request, areaId);
            
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (TemplateException ex) {
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

    public ModelAndView getInspectionTemplateList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArr = new JSONArray();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("AOUC_Tx_Save");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        Paging paging = null;
        try {
            String searchString = request.getParameter("ss");
            String start = "", limit = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("start")) && !StringUtil.isNullOrEmpty(request.getParameter("limit"))) {
                start = request.getParameter("start");
                limit = request.getParameter("limit");
                paging = new Paging(start, limit);
            }    
            String companyId = sessionHandlerImpl.getCompanyid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            List<InspectionTemplate> iTemplateList = templateService.getInspectionTemplateList(company, searchString, paging);
            for (InspectionTemplate it : iTemplateList) {
                JSONObject jObject = new JSONObject();
                jObject.put("templateId", it.getId());
                jObject.put("templateName", it.getName());
                jObject.put("templateDescription", it.getDescription());
                jArr.put(jObject);
            }
            issuccess = true;
                msg = "operation successfully";
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (TemplateException ex) {
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
                jobj.put("data", jArr);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArr.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getInspectionAreaList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jArr = new JSONArray();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("AOUC_Tx_Save");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        Paging paging = null; 
        try {
            String searchString = request.getParameter("ss");
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);

            String companyId = sessionHandlerImpl.getCompanyid(request);

            String templateId = request.getParameter("templateId");

            InspectionTemplate inspectionTemplate = templateService.getInspectionTemplate(templateId);
            if (inspectionTemplate != null) {
                List<InspectionArea> iAreaList = templateService.getInspectionAreaList(inspectionTemplate, searchString, paging);
                for (InspectionArea ia : iAreaList) {
                    JSONObject jObject = new JSONObject();
                    jObject.put("areaId", ia.getId());
                    jObject.put("areaName", ia.getName());
                    jObject.put("faults", ia.getFaults());
                    jObject.put("passingValue", ia.getPassingValue());
                    jObject.put("templateId", templateId);
                    jArr.put(jObject);
                }
            }
            issuccess = true;
            msg = "operation successfully";
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (TemplateException ex) {
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
                jobj.put("data", jArr);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArr.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }
}
