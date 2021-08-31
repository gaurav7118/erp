/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.labormanagement;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.CustomizeReportMapping;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.hql.accounting.SalesOrderDetail;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.mrp.WorkOrder.WorkOrder;
import com.krawler.spring.mrp.WorkOrder.WorkOrderComponentDetails;
import com.krawler.spring.mrp.WorkOrder.WorkOrderLabourMapping;
import com.krawler.spring.mrp.machinemanagement.accMachineManagementController;
import com.krawler.spring.mrp.workcentremanagement.WorkCentre;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AccLabourServiceImpl implements AccLabourServiceDAO {

    private MessageSource messageSource;
    private accLabourDAO accLabourDAO;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accAccountDAO accAccountDAOobj;
    private fieldDataManager fieldDataManagercntrl;
    private auditTrailDAO auditTrailObj;
    private APICallHandlerService apiCallHandlerService; 
    private accProductDAO accProductDAOObj;

    public void setAccProductDAOObj(accProductDAO accProductDAOObj) {
        this.accProductDAOObj = accProductDAOObj;
    }

    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     
        this.apiCallHandlerService = apiCallHandlerService;
    }
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

    public void setAccLabourDAO(accLabourDAO accLabourDAO) {
        this.accLabourDAO = accLabourDAO;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    /**
     *
     * @param requestParams = contains input params
     * @return
     * @throws ServiceException
     * @Description : Save Labour
     */
    public JSONObject saveLabour(HashMap<String, Object> requestParams) throws ServiceException {
        String labourId = "";
        JSONObject jSONObject = new JSONObject();
        KwlReturnObject result = null;
        boolean isEdit = false;
        String msg = "";
        String companyId = "";
        try {
            if (requestParams.containsKey("labourId")) {
                labourId = (String) requestParams.get("labourId");
            }
            if (requestParams.containsKey("company")) {
                companyId = (String) requestParams.get("company");
            }
            HttpServletRequest request = (HttpServletRequest) requestParams.get("request");
            String auditAction="";
            String action="";
            if (StringUtil.isNullOrEmpty(labourId)) {
                /*
                 Create New Case
                 */
                auditAction=AuditAction.LABOUR_CREATED;
                action="added";
                requestParams.put("createdon", new Date());
                result = accLabourDAO.addLabour(requestParams);
                msg = messageSource.getMessage("acc.mrp.labour.save", null, RequestContextUtils.getLocale(request));
            } else {
                /*
                 Edit case 
                 */
                isEdit = true;
                auditAction=AuditAction.LABOUR_UPDATED;
                action="updated";
                requestParams.put("labourId", labourId);
                requestParams.put("updatedon", new Date());
                result = accLabourDAO.addLabour(requestParams);
                msg = messageSource.getMessage("acc.mrp.labour.update", null, RequestContextUtils.getLocale(request));
            }
            Labour labour = (Labour) result.getEntityList().get(0);

            /**
             * Save Labour Work Centre Mapping
             */
            String workCentre = "";
            if (requestParams.containsKey("workcentre")) {
                workCentre = (String) requestParams.get("workcentre");
            }
            HashMap<String, Object> mappingParams = new HashMap<String, Object>();
            mappingParams.put("companyId", companyId);
            mappingParams.put("labourId", labour.getID());
            if (!StringUtil.isNullOrEmpty(workCentre)) {
                mappingParams.put("workcentreId", workCentre);
            }
            if (isEdit) {
                Set<LabourWorkCentreMapping> labourWorkCentreMapping = labour.getLabourWorkCentreMappings();
                for (LabourWorkCentreMapping workCentreMapping : labourWorkCentreMapping) {
                    mappingParams.put("labourWCMappingId", workCentreMapping.getID());
                    workCentre = workCentreMapping.getID();
                }
            }
            if(!StringUtil.isNullOrEmpty(workCentre)){
                Set<LabourWorkCentreMapping> labourWorkCentreMapping = accLabourDAO.getLabourWCMapping(mappingParams);
            
                accLabourDAO.savelabourWorkCentrmapping(labour,labourWorkCentreMapping); 
            }
            /*
             Save Labour Skill Mapping
             */
            mappingParams = new HashMap<String, Object>();
            mappingParams.put("companyid", companyId);
            mappingParams.put("labourId", labour);
            accLabourDAO.deleteLabourSkillMapping(mappingParams);
            if (requestParams.containsKey("keyskill")) {
//                String[] keyskill = requestParams.get("keyskill").toString().split(",");
                    mappingParams.put("keyskill", requestParams.get("keyskill"));
                    result = accLabourDAO.saveLabourSkillMapping(mappingParams);
//                for (int i = 0; i < keyskill.length; i++) {
//                    String keyskillId = keyskill[i];
//                    mappingParams.put("keyskillId", keyskillId);
//                }
//            }
            }
            /*
             Save Custom Field Data
             */
            String customfield = (String) requestParams.get("customfield");
            if (!StringUtil.isNullOrEmpty(customfield)) {

                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_Labour_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_LabourId);
                customrequestParams.put("modulerecid", labour.getID());
                customrequestParams.put("moduleid", Constants.Labour_Master);
                customrequestParams.put("companyid", companyId);
                customrequestParams.put("customdataclasspath", Constants.Acc_Labour_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    requestParams.put("acclabourcustomdataref", labour.getID());
                    requestParams.put("labourId", labour.getID());
                    KwlReturnObject accresult = accLabourDAO.addLabour(requestParams);
                }

            }

            msg += "<b>" + labour.getEmpcode()+"</b>" ;
            
            auditTrailObj.insertAuditLog(auditAction, "User " + sessionHandlerImpl.getUserFullName(request) + " has "+action+" labour " +"<b>" + labour.getFullName() + " ( "+ labour.getEmpcode() +" )</b>" , request, labour.getID(), "");
            jSONObject.put("msg", msg);
            jSONObject.put("labour", labour);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccLabourServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jSONObject;
    }

    public JSONObject saveLabourCost(Map<String, Object> requestParams) throws ServiceException {
        String resourceCostId = "";
        JSONObject jSONObject = new JSONObject();
        KwlReturnObject result = null;
        boolean isEdit = false;
        String msg = "";
        String audimsg="";
        String companyId = "";        
        try {
            if (requestParams.containsKey("resourceCostId")) {
                resourceCostId = (String) requestParams.get("resourceCostId");
            }
            if (requestParams.containsKey("company")) {
                companyId = (String) requestParams.get("company");
            }
            HttpServletRequest request = (HttpServletRequest) requestParams.get("request");
            if (StringUtil.isNullOrEmpty(resourceCostId)) {
                /*
                 Create New Case
                 */
                result = accLabourDAO.saveResourceCost(requestParams);
                audimsg = messageSource.getMessage("acc.mrp.labour.costsave", null, RequestContextUtils.getLocale(request));
                msg = messageSource.getMessage("acc.mrp.labour.costsaveSuccess", null, RequestContextUtils.getLocale(request));
            } else {
                /*
                 Edit case 
                 */
                isEdit = true;
                requestParams.put("resourceCostId", resourceCostId);
                result = accLabourDAO.saveResourceCost(requestParams);
                audimsg = messageSource.getMessage("acc.mrp.labour.costupdate", null, RequestContextUtils.getLocale(request));
                msg = messageSource.getMessage("acc.mrp.labour.costupdateSuccess", null, RequestContextUtils.getLocale(request));
            }
            ResourceCost resourceCost = (ResourceCost) result.getEntityList().get(0);

            audimsg=audimsg+ "<b>" + resourceCost.getLabour().getEmpcode();
            auditTrailObj.insertAuditLog(AuditAction.Labour_MANAGEMENT, audimsg, request, resourceCost.getID());
            jSONObject.put("msg", msg);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jSONObject;
    }
    @Override
    public JSONObject deleteLabours(HashMap<String, Object> requestParams) throws ServiceException {
        JSONObject jobj=new JSONObject();
        try {
            Boolean isPerm = (Boolean) requestParams.get("isPerm");
            if (isPerm) {
                jobj=deleteLaboursPermanently(requestParams);
            } else {
                jobj=deletelaboursTemporarily(requestParams);
            }
            return jobj;
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        
    }
    
    public JSONObject deletelaboursTemporarily(HashMap<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        Locale requestcontextutilsobj = null;
        try {
            HttpServletRequest request = (HttpServletRequest) requestParams.get("request");
            JSONArray jSONArray = new JSONArray(request.getParameter("data"));
            String msg = "";
            Labour labourObj = null;
            KwlReturnObject result = null;
            KwlReturnObject returnResult = null;
            StringBuffer labourCodeString=new StringBuffer();
            boolean islabourInUse=false, isPerm=false;
            if (requestParams.containsKey("isPerm") && requestParams.get("isPerm")!=null) {
                isPerm = (Boolean) requestParams.get("isPerm");
            }        
            for (int i = 0; i < jSONArray.length(); i++) { 
               JSONObject jobject = jSONArray.getJSONObject(i);
               String labourId = jobject.optString("billid");
               int usageCout=0;
                
                result = accountingHandlerDAOobj.getObject(Labour.class.getName(), labourId);
                labourObj = (Labour) result.getEntityList().get(0);
                 
               if (!StringUtil.isNullOrEmpty(labourId)) {
                    HashMap<String, Object> dataMap = new HashMap<>();
                    dataMap.put("labourId", labourId);
                    
                    // Checking For Work Centres
                        KwlReturnObject resultWC = accLabourDAO.getWCforLabour(dataMap);
                        int WCcount = resultWC.getRecordTotalCount();
                        if (WCcount > 0) {
                            usageCout++;
                        }
 
                   // CHecking For Work Orders
                        KwlReturnObject resultWO = accLabourDAO.getWOforLabour(dataMap);
                        int WOcount = resultWO.getRecordTotalCount();
                        if (WOcount > 0) {
                            usageCout++;                          
                        }
                        
                    // CHecking For Routing Templates
                        KwlReturnObject resultRT = accLabourDAO.getRTforLabour(dataMap);
                        int RTcount = resultRT.getRecordTotalCount();
                        if (RTcount > 0) {
                            usageCout++;
                        }
                }
                if (usageCout > 0) {
                        islabourInUse=true;
                        labourCodeString.append(" ");
                        labourCodeString.append(labourObj.getEmpcode());
                        labourCodeString.append(",");
                        continue;
                    } 
                if (!isPerm) { 
                        //set delete flag true  of labour obj to delete labour temporarily
                        labourObj.setDeleteflag(true);
                       // msg = messageSource.getMessage("acc.mrp.labour.delete", null, RequestContextUtils.getLocale(request));
                        auditTrailObj.insertAuditLog(AuditAction.LABOUR_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) +" has deleted labour <b>"+ labourObj.getFullName()+ " ( "+ labourObj.getEmpcode() +" )</b>", request, labourId, "");
                }    
         }
         if (islabourInUse) {
             /*If labour is tagged in work order, work centre and routing template  islabourInUse flag is true otherwise false */
                String labour = labourCodeString.toString();
                if (labour.length() > 0) {
                    labour = labour.substring(0, labour.length() - 1);
                }
                jobj.put("msg", messageSource.getMessage("acc.mrp.field.MappedLabourID", null, requestcontextutilsobj) + "<B>" + labour + "</B>" + " " + messageSource.getMessage("acc.field.cannnotdelete", null, requestcontextutilsobj)); 
            }     

        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    /**
     *
     * @param requestParams = contains labourid
     * @throws ServiceException
     * @desc : delete labours
     */
    public JSONObject deleteLaboursPermanently(HashMap<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        Locale requestcontextutilsobj = null;
        try {
            HttpServletRequest request = (HttpServletRequest) requestParams.get("request");
            JSONArray jSONArray = new JSONArray(request.getParameter("data"));
            JSONArray labouridsJarr = new JSONArray();
            JSONObject labourObj = new JSONObject();
            String userId = "";
            String companyId = "";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String msg = "";
            KwlReturnObject returnResult = null;
            StringBuffer labourCodeString=new StringBuffer();
            boolean islabourInUse=false, isPerm=false;
            if (requestParams.containsKey("isPerm") && requestParams.get("isPerm")!=null) {
                isPerm = (Boolean) requestParams.get("isPerm");
            }
            
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jobject = jSONArray.getJSONObject(i);
                String labourId = jobject.optString("billid");
                int usageCout=0;
                KwlReturnObject result = accountingHandlerDAOobj.getObject(Labour.class.getName(),labourId);
                Labour labour = (Labour) result.getEntityList().get(0);
                labourObj=new JSONObject();
                labourObj.put("resourceid", labourId);
                labouridsJarr.put(labourObj);
                requestParams.put("billId", labourId);
                requestParams.put("companyId", companyid);
                
                if (!StringUtil.isNullOrEmpty(labourId)) {
                    HashMap<String, Object> dataMap = new HashMap<>();
                    dataMap.put("labourId", labourId);
                    
                    // Checking For Work Centres
                        KwlReturnObject resultWC = accLabourDAO.getWCforLabour(dataMap);
                        int WCcount = resultWC.getRecordTotalCount();
                        if (WCcount > 0) {
                            usageCout++;
                        }
 
                   // CHecking For Work Orders
                        KwlReturnObject resultWO = accLabourDAO.getWOforLabour(dataMap);
                        int WOcount = resultWO.getRecordTotalCount();
                        if (WOcount > 0) {
                            usageCout++;                          
                        }
                        
                    // CHecking For Routing Templates
                        KwlReturnObject resultRT = accLabourDAO.getRTforLabour(dataMap);
                        int RTcount = resultRT.getRecordTotalCount();
                        if (RTcount > 0) {
                            usageCout++;
                        }
                }
                if (usageCout > 0) {
                        islabourInUse=true;
                        labourCodeString.append(" ");
                        labourCodeString.append(labour.getEmpcode());
                        labourCodeString.append(",");
                        continue;
                    }
                
                if (isPerm) {    // - If Permanent delete then execute below lines.
                      returnResult= accLabourDAO.deleteLabour(requestParams);
//                      jobj.put("msg",messageSource.getMessage("acc.mrp.labour.delete", null, requestcontextutilsobj));
                auditTrailObj.insertAuditLog(AuditAction.LABOUR_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) +" has deleted labour <b>"+ labour.getFullName()+ " ( "+ labour.getEmpcode() +" )</b> Permanently", request, labourId, "");
            }
            }
            
             if (islabourInUse) {
                  String labour = labourCodeString.toString();
                if (labour.length() > 0) {
                    labour = labour.substring(0, labour.length() - 1);
                }
                jobj.put("msg", messageSource.getMessage("acc.mrp.field.MappedLabourID", null, requestcontextutilsobj) + "<B>" + labour + "</B>" + " " + messageSource.getMessage("acc.field.cannnotdelete", null, requestcontextutilsobj));
                
            }           
            else{
            /*
             When labours are deleted from ERP , it should get deleted from PM too.  
             */
            if (requestParams.containsKey("companyId")) {
                companyId = (String) requestParams.get("companyId");
            }
            if (requestParams.containsKey("userId")) {
                userId = (String) requestParams.get("userId");
            }
            String accRestURL = URLUtil.buildRestURL("pmURL");

            JSONObject inputData = new JSONObject();
            inputData.put("inputdata", labouridsJarr);
            inputData.put("companyid", companyId);
            inputData.put("userid", userId);
            String endpoint = accRestURL + "master/deletereosurces";
            JSONObject resObj = apiCallHandlerService.restPostMethod(endpoint, inputData.toString());
         }
            
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } 
        return jobj;
    }
    public void deleteLaboursCost(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            HttpServletRequest request = (HttpServletRequest) requestParams.get("request");
            JSONArray jSONArray = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String msg = "",empCode = "";
            String resourceid="";
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jobj = jSONArray.getJSONObject(i);
                String costid = jobj.optString("resourcecostid");
                requestParams.put("resourcecostid", costid);
                requestParams.put("companyId", companyid);
                
                KwlReturnObject result = accountingHandlerDAOobj.getObject(ResourceCost.class.getName(),costid);
                ResourceCost resourceCost = (ResourceCost) result.getEntityList().get(0);
                resourceid=resourceCost.getLabour().getID();
                empCode += "<b>" + resourceCost.getLabour().getEmpcode()+"</b>";
                accLabourDAO.deleteLabourCost(requestParams);
                
                requestParams.put("labourId", resourceid);
                requestParams.put("isFromDeleteResourceCost", true);
                syncLabourCost(requestParams);
                msg = messageSource.getMessage("acc.mrp.labour.ResourceCost.deletecost.audit", null, RequestContextUtils.getLocale(request));
                auditTrailObj.insertAuditLog(AuditAction.Labour_MANAGEMENT, msg+empCode, request, costid);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    /**
     *
     * @param request
     * @throws ServiceException
     * @Description : Method to return column model with data for labours report
     */
    public void getColumnModelAndRecordDataForLabours(HashMap<String, Object> requestParams, JSONObject object) throws ServiceException {
        HttpServletRequest request = (HttpServletRequest) requestParams.get("request");
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        String storeRec = "";
        String start = "", limit = "";
        if (requestParams.containsKey("start")) {
            start = requestParams.get("start").toString();
        }
        if (requestParams.containsKey("limit")) {
            limit = requestParams.get("limit").toString();
        }
        try {
            storeRec = "billid,empid,empname,department,keyskill,shifttiming,taskassigned,sequenceformatid,workcenter,deleted";
            String[] recArr = storeRec.split(",");
            // Get those fields in record for whome, no special properties present like type, defVal, mapping etc.
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }

            // Gel column model - 
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.field.labourname", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "empname");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.labour.empid", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "empid");
            jobjTemp.put("width", 150);
            jobjTemp.put("align", "center");
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);


            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.labour.department", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "department");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.masterConfig.54", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "keyskill");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.labour.shifttiming", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "shifttiming");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.labour.taskassigned", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "taskassigned");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.resourceanalysis.columns.workcenter", null, RequestContextUtils.getLocale(request))); // "Work Centre ID",
            jobjTemp.put("dataIndex", "workcenter");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth",150);
            jarrColumns.put(jobjTemp);
            // get above data along with extra data
            String companyid = sessionHandlerImpl.getCompanyid(request);
            /*
             Add Custom Fields in Column Model
             */
            requestParams.put("companyId", companyid);
            requestParams.put("reportId", Constants.Labour_Master);
            putCustomColumnForLabour(jarrColumns, jarrRecords, requestParams);
            /*
             Get Data from DB 
             */
            KwlReturnObject result = accLabourDAO.getLabour(requestParams);
            List<Labour> list = result.getEntityList();
            int count = result.getRecordTotalCount();
            for (Labour labour : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("billid", labour.getID());
                jSONObject.put(Constants.SEQUENCEFORMATID, labour.getSeqformat() == null ? "" : labour.getSeqformat().getID());
                jSONObject.put("empid", labour.getEmpcode());
                jSONObject.put("empname", labour.getFname() + " " + labour.getLname());
                String deptId = labour.getDepartment() != null ? labour.getDepartment() : "";
                if (!StringUtil.isNullOrEmpty(deptId)) {
                    KwlReturnObject resultPayMethod = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), deptId);
                    MasterItem masterItem = (MasterItem) resultPayMethod.getEntityList().get(0);
                    jSONObject.put("department", masterItem != null ? masterItem.getValue() : "");
                }
                Set<LabourSkillMapping> labourSkillMappings = null;
                labourSkillMappings = labour.getLabourSkillMappings();
                String skill = "", skillId = "";
                if (labourSkillMappings != null) {
                    for (LabourSkillMapping labourSkillMapping : labourSkillMappings) {
                        skill = skill + labourSkillMapping.getSkill().getValue() + ",";
                        skillId = skillId + labourSkillMapping.getSkill().getID() + ",";

                    }
                    if (!StringUtil.isNullOrEmpty(skill)) {
                        skill = skill.substring(0, skill.length() - 1);
                    }
                    if (!StringUtil.isNullOrEmpty(skillId)) {
                        skillId = skillId.substring(0, skillId.length() - 1);
                    }
                }
                Set<LabourWorkCentreMapping> labourWorkCentreMappings = null;
                labourWorkCentreMappings = labour.getLabourWorkCentreMappings();
                StringBuilder wNameSb=new StringBuilder();
                StringBuilder wIDSb=new StringBuilder();
                
                if (labourWorkCentreMappings != null && labourWorkCentreMappings.size() > 0) {
                    for (LabourWorkCentreMapping labourWorkCentreMapping : labourWorkCentreMappings) {
                        if (labourWorkCentreMapping.getWorkCentre() != null) {
                             //Create comma seperated string of workcentres
                            wNameSb.append(labourWorkCentreMapping.getWorkCentre().getName());
                            wNameSb.append(",");
                            wIDSb.append(labourWorkCentreMapping.getWorkCentre().getID());
                            wIDSb.append(",");
                            
                        }
                    }
                }
                
                jSONObject.put("workcenter",wNameSb.length() > 1 ? wNameSb.substring(0, wNameSb.length()-1) : "");
                jSONObject.put("workcenterid", wIDSb.length() > 1 ? wIDSb.substring(0, wIDSb.length()-1) : "");
                
                jSONObject.put("keyskill", skill);

                jSONObject.put("shifttiming", labour.getShifttiming());
                jSONObject.put("taskassigned", labour.getTaskassigned());
                 jSONObject.put("deleted", labour.isDeleteflag());

                /*
                 Add Global Custom data for document
                 */
                Map globalMap = new HashMap();
                globalMap.put("moduleId", Constants.Labour_Master);
                globalMap.put("companyId", companyid);
                globalMap.put("billid", labour.getID());
                globalMap.put(Constants.userdf, requestParams.get(Constants.userdf));
                putGlobalCustomDetailsForLabour(jSONObject, globalMap);
                dataJArr.put(jSONObject);
                
            }
            JSONArray pagedJson = new JSONArray();
            pagedJson = dataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            object.put("totalCount", dataJArr.length());
            object.put("columns", jarrColumns);
            object.put("coldata", pagedJson);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            jMeta.put("fields", jarrRecords);
            object.put("metaData", jMeta);
            boolean isExport = false;
            if (requestParams.containsKey("isExport")) {
                isExport = Boolean.parseBoolean(requestParams.get("isExport").toString());
            }
            if (isExport) {
                object.put("data", dataJArr);
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    /**
     *
     * @param jarrColumns = Column Model
     * @param jarrRecords = Record for store
     * @param requestParams
     * @Description : Add Column model for Custom Field
     * @throws ServiceException
     */
    public void putCustomColumnForLabour(JSONArray jarrColumns, JSONArray jarrRecords, HashMap<String, Object> requestParams) throws ServiceException {
        try {
            HashMap hashMap = new HashMap();
            KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(requestParams);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            List arrayList = new ArrayList();
            for (CustomizeReportMapping customizeReportMapping : customizeReportList) {
                String column = "Custom_" + customizeReportMapping.getDataIndex();
                if (!arrayList.contains(customizeReportMapping.getDataIndex())) {
                    JSONObject jobjTemp = new JSONObject();
                    jobjTemp.put("name", column);
                    jarrRecords.put(jobjTemp);
                    jobjTemp = new JSONObject();
                    jobjTemp.put("header", customizeReportMapping.getDataHeader());
                    jobjTemp.put("dataIndex", column);
                    jobjTemp.put("width", 150);
                    jobjTemp.put("pdfwidth", 150);
                    jobjTemp.put("custom", "true");
                    jarrColumns.put(jobjTemp);
                    arrayList.add(customizeReportMapping.getDataIndex());
                }
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    /**
     *
     * @param jSONObject
     * @param map
     * @throws ServiceException
     * @throws JSONException
     * @Description : Put Custom fields data in JSON to show in Report
     */
    public void putGlobalCustomDetailsForLabour(JSONObject jSONObject, Map<String, Object> map) throws ServiceException, JSONException {

        String companyId = "";
        int moduleid = 0;
        Labour labour = null;
        String labourId = "";
        if (map.containsKey("companyId")) {
            companyId = map.get("companyId").toString();
        }
        if (map.containsKey("moduleId")) {
            moduleid = Integer.parseInt(map.get("moduleId").toString());
        }
        if (map.containsKey("billid")) {
            labourId = map.get("billid").toString();
        }
        // ## Get Custom Field Data 
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, moduleid));
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
        Map<String, Object> variableMap = new HashMap<String, Object>();

        KwlReturnObject custumObjresult = null;
        custumObjresult = accountingHandlerDAOobj.getObject(LabourCustomData.class.getName(), labourId);
        replaceFieldMap = new HashMap<String, String>();
        if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
            LabourCustomData jeDetailCustom = (LabourCustomData) custumObjresult.getEntityList().get(0);
            if (jeDetailCustom != null) {
                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                JSONObject params = new JSONObject();
                params.put("companyid", companyId);
                params.put("isExport", true);
                params.put(Constants.userdf, map.get(Constants.userdf));
                fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
            }
        }
    }

    /**
     *
     * @param request
     * @throws ServiceException
     * @Description : Method to return column model with data for assign task
     * report
     */
    public void getColumnModelForAssignTaskList(HashMap<String, Object> requestParams, JSONObject object) throws ServiceException {
        HttpServletRequest request = (HttpServletRequest) requestParams.get("request");
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        String storeRec = "";
        String start = "", limit = "";
        if (requestParams.containsKey("start")) {
            start = requestParams.get("start").toString();
        }
        if (requestParams.containsKey("limit")) {
            limit = requestParams.get("limit").toString();
        }
        try {
            storeRec = "empname,department,keyskill,completedtask";
            String[] recArr = storeRec.split(",");
            // Get those fields in record for whome, no special properties present like type, defVal, mapping etc.
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }

            // Gel column model - 
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.masterConfig.chequeLayoutSetup.name", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "empname");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.labour.skills", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "keyskill");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.labour.assigntask.taskcompleted", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "completedtask");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.labour.assigntask.userdepartment", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "department");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            // get above data along with extra data
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyId", companyid);
            KwlReturnObject result = accLabourDAO.getLabour(requestParams);
            List<Labour> list = result.getEntityList();
            int count = result.getRecordTotalCount();
            for (Labour labour : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("empid", labour.getEmpcode());
                jSONObject.put("empname", labour.getFname() + " " + labour.getLname());
                String deptId = labour.getDepartment() != null ? labour.getDepartment() : "";
                if (!StringUtil.isNullOrEmpty(deptId)) {
                    KwlReturnObject resultPayMethod = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), deptId);
                    MasterItem masterItem = (MasterItem) resultPayMethod.getEntityList().get(0);
                    jSONObject.put("department", masterItem != null ? masterItem.getValue() : "");
                }
//                jSONObject.put("keyskill", labour.getKeyskill() != null ? labour.getKeyskill().getID() : "");
                dataJArr.put(jSONObject);

            }
            JSONArray pagedJson = new JSONArray();
            pagedJson = dataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            object.put("totalCount", dataJArr.length());
            object.put("columns", jarrColumns);
            object.put("coldata", pagedJson);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            jMeta.put("fields", jarrRecords);
            object.put("metaData", jMeta);

        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    /**
     *
     * @param request
     * @throws ServiceException
     * @Description : Method to return column model with data for resource list
     * report
     */
    public void getColumnModelForResourceList(HashMap<String, Object> requestParams, JSONObject object) throws ServiceException {
        HttpServletRequest request = (HttpServletRequest) requestParams.get("request");
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        String storeRec = "";
        String start = "", limit = "";
        if (requestParams.containsKey("start")) {
            start = requestParams.get("start").toString();
        }
        if (requestParams.containsKey("limit")) {
            limit = requestParams.get("limit").toString();
        }
        try {
            storeRec = "empname,billid,empid";
            String[] recArr = storeRec.split(",");
            // Get those fields in record for whome, no special properties present like type, defVal, mapping etc.
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }
            // Gel column model - 

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.field.labourname", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "empname");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.labour.empid", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "empid");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            // get above data along with extra data
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyId", companyid);
            KwlReturnObject result = accLabourDAO.getLabour(requestParams);
            List<Labour> list = result.getEntityList();
            int count = result.getRecordTotalCount();
            for (Labour labour : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("empname", labour.getFname() + " " + labour.getLname());
                jSONObject.put("billid", labour.getID());
                jSONObject.put("empid", labour.getEmpcode());
                dataJArr.put(jSONObject);

            }
            JSONArray pagedJson = new JSONArray();
            pagedJson = dataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            object.put("totalCount", dataJArr.length());
            object.put("columns", jarrColumns);
            object.put("coldata", pagedJson);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            jMeta.put("fields", jarrRecords);
            object.put("metaData", jMeta);

        } catch (JSONException | NoSuchMessageException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public void getResourceCostList(Map<String, Object> requestParams, JSONObject object) throws ServiceException {
        HttpServletRequest request = (HttpServletRequest) requestParams.get("request");
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        String LabourId = "";
        String storeRec = "";
        String start = "", limit = "";
        DateFormat userdf = null;
        if (requestParams.containsKey("start")) {
//            start = requestParams.get("start").toString();
        }
        if (requestParams.containsKey("limit")) {
//            limit = requestParams.get("limit").toString();
        }
        if (requestParams.containsKey("LabourId")) {
            LabourId = requestParams.get("LabourId").toString();
        }
        if (requestParams.containsKey(Constants.userdf)) {
            userdf = (DateFormat) requestParams.get(Constants.userdf);
        }
        try {
            storeRec = "resourcecost,effectivedate,resourcecostid";
            String[] recArr = storeRec.split(",");
            // Get those fields in record for whome, no special properties present like type, defVal, mapping etc.
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }
            // Gel column model - 

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.labour.resourcecost.cost", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "resourcecost");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.labour.resourcecost.effectivedate", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "effectivedate");
            jobjTemp.put("align", "center");
//            jobjTemp.put("type", "date");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            // get above data along with extra data
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("labourId", LabourId);
            KwlReturnObject result = accLabourDAO.getResourceCostSQL(requestParams);
            List<Object[]> list = result.getEntityList();
            int count = result.getRecordTotalCount();
            for (Object[] resourceCost : list) {
               
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("resourcecost", resourceCost[2]);
                jSONObject.put("resourcecostid", resourceCost[0]);
                jSONObject.put("effectivedate", userdf != null ? userdf.format(resourceCost[1]) : resourceCost[1]);
                dataJArr.put(jSONObject);

            }
            JSONArray pagedJson = new JSONArray();
            pagedJson = dataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            object.put("totalCount", dataJArr.length());
            object.put("columns", jarrColumns);
            object.put("coldata", pagedJson);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            jMeta.put("fields", jarrRecords);
            object.put("metaData", jMeta);

        } catch (JSONException | NoSuchMessageException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    /**
     *
     * @param request
     * @throws ServiceException
     * @Description : Method to return column model with data for resolve
     * conflict report
     */
    public void getResolveConflictResourcesColumnModel(HashMap<String, Object> requestParams, JSONObject object) throws ServiceException {
        HttpServletRequest request = (HttpServletRequest) requestParams.get("request");
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        String storeRec = "";
        String start = "", limit = "";
        if (requestParams.containsKey("start")) {
//            start = requestParams.get("start").toString();
        }
        if (requestParams.containsKey("limit")) {
//            limit = requestParams.get("limit").toString();
        }
        try {
            String StoreRec = "id, empname, resourcetype";
            String[] recArr = StoreRec.split(",");
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "");
            jobjTemp.put("dataIndex", "id");
            jobjTemp.put("hidden", true);
            jobjTemp.put("hideable", false);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.resourceanalysis.columns.resourcename", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "empname");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 100);
            jobjTemp.put("pdfwidth", 100);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.resolveconflict.columns.resourcetype", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "resourcetype");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 100);
            jobjTemp.put("pdfwidth", 100);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            // get above data along with extra data
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyId", companyid);
            KwlReturnObject result = accLabourDAO.getLabour(requestParams);
            List<Labour> list = result.getEntityList();
            int count = result.getRecordTotalCount();
            for (Labour labour : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("empname", labour.getFname() + " " + labour.getLname());
                jSONObject.put("resourcetype", "Labour");
                dataJArr.put(jSONObject);

            }
            JSONArray pagedJson = new JSONArray();
            pagedJson = dataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            object.put("totalCount", dataJArr.length());
            object.put("columns", jarrColumns);
            object.put("coldata", pagedJson);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            jMeta.put("fields", jarrRecords);
            object.put("metaData", jMeta);

        } catch (JSONException | NoSuchMessageException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    /**
     *
     * @param request
     * @throws ServiceException
     * @Description : Method to return data for labour
     */
    public JSONObject getSingleLabourToLoad(HashMap<String, Object> requestParams) throws ServiceException {
        JSONObject jSONObject = new JSONObject();
        KwlReturnObject result = accLabourDAO.getLabour(requestParams);
        List<Labour> list = result.getEntityList();
        for (Labour labour : list) {
            try {
                jSONObject.put("empcode", labour.getEmpcode());
                jSONObject.put("fname", labour.getFname());
                jSONObject.put("mname", labour.getMname());
                jSONObject.put("lname", labour.getLname());
                jSONObject.put("fullname", labour.getFname() + " " + labour.getMname() + " " + labour.getLname());
                jSONObject.put("dob", labour.getDob());
                jSONObject.put("age", labour.getAge());
                jSONObject.put("gender", labour.getGender());
                jSONObject.put("maritalstatus", labour.getMaritalstatus());
                jSONObject.put("bgroup", labour.getBgroup());
                jSONObject.put("nationality", labour.getNationality());
                jSONObject.put("countryorigin", labour.getCountryorigin());
                jSONObject.put("department", labour.getDepartment());
                String deptId = labour.getDepartment() != null ? labour.getDepartment() : "";
                if (!StringUtil.isNullOrEmpty(deptId)) {
                    KwlReturnObject resultPayMethod = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), deptId);
                    MasterItem masterItem = (MasterItem) resultPayMethod.getEntityList().get(0);
                    jSONObject.put("departmentname", masterItem != null ? masterItem.getValue() : "");
                }
//                jSONObject.put("keyskill", labour.getKeyskill() != null ? labour.getKeyskill().getID() : "");
//                jSONObject.put("keyskillname", labour.getKeyskill() != null ? labour.getKeyskill().getValue() : "");
                jSONObject.put("shifttiming", labour.getShifttiming());
                jSONObject.put("taskassigned", labour.getTaskassigned());
                jSONObject.put("paymentmethod", labour.getPaymentmethod() != null ? labour.getPaymentmethod().getID() : "");
                jSONObject.put("paymentmethodname", labour.getPaymentmethod() != null ? labour.getPaymentmethod().getMethodName() : "");
                jSONObject.put("dlicenseno", labour.getDlicenseno());
                jSONObject.put("passportno", labour.getPassportno());
                jSONObject.put("expirydatepassport", labour.getExpirydatepassport());
                jSONObject.put("paycycle", labour.getPaycycle());
                jSONObject.put("residentstatus", labour.getResidentstatus());
                jSONObject.put("prdate", labour.getPrdate());
                jSONObject.put("race", labour.getRace());
                jSONObject.put("religion", labour.getReligion());
                jSONObject.put("bankac", labour.getBankac());
                jSONObject.put("bankaname", labour.getBankaname());
                jSONObject.put("accountname", labour.getAccountname());
                jSONObject.put("accountnumber", labour.getAccountnumber());
                jSONObject.put("banknumber", labour.getBanknumber());
                jSONObject.put("branchnumber", labour.getBranchnumber());
                jSONObject.put("bankbranch", labour.getBankbranch());
                jSONObject.put(Constants.SEQUENCEFORMATID, labour.getSeqformat() == null ? "" : labour.getSeqformat().getID());
                jSONObject.put("sequenceformatvalue", labour.getSeqformat() == null ? "" : labour.getSeqformat().getName());
                Set<LabourWorkCentreMapping> labourWorkCentreMapping = labour.getLabourWorkCentreMappings();
                for (LabourWorkCentreMapping workCentreMapping : labourWorkCentreMapping) {
                    WorkCentre workCentre = (WorkCentre) workCentreMapping.getWorkCentre();
                    if (workCentre != null) {
                        jSONObject.put("wcid", workCentre.getID());
                        jSONObject.put("wcname", workCentre.getName());
                    } else {
                        jSONObject.put("wcid", "");
                        jSONObject.put("wcname", "");
                    }
                }
                Set<LabourSkillMapping> labourSkillMappings = null;
                labourSkillMappings = labour.getLabourSkillMappings();
                String skill = "", skillId = "";
                if (labourSkillMappings != null) {
                    for (LabourSkillMapping labourSkillMapping : labourSkillMappings) {
                        skill = skill + labourSkillMapping.getSkill().getValue() + ",";
                        skillId = skillId + labourSkillMapping.getSkill().getID() + ",";

                    }
                    if (!StringUtil.isNullOrEmpty(skill)) {
                        skill = skill.substring(0, skill.length() - 1);
                    }
                    if (!StringUtil.isNullOrEmpty(skillId)) {
                        skillId = skillId.substring(0, skillId.length() - 1);
                    }
                }
                jSONObject.put("keyskill", skillId);
                jSONObject.put("keyskillname", skill);
            } catch (JSONException ex) {
                throw ServiceException.FAILURE(ex.getMessage(), ex);
            }
        }
        return jSONObject;
    }

    @Override
    public JSONArray getLabourCombo(Map<String, Object> map) throws ServiceException {
        JSONArray jArr = new JSONArray();
        JSONObject jSONObject = new JSONObject();
        KwlReturnObject result = accLabourDAO.getLabourCombo(map);
        List list = result.getEntityList();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            try {
                jSONObject = new JSONObject();
                Object obj[] = (Object[]) itr.next();
                jSONObject.put("id", (String) obj[0]);
                jSONObject.put("name", (String) obj[1] +" "+ (String) obj[2]); //ERP-23558
                jSONObject.put("empcode", (String) obj[3]);
                jArr.put(jSONObject);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                throw ServiceException.FAILURE(ex.getMessage(), ex);
            }
        }
        return jArr;
    }

    public JSONObject syncLabour(Map<String, Object> requestParams) throws ServiceException {
        JSONObject resObj=new JSONObject();
        try {
            JSONObject jSONObject = new JSONObject();
            
            JSONArray jSONArray = new JSONArray();
            if (requestParams.containsKey("isAutoSync")) {
                boolean isAutoSync = (boolean) requestParams.get("isAutoSync");
                Labour labour = (Labour) requestParams.get("labour");
                if (isAutoSync) {
                    jSONObject.put("id", labour.getID());
                    jSONObject.put("empcode", labour.getEmpcode());
                    jSONObject.put("fname", labour.getFname());
                    jSONObject.put("mname", labour.getMname());
                    jSONObject.put("lname", labour.getLname());
                    jSONObject.put("name", labour.getFname() + " " + labour.getMname() + " " + labour.getLname());
                    Set<LabourSkillMapping> labourSkillMappings = null;
                    labourSkillMappings = labour.getLabourSkillMappings();
                    String skill = "", skillId = "";
                    if (labourSkillMappings != null) {
                        for (LabourSkillMapping labourSkillMapping : labourSkillMappings) {
                            skill = skill + labourSkillMapping.getSkill().getValue() + ",";
                            skillId = skillId + labourSkillMapping.getSkill().getID() + ",";

                        }
                        if (!StringUtil.isNullOrEmpty(skill)) {
                            skill = skill.substring(0, skill.length() - 1);
                        }
                        if (!StringUtil.isNullOrEmpty(skillId)) {
                            skillId = skillId.substring(0, skillId.length() - 1);
                        }
                    }
                    jSONObject.put("keyskill", skillId);
//                jSONObject.put("keyskillname", skill);
//                    jSONObject.put("keyskill", labour.getKeyskill() != null ? labour.getKeyskill().getID() : "");
                    jSONArray.put(jSONObject);
                }
            } else {
                KwlReturnObject result = accLabourDAO.getLabour(requestParams);
                List<Labour> list = result.getEntityList();
                for (Labour labour : list) {
                    jSONObject.put("id", labour.getID());
                    jSONObject.put("empcode", labour.getEmpcode());
                    jSONObject.put("fname", labour.getFname());
                    jSONObject.put("mname", labour.getMname());
                    jSONObject.put("lname", labour.getLname());
                    jSONObject.put("name", labour.getFname()+" "+labour.getMname()+" "+labour.getLname());
                    Set<LabourSkillMapping> labourSkillMappings = null;
                    labourSkillMappings = labour.getLabourSkillMappings();
                    String skill = "", skillId = "";
                    if (labourSkillMappings != null) {
                        for (LabourSkillMapping labourSkillMapping : labourSkillMappings) {
                            skill = skill + labourSkillMapping.getSkill().getValue() + ",";
                            skillId = skillId + labourSkillMapping.getSkill().getID() + ",";
                        }
                        if (!StringUtil.isNullOrEmpty(skill)) {
                            skill = skill.substring(0, skill.length() - 1);
                        }
                        if (!StringUtil.isNullOrEmpty(skillId)) {
                            skillId = skillId.substring(0, skillId.length() - 1);
                        }
                    }
                    jSONObject.put("keyskill", skillId);
                    jSONArray.put(jSONObject);
                }
            }

            String userId = "";
            String companyId = "";
            if (requestParams.containsKey("companyId")) {
                companyId = (String) requestParams.get("companyId");
            }
            if (requestParams.containsKey("userId")) {
                userId = (String) requestParams.get("userId");
            }
            String accRestURL = URLUtil.buildRestURL("pmURL");

            JSONObject inputData = new JSONObject();
            inputData.put("inputdata", jSONArray);
            inputData.put("companyid", companyId);
            inputData.put("userid", userId);
            String endpoint = accRestURL + "master/labours";
             resObj = apiCallHandlerService.restPostMethod(endpoint, inputData.toString());
        } catch (JSONException ex) {
            Logger.getLogger(AccLabourServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccLabourServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resObj;
    }

    public JSONObject updateLabourFlag(Map<String, Object> map) throws ServiceException {
        try {
            KwlReturnObject result = accLabourDAO.updateLabourFlag(map);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new JSONObject();
    }

    @Override
    public JSONObject syncLabourCost(Map<String, Object> requestParams) throws ServiceException {
        JSONObject resObj = new JSONObject();
        try {
            JSONArray dataJArr = new JSONArray();
            Boolean isFromDeleteResourceCost=false;
            if (requestParams.containsKey("isFromDeleteResourceCost")) {
                isFromDeleteResourceCost=(Boolean)requestParams.get("isFromDeleteResourceCost");
            }
            if (requestParams.containsKey("labourId")) {
                requestParams.put("maxdate", true);
            }
            KwlReturnObject result = accLabourDAO.getResourceCostSQL(requestParams);
            List<Object[]> list = result.getEntityList();
            int count = result.getRecordTotalCount();
            
            for (Object[] resourceCost : list) {
                JSONObject jSONObject = new JSONObject();
                if (isFromDeleteResourceCost) {
                    jSONObject.put("cost", 0);
                } else {
                    jSONObject.put("cost", resourceCost[2]);
                }
                jSONObject.put("resourcecostid", resourceCost[0]);
                jSONObject.put("effectivedate", resourceCost[1]);
                jSONObject.put("company", resourceCost[3]);
                jSONObject.put("resourceid", resourceCost[4]);
                dataJArr.put(jSONObject);
            }
            String accRestURL = URLUtil.buildRestURL("pmURL");
            JSONObject userData = new JSONObject();
            String userId = "";
            String companyId = "";
            String cdomain="";
            if (requestParams.containsKey("companyId")) {
                companyId = (String) requestParams.get("companyId");
            }
            if (requestParams.containsKey("cdomain")) {
                cdomain = (String) requestParams.get("cdomain");
            }
            if (requestParams.containsKey("userId")) {
                userId = (String) requestParams.get("userId");
            }
            userData.put("iscommit", true);
            userData.put("userid", userId);
            userData.put("companyid", companyId);
            userData.put("cdomain", cdomain);
            userData.put("inputdata", dataJArr);
            String endpoint = accRestURL + "transaction/resourcescost?request=" + userData.toString();
            resObj = apiCallHandlerService.restPostMethod(endpoint, userData.toString());
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return resObj;
    }
    
    @Override
    public JSONObject exportLabourAllocationReportXlsx(Map<String, Object> requestParams) throws ServiceException {
        JSONObject retObj = new JSONObject();
        String msg = "Some error occured while processing your request";
        boolean success = false;
        try {
            JSONObject jObj = new JSONObject();
             jObj.put("companyid", requestParams.get("companyid"));
             jObj.put("userid", requestParams.get("userid"));
             jObj.put("dateformat", requestParams.get("dateformat"));
             jObj.put("fromdate", requestParams.get("fromdate"));
             jObj.put("todate", requestParams.get("todate"));
             jObj.put("resourceids", requestParams.get("resourceids"));
             
             String accRestURL = URLUtil.buildRestURL("pmURL");
             String endpoint = accRestURL + "transaction/resourceusage";
             ByteArrayOutputStream res=apiCallHandlerService.restGetMethodForFile(endpoint, jObj.toString());
             
             if(res != null){
                 String fileName = "ResourceAllocation";
                 if(requestParams.containsKey("filename")){
                     fileName = (String)requestParams.get("filename");
                 }
//                 ByteArrayOutputStream os = (ByteArrayOutputStream) res;
                 HttpServletResponse response = (HttpServletResponse) requestParams.get("response");
                 response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".xlsx\"");
                 response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                 response.setContentLength(res.size());
                 response.getOutputStream().write(res.toByteArray());
                 response.getOutputStream().flush();
                 
                 msg = "Data fetched successfully";
                 success = true;
             }

             
        } catch (MalformedURLException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccLabourServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | JSONException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccLabourServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                retObj.put("msg", msg);
                retObj.put("success", success);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return retObj;
    }

}
