/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.routingmanagement;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.mrp.WorkOrder.AccWorkOrderServiceDAO;
import com.krawler.spring.mrp.WorkOrder.WorkOrder;
import com.krawler.spring.mrp.WorkOrder.WorkOrderLabourMapping;
import com.krawler.spring.mrp.WorkOrder.WorkOrderMachineMapping;
import com.krawler.spring.mrp.WorkOrder.WorkOrderWorkCenterMapping;
import com.krawler.spring.mrp.jobwork.AccJobWorkController;
import com.krawler.spring.mrp.jobwork.JobWork;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class AccRoutingManagementController extends MultiActionController implements MessageSourceAware {

    private AccRoutingManagementService accRoutingManagementService;
    private HibernateTransactionManager txnManager;
    private String successView;
    private MessageSource messageSource;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccRoutingManagementDao accRoutingManagementDaoObj;
    private exportMPXDAOImpl exportDaoObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccProductModuleService accProductModuleService;
    private accProductDAO accProductDAO;
    private auditTrailDAO auditTrailObj;
   
      public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    public void setAccProductDAO(accProductDAO accProductDAO) {
        this.accProductDAO = accProductDAO;
    }
      private AccWorkOrderServiceDAO accWorkOrderServiceDAOObj;

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setAccProductModuleService(AccProductModuleService accProductModuleService) {
        this.accProductModuleService = accProductModuleService;
    }

   

    public void setAccWorkOrderServiceDAOObj(AccWorkOrderServiceDAO accWorkOrderServiceDAOObj) {
        this.accWorkOrderServiceDAOObj = accWorkOrderServiceDAOObj;
    }

    public void setExportDaoObj(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setAccCompanyPreferencesObj(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setAccRoutingManagementService(AccRoutingManagementService accRoutingManagementService) {
        this.accRoutingManagementService = accRoutingManagementService;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    public void setAccRoutingManagementDaoObj(AccRoutingManagementDao accRoutingManagementDaoObj) {
        this.accRoutingManagementDaoObj = accRoutingManagementDaoObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    public Map<String, Object> getCommonParameters(HttpServletRequest request) {
        Map<String, Object> requestParams = new HashMap<>();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            requestParams.put(Constants.userdf, userdf);
            requestParams.put(Constants.df, df);
            requestParams.put(Constants.ss, request.getParameter(Constants.ss));
            requestParams.put("isRoutingCode", request.getParameter("isRoutingCode"));
            requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
            requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
            requestParams.put("billid", request.getParameter("billid"));

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                requestParams.put("start", start);
                requestParams.put("limit", limit);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isforcombo"))) {
                requestParams.put("isforcombo", Boolean.parseBoolean(request.getParameter("isforcombo")));
            }
             if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.BOMID))) {
                requestParams.put(RoutingTemplate.BOMID, request.getParameter(RoutingTemplate.BOMID));
            }
            requestParams.put("companyid", companyid);
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("moduleid"))) {
                requestParams.put("moduleid", request.getParameter("moduleid"));
            }
             if (!StringUtil.isNullOrEmpty(request.getParameter("routingmastertype"))) {
                requestParams.put("routingmastertype", request.getParameter("routingmastertype"));
            }
            String searchJson = request.getParameter("searchJson");
            String filterConjuctionCriteria = request.getParameter("filterConjuctionCriteria");
            if(!StringUtil.isNullOrEmpty(searchJson) && !StringUtil.isNullOrEmpty(filterConjuctionCriteria)){
                requestParams.put("searchJson", request.getParameter("searchJson"));
                requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
            }

        } catch (Exception ex) {
            Logger.getLogger(AccRoutingManagementController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requestParams;
    }

    public ModelAndView getRoutingTemplates(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = getCommonParameters(request);
            jobj = accRoutingManagementService.getRoutingtemplates(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccRoutingManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccRoutingManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, jobj.toString());
    }

     
    public ModelAndView createProject(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = new HashMap<>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userid = sessionHandlerImpl.getUserid(request);
            requestParams.put("companyid", companyid);
            requestParams.put("userid", userid);
            requestParams.put("isNewProject", request.getParameter("isNewProject"));
            requestParams.put("isMasterProject", request.getParameter("isMasterProject"));
            requestParams.put("projectId", request.getParameter("projectId"));
            requestParams.put("projectcode","");
            jobj = accRoutingManagementService.createOrUpdateProjectRest(requestParams);
            issuccess = jobj.optBoolean("success");
            msg=jobj.optString("msg");
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccRoutingManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccRoutingManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
     public JSONObject syncResourceToPMOnPlanTaskClick(HttpServletRequest request) throws ServiceException {
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

            /*Sync labours*/
            requestParams.put("resourceId", request.getParameter("labourids"));
            requestParams.put("resourcetype", "labour");
            JSONObject jSONObject = accRoutingManagementService.syncResourceToPM(requestParams);
            /*Sync machines*/
            requestParams.put("resourceId", request.getParameter("machineids"));
            requestParams.put("resourcetype", "machine");
            jSONObject = accRoutingManagementService.syncResourceToPM(requestParams);

            if (jSONObject.has(Constants.RES_success)) {
                if (jSONObject.get(Constants.RES_success).toString().equalsIgnoreCase("true")) {
                    isSuccess = true;
                } else {
                    isSuccess = false;
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(msg, ex);
        }
        return jobj;
    }
     public JSONObject syncproductsToPM(HttpServletRequest request) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        try {
            /*Below function is used to sync product,Bom(with its products) and their checklist pm.*/
            Map<String, Object> requestParams1 = new HashMap<String, Object>();
            requestParams1.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams1.put("userId", sessionHandlerImpl.getUserid(request));
            requestParams1.put("projectId", request.getParameter("projectId"));
            requestParams1.put("productid", request.getParameter("productid"));
            requestParams1.put("bomid", request.getParameter("bomid"));
            
            
            
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
            KwlReturnObject result = accProductDAO.getAssemblyItems(requestParams);
           
            //json of product used in creation of  assembly product
            /*Get request parameters */
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accProductModuleService.getAssemblyItemsJson(paramJobj, result.getEntityList(),0);
            jobj.put("count", result.getRecordTotalCount());
            
            requestParams1.put("bomwiseproducts", jobj.get("data"));
            jobj = accWorkOrderServiceDAOObj.syncBOmWithChecklistToPM(requestParams1);
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccRoutingManagementController.syncproductsToPM", ex);
        }
        return jobj;
    }
    public ModelAndView syncDataToPM(HttpServletRequest request, HttpServletResponse response) {
        /*
         this function is used to sync routing template masterprojectidm,labour,machines to PM
         */
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = true;
        JSONObject jSONObject = null;
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("userId", sessionHandlerImpl.getUserid(request));
            requestParams.put("projectid", request.getParameter("projectId"));
         

            JSONObject jbj = syncResourceToPMOnPlanTaskClick(request);

            jSONObject = syncproductsToPM(request);
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccRoutingManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView syncResourceToPM(HttpServletRequest request, HttpServletResponse response) {
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
            requestParams.put("resourceId", request.getParameter("resourceId"));
            requestParams.put("projectId", request.getParameter("projectId"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("isLabour"))) {
                boolean isLabour = Boolean.parseBoolean(request.getParameter("isLabour"));
                if (isLabour) {
                    requestParams.put("resourcetype", "labour");
                } else {
                    requestParams.put("resourcetype", "machine");
                }
            }
            JSONObject jSONObject = accRoutingManagementService.syncResourceToPM(requestParams);
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
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, isSuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccRoutingManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
   public ModelAndView syncProjectCopyReqToPM(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            /*
             Below function is used to sync Resource
             */
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("userId", sessionHandlerImpl.getUserid(request));
            requestParams.put("masterprojectid", request.getParameter("masterprojectid"));
            requestParams.put("projectid", request.getParameter("projectId"));
            JSONObject jSONObject = accRoutingManagementService.syncProjectCopyReqToPM(requestParams);
            success = jSONObject.optBoolean("success");
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccRoutingManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    public ModelAndView saveRoutingTemplate(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String isEditStr=request.getParameter("isEdit");
            Boolean isEdit=!StringUtil.isNullOrEmpty(isEditStr) ? Boolean.parseBoolean(isEditStr) : false;
            JSONObject Obj = saveRoutingTemplate(request);
            String auditMsg = "";
            String auditID = "";
            isSuccess = true;
            if (Obj.has("isrtnamealreadypresnt") && Obj.getBoolean("isrtnamealreadypresnt")) {
                String rtName = "";
                if (Obj.has(RoutingTemplate.RTNAME) && !StringUtil.isNullOrEmpty(Obj.getString(RoutingTemplate.RTNAME))) {
                    rtName = Obj.getString(RoutingTemplate.RTNAME);
                    isSuccess = false;
                }
                msg = messageSource.getMessage("mrp.workorder.entry.routingcode", null, RequestContextUtils.getLocale(request)) + " <b>" + rtName + "</b> " + messageSource.getMessage("acc.mrp.routingtemplate.beforesave.duplicatertname.msg", null, RequestContextUtils.getLocale(request));
            } else {
                String docNo = Obj.has("documentno") ? Obj.getString("documentno") : "";
                msg = messageSource.getMessage("acc.mrp.routingtemplate.save.success", null, RequestContextUtils.getLocale(request)) + " <br> Document : <b>" + docNo + "</b>";
                if (!isEdit) {
                    auditMsg = Constants.ROUTING_TEMPLATE_ADDED;
                    auditID = AuditAction.ADD_ROUTING_TEMPLATE;
                    auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + Obj.getString("documentno"), request, "");
                } else {
                    auditMsg = Constants.ROUTING_TEMPLATE_UPDATED;
                    auditID = AuditAction.EDIT_ROUTING_TEMPLATE;
                    auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + Obj.getString("documentno"), request, "");

                }
                txnManager.commit(status);
            }
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = messageSource.getMessage("acc.mrp.routingtemplate.save.failure", null, RequestContextUtils.getLocale(request));
            Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", isSuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    public boolean isRoutingTemplateNameAlreadyExist(HttpServletRequest request, boolean isAlternateRouting) throws ServiceException {
        JSONObject jobj = new JSONObject();
        boolean isJobWorkIDAlreadyPresent = false;
        try {
            HashMap<String, Object> requestParams = new HashMap();
            requestParams.put(JobWork.COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put("checkDuplicateName", true);
            if (!isAlternateRouting) {
                if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.RTID))) {
                    requestParams.put(RoutingTemplate.RTID, request.getParameter(RoutingTemplate.RTID));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.RTNAME))) {
                    requestParams.put(RoutingTemplate.RTNAME, request.getParameter(RoutingTemplate.RTNAME));
                }
            } else {
                if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.ALTERNATERTID))) {
                    requestParams.put(RoutingTemplate.RTID, request.getParameter(RoutingTemplate.ALTERNATERTID));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.ALTERNATERTNAME))) {
                    requestParams.put(RoutingTemplate.RTNAME, request.getParameter(RoutingTemplate.ALTERNATERTNAME));
                }
            }

            isJobWorkIDAlreadyPresent = accRoutingManagementService.isROutingTemplateNameAlreadyExist(requestParams);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("", ex);
        }
        return isJobWorkIDAlreadyPresent;
    }
    public JSONObject saveRoutingTemplate(HttpServletRequest request) throws AccountingException, ServiceException {
        JSONObject jobj = new JSONObject();

        Map<String, Object> dataMap = new HashMap();

        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userid = sessionHandlerImpl.getUserid(request);
            dataMap = getCommonParameters(request);

            String roujtingTemplateName = request.getParameter(RoutingTemplate.RTNAME);
             DateFormat df = authHandler.getDateOnlyFormat(request);
             
            Calendar cal=Calendar.getInstance();
            cal.clear(Calendar.ZONE_OFFSET);
            Date createdOn=cal.getTime();
            String sequenceformat = request.getParameter(Constants.SEQFORMAT);
            dataMap.put(Constants.SEQFORMAT, sequenceformat);
            String id = "",nextAutoNumber = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.RTID))) {
                id = request.getParameter(RoutingTemplate.RTID);
                dataMap.put(RoutingTemplate.RTID, request.getParameter(RoutingTemplate.RTID));

            }
            String entrynumber = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.RTNAME))) {
                entrynumber = request.getParameter(RoutingTemplate.RTNAME);
                dataMap.put(RoutingTemplate.RTNAME, request.getParameter(RoutingTemplate.RTNAME));
            }
            
    
            /*
            If routing template name already present then do not allow user to add new routing tempalte with the same name
            */
            boolean isJobWorkIDAlreadyPresent = false;
            synchronized (this) {
                /*
                 * Checking if routing template name already exists or not
                 */
                isJobWorkIDAlreadyPresent = isRoutingTemplateNameAlreadyExist(request,false);
                if (isJobWorkIDAlreadyPresent) {
                    /*
                     * New Routing Temlpate  Case
                     */ 
                    if (StringUtil.isNullOrEmpty(id) && sequenceformat.equals("NA")) {
                        jobj.put(RoutingTemplate.RTNAME, request.getParameter(RoutingTemplate.RTNAME));
                        jobj.put("isrtnamealreadypresnt", true);
                        return jobj;
                    } else if (!StringUtil.isNullOrEmpty(id) && sequenceformat.equals("NA")) {
                        /*
                         * Edit Case
                         */ 
                        isJobWorkIDAlreadyPresent = isRoutingTemplateNameAlreadyExist(request,false);
                        if (isJobWorkIDAlreadyPresent) {
                            jobj.put(RoutingTemplate.RTNAME, request.getParameter(RoutingTemplate.RTNAME));
                            jobj.put("isrtnamealreadypresnt", true);
                            return jobj;
                        } else {
                            nextAutoNumber = entrynumber;
                        }

                    } else {
                        nextAutoNumber = entrynumber;

                    }
                    
                    
                } else {
                    boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                    String nextAutoNoInt = "";
                    String datePrefix = "";
                    String dateafterPrefix = "";
                    String dateSuffix = "";
                    if (!sequenceformat.equals("NA")) {
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_MRP_ROUTECODE, sequenceformat, false, null);
                        nextAutoNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                        nextAutoNoInt = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                        datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        dateafterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                        dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part

                        dataMap.put(Constants.SEQFORMAT, sequenceformat);
                        dataMap.put(Constants.SEQNUMBER, nextAutoNoInt);
                        dataMap.put(Constants.DATEPREFIX, datePrefix);
                        dataMap.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                        dataMap.put(Constants.DATESUFFIX, dateSuffix);
                        entrynumber = nextAutoNumber;
                    }
                }
            } 
            
            if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.MRP_RouteCode, entrynumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + entrynumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
            
            dataMap.put("autogenerated", sequenceformat.equals("NA") ? false : true);
            dataMap.put(RoutingTemplate.USERID, userid);
            dataMap.put(RoutingTemplate.COMPANYID, companyid);
            
            dataMap.put(RoutingTemplate.CREATEDON, createdOn);
           
            if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.PROJECTID))) {
                dataMap.put(RoutingTemplate.PROJECTID, request.getParameter(RoutingTemplate.PROJECTID));
            }
             if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.BOMID))) {

                dataMap.put(RoutingTemplate.BOMID, request.getParameter(RoutingTemplate.BOMID));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.DURATIONTYPE))) {

                if (request.getParameter(RoutingTemplate.DURATIONTYPE).equals(RoutingTemplate.HOURS)) {

                    dataMap.put(RoutingTemplate.DURATIONTYPE, 0);
                } else if (request.getParameter(RoutingTemplate.DURATIONTYPE).equals(RoutingTemplate.DAYS)) {
                    dataMap.put(RoutingTemplate.DURATIONTYPE, 1);
                }
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.DURATION))) {

                dataMap.put(RoutingTemplate.DURATION, request.getParameter(RoutingTemplate.DURATION));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.WORKCENTER))) {

                dataMap.put(RoutingTemplate.WORKCENTER, request.getParameter(RoutingTemplate.WORKCENTER));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.MACHINEMAPPING))) {

                dataMap.put(RoutingTemplate.MACHINEMAPPING, request.getParameter(RoutingTemplate.MACHINEMAPPING));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.LABOURMAPPING))) {

                dataMap.put(RoutingTemplate.LABOURMAPPING, request.getParameter(RoutingTemplate.LABOURMAPPING));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.CUSTOMFIELD))) {

                dataMap.put(RoutingTemplate.CUSTOMFIELD, request.getParameter(RoutingTemplate.CUSTOMFIELD));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("productid"))) {
                dataMap.put("productid", request.getParameter("productid"));
            }

            //*******************
            jobj = accRoutingManagementService.saveRoutingTemplate(dataMap);
            jobj.put("documentno", roujtingTemplateName);
            /*
             save Alternate routing
            
             */
            String alternateRouting = request.getParameter("alternateRouting");
            
            /*
            Need to remove while saving alternate routing rtid beacause rtid is pushed into datamap while saveing customfields
            */
//            dataMap.remove(RoutingTemplate.RTID);
            dataMap = getCommonParameters(request);
            dataMap.put(RoutingTemplate.USERID, userid);
            dataMap.put(RoutingTemplate.COMPANYID, companyid);
            dataMap.put(RoutingTemplate.CREATEDON, createdOn);
            
            if (!StringUtil.isNullOrEmpty(alternateRouting) && alternateRouting.equalsIgnoreCase("on")) {
                /*
                 If routing template name already present then do not allow user to add new routing tempalte with the same name
                 */
                isJobWorkIDAlreadyPresent = isRoutingTemplateNameAlreadyExist(request, true);
                if (isJobWorkIDAlreadyPresent) {
                    jobj.put(RoutingTemplate.RTNAME, jobj.optString(RoutingTemplate.RTNAME, "")+request.getParameter(RoutingTemplate.ALTERNATERTNAME));
                    jobj.put("isrtnamealreadypresnt", true);
                    return jobj;
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.ALTERNATERTID))) {

                    dataMap.put(RoutingTemplate.RTID, request.getParameter(RoutingTemplate.ALTERNATERTID));

                }
                if (!StringUtil.isNullOrEmpty(jobj.optString(RoutingTemplate.PARENTRTID))) {
                    dataMap.put(RoutingTemplate.PARENTRTID, jobj.optString(RoutingTemplate.PARENTRTID));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.ALTERNATERTNAME))) {
                    roujtingTemplateName+=", "+ request.getParameter(RoutingTemplate.ALTERNATERTNAME);
                    dataMap.put(RoutingTemplate.RTNAME, request.getParameter(RoutingTemplate.ALTERNATERTNAME));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.ALTERNATEPROJECTID))) {
                    dataMap.put(RoutingTemplate.PROJECTID, request.getParameter(RoutingTemplate.ALTERNATEPROJECTID));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.ALTERNATEBOMID))) {

                    dataMap.put(RoutingTemplate.BOMID, request.getParameter(RoutingTemplate.ALTERNATEBOMID));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("alternateproduct"))) {

                    dataMap.put("productid", request.getParameter("alternateproduct"));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.ALTERNATEDURATIONTYPE))) {

                    if (request.getParameter(RoutingTemplate.ALTERNATEDURATIONTYPE).equals(RoutingTemplate.ALTERNATEHOURS)) {

                        dataMap.put(RoutingTemplate.DURATIONTYPE, 0);
                    } else if (request.getParameter(RoutingTemplate.ALTERNATEDURATIONTYPE).equals(RoutingTemplate.ALTERNATEDAYS)) {
                        dataMap.put(RoutingTemplate.DURATIONTYPE, 1);
                    }
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.ALTERNATEDURATION))) {

                    dataMap.put(RoutingTemplate.DURATION, request.getParameter(RoutingTemplate.ALTERNATEDURATION));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.ALTERNATEWORKCENTER))) {

                    dataMap.put(RoutingTemplate.WORKCENTER, request.getParameter(RoutingTemplate.ALTERNATEWORKCENTER));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.ALTERNATEMACHINEMAPPING))) {

                    dataMap.put(RoutingTemplate.MACHINEMAPPING, request.getParameter(RoutingTemplate.ALTERNATEMACHINEMAPPING));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter(RoutingTemplate.ALTERNATELABOURMAPPING))) {

                    dataMap.put(RoutingTemplate.LABOURMAPPING, request.getParameter(RoutingTemplate.ALTERNATELABOURMAPPING));
                }
                jobj = accRoutingManagementService.saveRoutingTemplate(dataMap);
                
                jobj.put("documentno", roujtingTemplateName);
            }

        } catch (Exception ex) {
                throw ServiceException.FAILURE("saveRoutingTemplate " + ex.getMessage(), ex);
        }
        return jobj;
    }
  
    /*
    This function is used to delete routing templates permanently
    */
    
      public ModelAndView deleteRoutingTemplate(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        Map<String, Object> dataMap = new HashMap();
        TransactionStatus status = null;
        try {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("BRecnl_Tx");
            status = txnManager.getTransaction(def);
            dataMap = getCommonParameters(request);
            String data = request.getParameter("jsonObj");
            String templateNames=request.getParameter("templateNames");

            Boolean isPermDelete = !StringUtil.isNullOrEmpty(request.getParameter("isPermDelete")) ? Boolean.parseBoolean(request.getParameter("isPermDelete")) : false;
            dataMap.put("isPermDelete", isPermDelete);
            dataMap.put("data", data);
            jobj = accRoutingManagementService.deleteRoutingTemplate(dataMap);
            
            String auditMsg = Constants.ROUTING_TEMPLATE_DELETED;
            String auditID = AuditAction.DELETE_ROUTING_TEMPLATE;
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + templateNames, request, "");
            txnManager.commit(status);
            
             msg = messageSource.getMessage("acc.mrp.routingtemplate.delete.success", null, RequestContextUtils.getLocale(request));
            issuccess = true;
           
        } catch (Exception ex) {
            issuccess = false;
             msg = messageSource.getMessage("acc.mrp.labour.delete.failure", null, RequestContextUtils.getLocale(request));
            txnManager.rollback(status);
            Logger.getLogger(AccRoutingManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccRoutingManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
      
    /*
     This function is used to export routing template data
     */
    
       public ModelAndView exportRoutingTemplates(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            JSONArray invJArr = new JSONArray();

            Map<String, Object> requestParams = new HashMap();
            requestParams = getCommonParameters(request);
            requestParams.put("isExport", true);
             jobj = accRoutingManagementService.getRoutingtemplates(requestParams);

            exportDaoObj.processRequest(request, response, jobj);
        } catch (Exception ex) {
            Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
       
        public ModelAndView deleteDirtyProject(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            /*
             Below function is used to delete dirty project
             */
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("userid", sessionHandlerImpl.getUserid(request));
            requestParams.put("projectId", request.getParameter("projectId"));
            requestParams.put(Constants.MRP_isDirtyProject, true);
            JSONObject jSONObject = accRoutingManagementService.deleteDirtyProjectRest(requestParams);
            success = jSONObject.optBoolean("success");
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccRoutingManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

}
