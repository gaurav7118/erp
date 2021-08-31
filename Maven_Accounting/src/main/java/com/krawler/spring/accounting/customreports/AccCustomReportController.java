/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.customreports;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.creditnote.accCreditNoteServiceCMN;
import com.krawler.spring.accounting.debitnote.accDebitNoteService;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AccCustomReportController extends MultiActionController implements MessageSourceAware {

    private MessageSource messageSource;
    private AccCustomReportService accCustomReportService;    
    private exportMPXDAOImpl exportDaoObj;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setAccCustomReportService(AccCustomReportService accCustomReportService) {
        this.accCustomReportService = accCustomReportService;
    }
    
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    
    public ModelAndView getModulesCategories(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        JSONArray returnJarr = new JSONArray();
        if (sessionHandlerImpl.isValidSession(request, response)) {
            Map<String,Object> requestParams = new HashMap<>();
            String companyID = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyID", companyID);
            requestParams.put("isPivot", request.getParameter("isPivot"));
            logger.info("companyid = " + companyID);
            returnJarr = accCustomReportService.getModuleCategories(requestParams).optJSONArray("data");
            jobj.put("data", returnJarr != null ? returnJarr : new JSONArray());
            jobj.put("success", true);
            jobj.put("msg", "succes");
        } else {
            jobj.put("success", false);
            jobj.put("msg", "timeout");
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());

    }

    public ModelAndView getModules(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException {

        JSONObject jobj = new JSONObject();
        String moduleCatId = request.getParameter("moduleCatIdValue");
        String moduleCatName = request.getParameter("moduleCatName");
        logger.info("module id = " + moduleCatId);
        if (sessionHandlerImpl.isValidSession(request, response)) {
            JSONArray returnJarr = accCustomReportService.getModules(moduleCatId, moduleCatName).optJSONArray("data");
            jobj.put("data", returnJarr != null ? returnJarr : new JSONArray());
            jobj.put("success", true);
            jobj.put("msg", "succes");
        } else {
            jobj.put("success", false);
            jobj.put("msg", "timeout");
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());

    }

    public ModelAndView getFiledsData(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException {

        JSONObject jobj = new JSONObject();
        if (sessionHandlerImpl.isValidSession(request, response)) {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            if (!StringUtil.isNullOrEmpty(request.getParameter("xtype"))) {
                requestParams.put("xtype", request.getParameter("xtype"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isforformulabuilder))) {
                requestParams.put(Constants.isforformulabuilder, request.getParameter(Constants.isforformulabuilder));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("id"))) {
                requestParams.put(Constants.moduleid, request.getParameter("id"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("moduleCategory"))) {
                requestParams.put("moduleCategory", request.getParameter("moduleCategory"));
            }
            requestParams.put(Constants.useridKey, sessionHandlerImpl.getUserid(request));
            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            
            JSONObject returnJobj = accCustomReportService.getFields(requestParams);
            JSONArray returnJarr = returnJobj.optJSONArray("data");
            returnJarr = sortJsonArrayOnTransaction(returnJarr);
            if(!StringUtil.isNullOrEmpty(request.getParameter("moduleCategory")) && request.getParameter("moduleCategory").equals(CustomReportConstants.Reports_ModuleCategoryName)) {
                String reportUrl = returnJobj.optString("reportUrl");
                jobj.put("reportUrl", reportUrl);
            }
            jobj.put("data", returnJarr != null ? returnJarr : new JSONArray());
            jobj.put("success", true);
            jobj.put("msg", "succes");
        } else {
            jobj.put("success", false);
            jobj.put("msg", "timeout");
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());

    }
    
    public ModelAndView saveOrUpdateCustomReport(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException {

        JSONObject jobj = new JSONObject();
        String companyID;
        if (sessionHandlerImpl.isValidSession(request, response)) {
            String moduleCatId = request.getParameter("moduleCategory");
            String moduleCatName = request.getParameter("moduleCategoryName");
            String moduleID = request.getParameter("moduleID");
            String selectedRows = request.getParameter("selectedRows");
            String reportName = request.getParameter("reportName");
            String reportDesc = request.getParameter("reportDesc");
            String deleted = request.getParameter("deleted");
            String nondeleted = request.getParameter("nondeleted");
            String filter = request.getParameter("filter");
            String reportNo = request.getParameter("reportNo");
            Boolean isEdit = Boolean.parseBoolean(request.getParameter("isEdit"));
            String reportUrl = StringUtil.isNullOrEmpty(request.getParameter("reportUrl")) ? "" : request.getParameter("reportUrl");
            String parentReportId = StringUtil.isNullOrEmpty(request.getParameter("parentReportId")) ? "" : request.getParameter("parentReportId");
            JSONArray filterArray=new JSONArray();
            if (!StringUtil.isNullOrEmpty(filter) && !StringUtil.isNullOrEmpty(selectedRows)) {
                filterArray = new JSONArray(filter);
            }
            String pendingapproval = request.getParameter("pendingapproval");
            boolean isLeaseFixedAsset = request.getParameter("isLeaseFixedAsset")!=null?Boolean.valueOf((String)request.getParameter("isLeaseFixedAsset")):false;
            boolean isPivot = StringUtil.isNullOrEmpty(request.getParameter("isPivot")) ? false : Boolean.parseBoolean(request.getParameter("isPivot"));
            boolean isEWayReport = StringUtil.isNullOrEmpty(request.getParameter("isEWayReport")) ? false : Boolean.parseBoolean(request.getParameter("isEWayReport"));
            companyID = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);
            HashMap<String, Object> valueMap;
            valueMap = new HashMap<>();
            valueMap.put("reportUrl", reportUrl);
            valueMap.put("parentReportId", parentReportId);
            valueMap.put("reportNo", reportNo);
            valueMap.put("companyID", companyID);
            valueMap.put("userId", userId);
            valueMap.put("reportName", reportName);
            valueMap.put("reportDesc", reportDesc);
            valueMap.put("moduleCatId", moduleCatId);
            valueMap.put("moduleCatName", moduleCatName);
            valueMap.put("moduleID", moduleID);
            valueMap.put("deleted", deleted);
            valueMap.put("nondeleted", nondeleted);
            valueMap.put("pendingapproval", pendingapproval);
            valueMap.put("isLeaseFixedAsset", isLeaseFixedAsset);
            valueMap.put("isPivot", isPivot);
            valueMap.put("isEWayReport", isEWayReport);
            valueMap.put("isEdit",isEdit);
            valueMap.put("selectedRows", selectedRows);
            if (StringUtil.equal(moduleID, String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                valueMap.put("showGLFlag", Boolean.parseBoolean(request.getParameter("showGLFlag")));
            } else if (StringUtil.equal(moduleID, String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)) || StringUtil.equal(moduleID, String.valueOf(Constants.Acc_Purchase_Order_ModuleId))) {
                valueMap.put("showExpenseTypeTransactionsFlag", Boolean.parseBoolean(request.getParameter("showExpenseTypeTransactionsFlag")));
            }

            JSONObject returnJobj = accCustomReportService.saveOrUpdateCustomReport( valueMap,filterArray);
            jobj.put("data", returnJobj != null ? returnJobj : new JSONObject());
            jobj.put("success", returnJobj.optBoolean("success"));
            jobj.put("msg", messageSource.getMessage(returnJobj.getString(CustomReportConstants.MESSAGE_KEY), null, RequestContextUtils.getLocale(request)));
        } else {
            jobj.put("success", false);
            jobj.put("msg", "timeout");
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());

    }
    
    public ModelAndView isCustomReportNameExists(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        try {
            if (sessionHandlerImpl.isValidSession(request, response)) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("reportName", request.getParameter("reportName"));
                requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                requestParams.put("userId", sessionHandlerImpl.getUserid(request));
                boolean isReportNameExists = accCustomReportService.isCustomReportNameExists(requestParams);
                JSONObject resultObj = new JSONObject();
                if(isReportNameExists) {
                    jobj.put("isReportNameExists", true);
                    jobj.put("success", true);
                    jobj.put("msg", messageSource.getMessage("acc.CustomReport.dupReport", null, RequestContextUtils.getLocale(request)));
                } else {
                    jobj.put("isReportNameExists", false);
                    jobj.put("success", true);
                    jobj.put("msg", "success");
                }
            } else {
                jobj.put("success", false);
                jobj.put("msg", "timeout");
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccCustomReportController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public ModelAndView getCustomReportList(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {

        JSONObject jobj = new JSONObject();
        try {
            //String moduleCatId = request.getParameter("moduleCategory");
            if (sessionHandlerImpl.isValidSession(request, response)) {
                JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
                DateFormat sdf = new SimpleDateFormat(sessionHandlerImpl.getUserDateFormat(request));
                
                JSONObject result = accCustomReportService.getCustomReportList(paramJobj,sdf);
                JSONArray customReportJarr = result.optJSONArray("data");
                int totalCount = result.getInt("totalCount");
                jobj.put("data", customReportJarr != null ? customReportJarr : new JSONArray());
                jobj.put("totalCount", totalCount);
                jobj.put("showPivotInCustomReports", result.optBoolean("showPivotInCustomReports",false));
                jobj.put("countryid", result.opt("countryid"));
                jobj.put("userPref", result.optJSONObject("userPref"));
                jobj.put("role", result.optJSONObject("role"));
                jobj.put("success", true);
                jobj.put("msg", "succes");
            } else {
                jobj.put("success", false);
                jobj.put("msg", "timeout");
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccCustomReportController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

    public ModelAndView executeCustomReport(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException, SessionExpiredException, ParseException,IOException {

        JSONObject jobj = new JSONObject();
        String mainRecordbillid = "";
        HashMap<String, Object> requestParams = new HashMap();
        boolean isEWayReportValidation = request.getParameter("isEWayReportValidation") != null ? Boolean.parseBoolean(request.getParameter("isEWayReportValidation")) : false;
        boolean exportValidRecordsToJSON = request.getParameter("exportValidRecordsToJSON") != null ? Boolean.parseBoolean(request.getParameter("exportValidRecordsToJSON")) : false;
        boolean exportEWayInvalidRecords = request.getParameter("exportEWayInvalidRecords") != null ? Boolean.parseBoolean(request.getParameter("exportEWayInvalidRecords")) : false;
        boolean isWholeData = request.getParameter("isWholeData") != null ? Boolean.parseBoolean(request.getParameter("isWholeData")) : false;
        String eWayFilter = request.getParameter("ewayFilter") != null ? (request.getParameter("ewayFilter")) : "";
        String moduleid = request.getParameter("moduleid") != null ? request.getParameter("moduleid") : null;
        String filter = request.getParameter("filter") != null ? request.getParameter("filter") : "[]";
        String view = "jsonView_ex";
        String searchJson = request.getParameter("searchJson") != null ? request.getParameter("searchJson") : null;
        String companyName =  request.getParameter("companyName") != null ? request.getParameter("companyName") : null;
        String entityName="";
        boolean isEntityFilter = false;
//        JSONArray columns =  customReportDataJarr.getJSONObject(0).getJSONArray("metaData").getJSONObject(0).get("columns");
//        List columnList = accCustomReportService.getMandatoryReportColumn(moduleid);
//        accCustomReportService.checkIfReportHasAllColumns(columnList, null);
        if(!StringUtil.isNullOrEmpty(searchJson) && isEWayReportValidation){
            JSONObject searchJSON = new JSONObject(searchJson);
            JSONArray searchJSONArray = searchJSON.getJSONArray("root");
            JSONObject resObj = accCustomReportService.isEntityFilterApplied(searchJSONArray, moduleid);
            isEntityFilter = resObj.optBoolean("isModuleEntityFilterApplied");
            entityName = resObj.optString("entityName");
        }
        if (isEWayReportValidation && !isEntityFilter) {
            jobj.put("success", false);
            jobj.put("msg", "Please apply Advance Search filter on module's Entity Field");
        } else {

            if (sessionHandlerImpl.isValidSession(request, response)) {
                String reportID = request.getParameter("reportID");
                String start = request.getParameter("start") != null ? request.getParameter("start") : "";
                String limit = request.getParameter("limit") != null ? request.getParameter("limit") : "";
                String fromDate = request.getParameter("fromDate") != null ? request.getParameter("fromDate") : null;
                String toDate = request.getParameter("toDate") != null ? request.getParameter("toDate") : null;
                String filterConjuctionCriteria = request.getParameter("filterConjuctionCriteria") != null ? request.getParameter("filterConjuctionCriteria") : null;
                boolean showRowLevelFieldsflag = Boolean.valueOf((String) request.getParameter("showRowLevelFieldsflag"));
                boolean forExport = Boolean.valueOf((String) request.getParameter("forExport")) == null ? false : Boolean.valueOf((String) request.getParameter("forExport"));
                boolean isLeaseFixedAsset = request.getParameter("isLeaseFixedAsset") != null ? Boolean.valueOf((String) request.getParameter("isLeaseFixedAsset")) : false;
                boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
                boolean isreportloaded = request.getParameter("isreportloaded") != null ? Boolean.parseBoolean(request.getParameter("isreportloaded")) : false;
                boolean isclearfilter = request.getParameter("isclearfilter") != null ? Boolean.parseBoolean(request.getParameter("isclearfilter")) : false;
                boolean isCustomWidgetReport = request.getParameter("isCustomWidgetReport") != null ? Boolean.parseBoolean(request.getParameter("isCustomWidgetReport")) : false;
                boolean isChartRequest = request.getParameter("isChartRequest") != null ? Boolean.parseBoolean(request.getParameter("isChartRequest")) : false;
                String titleField = request.getParameter("titleField") != null ? request.getParameter("titleField") : "";
//            String titleFieldStoreIndex = request.getParameter("titleFieldStoreIndex") != null ? request.getParameter("titleFieldStoreIndex") : "";
                String valueField = request.getParameter("valueField") != null ? request.getParameter("valueField") : "";
//            String valueFieldStoreIndex = request.getParameter("valueFieldStoreIndex") != null ? request.getParameter("valueFieldStoreIndex") : "";
                String groupby = request.getParameter("groupby") != null ? request.getParameter("groupby") : "";

            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid")!=null)?request.getParameter("gcurrencyid"):sessionHandlerImpl.getCurrencyID(request);   
                String companyID = sessionHandlerImpl.getCompanyid(request);
                String userId = sessionHandlerImpl.getUserid(request);
                DateFormat sdf = new SimpleDateFormat(sessionHandlerImpl.getUserDateFormat(request));
                if (!StringUtil.isNullOrEmpty(moduleid) && moduleid.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId))) {
                    JSONObject creditNoteParamJobj = StringUtil.convertRequestToJsonObject(request);
                    requestParams.put("creditNoteParamJobj", creditNoteParamJobj);
                    requestParams.put("dateformatid", sessionHandlerImpl.getDateFormatID(request));
                    //KWLTimeZone timeZone = (KWLTimeZone) accCustomReportService.getTimzeZoneClassObject(KWLTimeZone.class.getName(), storageHandlerImpl.getDefaultTimeZoneID());
                    //requestParams.put(Constants.timezonedifference, timeZone.getDifference());
                    requestParams.put(Constants.timezonedifference, creditNoteParamJobj.get(Constants.timezonedifference));
                }
                requestParams.put("reportID", reportID);
                requestParams.put("userdf", sdf);
                requestParams.put("companyID", companyID);
                requestParams.put("userId", userId);
                requestParams.put("deleted", request.getParameter("deleted"));
                requestParams.put("nondeleted", request.getParameter("nondeleted"));
                requestParams.put("pendingapproval", request.getParameter("pendingapproval"));
                requestParams.put("userDateFormat", sessionHandlerImpl.getUserDateFormat(request));
                requestParams.put("isLeaseFixedAsset", isLeaseFixedAsset);
                requestParams.put("isWholeData", isWholeData);
                requestParams.put("exportValidRecordsToJSON", exportValidRecordsToJSON);
                requestParams.put("eWayFilter", eWayFilter);
                requestParams.put("isEWayReportValidation", isEWayReportValidation);
                requestParams.put("exportEWayInvalidRecords", exportEWayInvalidRecords);
                if (!StringUtil.isNullOrEmpty(companyName)) {
                    requestParams.put("companyName", companyName);
                }
                if (!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))) {
                    requestParams.put("browsertz", sessionHandlerImpl.getBrowserTZ(request));
                }
                requestParams.put("df1", authHandler.getOnlyDateFormat(request));
                requestParams.put("df", authHandler.getDateOnlyFormat(request));
                requestParams.put("start", start);
                requestParams.put("limit", limit);
                requestParams.put("fromDate", fromDate);
                requestParams.put("toDate", toDate);
                requestParams.put("showRowLevelFieldsflag", showRowLevelFieldsflag);
                requestParams.put("forExport", forExport);
                requestParams.put("searchJson", searchJson);
                requestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                requestParams.put("moduleid", moduleid);
                requestParams.put("gcurrencyid", gcurrencyid);
                if (!StringUtil.isNullOrEmpty((String) request.getParameter("billid"))) {
                    String billid = (String) request.getParameter("billid");
                mainRecordbillid=billid;
                    String[] recarr = billid.split(",");
                    StringBuilder recordsbuffer = new StringBuilder();
                    String recordids = "";
                    for (int i = 0; i < recarr.length; i++) {
                        recordsbuffer.append("'" + recarr[i] + "'").append(",");
                    }
                    if (recordsbuffer.length() > 0) {
                        recordids = recordsbuffer.substring(0, (recordsbuffer.length() - 1));
                        requestParams.put("billid", recordids);
                    }
                }
                if (!StringUtil.isNullOrEmpty((String) request.getParameter("linkedbillid"))) {
                    String linkedBillIdStr = (String) request.getParameter("linkedbillid");
                    String[] linkedBillIds = linkedBillIdStr.split(",");
                    StringBuilder recordsbuffer = new StringBuilder();
                    for (int i = 0; i < linkedBillIds.length; i++) {
                        recordsbuffer.append("'" + linkedBillIds[i] + "'").append(",");
                    }
                    if (recordsbuffer.length() > 0) {
                        requestParams.put("linkedbillid", recordsbuffer.substring(0, (recordsbuffer.length() - 1)));
                    }
                }
                requestParams.put("filter", filter);
                requestParams.put("isreportloaded", isreportloaded);
                requestParams.put("isclearfilter", isclearfilter);
                requestParams.put("isCustomWidgetReport", isCustomWidgetReport);
                requestParams.put("isChartRequest", isChartRequest);
                requestParams.put("titleField", titleField);
//            requestParams.put("titleFieldStoreIndex", titleFieldStoreIndex);
                requestParams.put("valueField", valueField);
//            requestParams.put("valueFieldStoreIndex", valueFieldStoreIndex);
                requestParams.put("groupby", groupby);
                JSONObject customReportDataObj = accCustomReportService.executeCustomReport(requestParams);
            if(customReportDataObj!=null && customReportDataObj.has("data")) {
                    JSONArray customReportDataJarr = customReportDataObj.optJSONArray("data");
                    if ((Boolean) customReportDataJarr.getJSONObject(0).getJSONArray("metaData").getJSONObject(0).get("success") == true) {
//                    jobj.put("metaData", customReportDataJarr.getJSONObject(0).getJSONArray("metaData"));
                        jobj.put("data", customReportDataJarr.getJSONObject(1).getJSONArray("reportdata"));
                        jobj.put("columns", customReportDataJarr.getJSONObject(0).getJSONArray("metaData").getJSONObject(0).get("columns"));
                        jobj.put("metaData", customReportDataJarr.optJSONObject(0).optJSONArray("metaData").optJSONObject(0).optJSONObject("storerec"));
                        jobj.put("sortConfigArray", customReportDataObj.getJSONArray("sortConfigArray"));
                        jobj.put("totalCount", customReportDataJarr.getInt(2));
                        jobj.put("userPreferences", customReportDataJarr.get(3));
                        jobj.put("pivotConfig", customReportDataObj.optJSONObject("pivotConfig"));
                        jobj.put("success", true);
                        jobj.put("msg", "succes");
                        jobj.put("reportID", reportID);//if any record is not there in expander getting expander id from main row. This is done for expander only.
                }else{
                        jobj.put("billid", mainRecordbillid);//if any record is not there in expander getting expander id from main row. This is done for expander only.
                        jobj.put("reportID", reportID);//if any record is not there in expander getting expander id from main row. This is done for expander only.
                    }

                } else {
                    String expCause = "";
                if(customReportDataObj.has("Exception")){
                            expCause =  customReportDataObj.optString("Exception");
                    }
                jobj.put("success", customReportDataObj.optString("success","false"));
                if(expCause.contains(CustomReportConstants.BadSqlGrammarException)){
                            jobj.put("msg",  messageSource.getMessage("acc.CustomReport.executionErrorMessage", null, RequestContextUtils.getLocale(request)));                            
                    } else {
                            jobj.put("msg", messageSource.getMessage(customReportDataObj.optString(CustomReportConstants.MESSAGE_KEY,"acc.field.Erroroccuredatserverside"), null, RequestContextUtils.getLocale(request)));                
                    }
                }
                jobj.put("filter", customReportDataObj.opt("filter"));
            if(isCustomWidgetReport){
                    view = "jsonView";
                }
            } else {
                jobj.put("success", false);
                jobj.put("msg", "timeout");
            }
            if (isEWayReportValidation) {
                requestParams.put("entityName", entityName);
                Map resultMap = accCustomReportService.validateEWayBillReport(jobj, requestParams);
                JSONObject resultJSON = (JSONObject) resultMap.get("dataObject");
//                Map separatedRecords = accCustomReportService.separateValidInvalidEWayRecords(resultJSON);
                Map separatedRecords = (HashMap) resultMap.get("separatedRecords");
                if (!exportEWayInvalidRecords && !exportValidRecordsToJSON && separatedRecords != null) {
                    boolean keepJSONExportEnabled = accCustomReportService.getJSONExportButtonStatus((JSONArray) separatedRecords.get("validRecords"), (HashSet) separatedRecords.get("invalidDocuments"));
                    resultJSON.put("keepJSONExportEnabled", keepJSONExportEnabled);
                }
                if (exportEWayInvalidRecords) {
                    JSONObject exportJSON = new JSONObject();
                    exportJSON.put("data", (separatedRecords.get("invalidRecords") != null ? separatedRecords.get("invalidRecords") : null));
                    exportDaoObj.processRequest(request, response, exportJSON);
                } else if (exportValidRecordsToJSON) {
                    Map keyValue = (resultMap.get("keyValue") != null ? (HashMap) resultMap.get("keyValue") : null);
                    separatedRecords.put("keyValue", keyValue);
                    separatedRecords.put("moduleId", moduleid);
                    separatedRecords.put("removeSpecialCharachter", resultMap.get("removeSpecialCharachter"));
                    JSONObject jsonExport = accCustomReportService.getJSONtoExport(separatedRecords);
                    String fileType = "json";
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    os.write(jsonExport.toString().getBytes());
                    exportDaoObj.writeDataToFile("EwayJSON", fileType, os, response);
                }
                return new ModelAndView(view, "model", resultJSON.toString());
            }

        }
        return new ModelAndView(view, "model", jobj.toString());

    }
    
    public ModelAndView revertEWayStatus(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        String reportIds = request.getParameter("reportIds") != null ? (request.getParameter("reportIds")) : "";
        String moduleId = request.getParameter("moduleId") != null ? request.getParameter("moduleId") : null;
        Map requestParam = new HashMap();
        requestParam.put("reportIds", reportIds);
        requestParam.put("moduleId", moduleId);
        try {
            accCustomReportService.revertEWayStatus(requestParam);
            jobj.put("success", true);
        } catch (ServiceException | JSONException ex) {
            Logger.getLogger(AccCustomReportController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                jobj.put("success", false);
            } catch (Exception ex1) {
                Logger.getLogger(AccCustomReportController.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        String view = "jsonView_ex";
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView executeCustomReportPreview(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException,IOException, ParseException {

        JSONObject jobj = new JSONObject();
        String companyID;
        String mainrecordbillid = "";
        Map<String, Object> jsonArrayMap = new HashMap<String, Object>();
        if (sessionHandlerImpl.isValidSession(request, response)) {
            String selectedRows = request.getParameter("selectedRows");
            boolean showRowLevelFieldsflag = Boolean.valueOf((String) request.getParameter("showRowLevelFieldsflag"));
            boolean isLeaseFixedAsset = request.getParameter("isLeaseFixedAsset") != null ? Boolean.valueOf((String) request.getParameter("isLeaseFixedAsset")) : false;
            String moduleID = request.getParameter("moduleID");
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            String deleted = request.getParameter("deleted");
            String nondeleted = request.getParameter("nondeleted");
            String pendingapproval = request.getParameter("pendingapproval");
            String filter = request.getParameter("filter") != null ? request.getParameter("filter") : "[]";
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            boolean isPivot = request.getParameter("isPivot") != null ? Boolean.parseBoolean(request.getParameter("isPivot")) : false;
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);

            Boolean isClearPreview = request.getParameter("isClearPreview") != null ? Boolean.valueOf((String) request.getParameter("isClearPreview")) : false;

            if (!isClearPreview) {
                JSONObject returnJobj = null;
                companyID = sessionHandlerImpl.getCompanyid(request);
                String userId = sessionHandlerImpl.getUserid(request);
                HashMap<String, Object> valueMap;
                valueMap = new HashMap<>();
                valueMap.put("companyID", companyID);
                valueMap.put("moduleID", moduleID);
                valueMap.put("userId", userId);
                valueMap.put("timezoneid", sessionHandlerImpl.getTimeZoneID(request));
                valueMap.put("dateformatid", sessionHandlerImpl.getDateFormatID(request));
                valueMap.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
                valueMap.put("userTimeFormat", sessionHandlerImpl.getUserTimeFormat(request));
                valueMap.put("userDateFormatWTF", sessionHandlerImpl.getDateFormatterWithUserTimeFormat(request));
                valueMap.put("userDateFormat", sessionHandlerImpl.getUserDateFormat(request));
                valueMap.put("userdf", new SimpleDateFormat(sessionHandlerImpl.getUserDateFormat(request)));
                valueMap.put("isLeaseFixedAsset", isLeaseFixedAsset);
                if (!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))) {
                    valueMap.put("browsertz", sessionHandlerImpl.getBrowserTZ(request));
                }
                valueMap.put("start", start);
                valueMap.put("limit", limit);
                valueMap.put("deleted", deleted);
                valueMap.put("nondeleted", nondeleted);
                valueMap.put("pendingapproval", pendingapproval);
                valueMap.put("gcurrencyid", gcurrencyid);
                valueMap.put("isPivot", isPivot);
                valueMap.put("df", authHandler.getDateOnlyFormat(request));
                
                if (!StringUtil.isNullOrEmpty((String) request.getParameter("billid"))) {
                    String billid = (String) request.getParameter("billid");
                    mainrecordbillid = billid;
                    String[] recarr = billid.split(",");
                    StringBuilder recordsbuffer = new StringBuilder();
                    String recordids = "";
                    for (int i = 0; i < recarr.length; i++) {
                        recordsbuffer.append("'" + recarr[i] + "'").append(",");
                    }
                    if (recordsbuffer.length() > 0) {
                        recordids = recordsbuffer.substring(0, (recordsbuffer.length() - 1));
                        valueMap.put("billid", recordids);
                    }
                }
                if (!StringUtil.isNullOrEmpty((String) request.getParameter("linkedbillid"))) {
                    String linkedBillIdStr = (String) request.getParameter("linkedbillid");
                    String[] linkedBillIds = linkedBillIdStr.split(",");
                    StringBuilder recordsbuffer = new StringBuilder();
                    for (int i = 0; i < linkedBillIds.length; i++) {
                        recordsbuffer.append("'" + linkedBillIds[i] + "'").append(",");
                    }
                    if (recordsbuffer.length() > 0) {
                        valueMap.put("linkedbillid", recordsbuffer.substring(0, (recordsbuffer.length() - 1)));
                    }
                }
                valueMap.put("showRowLevelFieldsflag", showRowLevelFieldsflag);
                valueMap.put("filter", filter);
                JSONArray selectedRowsJSON = new JSONArray(selectedRows);
                if (showRowLevelFieldsflag && ((moduleID.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId))) || (moduleID.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId))))) {
                    boolean isCrossModuleWithCNDN = false;
                    if (selectedRowsJSON.length() > 0) {
                        for (int columnCnt = 0; columnCnt < selectedRowsJSON.length(); columnCnt++) {
                            isCrossModuleWithCNDN = selectedRowsJSON.getJSONObject(columnCnt).optBoolean("allowcrossmodule", false);
                            if (isCrossModuleWithCNDN) {
                                break;
                            }
                        }
                    }
                    jsonArrayMap = accCustomReportService.showRowLevelFieldsJSONArray(selectedRowsJSON, showRowLevelFieldsflag);
                    if (jsonArrayMap.containsKey("jarrColumns")) {
                        selectedRowsJSON = (JSONArray) jsonArrayMap.get("jarrColumns");
                    }
                    JSONObject filteredColumns = accCustomReportService.filterAccountsAndInvoiceColumns(selectedRowsJSON);
                    JSONArray accountColumns = null, invoiceColumns = null;
                    JSONObject dataJobj = new JSONObject();
                    if (filteredColumns != null && filteredColumns.optJSONArray("accountColumns").length() > 0) {
                        accountColumns = filteredColumns.getJSONArray("accountColumns");
                    }
                    if (accountColumns != null && accountColumns.length() > 0) {
                        returnJobj = accCustomReportService.executeCustomReportPreview(accountColumns, valueMap);
                        dataJobj.put("accountData", returnJobj != null ? returnJobj.optJSONArray("data") : new JSONArray());
                    }
                    if (filteredColumns != null && filteredColumns.optJSONArray("invoiceColumns").length() > 0) {
                        invoiceColumns = filteredColumns.getJSONArray("invoiceColumns");
                    }
                    if (invoiceColumns != null && invoiceColumns.length() > 0) {
                        returnJobj = accCustomReportService.executeCustomReportPreview(invoiceColumns, valueMap);
                        dataJobj.put("invoiceData", returnJobj != null ? returnJobj.optJSONArray("data") : new JSONArray());
                    }
                        jobj.put("data", dataJobj);
                        jobj.put("columns", filteredColumns);
                        jobj.put("success", true);
                        jobj.put("msg", "succes");
                        jobj.put("billid", mainrecordbillid);//if any record is not there in expander getting expander id from main row. This is done for expander only.
                } else if(showRowLevelFieldsflag && (moduleID.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId)))){
                    JSONArray invoiceColumns = new JSONArray();
                    JSONArray linkedInvoiceColumns = new JSONArray();
                    JSONArray linkedCreditNoteColumns = new JSONArray();
                    JSONArray GLColumns = new JSONArray();
                    JSONArray refundColumns = new JSONArray();
                    JSONArray linkedRefundColumns = new JSONArray();
                    JSONArray creditNoteColumns = new JSONArray();
                    JSONArray advancePaymentColumns = new JSONArray();
                    JSONArray loanColumns = new JSONArray();
                    JSONObject dataJobj = new JSONObject();
                    JSONObject columns = new JSONObject();
                    jsonArrayMap = accCustomReportService.showRowLevelFieldsJSONArray(selectedRowsJSON, showRowLevelFieldsflag);
                    if (jsonArrayMap.containsKey("jarrColumns")) {
                        selectedRowsJSON = (JSONArray) jsonArrayMap.get("jarrColumns");
                    }
                    if (selectedRowsJSON.length() > 0) {
                        for (int columnCnt = 0; columnCnt < selectedRowsJSON.length(); columnCnt++) {
                            String defaultHeader = selectedRowsJSON.getJSONObject(columnCnt).optString("defaultHeader");
                            boolean iscustomField = selectedRowsJSON.getJSONObject(columnCnt).optBoolean("customfield");
//                            if (!iscustomField) {
                                if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Linked_Invoice) > -1) {
                                    linkedInvoiceColumns.put(selectedRowsJSON.getJSONObject(columnCnt));
                                } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Linked_Refund) > -1) {
                                    linkedRefundColumns.put(selectedRowsJSON.getJSONObject(columnCnt));
                                } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Linked_Credit_Note) > -1 || defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Linked_Debit_Note) > -1) {
                                    linkedCreditNoteColumns.put(selectedRowsJSON.getJSONObject(columnCnt));
                                } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Invoice) > -1) {
                                    invoiceColumns.put(selectedRowsJSON.getJSONObject(columnCnt));
                                } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Credit_Note) > -1 || defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Debit_Note) > -1) {
                                    creditNoteColumns.put(selectedRowsJSON.getJSONObject(columnCnt));
                                } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Advance_Payment) > -1 || defaultHeader.equals(CustomReportConstants.Product_Tax_Class)) {
                                    advancePaymentColumns.put(selectedRowsJSON.getJSONObject(columnCnt));
                                } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_GL) > -1) {
                                    GLColumns.put(selectedRowsJSON.getJSONObject(columnCnt));
                                } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Refund) > -1) {
                                    refundColumns.put(selectedRowsJSON.getJSONObject(columnCnt));
                                } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Receive_Payment_Loan) > -1) {
                                    loanColumns.put(selectedRowsJSON.getJSONObject(columnCnt));
                                }
//                            }
                        }
                        if (invoiceColumns.length() > 0) {
                            returnJobj = accCustomReportService.executeCustomReportPreview(invoiceColumns, valueMap);
                            dataJobj.put("invoiceData", returnJobj != null ? returnJobj.optJSONArray("data") : new JSONArray());
                        }
                  
                        if (creditNoteColumns.length() > 0) {
                            returnJobj = accCustomReportService.executeCustomReportPreview(creditNoteColumns, valueMap);
                            dataJobj.put("creditNoteData", returnJobj != null ? returnJobj.optJSONArray("data") : new JSONArray());
                        }
                        
                        if (advancePaymentColumns.length() > 0) {
                            returnJobj = accCustomReportService.executeCustomReportPreview(advancePaymentColumns, valueMap);
                            dataJobj.put("advancePaymentData", returnJobj != null ? returnJobj.optJSONArray("data") : new JSONArray());
                        }
                        
                        if (GLColumns.length() > 0) {
                            returnJobj = accCustomReportService.executeCustomReportPreview(GLColumns, valueMap);
                            dataJobj.put("GLData", returnJobj != null ? returnJobj.optJSONArray("data") : new JSONArray());
                        }
                        
                        if (refundColumns.length() > 0) {
                            returnJobj = accCustomReportService.executeCustomReportPreview(refundColumns, valueMap);
                            dataJobj.put("refundData", returnJobj != null ? returnJobj.optJSONArray("data") : new JSONArray());
                        }
                        
                        if (linkedInvoiceColumns.length() > 0) {
                            returnJobj = accCustomReportService.executeCustomReportPreview(linkedInvoiceColumns, valueMap);
                            dataJobj.put("linkedInvoiceData", returnJobj != null ? returnJobj.optJSONArray("data") : new JSONArray());
                        }
                        
                        if (linkedCreditNoteColumns.length() > 0) {
                            returnJobj = accCustomReportService.executeCustomReportPreview(linkedCreditNoteColumns, valueMap);
                            dataJobj.put("linkedCreditNoteData", returnJobj != null ? returnJobj.optJSONArray("data") : new JSONArray());
                        }
                        
                        if (linkedRefundColumns.length() > 0) {
                            returnJobj = accCustomReportService.executeCustomReportPreview(linkedRefundColumns, valueMap);
                            dataJobj.put("linkedRefundData", returnJobj != null ? returnJobj.optJSONArray("data") : new JSONArray());
                        }
                        
                        if (loanColumns.length() > 0) {
                            returnJobj = accCustomReportService.executeCustomReportPreview(loanColumns, valueMap);
                            dataJobj.put("loanData", returnJobj != null ? returnJobj.optJSONArray("data") : new JSONArray());
                        }
                        
                        columns.put("invoiceColumns",invoiceColumns);
                        columns.put("creditNoteColumns",creditNoteColumns);
                        columns.put("advancePaymentColumns",advancePaymentColumns);
                        columns.put("GLColumns",GLColumns);
                        columns.put("refundColumns",refundColumns);
                        columns.put("linkedInvoiceColumns",linkedInvoiceColumns);
                        columns.put("linkedCreditNoteColumns",linkedCreditNoteColumns);
                        columns.put("linkedRefundColumns",linkedRefundColumns);
                        columns.put("loanColumns",loanColumns);
                        columns.put("length",selectedRowsJSON.length());
                        
                        jobj.put("success", true);
                        jobj.put("msg", "succes");
                        jobj.put("data", dataJobj);
                        jobj.put("columns", columns);
                        jobj.put("billid", mainrecordbillid);
                    }
                } else {
                    jsonArrayMap = accCustomReportService.showRowLevelFieldsJSONArray(selectedRowsJSON, showRowLevelFieldsflag);
                    if (jsonArrayMap.containsKey("jarrColumns")) {
                        selectedRowsJSON = (JSONArray) jsonArrayMap.get("jarrColumns");
                    }
                    if (selectedRowsJSON.length() > 0) {
                        returnJobj = accCustomReportService.executeCustomReportPreview(selectedRowsJSON, valueMap);
                        if(returnJobj.has("dataIndexObject")){
                        jobj.put("dataIndexObject", returnJobj.get("dataIndexObject"));
                        jobj.put("sortConfigArray", returnJobj.getJSONArray("sortConfigArray"));
                        jobj.put("totalCount", returnJobj.optInt("totalCount", 0));
                        jobj.put("success", true);
                        jobj.put("msg", "succes");
                        jobj.put("data", returnJobj != null ? returnJobj.optJSONArray("data") : new JSONArray());
                        jobj.put("billid", mainrecordbillid);//if any record is not there in expander getting expander id from main row. This is done for expander only.
                        }
                        //jobj.put("dataIndexArray",returnJobj.get("dataIndexArray"));    
                    else{
                        String expCause = "";     
                        if(returnJobj.has("Exception")){
                            expCause =  returnJobj.optString("Exception");
                        }    
                        jobj.put("success","false");
                        if(expCause.contains(CustomReportConstants.BadSqlGrammarException)){
                            jobj.put("msg",  messageSource.getMessage("acc.CustomReport.executionErrorMessage", null, RequestContextUtils.getLocale(request)));                            
                        } else {
                            jobj.put("msg",  messageSource.getMessage("acc.common.errorOccuratServerSide", null, RequestContextUtils.getLocale(request)));
                        }
                        }
                    } else {
                        jobj.put("billid", mainrecordbillid);
                        jobj.put("success", false);
                    }
                }

            } else {
                jobj.put("totalCount", 0);
                jobj.put("success", true);
                jobj.put("msg", "success");
                jobj.put("data", new JSONArray());
            }
            
        } else {
            jobj.put("success", false);
            jobj.put("msg", "timeout");
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }    
    
    public ModelAndView updateCustomReportNameAndDescription(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException {

        JSONObject jobj = new JSONObject();
        if (sessionHandlerImpl.isValidSession(request, response)) {
            String companyID;
            String reportNo = request.getParameter("reportNo");
            String reportNewName = request.getParameter("reportNewName");
            String reportNewDesc = request.getParameter("reportNewDesc");
            String isreportNameFieldEdited = request.getParameter("isreportNameFieldEdited");
            companyID = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);
            HashMap<String, Object> valueMap;
            valueMap = new HashMap<>();
            valueMap.put("reportNo", reportNo);
            valueMap.put("browsertz", sessionHandlerImpl.getBrowserTZ(request));
            valueMap.put("userDateFormat", sessionHandlerImpl.getUserDateFormat(request));
            valueMap.put("companyID", companyID);
            valueMap.put("userId", userId);
            valueMap.put("reportNewName", reportNewName);
            valueMap.put("reportNewDesc", reportNewDesc);
            valueMap.put("isreportNameFieldEdited", isreportNameFieldEdited);
            jobj = accCustomReportService.updateCustomReportNameAndDescription(valueMap);
            jobj.put("msg", messageSource.getMessage(jobj.getString(CustomReportConstants.MESSAGE_KEY), null, RequestContextUtils.getLocale(request)));     
            
        } else {
            jobj.put("success", false);
            jobj.put("msg", "timeout");
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());

    }
    
    public ModelAndView deleteCustomReport(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException {

        JSONObject jobj = new JSONObject();
        if (sessionHandlerImpl.isValidSession(request, response)) {
            boolean isReportDeleted = false;
            String companyID;
            String reportIds = request.getParameter("reportIds");
            companyID = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);
            HashMap<String, Object> valueMap;
            valueMap = new HashMap<>();
            valueMap.put("reportIds", reportIds);
            valueMap.put("companyID", companyID);
            valueMap.put("userId", userId);
            isReportDeleted = accCustomReportService.deleteCustomReport(valueMap);
            if (isReportDeleted) {
                jobj.put("success", true);
                jobj.put("msg", messageSource.getMessage("acc.CustomReport.reportDeleteSuccess", null, RequestContextUtils.getLocale(request)));
            } else {
                jobj.put("success", false);
                jobj.put("msg", messageSource.getMessage("acc.CustomReport.reportDeleteFail", null, RequestContextUtils.getLocale(request)));
            }
        } else {
            jobj.put("success", false);
            jobj.put("msg", "timeout");
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());

    }

    public JSONArray sortJsonArrayOnTransaction(JSONArray array) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            Collections.sort(jsons, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject lhs, JSONObject rhs) {
                    String lid = "", rid = "";
                    try {
                        lid = lhs.getString("defaultHeader");
                        rid = rhs.getString("defaultHeader");
                    } catch (JSONException ex) {
                        Logger.getLogger(AccCustomReportController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return lid.compareTo(rid);
                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccCustomReportController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new JSONArray(jsons);
    }
     public ModelAndView saveCustomWidgetReports(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException {

        JSONObject jobj = new JSONObject();
        if (sessionHandlerImpl.isValidSession(request, response)) {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accCustomReportService.saveCustomWidgetReports(paramJobj);
            
        } else {
            jobj.put("success", false);
            jobj.put("valid", false);
            jobj.put("msg", "timeout");
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }
    
    public ModelAndView getCustomWidgetReports(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {

        JSONObject jobj = new JSONObject();
        try {
            if (sessionHandlerImpl.isValidSession(request, response)) {
                JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
                jobj = accCustomReportService.getCustomWidgetReports(paramJobj);
                jobj.put("valid", true);
            } else {
                jobj.put("success", false);
                jobj.put("msg", "timeout");
                jobj.put("valid", false);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public ModelAndView deleteCustomWidgetReport(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException {

        JSONObject jobj = new JSONObject();
        if (sessionHandlerImpl.isValidSession(request, response)) {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            boolean isReportDeleted = false;
            isReportDeleted = accCustomReportService.deleteCustomWidgetReport(paramJobj);
            if (isReportDeleted) {
                jobj.put("success", true);
                jobj.put("msg", messageSource.getMessage("acc.CustomReport.reportDeleteSuccess", null, RequestContextUtils.getLocale(request)));
            } else {
                jobj.put("success", false);
                jobj.put("msg", messageSource.getMessage("acc.CustomReport.reportDeleteFail", null, RequestContextUtils.getLocale(request)));
            }
        } else {
            jobj.put("success", false);
            jobj.put("msg", "timeout");
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }
    
    public ModelAndView executeCustomizedReportPreview(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException,IOException {
        JSONObject jobj = new JSONObject();
        if (sessionHandlerImpl.isValidSession(request, response)) {            
            Boolean isClearPreview = request.getParameter("isClearPreview") != null ? Boolean.valueOf((String) request.getParameter("isClearPreview")) : false;
            if (!isClearPreview) {
                String parentReportId = request.getParameter("parentReportId");
                String companyID = sessionHandlerImpl.getCompanyid(request);
                String userId = sessionHandlerImpl.getUserid(request);
                String selectedRows = request.getParameter("selectedRows");
                boolean showRowLevelFieldsflag = Boolean.valueOf((String) request.getParameter("showRowLevelFieldsflag"));
                boolean isLeaseFixedAsset = request.getParameter("isLeaseFixedAsset") != null ? Boolean.valueOf((String) request.getParameter("isLeaseFixedAsset")) : false;
                String start = request.getParameter("start");
                String limit = request.getParameter("limit");
                String deleted = request.getParameter("deleted");
                String nondeleted = request.getParameter("nondeleted");
                String pendingapproval = request.getParameter("pendingapproval");
                String filter = request.getParameter("filter") != null ? request.getParameter("filter") : "[]";
                boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
                boolean isPivot = request.getParameter("isPivot") != null ? Boolean.parseBoolean(request.getParameter("isPivot")) : false;
                String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
                JSONObject returnJobj = null;
                HashMap<String, Object> valueMap;
                valueMap = new HashMap<>();
                valueMap.put("companyID", companyID);
                valueMap.put("parentReportId", parentReportId);
                valueMap.put("timezoneid", sessionHandlerImpl.getTimeZoneID(request));
                valueMap.put("dateformatid", sessionHandlerImpl.getDateFormatID(request));
                valueMap.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
                valueMap.put("userTimeFormat", sessionHandlerImpl.getUserTimeFormat(request));
                valueMap.put("userDateFormatWTF", sessionHandlerImpl.getDateFormatterWithUserTimeFormat(request));
                valueMap.put("userDateFormat", sessionHandlerImpl.getUserDateFormat(request));
                valueMap.put("isLeaseFixedAsset", isLeaseFixedAsset);
                if (!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))) {
                    valueMap.put("browsertz", sessionHandlerImpl.getBrowserTZ(request));
                }
                valueMap.put("start", start);
                valueMap.put("limit", limit);
                valueMap.put("deleted", deleted);
                valueMap.put("nondeleted", nondeleted);
                valueMap.put("pendingapproval", pendingapproval);
                valueMap.put("gcurrencyid", gcurrencyid);
                valueMap.put("isPivot", isPivot);                
                valueMap.put("showRowLevelFieldsflag", showRowLevelFieldsflag);
                valueMap.put("filter", filter);
                JSONArray reportJArr = new JSONArray(request.getParameter("reportData"));
                JSONArray columnsJArr = new JSONArray(selectedRows);
                JSONObject resultJobj = accCustomReportService.executeCustomizedReportPreview(reportJArr, columnsJArr, valueMap);
                JSONArray sortConfigArray = resultJobj.optJSONArray("sortConfigArray");
                reportJArr = resultJobj.optJSONArray("reportJArr");
                JSONObject columnsJObj = resultJobj.optJSONObject("columnsJObj");
                jobj.put("sortConfigArray", sortConfigArray);
                jobj.put("dataIndexObject", columnsJObj);
                jobj.put("data", reportJArr);
                jobj.put("totalCount", reportJArr.length());
                jobj.put("billid", "");
                jobj.put("success", true);
                jobj.put("msg", "success");
            } else {
                jobj.put("totalCount", 0);
                jobj.put("success", true);
                jobj.put("msg", "success");
                jobj.put("data", new JSONArray());
            }
        } else {
            jobj.put("success", false);
            jobj.put("msg", "timeout");
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

    public ModelAndView getCustomizedReportURLandParams(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        if (sessionHandlerImpl.isValidSession(request, response)) {
            String parentReportId = request.getParameter("parentReportId");
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("parentReportId", parentReportId);
            JSONObject resultJObj = accCustomReportService.getCustomizedReportURLandParams(requestParams);
            jobj.put("data",resultJObj);
            jobj.put("success",true);
            jobj.put("msg","success");
        } else {
            jobj.put("success", false);
            jobj.put("msg", "timeout");
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public ModelAndView executeCustomizedReport(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException,IOException {
        JSONObject jobj = new JSONObject();
        if (sessionHandlerImpl.isValidSession(request, response)) {
            String reportID = request.getParameter("reportID");
            String parentReportId = request.getParameter("parentReportId"); //moduleID is reportId of report from Report-List in our case
            String companyID = sessionHandlerImpl.getCompanyid(request);
            boolean showRowLevelFieldsflag = Boolean.valueOf((String) request.getParameter("showRowLevelFieldsflag"));
            boolean isLeaseFixedAsset = request.getParameter("isLeaseFixedAsset") != null ? Boolean.valueOf((String) request.getParameter("isLeaseFixedAsset")) : false;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            String deleted = request.getParameter("deleted");
            String nondeleted = request.getParameter("nondeleted");
            String pendingapproval = request.getParameter("pendingapproval");
            String filter = request.getParameter("filter") != null ? request.getParameter("filter") : "[]";
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            HashMap<String, Object> valueMap;
            valueMap = new HashMap<>();
            valueMap.put("companyID", companyID);
            valueMap.put("parentReportId", parentReportId);
            valueMap.put("timezoneid", sessionHandlerImpl.getTimeZoneID(request));
            valueMap.put("dateformatid", sessionHandlerImpl.getDateFormatID(request));
            valueMap.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
            valueMap.put("userTimeFormat", sessionHandlerImpl.getUserTimeFormat(request));
            valueMap.put("userDateFormatWTF", sessionHandlerImpl.getDateFormatterWithUserTimeFormat(request));
            valueMap.put("userDateFormat", sessionHandlerImpl.getUserDateFormat(request));
            valueMap.put("isLeaseFixedAsset", isLeaseFixedAsset);
            if (!StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))) {
                valueMap.put("browsertz", sessionHandlerImpl.getBrowserTZ(request));
            }
            valueMap.put("start", start);
            valueMap.put("limit", limit);
            valueMap.put("deleted", deleted);
            valueMap.put("nondeleted", nondeleted);
            valueMap.put("pendingapproval", pendingapproval);
            valueMap.put("gcurrencyid", gcurrencyid);
            valueMap.put("showRowLevelFieldsflag", showRowLevelFieldsflag);
            valueMap.put("filter", filter);
            valueMap.put("fromdate", request.getParameter("fromDate"));
            valueMap.put("todate", request.getParameter("toDate"));
            valueMap.put("reportID", reportID);
            JSONArray reportJArr = new JSONArray(request.getParameter("reportData"));
            JSONObject resultJobj = accCustomReportService.executeCustomizedReport(reportJArr, valueMap);
            JSONArray sortConfigArray = resultJobj.optJSONArray("sortConfigArray");
            JSONArray columnsJArr = resultJobj.optJSONArray("columnsJArr");
            reportJArr = resultJobj.optJSONArray("reportJArr");
            int reportDataCount = StringUtil.isNullOrEmpty(request.getParameter("reportDataCount")) ? Integer.parseInt(request.getParameter("reportDataCount")) : reportJArr.length();
            JSONObject userPreferences = resultJobj.optJSONObject("userPreferences");
            JSONObject filterJson = resultJobj.optJSONObject("filterJson");
            boolean isPivot = resultJobj.optBoolean("isPivot");
            if(isPivot) {
                JSONObject pivotConfig = resultJobj.optJSONObject("pivotConfig");                
                jobj.put("pivotConfig", pivotConfig);
            }            
            jobj.put("sortConfigArray", sortConfigArray);
            jobj.put("columns", columnsJArr);
            jobj.put("data", reportJArr);
            jobj.put("totalCount", reportDataCount);
            jobj.put("userPreferences", userPreferences);
            jobj.put("filter", filterJson);
            jobj.put("reportID", reportID);
            jobj.put("success", true);
            jobj.put("msg", "success");
        } else {
            jobj.put("success", false);
            jobj.put("msg", "timeout");
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public ModelAndView saveOrUpdateChartDetails(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            if (sessionHandlerImpl.isValidSession(request, response)) {
                JSONObject paramObj = StringUtil.convertRequestToJsonObject(request);
                jobj = accCustomReportService.saveOrUpdateChartDetails(paramObj);
                jobj.put("valid", true);
            } else {
                jobj.put("success", false);
                jobj.put("msg", "timeout");
                jobj.put("valid", false);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public ModelAndView getChartDetails(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            if (sessionHandlerImpl.isValidSession(request, response)) {
                JSONObject paramObj = StringUtil.convertRequestToJsonObject(request);
                jobj = accCustomReportService.getChartDetails(paramObj);
                jobj.put("valid", true);
            } else {
                jobj.put("success", false);
                jobj.put("msg", "timeout");
                jobj.put("valid", false);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public ModelAndView deleteChartDetails(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            if (sessionHandlerImpl.isValidSession(request, response)) {
                JSONObject paramObj = StringUtil.convertRequestToJsonObject(request);
                jobj = accCustomReportService.deleteChartDetails(paramObj);
                jobj.put("valid", true);
            } else {
                jobj.put("success", false);
                jobj.put("msg", "timeout");
                jobj.put("valid", false);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    /**
     * save grid state config of custom report for current user
     * @param request
     * @param response
     * @return
     * @throws ServiceException
     * @throws SessionExpiredException 
     */
    public ModelAndView saveGridConfig(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            if (sessionHandlerImpl.isValidSession(request, response)) {
                JSONObject paramObj = StringUtil.convertRequestToJsonObject(request);
                jobj = accCustomReportService.saveGridConfig(paramObj);
                jobj.put("success", true);
            } else {
                jobj.put("success", false);
                jobj.put("msg", "timeout");
            }
        } catch (Exception ex) {
            Logger.getLogger(AccCustomReportController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    /**
     * get saved grid state config of custom report for current user
     * @param request
     * @param response
     * @return
     * @throws ServiceException
     * @throws SessionExpiredException 
     */
    public ModelAndView getGridConfig(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            if (sessionHandlerImpl.isValidSession(request, response)) {
                JSONObject paramObj = StringUtil.convertRequestToJsonObject(request);
                jobj = accCustomReportService.getGridConfig(paramObj);
                jobj.put("success", true);
            } else {
                jobj.put("success", false);
                jobj.put("msg", "timeout");
            }
        } catch (Exception ex) {
            Logger.getLogger(AccCustomReportController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    public ModelAndView CopyCustomReport(HttpServletRequest request, HttpServletResponse response){
        Boolean success = false;
          JSONObject jobj = new JSONObject();
        try {
             if (sessionHandlerImpl.isValidSession(request, response)) {
                String reportId = request.getParameter("reportId");
                String userId = sessionHandlerImpl.getUserid(request);
                String reportDescription = request.getParameter("reportDescription");
                String reportName = request.getParameter("reportName");
                boolean isEdit = request.getParameter("isEdit") != null ? Boolean.parseBoolean(request.getParameter("isEdit")) : false;
                
                Map<String, Object> valueMap = new HashMap<>();
                valueMap.put("reportId", reportId);
                valueMap.put("userId", userId);
                valueMap.put("reportDescription", reportDescription);
                valueMap.put("reportName", reportName);
                valueMap.put("isEdit", isEdit);
                success = accCustomReportService.copyCustomReport(valueMap);
                jobj.put("success", success);
            } else {
                jobj.put("success", false);
                jobj.put("msg", "timeout");
            }
        } catch (Exception e) {
            try {
                jobj.put("success", false);
                jobj.put("msg", messageSource.getMessage("acc.field.Erroroccuredatserverside", null, RequestContextUtils.getLocale(request)));
                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, e);
                return new ModelAndView("jsonView_ex", "model", jobj.toString()); 
            } catch (JSONException ex1) {
                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(AccCustomReportController.class.getName()).log(Level.SEVERE, null, e);
        }
        
        return new ModelAndView("jsonView_ex", "model", jobj.toString());

    }
}
