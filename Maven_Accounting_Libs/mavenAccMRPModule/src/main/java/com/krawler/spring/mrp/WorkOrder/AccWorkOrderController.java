/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.WorkOrder;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.mrp.jobwork.AccJobWorkController;
import com.krawler.spring.mrp.labormanagement.AccLabourControllerCMN;
import com.krawler.spring.mrp.machinemanagement.accMachineManagementController;
import com.krawler.spring.mrp.routingmanagement.AccRoutingManagementService;
import com.krawler.spring.mrp.workcentremanagement.AccWorkCentreControllerCMN;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
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

/**
 *
 * @author krawler
 */
public class AccWorkOrderController  extends MultiActionController implements MessageSourceAware{
    private MessageSource messageSource;
    private HibernateTransactionManager txnManager;
    private String successView;
    private AccWorkOrderServiceDAO accWorkOrderServiceDAOObj;
    private WorkOrderDAO workOrderDAOObj;
    private AccRoutingManagementService  routingManagementServiceObj;
    private AccProductModuleService accProductModuleService;
    private accProductDAO accProductObj;
      private exportMPXDAOImpl exportDaoObj; 

    public void setExportDaoObj(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
     
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }


    public void setAccProductModuleService(AccProductModuleService accProductModuleService) {
        this.accProductModuleService = accProductModuleService;
    }

    public void setRoutingManagementServiceObj(AccRoutingManagementService routingManagementServiceObj) {
        this.routingManagementServiceObj = routingManagementServiceObj;
    }
    public void setAccWorkOrderServiceDAOObj(AccWorkOrderServiceDAO accWorkOrderServiceDAOObj) {
        this.accWorkOrderServiceDAOObj = accWorkOrderServiceDAOObj;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }
    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setWorkOrderDAOObj(WorkOrderDAO workOrderDAOObj) {
        this.workOrderDAOObj = workOrderDAOObj;
    }
    
    

    @Override
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }
    public ModelAndView saveWorkOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        KwlReturnObject kmsg = null;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("WO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userid = sessionHandlerImpl.getUserid(request);
            boolean isEdit = false;
            boolean isBOMChanged = false;
            boolean isMassCreate = false;
            boolean genpocheck =false;
            String jsonDataStr = "[]";
//            JSONObject jobj = new jo
            if ( request.getParameter("data") != null) {
                jsonDataStr = request.getParameter("data");
            }
            if( request.getParameter("isEdit") != null ) {
                isEdit = Boolean.parseBoolean(request.getParameter("isEdit").toString());
            }
            if( request.getParameter("isBOMChanged") != null ) {
                isBOMChanged = Boolean.parseBoolean(request.getParameter("isBOMChanged").toString());
            }
            if( request.getParameter("isMassCreate") != null ) {
                isMassCreate = Boolean.parseBoolean(request.getParameter("isMassCreate").toString());
            }
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("data", jsonDataStr);
            requestParams.put("isEdit", isEdit);
            requestParams.put("isBOMChanged", isBOMChanged);
            requestParams.put("isMassCreate", isMassCreate);
            requestParams.put("companyid", companyid);
            requestParams.put("userid", userid);
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            requestParams.put("details", request.getParameter("details"));
            /**
             * Following values are added to request Params Object 
             * to save Audit Trail entry along with descriptive information about user action.
             */
            requestParams.put("prdjsondtls",request.getParameter("detail"));
            requestParams.put("remoteAddress",request.getRemoteAddr());
            requestParams.put("reqHeader",request.getHeader("x-real-ip"));
            requestParams.put("companyprefdetails",request.getAttribute("companyprefdetails"));
            requestParams.put("userfullname",sessionHandlerImpl.getUserFullName(request));
            kmsg = accWorkOrderServiceDAOObj.saveWorkOrder(requestParams);
            
            WorkOrder workorder=(WorkOrder)kmsg.getEntityList().get(1);
            jobj.put("workorderid", workorder.getID());
            msg = kmsg.getEntityList().get(0).toString();
            genpocheck= kmsg.getEntityList().get(2)!=null?Boolean.parseBoolean(kmsg.getEntityList().get(2).toString()):false;
            jobj.put("genpocheck", genpocheck);
            issuccess = true;
            txnManager.commit(status);
//            msg = kmsg.getMsg();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                if (msg.contains("#DuplicateException#")) {
                    jobj.put("isDuplicateExe", true);
                    msg = msg.replaceAll("#DuplicateException#", "");
                }
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, "model", jobj.toString());
    }
    public ModelAndView updateMassStatus(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        KwlReturnObject kmsg = null;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("WO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userid = sessionHandlerImpl.getUserid(request);
            boolean isEdit = false;
            boolean isBOMChanged = false;
            boolean isMassCreate = false;
            String jsonDataStr = "[]";
//            JSONObject jobj = new jo
            if ( request.getParameter("data") != null) {
                jsonDataStr = request.getParameter("data");
            }
            if( request.getParameter("isEdit") != null ) {
                isEdit = Boolean.parseBoolean(request.getParameter("isEdit").toString());
            }
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("data", jsonDataStr);
            requestParams.put("isEdit", isEdit);
            requestParams.put("isBOMChanged", isBOMChanged);
            requestParams.put("isMassCreate", isMassCreate);
            requestParams.put("companyid", companyid);
            requestParams.put("userid", userid);
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            requestParams.put("details", request.getParameter("details"));
            requestParams.put(Constants.RES_REQUEST, request);
            kmsg = accWorkOrderServiceDAOObj.updateMassStatus(requestParams);
            
            WorkOrder workorder=(WorkOrder)kmsg.getEntityList().get(1);
            jobj.put("workorderid", workorder.getID());
            msg = kmsg.getEntityList().get(0).toString();
            issuccess = true;
            txnManager.commit(status);
//            msg = kmsg.getMsg();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                if (msg.contains("#DuplicateException#")) {
                    jobj.put("isDuplicateExe", true);
                    msg = msg.replaceAll("#DuplicateException#", "");
                }
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, "model", jobj.toString());
    }
    public ModelAndView getWorkOrderDataandColumnModel(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        KwlReturnObject kwlObj= null;
        String msg = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Map<String, Object> requestParams = getWorkOrderCommonParameters(request);
            requestParams.put("companyid", companyid);
            jobj = accWorkOrderServiceDAOObj.getWorkOrderDataandColumnModel(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public Map<String, Object> getWorkOrderCommonParameters(HttpServletRequest request) {
        Map<String, Object> requestParams = new HashMap<>();
        String [] arrayOfID=null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            requestParams.put(Constants.userdf, userdf);
            requestParams.put(Constants.df, df);
            requestParams.put(Constants.ss, request.getParameter(Constants.ss));
            requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
            requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
            requestParams.put(WorkOrder.SALESORDERID, request.getParameter(WorkOrder.SALESORDERID));
            requestParams.put(WorkOrder.SALESCONTRACTID, request.getParameter(WorkOrder.SALESCONTRACTID));
            requestParams.put(WorkOrder.WOID, request.getParameter(WorkOrder.WOID));
            requestParams.put("type", request.getParameter("type")); // product Type
            requestParams.put("billid", request.getParameter("billid"));
            requestParams.put("bills", request.getParameter("bills"));
            requestParams.put("productidstr", request.getParameter("productidstr"));
            requestParams.put(Constants.RES_REQUEST, request);
            requestParams.put("isFromWO", request.getParameter("isFromWO"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("isForCompAvailablity"))) {

                requestParams.put("isForCompAvailablity", request.getParameter("isForCompAvailablity"));

            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isWOStockDetailsReport"))) {

                requestParams.put("isWOStockDetailsReport", request.getParameter("isWOStockDetailsReport"));

            }
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("customerComboValue"))) {

                requestParams.put("customerComboValue", request.getParameter("customerComboValue"));

            }
            
            
            if(!StringUtil.isNullOrEmpty(request.getParameter("isdelete"))&& request.getParameter("isdelete").equalsIgnoreCase("true")){
             JSONArray jArr = new JSONArray(request.getParameter("data"));
             arrayOfID=new String[jArr.length()];
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    arrayOfID[i]=StringUtil.DecodeText(jobj.optString("id"));
                }
            requestParams.put("idsfordelete", arrayOfID);
            }

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                requestParams.put("start", start);
                requestParams.put("limit", limit);
            }

            requestParams.put("companyid", companyid);
            requestParams.put("userid", sessionHandlerImpl.getUserid(request));
            requestParams.put(WorkOrder.COMPANYID, companyid);
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("moduleid"))) {
                requestParams.put("moduleid", request.getParameter("moduleid"));
            }
              if (!StringUtil.isNullOrEmpty(request.getParameter("wostatus"))) {
                requestParams.put("wostatus", request.getParameter("wostatus"));
            }
            String searchJson = request.getParameter("searchJson");
            String filterConjuction = request.getParameter("filterConjuctionCriteria");
            if (!StringUtil.isNullOrEmpty(searchJson) && !StringUtil.isNullOrEmpty(filterConjuction)) {
                requestParams.put("searchJson", searchJson);
                requestParams.put("filterConjuctionCriteria", filterConjuction);
            }
            
        } catch (SessionExpiredException | JSONException ex) {
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requestParams;
    }
    
    public ModelAndView deleteWorkOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("delWC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        String msg = "";
        try {
            status = txnManager.getTransaction(def);
            /*Get Machine Master Related Parameters*/
             Map<String, Object> requestParams = getWorkOrderCommonParameters(request);
             
             jobj=accWorkOrderServiceDAOObj.deleteWorkOrder(requestParams);
             txnManager.commit(status);
             issuccess = true;
             msg=jobj.getString("msg");
        } catch (Exception ex) {
             if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(AccWorkCentreControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkCentreControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    } 
    
    public ModelAndView getWorkOrderExpanderDetails(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        try {
            JSONObject tempJobj = new JSONObject();
            JSONArray jarr = new JSONArray();
            HashMap<String, Object> requestParams = (HashMap<String, Object>) getWorkOrderCommonParameters(request);
            
            if(requestParams.containsKey("isWOStockDetailsReport") && Boolean.parseBoolean((String) requestParams.get("isWOStockDetailsReport"))){
            jobj=accWorkOrderServiceDAOObj.getExpanderWOStockDetails(requestParams);
            
            }else{
            
            jobj = accWorkOrderServiceDAOObj.getExpanderDetails(requestParams);
            
            }
                
        } catch(Exception ex) {
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            try {
                jobj.put("success", true);
                jobj.put("msg", "");
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    public ModelAndView getSOSCForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            String linkfrom = request.getParameter("linkfrom");
            String customer = request.getParameter(WorkOrder.CUSTOMERID);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean isEdit = Boolean.parseBoolean(request.getParameter("isEdit").toString());
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put(WorkOrder.COMPANYID, sessionHandlerImpl.getCompanyid(request));
            hashMap.put("linkfrom", linkfrom);
            hashMap.put(WorkOrder.CUSTOMERID, customer);
            hashMap.put("isEdit", isEdit);            
            jobj = accWorkOrderServiceDAOObj.getSOSCCombo(hashMap);
        } catch(Exception ex) {
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            try {
                jobj.put("success", true);
                jobj.put("msg", "");
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    public ModelAndView deleteWorkOrderPermanently(HttpServletRequest request, HttpServletResponse response) {
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
             Map<String, Object> requestParams = getWorkOrderCommonParameters(request);
             
             jobj=accWorkOrderServiceDAOObj.deleteWorkOrderPermanently(requestParams);
             txnManager.commit(status);
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
    public ModelAndView exportWorkOrder(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            
            /*Get Machine Master Related Parameters*/
             Map<String, Object> requestParams = getWorkOrderCommonParameters(request);
             requestParams.put("request", request);
             requestParams.put("response", response);
             
             /*Get Machine Details*/
             
             jobj=accWorkOrderServiceDAOObj.exportWorkOrder(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
    
    public ModelAndView getProductsForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = (HashMap<String, Object>) getWorkOrderCommonParameters(request);
            jobj = accWorkOrderServiceDAOObj.getProductsForCombo(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
    public ModelAndView getWorkOrderComponentDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Map<String, Object> requestParams = getWorkOrderCommonParameters(request);
            requestParams.put("companyid", companyid);
            JSONArray jsonArray = accWorkOrderServiceDAOObj.getWorkOrderComponentDetails(requestParams);
            jobj.put("data", jsonArray);
            jobj.put("count", jsonArray.length());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getShortFallProductsDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Map<String, Object> requestParams = getWorkOrderCommonParameters(request);
            requestParams.put("companyid", companyid);
            JSONArray jsonArray = accWorkOrderServiceDAOObj.getShortFallProductsDetails(requestParams);
            jobj.put("data", jsonArray);
            jobj.put("count", jsonArray.length());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
      
    public ModelAndView getWorkOrderForCombo(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            boolean issuccess = false;
            JSONArray jSONArray = new JSONArray();
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("companyId", sessionHandlerImpl.getCompanyid(request));
            jSONArray = accWorkOrderServiceDAOObj.getWOCombo(hashMap);
            jobj.put("data", jSONArray);
            jobj.put("count", jSONArray.length());
        } catch (JSONException ex) {
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, "AccLabourControllerCMN.deleteLabours", ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    // controller function to check component availability
    public ModelAndView checkComponentAvailability(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            boolean issuccess = false;
            JSONArray jSONArray = new JSONArray();
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("companyId", sessionHandlerImpl.getCompanyid(request));
            hashMap.put("userid", sessionHandlerImpl.getUserid(request));
            hashMap.put("userid", sessionHandlerImpl.getUserid(request));
            hashMap.put("woidArr", request.getParameter("woidArr"));
            hashMap.put("flag", 1);
            /**
             * Following values are added to request Params Object 
             * to save Audit Trail entry along with descriptive information about user action.
             */
            hashMap.put("reqHeader",request.getHeader("x-real-ip"));
            hashMap.put("remoteAddress",request.getRemoteAddr());
            hashMap.put("prdjsondtls",request.getParameter("detail"));
            hashMap.put("companyprefdetails",request.getAttribute("companyprefdetails"));
            hashMap.put("userfullname",sessionHandlerImpl.getUserFullName(request));
            
            jobj = accWorkOrderServiceDAOObj.changeStatustoInProcess(hashMap);
            jobj.put("data", jSONArray);
            jobj.put("count", jSONArray.length());
            jobj.put("success", true);
        } catch (JSONException ex) {
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, "AccLabourControllerCMN.deleteLabours", ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject syncResourceToPM(HttpServletRequest request) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        try {
            /*
             Below function is used to sync Resource
             */
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("userId", sessionHandlerImpl.getUserid(request));
            requestParams.put("projectId", request.getParameter("projectId"));

            /*
            Sync labours
             */
            requestParams.put("resourceId", request.getParameter("labourids"));
            requestParams.put("resourcetype", "labour");
            JSONObject jSONObject = routingManagementServiceObj.syncResourceToPM(requestParams);
            /*
            Sync machines
             */
            requestParams.put("resourceId", request.getParameter("machineids"));
            requestParams.put("resourcetype", "machine");
            jSONObject = routingManagementServiceObj.syncResourceToPM(requestParams);

            if (jSONObject.has(Constants.RES_success)) {
                if (jSONObject.get(Constants.RES_success).toString().equalsIgnoreCase("true")) {
                    isSuccess = true;
                } else {
                    isSuccess = false;
                }
            }
            if (isSuccess) {
                msg = messageSource.getMessage("acc.RoutingMaster.syncResourceToPMSucess", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.mrppm.failure", null, RequestContextUtils.getLocale(request));
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(msg, ex);
        }
        return jobj;
    }
     public JSONObject syncBOmWithChecklistToPM(HttpServletRequest request) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        try {
            /*
             Below function is used to sync product,Bom(with its products) and their checklist pm.
             */
            Map<String, Object> requestParams1 = new HashMap<String, Object>();
            requestParams1.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams1.put("userId", sessionHandlerImpl.getUserid(request));
            requestParams1.put("projectId", request.getParameter("projectId"));
            requestParams1.put("productid", request.getParameter("productid"));
            requestParams1.put("bomid", request.getParameter("bomid"));
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("salesorderid"))) {
                requestParams1.put("salesorderid", request.getParameter("salesorderid"));
            }
            
                      
             HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("productid", request.getParameter("productid"));
            requestParams.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
            if(!StringUtil.isNullOrEmpty(request.getParameter("isdefaultbom"))){
                boolean isdefaultbom=Boolean.parseBoolean(request.getParameter("isdefaultbom"));
                requestParams.put("isdefaultbom", isdefaultbom);
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("bomid"))){
                requestParams.put("bomdetailid", request.getParameter("bomid"));
            }
            //get products with BOM and assembly products
            KwlReturnObject result = accProductObj.getAssemblyItems(requestParams);
           
            //json of product used in creation of  assembly product
             /*Get request parameters */
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accProductModuleService.getAssemblyItemsJson(paramJobj, result.getEntityList(),0);
            jobj.put("count", result.getRecordTotalCount());
            
            requestParams1.put("bomwiseproducts", jobj.get("data"));
            jobj = accWorkOrderServiceDAOObj.syncBOmWithChecklistToPM(requestParams1);
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccWorkOrderController.syncBOmWithChecklistToPM", ex);
        }
        return jobj;
    }
     
    public JSONObject syncWorkOrderDateToPM(HttpServletRequest request)throws ServiceException{
        JSONObject syncwodateresobj = new JSONObject();
        Map<String,Object> requestParam = new HashMap<String,Object>();        
        try {
            requestParam.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            requestParam.put(Constants.useridKey, sessionHandlerImpl.getUserid(request));
            requestParam.put(Constants.projectid, request.getParameter(Constants.projectId));
            requestParam.put(Constants.workorderdate, request.getParameter(Constants.workorderdate));
            requestParam.put(Constants.isShiftProjectStartDate,request.getParameter(Constants.isShiftProjectStartDate));
            syncwodateresobj = accWorkOrderServiceDAOObj.syncWorkOrderDateToPM(requestParam);
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccWorkOrderController.syncWorkOrderDateToPM", ex);
        }  
        
        return syncwodateresobj;
    }
    /*
     * This function is used to sync workorder date to PM
     */
    public ModelAndView syncWorkOrderDateToPM(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject syncwodateresobj = null;
        String msg = "";
        boolean success = false;
        try {
            syncwodateresobj = syncWorkOrderDateToPM(request);
            if (syncwodateresobj.has(Constants.RES_success)) {
                if (syncwodateresobj.get(Constants.RES_success).toString().equalsIgnoreCase("true")) {
                    success = true;
                }
            }
            if (success) {
                msg = messageSource.getMessage("acc.workorder.syncWorkOrderDateToPMSuccess", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.workorder.syncWorkOrderDateToPMFailure", null, RequestContextUtils.getLocale(request));
            }            
        } catch (Exception Ex) {
            msg = Ex.getMessage();
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, Ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (Exception e) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    public ModelAndView syncDataToPM(HttpServletRequest request, HttpServletResponse response) {
        /*
         this function is used to sync routing template masterprojectidm,labour,machines to PM
         */
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = true;
        JSONObject jSONObject = null;
        JSONObject syncwodateresobj = null; 
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("userId", sessionHandlerImpl.getUserid(request));
            requestParams.put("masterprojectid", request.getParameter("masterprojectid"));
            requestParams.put("projectid", request.getParameter("projectId"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("masterprojectid"))) {
                jSONObject = routingManagementServiceObj.syncProjectCopyReqToPM(requestParams);
            }

            JSONObject jbj = syncResourceToPM(request);

            jSONObject = syncBOmWithChecklistToPM(request);
            
            syncwodateresobj = syncWorkOrderDateToPM(request); 
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
        public ModelAndView getRejectedItemsList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = getWorkOrderCommonParameters(request);
            requestParams.put("isExport", false);
            jobj = accWorkOrderServiceDAOObj.getRejectedItemListReport(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, jobj.toString());
    }
        
    public ModelAndView getQualityControlParametersList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = getWorkOrderCommonParameters(request);
            requestParams.put("isExport", false);
            if (!StringUtil.isNullOrEmpty(request.getParameter("projectid"))) {
                requestParams.put("projectid", request.getParameter("projectid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("statustype"))) {
                requestParams.put("statustype", request.getParameter("statustype"));
            }
            
            jobj = accWorkOrderServiceDAOObj.getQualityControlParameters(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, jobj.toString());
    }
     
    public ModelAndView exportMRPQCReportList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            
            /*Get filter  Parameters*/
             Map<String, Object> requestParams = getWorkOrderCommonParameters(request);
             requestParams.put("request", request);
             requestParams.put("response", response);
             if (!StringUtil.isNullOrEmpty(request.getParameter("projectid"))) {
                requestParams.put("projectid", request.getParameter("projectid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("statustype"))) {
                requestParams.put("statustype", request.getParameter("statustype"));
            }
             /*Get rejected item  Details*/
             
             jobj=accWorkOrderServiceDAOObj.exportMRPQCReportList(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
    public ModelAndView exportRejectedItemsList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            
            /*Get filter  Parameters*/
             Map<String, Object> requestParams = getWorkOrderCommonParameters(request);
             requestParams.put("request", request);
             requestParams.put("response", response);
             
             /*Get rejected item  Details*/
             
             jobj=accWorkOrderServiceDAOObj.exportRejectedItemsList(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
    
    public ModelAndView getShortFallProductsReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject  jobj = new JSONObject();
        boolean issuccess = false;
        boolean isRowExpander = false;
        String jsonView= "jsonView_ex";
        String  msg = "";
        try {
            Map<String, Object> requestParams = getWorkOrderCommonParameters(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("productid"))) {
                requestParams.put("productid", request.getParameter("productid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isForExpander"))) {
                requestParams.put("isForExpander", request.getParameter("isForExpander"));
                isRowExpander = Boolean.parseBoolean(request.getParameter("isForExpander"));
            }
            requestParams.put("isExport", false);
            jobj = accWorkOrderServiceDAOObj.getWorkOrderShortFallReport(requestParams);
            issuccess = true;
            if (isRowExpander) {
                jsonView = "jsonView";
            } 
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return new ModelAndView(jsonView, Constants.model, jobj.toString());
    }
    
    public ModelAndView getWorkOrderTaskProgressDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = getWorkOrderCommonParameters(request);
            requestParams.put("isExport", false);
            String workorderid = "", projectid = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("workorderid"))) {
                workorderid = request.getParameter("workorderid");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("projectid"))) {
                projectid = request.getParameter("projectid");
            }
            requestParams.put("workorderid", workorderid);
            requestParams.put("projectid", projectid);
            jobj = accWorkOrderServiceDAOObj.getTaskDetailsOfworkOrder(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, jobj.toString());
    }
    
     public ModelAndView exportWorkOrderTaskProgressDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            
            /*Get filter  Parameters*/
             Map<String, Object> requestParams = getWorkOrderCommonParameters(request);
            requestParams.put("isExport", true);
             requestParams.put("request", request);
             requestParams.put("response", response);
             if (!StringUtil.isNullOrEmpty(request.getParameter("workorderid"))) {
                requestParams.put("workorderid", request.getParameter("workorderid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("projectid"))) {
                requestParams.put("projectid", request.getParameter("projectid"));
                jobj = accWorkOrderServiceDAOObj.exportWorkOrdersTask(requestParams);
                issuccess = true;
            }else{
            issuccess = false;
            }
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
     
        
    public ModelAndView getWorkOrderCostingReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            Map<String, Object> requestParams = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(request.getParameter("woid"))) {
                requestParams.put("woid", request.getParameter("woid"));
            }
            boolean isMRPProfitablityReport= request.getParameter("isMRPProfitablityReport")!=null ? Boolean.parseBoolean(request.getParameter("isMRPProfitablityReport")) : false;
             requestParams.put("isMRPProfitablityReport", isMRPProfitablityReport);
            requestParams.put("costingType", request.getParameter("costingType")!=null ? Integer.parseInt(request.getParameter("costingType")) : 0);
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            DateFormat df = authHandler.getOnlyDateFormat(request);
            requestParams.put(Constants.df, df);
                        requestParams.put("isExport", false);
            jobj = accWorkOrderServiceDAOObj.getWorkOrderCosting(requestParams);

        } catch (Exception ex) {
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return new ModelAndView(Constants.jsonView_ex, Constants.model, jobj.toString());
    }
 
    public ModelAndView expoertMRPCosting(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            Map<String, Object> requestParams = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(request.getParameter("woid"))) {
                requestParams.put("woid", request.getParameter("woid"));
            }
            boolean isMRPProfitablityReport= request.getParameter("isMRPProfitablityReport")!=null ? Boolean.parseBoolean(request.getParameter("isMRPProfitablityReport")) : false;
            requestParams.put("isMRPProfitablityReport", isMRPProfitablityReport);
            requestParams.put("costingType", request.getParameter("costingType")!=null ? Integer.parseInt(request.getParameter("costingType")) : 0);
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            DateFormat df = authHandler.getOnlyDateFormat(request);
            requestParams.put(Constants.df, df);
            requestParams.put("isExport", true);
            jobj = accWorkOrderServiceDAOObj.getWorkOrderCosting(requestParams);
            
             exportDaoObj.processRequest(request, response, jobj);
            
        } catch (Exception ex) {

            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());

    }
}

