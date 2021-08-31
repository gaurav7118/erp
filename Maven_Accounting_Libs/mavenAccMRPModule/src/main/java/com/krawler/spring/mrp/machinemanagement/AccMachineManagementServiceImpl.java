/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.machinemanagement;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.CustomizeReportMapping;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.hql.accounting.AssetMaintenanceSchedulerObject;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.mrp.WorkOrder.AccWorkOrderServiceImpl;
import com.krawler.spring.mrp.WorkOrder.WorkOrder;
import com.krawler.spring.mrp.WorkOrder.WorkOrderDAO;
import com.krawler.spring.mrp.WorkOrder.WorkOrderMachineMapping;
import com.krawler.spring.mrp.routingmanagement.RoutingTemplate;
import com.krawler.spring.mrp.routingmanagement.RoutingTemplateMachineMapping;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AccMachineManagementServiceImpl implements AccMachineManagementServiceDAO {

    private MessageSource messageSource;
    private AccMachineManagementDAO accMachineManagementDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private WorkOrderDAO workOrderDAO;
    private APICallHandlerService apiCallHandlerService;
    private fieldDataManager fieldDataManagercntrl;
    private accAccountDAO accAccountDAOobj;

    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

    public AccMachineManagementDAO getAccMachineManagementDAOObj() {
        return accMachineManagementDAOObj;
    }

    public void setAccMachineManagementDAOObj(AccMachineManagementDAO accMachineManagementDAOObj) {
        this.accMachineManagementDAOObj = accMachineManagementDAOObj;
    }
    
     public AccountingHandlerDAO getAccountingHandlerDAOobj() {
        return accountingHandlerDAOobj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public exportMPXDAOImpl getExportDaoObj() {
        return exportDaoObj;
    }

    public void setExportDaoObj(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public WorkOrderDAO getWorkOrderDAO() {
        return workOrderDAO;
    }

    public void setWorkOrderDAO(WorkOrderDAO workOrderDAO) {
        this.workOrderDAO = workOrderDAO;
    }
    public APICallHandlerService getApiCallHandlerService() {
        return apiCallHandlerService;
    }

    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {
        this.apiCallHandlerService = apiCallHandlerService;
    }
    
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getMachineMasterDetails(Map<String, Object> requestParams) throws ServiceException {

        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        try {

            int count = 0;
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            
            dataJArr = createMachineMasterJSON(requestParams);
            count = dataJArr.length();
            commData.put("coldata", dataJArr);
            commData.put("totalCount", count);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);
            jobj.put("valid", true);
            jobj.put("data", commData);
        } catch (JSONException ex) {
            Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }

        return jobj;
    }
    
    @Override
    public void getColumnModelForMachineList(HashMap<String, Object> requestParams, JSONObject object) throws ServiceException, SessionExpiredException {
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
            storeRec = "empname,billid";
            String[] recArr = storeRec.split(",");
            // Get those fields in record for whome, no special properties present like type, defVal, mapping etc.
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }
            // Gel column model - 

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header1", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "empname");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header2", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "empid");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            // get above data along with extra data
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyid", companyid);
            KwlReturnObject result = accMachineManagementDAOObj.getMachineMasterData(requestParams);
            List<Machine> list = result.getEntityList();
            int count = result.getRecordTotalCount();
            for (Machine machine : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("empname", machine.getMachineName());
                jSONObject.put("billid", machine.getID());
                jSONObject.put("empid", machine.getMachineID());
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

        } catch (JSONException | NoSuchMessageException | SessionExpiredException | ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    
    @Override
    public JSONObject saveMachineCost(Map<String, Object> requestParams) throws ServiceException {
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
                result = accMachineManagementDAOObj.saveMachineCost(requestParams);
                audimsg = messageSource.getMessage("acc.mrp.machine.costsave", null, RequestContextUtils.getLocale(request));
                msg = messageSource.getMessage("acc.mrp.machine.costsaveSuccess", null, RequestContextUtils.getLocale(request));
            } else {
                /*
                 Edit case 
                 */
                isEdit = true;
                requestParams.put("resourceCostId", resourceCostId);
                result = accMachineManagementDAOObj.saveMachineCost(requestParams);
                audimsg = messageSource.getMessage("acc.mrp.machine.costupdate", null, RequestContextUtils.getLocale(request));
                msg = messageSource.getMessage("acc.mrp.machine.costupdateSuccess", null, RequestContextUtils.getLocale(request));
            }
//            MachineCost resourceCost = (MachineCost) result.getEntityList().get(0);
//
//            audimsg=audimsg+ "<b>" + resourceCost.getMachine().getMachineID();
//            auditTrailObj.insertAuditLog(AuditAction.Machine_MANAGEMENT, audimsg, request, resourceCost.getID());
            jSONObject.put("msg", msg);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jSONObject;
    }
    
    @Override
    public void deleteMachineCost(HashMap<String, Object> requestParams) throws ServiceException {
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
                
                KwlReturnObject result = accountingHandlerDAOobj.getObject(MachineCost.class.getName(),costid);
                MachineCost resourceCost = (MachineCost) result.getEntityList().get(0);
                resourceid=resourceCost.getMachine().getID();
                empCode += "<b>" + resourceCost.getMachine().getMachineID()+"</b>";
                accMachineManagementDAOObj.deleteMachineCost(requestParams);
                
                requestParams.put("labourId", resourceid);
                requestParams.put("isFromDeleteResourceCost", true);
                syncMachineCost(requestParams);
                
//                msg = messageSource.getMessage("acc.mrp.labour.ResourceCost.deletecost.audit", null, RequestContextUtils.getLocale(request));
//                auditTrailObj.insertAuditLog(AuditAction.Labour_MANAGEMENT, msg+empCode, request, costid);
            }
        } catch (JSONException | SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    
    @Override
    public JSONObject syncMachineCost(Map<String, Object> requestParams) throws ServiceException {
        JSONObject resObj = new JSONObject();
        try {
            JSONArray dataJArr = new JSONArray();
            Boolean isFromDeleteResourceCost=false;
            if (requestParams.containsKey("isFromDeleteResourceCost")) {
                isFromDeleteResourceCost=(Boolean)requestParams.get("isFromDeleteResourceCost");
            }
            if (requestParams.containsKey("labourId")) {//"labourId" will not come in case of sync all 
                requestParams.put("maxdate",true);
            }
            KwlReturnObject result = accMachineManagementDAOObj.getMachineCostSQL(requestParams);
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
    public void getMachineCostList(Map<String, Object> requestParams, JSONObject object) throws ServiceException {
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
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            // get above data along with extra data
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("labourId", LabourId);
            KwlReturnObject result = accMachineManagementDAOObj.getMachineCostSQL(requestParams);
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
    
    public JSONObject getActiveSubstituteMachines(Map<String, Object> requestParams) throws ServiceException {

        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        try {
            KwlReturnObject returnResult = accMachineManagementDAOObj.getActiveSubstituteMachines(requestParams);
            List<Machine> list = returnResult.getEntityList();

            for (Machine machine : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("id", machine.getID());
                jSONObject.put("machinename", machine.getMachineName());
                dataJArr.put(jSONObject);
            }
            jobj.put("data", dataJArr);
            jobj.put("msg", returnResult.getMsg());
        } catch (Exception ex) {
            Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    public boolean isMachineIDAlreadyPresent(Map<String, Object> requestParams) throws ServiceException {

      boolean isMachineIDAlreadyPresent=false;
        try {
            KwlReturnObject returnResult = accMachineManagementDAOObj.isMachineIDAlreadyPresent(requestParams);
            
            if(returnResult.getRecordTotalCount()>0){
                isMachineIDAlreadyPresent=true;
            }

        } catch (Exception ex) {
            Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return isMachineIDAlreadyPresent;
    }

    public JSONObject saveMachineMaster(Map<String, Object> requestParams) throws ServiceException {

        JSONObject jobj = new JSONObject();
        Machine machineObj = null;
        KwlReturnObject returnMappingResult = null,returnMachineWCList;
        try {
            KwlReturnObject returnResult = accMachineManagementDAOObj.saveMachineMaster(requestParams);
            List list = returnResult.getEntityList();
            if (list != null) {
                machineObj = (Machine) list.get(0);

                /*Save Machine Man Ration details*/
                String companyId = (String) requestParams.get("companyid");
                String machineid = (String) machineObj.getID();
                String fullMachineTime = (String) requestParams.get("fullMachineTime");
                String fullManTime = (String) requestParams.get("fullManTime");
                String partMachineTime = (String) requestParams.get("partMachineTime");
                String partManTime = (String) requestParams.get("partManTime");

                MachineManRatio machineManRatio = null;
                if (requestParams.containsKey("id") && requestParams.get("id") !=null && !StringUtil.isNullOrEmpty(requestParams.get("id").toString())) {
                    /*Edit case*/
                    KwlReturnObject result1 = accountingHandlerDAOobj.getObject(MachineManRatio.class.getName(), machineid);
                    machineManRatio = (MachineManRatio) result1.getEntityList().get(0);
                }
                if (machineManRatio == null) {
                    machineManRatio = new MachineManRatio();
                }
                if (!StringUtil.isNullOrEmpty(companyId)) {
                    KwlReturnObject result1 = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                    Company co = (Company) result1.getEntityList().get(0);
                    machineManRatio.setCompany(co);
                }
                if (!StringUtil.isNullOrEmpty(fullMachineTime)) {
                    machineManRatio.setFullMachineTime(Double.parseDouble(fullMachineTime));
                }

                if (!StringUtil.isNullOrEmpty(fullManTime)) {
                    machineManRatio.setFullManTime(Double.parseDouble(fullManTime));
                }
                if (!StringUtil.isNullOrEmpty(partMachineTime)) {
                    machineManRatio.setPartMachineTime(Double.parseDouble(partMachineTime));
                }

                if (!StringUtil.isNullOrEmpty(partManTime)) {
                    machineManRatio.setPartManTime(Double.parseDouble(partManTime));
                }

                if (!StringUtil.isNullOrEmpty(machineid)) {
                    machineManRatio.setMachine(machineObj);
                }
                KwlReturnObject manMationRatioObj = accMachineManagementDAOObj.saveMachineManRatio(machineManRatio);
                MachineManRatio mcObj = (MachineManRatio) manMationRatioObj.getEntityList().get(0);

                /*
                 Save Custom Field Data
                 */
                String customfield = (String) requestParams.get("customfield");
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    JSONArray jcustomarray = new JSONArray(customfield);
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", Constants.Acc_MRPMachineMaster_Modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_MRPMachineMaster_Id);
                    customrequestParams.put("modulerecid", machineObj.getID());
                    customrequestParams.put("moduleid", Constants.MRP_Machine_Management_ModuleId);
                    customrequestParams.put("companyid", companyId);
                    customrequestParams.put("customdataclasspath", Constants.Acc_MRPMachineMaster_CustomData_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        requestParams.put("accmachinecustomdataref", machineObj.getID());
                        requestParams.put("id", machineObj.getID());
                        KwlReturnObject accResult = accMachineManagementDAOObj.saveMachineMaster(requestParams);
                    }
                }
                requestParams.put("machineid", machineObj.getID());
                boolean isSubstituteMachine = false;
                if(requestParams.containsKey("issubstitutemachine") && !StringUtil.isNullOrEmpty(requestParams.get("issubstitutemachine").toString())){ //ERP-34776
                    isSubstituteMachine = Boolean.parseBoolean(requestParams.get("issubstitutemachine").toString());
                }
                
                accMachineManagementDAOObj.deleteMachineProcessMapping(requestParams);
                if (requestParams.containsKey("processid")) {
                    String[] processIds = (String[]) requestParams.get("processid");

                    for (int i = 0; i < processIds.length; i++) {
                        String processId = processIds[i];
                        requestParams.put("processid", processId);
                        returnMappingResult = accMachineManagementDAOObj.saveMachineProcessMapping(requestParams);
                    }
                }

                if (isSubstituteMachine) {
                    accMachineManagementDAOObj.deleteSubstituteMachineMapping(requestParams);

                    if (requestParams.containsKey("activemachineid")) {
                        String[] activeMachineIds = (String[]) requestParams.get("activemachineid");

                        for (int i = 0; i < activeMachineIds.length; i++) {
                            String activeMachineID = activeMachineIds[i];
                            requestParams.put("activeid", activeMachineID);
                            returnMappingResult = accMachineManagementDAOObj.saveSubstituteMachineMapping(requestParams);
                        }
                    }
                }

                if(requestParams.containsKey("workcenter")){
                   accMachineManagementDAOObj.deleteMachineWorkCenterMapping(requestParams);
                   returnMachineWCList= accMachineManagementDAOObj.saveMachineWorkCenterMapping(requestParams);
                }
                if (requestParams.containsKey("assetdetailId")) {
                    accMachineManagementDAOObj.deleteMachineAssetMapping(requestParams);
                    returnMachineWCList = accMachineManagementDAOObj.saveMachineAssetMapping(requestParams);
                } else {
                    accMachineManagementDAOObj.deleteMachineAssetMapping(requestParams);
                }
                jobj.put("machineid", machineObj.getMachineID());
                jobj.put("machine", machineObj);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject deleteMachineMasterPermanently(Map<String, Object> requestParams) throws ServiceException {

        JSONObject jobj = new JSONObject();
        Machine machineObj = null;
        List list = null;
        String[] arrayOfID = null;
        String linkedTransaction = "";
        Locale requestcontextutilsobj = null;
        KwlReturnObject returnResult = null;
        StringBuffer machineCodeString=new StringBuffer();
        Set<SubstituteMachineMapping> activeMachineMappingDetails = null;
        boolean ismachinesInUse=false, istempdelete=false;
        try {
            if (requestParams.containsKey("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
            }
            //ERP-26188 - Check whether it is Temporary delete or permanent delete.
            if (requestParams.containsKey("istempdelete") && requestParams.get("istempdelete")!=null) {
                istempdelete = (Boolean) requestParams.get("istempdelete");
            }


            if (requestParams.containsKey("idsfordelete") && requestParams.get("idsfordelete") != null) {
                arrayOfID = (String[]) requestParams.get("idsfordelete");

                for (int count = 0; count < arrayOfID.length; count++) {
                    
                    String machineId = arrayOfID[count];
                    int usageCout=0;
                    KwlReturnObject res = accountingHandlerDAOobj.getObject(Machine.class.getName(), machineId);
                    machineObj = (Machine) res.getEntityList().get(0);
                    String machineCode = machineObj.getMachineID();
                    
                    requestParams.put("id", machineId);
                    requestParams.put("machineid", machineId);

                    /*  Check if Machine ID is Mapped to Substitute Machine*/
                    
                    activeMachineMappingDetails = machineObj.getActiveMachineMappingDetails();

                    if (activeMachineMappingDetails.size() > 0) {

                        for (SubstituteMachineMapping activeMachineMapping : activeMachineMappingDetails) {
//                            linkedTransaction += activeMachineMapping.getSubstituteMachineID().getMachineID() + ", ";
                            usageCout++;
                        }
//                        continue;
                    }
                    if (!StringUtil.isNullOrEmpty(machineId)) {
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("machineid", machineId);
                        
                        // Checking For Work Centres
                        KwlReturnObject resultWC = accMachineManagementDAOObj.getWCforMachine(dataMap);
                        int WCcount = resultWC.getRecordTotalCount();
                        if (WCcount > 0) {
                            usageCout++;
//                            linkedTransaction += machineCode + ", ";
//                            continue;
                        }
                        
                        // CHecking For Work Orders
                        KwlReturnObject resultWO = accMachineManagementDAOObj.getWOforMachine(dataMap);
                        int WOcount = resultWO.getRecordTotalCount();
                        if (WOcount > 0) {
                            usageCout++;
//                            linkedTransaction += machineCode + ", ";
//                            continue;
                        }
                        
                        // CHecking For Routing Templates
                        KwlReturnObject resultRT = accMachineManagementDAOObj.getRTforMachine(dataMap);
                        int RTcount = resultRT.getRecordTotalCount();
                        if (RTcount > 0) {
                            usageCout++;
//                            linkedTransaction += machineCode + ", ";
//                            continue;
                        }

                    }
                    if (usageCout > 0) {
                        ismachinesInUse=true;
                         machineCodeString.append(" ");
                        machineCodeString.append(machineCode);
                        machineCodeString.append(",");
                        continue;
                    }
                    if (!istempdelete) {    //ERP-26188 - If it is for temporary delete then do not execute below lines.
                        returnResult = accMachineManagementDAOObj.deleteMachineProcessMapping(requestParams);
                        returnResult = accMachineManagementDAOObj.deleteMachineAssetMapping(requestParams);
                        returnResult = accMachineManagementDAOObj.deleteMachineMasterPermanently(requestParams);
                    }                    
                }
            }
            if (!ismachinesInUse) {
                //ERP-26188 - In temporary delete case @returnResult gets NULL. In this case, we will send default error message which will override in caller method.
                jobj.put("msg", returnResult != null ? returnResult.getMsg() : "Some error has been occured. Please try again later.");
                jobj.put("isdelete", true);//ERP-26188 : This flag has uded in Temporary Machine delete functionality. 
            } else {
                String machines = machineCodeString.toString();
                if (machines.length() > 0) {
                    machines = machines.substring(0, machines.length() - 1);
                }
                jobj.put("msg", messageSource.getMessage("acc.mrp.field.MappedMachineID", null, requestcontextutilsobj) + "<B>" + machines + "</B>" + " " + messageSource.getMessage("acc.field.cannnotdelete", null, requestcontextutilsobj));
                jobj.put("isdelete", false);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }

    public JSONObject deleteMachineMaster(Map<String, Object> requestParams) throws ServiceException {

        JSONObject jobj = new JSONObject();
        KwlReturnObject returnResult=null;
        String [] arrayOfID=null;
        String msg = "";
        try {
            
            if(requestParams.containsKey("idsfordelete")&& requestParams.get("idsfordelete")!=null){
                requestParams.put("istempdelete", true);    //ERP-26188 - Used this to key to identify that this call from temporary delete operation.
                arrayOfID=(String[])requestParams.get("idsfordelete");
                for(int count=0 ;count<arrayOfID.length;count++){                    
                    JSONObject jsobj = deleteMachineMasterPermanently(requestParams);
                    if(jsobj.has("isdelete") && jsobj.optBoolean("isdelete", false)){   
                        /*ERP-26188 - If records has not been used then we will delete it temporarily and will show the msg from temporay delete method.
                        else we will show message from @msg key of Permanent delete method. If any uncertain error occurs, we will show common error message.*/
                        requestParams.put("id", arrayOfID[count]);
                        returnResult = accMachineManagementDAOObj.deleteMachineMaster(requestParams);
                        msg = returnResult.getMsg();
                    } else {
                        msg = jsobj.has("msg") ? jsobj.optString("msg", "Some error has been occured. Please try again later.") : "";
                    }
                    
                }
            }
            jobj.put("msg", msg);
        } catch (Exception ex) {
            Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    
    public JSONObject exportMachineMaster(Map<String, Object> requestParams) throws ServiceException {
        
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        HttpServletRequest request=null;
        HttpServletResponse response=null;
        try {
          
            boolean isExport=true;
            requestParams.put("isExport", isExport);
            dataJArr=createMachineMasterJSON(requestParams);
            jobj.put("data", dataJArr);
            request=(HttpServletRequest)requestParams.get("request");
            response=(HttpServletResponse)requestParams.get("response");
            exportDaoObj.processRequest(request, response, jobj);
            
        } catch (Exception ex) {
            Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    public JSONArray createMachineMasterJSON(Map<String, Object> requestParams) throws ServiceException {

        JSONArray dataJArr = new JSONArray();
        Map<String, Object> requestParams1 = new HashMap<String, Object>();
        int count = 0;
        String assetDetailID="";
        DateFormat df = null;
        DateFormat userdf = null;
        Set<MachineProcessMapping> machineProcessMappingDetails = null;
        Set<SubstituteMachineMapping> activeMachineMappingDetails = null;
        Set<SubstituteMachineMapping> substituteMachineMappings = null;
        Set<MachineWorkCenterMapping> machineWorkCenterMappings = null;
        Set<MachineAssetMapping> machineAssetMappings = null;
        Set<RoutingTemplateMachineMapping> routingTemplateMachineMappings = null;
        try {
            
            if(requestParams.containsKey(Constants.df)){
                df=(DateFormat)requestParams.get(Constants.df);
            }
            if(requestParams.containsKey(Constants.userdf)){
                userdf = (DateFormat) requestParams.get(Constants.userdf);
            }
            boolean isExport = false;
            if (requestParams.containsKey("isExport") && (Boolean) requestParams.get("isExport")) {
                isExport = (Boolean) requestParams.get("isExport");
            }
            KwlReturnObject returnResult = accMachineManagementDAOObj.getMachineMasterData(requestParams);
            List<Machine> list = returnResult.getEntityList();
            count = returnResult.getRecordTotalCount();
            
            for (Machine machine : list) {
                JSONObject jSONObject = new JSONObject();

                jSONObject.put("id", machine.getID());
                jSONObject.put("machinename", machine.getMachineName());
                jSONObject.put("machineid", machine.getMachineID());
                jSONObject.put(Constants.SEQUENCEFORMATID,machine.getSeqformat()==null?"":machine.getSeqformat().getID());
                machineProcessMappingDetails = machine.getMachineProcessMappingDetails();
                String process = "", processIds = "",routingCode="";
                if (machineProcessMappingDetails != null) {
                    for (MachineProcessMapping machineProcessMapping : machineProcessMappingDetails) {
                        process = process + machineProcessMapping.getProcessID().getValue() + ",";
                        processIds = processIds + machineProcessMapping.getProcessID().getID() + ",";

                    }
                    if (!StringUtil.isNullOrEmpty(process)) {
                        process = process.substring(0, process.length() - 1);
                    }
                    if (!StringUtil.isNullOrEmpty(processIds)) {
                        processIds = processIds.substring(0, processIds.length() - 1);
                    }
                }
                jSONObject.put("process", process);
                jSONObject.put("processid", processIds);
                jSONObject.put("machineserialno", machine.getMachineSerialNo());
                jSONObject.put("machineoperatingcapacity", (int)machine.getMachineOperatingCapacity());
                jSONObject.put("ageofmachine", machine.getAgeOfMachine());
                if (isExport) {
                    jSONObject.put("dateofpurchase", machine.getDateOfPurchase() != null ? authHandler.getDateOnlyFormat().format(machine.getDateOfPurchase()) : "");
                    jSONObject.put("dateofinstallation", machine.getDateOfInstallation() != null ? authHandler.getDateOnlyFormat().format(machine.getDateOfInstallation()) : "");
                } else {
                    jSONObject.put("dateofpurchase", machine.getDateOfPurchase());
                    jSONObject.put("dateofinstallation", machine.getDateOfInstallation());
                }
                jSONObject.put("insuranceduedate", machine.getInsuranceDueDate());
                jSONObject.put("machineusescount", machine.getMachineUsesCount());
                jSONObject.put("vendorid", machine.getVendor()==null?"":machine.getVendor().getID());
                jSONObject.put("vendorname", machine.getVendor()==null?"":machine.getVendor().getName());
                jSONObject.put("deleted", machine.isDeleted());
                jSONObject.put("issubstitute", machine.isIsSubstitute());
                jSONObject.put("isassetmachine", machine.isIsAsset());
                jSONObject.put("purchaseaccountid", machine.getPurchaseAccount().getID());
                jSONObject.put("purchaseaccount", machine.getPurchaseAccount().getAccountName());
                jSONObject.put("shifttiming", machine.getShifttiming());
                
                if (machine.getIsMachineOnLease()) {
                    jSONObject.put("hasmachineonlease",machine.getIsMachineOnLease());
                    jSONObject.put("machinetype","Lease");
                } else {
                    jSONObject.put("hasmachineonlease",machine.getIsMachineOnLease());
                    jSONObject.put("machinetype","Self-Owned ");
                }
                jSONObject.put("startdateoflease", machine.getStartDateOfLease());
                jSONObject.put("enddateoflease", machine.getEndDateOfLease());
                jSONObject.put("machineprice", machine.getMachinePrice());
                jSONObject.put("leaseyears", machine.getLeaseYears());
                jSONObject.put("depreciationmethod", machine.getDepreciationMethod());
                jSONObject.put("depreciationrate", machine.getDepreciationRate());
                
                
                /*Get Machine Man ratio details*/
                KwlReturnObject result1 = accountingHandlerDAOobj.getObject(MachineManRatio.class.getName(), machine.getID());
                if (result1.getEntityList().size() > 0) {
                    MachineManRatio machineManRatio = (MachineManRatio) result1.getEntityList().get(0);
                    if (machineManRatio != null) {
                        jSONObject.put("fullMachineTime", machineManRatio.getFullMachineTime());
                        jSONObject.put("fullManTime", machineManRatio.getFullManTime());
                        jSONObject.put("partMachineTime", machineManRatio.getPartMachineTime());
                        jSONObject.put("partManTime", machineManRatio.getPartManTime());
                        
                        /*Below two keys are used to show ration in grid nly*/
                        jSONObject.put("fulltimeratio", machineManRatio.getFullMachineTime() + ":" + machineManRatio.getFullManTime());
                        jSONObject.put("parttimeratio", machineManRatio.getPartMachineTime() + ":" + machineManRatio.getPartManTime());
                    }
                }
                 
                
                

                /* Below code is used to get Substitute Machine name of each Active Machine*/
                activeMachineMappingDetails = machine.getActiveMachineMappingDetails();
                String substitutemachinename="";
                String substitutemachineid="";
                
                if (activeMachineMappingDetails != null) {
                    for (SubstituteMachineMapping substituteMachineMapping : activeMachineMappingDetails) {
                        substitutemachinename += substituteMachineMapping.getSubstituteMachineID().getMachineName() + ",";
                        substitutemachineid += substituteMachineMapping.getSubstituteMachineID().getMachineID() + ",";
                    }
                    if (!StringUtil.isNullOrEmpty(substitutemachinename)) {
                        substitutemachinename = substitutemachinename.substring(0, substitutemachinename.length() - 1);
                        substitutemachineid = substitutemachineid.substring(0, substitutemachineid.length() - 1);
                        jSONObject.put("substitutemachinename", substitutemachinename);
                        jSONObject.put("substitutemachineid", substitutemachineid);
                    }else{
                        jSONObject.put("substitutemachinename", "");
                        jSONObject.put("substitutemachineid", "");
                    }
                }
                
                 /* Below code is used to get Active machine names of assinged substitute machine*/
                substituteMachineMappings=machine.getSustituteMachineMappingDetails();
                String activemachineids = "", activemachinenames = "";
                if (substituteMachineMappings != null && substituteMachineMappings.size()>0) {
                    for (SubstituteMachineMapping substituteMachineMapping : substituteMachineMappings) {
                        activemachineids = activemachineids + substituteMachineMapping.getActiveMachineID().getID() + ",";
                        activemachinenames = activemachinenames + substituteMachineMapping.getActiveMachineID().getMachineName() + ",";

                    }
                    activemachineids = activemachineids.substring(0, activemachineids.length() - 1);
                    activemachinenames = activemachinenames.substring(0, activemachinenames.length() - 1);
                    
                    jSONObject.put("activemachinenames", activemachinenames);
                    jSONObject.put("activemachineids", activemachineids);

                }
                
                 /* Below code is used to get Machine Work Center mapping details*/
                machineWorkCenterMappings = machine.getMachineWorkCenterMappingDetails();
                StringBuilder wNameSb=new StringBuilder();
                StringBuilder wIDSb=new StringBuilder();
                if (machineWorkCenterMappings != null && machineWorkCenterMappings.size() > 0) {

                    for (MachineWorkCenterMapping machineWorkCenterMapping : machineWorkCenterMappings) {
                           //Create comma seperated string of workcentres
                            wNameSb.append(machineWorkCenterMapping.getWorkCenterID().getName());
                            wNameSb.append(",");
                            wIDSb.append(machineWorkCenterMapping.getWorkCenterID().getID());
                            wIDSb.append(",");
                    }
                }

                jSONObject.put("workcenter", wNameSb.length() > 1 ? wNameSb.substring(0, wNameSb.length() - 1) : "");
                jSONObject.put("workcenterid", wIDSb.length() > 1 ? wIDSb.substring(0, wIDSb.length() - 1) : "");

                /* Below code is used to get Machine Asset mapping details*/
                machineAssetMappings = machine.getMachineAssetMappings();
                if (machineAssetMappings != null) {
                    for (MachineAssetMapping machineAssetMapping : machineAssetMappings) {
                        jSONObject.put("assetid", machineAssetMapping.getAssetDetails().getAssetId());
                        assetDetailID=machineAssetMapping.getAssetDetails().getId();
                        jSONObject.put("assetdetailId", assetDetailID);
                        jSONObject.put("productname", machineAssetMapping.getAssetDetails().getProduct().getID());
                        jSONObject.put("product", machineAssetMapping.getAssetDetails().getProduct().getName());
                    }
                }
                /* Below code is used to get Machine Routing Template mapping details*/
                routingTemplateMachineMappings = machine.getRoutingTemplateMachineMappings();
                if (routingTemplateMachineMappings != null) {
                     for (RoutingTemplateMachineMapping  routingTemplateMachineMapping: routingTemplateMachineMappings) {
                        routingCode+=routingTemplateMachineMapping.getRoutingtemplate().getName()+ ",";
                    }
                     if (!StringUtil.isNullOrEmpty(routingCode)) {
                        routingCode = routingCode.substring(0, routingCode.length() - 1);
                    }
                }
                jSONObject.put("assignedroutecode",routingCode);
                requestParams1.put("machineid", machine.getID());
                KwlReturnObject resObject = workOrderDAO.getWorkOrderMachineMapping(requestParams1);
                List<WorkOrder> workOrderList = resObject.getEntityList();
                
                String workOrderID = "",machineBreakDownID="",maintenaceDueDate="";
                if (workOrderList.size() > 0) {
                    for (WorkOrder workOrder : workOrderList) {
                        workOrderID += workOrder.getWorkOrderID() + ",";
                    }
                    workOrderID = workOrderID.substring(0, workOrderID.length() - 1);
                }
                jSONObject.put("assignedworkorder",workOrderID);
                
                if (!StringUtil.isNullOrEmpty(assetDetailID)) {
                    requestParams1.put("assetdetailsid", assetDetailID);
                    KwlReturnObject returnList = accMachineManagementDAOObj.getMachineAssetMaintenaceDetails(requestParams1);
                    List<AssetMaintenanceSchedulerObject> assetMaintenanceSchedulerObject = returnList.getEntityList();
                    if (assetMaintenanceSchedulerObject.size() > 0) {
                        for (AssetMaintenanceSchedulerObject assetMaintenanceSchedulerObject1 : assetMaintenanceSchedulerObject) {
                            jSONObject.put("breakdowntrackingid", assetMaintenanceSchedulerObject1.getScheduleName());
                            jSONObject.put("maintenanceschedule", (assetMaintenanceSchedulerObject1.getEndDate() != null) ? df.format(assetMaintenanceSchedulerObject1.getEndDate()) : "");
                        }
                    }
                }
                
                /*
                 Add Global Custom data for document
                 */
                String companyId = (String) requestParams.get("companyid");
                Map globalMap = new HashMap();
                globalMap.put("moduleid", Constants.MRP_Machine_Management_ModuleId);
                globalMap.put("companyid", companyId);
                globalMap.put("machineid", machine.getID());
                globalMap.put(Constants.userdf, userdf);
                putGlobalCustomDetailsForMachine(jSONObject, globalMap);
                dataJArr.put(jSONObject);
            }
            
            
        } catch (Exception ex) {
            Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return dataJArr;
    }
    
    public void putGlobalCustomDetailsForMachine(JSONObject jSONObject, Map<String, Object> map) throws ServiceException, JSONException {

        String companyId = "";
        int moduleId = 0;
        String machineId = "";
        if (map.containsKey("companyid")) {
            companyId = map.get("companyid").toString();
        }
        if (map.containsKey("moduleid")) {
            moduleId = Integer.parseInt(map.get("moduleid").toString());
        }
        if (map.containsKey("machineid")) {
            machineId = map.get("machineid").toString();
        }
        /*
         *   Get Custom Field Data 
         */
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> customFieldMap = new HashMap<>();
        HashMap<String, String> customDateFieldMap = new HashMap<>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, moduleId));
        HashMap<String, String> replaceFieldMap = new HashMap<>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
        Map<String, Object> variableMap = new HashMap<>();

        KwlReturnObject customObjresult = null;
        customObjresult = accountingHandlerDAOobj.getObject(MachineCustomData.class.getName(), machineId);
        replaceFieldMap = new HashMap<>();
        if (customObjresult != null && customObjresult.getEntityList().size() > 0) {
            MachineCustomData machineCustomData = (MachineCustomData) customObjresult.getEntityList().get(0);
            if (machineCustomData != null) {
                AccountingManager.setCustomColumnValues(machineCustomData, FieldMap, replaceFieldMap, variableMap);
                JSONObject params = new JSONObject();
                params.put("companyid", companyId);
                params.put("isExport", true);
                params.put(Constants.userdf, map.get(Constants.userdf));
                fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
            }
        }
    }
    
    @Override
    public JSONArray getMachineCombo(Map<String, Object> map) throws ServiceException {
        JSONArray jArr = new JSONArray();
        JSONObject jSONObject = new JSONObject();
        KwlReturnObject result = accMachineManagementDAOObj.getMachineCombo(map);
        List list = result.getEntityList();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            try {
                jSONObject = new JSONObject();
                Object obj[] = (Object[]) itr.next();
                jSONObject.put("id", (String) obj[0]);
                jSONObject.put("name", (String) obj[1]);
                jSONObject.put("machineid", (String) obj[2]);
                jArr.put(jSONObject);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                throw ServiceException.FAILURE(ex.getMessage(), ex);
            }
        }
        return jArr;
    }
    @Override
    public JSONObject saveMachineManRatio(Map<String, Object> requestParams) throws ServiceException {
       JSONObject jobj = new JSONObject();
       try{ 
        boolean isUpdate = false;
        String id = (String) requestParams.get("id");       
        String dateForRatio = (String) requestParams.get("dateForRatio");
        String machine = (String) requestParams.get("machine");
        String fullMachineTime = (String) requestParams.get("fullMachineTime");
        String fullManTime = (String) requestParams.get("fullManTime");
        String partMachineTime = (String) requestParams.get("partMachineTime");
        String partManTime = (String) requestParams.get("partManTime");
        String companyid = (String) requestParams.get("companyid");
        String userid = (String) requestParams.get("userid");
        DateFormat df=(DateFormat) requestParams.get(Constants.df);
        
        KwlReturnObject coresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) coresult.getEntityList().get(0);
        coresult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
        User creator = (User) coresult.getEntityList().get(0);

        long createdon = System.currentTimeMillis();
        long updatedon = System.currentTimeMillis();

        MachineManRatio machineManRatio = new MachineManRatio();
        if (!StringUtil.isNullOrEmpty(id)) {
            KwlReturnObject result1 = accountingHandlerDAOobj.getObject(MachineManRatio.class.getName(), id);
            machineManRatio = (MachineManRatio) result1.getEntityList().get(0);
            isUpdate = true;
        }
        machineManRatio.setCompany(company);
        machineManRatio.setCreatedon(createdon);
        machineManRatio.setUpdatedon(updatedon);
        machineManRatio.setCreatedby(creator);
        machineManRatio.setModifiedby(creator);
        
        if (!StringUtil.isNullOrEmpty(dateForRatio)) {
            machineManRatio.setDateForRatio(df.parse(dateForRatio));
        }
      
        if (!StringUtil.isNullOrEmpty(fullMachineTime)) {
            machineManRatio.setFullMachineTime(Double.parseDouble(fullMachineTime));
        } 
        
        if (!StringUtil.isNullOrEmpty(fullManTime)) {
            machineManRatio.setFullManTime(Double.parseDouble(fullManTime));
        }
        if (!StringUtil.isNullOrEmpty(partMachineTime)) {
            machineManRatio.setPartMachineTime(Double.parseDouble(partMachineTime));
        } 
        
        if (!StringUtil.isNullOrEmpty(partManTime)) {
            machineManRatio.setPartManTime(Double.parseDouble(partManTime));
        }
        
         if (!StringUtil.isNullOrEmpty(machine)) {
            KwlReturnObject result1 = accountingHandlerDAOobj.getObject(Machine.class.getName(), machine);
            Machine machineObj = (Machine) result1.getEntityList().get(0);
            machineManRatio.setMachine(machineObj);
        }

        accMachineManagementDAOObj.saveMachineManRatio(machineManRatio);
        jobj.put("machineManRatio", machineManRatio);
        jobj.put("isUpdate", isUpdate);
        
       }catch(Exception ex){
            Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
       }

        return jobj;
    }

    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getMachineManRatio(HashMap<String, Object> requestParams) throws ServiceException {
        JSONObject JObj = new JSONObject();
        try {
            KwlReturnObject result = accMachineManagementDAOObj.getMachineManRatio(requestParams);
            List<MachineManRatio> list = result.getEntityList();
            int count = result.getRecordTotalCount();
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            JSONArray jSONArray = new JSONArray();
            for (MachineManRatio machineManRatio : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("id", machineManRatio.getID());
                jSONObject.put("machine", machineManRatio.getMachine().getID());
                jSONObject.put("machinename", machineManRatio.getMachine().getMachineName());
                jSONObject.put("dateForRatio", machineManRatio.getDateForRatio());
                jSONObject.put("fullMachineTime", machineManRatio.getFullMachineTime());
                jSONObject.put("fullManTime", machineManRatio.getFullManTime());
                jSONObject.put("fulltimeratio", machineManRatio.getFullMachineTime()+":"+machineManRatio.getFullManTime());
                jSONObject.put("partMachineTime", machineManRatio.getPartMachineTime());
                jSONObject.put("partManTime", machineManRatio.getPartManTime());
                jSONObject.put("parttimeratio", machineManRatio.getPartMachineTime()+":"+machineManRatio.getPartManTime());
                jSONArray.put(jSONObject);
            }
            JObj.put("data", jSONArray);
            JObj.put("count", count);
        } catch (JSONException ex) {
             Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return JObj;

    }

    @Override
    public JSONObject deleteMachineManRatio(Map<String, Object> requestParams) throws ServiceException {
      JSONObject jObj = new JSONObject();
      boolean successFlag=false;
        try{    
         
        String id = (String) requestParams.get("id");       
        String companyid = (String) requestParams.get("companyid");
        KwlReturnObject rObject=accMachineManagementDAOObj.deleteMachineManRatio(companyid, id);
        successFlag=rObject.isSuccessFlag();
        jObj.put("successFlag",successFlag);
         
        } catch (Exception ex) {
             Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jObj;
    }
    @Override
    public JSONObject syncMachineDataToPM(Map<String, Object> requestParams) throws ServiceException {
      JSONObject jObj = new JSONObject();
      JSONObject resObj=new JSONObject();
      JSONArray dataJArr = new JSONArray();
      Set<MachineProcessMapping> machineProcessMappingDetails = null;
      int count=0;
        try {
            if (requestParams.containsKey("isAutoSync")) {
                boolean isAutoSync = (boolean) requestParams.get("isAutoSync");
                Machine machine = (Machine) requestParams.get("machine");
                if (isAutoSync) {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("id", machine.getID());
                    jSONObject.put("machinename", machine.getMachineName());
                    jSONObject.put("machineid", machine.getMachineID());
                    jSONObject.put("machineserialno", machine.getMachineSerialNo());
                    jSONObject.put("machineoperatingcapacity", machine.getMachineOperatingCapacity());
                    jSONObject.put("ageofmachine", machine.getAgeOfMachine());
                    jSONObject.put("dateofinstallation", machine.getDateOfInstallation());
                    jSONObject.put("dateofpurchase", machine.getDateOfPurchase());
                    jSONObject.put("insuranceduedate", machine.getInsuranceDueDate());
                    jSONObject.put("machineusescount", machine.getMachineUsesCount());
                    machineProcessMappingDetails = machine.getMachineProcessMappingDetails();
                    String process = "", processIds = "";
                    if (machineProcessMappingDetails != null) {
                        for (MachineProcessMapping machineProcessMapping : machineProcessMappingDetails) {
                            process = process + machineProcessMapping.getProcessID().getValue() + ",";
                            processIds = processIds + machineProcessMapping.getProcessID().getID() + ",";
                        }
                        process = process.substring(0, process.length() - 1);
                        processIds = processIds.substring(0, processIds.length() - 1);
                    }
                    jSONObject.put("process", processIds);
                    dataJArr.put(jSONObject);
                }
            } else {
                KwlReturnObject returnResult = accMachineManagementDAOObj.getMachineMasterData(requestParams);
                List<Machine> list = returnResult.getEntityList();
                count = returnResult.getRecordTotalCount();
                for (Machine machine : list) {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("id", machine.getID());
                    jSONObject.put("machinename", machine.getMachineName());
                    jSONObject.put("machineid", machine.getMachineID());
                    jSONObject.put("machineserialno", machine.getMachineSerialNo());
                    jSONObject.put("machineoperatingcapacity", machine.getMachineOperatingCapacity());
                    jSONObject.put("ageofmachine", machine.getAgeOfMachine());
                    jSONObject.put("dateofinstallation", machine.getDateOfInstallation());
                    jSONObject.put("dateofpurchase", machine.getDateOfPurchase());
                    jSONObject.put("insuranceduedate", machine.getInsuranceDueDate());
                    jSONObject.put("machineusescount", machine.getMachineUsesCount());
                    machineProcessMappingDetails = machine.getMachineProcessMappingDetails();
                    String process = "", processIds = "";
                    if (machineProcessMappingDetails != null) {
                        for (MachineProcessMapping machineProcessMapping : machineProcessMappingDetails) {
                            process = process + machineProcessMapping.getProcessID().getValue() + ",";
                            processIds = processIds + machineProcessMapping.getProcessID().getID() + ",";

                        }
                        process = process.substring(0, process.length() - 1);
                        processIds = processIds.substring(0, processIds.length() - 1);
                    }
                    jSONObject.put("process", processIds);
                    dataJArr.put(jSONObject);
                }
            }
            jObj.put("inputdata", dataJArr);
            jObj.put("count", count);
         
         
         /* Below code is used send Machine data to PM */
         
         JSONObject userData = new JSONObject();
            userData.put("inputdata", dataJArr);
            userData.put("count", count);
            userData.put("companyid", requestParams.get("companyid"));
            String accRestURL = URLUtil.buildRestURL("pmURL");
            String endpoint = accRestURL + "master/machines";
            resObj = apiCallHandlerService.restPostMethod(endpoint, userData.toString());
         
         /* Update Machine Syncable flag  */
         
         JSONArray machineIdsArray = resObj.optJSONArray("ids");
            if (machineIdsArray!=null && machineIdsArray.length()>0) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("ids", machineIdsArray);
                KwlReturnObject result = accMachineManagementDAOObj.updateMachineSyncableFlag(dataMap);
            }
         
        } catch (ServiceException | JSONException ex) {
             Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return resObj;
    }

    @Override
    public JSONObject exportMachineAllocationReportXlsx(Map<String, Object> requestParams) throws ServiceException {
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
//             JSONObject res=apiCallHandlerService.restGetMethod(endpoint, jObj.toString());
             ByteArrayOutputStream res=apiCallHandlerService.restGetMethodForFile(endpoint, jObj.toString());
             
             if(res != null){
                 String fileName = "ResourceAllocation";
                 if(requestParams.containsKey("filename")){
                     fileName = (String)requestParams.get("filename");
                 }
//                 Object osString = (String)res.get("data");
//                 ByteArrayOutputStream os = (ByteArrayOutputStream) osString;
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
            Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | JSONException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                retObj.put("msg", msg);
                retObj.put("success", success);
            } catch (JSONException ex) {
                Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return retObj;
    }
   @Override
    public JSONObject getExpanderDetails(HashMap<String, Object> requestParams )  throws ServiceException{
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        KwlReturnObject kmsgRT = null;
        KwlReturnObject kmsgWO = null;
        try {
            String mid = requestParams.get("machineid").toString();
            String machineid = "";
            String machineName= "";
            String labourid = "";
            String labourName  = "";
            String wcid = "";
            String wcName = "";
            int RTSize = 0;
            int WOSize = 0;
            int count = 0;
            kmsgRT = accMachineManagementDAOObj.getRTforMachine(requestParams);
            kmsgWO = accMachineManagementDAOObj.getWOforMachine(requestParams);
            List RTList = kmsgRT.getEntityList();
            List WOList = kmsgWO.getEntityList();
            RTSize = RTList.size();
            WOSize = WOList.size();
            
            if (RTSize >= WOSize ) {
                count = RTSize;
            } else {
                count = WOSize;
            }
            int RTIndex = 0;
            int WOIndex = 0;
            for (int index = 0 ; index < count; index++) {
                JSONObject tempJobj = new JSONObject();
                tempJobj.put("id",mid);
                
                if (RTIndex < RTSize) {
                    String rtid =  RTList.get(RTIndex).toString();
                    KwlReturnObject result1 = accountingHandlerDAOobj.getObject(RoutingTemplateMachineMapping.class.getName(), rtid);
                    RoutingTemplateMachineMapping RTObj = (RoutingTemplateMachineMapping) result1.getEntityList().get(0);
                    tempJobj.put("routingcode",(RTObj != null && RTObj.getRoutingtemplate()!= null) ? RTObj.getRoutingtemplate().getName(): "");
                    RTIndex++;
                }
                if (WOIndex < WOSize) {
                    String woid  = WOList.get(WOIndex).toString();
                    KwlReturnObject result1 = accountingHandlerDAOobj.getObject(WorkOrderMachineMapping.class.getName(), woid);
                    WorkOrderMachineMapping WOObj = (WorkOrderMachineMapping) result1.getEntityList().get(0);
                    tempJobj.put("wocode",(WOObj != null && WOObj.getWorkorderid() != null) ? WOObj.getWorkorderid().getWorkOrderID() : "");
                    tempJobj.put("woname",(WOObj != null && WOObj.getWorkorderid() != null) ? WOObj.getWorkorderid().getWorkOrderName() : "");
                    WOIndex++;
                }
                jarr.put(tempJobj);
            }
            
            jobj.put("data", jarr);
        } catch(Exception ex) {
            Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
}
