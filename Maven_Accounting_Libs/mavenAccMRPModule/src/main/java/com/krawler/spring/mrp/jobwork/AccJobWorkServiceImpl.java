/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.jobwork;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.CustomizeReportMapping;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.FileUploadHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import static com.krawler.spring.authHandler.authHandler.getDateOnlyFormatPattern;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.mrp.WorkOrder.AccWorkOrderServiceDAO;
import com.krawler.spring.mrp.WorkOrder.WorkOrder;
import com.krawler.spring.mrp.WorkOrder.WorkOrderDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.springframework.context.MessageSource;

/**
 *
 * @author krawler
 */
public class AccJobWorkServiceImpl implements AccJobWorkService {

    private AccJobWorkDao accJobWorkDaoObj;
    private MessageSource messageSource;
    private WorkOrderDAO workOrderDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accAccountDAO accAccountDAOobj;
    private fieldDataManager fieldDataManagercntrl;
    private auditTrailDAO auditTrailObj;
    private StockService stockService;

    public void setAccJobWorkDaoObj(AccJobWorkDao accJobWorkDaoObj) {
        this.accJobWorkDaoObj = accJobWorkDaoObj;
    }

    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;

    }

    public void setWorkOrderDAOObj(WorkOrderDAO workOrderDAOObj) {
        this.workOrderDAOObj = workOrderDAOObj;
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

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }
    
    @Override
    public JSONObject saveJobWork(Map<String, Object> dataMap) throws ServiceException {

        JSONObject jobj = new JSONObject();

        try {

            KwlReturnObject kwl = accJobWorkDaoObj.saveJobWork(dataMap);
            JobWork jobWork = (JobWork) kwl.getEntityList().get(0);
            /*
             Save Custom Field Data
             */
            String customfield = (String) dataMap.get(JobWork.CUSTOMFIELD);
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put(Constants.customarray, jcustomarray);
                customrequestParams.put(Constants.modulename, Constants.Acc_MRPJobWork_Modulename);
                customrequestParams.put(Constants.moduleprimarykey, Constants.Acc_MRPJobWork_Id);
                customrequestParams.put(Constants.modulerecid, jobWork.getId());
                customrequestParams.put(Constants.moduleid, Constants.MRP_JOB_WORK_MODULEID);
                customrequestParams.put(Constants.companyid, dataMap.get(Constants.companyid));
                customrequestParams.put(Constants.customdataclasspath, Constants.Acc_MRPJobWork_CustomData_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    dataMap.put(JobWork.ACCJOBWORKCUSTOMDATAREF, jobWork.getId());
                    dataMap.put(JobWork.ID, jobWork.getId());
                    kwl = accJobWorkDaoObj.saveJobWork(dataMap);
                }
            }
        } catch (Exception ex) {

            throw ServiceException.FAILURE("AccJobWorkServiceImpl.saveJobWork", ex);
        }
        return jobj;
    }

    public JSONObject saveForecastTemplate(Map<String, Object> dataMap) throws ServiceException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject result = null;
        try {
            String companyId = "";
            String entryNo = "";
            Locale locale = null;
            String id = "";
            if (dataMap.containsKey("requestcontextutilsobj")) {
                locale = (Locale) dataMap.get("requestcontextutilsobj");
            }
            if (dataMap.containsKey("company")) {
                companyId = dataMap.get("company").toString();
            }
            if (dataMap.containsKey(ForecastTemplate.FORECASTID)) {
                entryNo = dataMap.get(ForecastTemplate.FORECASTID).toString();
            }
            if (dataMap.containsKey("id")) {
                id = dataMap.get("id").toString();
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("companyId", companyId);
            map.put("entryNumber", entryNo);
            if (!StringUtil.isNullOrEmpty(entryNo)) {
                if (StringUtil.isNullOrEmpty(id)) {
                    result = accJobWorkDaoObj.getForecastNumberCount(map);
                    int countduplicate = result.getRecordTotalCount();
                    if (countduplicate > 0 && StringUtil.isNullOrEmpty(id)) {
                        throw new AccountingException(messageSource.getMessage("mrp.forecase.ID", null, locale) + " ' <b>" + entryNo + "</b> " + messageSource.getMessage("acc.field.alreadyexists.", null, locale));
                    }
                } else {
                    map.put("billid", id);
                    result = accJobWorkDaoObj.getForecastNumberCount(map);
                    int countduplicate = result.getRecordTotalCount();
                    if (countduplicate > 0) {
                        throw new AccountingException(messageSource.getMessage("mrp.forecase.ID", null, locale) + " ' <b>" + entryNo + "</b> " + messageSource.getMessage("acc.field.alreadyexists.", null, locale));
                    }
                }
            }
            KwlReturnObject kwl = null;
            String msg = "";
            String auditMsg = "";
            if (StringUtil.isNullOrEmpty(id)) {
                /*
                Create and Copy 
                */
                dataMap.put("createdon", new Date());
                kwl = accJobWorkDaoObj.saveForecastTemplate(dataMap);
                msg = messageSource.getMessage("acc.forecase.successfullysavedmsg", null, locale) + " <br> Document No : <b>" + entryNo + "</b>";
                auditMsg = messageSource.getMessage("acc.forecast.auditsave", null, locale) + " " + entryNo;
            } else {
                /*
                Edit case
                */
                dataMap.put("id", id);
                dataMap.put("updatedon", new Date());
                kwl = accJobWorkDaoObj.saveForecastTemplate(dataMap);
                msg = messageSource.getMessage("acc.forecase.successfullyupdatedmsg", null, locale) + " <br> Document No : <b>" + entryNo + "</b>";
                auditMsg = messageSource.getMessage("acc.forecast.auditupdate", null, locale) + " " + entryNo;
            }
            jobj.put("msg", msg);
            jobj.put("auditMsg", auditMsg);
            ForecastTemplate forecastTemplate = (ForecastTemplate) kwl.getEntityList().get(0);
            Map mappingParams = new HashMap<String, Object>();
            mappingParams.put("companyid", dataMap.get("company"));
            mappingParams.put("forecastTemplate", forecastTemplate);
            mappingParams.put("forecastTemplateId", forecastTemplate.getID());
            accJobWorkDaoObj.deleteForecastTemplateProductMapping(mappingParams);
            if (dataMap.containsKey("product")) {
                mappingParams.put("product", dataMap.get("product"));
                result = accJobWorkDaoObj.saveForecastTemplateProductMapping(mappingParams);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }

    public void getColumnModelAndRecordDataForForecast(Map<String, Object> requestParams, JSONObject object) throws ServiceException {
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        String storeRec = "";
        String companyid = "";
        String start = "", limit = "";
        if (requestParams.containsKey("start")) {
            start = requestParams.get("start").toString();
        }
        if (requestParams.containsKey("limit")) {
            limit = requestParams.get("limit").toString();
        }
        if (requestParams.containsKey("companyid")) {
            companyid = requestParams.get("companyid").toString();
        }
        Locale requestcontextutilsobj = null;

        if (requestParams.containsKey("requestcontextutilsobj")) {
            requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
        }
        boolean isExport = false;
        if (requestParams.containsKey("isExport")) {
            isExport = Boolean.parseBoolean(requestParams.get("isExport").toString());
        }
        try {
            storeRec = "forecastid,forecastyearhistory,forecastyear,forecasttype,forecastmethod,productname,productid,billid,templatename";
            String[] recArr = storeRec.split(",");
            // Get those fields in record for whome, no special properties present like type, defVal, mapping etc.
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }

            // Gel column model - 
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.forecase.tilte", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "templatename");
            jobjTemp.put("width", 150);
            jobjTemp.put("align", "center");
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.forecase.ID", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "forecastid");
            jobjTemp.put("width", 150);
            jobjTemp.put("align", "center");
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.forecase.yaerodhistory", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "forecastyearhistory");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.forecase.forecastyear", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "forecastyear");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.forecase.reporttype", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "forecasttype");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.forecase.method", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "forecastmethod");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("mrp.forecase.product", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "productname");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
                jobjTemp = new JSONObject();
                jobjTemp.put("header", messageSource.getMessage("acc.monthlyForecast", null, requestcontextutilsobj));
                jobjTemp.put("dataIndex", "productid");
                jobjTemp.put("align", "center");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
            /*
             Add Custom Fields in Column Model
             */
            requestParams.put("companyId", companyid);
            requestParams.put("reportId", Constants.Labour_Master);
            /*
             Get Data from DB 
             */
            KwlReturnObject result = accJobWorkDaoObj.getForecastTemplate(requestParams);
            List<ForecastTemplate> list = result.getEntityList();
            int count = result.getRecordTotalCount();
            for (ForecastTemplate forecastTemplate : list) {

                Set<ForecastProductMapping> forecastProductMappings = null;
                forecastProductMappings = forecastTemplate.getForecastProductMappings();
                if (forecastProductMappings != null && forecastProductMappings.size() > 0) {
                    for (ForecastProductMapping forecastProductMapping : forecastProductMappings) {
                        if (forecastProductMapping.getProduct() != null) {
                            JSONObject jSONObject = new JSONObject();
                            jSONObject.put("forecastid", forecastTemplate.getForecastId());
                            jSONObject.put("templatename", forecastTemplate.getTitle());
                            jSONObject.put("forecastyearhistory", forecastTemplate.getForecastYearHistory());
                            jSONObject.put("forecastyear", forecastTemplate.getForecastYear());
                            if (forecastTemplate.getForecastType().equalsIgnoreCase("1")) {
                                jSONObject.put("forecasttype", "Sales Order");
                            } else if (forecastTemplate.getForecastType().equalsIgnoreCase("2")) {
                                jSONObject.put("forecasttype", "Invoice");
                            } else if (forecastTemplate.getForecastType().equalsIgnoreCase("3")) {
                                jSONObject.put("forecasttype", "Delivery Order");
                            }
                             if (forecastTemplate.getForecastMethod().equalsIgnoreCase(forecastTemplate.PERCENTOVERLASTYEAR)) {
                                jSONObject.put("forecastmethod", "Percent Over Last Year");
                            } else if (forecastTemplate.getForecastMethod().equalsIgnoreCase(forecastTemplate.LASTYEARTOTHISYEAR)) {
                                jSONObject.put("forecastmethod", "Last Year To This Year");
                            } 
                            
                            jSONObject.put("productname", forecastProductMapping.getProduct().getName());
                            if (!isExport) {
                                jSONObject.put("productid", forecastProductMapping.getProduct().getID());
                            }
                            jSONObject.put("billid", forecastTemplate.getID());
                            dataJArr.put(jSONObject);
                        }
                    }
                }
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
            
            if (isExport) {
                object.put("data", dataJArr);
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public boolean isJobWorkIDAlreadyPresent(Map<String, Object> requestParams) throws ServiceException {

        boolean isJobWorkIDAlreadyPresent = false;
        try {
            KwlReturnObject returnResult = accJobWorkDaoObj.getJobWorkOrders(requestParams);

            if (returnResult.getRecordTotalCount() > 0) {
                isJobWorkIDAlreadyPresent = true;
            }

        } catch (Exception ex) {
            Logger.getLogger(AccJobWorkServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return isJobWorkIDAlreadyPresent;
    }

    @Override
    public JSONObject getColumnModelForJobOrderReport(Map<String, Object> requestParams) {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        JSONObject commData = new JSONObject();
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        try {

            Boolean isExport = false;
            Locale requestcontextutilsobj = null;
            DateFormat userdf = requestParams.containsKey(Constants.userdf) ? (DateFormat) requestParams.get(Constants.userdf) : null;
            if (requestParams.containsKey("isExport") && requestParams.get("isExport") != null) {
                isExport = (Boolean) requestParams.get("isExport");
            }
            if (requestParams.containsKey("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
            }
            //***************************Data**********************************************

            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            if (requestParams.containsKey(Constants.REQ_startdate) && !StringUtil.isNullObject(requestParams.get(Constants.REQ_startdate))) {
                requestParams.put(Constants.REQ_startdate, df.parse((String) requestParams.get(Constants.REQ_startdate)));
            }
            if (requestParams.containsKey(Constants.REQ_enddate) && !StringUtil.isNullObject(requestParams.get(Constants.REQ_enddate))) {
                requestParams.put(Constants.REQ_enddate, df.parse((String) requestParams.get(Constants.REQ_enddate)));
            }

            KwlReturnObject result = accJobWorkDaoObj.getJobWorkOrders(requestParams);
            List dataList = result.getEntityList();
            JobWork jobwork = null;
            JSONObject tmpObj = new JSONObject();
            for (Object obj : dataList) {
                jobwork = (JobWork) obj;
                tmpObj = new JSONObject();

                tmpObj.put(JobWork.ID, jobwork.getId());
                tmpObj.put(JobWork.JOBORDERNAME, jobwork.getJobordername());
                tmpObj.put(JobWork.JOBORDERNUMBER, jobwork.getJobordernumber());
                tmpObj.put(JobWork.SEQUENCEFORMAT, jobwork.getSeqformat() != null ? jobwork.getSeqformat().getID() : "");
                tmpObj.put(JobWork.JOBWORKDATE, userdf != null ? userdf.format(jobwork.getJobworkdate()) : jobwork.getJobworkdate());
                tmpObj.put(JobWork.DATEOFDELIVERY, userdf != null ? userdf.format(jobwork.getDateofdelivery()) : jobwork.getDateofdelivery());
                tmpObj.put(JobWork.VENDORID, jobwork.getVendorid() != null ? jobwork.getVendorid().getID() : "");
                tmpObj.put(JobWork.VENDORNAME, jobwork.getVendorid() != null ? jobwork.getVendorid().getName() : "");
                tmpObj.put(JobWork.VENDORCODE, jobwork.getVendorid() != null ? jobwork.getVendorid().getAcccode() : "");
                tmpObj.put(JobWork.DATEOFSHIPMENT, userdf != null ? userdf.format(jobwork.getDateofshipment()) : jobwork.getDateofshipment());
                tmpObj.put(JobWork.EXCISEDUTYCHARGES, jobwork.getExcisedutychargees());
                tmpObj.put(JobWork.JOBWORKLOCATION, jobwork.getJobworklocation() != null ? jobwork.getJobworklocation().getName() : "");
                tmpObj.put(JobWork.JOBWORKLOCATIONID, jobwork.getJobworklocation() != null ? jobwork.getJobworklocation().getId() : "");
                tmpObj.put(JobWork.SHIPMENTROUTE, jobwork.getShipmentroute());
                tmpObj.put(JobWork.GATEPASS, jobwork.getGatepass());
                tmpObj.put(JobWork.OTHERREMARKS, jobwork.getOtherremarks());
                tmpObj.put(JobWork.COMPANYID, jobwork.getCompanyid() != null ? jobwork.getCompanyid().getCompanyID() : "");
                tmpObj.put(JobWork.USERID, jobwork.getCreator().getUserID());
                tmpObj.put(JobWork.PRODUCTID, jobwork.getProductid() != null ? jobwork.getProductid().getID() : "");
                tmpObj.put(JobWork.PRODUCTCODE, jobwork.getProductid() != null ? jobwork.getProductid().getProductid() : "");
                tmpObj.put(JobWork.PRODUCTUOMNAME, jobwork.getProductid() != null ? jobwork.getProductid().getUnitOfMeasure() != null ? jobwork.getProductid().getUnitOfMeasure().getNameEmptyforNA() : "" : "");
                tmpObj.put(JobWork.PRODUCTTYPE, jobwork.getProductid() != null ? jobwork.getProductid().getProducttype() != null ? jobwork.getProductid().getProducttype().getName() : "" : "");
                 tmpObj.put("quantity", jobwork.getProductquantity());
                tmpObj.put("deleted", jobwork.isDeleted());

                tmpObj.put(JobWork.WORKORDERID, jobwork.getWorkorderid() != null ? jobwork.getWorkorderid().getID() : "");
                tmpObj.put(JobWork.WORKORDERCODE, jobwork.getWorkorderid() != null ? jobwork.getWorkorderid().getWorkOrderID() : "");

                /*
                 Add Global Custom data for document
                 */
                Map globalMap = new HashMap();
                globalMap.put("moduleid", Constants.MRP_JOB_WORK_MODULEID);
                globalMap.put("companyid", requestParams.get("companyid"));
                globalMap.put("id", jobwork.getId());
                globalMap.put(Constants.userdf, userdf);
                putGlobalCustomDetailsForJobOrder(tmpObj, globalMap);
                dataJArr.put(tmpObj);
            }

            //***************************Data**********************************************
            //*******************Record****************************
            JSONObject rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.ID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.JOBORDERNAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.JOBORDERNUMBER);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.SEQUENCEFORMAT);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.JOBWORKDATE);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.DATEOFDELIVERY);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.VENDORID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.VENDORCODE);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.VENDORNAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.DATEOFSHIPMENT);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.EXCISEDUTYCHARGES);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.JOBWORKLOCATION);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.JOBWORKLOCATIONID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.SHIPMENTROUTE);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.GATEPASS);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.OTHERREMARKS);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.COMPANYID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.USERID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.PRODUCTID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.PRODUCTCODE);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.PRODUCTUOMNAME);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.PRODUCTTYPE);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, "quantity");
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.WORKORDERCODE);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, JobWork.WORKORDERID);
            jarrRecords.put(rec);
            rec = new JSONObject();
            rec.put(JobWork.KEY, "deleted");
            jarrRecords.put(rec);

            //*******************Record****************************
            //*****************ColumnModel*************************
//         
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.workorder.joborder.fields.joborder.number", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", JobWork.JOBORDERNUMBER);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.workorder.joborder.fields.joborder.name", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", JobWork.JOBORDERNAME);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.workorder.joborder.fields.joborder.workorder.headername", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", JobWork.WORKORDERCODE);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.workorder.joborder.fields.joborder.date", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", JobWork.JOBWORKDATE);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.product.gridProductID", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", JobWork.PRODUCTCODE);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
//            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.accPref.autoVendorid", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", JobWork.VENDORCODE);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);
//           
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.workorder.joborder.fields.joborder.productqty", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "quantity");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.workorder.joborder.fields.joborder.dateofshipment", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", JobWork.DATEOFSHIPMENT);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.workorder.joborder.fields.joborder.dateOfDelivery", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", JobWork.DATEOFDELIVERY);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.workorder.joborder.fields.joborder.exduty", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", JobWork.EXCISEDUTYCHARGES);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.workorder.joborder.fields.joborder.jobworklocation", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", JobWork.JOBWORKLOCATION);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.workorder.joborder.fields.joborder.shipmentroute", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", JobWork.SHIPMENTROUTE);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.workorder.joborder.fields.joborder.gatepass", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", JobWork.GATEPASS);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.mrp.workorder.joborder.fields.joborder.otherremarks", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", JobWork.OTHERREMARKS);
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            /*
             Add Custom Fields in Column Model
             */
            requestParams.put("companyId", requestParams.get("companyid"));
            requestParams.put("reportId", Constants.MRP_JOB_WORK_MODULEID);
            putCustomColumnForJobOrder(jarrColumns, jarrRecords, requestParams);
//*****************ColumnModel******************************

            commData.put("success", true);
            commData.put("coldata", dataJArr);
            commData.put("columns", jarrColumns);
             commData.put("totalCount",result.getRecordTotalCount());
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
            Logger.getLogger(AccJobWorkServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    /**
     *
     * @param jSONObject
     * @param map
     * @throws ServiceException
     * @throws JSONException
     * @Description : Put Custom fields data in JSON to show in Report
     */
    public void putGlobalCustomDetailsForJobOrder(JSONObject jSONObject, Map<String, Object> map) throws ServiceException, JSONException {

        String companyId = "";
        int moduleid = 0;
        String jobOrderId = "";
        if (map.containsKey("companyid")) {
            companyId = map.get("companyid").toString();
        }
        if (map.containsKey("moduleid")) {
            moduleid = Integer.parseInt(map.get("moduleid").toString());
        }
        if (map.containsKey("id")) {
            jobOrderId = map.get("id").toString();
        }
        // Get Custom Field Data 
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> customFieldMap = new HashMap<>();
        HashMap<String, String> customDateFieldMap = new HashMap<>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, moduleid));
        HashMap<String, String> replaceFieldMap = new HashMap<>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
        Map<String, Object> variableMap = new HashMap<>();

        KwlReturnObject customObjresult = null;
        customObjresult = accountingHandlerDAOobj.getObject(JobWorkCustomData.class.getName(), jobOrderId);
        replaceFieldMap = new HashMap<>();
        if (customObjresult != null && customObjresult.getEntityList().size() > 0) {
            JobWorkCustomData jeDetailCustom = (JobWorkCustomData) customObjresult.getEntityList().get(0);
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
     * @param jarrColumns = Column Model
     * @param jarrRecords = Record for store
     * @param requestParams
     * @Description : Add Column model for Custom Field
     * @throws ServiceException
     */
    public void putCustomColumnForJobOrder(JSONArray jarrColumns, JSONArray jarrRecords, Map<String, Object> requestParams) throws ServiceException {
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
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    @Override
    public KwlReturnObject deleteJobWork(Map<String, Object> dataMap) throws ServiceException {
        List list = Collections.EMPTY_LIST;
        try {
            String entrynumber = "", auditMsg="";  //ERP-30663
            Boolean isTempDelete = dataMap.containsKey("isTempDelete") ? (Boolean) dataMap.get("isTempDelete") : false;
            Boolean isPermDelete = dataMap.containsKey("isPermDelete") ? (Boolean) dataMap.get("isPermDelete") : false;
            JSONObject jobj = new JSONObject((String) dataMap.get("data"));
            JSONArray jArr = jobj.getJSONArray("root");

            Map<String, Object> requestParams = new HashMap();
            requestParams.put(JobWork.COMPANYID, dataMap.get(JobWork.COMPANYID));
            if (isPermDelete) {
                for (int i = 0; i < jArr.length(); i++) {
                    String id = (String) jArr.get(i);
                    if (!StringUtil.isNullOrEmpty(id)) {
                        requestParams.put(JobWork.ID, id);
                        
                        //ERP-30663 : Fetch the JWO Details to get JWO Number
                        KwlReturnObject kwlReturnObject = accountingHandlerDAOobj.getObject(JobWork.class.getName(), id);
                        JobWork jobwork = (JobWork) kwlReturnObject.getEntityList().get(0);
                        String jwonumber = jobwork.getJobordernumber();
                        
                        KwlReturnObject result = accJobWorkDaoObj.deleteJobWorkOrdersPerm(requestParams);
                        
                        //ERP-30663 : If record deleted successfully, hold its entryno for audit trail purpose
                        int count = (Integer)result.getEntityList().get(0);
                        if(count>0){
                            if (entrynumber != "") {
                                entrynumber = entrynumber + " , " + jwonumber;
                            } else {
                                entrynumber = jwonumber;
                            }
                        }                        
                    }

                }
            } else if (isTempDelete) {
                for (int i = 0; i < jArr.length(); i++) {
                    String id = (String) jArr.get(i);
                    if (!StringUtil.isNullOrEmpty(id)) {
                        requestParams.put(JobWork.ID, id);
                        KwlReturnObject result = accJobWorkDaoObj.deleteJobWorkOrdersTemp(requestParams);                        
                        int count = (Integer)result.getEntityList().get(0);
                        if(count>0){
                            //ERP-30663 : If record deleted temporarily then hold its entryno for audit trail purpose
                            KwlReturnObject kwlReturnObject = accountingHandlerDAOobj.getObject(JobWork.class.getName(), id);
                            JobWork jobwork = (JobWork) kwlReturnObject.getEntityList().get(0);
                            if(entrynumber!=""){
                                entrynumber = entrynumber + " , "+jobwork.getJobordernumber();
                            } else{
                                entrynumber = jobwork.getJobordernumber();
                            }                            
                        }
                    }

                }
            }
            
            if (isPermDelete) { //ERP-30663 : Audit Trail entry for permanent delete.
                auditMsg = " User " + (String)dataMap.get("userfullname") + " has deleted Job Work Out  <b>" + entrynumber+ "</b>  permanently.";
                auditTrailObj.insertAuditLog(AuditAction.MRP_JOBWORKOUT_DELETED, auditMsg, dataMap, (String)dataMap.get("userid"));
            } else {    //ERP-30663 : Audit Trail entry for temporary delete.
                auditMsg = " User " + (String)dataMap.get("userfullname") + " has deleted Job Work Out  <b>" + entrynumber + "</b>  temporarily.";
                auditTrailObj.insertAuditLog(AuditAction.MRP_JOBWORKOUT_DELETED, auditMsg, dataMap, (String)dataMap.get("userid"));
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkServiceImpl.deleteJobWork", ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public JSONObject getWorkOrdersForCombo(Map<String, Object> requestParms) throws ServiceException {
        List list = Collections.EMPTY_LIST;
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            KwlReturnObject kwlResult = workOrderDAOObj.getWorkOrders(requestParms);
            List dataList = kwlResult.getEntityList();
            WorkOrder workOrder = null;
            JSONObject tmpObj = new JSONObject();
            for (Object obj : dataList) {
                workOrder = (WorkOrder) obj;
                tmpObj = new JSONObject();
                tmpObj.put(workOrder.WORKORDERID, workOrder.getID());
                tmpObj.put(workOrder.WORKORDERNAME, workOrder.getWorkOrderName());
                tmpObj.put("wocode", workOrder.getWorkOrderID());
                tmpObj.put("projectid", workOrder.getProjectId());  
                tmpObj.put(workOrder.BOMID, workOrder.getBomid() != null ? workOrder.getBomid().getID() : "");
                tmpObj.put(workOrder.BOMNAME, workOrder.getBomid() != null ? workOrder.getBomid().getBomName() : "");
                jArr.put(tmpObj);
            }
            jobj.put("data", jArr);
            jobj.put("success", true);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccJobWorkServiceImpl.getWorkOrdersForCombo", ex);
        }
        return jobj;
    }

    public void getForecastDetails(Map<String, Object> requestParams, JSONObject object) throws ServiceException {
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        String storeRec = "";
        String companyid = "";
        String productid = "";
        String billid = "";
        String start = "", limit = "";
        if (requestParams.containsKey("start")) {
            start = requestParams.get("start").toString();
        }
        if (requestParams.containsKey("limit")) {
            limit = requestParams.get("limit").toString();
        }
        if (requestParams.containsKey("companyid")) {
            companyid = requestParams.get("companyid").toString();
        }
        if (requestParams.containsKey("billid")) {
            billid = requestParams.get("billid").toString();
        }
        if (requestParams.containsKey("productid")) {
            productid = requestParams.get("productid").toString();
        }
        Locale requestcontextutilsobj = null;

        if (requestParams.containsKey("requestcontextutilsobj")) {
            requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
        }

        KwlReturnObject kwlReturnObject = accountingHandlerDAOobj.getObject(ForecastTemplate.class.getName(), billid);
        ForecastTemplate forecastTemplate = (ForecastTemplate) kwlReturnObject.getEntityList().get(0);

        Date forecastYear = forecastTemplate.getForecastYear();
        int yearOfHistory = Integer.parseInt(forecastTemplate.getForecastYearHistory());
        String forecastType = forecastTemplate.getForecastType();
        try {
            List<String> monthList = new ArrayList();
            monthList.add("January");
            monthList.add("February");
            monthList.add("March");
            monthList.add("April");
            monthList.add("May");
            monthList.add("June");
            monthList.add("July");
            monthList.add("August");
            monthList.add("September");
            monthList.add("October");
            monthList.add("November");
            monthList.add("December");

            storeRec = "month,";
            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>Month<b>");
            jobjTemp.put("dataIndex", "month");
            jobjTemp.put("width", 150);
            jobjTemp.put("align", "center");
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            List<String> yearList = new ArrayList();
            LocalDate forecastYearLocale = new LocalDate(forecastYear);
            String foreCastyear = forecastYearLocale.toString("yyyy");
            for (int i = 0; i < 2; i++) {
                forecastYearLocale = forecastYearLocale.minus(Period.years(1));
                String yearname = forecastYearLocale.toString("yyyy");
                yearList.add(yearname);
            }

            String dateStr = "";
            for (int i = yearList.size() - 1; i >= 0; i--) {
                /*
                 *Added dataindex in record
                 */
                storeRec += "amount_" + i + ",";

                dateStr = yearList.get(i);
                /*
                 * Added dataindex in Header
                 */
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "<b>" + dateStr + "<b> Sales");
                jobjTemp.put("dataIndex", "amount_" + i);
                jobjTemp.put("renderer", "WtfGlobal.quantityRenderer");
                jobjTemp.put("align", "right");
                jobjTemp.put("width", 100);
                jobjTemp.put("pdfwidth", 100);
                jarrColumns.put(jobjTemp);
            }
            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + foreCastyear + " Forecast<b>");
            jobjTemp.put("dataIndex", "forecast");
            jobjTemp.put("renderer", "WtfGlobal.quantityRenderer");
            jobjTemp.put("align", "right");
            jobjTemp.put("width", 100);
            jobjTemp.put("pdfwidth", 100);
            jarrColumns.put(jobjTemp);
            storeRec += "forecast,simulate";
            String[] recArr = storeRec.split(",");
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }
            // Gel column model - 

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>Simulated " + dateStr + " Forecast <b>");
            jobjTemp.put("dataIndex", "simulate");
            jobjTemp.put("renderer", "WtfGlobal.quantityRenderer");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            List sumofcurrentyear = new ArrayList();
            List sumoflastyear = new ArrayList();
            for (int j = 0; j < monthList.size(); j++) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("month", monthList.get(j).toString());
                double qty = 0d;
                Map sumMap = new HashMap();
                for (int k = 0; k < yearList.size(); k++) {
                    int year = Integer.parseInt(yearList.get(k));
                    SimpleDateFormat sdf = new SimpleDateFormat(getDateOnlyFormatPattern());
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, j); // 11 = december
                    cal.set(Calendar.DATE, 1);
                    String startdate = sdf.format(cal.getTime());
                    if (j != 11) {
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, j + 1);
                        cal.set(Calendar.DATE, 1);
                    } else {
                        cal.set(Calendar.YEAR, year + 1);
                        cal.set(Calendar.MONTH, 0);
                        cal.set(Calendar.DATE, 1);
                    }
                    cal.add(Calendar.DATE, -1);
                    cal.getTime();
                    String enddate = sdf.format(cal.getTime());
                    Map<String, Object> map = new HashMap();
                    map.put("companyId", companyid);
                    map.put("prductid", productid);
                    map.put("startdate", startdate);
                    map.put("enddate", enddate);
                    map.put("df", authHandler.getDateOnlyFormat());

                    // if Sales Order 
                    if (forecastType.equalsIgnoreCase(forecastTemplate.SALESORDER)) {
                        qty = accJobWorkDaoObj.getSalesQtyforModules(map);
                        jSONObject.put("amount_" + k, qty);
                    } else // if Invoice 
                    if (forecastType.equalsIgnoreCase(forecastTemplate.INVOICE)) {
                        qty = accJobWorkDaoObj.getInvoiceQtyforModules(map);
                        jSONObject.put("amount_" + k, qty);
                    } else // if Delivery Order
                    if (forecastType.equalsIgnoreCase(forecastTemplate.DELIVERYORDER)) {
                        qty = accJobWorkDaoObj.getDeliveryOrderQtyforModules(map);
                        jSONObject.put("amount_" + k, qty);
                    }

                    /*
                     calculate simulated
                     */
                    if (k == yearList.size() - 2) {
                        sumofcurrentyear.add(qty);
                    } else if (k == yearList.size() - 1) {
                        sumoflastyear.add(qty);
                    }

                }
                dataJArr.put(jSONObject);
            }

            /*
             Calculate forecast Sales Forecast - Percent over last year
             */
            if (forecastTemplate.getForecastMethod().equalsIgnoreCase(forecastTemplate.PERCENTOVERLASTYEAR)) {
                double sumoflast = (double) sumoflastyear.get(sumoflastyear.size() - 3) + (double) sumoflastyear.get(sumoflastyear.size() - 2) + (double) sumoflastyear.get(sumoflastyear.size() - 1);
                double sumofcurrent = (double) sumofcurrentyear.get(sumofcurrentyear.size() - 3) + (double) sumofcurrentyear.get(sumofcurrentyear.size() - 2) + (double) sumofcurrentyear.get(sumofcurrentyear.size() - 1);
                double calculateFactor = 1;
                if (sumofcurrent != 0 && sumoflast != 0) {
                    calculateFactor = sumofcurrent / sumoflast;
                }

                //calculate simulated factor
                sumoflast = (double) sumoflastyear.get(sumoflastyear.size() - 4) + (double) sumoflastyear.get(sumoflastyear.size() - 5) + (double) sumoflastyear.get(sumoflastyear.size() - 6);
                sumofcurrent = (double) sumofcurrentyear.get(sumofcurrentyear.size() - 4) + (double) sumofcurrentyear.get(sumofcurrentyear.size() - 5) + (double) sumofcurrentyear.get(sumofcurrentyear.size() - 6);
                double simulatedFactor = 1;
                if (sumofcurrent != 0 && sumoflast != 0) {
                    simulatedFactor = sumofcurrent / sumoflast;
                }

                for (int arr = 0; arr < dataJArr.length(); arr++) {
                    double val = (double) dataJArr.getJSONObject(arr).get("amount_0");
                    double simval = (double) dataJArr.getJSONObject(arr).get("amount_1");
                    dataJArr.getJSONObject(arr).put("forecast", val * calculateFactor);
                    if (arr > dataJArr.length() - 4) {
                        dataJArr.getJSONObject(arr).put("simulate", simval * simulatedFactor);
                    }
                }
            } else if (forecastTemplate.getForecastMethod().equalsIgnoreCase(forecastTemplate.LASTYEARTOTHISYEAR)) {
                for (int arr = 0; arr < dataJArr.length(); arr++) {
                    double val = (double) dataJArr.getJSONObject(arr).get("amount_0");
                    dataJArr.getJSONObject(arr).put("forecast", val);
                    if (arr > dataJArr.length() - 4) {
                        dataJArr.getJSONObject(arr).put("simulate", (double) dataJArr.getJSONObject(arr).get("amount_1"));
                    }
                }
            }

            JSONArray pagedJson = new JSONArray();
            pagedJson = dataJArr;
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
            Logger.getLogger(AccJobWorkServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /*
     Fetch Forecast data using Id
     */

    public JSONObject getSingleForecastToLoad(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jSONObject = new JSONObject();
        KwlReturnObject result = accJobWorkDaoObj.getForecastTemplate(requestParams);
        List<ForecastTemplate> list = result.getEntityList();
        for (ForecastTemplate forecastTemplate : list) {
            try {
                LocalDate forecastYearLocale = new LocalDate(forecastTemplate.getForecastYear());
                String foreCastyear = forecastYearLocale.toString("yyyy");
                jSONObject.put("title", forecastTemplate.getTitle());
                jSONObject.put("forecastid", forecastTemplate.getForecastId());
                jSONObject.put("forecastyearhistory", forecastTemplate.getForecastYearHistory());
                jSONObject.put("yearid", foreCastyear);
                jSONObject.put("forecasttype", forecastTemplate.getForecastType());
                jSONObject.put("forecastmethod", forecastTemplate.getForecastMethod());
                Set<ForecastProductMapping> forecastProductMappings = forecastTemplate.getForecastProductMappings();
                String productid = "", productname = "";
                if (forecastProductMappings != null) {
                    for (ForecastProductMapping forecastProductMapping : forecastProductMappings) {
                        productid = productid + forecastProductMapping.getProduct().getID() + ",";
                        productname = productname + forecastProductMapping.getProduct().getName() + ",";
                    }
                    if (!StringUtil.isNullOrEmpty(productid)) {
                        productid = productid.substring(0, productid.length() - 1);
                    }
                    if (!StringUtil.isNullOrEmpty(productname)) {
                        productname = productname.substring(0, productname.length() - 1);
                    }
                }
                jSONObject.put("productname", productname);
                jSONObject.put("productid", productid);
            } catch (JSONException ex) {
                throw ServiceException.FAILURE(ex.getMessage(), ex);
            }
        }
        return jSONObject;
    }
    /*
     Delete case for Forecast
     */

    public void deleteForecast(Map<String, Object> requestParams) throws ServiceException {
        try {
            Locale locale = null;
            String username="";
            if (requestParams.containsKey("requestcontextutilsobj")) {
                locale = (Locale) requestParams.get("requestcontextutilsobj");
            }
            HttpServletRequest request = null;
            if (requestParams.containsKey("request")) {
                request = (HttpServletRequest) requestParams.get("request");
            }
            if (requestParams.containsKey("username")) {
                username = (String) requestParams.get("username");
            }
            JSONArray jSONArray = new JSONArray((String) requestParams.get("data"));
            JSONArray labouridsJarr = new JSONArray();
            JSONObject labourObj = new JSONObject();
            String userId = "";
            String companyId = "";
            if (requestParams.containsKey("companyId")) {
                companyId = (String) requestParams.get("companyId");
            }
            String entryno = "";
            String msg = "";
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jobj = jSONArray.getJSONObject(i);
                String forecastid = jobj.optString("billid");
                entryno = jobj.optString("forecastid");
                requestParams.put("billId", forecastid);
                requestParams.put("companyId", companyId);
                accJobWorkDaoObj.deleteForecast(requestParams);
                msg = messageSource.getMessage("acc.mrp.forecast.delete", null, locale);
                auditTrailObj.insertAuditLog(AuditAction.FORECAST_TEMPLATE, "User " +username+" "+msg + " " + entryno, request, forecastid);
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    /**
     *
     * @param requestParams = Contains all request params
     * @param object = return column model with data
     * @throws ServiceException
     */
    public void getChallanReport(Map<String, Object> requestParams, JSONObject object) throws ServiceException {
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        String storeRec = "";
        String start = "", limit = "";
        String companyId = "";
        Date startDate = new Date();
        Date endDate = new Date();
        Date asOfDate = new Date();
        Company company = null;
        Date backdate = new Date();
        long differenceDays=0l;
        List customCol=new ArrayList();
        /*
         * To jobWorkOderInAged is used while creating purchase invoice from job work in aging report
         */
        boolean jobWorkOderInAged=false; 
        if (requestParams.containsKey("start")) {
            start = requestParams.get("start").toString();
        }
        if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
            backdate = new Date(requestParams.get(Constants.REQ_startdate).toString());
            Calendar cal = Calendar.getInstance();
            cal.setTime(backdate);
            cal.add(Calendar.DATE, -1);
            backdate = cal.getTime();
        }
        if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
            startDate = new Date(requestParams.get(Constants.REQ_startdate).toString());
        }
        if (requestParams.containsKey(Constants.REQ_enddate) && requestParams.get(Constants.REQ_enddate) != null) {
            endDate = new Date(requestParams.get(Constants.REQ_enddate).toString());
        }
        if (requestParams.containsKey("asOfDate") && requestParams.get("asOfDate") != null) {
            asOfDate = new Date(requestParams.get("asOfDate").toString());
        }
        if (requestParams.containsKey("companyId")) {
            companyId = requestParams.get("companyId").toString();
        }
        /*
         * To jobWorkOderInAged is used while creating purchase invoice from job work in aging report
         */
        if (requestParams.containsKey("jobWorkOderInAged")&&requestParams.get("jobWorkOderInAged")!=null) {
            jobWorkOderInAged = Boolean.parseBoolean(requestParams.get("jobWorkOderInAged").toString());
        }
        KwlReturnObject companyResult = null;
        companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
        company = (Company) companyResult.getEntityList().get(0);
        if (requestParams.containsKey("limit")) {
            limit = requestParams.get("limit").toString();
        }
        DateFormat df = (DateFormat) requestParams.get(Constants.df);
        Locale requestcontextutilsobj = null;
        if (requestParams.containsKey("requestcontextutilsobj")) {
            requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
        }
        try {
            storeRec = "date,challanno,receiveqty,consumeqty,balanceqty,invoicenumber,jobinno,customer,product,ageingdays,jobworkin,jobinId,billid";
            String[] recArr = storeRec.split(",");
            // Get those fields in record for whome, no special properties present like type, defVal, mapping etc.
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }

            // Gel column model - 
            
                        jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.up.3", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "customer");
            jobjTemp.put("width", 150);
            jobjTemp.put("align", "left");
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.challanreport.product", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "product");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            
                                    jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.challanreport.jobwinno", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "jobinno");
            jobjTemp.put("width", 150);
            jobjTemp.put("align", "left");
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.challanreport.date", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "date");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.challanreport.challan", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "challanno");
            jobjTemp.put("width", 150);
            jobjTemp.put("align", "left");
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            /*
             * To jobWorkOderInAged is used while creating purchase invoice from job work in aging report
             */
            if (jobWorkOderInAged) {
                jobjTemp = new JSONObject();
                jobjTemp.put("header", messageSource.getMessage("acc.jobwork.ageingdays", null, requestcontextutilsobj));
                jobjTemp.put("dataIndex", "ageingdays");
                jobjTemp.put("align", "left");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
                jobjTemp = new JSONObject();
                
                jobjTemp.put("header", messageSource.getMessage("acc.jobWorkOrder.vendorjobworkorder", null, requestcontextutilsobj)+" No.");
                jobjTemp.put("dataIndex", "jobworkin");
                jobjTemp.put("align", "left");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
            }
            /*
             * To jobWorkOderInAged is used while creating purchase invoice from job work in aging report
             */
            if (!jobWorkOderInAged) {
                jobjTemp = new JSONObject();
                jobjTemp.put("header", messageSource.getMessage("acc.challanreport.invno", null, requestcontextutilsobj));
                jobjTemp.put("dataIndex", "invoicenumber");
                jobjTemp.put("align", "left");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
            }
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.challanreport.recqty", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "receiveqty");
            jobjTemp.put("align", "right");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.challanreport.consqty", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "consumeqty");
            jobjTemp.put("align", "right");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.challanreport.balanceqty", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "balanceqty");
            jobjTemp.put("align", "right");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            requestParams.put("reportId", Constants.Labour_Master);
            /*
             Get stock in Data from DB 
             */
            Map<Product, List<Object[]>> backForwardQuantityMap = null;
            Set<String> productNameSet = new HashSet<>();
            KwlReturnObject result = accJobWorkDaoObj.getChallanReport(requestParams);
            List list = result.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                JSONObject jSONObject = new JSONObject();
                            
                Object[] row = (Object[]) itr.next();
                String productid = (String) row[0];
                if (row[0] != null) {
                    productid = (String) row[0];
                }
                String productcode = (String) row[1];
                if (row[1] != null) {
                    productcode = (String) row[1];
                }
                Date date = new Date();
                if (row[2] != null) {
                    date = (Date) row[2];
                }
                /*
                 * To jobWorkOderInAged is used while creating purchase invoice from job work in aging report
                 */
                if (jobWorkOderInAged) {
                    differenceDays=0l;
                    /*
                     * To calculating ageing days i.e difference of stock in date and as of date
                     */
                    Calendar calasoFdate = Calendar.getInstance();
                    calasoFdate.setTime(asOfDate);
                    Calendar CalDate = Calendar.getInstance();
                    CalDate.setTime(date);
                    long millisecond=24*60*60*1000;
                    differenceDays = (calasoFdate.getTimeInMillis() - CalDate.getTimeInMillis())/millisecond;
                    
//                    differenceDays = calasoFdate.getTimeInMillis() - CalDate..getTimeInMillis();
                }
                
                String transactionNumber = "";
                if (row[3] != null) {
                    transactionNumber = (String) row[3];
                }
                String lacation = "";
                if (row[4] != null) {
                    lacation = (String) row[4];
                }
                String store = "";
                if (row[5] != null) {
                    store = (String) row[5];
                }
                String batch = "";
                if (row[6] != null) {
                    batch = (String) row[6];
                }
                double qty = 0d;
                if (row[7] != null) {
                    qty = (Double) row[7];
                }
                String customer = "";
                if (row[8] != null) {
                    customer = (String) row[8];
                }
                String jobinno = "";
                if (row[9] != null) {
                    jobinno = (String) row[9];
                }
                String jobworkorder = "";
                if (row[10] != null) {
                    jobworkorder = (String) row[10];
                }
                /*
                 * Job Work in Number
                 */
                String jobworkInNO = "";
                if (row[13] != null) {
                    jobworkInNO = (String) row[13];
                }
                /*
                 * stockin id
                 */
                String jobinId = "";
                if (row[14] != null) {
                    jobinId = (String) row[14];
                }
                /*
                 * So Id
                 */
                String billId = "";
                if (row[15] != null) {
                    billId = (String) row[15];
                }
                String productname = "";
                if (row[11] != null) {
                    productname = (String) row[11];
                }
                if (!productNameSet.contains(productname)&&!jobWorkOderInAged) {
                    KwlReturnObject productResult = null;
                    productResult = accountingHandlerDAOobj.getObject(Product.class.getName(), productid);
                    Product product = (Product) productResult.getEntityList().get(0);
                    backForwardQuantityMap = stockService.getDateWiseStockDetailList(company, null, null, backdate, productname, null);
                    double quantity =  0.0;
                    if (backForwardQuantityMap != null && backForwardQuantityMap.containsKey(product)) {
                        for (Object[] stock : backForwardQuantityMap.get(product)) {
                            if (stock != null) {
                                quantity += Double.parseDouble(stock[8].toString());
                            }
                        }
                    }
                    productNameSet.add(productname);
                    if (qty == 0) {
                        if (quantity != 0) {
                            JSONObject openJobj = new JSONObject();
                            openJobj.put("date", "");
                            openJobj.put("challanno", "Opening Stock");
                            openJobj.put("receiveqty", "");
                            openJobj.put("consumeqty", "");
                            openJobj.put("balanceqty", quantity);
                            openJobj.put("invoicenumber", "");
                            openJobj.put("jobinno", "");
                            openJobj.put("customer", "");
                            openJobj.put("product", productcode);
                            dataJArr.put(openJobj);
                        }
                    } else {
                        JSONObject openJobj = new JSONObject();
                        openJobj.put("date", "");
                        openJobj.put("challanno", "Opening Stock");
                        openJobj.put("receiveqty", "");
                        openJobj.put("consumeqty", "");
                        openJobj.put("balanceqty", quantity);
                        openJobj.put("invoicenumber", "");
                        openJobj.put("jobinno", "");
                        openJobj.put("customer", "");
                        openJobj.put("product", productcode);
                        dataJArr.put(openJobj);
                    }
                }
                if (qty != 0) {
                    jSONObject.put("date", df.format(date));
                    jSONObject.put("challanno", batch);
                    jSONObject.put("receiveqty", qty);
                    jSONObject.put("consumeqty", "-");
                    jSONObject.put("balanceqty", qty);
                    jSONObject.put("invoicenumber", "-");
                    jSONObject.put("jobinno", jobinno);
                    jSONObject.put("jobinId", jobinId);
                    jSONObject.put("jobworkin", jobworkInNO);
                    jSONObject.put("billid", billId);
                    jSONObject.put("ageingdays", differenceDays);
                    jSONObject.put("customer", customer);
                    jSONObject.put("product", productcode);
//                    if (!jobWorkOderInAged) {
                        dataJArr.put(jSONObject);
//                    }
                }
                 /*
                 get Consume details
                 */
                requestParams.put("batchName", batch);
                requestParams.put("productid", productid);
                requestParams.put("jobworkorder", jobworkorder);
                long startdatelong = 0;
                startdatelong = startDate.getTime();
                long enddatelong = endDate.getTime();
                requestParams.put("startdatelong", startdatelong);
                requestParams.put("enddatelong", enddatelong);
                 KwlReturnObject result1=null;
                if (jobWorkOderInAged) {
                    result1 = accJobWorkDaoObj.getConsumeQtyForProduct(requestParams);
                } else {
                    result1 = accJobWorkDaoObj.getConsumeChallan(requestParams);
                }
                List list1 = result1.getEntityList();
                Iterator itr1 = list1.iterator();
                double deliveredqty = 0d;
                double balenceqty = 0d;
                double totalconsumeQuantityOfAsseblyProd = 0d;
                while (itr1.hasNext()) {
                    Object[] row1 = (Object[]) itr1.next();
                    if (jobWorkOderInAged) {
                        double finalQtyjobWorkIn = qty;
                        double consumeQuantityOfAsseblyProd =0.0;
                        if(row1[0]!=null){
                            consumeQuantityOfAsseblyProd = (Double) row1[0];
                        }else if(row1[2]!=null){
                            /*
                             * QC Approved
                             */
                            consumeQuantityOfAsseblyProd = (Double) row1[2];
                    }
                        double relationBetweenInvAndAssProdQty = (Double) row1[1];
                        consumeQuantityOfAsseblyProd = consumeQuantityOfAsseblyProd * relationBetweenInvAndAssProdQty;
                        totalconsumeQuantityOfAsseblyProd=totalconsumeQuantityOfAsseblyProd+consumeQuantityOfAsseblyProd;
                        deliveredqty = deliveredqty + consumeQuantityOfAsseblyProd;
                        jSONObject.put("consumeqty", totalconsumeQuantityOfAsseblyProd);
                        balenceqty = (finalQtyjobWorkIn - deliveredqty);
                        jSONObject.put("balanceqty", balenceqty);
                        jSONObject.put("ageingdays", differenceDays);
                    } else {
                        double deliveredquantity = (Double) row1[0];
                        double subproductqty = (Double) row1[3];
                        long invdatelong = Long.parseLong(row1[2].toString());
                        deliveredquantity = deliveredquantity * subproductqty;
                        deliveredqty = deliveredqty + deliveredquantity;
                        String donumber = (String) row1[1];
                        Date invdate = new Date(invdatelong);
                        JSONObject nObject = new JSONObject();
                        nObject.put("receiveqty", "-");
                        nObject.put("consumeqty", deliveredquantity);
                        nObject.put("invoicenumber", donumber);
                        nObject.put("balanceqty", qty - deliveredqty);
                        nObject.put("date", df.format(invdate));
                        nObject.put("challanno", batch);
                        nObject.put("jobinno", jobinno);
                        nObject.put("customer", customer);
                        nObject.put("product", productcode);
                        dataJArr.put(nObject);
                    }
                }
                /*
                 * If Balence Qty is Zero then record will continue
                 */
                
                if(jobWorkOderInAged){
                      /*
                 * Get Custome column of product master in aged job work report
                 */
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    KwlReturnObject custumObjresult = null;
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    JSONObject custJobj = new JSONObject();
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_Product_Master_ModuleId));
                    HashMap<String, Integer> FieldMapProd = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    custumObjresult = accountingHandlerDAOobj.getObject(AccProductCustomData.class.getName(), productid);
                    replaceFieldMap = new HashMap<>();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        AccProductCustomData stockDetailCustom = (AccProductCustomData) custumObjresult.getEntityList().get(0);
                        if (stockDetailCustom != null) {
                            AccountingManager.setCustomColumnValues(stockDetailCustom, FieldMapProd, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.isExport, false);
                            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
                        }
                    }
                    int i=0;
                    if (customFieldMap.size() > 0) {
                        for (String key :customFieldMap.keySet()) {
                            if (!customCol.contains(key)) {
                                String str=key;
                                jobjTemp = new JSONObject();
                                if(key.contains("Custom_")){
                                    key = key.replace("Custom_", "");
                                }
                                jobjTemp.put("name", str);
                                jarrRecords.put(jobjTemp);

                                jobjTemp = new JSONObject();
                                jobjTemp.put("header", key);
                                jobjTemp.put("dataIndex", str);
                                jobjTemp.put("width", 150);
                                jobjTemp.put("pdfwidth", 150);
                                jobjTemp.put("custom", "true");
                                jarrColumns.put(jobjTemp);

                                customCol.add(str);
                            }

                        }
                    }
//                    dataJArr.put(jSONObject);
                }
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
        }
    }
    
    /**
     * @param requestParams = Contains all request params
     * @param object = return column model with data
     * @throws ServiceException
     */
    public void getJWProductSummaryReport(Map<String, Object> requestParams, JSONObject object) throws ServiceException {
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        String storeRec = "";
        String start = "", limit = "";
        String companyId = "";
        Company company = null;
        Date startDate = new Date();
        Date endDate = new Date();
        Date backdate = new Date();
        if (requestParams.containsKey("start")) {
            start = requestParams.get("start").toString();
        }
        if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate)!= null) {
            backdate = new Date(requestParams.get(Constants.REQ_startdate).toString());
            Calendar cal = Calendar.getInstance();
            cal.setTime(backdate);
            cal.add(Calendar.DATE, -1);
            backdate = cal.getTime();
        }
        if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
            startDate = new Date(requestParams.get(Constants.REQ_startdate).toString());
        }
        if (requestParams.containsKey(Constants.REQ_enddate) && requestParams.get(Constants.REQ_enddate) != null) {
            endDate = new Date(requestParams.get(Constants.REQ_enddate).toString());
        }
        if (requestParams.containsKey("companyId")) {
            companyId = requestParams.get("companyId").toString();
        }
        KwlReturnObject companyResult = null;
        companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
        company = (Company) companyResult.getEntityList().get(0);
        if (requestParams.containsKey("limit")) {
            limit = requestParams.get("limit").toString();
        }
        DateFormat df = (DateFormat) requestParams.get(Constants.df);
        Locale requestcontextutilsobj = null;
        if (requestParams.containsKey("requestcontextutilsobj")) {
            requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
        }
        try {
            storeRec = "productcode,productdesc,uom,openingstock,recqty,despatchqty,closingstock";
            String[] recArr = storeRec.split(",");
            // Get those fields in record for whome, no special properties present like type, defVal, mapping etc.
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }

            // Get column model - 
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.JWProductSummary.itemcode", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "productcode");
            jobjTemp.put("width", 150);
            jobjTemp.put("align", "left");
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.JWProductSummary.itemdesc", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "productdesc");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.JWProductSummary.unit", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "uom");
            jobjTemp.put("width", 150);
            jobjTemp.put("align", "left");
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.JWProductSummary.openingstock", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "openingstock");
            jobjTemp.put("align", "right");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.JWProductSummary.recqty", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "recqty");
            jobjTemp.put("width", 150);
            jobjTemp.put("align", "right");
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.JWProductSummary.despqty", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "despatchqty");
            jobjTemp.put("align", "right");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.JWProductSummary.closingstock", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "closingstock");
            jobjTemp.put("align", "right");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            requestParams.put("reportId", Constants.Labour_Master);
            /*
             Get stock in Data from DB 
             */
            Map<Product, List<Object[]>> backForwardQuantityMap = null;
            Set<String> productNameSet = new HashSet<>();
            KwlReturnObject result = accJobWorkDaoObj.getJWProductSummaryReportIN(requestParams);
            List list = result.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                JSONObject jSONObject = new JSONObject();

                Object[] row = (Object[]) itr.next();
                String productid = (String) row[0];
                if (row[0] != null) {
                    productid = (String) row[0];
                }
                String productcode = (String) row[1];
                if (row[0] != null) {
                    productcode = (String) row[1];
                }
                Date date = new Date();
                if (row[2] != null) {
                    date = (Date) row[2];
                }
                String transactionNumber = "";
                if (row[3] != null) {
                    transactionNumber = (String) row[3];
                }
                String lacation = "";
                if (row[4] != null) {
                    lacation = (String) row[4];
                }
                String store = "";
                if (row[5] != null) {
                    store = (String) row[5];
                }
                String batch = "";
                if (row[6] != null) {
                    batch = (String) row[6];
                }
                double qty = 0d;
                if (row[7] != null) {
                    qty = (Double) row[7];
                }
                String customer = "";
                if (row[8] != null) {
                    customer = (String) row[8];
                }
                String jobinno = "";
                if (row[9] != null) {
                    jobinno = (String) row[9];
                }
                String jobworkorder = "";
                if (row[10] != null) {
                    jobworkorder = (String) row[10];
                }
                String productname = "";
                if (row[11] != null) {
                    productname = (String) row[11];
                }
                KwlReturnObject productResult = null;
                productResult = accountingHandlerDAOobj.getObject(Product.class.getName(), productid);
                Product product = (Product) productResult.getEntityList().get(0);
                backForwardQuantityMap = stockService.getDateWiseStockDetailList(company, null, null, backdate, productname, null);
                double quantity = 0.0;
                if (backForwardQuantityMap != null && backForwardQuantityMap.containsKey(product)) {
                    for (Object[] stock : backForwardQuantityMap.get(product)) {
                        if (stock != null) {
                            quantity += Double.parseDouble(stock[8].toString());
                        }
                    }
                }
                
                
                
                /*
                 get Consume details
                 */
                requestParams.put("batchName", batch);
                requestParams.put("productid", productid);
                requestParams.put("jobworkorder", jobworkorder);
                long startdatelong = 0;
                startdatelong = startDate.getTime();
                long enddatelong = endDate.getTime();
                requestParams.put("startdatelong", startdatelong);
                requestParams.put("enddatelong", enddatelong);
                KwlReturnObject result1 = accJobWorkDaoObj.getJWProductSummaryReportOUT(requestParams);
                List list1 = result1.getEntityList();
                Iterator itr1 = list1.iterator();
                double deliveredqty = 0d;
                while (itr1.hasNext()) {
                    Object[] row1 = (Object[]) itr1.next();
                    double deliveredquantity = 0d;
                    double suproductquantity = 0d;
                    if (row1[0] != null) {
                        deliveredquantity = (Double) row1[0];
                    }
                    if (row1[3] != null) {
                        suproductquantity = (Double) row1[3];
                    }
                    deliveredqty += deliveredquantity * suproductquantity;
                    
                }
                double closingStock = quantity + qty - deliveredqty;
                if (!(quantity == 0 && qty == 0 && closingStock == 0 && deliveredqty == 0) ) {
                    jSONObject.put("productcode", productcode);
                    jSONObject.put("productdesc", productname);
                    jSONObject.put("uom", product.getUnitOfMeasure() != null? product.getUnitOfMeasure().getNameEmptyforNA():"");
                    jSONObject.put("openingstock", quantity);
                    jSONObject.put("recqty", qty);
                    jSONObject.put("closingstock", closingStock);
                    jSONObject.put("despatchqty", deliveredqty);
                    dataJArr.put(jSONObject);
                }
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
        }
    }
    
    public  Map<String, Object>  exportJWProductSummaryjasperreport(Map<String, Object> requestParams,JSONObject request) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        Map<String, Object> challanMap = new HashMap<String, Object>();
        String view = "";
        JasperPrint jasperPrint = null;
        JasperReport jasperReport = null;
        JasperReport jasperReportSubReport = null;
        int templateFlag = request.optInt("templateflag",0);
        int templateType = request.optInt("type",0);
        
        try {
            String companyid = request.optString(Constants.companyKey,"");
            KwlReturnObject result = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) result.getEntityList().get(0);
            Date backdate = new Date();
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                backdate = new Date(requestParams.get(Constants.REQ_startdate).toString());
                Calendar cal = Calendar.getInstance();
                cal.setTime(backdate);
                cal.add(Calendar.DATE, -1);
                backdate = cal.getTime();
            }
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(request);//new SimpleDateFormat("dd/MM/yyyy");
            Date startDate = authHandler.getDateOnlyFormat().parse(requestParams.get(Constants.REQ_startdate).toString());
            Date endDate = authHandler.getDateOnlyFormat().parse(requestParams.get(Constants.REQ_enddate).toString());
            String customerid = "";
            if (requestParams.containsKey("customerid") && requestParams.get("customerid")!= null) {
                customerid = (requestParams.get("customerid").toString());
            }
            String customerName = "";
            if (!StringUtil.isNullOrEmpty(customerid)) {
                KwlReturnObject custresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerid);
                Customer customerObj = (Customer) custresult.getEntityList().get(0);    
                customerName = customerObj.getName();
            }
            String dateRange = "";  //ERP-6384
            
            Map<Product, List<Object[]>> backForwardQuantityMap = null;
            List tableDataList = new ArrayList();
            Set<String> productNameSet = new HashSet<>();
            KwlReturnObject summaryresult = accJobWorkDaoObj.getJWProductSummaryReportIN(requestParams);
            List list = summaryresult.getEntityList();
            Iterator itr = list.iterator();
            int count = 0;
            while (itr.hasNext()) {
                Map<String, Object> tableDataMap = new HashMap<>();
                JSONObject jSONObject = new JSONObject();
                Object[] row = (Object[]) itr.next();
                String productid = (String) row[0];
                if (row[0] != null) {
                    productid = (String) row[0];
                }
                String productcode = (String) row[1];
                if (row[0] != null) {
                    
                }
                productcode = (String) row[1];
                Date date = new Date();
                if (row[2] != null) {
                    date = (Date) row[2];
                }
                String transactionNumber = "";
                if (row[3] != null) {
                    transactionNumber = (String) row[3];
                }
                String lacation = "";
                if (row[4] != null) {
                    lacation = (String) row[4];
                }
                String store = "";
                if (row[5] != null) {
                    store = (String) row[5];
                }
                String batch = "";
                if (row[6] != null) {
                    batch = (String) row[6];
                }
                double qty = 0d;
                if (row[7] != null) {
                    qty = (Double) row[7];
                }
                String customer = "";
                if (row[8] != null) {
                    customer = (String) row[8];
                }
                String jobinno = "";
                if (row[9] != null) {
                    jobinno = (String) row[9];
                }
                String jobworkorder = "";
                if (row[10] != null) {
                    jobworkorder = (String) row[10];
                }
                String productname = "";
                if (row[11] != null) {
                    productname = (String) row[11];
                }
                KwlReturnObject productResult = null;
                productResult = accountingHandlerDAOobj.getObject(Product.class.getName(), productid);
                Product product = (Product) productResult.getEntityList().get(0);
                backForwardQuantityMap = stockService.getDateWiseStockDetailList(company, null, null, backdate, productname, null);
                double quantity = 0.0;
                if (backForwardQuantityMap != null && backForwardQuantityMap.containsKey(product)) {
                    for (Object[] stock : backForwardQuantityMap.get(product)) {
                        if (stock != null) {
                            quantity += Double.parseDouble(stock[8].toString());
                        }
                    }
                }
                jSONObject.put("productcode", productcode);
                jSONObject.put("productdesc", productname);
                
                jSONObject.put("uom", "Nos");
                jSONObject.put("openingstock", quantity);
                jSONObject.put("recqty", qty);
                
                
                
                /*
                 get Consume details
                 */
                requestParams.put("batchName", batch);
                requestParams.put("productid", productid);
                requestParams.put("jobworkorder", jobworkorder);
                long startdatelong = 0;
                startdatelong = startDate.getTime();
                long enddatelong = endDate.getTime();
                requestParams.put("startdatelong", startdatelong);
                requestParams.put("enddatelong", enddatelong);
                KwlReturnObject result1 = accJobWorkDaoObj.getJWProductSummaryReportOUT(requestParams);
                List list1 = result1.getEntityList();
                Iterator itr1 = list1.iterator();
                double deliveredqty = 0d;
                while (itr1.hasNext()) {
                    Object[] row1 = (Object[]) itr1.next();
                    double deliveredquantity = 0d;
                    if (row1[0] != null) {
                        deliveredquantity = (Double) row1[0];
                    }
                    deliveredqty += deliveredquantity;
                    
                }
                double closingStock = quantity + qty - deliveredqty;
                jSONObject.put("closingstock", closingStock);
                jSONObject.put("despatchqty", deliveredqty);
                
                if (!(quantity == 0 && qty == 0 && closingStock == 0 && deliveredqty == 0) ) {
                    count++;
                    tableDataMap.put("srno", String.valueOf(count));
                    tableDataMap.put("itemcodedesc",productcode +" " + productname);
                    tableDataMap.put("unit","Nos");
                    tableDataMap.put("openingstock",String.valueOf(quantity));
                    tableDataMap.put("recqty",String.valueOf(qty));
                    tableDataMap.put("closingstock",String.valueOf(closingStock));
                    tableDataMap.put("despqty",String.valueOf(deliveredqty));
                    tableDataList.add(tableDataMap);
                }
            }
            
//            challanMap.put("startdate", "hi");
//            challanMap.put("enddate", "hello");
//            challanMap.put("customer", "how are u");
            

            Calendar cal1 = Calendar.getInstance();
            Date date = cal1.getTime();
            String datePattern = "dd/MM/yyyy";
//            challanMap.put("format", "pdf");
            // Below 3 parameter to be put
            challanMap.put("startDate",startDate.toString());
            challanMap.put("endDate",endDate.toString());
            challanMap.put("customerName",customerName);
            challanMap.put("TableData", new JRBeanCollectionDataSource(tableDataList));
//            String fileName1 = "";
//            
//            boolean isLandscape = true;
//            String challanReportJrxml = "/JWProductSummaryReport.jrxml";
//            InputStream inputStream = new FileInputStream(request.optString(Constants.JRXML_REAL_PATH_KEY, "") + challanReportJrxml);
//            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
//            jasperReport = JasperCompileManager.compileReport(jasperDesign);
//            OnlyDatePojo odp = new OnlyDatePojo();
//            odp.setDate(df.format(new Date()));
//            List list3 = new ArrayList();
//            list3.add(odp);
//            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(list3);
//            jasperPrint = JasperFillManager.fillReport(jasperReport, challanMap, beanColDataSource);

        } catch (Exception ex) {
            Logger.getLogger(AccJobWorkServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return challanMap;
    }
    
}
