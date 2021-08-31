/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.workcentremanagement;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.mrp.WorkOrder.AccWorkOrderController;
import com.krawler.spring.mrp.WorkOrder.WorkOrder;
import com.krawler.spring.mrp.jobwork.AccJobWorkController;
import com.krawler.spring.mrp.labormanagement.AccLabourControllerCMN;
import com.krawler.spring.mrp.machinemanagement.accMachineManagementController;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
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
public class AccWorkCentreControllerCMN extends MultiActionController implements MessageSourceAware {

    private MessageSource messageSource;
    private HibernateTransactionManager txnManager;
    private String successView;
    private AccWorkCentreServiceDAO accWorkCentreServiceDAOObj;
    
    @Override
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setAccWorkCentreServiceDAOObj(AccWorkCentreServiceDAO accWorkCentreServiceDAOObj) {
        this.accWorkCentreServiceDAOObj = accWorkCentreServiceDAOObj;
    }
    
    public ModelAndView saveWorkCentre(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        KwlReturnObject kmsg = null;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("WC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userid = sessionHandlerImpl.getUserid(request);
            boolean isEdit = false;
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
            requestParams.put("companyid", companyid);
            requestParams.put("userid", userid);
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            requestParams.put("request", request);
            
            if (!StringUtil.isNullOrEmpty((String) request.getParameter("customfield"))) {
                String customfield = request.getParameter("customfield");
                requestParams.put("customfield", customfield);
            }
            kmsg = accWorkCentreServiceDAOObj.saveWorkCentre(requestParams);
            txnManager.commit(status);
            msg = kmsg.getMsg();
            issuccess = true;
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(AccWorkCentreControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                if (msg.contains("#DuplicateException#")) {
                    jobj.put("isDuplicateExe", true);
                    msg = msg.replaceAll("#DuplicateException#", "");
                }
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkCentreControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getWorkCentreDataandColumnModel(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        KwlReturnObject kwlObj= null;
        String msg = "";
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            Map<String, Object> requestParams = getWorkCentreCommonParameters(request);
            requestParams.put("companyId", companyId);
            jobj = accWorkCentreServiceDAOObj.getWorkCentreDataandColumnModel(requestParams);
            issuccess = true;
        } catch (Exception ex) {
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
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public Map<String, Object> getWorkCentreCommonParameters(HttpServletRequest request) {
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
            requestParams.put(WorkCentre.PRODUCTID, request.getParameter(WorkCentre.PRODUCTID));
            requestParams.put(Constants.RES_REQUEST, request);
            requestParams.put("billid", request.getParameter("billid"));
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
            requestParams.put("start", start);
            requestParams.put("limit", limit);

            requestParams.put("companyid", companyid);
            requestParams.put(WorkCentre.COMPANYID, companyid);
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("moduleid"))) {
                requestParams.put("moduleid", request.getParameter("moduleid"));
            }
             if (!StringUtil.isNullOrEmpty(request.getParameter("workcenterids"))) {
                requestParams.put("workcenterids", request.getParameter("workcenterids"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("totalWorkCenters"))) {
                requestParams.put("totalWorkCenters", request.getParameter("totalWorkCenters"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("selworkCenterIds"))) {
                requestParams.put("ids", request.getParameter("selworkCenterIds"));             
            }
            String searchJson = request.getParameter("searchJson");
            String filterConjuction = request.getParameter("filterConjuctionCriteria");
            if (!StringUtil.isNullOrEmpty(searchJson) && !StringUtil.isNullOrEmpty(filterConjuction)) {
                requestParams.put("searchJson", searchJson);
                requestParams.put("filterConjuctionCriteria", filterConjuction);
            }
            
        } catch (Exception ex) {
            Logger.getLogger(AccWorkCentreControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requestParams;
    }
    public ModelAndView deleteWorkCentres(HttpServletRequest request, HttpServletResponse response) {
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
             Map<String, Object> requestParams = getWorkCentreCommonParameters(request);
            
             jobj=accWorkCentreServiceDAOObj.deleteWorkcentre(requestParams);
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
    public ModelAndView getWorkCentreForCombo(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            boolean issuccess = false;
            JSONArray jSONArray = new JSONArray();
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("companyId", sessionHandlerImpl.getCompanyid(request));
            jSONArray = accWorkCentreServiceDAOObj.getWCCombo(hashMap);
            jobj.put("data", jSONArray);
            jobj.put("count", jSONArray.length());

        } catch (JSONException ex) {
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, "AccLabourControllerCMN.deleteLabours", ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView deleteWorkCentrePermanently(HttpServletRequest request, HttpServletResponse response) {
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
             Map<String, Object> requestParams = getWorkCentreCommonParameters(request);

             jobj=accWorkCentreServiceDAOObj.deleteWorkCentrePermanently(requestParams);
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
    
    public ModelAndView exportWorkCentre(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            
            /*Get Machine Master Related Parameters*/
             Map<String, Object> requestParams = getWorkCentreCommonParameters(request);
             requestParams.put("request", request);
             requestParams.put("response", response);
             
              if (requestParams.containsKey("start")) {
                requestParams.remove("start");
             }
             if (requestParams.containsKey("limit")) {
                requestParams.remove("limit");
             }
             /*Get Machine Details*/
             
             jobj=accWorkCentreServiceDAOObj.exportWorkCentre(requestParams);
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
    
    public ModelAndView getWorkcentresForCombo(HttpServletRequest request, HttpServletResponse response){
       JSONObject jobj = new JSONObject();
        try {
            Map<String, Object> requestParms = getWorkCentreCommonParameters(request);
            jobj = accWorkCentreServiceDAOObj.getWorkcentresForCombo(requestParms);
        } catch (Exception ex) {
            Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    
    }
}
