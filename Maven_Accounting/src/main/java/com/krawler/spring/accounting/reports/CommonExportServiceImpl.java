/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.spring.exportFunctionality.ExportLog;
import com.krawler.spring.accounting.reports.CommonExportService;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.jms.JMSExportProducer;
import com.krawler.spring.accounting.reports.ExportGroupDetailReport;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.CommonExportDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.hibernate.QueryException;

/**
 *
 * @author krawler
 */
public class CommonExportServiceImpl implements CommonExportService {

    private ExportGroupDetailReport exportGroupDetailReport;
    private CommonExportDAO commonExportDAO;
    private JMSExportProducer jmsExportProducer;

    public void setJmsExportProducer(JMSExportProducer jmsExportProducer) {
        this.jmsExportProducer = jmsExportProducer;
    }


    public void setExportGroupDetailReport(ExportGroupDetailReport exportGroupDetailReport) {
        this.exportGroupDetailReport = exportGroupDetailReport;
    }

    public void setCommonExportDAO(CommonExportDAO commonExportDAO) {
        this.commonExportDAO = commonExportDAO;
    }

    @Override
    public void exportFileService(JSONObject requestJSON) throws JSONException {
        jmsExportProducer.sendMessage(requestJSON);
    }

    @Override
    public String addOrRemoveExportLog(JSONObject requestJSON) throws JSONException, ServiceException {

        Map exportLogData = new HashMap();
        Date requestTime = new Date();
        DateFormat sdfTemp = new SimpleDateFormat("ddMMyyyy_hhmmssaa");
        String append="";
        String filename = requestJSON.optString("filename");
        String type = requestJSON.optString("filetype");
        String format = "";
        if (type.equals("pdf") || type.equals("detailedPDF")) {
            append = StringUtil.equal(requestJSON.optString("filetype"), "detailedPDF") ? "(Detail)" : "(Summary)";
            format = "pdf";
        } else if (type.equals("detailedXls") || type.equalsIgnoreCase("Xls")) {
            append = StringUtil.equal(requestJSON.optString("filetype"), "detailedXls") ? "(Detail)" : "(Summary)";
            format = "xlsx";
        }else if (type.equals("detailedCSV") || type.equalsIgnoreCase("csv")) {
            append = StringUtil.equal(requestJSON.optString("filetype"), "detailedCSV") ? "(Detail)" : "(Summary)";
            format = "csv";
        }
        
        if (!StringUtil.isNullOrEmpty(requestJSON.optString("filename", ""))) {
                filename = requestJSON.optString("filename", "") + (sdfTemp.format(requestTime)).toString();
            }
        
        filename = filename.replace(" ", "");
            filename = filename + append;

            requestJSON.put("filename", filename);
        
        String module = requestJSON.optString("module");
        String companyId = requestJSON.optString("companyid");
        String reportName = requestJSON.optString("ReportName");
        String user = requestJSON.optString("userid");
        boolean isLandscape = requestJSON.optBoolean("isLandscape");
        String reportDescription = "";
        if (type.equals("pdf") || type.equals("detailedPDF")) {
            reportDescription = reportName + " " + append + (isLandscape ? "- Landscape" : "- Portrait");
        } else {
            reportDescription = reportName + " " + append;
        }

        exportLogData.put("reportDescription", reportDescription);
        exportLogData.put("user", user);
        exportLogData.put("fileName", filename+ "." + format);
        exportLogData.put("requestTime", requestTime);
        exportLogData.put("status", 1);
        exportLogData.put("companyId", companyId);
        exportLogData.put("fileType", type);
        exportLogData.put("requestJSON", requestJSON);
        exportLogData.put("module", module);
        KwlReturnObject returnObj = commonExportDAO.addOrRemoveExportLog(exportLogData);
        ExportLog exportLog = (ExportLog) returnObj.getEntityList().get(0);
        return exportLog.getId();
    }

    @Override
    public List<ExportLog> getPendingExports() throws ServiceException {
        List<ExportLog> pendingExports = new ArrayList();
        pendingExports = commonExportDAO.getPendingExports();
        return pendingExports;
    }

    @Override
    public JSONObject getExportLog(Map requestParams) throws ServiceException, QueryException, JSONException, SessionExpiredException, ParseException {
        JSONObject exportLogObject = new JSONObject();
        JSONArray exportLogArray = new JSONArray();
        KwlReturnObject resultObject = commonExportDAO.getExportLog(requestParams);
        List<ExportLog> resultList = (ArrayList) resultObject.getEntityList();
        exportLogObject.put("count", resultList.size());
        for (ExportLog exportLog : resultList) {
            JSONObject obj = new JSONObject();
            JSONObject requestObject = new JSONObject(exportLog.getRequestJSON());
            DateFormat udf = (DateFormat) requestParams.get("udf");
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy hh:mm:ss a");
            String period = udf.format(sdf.parse(requestObject.optString("startdate")))+" To " + udf.format(sdf.parse(requestObject.optString("enddate")));
            obj.put("id", exportLog.getId());
            obj.put("description", exportLog.getReportDescription());
            obj.put("filename", exportLog.getFileName());
            obj.put("period", period);
            obj.put("type", exportLog.getFileType());
            obj.put("requestTime", udf.format(exportLog.getRequestTime()));
            obj.put("status", exportLog.getStatus());
            exportLogArray.put(obj);
        }
        exportLogObject.put("data", exportLogArray);
        exportLogObject.put("msg", "");
        exportLogObject.put("success", true);

        return exportLogObject;
    }

    @Override
    public boolean updateRequestStatus(int i, Map params)  throws ServiceException {
       return commonExportDAO.updateRequestStatus(i, params);
    }

}
