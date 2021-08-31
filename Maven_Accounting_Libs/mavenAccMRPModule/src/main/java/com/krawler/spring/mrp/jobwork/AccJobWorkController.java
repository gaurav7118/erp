/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.jobwork;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.accInvoiceController;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.NoSuchMessageException;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AccJobWorkController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private MessageSource messageSource;
    private String successView;
    private AccJobWorkService accJobWorkServiceObj;
    private exportMPXDAOImpl exportDaoObj;
    private auditTrailDAO auditTrailObj;

    public exportMPXDAOImpl getExportDaoObj() {
        return exportDaoObj;
    }

    public void setExportDaoObj(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    private accCompanyPreferencesDAO accCompanyPreferencesObj;

    public accCompanyPreferencesDAO getAccCompanyPreferencesObj() {
        return accCompanyPreferencesObj;
    }

    public void setAccCompanyPreferencesObj(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setAccJobWorkServiceObj(AccJobWorkService accJobWorkServiceObj) {
        this.accJobWorkServiceObj = accJobWorkServiceObj;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;

    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }


    public Map<String, Object> getJobWorkCommonParamsMap(HttpServletRequest request) {
        Map<String, Object> requestParams = new HashMap<String, Object>();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String useid = sessionHandlerImpl.getUserid(request);
            DateFormat userdf = authHandler.getUserDateFormatterWithoutTimeZone(request); // Time zone problem is occurring so that getUserDateFormatter() method is replaced with getUserDateFormatterWithoutTimeZone()
            requestParams.put(Constants.userdf, userdf);
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");

            requestParams.put(JobWork.COMPANYID, companyid);
            requestParams.put("companyId", companyid);
            requestParams.put(JobWork.USERID, useid);
            requestParams.put(Constants.df, df);
            requestParams.put(Constants.ss, request.getParameter(Constants.ss));
            requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
            requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
            requestParams.put("billid", request.getParameter("billid"));

            if (!StringUtil.isNullOrEmpty(start)) {
            requestParams.put("start", start);
            }
            if (!StringUtil.isNullOrEmpty(limit)) {
            requestParams.put("limit", limit);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(JobWork.WORKORDERID))) {
                requestParams.put(JobWork.WORKORDERID, request.getParameter(JobWork.WORKORDERID));
            }
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            if(!StringUtil.isNullOrEmpty(request.getParameter("moduleid"))){
                requestParams.put("moduleid", request.getParameter("moduleid"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("query"))){
                requestParams.put("query", request.getParameter("query"));
            }
            String searchJson = request.getParameter("searchJson");
            String filterConjuctionCriteria = request.getParameter("filterConjuctionCriteria");
            if(!StringUtil.isNullOrEmpty(searchJson)&& !StringUtil.isNullOrEmpty(filterConjuctionCriteria)){
                requestParams.put("searchJson", request.getParameter("searchJson"));
                requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
            }

        } catch (Exception ex) {
            Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requestParams;
    }

    public ModelAndView saveJobWork(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {

            JSONObject Obj = saveJobWork(request);
            txnManager.commit(status);
            isSuccess = true;
            String docNo=Obj.has("documentno") ? Obj.getString("documentno") : "";
            msg = messageSource.getMessage("acc.field.jobworkentryform.successfullysavedmsg", null, RequestContextUtils.getLocale(request)) +" <br> Document No : <b>"+docNo+"</b>";
        } catch (AccountingException | NumberFormatException | TransactionException | JSONException ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
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
    public ModelAndView saveForecastTemplate(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {

            JSONObject Obj = saveForecastTemplate(request);
            txnManager.commit(status);
            isSuccess = true;
            String docNo = Obj.has("documentno") ? Obj.getString("documentno") : "";
            String docid = Obj.has("docid") ? Obj.getString("docid") : "";
            msg = Obj.has("msg") ? Obj.getString("msg") : "";
            String auditMsg = Obj.has("auditMsg") ? Obj.getString("auditMsg") : "";
            auditTrailObj.insertAuditLog(AuditAction.FORECAST_TEMPLATE, "User " + sessionHandlerImpl.getUserFullName(request) + " " + auditMsg, request, docid);
        } catch (AccountingException | NumberFormatException | TransactionException | ServiceException | JSONException | SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            msg = msg.replaceAll("system failure: ", "");
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

    public JSONObject saveForecastTemplate(HttpServletRequest request) throws AccountingException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        List lst = new ArrayList();
        Map<String, Object> dataMap = new HashMap();

        try {
            dataMap = getJobWorkCommonParamsMap(request);

            String entrynumber = request.getParameter(ForecastTemplate.FORECASTID);
            String id = request.getParameter(JobWork.ID);
            dataMap.put(ForecastTemplate.TITLE, request.getParameter(ForecastTemplate.TITLE));
            dataMap.put(ForecastTemplate.FORECASTID, request.getParameter(ForecastTemplate.FORECASTID));
            dataMap.put(ForecastTemplate.FORECASTYEARHISTORY, request.getParameter(ForecastTemplate.FORECASTYEARHISTORY));
            dataMap.put(ForecastTemplate.FORECASTTYPE, request.getParameter(ForecastTemplate.FORECASTTYPE));
            dataMap.put(ForecastTemplate.FORECASTMETHOD, request.getParameter(ForecastTemplate.FORECASTMETHOD));
            dataMap.put(ForecastTemplate.FORECASTPRODUCT, request.getParameter(ForecastTemplate.FORECASTPRODUCT));
            dataMap.put("company", sessionHandlerImpl.getCompanyid(request));
            dataMap.put("createdby", sessionHandlerImpl.getUserid(request));
            dataMap.put("modifiedby", sessionHandlerImpl.getUserid(request));
            dataMap.put("createdon", new Date());
            dataMap.put("updatedon", new Date());
            dataMap.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter(JobWork.ID))) {
                dataMap.put(JobWork.ID, id);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(ForecastTemplate.FORECASTYEAR))) {
                dataMap.put(ForecastTemplate.FORECASTYEAR, df.parse(request.getParameter(ForecastTemplate.FORECASTYEAR)));
            }

            //*******************
            jobj = accJobWorkServiceObj.saveForecastTemplate(dataMap);
            jobj.put("documentno", entrynumber);

        } catch (NumberFormatException | SessionExpiredException | ServiceException | NoSuchMessageException | ParseException | JSONException ex) {
                throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    public JSONObject saveJobWork(HttpServletRequest request) throws AccountingException {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;

        List lst = new ArrayList();
        Map<String, Object> dataMap = new HashMap();

        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            dataMap = getJobWorkCommonParamsMap(request);
            String sequenceformat = "", nextAutoNumber = "", auditMsg = "";;
            boolean isJobWorkIDAlreadyPresent = false, isEdit=false;
            
            if(!StringUtil.isNullOrEmpty(request.getParameter("isEdit"))){   //ERP-30663
                isEdit = Boolean.parseBoolean(request.getParameter("isEdit"));
            }
            String entrynumber = request.getParameter(JobWork.JOBORDERNUMBER);
            String id = request.getParameter(JobWork.ID);
            sequenceformat = request.getParameter(JobWork.SEQUENCEFORMAT);
            dataMap.put(JobWork.JOBORDERNAME, request.getParameter(JobWork.JOBORDERNAME));
            dataMap.put(JobWork.SEQUENCEFORMAT, request.getParameter(JobWork.SEQUENCEFORMAT));
            dataMap.put(JobWork.VENDORID, request.getParameter(JobWork.VENDORID));
            dataMap.put(JobWork.JOBWORKLOCATIONID, request.getParameter(JobWork.JOBWORKLOCATIONID));
            dataMap.put(JobWork.SHIPMENTROUTE, request.getParameter(JobWork.SHIPMENTROUTE));
            dataMap.put(JobWork.GATEPASS, request.getParameter(JobWork.GATEPASS));
            dataMap.put(JobWork.OTHERREMARKS, request.getParameter(JobWork.OTHERREMARKS));
            dataMap.put(JobWork.PRODUCTID, request.getParameter(JobWork.PRODUCTID));
            dataMap.put(JobWork.WORKORDERID, request.getParameter(JobWork.WORKORDERID));
            dataMap.put(JobWork.COMPANYID, companyid);
//          
            dataMap.put(Constants.SEQNUMBER, request.getParameter(Constants.SEQNUMBER));

            DateFormat df = authHandler.getDateOnlyFormat(request);
            Date jobWorkDate = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter(JobWork.ID))) {
                dataMap.put(JobWork.ID, id);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(JobWork.JOBWORKDATE))) {
                jobWorkDate = df.parse(request.getParameter(JobWork.JOBWORKDATE));
                dataMap.put(JobWork.JOBWORKDATE, jobWorkDate);
            }
            if (!StringUtil.isNullObject(request.getParameter(JobWork.DATEOFDELIVERY))) {
                dataMap.put(JobWork.DATEOFDELIVERY, df.parse(request.getParameter(JobWork.DATEOFDELIVERY)));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(JobWork.DATEOFSHIPMENT))) {
                dataMap.put(JobWork.DATEOFSHIPMENT, df.parse((String) request.getParameter(JobWork.DATEOFSHIPMENT)));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(JobWork.EXCISEDUTYCHARGES))) {
                dataMap.put(JobWork.EXCISEDUTYCHARGES, Double.parseDouble(request.getParameter(JobWork.EXCISEDUTYCHARGES)));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(JobWork.PRODUCTQUANTITY))) {
                dataMap.put(JobWork.PRODUCTQUANTITY, Double.parseDouble(request.getParameter(JobWork.PRODUCTQUANTITY)));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(JobWork.CUSTOMFIELD))) {
                dataMap.put(JobWork.CUSTOMFIELD, request.getParameter(JobWork.CUSTOMFIELD));
            }

            //************************
             /* Sequence Format Code.*/
            synchronized (this) {

                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(JobWork.COMPANYID, companyid);
                requestParams.put(JobWork.JOBORDERNUMBER, request.getParameter(JobWork.JOBORDERNUMBER));
                isJobWorkIDAlreadyPresent = accJobWorkServiceObj.isJobWorkIDAlreadyPresent(requestParams);
                if (isJobWorkIDAlreadyPresent) {
                    if (StringUtil.isNullOrEmpty(id) && sequenceformat.equals("NA")) {

                        throw new AccountingException(messageSource.getMessage("acc.mrp.workorder.joborder.fields.joborder.number", null, RequestContextUtils.getLocale(request)) + " '  <b>" + entrynumber + "</b> " + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));

                    } else if (!StringUtil.isNullOrEmpty(id) && sequenceformat.equals("NA")) {
                        requestParams.put(JobWork.ID, id);
                        isJobWorkIDAlreadyPresent = accJobWorkServiceObj.isJobWorkIDAlreadyPresent(requestParams);
                        if (isJobWorkIDAlreadyPresent) {
                            throw new AccountingException(messageSource.getMessage("acc.mrp.workorder.joborder.fields.joborder.number", null, RequestContextUtils.getLocale(request)) + " '  <b>" + entrynumber + "</b> " + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
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
                        if (seqformat_oldflag) {
                            nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_MRP_JOBWORK, sequenceformat);
                        } else {
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_MRP_JOBWORK, sequenceformat, seqformat_oldflag, jobWorkDate);
                            nextAutoNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                            nextAutoNoInt = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                            datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                            dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                            dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part

                            dataMap.put(Constants.SEQFORMAT, sequenceformat);
                            dataMap.put(Constants.SEQNUMBER, nextAutoNoInt);
                            dataMap.put(Constants.DATEPREFIX, datePrefix);
                            dataMap.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                            dataMap.put(Constants.DATESUFFIX, dateSuffix);
                        }
                        entrynumber = nextAutoNumber;
                    }

                }

                if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.MRP_JOB_WORK_MODULEID, entrynumber, companyid);
                    if (!list.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) list.get(0);
                        String formatName = (String) list.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " ' <b>" + entrynumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                }

            }
            dataMap.put(JobWork.JOBORDERNUMBER, entrynumber);
            dataMap.put("autogenerated", nextAutoNumber.equals(entrynumber));

            //*******************
            jobj = accJobWorkServiceObj.saveJobWork(dataMap);
            jobj.put("documentno", entrynumber);
            
            //ERP-30663 : Add audit trail entry for Job Work Out - Add & Edit Case
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("userfullname",sessionHandlerImpl.getUserFullName(request));
            requestParams.put("companyid", companyid);
            requestParams.put("userid", sessionHandlerImpl.getUserid(request));
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            requestParams.put("prdjsondtls",request.getParameter("detail"));
            requestParams.put("remoteAddress",request.getRemoteAddr());
            requestParams.put("reqHeader",request.getHeader("x-real-ip"));
            if(request.getAttribute("companyprefdetails")!=null){
                requestParams.put("companyprefdetails",request.getAttribute("companyprefdetails"));
            }
            if (isEdit) {   //ERP-30663 : JWO Updated
                auditMsg = " User " + sessionHandlerImpl.getUserFullName(request) + " has updated Job Work Out  <b>" + entrynumber+ "</b>";
                auditTrailObj.insertAuditLog(AuditAction.MRP_JOBWORKOUT_UPDATED, auditMsg, requestParams, sessionHandlerImpl.getUserid(request));
            } else {        //ERP-30663 : JWO Added
                auditMsg = " User " + sessionHandlerImpl.getUserFullName(request) + " has added new Job Work Out  <b>" + entrynumber + "</b>";
                auditTrailObj.insertAuditLog(AuditAction.MRP_JOBWORKOUT_ADDED, auditMsg, requestParams, sessionHandlerImpl.getUserid(request));
            }
        } catch (NumberFormatException | SessionExpiredException | ServiceException | NoSuchMessageException | ParseException  | JSONException ex) {
            try {
                throw ServiceException.FAILURE("saveJobWork " + ex.getMessage(), ex);
            } catch (ServiceException ex1) {
                Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return jobj;
    }

    public ModelAndView getJobWorks(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = new HashMap();
            requestParams = getJobWorkCommonParamsMap(request);
            requestParams.put("isExport", false);
            jobj = accJobWorkServiceObj.getColumnModelForJobOrderReport(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, jobj.toString());
    }

    public ModelAndView exportJobWorks(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            JSONArray invJArr = new JSONArray();

            Map<String, Object> requestParams = new HashMap();
            requestParams = getJobWorkCommonParamsMap(request);
            requestParams.put("isExport", true);
            jobj = accJobWorkServiceObj.getColumnModelForJobOrderReport(requestParams);

            exportDaoObj.processRequest(request, response, jobj);
        } catch (Exception ex) {
            Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView deleteJobWorkOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        Map<String, Object> dataMap = new HashMap();
        TransactionStatus status = null;
        try {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("BRecnl_Tx");
            status = txnManager.getTransaction(def);
            dataMap = getJobWorkCommonParamsMap(request);
            String data = request.getParameter("jsonObj");

            Boolean isTempDelete = !StringUtil.isNullOrEmpty(request.getParameter("isTempDelete")) ? Boolean.parseBoolean(request.getParameter("isTempDelete")) : false;
            Boolean isPermDelete = !StringUtil.isNullOrEmpty(request.getParameter("isPermDelete")) ? Boolean.parseBoolean(request.getParameter("isPermDelete")) : false;
            dataMap.put("isTempDelete", isTempDelete);
            dataMap.put("isPermDelete", isPermDelete);
            dataMap.put("data", data);
            
            //ERP-30663 : Add audit trail entry for Job Work Out - Delete Case
            dataMap.put("userfullname",sessionHandlerImpl.getUserFullName(request));
            dataMap.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            dataMap.put("prdjsondtls",request.getParameter("detail"));
            dataMap.put("remoteAddress",request.getRemoteAddr());
            dataMap.put("reqHeader",request.getHeader("x-real-ip"));
            if(request.getAttribute("companyprefdetails")!=null){
                dataMap.put("companyprefdetails",request.getAttribute("companyprefdetails"));
            }
            
            KwlReturnObject kwl = accJobWorkServiceObj.deleteJobWork(dataMap);

            txnManager.commit(status);
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            txnManager.rollback(status);
            Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    public ModelAndView getWorkOrdersForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            Map<String, Object> requestParms = getJobWorkCommonParamsMap(request);
            if(!StringUtil.isNullOrEmpty(request.getParameter("getOpenWO"))){
                requestParms.put("getOpenWO", Boolean.parseBoolean(request.getParameter("getOpenWO").toString()));
            }
            requestParms.put("isWorkOrdersForCombo", true);
            jobj = accJobWorkServiceObj.getWorkOrdersForCombo(requestParms);
        } catch (Exception ex) {
            Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
        public ModelAndView getForecastMerge(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            /*
             Below function is used to create column model for grid
             */
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            String moduleId = request.getParameter("moduleid");
            String searchJson = request.getParameter("searchJson");
            String filterConjuction = request.getParameter("filterConjuctionCriteria");
            if (!StringUtil.isNullOrEmpty(searchJson) && !StringUtil.isNullOrEmpty(filterConjuction)) {
                requestParams.put("searchJson", searchJson);
                requestParams.put("filterConjuctionCriteria", filterConjuction);
            }
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            accJobWorkServiceObj.getColumnModelAndRecordDataForForecast(requestParams, jobj);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
        
                public ModelAndView getForecastDetailsMerge(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            /*
             Below function is used to create column model for grid
             */
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put("billid", request.getParameter("billid"));
            requestParams.put("productid", request.getParameter("productid"));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            accJobWorkServiceObj.getForecastDetails(requestParams, jobj);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView getSingleForecastToLoad(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            String billid = request.getParameter("billid") != null ? request.getParameter("billid") : "";
            requestParams.put("billId", billid);
            DateFormat df = authHandler.getOnlyDateFormat(request);
            requestParams.put("df", df);
            KwlReturnObject result = null;
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            /*
             Get record for single forecast
             */
            JSONObject labourObj = accJobWorkServiceObj.getSingleForecastToLoad(requestParams);
            jobj.put("data", labourObj);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteForecast(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JEC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            /*
             Delete Forecast
             */
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("data", request.getParameter("data"));
            requestParams.put("request", request);
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("userId", sessionHandlerImpl.getUserid(request));
            requestParams.put("username", sessionHandlerImpl.getUserFullName(request));
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            accJobWorkServiceObj.deleteForecast(requestParams);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.mrp.forecast.deletemsg", null, RequestContextUtils.getLocale(request));
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = messageSource.getMessage("acc.forecast.usedintransaction", null, RequestContextUtils.getLocale(request));
            Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, "AccJobWorkController.deleteForecast", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, "AccJobWorkController.deleteForecast", ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView exportForecastTemplate(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobj1 = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        List resultList = new ArrayList();
        boolean isSummaryReport = false;
        String view = "jsonView_ex";
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap();
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            String moduleId = request.getParameter("moduleid");
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            requestParams.put("isExport", true);
            accJobWorkServiceObj.getColumnModelAndRecordDataForForecast(requestParams, jobj1);
            DataJArr = (JSONArray) jobj1.optJSONArray("data");
            request.setAttribute("isExport", true);
            jobj.put("data", DataJArr);
            jobj.put("count", DataJArr.length());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     *
     * @param request = Request for challan report
     * @param response
     * @return
     */
    public ModelAndView getChallanReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            /*
             Put Request params into Map
             */
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("searchJson",request.getParameter("searchJson"));
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put("moduleid", request.getParameter("moduleid"));
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter("productid"))) {
                requestParams.put("productid", request.getParameter("productid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("customerid"))) {
                requestParams.put("customerid", request.getParameter("customerid"));
            }
            /*
             * To jobWorkOderInAged is used while creating purchase invoice from job work in aging report
             */
            if (!StringUtil.isNullOrEmpty(request.getParameter("jobWorkOderInAged"))) {
                requestParams.put("jobWorkOderInAged", request.getParameter("jobWorkOderInAged"));
            }
            requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
            requestParams.put(Constants.REQ_startdate, request.getParameter("startdate"));
            requestParams.put(Constants.REQ_enddate, request.getParameter("enddate"));
            requestParams.put("asOfDate", request.getParameter("asOfDate"));
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            accJobWorkServiceObj.getChallanReport(requestParams, jobj);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    /**
     *
     * @param request = Request for product Summary report
     * @param response
     * @return
     */
    public ModelAndView getJWProductSummaryReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            /*
             Put Request params into Map
             */
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter("productid"))) {
                requestParams.put("productid", request.getParameter("productid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("customerid"))) {
                requestParams.put("customerid", request.getParameter("customerid"));
            }
            requestParams.put(Constants.REQ_startdate, request.getParameter("startdate"));
            requestParams.put(Constants.REQ_enddate, request.getParameter("enddate"));
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            accJobWorkServiceObj.getJWProductSummaryReport(requestParams, jobj);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    /**
     *
     * @param request
     * @param response
     * @return Export Report for Challan data
     */
    public ModelAndView exportChallanReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobj1 = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        List resultList = new ArrayList();
        boolean isSummaryReport = false;
        String view = "jsonView_ex";
        String msg = "";
         boolean jobWorkOderInAged=false; 
        try {
            /*
             Put Request params into Map
             */
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put("isExport", true);
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter("productid"))) {
                requestParams.put("productid", request.getParameter("productid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("customerid"))) {
                requestParams.put("customerid", request.getParameter("customerid"));
            }
            
            /*
             * To jobWorkOderInAged is used while creating purchase invoice from
             * job work in aging report
             */
            if (request.getParameter("jobWorkOderInAged") != null) {
                jobWorkOderInAged = Boolean.parseBoolean(request.getParameter("jobWorkOderInAged").toString());
            }
            requestParams.put(Constants.REQ_startdate, request.getParameter("startdate"));
            requestParams.put(Constants.REQ_enddate, request.getParameter("enddate"));
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("jobWorkOderInAged", jobWorkOderInAged);
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            accJobWorkServiceObj.getChallanReport(requestParams, jobj1);
            DataJArr = (JSONArray) jobj1.optJSONArray("data");
            request.setAttribute("isExport", true);
            jobj.put("data", DataJArr);
            jobj.put("count", DataJArr.length());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     *
     * @param request
     * @param response
     * @return Export Report for product Summary data
     */
    public ModelAndView exportJWProductSummaryReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobj1 = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        List resultList = new ArrayList();
        boolean isSummaryReport = false;
        String view = "jsonView_ex";
        String msg = "";
        try {
            /*
             Put Request params into Map
             */
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put("isExport", true);
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter("productid"))) {
                requestParams.put("productid", request.getParameter("productid"));
}
            if (!StringUtil.isNullOrEmpty(request.getParameter("customerid"))) {
                requestParams.put("customerid", request.getParameter("customerid"));
            }
            requestParams.put(Constants.REQ_startdate, request.getParameter("startdate"));
            requestParams.put(Constants.REQ_enddate, request.getParameter("enddate"));
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            accJobWorkServiceObj.getJWProductSummaryReport(requestParams, jobj1);
            DataJArr = (JSONArray) jobj1.optJSONArray("data");
            request.setAttribute("isExport", true);
            jobj.put("data", DataJArr);
            jobj.put("count", DataJArr.length());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
//    public void exportchallanjasperreport(HttpServletRequest request, HttpServletResponse response) {
//
//        try {
//            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
//            paramJobj.put(Constants.JRXML_REAL_PATH_KEY, request.getSession().getServletContext().getRealPath("jrxml"));
//            boolean threadFlag = false;
//            if (!StringUtil.isNullOrEmpty(paramJobj.optString("threadflag", null))) {
//                threadFlag = Boolean.parseBoolean(paramJobj.optString("threadflag"));
//            }
//            JasperPrint jasperPrint = accJobWorkServiceObj.exportChallanJasperReport(paramJobj);
//            JRPdfExporter exp = new JRPdfExporter();
//            exp.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
//            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
//            response.setHeader("Content-Disposition", "attachment;filename=" + "StatementOfAccounts_v1.pdf");
//            exp.exportReport();
//
//        } catch (Exception e) {
//            Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, e);
//        }
//    }
//    
    public ModelAndView exportJWProductSummaryjasperreport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put(Constants.JRXML_REAL_PATH_KEY, request.getSession().getServletContext().getRealPath("jrxml"));
            boolean threadFlag = false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("threadflag", null))) {
                threadFlag = Boolean.parseBoolean(paramJobj.optString("threadflag"));
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            DateFormat df = authHandler.getDateOnlyFormat(request);
//            requestParams.put(Constants.REQ_startdate, df.parse(request.getParameter("startdate")));
//            requestParams.put(Constants.REQ_enddate, df.parse(request.getParameter("enddate")));
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter("productid"))) {
                requestParams.put("productid", request.getParameter("productid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("customerid"))) {
                requestParams.put("customerid", request.getParameter("customerid"));
            }
            requestParams.put(Constants.REQ_startdate, request.getParameter("startdate"));
            requestParams.put(Constants.REQ_enddate, request.getParameter("enddate"));
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            response.setHeader("Content-Disposition", "attachment;filename=" + "JWProductSummary_v1.pdf");
            Map<String, Object> jasperMap = accJobWorkServiceObj.exportJWProductSummaryjasperreport(requestParams,paramJobj);
            //
            JasperPrint jasperPrint = null;
            JasperReport jasperReport = null;
            InputStream inputStream = null;
            inputStream = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + "/JWProductSummaryReport.jrxml");
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
            jasperReport = JasperCompileManager.compileReport(jasperDesign);
            
            List SOList = new ArrayList();
            OnlyDatePojo odp = new OnlyDatePojo();
            odp.setDate(df.format(new Date()));
            SOList.add(odp);
            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(SOList);
            jasperPrint = JasperFillManager.fillReport(jasperReport, jasperMap, beanColDataSource);

            ArrayList list = new ArrayList();
            list.add(jasperPrint);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT_LIST, list);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();

        } catch (Exception e) {
            Logger.getLogger(AccJobWorkController.class.getName()).log(Level.SEVERE, null, e);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
