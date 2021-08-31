/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.workorder;

import com.krawler.common.admin.AccCustomData;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.goodsreceipt.AccGoodsReceiptServiceDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.service.AccProductService;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFuctionality.ExportRecordHandler;
import com.krawler.spring.mrp.WorkOrder.*;
import com.krawler.spring.mrp.routingmanagement.AccRoutingManagementDao;
import com.krawler.spring.mrp.routingmanagement.AccRoutingManagementService;
import com.krawler.spring.mrp.routingmanagement.RoutingTemplate;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
 * @author krawler
 */
public class AccWorkOrderControllerCMN extends MultiActionController implements MessageSourceAware {

    private MessageSource messageSource;
    private HibernateTransactionManager txnManager;
    private String successView;
    private AccWorkOrderServiceDAO accWorkOrderServiceDAOObj;
    private AccWorkOrderServiceDAOCMN accWorkOrderServiceDAOCMNObj;
    private WorkOrderDAO workOrderDAOObj;
    private AccRoutingManagementService  routingManagementServiceObj;
    private AccProductModuleService accProductModuleService;
    private accProductDAO accProductObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccRoutingManagementDao accRoutingManagementDaoObj;
    private AccProductService AccProductService;
    private auditTrailDAO auditTrailObj;
    private accAccountDAO accAccountDAOobj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccInvoiceServiceDAO accInvoiceServiceDAOObj;
    private AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOobj;
    private CustomDesignDAO customDesignDAOObj;
    private VelocityEngine velocityEngine;

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public void setCustomDesignDAOObj(CustomDesignDAO customDesignDAOObj) {
        this.customDesignDAOObj = customDesignDAOObj;
    }

    public void setAccGoodsReceiptServiceDAOobj(AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOobj) {
        this.accGoodsReceiptServiceDAOobj = accGoodsReceiptServiceDAOobj;
    }

    public void setAccInvoiceServiceDAOObj(AccInvoiceServiceDAO accInvoiceServiceDAOObj) {
        this.accInvoiceServiceDAOObj = accInvoiceServiceDAOObj;
    }

    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setAccAccountDAOobj(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
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
    public void setAccWorkOrderServiceDAOCMNObj(AccWorkOrderServiceDAOCMN accWorkOrderServiceDAOCMNObj) {
        this.accWorkOrderServiceDAOCMNObj = accWorkOrderServiceDAOCMNObj;
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
    
    public void setAccCompanyPreferencesObj(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    
    public void setAccRoutingManagementDaoObj(AccRoutingManagementDao accRoutingManagementDaoObj) {
        this.accRoutingManagementDaoObj = accRoutingManagementDaoObj;
    }
        public void setAccProductService(AccProductService AccProductService) {
        this.AccProductService = AccProductService;
    }
      public ModelAndView sendWOCloseReq(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        boolean isProjectCompleted = false;
        try {
            /*
             Below function is used to send WO Close Req
             */
            String woid = request.getParameter("workorderid");
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("userid", sessionHandlerImpl.getUserid(request));
            requestParams.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
            requestParams.put("workorderid", request.getParameter("workorderid"));
            requestParams.put("projectid", request.getParameter("projectid"));
//            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
//            requestParams.put("jeDataMap", jeDataMap);
//            requestParams.put("cost", 0);
             jobj = accWorkOrderServiceDAOCMNObj.sendWOCloseReq(requestParams);
//            msg = jSONObject.optString("msg");
            success = jobj.optBoolean("success");
            isProjectCompleted = jobj.optBoolean("isProjectCompleted");
            if (isProjectCompleted) {
                if(success == false && jobj.has("response_msg")){
                    msg = jobj.optString("response_msg","Consumption Details are not valid");
                }
               /*
                * This else part is commented because audit trial entry done before closing work order. 
                * The audit trial log entry done in updateInventoryForFinishedGood() method and this method is called when user clicks submit button.
                */
//                else{
//                    KwlReturnObject result = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), woid);
//                    WorkOrder woObj = (WorkOrder) result.getEntityList().get(0);
//                    String auditMsg = " User " + sessionHandlerImpl.getUserFullName(request) + " has closed Work Order  <b>" + woObj.getWorkOrderID() + "</b>";
//                    auditTrailObj.insertAuditLog(AuditAction.WORK_ORDER_CLOSED, auditMsg, request, woObj.getWorkOrderID());
//                    msg = messageSource.getMessage("acc.workorder.updatestatus.success.msg", null, RequestContextUtils.getLocale(request));
//                }
            } else {
                    msg = messageSource.getMessage("acc.workorder.updatestatus.failure.msg", null, RequestContextUtils.getLocale(request));            
            }           
        } catch (Exception ex) {
            msg = msg = messageSource.getMessage("acc.workorder.updatestatus.failure.msg", null, RequestContextUtils.getLocale(request));;
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
      
    public ModelAndView getWorkOrderProductDetail(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = new HashMap<>();
             requestParams.put(WorkOrder.WOID, request.getParameter(WorkOrder.WOID));
            jobj = accWorkOrderServiceDAOCMNObj.getWorkOrderProductDetail(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(AccWorkOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
    public ModelAndView updateInventoryForFinishedGood(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("WO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            Map<String, Object> requestParams = new HashMap<>();
             requestParams.put(WorkOrder.WOID, request.getParameter(WorkOrder.WOID));
             requestParams.put("finalQuantity", request.getParameter("quantity"));
             requestParams.put("fgproductbatchDetails", request.getParameter("fgproductbatchDetails"));
             requestParams.put("assemblygridJson", request.getParameter("assemblygridJson"));
             requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
             requestParams.put("userid", sessionHandlerImpl.getUserid(request));
             requestParams.put("createdby", sessionHandlerImpl.getUserid(request));
             requestParams.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
            jobj = accWorkOrderServiceDAOCMNObj.updateInventoryForFinishedGood(requestParams, jeDataMap);
            saveRoutingCodeTemplate(request);
            issuccess = jobj.optBoolean("isSuccess");
             if (issuccess) {
                /*
                 * If issuccess is true then prompt with success message and audit trial entry.
                 */
                msg = messageSource.getMessage("acc.workorder.updatestatus.success.msg", null, RequestContextUtils.getLocale(request));
                KwlReturnObject result = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), request.getParameter(WorkOrder.WOID));
                WorkOrder woObj = (WorkOrder) result.getEntityList().get(0);
                String auditMsg = " User " + sessionHandlerImpl.getUserFullName(request) + " has closed Work Order  <b>" + woObj.getWorkOrderID() + "</b>";
                auditTrailObj.insertAuditLog(AuditAction.WORK_ORDER_CLOSED, auditMsg, request, woObj.getWorkOrderID(),"");
                msg = messageSource.getMessage("acc.workorder.updatestatus.success.msg", null, RequestContextUtils.getLocale(request));

            } else {
                msg = messageSource.getMessage("acc.mrp.failure", null, RequestContextUtils.getLocale(request));
            }          
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(AccWorkOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
      
    /**
     *  Function To Save Routing Code On Work Order Completion
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView saveRoutingCode(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
             //Updating Status on work order to close if FG don't have Batch/Serial 
            HashMap<String, Object> params = new HashMap<>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            params.put(WorkOrder.WOID, request.getParameter("workorderid"));
            params.put("companyId", companyid);
            params.put("defaultStatusId", Constants.defaultWOstatus_CLOSED);
            params.put("isEdit", true);
            jobj = accWorkOrderServiceDAOObj.changeWOStatus(params);
            saveRoutingCodeTemplate(request);
            msg = messageSource.getMessage("acc.mrp.RC.save", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
            issuccess = true;
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
                msg = "" + ex.getMessage();
            }
            Logger.getLogger(AccWorkOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
      
    public JSONObject saveRoutingCodeTemplate(HttpServletRequest request) {
        JSONObject jSONObject = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {

            /*
             * Save RC
             */
            String routingCodeCreate = request.getParameter("routingCodeCreate") != null ? request.getParameter("routingCodeCreate") : "off";
            if (! StringUtil.isNullOrEmpty(routingCodeCreate) && routingCodeCreate.equalsIgnoreCase("on")) {
                Map<String, Object> requestParams = getRCInformationMap(request);

                /*
                 * Create project for current routing code in PM
                 */
                Map<String, Object> userData = new HashMap<>();
                userData.put("companyid", sessionHandlerImpl.getCompanyid(request));
                userData.put("userid", sessionHandlerImpl.getUserid(request));
                userData.put("projectname", requestParams.get("routecode"));// Sending Routing Code Number as a project Name
                userData.put("isNewProject", "true");// As Updating the Existing Project
                userData.put("isMasterProject", "true");// False for Work Order
                userData.put("projectId", "");
                userData.put("projectcode",requestParams.get("routecode"));
                userData.put(Constants.MRP_isDirtyProject, false);
                JSONObject jobj = routingManagementServiceObj.createOrUpdateProjectRest(userData);
                issuccess = jobj.optBoolean("success");

                /*
                 * Copy all task of current workorder project to newly created
                 * project for routing code
                 */
                String workorderprojectid = requestParams.containsKey("workorderprojectid") ? (String) requestParams.get("workorderprojectid") : "";
                String projectid = jobj.has("projectId") ? jobj.getString("projectId") : "";
                if (issuccess) {
                    Map<String, Object> syncTaskParams = new HashMap();
                    syncTaskParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
                    syncTaskParams.put("userId", sessionHandlerImpl.getUserid(request));
                    syncTaskParams.put("masterprojectid", workorderprojectid);
                    syncTaskParams.put("projectid", projectid);
                    if (!StringUtil.isNullOrEmpty(workorderprojectid)) {
                        jobj = routingManagementServiceObj.syncProjectCopyReqToPM(syncTaskParams);
                    }
                }
                /*
                 * save newly created projectid in against routing code
                 */
                requestParams.put(RoutingTemplate.PROJECTID, projectid);

                jSONObject = routingManagementServiceObj.saveRoutingTemplate(requestParams);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccWorkOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jSONObject;
    }
/**
 * 
 * @param request
 * @return = Map with Request parameter
 * @throws ServiceException
 * @throws AccountingException
 * @throws SessionExpiredException 
 */
    public HashMap<String, Object> getRCInformationMap(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        List list = new ArrayList();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("request", request);
        boolean isEdit = false;
        try {
            String sequenceformat = request.getParameter("sequenceformat") != null ? request.getParameter("sequenceformat") : "NA";
            String companyId = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("routecode", request.getParameter("routecode"));
            requestParams.put("workorder", request.getParameter("workorderid"));
            requestParams.put("sequenceformat", request.getParameter("sequenceformat"));
            requestParams.put("createdby", sessionHandlerImpl.getUserid(request));
            requestParams.put("isroutingcode", true);
            requestParams.put("modifiedby", sessionHandlerImpl.getUserid(request));
            requestParams.put("userid", sessionHandlerImpl.getUserid(request));
            requestParams.put("companyid", companyId);
            Calendar cal = Calendar.getInstance();
            cal.clear(Calendar.ZONE_OFFSET);
            Date createdOn = cal.getTime();
            requestParams.put(RoutingTemplate.CREATEDON, createdOn);
            String workorderid = request.getParameter("workorderid");
            if (!StringUtil.isNullOrEmpty(workorderid)) {
                KwlReturnObject resultPayMethod = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), workorderid);
                WorkOrder workOrder = (WorkOrder) resultPayMethod.getEntityList().get(0);
                if (workOrder != null) {
                    Set<WorkOrderLabourMapping> labourMapping = workOrder.getLabourmapping();
                    String labourids = "";
                    for (WorkOrderLabourMapping Obj : labourMapping) {
                        labourids += Obj.getLabourid().getID() + ",";
                    }
                    if (!StringUtil.isNullOrEmpty(labourids)) {
                        labourids = labourids.substring(0, (labourids.length()) - 1);
                        requestParams.put(RoutingTemplate.LABOURMAPPING, labourids);
                    }
                    Set<WorkOrderMachineMapping> machineMapping = workOrder.getMachinemapping();
                    String machineids = "";
                    for (WorkOrderMachineMapping Obj : machineMapping) {
                        machineids += Obj.getMachineid().getID() + ",";
                    }
                    if (!StringUtil.isNullOrEmpty(machineids)) {
                        machineids = machineids.substring(0, (machineids.length()) - 1);
                        requestParams.put(RoutingTemplate.MACHINEMAPPING, machineids);
                    }
                    Set<WorkOrderWorkCenterMapping> workcenterMapping = workOrder.getWorkcentermapping();
                    String workcenterids = "";
                    for (WorkOrderWorkCenterMapping Obj : workcenterMapping) {
                        workcenterids += Obj.getWorkcentreid().getID() + ",";
                    }
                    if (!StringUtil.isNullOrEmpty(workcenterids)) {
                        workcenterids = workcenterids.substring(0, (workcenterids.length()) - 1);
                        requestParams.put(RoutingTemplate.WORKCENTER, workcenterids);
                    }
                    requestParams.put("workorderprojectid", workOrder.getProjectId() != null ? workOrder.getProjectId() : "");
                    requestParams.put("productid", workOrder.getProductID() != null ? workOrder.getProductID().getID() : "");
                    requestParams.put(RoutingTemplate.BOMID, workOrder.getBomid() != null ? workOrder.getBomid().getID() : "");
                }
            }

            KwlReturnObject result = null;
            String entryNumber = request.getParameter("routecode");
            int countduplicate = 0;
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("companyId", companyId);
            map.put("entryNumber", entryNumber);
            if (sequenceformat.equals("NA")) {
                result = accRoutingManagementDaoObj.getRCNumberCount(map);
                countduplicate = result.getRecordTotalCount();
                if (countduplicate > 0 && sequenceformat.equals("NA")) {
                    throw new AccountingException(messageSource.getMessage("Routing Code", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
            }
            synchronized (this) { //this block is used to generate auto sequence number if number is not duplicate
                String nextAutoNo = "";
                if (!sequenceformat.equals("NA")) {
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_MRP_ROUTECODE, sequenceformat, false, null);
                    requestParams.put("routecode", seqNumberMap.get(Constants.AUTO_ENTRYNUMBER));//complete number
                    requestParams.put(Constants.SEQNUMBER, seqNumberMap.get(Constants.SEQNUMBER));//interger part
                    requestParams.put(Constants.DATEPREFIX, seqNumberMap.get(Constants.DATEPREFIX));
                    requestParams.put(Constants.DATESUFFIX, seqNumberMap.get(Constants.DATESUFFIX));
                    requestParams.put(Constants.SEQFORMAT, sequenceformat);
                }
                if (sequenceformat.equals("NA")) {
                    requestParams.put("routecode", entryNumber);
                }
                requestParams.put("autogenerated", sequenceformat.equals("NA") ? false : true);
            }
            if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.MRP_RouteCode, entryNumber, companyId);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return requestParams;
    }
    public ModelAndView getAssemblyItemsForWO(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            String productId=request.getParameter("productid");
            Product product=null;
            WorkOrder workOrder=null;
            if(!StringUtil.isNullOrEmpty(productId)){
                KwlReturnObject resultPayMethod = accountingHandlerDAOobj.getObject(Product.class.getName(), productId);
                product = (Product) resultPayMethod.getEntityList().get(0);
            }
            String workorderId=request.getParameter("workorderid");
            if(!StringUtil.isNullOrEmpty(workorderId)){
                KwlReturnObject resultPayMethod = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), workorderId);
                workOrder = (WorkOrder) resultPayMethod.getEntityList().get(0);
                requestParams.put("workorderid", workorderId);
                if(workOrder.getBomid()!=null){
                    requestParams.put("bomdetailid", workOrder.getBomid().getID());
                }else{
                    requestParams.put("isdefaultbom", true);
                }
                requestParams.put("projectId", workOrder.getProjectId());
                
            }
            
            requestParams.put("isForCompAvailablity",false);
            requestParams.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            JSONArray jSONArray=new JSONArray();
            jSONArray=AccProductService.getAssemblyProducts(product, requestParams, jSONArray);
            jobj.put("data", jSONArray);
            jobj.put("count", jSONArray.length());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccWorkOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
     public ModelAndView getJOBWOKINProductDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("productid", request.getParameter("productid"));
            requestParams.put("purchaseorderid", request.getParameter("bills"));
            requestParams.put("isMRPJOBWORKIN", request.getParameter("isMRPJOBWORKIN"));
            requestParams.put("type", request.getParameter("type"));
            requestParams.put("ids", request.getParameterValues("ids"));
            
            jobj = accWorkOrderServiceDAOCMNObj.getJOBWOKINProductDetails(requestParams);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccWorkOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccWorkOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccWorkOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccWorkOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }            
    /**
     * Function to print MRP Work Order
     */
    public ModelAndView exportSingleMRPWorkOrder(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        try {
            //get work order id from request
            String WOID = request.getParameter("bills");
            HashMap<String, Object>otherconfigrequestParams = new HashMap();
            String companyid = AccountingManager.getCompanyidFromRequest(request);
            int moduleid = Constants.MRP_WORK_ORDER_MODULEID;
            //get work order custom details object
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), WOID);
            WorkOrder workOrder = (WorkOrder) objItr.getEntityList().get(0);
            AccCustomData  accCustomData = null;
            if (workOrder.getAccWorkOrderCustomData()!=null) {
                accCustomData = workOrder.getAccWorkOrderCustomData();
            }
            //get selected record id's from request
            String recordids = "";
            if(!StringUtil.isNullOrEmpty(request.getParameter("recordids"))){
                recordids = request.getParameter("recordids");
            }
            ArrayList<String> WOIDList = CustomDesignHandler.getSelectedBillIDs(recordids);
            //get customfields details
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid,1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
            replaceFieldMap = new HashMap<String, String>();  
            //Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
            //get dimension details
            fieldrequestParams.clear();
            HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
            HashMap<String, Integer> DimensionFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, dimensionFieldMap);
            
            HashMap<String, JSONArray> itemDataSO = new HashMap<String, JSONArray>();
            //create json of work order details
            for(int count=0 ; count < WOIDList.size() ; count++ ){
                JSONArray lineItemsArr =  accWorkOrderServiceDAOCMNObj.getWODetailsItemJSON(request, companyid, WOIDList.get(count), FieldMap, replaceFieldMap,DimensionFieldMap);
                itemDataSO.put(WOIDList.get(count), lineItemsArr);
            }
            //put moduleid and record id's in map
            otherconfigrequestParams.put(Constants.moduleid, moduleid);
            otherconfigrequestParams.put("recordids", recordids);
            //pass all parameters to export function for printing template
            ExportRecordHandler.exportSingleGeneric(request, response, itemDataSO, accCustomData, customDesignDAOObj, accCommonTablesDAO, accAccountDAOobj, accountingHandlerDAOobj, velocityEngine, "", otherconfigrequestParams, accInvoiceServiceDAOObj, accGoodsReceiptServiceDAOobj);
        } catch (Exception e) {
            Logger.getLogger(AccWorkOrderControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
//    public ModelAndView getWorkOrderExpanderDetails(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj = new JSONObject();
//        try {
//            Map<String, Object> requestParams = new HashMap<>();
//            requestParams.put(WorkOrder.WOID, request.getParameter(WorkOrder.WOID));
//            jobj = accWorkOrderServiceDAOCMNObj.getWorkOrderProducedQtyDetails(requestParams);
//        } catch (Exception ex) {
//            Logger.getLogger(AccWorkOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                jobj.put("success", true);
//                jobj.put("msg", "");
//            } catch (JSONException ex) {
//                Logger.getLogger(AccWorkOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
//    }
    
}
