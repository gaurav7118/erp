/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.spring.mrp.machinemanagement;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.mrp.WorkOrder.AccWorkOrderController;
import com.krawler.spring.mrp.WorkOrder.WorkOrder;
import com.krawler.spring.mrp.labormanagement.AccLabourControllerCMN;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.NoSuchMessageException;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accMachineManagementController extends MultiActionController implements MessageSourceAware {
    
    private MessageSource messageSource;
    private HibernateTransactionManager txnManager;
    private String successView;
    private AccMachineManagementServiceDAO accMachineManagementServiceDAOObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private MachineManagementHandler machineManagementHandler;
    private auditTrailDAO auditTrailDaoObj;

    public void setAuditTrailDaoObj(auditTrailDAO auditTrailDaoObj) {
        this.auditTrailDaoObj = auditTrailDaoObj;
    }
    
    
    @Override
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }
    public void setMachineManagementHandler(MachineManagementHandler machineManagementHandler) {
        this.machineManagementHandler = machineManagementHandler;
    }
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

  
    public AccMachineManagementServiceDAO getAccMachineManagementServiceDAOObj() {
        return accMachineManagementServiceDAOObj;
    }

    public void setAccMachineManagementServiceDAOObj(AccMachineManagementServiceDAO accMachineManagementServiceDAOObj) {
        this.accMachineManagementServiceDAOObj = accMachineManagementServiceDAOObj;
    }
    
     public accCompanyPreferencesDAO getAccCompanyPreferencesObj() {
        return accCompanyPreferencesObj;
    }

    public void setAccCompanyPreferencesObj(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    /**
     * Description: This method is used Store Machine Master Details
     * @param request
     * @param response
     * @return 
     */
    
    public ModelAndView saveMachineMaster(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Machine_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        String msg = messageSource.getMessage("acc.machine.savemsg", null, RequestContextUtils.getLocale(request));
        boolean isEdit=false;
        try {
            status = txnManager.getTransaction(def);
            /*Get Machine Master Related Parameters*/
            String isEditStr=request.getParameter("isEdit");
            if(!StringUtil.isNullOrEmpty(isEditStr)){
                isEdit=Boolean.parseBoolean(isEditStr);
            } 
             Map<String, Object> requestParams = getMachineDetailParameters(request);
             
             /*Save Machine Details*/
             
             jobj=accMachineManagementServiceDAOObj.saveMachineMaster(requestParams);
             txnManager.commit(status);
             Machine machineObj= null;
            if (jobj.has("machine") && (jobj.opt("machine")) != null) {
                String companyid = sessionHandlerImpl.getCompanyid(request);
                machineObj=(Machine) jobj.opt("machine");
                Map<String, Object> syncParams = new HashMap<>();
                syncParams.put("companyid", companyid);
                syncParams.put("isAutoSync", true);
                syncParams.put("machine", machineObj);
                jobj = accMachineManagementServiceDAOObj.syncMachineDataToPM(syncParams);
                if (jobj.optJSONArray("ids") == null) {
                    msg = msg + messageSource.getMessage("acc.mrp.resource.notsync", null, RequestContextUtils.getLocale(request));
                }
            }
            if (machineObj != null) {
                String actionName = isEdit ?  "updated" : "created";
                String action = isEdit ?  AuditAction.MACHINE_MASTER_UPDATED : AuditAction.MACHINE_MASTER_CREATED;
                String auditMsg = " User " + sessionHandlerImpl.getUserFullName(request) + " has " + actionName + " machine  <b>" + machineObj.getMachineName() + "</b>";
                auditTrailDaoObj.insertAuditLog(action, auditMsg, request, machineObj.getID());
            }
             issuccess = true;
        } catch (Exception ex) {
             if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
    
    
    /**
     * Description: This method is used to get Machine Master Details
     * @param request
     * @param response
     * @return JSONObject
     */
    
    public ModelAndView getMachineMasterDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjTemp = new JSONObject();
        JSONObject returnObject = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            
            /*Get Machine Master Related Parameters*/
             Map<String, Object> requestParams = getMachineMasterCommonParameters(request);
             
             /*Get Grid configuration Meta Data and Column/Field Information */
             
             jobj=machineManagementHandler.getMachineMasterRegistryGridInfo(requestParams);
             /*Get Machine Details*/
             
             jobjTemp=accMachineManagementServiceDAOObj.getMachineMasterDetails(requestParams);
             JSONObject dataObj = jobjTemp.getJSONObject("data");
             dataObj.put("columns", jobj.getJSONArray("columns"));
             dataObj.put("success", true);
             dataObj.put("metaData", jobj.getJSONObject("metadata"));
             returnObject.put("data", dataObj);
             returnObject.put("valid", true);
             issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                returnObject.put("success", issuccess);
                returnObject.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, returnObject.toString());
    }
    
    public ModelAndView getMachineList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            HashMap<String, Object> requestParams = new HashMap();
            requestParams.put("request", request);
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("ss", request.getParameter("ss"));
            accMachineManagementServiceDAOObj.getColumnModelForMachineList(requestParams, jobj);
            success = true;
        } catch (ServiceException | SessionExpiredException ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    public ModelAndView saveMachineCost(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ParseException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            /*
             Save Machine's cost
             */
            Map<String, Object> requestParams = getMachineCostMap(request);
            JSONObject jSONObject = accMachineManagementServiceDAOObj.saveMachineCost(requestParams);
            msg = jSONObject.optString("msg");
            txnManager.commit(status);
            issuccess = true;
        } catch (SessionExpiredException | ParseException | ServiceException | TransactionException ex) {
            if (status != null) {
                txnManager.rollback(status);
                msg = "" + ex.getMessage();
            }
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public Map<String, Object> getMachineCostMap(HttpServletRequest request) throws SessionExpiredException, ParseException {
        Map<String, Object> requestParams = new HashMap();
        Date effectivedate = !StringUtil.isNullOrEmpty(request.getParameter("effectivedate")) ? authHandler.getDateOnlyFormat().parse(request.getParameter("effectivedate")) : null;
        requestParams.put("labourId", request.getParameter("labourId"));
        requestParams.put("effectivedate", effectivedate);
        if (!StringUtil.isNullOrEmpty(request.getParameter("resourceCostId"))) {
            requestParams.put("resourceCostId", request.getParameter("resourceCostId"));
        }
        requestParams.put("resourcecost", request.getParameter("resourcecost"));
        requestParams.put("company", sessionHandlerImpl.getCompanyid(request));
        requestParams.put("request", request);
        return requestParams;
    }
    
    public ModelAndView deleteMachineCost(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JEC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            /*
             Delete machine cost
             */
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("request", request);
            
            /*
             Following parameters used for updating machine cost
            */
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("cdomain", sessionHandlerImpl.getCompanySessionObj(request).getCdomain());
            requestParams.put("userId", sessionHandlerImpl.getUserid(request));
            /*
             Above parameters used for updating machine cost
            */
            accMachineManagementServiceDAOObj.deleteMachineCost(requestParams);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.mrp.labour.ResourceCost.deletecost", null, RequestContextUtils.getLocale(request));
        } catch (SessionExpiredException | ServiceException | TransactionException | NoSuchMessageException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, "accMachineManagementController.deleteMachineCost", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, "accMachineManagementController.deleteMachineCost", ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getMachineCostList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            Map<String, Object> requestParams = new HashMap();
            requestParams.put("request", request);
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            requestParams.put(Constants.userdf, userdf);
            requestParams.put("LabourId", request.getParameter("LabourId"));
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            /*
             Create column model for grid
             */
            accMachineManagementServiceDAOObj.getMachineCostList(requestParams, jobj);
            success = true;
        } catch (SessionExpiredException | ServiceException ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    public ModelAndView syncMachineCostToPM(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            /*
             Sync machine cost to PM
             */
            Map<String, Object> requestParams = new HashMap();
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("cdomain", sessionHandlerImpl.getCompanySessionObj(request).getCdomain());
            requestParams.put("userId", sessionHandlerImpl.getUserid(request));
            String labourId = request.getParameter("LabourId");
            if (!StringUtil.isNullOrEmpty(labourId)) {
                requestParams.put("labourId", labourId);
            }
            JSONObject jSONObject = accMachineManagementServiceDAOObj.syncMachineCost(requestParams);
            if (jSONObject.has(Constants.RES_success)) {
                if (jSONObject.get(Constants.RES_success).toString().equalsIgnoreCase("false")) {
                    success = false;
                } else {
                    success = true;
                }
            }
            if (success) {
                msg = messageSource.getMessage("acc.machine.syncMachineCostToPMSucess", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.machineMaster.syncMachineToPMFailure", null, RequestContextUtils.getLocale(request));
            }
        } catch (SessionExpiredException | ServiceException | JSONException | NoSuchMessageException ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    /**
     * Description: This method is used to get Machine Master Details
     * @param request
     * @param response
     * @return JSONObject
     */
    
    public ModelAndView getActiveSubstituteMachines(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            
            /*Get Machine Master Related Parameters*/
             Map<String, Object> requestParams = getMachineMasterCommonParameters(request);
             
             /*Get Machine Details*/
             
             jobj=accMachineManagementServiceDAOObj.getActiveSubstituteMachines(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
    
    
    /**
     * Description : Used get Machine Master common parameters
     * @param <request> getting parameters from request object
     * @return Map
     */
    
    public Map<String, Object> getMachineMasterCommonParameters(HttpServletRequest request) {
        Map<String, Object> requestParams = new HashMap<>();
        String [] arrayOfID=null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            requestParams.put(Constants.df, df);
            requestParams.put(Constants.userdf, userdf);
            
            if(!StringUtil.isNullOrEmpty(request.getParameter("isdelete"))&& request.getParameter("isdelete").equalsIgnoreCase("true")){
             JSONArray jArr = new JSONArray(request.getParameter("data"));
             arrayOfID=new String[jArr.length()];
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    arrayOfID[i]=StringUtil.DecodeText(jobj.optString("id"));
                }
            requestParams.put("idsfordelete", arrayOfID);
            }
            requestParams.put(Constants.ss, request.getParameter(Constants.ss));
            requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
            requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
            requestParams.put("start", start);
            requestParams.put("limit", limit);
            requestParams.put("companyid", companyid);
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            if(!StringUtil.isNullOrEmpty(request.getParameter("isactivemachine"))){
             requestParams.put("isactivemachine", request.getParameter("isactivemachine"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("activemachineid"))){
             requestParams.put("activemachineid", request.getParameter("activemachineid"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("issubstitutemachine"))){
             requestParams.put("issubstitutemachine", request.getParameter("issubstitutemachine"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("isleasemachine"))){
             requestParams.put("isleasemachine", request.getParameter("isleasemachine"));
            }
           
            if(!StringUtil.isNullOrEmpty(request.getParameter("isSubstituteMachine"))){
             requestParams.put("isSubstituteMachine", request.getParameter("isSubstituteMachine"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("workcenterid"))){
             requestParams.put("workcenterid", request.getParameter("workcenterid"));
            }
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("moduleid"))) {
                requestParams.put("moduleid", request.getParameter("moduleid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("machineids"))) {
                requestParams.put("machineids", request.getParameter("machineids"));
            }
            String searchJson = request.getParameter("searchJson");
            String filterConjuction = request.getParameter("filterConjuctionCriteria");
            if (!StringUtil.isNullOrEmpty(searchJson) && !StringUtil.isNullOrEmpty(filterConjuction)) {
                requestParams.put("searchJson", searchJson);
                requestParams.put("filterConjuctionCriteria", filterConjuction);
            }

        } catch (Exception ex) {
            Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requestParams;
    }
    public Map<String, Object> getMachineDetailParameters(HttpServletRequest request) throws AccountingException, ServiceException, Exception{
        Map<String, Object> requestParams = new HashMap<>();
        String id="",machineNumber="";
        String sequenceformat ="",nextAutoNumber="";
        boolean isMachineIDAlreadyPresent=false;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            sequenceformat = request.getParameter("sequenceformat");
            DateFormat df = authHandler.getDateOnlyFormat(request);
            requestParams.put(Constants.df, df);
            long createdon = System.currentTimeMillis();
            Date dateOfInstallation=null,dateOfPurchase=null,insuranceDueDate=null;
            id=request.getParameter("id");
            
            boolean isEdit=false;
            String isEditStr=request.getParameter("isEdit");
            if(!StringUtil.isNullOrEmpty(isEditStr)){
                isEdit=Boolean.parseBoolean(isEditStr);
            } 
 
            machineNumber=request.getParameter("machineid");
            requestParams.put("id", id);
            requestParams.put("machinename", request.getParameter("machinename"));
            requestParams.put("machineid", request.getParameter("machineid"));
            requestParams.put("machineserialno", request.getParameter("machineserialno"));
            requestParams.put("createdon", createdon);
            
            
            if(!StringUtil.isNullOrEmpty(request.getParameter("machineoperatingcapacity"))){
            requestParams.put("machineoperatingcapacity", request.getParameter("machineoperatingcapacity"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("machinevendor"))){
            requestParams.put("machinevendor", request.getParameter("machinevendor"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("machineusescount"))) {
                requestParams.put("machineusescount", request.getParameter("machineusescount"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("dateofinstallation"))) {
                dateOfInstallation=df.parse(request.getParameter("dateofinstallation"));
                requestParams.put("dateofinstallation", dateOfInstallation);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("dateofpurchase"))) {
                dateOfPurchase=df.parse(request.getParameter("dateofpurchase"));
                requestParams.put("dateofpurchase", dateOfPurchase);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("insuranceduedate"))) {
                insuranceDueDate=df.parse(request.getParameter("insuranceduedate"));
                requestParams.put("insuranceduedate", insuranceDueDate);
            }
          /* Machine Lease Management  */
            if (!StringUtil.isNullOrEmpty(request.getParameter("hasmachineonlease"))) {

                Date startDateOfLease = null, endDateOfLease = null;
                requestParams.put("ismachineonlease", request.getParameter("hasmachineonlease"));

                if (!StringUtil.isNullOrEmpty(request.getParameter("startdateoflease"))) {
                    startDateOfLease = df.parse(request.getParameter("startdateoflease"));
                    requestParams.put("startdateoflease", startDateOfLease);
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("enddateoflease"))) {
                    endDateOfLease = df.parse(request.getParameter("enddateoflease"));
                    requestParams.put("enddateoflease", endDateOfLease);
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("leaseyears"))) {
                    requestParams.put("leaseyears", request.getParameter("leaseyears"));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("machineprice"))) {
                    requestParams.put("machineprice", request.getParameter("machineprice"));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("depreciationmethod"))) {
                    requestParams.put("depreciationmethod", request.getParameter("depreciationmethod"));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("depreciationrate"))) {
                    requestParams.put("depreciationrate", request.getParameter("depreciationrate"));
                }

            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isassetmachine"))) {
                requestParams.put("isassetmachine", "true");

                if (!StringUtil.isNullOrEmpty(request.getParameter("assetdetailId"))) {
                    requestParams.put("assetdetailId", request.getParameter("assetdetailId"));
                }
            }
            
            if(!StringUtil.isNullOrEmpty(request.getParameter("ageofmachine"))){
            requestParams.put("ageofmachine", request.getParameter("ageofmachine"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("purchaseaccountid"))){
            requestParams.put("purchaseaccount", request.getParameter("purchaseaccountid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("vendorid"))) {
                requestParams.put("vendorid", request.getParameter("vendorid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("process"))) {
                requestParams.put("processid", request.getParameter("process").split(","));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("workcenter"))){
                requestParams.put("workcenter", request.getParameter("workcenter")); 
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("shifttiming"))){
                requestParams.put("shifttiming", request.getParameter("shifttiming")); 
            }
           
            requestParams.put(Constants.companyid, companyid);
            if(!StringUtil.isNullOrEmpty(request.getParameter("activemachineid"))){
             requestParams.put("activemachineid", request.getParameter("activemachineid").split(","));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("issubstitutemachine"))){
             requestParams.put("issubstitutemachine", request.getParameter("issubstitutemachine"));
            }
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("fullMachineTime"))) {
                requestParams.put("fullMachineTime", request.getParameter("fullMachineTime"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("fullManTime"))) {
                requestParams.put("fullManTime", request.getParameter("fullManTime"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("partMachineTime"))) {
                requestParams.put("partMachineTime", request.getParameter("partMachineTime"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("partManTime"))) {
                requestParams.put("partManTime", request.getParameter("partManTime"));
            }
            
            
            /* Sequence Format Code.*/
            synchronized (this) {
                 HashMap<String, Object> requestParams1 = new HashMap();
                    requestParams1.put("companyid", companyid);
                    requestParams1.put("machineid", request.getParameter("machineid"));
                if (isEdit && !StringUtil.isNullOrEmpty(id) && sequenceformat.equals("NA")) {
                    requestParams1.put("id", id);
                }
                isMachineIDAlreadyPresent = accMachineManagementServiceDAOObj.isMachineIDAlreadyPresent(requestParams1);
                if (isMachineIDAlreadyPresent) {
                    
                    
                    
                    if (!isEdit) {

                        if (sequenceformat.equals("NA")) {
                            throw new AccountingException(messageSource.getMessage("acc.machine.machineNumber", null, RequestContextUtils.getLocale(request)) + " ' " + " <b>" + machineNumber + "</b> " + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                        }

                    } else {
                        if (sequenceformat.equals("NA")) {
                            throw new AccountingException(messageSource.getMessage("acc.machine.machineNumber", null, RequestContextUtils.getLocale(request)) + " ' " + " <b>" + machineNumber + "</b> " + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                        }
                        nextAutoNumber = request.getParameter("machineid");

                    }
                    

                }else{
                    boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                    String nextAutoNoInt = "";
                    String datePrefix = "";
                    String dateafterPrefix = "";
                    String dateSuffix = "";
                    if (!sequenceformat.equals("NA")) {
                        if (seqformat_oldflag) {
                            nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_MACHINE_Management, sequenceformat);
                        } else {
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_MACHINE_Management, sequenceformat, seqformat_oldflag, dateOfPurchase);
                            nextAutoNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                            nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                            datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                            dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                            dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                            
                            requestParams.put(Constants.SEQFORMAT, sequenceformat);
                            requestParams.put(Constants.SEQNUMBER, nextAutoNoInt);
                            requestParams.put(Constants.DATEPREFIX, datePrefix);
                            requestParams.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                            requestParams.put(Constants.DATESUFFIX, dateSuffix);
                        }
                        machineNumber = nextAutoNumber;
                    }
                    
                    
                }
                
                if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.MRP_Machine_Management_ModuleId, machineNumber, companyid);
                    if (!list.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) list.get(0);
                        String formatName = (String) list.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + machineNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                }

            }
            requestParams.put("machineid", machineNumber);
            requestParams.put("autogenerated", nextAutoNumber.equals(machineNumber));
            /*
            Custom Field
            */
            if (!StringUtil.isNullOrEmpty((String) request.getParameter("customfield"))) {
                String customfield = request.getParameter("customfield");
                requestParams.put("customfield", customfield);
            }

        } catch (Exception ex) {
            Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
            if(ex instanceof AccountingException){
                throw ex;
            }else{
            throw ServiceException.FAILURE(ex.getMessage(), ex);
            }
        }
        return requestParams;
    }
    public ModelAndView exportMachineMaster(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            
            /*Get Machine Master Related Parameters*/
             Map<String, Object> requestParams = getMachineMasterCommonParameters(request);
             requestParams.put("request", request);
             requestParams.put("response", response);
             
             /*Get Machine Details*/
             
             jobj=accMachineManagementServiceDAOObj.exportMachineMaster(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
    
    /**
     * Description: This method is used to delete the Machine Master Details Permanently
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView deleteMachineMasterPermanently(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Machine_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        String msg = "";
        try {
            status = txnManager.getTransaction(def);
            /*Get Machine Master Related Parameters*/
             Map<String, Object> requestParams = getMachineMasterCommonParameters(request);
             
             jobj=accMachineManagementServiceDAOObj.deleteMachineMasterPermanently(requestParams);
             txnManager.commit(status);
            String action = AuditAction.MACHINE_MASTER_DELETED;
            String auditMsg = " User " + sessionHandlerImpl.getUserFullName(request) + " has deleted machine(s)";
            auditTrailDaoObj.insertAuditLog(action, auditMsg, request, "");

             issuccess = true;
             msg=jobj.getString("msg");
        } catch (Exception ex) {
             if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
    /**
     * Description: This method is used to delete the Machine Master Details Temporarily
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView deleteMachineMaster(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Machine_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        String msg = "";
        try {
            status = txnManager.getTransaction(def);
            /*Get Machine Master Related Parameters*/
             Map<String, Object> requestParams = getMachineMasterCommonParameters(request);
             
             jobj=accMachineManagementServiceDAOObj.deleteMachineMaster(requestParams);
             txnManager.commit(status);
               
                String action =AuditAction.MACHINE_MASTER_DELETED ;
                String auditMsg = " User " + sessionHandlerImpl.getUserFullName(request) + " has deleted machine(s)";
                auditTrailDaoObj.insertAuditLog(action, auditMsg, request, "");
          
             issuccess = true;
             msg=jobj.getString("msg");
        } catch (Exception ex) {
             if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
    public ModelAndView getMachinesForCombo(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {
            JSONArray jSONArray = new JSONArray();
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("companyId", sessionHandlerImpl.getCompanyid(request));
            if (request.getParameter("workcenterid") != null && !StringUtil.isNullOrEmpty(request.getParameter("workcenterid").toString())) {
                hashMap.put("workcenterid", request.getParameter("workcenterid").toString());
            }
            jSONArray = accMachineManagementServiceDAOObj.getMachineCombo(hashMap);
            jobj.put("data", jSONArray);
            jobj.put("count", jSONArray.length());
            issuccess = true;

        } catch (JSONException ex) {
            Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, "AccLabourControllerCMN.deleteLabours", ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "");
            } catch (JSONException ex) {
                Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView saveMachineManRatio(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        int isDuplicateflag = -1;
        boolean isUpdate = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {           
            Map<String, Object> requestParams = new HashMap<>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userid = sessionHandlerImpl.getUserid(request);
            requestParams.put("id",request.getParameter("id"));
            requestParams.put("machine",request.getParameter("machine"));
            requestParams.put("dateForRatio",request.getParameter("dateForRatio"));
            requestParams.put("fullMachineTime",request.getParameter("fullMachineTime"));
            requestParams.put("fullManTime",request.getParameter("fullManTime"));
            requestParams.put("partMachineTime",request.getParameter("partMachineTime"));
            requestParams.put("partManTime",request.getParameter("partManTime"));
            requestParams.put("companyid", companyid);
            requestParams.put("userid", userid);
            requestParams.put(Constants.df,authHandler.getDateOnlyFormat(request));            
            jobj=accMachineManagementServiceDAOObj.saveMachineManRatio(requestParams);           
            issuccess = true;
            txnManager.commit(status);
            if(jobj.has("isUpdate")){
                isUpdate=jobj.optBoolean("isUpdate", isUpdate);
            }            
            if(isUpdate){
                msg = messageSource.getMessage("acc.field.acc.field.Ratiohasbeenupdatedsuccessfully", null, RequestContextUtils.getLocale(request));
            }else{
                msg = messageSource.getMessage("acc.field.acc.field.Ratiohasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
            }
        } catch (SessionExpiredException | NumberFormatException | NoSuchMessageException | TransactionException | ServiceException ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("isDuplicateflag", isDuplicateflag);
            } catch (JSONException ex) {
                Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getMachineManRatio(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        try {
            HashMap<String, Object> requestParams = new HashMap<>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            filter_names.add("company.companyID");
            filter_params.add(companyid);
            requestParams.put(Constants.filterNamesKey, filter_names);
            requestParams.put(Constants.filterParamsKey, filter_params);
            requestParams.put(Constants.df,authHandler.getDateOnlyFormat(request));            
            jobj=accMachineManagementServiceDAOObj.getMachineManRatio(requestParams);
            issuccess = true;
        } catch (SessionExpiredException | ServiceException  ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView deleteMachineManRatio(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String id = request.getParameter("id");
            
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("id", id);
            requestParams.put("companyid", companyid);
            jobj = accMachineManagementServiceDAOObj.deleteMachineManRatio(requestParams);
            msg = messageSource.getMessage("acc.field.acc.field.Ratiohasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));

        } catch (SessionExpiredException | ServiceException ex) {
            Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    public ModelAndView syncMachineDataToPM(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyid);
            requestParams.put("syncable", "false");
            
            jobj = accMachineManagementServiceDAOObj.syncMachineDataToPM(requestParams);
            if (jobj.has(Constants.RES_success)) {

                if (jobj.get(Constants.RES_success).toString().equalsIgnoreCase("false")) {
                    issuccess = false;
                } else {
                    issuccess = true;
                }
            }
            if (issuccess) {
                msg = messageSource.getMessage("acc.machineMaster.syncMachineToPMSucess", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.machineMaster.syncMachineToPMFailure", null, RequestContextUtils.getLocale(request));
            }
           
        } catch (SessionExpiredException | ServiceException  | JSONException ex) {
            Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    public ModelAndView exportMachineAllocationReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String dateFormatPattern = "yyyy-MM-dd";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userid = sessionHandlerImpl.getUserid(request);
            String fromDate = request.getParameter("fromdate");
            String toDate = request.getParameter("todate");
            String resourceids = request.getParameter("resourceids");
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyid);
            requestParams.put("userid", userid);
            requestParams.put("request", request);
            requestParams.put("response", response);
            requestParams.put("fromdate", fromDate);
            requestParams.put("todate", toDate);
            requestParams.put("dateformat", dateFormatPattern);
            requestParams.put("resourceids", resourceids);
            
            jobj = accMachineManagementServiceDAOObj.exportMachineAllocationReportXlsx(requestParams);
            if (jobj.has(Constants.RES_success)) {
                if (jobj.optBoolean(Constants.RES_success)) {
                    issuccess = true;
                }
            }
            if (issuccess) {
                msg = messageSource.getMessage("acc.machineMaster.syncMachineToPMSucess", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.machineMaster.syncMachineToPMFailure", null, RequestContextUtils.getLocale(request));
            }
           
        } catch (SessionExpiredException | ServiceException ex) {
            Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    // Fucntion to get expander details for machine
    public ModelAndView getMachineExpanderDetails(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        try {
            JSONObject tempJobj = new JSONObject();
            JSONArray jarr = new JSONArray();
            HashMap<String, Object> requestParams = (HashMap<String, Object>) getMachineMasterCommonParameters(request);
            if(!StringUtil.isNullOrEmpty(request.getParameter("id"))){
             requestParams.put("machineid", request.getParameter("id"));
            }
            jobj = accMachineManagementServiceDAOObj.getExpanderDetails(requestParams);
        } catch(Exception ex) {
            Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            try {
                jobj.put("success", true);
                jobj.put("msg", "");
            } catch (JSONException ex) {
                Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

}
