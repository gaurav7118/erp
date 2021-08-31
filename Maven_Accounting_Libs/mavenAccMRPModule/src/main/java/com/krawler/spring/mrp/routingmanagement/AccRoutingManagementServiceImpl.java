/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.routingmanagement;

import com.krawler.common.admin.CustomizeReportMapping;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.mrp.WorkOrder.WorkOrder;
import com.krawler.spring.mrp.jobwork.JobWork;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;

/**
 *
 * @author krawler
 */
public class AccRoutingManagementServiceImpl implements AccRoutingManagementService {

    private MessageSource messageSource;
    private APICallHandlerService apiCallHandlerService;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accAccountDAO accAccountDAOobj;
    private AccRoutingManagementDao accRoutingManagementDaoObj;
    private fieldDataManager fieldDataManagercntrl;

    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {
        this.apiCallHandlerService = apiCallHandlerService;
    }

    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }
    public void setAccRoutingManagementDaoObj(AccRoutingManagementDao accRoutingManagementDaoObj) {
        this.accRoutingManagementDaoObj = accRoutingManagementDaoObj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    @Override
    public JSONObject saveRoutingTemplate(Map<String, Object> dataMap) throws ServiceException {
        JSONObject jobj = new JSONObject();

        try {
            boolean isEdit = false;
            if (dataMap.containsKey(RoutingTemplate.RTID) && !StringUtil.isNullOrEmpty(dataMap.get(RoutingTemplate.RTID).toString())) {
                isEdit = true;
            }
            KwlReturnObject kwl = accRoutingManagementDaoObj.saveRoutingTemplate(dataMap);
            RoutingTemplate routingTemplateObj = (RoutingTemplate) kwl.getEntityList().get(0);
            String routingtemplateid = routingTemplateObj.getId();
            /*
            Putting parent routing template id int ojobj so that it is used while saving alternate routing as parentid
            */
            jobj.put(RoutingTemplate.PARENTRTID,routingtemplateid);
            Map<String, Object> deleteParams = new HashMap();
            deleteParams.put(WorkOrder.WOID, routingtemplateid);
            //  ****************************Save one to many machine mapping *************************
            if (isEdit) {
                deleteParams.put(WorkOrder.POJO, RoutingTemplateMachineMapping.POJONAME);
                deleteParams.put(WorkOrder.ATTRIBUTE, RoutingTemplateMachineMapping.ATTRIBUTENAME);
                kwl = accRoutingManagementDaoObj.deleteRoutingTemplateMappings(deleteParams);
            }
            if (dataMap.containsKey(RoutingTemplate.MACHINEMAPPING) && !StringUtil.isNullOrEmpty(dataMap.get(RoutingTemplate.MACHINEMAPPING).toString())) {
                Map<String, Object> machineMappingDataMap = new HashMap();
                machineMappingDataMap.put(RoutingTemplate.MACHINEMAPPING, dataMap.get(RoutingTemplate.MACHINEMAPPING).toString());
                machineMappingDataMap.put("routingtemplateObj", routingTemplateObj);
                kwl = accRoutingManagementDaoObj.saveRoutingTemplateMachineMapping(machineMappingDataMap);
            }
//                ****************************Save one to many machine mapping *************************

            //****************************Save one to many labour mapping *************************
            if (isEdit) {
                deleteParams.put(WorkOrder.POJO, RoutingTemplateLabourMapping.POJONAME);
                deleteParams.put(WorkOrder.ATTRIBUTE, RoutingTemplateLabourMapping.ATTRIBUTENAME);
                kwl = accRoutingManagementDaoObj.deleteRoutingTemplateMappings(deleteParams);
            }
            if (dataMap.containsKey(RoutingTemplate.LABOURMAPPING) && !StringUtil.isNullOrEmpty(dataMap.get(RoutingTemplate.LABOURMAPPING).toString())) {
                Map<String, Object> LabourMappingDataMap = new HashMap();
                LabourMappingDataMap.put(RoutingTemplate.LABOURMAPPING, dataMap.get(RoutingTemplate.LABOURMAPPING).toString());
                LabourMappingDataMap.put("routingtemplateObj", routingTemplateObj);
                kwl = accRoutingManagementDaoObj.saveRoutingTemplateLabourMapping(LabourMappingDataMap);
            }
//                ****************************Save one to many labour mapping *************************
            if (dataMap.containsKey(RoutingTemplate.PROJECTID) && !StringUtil.isNullOrEmpty((String) dataMap.get(RoutingTemplate.PROJECTID))) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("companyid", dataMap.get(RoutingTemplate.COMPANYID));
                userData.put("userid", dataMap.get(RoutingTemplate.USERID));
                userData.put("projectname",dataMap.get(RoutingTemplate.RTNAME));// Sending routing template name as Project Name
                userData.put("isNewProject", "false");// As Updating the Existing Project
                userData.put("isMasterProject", "true");// True as Routing Tepmalte Project creates Master Project
                userData.put("projectId", (String) dataMap.get(RoutingTemplate.PROJECTID));
                userData.put("projectcode",routingTemplateObj.getName());
                createOrUpdateProjectRest(userData);
          
            }
            /*
             Save Custom Field Data
             */
            String customfield = (String) dataMap.get(RoutingTemplate.CUSTOMFIELD);
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_MRPRoutingTemplate_Modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_MRPRoutingTemplate_Id);
                customrequestParams.put("modulerecid", routingtemplateid);
                customrequestParams.put("moduleid", Constants.MRP_RouteCode);
                customrequestParams.put("companyid", dataMap.get(RoutingTemplate.COMPANYID));
                customrequestParams.put("customdataclasspath", Constants.Acc_MRPRoutingTemplate_CustomData_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    dataMap.put(RoutingTemplate.ACCROUTINGTEMPLATECUSTOMDATA, routingtemplateid);
                    dataMap.put(RoutingTemplate.RTID, routingtemplateid);
                    kwl = accRoutingManagementDaoObj.saveRoutingTemplate(dataMap);
                }
            }
        } catch (Exception ex) {

            throw ServiceException.FAILURE("AccRoutingManagementServiceImpl.saveRoutingTemplate", ex);
        }
        return jobj;

    }

    public JSONObject getRoutingtemplates(Map<String, Object> requestParams) throws ServiceException{
        JSONObject jobj = new JSONObject();
        JSONObject dataObj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        JSONObject commData = new JSONObject();
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        try {

            Boolean isExport = false;
            Boolean isforcombo = false;
            Locale requestcontextutilsobj = null;
            Boolean isRoutingCode = false;
            if (requestParams.containsKey("isExport") && requestParams.get("isExport") != null) {
                isExport = (Boolean) requestParams.get("isExport");
            }
            if (requestParams.containsKey("isforcombo") && requestParams.get("isforcombo") != null) {
                isforcombo = (Boolean) requestParams.get("isforcombo");
            }
            if (requestParams.containsKey("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
            }
            if (requestParams.containsKey("isRoutingCode") && requestParams.get("isRoutingCode") != null) {
                isRoutingCode = Boolean.parseBoolean(requestParams.get("isRoutingCode").toString());
            }
         
            dataObj=getDataJarrOfRoutingTemplate(requestParams);
            dataJArr = dataObj.optJSONArray("data");
            if (isforcombo) {
                commData.put("success", true);
                commData.put("data", dataJArr);
                jobj.put("data", commData);
                return jobj;
            }
            //*******************Record****************************
            JSONObject rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.RTID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.RTNAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.RTCODE);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.PROJECTID);
            jarrRecords.put(rec);
            
             rec = new JSONObject();
             rec.put(JobWork.KEY, RoutingTemplate.BOMID);
            jarrRecords.put(rec);
            
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.BOMNAME);
            jarrRecords.put(rec);
             rec = new JSONObject();
             rec.put(JobWork.KEY, RoutingTemplate.WORKCENTER);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.WORKCENTERNAME);
            jarrRecords.put(rec);
            
             rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.LABOURID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.LABOURNAME);
            jarrRecords.put(rec);
            
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.MACHINEID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.MACHINENAME);
            jarrRecords.put(rec);
        
             rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.DURATIONTYPE);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.DURATION);
            jarrRecords.put(rec);
             rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.PROJECTID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.DURATIONTYPENAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.ALTERNATERTID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.ALTERNATERTNAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.ALTERNATERTCODE);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.ALTERNATEPROJECTID);
            jarrRecords.put(rec);

            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.ALTERNATEBOMID);
            jarrRecords.put(rec);

            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.ALTERNATEBOMNAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.ALTERNATEWORKCENTER);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.ALTERNATEWORKCENTERNAME);
            jarrRecords.put(rec);

            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.ALTERNATELABOURID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.ALTERNATELABOURNAME);
            jarrRecords.put(rec);

            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.ALTERNATEMACHINEID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.ALTERNATEMACHINENAME);
            jarrRecords.put(rec);

            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.ALTERNATEDURATIONTYPE);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.ALTERNATEDURATION);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.ALTERNATEPROJECTID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.ALTERNATEDURATIONTYPENAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, RoutingTemplate.ALTERNATEROUTING);
            jarrRecords.put(rec);
             rec = new JSONObject();
            rec.put(JobWork.KEY, "product");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, "productid");
            jarrRecords.put(rec);
             rec = new JSONObject();
            rec.put(JobWork.KEY, "alternateproductid");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, "alternateproduct");
            jarrRecords.put(rec);
             rec = new JSONObject();
            rec.put(JobWork.KEY, "deleted");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, "seqformat");
            jarrRecords.put(rec);
            
            //*******************Record****************************
            //*****************ColumnModel*************************
//         
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.routingcode.Rc", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", RoutingTemplate.RTCODE);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("hidden",!isRoutingCode);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workorder.report.header8", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", RoutingTemplate.RTNAME);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("hidden",isRoutingCode);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.product.gridProduct", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "product");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
              jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.field.bomcode", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", RoutingTemplate.BOMNAME);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.field.workCenter", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", RoutingTemplate.WORKCENTERNAME);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.header9", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", RoutingTemplate.MACHINENAME);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
              jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.labour.empid", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", RoutingTemplate.LABOURNAME);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
              jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.field.durationtypecombo.fieldlabel", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", RoutingTemplate.DURATIONTYPENAME);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
              jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.field.durationtype.fieldlabel", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", RoutingTemplate.DURATION);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
            /*
             Add Custom Fields in Column Model
             */
            requestParams.put("companyId", requestParams.get(RoutingTemplate.COMPANYID));
            requestParams.put("reportId", Constants.MRP_RouteCode);
            putCustomColumnForRoutingTemplate(jarrColumns, jarrRecords, requestParams);
         
//*****************ColumnModel******************************
            commData.put("coldata", dataJArr);
            commData.put("columns", jarrColumns);
            commData.put("totalCount", dataObj.optInt("totalCount",0));
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);

            jobj.put("valid", true);

            if (isExport) {
                jobj.put("data", dataJArr);
            } else {
                jobj.put("data", commData);
            }
        } catch (Exception ex) {
             throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    
    /**
     *
     * @param jarrColumns = Column Model
     * @param jarrRecords = Record for store
     * @param requestParams
     * @Description : Add Column model for Custom Field
     * @throws ServiceException
     */
    public void putCustomColumnForRoutingTemplate(JSONArray jarrColumns, JSONArray jarrRecords, Map<String, Object> requestParams) throws ServiceException {
        try {
            HashMap requestParams1 = new HashMap(requestParams);
            KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(requestParams1);
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
        } catch (ServiceException | JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    /*
    @author sharad pawar
    this method is used to get list of routing template 
    */
    private JSONObject getDataJarrOfRoutingTemplate(Map<String, Object> requestParams) throws ServiceException {
        JSONArray dataJarr = new JSONArray();
        JSONObject dataObj = new JSONObject();
        try {
            Boolean isforcombo = requestParams.containsKey("isforcombo") ? (Boolean) requestParams.get("isforcombo") : false;
            
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
             int routingMasterType=0;
            if (requestParams.containsKey("routingmastertype")) {
                 routingMasterType = Integer.parseInt((String) requestParams.get("routingmastertype"));
                if (routingMasterType == RoutingTemplate.MASTERTYPE_ROUTING_TEMPLATE) {
                    requestParams.put("isRoutingCode", false);
                } else if (routingMasterType == RoutingTemplate.MASTERTYPE_ROUTING_CODE) {
                    requestParams.put("isRoutingCode", true);
                }
            }
            if (requestParams.containsKey(Constants.REQ_startdate) && !StringUtil.isNullObject(requestParams.get(Constants.REQ_startdate))) {
                requestParams.put(Constants.REQ_startdate, df.parse((String) requestParams.get(Constants.REQ_startdate)));
            }
            if (requestParams.containsKey(Constants.REQ_enddate) && !StringUtil.isNullObject(requestParams.get(Constants.REQ_enddate))) {
                requestParams.put(Constants.REQ_enddate, df.parse((String) requestParams.get(Constants.REQ_enddate)));
            }
            KwlReturnObject result = accRoutingManagementDaoObj.getRoutingTemplates(requestParams);
            List dataList = result.getEntityList();
            RoutingTemplate routing = null;
            JSONObject tmpObj = new JSONObject();
            for (Object obj : dataList) {
                routing = (RoutingTemplate) obj;
                tmpObj = new JSONObject();

                tmpObj.put(RoutingTemplate.RTID, routing.getId());
                tmpObj.put("deleted", routing.isDeleted());
                tmpObj.put(RoutingTemplate.RTNAME, routing.getName());
                tmpObj.put("seqformat", routing.getSeqformat() != null ? routing.getSeqformat().getID(): "");
                if (routingMasterType == RoutingTemplate.MASTERTYPE_ROUTING_CODE) {
                    /* This if block is added when we are fetching routing code to show in combo  in WO form  only*/
                    tmpObj.put(RoutingTemplate.RTNAME, routing.getRoutecode());
                }
                tmpObj.put(RoutingTemplate.RTCODE, routing.getRoutecode());
                tmpObj.put(RoutingTemplate.PROJECTID, routing.getProjectId());
                tmpObj.put(RoutingTemplate.WORKCENTER, routing.getWorkCenter() != null ? routing.getWorkCenter().getID() : "");
            //*************************routing template Labour mapppings***************************
                Set<RoutingTemplateLabourMapping> labourMapping = routing.getLabourmapping();
                String labourids = "";
                String labournames = "";
                for (RoutingTemplateLabourMapping Obj : labourMapping) {
                    labourids += Obj.getLabourid().getID() + ",";
                    labournames += Obj.getLabourid().getEmpcode() + ",";

                }
                if (!StringUtil.isNullOrEmpty(labourids) && !StringUtil.isNullOrEmpty(labournames)) {
                    labourids = labourids.substring(0, (labourids.length()) - 1);
                    labournames = labournames.substring(0, (labournames.length()) - 1);
                    tmpObj.put(RoutingTemplate.LABOURID, labourids);
                    tmpObj.put(RoutingTemplate.LABOURNAME, labournames);
                }

                //*************************routingtemplate Machine mapppings***************************
                Set<RoutingTemplateMachineMapping> machineMapping = routing.getMachinemapping();
                String machineids = "";
                String machinenames = "";
                for (RoutingTemplateMachineMapping Obj : machineMapping) {
                    machineids += Obj.getMachineid().getID() + ",";
                    machinenames += Obj.getMachineid().getMachineID() + ",";
                }
                if (!StringUtil.isNullOrEmpty(machineids) && !StringUtil.isNullOrEmpty(machinenames)) {
                    machineids = machineids.substring(0, (machineids.length()) - 1);
                    machinenames = machinenames.substring(0, (machinenames.length()) - 1);
                    tmpObj.put(RoutingTemplate.MACHINEID, machineids);
                    tmpObj.put(RoutingTemplate.MACHINENAME, machinenames);
                }
                if (!isforcombo) { 
                    tmpObj.put("productid", routing.getProduct() != null ? routing.getProduct().getID() : "");
                    tmpObj.put("product", routing.getProduct() != null ? routing.getProduct().getName() : "");
                    tmpObj.put(RoutingTemplate.BOMID, routing.getBomid() != null ? routing.getBomid().getID() : "");
                    tmpObj.put(RoutingTemplate.BOMNAME, routing.getBomid() != null ? routing.getBomid().getBomName() : "");
                    tmpObj.put(RoutingTemplate.WORKCENTERNAME, routing.getWorkCenter() != null ? routing.getWorkCenter().getName() : "");
                    tmpObj.put(RoutingTemplate.DURATIONTYPE, routing.getDurationType());
                    if (routing.getDurationType() == 0) {
                        tmpObj.put(RoutingTemplate.DURATIONTYPENAME, RoutingTemplate.HOURS);
                    } else if (routing.getDurationType() == 1) {
                        tmpObj.put(RoutingTemplate.DURATIONTYPENAME, RoutingTemplate.DAYS);
                    }
                    tmpObj.put(RoutingTemplate.DURATION, routing.getDuration());
                    /*
                    put cheild data
                    */
                    addChildTemplateData(tmpObj);
                }
                /*
                 Add Global Custom data for document
                 */
                Map globalMap = new HashMap();
                globalMap.put("moduleid", Constants.MRP_RouteCode);
                globalMap.put("companyid", requestParams.get(RoutingTemplate.COMPANYID));
                globalMap.put("routingtemplateid", routing.getId());
                globalMap.put(Constants.userdf, requestParams.get(Constants.userdf));
                putGlobalCustomDetailsForRoutingTemplate(tmpObj, globalMap);
                
                dataJarr.put(tmpObj);

            }
            dataObj.put("data", dataJarr);
            dataObj.put("totalCount",result.getRecordTotalCount());
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return dataObj;
    }
    /**
     *
     * @param jSONObject
     * @param map
     * @throws ServiceException
     * @throws JSONException
     * @Description : Put Custom fields data in JSON to show in Report
     */
    public void putGlobalCustomDetailsForRoutingTemplate(JSONObject jSONObject, Map<String, Object> map) throws ServiceException, JSONException {

        String companyId = "";
        int moduleid = 0;
        String routingTemplateId = "";
        if (map.containsKey("companyid")) {
            companyId = map.get("companyid").toString();
        }
        if (map.containsKey("moduleid")) {
            moduleid = Integer.parseInt(map.get("moduleid").toString());
        }
        if (map.containsKey("routingtemplateid")) {
            routingTemplateId = map.get("routingtemplateid").toString();
        }
        // ## Get Custom Field Data 
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> customFieldMap = new HashMap<>();
        HashMap<String, String> customDateFieldMap = new HashMap<>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, moduleid));
        HashMap<String, String> replaceFieldMap = new HashMap<>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
        Map<String, Object> variableMap = new HashMap<>();

        KwlReturnObject customObjresult = null;
        customObjresult = accountingHandlerDAOobj.getObject(RoutingTemplateCustomData.class.getName(), routingTemplateId);
        replaceFieldMap = new HashMap<String, String>();
        if (customObjresult != null && customObjresult.getEntityList().size() > 0) {
            RoutingTemplateCustomData routingTemplateCustomData = (RoutingTemplateCustomData) customObjresult.getEntityList().get(0);
            if (routingTemplateCustomData != null) {
                AccountingManager.setCustomColumnValues(routingTemplateCustomData, FieldMap, replaceFieldMap, variableMap);
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
     * @param tmpObj = put records in this JSON
     * @throws ServiceException
     * @throws JSONException
     */
    public void addChildTemplateData(JSONObject tmpObj) throws ServiceException, JSONException {
        Map<String, Object> requestParams = new HashMap();
        requestParams.put("parentid", tmpObj.optString(RoutingTemplate.RTID));
        String id = "";
        KwlReturnObject result = accRoutingManagementDaoObj.getChildTemplate(requestParams);
        List list = result.getEntityList();
        if (list.size() > 0) {
            id = (String) list.get(0);
        }
        if (!StringUtil.isNullOrEmpty(id)) {
            KwlReturnObject resultPayMethod = accountingHandlerDAOobj.getObject(RoutingTemplate.class.getName(), id);
            RoutingTemplate routing = (RoutingTemplate) resultPayMethod.getEntityList().get(0);
            tmpObj.put(RoutingTemplate.ALTERNATERTID, routing.getId());
            tmpObj.put(RoutingTemplate.ALTERNATEROUTING, "on");
            tmpObj.put(RoutingTemplate.ALTERNATERTNAME, routing.getName());
            tmpObj.put(RoutingTemplate.ALTERNATERTCODE, routing.getRoutecode());
            tmpObj.put(RoutingTemplate.ALTERNATEPROJECTID, routing.getProjectId());
            tmpObj.put("alternateproductid", routing.getProduct() != null ? routing.getProduct().getID() : "");
            tmpObj.put("alternateproduct", routing.getProduct() != null ? routing.getProduct().getName() : "");
            tmpObj.put(RoutingTemplate.ALTERNATEBOMID, routing.getBomid() != null ? routing.getBomid().getID() : "");
            tmpObj.put(RoutingTemplate.ALTERNATEBOMNAME, routing.getBomid() != null ? routing.getBomid().getBomName() : "");
            tmpObj.put(RoutingTemplate.ALTERNATEWORKCENTER, routing.getWorkCenter() != null ? routing.getWorkCenter().getID() : "");
            tmpObj.put(RoutingTemplate.ALTERNATEWORKCENTERNAME, routing.getWorkCenter() != null ? routing.getWorkCenter().getName() : "");
            tmpObj.put(RoutingTemplate.ALTERNATEDURATIONTYPE, routing.getDurationType());
            if (routing.getDurationType() == 0) {
                tmpObj.put(RoutingTemplate.ALTERNATEDURATIONTYPENAME, RoutingTemplate.HOURS);
            } else if (routing.getDurationType() == 1) {
                tmpObj.put(RoutingTemplate.ALTERNATEDURATIONTYPENAME, RoutingTemplate.DAYS);
            }
            tmpObj.put(RoutingTemplate.ALTERNATEDURATION, routing.getDuration());

            Set<RoutingTemplateLabourMapping> labourMapping = routing.getLabourmapping();
            String labourids = "";
            String labournames = "";
            for (RoutingTemplateLabourMapping Obj : labourMapping) {
                labourids += Obj.getLabourid().getID() + ",";
                labournames += Obj.getLabourid().getEmpcode() + ",";

            }
            if (!StringUtil.isNullOrEmpty(labourids) && !StringUtil.isNullOrEmpty(labournames)) {
                labourids = labourids.substring(0, (labourids.length()) - 1);
                labournames = labournames.substring(0, (labournames.length()) - 1);
                tmpObj.put(RoutingTemplate.ALTERNATELABOURID, labourids);
                tmpObj.put(RoutingTemplate.ALTERNATELABOURNAME, labournames);
            }

            //*************************WorkOrder Labour mapppings***************************
            //*************************WorkOrder Machine mapppings***************************
            Set<RoutingTemplateMachineMapping> machineMapping = routing.getMachinemapping();
            String machineids = "";
            String machinenames = "";
            for (RoutingTemplateMachineMapping Obj : machineMapping) {
                machineids += Obj.getMachineid().getID() + ",";
                machinenames += Obj.getMachineid().getMachineID() + ",";
            }
            if (!StringUtil.isNullOrEmpty(machineids) && !StringUtil.isNullOrEmpty(machinenames)) {
                machineids = machineids.substring(0, (machineids.length()) - 1);
                machinenames = machinenames.substring(0, (machinenames.length()) - 1);
                tmpObj.put(RoutingTemplate.ALTERNATEMACHINEID, machineids);
                tmpObj.put(RoutingTemplate.ALTERNATEMACHINENAME, machinenames);
            }
        }
    }
    public JSONObject syncResourceToPM(Map<String, Object> requestParams) throws ServiceException {
        JSONObject resObj = new JSONObject();
        String resourceId = "";
        String projectId = "";
        String resourcetype="";
        try {
            JSONArray dataJArr = new JSONArray();
            if (requestParams.containsKey("resourceId")) {
                resourceId = (String) requestParams.get("resourceId");
                String[] idArray = resourceId.split(",");
                resourceId = "";
                for (int i = 0; i < idArray.length; i++) {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("resourceid", idArray[i]);
                    if (requestParams.containsKey("projectId")) {
                        projectId = (String) requestParams.get("projectId");
                        jSONObject.put("projectid", projectId);
                    }
                    
                    dataJArr.put(jSONObject);
                }
            }
            
            String accRestURL = URLUtil.buildRestURL("pmURL");
            JSONObject userData = new JSONObject();
            String userId = "";
            String companyId = "";
            if (requestParams.containsKey("companyId")) {
                companyId = (String) requestParams.get("companyId");
            }
            if (requestParams.containsKey("userId")) {
                userId = (String) requestParams.get("userId");
            }
            userData.put("iscommit", true);
            userData.put("userid", userId);
            userData.put("companyid", companyId);
            userData.put("projectid", projectId);
            if (requestParams.containsKey("resourcetype")) {
                resourcetype = (String) requestParams.get("resourcetype");
                userData.put("resourcetype", resourcetype);
            }
            
            userData.put("inputdata", dataJArr);
            String endpoint = accRestURL + "transaction/projectresources";
            resObj = apiCallHandlerService.restPostMethod(endpoint, userData.toString());
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } 
        return resObj;
    }
    public JSONObject syncProjectCopyReqToPM(Map<String, Object> requestParams) throws ServiceException {
        JSONObject resObj = new JSONObject();
        String masterprojectid = "";
        String projectId = "";
        try {
            JSONArray dataJArr = new JSONArray();
            JSONObject jSONObject = new JSONObject();
            if (requestParams.containsKey("masterprojectid")) {
                masterprojectid = (String) requestParams.get("masterprojectid");
                jSONObject.put("masterprojectid", masterprojectid);
            }
            if (requestParams.containsKey("projectid")) {
                projectId = (String) requestParams.get("projectid");
                jSONObject.put("projectid", projectId);
            }
            dataJArr.put(jSONObject);
            String accRestURL = URLUtil.buildRestURL("pmURL");
            JSONObject userData = new JSONObject();
            String userId = "";
            String companyId = "";
            if (requestParams.containsKey("companyId")) {
                companyId = (String) requestParams.get("companyId");
            }
            if (requestParams.containsKey("userId")) {
                userId = (String) requestParams.get("userId");
            }
            userData.put("iscommit", true);
            userData.put("userid", userId);
            userData.put("isFromMRP", true);
            userData.put("companyid", companyId);
            if (requestParams.containsKey("projectid")) {
                projectId = (String) requestParams.get("projectid");
                userData.put("projectid", projectId);
            }
            if (requestParams.containsKey("masterprojectid")) {
                masterprojectid = (String) requestParams.get("masterprojectid");
                userData.put("masterprojectid", masterprojectid);
            }
            userData.put("inputdata", dataJArr);
            String endpoint = accRestURL + "master/tasksmastertoproject";
            resObj = apiCallHandlerService.restPostMethod(endpoint, userData.toString());
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } 
        return resObj;
    }   
    @Override
    public JSONObject createOrUpdateProjectRest(Map<String, Object> requestMap) {
        JSONObject jSONObject = new JSONObject();
        try {

            JSONObject json = new JSONObject();
            JSONObject userData = new JSONObject();
            userData.put("companyid", requestMap.get("companyid"));
            userData.put("userid", requestMap.get("userid"));
            
            /*
             * ERP-31857
             * If MRP not enabled in PM
             */
            userData.put("enablemrp", true);
            activateMRPInPM(userData);
            
            userData.put("projectname", "");//should be sent as blank
            userData.put("isFromMRP", true);
            if (requestMap.containsKey("projectname")) {
                userData.put("projectname",(String) requestMap.get("projectname"));
            }
            if (requestMap.containsKey("isNewProject")) {
                userData.put("isNewProject", Boolean.parseBoolean((String) requestMap.get("isNewProject")));
            }
            if (requestMap.containsKey("isMasterProject")) {
                userData.put("isMasterProject", Boolean.parseBoolean((String) requestMap.get("isMasterProject")));
            }
            if (requestMap.containsKey("projectId")) {
                userData.put("projectId", (String) requestMap.get("projectId"));
            }
              if (requestMap.containsKey("projectcode")) {
                userData.put("projectcode", (String) requestMap.get("projectcode"));
            }
            if (requestMap.containsKey(Constants.MRP_isDirtyProject)) {
                userData.put(Constants.MRP_isDirtyProject, (Boolean) requestMap.get(Constants.MRP_isDirtyProject));
            }
            /*
             * Fetching WO Status.
             */
            if (requestMap.containsKey("MRPWOStatus")) {
                userData.put("MRPWOStatus", (String) requestMap.get("MRPWOStatus"));
            }
            userData.put("isFromMRP",true);
            String accRestURL = URLUtil.buildRestURL("pmURL");   
            String endpoint = accRestURL + "transaction/projects";
            JSONObject resObj = apiCallHandlerService.restGetMethod(endpoint,userData.toString());
            if (resObj.getBoolean("success")) {
                JSONObject JObj = new JSONObject((resObj.get("data")).toString());
                if (JObj.has("projectId") == true) {
                    jSONObject.put("projectId", JObj.get("projectId"));
                    /*ERP-40195 : If duplicate finds in PM side then we update current Work Order ID.
                      The same Work Order ID is need to update in ERP side also. Moreover, if WO ID = WO Name then we update WO Name also.
                      And that name also need to update in ERP.
                    */
                    if(JObj.has("isprojectcodeupdated") && JObj.getBoolean("isprojectcodeupdated")){
                        jSONObject.put("projectcode", JObj.get("projectcode"));
                        jSONObject.put("projectname", JObj.get("projectname"));
                        jSONObject.put("isprojectcodeupdated", JObj.getBoolean("isprojectcodeupdated"));
                    }
                    jSONObject.put("success", true);
                    jSONObject.put("msg", JObj.opt("msg"));
                } else {
                    jSONObject.put("success", false);
                    jSONObject.put("msg", JObj.opt("msg"));
                }

            }else{
                jSONObject.put("success", false);
            }
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(AccRoutingManagementServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return jSONObject;
    }
    @Override
    public JSONObject deleteDirtyProjectRest(Map<String, Object> requestMap) {
        JSONObject jSONObject = new JSONObject();
        try {
            JSONObject userData = new JSONObject();
            userData.put("companyid", requestMap.get("companyid"));
            userData.put("userid", requestMap.get("userid"));
            if (requestMap.containsKey("projectId")) {
                userData.put("projectId", (String) requestMap.get("projectId"));
            }
            if (requestMap.containsKey(Constants.MRP_isDirtyProject)) {
                userData.put(Constants.MRP_isDirtyProject, (Boolean) requestMap.get(Constants.MRP_isDirtyProject));
            }
            String accRestURL = URLUtil.buildRestURL("pmURL");   
            String endpoint = accRestURL + "transaction/projects";
            JSONObject resObj = apiCallHandlerService.restDeleteMethod(endpoint,userData.toString());
            if (resObj.getBoolean("success")) {
                    jSONObject.put("success", true);
            }else{
                jSONObject.put("success", false);
            }
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(AccRoutingManagementServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return jSONObject;
    }
    /*
     * ERP-31857
     * REST Mehtod to enable MRP in PM
     * Author: Kausar
     */
    public void activateMRPInPM(JSONObject userData) {
        try {
            String accRestURL = URLUtil.buildRestURL("pmURL");
            String endpoint = accRestURL + "company/checks";
            JSONObject resObj = apiCallHandlerService.restGetMethod(endpoint, userData.toString());
        } catch (JSONException ex) {
            Logger.getLogger(AccRoutingManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccRoutingManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public JSONObject deleteRoutingTemplate(Map<String, Object> dataMap) throws ServiceException {
         List list = Collections.EMPTY_LIST;
         JSONObject returnJobj=new JSONObject();
        try {

            Boolean isPermDelete = dataMap.containsKey("isPermDelete") ? (Boolean) dataMap.get("isPermDelete") : false;
            JSONObject jobj = new JSONObject((String) dataMap.get("data"));
            JSONArray jArr = jobj.getJSONArray("root");
              KwlReturnObject result=null;
            Map<String, Object> requestParams = new HashMap();
            requestParams.put(JobWork.COMPANYID, dataMap.get(JobWork.COMPANYID));
            if (isPermDelete) {
                for (int i = 0; i < jArr.length(); i++) {
                    String id = (String) jArr.get(i);
                    if (!StringUtil.isNullOrEmpty(id)) {
                        /*
                         delete alternate Template
                         */
                        Map<String, Object> requestParams1 = new HashMap();
                        requestParams1.put("parentid", id);
                        String alternateid = "";
                        result = accRoutingManagementDaoObj.getChildTemplate(requestParams1);
                        list = result.getEntityList();
                        if (list.size() > 0) {
                            alternateid = (String) list.get(0);
                            requestParams.put(JobWork.ID, alternateid);
                            //*************delete machine mapping*********************
                            requestParams.put(WorkOrder.POJO, RoutingTemplateMachineMapping.POJONAME);
                            requestParams.put(WorkOrder.ATTRIBUTE, RoutingTemplateMachineMapping.ATTRIBUTENAME);
                            result = accRoutingManagementDaoObj.deleteRoutingTemplateMappings(requestParams);
                        //*************delete machine mapping*********************

                            //*******************delete labour mapping*******************
                            requestParams.put(WorkOrder.POJO, RoutingTemplateLabourMapping.POJONAME);
                            requestParams.put(WorkOrder.ATTRIBUTE, RoutingTemplateLabourMapping.ATTRIBUTENAME);
                            result = accRoutingManagementDaoObj.deleteRoutingTemplateMappings(requestParams);
                        //*******************delete labour mapping*******************

                            //***********finaly delete routing template
                            result = accRoutingManagementDaoObj.deleteRoutingTemplatePerm(requestParams);
                        }
                        
                        requestParams.put(JobWork.ID, id);
                        //*************delete machine mapping*********************
                        requestParams.put(WorkOrder.POJO, RoutingTemplateMachineMapping.POJONAME);
                        requestParams.put(WorkOrder.ATTRIBUTE, RoutingTemplateMachineMapping.ATTRIBUTENAME);
                        result = accRoutingManagementDaoObj.deleteRoutingTemplateMappings(requestParams);
                        //*************delete machine mapping*********************
                        
                        //*******************delete labour mapping*******************
                        requestParams.put(WorkOrder.POJO, RoutingTemplateLabourMapping.POJONAME);
                        requestParams.put(WorkOrder.ATTRIBUTE, RoutingTemplateLabourMapping.ATTRIBUTENAME);
                        result = accRoutingManagementDaoObj.deleteRoutingTemplateMappings(requestParams);
                        //*******************delete labour mapping*******************
                        
                        //*********** delete routing template custom data
                        result = accRoutingManagementDaoObj.deleteRoutingTemplateCustomData(requestParams);
                        
                        //***********finaly delete routing template
                        result = accRoutingManagementDaoObj.deleteRoutingTemplatePerm(requestParams);
                    }

                }
            }else {
                KwlReturnObject kwlObj = null;
                RoutingTemplate routingObj = null;
                for (int i = 0; i < jArr.length(); i++) {
                    String id = (String) jArr.get(i);
                    kwlObj = accountingHandlerDAOobj.getObject(RoutingTemplate.class.getName(), id);
                    routingObj = (RoutingTemplate) kwlObj.getEntityList().get(0);
                    routingObj.setDeleted(true);
                }

            } 

        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccRoutingManagementServiceImpl.deleteRoutingTemplate", ex);
        }
        return returnJobj;
    }

    @Override
    public boolean isROutingTemplateNameAlreadyExist(Map<String, Object> requestParams) throws ServiceException {
        boolean isROutingTemplatenameAlredyPresent = false;
        try {
            KwlReturnObject result = accRoutingManagementDaoObj.getRoutingTemplates(requestParams);
            List rtList = result.getEntityList();
            if (rtList.size() > 0) {
                isROutingTemplatenameAlredyPresent = true;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(null, ex);
        }
        return isROutingTemplatenameAlredyPresent;
    }
}
