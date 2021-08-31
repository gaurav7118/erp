/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.workcentremanagement;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.CustomizeReportMapping;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.mrp.WorkOrder.AccWorkOrderServiceDAO;
import com.krawler.spring.mrp.WorkOrder.WorkOrder;
import com.krawler.spring.mrp.WorkOrder.WorkOrderDAO;
import com.krawler.spring.mrp.labormanagement.LabourWorkCentreMapping;
import com.krawler.spring.mrp.machinemanagement.*;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.JSONException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AccWorkCentreServiceImpl implements AccWorkCentreServiceDAO {
    private MessageSource messageSource;
    private WorkCentreDAO workCentreDAOObj;
    private accCompanyPreferencesDAO accCompanyPreferencesDAOObj;
    private exportMPXDAOImpl exportDaoObj;
    private fieldDataManager fieldDataManagercntrl;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accAccountDAO accAccountDAOobj;
    private auditTrailDAO auditTrailObj;
    
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

    public void setWorkCentreDAOObj(WorkCentreDAO workCentreDAOObj) {
        this.workCentreDAOObj = workCentreDAOObj;
    }

    public void setAccCompanyPreferencesDAOObj(accCompanyPreferencesDAO accCompanyPreferencesDAOObj) {
        this.accCompanyPreferencesDAOObj = accCompanyPreferencesDAOObj;
    }

    public void setExportDaoObj(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    @Override
    public KwlReturnObject saveWorkCentre(Map<String, Object> requestParams) throws AccountingException, ServiceException {
        KwlReturnObject kwlRetObj = null;
        KwlReturnObject kmsg = null;
        KwlReturnObject kmsg1 = null;
        JSONObject jObj = null;
        Locale requestcontextutilsobj = null;
        boolean isNASeqFrmtDupExe = false;
        String msg = "", auditMsg = "";
        WorkCentre workCentre = null;
        try {
            
            HttpServletRequest request = (HttpServletRequest) requestParams.get(Constants.RES_REQUEST);
            if (requestParams.containsKey("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
            }
            String companyid = requestParams.get("companyid").toString();
            String userid = requestParams.get("userid").toString();
            boolean isEdit = Boolean.parseBoolean(requestParams.get("isEdit").toString());
            boolean isWorkCentreIDAlreadyPresent = false;
            String sequenceformat = "", nextAutoNumber = "";
            String codeStr = "";
            JSONObject dataJobj = new JSONObject(StringUtil.DecodeText(requestParams.get("data").toString()));
            JSONArray dataArr = dataJobj.getJSONArray("data");
            for (int recCount = 0; recCount < dataArr.length(); recCount++) {
                JSONObject dataObj = dataArr.getJSONObject(recCount);
                dataObj.put(WorkCentre.COMPANYID, companyid);
                dataObj.put(WorkCentre.CREATEDBYID, userid);
                dataObj.put(WorkCentre.MODIFIEDBYID, userid);
                String entrynumber = dataObj.getString(WorkCentre.WORKCENTREID);
                sequenceformat = dataObj.getString(WorkCentre.SEQUENCEFORMAT);
                dataObj.put("isEdit", isEdit);
                String wcid = dataObj.optString("id","");
                synchronized (this) {

                    HashMap<String, Object> requestParams1 = new HashMap();
                    requestParams1.put("companyid", companyid);
                    
                    requestParams1.put(WorkCentre.WORKCENTREID, dataObj.getString(WorkCentre.WORKCENTREID));
                    if (isEdit && !StringUtil.isNullOrEmpty(wcid) && sequenceformat.equals("NA")) {
                        requestParams1.put("wcid", wcid);
                    }
                    kmsg1 = workCentreDAOObj.getWorkCentres(requestParams1);
                    if (kmsg1.getEntityList().size() > 0) {
                        isWorkCentreIDAlreadyPresent = true;
                    }
                    if (isWorkCentreIDAlreadyPresent) {
                        if (!isEdit) {

                            if (sequenceformat.equals("NA")) {
                                throw new AccountingException(messageSource.getMessage("acc.field.workCenter", null, requestcontextutilsobj) + " '<b>" + entrynumber + "</b> " + messageSource.getMessage("acc.field.alreadyexists.", null, requestcontextutilsobj));
                            }

                        } else {
                            if (sequenceformat.equals("NA")) {
                                throw new AccountingException(messageSource.getMessage("acc.field.workCenter", null, requestcontextutilsobj) + " '<b>" + entrynumber + "</b> " + messageSource.getMessage("acc.field.alreadyexists.", null, requestcontextutilsobj));
                            }
                            nextAutoNumber = entrynumber;

                        }

                    } else {
                        boolean seqformat_oldflag = StringUtil.getBoolean(dataObj.optString("seqformat_oldflag", "false"));
                        String nextAutoNoInt = "";
                        String datePrefix = "";
                        String dateafterPrefix = "";
                        String dateSuffix = "";
                        if (!sequenceformat.equals("NA")) {
                            if (seqformat_oldflag) {
                                nextAutoNumber = accCompanyPreferencesDAOObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_MRP_WORKCENTRE, sequenceformat);
                            } else {
                                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                seqNumberMap = accCompanyPreferencesDAOObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_MRP_WORKCENTRE, sequenceformat, seqformat_oldflag, null);// No creation date in UI hence sending null
                                nextAutoNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                                nextAutoNoInt = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                                datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                                dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                                dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part

                                dataObj.put(Constants.SEQFORMAT, sequenceformat);
                                dataObj.put(Constants.SEQNUMBER, nextAutoNoInt);
                                dataObj.put(Constants.DATEPREFIX, datePrefix);
                                dataObj.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                                dataObj.put(Constants.DATESUFFIX, dateSuffix);
                            }
                            entrynumber = nextAutoNumber;
                        }

                    }

                    if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                        List list = accCompanyPreferencesDAOObj.checksEntryNumberForSequenceNumber(Constants.MRP_WORK_CENTRE_MODULEID, entrynumber, companyid);
                        if (!list.isEmpty()) {
                            boolean isvalidEntryNumber = (Boolean) list.get(0);
                            String formatName = (String) list.get(1);
                            if (!isvalidEntryNumber) {
                                throw new AccountingException("#DuplicateException#"+messageSource.getMessage("acc.common.enterdocumentnumber", null, requestcontextutilsobj) + " <b>" + entrynumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, requestcontextutilsobj) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, requestcontextutilsobj) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, requestcontextutilsobj));
                            }
                        }
                    }

                }
                dataObj.put(WorkCentre.WORKCENTREID, entrynumber);
                dataObj.put("autogenerated", nextAutoNumber.equals(entrynumber));
                kmsg = workCentreDAOObj.saveWorkCentre(dataObj);
                workCentre =(WorkCentre) kmsg.getEntityList().get(0);

                Map<String, Object> mappingParams = new HashMap<String, Object>();
                String labourid = "";
                if (dataObj.has(WorkCentre.LABOURID)) {
                    labourid = dataObj.getString(WorkCentre.LABOURID);
                }
                if (!StringUtil.isNullOrEmpty(labourid)) {
                    mappingParams.put(WorkCentre.LABOURID, labourid);
                }
                String machineid = "";
                if (dataObj.has(workCentre.MACHINEID)) {
                    machineid = dataObj.getString(WorkCentre.MACHINEID);
                }
                if (!StringUtil.isNullOrEmpty(machineid)) {
                    mappingParams.put(WorkCentre.MACHINEID, machineid);
                }
                String productid = "";
                if (dataObj.has(workCentre.PRODUCTID)) {
                    productid = dataObj.getString(WorkCentre.PRODUCTID);
                }
                if (!StringUtil.isNullOrEmpty(productid)) {
                    mappingParams.put(WorkCentre.PRODUCTID, productid);
                }
                String materialid = "";
                if (dataObj.has(workCentre.MATERIALID)) {
                    materialid = dataObj.getString(WorkCentre.MATERIALID);
                }
                if (!StringUtil.isNullOrEmpty(materialid)) {
                    mappingParams.put(WorkCentre.MATERIALID, materialid);
                }
                mappingParams.put(WorkCentre.COMPANYID, companyid);
                mappingParams.put(WorkCentre.WCID, workCentre.getID());
                mappingParams.put("isEdit", isEdit);
                saveMappingsForWorkCenter(mappingParams);

                /*
                 Save Custom Field Data
                 */
                boolean hasCustomField = dataObj.has("customfield");
                if (hasCustomField) {
                    JSONArray customfield = new JSONArray(dataObj.getString("customfield"));
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", customfield);
                    customrequestParams.put("modulename", Constants.Acc_MRPWorkCentre_Modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_MRPWorkCentre_Id);
                    customrequestParams.put("modulerecid", workCentre.getID());
                    customrequestParams.put("moduleid", Constants.MRP_WORK_CENTRE_MODULEID);
                    customrequestParams.put("companyid", companyid);
                    customrequestParams.put("customdataclasspath", Constants.Acc_MRPWorkCentre_CustomData_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        dataObj.put("accworkcentrecustomdataref", workCentre.getID());
                        dataObj.put("id", workCentre.getID());
                        dataObj.put("isEdit", true);        //Update Work Centre when Custom Field
                        kmsg = workCentreDAOObj.saveWorkCentre(dataObj);
                    }
                }
            }
            List list = new ArrayList();
            if (isEdit) {
                msg = messageSource.getMessage("mrp.workcentre.form.updatefullysavedmsg", null, requestcontextutilsobj);    //Work Centre has been updated successfully.
                auditMsg = " User " + sessionHandlerImpl.getUserFullName(request) + " has updated Work Centre  <b>" + workCentre.getWorkcenterid() + "</b>";
            } else {
                msg = messageSource.getMessage("mrp.workcentre.form.successfullysavedmsg", null, requestcontextutilsobj);   //Work Centre has been saved successfully.
                auditMsg = " User " + sessionHandlerImpl.getUserFullName(request) + " has added new Work Centre  <b>" + workCentre.getWorkcenterid() + "</b>";
            }
            auditTrailObj.insertAuditLog(AuditAction.WORKCENTRE_MANAGEMENT, auditMsg, request, workCentre.getWorkcenterid());
            msg += "<br/>" + messageSource.getMessage("acc.mrp.field.WorkCentreID", null, requestcontextutilsobj) + ": <b>" + workCentre.getWorkcenterid() + "</b>";
            list.add(msg);
            kwlRetObj = new KwlReturnObject(true, msg, null, list, 0);
        } catch (com.krawler.utils.json.base.JSONException | ServiceException  | SessionExpiredException jex) {
             Logger.getLogger(AccWorkCentreServiceImpl.class.getName()).log(Level.SEVERE, null, jex);
        }
        return kwlRetObj;
    }
    
    
    public JSONObject saveMappingsForWorkCenter(Map<String, Object> mappingParams) {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsgLab = null;
        KwlReturnObject kmsgMac = null;
        KwlReturnObject kmsgProd = null;
        KwlReturnObject kmsgMat = null;
        boolean isEdit = false;
        try {
            JSONObject dataObj = new JSONObject();
            isEdit = Boolean.parseBoolean(mappingParams.get("isEdit").toString());
            dataObj.put("isEdit", true);
            dataObj.put(WorkCentre.WCID, mappingParams.get(WorkCentre.WCID));
            if (isEdit) {
                Map<String, Object> deleteParams = new HashMap<>();
                deleteParams.put(WorkCentre.WCID, mappingParams.get(WorkCentre.WCID));
                kmsgLab = workCentreDAOObj.deletelabourWorkCentreMappings(deleteParams);
                kmsgMac = workCentreDAOObj.deleteMachineWorkCentreMappings(deleteParams);
                kmsgProd = workCentreDAOObj.deleteProductWorkCentreMappings(deleteParams);
                kmsgMat = workCentreDAOObj.deleteMaterialWorkCentreMappings(deleteParams);
            }
            Set<LabourWorkCentreMapping> labourWorkCentreMapping = workCentreDAOObj.getLabourWCMapping(mappingParams);
            mappingParams.put(WorkCentre.LABOURWCMAP, labourWorkCentreMapping);
            Set<MachineWorkCenterMapping> machineWorkCentreMapping = workCentreDAOObj.getMachineWCMapping(mappingParams);
            mappingParams.put(WorkCentre.MACHINEWCMAP, machineWorkCentreMapping);
            Set<ProductWorkCentreMapping> productWorkCentreMapping = workCentreDAOObj.getProductWCMapping(mappingParams);
            mappingParams.put(WorkCentre.PRODUCTWCMAP, productWorkCentreMapping);
            Set<MaterialWorkCentreMapping> materialWorkCentreMapping = workCentreDAOObj.getMaterialWCMapping(mappingParams);
            mappingParams.put(WorkCentre.MATERIALWCMAP, materialWorkCentreMapping);
            workCentreDAOObj.saveWorkCentreMappings(mappingParams);
                
        } catch(Exception ex) {
            Logger.getLogger(AccWorkCentreServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

    @Override
    public JSONObject getWorkCentreDataandColumnModel(Map<String, Object> requestParams) {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        JSONObject commData = new JSONObject();
        JSONObject dataJobj = new JSONObject();
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        Locale requestcontextutilsobj = null;
        String storeRec = "";
        String start = "", limit = "";
        try {
            if (requestParams.containsKey("start")) {
                start = requestParams.get("start").toString();
            }
            if (requestParams.containsKey("limit")) {
                limit = requestParams.get("limit").toString();
            }

            Boolean isExport = false;
            if (requestParams.containsKey("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
            }
            if (requestParams.containsKey("isExport") && requestParams.get("isExport") != null) {
                isExport = (Boolean) requestParams.get("isExport");
            }

            //***************************Data**********************************************
            dataJobj = createWorkCentreJSONArr(requestParams);
            dataJArr = dataJobj.getJSONArray("data");

            //***************************Data**********************************************
            //*******************Record****************************
            JSONObject rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.WCID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.SEQUENCEFORMAT);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.WORKCENTRENAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.WORKCENTREID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.WORKCENTERLOCATION);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.WORKCENTERLOCATIONID);
            jarrRecords.put(rec);
//            rec = new JSONObject();
//            rec.put(WorkCentre.KEY, WorkCentre.WORKCENTRECAPACITY);
//            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.WORKTYPE);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.WORKTYPEID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.WORKCENTREMANAGER);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.WORKCENTREMANAGERID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.COSTCENTER);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.COSTCENTERID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.CREATEDBY);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.CREATEDBYID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.WORKCENTRETYPE);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.WORKCENTRETYPEID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.MODIFIEDBYID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.MODIFIEDBYNAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.WAREHOUSE);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.WAREHOUSEID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.PRODUCTID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.PRODUCTNAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.LABOURID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.LABOURNAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.MACHINEID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.MACHINENAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.MATERIALID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, WorkCentre.MATERIALNAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(WorkCentre.KEY, "deleted");
            jarrRecords.put(rec);
            //*******************Record****************************

            //*****************ColumnModel******************************8
            JSONObject tmp = new JSONObject();
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workcentre.report.header1", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkCentre.WORKCENTRENAME);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workcentre.report.header2", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkCentre.WORKCENTREID);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workcentre.report.header3", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkCentre.WAREHOUSE);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workcentre.report.header4", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkCentre.WORKCENTERLOCATION);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workcentre.report.header5", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex",  WorkCentre.WORKTYPE);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workcentre.report.header9", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkCentre.PRODUCTNAME);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workcentre.report.header11", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkCentre.LABOURNAME);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workcentre.report.header12", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkCentre.MACHINENAME);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workcentre.report.header6", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkCentre.COSTCENTER);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

         /**
           * (ERP-37673)
           * Remove workCentreCapacity column from Work Center Master.
           */
//            jobjTemp = new JSONObject();
//            jobjTemp.put("header", messageSource.getMessage("acc.masterConfigWC.39", null, requestcontextutilsobj));
//            jobjTemp.put("dataIndex", WorkCentre.WORKCENTRECAPACITY);
//            jobjTemp.put("width", 150);
//            jobjTemp.put("pdfwidth", 150);
//            jobjTemp.put("sortable", true);
//            jarrColumns.put(jobjTemp);
//          
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.workcentre.report.header8", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", WorkCentre.WORKCENTREMANAGER);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
            
            /*
             Add Custom Fields in Column Model
             */
            requestParams.put("reportId", Constants.MRP_WORK_CENTRE_MODULEID);
            putCustomColumnForWorkCentre(jarrColumns, jarrRecords, requestParams);
            
            
//*****************ColumnModel******************************
            
            
            commData.put("success", true);
            commData.put("coldata", dataJArr);
            commData.put("totalCount", dataJobj.getInt("totalCount"));
            commData.put("columns", jarrColumns);
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
            Logger.getLogger(AccWorkCentreServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    public void putCustomColumnForWorkCentre(JSONArray jarrColumns, JSONArray jarrRecords, Map<String, Object> requestParams) throws ServiceException {
        try {
            HashMap<String, Object> requestParams1 = new HashMap<>(requestParams);
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
        } catch (com.krawler.utils.json.base.JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    
    public JSONObject createWorkCentreJSONArr(Map<String, Object> requestParams) throws  ServiceException {
        JSONObject dataJobj =  new JSONObject();
        JSONArray dataJArr = new  JSONArray();
        try{
            KwlReturnObject result = workCentreDAOObj.getWorkCentres(requestParams);
            List dataList = result.getEntityList();
            WorkCentre workCentre = null;
            JSONObject tmpObj = new JSONObject();
            Set<ProductWorkCentreMapping> productWCmappingSet = null;
            ProductWorkCentreMapping productWCmapping = null;
            Set<LabourWorkCentreMapping> labourWCmappingSet = null;
            LabourWorkCentreMapping labourWCmapping = null;
            Set<MachineWorkCenterMapping> machineWCmappingSet = null;
            MachineWorkCenterMapping machineWCmapping = null;
            Set<MaterialWorkCentreMapping> materialWCmappingSet = null;
            MaterialWorkCentreMapping materialWCmapping = null;
            String productid = "";
            String productname = "";
            String labourid = "";
            String labourname = "";
            String machineid = "";
            String machinename = "";
            String materialid = "";
            String materialname = "";
            Iterator ite = null;
            for (Object obj : dataList) {
                workCentre = (WorkCentre) obj;
                tmpObj = new JSONObject();

                tmpObj.put(WorkCentre.WCID, workCentre.getID());
                tmpObj.put(WorkCentre.SEQUENCEFORMAT, workCentre.getSeqformat() != null ? workCentre.getSeqformat().getID() : "");
                tmpObj.put(WorkCentre.WORKCENTRENAME, workCentre.getName());
                tmpObj.put(WorkCentre.WORKCENTREID, workCentre.getWorkcenterid());
                tmpObj.put(WorkCentre.WORKCENTERLOCATIONID, workCentre.getWorkcenterlocation() != null ?workCentre.getWorkcenterlocation().getID() :"");
                tmpObj.put(WorkCentre.WORKCENTERLOCATION, workCentre.getWorkcenterlocation() != null ?workCentre.getWorkcenterlocation().getValue() :"");
            //  tmpObj.put(WorkCentre.WORKCENTRECAPACITY, workCentre.getWorkcentercapacity());
                tmpObj.put(WorkCentre.WORKTYPE, workCentre.getWorktype() != null ? workCentre.getWorktype().getValue() : "");
                tmpObj.put(WorkCentre.WORKTYPEID, workCentre.getWorktype() != null ? workCentre.getWorktype().getID(): "");
                tmpObj.put(WorkCentre.WORKCENTREMANAGER, workCentre.getWorkcentermanager() != null ? workCentre.getWorkcentermanager().getValue(): "");
                tmpObj.put(WorkCentre.WORKCENTREMANAGERID, workCentre.getWorkcentermanager() != null ? workCentre.getWorkcentermanager().getID(): "");
                tmpObj.put(WorkCentre.COSTCENTER, workCentre.getCostcenter() != null ? workCentre.getCostcenter().getName(): "");
                tmpObj.put(WorkCentre.COSTCENTERID, workCentre.getCostcenter() != null ? workCentre.getCostcenter().getID() : "");
                tmpObj.put(WorkCentre.WAREHOUSE, workCentre.getWarehouseid() != null ? workCentre.getWarehouseid().getName() : "");
                tmpObj.put(WorkCentre.WAREHOUSEID, workCentre.getWarehouseid() != null ? workCentre.getWarehouseid().getId() : "");
//                tmpObj.put(workOrder.PRODUCTID, "");
//                tmpObj.put(workOrder.PRODUCTNAME, "");
                tmpObj.put(WorkCentre.CREATEDBY, workCentre.getCreatedby() != null ? workCentre.getCreatedby().getFullName():"");
                tmpObj.put(WorkCentre.CREATEDBYID, workCentre.getCreatedby() != null ? workCentre.getCreatedby().getUserID(): "");
                tmpObj.put(WorkCentre.WORKCENTRETYPE, workCentre.getWorkcentertype()!= null ? workCentre.getWorkcentertype().getValue(): "");
                tmpObj.put(WorkCentre.WORKCENTRETYPEID, workCentre.getWorkcentertype() != null ? workCentre.getWorkcentertype().getID(): "");
                tmpObj.put(WorkCentre.MODIFIEDBYID, workCentre.getModifiedby() != null ? workCentre.getModifiedby().getUserID(): "");
                tmpObj.put(WorkCentre.MODIFIEDBYNAME, workCentre.getModifiedby() != null ? workCentre.getModifiedby().getFullName(): "");
                tmpObj.put("deleted", workCentre.isDeleted());
                productWCmappingSet = workCentre.getProductworkcentremappings();
                ite = productWCmappingSet.iterator();
                productid = "";
                productname = "";
                while (ite.hasNext()) {
                    productWCmapping = (ProductWorkCentreMapping) ite.next();
                    if (ite.hasNext()) {
                        productname += productWCmapping.getProductid().getProductName()+",";
                        productid += productWCmapping.getProductid().getID()+",";
                    } else {
                        productname += productWCmapping.getProductid().getProductName();
                        productid += productWCmapping.getProductid().getID();
                    }
                }
                tmpObj.put(WorkCentre.PRODUCTID, productid);
                tmpObj.put(WorkCentre.PRODUCTNAME, productname);
                
                labourWCmappingSet = workCentre.getLabourworkcentremappings();
                ite = labourWCmappingSet.iterator();
                labourid = "";
                labourname = "";
                while (ite.hasNext()) {
                    labourWCmapping = (LabourWorkCentreMapping) ite.next();
                    if (ite.hasNext()) {
                        labourname += labourWCmapping.getLabour().getFname() + " "+ labourWCmapping.getLabour().getLname()+",";
                        labourid += labourWCmapping.getLabour().getID() + ",";
                    } else {
                        labourname += labourWCmapping.getLabour().getFname() + " "+ labourWCmapping.getLabour().getLname();
                        labourid += labourWCmapping.getLabour().getID();
                    }
                }
                tmpObj.put(WorkCentre.LABOURID, labourid);
                tmpObj.put(WorkCentre.LABOURNAME, labourname);
                
                machineWCmappingSet = workCentre.getMachineworkcentremappings();
                ite = machineWCmappingSet.iterator();
                machineid = "";
                machinename = "";
                while (ite.hasNext()) {
                    machineWCmapping = (MachineWorkCenterMapping) ite.next();
                    if (ite.hasNext()) {
                        machinename += machineWCmapping.getMachineID().getMachineName() +",";
                        machineid += machineWCmapping.getMachineID().getID() + ",";
                    } else {
                        machinename += machineWCmapping.getMachineID().getMachineName();
                        machineid += machineWCmapping.getMachineID().getID();
                    }
                }
                tmpObj.put(WorkCentre.MACHINEID, machineid);
                tmpObj.put(WorkCentre.MACHINENAME, machinename);
                
                
                materialWCmappingSet = workCentre.getMaterialworkcentremappings();
                ite = materialWCmappingSet.iterator();
                materialid = "";
                materialname = "";
                while (ite.hasNext()) {
                    materialWCmapping = (MaterialWorkCentreMapping) ite.next();
                    if (ite.hasNext()) {
                        materialname += materialWCmapping.getBomid().getBomName() +",";
                        materialid += materialWCmapping.getBomid().getID() + ",";
                    } else {
                        materialname += materialWCmapping.getBomid().getBomName();
                        materialid += materialWCmapping.getBomid().getID();
                    }
                }
                tmpObj.put(WorkCentre.MATERIALID, materialid);
                tmpObj.put(WorkCentre.MATERIALNAME, materialname);
                
                /*
                 Add Global Custom data
                 */
                String companyId = (String) requestParams.get("companyid");
                Map globalMap = new HashMap();
                globalMap.put("moduleid", Constants.MRP_WORK_CENTRE_MODULEID);
                globalMap.put("companyid", companyId);
                globalMap.put("workcentreid", workCentre.getID());
                globalMap.put(Constants.userdf, requestParams.get(Constants.userdf));
                putGlobalCustomDetailsForWorkCentre(tmpObj, globalMap);
                
                dataJArr.put(tmpObj);
            }
            dataJobj.put("totalCount",result.getRecordTotalCount());
            dataJobj.put("data",dataJArr);
        }catch(Exception ex) {
            Logger.getLogger(AccWorkCentreServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return  dataJobj;
    }
    
    public void putGlobalCustomDetailsForWorkCentre(JSONObject jSONObject, Map<String, Object> map) throws ServiceException, com.krawler.utils.json.base.JSONException {

        String companyId = "";
        int moduleid = 0;
        String workCentreId = "";
        if (map.containsKey("companyid")) {
            companyId = map.get("companyid").toString();
        }
        if (map.containsKey("moduleid")) {
            moduleid = Integer.parseInt(map.get("moduleid").toString());
        }
        if (map.containsKey("workcentreid")) {
            workCentreId = map.get("workcentreid").toString();
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
        customObjresult = accountingHandlerDAOobj.getObject(WorkCentreCustomData.class.getName(), workCentreId);
        replaceFieldMap = new HashMap<String, String>();
        if (customObjresult != null && customObjresult.getEntityList().size() > 0) {
            WorkCentreCustomData workCentreCustomData = (WorkCentreCustomData) customObjresult.getEntityList().get(0);
            if (workCentreCustomData != null) {
                AccountingManager.setCustomColumnValues(workCentreCustomData, FieldMap, replaceFieldMap, variableMap);
                JSONObject params = new JSONObject();
                params.put("companyid", companyId);
                params.put("isExport", true);
                params.put(Constants.userdf, map.get(Constants.userdf));
                fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
            }
        }
    }

    @Override
    public KwlReturnObject deleteWorkCentres(Map<String, Object> dataMap) throws ServiceException {
        List list=Collections.EMPTY_LIST;
        try{
            JSONObject jobj=new JSONObject((String)dataMap.get("data"));
            JSONArray jArr=jobj.getJSONArray("root");
            
            Map<String,Object>requestParams=new HashMap();
            requestParams.put(WorkOrder.COMPANYID,dataMap.get(WorkOrder.COMPANYID));
            for (int i = 0; i < jArr.length(); i++) {
                String id = (String) jArr.get(i);
                if (!StringUtil.isNullOrEmpty(id)) {
                    requestParams.put(WorkCentre.WCID, id);
                    KwlReturnObject result = workCentreDAOObj.deleteWorkCentres(requestParams);
                }

            }
            
            
        }catch(Exception ex){
         throw ServiceException.FAILURE("AccJobWorkServiceImpl.deleteJobWork", ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public JSONArray getWCCombo(Map<String, Object> map) throws ServiceException {
        JSONArray jArr = new JSONArray();
        JSONObject jSONObject = new JSONObject();
        KwlReturnObject result = workCentreDAOObj.getWorkCentreCombo(map);
        List list = result.getEntityList();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            try {
                jSONObject = new JSONObject();
                Object obj[] = (Object[]) itr.next();
                jSONObject.put("id", (String) obj[0]);
                jSONObject.put("name", (String) obj[1]);
                jSONObject.put("wcid", (String) obj[2]);
                jArr.put(jSONObject);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                throw ServiceException.FAILURE(ex.getMessage(), ex);
            }
        }
        return jArr;
    }
    @Override
    public JSONObject deleteWorkcentre(Map<String, Object> requestParams) throws ServiceException {

        JSONObject jobj = new JSONObject();
        KwlReturnObject returnResult=null;
        String [] arrayOfID=null;
        String workCenter = "";
        String msg = "";
        HttpServletRequest request = (HttpServletRequest) requestParams.get(Constants.RES_REQUEST);
        try {
            
            if(requestParams.containsKey("idsfordelete")&& requestParams.get("idsfordelete")!=null){
                requestParams.put("istempdelete", true);
                arrayOfID=(String[])requestParams.get("idsfordelete");
                for(int count=0 ;count<arrayOfID.length;count++){
                    JSONObject jsobj = deleteWorkCentrePermanently(requestParams);  
                    if(jsobj.has("isdelete") && jsobj.optBoolean("isdelete", false)){   
                    requestParams.put(WorkCentre.WCID, arrayOfID[count]);
                    KwlReturnObject result = accountingHandlerDAOobj.getObject(WorkCentre.class.getName(), arrayOfID[count]);
                    WorkCentre workCentre = (WorkCentre) result.getEntityList().get(0);
                    workCenter += "<b>" + workCentre.getWorkcenterid() + "</b>";
                    returnResult = workCentreDAOObj.deleteWorkCentre(requestParams);
                    msg = returnResult.getMsg();
                    auditTrailObj.insertAuditLog(AuditAction.WORKCENTRE_MANAGEMENT, " User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Work Centre "  + workCenter, request, arrayOfID[count]);
                }else {
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
    @Override
    public JSONObject deleteWorkCentrePermanently(Map<String, Object> requestParams) throws ServiceException {

        JSONObject jobj = new JSONObject();
        Machine machineObj = null;
        List list = null;
        String[] arrayOfID = null;
        String linkedTransaction = "", workCenter = "";
        Locale requestcontextutilsobj = null;
        KwlReturnObject returnResult = null;
        StringBuffer workCentreCodeString=new StringBuffer();
        boolean isworkCentreInUse=false, istempdelete=false;
        Set<SubstituteMachineMapping> activeMachineMappingDetails = null;
        HttpServletRequest request = (HttpServletRequest) requestParams.get(Constants.RES_REQUEST);
        try {
            if (requestParams.containsKey("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
            }
            
            // - Check whether it is Temporary delete or permanent delete.
            if (requestParams.containsKey("istempdelete") && requestParams.get("istempdelete")!=null) {
                istempdelete = (Boolean) requestParams.get("istempdelete");
            }
            
            if (requestParams.containsKey("idsfordelete") && requestParams.get("idsfordelete") != null) {
                arrayOfID = (String[]) requestParams.get("idsfordelete");
                                
                for (int count = 0; count < arrayOfID.length; count++) {
                    String workCentreId = arrayOfID[count];
                    int usageCout=0;
                    requestParams.put("id", workCentreId);
                    KwlReturnObject result = accountingHandlerDAOobj.getObject(WorkCentre.class.getName(), arrayOfID[count]);
                    WorkCentre workCentre = (WorkCentre) result.getEntityList().get(0);
                    workCenter += "<b>" + workCentre.getWorkcenterid() + "</b>";
                    
                     if (!StringUtil.isNullOrEmpty(workCentreId)) {
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("id", workCentreId);
                        dataMap.put("workcentreid", workCentreId);
                        
                        // CHecking For Work Orders
                        KwlReturnObject resultWO =workCentreDAOObj.getWOforWorkCentre(dataMap);
                        int WOcount = resultWO.getRecordTotalCount();
                        if (WOcount > 0) {
                            usageCout++;
//                            linkedTransaction += workCentreId + ", ";
//                            continue;
                        }
                        
                         // CHecking For Routing Templates
                        KwlReturnObject resultRT = workCentreDAOObj.getRTforWorkCentre(dataMap);
                        int RTcount = resultRT.getRecordTotalCount();
                        if (RTcount > 0) {
                            usageCout++;
//                            linkedTransaction += machineCode + ", ";
//                            continue;
                        }
                     }
                     if (usageCout > 0) {
                        isworkCentreInUse=true;
                        workCentreCodeString.append(" ");
                        workCentreCodeString.append(workCentre.getWorkcenterid());
                        workCentreCodeString.append(",");
                        continue;
                    }
                    if (!istempdelete) { 
                    returnResult = workCentreDAOObj.deleteMachineWorkCentreMappings(requestParams);
                    returnResult = workCentreDAOObj.deleteMaterialWorkCentreMappings(requestParams);
                    returnResult = workCentreDAOObj.deleteProductWorkCentreMappings(requestParams);
                    returnResult = workCentreDAOObj.deletelabourWorkCentreMappings(requestParams);
                    returnResult = workCentreDAOObj.deleteWorkCentreCustomData(requestParams);
                    returnResult = workCentreDAOObj.deleteWorkCentrePermanently(requestParams);
                    
                    auditTrailObj.insertAuditLog(AuditAction.WORKCENTRE_MANAGEMENT, " User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Work Centre Permanently " + workCenter, request, arrayOfID[count]);
                }
               }
            }
            
            if (!isworkCentreInUse) {
                /*If labour is tagged in work order, work centre and routing template  islabourInUse flag is true otherwise false */
                jobj.put("msg", returnResult != null ? returnResult.getMsg() : "Some error has been occured. Please try again later.");
                jobj.put("isdelete", true); 
            } else {
                String workCentre = workCentreCodeString.toString();
                if (workCentre.length() > 0) {
                    workCentre = workCentre.substring(0, workCentre.length() - 1);
                }
                jobj.put("msg", messageSource.getMessage("acc.workcentreMaster.deleteMsg", null, requestcontextutilsobj) + "<B>" + workCentre + "</B>" + " " + messageSource.getMessage("acc.field.cannnotdelete", null, requestcontextutilsobj));
                jobj.put("isdelete", false);
            }
//            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
//                jobj.put("msg", returnResult.getMsg());
//            } else {
//                jobj.put("msg", messageSource.getMessage("acc.machineMaster.machineExcept", null, requestcontextutilsobj)+ "<B>" +linkedTransaction.substring(0, linkedTransaction.length() - 2)+"</B>"+" "+messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, requestcontextutilsobj));
//            }
        } catch (Exception ex) {
            Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    
    @Override
    public JSONObject exportWorkCentre(Map<String, Object> requestParams) throws ServiceException {
        
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        JSONObject dataJobj = new JSONObject();
        HttpServletRequest request=null;
        HttpServletResponse response=null;
        try {
            
            dataJobj=createWorkCentreJSONArr(requestParams);
            jobj.put("data", dataJobj.getJSONArray("data"));
            request=(HttpServletRequest)requestParams.get("request");
            response=(HttpServletResponse)requestParams.get("response");
            exportDaoObj.processRequest(request, response, jobj);
            
        } catch (Exception ex) {
            Logger.getLogger(AccMachineManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }

    @Override
    public JSONObject getWorkcentresForCombo(Map<String, Object> requestParms) throws ServiceException {
         List list = Collections.EMPTY_LIST;
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            KwlReturnObject kwlResult = workCentreDAOObj.getWorkCentres(requestParms);
            List dataList = kwlResult.getEntityList();
            WorkCentre workcenter = null;
            JSONObject tmpObj = new JSONObject();
            for (Object obj : dataList) {
                workcenter = (WorkCentre) obj;
                tmpObj = new JSONObject();
                tmpObj.put(WorkCentre.WCID, workcenter.getID());
                tmpObj.put(workcenter.WORKCENTRENAME, workcenter.getName());
                tmpObj.put(workcenter.WORKCENTREID, workcenter.getWorkcenterid());
                jArr.put(tmpObj);
            }
            jobj.put("data", jArr);
             jobj.put("success", true);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkServiceImpl.getWorkOrdersForCombo", ex);
        }
        return jobj;
    }
    
    
}
